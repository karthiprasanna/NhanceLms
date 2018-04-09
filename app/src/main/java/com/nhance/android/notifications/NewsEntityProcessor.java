package com.nhance.android.notifications;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by prathibha on 6/8/2017.
 */

public class NewsEntityProcessor {

    private String html;

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
    public String getComment(JSONObject src) throws JSONException{
        //String text = src.content;
        String preText = "comment";
        JSONObject root = src.getJSONObject("rootDetails");
        if(root != null){
            preText = changePreText(root.getString("type"));
        }else{
            preText = changePreText(src.getString("type"));
        }

         if(preText == null){
             html = "comment";
         }else {
             html = preText;
         }
		/*var html = preText+"- \"<span class='greenTextColor singleLineText toolTipTitle displayInBlock' data-title='"+text+"' \n\
				style='max-width:200px;' >"+text+"</span>\"";*/
        return html;
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
    public String getOwnerUser_Plural(JSONObject user){
        String a = "Your";
        if(user != null){

		/*	String name = a;
			if(user.userId != USERID){
				name = user.name+"'s";
			}
			a = getUserLink(user,name);
		}*/
            return a;
        }
        return a;
    }
    public String getSolution(JSONObject ques){

        String link = "Question";
        link = "Solution of the "+link;
        return link;
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
}

    

