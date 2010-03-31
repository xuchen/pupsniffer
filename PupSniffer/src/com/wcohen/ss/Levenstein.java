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
		Levenstein l = new Levenstein();
		doMain(l, argv);
		String[] diffPair = l.getDiffPair();

		System.out.println("Diff Pair:");
		for (String s:diffPair)
			System.out.println(s);

	}
}
