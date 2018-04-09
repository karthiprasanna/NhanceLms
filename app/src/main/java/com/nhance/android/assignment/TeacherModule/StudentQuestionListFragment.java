package com.nhance.android.assignment.TeacherModule;

/**
 * Created by administrator on 2/13/17.
 */


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import android.widget.Toast;

import com.nhance.android.R;
import com.nhance.android.assignment.StudentPerformance.NetUtils;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.fragments.AbstractVedantuFragment;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.utils.VedantuWebUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentQuestionListFragment extends AbstractVedantuFragment {

    private SessionManager sessionManager;
    private String entity_id;
    private String entity_type;
    private String targetusetid;
    private RecyclerView lv;
    private List<QuestionStatusInfo> studentaddlist;

    private String solution,attempts;
    private JSONArray option;
    private JSONObject result;
    private String totalhits=null;
    private JSONObject list_data;
    private String quesno,type,content;
private JSONObject info;
    private JSONObject measures;
    private String left,correct,incorrect;

    private SwipeRefreshLayout refreshLayout;
    private QuestionListAdapter adapter;
    private ProgressDialog loading;
    private String assignmentName;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public View
    onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.student_question_listview, container, false);


        if(NetUtils.isOnline(getActivity()))
            new questionList().execute();

        else{
            Toast.makeText(getActivity(), R.string.no_internet_msg, Toast.LENGTH_SHORT).show();
        }

        sessionManager = SessionManager.getInstance(getActivity());
        Bundle values=getArguments();

        entity_id = values.getString(ConstantGlobal.ENTITY_ID);
        entity_type = values.getString(ConstantGlobal.ENTITY_TYPE);;
        targetusetid=values.getString(ConstantGlobal.TARGET_USERID);
        assignmentName=values.getString(ConstantGlobal.ASSIGNMENT_NAME);
        Log.e("assignmentNamelll","..."+assignmentName);


        studentaddlist=new ArrayList<>();



        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        lv = (RecyclerView) getActivity().findViewById(R.id.list_question);
        lv.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        lv.setLayoutManager(mLayoutManager);
        //info_listview.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        lv.setItemAnimator(new DefaultItemAnimator());


        refreshLayout=(SwipeRefreshLayout)getActivity().findViewById(R.id.refreshlayout);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {


                if(NetUtils.isOnline(getActivity()))
                    new questionList().execute();

                else{
                    Toast.makeText(getActivity(), R.string.no_internet_msg, Toast.LENGTH_SHORT).show();
                }







            }
        });


    }



    class  questionList extends AsyncTask<Object,Object,JSONObject> {
        String url=null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading = ProgressDialog.show(getActivity(), "Question Loading", "Please wait...",true,true);
            loading.show();
        }

        @Override
        protected JSONObject doInBackground(Object... params) {


            JSONObject jsonRes = null;
            this.url = sessionManager.getApiUrl("getEntityQuestionAttempts");





            Map<String, Object> httpParams = new HashMap<String, Object>();
            httpParams.put("entity.id", entity_id);
            httpParams.put("entity.type",entity_type);
            httpParams.put("targetUserId",targetusetid);


            sessionManager.addSessionParams(httpParams);

            try {
                jsonRes = VedantuWebUtils.getJSONData(url, VedantuWebUtils.GET_REQ, httpParams);

                Log.e("jsonres", "........" + jsonRes);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return jsonRes;
        }
        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);

