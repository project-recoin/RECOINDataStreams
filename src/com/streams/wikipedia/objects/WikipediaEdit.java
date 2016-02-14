package com.streams.wikipedia.objects;

import java.util.Date;

public class WikipediaEdit {
	

	Date editData;
	String revisionText;
	String wikipedia_page;
	String id;
	
	public WikipediaEdit() {
		// TODO Auto-generated constructor stub
	}
	
	public void setRevisionText(String revisionText) {
		this.revisionText = revisionText;
	}public void setEditData(Date editData) {
		this.editData = editData;
	}public String getRevisionText() {
		return revisionText;
	}public Date getEditData() {
		return editData;
	}
	public void setWikipedia_page(String wikipedia_page) {
		this.wikipedia_page = wikipedia_page;
	}public String getWikipedia_page() {
		return wikipedia_page;
	}public void setId(String id) {
		this.id = id;
	}public String getId() {
		return id;
	}
	

}
