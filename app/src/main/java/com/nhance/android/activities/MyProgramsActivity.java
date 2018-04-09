package com.nhance.android.activities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.nhance.android.activities.baseclasses.NhanceBaseActivity;
import com.nhance.android.async.tasks.AccessCodeHandler;
import com.nhance.android.async.tasks.ITaskProcessor;
import com.nhance.android.async.tasks.AccessCodeHandler.OnAccessButtonClickedHandler;
import com.nhance.android.async.tasks.socials.SocialActionPosterTask;
import com.nhance.android.async.tasks.socials.WebCommunicatorTask;
import com.nhance.android.async.tasks.socials.WebCommunicatorTask.ReqAction;
import com.nhance.android.billing.payments.PaymentHandler;
import com.nhance.android.billing.payments.PaymentResult;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.customviews.CustomListView;
import com.nhance.android.db.datamanagers.OrgDataManager;
import com.nhance.android.db.datamanagers.UserDataManager;
import com.nhance.android.db.models.PendingTranscation;
import com.nhance.android.db.models.UserOrgProfile;
import com.nhance.android.enums.EntityType;
import com.nhance.android.enums.ErrorCode;
import com.nhance.android.enums.SectionAccessScope;
import com.nhance.android.enums.SectionRevenueModel;
import com.nhance.android.enums.TransactionStatus;
import com.nhance.android.fragments.MyProgramPaymentInfoDialogFragment;
import com.nhance.android.managers.LibraryManager;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.pojos.CostRate;
import com.nhance.android.pojos.OrgMemberInfo;
import com.nhance.android.pojos.ProgCenterSecInfo;
import com.nhance.android.pojos.OrgMemberMappingInfo.OrgProgramCenterSectionIds;
import com.nhance.android.pojos.OrgMemberMappingInfo.OrgSectionInfo;
import com.nhance.android.pojos.OrgMemberMappingInfo.OrgStructureBasicInfo;
import com.nhance.android.pojos.responses.StartTransactionRes;
import com.nhance.android.utils.GoogleAnalyticsUtils;
import com.nhance.android.utils.JSONUtils;
import com.nhance.android.utils.SQLDBUtil;
import com.nhance.android.utils.VedantuWebUtils;
import com.nhance.android.utils.ViewUtils;
import com.nhance.android.R;

