package com.nhance.android.assignment.TeacherPerformance;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nhance.android.R;
import com.nhance.android.assignment.StudentPerformance.AsssignmentQuestion;
import com.nhance.android.assignment.StudentPerformance.NetUtils;
import com.nhance.android.assignment.TeacherPerformance.fragment.TeacherPurposeExamFragment;
import com.nhance.android.assignment.UserPerformance.AssignmentSolutionActivity;
import com.nhance.android.assignment.UserPerformance.UserPurposeAssignmentActivity;
import com.nhance.android.assignment.UserPerformance.model.UserPurposeInfo;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.utils.VedantuWebUtils;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class TeacherPurposeExamActivity extends AppCompatActivity implements View.OnClickListener, TeacherPurposeExamFragment.NotifyTakeTestFragment{

    private String questionId;
    private SessionManager session;
    private UserPurposeInfo userPurposeInfo;
    private String optionsString;
    private String attempted_correct_answer;
    private AsssignmentQuestion asssignmentQuestion;
    private AsssignmentQuestion currentQuestionData;
    private TextView  purpose_first_Name,purpose_sub_name,purpose_last_updated,purpose_type,purpose_solutions,submit,cancel;
    private ImageView purpose_user_image;
    private TextView  info_youranswer;
    private RelativeLayout purpose_submit_cancel;
    private LinearLayout purpose_answer;
    private  TextView info_correctanswer;
    private String tempAnswerForCurrentQuestion;
    private String attempted_answer_given;
    private TeacherPurposeExamFragment currentQuestionFragment;
    private Bundle bundle;
    private JSONObject record_result;
    private JSONArray submit_userAnswer,submit_correctAnswer;
    private String submit_attempt_correctAnswer,submit_attempt_userAnswer;
    private String assignmentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_teacher_purpose_exam);
        session = SessionManager.getInstance(this);




        info_youranswer=(TextView)findViewById(R.id.purpose_uranswer);
        info_correctanswer=(TextView)findViewById(R.id.purpose_crtanser);
        purpose_solutions=(TextView)findViewById(R.id.purpose_solutions);
        purpose_submit_cancel=(RelativeLayout)findViewById(R.id.purpose_submit_cancel);
        purpose_answer=(LinearLayout)findViewById(R.id.purpose_answer);
        purpose_first_Name=(TextView)findViewById(R.id.purpose_firstname);
        purpose_user_image=(ImageView)findViewById(R.id.purpose_thumbnail);
        purpose_sub_name=(TextView)findViewById(R.id.purpose_name);
        purpose_last_updated=(TextView)findViewById(R.id.purpose_date);
        purpose_type=(TextView)findViewById(R.id.purpose_type);
        assignmentName = getIntent().getStringExtra(ConstantGlobal.ASSIGNMENT_NAME);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // getSupportActionBar().setIcon(R.drawable.ic_launcher);
        getSupportActionBar().setDisplayShowCustomEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(assignmentName);
        session = SessionManager.getInstance(this);



        questionId=getIntent().getStringExtra(ConstantGlobal.ID);
        purpose_solutions.setOnClickListener(this);






        Log.e("ppp","...."+questionId);




        if(NetUtils.isOnline(TeacherPurposeExamActivity.this)) {
            new QuestionInfo().execute();
        }else{
            Toast.makeText(TeacherPurposeExamActivity.this, R.string.no_internet_msg, Toast.LENGTH_SHORT).show();
        }






    }

    public AsssignmentQuestion getCurrentQuestionData() {
        return asssignmentQuestion;
    }

    @Override
    public void setAnswerForCurrentQuestion(String newAnswer) {

        tempAnswerForCurrentQuestion=newAnswer;
    }

    @Override
    public void onClick(View v) {
        Intent intent=new Intent(TeacherPurposeExamActivity.this, AssignmentSolutionActivity.class);
        intent.putExtra(ConstantGlobal.QID,userPurposeInfo.id);
        intent.putExtra(ConstantGlobal.ASSIGNMENT_NAME,assignmentName);
        startActivity(intent);
    }

    class QuestionInfo extends AsyncTask<JSONObject, JSONObject, JSONObject> {
        private String url;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected JSONObject doInBackground(JSONObject... JSONObject) {

            org.json.JSONObject jsonRes = null;
            this.url = session.getApiUrl("getQuestionInfo");

            Map<String, Object> httpParams = new HashMap<String, Object>();
            httpParams.put("id", questionId);
            httpParams.put("parent.type","ORGANIZATION");
            httpParams.put("parent.id",session.getOrgMemberInfo().orgId);
            httpParams.put("callingAppId", "TapApp");
            httpParams.put("callingApp", "TapApp");

            session.addSessionParams(httpParams);

            try {
                jsonRes = VedantuWebUtils.getJSONData(url, VedantuWebUtils.GET_REQ, httpParams);



            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.e("questioninfo", "..........." + jsonRes);
            return jsonRes;
        }


        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);

            if (jsonObject!=null){


                try{

                    JSONObject result=jsonObject.getJSONObject("result");
                    userPurposeInfo=new UserPurposeInfo();
                    userPurposeInfo.solutions=result.getString("solutions");
                    userPurposeInfo.attempted=result.getString("attempted");
                    userPurposeInfo.lastUpdated=result.getString("lastUpdated");
                    userPurposeInfo.type=result.getString("type");
                    userPurposeInfo.id=result.getString("id");

                    JSONObject user=result.getJSONObject("user");


                    userPurposeInfo.thumbnail=user.getString("thumbnail");
                    userPurposeInfo.firstName=user.getString("firstName");
                    userPurposeInfo.lastName=user.getString("lastName");
                    userPurposeInfo.profile=user.getString("profile");
                    JSONArray boardTree=result.getJSONArray("boardTree");

                    for (int i=0;i<boardTree.length();i++){   purpose_submit_cancel=(RelativeLayout)findViewById(R.id.purpose_submit_cancel);
        purpose_answer=(LinearLayout)findViewById(R.id.purpose_answer);

                        JSONObject boardTree_object=boardTree.getJSONObject(i);
                        userPurposeInfo.name=boardTree_object.getString("name");

                    }


                    userPurposeInfo.content=result.getString("content");
                    userPurposeInfo.option=result.optJSONArray("options");

                    if (userPurposeInfo.option != null) {
                        for (int z = 0; z < userPurposeInfo.option.length(); z++) {
                            try {
                                if (z == 0)
                                    optionsString = userPurposeInfo.option.getString(z);
                                else
                                    optionsString += "#" + userPurposeInfo.option.getString(z);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }


                    JSONObject answer=result.optJSONObject("answer");


                    if (answer!=null) {

//                    correctAnswer=null;
//                    answegiven=null;

                        JSONArray server_correct_answer = answer.getJSONArray("correctAnswer");
                        JSONArray server_answer_given = answer.optJSONArray("answerGiven");

                        try {

                            attempted_correct_answer = "";

                            for (int l = 0; l< server_correct_answer.length(); l++) {

                                String str = server_correct_answer.getString(l);
                                if (l != server_correct_answer.length() - 1) {
                                    attempted_correct_answer = attempted_correct_answer + str + ",";
                                } else {
                                    attempted_correct_answer = attempted_correct_answer + str;
                                }


                            }



                        } catch (JSONException e) {
                            Log.e("JSON", "There was an error parsing the JSON", e);
                        }
                        if (server_answer_given!=null) {

                            try {
                                attempted_answer_given="";

                                for (int l = 0; l < server_answer_given.length(); l++) {

                                    String str = server_answer_given.getString(l);
                                    if (l != server_answer_given.length() - 1) {
                                        attempted_answer_given = attempted_answer_given + str + ",";
                                    } else {
                                        attempted_answer_given = attempted_answer_given + str;
                                    }


                                }


                            } catch (JSONException e) {
                                Log.e("JSON", "There was an error parsing the JSON", e);
                            }
                        }

                    }



                    info_youranswer.setText(attempted_answer_given);
                    info_correctanswer.setText(attempted_correct_answer);


                    asssignmentQuestion=new AsssignmentQuestion();
                    asssignmentQuestion.name=userPurposeInfo.content;
                    asssignmentQuestion.options=optionsString;
                    asssignmentQuestion.type=userPurposeInfo.type;


                    setUpAssignmentQuestion();



                    if (userPurposeInfo.attempted!=null && userPurposeInfo.attempted.equalsIgnoreCase("true")){

                        purpose_submit_cancel.setVisibility(View.GONE);
                        purpose_answer.setVisibility(View.VISIBLE);
                        purpose_solutions.setVisibility(View.VISIBLE);

                    }
                    long val = Long.parseLong(userPurposeInfo.lastUpdated);
                    Date date=new Date(val);
                    SimpleDateFormat df2 = new SimpleDateFormat("dd/MM/yy");
                    String  dateText = df2.format(date);
                    purpose_last_updated.setText("Added on  "+dateText);
                    purpose_type.setText(userPurposeInfo.type);
                    Picasso.with(TeacherPurposeExamActivity.this).load(userPurposeInfo.thumbnail).into(purpose_user_image);
                    purpose_sub_name.setText(userPurposeInfo.name);
                    purpose_first_Name.setText("by "+userPurposeInfo.firstName+" "+userPurposeInfo.lastName+" ("+userPurposeInfo.profile+")");
                    purpose_solutions.setText("Solutions: "+userPurposeInfo.solutions);



                    submit=(TextView)findViewById(R.id.purpose_submit);
                    submit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {


                            if(NetUtils.isOnline(TeacherPurposeExamActivity.this)) {
                                if (StringUtils.isEmpty(tempAnswerForCurrentQuestion)){
                                    Toast.makeText(TeacherPurposeExamActivity.this, "Please answer the question first", Toast.LENGTH_SHORT).show();

                                }else {

                                    new UserPerformance().execute();
                                    purpose_submit_cancel.setVisibility(View.GONE);
                                    purpose_answer.setVisibility(View.VISIBLE);
                                    purpose_solutions.setVisibility(View.VISIBLE);


                                }
                            }
                            else{
                                Toast.makeText(TeacherPurposeExamActivity.this, R.string.no_internet_msg, Toast.LENGTH_SHORT).show();
                            }


                        }
                    });

                    cancel=(TextView)findViewById(R.id.purpose_cancel);
                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            currentQuestionFragment.resetAnswer();
                        }
                    });

                }catch (Exception e){

                    e.printStackTrace();
                }
            }


        }

    }

    private void setUpAssignmentQuestion() {



        currentQuestionFragment = new TeacherPurposeExamFragment();
        bundle = new Bundle();
        bundle.putString("content",userPurposeInfo.content);
        bundle.putString("options",optionsString);
        bundle.putString("type",userPurposeInfo.type);
        currentQuestionFragment.setArguments(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.framlayout, currentQuestionFragment).commit();
        currentQuestionData = getCurrentQuestionData();
        //  Log.e("ques","..."+currentQuestionData);


        asssignmentQuestion.name=userPurposeInfo.content;
        asssignmentQuestion.options=optionsString;
    }
    public class UserPerformance extends AsyncTask<Object, Object, JSONObject> {
        private String url;
        private ProgressDialog loading;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading=new ProgressDialog(TeacherPurposeExamActivity.this);
            loading.setMessage("Please wait...");
            loading.show();
        }

        @Override
        protected JSONObject doInBackground(Object... params) {


            JSONObject jsonRes = null;
            this.url = session.getApiUrl("recordAttempt");


            Map<String, Object> httpParams = new HashMap<String, Object>();

            httpParams.put("qId", userPurposeInfo.id);


            if (tempAnswerForCurrentQuestion!=null) {
                String[] datavalue = tempAnswerForCurrentQuestion.split("#");
                for (int i = 0; i < datavalue.length; i++) {
                    System.out.println(datavalue[i]);
                    httpParams.put("answerGiven[" + i + "]", datavalue[i]);
                }
            }

            httpParams.put("entityId",userPurposeInfo.id);
            httpParams.put("entityType","QUESTION");
            httpParams.put("type",userPurposeInfo.type);
            httpParams.put("status","COMPLETE");
            httpParams.put("callingAppId","TapApp");


            session.addSessionParams(httpParams);
            try {
                jsonRes = VedantuWebUtils.getJSONData(url, VedantuWebUtils.POST_REQ, httpParams);
                Log.e("recordAttempt", "..........." + jsonRes);
                record_result = jsonRes.getJSONObject("result");


            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return record_result;

        }


        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            loading.dismiss();
            if (jsonObject!=null){

                try {

                    submit_userAnswer = jsonObject.getJSONArray("userAnswer");
                    submit_correctAnswer = jsonObject.getJSONArray("correctAnswer");

                    Log.e("userans1","....."+submit_userAnswer);
                    Log.e("corrans2","....."+submit_correctAnswer);
                    try {


                        for (int l = 0; l< submit_userAnswer.length(); l++) {

                            String str = submit_userAnswer.getString(l);
                            if (l != submit_userAnswer.length() - 1) {
                                submit_attempt_userAnswer = submit_attempt_userAnswer + str + ",";
                            } else {
                                submit_attempt_userAnswer = submit_attempt_userAnswer + str;
                            }


                        }




                    } catch (JSONException e) {
                        Log.e("JSON", "There was an error parsing the JSON", e);
                    }


                    try {


                        for (int l = 0; l< submit_correctAnswer.length(); l++) {

                            String str = submit_correctAnswer.getString(l);
                            if (l != submit_correctAnswer.length() - 1) {
                                submit_attempt_correctAnswer = submit_attempt_correctAnswer + str + ",";
                            } else {
                                submit_attempt_correctAnswer = submit_attempt_correctAnswer + str;
                            }


                        }



                    } catch (JSONException e) {
                        Log.e("JSON", "There was an error parsing the JSON", e);
                    }






                    info_youranswer.setText(submit_attempt_userAnswer.replace("null",""));
                    info_correctanswer.setText(submit_attempt_correctAnswer.replace("null",""));


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
