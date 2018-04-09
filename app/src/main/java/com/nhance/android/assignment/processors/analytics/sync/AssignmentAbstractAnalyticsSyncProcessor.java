package com.nhance.android.assignment.processors.analytics.sync;

import android.content.Context;

import com.nhance.android.assignment.async.tasks.AbstractAssignmentDownloader;
import com.nhance.android.async.tasks.ITaskProcessor;
import com.nhance.android.db.models.entity.Content;

import org.json.JSONObject;

/**
 * Created by Himank Shah on 12/6/2016.
 */

public class AssignmentAbstractAnalyticsSyncProcessor extends AbstractAssignmentDownloader {

    protected ITaskProcessor<JSONObject> taskProcessor;

    public AssignmentAbstractAnalyticsSyncProcessor(Context context, Content content,
                                                    ITaskProcessor<JSONObject> taskProcessor) {

        super(content, context);
        this.taskProcessor = taskProcessor;
    }
}
