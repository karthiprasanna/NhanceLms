package com.nhance.android.recentActivities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nhance.android.R;
import com.nhance.android.activities.AppLandingPageActivity;
import com.nhance.android.async.tasks.AbstractVedantuJSONAsyncTask;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.ContentDataManager;
import com.nhance.android.db.datamanagers.ContentLinkDataManager;
import com.nhance.android.db.models.entity.ContentLink;
import com.nhance.android.enums.EntityType;
import com.nhance.android.library.utils.LibraryUtils;
import com.nhance.android.managers.SessionManager;
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

/**
 * Created by prathibha on 3/31/2017.
 */

public class RecentActivitiesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    public List<JSONObject> recentActivitiesList;
    private JSONObject actor;
    private String userName;
    private String imageSrc;
    private Context context;
    private String profile;
    private JSONObject src;
    private String statusMessage;
    private int TYPE_ITEM = 1;
    private int TYPE_FOOTER = 2;
    private RecentActivitiesFragment fragment;
    private String newsActivityId;
    private SessionManager sessionManager;
    private boolean error;
    private int upVotesCount, commentsCount;
    private boolean upvoted;
    private String rootId;
    private String rootType;
    private boolean statusFeed;
    private String ownerId;
    private boolean removeFooter = false;

    public RecentActivitiesAdapter(Context context, List<JSONObject> activitiesList, RecentActivitiesFragment fragment) {

        recentActivitiesList = activitiesList;
        this.context = context;
        this.fragment = fragment;
//        Log.e("recentActivitiesList", ".."+recentActivitiesList.toString());
        sessionManager = SessionManager.getInstance(context.getApplicationContext());

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == TYPE_FOOTER) {
            //Inflating footer view
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.footer_recycler_layout, parent, false);
            return new FooterViewHolder(itemView);
        }else{
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recent_activities_item, parent, false);
            return new ItemViewHolder(itemView);
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if(holder instanceof ItemViewHolder) {

            final ItemViewHolder itemHolder = (ItemViewHolder)holder;
            try {
                Log.d("activitiesItem", ""+recentActivitiesList.get(position).getJSONObject("src").toString());
                JSONObject object = recentActivitiesList.get(position);
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
                    itemHolder.userNameTextView.setText(userName);
                } else {
                    itemHolder.userNameTextView.setText(userName + " (" + profile + ")");
                }
                src = object.getJSONObject("src");
                upVotesCount = src.getInt("upVotes");
                commentsCount = src.getInt("comments");

                upvoted = src.getBoolean("voted");

                Log.e("upvoted", ".." + src.getBoolean("voted"));

                newsActivityId = object.getString("newsActivityId");

                Log.e("newsActivityId", ".." + newsActivityId);
                if(src.has("statusMessage")) {
                    statusFeed = true;
                    try {
                        statusMessage = src.getString("statusMessage");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else{
                    statusFeed = false;
                }

                JSONObject sourceCon = src.optJSONObject("sourceContent");
                if (sourceCon != null) {

                    src = sourceCon;
                }
                String profile = (sessionManager.getOrgMemberInfo().profile).toString();
                if(statusFeed && ((!profile.equalsIgnoreCase("STUDENT")) || sessionManager.getOrgMemberInfo().userId.equalsIgnoreCase(ownerId))){
                    itemHolder.deleteButton.setVisibility(View.VISIBLE);
                }else{
                    itemHolder.deleteButton.setVisibility(View.GONE);
                }

                if(upvoted){
                    Log.e("upvoted", ".." + upvoted);
                    itemHolder.upVoteTextView.setText("Upvoted");
                    itemHolder.upVoteTextView.setTextColor(context.getResources().getColor(R.color.red));
                    itemHolder.upVoteImageView.setBackground(context.getResources().getDrawable(R.drawable.upvoted));
                }else{
                    Log.e("upvoted", ".." + upvoted);
                    itemHolder.upVoteTextView.setText("Upvote");
                    itemHolder.upVoteTextView.setTextColor(context.getResources().getColor(R.color.orange));
                    itemHolder.upVoteImageView.setBackground(context.getResources().getDrawable(R.drawable.upvote));

                }
                itemHolder.upVotesCount.setText(String.valueOf(upVotesCount));
                itemHolder.commentsCount.setText(String.valueOf(commentsCount));


                Log.e("object in src", ".." + src.toString());
                Log.e("position", ".." + position);
                if (src.getString("type").equalsIgnoreCase("TEST") || src.getString("type").equalsIgnoreCase("ASSIGNMENT")) {
                    Log.e("source type", ".." + "TEST");
                    itemHolder.feedTitleTextView.setVisibility(View.VISIBLE);
                    itemHolder.assessmentLayout.setVisibility(View.VISIBLE);
                    itemHolder.videoViewLayout.setVisibility(View.GONE);
                    itemHolder.youTubeVideoViewLayout.setVisibility(View.GONE);
                    itemHolder.statusFeedViewLayout.setVisibility(View.GONE);
                    itemHolder.assessmentTypeTextView.setVisibility(View.VISIBLE);


                    if( src.getString("type").equalsIgnoreCase("TEST") ){
                        itemHolder.feedTitleTextView.setText("New assessment added to your library. Have a look!!");
                        itemHolder.assessmentTypeTextView.setText("Online Test");
                        if(src.getBoolean("attempted")){
//                            itemHolder.assessmentTypeTextView.setVisibility(View.VISIBLE);
                            itemHolder.assessmentImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_test_attempted));
                        }else{
                            itemHolder.assessmentImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_small_test));
//                            itemHolder.assessmentTypeTextView.setVisibility(View.GONE);
                        }
                    }else{
                        itemHolder.assessmentTypeTextView.setText("Assignment");
                        itemHolder.assessmentImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_assignment));
                        itemHolder.feedTitleTextView.setText("New assignment added to your library. Have a look!!");
                    }
                    itemHolder.assessmentAuthorTextView.setText("by "+itemHolder.userNameTextView.getText());
