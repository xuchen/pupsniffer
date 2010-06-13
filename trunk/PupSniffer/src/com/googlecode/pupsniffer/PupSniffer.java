/**
 *
 */
package com.googlecode.pupsniffer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
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
	 * Threshold for dictionary lookup. Decrease this value returns more results.
	 */
	private double threshold = 1.0;

	/**
	 * Constructor
	 * @param configFile a configFile
	 */
	public PupSniffer (String configFile) {

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
			if (!urlList[i].startsWith("http")) {
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

		threshold = Double.parseDouble(prop.getProperty("threshold"));
		if (threshold > 1.0 || threshold <= 0) threshold = 1.0;

	}



	public void run() {
		boolean multithread = false;
		long t0 = System.currentTimeMillis();
		AbstractCrawler crawler = null;

		ILinkFilter fileExtFilter;
		Site site;
		fileExtFilter = new FileExtensionFilter(this.fileExtList);
		ILinkFilter serverFilter;
		File f;
		String url, dir;
		Collection<Link> visitedLinks, notVisitedLinks;

		saveMapping = new HashMap<String,String>();
		siteMapping = new HashMap<String,Site>();

		String summaryFile = saveDir+"/WebsiteSummary.txt";
		BufferedWriter out = null;
		try {
			File sf = new File(summaryFile);
			if (sf.exists()) sf.delete();
			out = new BufferedWriter(new FileWriter(summaryFile, true));
			out.write("#URL numOfLanguagesOfEachWebsite\n");
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (int i=0; i<urlList.length; i++) {
			url = this.urlList[i];

			if (!multithread)
				crawler = new Crawler();
			else
				crawler = new MultiThreadedCrawler(4, 1);

			// add serverFilter
			serverFilter= new ServerFilter(url);

			/*
			 * SimpleHttpClientParser is the default parser for crawler.
			 * But since HTML2TEXT uses htmlparser, maybe we can also
			 * use htmlparser here for better integration?
			 */
	        crawler.setParser(new SimpleHttpClientParser());
	        // use MaxIterationsModel instead of MaxDepthModel for high speed and low memory.
			crawler.setModel(new MaxIterationsModel(MaxIterationsModel.NO_LIMIT_ITERATIONS));
			crawler.setLinkFilter(LinkFilterUtil.and(fileExtFilter, serverFilter));

			dir = saveDir+url.substring(7);
			f = new File(dir);
			if(!f.exists() && !f.mkdirs()) {
				log.error("Mkdir "+dir+" failed. Aborting.");
				System.exit(-1);
			}
			saveMapping.put(url, dir);
			site = new Site(url, threshold, encDetector, langDetector);
			siteMapping.put(url, site);
			crawler.getModel().add(null, url);


	        crawler.addParserListener(new PupDownloadEventListener(saveMapping, siteMapping));
	        try {
	        	crawler.start();
	        } catch (java.lang.StringIndexOutOfBoundsException e) {
	        	e.printStackTrace();
	        }

			ArrayList<String> URLs = new ArrayList<String>();
			String visit;

	        visitedLinks = crawler.getModel().getVisitedURIs();
	        log.info("Links visited=" + visitedLinks.size());

	        Iterator<Link> list = visitedLinks.iterator();
	        while (list.hasNext()) {
	        	visit = list.next().getURI();
	        	URLs.add(visit);
	        	log.info(visit);
	        }

	        notVisitedLinks = crawler.getModel().getToVisitURIs();

	        log.info("Links NOT visited=" + notVisitedLinks.size());
	        Iterator<Link> listNot = notVisitedLinks.iterator();
	        while (listNot.hasNext()) {
	        	visit = listNot.next().getURI();
	        	URLs.add(visit);
	        	log.info(visit);
	        }


	        log.info("Crawling Website done: "+this.saveMapping.keySet());

	        long tf = System.currentTimeMillis();
	        log.info("runtime = "+((tf-t0)/1000.0)+" sec");

	        t0 = System.currentTimeMillis();
	        site.freezeSite();
	        site.lookupPairs();
	        tf = System.currentTimeMillis();
	        log.info("Computing patterns done.");
	        log.info("runtime = "+((tf-t0)/1000.0)+" sec");
	        //readLine();
			log.info("\nAll Patterns found (in detail):");

			site.printDetails();
			log.info("\nAll Patterns found (in summary):");
	       	site.printSummary();
        	if (site.prune()) {
        		log.info("\nAll Patterns after pruning:");
        		site.printSummary();
        	}
        	// save it after pruning
        	site.saveSummary(saveMapping.get(site.getMainUrl()));
        	site.savePatternList(saveMapping.get(site.getMainUrl()));

    		try {
    			if (out!=null)
    				out.write(url+" "+site.numLanguages()+"\n");
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
		}
		try {
			if (out!=null)
				out.close();
		} catch (IOException e) {
			e.printStackTrace();
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

