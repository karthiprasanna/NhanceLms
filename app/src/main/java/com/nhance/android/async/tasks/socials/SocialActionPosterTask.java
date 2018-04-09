package com.nhance.android.async.tasks.socials;

import java.io.IOException;

import org.json.JSONObject;

import android.util.Log;
import android.widget.ProgressBar;

import com.nhance.android.async.tasks.AbstractVedantuJSONAsyncTask;
import com.nhance.android.async.tasks.ITaskProcessor;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.utils.VedantuWebUtils;

public class SocialActionPosterTask extends AbstractVedantuJSONAsyncTask {

    private final String               TAG = "PostSocialActionTask";
    private ITaskProcessor<JSONObject> taskProcessor;

    public SocialActionPosterTask(SessionManager session, ProgressBar progressUpdater,
            ReqAction reqAction, ITaskProcessor<JSONObject> taskProcessor) {

        super(session, progressUpdater);
        this.url = reqAction.getUrl(session);
        this.taskProcessor = taskProcessor;
    }

    @Override
    protected JSONObject doInBackground(String... params) {

        session.addSessionParams(httpParams);
        session.addContentSrcParams(httpParams);
        JSONObject res = null;
        try {
            res = VedantuWebUtils.getJSONData(url, VedantuWebUtils.POST_REQ, httpParams);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            error = checkForError(res);
        }

        if (taskProcessor != null) {
            taskProcessor.onTaskStart(res);
        }
        return res;
    }

    @Override
    public void clear() {

        super.clear();
        taskProcessor = null;
    }

    @Override
    protected void onPostExecute(JSONObject result) {

        super.onPostExecute(result);

        if (taskProcessor != null) {
            taskProcessor.onTaskPostExecute(!error && !isCancelled() && result != null, result);
        }
        clear();
    }

    public static enum ReqAction {
        ADD_COMMENT {

            @Override
            public String getUrl(SessionManager session) {

                return session.getApiUrl("addComment");
            }
        },
        UP_VOTE {

            @Override
            public String getUrl(SessionManager session) {

                return session.getApiUrl("upVote");
            }
        },
        FOLLOW {

            @Override
            public String getUrl(SessionManager session) {

                return session.getApiUrl("follow");
            }
        },
        UNFOLLOW {

            @Override
            public String getUrl(SessionManager session) {

                return session.getApiUrl("unFollow");
            }
        },
        VIEW {

            @Override
            public String getUrl(SessionManager session) {

                return session.getApiUrl("view");
            }
        },
        ADD_ORG_MEMBER {

            @Override
            public String getUrl(SessionManager session) {

                return session.getApiUrl("addOrgMember");
            }
        },
        ADD_ORG_MEMBER_MAPPING {

            @Override
            public String getUrl(SessionManager session) {

                return session.getApiUrl("addOrgMemberMapping");
            }
        },
        START_TRANSACTION {

            @Override
            public String getUrl(SessionManager session) {

                return session.getApiUrl("startTransaction");
            }
        },
        UPDATE_TRANSACTION {

            @Override
            public String getUrl(SessionManager session) {

                return session.getApiUrl("updateTransaction");
            }
        };

        public abstract String getUrl(SessionManager session);

    }
}
