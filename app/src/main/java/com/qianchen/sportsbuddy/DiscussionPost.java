package com.qianchen.sportsbuddy;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.util.Date;

/**
 * Created by Qian Chen on 7/31/2014.
 */
@ParseClassName("DiscussionPost")
public class DiscussionPost extends ParseObject {
    public void setAuthor(String author) {
        put("author", author);
    }

    public String getAuthor() {
        return getString("author");
    }

    public void setCreatedDate(Date date) {
        put("createdAt", date);
    }

    public Date getCreatedDate() {
        return getDate("createdAt");
    }

//    public void setAvatar(ParseFile emblem) {
//        put("emblem", emblem);
//    }
//
//    public ParseFile getAvatar() {
//        return getParseFile("emblem");
//    }

    public void setTitle(String title) {
        put("title", title);
    }

    public String getTitle() {
        return getString("title");
    }

    public void setContent(String content) {
        put("content", content);
    }

    public String getContent() {
        return getString("content");
    }
}