package com.streams.twitter.objects;

public class TwitterCredentials {
	
	private String ID;
	private String ConsumerKey;
	private String ConsumerSecret;
	private String AccessToken;
	private String AccessTokenSecret;
	private String keywordToTrack;
	private boolean isSampleTracker;
	private boolean isGeoTracker;
	private boolean isKeywordTracker;
	private double[] geoBoundingBox; 
	
	
	public TwitterCredentials() {
		// TODO Auto-generated constructor stub
	}
	
	public void setAccessToken(String accessToken) {
		AccessToken = accessToken;
	}public void setConsumerSecret(String consumerSecret) {
		ConsumerSecret = consumerSecret;
	}public void setID(String iD) {
		ID = iD;
	}public void setConsumerKey(String consumerKey) {
		ConsumerKey = consumerKey;
	}public void setAccessTokenSecret(String accessTokenSecret) {
		AccessTokenSecret = accessTokenSecret;
	}public String getID() {
		return ID;
	}public String getConsumerSecret() {
		return ConsumerSecret;
	}public String getConsumerKey() {
		return ConsumerKey;
	}public String getAccessTokenSecret() {
		return AccessTokenSecret;
	}public String getAccessToken() {
		return AccessToken;
	}public void setKeywordTracker(boolean isKeywordTracker) {
		this.isKeywordTracker = isKeywordTracker;
	}public void setKeywordToTrack(String keywordToTrack) {
		this.keywordToTrack = keywordToTrack;
	}public void setGeoTracker(boolean isGeoTracker) {
		this.isGeoTracker = isGeoTracker;
	}public void setGeoBoundingBox(double[] geoBoundingBox) {
		this.geoBoundingBox = geoBoundingBox;
	}public boolean isKeywordTracker() {
		return isKeywordTracker;
	}public boolean isGeoTracker() {
		return isGeoTracker;
	}public String getKeywordToTrack() {
		return keywordToTrack;
	}public double[] getGeoBoundingBox() {
		return geoBoundingBox;
	}public boolean isSampleTracker() {
		return isSampleTracker;
	}public void setSampleTracker(boolean isSampleTracker) {
		this.isSampleTracker = isSampleTracker;
	}
	
	

}
