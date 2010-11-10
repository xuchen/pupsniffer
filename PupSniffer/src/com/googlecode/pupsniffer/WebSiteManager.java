package com.googlecode.pupsniffer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

/*
 * This class gets all bilingual webpages url pair sets, urls sets of all websites. 
 * @author Chengzhi Zhang
 * @since 2010-11-04
 */

public class WebSiteManager {

    String dir = "";

    String  temp = "";
    
    int j= 0;
 
    /*
     *  traversal file for a directory（including sbu-directory）
     */
    /*
    public int serachFiles(String dir) {
        File root = new File(dir);
        File[] filesOrDirs = root.listFiles();
        //String[] result = new String[1000000];
       for (int i = 0; i < filesOrDirs.length; i++) 
        {
            if (filesOrDirs[i].isDirectory())
            {
                serachFiles(filesOrDirs[i].getAbsolutePath());
            }
            else
            {       
            	if  (filesOrDirs[i].getAbsolutePath().indexOf("PupSnifferPatterns") < 0 ){
            		//result[i] = filesOrDirs[i].getAbsolutePath();
                    //temp += filesOrDirs[i].getAbsolutePath().substring(filesOrDirs[i].getAbsolutePath().indexOf(args)) + "\n";
                    j = j +1;
            	}
            }
        }
                
        //return j+"	\n"+temp;          //return urls counts and urls sets, use 'tab\n' as a delimiter
        //return  temp;                    //only return urls sets
        return j;                          //only retrun urls count
    }
    */
    
