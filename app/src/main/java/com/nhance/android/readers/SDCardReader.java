package com.nhance.android.readers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.nhance.android.activities.ActivateSDCardActivity;
import com.nhance.android.async.tasks.AbstractVedantuAsynTask;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.OrgDataManager;
import com.nhance.android.db.datamanagers.SDCardFileMetadataManager;
import com.nhance.android.db.datamanagers.SDCardGroupDataManager;
import com.nhance.android.db.models.SDCardFileMetadata;
import com.nhance.android.db.models.SDCardGroup;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.pojos.OrgMemberInfo;
import com.nhance.android.pojos.content.sdcards.SDCardGroupInfo;
import com.nhance.android.pojos.content.sdcards.SDCardInfo;
import com.nhance.android.processors.FileMaskProcessor;
import com.nhance.android.utils.IOUtils;
import com.nhance.android.utils.JSONAware;
import com.nhance.android.utils.JSONUtils;

public class SDCardReader {

    public static final String INTENT_ACTION_SDCARD_FOUND = "com.nhance.android.intent.action.SD_CARD_FOUND";
    public static final String CARD_GROUP_INFO = "cardGroupInfo";
    private static final String TAG = "SDCardReader";
    Context context = null;
    SessionManager session = null;
    byte[] orgPublicKey = null;
    String errorMsg = null;
    FileMaskProcessor maskProcessor;

    public static final String STORAGE_FOLDER = "nhance";

    private static final String V_SDCARD_GROUP_FILE = ".vg";
    private static final String V_SDCARD_FILE = ".vs";
    private static final String V_FILE_METADATA_FILE = ".vfmd";
    private static final String V_VERIFICATION_FILE = ".vv";

    private static final Set<String> validsFileSet = new HashSet<String>(
            Arrays.asList(new String[]{
                    V_SDCARD_GROUP_FILE, V_SDCARD_FILE, V_FILE_METADATA_FILE, V_VERIFICATION_FILE}));
    int i = 0;

    public SDCardReader(Context context) {

        this.context = context;
        this.session = SessionManager.getInstance(context);
        this.orgPublicKey = new OrgDataManager(context).getOrgPublicKey(
                session.getSessionStringValue(ConstantGlobal.ORG_ID),
                session.getSessionStringValue(ConstantGlobal.CMDS_URL));
        String passphrase = new String(Base64.encode(orgPublicKey, Base64.NO_WRAP));
        this.maskProcessor = new FileMaskProcessor(passphrase, passphrase.getBytes().length);
    }

    public void updateActiveSDCard(SDCardGroupInfo groupInfo, boolean active) {

        SDCardFileMetadataManager sdCardFileMetadataManager = new SDCardFileMetadataManager(context);
        if (active) {
            sdCardFileMetadataManager.updateActiveSDCard(null, false, null);
        }
        sdCardFileMetadataManager.updateActiveSDCard(groupInfo.cardInfo.id, active,
                groupInfo.mountPath);
        session.setCurrentCardId(groupInfo.cardInfo.id);
        //
    }

    public void removeActiveCard() {

        String cardId = session.getCurrentCardId();
        session.removeCurrentCardId();
        SDCardFileMetadataManager sdCardFileMetadataManager = new SDCardFileMetadataManager(context);
        sdCardFileMetadataManager.updateActiveSDCard(cardId, false, null);
    }

    public void importFileMetadata(SDCardGroupInfo cardGroupInfo, String accessCode) {

        BufferedReader fileReader = null;
        File mDataFiel = new File(getVedantuPath(cardGroupInfo, V_FILE_METADATA_FILE));
        try {
            fileReader = new BufferedReader(new FileReader(mDataFiel));
            String mDataString = null;
            SDCardFileMetadataManager manager = new SDCardFileMetadataManager(context);
            while ((mDataString = fileReader.readLine()) != null) {
                // get the decrypted string
                mDataString = getDecryptedBase64String(mDataString);
                JSONObject mDataJson = new JSONObject(mDataString);
                SDCardFileMetadata sdCardFileMetadata = new SDCardFileMetadata();
                sdCardFileMetadata.fromJSON(mDataJson);
                sdCardFileMetadata.targetId = cardGroupInfo.targetId;
                sdCardFileMetadata.targetType = cardGroupInfo.targetType;
                sdCardFileMetadata.mountPath = cardGroupInfo.mountPath;
                sdCardFileMetadata.orgKeyId = session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID);
                sdCardFileMetadata.userId = session.getSessionStringValue(ConstantGlobal.USER_ID);

                try {
                    manager.insert(sdCardFileMetadata);
                } catch (Throwable e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }

        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        SDCardGroup sdCardGroup = new SDCardGroup(
                session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID),
                session.getSessionStringValue(ConstantGlobal.USER_ID), cardGroupInfo.name,
                cardGroupInfo.id, cardGroupInfo.size, cardGroupInfo.targetId,
                cardGroupInfo.targetType);
        sdCardGroup.activated = true;
        sdCardGroup.activatedTime = System.currentTimeMillis();
        sdCardGroup.accessCode = accessCode;

        SDCardGroupDataManager sdCardGroupDataManager = new SDCardGroupDataManager(context);

        try {
            sdCardGroupDataManager.upsertGroupInfo(sdCardGroup);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        // TODO:

    }

    public void readDataFromSDCard(String path) {

        /**
         * Call SD card reader
         **/
        SDCardReaderTask task = new SDCardReaderTask(path);
        task.executeTask(false);
    }

    private Map<String, String> readMD5Values(File fromFile) {

        Map<String, String> fileToMD5ValueMap = new LinkedHashMap<String, String>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(fromFile));
            String line = null;
            while ((line = reader.readLine()) != null) {
                Log.d(TAG, "line: " + line);
                JSONObject checkSumJSON = new JSONObject(line);
                fileToMD5ValueMap.put(JSONUtils.getString(checkSumJSON, "i"),
                        JSONUtils.getString(checkSumJSON, "c"));
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            IOUtils.closeStream(reader);
        }

        return fileToMD5ValueMap;
    }

