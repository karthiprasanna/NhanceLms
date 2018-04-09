package com.nhance.android.assignment.processors.analytics;

import android.util.Log;

import com.nhance.android.assignment.pojo.TakeAssignmentQuestionWithAnswerGiven;
import com.nhance.android.assignment.pojo.TakeAssignmentQuestionWithAnswerGiven.AssignementAttemptStatus;
import com.nhance.android.assignment.processors.analytics.sync.AssignmentIAnalyticsProcessor;
import com.nhance.android.db.models.entity.Question;

/**
 * Created by Himank Shah on 12/6/2016.
 */

public abstract class AssignmentAbstractAnalyticsSeriesProcessor {

        AssignmentIAnalyticsProcessor[] processors;

        protected AssignmentAbstractAnalyticsSeriesProcessor(AssignmentIAnalyticsProcessor... processors) {

            this.processors = processors;
        }

        public void process(TakeAssignmentQuestionWithAnswerGiven questionAnsInfo,
                            AssignementAttemptStatus attemptStatus, Question question) {

            for (AssignmentIAnalyticsProcessor processor : processors) {
                Log.d(this.getClass().getSimpleName(), "processing  question with :  " + processor);
                processor.process(questionAnsInfo, attemptStatus, question);
            }
        }
    }


