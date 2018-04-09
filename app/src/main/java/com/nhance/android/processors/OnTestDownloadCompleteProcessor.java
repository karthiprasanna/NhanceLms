package com.nhance.android.processors;

import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.nhance.android.async.tasks.IDownloadCompleteProcessor;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.ContentDataManager;
import com.nhance.android.db.models.entity.Content;

public class OnTestDownloadCompleteProcessor implements IDownloadCompleteProcessor<JSONObject> {

    private final String TAG = "OnTestDownloadCompleteProcessor";
    private Content      content;
    private Context      context;

    public OnTestDownloadCompleteProcessor(Content content, Context context) {

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