    private class SDCardReaderTask extends AbstractVedantuAsynTask<Void, Void, SDCardGroupInfo> {

        private String mountPath;

        public SDCardReaderTask(String mountPath) {

            super();
            this.mountPath = mountPath;
        }

        @Override
        protected SDCardGroupInfo doInBackground(Void... params) {

            Log.d(TAG, "reading data from sdcard mountPath: " + mountPath);
            File vedantuDir = new File(mountPath, STORAGE_FOLDER);
            if (!vedantuDir.isDirectory()) {
                Log.e(TAG, "mounted path[" + mountPath + "] is not a directory");
                return null;
            }

            File[] files = vedantuDir.listFiles(new VedantuFileFilter());

            if (files == null || files.length == 0) {
                Log.e(TAG, "no vedantu file found at directory " + mountPath);
                return null;
            }

            if (files.length != validsFileSet.size()) {
                Log.d(TAG,
                        "validation file count does not matched, expected[" + validsFileSet.size()
                                + "], found[" + files.length + "]");
                return null;
            }

            File verificationFile = new File(files[0].getParentFile(), V_VERIFICATION_FILE);
            Map<String, String> fileToMD5ValueMap = readMD5Values(verificationFile);

            File sdCardGroupInfoFile = null;
            File sdCardFile = null;
            File fileMetadataInfoFile = null;

            for (File f : files) {
                if (f.getName().equals(V_VERIFICATION_FILE)) {
                    continue;
                } else if (f.getName().equals(V_SDCARD_GROUP_FILE)) {
                    sdCardGroupInfoFile = f;
                } else if (f.getName().equals(V_SDCARD_FILE)) {
                    sdCardFile = f;
                } else if (f.getName().equals(V_FILE_METADATA_FILE)) {
                    fileMetadataInfoFile = f;
                }

                Log.d(TAG, "checking MD5Checksum for file: " + f.getAbsolutePath()
                        + ", file exists: " + f.exists());

                boolean isValidChecksum = f.exists()
                        && isValidMD5Checksum(f, fileToMD5ValueMap.get(f.getName()));

                if (!isValidChecksum) {
                    Log.e(TAG, "file : " + f.getAbsolutePath() + " is modified by user");
                    return null;
                }
            }

            // read sdcard group info
            if (sdCardGroupInfoFile == null || sdCardFile == null || fileMetadataInfoFile == null) {
                Log.e(TAG, "some file are missing");
                return null;
            }

            SDCardGroupInfo groupInfo = readSDCardGroupInfo(sdCardGroupInfoFile);

            if (groupInfo == null) {
                Log.e(TAG, "no card group info is found");
                return null;
            }

            if (groupInfo.orgId == null
                    || !groupInfo.orgId
                    .equals(session.getSessionStringValue(ConstantGlobal.ORG_ID))) {
                Log.e(TAG, "invalid parent info or not part of the org");
                return null;
            }
            groupInfo.mountPath = mountPath;

            SDCardGroupDataManager dataManager = new SDCardGroupDataManager(context);

            SDCardGroup sdgInfo = dataManager.getSDCardGroup(groupInfo.id,
                    session.getSessionStringValue(ConstantGlobal.USER_ID),
                    session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID));
            groupInfo.isActivated = sdgInfo != null && sdgInfo.activated;

            // check if the user is part of the program/section
            OrgMemberInfo orgMemberInfo = session.getOrgMemberInfo();
            groupInfo.isPartOfProgramSection = orgMemberInfo != null
                    && orgMemberInfo._isSectionMappingPresent(groupInfo.targetId);

