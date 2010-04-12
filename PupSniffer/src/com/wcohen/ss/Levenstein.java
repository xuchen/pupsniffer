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
		if (argv.length!=2) {
			System.out.println("usage: string1 string2");
		} else {
			l.explainScore(argv[0],argv[1]);
		}

		System.out.println("Diff Pair: "+l.getDiffPair());


	}
}
