package com.nhance.android.notifications;

import android.util.Log;

import com.nhance.android.managers.SessionManager;
import com.nhance.android.pojos.OrgMemberInfo;
import com.nhance.android.recentActivities.Actor;
import com.nhance.android.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by prathibha on 6/15/2017.
 */

public class NewsFeed {


        String _ACTOR = "%{ACTOR}";
        String _ENTITY = "%{ENTITY}";
        String _ENTITY_NON_CLICK = "%{ENTITY_NON_CLICK}";
        String _REASON = "%{REASON}";
        String _ENTITY_OWNER = "%{ENTITY_OWNER}";
        String _ENTITY_OWNER_PLURAL = "%{ENTITY_OWNER_PLURAL}";
        String _ROOT_OWNER = "%{ROOT_OWNER}";
        String _ROOT_ENTITY = "%{ROOT_ENTITY}";
        String _AORAN = "%{A/AN}";
        String[] VOWELS = {"A","E","I","O","U"};
        String[] SUPPORTED_TYPES = {"VOTED","ADDED","COMMENTED","ATTEMPTED","FOLLOWED","SHARED","MADE_VISIBLE","ENDED"};
        private HashMap<String, String> textHtml;
        private String textHtml1;
        private JSONObject actor;
        private JSONArray feeds;
        private String reason;
        private SessionManager sessionManager1;


