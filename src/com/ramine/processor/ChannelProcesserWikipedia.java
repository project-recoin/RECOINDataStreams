package com.ramine.processor;

import java.util.ArrayList;
import java.util.Date;









import java.util.HashMap;

import javafx.application.Platform;
import javafx.concurrent.Task;
import mx.bigdata.jcalais.CalaisClient;
import mx.bigdata.jcalais.CalaisObject;
import mx.bigdata.jcalais.CalaisResponse;
import mx.bigdata.jcalais.rest.CalaisRestClient;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import com.ramine.functions.MiscFunctions;
import com.ramine.rabbmitMQ.objects.RabbitMQWikiObj;
import com.ramine.viewer.graph.WikipediaNodeGraph;
import com.ramine.viewer.metrics.WikipediaMetricsText;
import com.ramine.viewer.timeseries.WikipediaActivityTimeSeries;
import com.ramine.viewer.timeseries.WikipediaMetricBarGraph;
import com.ramine.viewer.timeseries.WikipediaMetricLineGraph;
import com.ramine.viewer.timeseries.WikipediaPageMetricScatterGraph;
import com.sun.javafx.scene.control.skin.VirtualFlow.ArrayLinkedList;

public class ChannelProcesserWikipedia extends Task<Void> {

	private Channel channel;
	private String EXCHANGE_NAME = "";
	
	private ArrayList<String> channels;
	
	private HashMap<String, Integer> WikiUsers;
	private HashMap<String, HashMap<String, Integer>> WikiPagesAndUsers;
	private ArrayList<RabbitMQWikiObj> queue_all;
	private ArrayList<RabbitMQWikiObj> queue_revisions;
	private ArrayList<RabbitMQWikiObj> queue_talk;
	private ArrayList<RabbitMQWikiObj> queue_abuse;
	private ArrayList<RabbitMQWikiObj> queue_newUsers;

	private HashMap<String, Integer> total_item_counts;

	private CalaisClient calaisClient;

	public ChannelProcesserWikipedia(Channel channel, String root_exchange_name) {
		// TODO Auto-generated constructor stub

		channels = new ArrayList<String>();
		this.channel = channel;
		queue_all = new ArrayList<RabbitMQWikiObj>();
		queue_revisions = new ArrayList<RabbitMQWikiObj>();
		queue_talk = new ArrayList<RabbitMQWikiObj>();
		queue_abuse = new ArrayList<RabbitMQWikiObj>();
		queue_newUsers = new ArrayList<RabbitMQWikiObj>();

		this.EXCHANGE_NAME = root_exchange_name;
		WikiPagesAndUsers = new HashMap<String, HashMap<String,Integer>>();
		WikiUsers = new HashMap<String, Integer>();
		total_item_counts = new HashMap<String, Integer>();
		total_item_counts.put("page_revision", 0);
		total_item_counts.put("talk_entry", 0);
		total_item_counts.put("abuse_filter", 0);
		total_item_counts.put("new_user_added", 0);

		System.out.println("Setting up OpenCalais Link");
		//calaisClient = new CalaisRestClient("rswystukezrpzc2fccqjfk39");

	}

	

	
	
	public void processChannelToQueue() {
		try {
			channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
			String queueName = channel.queueDeclare().getQueue();
			channel.queueBind(queueName, EXCHANGE_NAME, "");

			// get the channel stream, then add it to the graph.
			QueueingConsumer consumer = new QueueingConsumer(channel);
			channel.basicConsume(queueName, true, consumer);
			while (true) {
				QueueingConsumer.Delivery delivery = consumer.nextDelivery();
				String message = new String(delivery.getBody());

				// create the JSON object

				JSONObject obj = (JSONObject) new JSONParser().parse(message);
				
				System.out.println(obj.get("timestamp"));

				// now we have the object just need to add the node.

				RabbitMQWikiObj wikiObj = new RabbitMQWikiObj();
				wikiObj.setId("");
				wikiObj.setTimestamp(new Date());
				wikiObj.setWikipedia_entry_type("");
				wikiObj.setWikipedia_username("");
				wikiObj.setWikipedia_entry_type("");

				queue_all.add(wikiObj);
			}

		} catch (Exception e) {

		}

	}
	
