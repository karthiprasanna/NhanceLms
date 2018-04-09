package com.nhance.android.adapters.video;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nhance.android.R;
import com.nhance.android.activities.AppLandingPageActivity;
import com.nhance.android.activities.content.players.VideoPlayerActivity;
import com.nhance.android.async.tasks.AbstractVedantuJSONAsyncTask;
import com.nhance.android.managers.LocalManager;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.notifications.NotificationAdapter;
import com.nhance.android.pojos.LibraryContentRes;
import com.nhance.android.recentActivities.Actor;
import com.nhance.android.recentActivities.CommentsActivity;
import com.nhance.android.recentActivities.CustomListAdapterDialog;
import com.nhance.android.recentActivities.RecentActivitiesAdapter;
import com.nhance.android.utils.VedantuWebUtils;
import com.nhance.android.utils.ViewUtils;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static android.content.ContentValues.TAG;


/**
 * Created by prathibha
 */

public class VideoCommentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    public List<Actor> commentsList;
    private Context context;
    public boolean upvoted;
    private String videoId;
    private View    mVideoInfoContainer;
    private int TYPE_ITEM = 2;
    private int TYPE_HEADER = 1;
    private LibraryContentRes mVideo;
    private String htmlData;
    private Actor actor;

    public VideoCommentsAdapter(Context context, List<Actor> activitiesList, String videoId, LibraryContentRes mVideo) {
        commentsList = activitiesList;
        this.context = context;
        this.videoId = videoId;
        this.mVideo = mVideo;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


        if (viewType == TYPE_HEADER) {
            //Inflating footer view
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_comment_header_layout, parent, false);
            return new HeaderViewHolder(itemView);
        }else{
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_comment_list_item, parent, false);
            return new ItemViewHolder(itemView);
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if(holder instanceof ItemViewHolder && position < commentsList.size()) {

            ItemViewHolder itemHolder = (ItemViewHolder) holder;

             actor = commentsList.get(position);

            itemHolder.profileTextView.setText(actor.firstName + " " + actor.lastName);
            upvoted = actor.upVoted;
            Log.e("content", ".." + actor.message);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                itemHolder.messageTextView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);
