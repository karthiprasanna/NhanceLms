package com.nhance.android.ChallengeArena;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nhance.android.ChallengeArena.model.LeaderInfo;
import com.nhance.android.R;
import com.nhance.android.assignment.StudentPerformance.NetUtils;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.pojos.OrgMemberInfo;
import com.nhance.android.utils.VedantuWebUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ClosedLeaderBoardActivity extends AppCompatActivity {
  private ProgressDialog loading;
    private SessionManager session;
    private JSONObject result;
    private String challengeId;
private List<LeaderInfo>leaderInfoList;
    private RecyclerView mRecyclerView;
    private TextView closed_refresh;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ClosedBoardAdapter closedBoardAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_closed_leader_board);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      //  getSupportActionBar().setIcon(R.drawable.ic_launcher);
        getSupportActionBar().setDisplayShowCustomEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Challenge Leader Board");
        session = SessionManager.getInstance(this);
        challengeId=getIntent().getStringExtra("challengeId");
        leaderInfoList=new ArrayList<>();

        Log.e("id........","...."+challengeId);

        mRecyclerView = (RecyclerView) findViewById(R.id.global_closed_recyclerview);

        closed_refresh = (TextView) findViewById(R.id.closed_empty_leader_board);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.closed_refresh);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        mRecyclerView.setLayoutManager(layoutManager);
        closedBoardAdapter=new ClosedBoardAdapter(this,leaderInfoList);
        mRecyclerView.setAdapter(closedBoardAdapter);


        if(NetUtils.isOnline(this)) {
            new userleaderboard().execute();

        } else{
            Toast.makeText(ClosedLeaderBoardActivity.this, R.string.no_internet_msg, Toast.LENGTH_SHORT).show();
        }

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {


                if(NetUtils.isOnline(ClosedLeaderBoardActivity.this)) {
                    new userleaderboard().execute();


                } else{
                    Toast.makeText(ClosedLeaderBoardActivity.this, R.string.no_internet_msg, Toast.LENGTH_SHORT).show();

                }


            }

        });


    }



    public class userleaderboard extends AsyncTask<JSONObject, JSONObject, JSONObject> {
        private ProgressDialog loading;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading=new ProgressDialog(ClosedLeaderBoardActivity.this);
            loading.setMessage("Please wait...");
            loading.show();
        }

        @Override
        protected JSONObject doInBackground(JSONObject... jsonObjects) {
            String url = session.getApiUrl("getChallengeLeaderBoard");


            JSONObject jsonRes = null;

            HashMap<String, Object> httpParams = new HashMap<String, Object>();

            httpParams.put("callingApp", "TabApp");
            httpParams.put("callingAppId", "TabApp");
            httpParams.put("id",challengeId);
            session.addSessionParams(httpParams);

            try {
                jsonRes = VedantuWebUtils.getJSONData(url, VedantuWebUtils.GET_REQ, httpParams);

              //  Log.e("monthly_board","...."+jsonRes);
                Log.e("closedboard","..."+jsonRes);
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
                    result=jsonObject.getJSONObject("result");

                   leaderInfoList.clear();
                    JSONArray list=result.getJSONArray("list");

                    for (int i=0;i<list.length();i++){
                        JSONObject list_object=list.getJSONObject(i);
                        LeaderInfo leaderInfo=new LeaderInfo();
                        leaderInfo.rank=list_object.getString("rank");
                        leaderInfo.timeTaken=list_object.getLong("timeTaken");
                        JSONObject user=list_object.getJSONObject("user");
                        leaderInfo.firstName=user.getString("firstName");
                        leaderInfo.lastName=user.getString("lastName");
                        leaderInfo.thumbnail=user.getString("thumbnail");
                        leaderInfo.id=user.getString("id");
                        leaderInfoList.add(leaderInfo);

                    }





                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
            closedBoardAdapter.notifyDataSetChanged();
            mSwipeRefreshLayout.setRefreshing(false);
            if(leaderInfoList != null && leaderInfoList.size()>0){
                closed_refresh.setVisibility(View.GONE);
            }else{
                closed_refresh.setVisibility(View.VISIBLE);
            }

        }

    }
    private class ClosedBoardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private Activity activity;
        List<LeaderInfo> infoList;

        public ClosedBoardAdapter(Activity activity, List<LeaderInfo> infoList) {

            this.activity=activity;
            this.infoList=infoList;

        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.closed_individual_list_row, parent, false);
            return new MyHolder(itemView);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            MyHolder itemHolder = (MyHolder) holder;
            LeaderInfo globalLeaderInfo=infoList.get(position);

            itemHolder.rank.setText(globalLeaderInfo.rank);
            itemHolder.firstName.setText(globalLeaderInfo.firstName+"  "+globalLeaderInfo.lastName);
            Picasso.with(activity).load(globalLeaderInfo.thumbnail).into(itemHolder.user_image);
            String timetaken=   String.format("%02d:%02d",
                    //    TimeUnit.MILLISECONDS.toHours(globalLeaderInfo.timeTaken),
                    TimeUnit.MILLISECONDS.toMinutes(globalLeaderInfo.timeTaken) -
                            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(globalLeaderInfo.timeTaken)), // The change is in this line
                    TimeUnit.MILLISECONDS.toSeconds(globalLeaderInfo.timeTaken) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(globalLeaderInfo.timeTaken)));
            itemHolder.timetaken.setText("Time Taken  "+timetaken+"  mins");

            OrgMemberInfo orgMemberInfo = session.getOrgMemberInfo();
            if (orgMemberInfo.id.equals(globalLeaderInfo.id)) {
                //    itemHolder.your_position.setBackgroundColor(getResources().getColor(R.color.light_yellow));
                itemHolder.your_profile.setBackgroundColor(getResources().getColor(R.color.light_yellow));
                itemHolder.rank.setBackgroundColor(getResources().getColor(R.color.light_yellow));


            }
        }

        @Override
        public int getItemCount() {
            return infoList.size();
        }

        private class MyHolder extends RecyclerView.ViewHolder {


            public TextView firstName;
            public TextView timetaken;
            public TextView rank;
            public  TextView time;
            public ImageView user_image;

            public RelativeLayout your_profile;
            public MyHolder(View itemView) {
                super(itemView);

                rank= (TextView)itemView.findViewById(R.id.closed_individual);
                timetaken= (TextView)itemView.findViewById(R.id.closed_individual_points);
                firstName= (TextView)itemView.findViewById(R.id.closed_individual_user_fname);
                user_image=(ImageView)itemView.findViewById(R.id.closed_individual_user_image);
                your_profile=(RelativeLayout) itemView.findViewById(R.id.closed_profile);



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
