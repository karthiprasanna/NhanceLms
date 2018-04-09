package com.nhance.android.fragments.library;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.nhance.android.activities.AppLandingPageActivity;
import com.nhance.android.activities.MyProgramsActivity;
import com.nhance.android.activities.about.AboutActivity;
import com.nhance.android.async.tasks.AbstractVedantuAsynTask;
import com.nhance.android.async.tasks.AnalyticsSyncer;
import com.nhance.android.async.tasks.ITaskProcessor;
import com.nhance.android.async.tasks.socials.WebCommunicatorTask;
import com.nhance.android.async.tasks.socials.WebCommunicatorTask.ReqAction;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.BoardDataManager;
import com.nhance.android.db.datamanagers.ContentDataManager;
import com.nhance.android.db.datamanagers.OrgDataManager;
import com.nhance.android.db.datamanagers.UserDataManager;
import com.nhance.android.db.models.UserOrgProfile;
import com.nhance.android.db.models.entity.BoardModel;
import com.nhance.android.enums.EntityType;
import com.nhance.android.enums.NavigationItem;
import com.nhance.android.fragments.AbstractVedantuFragment;
import com.nhance.android.managers.LibraryManager;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.pojos.OrgMemberInfo;
import com.nhance.android.pojos.OrgMemberMappingInfo.OrgProgramCenterSectionIds;
import com.nhance.android.pojos.OrgMemberMappingInfo.OrgSectionInfo;
import com.nhance.android.pojos.OrgMemberMappingInfo.OrgStructureBasicInfo;
import com.nhance.android.utils.GoogleAnalyticsUtils;
import com.nhance.android.utils.JSONUtils;
import com.nhance.android.utils.VedantuWebUtils;
import com.nhance.android.utils.ViewUtils;
import com.nhance.android.utils.FontUtils.FontTypes;
import com.nhance.android.utils.bookmate.BookmateUtil;
import com.nhance.android.R;

public class LibraryCoursesFragment extends AbstractVedantuFragment {

    private final String                TAG                      = "LibraryCoursesActivity";
    private String                      selectedSectionId;
    private String                      selectedProgCenterSecName;

    private List<OrgStructureBasicInfo> sectionCourses           = new ArrayList<OrgStructureBasicInfo>();
    private SessionManager              sessionManager;
    private View                        fragmentRootView;
    private LinearLayout                coursesHolder;
    private String                      userId;
    private int                         orgKeyId;
    private ContentDataManager          contentDataManager;
    private BoardDataManager            boardManager;
    private OnClickListener             courseClickListner;
    private AppLandingPageActivity      appLandingPageActivityInstance;
    private boolean                     loadingNewCoursesContent = false;

