package com.ramine.rabbmitMQ.objects;

import java.util.Date;

import com.ramine.functions.MiscFunctions;

public class RabbitMQWikiObj {
	
	 
	private String id;
	private Date timestamp;
	private Date timestamp_to_min;

	private String	wikipedia_entry_type;
	private String wikipedia_username;
	private String wikipedia_page_name;
	
	public RabbitMQWikiObj() {
		// TODO Auto-generated constructor stub
	}
	
	 public void setWikipedia_username(String wikipedia_username) {
		this.wikipedia_username = wikipedia_username;
	}public void setWikipedia_page_name(String wikipedia_page_name) {
		this.wikipedia_page_name = wikipedia_page_name;
	}public void setWikipedia_entry_type(String wikipedia_entry_type) {
		this.wikipedia_entry_type = wikipedia_entry_type;
	}public void setId(String id) {
		this.id = id;
	}public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}public String getWikipedia_username() {
		return wikipedia_username;
	}public String getWikipedia_page_name() {
		return wikipedia_page_name;
	}public Date getTimestamp() {
		return timestamp;
	}public String getId() {
		return id;
	}public String getWikipedia_entry_type() {
		return wikipedia_entry_type;
	}@Override
	public String toString() {
		// TODO Auto-generated method stub
		if(!wikipedia_entry_type.contains("revision")){
		return wikipedia_entry_type;}
		else{
			return wikipedia_page_name;
		}
	}
	


	public Date getTimestampToMinute() {
		// TODO Auto-generated method stub
		String tmp = MiscFunctions.convertDateTimeToString(timestamp);
		//System.out.println(MiscFunctions.createUTCDate(tmp));
		return MiscFunctions.createUTCDate(tmp);
	}
	
	

}
