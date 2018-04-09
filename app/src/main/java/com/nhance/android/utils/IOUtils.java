package com.nhance.android.utils;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.util.Base64;
import android.util.Log;

public class IOUtils {

    private static final String TAG = "IOUtils";

    public static void closeStream(Closeable closeable) {

        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {}
        }
    }

    public static String checksumMD5(File file) {

        DigestInputStream dis = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            dis = new DigestInputStream(new FileInputStream(file), md);
            while (dis.read() != -1);
            byte[] digest = md.digest();
            String md5 = Base64.encodeToString(digest, Base64.DEFAULT);
            return md5.trim();
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            IOUtils.closeStream(dis);
        }

        return null;
    }
}
