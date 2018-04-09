package com.nhance.android.message;


import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nhance.android.R;
import com.nhance.android.assignment.StudentPerformance.NetUtils;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.message.Adapter.ChatActivityAdapter;
import com.nhance.android.utils.VedantuWebUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.support.v7.appcompat.R.id.scrollView;


public class ChatActivity extends AppCompatActivity {


    private static final String TAG = "ChatActivity";
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;

    private EditText messageET;
    private ListView messagesContainer;
    private Button sendBtn;
    private ChatActivityAdapter adapter;



    private SessionManager session;
    private List<ChatItem> feedsList;
    private JSONArray list,receivers;
    private JSONObject result,message,result1,result2,receiver;
    private ChatActivityAdapter mAdapter;
    private ListView mListview;
    private int membercount;
    public String userConversationId,conversationId,firstMessageId,parentMessageId;
    private ChatItem chatItem;
    private String msg;
    private String chat_subject, message_id;
    private String chat_content;
    private String firstName;
    private String totalHits;
    private String thumbnail;
    private String profile;
    private long timestamp;


    TextView txtsubject;
    WebView webview;
    EditText editText;
    ImageView send;
    ImageView img;
    private String submit_editText;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    boolean emptyEditText = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatactivity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().setDisplayShowCustomEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
       // getSupportActionBar().setIcon(R.drawable.ic_launcher);





        //  getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_launcher);
       // mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayoutchat);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(ChatActivity.this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        feedsList=new ArrayList<>();
        mRecyclerView.setAdapter( mAdapter );
        mRecyclerView.setHasFixedSize(true);

        txtsubject = (TextView) findViewById(R.id.subject1);
        webview = (WebView)this.findViewById(R.id.msg1);

        //webview = (WebView) findViewById(R.id.msg1);
        txtsubject.setMovementMethod(new ScrollingMovementMethod());
        webview.getSettings().setJavaScriptEnabled(true);
        WebSettings settings = webview.getSettings();

        settings.setDefaultTextEncodingName("utf-8");
       //improve webview performance
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webview.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
       // settings.setDomStorageEnabled(true);
        webview.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webview.setBackgroundColor(Color.TRANSPARENT);
       // webview.setScrollbarFadingEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setMinimumFontSize(40);

        webview.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
//        webview.loadDataWithBaseURL(null,"<style>img{display: inline;height: auto;max-width: 100%;}</style>" + chat_content, "text/html", "utf-8", null);


       /* content.getSettings().setJavaScriptEnabled(true);
        WebSettings settings = content.getSettings();
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setMinimumFontSize(60);
        content.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        content.setBackgroundColor(Color.TRANSPARENT);
        content.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                view.scrollTo(150,150);
            }
        });*/

        editText = (EditText) findViewById(R.id.Etext);
        send = (ImageView) findViewById(R.id.chatSendButton);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submit_editText=editText.getText().toString().trim();

                //  editText.getText().clear();

                Log.e("submit_editText",".........."+submit_editText);
                if(NetUtils.isOnline(ChatActivity.this)) {

                if( !editText.getText().toString().trim().isEmpty() ) {
                    new ChatAsyncTask2().execute();

                    emptyEditText = true;
                    Toast.makeText(ChatActivity.this, "Message send successfully", Toast.LENGTH_SHORT).show();
                    clearfield();
                }else {

                    emptyEditText = false;
                    Toast.makeText(ChatActivity.this, "Enter Some Text", Toast.LENGTH_SHORT).show();
                }

                }else{
                    Toast.makeText(ChatActivity.this, R.string.no_internet_msg, Toast.LENGTH_SHORT).show();
                }

/*

           if(editText.getText().toString().trim()!= null && !editText.getText().toString().trim().isEmpty()) {
                    new ChatAsyncTask2().execute();
                }else{
                    Toast.makeText(ChatActivity.this, "Please enter some text", Toast.LENGTH_SHORT).show();
                }
*/
                addItemsToList();


            }

        });

