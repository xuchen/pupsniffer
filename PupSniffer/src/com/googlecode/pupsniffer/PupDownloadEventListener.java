/**
 *
 */
package com.googlecode.pupsniffer;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.torunski.crawler.events.DownloadEventListener;
import com.torunski.crawler.events.ParserEvent;
import com.torunski.crawler.filter.ILinkFilter;
import com.torunski.crawler.link.Link;
import com.torunski.crawler.parser.PageData;
import com.torunski.crawler.parser.httpclient.PageDataHttpClient;
import com.torunski.crawler.util.FileUtil;
import com.torunski.crawler.util.UriFileSystemMapperUtil;

/**
 * @author Xuchen Yao
 *
 */
public class PupDownloadEventListener extends DownloadEventListener {

    private static final transient Log LOG = LogFactory.getLog(DownloadEventListener.class);

    /** the optional download save filter for the page data. */
    private ILinkFilter saveFilter;

    /** the mapping of the URIs to the file system destination. */
    private UriFileSystemMapperUtil mappingUtil;

    private HashMap<String, Site> siteMapping;

	public void parse(ParserEvent event) {
        PageData page = event.getPageData();
        String enc;
        Site site;
        // is the page data available and OK?
        if ((page.getStatus() == PageData.OK) || (page.getStatus() == PageData.NOT_MODIFIED)) {
            Link link = page.getLink();
            // is additional save filter set and should we save the page data?
            if ((saveFilter == null) || (saveFilter.accept(null, link.getURI()))) {
                // get destination of file
                String dest = getDestination(link);
                if (dest != null) {
                    Object obj = page.getData();
                    if (obj instanceof String) {
                        // save data to file
                        File file = new File(dest);
                        if (page instanceof PageDataHttpClient) {
                        	enc = ((PageDataHttpClient) page).getCharSet();
                        	site = getSite(link.getURI());
                        	if (site==null) return;
                        	site.addUrl(link.getURI(), enc, (String) obj);
                            FileUtil.save(file, (String) obj, enc, link.getTimestamp());
                        } else {
                        	enc = null;
                        	//site.addUrl(link.getURI(), enc, (String) obj);
                            FileUtil.save(file, (String) obj, null, -1L);
                        }
                    } else {
                        LOG.warn("Page data has to be stored as a string. link=" + link);
                    }
                } else {
                    LOG.warn("No file destination found for link=" + link);
                }
            }
        }
    }

	/**
	 * Constructor.
	 * @param saveMapping a mapping from URL to the saving directory.
	 * @param siteMapping a mapping from URL to its Site structure.
	 */
	public PupDownloadEventListener(Map saveMapping, Map siteMapping) {
		super(saveMapping);
		this.siteMapping = (HashMap<String, Site>)siteMapping;
	}

	private Site getSite(String url) {
		for (String s:siteMapping.keySet()) {
			if(url.startsWith(s))
				return siteMapping.get(s);
		}
		return null;
	}

}
