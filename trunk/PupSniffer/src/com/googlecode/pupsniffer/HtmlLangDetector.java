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
import java.util.Set;

import cue.lang.CharIterator;
import cue.lang.Counter;
import cue.lang.NonAsciiCharIterator;
import cue.lang.WordIterator;
import cue.lang.stop.StopWords;

/**
 *
 * @author Xuchen Yao
 *
 */
public class HtmlLangDetector
{

	public static final String propertyFile = "conf/lang.properties";
	protected HashMap<String, HashSet<String>> langMap;
	protected HashSet<String> excepWords;

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
			int count = 0;
			for (final String word : chars)
			{
				if (isStopWord(word, stopWords))
				{
					count++;
				}
			}
			if (count < 10 && words != null)
			{
				count = 0;
				for (final String word : words)
				{
					if (isStopWord(word, stopWords))
					{
						count++;
					}
				}
			}
			double norm = count*1.0/stopWords.size();
			if (norm > currentMax)
			{
				currentWinner = lang;
				currentMax = norm;
			}
		}
		return currentWinner;
	}

	private HtmlLangDetector()
	{
		Properties prop = new Properties();
		try {
            prop.load(new FileInputStream(propertyFile));
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
		HtmlLangDetector detector = new HtmlLangDetector();
		//fileName = "/home/xcyao/CityU/work/welcome.html";
		//url = "http://news.sina.com.cn";
		url = "http://ngramj.sourceforge.net/use_ngramj.html";
		url = "http://www.let.rug.nl/~vannoord/TextCat/ShortTexts/dutch.txt";
		url = "http://alias-i.com/lingpipe/demos/tutorial/langid/read-me.html";

		//html = HtmlLangDetector.fetch(url);
		//html = "证券简称,今日开盘价,昨日收盘价,最近成交价,最高成交价,最低成交价,买入价";
		//html = "证券简称，今日开盘价，昨日收盘价，最近成交价，最高成交价，最低成交价，买入价";
		//html = "今日开盘价，的，好，人，了，是，不";
		html = HTML2TEXT.getText(url);

		System.out.println(html);
		System.out.println(detector.guess(html));

	}
}
