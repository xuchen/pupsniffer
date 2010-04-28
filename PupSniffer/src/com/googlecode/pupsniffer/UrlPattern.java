package com.googlecode.pupsniffer;

import java.util.HashMap;
import java.util.HashSet;

public class UrlPattern {

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
			String fromPattern, String toPattern, String fromUrl, String toUrl) {
		String langPair = fromLang+PAIR_DELIMITER+toLang;
		String patternPair = fromPattern+PAIR_DELIMITER+toPattern;
		String urlPair = fromUrl+PAIR_DELIMITER+toUrl;

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
}
