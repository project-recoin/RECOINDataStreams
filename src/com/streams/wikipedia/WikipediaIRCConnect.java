package com.streams.wikipedia;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.util.ArrayList;

import EDU.oswego.cs.dl.util.concurrent.misc.Fraction;

import com.rabbitmq.client.Channel;
import com.streams.wikipedia.functions.WikiLogStringConverter;

public class WikipediaIRCConnect {
	
	Socket socket;
	String server = "irc.wikimedia.org";
	String nick = "sotonDivWOListenerNew";
	String login = "sotonDivWOListenerNew";
	String irc_channel = "#en.wikipedia";
	BufferedWriter writer;
	BufferedReader reader;
	WikiLogStringConverter converter;
	String line = null;
	Channel channel;
	String EXCHANGE_NAME;
	int channelListID;
	String channelListIDstr="";

	ArrayList<String> channels;

	
	public WikipediaIRCConnect(Channel rabbitMQChannel, String EXCHANGE_NAME){
		channels = new ArrayList<String>();
		this.channel = rabbitMQChannel;
		this.EXCHANGE_NAME = EXCHANGE_NAME;  	
	}
	
	public WikipediaIRCConnect(Channel rabbitMQChannel, String EXCHANGE_NAME, int channelListID){
		channels = new ArrayList<String>();
		this.channel = rabbitMQChannel;
		this.EXCHANGE_NAME = EXCHANGE_NAME;
		this.channelListID = channelListID;
		this.channelListIDstr = String.valueOf(channelListID);
		login = login+"_"+channelListIDstr;
		nick = nick+"_"+channelListIDstr;
	}


	public WikipediaIRCConnect(Channel rabbitMQChannel, String channel, String EXCHANGE_NAME){
		channels = new ArrayList<String>();
		this.channel = rabbitMQChannel;
		this.EXCHANGE_NAME = EXCHANGE_NAME;  
		this.irc_channel = channel;
	}

	
	/*
	 * Add to the channel list to process
	 */
	public void addChannelToList(String channel){
		channels.add(channel);
	}
	
