package com.nhance.android.assignment.TeacherPerformance;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nhance.android.R;
import com.nhance.android.assignment.StudentPerformance.AsssignmentQuestion;
import com.nhance.android.assignment.TeacherPerformance.fragment.TeacherPerformanceExamFragment;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.managers.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TeacherExamViewActivity extends AppCompatActivity {

    private String questionId,question_type,question_status,solutions,options,content,answer,entityId,entityType,assignmentName;
    private TextView ass_type,attmpt_status,attempt_btn;
    private LinearLayout lin_youranswer;

    private String option_answerg_given;
    private String option_correct_answer;
    private TextView  info_youranswer;
    TextView info_ccorrectanswer;
    private SessionManager session;
    private JSONArray jsonArray;
    private AsssignmentQuestion asssignmentQuestion;
    private AsssignmentQuestion currentQuestionData;
private TeacherPerformanceExamFragment currentQuestionFragment;

    private String optionsString;
    private Bundle bundle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_teacher_exam_view);
        ass_type = (TextView) findViewById(R.id.ass_questiontype);
        attmpt_status = (TextView) findViewById(R.id.attmpt_status);
        lin_youranswer = (LinearLayout) findViewById(R.id.lin_youranswer);
        attempt_btn= (TextView) findViewById(R.id.ass_attempt_btn);
        info_youranswer=(TextView)findViewById(R.id.ass_uranswer);
        info_ccorrectanswer=(TextView)findViewById(R.id.ass_crtanser);







        question_type=getIntent().getStringExtra(ConstantGlobal.TYPE);
        question_status=getIntent().getStringExtra(ConstantGlobal.STATUS);
        entityId=getIntent().getStringExtra(ConstantGlobal.ENTITY_ID);
        entityType=getIntent().getStringExtra(ConstantGlobal.ENTITY_TYPE);
        answer=getIntent().getStringExtra(ConstantGlobal.ANSWER);
        assignmentName=getIntent().getStringExtra(ConstantGlobal.ASSIGNMENT_NAME);
        options=getIntent().getStringExtra(ConstantGlobal.OPTIONS);
        content=getIntent().getStringExtra(ConstantGlobal.CONTENT);
        questionId=getIntent().getStringExtra(ConstantGlobal.ID);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // getSupportActionBar().setIcon(R.drawable.ic_launcher);
        getSupportActionBar().setDisplayShowCustomEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(assignmentName);
        session = SessionManager.getInstance(this);


        attempt_btn.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        Intent intent=new Intent(TeacherExamViewActivity.this,TeacherPurposeExamActivity.class);
        intent.putExtra(ConstantGlobal.ID,questionId);
        intent.putExtra(ConstantGlobal.ASSIGNMENT_NAME,assignmentName);

        startActivity(intent);
    }
});




        try {
            JSONObject answer_object=new JSONObject(answer);


            if (answer!=null) {

//                    correctAnswer=null;
//                    answegiven=null;

                JSONArray server_correct_answer = answer_object.getJSONArray("correctAnswer");
                JSONArray server_answer_given = answer_object.optJSONArray("answerGiven");

                try {

                    option_correct_answer = "";

                    for (int l = 0; l< server_correct_answer.length(); l++) {

                        String str = server_correct_answer.getString(l);
                        if (l != server_correct_answer.length() - 1) {
                            option_correct_answer = option_correct_answer + str + ",";
                        } else {
                            option_correct_answer = option_correct_answer + str;
                        }


                    }



                } catch (JSONException e) {
                    Log.e("JSON", "There was an error parsing the JSON", e);
                }
                if (server_answer_given!=null) {

                    try {
                        option_answerg_given="";

                        for (int l = 0; l < server_answer_given.length(); l++) {

                            String str = server_answer_given.getString(l);
                            if (l != server_answer_given.length() - 1) {
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




        } catch (JSONException e) {
            e.printStackTrace();
        }


        try {
            jsonArray = new JSONArray(options);
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
        asssignmentQuestion.name=content;
        asssignmentQuestion.options=optionsString;
        asssignmentQuestion.type=question_type;

        SetUpAssignmentQuestion();
        info_youranswer.setText(option_answerg_given);
        info_ccorrectanswer.setText(option_correct_answer);

        ass_type.setText(question_type);
        if (question_status!=null && question_status.equalsIgnoreCase("ATTEMPTED")){

            lin_youranswer.setVisibility(View.VISIBLE);
            attmpt_status.setVisibility(View.GONE);
            attempt_btn.setVisibility(View.VISIBLE);

        }else {

            attmpt_status.setVisibility(View.VISIBLE);
            attempt_btn.setVisibility(View.GONE);

        }
    }

    private void SetUpAssignmentQuestion() {



        currentQuestionFragment = new TeacherPerformanceExamFragment();
        bundle = new Bundle();
        bundle.putString("content",content);
        bundle.putString("options",optionsString);
        bundle.putString("type",question_type);
        currentQuestionFragment.setArguments(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.framlayout, currentQuestionFragment).commit();
        currentQuestionData = getCurrentQuestionData();
        //  Log.e("ques","..."+currentQuestionData);


        asssignmentQuestion.name=content;
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
