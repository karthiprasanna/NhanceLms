package com.nhance.android.message.Adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nhance.android.R;
import com.nhance.android.activities.AppLandingPageActivity;
import com.nhance.android.assignment.StudentPerformance.NetUtils;
import com.nhance.android.enums.NavigationItem;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.message.ChatActivity;
import com.nhance.android.message.ChatItem;
import com.nhance.android.message.MessageFragment;
import com.nhance.android.utils.VedantuWebUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.R.attr.key;
import static android.R.attr.value;
import static android.content.Context.MODE_PRIVATE;
import static com.nhance.android.R.id.url;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {

    public ArrayList<ChatItem> usersList = new ArrayList<>();
    public ArrayList<ChatItem> selected_usersList = new ArrayList<>();


    private String android_image_url;

    private Context context;
    private LayoutInflater inflater;
    private List<ChatItem> feedsList;
    private ChatItem chatItem = new ChatItem();
    List<ChatItem> data = Collections.emptyList();
    ChatItem chatlist;
    int currentPos = 0;
    ProgressDialog pd;

    AppLandingPageActivity apa = new AppLandingPageActivity();
    // array used to perform multiple animation at once
    private SparseBooleanArray animationItemsIndex;
    private boolean reverseAllAnimations = false;

    // index is used to animate only the selected row
    // dirty fix, find a better solution
    private static int currentSelectedIndex = -1;
    private MessageAdapter mAdapter;
    private Object i;
    private SessionManager session;
    private String conversationId, userConversationId;

    private String readuserconversationId, deleteuserconversationId;
    public int unreadcount;

    SharedPreferences pref;


    // create constructor to innitilize context and data sent from MainActivity
    public MessageAdapter(Context context, List<ChatItem> data) {
      pd = new ProgressDialog(context);
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.data = data;
        session = SessionManager.getInstance(context);







    }


    // Inflate the layout when viewholder created
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_row, parent, false);
        MyHolder holder = new MyHolder(view);
        return holder;
    }

    // Bind data

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {


        final MyHolder myHolder = (MyHolder) holder;
        final ChatItem current = data.get(position);
        myHolder.username.setText(current.firstName);
        myHolder.subject.setText(current.subject);
        myHolder.timestamp.setText(getDate(current.timestamp));
        new DownloadImageTask(myHolder.thumbnail).execute(current.thumbnail);

        // Picasso.with(context).load (String.valueOf(myHolder.thumbnail)).resize(45, 45).into(myHolder.thumbnail);

        //  Picasso.with(context).load(String.valueOf(data.get(position)))
        //       .into((myHolder.thumbnail));

        applyReadStatus(myHolder, current);


        myHolder.itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent detailIntent = new Intent(v.getContext(), ChatActivity.class);
                detailIntent.putExtra("conversationId", current.conversationId);
                detailIntent.putExtra("userConversationId", current.userConversationId);
                detailIntent.putExtra("firstMessageId", current.firstMessageId);

                if(NetUtils.isOnline(context)) {

                    if (current.isRead.equalsIgnoreCase("UNREAD")) {
                        System.out.println("Url ---" + current.conversationId + " " + current.firstMessageId);
                        context.startActivity(detailIntent);
                        readuserconversationId = current.userConversationId;
//<<<<<<< HEAD
//                    NavigationItem navItem = NavigationItem.valueOfKey("MESSAGE");
//                    NavigationItem navItem1 = NavigationItem.valueOfKey("NOTIFICATIONS");
//=======
//>>>>>>> a482651990bdf46b349a56d7776d9ed0379dc844
                        new MessageReadAsyncTask().execute();

                        current.isRead = "READ";
                        applyReadStatus(myHolder, current);

                        SharedPreferences pref = context.getSharedPreferences("MyPrefsFile", MODE_PRIVATE);
//<<<<<<< HEAD
//                   unreadcount = pref.getInt("unreadcount",0);
//                    notificationCount = pref.getInt("notificationCount", 0);
//                  unreadcount=unreadcount-1;
//                    SharedPreferences.Editor editor = context.getSharedPreferences("MyPrefsFile", MODE_PRIVATE).edit();
//                    editor.putInt("unreadcount",unreadcount);
//                    editor.apply();
//                   apa.populateCount(navItem, notificationCount, unreadcount);
//
//
//                apa.populateCount(navItem1, notificationCount,unreadcount);
//
//
//=======
                        unreadcount = pref.getInt("unreadcount", 0);
                        unreadcount = unreadcount - 1;
                        SharedPreferences.Editor editor = context.getSharedPreferences("MyPrefsFile", MODE_PRIVATE).edit();
                        editor.putInt("unreadcount", unreadcount);
                        editor.apply();
//>>>>>>> a482651990bdf46b349a56d7776d9ed0379dc844


                    } else {

                        System.out.println("Url ---" + current.conversationId + " " + current.firstMessageId);
                        context.startActivity(detailIntent);

                    }
                }else{
                        Toast.makeText(context, R.string.no_internet_msg, Toast.LENGTH_SHORT).show();
                    }


                    notifyDataSetChanged();



            }
        });


        myHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {


                AlertDialog.Builder alert = new AlertDialog.Builder(view.getContext());

                alert.setTitle("Delete");
                alert.setMessage("Are you sure to delete Message");
                alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                        deleteuserconversationId = current.userConversationId;
                        // feedsList.remove(current.userConversationId);
                        data.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, data.size());

                        if(NetUtils.isOnline(context)) {
                            new DeleteMessageAsyncTask().execute();

                        } else{
                            Toast.makeText(context, R.string.no_internet_msg, Toast.LENGTH_SHORT).show();
                        }
                        Toast.makeText(context, "Message has been successfully Deleted", Toast.LENGTH_SHORT).show();



                    }



                });

                alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });

                alert.show();

                return true;
            }

        });

    }


    private void notifyDataSetChanged(int userConversationId) {
    }


    private void applyReadStatus(MyHolder holder, ChatItem data) {
        if (data.isRead.equalsIgnoreCase("READ")) {
            holder.re.setBackgroundColor(ContextCompat.getColor(context, R.color.white_trans));
        } else {
            holder.re.setBackgroundColor(ContextCompat.getColor(context, R.color.lightergrey));
        }
    }




    @Override
    public void onClick(View view) {

    }

    public void toggleSelection(ChatItem position) {
    }


    class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();

            //  pd.setTitle("Loading.... ");
            //pd.show();
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            pd.dismiss();
            bmImage.setImageBitmap(result);
        }
    }

    public String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);
        String date = DateFormat.format("dd-MM-yyyy hh:mm a", cal).toString();
        return date;
    }

    // return total item from List
    @Override
    public int getItemCount() {
        return data.size();
    }

    class MyHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        TextView username;
        ImageView thumbnail, send;
        TextView subject;
        TextView timestamp;
        RecyclerView lv;
        RelativeLayout re;
        EditText editText;

        // create constructor to get widget reference
        public MyHolder(View itemView) {
            super(itemView);
            username = (TextView) itemView.findViewById(R.id.username);
            subject = (TextView) itemView.findViewById(R.id.subject);
            thumbnail = (ImageView) itemView.findViewById(R.id.thumbnail);

            timestamp = (TextView) itemView.findViewById(R.id.date);
            lv = (RecyclerView) itemView.findViewById(R.id.recycler_view1);
            re = (RelativeLayout) itemView.findViewById(R.id.re1);


            // username.setOnClickListener(this);
            // editText = (EditText) itemView.findViewById(R.id.Etext);
            //send = (ImageView) itemView.findViewById(R.id.chatSendButton);
           // thumbnail.setScaleType(ImageView.ScaleType.FIT_XY);
        }

        @Override
        public boolean onLongClick(View view) {
            return false;
        }
    }

    public interface MessageAdapterListener {
        void onIconClicked(int position);

        void onIconImportantClicked(int position);

        void onMessageRowClicked(int position);


        void onRowLongClicked(int position);
    }


    //post text to Message read status
    public class MessageReadAsyncTask extends AsyncTask<JSONObject, JSONObject, JSONObject> {

        private JSONObject result2;

        private ProgressDialog loading;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading=new ProgressDialog(context);
            loading.setMessage("Please wait...");
            loading.show();
        }
        @Override
        protected JSONObject doInBackground(JSONObject... jsonObjects) {

            String url = session.getApiUrl("markConversation");
            System.out.println("Url ---"+url);


            JSONObject jsonRes = null;

            Map<String, Object> httpParams = new HashMap<String, Object>();
            httpParams.put("callingApp", "TapApp");
            httpParams.put("callingUserId", session.getOrgMemberInfo().userId);
            httpParams.put("userRole", session.getOrgMemberInfo().profile);
            httpParams.put("callingAppId", "TapApp");
           // httpParams.put("orgId", session.getOrgMemberInfo().orgId);
            httpParams.put("userId", session.getOrgMemberInfo().userId);
            httpParams.put("memberId", session.getOrgMemberInfo().userId);
            httpParams.put("myUserId", session.getOrgMemberInfo().userId);
            httpParams.put("status", "READ");
            httpParams.put("conversationId",conversationId);
            httpParams.put("userConversationId",readuserconversationId );
            Log.e("status", "......." +  httpParams);




            session.addSessionParams(httpParams);
            try {
                jsonRes = VedantuWebUtils.getJSONData(url, VedantuWebUtils.POST_REQ, httpParams);
                Log.e("markConversation", "......." + jsonRes);

            }catch (Exception e){
                e.printStackTrace();
            }

            return jsonRes;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            loading.dismiss();
            if(jsonObject != null) {
                try {
                    result2 = jsonObject.getJSONObject("result");
                    conversationId = result2.getString("id");
                    Log.e("Id", "......." + conversationId);
                    readuserconversationId = result2.getString("marked");
                    Log.e("Read", "......." + readuserconversationId);


                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }



    //post DeleteMessage
    public class DeleteMessageAsyncTask extends AsyncTask<JSONObject, JSONObject, JSONObject> {

        private JSONObject result2;
        @Override
        protected JSONObject doInBackground(JSONObject... jsonObjects) {

            String url = session.getApiUrl("deleteConversation");
            System.out.println("Urldelete ---"+url);


            JSONObject jsonRes = null;
            Map<String, Object> httpParams = new HashMap<String, Object>();
            httpParams.put("callingApp", "TapApp");
            httpParams.put("callingUserId", session.getOrgMemberInfo().userId);
            httpParams.put("userRole", session.getOrgMemberInfo().profile);
            httpParams.put("callingAppId", "TapApp");
            httpParams.put("orgId", session.getOrgMemberInfo().orgId);
            httpParams.put("userId", session.getOrgMemberInfo().userId);
            httpParams.put("memberId", session.getOrgMemberInfo().userId);
            httpParams.put("myUserId", session.getOrgMemberInfo().userId);
            // httpParams.put("conversationId",conversationId);
            httpParams.put("userConversationId",deleteuserconversationId );
            Log.e("Deletereq", "......." +  httpParams);
            session.addSessionParams(httpParams);

            try {
                jsonRes = VedantuWebUtils.getJSONData(url, VedantuWebUtils.POST_REQ, httpParams);
                Log.e("Delete response", "......." + jsonRes);
            }catch (Exception e){
                e.printStackTrace();
            }
            return jsonRes;
        }


        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            try {
                result2 = jsonObject.getJSONObject("result");
                deleteuserconversationId=result2.getString("id");
                Log.e("Id", "......." + deleteuserconversationId);
                deleteuserconversationId=result2.getString("delete");
                Log.e("Delete", "......." + deleteuserconversationId);
            } catch (JSONException e1) {
                e1.printStackTrace();
            }

        }


    }

}

