package com.nhance.android.processors.anaytics;

import android.content.Context;

import com.nhance.android.db.models.entity.Content;
import com.nhance.android.db.models.entity.Question;
import com.nhance.android.pojos.TakeTestQuestionWithAnswerGiven;
import com.nhance.android.pojos.TakeTestQuestionWithAnswerGiven.AttemptStatus;

public class TestCourseWiseLevelAnalyticsProcessor extends AbstractAnalyticsProcessor {

    public TestCourseWiseLevelAnalyticsProcessor(Context context, Content test, String userId) {

        super(context, test, userId);
    }

    @Override
    public void process(TakeTestQuestionWithAnswerGiven questionAnsInfo,
            AttemptStatus attemptStatus, Question question) {

    }

}
