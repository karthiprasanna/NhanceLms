package com.nhance.android.assignment.pojo.assignment;

import com.nhance.android.assignment.db.models.analytics.AssignmentBoardAnalytics;
import com.nhance.android.assignment.pojo.TakeAssignmentQuestionWithAnswerGiven;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Himank Shah on 12/7/2016.
 */

public class AssignmentBoardQuestionAttemptInfo implements Serializable {

    /**
     *
     */
    private static final long                    serialVersionUID = 1L;
    public AssignmentBoardAnalytics boardAnalytics;
    public List<TakeAssignmentQuestionWithAnswerGiven> questionAttemptInfo;

    public AssignmentBoardQuestionAttemptInfo(AssignmentBoardAnalytics boardAnalytics,
                                        List<TakeAssignmentQuestionWithAnswerGiven> questionAttemptInfo) {

        super();
        this.boardAnalytics = boardAnalytics;
        this.questionAttemptInfo = questionAttemptInfo;
    }

}
