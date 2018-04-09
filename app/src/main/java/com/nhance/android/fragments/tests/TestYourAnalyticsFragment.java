package com.nhance.android.fragments.tests;

import java.util.ArrayList;
import java.util.List;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;

import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.customviews.PieChartView;
import com.nhance.android.db.datamanagers.AnalyticsDataManager;
import com.nhance.android.db.models.analytics.TestAnalytics;
import com.nhance.android.db.models.analytics.TestBoardAnalytics;
import com.nhance.android.enums.BoardType;
import com.nhance.android.fragments.AbstractVedantuFragment;
import com.nhance.android.managers.LocalManager;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.utils.GoogleAnalyticsUtils;
import com.nhance.android.utils.ViewUtils;
import com.nhance.android.utils.FontUtils.FontTypes;
import com.nhance.android.utils.analytics.AnalyticsUtil;
import com.nhance.android.R;

public class TestYourAnalyticsFragment extends AbstractVedantuFragment implements OnClickListener {

    private static final String           TAG     = "TestYourAnalyticsFragment";
    int                                   mActionBarHeight;
    private AnalyticsDataManager          analyticsDataManager;
    private SessionManager                session;
    private TestAnalytics                 analytics;
    private ArrayList<TestBoardAnalytics> coursesAnalytics;
    Handler                               handler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        GoogleAnalyticsUtils.setScreenName(ConstantGlobal.GA_SCREEN_TEST_USER_ANALYTICS);
        Bundle bundle = getArguments();
        session = SessionManager.getInstance(getActivity());
        analyticsDataManager = new AnalyticsDataManager(getActivity());
        analytics = analyticsDataManager.getTestAnalytics(
                session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID),
                session.getSessionStringValue(ConstantGlobal.USER_ID),
                bundle.getString(ConstantGlobal.ENTITY_ID),
                bundle.getString(ConstantGlobal.ENTITY_TYPE));
        if (analytics == null) {
            return;
        }
        coursesAnalytics = analyticsDataManager.getTestBoardAnalytics(analytics.orgKeyId,
                analytics.userId, analytics.entityId, analytics.entityType, null, BoardType.COURSE);
    }

    @Override
    public View
            onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_test_your_analytics, container, false);
        mActionBarHeight = getActionBarHeight();

        if (analytics == null) {
            return null;
        }
        addTestOverAllAnalytics(analytics, view);

        addTestCourseAnalytics(inflater, analytics, (ScrollView) view);

        Log.d(TAG, "onCreateView view is called ");
        return view;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);
        Log.i(TAG, "onConfigurationChanged called");
        destoryAndCreateFragmentView();
    }

    // @Override
    // public void onSaveInstanceState(Bundle outState) {
    //
    // super.onSaveInstanceState(outState);
    // if (outState != null) {
    // outState.putSerializable("testAnalytics", analytics);
    // outState.putSerializable("courseAnalytics", coursesAnalytics);
    // }
    // }

    private void addTestOverAllAnalytics(final TestAnalytics analytics, final View parent) {

        float scoredPercentage = analytics.totalMarks == 0 ? 0 : analytics.score * 100
                / analytics.totalMarks;
        TextView scoreView = (TextView) parent.findViewById(R.id.test_marks_score);
        if (analytics.score <= 0) {
            scoreView.setTextColor(getResources().getColor(R.color.red));
        }

        ViewUtils.setTextViewValue(scoreView, String.valueOf(analytics.score), null,
                FontTypes.ROBOTO_LIGHT);

        ViewUtils.setTextViewValue(parent, R.id.test_marks_total,
                String.valueOf(analytics.totalMarks), null,
                getResources().getString(R.string.seperator), FontTypes.ROBOTO_LIGHT);
        int percentageColorCode = AnalyticsUtil.getPercentageColorCode(scoredPercentage);

        PieChartView piChartView = (PieChartView) parent
                .findViewById(R.id.test_course_score_chart_view);
        piChartView.setColorAndValues(new int[] { getResources().getColor(percentageColorCode) },
                new float[] { scoredPercentage });

        ViewUtils.setTextViewValue(parent, R.id.test_score_percentage_view,
                AnalyticsUtil.getStringDisplayValue(scoredPercentage),
                getResources().getString(R.string.percentage_indicator));

        TextView timeTaken = (TextView) parent.findViewById(R.id.test_time_taken);
        String durationString = LocalManager.secondsToString((int)analytics.timeTaken);
        timeTaken.setText(durationString);

        // adding user test SWOT analytics

        final View swotAnalyticsContainer = parent.findViewById(R.id.test_swot_analytics_container);

        int correct = analytics.correct;
        int incorrect = analytics.attempted - analytics.correct;
        int left = analytics.qusCount - analytics.attempted;
        addQuestionAttemptCount(
                swotAnalyticsContainer.findViewById(R.id.test_correct_qus_count_container), correct);
        addQuestionAttemptCount(
                swotAnalyticsContainer.findViewById(R.id.test_incorrect_qus_count_container),
                incorrect);
        addQuestionAttemptCount(
                swotAnalyticsContainer.findViewById(R.id.test_left_qus_count_container), left);

        PieChartView attemptDonutChart = (PieChartView) swotAnalyticsContainer
                .findViewById(R.id.test_question_donut_view);
        attemptDonutChart.setColorAndValues(new int[] { getResources().getColor(R.color.green),
                getResources().getColor(R.color.red), getResources().getColor(R.color.lightgrey) },
                new float[] { correct, incorrect, left });
        ViewUtils.setTextViewValue(swotAnalyticsContainer, R.id.test_analytics_qus_attempt_count,
                analytics.qusCount);
        ViewUtils.setAcurracyView(analytics, swotAnalyticsContainer);
    }

    private void addTestCourseAnalytics(LayoutInflater inflater, TestAnalytics analytics,
            ScrollView parentView) {

        LinearLayout courseAnalyticsContainer = (LinearLayout) parentView
                .findViewById(R.id.test_course_analytics_list);

        for (TestBoardAnalytics courseAnalytics : coursesAnalytics) {
            addCourseAnalyticsView(inflater, parentView, courseAnalyticsContainer, courseAnalytics);
        }

    }

    private void addQuestionAttemptCount(View parent, int count) {

        ViewUtils.setTextViewValue(parent, R.id.test_status_qus_count, String.valueOf(count));
    }

    View activeCoursesAnalytics;

    private void addCourseAnalyticsView(final LayoutInflater inflater,
            final ScrollView rootScrollView, final ViewGroup courseAnalyticsContainer,
            final TestBoardAnalytics courseAnalytics) {

        final View view = inflater.inflate(R.layout.list_item_view_test_course_analytics,
                courseAnalyticsContainer, false);

        final ImageView navButton = (ImageView) view
                .findViewById(R.id.test_course_navigation_button);

        view.findViewById(R.id.test_course_analytics_stats_container).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        View timeTakenViewContainer = view
                                .findViewById(R.id.test_course_time_taken_container);
                        View percentageViewContainer = view
                                .findViewById(R.id.test_course_scored_percentage_container);

                        View courseAnalyticsDetailStats = view
                                .findViewById(R.id.test_analytics_course_stats_detail_container);

                        int visibility = courseAnalyticsDetailStats.getVisibility();
                        if (visibility == View.GONE) {

                            hideOpenedCourseAnalyticsView();

                            timeTakenViewContainer.setVisibility(View.VISIBLE);
                            percentageViewContainer.setVisibility(View.GONE);

                            ViewGroup topicAnalyticsContainer = (ViewGroup) view
                                    .findViewById(R.id.test_course_topic_analytics_table);

                            if (topicAnalyticsContainer.getChildCount() == 2) {
                                addTopicAnalyticsView(inflater, topicAnalyticsContainer,
                                        courseAnalytics);
                            }

                            activeCoursesAnalytics = view;
                            visibility = View.VISIBLE;
                            navButton.setImageResource(R.drawable.icon_arrow_up);
                            scrollChildToTop(rootScrollView, courseAnalyticsDetailStats);
                        } else {
                            visibility = View.GONE;
                            navButton.setImageResource(R.drawable.icon_arrow_down);
                            timeTakenViewContainer.setVisibility(View.GONE);
                            percentageViewContainer.setVisibility(View.VISIBLE);
                        }
                        courseAnalyticsDetailStats.setVisibility(visibility);
                    }
                });

        ViewUtils.setTextViewValue(view, R.id.test_course_name, courseAnalytics.name);

        View courseStatsView = view.findViewById(R.id.test_course_stats);
        ViewUtils.setTextViewValue(courseStatsView, R.id.test_course_scored_marks,
                courseAnalytics.score);

        int scoredPercentage = courseAnalytics.totalMarks == 0 ? 0 : (courseAnalytics.score * 100)
                / courseAnalytics.totalMarks;

        ViewUtils.setTextViewValue(courseStatsView, R.id.test_course_scored_percentage,
                scoredPercentage, getString(R.string.percentage_indicator));

        ViewUtils.setTextViewValue(courseStatsView, R.id.test_course_time_taken,
                LocalManager.secondsToString((int)courseAnalytics.timeTaken));

        // test_analytics_course_stats_detail_container
        View courseAnalyticsDetailStats = view
                .findViewById(R.id.test_analytics_course_stats_detail_container);

        PieChartView courseScoreChart = (PieChartView) courseAnalyticsDetailStats
                .findViewById(R.id.test_score_chart_view);

        courseScoreChart.setColorAndValues(
                new int[] {
                        getResources().getColor(
                                AnalyticsUtil.getPercentageColorCode(scoredPercentage)),
                        getResources().getColor(R.color.white) }, new float[] { scoredPercentage,
                        100 - scoredPercentage });
        ViewUtils.setTextViewValue(courseAnalyticsDetailStats, R.id.test_course_scored_percentage,
                scoredPercentage, getString(R.string.percentage_indicator));

        ViewUtils.setAcurracyView(courseAnalytics, courseAnalyticsDetailStats);

        courseAnalyticsContainer.addView(view);
    }

    private void addTopicAnalyticsView(LayoutInflater inflater, ViewGroup parent,
            final TestBoardAnalytics courseAnalytics) {

        List<TestBoardAnalytics> topicsAnalytics = analyticsDataManager.getTestBoardAnalytics(
                session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID),
                session.getSessionStringValue(ConstantGlobal.USER_ID), courseAnalytics.entityId,
                courseAnalytics.entityType, courseAnalytics.id, BoardType.TOPIC);

        for (TestBoardAnalytics topicAnalytics : topicsAnalytics) {
            View topicRow = inflater.inflate(R.layout.list_item_view_test_course_topic_analytics,
                    parent, false);
            ViewUtils.setTextViewValue(topicRow, R.id.test_course_topic_name, topicAnalytics.name);
            ViewUtils.setTextViewValue(topicRow, R.id.test_course_topic_marks_scored,
                    topicAnalytics.score);
            ViewUtils.setTextViewValue(topicRow, R.id.test_course_topic_attempted_question_count,
                    topicAnalytics.attempted);
            topicRow.setTag(topicAnalytics);
            topicRow.setOnClickListener(this);
            parent.addView(topicRow);
        }
    }

    private void hideOpenedCourseAnalyticsView() {

        if (activeCoursesAnalytics == null) {
            return;
        }

        final ImageView activeNavButton = (ImageView) activeCoursesAnalytics
                .findViewById(R.id.test_course_navigation_button);

        activeNavButton.setImageResource(R.drawable.icon_arrow_down);

        activeCoursesAnalytics.findViewById(R.id.test_analytics_course_stats_detail_container)
                .setVisibility(View.GONE);

        View timeTakenViewContainer = activeCoursesAnalytics
                .findViewById(R.id.test_course_time_taken_container);
        View percentageViewContainer = activeCoursesAnalytics
                .findViewById(R.id.test_course_scored_percentage_container);

        timeTakenViewContainer.setVisibility(View.GONE);
        percentageViewContainer.setVisibility(View.VISIBLE);

    }

    private void scrollChildToTop(final ScrollView scrollView, final View childView) {

        new Thread(new Runnable() {

            @Override
            public void run() {

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {}
                handler.post(new Runnable() {

                    @Override
                    public void run() {

                        translateView(scrollView, childView);
                    }
                });
            }
        }).start();
    }

    public void translateView(ScrollView scrollView, View view) {

        int[] xy = new int[2];
        // view that will be kept at top of the screen
        view.getLocationOnScreen(xy);
        int scrollY = xy[1];
        // screenHeight =ScreenHeight of the phone
        scrollView.getLocationOnScreen(xy);
        // 50 is hard-coding by @Shankar as the height was not calculating correctly
        scrollY = scrollY - xy[1] - mActionBarHeight - 55;
        Log.d("SCROLL", "scrolling by : " + scrollY);
        scrollView.smoothScrollBy(0, scrollY);

    }

    private int getActionBarHeight() {

        float actionBarHeight = 0;
        TypedValue tv = new TypedValue();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources()
                        .getDisplayMetrics());
        } /* TODO for testing commented this line Satya
        else if (getActivity().getTheme().resolveAttribute(
                com.actionbarsherlock.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources()
                    .getDisplayMetrics());
        }*/
        return (int) actionBarHeight;
    }

    /**
     * this method will be used to setOnClick event on topic row, as the no of topics can be more
     * 
     * @param v
     */
    @Override
    public void onClick(View v) {

        TestBoardAnalytics topicAnalytics = (TestBoardAnalytics) v.getTag();
        if (topicAnalytics == null) {
            return;
        }

        final PopupWindow pageBlackerPopup = ViewUtils.getBlackerPopupWindow(getActivity());
        final PopupWindow popup = ViewUtils.getPopupWindow(getActivity(),
                R.layout.popup_test_topic_analytics);
        final View popupLayout = popup.getContentView();

        ViewUtils.setTextViewValue(popupLayout, R.id.popup_title, topicAnalytics.name);

        View containerView = popupLayout
                .findViewById(R.id.test_topic_analytics_info_popup_container);

        ViewUtils.setTextViewValue(containerView, R.id.correct_count, topicAnalytics.correct);
        ViewUtils.setTextViewValue(containerView, R.id.incorrect_count, topicAnalytics.attempted
                - topicAnalytics.correct);
        ViewUtils.setTextViewValue(containerView, R.id.left_count, topicAnalytics.qusCount
                - topicAnalytics.attempted);
        popup.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {

                if (pageBlackerPopup.isShowing()) {
                    pageBlackerPopup.dismiss();
                }
            }
        });

        popupLayout.findViewById(R.id.close_popup).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                popup.dismiss();
            }
        });
    }

    @Override
    public void onResume() {

        super.onResume();
        GoogleAnalyticsUtils.sendPageViewDataToGA();
    }
}
