package com.nhance.android.ChallengeArena;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
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
import com.nhance.android.managers.SessionManager;
import com.nhance.android.utils.VedantuWebUtils;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ClosedViewChallengeActivity extends AppCompatActivity implements ClosedViewExamFragment.NotifyTakeTestFragment{
    private SessionManager session;
    private String questionId,type,solutions,content,attempt_status,thumbnail,profile,firstName,name;
    private TextView closed_first_Name,closed_sub_name,submit,cancel,closed_last_updated,closed_type,closed_view_solutions;
private ImageView closed_user_image;
    private long lastUpdated;
    private AsssignmentQuestion cuAsssignmentQuestion;
    private JSONArray options;
    private String optionsString=null;
    private ClosedViewExamFragment currentQuestionFragment;
    private Bundle bundle;
    private String currentuserselectedAnswer;
  private  ProgressDialog loading;
    private JSONObject record_result;
    private JSONArray submit_userAnswer,submit_correctAnswer;
    private String attempt_userAnswer;
    private String attempt_correctAnswer;
    private RelativeLayout closed_submit_cancel;
    private LinearLayout closed_view_answer;
    private TextView  info_youranswer;
    TextView info_ccorrectanswer;
    private String option_correct_answer;
    private String option_answerg_given;
    private     FloatingActionButton fab1,fab;
    private String ques_id;
    private String challengeId,channelname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_closed_view_challenge);
        session = SessionManager.getInstance(this);
        questionId=getIntent().getStringExtra("questionId");
        channelname=getIntent().getStringExtra("channelname");
        Log.e("questionId","....."+questionId);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
     //   getSupportActionBar().setIcon(R.drawable.ic_launcher);
        getSupportActionBar().setDisplayShowCustomEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(channelname);

       //  fab = (FloatingActionButton) findViewById(R.id.fab);

