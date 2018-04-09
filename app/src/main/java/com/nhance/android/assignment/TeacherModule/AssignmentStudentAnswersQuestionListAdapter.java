package com.nhance.android.assignment.TeacherModule;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.nhance.android.R;
import com.nhance.android.pojos.onlinedata.tests.EntityAttemptInfo;
import com.nhance.android.pojos.tests.EntityMeasures;
import com.nhance.android.utils.ViewUtils;

import java.util.List;

/**
 * Created by karthi on 1/3/17.
 */



public class AssignmentStudentAnswersQuestionListAdapter extends ArrayAdapter<EntityAttemptInfo> {

    View.OnClickListener questionItemClickListner;
    final private String TAG = "AssignmentStudentAnswersFragment";

    public AssignmentStudentAnswersQuestionListAdapter(Context context, int resource,
                                                       List<EntityAttemptInfo> objects, View.OnClickListener questionItemClickListner) {

        super(context, resource, objects);
        this.questionItemClickListner = questionItemClickListner;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_item_view_assignment_student_answer, parent, false);
        }
        EntityAttemptInfo ques = getItem(position);
        if (ques == null) {
            return null;
        }
        String qText = parent.getResources().getString(R.string.q);
        int quesNo = ques.qusNo;
        ViewUtils.setTextViewValue(view, R.id.assignment_student_answers_ques_no, String.valueOf(quesNo),
                null, qText, null);

        EntityMeasures measures = ques.measures;
        view.setOnClickListener(questionItemClickListner);
        view.setTag(R.integer.question_id_key, ques.id);
        view.setTag(R.integer.question_measures_key, measures);
        view.setClickable(true);
        return view;
    }
}
