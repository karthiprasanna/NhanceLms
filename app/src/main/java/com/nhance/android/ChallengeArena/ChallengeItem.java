package com.nhance.android.ChallengeArena;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

public class ChallengeItem {


    public String channelname,actopictext,acsubjecttext;
    public String leveltext;
    public int pointstext,attemptstext;
    public  long challenge_count;
public  String subjectid;
    public String topperName;
    public  String thumbnail;
    public String challengeId;

    public JSONArray toppers;

    public String totalPoint;

    public  long take_ch_count;
    public  long challenge_time ;
    public  String id;
    public String attempted;
    HashMap<String, String> subject_name_list = new HashMap<>();
    public Map<String, HashMap<String,String>> subname1 = new HashMap<String, HashMap<String,String>>();
    public Map<String, HashMap<String,String>> subId1 = new HashMap<String, HashMap<String,String>>();

    public long lifetime;

    public long timeCreated;

    public String challengeclosed_time;


    @Override
    public String toString() {
        return "ChallengeItem{" +
                "channelname='" + channelname + '\'' +
                ", actopictext='" + actopictext + '\'' +
                ", acsubjecttext='" + acsubjecttext + '\'' +
                ", leveltext='" + leveltext + '\'' +
                ", pointstext=" + pointstext +
                ", attemptstext=" + attemptstext +
                ", challenge_count=" + challenge_count +
                ", subjectid='" + subjectid + '\'' +
                ", topperName='" + topperName + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                ", challengeId='" + challengeId + '\'' +
                ", toppers=" + toppers +
                ", totalPoint='" + totalPoint + '\'' +
                ", take_ch_count=" + take_ch_count +
                ", challenge_time=" + challenge_time +
                ", id='" + id + '\'' +
                ", attempted='" + attempted + '\'' +
                ", subject_name_list=" + subject_name_list +
                ", subname1=" + subname1 +
                ", subId1=" + subId1 +
                ", lifetime=" + lifetime +
                ", timeCreated=" + timeCreated +
                ", challengeclosed_time='" + challengeclosed_time + '\'' +
                '}';
    }
}
