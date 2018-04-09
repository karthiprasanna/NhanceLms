package com.nhance.android.async.tasks;

import java.io.IOException;

import org.json.JSONObject;

import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ProgressBar;

import com.nhance.android.managers.SessionManager;
import com.nhance.android.utils.JSONAware;
import com.nhance.android.utils.JSONUtils;
import com.nhance.android.utils.VedantuWebUtils;

public class CachedWebDataFetcherTask<T extends JSONAware> extends AbstractVedantuJSONAsyncTask {

    private final String                       TAG          = "CachedWebDataFetcherTask";
    private static final int                   DEFAULT_SIZE = 10;
    private static LruCache<String, JSONAware> mMemoryCache = new LruCache<String, JSONAware>(50);

    private ICachedTaskProcessor<T>            taskProcessor;
    private String                             cacheKey;
    private boolean                            useCache;
    private Class<T>                           resultClass;
    private int                                start;
    private int                                size;

    public CachedWebDataFetcherTask(SessionManager session, ProgressBar progressUpdater,
            ReqAction reqAction, ICachedTaskProcessor<T> taskProcessor, boolean useCache,
            String cacheKey, Class<T> resultClass) {

        super(session, progressUpdater);
        this.progressUpdater = progressUpdater;
        this.url = reqAction.getUrl(session);
        this.taskProcessor = taskProcessor;
        this.cacheKey = cacheKey;
        this.useCache = useCache;
        this.resultClass = resultClass;
    }

    public CachedWebDataFetcherTask(SessionManager session, ProgressBar progressUpdater,
            ReqAction reqAction, ICachedTaskProcessor<T> taskProcessor, boolean useCache,
            String cacheKey, Class<T> resultClass, int start, int size) {

        super(session, progressUpdater);
        this.progressUpdater = progressUpdater;
        this.url = reqAction.getUrl(session);
        this.taskProcessor = taskProcessor;
        this.cacheKey = cacheKey;
        this.useCache = useCache;
        this.resultClass = resultClass;
        this.start = start;
        this.size = size;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onPreExecute() {

        super.onPreExecute();
        if (useCache && !TextUtils.isEmpty(cacheKey) && taskProcessor != null) {
            taskProcessor.onResultDataFromCache((T) mMemoryCache.get(getLocalCacheKey(cacheKey)));
        }
    }

    @Override
    protected JSONObject doInBackground(String... params) {

        httpParams.put("start", start);
        size = size == 0 ? DEFAULT_SIZE : size;
        httpParams.put("size", size);
        session.addSessionParams(httpParams);
        session.addContentSrcParams(httpParams);

        JSONObject res = null;
        try {
            res = VedantuWebUtils.getJSONData(url, VedantuWebUtils.GET_REQ, httpParams);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            error = checkForError(res);
        }

        return res;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onPostExecute(JSONObject result) {

        super.onPostExecute(result);
        if (taskProcessor != null) {
            JSONAware resultData = null;
            if (resultClass != null) {
                try {
                    resultData = resultClass.newInstance();
                    result = JSONUtils.getJSONObject(result, VedantuWebUtils.KEY_RESULT);
                    resultData.fromJSON(result);
                    if (useCache && !TextUtils.isEmpty(cacheKey)) {
                        mMemoryCache.put(getLocalCacheKey(cacheKey), resultData);
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
            taskProcessor.onTaskPostExecute(!error && !isCancelled() && resultData != null,
                    (T) resultData);
        }
        clear();
    }

    @Override
    public void clear() {

        taskProcessor = null;
        super.clear();
    }

    private String getLocalCacheKey(String key) {

        return url + "/" + key;
    }

    public static enum ReqAction {
        GET_ENTITY_LEADER_BOARD {

            @Override
            public String getUrl(SessionManager session) {

                return session.getApiUrl("getEntityLeaderBoard");
            }
        },
        GET_ENTITY_MARK_DISTRIBUTION {

            @Override
            public String getUrl(SessionManager session) {

                return session.getApiUrl("getEntityMarkDistribution");
            }
        },
        GET_ENTITY_QUESTION_ATTEMPT {

            @Override
            public String getUrl(SessionManager session) {

                return session.getApiUrl("getEntityQuestionAttempts");
            }
        },


        GET_TEST_DETAILS {

            @Override
            public String getUrl(SessionManager session) {

                return session.getApiUrl("getTestInfo");
            }
        },

        //karthi
        GET_ASSIGNMENT_DETAILS {

            @Override
            public String getUrl(SessionManager session) {

                return session.getApiUrl("getAssignmentInfo");
            }
        },

        GET_ENTITY_ASSIGNMENT_QUESTION_ATTEMPT {

            @Override
            public String getUrl(SessionManager session) {

                return session.getApiUrl("getEntityQuestionAttempts");
            }
        },


        ENTITY_SCHEDULE_ANALYTICS {

            @Override
            public String getUrl(SessionManager session) {

                return session.getApiUrl("getEntityScheduleAnalytics");
            }
        };

        public abstract String getUrl(SessionManager session);
    }
}
