package com.googlecode.pupsniffer;

import java.util.HashMap;

public class UrlPattern {

	protected HashMap<String, String> map;
	protected int count;
	
	public HashMap<String, String> getMap() {return map;}
	public void incCount() {count++;}
	public void addCount(int n) {count+=n;}
}
