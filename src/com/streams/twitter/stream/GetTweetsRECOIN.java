package com.streams.twitter.stream;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import com.rabbitmq.client.Channel;
import com.streams.twitter.objects.TwitterCredentials;

import twitter4j.FilterQuery;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.TwitterObjectFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
//import twitter4j.internal.org.json.JSONArray;
//import twitter4j.internal.org.json.JSONObject;
import twitter4j.json.DataObjectFactory;

public class GetTweetsRECOIN {

	private TwitterCredentials credentials;
	private String consumerKey = "";// "sAlpYrFeqdGEPE5rx6NlLnc5U";
	private String consumerSecret = "";// "jI4gakf33MKv4V6eKKQJDnMRw3zMn1EOarCaD6dcY7IS5sLN9m";
	private String accessToken = "";// "41944067-pAQOpBL0mddkID8YZaM4pT7lF8IgyB7eU2TaALT69";
	private String accessTokenSecret = "";// "SWe3yr7wzvHOmhHxBZyeW1J2ynL2S2dFnGg1B1qwi6JuP";
	long lastID; // the last ID of a Tweet to be captured.

	double lon1 = 0; // west
	double lon2 = 0; // east
	double lat2 = 0; // north
	double lat1 = 0; // south

	private TwitterStream twitter;
	private ConfigurationBuilder cb;
	private net.sf.json.JSONObject tweetObj;
	private JSONObject retweetObj;
	private JSONArray hashtags;
	private JSONObject geoLocation;
	private JSONArray tweetUrls;
	private JSONArray mentions;
	DateFormat dateFormat;
	Date date;

	private JSONObject pulse;

	public GetTweetsRECOIN() {
		// TODO Auto-generated constructor stub
		lastID = 0;
		this.twitter = setStream();
//		tweetObj = new JSONObject();
		hashtags = new JSONArray();
		geoLocation = new JSONObject();
		tweetUrls = new JSONArray();
		mentions = new JSONArray();

		retweetObj = new JSONObject();

		dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		// create pulse object
		try {
			pulse = new JSONObject();
			pulse.put("_source", "twitter");
			pulse.put("twitter_delete_heartbeat", "true");
		} catch (Exception e) {

		}

	}

	public void setlocation(double northLat, double southLat, double eastLng, double westLng) {
		this.lat2 = northLat;
		this.lat1 = southLat;
		this.lon1 = westLng;
		this.lon2 = eastLng;
	}

	
	public GetTweetsRECOIN(TwitterCredentials credentials) {
		this.consumerKey = credentials.getConsumerKey();
		this.consumerSecret = credentials.getConsumerSecret();
		this.accessToken = credentials.getAccessToken();
		this.accessTokenSecret = credentials.getAccessTokenSecret();

		this.credentials = credentials;
		
		lastID = 0;
		this.twitter = setStream();
		tweetObj = new net.sf.json.JSONObject();
		hashtags = new JSONArray();
		geoLocation = new JSONObject();
		tweetUrls = new JSONArray();
		mentions = new JSONArray();
		retweetObj = new JSONObject();
		dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		// create pulse object
		try {
			pulse = new JSONObject();
			pulse.put("_source", "twitter");
			pulse.put("twitter_delete_heartbeat", "true");
		} catch (Exception e) {

		}
	}

	public TwitterStream setStream() {

		System.setProperty("twitter4j.http.useSSL", "true");
		this.cb = new ConfigurationBuilder()
			.setDebugEnabled(true)
			.setOAuthConsumerKey(consumerKey)
			.setOAuthConsumerSecret(consumerSecret)
			.setOAuthAccessToken(accessToken)
			.setOAuthAccessTokenSecret(accessTokenSecret)
			.setJSONStoreEnabled(true);

//		TwitterFactory tf = new TwitterFactory(cb.build());
		System.out.println("Set Stream with credentials");
		return  new TwitterStreamFactory(cb.build()).getInstance();

	}

