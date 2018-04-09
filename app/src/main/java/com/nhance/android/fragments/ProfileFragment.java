package com.nhance.android.fragments;

import java.text.DecimalFormat;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nhance.android.activities.AppLandingPageActivity;
import com.nhance.android.activities.about.AboutActivity;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.AnalyticsDataManager;
import com.nhance.android.db.datamanagers.ContentDataManager;
import com.nhance.android.db.datamanagers.OrgDataManager;
import com.nhance.android.db.datamanagers.UserActivityDataManager;
import com.nhance.android.db.models.Organization;
import com.nhance.android.db.models.analytics.TestAvg;
import com.nhance.android.db.models.analytics.TestBoardAnalytics;
import com.nhance.android.enums.BoardType;
import com.nhance.android.enums.EntityType;
import com.nhance.android.library.utils.LibraryUtils;
import com.nhance.android.managers.LocalManager;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.pojos.LibraryContentRes;
import com.nhance.android.pojos.OrgMemberInfo;
import com.nhance.android.pojos.content.infos.StudyHistoryDetails;
import com.nhance.android.utils.FontUtils;
import com.nhance.android.utils.GoogleAnalyticsUtils;
import com.nhance.android.utils.ViewUtils;
import com.nhance.android.utils.FontUtils.FontTypes;
import com.nhance.android.R;

public class ProfileFragment extends AbstractVedantuFragment {

    private final String   TAG = "ProfileFragment";
    private View           fragmentRootView;
    private SessionManager session;
    private OrgMemberInfo  orgMemberInfo;
    private DecimalFormat decimalFormat = new DecimalFormat("#.##");

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        GoogleAnalyticsUtils.setScreenName(ConstantGlobal.GA_SCREEN_PROFILE);
        session = SessionManager.getInstance(m_cObjNhanceBaseActivity);
        Organization org = new OrgDataManager(m_cObjNhanceBaseActivity)
                .getOrganization(session.getSessionStringValue(ConstantGlobal.ORG_ID),
                        session.getSessionStringValue(ConstantGlobal.CMDS_URL));
        orgMemberInfo = session.getOrgMemberInfo();
        if (org == null || orgMemberInfo == null) {
            Toast.makeText(m_cObjNhanceBaseActivity, "Invalid session",
                    Toast.LENGTH_LONG).show();
            m_cObjNhanceBaseActivity.finish();
            session.logoutUser();
            return;
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // TODO Auto-generated method stub
        fragmentRootView = inflater.inflate(R.layout.fragment_profile, container, false);
        boolean isStudent = LibraryUtils._amIStudent(m_cObjNhanceBaseActivity);
        addOrgProfileInfo();
        if (isStudent) {
            addProfileAvgStats();
            addStudyHistory();
        } else {
            View studentSpecificView = fragmentRootView
                    .findViewById(R.id.student_profile_info_holder);
            View studentStudyHistoryView = fragmentRootView
                    .findViewById(R.id.student_profile_study_history_holder);
            if (studentSpecificView != null) {
                studentSpecificView.setVisibility(View.GONE);
            }
            if (studentStudyHistoryView != null) {
                studentStudyHistoryView.setVisibility(View.GONE);
            }
        }
        return fragmentRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        Activity appLandingPageActivity = getActivity();
        if (appLandingPageActivity instanceof AppLandingPageActivity) {
            AppLandingPageActivity appLandingPageActivityInstance = (AppLandingPageActivity) getActivity();
            appLandingPageActivityInstance.getNavigationDrawerLayout()
                    .findViewById(R.id.navigation_sync_button).setVisibility(View.GONE);
            appLandingPageActivityInstance.getNavigationDrawerLayout()
                    .findViewById(R.id.navigation_sync_in_progress).setVisibility(View.GONE);
        }

        super.onActivityCreated(savedInstanceState);
    }

