package com.nhance.android.assignment.UserPerformance;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nhance.android.R;
import com.nhance.android.assignment.StudentPerformance.AsssignmentQuestion;
import com.nhance.android.assignment.StudentPerformance.NetUtils;
import com.nhance.android.assignment.UserPerformance.fragment.AssignmentExamQuestionFragment;

import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.utils.GoogleAnalyticsUtils;
import com.nhance.android.utils.VedantuWebUtils;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StudentAssignmentActivity extends AppCompatActivity implements AssignmentExamQuestionFragment.NotifyTakeTestFragment{

    private TextView ass_type,ass_solutions,submit,cancel;
    private String question_type,solutions,options,content,answer;
    private JSONArray jsonArray;
    private String optionsString;
    private AsssignmentQuestion asssignmentQuestion;
    private AssignmentExamQuestionFragment currentQuestionFragment;
    private Bundle bundle;
    private AsssignmentQuestion currentQuestionData;
    private String tempAnswerForCurrentQuestion,user_attempt_status;
    private RelativeLayout submit_cancel;
    private SessionManager session;
    private JSONObject result;
    private String questionId,position;


    private TextView  info_youranswer;
    TextView info_ccorrectanswer,text_position;
    private LinearLayout view_answer;
    private String option_answerg_given;
    private String option_correct_answer;
    private String entityId,entityType;
    private JSONArray record_userAnswer,record_correctAnswer;
    private String submit_optionuserAnswer,submit_optioncorrectAnswer;
    private String assignmentName;
    private TextView attempt_btn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GoogleAnalyticsUtils.setScreenName(ConstantGlobal.GA_SCREEN_TAKE_ASSIGNMENT);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_student_assignment);
        assignmentName = getIntent().getStringExtra(ConstantGlobal.ASSIGNMENT_NAME);
        session = SessionManager.getInstance(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
       // getSupportActionBar().setIcon(R.drawable.ic_launcher);
        getSupportActionBar().setDisplayShowCustomEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(assignmentName);
        session = SessionManager.getInstance(this);





        ass_type = (TextView) findViewById(R.id.ass_questiontype);
        //ass_solutions = (TextView) findViewById(R.id.ass_asolution_count);
        submit_cancel=(RelativeLayout)findViewById(R.id.submit_cancel);
        view_answer=(LinearLayout)findViewById(R.id.view_answer);


        info_youranswer=(TextView)findViewById(R.id.view_uranswer);
        info_ccorrectanswer=(TextView)findViewById(R.id.view_crtanser);
        text_position=(TextView)findViewById(R.id.position);




        question_type=getIntent().getStringExtra(ConstantGlobal.TYPE);
        solutions=getIntent().getStringExtra(ConstantGlobal.SOLUTION);
        options=getIntent().getStringExtra(ConstantGlobal.OPTIONS);
        content=getIntent().getStringExtra(ConstantGlobal.CONTENT);
        user_attempt_status=getIntent().getStringExtra(ConstantGlobal.STATUS);
        questionId=getIntent().getStringExtra(ConstantGlobal.ID);
        answer=getIntent().getStringExtra(ConstantGlobal.ANSWER);
        entityId=getIntent().getStringExtra(ConstantGlobal.ENTITY_ID);
        entityType=getIntent().getStringExtra(ConstantGlobal.ENTITY_TYPE);
        position=getIntent().getStringExtra(ConstantGlobal.POSITION);









        attempt_btn=(TextView)findViewById(R.id.attempt_btn);
        attempt_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(StudentAssignmentActivity.this,UserPurposeAssignmentActivity.class);
                intent.putExtra(ConstantGlobal.ID,questionId);
                intent.putExtra(ConstantGlobal.ASSIGNMENT_NAME,assignmentName);
                startActivity(intent);

            }
        });
















        try {
            JSONObject answer_object=new JSONObject(answer);


            if (answer!=null) {

//                    correctAnswer=null;
//                    answegiven=null;

                JSONArray server_correct_answer = answer_object.getJSONArray("correctAnswer");
                JSONArray server_answer_given = answer_object.optJSONArray("answerGiven");

                try {

                    option_correct_answer = "";

                    for (int l = 0; l< server_correct_answer.length(); l++) {

                        String str = server_correct_answer.getString(l);
                        if (l != server_correct_answer.length() - 1) {
                            option_correct_answer = option_correct_answer + str + ",";
                        } else {
                            option_correct_answer = option_correct_answer + str;
                        }


                    }



                } catch (JSONException e) {
                    Log.e("JSON", "There was an error parsing the JSON", e);
                }
                if (server_answer_given!=null) {

                    try {
                     option_answerg_given="";

                        for (int l = 0; l < server_answer_given.length(); l++) {

                            String str = server_answer_given.getString(l);
                            if (l != server_answer_given.length() - 1) {
                                option_answerg_given = option_answerg_given + str + ",";
                            } else {
                                option_answerg_given = option_answerg_given + str;
                            }


                        }


                    } catch (JSONException e) {
                        Log.e("JSON", "There was an error parsing the JSON", e);
                    }
                }

            }




        } catch (JSONException e) {
            e.printStackTrace();
        }


        try {
            jsonArray = new JSONArray(options);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (jsonArray != null) {
            for (int z = 0; z < jsonArray.length(); z++) {
                try {
                    if (z == 0)
                        optionsString = jsonArray.getString(z);
                    else
                        optionsString += "#" + jsonArray.getString(z);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }



        asssignmentQuestion=new AsssignmentQuestion();
        asssignmentQuestion.name=content;
        asssignmentQuestion.options=optionsString;
        asssignmentQuestion.type=question_type;


        ass_type.setText(question_type);
      //  ass_solutions.setText("Solutions: "+solutions );
       text_position.setText(position);

        info_youranswer.setText(option_answerg_given);
        info_ccorrectanswer.setText(option_correct_answer);

        setUpAssignmentQuestion();
        if (user_attempt_status!=null && user_attempt_status.equalsIgnoreCase("ATTEMPTED")){

            attempt_btn.setVisibility(View.VISIBLE);


            submit_cancel.setVisibility(View.GONE);
        view_answer.setVisibility(View.VISIBLE);

        }


        submit=(TextView)findViewById(R.id.view_submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(NetUtils.isOnline(StudentAssignmentActivity.this)) {
                    if (StringUtils.isEmpty(tempAnswerForCurrentQuestion)){
                        Toast.makeText(StudentAssignmentActivity.this, "Please answer the question first", Toast.LENGTH_SHORT).show();

                    }else {

                     new singleperformance().execute();
                        submit_cancel.setVisibility(View.GONE);
                      view_answer.setVisibility(View.VISIBLE);
                        attempt_btn.setVisibility(View.VISIBLE);


                    }
                }
                else{
                    Toast.makeText(StudentAssignmentActivity.this, R.string.no_internet_msg, Toast.LENGTH_SHORT).show();
                }


            }
        });

        cancel=(TextView)findViewById(R.id.view_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentQuestionFragment.resetAnswer();
            }
        });



    }
    public class singleperformance extends AsyncTask<Object, Object, JSONObject> {
        private String url;
        private int position;



        @Override
        protected JSONObject doInBackground(Object... params) {


            JSONObject jsonRes = null;
            this.url = session.getApiUrl("recordAttempt");


            Map<String, Object> httpParams = new HashMap<String, Object>();

            httpParams.put("qId", questionId
            );


            if (tempAnswerForCurrentQuestion != null) {
                String[] datavalue = tempAnswerForCurrentQuestion.split("#");
                for (int i = 0; i < datavalue.length; i++) {
                    System.out.println(datavalue[i]);
                    httpParams.put("answerGiven[" + i + "]", datavalue[i]);
                }
            }

            httpParams.put("entityId", entityId);
            httpParams.put("entityType", entityType);

            session.addSessionParams(httpParams);
            try {
                jsonRes = VedantuWebUtils.getJSONData(url, VedantuWebUtils.POST_REQ, httpParams);
                Log.e("recordAttempt", "..........." + jsonRes);
                result = jsonRes.getJSONObject("result");


            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;

        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            if (jsonObject != null) {
                try {

                    record_userAnswer = result.getJSONArray("userAnswer");
                    record_correctAnswer = result.getJSONArray("correctAnswer");

                    Log.e("userans1", "....." + record_userAnswer);
                    Log.e("corrans2", "....." + record_correctAnswer);
                    try {
                        for (int l = 0; l < record_userAnswer.length(); l++) {
                            String str = record_userAnswer.getString(l);
                            if (l != record_userAnswer.length() - 1) {
                                submit_optionuserAnswer = submit_optionuserAnswer + str + ",";
                            } else {
                                submit_optionuserAnswer = submit_optionuserAnswer + str;
                            }
                        }
                    } catch (JSONException e) {
                        Log.e("JSON", "There was an error parsing the JSON", e);
                    }
                    try {
                        for (int l = 0; l < record_correctAnswer.length(); l++) {
                            String str = record_correctAnswer.getString(l);
                            if (l != record_correctAnswer.length() - 1) {
                                submit_optioncorrectAnswer = submit_optioncorrectAnswer + str + ",";
                            } else {
                                submit_optioncorrectAnswer = submit_optioncorrectAnswer + str;
                            }
                        }


                    } catch (JSONException e) {
                        Log.e("JSON", "There was an error parsing the JSON", e);
                    }




               info_youranswer.setText("");
                    info_youranswer.setText(submit_optionuserAnswer.replace("null",""));
                    info_ccorrectanswer.setText("");
                    info_ccorrectanswer.setText(submit_optioncorrectAnswer.replace("null",""));


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    private void setUpAssignmentQuestion() {



        currentQuestionFragment = new AssignmentExamQuestionFragment();
        bundle = new Bundle();
        bundle.putString("content",content);
        bundle.putString("options",optionsString);
        bundle.putString("type",question_type);
        currentQuestionFragment.setArguments(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.framlayout, currentQuestionFragment).commit();
        currentQuestionData = getCurrentQuestionData();
      //  Log.e("ques","..."+currentQuestionData);


        asssignmentQuestion.name=content;
        asssignmentQuestion.options=optionsString;
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

                Intent intent = new Intent(StudentAssignmentActivity.this, AssignmentViewAnalyticsActivity.class);

                intent.putExtra(ConstantGlobal.ASSIGNMENT_NAME,
                        assignmentName);
                intent.putExtra(ConstantGlobal.ENTITY_ID, entityId);
                intent.putExtra(ConstantGlobal.ENTITY_TYPE, entityType);
//                intent.putExtra(ConstantGlobal.TARGET_USERID, targetUserId);/
                startActivity(intent);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public AsssignmentQuestion getCurrentQuestionData() {

        return asssignmentQuestion;
    }
    @Override
    public void setAnswerForCurrentQuestion(String newAnswer) {
        tempAnswerForCurrentQuestion = newAnswer;
        //Log.e("currentanswer","...."+tempAnswerForCurrentQuestion);
    }
}
