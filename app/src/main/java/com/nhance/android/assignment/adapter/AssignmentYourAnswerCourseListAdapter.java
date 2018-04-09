package com.nhance.android.assignment.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.nhance.android.R;
import com.nhance.android.assignment.pojo.assignment.AssignmentBoardQuestionAttemptInfo;
import com.nhance.android.customviews.NonScrollableGridView;
import com.nhance.android.utils.ViewUtils;

import java.util.List;

/**
 * Created by Himank Shah on 12/7/2016.
 */

public class AssignmentYourAnswerCourseListAdapter extends ArrayAdapter<AssignmentBoardQuestionAttemptInfo> {

    AdapterView.OnItemClickListener questionItemClickListner;
    int assignmentAttempts;

    public AssignmentYourAnswerCourseListAdapter(Context context, int textViewResourceId,
                                                 List<AssignmentBoardQuestionAttemptInfo> objects, AdapterView.OnItemClickListener questionItemClickListner, int assignmentAttempts) {

        super(context, textViewResourceId, objects);
        this.questionItemClickListner = questionItemClickListner;
        this.assignmentAttempts = assignmentAttempts;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_item_view_assignment_course_your_answer, parent, false);
        }

        AssignmentBoardQuestionAttemptInfo item = getItem(position);
        if (item == null) {
            return null;
        }

        ViewUtils.setTextViewValue(view, R.id.assignment_course_name, item.boardAnalytics.name);
        ViewUtils.setTextViewValue(view, R.id.assignment_course_marks_scored, assignmentAttempts);

        NonScrollableGridView gridView = (NonScrollableGridView) view
                .findViewById(R.id.assignment_question_answers);

        gridView.setAdapter(new AssignmentYourAnswerAdapter(getContext(),
                R.layout.list_item_view_assignment_your_answer_grid_view, item.questionAttemptInfo));
        gridView.setOnItemClickListener(questionItemClickListner);
        view.setTag(item);
        return view;
    }

}