	public void processChannelForGraph(WikipediaNodeGraph wikipedia_graph_panel) {
		try {
			channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
			String queueName = channel.queueDeclare().getQueue();
			channel.queueBind(queueName, EXCHANGE_NAME, "");

			// get the channel stream, then add it to the graph.
			QueueingConsumer consumer = new QueueingConsumer(channel);
			channel.basicConsume(queueName, true, consumer);
			while (true) {
				try{
					QueueingConsumer.Delivery delivery = consumer.nextDelivery();
					String message = new String(delivery.getBody());
					//System.out.println("Got Item: "+message);
				// create the JSON object
				JSONObject obj = (JSONObject) new JSONParser().parse(message);

				RabbitMQWikiObj wikiObj = new RabbitMQWikiObj();
				//wikiObj.setId("");
				wikiObj.setTimestamp(MiscFunctions.createUTCDate((String) obj.get("timestamp")));
				wikiObj.setWikipedia_page_name((String) obj.get("wikipedia_page_name"));
				wikiObj.setWikipedia_username((String) obj.get("wikipedia_username"));
				wikiObj.setWikipedia_entry_type((String) obj.get("wikipedia_entry_type"));
				//System.out.println("Got Object");
				wikipedia_graph_panel.addEdge(wikiObj);
			

				}catch(Exception e){
					e.printStackTrace();
				}
			}

		} catch (Exception e) {

		}

	}
	
	
	
	
	public void processChannelToPanels(WikipediaNodeGraph wikipedia_graph_panel, WikipediaActivityTimeSeries wikipedia_timeseries_panel, WikipediaActivityTimeSeries wikipedia_timeseries_panel_talk, WikipediaActivityTimeSeries wikipedia_timeseries_panel_revisions, WikipediaActivityTimeSeries wikipedia_timeseries_panel_abuse, WikipediaActivityTimeSeries wikipedia_timeseries_panel_newUsers, WikipediaMetricsText wikiMetrics) {
		try {
			channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
			String queueName = channel.queueDeclare().getQueue();
			channel.queueBind(queueName, EXCHANGE_NAME, "");

			// get the channel stream, then add it to the graph.
			QueueingConsumer consumer = new QueueingConsumer(channel);
			channel.basicConsume(queueName, true, consumer);
			while (true) {
				try{
					QueueingConsumer.Delivery delivery = consumer.nextDelivery();
					String message = new String(delivery.getBody());
					//System.out.println("Got Item: "+message);
				// create the JSON object

				JSONObject obj = (JSONObject) new JSONParser().parse(message);
				
//				System.out.println(obj.get("timestamp"));
				

				// now we have the object just need to add the node.

				RabbitMQWikiObj wikiObj = new RabbitMQWikiObj();
				//wikiObj.setId("");
				wikiObj.setTimestamp(MiscFunctions.createUTCDate((String) obj.get("timestamp")));
				wikiObj.setWikipedia_page_name((String) obj.get("wikipedia_page_name"));
				wikiObj.setWikipedia_username((String) obj.get("wikipedia_username"));
				wikiObj.setWikipedia_entry_type((String) obj.get("wikipedia_entry_type"));

				
				
				//queue.add(wikiObj);
				
				//System.out.println("Got Object");
				wikipedia_graph_panel.addVertext(wikiObj);
				
				wikipedia_timeseries_panel.addToDataSeries(wikiObj);
				wikipedia_timeseries_panel_talk.addToDataSeries(wikiObj);
				wikipedia_timeseries_panel_revisions.addToDataSeries(wikiObj);
				wikipedia_timeseries_panel_abuse.addToDataSeries(wikiObj);
				wikipedia_timeseries_panel_newUsers.addToDataSeries(wikiObj);
				
				
				if(WikiPagesAndUsers.containsKey(wikiObj.getWikipedia_page_name())){
					HashMap<String, Integer> pageUsers = WikiPagesAndUsers.get(wikiObj.getWikipedia_page_name());
					if(pageUsers.containsKey(wikiObj.getWikipedia_username())){
						pageUsers.put(wikiObj.getWikipedia_username(),pageUsers.get(wikiObj.getWikipedia_username())+1);
					}else{
						pageUsers.put(wikiObj.getWikipedia_username(),1);
					}
				}else{
					HashMap<String, Integer> pageUsers = new HashMap<String, Integer>();
					WikiPagesAndUsers.put(wikiObj.getWikipedia_page_name(), pageUsers);
					if(WikiUsers.containsKey(wikiObj.getWikipedia_username())){
						
					}else{
						WikiUsers.put(wikiObj.getWikipedia_username(), 1);
					}
				}
				
				
				wikiMetrics.addMetrics(WikiPagesAndUsers,WikiUsers);


				}catch(Exception e){
					e.printStackTrace();
				}
			}

		} catch (Exception e) {

		}

	}
	
	
	public void processChannelToPanels(final WikipediaMetricLineGraph lineGraph, final WikipediaMetricBarGraph barGraph, final WikipediaPageMetricScatterGraph scatterGraph){

		new Thread() {
            public void run() {
		
		try {

			channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
			String queueName = channel.queueDeclare().getQueue();
			channel.queueBind(queueName, EXCHANGE_NAME, "");

			// get the channel stream, then add it to the graph.
			QueueingConsumer consumer = new QueueingConsumer(channel);
			channel.basicConsume(queueName, true, consumer);
			boolean go = true;
			RabbitMQWikiObj wikiObj = new RabbitMQWikiObj();
			Date currentTime = new Date();
			while (go) {
				try{
					QueueingConsumer.Delivery delivery = consumer.nextDelivery();
					String message = new String(delivery.getBody());
				
					//System.out.println("Got Item: "+message);
				// create the JSON object

				JSONObject obj = (JSONObject) new JSONParser().parse(message);
				
//				System.out.println(obj.get("timestamp"));
				

				// now we have the object just need to add the node.

				
				//wikiObj.setId("");
				wikiObj.setTimestamp(MiscFunctions.createUTCDate((String) obj.get("timestamp")));
				wikiObj.setWikipedia_page_name((String) obj.get("wikipedia_page_name"));
				wikiObj.setWikipedia_username((String) obj.get("wikipedia_username"));
				wikiObj.setWikipedia_entry_type((String) obj.get("wikipedia_entry_type"));

				//checkForEntities(wikiObj);
				
				//add to the total item count map
				try{
					total_item_counts.put(wikiObj.getWikipedia_entry_type(), (total_item_counts.get(wikiObj.getWikipedia_entry_type())+1));
				}catch(Exception e){
					total_item_counts.put(wikiObj.getWikipedia_entry_type(), 1);
				}
				
								
				if(WikiPagesAndUsers.containsKey(wikiObj.getWikipedia_page_name())){
					HashMap<String, Integer> pageUsers = WikiPagesAndUsers.get(wikiObj.getWikipedia_page_name());
					if(pageUsers.containsKey(wikiObj.getWikipedia_username())){
						pageUsers.put(wikiObj.getWikipedia_username(),pageUsers.get(wikiObj.getWikipedia_username())+1);
					}else{
						pageUsers.put(wikiObj.getWikipedia_username(),1);
					}
				}else{
					HashMap<String, Integer> pageUsers = new HashMap<String, Integer>();
					WikiPagesAndUsers.put(wikiObj.getWikipedia_page_name(), pageUsers);
					if(WikiUsers.containsKey(wikiObj.getWikipedia_username())){
						
					}else{
						WikiUsers.put(wikiObj.getWikipedia_username(), 1);
					}
				}
				//currentTime = MiscFunctions.getTimestampToMinute(currentTime);
				//go = false;
				int diff = wikiObj.getTimestamp().getSeconds() - currentTime.getSeconds();
				//System.out.println(diff);
				if((diff < 3) && (diff>=0)){ 
					//queue_all.add(wikiObj);
					//System.out.println("Adding to Queue:"+diff);
					
					if(wikiObj.getWikipedia_entry_type().contains("revision")){
						queue_revisions.add(wikiObj);

					}else if(wikiObj.getWikipedia_entry_type().contains("talk")){
						queue_talk.add(wikiObj);

					}else if(wikiObj.getWikipedia_entry_type().contains("abuse")){
						queue_abuse.add(wikiObj);

					}else if(wikiObj.getWikipedia_entry_type().contains("user")){
						queue_newUsers.add(wikiObj);
					}
					
				}else{
					final ArrayList<ArrayList<RabbitMQWikiObj>> queues = new ArrayList<ArrayList<RabbitMQWikiObj>>();
					//queues.add(queue_all);
					queues.add(queue_revisions);
					queues.add(queue_talk);
					queues.add(queue_abuse);
					queues.add(queue_newUsers);
					Platform.runLater(new Runnable() {
		                 @Override public void run() {
						lineGraph.addObjectToSeriesAllActivity(queues);
						barGraph.addObjectToSeriesAllActivity(total_item_counts);
						scatterGraph.addObjectToSeriesAllActivity(WikiPagesAndUsers);
					
						for(ArrayList<RabbitMQWikiObj> queue_item: queues){
							queue_item.clear();
						} }});

						currentTime = new Date();
		                				}			
				
				}catch(Exception e){
					e.printStackTrace();
				}
				
//				if(wikiObj.getWikipedia_entry_type().contains("revision")){
//					lineGraph.addObjectToSeriesRevisionActivity(wikiObj);
//				}else if(wikiObj.getWikipedia_entry_type().contains("talk")){
//					lineGraph.addObjectToSeriesTalkActivity(wikiObj);
//				}
//				
				
			}
			
			//Thread.sleep(4000);
			//processChannelToPanels(lineGraph);

		} catch (Exception e) {

		}}}.start();

	}

	protected void checkForEntities(RabbitMQWikiObj wikiObj) {
		try{
			CalaisResponse response = calaisClient.analyze(wikiObj.getWikipedia_page_name());	
			 for (CalaisObject entity : response.getEntities()) {
			      System.out.println(entity.getField("_type") + ":" 
			                         + entity.getField("name"));
			    }
		}catch(Exception e){
			
		}
	}

	public ArrayList<RabbitMQWikiObj> getQueue() {
		return queue_all;
	}

	public void clearQueue() {
		queue_all.clear();
	}

	@Override
	protected Void call() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
