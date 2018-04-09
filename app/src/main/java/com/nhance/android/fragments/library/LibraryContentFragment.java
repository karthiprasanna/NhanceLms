package com.nhance.android.fragments.library;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nhance.android.QuestionCount.QuestionCountActivity;
import com.nhance.android.activities.AppLandingPageActivity;
import com.nhance.android.activities.baseclasses.NhanceBaseFragment;
import com.nhance.android.adapters.library.LibraryContentAdapter;
import com.nhance.android.async.tasks.AnalyticsSyncer;
import com.nhance.android.async.tasks.ITaskProcessor;
import com.nhance.android.async.tasks.LibraryLoader;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.AnalyticsDataManager;
import com.nhance.android.db.datamanagers.BoardDataManager;
import com.nhance.android.db.datamanagers.ContentDataManager;
import com.nhance.android.db.datamanagers.QuestionAttemptStatusDataManager;
import com.nhance.android.db.datamanagers.SDCardFileMetadataManager;
import com.nhance.android.db.models.analytics.TestAnalytics;
import com.nhance.android.db.models.entity.BoardModel;
import com.nhance.android.db.models.entity.QuestionStatus;
import com.nhance.android.enums.EntityType;
import com.nhance.android.enums.NavigationItem;
import com.nhance.android.fragments.library.LibraryInfoDialogFragment.ILibraryUpdater;
import com.nhance.android.library.utils.LibraryUtils;
import com.nhance.android.managers.LibraryManager;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.managers.VApp;
import com.nhance.android.pojos.LibraryContentRes;
import com.nhance.android.pojos.VedantuDBResult;
import com.nhance.android.readers.SDCardReader;
import com.nhance.android.utils.GoogleAnalyticsUtils;
import com.nhance.android.R;
import com.nhance.android.utils.VedantuWebUtils;
import com.nhance.android.utils.ViewUtils;

public class LibraryContentFragment extends NhanceBaseFragment implements ILibraryUpdater {

    private final String                 TAG                        = "LibraryContentFragment";

    private String                       sectionId,programId,centerId;
    public String                        progCenterSecName;
    private int                          course_id;
    private int                          topic_id                   = -1;
    private String                       courseName;
    private Boolean                      showStarredContent         = null;
    private Boolean                      showOnDeviceContent        = null;
    private ProgressDialog loading;
    SessionManager                       sessionManager;
    ContentDataManager                   contentDataManager;
    LibraryLoader                        libraryLoader              = null;
    AnalyticsDataManager                 analyticsManager;
    private View                         fragmentRootView;
    private AppLandingPageActivity       appLandingPageActivityInstance;
    private RelativeLayout               navigationDrawerLayout;
    private LayoutInflater               inflater;
    private String                       orderBy                    = "position";
    private int                          selectedSortingOptionResId = R.id.show_custom_order;
    private String                       query;
    private EntityType                   activeContentEntityType    = EntityType.ALL;
    private ListView                     libraryListView;
    private LibraryContentAdapter        libraryContentAdapter;
    List<LibraryContentRes>              contentEntities            = new ArrayList<LibraryContentRes>();
    List<LibraryContentRes>              moduleEntities             = new ArrayList<LibraryContentRes>();
    List<LibraryContentRes>              questionEntities             = new ArrayList<LibraryContentRes>();
    List<LibraryContentRes>              nonModuleContentEntities   = new ArrayList<LibraryContentRes>();
    private boolean                      loadingNewContent          = false;
    private Set<String>                  attemptedTestIds;
    private String                       course_entity_id;
    private Map<EntityType, Set<String>> activeSDCardContent        = new HashMap<EntityType, Set<String>>();
    SDCardFoundReceiver                  sdCardFoundReceiver;
    private RelativeLayout question_headView;
    private JSONObject result;
    private TextView library_question_count;
    private ViewGroup star_device;
    private JSONArray courses_array;
    private JSONObject courses_object,courses_obj;
    private String question_topicId;
    private ArrayList<String>questiontopiclist;
    private String question_topicid;
    private boolean loadedAllQuestions = false;
    private ImageView library_item_info;
    private int syncStatus;
    private QuestionAttemptStatusDataManager questionAttemptStatus;
    private List<QuestionStatus> questionAttemptStatusList;
    private boolean isDownloaded = false;
    private LinearLayout progressBarLayout;
    private ProgressBar progressBar;
    private TextView statusTypeTextView;
    private TextView downloadButton;
    private TextView openButton;
    private LinearLayout library_question_content_layout;
    private Dialog dialog;
    private View headerview;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        sdCardFoundReceiver = new SDCardFoundReceiver();
        GoogleAnalyticsUtils.setScreenName(ConstantGlobal.GA_SCREEN_LIBRARY_CONTNET);
        sessionManager = SessionManager.getInstance(m_cObjNhanceBaseActivity);
        contentDataManager = new ContentDataManager(m_cObjNhanceBaseActivity);
        questionAttemptStatus = new QuestionAttemptStatusDataManager(getActivity());
        if (getArguments() != null) {
            Bundle args = getArguments();
            courseName = args.getString("courseName");
            course_id = args.getInt("course_id", -1);
            course_entity_id = args.getString("course_entity_id");
            sectionId = args.getString("sectionId");
            programId = args.getString(ConstantGlobal.PROGRAM_ID);
            centerId = args.getString(ConstantGlobal.CENTER_ID);
            Log.e("cname","..."+courseName+"..."+course_id+"...."+course_entity_id);


            progCenterSecName = args.getString("progCenterSecName");
            loadActiveSDCardContent();
        } else {
            Toast.makeText(m_cObjNhanceBaseActivity, R.string.error_general, Toast.LENGTH_SHORT)
                    .show();
            m_cObjNhanceBaseActivity.finish();
        }
        GoogleAnalyticsUtils.setCustomDimensions(course_entity_id, courseName,
                ConstantGlobal.COURSE_KEY);
        Activity appLandingPageActivity = getActivity();
        if (appLandingPageActivity instanceof AppLandingPageActivity) {
            appLandingPageActivityInstance = (AppLandingPageActivity) getActivity();
        }
        questionAttemptStatusList = new ArrayList<QuestionStatus>();
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        fragmentRootView = inflater.inflate(R.layout.fragment_library_content, container, false);

         headerview = inflater.inflate(R.layout.question_content_header_layout, null, false);

        question_headView = (RelativeLayout) headerview.findViewById(R.id.question_layout);

        library_question_content_layout = (LinearLayout) headerview.findViewById(R.id.library_content_layout);

         library_question_count=(TextView)headerview.findViewById(R.id.library_question_head);

        library_item_info = (ImageView) headerview.findViewById(R.id.library_item_info);

