package com.streams.zooniverse;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.ProcessBuilder.Redirect;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;

import com.rabbitmq.client.Channel;

public class ZooniverseClassifications {
	
	
	
	private class StreamGobbler extends Thread {
	    InputStream is;
	    String type;

	    private StreamGobbler(InputStream is, String type) {
	        this.is = is;
	        this.type = type;
	    }

	    @Override
	    public void run() {
	        try {
	            InputStreamReader isr = new	 InputStreamReader(is);
	            BufferedReader br = new BufferedReader(isr);
	            String line = null;
	            while ((line = br.readLine()) != null)
	                System.out.println(type + "> " + line);
	        }
	        catch (IOException ioe) {
	            ioe.printStackTrace();
	        }
	    }
	}


	
	
	
	
//	public static void main(String[] args) throws Exception {		
//		
//		System.out.println("Zooniverse Classification Stream");
//		
//		
//		 ProcessBuilder pb = new ProcessBuilder(
//		            "curl.exe",
//		            "-H",
//		            "Accept: application/vnd.zooevents.stream.v1+json",
//		            "http://event.zooniverse.org/classifications");
//
//		   // pb.directory(new File("C:/curl"));
//		    pb.redirectErrorStream(true);
//		    File log = new File("log");
////		    pb.redirectOutput(Redirect.appendTo(log));
//
//		    PrintStream old_out = System.out;
//
//		   pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
//		    
//		    
//		    ByteArrayOutputStream pipeOut = new ByteArrayOutputStream();
//
//
//		    //pb.inheritIO();
//
//		    //OutputStream stream = System.out; 
//		   // System.setOut();
//		    		//Process p = pb.start();
//		    
//		    try {	
//		    	  Process p = pb.start();
//				    System.setOut(new PrintStream(pipeOut));
//
//		    	  // wait for termination.
//		    	  p.waitFor();
//		    	  
//				// StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), "OUTPUT");
//		    	  String output = new String(pipeOut.toByteArray());
//		    	  System.out.println(output);
//		    	  
//		    	} catch (IOException e) {
//		    	  e.printStackTrace();
//		    	} catch (InterruptedException e) {
//		    	  e.printStackTrace();
//		    	}
//		    
////		    assert pb.redirectInput() == Redirect.PIPE;
////		    assert pb.redirectOutput().toString() !=null;
////		    assert p.getInputStream().read() == -1;
////		    
//		    //InputStream is = p.getInputStream();
//
////		    FileOutputStream outputStream = new FileOutputStream(
////		            "/home/your_user_name/Pictures/simpson_download.jpg");
//
////		    String line;
////		    BufferedInputStream bis = new BufferedInputStream(is);
////		    byte[] bytes = new byte[100];
////		    int numberByteReaded;
////		    String myString = IOUtils.toString(is, "UTF-8");
////		    
////		    System.out.println(myString);
////		    
////		    while ((numberByteReaded = bis.read(bytes, 0, 100)) != -1) {
////
////		        System.out.println(is);
////		        //Arrays.fill(bytes, (byte) 0);
////
////		    }
//
//		    //outputStream.flush();
//		    //outputStream.close();
//		
//	}
	
	
	public static void main(String[] args) throws Exception{     
	    URL url = new URL("http://event.zooniverse.org/classifications");
	    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	    conn.setDoOutput(true);
	    conn.setDoInput(true);
	    conn.setRequestMethod("GET");
	    //conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
	    conn.setRequestProperty("Accept", "application/vnd.zooevents.stream.v1+json");

	    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	    StringBuilder stb = new StringBuilder();

	    String line;
	    while ((line = br.readLine()) != null) {
	      stb.append(line);
	      System.out.println(line);
	    }

	    if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {

	    throw new RuntimeException("Failed : HTTP error code : "+ conn.getResponseCode() + conn.getResponseMessage());

	    }
	
	}






	public void startStreaming(Channel channel, String exchangeName) throws Exception {
		// TODO Auto-generated method stub
		URL url = new URL("http://event.zooniverse.org/classifications");
	    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	    conn.setDoOutput(true);
	    conn.setDoInput(true);
	    conn.setRequestMethod("GET");
	    //conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
	    conn.setRequestProperty("Accept", "application/vnd.zooevents.stream.v1+json");

	    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	    StringBuilder stb = new StringBuilder();

	    String line;
	    while ((line = br.readLine()) != null) {
	      //stb.append(line);
	      //System.out.println(line);
	      if(line.startsWith("{")){
	    	  System.out.println(line);
	    	  channel.basicPublish(exchangeName, "", null, line.getBytes());
	      }
	    }

	    if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {

	    throw new RuntimeException("Failed : HTTP error code : "+ conn.getResponseCode() + conn.getResponseMessage());

	    }
		
	}
}
	
	
	
