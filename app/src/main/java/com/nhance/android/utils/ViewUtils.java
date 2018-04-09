package com.nhance.android.utils;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.nhance.android.db.models.AbstractTestAnalytics;
import com.nhance.android.jsinterfaces.SingleQuestionJSInterface;
import com.nhance.android.utils.FontUtils.FontTypes;
import com.nhance.android.utils.analytics.AnalyticsUtil;
import com.nhance.android.R;

@SuppressLint("NewApi")
public class ViewUtils {

    private static final String TAG = "ViewUtils";

    public static void setPercentageBar(View parentView, View childView, float percentage) {

        int parentHeight = parentView.getLayoutParams().height;
        setPercentageBar(parentView, childView, percentage, parentHeight);
    }

    public static void setPercentageBar(View parentView, View childView, float percentage,
            int parentHeight) {

        int percentageBarHeight = (int) ((parentHeight * percentage) / 100);
        Log.i(TAG, "parent height: " + parentHeight + ", accuracy: " + percentage);
        childView.getLayoutParams().height = percentageBarHeight;
        int color = AnalyticsUtil.getPercentageColorCode(percentage);
        childView.setBackgroundColor(parentView.getContext().getResources().getColor(color));

    }

    /**
     * <blockquote>this function assume that the max width of percentage bar will be
     * 100dp</blockquote>
     * 
     * @param percentageBar
     * @param percentage
     */
    public static void setPercentageBar(View percentageBar, int percentage) {

        percentageBar.getLayoutParams().width = dpToPx(percentage, percentageBar.getContext());
        Log.d(TAG, "layout params: " + percentageBar.getLayoutParams().width + ", percentage: "
                + percentage);
    }

