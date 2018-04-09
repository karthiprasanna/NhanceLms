package com.nhance.android.assignment.TeacherModule;

import com.nhance.android.assignment.pojo.content.infos.AssignmentExtendedInfo;
import com.nhance.android.pojos.UserInfoRes;
import com.nhance.android.utils.JSONUtils;

import org.json.JSONObject;

/**
 * Created by karthi on 1/3/17.
 */

public class AssignmentInfo extends AssignmentExtendedInfo {

    private static final long serialVersionUID = 1L;

    public float              avgMarks;
    public long               avgTimeTaken;
    public long               attempts;
    public UserInfoRes user;

    @Override
    public void fromJSON(JSONObject json) {

        super.fromJSON(json);
        avgMarks = (float) JSONUtils.getDouble(json, "avgMarks");
        avgTimeTaken = JSONUtils.getLong(json, "avgTimeTaken");
        attempts = JSONUtils.getLong(json, "attempts");
        JSONObject userJSON = JSONUtils.getJSONObject(json, "user");
        if (userJSON != null && userJSON.length() > 0) {
            user = new UserInfoRes();
            user.fromJSON(userJSON);
        }
    }

    @Override
    public JSONObject toJSON() {

        return null;
    }
//    private static final long serialVersionUID = 1L;
//
//    public float              avgMarks;
//    public long               avgTimeTaken;
//    public long               attempts;
//    public UserInfoRes user;
//
//    @Override
//    public void fromJSON(JSONObject json) {
//
//        super.fromJSON(json);
///*        avgMarks = (float) JSONUtils.getDouble(json, "avgMarks");
//        avgTimeTaken = JSONUtils.getLong(json, "avgTimeTaken");
//        attempts = JSONUtils.getLong(json, "attempts");*/
//        user.fromJSON(JSONUtils.getJSONObject(json, "user"));
//     //   user  = JSONUtils.getJSONObject(json, "user");
//
//        user = new UserInfoRes();
//  /*      if (userJSON != null && userJSON.length() > 0) {
//            user = new UserInfoRes();
//            user.fromJSON(userJSON);
//        }*/
//    }
//
//    @Override
//    public JSONObject toJSON() {
//
//        return null;
//    }

}
