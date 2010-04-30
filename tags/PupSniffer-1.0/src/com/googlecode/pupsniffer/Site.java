/**
 *
 */
package com.googlecode.pupsniffer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.log4j.Logger;

import com.wcohen.ss.Levenstein;
import com.wcohen.ss.lookup.SoftTFIDFDictionary;

/**
 * @author Xuchen Yao
 * @since 2010-03-30
 */
public class Site {

	private static Logger log = Logger.getLogger(Site.class);

	/** The main url of this site. */
	protected String mainUrl;

	/** Each member is a list of URL strings of different language group. */

	protected HashMap<String, HashSet<String>> groupURLs;

	/**
	 * Inverted index for the (larger) group.
	 */
	private HashMap<String, SoftTFIDFDictionary> groupDict;

	/**
	 * Threshold for dictionary lookup. Decrease this value returns more results.
	 */
	private final double threshold = 0.99;

	/**
	 * A URL encoding detector.
	 */
	private EncodingDetector encDetector;

	/**
	 * A language detector for HTML
	 */
	private HtmlLangDetector langDetector;

	/**
	 * the overall number of URLs
	 */
	private int numURL;

	/**
	 * the overall number of languages
	 */
	private int numLang;

	/**
	 * the language taken as a reference in pair matching. Ususally
	 * it's the language that has the most webpages.
	 */
	private String refLang;

	//protected ArrayList<UrlPattern> patternList;
	protected HashMap<HashMap<String, String>, Integer> patternMap;

	protected UrlPattern pattern;

	public Site(String mainUrl) {
		patternMap = new HashMap<HashMap<String, String>, Integer>();
		groupDict = new HashMap<String, SoftTFIDFDictionary>();
		groupURLs = new HashMap<String, HashSet<String>>();
		pattern = new UrlPattern(mainUrl);
		this.mainUrl = mainUrl;
	}

	public Site(String mainUrl, EncodingDetector encDetector, HtmlLangDetector langDetector) {
		this(mainUrl);
		this.encDetector = encDetector;
		this.langDetector = langDetector;
	}

	public String getMainUrl() {return mainUrl;}

