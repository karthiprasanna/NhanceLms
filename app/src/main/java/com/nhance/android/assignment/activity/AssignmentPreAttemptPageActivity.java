package com.nhance.android.assignment.activity;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.nhance.android.R;
import com.nhance.android.activities.baseclasses.NhanceBaseActivity;
import com.nhance.android.assignment.StudentPerformance.AssignmentData;
import com.nhance.android.assignment.StudentPerformance.NetUtils;
import com.nhance.android.assignment.UserPerformance.StudentExamGridActivity;
import com.nhance.android.assignment.async.tasks.AssignmentDownloader;
import com.nhance.android.assignment.db.models.analytics.AssignmentAnalytics;
import com.nhance.android.assignment.fragments.assignment.AssignmentInstructionsDialogFragment;
import com.nhance.android.assignment.fragments.assignment.AssignmentPostAttemptPageFragment;
import com.nhance.android.assignment.fragments.assignment.AssignmentPreAttemptPageFragment;
import com.nhance.android.assignment.pojo.content.infos.AssignmentExtendedInfo;
import com.nhance.android.assignment.processors.analytics.sync.AssignmentServerToLocalAnalyticsSyncProcessor;
import com.nhance.android.async.tasks.IDownloadCompleteProcessor;
import com.nhance.android.async.tasks.ITaskProcessor;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.AnalyticsDataManager;
import com.nhance.android.db.datamanagers.ContentDataManager;
import com.nhance.android.db.datamanagers.ModuleStatusDataManager;
import com.nhance.android.db.models.entity.Content;
import com.nhance.android.enums.AttemptState;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.utils.GoogleAnalyticsUtils;
import com.nhance.android.utils.VedantuWebUtils;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//Himank Shah
public class AssignmentPreAttemptPageActivity extends NhanceBaseActivity implements
        ITaskProcessor<JSONObject> {
    private String entityId, entityType;

    private static final String TAG = "AssignmentPreAttemptPageActivity";

    private int contentId;

    private AssignmentExtendedInfo assignmentInfo;
    private AnalyticsDataManager analyticsDataManager;
    private int                  currentSubjectIndex   = -1;
    private static final String  CURRENT_SUBJECT_INDEX = "currentSubjectIndex";
    boolean                      isAssignmentModeOffline     = false;
    private TextView takeAssignmentOrShowResultsButtom;
    private AssignmentInstructionsDialogFragment dialogFragment;
    private AssignmentDownloader assignmentDownloader;
    private TextView tvAssignmentPreStartTheAssignment,view_analytics;
    private ContentDataManager cDataManager;
    private Content assignment;
    private int assignmentContentId;
    private SessionManager session;
    private AnalyticsDataManager aDataManager;
    private String entity_id,entity_type,targetUserId, assignmentName;
    private String titlee;

    ArrayList<HashMap<String, String>> assignmentaddlist;
    private JSONObject savedata;
    private JSONArray questions;

    private JSONObject quesobject;

    private JSONArray correctanswer;
    private JSONObject quesanswer;
    private JSONArray answegiven;

    private JSONArray option;
//    private ListView lv;
    private AssignmentData assignmentData;
    private String questionid;

    private String status;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private RecyclerView recyclerView;
    private JSONObject info;
    private String subjectName;
    private String coursestype;
    private ProgressDialog loading;
    private ArrayList<String> subjectlist = new ArrayList<>();
    private ArrayAdapter<String> arrayapter;
    private Spinner subject_spinner;
    private String boardsubjectId;
    ArrayList<JSONArray> assignmentoption;
    private String subject_name;
    private JSONArray boared, filteredList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        GoogleAnalyticsUtils.setCustomDimensions(
                getIntent().getStringExtra(ConstantGlobal.ENTITY_ID),
                getIntent().getStringExtra(ConstantGlobal.NAME), ConstantGlobal.ASSIGNMENT);
        setContentView(R.layout.activity_assignment_pre_attempt_page);
        session = SessionManager.getInstance(getApplicationContext());
        cDataManager = new ContentDataManager(getApplicationContext());
        analyticsDataManager = new AnalyticsDataManager(this);
        if (savedInstanceState != null) {
            currentSubjectIndex = savedInstanceState.getInt(CURRENT_SUBJECT_INDEX);
        }
        assignmentoption=new ArrayList<>();
        assignmentaddlist=new ArrayList<>();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      //  getSupportActionBar().setIcon(R.drawable.ic_launcher);
        getSupportActionBar().setDisplayShowCustomEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        contentId = getIntent().getIntExtra(ConstantGlobal.CONTENT_ID, -1);
        if (contentId == -1) {
            Toast.makeText(getApplicationContext(), "There is some problem in opening the assignment.",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
         assignmentName = getIntent().getStringExtra(ConstantGlobal.ASSIGNMENT_NAME);
        loadAssignmentMetadata();
        boolean showPostAssignmentPages = getIntent().getBooleanExtra("showPostAssignmentPages", false);

        Log.d(TAG, "Show Post assignment pages::  " + showPostAssignmentPages);
        if (showPostAssignmentPages) {
            // TODO for now assuming this call comes only after end assignment ie the
            // user has
            // already attempted the assignment
            loadPostAssignmentPageContent(false);
            findViewById(R.id.assignment_pre_bottom_layout).setVisibility(View.GONE);
        } else {
            loadPreAssignmentPageContent(false);
            findViewById(R.id.assignment_pre_bottom_layout).setVisibility(View.VISIBLE);
        }
//        loadPreAssignmentPageContent(false);
//        findViewById(R.id.assignment_pre_bottom_layout).setVisibility(View.VISIBLE);


        if (assignment != null) {
            getSupportActionBar().setTitle(assignment.name);
        }
    }
    
    @Override
    public void onBackPressed() {

        if (getSupportFragmentManager().getBackStackEntryCount() != 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
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
    private AssignmentExtendedInfo loadAssignmentMetadata() {

        assignment = cDataManager.getContent(contentId);
        assignmentInfo = assignment == null ? null : (AssignmentExtendedInfo) assignment.toContentExtendedInfo();

        if (assignmentInfo != null) {
            AssignmentAnalytics analytics = analyticsDataManager.getAssignmentAnalytics(
                    session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID),
                    session.getSessionStringValue(ConstantGlobal.USER_ID), assignment.id, assignment.type);

            assignmentInfo.attempteState = analytics != null ? (Long.valueOf(analytics.endTime) > 0 ? AttemptState.ATTEMPTED
                    : AttemptState.INPROGRESS)
                    : AttemptState.UNATTEMPTED;
            Log.e(TAG, " AssignmentAnalytics :" + analytics);
        }
        return assignmentInfo;
    }

    public void loadPreAssignmentPageContent(boolean addToBackStack) {

        Bundle args = new Bundle();
        args.putInt(CURRENT_SUBJECT_INDEX, currentSubjectIndex);
        args.putSerializable(ConstantGlobal.INFO, assignmentInfo);
        Fragment fragment = new AssignmentPreAttemptPageFragment();
        fragment.setArguments(args);
        addOrReplaceFragment(fragment, "PRE_ASSIGNMENT_PAGE", addToBackStack);
    }

    private void addOrReplaceFragment(Fragment fragment, String fragmentTag, boolean addToBackStack) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        if (addToBackStack) {
            ft.replace(R.id.assignment_pre_attempt_frame_layout, fragment);
            ft.addToBackStack(fragmentTag);
        } else {
            ft.add(R.id.assignment_pre_attempt_frame_layout, fragment);
        }

        ft.commit();
    }
    public Content getBasicAssignmentInfo() {

        return assignment;
    }
    public AssignmentExtendedInfo getExtendedAssignmentInfo() {

        return assignmentInfo;
    }


    public void setCurrentSubjectIndex(int newIndex) {

        currentSubjectIndex = newIndex;
    }
    
    private void startAssignment() {

        if (StringUtils.isNotEmpty(getIntent().getStringExtra(ConstantGlobal.MODULE_ID))) {
            String moduleId = getIntent().getStringExtra(ConstantGlobal.MODULE_ID);
             entityId = getIntent().getStringExtra(ConstantGlobal.ENTITY_ID);
            try {
                (new ModuleStatusDataManager(this)).updateModuleEntryStatus(
                        session.getSessionStringValue(ConstantGlobal.USER_ID),
                        session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID), moduleId, entityId,
                        "ASSIGNMENT");
            } catch (Exception e) {
                Log.d(TAG, "Some error occured in updating assignment of id: " + entityId + " Error: "
                        + e.getMessage());
            }
        }
        int attempts =  session.getAssignmentAttempts(assignment.name);
        session.updateAssignmentAttempts(assignment.name,attempts+1);
        Log.e("attempts",""+session.getAssignmentAttempts(assignment.name));
   /*     Intent intent = new Intent(this, TakeAssignment.class);



        intent.putExtra(ConstantGlobal.ASSIGNMENT_NAME,
                assignmentName);
        intent.putExtra(ConstantGlobal.ENTITY_ID, assignment.id);
        intent.putExtra(ConstantGlobal.ENTITY_TYPE, assignment.type);
        intent.putExtra(ConstantGlobal.TARGET_USERID, targetUserId);
        intent.putExtra(ConstantGlobal.CONTENT_ID, assignment._id);

        Log.e("testid","...."+assignment.id);
        Log.e("testtype","...."+assignment.type);
        startActivity(intent);
*/

    }

    @Override
    protected void onResume() {

        loadAssignmentMetadata();

        // TODO: this method need to be changed addedBy Shankar

        isAssignmentModeOffline = assignmentInfo != null && assignment.subType != null
                && "OFFLINE".equalsIgnoreCase(assignment.subType);
        takeAssignmentOrShowResultsButtom = (TextView) findViewById(R.id.assignment_pre_button);



        tvAssignmentPreStartTheAssignment = (TextView)findViewById(R.id.assignment_pre_start_the_assignment);
        tvAssignmentPreStartTheAssignment.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                    if (StringUtils.isNotEmpty(getIntent().getStringExtra(ConstantGlobal.MODULE_ID))) {
                        String moduleId = getIntent().getStringExtra(ConstantGlobal.MODULE_ID);
                        String entityId = getIntent().getStringExtra(ConstantGlobal.ENTITY_ID);
                        try {
                            (new ModuleStatusDataManager(AssignmentPreAttemptPageActivity.this)).updateModuleEntryStatus(
                                    session.getSessionStringValue(ConstantGlobal.USER_ID),
                                    session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID), moduleId, entityId,
                                    "ASSIGNMENT");
                        } catch (Exception e) {
                            Log.d(TAG, "Some error occured in updating test of id: " + entityId + " Error: "
                                    + e.getMessage());
                        }
                    }

//                if(tvAssignmentPreStartTheAssignment.getText().toString().trim().equalsIgnoreCase("Take Assignment"))
                if(NetUtils.isOnline(AssignmentPreAttemptPageActivity.this)) {
                    new questionlist().execute();        }
                else{
                    Toast.makeText(AssignmentPreAttemptPageActivity.this, R.string.no_internet_msg, Toast.LENGTH_SHORT).show();
                }


//                startAssignment();
            }
        });
        //resetAllThingsInBottomLayout();
        takeAssignmentOrShowResultsButtom.setVisibility(View.GONE);
  /*      if (assignmentInfo.attempteState == AttemptState.ATTEMPTED) {
            takeAssignmentOrShowResultsButtom.setText(R.string.view_assignment_results);
            tvAssignmentPreStartTheAssignment.setText(R.string.retake_assignment);
            tvAssignmentPreStartTheAssignment.setVisibility(View.VISIBLE);
        } else if (isAssignmentModeOffline) {
            takeAssignmentOrShowResultsButtom.setText(R.string.download_assignment_results);
        } else {
            setRedTakeAssignmentButton(assignmentInfo.attempteState);
        }*/

        takeAssignmentOrShowResultsButtom.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (assignmentInfo.attempteState == AttemptState.ATTEMPTED) {
                    // if the assignmentResultVisibility==HIDDEN then show the
                    findViewById(R.id.assignment_pre_bottom_layout).setVisibility(View.GONE);
                    loadPostAssignmentPageContent(true);
                } else {
                    if (isAssignmentModeOffline && !SessionManager.isOnline()) {
                        Toast.makeText(getBaseContext(), getString(R.string.no_internet_msg),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    showPrepareAssignmentPopup();
                }
            }
        });
        super.onResume();
    }



    private void resetAllThingsInBottomLayout() {
        int paddingSide = getResources().getDimensionPixelSize(R.dimen.test_pre_common_side_margin);
        int paddingBottom = getResources().getDimensionPixelSize(
                R.dimen.test_pre_bottom_layout_padding_bottom);
        findViewById(R.id.assignment_pre_bottom_layout).setPadding(paddingSide, 0, paddingSide,
                paddingBottom);
        takeAssignmentOrShowResultsButtom.setVisibility(View.GONE);
        takeAssignmentOrShowResultsButtom.setBackgroundResource(R.color.green);
        tvAssignmentPreStartTheAssignment.setVisibility(View.GONE);
        findViewById(R.id.assignment_pre_download_progress_bar_holder).setVisibility(View.GONE);
        findViewById(R.id.assignment_pre_downloading_offline_assignment_results).setVisibility(View.GONE);
    }

    private void setRedTakeAssignmentButton(AttemptState attemptState) {

        int paddingSide = getResources().getDimensionPixelSize(R.dimen.test_pre_common_side_margin);
        int paddingBottom = getResources().getDimensionPixelSize(
                R.dimen.test_pre_bottom_layout_padding_bottom);
        findViewById(R.id.assignment_pre_bottom_layout).setPadding(paddingSide, 0, paddingSide,
                paddingBottom);
/*        takeAssignmentOrShowResultsButtom.setVisibility(View.VISIBLE);
        takeAssignmentOrShowResultsButtom.setBackgroundResource(R.color.darkred);
        takeAssignmentOrShowResultsButtom.setText(getString(attemptState == AttemptState.INPROGRESS ? R.string.resume_this_assignment
                        : R.string.take_this_assignment));*/
    }
    private AssignmentServerToLocalAnalyticsSyncProcessor syncer;

    private void showPrepareAssignmentPopup() {

        syncer = new AssignmentServerToLocalAnalyticsSyncProcessor(this, assignment, this, !isAssignmentModeOffline);
        resetAllThingsInBottomLayout();
        if (isAssignmentModeOffline) {
            findViewById(R.id.assignment_pre_downloading_offline_assignment_results)
                    .setVisibility(View.VISIBLE);
            syncer.executeTask(false);
        } else {
            // this assignment is online assignment

//            dialogFragment = new AssignmentInstructionsDialogFragment();
//            Bundle args = new Bundle();
//             TODO this is a wrong way of doing, but had no choice. I am in a
//             hurry
//            args.putInt("heightOfButton", findViewById(R.id.assignment_pre_bottom_layout).getHeight());
//            dialogFragment.setArguments(args);
//            dialogFragment.show(getSupportFragmentManager(), null);
//
            Log.d(TAG, "checking if user has already attempted the assignment");
            if (!SessionManager.isOnline()) {
                if (!assignment.downloaded) {
                    setRedTakeAssignmentButton(assignmentInfo.attempteState);
                    Log.e(TAG, "assignment is not downloaded and internet connection is not available");
                    Toast.makeText(getBaseContext(), getString(R.string.no_internet_msg),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                setGreenStartAssignmentButton();
            } else {

                // if online, then check if the assignment is already being attempted
                resetAllThingsInBottomLayout();
                findViewById(R.id.assignment_pre_download_progress_bar_holder)
                        .setVisibility(View.VISIBLE);
                syncer.executeTask(false);
            }

        }
        // TODO : check for analytics
    }
    @Override
    public void onTaskPostExecute(boolean success, JSONObject result) {

        resetAllThingsInBottomLayout();
        if (isAssignmentModeOffline) {
            takeAssignmentOrShowResultsButtom.setVisibility(View.VISIBLE);
        }

        if (success) {
            if (!isAssignmentModeOffline) {
                Toast.makeText(this, getString(R.string.already_attempted), Toast.LENGTH_LONG)
                        .show();
            }
            onResume();
            return;
        }

        if (assignment != null && assignment.downloaded) {
            setGreenStartAssignmentButton();
        } else if (!isAssignmentModeOffline && !success) {
            // online assignment and user has not attempted the assignment, also the assignment is
            // not downloaded,
            // so 1st download the assignment questions and start assignment on post
            // completion
            findViewById(R.id.assignment_pre_download_progress_bar_holder).setVisibility(View.VISIBLE);
            assignmentDownloader = new AssignmentDownloader(assignment, this, new OnAssignmentQuestionDownload());
            assignmentDownloader.executeTask(false);
        }

    }

    @Override
    public void onTaskStart(JSONObject result) {

    }

    private class OnAssignmentQuestionDownload implements IDownloadCompleteProcessor<JSONObject> {

        @Override
        public JSONObject onComplete(JSONObject result, boolean completed) {

            Log.d(TAG, "onComplete method is called for assignment download");
            resetAllThingsInBottomLayout();
            if (completed) {
                assignment.downloaded = true;
                ContentValues tobeUpdatedValues = new ContentValues();
                tobeUpdatedValues.put(ConstantGlobal.DOWNLOADED, 1);
                cDataManager.updateContent(assignment._id, tobeUpdatedValues);
                setGreenStartAssignmentButton();
            } else {
                setRedTakeAssignmentButton(assignmentInfo.attempteState);
                Toast.makeText(getBaseContext(), "Can not download assignment questions",
                        Toast.LENGTH_SHORT).show();
            }

            return result;
        }

    }

    private void setGreenStartAssignmentButton() {

        int paddingSide = getResources().getDimensionPixelSize(R.dimen.test_pre_common_side_margin);
        int paddingBottom = getResources().getDimensionPixelSize(
                R.dimen.test_pre_bottom_layout_padding_bottom);
        findViewById(R.id.assignment_pre_bottom_layout).setPadding(paddingSide, 0, paddingSide,
                paddingBottom);
        tvAssignmentPreStartTheAssignment.setVisibility(View.VISIBLE);
    }
    private void loadPostAssignmentPageContent(boolean addToBackStack) {

        if (assignment!= null) {
            getSupportActionBar().setTitle(assignment.name);
        }
        Bundle args = new Bundle();
        args.putString(ConstantGlobal.ENTITY_ID, assignment.id);
        args.putString(ConstantGlobal.ASSIGNMENT_NAME, assignment.name);
        args.putString(ConstantGlobal.ENTITY_TYPE, assignment.type);
        args.putSerializable(ConstantGlobal.ASSIGNMENT_METADATA, assignmentInfo);

        AssignmentPostAttemptPageFragment fragment = new AssignmentPostAttemptPageFragment();
        fragment.setArguments(args);
        addOrReplaceFragment(fragment, "AssignmentPostAttemptPageFragment", addToBackStack);
    }

    class  questionlist extends AsyncTask<Object, Object, JSONObject> {
        private String url;
        private ProgressDialog loading;
        private JSONArray boared;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading = ProgressDialog.show(AssignmentPreAttemptPageActivity.this, "Question Loading", "Please wait...",true,true);
            loading.show();
        }

        @Override
        protected JSONObject doInBackground(Object... params) {


            JSONObject jsonRes = null;
            this.url = session.getApiUrl("getUserEntityQuestionAttempts");

            Log.e("urldata","........"+url);
            Map<String, Object> httpParams = new HashMap<String, Object>();
            httpParams.put("entity.id", assignment.id);
            httpParams.put("entity.type", assignment.type);
            httpParams.put("targetUserId",assignment.targetIds);

            session.addSessionParams(httpParams);
            try {
                jsonRes = VedantuWebUtils.getJSONData(url, VedantuWebUtils.GET_REQ, httpParams);


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
                    Log.e("questionlis", "......" + jsonObject);

                    JSONObject result = jsonObject.getJSONObject("result");
                    if (result != null) {

                        assignmentaddlist.clear();


                        boared = result.getJSONArray("boards");
//                        subjectlist.add("All");
                        for (int i = 0; i < boared.length(); i++) {

                            savedata = boared.getJSONObject(i);
                            subject_name = savedata.getString("name");
                            boardsubjectId = savedata.getString("id");
                            subjectlist.add(subject_name);

                        }

                        filterList(boared);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void filterList(JSONArray boared){


        try {

            for (int i = 0; i < boared.length(); i++) {

                savedata = boared.getJSONObject(i);
                subject_name=savedata.getString("name");
                boardsubjectId=savedata.getString("id");
                questions = savedata.getJSONArray("questions");
                assignmentData = new AssignmentData();

                for (int j = 0; j < questions.length(); j++) {

                    Log.e("length()", "......" + questions.length());
                    quesobject = questions.getJSONObject(j);
                    Log.e("questions", "" + quesobject);
                    String ss_status;
                    status=quesobject.getString("status");
                    info = quesobject.getJSONObject("info");
                    assignmentData.solutions = info.getString("solutions");
                    assignmentData.type = info.getString("type");
                    assignmentData.content = info.getString("content");
                    questionid = info.getString("id");
                    quesanswer = quesobject.getJSONObject("answer");
                    Log.e("quesanswer", "........" + quesanswer);
                    correctanswer= quesanswer.getJSONArray("correctAnswer");
                    Log.e("correctanswer", "........" + correctanswer);
                    answegiven = quesanswer.optJSONArray("answerGiven");
                    Log.e("answerGiven","........"+answegiven);
                    ss_status = quesobject.getString("status");
                    Log.e("ss_status", "........" + ss_status);
                    option = info.optJSONArray("options");
                   assignmentoption.add(option);
                    Log.e("option", "........" + option);
                    String option_correctanswer = "";
                    try {
                        for (int l = 0; l< correctanswer.length(); l++) {

                            String str = correctanswer.getString(l);
                            if (l != correctanswer.length() - 1) {
                                option_correctanswer = option_correctanswer + str + ",";
                            } else {
                                option_correctanswer = option_correctanswer + str;
                            }
                        }

                    } catch (JSONException e) {
                        Log.e("JSON", "There was an error parsing the JSON", e);
                    }

                    String option_answerGiven  = "";
                    if (answegiven!=null) {

                        try {


                            for (int l = 0; l < answegiven.length(); l++) {

                                String str = answegiven.getString(l);
                                if (l != answegiven.length() - 1) {
                                    option_answerGiven = option_answerGiven + str + ",";
                                } else {
                                    option_answerGiven = option_answerGiven + str;
                                }


                            }


                        } catch (JSONException e) {
                            Log.e("JSON", "There was an error parsing the JSON", e);
                        }
                    }
                    HashMap<String, String> studentquestionlist = new HashMap<>();
                    studentquestionlist.put("solutions", assignmentData.solutions);
                    studentquestionlist.put("type", assignmentData.type);
                    studentquestionlist.put("content", assignmentData.content);
                    //studentquestionlist.put("options", optionString);
                    studentquestionlist.put("id", questionid);
                    studentquestionlist.put("status",ss_status);
                    studentquestionlist.put("correctAnswer",option_correctanswer);
                    studentquestionlist.put("subjectName",subjectName);
                    studentquestionlist.put("boardsubjectId",boardsubjectId);
                    studentquestionlist.put("answerGiven",option_answerGiven);
                    assignmentaddlist.add(studentquestionlist);
                    Log.e("takeassignmentlist","........."+assignmentaddlist.size());
                    Log.e("statuslist","............"+assignmentData.type.length());


                }

                Log.e("assignmentaddlist","........"+assignmentaddlist);
//                adapter = new AssignmentQuestionAdapter(AssignmentPreAttemptPageActivity.this, assignmentaddlist, assignmentoption,entity_id, entity_type);
//                lv.setAdapter(adapter);


                        Intent intent=new Intent(AssignmentPreAttemptPageActivity.this,StudentExamGridActivity.class);

                        String optionsString = null;
                        if (option!=null ) {
                            for (int j = 0; j < option.length(); j++) {
                                try {
                                    if (j == 0)
                                        optionsString = option.getString(j);
                                    else
                                        optionsString += "#" + option.getString(j);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

         /*               intent.putExtra("solutions", assignmentData.solutions);
                        intent.putExtra("type", assignmentData.type);
                        intent.putExtra("content", assignmentData.content);
                        intent.putExtra("options", optionsString);
                        intent.putExtra("id",assignmentData.id);
*/
                        intent.putExtra(ConstantGlobal.ASSIGNMENT_NAME,
                                assignmentName);
                        intent.putExtra(ConstantGlobal.ENTITY_ID, assignment.id);
                        intent.putExtra(ConstantGlobal.ENTITY_TYPE, assignment.type);
                intent.putExtra(ConstantGlobal.TARGET_USERID, assignment.targetIds);
//                        intent.putExtra("position", (int)v.getTag());

                        Log.e("entid","............."+entityId);
                        Log.e("entidtype","............."+entityType);
                        Log.e("options in intent","............."+ optionsString);


              //  tvAssignmentPreStartTheAssignment.setText("Take Assignment");
               // tvAssignmentPreStartTheAssignment.setBackgroundColor(getResources().getColor(R.color.grey));

//                        activity.startActivityForResult(intent,101, null);
                startActivity(intent);

//            }); }


            }
//            adapter.notifyDataSetChanged();
            //refreshLayout.setRefreshing(false);
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

}
