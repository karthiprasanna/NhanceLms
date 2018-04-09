package com.nhance.android.activities;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.nhance.android.ChallengeArena.ChallengesFragment;
import com.nhance.android.GlobalLeaderBoard.GlobalChallengeLeaderFragment;
import com.nhance.android.activities.baseclasses.NhanceBaseActivity;
import com.nhance.android.async.tasks.AbstractVedantuJSONAsyncTask;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.ContentDataManager;
import com.nhance.android.db.datamanagers.ContentLinkDataManager;
import com.nhance.android.db.models.entity.ContentLink;
import com.nhance.android.enums.EntityType;
import com.nhance.android.enums.MemberProfile;
import com.nhance.android.enums.NavigationItem;
import com.nhance.android.fragment.doubts.DoubtsFragment;
import com.nhance.android.fragments.ProfileFragment;
import com.nhance.android.fragments.UserGuideDialogFragment;
import com.nhance.android.fragments.analytics.OverallAnalyticsFragment;
import com.nhance.android.fragments.analytics.TeacherOverallAnalyticsFragment;
import com.nhance.android.fragments.library.LibraryContentFragment;
import com.nhance.android.fragments.library.LibraryCoursesFragment;
import com.nhance.android.interfaces.INavgationDrawer;
import com.nhance.android.library.utils.LibraryUtils;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.message.MessageFragment;
import com.nhance.android.notifications.NotificationFragment;
import com.nhance.android.pojos.LibraryContentRes;
import com.nhance.android.recentActivities.Actor;
import com.nhance.android.recentActivities.RecentActivitiesFragment;
import com.nhance.android.utils.VedantuWebUtils;
import com.nhance.android.utils.ViewUtils;
import com.nhance.android.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.security.AccessController.getContext;


public class AppLandingPageActivity extends NhanceBaseActivity implements INavgationDrawer {

    private final String            TAG                   = "AppLandingPageActivity";
    SessionManager                  sessionManager;
    private DrawerLayout            mDrawerLayout;
    private RelativeLayout          mDrawerContent;
    private ActionBarDrawerToggle   mDrawerToggle;
    private CharSequence            mTitle;
    private NavigationItem          currentNavItem;
    private static final String     CURRENT_NAV_ITEM_NAME = "currentNavItemName";
    private String[]                menuItems;
    private LinearLayout            navItemsHolder;
    private Fragment                currentFragment;
    private UserGuideDialogFragment userGuideDialogFragment;


    private static TextView menuCountTextView;

    private int notificationCount;
    private Bundle bundleInstance;
    private TextView navItemNameView;
    private View navRootView;

    public int unreadcount;
    public static final String PREFS_NAME = "MyPrefsFile";

    SharedPreferences pref;
    private boolean fromNotification = false;


    @SuppressLint("LongLogTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        sessionManager = SessionManager.getInstance(getApplicationContext());
        sessionManager.captureUserInfoInGACustomDimensions();
        bundleInstance = savedInstanceState;

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_app_landing_page);

        fromNotification = getIntent().getBooleanExtra("fromNotification", false);

