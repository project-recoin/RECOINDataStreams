package com.streams.wikipedia.cascades;

import java.util.ArrayList;
import java.util.HashMap;

import net.sf.json.JSONObject;

import com.rabbitmq.client.Channel;
import com.ramine.functions.EntityExtractor;
import com.ramine.functions.MiscFunctions;
import com.streams.wikipedia.objects.Cascade;
import com.streams.wikipedia.objects.WikipediaEdit;

public class WikipediaCascadeConstructor {

	EntityExtractor extractor;
	HashMap<String, Cascade> cascade_cache;
	Channel broadcast_channel;
	String channel_name;
	
	public WikipediaCascadeConstructor(Channel channel_out, String outgoingExchangeName, int cascasdeConstructionStringSize) {
		// TODO Auto-generated constructor stub
		this.broadcast_channel = channel_out;
		this.channel_name = outgoingExchangeName;
		cascade_cache = new HashMap<String, Cascade>();
		
		extractor = new EntityExtractor(cascasdeConstructionStringSize);
		
	}
	
	
	
	public void processNewData(JSONObject jsonEntry) {

		
		//first check if the entry contains any identifier which is relevant....
		try{	
			ArrayList<String> unigrams = extractor.extractMatchingFunctionEntity(jsonEntry.getString("wikipedia_div_text"));

			for(String unigram : unigrams){
			
				//is it already in the cache.
				WikipediaEdit edit = constructEdit(jsonEntry);

				if(cascade_cache.containsKey(unigram)){
					
					Cascade c = cascade_cache.get(unigram);
					c.getChildren().put(MiscFunctions.createUTCDate(jsonEntry.getString("timestamp")), edit);
					cascade_cache.put(unigram, c);
					System.out.println("INFO: Adding to Existing Cascade");

				}else{
					
					Cascade c = new Cascade();
					c.setMatching_entity(unigram);
					c.getChildren().put(MiscFunctions.createUTCDate(jsonEntry.getString("timestamp")), edit);
					cascade_cache.put(unigram, c);
					System.out.println("INFO: New Cascade Identifer");

				}
							
				System.out.println(unigram);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		
		
		
	}



	private WikipediaEdit constructEdit(JSONObject jsonEntry) {
		WikipediaEdit edit = new WikipediaEdit();
		String id = jsonEntry.getString("wikipedia_page_name")+"_"+jsonEntry.getString("timestamp");
		edit.setId(id);
		edit.setEditData(MiscFunctions.createUTCDate(jsonEntry.getString("timestamp")));
		//edit.setRevisionText(revisionText);
		edit.setWikipedia_page(jsonEntry.getString("wikipedia_page_name"));
		return edit;
	}
	
	
	
	
	

}
