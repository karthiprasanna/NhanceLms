package com.nhance.android.customviews;

import com.nhance.android.utils.ViewUtils;
import com.nhance.android.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

public class ThreeHorizontalBarGraph extends RelativeLayout{
	protected Handler       mHandler = new Handler();
	private String			TAG	= "CustomHorizontalBarGraph";
	private int				maxBarWidth = 0;
	final int 				barWidthOffset = 20;
	private LayoutInflater 	layoutInflater;
	private View			parentView;
	private int				correct;
	private int				incorrect;
	private int				left;
	
	public ThreeHorizontalBarGraph(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	private void init(Context context){
		layoutInflater = LayoutInflater.from(context);
		parentView = layoutInflater.inflate(R.layout.horizontal_bar_graph,this);
	}
	@SuppressLint("DrawAllocation")
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b){
		super.onLayout(changed, l, t, r, b);
		Log.d(TAG,"LAYOUT CHANGED === "+changed);
		if(changed){
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					calcBarWidth();
					setValues(correct, incorrect, left);
				}
			});
		}
	}
	private void calcBarWidth(){
		maxBarWidth = parentView.getWidth() - barWidthOffset;
	}
	public boolean setValues(int correctCount,int incorrectCount,int leftCount){
		int total = correctCount + incorrectCount + leftCount;
		View correctView = parentView.findViewById(R.id.test_student_answers_correct_bar);
		View incorrectView = parentView.findViewById(R.id.test_student_answers_incorrect_bar);
		View leftView = parentView.findViewById(R.id.test_student_answers_left_bar);
		correctView.getLayoutParams().width = calcBarWidth(total,correctCount);
		incorrectView.getLayoutParams().width = calcBarWidth(total,incorrectCount);
		leftView.getLayoutParams().width = calcBarWidth(total,leftCount);
		ViewUtils.setTextViewValue(parentView, R.id.test_student_answers_correct_bar_text, String.valueOf(correctCount));
		ViewUtils.setTextViewValue(parentView, R.id.test_student_answers_incorrect_bar_text, String.valueOf(incorrectCount));
		ViewUtils.setTextViewValue(parentView, R.id.test_student_answers_left_bar_text, String.valueOf(leftCount));
		correct = correctCount;
		incorrect = incorrectCount;
		left = leftCount;
		return true;
	}
	private int calcBarWidth(int total,int value){
		int width = 1;
		total = total == 0 ? 1 : total;
		width = (value*maxBarWidth)/total;
		Log.d(TAG,"Total = "+total+", value = "+value+", maxWidth = "+maxBarWidth+", width = "+width);
		width = width<=0?1:width;
		return width;
	}
}
