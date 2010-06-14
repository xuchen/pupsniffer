
package com.googlecode.pupsniffer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;

import info.monitorenter.cpdetector.io.*;

/**
 * A simple encoding detector based on cpdetector.sf.net
 * @author Xuchen Yao
 * @since 2010-03-30
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
	 * @return the encoding in upper case
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	public String detect(String url) throws MalformedURLException, IOException {

		// Work with the configured proxy:
		Charset charset = null;

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

	public String detectFromRaw(String raw, String encoding) throws IOException {

		// Work with the configured proxy:
		Charset charset = null;
		InputStream is;
		byte[] bs;

		// convert String to inputstream
		try {
			if (encoding == null)
				bs = raw.getBytes();
			else
				bs = raw.getBytes(encoding);
            is = new ByteArrayInputStream(bs);

    		charset = detector.detectCodepage(is, bs.length);
    		if(charset == null){
    			return null;
    		} else{
    			// Open the document in the given code page:
    			//java.io.Reader reader = new java.io.InputStreamReader(new java.io.FileInputStream(document),charset);
    			// Read from it, do sth., whatever you desire. The character are now - hopefully - correct..
    			return charset.name();
    		}
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;

	}

	/**
	 * List the supported encoding on your system. For debugging and coding.
	 */
	public static void supportedEncoding () {
		// http://www.java2s.com/Code/Java/File-Input-Output/ListtheCharsetinyoursystem.htm
		SortedMap charsets = Charset.availableCharsets();
	    Set names = charsets.keySet();
	    for (Iterator e = names.iterator(); e.hasNext();) {
	      String name = (String) e.next();
	      Charset charset = (Charset) charsets.get(name);
	      System.out.println(charset);
	      Set aliases = charset.aliases();
	      for (Iterator ee = aliases.iterator(); ee.hasNext();) {
	        System.out.println("    " + ee.next());
	      }
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

		supportedEncoding();
	}

}
