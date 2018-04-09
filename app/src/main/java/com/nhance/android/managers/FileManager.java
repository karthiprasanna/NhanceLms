package com.nhance.android.managers;

import java.io.File;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.nhance.android.db.datamanagers.DownloadHistoryManager;
import com.nhance.android.db.datamanagers.SDCardFileMetadataManager;
import com.nhance.android.db.models.DownloadHistory;
import com.nhance.android.db.models.SDCardFileMetadata;
import com.nhance.android.enums.EncryptionLevel;
import com.nhance.android.enums.EncryptionLevel.EncryptionLevelOrder;
import com.nhance.android.pojos.LibraryContentRes;
import com.nhance.android.processors.FileMaskProcessor;
import com.nhance.android.readers.SDCardReader;

public class FileManager {

    private final static String TAG                 = "FileManager";
    public static final String  SEPARATOR_DOT       = ".";
    public static final String  SEPARATOR_ENC_LEVEL = "_";
    public static final String  SEPARATOR_FWDSLASH  = "/";

    public static final String  baseUrl             = "http://localhost:8080";
    public static String  MOUNT_PATH          = "/storage";

    public static String getContentLocalUrl(LibraryContentRes content, Context context) {

        if (!content.downloaded) {
            return null;
        }
        DownloadHistory downloadHistory = new DownloadHistoryManager(context)
                .getDownloadHistory(content._id);

        if (downloadHistory == null) {
            return null;
        }

        String filePath = downloadHistory.file;
        addEncLevelToCache(content, context, filePath);
        // if the content is encrypted then, add the decrypted passphrase to the file server

        return filePath.replace(MOUNT_PATH, baseUrl);

    }

    public static void addDecryptedPassPhraseToCache(Context context, String passphrase,
            EncryptionLevel encLevel, String filePath) throws NoSuchPaddingException, Exception {

        if (encLevel == EncryptionLevel.NA || passphrase == null) {
            return;
        }

        filePath = getFileUrlCachePath(filePath);

        Log.d(TAG, "filePath:" + filePath);

        /*if (FileMaskProcessor.getPassPhrase(filePath) != null) {
            // if the decrypted passPhrase value already in cache than we need
            // not decrypt it again..
            Log.d(TAG, "passPhrase already present in cache [filePath:" + filePath + "] : "
                    + FileMaskProcessor.getPassPhrase(filePath));
            return;
        }*/

        String levels[] = encLevel.name().split(SEPARATOR_ENC_LEVEL);
        if (encLevel != EncryptionLevel.P) {
            for (int i = levels.length - 1; i >= 0; i--) {
                EncryptionLevelOrder levelOrder = EncryptionLevelOrder.valueOfKey(levels[i]);
                Log.d(TAG, "levelOrder : " + levelOrder + ", levelValue: " + levels[i]);
                passphrase = levelOrder.getDecryptedPassPhrase(passphrase, context);
            }
        }

        Log.d(TAG, "decrypted passpharase: " + passphrase);
        FileMaskProcessor.putContentPassPhrase(filePath, passphrase);
    }

    private static String getFileUrlCachePath(String fileUrl) {

        return fileUrl.replace(baseUrl, LocalManager.EMPTY_TEXT);
    }

    private static void addEncLevelToCache(LibraryContentRes content, Context context,
            String filePath) {

        EncryptionLevel encLevel = EncryptionLevel.valueOfKey(content.encLevel);
        Log.d(TAG, "video encLevel : " + encLevel);
        if (encLevel != EncryptionLevel.NA) {
            try {
                addDecryptedPassPhraseToCache(context, content.passphrase, encLevel, filePath);
            } catch (NoSuchPaddingException e) {
                Log.e(TAG, e.getMessage(), e);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }

    }

    public static String getSDCardUrl(LibraryContentRes content, String name, String targetId,
            String targetType, Context context, boolean isThumb) {

        return getSDCardPath(content, name, targetId, targetType, context, isThumb, true);
    }

    public static String getSDCardPath(LibraryContentRes content, String name, String targetId,
            String targetType, Context context, boolean isThumb) {

        return getSDCardPath(content, name, targetId, targetType, context, isThumb, false);
    }

    private static String getSDCardPath(LibraryContentRes content, String name, String targetId,
            String targetType, Context context, boolean isThumb, boolean returnHttpUrl) {

        SDCardFileMetadata sdCardFileMetadata = new SDCardFileMetadataManager(context)
                .getSDCardFileMetadata(content.orgKeyId, content.userId, targetId, targetType,
                        content.id, content.type, name, true);
        if (sdCardFileMetadata == null) {
            return null;
        }

        String filePath = TextUtils.join(File.separator, new String[] {
                sdCardFileMetadata.mountPath, SDCardReader.STORAGE_FOLDER,
                sdCardFileMetadata.location, sdCardFileMetadata.name });
        if (!isThumb) {
            addEncLevelToCache(content, context, filePath);
        }
        return returnHttpUrl ? filePath.replace(MOUNT_PATH, baseUrl) : filePath;
    }

    public static String getEncryptedFileName(String fileName) {

        return fileName.contains("conv") ? fileName.replace("conv", "enc") : fileName.replace(
                "orig", "enc");
    }


    public static byte[] generateKey(String password) throws Exception
    {
        byte[] keyStart = password.getBytes("UTF-8");

        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");
        sr.setSeed(keyStart);
        kgen.init(128, sr);
        SecretKey skey = kgen.generateKey();
        return skey.getEncoded();
    }

    public static byte[] encodeFile(byte[] key, byte[] fileData) throws Exception
    {

        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);

        byte[] encrypted = cipher.doFinal(fileData);

        return encrypted;
    }

    public static byte[] decodeFile(byte[] key, byte[] fileData) throws Exception
    {
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);

        byte[] decrypted = cipher.doFinal(fileData);

        return decrypted;
    }
}
