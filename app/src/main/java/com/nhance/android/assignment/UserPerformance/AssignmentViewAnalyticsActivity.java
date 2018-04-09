package com.nhance.android.assignment.UserPerformance;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nhance.android.R;
import com.nhance.android.activities.AppLandingPageActivity;
import com.nhance.android.assignment.StudentPerformance.NetUtils;
import com.nhance.android.assignment.pojo.content.infos.AssignmentExtendedInfo;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.utils.GoogleAnalyticsUtils;
import com.nhance.android.utils.VedantuWebUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

//import com.android.volley.RequestQueue;

public class AssignmentViewAnalyticsActivity extends AppCompatActivity {
    private String entity_id,entity_type,targetUserId, assignmentName;
    private SessionManager session;

    private TextView ass_firstnemre,ass_name;
    private TextView ass_profile,ass_correct,ass_incorrect;
    private String user_firstname,user_lastName;
    private String user_profile;
    private String ass_correctanswer;
    private String ass_incorrectanswer;
    private ImageView ass_thumbnail;
    private String user_thumbnail;
    private String ass_username;
    private int progressStatus=0;
    private ProgressBar pb_drawable;
    private ImageView profile_img;
    //private RequestQueue requestQueue;
    private HashMap<String, String> song = new HashMap<String, String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        GoogleAnalyticsUtils.setScreenName(ConstantGlobal.GA_SCREEN_TAKE_ASSIGNMENT);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        setContentView(R.layout.activity_view_analytics);

        session = SessionManager.getInstance(this);
        entity_id = getIntent().getStringExtra(ConstantGlobal.ENTITY_ID);
        entity_type = getIntent().getStringExtra(ConstantGlobal.ENTITY_TYPE);
        targetUserId = getIntent().getStringExtra(ConstantGlobal.TARGET_USERID);
        pb_drawable = (ProgressBar) findViewById(R.id.pb_drawable);
        assignmentName = getIntent().getStringExtra(ConstantGlobal.ASSIGNMENT_NAME);



        if(NetUtils.isOnline(AssignmentViewAnalyticsActivity.this)) {
            new userDetails().execute();
        }else{
            Toast.makeText(AssignmentViewAnalyticsActivity.this, R.string.no_internet_msg, Toast.LENGTH_SHORT).show();
        }


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
       // getSupportActionBar().setIcon(R.drawable.ic_launcher);
        getSupportActionBar().setDisplayShowCustomEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(assignmentName);

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
//                Intent i = new Intent(AssignmentViewAnalyticsActivity.this, AppLandingPageActivity.class);
//                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    class userDetails extends AsyncTask<Object, Object, JSONObject> {
        private String url;
        private AssignmentExtendedInfo assignmentInfo;

        @Override
        protected JSONObject doInBackground(Object... strings) {
            JSONObject jsonRes = null;
            this.url = session.getApiUrl("getUserEntityMeasures");



            Map<String, Object> httpParams = new HashMap<String, Object>();
            httpParams.put("entity.id", entity_id);
            httpParams.put("entity.type", entity_type);
            httpParams.put("targetUserId",targetUserId);

            session.addSessionParams(httpParams);

            try {
                jsonRes = VedantuWebUtils.getJSONData(url, VedantuWebUtils.GET_REQ, httpParams);
                Log.d("jsonres","jsonres"+jsonRes);



                for (int j=0;j<=jsonRes.length();j++) {
                    JSONObject result = jsonRes.getJSONObject("result");
                    JSONObject measure = result.getJSONObject("measures");
                    JSONObject user = result.getJSONObject("user");
                    JSONObject mappings = user.getJSONObject("mappings");
                    JSONArray programs = mappings.getJSONArray("programs");


                    for (int i=0;i<programs.length();i++){
                        JSONObject jsonObject=programs.getJSONObject(i);
                        ass_username=jsonObject.getString("name");

                    }



                    user_firstname=user.getString("firstName");

                    user_lastName=user.getString("lastName");
                    user_profile=user.getString("profile");
                    user_thumbnail=user.getString("thumbnail");

                    ass_correctanswer=measure.getString("correct");
                    ass_incorrectanswer=measure.getString("incorrect");





                }


            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }


            return null;
        }




        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);

            ass_firstnemre =(TextView)findViewById(R.id.ass_firstname);
            ass_firstnemre.setText(user_firstname+" "+user_lastName);
            ass_profile=(TextView)findViewById(R.id.ass_profile);
            ass_profile.setText(user_profile);
            ass_name=(TextView)findViewById(R.id.ass_name);
            ass_name.setText(ass_username);
            ass_correct=(TextView) findViewById(R.id.ass_correct);
            ass_correct.setText(ass_correctanswer);
            ass_incorrect=(TextView) findViewById(R.id.ass_incorrect);
            ass_incorrect.setText(ass_incorrectanswer);

             profile_img=(ImageView) findViewById(R.id.ass_thumbnail) ;

            Picasso.with(AssignmentViewAnalyticsActivity.this).load(user_thumbnail).into(profile_img);
        profile_img.setTag(user_thumbnail);

         //   new DownloadImageTask().execute(profile_img);

            float crr_question = Float.parseFloat(ass_correctanswer);

            Log.e("correct","correct"+crr_question);


            float total_question = crr_question+ Float.parseFloat(ass_incorrectanswer);


            Log.e("total","total"+total_question);

            float per = crr_question/total_question;

            final float percent = per*100;



            Log.e("percent","percent"+percent);



            progressStatus = 0;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    while(progressStatus < percent){
                        progressStatus +=1;

                        try{
                            Thread.sleep(20);
                        }catch(InterruptedException e){
                            e.printStackTrace();
                        }


                        pb_drawable.setProgress(progressStatus);

                    }
                }
            }).start();


        }





    }


    @Override public void onBackPressed() {
        super.onBackPressed();
this.finish();
/*
        Intent i = new Intent(AssignmentViewAnalyticsActivity.this, AppLandingPageActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
*/

    }

}