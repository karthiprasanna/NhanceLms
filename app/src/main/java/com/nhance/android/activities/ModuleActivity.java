package com.nhance.android.activities;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nhance.android.activities.baseclasses.NhanceBaseActivity;
import com.nhance.android.async.tasks.AnalyticsSyncer;
import com.nhance.android.async.tasks.ITaskProcessor;
import com.nhance.android.async.tasks.LibraryLoader;
import com.nhance.android.async.tasks.ModuleEntryStatusSyncer;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.AnalyticsDataManager;
import com.nhance.android.db.datamanagers.ContentDataManager;
import com.nhance.android.db.datamanagers.ContentLinkDataManager;
import com.nhance.android.db.datamanagers.ModuleStatusDataManager;
import com.nhance.android.db.models.UserModuleEntryStatus;
import com.nhance.android.db.models.entity.Content;
import com.nhance.android.db.models.entity.ContentLink;
import com.nhance.android.enums.AttemptState;
import com.nhance.android.enums.EntityType;
import com.nhance.android.enums.ModuleEntryCompletionRuleType;
import com.nhance.android.enums.ModuleRun;
import com.nhance.android.fragments.library.LibraryInfoDialogFragment;
import com.nhance.android.library.utils.LibraryUtils;
import com.nhance.android.managers.LibraryManager;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.pojos.LibraryContentRes;
import com.nhance.android.pojos.SrcEntity;
import com.nhance.android.pojos.content.infos.ModuleBasicInfo;
import com.nhance.android.pojos.content.infos.ModuleExtendedInfo;
import com.nhance.android.pojos.content.infos.TestExtendedInfo;
import com.nhance.android.pojos.slpmodules.ModuleEntryCompletionRule;
import com.nhance.android.pojos.slpmodules.ModuleEntryInfo;
import com.nhance.android.utils.ViewUtils;
import com.nhance.android.R;

public class ModuleActivity extends NhanceBaseActivity {

