package com.nhance.android.assignment.processors.analytics.sync;

import com.nhance.android.assignment.pojo.TakeAssignmentQuestionWithAnswerGiven;
import com.nhance.android.assignment.pojo.TakeAssignmentQuestionWithAnswerGiven.AssignementAttemptStatus;
import com.nhance.android.db.models.entity.Question;

/**
 * Created by Himank Shah on 12/6/2016.
 */

public interface AssignmentIAnalyticsProcessor {
    public void process(TakeAssignmentQuestionWithAnswerGiven questionAnsInfo, AssignementAttemptStatus attemptStatus, Question question);
}