    public String process(String feedLocation, JSONObject feed, String actionType, boolean clustered, SessionManager sessionManager){
              feeds = new JSONArray();
              feeds.put(feed);
              sessionManager1 = sessionManager;
            if(clustered){
                String lastFeedId = null;
                try {
                 lastFeedId = feed.getString("lastNewsFeedId");
                feeds = feed.getJSONArray("clusteredNews");
                feed = feed.getJSONArray("clusteredNews").getJSONObject(0);

                feed.put("newsFeedId", lastFeedId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        JSONObject src = null;
        try {
            src = feed.getJSONObject("src");
            String eventType = feed.getString("eType");
            textHtml = getActionTypeText(actionType,src.getString("type"),eventType,feed);
            Log.e("textHtml..", textHtml.toString());
            JSONObject root = src.optJSONObject("rootDetails");
            actor = feed.getJSONObject("actor");
            reason = feed.getString("why");
            if(root != null){
              textHtml1 = String.valueOf(textHtml.get("ROOT"));
                if(reason.equalsIgnoreCase("ROOT_OWNER")){
                    textHtml1 = textHtml1.replace(_ROOT_OWNER,StringUtils.TXT_YOUR);
                }else{
                    textHtml1 = textHtml1.replace(_ROOT_OWNER,"");
                }
                textHtml1 = textHtml1.replace(_ROOT_ENTITY,getSrcEntity(root,feed,true));
            }else{
                textHtml1 = (String) textHtml.get("SRC");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

            if(src != null){
                String user = null;
                try {
                    user = getOwnerUser_Singular(feed.getJSONObject("srcOwner"));
                    textHtml1 = textHtml1.replace(_ENTITY_OWNER,user);
                    if(textHtml1.contains(_ENTITY_OWNER_PLURAL)){
                        String user_Plural = getOwnerUser_Plural(feed.getJSONObject("srcOwner"));
                        textHtml1 = textHtml1.replace(_ENTITY_OWNER_PLURAL,user_Plural);
                    }
                    String entity = getSrcEntity(src,feed,true);

                    textHtml1 = textHtml1.replace(_ENTITY,entity);
                    if(textHtml1.contains(_ENTITY_NON_CLICK)){
                        String entityNonClick = getSrcEntity(src,feed,false);
                        textHtml1 = textHtml1.replace(_ENTITY_NON_CLICK,entityNonClick);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            if(actor != null){
                Log.e("textHtml0", textHtml1);
                String actorHTML = StringUtils.TXT_A_MEMBER;
                if(feeds != null && feeds.length()>0){
                    actorHTML = getActor(actor, sessionManager1.getOrgMemberInfo().id);
                    for(int a = 0 ; a <feeds.length() ; a++){
                        JSONObject actor_n = null;
                        try {
                            actor_n = feeds.getJSONObject(a).getJSONObject("actor");
                            if(!actor_n.getString("id").equalsIgnoreCase(actor.getString("id"))){

                                actorHTML += ", "+getActor(actor_n, sessionManager1.getOrgMemberInfo().id);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                    actorHTML = actorHTML.replace("/", "[^,]*)$/, & $1");
                }else{
                    actorHTML = getActor(actor, sessionManager1.getOrgMemberInfo().id);
                }
                textHtml1 = textHtml1.replace(_ACTOR,actorHTML);
                Log.e("textHtml1", textHtml1);
            }
            if(textHtml1.contains(_REASON)){
                String reasonText = "";
                OrgMemberInfo me = sessionManager.getOrgMemberInfo();
//                var me = {"id":USERID,"profile":me.userRole};
                JSONObject object = new JSONObject();
                try {
                    object.put("firstName", me.firstName);
                    object.put("lastName", me.lastName);
                    object.put("id", me.id);
                    object.put("profile", me.profile);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String you = getActor(object, me.userId);
                Log.e("reason", ""+reason);
                switch(reason){
                    case "ATTEMPTED" : reasonText = StringUtils.ATTEMPTED_BY+" "+you;
                        break;
                    case "ROOT_OWNER" :
                    case "OWNER" : reasonText = StringUtils.ADDED_BY+" "+you;
                        break;
                    case "SHARED_WITH" : reasonText = StringUtils.SHARED_BY+" "+you;
                        break;
                    case "COMMENTED" : reasonText = StringUtils.COMMENTED_BY+" "+you;
                        break;
                    case "FOLLOWING_SOURCE" : reasonText = StringUtils.FOLLOWED_BY+" "+you;
                        break;
                    case "ADDED_SOLUTION" : reasonText = StringUtils.SOLUTION_ADDED_BY+" "+you;
                        break;
                }
                textHtml1 = textHtml1.replace(_REASON,reasonText);
            }
            if(textHtml1.contains(_AORAN)){
                String A = "a";
                String text = textHtml1.trim();
                Log.e("text1", text+".."+text.indexOf(_AORAN) );
                String nextAlphabet = text.substring(text.indexOf(_AORAN) + _AORAN.length() + 1, text.indexOf(_AORAN) + _AORAN.length() + 1+1);

                Log.e("text2", text+".."+text.indexOf(_AORAN)+"...."+nextAlphabet );
                if(nextAlphabet != null){
                    nextAlphabet = nextAlphabet.toUpperCase();
                    if(Arrays.asList(VOWELS).indexOf(nextAlphabet)>=0){
                        A = "an";
                    }
                }
                textHtml1 = textHtml1.replace(_AORAN,A);
            }
        Object html;
       /* switch(feedLocation){
                case "NOTIFICATIONS" :
                    html = notiInsert(textHtml,feed);
                    break;
                case "ACTIVITIES" :
                    html = activityInsert(textHtml,feed);
                    break;
            };*/
            return textHtml1;
        };
	/*this.isSupported = function(actionType){
            if(SUPPORTED_TYPES.indexOf(actionType)>=0){
                return true;
            }else{
                return false;
            }
        };*/
    public String getSrcEntity(JSONObject src, JSONObject feed, boolean b){
        NewsEntityProcessor entityProcessor = new NewsEntityProcessor();
        String result = null;

        try {
            Log.e("src.getString", src.getString("type"));
            switch(src.getString("type")){

                case "DISCUSSION" :
                    result = entityProcessor.getDoubt(src);
                    break;
                case "COMMENT" :
                    result =  entityProcessor.getComment(src);
                    break;
                case "TEST" :
                    result =  entityProcessor.getTest(src);
                    break;
                case "ASSIGNMENT" :
                    result =  entityProcessor.getAssignment(src);
                    break;
                case "QUESTION" :
                    result =  entityProcessor.getQuestion(src);
                    break;
                case "CHALLENGE" :
                    result =  entityProcessor.getChallenge(src);
                    break;
                case "STATUSFEED" :
                    result =  entityProcessor.getStatusFeed(src, feed.getString("newsFeedId"));
                    break;
                case "REMARK" :
                    result =  entityProcessor.getRemark(src,feed.getJSONObject("srcOwner"));
                    break;
                case "SOLUTION" :
                    result =  entityProcessor.getSolution(src);
                    break;
                case "VIDEO" : result =  entityProcessor.getVideo(src);
                    break;
                case "DOCUMENT" : result =  entityProcessor.getDoc(src);
                    break;
                case "FILE" : result =  entityProcessor.getFile(src);
                    break;
                case "HTMLCONTENT" : result =  entityProcessor.getHtmlContent(src);
                    break;

                case "MODULE" : result =  entityProcessor.getModule(src);
                    break;
                //case "PLAYLIST" : return getPlaylist(src);
                default : return "Entity";

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
    private HashMap<String, String> getActionTypeText(String actionType, String srcType, String eType, JSONObject feed){
        String langString_ROOT = null;
        String langString_SRC = null;
        Log.e("in actionType",".."+actionType);
        switch(actionType){
            case "VOTE_ENTITY" :
            case "VOTED" :
                langString_ROOT = _ACTOR+" "+ StringUtils.NEWS_FEED_UPVOTED+" "+_ENTITY+" "+StringUtils.NEWS_FEED_ADDED_BY+" "+_ENTITY_OWNER+", "+StringUtils.NEWS_FEED_ON+" "+_ROOT_OWNER+" "+_ROOT_ENTITY;
                langString_SRC = _ACTOR+" "+StringUtils.NEWS_FEED_UPVOTED+" "+_ENTITY+" "+StringUtils.NEWS_FEED_ADDED_BY+" "+_ENTITY_OWNER;
                break;
            case "FOLLOWED" :
                langString_ROOT = _ACTOR+" "+StringUtils.NEWS_FEED_FOLLOWED+" "+_ENTITY_OWNER+"'s "+_ENTITY+" "+StringUtils.NEWS_FEED_ON+" "+_ROOT_OWNER+" "+_ROOT_ENTITY;
                langString_SRC = _ACTOR+" "+StringUtils.NEWS_FEED_FOLLOWING+" "+_ENTITY+" "+StringUtils.NEWS_FEED_ADDED_BY+" "+_ENTITY_OWNER;
                break;
            case "FOLLOWING" :
                langString_ROOT = _ACTOR+" "+StringUtils.NEWS_FEED_FOLLOWED+" "+_ENTITY_OWNER+"'s "+_ENTITY+" "+StringUtils.NEWS_FEED_ON+" "+_ROOT_OWNER+" "+_ROOT_ENTITY;
                langString_SRC = _ACTOR+" "+StringUtils.NEWS_FEED_FOLLOWING+" "+_ENTITY+" "+StringUtils.NEWS_FEED_ADDED_BY+" "+_ENTITY_OWNER;
                break;
            case "ADDED" :
                if(srcType.equalsIgnoreCase("CHALLENGE")){
                    langString_SRC = _ACTOR+" "+StringUtils.NEWS_FEED_ADDED+" "+_AORAN+" "+StringUtils.NEWS_FEED_NEW+" "+_ENTITY_NON_CLICK;
                }else{
                    switch(eType){
                        case "ADD_SOLUTION" :
                            langString_SRC = _ACTOR+" "+StringUtils.NEWS_FEED_ADDED+" "+_AORAN+" "+StringUtils.NEWS_FEED_NEW_SOLUTION_TO+" "+_ENTITY+" "+_REASON;
                            break;
                        default : langString_SRC = _ACTOR+" "+StringUtils.NEWS_FEED_ADDED_A_NEW+" "+_ENTITY;
                            break;
                    }
                }
                langString_ROOT = langString_SRC;
                break;
            case "MADE_VISIBLE" :
                try {

                    if(feed.getJSONObject("actor").getString("id").equals(feed.getJSONObject("srcOwner").getString("id"))){
                        langString_SRC = _ACTOR+" "+StringUtils.NEWS_FEED_ADDED+" "+_AORAN+" "+_ENTITY+" "+StringUtils.NEWS_FEED_ADDED_TO_YOUR_LIB+".";
                    }else{
                        langString_SRC = _ACTOR+" "+StringUtils.NEWS_FEED_ADDED_TO_YOUR_LIB+" "+_AORAN+" "+_ENTITY+", "+StringUtils.NEWS_FEED_CREATED_BY+" "+_ENTITY_OWNER;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                langString_ROOT = langString_SRC;
                break;
            case "SHARED" :
                langString_SRC = langString_ROOT = _ACTOR+" "+StringUtils.NEWS_FEED_SHARED+" "+_AORAN+" "+_ENTITY;
                break;
            case "ATTEMPTED" :
                langString_SRC = langString_ROOT = _ACTOR+" "+StringUtils.NEWS_FEED_ATTEMPTED+" "+_ENTITY+" "+StringUtils.NEWS_FEED_ADDED_BY+" "+_ENTITY_OWNER;
                break;
            case "COMMENTED" :
                String clang =  StringUtils.NEWS_FEED_COMMENTED_ON;
                clang = srcSpecificLang(clang,srcType,actionType);
                clang = " "+clang+" ";
                langString_ROOT = _ACTOR + clang +_ENTITY+" "+StringUtils.ADDED_BY+" "+_ENTITY_OWNER+" "+StringUtils.TXT_ON+" "+_ROOT_OWNER+" "+_ROOT_ENTITY;
                langString_SRC = _ACTOR + clang + _ENTITY+" "+StringUtils.ADDED_BY+" "+_ENTITY_OWNER;
                break;
            case "ASKED" :
                langString_SRC = langString_ROOT = _ACTOR+" "+StringUtils.NEWS_FEED_ASKED+" "+_AORAN+" "+_ENTITY;
                break;
            case "ENDED" :
                langString_SRC = langString_ROOT = StringUtils.NEWS_FEED_RESULT_OF+" "+_ENTITY+", "+_REASON+", "+StringUtils.NEWS_FEED_RESULTS_OUT_CHECK_OUT;
                break;
        };
        langString_SRC += ".";
        langString_ROOT += ".";
        HashMap<String, String> lang = new HashMap<String, String>();
        Log.e("text in added..", langString_SRC);
        lang.put("ROOT", langString_ROOT);
        lang.put("SRC", langString_SRC);
        return lang;
    };
    private String srcSpecificLang(String lang,String srcType,String action){
        switch(action){
            case "COMMENTED" :
                if(srcType.equalsIgnoreCase("DISCUSSION")){
                    lang = StringUtils.NEWS_FEED_ANSWERED;
                }else if(srcType.equalsIgnoreCase("QUESTION")){
                    lang = StringUtils.NEWS_FEED_PROVIDED_SOULTION;
                }
                break;
        }
        return lang;
    }
   /*     var notiInsert = function(textHtml,feed){
            var appendHTML = "<div class='notiFeed' data-feed-id="+feed.newsFeedId+">\n\
                    <div class='postTitle'>"+textHtml+"</div>"+getAgo(feed.time)+"</div>";
            return appendHTML;
        };
        var activityInsert = function(textHtml,feed){
            var appendHTML = "<div class='notiFeed' data-feed-id="+feed.newsActivityId+">\n\
                    <div class='postTitle'>"+textHtml+"</div>"+getAgo(feed.time)+"</div>";
            return appendHTML;
        }
    */
    public String getComment(JSONObject src) throws JSONException{
        //String text = src.content;
        String preText = "comment";
        JSONObject root = src.getJSONObject("rootDetails");
        if(root != null){
            preText = changePreText(root.getString("type"));
        }else{
            preText = changePreText(src.getString("type"));
        }


        String html = preText;
		/*var html = preText+"- \"<span class='greenTextColor singleLineText toolTipTitle displayInBlock' data-title='"+text+"' \n\
				style='max-width:200px;' >"+text+"</span>\"";*/
        return html;
    }
    public String getDoubt(JSONObject src){


        String name = "";
        try {
            name = src.getString("name")!=null?src.getString("name"):src.getString("title");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String aTag = "Doubt - "+name;
        return aTag;

    }
    public String getTest(JSONObject test){

        String data = "Test"+" - ";

        try {
            data = data+test.getString("name");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return data;
    }
    public String getAssignment(JSONObject test){

        String data = "Assignment"+" - ";

        try {
            data = data+test.getString("name");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return data;
    }

    public String changePreText(String type) {

        // TODO Auto-generated method stub

        String result = null;
        switch(type.toUpperCase()){
            case "QUESTION" :
                result = "solution";
                break;
            case "DISCUSSION" :
                result = "answer";
                break;
        }
        return result;
    }
    public String getQuestion(JSONObject ques){
        return "Question";
    }
    public String getChallenge(JSONObject chal) throws JSONException{
        String name = "Challenge"+" - "+chal.getString("name");
        if(chal.getString("name")!=null){
            return name;
        }
        else return "Challenge";
    }
    public String getStatusFeed(JSONObject feed, String newsFeedId){
        String msg = "new status feed";
        try {
            if(feed.getString("statusMessage")!=null){
                msg = "Feed"+" - "+feed.getString("statusMessage");
            }else if(feed.getJSONObject("sourceContent")!=null){
                if(feed.getJSONObject("sourceContent").getString("title")!=null){
                    msg = "Feed"+" - "+feed.getJSONObject("sourceContent").getString("title");
                }else if(feed.getJSONObject("sourceContent").getString("type")!=null){
                    switch(feed.getJSONObject("sourceContent").getString("type").toUpperCase()){
                        case "IMAGE" : msg = "Image in status feed";
                            break;
                        case "LINK_VIDEO" : msg = "Video in status feed";
                            break;
                        case "WEB_PAGE" : msg = "Web Page Link in status feed";
                            break;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return msg;
    }
    public String getRemark(JSONObject remark,JSONObject owner) throws JSONException{

        String profileOwner = getOwnerUser_Plural(owner);
        String data = "Remark"+" - "+remark.getString("content")+" in "+profileOwner+" profile";
        return data;
    }
    public String getVideo(JSONObject video) throws JSONException{


        String data = "Video"+" - ";
        if(video.getString("name")!=null){
            data = data+video.getString("name");
        }else{
            return "Video";
        }

        return data;
    }
    public String getDoc(JSONObject video) throws JSONException{


        String data = "Document"+" - ";
        if(video.getString("name")!=null){
            data = data+video.getString("name");
        }else{
            return "Document";
        }

        return data;
    }
    public String getFile(JSONObject video) throws JSONException{


        String data = "File"+" - ";
        if(video.getString("name")!=null){
            data = data+video.getString("name");
        }else{
            return "File";
        }

        return data;
    }
    public String getHtmlContent(JSONObject video) throws JSONException{


        String data = "Html Content"+" - ";
        if(video.getString("name")!=null){
            data = data+video.getString("name");
        }else{
            return "HtmlContent";
        }

        return data;
    }

    public String getModule(JSONObject video) throws JSONException{


        String data = "Module"+" - ";
        if(video.getString("name")!=null){
            data = data+video.getString("name");
        }else{
            return "Module";
        }

        return data;
    }

         public String getActor(JSONObject user, String USERID){
//            user = getUserObjForUI(user);
             String name = null;
             String userName = null;
             try {
                 name = user.getString("firstName");
                 name += " " + user.getString("lastName");
             String profile = user.getString("profile");

             if (profile != null && profile.equalsIgnoreCase("STUDENT")) {
                 userName = name;
             } else {
                 userName = name + " (" + profile + ")";
             }
      
                 if(user.getString("id").equalsIgnoreCase(USERID)){
                     userName = StringUtils.TXT_YOU+" ";
                 }
             } catch (JSONException e) {
                 e.printStackTrace();
             }

//            var a = getUserLink(user,name);
            return userName;
        }
        public String getOwnerUser_Singular(JSONObject user){
            return getActor(user, sessionManager1.getOrgMemberInfo().id);
        }
        public String getOwnerUser_Plural(JSONObject user){
            String  a = StringUtils.TXT_YOUR;
            String name = a;
            if(user != null){
//                user = getUserObjForUI(user);

                try {
                    if(!user.getString("userId").equalsIgnoreCase(sessionManager1.getOrgMemberInfo().id)){
                        name = user.getString("name")+"'s";
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
//                a = getUserLink(user,name);

            }
            return name;
        }


}
