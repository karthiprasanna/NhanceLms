package com.nhance.android.factory;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;

public class CMDSUrlFactory {

    private final Map<String, String> cmdsUrlMap = new HashMap<String, String>();
    public static final CMDSUrlFactory INSTANCE = new CMDSUrlFactory();
    private static String CMDS_ROUTER = "/tabappRouter/";

    private CMDSUrlFactory() {

        cmdsUrlMap.put("getOrgInfo", "getOrgInfo");
        cmdsUrlMap.put("getOrgCategories", "getOrgCategories");
        cmdsUrlMap.put("getCategorySections", "getCategorySections");
        cmdsUrlMap.put("getOrgMemberExtraInputFields", "getOrgMemberExtraInputFields");
        cmdsUrlMap.put("startTransaction", "startTransaction");
        cmdsUrlMap.put("updateTransaction", "updateTransaction");
        cmdsUrlMap.put("getBuyOrders", "getBuyOrders");
        cmdsUrlMap.put("addOrgMember", "addOrgMember");
        cmdsUrlMap.put("authenticate", "authenticate");
        cmdsUrlMap.put("getSectionByAccessCode", "getSectionByAccessCode");
        cmdsUrlMap.put("addMemberWithAccessCode", "addMemberWithAccessCode");
        cmdsUrlMap.put("addOrgMember", "addOrgMember");
        cmdsUrlMap.put("addOrgMemberMapping", "addOrgMemberMapping");
        cmdsUrlMap.put("getOrgMemberProfile", "getOrgMemberProfile");
        cmdsUrlMap.put("getProgramCourses", "getProgramCourses");
        cmdsUrlMap.put("getMemberProfileWithCourses", "getMemberProfileWithCourses");

        cmdsUrlMap.put("recordLogin", "recordLogin");
        cmdsUrlMap.put("recordLogout", "recordLogout");
        cmdsUrlMap.put("recordActivity", "recordActivity");

        cmdsUrlMap.put("getContentLinks", "getContentLinks");
        cmdsUrlMap.put("getRemovedContentLinks", "getRemovedContentLinks");
        cmdsUrlMap.put("getContentDownloadLink", "getContentDownloadLink");

        cmdsUrlMap.put("getContents", "getContents");
        cmdsUrlMap.put("uploadImage", "uploadImage");
        cmdsUrlMap.put("uploadStatusImage", "uploadStatusImage");

        cmdsUrlMap.put("getEntityQuestionsAttemptStat", "getEntityQuestionsAttemptStat");
        cmdsUrlMap.put("syncTabletAnalytics", "syncTabletAnalytics");
        cmdsUrlMap.put("getAttemptedEntities", "getAttemptedEntities");
        cmdsUrlMap.put("getQuestionsSolutions", "getQuestionsSolutions");

        // only online features
        cmdsUrlMap.put("getEntityLeaderBoard", "getEntityLeaderBoard");
        cmdsUrlMap.put("getUserEntityRank", "getUserEntityRank");
        cmdsUrlMap.put("addDiscussion", "addDiscussion");
        cmdsUrlMap.put("getDiscussions", "getDiscussions");
        cmdsUrlMap.put("getSimilarDiscussions", "getSimilarDiscussions");
        cmdsUrlMap.put("addComment", "addComment");
        cmdsUrlMap.put("getComments", "getComments");
        cmdsUrlMap.put("follow", "follow");
        cmdsUrlMap.put("unFollow", "unFollow");
        cmdsUrlMap.put("upVote", "upVote");
        cmdsUrlMap.put("view", "view");
        cmdsUrlMap.put("getFollowers", "getFollowers");
        cmdsUrlMap.put("getBoards", "getBoards");

        // Module Operations
        cmdsUrlMap.put("moduleEntryStatusSyncer", "moduleEntryStatusSyncer");

        // api for fetching use test analytics data for teacher view
        cmdsUrlMap.put("getTestInfo", "getTestInfo");
        cmdsUrlMap.put("getEntityMarkDistribution", "getEntityMarkDistribution");
        cmdsUrlMap.put("getEntityQuestionAttempts", "getEntityQuestionAttempts");
        cmdsUrlMap.put("getEntityScheduleAnalytics", "getEntityScheduleAnalytics");
        //question attempt api
         cmdsUrlMap.put("getQuestions","getQuestions");
        cmdsUrlMap.put("getQuestionstat","getQuestionstat");

        // for verification of access code
        cmdsUrlMap.put("verifyAccessCode", "verifyAccessCode");

        //for fetching recent activities
        cmdsUrlMap.put("getActivityFeeds", "getActivityFeeds");
        cmdsUrlMap.put("getOlderActivityFeeds", "getOlderActivityFeeds");
        cmdsUrlMap.put("getStatusFeed", "getStatusFeed");

        //for posting status feed
        cmdsUrlMap.put("addStatusFeed", "addStatusFeed");

        //for fetching upVotes and uploading upVote in recent activities
        cmdsUrlMap.put("upVote", "upVote");
        cmdsUrlMap.put("getVoters", "getVoters");

        //For comments getting and uploading comments in recent activities page
        cmdsUrlMap.put("getComments", "getComments");

        //for deleting status feed
        cmdsUrlMap.put("statusFeedDelete", "statusFeedDelete");

        //for getting notifications
        cmdsUrlMap.put("getNotifcationsSummary", "getNotifcationsSummary");
        cmdsUrlMap.put("getNotifcations", "getNotifcations");
        cmdsUrlMap.put("getOlderNotifcations", "getOlderNotifcations");

        //for getting single doubt
        cmdsUrlMap.put("getDiscussionInfo", "getDiscussionInfo");

        //for posting pushId to server
        cmdsUrlMap.put("updateDevicePushId", "updateDevicePushId");


        //Message module
        cmdsUrlMap.put("getConversationSummaries", "getConversationSummaries");
        cmdsUrlMap.put("getConversationSummary", "getConversationSummary");
        cmdsUrlMap.put("getMessage", "getMessage");
        cmdsUrlMap.put("getMessageSummaries", "getMessageSummaries");
        cmdsUrlMap.put("sendMessage", "sendMessage");
        cmdsUrlMap.put("getUserMailBoxInfo", "getUserMailBoxInfo");
        cmdsUrlMap.put("getMembers", "getMembers");
        cmdsUrlMap.put("markConversation", "markConversation");
        cmdsUrlMap.put("deleteConversation", "deleteConversation");



        //Challenge Arena module
        cmdsUrlMap.put("getChannels", "getChannels");
        cmdsUrlMap.put("getChallenges", "getChallenges");
        cmdsUrlMap.put("getChallengeDetails", "getChallengeDetails");
        cmdsUrlMap.put("attemptChallenge","attemptChallenge");
        cmdsUrlMap.put("getChallengeInfo","getChallengeInfo");
        cmdsUrlMap.put("getHint","getHint");
        cmdsUrlMap.put("getChallengeUserAttemptInfo","getChallengeUserAttemptInfo");
        cmdsUrlMap.put("getChallengeGlobalLeaderBoard","getChallengeGlobalLeaderBoard");
        cmdsUrlMap.put("getChallengeLeaderBoard","getChallengeLeaderBoard");



        // api for fetching use assignment analytics data for teacher view

        cmdsUrlMap.put("getAssignmentInfo", "getAssignmentInfo");


        cmdsUrlMap.put("getUserEntityQuestionAttempts", "getUserEntityQuestionAttempts");
        cmdsUrlMap.put("recordAttempt", "recordAttempt");
        cmdsUrlMap.put("getUserEntityMeasures", "getUserEntityMeasures");
        cmdsUrlMap.put("getQuestionInfo", "getQuestionInfo");

        cmdsUrlMap.put("getSolutions", "getSolutions");
        cmdsUrlMap.put("getEntityMeasures", "getEntityMeasures");
        cmdsUrlMap.put("getQuestionAnalytics", "getQuestionAnalytics");
        cmdsUrlMap.put("getQuestionInfo", "getQuestionInfo");

//assignment get comment api
        cmdsUrlMap.put("getComments", "getComments");
        cmdsUrlMap.put("addSolution", "addSolution");


    }

    public String getCMDSUrl(String cmdsHost, String api) {

        String cmdsUrl = cmdsUrlMap.get(api);
        if (!TextUtils.isEmpty(cmdsUrl)) {
            cmdsUrl = cmdsHost + CMDS_ROUTER + cmdsUrl;
        }
        return cmdsUrl;
    }

}
