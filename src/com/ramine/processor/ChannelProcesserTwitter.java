package com.ramine.processor;

import java.util.ArrayList;
import java.util.Date;















import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import com.ramine.functions.MiscFunctions;
import com.ramine.rabbmitMQ.objects.RabbitMQTwitterObj;
import com.ramine.rabbmitMQ.objects.RabbitMQWikiObj;
import com.ramine.viewer.graph.WikipediaNodeGraph;
import com.ramine.viewer.timeseries.TwitterMetricLineGraph;
import com.ramine.viewer.timeseries.WikipediaActivityTimeSeries;

public class ChannelProcesserTwitter {

	private Channel channel;
	private String EXCHANGE_NAME = "";

	private ArrayList<Object> queue;
	
	private ArrayList<RabbitMQTwitterObj> queue_all;
	private ArrayList<RabbitMQTwitterObj> queue_wiki;


	public ChannelProcesserTwitter(Channel channel, String exchange_name) {
		// TODO Auto-generated constructor stub

		this.channel = channel;
		queue = new ArrayList<Object>();
		this.EXCHANGE_NAME = exchange_name;
		queue_all = new ArrayList<RabbitMQTwitterObj>();
		queue_wiki = new ArrayList<RabbitMQTwitterObj>();
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

				queue.add(wikiObj);
			}

		} catch (Exception e) {

		}

	}
	
	
	public void processChannelToPanels(final TwitterMetricLineGraph twitterGraph) {
		new Thread() {
			public void run() {

				try {
					channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
					String queueName = channel.queueDeclare().getQueue();
					channel.queueBind(queueName, EXCHANGE_NAME, "");

					// get the channel stream, then add it to the graph.
					QueueingConsumer consumer = new QueueingConsumer(channel);
					channel.basicConsume(queueName, true, consumer);
					Date currentTime = new Date();

					while (true) {
						try {
							QueueingConsumer.Delivery delivery = consumer
									.nextDelivery();
							String message = new String(delivery.getBody());
							// System.out.println("Got Item: "+message);
							// create the JSON object

							JSONObject obj = (JSONObject) new JSONParser()
									.parse(message);

							// System.out.println(obj.get("timestamp"));

							// now we have the object just need to add the node.

							// Ra twitterObj = new RabbitMQWikiObj();
							RabbitMQTwitterObj twitterObj = new RabbitMQTwitterObj();
							// wikiObj.setId("");
							twitterObj.setTimestamp(MiscFunctions
									.createUTCDate((String) obj
											.get("timestamp")));
							twitterObj
									.setTwitter_text((String) obj.get("text"));

							// System.out.println(obj.get("text"));

							int diff = twitterObj.getTimestamp().getSeconds()
									- currentTime.getSeconds();
							// System.out.println(diff);
							if ((diff < 3) && (diff >= 0)) {
								queue_all.add(twitterObj);
								System.out.println(obj.get("text"));
								if (twitterObj.getTwitter_text().contains("wikipedia")) {
									queue_wiki.add(twitterObj);
								}
							} else {
								final ArrayList<ArrayList<RabbitMQTwitterObj>> queues = new ArrayList<ArrayList<RabbitMQTwitterObj>>();
								queues.add(queue_all);
								queues.add(queue_wiki);
								Platform.runLater(new Runnable() {
									@Override
									public void run() {
										twitterGraph.addObjectToSeriesAllActivity(queues);

										for (ArrayList<RabbitMQTwitterObj> queue_item : queues) {
											queue_item.clear();
										}
									}
								});
								currentTime = new Date();

							}

							// queue.add(wikiObj);

							// System.out.println("Got Object");
							// wikipedia_graph_panel.addVertext(twitterObj);
							// wikipedia_timeseries_panel.addToDataSeries(twitterObj);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				} catch (Exception e) {

				}
			}
		}.start();

	}

	public ArrayList<Object> getQueue() {
		return queue;
	}

	public void clearQueue() {
		queue.clear();
	}

}
