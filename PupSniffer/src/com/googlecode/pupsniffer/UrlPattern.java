package com.googlecode.pupsniffer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.log4j.Logger;

/**
 * A URL Pattern class.
 * @author Xuchen Yao
 * @since 2010-03-30
 */
public class UrlPattern {

	private static Logger log = Logger.getLogger(UrlPattern.class);

	/**
	 * I confess this is a piece of code written in C style ;-)
	 * HashMap1<String1, HashMap2<String2, HashSet1<String3>>>
	 * HashMap1: Mapping from a language pair (String1) to all pattern pairs (HashMap2)
	 * String1: Mapping from language 1 to language 2 in the form such as "chinese<->english"
	 * HashMap2: Mapping from pattern pairs (String2) to all sets of this pattern (HashSet1)
	 * String2: Mapping from pattern 1 to pattern 2 in the form such as "sc<->en"
	 * HashSet1: A collection of all URL mapping under patterns specified by String1 and String2
	 * String3: Mapping from URL 1 to URL 2 in the form such as "url1<->url2"
	 */

	protected HashMap<String, HashMap<String, HashSet<String>>> pattern;

	protected static final String PAIR_DELIMITER = "<->";

	/** the main URL this pattern is about */
	protected String mainUrl;

	/**
	 * Constructor
	 * @param mainUrl the main URL this pattern is about
	 */
	public UrlPattern (String mainUrl) {
		this.mainUrl = mainUrl;
		pattern = new HashMap<String, HashMap<String, HashSet<String>>>();
	}

	/**
	 * Allocate memory for language pair mapping. Every time a new pair of
	 * languages <code>from:to</code> is added, call this function.
	 * @param from a mapping from
	 * @param to a mapping to
	 */
	public void initLangPair(String from, String to) {
		String pair = from+PAIR_DELIMITER+to;
		if (!pattern.containsKey(pair)) {
			pattern.put(pair, new HashMap<String, HashSet<String>>());
		}
	}

	/**
	 * Add a pattern pair such as sc:en under URL pari such as url1:url2 to the language pair such as chinese:english
	 * @param fromLang "chinese"
	 * @param toLang "english"
	 * @param fromPattern "sc"
	 * @param toPattern "en"
	 * @param fromUrl url1
	 * @param toUrl url2
	 */
	public void addPattern(String fromLang, String toLang,
			String fromPattern, String toPattern, String fromUrl, String toUrl,
			double lookupScore) {
		String langPair = fromLang+PAIR_DELIMITER+toLang;
		String patternPair = fromPattern+PAIR_DELIMITER+toPattern;
		String urlPair = fromUrl+PAIR_DELIMITER+toUrl+" "+String.format("%6.5f", lookupScore);;

		HashMap<String, HashSet<String>> pMap = pattern.get(langPair);
		if (!pMap.containsKey(patternPair)) {
			pMap.put(patternPair, new HashSet<String>());
		}
		pMap.get(patternPair).add(urlPair);
	}

	public boolean prune() {
		int size = 0;
		int categ = 0;
		HashMap<String, HashSet<String>> patternMap;

		for (String langPair:pattern.keySet()) {
			patternMap = pattern.get(langPair);
			for (String patternPair:patternMap.keySet()) {
				categ++;
				size+=patternMap.get(patternPair).size();
			}
		}

		double thresh = size*0.2/categ;
		boolean pruned = false;
		HashSet<String> removed = new HashSet<String>();

		for (String langPair:pattern.keySet()) {
			patternMap = pattern.get(langPair);
			removed.clear();
			for (String patternPair:patternMap.keySet()) {
				size=patternMap.get(patternPair).size();
				if (size<thresh) {
					removed.add(patternPair);
				}
			}
			if(removed.size() != 0) {
				for (String s:removed) {
					patternMap.remove(s);
				}
				pruned = true;
			}
		}

		return pruned;
	}

	public String toString() {
		//protected HashMap<String, HashMap<String, Integer>> pattern;
		StringBuilder text = new StringBuilder();
		HashMap<String, HashSet<String>> patternMap;

		//text.append("Website of "+mainUrl+":\n");

		text.append("==========Details==========\n");
		for (String langPair:pattern.keySet()) {
			text.append(langPair+":\n");
			patternMap = pattern.get(langPair);
			for (String patternPair:patternMap.keySet()) {
				text.append(patternPair+"\n");
				for (String urlPair:patternMap.get(patternPair)) {
					text.append(urlPair+"\n");
				}
			}
		}

		text.append("==========Summary==========\n");
		for (String langPair:pattern.keySet()) {
			text.append(langPair+":\n");
			patternMap = pattern.get(langPair);
			for (String patternPair:patternMap.keySet()) {
				text.append(patternPair+"="+patternMap.get(patternPair).size()+"\n");
			}
		}


		return text.toString();
	}

	public String getSummary() {
		StringBuilder text = new StringBuilder();
		HashMap<String, HashSet<String>> patternMap;

		//		text.append("Website of "+mainUrl+":\n");
		text.append("==========Summary==========\n");
		for (String langPair:pattern.keySet()) {
			text.append(langPair+":\n");
			patternMap = pattern.get(langPair);
			for (String patternPair:patternMap.keySet()) {
				text.append(patternPair+"="+patternMap.get(patternPair).size()+"\n");
			}
		}


		return text.toString();
	}

	public String getDetails() {
		StringBuilder text = new StringBuilder();
		HashMap<String, HashSet<String>> patternMap;

		//text.append("Website of "+mainUrl+":\n");
		text.append("==========Details==========\n");
		for (String langPair:pattern.keySet()) {
			text.append(langPair+":\n");
			patternMap = pattern.get(langPair);
			for (String patternPair:patternMap.keySet()) {
				text.append(patternPair+"\n");
				for (String urlPair:patternMap.get(patternPair)) {
					text.append(urlPair+"\n");
				}
			}
		}

		return text.toString();
	}

	/**
	 * Save all patterns and URL list to <code>dir</code>
	 * @param dir the directory to save to
	 */
	public void savePatternList (String dir) {
		dir = dir+"/PupSnifferPatterns";
		File f = new File(dir);
		if (!f.exists() && !f.mkdir()) {
			log.error("Making dir "+dir+" failed when saving the pattern list!");
			return;
		}

		StringBuilder text;
		String fileName;
		HashMap<String, HashSet<String>> patternMap;


		for (String langPair:pattern.keySet()) {
			text = new StringBuilder();
			// for every language pair write to a file
			fileName = dir+"/"+langPair.replaceAll("<->", "_")+".txt";

			patternMap = pattern.get(langPair);
			for (String patternPair:patternMap.keySet()) {
				text.append("# Pattern: "+patternPair+"\n");
				for (String urlPair:patternMap.get(patternPair)) {
					text.append(urlPair+"\n");
				}
			}

			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
				out.write(text.toString());
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}
}
