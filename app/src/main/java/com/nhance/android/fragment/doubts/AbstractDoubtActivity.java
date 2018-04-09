package com.nhance.android.fragment.doubts;

import java.io.File;
import java.util.Date;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.support.v4.content.CursorLoader;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import com.nhance.android.activities.baseclasses.NhanceBaseActivity;
import com.nhance.android.async.tasks.ITaskProcessor;
import com.nhance.android.async.tasks.ImageUploaderTask;
import com.nhance.android.jsinterfaces.DoubtJSInterface;
import com.nhance.android.recentActivities.RealPathUtil;
import com.nhance.android.utils.JSONUtils;
import com.nhance.android.utils.VedantuWebUtils;
import com.nhance.android.R;

import static com.nhance.android.utils.VedantuWebUtils.TAG;

public abstract class AbstractDoubtActivity extends NhanceBaseActivity {

    private static final String   TAG                  = "AbstractDoubtActivity";
    protected static final int    GALLERY_REQUEST_CODE = 101;
    protected static final int    CAMERA_REQUEST_CODE  = 102;
    protected static final String CAMERA_IMG_PREFIX    = "IMG_V_";
    protected File                cameraFileName;
    protected DoubtJSInterface    mJSI;
    private String imagePath;

    protected void showSelectedPictureDialogue() {

        AlertDialog.Builder getImageFrom = new AlertDialog.Builder(this);
        getImageFrom.setTitle("Select:");
        final CharSequence[] opsChars = { getResources().getString(R.string.take_picture),
                getResources().getString(R.string.choose_gallery) };
        getImageFrom.setItems(opsChars, new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    Intent cameraIntent = new Intent(
                            android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    File imagesFolder = Environment
                            .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                    if (!imagesFolder.exists()) {
                        imagesFolder.mkdirs();
                    }
                    CharSequence s = CAMERA_IMG_PREFIX
                            + DateFormat.format("yyyyMMdd_hhmmss", new Date().getTime());
                    cameraFileName = new File(imagesFolder, s + ".jpg");
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraFileName));
                    startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
                } else if (which == 1) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(
                            Intent.createChooser(intent, getResources().getString(R.string.open)),
                            GALLERY_REQUEST_CODE);
                }
                dialog.dismiss();
            }
        });
        getImageFrom.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult requestCode:" + requestCode + ", resultCode:" + resultCode
                + ", data:" + data);
        if (resultCode == RESULT_OK) {
            final ProgressDialog dialog = new ProgressDialog(this);
            dialog.setCancelable(false);
            dialog.setMessage("Uploading Image...");
            dialog.show();

            ImageUploaderTask upImageUploaderTask = new ImageUploaderTask(session, null,
                    new ITaskProcessor<JSONObject>() {
                        @Override
                        public void onTaskStart(JSONObject result) {
                        }
                        @Override
                        public void onTaskPostExecute(boolean success, JSONObject result) {
                            dialog.cancel();
                            if (!success) {
                                return;
                            }
                            result = JSONUtils.getJSONObject(result, VedantuWebUtils.KEY_RESULT);
                            boolean uploaded = JSONUtils.getBoolean(result, "uploaded");
                            if (uploaded) {
                                // the image is being uploaded to server and show this image in
                                // webview
                                mJSI.appendUploadedImgToRTE(JSONUtils.getString(result, "imgHtml"));
                                cameraFileName = null;
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        getString(R.string.error_upload), Toast.LENGTH_LONG).show();
                            }
                        }
                    }, "uploadImage");
            if (requestCode == GALLERY_REQUEST_CODE && data != null) {
                Uri imageUri = data.getData();
//                String imagePath = getRealPathFromURI(imageUri);
//                Log.d(TAG, "imagePath: " + imagePath);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){

                    imagePath =  RealPathUtil.getRealPathFromURI_API19(AbstractDoubtActivity.this, imageUri);

                } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH && Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT){

                    imagePath =  RealPathUtil.getRealPathFromURI_API11to18(AbstractDoubtActivity.this, imageUri);

                }

//                imagePath = getRealPathFromURI(imageUri);
                Log.d(TAG, "imagePath: " + imagePath);
                upImageUploaderTask.executeTask(false, imagePath);
            } else if (requestCode == CAMERA_REQUEST_CODE) {
                Log.d(TAG, "file output: " + cameraFileName);
                if (cameraFileName != null) {
                    Log.d(TAG,"file created at : " + cameraFileName + ", exist :"+cameraFileName.exists());
                    upImageUploaderTask.executeTask(false, cameraFileName.getAbsolutePath());
                }
            }
        }
    }

    // Convert the image URI to the direct file system path of the image file
    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaColumns.DATA };
        Cursor cursor = new CursorLoader(getApplicationContext(), contentUri, proj, null, null,
                null).loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();
        return path;
    }
}
