package com.ramine.iplookup;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jsoup.Jsoup;

import com.ramine.functions.MiscFunctions;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;



public class WikipediaAPILookup {
	
	
public static String lookupDiff(String pageName, String timestamp, String language) throws Exception {
		
		timestamp = URLEncoder.encode(timestamp);
		String urlStr = "https://"+language+".wikipedia.org/w/api.php?action=query&prop=revisions&format=json&rvprop=timestamp%7Cuser%7Ccomment%7Ccontent&rvlimit=1&rvdiffto=prev&rvstart="
					+timestamp+"&titles="
					+pageName.replace(" ", "%20")+"&continue=";
	
		// 2015-10-01 %20 %02 3A 01 %3A 03
		///https://en.wikipedia.org/w/api.php?action=query&prop=revisions&format=json&rvprop=timestamp%7Cuser%7Ccomment%7Ccontent&rvlimit=1&rvdiffto=prev&rvstart=2015-10-01%20%023A01%3A03&titles=Every%20Trick%20in%20the%20Book
		URL url = new URL(urlStr);
	   //URL url = new URL("http://openls.geog.uni-heidelberg.de/testing2015/route?Start=8.6817521,49.4173462&End=8.6828883,49.4067577&Via=&lang=en&distunit=KM&routepref=Fastest&avoidAreas=&useTMC=false&noMotorways=false&noTollways=false&instructions=false");

		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	    conn.setDoOutput(true);
	    conn.setDoInput(true);
	    conn.setRequestMethod("GET");
	    //conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
	    //conn.setRequestProperty("Accept", "application/vnd.zooevents.stream.v1+json");

	    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

	    String line;
	    String div = "null";
	    JSONObject jsonResult = new JSONObject();
	    while ((line = br.readLine()) != null) {
	    	
	    	try{
		    	jsonResult = (JSONObject) JSONSerializer.toJSON(line);
				jsonResult = (JSONObject) jsonResult.get("query");
				
				jsonResult = (JSONObject) jsonResult.get("pages");
				
				Iterator<?> keys = jsonResult.keys();	
				
				while( keys.hasNext() ) {
				    String key = (String)keys.next();
				    if ( jsonResult.get(key) instanceof JSONObject ) {
				    	jsonResult = (JSONObject) jsonResult.get(key) ;
				    }
				}
				
				JSONArray revisions = (JSONArray) jsonResult.get("revisions");
				jsonResult = (JSONObject) revisions.get(0);
				
				jsonResult = (JSONObject) jsonResult.get("diff");
				
				div = (String) jsonResult.get("*");
				div = html2text(div);
	    	}catch(Exception e){
	    		//e.printStackTrace();	
	    	}
	    	//System.out.println(div);

	    }

	    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {

	    	//System.out.println("Success: "+ conn.getResponseCode() + conn.getResponseMessage());
	    //	System.out.println("Total Coord points added: "+allCoords.size());

	    }
	    else if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
	    
	    	throw new RuntimeException("Failed : HTTP error code : "+ conn.getResponseCode() + conn.getResponseMessage());

	    }
		return div;
	}


public static String html2text(String html) {
    return Jsoup.parse(html).text();
}


public static twitter4j.internal.org.json.JSONObject lookupPageViewCountAllAgents(String pageName, String timestamp,
		String language)  throws Exception {
	//https://wikimedia.org/api/rest_v1/metrics/pageviews/per-article/en.wikipedia/all-access/all-agents/Albert_Einstein/daily/2015100100/2015103100
	
	
	//timestamp = URLEncoder.encode(timestamp);
	timestamp = MiscFunctions.createYYYYMMDDHHString(timestamp);
	String timestampAWeekBefore = MiscFunctions.decreaseYYYYMMDDHHStringDateByDays(timestamp, 14);
	
	pageName = pageName.replace(" ", "_");
	
	String urlStr = "https://wikimedia.org/api/rest_v1/metrics/pageviews/per-article/"+language+".wikipedia/all-access/all-agents/"+pageName+"/daily/"+timestampAWeekBefore+"/"+timestamp;

	// 2015-10-01 %20 %02 3A 01 %3A 03
	///https://en.wikipedia.org/w/api.php?action=query&prop=revisions&format=json&rvprop=timestamp%7Cuser%7Ccomment%7Ccontent&rvlimit=1&rvdiffto=prev&rvstart=2015-10-01%20%023A01%3A03&titles=Every%20Trick%20in%20the%20Book
	URL url = new URL(urlStr);
   //URL url = new URL("http://openls.geog.uni-heidelberg.de/testing2015/route?Start=8.6817521,49.4173462&End=8.6828883,49.4067577&Via=&lang=en&distunit=KM&routepref=Fastest&avoidAreas=&useTMC=false&noMotorways=false&noTollways=false&instructions=false");

	HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setDoOutput(true);
    conn.setDoInput(true);
    conn.setRequestMethod("GET");
    //conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
    //conn.setRequestProperty("Accept", "application/vnd.zooevents.stream.v1+json");

    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

    String line;
    String div = "null";
    JSONObject jsonResult = new JSONObject();
    twitter4j.internal.org.json.JSONObject pageViews= new twitter4j.internal.org.json.JSONObject();
    String timestamp_str;
    int pageView_cnt;
    while ((line = br.readLine()) != null) {
    	try{
	    	jsonResult = (JSONObject) JSONSerializer.toJSON(line);
			JSONArray items = (JSONArray) jsonResult.get("items");
			for(int i =0; i<items.size(); i++){
				
				timestamp_str = items.getJSONObject(i).getString("timestamp");
				pageView_cnt = items.getJSONObject(i).getInt("views");
				pageViews.put(timestamp_str, pageView_cnt);
			
			
			}
			
    	}catch(Exception e){
    		   		
    	}
    
    }

    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {

    	//System.out.println("Success: "+ conn.getResponseCode() + conn.getResponseMessage());
    //	System.out.println("Total Coord points added: "+allCoords.size());

    }
    else if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
    
    	throw new RuntimeException("Failed : HTTP error code : "+ conn.getResponseCode() + conn.getResponseMessage());

    }
	return pageViews;
}
		
	
	
	
	

}
