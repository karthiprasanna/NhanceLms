package com.nhance.android.assignment.TeacherModule;

/**
 * Created by KARTHI on 1/4/17.
 */

import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.nhance.android.R;
import com.nhance.android.assignment.StudentPerformance.NetUtils;
import com.nhance.android.async.tasks.CachedWebDataFetcherTask;
import com.nhance.android.async.tasks.ICachedTaskProcessor;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.customviews.BarGraphBean;
import com.nhance.android.fragments.AbstractVedantuFragment;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.utils.GoogleAnalyticsUtils;
import com.nhance.android.utils.VedantuWebUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AssignmentStudentAnalaticsFragment extends AbstractVedantuFragment implements
        OnClickListener {

    SessionManager session;
    final int              BUCKET_COUNT            = 5;
    final private String TAG                     = "AssignmentStudentAnalaticsFragment";
    int                    mActionBarHeight;
    private String entityId;
    private String entityType;
    private AssignmentInfo       assignmentInfo;
    private View parentView;
    private BarGraphBean[] marksDistributionValues = null;
    private boolean        showCache               = true;
    private boolean        isDestroyed             = false;
    private String entity_id,entity_type,targetUserId;
    private String correct=null;
   private String left=null;
    private String incorrect=null;
    private String prg_name = null;
    private String username = null;
    private String lastatemmpted = null;
    private String totalhits = null;
    private String thumbnail=null;
    private String id=null;
   private JSONObject result = null;
    private JSONObject list_data = null;

    ArrayList<HashMap<String, String>> contactList;
    private ListView lv;
    private String dateText;
    private SwipeRefreshLayout refreshLayout;
    private StudentAnalayticsAdapter adapter;
    private ProgressDialog loading;
    private String assignmentName;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GoogleAnalyticsUtils.setScreenName(ConstantGlobal.GA_SCREEN_ASSIGNMENT_STUDENT_ANALYTICS);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        parentView = inflater.inflate(R.layout.fragment_assignment_student_performance_list, container, false);
        // mActionBarHeight = getActionBarHeight();
        fillAssignmentContent();
        Log.d(TAG, "onCreateView view is called ");

       Bundle values = getArguments();
        if (values == null) {
            return null;
        }

       entity_id = values.getString(ConstantGlobal.ENTITY_ID);
        entity_type = values.getString(ConstantGlobal.ENTITY_TYPE);
        targetUserId=values.getString(ConstantGlobal.TARGET_USERID);
       assignmentName = values.getString(ConstantGlobal.ASSIGNMENT_NAME);

Log.e("assignmentName","........."+assignmentName);
        Log.e("assignmentName","........."+entity_type);
        Log.e("assignmentName","........."+targetUserId);
        Log.e("assignmentName","........."+entity_id);





        contactList = new ArrayList<>();


        if(NetUtils.isOnline(getActivity()))
            new studentanalytics().execute();
        else{
            Toast.makeText(getActivity(), R.string.no_internet_msg, Toast.LENGTH_SHORT).show();
        }





        return parentView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        lv = (ListView) getActivity().findViewById(R.id.ass_list);
        refreshLayout=(SwipeRefreshLayout)getActivity().findViewById(R.id.swipelayout);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if(NetUtils.isOnline(getActivity()))
                    new studentanalytics().execute();
                else{
                    Toast.makeText(getActivity(), R.string.no_internet_msg, Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    class studentanalytics extends AsyncTask<Object, Object, JSONObject> {
        private String url;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading = ProgressDialog.show(getActivity(), "Question Loading", "Please wait...",true,true);
            loading.show();
        }

        @Override
        protected JSONObject doInBackground(Object... strings) {

            JSONObject jsonRes = null;
            this.url = session.getApiUrl("getEntityMeasures");



            Map<String, Object> httpParams = new HashMap<String, Object>();
            httpParams.put("id", entity_id);
            httpParams.put("entity.type",entity_type);
           httpParams.put("entity.id",entity_id);


            session.addSessionParams(httpParams);




            try {
                jsonRes = VedantuWebUtils.getJSONData(url, VedantuWebUtils.GET_REQ, httpParams);

                 Log.e("analyticd","......."+jsonRes);


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


        if (result != null) {


            contactList.clear();


            totalhits = result.getString("totalHits");


            JSONArray list = result.getJSONArray("list");
            for (int i = 0; i < list.length(); i++) {
                list_data = list.getJSONObject(i);

                lastatemmpted = list_data.getString("lastAttempted");
                JSONObject measures = list_data.getJSONObject("measures");
                correct = measures.getString("correct");
                incorrect = measures.getString("incorrect");
                left = measures.getString("left");
                JSONObject users = list_data.getJSONObject("user");
                JSONObject mappings = users.getJSONObject("mappings");
                JSONArray programs = mappings.getJSONArray("programs");
                JSONObject prgm_details = programs.getJSONObject(0);
                prg_name = prgm_details.getString("name");
                username = users.getString("firstName");
                thumbnail = users.getString("thumbnail");

                Log.e("thumbnail", "......" + thumbnail);
                id = users.getString("userId");

                long val = Long.parseLong(lastatemmpted);
                Date date = new Date(val);
                SimpleDateFormat df2 = new SimpleDateFormat("dd/MM/yy");
                dateText = df2.format(date);
                System.out.println(dateText);


                HashMap<String, String> contact = new HashMap<>();

                // adding each child node to HashMap key => value
                contact.put("firstName", username);
                contact.put("name", prg_name);
                contact.put("correct", correct);
                contact.put("incorrect", incorrect);
                contact.put("lastAttempted", dateText);
                contact.put("thumbnail", thumbnail);
                contact.put("userId", id);
                contact.put("left", left);

                // adding contact to contact list
                contactList.add(contact);
                Log.e("left", "............" + left);

            }
        }

    } catch (JSONException e) {
        e.printStackTrace();
    }
}

             adapter = new StudentAnalayticsAdapter(getActivity(), contactList,entity_id,entity_type,targetUserId,assignmentName);

            lv.setAdapter(adapter);



adapter.notifyDataSetChanged();
            refreshLayout.setRefreshing(false);
        }


    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.test_post_attempt_rank_list_page, menu);
        progressMenuItem = menu.findItem(R.id.action_refresh);
     //   showRefreshButton();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_refresh) {
            if (!SessionManager.isOnline()) {
                Toast.makeText(getActivity(), R.string.no_internet_msg, Toast.LENGTH_LONG).show();
            } else {
                showCache = false;
                fillAssignmentContent();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void fillAssignmentContent() {

        Bundle bundle = getArguments();
        session = SessionManager.getInstance(getActivity());
        entityId = bundle.getString(ConstantGlobal.ENTITY_ID);
        entityType = bundle.getString(ConstantGlobal.ENTITY_TYPE);
        if (TextUtils.isEmpty(entityId)) {
            return;
        }
        showProgressBar();
        String cacheKey = entityType + "/" + entityId;
        Log.d(TAG, "GET ASSIGNMENT INFO ========" + entityId);
        CachedWebDataFetcherTask<AssignmentInfo> cachedWebDataFetcherTask = new CachedWebDataFetcherTask<AssignmentInfo>(
                session, null, CachedWebDataFetcherTask.ReqAction.GET_ASSIGNMENT_DETAILS,
                new AssignmentInfoTaskProcessor(), showCache, cacheKey, AssignmentInfo.class);

        cachedWebDataFetcherTask.addExtraParams("id", entityId);
        cachedWebDataFetcherTask.executeTask(false);
    }

    private final class AssignmentInfoTaskProcessor implements ICachedTaskProcessor<AssignmentInfo> {

        @Override
        public void onResultDataFromCache(AssignmentInfo data) {

            if (isDestroyed) {
                return;
            }
            /*if (data != null) {
                drawAssignmentInfo(data);
            }*/ else {
                Toast.makeText(getActivity().getApplicationContext(),
                        R.string.downloading_assignment_result_msg, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onTaskPostExecute(boolean success, AssignmentInfo result) {

            if (isDestroyed) {
                return;
            }
            Log.d(TAG, "ASSIGNMENT INFO RESULTS ======== success ==" + success + ", RESULT === "
                    + result);
//            if (success && result != null) {
//              //  boolean proceed = drawAssignmentInfo(result);
//                if (proceed) {
//                    //getHighestScorer();
//                }
//            } else {
//                onFetchFailure(false);
//            }
        }

    }

//    private boolean drawAssignmentInfo(AssignmentInfo resp) {
//
//        assignmentInfo = resp;
//        long attempts = assignmentInfo.attempts;
//        Log.d(TAG,"AASSIGNMENT INFO == "+assignmentInfo+", attempts = "+assignmentInfo.attempts);
//        if (attempts <= 0) {
//            onFetchFailure(true);
//            return false;
//        }
//        if (m_cObjNhanceBaseActivity != null) {
//            m_cObjNhanceBaseActivity.getSupportActionBar().setTitle(assignmentInfo.name);
//        }
//
//        float avgScore = assignmentInfo.avgMarks;
//        ViewUtils.setTextViewValue(parentView, R.id.assignment_avg_score_value,
//                AnalyticsUtil.getStringDisplayValue(avgScore));
//
//        int totalMarks = assignmentInfo.totalMarks;
//        ViewUtils.setTextViewValue(parentView, R.id.assignment_marks_total,
//                AnalyticsUtil.getStringDisplayValue(totalMarks), null,
//                getResources().getString(R.string.seperator), FontTypes.ROBOTO_LIGHT);
//
//        float percentageScore = totalMarks == 0 ? 0 : (avgScore * 100 / totalMarks);
//        int percentageColorCode = AnalyticsUtil.getPercentageColorCode(percentageScore);
//        AssignmentPieChartView pieChart = (AssignmentPieChartView) parentView
//                .findViewById(R.id.assignment_avg_score_chart_view);
//        pieChart.setColorAndValues(new int[] { getResources().getColor(percentageColorCode) },
//                new float[] { percentageScore });
//        pieChart.postInvalidate();
//
//        ViewUtils.setTextViewValue(parentView, R.id.assignment_avg_score_percentage_value,
//                AnalyticsUtil.getStringDisplayValue(percentageScore,2),
//                getResources().getString(R.string.percentage_indicator));
//
//        long avgTimeTaken = assignmentInfo.avgTimeTaken;
//        int durationInSeconds = (int) avgTimeTaken / 1000;
//        String durationString = LocalManager.getDurationMinString(durationInSeconds);
//        ViewUtils.setTextViewValue(parentView, R.id.assignment_avg_time_taken_value, durationString,
//                null, FontTypes.ROBOTO_LIGHT);
//        ViewUtils.setTextViewValue(parentView, R.id.assignment_text_mins,
//                getResources().getString(R.string.mins), null, FontTypes.ROBOTO_LIGHT);
//
//        ViewUtils.setTextViewValue(parentView, R.id.assignment_attempts_value, String.valueOf(attempts),
//                null, FontTypes.ROBOTO_LIGHT);
//        return true;
//    }

//    private void getHighestScorer() {
//
//        showProgressBar();
//        String cacheKey = "UR/" + entityType + "/" + entityId;
//        CachedWebDataFetcherTask<EntityRankList> cachedWebDataFetcherTask = new CachedWebDataFetcherTask<EntityRankList>(
//                session, null, CachedWebDataFetcherTask.ReqAction.GET_ENTITY_LEADER_BOARD,
//                new HighestScorerTaskProcessor(), showCache, cacheKey, EntityRankList.class);
//        cachedWebDataFetcherTask.addExtraParams("entity.id", entity_id);
//        cachedWebDataFetcherTask.addExtraParams("entity.type", entity_type);
//        cachedWebDataFetcherTask.addExtraParams("miniInfo", String.valueOf(true));
//        cachedWebDataFetcherTask.executeTask(false);
//
//
//
//        Log.e("cache","cache"+cachedWebDataFetcherTask);
//    }

//    private final class HighestScorerTaskProcessor implements ICachedTaskProcessor<EntityRankList> {
//
//        @Override
//        public void onResultDataFromCache(EntityRankList data) {
//
//            if (isDestroyed) {
//                return;
//            }
//            if (data != null) {
//                drawHighestScorer(data);
//            }
//        }
//
//        @Override
//        public void onTaskPostExecute(boolean success, EntityRankList result) {
//
//            if (isDestroyed) {
//                return;
//            }
//            Log.d(TAG, "HIGHEST SCORER RESULTS ======== " + result);
//            if (success && result != null) {
//                drawHighestScorer(result);
//                getMarksDistributionData();
//            } else {
//                onFetchFailure(false);
//            }
//        }
//    }

//    private void drawHighestScorer(EntityRankList resp) {
//
//        RelativeLayout holder = (RelativeLayout) parentView
//                .findViewById(R.id.assignment_highest_scorer_holder_layout);
//        if (resp != null && resp.list.size() > 0) {
//            try {
//                UserEntityRank highest = resp.list.get(0);
//                holder.setVisibility(View.VISIBLE);
//                UserInfoRes user = highest.user;
//
//                ImageView userThum = (ImageView) parentView
//                        .findViewById(R.id.assignment_highest_scorer_profile_pic);
//                Drawable defaultPic = getResources().getDrawable(R.drawable.default_profile_pic);
//                userThum.setImageDrawable(defaultPic);
//                if (user != null) {
//                    String thumbnail = user.thumbnail;
//                    LocalManager.downloadImage(thumbnail, userThum);
//
//                    String firstName = user.firstName;
//                    ViewUtils.setTextViewValue(parentView, R.id.assignment_higest_scorer_first_name,
//                            firstName);
//                    String lastName = user.lastName;
//                    ViewUtils.setTextViewValue(parentView, R.id.assignment_higest_scorer_last_name,
//                            lastName);
//                }
//                EntityMeasures measures = highest.measures;
//                int userScore = measures.score;
//                int totalMarks = assignmentInfo.totalMarks;
//                float percentageScore = totalMarks == 0 ? 0 : userScore * 100 / totalMarks;
//                ViewUtils.setTextViewValue(parentView, R.id.assignment_highest_score_value,
//                        getResources().getString(R.string.seperator), String.valueOf(totalMarks),
//                        String.valueOf(userScore), FontTypes.ROBOTO_LIGHT);
//                ViewUtils.setTextViewValue(parentView, R.id.assignment_highest_score_percent_value,
//                        String.valueOf(percentageScore),
//                        getResources().getString(R.string.percentage_indicator)
//                                + getResources().getString(R.string.bracketEnd), getResources()
//                                .getString(R.string.bracketStart), FontTypes.ROBOTO_LIGHT);
//            } catch (Exception e) {
//                Log.d(TAG, e.getMessage());
//                holder.setVisibility(View.GONE);
//            }
//        } else {
//            holder.setVisibility(View.GONE);
//        }
//    }

//    private void getMarksDistributionData() {
//
//        showProgressBar();
//        String cacheKey = "MD/" + entityType + "/" + entityId;
//        CachedWebDataFetcherTask<EntityMarkDistribution> cachedWebDataFetcherTask = new CachedWebDataFetcherTask<EntityMarkDistribution>(
//                session, null, CachedWebDataFetcherTask.ReqAction.GET_ENTITY_MARK_DISTRIBUTION,
//                new MarksDistributionTaskProcessor(), showCache, cacheKey,
//                EntityMarkDistribution.class);
//        cachedWebDataFetcherTask.addExtraParams("entity.id", entityId);
//        cachedWebDataFetcherTask.addExtraParams("entity.type", entityType);
//        cachedWebDataFetcherTask.addExtraParams("bucketCount", BUCKET_COUNT);
//        cachedWebDataFetcherTask.executeTask(false);
//    }

//    private final class MarksDistributionTaskProcessor implements
//            ICachedTaskProcessor<EntityMarkDistribution> {
//
//        @Override
//        public void onResultDataFromCache(EntityMarkDistribution data) {
//
//            if (isDestroyed) {
//                return;
//            }
//            if (data != null) {
//                drawMarksDistribution(data);
//            }
//        }
//
//        @Override
//        public void onTaskPostExecute(boolean success, EntityMarkDistribution result) {
//
//            if (isDestroyed) {
//                return;
//            }
//            Log.d(TAG, "MARKS DISTRIBUTION RESULTS ======== " + result);
//            if (success && result != null) {
//                drawMarksDistribution(result);
//                showRefreshButton();
//            } else {
//                onFetchFailure(false);
//            }
//        }
//    }

//    @SuppressLint("ShowToast")
//    private void onFetchFailure(boolean isAssignmentAttempted) {
//
//        showRefreshButton();
//        if (isAssignmentAttempted) {
//            Toast.makeText(session.getContext(), R.string.assignment_not_attempted_yet, Toast.LENGTH_LONG)
//                    .show();
//            getActivity().finish();
//        } else {
//            Toast.makeText(getActivity().getApplicationContext(), R.string.assignment_data_fetch_failed,
//                    Toast.LENGTH_LONG).show();
//        }
//    }

//    private void drawMarksDistribution(EntityMarkDistribution resp) {
//
//        long total = assignmentInfo.attempts;
//        total = total < 0 ? 1 : total;
//        BarGraphView barGraph = (BarGraphView) parentView
//                .findViewById(R.id.assignment_marks_distribution_bar_graph);
//        marksDistributionValues = new BarGraphBean[resp.list.size()];
//        for (int index = 0; index < resp.list.size() && index < 5; index++) {
//            MarkDistribution value = resp.list.get(index);
//            if (value == null) {
//                continue;
//            }
//            String title = value.from + " to " + value.to;
//            int percentage = (int) (value.count / total * 100);
//            marksDistributionValues[index] = new BarGraphBean(percentage, title);
//        }
//        barGraph.setValues(marksDistributionValues);
//    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);
        Log.i(TAG, "onConfigurationChanged called");
    }

    @Override
    public void onResume() {

        super.onResume();
        GoogleAnalyticsUtils.sendPageViewDataToGA();
    }

    @Override
    public void onDestroy() {

        isDestroyed = true;
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {

        // TODO Auto-generated method stub
    }
}
