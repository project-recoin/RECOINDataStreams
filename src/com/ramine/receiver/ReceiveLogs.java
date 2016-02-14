package com.ramine.receiver;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

public class ReceiveLogs {

  private static String EXCHANGE_NAME = "wikipedia_hose";


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
    
    System.out.println(" [*] Waiting for messages. To exit press CTRL+C. ExchangeName: "+EXCHANGE_NAME);

    QueueingConsumer consumer = new QueueingConsumer(channel);	
    channel.basicConsume(queueName, true, consumer);
    while (true) {
      QueueingConsumer.Delivery delivery = consumer.nextDelivery();
      String message = new String(delivery.getBody());
      JSONObject jsonClassEntry = (JSONObject) JSONSerializer.toJSON(message);
      //jsonClassEntry.toString(4);
      System.out.println(jsonClassEntry.toString(4));   
    }
  }	
}


