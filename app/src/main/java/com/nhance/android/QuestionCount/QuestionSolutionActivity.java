package com.nhance.android.QuestionCount;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
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
import org.jsoup.nodes.Document;
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

public class  QuestionSolutionActivity extends AppCompatActivity {

    private SessionManager session;
    private String question_id;
    private RecyclerView info_listview;
    private JSONObject solutionsresult;
    private JSONArray listArray;
    private  JSONObject userlist;
    private String totalHits;
    List<QuestionInfo> questionsolutionaddlist;
    private QuestionSolutionAdapter solutionAdapter;
    private TextView emptymsg_textview;
//    private SwipeRefreshLayout refreshLayout;
    private EditText post_comment;
    private ImageView choose_img_btn,delete_btn;
    private ImageView comment_img,send_btn;
    private LinearLayout delete_btn_layout;
    protected static final int    GALLERY_REQUEST_CODE = 101;
    protected static final int    CAMERA_REQUEST_CODE  = 102;
    private String sendtext;
    boolean emptyEditText = false;
    private boolean imageAttached;
    private String uuid, imageUrl,imgUrl;
    private Uri imageUri;
    private String imagePath;
    protected File cameraFileName;
    private QuestionInfo questionInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_question_solution);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
       // getSupportActionBar().setIcon(R.drawable.ic_launcher);
        getSupportActionBar().setDisplayShowCustomEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Question");
        session = SessionManager.getInstance(this);
        question_id=getIntent().getStringExtra(ConstantGlobal.ID);

        questionsolutionaddlist=new ArrayList<>();

        info_listview=(RecyclerView)findViewById(R.id.question_solution_list);
//        refreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipelayout);
       solutionAdapter = new QuestionSolutionAdapter(this,questionsolutionaddlist);
        info_listview.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        info_listview.setLayoutManager(mLayoutManager);
       //info_listview.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        info_listview.setItemAnimator(new DefaultItemAnimator());
        info_listview.setAdapter(solutionAdapter);
        emptymsg_textview=(TextView)findViewById(R.id.emptymsg_solution);




        if(NetUtils.isOnline(QuestionSolutionActivity.this)) {
            new getSolutions().execute();
        }else{
            Toast.makeText(QuestionSolutionActivity.this, R.string.no_internet_msg, Toast.LENGTH_SHORT).show();
        }


