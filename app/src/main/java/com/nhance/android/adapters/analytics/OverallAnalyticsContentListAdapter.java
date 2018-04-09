package com.nhance.android.adapters.analytics;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nhance.android.R;
import com.nhance.android.db.datamanagers.AnalyticsDataManager;
import com.nhance.android.db.models.analytics.TestAvg;
import com.nhance.android.db.models.analytics.TestBoardAnalytics;
import com.nhance.android.pojos.TestAnalyticsMiniPojo;
import com.nhance.android.utils.FontUtils.FontTypes;
import com.nhance.android.utils.ViewUtils;
import com.nhance.android.utils.analytics.AnalyticsUtil;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


@SuppressLint("SimpleDateFormat")
public class
OverallAnalyticsContentListAdapter extends ArrayAdapter<TestBoardAnalytics> {

    private static final String TAG = "OverallAnalyticsContentListAdapter";

    private List<TestAnalyticsMiniPojo> testItems;
    private int courseItemCount;
    private String userId;
    private int orgKeyId;
    private AnalyticsDataManager ad;
    private DecimalFormat decimalFormat = new DecimalFormat("#.##");

    public OverallAnalyticsContentListAdapter(Context context, int textViewResourceId,
                                              List<TestBoardAnalytics> courseItemList, AnalyticsDataManager ad, int orgKeyId, String userId, List<TestAnalyticsMiniPojo> testItems) {

        super(context, textViewResourceId, courseItemList);
        this.testItems = testItems;
        this.ad = ad;
        this.orgKeyId = orgKeyId;
        this.userId = userId;

        this.courseItemCount = courseItemList == null ? 0 : courseItemList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        int itemViewType = getItemViewType(position);
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            int layout = itemViewType == 0 ? R.layout.list_item_view_overall_analytics_course
                    : R.layout.list_item_view_overall_analytics_test_list;

            view = inflater.inflate(layout, parent, false);
        }

        if (itemViewType == 0) {
            populateCourseAnalyticsItemView(parent, view, position);
        } else {
            populateTestItemView(parent, view, position);
        }
        return view;
    }

    @Override
    public int getCount() {

        return courseItemCount + (testItems == null ? 0 : testItems.size());
    }

    @Override
    public int getItemViewType(int position) {

        return position < courseItemCount ? 0 : 1;
    }

    @Override
    public int getViewTypeCount() {

        return courseItemCount != 0 && testItems != null ? 2 : 1;
    }

    private void populateCourseAnalyticsItemView(ViewGroup parent, View view, int position) {

        TestBoardAnalytics courseAnalytics = getItem(position);
        if (courseAnalytics == null) {
            return;
        }
        view.setTag(courseAnalytics);
        int courseLisLastItemMarginVisibility = position == (courseItemCount - 1) ? View.VISIBLE
                : View.GONE;

        view.findViewById(R.id.course_list_last_item_margin).setVisibility(
                courseLisLastItemMarginVisibility);

        ViewUtils.setTextViewValue(view, R.id.course_name, courseAnalytics.name, null,
                FontTypes.ROBOTO_LIGHT);
        float percentage = getAvgPercentage(ad, courseAnalytics.name);
        ViewUtils.setTextViewValue(view, R.id.agv_percentage, AnalyticsUtil
                        .getStringDisplayValue(percentage),
                getContext().getResources().getString(R.string.percentage_indicator));

        View percentageIdicatorView = view.findViewById(R.id.course_percentage_marks_indicator);
        int newWidth = (int) ((percentage * parent.getRight()) / 100);

        percentageIdicatorView.getLayoutParams().width = newWidth;

        Log.d(TAG, "new layout width : " + newWidth + " position : " + position);

        percentageIdicatorView.setBackgroundColor(getContext().getResources().getColor(
                AnalyticsUtil.getPercentageColorCode(percentage)));

    }

    private float getAvgPercentage(AnalyticsDataManager ad, String couseName) {
        List<TestAvg> testAvgMark = ad.getTestAvgPercentage(orgKeyId, userId, couseName);
        float totalPercentage = 0;
        for (TestAvg tAvgMark : testAvgMark) {
            if (Integer.parseInt(tAvgMark.totalMarks) != 0 && Integer.parseInt(tAvgMark.attempted) != 0) {
                float Percentage = AnalyticsUtil.getPercentage(Integer.parseInt(tAvgMark.totalMarks), Integer.parseInt(tAvgMark.score));
                totalPercentage = totalPercentage + Percentage;
            }
        }
        totalPercentage = totalPercentage / testAvgMark.size();
        Log.e("HHHHHHHHH", "" + totalPercentage);
        return Float.valueOf(decimalFormat.format(totalPercentage));


    }

    SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yy");

    private void populateTestItemView(ViewGroup parent, View view, int position) {

        int listPosition = position - courseItemCount;
        int bgColor = listPosition == 0 ? R.color.white : R.color.white;

        view.findViewById(R.id.test_name_container).setBackgroundColor(
                getContext().getResources().getColor(bgColor));

        TextView nameView = (TextView) view.findViewById(R.id.test_name);
        TextView markScored = (TextView) view.findViewById(R.id.test_marks_scored);
        TextView markTotal = (TextView) view.findViewById(R.id.test_marks_total);
        TextView testTakneOnText = (TextView) view.findViewById(R.id.test_taken_on_text);
        testTakneOnText.setVisibility(View.VISIBLE);

        View dividerLine = view.findViewById(R.id.divider_line);
        View dividerGap = view.findViewById(R.id.divider_gap);
        dividerLine.setVisibility(View.VISIBLE);
        dividerGap.setVisibility(View.VISIBLE);

        TextView testTakneDate = (TextView) view.findViewById(R.id.test_taken_date);
        testTakneDate.setVisibility(View.VISIBLE);
        if (listPosition == 0) {
            // populate testName and marks header
            markScored.setVisibility(View.GONE);
            testTakneOnText.setVisibility(View.GONE);
            testTakneDate.setVisibility(View.GONE);

            dividerLine.setVisibility(View.GONE);
            dividerGap.setVisibility(View.GONE);

            nameView.setText(R.string.test_name);
            nameView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            nameView.setTextColor(getContext().getResources().getColor(R.color.red));

            markTotal.setText(R.string.marks);
            markTotal.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            markTotal.setTextColor(getContext().getResources().getColor(R.color.red));
            view.setTag(null);
            return;
        }

        TestAnalyticsMiniPojo testInfo = testItems.get(listPosition);
        if (testInfo == null) {
            return;
        }

        view.setTag(testInfo);

        markScored.setVisibility(View.VISIBLE);
        markScored.setText(String.valueOf(testInfo.score));

        nameView.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
        nameView.setText(testInfo.name);

        markTotal.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
        markTotal.setText(getContext().getString(R.string.seperator) + testInfo.totalMarks);

        testTakneDate.setText(dateFormatter.format(new Date(testInfo.timeAttempted)));

    }
}
