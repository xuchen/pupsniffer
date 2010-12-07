package com.googlecode.pupsniffer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


import java.net.HttpURLConnection;
import java.net.URL;


/*
 * This class detects whether candidate bilingual deep URL pair is valid
 * @author Chengzhi Zhang
 * @since 2010-12-07
 */
public class DeepURLDetecting
{
	private static URL url;   
	private static HttpURLConnection con;   
	private static int state = -1;   
	  
	/**  
	 * Detecting whether the current URL is valid,  
	 * Description：the max times of link is 5, the URL is invalid if the return is not 200 after 5 links.  
	 * @param urlStr is URL Address  
	 * @return link result, the URL Address is valid if the return value is 200  
	*/  
	
	public synchronized static int isConnect(String urlStr) {   
	   int counts = 0;  
	   if (urlStr == null || urlStr.length() <= 0) {                          
	    return 0;                    
	   }   
	   while (counts < 5) {   
	    try {   
	     
	     url = new URL(urlStr);   
	     con = (HttpURLConnection) url.openConnection();   
	     state = con.getResponseCode();     
	     break;   
	    }catch (Exception ex) {   
	     counts++;   
	     urlStr = null;   
	     continue;   
	    }   
	   }   
	   return state;   
	}   
  
  public static void main(String[] args) 
  {
	 
    /**   
     * @param args
     * args[0] is the candidate bilingual deep URL list text file
     * args[1] is the directory for saving URLPairs_Deep_Detecting.txt  
    */
	  
    String RwfFile = args[1]+"/URLPairs_Deep_Detecting.txt";   
    BufferedWriter outrwf = null;
    try {
       File rwffile = new File(RwfFile);
       if (rwffile.exists()) rwffile.delete();
          outrwf = new BufferedWriter(new FileWriter(RwfFile , true));
        }catch (IOException e) {
		 e.printStackTrace();
	}  
	
	String[] eurl = new String[1000000];
	String[] curl = new String[1000000];
	String read;
	
	int Numofurl = 0;             // number of  candidate URL Pair                
	BufferedReader bufread;
    FileReader fileread;
    
    try {
        fileread = new FileReader(args[0]);      
        bufread = new BufferedReader(fileread);
        try {
            while ((read = bufread.readLine()) != null) {   	
                Numofurl ++;
                eurl[Numofurl] = read.substring(0, read.indexOf("	"));
                curl[Numofurl] = read.substring(read.indexOf("	")+1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    } catch (FileNotFoundException e) {
        e.printStackTrace();
    }	
	
    for (int i = 1; i < Numofurl-1 ; i++)
    {
          	//eurl = "www.housingauthority.gov.hk/en/residential/shos/safetyguarantee/0,,1-162-265,00.html ";
        	//curl = "www.housingauthority.gov.hk/tc/residential/shos/safetyguarantee/0,,1-162-265,00.html ";
		    try
		    {
        	     if ((isConnect("http://" + eurl[i])==200) && (isConnect("http://" + curl[i])==200))
		         {	 
		              outrwf.write(eurl[i] + "	" + curl[i] + "\n");
		              System.out.println("[" +  i  + "] Good:	http://" + eurl[i]+ " | http://" + curl[i]);
		         }
        	     else
        	    	  System.out.println("[" +  i  + "] Bad: 	http://" + eurl[i]+ " | http://" + curl[i]);
		                   	         
		    }catch(Exception e){
		    		    	
		    }
    }
   	     	
    try {
	   if (outrwf!=null)
		  outrwf.close();
	} catch (IOException e) {
	  e.printStackTrace();
	}
  }  
}


