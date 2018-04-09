package com.nhance.android.recentActivities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nhance.android.R;
import com.nhance.android.activities.baseclasses.NhanceBaseActivity;
import com.nhance.android.async.tasks.AbstractVedantuJSONAsyncTask;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.ContentDataManager;
import com.nhance.android.db.datamanagers.ContentLinkDataManager;
import com.nhance.android.db.models.entity.ContentLink;
import com.nhance.android.enums.EntityType;
import com.nhance.android.library.utils.LibraryUtils;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.message.Utils.*;
import com.nhance.android.message.Utils.Utility;
import com.nhance.android.notifications.NotificationAdapter;
import com.nhance.android.pojos.LibraryContentRes;
import com.nhance.android.utils.VedantuWebUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class CommentsActivity extends NhanceBaseActivity implements View.OnClickListener{

    private RecyclerView recyclerView;
    private LinearLayoutManager mLayoutManager;
    private ArrayList<Actor> commentsList;
    private CommentsAdapter mAdapter;
    private String rootId, rootType;
    private TextView noCommentsTextView;
    private int position;
    private int commentsCount;
    private String feedId;
    private JSONObject actor;
    private String userName, profile, imageSrc, ownerId;
    private SessionManager sessionManager;
    private int upVotesCount;
    private boolean upvoted, statusFeed;
    private String newsActivityId, statusMessage;

    public TextView youTubeTitleView;
    public TextView youTubeContentView;
    public ImageView youTubeImageView;
    public TextView feedTitleTextView;
    public TextView statusFeedTextView;
    public ImageView statusFeedImageView;
    public TextView userNameTextView;
    public ImageView imgViewIcon;
    public LinearLayout statusFeedViewLayout;
//    public LinearLayout videoViewLayout;
    public LinearLayout youTubeVideoViewLayout;
    public TextView upVotesCountTextView;
    public TextView commentsCountTextView;
    public TextView upVoteTextView;
    public LinearLayout upVoteLayout;
    public ImageView upVoteImageView;
    private boolean error;
    private JSONObject object;
    private boolean fromNotification, fromVideoComments;
    private LinearLayout contentLayout;
    private EditText userInput;
    private FloatingActionButton fabButton;
    private String parentId, parentType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        Utility.initStatusBar(CommentsActivity.this);
        setContentView(R.layout.activity_comments);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Comments");
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        commentsCount = 0;

        recyclerView= (RecyclerView)findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(CommentsActivity.this);


        userNameTextView = (TextView) findViewById(R.id.user_name);
        imgViewIcon = (ImageView) findViewById(R.id.user_profile_image);

        upVotesCountTextView = (TextView) findViewById(R.id.upVoteCount);
        commentsCountTextView = (TextView) findViewById(R.id.commentCount);
        upVoteTextView = (TextView) findViewById(R.id.upVote);
        upVoteImageView = (ImageView) findViewById(R.id.upVoteImage);
        upVoteLayout = (LinearLayout) findViewById(R.id.upVOteLayout);
        contentLayout = (LinearLayout) findViewById(R.id.content_layout);


        statusFeedViewLayout = (LinearLayout) findViewById(R.id.statusFeedView);
        youTubeVideoViewLayout  = (LinearLayout) findViewById(R.id.youTubeVideoView);

        youTubeImageView = (ImageView) findViewById(R.id.you_tube_image);
        youTubeTitleView = (TextView) findViewById(R.id.you_tube_title);
        youTubeContentView = (TextView) findViewById(R.id.you_tube_content);

        feedTitleTextView = (TextView) findViewById(R.id.feed_title);
        statusFeedTextView = (TextView) findViewById(R.id.statusFeedTextView);
        statusFeedImageView = (ImageView) findViewById(R.id.statusFeedImageView);

        youTubeVideoViewLayout.setOnClickListener(this);
        upVoteLayout.setOnClickListener(this);

        recyclerView.setLayoutManager(mLayoutManager);
        commentsList = new ArrayList<Actor>();
        mAdapter = new CommentsAdapter(this, commentsList);
        recyclerView.setAdapter(mAdapter);
//        commentEditText = (EditText)findViewById(R.id.message);

        fabButton = (FloatingActionButton)findViewById(R.id.floatingActionButton);
        rootId = getIntent().getStringExtra("rootId");
        rootType = getIntent().getStringExtra("rootType");
        parentId = getIntent().getStringExtra("parentId");
        parentType = getIntent().getStringExtra("parentType");
        fromVideoComments = getIntent().getBooleanExtra("fromVideoComments", false);
        position = getIntent().getIntExtra("position", -1);
        fromNotification = getIntent().getBooleanExtra("fromNotification", false);

        noCommentsTextView = (TextView)findViewById(R.id.NoCommentsTextView);
        sessionManager = SessionManager.getInstance(this);
        if(fromNotification) {
            contentLayout.setVisibility(View.VISIBLE);
            new GetStatusFeedAsyncTask(sessionManager, null, rootId, rootType).executeTask(false);
        }else{
            contentLayout.setVisibility(View.GONE);
        }

        if(fromVideoComments){
            new GetCommentsAsyncTask(sessionManager, null, parentId, parentType).executeTask(false);
        }else {
            new GetCommentsAsyncTask(sessionManager, null, rootId, rootType).executeTask(false);
        }

        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // get prompts.xml view
                LayoutInflater li = LayoutInflater.from(CommentsActivity.this);
                View promptsView = li.inflate(R.layout.custom_dialog, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        CommentsActivity.this);
                userInput = (EditText) promptsView
                        .findViewById(R.id.message);
                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(promptsView);


                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("Send",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        String commentText = userInput.getText().toString();
                                        Log.e("commentText", ".."+commentText);
                                        // get user input and set it to result
                                        // edit text
//                                        result.setText(userInput.getText());

                                        if(userInput != null && !userInput.getText().toString().trim().isEmpty()){
                                            new PostCommentAsyncTask(SessionManager.getInstance(CommentsActivity.this), null, rootId, rootType,parentId, parentType, userInput.getText().toString(), fromVideoComments).executeTask(false);
                                            userInput.setText("");
                                        }else{
                                            Toast.makeText(CommentsActivity.this, "Please enter comments..", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        dialog.cancel();
                                    }
                                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();

            }
        });
    }

    @Override
    public void onClick(View v) {
            if(v.getId() == youTubeVideoViewLayout.getId()) {

            try {
                JSONObject src = object;
                Log.e("object ", ".."+src.toString());
//                 JSONObject sourceContent = src.getJSONObject("sourceContent");
                String url = src.getString("url");
                Intent intent = new Intent(Intent.ACTION_VIEW,  Uri.parse(url));
//
                startActivity(intent);
            } catch (JSONException e) {
                e.printStackTrace();
            }
//                Toast.makeText(v.getContext(), "ROW PRESSED = " + String.valueOf(getAdapterPosition()), Toast.LENGTH_SHORT).show();
        }else if(v.getId() == upVoteLayout.getId()) {
//            try {

//                upvoted = object.getBoolean("voted");
                if (upvoted) {
                    Log.e("upvoted", ".." + upvoted);
//                    try {
                        new GetUpVoteAsyncTask(sessionManager, null, rootId, rootType).executeTask(false);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }

                } else {
                    Log.e("upvoted", ".." + upvoted);

                        new UpVoteAsyncTask(sessionManager, null, rootId, rootType).executeTask(false);

                    }

//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
////                Toast.makeText(v.getContext(), "ROW PRESSED = " + String.valueOf(getAdapterPosition()), Toast.LENGTH_SHORT).show();
            }

        }

    public class GetStatusFeedAsyncTask extends AbstractVedantuJSONAsyncTask {

        private ProgressDialog pDialog;
        private String entityType;
        private String feedId;
        private String type;

        public  GetStatusFeedAsyncTask(SessionManager session, ProgressBar progressUpdater, String feedId, String entityType) {
            super(session, progressUpdater);
            this.url = session.getApiUrl("getStatusFeed");
            this.feedId = feedId;
            this.entityType = entityType;
            pDialog = new ProgressDialog(CommentsActivity.this);
            pDialog.setMessage("Please wait..");
            pDialog.setCancelable(false);

            pDialog.show();


            Log.e("getStatusFeedAsyncTask", "call");
        }

        @Override
        protected JSONObject doInBackground(String... params) {

//            session.addSessionParams(httpParams);
            httpParams.put("callingApp", "TapApp");
            httpParams.put("callingUserId", session.getOrgMemberInfo().userId);
            httpParams.put("userRole", session.getOrgMemberInfo().profile);
            httpParams.put("callingAppId", "TapApp");
            httpParams.put("orgId",session.getOrgMemberInfo().orgId);
            httpParams.put("feedId",feedId);
            httpParams.put("parent.type","ORGANIZATION");
            httpParams.put("userId",session.getOrgMemberInfo().userId);
            httpParams.put("parent.id",session.getOrgMemberInfo().orgId);
            httpParams.put("myUserId",session.getOrgMemberInfo().userId);
            httpParams.put("memberId",session.getOrgMemberInfo().memberId);

            JSONObject res = null;
            try {
                res = VedantuWebUtils.getJSONData(url, VedantuWebUtils.GET_REQ, httpParams);
                Log.e("getStatusFeedAsyncTask", "do in background"+res.toString());
                Log.d("getStatusFeedAsyncTask",".."+httpParams);

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
            try {
                JSONObject result = jsonObject.getJSONObject("result");
                 object = result.getJSONObject("info");
                Log.e("list", ".." + object.toString());

  try {
//            Log.d("activitiesItem", ""+recentActivitiesList.get(position).getJSONObject("src").toString());
//            JSONObject object = recentActivitiesList.get(position);
//                JSONArray jsonArray = jsonObject.getJSONArray("clusteredNews");
//                JSONObject object = jsonObject.getJSONObject(0);
            Log.e("object in adapter", ".." + object.toString());
            if (object != null && object.has("actor")) {
                actor = object.getJSONObject("actor");

            } else if (object != null) {
                actor = object.getJSONObject("srcOwner");
            }

            userName = actor.getString("firstName");
            userName += " " + actor.getString("lastName");
            profile = actor.getString("profile");
            imageSrc = actor.getString("thumbnail");
            ownerId = actor.getString("id");


            if (profile != null && profile.equalsIgnoreCase("STUDENT")) {
               userNameTextView.setText(userName);
            } else {
                userNameTextView.setText(userName + " (" + profile + ")");
            }
//            src = object.getJSONObject("src");
            upVotesCount = object.getInt("upVotes");
            commentsCount = object.getInt("comments");

            upvoted = object.getBoolean("voted");

            Log.e("upvoted", ".." + object.getBoolean("voted"));

//            newsActivityId = object.getString("newsActivityId");
//
//            Log.e("newsActivityId", ".." + newsActivityId);
            if(object.has("statusMessage")) {
                statusFeed = true;
                try {
                    statusMessage = object.getString("statusMessage");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else{
                statusFeed = false;
            }

            JSONObject sourceCon = object.optJSONObject("sourceContent");
            if (sourceCon != null) {

                object = sourceCon;
            }
            String profile = (sessionManager.getOrgMemberInfo().profile).toString();


            if(upvoted){
                Log.e("upvoted", ".." + upvoted);
                upVoteTextView.setText("Upvoted");
                upVoteTextView.setTextColor(getResources().getColor(R.color.red));
                upVoteImageView.setBackground(getResources().getDrawable(R.drawable.upvoted));
            }else{
                Log.e("upvoted", ".." + upvoted);
                upVoteTextView.setText("Upvote");
                upVoteTextView.setTextColor(getResources().getColor(R.color.orange));
                upVoteImageView.setBackground(getResources().getDrawable(R.drawable.upvote));

            }
            upVotesCountTextView.setText(String.valueOf(upVotesCount));
            commentsCountTextView.setText(String.valueOf(commentsCount));


            Log.e("object in src", ".." + object.toString());
            Log.e("position", ".." + position);
         try {
             type = object.getString("type");
         } catch (JSONException e) {
             e.printStackTrace();
        }

        if(type != null) {

            if (object.getString("type").equalsIgnoreCase("IMAGE")) {
                statusFeedViewLayout.setVisibility(View.VISIBLE);
                youTubeVideoViewLayout.setVisibility(View.GONE);
                if (statusMessage != null)
                    statusFeedTextView.setText(Html.fromHtml(statusMessage));
                Picasso.with(CommentsActivity.this).load(sourceCon.getString("image")).into(statusFeedImageView);
                feedTitleTextView.setVisibility(View.GONE);
            } else if (object.getString("type").equalsIgnoreCase("WEB_PAGE") || object.getString("type").equalsIgnoreCase("LINK_VIDEO")) {
                if (statusMessage != null)
                    feedTitleTextView.setText(Html.fromHtml(statusMessage));
                feedTitleTextView.setVisibility(View.VISIBLE);
                youTubeTitleView.setText(object.getString("title"));
                youTubeContentView.setText(object.getString("content"));
                Picasso.with(CommentsActivity.this).load(sourceCon.getString("image")).resize(100, 100).into(youTubeImageView);
                statusFeedViewLayout.setVisibility(View.GONE);
                youTubeVideoViewLayout.setVisibility(View.VISIBLE);
            }

        }else{
              if (statusMessage != null)
              feedTitleTextView.setText(Html.fromHtml(statusMessage));
              feedTitleTextView.setVisibility(View.VISIBLE);
              youTubeVideoViewLayout.setVisibility(View.GONE);
              statusFeedViewLayout.setVisibility(View.GONE);
          }

        } catch (JSONException e) {
            e.printStackTrace();
        }


            } catch (JSONException e) {
                e.printStackTrace();
            }


            pDialog.dismiss();


        }
    }


    public class GetCommentsAsyncTask extends AbstractVedantuJSONAsyncTask {

        private ProgressDialog pDialog;
        private String entityType;
        private String entityId;
//        private List<Actor> commentsActorsList = new ArrayList<Actor>();

        public GetCommentsAsyncTask(SessionManager session, ProgressBar progressUpdater, String rootId, String rootType) {
            super(session, progressUpdater);
            this.url = session.getApiUrl("getComments");
            this.entityType = rootType;
            this.entityId = rootId;
            pDialog = new ProgressDialog(CommentsActivity.this);
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
            httpParams.put("rootId", entityId);
            httpParams.put("sortOrder", "DESC");
            httpParams.put("rootType", entityType);
            httpParams.put("userRole", session.getOrgMemberInfo().profile);
            httpParams.put("callingAppId", "TapApp");
            httpParams.put("start", 0);
//            httpParams.put("size", 8);
            httpParams.put("orgId",session.getOrgMemberInfo().orgId);
            httpParams.put("parent.type",entityType);
            httpParams.put("userId",session.getOrgMemberInfo().userId);
            httpParams.put("parent.id",entityId);
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
                            actor.dateTime = ((JSONObject) list.get(i)).getString("timeCreated");
                            commentsList.add(actor);
                        }

                        mAdapter.notifyDataSetChanged();

                        Log.e("commentsActorsList", ".." + commentsList.toString());


                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (commentsList != null && commentsList.size() > 0) {
                    noCommentsTextView.setVisibility(View.GONE);
                } else {
                    noCommentsTextView.setVisibility(View.VISIBLE);
                }
            }
            pDialog.dismiss();


        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
               onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);     } }


    public class PostCommentAsyncTask extends AbstractVedantuJSONAsyncTask {


        private final ProgressDialog pDialog;
        private String entityId;
        private String entityType;
        private String comment;
        private String parentId;
        private String parentType;
        private boolean fromVideoComment;

        public PostCommentAsyncTask(SessionManager session, ProgressBar progressUpdater, String rootId, String rootType, String parentId, String parentType, String commentEntered, boolean fromVideo) {
            super(session, progressUpdater);
            this.url = session.getApiUrl("addComment");
            this.entityType = rootType;
            this.entityId = rootId;
            this.parentId = parentId;
            this.parentType = parentType;
            fromVideoComment = fromVideo;

            comment = commentEntered;
            pDialog = new ProgressDialog(CommentsActivity.this);
            pDialog.setMessage("Please wait..");
            pDialog.setCancelable(false);
           if(!isFinishing()){
               pDialog.show();
           }

            Log.e("addCommentsAsyncTask", "call");
        }
        @Override
        protected JSONObject doInBackground(String... params) {

            session.addSessionParams(httpParams);
            httpParams.put("callingApp", "TapApp");
            httpParams.put("callingUserId", session.getOrgMemberInfo().userId);
            httpParams.put("userRole", session.getOrgMemberInfo().profile);
            httpParams.put("callingAppId", "TapApp");
            httpParams.put("type", "COMMENT");
            httpParams.put("orgId",session.getOrgMemberInfo().orgId);
            httpParams.put("content",comment);
            httpParams.put("userId",session.getOrgMemberInfo().userId);
            if(fromVideoComment){
                httpParams.put("parent.id", parentId);
                httpParams.put("parent.type", parentType);
                httpParams.put("root.id", entityId);
                httpParams.put("root.type", entityType);
                httpParams.put("type", "REPLY");
            }else {
                httpParams.put("parent.id", entityId);
                httpParams.put("parent.type", entityType);
                httpParams.put("root.id", entityId);
                httpParams.put("root.type", entityType);
            }
            httpParams.put("myUserId",session.getOrgMemberInfo().userId);
            httpParams.put("memberId",session.getOrgMemberInfo().memberId);

            JSONObject res = null;
            try {
                res = VedantuWebUtils.getJSONData(url, VedantuWebUtils.GET_REQ, httpParams);
                Log.e("sendCommentsAsyncTask", "do in background");

            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            } finally {
                error = checkForError(res);
                Log.e(TAG,  ""+error);
                pDialog.dismiss();
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

            try {
                JSONObject result = jsonObject.getJSONObject("result");

//                JSONArray list = result.getJSONArray("list");

                if(result != null) {
                    commentsCount += 1;
                       Log.e("list", ".." + result.toString()+"commentsCount "+commentsCount);
                        Actor actor = new Actor();
                        actor.message = result.getString("content");
                        JSONObject user = result.getJSONObject("user");
                        actor.firstName = user.getString("firstName");
                        actor.lastName = user.getString("lastName");
                        actor.thumbnail = session.getOrgMemberInfo().thumbnail;
//                        actor.profile =  user.getString("profile");
//                    Log.e("timeCreated",result.getString("timeCreated"));
                        actor.dateTime = result.getString("timeCreated");
                        commentsList.add(actor);

                    mAdapter.notifyDataSetChanged();

                    pDialog.dismiss();

                    Toast.makeText(CommentsActivity.this, "Message sent successfully", Toast.LENGTH_SHORT).show();
                    }

                    commentsCountTextView.setText(""+commentsCount);

                    Log.e("commentsActorsList", ".." + commentsList.toString());




            } catch (JSONException e) {
                e.printStackTrace();
            }

            if(commentsList != null && commentsList.size()>0){
                noCommentsTextView.setVisibility(View.GONE);
            }else{
                noCommentsTextView.setVisibility(View.VISIBLE);
            }



        }
    }


    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        Log.e("onbackpress","called");
        Intent intent = new Intent();
        intent.putExtra("count", commentsCount);
        intent.putExtra("position", position);
        setResult(100, intent);
        finish();
    }

    private void loadingContent(String entityId, String entityType){
        String invalidContentMsg = "OOPs! Invalid V(QR) Code :(";

        ContentLinkDataManager linkDataManager = new ContentLinkDataManager(
              CommentsActivity.this);
        ContentLink contentLink = linkDataManager.getContentLink(entityId, EntityType
                .valueOfKey(entityType).name(), sessionManager
                .getSessionStringValue(ConstantGlobal.USER_ID), null, null, sessionManager
                .getSessionIntValue(ConstantGlobal.ORG_KEY_ID));
        error = contentLink == null;
        if (error) {
            Toast.makeText(CommentsActivity.this, "OOPs! Seem content is not available locally, sync your library to get latest content :(",
                    Toast.LENGTH_LONG).show();
            return;
        }
        LibraryContentRes libraryContentRes = new ContentDataManager(
                CommentsActivity.this).getLibraryContentRes(contentLink.linkId);
        if (libraryContentRes != null) {
            LibraryUtils.onLibraryItemClickListnerImpl(CommentsActivity.this,
                    libraryContentRes);
        } else {
            Toast.makeText(CommentsActivity.this, invalidContentMsg, Toast.LENGTH_LONG).show();
            return;
        }


    }

    public class GetUpVoteAsyncTask extends AbstractVedantuJSONAsyncTask {

        private ProgressDialog pDialog;
        private String entityType;
        private String entityId;
        private List<Actor> upVotedActorsList = new ArrayList<Actor>();
        private Dialog dialog;

        public GetUpVoteAsyncTask(SessionManager session, ProgressBar progressUpdater, String entityId, String entityType) {
            super(session, progressUpdater);
            this.url = session.getApiUrl("getVoters");
            this.entityType = entityType;
            this.entityId = entityId;
            pDialog = new ProgressDialog(CommentsActivity.this);
            pDialog.setMessage("Please wait..");
            pDialog.show();

            Log.e("getUpvoteAsyncTask", "call");
        }

        @Override
        protected JSONObject doInBackground(String... params) {


            session.addSessionParams(httpParams);
            httpParams.put("callingApp", "TapApp");
            httpParams.put("userRole", session.getOrgMemberInfo().profile);
            httpParams.put("callingUserId", session.getOrgMemberInfo().userId);
            httpParams.put("entity.type", entityType);
            httpParams.put("callingAppId", "TapApp");
            httpParams.put("start", 0);
//            httpParams.put("size", 8);
            httpParams.put("orgId",session.getOrgMemberInfo().orgId);
            httpParams.put("userId",session.getOrgMemberInfo().userId);
            httpParams.put("myUserId",session.getOrgMemberInfo().userId);
            httpParams.put("memberId",session.getOrgMemberInfo().memberId);
            httpParams.put("entity.id",entityId);


            JSONObject res = null;
            try {
                res = VedantuWebUtils.getJSONData(url, VedantuWebUtils.GET_REQ, httpParams);
                Log.e("upvoteAsyncTask", "do in background");

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
            try {
                JSONObject result = jsonObject.getJSONObject("result");

                JSONArray list = result.getJSONArray("list");

                if(list != null) {
                    Log.e("list", ".." + list.toString());
                    for (int i = 0; i < list.length();i++){
                        Actor actor = new Actor();
                        actor.firstName = ((JSONObject)list.get(i)).getString("firstName");
                        actor.lastName = ((JSONObject)list.get(i)).getString("lastName");
                        actor.thumbnail = ((JSONObject)list.get(i)).getString("thumbnail");
                        actor.profile =  ((JSONObject)list.get(i)).getString("profile");
                        upVotedActorsList.add(actor);
                    }

                    Log.e("upVotedActorsList", ".." + upVotedActorsList.toString());



                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


            pDialog.dismiss();
            dialog = new Dialog(CommentsActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.custom_dialog_layout);
            dialog.setCancelable(true);
            ListView listView = (ListView) dialog.findViewById(R.id.dialogList);
            ImageView closeButton = (ImageView) dialog.findViewById(R.id.crossButton);
            CustomListAdapterDialog adapter = new CustomListAdapterDialog(CommentsActivity.this, upVotedActorsList);
            listView.setAdapter(adapter);
            dialog.show();
            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
//            notifyDataSetChanged();

        }
    }

    public class UpVoteAsyncTask extends AbstractVedantuJSONAsyncTask {

        private ProgressDialog pDialog;
        private String entityType;
        private String entityId;

        public UpVoteAsyncTask(SessionManager session, ProgressBar progressUpdater, String entityId, String entityType) {
            super(session, progressUpdater);
            this.url = session.getApiUrl("upVote");
            this.entityType = entityType;
            this.entityId = entityId;
            pDialog = new ProgressDialog(CommentsActivity.this);
            pDialog.setMessage("Please wait..");
            pDialog.show();

            Log.e("upvoteAsyncTask", "call");
        }

        @Override
        protected JSONObject doInBackground(String... params) {


            session.addSessionParams(httpParams);
            httpParams.put("callingApp", "TapApp");
            httpParams.put("userRole", session.getOrgMemberInfo().profile);
            httpParams.put("callingUserId", session.getOrgMemberInfo().userId);
            httpParams.put("entity.type", entityType);
            httpParams.put("callingAppId", "TapApp");
            httpParams.put("orgId",session.getOrgMemberInfo().orgId);
            httpParams.put("userId",session.getOrgMemberInfo().userId);
            httpParams.put("myUserId",session.getOrgMemberInfo().userId);
            httpParams.put("memberId",session.getOrgMemberInfo().memberId);
            httpParams.put("entity.id",entityId);
            JSONObject res = null;
            try {
                res = VedantuWebUtils.getJSONData(url, VedantuWebUtils.GET_REQ, httpParams);
                Log.e("upvoteAsyncTask", "do in background");

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
            try {
                JSONObject result = jsonObject.getJSONObject("result");
                upvoted = result.getBoolean("processed");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.e("result", ".."+jsonObject);

            if(upvoted){
                   upVotesCount+=1;

                    Log.e("upvoted", ".." + upvoted);
                    upVoteTextView.setText("Upvoted");
                    upVoteTextView.setTextColor(getResources().getColor(R.color.red));
                    upVoteImageView.setBackground(getResources().getDrawable(R.drawable.upvoted));
                    upVotesCountTextView.setText(String.valueOf(upVotesCount));
                }else{
                    Log.e("upvoted", ".." + upvoted);
                    upVoteTextView.setText("Upvote");
                    upVoteTextView.setTextColor(getResources().getColor(R.color.orange));
                    upVoteImageView.setBackground(getResources().getDrawable(R.drawable.upvote));
                    upVotesCountTextView.setText(String.valueOf(upVotesCount));
            }
            pDialog.dismiss();

        }
    }





}
