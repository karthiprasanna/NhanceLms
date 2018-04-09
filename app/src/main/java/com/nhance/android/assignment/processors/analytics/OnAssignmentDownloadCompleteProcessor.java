package com.nhance.android.assignment.processors.analytics;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.nhance.android.async.tasks.IDownloadCompleteProcessor;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.ContentDataManager;
import com.nhance.android.db.models.entity.Content;

import org.json.JSONObject;

/**
 * Created by Himank Shah on 12/9/2016.
 */

public class OnAssignmentDownloadCompleteProcessor implements IDownloadCompleteProcessor<JSONObject> {

    private final String TAG = "OnAssignmentDownloadCompleteProcessor";
    private Content content;
    private Context context;

    public OnAssignmentDownloadCompleteProcessor(Content content, Context context) {

        super();
        this.content = content;
        this.context = context;
    }

    @Override
    public JSONObject onComplete(JSONObject result, boolean completed) {

        Log.d(TAG, "downloaded file[" + result + "]");
        if (!completed) {
            Log.d(TAG, "download was not successfull");
            return null;
        }

        ContentDataManager cDataManager = new ContentDataManager(context);
        content.downloaded = completed;
        ContentValues tobeUpdatedValues = new ContentValues();
        tobeUpdatedValues.put(ConstantGlobal.DOWNLOADED, 1);
        cDataManager.updateContent(content._id, tobeUpdatedValues);

        return result;
    }

}
