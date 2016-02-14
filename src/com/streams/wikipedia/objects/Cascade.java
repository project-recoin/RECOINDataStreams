package com.streams.wikipedia.objects;

import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;

public class Cascade {

	

	
	TreeMap<Date, WikipediaEdit> children;
	Date rootDate;
	Date stubDate;
	int pathcount;
	int deltaBetweenEdits_hours;
	int totalDuration_hours;
	String matching_entity;
	
	
	public Cascade() {

	children = new TreeMap<>();
	}
	
	public int getPathcount() {
		return pathcount;
	}public Date getRootDate() {
		return rootDate;
	}public Date getStubDate() {
		return stubDate;
	}public void setPathcount(int pathcount) {
		this.pathcount = pathcount;
	}public void setStubDate(Date stubDate) {
		this.stubDate = stubDate;
	}public void setRootDate(Date rootDate) {
		this.rootDate = rootDate;
	}
	public void setChildren(TreeMap<Date, WikipediaEdit> children) {
		this.children = children;
	}public TreeMap<Date, WikipediaEdit> getChildren() {
		return children;
	}
	
	public void setTotalDuration_hours(int totalDuration_hours) {
		this.totalDuration_hours = totalDuration_hours;
	}public void setMatching_entity(String matching_entity) {
		this.matching_entity = matching_entity;
	}public void setDeltaBetweenEdits_hours(int deltaBetweenEdits_hours) {
		this.deltaBetweenEdits_hours = deltaBetweenEdits_hours;
	}public int getTotalDuration_hours() {
		return totalDuration_hours;
	}public String getMatching_entity() {
		return matching_entity;
	}public int getDeltaBetweenEdits_hours() {
		return deltaBetweenEdits_hours;
	}
	
	
}