	private boolean tweetLogicTest(Status status) {

		// is it english?
		// if(status.getIsoLanguageCode().contains("en")){
		// does it contain a hashtag?
		// if(status.getText().contains("#")){
		try {
			// clear object
			tweetObj = new JSONObject();
			hashtags = new JSONArray();
			geoLocation = new JSONObject();
			tweetUrls = new JSONArray();
			mentions = new JSONArray();
			retweetObj = new JSONObject();

			tweetObj.put("_source", "twitter");
			tweetObj.put("id", status.getId());
			tweetObj.put("timestamp", dateFormat.format(status.getCreatedAt()));
			tweetObj.put("text", status.getText());
			tweetObj.put("screen_name", status.getUser().getScreenName());
			tweetObj.put("isRetweet", status.isRetweet());
			if (status.isRetweet()) {
				retweetObj.put("retweeted_tweet_id", status.getRetweetedStatus().getId());
				retweetObj.put("retweeted_tweet_timestamp", status.getRetweetedStatus().getCreatedAt().toString());
				retweetObj.put("retweeted_tweet_screen_name", status.getRetweetedStatus().getUser().getScreenName());
				tweetObj.put("retweet_tweet", retweetObj);

			}

			try{
				for(int x=0; x < status.getURLEntities().length; x++){
					tweetUrls.add(status.getURLEntities()[x].getURL());
				}
			}catch(Exception e){
				
			}
			
			tweetObj.put("urls", tweetUrls);

			for (int i = 0; i < status.getUserMentionEntities().length; i++) {
				mentions.add(status.getUserMentionEntities()[i].getText());
			}
			tweetObj.put("mentions", mentions);

			for (int i = 0; i < status.getHashtagEntities().length; i++) {
				hashtags.add(status.getHashtagEntities()[i].getText());
			}
			tweetObj.put("hashtags", hashtags);
			try {
				geoLocation.put("lat", status.getGeoLocation().getLatitude());
				geoLocation.put("lng", status.getGeoLocation().getLongitude());
			} catch (Exception e) {
				// TODO: handle exception
			}
			tweetObj.put("geo", geoLocation);
			for (int i = 0; i < status.getURLEntities().length; i++) {
				tweetUrls.add(status.getURLEntities()[i].getURL());
			}
			try{
				String rawJson = DataObjectFactory.getRawJSON(status);
				net.sf.json.JSONObject raw = (net.sf.json.JSONObject) JSONSerializer.toJSON(rawJson);
				tweetObj.put("status_raw", raw);
			}catch(Exception e){
				e.printStackTrace();
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}
		// }

		// }

		return false;
	}

	public void getStream(final Channel channel, final String EXCHANGE_NAME) {
		StatusListener listener = new StatusListener() {
			public void onStatus(Status status) {
				// System.out.println(status.toString());
				try {
					if (tweetLogicTest(status)) {
						//System.out.println(tweetObj.toString());
						channel.basicPublish(EXCHANGE_NAME, "", null, tweetObj.toString().getBytes());
					}
					// System.out.println(status.toString());
					// channel.basicPublish(EXCHANGE_NAME, "", null,
					// status.get);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

			public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
			}

			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
			}

			public void onException(Exception ex) {
				ex.printStackTrace();
			}

			@Override
			public void onScrubGeo(long arg0, long arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStallWarning(StallWarning arg0) {
				// TODO Auto-generated method stub
				System.out.println("Stalling" + arg0.toString());

			}
		};
		// twitter = new TwitterStreamFactory().getInstance();
		this.twitter.addListener(listener);

		// the various types of harvesters

		if (credentials.isKeywordTracker()) {
			FilterQuery query = new FilterQuery();
			String[] wordsToTrack = credentials.getKeywordToTrack().split(" ");
			query.track(wordsToTrack);
			this.twitter.filter(query);
		} else if (credentials.isGeoTracker()) {
			FilterQuery query = new FilterQuery();
			double box[][] = { { credentials.getGeoBoundingBox()[0], credentials.getGeoBoundingBox()[1] }, 
					{ credentials.getGeoBoundingBox()[2], credentials.getGeoBoundingBox()[3] } };
			query.locations(box);
			this.twitter.filter(query);
		} else if (credentials.isSampleTracker()) {
			this.twitter.sample();
		}
	}



}
