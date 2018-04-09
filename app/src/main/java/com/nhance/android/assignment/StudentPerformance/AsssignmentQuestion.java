package com.nhance.android.assignment.StudentPerformance;

import com.nhance.android.db.models.entity.ContentBoardEntity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by administrator on 6/29/17.
 */

public class AsssignmentQuestion  {


    /**
     *
     */
    private static final long serialVersionUID = 1L;

    // name--> content of the question
    public String             name;
    public String             id;

    // questionType SCQ/MCQ etc
    public String             type;
    public String             difficulty;

    public String             options;

    public String             status;

    // JSONObject--> Map<String, List<String>>
    public JSONObject matrix;

    // an organization
    public String             code;
    public boolean            hasAns;

    public String          answerGiven;

    public String             solutions;

    public String         correctAnswer;

    public String             source;

    public int position;

    public AsssignmentQuestion() {

        super();
    }


    @Override
    public String toString() {
        return "AsssignmentQuestion{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", difficulty='" + difficulty + '\'' +
                ", options='" + options + '\'' +
                ", status='" + status + '\'' +
                ", matrix=" + matrix +
                ", code='" + code + '\'' +
                ", hasAns=" + hasAns +
                ", answerGiven='" + answerGiven + '\'' +
                ", solutions='" + solutions + '\'' +
                ", correctAnswer='" + correctAnswer + '\'' +
                ", source='" + source + '\'' +
                ", position=" + position +
                '}';
    }
}
