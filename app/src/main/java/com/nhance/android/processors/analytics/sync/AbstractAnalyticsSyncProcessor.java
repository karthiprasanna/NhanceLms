package com.nhance.android.processors.analytics.sync;

import org.json.JSONObject;

import android.content.Context;

import com.nhance.android.async.tasks.AbstractTestDownloader;
import com.nhance.android.async.tasks.ITaskProcessor;
import com.nhance.android.db.models.entity.Content;

public abstract class AbstractAnalyticsSyncProcessor extends AbstractTestDownloader {

    protected ITaskProcessor<JSONObject> taskProcessor;

    public AbstractAnalyticsSyncProcessor(Context context, Content content,
            ITaskProcessor<JSONObject> taskProcessor) {

        super(content, context);
        this.taskProcessor = taskProcessor;
    }
}
