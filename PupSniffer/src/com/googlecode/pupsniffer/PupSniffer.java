/**
 * 
 */
package com.googlecode.pupsniffer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.torunski.crawler.Crawler;
import com.torunski.crawler.filter.*;
import com.torunski.crawler.link.Link;
import com.torunski.crawler.model.MaxDepthModel;

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
	private File dir;
	
	/**
	 * An URL PupSniffer should sniff at.
	 */
	private String url;
	// sdfja
	/* *
	 */
	private String uuu;
	/**
	 * The set of file extensions (such as "html", "css") to sniff at.
	 */
	private String[] fileExtList;
	
	//private ArrayList<File> files;
	
	/**
	 * An ArrayList of all sites to be sniffed.
	 */
	private ArrayList<Site> sites;
	
	public PupSniffer () {
		PropertyConfigurator.configure("conf/log4j.properties");
		log = Logger.getLogger(PupSniffer.class);
		this.sites = new ArrayList<Site>();
		fileExtList = new String[]{"html", "htm"};
	}
	
	public PupSniffer (File dir) {
		this();

		File[] files = dir.listFiles();
		this.dir = dir;

		if (files == null) {
			log.error("-d option "+this.dir+" isn't a directory!");
			System.exit(-1);
		}
		if (files.length == 0) {
			log.error("Directory "+dir+" doesn't contain any files!");
			System.exit(-1);
		}
		
//		for (File f:files) {
//			if (f.getName().startsWith(".")) continue;
//			this.sites.add(new Site(f));
//		}
		this.sites.add(new Site(files[0]));

		log.info("Initializatio done.");
	}
	
	public PupSniffer (String url) {
		this();
		if (url.endsWith("/"))
			this.url = url.substring(0, url.length()-1);
		else
			this.url = url;

		ILinkFilter fileExtFilter = new FileExtensionFilter(this.fileExtList);
		ILinkFilter serverFilter = new ServerFilter(this.url);
		
		Crawler crawler = new Crawler();
		crawler.setModel(new MaxDepthModel());
		crawler.setLinkFilter(LinkFilterUtil.and(fileExtFilter, serverFilter));
		crawler.start(this.url, "/");
		
		readLine();
		
		ArrayList<String> URLs = new ArrayList<String>();
		String visit;
		
        Collection<Link> visitedLinks = crawler.getModel().getVisitedURIs();
        //log.info("Links visited=" + visitedLinks.size());
        
        Iterator<Link> list = visitedLinks.iterator();
        while (list.hasNext()) {
        	visit = list.next().getURI();
        	URLs.add(visit);
        	log.info(visit);
        }
        
        Collection<Link> notVisitedLinks = crawler.getModel().getToVisitURIs();

        //log.info("Links NOT visited=" + notVisitedLinks.size());
        Iterator<Link> listNot = notVisitedLinks.iterator();
        while (listNot.hasNext()) {
        	visit = listNot.next().getURI();
        	URLs.add(visit);
        	log.info(visit);
        }

        
        log.info("Crawling Website "+this.url+" done.");
        
		
//		for (File f:files) {
//			if (f.getName().startsWith(".")) continue;
//			this.sites.add(new Site(f));
//		}
		this.sites.add(new Site(URLs));

		log.info("Initialization done.");
		readLine();
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
	
	protected String readLine() {
        try {
            return new java.io.BufferedReader(new
                java.io.InputStreamReader(System.in)).readLine();
        }
        catch(java.io.IOException e) {
            return new String("");
        }
    }


	public static void main (String[] args) {
		int i = 0;
		String arg;
		File dir = null;
		String url = null;

		while (i < args.length && args[i].startsWith("-")) {
			arg = args[i++];

			if (arg.equals("-d")) {
				if (i < args.length)
					dir = new File(args[i++]);
				else
					System.err.println("-d requires a filename");
			} else if (arg.equals("-u")) {
				if (i < args.length)
					url = args[i++];
				else
					System.err.println("-u requires a URL");
			}
		}
		if (i != args.length) {
			System.err.println("Usage: ");
			System.err.println("Usage: PupSniffer -d dir_with_url_list");
			System.err.println("Usage: PupSniffer -u URL");
		}
		
		PupSniffer sniffer;
		if (url!=null)
			sniffer = new PupSniffer(url);
		else 
			sniffer = new PupSniffer(dir);
		sniffer.run();

	}

}

