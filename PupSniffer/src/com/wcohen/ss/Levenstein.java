package com.wcohen.ss;


/**
 * Levenstein string distance. Levenstein distance is basically
 * NeedlemanWunsch with unit costs for all operations.
 */

public class Levenstein extends NeedlemanWunsch
{
	public Levenstein() {
		super(CharMatchScore.DIST_01, 1.0 );
	}
	public String toString() { return "[Levenstein]"; }

	static public void main(String[] argv) {
		String s, t;
		s = "http://www.cas.gov.hk/sctext/about/about_service.html";
		t = "http://www.cas.gov.hk/ctext/about/about_service.html";
//		s = "http://www.cas.gov.hk/ctext/notice/notice.html";
//		t = "http://www.cas.gov.hk/etext/notice.html";
		s = "http://www.cas.gov.hk/en/about/about_service.html";
		t = "http://www.cas.gov.hk/ad/tc/about/about_service.html";
		Levenstein l = new Levenstein();
		l.explainScore(s, t);

		System.out.println("Diff Pair: "+l.getDiffPair());


	}
}
