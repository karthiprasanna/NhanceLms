package com.nhance.android.fragments.library;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nhance.android.async.tasks.AbstractVedantuAsynTask;
import com.nhance.android.async.tasks.ContentDownloaderProcessor;
import com.nhance.android.async.tasks.DownloadProgressTrackerTask;
import com.nhance.android.async.tasks.IPostDownloadProgressTracker;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.AnalyticsDataManager;
import com.nhance.android.db.datamanagers.BoardDataManager;
import com.nhance.android.db.datamanagers.ContentDataManager;
import com.nhance.android.db.datamanagers.DownloadHistoryManager;
import com.nhance.android.db.datamanagers.SDCardFileMetadataManager;
import com.nhance.android.db.models.DownloadHistory;
import com.nhance.android.db.models.SDCardFileMetadata;
import com.nhance.android.downloader.DownloadInfo.DownloadState;
import com.nhance.android.enums.EntityType;
import com.nhance.android.library.utils.LibraryUtils;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.pojos.BoardTree;
import com.nhance.android.pojos.LibraryContentRes;
import com.nhance.android.pojos.content.infos.ModuleBasicInfo;
import com.nhance.android.utils.ViewUtils;
import com.nhance.android.R;

public class LibraryInfoDialogFragment extends DialogFragment implements
        IPostDownloadProgressTracker {

    private static final String  TAG = "LibraryInfoDialogFragment";
    private View                 popupLayout;
    private LibraryContentRes    contentResForPopup;
    private String               contentLinkId;
    private AnalyticsDataManager analyticsDataManager;
    private ILibraryUpdater      iLibraryUpdaterInstance;
    private int                  position;
    private SDCardFileMetadata   contentSDCardFileMetadata;
    private SessionManager       sessionManager;

    public interface ILibraryUpdater {

        public void updateListViewItem(int position, boolean finalStarStatus, EntityType type);
    }

    public void setILibraryUpdaterInstance(ILibraryUpdater iLibraryUpdaterInstance) {

        this.iLibraryUpdaterInstance = iLibraryUpdaterInstance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        setStyle(STYLE_NO_TITLE, R.style.DialogFragmentStyle);
        setCancelable(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {

        super.onStart();
        getDialog().getWindow().setLayout(
                ViewUtils.getOrientationSpecificWidth(getActivity()),
                LayoutParams.WRAP_CONTENT);
        getDialog().setCanceledOnTouchOutside(true);
    }

    private boolean isTestAndIsAttempted;

    @Override
    public View
            onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        sessionManager = SessionManager.getInstance(getActivity());
        if (getArguments() != null) {
            contentLinkId = getArguments().getString(ConstantGlobal.CONTENT_ID);
            position = getArguments().getInt(ConstantGlobal.POSITION);
            isTestAndIsAttempted = getArguments().getBoolean(ConstantGlobal.ATTEMPTED, false);
        }
        if (StringUtils.isEmpty(contentLinkId)) {
            dismiss();
            Toast.makeText(getActivity(), "Some error in fetching the info",
                    Toast.LENGTH_SHORT).show();
        }

        analyticsDataManager = new AnalyticsDataManager(getActivity());
        popupLayout = inflater.inflate(R.layout.library_info_popup, container, false);
        contentResForPopup = new ContentDataManager(getActivity())
                .getLibraryContentRes(contentLinkId);
        if (getArguments() != null) {
            contentSDCardFileMetadata = (new SDCardFileMetadataManager(getActivity()))
                    .getSDCardFileMetadata(
                            sessionManager.getSessionIntValue(ConstantGlobal.ORG_KEY_ID),
                            sessionManager.getSessionStringValue(ConstantGlobal.USER_ID),
                            getArguments().getString(ConstantGlobal.SECTION_ID),
                            EntityType.SECTION.name(), contentResForPopup.id,
                            contentResForPopup.type);
        }
        Log.d(TAG, "Fetched Content Res:" + contentResForPopup);
        return popupLayout;
    }

    @Override
    public void onActivityCreated(Bundle arg0) {

        super.onActivityCreated(arg0);
        String contentType = contentResForPopup.type;
        ImageView thumbnailView = (ImageView) popupLayout
                .findViewById(R.id.library_info_popup_image);
        TextView titleView = (TextView) popupLayout.findViewById(R.id.library_info_popup_title);
        TextView authorView = (TextView) popupLayout.findViewById(R.id.library_info_popup_author);

        titleView.setText(contentResForPopup.name);
        authorView.setText("by " + contentResForPopup.ownerName);
        final EntityType type = EntityType.valueOfKey(contentType);
        if (contentResForPopup.type.equalsIgnoreCase(EntityType.TEST.name())
                && analyticsDataManager.getTestAnalytics(
                        sessionManager.getSessionIntValue(ConstantGlobal.ORG_KEY_ID),
                        sessionManager.getSessionStringValue(ConstantGlobal.USER_ID),
                        contentResForPopup.id, EntityType.TEST.name()) != null) {
            thumbnailView.setImageResource(R.drawable.icon_test_attempted);
        } else {
            thumbnailView.setImageResource(type.icon_res_id);
        }

        // changing the text of the open button for entity type test only
        if (type.equals(EntityType.TEST)) {

            TextView openView = (TextView) popupLayout.findViewById(R.id.open_library_popup_item);
            boolean isStudent = LibraryUtils._amIStudent(getActivity());
            if (!isStudent) {
                openView.setText("View Ananlytics");
            } else if (isTestAndIsAttempted) {
                openView.setText("View Results");
            } else {
                openView.setText("Attempt Now");
            }
        } else if (type.equals(EntityType.MODULE)) {
         //   thumbnailView.setBackgroundResource(R.color.darkgreen);
            TextView moduleRun = (TextView) popupLayout
                    .findViewById(R.id.library_info_popup_module_run);
            moduleRun.setVisibility(View.VISIBLE);
            ModuleBasicInfo moduleInfo = (ModuleBasicInfo) contentResForPopup.toContentBasicInfo();
            String moduleRunText = "Module Run: " + moduleInfo.moduleRun.displayName;
            moduleRun.setText(moduleRunText);
        }

        TextView starView = (TextView) popupLayout.findViewById(R.id.star_this_content);
        setStarView(starView, contentResForPopup.starred);
        starView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                ContentDataManager contentDataManager = new ContentDataManager(getActivity());
                boolean finalStarStatus = false;
                if (contentResForPopup.starred) {
                    finalStarStatus = false;
                } else {
                    finalStarStatus = true;
                }
                setStarView((TextView) v, finalStarStatus);
                contentDataManager.starContent(contentResForPopup._id, finalStarStatus);
                contentResForPopup.starred = finalStarStatus;
                if (iLibraryUpdaterInstance != null) {
                    iLibraryUpdaterInstance.updateListViewItem(position, finalStarStatus, type);
                }
            }
        });

        boolean isDownloaded = contentResForPopup.downloaded;

        if (getArguments() != null && getArguments().containsKey(ConstantGlobal.DOWNLOADABLE)) {
            contentResForPopup.downloadable = getArguments()
                    .getBoolean(ConstantGlobal.DOWNLOADABLE);
        }

        if (!contentResForPopup.downloadable) {
            popupLayout.findViewById(R.id.status_type).setVisibility(View.VISIBLE);
        }

        final TextView downloadButton = (TextView) popupLayout
                .findViewById(R.id.library_info_popup_download_btn);

        if (contentResForPopup.downloadable && !isDownloaded) {
            setDownloadToDeviceButton(downloadButton);
            DownloadHistoryManager dhManager = new DownloadHistoryManager(getActivity());
            DownloadHistory downloadHistory = dhManager.getDownloadHistory(contentResForPopup._id);
            if (downloadHistory != null) {
                int status = downloadHistory.status;
                if (status == DownloadState.PAUSED.toInt()
                        || status == DownloadState.STARTED.toInt()
                        || status == DownloadState.TOSTART.toInt()) {
                    setDownloadRunning(downloadButton, contentResForPopup._id);
                }
            }
        } else if (isDownloaded) {
            setRemoveFromDeviceButton(downloadButton);
            popupLayout.findViewById(R.id.status_type).setVisibility(View.GONE);
        }

        downloadButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (contentResForPopup == null) {
                    return;
                }

                final ContentDownloaderProcessor cDownloaderProcessor = new ContentDownloaderProcessor(
                        getActivity().getApplicationContext(), contentResForPopup);

                if (contentResForPopup.downloaded) {
                    Log.d(TAG, "content is already downloaded");
                    AlertDialog dialog = new AlertDialog.Builder(getActivity())
                            .setTitle("Remove downloaded content")
                            .setMessage("Remove " + contentResForPopup.name)
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    dialog.cancel();
                                }
                            }).setNeutralButton("Remove", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    boolean removed = contentResForPopup.downloaded = cDownloaderProcessor
                                            .removeFromLocalDisk();
                                    if (contentResForPopup.downloadable && removed) {
                                        setDownloadToDeviceButton(downloadButton);
                                        contentResForPopup.downloaded = false;
                                        ((TextView) popupLayout.findViewById(R.id.status_type))
                                                .setText(R.string.online);
                                    } else if (!contentResForPopup.downloadable && removed) {
                                        downloadButton.setVisibility(View.GONE);
                                        popupLayout.findViewById(R.id.status_type).setVisibility(
                                                View.VISIBLE);
                                        ((TextView) popupLayout.findViewById(R.id.status_type))
                                                .setText(R.string.online);
                                        if (contentSDCardFileMetadata != null
                                                && contentSDCardFileMetadata.active) {
                                            ((TextView) popupLayout.findViewById(R.id.status_type))
                                                    .setText(getActivity()
                                                            .getResources()
                                                            .getString(
                                                                    R.string.library_info_pop_up_content_on_sdcard));
                                        }
                                    } else {
                                        Toast.makeText(getActivity(),
                                                R.string.removed_from_device, Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                    dialog.cancel();
                                }

                            }).create();
                    dialog.show();

                } else if (contentResForPopup.downloadable && !contentResForPopup.downloaded) {
                    Log.d(TAG, "starting content download");
                    final View loadingView = popupLayout.findViewById(R.id.start_download_loading);
                    downloadButton.setVisibility(View.GONE);
                    loadingView.setVisibility(View.VISIBLE);

                    AbstractVedantuAsynTask<Void, Void, Boolean> task = new AbstractVedantuAsynTask<Void, Void, Boolean>() {

                        String errorMgs = null;

                        @Override
                        protected void onPreExecute() {

                            super.onPreExecute();
                        }

                        @Override
                        protected Boolean doInBackground(Void... params) {

                            Log.d(TAG, "starting content download");
                            return cDownloaderProcessor.startDownload(false);
                        }

                        @Override
                        protected void onPostExecute(Boolean result) {

                            super.onPostExecute(result);
                            if (result == null) {
                                if (downlodDownloadProgressTrackerTask != null) {
                                    downlodDownloadProgressTrackerTask.cancel(true);
                                }
                            }
                            if (!result) {
                                errorMgs = cDownloaderProcessor.errorMsg;
                            }

                            loadingView.setVisibility(View.GONE);
                            if (!result) {
                                Toast.makeText(getActivity(), errorMgs, Toast.LENGTH_SHORT)
                                        .show();
                                downloadButton.setVisibility(View.VISIBLE);
                                setDownloadToDeviceButton(downloadButton);

                                loadingView.setVisibility(View.GONE);

                                if (downlodDownloadProgressTrackerTask != null) {
                                    downlodDownloadProgressTrackerTask.cancel(true);
                                }

                            } else {
                                setDownloadRunning(downloadButton, contentResForPopup._id);
                            }
                        }

                    };

                    task.executeTask(false);
                }
            }
        });

        TextView lastOpenedView = (TextView) popupLayout
                .findViewById(R.id.library_info_popup_last_opened);
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy", Locale.US);
        long viewedTime = Long.valueOf(contentResForPopup.lastViewed);
        String formattedDate = formatter.format(new Date(viewedTime));
        if (viewedTime > 0) {
            lastOpenedView.setText("Last Opened: " + formattedDate);
            lastOpenedView.setVisibility(View.VISIBLE);
        }
        BoardDataManager boardDataManager = new BoardDataManager(getActivity());
        List<BoardTree> boardTrees = boardDataManager.getBoardTree(contentResForPopup);
        LinearLayout coursesInfoHolder = (LinearLayout) popupLayout
                .findViewById(R.id.library_info_popup_courses_info);
        if (boardTrees != null && !boardTrees.isEmpty()) {

            for (int p = 0; p < boardTrees.size(); p++) {
                BoardTree course = boardTrees.get(p);
                TextView courseView = (TextView) ((LayoutInflater) getActivity()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
                        R.layout.library_info_popup_course_topic, coursesInfoHolder, false);
                courseView.setText(course.name);
                coursesInfoHolder.addView(courseView);

                List<BoardTree> topicsList = course.children;
                if (topicsList != null && topicsList.size() > 0) {
                    String topics = "";
                    int topicsSize = topicsList.size();
                    for (int k = 0; k < topicsSize; k++) {
                        topics += topicsList.get(k).name + ((k < (topicsSize - 1)) ? ", " : "");
                    }
                    TextView topicsView = (TextView) ((LayoutInflater) getActivity()
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
                            R.layout.library_info_popup_course_topic, coursesInfoHolder, false);
                    topicsView.setText(topics);
                    topicsView.setTextColor(getResources().getColor(R.color.lightgrey));
                    coursesInfoHolder.addView(topicsView);
                }
            }

        }

        TextView statsView = (TextView) popupLayout.findViewById(R.id.library_info_popup_stats);
        LibraryUtils.setStatsView(getActivity(), contentResForPopup, statsView, type, true);

        ImageView closePopup = (ImageView) popupLayout.findViewById(R.id.close_library_info_popup);
        closePopup.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                dismiss();
            }
        });

        TextView openLibraryItem = (TextView) popupLayout
                .findViewById(R.id.open_library_popup_item);
        if (getArguments() != null && getArguments().getBoolean("RESTRICT_ACCESS", false)) {
            openLibraryItem.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    Toast.makeText(getActivity(),
                            "Please consume the content in sequential order", Toast.LENGTH_SHORT)
                            .show();
                }
            });
        } else {
            openLibraryItem.setTag(contentResForPopup);
            openLibraryItem.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    if (getArguments() != null
                            && StringUtils.isNotEmpty(getArguments().getString(
                                    ConstantGlobal.MODULE_ID))) {
                        LibraryUtils.onLibraryItemClickListnerImpl(getActivity(),
                                contentResForPopup,
                                getArguments().getString(ConstantGlobal.MODULE_ID));
                    } else {
                        LibraryUtils.onLibraryItemClickListnerImpl(getActivity(),
                                (LibraryContentRes) v.getTag());
                    }

                }
            });
        }

        if (contentSDCardFileMetadata != null) {
            TextView cardInfoTextView = (TextView) popupLayout
                    .findViewById(R.id.library_info_popup_sdcard_info);
            cardInfoTextView.setVisibility(View.VISIBLE);
            String cardInfoText = getActivity().getResources().getString(
                    R.string.library_info_pop_up_content_current_sdcard);
            if (!contentSDCardFileMetadata.active) {
                cardInfoText = getActivity().getResources().getString(
                        R.string.library_info_pop_up_content_other_sdcard)
                        + " " + contentSDCardFileMetadata.cardName;
            }
            cardInfoTextView.setText(cardInfoText);
            if (!contentResForPopup.downloadable && contentSDCardFileMetadata.active) {
                popupLayout.findViewById(R.id.status_type).setVisibility(View.VISIBLE);
                ((TextView) popupLayout.findViewById(R.id.status_type))
                        .setText(getActivity().getResources().getString(
                                R.string.library_info_pop_up_content_on_sdcard));
            }
            //TODO change the image according to sd card
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {

        if (downlodDownloadProgressTrackerTask != null) {
            downlodDownloadProgressTrackerTask.cancel(true);
        }
        super.onDismiss(dialog);
    }

    @Override
    public void onProgressComplete(DownloadHistory downloadHistory) {

        popupLayout.findViewById(R.id.library_info_popup_download_progress)
                .setVisibility(View.GONE);
        if (downlodDownloadProgressTrackerTask != null) {
            downlodDownloadProgressTrackerTask.cancel(true);
        }
        if (downloadHistory != null) {
            int status = downloadHistory.status;
            TextView downloadButton = (TextView) popupLayout
                    .findViewById(R.id.library_info_popup_download_btn);
            if (status == DownloadState.FINISHED.toInt()) {
                downloadButton.setVisibility(View.VISIBLE);
                setRemoveFromDeviceButton(downloadButton);
                contentResForPopup.downloaded = true;
                ((TextView) popupLayout.findViewById(R.id.status_type)).setText(R.string.on_device);
            } else if (status == DownloadState.STOPPED.toInt()) {
                downloadButton.setVisibility(View.VISIBLE);
                setDownloadToDeviceButton(downloadButton);
            }
        }
    }

    private void setDownloadToDeviceButton(TextView downloadButton) {

        downloadButton.setVisibility(View.VISIBLE);
        downloadButton.setText(R.string.download_to_device);
        //downloadButton.setBackgroundResource(R.color.blue);
        downloadButton.setTextColor(getResources().getColor(R.color.white));
    }

    private void setRemoveFromDeviceButton(TextView downloadButton) {

        downloadButton.setVisibility(View.VISIBLE);
        downloadButton.setText(R.string.remove_from_device);
       // downloadButton.setBackgroundResource(R.color.light_gray);
        downloadButton.setTextColor(getResources().getColor(R.color.darkergrey));
    }

    DownloadProgressTrackerTask downlodDownloadProgressTrackerTask;

    private void setDownloadRunning(final TextView downloadButton, final int contentId) {

        // commented by Shankar
        // downloadButton.setText(R.string.download_under_progress);
        // downloadButton.setTextColor(getContext().getResources().getColor(R.color.darkestgrey));
        // downloadButton.setBackgroundResource(R.color.white);

        downloadButton.setVisibility(View.GONE);
        final View progresslayout = popupLayout
                .findViewById(R.id.library_info_popup_download_progress);
        progresslayout.setVisibility(View.VISIBLE);
        downlodDownloadProgressTrackerTask = new DownloadProgressTrackerTask(
                SessionManager.getInstance(getActivity()),
                (ProgressBar) progresslayout.findViewById(R.id.progress_bar),
                (TextView) progresslayout.findViewById(R.id.progress_text), this);
        downlodDownloadProgressTrackerTask.execute(contentId);
        popupLayout.findViewById(R.id.library_info_popup_cancel_download_btn).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        DownloadHistoryManager dHistoryManager = new DownloadHistoryManager(
                                getActivity());
                        DownloadHistory dhiHistory = dHistoryManager.getDownloadHistory(contentId);
                        if (dhiHistory != null) {
                            dHistoryManager.deleteDownloadHistory(dhiHistory._id);
                            if (StringUtils.isNotEmpty(dhiHistory.file))
                                new File(dhiHistory.file).delete();
                        }

                        downloadButton.setVisibility(View.VISIBLE);
                        setDownloadToDeviceButton(downloadButton);
                        progresslayout.setVisibility(View.GONE);

                        if (downlodDownloadProgressTrackerTask != null) {
                            downlodDownloadProgressTrackerTask.cancel(true);
                        }
                    }
                });
    }

    private void setStarView(TextView starView, boolean isStarred) {

        int starredResId = 0;
        int starredTextResId = 0;
        if (isStarred) {
            starredResId = R.drawable.star_selected;
            starredTextResId = R.string.unstar;

        } else {
            starredResId = R.drawable.star;
            starredTextResId = R.string.star;
        }
        starView.setCompoundDrawablesWithIntrinsicBounds(starredResId, 0, 0, 0);
        starView.setText(starredTextResId);
    }

}
