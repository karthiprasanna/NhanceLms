package com.nhance.android.assignment.TeacherModule;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.nhance.android.R;
import com.nhance.android.activities.baseclasses.NhanceBaseActivity;
import com.nhance.android.activities.content.players.VideoPlayerFullScreenActivity;
import com.nhance.android.assignment.StudentPerformance.AsssignmentQuestion;
import com.nhance.android.assignment.pojo.TakeAssignmentQuestionWithAnswerGiven;
import com.nhance.android.async.tasks.ITaskProcessor;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.ContentDataManager;
import com.nhance.android.db.models.entity.Answer;
import com.nhance.android.db.models.entity.Question;
import com.nhance.android.enums.EntityType;
import com.nhance.android.jsinterfaces.AbstractJSInterface;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.utils.JSONUtils;
import com.nhance.android.utils.QuestionImageUtil;
import com.nhance.android.web.VedantuWebChromeClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;



public class StudentQuestionStatusActivity extends NhanceBaseActivity {
    private String assignmentName;
    private String contentname,typename,optionsname,questionnumber;
    private AsssignmentQuestion asssignmentQuestion;
    private TextView ass_type,question_number;
    private JSONArray jsonArray;
    private String optionsString;
    private studentQuestionStatusFragment currentQuestionFragment;
    private Bundle bundle;
    private AsssignmentQuestion currentQuestionData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_student_question_status);

        assignmentName=getIntent().getStringExtra(ConstantGlobal.ASSIGNMENT_NAME);
        contentname=getIntent().getStringExtra(ConstantGlobal.CONTENT);
        questionnumber=getIntent().getStringExtra("qno");
        typename=getIntent().getStringExtra(ConstantGlobal.TYPE);
        optionsname=getIntent().getStringExtra(ConstantGlobal.OPTIONS);
        ass_type = (TextView) findViewById(R.id.list_questiontype);
        question_number = (TextView) findViewById(R.id.question_no);

        ass_type.setText(typename);
        question_number.setText("Q"+questionnumber);


        asssignmentQuestion=new AsssignmentQuestion();

Log.e("kkk","....."+assignmentName+""+contentname+""+typename+""+optionsname);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_launcher);
        getSupportActionBar().setDisplayShowCustomEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(assignmentName);




        try {
            jsonArray = new JSONArray(optionsname);
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
        asssignmentQuestion.name=contentname;
        asssignmentQuestion.options=optionsString;
        asssignmentQuestion.type=typename;
        setUpAssignmentQuestion();



    }

    private void setUpAssignmentQuestion() {



        currentQuestionFragment = new studentQuestionStatusFragment();
        bundle = new Bundle();
        bundle.putString("content",contentname);
        bundle.putString("options",optionsString);
        bundle.putString("type",typename);
        currentQuestionFragment.setArguments(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.framlayout, currentQuestionFragment).commit();
        currentQuestionData = getCurrentQuestionData();
        //  Log.e("ques","..."+currentQuestionData);


        asssignmentQuestion.name=contentname;
        asssignmentQuestion.options=optionsString;
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


    public AsssignmentQuestion getCurrentQuestionData() {

        return asssignmentQuestion;
    }
}











