package com.nhance.android.adapters.tests;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.nhance.android.pojos.onlinedata.tests.EntityAttemptInfo;
import com.nhance.android.pojos.tests.EntityMeasures;
import com.nhance.android.utils.ViewUtils;
import com.nhance.android.R;

public class TestStudentAnswersQuestionListAdapter extends ArrayAdapter<EntityAttemptInfo> {

    OnClickListener      questionItemClickListner;
    final private String TAG = "TestStudentAnswersFragment";

    public TestStudentAnswersQuestionListAdapter(Context context, int resource,
            List<EntityAttemptInfo> objects, OnClickListener questionItemClickListner) {

        super(context, resource, objects);
        this.questionItemClickListner = questionItemClickListner;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_item_view_test_student_answer, parent, false);
        }
        EntityAttemptInfo ques = getItem(position);
        if (ques == null) {
            return null;
        }
        String qText = parent.getResources().getString(R.string.q);
        int quesNo = ques.qusNo;
        ViewUtils.setTextViewValue(view, R.id.test_student_answers_ques_no, String.valueOf(quesNo),
                null, qText, null);

        EntityMeasures measures = ques.measures;
        view.setOnClickListener(questionItemClickListner);
        view.setTag(R.integer.question_id_key, ques.id);
        view.setTag(R.integer.question_measures_key, measures);
        view.setClickable(true);
        return view;
    }
}
