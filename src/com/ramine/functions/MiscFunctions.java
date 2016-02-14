package com.ramine.functions;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.time.DateUtils;
import org.jfree.date.DateUtilities;



public class MiscFunctions {
	
	
	public static Date createUTCDate(String dateStr) {
		Date date;
		try {
			try {
				DateFormat dateFormat = new SimpleDateFormat(
						"yyyy-MM-dd hh:mm:ss");
				date = dateFormat.parse(dateStr);
			} catch (Exception e1) {
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
				date = dateFormat.parse(dateStr);
			}
			return date;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			return null;
		}

	}

	
	public static String createYYYYMMDDHHString(String dateStr) {
		Date date;
		try {
		
				DateFormat dateFormat = new SimpleDateFormat(
						"yyyy-MM-dd hh:mm:ss");
				date = dateFormat.parse(dateStr);
				
				DateFormat dateFormatNew = new SimpleDateFormat("yyyyMMdd");
				String date_to_string = dateFormatNew.format(date);
				//append the hour
				date_to_string = date_to_string+"00";
				return date_to_string;

			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			return null;
		}

	}
	
	
	public static String decreaseYYYYMMDDHHStringDateByDays(String dateStr, int days) {
		Date date;
		try {
		
				DateFormat dateFormat = new SimpleDateFormat(
						"yyyyMMddhh");
				date = dateFormat.parse(dateStr);
				Date decrement = DateUtils.addDays(date, -days);
				
				DateFormat dateFormatNew = new SimpleDateFormat("yyyyMMdd");
				String date_to_string = dateFormatNew.format(decrement);
				//append the hour
				date_to_string = date_to_string+"00";
				return date_to_string;

			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			return null;
		}

	}
	
	public static String convertDateTimeToString(Date date){
		SimpleDateFormat dateformatyyyyMMdd = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String date_to_string = dateformatyyyyMMdd.format(date);
		return date_to_string;
		
	}
	
	
	public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
	    long diffInMillies = date2.getTime() - date1.getTime();
	    return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
	}
	

	
	
	public static String wordsToReplace(String word){
		return word.replaceAll("[^A-Za-z0-9]", "");
	}
	
	public static Date getTimestampToMinute(Date timestamp) {
		// TODO Auto-generated method stub
		String tmp = MiscFunctions.convertDateTimeToString(timestamp);
		//System.out.println(MiscFunctions.createUTCDate(tmp));
		return MiscFunctions.createUTCDate(tmp);
	}
	
	
	
}




