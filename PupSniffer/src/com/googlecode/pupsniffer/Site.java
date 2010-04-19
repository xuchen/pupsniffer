/**
 * 
 */
package com.googlecode.pupsniffer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
	
	/**
	 * A list of all URLs.
	 */
	//private ArrayList<String> URLs;
	
	/** Each member is a list of URL strings of different language group. */
	private ArrayList<ArrayList<String>> groupURLs;
	
	/** 
	 * A file containing all URLs. 
	 */
	private File file;
	
	/**
	 * Inverted index for the (larger) group.
	 */
	private SoftTFIDFDictionary dict;
	
	/**
	 * Threshold for dictionary lookup. Decrease this value returns more results.
	 */
	private final double threshold = 0.99;
	
	/**
	 * A URL encoding detector.
	 */
	private EncodingDetector detector;
	
	//protected ArrayList<UrlPattern> patternList;
	protected HashMap<HashMap<String, String>, Integer> patternMap;
	
	public Site() {
		groupURLs = new ArrayList<ArrayList<String>>();
		patternMap = new HashMap<HashMap<String, String>, Integer>();
		detector = new EncodingDetector();
	}
	
	public Site (File f) {
		this();
		this.file = f;
 
		loadFile(f);
		if (groupURLs.size()==0) {
			log.warn("Empty file "+f+"?");
		}
		dict = new SoftTFIDFDictionary();
		buildIndex();
	}
	
	public Site (ArrayList<String> URLs) {
		this();
		if (URLs!=null) {
			groupURLs.add(URLs);
			// add it twice since we don't know the language category so fars
			groupURLs.add(URLs);
		}
		dict = new SoftTFIDFDictionary();
		buildIndex();
		try {
			for (String u:URLs) {
				log.info(detector.detect(u));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void loadFile (File f) {
		BufferedReader in;
		String line;
		
		ArrayList<String> URLs = null;
		
		try {
			in = new BufferedReader(new FileReader(f));
			
			while (in.ready()) {
				line = in.readLine().trim();
				if (line.length() == 0 || line.startsWith("#"))
					continue;  // skip blank lines and comments
				if (line.endsWith("] :")) {
					if (URLs!=null) groupURLs.add(URLs);
					URLs = new ArrayList<String>();
				} else {
					URLs.add(line);
				}
			}
			in.close();
			if (URLs!=null) groupURLs.add(URLs);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
			log.error("The first line in your file must be of pattern [.*] :");
			log.error("for indication of different langauge groups.");
		}
	}
	
	public String getName() {return file.getAbsolutePath();}
	
	public void buildIndex() {
		// first we suppose the second group is the larger one.
		ArrayList<String> group1 = groupURLs.get(1);
		String alias;
		String[] splits;
		for (String url:group1) {
			splits = url.split("/");
			// the last one, usually the filename, is used as an alias
			alias = splits[splits.length-1];
			dict.put(alias, url);
		}
		dict.freeze();
	}
	
	/**
	 * Look up and find pairs using an inverted index, O(n)
	 */
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
