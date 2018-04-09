package com.nhance.android.notifications;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nhance.android.R;
import com.nhance.android.activities.AppLandingPageActivity;
import com.nhance.android.activities.CompoundMediaPlayerActivity;
import com.nhance.android.activities.ModuleActivity;
import com.nhance.android.activities.content.players.DocumentPlayerActivity;
import com.nhance.android.activities.content.players.HTMLContentDisplayActivity;
import com.nhance.android.activities.content.players.VideoPlayerActivity;
import com.nhance.android.activities.tests.TestPreAttemptPageActivity;
import com.nhance.android.activities.tests.TestTeacherPageActivity;
import com.nhance.android.assignment.activity.AssigmentTeacherPageActivity;
import com.nhance.android.assignment.activity.AssignmentPreAttemptPageActivity;
import com.nhance.android.async.tasks.AbstractVedantuJSONAsyncTask;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.ContentDataManager;
import com.nhance.android.db.datamanagers.ContentLinkDataManager;
import com.nhance.android.db.models.entity.ContentLink;
import com.nhance.android.enums.EntityType;
import com.nhance.android.enums.MemberProfile;
import com.nhance.android.fragment.doubts.DoubtsFragment;
import com.nhance.android.fragment.doubts.SingleDoubtActivity;
import com.nhance.android.library.utils.LibraryUtils;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.pojos.LibraryContentRes;
import com.nhance.android.recentActivities.Actor;
import com.nhance.android.recentActivities.CommentsActivity;
import com.nhance.android.utils.VedantuWebUtils;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static android.content.ContentValues.TAG;
import static com.nhance.android.library.utils.LibraryUtils._getMemberProfile;


