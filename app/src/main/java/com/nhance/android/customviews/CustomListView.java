package com.nhance.android.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ListView;

import com.nhance.android.R;

public class CustomListView extends ListView {

    private boolean scroll;

    public CustomListView(Context context, AttributeSet attrs) {

        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PieChartView, 0, 0);

        try {
            scroll = a.getBoolean(R.styleable.CustomListView_scroll, false);
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        // Do not use the highest two bits of Integer.MAX_VALUE because they are
        // reserved for the MeasureSpec mode
        if (scroll) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        int heightSpec = MeasureSpec.makeMeasureSpec(MEASURED_SIZE_MASK, MeasureSpec.AT_MOST);

        super.onMeasure(widthMeasureSpec, heightSpec);

        getLayoutParams().height = getMeasuredHeight();
    }

    public boolean isScroll() {

        return scroll;
    }

    public void setScroll(boolean scroll) {

        this.scroll = scroll;
    }

}