package com.nhance.android.ChallengeArena;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.nhance.android.R;
import com.nhance.android.assignment.StudentPerformance.NetUtils;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.utils.FontUtils;
import com.nhance.android.utils.VedantuWebUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class YoursChallengesActivity extends AppCompatActivity {
    private ProgressDialog loading;
    private android.content.Context context;
    private RecyclerView mRecyclerView;
    private TextView empty_yc_list;
    private SessionManager session;
    private ClosedChallengesAdapter ycAdapter;
    private List<ChallengeItem> challengeItemList;
    private JSONObject result, result2, facet,ac_count, yc_count;
    private int ac_count1;
    private JSONArray list, boards;
    private ArrayAdapter<String> ycspinadapter;
    private ArrayAdapter<String> yc_re_pop_Adapter;
    private Spinner ycspinner,yc_recent_popular;
    private ArrayList<String> spinsubjectlist = new ArrayList<>();
    private  String sub_name,id;
    private String mParam1;  private String mParam2;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    ArrayList<HashMap<String, String>> subnameaddlist;
    ArrayList<HashMap<String, String>> topicnameaddlist;
    HashMap<String, String> channelname;
    private String subjectId;
    private String subId;
    private JSONObject board_obj;
    private String subName;
    private String subType;
    private String topicName,topicId;
    private String topicType;
    private String channelId;

        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_your_closed_challenges);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      //  getSupportActionBar().setIcon(R.drawable.ic_launcher);
        getSupportActionBar().setDisplayShowCustomEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Closed Challenges");
        session = SessionManager.getInstance(this);
        channelId=getIntent().getStringExtra("channel_id");
        challengeItemList = new ArrayList<>();
        subnameaddlist=new ArrayList<>();
        topicnameaddlist=new ArrayList<>();
        channelname = new HashMap<String, String>();
        mRecyclerView = (RecyclerView) findViewById(R.id.yc_recyclerview);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayoutmsg);
        empty_yc_list = (TextView) findViewById(R.id.empty_yc_list);

            FontUtils.setTypeface(empty_yc_list, FontUtils.FontTypes.ROBOTO_LIGHT);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(layoutManager);
        ycspinner = (Spinner) findViewById(R.id.ycspin_sublist);
        yc_recent_popular = (Spinner) findViewById(R.id.yc_recent_popular);
        String[] ac_recent_array = {"Most Recent", "Most Popular"};
        spinsubjectlist = new ArrayList<String>();
        ycspinadapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, spinsubjectlist);
        ycspinner.setAdapter(ycspinadapter);

        ycspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {

                String type = ycspinner.getSelectedItem().toString().trim();
                if (type.equalsIgnoreCase("All Subjects")) {
                    // subjectId = null;
                    Log.e("red","....."+result2);
                    try {
                        challengeItemList.clear();
                        challengeItemList.removeAll(challengeItemList);
                        topicnameaddlist.clear();
                        topicnameaddlist.removeAll(topicnameaddlist);
                        subnameaddlist.clear();
                        subnameaddlist.removeAll(subnameaddlist);

                        list = result2.getJSONArray("list");
                        Log.e("list.....","...."+list);

                        for (int i = 0; i < list.length(); i++) {
                            JSONObject chlist = list.getJSONObject(i);
                            ChallengeItem channelitem = new ChallengeItem();
                            channelitem.channelname = chlist.getString("name");
                            Log.e("name", "......." + channelitem.channelname);
                            channelitem.leveltext = chlist.getString("difficulty");
                            channelitem.attemptstext = chlist.getInt("attempts");
                            channelitem.pointstext = chlist.getInt("maxBid");
                            channelitem.challenge_time = chlist.getLong("duration");
                            channelitem.challengeclosed_time = chlist.getString("lastUpdated");
                            channelitem.timeCreated = chlist.getLong("timeCreated");
                            JSONObject object_point = chlist.getJSONObject("info");
                            channelitem.totalPoint = object_point.getString("totalPoint");
                            channelitem.challengeId=object_point.getString("challengeId");
                            HashMap<String, String> subject_name_list = new HashMap<>();
                            boards = chlist.getJSONArray("boards");
                            for (int j = 0; j < boards.length(); j++) {
                                JSONObject board_obj = boards.getJSONObject(j);
                                if (board_obj.getString("type").equalsIgnoreCase("COURSE")) {
                                    subName = board_obj.getString("name");
                                    subId = board_obj.getString("id");
                                    subType = board_obj.getString("type");
                                    subject_name_list.put("subName", subName);
                                    subject_name_list.put("subId", subId);
                                    subject_name_list.put("subType", subType);
                                    subnameaddlist.add(subject_name_list);
                                    Log.e("subnameaddlist", "...." + subnameaddlist.toString());
                                }
                                if (board_obj.getString("type").equalsIgnoreCase("TOPIC")) {
                                    topicName = board_obj.getString("name");
                                    topicId = board_obj.getString("id");
                                    topicType = board_obj.getString("type");
                                    subject_name_list.put("topicName", topicName);
                                    subject_name_list.put("topicId", topicId);
                                    subject_name_list.put("topicType", topicType);
                                    topicnameaddlist.add(subject_name_list);
                                    Log.e("topicnameaddlist", "...." + topicnameaddlist.toString());
                                }
                            }
                            JSONArray entities = chlist.getJSONArray("entities");
                            for (int k = 0; k < entities.length(); k++) {
                                JSONObject entities_objects = entities.getJSONObject(k);
                                channelitem.id = entities_objects.getString("id");
                                Log.e("entities_id", "....." + channelitem.id);
                            }
                            challengeItemList.add(channelitem);
                        }


                    }catch (Exception e){

                        e.printStackTrace();
                    }

                    if(challengeItemList != null && challengeItemList.size()>0){
                        empty_yc_list.setVisibility(View.GONE);
                    }else{
                        empty_yc_list.setVisibility(View.VISIBLE);
                    }


                    ycAdapter = new ClosedChallengesAdapter(YoursChallengesActivity.this, challengeItemList,subnameaddlist,topicnameaddlist);
                    mRecyclerView.setAdapter(ycAdapter);
                    mSwipeRefreshLayout.setRefreshing(false);
                    ycAdapter.notifyDataSetChanged();


                } else {
                    // subjectId = channelname.get(ycspinner.getSelectedItem().toString().trim());
                    try {
                        subjectId = channelname.get(type.trim());
                        Log.e("sublist","..."+subjectId);
                        challengeItemList.clear();
                        challengeItemList.removeAll(challengeItemList);
                        topicnameaddlist.clear();
                        topicnameaddlist.removeAll(topicnameaddlist);
                        subnameaddlist.clear();
                        subnameaddlist.removeAll(subnameaddlist);
                        list = result2.getJSONArray("list");
                        for (int i = 0; i < list.length(); i++) {
                            JSONObject chlist = list.getJSONObject(i);
                            ChallengeItem channelitem = new ChallengeItem();
                            channelitem.channelname = chlist.getString("name");
                            Log.e("name", "......." + channelitem.channelname);
                            channelitem.leveltext = chlist.getString("difficulty");
                            channelitem.attemptstext = chlist.getInt("attempts");
                            channelitem.pointstext = chlist.getInt("maxBid");
                            channelitem.challenge_time = chlist.getLong("duration");
                            channelitem.challengeclosed_time = chlist.getString("lastUpdated");
                            channelitem.timeCreated = chlist.getLong("timeCreated");

                            JSONObject object_point = chlist.getJSONObject("info");

                            channelitem.totalPoint = object_point.getString("totalPoint");
                            channelitem.challengeId=object_point.getString("challengeId");

                            HashMap<String, String> subject_name_list = new HashMap<>();
                            boards = chlist.getJSONArray("boards");
                            for (int j = 0; j < boards.length(); j++) {
                                board_obj = boards.getJSONObject(j);
                                if (board_obj.getString("type").equalsIgnoreCase("COURSE")) {
                                    subName = board_obj.getString("name");
                                    subId = board_obj.getString("id");
                                    subType = board_obj.getString("type");
                                    subject_name_list.put("subName", subName);
                                    subject_name_list.put("subId", subId);
                                    subject_name_list.put("subType", subType);
                                    if (subjectId.equalsIgnoreCase(board_obj.getString("id"))) {
                                        subnameaddlist.add(subject_name_list);
                                        Log.e("subnamelist","..."+subnameaddlist.toString());
                                        Log.e("subnamelist","..."+subnameaddlist.size());
                                        challengeItemList.add(channelitem);

                                    }
                                }
                                if (board_obj.getString("type").equalsIgnoreCase("TOPIC")) {
                                    topicName = board_obj.getString("name");
                                    topicId = board_obj.getString("id");
                                    topicType = board_obj.getString("type");
                                    subject_name_list.put("topicName", topicName);
                                    subject_name_list.put("topicId", topicId);
                                    subject_name_list.put("topicType", topicType);
                                    topicnameaddlist.add(subject_name_list);

                                }

                            }


                            JSONArray entities = chlist.getJSONArray("entities");

                            for (int k = 0; k < entities.length(); k++) {
                                JSONObject entities_objects = entities.getJSONObject(k);
                                channelitem.id = entities_objects.getString("id");
                                Log.e("entities_idhhhhhhh", "....." + channelitem.id);
                            }



                            channelitem.subject_name_list=subject_name_list;
//
//if (board_obj.getString("type").equalsIgnoreCase("COURSE")) {
//    if (subjectId.equalsIgnoreCase(board_obj.getString("id").trim())) {
//
//
//        challengeItemList.add(channelitem);
//;
//
//    }
//
//
//}

                        }


                    }catch (Exception e){

                        e.printStackTrace();
                    }

                    if(challengeItemList != null && challengeItemList.size()>0){
                        empty_yc_list.setVisibility(View.GONE);
                    }else{
                        empty_yc_list.setVisibility(View.VISIBLE);
                    }


                    ycAdapter = new ClosedChallengesAdapter(YoursChallengesActivity.this, challengeItemList,subnameaddlist,topicnameaddlist);
                    mRecyclerView.setAdapter(ycAdapter);

                    mSwipeRefreshLayout.setRefreshing(false);
                    ycAdapter.notifyDataSetChanged();


                }



            }


            public void onNothingSelected(AdapterView<?> parent) {

            }
        });








        yc_re_pop_Adapter= new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,ac_recent_array);
        yc_re_pop_Adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        yc_recent_popular.setAdapter(yc_re_pop_Adapter);




        yc_recent_popular.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {




                String type = yc_recent_popular.getSelectedItem().toString().trim();
                if (type.equalsIgnoreCase("Most Popular")) {
                    new ClosedYourchallengesMostPopular().execute();
                    // Toast.makeText(getActivity(), "Most Popular Clicked", Toast.LENGTH_SHORT).show();
                } else {
                    new ClosedYourchallenges().execute();
                    // Toast.makeText(getActivity(), "Most Recent Clicked", Toast.LENGTH_SHORT).show();

                }



            }


            public void onNothingSelected(AdapterView<?> parent) {

            }
        });








        if(NetUtils.isOnline(YoursChallengesActivity.this)) {
            new ClosedYourchallenges().execute();

            new yc_GetBoards().execute();

        } else{
            Toast.makeText(this, R.string.no_internet_msg, Toast.LENGTH_SHORT).show();
        }


        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {


                if(NetUtils.isOnline(YoursChallengesActivity.this)) {
                    new ClosedYourchallenges().execute();

                    new yc_GetBoards().execute();

                } else{
                    Toast.makeText(YoursChallengesActivity.this, R.string.no_internet_msg, Toast.LENGTH_SHORT).show();
                }


            }

        });



    }




    public class ClosedYourchallengesMostPopular extends AsyncTask<JSONObject, JSONObject, JSONObject> {
        private ProgressDialog loading;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading=new ProgressDialog(YoursChallengesActivity.this);
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
            httpParams.put("resultType", "ATTEMPTED");
            httpParams.put("orderBy", "mostpopular");
            httpParams.put("status", "ENDED");
            httpParams.put("type", "FIXED_TIME");
            httpParams.put("channelId",channelId);
            httpParams.put("size", "10"+1);
            httpParams.put("start", 0);
            httpParams.put("year", 2017);
            Log.e("Httpparams", "......." +   httpParams);

            session.addSessionParams(httpParams);

            try {
                jsonRes = VedantuWebUtils.getJSONData(url, VedantuWebUtils.GET_REQ, httpParams);
                Log.e("Closedchallengesss", "......." + jsonRes);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return jsonRes;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);

            loading.dismiss();
            if (jsonObject!=null) {
                try {
                    challengeItemList.clear();
                    challengeItemList.removeAll(challengeItemList);
                    subnameaddlist.clear();
                    subnameaddlist.removeAll(subnameaddlist);
                    topicnameaddlist.clear();
                    topicnameaddlist.removeAll(topicnameaddlist);
                    result2 = jsonObject.getJSONObject("result");
                    facet = result2.getJSONObject("facet");
                    Log.e("summary of response", "......." + facet);

                    ac_count1 = 0;
                    ac_count = facet.getJSONObject("pointStatFacet");


                    Log.e("totoal_value", "......." + ac_count1);

                    yc_count = facet.getJSONObject("statusFacet");
                    ac_count1 = yc_count.optInt("active");
                    Log.e("aycount", ".." + ac_count1);

                    filterlist(result2);


                    Log.e("challengeItemList", "......." + challengeItemList);
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }


            }

        }

    }






    //Get the challenges
    public class ClosedYourchallenges extends AsyncTask<JSONObject, JSONObject, JSONObject> {
        private ProgressDialog loading;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading=new ProgressDialog(YoursChallengesActivity.this);
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
            httpParams.put("resultType", "ATTEMPTED");
            httpParams.put("orderBy", "timeCreated");
            httpParams.put("status", "ENDED");
            httpParams.put("type", "FIXED_TIME");
            httpParams.put("channelId",channelId);
            httpParams.put("size", "10"+1);
            httpParams.put("start", 0);
            httpParams.put("year", 2017);
            Log.e("Httpparams", "......." +   httpParams);

            session.addSessionParams(httpParams);

            try {
                jsonRes = VedantuWebUtils.getJSONData(url, VedantuWebUtils.GET_REQ, httpParams);
                Log.e("Closedchallengesss", "......." + jsonRes);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return jsonRes;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);

            loading.dismiss();
            if (jsonObject!=null) {
                try {
                    challengeItemList.clear();
                    challengeItemList.removeAll(challengeItemList);
                    subnameaddlist.clear();
                    subnameaddlist.removeAll(subnameaddlist);
                    topicnameaddlist.clear();
                    topicnameaddlist.removeAll(topicnameaddlist);
                    result2 = jsonObject.getJSONObject("result");
                    facet = result2.getJSONObject("facet");
                    Log.e("summary of response", "......." + facet);

                    ac_count1 = 0;
                    ac_count = facet.getJSONObject("pointStatFacet");


                    Log.e("totoal_value", "......." + ac_count1);

                    yc_count = facet.getJSONObject("statusFacet");
                    ac_count1 = yc_count.optInt("active");
                    Log.e("aycount", ".." + ac_count1);




                    filterlist(result2);


                    Log.e("challengeItemList", "......." + challengeItemList);
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }


            }

        }

    }

    private void filterlist(JSONObject result2) {


        Log.e("redddddd","....."+result2);

        try {

            list = result2.getJSONArray("list");
            for (int i = 0; i < list.length(); i++) {
                JSONObject chlist = list.getJSONObject(i);
                ChallengeItem channelitem = new ChallengeItem();
                channelitem.channelname = chlist.getString("name");
                Log.e("name", "......." + channelitem.channelname);
                channelitem.leveltext = chlist.getString("difficulty");
                channelitem.attemptstext = chlist.getInt("attempts");
                channelitem.pointstext = chlist.getInt("maxBid");
                channelitem.challenge_time = chlist.getLong("duration");
                channelitem.challengeclosed_time = chlist.getString("lastUpdated");
                channelitem.timeCreated = chlist.getLong("timeCreated");

                JSONObject object_point = chlist.getJSONObject("info");

                channelitem.totalPoint = object_point.getString("totalPoint");
                channelitem.challengeId=object_point.getString("challengeId");
                HashMap<String, String> subject_name_list = new HashMap<>();
                boards = chlist.getJSONArray("boards");
                for (int j = 0; j < boards.length(); j++) {
                    JSONObject board_obj = boards.getJSONObject(j);
                    if (board_obj.getString("type").equalsIgnoreCase("COURSE")) {
                        subName = board_obj.getString("name");
                        subId = board_obj.getString("id");
                        subType = board_obj.getString("type");
                        subject_name_list.put("subName", subName);
                        subject_name_list.put("subId", subId);
                        subject_name_list.put("subType", subType);

                        subnameaddlist.add(subject_name_list);


                        Log.e("subnameaddlist", "...." + subnameaddlist.toString());

                    }

                    if (board_obj.getString("type").equalsIgnoreCase("TOPIC")) {


                        topicName = board_obj.getString("name");
                        topicId = board_obj.getString("id");
                        topicType = board_obj.getString("type");


                        subject_name_list.put("topicName", topicName);
                        subject_name_list.put("topicId", topicId);
                        subject_name_list.put("topicType", topicType);

                        topicnameaddlist.add(subject_name_list);


                        Log.e("topicnameaddlist", "...." + topicnameaddlist.toString());

                    }

                }


                JSONArray entities = chlist.getJSONArray("entities");

                for (int k = 0; k < entities.length(); k++) {


                    JSONObject entities_objects = entities.getJSONObject(k);

                    channelitem.id = entities_objects.getString("id");


                    Log.e("entities_id", "....." + channelitem.id);

                }


                challengeItemList.add(channelitem);
            }


        }catch (Exception e){

            e.printStackTrace();
        }

        if(challengeItemList != null && challengeItemList.size()>0){
            empty_yc_list.setVisibility(View.GONE);
        }else{
            empty_yc_list.setVisibility(View.VISIBLE);
        }


        ycAdapter = new ClosedChallengesAdapter(YoursChallengesActivity.this, challengeItemList,subnameaddlist,topicnameaddlist);
        mRecyclerView.setAdapter(ycAdapter);

        mSwipeRefreshLayout.setRefreshing(false);
        ycAdapter.notifyDataSetChanged();




    }

    //Get the Boards
    public class yc_GetBoards extends AsyncTask<JSONObject, JSONObject, JSONObject> {
        private ProgressDialog loading;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            loading=new ProgressDialog(YoursChallengesActivity.this);
            loading.setMessage("Please wait...");
            loading.show();
        }

        @Override
        protected JSONObject doInBackground(JSONObject... jsonObjects) {
            String url = session.getApiUrl("getBoards");


            JSONObject jsonRes = null;

            HashMap<String, Object> httpParams = new HashMap<String, Object>();

            httpParams.put("callingApp", "TabApp");
            httpParams.put("callingUserId", session.getOrgMemberInfo().userId);
            httpParams.put("userRole", session.getOrgMemberInfo().profile);
            httpParams.put("userId", session.getOrgMemberInfo().userId);
            httpParams.put("myUserId", session.getOrgMemberInfo().userId);
            httpParams.put("orgId", session.getOrgMemberInfo().orgId);
            httpParams.put("ownerId", session.getOrgMemberInfo().orgId);
            httpParams.put("memberId", session.getOrgMemberInfo().userId);
            httpParams.put("ecordState", "ACTIVE");
            httpParams.put("type", "COURSE");
            httpParams.put("context", "ORG");

            session.addSessionParams(httpParams);

            try {
                jsonRes = VedantuWebUtils.getJSONData(url, VedantuWebUtils.GET_REQ, httpParams);
                Log.e("getboards", "......." + jsonRes);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return jsonRes;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);

            loading.dismiss();

            if (jsonObject!=null) {
                try {
                    spinsubjectlist.clear();
                    result = jsonObject.getJSONObject("result");
                    list = result.getJSONArray("list");

                    Log.e("list of response", "......." + list);
                    spinsubjectlist.add("All Subjects");

                    for (int i = 0; i < list.length(); i++) {
                        JSONObject chlist = list.getJSONObject(i);
                        id = chlist.getString("id");
                        Log.e("Id", "......." + id);

                        sub_name = chlist.getString("name");
                        spinsubjectlist.add(sub_name);
                        channelname.put(sub_name, id);
                        Log.e("responseList", "......." + spinsubjectlist.toString());
                    }

                    ycspinadapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            ycAdapter = new ClosedChallengesAdapter(YoursChallengesActivity.this, challengeItemList,subnameaddlist,topicnameaddlist);
            mRecyclerView.setAdapter(ycAdapter);
            mSwipeRefreshLayout.setRefreshing(false);

            ycAdapter.notifyDataSetChanged();


        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;

            default:

                return super.onOptionsItemSelected(item);
        }
    }










    public class ClosedChallengesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {
        private Activity context;
        List<ChallengeItem> data = Collections.emptyList();
        private static final int MINUTES_IN_AN_HOUR = 60;
        private static final int SECONDS_IN_A_MINUTE = 60;
        private SessionManager session;
        private int TYPE_FOOTER=1;
        private   MyHolder myHolder;
        private   ChallengeItem current;

        private  ArrayList<HashMap<String, String>> subnameaddlist;
        private  ArrayList<HashMap<String, String>> topicnameaddlist;

        // create constructor to innitilize context and data sent from MainActivity
        public ClosedChallengesAdapter(Activity context, List<ChallengeItem> data,ArrayList<HashMap<String, String>> subnameaddlist,ArrayList<HashMap<String, String>> topicnameaddlist) {

            this.context = context;
            this.data = data;
            session = SessionManager.getInstance(context);

            this.subnameaddlist=subnameaddlist;
            this.topicnameaddlist=topicnameaddlist;
        }


        // Inflate the layout when viewholder created
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            //View view = inflate(R.layout.closedchallenge_list_row, parent, false);

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.yourchallenge_list_row, parent, false);

            MyHolder holder = new MyHolder(view);
            return holder;



        }



        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            Map<String, String> subname1 = new HashMap<String, String>();
            CountDownTimer countDownTimer;
              myHolder = (MyHolder) holder;
             current = data.get(position);
            myHolder.channelname.setText(current.channelname);
            FontUtils.setTypeface(myHolder.channelname, FontUtils.FontTypes.ROBOTO_LIGHT);



            subname1 = current.subname1.get(current.channelname);
            HashMap<String, String> challenge_course;
            challenge_course = subnameaddlist.get(position);
            HashMap<String, String> challenge_topic;
            challenge_topic = topicnameaddlist.get(position);
            myHolder.actopictext.setText(challenge_topic.get("topicName"));
            FontUtils.setTypeface(myHolder.actopictext, FontUtils.FontTypes.ROBOTO_LIGHT);
            myHolder.acsubjecttext.setText(challenge_course.get("subName"));
            FontUtils.setTypeface(myHolder.acsubjecttext, FontUtils.FontTypes.ROBOTO_LIGHT);
            myHolder.attemptstext.setText(String.valueOf(current.attemptstext));
            FontUtils.setTypeface(myHolder.attemptstext, FontUtils.FontTypes.ROBOTO_LIGHT);
            myHolder.challenge_time.setText(timeConversion((long) (current.challenge_time)));
            FontUtils.setTypeface(myHolder.challenge_time, FontUtils.FontTypes.ROBOTO_LIGHT);
            myHolder.leveltext.setText(current.leveltext);
            FontUtils.setTypeface(myHolder.leveltext, FontUtils.FontTypes.ROBOTO_LIGHT);
             myHolder.border_layout.setVisibility(View.GONE);
             myHolder.upArrow.setVisibility(View.GONE);
            myHolder.downArrow.setVisibility(View.VISIBLE);

            if (current.totalPoint!=null && current.totalPoint.equalsIgnoreCase("0")){
                myHolder.status_img.setBackgroundResource(R.drawable.unlike);
                myHolder.points_table.setBackgroundColor(getResources().getColor(R.color.red));
                myHolder.pointstext.setText(current.totalPoint+"  Points Earned");
                FontUtils.setTypeface(myHolder.pointstext, FontUtils.FontTypes.ROBOTO_LIGHT);

            }else {
                myHolder.status_img.setBackgroundResource(R.drawable.cracked);
                myHolder.points_table.setBackgroundColor(getResources().getColor(R.color.green));
                myHolder.pointstext.setText("+ "+current.totalPoint+"  Points Earned");
                FontUtils.setTypeface(myHolder.pointstext, FontUtils.FontTypes.ROBOTO_LIGHT);
            }
            FontUtils.setTypeface(myHolder.your_view_challenge, FontUtils.FontTypes.ROBOTO_LIGHT);

            myHolder.downArrow.setTag(position);
            myHolder.your_view_challenge.setTag(position);


        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class MyHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener,View.OnClickListener {

            TextView your_view_challenge,channelname,challenge_time,leveltext,pointstext,attemptstext,challenge_count,actopictext,acsubjecttext,challengeclosed_time,timeCreated;
            ProgressBar pb;
            RecyclerView lv;
            RelativeLayout ycre1;
            ImageView status_img,upArrow,downArrow;
            RelativeLayout points_table;
            TextView dynamic_hinttaken,dynamic_timetaken,dynamic_pointsearned,exam_status,text_points_earned,point_multiply,text_timetaken,text_hinttaken;

            LinearLayout border_layout;


            public MyHolder(final View itemView) {
                super(itemView);


                channelname = (TextView) itemView.findViewById(R.id.channelname);
              pointstext = (TextView) itemView.findViewById(R.id.pointstext);
                attemptstext = (TextView) itemView.findViewById(R.id.attemptstext);
                leveltext = (TextView) itemView.findViewById(R.id.leveltext);
               // challenge_count = (TextView) itemView.findViewById(R.id.challenge_count);
                actopictext = (TextView) itemView.findViewById(R.id.actopictext);
                acsubjecttext = (TextView) itemView.findViewById(R.id.acsubjecttext);
                challenge_time = (TextView) itemView.findViewById(R.id.challengetime_text);
                your_view_challenge=(TextView)itemView.findViewById(R.id.your_view_challenge);
               challengeclosed_time = (TextView) itemView.findViewById(R.id.challengeclosed_time);
                status_img = (ImageView) itemView.findViewById(R.id.status_img);
                points_table = (RelativeLayout) itemView.findViewById(R.id.points_table);

                lv = (RecyclerView) itemView.findViewById(R.id.acrecyclerview);
                ycre1=(RelativeLayout)itemView.findViewById(R.id.acre);

                exam_status = (TextView) itemView.findViewById(R.id.exam_status);
                text_points_earned = (TextView) itemView.findViewById(R.id.text_points_earned);
                point_multiply = (TextView) itemView.findViewById(R.id.point_multiply);
                text_timetaken = (TextView) itemView.findViewById(R.id.text_timetaken);
                text_hinttaken = (TextView) itemView.findViewById(R.id.text_hinttaken);
                upArrow = (ImageView) itemView.findViewById(R.id.uparraow);
                downArrow = (ImageView) itemView.findViewById(R.id.downarraow);
                border_layout = (LinearLayout) itemView.findViewById(R.id.border_layout);
                dynamic_pointsearned=(TextView) itemView.findViewById(R.id.pointsearned);
                dynamic_timetaken=(TextView) itemView.findViewById(R.id.timetaken);
                dynamic_hinttaken=(TextView) itemView.findViewById(R.id.hinttaken);



                FontUtils.setTypeface(exam_status, FontUtils.FontTypes.ROBOTO_LIGHT);
                FontUtils.setTypeface(text_points_earned, FontUtils.FontTypes.ROBOTO_LIGHT);
                FontUtils.setTypeface(point_multiply, FontUtils.FontTypes.ROBOTO_LIGHT);
                FontUtils.setTypeface(text_timetaken, FontUtils.FontTypes.ROBOTO_LIGHT);
                FontUtils.setTypeface(text_hinttaken, FontUtils.FontTypes.ROBOTO_LIGHT);
                FontUtils.setTypeface(dynamic_pointsearned, FontUtils.FontTypes.ROBOTO_LIGHT);
                FontUtils.setTypeface(dynamic_timetaken, FontUtils.FontTypes.ROBOTO_LIGHT);
                FontUtils.setTypeface(dynamic_hinttaken, FontUtils.FontTypes.ROBOTO_LIGHT);











                your_view_challenge.setOnClickListener(this);

//                new attemptInfo().execute();

                downArrow.setOnClickListener(this);

                upArrow.setOnClickListener(this);






            }


            @Override
            public boolean onLongClick(View view) {
                return false;
            }

            @Override
            public void onClick(View v) {
                 if(v.getId() == R.id.your_view_challenge){
                     int pos = (int) v.getTag();
                     String id = data.get(pos).id;
                     String id1 = data.get(pos).channelname;

                     Intent intent = new Intent(context, YourChallengesViewActivity.class);
                     intent.putExtra("questionId", id);
                     intent.putExtra("channelname",id1);
                     context.startActivity(intent);
                 }

                if(v.getId() == R.id.downarraow){
                    int pos = (int) v.getTag();
                    String id = data.get(pos).challengeId;
                    downArrow.setVisibility(View.GONE);
                    border_layout.setVisibility(View.VISIBLE);
                    upArrow.setVisibility(View.VISIBLE);
                    new AttemptInfo(pos, id).execute();
                }
                if(v.getId() == R.id.uparraow){
                    downArrow.setVisibility(View.VISIBLE);
                    border_layout.setVisibility(View.GONE);
                    upArrow.setVisibility(View.GONE);
                }
            }
        }
        private  String timeConversion(long totalSeconds) {
            long hours = totalSeconds / MINUTES_IN_AN_HOUR / SECONDS_IN_A_MINUTE;
            long minutes = (totalSeconds - (hoursToSeconds((int) hours)))
                    / SECONDS_IN_A_MINUTE;
            long seconds = totalSeconds
                    - ((hoursToSeconds((int) hours)) + (minutesToSeconds((int) minutes)));
            if (hours == 0 ){
                return (minutes +  "m:" + seconds + "s");
            }else

                return (hours + "h:" + minutes +  "m:" + seconds + "s");

        }
        private  int hoursToSeconds(int hours) {
            return hours * MINUTES_IN_AN_HOUR * SECONDS_IN_A_MINUTE;
        }
        private  int minutesToSeconds(int minutes) {
            return minutes * SECONDS_IN_A_MINUTE;
        }
        public  String convertDate(String dateInMilliseconds,String dateFormat) {
            return DateFormat.format(dateFormat, Long.parseLong(dateInMilliseconds)).toString();
        }
        public String getlongtoago(long createdAt) {
            SimpleDateFormat userDateFormat = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
            SimpleDateFormat dateFormatNeeded = new SimpleDateFormat("MM/dd/yyyy HH:MM:SS");

            Date date = null;
            date = new Date(createdAt);
            String crdate1 = dateFormatNeeded.format(date);

            // Date Calculation
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            crdate1 = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(date);

            // get current date time with Calendar()
            Calendar cal = Calendar.getInstance();
            String currenttime = dateFormat.format(cal.getTime());

            Date CreatedAt = null;
            Date current = null;
            try {
                CreatedAt = dateFormat.parse(crdate1);
                current = dateFormat.parse(currenttime);
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // Get msec from each, and subtract.
            long diff = current.getTime() - CreatedAt.getTime();
            long diffSeconds = diff / 1000;
            long diffMinutes = diff / (60 * 1000) % 60;
            long diffHours = diff / (60 * 60 * 1000) % 24;
            long diffDays = diff / (24 * 60 * 60 * 1000);
            int weeks = (int) (diff/ (1000*60*60*24*7));
            double month = diffDays /30;
            double year = diffDays / 365;

            Log.e("weeks.."+weeks+"months..."+month, "years.."+year);

            String time = null;
            if(year > 0){
                if (year == 1) {
                    time = (int)year + " year ago ";
                } else {
                    time = (int)year + " years ago ";
                }
            } else if(month >0){
                if (month == 1) {
                    time = (int)month + " month ago ";
                } else {
                    time = (int)month + " months ago ";
                }
            }else{
                if (diffDays > 0) {
                    if (diffDays == 1) {
                        time = diffDays + " day ago ";
                    } else {
                        time = diffDays + " days ago ";
                    }
                } else {
                    if (diffHours > 0) {
                        if (diffHours == 1) {
                            time = diffHours + " hr ago";
                        } else {
                            time = diffHours + " hrs ago";
                        }
                    } else {
                        if (diffMinutes > 0) {
                            if (diffMinutes == 1) {
                                time = diffMinutes + " min ago";
                            } else {
                                time = diffMinutes + " mins ago";
                            }
                        } else {
                            if (diffSeconds > 0) {
                                time = diffSeconds + " secs ago";
                            }
                        }

                    }

                }
            }
            return time;
        }

        public class AttemptInfo extends AsyncTask<JSONObject, JSONObject, JSONObject> {
            private int pos;
            private String challengeId;

             private ProgressDialog loading;
            public AttemptInfo(int position, String challengeId){
                pos = position;
                this.challengeId = challengeId;

            }
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading=new ProgressDialog(YoursChallengesActivity.this);
                loading.setMessage("Please wait...");
                loading.show();
            }

            @Override
            protected JSONObject doInBackground(JSONObject... jsonObjects) {
                String url = session.getApiUrl("getChallengeUserAttemptInfo");
               JSONObject jsonRes = null;
                HashMap<String, Object> httpParams = new HashMap<String, Object>();
                httpParams.put("callingApp", "TabApp");
                httpParams.put("callingAppId", "TabApp");
                httpParams.put("callingUserId", session.getOrgMemberInfo().userId);
                httpParams.put("userRole", session.getOrgMemberInfo().profile);
                httpParams.put("userId", session.getOrgMemberInfo().userId);
                httpParams.put("myUserId", session.getOrgMemberInfo().userId);
                httpParams.put("orgId", session.getOrgMemberInfo().orgId);
                httpParams.put("memberId", session.getOrgMemberInfo().userId);
                httpParams.put("id", challengeId);
                httpParams.put("size", "5"+1);
                httpParams.put("start", 0);

                session.addSessionParams(httpParams);

                try {
                    jsonRes = VedantuWebUtils.getJSONData(url, VedantuWebUtils.GET_REQ, httpParams);
                    Log.e("attemptInfo", "......." + jsonRes);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                return jsonRes;
            }

            @Override
            protected void onPostExecute(JSONObject jsonObject) {
                super.onPostExecute(jsonObject);

            loading.dismiss();


                if (jsonObject!=null){
                    try {
                        JSONObject result = jsonObject.getJSONObject("result");

                        String totalPoint=result.getString("totalPoint");
                        String multiplierPower=result.optString("multiplierPower");
                        String hint=result.getString("hint");
                        String basePoint=result.getString("basePoint");
                        String timeTaken=result.optString("timeTaken");
                        String success=result.getString("success");

                        View view = mRecyclerView.findViewHolderForAdapterPosition(pos).itemView;
//                        linearLayout = (LinearLayout) view.findViewById(R.id.dateContainerLayout);
//                        linearLayout.setBackgroundColor(Color.GREEN);


                        ((TextView) view.findViewById(R.id.text_points_earned)).setText(totalPoint);

                        ((TextView) view.findViewById(R.id.text_hinttaken)).setText(hint);

                        long time_taken= Long.parseLong(timeTaken);


                        String timetaken=String.format("%02d:%02d",
                                TimeUnit.MILLISECONDS.toMinutes(time_taken) -
                                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(time_taken)), // The change is in this line
                                TimeUnit.MILLISECONDS.toSeconds(time_taken) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time_taken)));
                        if (totalPoint!=null&& totalPoint.equalsIgnoreCase("0")){
                            ((TextView) view.findViewById(R.id.exam_status)).setText("Lost");
                            ((TextView) view.findViewById(R.id.exam_status)).setTextColor(getResources().getColor(R.color.red));
                        }else {
                            ((TextView) view.findViewById(R.id.exam_status)).setText("Cracked");
                            ((TextView) view.findViewById(R.id.exam_status)).setTextColor(getResources().getColor(R.color.green));
                        }
                        if (timeTaken!=null&& timeTaken.equalsIgnoreCase("0")){
                            ((TextView) view.findViewById(R.id.text_timetaken)).setText("NA");
                        }else {
                            ((TextView) view.findViewById(R.id.text_timetaken)).setText(timetaken + "  mins");
                        }
                        if (success!=null && success.equalsIgnoreCase("true")){
                            if(multiplierPower!=null && multiplierPower.equalsIgnoreCase("TRIPLE")){
                                (view.findViewById(R.id.point_multiply)).setVisibility(View.VISIBLE);
                                ((TextView) view.findViewById(R.id.point_multiply)).setText(basePoint+"*"+"3");
                            }else if(multiplierPower!=null && multiplierPower.equalsIgnoreCase("DOUBLE")){
                                ( view.findViewById(R.id.point_multiply)).setVisibility(View.VISIBLE);
                                ((TextView) view.findViewById(R.id.point_multiply)).setText(basePoint+"*"+"2");
                            }else if(multiplierPower!=null && multiplierPower.equalsIgnoreCase("SINGLE")){
                                (view.findViewById(R.id.point_multiply)).setVisibility(View.VISIBLE);
                                ((TextView) view.findViewById(R.id.point_multiply)).setText(basePoint+"*"+"1");
                            }
                        }else {
                            ( view.findViewById(R.id.point_multiply)).setVisibility(View.GONE);
                        }


                    }catch (Exception e){

                        e.printStackTrace();
                    }



                }



                }

            }

        }








    }