    public static int dpToPx(int dp, Context context) {

        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp / scale + 0.5f);
    }

    public static int pxToDp(int pixels, Context context) {

        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pixels / scale + 0.5f);
    }

    public static void setTextViewValue(View parent, int id, int value) {

        setTextViewValue(parent, id, value, null);
    }

    public static void setTextViewValue(View parent, int id, String value, String appendWith,
            FontTypes fontTypes) {

        setTextViewValue(parent, id, value, appendWith, null, fontTypes);
    }

    public static void setTextViewValue(View parent, int id, int value, String appendWith) {

        setTextViewValue(parent, id, value, appendWith, null);
    }

    public static void setTextViewValue(View parent, int id, int value, String appendWith,
            String prependWith) {

        setTextViewValue(parent, id, String.valueOf(value), appendWith, prependWith, null);
    }

    public static void setTextViewValue(View parent, int id, String value, String appendWith,
            String prependWith, FontTypes fontTypes) {

        TextView textView = (TextView) parent.findViewById(id);
        setTextViewValue(textView, value, appendWith, prependWith, fontTypes);
    }

    public static void setTextViewValue(TextView textView, String value, String appendWith,
            FontTypes fontTypes) {

        setTextViewValue(textView, value, appendWith, null, fontTypes);
    }

    public static void setTextViewValue(TextView textView, String value, String appendWith,
            String prependWith, FontTypes fontTypes) {

        if (!TextUtils.isEmpty(prependWith)) {
            value = prependWith + value;
        }
        if (!TextUtils.isEmpty(appendWith)) {
            value += appendWith;
        }
        if (fontTypes != null) {
            FontUtils.setTypeface(textView, fontTypes);
        }
        textView.setText(StringUtils.defaultString(value));
    }

    public static void setTextViewValue(View parent, int id, String value) {

        setTextViewValue(parent, id, value, null);
    }

    public static void setAcurracyView(AbstractTestAnalytics analytics, View parentView) {

        setAcurracyView(analytics, parentView, 0);
    }

    public static void setAcurracyView(AbstractTestAnalytics analytics, View parentView,
            int parentViewHeight) {

        float accuracy = analytics.attempted < 1 ? 0 : (analytics.correct * 100)
                / analytics.attempted;
        Log.d(TAG, "accuracy : " + accuracy);
        View accuractView = parentView.findViewById(R.id.test_accuracy_container);

        if (parentViewHeight > 0) {
            ViewUtils.setPercentageBar(accuractView,
                    accuractView.findViewById(R.id.accuracy_bar_indicator), accuracy,
                    parentViewHeight);
        } else {
            ViewUtils.setPercentageBar(accuractView,
                    accuractView.findViewById(R.id.accuracy_bar_indicator), accuracy);
        }

        TextView accuracyTextView = (TextView) parentView
                .findViewById(R.id.accuracy_container_accuracy_value_as_value);
        accuracyTextView.setText(AnalyticsUtil.getStringDisplayValue(accuracy)
                + parentView.getContext().getResources().getString(R.string.percentage_indicator));
    }

    public static void setTextViewValue(View parent, int id, String value, String appendWith) {

        setTextViewValue(parent, id, value, appendWith, null);
    }

    public static void setQuestionAttemptedBar(AbstractTestAnalytics analytics, View parentView,
            int idCorrect, int idInCorrect, int idLeft) {

        float correctWeigth = analytics.qusCount < 1 ? 0 : analytics.correct * 100
                / analytics.qusCount;
        float incorrectWeigth = analytics.qusCount < 1 ? 0
                : (analytics.attempted - analytics.correct) * 100 / analytics.qusCount;

        float leftWeigth = 100 - (correctWeigth + incorrectWeigth);

        View correctView = parentView.findViewById(idCorrect);
        LinearLayout.LayoutParams params = (LayoutParams) correctView.getLayoutParams();
        params.weight = correctWeigth / 100;
        correctView.setLayoutParams(params);

        View incorrectView = parentView.findViewById(idInCorrect);
        params = (LayoutParams) incorrectView.getLayoutParams();
        params.weight = incorrectWeigth / 100;
        incorrectView.setLayoutParams(params);

        View leftView = parentView.findViewById(idLeft);
        params = (LayoutParams) leftView.getLayoutParams();
        params.weight = leftWeigth / 100;
        leftView.setLayoutParams(params);

        Log.d(TAG, "attempted : " + analytics.attempted + ", Correct:" + analytics.correct
                + ", total: " + analytics.qusCount);
    }

    public static void setQuestionAttemptedStatusCount(AbstractTestAnalytics analytics,
            View parentView, int idCorrect, int idInCorrect, int idLeft) {

        ViewUtils.setTextViewValue(parentView, idCorrect, analytics.correct);
        ViewUtils
                .setTextViewValue(parentView, idInCorrect, analytics.attempted - analytics.correct);
        ViewUtils.setTextViewValue(parentView, idLeft, analytics.qusCount - analytics.attempted);
    }

    public static void setProgrammeProgressView(View parent,
            Map<String, Integer> programmeEntityCountMap,
            Map<String, Integer> programmeCompletedEntityCountMap) {

        // int total = 0;
        // int completed = 0;
        // for (Entry<String, Integer> entry : programmeEntityCountMap.entrySet()) {
        // total += entry.getValue();
        // }
        // for (Entry<String, Integer> entry : programmeCompletedEntityCountMap.entrySet()) {
        // completed += entry.getValue();
        // }
        // int completedPercent = total < 1 ? 0 : (completed * 100) / total;
        // View statusBar = parent.findViewById(R.id.prog_status_bar);
        // View completedBar = parent.findViewById(R.id.prog_status_bar_completed);
        // int width = (statusBar.getLayoutParams().width * completedPercent / 100);
        //
        // completedBar.getLayoutParams().width = width;
        //
        // TextView completedValue = (TextView) parent
        // .findViewById(R.id.prog_status_bar_completed_value);
        // completedValue.setText(completedPercent
        // + parent.getResources().getString(R.string.percentage));
        // completedValue.setPadding(width, 0, 0, 0);
    }

    @SuppressLint({ "JavascriptInterface", "SetJavaScriptEnabled" })
    public static SingleQuestionJSInterface setUpQuestionWebView(WebView mWebView,
            WebViewClient callback) {

        Log.d(TAG, "setting up question webview");
        mWebView.setWebChromeClient(webChromeClientInstance());
        if (callback != null) {
            mWebView.setWebViewClient(callback);
        }
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl("file:///android_asset/html/single_question_view.html");
        SingleQuestionJSInterface jsInterface = new SingleQuestionJSInterface(mWebView);
        mWebView.addJavascriptInterface(jsInterface, "qus");
        return jsInterface;
    }

    class Callback extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            return (true);

        }

        @Override
        public void onPageFinished(WebView view, String url) {

            super.onPageFinished(view, url);
        }

    }

    public static WebChromeClient webChromeClientInstance() {

        return (new WebChromeClient() {

            @Override
            public boolean onConsoleMessage(ConsoleMessage cm) {

                Log.d("ShowNote",
                        cm.message() + " -- From line " + cm.lineNumber() + " of " + cm.sourceId());
                return true;
            }
        });
    }

    public static PopupWindow getBlackerPopupWindow(Context ctx) {

        View pageBlacker = ((LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.page_blacker, null);
        PopupWindow pageBlackerPopup = new PopupWindow(pageBlacker,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT, true);
        pageBlackerPopup.showAtLocation(pageBlacker, Gravity.CENTER, 0, 0);
        return pageBlackerPopup;
    }

    public static PopupWindow getPopupWindow(Context ctx, int layoutResourceId) {

        View popupLayout = ((LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(layoutResourceId, null);

        int widthPercent = 55;
        if (ctx.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            widthPercent = 80;
        }

        int popupWindowWidth = ((ViewUtils.getMeasuredWidthInPx(ctx) * widthPercent) / 100);
        PopupWindow popup = new PopupWindow(popupLayout, popupWindowWidth,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT, true);

        popup.setFocusable(true);
        popup.setOutsideTouchable(true);
        popup.setBackgroundDrawable(ctx.getResources().getDrawable(R.color.darkergrey));

        popup.showAtLocation(popupLayout, Gravity.CENTER, 0, 0);
        return popup;
    }

    // this is the dimension for 80% in portrait and 55% landscape and used at many places so making
    // the common method for this
    public static int getOrientationSpecificWidth(Context ctx) {

        return getOrientationSpecificWidth(ctx, 80, 55);
    }

    public static int getOrientationSpecificWidth(Context ctx, int portraitPercentage,
            int landPercentage) {

        int widthPercent = landPercentage;
        if (ctx.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            widthPercent = portraitPercentage;
        }

        return ((ViewUtils.getMeasuredWidthInPx(ctx) * widthPercent) / 100);
    }

    @SuppressWarnings("deprecation")
    public static int getMeasuredWidthInPx(Context ctx) {

        int measuredwidth = 0;
        Point size = new Point();
        WindowManager w = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            w.getDefaultDisplay().getSize(size);
            measuredwidth = size.x;
        } else {
            Display d = w.getDefaultDisplay();
            measuredwidth = d.getWidth();
        }
        return measuredwidth;
    }

    @SuppressWarnings("deprecation")
    public static int getMeasuredHeightInPx(Context ctx) {

        int measuredheight = 0;
        Point size = new Point();
        WindowManager w = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            w.getDefaultDisplay().getSize(size);
            measuredheight = size.y;
        } else {
            Display d = w.getDefaultDisplay();
            measuredheight = d.getHeight();
        }
        return measuredheight;
    }
}
