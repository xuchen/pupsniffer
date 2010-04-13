/**
 * 
 */
package com.googlecode.pupsniffer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

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
	SoftTFIDFDictionary dict;
	
	/**
	 * Threshold for dictionary lookup. Decrease this value returns more results.
	 */
	private final double threshold = 0.8;
	
	public Site (File f) {

		this.file = f;
		groupURLs = new ArrayList<ArrayList<String>>();
		
		loadFile(f);
		if (groupURLs.size()==0) {
			log.warn("Empty file "+f+"?");
		}
		dict = new SoftTFIDFDictionary();
		buildIndex();
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
		String s0, s1;
		String alias;
		String[] splits;
		Levenstein l = new Levenstein();
		int n;
		
		for (int i=0; i<group0.size(); i++) {
			s0 = group0.get(i);
			splits = s0.split("/");
			// the last one, usually the filename, is used as an alias
			alias = splits[splits.length-1];
			
			n = dict.lookup(threshold, alias);
			if (n>1) {
				log.info("Multiple results retrieved:");
				log.info(dict.getRawResult());
			} else if (n==0) {
				log.warn("No lookup from dict: "+s0);
				continue;
			}			
			s1 = (String)dict.getValue(0);
			l.score(s0, s1);
			log.info(String.format("%d: ", i)+l.getDiffPair());
		}
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
			log.info(String.format("%d <--> %d ", i, j)+l.getDiffPair());
		}
	}

}
