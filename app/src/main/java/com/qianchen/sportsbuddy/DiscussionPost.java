package com.qianchen.sportsbuddy;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

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

    public void addReply(String reply) {
        List<String> replies = getList("replies");
        if (replies == null) {
            replies = new ArrayList<String>();
        }
        replies.add(reply);
        put("replies", replies);
    }

    public List<String> getReplies() {
        return getList("replies");
    }
}