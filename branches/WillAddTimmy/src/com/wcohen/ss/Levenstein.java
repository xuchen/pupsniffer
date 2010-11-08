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
		s = "http://www.wsd.gov.hk/tc/water_resources/raw_water_sources/water_sources_in_hong_kong/water_from_dongjiang_at_guangdong/index.html";
		t = "http://www.wsd.gov.hk/tc/water_resources/raw_water_sources/water_sources_in_hong_kong/water_from_dongjiang_at_guangdong/shenzhen_water_supply_schematic_diagram/index.html";
		s = "http://www.cad.gov.hk/reports/environmentreport2003/chi/ch5.html";
		t = "http://www.cad.gov.hk/reports/environmentreport2003/ch5.html";
		Levenstein l = new Levenstein();
		l.explainScore(s, t);

		System.out.println("Diff Pair: "+l.getDiffPair());


	}
}