	public WikipediaIRCConnect() throws Exception {
		// connect to the server.
		// Connect directly to the IRC server.
		socket = new Socket(server, 6667);
		writer = new BufferedWriter(new OutputStreamWriter(
				socket.getOutputStream()));
		reader = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));

		// Log on to the server.
		writer.write("NICK " + nick + "\r\n");
		writer.write("USER " + login + " 8 * : SOCIAM Wikipedia IRC Activity Bot\r\n");
		writer.flush();
		// Read lines from the server until it tells us we have connected.
		
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (line.indexOf("004") >= 0) {
					System.out.println("Logged in as "+nick);
					break;
				} else if (line.indexOf("433") >= 0) {
					System.out.printf("Nickname %s is already in use.\n",nick);
					return;
				}
			}

			// Join the channel.
			writer.write("JOIN " + irc_channel + "\r\n");
			writer.flush();
			// Keep reading lines from the server.
			converter = new WikiLogStringConverter();
	}

	public void getWikipediaEditsAndPublishToRabbitMQ() {
		String toSend = null;
		try{
			while ((line = reader.readLine()) != null) {
				if (line.toLowerCase().startsWith("PING ")) {
					// We must respond to PINGs to avoid being disconnected.
					writer.write("PONG " + line.substring(5) + "\r\n");
					writer.write("PRIVMSG " + channel + " :I got pinged!\r\n");
					writer.flush();
				} else {
					// Print the raw line received by the bot.
					// String s2 = new String(line.getBytes(), "UTF-8");
					try{
						if((toSend = converter.convertLogStringToJSONObject(line)).length()>30){
								channel.basicPublish(EXCHANGE_NAME, "", null, toSend.getBytes());
						}
					}catch (Exception e) {
						//e.printStackTrace();
						// TODO: handle exception
					}
					//System.out.println(converter.convertLogStringToJSONObject(line));
				}
			}
		} catch (SocketException e) {
			System.out.println("socket Exception");
			//getWikipediaEditsAndPublishToRabbitMQ();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("WHILE LOOP STOPPED! Trying Again");
		connectToChannelAndStartBroadcasting();
	}
	
	
	public void connectToChannelAndStartBroadcasting() {

		System.out.println("Connecting To IRC Channel...");
		if(connectToChannel()){
			System.out.println("Broadcasting to RabbitMQ Channel");
			getWikipediaEditsAndPublishToRabbitMQ();
		}else{
			//there seems to be a problem with the connection, we shall wait for a while and try again.
			try{
			Thread.sleep(2000);
			}catch(Exception e){
				
			}
			//then go and try this again!
			connectToChannelAndStartBroadcasting();
			
		}
		
	}
	
	/*
	 * Connect to all channels based ont the list provided in wikipeida_channel_list
	 */
	public boolean connectToChannel(){
		try{
			// connect to the server.
			// Connect directly to the IRC server.
			socket = new Socket(server, 6667);
			writer = new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream()));
			reader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));

			// Log on to the server.
			writer.write("NICK " + nick + "\r\n");
			writer.write("USER " + login + " 8 * : Java IRC Hacks Bot\r\n");
			writer.flush();
			// Read lines from the server until it tells us we have connected.
			
				String line = null;
				while ((line = reader.readLine()) != null) {
					if (line.indexOf("004") >= 0) {
						System.out.println("Logged in as "+nick);
						break;
					} else if (line.indexOf("433") >= 0) {
						System.out.println("Nickname is already in use.");
						return false;
					}
				}

				// Join the channel.
				//need to join the channel list
				for(String channel_name : channels){
					System.out.printf("Joining Channel %s \n", channel_name);
					try{
					writer.write("JOIN " + channel_name +"\r\n");
					Thread.sleep(100);
					}catch(Exception e){
						
					}
				}
				writer.flush();
				// Keep reading lines from the server.
				converter = new WikiLogStringConverter();
			return true;
		}catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	public boolean populateChannelList(){
		
		try{
			FileReader reader = new FileReader("wikipedia_channel_list_"+channelListIDstr);
			BufferedReader br = new BufferedReader(reader);
			String currentLine;
			while ((currentLine=br.readLine()) !=null){
				channels.add(currentLine.replace("\n", "").trim());
			}
			System.out.println("populated wikipedia channel list: "+channelListID+"with size: "+channels.size());
			br.close();
			reader.close();
			return true;
		}catch(Exception e){
			return false;
		}
		
		
	}

	public static void main(String[] args) throws Exception {

		WikiLogStringConverter converter = new WikiLogStringConverter();
		// The server to connect to and our details.
		String server = "irc.wikimedia.org";
		String nick = "ramine_edit_listener";
		String login = "ramine_edit_listener";

		// The channel which the bot will join.
		String channel = "#en.wikipedia";

		// Connect directly to the IRC server.
		Socket socket = new Socket(server, 6667);
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				socket.getOutputStream()));
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));

		// Log on to the server.
		writer.write("NICK " + nick + "\r\n");
		writer.write("USER " + login + " 8 * : Java IRC Hacks Bot\r\n");
		writer.flush();

		// Read lines from the server until it tells us we have connected.
		String line = null;
		while ((line = reader.readLine()) != null) {
			if (line.indexOf("004") >= 0) {
				// We are now logged in.
				break;
			} else if (line.indexOf("433") >= 0) {
				System.out.println("Nickname is already in use.");
				return;
			}
		}

		// Join the channel.
		writer.write("JOIN " + channel + "\r\n");
		writer.flush();
		// Keep reading lines from the server.
		while ((line = reader.readLine()) != null) {
			if (line.toLowerCase().startsWith("PING ")) {
				// We must respond to PINGs to avoid being disconnected.
				writer.write("PONG " + line.substring(5) + "\r\n");
				writer.write("PRIVMSG " + channel + " :I got pinged!\r\n");
				writer.flush();
			} else {
				// Print the raw line received by the bot.
				// String s2 = new String(line.getBytes(), "UTF-8");
				
				System.out
						.println(converter.convertLogStringToJSONObject(line));
			}
		}
	}

}