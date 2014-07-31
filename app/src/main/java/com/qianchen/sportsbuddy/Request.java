package com.qianchen.sportsbuddy;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * A request object.
 *
 * Created by Qian Chen on 7/31/2014.
 */
@ParseClassName("Request")
public class Request extends ParseObject {

    public void setUserID(String userID) {
        put("userID", userID);
    }

    public String getUserID() {
        return getString("userID");
    }

    public void setUserName(String userName) {
        put("userName", userName);
    }

    public String getUserName() {
        return getString("userName");
    }

    public void setTeamID(String teamID) {
        put("teamID", teamID);
    }

    public String getTeamID() {
        return getString("teamID");
    }
}
