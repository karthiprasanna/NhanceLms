package com.nhance.android.fragments;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.nhance.android.activities.SignupAndLoginActivity;
import com.nhance.android.activities.baseclasses.NhanceBaseFragment;
import com.nhance.android.async.tasks.AbstractVedantuJSONAsyncTask;
import com.nhance.android.async.tasks.ITaskProcessor;
import com.nhance.android.async.tasks.socials.WebCommunicatorTask;
import com.nhance.android.async.tasks.socials.WebCommunicatorTask.ReqAction;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.OrgDataManager;
import com.nhance.android.db.datamanagers.UserDataManager;
import com.nhance.android.db.models.Organization;
import com.nhance.android.factory.CMDSUrlFactory;
import com.nhance.android.login.utils.LoginUtils;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.utils.JSONUtils;
import com.nhance.android.utils.VedantuWebUtils;
import com.nhance.android.R;

public class LoginPageFragment extends NhanceBaseFragment implements View.OnClickListener {

    private final String   TAG            = "LoginPageFragment";
    int                    currentPos;
    private boolean        initLoginPortion;
    private View           fragmentRootView;
    private OrgDataManager orgDataManager = null;
    private SessionManager sessionManager = null;
    private GetOrgInfo     addOrgReq      = null;
    private Organization   activeOrg      = null;

