package com.nhance.android.recentActivities;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.nhance.android.R;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;


/**
 * Created by prathibha on 3/31/2017.
 */

public class CommentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    public List<Actor> commentsList;
    private Context context;

    public CommentsAdapter(Context context, List<Actor> activitiesList) {

        commentsList = activitiesList;
        this.context = context;


    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chatactivity_list_item, parent, false);
            return new ItemViewHolder(itemView);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
       ItemViewHolder itemHolder = (ItemViewHolder) holder;
        Actor actor = commentsList.get(position);
        itemHolder.profileTextView.setText(actor.firstName+" "+actor.lastName);

//        itemHolder.messageTextView.setText(actor.message);

         itemHolder.messageTextView.loadDataWithBaseURL(null, actor.message, "text/html", "UTF-8", null);
        Picasso.with(context).load(actor.thumbnail).into(itemHolder.commentsImageView);

        String dateTime = convertDate(actor.dateTime,"dd/MM/yyyy hh:mm:ss");
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Date date = null;
//        try {
//            date = inputFormat.parse(dateTime);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
        String niceDateStr = (String) DateUtils.getRelativeTimeSpanString(getGMTDate(Long.parseLong(actor.dateTime)).getTime() , Calendar.getInstance().getTimeInMillis(), DateUtils.MINUTE_IN_MILLIS);

        itemHolder.dateTextView.setText(getlongtoago(Long.parseLong(actor.dateTime)));
    }



    @Override
    public int getItemCount() {

        return commentsList.size();
    }

    // inner class to hold a reference to each item of RecyclerView
    private class ItemViewHolder extends RecyclerView.ViewHolder {

        public ImageView commentsImageView;
        public TextView profileTextView;
        public WebView messageTextView;
        public TextView dateTextView;


        public ItemViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            commentsImageView = (ImageView)itemLayoutView.findViewById(R.id.thumbnail);
            profileTextView = (TextView)itemLayoutView.findViewById(R.id.name);
            messageTextView = (WebView)itemLayoutView.findViewById(R.id.msg);
            dateTextView = (TextView)itemLayoutView.findViewById(R.id.time);

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


    public static String getlongtoago(long createdAt) {


        String time;
        Date d2 = new Date();
        Date d1 = new Date(createdAt);

        Log.e("createdAt", ""+createdAt+"  currenttime "+d2.getTime());
        long diff = d2.getTime() - d1.getTime();
        System.out.println(diff + "  diff");
        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000);
        long diffInDays = diff / (1000 * 60 * 60 * 24);
        int weeks = (int) (diff/ (1000*60*60*24*7));
        long month = diffInDays /30;
        long year = diffInDays / 365;

//        time = diffInDays+" days";

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
        }else{
            time = "moments ago";
        }

        return  time;
    }


}
