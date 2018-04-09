package com.nhance.android.ChallengeArena;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.nhance.android.ChallengeArena.model.ChallengeUserExamFragment;
import com.nhance.android.ChallengeArena.model.HintInfo;
import com.nhance.android.R;
import com.nhance.android.activities.AppLandingPageActivity;
import com.nhance.android.assignment.StudentPerformance.AsssignmentQuestion;
import com.nhance.android.assignment.StudentPerformance.NetUtils;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.customviews.NonScrollableGridView;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.utils.GoogleAnalyticsUtils;
import com.nhance.android.utils.VedantuWebUtils;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class ChallengeExamActivity extends AppCompatActivity implements ChallengeUserExamFragment.NotifyTakeTestFragment, IMethodCaller {

    private SessionManager session;
    private String challenge_id;
    private JSONArray options;
    private String hint;
    private String optionstring = null;
    private AsssignmentQuestion question;
    private ChallengeUserExamFragment challengeUserExamFragment;
    private Bundle bundle;
    private long startTime;
    private int minutes;
    private TextView exame_time, total_points, end_challenge;
    private String ch_content;
    private String ch_type;
    private long start_timer_count;
    private String token;
    private TextView submit, cancel;
    private String tempAnswerForCurrentQuestion;
    private String info_name;
    int info_points;
    private int info_duration;
    private TextView ch_title;
    private long ch_time;

    private ProgressDialog loading;
    private JSONArray hintsDeductionValues;
    List<JSONArray> hintcount;


    private HintViewAdpter hintViewAdpter;
    private NonScrollableGridView scrollableGridView;
    private int hint_reduce;
    private int total_hint_point;
    private String expiry_date;

    private long challengetime;
    private String hms;
    private String hms_lifetime_remain;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GoogleAnalyticsUtils.setScreenName(ConstantGlobal.GA_SCREEN_TAKE_ASSIGNMENT);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        setContentView(R.layout.activity_challenge_exam);
        session = SessionManager.getInstance(this);
        challenge_id = getIntent().getStringExtra(ConstantGlobal.ID);
        new ac_takechallenge().execute();
        setUpAssignmentQuestion();

        Intent intent = getIntent();
        long challengetime = intent.getIntExtra("current.challenge_count", 0);
        Log.e("deeeeeeev", "...." + challengetime);


        exame_time = (TextView) findViewById(R.id.start_timer);
        ch_title = (TextView) findViewById(R.id.ch_title);

        total_points = (TextView) findViewById(R.id.total_points);

        end_challenge = (TextView) findViewById(R.id.end_challenge);
        end_challenge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endChallenge();
            }
        });


        submit = (TextView) findViewById(R.id.challenge_submit);
        cancel = (TextView) findViewById(R.id.challenge_cancel);


        new getChallengeInfo().execute();


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (NetUtils.isOnline(ChallengeExamActivity.this)) {
                    if (StringUtils.isEmpty(tempAnswerForCurrentQuestion)) {
                        Toast.makeText(ChallengeExamActivity.this, "Please answer the question first", Toast.LENGTH_SHORT).show();

                    } else {
                        new attemptChallenge().execute();
                        alertOneButton();
                    }
                } else {
                    Toast.makeText(ChallengeExamActivity.this, R.string.no_internet_msg, Toast.LENGTH_SHORT).show();
                }


            }


        });


        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                challengeUserExamFragment.resetAnswer();
            }
        });
        hintcount = new ArrayList<>();

        scrollableGridView = (NonScrollableGridView) findViewById(R.id.hints);


    }

    private void endChallenge() {

        AlertDialog.Builder alert = new AlertDialog.Builder(ChallengeExamActivity.this);

        alert.setTitle("End Challenge");
        alert.setMessage("Are you sure to exit this challenge");
        alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

//                Intent intent = new Intent(ChallengeExamActivity.this, AppLandingPageActivity.class);
//                startActivity(intent);

                dialog.cancel();
                finish();
            }


        });

        alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        alert.show();


    }

    public void alertOneButton() {


        new AlertDialog.Builder(ChallengeExamActivity.this)
                .setTitle("Message")
                .setMessage("You will be able to view the answer in " + expiry_date + "hrs")
                // .setMessage("Are you submit the exam"+ch_time+"")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel();
                        finish();

                    }
                }).show();
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


