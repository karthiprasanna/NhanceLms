package com.nhance.android.GlobalLeaderBoard;


import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
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

import com.nhance.android.R;
import com.nhance.android.activities.baseclasses.NhanceBaseFragment;
import com.nhance.android.assignment.StudentPerformance.NetUtils;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.utils.VedantuWebUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;


public class GlobalweeklyLeaderBoardFragment extends NhanceBaseFragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private RecyclerView mRecyclerView;
        private SessionManager session;
    private String mParam1;
    private String mParam2;
    private android.content.Context context;
    private TextView  empty_ac_list;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private JSONObject result;
    private List<GlobalLeaderInfo>leaderInfoList;
    private WeeklyBoardAdapter weeklyBoardAdapter;
    private TextView weekly_list_status;

    public GlobalweeklyLeaderBoardFragment() {
    }


    // TODO: Rename and change types and number of parameters
    public GlobalweeklyLeaderBoardFragment newInstance(String param1, String param2) {
        GlobalweeklyLeaderBoardFragment fragment = new GlobalweeklyLeaderBoardFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
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
        leaderInfoList=new ArrayList<>();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.global_weekly_fragment, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.global_weekly_recyclerview);

        empty_ac_list = (TextView) view.findViewById(R.id.weekly_empty_leader_board);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.weekly_refresh);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(context);

        mRecyclerView.setLayoutManager(layoutManager);
        weeklyBoardAdapter=new WeeklyBoardAdapter(getActivity(),leaderInfoList);
        mRecyclerView.setAdapter(weeklyBoardAdapter);
        weekly_list_status=(TextView)view.findViewById(R.id.weekly_empty_leader_board);


        if(NetUtils.isOnline(getActivity())) {
            new weeklyLeaderBpard().execute();

        } else{
            Toast.makeText(getActivity(), R.string.no_internet_msg, Toast.LENGTH_SHORT).show();
        }

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {


                if(NetUtils.isOnline(getActivity())) {
                    new weeklyLeaderBpard().execute();


                } else{
                    Toast.makeText(getActivity(), R.string.no_internet_msg, Toast.LENGTH_SHORT).show();

                }


            }

        });



        return view;

    }




    public class weeklyLeaderBpard extends AsyncTask<JSONObject, JSONObject, JSONObject> {




        @Override
        protected JSONObject doInBackground(JSONObject... jsonObjects) {
            String url = session.getApiUrl("getChallengeGlobalLeaderBoard");


            JSONObject jsonRes = null;

            HashMap<String, Object> httpParams = new HashMap<String, Object>();

            httpParams.put("callingApp", "TabApp");
            httpParams.put("callingAppId", "TabApp");
            httpParams.put("rankType","WEEKLY");
            session.addSessionParams(httpParams);

            try {
                jsonRes = VedantuWebUtils.getJSONData(url, VedantuWebUtils.GET_REQ, httpParams);

                Log.e("weekly_board","...."+jsonRes);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return jsonRes;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);

            if (jsonObject!=null){

                try {
                    result=jsonObject.getJSONObject("result");

                    leaderInfoList.clear();
                    JSONArray list=result.getJSONArray("list");


                    for (int i=0;i<list.length();i++){

                        JSONObject list_object=list.getJSONObject(i);

                        GlobalLeaderInfo leaderInfo=new GlobalLeaderInfo();
                        leaderInfo.points=list_object.getString("points");
                        leaderInfo.rank=list_object.getString("rank");
                        JSONObject user=list_object.getJSONObject("user");
                        leaderInfo.thumbnail=user.getString("thumbnail");
                        leaderInfo.firstName=user.getString("firstName");
                        leaderInfo.lastName=user.getString("lastName");
                        leaderInfoList.add(leaderInfo);


                        Log.e("kkkk", leaderInfo.points+"..."+ leaderInfo.rank+"..."+ leaderInfo.firstName+"..."+ leaderInfo.thumbnail);
                    }



                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
            weeklyBoardAdapter.notifyDataSetChanged();
            mSwipeRefreshLayout.setRefreshing(false);
            if(leaderInfoList != null && leaderInfoList.size()>0){
                weekly_list_status.setVisibility(View.GONE);
            }else{
                weekly_list_status.setVisibility(View.VISIBLE);
            }

        }

    }


    private class WeeklyBoardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private Activity activity;
        List<GlobalLeaderInfo> infoList;

        public WeeklyBoardAdapter(Activity activity, List<GlobalLeaderInfo> infoList) {

            this.activity=activity;
            this.infoList=infoList;

        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.weekly_list_row, parent, false);
            return new MyHolder(itemView);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            MyHolder itemHolder = (MyHolder) holder;
            GlobalLeaderInfo globalLeaderInfo=infoList.get(position);

            Log.e("globallist","...."+globalLeaderInfo);
            itemHolder.rank.setText(globalLeaderInfo.rank);
            itemHolder.points.setText(globalLeaderInfo.points+"  Points");
            itemHolder.firstName.setText(globalLeaderInfo.firstName+"  "+globalLeaderInfo.lastName);
            Picasso.with(activity).load(globalLeaderInfo.thumbnail).into(itemHolder.user_image);

        }

        @Override
        public int getItemCount() {
            return infoList.size();
        }

        private class MyHolder extends RecyclerView.ViewHolder {


            public TextView firstName;
            public TextView points;
            public TextView rank;
            public  TextView time;
            public ImageView user_image;
            public MyHolder(View itemView) {
                super(itemView);

                rank= (TextView)itemView.findViewById(R.id.weekly);
                points= (TextView)itemView.findViewById(R.id.weekly_points);
                firstName= (TextView)itemView.findViewById(R.id.weekly_user_fname);
                user_image=(ImageView)itemView.findViewById(R.id.weekly_user_image);



            }


        }
    }
}
