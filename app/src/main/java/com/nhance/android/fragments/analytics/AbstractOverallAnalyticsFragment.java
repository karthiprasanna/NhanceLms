package com.nhance.android.fragments.analytics;

import org.json.JSONObject;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.nhance.android.async.tasks.AnalyticsSyncer;
import com.nhance.android.async.tasks.ITaskProcessor;
import com.nhance.android.db.datamanagers.AnalyticsDataManager;
import com.nhance.android.enums.EntityType;
import com.nhance.android.fragments.AbstractVedantuFragment;
import com.nhance.android.interfaces.INavgationDrawer;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.R;

public abstract class AbstractOverallAnalyticsFragment extends AbstractVedantuFragment {

    private final String           TAG                       = "AbstractOverallAnalyticsFragment";
    protected AnalyticsDataManager analyticsDataManager;
    protected SessionManager       session;
    protected boolean              reloadAnalytics           = false;
    protected boolean              destroyViewOnConfigChange = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        analyticsDataManager = new AnalyticsDataManager(m_cObjNhanceBaseActivity);
        session = SessionManager.getInstance(m_cObjNhanceBaseActivity);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);
        Log.i(TAG, "onConfigurationChanged called");
        if (destroyViewOnConfigChange) {
            destoryAndCreateFragmentView();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);
        Log.d(TAG, "onCreateOptionsMenu");
        inflater.inflate(R.menu.overall_analytics, menu);
        progressMenuItem = menu.findItem(R.id.action_refresh);
        // if (loadingNewCoursesContent) {
        // progressMenuItem.setVisible(true);
        // } else {
        // progressMenuItem.setVisible(false);
        // }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_logout && session != null) {
            session.logoutUser();
        } else if (item.getItemId() == R.id.action_refresh) {
            showLoadingView();
            reloadAnalytics = true;
            syncAnalytics();
        }
        return super.onOptionsItemSelected(item);
    }

    protected void setupSyncButton() {

        final INavgationDrawer mNavgationDrawer = (INavgationDrawer) getActivity();
        final View syncButton = mNavgationDrawer.getSyncButton();
        syncButton.setVisibility(View.VISIBLE);

        syncButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Log.d(TAG, "syncButton is clicked");
                if (!SessionManager.isOnline()) {
                    Toast.makeText(m_cObjNhanceBaseActivity, getString(R.string.no_internet_msg),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                showLoadingView();
                mNavgationDrawer.closeDrawer();
                syncAnalytics();

            }
        });

    }

    @Override
    public void onPause() {

        super.onPause();
        if (analyticsSyncer != null) {
            analyticsSyncer.cancel(true);
        }
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        hideLoadingViews();
    }

    protected void hideLoadingViews() {

        final INavgationDrawer mNavgationDrawer = (INavgationDrawer) getActivity();
        mNavgationDrawer.getSyncButton().setVisibility(View.VISIBLE);
        mNavgationDrawer.getDrawerView().findViewById(R.id.navigation_sync_in_progress)
                .setVisibility(View.GONE);
        showRefreshButton();
    }

    protected void showLoadingView() {

        final INavgationDrawer mNavgationDrawer = (INavgationDrawer) getActivity();
        mNavgationDrawer.getSyncButton().setVisibility(View.GONE);
        mNavgationDrawer.getDrawerView().findViewById(R.id.navigation_sync_in_progress)
                .setVisibility(View.VISIBLE);
        showProgressBar();
    }

    AnalyticsSyncer analyticsSyncer;

    private void syncAnalytics() {

        Log.d(TAG, "syncButton is clicked");
        if (!SessionManager.isOnline()) {
            showRefreshButton();
            Toast.makeText(m_cObjNhanceBaseActivity, getString(R.string.no_internet_msg), Toast.LENGTH_LONG)
                    .show();
            return;
        }
        analyticsSyncer = new AnalyticsSyncer(session, null, EntityType.TEST.name(),
                new ITaskProcessor<JSONObject>() {

                    @Override
                    public void onTaskStart(JSONObject result) {

                    }

                    @Override
                    public void onTaskPostExecute(boolean success, JSONObject result) {

                        hideLoadingViews();

                        if (!success) {
                            return;
                        }
                        reloadAnalytics = true;
                        onResume();
                        Toast.makeText(m_cObjNhanceBaseActivity, R.string.syncing_success, Toast.LENGTH_LONG)
                                .show();
                    }
                });
        analyticsSyncer.executeTask(false);
    }
}
