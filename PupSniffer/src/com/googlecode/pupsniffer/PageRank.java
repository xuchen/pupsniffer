package com.googlecode.pupsniffer;
 
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

/**
 * Modified from http://hi.baidu.com/bjtulan/blog/item/8856df545ecbb058d109069e.html
 * The PageRank class computes pagerank value of each website based on their link relationship. 
 * @author Chengzhi Zhang
 * @since 2010-11-04
 */

public class PageRank {
	
    /*
     * @param args
     * args[0] is the directory including 'website.txt' (WebSites list file) and 
     * 'Links_Matrix.txt' (WebSites relationship file), the result of PageRank computing
     * is saved in the file 'Website_PageRank.txt' of the same directory.
     */
    public static void main(String[] args) {
        /*Define a maximum error different*/
        double MAX = 0.000000000000001;
        
        /*Define a dampening factor*/
        double RIGHT = 0.15;
        
        String [] website = new String[1237];
        int i = 0;
        String read; 
    	BufferedReader bufread;
    	FileReader fileread;
    	
    	try {
            fileread = new FileReader(args[0] + "/website.txt");      
            bufread = new BufferedReader(fileread);
            try {
                while ((read = bufread.readLine()) != null)
                {                  
            	    website[i] = read; 
            	    i = i + 1;
                }    
            }  catch (IOException e) {
               e.printStackTrace();
            } 
    	} catch (FileNotFoundException e) {
            e.printStackTrace();
        } 
        
        String seFile = args[0] + "/Website_PageRank.txt";      
        BufferedWriter outsesf = null;
        try {
        File sesf = new File(seFile );
        if (sesf.exists()) sesf.delete();
        outsesf = new BufferedWriter(new FileWriter(seFile , true));
        }catch (IOException e) {
			e.printStackTrace();
		} 
       
         /*
        The Graph of WebSites Links is as follows:
        
             A    B    C
        A    0    1    1
        B    0    0    1
        C    1    0    0
        
        */
        /*
         * Get links relationship from the file 'Links_Matrix.txt'.
        */
        int [][] links = new int[1237][1237];
        
    	int j = 0;
        try {
            fileread = new FileReader(args[0] + "/Links_Matrix.txt");      
            bufread = new BufferedReader(fileread);
            try {
                while ((read = bufread.readLine()) != null)
                {
                	int k = 0;
                	while( read.length()> 2*k )
                	{
                	   links[j][k] = Integer.valueOf(read.substring(2*k, 2*k+1)); 
                	   k = k+1;
                	}
                	
                	j = j +1;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }        
		
        int len = links.length;
        
        //Sum of Link Out of each WebSite
        int[] linkOut = new int[len];
        for(int k = 0; k<len; k ++) {
            for(int l = 0; l < len; l++) {
                linkOut[k] += links[k][l];
            }
        }
        
        /*pr is the PageRank value of each WebSite*/
        double[] pr = new double[len];
        
        //temporary variable
        double[] init = new double[len];
        
        /*initialization*/
        for(i=0; i < len; i++ ) {
            init[i] = 0.0;
        }
        
        //Do PageRank computation at first time
        pr = doPageRank(init, linkOut, links, RIGHT);
        
        //Iteration until the error different is lower than the maximum error
        while(!checkPrecision(pr, init, MAX)) {
            System.arraycopy(pr, 0, init, 0, len);
            pr = doPageRank(pr, linkOut, links, RIGHT);
        }
        
        for(i = 0; i < len; i++) {
        	System.out.println(website[i] +  "	" + pr[i]);
        	try{ 
       	      outsesf.write(website[i] +  "	" + pr[i] + "\n" );
     	    }catch(IOException e)
  		    {
  			   e.printStackTrace();
  		    }
        }
        
		try {
			if (outsesf!=null)
				outsesf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
    }
    
    /**
     * This function is used to verify the result of error different 
     *@param checked: the array requested to be checked
     *@param ref : the reference array 
     *@param precision : the maximum error
     *@return result : result of verifying.
     */
    public static boolean checkPrecision(double[] checked, double[] ref, double precision) {

        if(checked.length != ref.length) {
            System.out.println("lenth is not the same!");
            return false;
        }
        
        boolean result = true;
        int len = ref.length;
        for(int i=0; i<len; i++) {
            if(Math.abs(checked[i] - ref[i]) > precision) {
                result = false;
                break;
            }
        }
        return result;
    }
    
    /**
    *This function is used to do PageRank computation
    *@param init : the Initial or the previous result without meeting the requirement
    *@param linkOut : the array of Link Out of each WebSite。
    *@param links : the Graph of WebSite's Links relationship
    *@param right : the dampening factor
    *@param pr : result of PageRank computation
    */
    public static double[] doPageRank(double[] init, int[] linkOut, int[][] links, double right) {

        if(init.length != linkOut.length) {
            return null;
        }
        
        int len = init.length;
        
        double[] pr = new double[len];
        
        double temp = 0;
        
        int i = 0, j = 0;
        for(i = 0; i<len; i++) {
            j  = 0;
            temp = 0;
            for(j = 0; j < len; j++) {
                if((i != j) && (linkOut[j] != 0) && (links[j][i] != 0)) {
                    temp += init[j]/linkOut[j];
                }             
            }
            pr[i] = right/len + (1 - right)* temp;
        }
        return pr;
    }
} 

