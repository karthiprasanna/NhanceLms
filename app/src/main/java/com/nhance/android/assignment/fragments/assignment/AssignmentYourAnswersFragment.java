package com.nhance.android.assignment.fragments.assignment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.nhance.android.R;
import com.nhance.android.assignment.activity.AssignmentSingleQuestionActivity;
import com.nhance.android.assignment.adapter.AssignmentYourAnswerCourseListAdapter;
import com.nhance.android.assignment.db.models.analytics.AssignmentBoardAnalytics;
import com.nhance.android.assignment.pojo.TakeAssignmentQuestionWithAnswerGiven;
import com.nhance.android.assignment.pojo.assignment.AssignmentBoardQuestionAttemptInfo;
import com.nhance.android.assignment.pojo.assignment.AssignmentMetadata;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.AnalyticsDataManager;
import com.nhance.android.db.datamanagers.ContentDataManager;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.utils.GoogleAnalyticsUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Himank Shah on 12/7/2016.
 */

public class AssignmentYourAnswersFragment extends AbstractAssignmentFragment implements OnItemClickListener {

    private static final String TAG = "AssignmentYourAnswersFragment";
    private AnalyticsDataManager analyticsDataManager;
    private SessionManager session;
    ArrayList<AssignmentBoardQuestionAttemptInfo> courseWiseQuestionAttempts;

    @SuppressWarnings("unchecked")
    private void restoreFromSavedInstanceState(Bundle savedInstanceState) {

        if (savedInstanceState == null) {
            return;
        }
        courseWiseQuestionAttempts = (ArrayList<AssignmentBoardQuestionAttemptInfo>) savedInstanceState
                .getSerializable("courseWiseQuestionAttempts");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        GoogleAnalyticsUtils.setScreenName(ConstantGlobal.GA_SCREEN_ASSIGNMENT_USER_ANSWERS);
    }

    @Override
    public View
    onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_assignment_your_answers, container, false);

        Bundle values = getArguments();
        if (values == null) {
            return null;
        }

        cDataManager = new ContentDataManager(getActivity());
        session = SessionManager.getInstance(getActivity());

        @SuppressWarnings("unchecked")
        List<AssignmentMetadata> assignmentCourseMetadata = (List<AssignmentMetadata>) getArguments()
                .getSerializable(ConstantGlobal.ASSIGNMENT_METADATA);

        restoreFromSavedInstanceState(savedInstanceState);

        if (courseWiseQuestionAttempts == null) {

            analyticsDataManager = new AnalyticsDataManager(getActivity());

            courseWiseQuestionAttempts = new ArrayList<AssignmentBoardQuestionAttemptInfo>();
            LinkedHashMap<AssignmentBoardAnalytics, List<TakeAssignmentQuestionWithAnswerGiven>> userQuestionAttemptMap = analyticsDataManager
                    .getAssignmentCourseWiseQuestionAnswerGivenMap(assignmentCourseMetadata,
                            session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID),
                            session.getSessionStringValue(ConstantGlobal.USER_ID),
                            values.getString(ConstantGlobal.ENTITY_ID),
                            values.getString(ConstantGlobal.ENTITY_TYPE));

            for (Map.Entry<AssignmentBoardAnalytics, List<TakeAssignmentQuestionWithAnswerGiven>> entry : userQuestionAttemptMap
                    .entrySet()) {
                courseWiseQuestionAttempts.add(new AssignmentBoardQuestionAttemptInfo(entry.getKey(),
                        entry.getValue()));
            }
        }
        ListView courseAnsListContainer = (ListView) view.findViewById(R.id.assignment_course_list);
        courseAnsListContainer.setAdapter(new AssignmentYourAnswerCourseListAdapter(getActivity(),
                R.layout.list_item_view_assignment_course_your_answer, courseWiseQuestionAttempts, this, SessionManager.getInstance(getActivity()).getAssignmentAttempts(values.getString(ConstantGlobal.ASSIGNMENT_NAME))));

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
        // setQuestionViewPopup(courseName, (TakeAssignmentQuestionWithAnswerGiven) view.getTag());
        TakeAssignmentQuestionWithAnswerGiven ansGiven = (TakeAssignmentQuestionWithAnswerGiven) view.getTag();
        Intent intent = new Intent(getActivity().getApplicationContext(),
                AssignmentSingleQuestionActivity.class);
        intent.putExtra(AssignmentSingleQuestionActivity.SINGLE_QUES_DATA, ansGiven);
        startActivity(intent);
    }

    @Override
    public void onResume() {

        super.onResume();
        GoogleAnalyticsUtils.sendPageViewDataToGA();
    }
}
