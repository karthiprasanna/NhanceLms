package com.nhance.android.message;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.nhance.android.R;
import com.nhance.android.activities.baseclasses.NhanceBaseFragment;
import com.nhance.android.assignment.StudentPerformance.NetUtils;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.message.Adapter.MessageAdapter;
import com.nhance.android.utils.VedantuWebUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**

 * Use the {@link MessageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MessageFragment extends NhanceBaseFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private ChatItem chatItem = new ChatItem();
    private LinearLayoutManager mLayoutManager;
    private List<ChatItem> feedsList;
    private RecyclerView mRecyclerView;
    // private MessageListRetriverTask messageListRetriverTask;
    private JSONObject resultobject;
    private String totalhits;
    private SessionManager session;
    private String readuserconversationId;
    private JSONArray list;
    private JSONObject result;
    private String totalHits;
    private ArrayList<String> messagelist;
    private String firstName;
    private String thumbnail;
    private String subject;
    private String profile;
    private String date;
    private String userConversationId;
    private String conversationId;
    private String firstMessageId;
    private JSONObject markconversation;
    private MessageAdapter mAdapter;
    private String unreadcount;
     private int mNotificationsCount = 0;

    ActionMode mActionMode;
    Menu context_menu;

    FloatingActionButton fab;
    RecyclerView recyclerView;
    MessageAdapter multiSelectAdapter;
    boolean isMultiSelect = false;
    private MenuItem refreshButton;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView EmptymsgTextview;

    ArrayList<ChatItem> user_list = new ArrayList<>();
    ArrayList<ChatItem> multiselect_list = new ArrayList<>();
    private MessageFragment fragment;


    public MessageFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public MessageFragment newInstance(String param1, String param2) {
        MessageFragment fragment = new MessageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        setHasOptionsMenu(true);
        return fragment;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        session = SessionManager.getInstance(getActivity());
        feedsList = new ArrayList<>();
        messagelist = new ArrayList<>();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.activity_main, container, false);


        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view1);
        EmptymsgTextview = (TextView)view.findViewById(R.id.EmptymsglistTextView);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayoutmsg);
        mLayoutManager = new LinearLayoutManager(this.getActivity());

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(multiSelectAdapter);

        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);



        Log.d("debugMode", "The application stopped after this");
        setHasOptionsMenu(true);

        Intent intent =getActivity().getIntent();
        //session = SessionManager.getInstance(getActivity());
        userConversationId = intent.getStringExtra("userConversationId");
        conversationId = intent.getStringExtra("conversationId");
        firstMessageId = intent.getStringExtra("firstMessageId");



        if(NetUtils.isOnline(getActivity())) {
            new MessageAsyncTask().execute();

        } else{
            Toast.makeText(getActivity(), R.string.no_internet_msg, Toast.LENGTH_SHORT).show();
        }

      //  new FetchCountTask().execute();

        // new MessageReadAsyncTask().execute();

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                new MessageAsyncTask().execute();

            }

        });
        return view;

    }



        @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        
        
        menu.add(0, R.id.action_add, 0,
                getResources().getString(R.string.add))
                .setIcon(R.drawable.ic_add)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

      /*  menu.add(0, R.id.refresh_msg_btn, 0,
                getResources().getString(R.string.refreshmsg))
                .setIcon(R.drawable.refresh)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        */

       /* refreshButton = menu.findItem(R.id.refresh_msg_btn);
        refreshButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
          
            @Override
            public boolean onMenuItemClick(MenuItem item) {
             
                item.setActionView(R.layout.progress_bar_in_action_bar);
                item.setTitle(R.string.loading);
               new  MessageFragment();

                return false;
            }
        });*/
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        if(NetUtils.isOnline(getActivity())) {

            if (id == R.id.action_add) {

                Intent intent = new Intent(getActivity(), SendNewMessage.class);


                this.startActivity(intent);

                return true;
            }
        }else{
            Toast.makeText(getActivity(), R.string.no_internet_msg, Toast.LENGTH_SHORT).show();
        }

       /* if (item.getItemId() == R.id.refresh_msg_btn) {


            item.setActionView(R.layout.progress_bar_in_action_bar);
            item.setTitle(R.string.loading);
            new MessageFragment();
            mAdapter.notifyDataSetChanged();

            return true;

        }
*/


        return super.onOptionsItemSelected(item);
    }




    private void enableActionMode(int position) {
    }



    public class MessageAsyncTask extends AsyncTask<JSONObject, JSONObject, JSONObject> {

        private ProgressDialog loading;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading=new ProgressDialog(getActivity());
            loading.setMessage("Please wait...");
            loading.show();}

        @Override
        protected JSONObject doInBackground(JSONObject... jsonObjects) {
            String url = session.getApiUrl("getConversationSummaries");

            JSONObject jsonRes = null;
            Map<String, Object> httpParams = new HashMap<String, Object>();
            httpParams.put("callingApp", "TapApp");
            httpParams.put("callingUserId", session.getOrgMemberInfo().userId);
            httpParams.put("userRole", session.getOrgMemberInfo().profile);
            httpParams.put("callingAppId", "TapApp");
            httpParams.put("orgId", session.getOrgMemberInfo().orgId);
            httpParams.put("userId", session.getOrgMemberInfo().userId);
            httpParams.put("myUserId", session.getOrgMemberInfo().userId);
            httpParams.put("memberId", session.getOrgMemberInfo().firstName);
            httpParams.put("messageCount", session.getOrgMemberInfo().userId);
            httpParams.put("messagesUnread", session.getOrgMemberInfo().userId);
            // session.addSessionParams(httpParams);
            try {
                jsonRes = VedantuWebUtils.getJSONData(url, VedantuWebUtils.GET_REQ, httpParams);
                Log.e("jsonresp", "......." + jsonRes);

            } catch (Exception e) {
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
                    result = jsonObject.getJSONObject("result");
                    if (result != null) {
                        feedsList.clear();


                        if (!result.getString("totalHits").equalsIgnoreCase("0")) {
                            //  totalHits = result.getString("totalHits");

                            list = result.getJSONArray("list");
                            Log.e("list of response", "......." + list);

                            for (int i = 0; i < list.length(); i++) {


                                JSONObject msglist = list.getJSONObject(i);
                                ChatItem chatItem = new ChatItem();
                                JSONObject mostRecentSender = msglist.getJSONObject("mostRecentSender");
                                Log.e("mostRecentSender", ".........." + mostRecentSender);
                                chatItem.firstName = mostRecentSender.getString("firstName");
                                chatItem.thumbnail = mostRecentSender.getString("thumbnail");
                                chatItem.subject = msglist.getString("subject");

                                // chatItem.isRead=msglist.getBoolean("status");

                                chatItem.timestamp = msglist.getLong("timestamp");
                                chatItem.conversationId = msglist.getString("conversationId");
                                chatItem.isRead = msglist.getString("status");
                                chatItem.userConversationId = msglist.getString("userConversationId");
                                chatItem.firstMessageId = msglist.getString("firstMessageId");
                                Log.e("chat item", ".." + chatItem.toString());
                                feedsList.add(chatItem);
                                Log.e("feed item list", ".." + feedsList.size());
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            if(feedsList != null && feedsList.size()>0){
                EmptymsgTextview.setVisibility(View.GONE);
            }else{
                EmptymsgTextview.setVisibility(View.VISIBLE);
            }

            mAdapter = new MessageAdapter(getContext(), feedsList);
            mRecyclerView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
            mSwipeRefreshLayout.setRefreshing(false);

        }


    }




/*

    */
/*
         Sample AsyncTask to fetch the notifications count
         *//*

    class FetchCountTask extends AsyncTask<JSONObject, JSONObject, JSONObject> {

        private JSONObject result1;

        @Override
        protected JSONObject doInBackground(JSONObject... jsonObjects) {


            String url = session.getApiUrl("getUserMailBoxInfo");
            JSONObject jsonRes = null;


            Map<String, Object> httpParams = new HashMap<String, Object>();
            httpParams.put("callingApp", "TapApp");
            httpParams.put("callingUserId", session.getOrgMemberInfo().userId);
            httpParams.put("userRole", session.getOrgMemberInfo().profile);
            httpParams.put("callingAppId", "TapApp");
            httpParams.put("orgId", session.getOrgMemberInfo().orgId);
            httpParams.put("userId", session.getOrgMemberInfo().userId);
            httpParams.put("myUserId", session.getOrgMemberInfo().userId);
            httpParams.put("memberId", session.getOrgMemberInfo().firstName);


            try {
                jsonRes = VedantuWebUtils.getJSONData(url, VedantuWebUtils.GET_REQ, httpParams);
                Log.e("MessageUnreadcountRes", "......." + jsonRes);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return jsonRes;


        }

        @Override
        public void onPostExecute(JSONObject jsonObject) {

            super.onPostExecute(jsonObject);
            if(jsonObject != null) {
                try {
                    result1 = jsonObject.getJSONObject("result");
                    //totalHits=result.getString("totalHits");
                    unreadcount = result1.getString("unreadConversationCount");
                    Log.e("UnreadMessagecount", "......." + unreadcount);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            //updateNotificationsBadge(unreadcount);

        }

    }
*/




   /* public class MessageReadAsyncTask extends AsyncTask<JSONObject, JSONObject, JSONObject> {

        private JSONObject result2;

        String url = session.getApiUrl("markConversation");
        @Override
        protected JSONObject doInBackground(JSONObject... jsonObjects) {


            System.out.println("Url ---"+url);


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
            httpParams.put("status", "READ");



            httpParams.put("conversationId",conversationId);
            httpParams.put("userConversationId",userConversationId);
            Log.e("status", "......." +  httpParams);



           // session.addSessionParams(httpParams);
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

            if(jsonObject != null) {
                try {
                    result2 = jsonObject.getJSONObject("result");
                    conversationId = result2.getString("id");
                    Log.e("Id", "......." + conversationId);
                    userConversationId = result2.getString("marked");
                    Log.e("Read", "......." + userConversationId);


                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }




        }


    }

*/


}







