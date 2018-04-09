package com.nhance.android.activities.tests;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.nhance.android.activities.baseclasses.NhanceBaseActivity;
import com.nhance.android.async.tasks.IDownloadCompleteProcessor;
import com.nhance.android.async.tasks.ITaskProcessor;
import com.nhance.android.async.tasks.TestDownloader;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.AnalyticsDataManager;
import com.nhance.android.db.datamanagers.ContentDataManager;
import com.nhance.android.db.datamanagers.ModuleStatusDataManager;
import com.nhance.android.db.models.analytics.TestAnalytics;
import com.nhance.android.db.models.entity.Content;
import com.nhance.android.enums.AttemptState;
import com.nhance.android.enums.EntityType;
import com.nhance.android.enums.TestResultVisibility;
import com.nhance.android.fragments.tests.TestInstructionsDialogFragment;
import com.nhance.android.fragments.tests.TestPostAttemptPageFragment;
import com.nhance.android.fragments.tests.TestPreAttemptPageFragment;
import com.nhance.android.managers.LocalManager;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.pojos.content.infos.TestExtendedInfo;
import com.nhance.android.processors.analytics.sync.ServerToLocalAnalyticsSyncProcessor;
import com.nhance.android.utils.GoogleAnalyticsUtils;
import com.nhance.android.R;

