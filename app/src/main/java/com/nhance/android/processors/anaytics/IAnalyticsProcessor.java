package com.nhance.android.processors.anaytics;

import com.nhance.android.db.models.entity.Question;
import com.nhance.android.pojos.TakeTestQuestionWithAnswerGiven;
import com.nhance.android.pojos.TakeTestQuestionWithAnswerGiven.AttemptStatus;

public interface IAnalyticsProcessor {
    public void process(TakeTestQuestionWithAnswerGiven questionAnsInfo, AttemptStatus attemptStatus, Question question);
}