if (NetUtils.isOnline(this)) {
    new viewchallenge().execute();

}else {

    Toast.makeText(this,R.string.no_internet_msg,Toast.LENGTH_SHORT).show();

}



        closed_first_Name=(TextView)findViewById(R.id.closed_view_firstname);
        closed_user_image=(ImageView)findViewById(R.id.closed_view_thumbnail);

        closed_sub_name=(TextView)findViewById(R.id.closed_view_name);
        closed_last_updated=(TextView)findViewById(R.id.closed_view_date);
        closed_type=(TextView)findViewById(R.id.closed_view_type);
        closed_view_solutions=(TextView)findViewById(R.id.closed_view_solutions);
        closed_submit_cancel=(RelativeLayout)findViewById(R.id.closed_submit_cancel);
        closed_view_answer=(LinearLayout)findViewById(R.id.closed_view_answer);

        info_youranswer=(TextView)findViewById(R.id.closed_view_uranswer);
        info_ccorrectanswer=(TextView)findViewById(R.id.closed_view_crtanser);


        submit=(TextView)findViewById(R.id.closed_view_submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(NetUtils.isOnline(ClosedViewChallengeActivity.this)) {
                    if (StringUtils.isEmpty(currentuserselectedAnswer)){
                        Toast.makeText(ClosedViewChallengeActivity.this, "Please answer the question first", Toast.LENGTH_SHORT).show();

                    }else {

                        new singlequestionpeformance().execute();
                        closed_submit_cancel.setVisibility(View.GONE);
                        closed_view_answer.setVisibility(View.VISIBLE);
                        closed_view_solutions.setVisibility(View.VISIBLE);



                    }
                }
                else{
                    Toast.makeText(ClosedViewChallengeActivity.this, R.string.no_internet_msg, Toast.LENGTH_SHORT).show();
                }


            }
        });

        cancel=(TextView)findViewById(R.id.closed_view_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentQuestionFragment.resetAnswer();
            }
        });

    }


    public class viewchallenge extends AsyncTask<JSONObject, JSONObject, JSONObject> {

        private ProgressDialog loading;


        @Override
        protected JSONObject doInBackground(JSONObject... jsonObjects) {
            String url = session.getApiUrl("getQuestionInfo");


            JSONObject jsonRes = null;

            HashMap<String, Object> httpParams = new HashMap<String, Object>();

            httpParams.put("callingApp", "TabApp");
            httpParams.put("callingAppId", "TabApp");
            httpParams.put("parent.id", session.getOrgMemberInfo().orgId);
            httpParams.put("parent.type", "ORGANIZATION");
            httpParams.put("resultType", "ALL");
            httpParams.put("id",questionId);


            session.addSessionParams(httpParams);

            try {
                jsonRes = VedantuWebUtils.getJSONData(url, VedantuWebUtils.GET_REQ, httpParams);
                Log.e("questioninfo","...."+jsonRes);
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
            JSONObject result=jsonObject.getJSONObject("result");
         ques_id=result.getString("id");

             type=result.getString("type");
             solutions=result.getString("solutions");
             lastUpdated=result.getLong("lastUpdated");
             content=result.getString("content");
             options=result.optJSONArray("options");
        attempt_status=result.getString("attempted");
        challengeId=result.getString("challengeId");
        Log.e("iss","..."+challengeId);

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


        JSONObject answer = result.optJSONObject("answer");



        if (answer!=null) {

            JSONArray correctanswer = answer.getJSONArray("correctAnswer");
            JSONArray answegiven = answer.optJSONArray("answerGiven");


            try {

                option_correct_answer = "";

                for (int l = 0; l< correctanswer.length(); l++) {

                    String str = correctanswer.getString(l);
                    if (l != correctanswer.length() - 1) {
                        option_correct_answer = option_correct_answer + str + ",";
                    } else {
                        option_correct_answer = option_correct_answer + str;
                    }


                }



            } catch (JSONException e) {
                Log.e("JSON", "There was an error parsing the JSON", e);
            }
            if (answegiven!=null) {

                try {
                    option_answerg_given="";

                    for (int l = 0; l < answegiven.length(); l++) {

                        String str = answegiven.getString(l);
                        if (l != answegiven.length() - 1) {
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


        info_youranswer.setText(option_answerg_given);


        info_ccorrectanswer.setText(option_correct_answer);






        
        
        


        JSONArray boardTree=result.getJSONArray("boardTree");

        for (int i=0;i<boardTree.length();i++){
            JSONObject boardTree_object=boardTree.getJSONObject(i);
            name=boardTree_object.getString("name");

        }
        JSONObject user=result.getJSONObject("user");
        thumbnail=user.getString("thumbnail");
        firstName=user.getString("firstName");
        profile=user.getString("profile");

        long val = Long.parseLong(String.valueOf(lastUpdated));
        Date date=new Date(val);
        SimpleDateFormat df2 = new SimpleDateFormat("dd/MM/yy");
        String  dateText = df2.format(date);

        closed_type.setText(type);
        closed_sub_name.setText(name);
        closed_view_solutions.setText("Solutions: "+solutions);
        closed_user_image.setTag(thumbnail);
        closed_first_Name.setText("by "+firstName+"  "+profile);
        Picasso.with(ClosedViewChallengeActivity.this).load(thumbnail).into(closed_user_image);
        closed_last_updated.setText("Added on  "+dateText);




        cuAsssignmentQuestion=new AsssignmentQuestion();
        cuAsssignmentQuestion.name=content;
        cuAsssignmentQuestion.options=optionsString;
        cuAsssignmentQuestion.type=type;


        setUpAssignmentQuestion();


        if (attempt_status!=null && attempt_status.equalsIgnoreCase("true")){

            closed_submit_cancel.setVisibility(View.GONE);
            closed_view_answer.setVisibility(View.VISIBLE);
            closed_view_solutions.setVisibility(View.VISIBLE);


        }



        closed_view_solutions.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        Intent intent=new Intent(ClosedViewChallengeActivity.this,ClosedChallengeSolutionActivity.class);
intent.putExtra("questionId",ques_id);
       startActivity(intent);
    }
});





    } catch (JSONException e) {
        e.printStackTrace();
    }



}


        }

    }



    public AsssignmentQuestion getCurrentQuestionData() {
        return cuAsssignmentQuestion;
    }

    private void setUpAssignmentQuestion() {



        currentQuestionFragment = new ClosedViewExamFragment();
        bundle = new Bundle();
        bundle.putString("content",content);
        bundle.putString("options",optionsString);
        bundle.putString("type",type);
        currentQuestionFragment.setArguments(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.framlayout, currentQuestionFragment).commit();
        cuAsssignmentQuestion = getCurrentQuestionData();
        Log.e("ques","..."+cuAsssignmentQuestion);


        cuAsssignmentQuestion.name=content;
        cuAsssignmentQuestion.options=optionsString;
    }



    @Override
    public void setAnswerForCurrentQuestion(String newAnswer) {

         currentuserselectedAnswer=newAnswer;

    }

    public class singlequestionpeformance extends AsyncTask<Object, Object, JSONObject> {
        private String url;
        private int position;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading=new ProgressDialog(ClosedViewChallengeActivity.this);
            loading.setMessage("Please wait...");
            loading.show();
        }

        @Override
        protected JSONObject doInBackground(Object... params) {


            JSONObject jsonRes = null;
            this.url = session.getApiUrl("recordAttempt");


            Map<String, Object> httpParams = new HashMap<String, Object>();

            httpParams.put("qId", questionId);


            if (currentuserselectedAnswer!=null) {
                String[] datavalue = currentuserselectedAnswer.split("#");
                for (int i = 0; i < datavalue.length; i++) {
                    System.out.println(datavalue[i]);
                    httpParams.put("answerGiven[" + i + "]", datavalue[i]);
                }
            }

            httpParams.put("entityId",questionId);
            httpParams.put("entityType","QUESTION");
            httpParams.put("type",type);
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

                    submit_userAnswer = record_result.getJSONArray("userAnswer");
                    submit_correctAnswer = record_result.getJSONArray("correctAnswer");

                    Log.e("userans1","....."+submit_userAnswer);
                    Log.e("corrans2","....."+submit_correctAnswer);
                    try {


                        for (int l = 0; l< submit_userAnswer.length(); l++) {

                            String str = submit_userAnswer.getString(l);
                            if (l != submit_userAnswer.length() - 1) {
                                attempt_userAnswer = attempt_userAnswer + str + ",";
                            } else {
                                attempt_userAnswer = attempt_userAnswer + str;
                            }


                        }




                    } catch (JSONException e) {
                        Log.e("JSON", "There was an error parsing the JSON", e);
                    }


                    try {


                        for (int l = 0; l< submit_correctAnswer.length(); l++) {

                            String str = submit_correctAnswer.getString(l);
                            if (l != submit_correctAnswer.length() - 1) {
                                attempt_correctAnswer = attempt_correctAnswer + str + ",";
                            } else {
                                attempt_correctAnswer = attempt_correctAnswer + str;
                            }


                        }



                    } catch (JSONException e) {
                        Log.e("JSON", "There was an error parsing the JSON", e);
                    }

                    Log.e("userAnswer","........."+attempt_userAnswer);
                    Log.e("correctAnswer","........."+attempt_correctAnswer);
                    info_youranswer.setText(attempt_userAnswer.replace("null",""));
                    info_ccorrectanswer.setText(attempt_correctAnswer.replace("null",""));


                } catch (JSONException e) {
                    e.printStackTrace();
                }




            }


        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.closed_challengearena, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.closed_challenge:
                if (NetUtils.isOnline(ClosedViewChallengeActivity.this)) {

                        Intent intent = new Intent(ClosedViewChallengeActivity.this, ClosedLeaderBoardActivity.class);
                        intent.putExtra("challengeId", challengeId);
                    Log.e("issssss","..."+challengeId);
                        startActivity(intent);

                } else {
                    Toast.makeText(ClosedViewChallengeActivity.this, R.string.no_internet_msg, Toast.LENGTH_SHORT).show();
                }

            default:

                return super.onOptionsItemSelected(item);
        }
    }

}
