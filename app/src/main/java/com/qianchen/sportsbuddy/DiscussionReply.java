package com.qianchen.sportsbuddy;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by Qian Chen on 8/1/2014.
 */
@ParseClassName("DiscussionReply")
public class DiscussionReply extends ParseObject {

    public void setPostID(String postID) {
        put("postID", postID);
    }

    public String getPostID() {
        return getString("postID");
    }

    public void setUserName(String userName) {
        put("userName", userName);
    }

    public String getUserName() {
        return getString("userName");
    }

    public void setReplyMessage(String replyMessage) {
        put("replyMessage", replyMessage);
    }

    public String getReplyMessage() {
        return getString("replyMessage");
    }
}
