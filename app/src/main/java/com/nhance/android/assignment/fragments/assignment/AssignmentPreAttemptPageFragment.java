package com.nhance.android.assignment.fragments.assignment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nhance.android.R;
import com.nhance.android.assignment.activity.AssignmentPreAttemptPageActivity;
import com.nhance.android.assignment.pojo.assignment.AssignmentDetails;
import com.nhance.android.assignment.pojo.assignment.AssignmentMetadata;
import com.nhance.android.assignment.pojo.content.infos.AssignmentExtendedInfo;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.models.entity.Content;
import com.nhance.android.fragments.AbstractVedantuFragment;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.pojos.tests.BoardQus;
import com.nhance.android.pojos.tests.Marks;
import com.nhance.android.utils.GoogleAnalyticsUtils;

import java.util.List;

/**
 * Created by Himank Shah on 12/2/2016.
 */

public class AssignmentPreAttemptPageFragment extends AbstractVedantuFragment {

    private AssignmentPreAttemptPageActivity assignmentPreAttemptActivityInstance;
    private static final String CURRENT_SUBJECT_INDEX = "currentSubjectIndex";
    private View fragmentRootView;
    private int                        currentSubjectIndex   = -1;
    private Content assignment;
    private AssignmentExtendedInfo assignmentInfo;
    private SessionManager session;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GoogleAnalyticsUtils.setScreenName(ConstantGlobal.GA_SCREEN_PRE_ASSIGNMENT);
        Activity assignmentPreAttemptActivity = getActivity();
        currentSubjectIndex = getArguments().getInt(CURRENT_SUBJECT_INDEX);
        session = SessionManager.getInstance(getActivity());

