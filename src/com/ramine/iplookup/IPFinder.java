package com.ramine.iplookup;

import java.io.File;
import java.net.InetAddress;



import twitter4j.internal.org.json.JSONObject;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.model.CountryResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Country;
import com.maxmind.geoip2.record.Location;

public class IPFinder {

	
	DatabaseReader countryReader;

	DatabaseReader cityReader;

	
	public IPFinder() {
		// TODO Auto-generated constructor stub
		
		if(setUpDatabase()){
			System.out.println("IP Database Setup");
		}
	
	}
	
	
	public boolean setUpDatabase(){
		try{
		File database = new File("db/GeoLite2-Country.mmdb");
		countryReader = new DatabaseReader.Builder(database).build();
		
		File databaseCity = new File("db/GeoLite2-City.mmdb");
		cityReader = new DatabaseReader.Builder(databaseCity).build();		
		
		return true;
		
		}catch(Exception e){
			return false;
		}
		
		
	}
	
	public String findIPtoCountryName(String ipAdd){
		String countryName= "";

		try{
			InetAddress ipAddress = InetAddress.getByName(ipAdd);
			CountryResponse response = countryReader.country(ipAddress);
			Country country = response.getCountry();
			//System.out.println("Country Name:" + country.getName());  
			countryName = country.getName();
		}catch(Exception e){
			
		}
		return countryName;	
	}
	
	public String findIPtoCountryCode(String ipAdd){
		String countryCode = "";

		try{
			InetAddress ipAddress = InetAddress.getByName(ipAdd);
			CountryResponse response = countryReader.country(ipAddress);
			Country country = response.getCountry();
			//System.out.println("Country Code:" + country.getIsoCode());
			countryCode = country.getName();
		}catch(Exception e){
			
		}
		return countryCode;	
	}
	
	

	public String findIPtoCityName(String ipAdd){
		String cityName= "";
		
		try{
			InetAddress ipAddress = InetAddress.getByName(ipAdd);
			CityResponse response = cityReader.city(ipAddress);
			City city = response.getCity();
			//System.out.println("City Name:" + city.getName());
			cityName = city.getName();
		}catch(Exception e){
			
		}
		
		
		
		return cityName;	
	}
	

	public JSONObject findIPtoLatLongObject(String ipAdd){
		JSONObject latLng = new JSONObject();
		
		try{
			InetAddress ipAddress = InetAddress.getByName(ipAdd);
			CityResponse response = cityReader.city(ipAddress);
			
			
			//for lat long
			Location location = response.getLocation();

			latLng.put("lat", location.getLatitude());
			latLng.put("lng", location.getLongitude());

			//System.out.println("Lat Long:" + latLng.toString());
		}catch(Exception e){
			
		}
		
		return latLng;	
	}
	
	
}