    private static final String     TAG                 = "ModuleActivity";
    private LayoutInflater          inflater;
    private ContentDataManager      contentDataManager;
    private ContentLinkDataManager  contentLinkDataManager;
    private LibraryContentRes       contentRes;
    private ModuleExtendedInfo      moduleExtendedInfo;
    private LibraryLoader           moduleContentLoader = null;
    private String                  moduleId;
    private ModuleRun               moduleRun           = ModuleRun.NON_SEQUENTIAL;
    private String                  contentIdNowForSequentialModuleRun;
    private ModuleBasicInfo         moduleBasicInfo;
    private AnalyticsDataManager    analyticsManager;
    private ModuleStatusDataManager moduleStatusDataManager;
    private boolean                 fetchContentIfNotAvailable;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_module);
        getSupportActionBar().hide();
        moduleId = getIntent().getStringExtra(ConstantGlobal.ENTITY_ID);
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentDataManager = new ContentDataManager(this);
        contentLinkDataManager = new ContentLinkDataManager(this);
        moduleStatusDataManager = new ModuleStatusDataManager(this);
        analyticsManager = new AnalyticsDataManager(this);
        contentRes = contentDataManager.getLibraryContentRes(getIntent().getStringExtra(
                ConstantGlobal.LINK_ID));
        if (contentRes == null) {
            Toast.makeText(this, "Some error occured", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        ((TextView) findViewById(R.id.module_title)).setText(getIntent().getStringExtra(
                ConstantGlobal.NAME));
        // TODO unhandled exception
        String timeAdded = DateUtils.formatDateTime(ModuleActivity.this, Long.parseLong(contentRes.linkTime), DateUtils.FORMAT_NUMERIC_DATE);
        ((TextView) findViewById(R.id.module_added_date)).setText("LEARNING PATH | Added on " + timeAdded);

        fetchContentIfNotAvailable = true;

        findViewById(R.id.sync_module).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
            try {
                Log.d(TAG, "Beginning the syncing of module with module id: " + moduleId);
                SessionManager session = SessionManager.getInstance(getApplicationContext());
                findViewById(R.id.moudle_sync_progress_bar).setVisibility(View.VISIBLE);
                ModuleEntryStatusSyncer moduleEntryStatusSyncer;
                moduleEntryStatusSyncer = new ModuleEntryStatusSyncer(session, null, moduleId,

                new ITaskProcessor<JSONObject>() {

                    @Override
                    public void onTaskStart(JSONObject result) {

                    }

                    @Override
                    public void onTaskPostExecute(boolean success, JSONObject result) {

                        findViewById(R.id.moudle_sync_progress_bar).setVisibility(View.GONE);
                        onResume();
                    }
                });
                moduleEntryStatusSyncer.executeTask(false);
            } catch (Exception e) {
                Log.d(TAG, "Some error occured in updating module of id: " + moduleId
                        + " Error: " + e.getMessage());
            }
            }
        });
        setInfoButtons();
        findViewById(R.id.module_head_icon_holder).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    private OnClickListener infoClickListner = new OnClickListener() {
        @Override
        public void onClick(View v) {

            ModuleEntryInfoStore infoStored = (ModuleEntryInfoStore) v.getTag();
            if (infoStored == null) {
             return;
            }
            ContentLink contentLink = infoStored.contentLink;
            String contentLinkId = contentLink.linkId;
            boolean downloadable = infoStored.downloadable;

            if (StringUtils.equalsIgnoreCase(
                 EntityType.TEST.name(),
                 contentLink.entityType)
                 || StringUtils.equalsIgnoreCase(
                         EntityType.ASSIGNMENT.name(),
                         contentLink.entityType)) {
                downloadable = true;
            }
            LibraryInfoDialogFragment popupFragment = new LibraryInfoDialogFragment();
            Bundle args = new Bundle();
            args.putString(ConstantGlobal.CONTENT_ID,
                 contentLinkId);
            args.putBoolean(ConstantGlobal.DOWNLOADABLE,
                 downloadable);
            args.putString(ConstantGlobal.MODULE_ID,
                 moduleId);
            if (ModuleRun.SEQUENTIAL.equals(moduleRun)
                 && StringUtils
                         .isNotEmpty(contentIdNowForSequentialModuleRun)
                 && !StringUtils
                         .equals(contentLink.entityId,
                                 contentIdNowForSequentialModuleRun)
                 && !infoStored.consumed
                 && infoStored.completionRule.type
                         .equals(ModuleEntryCompletionRuleType.VIEW)) {
                args.putBoolean("RESTRICT_ACCESS", true);
            }
            popupFragment.setArguments(args);
            popupFragment.show(getSupportFragmentManager(), null);
        }
    };

    private void setInfoButtons() {

        findViewById(R.id.make_info_buttons).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                boolean showInfoButtons = false;
                if (v.getTag() != null && (Boolean) v.getTag() == true) {
                    // hide info buttons
                    ((ImageView) v).setImageResource(R.drawable.ic_settings_white_24dp);
                    v.setTag(false);
                } else {
                    // show info buttons
                    ((ImageView) v).setImageResource(R.drawable.ic_cancel_white_24dp);
                    showInfoButtons = true;
                    v.setTag(true);
                }

                LinearLayout mainSection = (LinearLayout) findViewById(R.id.module_main_section);
                for (int k = 0, l = mainSection.getChildCount(); k < l; k++) {
                    RelativeLayout entryView = (RelativeLayout) mainSection.getChildAt(k);
                    View imageView = entryView.findViewById(R.id.module_content_navigator);
                    if (imageView != null) {
                        if (showInfoButtons) {
                            ((ImageView) imageView).setImageResource(R.drawable.information);
                            imageView.setOnClickListener(infoClickListner);
                        } else {
                            int resId = -1;
                            if (imageView.getTag() != null) {
                                resId = ((ModuleEntryInfoStore) imageView.getTag()).navigatorResId;
                            }
                            ((ImageView) imageView).setImageResource(resId);
                            imageView.setOnClickListener(null);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {

        finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        super.onBackPressed();
    }

    private Map<SrcEntity, UserModuleEntryStatus> moduleEntryStatus;

    private int                                   compulsoryContentCount;
    private int                                   consumedCompulsoryContentCount;

    @Override
    protected void onResume() {

        contentIdNowForSequentialModuleRun = null;
        if (contentRes != null) {
            moduleExtendedInfo = (ModuleExtendedInfo) contentRes.toContentExtendedInfo();
            moduleBasicInfo = (ModuleBasicInfo) contentRes.toContentBasicInfo();
            moduleRun = moduleBasicInfo.moduleRun;
            // TODO change this way of storing consumed ids, it can be faulty
            moduleEntryStatus = moduleStatusDataManager.getModuleEntryStatus(
                    session.getSessionStringValue(ConstantGlobal.USER_ID),
                    session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID), moduleId);
            Log.d(TAG, "module Entry status of this module " + moduleEntryStatus);
            compulsoryContentCount = 0;
            consumedCompulsoryContentCount = 0;
            populateContent();
            putCompletionPercent();
            ImageView imageView = (ImageView) findViewById(R.id.make_info_buttons);
            imageView.setTag(null);
            imageView.setImageResource(R.drawable.ic_settings_white_24dp);
        } else {
            Toast.makeText(this, "Some error occured", Toast.LENGTH_SHORT).show();
            finish();
        }
        super.onResume();
    }

    public void putCompletionPercent() {

        // TODO change based on completion rule
        if (moduleBasicInfo != null) {
            Log.d(TAG, "Compulsory content count is:  " + compulsoryContentCount);
            Log.d(TAG, "consumedCompulsory content count is:  " + consumedCompulsoryContentCount);
            int percent = 0;
            if (compulsoryContentCount > 0) {
                percent = consumedCompulsoryContentCount * 100 / compulsoryContentCount;
                int width = Math.round(ViewUtils.getMeasuredWidthInPx(this) * percent / 100);
                View progressBar = findViewById(R.id.module_progress_bar);
                LayoutParams params = progressBar.getLayoutParams();
                params.width = width;
                progressBar.setLayoutParams(params);
                ((TextView) findViewById(R.id.module_progress_text)).setText("Progress " + percent
                        + "%");
                findViewById(R.id.module_progress_holder).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.module_progress_holder).setVisibility(View.GONE);
            }
        }
    }

    private ProgressDialog moduleContentProcessDialog;

    private void populateContent() {

        LinearLayout mainSection = (LinearLayout) findViewById(R.id.module_main_section);
        mainSection.removeAllViews();

        int countForContent = 1;
        int topicCount = 0;
        if (moduleExtendedInfo != null) {
            List<ModuleEntryInfo> children = moduleExtendedInfo.children;
            if (children.size() > 0 && children.get(0).entity != null) {
                // ==> first item is a content, so adding a line there
                addTopicHead("", mainSection, 0);
                RelativeLayout firstView = (RelativeLayout) mainSection.getChildAt(0);
                firstView.findViewById(R.id.module_topic_count).setVisibility(View.GONE);
                LayoutParams params = firstView.getLayoutParams();
                params.height = (params.height / 2);
            }
            for (ModuleEntryInfo entryInfo : children) {
                if (entryInfo.entity == null) {
                    addTopicHead(entryInfo.name, mainSection, topicCount);
                    topicCount++;
                } else {
                    Content content = entryInfo.info;
                    SrcEntity entity = entryInfo.entity;
                    if (content == null) {
                        // TODO since the content after fetching is not
                        // being
                        // set, doing this
                        content = contentDataManager.getContent(entity.id, entity.type.name(),
                                session.getSessionStringValue(ConstantGlobal.USER_ID),
                                session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID));
                    }
                    if (content != null) {
                        ContentLink contentLink = contentLinkDataManager.getContentLink(entity.id,
                                entity.type.name(),
                                session.getSessionStringValue(ConstantGlobal.USER_ID), null, null,
                                session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID));
                        if (contentLink != null) {
                            addContentView(contentLink, content, mainSection, countForContent,
                                    entryInfo, entryInfo.downloadable);
                            countForContent++;
                        }
                    } else {
                        Log.d(TAG, "Some Content not found, so starting Syncing..");
                        if (fetchContentIfNotAvailable) {
                            moduleContentLoader = LibraryManager.getInstance(
                                    getApplicationContext())
                                    .fetchLibraryContentFromCMDS(null,
                                            session.getSessionStringValue(ConstantGlobal.USER_ID),
                                            moduleId, "MODULE", true,
                                            new FetchModuleContentPostProcessor());
                            findViewById(R.id.module_main_section).setVisibility(View.INVISIBLE);
                            moduleContentProcessDialog = ProgressDialog.show(this,
                                    "Loading Module Content",
                                    "Please wait while the content is loading");
                            // moduleContentProcessDialog.setCancelable(true);
                            moduleContentProcessDialog.setCanceledOnTouchOutside(false);
                        }
                        fetchContentIfNotAvailable = false;
                        break;
                    }
                }

            }

        }
    }

    private void addTopicHead(String name, LinearLayout holder, int count) {

        String countStr = getIndexChar(count + 1);
        View head = inflater.inflate(R.layout.module_topic_head, holder, false);
        ((TextView) head.findViewById(R.id.module_topic_count)).setText(countStr);
        ((TextView) head.findViewById(R.id.module_topic_title))
                .setText(StringUtils.upperCase(name));
        holder.addView(head);
    }

    String[] alphabets = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N",
            "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };

    private String getIndexChar(int count) {

        String returnStr = "";
        if (count > 26) {
            int firstCharIndex = ((count - 1) / 26);
            if (firstCharIndex > 25) {
                return returnStr;
            }
            String firstChar = alphabets[firstCharIndex - 1];
            String secondChar = alphabets[(count - 1) % 26];
            returnStr = StringUtils.upperCase((firstChar + secondChar));
        } else {
            returnStr = StringUtils.upperCase(alphabets[count - 1]);
        }
        return returnStr;
    }

    private OnClickListener onContentClickListner = new OnClickListener() {

                                                      @Override
                                                      public void onClick(View v) {

                                                          ModuleEntryInfoStore infoStored = (ModuleEntryInfoStore) v
                                                                  .getTag();
                                                          if (infoStored == null) {
                                                              return;
                                                          }
                                                          ContentLink contentLink = infoStored.contentLink;
                                                          String contentId = contentLink.entityId;
                                                          LibraryContentRes libraryContentRes = contentDataManager
                                                                  .getLibraryContentRes(contentLink.linkId);
                                                          if (ModuleRun.SEQUENTIAL
                                                                  .equals(moduleRun)
                                                                  && StringUtils
                                                                          .isNotEmpty(contentIdNowForSequentialModuleRun)
                                                                  && !StringUtils
                                                                          .equals(contentId,
                                                                                  contentIdNowForSequentialModuleRun)
                                                                  && !infoStored.consumed
                                                                  && infoStored.completionRule.type
                                                                          .equals(ModuleEntryCompletionRuleType.VIEW)) {
                                                              Toast.makeText(
                                                                      ModuleActivity.this,
                                                                      "Please consume the content in sequential order",
                                                                      Toast.LENGTH_SHORT).show();
                                                              return;
                                                          }
                                                          LibraryUtils
                                                                  .onLibraryItemClickListnerImpl(
                                                                          ModuleActivity.this,
                                                                          libraryContentRes,
                                                                          moduleId);
                                                      }
                                                  };

    private void addContentView(ContentLink contentLink, Content content, LinearLayout mainSection,
            int pos, ModuleEntryInfo entryInfo, boolean downloadable) {

        EntityType entityType = EntityType.valueOf(contentLink.entityType);
        ModuleContentEntityType moduleContentEntityType = ModuleContentEntityType
                .valueOfKey(entityType.name());
        ModuleEntryCompletionRule completionRule = entryInfo.completionRule;
        ModuleEntryCompletionRuleType moduleEntryCompletionRuleType = completionRule.type;
        if (ModuleContentEntityType.UNKNOWN.equals(moduleContentEntityType)) {
            return;
        }
        View contentView = inflater.inflate(R.layout.module_content, mainSection, false);
        ((TextView) contentView.findViewById(R.id.module_content_title_count)).setText(pos + ".");
        String contentName = content.name;
        if (StringUtils.isNotEmpty(entryInfo.name)) {
            contentName = entryInfo.name;
        }
        ((TextView) contentView.findViewById(R.id.module_content_title)).setText(contentName);
        TextView statusView = ((TextView) contentView.findViewById(R.id.module_content_state));

        // language of module content
        boolean consumed = moduleEntryStatus.containsKey(new SrcEntity(entityType, content.id));
        Log.e("consumed", "..."+consumed);
        // TODO to be done by backend
        if (entityType.equals(EntityType.TEST)) {
            TestExtendedInfo testInfo = (TestExtendedInfo) content.toContentExtendedInfo();
            if (testInfo != null && testInfo.attempteState != null
                    && testInfo.attempteState.equals(AttemptState.ATTEMPTED)) {
                consumed = true;
                try {
                    moduleStatusDataManager.updateModuleEntryStatus(
                            session.getSessionStringValue(ConstantGlobal.USER_ID),
                            session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID), moduleId,
                            content.id, "TEST");
                    // TODO very bad
                    moduleEntryStatus.put(new SrcEntity(entityType, content.id), null);
                } catch (Exception e) {
                    Log.d(TAG, "Some error occured in updating test of id: " + content.id
                            + " Error: " + e.getMessage());
                }
            }
        }

        int resIdForNavigator = R.drawable.ic_keyboard_arrow_right_black_24dp;
        if (consumed) {
            resIdForNavigator = R.drawable.ic_done_black_24dp;
            statusView.setText(moduleContentEntityType.consumedText);
            contentView.setBackgroundResource(R.drawable.selector_white_bg);
        } else {
            if (moduleEntryCompletionRuleType.equals(ModuleEntryCompletionRuleType.VIEW)) {
                statusView.setText(moduleContentEntityType.consumeTextCompulsory);
            } else {
                statusView.setText(moduleContentEntityType.consumeTextOptional);
            }
            if (moduleRun.equals(ModuleRun.SEQUENTIAL)) {
                resIdForNavigator = 0;
            }
        }
        Log.d(TAG, "module run for " + content.name + ":" + moduleRun.name());
        Log.d(TAG, "module rn" + StringUtils.isEmpty(contentIdNowForSequentialModuleRun));
        Log.d(TAG, "consumed " + consumed);
        Log.d(TAG, "moduleEntryCompletionRuleType " + moduleEntryCompletionRuleType.name());
        if (moduleRun.equals(ModuleRun.SEQUENTIAL)
                && StringUtils.isEmpty(contentIdNowForSequentialModuleRun) && !consumed
                && moduleEntryCompletionRuleType.equals(ModuleEntryCompletionRuleType.VIEW)) {
            contentIdNowForSequentialModuleRun = content.id;

            resIdForNavigator = R.drawable.icon_arrow_module_content_green;
            statusView.setText(moduleContentEntityType.consumeNowText);
            statusView.setTextColor(getResources().getColor(R.color.slpgreen));
            /*
             * float s = getResources().getDimension(
             * R.dimen.module_content_state_active_font_size);
             */
            // statusView.setTextSize(s);
        }
        if (moduleEntryCompletionRuleType.equals(ModuleEntryCompletionRuleType.VIEW)) {
            compulsoryContentCount++;
            if (consumed) {
                consumedCompulsoryContentCount++;
            }
        }
        ModuleEntryInfoStore infoStore = new ModuleEntryInfoStore(consumed, completionRule,
                contentLink, resIdForNavigator, downloadable);

        ImageView navigatorView = ((ImageView) contentView
                .findViewById(R.id.module_content_navigator));
        navigatorView.setImageResource(resIdForNavigator);
        navigatorView.setTag(infoStore);

        statusView.setCompoundDrawablesWithIntrinsicBounds(moduleContentEntityType.smallIconResId,
                0, 0, 0);

        contentView.setTag(infoStore);
        contentView.setOnClickListener(onContentClickListner);
        mainSection.addView(contentView);
    }

    private enum ModuleContentEntityType {
        DOCUMENT(R.drawable.icon_small_document, "Read", "Must Read", "Read", "Read Now"),
        TEST(R.drawable.icon_small_test, "Attempted", "Must Attempt", "Attempt", "Attempt Now"),
        VIDEO(R.drawable.icon_small_video, "Watched", "Must Watch", "Watch", "Watch Now"),
        ASSIGNMENT(R.drawable.icon_small_assignment, "Attempted", "Must Attempt", "Attempt", "Attempt Now"),
        FILE(R.drawable.icon_small_file, "", "", "", ""),
        HTMLCONTENT(R.drawable.icon_small_scorm, "Read", "Must Read", "Read", "Read Now"),
        UNKNOWN;

        int    smallIconResId = -1;
        String consumedText;
        String consumeTextCompulsory;
        String consumeTextOptional;
        String consumeNowText;

        private ModuleContentEntityType(int smallIconResId, String consumedText,
                String consumeTextCompulsory, String consumeTextOptional, String consumeNowText) {
            this.smallIconResId = smallIconResId;
            this.consumedText = consumedText;
            this.consumeTextCompulsory = consumeTextCompulsory;
            this.consumeTextOptional = consumeTextOptional;
            this.consumeNowText = consumeNowText;
        }

        private ModuleContentEntityType() {
        }

        public static ModuleContentEntityType valueOfKey(String value) {
            ModuleContentEntityType entityType = UNKNOWN;
            try {
                entityType = ModuleContentEntityType.valueOf(StringUtils.upperCase(value));
            } catch (Exception e) {
                // Swallow
            }
            return entityType;
        }
    }

    private class ModuleEntryInfoStore implements Serializable {

        private static final long        serialVersionUID = 1L;
        public boolean                   consumed;
        public ModuleEntryCompletionRule completionRule;
        public ContentLink               contentLink;
        public int                       navigatorResId;
        public boolean                   downloadable;

        private ModuleEntryInfoStore(boolean consumed, ModuleEntryCompletionRule completionRule,
                ContentLink contentLink, int navigatorResId, boolean downloadable) {

            this.consumed = consumed;
            this.completionRule = completionRule;
            this.contentLink = contentLink;
            this.navigatorResId = navigatorResId;
            this.downloadable = downloadable;
        }
    }

    private class FetchModuleContentPostProcessor implements ITaskProcessor<Integer> {

        @Override
        public void onTaskPostExecute(boolean success, final Integer result) {
            if (moduleContentProcessDialog != null) {
                moduleContentProcessDialog.dismiss();
            }
            ITaskProcessor<JSONObject> taskProcessor = new ITaskProcessor<JSONObject>() {

                @Override
                public void onTaskStart(JSONObject result) {

                } 
                @Override
                public void onTaskPostExecute(boolean success, JSONObject res) {

                    Log.d(TAG, "response is for the module content: " + res);
                    if (result != null) {
                        findViewById(R.id.module_main_section).setVisibility(View.VISIBLE);
                        onResume();
                    }
                }
            };
            // TODO what is this???
            if (success) {
                AnalyticsSyncer analyticsSyncer = new AnalyticsSyncer(session, null,
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
        if (moduleContentLoader != null) {
            moduleContentLoader.cancel(true);
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);
        Log.d(TAG,
                "Configuartion changed, so recalculating the completion percent and updating progress");
        putCompletionPercent();
    }
}