package com.aptitekk.binghamapp.News;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NewsArticle {

    String articleTitle;
    String articleLink;
    String articleGuid;
    String articleDescription;
    String articlePublishDate;

    public NewsArticle(String articleTitle, String articleLink, String articleGuid, String articleDescription, String articlePublishDate) {
        this.articleTitle = articleTitle;
        this.articleLink = articleLink;
        this.articleGuid = articleGuid;
        this.articleDescription = articleDescription;
        if (this.articleDescription != null)
            this.articleDescription = this.articleDescription.replaceAll("<br>", "\n");
        this.articlePublishDate = articlePublishDate;
        if (this.articlePublishDate != null) {
            try {
                Date date = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US).parse(this.articlePublishDate);
                this.articlePublishDate = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.US).format(date);
            } catch (ParseException ignored) {
            }
        }
    }

    public String getArticleTitle() {
        return this.articleTitle != null ? this.articleTitle : "(No Title)";
    }

    public String getArticleLink() {
        return articleLink != null ? this.articleLink : "#";
    }

    public String getArticleGuid() {
        return articleGuid;
    }

    public String getArticleDescription() {
        return articleDescription != null ? this.articleDescription : "";
    }

    public String getArticlePublishDate() {
        return articlePublishDate != null ? this.articlePublishDate : "Publish Date Unknown";
    }
}