//                    Picasso.with(context).load(src.getString("image")).resize(100, 100).into(itemHolder.assessmentImageView);
                    itemHolder.assessmentTitleTextView.setText(src.getString("name"));
                    itemHolder.assessmentProgramTextView.setText(((JSONObject)(src.getJSONArray("boardTree").get(0))).getString("name"));

                } else if (src.getString("type").equalsIgnoreCase("VIDEO")) {
                    Log.e("source type", ".." + "VIDEO");
                    itemHolder.feedTitleTextView.setVisibility(View.VISIBLE);
                    itemHolder.feedTitleTextView.setText("New vedio added to your library. Have a look!!");
                    itemHolder.videoViewLayout.setVisibility(View.VISIBLE);
                    itemHolder.videoLayoutTitleTextView.setText(src.getString("name"));
                    itemHolder.videoLayoutAuthorTextView.setText("by " + itemHolder.userNameTextView.getText());
                    itemHolder.videoLayoutViewsTextView.setText("views : " + src.getInt("views"));
                    Picasso.with(context).load(src.getString("thumbnail")).resize(200, 200).into(itemHolder.videoLayoutImageView);
                    itemHolder.assessmentLayout.setVisibility(View.GONE);
                    itemHolder.youTubeVideoViewLayout.setVisibility(View.GONE);
                    itemHolder.statusFeedViewLayout.setVisibility(View.GONE);
                } else if (src.getString("type").equalsIgnoreCase("MODULE")) {
                    Log.e("source type", ".." + "MODULE");
                    itemHolder.feedTitleTextView.setVisibility(View.VISIBLE);
                    itemHolder.feedTitleTextView.setText("New Learning Path added to your library. Have a look!!");
                    itemHolder.videoViewLayout.setVisibility(View.GONE);
                    itemHolder.assessmentLayout.setVisibility(View.VISIBLE);
                    itemHolder.assessmentTypeTextView.setVisibility(View.GONE);
                    itemHolder.youTubeVideoViewLayout.setVisibility(View.GONE);
                    itemHolder.statusFeedViewLayout.setVisibility(View.GONE);
                    itemHolder.assessmentAuthorTextView.setText("by "+itemHolder.userNameTextView.getText());
                    itemHolder.assessmentImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_slpcontent_leftnav));
                    itemHolder.assessmentTitleTextView.setText(src.getString("name"));
                    itemHolder.assessmentProgramTextView.setText(((JSONObject)(src.getJSONArray("boardTree").get(0))).getString("name"));

                } else if (src.getString("type").equalsIgnoreCase("STATUSFEED")) {
                    if (statusMessage != null)
                        itemHolder.feedTitleTextView.setText(Html.fromHtml(statusMessage));
                    itemHolder.feedTitleTextView.setVisibility(View.VISIBLE);
                    itemHolder.youTubeVideoViewLayout.setVisibility(View.GONE);
                    itemHolder.statusFeedViewLayout.setVisibility(View.GONE);
                    itemHolder.videoViewLayout.setVisibility(View.GONE);
                    itemHolder.assessmentLayout.setVisibility(View.GONE);
                } else if (src.getString("type").equalsIgnoreCase("IMAGE")) {
                    itemHolder.statusFeedViewLayout.setVisibility(View.VISIBLE);
                    itemHolder.youTubeVideoViewLayout.setVisibility(View.GONE);
                    if (statusMessage != null)
                        itemHolder.statusFeedTextView.setText(Html.fromHtml(statusMessage));
                    Picasso.with(context).load(sourceCon.getString("image")).into(itemHolder.statusFeedImageView);
                    itemHolder.feedTitleTextView.setVisibility(View.GONE);
                    itemHolder.videoViewLayout.setVisibility(View.GONE);
                    itemHolder.assessmentLayout.setVisibility(View.GONE);
                } else if (src.getString("type").equalsIgnoreCase("WEB_PAGE") || src.getString("type").equalsIgnoreCase("LINK_VIDEO")) {
                    if (statusMessage != null)
                    itemHolder.feedTitleTextView.setText(Html.fromHtml(statusMessage));
                    itemHolder.feedTitleTextView.setVisibility(View.VISIBLE);
                    itemHolder.youTubeTitleView.setText(src.getString("title"));
                    itemHolder.youTubeContentView.setText(src.getString("content"));
                    Picasso.with(context).load(sourceCon.getString("image")).resize(100, 100).into(itemHolder.youTubeImageView);
                    itemHolder.statusFeedViewLayout.setVisibility(View.GONE);
                    itemHolder.youTubeVideoViewLayout.setVisibility(View.VISIBLE);
                    itemHolder.videoViewLayout.setVisibility(View.GONE);
                    itemHolder.assessmentLayout.setVisibility(View.GONE);

                } else if (src.getString("type").equalsIgnoreCase("FILE")) {
                    itemHolder.feedTitleTextView.setVisibility(View.VISIBLE);
                    itemHolder.feedTitleTextView.setText("New Resource added to your library. Have a look!!");
                    itemHolder.statusFeedViewLayout.setVisibility(View.GONE);
                    itemHolder.youTubeVideoViewLayout.setVisibility(View.GONE);
                    itemHolder.videoViewLayout.setVisibility(View.GONE);
                    itemHolder.assessmentTypeTextView.setVisibility(View.GONE);
                    itemHolder.assessmentLayout.setVisibility(View.VISIBLE);
                    itemHolder.assessmentAuthorTextView.setText("by "+itemHolder.userNameTextView.getText());
                    itemHolder.assessmentImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_file));
                    itemHolder.assessmentTitleTextView.setText(src.getString("name"));
                    itemHolder.assessmentProgramTextView.setText(((JSONObject)(src.getJSONArray("boardTree").get(0))).getString("name"));

                } else if (src.getString("type").equalsIgnoreCase("HTMLCONTENT")) {
                    itemHolder.feedTitleTextView.setVisibility(View.VISIBLE);
                    itemHolder.feedTitleTextView.setText("New E-Content added to your library. Have a look!!");
                    itemHolder.statusFeedViewLayout.setVisibility(View.GONE);
                    itemHolder.youTubeVideoViewLayout.setVisibility(View.GONE);
                    itemHolder.videoViewLayout.setVisibility(View.GONE);
                    itemHolder.assessmentTypeTextView.setVisibility(View.GONE);
                    itemHolder.assessmentLayout.setVisibility(View.VISIBLE);
                    itemHolder.assessmentAuthorTextView.setText("by "+itemHolder.userNameTextView.getText());
                    itemHolder.assessmentImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.scorm_icon));
                    itemHolder.assessmentTitleTextView.setText(src.getString("name"));
                    itemHolder.assessmentProgramTextView.setText(((JSONObject)(src.getJSONArray("boardTree").get(0))).getString("name"));

                } else if (src.getString("type").equalsIgnoreCase("DOCUMENT")) {
                    itemHolder.feedTitleTextView.setVisibility(View.VISIBLE);
                    itemHolder.feedTitleTextView.setText("New E-Book added to your library. Have a look!!");
                    itemHolder.statusFeedViewLayout.setVisibility(View.GONE);
                    itemHolder.youTubeVideoViewLayout.setVisibility(View.GONE);
                    itemHolder.videoViewLayout.setVisibility(View.GONE);
                    itemHolder.assessmentTypeTextView.setVisibility(View.GONE);
                    itemHolder.assessmentLayout.setVisibility(View.VISIBLE);
                    itemHolder.assessmentAuthorTextView.setText("by "+itemHolder.userNameTextView.getText());
                    itemHolder.assessmentImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_book));
                    itemHolder.assessmentTitleTextView.setText(src.getString("name"));
                    itemHolder.assessmentProgramTextView.setText(((JSONObject)(src.getJSONArray("boardTree").get(0))).getString("name"));

                }


            } catch (JSONException e) {
                e.printStackTrace();
            }

            Picasso.with(context)
                    .load(imageSrc)
                    .into(itemHolder.imgViewIcon);