    private void addOrgProfileInfo() {

        OrgMemberInfo orgMemberInfo = session.getOrgMemberInfo();
        List<String> progCenterSections = OrgDataManager
                .getCenterSectionString(orgMemberInfo, null);
        ImageView profilePic = (ImageView) fragmentRootView.findViewById(R.id.profile_page_image);
        if (!TextUtils.isEmpty(orgMemberInfo.thumbnail)) {
            LocalManager.downloadImage(orgMemberInfo.thumbnail, profilePic, true);
        }

        TextView firstNameView = (TextView) fragmentRootView.findViewById(R.id.profile_first_name);
        firstNameView.setText(StringUtils.upperCase(orgMemberInfo.firstName));

        TextView lastNameView = (TextView) fragmentRootView.findViewById(R.id.profile_last_name);
        String lastName = orgMemberInfo.lastName;
        if (StringUtils.isNotEmpty(lastName)) {
            lastNameView.setText(StringUtils.upperCase(lastName));
            FontUtils.setTypeface(lastNameView, FontTypes.ROBOTO_LIGHT);
        } else {
            lastNameView.setVisibility(View.GONE);
        }

        LinearLayout progsHolder = (LinearLayout) fragmentRootView
                .findViewById(R.id.profile_user_programs);
        progsHolder.removeAllViews();
        if (!progCenterSections.isEmpty()) {
            for (int k = 0; k < progCenterSections.size(); k++) {
                TextView t = (TextView) ((LayoutInflater) m_cObjNhanceBaseActivity.getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE)).inflate(
                        R.layout.list_item_view_profile_program, progsHolder, false);
                t.setText(StringUtils.upperCase(progCenterSections.get(k)));
                progsHolder.addView(t);
            }
        }
    }

    private void addProfileAvgStats() {

        AnalyticsDataManager ad = new AnalyticsDataManager(m_cObjNhanceBaseActivity);
        List<TestBoardAnalytics> testCourseAnalytics = ad.getTestBoardAnalytics(
                session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID),
                session.getSessionStringValue(ConstantGlobal.USER_ID),
                AnalyticsDataManager.TEST_ID_OVERALL_SYSTEM, EntityType.TEST.name(), null,
                BoardType.COURSE);
        float totalQusCorrect = 0;
        float totalQusAttempted = 0;
        int totalScore = 0;
        int totalMarks = 0;
        float totalTimeTaken = 0;
        String strongCourseName = "";
        float cAvgMarks = 0;
        for (TestBoardAnalytics courseAnalytics : testCourseAnalytics) {
            totalQusAttempted += courseAnalytics.attempted;
            totalQusCorrect += courseAnalytics.correct;
            totalScore += courseAnalytics.score;
            totalMarks += courseAnalytics.totalMarks;
            totalTimeTaken += courseAnalytics.timeTaken;
            float ccAvgMarks = courseAnalytics.totalMarks < 1 ? 0 : (courseAnalytics.score * 100)
                    / courseAnalytics.totalMarks;
            if (ccAvgMarks > cAvgMarks) {
                strongCourseName = courseAnalytics.name;
            }
        }
        if (StringUtils.isEmpty(strongCourseName)) {
            strongCourseName = getResources().getString(R.string.no_strong_subject);
        }

        ((TextView) fragmentRootView.findViewById(R.id.profile_avg_marks)).setText(getAvgMark(ad) + "" + getResources().getString(R.string.percentage));

        ((TextView) fragmentRootView.findViewById(R.id.profile_avg_speed))
                .setText(getAvgTimeTakenString((int) totalQusAttempted, totalTimeTaken));

        float avgAccuracy = totalQusAttempted > 0 ? totalQusCorrect * 100 / totalQusAttempted : 0;
        float twoDigitsF = Float.valueOf(decimalFormat.format(avgAccuracy));
        ((TextView) fragmentRootView.findViewById(R.id.profile_avg_accuracy)).setText(String
                .valueOf(twoDigitsF) + getResources().getString(R.string.percentage));

        ((TextView) fragmentRootView.findViewById(R.id.profile_test_attempted)).setText(String
                .valueOf(ad.getTotalTestAttemptedCount(
                        session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID),
                        session.getSessionStringValue(ConstantGlobal.USER_ID))));

        ((TextView) fragmentRootView.findViewById(R.id.profile_question_attempted)).setText(String
                .valueOf((int)totalQusAttempted));

        ((TextView) fragmentRootView.findViewById(R.id.profile_strong_course))
                .setText(strongCourseName);
    }

    private float getAvgMark(AnalyticsDataManager ad) {
        List<TestAvg> testAvgMark = ad.getTestAvgMark(
                session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID),
                session.getSessionStringValue(ConstantGlobal.USER_ID));
        float averageScorePercentage = 0;
        for (TestAvg tAvgMark : testAvgMark) {
            if(Integer.parseInt(tAvgMark.totalMarks) != 0 && Integer.parseInt(tAvgMark.attempted)!= 0) {
                float Percentage = Integer.parseInt(tAvgMark.score) * 100 / Integer.parseInt(tAvgMark.totalMarks);
                averageScorePercentage=averageScorePercentage+Percentage;
            }
        }
        averageScorePercentage = averageScorePercentage/testAvgMark.size();
        Log.e("TestAvgMark averageScorePercentage", "" + averageScorePercentage);
        return Float.valueOf(decimalFormat.format(averageScorePercentage));
    }

    protected String getAvgTimeTakenString(int qusAttempted, float timeTaken) {
        Log.e("Time timeTaken", timeTaken + "");
        float totalSecondsPerQus = qusAttempted != 0 ? (timeTaken / qusAttempted) : 0;
        Log.e("Time totalSecondsPerQus", totalSecondsPerQus + "");
        String avgTime = "" + Math.round((totalSecondsPerQus / 60) * 100.0) / 100.0;
        Log.e("Time avgTime", (totalSecondsPerQus / 60) + "");
        return avgTime;
    }

    protected void addStudyHistory() {

        Log.d("AbstractVedantuActivity", "adding study history");
        ListView listView = (ListView) fragmentRootView
                .findViewById(R.id.profile_study_history_list);
        SessionManager sm = SessionManager.getInstance(m_cObjNhanceBaseActivity
                .getApplicationContext());
        List<StudyHistoryDetails> history = new UserActivityDataManager(m_cObjNhanceBaseActivity
                .getApplicationContext()).getStudyHistoryDetails(
                sm.getSessionIntValue(ConstantGlobal.ORG_KEY_ID),
                sm.getSessionStringValue(ConstantGlobal.USER_ID), 0, 20);
        listView.setAdapter(new StudyHistoryAdapter(m_cObjNhanceBaseActivity,
                R.layout.list_item_view_study_history, history));

        View noStudyHistory = fragmentRootView.findViewById(R.id.profile_no_study_history);
        if (noStudyHistory != null) {
            if (history.isEmpty()) {
                noStudyHistory.setVisibility(View.VISIBLE);
            } else {
                noStudyHistory.setVisibility(View.GONE);
            }
        }
        final ContentDataManager cDataManager = new ContentDataManager(m_cObjNhanceBaseActivity
                .getApplicationContext());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                StudyHistoryDetails hs = (StudyHistoryDetails) view.getTag();
                if (hs != null) {
                    LibraryContentRes contentRes = cDataManager
                            .getLibraryContentRes(hs.studyHistory.linkId);
                    LibraryUtils.onLibraryItemClickListnerImpl(m_cObjNhanceBaseActivity
                            .getApplicationContext(), contentRes);
                }
            }
        });
    }

    public class StudyHistoryAdapter extends ArrayAdapter<StudyHistoryDetails> {

        List<StudyHistoryDetails> history;
        int                       textViewResourceId;

        public StudyHistoryAdapter(Context context, int textViewResourceId,
                List<StudyHistoryDetails> objects) {

            super(context, textViewResourceId, objects);
            this.history = objects;
            this.textViewResourceId = textViewResourceId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view = convertView;
            if (view == null) {
                LayoutInflater inflator = (LayoutInflater) getContext().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                view = inflator.inflate(textViewResourceId, parent, false);
            }

            StudyHistoryDetails hs = history != null && !history.isEmpty() ? history.get(position)
                    : null;
            if (hs == null || hs.studyHistory == null) {
                return null;
            }

            ViewUtils.setTextViewValue(view, R.id.study_history_content_title, hs.name);
            ViewUtils.setTextViewValue(
                    view,
                    R.id.study_history_time_string,
                    DateUtils.getRelativeTimeSpanString(Long.valueOf(hs.studyHistory.timeCreated),
                            System.currentTimeMillis(), 0, DateUtils.FORMAT_ABBREV_RELATIVE)
                            .toString());
            ViewUtils.setTextViewValue(view, R.id.study_history_content_type,
                    hs.entityType.getDisplayName());
            Log.d("StudyHistoryAdapter", "note time: " + hs.studyHistory.timeCreated);
            view.setTag(hs);
            return view;
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);
        Log.i(TAG, "onConfigurationChanged called");
        destoryAndCreateFragmentView();
    }

    @Override
    public void onResume() {

        GoogleAnalyticsUtils.sendPageViewDataToGA();
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.profile, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action buttons
        switch (item.getItemId()) {
        case R.id.action_logout:
            session.logoutUser();
            return true;
        case R.id.action_about:
            Intent intent = new Intent(m_cObjNhanceBaseActivity, AboutActivity.class);
            startActivity(intent);
        default:
            return super.onOptionsItemSelected(item);
        }
    }

}
