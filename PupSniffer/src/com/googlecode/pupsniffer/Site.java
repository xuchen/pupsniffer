/**
 *
 */
package com.googlecode.pupsniffer;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.wcohen.ss.Levenstein;
import com.wcohen.ss.lookup.SoftTFIDFDictionary;

/**
 * @author Xuchen Yao
 *
 */
public class Site {

	private static Logger log = Logger.getLogger(Site.class);;

	/** Each member is a list of URL strings of different language group. */

	protected HashMap<String, ArrayList<String>> groupURLs;

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
	 * it's the langauge that has the fewest webpages.
	 */
	private String refLang;

	//protected ArrayList<UrlPattern> patternList;
	protected HashMap<HashMap<String, String>, Integer> patternMap;

	public Site() {
		patternMap = new HashMap<HashMap<String, String>, Integer>();
		groupDict = new HashMap<String, SoftTFIDFDictionary>();
		groupURLs = new HashMap<String, ArrayList<String>>();
	}

	public Site (ArrayList<String> URLs, EncodingDetector encDetector,
			HtmlLangDetector langDetector) {
		this();
		this.encDetector = encDetector;
		this.langDetector = langDetector;
		String enc, lang, alias;
		String[] splits;


		for (String url:URLs) {
			try {
				lang = null;
				enc = encDetector.detect(url);
				if (enc.startsWith("EUC-JP")) {
					log.warn("Japanese is not supported, abandoning "+url);
					continue;
				} else if (enc.startsWith("EUC-KR")) {
					log.warn("Korean is not supported, abandoning "+url);
					continue;
				} else {
					// go to language detector
					lang = langDetector.detect(url);

					// double check
					if (lang.equals(HtmlLangDetector.CHINESE_TRADITIONAL) &&
							!(enc.startsWith("BIG5") || enc.startsWith("UTF"))) {
						log.warn("Language detection and encoding mismatch: "+lang+"/"+enc);
					} else if (lang.equals(HtmlLangDetector.CHINESE_SIMPLIFIED) &&
							!(enc.startsWith("GB") || enc.startsWith("UTF"))) {
						// GB2312, GB18030, GBK
						log.warn("Language detection and encoding mismatch: "+lang+"/"+enc);
					}
				}
				if (!groupURLs.containsKey(lang)) {
					groupURLs.put(lang, new ArrayList<String>());
					groupDict.put(lang, new SoftTFIDFDictionary());
				} else {
					groupURLs.get(lang).add(url);
					splits = url.split("/");
					// the last one, usually the filename, is used as an alias
					alias = splits[splits.length-1];
					groupDict.get(lang).put(alias, url);
				}
			} catch (FileNotFoundException e) {
				log.warn("URL doesn't exist: "+url);
				continue;
			} catch (NullPointerException e) {
				log.warn("Detecting language of URL failed: "+url);
				continue;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		freezeDict();

		int num;
		log.info("Language group sizes:");
		for (String l: groupURLs.keySet()) {
			num = groupURLs.get(l).size();
			this.numURL += num;
			log.info(l+": "+num);
		}
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
				log.warn(l+" has two few webpages, removing...");
				//log.warn(groupURLs.get(l));
				remove.add(l);
			}
		}
		for (String l:remove) {
			this.numLang--;
			this.numURL -= groupURLs.get(l).size();
			groupURLs.remove(l);
		}

		int min = this.numURL;
		for (String l: groupURLs.keySet()) {
			num = groupURLs.get(l).size();
			if (num < min) {
				this.refLang = l;
			}
		}

		if (this.refLang == null)
			log.warn("There's no reference language in this list. Does this" +
					"website contain pages of only 1 langauge?");
	}

	protected void freezeDict() {
		for (SoftTFIDFDictionary d:groupDict.values()) {
			d.freeze();
		}
	}

	/**
	 * Look up and find pairs using an inverted index, O(n)
	 */
	/*
	public void lookupPairs() {
		ArrayList<String> group0 = groupURLs.get(0);
		String s0, s1=null;
		String alias;
		String[] splits, diffs;
		//ArrayList<String> diffs;
		Levenstein l = new Levenstein();
		int n;

		for (int i=0; i<group0.size(); i++) {
			s0 = group0.get(i);
			splits = s0.split("/");
			// the last one, usually the filename, is used as an alias
			alias = splits[splits.length-1];

			n = dict.lookup(threshold, alias);

			if (n==0) {
				log.warn("No lookup from dict: "+s0);
				continue;
			}

			double minScore = 100000;
			double score = 0;
			int minJ = 0;
			for (int j=0; j<n; j++) {
				s1 = (String)dict.getValue(j);
				score = l.scoreAbs(s0, s1);
				if (score < minScore && score!=0.0) {
					minScore = score;
					minJ = j;
				}
			}
			if (n>1) {
				s1 = (String)dict.getValue(minJ);
				l.score(s0, s1);
				log.info("Multiple results retrieved for " + s0);
				log.info("Using index "+minJ+": "+dict.getRawResult());
			} else {
				log.info(s0+" <-> "+s1);
			}

			diffs = l.getDiffPairAsArray();
			//log.info(String.format("%d: ", i)+diffs);

			if (diffs != null) {
				HashMap<String, String> m = Site.ArrayToHashMap(diffs);
				if (patternMap.containsKey(m))
					patternMap.put(m, patternMap.get(m)+1);
				else
					patternMap.put(m, 1);
				log.info(m);
			}
		}

		log.info("\nAll Patterns found:");
		log.info(patternMap);
	}
	 */
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

	/**
	 * Find and print pairs one by one, O(n^2)
	 */
	public void findPairs() {
		ArrayList<String> group0 = groupURLs.get(0);
		ArrayList<String> group1 = groupURLs.get(1);
		ArrayList<ArrayList<Integer>> groupLowestIndices = new ArrayList<ArrayList<Integer>>();
		String s0, s1;
		Levenstein l = new Levenstein();

		for (int i=0; i<group0.size(); i++) {
			s0 = group0.get(i);
			ArrayList<Integer> lowestIndices = new ArrayList<Integer>();
			double lowest, oldLowest=1000;
			for (int j=0; j<group1.size(); j++) {
				s1 = group1.get(j);
				lowest = Math.abs(l.score(s0, s1));
				if (lowest < oldLowest) {
					oldLowest = lowest;
					lowestIndices.clear();
					lowestIndices.add(j);
				} else if (lowest == oldLowest) {
					lowestIndices.add(j);
				}
			}
			groupLowestIndices.add(lowestIndices);
		}

		if (group0.size()!=groupLowestIndices.size())
			log.error(String.format("Error: the size of group0(%d) and " +
					"groupLowestIndices(%d) should match!", group0.size(), groupLowestIndices.size()));

		for (int i=0; i<groupLowestIndices.size(); i++) {
			ArrayList<Integer> group1Lowest = groupLowestIndices.get(i);
			if (group1Lowest.size()>1)
				log.warn("Warning: the correspoing group1's size exceeds 1: "+group1Lowest);
			int j = group1Lowest.get(0);
			s0 = group0.get(i);
			s1 = group1.get(j);
			l.score(s0, s1);
			log.info(String.format("%d <--> %d ", i, j)+l.getDiffPairAsArrayList());
		}
	}

}
