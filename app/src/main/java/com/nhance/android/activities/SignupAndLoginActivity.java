package com.nhance.android.activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.nhance.android.activities.baseclasses.NhanceBaseActivity;
import com.nhance.android.async.tasks.AbstractVedantuJSONAsyncTask;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.OrgDataManager;
import com.nhance.android.db.datamanagers.UserDataManager;
import com.nhance.android.db.models.Organization;
import com.nhance.android.db.models.UserOrgProfile;
import com.nhance.android.db.models.entity.User;
import com.nhance.android.factory.CMDSUrlFactory;
import com.nhance.android.fragments.SignupDatePickerDialogFragment;
import com.nhance.android.interfaces.ILoginPageListner;
import com.nhance.android.login.utils.LoginUtils;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.utils.JSONUtils;
import com.nhance.android.utils.VedantuEncrypter;
import com.nhance.android.utils.VedantuWebUtils;
import com.nhance.android.R;

public class SignupAndLoginActivity extends NhanceBaseActivity implements OnDateSetListener,
        ILoginPageListner {

    private static final String TAG = "SignupAndLoginActivity";
    private UserDataManager userDataManager = null;
    private List<String> allowedProfiles = Arrays.asList("STUDENT", "OFFLINE_USER",
            "TEACHER", "MANAGER");
    private Organization activeOrg = null;
    private OrgDataManager orgDataManager = null;
    private String entryType = "LOGIN";
    private OnClickListener dateFieldClickListner = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            selecteDateTextView = (TextView) v;
            SignupDatePickerDialogFragment dateDialog = new SignupDatePickerDialogFragment();
            dateDialog.show(
                    getSupportFragmentManager(),
                    null);
        }
    };
    private Spinner spinnerView;
    private Spinner genderSpinner;
    private JSONObject info;
    private String userState;

    private enum FieldType {
        DECIMAL, WHOLE_NUMBER, EMAIL, TEXT, DATE, PHONE_NO;

        public static FieldType valueOfKey(String key) {

            FieldType fieldType = TEXT;
            try {
                fieldType = valueOf(StringUtils.upperCase(key.trim()));
            } catch (Exception e) {
            }
            return fieldType;
        }
    }

    private enum ValidationType {
        LIST, VALUE, REGEX;

        public static ValidationType valueOfKey(String key) {

            ValidationType validationType = VALUE;
            try {
                validationType = valueOf(StringUtils.upperCase(key.trim()));
            } catch (Exception e) {
            }
            return validationType;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        userDataManager = new UserDataManager(getApplicationContext());
        orgDataManager = new OrgDataManager(getApplicationContext());
        String orgId = getIntent().getStringExtra(ConstantGlobal.ORG_ID);
        String cmdsUrl = getIntent().getStringExtra(ConstantGlobal.CMDS_URL);
        String token = FirebaseInstanceId.getInstance().getToken();
        activeOrg = orgDataManager.getOrganization(orgId, cmdsUrl);
        Log.d(TAG, "Org Info==================== " + orgId);
        if (activeOrg == null) {
            Toast.makeText(this, getResources().getString(R.string.error_general),
                    Toast.LENGTH_SHORT).show();
            finish();
        }
        getSupportActionBar().hide();
        entryType = getIntent().getStringExtra("ENTRY_TYPE");
        if (StringUtils.equals(entryType, "LOGIN")) {
            setContentView(R.layout.login_inputs_page);
            setUpLoginInputsPage();
        } else if (StringUtils.equals(entryType, "SIGN_UP")) {
            setContentView(R.layout.signup_inputs_page);
            genderSpinner = (Spinner) findViewById(R.id.signup_gender);
            ArrayAdapter<String> spinnerAdapter = new SignUpSpinnerAdapter(this,
                    android.R.layout.simple_list_item_1, Arrays.asList(getResources().getStringArray(
                    R.array.gender)));
            genderSpinner.setAdapter(spinnerAdapter);
            //  spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            String resultStr = getIntent().getStringExtra(VedantuWebUtils.KEY_RESULT);
            JSONObject result = null;
            try {
                result = new JSONObject(resultStr);
            } catch (JSONException e) {
                Log.d(TAG, e.getLocalizedMessage());
            }
            if (result != null) {
                setUpSignupInputsPage(result);
                setClickListnersForSignupPage();
            } else {
                Toast.makeText(this, getResources().getString(R.string.error_general),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        View cBoxView = findViewById(R.id.login_page_check_box);
        if (cBoxView != null) {
            ((CheckBox) cBoxView)
                    .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                            EditText passView = (EditText) findViewById(R.id.login_password);

                            int inputType = isChecked ? InputType.TYPE_CLASS_TEXT
                                    : (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            Log.d(TAG, "isChecked: " + isChecked + ", inputType:" + inputType);
                            passView.setInputType(inputType);
                        }
                    });
        }



    findViewById(R.id.back_to_landing_pages).

    setOnClickListener(new View.OnClickListener() {

        @Override
        public void onClick (View v){

            onBackPressed();

        }
    });
}

    private TextView selecteDateTextView;

    private void setUpSignupInputsPage(JSONObject result) {

        JSONArray fields = JSONUtils.getJSONArray(
                JSONUtils.getJSONObject(result, VedantuWebUtils.KEY_RESULT), "fields");
        if (fields.length() > 0) {
            LinearLayout fieldsHolder = (LinearLayout) findViewById(R.id.signup_extra_fields_holder);
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            for (int k = 0; k < fields.length(); k++) {
                try {
                    JSONObject field = fields.getJSONObject(k);
                    String fieldName = JSONUtils.getString(field, ConstantGlobal.NAME);
                    boolean isFieldRequired = JSONUtils.getBoolean(field, "required", false);
                    String hint = fieldName;
                    if (isFieldRequired) {
                        hint = fieldName + "*";
                    }
                    if (StringUtils.isNotEmpty(JSONUtils.getString(field,
                            ConstantGlobal.PLACE_HOLDER))) {
                        hint += " (" + JSONUtils.getString(field, ConstantGlobal.PLACE_HOLDER)
                                + ")";
                    }
                    Map<String, Object> fieldMap = new HashMap<String, Object>();
                    List<String> valueSet = JSONUtils.getList(field, ConstantGlobal.VALUE_SET);
                    FieldType fieldType = FieldType.valueOfKey(JSONUtils.getString(field,
                            ConstantGlobal.FIELD_TYPE));
                    ValidationType validationType = ValidationType.valueOfKey(JSONUtils.getString(
                            field, ConstantGlobal.VALIDATION_TYPE));
                    fieldMap.put(ConstantGlobal.NAME,
                            JSONUtils.getString(field, ConstantGlobal.NAME));
                    fieldMap.put("required", isFieldRequired);
                    fieldMap.put(ConstantGlobal.VALUE_SET, valueSet);
                    fieldMap.put(ConstantGlobal.FIELD_TYPE, fieldType);
                    fieldMap.put(ConstantGlobal.VALIDATION_TYPE, validationType);
                    View fieldView;
                    if (FieldType.TEXT.equals(fieldType) && valueSet.size() > 0
                            && ValidationType.LIST.equals(validationType)) {
                        // for drop down
                        fieldView = inflater.inflate(R.layout.spinner_signup, fieldsHolder, false);
                         spinnerView = (Spinner) fieldView;
                        valueSet.add(0, hint);
                        ArrayAdapter<String> spinnerAdapter = new SignUpSpinnerAdapter(this,
                                android.R.layout.simple_list_item_1, valueSet);
                        spinnerView.setAdapter(spinnerAdapter);
                    //    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        String selectedValue = JSONUtils.getString(field,
                                ConstantGlobal.DEFAULT_VALUE);
                        if (StringUtils.isNotEmpty(selectedValue)) {
                            int positionSelected = 0;
                            for (int i = 0; i < valueSet.size(); i++) {
                                if (StringUtils.equals(valueSet.get(i), selectedValue)) {
                                    positionSelected = i;
                                    break;
                                }
                            }
                            spinnerView.setSelection(
                                    positionSelected);
                        }
                    } else if (FieldType.DATE.equals(fieldType)) {
                        // for date
                        fieldView = inflater.inflate(R.layout.textview_signup, fieldsHolder, false);
                        fieldView.setOnClickListener(dateFieldClickListner);
                        ((TextView) fieldView).setText(hint);
                    } else {
                        // this the default,
                        // phone no,decimal,whole number,email and normal text fields,regex come
                        // here
                        fieldView = inflater.inflate(R.layout.signup_input_field, fieldsHolder,
                                false);
                        EditText inputFieldView = (EditText) fieldView;
                        inputFieldView.setHint(hint);
                        if (FieldType.PHONE_NO.equals(fieldType)) {
                            inputFieldView.setInputType(InputType.TYPE_CLASS_PHONE);
                        }
                    }
                    fieldView.setTag(fieldMap);
                    fieldsHolder.addView(fieldView);
                } catch (JSONException e) {
                    Log.d(TAG, e.getLocalizedMessage());
                }
            }
        }
    }

    private void setUpLoginInputsPage() {

        findViewById(R.id.login_submit_button).setOnClickListener(getLoginButtonClickListner());
    }

    private void setClickListnersForSignupPage() {

        findViewById(R.id.signup_dob).setOnClickListener(dateFieldClickListner);

        findViewById(R.id.sign_up_button).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                if (!SessionManager.isOnline()) {
                    Toast.makeText(SignupAndLoginActivity.this,
                            getResources().getString(R.string.connect_to_network_msg),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                String token = FirebaseInstanceId.getInstance().getToken();
                EditText fnameView = (EditText) findViewById(R.id.signup_fname);
                EditText lnameView = (EditText) findViewById(R.id.signup_lname);
                EditText passwordView = (EditText) findViewById(R.id.login_password);
                TextView dobView = (TextView) findViewById(R.id.signup_dob);
                View selectedGenderView = ((Spinner) findViewById(R.id.signup_gender))
                        .getSelectedView();

                String fname = fnameView.getText().toString().trim();
                String password = passwordView.getText().toString().trim();
                String lname = lnameView.getText().toString().trim();
                String dob = dobView.getText().toString().trim();
                String gender = genderSpinner.getSelectedItem().toString();
                String email = ((EditText) findViewById(R.id.signup_emailid)).getText().toString()
                        .trim();

                LinearLayout fieldsHolder = (LinearLayout) findViewById(R.id.signup_extra_fields_holder);
                boolean showError = false;
                String errorMessage = "";
                List<Map<String, String>> extraInfo = new ArrayList<Map<String, String>>();
                for (int k = 0; k < fieldsHolder.getChildCount(); k++) {
                    View fieldView = fieldsHolder.getChildAt(k);
                    @SuppressWarnings("unchecked")
                    Map<String, Object> fieldMap = ((Map<String, Object>) fieldView.getTag());
                    String fieldValue = "";
                    FieldType fieldType = (FieldType) fieldMap.get(ConstantGlobal.FIELD_TYPE);
                    ValidationType validationType = (ValidationType) fieldMap
                            .get(ConstantGlobal.VALIDATION_TYPE);
                    String fieldName = (String) fieldMap.get(ConstantGlobal.NAME);
                    if (FieldType.TEXT.equals(fieldType)
                            && ValidationType.LIST.equals(validationType)) {
                        Spinner spinnerView = (Spinner) fieldView;
                        if (spinnerView.getSelectedItemPosition() > 0) {
                            View selectedSpinnerItemView = spinnerView.getSelectedView();
                            fieldValue = ((TextView) selectedSpinnerItemView
                                    .findViewById(R.id.signup_spinner_item_text)).getText()
                                    .toString().trim();
                        }
                    } else if (fieldType.equals(FieldType.DATE)) {
                        if (StringUtils.isNotEmpty(((TextView) fieldView).getText().toString()
                                .trim())) {
                            fieldValue = ((TextView) fieldView).getText().toString().trim();
                        }
                    } else {
                        fieldValue = ((EditText) fieldView).getText().toString().trim();
                        if (StringUtils.isNotEmpty(fieldValue)) {
                            switch (fieldType) {
                            case DECIMAL:
                                try {
                                    Float.parseFloat(fieldValue);
                                } catch (Exception e) {
                                    errorMessage = "Enter a decimal value for " + fieldName;
                                }
                                break;
                            case WHOLE_NUMBER:
                                try {
                                    Integer.parseInt(fieldValue);
                                } catch (NumberFormatException nfe) {
                                    errorMessage = "Enter a numeric value for " + fieldName;
                                }
                                break;
                            case EMAIL:
                                if (!Patterns.EMAIL_ADDRESS.matcher(fieldValue).matches()) {
                                    errorMessage = "Enter an Email ID for " + fieldName;
                                }
                                break;
                            case PHONE_NO:
                                if (!Pattern
                                        .matches(
                                                "^((\\+[1-9]{1,4}[ \\-]*)|(\\([0-9]{2,3}\\)[ \\-]*)|([0-9]{2,4})[ \\-]*)*?[0-9]{3,4}?[ \\-]*[0-9]{3,4}?$",
                                                fieldValue)
                                        || fieldValue.length() < 8) {
                                    errorMessage = "Enter an valid phone number for " + fieldName;
                                }
                                break;
                            case TEXT:
                                @SuppressWarnings("unchecked")
                                List<String> valueSet = (List<String>) fieldMap
                                        .get(ConstantGlobal.VALUE_SET);
                                if (valueSet.size() > 0
                                        && ValidationType.REGEX.equals(validationType)) {
                                    String regex = valueSet.get(0);
                                    try {
                                        if (!Pattern.matches(regex, fieldValue)) {
                                            errorMessage = "Enter proper value for " + fieldName;
                                        }
                                    } catch (Exception e) {
                                        Log.e(TAG, "Problem in validating the value against regex"
                                                + regex);
                                        errorMessage = "Problem in validating the field "
                                                + fieldName;

                                    }
                                }
                                break;
                            default:
                                break;
                            }
                            if (StringUtils.isNotEmpty(errorMessage)) {
                                break;
                            }
                        }
                    }
                    boolean isFieldRequired = (Boolean) fieldMap.get("required");
                    if (isFieldRequired && TextUtils.isEmpty(fieldValue)) {
                        showError = true;
                        break;
                    }
                    if (StringUtils.isNotEmpty(fieldValue)) {
                        Map<String, String> fieldMapForPost = new HashMap<String, String>();
                        fieldMapForPost.put(ConstantGlobal.NAME, fieldName);
                        fieldMapForPost.put("value", fieldValue);
                        extraInfo.add(fieldMapForPost);
                    }
                }
                Log.d(TAG, "Error message is " + errorMessage);
                boolean dobSet = false;
                if (StringUtils.isNotEmpty(dobView.getText().toString())) {
                    dobSet = true;
                }

                ScrollView mainPageLayout = (ScrollView) findViewById(R.id.signup_page_main_layout);
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                View focusedView = mainPageLayout.findFocus();
                if (focusedView != null) {
                    imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
                }

                if (TextUtils.isEmpty(fname) || TextUtils.isEmpty(password) || !dobSet || showError) {
                    errorMessage = "Field marked with * can not be empty";
                } else if (password.length() < 5) {
                    errorMessage = "Password should have atleast 5 characters";
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    errorMessage = "Enter a valid email address";
                }

                if (!TextUtils.isEmpty(errorMessage)) {
                    Toast.makeText(getBaseContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    return;
                }

                findViewById(R.id.sign_up_button).setVisibility(View.GONE);
                findViewById(R.id.signup_page_main_layout).setVisibility(View.GONE);
                findViewById(R.id.signup_progressBar_container).setVisibility(View.VISIBLE);

                final Map<String, Object> httpParams = new HashMap<String, Object>();
                // TODO encrypt password
                httpParams.put(ConstantGlobal.ORG_ID, activeOrg.orgId);
                httpParams.put(ConstantGlobal.FIRST_NAME, fname);
                httpParams.put(ConstantGlobal.LAST_NAME, lname);
                httpParams.put("dob", dob);
                httpParams.put("gender", gender);
                httpParams.put("email", email);
                httpParams.put(ConstantGlobal.PUSH_ID, token);
                httpParams.put(ConstantGlobal.PASSWORD, password);

                for (int i = 0; i < extraInfo.size(); i++) {
                    String keyPrefix = "extraInfo[" + i + "].";
                    for (Entry<String, String> entry : extraInfo.get(i).entrySet()) {
                        httpParams.put(keyPrefix + entry.getKey(), entry.getValue());
                    }
                }

                httpParams.put("getKey", true);
                httpParams.put("profile", "STUDENT");
                // TODO hard coding callingUserId and userId
                httpParams.put("callingUserId", "MOBILE_SIGNUP");
                httpParams.put("userId", "MOBILE_SIGNUP");
                SignUpTask lTask = new SignUpTask(httpParams);

                Log.e("signup","signup"+httpParams);
                lTask.executeTask(true,
                        CMDSUrlFactory.INSTANCE.getCMDSUrl(activeOrg.cmdsUrl, "addOrgMember"));
            }
        });
    }

    private OnClickListener getLoginButtonClickListner() {

        return new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String token = FirebaseInstanceId.getInstance().getToken();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                EditText loginIdView = (EditText) findViewById(R.id.login_id);
                EditText passwordView = (EditText) findViewById(R.id.login_password);
                View focusedView = findViewById(R.id.login_inputs_layout).findFocus();
                if (focusedView != null) {
                    imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
                }
                String loginId = loginIdView.getText().toString().trim();
                String password = passwordView.getText().toString().trim();
                if (TextUtils.isEmpty(loginId) || TextUtils.isEmpty(password)) {
                    Toast.makeText(getBaseContext(), " Login Id  and Password can not be empty",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                final Map<String, Object> httpParams = new HashMap<String, Object>();
                httpParams.put(ConstantGlobal.ORG_ID, activeOrg.orgId);
                httpParams.put(ConstantGlobal.MEMBER_ID, loginId);
                httpParams.put(ConstantGlobal.USERNAME, loginId);
                httpParams.put(ConstantGlobal.PASSWORD, password);
                httpParams.put(ConstantGlobal.PUSH_ID, token);
                httpParams.put("getKey", true);
                boolean extAuth = activeOrg.authType != null
                        && activeOrg.authType.equalsIgnoreCase("EXT_AUTH_ORG");

                if (!extAuth && Patterns.EMAIL_ADDRESS.matcher(loginId).matches()) {
                    httpParams.put("useGlobalUsername", true);
                    httpParams.put(ConstantGlobal.USERNAME, StringUtils.lowerCase(loginId));
                }
                hideAllLoginContainers();
                ViewGroup loginProgressBarContainer = (ViewGroup) findViewById(R.id.login_progressBar_container);
                loginProgressBarContainer.setVisibility(View.VISIBLE);
                LoginTask lTask = new LoginTask(httpParams);
                Log.e("login","login"+httpParams);
                lTask.executeTask(true, CMDSUrlFactory.INSTANCE.getCMDSUrl(activeOrg.cmdsUrl, "authenticate"));
            }
        };
    }

    class LoginTask extends AbstractVedantuJSONAsyncTask {

        boolean isOnlineLogin = false;
        String  errorMsg      = "";
        Date mExpiryDate = null;
        public LoginTask(Map<String, Object> httpParams) {

            super(null, null, httpParams);

        }

        @Override
        protected JSONObject doInBackground(String... params) {

            User user = null;
            UserOrgProfile userOrgProfile = null;

            JSONObject json = null;
            try {
                if (SessionManager.isOnline()) {
                    json = VedantuWebUtils.getJSONData(params[0], VedantuWebUtils.POST_REQ,
                            httpParams);
                }
            } catch (IOException e1) {
                Log.e(TAG, e1.getMessage());
            }

            error = VedantuWebUtils.checkErrorMsg(json, params[0]);
            if (SessionManager.isOnline() && !error) {
                isOnlineLogin = true;
                json = JSONUtils.getJSONObject(json, VedantuWebUtils.KEY_RESULT);

                // store the user credentials locally for verifying off-line authentication
                JSONObject orgProfileJSON = JSONUtils.getJSONObject(json, "orgProfile");
                if (orgProfileJSON == null) {
                    error = true;
                    Log.e(TAG,
                            "no org profile received from server for mermberId: "
                                    + httpParams.get(ConstantGlobal.USERNAME));
                    return null;
                }
                String profile = JSONUtils.getString(
                        JSONUtils.getJSONObject(orgProfileJSON, ConstantGlobal.INFO), "profile");
                if (!allowedProfiles.contains(profile)) {
                    error = true;
                    Log.e(TAG, "profile[" + profile + "] not allowed to login allowed set : "
                            + allowedProfiles);
                    errorMsg = "Only Students are allowed to login";
                    return null;
                }

                //TODO get the expari date as "endTime"
                JSONObject lInfoJson = JSONUtils.getJSONObject(orgProfileJSON, "info");
                Log.e("lInfoJson", ".."+lInfoJson);
                userState = JSONUtils.getString(lInfoJson, "userState");
                JSONObject lMappingJson = JSONUtils.getJSONObject(lInfoJson, "mappings");
                JSONArray programsJsonArry = JSONUtils.getJSONArray(lMappingJson, "programs");
                if(null != programsJsonArry) {
                    for (int i = 0; i < programsJsonArry.length(); i++) {
                        try {
                            JSONObject lProgramJSON = programsJsonArry.getJSONObject(i);
                            long lDate = lProgramJSON.getLong("endTime");
                            mExpiryDate = new Date(lDate);
                            break;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // notify the ui for successful login and update ui for synchronisation of
                // library

                publishProgress();
                if(userState != null && !userState.equalsIgnoreCase("BLOCKED")){
                    try {
                        LoginUtils.loginUser(activeOrg, orgProfileJSON,
                                (String) httpParams.get(ConstantGlobal.USERNAME),
                                (String) httpParams.get(ConstantGlobal.PASSWORD), false,
                                userDataManager, getBaseContext());
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage(), e);
                        return null;
                    }
                }
            } else if (!SessionManager.isOnline()) {
                user = userDataManager.getUser((String) httpParams.get(ConstantGlobal.USERNAME),
                        VedantuEncrypter.INSTANCE.md5((String) httpParams
                                .get(ConstantGlobal.PASSWORD)));
                error = user == null;
                if (!error && (userState != null && !userState.equalsIgnoreCase("BLOCKED"))) {
                    Log.e("error", "..."+error);
                    user.lastLogin = String.valueOf(System.currentTimeMillis());
                    userOrgProfile = userDataManager
                            .getUserOrgProfile(user.userId, activeOrg.orgId);
                    if (userOrgProfile != null
                            && allowedProfiles.contains(JSONUtils.getString(JSONUtils
                                    .getJSONObject(userOrgProfile.orgProfile, ConstantGlobal.INFO),
                                    "profile"))) {
                        userDataManager.updateUser(user);
                        publishProgress();
                        LoginUtils.createUserSession(user, userOrgProfile, activeOrg,
                                userDataManager, getBaseContext());
                    } else {
                        error = true;
                        errorMsg = "Only Students are allowed to login";
                        return null;
                    }

                } else {
                    // added by Shankar--> this msg is required as we have included hashing for
                    // local password storage
                    errorMsg = "Invalid credentials, please try after connecting to the Internet";
                }
            }
            return json;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {

            super.onProgressUpdate(values);

            Log.d(TAG, "Successfully logined");
//            Toast.makeText(getBaseContext(), "Login Successful", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(JSONObject result) {

            super.onPostExecute(result);
            if (error) {
                String errorCode = null;
                try {
                    if (result != null && result.has(VedantuWebUtils.KEY_ERROR_CODE)
                            && !TextUtils.isEmpty(result.getString(VedantuWebUtils.KEY_ERROR_CODE))) {
                        errorCode = result.getString(VedantuWebUtils.KEY_ERROR_CODE);
                    }
                } catch (JSONException e) {
                    Log.d(TAG, e.getLocalizedMessage());
                }
                if(errorCode != null && errorCode.equalsIgnoreCase("MEMBER_IS_BLOCKED")){
                    Log.d(TAG, "member blocked: " + result);
                    String errorMsg = "Sorry, your account is currently De-activated. Please contact your Organisation Administrator.";
                    if (!TextUtils.isEmpty(this.errorMsg)) {
                        errorMsg = this.errorMsg;
                    }
                    Toast.makeText(getBaseContext(), errorMsg, Toast.LENGTH_LONG).show();
                    hideAllLoginContainers();
                    ViewGroup loginContainer = (ViewGroup) findViewById(R.id.login_inputs_layout);
                    loginContainer.setVisibility(View.VISIBLE);

                }else {
                    Log.d(TAG, "logined failed : " + result);
                    String errorMsg = isOnlineLogin ? "Login Failed. Login Id and Password do not match"
                            : "Login Id and Password does not matched or if this is your first login. Please login online";
                    if (!TextUtils.isEmpty(this.errorMsg)) {
                        errorMsg = this.errorMsg;
                    }
                    Toast toast = Toast.makeText(getBaseContext(), errorMsg, Toast.LENGTH_LONG);
                    ViewGroup group = (ViewGroup) toast.getView();
                    TextView messageTextView = (TextView) group.getChildAt(0);
                    group.setPadding((int) getResources().getDimension(R.dimen.toast_padding),(int) getResources().getDimension(R.dimen.toast_padding),(int) getResources().getDimension(R.dimen.toast_padding),(int) getResources().getDimension(R.dimen.toast_padding));
                    messageTextView.setTextSize((int) getResources().getDimension(R.dimen.toast_text_size));
                    toast.show();
                    hideAllLoginContainers();
                    ViewGroup loginContainer = (ViewGroup) findViewById(R.id.login_inputs_layout);
                    loginContainer.setVisibility(View.VISIBLE);
                }
            } else if(userState != null && userState.equalsIgnoreCase("BLOCKED")){
                       Log.e("userState","..."+userState);
                       String errorMsg = "Sorry, your account is currently De-activated. Please contact your Organisation Administrator.";
                       Toast toast = Toast.makeText(getBaseContext(), errorMsg, Toast.LENGTH_LONG);
                 ViewGroup group = (ViewGroup) toast.getView();
                 group.setPadding((int) getResources().getDimension(R.dimen.toast_padding),(int) getResources().getDimension(R.dimen.toast_padding),(int) getResources().getDimension(R.dimen.toast_padding),(int) getResources().getDimension(R.dimen.toast_padding));
                 TextView messageTextView = (TextView) group.getChildAt(0);
                 messageTextView.setTextSize((int) getResources().getDimension(R.dimen.toast_text_size));
                          toast.show();
                       Log.e("userState","..."+userState);
                hideAllLoginContainers();
                ViewGroup loginContainer = (ViewGroup) findViewById(R.id.login_inputs_layout);
                loginContainer.setVisibility(View.VISIBLE);

            }else{
                //TODO For testing Expiry Date Hardcoded, Need to get Expiry date from server save in SharedPrefarence
//                Date lExpDate = ConstantGlobal.getDate("2017-04-30 23:59:59", ConstantGlobal.DEFULT_DATE_FORMAT);
                Toast.makeText(getBaseContext(), "Login Successful", Toast.LENGTH_SHORT).show();
                ConstantGlobal.setExpiryDate(SignupAndLoginActivity.this, mExpiryDate);
                LoginUtils.syncLibrary(SignupAndLoginActivity.this);
            }
        }

        @Override
        public void clear() {

        }
    }

    class SignUpTask extends AbstractVedantuJSONAsyncTask {

        public SignUpTask(Map<String, Object> httpParams) {

            super(null, null, httpParams);

        }

        @Override
        protected JSONObject doInBackground(String... params) {

            JSONObject json = null;
            try {
                if (SessionManager.isOnline()) {
                    json = VedantuWebUtils.getJSONData(params[0], VedantuWebUtils.POST_REQ,
                            httpParams);
                }
            } catch (IOException e1) {
                Log.e(TAG, e1.getMessage());
            }

            error = VedantuWebUtils.checkErrorMsg(json, params[0]);
            if (SessionManager.isOnline() && !error) {
                json = JSONUtils.getJSONObject(json, VedantuWebUtils.KEY_RESULT);

                // store the user credentials locally for verifying off-line authentication
                JSONObject orgProfileJSON = JSONUtils.getJSONObject(json, "orgProfile");
                if (orgProfileJSON == null) {
                    error = true;
                    Log.e(TAG,
                            "no org profile received from server for mermberId: "
                                    + httpParams.get(ConstantGlobal.USERNAME));
                    return null;
                }
                // String profile = JSONUtils.getString(
                // JSONUtils.getJSONObject(orgProfileJSON, ConstantGlobal.INFO), "profile");
                // if (!allowedProfiles.contains(profile)) {
                // error = true;
                // Log.e(TAG, "profile[" + profile + "] not allowed to login allowed set : "
                // + allowedProfiles);
                // errorMsg = "Only Students are allowed to login";
                // return null;
                // }
                publishProgress();
                try {
                    Log.d(TAG, "String for eamil " + httpParams.get("email"));
                    LoginUtils.loginUser(activeOrg, orgProfileJSON,
                            (String) httpParams.get("email"),
                            (String) httpParams.get(ConstantGlobal.PASSWORD), false,
                            userDataManager, getBaseContext());
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                    return null;
                }
            }
            return json;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {

            super.onProgressUpdate(values);

            Log.d(TAG, "Successfully Signed up");
            Toast.makeText(getBaseContext(), "You are successfully signed up", Toast.LENGTH_SHORT)
                    .show();
        }

        @Override
        protected void onPostExecute(JSONObject result) {




            super.onPostExecute(result);
            findViewById(R.id.sign_up_button).setVisibility(View.VISIBLE);
            findViewById(R.id.signup_page_main_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.signup_progressBar_container).setVisibility(View.GONE);
            if (error) {
                String errorCode = "";
                String errorMsg = "Some error occured. Please try again";
                try {
                    if (result != null && result.has(VedantuWebUtils.KEY_ERROR_CODE)
                            && !TextUtils.isEmpty(result.getString(VedantuWebUtils.KEY_ERROR_CODE))) {
                        errorCode = result.getString(VedantuWebUtils.KEY_ERROR_CODE);
                    }
                } catch (JSONException e) {
                    Log.d(TAG, e.getLocalizedMessage());
                }
                Log.d(TAG, errorCode + " is the error code for sign up");
                if (StringUtils.isNotEmpty(errorCode)) {
                    int resId = -1;
                    if (StringUtils.equals(errorCode, "ORG_SIGNUP_NOT_SUPPORTED")) {
                        resId = R.string.error_org_singup_not_supported;
                    } else if (StringUtils.equals(errorCode, "USER_ALREADY_EXISTS")) {
                        resId = R.string.error_user_already_exists;
                    } else if (StringUtils.equals(errorCode, "ORGANIZATION_MEMBER_ALREADY_EXISTS")) {
                        resId = R.string.error_organization_member_already_exists;
                    } else {
                        resId = R.string.error_general;
                    }
                    errorMsg = getResources().getString(resId);
                }
                Toast.makeText(getBaseContext(), errorMsg, Toast.LENGTH_LONG).show();
            } else {
                Intent intent = new Intent(SignupAndLoginActivity.this, OrgCategoriesActivity.class);
                startActivity(intent);
                finish();
            }
        }

        @Override
        public void clear() {

        }
    }

    private void hideAllLoginContainers() {

        findViewById(R.id.login_inputs_layout).setVisibility(View.GONE);
        findViewById(R.id.login_progressBar_container).setVisibility(View.GONE);
    }

    @Override
    public void onStop() {

        super.onStop();
        // TODO cancel signup request
    }

    @Override
    public void onBackPressed() {

        Intent intent = new Intent(SignupAndLoginActivity.this, LoginActivity.class);
        intent.putExtra("fromSignupAndLoginPage", true);
        startActivity(intent);
        finish();
        // super.onBackPressed();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {

        if (selecteDateTextView != null) {
            int monthToSend = month + 1;
            selecteDateTextView.setText(year + "-"
                    + ((monthToSend < 10) ? "0" + monthToSend : monthToSend) + "-"
                    + ((day < 10) ? "0" + day : day));
        }
    }

    @Override
    public void finishActivity() {

        finish();

    }

    final public class SignUpSpinnerAdapter extends ArrayAdapter<String> {

        private List<String>   items;
        private int            layoutResource;
        private LayoutInflater inflater;

        public SignUpSpinnerAdapter(Context context, int layoutResource, List<String> items) {

            super(context, layoutResource, items);
            this.items = items;
            this.layoutResource = layoutResource;
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
          /*  ((TextView) view.findViewById(R.id.signup_spinner_item_text)).setText(items
                   .get(position));*/
            return view;
        }
    }
}
