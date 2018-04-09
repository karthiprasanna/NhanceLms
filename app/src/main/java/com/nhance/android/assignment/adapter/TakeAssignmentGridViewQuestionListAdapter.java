package com.nhance.android.assignment.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nhance.android.R;
import com.nhance.android.assignment.pojo.TakeAssignmentQuestionWithAnswerGiven;
import com.nhance.android.assignment.pojo.TakeAssignmentQuestionWithAnswerGiven.AssignementAttemptStatus;

import java.util.List;

/**
 * Created by Himank Shah on 12/6/2016.
 */

public class TakeAssignmentGridViewQuestionListAdapter extends
        ArrayAdapter<TakeAssignmentQuestionWithAnswerGiven> {

    private int                                   currentQus;
    private List<TakeAssignmentQuestionWithAnswerGiven> questionAttemptInfoList;

    public TakeAssignmentGridViewQuestionListAdapter(Context context, int textViewResourceId,
                                                     List<TakeAssignmentQuestionWithAnswerGiven> objects) {

        super(context, textViewResourceId, objects);
        this.questionAttemptInfoList = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if (view == null) {
            LayoutInflater inflator = (LayoutInflater) getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            view = inflator.inflate(R.layout.grid_item_view_take_assignment_question, parent, false);
        }
        TakeAssignmentQuestionWithAnswerGiven questionAttemptInfo = questionAttemptInfoList.get(position);
        TextView qTextView = (TextView) ((ViewGroup) view).getChildAt(0);
        view.setTag(questionAttemptInfo);

        qTextView.setText(getContext().getResources().getString(R.string.q)
                + (questionAttemptInfo.getQusNo()));

        if (questionAttemptInfo.getStatus() == null) {
            questionAttemptInfo.setStatus(AssignementAttemptStatus.SKIP);
        }

        if (currentQus == position) {
            view.setPadding(3, 3, 3, 3);
            view.setBackgroundResource(R.color.green);
        } else {
            view.setPadding(1, 1, 1, 1);
            view.setBackgroundResource(R.color.morelightergrey);
        }
        if (questionAttemptInfo.getStatus().equals(AssignementAttemptStatus.REVIEW)) {
            qTextView.setBackgroundResource(R.color.blue);
            qTextView.setTextColor(getContext().getResources().getColor(R.color.white));
        } else if (questionAttemptInfo.getStatus().equals(AssignementAttemptStatus.SAVED)) {
            qTextView.setBackgroundResource(R.color.green);
            qTextView.setTextColor(getContext().getResources().getColor(R.color.white));
        } else {
            qTextView.setBackgroundResource(R.color.white);
            qTextView.setTextColor(getContext().getResources().getColor(R.color.darkgrey));
        }
        return view;
    }

    public void setCurrentQus(int currentQus) {

        this.currentQus = currentQus;
    }
}
