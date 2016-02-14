package com.streams.wikipedia;

import java.io.BufferedReader;
import java.io.BufferedWriter;

import com.streams.wikipedia.functions.WikiLogStringConverter;

public class WikipediaChannel {
	
	String ChannelName;
	
	BufferedWriter channelWriter;
	
	BufferedReader channelReader;
	
	WikiLogStringConverter converter;

	
	public WikipediaChannel() {


	}
	
	public void setChannelWriter(BufferedWriter channelWriter) {
		this.channelWriter = channelWriter;
	}public void setChannelReader(BufferedReader channelReader) {
		this.channelReader = channelReader;
	}public void setChannelName(String channelName) {
		ChannelName = channelName;
	}public BufferedWriter getChannelWriter() {
		return channelWriter;
	}public BufferedReader getChannelReader() {
		return channelReader;
	}public String getChannelName() {
		return ChannelName;
	}
	
	public void setConverter(WikiLogStringConverter converter) {
		this.converter = converter;
	}public WikiLogStringConverter getConverter() {
		return converter;
	}

}