//challengeInfo

    public class getChallengeInfo extends AsyncTask<JSONObject, JSONObject, JSONObject> {


        @Override
        protected JSONObject doInBackground(JSONObject... jsonObjects) {
            String url = session.getApiUrl("getChallengeInfo");


            JSONObject jsonRes = null;

            HashMap<String, Object> httpParams = new HashMap<String, Object>();

            httpParams.put("callingApp", "TabApp");
            httpParams.put("callingAppId", "TabApp");
            httpParams.put("callingUserId", session.getOrgMemberInfo().userId);
            httpParams.put("userRole", session.getOrgMemberInfo().profile);
            httpParams.put("userId", session.getOrgMemberInfo().userId);
            httpParams.put("myUserId", session.getOrgMemberInfo().userId);
            httpParams.put("orgId", session.getOrgMemberInfo().orgId);
            httpParams.put("memberId", session.getOrgMemberInfo().userId);
            httpParams.put("id", challenge_id);


            session.addSessionParams(httpParams);

            try {
                jsonRes = VedantuWebUtils.getJSONData(url, VedantuWebUtils.GET_REQ, httpParams);
                Log.e("challengeInfo", "......." + jsonRes);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return jsonRes;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            if (jsonObject != null) {

                try {
                    JSONObject info_result = jsonObject.getJSONObject("result");


                    info_name = info_result.getString("name");
                    info_points = info_result.getInt("maxBid");
                    info_duration = info_result.getInt("duration");
                    Log.e("info_duration", "..." + info_duration);

                    total_points.setText(info_points + "");
                    total_hint_point = info_points;
                    Log.e("info_points", "..." + info_points);

                    new CountDownTimer(info_duration * 1000, 1000) {
                        public void onTick(long millisUntilFinished) {
                            long millis = millisUntilFinished;
                            hms = String.format("%02d:%02d:%02d",
                                    TimeUnit.MILLISECONDS.toHours(millis),
                                    TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                                    TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
                            Log.i("hms", "...." + hms);
                            // exame_time.setText(hms);//set text
                            lifetimediff();
                        }

                        public void onFinish() {

                            new AlertDialog.Builder(ChallengeExamActivity.this)
                                    .setTitle("Error")
                                    .setMessage("Sorry, This alloted for the challenge has expired.")
                                    // .setMessage("Are you submit the exam"+ch_time+"")
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                            finish();
                                            // dialog.cancel();
                                        }
                                    }).show();
                        }
                    }.start();


                    //submit button alert time

                    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                    long currenttime = cal.getTimeInMillis();
                    long timercount = info_result.getLong("endTime") - (currenttime);
                    Log.e("endTime", "......." + info_result.getLong("endTime"));
                    Log.e("currenttime", "......." + currenttime);
                    ch_time = timercount;

                    minutes = (int) (ch_time);
                    new CountDownTimer(ch_time, 1000) {
                        public void onTick(long millisUntilFinished) {
                            long millis = millisUntilFinished;


                            expiry_date = String.format("%02d:%02d:%02d",
                                    TimeUnit.MILLISECONDS.toHours(millis),
                                    TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                                    TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
                            Log.i("hms", "...." + expiry_date);

                            //  exame_time.setText(hms);//set text

                        }

                        public void onFinish() {

                        }
                    }.start();


                    //////////////challenge time>lifetime

                    Calendar cal1 = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                    long currenttime1 = cal1.getTimeInMillis();
                    long timercount1 = info_result.getLong("endTime") - (currenttime1);
                    Log.e("endTime", "......." + info_result.getLong("endTime"));
                    Log.e("currenttime", "......." + currenttime1);
                    challengetime = timercount1;

                    minutes = (int) (challengetime);
                    new CountDownTimer(challengetime, 1000) {

                        public void onTick(long millisUntilFinished) {
                            long millis = millisUntilFinished;


                            hms_lifetime_remain = String.format("%02d:%02d:%02d",
                                    TimeUnit.MILLISECONDS.toHours(millis),
                                    TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                                    TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));

                            // exame_time.setText(hms_lifetime_remain);//set text
                            lifetimediff();
                            Log.i("hmslifetime", "...." + hms_lifetime_remain);

                        }

                        public void onFinish() {

                          //  challengetimeexpired();



                        }
                    }.start();


                    ch_title.setText(info_name);

                    hintsDeductionValues = info_result.getJSONArray("hintsDeductionValues");


                    Log.e("hintsDeductionValues", "...." + hintsDeductionValues);

                    Log.e("hintcount", "....." + hintcount.toString());


                    //  exame_time.setText(info_duration);

                    hintViewAdpter = new HintViewAdpter(ChallengeExamActivity.this, hintsDeductionValues, token);
                    scrollableGridView.setAdapter(hintViewAdpter);
                    hintViewAdpter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }


    public String convertDate(String dateInMilliseconds, String dateFormat) {
        return android.text.format.DateFormat.format(dateFormat, Long.parseLong(dateInMilliseconds)).toString();
    }

    @Override
    public void yourDesiredMethod(int count_value) {

        hint_reduce = count_value;

        Log.e("hint_reduce", "...." + hint_reduce);


        total_hint_point = total_hint_point - hint_reduce;


        Log.e("hint_pint", "..." + total_hint_point + "");


        total_points.setText(total_hint_point + "");


    }

    public class ac_takechallenge extends AsyncTask<JSONObject, JSONObject, JSONObject> {


        @Override
        protected JSONObject doInBackground(JSONObject... jsonObjects) {
            String url = session.getApiUrl("getChallengeDetails");


            JSONObject jsonRes = null;

            HashMap<String, Object> httpParams = new HashMap<String, Object>();

            httpParams.put("callingApp", "TabApp");
            httpParams.put("callingUserId", session.getOrgMemberInfo().userId);
            httpParams.put("userRole", session.getOrgMemberInfo().profile);
            httpParams.put("userId", session.getOrgMemberInfo().userId);
            httpParams.put("myUserId", session.getOrgMemberInfo().userId);
            httpParams.put("orgId", session.getOrgMemberInfo().orgId);
            httpParams.put("memberId", session.getOrgMemberInfo().userId);
            httpParams.put("id", challenge_id);


            session.addSessionParams(httpParams);

            try {
                jsonRes = VedantuWebUtils.getJSONData(url, VedantuWebUtils.GET_REQ, httpParams);
                Log.e("ac_takechallenge", "......." + jsonRes);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return jsonRes;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            if (jsonObject != null) {
                try {
                    JSONObject ch_resulr = jsonObject.getJSONObject("result");

                    token = ch_resulr.getString("token");

                    Log.e("result1111", "...." + token);
                    ///     startTime=ch_resulr.getLong("startTime");
                    JSONObject ch_entity = ch_resulr.optJSONObject("entity");
                    Log.e("result1", "...." + ch_entity);
                    ch_content = ch_entity.getString("content");
                    Log.e("result1", "...." + ch_content);
                    ch_type = ch_entity.getString("type");
                    Log.e("result1", "...." + ch_type);
                    JSONArray options = ch_entity.optJSONArray("options");


                    if (options != null) {
                        for (int z = 0; z < options.length(); z++) {
                            try {
                                if (z == 0)
                                    optionstring = options.getString(z);
                                else
                                    optionstring += "#" + options.getString(z);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }


                    setUpAssignmentQuestion();
                    // total_points.setText(ch_type);
                    Log.e("result", "...." + ch_resulr);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }


    private void setUpAssignmentQuestion() {


        challengeUserExamFragment = new ChallengeUserExamFragment();
        bundle = new Bundle();
        bundle.putString("content", ch_content);
        bundle.putString("options", optionstring);
        bundle.putString("type", ch_type);
        challengeUserExamFragment.setArguments(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.framlayout, challengeUserExamFragment).commit();

    }

    public AsssignmentQuestion getCurrentQuestionData() {


        return question;
    }

    @Override
    public void setAnswerForCurrentQuestion(String newAnswer) {


        tempAnswerForCurrentQuestion = newAnswer;

        Log.e("attemptChallengeuserans", "...." + tempAnswerForCurrentQuestion);

    }


    public class attemptChallenge extends AsyncTask<JSONObject, JSONObject, JSONObject> {

        private ProgressDialog loading;

        @Override
        protected JSONObject doInBackground(JSONObject... jsonObjects) {
            String url = session.getApiUrl("attemptChallenge");


            JSONObject jsonRes = null;

            HashMap<String, Object> httpParams = new HashMap<String, Object>();
            httpParams.put("callingApp", "TabApp");
            httpParams.put("callingUserId", session.getOrgMemberInfo().userId);
            httpParams.put("userRole", session.getOrgMemberInfo().profile);
            httpParams.put("userId", session.getOrgMemberInfo().userId);
            httpParams.put("myUserId", session.getOrgMemberInfo().userId);
            httpParams.put("orgId", session.getOrgMemberInfo().orgId);
            httpParams.put("memberId", session.getOrgMemberInfo().userId);
            httpParams.put("token", token);
            if (tempAnswerForCurrentQuestion != null) {
                String[] datavalue = tempAnswerForCurrentQuestion.split("#");
                for (int i = 0; i < datavalue.length; i++) {
                    System.out.println(datavalue[i]);
                    httpParams.put("answer[" + i + "]", datavalue[i]);
                }
            }

            session.addSessionParams(httpParams);
            try {
                jsonRes = VedantuWebUtils.getJSONData(url, VedantuWebUtils.POST_REQ, httpParams);
                Log.e("attemptChallenge", "......." + jsonRes);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return jsonRes;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
        }
    }


    private class HintViewAdpter extends ArrayAdapter {
        private List<JSONArray> hintcount;
        private LayoutInflater inflater = null;
        private Activity activity;
        private JSONArray countvalue = null;
        private JSONArray hintsDeductionValues;
        private String token;

        public HintViewAdpter(Activity activity, JSONArray hintsDeductionValues, String token) {
            super(activity, 0);
            this.activity = activity;
            this.token = token;

            this.hintsDeductionValues = hintsDeductionValues;
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        @Override
        public int getCount() {
            return hintsDeductionValues.length();
        }

        @Override
        public Object getItem(int position) {
            Object ss = null;
            try {
                ss = hintsDeductionValues.get(position);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.e("position", position + "");
            return ss;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View vi = convertView;
            if (vi == null)
                vi = inflater.inflate(R.layout.hint_grid_view, null);
            final TextView hint_position = (TextView) vi.findViewById(R.id.grid_hint_position);
            final TextView hint_value = (TextView) vi.findViewById(R.id.grid_hint_value);
            final int count_value = (int) getItem(position);

            int s = position + 1;
            Log.e("p", "..." + count_value);


            hint_position.setText("Hint " + s);
            hint_position.setBackgroundColor(getResources().getColor(R.color.slpdarkgreen));
            hint_position.setTextColor(getResources().getColor(R.color.white));
            hint_value.setText(count_value + "");
            hint_position.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new getHint().execute();
                    hint_position.setVisibility(View.VISIBLE);
                    hint_value.setVisibility(View.VISIBLE);
                    hint_position.setBackgroundColor(getResources().getColor(R.color.red));
                    hint_position.setPaintFlags(hint_position.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    hint_position.setOnClickListener(null);
                    if (activity instanceof IMethodCaller) {
                        ((IMethodCaller) activity).yourDesiredMethod(count_value);
                    }
                }
            });
            return vi;
        }

        public class getHint extends AsyncTask<JSONObject, JSONObject, JSONObject> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = new ProgressDialog(ChallengeExamActivity.this);
                loading.setMessage("Please wait...");
                loading.show();
            }

            @Override
            protected JSONObject doInBackground(JSONObject... jsonObjects) {
                String url = session.getApiUrl("getHint");

                JSONObject jsonRes = null;
                HashMap<String, Object> httpParams = new HashMap<String, Object>();
                httpParams.put("callingApp", "TabApp");
                httpParams.put("callingAppId", "TabApp");
                httpParams.put("callingUserId", session.getOrgMemberInfo().userId);
                httpParams.put("userRole", session.getOrgMemberInfo().profile);
                httpParams.put("userId", session.getOrgMemberInfo().userId);
                httpParams.put("myUserId", session.getOrgMemberInfo().userId);
                httpParams.put("orgId", session.getOrgMemberInfo().orgId);
                httpParams.put("memberId", session.getOrgMemberInfo().userId);
                httpParams.put("token", token);
                session.addSessionParams(httpParams);

                try {
                    jsonRes = VedantuWebUtils.getJSONData(url, VedantuWebUtils.GET_REQ, httpParams);
                    Log.e("challengehints", "......." + jsonRes);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return jsonRes;
            }

            @Override
            protected void onPostExecute(JSONObject jsonObject) {
                super.onPostExecute(jsonObject);
                loading.dismiss();

                try {
                    JSONObject ch_hint = jsonObject.getJSONObject("result");
                    hint = ch_hint.getString("hint");
                    Log.e("hint", "...." + hint);
                    hintdialouge();
                } catch (JSONException e) {

                    e.printStackTrace();
                }
            }
        }

        private void hintdialouge() {
            WebView webView = new WebView(activity);
            hint = "<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />" + hint;

            //  webView.loadDataWithBaseURL(null, "<style>img{display: inline;height: auto;max-width: 100%;}</style>" + hint, "text/html", "UTF-8", null);
            //webView.loadDataWithBaseURL("file:///android_asset/", hint, "text/html", "UTF-8", null);

            webView.getSettings().setJavaScriptEnabled(true);
            webView.loadDataWithBaseURL("http://bar", "<script type='text/x-mathjax-config'>"
                    +"MathJax.Hub.Config({ showMathMenu: false, "
                    +"jax: ['input/TeX','output/HTML-CSS'], "
                    +"extensions: ['tex2jax.js'], "
                    +"TeX: { extensions: ['AMSmath.js','AMSsymbols.js',"
                    +"'noErrors.js','noUndefined.js'] } "
                    +"});</script>"
                    +"<script type='text/javascript' "
                    +"src='file:///android_asset/js/MathJax/MathJax.js'"
                    +"></script><span id='math'></span>"+hint,"text/html","utf-8","");
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("Hint")
                    .setView(webView)
                    .setNeutralButton("OK", null)
                    .show();

        }
    }


    /*  public void challengetimeexpired() {
        new AlertDialog.Builder(ChallengeExamActivity.this)
                .setTitle("Error")
                .setMessage("Sorry, This alloted for the challenge has expired.")
                // .setMessage("Are you submit the exam"+ch_time+"")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        finish();
                        // dialog.cancel();
                    }
                }).show();
    }*/


    public void lifetimediff() {
        if (challengetime<info_duration) {
            exame_time.setText(hms_lifetime_remain);//set text
        } else {
            exame_time.setText(hms);//set text
        }
    }
}

