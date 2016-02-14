package com.streams.twitter.stream;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

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
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.internal.org.json.JSONArray;
import twitter4j.internal.org.json.JSONObject;

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
	private JSONObject tweetObj;
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
		twitter = setStream();
		tweetObj = new JSONObject();
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

	public GetTweetsRECOIN(String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret) {
		this.consumerKey = consumerKey;
		this.consumerSecret = consumerSecret;
		this.accessToken = accessToken;
		this.accessTokenSecret = accessTokenSecret;

		// TODO Auto-generated constructor stub
		lastID = 0;
		twitter = setStream();
		tweetObj = new JSONObject();
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

	public GetTweetsRECOIN(TwitterCredentials credentials) {
		this.consumerKey = credentials.getConsumerKey();
		this.consumerSecret = credentials.getConsumerSecret();
		this.accessToken = credentials.getAccessToken();
		this.accessTokenSecret = credentials.getAccessTokenSecret();

		this.credentials = credentials;
		
		lastID = 0;
		twitter = setStream();
		tweetObj = new JSONObject();
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
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true);
		cb.setOAuthConsumerKey(consumerKey);
		cb.setOAuthConsumerSecret(consumerSecret);
		cb.setOAuthAccessToken(accessToken);
		cb.setOAuthAccessTokenSecret(accessTokenSecret);
		// TwitterFactory tf = new TwitterFactory(cb.build());
		System.out.println("Set Stream with credentials");
		return twitter = new TwitterStreamFactory(cb.build()).getInstance();

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
			for (int i = 0; i < status.getUserMentionEntities().length; i++) {
				mentions.put(status.getUserMentionEntities()[i].getText());
			}
			tweetObj.put("mentions", mentions);

			for (int i = 0; i < status.getHashtagEntities().length; i++) {
				hashtags.put(status.getHashtagEntities()[i].getText());
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
				tweetUrls.put(status.getURLEntities()[i].getURL());
			}
			tweetObj.put("urls", tweetUrls);
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
		twitter.addListener(listener);

		// the various types of harvesters

		if (credentials.isKeywordTracker()) {
			FilterQuery query = new FilterQuery();
			String[] wordsToTrack = credentials.getKeywordToTrack().split(" ");
			query.track(wordsToTrack);
			twitter.filter(query);
		} else if (credentials.isGeoTracker()) {
			FilterQuery query = new FilterQuery();
			double box[][] = { { credentials.getGeoBoundingBox()[0], credentials.getGeoBoundingBox()[1] }, 
					{ credentials.getGeoBoundingBox()[2], credentials.getGeoBoundingBox()[3] } };
			query.locations(box);
			twitter.filter(query);
		} else if (credentials.isSampleTracker()) {
			twitter.sample();
		}
	}

	// private boolean allTweets(Status status) {
	//
	// // is it english?
	// // does it contain a hashtag?
	// try {
	// // clear object
	// tweetObj = new JSONObject();
	// hashtags = new JSONArray();
	// geoLocation = new JSONObject();
	// tweetUrls = new JSONArray();
	// mentions = new JSONArray();
	// retweetObj = new JSONObject();
	// tweetObj.put("_source", "twitter");
	// tweetObj.put("id", status.getId());
	// tweetObj.put("timestamp", dateFormat.format(status.getCreatedAt()));
	// tweetObj.put("text", status.getText());
	// tweetObj.put("screen_name", status.getUser().getScreenName());
	// tweetObj.put("isRetweet", status.isRetweet());
	// tweetObj.put("lang", status.getIsoLanguageCode());
	//
	// if (status.isRetweet()) {
	// retweetObj.put("retweeted_tweet_id",
	// status.getRetweetedStatus().getId());
	// retweetObj.put("retweeted_tweet_timestamp",
	// status.getRetweetedStatus().getCreatedAt().toString());
	// retweetObj.put("retweeted_tweet_screen_name",
	// status.getRetweetedStatus().getUser().getScreenName());
	// tweetObj.put("retweet_tweet", retweetObj);
	//
	// }
	// for (int i = 0; i < status.getUserMentionEntities().length; i++) {
	// mentions.put(status.getUserMentionEntities()[i].getText());
	// }
	// tweetObj.put("mentions", mentions);
	//
	// for (int i = 0; i < status.getHashtagEntities().length; i++) {
	// hashtags.put(status.getHashtagEntities()[i].getText());
	// }
	// tweetObj.put("hashtags", hashtags);
	// try {
	// geoLocation.put("lat", status.getGeoLocation().getLatitude());
	// geoLocation.put("lng", status.getGeoLocation().getLongitude());
	// } catch (Exception e) {
	// // TODO: handle exception
	// }
	// tweetObj.put("geo", geoLocation);
	// for (int i = 0; i < status.getURLEntities().length; i++) {
	// tweetUrls.put(status.getURLEntities()[i].getURL());
	// }
	// tweetObj.put("urls", tweetUrls);
	// return true;
	// } catch (Exception e) {
	// // TODO: handle exception
	// }
	//
	// return false;
	// }

	// public void getStreamWithCacheCheck(final Channel channel, final String
	// EXCHANGE_NAME, final HashMap<Long, Boolean> duplicateCache ){
	// StatusListener listener = new StatusListener(){
	// public void onStatus(Status status) {
	// //System.out.println(status.toString());
	// try{
	// if(tweetLogicTest(status)){
	// System.out.println(tweetObj.toString());
	// if(!duplicateCache.containsKey((Long) status.getId())){
	// channel.basicPublish(EXCHANGE_NAME, "", null,
	// tweetObj.toString().getBytes());
	// duplicateCache.put(status.getId(), true);
	// }
	// }
	// //System.out.println(status.toString());
	// //channel.basicPublish(EXCHANGE_NAME, "", null, status.get);
	// }catch (Exception e) {
	// e.printStackTrace();
	// }
	//
	// }
	//
	// public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice)
	// {}
	// public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}
	// public void onException(Exception ex) {
	// ex.printStackTrace();
	// }
	// @Override
	// public void onScrubGeo(long arg0, long arg1) {
	// // TODO Auto-generated method stub
	//
	// }
	// @Override
	// public void onStallWarning(StallWarning arg0) {
	// // TODO Auto-generated method stub
	// System.out.println("Stalling"+arg0.toString());
	//
	// }
	// };
	// // twitter = new TwitterStreamFactory().getInstance();
	// twitter.addListener(listener);
	// // sample() method internally creates a thread which manipulates
	// TwitterStream and calls these adequate listener methods continuously.
	//
	// //KOREAN
	//// double lon1 = -10.283; //west
	//// double lon2 = 35.991; // east
	//// double lat2 = 44.182; //north
	//// double lat1 = 35.839; //south
	//
	// //FilterQuery query = new FilterQuery();
	// //'138.346613','-35.157563','138.899025','-34.64243'
	//// double lon1 = 138.346613;
	//// double lon2 = 138.899025;
	//// double lat1 = -35.157563;
	//// double lat2 = -34.64243;
	// //double box[][] = {{lon1, lat1}, {lon2, lat2}};
	// //query.locations(box);
	// // String[] wordsToTrack = {" "};
	// // query.track(wordsToTrack);
	// //twitter.filter(query);
	// twitter.sample();
	// }

	// HashMap<Long, JSONObject> statuses = new HashMap<Long, JSONObject>();
	//
	// public void getStreamDeletedMessageStream(final Channel channel, final
	// String EXCHANGE_NAME, final String twitterDeletePulseExchangeName){
	// StatusListener listener = new StatusListener(){
	// public void onStatus(Status status) {
	// //System.out.println(status.getUser().getName() + " : " +
	// status.getText());
	// try{
	// if(allTweets(status)){
	// statuses.put(status.getId(), tweetObj);
	// //System.out.println(tweetObj.toString());
	// //channel.basicPublish(EXCHANGE_NAME, "", null,
	// tweetObj.toString().getBytes());
	// }
	// //System.out.println(statuses.size());
	// //channel.basicPublish(EXCHANGE_NAME, "", null, status.get);
	// }catch (Exception e) {
	// e.printStackTrace();
	//
	// }
	//
	// //dont let the hash get too big, this could bit a bit memory hungry...
	// if(statuses.size()>500000){
	// statuses.clear();
	// System.out.println("Cleared Status List, got a little too long...");
	//
	// }
	//
	// }
	//
	// public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
	//
	// if(statuses.containsKey(statusDeletionNotice.getStatusId())){
	// //System.out.println("Known Status");
	// //System.out.println(statuses.get(statusDeletionNotice.getStatusId()));
	// JSONObject status = statuses.get(statusDeletionNotice.getStatusId());
	// date = new Date();
	// try{
	// status.put("deleted_timestamp", dateFormat.format(date));
	// }catch(Exception e){}
	// try{
	// channel.basicPublish(EXCHANGE_NAME, "", null,
	// status.toString().getBytes());
	// }catch(Exception e){
	//
	// }
	// }
	// //and then send a pulse no matter what!
	// try{
	//// date = new Date();
	//// pulse.put("deleted_timestamp", dateFormat.format(date));
	//// pulse.put("id", statusDeletionNotice.getStatusId());
	//// pulse.put("user_id", statusDeletionNotice.getUserId());
	// channel.basicPublish(twitterDeletePulseExchangeName, "", null,
	// createPulse(statusDeletionNotice).toString().getBytes());
	// }catch(Exception e){
	//
	// }
	//
	// //System.out.println(statusDeletionNotice.getUserId());
	//
	//
	// }
	// public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}
	// public void onException(Exception ex) {
	// //ex.printStackTrace();
	// }
	// @Override
	// public void onScrubGeo(long arg0, long arg1) {
	// // TODO Auto-generated method stub
	//
	// }
	// @Override
	// public void onStallWarning(StallWarning arg0) {
	// // TODO Auto-generated method stub
	//
	// }
	// };
	// // twitter = new TwitterStreamFactory().getInstance();
	// twitter.addListener(listener);
	// // sample() method internally creates a thread which manipulates
	// TwitterStream and calls these adequate listener methods continuously.
	//// FilterQuery query = new FilterQuery();
	//// String[] wordsToTrack = {"wikipedia", "wiki", "en.wikipedia.org"};
	//// query.track(wordsToTrack);
	// //twitter.filter(query);
	// twitter.sample();
	// }
	//
	// private JSONObject createPulse(StatusDeletionNotice notice) throws
	// Exception{
	// pulse = new JSONObject();
	// pulse.put("_source", "twitter");
	// pulse.put("twitter_delete_heartbeat", "true");
	// date = new Date();
	// pulse.put("deleted_timestamp", dateFormat.format(date));
	// pulse.put("id", notice.getStatusId());
	// pulse.put("user_id", notice.getUserId());
	// return pulse;
	// }
	//
	// public static void main(String[] args) {
	//
	// GetTweetsRECOIN tweets = new GetTweetsRECOIN();
	// tweets.setStream();
	//
	// }

}