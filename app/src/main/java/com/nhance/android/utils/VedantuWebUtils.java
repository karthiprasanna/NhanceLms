package com.nhance.android.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.http.AndroidHttpClient;
import android.text.TextUtils;
import android.util.Log;

import com.nhance.android.managers.SessionManager;

@SuppressLint("NewApi")
public class VedantuWebUtils {

    public static int          GET_REQ        = 1;
    public static int          POST_REQ       = 1;
    public static final String TAG            = "VedantuWebUtils";
    public static final String KEY_ERROR_CODE = "errorCode";
    public static final String KEY_RESULT     = "result";
    public static final String KEY_LIST       = "list";
    public static final String KEY_TOTAL_HITS = "totalHits";
    public static final String KEY_FACETS     = "facet";
    public static final String KEY_TOPICS     = "topics";
    public static final String KEY_COURSES    = "courses";

    public static boolean isOnline(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null
                && (netInfo.getType() == ConnectivityManager.TYPE_WIFI || (netInfo.getType() == ConnectivityManager.TYPE_MOBILE))
                && netInfo.isConnected()) {
            return true;
        }
        return false;
    }

    public static boolean checkErrorMsg(JSONObject json, String fromUrl) {

        try {
            if (json == null
                    || (json.has(KEY_ERROR_CODE) && !TextUtils.isEmpty(json
                            .getString(KEY_ERROR_CODE)))) {
                Log.d(TAG, JSONUtils.getString(json, KEY_ERROR_CODE));
                // Toast.makeText(
                // getBaseContext(),
                // "error while downloading data from[" + fromUrl + "] : "
                // + (json != null ? json.getString(KEY_ERROR_CODE) : null),
                // Toast.LENGTH_SHORT).show();
                return true;
            }
        } catch (JSONException e) {

        }
        return false;
    }

    public static JSONObject getJSONData(String url, int reqType, Map<String, Object> params)
            throws IOException {

        JSONObject json = null;
        if (!SessionManager.isOnline()) {
            Log.e(TAG, "Internet connection not available");
            return json;
        }
        params.put("callingApp", "TabApp");
        params.put("callingAppId", "TabApp");
        String data = getStringData(url, reqType, params);
        if (!TextUtils.isEmpty(data)) {
            try {
                json = new JSONObject(data);
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
        return json;
    }

    public static String getStringData(String url, int reqType, Map<String, Object> params) {

        final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
        if (params != null && params.get("password") == null) {
            Log.d(TAG, "request params: " + params);
        }
        HttpPost req = null;
        try {
            req = new HttpPost(url);
            List<NameValuePair> postParams = new ArrayList<NameValuePair>();

            if (params != null) {
                for (Entry<String, Object> entry : params.entrySet()) {
                    if (entry.getValue() != null && entry.getKey() != null) {
                        postParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()
                                .toString()));
                    }
                }
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postParams);
                req.setEntity(entity);
            }
        } catch (Throwable e) {
            Log.d(TAG, e.getMessage(), e);
            client.close();
            return null;
        }

        String data = "";
        try {
            Log.d(TAG, "schema : "+req.getURI().getScheme());
            int port = -1 == req.getURI().getPort() ? (req.getURI().getScheme().equals("https") ? 443
                    : 80)
                    : req.getURI().getPort();

            HttpHost httpHost = new HttpHost(req.getURI().getHost(), port, req.getURI().getScheme());
            Log.d(TAG, "fetching data from " + req.getURI());
            HttpResponse response = client.execute(httpHost, req);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                Log.w(TAG, "Error " + statusCode + " while downloading data from " + url);
                return data;
            }
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream inputStream = null;
                try {
                    inputStream = new BufferedInputStream(entity.getContent());
                    data = fromStream(inputStream);
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    entity.consumeContent();
                }
            }

        } catch (Throwable e) {
            req.abort();

            Log.e(TAG,
                    "Error while downloading data from " + url + ", errorMessage:" + e.getMessage(),
                    e);
        } finally {
            if (client != null) {
                client.close();
            }
        }
        Log.v(TAG, "response from[" + url + "] : " + data);
        return data;
    }

    public static String fromStream(InputStream inputStream) throws IOException {

        InputStreamReader streamReader = new InputStreamReader(inputStream);
        BufferedReader reader = new BufferedReader(streamReader);
        StringBuilder out = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            out.append(line);
        }
        streamReader.close();
        return out.toString();
    }

}
