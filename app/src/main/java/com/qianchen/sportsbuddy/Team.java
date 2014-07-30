package com.qianchen.sportsbuddy;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

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

//    public void setEmblem() {
//
//    }

    public ParseFile getEmblem() {
        return getParseFile("emblem");
    }

    public void setDescription(String description) {
        put("description", description);
    }

    public String getDescription() {
        return getString("description");
    }

    public void setLeaderID(String leader) {
        put("leaderID", leader);
    }

    public String getLeaderID() {
        return getString("leaderID");
    }

    public void addMember(String member) {
        List<String> members = getList("members");
        if (members == null) {
            members = new ArrayList<String>();
            members.add(member);
        }
        put("members", members);
    }

    public List<String> getMembers() {
        return getList("members");
    }
}