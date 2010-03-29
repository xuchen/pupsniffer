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

/**
 * @author Xuchen Yao
 *
 */
public class Site {
	
	private static Logger log = Logger.getLogger(Site.class);;
	
	/**
	 * A list of all URLs.
	 */
	private ArrayList<String> URLs;
	
	/** 
	 * A file containing all URLs. 
	 */
	private File file;
	
	public Site (File f) {

		this.file = f;
		URLs = new ArrayList<String>();
		
		loadFile(f);
		if (URLs.size()==0) {
			log.warn("Empty file "+f+"?");
		}
		
	}
	
	public void loadFile (File f) {
		BufferedReader in;
		String line;
		
		try {
			in = new BufferedReader(new FileReader(f));
			
			while (in.ready()) {
				line = in.readLine().trim();
				if (line.length() == 0 || line.startsWith("#"))
					continue;  // skip blank lines and comments
				URLs.add(line);
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
