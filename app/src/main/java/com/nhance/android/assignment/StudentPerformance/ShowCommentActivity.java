package com.nhance.android.assignment.StudentPerformance;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nhance.android.R;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.utils.VedantuWebUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class ShowCommentActivity extends AppCompatActivity {

    private String assignmentName;
    private SessionManager session;
    private String parent_id,qid;
    private JSONObject result;
    private JSONArray list;
    private QuestionInfo questionInfo;
    private JSONObject user;
    private List<QuestionInfo>commentslist;
    private ShowCommentsAdapter commentsdapter;
    private RecyclerView info_listview;

    private SwipeRefreshLayout refreshLayout;
    private EditText post_comment;
    private ImageView send_btn;
    public static final int SELECT_FILE = 101;
    public static final int REQUEST_CAMERA = 100;

    protected static final int    GALLERY_REQUEST_CODE = 101;
    protected static final int    CAMERA_REQUEST_CODE  = 102;




    protected File cameraFileName;
    private boolean isWebViewLoaded;
    private String sendtext;
    boolean emptyEditText = false;
    private TextView emptymsg_comment_solution;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_show_comment);
        assignmentName = getIntent().getStringExtra(ConstantGlobal.ASSIGNMENT_NAME);
        parent_id=getIntent().getStringExtra(ConstantGlobal.ID);
        qid=getIntent().getStringExtra(ConstantGlobal.QID);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
       // getSupportActionBar().setIcon(R.drawable.ic_launcher);
        getSupportActionBar().setDisplayShowCustomEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(assignmentName);
        session = SessionManager.getInstance(this);
        commentslist=new ArrayList<>();

        info_listview=(RecyclerView)findViewById(R.id.comments_list);
        refreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipelayout);

        commentsdapter = new ShowCommentsAdapter(this,commentslist);

        info_listview.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        info_listview.setLayoutManager(mLayoutManager);
        //info_listview.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        info_listview.setItemAnimator(new DefaultItemAnimator());
        info_listview.setAdapter(commentsdapter);

        if(NetUtils.isOnline(ShowCommentActivity.this)) {
            new getComments().execute();
        }else{
            Toast.makeText(ShowCommentActivity.this, R.string.no_internet_msg, Toast.LENGTH_SHORT).show();
        }


        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if(NetUtils.isOnline(ShowCommentActivity.this)) {
                    new getComments().execute();
                }else{
                    Toast.makeText(ShowCommentActivity.this, R.string.no_internet_msg, Toast.LENGTH_SHORT).show();
                }

            }
        });

        post_comment=(EditText)findViewById(R.id.post_assignment_comment);

        send_btn=(ImageView)findViewById(R.id.comment_send_btn);

        emptymsg_comment_solution=(TextView)findViewById(R.id.emptymsg_comment_solution);





        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendtext=post_comment.getText().toString().trim();



                if(NetUtils.isOnline(ShowCommentActivity.this)) {
                    if (!post_comment.getText().toString().trim().isEmpty()) {

                        emptyEditText = true;
                       new addSolution(session).execute();
                        clearfield();



                    }else{
                        emptyEditText = false;
                        Toast.makeText(ShowCommentActivity.this,"Please enter the comment",Toast.LENGTH_LONG).show();

                    }
                }else{
                    Toast.makeText(ShowCommentActivity.this, R.string.no_internet_msg, Toast.LENGTH_SHORT).show();
                }

            }
        });



    }


    class  getComments extends AsyncTask<JSONObject, JSONObject, JSONObject> {
        private String url;
        private ProgressDialog loading;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading = new ProgressDialog(ShowCommentActivity.this);
            loading.setMessage("Please wait..");
            loading.show();
        }

        @Override
        protected JSONObject doInBackground(JSONObject... params) {


            JSONObject jsonRes = null;
            this.url = session.getApiUrl("getComments");

            Map<String, Object> httpParams = new HashMap<String, Object>();
            httpParams.put("parent.type", "SOLUTION");
            httpParams.put("parent.id",parent_id);



            session.addSessionParams(httpParams);


            try {
                jsonRes = VedantuWebUtils.getJSONData(url, VedantuWebUtils.GET_REQ, httpParams);

                Log.e("comments","..."+jsonRes);
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
                    result = jsonObject.getJSONObject("result");

                    if (result!=null) {

                        commentslist.clear();
                        Log.e("result", "......" + result);

                        list = result.getJSONArray("list");


                        for (int i = 0; i < list.length(); i++) {
                            JSONObject list_object = list.getJSONObject(i);
                            questionInfo = new QuestionInfo();
                            questionInfo.lastUpdated = list_object.getString("lastUpdated");
                            questionInfo.content = list_object.getString("content");

                            user = list_object.getJSONObject("user");

                            questionInfo.firstName = user.getString("firstName");
                            questionInfo.thumbnail = user.getString("thumbnail");
                            questionInfo.lastName = user.getString("lastName");

                            Log.e("fname", "...." + questionInfo.firstName);
                            Log.e("fname", "...." + questionInfo.content);
                            Log.e("fname", "...." + questionInfo.thumbnail);

                            commentslist.add(questionInfo);


                        }
                        commentsdapter.notifyDataSetChanged();
                        refreshLayout.setRefreshing(false);
                        if(commentslist != null && commentslist.size()>0){
                            emptymsg_comment_solution.setVisibility(View.GONE);
                        }else{
                            emptymsg_comment_solution.setVisibility(View.VISIBLE);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
                loading.dismiss();


        }


    }




    private void clearfield(){

        post_comment.getText().clear();
    }



    class  addSolution extends AsyncTask<JSONObject, JSONObject, JSONObject> {
        private ProgressDialog loading;

        private String url;


        public addSolution(SessionManager session) {
            loading = new ProgressDialog(ShowCommentActivity.this);
            loading.setMessage("Please wait..");
            loading.show();

        }




        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            loading=new ProgressDialog(ShowCommentActivity.this);
//            loading.setMessage("Please wait...");
//            loading.show();
        }

        @Override
        protected JSONObject doInBackground(JSONObject... params) {


            JSONObject jsonRes = null;
            this.url = session.getApiUrl("addComment");

            Map<String, Object> httpParams = new HashMap<String, Object>();
            httpParams.put("parent.id", parent_id);
            httpParams.put("parent.type","SOLUTION");
            httpParams.put("root.id",qid);
            httpParams.put("root.type","QUESTION");
            httpParams.put("base.id",qid);
            httpParams.put("base.type","QUESTION");
            httpParams.put("content",sendtext);
            session.addSessionParams(httpParams);


            try {
                jsonRes = VedantuWebUtils.getJSONData(url, VedantuWebUtils.POST_REQ, httpParams);
                Log.e("addcomments","...."+jsonRes);
            } catch (IOException e) {
                e.printStackTrace();
            }


            return jsonRes;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);

            loading.dismiss();

            if (jsonObject!=null) {

                try {
                    result = jsonObject.getJSONObject("result");
                    questionInfo = new QuestionInfo();
                    questionInfo.lastUpdated = result.getString("lastUpdated");
                    questionInfo.content = result.getString("content");
                    user = result.getJSONObject("user");
                    questionInfo.firstName = user.getString("firstName");
                    questionInfo.thumbnail = user.getString("thumbnail");
                    questionInfo.lastName = user.getString("lastName");
                    commentslist.add(questionInfo);

                    commentsdapter.notifyDataSetChanged();
                    if(commentslist != null && commentslist.size()>0){
                        emptymsg_comment_solution.setVisibility(View.GONE);
                    }else{
                        emptymsg_comment_solution.setVisibility(View.VISIBLE);
                    }

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

    private class ShowCommentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        private Activity activity;
        private List<QuestionInfo>infoList;
        public ShowCommentsAdapter(Activity activity, List<QuestionInfo> infoList) {

            this.activity=activity;
            this.infoList=infoList;

        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.show_comments_solutions, parent, false);
            return new MyHolder(itemView);



        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            MyHolder itemHolder = (MyHolder) holder;
            final QuestionInfo questionInfo=infoList.get(position);
            Log.e("questionInfo","...."+infoList.toString());
            itemHolder.solution_id.setText(questionInfo.id);
            itemHolder.firstName.setText(questionInfo.firstName+" "+questionInfo.lastName);
            itemHolder.content.loadDataWithBaseURL(null, "<style>img{display: inline;height: auto;max-width: 100%;}</style>" + questionInfo.content, "text/html", "UTF-8", null);

           // itemHolder.content.loadDataWithBaseURL(null, questionInfo.content, "text/html", "UTF-8", null);
            Picasso.with(activity).load(questionInfo.thumbnail).into(itemHolder.user_image);
            String dateTime = convertDate(questionInfo.lastUpdated,"dd/MM/yyyy hh:mm:ss");
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            Date date = null;
            String niceDateStr = (String) DateUtils.getRelativeTimeSpanString(getGMTDate(Long.parseLong(questionInfo.lastUpdated)).getTime() , Calendar.getInstance().getTimeInMillis(), DateUtils.MINUTE_IN_MILLIS);
            itemHolder.time.setText(getlongtoago(Long.parseLong(questionInfo.lastUpdated)));




        }

        @Override
        public int getItemCount() {
            return infoList.size();
        }

        private class MyHolder extends RecyclerView.ViewHolder {

            public TextView lastName;
            public TextView firstName;
            public  TextView time,solution_id;
            public ImageView user_image;
            public WebView content;
            public MyHolder(View itemView) {
                super(itemView);


                firstName= (TextView)itemView.findViewById(R.id.comments_sol_firstname);
                lastName= (TextView)itemView.findViewById(R.id.comments_sol_lastName);
                time= (TextView)itemView.findViewById(R.id.comments_sol_time);
                user_image=(ImageView)itemView.findViewById(R.id.comments_sol_profile);
                content = (WebView) itemView.findViewById(R.id.comments_content_img);
                solution_id=(TextView)itemView.findViewById(R.id.comments_sol_id);



            }
        }


        private Date getGMTDate(long date) {
            SimpleDateFormat dateFormatGmt = new SimpleDateFormat(
                    "yyyy-MMM-dd HH:mm:ss");
            dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
            SimpleDateFormat dateFormatLocal = new SimpleDateFormat(
                    "yyyy-MMM-dd HH:mm:ss");

            Date temp = new Date(date);

            try {
                return dateFormatLocal.parse(dateFormatGmt.format(temp));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return temp;
        }

        public  String convertDate(String dateInMilliseconds,String dateFormat) {
            return DateFormat.format(dateFormat, Long.parseLong(dateInMilliseconds)).toString();
        }


        public String getlongtoago(long createdAt) {
            SimpleDateFormat userDateFormat = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
            SimpleDateFormat dateFormatNeeded = new SimpleDateFormat("MM/dd/yyyy HH:MM:SS");

            Date date = null;
            date = new Date(createdAt);
            String crdate1 = dateFormatNeeded.format(date);

            // Date Calculation
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            crdate1 = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(date);

            // get current date time with Calendar()
            Calendar cal = Calendar.getInstance();
            String currenttime = dateFormat.format(cal.getTime());

            Date CreatedAt = null;
            Date current = null;
            try {
                CreatedAt = dateFormat.parse(crdate1);
                current = dateFormat.parse(currenttime);
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // Get msec from each, and subtract.
            long diff = current.getTime() - CreatedAt.getTime();
            long diffSeconds = diff / 1000;
            long diffMinutes = diff / (60 * 1000) % 60;
            long diffHours = diff / (60 * 60 * 1000) % 24;
            long diffDays = diff / (24 * 60 * 60 * 1000);
            int weeks = (int) (diff/ (1000*60*60*24*7));
            double month = diffDays /30;
            double year = diffDays / 365;

            Log.e("weeks.."+weeks+"months..."+month, "years.."+year);

            String time = null;
            if(year > 0){
                if (year == 1) {
                    time = (int)year + " year ago ";
                } else {
                    time = (int)year + " years ago ";
                }
            } else if(month >0){
                if (month == 1) {
                    time = (int)month + " month ago ";
                } else {
                    time = (int)month + " months ago ";
                }
            }else{
                if (diffDays > 0) {
                    if (diffDays == 1) {
                        time = diffDays + " day ago ";
                    } else {
                        time = diffDays + " days ago ";
                    }
                } else {
                    if (diffHours > 0) {
                        if (diffHours == 1) {
                            time = diffHours + " hr ago";
                        } else {
                            time = diffHours + " hrs ago";
                        }
                    } else {
                        if (diffMinutes > 0) {
                            if (diffMinutes == 1) {
                                time = diffMinutes + " min ago";
                            } else {
                                time = diffMinutes + " mins ago";
                            }
                        } else {
                            if (diffSeconds > 0) {
                                time = diffSeconds + " secs ago";
                            }
                        }

                    }

                }
            }
            return time;
        }
    }
}
