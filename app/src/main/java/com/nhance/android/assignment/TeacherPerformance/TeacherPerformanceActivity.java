package com.nhance.android.assignment.TeacherPerformance;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.nhance.android.R;
import com.nhance.android.assignment.StudentPerformance.NetUtils;
import com.nhance.android.assignment.StudentPerformance.QuestionInfo;
import com.nhance.android.assignment.UserPerformance.AssignmentViewAnalyticsActivity;
import com.nhance.android.assignment.UserPerformance.model.GridItemInfo;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.customviews.NonScrollableGridView;
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

public class TeacherPerformanceActivity extends AppCompatActivity {
    private String entity_id,entity_type,targetUserId, assignmentName;
    SessionManager session;
    private JSONObject savedata;
    private JSONArray questions;
    private JSONObject quesobject;
    List <QuestionInfo> subjectNameList;
    private JSONObject info;
    private RecyclerView info_listview;
     private SwipeRefreshLayout refreshLayout;
    private PerformanceAdapter performanceAdapter;
    private String subjectName,subjectId;
    private List<GridItemInfo> questionList;
    private JSONArray boardTree_array;
    private JSONObject boardTree_object;
    private HashMap<String, List<GridItemInfo>> subjectQuestionMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.teacher_performance);
        assignmentName = getIntent().getStringExtra(ConstantGlobal.ASSIGNMENT_NAME);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
       // getSupportActionBar().setIcon(R.drawable.ic_launcher);
        getSupportActionBar().setDisplayShowCustomEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(assignmentName);
        session = SessionManager.getInstance(this);
        entity_id = getIntent().getStringExtra(ConstantGlobal.ENTITY_ID);
        entity_type = getIntent().getStringExtra(ConstantGlobal.ENTITY_TYPE);
        targetUserId=getIntent().getStringExtra("userId");

        Log.e("kkk","..."+entity_id+"..."+entity_type+"...."+targetUserId);


        info_listview=(RecyclerView)findViewById(R.id.comments_list);
        subjectNameList=new ArrayList<>();
        questionList=new ArrayList<>();
        subjectQuestionMap = new HashMap<>();
        refreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipelayout);
          info_listview.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        info_listview.setLayoutManager(mLayoutManager);
        //info_listview.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        info_listview.setItemAnimator(new DefaultItemAnimator());


        if (NetUtils.isOnline(this)){
     new Studentperformance().execute();
        }else {
            Toast.makeText(TeacherPerformanceActivity.this,R.string.no_internet_msg,Toast.LENGTH_SHORT).show();
        }

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {


                if(NetUtils.isOnline(TeacherPerformanceActivity.this)) {
                    new Studentperformance().execute();
                } else{
                    Toast.makeText(TeacherPerformanceActivity.this, R.string.no_internet_msg, Toast.LENGTH_SHORT).show();

                }


            }

        });

    }


    class Studentperformance extends AsyncTask<Object, Object, JSONObject> {
        private String url;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(Object... params) {
            JSONObject jsonRes = null;
            this.url = session.getApiUrl("getUserEntityQuestionAttempts");
            Map<String, Object> httpParams = new HashMap<String, Object>();
            httpParams.put("entity.id", entity_id);
            httpParams.put("entity.type", entity_type);
            httpParams.put("targetUserId",targetUserId);
            httpParams.put("id",entity_id);
            httpParams.put("targetUserId1",targetUserId);
            httpParams.put("myUserId",session.getOrgMemberInfo().userId);
            session.addSessionParams1(httpParams);


            try {
                jsonRes = VedantuWebUtils.getJSONData(url, VedantuWebUtils.GET_REQ, httpParams);
                Log.e("performance","........"+jsonRes);

            } catch (IOException e) {
                e.printStackTrace();
            }


            return jsonRes;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);

if (jsonObject!=null) {
    try {
        JSONObject result = jsonObject.getJSONObject("result");
        if (result != null) {

           subjectNameList.clear();
           questionList.clear();
            subjectNameList.removeAll(subjectNameList);
            questionList.removeAll(questionList);

            JSONArray boared = result.getJSONArray("boards");

            for (int i = 0; i < boared.length(); i++) {
                savedata = boared.getJSONObject(i);
                QuestionInfo  subjectInfo=new QuestionInfo();
                subjectName=savedata.getString("name");
                subjectId=savedata.getString("id");
                subjectInfo.name=subjectName;
                subjectInfo.id=subjectId;

                questionList = new ArrayList<>();
                questions = savedata.getJSONArray("questions");

                for (int j = 0; j < questions.length(); j++) {
                    quesobject = questions.getJSONObject(j);
                   GridItemInfo  questionInfo=new GridItemInfo();
                    info = quesobject.getJSONObject("info");
                    questionInfo.solutions = info.getString("solutions");
                    questionInfo.type = info.getString("type");
                    questionInfo.id = info.getString("id");
                    questionInfo.status = quesobject.getString("status");
                    questionInfo.content = info.getString("content");
                    questionInfo.answer = quesobject.getJSONObject("answer");
                    questionInfo.options = info.optJSONArray("options");
                    boardTree_array = info.getJSONArray("boards");
                            questionList.add(questionInfo);

               }

                subjectQuestionMap.put(subjectInfo.id, questionList);
                Log.e("subiiiiiiii", "...." + subjectId);
                subjectNameList.add(subjectInfo);

            }
        }
        performanceAdapter = new PerformanceAdapter(TeacherPerformanceActivity.this,subjectNameList,entity_id,entity_type,assignmentName);
        info_listview.setAdapter(performanceAdapter);

        performanceAdapter.notifyDataSetChanged();
        refreshLayout.setRefreshing(false);




    } catch (JSONException e) {
        e.printStackTrace();
    }

}


        }
    }

    private class PerformanceAdapter  extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
  private Activity activity;
        private List<QuestionInfo> subjectNameList;
        private GridViewAdapter gridViewAdapter;

        private String entity_id;
        private  String entity_type;
        private  String assignmentName;

        public PerformanceAdapter(Activity activity, List<QuestionInfo> subjectNameList, String entity_id, String entity_type,String assignmentName) {
            this.activity=activity;
            this.subjectNameList=subjectNameList;
            this.entity_id=entity_id;
            this.entity_type=entity_type;
            this.assignmentName=assignmentName;

        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.teacher_list, parent, false);
            return new MyHolder(itemView);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {


            MyHolder itemHolder = (MyHolder) holder;
            final QuestionInfo questionInfo=subjectNameList.get(position);
            Log.e("questionInfo","...."+subjectNameList.toString());
            itemHolder.sub_name.setText(questionInfo.name);
            questionList = subjectQuestionMap.get(questionInfo.id);
            gridViewAdapter = new GridViewAdapter(activity,questionList);
            itemHolder.gridView.setAdapter(gridViewAdapter);

            gridViewAdapter.notifyDataSetChanged();


        }

        @Override
        public int getItemCount() {
            return subjectNameList.size();
        }

        private class MyHolder extends RecyclerView.ViewHolder {
            public TextView sub_name;
            public NonScrollableGridView gridView;
            public MyHolder(View itemView) {
                super(itemView);

                sub_name= (TextView)itemView.findViewById(R.id.sub_name_list);
                gridView = (NonScrollableGridView) itemView.findViewById(R.id.question_number);

            }
        }

        private class GridViewAdapter extends BaseAdapter {

            private  LayoutInflater inflater=null;
            private Activity activity;
            private List<GridItemInfo>questionInfoList;
            GridItemInfo questionInfo = new GridItemInfo();




            public GridViewAdapter(Activity activity, List<GridItemInfo> questionInfoList) {
                this.activity=activity;
                this.questionInfoList=questionInfoList;


                inflater = (LayoutInflater)activity.getSystemService(activity.LAYOUT_INFLATER_SERVICE);
            }


            @Override
            public int getCount() {
                return questionInfoList.size();
            }

            @Override
            public Object getItem(int position) {
                return questionInfoList.get(position);
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
                questionInfo=questionInfoList.get(position);





                final int s = position+1;
                gridview.setText("Q" + s);
                gridview.setTextColor(getResources().getColor(R.color.white));

            if (questionInfo.status!=null && questionInfo.status.equalsIgnoreCase("LEFT")) {

                gridview.setBackgroundColor(getResources().getColor(R.color.red));

            }else {
                gridview.setBackgroundColor(getResources().getColor(R.color.green));


            }
            gridview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                   Intent intent=new Intent(activity,TeacherExamViewActivity.class);
                    Toast.makeText(activity,questionInfoList.get(position).id,Toast.LENGTH_LONG).show();

                 intent.putExtra(ConstantGlobal.STATUS,questionInfoList.get(position).status);
                    intent.putExtra(ConstantGlobal.ASSIGNMENT_NAME,assignmentName);
                    intent.putExtra(ConstantGlobal.ENTITY_ID,entity_id);
                    intent.putExtra(ConstantGlobal.ENTITY_TYPE,entity_type);
                    intent.putExtra(ConstantGlobal.ANSWER,questionInfoList.get(position).answer+"");
                    intent.putExtra(ConstantGlobal.CONTENT,questionInfoList.get(position).content);
                    intent.putExtra(ConstantGlobal.OPTIONS,questionInfoList.get(position).options+"");
                    intent.putExtra(ConstantGlobal.TYPE,questionInfoList.get(position).type);
                    intent.putExtra(ConstantGlobal.ID,questionInfoList.get(position).id);

                   startActivity(intent);
                }
            });


                return vi;
            }
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.analytics, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();

                return true;
            case R.id.assignment_analytics:

                Intent intent = new Intent(TeacherPerformanceActivity.this, AssignmentViewAnalyticsActivity.class);

                intent.putExtra(ConstantGlobal.ASSIGNMENT_NAME,
                        assignmentName);
                intent.putExtra(ConstantGlobal.ENTITY_ID, entity_id);
                intent.putExtra(ConstantGlobal.ENTITY_TYPE, entity_type);
                intent.putExtra(ConstantGlobal.TARGET_USERID, targetUserId);
                startActivity(intent);
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
