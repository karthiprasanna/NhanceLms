package com.nhance.android.fragments.tests;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.nhance.android.async.tasks.CachedWebDataFetcherTask;
import com.nhance.android.async.tasks.ICachedTaskProcessor;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.customviews.BarGraphBean;
import com.nhance.android.customviews.BarGraphView;
import com.nhance.android.customviews.PieChartView;
import com.nhance.android.fragments.AbstractVedantuFragment;
import com.nhance.android.managers.LocalManager;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.pojos.UserInfoRes;
import com.nhance.android.pojos.onlinedata.tests.EntityMarkDistribution;
import com.nhance.android.pojos.onlinedata.tests.EntityRankList;
import com.nhance.android.pojos.onlinedata.tests.MarkDistribution;
import com.nhance.android.pojos.onlinedata.tests.TestInfo;
import com.nhance.android.pojos.tests.EntityMeasures;
import com.nhance.android.pojos.tests.UserEntityRank;
import com.nhance.android.utils.GoogleAnalyticsUtils;
import com.nhance.android.utils.ViewUtils;
import com.nhance.android.utils.FontUtils.FontTypes;
import com.nhance.android.utils.analytics.AnalyticsUtil;
import com.nhance.android.R;

public class TestStudentAnalaticsFragment extends AbstractVedantuFragment implements
        OnClickListener {

    SessionManager         session;
    final int              BUCKET_COUNT            = 5;
    final private String   TAG                     = "TestStudentAnalaticsFragment";
    int                    mActionBarHeight;
    private String         entityId;
    private String         entityType;
    private TestInfo       testInfo;
    private View           parentView;
    private BarGraphBean[] marksDistributionValues = null;
    private boolean        showCache               = true;
    private boolean        isDestroyed             = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GoogleAnalyticsUtils.setScreenName(ConstantGlobal.GA_SCREEN_TEST_STUDENT_ANALYTICS);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        parentView = inflater.inflate(R.layout.fragment_test_student_analytics, container, false);
        // mActionBarHeight = getActionBarHeight();
        fillTestContent();
        Log.d(TAG, "onCreateView view is called ");
        return parentView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.test_post_attempt_rank_list_page, menu);
        progressMenuItem = menu.findItem(R.id.action_refresh);
        showRefreshButton();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_refresh) {
            if (!SessionManager.isOnline()) {
                Toast.makeText(getActivity(), R.string.no_internet_msg, Toast.LENGTH_LONG).show();
            } else {
                showCache = false;
                fillTestContent();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void fillTestContent() {

        Bundle bundle = getArguments();
        session = SessionManager.getInstance(getActivity());
        entityId = bundle.getString(ConstantGlobal.ENTITY_ID);
        entityType = bundle.getString(ConstantGlobal.ENTITY_TYPE);
        if (TextUtils.isEmpty(entityId)) {
            return;
        }
        showProgressBar();
        String cacheKey = entityType + "/" + entityId;
        Log.d(TAG, "GET TEST INFO ========" + entityId);
        CachedWebDataFetcherTask<TestInfo> cachedWebDataFetcherTask = new CachedWebDataFetcherTask<TestInfo>(
                session, null, CachedWebDataFetcherTask.ReqAction.GET_TEST_DETAILS,
                new TestInfoTaskProcessor(), showCache, cacheKey, TestInfo.class);

        cachedWebDataFetcherTask.addExtraParams("id", entityId);
        cachedWebDataFetcherTask.executeTask(false);
    }

    private final class TestInfoTaskProcessor implements ICachedTaskProcessor<TestInfo> {

        @Override
        public void onResultDataFromCache(TestInfo data) {

            if (isDestroyed) {
                return;
            }
            if (data != null) {
                drawTestInfo(data);
            } else {
                Toast.makeText(getActivity().getApplicationContext(),
                        R.string.downloading_test_result_msg, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onTaskPostExecute(boolean success, TestInfo result) {

            if (isDestroyed) {
                return;
            }
            Log.d(TAG, "TEST INFO RESULTS ======== success == " + success + ", RESULT === "
                    + result);
            if (success && result != null) {
                boolean proceed = drawTestInfo(result);
                if (proceed) {
                    getHighestScorer();
                }
            } else {
                onFetchFailure(false);
            }
        }

    }

    private boolean drawTestInfo(TestInfo resp) {

        testInfo = resp;
        long attempts = testInfo.attempts;
        Log.d(TAG,"TEST INFO == "+testInfo+", attempts = "+testInfo.attempts);
        if (attempts <= 0) {
            onFetchFailure(true);
            return false;
        }
        if (m_cObjNhanceBaseActivity != null) {
            m_cObjNhanceBaseActivity.getSupportActionBar().setTitle(testInfo.name);
        }

        float avgScore = testInfo.avgMarks;
        ViewUtils.setTextViewValue(parentView, R.id.test_avg_score_value,
                AnalyticsUtil.getStringDisplayValue(avgScore));

        int totalMarks = testInfo.totalMarks;
        ViewUtils.setTextViewValue(parentView, R.id.test_marks_total,
                AnalyticsUtil.getStringDisplayValue(totalMarks), null,
                getResources().getString(R.string.seperator), FontTypes.ROBOTO_LIGHT);

        float percentageScore = totalMarks == 0 ? 0 : (avgScore * 100 / totalMarks);
        int percentageColorCode = AnalyticsUtil.getPercentageColorCode(percentageScore);
        PieChartView pieChart = (PieChartView) parentView
                .findViewById(R.id.test_avg_score_chart_view);
        pieChart.setColorAndValues(new int[] { getResources().getColor(percentageColorCode) },
                new float[] { percentageScore });
        pieChart.postInvalidate();

        ViewUtils.setTextViewValue(parentView, R.id.test_avg_score_percentage_value,
                AnalyticsUtil.getStringDisplayValue(percentageScore,2),
                getResources().getString(R.string.percentage_indicator));

        long avgTimeTaken = testInfo.avgTimeTaken;
        int durationInSeconds = (int) avgTimeTaken / 1000;
        String durationString = LocalManager.getDurationMinString(durationInSeconds);
        ViewUtils.setTextViewValue(parentView, R.id.test_avg_time_taken_value, durationString,
                null, FontTypes.ROBOTO_LIGHT);
        ViewUtils.setTextViewValue(parentView, R.id.test_text_mins,
                getResources().getString(R.string.mins), null, FontTypes.ROBOTO_LIGHT);

        ViewUtils.setTextViewValue(parentView, R.id.test_attempts_value, String.valueOf(attempts),
                null, FontTypes.ROBOTO_LIGHT);
        return true;
    }

    private void getHighestScorer() {

        showProgressBar();
        String cacheKey = "UR/" + entityType + "/" + entityId;
        CachedWebDataFetcherTask<EntityRankList> cachedWebDataFetcherTask = new CachedWebDataFetcherTask<EntityRankList>(
                session, null, CachedWebDataFetcherTask.ReqAction.GET_ENTITY_LEADER_BOARD,
                new HighestScorerTaskProcessor(), showCache, cacheKey, EntityRankList.class);
        cachedWebDataFetcherTask.addExtraParams("entity.id", entityId);
        cachedWebDataFetcherTask.addExtraParams("entity.type", entityType);
        cachedWebDataFetcherTask.addExtraParams("miniInfo", String.valueOf(true));
        cachedWebDataFetcherTask.executeTask(false);
    }

    private final class HighestScorerTaskProcessor implements ICachedTaskProcessor<EntityRankList> {

        @Override
        public void onResultDataFromCache(EntityRankList data) {

            if (isDestroyed) {
                return;
            }
            if (data != null) {
                drawHighestScorer(data);
            }
        }

        @Override
        public void onTaskPostExecute(boolean success, EntityRankList result) {

            if (isDestroyed) {
                return;
            }
            Log.d(TAG, "HIGHEST SCORER RESULTS ======== " + result);
            if (success && result != null) {
                drawHighestScorer(result);
                getMarksDistributionData();
            } else {
                onFetchFailure(false);
            }
        }
    }

    private void drawHighestScorer(EntityRankList resp) {

        RelativeLayout holder = (RelativeLayout) parentView
                .findViewById(R.id.test_highest_scorer_holder_layout);
        if (resp != null && resp.list.size() > 0) {
            try {
                UserEntityRank highest = resp.list.get(0);
                holder.setVisibility(View.VISIBLE);
                UserInfoRes user = highest.user;

                ImageView userThum = (ImageView) parentView
                        .findViewById(R.id.test_highest_scorer_profile_pic);
                Drawable defaultPic = getResources().getDrawable(R.drawable.default_profile_pic);
                userThum.setImageDrawable(defaultPic);
                if (user != null) {
                    String thumbnail = user.thumbnail;
                    LocalManager.downloadImage(thumbnail, userThum);

                    String firstName = user.firstName;
                    ViewUtils.setTextViewValue(parentView, R.id.test_higest_scorer_first_name,
                            firstName);
                    String lastName = user.lastName;
                    ViewUtils.setTextViewValue(parentView, R.id.test_higest_scorer_last_name,
                            lastName);
                }
                EntityMeasures measures = highest.measures;
                int userScore = measures.score;
                int totalMarks = testInfo.totalMarks;
                float percentageScore = totalMarks == 0 ? 0 : userScore * 100 / totalMarks;
                ViewUtils.setTextViewValue(parentView, R.id.test_highest_score_value,
                        getResources().getString(R.string.seperator), String.valueOf(totalMarks),
                        String.valueOf(userScore), FontTypes.ROBOTO_LIGHT);
                ViewUtils.setTextViewValue(parentView, R.id.test_highest_score_percent_value,
                        String.valueOf(percentageScore),
                        getResources().getString(R.string.percentage_indicator)
                                + getResources().getString(R.string.bracketEnd), getResources()
                                .getString(R.string.bracketStart), FontTypes.ROBOTO_LIGHT);
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
                holder.setVisibility(View.GONE);
            }
        } else {
            holder.setVisibility(View.GONE);
        }
    }

    private void getMarksDistributionData() {

        showProgressBar();
        String cacheKey = "MD/" + entityType + "/" + entityId;
        CachedWebDataFetcherTask<EntityMarkDistribution> cachedWebDataFetcherTask = new CachedWebDataFetcherTask<EntityMarkDistribution>(
                session, null, CachedWebDataFetcherTask.ReqAction.GET_ENTITY_MARK_DISTRIBUTION,
                new MarksDistributionTaskProcessor(), showCache, cacheKey,
                EntityMarkDistribution.class);
        cachedWebDataFetcherTask.addExtraParams("entity.id", entityId);
        cachedWebDataFetcherTask.addExtraParams("entity.type", entityType);
        cachedWebDataFetcherTask.addExtraParams("bucketCount", BUCKET_COUNT);
        cachedWebDataFetcherTask.executeTask(false);
    }

    private final class MarksDistributionTaskProcessor implements
            ICachedTaskProcessor<EntityMarkDistribution> {

        @Override
        public void onResultDataFromCache(EntityMarkDistribution data) {

            if (isDestroyed) {
                return;
            }
            if (data != null) {
                drawMarksDistribution(data);
            }
        }

        @Override
        public void onTaskPostExecute(boolean success, EntityMarkDistribution result) {

            if (isDestroyed) {
                return;
            }
            Log.d(TAG, "MARKS DISTRIBUTION RESULTS ======== " + result);
            if (success && result != null) {
                drawMarksDistribution(result);
                showRefreshButton();
            } else {
                onFetchFailure(false);
            }
        }
    }

    @SuppressLint("ShowToast")
    private void onFetchFailure(boolean isTestAttempted) {

        showRefreshButton();
        if (isTestAttempted) {
            Toast.makeText(session.getContext(), R.string.test_not_attempted_yet, Toast.LENGTH_LONG)
                    .show();
            getActivity().finish();
        } else {
            Toast.makeText(getActivity().getApplicationContext(), R.string.test_data_fetch_failed,
                    Toast.LENGTH_LONG).show();
        }
    }

    private void drawMarksDistribution(EntityMarkDistribution resp) {

        long total = testInfo.attempts;
        total = total < 0 ? 1 : total;
        BarGraphView barGraph = (BarGraphView) parentView
                .findViewById(R.id.test_marks_distribution_bar_graph);
        marksDistributionValues = new BarGraphBean[resp.list.size()];
        for (int index = 0; index < resp.list.size() && index < 5; index++) {
            MarkDistribution value = resp.list.get(index);
            if (value == null) {
                continue;
            }
            String title = value.from + " to " + value.to;
            int percentage = (int) (value.count / total * 100);
            marksDistributionValues[index] = new BarGraphBean(percentage, title);
        }
        barGraph.setValues(marksDistributionValues);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);
        Log.i(TAG, "onConfigurationChanged called");
    }

    @Override
    public void onResume() {

        super.onResume();
        GoogleAnalyticsUtils.sendPageViewDataToGA();
    }

    @Override
    public void onDestroy() {

        isDestroyed = true;
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {

        // TODO Auto-generated method stub
    }
}
