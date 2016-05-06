package com.ramine.emitter.twitter.recoin;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.IOUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import com.ramine.receiver.MultipleTwitterFeedAggregator.MyRunnable;
import com.streams.trends.Trends;
import com.streams.twitter.objects.TwitterCredentials;
import com.streams.twitter.stream.GetTweetsLocation;
import com.streams.twitter.stream.GetTweetsRECOIN;
import com.streams.twitter.stream.GetTweetsSocPub;
import com.streams.twitter.stream.country.GetAdelaideTweets;
import com.streams.twitter.stream.country.GetTweetsSouthKorea;
import com.streams.twitter.stream.country.GetTweetsUK;
import com.streams.twitter.stream.country.GetTweetsUS;
import com.streams.wikipedia.WikipediaChannel;
import com.streams.wikipedia.WikipediaIRCConnect;

public class RECOINEmitter {

	private static String EXCHANGE_NAME = "twitter_RECOIN";
	private static String rabbitHost = "";
	private static String rabbitUsername = "";
	private static String rabbitPassword = "";

	private static final int MYTHREADS = 30;

	public static void main(String[] argv) throws Exception {

		// first let's load some different keys
		HashMap<String, TwitterCredentials> apiCredentials = new HashMap<>();


		try{
			JSONArray tmp = new JSONArray();
			tmp = loadKeys("config/harvester_config.json");
			JSONObject harvester_config = tmp.getJSONObject(0);
			EXCHANGE_NAME = harvester_config.getString("rabbitMQExchangeName");
			rabbitHost = harvester_config.getString("rabbitMQHost");
			rabbitUsername = harvester_config.getString("rabbitMQUsername");
			rabbitPassword = harvester_config.getString("rabbitMQPassword");
			System.out.printf("Harvester Config Loaded! Wee'll be using %s as the rabbmitMQ exchage. Get ready, things are about to get wild.\n", EXCHANGE_NAME);
		}catch(Exception e){
			System.err.print("looks like your harvester_config.json couldnt be found... have you got a config folder? Have you had enough coffee?");
			System.exit(1);
		}


		
		
		
		JSONArray config_file = new JSONArray();		
		config_file = loadKeys("config/twitter_config.json");
		if (config_file.size() > 0) {
			System.out.println("Twitter API keys successfully loaded");
			apiCredentials = loadCredentials(config_file, apiCredentials);
			System.out.printf("We have %d Streams accessible to us \n", apiCredentials.size());
		} else {
			System.err.print(
					"Opps, Looks like there's an error loading the keys, go check your config file and try again");
			System.exit(1);
		}
		
		System.setProperty("twitter4j.jsonStoreEnabled", "true");

		

		ArrayList<GetTweetsRECOIN> tweetHarvesters = new ArrayList<GetTweetsRECOIN>();

		for (Entry<String, TwitterCredentials> twit : apiCredentials.entrySet()) {

			GetTweetsRECOIN harvester = new GetTweetsRECOIN(twit.getValue());
			tweetHarvesters.add(harvester);
		}

		System.out.printf("We have %d Twitter Harvesters Ready to go \n", tweetHarvesters.size());

		ExecutorService executor = Executors.newFixedThreadPool(MYTHREADS);
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(rabbitHost);
//		factory.setUsername(rabbitUsername);
//		factory.setPassword(rabbitPassword);
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
		channel.exchangeDeclare(EXCHANGE_NAME, "fanout");

		for (GetTweetsRECOIN con : tweetHarvesters) {
			Runnable worker = new MyRunnable(con, channel, EXCHANGE_NAME);
			executor.execute(worker);
		}

	}

	private static HashMap<String, TwitterCredentials> loadCredentials(JSONArray config_file2,
			HashMap<String, TwitterCredentials> apiCredentials) {

		for (int i = 0; i < config_file2.size(); i++) {
			JSONObject credents = config_file2.getJSONObject(i);
			System.out.println(credents.get("ID"));
			TwitterCredentials cred = new TwitterCredentials();
			cred.setID(credents.getString("ID"));
			cred.setConsumerKey(credents.getString("ConsumerKey"));
			cred.setConsumerSecret(credents.getString("ConsumerSecret"));
			cred.setAccessToken(credents.getString("AccessToken"));
			cred.setAccessTokenSecret(credents.getString("AccessTokenSecret"));
			if (credents.getString("HarvesterType").equals("Keyword")) {
				cred.setKeywordTracker(true);
				cred.setSampleTracker(false);
				cred.setGeoTracker(false);
				cred.setKeywordToTrack(credents.getString("Keyword"));
			} else if (credents.getString("HarvesterType").equals("Location")) {
				cred.setKeywordTracker(false);
				cred.setSampleTracker(false);
				cred.setGeoTracker(true);
				JSONArray boundingBox = credents.getJSONArray("GeoBox");
				double[] box = new double[4];
				for (int j = 0; j < boundingBox.size(); j++) {
					box[j] = boundingBox.getDouble(j);
					System.out.println(box[j]);
				}
				cred.setGeoBoundingBox(box);
			} else if (credents.getString("HarvesterType").equals("SampleFeed")) {
				cred.setKeywordTracker(false);
				cred.setSampleTracker(true);
				cred.setGeoTracker(false);
			}
			apiCredentials.put(cred.getID(), cred);
		}

		return apiCredentials;
	}

	private static JSONArray loadKeys(String configFilepath) {

		try {
			JSONArray jsonKeys = new JSONArray();
			File f = new File(configFilepath);
			if (f.exists()) {
				InputStream is = new FileInputStream(configFilepath);
				String jsonTxt = IOUtils.toString(is);
				jsonKeys = (JSONArray) JSONSerializer.toJSON(jsonTxt.toString());
				
			}
			return jsonKeys;
		} catch (Exception e) {
			e.printStackTrace();
			return new JSONArray();
		}

	}
	
	


	private static String joinStrings(String[] strings, String delimiter) {
		int length = strings.length;
		if (length == 0)
			return "";
		StringBuilder words = new StringBuilder(strings[0]);
		for (int i = 1; i < length; i++) {
			words.append(delimiter).append(strings[i]);
		}
		return words.toString();
	}

	public static class MyRunnable implements Runnable {

		private final GetTweetsRECOIN harvester;
		private final Channel channel;
		private final String EXCHANGE_NAME;

		MyRunnable(GetTweetsRECOIN harvester, Channel channel, String EXCHANGE_NAME) {
			this.harvester = harvester;
			this.channel = channel;
			this.EXCHANGE_NAME = EXCHANGE_NAME;
		}

		@Override
		public void run() {

			harvester.getStream(channel, EXCHANGE_NAME);
		}

	}

}