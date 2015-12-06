package com.aptitekk.binghamapp.News;

public class NewsArticle {
	
	String title;
	String link;
	String guid;
	String description;
	String pubDate;
	
	public NewsArticle(String title, String link, String guid, String description, String pubDate) {
		if(title == null)
			title = "N/A";
		if(link == null)
			link = "N/A";
		if(guid == null)
			guid = "N/A";
		if(description == null)
			description = "N/A";
		if(pubDate == null)
			pubDate = "N/A";
		this.title = title;
		this.link = link;
		this.guid = guid;
		this.description = description;
		this.pubDate = pubDate;
		
	}

	public String getTitle() {
		return this.title;
	}
	public String getLink() {
		return link;
	}
	public String getGUID() {
        return guid;
    }
    public String getDescription() {
        return description;
    }
    public String getPubDate() {
        return pubDate;
    }
}
