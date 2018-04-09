package com.nhance.android.fragments.tests;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nhance.android.adapters.tests.RankListAdapter;
import com.nhance.android.async.tasks.ITaskProcessor;
import com.nhance.android.async.tasks.socials.WebCommunicatorTask;
import com.nhance.android.async.tasks.socials.WebCommunicatorTask.ReqAction;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.enums.MemberProfile;
import com.nhance.android.fragments.AbstractVedantuFragment;
import com.nhance.android.library.utils.LibraryUtils;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.pojos.tests.UserEntityRank;
import com.nhance.android.utils.GoogleAnalyticsUtils;
import com.nhance.android.utils.JSONUtils;
import com.nhance.android.utils.VedantuWebUtils;
import com.nhance.android.R;

public class TestRankListFragment extends AbstractVedantuFragment implements OnScrollListener {

    private static final String         TAG           = "TestRankListFragment";
    private WebCommunicatorTask userRankFetcher;
    private WebCommunicatorTask entityRankListFetcher;
    RankListAdapter                     rankListAdapter;
    final int                           size          = 15;
    int                                 totalAttempts = 0;
    SessionManager                      session;
    List<UserEntityRank>                testRankList  = new ArrayList<UserEntityRank>();
    boolean                             loading       = false;
    private TextView                    userRankView;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        GoogleAnalyticsUtils.setScreenName(ConstantGlobal.GA_SCREEN_TEST_RANK_LIST);
        setHasOptionsMenu(true);
    }

    @Override
    public View
            onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_test_rank_list, container, false);
        if (getArguments() == null) {
            Log.e(TAG, "no argument found");
            return null;
        }
        AppCompatActivity parentActivity = (AppCompatActivity) getActivity();
        session = SessionManager.getInstance(parentActivity);
        
        MemberProfile memberProfile = LibraryUtils._getMemberProfile(parentActivity.getApplicationContext());
        if(MemberProfile.STUDENT == memberProfile || MemberProfile.OFFLINE_USER == memberProfile){
        	userRankView = (TextView) view.findViewById(R.id.test_rank);
        	loadUserRank();
        }else{
        	LinearLayout userRankHolder = (LinearLayout) view.findViewById(R.id.test_analytics_user_rank_status);
        	userRankHolder.setVisibility(View.GONE);
        }

        ListView rankList = (ListView) view.findViewById(R.id.test_rank_list);
        rankListAdapter = new RankListAdapter(getActivity(),
                R.layout.list_item_view_test_rank, this.testRankList, getArguments().getInt(
                        ConstantGlobal.TOTAL_MARKS));
        rankList.setAdapter(rankListAdapter);
        rankList.setOnScrollListener(this);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        loadRankList(0);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
        setUserVisibleHint(true);
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
                showProgressBar();
                // start reloading of userRank and rankList
                loadUserRank();
                if (entityRankListFetcher != null) {
                    // cancel already going fetching
                    entityRankListFetcher.cancel(true);
                }
                loading = false;
                loadRankList(0);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadUserRank() {

        userRankFetcher = new WebCommunicatorTask(session, null,
                ReqAction.GET_USER_ENTITY_RANK, new ITaskProcessor<JSONObject>() {

                    @Override
                    public void onTaskStart(JSONObject result) {

                    }

                    @Override
                    public void onTaskPostExecute(boolean success, JSONObject result) {

                        if (success) {
                            int rank = JSONUtils.getInt(
                                    JSONUtils.getJSONObject(result, VedantuWebUtils.KEY_RESULT),
                                    "rank");
                            if (userRankView != null) {
                                userRankView.setText(String.valueOf(rank));
                            }
                        }
                    }
                }, null, null, 0);
        userRankFetcher.addExtraParams("entity.id",
                getArguments().getString(ConstantGlobal.ENTITY_ID));
        userRankFetcher.addExtraParams("entity.type",
                getArguments().getString(ConstantGlobal.ENTITY_TYPE));
        userRankFetcher.executeTask(false);
    }

    private void loadRankList(int start) {

        if (!loading && (start == 0 || start < totalAttempts)) {

            Log.d(TAG, "loadingRankList start:" + start);
            loading = true;
            entityRankListFetcher = new WebCommunicatorTask(session, null,
                    ReqAction.GET_ENTITY_LEADER_BOARD, new RankListTaskPostExecuter(start), null,
                    null, start, size);
            entityRankListFetcher.addExtraParams("entity.id",
                    getArguments().getString(ConstantGlobal.ENTITY_ID));
            entityRankListFetcher.addExtraParams("entity.type",
                    getArguments().getString(ConstantGlobal.ENTITY_TYPE));
            entityRankListFetcher.addExtraParams("miniInfo", String.valueOf(true));
            entityRankListFetcher.executeTask(true);
        }
    }

    @Override
    public void onStop() {

        super.onStop();
        if (userRankFetcher != null) {
            userRankFetcher.cancel(true);
        }

        if (entityRankListFetcher != null) {
            entityRankListFetcher.cancel(true);
        }
    }

    private class RankListTaskPostExecuter implements ITaskProcessor<JSONObject> {

        private int start = 0;

        private RankListTaskPostExecuter(int start) {

            super();
            this.start = start;
        }

        @Override
        public void onTaskStart(JSONObject result) {

        }

        @Override
        public void onTaskPostExecute(boolean success, JSONObject result) {

            if (!success) {
                // TODO: handle failed event
                return;
            }
            result = JSONUtils.getJSONObject(result, VedantuWebUtils.KEY_RESULT);
            int totalCount = JSONUtils.getInt(result, VedantuWebUtils.KEY_TOTAL_HITS);
            totalAttempts = totalCount;
            JSONArray list = JSONUtils.getJSONArray(result, VedantuWebUtils.KEY_LIST);
            List<UserEntityRank> userRanks = new ArrayList<UserEntityRank>();
            for (int i = 0; i < list.length(); i++) {
                UserEntityRank userRank = new UserEntityRank();
                try {
                    userRank.fromJSON(list.getJSONObject(i));
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
                userRanks.add(userRank);
            }

            if (start == 0) {
                // this is refresh call hence clear all rank list and populate it again
                testRankList.clear();
            }
            testRankList.addAll(userRanks);
            rankListAdapter.notifyDataSetChanged();
            loading = false;
            showRefreshButton();
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
            int totalItemCount) {

        int lastItem = firstVisibleItem + visibleItemCount;
        if (lastItem == totalItemCount && !loading) {
            loadRankList(lastItem);
        }
    }

    @Override
    public void onResume() {

        super.onResume();
        GoogleAnalyticsUtils.sendPageViewDataToGA();
    }

    @Override
    public void onPause() {

        super.onPause();
        if (userRankFetcher != null) {
            userRankFetcher.cancel(true);
        }

        if (entityRankListFetcher != null) {
            entityRankListFetcher.cancel(true);
            loading = false;
        }
    }

}
