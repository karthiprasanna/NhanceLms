package com.nhance.android.ChallengeArena;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.nhance.android.R;
import com.nhance.android.activities.AppLandingPageActivity;
import com.nhance.android.assignment.StudentPerformance.NetUtils;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.utils.FontUtils;
import com.nhance.android.utils.VedantuWebUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;

public class ResultAwaitedActivity extends AppCompatActivity {
    HashMap<String, String> channelname;

    private Context context;

    private TextView result_awaited_text,result_wait;
    private SessionManager session;
    private ProgressDialog loading;
    private TextView empty_cc_list;
    private String subId;
    private JSONObject board_obj;
    private String subName;
    private String subType;
    private String topicName,topicId;
    private String topicType;


    private List<ChallengeItem> active_challengeItemList;

    private JSONObject result, result2, facet,ac_count, cc_count;
    ArrayList<HashMap<String, String>> subnameaddlist;
    ArrayList<HashMap<String, String>> topicnameaddlist;
    private int ac_count1;
    private JSONArray list, boards,qtypes;

    private ArrayAdapter<String> ycspinadapter;

    private ArrayAdapter<String> yc_re_pop_Adapter;
    private Spinner ycspinner,yc_recent_popular;
    private ArrayList<String> spinsubjectlist = new ArrayList<>();
    private  String sub_name,id;

