package com.nhance.android.adapters.tests;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;

import com.nhance.android.customviews.NonScrollableGridView;
import com.nhance.android.pojos.tests.TestBoardQuestionAttemptInfo;
import com.nhance.android.utils.ViewUtils;
import com.nhance.android.R;

public class TestYourAnswerCourseListAdapter extends ArrayAdapter<TestBoardQuestionAttemptInfo> {

    OnItemClickListener questionItemClickListner;

    public TestYourAnswerCourseListAdapter(Context context, int textViewResourceId,
            List<TestBoardQuestionAttemptInfo> objects, OnItemClickListener questionItemClickListner) {

        super(context, textViewResourceId, objects);
        this.questionItemClickListner = questionItemClickListner;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_item_view_test_course_your_answer, parent, false);
        }

        TestBoardQuestionAttemptInfo item = getItem(position);
        if (item == null) {
            return null;
        }

        ViewUtils.setTextViewValue(view, R.id.test_course_name, item.boardAnalytics.name);
        ViewUtils.setTextViewValue(view, R.id.test_course_marks_scored, item.boardAnalytics.score);

        NonScrollableGridView gridView = (NonScrollableGridView) view
                .findViewById(R.id.test_question_answers);

        gridView.setAdapter(new TestYourAnswerAdapter(getContext(),
                R.layout.list_item_view_test_your_answer_grid_view, item.questionAttemptInfo));
        gridView.setOnItemClickListener(questionItemClickListner);
        view.setTag(item);
        return view;
    }

}