loading.dismiss();

            try {
                result = jsonObject.getJSONObject("result");

                if (result != null) {


                    studentaddlist.clear();


                    totalhits = result.getString("totalHits");
                    JSONArray list = result.getJSONArray("list");
                    for (int i = 0; i < list.length(); i++) {

                        QuestionStatusInfo statusInfo=new QuestionStatusInfo();

                        list_data = list.getJSONObject(i);
                        statusInfo.qusNo = list_data.getString("qusNo");
                        info = list_data.getJSONObject("info");
                        statusInfo.solutions = info.getString("solutions");
                        statusInfo.type = info.getString("type");


                        statusInfo.content = info.getString("content");
                        statusInfo.options = info.optJSONArray("options");
                        Log.e("pppp","...."+statusInfo.content);

                        statusInfo.measures = list_data.getJSONObject("measures");
                        statusInfo.correct = statusInfo.measures.getInt("correct");
                        statusInfo.incorrect = statusInfo.measures.getInt("incorrect");
                        statusInfo.left = statusInfo.measures.getInt("left");
                        statusInfo.attempts = statusInfo.measures.getString("attempts");

                        studentaddlist.add(statusInfo);

                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


            adapter = new QuestionListAdapter(getActivity(), studentaddlist,assignmentName);

            lv.setAdapter(adapter);

            adapter.notifyDataSetChanged();
            refreshLayout.setRefreshing(false);
        }




    }


    private class QuestionListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        private  Activity activity;
        private  List<QuestionStatusInfo> studentaddlist;
        private String assignmentName;
        private int progressStatus;
        private ProgressItem mProgressItem;
        private ArrayList progressItemList;

        public QuestionListAdapter(Activity activity, List<QuestionStatusInfo> studentaddlist,String assignmentName) {
            this.activity=activity;
            this.studentaddlist=studentaddlist;
            this.assignmentName=assignmentName;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.question_list_performane, parent, false);
            return new MyHolder(itemView);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            final MyHolder itemHolder = (MyHolder) holder;
            QuestionStatusInfo questionStatusInfo=studentaddlist.get(position);

            itemHolder.ques_no.setText("Q"+questionStatusInfo.qusNo);

            long correctCount=questionStatusInfo.correct;
            long incorrectCount=questionStatusInfo.incorrect;
            long leftCount=questionStatusInfo.left;
//












            Log.e("crt","...."+correctCount+"incrt"+incorrectCount+"left"+leftCount);
//            AssignmentThreeHorizontalBarGraph holderr = (AssignmentThreeHorizontalBarGraph) itemHolder
//                    .findViewById(R.id.user_graph);
         /*   long correctCount = measures.correct;
            long incorrectCount = measures.incorrect;
            long leftCount = measures.left;*/
            itemHolder.user_graph.setValues((int) correctCount, (int) incorrectCount, (int) leftCount);










 /*           float crr_question = Float.parseFloat(questionStatusInfo.correct+"");
            float incrr_question = Float.parseFloat(questionStatusInfo.incorrect+"");
            float left_question= Float.parseFloat(questionStatusInfo.left+"");
            float total_question = crr_question+incrr_question+left_question;
            final float corrPer=(crr_question/total_question)*100;
            final float incorrPer=(incrr_question/total_question)*100;
            final float leftOver=(left_question/total_question)*100;
Log.e("fffcr","..."+corrPer+"...."+"fffincrt"+incorrPer);
            progressStatus = 0;
*/



            itemHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(activity,StudentQuestionStatusActivity.class);
                    intent.putExtra("qno",studentaddlist.get(position).qusNo);
                    intent.putExtra(ConstantGlobal.OPTIONS,studentaddlist.get(position).options+"");
                    intent.putExtra(ConstantGlobal.TYPE,studentaddlist.get(position).type);
                    intent.putExtra(ConstantGlobal.CONTENT,studentaddlist.get(position).content);
                    intent.putExtra(ConstantGlobal.ASSIGNMENT_NAME,assignmentName+"");
                    startActivity(intent);
                }
            });


        }

        @Override
        public int getItemCount() {
            return studentaddlist.size();
        }

        private class MyHolder extends RecyclerView.ViewHolder {
            private TextView ques_no;
            private AssignmentThreeHorizontalBarGraph user_graph;
            public MyHolder(View itemView) {
                super(itemView);

                 ques_no=(TextView)itemView.findViewById(R.id.ques_no);
                user_graph=(AssignmentThreeHorizontalBarGraph)itemView.findViewById(R.id.user_graph);

            }


        }


    }
}