    private StatusActiveChallengesAdapter statusActiveChallengesAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView active_mRecyclerView;
    private String totalHits;
    private int questionSize;
    private String ch_id;
    private String subjectId;
    private String channelId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_result_awaited);
        empty_cc_list = (TextView) findViewById(R.id.result_awaited_text);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setIcon(R.drawable.ic_launcher);
        getSupportActionBar().setDisplayShowCustomEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Result Awaited");
        channelId=getIntent().getStringExtra("channel_id");
        session = SessionManager.getInstance(this);
                active_challengeItemList=new ArrayList<>();
        subnameaddlist=new ArrayList<>();
        topicnameaddlist=new ArrayList<>();
        channelname = new HashMap<String, String>();

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayoutmsg);
        result_awaited_text = (TextView) findViewById(R.id.result_awaited_text);
        FontUtils.setTypeface(result_awaited_text, FontUtils.FontTypes.ROBOTO_LIGHT);


        result_wait = (TextView) findViewById(R.id.result_wait);
        FontUtils.setTypeface(result_wait, FontUtils.FontTypes.ROBOTO_LIGHT);


        active_mRecyclerView = (RecyclerView) findViewById(R.id.activie_rcrecyclerview1);
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(context);
        active_mRecyclerView.setLayoutManager(layoutManager1);
        active_mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));


        ycspinner = (Spinner) findViewById(R.id.ycspin_sublist);
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
                    try {
                        list = result2.getJSONArray("list");
                        Log.i("Listitem", "...." + list);
                        for (int i = 0; i < list.length(); i++) {
                            JSONObject chlist = list.getJSONObject(i);
                            ChallengeItem channelitem = new ChallengeItem();
                            channelitem.channelname = chlist.getString("name");
                            Log.e("name", "......." + channelitem.channelname);
                            channelitem.leveltext = chlist.getString("difficulty");
                            channelitem.id = chlist.getString("id");
                            channelitem.attemptstext = chlist.getInt("attempts");
                            channelitem.pointstext = chlist.getInt("maxBid");
                            channelitem.attempted = chlist.getString("attempted");
                            channelitem.challenge_time = chlist.getLong("duration");
                            channelitem.lifetime = chlist.getLong("lifeTime");
                            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                            long currenttime = cal.getTimeInMillis();
                            long timercount = chlist.getLong("endTime") - (currenttime);
                            Log.e("endTime", "......." + chlist.getLong("endTime"));
                            Log.e("currenttime", "......." + currenttime);
                            channelitem.challenge_count = timercount;
                            Log.e("lifeTime", "......." + channelitem.challenge_count);
                            JSONObject object_point = chlist.getJSONObject("info");
                            channelitem.totalPoint = object_point.getString("totalPoint");
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

                            active_challengeItemList.add(channelitem);

                        }

                    }catch (Exception e){

                        e.printStackTrace();
                    }

                    if(active_challengeItemList != null && active_challengeItemList.size()>0){
                        result_awaited_text.setVisibility(View.GONE);
                    }else{
                        result_awaited_text.setVisibility(View.VISIBLE);
                    }

                    statusActiveChallengesAdapter = new StatusActiveChallengesAdapter(ResultAwaitedActivity.this, active_challengeItemList,subnameaddlist,topicnameaddlist);
                    active_mRecyclerView.setAdapter(statusActiveChallengesAdapter);
                    statusActiveChallengesAdapter.notifyDataSetChanged();
                    mSwipeRefreshLayout.setRefreshing(false);






                } else {
                    // subjectId = channelname.get(ycspinner.getSelectedItem().toString().trim());
                    try {
                        subjectId = channelname.get(type.trim());

                        active_challengeItemList.clear();
                        active_challengeItemList.removeAll(active_challengeItemList);
                        topicnameaddlist.clear();
                        topicnameaddlist.removeAll(topicnameaddlist);
                        subnameaddlist.clear();
                        subnameaddlist.removeAll(subnameaddlist);
                        list = result2.getJSONArray("list");

                        Log.e("sublist","..."+list);
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
                                        active_challengeItemList.add(channelitem);

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

                    if(active_challengeItemList != null && active_challengeItemList.size()>0){
                        empty_cc_list.setVisibility(View.GONE);
                    }else{
                        empty_cc_list.setVisibility(View.VISIBLE);
                    }


                    statusActiveChallengesAdapter = new StatusActiveChallengesAdapter(ResultAwaitedActivity.this, active_challengeItemList,subnameaddlist,topicnameaddlist);
                    active_mRecyclerView.setAdapter(statusActiveChallengesAdapter);

                    mSwipeRefreshLayout.setRefreshing(false);
                    statusActiveChallengesAdapter.notifyDataSetChanged();


                }



            }


            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        yc_recent_popular = (Spinner) findViewById(R.id.yc_recent_popular);

        String[] ac_recent_array = {"Most Recent", "Most Popular"};

        yc_re_pop_Adapter= new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,ac_recent_array);
        yc_re_pop_Adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        yc_recent_popular.setAdapter(yc_re_pop_Adapter);

        yc_recent_popular.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {




                String type = yc_recent_popular.getSelectedItem().toString().trim();
                if (type.equalsIgnoreCase("Most Popular")) {
                    new actiiviesChangesMostpopular().execute();
                    // Toast.makeText(getActivity(), "Most Popular Clicked", Toast.LENGTH_SHORT).show();
                } else {
                    new actiiviesChanges().execute();
                    // Toast.makeText(getActivity(), "Most Recent Clicked", Toast.LENGTH_SHORT).show();

                }



            }


            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        if(NetUtils.isOnline(this)) {
            new cc_GetBoards().execute();
            new actiiviesChanges().execute();

        } else{
            Toast.makeText(this, R.string.no_internet_msg, Toast.LENGTH_SHORT).show();
        }

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {


                if(NetUtils.isOnline(ResultAwaitedActivity.this)) {


                    new cc_GetBoards().execute();
                    new actiiviesChanges().execute();

                } else{
                    Toast.makeText(ResultAwaitedActivity.this, R.string.no_internet_msg, Toast.LENGTH_SHORT).show();
                }


            }

        });


    }

    //Get the challenges mostpopular
    public class actiiviesChangesMostpopular extends AsyncTask<JSONObject, JSONObject, JSONObject> {
        private ProgressDialog loading;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading=new ProgressDialog(ResultAwaitedActivity.this);
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
            httpParams.put("status", "ACTIVE");
            httpParams.put("type", "FIXED_TIME");
            httpParams.put("subjectId",subjectId);
            httpParams.put("channelId",channelId);

            httpParams.put("size", 10);
            httpParams.put("start", 0);
            httpParams.put("year", 2017);
            Log.e("Httpparams", "......." +   httpParams);

            session.addSessionParams(httpParams);

            try {
                jsonRes = VedantuWebUtils.getJSONData(url, VedantuWebUtils.GET_REQ, httpParams);
                Log.e("actiiviesChanges", "......." + jsonRes);

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

                    active_challengeItemList.clear();
                    active_challengeItemList.removeAll(active_challengeItemList);
                    topicnameaddlist.clear();
                    topicnameaddlist.removeAll(topicnameaddlist);
                    subnameaddlist.clear();
                    subnameaddlist.removeAll(subnameaddlist);


                    result2 = jsonObject.getJSONObject("result");
                    facet = result2.getJSONObject("facet");


                    ac_count1 = 0;
                    ac_count = facet.getJSONObject("pointStatFacet");

                    filterlist(result2);

                } catch (JSONException e1) {
                    e1.printStackTrace();
                }

            }

        }

    }

    //Get the challenges
    public class actiiviesChanges extends AsyncTask<JSONObject, JSONObject, JSONObject> {
        private ProgressDialog loading;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading=new ProgressDialog(ResultAwaitedActivity.this);
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
            httpParams.put("status", "ACTIVE");
            httpParams.put("type", "FIXED_TIME");
            httpParams.put("subjectId",subjectId);
            httpParams.put("channelId",channelId);

            httpParams.put("size", 10);
            httpParams.put("start", 0);
            httpParams.put("year", 2017);
            Log.e("Httpparams", "......." +   httpParams);

            session.addSessionParams(httpParams);

            try {
                jsonRes = VedantuWebUtils.getJSONData(url, VedantuWebUtils.GET_REQ, httpParams);
                Log.e("actiiviesChanges", "......." + jsonRes);

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

        active_challengeItemList.clear();
        active_challengeItemList.removeAll(active_challengeItemList);
        topicnameaddlist.clear();
        topicnameaddlist.removeAll(topicnameaddlist);
        subnameaddlist.clear();
        subnameaddlist.removeAll(subnameaddlist);


        result2 = jsonObject.getJSONObject("result");
        facet = result2.getJSONObject("facet");


        ac_count1 = 0;
        ac_count = facet.getJSONObject("pointStatFacet");

   filterlist(result2);

    } catch (JSONException e1) {
        e1.printStackTrace();
    }

}

        }

    }

    private void filterlist(JSONObject result2) {
        
        
        
        try {



            list = result2.getJSONArray("list");
            Log.i("Listitem", "...." + list);


            for (int i = 0; i < list.length(); i++) {


                JSONObject chlist = list.getJSONObject(i);
                ChallengeItem channelitem = new ChallengeItem();
                channelitem.channelname = chlist.getString("name");
                Log.e("name", "......." + channelitem.channelname);
                channelitem.leveltext = chlist.getString("difficulty");
                channelitem.id = chlist.getString("id");
                channelitem.attemptstext = chlist.getInt("attempts");
                channelitem.pointstext = chlist.getInt("maxBid");

                channelitem.attempted = chlist.getString("attempted");
                channelitem.challenge_time = chlist.getLong("duration");
                channelitem.lifetime = chlist.getLong("lifeTime");
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                long currenttime = cal.getTimeInMillis();
                long timercount = chlist.getLong("endTime") - (currenttime);
                Log.e("endTime", "......." + chlist.getLong("endTime"));
                Log.e("currenttime", "......." + currenttime);
                channelitem.challenge_count = timercount;

                Log.e("lifeTime", "......." + channelitem.challenge_count);

                JSONObject object_point = chlist.getJSONObject("info");
                channelitem.totalPoint = object_point.getString("totalPoint");
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

                active_challengeItemList.add(channelitem);
            }
        }catch (Exception e){
            
            e.printStackTrace();
        }

        if(active_challengeItemList != null && active_challengeItemList.size()>0){
            result_awaited_text.setVisibility(View.GONE);
        }else{
            result_awaited_text.setVisibility(View.VISIBLE);
        }

        statusActiveChallengesAdapter = new StatusActiveChallengesAdapter(ResultAwaitedActivity.this, active_challengeItemList,subnameaddlist,topicnameaddlist);
        active_mRecyclerView.setAdapter(statusActiveChallengesAdapter);
        statusActiveChallengesAdapter.notifyDataSetChanged();
        mSwipeRefreshLayout.setRefreshing(false);
        
        
    }

    public static class StatusActiveChallengesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {

        int minutes;
        private Context context;
        private LayoutInflater inflater;
        //        private List<ChallengeItem> active_challengeItemList;
        private ChallengeItem channelitem = new ChallengeItem();
        List<ChallengeItem> data = Collections.emptyList();
        ProgressDialog pd;
        int minutes1 = 0;
        int j =0;
        AppLandingPageActivity apa = new AppLandingPageActivity();
        // array used to perform multiple animation at once
        private SparseBooleanArray animationItemsIndex;
        private boolean reverseAllAnimations = false;

        private Object i;
        private SessionManager session;
        private Handler handler;
        private static final int MINUTES_IN_AN_HOUR = 60;
        private static final int SECONDS_IN_A_MINUTE = 60;

        private ArrayList<HashMap<String, String>> subnameaddlist;
        private ArrayList<HashMap<String, String>> topicnameaddlist;

        // create constructor to innitilize context and data sent from MainActivity
        public StatusActiveChallengesAdapter(Context context, List<ChallengeItem> data,ArrayList<HashMap<String, String>> subnameaddlist,ArrayList<HashMap<String, String>> topicnameaddlist) {
            //  pd = new ProgressDialog(context);
            this.context = context;
            inflater = LayoutInflater.from(context);
            this.data = data;
            session = SessionManager.getInstance(context);

            this.subnameaddlist=subnameaddlist;
            this.topicnameaddlist=topicnameaddlist;


        }

        // Inflate the layout when viewholder created
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.active_status_list, parent, false);
            MyHolder holder = new MyHolder(view);
            return holder;
        }

        // Bind data

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            Map<String ,String> subname1 = new HashMap<String,String>();


            final MyHolder myHolder = (MyHolder) holder;
            final ChallengeItem current = data.get(position);
            myHolder.channelname.setText(current.channelname);
            FontUtils.setTypeface(myHolder.channelname, FontUtils.FontTypes.ROBOTO_LIGHT);


            subname1 = current.subname1.get(current.channelname);
          //  myHolder.actopictext.setText(subname1.get("TOPIC"));
            myHolder.challenge_id.setText(current.id);
            FontUtils.setTypeface(myHolder.challenge_id, FontUtils.FontTypes.ROBOTO_LIGHT);

            HashMap<String, String> challenge_course;
            challenge_course = subnameaddlist.get(position);
            HashMap<String, String> challenge_topic;
            challenge_topic = topicnameaddlist.get(position);
            myHolder.actopictext.setText(challenge_topic.get("topicName"));
            FontUtils.setTypeface(myHolder.actopictext, FontUtils.FontTypes.ROBOTO_LIGHT);
            myHolder.acsubjecttext.setText(challenge_course.get("subName"));
            FontUtils.setTypeface(myHolder.acsubjecttext, FontUtils.FontTypes.ROBOTO_LIGHT);
            //  myHolder.acsubjecttext.setText(subname1.get("COURSE"));
            myHolder.pointstext.setText("Win upto  "+String.valueOf(current.pointstext)+"  points");
            FontUtils.setTypeface(myHolder.pointstext, FontUtils.FontTypes.ROBOTO_LIGHT);
            myHolder.attemptstext.setText(String.valueOf(current.attemptstext));
            FontUtils.setTypeface(myHolder.attemptstext, FontUtils.FontTypes.ROBOTO_LIGHT);
            myHolder.leveltext.setText(current.leveltext);
            FontUtils.setTypeface(myHolder.leveltext, FontUtils.FontTypes.ROBOTO_LIGHT);
            myHolder.challenge_time.setText(timeConversion((long)(current.challenge_time)));
            FontUtils.setTypeface(myHolder.challenge_time, FontUtils.FontTypes.ROBOTO_LIGHT);


            minutes = (int) (current.challenge_count);
            Log.i("current.challenge_count","...."+current.challenge_count);
            Log.i("Minutes","...."+current.challenge_count);
            myHolder.progressBarCircle.setMax((int) (current.lifetime));
            long processed_time=  (current.lifetime-(current.challenge_count/1000));
            Log.i("processed_time","...."+processed_time);
            Log.i("current.challenge_time","...."+current.challenge_count);
            Log.i("current.lifetime","...."+current.lifetime);

            myHolder.progressBarCircle.setProgress((int) (current.lifetime-(current.challenge_count/1000)));
            // Log.i("setProgress1","...."+ ((int) (current.challenge_count)/1000));
            new CountDownTimer(current.challenge_count, 1000) {


                public void onTick(long millisUntilFinished) {

                    long millis = millisUntilFinished  ;
                    myHolder.progressBarCircle.setProgress( ((int) (millisUntilFinished)/1000));
                    //    Log.i("setProgress","...."+ ((int) (millisUntilFinished)/1000));
                    String hms = String.format("%02d:%02d:%02d",
                            TimeUnit.MILLISECONDS.toHours(millis),
                            TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                            TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
                    //  Log.i("hms","...."+hms);
                    myHolder.challenge_count.setText(hms);//set text
                    FontUtils.setTypeface(myHolder.challenge_count, FontUtils.FontTypes.ROBOTO_LIGHT);


                }

                public void onFinish() {
                    if(data.size()>position)
                        data.remove(position);
                   // notifyDataSetChanged();
                }
            }.start();

        }

        @Override
        public void onClick(View view) {

        }

        // return total item from List
        @Override
        public int getItemCount() {
            return data.size();
        }
        class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView channelname,challenge_id,challenge_time,leveltext,pointstext,attemptstext,challenge_count,actopictext,acsubjecttext;
            Button take_challenge_test;
            RecyclerView lv;
            RelativeLayout acre1;
            private Handler handler;
            private ProgressBar progressBarCircle;


            // create constructor to get widget reference
            public MyHolder(View itemView) {
                super(itemView);
                progressBarCircle = (ProgressBar) itemView.findViewById(R.id.result_progressBarCircle);
                channelname = (TextView) itemView.findViewById(R.id.result_channelname);
                pointstext = (TextView) itemView.findViewById(R.id.result_pointstext);
                attemptstext = (TextView) itemView.findViewById(R.id.result_attemptstext);
                leveltext = (TextView) itemView.findViewById(R.id.result_leveltext);
                challenge_count = (TextView) itemView.findViewById(R.id.result_challenge_count);
                actopictext = (TextView) itemView.findViewById(R.id.result_actopictext);
                acsubjecttext = (TextView) itemView.findViewById(R.id.result_acsubjecttext);
                challenge_time = (TextView) itemView.findViewById(R.id.result_challengetime_text);
                challenge_id=(TextView)itemView.findViewById(R.id.result_challenge_id);
            }
            @Override
            public void onClick(View v) {
            }
        }

        private static String timeConversion(long totalSeconds) {
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

        private static int hoursToSeconds(int hours) {
            return hours * MINUTES_IN_AN_HOUR * SECONDS_IN_A_MINUTE;
        }

        private static int minutesToSeconds(int minutes) {
            return minutes * SECONDS_IN_A_MINUTE;
        }


    }


    public class cc_GetBoards extends AsyncTask<JSONObject, JSONObject, JSONObject> {
        private ProgressDialog loading;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            loading=new ProgressDialog(ResultAwaitedActivity.this);
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




}
