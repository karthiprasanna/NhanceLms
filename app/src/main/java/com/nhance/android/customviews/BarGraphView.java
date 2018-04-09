package com.nhance.android.customviews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nhance.android.R;

public class BarGraphView extends RelativeLayout{
	private int 			graphColor;
	private int 			titleBgColor;
	private int 			titleColor;
	private int				percentageTextColor;
	private float			percentageTextSize;
	private int				percentageTextSizeInPixel;
	private int 			maxNoOfGraphs = 5;
	private String          xAxisTitle="";
	private View			parentView;
	private String			TAG	= "CustomBarGraph";
	private LayoutInflater 	layoutInflater;
	private int				eachBarWidth;
	protected Handler       mHandler = new Handler();
	private BarGraphBean[] 	savedValues=null;
	
	public BarGraphView(Context context) {
		super(context);
	}
	public BarGraphView(Context context,AttributeSet attrs) {
		super(context,attrs);
		setAttrs(context, attrs, 0);
		init(context);
	}
	public BarGraphView(Context context,AttributeSet attrs, int defStyle) {
		super(context,attrs,defStyle);
		setAttrs(context, attrs, defStyle);
		init(context);
	}
	private void setAttrs(Context context,AttributeSet attrs,int defStyle){
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs,R.styleable.BarGraphView,0,0);
		graphColor = a.getInt(R.styleable.BarGraphView_graphColor,getResources().getColor(R.color.darkgrey));
		titleBgColor = a.getInt(R.styleable.BarGraphView_titleBgColor,getResources().getColor(R.color.darkergrey));
		titleColor = a.getInt(R.styleable.BarGraphView_titleColor,getResources().getColor(R.color.white));
		percentageTextColor = a.getInt(R.styleable.BarGraphView_percentageTextColor,getResources().getColor(R.color.white));
		percentageTextSize = a.getFloat(R.styleable.BarGraphView_percentageTextSize,getResources().getDimension(R.dimen.bar_graph_percentage_font_size));
		percentageTextSizeInPixel = a.getDimensionPixelSize(R.styleable.BarGraphView_percentageTextSize,getResources().getDimensionPixelSize(R.dimen.bar_graph_percentage_font_size));
		maxNoOfGraphs = a.getInt(R.styleable.BarGraphView_maxNoOfGraphs,maxNoOfGraphs);
		xAxisTitle = a.getString(R.styleable.BarGraphView_xAxisTitle);
		xAxisTitle = TextUtils.isEmpty(xAxisTitle)?"":xAxisTitle;
	}
	private void init(Context context){
		layoutInflater = LayoutInflater.from(context);
		parentView = layoutInflater.inflate(R.layout.bar_graph_main,this);
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
					initializeGraph();
					if(savedValues!=null){
						setValues(savedValues);
					}
				}
			});
		}
	}
	private void initializeGraph(){
		int height = parentView.getHeight();
		int eachLineHeight = getResources().getDimensionPixelSize(R.dimen.bar_graph_lines_margin_top)+1;
		int numberOfLines = height/eachLineHeight;
		LinearLayout barGraphLineHolder = (LinearLayout) parentView.findViewById(R.id.bar_graph_line_holder);
		if(barGraphLineHolder.getChildCount()>0){
			barGraphLineHolder.removeAllViews();
		}
		for(int index=0;index<numberOfLines;index++){
			View v = layoutInflater.inflate(R.layout.bar_graph_line, barGraphLineHolder,false);
			barGraphLineHolder.addView(v);
		}
		Log.d(TAG,"GET layout child count ======= "+barGraphLineHolder.getChildCount());
		
		TextView xAxisTitleView = (TextView) parentView.findViewById(R.id.bar_graph_xAxis_title);
		xAxisTitleView.setText(xAxisTitle);
		
		int width = this.getWidth() - 2*(getResources().getDimensionPixelSize(R.dimen.bar_graph_holder_pad_left));
		int eachBarMarginRight = getResources().getDimensionPixelSize(R.dimen.bar_graph_bar_margin);
		eachBarWidth = (width - eachBarMarginRight*(maxNoOfGraphs-1))/maxNoOfGraphs;
		Log.d(TAG,"Each Bar Width Calculated === "+eachBarWidth);
	}
	public boolean setValues(BarGraphBean[] values){
		LinearLayout barGraphBarHolder = (LinearLayout) parentView.findViewById(R.id.bar_graph_bars_holder);
		LinearLayout barGraphTextHolder = (LinearLayout) parentView.findViewById(R.id.bar_graph_values_holder);
		if(barGraphBarHolder.getChildCount()>0){
			barGraphBarHolder.removeAllViews();
		}
		if(barGraphTextHolder.getChildCount()>0){
			barGraphTextHolder.removeAllViews();
		}
		int barMaxHeight = barGraphBarHolder.getHeight() - percentageTextSizeInPixel - 15;
		Log.d(TAG,"Values count === "+values.length);
		for(int index=0;index<values.length && index<maxNoOfGraphs;index++){
			BarGraphBean value = values[index];
			Log.d(TAG,"Value === "+value);
			if(value==null){
				continue;
			}
			int percentage = value.getPercentage();
			String title = value.getTitle();
			RelativeLayout bar = (RelativeLayout)layoutInflater.inflate(R.layout.bar_graph_bar, barGraphBarHolder, false);
			int barHeight = (percentage * barMaxHeight) / 100;
			barHeight = barHeight < 2 ? 2 : barHeight;
			bar.getLayoutParams().width = eachBarWidth;
			View barBlock = bar.findViewById(R.id.bar_graph_block);
			barBlock.getLayoutParams().height = barHeight;
			barBlock.setBackgroundColor(graphColor);
			TextView barPercent = (TextView) bar.findViewById(R.id.bar_graph_block_text);
			barPercent.setText(percentage+"%");
			barPercent.setTextColor(percentageTextColor);
			barPercent.setTextSize(percentageTextSize);
			Log.d(TAG,"NEW BAR ADDED === "+bar.getId());
			barGraphBarHolder.addView(bar);
			
			LinearLayout titleView = (LinearLayout)layoutInflater.inflate(R.layout.bar_graph_bar_title, barGraphTextHolder, false);
			TextView titleTextView = (TextView) titleView.getChildAt(0);
			titleTextView.setText(title);
			titleTextView.setBackgroundColor(titleBgColor);
			titleTextView.setTextColor(titleColor);
			titleView.getLayoutParams().width = eachBarWidth;
			barGraphTextHolder.addView(titleView);
			//postInvalidate();
		}
		Log.d(TAG,"done with set values === ");
		savedValues = values;
		return true;
	}
}
