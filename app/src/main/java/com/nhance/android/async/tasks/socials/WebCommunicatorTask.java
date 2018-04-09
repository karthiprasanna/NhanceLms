package com.nhance.android.async.tasks.socials;

import java.io.IOException;

import org.json.JSONObject;

import android.util.Log;
import android.widget.ProgressBar;

import com.nhance.android.async.tasks.AbstractVedantuJSONAsyncTask;
import com.nhance.android.async.tasks.ITaskProcessor;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.utils.VedantuWebUtils;

public class WebCommunicatorTask extends AbstractVedantuJSONAsyncTask {

    private static final String        TAG          = "SocialActionListFetcherTask";
    private static final int           DEFAULT_SIZE = 20;

    private ITaskProcessor<JSONObject> taskProcessor;
    private int                        start;
    private int                        size;
    private String                     orderBy;
    private String                     sortOrder;

    public WebCommunicatorTask(SessionManager session, ProgressBar progressUpdater,
            ReqAction reqAction, ITaskProcessor<JSONObject> taskProcessor) {

        this(session, progressUpdater, reqAction, taskProcessor, null, null, 0);
    }

    public WebCommunicatorTask(SessionManager session, ProgressBar progressUpdater,
            ReqAction reqAction, ITaskProcessor<JSONObject> taskProcessor, String orderBy,
            String sortOrder, int start) {

        this(session, progressUpdater, reqAction, taskProcessor, orderBy, sortOrder, start,
                DEFAULT_SIZE);
    }

    public WebCommunicatorTask(SessionManager session, ProgressBar progressUpdater,
            ReqAction reqAction, ITaskProcessor<JSONObject> taskProcessor, String orderBy,
            String sortOrder, int start, int size) {

        super(session, progressUpdater);
        this.url = reqAction.getUrl(session);
        this.taskProcessor = taskProcessor;
        this.orderBy = orderBy;
        this.sortOrder = sortOrder;
        this.start = start;
        this.size = size;

    }

    @Override
    protected JSONObject doInBackground(String... params) {

        session.addSessionParams(httpParams);
        session.addContentSrcParams(httpParams);
        httpParams.put("start", start);
        httpParams.put("size", size);
        httpParams.put("orderBy", orderBy);
        httpParams.put("sortOrder", sortOrder);

        JSONObject res = null;
        try {
            res = VedantuWebUtils.getJSONData(url, VedantuWebUtils.GET_REQ, httpParams);
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
    protected void onPostExecute(JSONObject result) {

        super.onPostExecute(result);
        if (taskProcessor != null) {
            taskProcessor.onTaskPostExecute(!error && !isCancelled() && result != null, result);
        }
        clear();
    }

    @Override
    public void clear() {

        taskProcessor = null;
        orderBy = null;
        sortOrder = null;
        super.clear();
    }

    public static enum ReqAction {

        GET_COMMENTS {

            @Override
            public String getUrl(SessionManager session) {

                return session.getApiUrl("getComments");
            }
        },

        GET_SIMILAR_DOUBTS {

            @Override
            public String getUrl(SessionManager session) {

                return session.getApiUrl("getSimilarDiscussions");
            }
        },

        GET_FOLLOWERS {

            @Override
            public String getUrl(SessionManager session) {

                return session.getApiUrl("getFollowers");
            }
        },
        GET_BOARDS {

            @Override
            public String getUrl(SessionManager session) {

                return session.getApiUrl("getBoards");
            }
        },
        GET_ENTITY_LEADER_BOARD {

            @Override
            public String getUrl(SessionManager session) {

                return session.getApiUrl("getEntityLeaderBoard");
            }
        },
        GET_USER_ENTITY_RANK {

            @Override
            public String getUrl(SessionManager session) {

                return session.getApiUrl("getUserEntityRank");
            }
        },

        GET_ORG_CATEGORIES {

            @Override
            public String getUrl(SessionManager session) {

                return session.getApiUrl("getOrgCategories");
            }
        },
        GET_CATEGORY_SECTIONS {

            @Override
            public String getUrl(SessionManager session) {

                return session.getApiUrl("getCategorySections");
            }
        },
        GET_ORG_MEMBER_SIGN_UP_EXTRA_INPUT_FIELDS {

            @Override
            public String getUrl(SessionManager session) {

                return session.getApiUrl("getOrgMemberExtraInputFields");
            }
        },
        GET_PROGRAM_COURSES {

            @Override
            public String getUrl(SessionManager session) {

                return session.getApiUrl("getProgramCourses");
            }
        },
        GET_ORG_MEMBER_PROFILE {

            @Override
            public String getUrl(SessionManager session) {

                return session.getApiUrl("getOrgMemberProfile");
            }
        },
        GET_BUY_ORDERS {

            @Override
            public String getUrl(SessionManager session) {

                return session.getApiUrl("getBuyOrders");
            }
        },
        VERIFY_ACCESS_CODE {

            @Override
            public String getUrl(SessionManager session) {

                return session.getApiUrl("verifyAccessCode");
            }
        };

        public abstract String getUrl(SessionManager session);

    }
}