	public void addUrl (String url, String oriEnc, String raw) {
		String lang, alias, enc;
		String[] splits;

		try {
			enc = encDetector.detectFromRaw(raw, oriEnc);
			raw = new String(raw.getBytes(oriEnc), enc);

			if (enc != null) {
				if (enc.startsWith("EUC-JP")) {
					log.warn("Japanese is not supported, abandoning "+url);
					return;
				} else if (enc.startsWith("EUC-KR")) {
					log.warn("Korean is not supported, abandoning "+url);
					return;
				}
			}

			// go to language detector
			lang = langDetector.detectFromRaw(raw);

			// double check
			if (lang.equals(HtmlLangDetector.CHINESE_TRADITIONAL) &&
					!(enc.startsWith("BIG5") || enc.startsWith("UTF"))) {
				log.warn("Language detection and encoding mismatch: "+lang+"/"+enc+" in "+url);
			} else if (lang.equals(HtmlLangDetector.CHINESE_SIMPLIFIED) &&
					!(enc.startsWith("GB") || enc.startsWith("UTF"))) {
				// GB2312, GB18030, GBK
				log.warn("Language detection and encoding mismatch: "+lang+"/"+enc+" in "+url);
			}

			if (!groupURLs.containsKey(lang)) {
				groupURLs.put(lang, new HashSet<String>());
				groupDict.put(lang, new SoftTFIDFDictionary());
			}
			if (!groupURLs.get(lang).contains(url)) {
				groupURLs.get(lang).add(url);
				splits = url.split("/");
				// the last one, usually the filename, is used as an alias
				alias = splits[splits.length-1];
				groupDict.get(lang).put(alias, url);
			}
		} catch (NullPointerException e) {
			log.warn("Detecting language of URL failed: "+url);
			//e.printStackTrace();
			return;
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void freezeSite () {

		freezeDict();

		int num;
		this.numLang = groupURLs.size();

		/*
		 * Remove any languages that have too little presence, which most
		 * likely due to wrong language detection or some random rare
		 * web pages with a different language.
		 */
		double thresh = this.numURL*0.5/this.numLang;
		ArrayList<String> remove = new ArrayList<String>();
		for (String l: groupURLs.keySet()) {
			num = groupURLs.get(l).size();
			if (num < thresh) {
				log.warn(l+"("+num+") has too few webpages, removing...");
				//log.warn(groupURLs.get(l));
				remove.add(l);
			}
		}
		for (String l:remove) {
			this.numLang--;
			this.numURL -= groupURLs.get(l).size();
			groupURLs.remove(l);
			groupDict.remove(l);
		}

		int max = 0;
		for (String l: groupURLs.keySet()) {
			num = groupURLs.get(l).size();
			if (num > max) {
				this.refLang = l;
				max = num;
			}
		}

		if (groupURLs.size()<=1)
			log.warn("This website seems to contain pages of no more than 1 langauge.");
		else {
			for (String l: groupURLs.keySet()) {
				if (l.equals(refLang)) continue;
				pattern.initLangPair(l, refLang);
			}
		}

		log.info("Website of "+mainUrl);
		log.info("Language group sizes:");
		for (String l: groupURLs.keySet()) {
			num = groupURLs.get(l).size();
			this.numURL += num;
			log.info(l+": "+num);
			log.info(groupURLs.get(l));
		}
	}

	protected void freezeDict() {
		for (SoftTFIDFDictionary d:groupDict.values()) {
			d.freeze();
		}
	}

	/**
	 * Look up and find pairs using an inverted index, O(n)
	 */

	public void lookupPairs() {
		HashSet<String> refURLs = groupURLs.get(refLang);
		HashSet<String> fromURLs;
		String s1=null;
		String alias;
		String[] splits, diffs;
		//ArrayList<String> diffs;
		Levenstein levenstein = new Levenstein();
		SoftTFIDFDictionary dict;
		SoftTFIDFDictionary refDict = groupDict.get(refLang);
		int n, minJ;
		double minScore, score;

		/*
		 * every other language is compared against refLang,
		 * which has the most URLs
		 */
		for (String fromLang: groupURLs.keySet()) {
			if (fromLang.equals(refLang)) continue;
			fromURLs = groupURLs.get(fromLang);

			for (String s0:fromURLs) {
				splits = s0.split("/");
				// the last one, usually the filename, is used as an alias
				alias = splits[splits.length-1];

				n = refDict.lookup(threshold, alias);

				if (n==0) {
					log.warn("No lookup from dict for " + fromLang + ": "+s0);
					continue;
				}

				minScore = 100000;
				score = 0;
				minJ = 0;
				for (int j=0; j<n; j++) {
					s1 = (String)refDict.getValue(j);
					score = levenstein.scoreAbs(s0, s1);
					if (score < minScore && score!=0.0) {
						minScore = score;
						minJ = j;
					}
				}
				if (n>1) {
					s1 = (String)refDict.getValue(minJ);
					levenstein.score(s0, s1);
					log.info("Multiple results retrieved for " + fromLang +": " + s0);
					log.info("Using index "+minJ+": "+refDict.getRawResult());
				} else {
					log.info(s0+" <-> "+s1);
				}

				diffs = levenstein.getDiffPairAsArray();
				//log.info(String.format("%d: ", i)+diffs);

				if (diffs != null) {
					pattern.addPattern(fromLang, refLang, diffs[0], diffs[1], s0, s1);
					log.info(diffs[0]+":"+diffs[1]);
				}
			}

		}

	}

	/**
	 * Print Details and Summary of patterns.
	 */
	public void printPatterns () {
		log.info(pattern.toString());
	}

	/**
	 * Print Summary of patterns.
	 */
	public void printSummary () {
		int num;
		log.info("Website of "+mainUrl);
		log.info("Language group sizes:");
		for (String l: groupURLs.keySet()) {
			num = groupURLs.get(l).size();
			log.info(l+": "+num);
		}
		log.info(pattern.getSummary());
	}

	/**
	 * Print Details of patterns.
	 */
	public void printDetails () {
		int num;
		log.info("Website of "+mainUrl);
		log.info("Language group sizes:");
		for (String l: groupURLs.keySet()) {
			num = groupURLs.get(l).size();
			log.info(l+": "+num);
		}
		log.info(pattern.getDetails());
	}

	/**
	 * Save all patterns and URL list to <code>dir</code>
	 * @param dir the directory to save to
	 */
	public void savePatternList (String dir) {
		pattern.savePatternList(dir);
	}

	/**
	 * Prune patterns by removing too few ones (number of instances
	 * is less than 20% of average).
	 * @return whether pruning is performed.
	 */
	public boolean prune () {
		return this.pattern.prune();
	}

	/**
	 * Convert an ArrayList of size 2 to a HashMap
	 * @param pair an ArrayList of size 2
	 * @return a HashMap with the key/value as <code>pair.get(0)/pair.get(1)</code>
	 */
	public static HashMap<String, String> ArrayListToHashMap(ArrayList<String> pair) {
		if (pair.size() != 2) {
			log.error("ArrayList "+pair+" must be of size 2.");
			return null;
		}
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(pair.get(0), pair.get(1));

		return map;
	}

	/**
	 * Convert an ArrayList of size 2 to a HashMap
	 * @param pair an ArrayList of size 2
	 * @return a HashMap with the key/value as <code>pair.get(0)/pair.get(1)</code>
	 */
	public static HashMap<String, String> ArrayToHashMap(String[] pair) {
		if (pair.length != 2) {
			log.error("Array "+pair+" must be of size 2.");
			return null;
		}
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(pair[0], pair[1]);

		return map;
	}


}
