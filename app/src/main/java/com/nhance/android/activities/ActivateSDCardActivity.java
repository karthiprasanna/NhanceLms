package com.nhance.android.activities;

import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.nhance.android.activities.baseclasses.NhanceBaseActivity;
import com.nhance.android.async.tasks.AccessCodeHandler;
import com.nhance.android.async.tasks.ITaskProcessor;
import com.nhance.android.async.tasks.TestDownloaderWithoutNotification;
import com.nhance.android.async.tasks.AccessCodeHandler.OnAccessButtonClickedHandler;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.BoardDataManager;
import com.nhance.android.db.datamanagers.ContentDataManager;
import com.nhance.android.db.models.entity.Content;
import com.nhance.android.enums.EntityType;
import com.nhance.android.enums.ErrorCode;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.pojos.LibraryContentRes;
import com.nhance.android.pojos.VedantuDBResult;
import com.nhance.android.pojos.content.sdcards.SDCardGroupInfo;
import com.nhance.android.readers.SDCardReader;
import com.nhance.android.utils.JSONUtils;
import com.nhance.android.utils.SQLDBUtil;
import com.nhance.android.utils.VedantuWebUtils;
import com.nhance.android.R;

public class ActivateSDCardActivity extends NhanceBaseActivity implements
        ITaskProcessor<JSONObject>, OnAccessButtonClickedHandler {

    private static final String TAG = "ActivateSDCardActivity";
    private SDCardGroupInfo cardGroupInfo;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_activate_sdcard);

        cardGroupInfo = (SDCardGroupInfo) getIntent().getSerializableExtra(
                SDCardReader.CARD_GROUP_INFO);
        Log.d(TAG, "cardGroupInfo: " + cardGroupInfo);
        if (cardGroupInfo == null) {
            Log.e(TAG, "could not find a valid sdcard group info");
            finish();
            return;
        }

        getSupportActionBar().setTitle(cardGroupInfo.name);

        View accessCodeContainer = findViewById(R.id.access_code_container);

        TextView sdcardGroupName = (TextView) findViewById(R.id.sdcard_group_name);
        sdcardGroupName.setText(cardGroupInfo.name);

        TextView errorNotPartOfProgram = (TextView) findViewById(R.id.error_not_part_of_program);

        if (!cardGroupInfo.isPartOfProgramSection) {
            accessCodeContainer.setVisibility(View.GONE);
            errorNotPartOfProgram.setVisibility(View.VISIBLE);
            errorNotPartOfProgram.setText(R.string.error_not_allowed_to_access_sdcard_content);
            return;
        } else {
            accessCodeContainer.setVisibility(View.VISIBLE);
            errorNotPartOfProgram.setVisibility(View.GONE);
        }

        AccessCodeHandler accessCodeHandler = new AccessCodeHandler(accessCodeContainer, this,
                getApplicationContext(), cardGroupInfo.id, EntityType.SDCARDGROUP.name(), this);
        accessCodeHandler.setUpAccessCodeHandler();

    }

    @Override
    public void onTaskStart(JSONObject result) {

    }

    @Override
    public void onTaskPostExecute(boolean success, JSONObject result) {

        if (result == null) {
            closeProgressBar();
            Toast.makeText(getApplicationContext(), R.string.error_connecting_to_internet,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!success) {
            closeProgressBar();
            ErrorCode errorCode = ErrorCode.valueOfKey(JSONUtils.getString(result,
                    VedantuWebUtils.KEY_ERROR_CODE));

            Toast.makeText(
                    getApplicationContext(),
                    errorCode.getErrorMessageResource() != -1 ? errorCode.getErrorMessageResource()
                            : R.string.error_general, Toast.LENGTH_SHORT).show();
            return;
        }

        result = JSONUtils.getJSONObject(result, VedantuWebUtils.KEY_RESULT);
        final String accessCode = JSONUtils.getString(result, ConstantGlobal.ACCESS_CODE);

        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {

                if (!TextUtils.isEmpty(accessCode)) {
                    final SDCardReader sdCardReader = new SDCardReader(getApplicationContext());
                    sdCardReader.importFileMetadata(cardGroupInfo, accessCode);
                    sdCardReader.updateActiveSDCard(cardGroupInfo, true);
                    cardGroupInfo.activeGroupInfo = new SDCardReader.ActiveGroupInfo(
                            cardGroupInfo.id,
                            session.getSessionStringValue(ConstantGlobal.USER_ID),
                            SessionManager.getMacAddress(getApplicationContext()));
                    sdCardReader.updateGroupInfoFile(cardGroupInfo);
                    downloadAllTests(cardGroupInfo.targetId, cardGroupInfo.targetType);
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            closeProgressBar();
                            sdCardReader.sendBroadCast(true);
                            Toast.makeText(
                                    getApplicationContext(),
                                    String.format(
                                            getResources().getString(R.string.activate_sdcard_msg),
                                            cardGroupInfo.name), Toast.LENGTH_LONG).show();
                            finish();
                        }
                    });
                }
            }
        });
        t.start();

    }

    private void showProgressbar() {

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.access_code_validating_msg));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    private void closeProgressBar() {

        if (progressDialog != null) {
            progressDialog.cancel();
            progressDialog = null;
        }
    }

    /**
     * @param targetId
     * @param targetType NOTE: this function should only be called from a background thread
     */
    public void downloadAllTests(String targetId, String targetType) {

        ContentDataManager cDataManager = new ContentDataManager(getApplicationContext());
        VedantuDBResult<LibraryContentRes> result = cDataManager.getLibraryContents(session.getSessionStringValue(ConstantGlobal.USER_ID),
                session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID), targetId, targetType, EntityType.TEST.name(),
                null, null, BoardDataManager.NO_BRD_ID, null, null, SQLDBUtil.NO_START, SQLDBUtil.NO_LIMIT, null, false, null);
        Log.d(TAG, "no of test to be downloaded: " + result.totalHits);
        for (Content test : result.entities) {
            TestDownloaderWithoutNotification testDownloader = new TestDownloaderWithoutNotification(
                    test, getApplicationContext());
            testDownloader.runOnCallingThread = true;
            testDownloader.download();
            if (testDownloader.isDownloadFinished()) {
                test.downloaded = true;
                ContentValues tobeUpdatedValues = new ContentValues();
                tobeUpdatedValues.put(ConstantGlobal.DOWNLOADED, 1);
                cDataManager.updateContent(test._id, tobeUpdatedValues);
            }
        }
    }

    @Override
    public void onButtonClicked() {

        showProgressbar();

    }
}
