package com.nhance.android.activities.tests;

import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.nhance.android.activities.baseclasses.NhanceBaseActivity;
import com.nhance.android.async.tasks.IDownloadCompleteProcessor;
import com.nhance.android.async.tasks.TestDownloader;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.ContentDataManager;
import com.nhance.android.db.models.entity.Content;
import com.nhance.android.fragments.tests.TestPostAttemptPageFragment;
import com.nhance.android.pojos.content.infos.TestExtendedInfo;
import com.nhance.android.utils.GoogleAnalyticsUtils;
import com.nhance.android.R;

public class TestTeacherPageActivity extends NhanceBaseActivity {

    private ContentDataManager  cDataManager;
    private Content             test;
    private TestExtendedInfo    testInfo;
    private static final String TAG                     = "TestTeacherPageActivity";
    private ProgressDialog      dialog;
    private boolean             isDestroyed             = false;
    private boolean             mDestroyedForReCreation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_test_teacher_page);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        GoogleAnalyticsUtils.setCustomDimensions(
                getIntent().getStringExtra(ConstantGlobal.ENTITY_ID),
                getIntent().getStringExtra(ConstantGlobal.NAME), ConstantGlobal.TEST);
        String title = getIntent().getStringExtra(ConstantGlobal.NAME);
        getSupportActionBar().setTitle(title);
        fillTestContent();
        loadOrDownloadTest();
    }

    private void loadOrDownloadTest() {

        if (!test.downloaded) {
            dialog = new ProgressDialog(this);
            dialog.setCancelable(true);
            dialog.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {

                    finish();
                }
            });
            dialog.setMessage(getResources().getString(R.string.downloading_test_result_msg));
            dialog.show();
            TestDownloader testDownloader = new TestDownloader(test, getApplicationContext(),
                    new IDownloadCompleteProcessor<JSONObject>() {

                        @Override
                        public JSONObject onComplete(JSONObject result, boolean completed) {

                            if (completed && !isDestroyed) {
                                test.downloaded = true;
                                ContentValues tobeUpdatedValues = new ContentValues();
                                tobeUpdatedValues.put(ConstantGlobal.DOWNLOADED, 1);
                                cDataManager.updateContent(test._id, tobeUpdatedValues);
                                if (dialog != null) {
                                    dialog.dismiss();
                                }
                                loadPostTestPageContent();
                            }
                            return null;
                        }
                    });
            testDownloader.executeTask(false);
        } else {
            loadPostTestPageContent();
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {

        mDestroyedForReCreation = true;
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onResume() {

        if (mDestroyedForReCreation) {
            mDestroyedForReCreation = false;
            loadOrDownloadTest();

        }
        super.onResume();
    }

    private void fillTestContent() {

        cDataManager = new ContentDataManager(getApplicationContext());
        int contentId = getIntent().getIntExtra(ConstantGlobal.CONTENT_ID, -1);
        if (contentId == -1) {
            Toast.makeText(getApplicationContext(), "There is some problem in opening the test.",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
        test = cDataManager.getContent(contentId);
        testInfo = test == null ? null : (TestExtendedInfo) test.toContentExtendedInfo();
    }

    private void loadPostTestPageContent() {

        if (mDestroyedForReCreation) {
            return;
        }
        Bundle args = new Bundle();
        args.putString(ConstantGlobal.ENTITY_ID, test.id);
        args.putString(ConstantGlobal.ENTITY_TYPE, test.type);
        Log.d(TAG, "TEST INFO ========== " + testInfo);
        args.putSerializable(ConstantGlobal.TEST_METADATA, testInfo);

        TestPostAttemptPageFragment fragment = new TestPostAttemptPageFragment();
        fragment.setArguments(args);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.test_teacher_fragment_layout, fragment);
        ft.addToBackStack("TestPostAttemptPageFragment");
        ft.commit();
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
    public void onDestroy() {

        isDestroyed = true;
        super.onDestroy();
    }
}
