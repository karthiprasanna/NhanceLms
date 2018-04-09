package com.nhance.android.customviews;

import android.text.TextUtils;

public class BarGraphBean {
	private int percentage;
	private String title;
	public BarGraphBean(int percentage, String title) {
		super();
		this.percentage = percentage>100?100:percentage;
		this.title = TextUtils.isEmpty(title)?String.valueOf(this.percentage):title;
	}
	public int getPercentage() {
		return percentage;
	}
	public void setPercentage(int percentage) {
		this.percentage = percentage;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	@Override
	public String toString(){
		return "percentage == "+percentage+", title === "+title;
	}
}
