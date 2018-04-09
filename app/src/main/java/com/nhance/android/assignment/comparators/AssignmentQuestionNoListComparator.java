package com.nhance.android.assignment.comparators;

import com.nhance.android.assignment.pojo.TakeAssignmentQuestionWithAnswerGiven;

import java.util.Comparator;

/**
 * Created by Himank Shah on 12/6/2016.
 */

public class AssignmentQuestionNoListComparator implements Comparator<TakeAssignmentQuestionWithAnswerGiven> {

    @Override
    public int compare(TakeAssignmentQuestionWithAnswerGiven lhs, TakeAssignmentQuestionWithAnswerGiven rhs) {

        return lhs.getQusNo() < rhs.getQusNo() ? -1 : (lhs.getQusNo() > rhs.getQusNo() ? 1 : 0);
    }

}
