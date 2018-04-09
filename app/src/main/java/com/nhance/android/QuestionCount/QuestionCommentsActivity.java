package com.nhance.android.QuestionCount;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nhance.android.R;
import com.nhance.android.assignment.StudentPerformance.NetUtils;
import com.nhance.android.assignment.StudentPerformance.QuestionInfo;
import com.nhance.android.assignment.StudentPerformance.ShowCommentActivity;
import com.nhance.android.async.tasks.ITaskProcessor;
import com.nhance.android.async.tasks.ImageUploaderTask;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.message.RealPathUtil;
import com.nhance.android.utils.JSONUtils;
import com.nhance.android.utils.VedantuWebUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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

public class QuestionCommentsActivity extends AppCompatActivity {

    private SessionManager session;
    private String parentId,qid;
    private RecyclerView info_listview;
    private ProgressDialog loading;
    private JSONObject result;
    private JSONArray list;
    private QuestionInfo questionInfo;
    private JSONObject user;
    private List<QuestionInfo> questioncommentslist;
    private QuestionCommentsAdapter commentsAdapter;
    private SwipeRefreshLayout refreshLayout;

//    private EditText post_comment;
//    private Button choose_img_btn,send_btn,delete_btn;


//    protected static final int    GALLERY_REQUEST_CODE = 101;
//    protected static final int    CAMERA_REQUEST_CODE  = 102;
//    private ImageView comment_img;
//    private LinearLayout delete_btn_layout;
//    private String imagePath;
//    private boolean imageAttached;
//    private String uuid, imageUrl,imgUrl;
//    protected File cameraFileName;
//    private Uri imageUri;
//    private boolean isWebViewLoaded;
//    private String sendtext;
//    boolean emptyEditText = false;
    private TextView emptymsg_comment_solution;
    private FloatingActionButton fabButton;
    private EditText userInput;
    private int count;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_question_comments);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setIcon(R.drawable.ic_launcher);
        getSupportActionBar().setDisplayShowCustomEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Question");
        session = SessionManager.getInstance(this);

        parentId=getIntent().getStringExtra(ConstantGlobal.ID);
        qid=getIntent().getStringExtra(ConstantGlobal.QID);
        position = getIntent().getIntExtra("position", 0);

        questioncommentslist=new ArrayList<>();
        info_listview=(RecyclerView)findViewById(R.id.comments_list);
        refreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipelayout);
        commentsAdapter = new QuestionCommentsAdapter(this,questioncommentslist);
        info_listview.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        info_listview.setLayoutManager(mLayoutManager);
        info_listview.setItemAnimator(new DefaultItemAnimator());
        info_listview.setAdapter(commentsAdapter);
        emptymsg_comment_solution=(TextView)findViewById(R.id.emptymsg_comment_solution);
        fabButton = (FloatingActionButton)findViewById(R.id.floatingActionButton);



        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // get prompts.xml view
                LayoutInflater li = LayoutInflater.from(QuestionCommentsActivity.this);
                View promptsView = li.inflate(R.layout.custom_dialog, null);

                android.support.v7.app.AlertDialog.Builder alertDialogBuilder = new android.support.v7.app.AlertDialog.Builder(
                        QuestionCommentsActivity.this);
                userInput = (EditText) promptsView
                        .findViewById(R.id.message);
                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(promptsView);


                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("Send",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        String commentText = userInput.getText().toString();
                                        Log.e("commentText", ".."+commentText);
                                        // get user input and set it to result
                                        // edit text
//                                        result.setText(userInput.getText());

                                        if(commentText != null && !commentText.trim().isEmpty()){
                                            new AddCommentAsyncTask(session, commentText.trim()).execute();
                                            userInput.setText("");
                                        }else{
                                            Toast.makeText(QuestionCommentsActivity.this, "Please enter comments..", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        dialog.cancel();
                                    }
                                });

                // create alert dialog
                android.support.v7.app.AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();

            }
        });



        if(NetUtils.isOnline(QuestionCommentsActivity.this)) {
            new getComments().execute();
        }else{
            Toast.makeText(QuestionCommentsActivity.this, R.string.no_internet_msg, Toast.LENGTH_SHORT).show();
        }

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(NetUtils.isOnline(QuestionCommentsActivity.this)) {
                    new getComments().execute();
                }else{
                    Toast.makeText(QuestionCommentsActivity.this, R.string.no_internet_msg, Toast.LENGTH_SHORT).show();
                }


            }
        });

