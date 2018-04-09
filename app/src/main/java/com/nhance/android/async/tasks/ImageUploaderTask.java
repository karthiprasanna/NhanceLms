package com.nhance.android.async.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.widget.ProgressBar;

import com.nhance.android.db.datamanagers.ContentDataManager;
import com.nhance.android.managers.SessionManager;

public class ImageUploaderTask extends AbstractVedantuJSONAsyncTask {

    private final String               TAG = "ImageUploaderTask";
    private ITaskProcessor<JSONObject> taskProcessor;

    public ImageUploaderTask(SessionManager session, ProgressBar progressUpdater,
                             ITaskProcessor<JSONObject> taskProcessor, String uri) {

        super(session, progressUpdater);
        this.url = session.getApiUrl(uri);
        this.taskProcessor = taskProcessor;
    }

    @Override
    protected JSONObject doInBackground(String... params) {

        JSONObject json = null;
        Log.d(TAG, "uploading file[" + params[0] + "] to " + this.url);
        json = multiPartUpload(params[0]);
        return json;

    }

    @Override
    protected void onPostExecute(JSONObject result) {

        super.onPostExecute(result);
        if (taskProcessor != null) {
            taskProcessor.onTaskPostExecute(!isCancelled() && !error && result != null, result);
        }
    }

    private JSONObject multiPartUpload(String localPath) {

        boolean removeTempFile = true;
        File tempLocalFile = null;
        try {

            String imageName = StringUtils.substringAfterLast(localPath, File.separator);

            tempLocalFile = new File(ContentDataManager.getTempContentDir() + File.separator
                    + RandomUtils.nextInt() + imageName);
            try {
                Log.i(TAG, "compressing file[" + localPath + "] to " + tempLocalFile);
                Bitmap bitmap = BitmapFactory.decodeFile(localPath);

                FileOutputStream fio = new FileOutputStream(tempLocalFile);
                bitmap.compress(CompressFormat.JPEG, 85, fio);
                fio.close();
            } catch (Exception e) {
                removeTempFile = false;
                Log.e(TAG, "file compressing failed hence uploading origional file", e);
                tempLocalFile = new File(localPath);
            }

            Map<String, Object> sessionParams = new HashMap<String, Object>();
            session.addSessionParams(sessionParams);

            HttpClient client = new DefaultHttpClient();
            HttpPost postReq = new HttpPost(url);
            postReq.addHeader("x-file-name", imageName);
            postReq.addHeader("uploadFileParamName", "imageFile");

            postReq.addHeader("imageName",
                    StringUtils.substringAfterLast(localPath, File.separator));

            MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();

            // in sessionParams on string values are there
            for (Entry<String, Object> entry : sessionParams.entrySet()) {
                multipartEntityBuilder.addTextBody(entry.getKey(), entry.getValue().toString());
            }
            postReq.setEntity(multipartEntityBuilder
                    .addTextBody("callingAppId", "TabApp")
                    .addTextBody("callingApp", "TabApp")
                    .addTextBody("imageName", imageName)
                    .addTextBody("bytesString",
                            Base64.encodeToString(getBytesFromFile(tempLocalFile), Base64.DEFAULT))
                    .build());
            Log.i(TAG, "trying to execute request ");

            HttpResponse res = client.execute(postReq);
            if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String responseString = EntityUtils.toString(res.getEntity());
                Log.d(TAG, "response : " + responseString);
                JSONObject resJSON = new JSONObject(responseString);
                error = checkForError(resJSON);
                return resJSON;
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            error = true;
        } finally {

            if (removeTempFile && tempLocalFile != null) {
                tempLocalFile.delete();
            }
        }
        return null;
    }

    public byte[] getBytesFromFile(File file) throws IOException {

        InputStream is = new FileInputStream(file);
        // Get the size of the file
        long length = file.length();
        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }
        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];
        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }
        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }
        // Close the input stream and return bytes
        is.close();
        return bytes;
    }
}
