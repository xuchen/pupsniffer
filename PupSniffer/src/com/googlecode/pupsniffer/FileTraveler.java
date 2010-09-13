package com.googlecode.pupsniffer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;


import org.apache.log4j.Logger;

public class FileTraveler {
	private static Logger log = Logger.getLogger(FileTraveler.class);

	protected String url;

	protected String saveDir;

	protected Site site;

	HashSet<String> extSet;

	public FileTraveler (String url, String saveDir, Site site, String[] fileExtList) {
		this.url = url;
		this.saveDir = saveDir;
		this.site = site;
		extSet = new HashSet<String>();
		if (fileExtList==null) return;
		for (String ext:fileExtList) {
			extSet.add(ext);
		}
	}

	public Site getSite () { return this.site; }

	public void start () {
		File f = new File(saveDir+url);
		try {
			traverse(f);
		} catch (Exception e) {
			log.error(e.toString());
		}
	}

	/**
	 * http://vafer.org/blog/20071112204524
	 * Recursively travels a directory.
	 * @param f A file or directory
	 * @throws IOException
	 */
	public final void traverse( final File f ) throws IOException {
		if (f.isDirectory()) {
			onDirectory(f);
			final File[] childs = f.listFiles();
			for( File child : childs ) {
				traverse(child);
			}
			return;
		}
		onFile(f);
	}

	private void onDirectory( final File d ) {
	}

	private void onFile( final File f ) {
		String ext = FileTraveler.getExtension(f);
		if (!this.extSet.contains(ext)) return;
	    char[] buffer = new char[(int) f.length()];
	    BufferedInputStream bis = null;
		try {
			bis = new BufferedInputStream(new FileInputStream(f));
			InputStreamReader reader = new InputStreamReader(bis, "iso-8859-1");
			reader.read(buffer);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (bis!=null)
				try {
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		String raw = new String(buffer);
		String uri = f.getPath().substring(this.saveDir.length());
		site.addUrl(uri, null, raw);
	}

	public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (f.isDirectory())
        	ext = null;
        else if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }

}
