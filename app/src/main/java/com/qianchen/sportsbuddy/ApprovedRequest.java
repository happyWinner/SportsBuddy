package com.qianchen.sportsbuddy;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * An approved request object
 *
 * Created by Qian Chen on 7/31/2014.
 */
@ParseClassName("ApprovedRequest")
public class ApprovedRequest extends ParseObject {

    public void setUserID(String userID) {
        put("userID", userID);
    }

    public String getUserID() {
        return getString("userID");
    }

    public void setTeamID(String teamID) {
        put("teamID", teamID);
    }

    public String getTeamID() {
        return getString("teamID");
    }
}