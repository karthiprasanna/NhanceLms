package com.nhance.android.fragments.tests;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.nhance.android.activities.tests.SingleQuestionActivity;
import com.nhance.android.adapters.tests.AbstractTestFragment;
import com.nhance.android.adapters.tests.TestYourAnswerCourseListAdapter;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.AnalyticsDataManager;
import com.nhance.android.db.datamanagers.ContentDataManager;
import com.nhance.android.db.models.analytics.TestBoardAnalytics;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.pojos.TakeTestQuestionWithAnswerGiven;
import com.nhance.android.pojos.tests.TestBoardQuestionAttemptInfo;
import com.nhance.android.pojos.tests.TestMetadata;
import com.nhance.android.utils.GoogleAnalyticsUtils;
import com.nhance.android.R;

public class TestYourAnswersFragment extends AbstractTestFragment implements OnItemClickListener {

    private static final String             TAG = "TestYourAnswersFragment";
    private AnalyticsDataManager            analyticsDataManager;
    private SessionManager                  session;
    ArrayList<TestBoardQuestionAttemptInfo> courseWiseQuestionAttempts;

    @SuppressWarnings("unchecked")
    private void restoreFromSavedInstanceState(Bundle savedInstanceState) {

        if (savedInstanceState == null) {
            return;
        }
        courseWiseQuestionAttempts = (ArrayList<TestBoardQuestionAttemptInfo>) savedInstanceState
                .getSerializable("courseWiseQuestionAttempts");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        GoogleAnalyticsUtils.setScreenName(ConstantGlobal.GA_SCREEN_TEST_USER_ANSWERS);
    }

    @Override
    public View
            onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_test_your_answers, container, false);

        Bundle values = getArguments();
        if (values == null) {
            return null;
        }

        cDataManager = new ContentDataManager(getActivity());
        session = SessionManager.getInstance(getActivity());

        @SuppressWarnings("unchecked")
        List<TestMetadata> testCourseMetadata = (List<TestMetadata>) getArguments()
                .getSerializable(ConstantGlobal.TEST_METADATA);

        restoreFromSavedInstanceState(savedInstanceState);

        if (courseWiseQuestionAttempts == null) {

            analyticsDataManager = new AnalyticsDataManager(getActivity());

            courseWiseQuestionAttempts = new ArrayList<TestBoardQuestionAttemptInfo>();
            LinkedHashMap<TestBoardAnalytics, List<TakeTestQuestionWithAnswerGiven>> userQuestionAttemptMap = analyticsDataManager
                    .getTestCourseWiseQuestionAnswerGivenMap(testCourseMetadata,
                            session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID),
                            session.getSessionStringValue(ConstantGlobal.USER_ID),
                            values.getString(ConstantGlobal.ENTITY_ID),
                            values.getString(ConstantGlobal.ENTITY_TYPE));

            for (Entry<TestBoardAnalytics, List<TakeTestQuestionWithAnswerGiven>> entry : userQuestionAttemptMap
                    .entrySet()) {
                courseWiseQuestionAttempts.add(new TestBoardQuestionAttemptInfo(entry.getKey(),
                        entry.getValue()));
            }
        }
        ListView courseAnsListContainer = (ListView) view.findViewById(R.id.test_course_list);
        courseAnsListContainer.setAdapter(new TestYourAnswerCourseListAdapter(getActivity(),
                R.layout.list_item_view_test_course_your_answer, courseWiseQuestionAttempts, this));

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
        if (outState != null) {
            outState.putSerializable("courseWiseQuestionAttempts", courseWiseQuestionAttempts);
        }
    }

    /**
     * this method will be used to select grid view item (question) and will show single question
     * popup
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        String courseName = (String) parent.getTag();
        Log.d(TAG, "courseName: " + courseName + ", qusItem: " + view.getTag());
        // setQuestionViewPopup(courseName, (TakeTestQuestionWithAnswerGiven) view.getTag());
        TakeTestQuestionWithAnswerGiven ansGiven = (TakeTestQuestionWithAnswerGiven) view.getTag();
        Intent intent = new Intent(getActivity().getApplicationContext(),
                SingleQuestionActivity.class);
        intent.putExtra(SingleQuestionActivity.SINGLE_QUES_DATA, ansGiven);
        startActivity(intent);
    }

    @Override
    public void onResume() {

        super.onResume();
        GoogleAnalyticsUtils.sendPageViewDataToGA();
    }
}
