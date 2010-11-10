package com.googlecode.pupsniffer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
//import java.sql.*; 

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


/*
 * This class gets all related websites linked-in bilingual web pages recognized by Pupsniffer.
 * @author Chengzhi Zhang
 * @since 2010-11-04
 */
public class GetLink
{

  public static void main(String[] args) 
  {
	  
    /* Related Websites Set 
     * @param args
     * args[0] is the bilingual url text file
     * args[1] is the directory for saving RelatedWebsites.txt  
    */
    String RwfFile = args[1]+"/RelatedWebsites.txt";   
    BufferedWriter outrwf = null;
    try {
       File rwffile = new File(RwfFile);
       if (rwffile.exists()) rwffile.delete();
          outrwf = new BufferedWriter(new FileWriter(RwfFile , true));
        }catch (IOException e) {
		 e.printStackTrace();
	}  
	  
	String url = "";
	String tempstr = "";
	
	/*
	 * Get urls of Chinese web pages from database
	 
    try{
		Connection conn;
        Statement stmt;
        ResultSet res;  
		Class.forName("com.mysql.jdbc.Driver").newInstance();
    	conn = DriverManager.getConnection( "jdbc:mysql://localhost/pupsniffer","root","");        
        stmt = conn.createStatement(); 
        res = stmt.executeQuery("select * from urltable");
        while(res.next())
        {  
        	String curl = res.getString("curl");
        }
        res.close();
     */
	
	
	/*
	 * Get urls of chinese webpages from text file
	 */
	
	String[] curl = new String[1000000];
	String read;
	String readStr = "";
	int Numofcurl = 0;             // number of curl                
	BufferedReader bufread;
    FileReader fileread;
    
    try {
        fileread = new FileReader(args[0]);      
        bufread = new BufferedReader(fileread);
        try {
            while ((read = bufread.readLine()) != null) {   	
                Numofcurl ++;
                curl[Numofcurl] = read.substring(read.indexOf("	")+1);
                //System.out.println(curl[Numofcurl]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    } catch (FileNotFoundException e) {
        e.printStackTrace();
    }	
	
    for (int i = 1; i < Numofcurl-1 ; i++)
    {
          	//curl = "www.cheu.gov.hk/eng/contactus/index.htm";
        	//curl = "mega.ctl.cityu.edu.hk/~czhang22/pupsniffer-eval/about.html";
        	
		    System.out.println("[" +  i  + "]	http://" + curl[i]);
		    
        	String website = curl[i].substring(0, curl[i].indexOf('/'));		    	
		    url = "http://" + curl[i]; 
		    try
		    {
		        Document doc = Jsoup.connect(url).get();
		        Elements links = doc.select("a[href]");
		         
		        for (Element link : links) { 
		            tempstr = link.absUrl("href"); 
		            if ( tempstr.length() > 7 && tempstr.indexOf("mailto:") < 0 )
		            {  
		                  tempstr = tempstr.substring(7);
		                  if (tempstr.indexOf('/')> 0)
		                  {
		                     tempstr = tempstr.substring(0,tempstr.indexOf('/'));
		                     if ((!tempstr.equals(website)) && (!tempstr.equals("www." + website)))
		                     {	 
		                    	  System.out.println("	" + tempstr );
		                    	  outrwf.write(website + "	"+tempstr + "\n");
		                     }
		                  }
		            }
		        } 
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


