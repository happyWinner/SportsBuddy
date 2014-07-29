package com.qianchen.sportsbuddy;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * A team object.
 *
 * Created by Qian Chen on 7/28/2014.
 */
@ParseClassName("Team")
public class Team extends ParseObject {

    public void setTeamName(String teamName) {
        put("teamName", teamName);
    }

    public String getTeamName() {
        return getString("teamName");
    }

    public void setTeamType(String teamType) {
        put("teamType", teamType);
    }

    public String setTeamType() {
        return getString("teamType");
    }
}