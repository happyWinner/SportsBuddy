package com.qianchen.sportsbuddy;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

/**
 * An event object.
 *
 * Created by Qian Chen on 7/27/2014.
 */
@ParseClassName("Event")
public class Event extends ParseObject {

    public void setSportType(String sportType) {
        put("sportType", sportType);
    }

    public String getSportType() {
        return getString("sportType");
    }

    public void setDateMilliseconds(long dateMilliseconds) {
        put("dateMilliseconds", dateMilliseconds);
    }

    public long getDateMilliseconds() {
        return getLong("dateMilliseconds");
    }

    public void setHour(int hour) {
        put("hour", hour);
    }

    public int getHour() {
        return getInt("hour");
    }

    public void setMinute(int minute) {
        put("minute", minute);
    }

    public int getMinute() {
        return getInt("minute");
    }

    public void setLatitude(double latitude) {
        put("latitude", latitude);
    }

    public double getLatitude() {
        return getDouble("latitude");
    }

    public void setLongitude(double longitude) {
        put("longitude", longitude);
    }

    public double getLongitude() {
        return getDouble("longitude");
    }

    public void setAddressText(String addressText) {
        put("addressText", addressText);
    }

    public String getAddressText() {
        return getString("addressText");
    }

    public void setMaxPeople(int maxPeople) {
        put("maxPeople", maxPeople);
    }

    public int getMaxPeople() {
        return getInt("maxPeople");
    }

    public void setCurrentPeople(int currentPeople) {
        put("currentPeople", currentPeople);
    }

    public int getCurrentPeople() {
        return getInt("currentPeople");
    }

    public void setVisibility(String visibility) {
        put("visibility", visibility);
    }

    public String getVisibility() {
        return getString("visibility");
    }

    public void setNotes(String notes) {
        put("notes", notes);
    }

    public String getNotes() {
        return getString("notes");
    }

    public void addParticipant(String participant) {
        List<String> participants = getList("participants");
        if (participants == null) {
            participants = new ArrayList<String>();
            participants.add(participant);
        }
        put("participants", participants);
    }

    public List<String> getParticipants() {
        return getList("participants");
    }
}
