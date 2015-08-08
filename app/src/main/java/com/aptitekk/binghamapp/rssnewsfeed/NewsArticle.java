package com.aptitekk.binghamapp.RSSNewsFeed;

public class NewsArticle {
	
	String title;
	String link;
	String guid;
	String description;
	String pubDate;
	
	public NewsArticle(String title, String link, String guid, String description, String pubDate) {
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
