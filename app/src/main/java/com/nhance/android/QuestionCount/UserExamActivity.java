package com.nhance.android.QuestionCount;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.itextpdf.text.pdf.parser.Line;
import com.nhance.android.R;
import com.nhance.android.assignment.StudentPerformance.AsssignmentQuestion;
import com.nhance.android.assignment.StudentPerformance.NetUtils;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.QuestionAttemptStatusDataManager;
import com.nhance.android.db.models.entity.Content;
import com.nhance.android.db.models.entity.QuestionStatus;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.managers.VApp;
import com.nhance.android.pojos.LibraryContentRes;
import com.nhance.android.utils.VedantuWebUtils;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserExamActivity extends AppCompatActivity {
    private String  content,type,lastUpdated,ownerName,courseName;
    public int solutions;
    private JSONArray options;
    private String optionsString=null;
    AsssignmentQuestion asssignmentQuestion;
    private UserExamFragment currentQuestionFragment;
    private Bundle bundle;
    private TextView first_Name,last_Name,profile_name,sub_name,last_updated,ques_type,
            view_solutions_count, yourAnswerTextView,correctAnswerTextView, submitButton, cancelButton;
    private String dateText;
    private LinearLayout userAnswerLayout, submitButtonLayout;
    private boolean attempted;
    private String userAnswerString, correctAnswerString;
    private SessionManager session;
    private String questionId;
    private int position;
    private String tempAnswerForCurrentQuestion;
    private QuestionAttemptStatusDataManager questionAttemptStatus;
    private long startTime;
    private int syncStatus;
    private String thumbnail;
    private ImageView user_image;

    private String info;
//    private FloatingActionButton infoFloatingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_user_exam);
         startTime = System.currentTimeMillis();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setIcon(R.drawable.ic_launcher);
        getSupportActionBar().setDisplayShowCustomEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Question");
        first_Name=(TextView)findViewById(R.id.view_firstname);
        user_image=(ImageView)findViewById(R.id.view_thumbnail);
        profile_name=(TextView)findViewById(R.id.view_firstname);
        sub_name=(TextView)findViewById(R.id.view_name);
        last_updated=(TextView)findViewById(R.id.view_date);
        ques_type=(TextView)findViewById(R.id.view_type);
        sub_name=(TextView)findViewById(R.id.view_name);
        yourAnswerTextView = (TextView)findViewById(R.id.view_uranswer);
        correctAnswerTextView = (TextView)findViewById(R.id.view_crtanser);
        userAnswerLayout = (LinearLayout)findViewById(R.id.view_answer);
        submitButton=(TextView)findViewById(R.id.view_submit);
        cancelButton=(TextView)findViewById(R.id.view_cancel);
        submitButtonLayout = (LinearLayout)findViewById(R.id.submit_button_layout);
        view_solutions_count=(TextView)findViewById(R.id.view_solutions_count);
//        infoFloatingButton = (FloatingActionButton)findViewById(R.id.info_floating_button);

        String name=getIntent().getStringExtra("name");
        type=getIntent().getStringExtra("type");
        questionId = getIntent().getStringExtra("id");
        lastUpdated=getIntent().getStringExtra(ConstantGlobal.LAST_UPDATED);
        ownerName=getIntent().getStringExtra("ownerName");
        courseName=getIntent().getStringExtra("courseName");
        info=getIntent().getStringExtra("info");
        attempted = getIntent().getBooleanExtra("status", false);
        userAnswerString = getIntent().getStringExtra("userAnswer");
        correctAnswerString = getIntent().getStringExtra("correctAnswer");
        position = getIntent().getIntExtra("position", -1);
        thumbnail = getIntent().getStringExtra("thumbnail");

        Log.e("attempted", ".."+attempted+"userAnswer "+userAnswerString+"correctAnswer "+correctAnswerString);

        session = SessionManager.getInstance(this);
        questionAttemptStatus = new QuestionAttemptStatusDataManager(getApplicationContext());
        if(attempted){
            userAnswerLayout.setVisibility(View.VISIBLE);
            submitButtonLayout.setVisibility(View.GONE);
            yourAnswerTextView.setText(" "+userAnswerString.trim());
            correctAnswerTextView.setText(" "+correctAnswerString.trim());
            view_solutions_count.setVisibility(View.VISIBLE);
        }else{
            userAnswerLayout.setVisibility(View.GONE);
            view_solutions_count.setVisibility(View.GONE);
            submitButtonLayout.setVisibility(View.VISIBLE);

        }

        if(thumbnail != null){
            Picasso.with(UserExamActivity.this).load(thumbnail).into(user_image);
        }

        long val = Long.parseLong(lastUpdated);
        Date date=new Date(val);
        SimpleDateFormat df2 = new SimpleDateFormat("dd/MM/yy");
        dateText = df2.format(date);
        System.out.println(dateText);


        try {
            JSONObject jsonObject=new JSONObject(info);
            content=jsonObject.getString("content");
            options=jsonObject.optJSONArray("options");
            solutions=jsonObject.getInt("solutions");



            if (options != null) {
                for (int z = 0; z < options.length(); z++) {
                    try {
                        if (z == 0)
                            optionsString = options.getString(z);
                        else
                            optionsString += "#" + options.getString(z);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }



            ques_type.setText(type);
            last_updated.setText("Added on  "+dateText);
            profile_name.setText(ownerName);
            sub_name.setText(courseName);
            view_solutions_count.setText("Solutions: "+solutions);

            Log.e("solutions","...."+solutions);
        } catch (JSONException e) {
            e.printStackTrace();
        }




        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                if(NetUtils.isOnline(UserExamActivity.this)) {
                    if (StringUtils.isEmpty(tempAnswerForCurrentQuestion)){
                        Toast.makeText(UserExamActivity.this, "Please answer the question first", Toast.LENGTH_SHORT).show();

                    }else {

                        new SingleQuestionPerformance(questionId, tempAnswerForCurrentQuestion, type, UserExamActivity.this).execute();


                    }
//                }else{
//                    Toast.makeText(UserExamActivity.this, R.string.no_internet_msg, Toast.LENGTH_SHORT).show();
//                }


            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                currentQuestionFragment.resetAnswer();

            }
        });

        view_solutions_count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(NetUtils.isOnline(UserExamActivity.this)) {


                        Intent intent = new Intent(UserExamActivity.this, QuestionSolutionActivity.class);
                        intent.putExtra(ConstantGlobal.ID, questionId);
                        startActivityForResult(intent, 101);


                }else{
                    Toast.makeText(UserExamActivity.this, R.string.no_internet_msg, Toast.LENGTH_SHORT).show();
                }
            }
        });


        asssignmentQuestion=new AsssignmentQuestion();
        asssignmentQuestion.name=content;
        asssignmentQuestion.options=optionsString;
        asssignmentQuestion.type=type;

        setUpAssignmentQuestion();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void setUpAssignmentQuestion() {



        currentQuestionFragment = new UserExamFragment();
        bundle = new Bundle();
        bundle.putString("content",content);
        bundle.putString("options",optionsString);
        bundle.putString("type",type);
        currentQuestionFragment.setArguments(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.framlayout, currentQuestionFragment).commit();
        asssignmentQuestion = getCurrentQuestionData();


        asssignmentQuestion.name=content;
       asssignmentQuestion.options=optionsString;
    }

    public AsssignmentQuestion getCurrentQuestionData() {



        return asssignmentQuestion;

    }

    public void setAnswerForCurrentQuestion(String newAnswer) {
        tempAnswerForCurrentQuestion = newAnswer;
        Log.e("currentanswer","...."+tempAnswerForCurrentQuestion);
    }

    public class SingleQuestionPerformance extends AsyncTask<Object, Object, JSONObject> {
        private String url;
        private int attempts;
        private ProgressDialog progressDialog;
        public String questionId;
        public String tempAnswerForCurrentQuestion;
        public String questionType;
        private JSONObject result;
        private JSONArray userAnswer, correctAnswer;
        private String option_correct_answer;
        private String option_answerGiven;
        private Context context;
        private  String timeCreated;
        private long timeTaken;


        public SingleQuestionPerformance(String questionId, String tempAnswerForCurrentQuestion, String questionType, Context context){
            this.questionId = questionId;
            this.tempAnswerForCurrentQuestion = tempAnswerForCurrentQuestion;
            this.questionType = questionType;
            this.context = context;
            progressDialog = ProgressDialog.show(context, "", "Loading. Please wait...", true);
            timeCreated = String.valueOf(Calendar.getInstance().getTimeInMillis());
            long tEnd = System.currentTimeMillis();
            long tDelta = tEnd - startTime;
            timeTaken  = System.currentTimeMillis() - startTime;
            Log.e("timeTaken", ".."+timeTaken);
        }
        
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected JSONObject doInBackground(Object... params) {


            JSONObject jsonRes = null;
            this.url = session.getApiUrl("recordAttempt");

            Map<String, Object> httpParams = new HashMap<String, Object>();

            httpParams.put("qId", questionId);
            if (tempAnswerForCurrentQuestion!=null) {
                String[] datavalue = tempAnswerForCurrentQuestion.split("#");
                for (int i = 0; i < datavalue.length; i++) {
                    System.out.println(datavalue[i]);
                    httpParams.put("answerGiven[" + i + "]", datavalue[i]);
                }
            }

            httpParams.put("entityId",questionId);
            httpParams.put("entityType","QUESTION");
            httpParams.put("type",questionType);
            httpParams.put("status","COMPLETE");
            httpParams.put("callingAppId","TapApp");
            httpParams.put("timeCreated", timeCreated);
            httpParams.put("timeTaken", timeTaken);


            session.addSessionParams(httpParams);
            try {
                jsonRes = VedantuWebUtils.getJSONData(url, VedantuWebUtils.POST_REQ, httpParams);
                Log.e("recordAttempt", "..........." + jsonRes);
                if(jsonRes != null)
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
            if(progressDialog!=null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
            if (jsonObject!=null){


                try {

                  userAnswer = result.getJSONArray("userAnswer");
                   correctAnswer = result.getJSONArray("correctAnswer");

                    Log.e("userans1","....."+userAnswer);
                    Log.e("corrans2","....."+correctAnswer);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
               syncStatus = 1;
            }else{
                syncStatus = -1;
            }
            if(userAnswer == null){
                userAnswer = new JSONArray();
                userAnswer.put(tempAnswerForCurrentQuestion);
            }


            attempted = true;
            QuestionStatus questionStatus1 = questionAttemptStatus.getQuestionStatus(questionId);
            Log.e("questionStatus1",".."+questionStatus1);

            if (questionStatus1 != null && questionStatus1.id.equalsIgnoreCase(questionId)) {
                if(correctAnswer == null)
                    correctAnswer = questionStatus1.correctAnswer;
                attempts = questionStatus1.attempts+1;

                QuestionStatus  questionStatus = new QuestionStatus(questionId, correctAnswer, attempted, userAnswer, attempts, timeCreated, timeTaken, questionType, syncStatus);
                Log.e("update ", ".."+questionStatus);
                questionAttemptStatus.updateQuestionStatus(questionId, questionStatus);
            }

            if (correctAnswer != null) {
                try {

                    option_correct_answer = "";

                    for (int l = 0; l < correctAnswer.length(); l++) {

                        String str = correctAnswer.getString(l);
                        if (l != correctAnswer.length() - 1) {
                            option_correct_answer = option_correct_answer + str + ",";
                        } else {
                            option_correct_answer = option_correct_answer + str;
                        }


                    }


                } catch (JSONException e) {
                    Log.e("JSON", "There was an error parsing the JSON", e);
                }


            }

            if (userAnswer != null) {

                try {

                    option_answerGiven = "";

                    for (int l = 0; l < userAnswer.length(); l++) {

                        String str = userAnswer.getString(l);
                        if (l != correctAnswer.length() - 1) {
                            option_answerGiven = option_answerGiven + str + ",";
                        } else {
                            option_answerGiven = option_answerGiven + str;
                        }


                    }


                } catch (JSONException e) {
                    Log.e("JSON", "There was an error parsing the JSON", e);
                }

            }
            view_solutions_count.setVisibility(View.VISIBLE);
            yourAnswerTextView.setText(option_answerGiven);
            correctAnswerTextView.setText(option_correct_answer);
            userAnswerLayout.setVisibility(View.VISIBLE);
            submitButtonLayout.setVisibility(View.GONE);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == 102 && requestCode ==101 && data != null){
            int solCount = data.getIntExtra("solCount", 0);
            if(solCount != 0){
                solutions = solCount;
                view_solutions_count.setText("Solutions: "+solutions);
                JSONObject infoObject= null;
                try {
                    infoObject = new JSONObject(info);
                    infoObject.put("solutions", solutions);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

               VApp.updateQuestionList(infoObject, position);
            }
        }

    }

    @Override
    public void onBackPressed() {
        Log.e("onBackPress ", "..called");
        Intent intent = new Intent();
        intent.putExtra("attempted", attempted);
        intent.putExtra("questionId", questionId);
        intent.putExtra("position", position);
        Log.e("onBackPress ", "..called"+intent.getBooleanExtra("attempted", false));
        setResult(Activity.RESULT_OK, intent);
        finish();
        super.onBackPressed();
    }
}
