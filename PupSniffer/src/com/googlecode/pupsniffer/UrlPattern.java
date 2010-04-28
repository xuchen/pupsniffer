package com.googlecode.pupsniffer;

import java.util.HashMap;
import java.util.HashSet;

public class UrlPattern {

	/**
	 * HashMap0<String1, HashMap2<String2, Integer1>>
	 * HashMap0: Mapping from a language pair (String1) to all pattern pairs (HashMap2)
	 * String1: Mapping from language 1 to language 2 in the form of "from:to"
	 * HashMap2: Mapping from pattern pairs (HashMap3) to counts (Integer1)
	 * HashMap3: Mapping from language 1 pattern (String3) to language 2 pattern (String4)
	 */
	// I confess this is a piece of code written in C style ;-)
	protected HashMap<String, HashMap<String, HashSet<String>>> pattern;

	protected static final String PAIR_DELIMITER = "<->";

	public UrlPattern () {
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
	 * Add a pattern pair such as sc:en to the language pair such as chinese:english
	 * @param fromLang "chinese"
	 * @param toLang "english"
	 * @param fromPattern "sc"
	 * @param toPattern "en"
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

		for (String langPair:pattern.keySet()) {
			patternMap = pattern.get(langPair);
			for (String patternPair:patternMap.keySet()) {
				size=patternMap.get(patternPair).size();
				if (size<thresh) {
					patternMap.remove(patternPair);
					pruned = true;
				}
			}
		}

		return pruned;
	}

	public String toString() {
		//protected HashMap<String, HashMap<String, Integer>> pattern;
		StringBuilder text = new StringBuilder();
		HashMap<String, HashSet<String>> patternMap;

		text.append("==========Details==========\n");
		for (String langPair:pattern.keySet()) {
			text.append(langPair+"\n");
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
			text.append(langPair+"\n");
			patternMap = pattern.get(langPair);
			for (String patternPair:patternMap.keySet()) {
				text.append(patternPair+"="+patternMap.get(patternPair).size()+"\n");
			}
		}


		return text.toString();
	}
}
