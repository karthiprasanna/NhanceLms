package com.nhance.android.fragments.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.nhance.android.activities.tests.SingleQuestionActivity;
import com.nhance.android.adapters.tests.AbstractTestFragment;
import com.nhance.android.adapters.tests.TestCourseArrayAdapter;
import com.nhance.android.adapters.tests.TestStudentAnswersQuestionListAdapter;
import com.nhance.android.async.tasks.CachedWebDataFetcherTask;
import com.nhance.android.async.tasks.ICachedTaskProcessor;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.customviews.ThreeHorizontalBarGraph;
import com.nhance.android.db.datamanagers.AnalyticsDataManager;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.pojos.TakeTestQuestionWithAnswerGiven;
import com.nhance.android.pojos.content.infos.TestExtendedInfo;
import com.nhance.android.pojos.onlinedata.tests.EntityAttemptInfo;
import com.nhance.android.pojos.onlinedata.tests.EntityAttemptsInfoRes;
import com.nhance.android.pojos.onlinedata.tests.TestInfo;
import com.nhance.android.pojos.tests.EntityMeasures;
import com.nhance.android.pojos.tests.TestMetadata;
import com.nhance.android.utils.GoogleAnalyticsUtils;
import com.nhance.android.utils.SQLDBUtil;
import com.nhance.android.R;

public class TestStudentAnswersFragment extends AbstractTestFragment implements OnClickListener {

    SessionManager                                       session;
    private View                                         parentView;
    final private String                                 TAG         = "TestStudentAnswersFragment";
    private String                                       entityId;
    private String                                       entityType;
    protected Handler                                    mHandler    = new Handler();

