package com.qianchen.sportsbuddy;

/**
 * Created by Qian Chen on 8/2/2014.
 */
public class ProfileInterest {

    private String kind;
    private float score;

    //TODO: rewrite get/set method to parse way
    public void setKind(String kind){
        this.kind = kind;
    }
    public String getKind(){
        return kind;
    }
    public void setScore(float score){
        this.score = score;
    }
    public float getScore(){
        return score;
    }
}