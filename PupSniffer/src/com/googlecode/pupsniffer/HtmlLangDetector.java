/*
   Copyright 2009 IBM Corp

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.googlecode.pupsniffer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Properties;

import cue.lang.Counter;
import cue.lang.NonAsciiCharIterator;
import cue.lang.WordIterator;
import cue.lang.stop.StopWords;

/**
 * Modified from cue.language
 * TODO: add word weighting in respect to frequencies as described in:
 * http://www.mozilla.org/projects/intl/UniversalCharsetDetection.html
 * @author Xuchen Yao
 * @since 2010-03-30
 */
public class HtmlLangDetector
{
	protected HashMap<String, HashSet<String>> langMap;
	protected HashSet<String> excepWords;

	public static final String CHINESE_TRADITIONAL = "chinesetraditional";
	public static final String CHINESE_SIMPLIFIED = "chinesesimplified";
	public static final String ENGLISH = "english";
	public static final String JAPANESE = "japanese";
	public static final String KOREAN = "korean";

	public String detect(final String url) {
		String text = HTML2TEXT.getText(url);
		return guess(text);
	}

	public String detectFromRaw(final String raw) {
		String text = HTML2TEXT.getTextFromRaw(raw);
		return guess(text);
	}

	public String guess(final String text)
	{
		return guess(new Counter<String>(new WordIterator(text)), new Counter<String>(new NonAsciiCharIterator(text)));
	}

	public String guess(final Counter<String> wordCounter, final Counter<String> charCounter)
	{
		return guess(wordCounter.getMostFrequent(50), charCounter.getMostFrequent(50));
	}

	public String guess(final Collection<String> words, final Collection<String> chars)
	{
		String currentWinner = null;
		double currentMax = 0.0;
		HashSet<String> stopWords;

		for (final String lang : langMap.keySet())
		{
			stopWords = langMap.get(lang);
			int count = 0, newcount = 0;
			for (final String word : chars)
			{
				if (isStopWord(word, stopWords))
				{
					count++;
				}
			}
			newcount = 0;
			if (count < 10 && words != null)
			{
				for (final String word : words)
				{
					if (isStopWord(word, stopWords))
					{
						newcount++;
					}
				}
			}
			count = newcount>count?newcount:count;
			//double norm = count*1.0/stopWords.size();
			// don't do normalization yet.
			/*
			 * Normalization has a negative effect on short text: shorter
			 * text has a higher chance to be wrongly classified into another
			 * language, especially when the word list of that language is
			 * small. Thus, if we don't divide the count factor with
			 * the stopWords size, then languages provided with a bigger
			 * stopWords (such as English, Chinese, which we favor in this
			 * project) are more likely recognized.
			 */
			double norm = count*1.0;
			if (norm > currentMax) {
				currentWinner = lang;
				currentMax = norm;
			}
		}
		return currentWinner;
	}