    public static Fragment newInstance(int position) {

        LoginPageFragment f = new LoginPageFragment();
        Bundle args = new Bundle();
        args.putInt(ConstantGlobal.POSITION, position);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        initLoginPortion = false;
        currentPos = getArguments() != null ? getArguments().getInt(ConstantGlobal.POSITION) : 4;
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        int layoutResId = 0;
        int imageResId = 0;
        int imageTitleResId = 0;
        int imageDescResId = 0;
        int imageTitleColorResId = 0;

        if (currentPos == 0) {
            layoutResId = R.layout.fragment_login_landing_page;
            imageResId = R.drawable.logo_for_acme;
        } /*else if (currentPos < 4) {
            layoutResId = R.layout.fragment_login_image_page;
            switch (currentPos) {
            case 1:
                imageResId = R.drawable.login_consume_content;
                imageTitleResId = R.string.consume_content;
                imageDescResId = R.string.consume_content_desc;
                imageTitleColorResId = R.color.darkgreen;
                break;
            case 2:
                imageResId = R.drawable.login_take_test;
                imageTitleResId = R.string.take_test;
                imageDescResId = R.string.take_test_desc;
                imageTitleColorResId = R.color.darkred;
                break;
            case 3:
                imageResId = R.drawable.login_analytics;
                imageTitleResId = R.string.view_analytics;
                imageDescResId = R.string.view_analytics_desc;
                imageTitleColorResId = R.color.blue;
                break;
            default:
                break;
            }
        }*/ else if (currentPos == 1) {
            imageResId = R.drawable.logo_for_acme;
            initLoginPortion = true;
            layoutResId = R.layout.fragment_landing_pages_last_page;

        }
        fragmentRootView = inflater.inflate(layoutResId, container, false);
        if (imageResId != 0) {// ==> this page contains image
            fragmentRootView.findViewById(R.id.login_page_image).setBackgroundResource(imageResId);
        }

        if (imageTitleResId != 0) {
            TextView titleView = (TextView) fragmentRootView.findViewById(R.id.image_title);
            titleView.setText(imageTitleResId);
            titleView.setTextColor(getResources().getColor(imageTitleColorResId));
            ((TextView) fragmentRootView.findViewById(R.id.image_desc)).setText(imageDescResId);
        }

        return fragmentRootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (initLoginPortion) {
            orgDataManager = new OrgDataManager(m_cObjNhanceBaseActivity.getApplicationContext());
            sessionManager = SessionManager.getInstance(m_cObjNhanceBaseActivity);
            List<Organization> orgs = orgDataManager.getOrganizations();
            String cmdsUrl = SessionManager.getCMDSUrlForWhiteLabeledOrg();
            URL url;
            String slug = null;
            Log.d(TAG, "Making a white labeled apk for cmds url : " + cmdsUrl);
            try {
                url = new URL(cmdsUrl);
                cmdsUrl = StringUtils.substringBefore(url.toString(), url.getPath());

                slug = StringUtils.substringBetween(url.getPath().replace("org/", ""), File.separator);
                String memberId = StringUtils.substringAfterLast(url.getPath(), File.separator);
                if (slug == null && memberId != null) {
                    slug = memberId;
                }

            } catch (MalformedURLException e) {
                Log.e(TAG, e.getMessage(), e);
                Toast.makeText(m_cObjNhanceBaseActivity, R.string.error_invalid_cmds_url,
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (!SessionManager.isOnline()
                    && ((!orgs.isEmpty() && !orgs.get(0).slug.equalsIgnoreCase(slug)) || orgs
                            .isEmpty())) {
                showNoOrgNoInternet();
            } else {
                // bypassing by adding org at the start of the application
                final Map<String, Object> params = new HashMap<String, Object>();
                params.put("orgCmdsURL", SessionManager.getCMDSUrlForWhiteLabeledOrg());

                Organization org = orgDataManager.getOrganization(null, cmdsUrl, slug);
                if (org == null) {
                    Log.d(TAG, "org with cmds url : " + cmdsUrl
                            + " not added, so adding it and getting its info");
                    if (!SessionManager.isOnline()) {
                        Log.d(TAG, "Internet connection is not available ");
                        Toast.makeText(m_cObjNhanceBaseActivity.getApplicationContext(),
                                R.string.no_internet_msg, Toast.LENGTH_LONG).show();
                        return;
                    }
                    fragmentRootView.findViewById(R.id.login_fetching_org_screen_layout)
                            .setVisibility(View.VISIBLE);
                    addOrgReq = new GetOrgInfo(params, slug);
                    addOrgReq.execute(CMDSUrlFactory.INSTANCE.getCMDSUrl(cmdsUrl, "getOrgInfo"));
                } else {
                    // go to login screen directly
                    Log.d(TAG, "org with cmds url : " + cmdsUrl
                            + " added, so taking to login screen directly");
                    sessionManager.createOrgSession(org);
                    showButtons(org);
                }
            }
        }        
    }

    private void showNoOrgNoInternet() {

        m_cObjNhanceBaseActivity.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        hideAllContainers();
        fragmentRootView.findViewById(R.id.login_no_internet_layout).setVisibility(View.VISIBLE);

        Button networkSetting = (Button) fragmentRootView.findViewById(R.id.open_network_setting);
        networkSetting.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Toast.makeText(m_cObjNhanceBaseActivity, "Opening network setting", Toast.LENGTH_SHORT)
                        .show();
                startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
            }
        });
    }

    private void hideAllContainers() {

        fragmentRootView.findViewById(R.id.landing_page_buttons_layout).setVisibility(View.GONE);
        fragmentRootView.findViewById(R.id.login_no_internet_layout).setVisibility(View.GONE);
        fragmentRootView.findViewById(R.id.login_fetching_org_screen_layout).setVisibility(
                View.GONE);
    }

    private void showButtons(Organization org) {

        Log.d(TAG, "===== showing buttons in last landing page==== ");
        activeOrg = org;
        hideAllContainers();
        ViewGroup buttonsContainer = (ViewGroup) fragmentRootView
                .findViewById(R.id.landing_page_buttons_layout);
        buttonsContainer.setVisibility(View.VISIBLE);
        buttonsContainer.findViewById(R.id.take_me_to_login_page).setOnClickListener(this);
        buttonsContainer.findViewById(R.id.take_me_to_signup_page).setOnClickListener(this);
    }

    WebCommunicatorTask orgSignupDetailsReq;

    @Override
    public void onClick(View v) {

        final Intent intent = new Intent(m_cObjNhanceBaseActivity, SignupAndLoginActivity.class);
        if (activeOrg != null) {
            URL url;
            try {
                url = new URL(SessionManager.getCMDSUrlForWhiteLabeledOrg());
                String cmdsUrl = StringUtils.substringBefore(url.toString(), url.getPath());
                intent.putExtra(ConstantGlobal.ORG_ID, activeOrg.orgId);
                intent.putExtra(ConstantGlobal.CMDS_URL, cmdsUrl);
            } catch (MalformedURLException e) {
                Log.e(TAG, e.getMessage(), e);
                Toast.makeText(m_cObjNhanceBaseActivity, R.string.error_invalid_cmds_url,
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (v == fragmentRootView.findViewById(R.id.take_me_to_signup_page)) {
            if (!SessionManager.isOnline()) {
                Toast.makeText(m_cObjNhanceBaseActivity,
                        getResources().getString(R.string.connect_to_internet_msg),
                        Toast.LENGTH_SHORT).show();
            }
            final ProgressDialog dialog = new ProgressDialog(m_cObjNhanceBaseActivity);
            dialog.setMessage("Loading Signup form...");
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.cancel();

                        }
                    });
            dialog.show();
            orgSignupDetailsReq = new WebCommunicatorTask(
                    SessionManager.getInstance(m_cObjNhanceBaseActivity.getApplicationContext()),
                    null, ReqAction.GET_ORG_MEMBER_SIGN_UP_EXTRA_INPUT_FIELDS,
                    new ITaskProcessor<JSONObject>() {

                        @Override
                        public void onTaskStart(JSONObject result) {

                        }

                        @Override
                        public void onTaskPostExecute(boolean success, JSONObject result) {

                            dialog.cancel();
                            if (!success) {
                                String errorMessage = getResources().getString(
                                        R.string.error_general);
                                String errorCode = JSONUtils.getString(result,
                                        VedantuWebUtils.KEY_ERROR_CODE);
                                if (StringUtils.isNotEmpty(errorCode)
                                        && StringUtils
                                                .equals(errorCode, "ORG_SIGNUP_NOT_SUPPORTED")) {
                                    errorMessage = getResources().getString(
                                            R.string.error_org_singup_not_supported);
                                }
                                Toast.makeText(m_cObjNhanceBaseActivity, errorMessage,
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }
                            intent.putExtra(VedantuWebUtils.KEY_RESULT, result.toString());
                            intent.putExtra("ENTRY_TYPE", "SIGN_UP");
                            startActivity(intent);
                            m_cObjNhanceBaseActivity.finish();
                        }
                    }, null, null, 0);
            orgSignupDetailsReq.addExtraParams(ConstantGlobal.ORG_ID, activeOrg.orgId);
            orgSignupDetailsReq.addExtraParams("checkIfSignupAllowed", true);
            orgSignupDetailsReq.addExtraParams("targetOrgMemberProfile", "STUDENT");
            orgSignupDetailsReq.executeTask(false);
        } else {
            intent.putExtra("ENTRY_TYPE", "LOGIN");
            startActivity(intent);
            m_cObjNhanceBaseActivity.finish();
        }
    }

    private class GetOrgInfo extends AbstractVedantuJSONAsyncTask {

        private String slug;

        public GetOrgInfo(Map<String, Object> httpParams, String slug) {

            super(SessionManager.getInstance(m_cObjNhanceBaseActivity.getApplicationContext()), null,
                    httpParams);
            this.slug = slug;
            if (this.slug != null) {
                httpParams.put("slug", this.slug);
            }
        }

        @Override
        protected JSONObject doInBackground(String... params) {

            this.url = params[0];
            httpParams.put("getKey", true);
            Log.d(TAG, "url : " + url + ", httpParams:" + httpParams);

            JSONObject json = null;
            try {
                json = VedantuWebUtils.getJSONData(url, VedantuWebUtils.GET_REQ, httpParams);

                Log.e("jsonresult","jsonresult"+json);




            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
            if (isCancelled()) {
                return json;
            }
            return json;
        }

        @Override
        protected void onPostExecute(JSONObject result) {

            super.onPostExecute(result);
            Log.d(TAG, "adding org info to database");
            boolean error = checkForError(result);
            if (error) {
                String errorMsg = JSONUtils.getString(result, "errorMessage");
                if (SessionManager.isOnline()) {
                    Toast.makeText(
                            m_cObjNhanceBaseActivity,
                            TextUtils.isEmpty(errorMsg) ? "Unknown error occurred.Close the application and try again"
                                    : errorMsg, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(m_cObjNhanceBaseActivity, R.string.no_internet_msg,
                            Toast.LENGTH_SHORT).show();
                }

                return;
            }
            try {
                if(result != null) {
                    result = result.getJSONObject(VedantuWebUtils.KEY_RESULT);
                    Organization org = addOrganization(result);
                    sessionManager.createOrgSession(org);
                    JSONObject orgProfileJSON = JSONUtils.getJSONObject(result, "orgProfile");
                    UserDataManager userDataManager = new UserDataManager(m_cObjNhanceBaseActivity
                            .getApplicationContext());
                    Log.d(TAG, "successfully saved org info in local database : " + org);
                    if (org.autoLogin) {
                        Log.i(TAG, "org[" + org.name + "] is configured as autoLogin, login user now");
                        JSONObject loginResponse = LoginUtils.loginUser(org, orgProfileJSON,
                                JSONUtils.getString(JSONUtils.getJSONObject(orgProfileJSON, "info"),
                                        "memberId"), StringUtils.EMPTY, org.autoLogin, userDataManager,
                                m_cObjNhanceBaseActivity.getBaseContext());
                        if (loginResponse != null) {
                            LoginUtils.syncLibrary(m_cObjNhanceBaseActivity);
                        }
                    }
                }
            } catch (Throwable e) {
                Log.e(TAG, e.getMessage(), e);
                Toast.makeText(m_cObjNhanceBaseActivity, R.string.error_unknown, Toast.LENGTH_SHORT)
                        .show();
                return;
            }
            onResume();
        }

        @Override
        public void clear() {

        }
    }

    @SuppressLint("NewApi")
    private Organization addOrganization(JSONObject result) throws Exception {

        if (TextUtils.isEmpty(result.getString(ConstantGlobal.CMDS_URL))) {
            Toast.makeText(m_cObjNhanceBaseActivity, "incomplete information received from server",
                    Toast.LENGTH_SHORT).show();
            throw new Exception("incomplete information received from server");
        }
        Organization org = new Organization(result.getString(ConstantGlobal.NAME),
                result.getString(ConstantGlobal.FULL_NAME), result.getString(ConstantGlobal.ID),
                result.getString("orgThumbnail"), result.getString(ConstantGlobal.DESCRIPTION),
                result.getString(ConstantGlobal.CMDS_URL), JSONUtils.getString(result, "slug"),
                JSONUtils.getString(result, ConstantGlobal.AUTH_TYPE));
        org.website = result.getString("website");
        org.contactNo = result.getString("contactNumber");
        // org.key = com.android.vedantu.dlp.utils.StringUtils.parseHexBinary(result
        // .getString(ConstantGlobal.KEY));
        org.key = Base64.decode(JSONUtils.getString(result, ConstantGlobal.KEY), Base64.DEFAULT);
        JSONObject orgProfileJSON = JSONUtils.getJSONObject(result, "orgProfile");
        if (orgProfileJSON != null && orgProfileJSON.length() > 0) {
            org.autoLogin = true;
        }
        orgDataManager.insertOrganization(org);
        return org;
    }

    @Override
    public void onStop() {

        super.onStop();
        if (addOrgReq != null) {
            addOrgReq.cancel(true);
        }
        if (orgSignupDetailsReq != null) {
            orgSignupDetailsReq.cancel(true);
        }
    }

}