//        holder.imgViewIcon.setImageBitmap(movie.getGenre());
//        holder.year.setText(movie.getYear());
        }else if(holder instanceof FooterViewHolder){
            FooterViewHolder footerHolder = (FooterViewHolder) holder;
            footerHolder.footerText.setText("Load More..");
            footerHolder.footerText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
//                      JSONObject  src = (recentActivitiesList.get(recentActivitiesList.size()-1).getJSONArray("clusteredNews").getJSONObject(0));
                      JSONObject  src = (recentActivitiesList.get(recentActivitiesList.size()-1));
                        fragment.loadMore(recentActivitiesList.size(), src.getString("newsActivityId"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

//                    Toast.makeText(context, "You clicked at Footer View", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == recentActivitiesList.size()-1 && !removeFooter) {
            return TYPE_FOOTER;
        }else{
            return TYPE_ITEM;
        }

    }

    @Override
    public int getItemCount() {

        return recentActivitiesList.size();
    }

    // inner class to hold a reference to each item of RecyclerView
    private class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView assessmentTypeTextView;
        public TextView assessmentProgramTextView;
        public TextView assessmentTitleTextView;
        public TextView assessmentAuthorTextView;
        public ImageView assessmentImageView;
        public TextView videoLayoutTitleTextView;
        public TextView videoLayoutAuthorTextView;
        public TextView videoLayoutViewsTextView;
        public ImageView videoLayoutImageView;
        public TextView youTubeTitleView;
        public TextView youTubeContentView;
        public ImageView youTubeImageView;
        public TextView feedTitleTextView;
        public TextView statusFeedTextView;
        public ImageView statusFeedImageView;
        public TextView userNameTextView;
        public ImageView imgViewIcon;
        public LinearLayout assessmentLayout;
        public LinearLayout statusFeedViewLayout;
        public LinearLayout videoViewLayout;
        public LinearLayout youTubeVideoViewLayout;
        public TextView upVotesCount;
        public TextView commentsCount;
        public TextView upVoteTextView;
        public LinearLayout upVoteLayout;
        public LinearLayout commentsLayout;
        public Button deleteButton;
        public ImageView upVoteImageView;


        public ItemViewHolder(View itemLayoutView) {
            super(itemLayoutView);

            userNameTextView = (TextView) itemLayoutView.findViewById(R.id.user_name);
            imgViewIcon = (ImageView) itemLayoutView.findViewById(R.id.user_profile_image);
            deleteButton = (Button) itemLayoutView.findViewById(R.id.delete);

            upVotesCount = (TextView) itemLayoutView.findViewById(R.id.upVoteCount);
            commentsCount = (TextView) itemLayoutView.findViewById(R.id.commentCount);
            upVoteTextView = (TextView) itemLayoutView.findViewById(R.id.upVote);
            upVoteImageView = (ImageView) itemLayoutView.findViewById(R.id.upVoteImage);
            upVoteLayout = (LinearLayout) itemLayoutView.findViewById(R.id.upVOteLayout);
            commentsLayout = (LinearLayout) itemLayoutView.findViewById(R.id.commentsLayout);


            assessmentLayout = (LinearLayout) itemLayoutView.findViewById(R.id.lin1);
            statusFeedViewLayout = (LinearLayout) itemLayoutView.findViewById(R.id.statusFeedView);
            videoViewLayout = (LinearLayout) itemLayoutView.findViewById(R.id.vedioView);
            youTubeVideoViewLayout  = (LinearLayout) itemLayoutView.findViewById(R.id.youTubeVideoView);

            youTubeImageView = (ImageView) itemLayoutView.findViewById(R.id.you_tube_image);
            youTubeTitleView = (TextView) itemLayoutView.findViewById(R.id.you_tube_title);
            youTubeContentView = (TextView) itemLayoutView.findViewById(R.id.you_tube_content);

            feedTitleTextView = (TextView) itemLayoutView.findViewById(R.id.feed_title);
            statusFeedTextView = (TextView) itemLayoutView.findViewById(R.id.statusFeedTextView);
            statusFeedImageView = (ImageView) itemLayoutView.findViewById(R.id.statusFeedImageView);

            videoLayoutTitleTextView = (TextView) itemLayoutView.findViewById(R.id.videoLayoutTitleTextView);
            videoLayoutAuthorTextView = (TextView) itemLayoutView.findViewById(R.id.videoLayoutAuthorTextView);
            videoLayoutImageView = (ImageView) itemLayoutView.findViewById(R.id.videoLayoutImageView);
            videoLayoutViewsTextView = (TextView) itemLayoutView.findViewById(R.id.videoLayoutNoOfViews);

            assessmentTitleTextView = (TextView) itemLayoutView.findViewById(R.id.ass_title);
            assessmentTypeTextView = (TextView) itemLayoutView.findViewById(R.id.ass_type);
            assessmentAuthorTextView = (TextView) itemLayoutView.findViewById(R.id.ass_added_by);
            assessmentImageView = (ImageView) itemLayoutView.findViewById(R.id.ass_image_view);
            assessmentProgramTextView = (TextView) itemLayoutView.findViewById(R.id.ass_program_name);

            assessmentLayout.setOnClickListener(this);
            videoViewLayout.setOnClickListener(this);
            youTubeVideoViewLayout.setOnClickListener(this);
            upVoteLayout.setOnClickListener(this);
            commentsLayout.setOnClickListener(this);
            deleteButton.setOnClickListener(this);


        }

        @Override
        public void onClick(View v) {

            final JSONObject object = recentActivitiesList.get(getAdapterPosition());
            if (v.getId() == assessmentLayout.getId()){

                try {
                    JSONObject src = object.getJSONObject("src");
                    loadingContent(src.getString("id"), src.getString("type"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
//                Toast.makeText(v.getContext(), "ITEM PRESSED = " + String.valueOf(getAdapterPosition()), Toast.LENGTH_SHORT).show();
            }
            else if( v.getId() == videoViewLayout.getId()){

                try {
                    JSONObject src = object.getJSONObject("src");
                    loadingContent(src.getString("id"), src.getString("type"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }else if(v.getId() == youTubeVideoViewLayout.getId()) {

                try {
                    JSONObject src = object.getJSONObject("src");
                    JSONObject sourceContent = src.getJSONObject("sourceContent");
                    String url = sourceContent.getString("url");

                    Intent intent = new Intent(Intent.ACTION_VIEW,  Uri.parse(url));
//                    startActivity(intent);
//                    loadingContent(src.getString("id"), src.getString("type"));
//                    Intent intent= new Intent(context, WebActivity.class);
//                    intent.putExtra("url",url);
                    (context).startActivity(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
//                Toast.makeText(v.getContext(), "ROW PRESSED = " + String.valueOf(getAdapterPosition()), Toast.LENGTH_SHORT).show();
            }else if(v.getId() == upVoteLayout.getId()){
                try {
                     src = (object).getJSONObject("src");
                    upvoted = src.getBoolean("voted");
                    if (upvoted) {
                        Log.e("upvoted", ".." + upvoted);
                        try {
                            new GetUpVoteAsyncTask(SessionManager.getInstance(context), null, src.getString("id"), src.getString("type"), getAdapterPosition()).executeTask(false);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }else{
                        Log.e("upvoted", ".." + upvoted);
                        try {
                            new UpVoteAsyncTask(SessionManager.getInstance(context), null, src.getString("id"), src.getString("type"), getAdapterPosition()).executeTask(false);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
//                Toast.makeText(v.getContext(), "ROW PRESSED = " + String.valueOf(getAdapterPosition()), Toast.LENGTH_SHORT).show();

            }else if(v.getId() == commentsLayout.getId()){
                try {
                    src = (object).getJSONObject("src");
                    rootId = src.getString("id");
                    rootType = src.getString("type");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(context, CommentsActivity.class);
                 intent.putExtra("rootId", rootId);
                 intent.putExtra("rootType", rootType);
                 intent.putExtra("position", getAdapterPosition());
                ((AppLandingPageActivity)context).startActivityForResult(intent, 100);
            }else if(v.getId() == deleteButton.getId()){
                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                builder.setTitle("Do you want to delete the Feed?");
                builder.setMessage("Once it's deleted you can't undo.");

                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // Do do my action here

                        dialog.dismiss();
                        try {
                            src = (object).getJSONObject("src");
                            new DeleteFeedAsyncTask(SessionManager.getInstance(context), null, src.getString("id"),  getAdapterPosition()).executeTask(false);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                });

                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // I do not need any action here you might
                        dialog.dismiss();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();


            }

        }
    }

    // FOOTER VIEW HOLDER OF RECYCLER VIEW
    private class FooterViewHolder extends RecyclerView.ViewHolder {
        TextView footerText;

        public FooterViewHolder(View view) {
            super(view);
            footerText = (TextView) view.findViewById(R.id.footer_text);
        }
    }

    private void loadingContent(String entityId, String entityType){
        String invalidContentMsg = "OOPs! Invalid V(QR) Code :(";

        ContentLinkDataManager linkDataManager = new ContentLinkDataManager(
                context.getApplicationContext());
        ContentLink contentLink = linkDataManager.getContentLink(entityId, EntityType
                .valueOfKey(entityType).name(), sessionManager
                .getSessionStringValue(ConstantGlobal.USER_ID), null, null, sessionManager
                .getSessionIntValue(ConstantGlobal.ORG_KEY_ID));
        error = contentLink == null;
        if (error) {
            Toast.makeText(context,
                    "OOPs! Seem content is not available locally, sync your library to get latest content :(",
                    Toast.LENGTH_LONG).show();
            return;
        }
        LibraryContentRes libraryContentRes = new ContentDataManager(
                context.getApplicationContext()).getLibraryContentRes(contentLink.linkId);
        if (libraryContentRes != null) {
            LibraryUtils.onLibraryItemClickListnerImpl(context.getApplicationContext(),
                    libraryContentRes);
        } else {
            Toast.makeText(context, invalidContentMsg, Toast.LENGTH_LONG).show();
            return;
        }


    }

    public class UpVoteAsyncTask extends AbstractVedantuJSONAsyncTask {

        private ProgressDialog pDialog;
        private String entityType;
        private String entityId;
        private int position;

        public UpVoteAsyncTask(SessionManager session, ProgressBar progressUpdater, String entityId, String entityType, int pos) {
            super(session, progressUpdater);
            this.url = session.getApiUrl("upVote");
            this.entityType = entityType;
            this.entityId = entityId;
            this.position = pos;
            pDialog = new ProgressDialog(context);
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
            JSONObject object = recentActivitiesList.get(position);
            try {
                JSONObject src = object.getJSONObject("src");
                upVotesCount = src.getInt("upVotes");
                src.put("voted", true);
                src.put("upVotes", upVotesCount+1);
                object.put("src", src);
                recentActivitiesList.remove(position);
                recentActivitiesList.add(position, object);
                Log.d("activitiesItem", ""+recentActivitiesList.get(position).toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            pDialog.dismiss();
            notifyDataSetChanged();

        }
    }

    public class GetUpVoteAsyncTask extends AbstractVedantuJSONAsyncTask {

        private ProgressDialog pDialog;
        private String entityType;
        private String entityId;
        private int position;
        private List<Actor> upVotedActorsList = new ArrayList<Actor>();
        private Dialog dialog;

        public GetUpVoteAsyncTask(SessionManager session, ProgressBar progressUpdater, String entityId, String entityType, int pos) {
            super(session, progressUpdater);
            this.url = session.getApiUrl("getVoters");
            this.entityType = entityType;
            this.entityId = entityId;
            this.position = pos;
            pDialog = new ProgressDialog(context);
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
            dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.custom_dialog_layout);
            dialog.setCancelable(true);
            ListView listView = (ListView) dialog.findViewById(R.id.dialogList);
            ImageView closeButton = (ImageView) dialog.findViewById(R.id.crossButton);
            CustomListAdapterDialog adapter = new CustomListAdapterDialog(context, upVotedActorsList);
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


    public class DeleteFeedAsyncTask extends AbstractVedantuJSONAsyncTask {

        private ProgressDialog pDialog;
        private String entityType;
        private String entityId;
        private int position;
        private List<Actor> upVotedActorsList = new ArrayList<Actor>();
        private Dialog dialog;

        public DeleteFeedAsyncTask(SessionManager session, ProgressBar progressUpdater, String entityId, int pos) {
            super(session, progressUpdater);
            this.url = session.getApiUrl("statusFeedDelete");
            this.entityId = entityId;
            this.position = pos;
            pDialog = new ProgressDialog(context);
            pDialog.setMessage("Please wait..");
            pDialog.show();

            Log.e("deleteFeedAsyncTask", "call");
        }

        @Override
        protected JSONObject doInBackground(String... params) {


//            session.addSessionParams(httpParams);
            httpParams.put("callingApp", "TapApp");
            httpParams.put("userRole", session.getOrgMemberInfo().profile);
            httpParams.put("callingUserId", session.getOrgMemberInfo().userId);
            httpParams.put("callingAppId", "TapApp");
            httpParams.put("orgId",session.getOrgMemberInfo().orgId);
            httpParams.put("id",entityId);
            httpParams.put("userId",session.getOrgMemberInfo().userId);
            httpParams.put("myUserId",session.getOrgMemberInfo().userId);
            httpParams.put("memberId",session.getOrgMemberInfo().memberId);


            JSONObject res = null;
            try {
                res = VedantuWebUtils.getJSONData(url, VedantuWebUtils.GET_REQ, httpParams);
                Log.e("deleteAsyncTask", "do in background");

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
//            JSONObject object = recentActivitiesList.get(position);
            try {
                if(jsonObject != null && jsonObject.getString("errorCode").equals("")) {
                    JSONObject result = jsonObject.getJSONObject("result");

                    boolean deleted = result.getBoolean("deleted");

                    if (deleted) {

                        Log.e("deleted", ".." + deleted);
                      recentActivitiesList.remove(position);
                        notifyDataSetChanged();

                    }
                }else{
                    Toast.makeText(context, "Error occured while deleting status feed.", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            pDialog.dismiss();


        }
    }

    public void removeFooter(){
        removeFooter = true;
    }


    public  void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("MyAdapter", "onActivityResult"+requestCode);
    }

}