    AnalyticsSyncer analyticsSyncer;
    private boolean startSync = false;
    private View navigationDrawerSyncButton;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        GoogleAnalyticsUtils.setScreenName(ConstantGlobal.GA_SCREEN_LIBRARY_COURSES);
        Activity appLandingPageActivity = getActivity();
        if (appLandingPageActivity instanceof AppLandingPageActivity) {
            appLandingPageActivityInstance = (AppLandingPageActivity) getActivity();
        }
        sessionManager = SessionManager.getInstance(appLandingPageActivityInstance.getApplicationContext());
        userId = sessionManager.getSessionStringValue(ConstantGlobal.USER_ID);
        orgKeyId = sessionManager.getSessionIntValue(ConstantGlobal.ORG_KEY_ID);
        contentDataManager = new ContentDataManager(appLandingPageActivityInstance);
        boardManager = new BoardDataManager(appLandingPageActivityInstance);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentRootView = inflater.inflate(R.layout.fragment_library_courses, container, false);
        View qrCodeScannerView = fragmentRootView.findViewById(R.id.vcode_scanner);
        if (Boolean.valueOf(SessionManager.getConfigProperty("qrcode.scanner.enabled"))) {

            qrCodeScannerView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    BookmateUtil.scanQRCode(m_cObjNhanceBaseActivity);
                }
            });
        } else {
            ViewGroup containerView = (ViewGroup) fragmentRootView
                    .findViewById(R.id.library_course_container);
            containerView.setPadding(0, 0, 0, 0);
            qrCodeScannerView.setVisibility(View.GONE);
        }
        return fragmentRootView;
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
    public void onActivityCreated(Bundle savedInstanceState) {

        fragmentRootView.findViewById(R.id.take_to_all_programs_from_courses_page)
                .setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {

                        Intent intent = new Intent(m_cObjNhanceBaseActivity, MyProgramsActivity.class);
                        startActivity(intent);
                    }
                });

        courseClickListner = new OnClickListener() {

            @Override
            public void onClick(View v) {

                BoardModel courseInfo = (BoardModel) (v.getTag());
                String courseName = ((TextView) v.findViewById(R.id.library_courses_item_name))
                        .getText().toString();
                LibraryContentFragment contentFragment = new LibraryContentFragment();
                Bundle args = new Bundle();
                args.putString("courseName", courseName);
                args.putString("progCenterSecName", selectedProgCenterSecName);
                args.putString("sectionId", selectedSectionId);
                if (courseInfo != null) {
                    args.putInt("course_id", courseInfo._id);
                    args.putString("course_entity_id", courseInfo.id);
                    contentFragment.setArguments(args);
                    Activity appLandingPageActivity = getActivity();
                    if (appLandingPageActivity instanceof AppLandingPageActivity) {
                        ((AppLandingPageActivity) getActivity()).replaceFragment(
                                contentFragment, NavigationItem.LIBRARY_CONTENT);
                    }
                } else {
                    Log.d(TAG, "CourseInfo not found for" + courseName);
                    // args.putInt("course_id", -1);
                    Toast.makeText(m_cObjNhanceBaseActivity, R.string.no_content_available_for_course,
                            Toast.LENGTH_SHORT).show();
                }
            }
        };
        setUpLibraryRefreshButton();
        super.onActivityCreated(savedInstanceState);

        startSync = m_cObjNhanceBaseActivity.getIntent().getBooleanExtra(ConstantGlobal.SYNCING, false);

        if (startSync && SessionManager.isOnline()) {
            // start library content sync here and remove this property so that
            // 2nd time sync will
            // not be started
            m_cObjNhanceBaseActivity.getIntent().removeExtra(ConstantGlobal.SYNCING);
            showProgressBar();
            syncClickedProcess();

        }
    }

    private void populateCourses() {
        Map<String, BoardModel> courseToBoardInfoMap = new HashMap<String, BoardModel>();
        for (OrgStructureBasicInfo course : sectionCourses) {
            BoardModel boardInfo = boardManager.getBoard(course.id, course.type);
            if (boardInfo != null) {
                courseToBoardInfoMap.put(course.id, boardInfo);
            }
        }

        if (m_cObjNhanceBaseActivity == null) {
            Log.e(TAG, "this activity is no more active");
            return;
        }
        LayoutInflater inflater = (LayoutInflater) m_cObjNhanceBaseActivity.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        for (OrgStructureBasicInfo course : sectionCourses) {
            View rootView = inflater.inflate(R.layout.list_item_view_library_course, coursesHolder,
                    false);
            TextView courseName = (TextView) rootView.findViewById(R.id.library_courses_item_name);
            courseName.setText(course.name);
            BoardModel boardInfo = courseToBoardInfoMap.get(course.id);

            int totalBoardContentCount = 0;
            if (boardInfo != null) {
                Map<String, Integer> contentCountMap = contentDataManager.getBoardContentCountMap(
                        userId, orgKeyId, boardInfo._id, selectedSectionId, "SECTION");
                if (contentCountMap != null) {
                    String[] entities = { "ASSIGNMENT", "VIDEO", "TEST", "DOCUMENT", "FILE",
                            "MODULE","HTMLCONTENT", "QUESTION" };
                    for (String entityName : entities) {
                        EntityType entityType = EntityType.valueOf(entityName);
                        Integer count = contentCountMap.get(entityType.name());
                        int returnCount = count == null ? 0 : count.intValue();
                        totalBoardContentCount += returnCount;
                    }
                }
            }

            ViewUtils.setTextViewValue(rootView, R.id.library_courses_content_count,
                    String.valueOf(totalBoardContentCount), null, FontTypes.ROBOTO_LIGHT);
            rootView.setTag(boardInfo);
            rootView.setOnClickListener(courseClickListner);
            coursesHolder.addView(rootView);
        }
    }

    private void setUpLibraryRefreshButton() {

        if (appLandingPageActivityInstance != null) {
            navigationDrawerSyncButton = appLandingPageActivityInstance.getNavigationDrawerLayout()
                    .findViewById(R.id.navigation_sync_button);
             navigationDrawerSyncButton.setVisibility(View.VISIBLE);

            OnClickListener syncListner = new OnClickListener() {

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
                null, ReqAction.GET_ORG_MEMBER_PROFILE, new ITaskProcessor<JSONObject>() {

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
                                        sessionManager.getSessionStringValue(ConstantGlobal.USER_ID),
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
    public void onDestroy() {
        super.onDestroy();
        hideLoadingViews();
    }

    private void hideLoadingViews() {
        if (appLandingPageActivityInstance != null) {
            appLandingPageActivityInstance.getNavigationDrawerLayout()
                    .findViewById(R.id.navigation_sync_button).setVisibility(View.VISIBLE);
            appLandingPageActivityInstance.getNavigationDrawerLayout()
                    .findViewById(R.id.navigation_sync_in_progress).setVisibility(View.GONE);
            if (progressMenuItem != null) {
                Log.d(TAG, "showing refresh");
                showRefreshButton();
            }
        }
    }

    final public class LibrarySectionsAdapter extends ArrayAdapter<String> {

        private List<String>         items;
        private int                  layoutResource;
        private LayoutInflater       inflater;
        private List<OrgSectionInfo> sectionsList;

        public LibrarySectionsAdapter(Context context, int layoutResource, List<String> items,
                List<OrgSectionInfo> sectionsList) {

            super(context, layoutResource, items);
            this.items = items;
            this.layoutResource = layoutResource;
            this.sectionsList = sectionsList;
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
            sectionView.setText(progCenterSecName);
            OrgSectionInfo section = sectionsList.get(position);
            Map<String, Object> sectionDataMap = new HashMap<String, Object>();
            sectionDataMap.put("courses", section.courses);
            sectionDataMap.put("sectionId", section.id);
            sectionDataMap.put("progCenterSecName", progCenterSecName);
            view.setTag(sectionDataMap);
            return view;
        }
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
                if (coursesHolder != null && orgMemberInfo != null) {
                    populateSections();
                    coursesHolder.removeAllViews();
                    populateCourses();
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

    @Override
    public void onResume() {

        populateSections();
        GoogleAnalyticsUtils.sendPageViewDataToGA();
        super.onResume();
    }

    private void populateSections() {

        OrgMemberInfo orgMemberInfo = sessionManager.getOrgMemberInfo();

        final List<OrgSectionInfo> sectionsList = new ArrayList<OrgSectionInfo>();
        List<String> progCenterSections = OrgDataManager.getCenterSectionString(orgMemberInfo,
                sectionsList);
        final Map<String, OrgProgramCenterSectionIds> orgIds = OrgDataManager
                .getProgramCenterSectionIds(orgMemberInfo);
        if (!sectionsList.isEmpty()) {
            coursesHolder = (LinearLayout) fragmentRootView
                    .findViewById(R.id.library_courses_holder);

            Spinner librarySectionsFilter = (Spinner) fragmentRootView
                    .findViewById(R.id.library_sections_filter);
            LibrarySectionsAdapter spinnerAdapter = new LibrarySectionsAdapter(
                    getActivity(), android.R.layout.simple_list_item_1,
                    progCenterSections, sectionsList);
            librarySectionsFilter.setAdapter(spinnerAdapter);
//            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item );

            librarySectionsFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                              Log.d(TAG, "Loading courses");
                            coursesHolder.removeAllViews();
                             if (view == null) {
                                // Added by Shankar:
                                // this view was coming as null if the application context is
                                // destroyed and LibraryCourseFragment is tried to load
                                return;
                            }
                            Map<String, Object> sectionData = (Map<String, Object>) view.getTag();
                            selectedSectionId = (String) sectionData.get("sectionId");
                            selectedProgCenterSecName = (String) sectionData
                                    .get("progCenterSecName");
                            sessionManager.setCurrentSectionDetails(selectedSectionId,
                                    selectedProgCenterSecName);
                            OrgProgramCenterSectionIds currentOrgId = orgIds.get(selectedSectionId);
                            sessionManager.setCurrentProgramCenterSectionIds(currentOrgId);
                            if (appLandingPageActivityInstance != null) {
                                appLandingPageActivityInstance.setUserInfo();
                            }
                            sectionCourses.clear();
                            if (sectionData.get("courses") != null) {
                                sectionCourses.addAll((List<OrgStructureBasicInfo>) sectionData
                                        .get("courses"));
                            }
                            if (sectionCourses.isEmpty()) {
                                fragmentRootView.findViewById(R.id.no_courses_in_section)
                                        .setVisibility(View.VISIBLE);
                            } else {
                                populateCourses();
                                fragmentRootView.findViewById(R.id.no_courses_in_section)
                                        .setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
            int selectPosition = 0;
            String sessionSectionId = sessionManager
                    .getSessionStringValue(ConstantGlobal.CURRENT_SECTION_ID);
            for (int k = 0; k < sectionsList.size(); k++) {
                if (StringUtils.equals(sectionsList.get(k).id, sessionSectionId)) {
                    selectPosition = k;
                    break;
                }
            }
            librarySectionsFilter.setSelection(selectPosition);
            //            Himank Shah
            TextView tvCourse = (TextView) fragmentRootView.findViewById(R.id.tv_course);
            if (sectionsList.size() == 1) {
                tvCourse.setText(progCenterSections.get(0));
                tvCourse.setVisibility(View.VISIBLE);
                librarySectionsFilter.setVisibility(View.INVISIBLE);
            } else {
                tvCourse.setVisibility(View.GONE);
                librarySectionsFilter.setVisibility(View.VISIBLE);
            }
        } else {
            Log.d(TAG, "No Sections found for this user.");
            fragmentRootView.findViewById(R.id.library_courses_main_layout)
                    .setVisibility(View.GONE);
            fragmentRootView.findViewById(R.id.library_courses_no_section_layout).setVisibility(
                    View.VISIBLE);
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
}
