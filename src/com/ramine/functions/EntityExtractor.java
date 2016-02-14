package com.ramine.functions;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;


import org.jsoup.Jsoup;

import net.sf.json.JSONObject;

public class EntityExtractor {
	
	
	int nounPhraseSize;
	//MarkupParser parser;
	HashMap<String, Boolean> entities_uniqueMap;
	ArrayList<String> entities_uniqueList;

	public EntityExtractor(int nounPhraseSize) {
		// TODO Auto-generated constructor stub
		
		this.nounPhraseSize = nounPhraseSize;
		
		
		entities_uniqueMap = new HashMap<String, Boolean>();
		entities_uniqueList = new ArrayList<String>();

	}
	
	
	

	public ArrayList<JSONObject> extractAllEntitiesAndCleanDivtext(ArrayList<JSONObject> wikiJSONEntries) {
	
		JSONObject entryI = new JSONObject();
		JSONObject entryJ = new JSONObject();
		
		for(int i = 0; i< wikiJSONEntries.size(); i++){
			 
			try{
				entryI = wikiJSONEntries.get(i);
				
				//first get all the nice entitied - this list is unique.
				ArrayList<String> ngrams = extractMatchingFunctionEntity(entryI.getString("div_text"));
				//update the original entry with a cleaned div
				entryI.put("div_text", WikiMarkupRemoval(entryI.getString("div_text")));
				for (String ngram: ngrams){
					entities_uniqueMap.put(ngram, true);
				}
			
					
				}catch (Exception e) {
					// TODO: handle exception
				}
		
		}
		
		for(Entry<String, Boolean> trigram: entities_uniqueMap.entrySet()){
			entities_uniqueList.add(trigram.getKey());
			
		}
		
		System.out.println("Total Unique unigrams: "+entities_uniqueMap.size());
		
		return wikiJSONEntries;

	}
			
	public ArrayList<String> extractMatchingFunctionEntity(String object) {

		ArrayList<String> nGrams = new ArrayList<String>();
		HashMap<String, Boolean> nGramsMap = new HashMap<String, Boolean>();
		//System.out.println("BEFORE: "+object);
		object = WikiMarkupRemoval(object);
//		WikiModel model = new WikiModel("http://en.wikipedia.org/wiki/${image}", "http://en.wikipedia.org/wiki/${title}");
//		object = model.render(new PlainTextConverter(), object);
		//object = parser.parseToHtml(object);
		//object = Jsoup.parse(object).text();
		//System.out.println("AFTER: "+object+"\n");
		String[] words = object.trim().split(" "); 
		String test = "";
		char testChar;
		
		
		
		Boolean proceed = false;
		for(int i = 0; i<words.length; i++){
			proceed = false;
			try{
				String matchingNGram = "";
				test = words[i].trim();
				if(test.length()>1){
					testChar = test.charAt(0);
					if(isUpperLetter(testChar)){
						//System.out.println(testChar);
						proceed = true;
						matchingNGram = matchingNGram + test;
						int pos = i;
						int wordsInNgram = 0;
						while(proceed && (pos<words.length-1)){// && (wordsInNgram<nounPhraseSize)){
							pos = pos + 1;
							test = words[pos];
							testChar = test.charAt(0);
							if(isUpperLetter(testChar)){
								//System.out.println(matchingNGram);
								matchingNGram = matchingNGram + " " + test+" ";
								wordsInNgram++;
							}else{
								proceed = false;
							}
						}
						
					}
				}
				matchingNGram = matchingNGram.replace("  ", " ");
				if(matchingNGram.split(" ").length >= nounPhraseSize){
					//System.out.println(matchingNGram);
					nGramsMap.put(matchingNGram, true);
					i = i + matchingNGram.length();
				}
				
			}catch(Exception e){
				//e.printStackTrace();
				//System.out.println("ERROR word: "+test);
			}
			
		}
		
		//Remove duplicates, as this is not desired....
		for(Entry<String, Boolean> entry: nGramsMap.entrySet()){
			nGrams.add(entry.getKey());
		}
	//	System.out.println("Total Unique unigrams: "+nGrams.size());

		return nGrams;
		
	}
	
	
	private boolean isUpperLetter(char c) {
	    return (c >= 'A' && c <= 'Z');
	}	
			
			
			
	public String WikiMarkupRemoval(String object){
		
		
		object =  object.
				 replace("=", " ")
				.replace("==", " ")
				.replace("===", " ")
				.replace("====", " ")
				.replace("=====", " ")
				.replace("======", " ")
				.replace("=======", " ")
				.replace("**", " ")
				.replace("*", " ")
				.replace("&ndash;", " ")
				.replace("--", " ")
				.replace("|", " ")
				.replace("{{", " ")
				.replace("}}", " ")
				.replace("[[", " ")
				.replace("]]", " ")
				.replace("[", " ")
				.replace("]", " ")
				.replace("(", " ")
				.replace(")", " ")
				.replace(">", " ")
				.replace("\"", " ")
				//.replace("\t", " ")
				//.replace(" . ", " ")
				.replace(",", " ")
				.replace("'s", " ")
				.replace("Image:", " ")
				.replace("url=", " ")
				.replace("'", " ")
				.replace("'''", " ")
				.replace("<!--", " ")
				.replace("<!", " ")
				.replace("-->", " ")
				.replace("/ref>", " ")
				.replace("<ref", " ")
				.replace("</ref", " ")
				.replace("<br", " ")
				.replace("<ref name", " ")
				.replace(". ", " ");				
				
		return object.trim();
				
				
	}
			
			
	
	public HashMap<String, Boolean> getEntities_uniqueMap() {
		return entities_uniqueMap;
	}
			
			
			public ArrayList<String> getEntities_uniqueList() {
				return entities_uniqueList;
			}
			
			
			
			
			
			

}
