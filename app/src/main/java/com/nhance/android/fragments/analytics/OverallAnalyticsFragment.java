package com.nhance.android.fragments.analytics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.nhance.android.activities.AppLandingPageActivity;
import com.nhance.android.activities.tests.TestPreAttemptPageActivity;
import com.nhance.android.adapters.analytics.OverallAnalyticsContentListAdapter;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.AnalyticsDataManager;
import com.nhance.android.db.models.analytics.TestBoardAnalytics;
import com.nhance.android.enums.BoardType;
import com.nhance.android.enums.EntityType;
import com.nhance.android.enums.NavigationItem;
import com.nhance.android.managers.LocalManager;
import com.nhance.android.pojos.TestAnalyticsMiniPojo;
import com.nhance.android.receivers.NetworkMonitor;
import com.nhance.android.utils.GoogleAnalyticsUtils;
import com.nhance.android.utils.SQLDBUtil;
import com.nhance.android.R;

public class OverallAnalyticsFragment extends AbstractOverallAnalyticsFragment implements
        OnItemClickListener {

    List<TestBoardAnalytics>           courseAnalytics;
    List<TestAnalyticsMiniPojo>        testItems;
    OverallAnalyticsContentListAdapter courseContentListDapter;
    OverallAnalyticsContentListAdapter topicListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        GoogleAnalyticsUtils.setScreenName(ConstantGlobal.GA_SCREEN_ANALYTICS);
        loadAnalytics();
        // this is just to trigger background sync process
        m_cObjNhanceBaseActivity.sendBroadcast(new Intent(m_cObjNhanceBaseActivity, NetworkMonitor.class));
        setHasOptionsMenu(true);
    }

    @Override
    public View
            onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_overall_analytics, container, false);
        setupSyncButton();

        // if overall course analytics is null that means no test is being attempted, hence no need
        // to go for test list population

        ListView courseListView = (ListView) view.findViewById(R.id.overall_analytics_course_list);

        ListView testListListView = (ListView) view.findViewById(R.id.overall_analytics_test_list);

        boolean splitView = testListListView != null;

        if (courseAnalytics == null || courseAnalytics.isEmpty()) {
            courseListView.setVisibility(View.GONE);
            if (testListListView != null) {
                testListListView.setVisibility(View.GONE);
            }
            view.findViewById(R.id.no_analytics).setVisibility(View.VISIBLE);
            return view;
        }

        courseListView.setAdapter(courseContentListDapter = new OverallAnalyticsContentListAdapter(
                m_cObjNhanceBaseActivity, R.layout.list_item_view_overall_analytics_course, courseAnalytics, analyticsDataManager, session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID),
                session.getSessionStringValue(ConstantGlobal.USER_ID),
                splitView ? null : testItems));
        courseListView.setOnItemClickListener(this);

        if (splitView) {
            testListListView.setAdapter(topicListAdapter = new OverallAnalyticsContentListAdapter(
                    m_cObjNhanceBaseActivity, R.layout.list_item_view_overall_analytics_test_list, null,
                    analyticsDataManager, session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID), session.getSessionStringValue(ConstantGlobal.USER_ID), testItems));
            testListListView.setOnItemClickListener(this);
        }

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);
        showRefreshButton();
    }

    @Override
    public void onResume() {

        super.onResume();
        if (reloadAnalytics) {
            loadAnalytics();
            if (courseContentListDapter != null) {
                courseContentListDapter.notifyDataSetChanged();
            }

            if (topicListAdapter != null) {
                topicListAdapter.notifyDataSetChanged();
            }

            reloadAnalytics = false;
        }
        GoogleAnalyticsUtils.sendPageViewDataToGA();
        showRefreshButton();
    }

    private void loadAnalytics() {

        if (courseAnalytics == null) {
            courseAnalytics = new ArrayList<TestBoardAnalytics>();
        }

        courseAnalytics.clear();
        courseAnalytics.addAll(analyticsDataManager.getTestBoardAnalytics(
                session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID),
                session.getSessionStringValue(ConstantGlobal.USER_ID),
                AnalyticsDataManager.TEST_ID_OVERALL_SYSTEM, EntityType.TEST.name(), null,
                BoardType.COURSE, ConstantGlobal.NAME, SQLDBUtil.ORDER_ASC));

        if (testItems == null) {
            testItems = new ArrayList<TestAnalyticsMiniPojo>();
        }

        testItems.clear();
        testItems.addAll(LocalManager.getTestAnalyticsMiniInfo(
                session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID),
                session.getSessionStringValue(ConstantGlobal.USER_ID), null,
                EntityType.TEST.name(), m_cObjNhanceBaseActivity, true));
        if (testItems != null) {
            testItems.add(0, null);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (view.getTag() == null) {
            return;
        }

        Fragment fragment = null;
        Bundle args = new Bundle();
        if (view.getTag() instanceof TestBoardAnalytics) {
            fragment = new OverallCourseAnalyticsFragment();
            args.putSerializable(ConstantGlobal.TEST_COURSE_ANALYTICS, (Serializable) view.getTag());
        } else if (view.getTag() instanceof TestAnalyticsMiniPojo) {
            args.putSerializable(ConstantGlobal.CONTENT_ID,
                    ((TestAnalyticsMiniPojo) view.getTag()).contentId);
            // start test preTestPageActivity
            Intent intent = new Intent(m_cObjNhanceBaseActivity, TestPreAttemptPageActivity.class);
            intent.putExtras(args);
            startActivity(intent);
        }

        if (fragment != null) {
            fragment.setArguments(args);

            if (m_cObjNhanceBaseActivity instanceof AppLandingPageActivity) {
                ((AppLandingPageActivity) m_cObjNhanceBaseActivity).replaceFragment(fragment,
                        NavigationItem.ANALYTICS_COURSE);
            }
        }
    }

}
