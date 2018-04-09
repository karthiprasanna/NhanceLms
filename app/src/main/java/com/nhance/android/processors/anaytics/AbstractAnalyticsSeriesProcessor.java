package com.nhance.android.processors.anaytics;

import android.util.Log;

import com.nhance.android.db.models.entity.Question;
import com.nhance.android.pojos.TakeTestQuestionWithAnswerGiven;
import com.nhance.android.pojos.TakeTestQuestionWithAnswerGiven.AttemptStatus;

public abstract class AbstractAnalyticsSeriesProcessor {

    IAnalyticsProcessor[] processors;

    protected AbstractAnalyticsSeriesProcessor(IAnalyticsProcessor... processors) {

        this.processors = processors;
    }

    public void process(TakeTestQuestionWithAnswerGiven questionAnsInfo,
            AttemptStatus attemptStatus, Question question) {

        for (IAnalyticsProcessor processor : processors) {
            Log.d(this.getClass().getSimpleName(), "processing  question with :  " + processor);
            processor.process(questionAnsInfo, attemptStatus, question);
        }
    }
}
