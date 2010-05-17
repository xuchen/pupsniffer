/**
 * 
 */
package com.googlecode.pupsniffer;

import java.io.File;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * @author Xuchen Yao
 *
 */
public class PupSniffer {
	
	/** Apache logger */
	private static Logger log;
	/** 
	 * The folder containing files of URLs.
	 * Each file contains URLs from one site.
	 */
	private String dir;
	
	//private ArrayList<File> files;
	
	/**
	 * An ArrayList of all sites to be sniffed.
	 */
	private ArrayList<Site> sites;
	
	public PupSniffer (String dir) {
		PropertyConfigurator.configure("conf/log4j.properties");
		log = Logger.getLogger(PupSniffer.class);
		File[] files = new File(dir).listFiles();
		this.dir = dir;

		if (files == null) {
			log.error("-d option "+this.dir+" isn't a directory!");
			System.exit(-1);
		}
		if (files.length == 0) {
			log.error("Directory "+dir+" doesn't contain any files!");
			System.exit(-1);
		}
		
		this.sites = new ArrayList<Site>();
		
//		for (File f:files) {
//			if (f.getName().startsWith(".")) continue;
//			this.sites.add(new Site(f));
//		}
		this.sites.add(new Site(files[0]));

		log.info("Initializatio done.");
	}
	
	public void run() {
        long t0 = System.currentTimeMillis();

		//sites.get(0).findPairs();
		sites.get(0).lookupPairs();
//		for (Site site:sites) {
//			log.info(site.getName());
//			site.findPairs();
//		}
        long tf = System.currentTimeMillis();
        log.info("runtime = "+((tf-t0)/1000.0)+" sec");

		
	}

	public static void main (String[] args) {
		int i = 0;
		String arg;
		String dir = ".";

		while (i < args.length && args[i].startsWith("-")) {
			arg = args[i++];

			if (arg.equals("-d")) {
				if (i < args.length)
					dir = args[i++];
				else
					System.err.println("-d requires a filename");
			}
		}
		if (i != args.length)
			System.err.println("Usage: PupSniffer -d dir_with_url_list");
		
		PupSniffer s = new PupSniffer(dir);
		s.run();

	}

}
