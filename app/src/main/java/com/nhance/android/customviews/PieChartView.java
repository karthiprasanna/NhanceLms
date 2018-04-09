package com.nhance.android.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import com.nhance.android.R;

public class PieChartView extends View {

    private int     iDisplayWidth, iDisplayHeight;
    private Paint   paint        = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF   rectF;

    private int     iCenterWidth = 0;
    private int     iShift       = 0;
    private int     iMargin      = 0;                               // margin to left and
                                                                     // right,used
                                                                     // for get Radius

    private float   fDensity     = 0.0f;
    private float   fStartAngle  = -90f;

    private int     mChartType;

    private int     fillColor;                                      // will be used to fill the
                                                                     // innerCircle of the Chart

    private int[]   colors;                                          ; // default colors
    private float[] values;                                         // default values

    public PieChartView(Context context, AttributeSet attrs) {

        this(context, attrs, 0);
    }

    public PieChartView(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PieChartView, 0, 0);

        try {
            mChartType = a.getInt(R.styleable.PieChartView_chartType, -1);
            iShift = a.getDimensionPixelSize(R.styleable.PieChartView_innerShift, 0);
            fillColor = a.getColor(R.styleable.PieChartView_fillColor,
                    getResources().getColor(R.color.darkergrey));
        } finally {
            a.recycle();
        }
        initDefaultValues();
        fnGetDisplayMetrics(context);

        iMargin = (int) fnGetRealPxFromDp(0);
    }

    /**
     * by calling this method on constructor we will be able to see it's view on xml layout
     */
    private void initDefaultValues() {

        if (mChartType == 1) {
            // init default values;
            colors = new int[] { getResources().getColor(R.color.green),
                    getResources().getColor(R.color.lightgrey),
                    getResources().getColor(R.color.red) };
            values = new float[] { 4, 3, 1 };
        } else {
            // init default values;
            colors = new int[] { getResources().getColor(R.color.green) };
            values = new float[] { 75 };
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);
        if (mChartType == 1) {
            drawDonutChart(canvas);
        } else {
            drawPieChart(canvas);
        }
    }

    private void drawPieChart(Canvas canvas) {

        paint.setColor(fillColor);

        canvas.drawCircle(iCenterWidth, iCenterWidth, iCenterWidth - iShift, paint);
        float[] arcValues = getArchValues(values);

        for (int i = 0; i < arcValues.length; i++) {
            paint.setColor(colors[i]);
            canvas.drawArc(rectF, fStartAngle, arcValues[i], true, paint);
        }
    }

    private void drawDonutChart(Canvas canvas) {

        float[] arcValues = getArchValues(getPercentageArcValues(values));
        for (int i = 0; i < arcValues.length; i++) {
            if (arcValues[i] == 0f) {
                continue;
            }
            paint.setColor(colors[i]);
            canvas.drawArc(rectF, fStartAngle, arcValues[i], true, paint);
            fStartAngle += arcValues[i];
        }
        paint.setColor(fillColor);
        canvas.drawCircle(iCenterWidth, iCenterWidth, iCenterWidth - iShift, paint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // get screen size
        iDisplayWidth = MeasureSpec.getSize(widthMeasureSpec);
        iDisplayHeight = MeasureSpec.getSize(heightMeasureSpec);

        if (iDisplayWidth > iDisplayHeight) {
            iDisplayWidth = iDisplayHeight;
        }

        /*
         * determine the rectangle size
         */
        iCenterWidth = iDisplayWidth / 2;
        int iR = iCenterWidth - iMargin;
        if (rectF == null) {
            rectF = new RectF(iCenterWidth - iR, // top
                    iCenterWidth - iR, // left
                    iCenterWidth + iR, // rights
                    iCenterWidth + iR); // bottom
        }
        setMeasuredDimension(iDisplayWidth, iDisplayWidth);
    }

    private float[] getArchValues(float[] values) {

        float[] arcValues = new float[values.length];

        for (int i = 0; i < values.length; i++) {
            arcValues[i] = (values[i] * 360) / 100;
        }
        return arcValues;
    }

    private float[] getPercentageArcValues(float[] values) {

        float[] arcValues = new float[values.length];
        float sum = 0;

        for (int i = 0; i < values.length; i++) {
            sum += values[i];
        }

        float valuesSum = 0;
        for (int i = 0; i < values.length; i++) {
            arcValues[i] = (values[i] * 100) / sum;
            valuesSum += arcValues[i];
            if (i == (values.length) && valuesSum != 100) {
                arcValues[i] = 100 - valuesSum - arcValues[i];
            }
        }
        return arcValues;
    }

    public int getChartType() {

        return mChartType;
    }

    public void setChartType(int chartType) {

        this.mChartType = chartType;
    }

    public int getInnerShift() {

        return iShift;
    }

    public void setInnerShift(int innerShift) {

        this.iShift = innerShift;
    }

    public int getFillColor() {

        return fillColor;
    }

    public void setFillColor(int fillColor) {

        this.fillColor = fillColor;
    }

    public void setColorAndValues(int[] colors, float[] values) {
        this.colors = colors;
        this.values = values;
    }

    private void fnGetDisplayMetrics(Context cxt) {

        final DisplayMetrics dm = cxt.getResources().getDisplayMetrics();
        fDensity = dm.density;
    }

    private float fnGetRealPxFromDp(float fDp) {

        return (fDensity != 1.0f) ? fDensity * fDp : fDp;
    }

}
