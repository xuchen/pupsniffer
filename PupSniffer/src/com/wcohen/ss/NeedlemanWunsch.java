package com.wcohen.ss;

import java.util.ArrayList;

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
     * Get the indices as a tuple of different parts in two strings. For instance, 
     * LasVegas and LosAngles have "asVega" and "osAngle" as different parts. Then
     * 3 numbers are returned: {1,7,9}, indicating the the end of the common prefix
     * ("L"), the start of common suffix in string S ("s" in "LasVegas"), and the
     * start of the common suffix in string T ("s" in "LosAngeles").
     * @return an int array
     */
    public int[] getDiffPrefixSuffix () {
    	int[] pair = new int[2];
    	int prefixEnd = 0, suffixStartS = 0, suffixStartT = 0;
    	int minLen = Math.min(mat.getSlen(), mat.getTlen());
    	
    	for (int i=1; i< minLen; i++) {
    		if (!(mat.getAbs(i, i) ==  mat.getAbs(i+1, i+1) &&
    				mat.getAbs(i, i) <=  mat.getAbs(i, i+1) &&
    				mat.getAbs(i, i) <=  mat.getAbs(i+1, i))) {
    			prefixEnd = i;
    			break;
    		}
    	}
    	
    	for (int i=mat.getRowNum(), j=mat.getColumnNum(); i>1 && j>1; i--, j--) {
    		if (!(mat.getAbs(i, j) == mat.getAbs(i-1, j-1) &&
    				mat.getAbs(i, j) <= mat.getAbs(i, j-1) &&
    				mat.getAbs(i, j) <= mat.getAbs(i-1, j))) {
    			suffixStartS = i;
    			suffixStartT = j;
    			break;
    		}
    		
    	}
    	
    	return new int[]{prefixEnd, suffixStartS, suffixStartT};
    }
	
    /**
     * Get the difference pair between two strings. For instance,
     * {"asVega", "osAngle"} for "LasVegas" and "LosAngles".
     * @return a string ArrayList with the 0th as the difference pair for S and 1st for T
     */
    public ArrayList<String> getDiffPair () {
		int[] intPair = getDiffPrefixSuffix();
		int prefixEnd = intPair[0], suffixStartS = intPair[1], suffixStartT = intPair[2];
		String s = mat.getSstring(), t = mat.getTstring();
		if (!(intPair[0] == 0 && intPair[1] == 0 && intPair[2] == 0)) {
//			String[] diffPair = new String[] {s.substring(prefixEnd, suffixStartS), 
//					t.substring(prefixEnd, suffixStartT)};
			ArrayList<String> diffPair = new ArrayList<String>();
			diffPair.add(s.substring(prefixEnd, suffixStartS));
			diffPair.add(t.substring(prefixEnd, suffixStartT));
			
			return diffPair;
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
