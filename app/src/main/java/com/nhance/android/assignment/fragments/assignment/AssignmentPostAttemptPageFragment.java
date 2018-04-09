package com.nhance.android.assignment.fragments.assignment;

import android.os.Bundle;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

import com.nhance.android.R;
import com.nhance.android.assignment.adapter.viewpager.AssignmentPostAttemptFragmentPageAdapter;
import com.nhance.android.assignment.pojo.content.infos.AssignmentExtendedInfo;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.AnalyticsDataManager;
import com.nhance.android.fragments.AbstractVedantuFragment;

/**
 * Created by Himank Shah on 12/7/2016.
 */

public class AssignmentPostAttemptPageFragment extends AbstractVedantuFragment implements
        OnTabChangeListener {

    private static final String TAG = "AssignmentPostAttemptPageFragment";

    private AnalyticsDataManager analyticsDataManager;

    ViewPager mPager;

    @Override
    public View
    onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_assignment_post_attempt_page, container, false);
        if (getArguments() == null) {
            Log.e(TAG, "not argument received via getArguments()");
            return null;
        }

        analyticsDataManager = new AnalyticsDataManager(getActivity());
        setUpTabView(view);

        return view;
    }

    @Override
    public void onTabChanged(String tabId) {

        int position = mTabHost.getCurrentTab();
        mPager.setCurrentItem(position);
    }

    private void setUpTabView(View parentView) {

        mTabHost = (TabHost) parentView.findViewById(android.R.id.tabhost);
        mTabHost.setup();

        mPager = (ViewPager) parentView.findViewById(R.id.pager_assignment_post_attempt);

        // Capture ViewPager page swipes
        ViewPager.SimpleOnPageChangeListener viewPagerListener = new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {

                super.onPageSelected(position);
                // Find the ViewPager Position
                mTabHost.setCurrentTab(position);
                // mActionBar.setSelectedNavigationItem(position);
            }
        };
        Log.e("ASSIGNMENT_METADATA", "" + (AssignmentExtendedInfo) getArguments().getSerializable(ConstantGlobal.ASSIGNMENT_METADATA));
        mPager.setOnPageChangeListener(viewPagerListener);
            FragmentStatePagerAdapter adapter = new AssignmentPostAttemptFragmentPageAdapter(
                    getActivity().getSupportFragmentManager(), analyticsDataManager,
                    (AssignmentExtendedInfo) getArguments().getSerializable(ConstantGlobal.ASSIGNMENT_METADATA),
                    getArguments().getString(ConstantGlobal.ENTITY_ID), getArguments().getString(
                    ConstantGlobal.ENTITY_TYPE), getActivity().getApplicationContext(),getArguments().getString(
                    ConstantGlobal.ASSIGNMENT_NAME));

            mPager.setOffscreenPageLimit(adapter.getCount());

            mPager.setAdapter(adapter);

        for (String tabName : getResources().getStringArray(R.array.assignment_post_page_tabs)) {
            addTab(tabName, tabName);
        }
        mTabHost.setOnTabChangedListener(this);

    }
}
