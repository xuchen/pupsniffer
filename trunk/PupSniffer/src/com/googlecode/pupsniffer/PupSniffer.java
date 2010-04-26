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
	 * An URL PupSniffer should sniff at.
	 */
	private String url;

	/**
	 * The set of file extensions (such as "html", "css") to sniff at.
	 */
	private String[] fileExtList;

	//private ArrayList<File> files;

	/**
	 * An ArrayList of all sites to be sniffed.
	 */
	private ArrayList<Site> sites;

	private static EncodingDetector encDetector;

	private static HtmlLangDetector langDetector;

	public PupSniffer () {
		PropertyConfigurator.configure("conf/log4j.properties");
		log = Logger.getLogger(PupSniffer.class);
		this.sites = new ArrayList<Site>();
		fileExtList = new String[]{"html", "htm"};
		encDetector = new EncodingDetector();
		langDetector = new HtmlLangDetector();
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


        long t0 = System.currentTimeMillis();
		this.sites.add(new Site(URLs, encDetector, langDetector));

		log.info("Initialization done.");
        long tf = System.currentTimeMillis();
        log.info("runtime = "+((tf-t0)/1000.0)+" sec");

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
		String url = null;

		while (i < args.length && args[i].startsWith("-")) {
			arg = args[i++];

			if (arg.equals("-u")) {
				if (i < args.length)
					url = args[i++];
				else
					log.error("-u requires a URL");
				if (!url.startsWith("http://")) {
					log.error("-u URL must start with http://");
				}
			}
		}
		if (i != args.length) {
			System.err.println("Usage: ");
			System.err.println("Usage: PupSniffer -u URL");
		}

		PupSniffer sniffer;
		sniffer = new PupSniffer(url);

		sniffer.run();

	}

}

