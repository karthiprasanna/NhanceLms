package com.nhance.android.async.tasks;

import android.content.Context;

import com.nhance.android.db.models.entity.Content;

public class TestDownloaderWithoutNotification extends AbstractTestDownloader {

    public TestDownloaderWithoutNotification(Content test, Context context) {

        super(test, context);
    }

}
