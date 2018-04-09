package com.nhance.android.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import android.widget.ProgressBar;

import com.nhance.android.async.tasks.ITaskProcessor;
import com.nhance.android.async.tasks.LibraryLoader;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.enums.EntityType;
import com.nhance.android.pojos.OrgMemberInfo;
import com.nhance.android.pojos.OrgMemberMappingInfo.OrgProgramBasicInfo;

public class LibraryManager {

    private static final String   TAG = "LibraryManager";
    private final SessionManager  session;

    private static LibraryManager INSTANCE;

    private LibraryManager(Context context) {

        super();
        session = SessionManager.getInstance(context);
    }

    public static LibraryManager getInstance(Context context) {

        if (INSTANCE == null) {
            INSTANCE = new LibraryManager(context);
        }
        return INSTANCE;
    }

    public List<LibraryLoader> fetchLibraryContentsFromCMDS(ProgressBar updateContainer,
            OrgMemberInfo orgMemberInfo, boolean noExecute) {

        return fetchLibraryContentsFromCMDS(updateContainer, orgMemberInfo, null, noExecute);
    }

    public List<LibraryLoader> fetchLibraryContentsFromCMDS(ProgressBar updateContainer,
            OrgMemberInfo orgMemberInfo, ITaskProcessor<Integer> postExecuteProcessor,
            boolean noExecute) {

        List<LibraryLoader> loaders = new ArrayList<LibraryLoader>();
        if (orgMemberInfo != null) {
            for (OrgProgramBasicInfo orgProg : orgMemberInfo.mappings.programs) {
                for (String sectionId : orgProg._getSectionIds()) {
                    Log.d(TAG, "fetching library content for section:" + sectionId);
                    loaders.add(fetchLibraryContentFromCMDS(updateContainer, orgMemberInfo.userId,
                            sectionId, EntityType.SECTION.name(), true, postExecuteProcessor,
                            noExecute));
                }
            }
        }
        return loaders;

    }

    public LibraryLoader fetchLibraryContentFromCMDS(ProgressBar progressContainer, String userId,
            String targetId, String targetType, boolean fetchContent,
            ITaskProcessor<Integer> postExecuteProcessor) {

        return fetchLibraryContentFromCMDS(progressContainer, userId, targetId, targetType,
                fetchContent, postExecuteProcessor, false);
    }

    public LibraryLoader fetchLibraryContentFromCMDS(ProgressBar progressContainer, String userId,
            String targetId, String targetType, boolean fetchContent,
            ITaskProcessor<Integer> postExecuteProcessor, boolean noExecute) {

        String cmdsUrl = session.getSessionStringValue(ConstantGlobal.CMDS_URL);
        Map<String, Object> httpParams = new HashMap<String, Object>();
        httpParams.put("target.id", targetId);
        httpParams.put("target.type", targetType);
        httpParams.put("linkType", "ADDED");
        httpParams.put("orderBy", ConstantGlobal.TIME_CREATED);
        httpParams.put("targetUserId", userId);
        httpParams.put("addContent", fetchContent);
        session.addSessionParams(httpParams);
        LibraryLoader loader = new LibraryLoader(session, progressContainer, httpParams,
                postExecuteProcessor, targetId, targetType);
        if (!noExecute) {
            loader.executeTask(false, cmdsUrl);
        }
        return loader;
    }

}