//        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                if(NetUtils.isOnline(QuestionSolutionActivity.this)) {
//                    new getSolutions().execute();
//                }else{
//                    Toast.makeText(QuestionSolutionActivity.this, R.string.no_internet_msg, Toast.LENGTH_SHORT).show();
//                }
//
//            }
//        });
        post_comment=(EditText)findViewById(R.id.question_post_question_comment);
        choose_img_btn=(ImageView)findViewById(R.id.question_select_image);
        send_btn=(ImageView)findViewById(R.id.question_comment_send_btn);
        delete_btn=(ImageView) findViewById(R.id.question_comment_delete_image);
        comment_img=(ImageView)findViewById(R.id.question_comment_img);
        delete_btn_layout=(LinearLayout)findViewById(R.id.delete_btn_layout);

        choose_img_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showSelectedPictureDialogue();


            }
        });


        delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choose_img_btn.setVisibility(View.VISIBLE);
                delete_btn_layout.setVisibility(View.GONE);
            }
        });

        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendtext=post_comment.getText().toString().trim();






                if(NetUtils.isOnline(QuestionSolutionActivity.this)) {
                    if (!post_comment.getText().toString().trim().isEmpty()) {

                        emptyEditText = true;
                        new addSolution(session,imageAttached, uuid, imageUrl).execute();
                        clearfield();
                        delete_btn.setVisibility(View.GONE);
                        delete_btn_layout.setVisibility(View.GONE);


                    }else{
                        emptyEditText = false;
                        Toast.makeText(QuestionSolutionActivity.this,"Please enter the solutions",Toast.LENGTH_LONG).show();

                    }
                }else{
                    Toast.makeText(QuestionSolutionActivity.this, R.string.no_internet_msg, Toast.LENGTH_SHORT).show();
                }

            }
        });

    }



    private void clearfield(){

        post_comment.getText().clear();
        comment_img.setImageDrawable(null);
    }



    protected void showSelectedPictureDialogue() {

        AlertDialog.Builder getImageFrom = new AlertDialog.Builder(this);
        getImageFrom.setTitle("Select:");
        final CharSequence[] opsChars = { getResources().getString(R.string.take_picture),
                getResources().getString(R.string.choose_gallery),"Cancel"};
        getImageFrom.setItems(opsChars, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    Intent cameraIntent = new Intent(
                            MediaStore.ACTION_IMAGE_CAPTURE);
                    File imagesFolder = Environment
                            .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                    if (!imagesFolder.exists()) {
                        imagesFolder.mkdirs();
                    }
                    startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
                } else if (which == 1) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(
                            Intent.createChooser(intent, getResources().getString(R.string.open)),
                            GALLERY_REQUEST_CODE);
                }
                else if (which == 2) {
                    dialog.dismiss();
                }


            }
        });
        getImageFrom.create().show();
    }

    class  getSolutions extends AsyncTask<JSONObject, JSONObject, JSONObject> {
        private String url;
        private ProgressDialog loading;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading=new ProgressDialog(QuestionSolutionActivity.this);
            loading.setMessage("Please wait...");
            loading.show();
        }

        @Override
        protected JSONObject doInBackground(JSONObject... params) {


            JSONObject jsonRes = null;
            this.url = session.getApiUrl("getSolutions");

            Map<String, Object> httpParams = new HashMap<String, Object>();
            httpParams.put("qId", question_id);
            httpParams.put("attempted","true");
            httpParams.put("callingApp", "TapApp");
            httpParams.put("orderBy", "timeCreated");
            httpParams.put("callingAppId", "TapApp");



            session.addSessionParams(httpParams);


            try {
                jsonRes = VedantuWebUtils.getJSONData(url, VedantuWebUtils.GET_REQ, httpParams);
                Log.e("questionsolution","...."+jsonRes);
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
                    solutionsresult = jsonObject.getJSONObject("result");

                    if (solutionsresult != null) {
                        questionsolutionaddlist.clear();



                        totalHits = solutionsresult.getString("totalHits");

                        listArray = solutionsresult.getJSONArray("list");


                        for (int i = 0; i < listArray.length(); i++) {

                            QuestionInfo questionInfo = new QuestionInfo();

                            JSONObject object = listArray.getJSONObject(i);
                            questionInfo.id = object.getString("id");
                            questionInfo.qid=object.getString("qid");
                            questionInfo.timeCreated = object.getLong("timeCreated");
                            questionInfo.lastUpdated = String.valueOf(object.getLong("lastUpdated"));
                            questionInfo.content = object.getString("content");
                            userlist = object.getJSONObject("user");
                            questionInfo.firstName = userlist.getString("firstName");
                            questionInfo.thumbnail = userlist.getString("thumbnail");
                            questionInfo.comments = object.getString("comments");

                            Log.e("comments_log", "...." + questionInfo.comments);
                            questionInfo.totalHits = totalHits;

                            questionsolutionaddlist.add(questionInfo);
                            Log.e("info_list", "...." + questionInfo);

                            Log.e("total_list", "..." + questionsolutionaddlist.toString());

                        }


                       solutionAdapter.notifyDataSetChanged();
//                        refreshLayout.setRefreshing(false);

                        if(questionsolutionaddlist != null && questionsolutionaddlist.size()>0){
                            emptymsg_textview.setVisibility(View.GONE);
                        }else{
                            emptymsg_textview.setVisibility(View.VISIBLE);
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            loading.dismiss();

        }


    }
    private class QuestionSolutionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private Activity activity;
        private  List<QuestionInfo>questionsolutionaddlist;
        public QuestionSolutionAdapter(Activity activity, List<QuestionInfo> solutionaddlist) {
            this.activity=activity;
            this.questionsolutionaddlist=solutionaddlist;

        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.get_question_solutions, parent, false);
            return new MyHolder(itemView);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            MyHolder itemHolder = (MyHolder) holder;
            questionInfo=questionsolutionaddlist.get(position);

            itemHolder.solution_id.setText(questionInfo.id);
            itemHolder.firstName.setText(questionInfo.firstName);

            itemHolder.commentscount.setText("("+questionInfo.comments+")");
//            itemHolder.content.loadDataWithBaseURL(null, questionInfo.content, "text/html", "UTF-8", null);
//            itemHolder.content.loadDataWithBaseURL(null, "<style>img{display: inline;height: auto;max-width: 100%;}</style>" + questionInfo.content, "text/html", "UTF-8", null);
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
            String dateTime = convertDate(questionInfo.lastUpdated,"dd/MM/yyyy hh:mm:ss");
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            Date date = null;
            String niceDateStr = (String) DateUtils.getRelativeTimeSpanString(getGMTDate(Long.parseLong(questionInfo.lastUpdated)).getTime() , Calendar.getInstance().getTimeInMillis(), DateUtils.MINUTE_IN_MILLIS);
            itemHolder.time.setText(getlongtoago(Long.parseLong(questionInfo.lastUpdated)));
            itemHolder.showcomments.setTag(position);



        }

        @Override
        public int getItemCount() {
            return questionsolutionaddlist.size();
        }

        private class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public TextView lastName;
            public TextView firstName;
            public  TextView time,solution_id;
            public ImageView user_image;
            public WebView content;
            public TextView showcomments;
            public  TextView commentscount;


            public MyHolder(View itemView) {
                super(itemView);


                firstName= (TextView)itemView.findViewById(R.id.view_sol_firstname);
                lastName= (TextView)itemView.findViewById(R.id.view_sol_lastName);
                time= (TextView)itemView.findViewById(R.id.view_sol_time);
                user_image=(ImageView)itemView.findViewById(R.id.view_sol_profile);
                content = (WebView) itemView.findViewById(R.id.view_content_img);
                showcomments=(TextView)itemView.findViewById(R.id.view_show_comments);
                solution_id=(TextView)itemView.findViewById(R.id.view_sol_id);
                commentscount=(TextView)itemView.findViewById(R.id.view_comments_count);
                showcomments.setOnClickListener(this);

            }

            @Override
            public void onClick(View v) {
                Intent intent=new Intent(activity,QuestionCommentsActivity.class);
                int position = (int) v.getTag();
                intent.putExtra(ConstantGlobal.ID, questionsolutionaddlist.get(position).id);
                intent.putExtra(ConstantGlobal.QID,questionsolutionaddlist.get(position).qid);
                intent.putExtra("position", position);
                startActivityForResult(intent, 100);
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




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode != 100) {
            final ProgressDialog dialog = new ProgressDialog(this);
            dialog.setCancelable(false);
            dialog.setMessage("Uploading Image...");
            dialog.show();

            ImageUploaderTask upImageUploaderTask = new ImageUploaderTask(session, null,
                    new ITaskProcessor<JSONObject>() {
                        @Override
                        public void onTaskStart(JSONObject result) {
                        }
                        @Override
                        public void onTaskPostExecute(boolean success, JSONObject result) {
                            dialog.cancel();
                            if (!success) {
                                return;
                            }
                            result = JSONUtils.getJSONObject(result, VedantuWebUtils.KEY_RESULT);
                            boolean uploaded = JSONUtils.getBoolean(result, "uploaded");
                            imageAttached = uploaded;
                            if (uploaded) {
                                try {
                                    Log.e("result url", result.getString("imgHtml"));

                                    uuid = result.getString("uuid");
                              Document doc = Jsoup.parse(result.getString("imgHtml"));
                                    Elements element = doc.getAllElements();
                                    for(Element e: element)
                                    {
                                        Elements str = e.getElementsByTag("img");
                                        for(Element el: str)
                                        {
                                            String src= el.attr("src");
                                            System.out.println("The src:"+src+".."+uuid);
                                            imageUrl = src;
                                            imgUrl = src;
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                cameraFileName = null;
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        getString(R.string.error_upload), Toast.LENGTH_LONG).show();
                            }
                        }
                    }, "uploadImage");
            if (requestCode == GALLERY_REQUEST_CODE && data != null) {
                imageUri = data.getData();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){

                    imagePath =  RealPathUtil.getRealPathFromURI_API19(QuestionSolutionActivity.this, imageUri);

                } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH && Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT){

                    imagePath =  RealPathUtil.getRealPathFromURI_API11to18(QuestionSolutionActivity.this, imageUri);

                }

                onSelectFromGalleryResult(imageUri);
                upImageUploaderTask.executeTask(false, imagePath);
            } else if (requestCode == CAMERA_REQUEST_CODE) {

                Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
                File destination = new File(Environment.getExternalStorageDirectory(),
                        System.currentTimeMillis() + ".jpg");
                FileOutputStream fo;
                try {
                    destination.createNewFile();
                    fo = new FileOutputStream(destination);
                    fo.write(bytes.toByteArray());
                    fo.close();


                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                upImageUploaderTask.executeTask(false, destination.getAbsolutePath());

                Log.e("image url", thumbnail.toString());
                comment_img.setImageBitmap(thumbnail);
                delete_btn_layout.setVisibility(View.VISIBLE);
                choose_img_btn.setVisibility(View.GONE);

            }
        }else if(resultCode == RESULT_OK && requestCode == 100){

             if(data != null){
                 int position = data.getIntExtra("position", 0);
                 int count = data.getIntExtra("count", 0);
                 Log.e("position, count", position+".."+count);
                 QuestionInfo questionInfo = questionsolutionaddlist.get(position);
                 questionsolutionaddlist.remove(questionInfo);
                 String totalCount = questionInfo.comments;
                 totalCount = totalCount+count;
                 questionInfo.comments = totalCount;
                 questionsolutionaddlist.add(position, questionInfo);
                 solutionAdapter.notifyDataSetChanged();
             }

        }
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Uri imageUri) {
        Bitmap bm=null;

        try {
            bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), imageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.e("bitmap", bm.toString());
        comment_img.setImageBitmap(bm);
        delete_btn_layout.setVisibility(View.VISIBLE);
        choose_img_btn.setVisibility(View.VISIBLE);
    }



    class  addSolution extends AsyncTask<JSONObject, JSONObject, JSONObject> {
        private String url;
        private String uuid, imageUrl,imgUrl;
        private boolean imageAttached = false;
        private ProgressDialog pDialog;

        public addSolution(SessionManager session, boolean imageAttached, String uuid, String imageUrl) {

            this.imageAttached = imageAttached;
            this.uuid = uuid;
            this.imageUrl = imageUrl;
            pDialog = new ProgressDialog(QuestionSolutionActivity.this);
            pDialog.setMessage("Please wait..");
            pDialog.show();

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            loading=new ProgressDialog(ViewSolutionActivity.this);
//            loading.setMessage("Please wait...");
//            loading.show();
        }

        @Override
        protected JSONObject doInBackground(JSONObject... params) {


            JSONObject jsonRes = null;
            this.url = session.getApiUrl("addSolution");

            Map<String, Object> httpParams = new HashMap<String, Object>();
            httpParams.put("qId", question_id);


            //session.addSessionParams(httpParams);
            if(imageAttached){

                String imageUrl2 = String.format("<div class=\"RTEImageDiv\"> \n <img src=\""+imgUrl+"\" v-uid=\""+uuid+"\" class=\"vUrl\" /> </div \">"+sendtext+"</div>");

                httpParams.put("content",imageUrl2);
                // session.addSessionParams(httpParams);
            }else {
                httpParams.put("content",sendtext);
                //session.addSessionParams(httpParams);
            }

            session.addSessionParams(httpParams);



            try {
                jsonRes = VedantuWebUtils.getJSONData(url, VedantuWebUtils.POST_REQ, httpParams);
                Log.e("addsolution","...."+jsonRes);
            } catch (IOException e) {
                e.printStackTrace();
            }


            return jsonRes;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);

            pDialog.dismiss();

            JSONObject  solutionsresult = null;
            try {
                solutionsresult = jsonObject.getJSONObject("result");

            if (solutionsresult != null) {



                listArray = solutionsresult.getJSONArray("list");


                for (int i = 0; i < listArray.length(); i++) {

                    QuestionInfo questionInfo = new QuestionInfo();

                    JSONObject object = listArray.getJSONObject(i);
                    questionInfo.id = object.getString("id");
                    questionInfo.qid=object.getString("qid");
                    questionInfo.timeCreated = object.getLong("timeCreated");
                    questionInfo.lastUpdated = String.valueOf(object.getLong("lastUpdated"));
                    questionInfo.content = object.getString("content");
                    userlist = object.getJSONObject("user");
                    questionInfo.firstName = userlist.getString("firstName");
                    questionInfo.thumbnail = userlist.getString("thumbnail");
                    questionInfo.comments = object.getString("comments");

                    Log.e("comments_log", "...." + questionInfo.comments);
                    questionInfo.totalHits = totalHits;

                    questionsolutionaddlist.add(questionInfo);
                    Log.e("info_list", "...." + questionInfo);

                    Log.e("total_list", "..." + questionsolutionaddlist.toString());

                }


                solutionAdapter.notifyDataSetChanged();
//                refreshLayout.setRefreshing(false);

                if(questionsolutionaddlist != null && questionsolutionaddlist.size()>0){
                    emptymsg_textview.setVisibility(View.GONE);
                }else{
                    emptymsg_textview.setVisibility(View.VISIBLE);
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        }


    }


    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    public void finish() {
        Intent data = new Intent();
        data.putExtra("solCount", questionsolutionaddlist.size());
         setResult(102, data);
        super.finish();
    }
}
