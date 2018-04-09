package com.nhance.android.fragments.tests;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nhance.android.activities.tests.TestPreAttemptPageActivity;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.models.entity.Content;
import com.nhance.android.fragments.AbstractVedantuFragment;
import com.nhance.android.managers.LocalManager;
import com.nhance.android.pojos.content.infos.TestExtendedInfo;
import com.nhance.android.pojos.tests.BoardQus;
import com.nhance.android.pojos.tests.Marks;
import com.nhance.android.pojos.tests.TestDetails;
import com.nhance.android.pojos.tests.TestMetadata;
import com.nhance.android.utils.GoogleAnalyticsUtils;
import com.nhance.android.R;

public class TestPreAttemptPageFragment extends AbstractVedantuFragment {

    private TestPreAttemptPageActivity testPreAttemptActivityInstance;
    private static final String        CURRENT_SUBJECT_INDEX = "currentSubjectIndex";
    private View                       fragmentRootView;
    private int                        currentSubjectIndex   = -1;
    private Content                    test;
    private TestExtendedInfo           testInfo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GoogleAnalyticsUtils.setScreenName(ConstantGlobal.GA_SCREEN_PRE_TEST);
        Activity testPreAttemptActivity = getActivity();
        currentSubjectIndex = getArguments().getInt(CURRENT_SUBJECT_INDEX);
        if (testPreAttemptActivity instanceof TestPreAttemptPageActivity) {
            testPreAttemptActivityInstance = (TestPreAttemptPageActivity) getActivity();
            test = testPreAttemptActivityInstance.getBasicTestInfo();
            testInfo = testPreAttemptActivityInstance.getExtendedTestInfo();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int layout;
        if (currentSubjectIndex == -1) {
            layout = R.layout.fragment_test_pre_attempt_page;
        } else {
            layout = R.layout.fragment_test_pre_attempt_subject_page;
        }
        fragmentRootView = inflater.inflate(layout, container, false);
        return fragmentRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        if (currentSubjectIndex == -1) {
            pouplatedPreTestPageContent();
        } else {
            pouplatedPreTestSubjectPageContent();
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {

        super.onResume();

        View takeTestOrShowResultsButton = m_cObjNhanceBaseActivity.findViewById(R.id.test_pre_bottom_layout);
        if (takeTestOrShowResultsButton != null) {
            takeTestOrShowResultsButton.setVisibility(View.VISIBLE);
        }
        if (currentSubjectIndex == -1) {
            GoogleAnalyticsUtils.sendPageViewDataToGA();
        }
    }

    private void pouplatedPreTestPageContent() {

        m_cObjNhanceBaseActivity.getSupportActionBar().setTitle(test.name);

        // marks,questions,duration
        TextView marks = (TextView) fragmentRootView.findViewById(R.id.test_pre_marks);
        marks.setText(Integer.toString(testInfo.totalMarks));

        TextView qus = (TextView) fragmentRootView.findViewById(R.id.test_pre_questions);
        qus.setText(Integer.toString(testInfo.qusCount));

        TextView duration = (TextView) fragmentRootView.findViewById(R.id.test_pre_duration);
        duration.setText(LocalManager.getDurationSpecificString(testInfo.duration, false, false));
        int durationStringResId = R.string.mins_caps;
        if ((testInfo.duration / 1000) >= 3600) {
            durationStringResId = R.string.hours_caps;
        }

        ((TextView) fragmentRootView.findViewById(R.id.test_pre_duration_string))
                .setText(durationStringResId);

        OnClickListener subjectClickListner = new OnClickListener() {

            @Override
            public void onClick(View v) {

                int subjectIndex = (Integer) v.getTag();
                testPreAttemptActivityInstance.setCurrentSubjectIndex(subjectIndex);
                testPreAttemptActivityInstance.loadPreTestPageContent(true);
            }
        };

        // subjects and their question count
        LinearLayout subjectsHolder = (LinearLayout) fragmentRootView
                .findViewById(R.id.test_pre_subjects_holder);
        int subjectsCount = testInfo.metadata.size();
        for (int k = 0; k < subjectsCount; k++) {
            TestMetadata subjectMetadata = testInfo.metadata.get(k);
            View subjectRootView = ((LayoutInflater) m_cObjNhanceBaseActivity.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE)).inflate(
                    R.layout.list_item_view_test_pre_subject, subjectsHolder, false);
            ((TextView) subjectRootView.findViewById(R.id.test_pre_subject_name))
                    .setText(subjectMetadata.name);
            ((TextView) subjectRootView.findViewById(R.id.test_pre_list_item_questions))
                    .setText(Integer.toString(subjectMetadata.qusCount));
            ((TextView) subjectRootView.findViewById(R.id.test_pre_list_item_marks))
                    .setText(Integer.toString(subjectMetadata.totalMarks));
            subjectRootView.setTag(k);
            subjectsHolder.addView(subjectRootView);
            subjectRootView.setOnClickListener(subjectClickListner);
        }
    }

    private void pouplatedPreTestSubjectPageContent() {

        TestMetadata subjectMetadata = testInfo.metadata.get(currentSubjectIndex);
        m_cObjNhanceBaseActivity.getSupportActionBar().setTitle(subjectMetadata.name);

        // marks and question count
        TextView marks = (TextView) fragmentRootView.findViewById(R.id.test_pre_subject_marks);
        marks.setText(Integer.toString(subjectMetadata.totalMarks));

        TextView qus = (TextView) fragmentRootView
                .findViewById(R.id.test_pre_subject_page_question_count);
        qus.setText(Integer.toString(subjectMetadata.qusCount));

        List<TestDetails> testQTypeDetails = subjectMetadata.details;
        LinearLayout qTypesTable = (LinearLayout) fragmentRootView
                .findViewById(R.id.test_pre_qtypes_table);
        if (testQTypeDetails.isEmpty()) {
            qTypesTable.setVisibility(View.GONE);
        }
        for (TestDetails qType : testQTypeDetails) {
            View qTypeRootView = ((LayoutInflater) m_cObjNhanceBaseActivity.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE)).inflate(
                    R.layout.list_item_view_test_pre_qtype, qTypesTable, false);
            ((TextView) qTypeRootView.findViewById(R.id.test_pre_qtype)).setText(qType.type.name());
            Marks qTypeMarks = qType.marks;
            String postiveMarks = Integer.toString(qTypeMarks.positive);
            TextView negativeMarksView = ((TextView) qTypeRootView
                    .findViewById(R.id.test_pre_qtype_negative_marks));
            String negativeMarks = "0";
            if (qTypeMarks.negative > 0) {
                negativeMarksView.setVisibility(View.VISIBLE);
                postiveMarks += ",";
                negativeMarks = "-" + Integer.toString(qTypeMarks.negative);
            }
            ((TextView) qTypeRootView.findViewById(R.id.test_pre_qtype_postive_marks))
                    .setText(postiveMarks);
            ((TextView) qTypeRootView.findViewById(R.id.test_pre_qtype_negative_marks))
                    .setText(negativeMarks);
            ((TextView) qTypeRootView.findViewById(R.id.test_pre_qtype_questions)).setText(Integer
                    .toString(qType.qusCount));
            qTypesTable.addView(qTypeRootView);
        }

        // topics
        List<BoardQus> topics = subjectMetadata.children;
        LinearLayout topicsHolder = (LinearLayout) fragmentRootView
                .findViewById(R.id.test_pre_subject_topics_holder);
        if (topics.isEmpty()) {
            fragmentRootView.findViewById(R.id.test_pre_subject_curriculum_head).setVisibility(
                    View.GONE);
        }
        for (BoardQus topicBoard : topics) {
            TextView topicView = (TextView) ((LayoutInflater) m_cObjNhanceBaseActivity.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE)).inflate(
                    R.layout.list_item_view_test_pre_topic, topicsHolder, false);
            topicView.setText(topicBoard.name);
            topicsHolder.addView(topicView);
        }
    }
}