        if (assignmentPreAttemptActivity instanceof AssignmentPreAttemptPageActivity) {
            assignmentPreAttemptActivityInstance = (AssignmentPreAttemptPageActivity) getActivity();
            assignment = assignmentPreAttemptActivityInstance.getBasicAssignmentInfo();
            assignmentInfo = assignmentPreAttemptActivityInstance.getExtendedAssignmentInfo();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int layout;
        if (currentSubjectIndex == -1) {
            layout = R.layout.fragment_assignment_pre_attempt_page;
        } else {
            layout = R.layout.fragment_assignment_pre_attempt_subject_page;
        }
        fragmentRootView = inflater.inflate(layout, container, false);
        return fragmentRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        if (currentSubjectIndex == -1) {
            pouplatedPreAssignmentPageContent();
        } else {
            pouplatedPreAssignmentSubjectPageContent();
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {

        super.onResume();

        View takeAssignmentOrShowResultsButton = m_cObjNhanceBaseActivity.findViewById(R.id.assignment_pre_bottom_layout);
        if (takeAssignmentOrShowResultsButton != null) {
            takeAssignmentOrShowResultsButton.setVisibility(View.VISIBLE);
        }
        if (currentSubjectIndex == -1) {
            GoogleAnalyticsUtils.sendPageViewDataToGA();
        }
    }

    private void pouplatedPreAssignmentPageContent() {

        m_cObjNhanceBaseActivity.getSupportActionBar().setTitle(assignment.name);

        // marks,questions,duration
//        TextView marks = (TextView) fragmentRootView.findViewById(R.id.assignment_pre_marks);
//        marks.setText(Integer.toString(assignmentInfo.totalMarks));

//        TextView qus = (TextView) fragmentRootView.findViewById(R.id.assignment_pre_questions);
//        qus.setText(Integer.toString(assignmentInfo.qusCount));

//        TextView duration = (TextView) fragmentRootView.findViewById(R.id.assignment_pre_duration);
//        duration.setText(AssignmentLocalManager.getDurationSpecificString(assignmentInfo.duration, false, false));
//        int durationStringResId = R.string.mins_caps;
//        if ((assignmentInfo.duration / 1000) >= 3600) {
//            durationStringResId = R.string.hours_caps;
//        }

//        ((TextView) fragmentRootView.findViewById(R.id.assignment_pre_duration_string))
//                .setText(durationStringResId);

        View.OnClickListener subjectClickListner = new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                int subjectIndex = (Integer) v.getTag();
                assignmentPreAttemptActivityInstance.setCurrentSubjectIndex(subjectIndex);
                assignmentPreAttemptActivityInstance.loadPreAssignmentPageContent(true);
            }
        };

        // subjects and their question count
        LinearLayout subjectsHolder = (LinearLayout) fragmentRootView
                .findViewById(R.id.assignment_pre_subjects_holder);
        int subjectsCount = assignmentInfo.metadata.size();
        for (int k = 0; k < subjectsCount; k++) {
            AssignmentMetadata subjectMetadata = assignmentInfo.metadata.get(k);
            View subjectRootView = ((LayoutInflater) m_cObjNhanceBaseActivity.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE)).inflate(
                    R.layout.list_item_view_assignment_pre_subject, subjectsHolder, false);
            ((TextView) subjectRootView.findViewById(R.id.assignment_pre_subject_name))
                    .setText(subjectMetadata.name);
            ((TextView) subjectRootView.findViewById(R.id.assignment_pre_list_item_questions))
                    .setText(Integer.toString(subjectMetadata.qusCount));
            ((TextView) subjectRootView.findViewById(R.id.assignment_pre_list_item_marks))
                    .setText(""+session.getAssignmentAttempts(assignment.name));
            subjectRootView.setTag(k);
            subjectsHolder.addView(subjectRootView);
            subjectRootView.setOnClickListener(subjectClickListner);
        }
    }

    private void pouplatedPreAssignmentSubjectPageContent() {

        AssignmentMetadata subjectMetadata = assignmentInfo.metadata.get(currentSubjectIndex);
        m_cObjNhanceBaseActivity.getSupportActionBar().setTitle(subjectMetadata.name);

        // marks and question count
        TextView marks = (TextView) fragmentRootView.findViewById(R.id.assignment_pre_subject_marks);
        marks.setText(""+session.getAssignmentAttempts(assignment.name));

        TextView qus = (TextView) fragmentRootView
                .findViewById(R.id.assignment_pre_subject_page_question_count);
        qus.setText(Integer.toString(subjectMetadata.qusCount));

        List<AssignmentDetails> assignmentQTypeDetails = subjectMetadata.details;
        LinearLayout qTypesTable = (LinearLayout) fragmentRootView
                .findViewById(R.id.assignment_pre_qtypes_table);
        if (assignmentQTypeDetails.isEmpty()) {
            qTypesTable.setVisibility(View.GONE);
        }
        for (AssignmentDetails qType : assignmentQTypeDetails) {
            View qTypeRootView = ((LayoutInflater) m_cObjNhanceBaseActivity.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE)).inflate(
                    R.layout.list_item_view_assignment_pre_qtype, qTypesTable, false);
            ((TextView) qTypeRootView.findViewById(R.id.assignment_pre_qtype)).setText(qType.type.name());
            Marks qTypeMarks = qType.marks;
            String postiveMarks = Integer.toString(qTypeMarks.positive);
            TextView negativeMarksView = ((TextView) qTypeRootView
                    .findViewById(R.id.assignment_pre_qtype_negative_marks));
            String negativeMarks = "0";
            if (qTypeMarks.negative > 0) {
                negativeMarksView.setVisibility(View.VISIBLE);
                postiveMarks += ",";
                negativeMarks = "-" + Integer.toString(qTypeMarks.negative);
            }
            ((TextView) qTypeRootView.findViewById(R.id.assignment_pre_qtype_postive_marks))
                    .setText(postiveMarks);
            ((TextView) qTypeRootView.findViewById(R.id.assignment_pre_qtype_negative_marks))
                    .setText(negativeMarks);
            ((TextView) qTypeRootView.findViewById(R.id.assignment_pre_qtype_questions)).setText(Integer
                    .toString(qType.qusCount));
            qTypesTable.addView(qTypeRootView);
        }

        // topics
        List<BoardQus> topics = subjectMetadata.children;
        LinearLayout topicsHolder = (LinearLayout) fragmentRootView
                .findViewById(R.id.assignment_pre_subject_topics_holder);
        if (topics.isEmpty()) {
            fragmentRootView.findViewById(R.id.assignment_pre_subject_curriculum_head).setVisibility(
                    View.GONE);
        }
        for (BoardQus topicBoard : topics) {
            TextView topicView = (TextView) ((LayoutInflater) m_cObjNhanceBaseActivity.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE)).inflate(
                    R.layout.list_item_view_assignment_pre_topic, topicsHolder, false);
            topicView.setText(topicBoard.name);
            topicsHolder.addView(topicView);
        }
    }
}
