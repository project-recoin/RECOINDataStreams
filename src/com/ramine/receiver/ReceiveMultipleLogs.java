package com.ramine.receiver;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

public class ReceiveMultipleLogs {

  private static String EXCHANGE_NAME = "spinn3r_hose";
  private static String EXCHANGE_NAME_TWO = "wikipedia_hose";
  private static String EXCHANGE_NAME_THREE = "trends_hose";



  public static void main(String[] argv) throws Exception {
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
    
    System.out.println(" [*] Waiting for messages. To exit press CTRL+C. ExchangeName: "+EXCHANGE_NAME);

    QueueingConsumer consumer = new QueueingConsumer(channel);
    channel.basicConsume(queueName, true, consumer);
    channel.basicConsume(queueName_two, true, consumer);
    channel.basicConsume(queueName_three, true, consumer);


    String msg_wiki = "";
    String msg_twitter = "";
    while (true) {
      QueueingConsumer.Delivery delivery = consumer.nextDelivery();
      String message = new String(delivery.getBody());
      //System.out.println(message);
      if(message.contains("wikipedia")){
    	  msg_wiki = message;
    	  counter_one++;
      }else if(message.contains("trends")){
    	  counter_three++;
      }
      else if(message.contains("twitter")){
         // System.out.println(message + "'");   

    	  msg_twitter = message;
    	  counter_two++;
    	  if((msg_twitter !=null) && (msg_wiki !=null)){
    		  String[] words = msg_twitter.split(" ");
    		  for(int i = 0; i< words.length; i++){
    			  if((words[i].length()>3) && (!words[i].contains("timestamp"))){
    			  if(msg_wiki.contains(words[i])){
    	    		  //System.out.println(words[i]);
    			  }
    			  }
    		  }
    	  }
    	  if(msg_twitter.contains(msg_wiki)){
    		  //System.out.println(message);
    	  }
      }
      System.out.printf("WikiItems: %d, TwitterItems: %d Trends: %d \n", counter_one, counter_two, counter_three );
      //System.out.println(" [x] Received '" + message + "'");   
    }
  }	
}
