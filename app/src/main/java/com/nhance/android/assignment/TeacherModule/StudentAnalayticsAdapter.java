package com.nhance.android.assignment.TeacherModule;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nhance.android.R;

import com.nhance.android.assignment.TeacherPerformance.TeacherPerformanceActivity;
import com.nhance.android.assignment.db.models.analytics.AssignmentAnalytics;
import com.nhance.android.constants.ConstantGlobal;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class StudentAnalayticsAdapter extends BaseAdapter {

    private Activity activity;
    private CustomProgressBar seekbar;
    private String assignmentName;
    private ArrayList progressItemList;
    private ProgressItem mProgressItem;
    private ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater=null;
    private int progressStatus;

    private String entityId, entityType, targetUserId;
private AssignmentAnalytics assignment;
    private TextView userid;
    private HashMap<String, String> question_list = new HashMap<String, String>();
    private ArrayList<String> targetuserid = new ArrayList<>();
    private ImageView user_image;

    public StudentAnalayticsAdapter(Activity a, ArrayList<HashMap<String, String>> d, String entityId, String entityType, String targetUserId,String assignmentName) {
        this.activity = a;
        this.data=d;
        this.assignmentName=assignmentName;
        this.entityId = entityId;
        this.entityType = entityType;
        this.targetUserId = targetUserId;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.fragment_assignment_student_analytics_result, null);

        TextView firstname = (TextView)vi.findViewById(R.id.ass_username);
        TextView prgname = (TextView)vi.findViewById(R.id.ass_programname);
        TextView correct = (TextView)vi.findViewById(R.id.ass_correctanswer);
        TextView left = (TextView)vi.findViewById(R.id.ass_left);

        final TextView incorrect = (TextView)vi.findViewById(R.id.ass_incorrectanswer);
          userid = (TextView)vi.findViewById(R.id.userid);


        TextView date = (TextView)vi.findViewById(R.id.ass_lastdateatempted);
         user_image=(ImageView)vi.findViewById(R.id.ass_userprofile);
        Button viewattempts=(Button)vi.findViewById(R.id.viewattempts);
        seekbar = ((CustomProgressBar) vi.findViewById(R.id.student_progressbar));
        seekbar.getThumb().mutate().setAlpha(0);


        question_list = data.get(position);


        float crr_question = Float.parseFloat(question_list.get("correct"));


        float incrr_question = Float.parseFloat(question_list.get("incorrect"));
        float left_question= Float.parseFloat(question_list.get("left"));

        float total_question = crr_question+incrr_question+left_question;

        final float corrPer=(crr_question/total_question)*100;
        final float incorrPer=(incrr_question/total_question)*100;
        final float leftOver=(left_question/total_question)*100;



        progressStatus = 0;

        new Thread(new Runnable() {
            @Override
            public void run() {

                initDataToSeekbar( corrPer, incorrPer, leftOver);

                try{
                    Thread.sleep(20);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }


            }
        }).start();





        // Setting all values in listview
        firstname.setText(question_list.get("firstName"));
        prgname.setText(question_list.get("name"));
        userid.setText(question_list.get("userId"));
        targetuserid.add(question_list.get("userId"));
        correct.setText(question_list.get("correct"));
        incorrect.setText(question_list.get("incorrect"));
        date.setText(question_list.get("lastAttempted"));
        left.setText(question_list.get("left"));





        user_image.setTag(question_list.get("thumbnail"));
        new DownloadImageTask().execute(user_image);

        viewattempts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                StudentPerformanceActivity
               Intent intent=new Intent(activity.getApplicationContext(),TeacherPerformanceActivity.class);

                intent.putExtra(ConstantGlobal.ASSIGNMENT_NAME,
                        assignmentName);
                intent.putExtra(ConstantGlobal.ENTITY_ID, entityId);
                intent.putExtra(ConstantGlobal.ENTITY_TYPE, entityType);
                intent.putExtra(ConstantGlobal.TARGET_USERID, targetUserId);
                intent.putExtra("userId",targetuserid.get(position));

Log.e("ass_name","........."+assignmentName);
                activity.startActivity(intent);
                activity.finish();
            //    Toast.makeText(activity, "position "+position, Toast.LENGTH_SHORT).show();
               // Toast.makeText(activity, "userid "+targetuserid.get(position), Toast.LENGTH_SHORT).show();
            }
        });

        return vi;
    }




class DownloadImageTask extends AsyncTask<ImageView, Void, Bitmap> {

    ImageView imageView = null;

    @Override
    protected Bitmap doInBackground(ImageView... imageViews) {
        this.imageView = imageViews[0];
        return download_Image((String)imageView.getTag());
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        imageView.setImageBitmap(result);
    }

    private Bitmap download_Image(String url) {

        Bitmap bmp =null;
        try{
            URL ulrn = new URL(url);
            HttpURLConnection con = (HttpURLConnection)ulrn.openConnection();
            InputStream is = con.getInputStream();
            bmp = BitmapFactory.decodeStream(is);
            if (null != bmp)
                return bmp;

        }catch(Exception e){}
        return bmp;
    }

}





    private void initDataToSeekbar(float corr, float inCorr, float left) {
        Log.e("correct.."+corr, "inCorr.."+inCorr+"..left.."+left);
        progressItemList = new ArrayList();
        // red span
        mProgressItem = new ProgressItem();
        mProgressItem.progressItemPercentage = corr;
        Log.i("Mainactivity", mProgressItem.progressItemPercentage + "");
        mProgressItem.color = R.color.green;
        progressItemList.add(mProgressItem);
        // blue span
        mProgressItem = new ProgressItem();
        mProgressItem.progressItemPercentage = inCorr;
        mProgressItem.color = R.color.red;
        progressItemList.add(mProgressItem);
        // green span
        mProgressItem = new ProgressItem();
        mProgressItem.progressItemPercentage = left;
        mProgressItem.color = R.color.lightergrey;
        progressItemList.add(mProgressItem);
        seekbar.initData(progressItemList);
         // seekbar.invalidate();
    }
}