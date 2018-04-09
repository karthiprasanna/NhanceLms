package com.nhance.android.fragments.analytics;

import java.util.Calendar;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.AbsListView.OnScrollListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.nhance.android.async.tasks.CachedWebDataFetcherTask;
import com.nhance.android.async.tasks.ICachedTaskProcessor;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.ContentDataManager;
import com.nhance.android.db.datamanagers.ContentLinkDataManager;
import com.nhance.android.db.models.entity.ContentLink;
import com.nhance.android.library.utils.LibraryUtils;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.pojos.LibraryContentRes;
import com.nhance.android.pojos.OrgMemberMappingInfo.OrgProgramCenterSectionIds;
import com.nhance.android.pojos.onlinedata.tests.TestAnalyticsListRes;
import com.nhance.android.pojos.onlinedata.tests.TestAnalyticsRes;
import com.nhance.android.utils.GoogleAnalyticsUtils;
import com.nhance.android.utils.ViewUtils;
import com.nhance.android.R;

public class TeacherOverallAnalyticsFragment extends AbstractOverallAnalyticsFragment {

    SessionManager                 session;
    private View                   parentView;
    final private String           TAG                      = "TeacherOverallAnalyticsFragment";
    final String                   entityType               = "TEST";
    private List<TestAnalyticsRes> testAnalyticsList;
    private boolean                showCache                = true;
    private Handler                mHandler                 = new Handler();
    private long                   totalHits                = 0;
    private TestAnalyticsAdapter   testAnalyticsAdapter;
    private ProgressDialog         dialog;
    private boolean                isDestroyed              = false;

