package com.ramine.rabbmitMQ.objects;

import java.util.Date;

import com.ramine.functions.MiscFunctions;

public class RabbitMQTwitterObj {
	
	 
	private String id;
	private Date timestamp;
	private Date timestamp_to_min;

	private String	twitter_entry_type;
	private String twitter_username;
	private String twitter_page_name;
	private String twitter_text;
	
	public RabbitMQTwitterObj() {
		// TODO Auto-generated constructor stub
	}
	
	 public void settwitter_username(String twitter_username) {
		this.twitter_username = twitter_username;
	}public void settwitter_page_name(String twitter_page_name) {
		this.twitter_page_name = twitter_page_name;
	}public void settwitter_entry_type(String twitter_entry_type) {
		this.twitter_entry_type = twitter_entry_type;
	}public void setId(String id) {
		this.id = id;
	}public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}public String gettwitter_username() {
		return twitter_username;
	}public String gettwitter_page_name() {
		return twitter_page_name;
	}public Date getTimestamp() {
		return timestamp;
	}public String getId() {
		return id;
	}public String gettwitter_entry_type() {
		return twitter_entry_type;
	}@Override
	public String toString() {
		// TODO Auto-generated method stub
		return twitter_entry_type;
	}
	


	public Date getTimestampToMinute() {
		// TODO Auto-generated method stub
		String tmp = MiscFunctions.convertDateTimeToString(timestamp);
		//System.out.println(MiscFunctions.createUTCDate(tmp));
		return MiscFunctions.createUTCDate(tmp);
	}
	
	public void setTwitter_text(String twitter_text) {
		this.twitter_text = twitter_text;
	}public String getTwitter_text() {
		return twitter_text;
	}

}
