package com.ramine.receiver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import com.ramine.emitter.WikipediaEmitLogThreaded.MyRunnable;
import com.streams.wikipedia.WikipediaIRCConnect;

public class MultipleTwitterFeedAggregator {

  private static String EXCHANGE_NAME = "twitter_uk";
  private static String EXCHANGE_NAME_TWO = "twitter_us";
  private static String EXCHANGE_NAME_THREE = "twitter_korea";
  private static String EXCHANGE_NAME_FOUR = "twitter_aus";
  private static String EXCHANGE_NAME_FIVE = "twitter_southAmerica";
  private static String EXCHANGE_NAME_SIX = "twitter_eu";



	private static final int MYTHREADS = 30;


  public static void main(String[] argv) throws Exception {
	  
	  
	  ExecutorService executor = Executors.newFixedThreadPool(MYTHREADS);
	  
	  ExecutorService executoTwo = Executors.newFixedThreadPool(MYTHREADS);

	  int counter_one = 0;
	  int counter_two = 0;
	  int counter_three = 0;

	 System.out.println("Usage: Argv[0] exchange_name");

	 try{
	if(argv[0].length()>1){
		EXCHANGE_NAME = argv[0];
		EXCHANGE_NAME_TWO = argv[1];
		
	}}catch (Exception e) {
		// TODO: handle exception
	}
	 
    ConnectionFactory factory = new ConnectionFactory();
    //factory.setHost("localhost");
    factory.setHost("wsi-h1.soton.ac.uk");
    //factory.setUsername("test");
    //factory.setPassword("admin");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    	
    channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
    String queueName = channel.queueDeclare().getQueue();
    channel.queueBind(queueName, EXCHANGE_NAME, "");
    
    channel.exchangeDeclare(EXCHANGE_NAME_TWO, "fanout");
    String queueName_two = channel.queueDeclare().getQueue();
    channel.queueBind(queueName_two, EXCHANGE_NAME_TWO, "");
    
    channel.exchangeDeclare(EXCHANGE_NAME_THREE, "fanout");
    String queueName_three = channel.queueDeclare().getQueue();
    channel.queueBind(queueName_three, EXCHANGE_NAME_THREE, "");
    
    channel.exchangeDeclare(EXCHANGE_NAME_FOUR, "fanout");
    String queueName_five = channel.queueDeclare().getQueue();
    channel.queueBind(queueName_five, EXCHANGE_NAME_FOUR, "");
    
    channel.exchangeDeclare(EXCHANGE_NAME_FIVE, "fanout");
    String queueName_six = channel.queueDeclare().getQueue();
    channel.queueBind(queueName_six, EXCHANGE_NAME_FIVE, "");
    
    channel.exchangeDeclare(EXCHANGE_NAME_SIX, "fanout");
    String queueName_seven = channel.queueDeclare().getQueue();
    channel.queueBind(queueName_seven, EXCHANGE_NAME_SIX, "");
    
    
    //System.out.println(" [*] Waiting for messages. To exit press CTRL+C. ExchangeName: "+EXCHANGE_NAME);
    ArrayList<QueueingConsumer> consumers = new ArrayList<QueueingConsumer>();
    
    QueueingConsumer consumer = new QueueingConsumer(channel);
    channel.basicConsume(queueName, true, consumer);
    consumers.add(consumer);
    	
    QueueingConsumer consumer_two = new QueueingConsumer(channel);
    channel.basicConsume(queueName_two, true, consumer_two);
    consumers.add(consumer_two);

    
    QueueingConsumer consumer_three = new QueueingConsumer(channel);
    channel.basicConsume(queueName_three, true, consumer_three);
    consumers.add(consumer_three);
    
    QueueingConsumer consumer_five = new QueueingConsumer(channel);
    channel.basicConsume(queueName_five, true, consumer_five);
    consumers.add(consumer_five);
    
    QueueingConsumer consumer_six = new QueueingConsumer(channel);
    channel.basicConsume(queueName_six, true, consumer_six);
    consumers.add(consumer_six);
    
    QueueingConsumer consumer_seven = new QueueingConsumer(channel);
    channel.basicConsume(queueName_seven, true, consumer_seven);
    consumers.add(consumer_seven);

    Connection connectionTwo = factory.newConnection();
    Channel channelTwo = connectionTwo.createChannel();
    channelTwo.exchangeDeclare("logs", "fanout");
    String channelTwo_queue = channelTwo.queueDeclare().getQueue();
    channelTwo.queueBind(channelTwo_queue, "logs", "");
    
    QueueingConsumer consumer_four = new QueueingConsumer(channelTwo);
    channelTwo.basicConsume(channelTwo_queue, true, consumer_four);
    //consumers.add(consumer_four);

    int match = 0;
    String msg_wiki = "";
    String msg_twitter = "";
    HashMap<Long, Boolean> cache = new HashMap<Long, Boolean>();
    ArrayList<JSONObject> sample = new ArrayList<JSONObject>();
    ArrayList<JSONObject> aggregateHarvesters = new ArrayList<JSONObject>();
    
    
    Runnable worker = new MyRunnableTwo(consumer_four, sample, cache);
	executor.execute(worker);
    
    for(QueueingConsumer con : consumers){
    	Runnable workerTwo = new MyRunnable(con, aggregateHarvesters, cache);
    	executoTwo.execute(workerTwo);
    }
    
    
    	
    
    
//    while (true) {
//      QueueingConsumer.Delivery delivery = consumer.nextDelivery();
//      String message = new String(delivery.getBody());
//      //System.out.println(message);
//      JSONObject msg = (JSONObject) JSONSerializer.toJSON(message);
//      aggregateHarvesters.add(msg);
//      System.out.println(msg);
//      
//      QueueingConsumer.Delivery deliveryTwo = consumerTwo.nextDelivery();
//      String messageTwo = new String(deliveryTwo.getBody());
//      JSONObject msgTwo = (JSONObject) JSONSerializer.toJSON(messageTwo);
//      sample.add(msgTwo);
//      
//      System.out.println("1% Sample: "+sample.size() + " Aggregate: "+aggregateHarvesters.size());
//
//      //System.out.println( msg.get("id"));
//      if(cache.containsKey((Long) msg.get("id"))){
//    	  match++;
//    	  System.out.println("match: "+match+"/"+cache.size());
//    	  //System.out.println("At CACHE SIZE: "+ cache.size());
//      }else{
//    	  cache.put((Long) msgTwo.get("id"), true);
//    	  //System.out.println(cache.size());
//      }
      //
//      if(message.contains("wikipedia")){
//    	  msg_wiki = message;
//    	  counter_one++;
//      }else if(message.contains("trends")){
//    	  counter_three++;
//      }
//      else if(message.contains("twitter")){
//         // System.out.println(message + "'");   
//
//    	  msg_twitter = message;
//    	  counter_two++;
//    	  if((msg_twitter !=null) && (msg_wiki !=null)){
//    		  String[] words = msg_twitter.split(" ");
//    		  for(int i = 0; i< words.length; i++){
//    			  if((words[i].length()>3) && (!words[i].contains("timestamp"))){
//    			  if(msg_wiki.contains(words[i])){
//    	    		  //System.out.println(words[i]);
//    			  }
//    			  }
//    		  }
//    	  }
//    	  if(msg_twitter.contains(msg_wiki)){
//    		  //System.out.println(message);
//    	  }
//      }
//      System.out.printf("WikiItems: %d, TwitterItems: %d Trends: %d \n", counter_one, counter_two, counter_three );
//      //System.out.println(" [x] Received '" + message + "'");   
    }
 	
  