    private int                    start                    = 0;
    private final int              SIZE                     = 10;
    private boolean                fetchInProgressSemaphore = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        GoogleAnalyticsUtils.setScreenName(ConstantGlobal.GA_SCREEN_ANALYTICS);
        setHasOptionsMenu(true);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.destroyViewOnConfigChange = false;
        super.onConfigurationChanged(newConfig);
        Log.i(TAG, "onConfigurationChanged called");
    }

    @Override
    public View
            onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_teacher_overall_analytics, container, false);
        parentView = view;
        setupSyncButton();
        initViews();
        loadAnalytics();
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);
        showRefreshButton();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_refresh) {
            if (!SessionManager.isOnline()) {
                Toast.makeText(m_cObjNhanceBaseActivity, R.string.no_internet_msg, Toast.LENGTH_LONG).show();
            } else {
                resetAll();
                showCache = false;
                loadAnalytics();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {

        super.onResume();
        if (reloadAnalytics) {
            resetAll();
            loadAnalytics();
        }
    }

    private void initViews() {

        CheckBox showHighest = (CheckBox) parentView
                .findViewById(R.id.teacher_analytics_show_highest);
        final ListView container = (ListView) parentView
                .findViewById(R.id.teacher_analytics_test_container);
        showHighest.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                int visibility = View.INVISIBLE;
                if (isChecked) {
                    visibility = View.VISIBLE;
                }

                for (int index = 0; index < container.getChildCount(); index++) {
                    RelativeLayout holder = (RelativeLayout) container.getChildAt(index);
                    holder.findViewById(R.id.teacher_analytics_highest_score_bar).setVisibility(
                            visibility);
                    holder.findViewById(R.id.teacher_analytics_test_highest_score).setVisibility(
                            visibility);
                }

            }
        });
        container.setOnScrollListener(new OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                    int totalItemCount) {

                int lastItem = firstVisibleItem + visibleItemCount;
                if (lastItem == totalItemCount) {
                    Log.d(TAG, "Now is the time to scroll, total Items found " + totalItemCount);
                    loadMoreAnalytics(totalItemCount);
                }
            }
        });
    }

    private void loadMoreAnalytics(int newStart) {

        if (newStart < totalHits && start != newStart && !fetchInProgressSemaphore) {
            start = newStart;
            loadAnalytics();
        }
    }

    private void loadAnalytics() {

        showLoadingView();
        session = SessionManager.getInstance(m_cObjNhanceBaseActivity);
        OrgProgramCenterSectionIds currentOrgIds = session.getCurrentProgramCenterSectionIds();
        Log.d(TAG, "Current Org Ids = " + currentOrgIds);

        String cacheKey = "TEACHER_ANALYTICS/" + entityType + "/section/" + currentOrgIds.sectionId;
        boolean doCache = start > 0 ? false : showCache;
        Log.d(TAG, "do cache = " + doCache + " , start = " + start + ", size = " + SIZE);
        CachedWebDataFetcherTask<TestAnalyticsListRes> cachedWebDataFetcherTask = new CachedWebDataFetcherTask<TestAnalyticsListRes>(
                session, null, CachedWebDataFetcherTask.ReqAction.ENTITY_SCHEDULE_ANALYTICS,
                new AnalyticsTaskProcessor(), doCache, cacheKey, TestAnalyticsListRes.class, start,
                SIZE);

        cachedWebDataFetcherTask.addExtraParams("entityType", entityType);
        int year = Calendar.getInstance().get(Calendar.YEAR);
        cachedWebDataFetcherTask.addExtraParams("year", year);
        cachedWebDataFetcherTask.addExtraParams("programId", currentOrgIds.programId);
        cachedWebDataFetcherTask.addExtraParams("centerId", currentOrgIds.centerId);
        cachedWebDataFetcherTask.addExtraParams("sectionId", currentOrgIds.sectionId);
        cachedWebDataFetcherTask.executeTask(false);
        fetchInProgressSemaphore = true;
    }

    private final class AnalyticsTaskProcessor implements
            ICachedTaskProcessor<TestAnalyticsListRes> {

        @Override
        public void onResultDataFromCache(TestAnalyticsListRes data) {

            if (isDestroyed)
                return;
            if (start == 0) {
                if (data != null) {
                    drawAnalytics(data, true);
                } else {
                    dialog = new ProgressDialog(m_cObjNhanceBaseActivity);
                    dialog.setCancelable(true);
                    dialog.setOnCancelListener(new OnCancelListener() {

                        @Override
                        public void onCancel(DialogInterface dialog) {
                            m_cObjNhanceBaseActivity.finish();
                        }
                    });
                    dialog.setMessage(getResources().getString(R.string.loading));
                    dialog.show();
                }
            }
        }

        @Override
        public void onTaskPostExecute(boolean success, TestAnalyticsListRes result) {
            
            if (isDestroyed)
                return;
            hideLoadingViews();
            fetchInProgressSemaphore = false;
            if (dialog != null) {
                dialog.dismiss();
            }
            if (success && result != null) {
                showZeroLevel(false);
                drawAnalytics(result, false);
            } else if (testAnalyticsList == null || testAnalyticsList.size() == 0) {
                showZeroLevel(true);
            }

        }
    }

    private void showZeroLevel(boolean showNoAnalytics) {

        if (showNoAnalytics) {
            parentView.findViewById(R.id.no_analytics).setVisibility(View.VISIBLE);
            parentView.findViewById(R.id.teacher_analytics_top_bar).setVisibility(View.INVISIBLE);
            parentView.findViewById(R.id.teacher_analytics_test_container).setVisibility(
                    View.INVISIBLE);
        } else {
            parentView.findViewById(R.id.no_analytics).setVisibility(View.GONE);
            parentView.findViewById(R.id.teacher_analytics_top_bar).setVisibility(View.VISIBLE);
            parentView.findViewById(R.id.teacher_analytics_test_container).setVisibility(
                    View.VISIBLE);
        }
    }

    private void drawAnalytics(TestAnalyticsListRes resp, boolean fromCache) {

        if (isDestroyed)
            return;
        Log.d(TAG, "Analytics data ====== " + resp.totalHits);
        totalHits = resp.totalHits;

        if (resp.list.size() > 0) {
            ListView container = (ListView) parentView
                    .findViewById(R.id.teacher_analytics_test_container);
            if (start == 0) {
                testAnalyticsList = resp.list;
                testAnalyticsAdapter = new TestAnalyticsAdapter(m_cObjNhanceBaseActivity,
                        R.layout.list_item_teacher_analytics_test, testAnalyticsList);
                container.setAdapter(testAnalyticsAdapter);
            } else if (!fromCache) {
                testAnalyticsList.addAll(resp.list);
                testAnalyticsAdapter.notifyDataSetChanged();
            }
        } else if (!fromCache) {
            showZeroLevel(true);
        }
    }

    private final class TestAnalyticsAdapter extends ArrayAdapter<TestAnalyticsRes> {

        public TestAnalyticsAdapter(Context context, int resource, List<TestAnalyticsRes> objects) {

            super(context, resource, objects);
            // TODO Auto-generated constructor stub
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {

            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.list_item_teacher_analytics_test, parent, false);
            }
            final TestAnalyticsRes test = getItem(position);
            final RelativeLayout holder = (RelativeLayout) view;
            ViewUtils.setTextViewValue(holder, R.id.teacher_analytics_test_name, test.name);
            ViewUtils.setTextViewValue(holder, R.id.teacher_analytics_test_attempts,
                    String.valueOf(test.totalAttempts));

            final int totalMarks = test.totalMarks == 0 ? 1 : test.totalMarks;
            final int totalAttempts = (int) (test.totalAttempts == 0 ? 1 : test.totalAttempts);
            int highestScore = (test.measures.maxScore * 100) / totalMarks;
            ViewUtils.setTextViewValue(holder, R.id.teacher_analytics_test_highest_score,
                    String.valueOf(highestScore),
                    getResources().getString(R.string.percentage_indicator));
            int averageScore = ((test.measures.score / totalAttempts) * 100) / totalMarks;
            ViewUtils.setTextViewValue(holder, R.id.teacher_analytics_test_avg_score,
                    String.valueOf(averageScore),
                    getResources().getString(R.string.percentage_indicator));

            mHandler.post(new Runnable() {

                @Override
                public void run() {

                    int width = holder.getWidth();
                    int avgBarWidth = ((test.measures.score / totalAttempts) * width) / totalMarks;
                    avgBarWidth = avgBarWidth < 0 ? 0 : avgBarWidth;
                    int highestBarWidth = (test.measures.maxScore * width) / totalMarks;
                    highestBarWidth -= 2;
                    highestBarWidth = highestBarWidth < 0 ? 0 : highestBarWidth;
                    View avgBarView = holder.findViewById(R.id.teacher_analytics_avg_score_bar);
                    avgBarView.getLayoutParams().width = avgBarWidth;
                    View highestBarView = holder
                            .findViewById(R.id.teacher_analytics_highest_score_bar);
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) highestBarView
                            .getLayoutParams();
                    params.setMargins(highestBarWidth, 0, 0, 0);
                    highestBarView.setLayoutParams(params);
                }
            });
            holder.setTag(test);
            holder.setOnClickListener(new OnTestClick());
            return view;
        }
    }

    private final class OnTestClick implements OnClickListener {

        ContentLinkDataManager contentLinkDataManager;
        ContentDataManager     contentDataManager;

        public OnTestClick() {

            super();
            contentLinkDataManager = new ContentLinkDataManager(m_cObjNhanceBaseActivity);
            contentDataManager = new ContentDataManager(m_cObjNhanceBaseActivity);
        }

        @Override
        public void onClick(View v) {

            TestAnalyticsRes test = (TestAnalyticsRes) v.getTag();

            ContentLink contentLink = contentLinkDataManager.getContentLink(test.id, entityType,
                    session.getSessionStringValue(ConstantGlobal.USER_ID), null, null,
                    session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID));
            if (contentLink == null) {
                Toast.makeText(m_cObjNhanceBaseActivity, getResources().getString(R.string.do_sync_content),
                        Toast.LENGTH_LONG).show();
                return;
            }
            LibraryContentRes contentRes = contentDataManager
                    .getLibraryContentRes(contentLink.linkId);
            if (contentRes != null) {
                LibraryUtils.onLibraryItemClickListnerImpl(m_cObjNhanceBaseActivity, contentRes);
            } else {
                Toast.makeText(m_cObjNhanceBaseActivity, "Something went wrong , try later!",
                        Toast.LENGTH_LONG).show();
            }

        }

    }

    private void resetAll() {

        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
        if (parentView != null) {
            CheckBox showHighest = (CheckBox) parentView
                    .findViewById(R.id.teacher_analytics_show_highest);
            showHighest.setChecked(false);
        }
        testAnalyticsAdapter = null;
        testAnalyticsList = null;
        start = 0;
        totalHits = 0;
    }

    @Override
    public void onDestroy() {

        resetAll();
        showCache = true;
        isDestroyed = true;
        super.onDestroy();
    }
}
