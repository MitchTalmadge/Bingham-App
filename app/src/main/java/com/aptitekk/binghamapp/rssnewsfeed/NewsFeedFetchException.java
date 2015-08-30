package com.aptitekk.binghamapp.rssnewsfeed;

/**
 * Created by kevint on 8/30/2015.
 */
public class NewsFeedFetchException extends Exception {

    public NewsFeedFetchException() {
    }

    public NewsFeedFetchException(String message) {
        super(message);
    }

    public NewsFeedFetchException(Throwable cause) {
        super(cause);
    }

    public NewsFeedFetchException(String message, Throwable cause) {
        super(message, cause);
    }


}
