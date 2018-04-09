package com.nhance.android.activities.content.players;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.nhance.android.R;
import com.nhance.android.async.tasks.AbstractVedantuJSONAsyncTask;
import com.nhance.android.async.tasks.ITaskProcessor;
import com.nhance.android.async.tasks.ImageUploaderTask;
import com.nhance.android.managers.LibraryManager;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.pojos.OrgMemberMappingInfo;
import com.nhance.android.recentActivities.Actor;
import com.nhance.android.recentActivities.Parcel;
import com.nhance.android.recentActivities.RealPathUtil;
import com.nhance.android.utils.JSONUtils;
import com.nhance.android.utils.VedantuWebUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.nhance.android.utils.VedantuWebUtils.TAG;

public class AddVideoCommentActivity extends AppCompatActivity {

    private ImageView uploadImageButton;
    private LinearLayout imageLayout;
    private ImageView imageView,deleteImageButton;
    protected static final int    GALLERY_REQUEST_CODE = 101;
    protected static final int    CAMERA_REQUEST_CODE  = 102;
    private SessionManager sessionManager;
    private String imagePath;
    private String uuid, imageUrl;
    private Uri imageUri;
    private boolean imageAttached = false;
    protected File   cameraFileName;
    private ImageView submitButton;
    private EditText videoEditText;
    private String entityId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_video_comment);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Comments");
        uploadImageButton = (ImageView)findViewById(R.id.uploadImageButton);
        deleteImageButton = (ImageView)findViewById(R.id.deleteImageButton);
        imageLayout = (LinearLayout)findViewById(R.id.imageLayout);
        imageView = (ImageView)findViewById(R.id.imageView);
        submitButton = (ImageView)findViewById(R.id.submitButton);
        videoEditText = (EditText)findViewById(R.id.videoEditText);

        entityId = getIntent().getStringExtra("videoId");
        sessionManager = SessionManager.getInstance(AddVideoCommentActivity.this);

        uploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectedPictureDialogue();
            }
        });
        deleteImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImageButton.setVisibility(View.VISIBLE);
                imageLayout.setVisibility(View.GONE);
            }
        });
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(videoEditText.getText().toString() != null && !videoEditText.getText().toString().trim().isEmpty()) {
                    new AddVideoCommentAsyncTask(sessionManager, null, videoEditText.getText().toString(), "VIDEO", entityId, imageAttached, uuid, imageUrl).executeTask(false);
                }else{
                    Toast.makeText(AddVideoCommentActivity.this, "Please enter some text", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // this takes the user 'back', as if they pressed the left-facing triangle icon on the main android toolbar.
                // if this doesn't work as desired, another possibility is to call `finish()` here.
                super.onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void showSelectedPictureDialogue() {

        android.app.AlertDialog.Builder getImageFrom = new android.app.AlertDialog.Builder(this);
        getImageFrom.setTitle("Select:");
        final CharSequence[] opsChars = { getResources().getString(R.string.take_picture),
                getResources().getString(R.string.choose_gallery) };
        getImageFrom.setItems(opsChars, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    Intent cameraIntent = new Intent(
                            MediaStore.ACTION_IMAGE_CAPTURE);
                    File imagesFolder = Environment
                            .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                    if (!imagesFolder.exists()) {
                        imagesFolder.mkdirs();
                    }
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

            ImageUploaderTask upImageUploaderTask = new ImageUploaderTask(sessionManager, null,
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
                            imageAttached = uploaded;
                            if (uploaded) {
                                // the image is being uploaded to server and show this image in
                                // webview
//                                mJSI.appendUploadedImgToRTE(JSONUtils.getString(result, "imgHtml"));
                                try {
                                    Log.e("result url", result.getString("imgHtml"));

                                    uuid = result.getString("uuid");
                                    Document doc = Jsoup.parse(result.getString("imgHtml"));
                                    Elements element = doc.getAllElements();
                                    for(Element e: element)
                                    {
                                        Elements str = e.getElementsByTag("img");
                                        for(Element el: str)
                                        {
                                            String src= el.attr("src");
                                            System.out.println("The src:"+src+".."+uuid);
                                            imageUrl = src;
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                cameraFileName = null;
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        getString(R.string.error_upload), Toast.LENGTH_LONG).show();
                            }
                        }
                    }, "uploadImage");
            if (requestCode == GALLERY_REQUEST_CODE && data != null) {
                imageUri = data.getData();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){

                    imagePath =  RealPathUtil.getRealPathFromURI_API19(AddVideoCommentActivity.this, imageUri);

                } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH && Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT){

                    imagePath =  RealPathUtil.getRealPathFromURI_API11to18(AddVideoCommentActivity.this, imageUri);

                }

//                imagePath = getRealPathFromURI(imageUri);
                Log.d(TAG, "imagePath: " + imagePath);
                onSelectFromGalleryResult(imageUri);
                upImageUploaderTask.executeTask(false, imagePath);
            } else if (requestCode == CAMERA_REQUEST_CODE) {


//                if (requestCode == REQUEST_CAMERA) {

//                    onCaptureImageResult(data);

                Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
                File destination = new File(Environment.getExternalStorageDirectory(),
                        System.currentTimeMillis() + ".jpg");
                FileOutputStream fo;
                try {
                    destination.createNewFile();
                    fo = new FileOutputStream(destination);
                    fo.write(bytes.toByteArray());
                    fo.close();


                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d(TAG,"file created at : " + destination.getName() + ", exist :"+destination.exists());
                upImageUploaderTask.executeTask(false, destination.getAbsolutePath());

                Log.e("image url", thumbnail.toString());
                imageView.setImageBitmap(thumbnail);
                imageLayout.setVisibility(View.VISIBLE);
                uploadImageButton.setVisibility(View.GONE);
//                }


            }
        }
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Uri imageUri) {
        Bitmap bm=null;
//        if (data != null) {
        try {
            bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), imageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        }
        Log.e("bitmap", bm.toString());
        imageView.setImageBitmap(bm);
        imageLayout.setVisibility(View.VISIBLE);
        uploadImageButton.setVisibility(View.GONE);
    }

    public class AddVideoCommentAsyncTask extends AbstractVedantuJSONAsyncTask {

        private ProgressDialog pDialog;
        private String message, entityType, entityId;
        private List<OrgMemberMappingInfo.OrgCenterInfo> centersList;
        private String uuid, imageUrl;
        private boolean imageAttached = false;

        public AddVideoCommentAsyncTask(SessionManager session, ProgressBar progressUpdater, String message, String entityType, String entityId, boolean attached, String uuid, String imageUrl) {
            super(session, progressUpdater);
            this.url = session.getApiUrl("addComment");
            this.message = message;
            this.entityType = entityType;
            this.entityId = entityId;
            imageAttached = attached;
            this.uuid = uuid;
            this.imageUrl = imageUrl;
            pDialog = new ProgressDialog(AddVideoCommentActivity.this);
            pDialog.setMessage("Please wait..");
            pDialog.show();

            Log.e("addCommentAsyncTask", "call"+entityType);
        }

        @Override
        protected JSONObject doInBackground(String... params) {

//            session.addSessionParams(httpParams);
            httpParams.put("base.type", "VIDEO");
            httpParams.put("scope", "ORG");
            httpParams.put("userRole", session.getOrgMemberInfo().profile);
            httpParams.put("callingUserId", session.getOrgMemberInfo().userId);
            httpParams.put("base.id", entityId);
            httpParams.put("parent.id", entityId);
            httpParams.put("root.id", entityId);
            httpParams.put("type", "COMMENT");
            httpParams.put("root.type", entityType);
            httpParams.put("parent.type", entityType);
            httpParams.put("callingAppId", "TapApp");
            httpParams.put("orgId",session.getOrgMemberInfo().orgId);
//            httpParams.put("content", message);
            httpParams.put("userId",session.getOrgMemberInfo().userId);
            httpParams.put("myUserId",session.getOrgMemberInfo().userId);
            httpParams.put("memberId",session.getOrgMemberInfo().memberId);

           /* if(imageAttached){
                httpParams.put("entity.type", "STATUSFEED");
                httpParams.put("entity.id","");
                httpParams.put("source.linkType","UPLOADED");
                httpParams.put("source.type","IMAGE");
                httpParams.put("source.uuid",uuid);
                httpParams.put("source.image",imageUrl);
                httpParams.put("source.url",imageUrl);

            }*/
            if(imageAttached){


                String contentWithImage = String.format("<div class=\"RTEImageDiv\"> \n <img src=\""+imageUrl+"\" v-uid=\""+uuid+"\" class=\"vUrl\" /> </div \">"+message+"</div>");

                httpParams.put("content", contentWithImage);
            }else {
                httpParams.put("content", message);




            }


            JSONObject res = null;
            try {
                res = VedantuWebUtils.getJSONData(url, VedantuWebUtils.GET_REQ, httpParams);
                Log.e("postFeed", "do in background");

            } catch (IOException e) {
                Log.e(ContentValues.TAG, e.getMessage(), e);
            } finally {
                error = checkForError(res);
            }

            if (isCancelled()) {
                Log.i(ContentValues.TAG, "task cancled by user");
                pDialog.dismiss();
                return null;
            }


            return res;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            try {
                JSONObject result = jsonObject.getJSONObject("result");
                if(result != null) {
                    Log.e("result", ".." + result.toString());
                    Intent intent = new Intent();
                    Parcel p = new Parcel(result);
                    Bundle args = new Bundle();
                    args.putSerializable("events", p);
                    intent.putExtra("bundle", result.toString());
                    setResult(200, intent);
                    finish();
                }else{
                    Toast.makeText(AddVideoCommentActivity.this, "Some thing went wrong, Please try again later.", Toast.LENGTH_SHORT).show();
                }




            } catch (JSONException e1) {
                e1.printStackTrace();
            }



            pDialog.dismiss();

        }
    }

}
