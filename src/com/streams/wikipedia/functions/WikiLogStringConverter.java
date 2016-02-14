package com.streams.wikipedia.functions;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.pircbotx.Colors;

import com.ramine.iplookup.IPAddressValidator;
import com.ramine.iplookup.IPFinder;
import com.ramine.iplookup.WikipediaAPILookup;

import twitter4j.internal.org.json.JSONObject;


public class WikiLogStringConverter {

	JSONObject wikiItem;
	DateFormat dateFormat;
	Date date;
	HashMap<String, JSONObject> countryCodesGeoLatLong;
	String divText;
	
	//for lookups
	IPFinder ipFinder;
	IPAddressValidator ipValidator;
	
	public WikiLogStringConverter() {
		// TODO Auto-generated constructor stub
		wikiItem = new JSONObject();
		dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		countryCodesGeoLatLong = new HashMap<String, JSONObject>();

		if(loadCountryCodes()){
			System.out.println("Country Codes Loaded! Total Countires: "+countryCodesGeoLatLong.size());
		}
		
		ipFinder = new IPFinder();
		ipValidator = new IPAddressValidator();
	}
	
	private boolean loadCountryCodes(){
		try{
			FileReader fr = new FileReader("country_lat_long");
			BufferedReader br = new BufferedReader(fr);
			String currentLine= "";
			while ((currentLine=br.readLine()) !=null){
				try {
					String countryCode = currentLine.split(",")[0];
					double lat = Double.parseDouble(currentLine.split(",")[1]);
					double lng = Double.parseDouble(currentLine.split(",")[2]);
					JSONObject geo = new JSONObject();
					geo.put("lat", lat);
					geo.put("lng", lng);
					countryCodesGeoLatLong.put(countryCode.toLowerCase().trim(), geo);
					//System.out.println("lang = " + countryCode.toLowerCase().trim() );

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			br.close();
			fr.close();
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		
		
	}
	
	public String convertLogStringToJSONObject(String log){
		String newLogStr="";
		wikiItem = new JSONObject();
		try{
			newLogStr = Colors.removeFormattingAndColors(log);
			//first let's get the language. All messages should be supplied with this
			String lng="";
			try{
				lng = newLogStr.split("PRIVMSG #")[1];
				lng = lng.split(".wikipedia ")[0];
				wikiItem.put("wikipedia_language",lng);
				
				//lets see if we can add the channel geo location as well.
				if(countryCodesGeoLatLong.containsKey(lng)){
					wikiItem.put("wikipedia_language_geo", countryCodesGeoLatLong.get(lng));
					//System.out.println(newLogStr);

				}				
			}catch(Exception e){
				wikiItem.put("wikipedia_language","u");
			}
			
			
			
			//System.out.println(newLogStr);
			String firstSplit = newLogStr.split("\\]\\]")[0];
			String actionType = firstSplit.split("\\[\\[")[1];
			
			
			
			if(actionType.contains("Talk") || actionType.contains("Discussion") ){
				date = new Date();
				wikiItem.put("timestamp", dateFormat.format(date));
				String name = actionType.split(":")[1];
				wikiItem.put("wikipedia_entry_type", "talk_entry");
				wikiItem.put("wikipedia_username", name);
				
				wikiItem.put("wikipedia_user", createWikiUserObject(name));
				
				
			}else if(actionType.contains("Project:")){
				date = new Date();
				wikiItem.put("timestamp", dateFormat.format(date));
				String name = actionType.split(":")[1];
				wikiItem.put("wikipedia_entry_type", "project_entry");
				String projectName = actionType.replace("Project:", "");
				try{
					projectName = projectName.split("/")[0];
				}catch(Exception e){}
				wikiItem.put("wikipedia_project_name", projectName );
				wikiItem.put("wikipedia_user", createWikiUserObject(name));
			}
			else if(actionType.contains("Special:Log")){
				String name = actionType.split(":Log/")[1];
				if(name.contains("newusers")){
					wikiItem.put("wikipedia_entry_type", "new_user_added");
					String tempOne = newLogStr.split(" \\* ")[1];
					String wikiusername = tempOne.split(" \\* ")[0];
					wikiItem.put("wikipedia_user", createWikiUserObject(wikiusername));
					date = new Date();
					wikiItem.put("timestamp", dateFormat.format(date));
				}else if(name.contains("abusefilter")){
					wikiItem.put("wikipedia_entry_type", "abuse_filter");
					String tempOne = newLogStr.split(" \\* ")[1];
					String wikiusername = tempOne.split(" \\* ")[0];
					wikiItem.put("wikipedia_user", createWikiUserObject(wikiusername));
					date = new Date();
					wikiItem.put("timestamp", dateFormat.format(date));
				}
			//these are the page revisions.
			}else if(!(actionType.contains(":") || actionType.contains("/"))){
				//let's try and get the related url
				try{
					String url = newLogStr.split(" http://")[1];
					url = url.split(" ")[0];
					url = "http://"+url;
					//System.out.println(url);
					wikiItem.put("wikipedia_activity_url", url);
				}catch(Exception e){
					wikiItem.put("wikipedia_activity_url", "");
				}
				
				date = new Date();
				wikiItem.put("timestamp", dateFormat.format(date));
				wikiItem.put("wikipedia_entry_type", "page_revision");
				wikiItem.put("wikipedia_page_name", actionType);
				String tempOne = newLogStr.split(" \\* ")[1];
				String wikiusername = tempOne.split(" \\* ")[0];
				
				wikiItem.put("wikipedia_user", createWikiUserObject(wikiusername));

				wikiItem.put("wikipedia_div_text", createWikiDivText(
						(String) wikiItem.get("wikipedia_page_name"), 
						(String) wikiItem.get("timestamp"), 
						(String) wikiItem.get("wikipedia_language")));
				
				wikiItem.put("wikipedia_page_views", createWikiPageViewCount(
						(String) wikiItem.get("wikipedia_page_name"), 
						(String) wikiItem.get("timestamp"), 
						(String) wikiItem.get("wikipedia_language")));
				
				
				try{
					String pageURI = "http://"+lng+".wikipedia.org/wiki/"+actionType.replace(" ", "_");
					wikiItem.put("wikipedia_page_url", pageURI);
					//System.out.println(pageURI);

				}catch(Exception e){
					wikiItem.put("wikipedia_page_url", "");
				}
			}
			
//			newLogStr=log.replaceAll("\u0003[0-9]{1,2}(,[0-9]{1,2})?", "");
			//newLogStr=log.replaceAll(" \u0003[0-9]{1,2}(,[0-9]{1,2})?", "");
		}catch (Exception e) {
			//e.printStackTrace();
			return null;
		}
		
		if(wikiItem.has("timestamp")){
			//System.out.println(wikiItem.toString());

			return wikiItem.toString(); 
		}else{
			return null;
		}
	}

	private JSONObject createWikiUserObject(String wikiusername) {
		JSONObject wikipediaUser = new JSONObject();
		try{
		wikipediaUser.put("username", wikiusername);
		//construct a page url
		//do a lookup for location....
			if(ipValidator.validate(wikiusername)){
				wikipediaUser.put("user_country", ipFinder.findIPtoCountryName(wikiusername));
				wikipediaUser.put("user_city", ipFinder.findIPtoCityName(wikiusername));
				wikipediaUser.put("user_countryCode", ipFinder.findIPtoCountryCode(wikiusername));					
				wikipediaUser.put("user_geo", ipFinder.findIPtoLatLongObject(wikiusername));		
			}
	
		}catch(Exception e2){
			
		}
		return wikipediaUser;
	}
	
	private String createWikiDivText(String pageName, String timestamp, String language){
			try{
				return WikipediaAPILookup.lookupDiff(pageName, timestamp, language);
			}catch(Exception e){
				e.printStackTrace();
				return "";
			}
	}
	
	
	private JSONObject createWikiPageViewCount(String pageName, String timestamp, String language){
		try{
			return WikipediaAPILookup.lookupPageViewCountAllAgents(pageName, timestamp, language);
		}catch(Exception e){
			//e.printStackTrace();
			return new JSONObject();
		}
}
	
	
	
	
	
}

