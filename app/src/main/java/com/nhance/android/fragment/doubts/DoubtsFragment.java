package com.nhance.android.fragment.doubts;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ActionMenuView;
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
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.nhance.android.activities.AppLandingPageActivity;
import com.nhance.android.activities.about.AboutActivity;
import com.nhance.android.activities.baseclasses.NhanceBaseActivity;
import com.nhance.android.activities.baseclasses.NhanceBaseFragment;
import com.nhance.android.async.tasks.ITaskProcessor;
import com.nhance.android.async.tasks.doubts.DoubtListRetreiverTask;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.enums.NavigationItem;
import com.nhance.android.jsinterfaces.DoubtJSInterface;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.utils.GoogleAnalyticsUtils;
import com.nhance.android.utils.JSONUtils;
import com.nhance.android.utils.SQLDBUtil;
import com.nhance.android.utils.VedantuWebUtils;
import com.nhance.android.web.VedantuWebChromeClient;
import com.nhance.android.R;

public class DoubtsFragment extends NhanceBaseFragment implements ITaskProcessor<JSONObject> {

    private final String             TAG                       = "DoubtsFragment";

    static final int                 SINGLE_DOUBT_REQUEST_CODE = 100;
    static final int                 ASK_DOUBT_REQUEST_CODE    = 110;

    static final String              DOUBT_EXTRA_MESSAGE       = "DOUBT_DATA";
    static final String              DOUBT_INDEX               = "DOUBT_INDEX";
    static final String              KEY_ADD_ANSWER            = "ADDED_ANSWER";
    static final String              KEY_FOLLOW_UNFOLLOW       = "FOLLOW_UNFOLLOW_DOUBT";
    static final String              KEY_FOLLOW_UNFOLLOW_TYPE  = "FOLLOW_UNFOLLOW_DOUBT_TYPE";
    static final String              ADDED_DOUBT               = "DOUBT_ADDED";

    private View                     fragmentRootView;

    private WebView                  doubtsContainer;
    private boolean                  isWebViewLoaded           = false;
    private boolean                  isDestroyed               = false;

    private DoubtsJSInterface        jsInterface;
    private DoubtListRetreiverTask   doubtRetreiverTask;
    TextView                         progressBar;
    private SessionManager           session;
    private SearchView searchView;
    private MenuItem refreshButton;
    private RelativeLayout           navigationDrawerLayout;
    private LayoutInflater           viewInflater;

    // PARAMETERS FOR DOUBTS
    private String                   queryString;
    private boolean                  loadFacets                = true;
    private boolean                  loadTopicFacets           = false;
    private String                   orderBy                   = "timeCreated";
    private String                   resultType                = "ALL";
    private List<String>             brdIds;
    private final int                SIZE                      = 10;
    private int                      doubtsStart               = 0;
    private int                      totalHits                 = 0;
    private boolean                  doubtsFetchInProgress     = false;

    private JSONArray                doubtList;
    private JSONObject               facets;

    private final String             TYPE_COURSE               = "COURSE";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        fragmentRootView = inflater.inflate(R.layout.activity_doubts, container, false);
        viewInflater = inflater;
        return fragmentRootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        GoogleAnalyticsUtils.setScreenName(ConstantGlobal.GA_SCREEN_DOUBTS);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        if(null == m_cObjNhanceBaseActivity) {
            m_cObjNhanceBaseActivity = (NhanceBaseActivity) getActivity();
        }
        navigationDrawerLayout = ((AppLandingPageActivity) m_cObjNhanceBaseActivity)
                .getNavigationDrawerLayout();
        session = SessionManager.getInstance(m_cObjNhanceBaseActivity.getApplicationContext());
        boolean isLoggedIn = session.checkLogin();

        if (!isLoggedIn)
            return;

        setRetainInstance(true);
        super.setRetainInstance(true);

