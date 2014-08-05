package com.qianchen.sportsbuddy;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Event communicating with Parse.com
 * It contains the information of an event created by user.
 *
 * Created by Qian Chen on 7/27/2014.
 */
@ParseClassName("Event")
public class Event extends ParseObject implements Comparable<Event> {

    public static final int MILLISECONDS_PER_HOUR = 3600000;
    public static final int MILLISECONDS_PER_MINUTE = 60000;

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
        }
        participants.add(participant);
        put("participants", participants);
    }

    public List<String> getParticipants() {
        return getList("participants");
    }

    @Override
    public int compareTo(Event another) {
        Date date = new Date(getDateMilliseconds() + getHour() * MILLISECONDS_PER_HOUR + getMinute() * MILLISECONDS_PER_MINUTE);
        Date anotherDate = new Date(another.getDateMilliseconds() + another.getHour() * MILLISECONDS_PER_HOUR + another.getMinute() * MILLISECONDS_PER_MINUTE);
        return date.compareTo(anotherDate);
    }
}