        Log.e("fromNotification",""+fromNotification+"bundle..");
        Log.e("sessionManager.isLoggedIn()",""+sessionManager.isLoggedIn());


        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);

            // Closing all the Activities!!
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Staring Login Activity
            startActivity(intent);
            Log.i("...............After login activity.................", "dsds1");
            finish();
        } else {
            Log.i("...............Inside else function.................", "dsds");
            if (getIntent() == null) {
                return;
            }
            getIntent().putExtra(ConstantGlobal.SYNCING, true);
            boolean showed = sessionManager.getShowedContentLoadToastOnLogin();
            Log.d(TAG, "Showed " + showed);
            if (!showed) {
                sessionManager.setShowedContentLoadToastOnLogin();
                Toast.makeText(this, "Syncing Content Now", Toast.LENGTH_LONG).show();
            }
            boolean showUs0erGuide = getIntent().getBooleanExtra("showUserGuide", false);
    /*        if (showUserGuide) {
                userGuideDialogFragment = new UserGuideDialogFragment();
                userGuideDialogFragment.show(getSupportFragmentManager(), null);
            }*/
            new GetNotificationAsyncTask(SessionManager.getInstance(this), null, bundleInstance).executeTask(false);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IntentIntegrator.REQUEST_CODE) {

            IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode,
                    data);
            if (scanResult != null) {
                String result = scanResult.getContents();
                String invalidContentMsg = "OOPs! Invalid V(QR) Code :(";
                String[] values = TextUtils.isEmpty(result) ? null : TextUtils.split(result, "/");
                boolean error = values == null || values.length < 5; // /org/oggId/entityType/entityId
                if (error) {
                    Toast.makeText(this, invalidContentMsg, Toast.LENGTH_LONG).show();
                    return;
                }

                if (!TextUtils.equals(values[2],
                        sessionManager.getSessionStringValue(ConstantGlobal.ORG_ID))) {
                    Toast.makeText(this, "OOPs! Content does not belong to app publisher :(",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                ContentLinkDataManager linkDataManager = new ContentLinkDataManager(
                        getApplicationContext());
                ContentLink contentLink = linkDataManager.getContentLink(values[4], EntityType
                        .valueOfKey(values[3]).name(), sessionManager
                        .getSessionStringValue(ConstantGlobal.USER_ID), null, null, sessionManager
                        .getSessionIntValue(ConstantGlobal.ORG_KEY_ID));
                error = contentLink == null;
                if (error) {
                    Toast.makeText(
                            this,
                            "OOPs! Seem content is not available locally, sync your library to get latest content :(",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                LibraryContentRes libraryContentRes = new ContentDataManager(
                        getApplicationContext()).getLibraryContentRes(contentLink.linkId);
                if (libraryContentRes != null) {
                    LibraryUtils.onLibraryItemClickListnerImpl(getApplicationContext(),
                            libraryContentRes);
                } else {
                    Toast.makeText(this, invalidContentMsg, Toast.LENGTH_LONG).show();
                    return;
                }

            } else {
                Toast.makeText(this, "OOPs! It failed :(", Toast.LENGTH_LONG).show();
            }
        }else if(requestCode == 100  && data!= null ){
            Log.e("onActivityResult......", "called"+data.getIntExtra("position", 0));
            currentFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void setUpNavigationDrawer() {

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        mDrawerToggle = getDrawerListener();
        mDrawerLayout.setDrawerListener(mDrawerToggle);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_launcher);
        mDrawerContent = (RelativeLayout) findViewById(R.id.navigation_drawer_content);

        // setting the width
        mDrawerContent.getLayoutParams().width = ViewUtils.getOrientationSpecificWidth(this);
    }

    private ActionBarDrawerToggle drawerListener;

    private ActionBarDrawerToggle getDrawerListener() {

        if (drawerListener == null) {
            drawerListener = new ActionBarDrawerToggle(this, /* host Activity */
            mDrawerLayout, /* DrawerLayout object */
            R.drawable.navbar, /* nav drawer image to replace 'Up' caret */
            R.string.drawer_open, /* "open drawer" description for accessibility */
            R.string.drawer_close /* "close drawer" description for accessibility */
            ) {

                @Override
                public void onDrawerClosed(View view) {

                    Log.d(TAG, "In activity");
                    supportInvalidateOptionsMenu();
                }

                @Override
                public void onDrawerOpened(View drawerView) {

                    supportInvalidateOptionsMenu();
                    SharedPreferences pref = AppLandingPageActivity.this.getSharedPreferences(AppLandingPageActivity.PREFS_NAME, 0);
                    unreadcount = pref.getInt("unreadcount", 0);
                    updateMessageCount(NavigationItem.valueOfKey("MESSAGE"), notificationCount, AppLandingPageActivity.this.unreadcount);
//                    SharedPreferences pref = getSharedPreferences("MyPrefsFile", MODE_PRIVATE);
//                    unreadcount = pref.getInt("unreadcount",0);
//                    NavigationItem navItem = NavigationItem.valueOfKey("MESSAGE");
//                    populateMessageCount(navItem, notificationCount, unreadcount);
                }
            };
        }
        return drawerListener;
    }

    public void setDrawerToggle(ActionBarDrawerToggle toggle) {

        mDrawerToggle = toggle;
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private ViewGroup  extraContentHolder;
    private ScrollView menuItemsScrollView;

    private void populateNavigationDrawerContent(int notificationCount, int messageCount) {

        setUserInfo();
        mDrawerLayout.findViewById(R.id.navigation_drawer_go_to_programs).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        closeDrawer();
                        Intent intent = new Intent(AppLandingPageActivity.this,
                                MyProgramsActivity.class);
                        startActivity(intent);
                    }
                });

        extraContentHolder = (ViewGroup) mDrawerContent
                .findViewById(R.id.navigation_extra_content_holder);
        menuItemsScrollView = (ScrollView) mDrawerContent
                .findViewById(R.id.navigation_items_holder_scroll_view);
        menuItems = getResources().getStringArray(R.array.navigation_items);
        navItemsHolder = (LinearLayout) findViewById(R.id.navigation_items_holder);


        OnClickListener navItemClickListner = new OnClickListener() {

            @Override
            public void onClick(View v) {

                NavigationItem navItem = (NavigationItem) v.getTag();


                if (navItem != null) {
                    selectNavItem(navItem);

                }
            }

        };

        for (String item : menuItems) {

                NavigationItem navItem = NavigationItem.valueOfKey(item);

                navRootView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                        .inflate(R.layout.list_item_view_navigation, navItemsHolder, false);


                navItemNameView = (TextView) navRootView
                        .findViewById(R.id.navigation_item_name);
                ((ImageView) navRootView.findViewById(R.id.navigation_item_img))
                        .setBackgroundResource(navItem.icon_res_id);

                menuCountTextView = (TextView) navRootView
                        .findViewById(R.id.menurow_counter);


                populateCount(navItem, notificationCount, messageCount);
                navItemNameView.setText(navItem.displayName);
                navItemsHolder.addView(navRootView);
                navRootView.setTag(navItem);
                navRootView.setOnClickListener(navItemClickListner);


            }
        }

    public void updateMessageCount(NavigationItem navItem, int notificationCount, int messageCount) {
        int childCount = this.navItemsHolder.getChildCount();
        for (int k = 0; k < childCount; k++) {
            View navItemView = this.navItemsHolder.getChildAt(k);
            if (((NavigationItem) navItemView.getTag()).equals(navItem)) {
                if (messageCount != 0) {
                    ((TextView) navItemView.findViewById(R.id.menurow_counter)).setText("" + messageCount);
                    ((TextView) navItemView.findViewById(R.id.menurow_counter)).setVisibility(View.GONE);
                    //navItemView.findViewById(R.id.menurow_counter).setBackground(
                          // getApplicationContext().getResources().getDrawable(R.drawable.custom_shape_leaderboard));

                } else {
                    ((TextView) navItemView.findViewById(R.id.menurow_counter)).setText(""+notificationCount);
                   ///navItemView.findViewById(R.id.menurow_counter).setBackground(
                       ///getApplicationContext().getResources().getDrawable(R.drawable.custom_shape_leaderboard));
                }
            }
        }
   }
    public void populateCount(NavigationItem navItem, int notificationCount, int messageCount ) {

        if (navItem != null && navItem.displayName.equalsIgnoreCase("Message") && messageCount != 0) {
            menuCountTextView.setText("" + messageCount);
            menuCountTextView.findViewById(R.id.menurow_counter).setBackground(
                    getApplicationContext().getResources().getDrawable(R.drawable.custom_shape_leaderboard));

        } else if ((navItem != null && navItem.displayName.equalsIgnoreCase("Notifications")) && notificationCount != 0) {
            menuCountTextView.setText("" + notificationCount);
            menuCountTextView.findViewById(R.id.menurow_counter).setBackground(
                    getApplicationContext().getResources().getDrawable(R.drawable.custom_shape_leaderboard));

        } else if (navItem != null && !navItem.displayName.equalsIgnoreCase("Message")) {
            menuCountTextView.setText("");

            Log.e("update notification..", "" + notificationCount);
        }
    }



    public void setUserInfo() {

        TextView programView = (TextView) mDrawerLayout.findViewById(R.id.navigation_user_prog);
        View noProgramView = mDrawerLayout.findViewById(R.id.navigation_no_program_selected);
        String currentSectionName = sessionManager
                .getSessionStringValue(ConstantGlobal.CURRENT_SECTION_NAME);
        if (!TextUtils.isEmpty(currentSectionName)) {
            programView.setVisibility(View.VISIBLE);
            noProgramView.setVisibility(View.GONE);
            programView.setText(currentSectionName);
        } else {
            programView.setVisibility(View.GONE);
            noProgramView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        menu.clear();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action buttons
        switch (item.getItemId()) {
        case android.R.id.home:
            boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerContent);
            if (!drawerOpen) {
                mDrawerLayout.openDrawer(mDrawerContent);
            } else {
                mDrawerLayout.closeDrawer(mDrawerContent);
            }
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void setTitle(CharSequence title) {

        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    private void selectNavItem(final NavigationItem navItem) {

        Fragment fragment = null;
        MemberProfile profile = LibraryUtils._getMemberProfile(getApplicationContext());
        boolean isStudent = MemberProfile.STUDENT == profile
                || MemberProfile.OFFLINE_USER == profile;
        Log.i(TAG, "CURRENT NAV ITEM ================ " + currentNavItem);


        switch (navItem) {
        case ACTIVITY:
            Toast.makeText(this, "We are still waiting for apis from Shankar", Toast.LENGTH_SHORT)
                    .show();
            break;
        case LIBRARY:
            if (currentNavItem != null && currentNavItem.equals(NavigationItem.LIBRARY_CONTENT)) {
                hideMainMenuAndShowMenuHead();
                return;
            } else {
                fragment = new LibraryCoursesFragment();
            }
            break;
        case ANALYTICS:
            if (isStudent) {
                fragment = new OverallAnalyticsFragment();
            } else {
                fragment = new TeacherOverallAnalyticsFragment();
            }
            break;
        case PROFILE:
            fragment = new ProfileFragment();
            break;
        case DOUBTS:
            if (currentNavItem != null && currentNavItem.equals(NavigationItem.DOUBTS)) {
                hideMainMenuAndShowMenuHead();
                return;
            } else {
                fragment = new DoubtsFragment();
            }
            break;

            case RECENTACTIVITY:
                fragment = new RecentActivitiesFragment();
                break;
            case MESSAGE:
                fragment = new MessageFragment();

                break;

            case NOTIFICATIONS:
                Log.e("notification ","on click");
                notificationCount = 0;
                fragment = new NotificationFragment();
                break;
            case CHALLENGEARENA:
          fragment = new ChallengesFragment();

                break;

            case LEADERBOARD:
                fragment = new GlobalChallengeLeaderFragment();

                break;


        default:
            break;
        }
        mDrawerLayout.closeDrawer(mDrawerContent);
        final Fragment fragmentToLoad = fragment;
        // TODO this is a wrong way of doing, could have put it in OnDrawerClosed
        // 200 is the amount of time it takes to close drawer approximately
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                replaceFragment(fragmentToLoad, navItem);
            }
        }, 400);

    }

    public void replaceFragment(Fragment fragment, NavigationItem navItem) {

        int navItemsCount = navItemsHolder.getChildCount();
        for (int k = 0; k < navItemsCount; k++) {
            View navItemView = navItemsHolder.getChildAt(k);
            NavigationItem thisItemTag = (NavigationItem) navItemView.getTag();

            if (thisItemTag.equals(navItem)) {
                Log.e("notificationCount",".."+notificationCount);
                ((TextView) navItemView.findViewById(R.id.navigation_item_name))
                        .setTextColor(getResources().getColor(R.color.blue));


                if(notificationCount == 0 && navItem.displayName.equalsIgnoreCase("Notifications")){
                    ((TextView) navItemView.findViewById(R.id.menurow_counter)).setText("");
                    ((TextView) navItemView.findViewById(R.id.menurow_counter)).setVisibility(View.GONE);

                    Log.e("notificationCount",".."+notificationCount);
                }

            } else {
                ((TextView) navItemView.findViewById(R.id.navigation_item_name))
                        .setTextColor(getResources().getColor(R.color.black));
            }



            navItemView.findViewById(R.id.navigation_item_right_arrow).setVisibility(View.GONE);
            navItemView.setVisibility(View.VISIBLE);
        }

        // cleaning/resetting navigation drawer content
        extraContentHolder.setVisibility(View.GONE);
        showMainMenuItems(0);

        final View syncButton = mDrawerContent.findViewById(R.id.navigation_sync_button);
        syncButton.setVisibility(View.GONE);
        syncButton.setOnClickListener(null);
        mDrawerContent.findViewById(R.id.navigation_sync_in_progress).setVisibility(View.GONE);
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        getSupportActionBar().setDisplayShowCustomEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        //getSupportActionBar().setDisplayShowHomeEnabled(true);
       // getSupportActionBar().setIcon(R.drawable.ic_launcher);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.navbar);
        mDrawerToggle = getDrawerListener();
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        if (fragment != null) {
            try {
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.main_content_frame, fragment)
                        .commit();
                currentNavItem = navItem;
                currentFragment = fragment;
                getSupportActionBar().setTitle(navItem.displayName);

            } catch (Throwable e) {}
        }

    }


    public void setCurrentNavItem(NavigationItem navItem) {

        this.currentNavItem = navItem;
    }

    public RelativeLayout getNavigationDrawerLayout() {

        return mDrawerContent;
    }

    public ViewGroup setUpMenuHead(int subMenuLayoutResId, NavigationItem navItem) {

        hideMainMenuAndShowMenuHead();
        extraContentHolder.removeAllViews();

        ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(subMenuLayoutResId,
                extraContentHolder, true);
        // putting the menu head
        final ViewGroup menuHead = (ViewGroup) extraContentHolder
                .findViewById(R.id.navigation_drawer_menu_head);

        menuHead.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                showMainMenuItems(200);
            }
        });
        Log.d(TAG, "name is " + navItem.displayName);

        ((TextView) extraContentHolder.findViewById(R.id.navigation_drawer_menu_head_title))
                .setText(navItem.displayName);
        ((ImageView) extraContentHolder.findViewById(R.id.navigation_drawer_menu_head_img))
                .setBackgroundResource(navItem.icon_res_id);

        ((ViewGroup) mDrawerContent.findViewWithTag(navItem)).findViewById(
                R.id.navigation_item_right_arrow).setVisibility(View.VISIBLE);


        return extraContentHolder;
    }

    private void showMainMenuItems(int animDuration) {

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            int mDrawerContentWidth = mDrawerContent.getWidth();
            final ObjectAnimator extraContentsAnimation = ObjectAnimator.ofFloat(
                    extraContentHolder, "x", 0, mDrawerContentWidth);
            extraContentsAnimation.setDuration(animDuration);

            final ObjectAnimator menuItemsAnimation = ObjectAnimator.ofFloat(menuItemsScrollView,
                    "x", -mDrawerContentWidth, 0);
            menuItemsAnimation.setDuration(animDuration);
            extraContentsAnimation.start();
            menuItemsAnimation.start();
        } else {
            extraContentHolder.setVisibility(View.GONE);
            menuItemsScrollView.setVisibility(View.VISIBLE);
        }

    }

    private void hideMainMenuAndShowMenuHead() {

        extraContentHolder.setVisibility(View.VISIBLE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            int animDuration = 200;
            int mDrawerContentWidth = mDrawerContent.getWidth();
            final ObjectAnimator extraContentsAnimation = ObjectAnimator.ofFloat(
                    extraContentHolder, "x", mDrawerContentWidth, 0);
            extraContentsAnimation.setDuration(animDuration);

            final ObjectAnimator menuItemsAnimation = ObjectAnimator.ofFloat(menuItemsScrollView,
                    "x", 0, -mDrawerContentWidth);
            menuItemsAnimation.setDuration(animDuration);
            extraContentsAnimation.start();
            menuItemsAnimation.start();
        } else {
            menuItemsScrollView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {

        super.onPostCreate(savedInstanceState);
        if (mDrawerToggle != null)
            mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);
        if (mDrawerToggle != null) {
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
        if (userGuideDialogFragment != null && userGuideDialogFragment.isVisible()) {
            userGuideDialogFragment.onStart();
        }
    }

    @Override
    protected void onResume() {

        sessionManager = SessionManager.getInstance(getApplicationContext());
        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);

            // Closing all the Activities!!
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Staring Login Activity
            startActivity(intent);
            finish();
        }
        super.onResume();
    }

    @Override
    public void onBackPressed() {

        if (currentNavItem != null
                && ((currentNavItem.equals(NavigationItem.LIBRARY_CONTENT) && currentFragment instanceof LibraryContentFragment) || currentNavItem
                        .equals(NavigationItem.ANALYTICS_COURSE))) {
            Fragment activeFragment = null;
            if (currentNavItem.equals(NavigationItem.LIBRARY_CONTENT)) {
                activeFragment = new LibraryCoursesFragment();
                currentNavItem = NavigationItem.LIBRARY;
            } else if (currentNavItem.equals(NavigationItem.ANALYTICS_COURSE)) {
                currentNavItem = NavigationItem.ANALYTICS;
                activeFragment = new OverallAnalyticsFragment();
            }
            replaceFragment(activeFragment, currentNavItem);

        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        if (outState != null && currentNavItem != null) {
            outState.putString(CURRENT_NAV_ITEM_NAME, currentNavItem.name());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public DrawerLayout getDrawerLayout() {

        return mDrawerLayout;
    }

    @Override
    public View getDrawerView() {

        return mDrawerContent;
    }

    @Override
    public View getSyncButton() {

        return getDrawerView().findViewById(R.id.navigation_sync_button);
    }

    @Override
    public boolean isDrawerOpen() {

        return getDrawerLayout().isDrawerOpen(getDrawerView());
    }

    @Override
    public void closeDrawer() {

        getDrawerLayout().closeDrawer(getDrawerView());
    }


    private void navigationDrawerSetUp(Bundle savedInstanceState, int count, int messageCount){
        setUpNavigationDrawer();
        populateNavigationDrawerContent(count, messageCount);

        if(fromNotification){
            selectNavItem(NavigationItem.NOTIFICATIONS);
            fromNotification = false;
        }else {
            if (savedInstanceState == null) {
                selectNavItem(NavigationItem.LIBRARY);
            } else {
                String navItemName = savedInstanceState.getString(CURRENT_NAV_ITEM_NAME);
                selectNavItem(NavigationItem.valueOfKey(navItemName));
            }
        }
    }


    public class GetNotificationAsyncTask extends AbstractVedantuJSONAsyncTask {

        private ProgressDialog pDialog;
        private String entityType;
        private String entityId;
        private List<Actor> commentsActorsList = new ArrayList<Actor>();
        private Bundle instance;

        public GetNotificationAsyncTask(SessionManager session, ProgressBar progressUpdater, Bundle bundleInstance) {
            super(session, progressUpdater);
            this.url = session.getApiUrl("getNotifcationsSummary");
            pDialog = new ProgressDialog(AppLandingPageActivity.this);
            pDialog.setMessage("Please wait..");
            pDialog.setCancelable(false);
            instance = bundleInstance;
            if(!isFinishing())
            {
                pDialog.show();
            }
            Log.e("getNotificatioAsyncTask", "call");
        }

        @Override
        protected JSONObject doInBackground(String... params) {

            session.addSessionParams(httpParams);
            httpParams.put("callingApp", "TapApp");
            httpParams.put("callingUserId", session.getOrgMemberInfo().userId);
            httpParams.put("userRole", session.getOrgMemberInfo().profile);
            httpParams.put("callingAppId", "TapApp");
            httpParams.put("orgId",session.getOrgMemberInfo().orgId);
            httpParams.put("userId",session.getOrgMemberInfo().userId);
            httpParams.put("action","getNotificationsSummary");
            httpParams.put("myUserId",session.getOrgMemberInfo().userId);
            httpParams.put("memberId",session.getOrgMemberInfo().firstName);
            JSONObject res = null;
            try {
                res = VedantuWebUtils.getJSONData(url, VedantuWebUtils.GET_REQ, httpParams);
                Log.e("getNotificaAsyncTask", "do in background");

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

                    notificationCount = result.getInt("totalHits");

                    Log.e("notificationCount", "" + notificationCount);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            pDialog.dismiss();
            new FetchCountTask(instance, notificationCount).execute();

        }
    }


    public class UpdatePushIdAsyncTask extends AbstractVedantuJSONAsyncTask {

        private ProgressDialog pDialog;


        public UpdatePushIdAsyncTask(SessionManager session, ProgressBar progressUpdater) {
            super(session, progressUpdater);
            this.url = session.getApiUrl("updateDevicePushId");
            pDialog = new ProgressDialog(AppLandingPageActivity.this);
            pDialog.setMessage("Please wait..");
            pDialog.setCancelable(false);
            if(!isFinishing())
            {
                pDialog.show();
            }


            Log.e("updatePushIdAsyncTask", "call");
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            session.addSessionParams(httpParams);
            String token = FirebaseInstanceId.getInstance().getToken();
            httpParams.put("callingApp", "TapApp");
            httpParams.put("callingUserId", session.getOrgMemberInfo().userId);
            httpParams.put("userRole", session.getOrgMemberInfo().profile);
            httpParams.put("callingAppId", "TapApp");
            httpParams.put("orgId",session.getOrgMemberInfo().orgId);
            httpParams.put("userId",session.getOrgMemberInfo().userId);
            httpParams.put("myUserId",session.getOrgMemberInfo().userId);
            httpParams.put("memberId",session.getOrgMemberInfo().firstName);
            httpParams.put("push_id", token);
            httpParams.put("deviceType","android");
            httpParams.put("targetUserId",session.getOrgMemberInfo().userId);

            JSONObject res = null;
            try {
                res = VedantuWebUtils.getJSONData(url, VedantuWebUtils.GET_REQ, httpParams);
                Log.e("DevicePushIdAsyncTask", "do in background");

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
                    String result = jsonObject.getString("result");


                    Log.e("response", "....."+result);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            pDialog.dismiss();

        }
    }


    /*
       Sample AsyncTask to fetch the notifications count
       */
    class FetchCountTask extends AsyncTask<JSONObject, JSONObject, JSONObject> {

        private final ProgressDialog pDialog;
        private JSONObject result1;
        private Bundle myInstance;
        private int notificationCount1;

        public FetchCountTask(Bundle instance, int notificationCount) {

           myInstance = instance;
            notificationCount1 = notificationCount;
            pDialog = new ProgressDialog(AppLandingPageActivity.this);
            pDialog.setMessage("Please wait..");
            pDialog.setCancelable(false);
            if(!isFinishing())
            {
                pDialog.show();
            }
        }
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
                pDialog.dismiss();
            }

            return jsonRes;


        }

        @Override
        public void onPostExecute(JSONObject jsonObject) {

            super.onPostExecute(jsonObject);

            if(jsonObject != null) {
                try {
                    result1 = jsonObject.getJSONObject("result");
                    unreadcount = result1.getInt("unreadConversationCount");
                    Log.e("UnreadMessagecount", "......." + unreadcount);
                    SharedPreferences.Editor editor = getSharedPreferences("MyPrefsFile",MODE_PRIVATE).edit();
                    editor.putInt("unreadcount",unreadcount);
                    editor.commit();



                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            pDialog.dismiss();
            navigationDrawerSetUp(myInstance, notificationCount1, unreadcount);
            new UpdatePushIdAsyncTask(SessionManager.getInstance(AppLandingPageActivity.this), null).executeTask(false);

        }

    }
}