            SDCardInfo cardInfo = readSDCardInfo(sdCardFile);
            if (cardInfo == null) {
                Log.e(TAG, "failed to read SDCard info");
                return null;
            }

            groupInfo.cardInfo = cardInfo;

            if (groupInfo.isPartOfProgramSection && groupInfo.isActivated) {
                // if the group was activated then validate if the groupInfoFile has correct data
                if (groupInfo.activeGroupInfo != null) {
                    boolean isValidActivation = session.getSessionStringValue(
                            ConstantGlobal.USER_ID).equals(groupInfo.activeGroupInfo.userId)
                            && groupInfo.id.equals(groupInfo.activeGroupInfo.groupId)
                            && SessionManager.getMacAddress(context).equals(
                            groupInfo.activeGroupInfo.deviceId);
                    if (!isValidActivation) {
                        Log.e(TAG, "this sdcard is already in used for a different device");
                        return null;
                    }
                } else {
                    // write activeGroupInfo to the groupInfoFile
                    groupInfo.activeGroupInfo = new ActiveGroupInfo(groupInfo.id,
                            session.getSessionStringValue(ConstantGlobal.USER_ID),
                            SessionManager.getMacAddress(context));
                    updateGroupInfoFile(groupInfo);
                }

                // mark content of the sdcard as active in the file
                // metadata table
                updateActiveSDCard(groupInfo, true);

            }
            return groupInfo;
        }

        @Override
        protected void onPostExecute(SDCardGroupInfo result) {

            super.onPostExecute(result);
            if (result == null || result.cardInfo == null) {
                return;
            }

            if (result.isPartOfProgramSection && result.isActivated) {
                Log.d(TAG, "found sdcard group: " + result.name + ", sdcard:"
                        + result.cardInfo.name);
                Toast.makeText(context, "Found " + result.name + "-" + result.cardInfo.name,
                        Toast.LENGTH_LONG).show();

                /** send broadcast for {@link #INTENT_ACTION_SDCARD_FOUND} **/
                sendBroadCast(true);
                return;
            }

            Intent intent = new Intent(context, ActivateSDCardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(CARD_GROUP_INFO, result);
            context.startActivity(intent);
        }
    }

