package com.nhance.android.notifications;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nhance.android.R;
import com.nhance.android.activities.about.AboutActivity;
import com.nhance.android.async.tasks.AbstractVedantuJSONAsyncTask;
import com.nhance.android.fragments.AbstractVedantuFragment;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.recentActivities.Actor;
import com.nhance.android.utils.VedantuWebUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import static android.content.ContentValues.TAG;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link NotificationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NotificationFragment extends AbstractVedantuFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private RecyclerView listView;
    private ArrayList<Actor> notificationsList;
    private NotificationAdapter itemsAdapter;
    private TextView noNotificationTextView;
    private String userName, profile;
    private String user;
    private LinearLayoutManager mLayoutManager;
    private View rootView;
    private SessionManager session;


    public NotificationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NotificationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NotificationFragment newInstance(String param1, String param2) {
        NotificationFragment fragment = new NotificationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        session = SessionManager.getInstance(m_cObjNhanceBaseActivity);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_notification, container, false);


        notificationsList = new ArrayList<Actor>();
        listView = (RecyclerView)rootView.findViewById(R.id.list_view);
        mLayoutManager = new LinearLayoutManager(getActivity());
        listView.setLayoutManager(mLayoutManager);
        noNotificationTextView = (TextView)rootView.findViewById(R.id.NoNotificationTextView);
        itemsAdapter = new NotificationAdapter(NotificationFragment.this,  notificationsList);
        listView.setAdapter(itemsAdapter);
        new GetNotificationsAsyncTask(SessionManager.getInstance(getActivity()), null, "getNotifcations", null, 0, "NEW").executeTask(false);
        return rootView;
    }

    public class GetNotificationsAsyncTask extends AbstractVedantuJSONAsyncTask {


        private final ProgressDialog pDialog;
        private String entityId;
        private String entityType;
        private String comment;
        private String beforeNotificationId1;
        private int startCount;
        private String feedType;

        public GetNotificationsAsyncTask(SessionManager session, ProgressBar progressUpdater, String url, String beforeNotificationId, int count, String feedType) {
            super(session, progressUpdater);
            this.url = session.getApiUrl(url);
            pDialog = new ProgressDialog(getActivity());
            beforeNotificationId1 = beforeNotificationId;
            startCount = count;
            this.feedType = feedType;
            pDialog.setMessage("Please wait..");
            pDialog.setCancelable(false);
            if(!(getActivity().isFinishing())){
                pDialog.show();
            }

            Log.e("getNotifiAsyncTask", "call");
        }
        @Override
        protected JSONObject doInBackground(String... params) {

            session.addSessionParams(httpParams);

            httpParams.put("callingApp", "TapApp");
            httpParams.put("callingUserId", session.getOrgMemberInfo().userId);
            httpParams.put("userRole", session.getOrgMemberInfo().profile);
            httpParams.put("callingAppId", "TapApp");
            httpParams.put("size", 10);
            httpParams.put("orgId",session.getOrgMemberInfo().orgId);
            httpParams.put("beforeNotificationId", beforeNotificationId1);
            httpParams.put("start", startCount);
            httpParams.put("feedType", feedType);
            httpParams.put("userId",session.getOrgMemberInfo().userId);
            httpParams.put("myUserId",session.getOrgMemberInfo().userId);
            httpParams.put("memberId",session.getOrgMemberInfo().firstName);
            httpParams.put("needClustered",true);

            JSONObject res = null;
            try {
                res = VedantuWebUtils.getJSONData(url, VedantuWebUtils.GET_REQ, httpParams);
                Log.e("NotificationAsyncTask", "do in background");

            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            } finally {
                error = checkForError(res);
            }

            if (isCancelled()) {
                Log.i(TAG, "task canceled by user");
                pDialog.dismiss();
                return null;
            }


            return res;

        }
        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);

            if(jsonObject != null) {

                try {
                    JSONObject result = jsonObject.getJSONObject("result");

                    if (result != null) {
                        NewsFeed newsFeed = new NewsFeed();

                        JSONArray list = result.getJSONArray("list");
                        Log.e("result list", ".." + list.toString());
                        if(list != null && list.length()>0) {
                            for (int i = 0; i < list.length(); i++) {
                                JSONObject jsonObject1 = (list.getJSONObject(i).getJSONArray("clusteredNews")).getJSONObject(0);
                                String actionType = jsonObject1.getJSONObject("info").getString("actionType");
                                Log.e("result object", ".." + jsonObject1.toString());
                                Log.d("action type", ".." + actionType);

                                String notificationString = newsFeed.process(null, list.getJSONObject(i), actionType, true, session);
                                Log.e("result String", ".." + notificationString);
                                JSONObject src = jsonObject1.getJSONObject("src");
                               String srcType = src.getString("type");
                                Log.e("src type", ".." + srcType);
                                Actor actor = new Actor();
                                actor.dateTime = jsonObject1.getString("time");
                                actor.id = jsonObject1.getString("newsFeedId");
                                actor.message = notificationString;
                                actor.type = srcType;
                                actor.srcObject = src;

                                notificationsList.add(actor);
                            }
                        }
                        if(!result.getString("totalHits").equalsIgnoreCase("10")){
                            itemsAdapter.removeFooter();
                        }
                        itemsAdapter.notifyDataSetChanged();

                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            pDialog.dismiss();
            if(notificationsList != null && notificationsList.size()>0){
                noNotificationTextView.setVisibility(View.GONE);
            }else{
                noNotificationTextView.setVisibility(View.VISIBLE);
            }

        }
    }
    public void loadMore(int size, String activityId){
        new GetNotificationsAsyncTask(SessionManager.getInstance(getActivity()), null, "getOlderNotifcations", activityId, size, "OLD").executeTask(false);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.profile, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action buttons
        switch (item.getItemId()) {
            case R.id.action_logout:
                session.logoutUser();
                return true;
            case R.id.action_about:
                Intent intent = new Intent(m_cObjNhanceBaseActivity, AboutActivity.class);
                startActivity(intent);
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
