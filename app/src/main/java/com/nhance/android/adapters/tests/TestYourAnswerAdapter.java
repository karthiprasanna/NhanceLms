package com.nhance.android.adapters.tests;

import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nhance.android.pojos.TakeTestQuestionWithAnswerGiven;
import com.nhance.android.R;

public class TestYourAnswerAdapter extends ArrayAdapter<TakeTestQuestionWithAnswerGiven> {

    SparseIntArray colorMap;
    int            CORRECT   = -1;
    int            INCORRECT = -2;
    int            LEFT      = -3;

    public TestYourAnswerAdapter(Context context, int textViewResourceId,
            List<TakeTestQuestionWithAnswerGiven> objects) {

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
            view = inflater.inflate(R.layout.list_item_view_test_your_answer_grid_view, parent,
                    false);
        }
        TakeTestQuestionWithAnswerGiven item = getItem(position);
        view.setTag(item);
        setUpGridView(view, item);
        return view;
    }

    private void setUpGridView(View view, TakeTestQuestionWithAnswerGiven qusAnswerAnalytics) {

        int qusStatus = LEFT;
        if (qusAnswerAnalytics.correct) {
            qusStatus = CORRECT;
        } else if (!qusAnswerAnalytics.correct
                && !TextUtils.isEmpty(qusAnswerAnalytics.answerGiven)) {
            qusStatus = INCORRECT;
        }
        TextView qTextView = (TextView) view;
        qTextView.setBackgroundColor(colorMap.get(qusStatus));

        if (qusStatus==CORRECT) {
            qTextView.setText(getContext().getString(R.string.q) + qusAnswerAnalytics.getQusNo());
            qTextView.setBackground(getContext().getResources().getDrawable(R.drawable.correct_round_corner));
        }else if (qusStatus==INCORRECT) {
            qTextView.setText(getContext().getString(R.string.q) + qusAnswerAnalytics.getQusNo());
            qTextView.setBackground(getContext().getResources().getDrawable(R.drawable.incorrect_round_corner));
        }else if (qusStatus==LEFT) {
            qTextView.setText(getContext().getString(R.string.q) + qusAnswerAnalytics.getQusNo());
            qTextView.setBackground(getContext().getResources().getDrawable(R.drawable.left_round_corner));
        }
    }

}