//            } else {
//                itemHolder.messageTextView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
//            }
//            itemHolder.messageTextView.getSettings().setLoadWithOverviewMode(true);
//            itemHolder.messageTextView.getSettings().setUseWideViewPort(true);
            htmlData = "<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />" + actor.message;
            itemHolder.messageTextView.loadDataWithBaseURL(null, "<style>img{display: inline;height: auto;max-width: 100%;}</style>" + actor.message, "text/html", "UTF-8", null);
            if (upvoted) {
                Log.e("upvoted", ".." + upvoted);
                itemHolder.upVoteTextView.setText("Upvoted");
                itemHolder.upVoteTextView.setTextColor(context.getResources().getColor(R.color.red));
                itemHolder.upVoteImageView.setBackground(context.getResources().getDrawable(R.drawable.upvoted));
            } else {
                Log.e("upvoted", ".." + upvoted);
                itemHolder.upVoteTextView.setText("Upvote");
                itemHolder.upVoteTextView.setTextColor(context.getResources().getColor(R.color.orange));
                itemHolder.upVoteImageView.setBackground(context.getResources().getDrawable(R.drawable.upvote));

            }
            itemHolder.upVoteCountTextView.setText("" + actor.upVoteCount);
            itemHolder.replyCountTextView.setText("" + actor.replyCount);
            Picasso.with(context).load(actor.thumbnail).into(itemHolder.commentsImageView);

            String dateTime = convertDate(actor.dateTime, "dd/MM/yyyy hh:mm:ss");
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

            itemHolder.dateTextView.setText(getlongtoago(Long.parseLong(actor.dateTime)));
        }else if(holder instanceof HeaderViewHolder) {
            setVideoInfo();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        }else{
            return TYPE_ITEM;
        }
    }

    @Override
    public int getItemCount() {

        if(commentsList.isEmpty()){
            return 1;
        }else {
            return commentsList.size();
        }
    }

    // inner class to hold a reference to each item of RecyclerView
    private class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public ImageView commentsImageView, upVoteImageView;
        public TextView profileTextView;
        public WebView messageTextView;
        public TextView dateTextView, upVoteCountTextView, replyCountTextView, upVoteTextView;
        public LinearLayout upVoteLayout;
        public LinearLayout commentsLayout;


        public ItemViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            commentsImageView = (ImageView)itemLayoutView.findViewById(R.id.thumbnail);
            upVoteImageView = (ImageView)itemLayoutView.findViewById(R.id.upVoteImage);
            profileTextView = (TextView)itemLayoutView.findViewById(R.id.nameTextView);
            messageTextView = (WebView)itemLayoutView.findViewById(R.id.msg);
            dateTextView = (TextView)itemLayoutView.findViewById(R.id.dateTextView);
            upVoteTextView =(TextView) itemLayoutView.findViewById(R.id.upVote);
            upVoteCountTextView = (TextView) itemLayoutView.findViewById(R.id.upVoteCount);
            replyCountTextView = (TextView) itemLayoutView.findViewById(R.id.commentCount);
            upVoteLayout = (LinearLayout) itemLayoutView.findViewById(R.id.upVOteLayout);
            commentsLayout = (LinearLayout) itemLayoutView.findViewById(R.id.commentsLayout);
            upVoteLayout.setOnClickListener(this);
            commentsLayout.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Actor actor = commentsList.get(getAdapterPosition());
            if(v.getId() == upVoteLayout.getId()){
                    upvoted = actor.upVoted;
                    if (upvoted) {
                        Log.e("upvoted", ".." + upvoted);
                         new GetUpVoteAsyncTask(SessionManager.getInstance(context), null, actor.id, actor.type, getAdapterPosition()).executeTask(false);

                    }else{
                        Log.e("upvoted", ".." + upvoted);
                           new UpVoteAsyncTask(SessionManager.getInstance(context), null, actor.id, actor.type, getAdapterPosition()).executeTask(false);

                    }


            }else if(v.getId() == commentsLayout.getId()){
//
                Intent intent = new Intent(context, CommentsActivity.class);
                intent.putExtra("rootId", videoId);
                intent.putExtra("rootType", "VIDEO");
                intent.putExtra("parentId", actor.id);
                intent.putExtra("parentType", "COMMENT");
                intent.putExtra("fromVideoComments", true);
                intent.putExtra("position", getAdapterPosition());
                ((VideoPlayerActivity)context).startActivityForResult(intent, 100);
            }
        }
    }


    // HEADER VIEW HOLDER OF RECYCLER VIEW
    private class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, description;


        public HeaderViewHolder(View view) {
            super(view);
            mVideoInfoContainer = view.findViewById(R.id.video_info_container);

        }
    }

    private Date getGMTDate(long date) {
        SimpleDateFormat dateFormatGmt = new SimpleDateFormat(
                "yyyy-MMM-dd HH:mm:ss");
        dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
        SimpleDateFormat dateFormatLocal = new SimpleDateFormat(
                "yyyy-MMM-dd HH:mm:ss");

        Date temp = new Date(date);

        try {
            return dateFormatLocal.parse(dateFormatGmt.format(temp));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return temp;
    }

    public static String convertDate(String dateInMilliseconds,String dateFormat) {
        return DateFormat.format(dateFormat, Long.parseLong(dateInMilliseconds)).toString();
    }

    private void setVideoInfo() {

        ViewUtils.setTextViewValue(mVideoInfoContainer, R.id.video_name, mVideo.name);
        ViewUtils.setTextViewValue(mVideoInfoContainer, R.id.video_added_by, mVideo.ownerName);
        TextView desc = (TextView) mVideoInfoContainer.findViewById(R.id.video_desc);

        if (StringUtils.isNotEmpty(mVideo.desc) && !mVideo.desc.trim().equalsIgnoreCase("null")) {
            desc.setText(Html.fromHtml(mVideo.desc));
        } else {
            desc.setVisibility(View.GONE);
        }
//        int duration = (int) (vInfo.duration / DateUtils.SECOND_IN_MILLIS);
//        if (duration > 0) {
//            ViewUtils.setTextViewValue(mVideoStatsContainer, R.id.video_duration,
//                    LocalManager.getDurationString(duration));
//        } else if (mVideoStatsContainer != null) {
//
//            mVideoStatsContainer.findViewById(R.id.video_duration).setVisibility(View.GONE);
//        }
    }


    public static String getlongtoago(long createdAt) {

        String time;
        Date d2 = new Date();
        Date d1 = new Date(createdAt);

        long diff = d2.getTime() - d1.getTime();
        System.out.println(diff + "  diff");
        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000);
        long diffInDays = diff / (1000 * 60 * 60 * 24);
        int weeks = (int) (diff/ (1000*60*60*24*7));
        long month = diffInDays /30;
        long year = diffInDays / 365;

        time = diffInDays+" days";

        System.out.println(diffInDays + "  days");
        System.out.println(diffHours + "  Hour");
        System.out.println(diffMinutes + "  min");
        System.out.println(diffSeconds + "  sec");
        System.out.println(weeks + "  weeks");
        System.out.println(month + "  month");
        System.out.println(year + "  year");


        if(year > 0){
            if (year == 1) {
                time = (int)year + " year ago ";
            } else {
                time = (int)year + " years ago ";
            }
        } else if(month >0){
            if (month == 1) {
                time = (int)month + " month ago ";
            } else {
                time = (int)month + " months ago ";
            }
        }else if (diffInDays > 0) {
                if (diffInDays == 1) {
                    time = diffInDays + " day ago ";
                } else {
                    time = diffInDays + " days ago ";
                }
        } else if (diffHours > 0) {
                    if (diffHours == 1) {
                        time = diffHours + " hr ago";
                    } else {
                        time = diffHours + " hrs ago";
                    }
        } else if (diffMinutes > 0) {
                        if (diffMinutes == 1) {
                            time = diffMinutes + " min ago";
                        } else {
                            time = diffMinutes + " mins ago";
                        }
        } else if (diffSeconds > 0) {
                   time = diffSeconds + " secs ago";
        }



        return  time;
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
            Actor actor = commentsList.get(position);
            actor.upVoted = upvoted;
            actor.upVoteCount = actor.upVoteCount+1;
            commentsList.remove(position);
            commentsList.add(position, actor);
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




}
