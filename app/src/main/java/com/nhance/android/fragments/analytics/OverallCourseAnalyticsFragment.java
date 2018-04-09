package com.nhance.android.fragments.analytics;

import java.util.List;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.nhance.android.adapters.analytics.OverallCourseAnalyticsContentListAdapter;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.models.analytics.TestBoardAnalytics;
import com.nhance.android.db.models.analytics.TestCourseLevelAnalytics;
import com.nhance.android.enums.BoardType;
import com.nhance.android.utils.SQLDBUtil;
import com.nhance.android.R;

public class OverallCourseAnalyticsFragment extends AbstractOverallAnalyticsFragment implements
        OnItemClickListener {

    private static final String    TAG = "OverallCourseAnalyticsFragment";
    List<TestCourseLevelAnalytics> levelsAnalytics;
    List<TestBoardAnalytics>       topicAnalytics;
    TestBoardAnalytics             courseAnalytics;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        courseAnalytics = (TestBoardAnalytics) getArguments().getSerializable(
                ConstantGlobal.TEST_COURSE_ANALYTICS);
        levelsAnalytics = analyticsDataManager.getTestCourseLevelWiseAnalytics(
                courseAnalytics.orgKeyId, courseAnalytics.userId, courseAnalytics.entityId,
                courseAnalytics.entityType, courseAnalytics.id, courseAnalytics.type, null);
        topicAnalytics = analyticsDataManager.getTestBoardAnalytics(
                session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID),
                session.getSessionStringValue(ConstantGlobal.USER_ID), courseAnalytics.entityId,
                courseAnalytics.entityType, courseAnalytics.id, BoardType.TOPIC,
                ConstantGlobal.NAME, SQLDBUtil.ORDER_ASC);
        topicAnalytics.add(0, null);
        m_cObjNhanceBaseActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        m_cObjNhanceBaseActivity.getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_overall_course_analytics, container, false);

        ListView topicListView = (ListView) view
                .findViewById(R.id.overall_course_analytics_topic_list_container);

        boolean splitLayout = topicListView != null;

        // add null at starting of topicAnalytics so that we can populate headers view for topic
        // analytics section

        ListView containerView = (ListView) view
                .findViewById(R.id.overall_course_analytics_container);
        containerView.setAdapter(new OverallCourseAnalyticsContentListAdapter(m_cObjNhanceBaseActivity,
                R.layout.list_item_view_overall_course_analytics_topic, courseAnalytics,analyticsDataManager, session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID),
                session.getSessionStringValue(ConstantGlobal.USER_ID),
                splitLayout ? null : topicAnalytics, levelsAnalytics, 3));
        containerView.setOnItemClickListener(this);

        if (splitLayout) {
            topicListView.setAdapter(new OverallCourseAnalyticsContentListAdapter(m_cObjNhanceBaseActivity,
                    R.layout.list_item_view_overall_course_analytics_topic, null, analyticsDataManager, session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID), session.getSessionStringValue(ConstantGlobal.USER_ID), topicAnalytics,
                    null, 0));
            topicListView.setOnItemClickListener(this);
        }
        setupSyncButton();
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);
        showRefreshButton();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

        case android.R.id.home:
            m_cObjNhanceBaseActivity.onBackPressed();
            break;
        default:
            return super.onOptionsItemSelected(item);
        }
        return false;
    }

    @Override
    public void onPause() {

        super.onPause();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Log.d(TAG, "item position:" + position);
        if (position == 0) {
            // don't do anything

        } else if (view.getTag() != null && view.getTag() instanceof TestBoardAnalytics) {
            // user clicked on course topic analytics bar
            // TODO: show topic popup if needed
        }

    }
}
