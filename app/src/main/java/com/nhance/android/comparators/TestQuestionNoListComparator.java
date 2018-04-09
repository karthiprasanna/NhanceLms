package com.nhance.android.comparators;

import java.util.Comparator;

import com.nhance.android.pojos.TakeTestQuestionWithAnswerGiven;

public class TestQuestionNoListComparator implements Comparator<TakeTestQuestionWithAnswerGiven> {

    @Override
    public int compare(TakeTestQuestionWithAnswerGiven lhs, TakeTestQuestionWithAnswerGiven rhs) {

        return lhs.getQusNo() < rhs.getQusNo() ? -1 : (lhs.getQusNo() > rhs.getQusNo() ? 1 : 0);
    }

}