public class MyProgramsActivity extends NhanceBaseActivity implements
        PaymentHandler.OnPaymentFinishedListner, OnAccessButtonClickedHandler {

    private static final String        TAG                                        = "MyProgramsActivity";
    private boolean                    fromSignUpRoute;
    private List<String>               categories                                 = new ArrayList<String>();
    private int                        selectedSpinnerItemPos                     = 0;
    private String                     preSelectThisCategory;
    PaymentHandler                     mPaymentHandler;

    private String                     currentSectionId;
    private OrgProgramCenterSectionIds currentOrgIds;
    private String                     currentTransactionId;
    private String                     currentOrderId;
    private OrgDataManager             orgDataManager;
    private String                     currentProgCenterSecName;
    ProgCenterSecInfo                  currentProgCenterSecInfo;

    private String                     ITEM_SKU                                   = null;

    private static final int           PENDING_TRANSACTION_STATE_PAYMENT_RECEIVED = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        GoogleAnalyticsUtils.setScreenName(ConstantGlobal.GA_SCREEN_PROGRAMS);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_my_programs);
        getSupportActionBar().hide();
        this.orgDataManager = new OrgDataManager(getApplicationContext());
        preSelectThisCategory = getIntent().getStringExtra(ConstantGlobal.CATEGORY_ID);
        fromSignUpRoute = getIntent().getBooleanExtra("fromSignUpRoute", false);
        setUpBackButton();
        currentSectionId = session.getSessionStringValue(ConstantGlobal.CURRENT_SECTION_ID);
        findViewById(R.id.refresh_all_programs_page).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                stopAllOnGoingRequests();
                reloadPage();
            }
        });
    }

    @Override
    protected void onResume() {

        reloadPage();
        GoogleAnalyticsUtils.sendPageViewDataToGA();
        super.onResume();
    }

    private void reloadPage() {

        setUpPaymentHandler();
        setUpMyPrograms();
        setUpCategoriesSpinner();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.d(TAG, "==== intent data: " + data);
        if (mPaymentHandler != null
                && !mPaymentHandler.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private List<String> myProgramIdsList = new ArrayList<String>();

    private void setUpMyPrograms() {

        OrgMemberInfo orgMemberInfo = SessionManager.getInstance(getApplicationContext())
                .getOrgMemberInfo();

        List<OrgSectionInfo> sectionsList = new ArrayList<OrgSectionInfo>();
        List<ProgCenterSecInfo> progCenterSectionInfos = new ArrayList<ProgCenterSecInfo>();
        List<String> progCenterSections = OrgDataManager.getCenterSectionString(orgMemberInfo,
                sectionsList, progCenterSectionInfos);
        LinearLayout myProgramsHolder = (LinearLayout) findViewById(R.id.my_programs_holder);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        myProgramsHolder.removeAllViews();
        if (myProgramIdsList != null) {
            myProgramIdsList.clear();
        }

        // added by Shankar
        Set<String> mySectionIds = new HashSet<String>();
        //
        for (int k = 0; k < sectionsList.size(); k++) {
            OrgSectionInfo secInfo = sectionsList.get(k);
            addProgram(inflater, true, myProgramsHolder,
                    StringUtils.upperCase(progCenterSections.get(k)), secInfo.desc, secInfo, null,
                    progCenterSectionInfos.get(k), secInfo.courses);
            myProgramIdsList.add(secInfo.id);
            mySectionIds.add(secInfo.id);
        }

        List<PendingTranscation> pendingTransactions = orgDataManager.getPendingTransactions();
        for (int k = 0; k < pendingTransactions.size(); k++) {
            PendingTranscation pendingTransactionInfo = pendingTransactions.get(k);
            ProgCenterSecInfo progCenterSecInfo = pendingTransactionInfo._getProgCenterSecInfo();
            // added by Shankar
            // if the user is already part of the program(she has joined from web)
            if (mySectionIds.contains(progCenterSecInfo.sectionId)) {
                orgDataManager.deletePendingTransaction(pendingTransactionInfo.transactionId);
                continue;
            }
            //
            addPendingTransactionProgram(inflater, myProgramsHolder, pendingTransactionInfo,
                    progCenterSecInfo);
            myProgramIdsList.add(progCenterSecInfo.sectionId);
        }
        mySectionIds.clear();
        mySectionIds = null;
        if (sectionsList.isEmpty() && pendingTransactions.isEmpty()) {
            findViewById(R.id.no_my_programs_found).setVisibility(View.VISIBLE);
            myProgramsHolder.setVisibility(View.GONE);
        }
    }

    private WebCommunicatorTask orgCategoriesReq;

    private JSONArray           categoriesJSONArray;

    private void hideAllCategoryRelatedViews() {

        findViewById(R.id.my_programs_fetching_categories_message).setVisibility(View.GONE);
        findViewById(R.id.choose_a_category_head).setVisibility(View.GONE);
        findViewById(R.id.my_programs_categories_spinner).setVisibility(View.GONE);
        findViewById(R.id.my_programs_no_categories_found).setVisibility(View.GONE);
        hideAllCategoryProgramRelatedViews();
    }

    private void hideAllCategoryProgramRelatedViews() {

        findViewById(R.id.category_programs_holder).setVisibility(View.GONE);
        findViewById(R.id.my_programs_fetching_category_programs_message).setVisibility(View.GONE);
        findViewById(R.id.no_programs_of_category_found).setVisibility(View.GONE);
    }

    private void setUpCategoriesSpinner() {

        if (!showNoInternetMessageIfNotOnline()) {
            return;
        }
        hideAllCategoryRelatedViews();
        findViewById(R.id.my_programs_fetching_categories_message).setVisibility(View.VISIBLE);
        orgCategoriesReq = new WebCommunicatorTask(
                SessionManager.getInstance(getApplicationContext()), null,
                ReqAction.GET_ORG_CATEGORIES, new ITaskProcessor<JSONObject>() {

                    @Override
                    public void onTaskStart(JSONObject result) {

                    }

                    @Override
                    public void onTaskPostExecute(boolean success, JSONObject result) {

                        findViewById(R.id.my_programs_fetching_categories_message).setVisibility(
                                View.GONE);
                        if (!success) {
                            Toast.makeText(MyProgramsActivity.this,
                                    getResources().getString(R.string.error_general),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        categoriesJSONArray = JSONUtils.getJSONArray(
                                JSONUtils.getJSONObject(result, VedantuWebUtils.KEY_RESULT),
                                VedantuWebUtils.KEY_LIST);
                        categories.clear();

                        if (categoriesJSONArray != null && categoriesJSONArray.length() > 0) {
                            for (int k = 0; k < categoriesJSONArray.length(); k++) {
                                try {
                                    JSONObject category = categoriesJSONArray.getJSONObject(k);
                                    String id = JSONUtils.getString(category, ConstantGlobal.ID);
                                    if (StringUtils.equals(id, preSelectThisCategory)) {
                                        selectedSpinnerItemPos = k;
                                    }
                                    categories.add(JSONUtils.getString(category,
                                            ConstantGlobal.NAME));
                                } catch (JSONException e) {
                                    Log.d(TAG, e.getMessage());
                                }
                            }
                            Spinner categoriesSpinner = (Spinner) findViewById(R.id.my_programs_categories_spinner);
                            categoriesSpinner.setVisibility(View.VISIBLE);
                            findViewById(R.id.choose_a_category_head).setVisibility(View.VISIBLE);
                            ArrayAdapter<String> categoriesSpinnerAdapter = new ArrayAdapter<String>(
                                    MyProgramsActivity.this,
                                    android.R.layout.simple_list_item_1, categories);
                          //  categoriesSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            categoriesSpinner.setAdapter(categoriesSpinnerAdapter);
                            categoriesSpinner
                                    .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                                        @Override
                                        public void onItemSelected(AdapterView<?> parent,
                                                View view, int position, long id) {

                                            loadCategoryPrograms(position);
                                        }

                                        @Override
                                        public void onNothingSelected(AdapterView<?> parent) {

                                        }
                                    });
                            Log.d(TAG, "Setting the first selection of spinner to position "
                                    + selectedSpinnerItemPos);
                            categoriesSpinner.setSelection(selectedSpinnerItemPos);
                        } else {
                            findViewById(R.id.my_programs_no_categories_found).setVisibility(
                                    View.VISIBLE);
                        }
                    }
                }, null, null, 0);
        orgCategoriesReq.executeTask(false);

    }

    private WebCommunicatorTask orgCategoryPorgramsReq;

    private void loadCategoryPrograms(int categoryPos) {

        Log.d(TAG, "Loading the programs of spinner item " + categoryPos);
        if (orgCategoryPorgramsReq != null) {
            orgCategoryPorgramsReq.cancel(true);
        }
        if (categoriesJSONArray == null) {
            return;
        }
        if (!showNoInternetMessageIfNotOnline()) {
            return;
        }
        JSONObject categoryJSON;
        try {
            categoryJSON = categoriesJSONArray.getJSONObject(categoryPos);
        } catch (JSONException e) {
            Log.d(TAG, e.getLocalizedMessage());
            return;
        }
        String categoryId = JSONUtils.getString(categoryJSON, ConstantGlobal.ID);
        String categoryName = JSONUtils.getString(categoryJSON, ConstantGlobal.NAME);
        final LinearLayout categoryProgramsHolder = (LinearLayout) findViewById(R.id.category_programs_holder);
        categoryProgramsHolder.removeAllViews();
        hideAllCategoryProgramRelatedViews();
        findViewById(R.id.my_programs_fetching_category_programs_message).setVisibility(
                View.VISIBLE);
        orgCategoryPorgramsReq = new WebCommunicatorTask(
                SessionManager.getInstance(getApplicationContext()), null,
                ReqAction.GET_CATEGORY_SECTIONS, new ITaskProcessor<JSONObject>() {

                    @Override
                    public void onTaskStart(JSONObject result) {

                    }

                    @Override
                    public void onTaskPostExecute(boolean success, JSONObject result) {

                        findViewById(R.id.my_programs_fetching_category_programs_message)
                                .setVisibility(View.GONE);
                        if (!success) {
                            Toast.makeText(MyProgramsActivity.this,
                                    getResources().getString(R.string.error_general),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        JSONArray sectionsList = JSONUtils.getJSONArray(
                                JSONUtils.getJSONObject(result, VedantuWebUtils.KEY_RESULT),
                                VedantuWebUtils.KEY_LIST);
                        int addedToMyProg = 0;
                        categoryProgramsHolder.setVisibility(View.VISIBLE);
                        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        for (int k = 0; k < sectionsList.length(); k++) {
                            try {
                                JSONObject secObj = JSONUtils.getJSONObject(
                                        sectionsList.getJSONObject(k), "sectionInfo");

                                JSONObject centerInfo = JSONUtils.getJSONObject(
                                        sectionsList.getJSONObject(k), "centerInfo");

                                JSONObject progInfo = JSONUtils.getJSONObject(
                                        sectionsList.getJSONObject(k), "programInfo");

                                String secName = JSONUtils.getString(secObj, ConstantGlobal.NAME);
                                String centerName = JSONUtils.getString(centerInfo,
                                        ConstantGlobal.NAME);
                                String progName = JSONUtils
                                        .getString(progInfo, ConstantGlobal.NAME);
                                String desc = JSONUtils.getString(secObj, ConstantGlobal.DESC);
                                String sectionId = JSONUtils.getString(secObj, ConstantGlobal.ID);
                                JSONObject cRateJson = JSONUtils.getJSONObject(secObj,
                                        ConstantGlobal.COST_RATE);
                                CostRate cRate = null;
                                if (cRateJson.length() > 0) {
                                    cRate = new CostRate();
                                    cRate.fromJSON(cRateJson);
                                }

                                ProgCenterSecInfo ids = new ProgCenterSecInfo(JSONUtils.getString(
                                        progInfo, ConstantGlobal.ID), JSONUtils.getString(
                                        centerInfo, ConstantGlobal.ID), sectionId,
                                        SectionAccessScope.valueOfKey(JSONUtils.getString(secObj,
                                                ConstantGlobal.ACCESS_SCOPE)), SectionRevenueModel
                                                .valueOfKey(JSONUtils.getString(secObj,
                                                        ConstantGlobal.REVENUE_MODEL)), cRate);

                                String progCenterSecName = StringUtils.upperCase(progName + ", "
                                        + centerName + ", " + secName);
                                ids.displayName = progCenterSecName;
                                // while drawing the category programs checking for programs
                                // which the user is already part
                                // of
                                if (myProgramIdsList.indexOf(ids.sectionId) > -1) {
                                    addedToMyProg++;
                                    continue;
                                }

                                addProgram(inflater, false, categoryProgramsHolder,
                                        progCenterSecName, desc, null, secObj, ids, null);
                            } catch (JSONException e) {
                                Log.d(TAG, e.getMessage());
                            }
                        }

                        if (addedToMyProg == sectionsList.length()) {
                            findViewById(R.id.no_programs_of_category_found).setVisibility(
                                    View.VISIBLE);
                        }
                    }
                }, null, null, 0, 0);
        orgCategoryPorgramsReq.addExtraParams(ConstantGlobal.ID, categoryId);
        orgCategoryPorgramsReq.addExtraParams(ConstantGlobal.NAME, categoryName);
        orgCategoryPorgramsReq.addExtraParams("openOnly", String.valueOf(true));
        orgCategoryPorgramsReq.addExtraParams("excludeSubscribed", String.valueOf(true));
        orgCategoryPorgramsReq.executeTask(false);
    }

    private void addPendingTransactionProgram(LayoutInflater inflater, LinearLayout programsHolder,
            final PendingTranscation pendingTranscation, ProgCenterSecInfo progCenterSecInfo) {

        View view = inflater.inflate(R.layout.list_item_view_category_program, programsHolder,
                false);

        TextView programNameView = ((TextView) view.findViewById(R.id.program_name_view));
        programNameView.setText(StringUtils.upperCase(progCenterSecInfo.displayName));

        view.findViewById(R.id.my_program_desc_and_courses_layout).setVisibility(View.GONE);
        view.findViewById(R.id.my_program_payment_status_message).setVisibility(View.VISIBLE);
        View paymentInfoButton = view.findViewById(R.id.my_programs_program_desc_buy_join_button);
        paymentInfoButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                startPendingTransaction(pendingTranscation);
            }
        });

        ((TextView) paymentInfoButton).setText(R.string.check_status);
        paymentInfoButton.setBackgroundResource(R.color.darkred);

        view.setOnClickListener(getPendingProgClickListner());
        programsHolder.addView(view);
    }

    private OnClickListener pendingProgClickListner;

    private OnClickListener getPendingProgClickListner() {

        if (pendingProgClickListner == null) {
            pendingProgClickListner = new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    openProgramDetails(v, true);
                }
            };
        }
        return pendingProgClickListner;
    }

    private void addProgram(LayoutInflater inflater, boolean isMyProgram,
            LinearLayout programsHolder, final String progCenterSecName, String description,
            /* isMyProgram else null */OrgSectionInfo mySecInfo, JSONObject secExtraInfo,
            final ProgCenterSecInfo ids, List<OrgStructureBasicInfo> courses) {

        View view = inflater.inflate(R.layout.list_item_view_category_program, programsHolder,
                false);

        TextView programNameView = ((TextView) view.findViewById(R.id.program_name_view));
        programNameView.setText(StringUtils.upperCase(progCenterSecName));

        String desc = getResources().getString(R.string.no_description_available);
        if (!StringUtils.isEmpty(description)) {
            desc = description;
        }
        ((TextView) view.findViewById(R.id.my_program_desc)).setText(desc);
        if (isMyProgram) {
            view.findViewById(R.id.open_program_details).setOnClickListener(
                    getOpenMyProgramDetailsClickListner(view));
            List<String> courseNames = new ArrayList<String>();
            for (OrgStructureBasicInfo courseInfo : courses) {
                courseNames.add(courseInfo.name);
            }
            view.findViewById(R.id.my_program_courses_list_view).setTag(courseNames);
            if (StringUtils.equals(mySecInfo.id, currentSectionId)) {
                programNameView.setTextColor(getResources().getColor(R.color.darkergrey));
            }
            View programNameViewHolder = view.findViewById(R.id.my_programs_program_name_holder);
            programNameViewHolder.setTag(R.integer.section_info_key, mySecInfo);
            if (ids != null) {
                OrgProgramCenterSectionIds orgIds = new OrgProgramCenterSectionIds(ids.programId,
                        ids.centerId, ids.sectionId);
                programNameViewHolder.setTag(R.integer.org_ids_key, orgIds);
            }
            programNameViewHolder
                    .setOnClickListener(getMyProgramNameClickListner(progCenterSecName));

            // for payment info
            View paymentInfoButton = view
                    .findViewById(R.id.my_programs_program_desc_buy_join_button);
            paymentInfoButton.setTag(mySecInfo);
            paymentInfoButton.setOnClickListener(getOpenPaymentInfoClickListner());
            ((TextView) paymentInfoButton).setText(R.string.payment_info);
        } else {

            view.findViewById(R.id.program_desc_holder).setTag(ids.programId);
            if (secExtraInfo != null) {

                View buyJoinButton = view.findViewById(R.id.my_programs_program_buy_join_button);

                View buyJoinDescButton = view
                        .findViewById(R.id.my_programs_program_desc_buy_join_button);
                if (ids.revenueModel == SectionRevenueModel.FREE) {
                    buyJoinButton.setVisibility(View.VISIBLE);
                    buyJoinDescButton.setVisibility(View.VISIBLE);

                } else if (ids.revenueModel == SectionRevenueModel.PAID) {

                    View groupButtonContainer = view
                            .findViewById(R.id.my_programs_program_buy_button_container);
                    groupButtonContainer.setVisibility(View.VISIBLE);
                    buyJoinDescButton.setVisibility(View.GONE);

                    View alreadyPurchasedButton = groupButtonContainer
                            .findViewById(R.id.my_programs_program_already_purchased_button_group);

                    alreadyPurchasedButton.setTag(ids);

                    alreadyPurchasedButton.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            Log.d(TAG, "alreadyPurchasedButton clicked");
                            final ProgCenterSecInfo ids = (ProgCenterSecInfo) v.getTag();

                            DialogFragment accessInfoDialogue = new DialogFragment() {

                                @Override
                                public void onCreate(Bundle savedInstanceState) {

                                    setCancelable(false);

                                    super.onCreate(savedInstanceState);
                                }

                                @Override
                                public View onCreateView(LayoutInflater inflater,
                                        ViewGroup container, Bundle savedInstanceState) {

                                    View view = inflater.inflate(R.layout.popup_access_code_layout,
                                            container);

                                    TextView programName = (TextView) view
                                            .findViewById(R.id.sdcard_group_name);
                                    programName.setText(ids.displayName);

                                    AccessCodeHandler accessCodeHandler = new AccessCodeHandler(
                                            view, new AccessCodeVerificationTask(this, ids),
                                            getApplicationContext(), ids.sectionId,
                                            EntityType.SECTION.name(), MyProgramsActivity.this);
                                    accessCodeHandler.setUpAccessCodeHandler();

                                    view.findViewById(R.id.close_payment_info_popup)
                                            .setOnClickListener(new View.OnClickListener() {

                                                @Override
                                                public void onClick(View v) {

                                                    dismiss();
                                                    closeProgressBar();
                                                }
                                            });

                                    return view;
                                }
                            };
                            accessInfoDialogue.show(getSupportFragmentManager(), "AccessCodePopup");
                        }
                    });

                    buyJoinDescButton = groupButtonContainer
                            .findViewById(R.id.my_programs_program_buy_join_button_group);

                    buyJoinButton.setVisibility(View.VISIBLE);
                    buyJoinDescButton.setVisibility(View.VISIBLE);
                    buyJoinButton.setBackgroundResource(R.color.darkred);
                    buyJoinDescButton.setBackgroundResource(R.color.darkred);

                    CostRate costRate = ids.costRate;

                    String currencyCode = costRate.currencyCode;
                    try {
                        currencyCode = Currency.getInstance(costRate.currencyCode).getSymbol();
                    } catch (Throwable e) {
                        // swallow
                    }
                    String cost = currencyCode + " " + (costRate.value / 100);

                    ((TextView) buyJoinButton).setText(cost);
                    ((TextView) buyJoinDescButton).setText(R.string.buy_now);
                }
                buyJoinDescButton.setTag(ids);
                buyJoinDescButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        currentProgCenterSecName = progCenterSecName;
                        if (ids.revenueModel == SectionRevenueModel.PAID) {
                            startPurchaseFlow(ids);
                        } else {
                            joinOrgSection(null, null, ids.programId, ids.centerId, ids.sectionId);
                        }
                    }
                });
            }

            view.setOnClickListener(getOpenCategoryProgramDetailsClickListner());
        }
        programsHolder.addView(view);
    }

    private OnClickListener openPaymentInfoClickListner;

    private OnClickListener getOpenPaymentInfoClickListner() {

        if (openPaymentInfoClickListner == null) {
            openPaymentInfoClickListner = new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    OrgSectionInfo orgSecInfo = (OrgSectionInfo) v.getTag();
                    MyProgramPaymentInfoDialogFragment dialogFrag = new MyProgramPaymentInfoDialogFragment();
                    Bundle args = new Bundle();
                    args.putSerializable("orgSecInfo", orgSecInfo);
                    dialogFrag.setArguments(args);
                    dialogFrag.show(getSupportFragmentManager(), null);
                }
            };
        }
        return openPaymentInfoClickListner;
    }

    private OnClickListener getMyProgramNameClickListner(final String progCenterSecName) {

        return new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                OrgSectionInfo secInfo = (OrgSectionInfo) v.getTag(R.integer.section_info_key);
                session.setCurrentSectionDetails(secInfo.id, progCenterSecName);

                OrgProgramCenterSectionIds orgIds = (OrgProgramCenterSectionIds) v
                        .getTag(R.integer.org_ids_key);
                session.setCurrentProgramCenterSectionIds(orgIds);

                Intent intent = new Intent(MyProgramsActivity.this, AppLandingPageActivity.class);
                startActivity(intent);
            }
        };
    }

    private View openedProgramView;

    private OnClickListener getOpenMyProgramDetailsClickListner(final View parentView) {

        return new View.OnClickListener() {

            @Override
            public void onClick(View arrowView) {

                openProgramDetails(parentView, false);
            }
        };
    }

    private OnClickListener openCategoryProgramDetailsClickListner;

    private OnClickListener getOpenCategoryProgramDetailsClickListner() {

        if (openCategoryProgramDetailsClickListner == null) {
            openCategoryProgramDetailsClickListner = new View.OnClickListener() {

                @Override
                public void onClick(View view) {

                    openProgramDetails(view, false);
                }
            };
        }
        return openCategoryProgramDetailsClickListner;
    }

    private WebCommunicatorTask programCoursesReq;

    private void openProgramDetails(final View programView, boolean forPendingTransactionProgram) {

        View descView = programView.findViewById(R.id.program_desc_holder);
        ImageView arrowView = (ImageView) programView.findViewById(R.id.open_program_details);
        if (descView.getVisibility() == View.VISIBLE) {
            descView.setVisibility(View.GONE);
            arrowView.setImageResource(R.drawable.icon_arrow_down);
            openedProgramView = null;
        } else {
            descView.setVisibility(View.VISIBLE);
            arrowView.setImageResource(R.drawable.icon_arrow_up);
            if (openedProgramView != null) {
                openedProgramView.findViewById(R.id.program_desc_holder).setVisibility(View.GONE);
                ((ImageView) openedProgramView.findViewById(R.id.open_program_details))
                        .setImageResource(R.drawable.icon_arrow_down);
            }
            openedProgramView = programView;
            if (forPendingTransactionProgram) {
                return;
            }
            final CustomListView coursesListView = (CustomListView) programView
                    .findViewById(R.id.my_program_courses_list_view);
            if (coursesListView.getTag() == null) {
                String programId = (String) programView.findViewById(R.id.program_desc_holder)
                        .getTag();
                if (programCoursesReq != null) {
                    programCoursesReq.cancel(true);
                }
                programCoursesReq = new WebCommunicatorTask(
                        SessionManager.getInstance(getApplicationContext()), null,
                        ReqAction.GET_PROGRAM_COURSES, new ITaskProcessor<JSONObject>() {

                            @Override
                            public void onTaskStart(JSONObject result) {

                            }

                            @Override
                            public void onTaskPostExecute(boolean success, JSONObject result) {

                                if (!success) {
                                    Toast.makeText(MyProgramsActivity.this,
                                            getResources().getString(R.string.error_general),
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                JSONArray coursesJSONArray = JSONUtils.getJSONArray(
                                        JSONUtils.getJSONObject(result, VedantuWebUtils.KEY_RESULT),
                                        VedantuWebUtils.KEY_LIST);
                                List<String> coursesList = new ArrayList<String>();
                                if (coursesJSONArray != null && coursesJSONArray.length() > 0) {
                                    programView.findViewById(R.id.my_program_courses_status)
                                            .setVisibility(View.GONE);
                                    for (int k = 0; k < coursesJSONArray.length(); k++) {
                                        try {
                                            JSONObject courseObj = coursesJSONArray
                                                    .getJSONObject(k);
                                            coursesList.add(JSONUtils.getString(courseObj,
                                                    ConstantGlobal.NAME));
                                        } catch (JSONException e) {
                                            Log.d(TAG, e.getMessage());
                                        }
                                    }
                                    coursesListView.setTag(coursesList);
                                    coursesListView.setAdapter(new ArrayAdapter<String>(
                                            MyProgramsActivity.this,
                                            R.layout.list_item_view_category_program_course,
                                            coursesList));
                                } else {
                                    ((TextView) programView
                                            .findViewById(R.id.my_program_courses_status))
                                            .setText(R.string.no_courses_in_program);
                                }
                            }
                        }, null, null, 0);
                programCoursesReq.addExtraParams(ConstantGlobal.PROGRAM_ID, programId);
                programCoursesReq.executeTask(false);
            } else {
                @SuppressWarnings("unchecked")
                List<String> courses = (List<String>) coursesListView.getTag();
                programView.findViewById(R.id.my_program_courses_status).setVisibility(View.GONE);
                if (coursesListView.getAdapter() == null) {
                    coursesListView.setAdapter(new ArrayAdapter<String>(MyProgramsActivity.this,
                            R.layout.list_item_view_category_program_course, courses));
                }
            }
        }
    }

    private void setUpBackButton() {

        findViewById(R.id.back_from_my_programs).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                onBackPressed();

            }
        });
    }

    @Override
    public void onBackPressed() {

        if (fromSignUpRoute) {
            // Taking the user to home page if he comes from sign up process
            Intent intent = new Intent(MyProgramsActivity.this, AppLandingPageActivity.class);
            startActivity(intent);
        }
        super.onBackPressed();
    }

    private void stopAllOnGoingRequests() {

        if (orgCategoriesReq != null) {
            orgCategoriesReq.cancel(true);
        }
        if (orgCategoryPorgramsReq != null) {
            orgCategoryPorgramsReq.cancel(true);
        }
        if (programCoursesReq != null) {
            programCoursesReq.cancel(true);
        }
    }

    @Override
    protected void onStop() {

        super.onStop();
        stopAllOnGoingRequests();
    }

    private void setUpPaymentHandler() {

        mPaymentHandler = new PaymentHandler();
        mPaymentHandler.setUp();
    }

    ProgressDialog progressDialog;

    private void joinOrgSection(String transactionId, String orderId, final String programId,
            final String centerId, final String sectionId) {

        if (progressDialog == null) {
            launchProgressBar(getString(R.string.joining_msg));
        } else {
            updateProgressBarMsg(getString(R.string.joining_msg));
        }

        // show progress dialogue fetching library
        SocialActionPosterTask task = new SocialActionPosterTask(
                session,
                null,
                com.nhance.android.async.tasks.socials.SocialActionPosterTask.ReqAction.ADD_ORG_MEMBER_MAPPING,
                new ITaskProcessor<JSONObject>() {

                    @Override
                    public void onTaskStart(JSONObject result) {

                    }

                    @Override
                    public void onTaskPostExecute(boolean success, JSONObject result) {

                        // if success then fetch library content
                        if (!success) {
                            String errorCode = JSONUtils.getString(result,
                                    VedantuWebUtils.KEY_ERROR_CODE);
                            int msgId = R.string.error_not_added_to_program;
                            if ("ORGANIZATION_MEMBER_MAPPING_ALREADY_EXISTS".equals(errorCode)) {
                                msgId = R.string.error_already_part_of_program;
                                orgDataManager.deletePendingTransaction(currentTransactionId);
                            }
                            Toast.makeText(getApplicationContext(), msgId, Toast.LENGTH_LONG)
                                    .show();
                            closeProgressBar();
                            return;
                        }

                        result = JSONUtils.getJSONObject(result, VedantuWebUtils.KEY_RESULT);
                        JSONObject orgProfileInfo = JSONUtils.getJSONObject(result,
                                ConstantGlobal.INFO);
                        UserDataManager userDataManager = new UserDataManager(
                                getApplicationContext());

                        UserOrgProfile userOrgProfile = userDataManager.getUserOrgProfile(
                                session.getSessionStringValue(ConstantGlobal.USER_ID),
                                session.getSessionStringValue(ConstantGlobal.ORG_ID));
                        try {
                            userOrgProfile.orgProfile.put(ConstantGlobal.INFO, orgProfileInfo);
                            userDataManager.updateUserOrgProfile(userOrgProfile);
                            session.updateUserOrgProfile(userOrgProfile);
                        } catch (JSONException e) {}

                        // user is now part of the section hence we can remove local transaction
                        // status
                        orgDataManager.deletePendingTransaction(currentTransactionId);

                        updateProgressBarMsg(getString(R.string.fetching_library));
                        LibraryManager.getInstance(getApplicationContext())
                                .fetchLibraryContentFromCMDS(null,
                                        session.getSessionStringValue(ConstantGlobal.USER_ID),
                                        sectionId, "SECTION", true, new OnLibraryLoaded());
                        currentSectionId = sectionId;
                        currentOrgIds = new OrgProgramCenterSectionIds(programId, centerId,
                                sectionId);
                        ITEM_SKU = null;
                    }
                });
        task.addExtraParams(getAddMappingParams(programId, centerId, sectionId));
        if (!TextUtils.isEmpty(transactionId)) {
            task.addExtraParams(ConstantGlobal.TRANSACTION_ID, transactionId);
            task.addExtraParams(ConstantGlobal.ORDER_ID, orderId);
        }
        task.executeTask(true);
    }

    private Map<String, Object> getAddMappingParams(String programId, String centerId,
            String sectionId) {

        Map<String, Object> params = new HashMap<String, Object>();
        OrgMemberInfo mInfo = session.getOrgMemberInfo();
        if (mInfo == null) {
            return params;
        }
        params.put("targetOrgMemberId", mInfo.id);
        params.put("returnOrgProfileWithCourseInfo", String.valueOf(true));
        params.put("targetProfile", mInfo.profile);
        params.put("programId", programId);
        params.put("centerId", centerId);
        SessionManager.addListParams(Arrays.asList(new String[] { sectionId }), "sectionIds",
                params);

        return params;
    }

    private void launchProgressBar(String msg) {

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(msg);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        // progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
        // new DialogInterface.OnClickListener() {
        //
        // @Override
        // public void onClick(DialogInterface dialog, int which) {
        //
        // dialog.cancel();
        // }
        // });
        progressDialog.show();
    }

    private void closeProgressBar() {

        if (progressDialog != null) {
            progressDialog.cancel();
            progressDialog = null;
        }
    }

    private void updateProgressBarMsg(String msg) {

        if (progressDialog != null) {
            progressDialog.setMessage(msg);
        }
    }

    class OnLibraryLoaded implements ITaskProcessor<Integer> {

        @Override
        public void onTaskStart(Integer result) {

        }

        @Override
        public void onTaskPostExecute(boolean success, Integer result) {

            closeProgressBar();
            session.setCurrentSectionDetails(currentSectionId, currentProgCenterSecName);
            session.setCurrentProgramCenterSectionIds(currentOrgIds);
            Intent intent = new Intent(MyProgramsActivity.this, AppLandingPageActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    private void startPurchaseFlow(final ProgCenterSecInfo ids) {

        if (!SessionManager.isOnline()) {
            Toast.makeText(getApplicationContext(), R.string.no_internet_msg, Toast.LENGTH_LONG)
                    .show();
            closeProgressBar();
            return;
        }

        SocialActionPosterTask task = new SocialActionPosterTask(
                session,
                null,
                com.nhance.android.async.tasks.socials.SocialActionPosterTask.ReqAction.START_TRANSACTION,
                new ITaskProcessor<JSONObject>() {

                    @Override
                    public void onTaskStart(JSONObject result) {

                    }

                    @Override
                    public void onTaskPostExecute(boolean success, JSONObject result) {

                        if (!success) {
                            Toast.makeText(getApplicationContext(),
                                    R.string.error_transaction_not_started, Toast.LENGTH_LONG)
                                    .show();
                            closeProgressBar();
                            return;
                        }
                        result = JSONUtils.getJSONObject(result, VedantuWebUtils.KEY_RESULT);

                        final StartTransactionRes res = new StartTransactionRes();
                        res.fromJSON(result);
                        currentTransactionId = res.transactionId;
                        currentOrderId = res.orderId;
                        Log.i(TAG, "=== transactionid === : " + currentTransactionId + ", orderId:"
                                + currentOrderId);

                        ITEM_SKU = getItemIdentifier(currentTransactionId, currentOrderId, ids);

                        currentProgCenterSecInfo = ids;

                        closeProgressBar();
                        if (TextUtils.isEmpty(res.paymentUrl)) {
                            Toast.makeText(getApplicationContext(),
                                    R.string.error_transaction_not_started, Toast.LENGTH_LONG)
                                    .show();
                            Log.e(TAG, "no pyament Url received from server [ paymentUrl : "
                                    + res.paymentUrl + "]");
                            return;
                        }
                        DialogFragment transactionInfoDialogue = new DialogFragment() {

                            @Override
                            public void onCreate(Bundle savedInstanceState) {

                                setStyle(STYLE_NO_TITLE, R.style.DialogFragmentStyle);
                                setCancelable(false);
                                super.onCreate(savedInstanceState);
                            }

                            @Override
                            public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                    Bundle savedInstanceState) {

                                View view = inflater.inflate(R.layout.popup_start_transaction_info,
                                        container);

                                ViewUtils.setTextViewValue(view, R.id.transaction_order_id,
                                        res.orderId);

                                final EditText paymentEmail = (EditText) view
                                        .findViewById(R.id.transaction_email);
                                if (!res.needEmail && !TextUtils.isEmpty(res.email)) {
                                    paymentEmail.setText(res.email);
                                    paymentEmail.setInputType(InputType.TYPE_NULL);
                                    paymentEmail.setEnabled(false);
                                }
                                view.findViewById(R.id.proceed_transaction).setOnClickListener(
                                        new View.OnClickListener() {

                                            @Override
                                            public void onClick(View v) {

                                                final String email = paymentEmail.getText()
                                                        .toString();
                                                if (!com.nhance.android.utils.StringUtils
                                                        .isValidEmail(email)) {
                                                    Toast.makeText(getActivity(),
                                                            R.string.transaction_email_message,
                                                            Toast.LENGTH_LONG).show();
                                                    return;
                                                }
                                                mPaymentHandler.startPayment(
                                                        email,
                                                        res.paymentUrl,
                                                        MyProgramsActivity.this,
                                                        session.getSessionStringValue(ConstantGlobal.USER_ID),
                                                        ITEM_SKU, currentTransactionId,
                                                        PaymentHandler.REQUEST_CODE,
                                                        MyProgramsActivity.this);

                                                dismiss();
                                            }
                                        });

                                view.findViewById(R.id.close_payment_info_popup)
                                        .setOnClickListener(new View.OnClickListener() {

                                            @Override
                                            public void onClick(View v) {

                                                dismiss();
                                                closeProgressBar();
                                            }
                                        });

                                return view;
                            }
                        };
                        transactionInfoDialogue
                                .show(getSupportFragmentManager(), "transactionInfo");
                    }
                });
        task.addExtraParams("itemName", ids.displayName);
        task.addExtraParams("item.id", ids.sectionId);
        task.addExtraParams("item.type", "SECTION");
        task.addExtraParams("customer.type", "USER");
        task.addExtraParams("customer.id", session.getSessionStringValue(ConstantGlobal.USER_ID));
        task.executeTask(true);
        launchProgressBar(getString(R.string.transaction_initating));
    }

    private String getItemIdentifier(String transactionId, String orderId, ProgCenterSecInfo ids) {

        return TextUtils.join(SQLDBUtil.SEPARATOR, new String[] { "transaction", transactionId,
                "order", orderId, "program", ids.programId, "center", ids.centerId, "section",
                ids.sectionId, "user", session.getSessionStringValue(ConstantGlobal.USER_ID) });
    }

    private void startPendingTransaction(PendingTranscation pendingTranscation) {

        if (pendingTranscation == null) {
            return;
        }

        Log.d(TAG, "pendingTranscation: " + pendingTranscation);

        PaymentResult result = new PaymentResult(pendingTranscation.transactionInfo);

        Log.d(TAG, "result : " + result);
        if (result.item_sku == null || result.isFailure()) {
            Toast.makeText(getApplicationContext(), R.string.error_transaction_invalid,
                    Toast.LENGTH_LONG).show();
            orgDataManager.deletePendingTransaction(pendingTranscation.transactionId);
            return;
        }
        Log.d(TAG, "transaction step : " + pendingTranscation.step);

        currentTransactionId = pendingTranscation.transactionId;
        currentOrderId = pendingTranscation.orderId;

        switch (pendingTranscation.step) {
        case PENDING_TRANSACTION_STATE_PAYMENT_RECEIVED:
            final String[] verificationParams = TextUtils.split(result.item_sku,
                    SQLDBUtil.SEPARATOR);
            joinOrgSection(result.transactionId, verificationParams[3], verificationParams[5],
                    verificationParams[7], verificationParams[9]);

            break;
        default:
            break;
        }
    }

    @Override
    public void onPaymentFinished(PaymentResult result) {

        if (result == null
                || PaymentResult.TRANSACTION_STATUS_NOT_STARTED.equals(result.transactionStatus)) {
            Toast.makeText(getApplicationContext(), R.string.error_transaction_not_started,
                    Toast.LENGTH_LONG).show();
            closeProgressBar();
            return;
        }

        if (result.isFailure()) {
            Toast.makeText(
                    getApplicationContext(),
                    result.isCancelled() ? R.string.error_transaction_cancelled
                            : R.string.error_transaction_failure, Toast.LENGTH_LONG).show();
            closeProgressBar();
            return;
        }
        launchProgressBar(getString(R.string.checking_transaction_status));

        final boolean isSuccess = result.isSuccess() && result.item_sku != null
                && result.item_sku.equals(ITEM_SKU);
        PendingTranscation pendingTranscation = null;
        if (isSuccess) {
            pendingTranscation = new PendingTranscation(
                    session.getSessionIntValue(ConstantGlobal.ORG_KEY), currentTransactionId,
                    currentOrderId, currentProgCenterSecInfo, TransactionStatus.SUCCESS);
            pendingTranscation.transactionInfo = result != null ? result.getOriginalJson() : null;
            pendingTranscation.step = PENDING_TRANSACTION_STATE_PAYMENT_RECEIVED;
            try {
                Log.d(TAG, "pendingTranscation: " + pendingTranscation);
                orgDataManager.insertPendingTransaction(pendingTranscation);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        } else {
            Toast.makeText(getApplicationContext(), R.string.error_transaction_invalid,
                    Toast.LENGTH_LONG).show();
            closeProgressBar();
            return;
        }

        updateProgressBarMsg(getString(R.string.transaction_successful));

        final String[] verificationParams = isSuccess ? TextUtils.split(result.item_sku,
                SQLDBUtil.SEPARATOR) : new String[] {};

        joinOrgSection(result.transactionId, verificationParams[3], verificationParams[5],
                verificationParams[7], verificationParams[9]);
    }

    private class AccessCodeVerificationTask implements ITaskProcessor<JSONObject> {

        DialogFragment    accessInfoDialogue;
        ProgCenterSecInfo ids;

        public AccessCodeVerificationTask(DialogFragment accessInfoDialogue,
                final ProgCenterSecInfo ids) {

            super();
            this.accessInfoDialogue = accessInfoDialogue;
            this.ids = ids;
        }

        @Override
        public void onTaskStart(JSONObject result) {

        }

        @Override
        public void onTaskPostExecute(boolean success, JSONObject result) {

            if (result == null) {
                closeProgressBar();
                Toast.makeText(getApplicationContext(), R.string.error_connecting_to_internet,
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (!success) {
                closeProgressBar();
                ErrorCode errorCode = ErrorCode.valueOfKey(JSONUtils.getString(result,
                        VedantuWebUtils.KEY_ERROR_CODE));
                Toast.makeText(
                        getApplicationContext(),
                        errorCode.getErrorMessageResource() != -1 ? errorCode
                                .getErrorMessageResource() : R.string.error_general,
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (accessInfoDialogue != null) {
                try {
                    // this try catch block is used to ensure handelling of RuntimeException when
                    // the accessInfoDialogue is not attach to a window
                    accessInfoDialogue.dismiss();
                } catch (Throwable e) {}
            }

            result = JSONUtils.getJSONObject(result, VedantuWebUtils.KEY_RESULT);
            final String accessCode = JSONUtils.getString(result, ConstantGlobal.ACCESS_CODE);
            final String transactionId = JSONUtils.getString(result, ConstantGlobal.TRANSACTION_ID);
            final long orderId = JSONUtils.getLong(result, ConstantGlobal.ORDER_ID);

            if (!TextUtils.isEmpty(accessCode)) {
                joinOrgSection(transactionId, String.valueOf(orderId), ids.programId, ids.centerId,
                        ids.sectionId);
            }

        }
    }

    @Override
    public void onButtonClicked() {

        launchProgressBar(getString(R.string.access_code_validating_msg));

    }

}
