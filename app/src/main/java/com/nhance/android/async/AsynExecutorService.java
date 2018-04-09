package com.nhance.android.async;

import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;

import com.nhance.android.utils.ExecutorUtils;

public class AsynExecutorService {

    private static AsynExecutorService INSTANCE;
    @SuppressWarnings("rawtypes")
    public final CompletionService     COMPLETION_SERVICE;

    private <T extends Object> AsynExecutorService() {

        COMPLETION_SERVICE = new ExecutorCompletionService<T>(ExecutorUtils.executor);
    }

    public static AsynExecutorService getInstance() {

        if (INSTANCE == null) {
            createInstance();
        }
        return INSTANCE;
    }

    private static synchronized void createInstance() {

        if (INSTANCE == null) {
            INSTANCE = new AsynExecutorService();
        }
    }
}
