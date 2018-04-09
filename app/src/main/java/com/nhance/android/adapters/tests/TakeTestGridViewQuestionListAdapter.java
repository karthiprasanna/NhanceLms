package com.nhance.android.adapters.tests;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nhance.android.pojos.TakeTestQuestionWithAnswerGiven;
import com.nhance.android.pojos.TakeTestQuestionWithAnswerGiven.AttemptStatus;
import com.nhance.android.R;

public class TakeTestGridViewQuestionListAdapter extends
        ArrayAdapter<TakeTestQuestionWithAnswerGiven> {

    private int                                   currentQus;
    private List<TakeTestQuestionWithAnswerGiven> questionAttemptInfoList;

    public TakeTestGridViewQuestionListAdapter(Context context, int textViewResourceId,
            List<TakeTestQuestionWithAnswerGiven> objects) {

        super(context, textViewResourceId, objects);
        this.questionAttemptInfoList = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if (view == null) {
            LayoutInflater inflator = (LayoutInflater) getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            view = inflator.inflate(R.layout.grid_item_view_take_test_question, parent, false);
        }
        TakeTestQuestionWithAnswerGiven questionAttemptInfo = questionAttemptInfoList.get(position);
        TextView qTextView = (TextView) ((ViewGroup) view).getChildAt(0);
        view.setTag(questionAttemptInfo);

        qTextView.setText(getContext().getResources().getString(R.string.q)
                + (questionAttemptInfo.getQusNo()));

        if (questionAttemptInfo.getStatus() == null) {
            questionAttemptInfo.setStatus(AttemptStatus.SKIP);
        }

        if (currentQus == position) {
       //view.setPadding(3, 3, 3, 3);
         //   view.setBackgroundResource(R.color.green);
            //view.setBackground(getContext().getDrawable(R.drawable.question_correct_round_corner));
        } else {
     // view.setPadding(1, 1, 1, 1);
         //   view.setBackgroundResource(R.color.morelightergrey);

        }
        if (questionAttemptInfo.getStatus().equals(AttemptStatus.REVIEW)) {

            qTextView.setBackground(getContext().getDrawable(R.drawable.question_blue_round_corner));

            qTextView.setTextColor(getContext().getResources().getColor(R.color.white));
        } else if (questionAttemptInfo.getStatus().equals(AttemptStatus.SAVED)) {

            qTextView.setBackground(getContext().getDrawable(R.drawable.question_correct_round_corner));
            qTextView.setTextColor(getContext().getResources().getColor(R.color.white));
        } else {
            qTextView.setBackground(getContext().getDrawable(R.drawable.question_darkgrey_round_corner));
            qTextView.setTextColor(getContext().getResources().getColor(R.color.white));
        }
        return view;
    }

    public void setCurrentQus(int currentQus) {

        this.currentQus = currentQus;
    }
}