//        post_comment=(EditText)findViewById(R.id.question_post_assignment_comment);
//        choose_img_btn=(Button)findViewById(R.id.question_select_image);
//        send_btn=(Button)findViewById(R.id.question_comment_send_btn);
//        delete_btn=(Button)findViewById(R.id.question_comment_delete_image);
//        comment_img=(ImageView)findViewById(R.id.question_comment_img);
//        delete_btn_layout=(LinearLayout)findViewById(R.id.delete_btn_layout);
//        emptymsg_comment_solution=(TextView)findViewById(R.id.emptymsg_comment_solution);
//
//        choose_img_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                showSelectedPictureDialogue();
//
//
//            }
//        });

//
//        delete_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                choose_img_btn.setVisibility(View.VISIBLE);
//                delete_btn_layout.setVisibility(View.GONE);
//            }
//        });
//
//
//        send_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                sendtext=post_comment.getText().toString().trim();
//
//
//
//                if(NetUtils.isOnline(QuestionCommentsActivity.this)) {
//                    if (!post_comment.getText().toString().trim().isEmpty()) {
//
//                        emptyEditText = true;
//                        new addSolution(session,imageAttached, uuid, imageUrl).execute();
//                        clearfield();
//                        delete_btn.setVisibility(View.GONE);
//                        delete_btn_layout.setVisibility(View.GONE);
//
//
//
//                    }else{
//                        emptyEditText = false;
//                        Toast.makeText(QuestionCommentsActivity.this,"Please enter the comments",Toast.LENGTH_LONG).show();
//
//                    }
//                }else{
//                    Toast.makeText(QuestionCommentsActivity.this, R.string.no_internet_msg, Toast.LENGTH_SHORT).show();
//                }
//
//            }
//        });
//





    }



    class  getComments extends AsyncTask<JSONObject, JSONObject, JSONObject> {
        private String url;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading = new ProgressDialog(QuestionCommentsActivity.this);
            loading.setMessage("Please wait..");
            loading.show();
        }

        @Override
        protected JSONObject doInBackground(JSONObject... params) {


            JSONObject jsonRes = null;
            this.url = session.getApiUrl("getComments");

            Map<String, Object> httpParams = new HashMap<String, Object>();
            httpParams.put("parent.type", "SOLUTION");
            httpParams.put("parent.id",parentId);
            httpParams.put("callingApp", "TapApp");
            httpParams.put("orderBy", "timeCreated");
            httpParams.put("callingAppId", "TapApp");

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
            loading.dismiss();
            if (jsonObject!=null) {

                try {
                    result = jsonObject.getJSONObject("result");

                    if (result!=null) {

                     questioncommentslist.clear();
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

                            questioncommentslist.add(questionInfo);

                        }
                     commentsAdapter.notifyDataSetChanged();
                    refreshLayout.setRefreshing(false);
                        if(questioncommentslist != null && questioncommentslist.size()>0){
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

    private class QuestionCommentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        private Activity activity;
        private List<QuestionInfo>infoList;
        public QuestionCommentsAdapter(Activity activity, List<QuestionInfo> infoList) {

            this.activity=activity;
            this.infoList=infoList;

        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.question_comments_solutions, parent, false);
            return new MyHolder(itemView);



        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            MyHolder itemHolder = (MyHolder) holder;
            final QuestionInfo questionInfo=infoList.get(position);
            Log.e("questionInfo","...."+infoList.toString());
            itemHolder.solution_id.setText(questionInfo.id);
            itemHolder.firstName.setText(questionInfo.firstName);

//            itemHolder.content.loadDataWithBaseURL(null, questionInfo.content, "text/html", "UTF-8", null);
            itemHolder.content.getSettings().setJavaScriptEnabled(true);
//            itemHolder.content.getSettings().setBuiltInZoomControls(true);
            itemHolder.content.loadDataWithBaseURL("http://bar",
                    "<script type=\"text/x-mathjax-config\">" +
                            "  MathJax.Hub.Config({" +
                            "extensions: [\"tex2jax.js\"],messageStyle:\"none\"," +
                            "jax: [\"input/TeX\",\"output/HTML-CSS\"]," +
                            "tex2jax: {inlineMath: [['$','$'],['\\\\(','\\\\)']]}" +
                            "});" +
                            "</script>" +
                            "<script type=\"text/javascript\" async src=\"file:///android_asset/js/MathJax/MathJax.js?config=TeX-AMS-MML_HTMLorMML\"></script>" +
                            "" +
                            "</head>" +
                            "" +
                            "<body>" +
                            questionInfo.content +
                            "</body>" +
                            "</html>", "text/html", "utf-8", "");

            Picasso.with(activity).load(questionInfo.thumbnail).into(itemHolder.user_image);
//            String dateTime = convertDate(questionInfo.lastUpdated,"dd/MM/yyyy hh:mm:ss");
//            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
//            Date date = null;
//            String niceDateStr = (String) DateUtils.getRelativeTimeSpanString(getGMTDate(Long.parseLong(questionInfo.lastUpdated)).getTime() , Calendar.getInstance().getTimeInMillis(), DateUtils.MINUTE_IN_MILLIS);
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


                firstName= (TextView)itemView.findViewById(R.id.question_comments_sol_firstname);
                lastName= (TextView)itemView.findViewById(R.id.question_comments_sol_lastName);
                time= (TextView)itemView.findViewById(R.id.question_comments_sol_time);
                user_image=(ImageView)itemView.findViewById(R.id.question_comments_sol_profile);
                content = (WebView) itemView.findViewById(R.id.question_comments_content_img);
                solution_id=(TextView)itemView.findViewById(R.id.question_comments_sol_id);



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


    public class  AddCommentAsyncTask extends AsyncTask<JSONObject, JSONObject, JSONObject> {


        private String url;
        private String commentsText;
        private JSONObject result;


        public AddCommentAsyncTask(SessionManager session, String commentsText) {

            this.commentsText = commentsText;


        }




        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading = new ProgressDialog(QuestionCommentsActivity.this);
            loading.setMessage("Please wait..");
            loading.show();
        }

        @Override
        protected JSONObject doInBackground(JSONObject... params) {


            JSONObject jsonRes = null;
            this.url = session.getApiUrl("addComment");

            Map<String, Object> httpParams = new HashMap<String, Object>();
            httpParams.put("parent.id", parentId);
            httpParams.put("parent.type","SOLUTION");

            httpParams.put("root.id",qid);
            httpParams.put("root.type","QUESTION");
            httpParams.put("base.id",qid);
            httpParams.put("base.type","QUESTION");

            httpParams.put("content",commentsText);
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
            try {
                 result = jsonObject.getJSONObject("result");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if(result != null) {

                questionInfo = new QuestionInfo();
                try {
                questionInfo.lastUpdated = result.getString("lastUpdated");
                questionInfo.content = result.getString("content");


                    user = result.getJSONObject("user");


                questionInfo.firstName = user.getString("firstName");
                questionInfo.thumbnail = user.getString("thumbnail");



                questioncommentslist.add(questionInfo);
                    commentsAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                count += 1;

            }

        }


    }

    @Override
    public void onBackPressed() {
        finish();

    }

    @Override
    public void finish() {
        Intent intent = new Intent();
        intent.putExtra("count", count);
        intent.putExtra("position", position);
        setResult(RESULT_OK, intent);
        super.finish();
    }
}
