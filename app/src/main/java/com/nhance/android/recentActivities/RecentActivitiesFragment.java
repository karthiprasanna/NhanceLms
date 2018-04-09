package com.nhance.android.recentActivities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.nhance.android.R;
import com.nhance.android.activities.AppLandingPageActivity;
import com.nhance.android.activities.about.AboutActivity;
import com.nhance.android.async.tasks.AbstractVedantuAsynTask;
import com.nhance.android.async.tasks.AnalyticsSyncer;
import com.nhance.android.async.tasks.ITaskProcessor;
import com.nhance.android.async.tasks.socials.WebCommunicatorTask;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.OrgDataManager;
import com.nhance.android.db.datamanagers.UserDataManager;
import com.nhance.android.db.models.UserOrgProfile;
import com.nhance.android.enums.EntityType;
import com.nhance.android.fragments.AbstractVedantuFragment;
import com.nhance.android.managers.LibraryManager;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.pojos.OrgMemberInfo;
import com.nhance.android.pojos.OrgMemberMappingInfo;
import com.nhance.android.utils.JSONUtils;
import com.nhance.android.utils.VedantuWebUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nhance.android.utils.VedantuWebUtils.TAG;

/**

 * Use the {@link RecentActivitiesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecentActivitiesFragment extends AbstractVedantuFragment implements ITaskProcessor<JSONObject> {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final int ADDNEWFEED = 102;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private RecentListRetrieverTask recentListRetreiverTask;
    private JSONObject resultobject;
    private String totalhits;
    private JSONArray activitesList;
    private List<JSONArray> clusteredNewstotalList;
    private List<JSONObject> adapterActivitiesList;
    private JSONArray clusteredNewsList;
    private RecentActivitiesAdapter mAdapter;
    private ProgressDialog pDialog;
    private SessionManager sessionManager;
    private View rootView;
    private String selectedSectionId;
    private String selectedProgramId;
    private String selectedCenterId;
    private String eType, eId;
    private Button addNewFeedButton;
    private String organizationName;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private JSONObject srcObject;
    private int commentsCount;
    private AppLandingPageActivity      appLandingPageActivityInstance;
    private View navigationDrawerSyncButton;
    private boolean loadingNewCoursesContent = false;
    private AnalyticsSyncer analyticsSyncer;

    public RecentActivitiesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RecentActivitiesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RecentActivitiesFragment newInstance(String param1, String param2) {
        RecentActivitiesFragment fragment = new RecentActivitiesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        Activity appLandingPageActivity = getActivity();
        if (appLandingPageActivity instanceof AppLandingPageActivity) {
            appLandingPageActivityInstance = (AppLandingPageActivity) getActivity();
        }
        clusteredNewstotalList = new ArrayList<JSONArray>();
        adapterActivitiesList = new ArrayList<JSONObject>();
        sessionManager = SessionManager.getInstance(m_cObjNhanceBaseActivity);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_recent_activities, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
        addNewFeedButton = (Button)rootView.findViewById(R.id.add_new_feed);
        mLayoutManager = new LinearLayoutManager(this.getActivity());
        Log.d("debugMode", "The application stopped after this");
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new RecentActivitiesAdapter(getActivity(), adapterActivitiesList, RecentActivitiesFragment.this);
        mRecyclerView.setAdapter(mAdapter);
        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage("Loading Recent Activities..");
        pDialog.setCancelable(false);
        eType = "ORGANIZATION";
        eId = sessionManager.getOrgMemberInfo().orgId;
//        getAllActivities(eType, eId);

        populateSections();
        addNewFeedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddFeedActivity.class);
                startActivityForResult(intent, ADDNEWFEED);
            }
        });
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                populateSections();
            }
        });

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.library_courses, menu);
        progressMenuItem = menu.findItem(R.id.action_library_courses_progress);
        showRefreshButton();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_logout) {
            sessionManager.logoutUser();
        } else if (item.getItemId() == R.id.action_about) {
            Intent intent = new Intent(m_cObjNhanceBaseActivity, AboutActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.action_library_courses_progress) {
            if (!loadingNewCoursesContent) {
                syncClickedProcess();
            }
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        setUpLibraryRefreshButton();
        super.onActivityCreated(savedInstanceState);
    }

    private void setUpLibraryRefreshButton() {

        if (appLandingPageActivityInstance != null) {
            navigationDrawerSyncButton = appLandingPageActivityInstance.getNavigationDrawerLayout()
                    .findViewById(R.id.navigation_sync_button);
            navigationDrawerSyncButton.setVisibility(View.VISIBLE);

            View.OnClickListener syncListner = new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    syncClickedProcess();
                }
            };

            navigationDrawerSyncButton.setOnClickListener(syncListner);
        }
    }

    private void syncClickedProcess() {
        View textViewProgress = appLandingPageActivityInstance.getNavigationDrawerLayout()
                .findViewById(R.id.navigation_sync_in_progress);
        Log.d(TAG, "fetching content");
        boolean online = SessionManager.isOnline();
        if (!online) {
            Toast.makeText(m_cObjNhanceBaseActivity, getString(R.string.no_internet_msg),
                    Toast.LENGTH_LONG).show();
            return;
        }
        navigationDrawerSyncButton.setVisibility(View.GONE);
        textViewProgress.setVisibility(View.VISIBLE);
        if (progressMenuItem != null) {
            Log.d(TAG, "showing progess");
            showProgressBar();
        }
        loadingNewCoursesContent = true;
        appLandingPageActivityInstance.getDrawerLayout().closeDrawer(
                appLandingPageActivityInstance.getNavigationDrawerLayout());
        // fetch user update orgMemberProfile
        // added by Shankar
        WebCommunicatorTask memberProfileFetcherTask = new WebCommunicatorTask(sessionManager,
                null, WebCommunicatorTask.ReqAction.GET_ORG_MEMBER_PROFILE, new ITaskProcessor<JSONObject>() {

            @Override
            public void onTaskStart(JSONObject result) {

            }

            @Override
            public void onTaskPostExecute(boolean success, JSONObject result) {

                if (success) {
                    result = JSONUtils.getJSONObject(result, VedantuWebUtils.KEY_RESULT);

                    JSONObject orgProfileInfo = JSONUtils.getJSONObject(result,
                            ConstantGlobal.INFO);
                    if (orgProfileInfo.length() > 0) {
                        UserDataManager userDataManager = new UserDataManager(m_cObjNhanceBaseActivity);
                        UserOrgProfile userOrgProfile = userDataManager.getUserOrgProfile(
                                sessionManager
                                        .getSessionStringValue(ConstantGlobal.USER_ID),
                                sessionManager.getSessionStringValue(ConstantGlobal.ORG_ID));
                        if (userOrgProfile == null) {
                            return;
                        }

                        try {
                            userOrgProfile.orgProfile.put(ConstantGlobal.INFO,
                                    orgProfileInfo);
                            userDataManager.updateUserOrgProfile(userOrgProfile);
                            sessionManager.updateUserOrgProfile(userOrgProfile);
                        } catch (Throwable e) {
                            Log.e(TAG, e.getMessage(), e);
                        }
                    }
                }
                new LibrarySyncer(sessionManager.getOrgMemberInfo()).executeTask(false);
            }
        }, null, null, 0);
        memberProfileFetcherTask.addExtraParams("ensureCourseInfo", String.valueOf(true));
        memberProfileFetcherTask.executeTask(false);
    }



    @Override
    public void onTaskStart(JSONObject result) {

    }

    @Override
    public void onTaskPostExecute(boolean success, JSONObject result) {
        if(result != null) {
            Log.d("onTask post Execute", ".." + result.toString());

            try {

                    resultobject = result.getJSONObject("result");
                    Log.e("result", "......" + resultobject);

                if (!resultobject.getString("totalHits").equalsIgnoreCase("0")) {
                    activitesList = resultobject.getJSONArray("list");
                    Log.e("activitesList", "......" + activitesList);

                    for (int i = 0; i < activitesList.length(); i++) {
                        JSONObject object = activitesList.getJSONObject(i);

                clusteredNewsList = object.getJSONArray("clusteredNews");
                        adapterActivitiesList.add((JSONObject) clusteredNewsList.get(0));
//                Log.e("clusteredNewsList","......"+clusteredNewsList);
//                clusteredNewstotalList.add(clusteredNewsList);

                        Log.e("adapterActivitiesList", "......" + adapterActivitiesList.size());

                    }
                   if(!resultobject.getString("totalHits").equalsIgnoreCase("10")){
                        mAdapter.removeFooter();
                    }
                    mAdapter.notifyDataSetChanged();

                }
                } catch(JSONException e){
                    e.printStackTrace();
                }


        }else if(appLandingPageActivityInstance.isOnline()){
            Toast.makeText(getActivity(), "Server error please try again later", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getActivity(), "Please check network connectivity", Toast.LENGTH_SHORT).show();
        }
        mSwipeRefreshLayout.setRefreshing(false);

    }

    public void loadMore(int size, String activityId){
        recentListRetreiverTask = new RecentListRetrieverTask(sessionManager, null, this, size, "getOlderActivityFeeds", activityId, pDialog, eType, eId);
        recentListRetreiverTask.executeTask(false);
    }

    @Override
    public void onResume() {
        super.onResume();


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if((requestCode == ADDNEWFEED ) && data!= null ){
            Log.e("onActivityResult......", "called"+eType+".."+eId);
//            eType = "ORGANIZATION";
//            eId = sessionManager.getOrgMemberInfo().orgId;
            String arg = data.getStringExtra("bundle");
            JSONObject obj = null;
            try {
                obj = new JSONObject(arg);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.e("json add feed......", "called"+obj);
//            adapterActivitiesList.clear();
            adapterActivitiesList.add(0, obj);
            mAdapter.notifyDataSetChanged();
//            getAllActivities(eType, eId);
        }else if(requestCode == 100  && data!= null ){
            Log.e("onActivityResult......", "called"+data.getIntExtra("position", 0));
            int position = data.getIntExtra("position", 0);
            int count = data.getIntExtra("count", 0);
            if(count != 0) {
                JSONObject object = adapterActivitiesList.get(data.getIntExtra("position", 0));
                try {
                    srcObject = object.getJSONObject("src");
                    commentsCount = srcObject.getInt("comments");
                    srcObject.put("comments", commentsCount + count);
                    object.put("src", srcObject);
                    adapterActivitiesList.remove(position);
                    adapterActivitiesList.add(position, object);
                    mAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }


    private void populateSections() {

        OrgMemberInfo orgMemberInfo = sessionManager.getOrgMemberInfo();
        final List<OrgMemberMappingInfo.OrgSectionInfo> sectionsList = new ArrayList<OrgMemberMappingInfo.OrgSectionInfo>();
        List<String> programsList = new ArrayList<>();
        programsList.add("All");
        List<String> progCenterSections = OrgDataManager.getProgramCenterSectionString(orgMemberInfo,
                sectionsList, null);
        final Map<String, OrgMemberMappingInfo.OrgProgramCenterSectionIds> orgIds = OrgDataManager
                .getProgramCenterSectionIds(orgMemberInfo);
        programsList.addAll(progCenterSections);

            Spinner librarySectionsFilter = (Spinner) rootView
                    .findViewById(R.id.feed_list_filter);

            ProgramSectionsAdapter spinnerAdapter = new ProgramSectionsAdapter(
                    getActivity(), android.R.layout.simple_list_item_1,
                    programsList, sectionsList, orgIds);
            librarySectionsFilter.setAdapter(spinnerAdapter);
            librarySectionsFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Log.d(TAG, "Loading courses");
//                    coursesHolder.removeAllViews();
                    if (view == null) {
                        // this view was coming as null if the application context is
                        // destroyed and LibraryCourseFragment is tried to load
                        return;
                    }
//                    if(position != 0) {
                        Map<String, Object> sectionData = (Map<String, Object>) view.getTag();
                        selectedSectionId = (String) sectionData.get("sectionId");
                        selectedProgramId = (String) sectionData.get("programId");
                        selectedCenterId = (String) sectionData.get("centerId");
                        Log.e("selectedSectionId", "" + selectedSectionId+"..programId.."+selectedProgramId+"..centerId.."+selectedCenterId);
                    adapterActivitiesList.clear();
                    if(selectedProgramId == null){
                        eType = "ORGANIZATION";
                        eId = sessionManager.getOrgMemberInfo().orgId;
                        getAllActivities(eType, eId);
                    }else{
                        eType = "CENTER";
                        eId = selectedProgramId+"#"+selectedCenterId;
                        getAllActivities(eType, eId);
                    }
//
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });


    }

    final public class ProgramSectionsAdapter extends ArrayAdapter<String> {

        private List<String>         items;
        private int                  layoutResource;
        private LayoutInflater       inflater;
        private List<OrgMemberMappingInfo.OrgSectionInfo> sectionsList = new ArrayList<OrgMemberMappingInfo.OrgSectionInfo>();
        private Map<String, OrgMemberMappingInfo.OrgProgramCenterSectionIds> programList;

        public ProgramSectionsAdapter(Context context, int layoutResource, List<String> items,
                                      List<OrgMemberMappingInfo.OrgSectionInfo> sectionsList, Map<String, OrgMemberMappingInfo.OrgProgramCenterSectionIds> programCentersList) {

            super(context, layoutResource, items);
            this.items = items;
            this.layoutResource = layoutResource;
            this.sectionsList.add(new OrgMemberMappingInfo.OrgSectionInfo());
            this.sectionsList.addAll(sectionsList);
            this.programList = programCentersList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view = convertView;
            if (view == null) {
                inflater = (LayoutInflater) getContext().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(layoutResource, parent, false);
            }
            TextView sectionView = (TextView) view;
            String progCenterSecName = items.get(position);
//            Log.e("progCenterSecName", progCenterSecName);
            sectionView.setText(progCenterSecName);
            OrgMemberMappingInfo.OrgSectionInfo section = sectionsList.get(position);
//            Log.e("section ", section.toString());
            Map<String, Object> sectionDataMap = new HashMap<String, Object>();
            sectionDataMap.put("courses", section.courses);

                sectionDataMap.put("sectionId", section.id);
            if(programList.get(section.id) != null) {
                sectionDataMap.put("programId", programList.get(section.id).programId);
                sectionDataMap.put("centerId", programList.get(section.id).centerId);
            }
                sectionDataMap.put("progCenterSecName", progCenterSecName);
                view.setTag(sectionDataMap);
            return view;
        }
    }

    public void getAllActivities(String eType, String eId){
        adapterActivitiesList.clear();
        recentListRetreiverTask = new RecentListRetrieverTask(SessionManager.getInstance(getActivity()), null, this, 0, "getActivityFeeds", null, pDialog, eType, eId);
        recentListRetreiverTask.executeTask(false);
    }

    class LibrarySyncer extends AbstractVedantuAsynTask<Void, Void, Void> implements
            ITaskProcessor<Integer> {

        OrgMemberInfo orgMemberInfo;


        private LibrarySyncer(OrgMemberInfo orgMemberInfo) {

            super();
            this.orgMemberInfo = orgMemberInfo;
        }

        @Override
        protected Void doInBackground(Void... params) {

            if (orgMemberInfo == null) {
                return null;
            }
            LibraryManager.getInstance(m_cObjNhanceBaseActivity).fetchLibraryContentsFromCMDS(null,
                    orgMemberInfo, this, false);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            super.onPostExecute(result);
        }

        @Override
        protected void onProgressUpdate(Void... values) {

            super.onProgressUpdate(values);
            try {
                if ( orgMemberInfo != null) {
                    populateSections();
//                    coursesHolder.removeAllViews();
//                    populateCourses();
                    loadAnalytics();
                }
            } catch (Throwable e) {
                Log.e(TAG, e.getMessage(), e);
            }

        }

        @Override
        public void onTaskStart(Integer result) {

        }

        @Override
        public void onTaskPostExecute(boolean success, Integer result) {

            publishProgress();
        }

    }

    private void loadAnalytics() {

        Log.i(TAG, "================ loading analytics ==============");
        analyticsSyncer = new AnalyticsSyncer(sessionManager, null, EntityType.TEST.name(),
                new ITaskProcessor<JSONObject>() {

                    @Override
                    public void onTaskStart(JSONObject result) {

                    }

                    @Override
                    public void onTaskPostExecute(boolean success, JSONObject result) {

                        hideLoadingViews();
                    }
                });
        analyticsSyncer.executeTask(false);
    }

    private void hideLoadingViews() {

        if (appLandingPageActivityInstance != null) {
            appLandingPageActivityInstance.getNavigationDrawerLayout()
                    .findViewById(R.id.navigation_sync_button).setVisibility(View.VISIBLE);
            appLandingPageActivityInstance.getNavigationDrawerLayout()
                    .findViewById(R.id.navigation_sync_in_progress).setVisibility(View.GONE);
            if (progressMenuItem != null) {
                progressMenuItem.setVisible(false);
            }
        }
    }





}
