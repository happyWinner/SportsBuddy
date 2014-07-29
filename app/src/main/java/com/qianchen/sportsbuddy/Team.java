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

    public void setName(String teamName) {
        put("name", teamName);
    }

    public String getName() {
        return getString("name");
    }

    public void setSportsType(String teamType) {
        put("sportsType", teamType);
    }

    public String getSportsType() {
        return getString("sportsType");
    }
}