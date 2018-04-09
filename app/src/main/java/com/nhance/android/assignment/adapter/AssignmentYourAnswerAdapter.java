package com.nhance.android.assignment.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nhance.android.R;
import com.nhance.android.assignment.pojo.TakeAssignmentQuestionWithAnswerGiven;

import java.util.List;

/**
 * Created by Himank Shah on 12/7/2016.
 */

public class AssignmentYourAnswerAdapter extends ArrayAdapter<TakeAssignmentQuestionWithAnswerGiven> {

    SparseIntArray colorMap;
    int            CORRECT   = -1;
    int            INCORRECT = -2;
    int            LEFT      = -3;

    public AssignmentYourAnswerAdapter(Context context, int textViewResourceId,
                                       List<TakeAssignmentQuestionWithAnswerGiven> objects) {

        super(context, textViewResourceId, objects);
        colorMap = new SparseIntArray();
        colorMap.put(CORRECT, getContext().getResources().getColor(R.color.green));
        colorMap.put(INCORRECT, getContext().getResources().getColor(R.color.red));
        colorMap.put(LEFT, getContext().getResources().getColor(R.color.lightergrey));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_item_view_assignment_your_answer_grid_view, parent,
                    false);
        }
        TakeAssignmentQuestionWithAnswerGiven item = getItem(position);
        view.setTag(item);
        setUpGridView(view, item);
        return view;
    }

    private void setUpGridView(View view, TakeAssignmentQuestionWithAnswerGiven qusAnswerAnalytics) {

        int qusStatus = LEFT;
        if (qusAnswerAnalytics.correct) {
            qusStatus = CORRECT;
        } else if (!qusAnswerAnalytics.correct
                && !TextUtils.isEmpty(qusAnswerAnalytics.answerGiven)) {
            qusStatus = INCORRECT;
        }
        TextView qTextView = (TextView) view;
        qTextView.setBackgroundColor(colorMap.get(qusStatus));

        qTextView.setText(getContext().getString(R.string.q) + qusAnswerAnalytics.getQusNo());
    }

}
