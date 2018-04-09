package com.nhance.android.assignment.UserPerformance.model;

import org.json.JSONObject;

import org.json.JSONArray;


    public class GridItemInfo  {

        private static final long serialVersionUID = 1L;

        // name--> content of the question
        public String             name;
        public String             id;

        // questionType SCQ/MCQ etc
        public String             type;
        public String             difficulty;
        public String Q_no;


        public JSONArray             options;

        public String             status;
        public String content;
        public String boardsubid;
        public String course;

        @Override
        public String toString() {
            return "GridItemInfo{" +
                    "name='" + name + '\'' +
                    ", id='" + id + '\'' +
                    ", type='" + type + '\'' +
                    ", difficulty='" + difficulty + '\'' +
                    ", Q_no='" + Q_no + '\'' +
                    ", options=" + options +
                    ", status='" + status + '\'' +
                    ", content='" + content + '\'' +
                    ", boardsubid='" + boardsubid + '\'' +
                    ", course='" + course + '\'' +
                    ", matrix=" + matrix +
                    ", code='" + code + '\'' +
                    ", hasAns=" + hasAns +
                    ", answerGiven=" + answerGiven +
                    ", solutions='" + solutions + '\'' +
                    ", answer=" + answer +
                    ", correctAnswer=" + correctAnswer +
                    ", source='" + source + '\'' +
                    ", position=" + position +
                    '}';
        }

        // JSONObject--> Map<String, List<String>>
        public JSONObject matrix;

        // an organization

        public String             code;
        public boolean            hasAns;

        public JSONArray          answerGiven;

        public String             solutions;
        public JSONObject answer;

        public JSONArray         correctAnswer;

        public String             source;

        public int position;


    }