//initControls();
        Intent intent = getIntent();
        session = SessionManager.getInstance(this);
        userConversationId = intent.getStringExtra("userConversationId");
        conversationId = intent.getStringExtra("conversationId");
        firstMessageId = intent.getStringExtra("firstMessageId");


        System.out.println("Url ---"+conversationId+ " "+firstMessageId);
        new ChatAsyncTask().execute();


      /*  mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                new ChatAsyncTask1().execute();


            }

        });*/



    }


    private void addItemsToList() {

        new ChatAsyncTask1().execute();
        mAdapter.notifyDataSetChanged();
    }

    private void clearfield(){

        editText.getText().clear();

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);     } }



    public class ChatAsyncTask extends AsyncTask<JSONObject, JSONObject, JSONObject> {
        private ProgressDialog loading;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            loading=new ProgressDialog(ChatActivity.this);
            loading.setMessage("Please wait...");
            loading.show();
        }

        @Override
        protected JSONObject doInBackground(JSONObject... jsonObjects) {

            String url = session.getApiUrl("getMessage");
            System.out.println("Url ---"+url);


            JSONObject jsonRes = null;

            Map<String, Object> httpParams = new HashMap<String, Object>();
            httpParams.put("callingApp", "TapApp");
            httpParams.put("callingUserId", session.getOrgMemberInfo().userId);
            //httpParams.put("userRole", session.getOrgMemberInfo().profile);
            httpParams.put("callingAppId", "TapApp");
            //httpParams.put("orgId", session.getOrgMemberInfo().orgId);
            httpParams.put("userId", session.getOrgMemberInfo().userId);
            httpParams.put("conversationId", conversationId);
            httpParams.put("messageId", firstMessageId);
            httpParams.put("message.sender.id", session.getOrgMemberInfo().userId);



            // session.addSessionParams(httpParams);
            try {
                jsonRes = VedantuWebUtils.getJSONData(url, VedantuWebUtils.GET_REQ, httpParams);
                Log.e("jsonresp", "......." + jsonRes);

            }catch (Exception e){
                e.printStackTrace();
            }

            return jsonRes;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            loading.dismiss();
            try{
                result1=jsonObject.getJSONObject("result");
                message=result1.getJSONObject("message");
                Log.e("summary of response", "......." + message);

                chat_subject=message.getString("subject");
                chat_content=message.getString("content");


                message_id =message.getString("messageId");
                txtsubject.setText(chat_subject);
                //webview.setText(Html.fromHtml(chat_content));
               // webview.loadDataWithBaseURL(null, chat_content, "text/html", "UTF-8", null);
                webview.loadDataWithBaseURL(null,"<style>img{display: inline;height: auto;max-width: 100%;}</style>" + chat_content, "text/html", "UTF-8", null);

                Log.e("chat_content","......."+chat_content);
                new ChatAsyncTask1().execute();

            } catch (JSONException e1) {
                e1.printStackTrace();
            }



        }


    }


    public class ChatAsyncTask1 extends AsyncTask<JSONObject, JSONObject, JSONObject> {

        @Override
        protected JSONObject doInBackground(JSONObject... jsonObjects) {

            String url = session.getApiUrl("getMessageSummaries");
            System.out.println("Url ---"+url);


            JSONObject jsonRes = null;
            Map<String, Object> httpParams = new HashMap<String, Object>();
            httpParams.put("callingApp", "TapApp");
            httpParams.put("callingUserId", session.getOrgMemberInfo().userId);
            httpParams.put("userRole", session.getOrgMemberInfo().profile);
            httpParams.put("callingAppId", "TapApp");
            httpParams.put("orgId", session.getOrgMemberInfo().orgId);
            httpParams.put("userId", session.getOrgMemberInfo().userId);
            httpParams.put("memberId", session.getOrgMemberInfo().memberId);
            httpParams.put("conversationId", conversationId);
            //httpParams.put("messageId", firstMessageId);
            httpParams.put("size", 100);

            // session.addSessionParams(httpParams);
            try {
                jsonRes = VedantuWebUtils.getJSONData(url, VedantuWebUtils.GET_REQ, httpParams);
                Log.e("jsonresp", "......." + jsonRes);

            }catch (Exception e){
                e.printStackTrace();
            }

            return jsonRes;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            try{
                result=jsonObject.getJSONObject("result");
                Log.e("result","........"+result);
                // totalHits=result.getString("totalHits");
                if (result != null) {
                    feedsList.clear();


                    list = result.getJSONArray("list");
                    Log.e("summary of response", "......." + list);

                    for (int i = 0; i < list.length(); i++) {

                        JSONObject msglist = list.getJSONObject(i);
                        JSONObject chat_nsg = msglist.getJSONObject("sender");
                        String firstName = chat_nsg.getString("firstName");
                        Log.e("receiver response", "......." + firstName);
                        String thumbnail = chat_nsg.getString("thumbnail");
                        Log.e("receiver response", "......." + thumbnail);
                        String content = msglist.getString("content");
                        Log.e("receiver response", "......." + content);
                        String profile = chat_nsg.getString("profile");
                        Log.e("receiver response", "......." + profile);
                        Long timestamp = msglist.getLong("timestamp");
                        Log.e("receiver response", "......." + timestamp);
                        String id = chat_nsg.getString("id");

                        ChatItem chatItem = new ChatItem();
                        chatItem.id = id;
                        if (id.equalsIgnoreCase(session.getOrgMemberInfo().userId)) {
                            chatItem.isMe = true;
                        } else {
                            chatItem.isMe = false;
                        }
                        Log.e("isMe ", ".." + chatItem.isMe);
                        chatItem.firstName = firstName;
                        chatItem.content = content;
                        chatItem.thumbnail = thumbnail;
                        chatItem.timestamp = timestamp;
                        feedsList.add(chatItem);
                        Log.e("chat item", ".." + chatItem.toString());
                        Log.e("feed item list", ".." + feedsList.size());
                    }
                }

            }catch (JSONException e1) {
                e1.printStackTrace();
            }
            Collections.reverse(feedsList);
            mAdapter = new ChatActivityAdapter(ChatActivity.this, feedsList);
            mRecyclerView.setAdapter(mAdapter);
             //  mAdapter.notifyDataSetChanged();
            //  mSwipeRefreshLayout.setRefreshing(false);
        }
    }



    //post text to server
    public class ChatAsyncTask2 extends AsyncTask<JSONObject, JSONObject, JSONObject> {
        @Override
        protected JSONObject doInBackground(JSONObject... jsonObjects) {
            String url = session.getApiUrl("sendMessage");
            System.out.println("Url ---"+url);

            JSONObject jsonRes = null;
            Map<String, Object> httpParams = new HashMap<String, Object>();
            httpParams.put("callingApp", "TapApp");
            httpParams.put("callingUserId", session.getOrgMemberInfo().userId);
            httpParams.put("userRole", session.getOrgMemberInfo().profile);
            httpParams.put("callingAppId", "TapApp");
            httpParams.put("userId", session.getOrgMemberInfo().userId);
            httpParams.put("memberId", session.getOrgMemberInfo().userId);
            httpParams.put("messageId", firstMessageId);
            httpParams.put("message.conversationId",conversationId );
            httpParams.put("myUserId", session.getOrgMemberInfo().userId);
            httpParams.put("message.content", submit_editText);
            Log.e("message.content", "......." +   submit_editText);
            httpParams.put(" message.action", "REPLY");
            httpParams.put("message.sender.type",  "USER");
            httpParams.put("message.parentMessageId", message_id);
            Log.e("message.parentMessageId", "......." +   message_id);
            httpParams.put("message.sender.id", session.getOrgMemberInfo().userId);
            session.addSessionParams(httpParams);
            try {
                jsonRes = VedantuWebUtils.getJSONData(url, VedantuWebUtils.POST_REQ, httpParams);
                Log.e("chatresponse", "......." + jsonRes);

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
                //totalHits=result.getString("totalHits");
                message = result2.getJSONObject("message");
                Log.e("summary of response", "......." + message);
                message = message.getJSONObject("receiver");
                String firstName=message.getString("firstName");
                Log.e("receiver response","......."+firstName);
                String thumbnail=message.getString("thumbnail");
                Log.e("receiver response","......."+thumbnail);
                String content=message.getString("content");
                Log.e("receiver response","......."+content);
                String profile=message.getString("profile");
                Log.e("receiver response","......."+profile);
                Long timestamp=message.getLong("timestamp");
                Log.e("receiver response","......."+timestamp);
                //  new ChatAsyncTask2().execute();
                feedsList.add(chatItem);

            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            // mAdapter.notifyDataSetChanged();
            // Collections.reverse(feedsList);
            mAdapter = new ChatActivityAdapter(ChatActivity.this, feedsList);
            mRecyclerView.setAdapter(mAdapter);
        }


    }


}


