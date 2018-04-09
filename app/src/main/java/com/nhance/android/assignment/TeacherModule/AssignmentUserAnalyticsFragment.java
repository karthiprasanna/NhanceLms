package com.nhance.android.assignment.TeacherModule;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nhance.android.R;
import com.nhance.android.assignment.db.models.analytics.AssignmentAnalytics;
import com.nhance.android.assignment.db.models.analytics.AssignmentBoardAnalytics;
import com.nhance.android.assignment.pojo.content.infos.AssignmentExtendedInfo;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.AnalyticsDataManager;
import com.nhance.android.enums.BoardType;
import com.nhance.android.fragments.AbstractVedantuFragment;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.utils.GoogleAnalyticsUtils;
import com.nhance.android.utils.VedantuWebUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by karthi on 1/12/17.
 */

public class AssignmentUserAnalyticsFragment extends AbstractVedantuFragment {

    private String entityId;
    private String entityType;
    private SessionManager session;
    private AssignmentAnalytics analytics;
    private AnalyticsDataManager analyticsDataManager;
    private ArrayList<AssignmentBoardAnalytics> coursesAnalytics;
    int                                   mActionBarHeight;
    private boolean        showCache               = true;
    private TextView ass_firstnemre,ass_name;
    private TextView ass_profile,ass_correct,ass_incorrect;
    private String user_firstname;
    private String user_profile;
    private String ass_correctanswer;
    private String ass_incorrectanswer;
    private ImageView ass_thumbnail;
    private String user_thumbnail;
    private String ass_username;
    private int progressStatus=0;
    private ProgressBar pb_drawable;
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        GoogleAnalyticsUtils.setScreenName(ConstantGlobal.GA_SCREEN_ASSIGNMENT_USER_ANALYTICS);
        Bundle bundle = getArguments();
        session = SessionManager.getInstance(getActivity());

        analyticsDataManager = new AnalyticsDataManager(getActivity());
        analytics = analyticsDataManager.getAssignmentAnalytics(
                session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID),
                session.getSessionStringValue(ConstantGlobal.USER_ID),
                bundle.getString(ConstantGlobal.ENTITY_ID),
                bundle.getString(ConstantGlobal.ENTITY_TYPE));
        if (analytics == null) {
            return;
        }
        coursesAnalytics = analyticsDataManager.getAssignmentBoardAnalytics(analytics.orgKeyId,
                analytics.userId, analytics.entityId, analytics.entityType, null, BoardType.COURSE);
    }


    @Override
    public View
    onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.assignment_user_analytics_fragment, container, false);
        mActionBarHeight = getActionBarHeight();
        pb_drawable = (ProgressBar) view.findViewById(R.id.pb_drawable);
new userDetails().execute();
        if (analytics == null) {
            return null;
        }

        return view;
    }


    private int getActionBarHeight() {

        float actionBarHeight = 0;
        TypedValue tv = new TypedValue();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources()
                        .getDisplayMetrics());
        } /* TODO for testing commented this line Satya
        else if (getActivity().getTheme().resolveAttribute(
                com.actionbarsherlock.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources()
                    .getDisplayMetrics());
        }*/
        return (int) actionBarHeight;
    }


    class userDetails extends AsyncTask<Object, Object, JSONObject> {
        private String url;
        private AssignmentExtendedInfo assignmentInfo;

        @Override
        protected JSONObject doInBackground(Object... strings) {
            JSONObject jsonRes = null;
            this.url = session.getApiUrl("getUserEntityMeasures");



            Map<String, Object> httpParams = new HashMap<String, Object>();
            httpParams.put("entity.id", analytics.entityId);
            httpParams.put("entity.type", analytics.entityType);
            httpParams.put("targetUserId",analytics.userId);

            session.addSessionParams(httpParams);

            try {
                jsonRes = VedantuWebUtils.getJSONData(url, VedantuWebUtils.GET_REQ, httpParams);
                Log.d("jsonres","jsonres"+jsonRes);



                for (int j=0;j<=jsonRes.length();j++) {
                    JSONObject result = jsonRes.getJSONObject("result");
                    JSONObject measure = result.getJSONObject("measures");
                    JSONObject user = result.getJSONObject("user");
                    JSONObject mappings = user.getJSONObject("mappings");
                    JSONArray programs = mappings.getJSONArray("programs");


                    for (int i=0;i<programs.length();i++){
                        JSONObject jsonObject=programs.getJSONObject(i);
                         ass_username=jsonObject.getString("name");




                    }



                     user_firstname=user.getString("firstName");
                     user_profile=user.getString("profile");
                    user_thumbnail=user.getString("thumbnail");

                     ass_correctanswer=measure.getString("correct");
                     ass_incorrectanswer=measure.getString("incorrect");


                }


            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);

            ass_firstnemre =(TextView)getActivity().findViewById(R.id.ass_firstname);
            ass_firstnemre.setText(user_firstname);
            ass_profile=(TextView)getActivity().findViewById(R.id.ass_profile);
            ass_profile.setText(user_profile);
            ass_name=(TextView)getActivity().findViewById(R.id.ass_name);
            ass_name.setText(ass_username);
            ass_correct=(TextView) getActivity().findViewById(R.id.ass_correct);
            ass_correct.setText(ass_correctanswer);
            ass_incorrect=(TextView) getActivity().findViewById(R.id.ass_incorrect);
            ass_incorrect.setText(ass_incorrectanswer);






            float crr_question = Float.parseFloat(ass_correctanswer);

            Log.e("correct","correct"+crr_question);


            float total_question = crr_question+ Float.parseFloat(ass_incorrectanswer);


            Log.e("total","total"+total_question);

            float per = crr_question/total_question;

       final float percent = per*100;



            Log.e("percent","percent"+percent);



            progressStatus = 0;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    while(progressStatus < percent){
                        progressStatus +=1;

                        try{
                            Thread.sleep(20);
                        }catch(InterruptedException e){
                            e.printStackTrace();
                        }


                                pb_drawable.setProgress(progressStatus);

                    }
                }
            }).start();



        }
    }


}