        libraryListView = (ListView) fragmentRootView.findViewById(R.id.library_content_list_view);

        questiontopiclist=new ArrayList<>();




        libraryListView.addHeaderView(headerview);


        library_item_info.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if(sessionManager.isOnline() ){

                    dialog = new Dialog(getActivity());
                    dialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
                    dialog.setContentView(R.layout.question_info_layout);
                    dialog.getWindow().setLayout(
                            ViewUtils.getOrientationSpecificWidth(getActivity()),
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.setTitle("Question Info");
                    dialog.setCancelable(false);

                 downloadButton = (TextView) dialog.findViewById(R.id.library_info_popup_download_btn);
                    openButton = (TextView)dialog.findViewById(R.id.open_library_popup_item);
                    progressBarLayout = (LinearLayout) dialog.findViewById(R.id.library_info_popup_download_progress);
                    progressBar = (ProgressBar) dialog.findViewById(R.id.progress_bar);
                    ImageView imageView = (ImageView)dialog.findViewById(R.id.close_library_info_popup);
                    statusTypeTextView = (TextView)dialog.findViewById(R.id.status_type);

                    if(isDownloaded){
                        statusTypeTextView.setVisibility(View.VISIBLE);
                        downloadButton.setVisibility(View.GONE);
                    }else{
                        statusTypeTextView.setVisibility(View.GONE);
                        downloadButton.setVisibility(View.VISIBLE);
                    }

                    openButton.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(isDownloaded){
                                Intent intent = new Intent(getActivity(), QuestionCountActivity.class);
                                intent.putExtra(ConstantGlobal.SECTION_ID, sectionId);
                                intent.putExtra(ConstantGlobal.PROGRAM_ID, programId);
                                intent.putExtra(ConstantGlobal.CENTER_ID, centerId);
                                intent.putExtra(ConstantGlobal.COURSE_ID, course_entity_id);
                                intent.putExtra("courseName", courseName);
                                startActivity(intent);
                            }else{
                                Toast.makeText(getActivity(), "Content is not downloaded.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


                    imageView.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    downloadButton.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // start progress
                            progressBarLayout.setVisibility(View.VISIBLE);

                            progressBar.setProgress(0);

                            isDownloaded = true;
                            final int totalProgressTime = 100;
                            final Thread t = new Thread() {
                                @Override
                                public void run() {
                                    int jumpTime = 0;

                                    while(jumpTime < totalProgressTime) {
                                        try {
                                            sleep(200);
                                            jumpTime += 5;
                                            progressBar.setProgress(jumpTime);
                                        } catch (InterruptedException e) {
                                            // TODO Auto-generated catch block
                                            e.printStackTrace();
                                        }
                                    }

                                }
                            };
                            t.start();

                            new GetQuestionStatus(sectionId, centerId, programId).execute();
                        }
                    });

                    dialog.show();


                }else {
                    Toast.makeText(getActivity(),R.string.no_internet_msg,Toast.LENGTH_LONG).show();


                }
            }
        }
        );



//new getQuestions().execute();


        this.inflater = inflater;
        return fragmentRootView;
    }




