package com.nhance.android.adapters.analytics;

import java.text.DecimalFormat;
import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nhance.android.db.datamanagers.AnalyticsDataManager;
import com.nhance.android.db.models.analytics.TestAvg;
import com.nhance.android.db.models.analytics.TestBoardAnalytics;
import com.nhance.android.db.models.analytics.TestCourseLevelAnalytics;
import com.nhance.android.enums.QuestionLevel;
import com.nhance.android.managers.LocalManager;
import com.nhance.android.utils.ViewUtils;
import com.nhance.android.utils.FontUtils.FontTypes;
import com.nhance.android.utils.analytics.AnalyticsUtil;
import com.nhance.android.R;

public class OverallCourseAnalyticsContentListAdapter extends ArrayAdapter<TestBoardAnalytics> {

    private static final String            TAG         = "OverallCourseAnalyticsContentAdapter";
    private int                            courseHeads = 3;
    private List<TestCourseLevelAnalytics> levelAnalytics;
    private List<TestBoardAnalytics>       topics;
    private TestBoardAnalytics             courseAnalytics;
    private String userId;
    private int orgKeyId;
    private AnalyticsDataManager ad;
    private DecimalFormat decimalFormat = new DecimalFormat("#.##");

    public OverallCourseAnalyticsContentListAdapter(Context context, int textViewResourceId,
                                                    TestBoardAnalytics courseAnalytics, AnalyticsDataManager ad, int orgKeyId, String userId, List<TestBoardAnalytics> topics,
                                                    List<TestCourseLevelAnalytics> levels, int courseHeads) {

        super(context, textViewResourceId, topics);
        this.levelAnalytics = levels;
        this.topics = topics;
        this.courseHeads = courseHeads;
        this.courseAnalytics = courseAnalytics;
        this.ad = ad;
        this.orgKeyId = orgKeyId;
        this.userId = userId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(getLayout(position), parent, false);
            if (courseHeads > 0 && position == 2) {
                populateQuestionLevelAnalyticsView(view, position);
            }
        }
        populateView(view, position, parent);
        return view;
    }

    @Override
    public int getCount() {

        return courseHeads + (topics == null ? 0 : topics.size());
    }

    @Override
    public int getItemViewType(int position) {

        int viewType = 0;
        if (topics == null) {
            viewType = position;
        } else {
            viewType = position < courseHeads ? position : (position == courseHeads ? courseHeads
                    : courseHeads + 1);
        }
        return viewType;
    }

    @Override
    public int getViewTypeCount() {

        return topics == null ? courseHeads : courseHeads + 2;
    }

    private int getLayout(int position) {

        int layout = R.layout.list_item_view_overall_course_analytics_topic;
        if (position >= courseHeads) {
            return layout;
        }

        switch (position) {
            case 0:
                layout = R.layout.list_item_view_overall_analytics_course;
                break;
            case 1:
                layout = R.layout.list_item_view_overall_course_analytics_stats;
                break;
            case 2:
                layout = R.layout.list_item_view_overall_course_analytics_q_level_container;
                break;
            default:
                break;
        }
        return layout;
    }

    private void populateView(View view, int position, ViewGroup parent) {

        if (position >= courseHeads) {

            TextView topicNameView = (TextView) view.findViewById(R.id.topic_name);
            TextView topicStrengthView = (TextView) view.findViewById(R.id.topic_strength);

            if (position == courseHeads) {
                // populate topic headers
                view.setTag(null);
                view.setPadding(
                        view.getPaddingLeft(),
                        (int) getContext().getResources().getDimension(
                                R.dimen.overall_course_analytics_topic_name_header_padding_top),
                        view.getPaddingRight(), view.getPaddingBottom());
                view.findViewById(R.id.container).setBackground(
                        getContext().getResources().getDrawable(R.drawable.rounded_corner2));

                topicNameView.setText(R.string.topic);
                topicNameView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                topicNameView
                        .setTextColor(getContext().getResources().getColor(R.color.darkergrey));
                topicNameView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getContext().getResources()
                        .getDimension(R.dimen.overall_course_analytics_topic_name_heade_text_size));

                topicStrengthView.setText(R.string.strength);
                topicStrengthView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                topicStrengthView.setTextColor(getContext().getResources().getColor(
                        R.color.darkergrey));
                topicStrengthView.setTextSize(
                        TypedValue.COMPLEX_UNIT_PX,
                        getContext().getResources().getDimension(
                                R.dimen.overall_course_analytics_topic_name_heade_text_size));
            } else {

                // populate topic name and strength
                view.findViewById(R.id.container).setBackground(
                        getContext().getResources().getDrawable(R.drawable.rounded_corner2));

                TestBoardAnalytics item = topics.get(position - courseHeads);
                view.setTag(item);

                float percentage = AnalyticsUtil.getPercentage(item.attempted, item.correct);

                int color = AnalyticsUtil.getStrengthColorCode(percentage);

                topicNameView.setText(item.name);
                topicNameView.setPadding(10,10,10,10);
                topicNameView.setTextColor(getContext().getResources().getColor(color));

                topicStrengthView.setText(AnalyticsUtil.getStrengthDisplayStringCode(percentage));
                topicStrengthView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                topicStrengthView.setTextColor(getContext().getResources().getColor(color));
            }
            return;
        }

        switch (position) {
            case 0:
                // populate course analytics view
                populateCourseAnalyticsView(view, position, parent);
                break;
            case 1:
                // populate course accuracy and avgSpeed view
                populateCourseAnalyticsAccuracyAndSpeedView(view, position, parent);
                break;
            case 2:
                // level wise analytics already populated when 1st time the view is created in
                // onCreateView method
                break;
        }
    }

    private void populateCourseAnalyticsView(View view, int position, ViewGroup parent) {

        view.setTag(null);
        view.findViewById(R.id.navigation_icon_next).setVisibility(View.INVISIBLE);

        ViewUtils.setTextViewValue(view, R.id.course_name, courseAnalytics.name, null,
                FontTypes.ROBOTO_LIGHT);

        float percentage = getAvgPercentage(ad, courseAnalytics.name);

        View percentageIdicatorView = view.findViewById(R.id.course_percentage_marks_indicator);

        int newWidth = (int) ((percentage * parent.getRight()) / 100);
        percentageIdicatorView.getLayoutParams().width = newWidth;

        Log.d(TAG, "new layout width : " + newWidth + " position : " + position + ", percentage:"
                + percentage);

        percentageIdicatorView.setBackgroundColor(getContext().getResources().getColor(
                AnalyticsUtil.getPercentageColorCode(percentage)));

        ViewUtils.setTextViewValue(view, R.id.agv_percentage, AnalyticsUtil
                        .getStringDisplayValue(percentage),
                getContext().getResources().getString(R.string.percentage_indicator));
    }

    private float getAvgPercentage(AnalyticsDataManager ad, String couseName) {
        List<TestAvg> testAvgMark = ad.getTestAvgPercentage(orgKeyId, userId, couseName);
        float totalPercentage = 0;
        Log.e("HHHHHHHHH", "" + testAvgMark.size());
        for (TestAvg tAvgMark : testAvgMark) {


            if (Integer.parseInt(tAvgMark.totalMarks) != 0 && Integer.parseInt(tAvgMark.attempted) != 0) {
                float Percentage = AnalyticsUtil.getPercentage(Integer.parseInt(tAvgMark.totalMarks), Integer.parseInt(tAvgMark.score));
                Log.e("HHHHHHHHH", "" + Percentage);
                totalPercentage = totalPercentage + Percentage;
            }
        }
        Log.e("HHHHHHHHH", "" + totalPercentage);
        totalPercentage = totalPercentage / testAvgMark.size();
        return Float.valueOf(decimalFormat.format(totalPercentage));
    }

    private void populateCourseAnalyticsAccuracyAndSpeedView(View view, int position,
                                                             ViewGroup parent) {

        view.setTag(null);
        ViewUtils.setTextViewValue(view, R.id.accuracy_text_value, AnalyticsUtil
                        .getStringDisplayValue(AnalyticsUtil.getPercentage(courseAnalytics.attempted,
                                courseAnalytics.correct)),
                getContext().getResources().getString(R.string.percentage_indicator));

        float durationPerQus = courseAnalytics.timeTaken == 0 ? 0 : courseAnalytics.timeTaken
                / courseAnalytics.qusCount;
        ViewUtils.setTextViewValue(view, R.id.avg_speed_text_value,
                LocalManager.getDurationMinString(durationPerQus));

    }

    private void populateQuestionLevelAnalyticsView(View view, int position) {

        view.setTag(null);
        LinearLayout container = (LinearLayout) view;

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_item_view_overall_course_analytics_q_level,
                container, false);
        rowView.findViewById(R.id.level_bar).setVisibility(View.INVISIBLE);
        rowView.findViewById(R.id.level_name).setVisibility(View.GONE);
        rowView.findViewById(R.id.avg_speed_value).setVisibility(View.GONE);

        TextView qLevelText = (TextView) rowView.findViewById(R.id.level_stats);
        qLevelText.setText(R.string.question_levels);
        qLevelText.setTextColor(getContext().getResources().getColor(R.color.darkergrey));
        qLevelText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getContext().getResources()
                .getDimension(R.dimen.overall_course_analytics_topic_name_heade_text_size));
        qLevelText.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        TextView accuracyValue = (TextView) rowView.findViewById(R.id.level_accuracy_value);
        accuracyValue.setText(R.string.accuracy);
        accuracyValue.setTextSize(TypedValue.COMPLEX_UNIT_PX, getContext().getResources()
                .getDimension(R.dimen.overall_course_analytics_topic_name_heade_text_size));
        accuracyValue.setTextColor(getContext().getResources().getColor(R.color.darkergrey));
        accuracyValue.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        TextView qSpeedText = (TextView) rowView.findViewById(R.id.avg_speed_text_indicator);
        qSpeedText.setText(R.string.speed);
        qSpeedText.setTextColor(getContext().getResources().getColor(R.color.darkergrey));
        qSpeedText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getContext().getResources()
                .getDimension(R.dimen.overall_course_analytics_topic_name_heade_text_size));
        qSpeedText.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        container.addView(rowView);

        for (TestCourseLevelAnalytics qLevelAnalytics : levelAnalytics) {
            rowView = inflater.inflate(R.layout.list_item_view_overall_course_analytics_q_level,
                    container, false);
            QuestionLevel qLevel = QuestionLevel.valueOfKey(qLevelAnalytics.level);
            rowView.findViewById(R.id.level_bar).setBackgroundColor(
                    getContext().getResources().getColor(qLevel.getColorIndicator()));


            ViewUtils.setTextViewValue(rowView, R.id.level_name, qLevel.name());
            ViewUtils.setTextViewValue(rowView, R.id.level_stats, "(" + qLevelAnalytics.correct
                    + "/" + qLevelAnalytics.attempted + ")");

            ViewUtils.setTextViewValue(rowView, R.id.level_accuracy_value, AnalyticsUtil
                            .getStringDisplayValue(AnalyticsUtil.getPercentage(qLevelAnalytics.attempted,
                                    qLevelAnalytics.correct)),
                    getContext().getString(R.string.percentage_indicator));

            float durationPerQus = qLevelAnalytics.qusCount == 0 ? 0 : qLevelAnalytics.timeTaken
                    / qLevelAnalytics.qusCount;
            ViewUtils.setTextViewValue(rowView, R.id.avg_speed_value,
                    LocalManager.getDurationMinString(durationPerQus));

            container.addView(rowView);
        }
    }
}
