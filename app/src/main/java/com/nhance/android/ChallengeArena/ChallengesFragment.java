package com.nhance.android.ChallengeArena;


import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.nhance.android.ChallengeArena.model.ViewPagerTab;
import com.nhance.android.R;
import com.nhance.android.activities.baseclasses.NhanceBaseFragment;
import com.nhance.android.assignment.StudentPerformance.NetUtils;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.utils.FontUtils;
import com.nhance.android.utils.VedantuWebUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ChallengesFragment extends NhanceBaseFragment {
    private ProgressDialog loading;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private LinearLayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;

    private ArrayList<ViewPagerTab> tabsList = new ArrayList<>();

    private List<ChallengeItem> challengeItemList;
    private SessionManager session;
    private String mParam1;
    private String mParam2;


    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ArrayAdapter<String> channelspinner_adapter;


    private ArrayList<String> channelspinner_list = new ArrayList<>();


    private Spinner channelspinner;
    private String channelId;
    HashMap<String, String> channelname;

    private JSONObject result, result2, facet, ac_count, cc_count, yc_count;

    private int ac_count1, cc_count1, yc_count1;
    private JSONArray list;
    private String name;
    private String channel_id;
    private TextView active_challenge_count;

    private TextView closed_challenge_count;
    private TextView your_challenge_count;

    private ImageView activechallenge_cardview1;
    private CardView activechallenge_cardview;

    private ImageView closedchallenge_cardview1;
    private CardView closedchallenge_cardview;

    private ImageView yourchallenge_cardview1;
    private CardView yourchallenge_cardview;
    private ChallengesFragment fragmentTwo;
    private TextView your_challenge, closed_challenge, active_challenge;


    // TODO: Rename and change types and number of parameters
    public ChallengesFragment newInstance(String param1, String param2) {
        ChallengesFragment fragment = new ChallengesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        // fragment.setArguments(args);
        setHasOptionsMenu(true);
        return fragment;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        session = SessionManager.getInstance(getActivity());


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.challenge_main, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayoutmsg);

        channelspinner = (Spinner) view.findViewById(R.id.channel_spinner);
        active_challenge_count = (TextView) view.findViewById(R.id.activechallenge_count);
        closed_challenge_count = (TextView) view.findViewById(R.id.closedchallenge_count);
        your_challenge_count = (TextView) view.findViewById(R.id.yourchallenge_count);
        activechallenge_cardview1 = (ImageView) view.findViewById(R.id.activecard_view1);
       activechallenge_cardview = (CardView) view.findViewById(R.id.activecard_view);
        closedchallenge_cardview1 = (ImageView) view.findViewById(R.id.card_view_closedchallenge1);
        closedchallenge_cardview = (CardView) view.findViewById(R.id.card_view_closedchallenge);
        yourchallenge_cardview1 = (ImageView) view.findViewById(R.id.card_view_yourchallenge1);
        yourchallenge_cardview = (CardView) view.findViewById(R.id.card_view_yourchallenge);
        your_challenge = (TextView) view.findViewById(R.id.your_challenge);
        closed_challenge = (TextView) view.findViewById(R.id.closed_challenge);
        active_challenge = (TextView) view.findViewById(R.id.active_challenge);

        FontUtils.setTypeface(active_challenge_count, FontUtils.FontTypes.ROBOTO_LIGHT);
        FontUtils.setTypeface(closed_challenge_count, FontUtils.FontTypes.ROBOTO_LIGHT);
        FontUtils.setTypeface(your_challenge_count, FontUtils.FontTypes.ROBOTO_LIGHT);
        FontUtils.setTypeface(your_challenge, FontUtils.FontTypes.ROBOTO_LIGHT);
        FontUtils.setTypeface(closed_challenge, FontUtils.FontTypes.ROBOTO_LIGHT);
        FontUtils.setTypeface(active_challenge, FontUtils.FontTypes.ROBOTO_LIGHT);


        channelname = new HashMap<String, String>();
        channelspinner_list = new ArrayList<String>();
        channelspinner_list.add("All Channels");
        channelspinner_adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, channelspinner_list);
        channelspinner.setAdapter(channelspinner_adapter);


        //All subject Spinner onclick
        channelspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {

                String type = channelspinner.getSelectedItem().toString().trim();

                if (type.equalsIgnoreCase("All Channels")) {
                    channelId = null;
                } else {
                    channelId = channelname.get(channelspinner.getSelectedItem().toString().trim());

                    Log.e("ggg", "..." + channelId);


                }
                new GetChallengessAsyncTask().execute();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }


        });


        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {


                if (NetUtils.isOnline(getActivity())) {
                    new GetchannelsAsyncTask().execute();
                    new GetChallengessAsyncTask().execute();
                } else {
                    Toast.makeText(getActivity(), R.string.no_internet_msg, Toast.LENGTH_SHORT).show();
                }


            }

        });


        if (NetUtils.isOnline(getActivity())) {
            new GetchannelsAsyncTask().execute();

        } else {
            Toast.makeText(getActivity(), R.string.no_internet_msg, Toast.LENGTH_SHORT).show();
        }


        return view;

    }


    public class GetchannelsAsyncTask extends AsyncTask<JSONObject, JSONObject, JSONObject> {
        private ProgressDialog loading;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            loading = new ProgressDialog(getActivity());
            loading.setMessage("Please wait...");
            loading.show();
        }

        @Override
        protected JSONObject doInBackground(JSONObject... jsonObjects) {
            String url = session.getApiUrl("getChannels");

            JSONObject jsonRes = null;

            HashMap<String, Object> httpParams = new HashMap<String, Object>();

            httpParams.put("callingApp", "TabApp");
            httpParams.put("callingUserId", session.getOrgMemberInfo().userId);
            httpParams.put("userRole", session.getOrgMemberInfo().profile);
            httpParams.put("userId", session.getOrgMemberInfo().userId);
            httpParams.put("contentSrc.id", session.getOrgMemberInfo().orgId);
            httpParams.put("myUserId", session.getOrgMemberInfo().userId);
            httpParams.put("contentSrc.type", "ORGANIZATION");
            httpParams.put("orgId", session.getOrgMemberInfo().orgId);
            httpParams.put("memberId", session.getOrgMemberInfo().userId);
            httpParams.put("resultType", "ALL");

            session.addSessionParams(httpParams);


            try {
                jsonRes = VedantuWebUtils.getJSONData(url, VedantuWebUtils.GET_REQ, httpParams);
                Log.e("jsonrespgetchannels", "......." + jsonRes);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return jsonRes;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            loading.dismiss();
            if (jsonObject != null) {
                try {
                     channelspinner_list.clear();
                    result = jsonObject.getJSONObject("result");
                    list = result.getJSONArray("list");

                    Log.e("list of response", "......." + list);


                    for (int i = 0; i < list.length(); i++) {
                        JSONObject chlist = list.getJSONObject(i);
                        channel_id = chlist.getString("id");
                        Log.e("Id", "......." + channel_id);


                        name = chlist.getString("name");
                        channelspinner_list.add(name);
                        Log.e("responseList", "......." + channelspinner_list.toString());
                        channelname.put(name, channel_id);

                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


        }

    }


    //Get the challenges
    public class GetChallengessAsyncTask extends AsyncTask<JSONObject, JSONObject, JSONObject> {
        private ProgressDialog loading;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            loading = new ProgressDialog(getActivity());
            loading.setMessage("Please wait...");
            loading.show();
        }

        @Override
        protected JSONObject doInBackground(JSONObject... jsonObjects) {
            String url = session.getApiUrl("getChallenges");

            JSONObject jsonRes = null;

            HashMap<String, Object> httpParams = new HashMap<String, Object>();

            httpParams.put("callingApp", "TabApp");
            httpParams.put("callingUserId", session.getOrgMemberInfo().userId);
            httpParams.put("userRole", session.getOrgMemberInfo().profile);
            httpParams.put("userId", session.getOrgMemberInfo().userId);
            httpParams.put("contentSrc.id", session.getOrgMemberInfo().orgId);
            httpParams.put("myUserId", session.getOrgMemberInfo().userId);
            httpParams.put("contentSrc.type", "ORGANIZATION");
            httpParams.put("orgId", session.getOrgMemberInfo().orgId);
            httpParams.put("memberId", session.getOrgMemberInfo().userId);
            httpParams.put("resultType", "ALL");
            httpParams.put("orderBy", "timeCreated");
            httpParams.put("status", "ACTIVE");
            httpParams.put("type", "FIXED_TIME");
            httpParams.put("size", 10);
            httpParams.put("start", 0);
            httpParams.put("year", 2017);
            if (channelId != null && !channelId.isEmpty()) {

                Log.e("channelid ", "......." + channelId);
                httpParams.put("channelId", channelId);
            }

            session.addSessionParams(httpParams);

            try {
                jsonRes = VedantuWebUtils.getJSONData(url, VedantuWebUtils.GET_REQ, httpParams);
                Log.e("channelsdeatils", "......." + jsonRes);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return jsonRes;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            loading.dismiss();
            if (jsonObject != null) {
                try {
                    tabsList.clear();
                    result2 = jsonObject.getJSONObject("result");
                    facet = result2.getJSONObject("facet");
                    Log.e("summary of response", "......." + facet);

                    ac_count1 = 0;
                    cc_count1 = 0;
                    yc_count1 = 0;
                    ac_count = facet.getJSONObject("pointStatFacet");
                    Log.e("totoal_value", "......." + ac_count1);
                    cc_count = facet.getJSONObject("statusFacet");
                    ac_count1 = cc_count.optInt("active");
                    cc_count1 = cc_count.optInt("ended");
                    Log.e("closedcount", "......." + cc_count1);
                    yc_count = facet.getJSONObject("attemptFacet");
                    yc_count1 = yc_count.optInt("count");
                    Log.e("yourscount", "......." + yc_count1);

                    active_challenge_count.setText(String.valueOf(ac_count1));
                    closed_challenge_count.setText(String.valueOf(cc_count1));
                    your_challenge_count.setText(String.valueOf(yc_count1));
                    mSwipeRefreshLayout.setRefreshing(false);
             /*   tabsList.add(new ViewPagerTab("Active Challenges"));
                tabsList.add(new ViewPagerTab("Closed Challenges"));
                tabsList.add(new ViewPagerTab("Your Challenges"));*/

                    //adapter1.notifyDataSetChanged();

                } catch (JSONException e1) {
                    e1.printStackTrace();
                }

            }


            activechallenge_cardview
                    .setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {

                    if (String.valueOf(ac_count1).equalsIgnoreCase("0")) {

                        Toast.makeText(getActivity(), "No Chalenges Available.", Toast.LENGTH_SHORT).show();

                    } else {

                        Intent intent = new Intent(getActivity(), ActivieChallengesActivity.class);
                        intent.putExtra("channel_id", channelId);
                        startActivity(intent);
                    }
                }
            });

            closedchallenge_cardview.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {

                    if (String.valueOf(cc_count1).equalsIgnoreCase("0")) {

                        Toast.makeText(getActivity(), "No Chalenges Available.", Toast.LENGTH_SHORT).show();

                    } else {


                        Intent intent = new Intent(getActivity(), ClosedChallengesActivity.class);
                        intent.putExtra("channel_id", channelId);
                        startActivity(intent);
                    }

                }
            });

            yourchallenge_cardview.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    if (String.valueOf(yc_count1).equalsIgnoreCase("0")) {

                        Toast.makeText(getActivity(), "No Chalenges Available.", Toast.LENGTH_SHORT).show();

                    } else {

                        Intent intent = new Intent(getActivity(), YoursChallengesCountActivity.class);
                        intent.putExtra("channel_id", channelId);
                        Log.e("channel_id", "...." + channelId);
                        startActivity(intent);
                    }
                }
            });

        }

    }

}