	public HtmlLangDetector(String configFile)
	{
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(configFile));
		} catch (IOException e) {
			e.printStackTrace();
		}

		langMap = new HashMap<String, HashSet<String>>();

		String exceptionFile = prop.getProperty("exception");
		if (exceptionFile!=null && !exceptionFile.equals("")) {
			excepWords = loadLanguage(exceptionFile, true);
		}
		String[] langList = prop.getProperty("lang").split(",");
		HashSet<String> h;
		for (String l:langList) {
			l = l.toLowerCase(Locale.ENGLISH);
			h = loadLanguage(l, false);
			if (h!=null)
				langMap.put(l, h);
		}
	}

	public static boolean isStopWord(final String s, HashSet<String> stopwords)
	{
		// check rightquotes as apostrophes
		return stopwords.contains(s.replace('\u2019', '\'').toLowerCase(Locale.ENGLISH));
	}

	private HashSet<String> loadLanguage(String file, boolean exception)
	{
		final String wordlistResource = file.toLowerCase(Locale.ENGLISH);

		return readStopWords(StopWords.class.getResourceAsStream(wordlistResource), Charset.forName("UTF-8"), exception);

	}

	public HashSet<String> readStopWords(final InputStream inputStream, final Charset encoding, boolean exception)
	{
		HashSet<String> stopwords = new HashSet<String>();
		try
		{
			final BufferedReader in = new BufferedReader(new InputStreamReader(
					inputStream, encoding));
			try
			{
				String line;
				while ((line = in.readLine()) != null)
				{
					if (line.startsWith("#")) continue;
					line = line.replaceAll("\\|.*", "").trim();
					if (line.length() == 0)
					{
						continue;
					}
					for (final String w : line.split("\\s+"))
					{
						if (exception || (excepWords!=null && !excepWords.contains(w)))
							stopwords.add(w.toLowerCase(Locale.ENGLISH));
					}
				}
			}
			finally
			{
				in.close();
			}
		}
		catch (final IOException e)
		{
			throw new RuntimeException(e);
		}

		return stopwords.size()==0?null:stopwords;
	}

	/**
	 * Get the contents of a URL and return it as a string.
	 */
	public static String fetch(final String address)
	{
		try
		{
			URL url = new URL(address);
			BufferedReader br = new BufferedReader(
					new InputStreamReader(url.openStream()));
			String strline, all = "";

			while ((strline = br.readLine()) != null)
			{
				all += strline;
			}
			br.close();
			return all;
		}
		catch (MalformedURLException mue)
		{
			System.out.println("Unknown URL");
		}

		catch (IOException ioe)
		{
			System.out.println("IO Error");
		}
		return null;

	}

	public static void main(final String[] args)
	{
		String url, html;
		HtmlLangDetector detector = new HtmlLangDetector("conf/lang.properties");
		//fileName = "/home/xcyao/CityU/work/welcome.html";
		url = "http://news.sina.com.cn";
		//		url = "http://ngramj.sourceforge.net/use_ngramj.html";
		//		url = "http://www.let.rug.nl/~vannoord/TextCat/ShortTexts/dutch.txt";
		url = "http://alias-i.com/lingpipe/demos/tutorial/langid/read-me.html";
		url = "http://www.cas.gov.hk/eng/notice/notice_remove.html";
		url = "http://www.wsd.gov.hk/tc/job_opportunities/index_t.html";
		url = "http://www.cas.gov.hk/eng/about/about_performance2005.html";
        url = "http://www.islam.org.hk/big5/child1/ReadNews.asp?NewsID=577&BigClassName=锟紾锟斤拷锟絇锟斤拷&BigClassID=49&SmallClassID=58&SmallClassName=锟紾锟斤拷&SpecialID=6";
        url = "http://www.hkjc.com/chinese/racing/Track_Result.asp?txtHorse_BrandNo=J329";
        url = "http://www.hkjc.com/chinese/racing/Track_Result.asp?txtHorse_BrandNo=J329";
        url = "http://www.baby-and-me.biz/en/category/16_88/product/item/1344/product_detail.html";
        url = "http://www.123smile.com.hk/index.php?sp=&p=6&cat2=144&cat1=55&cat0=27&id=2025&cat1=55&cat0=27&new=&more=&s=cdd649fc3f737c443a7eb30ca655d0d0&lang=gb";
        //url = "http://www.hkjc.com/chinese/pressrelease/mcs01_showhtml.asp?SelType=NEWS&filename=20100831_185650_C_NEWS.htm";
        url = "http://www.hongkongpost.gov.hk/product/download/root/img/smartid_ca.cacert";
        url = "http://webstat.cis.gov.hk/webstat/jsp/country.jsp?dept=ldtss&period=20101";
        url = "http://www.hkqf.gov.hk/guie/RC_evt_20051108.asp";
        
		//html = HtmlLangDetector.fetch(url);
		//html = "证券简称,今日开盘价,昨日收盘价,最近成交价,最高成交价,最低成交价,买入价";
		//html = "证券简称，今日开盘价，昨日收盘价，最近成交价，最高成交价，最低成交价，买入价";
		//html = "今日开盘价，的，好，人，了，是，不";
		html = HTML2TEXT.getText(url);

		System.out.println(html);
		System.out.println(detector.guess(html));

	}
}