    private SDCardGroupInfo readSDCardGroupInfo(File groupInfoFile) {

        Log.d(TAG, "reading readSDCardGroupInfo from file :" + groupInfoFile);
        SDCardGroupInfo groupInfo = null;
        BufferedReader fileReader = null;

        try {
            fileReader = new BufferedReader(new FileReader(groupInfoFile));
            int lineNo = 0;
            String sdCardGroupInfoString = null;
            while ((sdCardGroupInfoString = fileReader.readLine()) != null) {
                sdCardGroupInfoString = getDecryptedBase64String(sdCardGroupInfoString);
                lineNo++;
                JSONObject lineInfoJson = new JSONObject(sdCardGroupInfoString);
                if (lineNo == 1) {
                    groupInfo = new SDCardGroupInfo();
                    groupInfo.fromJSON(lineInfoJson);
                } else if (lineNo == 2) {
                    // if the SDCard file has data of activation
                    ActiveGroupInfo activeGroupInfo = new ActiveGroupInfo();
                    activeGroupInfo.fromJSON(lineInfoJson);
                    if (groupInfo != null) {
                        groupInfo.activeGroupInfo = activeGroupInfo;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        IOUtils.closeStream(fileReader);
        return groupInfo;
    }

    private SDCardInfo readSDCardInfo(File sdCardInfoFile) {

        SDCardInfo cardInfo = null;
        BufferedReader fileReader = null;

        try {
            fileReader = new BufferedReader(new FileReader(sdCardInfoFile));
            StringBuilder sb = new StringBuilder();
            String sdCardInfoString = null;
            while ((sdCardInfoString = fileReader.readLine()) != null) {
                sdCardInfoString = getDecryptedBase64String(sdCardInfoString);
                sb.append(sdCardInfoString);
            }
            sdCardInfoString = sb.toString();
            JSONObject cardInfoJson = new JSONObject(sdCardInfoString);
            cardInfo = new SDCardInfo();
            cardInfo.fromJSON(cardInfoJson);
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        IOUtils.closeStream(fileReader);
        return cardInfo;
    }

    private boolean isValidMD5Checksum(File file, String md5) {

        if (TextUtils.isEmpty(md5)) {
            return false;
        }

        String calculatedMD5 = IOUtils.checksumMD5(file);
        Log.d(TAG, "calculated md5: " + calculatedMD5 + ", expected md5: " + md5 + " for file: "
                + file);
        boolean isValidCheckSum = md5.equals(calculatedMD5);
        Log.d(TAG, "isValidCheckSum : " + isValidCheckSum);
        return isValidCheckSum;

    }

    private String getDecryptedBase64String(String value) {

        Log.d(TAG, "input value : " + value);
        String decryptedString = maskProcessor.process(Base64.decode(value.getBytes(),
                Base64.DEFAULT));
        Log.d(TAG, "decrypted string : " + decryptedString);
        return decryptedString;
    }

    private String getEncryptedBase64String(String value) {

        Log.d(TAG, "input valud : " + value);
        String encryptedString = maskProcessor.process(value.getBytes());
        Log.d(TAG, "encrypted string : " + encryptedString);
        String encodedString = Base64.encodeToString(encryptedString.getBytes(), Base64.NO_WRAP);
        Log.d(TAG, "encodedString string : " + encodedString);
        return encodedString;
    }

    public void updateGroupInfoFile(SDCardGroupInfo cardGroupInfo) {

        try {
            File file = new File(getVedantuPath(cardGroupInfo, V_SDCARD_GROUP_FILE));
            long lastModified = file.lastModified();
            FileWriter fileWriter = new FileWriter(file, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.newLine();

            String value = cardGroupInfo.activeGroupInfo.toJSON().toString();
            String encryptedString = getEncryptedBase64String(value);
            bufferedWriter.write(encryptedString);
            IOUtils.closeStream(bufferedWriter);
            file.setLastModified(lastModified);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        updateFileHash(cardGroupInfo);

    }

    private class VedantuFileFilter implements FileFilter {

        @Override
        public boolean accept(File pathname) {

            return pathname != null && validsFileSet.contains(pathname.getName());
        }
    }

    private String getVedantuPath(SDCardGroupInfo cardGroupInfo, String fileName) {

        StringBuilder sb = new StringBuilder();
        sb.append(cardGroupInfo.mountPath).append(File.separator).append(STORAGE_FOLDER)
                .append(File.separator).append(fileName);

        return sb.toString();
    }

    private void updateFileHash(SDCardGroupInfo cardGroupInfo) {

        File verificationFile = new File(getVedantuPath(cardGroupInfo, V_VERIFICATION_FILE));
        Map<String, String> fileToMD5ValueMap = readMD5Values(verificationFile);
        long lastModified = verificationFile.lastModified();
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(verificationFile));
            String newCheckSum = IOUtils.checksumMD5(new File(getVedantuPath(cardGroupInfo,
                    V_SDCARD_GROUP_FILE)));
            Log.d(TAG, "old checksum : " + fileToMD5ValueMap.get(V_SDCARD_GROUP_FILE)
                    + ", new checksum: " + newCheckSum);

            fileToMD5ValueMap.put(V_SDCARD_GROUP_FILE, newCheckSum);
            int count = 0;
            for (Entry<String, String> entry : fileToMD5ValueMap.entrySet()) {
                JSONObject checkSumJson = new JSONObject();
                JSONUtils.putValue("i", entry.getKey(), checkSumJson);
                JSONUtils.putValue("c", entry.getValue(), checkSumJson);
                if (count > 0) {
                    writer.newLine();
                }
                writer.write(checkSumJson.toString());
                count++;
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            IOUtils.closeStream(writer);
        }

        verificationFile.setLastModified(lastModified);
    }

    public static class ActiveGroupInfo implements JSONAware {

        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private static final String FIELD_DEVICE_ID = "deviceId";
        private static final String FIELD_GROUP_ID = "groupId";

        public String groupId;
        public String userId;
        public String deviceId;

        public ActiveGroupInfo() {

        }

        public ActiveGroupInfo(String groupId, String userId, String deviceId) {

            this.groupId = groupId;
            this.userId = userId;
            this.deviceId = deviceId;
        }

        @Override
        public void fromJSON(JSONObject json) {

            groupId = JSONUtils.getString(json, FIELD_GROUP_ID);
            deviceId = JSONUtils.getString(json, FIELD_DEVICE_ID);
            userId = JSONUtils.getString(json, ConstantGlobal.USER_ID);
        }

        @Override
        public JSONObject toJSON() {

            JSONObject json = new JSONObject();
            JSONUtils.putValue(FIELD_DEVICE_ID, deviceId, json);
            JSONUtils.putValue(FIELD_GROUP_ID, groupId, json);
            JSONUtils.putValue(ConstantGlobal.USER_ID, userId, json);
            return json;
        }

    }

    public void sendBroadCast(boolean sdCardFound) {

        /** send broadcast for {@link #INTENT_ACTION_SDCARD_FOUND} **/

        Intent intent = new Intent(INTENT_ACTION_SDCARD_FOUND);
        intent.putExtra("loadContent", sdCardFound);
        context.sendBroadcast(intent);
    }

}