    /*
     * @param args
     * args[0] is websist list file
     * args[1] is directory for saving web pages
     */
    public static void main(String[] args) {
        
    	String[] website = new String[100000];
    	String read;
    	String readte;
    	String readse;
    	String readStr = "";
    	int NumofWebsite = 0;                           //websites count
    	BufferedReader bufread;
    	BufferedReader bufreadte;
    	BufferedReader bufreadse;
        FileReader fileread;
        FileReader filereadte;
        FileReader filereadse;
        
        try {
            fileread = new FileReader(args[0]);      
            bufread = new BufferedReader(fileread);
            try {
                while ((read = bufread.readLine()) != null) {
                	read = read.replace("http://", "");
                	read = read.replace("https://", "");
                	read = read.replace("/", "");
                	               	
                    readStr = readStr + read+ ",";
                    NumofWebsite ++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        
        website = readStr.split(",");
        
       
        if (args[1].lastIndexOf("/") != args[1].length()-1)
        	args[1]= args[1] + "/";   
        
        //all urs list of all websites
        String AllUrlFile = args[1]+"/AllUrls.txt";         
        BufferedWriter outallurl = null;
        try {
        File allurlfile = new File(AllUrlFile );
        if (allurlfile.exists()) allurlfile.delete();
        outallurl = new BufferedWriter(new FileWriter(AllUrlFile , true));
        }catch (IOException e) {
			e.printStackTrace();
		}
        
        //statistical file saving url counts of each website
        String WsfFile = args[1]+"/WebsiteStat.txt";   
        BufferedWriter outwsf = null;
        try {
        File wsffile = new File(WsfFile);
        if (wsffile.exists()) wsffile.delete();
        outwsf = new BufferedWriter(new FileWriter(WsfFile , true));
        }catch (IOException e) {
			e.printStackTrace();
		}        
        
        //Chinese traditional and English URLs Pair of  all websites
        String teFile = args[1]+"/te.txt";      
        BufferedWriter outtesf = null;
        try {
        File tesf = new File(teFile );
        if (tesf.exists()) tesf.delete();
        outtesf = new BufferedWriter(new FileWriter(teFile , true));
        }catch (IOException e) {
			e.printStackTrace();
		}   
        
        //Chinese simplified and English URLs Pair of  all websites
        String seFile = args[1]+"/se.txt";      
        BufferedWriter outsesf = null;
        try {
        File sesf = new File(seFile );
        if (sesf.exists()) sesf.delete();
        outsesf = new BufferedWriter(new FileWriter(seFile , true));
        }catch (IOException e) {
			e.printStackTrace();
		}          
                  
        int k = 0;
        String cmds = "";
        Process process = null;
        InputStreamReader ir = null;
        LineNumberReader input = null;
        String line = "";
        int eachwebsitecout = 0; 
        
        for (int i = 0; i < NumofWebsite ; i++)
        {
        	/*
        	 * Collecting all urls of all websites, including urls count of each webistes,
        	 * Chinese and English URLs Pair
        	*/
        	 
			if (new File(args[1] + website[i]).isDirectory())        
        	{      	        	
        		k = k + 1;
        		System.out.println("["+k+"] " + website[i]);
        		       		    
        		eachwebsitecout = 0; 
        		 
        		String UrlsFile = args[1] + website[i]+"/PupSnifferPatterns/Urls.txt";         //Collecting all ursl of current website
        		try{
        		    File sf = new File(UrlsFile);
					if (sf.exists()) sf.delete();
					BufferedWriter out = new BufferedWriter(new FileWriter(UrlsFile, true));
		
					cmds ="find " + args[1] + website[i] +" -type f";
					process = Runtime.getRuntime().exec(cmds); 
					ir=new InputStreamReader(process.getInputStream());
                    input=new LineNumberReader(ir);
		            
                    
			        while((line=input.readLine())!=null)
			        {
			            if (line.indexOf("PupSnifferPatterns") < 0)
			            {
			                try{
					           out.write(line.substring(args[1].length())+"\n");             //URLs file of each website
					           eachwebsitecout = eachwebsitecout + 1;
					        }catch (IOException e) {
							 e.printStackTrace();
					        }
					    
					        try{
							   outallurl.write(line.substring(args[1].length())+"\n");       //URLs file of all websites
						    }catch (IOException e) {
							 e.printStackTrace();
						    } 
			            }
			        }  
			        try {
					   if (out!=null)
						  out.close();
				    }catch (IOException e) {
						e.printStackTrace();
					}
				    
				    try{
					    outwsf.write(website[i] + " : " + eachwebsitecout + "\n");
					}catch (IOException e) {
						e.printStackTrace();
					}
					
					System.out.println("    URL Number is " + eachwebsitecout);
					
        		}catch (IOException e) {
					e.printStackTrace();
				}    

				File path = new File(args[1] + website[i]+"/PupSnifferPatterns");
				String[] list;
				list = path.list();
				if (path.exists())
				{
				   for(int q = 0; q < list.length; q++)
				   {	
					  //Chinese traditional and English URLs Pair --->  English and Chinese traditional URLs Pair
					  //http://www.wsd.gov.hk/pda/tc/press_release_publicity/events/index_id_43.html<->
					  //http://www.wsd.gov.hk/pda/en/press_release_publicity/events/index_id_43.html 1.000000
				      if (list[q].contains("chinesetraditional_english"))
				      {
				    	//System.out.println(args[1] + website[i]+"\\PupSnifferPatterns\\"+list[q]);
				    	try{
				    	    filereadte = new FileReader(args[1] + website[i]+"/PupSnifferPatterns/"+list[q]);      
				    	    bufreadte = new BufferedReader(filereadte);
				    	    try {
				    	    	 String patternte = "";
				    	         while ((readte = bufreadte.readLine()) != null) {
				    	        	try{  	
				    	        	    //# Pattern: tc<->en ---> en<->tc
				    	        		if (readte.contains("# Pattern:"))
				    	        		{
				    	        		   patternte = readte.substring(11);
				    	        		   patternte = patternte.substring(patternte.indexOf("<->")+3)+"<->"+patternte.substring(0, patternte.indexOf("<->"));
				    	        		}
				    	        		else 
				    	        		{  
				    	        		   try{
				    	        		      readte = readte.substring(readte.indexOf("<->")+3,readte.length()-9)+"	"+readte.substring(0,readte.indexOf("<->"))+"	t	"+readte.substring(readte.length()-8);    //中英文url位置互换,各字段以tab为分界符,t为繁体标识
				    	           	          outtesf.write(readte+"	"+patternte+"\n");
				    	        		   }catch(IOException e)
				    	        		   {
				    	        			   e.printStackTrace();
				    	        		   }
				    	        		}
				    	           	}catch (Exception e) {
						                    e.printStackTrace();
						            }  
				    	          }
				    	          } catch (IOException e) {
				    	                e.printStackTrace();
				    	          }
				    	    } catch (FileNotFoundException e) {
				    	            e.printStackTrace();
				    	    }		    	
				       }
					    
				      //English and Chinese traditional
				      if (list[q].contains("english_chinesetraditional"))
				      {
				    	//System.out.println(args[1] + website[i]+"\\PupSnifferPatterns\\"+list[q]);
				    	try{
				    	    filereadte = new FileReader(args[1] + website[i]+"/PupSnifferPatterns/"+list[q]);      
				    	    bufreadte = new BufferedReader(filereadte);
				    	    try {
				    	    	 String patternte = "";
				    	         while ((readte = bufreadte.readLine()) != null) {
				    	        	try{  	
				    	        	    //# Pattern: eng<->chi
				    	        		if (readte.contains("# Pattern:"))
				    	        		   patternte = readte.substring(11);
				    	        		else
				    	        		{  
				    	        		   readte = readte.substring(0,readte.indexOf("<->"))+"	"+readte.substring(readte.indexOf("<->")+3,readte.length()-9)+"	t	"+readte.substring(readte.length()-8);  //将分割符设置为tab,t为繁体标识
				    	           	       outtesf.write(readte+"	"+patternte+"\n");
				    	        		}
				    	           	}catch (IOException e) {
						                    e.printStackTrace();
						            }  
				    	          }
				    	          } catch (IOException e) {
				    	                e.printStackTrace();
				    	          }
				    	    } catch (FileNotFoundException e) {
				    	            e.printStackTrace();
				    	    }		    	
				      }
				    
				    
				      //Chinese simplified and English URLs Pair --->  English and Chinese simplified URLs Pair
				      if (list[q].contains("chinesesimplified_english"))
				      {
				    	//System.out.println(args[1] + website[i]+"\\PupSnifferPatterns\\"+list[q]);
				    	try{
				    	    filereadse = new FileReader(args[1] + website[i]+"/PupSnifferPatterns/"+list[q]);      
				    	    bufreadse = new BufferedReader(filereadse);
				    	    try {
				    	    	String patternse = "";
				    	         while ((readse = bufreadse.readLine()) != null) {
				    	        	try{  	
				    	        		if  (readse.contains("# Pattern:"))
				    	        		{
					    	        		   patternse = readse.substring(11);
					    	        		   patternse = patternse.substring(patternse.indexOf("<->")+3)+"<->"+patternse.substring(0, patternse.indexOf("<->"));
					    	        	}
					    	        	else
					    	        	{   
					    	        		try{
					    	        			readse = readse.substring(readse.indexOf("<->")+3,readse.length()-9)+"	"+readse.substring(0,readse.indexOf("<->"))+"	s	"+readse.substring(readse.length()-8);  //中英文url位置互换,各字段以tab为分界符,s为简体标识
					    	        			outsesf.write(readse+"	"+patternse+"\n");
					    	        		   }catch(IOException e)
					    	        		   {
					    	        			   e.printStackTrace();
					    	        		   }
					    	        		}
					    	           	}catch (Exception e) {
							                    e.printStackTrace();
							            }  
					    	          }
					    	          } catch (IOException e) {
					    	                e.printStackTrace();
					    	          }
					    	    } catch (FileNotFoundException e) {
					    	            e.printStackTrace();
					    	    }		    	
					  }					    
				    
				      //English and simplified Chinese
				      if (list[q].contains("english_chinesesimplified"))
				      {
				    	//System.out.println(args[1] + website[i]+"\\PupSnifferPatterns\\"+list[q]);
				    	try{
				    	    filereadse = new FileReader(args[1] + website[i]+"/PupSnifferPatterns/"+list[q]);      
				    	    bufreadse = new BufferedReader(filereadse);
				    	    try {
				    	    	String patternse = "";
				    	         while ((readse = bufreadse.readLine()) != null) {
				    	        	try{  	
				    	        		if  (readse.contains("# Pattern:"))
				    	        			patternse = readse.substring(11);
					    	        	else
					    	        	{
					    	        		readse = readse.substring(0,readse.indexOf("<->"))+"	"+readse.substring(readse.indexOf("<->")+3,readse.length()-9)+"	s	"+readse.substring(readse.length()-8);   //将分割符设置为tab,s为简体标识
					    	           	    outsesf.write(readse+"	"+patternse+"\n");
					    	        	}
				    	           	}catch (IOException e) {
						                    e.printStackTrace();
						             }  
				    	          }
				    	          } catch (IOException e) {
				    	                e.printStackTrace();
				    	          }
				    	    } catch (FileNotFoundException e) {
				    	            e.printStackTrace();
				    	    }	
				      }					    
								    
				   }
				}
					
        	}		
					
        }
        
		try {
			if (outwsf!=null)
				outwsf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	
		
		try {
			if (outtesf!=null)
				outtesf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			if (outsesf!=null)
				outsesf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			if (outallurl!=null)
				outallurl.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("\nThe task of Bilingual Web pages Url Collecting has completed.");
    }
}
