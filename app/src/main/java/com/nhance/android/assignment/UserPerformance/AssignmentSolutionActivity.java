package com.nhance.android.assignment.UserPerformance;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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

public class AssignmentSolutionActivity extends AppCompatActivity {

    private String assignmentName;
    private String entity_id,entity_type;
    private SessionManager session;
    private String questionid;

    private JSONObject solutionsresult;
    private JSONArray listArray;
    private  JSONObject userlist;

    private RecyclerView info_listview;

    private int position;
    List<QuestionInfo>solutionaddlist;
    private SolutionAdapter solutionAdapter;
    private String totalHits;
    private ProgressDialog loading;

    private SwipeRefreshLayout refreshLayout;
    private EditText post_comment;
    private ImageView send_btn;
    public static final int SELECT_FILE = 101;
    public static final int REQUEST_CAMERA = 100;

    protected static final int    GALLERY_REQUEST_CODE = 101;
    protected static final int    CAMERA_REQUEST_CODE  = 102;
    private ImageView comment_img,choose_img_btn,delete_btn;
    private LinearLayout delete_btn_layout;
    private String imagePath;
    private boolean imageAttached;
    private String uuid, imageUrl,imgUrl;
    protected File                cameraFileName;
    private Uri imageUri;
    private boolean isWebViewLoaded;
    private String sendtext;
    boolean emptyEditText = false;
    private TextView emptymsg_textview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.assignment_view_solution);

        assignmentName = getIntent().getStringExtra(ConstantGlobal.ASSIGNMENT_NAME);
        entity_id = getIntent().getStringExtra(ConstantGlobal.ENTITY_ID);
        entity_type = getIntent().getStringExtra(ConstantGlobal.ENTITY_TYPE);
        session = SessionManager.getInstance(this);
        questionid = getIntent().getStringExtra(ConstantGlobal.QID);
      int  position = getIntent().getIntExtra("position",0);
        solutionaddlist=new ArrayList<>();






        emptymsg_textview=(TextView)findViewById(R.id.emptymsg_solution);


        info_listview=(RecyclerView)findViewById(R.id.solution_list);
        refreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipelayout);
        solutionAdapter = new SolutionAdapter(this,solutionaddlist,assignmentName);
        info_listview.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        info_listview.setLayoutManager(mLayoutManager);
        //info_listview.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        info_listview.setItemAnimator(new DefaultItemAnimator());
        info_listview.setAdapter(solutionAdapter);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      //  getSupportActionBar().setIcon(R.drawable.ic_launcher);
        getSupportActionBar().setDisplayShowCustomEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(assignmentName);
        if(NetUtils.isOnline(AssignmentSolutionActivity.this)) {
            new getSolutions().execute();
        }else{
            Toast.makeText(AssignmentSolutionActivity.this, R.string.no_internet_msg, Toast.LENGTH_SHORT).show();
        }

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(NetUtils.isOnline(AssignmentSolutionActivity.this)) {
                    new getSolutions().execute();
                }else{
                    Toast.makeText(AssignmentSolutionActivity.this, R.string.no_internet_msg, Toast.LENGTH_SHORT).show();
                }
            }
        });

         post_comment=(EditText)findViewById(R.id.post_assignment_comment);
         choose_img_btn=(ImageView)findViewById(R.id.select_image);
         send_btn=(ImageView)findViewById(R.id.comment_send_btn);
         delete_btn=(ImageView)findViewById(R.id.comment_delete_image);
        comment_img=(ImageView)findViewById(R.id.comment_img);
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






        if(NetUtils.isOnline(AssignmentSolutionActivity.this)) {
if (!post_comment.getText().toString().trim().isEmpty()) {

    emptyEditText = true;
    new addSolution(session,imageAttached, uuid, imageUrl).execute();
    clearfield();
    delete_btn.setVisibility(View.GONE);
    delete_btn_layout.setVisibility(View.GONE);


}else{
    emptyEditText = false;
    Toast.makeText(AssignmentSolutionActivity.this,"Please enter the solution",Toast.LENGTH_LONG).show();

}
    }else{
        Toast.makeText(AssignmentSolutionActivity.this, R.string.no_internet_msg, Toast.LENGTH_SHORT).show();
    }

    }
});



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



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
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

                    imagePath =  RealPathUtil.getRealPathFromURI_API19(AssignmentSolutionActivity.this, imageUri);

                } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH && Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT){

                    imagePath =  RealPathUtil.getRealPathFromURI_API11to18(AssignmentSolutionActivity.this, imageUri);

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
        private String uuid, imageUrl;
        private boolean imageAttached = false;
        private ProgressDialog pDialog;

        public addSolution(SessionManager session, boolean imageAttached, String uuid, String imageUrl) {
            String url = session.getApiUrl("addSolution");

            this.imageAttached = imageAttached;
            this.uuid = uuid;
            this.imageUrl = imageUrl;
            pDialog = new ProgressDialog(AssignmentSolutionActivity.this);
            pDialog.setMessage("Please wait..");
            loading.show();

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
            httpParams.put("qId", questionid);


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

loading.dismiss();

if (jsonObject!=null){


    try {
        solutionsresult = jsonObject.getJSONObject("result");


         //   solutionaddlist.clear();



//            totalHits = solutionsresult.getString("totalHits");

            listArray = solutionsresult.getJSONArray("list");


            for (int i = 0; i < listArray.length(); i++) {

                QuestionInfo questionInfo = new QuestionInfo();

                JSONObject object = listArray.getJSONObject(i);
              //  questionInfo.id = object.getString("id");
                questionInfo.qid=object.getString("qid");
                questionInfo.timeCreated = object.getLong("timeCreated");
                questionInfo.lastUpdated = String.valueOf(object.getLong("lastUpdated"));
                questionInfo.content = object.getString("content");
                userlist = object.getJSONObject("user");
                questionInfo.firstName = userlist.getString("firstName");
                questionInfo.thumbnail = userlist.getString("thumbnail");
                questionInfo.comments = object.getString("comments");
                questionInfo.lastName = userlist.getString("lastName");


              //  questionInfo.totalHits = totalHits;

                solutionaddlist.add(questionInfo);


            }


            solutionAdapter.notifyDataSetChanged();
            refreshLayout.setRefreshing(false);

            if(solutionaddlist != null && solutionaddlist.size()>0){
                emptymsg_textview.setVisibility(View.GONE);
            }else{
                emptymsg_textview.setVisibility(View.VISIBLE);
            }


    } catch (JSONException e) {
        e.printStackTrace();
    }
}



        }


    }


    private void clearfield(){

        post_comment.getText().clear();
        comment_img.setImageDrawable(null);
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

    class  getSolutions extends AsyncTask<JSONObject, JSONObject, JSONObject> {
        private String url;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
             loading=new ProgressDialog(AssignmentSolutionActivity.this);
            loading.setMessage("Please wait...");
            loading.show();
        }

        @Override
        protected JSONObject doInBackground(JSONObject... params) {


            JSONObject jsonRes = null;
            this.url = session.getApiUrl("getSolutions");

            Map<String, Object> httpParams = new HashMap<String, Object>();
            httpParams.put("qId", questionid);
            httpParams.put("attempted","true");



            session.addSessionParams(httpParams);


            try {
                jsonRes = VedantuWebUtils.getJSONData(url, VedantuWebUtils.GET_REQ, httpParams);
                Log.e("getsolutions","...."+jsonRes);

            //    Log.e("sollist","...."+jsonRes);
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
                    solutionsresult = jsonObject.getJSONObject("result");

                    if (solutionsresult != null) {
                        solutionaddlist.clear();



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
                            questionInfo.lastName = userlist.getString("lastName");
                            Log.e("comments_log", "...." + questionInfo.comments);
                            questionInfo.totalHits = totalHits;

                            solutionaddlist.add(questionInfo);
                            Log.e("info_list", "...." + questionInfo);

                            Log.e("total_list", "..." + solutionaddlist.toString());

                        }


                        solutionAdapter.notifyDataSetChanged();
                        refreshLayout.setRefreshing(false);

                        if(solutionaddlist != null && solutionaddlist.size()>0){
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




    private class SolutionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private Activity activity;
        private  List<QuestionInfo>solutionaddlist;
        private String assignmentName;
        public SolutionAdapter(Activity activity, List<QuestionInfo> solutionaddlist,String assignmentName) {
            this.activity=activity;
            this.solutionaddlist=solutionaddlist;
            this.assignmentName=assignmentName;





        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.get_solutions, parent, false);
            return new MyHolder(itemView);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            MyHolder itemHolder = (MyHolder) holder;
            final QuestionInfo questionInfo=solutionaddlist.get(position);

            //Log.e("questionInfo","...."+questionInfo.toString());
            itemHolder.solution_id.setText(questionInfo.id);
            itemHolder.firstName.setText(questionInfo.firstName+" "+questionInfo.lastName);
            //itemHolder.commentscount.setText(questionInfo.comments);
           // itemHolder.commentscount.setPaintFlags(itemHolder.commentscount.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

            itemHolder.commentscount.setText("("+questionInfo.comments+")");
            itemHolder.content.loadDataWithBaseURL(null, "<style>img{display: inline;height: auto;max-width: 100%;}</style>" + questionInfo.content, "text/html", "UTF-8", null);

           // itemHolder.content.loadDataWithBaseURL(null, questionInfo.content, "text/html", "UTF-8", null);
            Picasso.with(activity).load(questionInfo.thumbnail).into(itemHolder.user_image);
            String dateTime = convertDate(questionInfo.lastUpdated,"dd/MM/yyyy hh:mm:ss");
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            Date date = null;
            String niceDateStr = (String) DateUtils.getRelativeTimeSpanString(getGMTDate(Long.parseLong(questionInfo.lastUpdated)).getTime() , Calendar.getInstance().getTimeInMillis(), DateUtils.MINUTE_IN_MILLIS);
            itemHolder.time.setText(getlongtoago(Long.parseLong(questionInfo.lastUpdated)));
            itemHolder.showcomments.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent=new Intent(activity,AssignmentCommentActivity.class);
                    intent.putExtra(ConstantGlobal.ID,questionInfo.id);
                    intent.putExtra(ConstantGlobal.QID,questionInfo.qid);
                    intent.putExtra(ConstantGlobal.ASSIGNMENT_NAME,
                            assignmentName);
                    startActivity(intent);

                }
            });







        }

        @Override
        public int getItemCount() {
            return solutionaddlist.size();
        }

        private class MyHolder extends RecyclerView.ViewHolder {
            public TextView lastName;
            public TextView firstName;
            public  TextView time,solution_id;
            public ImageView user_image;
            public WebView content;
            public TextView showcomments;
            public  TextView commentscount;
            public MyHolder(View itemView) {
                super(itemView);


                firstName= (TextView)itemView.findViewById(R.id.sol_firstname);
                lastName= (TextView)itemView.findViewById(R.id.sol_lastName);
                time= (TextView)itemView.findViewById(R.id.sol_time);
                user_image=(ImageView)itemView.findViewById(R.id.sol_profile);
                content = (WebView) itemView.findViewById(R.id.content_img);
                showcomments=(TextView)itemView.findViewById(R.id.show_commenys);
                solution_id=(TextView)itemView.findViewById(R.id.sol_id);
                commentscount=(TextView)itemView.findViewById(R.id.comments_count);




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
