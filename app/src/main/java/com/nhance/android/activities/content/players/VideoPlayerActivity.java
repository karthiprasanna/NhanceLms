package com.nhance.android.activities.content.players;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nhance.android.activities.baseclasses.NhanceBaseActivity;
import com.nhance.android.adapters.video.VideoCommentsAdapter;
import com.nhance.android.async.tasks.AbstractVedantuJSONAsyncTask;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.ContentDataManager;
import com.nhance.android.db.datamanagers.ModuleStatusDataManager;
import com.nhance.android.enums.ContentType;
import com.nhance.android.enums.EntityType;
import com.nhance.android.managers.FileManager;
import com.nhance.android.managers.LocalManager;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.pojos.LibraryContentRes;
import com.nhance.android.pojos.content.infos.VideoExtendedInfo;
import com.nhance.android.recentActivities.Actor;
import com.nhance.android.utils.VedantuWebUtils;
import com.nhance.android.utils.ViewUtils;
import com.nhance.android.R;

public class VideoPlayerActivity extends NhanceBaseActivity {

    private static final String TAG      = "VideoPlayerActivity";
    private String              videoUrl = null;

    ImageView                   mVideoThumb;
//    View                        mVideoInfoContainer;
    View                        mVideoStatsContainer;
    private LibraryContentRes   mVideo;
    private VideoExtendedInfo   vInfo;
    ContentDataManager          mContentDataManager;
    private RecyclerView        recyclerView;
    private List<Actor>         commentsList;
    private SessionManager      sessionManager;
    private VideoCommentsAdapter videoCommentsAdapter;
    private LinearLayoutManager mLayoutManager;
    private EditText userInput;
    private static final int ADDNEWVIDEO = 102;
    private String videoId;
    private int commentsCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        startVServer();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video_player);
        sessionManager = SessionManager.getInstance(this);
        String linkId = getIntent().getStringExtra(ConstantGlobal.LINK_ID);

        mContentDataManager = new ContentDataManager(getApplicationContext());
        mVideo = mContentDataManager.getLibraryContentRes(linkId);
        if (mVideo == null) {
            Toast.makeText(getBaseContext(), "No Video Found", Toast.LENGTH_LONG).show();
            onStop();
        }
        vInfo = (VideoExtendedInfo) mVideo.toContentExtendedInfo();
        videoId = mVideo.id;

        setUpNoteListFragment();
        mVideoThumb = (ImageView) findViewById(R.id.video_thum);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(VideoPlayerActivity.this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHasFixedSize(true);
        commentsList = new ArrayList<Actor>();
        videoCommentsAdapter = new VideoCommentsAdapter(VideoPlayerActivity.this, commentsList, videoId, mVideo);
        recyclerView.setAdapter(videoCommentsAdapter);
        String thumbFromSDCard = FileManager.getSDCardUrl(mVideo,
                StringUtils.substringAfterLast(mVideo.thumb, File.separator), null, null,
                getApplicationContext(), true);
        if (StringUtils.isNotEmpty(thumbFromSDCard)) {
            LocalManager.downloadImage(thumbFromSDCard, mVideoThumb, false);
        } else {
            LocalManager.downloadImage(mVideo.thumb, mVideoThumb, true);
        }

        Log.d(TAG, " Video content : " + mVideo);

        videoUrl = vInfo.url;

        if (videoUrl.startsWith("https:")) {
            videoUrl = videoUrl.replace("https:", "http:");
        }

//        mVideoInfoContainer = findViewById(R.id.video_info_container);
        mVideoStatsContainer = findViewById(R.id.video_stats_container);
        findViewById(R.id.video_play_button).setOnClickListener(getVideoPlayListener());
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        new GetCommentsAsyncTask(sessionManager, null, mVideo.id).executeTask(false);
        setVideoInfo();

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menu.add(0, R.id.action_add, 0,
                getResources().getString(R.string.add))
                .setIcon(R.drawable.ic_add)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        super.onCreateOptionsMenu(menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // this takes the user 'back', as if they pressed the left-facing triangle icon on the main android toolbar.
                // if this doesn't work as desired, another possibility is to call `finish()` here.
                super.onBackPressed();
                return true;

            case  R.id.action_add:
                Intent intent = new Intent(VideoPlayerActivity.this, AddVideoCommentActivity.class);
                 intent.putExtra("videoId", mVideo.id);
                this.startActivityForResult(intent, ADDNEWVIDEO);

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setUpNoteListFragment() {

        Bundle bundle = getIntent().getExtras() != null ? getIntent().getExtras() : new Bundle();
        bundle.putString(ConstantGlobal.NAME, mVideo.name);
        bundle.putString(ConstantGlobal.ENTITY_ID, mVideo.id);
        bundle.putString(ConstantGlobal.ENTITY_TYPE, mVideo.type);
        bundle.putString(ConstantGlobal.CONTENT_TYPE, vInfo.contentType);
        ContentType contentType = ContentType.valueOfKey(vInfo.contentType);
        String thumb = contentType == ContentType.ANIMATION ? mVideo.thumb : vInfo.uuid;
        bundle.putString(ConstantGlobal.THUMB, thumb);
        // TODO: add boardId and boardName
        // bundle.putString(ConstantGlobal.COURSE_BRD_NAME,
        // video.courseBrdName);
        // bundle.putString(ConstantGlobal.COURSE_BRD_ID, video.courseBrdId);
        bundle.putString(ConstantGlobal.BY, mVideo.ownerName);
        getIntent().putExtras(bundle);
    }

    private void setVideoInfo() {

//        ViewUtils.setTextViewValue(mVideoInfoContainer, R.id.video_name, mVideo.name);
//        ViewUtils.setTextViewValue(mVideoInfoContainer, R.id.video_added_by, mVideo.ownerName);
//        TextView desc = (TextView) mVideoInfoContainer.findViewById(R.id.video_desc);
//
//        if (StringUtils.isNotEmpty(mVideo.desc) && !mVideo.desc.trim().equalsIgnoreCase("null")) {
//            desc.setText(Html.fromHtml(mVideo.desc));
//        } else {
//            desc.setVisibility(View.GONE);
//        }
        int duration = (int) (vInfo.duration / DateUtils.SECOND_IN_MILLIS);
        if (duration > 0) {
            ViewUtils.setTextViewValue(mVideoStatsContainer, R.id.video_duration,
                    LocalManager.getDurationString(duration));
        } else if (mVideoStatsContainer != null) {

            mVideoStatsContainer.findViewById(R.id.video_duration).setVisibility(View.GONE);
        }
    }

    private View.OnClickListener getVideoPlayListener() {

        return new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Log.d(TAG, "starting video");

                String vUrl = FileManager.getContentLocalUrl(mVideo, getApplicationContext());
                boolean isFromSDCard = false;
                if (TextUtils.isEmpty(vUrl)) {
                    vUrl = FileManager.getSDCardUrl(  mVideo, FileManager
                            .getEncryptedFileName(StringUtils.substringAfterLast(videoUrl,
                                    File.separator)), null, null, getApplicationContext(), false);
                    Log.d(TAG, "starting  vUrl -----> " + vUrl);
                }

                  if (!mVideo.downloaded && TextUtils.isEmpty(vUrl) && !SessionManager.isOnline()) {
                    Log.e(TAG, "internet connection not available");
                    Toast.makeText(getApplicationContext(),
                            "Internet connectivity is required to play this video",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                VideoExtendedInfo vInfo = (VideoExtendedInfo) mVideo.toContentExtendedInfo();
                Class<?> activityClass = "swf".equalsIgnoreCase(vInfo.contentType) ? SWFPlayerActivity.class
                        : VideoPlayerFullScreenActivity.class;

                Intent intent = new Intent(getApplication(), activityClass);
                intent.putExtra(ConstantGlobal.CONTENT_ID, mVideo._id);
                intent.putExtra(ConstantGlobal.LINK_TYPE, mVideo.subType);
                intent.putExtra(ConstantGlobal.DOWNLOADED, mVideo.downloaded);
                intent.putExtra(ConstantGlobal.THUMB, mVideo.thumb);
                intent.putExtra(ConstantGlobal.ENC_LEVEL, mVideo.encLevel);
                intent.putExtra(ConstantGlobal.LINK_ID, mVideo.linkId);
                intent.putExtra("SD_CARD", isFromSDCard);

                if (!TextUtils.isEmpty(vUrl)) {
                    videoUrl = vUrl;
                }

                intent.putExtra(ConstantGlobal.VIDEO_URL, videoUrl);

                startActivity(intent);

                if (StringUtils.isNotEmpty(getIntent().getStringExtra(ConstantGlobal.MODULE_ID))) {
                    String moduleId = getIntent().getStringExtra(ConstantGlobal.MODULE_ID);
                    String entityId = getIntent().getStringExtra(ConstantGlobal.ENTITY_ID);
                    try {
                        (new ModuleStatusDataManager(VideoPlayerActivity.this))
                                .updateModuleEntryStatus(
                                        session.getSessionStringValue(ConstantGlobal.USER_ID),
                                        session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID),
                                        moduleId, entityId, "VIDEO");
                    } catch (Exception e) {
                        Log.d(TAG, "Some error occured in updating video of id: " + entityId
                                + " Error: " + e.getMessage());
                    }
                }
                if (mVideo != null) {
                    LocalManager.recordStudyHistory(getApplicationContext(), mVideo.orgKeyId,
                            SessionManager.getInstance(getApplicationContext())
                                    .getSessionStringValue(ConstantGlobal.USER_ID), mVideo._id,
                            mVideo.linkId, mVideo.id, EntityType.valueOfKey(mVideo.type));
                    mContentDataManager.updateLastViewed(mVideo._id);
                }
            }
        };
    }


    public class GetCommentsAsyncTask extends AbstractVedantuJSONAsyncTask {

        private ProgressDialog pDialog;
        private String entityId;
//        private List<Actor> commentsActorsList = new ArrayList<Actor>();

        public GetCommentsAsyncTask(SessionManager session, ProgressBar progressUpdater, String rootId) {
            super(session, progressUpdater);
            this.url = session.getApiUrl("getComments");
            this.entityId = rootId;
            pDialog = new ProgressDialog(VideoPlayerActivity.this);
            pDialog.setMessage("Please wait..");
            pDialog.setCancelable(false);
            if(!isFinishing())
            {
                pDialog.show();
            }


            Log.e("getCommentsAsyncTask", "call");
        }

        @Override
        protected JSONObject doInBackground(String... params) {


            session.addSessionParams(httpParams);
            httpParams.put("callingApp", "TapApp");
            httpParams.put("orderBy", "timeCreated");
            httpParams.put("callingUserId", session.getOrgMemberInfo().userId);
            httpParams.put("sortOrder", "DESC");
            httpParams.put("rootType", "VIDEO");
            httpParams.put("userRole", session.getOrgMemberInfo().profile);
            httpParams.put("callingAppId", "TapApp");
            httpParams.put("start", 0);
            httpParams.put("size", 10);
            httpParams.put("orgId",session.getOrgMemberInfo().orgId);
            httpParams.put("parent.type","VIDEO");
            httpParams.put("userId",session.getOrgMemberInfo().userId);
            httpParams.put("parent.id",entityId);
            httpParams.put("rootId", entityId);
            httpParams.put("myUserId",session.getOrgMemberInfo().userId);
            httpParams.put("memberId",session.getOrgMemberInfo().memberId);



            JSONObject res = null;
            try {
                res = VedantuWebUtils.getJSONData(url, VedantuWebUtils.GET_REQ, httpParams);
                Log.e("getCommentsAsyncTask", "do in background");

            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            } finally {
                error = checkForError(res);
            }

            if (isCancelled()) {
                Log.i(TAG, "task cancled by user");
                pDialog.dismiss();
                return null;
            }


            return res;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            if(jsonObject != null) {
                try {
                    JSONObject result = jsonObject.getJSONObject("result");

                    JSONArray list = result.getJSONArray("list");

                    if (list != null) {
                        Log.e("list", ".." + list.toString());
                        for (int i = 0; i < list.length(); i++) {
                            Actor actor = new Actor();
                            actor.message = ((JSONObject) list.get(i)).getString("content");
                            JSONObject user = ((JSONObject) list.get(i)).getJSONObject("user");
                            actor.firstName = user.getString("firstName");
                            actor.lastName = user.getString("lastName");
                            actor.thumbnail = user.getString("thumbnail");
                            actor.profile = user.getString("profile");
                            actor.type = ((JSONObject) list.get(i)).getString("type");
                            actor.id = ((JSONObject) list.get(i)).getString("id");
                            actor.upVoted = ((JSONObject) list.get(i)).getBoolean("voted");
                            actor.dateTime = ((JSONObject) list.get(i)).getString("timeCreated");
                            actor.upVoteCount = ((JSONObject) list.get(i)).getInt("upVotes");
                            actor.replyCount = ((JSONObject) list.get(i)).getInt("comments");
                            commentsList.add(actor);
                        }

                        videoCommentsAdapter.notifyDataSetChanged();

                        Log.e("commentsActorsList", ".." + commentsList.toString());


                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
            pDialog.dismiss();


        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == ADDNEWVIDEO) && data != null) {
            String arg = data.getStringExtra("bundle");
            JSONObject obj = null;
            try {
                obj = new JSONObject(arg);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.e("json add comment......", "called" + obj);
            Actor actor = new Actor();
            try {
                actor.message = obj.getString("content");

                JSONObject user = obj.getJSONObject("user");
                actor.firstName = user.getString("firstName");
                actor.lastName = user.getString("lastName");
                actor.thumbnail = user.getString("thumbnail");
//            actor.profile = user.getString("profile");
                actor.type = obj.getString("type");
                actor.id = obj.getString("id");
                actor.upVoted = obj.getBoolean("voted");
                actor.dateTime = obj.getString("timeCreated");
                actor.upVoteCount = obj.getInt("upVotes");
                actor.replyCount = obj.getInt("comments");
                commentsList.add(1, actor);
                videoCommentsAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }

//            getAllActivities(eType, eId);
        } else if (requestCode == 100 && data != null) {
            Log.e("onActivityResult......", "called" + data.getIntExtra("position", 0));
            int position = data.getIntExtra("position", 0);
            int count = data.getIntExtra("count", 0);
            if (count != 0) {
                Actor object = commentsList.get(data.getIntExtra("position", 0));
                commentsCount = object.replyCount;
                object.replyCount = commentsCount + count;
                commentsList.remove(position);
                commentsList.add(position, object);
                videoCommentsAdapter.notifyDataSetChanged();

            }
        }
    }
}
