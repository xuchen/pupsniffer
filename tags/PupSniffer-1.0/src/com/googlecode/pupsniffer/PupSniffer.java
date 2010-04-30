/**
 *
 */
package com.googlecode.pupsniffer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.SimpleLayout;

import com.torunski.crawler.Crawler;
import com.torunski.crawler.MultiThreadedCrawler;
import com.torunski.crawler.core.AbstractCrawler;
import com.torunski.crawler.filter.*;
import com.torunski.crawler.link.Link;
import com.torunski.crawler.model.MaxDepthModel;
import com.torunski.crawler.model.MaxIterationsModel;
import com.torunski.crawler.parser.httpclient.SimpleHttpClientParser;

/**
 * @author Xuchen Yao
 * @since 2010-03-30
 */
public class PupSniffer {

	/** Apache logger */
	private static Logger log;

	/**
	 * A URL list PupSniffer should sniff at.
	 */
	private String[] urlList;

	/**
	 * The set of file extensions (such as "html", "css") to sniff at.
	 */
	private String[] fileExtList;

	/**
	 * Webpage encoding detection.
	 */
	private static EncodingDetector encDetector;

	/**
	 * Webpage language detection.
	 */
	private static HtmlLangDetector langDetector;

	/**
	 * A directory to save all downloaded files.
	 */
	private String saveDir;

	/**
	 * A mapping from URL to directory where the URL is saved.
	 */
	protected HashMap<String,String> saveMapping;

	/**
	 * A mapping from URL to its corresponding Site.
	 */
	protected HashMap<String,Site> siteMapping;

	/**
	 * Constructor
	 * @param configFile a configFile
	 */
	public PupSniffer (String configFile) {
		boolean multithread = false;
		long t0 = System.currentTimeMillis();
		/*
		 * the directory "conf" is added to the classpath in
		 * both eclipse and ant, so no need to specify its path.
		 */
		//PropertyConfigurator.configure("conf/log4j.properties");
		log = Logger.getLogger(PupSniffer.class);

		encDetector = new EncodingDetector();
		langDetector = new HtmlLangDetector(configFile);
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(configFile));
		} catch (IOException e) {
			e.printStackTrace();
		}

		// external logging
