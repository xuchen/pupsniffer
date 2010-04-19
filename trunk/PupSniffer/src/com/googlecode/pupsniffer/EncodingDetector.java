
package com.googlecode.pupsniffer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import info.monitorenter.cpdetector.io.*;

/**
 * A simple encoding detector based on cpdetector.sf.net
 * @author Xuchen Yao
 *
 */
public class EncodingDetector {
	protected CodepageDetectorProxy detector;

	public EncodingDetector() {
		detector = CodepageDetectorProxy.getInstance(); // A singleton.
		// Add the implementations of info.monitorenter.cpdetector.io.ICodepageDetector: 
		// This one is quick if we deal with unicode codepages: 
		detector.add(new ByteOrderMarkDetector()); 
		// The first instance delegated to tries to detect the meta charset attribut in html pages.
		detector.add(new ParsingDetector(false)); // be verbose about parsing.
		// This one does the tricks of exclusion and frequency detection, if first implementation is 
		// unsuccessful:
		detector.add(JChardetFacade.getInstance()); // Another singleton.
		detector.add(ASCIIDetector.getInstance()); // Fallback, see javadoc.
	}

	/**
	 * Detect the encoding of a URL
	 * @param url the URL address
	 * @return the encoding in uppder case
	 * @throws IOException 
	 * @throws MalformedURLException 
	 */
	public String detect(String url) throws MalformedURLException, IOException {

		// Work with the configured proxy: 
		java.nio.charset.Charset charset = null;

		charset = detector.detectCodepage(new URL(url));
		if(charset == null){
			return null;
		}
		else{
			// Open the document in the given code page:
			//java.io.Reader reader = new java.io.InputStreamReader(new java.io.FileInputStream(document),charset);
			// Read from it, do sth., whatever you desire. The character are now - hopefully - correct..
			return charset.name().toUpperCase();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		EncodingDetector d = new EncodingDetector();

		//d.detect(new URL("http://www.cas.gov.hk/sctext/welcome.html"));
		try {
			d.detect("http://www.let.rug.nl/~vannoord/TextCat/ShortTexts/chinese-big5.txt");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