public class TestPreAttemptPageActivity extends NhanceBaseActivity implements
        ITaskProcessor<JSONObject> {

    private static final String  TAG                   = "TestPreAttemptPageActivity";
    private int                  currentSubjectIndex   = -1;
    private static final String  CURRENT_SUBJECT_INDEX = "currentSubjectIndex";
    private Content              test;
    private TestExtendedInfo     testInfo;
    private ContentDataManager   cDataManager;
    private SessionManager       session;
    private AnalyticsDataManager analyticsDataManager;
    PopupWindow                  popup                 = null;
    boolean                      isTestModeOffline     = false;
    TestDownloader               testDownloader;
    private int                  contentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        GoogleAnalyticsUtils.setCustomDimensions(
                getIntent().getStringExtra(ConstantGlobal.ENTITY_ID),
                getIntent().getStringExtra(ConstantGlobal.NAME), ConstantGlobal.TEST);
        setContentView(R.layout.activity_test_pre_attempt_page);
        cDataManager = new ContentDataManager(getApplicationContext());
        session = SessionManager.getInstance(getApplicationContext());
        analyticsDataManager = new AnalyticsDataManager(this);
        if (savedInstanceState != null) {
            currentSubjectIndex = savedInstanceState.getInt(CURRENT_SUBJECT_INDEX);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setDisplayShowCustomEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        contentId = getIntent().getIntExtra(ConstantGlobal.CONTENT_ID, -1);
        if (contentId == -1) {
            Toast.makeText(getApplicationContext(), "There is some problem in opening the test.",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
        loadTestMetadata();
        boolean showPostTestPages = testInfo.resultVisibility == TestResultVisibility.VISIBLE
                && getIntent().getBooleanExtra("showPostTestPages", false);

        Log.d(TAG, "Show Post test pages::  " + showPostTestPages);
        if (showPostTestPages) {
            // TODO for now assuming this call comes only after end test ie the
            // user has
            // already attempted the test
            Log.d("attempted the test", "Show Post test pages::  " + showPostTestPages);
            loadPostTestPageContent(false);
            findViewById(R.id.test_pre_bottom_layout).setVisibility(View.GONE);
        } else {
            loadPreTestPageContent(false);
            findViewById(R.id.test_pre_bottom_layout).setVisibility(View.VISIBLE);
        }

        findViewById(R.id.test_pre_start_the_test).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                startTest();
            }
        });

    }

    private TextView takeTestOrShowResultsButtom;

    @Override
    protected void onResume() {

        loadTestMetadata();

        // TODO: this method need to be changed addedBy Shankar

        isTestModeOffline = testInfo != null && test.subType != null
                && "OFFLINE".equalsIgnoreCase(test.subType);
        resetAllThingsInBottomLayout();
        takeTestOrShowResultsButtom = (TextView) findViewById(R.id.test_pre_button);
        findViewById(R.id.test_pre_button).setVisibility(View.VISIBLE);
        if (testInfo.attempteState == AttemptState.ATTEMPTED) {
            updateModuleConsumedStatus();
            takeTestOrShowResultsButtom.setText(R.string.view_test_results);
        } else if (isTestModeOffline) {
            takeTestOrShowResultsButtom.setText(R.string.download_test_results);
        } else {
            setRedTakeTestButton(testInfo.attempteState);
        }
        takeTestOrShowResultsButtom.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (testInfo.attempteState == AttemptState.ATTEMPTED) {
                    // if the testResultVisibility==HIDDEN then show the
                    // message provide by the test
                    // creater/admin

                    if (testInfo.resultVisibility == TestResultVisibility.HIDDEN) {
                        showTestResultVisiblityPopup();
                        return;
                    }

                    findViewById(R.id.test_pre_bottom_layout).setVisibility(View.GONE);
                    loadPostTestPageContent(true);
                } else {
                    if (isTestModeOffline && !SessionManager.isOnline()) {
                        Toast.makeText(getBaseContext(), getString(R.string.no_internet_msg),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    showPrepareTestPopup();
                }
            }
        });
        super.onResume();
    }

    private ServerToLocalAnalyticsSyncProcessor syncer;
    private TestInstructionsDialogFragment      dialogFragment;

    private void showPrepareTestPopup() {

        syncer = new ServerToLocalAnalyticsSyncProcessor(this, test, this, !isTestModeOffline);
        resetAllThingsInBottomLayout();
        if (isTestModeOffline) {
            findViewById(R.id.test_pre_downloading_offline_test_results)
                    .setVisibility(View.VISIBLE);
            syncer.executeTask(false);
        } else {
            // this test is online test

            dialogFragment = new TestInstructionsDialogFragment();
            Bundle args = new Bundle();
            // TODO this is a wrong way of doing, but had no choice. I am in a
            // hurry
            args.putInt("heightOfButton", findViewById(R.id.test_pre_bottom_layout).getHeight());
            dialogFragment.setArguments(args);
            dialogFragment.show(getSupportFragmentManager(), null);

            Log.d(TAG, "checking if user has already attempted the test");
            if (!SessionManager.isOnline()) {
                if (!test.downloaded) {
                    setRedTakeTestButton(testInfo.attempteState);
                    Log.e(TAG, "test is not downloaded and internet connection is not available");
                    Toast.makeText(getBaseContext(), getString(R.string.no_internet_msg),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                setGreenStartTestButton();
            } else {

                // if online, then check if the test is already being attempted
                resetAllThingsInBottomLayout();
                findViewById(R.id.test_pre_download_progress_bar_holder)
                        .setVisibility(View.VISIBLE);
                syncer.executeTask(false);
            }

        }
        // TODO : check for analytics
    }

    @Override
    public void onTaskPostExecute(boolean success, JSONObject result) {

        resetAllThingsInBottomLayout();
        if (isTestModeOffline) {
            findViewById(R.id.test_pre_button).setVisibility(View.VISIBLE);
        }

        if (success) {
            if (!isTestModeOffline) {
                Toast.makeText(this, getString(R.string.already_attempted), Toast.LENGTH_LONG)
                        .show();
            }
            onResume();
            return;
        }

        if (test != null && test.downloaded) {
            setGreenStartTestButton();
        } else if (!isTestModeOffline && !success) {
            // online test and user has not attempted the test, also the test is
            // not downloaded,
            // so 1st download the test questions and start test on post
            // completion
            findViewById(R.id.test_pre_download_progress_bar_holder).setVisibility(View.VISIBLE);
            testDownloader = new TestDownloader(test, this, new OnTestQuestionDownload());
            testDownloader.executeTask(false);
        }

    }

    @Override
    public void onTaskStart(JSONObject result) {

    }

    private class OnTestQuestionDownload implements IDownloadCompleteProcessor<JSONObject> {

        @Override
        public JSONObject onComplete(JSONObject result, boolean completed) {

            Log.d(TAG, "onComplete method is called for test download");
            resetAllThingsInBottomLayout();
            if (completed) {
                test.downloaded = true;
                ContentValues tobeUpdatedValues = new ContentValues();
                tobeUpdatedValues.put(ConstantGlobal.DOWNLOADED, 1);
                cDataManager.updateContent(test._id, tobeUpdatedValues);
                setGreenStartTestButton();
            } else {
                setRedTakeTestButton(testInfo.attempteState);
                Toast.makeText(getBaseContext(), "Can not download test questions",
                        Toast.LENGTH_SHORT).show();
            }

            return result;
        }

    }

    private void resetAllThingsInBottomLayout() {

        findViewById(R.id.test_pre_bottom_layout).setPadding(0, 0, 0, 0);
        findViewById(R.id.test_pre_button).setVisibility(View.GONE);
//        findViewById(R.id.test_pre_button).setBackgroundResource(R.color.green);
        findViewById(R.id.test_pre_start_the_test).setVisibility(View.GONE);
        findViewById(R.id.test_pre_download_progress_bar_holder).setVisibility(View.GONE);
        findViewById(R.id.test_pre_downloading_offline_test_results).setVisibility(View.GONE);
    }

    private void setRedTakeTestButton(AttemptState attemptState) {

        int paddingSide = getResources().getDimensionPixelSize(R.dimen.test_pre_common_side_margin);
        int paddingBottom = getResources().getDimensionPixelSize(
                R.dimen.test_pre_bottom_layout_padding_bottom);
        findViewById(R.id.test_pre_bottom_layout).setPadding(paddingSide, 0, paddingSide,
                paddingBottom);
        TextView takeTestView = (TextView) findViewById(R.id.test_pre_button);
        takeTestView.setVisibility(View.VISIBLE);
      takeTestView.setBackgroundResource(R.drawable.btn_rounded_corner);
        takeTestView
                .setText(getString(attemptState == AttemptState.INPROGRESS ? R.string.resume_this_test
                        : R.string.take_this_test));
    }

    private void setGreenStartTestButton() {

        int paddingSide = getResources().getDimensionPixelSize(R.dimen.test_pre_common_side_margin);
        int paddingBottom = getResources().getDimensionPixelSize(
                R.dimen.test_pre_bottom_layout_padding_bottom);
        findViewById(R.id.test_pre_bottom_layout).setPadding(paddingSide, 0, paddingSide,
                paddingBottom);
        findViewById(R.id.test_pre_start_the_test).setVisibility(View.VISIBLE);
    }

    private void startTest() {

        updateModuleConsumedStatus();
        Intent intent = new Intent(this, TakeTestActivity.class);
        intent.putExtra(ConstantGlobal.CONTENT_ID, test._id);
        startActivity(intent);

        LocalManager.recordStudyHistory(
                getApplicationContext(),
                test.orgKeyId,
                SessionManager.getInstance(getApplicationContext()).getSessionStringValue(
                        ConstantGlobal.USER_ID), test._id,
                getIntent().getStringExtra(ConstantGlobal.LINK_ID), test.id,
                EntityType.valueOfKey(test.type));
    }

    public void loadPreTestPageContent(boolean addToBackStack) {

        Bundle args = new Bundle();
        args.putInt(CURRENT_SUBJECT_INDEX, currentSubjectIndex);
        args.putSerializable(ConstantGlobal.INFO, testInfo);
        Fragment fragment = new TestPreAttemptPageFragment();
        fragment.setArguments(args);
        addOrReplaceFragment(fragment, "PRE_TEST_PAGE", addToBackStack);
    }

    private void loadPostTestPageContent(boolean addToBackStack) {

        if (test!= null) {
            getSupportActionBar().setTitle(test.name);
        }
        Bundle args = new Bundle();
        args.putString(ConstantGlobal.ENTITY_ID, test.id);
        args.putString(ConstantGlobal.ENTITY_TYPE, test.type);
        args.putSerializable(ConstantGlobal.TEST_METADATA, testInfo);

        TestPostAttemptPageFragment fragment = new TestPostAttemptPageFragment();
        fragment.setArguments(args);
        addOrReplaceFragment(fragment, "TestPostAttemptPageFragment", addToBackStack);
    }

    private void addOrReplaceFragment(Fragment fragment, String fragmentTag, boolean addToBackStack) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        if (addToBackStack) {
            ft.replace(R.id.test_pre_attempt_frame_layout, fragment);
            ft.addToBackStack(fragmentTag);
        } else {
            ft.add(R.id.test_pre_attempt_frame_layout, fragment);
        }

        ft.commit();
    }

    public void setCurrentSubjectIndex(int newIndex) {

        currentSubjectIndex = newIndex;
    }

    public Content getBasicTestInfo() {

        return test;
    }

    public TestExtendedInfo getExtendedTestInfo() {

        return testInfo;
    }

    private TestExtendedInfo loadTestMetadata() {

        test = cDataManager.getContent(contentId);
        testInfo = test == null ? null : (TestExtendedInfo) test.toContentExtendedInfo();

        if (testInfo != null) {
            TestAnalytics analytics = analyticsDataManager.getTestAnalytics(
                    session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID),
                    session.getSessionStringValue(ConstantGlobal.USER_ID), test.id, test.type);

            testInfo.attempteState = analytics != null ? (Long.valueOf(analytics.endTime) > 0 ? AttemptState.ATTEMPTED
                    : AttemptState.INPROGRESS)
                    : AttemptState.UNATTEMPTED;
            Log.d(TAG, " TestAnalytics :" + analytics);
        }
        return testInfo;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        if (outState != null) {
            outState.putInt(CURRENT_SUBJECT_INDEX, currentSubjectIndex);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
        case android.R.id.home:
            onBackPressed();
            return true;
        default:
            return super.onOptionsItemSelected(item);
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
    protected void onDestroy() {

        if (syncer != null) {
            syncer.cancel(true);
            syncer.clear();
        }

        if (testDownloader != null) {
            testDownloader.cancel(true);
            testDownloader = null;
        }
        super.onDestroy();
    }

    private void showTestResultVisiblityPopup() {

        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {

                dialog.dismiss();
            }
        });
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        String message = TextUtils.isEmpty(testInfo.resultVisibilityMessage) ? getString(R.string.test_result_hidden_default_msg)
                : testInfo.resultVisibilityMessage;
        dialog.setMessage(Html.fromHtml(message));
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();

            }
        });
        dialog.show();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);
        if (dialogFragment != null && dialogFragment.isVisible()) {
            dialogFragment.onStart();
        }
    }

    public void updateModuleConsumedStatus(){
        if (StringUtils.isNotEmpty(getIntent().getStringExtra(ConstantGlobal.MODULE_ID))) {
            Log.e("post test ", "page ");
            String moduleId = getIntent().getStringExtra(ConstantGlobal.MODULE_ID);
            String entityId = getIntent().getStringExtra(ConstantGlobal.ENTITY_ID);
            try {
                (new ModuleStatusDataManager(this)).updateModuleEntryStatus(
                        session.getSessionStringValue(ConstantGlobal.USER_ID),
                        session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID), moduleId, entityId,
                        "TEST");
            } catch (Exception e) {
                Log.d(TAG, "Some error occured in updating test of id: " + entityId + " Error: "
                        + e.getMessage());
            }
        }
    }
}