/**
 * Created by prathibha on 3/31/2017.
 */

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{


    private final SessionManager sessionManager;
    public List<Actor> notificationList;
    private NotificationFragment context;
    private int TYPE_ITEM = 1;
    private int TYPE_FOOTER = 2;
    private boolean removeFooter;
    private boolean error;

    public NotificationAdapter(NotificationFragment context, List<Actor> activitiesList) {

        notificationList = activitiesList;
        this.context = context;
        sessionManager = SessionManager.getInstance(context.getActivity());

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

//            View itemView = LayoutInflater.from(parent.getContext())
//                    .inflate(R.layout.notification_list_item, parent, false);
//            return new ItemViewHolder(itemView);

        if (viewType == TYPE_FOOTER) {
            //Inflating footer view
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.footer_recycler_layout, parent, false);
            return new FooterViewHolder(itemView);
        }else{
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_list_item, parent, false);
            return new ItemViewHolder(itemView);
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if(holder instanceof ItemViewHolder) {

            final ItemViewHolder itemHolder = (ItemViewHolder) holder;

            Actor actor = notificationList.get(position);

            Log.e("actor", actor.toString());
            itemHolder.messageTextView.setText(actor.message);
            itemHolder.dateTextView.setText(getlongtoago(Long.parseLong(actor.dateTime)));
        }else if(holder instanceof FooterViewHolder){
            FooterViewHolder footerHolder = (FooterViewHolder) holder;
            footerHolder.footerText.setText("Load More..");
            footerHolder.footerText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                        Actor actor = (notificationList.get(notificationList.size()-1));
                    ((NotificationFragment)context).loadMore(notificationList.size() , actor.id);
                }
            });
        }
    }



    @Override
    public int getItemCount() {

        return notificationList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == notificationList.size()-1 && !removeFooter) {
            return TYPE_FOOTER;
        }else{
            return TYPE_ITEM;
        }

    }


    // inner class to hold a reference to each item of RecyclerView
    private class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView messageTextView;
        public TextView dateTextView;
        public LinearLayout itemLayout;


        public ItemViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            itemLayout = (LinearLayout) itemLayoutView.findViewById(R.id.item_layout);
            messageTextView = (TextView) itemLayoutView.findViewById(R.id.message_text_view);
            dateTextView = (TextView) itemLayoutView.findViewById(R.id.time_text);
            itemLayoutView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            final Actor object = notificationList.get(getAdapterPosition());
            JSONObject src = object.srcObject;

            Log.e("on Click", "Added...."+object.type);
            switch (object.type) {
                case "DISCUSSION":
                    String doubtId = null;
                    try {
                         doubtId = src.getString("id");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if(doubtId != null)
                    new GetSingleDoubtAsyncTask(sessionManager, null, "getDiscussionInfo", doubtId).executeTask(false);

                    break;
                case "STATUSFEED":
                    Log.e("on Click", "comment");
                    String rootId = null, rootType = null;

                    try {
                        rootId = src.getString("id");
                        rootType = src.getString("type");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
//                    new GetStatusFeedAsyncTask(SessionManager.getInstance(context.getActivity()), null, rootId, rootType).executeTask(false);
                    Intent intent = new Intent(context.getActivity(), CommentsActivity.class);
                    intent.putExtra("rootId", rootId);
                    intent.putExtra("rootType", rootType);
                    intent.putExtra("position", getAdapterPosition());
                    intent.putExtra("fromNotification", true);
                    (context.getActivity()).startActivityForResult(intent, 100);
                    break;
                case "TEST":
                    try {
                        loadingContent(src.getString("id"), src.getString("type"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case "ASSIGNMENT":
                    Log.e("on Click", "assignment");
                    try {
                        loadingContent(src.getString("id"), src.getString("type"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;

                case "MODULE":
                    try {
                        loadingContent(src.getString("id"), src.getString("type"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case "VIDEO":
                    try {
                        loadingContent(src.getString("id"), src.getString("type"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case "DOCUMENT":
                    try {
                        loadingContent(src.getString("id"), src.getString("type"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case "FILE":
                    try {
                        loadingContent(src.getString("id"), src.getString("type"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case "HTMLCONTENT":
                    try {
                        loadingContent(src.getString("id"), src.getString("type"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
//                case "QUESTION":
//                    result = entityProcessor.getQuestion(src);
//                    break;
               //case "CHALLENGEARENA":
                   // result = entityProcessor.getChallenge(src);
                   // break;

//                case "REMARK":
//                    result = entityProcessor.getRemark(src, feed.getJSONObject("srcOwner"));
//                    break;
//                case "SOLUTION":
//                    result = entityProcessor.getSolution(src);
//                    break;
//
//
//
//
//
                //case "PLAYLIST" : return getPlaylist(src);
//                default:
//                    return "Entity";
            }
//               else if (v.getId() == videoViewLayout.getId()) {
//
//                    try {
//                        JSONObject src = object.srcObject;
//                        loadingContent(src.getString("id"), src.getString("type"));
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//
//
//                } else if (v.getId() == youTubeVideoViewLayout.getId()) {
//
//                    try {
//                        JSONObject src = object.srcObject;
//                        JSONObject sourceContent = src.getJSONObject("sourceContent");
//                        String url = sourceContent.getString("url");
//
//                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
////                    startActivity(intent);
////                    loadingContent(src.getString("id"), src.getString("type"));
////                    Intent intent= new Intent(context, WebActivity.class);
////                    intent.putExtra("url",url);
//                        (context).startActivity(intent);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
////                Toast.makeText(v.getContext(), "ROW PRESSED = " + String.valueOf(getAdapterPosition()), Toast.LENGTH_SHORT).show();
//                } else if (v.getId() == commentsLayout.getId()) {
//
//
//                }

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



    public void removeFooter(){
        removeFooter = true;
    }

    private void loadingContent(String entityId, String entityType){
        String invalidContentMsg = "OOPs! Invalid V(QR) Code :(";

        ContentLinkDataManager linkDataManager = new ContentLinkDataManager(
                context.getActivity());
        ContentLink contentLink = linkDataManager.getContentLink(entityId, EntityType
                .valueOfKey(entityType).name(), sessionManager
                .getSessionStringValue(ConstantGlobal.USER_ID), null, null, sessionManager
                .getSessionIntValue(ConstantGlobal.ORG_KEY_ID));
        error = contentLink == null;
        if (error) {
            Toast.makeText(context.getActivity(), "OOPs! Seem content is not available locally, sync your library to get latest content :(",
                    Toast.LENGTH_LONG).show();
            return;
        }
        LibraryContentRes libraryContentRes = new ContentDataManager(
                context.getActivity()).getLibraryContentRes(contentLink.linkId);
        if (libraryContentRes != null) {
            LibraryUtils.onLibraryItemClickListnerImpl(context.getActivity(),
                    libraryContentRes);
        } else {
            Toast.makeText(context.getActivity(), invalidContentMsg, Toast.LENGTH_LONG).show();
            return;
        }


    }

    public class GetSingleDoubtAsyncTask extends AbstractVedantuJSONAsyncTask {


        private final ProgressDialog pDialog;
        private String doubtId;


        public GetSingleDoubtAsyncTask(SessionManager session, ProgressBar progressUpdater, String url, String doubtsId) {
            super(session, progressUpdater);
            this.url = session.getApiUrl(url);
            pDialog = new ProgressDialog(context.getActivity());
            doubtId = doubtsId;
            pDialog.setMessage("Please wait..");
            pDialog.setCancelable(false);
            if(!(context.getActivity().isFinishing())){
                pDialog.show();
            }

            Log.e("getSingleDoubtAsyncTask", "call");
        }
        @Override
        protected JSONObject doInBackground(String... params) {

            session.addSessionParams(httpParams);

            httpParams.put("callingApp", "TapApp");
            httpParams.put("callingUserId", session.getOrgMemberInfo().userId);
            httpParams.put("userRole", session.getOrgMemberInfo().profile);
            httpParams.put("callingAppId", "TapApp");
            httpParams.put("orgId",session.getOrgMemberInfo().orgId);
            httpParams.put("id", doubtId);
            httpParams.put("parent.type", "ORGANIZATION");
            httpParams.put("userId",session.getOrgMemberInfo().userId);
            httpParams.put("parent.id",session.getOrgMemberInfo().orgId);
            httpParams.put("myUserId",session.getOrgMemberInfo().userId);
            httpParams.put("memberId",session.getOrgMemberInfo().firstName);


            JSONObject res = null;
            try {
                res = VedantuWebUtils.getJSONData(url, VedantuWebUtils.GET_REQ, httpParams);
                Log.e("DoubtAsyncTask", "do in background");

            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            } finally {
                error = checkForError(res);
            }

            if (isCancelled()) {
                Log.i(TAG, "task canceled by user");
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

                    if (result != null) {

                        Intent intent = new Intent(context.getActivity(), SingleDoubtActivity.class);
                        intent.putExtra("DOUBT_DATA", result.toString());

                        (context.getActivity()).startActivity(intent);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            pDialog.dismiss();


        }
    }





}
