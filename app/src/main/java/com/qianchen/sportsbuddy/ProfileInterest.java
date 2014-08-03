package com.qianchen.sportsbuddy;

/**
 * Created by Qian Chen on 8/2/2014.
 */
public class ProfileInterest implements Comparable<ProfileInterest> {

    private String type;
    private float weight;

    public ProfileInterest(String type, float weight) {
        this.type = type;
        this.weight = weight;
    }

    public void setType(String type){
        this.type = type;
    }

    public String getType(){
        return type;
    }

    public void setScore(float weight){
        this.weight = weight;
    }

    public float getScore(){
        return weight;
    }

    @Override
    public int compareTo(ProfileInterest another) {
        return (int) ((another.weight - weight) * 100);
    }
}