/*
    class  getQuestions extends AsyncTask<JSONObject, JSONObject, JSONObject> {


        private String url;



        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading=new ProgressDialog(getActivity());
            loading.setMessage("Please wait...");
            loading.show();

        }

        @Override
        protected JSONObject doInBackground(JSONObject... params) {

            JSONObject jsonRes = null;
            this.url = sessionManager.getApiUrl("getQuestions");

            Map<String, Object> httpParams = new HashMap<String, Object>();
            httpParams.put("entityType","QUESTION");
            httpParams.put("resultType","ALL");
            httpParams.put("callingApp","TabApp");
            httpParams.put("callingAppId","TabApp");
            httpParams.put("orderBy","timeCreated");
            httpParams.put("shareBy","ALL");
            httpParams.put("sectionId",sectionId);
            httpParams.put("centerId",centerId);
            httpParams.put("programId",programId);
            httpParams.put("brdIds[0]",course_entity_id);

            sessionManager.addContentSrcParams(httpParams);
            sessionManager.addSessionParams(httpParams);


            try {
                jsonRes = VedantuWebUtils.getJSONData(url, VedantuWebUtils.GET_REQ, httpParams);
                //Log.e("getQuestions","...."+jsonRes);
            } catch (IOException e) {
                e.printStackTrace();
            }


            return jsonRes;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            loading.dismiss();

            if (jsonObject!=null){

                try {
                    result=jsonObject.getJSONObject("result");
                    String totalHits=result.getString("totalHits");
                    JSONObject fact = result.getJSONObject("facet");
                    JSONArray topics=fact.getJSONArray("courses");
                  //  Log.e("courses courses","...."+topics.toString());

                    for(int i=0;i<topics.length();i++){


                       JSONObject topicsobject=topics.getJSONObject(i);

                       JSONObject obj=topicsobject.getJSONObject("obj");

                        question_topicid=obj.getString("name");
                       questiontopiclist.add(question_topicid);

                       // Log.e("questioncourseslist","...."+questiontopiclist.toString());
                   }

                    //    library_question_count.setText("Question Count:  "+totalHits);

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }

        }


    }
*/


    private boolean isSpinnerPopulated = false;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        if (appLandingPageActivityInstance != null) {
            navigationDrawerLayout = appLandingPageActivityInstance.getNavigationDrawerLayout();
        }

        setUpNavigationDrawerContent();
        setUpTopicsSpinner();
        setUpLibraryRefreshButton();
        loadContent();
        super.onActivityCreated(savedInstanceState);
    }

    private int              defaultFetchSize = 15;
    private boolean          loadedAllModules = false;
    private boolean          loadedAllContent = false;
    private OnScrollListener onScrollListener = new OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {

        }

        @Override
        public void onScroll(AbsListView view,
                  int firstVisibleItem,
                  int visibleItemCount, int totalItemCount) {

              // TODO: remove below hard check for
              // compoundmedia

            if (activeContentEntityType != null
                  && activeContentEntityType
                          .equals(EntityType.COMPOUNDMEDIA)) {
                return;
            }


            int lastItem = firstVisibleItem
                  + visibleItemCount;
            if (lastItem == totalItemCount
                      && totalItemCount != 0) {
                Log.d(TAG,
                      "Now is the time to scroll, total Items found "
                              + totalItemCount);
                loadMoreContent();
            }
        }
    };

    private void loadContent() {

        loadedAllModules = false;
        loadedAllContent = false;
        loadedAllQuestions = false;
        contentEntities.clear();
        nonModuleContentEntities.clear();
        moduleEntities.clear();
        questionEntities.clear();



        checkAndLoadLibraryQuestionContentEntities(0);


        if (activeContentEntityType.equals(EntityType.ALL)) {
            checkAndLoadLibraryContentEntities(0);
        } else if (activeContentEntityType.equals(EntityType.COMPOUNDMEDIA)) {
            // TODO: remove this hard coded content --
            nonModuleContentEntities.addAll(getDemoContents().entities);
        } else if(!activeContentEntityType.equals(EntityType.QUESTION)){
            VedantuDBResult<LibraryContentRes> libraryContents = getNonModulesContentsResult(0,
                    defaultFetchSize);

            nonModuleContentEntities.addAll(libraryContents.entities);
            Log.e("nonModuleContentEntities","..."+nonModuleContentEntities.size());
            if (nonModuleContentEntities.size() == libraryContents.totalHits) {
                // ==> all contents are loaded


                Log.e("thits","..."+libraryContents.totalHits);
                loadedAllContent = true;
            }
            // requesting for specific content==> no need of separate sections
            loadedAllModules = true;

        }


        // creating final list for drawing
        Log.d(TAG, "adding contents" + contentEntities.size());


        contentEntities.addAll(moduleEntities);
        contentEntities.addAll(nonModuleContentEntities);
        Log.d(TAG, "added contents " + contentEntities.size());
        attemptedTestIds = getAttemptedTestIds(contentEntities);
        if (libraryContentAdapter != null) {
            libraryContentAdapter.setAttemptedTestIds(attemptedTestIds);
            libraryContentAdapter.setActiveEntityType(activeContentEntityType);
            setFirstModuleAndNonModuleIds();
            libraryContentAdapter.notifyDataSetChanged();
        } else {
            libraryContentAdapter = new LibraryContentAdapter(m_cObjNhanceBaseActivity,
                    R.layout.list_item_view_library_content, contentEntities,
                    contentInfoClickListner, activeSDCardContent);
            libraryContentAdapter.setAttemptedTestIds(attemptedTestIds);
            libraryContentAdapter.setActiveEntityType(activeContentEntityType);
            setFirstModuleAndNonModuleIds();
            libraryListView.setAdapter(libraryContentAdapter);
        }


        if (contentEntities.isEmpty() && questionEntities.isEmpty()) {
//            if (activeContentEntityType.equals(EntityType.QUESTION)){
//
//                fragmentRootView.findViewById(R.id.library_no_content_layout).setVisibility(
//                        View.GONE);
//                fragmentRootView.findViewById(R.id.library_content_list_view).setVisibility(View.GONE);
//
//            }else{

                fragmentRootView.findViewById(R.id.library_no_content_layout).setVisibility(
                        View.VISIBLE);

                fragmentRootView.findViewById(R.id.library_content_list_view).setVisibility(View.GONE);
//                fragmentRootView.findViewById(R.id.library_content_layout).setVisibility(View.GONE);
            //}

        } else {
            fragmentRootView.findViewById(R.id.library_no_content_layout).setVisibility(View.GONE);
            fragmentRootView.findViewById(R.id.library_content_list_view).setVisibility(
                    View.VISIBLE);
//            libraryListView.addHeaderView(headerview);
//            fragmentRootView.findViewById(R.id.library_content_layout).setVisibility(View.VISIBLE);
            libraryListView.setOnScrollListener(onScrollListener);
        }
        if (appLandingPageActivityInstance != null) {
            appLandingPageActivityInstance.getDrawerLayout().closeDrawer(
                    appLandingPageActivityInstance.getNavigationDrawerLayout());
        }






        if(activeContentEntityType.equals(EntityType.ALL)){

            library_question_content_layout.setVisibility(View.VISIBLE);
            if(libraryListView.getHeaderViewsCount() == 0){
                libraryListView.addHeaderView(headerview);
            }

        }else if(activeContentEntityType.equals(EntityType.QUESTION)) {

            Log.e("activeContentEntityType", activeContentEntityType.toString());
            library_question_content_layout.setVisibility(View.VISIBLE);
            if(libraryListView.getHeaderViewsCount() == 0){
                libraryListView.addHeaderView(headerview);
            }
//            libraryListView.setVisibility(View.GONE);
        }else{

//            library_question_content_layout.setVisibility(View.GONE);
            libraryListView.removeHeaderView(headerview);

        }


    }

    private void loadMoreContent() {

        if(!activeContentEntityType.equals(EntityType.QUESTION)) {


            if (!loadedAllModules) {
                checkAndLoadLibraryContentEntities(moduleEntities.size());
            } else if (!loadedAllContent) {
                VedantuDBResult<LibraryContentRes> libraryContents = getNonModulesContentsResult(
                        nonModuleContentEntities.size(), defaultFetchSize);
                nonModuleContentEntities.addAll(libraryContents.entities);
                if (nonModuleContentEntities.size() == libraryContents.totalHits) {
                    // ==> all contents are loaded
                    loadedAllContent = true;
                    // TODO: remove this hard coded content --
                    nonModuleContentEntities.addAll(getDemoContents().entities);
                }
            }


            if (loadedAllContent && loadedAllModules) {
                Log.d(TAG,
                        "Found that all modules and contents are loaded, so setting scroll listner to null");
                libraryListView.setOnScrollListener(null);
            }

            contentEntities.clear();
            contentEntities.addAll(moduleEntities);
            contentEntities.addAll(nonModuleContentEntities);

            Log.e("contentEnti", "...." + contentEntities.size());
            Log.e("contentEntities", "...." + contentEntities.toString());


            if (libraryContentAdapter != null) {
                attemptedTestIds = getAttemptedTestIds(contentEntities);
                libraryContentAdapter.setAttemptedTestIds(attemptedTestIds);
                libraryContentAdapter.setActiveEntityType(activeContentEntityType);
                setFirstModuleAndNonModuleIds();
                libraryContentAdapter.notifyDataSetChanged();
            }
        }
    }

    private void setFirstModuleAndNonModuleIds() {

        if (libraryContentAdapter != null) {
            String firstModuleLinkId = null;
            String firstNonModuleLinkId = null;
            for (LibraryContentRes contentRes : contentEntities) {
                EntityType type = contentRes.__getEntityType();
                if (StringUtils.isEmpty(firstModuleLinkId) && type.equals(EntityType.MODULE)) {
                    firstModuleLinkId = contentRes.linkId;
                } else if (StringUtils.isEmpty(firstNonModuleLinkId)
                        && !type.equals(EntityType.MODULE)) {
                    firstNonModuleLinkId = contentRes.linkId;
                }
            }
            libraryContentAdapter.setFirstModuleAndNonModuleIds(firstModuleLinkId,
                    firstNonModuleLinkId);
        }
    }

    private void checkAndLoadLibraryContentEntities(int startForModules) {




        Log.d(TAG, "The current count of module is " + moduleEntities.size()
                + " Going for loading more at index " + startForModules);


        VedantuDBResult<LibraryContentRes> modulesResult = getModulesResult(startForModules,
                defaultFetchSize);
        moduleEntities.addAll(modulesResult.entities);
        if (moduleEntities.size() == modulesResult.totalHits) {
            // ==> all modules are loaded
            Log.d(TAG, "Loaded all modules");
            loadedAllModules = true;
            Log.d(TAG,
                    "The current count of non modulecontents is " + nonModuleContentEntities.size());

            VedantuDBResult<LibraryContentRes> libraryContents = getNonModulesContentsResult(0, defaultFetchSize);
            nonModuleContentEntities.addAll(libraryContents.entities);


            if (nonModuleContentEntities.size() == libraryContents.totalHits) {
                Log.d(TAG, "Loaded all contents");
                // ==> all contents are loaded
                loadedAllContent = true;
                // TODO: remove this hard coded content --
                nonModuleContentEntities.addAll(getDemoContents().entities);
            }

        }
    }



    private void checkAndLoadLibraryQuestionContentEntities(int startForModules) {

        Log.d(TAG, "The current count of module is " + moduleEntities.size()
                + " Going for loading more at index " + startForModules);

            VedantuDBResult<LibraryContentRes> questionResult = getQuestionsResult(startForModules,
                    100);


        Log.e("questionResult", ".." + questionResult.totalHits);
            questionEntities.addAll(questionResult.entities);

        Log.e("questionResult.."+"questionEntities", ".." + questionEntities.size());
        if (questionEntities.size() == questionResult.totalHits) {
            VApp.setQuestionsList(questionEntities);
            loadedAllQuestions = true;
            Log.e("question entitiles", ".." + questionEntities.toString());

            library_question_count.setText("Questions" + "(" + questionEntities.size() + ")");

            if(questionEntities.size() != 0){
                library_item_info.setVisibility(View.VISIBLE);
            }else{
                library_item_info.setVisibility(View.GONE);
            }

            for (int i = 0; i < VApp.getQuestionsList().size(); i++) {
                LibraryContentRes contentRes = VApp.getQuestionsList().get(i);
                QuestionStatus question = questionAttemptStatus.getQuestionStatus(contentRes.id);
                if (question != null)
                    questionAttemptStatusList.add(question);
            }

            if ( questionAttemptStatusList.size() > 0 &&  VApp.getQuestionsList().size() == questionAttemptStatusList.size()) {
                isDownloaded = true;
            }else{
                isDownloaded = false;
            }
        }

            question_headView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(questionEntities!=null && questionEntities.size()>0) {

                        Log.e("isDownloaded", ""+isDownloaded);
                        if(isDownloaded && questionAttemptStatusList.size() > 0 && questionAttemptStatusList.size() == questionEntities.size()){

                            Log.e("isDownloaded", ""+isDownloaded);
                            Intent intent = new Intent(getActivity(), QuestionCountActivity.class);
                            intent.putExtra(ConstantGlobal.SECTION_ID, sectionId);
                            intent.putExtra(ConstantGlobal.PROGRAM_ID, programId);
                            intent.putExtra(ConstantGlobal.CENTER_ID, centerId);
                            intent.putExtra(ConstantGlobal.COURSE_ID, course_entity_id);
                            intent.putExtra("courseName", courseName);
                            startActivity(intent);

                        }else if(sessionManager.isOnline() && questionAttemptStatusList.size() != questionEntities.size()){
                            Log.e("isDownloaded", ""+isDownloaded);
                            new GetQuestionStatus(sectionId, centerId, programId).execute();

                        }else {
                            Toast.makeText(getActivity(),R.string.no_internet_msg,Toast.LENGTH_LONG).show();

                        }
                    }else{
                        Toast.makeText(getActivity(),"No questions available",Toast.LENGTH_LONG).show();

                    }


                }


            });


    }

    private Set<String> getAttemptedTestIds(List<LibraryContentRes> entities) {

        Set<String> eIds = new HashSet<String>();
        if (entities.isEmpty()) {
            return eIds;
        }
        if (analyticsManager == null) {
            analyticsManager = new AnalyticsDataManager(m_cObjNhanceBaseActivity);
        }

        for (LibraryContentRes res : entities) {
            if (EntityType.TEST.name().equalsIgnoreCase(res.type)) {
                eIds.add(res.id);
            }
        }

        List<TestAnalytics> attemptedTests = analyticsManager.getAllTestAnalytics(
                sessionManager.getSessionIntValue(ConstantGlobal.ORG_KEY_ID),
                sessionManager.getSessionStringValue(ConstantGlobal.USER_ID), eIds,
                EntityType.TEST.name(), new String[] { ConstantGlobal._ID,
                        ConstantGlobal.ENTITY_ID, ConstantGlobal.ENTITY_TYPE }, false);
        eIds.clear();
        for (TestAnalytics ta : attemptedTests) {
            eIds.add(ta.entityId);
        }
        return eIds;
    }

    private int     topicsSpinnerSelectedPos = 0;
    private boolean hasTopics;

    private void setUpTopicsSpinner() {

        BoardDataManager boardDataManager = new BoardDataManager(m_cObjNhanceBaseActivity);
        VedantuDBResult<BoardModel> topicsResult = boardDataManager.getChildBoards(course_id, null,
                null, 0, 100);
        final List<BoardModel> courseTopicItems = topicsResult.entities;
        ActionBar actionBar = m_cObjNhanceBaseActivity.getSupportActionBar();
        m_cObjNhanceBaseActivity.getSupportActionBar().setTitle(courseName);
        if (!courseTopicItems.isEmpty()) {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            hasTopics = true;
            List<String> dropDownValues = new ArrayList<String>();
            dropDownValues.add("All Topics");
            for (BoardModel topic : courseTopicItems) {
                dropDownValues.add(topic.name);




            }
            final ArrayAdapter<String> adapter = new TopicsSpinnerAdapter(
                    actionBar.getThemedContext(), android.R.layout.simple_list_item_1,
                    dropDownValues);




//            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            actionBar.setListNavigationCallbacks(adapter,
                    new ActionBar.OnNavigationListener() {

                @Override
                public boolean onNavigationItemSelected(int itemPosition, long itemId) {

                    if (isSpinnerPopulated) {
                        if (itemPosition == 0) {
                            topic_id = -1;


                        } else {
                            topic_id = courseTopicItems.get(itemPosition - 1)._id;


                        }
                        loadContent();
                      //  new getQuestions().execute();
                        topicsSpinnerSelectedPos = itemPosition;
                        adapter.notifyDataSetChanged();
                    }
                    isSpinnerPopulated = true;
                    return false;
                }
            });
        }
    }

    class TopicsSpinnerAdapter extends ArrayAdapter<String> {

        private List<String> objects;

        public TopicsSpinnerAdapter(Context context, int textViewResourceId, List<String> objects) {

            super(context, textViewResourceId, objects);
            this.objects = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(
                        android.R.layout.simple_spinner_item, null);
            }
            TextView textView = (TextView) convertView;
            textView.setText(objects.get(topicsSpinnerSelectedPos));
            return convertView;
        }
    }

    private void setUpNavigationDrawerContent() {

        String[] contentTypesArray = getResources().getStringArray(R.array.library_content_types);
        if (navigationDrawerLayout != null) {
            ViewGroup extraContentHolder = appLandingPageActivityInstance.setUpMenuHead(
                    R.layout.library_content_filters, NavigationItem.LIBRARY);

            // back button for courses page
            LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            TextView customeView = (TextView) inflater.inflate(
                    R.layout.library_content_back_to_courses, null);
            customeView.setText(courseName);
            customeView.setLayoutParams(lp);

            m_cObjNhanceBaseActivity.getSupportActionBar().setCustomView(customeView);

            customeView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    m_cObjNhanceBaseActivity.onBackPressed();
                    appLandingPageActivityInstance.closeDrawer();
                }
            });

            appLandingPageActivityInstance.setDrawerToggle(new ActionBarDrawerToggle(
                    m_cObjNhanceBaseActivity, /*
                                            * host Activity
                                            */
                    appLandingPageActivityInstance.getDrawerLayout(), /*
                                                                       * DrawerLayout object
                                                                       */
                    R.drawable.ic_navigation_drawer, /*
                                                      * nav drawer image to replace 'Up' caret
                                                      */
                    R.string.drawer_open, /*
                                           * "open drawer" description for accessibility
                                           */
                    R.string.drawer_close /*
                                           * "close drawer" description for accessibility
                                           */
            ) {

                @Override
                public void onDrawerClosed(View view) {

                    m_cObjNhanceBaseActivity.getSupportActionBar().setDisplayShowCustomEnabled(false);
                    m_cObjNhanceBaseActivity.supportInvalidateOptionsMenu();
                    if (hasTopics) {
                        m_cObjNhanceBaseActivity.getSupportActionBar().setNavigationMode(
                                ActionBar.NAVIGATION_MODE_LIST);
                    }
                    m_cObjNhanceBaseActivity.getSupportActionBar().setDisplayShowTitleEnabled(true);
                }

                @Override
                public void onDrawerOpened(View drawerView) {

                    m_cObjNhanceBaseActivity.getSupportActionBar().setNavigationMode(
                            ActionBar.NAVIGATION_MODE_STANDARD);
                    m_cObjNhanceBaseActivity.getSupportActionBar().setDisplayShowCustomEnabled(true);
                    m_cObjNhanceBaseActivity.getSupportActionBar().setDisplayShowTitleEnabled(false);
                    m_cObjNhanceBaseActivity.supportInvalidateOptionsMenu();
                }
            });

            final LinearLayout contentTypesHolder = (LinearLayout) extraContentHolder
                    .findViewById(R.id.library_content_types_holder);

            OnClickListener onContentTypeClickListner = new OnClickListener() {

                @Override
                public void onClick(View v) {

                    int typesCount = contentTypesHolder.getChildCount();
                    activeContentEntityType = (EntityType) v.getTag();
                    for (int k = 0; k < typesCount; k++) {
                        View rootView = contentTypesHolder.getChildAt(k);
                        TextView textView = (TextView) rootView
                                .findViewById(R.id.library_filter_text);
                        EntityType type = (EntityType) rootView.getTag();
                        if (type.equals(activeContentEntityType)) {
                            textView.setTextColor(getResources().getColor(R.color.blue));
                        } else {
                            textView.setTextColor(getResources().getColor(R.color.black));
                        }
                    }
                    loadContent();
                }
            };

            for (String type : contentTypesArray) {
                EntityType contentEntityType = EntityType.valueOfKey(type);
                View rootView = (inflater.inflate(R.layout.list_item_view_library_content_type,
                        contentTypesHolder, false));
                TextView textView = (TextView) rootView.findViewById(R.id.library_filter_text);
                textView.setText(StringUtils.upperCase(contentEntityType.getDisplayName()));

                ((ImageView) rootView.findViewById(R.id.library_filter_image))
                        .setBackgroundResource(contentEntityType.filter_icon_res_id);

                if (contentEntityType.equals(EntityType.ALL)) {
                    textView.setTextColor(getResources().getColor(R.color.blue));
                } else {
                    textView.setTextColor(getResources().getColor(R.color.black));
                }

                if (contentEntityType.equals(EntityType.COMPOUNDMEDIA)
                        && !Boolean.parseBoolean(SessionManager
                                .getConfigProperty("show.compoundmedia.demo"))) {
                    continue;
                }
                rootView.setTag(contentEntityType);
                rootView.setOnClickListener(onContentTypeClickListner);
                contentTypesHolder.addView(rootView);
            }



             star_device = (ViewGroup) extraContentHolder
                    .findViewById(R.id.star_device);


            // starred and on device

            ViewGroup starHolder = (ViewGroup) extraContentHolder
                    .findViewById(R.id.library_show_starred_content);
            final TextView starredContentView = (TextView) (starHolder.getChildAt(1));
            starHolder.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                  //  question_headView.setVisibility(View.GONE);

                    if (showStarredContent == null) {
                        starredContentView.setTextColor(getResources().getColor(R.color.blue));
                        showStarredContent = true;
                      //  question_headView.setVisibility(View.GONE);
                    } else {
                        starredContentView.setTextColor(getResources().getColor(R.color.black));
                        showStarredContent = null;
                      //  question_headView.setVisibility(View.GONE);
                    }
                    loadContent();
                }

            });

            ViewGroup onDeviceHolder = (ViewGroup) extraContentHolder
                    .findViewById(R.id.library_show_on_device_content);

            final TextView onDeviceContentView = (TextView) (onDeviceHolder.getChildAt(1));
            onDeviceHolder.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    //question_headView.setVisibility(View.GONE);
                    if (showOnDeviceContent == null) {
                       // question_headView.setVisibility(View.GONE);
                        onDeviceContentView.setTextColor(getResources().getColor(R.color.blue));
                        showOnDeviceContent = true;
                    } else {
                      //  question_headView.setVisibility(View.GONE);
                        onDeviceContentView.setTextColor(getResources().getColor(R.color.black));
                        showOnDeviceContent = null;
                    }
                    loadContent();
                }
            });

        }
    }

    private VedantuDBResult<LibraryContentRes> getNonModulesContentsResult(int start, int size) {

        String contentTypeStr = activeContentEntityType.name();
        String excludeStr = null;
        String excludeQuestionStr = null;
        if (activeContentEntityType.equals(EntityType.ALL)) {
            contentTypeStr = null;
            excludeStr = EntityType.MODULE.name();
        }
        if (activeContentEntityType.equals(EntityType.ALL)) {
            contentTypeStr = null;
            excludeQuestionStr = EntityType.QUESTION.name();
        }
        return getContentsResult(start, size, contentTypeStr, excludeStr, excludeQuestionStr);
    }

    private VedantuDBResult<LibraryContentRes> getModulesResult(int start, int size) {

        return getContentsResult(start, size, EntityType.MODULE.name(), null, null);

    }

    private VedantuDBResult<LibraryContentRes> getQuestionsResult(int start, int size) {

        return getContentsResult(start, size, EntityType.QUESTION.name(), null, null);

    }

    private VedantuDBResult<LibraryContentRes> getContentsResult(int start, int size,
            String contentTypeStr, String excludeStr, String excludeQuestionStr) {

        int brdId = course_id;
        if (topic_id != -1) {
            brdId = topic_id;
        }

        String sortOrder = "DESC";
        if (orderBy.equals("name") || orderBy.equals("position")) {
            sortOrder = "ASC";
        }
        Log.d(TAG, "Start " + start + " and size " + size + "::::::::::::::::::::::::::");
        Log.d(TAG, "Start " + start + " and size " + size + ",contentTypeStr: " + contentTypeStr
                + ", excludeStr: " + excludeStr + "::::::::::::::::::::::::::");
        VedantuDBResult<LibraryContentRes> contentsResult = contentDataManager.getLibraryContents(
                sessionManager.getSessionStringValue(ConstantGlobal.USER_ID),
                sessionManager.getSessionIntValue(ConstantGlobal.ORG_KEY_ID), sectionId, "SECTION",
                contentTypeStr, excludeStr, excludeQuestionStr, brdId, orderBy, sortOrder, start, size, query,
                showOnDeviceContent, showStarredContent);
        Log.d(TAG, "Total hits :" + contentsResult.totalHits);
        Log.e("entities  ","..."+ contentsResult.entities);
        return contentsResult;
    }

    private void setUpLibraryRefreshButton() {

        if (appLandingPageActivityInstance != null) {
            final View syncButton = appLandingPageActivityInstance.getNavigationDrawerLayout()
                    .findViewById(R.id.navigation_sync_button);
            syncButton.setVisibility(View.VISIBLE);
            final View textViewProgress = appLandingPageActivityInstance
                    .getNavigationDrawerLayout().findViewById(R.id.navigation_sync_in_progress);
            syncButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    Log.d(TAG, "fetching content");
                    boolean online = SessionManager.isOnline();
                    if (!online) {
                        Toast.makeText(m_cObjNhanceBaseActivity,
                                "Internet connection is required to update library",
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    syncButton.setVisibility(View.GONE);
                    textViewProgress.setVisibility(View.VISIBLE);
                    if (progressMenuItem != null) {
                        progressMenuItem.setVisible(true);
                    }
                    loadingNewContent = true;
                    appLandingPageActivityInstance.getDrawerLayout().closeDrawer(
                            appLandingPageActivityInstance.getNavigationDrawerLayout());
                    libraryLoader = LibraryManager.getInstance(m_cObjNhanceBaseActivity)
                                    .fetchLibraryContentFromCMDS(null,
                                    sessionManager.getSessionStringValue(ConstantGlobal.USER_ID),
                                    sectionId, "SECTION", true, new RefreshLibraryPostProcessor());
                }
            });
        }

    }

    private class RefreshLibraryPostProcessor implements ITaskProcessor<Integer> {

        @Override
        public void onTaskPostExecute(boolean success, final Integer result) {

            ITaskProcessor<JSONObject> taskProcessor = new ITaskProcessor<JSONObject>() {

                @Override
                public void onTaskStart(JSONObject result) {

                }

                @Override
                public void onTaskPostExecute(boolean success, JSONObject res) {

                    loadingNewContent = false;
                    hideLoadingViews();
                    if (result != null && isVisible() && m_cObjNhanceBaseActivity != null) {
                        String msg = result.intValue() == 0 ? "No new content available" :
//                                result.intValue() +
                                        " New content available";
                        // addLastSyncInfo();
                        Toast.makeText(m_cObjNhanceBaseActivity, msg, Toast.LENGTH_LONG).show();
                        loadContent();
                    }
                }

            };
            if (success) {
                 AnalyticsSyncer analyticsSyncer = new AnalyticsSyncer(sessionManager, null,
                        EntityType.TEST.name(), taskProcessor);
                analyticsSyncer.executeTask(false);
            } else {
                taskProcessor.onTaskPostExecute(false, null);
            }
        }

        @Override
        public void onTaskStart(Integer result) {

        }

    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        if (libraryLoader != null) {
            libraryLoader.cancel(true);
        }
        hideLoadingViews();
        Thread t = new Thread() {

            @Override
            public void run() {

                File tempDir = new File(ContentDataManager.getTempContentDir());
                synchronized (tempDir.getAbsolutePath().intern()) {
                    try {
                        for (File f : tempDir.listFiles()) {
                            Log.d(TAG, "deleting file : " + f.getAbsolutePath());
                            f.delete();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                }
            }
        };

        t.start();
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

    // private void addLastSyncInfo() {
    //
    // long lastSyncTime = sessionManager.getContentLastSyncTime(sectionId,
    // "SECTION");
    // TextView viewLastSyncInfo = (TextView) findViewById(R.id.last_sync_info);
    // if (lastSyncTime < 1) {
    // viewLastSyncInfo.setVisibility(View.GONE);
    // } else {
    // viewLastSyncInfo.setVisibility(View.VISIBLE);
    // viewLastSyncInfo.setText("Last Sync "
    // + DateUtils.getRelativeTimeSpanString(lastSyncTime,
    // System.currentTimeMillis(),
    // DateUtils.SECOND_IN_MILLIS));
    // }
    // }

    private SearchView searchView;
    private boolean    searchViewEmptyForFirstTime = true;
    private MenuItem progressMenuItem;
    private Menu       menuStore;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);
        if (appLandingPageActivityInstance != null) {
            menuStore = menu;
            boolean drawerOpen = appLandingPageActivityInstance.getDrawerLayout().isDrawerOpen(appLandingPageActivityInstance.getNavigationDrawerLayout());
            if (!drawerOpen) {
                // adding this section of code resolved the problem
                m_cObjNhanceBaseActivity.getMenuInflater().inflate(R.menu.library_content, menu);
                setCurrentSortingOption();
                // adding this section of code resolved the problem
                MenuItem lItem = menu.findItem(R.id.search_library);
                searchView = (SearchView) MenuItemCompat.getActionView(lItem);
//                searchView = (SearchView) lItem.getActionView();
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

                    @Override
                    public boolean onQueryTextSubmit(String newText) {

                        query = newText;

                 /*       if (newText.equalsIgnoreCase("Q") && newText.equalsIgnoreCase("ques")  && newText.equalsIgnoreCase("question")){

                            question_headView.setVisibility(View.VISIBLE);


                        }else {

                            question_headView.setVisibility(View.GONE);
                        }
                        Log.e("query","..."+newText);
*/


                        loadContent();
                        InputMethodManager imm = (InputMethodManager) m_cObjNhanceBaseActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                        Log.e("imm","...."+imm);

                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {

                        query = newText;
                        if (TextUtils.isEmpty(newText) && !searchViewEmptyForFirstTime) {


                            loadContent();
                        }
                        searchViewEmptyForFirstTime = false;
                        return false;
                    }
                });
                progressMenuItem = menu.findItem(R.id.action_library_content_progress);
                if (loadingNewContent) {
                    progressMenuItem.setVisible(true);
                } else {
                    progressMenuItem.setVisible(false);
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // resetting submenu options
        switch (item.getItemId()) {
        case R.id.show_alphabetical:
            resetSubMenu();
            item.setIcon(R.drawable.tick);
            orderBy = "name";
            selectedSortingOptionResId = R.id.show_alphabetical;
            loadContent();
            return true;
        case R.id.show_recently_added:
            resetSubMenu();
            item.setIcon(R.drawable.tick);
            orderBy = "timeCreated";
            selectedSortingOptionResId = R.id.show_recently_added;
            loadContent();
            return true;
        case R.id.show_recently_viewed:
            resetSubMenu();
            item.setIcon(R.drawable.tick);
            orderBy = "lastViewed";
            selectedSortingOptionResId = R.id.show_recently_viewed;
            loadContent();
            return true;
        case R.id.show_custom_order:
            resetSubMenu();
            item.setIcon(R.drawable.tick);
            orderBy = "position";
            selectedSortingOptionResId = R.id.show_custom_order;
            loadContent();
            return true;
            case R.id.logout:
                sessionManager.logoutUser();
                return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void resetSubMenu() {

        if (menuStore != null) {
            menuStore.findItem(R.id.show_alphabetical).setIcon(null);
            menuStore.findItem(R.id.show_recently_added).setIcon(null);
            menuStore.findItem(R.id.show_recently_viewed).setIcon(null);
            menuStore.findItem(R.id.show_custom_order).setIcon(null);
        }
    }

    private void setCurrentSortingOption() {

        MenuItem item = menuStore.findItem(selectedSortingOptionResId);
        item.setIcon(R.drawable.tick);
    }

    private OnClickListener contentInfoClickListner = new OnClickListener() {

        @Override
        public void onClick(View view) {
             int position = (Integer) view.getTag();
            LibraryContentRes contentRes = contentEntities.get(position);
            String contentLinkId = contentRes.linkId;
            String idOfContent = contentRes.id;
            LibraryInfoDialogFragment popupFragment = new LibraryInfoDialogFragment();
            Bundle args = new Bundle();
            args.putString(ConstantGlobal.CONTENT_ID, contentLinkId);
            args.putInt(ConstantGlobal.POSITION, position);
            args.putString(ConstantGlobal.SECTION_ID, sectionId);

            // checking if the entity is test and is
            // attempted
            if (attemptedTestIds != null && attemptedTestIds.contains(idOfContent)) {
                args.putBoolean(ConstantGlobal.ATTEMPTED, true);
            }
            popupFragment.setArguments(args);
            popupFragment.setILibraryUpdaterInstance(LibraryContentFragment.this);
            popupFragment.show(m_cObjNhanceBaseActivity.getSupportFragmentManager(), null);
    }
};

    @Override
    public void updateListViewItem(int position, boolean finalStarStatus, EntityType type) {

        LibraryContentRes contentRes = contentEntities != null ? contentEntities.get(position) : null;


        Log.e("listrview","..."+contentRes);
        View listItemView = libraryListView.getChildAt(position+1 - libraryListView.getFirstVisiblePosition());
        if (contentRes != null) {
            contentRes.starred = finalStarStatus;
            LibraryUtils.addStarViewStatus(listItemView, finalStarStatus);
        }
    }

    @Override
    public void onResume() {

        GoogleAnalyticsUtils.sendPageViewDataToGA();
        super.onResume();
        attemptedTestIds = getAttemptedTestIds(contentEntities);
        if (libraryContentAdapter != null) {
            libraryContentAdapter.setAttemptedTestIds(attemptedTestIds);
            libraryContentAdapter.notifyDataSetChanged();
        }
        m_cObjNhanceBaseActivity.registerReceiver(sdCardFoundReceiver, new IntentFilter(SDCardReader.INTENT_ACTION_SDCARD_FOUND));
    }

    @Override
    public void onPause() {

        super.onPause();
        m_cObjNhanceBaseActivity.unregisterReceiver(sdCardFoundReceiver);
    }

    private void loadActiveSDCardContent() {

        activeSDCardContent.clear();
        activeSDCardContent.putAll((new SDCardFileMetadataManager(m_cObjNhanceBaseActivity))
                .getActiveSDCardContent(
                        sessionManager.getSessionIntValue(ConstantGlobal.ORG_KEY_ID),
                        sessionManager.getSessionStringValue(ConstantGlobal.USER_ID), sectionId,
                        EntityType.SECTION.name()));
        Log.d(TAG, "activeSDCardContent: " + activeSDCardContent);
    }

    private class SDCardFoundReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d(TAG, "received broadcast : " + intent);
            if (SDCardReader.INTENT_ACTION_SDCARD_FOUND.equals(intent.getAction())) {
                loadActiveSDCardContent();
                Log.d(TAG, "activeSDCardContent: " + activeSDCardContent);
                loadContent();
            }
        }
    }

    // TODO: remove this content form here
    private VedantuDBResult<LibraryContentRes> getDemoContents() {

        VedantuDBResult<LibraryContentRes> result = new VedantuDBResult<LibraryContentRes>(6);
        if (!Boolean.parseBoolean(SessionManager.getConfigProperty("show.compoundmedia.demo"))) {
            return result;
        }

        result.addEntity(createDemoContent("Introduction to Lathe",
                "3_D_Objects/191323/191323_01.html", 1));
        result.addEntity(createDemoContent("Lathe Turning Operation",
                "Animations/47318/lathe_turning_operation_engg.swf", 2));

        result.addEntity(createDemoContent("Lathe Machine", "3_D_Objects/ME_3D_1060/main.swf", 3));
        result.addEntity(createDemoContent("Introduction to Shaper",
                "3_D_Objects/216633/216633_01.html", 4));
        result.addEntity(createDemoContent("Shaping Machine", "3_D_Objects/ME_3D_1013/main.swf", 5));
        result.addEntity(createDemoContent("Introduction to Drilling Machine",
                "3_D_Objects/191793/191793_01.html", 6));
        result.addEntity(createDemoContent("Bench Drilling Machine",
                "3_D_Objects/ME_3D_1007/main.swf", 7));
        result.addEntity(createDemoContent("Drill Bit", "3_D_Objects/ME_3D_1054/main.swf", 8));

        result.addEntity(createDemoContent("Introduction to Milling Machine",
                "3_D_Objects/191794/191794_01.html", 9));
        result.addEntity(createDemoContent("Milling Machine", "ME_3D_1062/main.swf", 10));
        result.addEntity(createDemoContent("Up Milling", "Animations/47319/Upmilling_engg.swf", 11));
        result.addEntity(createDemoContent("Down Milling", "Animations/47320/Downmilling_engg.swf",
                12));

        return result;
    }

    private LibraryContentRes createDemoContent(String name, String indexFile, int index) {

        long timeCreated = System.currentTimeMillis() - (60 * 60 * 24);
        LibraryContentRes res = new LibraryContentRes();
        res.timeCreated = String.valueOf(timeCreated);
        res.orgKeyId = sessionManager.getSessionIntValue(ConstantGlobal.ORG_KEY_ID);
        res.userId = sessionManager.getSessionStringValue(ConstantGlobal.USER_ID);
        res.lastUpdated = "0";

        res.brdIds = "";
        res.tags = null;
        res.ownerId = res.userId;
        res.ownerName = "nhance";
        res.id = "demo" + index;
        res.type = EntityType.COMPOUNDMEDIA.name();
        res.name = name;
        res.desc = "";
        res.info = new JSONObject();

        res.thumb = null;
        res.file = indexFile;
        res.linkId = "linkId";
        res.downloadable = false;
        res.linkTime = res.timeCreated;
        res.encLevel = null;
        res.passphrase = null;
        return res;
    }

    public class GetQuestionStatus extends AsyncTask<JSONObject, JSONObject, JSONObject> {

        private String url;
        private String questionId;
        private QuestionStatus questionStatus;
        private QuestionStatus questionStatus1;
        private ProgressDialog loading;
        private  String sectionId, centerId, programId;

        public GetQuestionStatus(String sectionId, String centerId, String programId){
            this.sectionId = sectionId;
            this.centerId = centerId;
            this.programId = programId;
//            isDownloaded = false;
            Log.e("getQuestionstatus", "..sectionId" + sectionId+".."+centerId+".."+programId);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading=new ProgressDialog(getActivity());
            loading.setMessage("Please wait...");
            if(!isDownloaded)
            loading.show();

        }

        @Override
        protected JSONObject doInBackground(JSONObject... params) {

            JSONObject jsonRes = null;
            this.url = sessionManager.getApiUrl("getQuestionstat");

            Map<String, Object> httpParams = new HashMap<String, Object>();
            httpParams.put("entityType", "QUESTION");
            httpParams.put("resultType", "ALL");
            httpParams.put("callingApp", "TabApp");
            httpParams.put("callingAppId", "TabApp");
            httpParams.put("orderBy", "timeCreated");
            httpParams.put("orderByOrg", "timeCreated");
            httpParams.put("sortOrder", "DESC");
            httpParams.put("shareBy", "ALL");
            httpParams.put("sectionId", sectionId);
            httpParams.put("centerId", centerId);
            httpParams.put("programId", programId);

            sessionManager.addContentSrcParams(httpParams);
            sessionManager.addSessionParams(httpParams);


            try {
                jsonRes = VedantuWebUtils.getJSONData(url, VedantuWebUtils.GET_REQ, httpParams);
                Log.e("getQuestionstatus", "...." + jsonRes);
            } catch (IOException e) {
                e.printStackTrace();
            }


            return jsonRes;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            loading.dismiss();


            if (jsonObject != null) {
                //  QuestionStatus questionStatus = new QuestionStatus();

                try {
                    JSONObject result = jsonObject.getJSONObject("result");


                    JSONArray list_array = result.getJSONArray("list");

                    for (int i = 0; i < list_array.length(); i++) {

                        JSONObject list_object = list_array.getJSONObject(i);

                        String id = list_object.getString("id");
                        int attempts = list_object.getInt("attempts");
                        boolean attempted = list_object.getBoolean("attempted");
                        JSONArray answerGiven = list_object.optJSONArray("answerGiven");
                        JSONArray correctAnswer = list_object.optJSONArray("correctAnswer");
                        String timeCreated = list_object.getString("qAttemptedTime");
                        long timeTaken = list_object.getLong("timeTaken");
                        String questionType = list_object.getString("quesType");

                        if (correctAnswer == null) {
                            correctAnswer = new JSONArray();
                        }


                        if (answerGiven == null) {
                            answerGiven = new JSONArray();
                        }



                        Log.e("status", ".." + id + "..." + attempts + "..." + attempted + "..." );


                        questionId = id;
                        if(attempted == true){
                            syncStatus = 1;
                        }else{
                            syncStatus = 0;
                        }
                        questionStatus = new QuestionStatus(id, correctAnswer, attempted, answerGiven, attempts, timeCreated, timeTaken, questionType, syncStatus);

                        Log.e("questionStatus..", questionStatus.toString());
                        questionStatus1 = questionAttemptStatus.getQuestionStatus(questionId);
                        if (questionStatus1 != null && questionStatus1.id.equalsIgnoreCase(questionId)) {
                            questionAttemptStatus.updateQuestionStatus(questionId, questionStatus);
                        } else {
                            questionAttemptStatus.insertContent(questionStatus);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(!isDownloaded) {
                    Intent intent = new Intent(getActivity(), QuestionCountActivity.class);
                    intent.putExtra(ConstantGlobal.SECTION_ID, sectionId);
                    intent.putExtra(ConstantGlobal.PROGRAM_ID, programId);
                    intent.putExtra(ConstantGlobal.CENTER_ID, centerId);
                    intent.putExtra(ConstantGlobal.COURSE_ID, course_entity_id);
                    startActivity(intent);
                    isDownloaded = true;
                }else{
                    if(dialog != null) {
                        progressBarLayout.setVisibility(View.GONE);
                        downloadButton.setVisibility(View.GONE);
                        statusTypeTextView.setVisibility(View.VISIBLE);
                    }
                }

            }else{
                Toast.makeText(getContext(), "Server error  please try again later", Toast.LENGTH_SHORT).show();
            }

        }
    }


}