  public static class MyRunnable implements Runnable {
		private final QueueingConsumer consumer;
		private final ArrayList<JSONObject> msgs;
		private final HashMap<Long, Boolean> cache;

		
		MyRunnable(QueueingConsumer con, ArrayList<JSONObject> msgs,HashMap<Long, Boolean> cache) {
			this.consumer = con;
			this.msgs = msgs;
			this.cache = cache;
		}

		@Override
		public void run() {
			
			while(true){
				try{
			      QueueingConsumer.Delivery deliveryTwo = consumer.nextDelivery();
			      String messageTwo = new String(deliveryTwo.getBody());
			      JSONObject msgTwo = (JSONObject) JSONSerializer.toJSON(messageTwo);
			      msgs.add(msgTwo);
			      //System.out.println("aggregate size: "+msgs.size());
			      if(cache.containsKey((Long) msgTwo.get("id"))){
			    	  System.out.println("match: "+cache.size());
			      }else{
			    	  System.out.println("Aggregate Streams:"+msgs.size()+" | Cache: "+cache.size());
			      }
				}catch(Exception e){
					
				}
			}
			


		}
	}
  
  public static class MyRunnableTwo implements Runnable {
		private final QueueingConsumer consumer;
		private final ArrayList<JSONObject> msgs;
		private final HashMap<Long, Boolean> cache;
		
		MyRunnableTwo(QueueingConsumer con, ArrayList<JSONObject> msgs, HashMap<Long, Boolean> cache) {
			this.consumer = con;
			this.msgs = msgs;
			this.cache = cache;
		}

		@Override
		public void run() {
			
			while(true){
				try{
			      QueueingConsumer.Delivery deliveryTwo = consumer.nextDelivery();
			      String messageTwo = new String(deliveryTwo.getBody());
			      JSONObject msgTwo = (JSONObject) JSONSerializer.toJSON(messageTwo);
			      msgs.add(msgTwo);
			      
			      cache.put((Long) msgTwo.get("id"), true);			      //System.out.println("1% size: "+msgs.size());
				}catch(Exception e){
					
				}
			}
			


		}
	}
  
  
//  public static class MyRunnableThree implements Runnable {
//		private final ArrayList<JSONObject> aggregate;
//		private final ArrayList<JSONObject> sample;
//
//		MyRunnableThree(ArrayList<JSONObject> sample, ArrayList<JSONObject> aggregate) {
//			this.sample = sample;
//			this.aggregate = aggregate;
//		}
//
//		@Override
//		public void run() {
//			
//			while(true){
//				try{
//			      QueueingConsumer.Delivery deliveryTwo = consumer.nextDelivery();
//			      String messageTwo = new String(deliveryTwo.getBody());
//			      JSONObject msgTwo = (JSONObject) JSONSerializer.toJSON(messageTwo);
//			      msgs.add(msgTwo);
//			      System.out.println("1% size: "+msgs.size());
//				}catch(Exception e){
//					
//				}
//			}
//			
//
//
//		}
//	}


  
}
