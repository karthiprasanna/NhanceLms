package com.nhance.android.assignment.UserPerformance;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nhance.android.R;

import com.nhance.android.assignment.StudentPerformance.NetUtils;
import com.nhance.android.assignment.UserPerformance.model.GridItemInfo;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.customviews.NonScrollableGridView;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.utils.GoogleAnalyticsUtils;
import com.nhance.android.utils.VedantuWebUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentExamGridActivity extends AppCompatActivity {
    private String boardsubjectId;



    private JSONObject info;
    private JSONObject savedata;
    private JSONArray questions;
    private List<GridItemInfo> griditemList;
    private JSONObject quesobject;

    private JSONObject quesanswer;

    private JSONArray option;


    private String subject_name;

    private SessionManager session;
    private String entity_id, entity_type, assignmentName;
    private GridViewAdapter gridViewAdapter;






    private GridItemInfo currentQuestionData;
    private String targetUserId;
    private ArrayList<String> subjectlist = new ArrayList<>();
    private SwipeRefreshLayout refreshLayout;
    private NonScrollableGridView gridView;
    private TextView emptymsg_textview,unattempted;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GoogleAnalyticsUtils.setScreenName(ConstantGlobal.GA_SCREEN_TAKE_ASSIGNMENT);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_student_exam_grid);

        assignmentName = getIntent().getStringExtra(ConstantGlobal.ASSIGNMENT_NAME);
        entity_id = getIntent().getStringExtra(ConstantGlobal.ENTITY_ID);
        entity_type = getIntent().getStringExtra(ConstantGlobal.ENTITY_TYPE);
        targetUserId = getIntent().getStringExtra(ConstantGlobal.TARGET_USERID);
        session = SessionManager.getInstance(this);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
       // getSupportActionBar().setIcon(R.drawable.ic_launcher);
        getSupportActionBar().setDisplayShowCustomEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(assignmentName);

        griditemList = new ArrayList<>();




        refreshLayout=(SwipeRefreshLayout)findViewById(R.id.text_swipelayout);
        gridView = (NonScrollableGridView) findViewById(R.id.text_question_answers);
        emptymsg_textview=(TextView)findViewById(R.id.text_emptymsg_solution);
        unattempted=(TextView)findViewById(R.id.text_unattempted);

        if (NetUtils.isOnline(StudentExamGridActivity.this)) {
            new questionlist().execute();
        } else {
            Toast.makeText(StudentExamGridActivity.this, R.string.no_internet_msg, Toast.LENGTH_SHORT).show();
        }


        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if(NetUtils.isOnline(StudentExamGridActivity.this)) {
                    new questionlist().execute();                }else{
                    Toast.makeText(StudentExamGridActivity.this, R.string.no_internet_msg, Toast.LENGTH_SHORT).show();
                }

            }
        });

        gridViewAdapter = new GridViewAdapter(this,griditemList,entity_id,entity_type,assignmentName);
        gridView.setAdapter(gridViewAdapter);




    }




    class questionlist extends AsyncTask<Object, Object, JSONObject> {
        private String url;
        private ProgressDialog loading;
        private JSONArray boared;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            loading=new ProgressDialog(StudentExamGridActivity.this);
            loading.setMessage("Please wait...");
            loading.show();
        }

        @Override
        protected JSONObject doInBackground(Object... params) {


            JSONObject jsonRes = null;
            this.url = session.getApiUrl("getUserEntityQuestionAttempts");

         //   Log.e("urldata", "........" + url);
            Map<String, Object> httpParams = new HashMap<String, Object>();
            httpParams.put("entity.id", entity_id);
            httpParams.put("entity.type", entity_type);
            httpParams.put("targetUserId", targetUserId);

            session.addSessionParams(httpParams);
            try {
                jsonRes = VedantuWebUtils.getJSONData(url, VedantuWebUtils.GET_REQ, httpParams);

                Log.e("jsonRes","..."+jsonRes);
            } catch (IOException e) {
                e.printStackTrace();

            }
            return jsonRes;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {

            super.onPostExecute(jsonObject);

            loading.dismiss();


            if (jsonObject != null) {
                griditemList.clear();
                try {
                    JSONObject result = jsonObject.getJSONObject("result");
                    if (result != null) {
                        boared = result.getJSONArray("boards");
                        for (int i = 0; i < boared.length(); i++) {
                            savedata = boared.getJSONObject(i);
                            subject_name = savedata.getString("name");
                            boardsubjectId = savedata.getString("id");
                            subjectlist.add(subject_name);
                        }

                        filterList(boared);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


        }


    }

    public void filterList(JSONArray boared) {


        try {

            for (int i = 0; i < boared.length(); i++) {
                savedata = boared.getJSONObject(i);
                subject_name = savedata.getString("name");
                boardsubjectId = savedata.getString("id");
                questions = savedata.getJSONArray("questions");

                for (int j = 0; j < questions.length(); j++) {
                    currentQuestionData = new GridItemInfo();
                    quesobject = questions.getJSONObject(j);
                    info = quesobject.getJSONObject("info");
                    currentQuestionData.answer = quesobject.getJSONObject("answer");
                    currentQuestionData.options = info.optJSONArray("options");
                    currentQuestionData.id = info.getString("id");
                    currentQuestionData.name = info.getString("content");
                    currentQuestionData.solutions = info.getString("solutions");
                    currentQuestionData.type = info.getString("type");
                    currentQuestionData.status = quesobject.getString("status");

                    griditemList.add(currentQuestionData);
                }


            }

            refreshLayout.setRefreshing(false);
            gridViewAdapter.notifyDataSetChanged();

        } catch (JSONException e) {
            e.printStackTrace();
        }




    }

    private class GridViewAdapter extends BaseAdapter {

        private  LayoutInflater inflater=null;
        private Activity activity;
        private List<GridItemInfo>infoList;
        GridItemInfo questionInfo = new GridItemInfo();
        private String entityId;
        private  String entityType;
        private String assignmentName;


        public GridViewAdapter(Activity activity, List<GridItemInfo> infoList,String entityId,String entityType,String assignmentName) {
            this.activity=activity;
            this.infoList=infoList;
            this.entityId=entityId;
            this.entityType=entityType;
            this.assignmentName=assignmentName;

            inflater = (LayoutInflater)activity.getSystemService(activity.LAYOUT_INFLATER_SERVICE);
        }



        @Override
        public int getCount() {
            return infoList.size();
        }

        @Override
        public Object getItem(int position) {
            return infoList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            View vi=convertView;
            if(vi==null)

                vi = inflater.inflate(R.layout.student_exam_grid_item, null);

            TextView gridview=(TextView)vi.findViewById(R.id.text_grid_item_title);
            questionInfo=infoList.get(position);

          Log.e("Q_No ",questionInfo.toString());



            final int s = position+1;
            gridview.setText("Q" + s);
           gridview.setTextColor(getResources().getColor(R.color.white));

            if (questionInfo.status!=null && questionInfo.status.equalsIgnoreCase("LEFT")) {

                gridview.setBackground(getResources().getDrawable(R.drawable.question_correct_round_corner));


            }else {
                gridview.setBackground(getResources().getDrawable(R.drawable.question_incorrect_round_corner));


            }

gridview.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        Intent intent=new Intent(StudentExamGridActivity.this,StudentAssignmentActivity.class);
       intent.putExtra(ConstantGlobal.STATUS,infoList.get(position).status);
//        intent.putExtra(ConstantGlobal.FIRST_NAME,infoList.get(position).firstName);
//        intent.putExtra(ConstantGlobal.LAST_NAME,infoList.get(position).lastName);
//        intent.putExtra(ConstantGlobal.THUMBNAIL,infoList.get(position).thumbnail);
//        intent.putExtra(ConstantGlobal.PROFILE,infoList.get(position).profile);
      //  intent.putExtra(ConstantGlobal.STATUS,infoList.get(position).status);
//        intent.putExtra(ConstantGlobal.LAST_UPDATED,infoList.get(position).lastUpdated);
        intent.putExtra(ConstantGlobal.ASSIGNMENT_NAME,assignmentName);
        intent.putExtra(ConstantGlobal.ENTITY_ID,entity_id);
        intent.putExtra(ConstantGlobal.ENTITY_TYPE,entity_type);
        intent.putExtra(ConstantGlobal.ANSWER,infoList.get(position).answer+"");
        intent.putExtra(ConstantGlobal.CONTENT,infoList.get(position).name);
        intent.putExtra(ConstantGlobal.OPTIONS,infoList.get(position).options+"");
        intent.putExtra(ConstantGlobal.TYPE,infoList.get(position).type);
        intent.putExtra(ConstantGlobal.SOLUTION,infoList.get(position).solutions);
        intent.putExtra(ConstantGlobal.ID,infoList.get(position).id);
        intent.putExtra(ConstantGlobal.POSITION,"Q" + s);

        startActivity(intent);
    }
});


            return vi;
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

    @Override
    protected void onResume() {
        super.onResume();

        if(NetUtils.isOnline(StudentExamGridActivity.this)) {
            new questionlist().execute();
        }else{
            Toast.makeText(StudentExamGridActivity.this, R.string.no_internet_msg, Toast.LENGTH_SHORT).show();
        }

    }
}
