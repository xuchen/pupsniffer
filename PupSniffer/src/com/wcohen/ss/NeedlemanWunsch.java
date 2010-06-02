package com.wcohen.ss;

import java.util.ArrayList;

import org.jfree.util.Log;

import com.wcohen.ss.api.*;

/**
 * Needleman-Wunsch string distance, following Durban et al.
 * Sec 2.3.
 *
 * <p>
 * X. Yao. 31-Mar-2010.
 * Modify this class to retrieve difference pairs between two strings,
 * with an assumption that the two strings sharing the same affix (either
 * prefix or suffix). So there's only one difference pair between
 * the two.
 */

public class NeedlemanWunsch extends AbstractStringDistance
{
    private CharMatchScore charMatchScore;
    private double gapCost;
    private MyMatrix mat;

    public NeedlemanWunsch() { this(CharMatchScore.DIST_01, 1.0 ); }

    public NeedlemanWunsch(CharMatchScore charMatchScore,double gapCost) {
        this.charMatchScore = charMatchScore;
        this.gapCost = gapCost;
    }

    public double score(StringWrapper s,StringWrapper t) {
        mat = new MyMatrix( s, t );
        return mat.get(s.length(), t.length() );
    }

    public String explainScore(StringWrapper s,StringWrapper t) {
        mat = new MyMatrix( s, t );
        double d = mat.get(s.length(), t.length() );
        mat.setPrintNegativeValues(true);
        return mat.toString() + "\nScore = "+d;
    }

    /**
     * Get the difference pair between two strings. For instance,
     * {"asVega", "osAngle"} for "LasVegas" and "LosAngles".
     * @return a string ArrayList with the 0th as the difference pair for S and 1st for T
     */
    public ArrayList<String> getDiffPair () {
		return getDiffPairAsArrayList();
    }


    public ArrayList<String> getDiffPairAsArrayList () {
    	String[] diffs = getDiffPairAsArray();
    	if (diffs == null) return null;

    	ArrayList<String> diffPair = new ArrayList<String>();
    	for (String d:diffs)
    		diffPair.add(d);

    	return diffPair;
    }

    public String[] getDiffPairAsArray() {

		int prefixEnd = 0, suffixStartS = 0, suffixStartT = 0;
		String s = mat.getSstring(), t = mat.getTstring();

		char[] sChars = s.toCharArray();
		char[] tChars = t.toCharArray();
		int minLen = Math.min(sChars.length, tChars.length);

		for (int i=0; i<minLen; i++) {
			if (sChars[i] != tChars[i]) {
				prefixEnd = i;
				break;
			}
		}

		for (int i=sChars.length-1, j=tChars.length-1; i>=0 && j>=0; i--, j--) {
			if (sChars[i] != tChars[j] || i<prefixEnd || j<prefixEnd) {
				suffixStartS = i+1;
				suffixStartT = j+1;
				break;
			}
		}

		if (!(prefixEnd == 0 && suffixStartS == 0 && suffixStartT == 0)) {
			try {
				String[] diffPair = new String[] {
						suffixStartS-prefixEnd==-1?"":s.substring(prefixEnd, suffixStartS),
						suffixStartT-prefixEnd==-1?"":t.substring(prefixEnd, suffixStartT)};
				return diffPair;
			} catch (java.lang.StringIndexOutOfBoundsException e) {
				e.printStackTrace();
				System.out.println("s: "+s);
				System.out.println("t: "+t);
				System.out.println(String.format("prefixEnd: %d, suffixStartS: %d, suffixStartT: %d", prefixEnd, suffixStartS, suffixStartT));
			}
		}
		return null;
    }

    private class MyMatrix extends MemoMatrix {
        public MyMatrix(StringWrapper s,StringWrapper t) {
        	super(s,t);
        }
        public double compute(int i,int j) {
	    if (i==0) return -j*gapCost;
	    if (j==0) return -i*gapCost;
	    return max3( get(i-1,j-1) + charMatchScore.matchScore( sAt(i), tAt(j) ),
                         get(i-1, j) - gapCost,
                         get(i, j-1) - gapCost);
        }
    }

    static public void main(String[] argv) {
        doMain(new NeedlemanWunsch(), argv);
    }
}