//		String logFile = prop.getProperty("log");
//		SimpleLayout layout = new SimpleLayout();
//
//		FileAppender appender = null;
//		try {
//			appender = new FileAppender(layout, logFile, false);
//		} catch(Exception e) {e.printStackTrace();}
//
//		log.addAppender(appender);

		urlList = prop.getProperty("urlList").split(",");
		if (urlList==null || urlList.length==0) {
			log.error("urlList in conf/pussniffer.properties has the " +
					"no entry. Exiting.");
			System.exit(-1);
		}

		for (int i=0; i<urlList.length; i++) {
			if (!urlList[i].startsWith("http://")) {
				urlList[i] = "http://"+urlList[i];
			}
			if (urlList[i].endsWith("/"))
				this.urlList[i] = urlList[i].substring(0, urlList[i].length()-1);
		}


		fileExtList = prop.getProperty("suffixList").split(",");
		if (fileExtList==null || fileExtList.length==0) {
			log.error("suffixList in conf/pussniffer.properties has the " +
					"wrong format. Using defaults (html,htm)");
			fileExtList = new String[]{"html", "htm"};
		}

		saveDir = prop.getProperty("saveDir");
		if (!saveDir.endsWith("/"))
			saveDir += "/";

		ILinkFilter fileExtFilter = new FileExtensionFilter(this.fileExtList);
		ILinkFilter serverFilter = new ServerFilter(this.urlList[0]);

		// add serverFilter
		for (int i=1; i<urlList.length; i++) {
			serverFilter = LinkFilterUtil.or(serverFilter, new ServerFilter(this.urlList[i]));
		}


		AbstractCrawler crawler = null;
		if (!multithread)
			crawler = new Crawler();
		else
			crawler = new MultiThreadedCrawler(4, 1);
		/*
		 * SimpleHttpClientParser is the default parser for crawler.
		 * But since HTML2TEXT uses htmlparser, maybe we can also
		 * use htmlparser here for better integration?
		 */
        crawler.setParser(new SimpleHttpClientParser());
        // use MaxIterationsModel instead of MaxDepthModel for high speed and low memory.
		crawler.setModel(new MaxIterationsModel(MaxIterationsModel.NO_LIMIT_ITERATIONS));
		crawler.setLinkFilter(LinkFilterUtil.and(fileExtFilter, serverFilter));

		saveMapping = new HashMap<String,String>();
		siteMapping = new HashMap<String,Site>();

		for (int i=0; i<urlList.length; i++) {

			String url = urlList[i];
			// remove http:// in the front
			String dir = saveDir+url.substring(7);
			File f = new File(dir);
			if(!f.exists() && !f.mkdirs()) {
				log.error("Mkdir "+dir+" failed. Aborting.");
				System.exit(-1);
			}
			saveMapping.put(url, dir);
			Site site = new Site(url, encDetector, langDetector);
			siteMapping.put(url, site);
			crawler.getModel().add(null, url);
		}
		/*
		 * DONE: rewrite parse() of DownloadEventListener.java so that
		 * every time before a file is saved, the strings of that file
		 * is re-directed to HTML2TEXT to extract plain text from the
		 * raw strings. To let HTML2TEXT extract from raw html strings,
		 * the StringBean class
		 * http://htmlparser.sourceforge.net/javadoc/org/htmlparser/beans/StringBean.html
		 * must be re-written to use a Lexer to accept raw Strings.
		 * http://htmlparser.sourceforge.net/javadoc/org/htmlparser/lexer/Lexer.html
		 * In this way files do not need to be read from disk so time-saving.
		 */

        crawler.addParserListener(new PupDownloadEventListener(saveMapping, siteMapping));

        crawler.start();

		ArrayList<String> URLs = new ArrayList<String>();
		String visit;

        Collection<Link> visitedLinks = crawler.getModel().getVisitedURIs();
        log.info("Links visited=" + visitedLinks.size());

        Iterator<Link> list = visitedLinks.iterator();
        while (list.hasNext()) {
        	visit = list.next().getURI();
        	URLs.add(visit);
        	log.info(visit);
        }

        Collection<Link> notVisitedLinks = crawler.getModel().getToVisitURIs();

        log.info("Links NOT visited=" + notVisitedLinks.size());
        Iterator<Link> listNot = notVisitedLinks.iterator();
        while (listNot.hasNext()) {
        	visit = listNot.next().getURI();
        	URLs.add(visit);
        	log.info(visit);
        }


        log.info("Crawling Websites done: "+this.saveMapping.keySet());

        long tf = System.currentTimeMillis();
        log.info("runtime = "+((tf-t0)/1000.0)+" sec");

        //readLine();

        t0 = System.currentTimeMillis();

        for (Site site:siteMapping.values()) {
        	site.freezeSite();
        }

		log.info("Initialization done.");
        tf = System.currentTimeMillis();
        log.info("runtime = "+((tf-t0)/1000.0)+" sec");

		//readLine();
	}



	public void run() {
        long t0 = System.currentTimeMillis();

        for (Site site:siteMapping.values()) {
        	site.lookupPairs();
        }

        long tf = System.currentTimeMillis();
        log.info("Computing patterns done.");
        log.info("runtime = "+((tf-t0)/1000.0)+" sec");
        //readLine();
		log.info("\nAll Patterns found (in detail):");
        for (Site site:siteMapping.values()) {
        	site.printDetails();
        }

		log.info("\nAll Patterns found (in summary):");
        for (Site site:siteMapping.values()) {
        	site.printSummary();
        	if (site.prune()) {
        		log.info("\nAll Patterns after pruning:");
        		site.printSummary();
        	}
        	// save it after pruning
        	site.savePatternList(saveMapping.get(site.getMainUrl()));
        }

	}

	/**
	 * Pause the program and continue after any keyboard input.
	 * @return a string of input
	 */
	public String readLine() {
        try {
            return new java.io.BufferedReader(new
                java.io.InputStreamReader(System.in)).readLine();
        }
        catch(java.io.IOException e) {
            return new String("");
        }
    }


	public static void main (String[] args) {
		PupSniffer sniffer;

		if (args.length != 1) {
			System.out.println("Error: must specify a config file.");
			System.exit(-1);
		}

		File f = new File(args[0]);
		if(!f.exists()) {
			System.out.println(args[0]+" must exist.");
			System.exit(-1);
		}

		sniffer = new PupSniffer(args[0]);

		sniffer.run();

	}

}

