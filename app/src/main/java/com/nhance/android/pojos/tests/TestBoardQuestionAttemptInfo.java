package com.nhance.android.pojos.tests;

import java.io.Serializable;
import java.util.List;

import com.nhance.android.db.models.analytics.TestBoardAnalytics;
import com.nhance.android.pojos.TakeTestQuestionWithAnswerGiven;

public class TestBoardQuestionAttemptInfo implements Serializable {

    /**
     * 
     */
    private static final long                    serialVersionUID = 1L;
    public TestBoardAnalytics                    boardAnalytics;
    public List<TakeTestQuestionWithAnswerGiven> questionAttemptInfo;

    public TestBoardQuestionAttemptInfo(TestBoardAnalytics boardAnalytics,
            List<TakeTestQuestionWithAnswerGiven> questionAttemptInfo) {

        super();
        this.boardAnalytics = boardAnalytics;
        this.questionAttemptInfo = questionAttemptInfo;
    }

}
