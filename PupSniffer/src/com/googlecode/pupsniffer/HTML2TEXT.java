/**
 * 
 */
package com.googlecode.pupsniffer;


import org.htmlparser.beans.StringBean;
import org.htmlparser.util.ParserException;


/**
 * This class extracts plain-text from html pages. JSP files also have
 * limited support. See:
 * http://htmlparser.sourceforge.net/faq.html#jsp
 * <p/> 
 * Things I've tried to convert HTML to plain text:
 * 1. use HTMLEditorKit to parse HTML and get only text --> too much noise from markups
 * 2. use replaceAll() method to strip off all markups --> too slow (but generally works ok).
 * 3. process string char by char and strip off all markups --> also too slow
 * 4. use stringextractor from htmlparser.sf.net, it's quick, though with a little noise.
 * Reference: http://stackoverflow.com/questions/240546/removing-html-from-a-java-string
 *  
 * @author Xuchen Yao
 *
 */
public class HTML2TEXT {


    /**
     * Extract the text from a page.
     * @return The textual contents of the page.
     * @param url the URL
     * @param links if <code>true</code> include hyperlinks in output.
     * @exception ParserException If a parse error occurs.
     */
    public static String extractStrings(String url, boolean links) throws ParserException
    {
        StringBean sb;

        sb = new StringBean();
        sb.setLinks(links);
        sb.setURL(url);

        return (sb.getStrings());
    }
    
    /**
     * Extract the text from a page without including hyperlinks
     * @param url the URL
     * @return The textual contents of the page.
     */
    public static String getText(String url) {
    	String text=null;
    	try {
    		text = extractStrings(url, false);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return text;
    }


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//String fileName = "/home/xcyao/CityU/work/welcome.html";
		String url = "http://news.sina.com.cn";
		url = "http://www.cas.gov.hk/sctext/welcome.html";
		
		System.out.println(HTML2TEXT.getText(url));

//		BufferedReader in=null;
//		InputStreamReader ins=null;
//		try 
//		{    
//			URL url = new URL(urlName);
//			//FileInputStream fis = new FileInputStream(fileName);
//			//InputStreamReader ins = new InputStreamReader(fis, "UTF-8");  
//			//InputStreamReader ins = new InputStreamReader(url.openStream(), "UTF-8");
//
//			in = new BufferedReader(ins);
//
////			while (in.ready()) {
////				line = in.readLine();
////				html += line;
////			}
////			in.close();
//		} catch (Exception e) {    
//			e.printStackTrace ();     
//		}

	}

}
