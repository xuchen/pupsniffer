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
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.torunski.crawler.Crawler;
import com.torunski.crawler.events.DownloadEventListener;
import com.torunski.crawler.filter.*;
import com.torunski.crawler.link.Link;
import com.torunski.crawler.model.MaxDepthModel;
import com.torunski.crawler.model.MaxIterationsModel;
import com.torunski.crawler.parser.httpclient.SimpleHttpClientParser;

/**
 * @author Xuchen Yao
 *
 */
public class PupSniffer {

	/** Apache logger */
	private static Logger log;

	/** Cconfiguration file. */
	public static final String propertyFile = "conf/pupsniffer.properties";

	/**
	 * A URL list PupSniffer should sniff at.
	 */
	private String[] urlList;

	/**
	 * The set of file extensions (such as "html", "css") to sniff at.
	 */
	private String[] fileExtList;

	//private ArrayList<File> files;

	/**
	 * An ArrayList of all sites to be sniffed.
	 */
	private ArrayList<Site> sites;

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

	public PupSniffer () {
		PropertyConfigurator.configure("conf/log4j.properties");
		log = Logger.getLogger(PupSniffer.class);
		this.sites = new ArrayList<Site>();

		encDetector = new EncodingDetector();
		langDetector = new HtmlLangDetector();
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(propertyFile));
		} catch (IOException e) {
			e.printStackTrace();
		}

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
		// TODO: complete the loop through urlList.
		if (urlList[0].endsWith("/"))
			this.urlList[0] = urlList[0].substring(0, urlList[0].length()-1);
		else
			this.urlList[0] = urlList[0];

		ILinkFilter fileExtFilter = new FileExtensionFilter(this.fileExtList);
		ILinkFilter serverFilter = new ServerFilter(this.urlList[0]);

		Crawler crawler = new Crawler();
		/*
		 * SimpleHttpClientParser is the default parser for crawler.
		 * But since HTML2TEXT uses htmlparser, maybe we can also
		 * use htmlparser here for better integration?
		 */
        crawler.setParser(new SimpleHttpClientParser());
        // use MaxIterationsModel instead of MaxDepthModel for high speed and low memory.
		crawler.setModel(new MaxIterationsModel(MaxIterationsModel.NO_LIMIT_ITERATIONS));
		crawler.setLinkFilter(LinkFilterUtil.and(fileExtFilter, serverFilter));

		HashMap<String,String> mapping = new HashMap<String,String>();

		// remove http:// in the front
		for (int i=0; i<urlList.length; i++) {
			String url = urlList[i];
			String dir = saveDir+url.substring(7);
			File f = new File(dir);
			if(!f.exists() && !f.mkdirs()) {
				log.error("Mkdir "+dir+" failed. Aborting.");
				System.exit(-1);
			}
			mapping.put(url, dir);
		}
		/*
		 * TODO: rewrite parse() of DownloadEventListener.java so that
		 * every time before a file is saved, the strings of that file
		 * is re-directed to HTML2TEXT to extract plain text from the
		 * raw strings. To let HTML2TEXT extract from raw html strings,
		 * the StringBean class
		 * http://htmlparser.sourceforge.net/javadoc/org/htmlparser/beans/StringBean.html
		 * must be re-written to use a Lexer to accept raw Strings.
		 * http://htmlparser.sourceforge.net/javadoc/org/htmlparser/lexer/Lexer.html
		 * In this way files do not need to be read from disk so time-saving.
		 */
        crawler.addParserListener(new DownloadEventListener(mapping));

		crawler.start(this.urlList[0], "/");

		readLine();

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


        log.info("Crawling Website "+this.urlList[0]+" done.");


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
		PupSniffer sniffer;
		sniffer = new PupSniffer();

		sniffer.run();

	}

}

