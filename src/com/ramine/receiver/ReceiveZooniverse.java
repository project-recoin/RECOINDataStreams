package com.ramine.receiver;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONArray;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

public class ReceiveZooniverse {

  private static String EXCHANGE_NAME = "zooniverse_classifications";
  private static String EXCHANGE_NAME_TWO = "zooniverse_talk";


  public static void main(String[] argv) throws Exception {

	 System.out.println("Usage: Argv[0] exchange_name");

	 try{
	if(argv[0].length()>1){
		EXCHANGE_NAME = argv[0];
		
	}}catch (Exception e) {
		// TODO: handle exception
	}
	 
    ConnectionFactory factory = new ConnectionFactory();
    //factory.setHost("localhost");
    factory.setHost("wsi-h1.soton.ac.uk");
    //factory.setPort(34156);
    //factory.setUsername("guest");
    //factory.setPassword("guest");
 
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
    String queueName = channel.queueDeclare().getQueue();
    channel.queueBind(queueName, EXCHANGE_NAME, "");	
    
    channel.exchangeDeclare(EXCHANGE_NAME_TWO, "fanout");
    String queueName_two = channel.queueDeclare().getQueue();
    channel.queueBind(queueName_two, EXCHANGE_NAME_TWO, "");
    
    System.out.println(" [*] Waiting for messages. To exit press CTRL+C. ExchangeName: "+EXCHANGE_NAME);

    QueueingConsumer consumer = new QueueingConsumer(channel);	
    channel.basicConsume(queueName, true, consumer);
    channel.basicConsume(queueName_two, true, consumer);

    HashMap<Integer, ArrayList<JSONObject>> classifications = new HashMap<Integer, ArrayList<JSONObject>>();
	JSONObject jsonClassEntry;
	int total = 0;
    while (true) {
    	total++;
      QueueingConsumer.Delivery delivery = consumer.nextDelivery();
      String message = new String(delivery.getBody());
      jsonClassEntry = (JSONObject) JSONSerializer.toJSON(message);
      int user_id;
      try{
    	  user_id = jsonClassEntry.getInt("user_id");
      }catch(Exception e){
    	  user_id = -1; //set to unknown
      }
      
      if(classifications.containsKey(user_id)){
    	  ArrayList<JSONObject> user_class = classifications.get(user_id);
    	  user_class.add(jsonClassEntry);	
      }else{
    	  ArrayList<JSONObject> user_class = new ArrayList<JSONObject>();
    	  user_class.add(jsonClassEntry);
    	  classifications.put(user_id, user_class);
      }
      
      System.out.printf("Total Classifications: %d, Users: %d \n", total, classifications.size()); 
      //System.out.println("Total Users:" + classifications.size()); 
           
      
      //System.out.println(" [x] Received '" + message + "'");   
      
    }
  }	
}
