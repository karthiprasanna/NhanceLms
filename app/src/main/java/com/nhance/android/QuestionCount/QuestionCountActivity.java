package com.nhance.android.QuestionCount;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.nhance.android.R;
import com.nhance.android.activities.baseclasses.NhanceBaseActivity;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.customviews.NonScrollableGridView;
import com.nhance.android.db.datamanagers.QuestionAttemptStatusDataManager;
import com.nhance.android.db.models.entity.QuestionStatus;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.managers.VApp;
import com.nhance.android.pojos.LibraryContentRes;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.List;

public class QuestionCountActivity extends NhanceBaseActivity {

    private QuestionAttemptStatusDataManager questionAttemptStatus;
    private SessionManager session;
    private List<LibraryContentRes> items;
//    private int defaultFetchSize = 15;
    private NonScrollableGridView gridView;
    private GridViewAdapter gridViewAdapter;
    private String courseName;
    private String section_id, center_id, program_id, course_entity_id;
    private QuestionStatus questionStatus, questionStatus1;
    private String option_correct_answer = null;
    private String option_answerGiven = null;
    private String questionId;
    private List<QuestionStatus> questionStatusList;
    private int itemPosition;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_question_count);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
       // getSupportActionBar().setIcon(R.drawable.ic_launcher);
        getSupportActionBar().setDisplayShowCustomEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Question");

        session = SessionManager.getInstance(this);
        questionAttemptStatus = new QuestionAttemptStatusDataManager(getApplicationContext());
        section_id = getIntent().getStringExtra(ConstantGlobal.SECTION_ID);
        center_id = getIntent().getStringExtra(ConstantGlobal.CENTER_ID);
        program_id = getIntent().getStringExtra(ConstantGlobal.PROGRAM_ID);
        course_entity_id = getIntent().getStringExtra(ConstantGlobal.COURSE_ID);
        courseName = getIntent().getStringExtra("courseName");

        items = VApp.getQuestionsList();
        questionStatusList = new ArrayList<>();
        Log.e("jjj", "..." + items.size());

        Log.e("hh", items.get(0).id);
        Log.e("hh", String.valueOf(items.get(0).position));


        gridView = (NonScrollableGridView) findViewById(R.id.question_answers);


        // statusList=new ArrayList<>();


        for (int i = 0; i < items.size(); i++) {
            LibraryContentRes res = items.get(i);

            QuestionStatus questionStatus = questionAttemptStatus.getQuestionStatus(res.id);
            Log.e("questionId", "..." + res.id+","+questionStatus);
            if(questionStatus != null)
                questionStatusList.add(questionStatus);
        }

        gridViewAdapter = new GridViewAdapter(this, items, questionStatusList);
        gridView.setAdapter(gridViewAdapter);

    }

    @Override
    protected void onResume() {
        super.onResume();

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
        Log.e("onActivityResult", ".."+data);
        if(resultCode == Activity.RESULT_OK && data != null){
           boolean attempted =  data.getBooleanExtra("attempted", false);
            String questionId = data.getStringExtra("questionId");
            int pos = data.getIntExtra("position", -1);
            Log.e("onActivityResult", ".."+attempted);
            if(attempted && pos != -1){
               QuestionStatus questionStatus = questionStatusList.get(pos);
                questionStatus.attempted = true;
                questionStatus.attempts = questionStatus.attempts+1;
                questionStatusList.set(pos, questionStatus);
                gridViewAdapter.notifyDataSetChanged();

            }

        }
    }

    private class GridViewAdapter extends BaseAdapter {
        private Activity activity;
        List<LibraryContentRes> libraryContentRes;
        private LayoutInflater inflater = null;
        List<QuestionStatus> questionStatusList;
        private QuestionStatus questionStatus;

        public GridViewAdapter(Activity activity, List<LibraryContentRes> libraryContentRes, List<QuestionStatus> questionStatusList) {
            this.activity = activity;
            this.libraryContentRes = libraryContentRes;
            this.questionStatusList = questionStatusList;
            inflater = (LayoutInflater) activity.getSystemService(activity.LAYOUT_INFLATER_SERVICE);

        }

        @Override
        public int getCount() {
            return libraryContentRes.size();
        }

        @Override
        public Object getItem(int position) {
            return libraryContentRes.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

           Log.e("getView", "..called"+questionStatusList.size());
            View vi = convertView;
              itemPosition = position;

            if (vi == null)

                vi = inflater.inflate(R.layout.grid_item_layout, null);

            TextView gridview = (TextView) vi.findViewById(R.id.grid_item_title);

//            if (questionStatusList != null) {
                questionStatus = questionStatusList.get(position);

//            }

            final int s = position + 1;

            if (questionStatus != null) {
                gridview.setText("Q" + s);
                gridview.setTextColor(getResources().getColor(R.color.white));
                if (questionStatus.attempted) {
                    Log.e("question", "..attempted");
                    gridview.setBackground(getResources().getDrawable(R.drawable.question_incorrect_round_corner));
                } else {
                    gridview.setBackground(getResources().getDrawable(R.drawable.question_correct_round_corner));

                }
            }
            gridview.setTag(itemPosition);

            gridview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity, UserExamActivity.class);
//                    LibraryUtils.onLibraryItemClickListnerImpl(activity,
//                            (LibraryContentRes) v.getTag());
                    int position = (int) v.getTag();
//                    Toast.makeText(QuestionCountActivity.this, "position.."+position, Toast.LENGTH_SHORT).show();
                    intent.putExtra("courseName", courseName);
                    intent.putExtra("name", items.get(position).name);
                    intent.putExtra("type", items.get(position).subType);
                    intent.putExtra("lastUpdated", items.get(position).timeCreated);
                    intent.putExtra("ownerName", items.get(position).ownerName);
                    intent.putExtra("thumbnail", items.get(position).thumb);
                    intent.putExtra("info", String.valueOf(items.get(position).info));
                    intent.putExtra("status", questionStatusList.get(position).attempted);
                    intent.putExtra("id",items.get(position).id);
                    intent.putExtra("position", position);

                    if (questionStatusList.get(position).correctAnswer != null) {
                        try {

                            option_correct_answer = "";

                            for (int l = 0; l < questionStatusList.get(position).correctAnswer.length(); l++) {

                                String str = questionStatusList.get(position).correctAnswer.getString(l);
                                if (l != questionStatusList.get(position).correctAnswer.length() - 1) {
                                    option_correct_answer = option_correct_answer + str + ",";
                                } else {
                                    option_correct_answer = option_correct_answer + str;
                                }


                            }

                        } catch (JSONException e) {
                            Log.e("JSON", "There was an error parsing the JSON", e);
                        }
                    }


                    if (questionStatusList.get(position).answerGiven != null) {

                        try {

                            option_answerGiven = "";

                            for (int l = 0; l < questionStatusList.get(position).answerGiven.length(); l++) {

                                String str = questionStatusList.get(position).answerGiven.getString(l);
                                if (l != questionStatusList.get(position).correctAnswer.length() - 1) {
                                    option_answerGiven = option_answerGiven + str + ",";
                                } else {
                                    option_answerGiven = option_answerGiven + str;
                                }


                            }


                        } catch (JSONException e) {
                            Log.e("JSON", "There was an error parsing the JSON", e);
                        }
                    }
                    intent.putExtra("userAnswer", option_answerGiven);
                    intent.putExtra("correctAnswer", option_correct_answer);
                    startActivityForResult(intent, 100);
                }
            });


            return vi;
        }
    }


}