    private String                                       orderBy;
    private String                                       sortOrder   = SQLDBUtil.ORDER_DESC;
    private String                                       courseBrdId = null;
    private Map<String, TakeTestQuestionWithAnswerGiven> qidToQuestionAnswerMap;
    private boolean                                      isDestroyed = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        GoogleAnalyticsUtils.setScreenName(ConstantGlobal.GA_SCREEN_TEST_STUDENT_ANSWERS);
    }

    @Override
    public View
            onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_test_your_answers, container, false);
        parentView = view;
        Bundle values = getArguments();
        if (values == null) {
            return null;
        }
        setSpinners();
        getTestData();
        // parentView.setBackgroundColor(getResources().getColor(R.color.white));
        return view;
    }

    private void setSpinners() {

        RelativeLayout holder = (RelativeLayout) parentView
                .findViewById(R.id.test_student_answers_course_filter_container);
        holder.setVisibility(View.VISIBLE);
        Spinner attemptFilter = (Spinner) holder
                .findViewById(R.id.test_student_answers_attempt_filter);
        final String[] filetrSortOrderList = { SQLDBUtil.ORDER_DESC, SQLDBUtil.ORDER_DESC,
                SQLDBUtil.ORDER_ASC, SQLDBUtil.ORDER_DESC, SQLDBUtil.ORDER_ASC };
        final String[] filetrOrderByList = { null, "attempts", "attempts", "correct", "correct" };
        attemptFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {

                orderBy = filetrOrderByList[position];
                sortOrder = filetrSortOrderList[position];
                fetchTestData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

                // TODO Auto-generated method stub
            }
        });
        Spinner courseFilter = (Spinner) parentView
                .findViewById(R.id.test_student_answers_course_filter);
        List<TestMetadata> metadataList = new ArrayList<TestMetadata>();
        metadataList.add(0, new TestMetadata(null, getResources().getString(R.string.all), 0));
        courseFilter.setAdapter(new TestCourseArrayAdapter(getActivity(),
                R.layout.list_item_spinner_course, metadataList));

        session = SessionManager.getInstance(getActivity());
        Bundle bundle = getArguments();
        entityId = bundle.getString(ConstantGlobal.ENTITY_ID);
        entityType = bundle.getString(ConstantGlobal.ENTITY_TYPE);
        String cacheKey = entityType + "/" + entityId;
        CachedWebDataFetcherTask<TestInfo> cachedWebDataFetcherTask = new CachedWebDataFetcherTask<TestInfo>(
                session, null, CachedWebDataFetcherTask.ReqAction.GET_TEST_DETAILS,
                new TestInfoTaskProcessor(), true, cacheKey, TestInfo.class);

        cachedWebDataFetcherTask.addExtraParams("id", entityId);
        cachedWebDataFetcherTask.executeTask(false);
    }

    private final class TestInfoTaskProcessor implements ICachedTaskProcessor<TestInfo> {

        @Override
        public void onResultDataFromCache(TestInfo data) {

            if (isDestroyed) {
                return;
            }
            if (data != null) {
                drawCourseSpinner(data);
            }
        }

        @Override
        public void onTaskPostExecute(boolean success, TestInfo result) {

            if (isDestroyed) {
                return;
            }
            Log.d(TAG, "TEST INFO RESULTS ======== success == " + success + ", RESULT === "
                    + result);
            if (success && result != null) {
                drawCourseSpinner(result);
            }
        }

    }

    private void drawCourseSpinner(TestInfo resp) {

        List<TestMetadata> metadataList = new ArrayList<TestMetadata>(resp.metadata);
        metadataList.add(0, new TestMetadata(null, getResources().getString(R.string.all), 0));

        Log.d(TAG, "test Meta data == " + metadataList);
        Spinner courseFilter = (Spinner) parentView
                .findViewById(R.id.test_student_answers_course_filter);
        if (metadataList.size() <= 0) {
            return;
        }
        courseFilter.setAdapter(new TestCourseArrayAdapter(getActivity(),
                R.layout.list_item_spinner_course, metadataList));
        courseFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View view, int arg2, long arg3) {

                courseBrdId = (String) view.getTag();
                fetchTestData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

                courseBrdId = null;
                fetchTestData();
            }
        });
    }

    private void getTestData() {

        Bundle bundle = getArguments();
        session = SessionManager.getInstance(getActivity());
        entityId = bundle.getString(ConstantGlobal.ENTITY_ID);
        entityType = bundle.getString(ConstantGlobal.ENTITY_TYPE);
        if (TextUtils.isEmpty(entityId)) {
            return;
        }
        TestExtendedInfo testInfo = (TestExtendedInfo) bundle
                .getSerializable(ConstantGlobal.TEST_METADATA);
        Log.d(TAG, "testInfo fetched === " + testInfo);
        if (testInfo != null) {
            qidToQuestionAnswerMap = new AnalyticsDataManager(getActivity().getApplicationContext())
                    .getQuestionWithAnswerAndQuestionMap(
                            session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID),
                            session.getSessionStringValue(ConstantGlobal.USER_ID),
                            testInfo.__getAllQIds());
        }
        sortOrder = SQLDBUtil.ORDER_DESC;
        fetchTestData();
    }

    private void fetchTestData() {

        Log.d(TAG, "GET TEST QUESTION ATTEMPTS ========" + entityId);
        String cacheKey = "QUESMD/" + entityType + "/" + entityId;
        CachedWebDataFetcherTask<EntityAttemptsInfoRes> cachedWebDataFetcherTask = new CachedWebDataFetcherTask<EntityAttemptsInfoRes>(
                session, null, CachedWebDataFetcherTask.ReqAction.GET_ENTITY_QUESTION_ATTEMPT,
                new TestQuestionAttemptsTaskProcessor(), true, cacheKey,
                EntityAttemptsInfoRes.class, 0, 200);
        // TODO REMOVE
        // HARD-CODE
        // SIZE
        cachedWebDataFetcherTask.addExtraParams("entity.id", entityId);
        cachedWebDataFetcherTask.addExtraParams("entity.type", entityType);
        if (orderBy != null) {
            cachedWebDataFetcherTask.addExtraParams("orderBy", orderBy);
        }
        cachedWebDataFetcherTask.addExtraParams("sortOrder", sortOrder);
        if (!TextUtils.isEmpty(courseBrdId)) {
            cachedWebDataFetcherTask.addExtraParams("brdId", courseBrdId);
        }
        cachedWebDataFetcherTask.executeTask(false);
    }

    private final class TestQuestionAttemptsTaskProcessor implements
            ICachedTaskProcessor<EntityAttemptsInfoRes> {

        @Override
        public void onResultDataFromCache(EntityAttemptsInfoRes data) {

            if (isDestroyed) {
                return;
            }
            if (data != null) {
                drawQuestions(data);
            }
        }

        @Override
        public void onTaskPostExecute(boolean success, EntityAttemptsInfoRes result) {

            if (isDestroyed) {
                return;
            }
            if (success && result != null) {
                drawQuestions(result);
            }
        }
    }

    private void drawQuestions(EntityAttemptsInfoRes resp) {

        List<EntityAttemptInfo> questions = resp.list;
        if (questions.size() > 0) {
            ListView courseAnsListContainer = (ListView) parentView
                    .findViewById(R.id.test_course_list);
            courseAnsListContainer.setAdapter(new TestStudentAnswersQuestionListAdapter(
                    getActivity(), R.layout.list_item_view_test_student_answer, questions, this));
            fillGraphData();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);
        Log.i(TAG, "onConfigurationChanged called");
    }

    private void fillGraphData() {

        mHandler.post(new Runnable() {

            @Override
            public void run() {

                ListView container = (ListView) parentView.findViewById(R.id.test_course_list);
                container.setDivider(null);
                container.setDividerHeight(getResources().getDimensionPixelSize(
                        R.dimen.test_student_answers_each_question_divider_height));

                for (int index = 0; index < container.getChildCount(); index++) {
                    LinearLayout view = (LinearLayout) container.getChildAt(index);
                    EntityMeasures measures = (EntityMeasures) view
                            .getTag(R.integer.question_measures_key);
                    ThreeHorizontalBarGraph holder = (ThreeHorizontalBarGraph) view
                            .findViewById(R.id.test_student_answer_analytics_holder);
                    long correctCount = measures.correct;
                    long incorrectCount = measures.incorrect;
                    long leftCount = measures.left;
                    holder.setValues((int) correctCount, (int) incorrectCount, (int) leftCount);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {

        String qId = (String) v.getTag(R.integer.question_id_key);
        Log.d(TAG, "question clicked === " + qId);
        if (qidToQuestionAnswerMap != null) {
            TakeTestQuestionWithAnswerGiven ansGiven = qidToQuestionAnswerMap.get(qId);
            Intent intent = new Intent(getActivity().getApplicationContext(),
                    SingleQuestionActivity.class);
            intent.putExtra(SingleQuestionActivity.SINGLE_QUES_DATA, ansGiven);
            intent.putExtra(SingleQuestionActivity.SHOW_ANS_GIVEN, SingleQuestionActivity.HIDE);
            startActivity(intent);
        } else {
            Toast.makeText(getActivity(), R.string.error_general, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {

        isDestroyed = true;
        super.onDestroy();
    }
}
