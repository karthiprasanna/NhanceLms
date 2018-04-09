package com.nhance.android.ChallengeArena;

import org.json.JSONArray;

/**
 * Created by administrator on 8/30/17.
 */

class SolutionInfo {



    public String lastName;
    public String thumbnail;
    public String firstName;
    public String profile;
    public String name;

    public String Q_No;

    public String id;
    public String boardsubid;

    @Override
    public String toString() {
        return "QuestionInfo{" +
                "lastName='" + lastName + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                ", firstName='" + firstName + '\'' +
                ", profile='" + profile + '\'' +
                ", name='" + name + '\'' +
                ", Q_No='" + Q_No + '\'' +
                ", id='" + id + '\'' +
                ", boardsubid='" + boardsubid + '\'' +
                ", boardsubname='" + boardsubname + '\'' +
                ", userAnswer='" + userAnswer + '\'' +
                ", correctAnswer='" + correctAnswer + '\'' +
                ", lastUpdated='" + lastUpdated + '\'' +
                ", attempts='" + attempts + '\'' +
                ", type='" + type + '\'' +
                ", timeCreated=" + timeCreated +
                ", content='" + content + '\'' +
                ", attempted='" + attempted + '\'' +
                ", solutions='" + solutions + '\'' +
                ", totalHits='" + totalHits + '\'' +
                ", comments='" + comments + '\'' +
                ", qid='" + qid + '\'' +
                ", option=" + option +
                '}';
    }

    public  String boardsubname;

    public String userAnswer;
    public String correctAnswer;

    public String lastUpdated;
    public String attempts;

    public String type;

    public long timeCreated;

    public String content ;
    public String attempted;
    public String solutions;

    public String totalHits;
    public String comments;
    public String qid;

    public JSONArray option;

}