        Log.e("onActivity ", "created");
        setSpinnerValues();
        setAskDoubtBtn();
        setUpWebView();
        setUpFacets();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SINGLE_DOUBT_REQUEST_CODE) {
                int dbtIndex = data.getIntExtra(DOUBT_INDEX, 0);
                int dbtAnswers = Integer.parseInt(data.getStringExtra(KEY_ADD_ANSWER));
                int dbtFollowers = Integer.parseInt(data.getStringExtra(KEY_FOLLOW_UNFOLLOW));
                String dbtFollowType = data.getStringExtra(KEY_FOLLOW_UNFOLLOW_TYPE);
                JSONObject doubt;
                try {
                    doubt = doubtList.getJSONObject(dbtIndex);
                    doubt.put("comments", dbtAnswers);
                    doubt.put("followers", dbtFollowers);
                    doubt.put("followType", dbtFollowType);
                    jsInterface.updateDoubt(dbtIndex, doubt);
                } catch (JSONException e) {
                    Log.d(TAG, e.getLocalizedMessage());
                }
            } else if (requestCode == ASK_DOUBT_REQUEST_CODE) {
                String result = data.getStringExtra(ADDED_DOUBT);
                try {
                    JSONObject doubt = new JSONObject(result);
                    addDoubtInUi(doubt);
                } catch (JSONException e) {
                    Log.d(TAG, e.getLocalizedMessage());
                }
            }
        }
    }

    private void addDoubtInUi(JSONObject doubt) {

        jsInterface.prependDoubt(doubt);
        doubtsStart++;
        JSONArray tempList = new JSONArray();
        try {
            tempList.put(0, doubt);
            doubtList = JSONUtils.concatArray(tempList, doubtList);
        } catch (JSONException e) {
            Log.d(TAG, e.getLocalizedMessage());
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setUpWebView() {

        doubtsContainer = (WebView) fragmentRootView.findViewById(R.id.doubts_container);
        progressBar = (TextView) fragmentRootView.findViewById(R.id.doubt_progressBar);
        progressBar.setVisibility(View.VISIBLE);

        doubtsContainer.getSettings().setJavaScriptEnabled(true);
        doubtsContainer.setWebChromeClient(new VedantuWebChromeClient());
        jsInterface = new DoubtsJSInterface(doubtsContainer);
        doubtsContainer.addJavascriptInterface(jsInterface, "doubtJSInterface");
        doubtsContainer.loadUrl("file:///android_asset/html/doubts_view.html");
        doubtsContainer.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {

                if (isDestroyed) {
                    return;
                }
                Log.d(TAG, "Static Html page load finished.");
                super.onPageFinished(view, url);
                isWebViewLoaded = true;
                progressBar.setVisibility(View.GONE);
                jsInterface.loadDimensionFiles();
                resetParams();
                loadDoubts();
            }
        });
    }

    private void loadDoubts() {

        if (isDestroyed)
            return;
        jsInterface.showLoading();
        clearAllreadyPresentFacets();
        refreshButton.setActionView(R.layout.progress_bar_in_action_bar);
        refreshButton.setTitle(R.string.loading);
        if (getActivity() instanceof AppLandingPageActivity) {
            ((LinearLayout) navigationDrawerLayout.findViewById(R.id.navigation_sync_button)).setVisibility(View.GONE);
            navigationDrawerLayout.findViewById(R.id.navigation_sync_in_progress).setVisibility(View.VISIBLE);
        }
        loadDoubts(true, doubtsStart);
    }

    private void resetSearch() {

        if (searchView != null) {
            searchView.clearFocus();
            queryString = null;
            searchView.setQuery(queryString, false);
            searchView.setIconified(true);
        }
    }

    private void resetParams() {

        resetSearch();
        orderBy = "timeCreated";
        resultType = "ALL";
        brdIds = null;
        resetData(true);
    }

    private void resetData(boolean resetFacetsVal) {

        if (resetFacetsVal) {
            loadFacets = true;
            loadTopicFacets = false;
        }
        doubtsStart = 0;
        doubtList = null;
        jsInterface.resetValues();
    }

    private void loadDoubts(boolean reloadTopicFacets, int start) {

        List<String> queriedBrdIds = (brdIds == null ? null : new ArrayList<String>(brdIds));
        jsInterface.setFetchInProgress(true);

        Log.d(TAG, "LOADING DOUBTS STARTED========== ");
        if (isDestroyed || m_cObjNhanceBaseActivity == null) {
            return;
        }
        doubtRetreiverTask = new DoubtListRetreiverTask(SessionManager.getInstance(m_cObjNhanceBaseActivity
                .getApplicationContext()), null, this, queryString, queriedBrdIds, orderBy,
                SQLDBUtil.ORDER_DESC, resultType, loadFacets, reloadTopicFacets, start, SIZE, true);
        doubtsStart = start;
        doubtRetreiverTask.executeTask(false);

        // HIDE OPEN KEYBOARD
        hideKeyboard();
    }

    private void setSpinnerValues() {
        Log.e("onActivity ", "created");
        Spinner spinner = (Spinner) fragmentRootView.findViewById(R.id.doubts_list_filter);
        final String[] orderBysList = { "timeCreated", "mostPopular", "timeCreated", "timeCreated" };
        final String[] resultTypesList = { "ALL", "ALL", "FOLLOWING", "CREATED" };
        Log.e("onItem","orderBysList ");
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.doubts_list_filter));
        spinner.setAdapter(spinnerArrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
                Log.e("onItem","orderBysList "+orderBysList[position]);
                if (isWebViewLoaded) {
                    resetParams();
                    orderBy = orderBysList[position];
                    resultType = resultTypesList[position];
                    loadDoubts();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }

        });
    }

    private void setAskDoubtBtn() {

        Button askDoubtBtn = (Button) fragmentRootView.findViewById(R.id.ask_doubt);
        askDoubtBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent(m_cObjNhanceBaseActivity, AskDoubtActivity.class);
                startActivityForResult(intent, ASK_DOUBT_REQUEST_CODE);
            }
        });
    }

    final class DoubtsJSInterface extends DoubtJSInterface {

        public DoubtsJSInterface(WebView myWebView) {

            super(myWebView);
        }

        @JavascriptInterface
        public void openDoubt(int index) {

            Log.d(TAG, "openDoubt function is called : " + index);
            Intent intent = new Intent(m_cObjNhanceBaseActivity, SingleDoubtActivity.class);
            try {
                intent.putExtra(DOUBT_EXTRA_MESSAGE, doubtList.getJSONObject(index).toString());
                intent.putExtra(DOUBT_INDEX,index);
                startActivityForResult(intent, SINGLE_DOUBT_REQUEST_CODE);
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }

        @JavascriptInterface
        public void resetValues() {

            if (!isWebViewLoaded)
                return;
            mHandler.post(new Runnable() {

                @Override
                public void run() {

                    myWebView.loadUrl("javascript:resetValues()");
                }
            });
        }

        @JavascriptInterface
        public void showLoading() {

            if (!isWebViewLoaded)
                return;
            mHandler.post(new Runnable() {

                @Override
                public void run() {

                    if (isDestroyed)
                        return;
                    int fontSize = (int) getResources().getDimension(
                            R.dimen.doubts_loading_font_size);
                    myWebView.loadUrl("javascript:showLoadingForDoubts(" + fontSize + ")");
                }
            });
        }

        @JavascriptInterface
        public void appendLoading() {

            if (!isWebViewLoaded)
                return;
            mHandler.post(new Runnable() {

                @Override
                public void run() {

                    if (isDestroyed)
                        return;
                    int fontSize = (int) getResources().getDimension(
                            R.dimen.doubts_loading_font_size);
                    myWebView.loadUrl("javascript:appendLoadingForDoubts(" + fontSize + ")");
                }
            });
        }

        @JavascriptInterface
        public void showLoadError() {

            if (!isWebViewLoaded)
                return;
            mHandler.post(new Runnable() {

                @Override
                public void run() {

                    if (isDestroyed)
                        return;
                    int fontSize = (int) getResources().getDimension(
                            R.dimen.doubts_loading_font_size);
                    myWebView.loadUrl("javascript:showLoadDoubtsError(" + fontSize + ")");
                }
            });
        }

        @JavascriptInterface
        public void clearContainer() {

            if (!isWebViewLoaded)
                return;
            mHandler.post(new Runnable() {

                @Override
                public void run() {

                    if (isDestroyed)
                        return;
                    myWebView.loadUrl("javascript:clearContainer()");
                }
            });
        }

        @JavascriptInterface
        public void removeLoading() {

            if (!isWebViewLoaded)
                return;
            mHandler.post(new Runnable() {

                @Override
                public void run() {

                    if (isDestroyed)
                        return;
                    myWebView.loadUrl("javascript:removeLoading()");
                }
            });
        }

        @JavascriptInterface
        public void setAllDoubtsLoaded() {

            if (!isWebViewLoaded)
                return;
            mHandler.post(new Runnable() {

                @Override
                public void run() {

                    if (isDestroyed)
                        return;
                    myWebView.loadUrl("javascript:setAllDoubtsLoaded()");
                }
            });
        }

        @JavascriptInterface
        public void setFetchInProgress(boolean inProgress) {

            doubtsFetchInProgress = inProgress ? true : false;
            if (!isWebViewLoaded)
                return;
            mHandler.post(new Runnable() {

                @Override
                public void run() {

                    if (isDestroyed)
                        return;
                    myWebView.loadUrl("javascript:setFetchInProgress(" + doubtsFetchInProgress
                            + ")");
                }
            });
        }

        @JavascriptInterface
        @Override
        public void loadDimensionFiles() {

            if (!isWebViewLoaded)
                return;
            mHandler.post(new Runnable() {

                @Override
                public void run() {

                    if (isDestroyed)
                        return;
                    float dimen = getResources().getDimension(R.dimen.device_dimension_value);
                    Log.d(TAG, "============== DIMENSION VALUE ========== " + dimen);
                    int dimension = (int) dimen;
                    myWebView.loadUrl("javascript:loadDimensionFile(" + dimension + ")");
                }
            });
        }

        @JavascriptInterface
        public void loadMoreDoubts() {

            if (!isWebViewLoaded)
                return;
            mHandler.post(new Runnable() {

                @Override
                public void run() {

                    final int start = doubtsStart + SIZE;
                    if (start < totalHits && !doubtsFetchInProgress) {
                        loadFacets = false;
                        loadDoubts(false, start);
                    } else {
                        removeLoading();
                        setAllDoubtsLoaded();
                    }
                }
            });
        }

        @JavascriptInterface
        public void showZeroLevel() {

            if (!isWebViewLoaded)
                return;
            mHandler.post(new Runnable() {

                @Override
                public void run() {

                    if (isDestroyed)
                        return;
                    int dimension = (int) getResources().getDimension(
                            R.dimen.device_dimension_value);
                    myWebView.loadUrl("javascript:showZeroLevel(" + dimension + ")");
                }
            });
        }
    }

    @Override
    public void onTaskStart(JSONObject result) {

    }

    @Override
    public void onTaskPostExecute(boolean success, JSONObject resp) {

        if (isDestroyed) {
            return;
        }
        Log.d(TAG, "Data Loaded for doubts");

        if (!success || resp == null) {
            jsInterface.showLoadError();
            return;
        }
        // boolean reloadTopicFacets = false;
        JSONObject result = JSONUtils.getJSONObject(resp, VedantuWebUtils.KEY_RESULT);
        totalHits = JSONUtils.getInt(result, VedantuWebUtils.KEY_TOTAL_HITS);
        JSONArray doubtsList = JSONUtils.getJSONArray(result, VedantuWebUtils.KEY_LIST);
        facets = JSONUtils.getJSONObject(result, VedantuWebUtils.KEY_FACETS);
        Log.d(TAG, "doubts totalHits : " + totalHits);

        if (totalHits <= 0) {
            jsInterface.showZeroLevel();
        }

        for (int d = 0; d < doubtsList.length(); d++) {
            try {
                JSONObject doubt = doubtsList.getJSONObject(d);
                // Log.d(TAG,doubt.toString());
                int doubtIndex = doubtsStart + d;
                jsInterface.appendDoubt(doubtIndex, doubt);
                Log.d(TAG, "doubt drawn : " + d);
            } catch (JSONException e) {
                Log.d(TAG, "Error traversing doubt list " + e.getLocalizedMessage());
            }
        }
        if (doubtList == null) {
            doubtList = doubtsList;
        } else {
            try {
                doubtList = JSONUtils.concatArray(doubtList, doubtsList);
            } catch (JSONException e) {
                Log.d(TAG, "ERROR While JOINING ARRAY ==== " + e.getLocalizedMessage());
            }
        }
        jsInterface.removeLoading();
        jsInterface.setFetchInProgress(false);
        if ((doubtsStart + doubtsList.length()) >= totalHits) {
            jsInterface.setAllDoubtsLoaded();
        } else {
            jsInterface.appendLoading();
        }
        if (loadFacets) {
            showSubjectFacets();
        } else if (loadTopicFacets) {
            showTopicFacets();
        }
        resetRefreshButton();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) m_cObjNhanceBaseActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
    }

    private SearchView.OnQueryTextListener queryListener = new SearchView.OnQueryTextListener() {

         @Override
         public boolean onQueryTextSubmit(String query) {
             queryString = query;
             brdIds = null;
             resetData(true);
             loadDoubts();
             hideKeyboard();
             return false;
         }

         @Override
         public boolean onQueryTextChange(String newText) {
             if (TextUtils.isEmpty(newText) && queryString != null) {
                 // resetSearch();
                 brdIds = null;
                 queryString = null;
                 resetData(true);
                 loadDoubts();
                 hideKeyboard();
             }
             return false;
         }
    };

    private void resetRefreshButton() {
        if (refreshButton != null && !isDestroyed) {
            refreshButton.setActionView(null);
        }
        if (getActivity() instanceof AppLandingPageActivity) {
            ((LinearLayout) navigationDrawerLayout.findViewById(R.id.navigation_sync_button)).setVisibility(View.VISIBLE);
            navigationDrawerLayout.findViewById(R.id.navigation_sync_in_progress).setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        menu.clear();
        inflater.inflate(R.menu.doubts, menu);
        MenuItem lItem = menu.findItem(R.id.search_doubts);
        searchView = (SearchView) MenuItemCompat.getActionView(lItem);
        searchView.setIconifiedByDefault(true);
        searchView.setOnQueryTextListener(queryListener);
        refreshButton = menu.findItem(R.id.refresh_doubts_btn);
        refreshButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                item.setActionView(R.layout.progress_bar_in_action_bar);
                item.setTitle(R.string.loading);
                resetData(false);
                loadDoubts();
                hideKeyboard();
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action buttons
        switch (item.getItemId()) {
        case R.id.action_logout:
            session.logoutUser();
            return true;
        case R.id.action_about:
            Intent intent = new Intent(m_cObjNhanceBaseActivity, AboutActivity.class);
            startActivity(intent);
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void setUpFacets() {

        if (getActivity() instanceof AppLandingPageActivity) {
            ((AppLandingPageActivity) getActivity()).setUpMenuHead(R.layout.doubts_facets, NavigationItem.DOUBTS);
            View syncButton = navigationDrawerLayout.findViewById(R.id.navigation_sync_button);

            syncButton.setVisibility(View.VISIBLE);
            syncButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    resetData(false);
                    loadDoubts();
                    hideKeyboard();
                }
            });
        }
    }

    private void clearAllreadyPresentFacets() {

        if (loadFacets) {
            LinearLayout courseFacetHolder = (LinearLayout) navigationDrawerLayout.findViewById(R.id.doubts_subject_facet_holder);
            if (courseFacetHolder.getChildCount() > 0) {
                courseFacetHolder.removeAllViewsInLayout();
            }
            LinearLayout topicFacetHolder = (LinearLayout) navigationDrawerLayout.findViewById(R.id.doubts_topic_facet_holder);
            if (topicFacetHolder.getChildCount() > 0) {
                topicFacetHolder.removeAllViewsInLayout();
            }
        } else if (loadTopicFacets) {
            LinearLayout topicFacetHolder = (LinearLayout) navigationDrawerLayout.findViewById(R.id.doubts_topic_facet_holder);
            if (topicFacetHolder.getChildCount() > 0) {
                topicFacetHolder.removeAllViewsInLayout();
            }
        }
    }

    private void showSubjectFacets() {
        if (navigationDrawerLayout != null) {
            JSONArray courseFacets = JSONUtils.getJSONArray(facets, VedantuWebUtils.KEY_COURSES);
            LinearLayout courseFacetHolder = (LinearLayout) navigationDrawerLayout
                    .findViewById(R.id.doubts_subject_facet_holder);
            TextView allView = drawFacetItem("All", "ALL", TYPE_COURSE, totalHits,
                    courseFacetHolder);
            selectFacetItemUi(allView, TYPE_COURSE);
            int length = courseFacets.length();
            for (int index = 0; index < length; index++) {
                try {
                    JSONObject course = courseFacets.getJSONObject(index);
                    drawFacetItem(course, courseFacetHolder);
                } catch (JSONException e) {
                    Log.d(TAG, e.getLocalizedMessage());
                }
            }
            showTopicFacets();
        }
    }

    private void showTopicFacets() {

        if (navigationDrawerLayout != null) {
            JSONArray topicFacets = JSONUtils.getJSONArray(facets, VedantuWebUtils.KEY_TOPICS);
            LinearLayout topicFacetHolder = (LinearLayout) navigationDrawerLayout
                    .findViewById(R.id.doubts_topic_facet_holder);
            int length = topicFacets.length();
            for (int index = 0; index < length; index++) {
                try {
                    JSONObject topic = topicFacets.getJSONObject(index);
                    drawFacetItem(topic, topicFacetHolder);
                } catch (JSONException e) {
                    Log.d(TAG, e.getLocalizedMessage());
                }
            }
        }
    }

    private void drawFacetItem(JSONObject data, LinearLayout holder) throws JSONException {

        int count = data.getInt("count");
        data = data.getJSONObject("obj");
        String id = data.getString("id");
        String name = data.getString("name");
        String brdType = data.getString("type");
        drawFacetItem(name, id, brdType, count, holder);
    }

    private TextView drawFacetItem(final String name, final String id, final String brdType,
            final int count, LinearLayout holder) {

        TextView textView = (TextView) viewInflater.inflate(R.layout.doubt_each_facet_item, holder,
                false);
        String text = (String) TextUtils.concat(name, "(", Integer.toString(count), ")");
        textView.setText(text);
        textView.setTag(R.integer.doubt_brd_id_key, id);
        textView.setTag(R.integer.doubt_brd_type_key, brdType);
        textView.setTag(R.integer.doubt_brd_selected, false);
        textView.setOnClickListener(onFacetItemClicked);
        holder.addView(textView);
        return textView;
    }

    private void selectFacetItemUi(final TextView view, final String type) {

        boolean isSelected = (Boolean) view.getTag(R.integer.doubt_brd_selected);
        if (TYPE_COURSE.equals(type)) {
            resetAllFacetItemUi(R.id.doubts_subject_facet_holder);
            resetAllFacetItemUi(R.id.doubts_topic_facet_holder);
            if (!isSelected) {
                setFacetItemUi(view);
            }
        } else {
            if (isSelected) {
                resetFacetItemUi(view);
            } else {
                setFacetItemUi(view);
            }
        }
    }

    private void setFacetItemUi(TextView view) {
        view.setTextColor(m_cObjNhanceBaseActivity.getResources().getColor(R.color.blue));
        Drawable tick = m_cObjNhanceBaseActivity.getResources().getDrawable(R.drawable.tick);
        view.setCompoundDrawablesWithIntrinsicBounds(tick, null, null, null);
        view.setPadding(0, 0, 0, 0);
        view.setTag(R.integer.doubt_brd_selected, true);
    }

    private void resetAllFacetItemUi(int holderId) {

        LinearLayout facetHolder = (LinearLayout) navigationDrawerLayout.findViewById(holderId);
        for (int index = 0; index < facetHolder.getChildCount(); index++) {
            TextView child = (TextView) facetHolder.getChildAt(index);
            resetFacetItemUi(child);
        }
    }

    private void resetFacetItemUi(TextView view) {

        int color = m_cObjNhanceBaseActivity.getResources().getColor(R.color.white_trans_90);
        int defaultPadLeft = (int) m_cObjNhanceBaseActivity.getResources().getDimension(
                R.dimen.doubt_nav_item_pad_left);
        view.setTextColor(color);
        view.setCompoundDrawables(null, null, null, null);
        view.setPadding(defaultPadLeft, 0, 0, 0);
        view.setTag(R.integer.doubt_brd_selected, false);
    }

    private OnClickListener onFacetItemClicked = new OnClickListener() {

       @Override
       public void onClick(View v) {
           TextView textView = (TextView) v;
           String id = (String) textView.getTag(R.integer.doubt_brd_id_key);
           String type = (String) textView.getTag(R.integer.doubt_brd_type_key);
           boolean isSelected = (Boolean) textView.getTag(R.integer.doubt_brd_selected);
           // resetSearch();
           resetData(true);
           if ("ALL".equals(id)) {
               brdIds = null;
               selectFacetItemUi(textView, type);
           } else {
               loadFacets = false;
               if (TYPE_COURSE.equals(type)) {
                   if (isSelected) {
                       brdIds = null;
                       loadFacets = true;
                       selectFacetItemUi(textView, type);
                   } else {
                       loadTopicFacets = true;
                       brdIds = new ArrayList<String>();
                       brdIds.add(id);
                       selectFacetItemUi(textView, type);
                   }
               } else {
                   if (brdIds == null) {
                       brdIds = new ArrayList<String>();
                   }
                   if (isSelected) {
                       brdIds.remove(id);
                   } else {
                       brdIds.add(id);
                   }
                   selectFacetItemUi(textView, type);
               }
           }
           if (navigationDrawerLayout != null) {
               ((AppLandingPageActivity) m_cObjNhanceBaseActivity).getDrawerLayout().closeDrawer(navigationDrawerLayout);
           }
           Log.d(TAG, "CURRENT BRDID ============== "
                   + id);
           Log.d(TAG,
                   "ALL BRDIDS SELECTED ============== "
                           + brdIds);
           loadDoubts();
       }
    };

    @Override
    public void onDestroy() {
        m_cObjNhanceBaseActivity = null;
        doubtList = null;
        isWebViewLoaded = false;
        isDestroyed = true;
        super.onDestroy();
    }

    @Override
    public void onResume() {

        GoogleAnalyticsUtils.sendPageViewDataToGA();
        super.onResume();
    }
}
