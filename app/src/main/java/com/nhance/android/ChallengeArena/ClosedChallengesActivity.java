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
import com.squareup.picasso.Picasso;

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

public class ClosedChallengesActivity extends AppCompatActivity {
  private ProgressDialog loading;
    private android.content.Context context;
    private RecyclerView mRecyclerView;
    private TextView empty_cc_list;
    private SessionManager session;
    private ClosedChallengesAdapter ccAdapter;
    private List<ChallengeItem> challengeItemList;
    private JSONObject result, result2, facet,ac_count, cc_count;
    private int ac_count1;
    private JSONArray list, boards;
    private ArrayAdapter<String> ccspinadapter;
    private ArrayAdapter<String> cc_re_pop_Adapter;
    private Spinner ccspinner,cc_recent_popular;
    private ArrayList<String> spinsubjectlist = new ArrayList<>();
    private  String sub_name,id;
    private String mParam1;
    private String mParam2;
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
        setContentView(R.layout.activity_closed_challenges);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
       // getSupportActionBar().setIcon(R.drawable.ic_launcher);
        getSupportActionBar().setDisplayShowCustomEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Closed Challenges");
        channelId=getIntent().getStringExtra("channel_id");
        session = SessionManager.getInstance(this);
        challengeItemList = new ArrayList<>();
        subnameaddlist=new ArrayList<>();
        topicnameaddlist=new ArrayList<>();
        channelname = new HashMap<String, String>();
        mRecyclerView = (RecyclerView) findViewById(R.id.cc_recyclerview);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayoutmsg);
        empty_cc_list = (TextView) findViewById(R.id.empty_cc_list);
        FontUtils.setTypeface(empty_cc_list, FontUtils.FontTypes.ROBOTO_LIGHT);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(layoutManager);
        ccspinner = (Spinner) findViewById(R.id.ccspin_sublist);

        spinsubjectlist = new ArrayList<String>();
        ccspinadapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, spinsubjectlist);
        ccspinner.setAdapter(ccspinadapter);

        ccspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {

                String type = ccspinner.getSelectedItem().toString().trim();
                if (type.equalsIgnoreCase("All Subjects")) {
                   // subjectId = null;
                    Log.e("red","....."+result2);
                  //  filterlist(result2);
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


                            JSONObject object_point = chlist.optJSONObject("info");
                            if (object_point!=null) {

                                channelitem.challengeId = object_point.getString("challengeId");
                            }

                            channelitem.toppers = chlist.optJSONArray("toppers");
                            Log.e("toppers", "..." + channelitem.toppers);
                            if (channelitem.toppers != null) {
                                for (int l = 0; l < channelitem.toppers.length(); l++) {
                                    JSONObject topper_object = channelitem.toppers.getJSONObject(l);
                                    channelitem.topperName = topper_object.getString("firstName");
                                    channelitem.thumbnail = topper_object.getString("thumbnail");
                                }

                            }




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
                        empty_cc_list.setVisibility(View.GONE);
                    }else{
                        empty_cc_list.setVisibility(View.VISIBLE);
                    }


                    ccAdapter = new ClosedChallengesAdapter(ClosedChallengesActivity.this, challengeItemList,subnameaddlist,topicnameaddlist);
                    mRecyclerView.setAdapter(ccAdapter);
                    mSwipeRefreshLayout.setRefreshing(false);
                    ccAdapter.notifyDataSetChanged();


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


                            JSONObject object_point = chlist.optJSONObject("info");
                            if (object_point!=null) {

                                channelitem.challengeId = object_point.getString("challengeId");
                            }

                            channelitem.toppers = chlist.optJSONArray("toppers");
                            Log.e("toppers", "..." + channelitem.toppers);
                            if (channelitem.toppers != null) {
                                for (int l = 0; l < channelitem.toppers.length(); l++) {
                                    JSONObject topper_object = channelitem.toppers.getJSONObject(l);
                                    channelitem.topperName = topper_object.getString("firstName");
                                    channelitem.thumbnail = topper_object.getString("thumbnail");
                                }

                            }


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

                        }


                    }catch (Exception e){

                        e.printStackTrace();
                    }

                    if(challengeItemList != null && challengeItemList.size()>0){
                        empty_cc_list.setVisibility(View.GONE);
                    }else{
                        empty_cc_list.setVisibility(View.VISIBLE);
                    }


                    ccAdapter = new ClosedChallengesAdapter(ClosedChallengesActivity.this, challengeItemList,subnameaddlist,topicnameaddlist);
                    mRecyclerView.setAdapter(ccAdapter);

                    mSwipeRefreshLayout.setRefreshing(false);
                    ccAdapter.notifyDataSetChanged();


                }



            }


            public void onNothingSelected(AdapterView<?> parent) {

            }
        });







        cc_recent_popular = (Spinner) findViewById(R.id.cc_recent_popular);
        String[] ac_recent_array = {"Most Recent", "Most Popular"};
        cc_re_pop_Adapter= new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,ac_recent_array);
        cc_re_pop_Adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        cc_recent_popular.setAdapter(cc_re_pop_Adapter);



        cc_recent_popular.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {




                String type = cc_recent_popular.getSelectedItem().toString().trim();
                if (type.equalsIgnoreCase("Most Popular")) {
                    new ClosedChallengesMostPopular().execute();
                } else {
                    new ClosedChallenges().execute();

                }



            }


            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



        if(NetUtils.isOnline(ClosedChallengesActivity.this)) {
            new ClosedChallenges().execute();

            new cc_GetBoards().execute();

        } else{
            Toast.makeText(this, R.string.no_internet_msg, Toast.LENGTH_SHORT).show();
        }


        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {


                if(NetUtils.isOnline(ClosedChallengesActivity.this)) {
                    new ClosedChallenges().execute();

                    new cc_GetBoards().execute();

                } else{
                    Toast.makeText(ClosedChallengesActivity.this, R.string.no_internet_msg, Toast.LENGTH_SHORT).show();
                }


            }

        });



    }

    public class ClosedChallengesMostPopular extends AsyncTask<JSONObject, JSONObject, JSONObject> {
        private ProgressDialog loading;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading=new ProgressDialog(ClosedChallengesActivity.this);
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
            httpParams.put("orderBy", "mostpopular");
            httpParams.put("status", "ENDED");
            httpParams.put("type", "FIXED_TIME");

            httpParams.put("size", "10"+1);
            httpParams.put("start", 0);
            httpParams.put("year", 2017);
            httpParams.put("channelId",channelId);
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

                    cc_count = facet.getJSONObject("statusFacet");
                    ac_count1 = cc_count.optInt("active");
                    Log.e("account", ".." + ac_count1);
                    filterlist(result2);


                    Log.e("challengeItemList", "......." + challengeItemList);
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }


            }

        }

    }



    //Get the challenges
    public class ClosedChallenges extends AsyncTask<JSONObject, JSONObject, JSONObject> {
        private ProgressDialog loading;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading=new ProgressDialog(ClosedChallengesActivity.this);
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
            httpParams.put("status", "ENDED");
            httpParams.put("type", "FIXED_TIME");

            httpParams.put("size", "10"+1);
            httpParams.put("start", 0);
            httpParams.put("year", 2017);
            httpParams.put("channelId",channelId);
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

                    cc_count = facet.getJSONObject("statusFacet");
                    ac_count1 = cc_count.optInt("active");
                    Log.e("account", ".." + ac_count1);




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


        JSONObject object_point = chlist.optJSONObject("info");
        if (object_point!=null) {

            channelitem.challengeId = object_point.getString("challengeId");
            Log.e("challengeIddddddd", "..." +  channelitem.challengeId);
        }

    channelitem.toppers = chlist.optJSONArray("toppers");
    Log.e("toppers", "..." + channelitem.toppers);
    if (channelitem.toppers != null) {
        for (int l = 0; l < channelitem.toppers.length(); l++) {
            JSONObject topper_object = channelitem.toppers.getJSONObject(l);
            channelitem.topperName = topper_object.getString("firstName");
            channelitem.thumbnail = topper_object.getString("thumbnail");
        }

    }




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
            empty_cc_list.setVisibility(View.GONE);
        }else{
            empty_cc_list.setVisibility(View.VISIBLE);
        }


        ccAdapter = new ClosedChallengesAdapter(ClosedChallengesActivity.this, challengeItemList,subnameaddlist,topicnameaddlist);
        mRecyclerView.setAdapter(ccAdapter);

        mSwipeRefreshLayout.setRefreshing(false);
        ccAdapter.notifyDataSetChanged();



    }

    //Get the Boards
    public class cc_GetBoards extends AsyncTask<JSONObject, JSONObject, JSONObject> {
        private ProgressDialog loading;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            loading=new ProgressDialog(ClosedChallengesActivity.this);
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

        ccspinadapter.notifyDataSetChanged();
    } catch (JSONException e) {
        e.printStackTrace();
    }
}

            ccAdapter = new ClosedChallengesAdapter(ClosedChallengesActivity.this, challengeItemList,subnameaddlist,topicnameaddlist);
            mRecyclerView.setAdapter(ccAdapter);
            mSwipeRefreshLayout.setRefreshing(false);

            ccAdapter.notifyDataSetChanged();


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
        private ChallengeItem current;

        private  ArrayList<HashMap<String, String>> subnameaddlist;
        private  ArrayList<HashMap<String, String>> topicnameaddlist;

        public ClosedChallengesAdapter(Activity context, List<ChallengeItem> data,ArrayList<HashMap<String, String>> subnameaddlist,ArrayList<HashMap<String, String>> topicnameaddlist) {

            this.context = context;
            this.data = data;
            session = SessionManager.getInstance(context);

            this.subnameaddlist=subnameaddlist;
            this.topicnameaddlist=topicnameaddlist;
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.closedchallenge_list_row, parent, false);

            MyHolder holder = new MyHolder(view);
            return holder;



        }

        // Bind data

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {



            Map<String ,String> subname1 = new HashMap<String,String>();
            CountDownTimer countDownTimer;

            final MyHolder myHolder = (MyHolder) holder;
              current = data.get(position);
            myHolder.channelname.setText(current.channelname);
            subname1 = current.subname1.get(current.channelname);
            myHolder.question_id.setText(current.id);


            HashMap<String, String> challenge_course;
            challenge_course = subnameaddlist.get(position);

            HashMap<String, String> challenge_topic;

            challenge_topic = topicnameaddlist.get(position);
            myHolder.actopictext.setText(challenge_topic.get("topicName"));

            myHolder.acsubjecttext.setText(challenge_course.get("subName"));

            myHolder.pointstext.setText(String.valueOf(current.pointstext));

            myHolder.attemptstext.setText(String.valueOf(current.attemptstext));

            myHolder.challenge_time.setText(timeConversion((long) (current.challenge_time)));

            myHolder.challengeclosed_time.setText(getlongtoago(Long.parseLong(current.challengeclosed_time)));
            myHolder.timeCreated.setText(getDate(current.timeCreated));


            myHolder.leveltext.setText(current.leveltext);



    /*        myHolder.border_layout.setVisibility(View.GONE);
            myHolder.upArrow.setVisibility(View.GONE);
            myHolder.downArrow.setVisibility(View.VISIBLE);
*/
            Log.e("topername","...."+current.toppers+"......"+current.topperName+"....."+current.thumbnail);




            if (current.challengeId==null){


                myHolder.border_layout.setVisibility(View.GONE);
                myHolder.upArrow.setVisibility(View.GONE);
                myHolder.downArrow.setVisibility(View.GONE);

            }else{

                myHolder.border_layout.setVisibility(View.GONE);
                myHolder.upArrow.setVisibility(View.GONE);
                myHolder.downArrow.setVisibility(View.VISIBLE);

            }

if (current.topperName==null && current.thumbnail==null){

    myHolder.topper_profile.setVisibility(View.GONE);
    myHolder.topper_firstName.setVisibility(View.GONE);
    myHolder.topper.setVisibility(View.GONE);
    myHolder.no_topper.setVisibility(View.VISIBLE);


}else{

                myHolder.topper.setVisibility(View.VISIBLE);
                myHolder.topper_profile.setVisibility(View.VISIBLE);
                myHolder.topper_firstName.setVisibility(View.VISIBLE);
                myHolder.topper_firstName.setText(current.topperName);
    myHolder.no_topper.setVisibility(View.GONE);

    Picasso.with(context).load(current.thumbnail).into(myHolder.topper_profile);

            }
            FontUtils.setTypeface(myHolder.cc_view_challenge, FontUtils.FontTypes.ROBOTO_LIGHT);

           //cc_view_challenge.setOnClickListener(this);
            myHolder.downArrow.setTag(position);
            myHolder.cc_view_challenge.setTag(position);
        }


        @Override
        public int getItemCount() {
            return data.size();
        }


        class MyHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener ,View.OnClickListener{

            TextView topper,no_topper,topper_firstName,question_id,channelname,challenge_time,leveltext,pointstext,attemptstext,challenge_count,actopictext,acsubjecttext,challengeclosed_time,timeCreated;
            ProgressBar pb;
            RecyclerView lv;
            RelativeLayout ccre1;
            TextView cc_view_challenge;
            TextView dynamic_hinttaken,dynamic_timetaken,dynamic_pointsearned,exam_status,text_points_earned,point_multiply,text_timetaken,text_hinttaken;

            LinearLayout border_layout;
            ImageView upArrow,downArrow;

           ImageView topper_profile;

            public MyHolder(final View itemView) {
                super(itemView);




                question_id = (TextView) itemView.findViewById(R.id.question_id);


                channelname = (TextView) itemView.findViewById(R.id.channelname);
                pointstext = (TextView) itemView.findViewById(R.id.pointstext);
                attemptstext = (TextView) itemView.findViewById(R.id.attemptstext);
                leveltext = (TextView) itemView.findViewById(R.id.leveltext);
                challenge_count = (TextView) itemView.findViewById(R.id.challenge_count);
                actopictext = (TextView) itemView.findViewById(R.id.actopictext);
                acsubjecttext = (TextView) itemView.findViewById(R.id.acsubjecttext);
                challenge_time = (TextView) itemView.findViewById(R.id.challengetime_text);
                challengeclosed_time = (TextView) itemView.findViewById(R.id.challengeclosed_time);
                timeCreated = (TextView) itemView.findViewById(R.id.cc_time);
                cc_view_challenge=(TextView) itemView.findViewById(R.id.cc_view_challenge);
                lv = (RecyclerView) itemView.findViewById(R.id.acrecyclerview);
                ccre1=(RelativeLayout)itemView.findViewById(R.id.acre);
                topper_profile=(ImageView)itemView.findViewById(R.id.topper_profile);
                topper_firstName=(TextView)itemView.findViewById(R.id.topper_firstname);
                no_topper=(TextView)itemView.findViewById(R.id.notopper);
                topper=(TextView)itemView.findViewById(R.id.topper);

                exam_status = (TextView) itemView.findViewById(R.id.closed_exam_status);
                text_points_earned = (TextView) itemView.findViewById(R.id.closed_points_earned);
                point_multiply = (TextView) itemView.findViewById(R.id.closed_point_multiply);
                text_timetaken = (TextView) itemView.findViewById(R.id.closed_timetaken);
                text_hinttaken = (TextView) itemView.findViewById(R.id.closed_text_hinttaken);
                upArrow = (ImageView) itemView.findViewById(R.id.closed_uparraow);
                downArrow = (ImageView) itemView.findViewById(R.id.closed_downarraow);
                border_layout = (LinearLayout) itemView.findViewById(R.id.closed_border_layout);
                dynamic_pointsearned=(TextView) itemView.findViewById(R.id.pointsearned);
                dynamic_timetaken=(TextView) itemView.findViewById(R.id.timetaken);
                dynamic_hinttaken=(TextView) itemView.findViewById(R.id.hinttaken);


                cc_view_challenge.setOnClickListener(this);
                downArrow.setOnClickListener(this);

                upArrow.setOnClickListener(this);




                FontUtils.setTypeface(actopictext, FontUtils.FontTypes.ROBOTO_LIGHT);
                FontUtils.setTypeface(acsubjecttext, FontUtils.FontTypes.ROBOTO_LIGHT);
                FontUtils.setTypeface(pointstext, FontUtils.FontTypes.ROBOTO_LIGHT);
                FontUtils.setTypeface(attemptstext, FontUtils.FontTypes.ROBOTO_LIGHT);
                FontUtils.setTypeface(challenge_time, FontUtils.FontTypes.ROBOTO_LIGHT);
                FontUtils.setTypeface(challenge_time, FontUtils.FontTypes.ROBOTO_LIGHT);
                FontUtils.setTypeface(challenge_time, FontUtils.FontTypes.ROBOTO_LIGHT);
                FontUtils.setTypeface(challenge_time, FontUtils.FontTypes.ROBOTO_LIGHT);
                FontUtils.setTypeface(challenge_time, FontUtils.FontTypes.ROBOTO_LIGHT);
                FontUtils.setTypeface(challengeclosed_time, FontUtils.FontTypes.ROBOTO_LIGHT);
                FontUtils.setTypeface(timeCreated, FontUtils.FontTypes.ROBOTO_LIGHT);
                FontUtils.setTypeface(leveltext, FontUtils.FontTypes.ROBOTO_LIGHT);
                FontUtils.setTypeface(challenge_count, FontUtils.FontTypes.ROBOTO_LIGHT);
                FontUtils.setTypeface(topper, FontUtils.FontTypes.ROBOTO_LIGHT);
                FontUtils.setTypeface(no_topper, FontUtils.FontTypes.ROBOTO_LIGHT);



                FontUtils.setTypeface(exam_status, FontUtils.FontTypes.ROBOTO_LIGHT);
                FontUtils.setTypeface(text_points_earned, FontUtils.FontTypes.ROBOTO_LIGHT);
                FontUtils.setTypeface(point_multiply, FontUtils.FontTypes.ROBOTO_LIGHT);
                FontUtils.setTypeface(text_timetaken, FontUtils.FontTypes.ROBOTO_LIGHT);
                FontUtils.setTypeface(text_hinttaken, FontUtils.FontTypes.ROBOTO_LIGHT);
                FontUtils.setTypeface(dynamic_pointsearned, FontUtils.FontTypes.ROBOTO_LIGHT);
                FontUtils.setTypeface(dynamic_timetaken, FontUtils.FontTypes.ROBOTO_LIGHT);
                FontUtils.setTypeface(dynamic_hinttaken, FontUtils.FontTypes.ROBOTO_LIGHT);





            }
            @Override
            public boolean onLongClick(View view) {
                return false;
            }

            @Override
            public void onClick(View v) {

                if (v.getId()==R.id.cc_view_challenge){

                    int pos = (int) v.getTag();
                    String id = data.get(pos).id;
                    String id1 = data.get(pos).channelname;

                    Intent intent=new Intent(context,ClosedViewChallengeActivity.class);

                    intent.putExtra("questionId",id);
                    intent.putExtra("channelname",id1);
                    context.startActivity(intent);
                }
                if(v.getId() == R.id.closed_downarraow){
                    int pos = (int) v.getTag();
                    String id = data.get(pos).challengeId;
                    downArrow.setVisibility(View.GONE);
                    border_layout.setVisibility(View.VISIBLE);
                    upArrow.setVisibility(View.VISIBLE);
                    new AttemptInfo(pos, id).execute();
                }
                if(v.getId() == R.id.closed_uparraow){
                    downArrow.setVisibility(View.VISIBLE);
                    border_layout.setVisibility(View.GONE);
                    upArrow.setVisibility(View.GONE);
                }

            }
        }

        private String timeConversion(long totalSeconds) {
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
        public  String getlongtoago(long createdAt) {

            String time;
            Date d2 = new Date();
            Date d1 = new Date(createdAt);

            long diff = d2.getTime() - d1.getTime();
            System.out.println(diff + "  diff");
            long diffSeconds = diff / 1000 % 60;
            long diffMinutes = diff / (60 * 1000) % 60;
            long diffHours = diff / (60 * 60 * 1000);
            long diffInDays = diff / (1000 * 60 * 60 * 24);
            int weeks = (int) (diff/ (1000*60*60*24*7));
            long month = diffInDays /30;
            long year = diffInDays / 365;

            time = diffInDays+" days";

            System.out.println(diffInDays + "  days");
            System.out.println(diffHours + "  Hour");
            System.out.println(diffMinutes + "  min");
            System.out.println(diffSeconds + "  sec");
            System.out.println(weeks + "  weeks");
            System.out.println(month + "  month");
            System.out.println(year + "  year");


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
            }else if (diffInDays > 0) {
                if (diffInDays == 1) {
                    time = diffInDays + " day ago ";
                } else {
                    time = diffInDays + " days ago ";
                }
            } else if (diffHours > 0) {
                if (diffHours == 1) {
                    time = diffHours + " hr ago";
                } else {
                    time = diffHours + " hrs ago";
                }
            } else if (diffMinutes > 0) {
                if (diffMinutes == 1) {
                    time = diffMinutes + " min ago";
                } else {
                    time = diffMinutes + " mins ago";
                }
            } else if (diffSeconds > 0) {
                time = diffSeconds + " secs ago";
            }



            return  time;
        }
        public  String convertDate(String dateInMilliseconds,String dateFormat) {
            return DateFormat.format(dateFormat, Long.parseLong(dateInMilliseconds)).toString();
        }
        public String getDate(long time) {
            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
            cal.setTimeInMillis(time);
            String date = DateFormat.format("dd-MM-yyyy", cal).toString();
            return date;
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
                loading=new ProgressDialog(ClosedChallengesActivity.this);
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


                        ((TextView) view.findViewById(R.id.closed_points_earned)).setText(totalPoint);

                        ((TextView) view.findViewById(R.id.closed_text_hinttaken)).setText(hint);

                        long time_taken= Long.parseLong(timeTaken);


                        String timetaken=String.format("%02d:%02d",
                                TimeUnit.MILLISECONDS.toMinutes(time_taken) -
                                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(time_taken)), // The change is in this line
                                TimeUnit.MILLISECONDS.toSeconds(time_taken) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time_taken)));
                        if (totalPoint!=null&& totalPoint.equalsIgnoreCase("0")){
                            ((TextView) view.findViewById(R.id.closed_exam_status)).setText("Lost");
                            ((TextView) view.findViewById(R.id.closed_exam_status)).setTextColor(getResources().getColor(R.color.red));
                        }else {
                            ((TextView) view.findViewById(R.id.closed_exam_status)).setText("Cracked");
                            ((TextView) view.findViewById(R.id.closed_exam_status)).setTextColor(getResources().getColor(R.color.green));
                        }
                        if (timeTaken!=null&& timeTaken.equalsIgnoreCase("0")){
                            ((TextView) view.findViewById(R.id.closed_timetaken)).setText("NA");
                        }else {
                            ((TextView) view.findViewById(R.id.closed_timetaken)).setText(timetaken + "  mins");
                        }
                        if (success!=null && success.equalsIgnoreCase("true")){
                            if(multiplierPower!=null && multiplierPower.equalsIgnoreCase("TRIPLE")){
                                (view.findViewById(R.id.closed_point_multiply)).setVisibility(View.VISIBLE);
                                ((TextView) view.findViewById(R.id.closed_point_multiply)).setText(basePoint+"*"+"3");
                            }else if(multiplierPower!=null && multiplierPower.equalsIgnoreCase("DOUBLE")){
                                ( view.findViewById(R.id.closed_point_multiply)).setVisibility(View.VISIBLE);
                                ((TextView) view.findViewById(R.id.closed_point_multiply)).setText(basePoint+"*"+"2");
                            }else if(multiplierPower!=null && multiplierPower.equalsIgnoreCase("SINGLE")){
                                (view.findViewById(R.id.closed_point_multiply)).setVisibility(View.VISIBLE);
                                ((TextView) view.findViewById(R.id.closed_point_multiply)).setText(basePoint+"*"+"1");
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
