package com.nhance.android.recentActivities;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.nhance.android.R;
import com.nhance.android.activities.baseclasses.NhanceBaseActivity;
import com.nhance.android.async.tasks.AbstractVedantuJSONAsyncTask;
import com.nhance.android.async.tasks.ITaskProcessor;
import com.nhance.android.async.tasks.ImageUploaderTask;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.OrgDataManager;
import com.nhance.android.db.models.Organization;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.pojos.OrgMemberInfo;
import com.nhance.android.pojos.OrgMemberMappingInfo;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nhance.android.utils.VedantuWebUtils.TAG;

public class AddFeedActivity extends NhanceBaseActivity implements CompoundButton.OnCheckedChangeListener{

    protected static final int    GALLERY_REQUEST_CODE = 101;
    protected static final int    CAMERA_REQUEST_CODE  = 102;
    private String organizationName;
    private EditText discussionEditText;
    private TextView organizationTextView;
    private Spinner programListSpinner;
    private SessionManager sessionManager;
    private List<String> programsList;
    private LinearLayout centersLayout;
    private Button shareButton;
    private HashMap<String, List<OrgMemberMappingInfo.OrgCenterInfo>> centersList;
    private List<OrgMemberMappingInfo.OrgProgramBasicInfo> programCentersList;
    private String statusType, statusTypeId;
    private List<OrgMemberMappingInfo.OrgCenterInfo> centersList1;
    private List<OrgMemberMappingInfo.OrgCenterInfo> selectedCentersList;
    private String programId;
    private Button uploadImageButton;
    private String userChoosenTask;
    private ImageView imageView;
    protected static final String CAMERA_IMG_PREFIX    = "IMG_V_";
    protected File   cameraFileName;
    private String uuid, imageUrl;
    private boolean imageAttached = false;
    private String imagePath;
    private Uri imageUri;
    private ImageView deleteImageButton;
    private LinearLayout imageLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_add_feed);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Add New Feed");
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        sessionManager = SessionManager.getInstance(AddFeedActivity.this);
        programsList = new ArrayList<String>();
        discussionEditText = (EditText)findViewById(R.id.discussionEditText);
        organizationTextView = (TextView)findViewById(R.id.shareWithTextView);
        programListSpinner = (Spinner)findViewById(R.id.program_list_filter);
        centersLayout = (LinearLayout)findViewById(R.id.centersLayout);
        shareButton = (Button)findViewById(R.id.shareButton);
        uploadImageButton = (Button)findViewById(R.id.uploadImageButton);
        deleteImageButton = (ImageView)findViewById(R.id.deleteImageButton);
        imageLayout = (LinearLayout)findViewById(R.id.imageLayout);
        imageView = (ImageView)findViewById(R.id.imageView);

        programCentersList = new ArrayList<OrgMemberMappingInfo.OrgProgramBasicInfo> ();
        centersList = new HashMap<String, List<OrgMemberMappingInfo.OrgCenterInfo>>();
        centersList1 = new ArrayList<OrgMemberMappingInfo.OrgCenterInfo>();
        session = SessionManager.getInstance(AddFeedActivity.this);
        Organization org = new OrgDataManager(AddFeedActivity.this)
                .getOrganization(session.getSessionStringValue(ConstantGlobal.ORG_ID),
                        session.getSessionStringValue(ConstantGlobal.CMDS_URL));
        OrgMemberInfo orgMemberInfo = sessionManager.getOrgMemberInfo();
        Log.e("orgInfo ", org.toString());
        if(org != null)
            organizationName = org.fullName;

        programsList.add("All");
        for (OrgMemberMappingInfo.OrgProgramBasicInfo program : orgMemberInfo.mappings.programs) {
            Log.e("orgIds", program.toString()+program.name+program.centers);
            programsList.add(program.name);
            programCentersList.add(program);

        }
        selectedCentersList = new ArrayList<OrgMemberMappingInfo.OrgCenterInfo>();
      ProgramSectionsAdapter spinnerAdapter = new ProgramSectionsAdapter(
               AddFeedActivity.this, android.R.layout.simple_list_item_1,
                programsList, centersList, programCentersList);

        programListSpinner.setAdapter(spinnerAdapter);

        programListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "Loading courses");
                centersLayout.removeAllViewsInLayout();
                selectedCentersList.clear();
                statusType = "ORGANIZATION";
                statusTypeId = sessionManager.getOrgMemberInfo().orgId;
                if (view == null) {
                    // this view was coming as null if the application context is
                    // destroyed and LibraryCourseFragment is tried to load
                    return;
                }
//                    if(position != 0) {
                Map<String, List<OrgMemberMappingInfo.OrgCenterInfo>> sectionData = (Map<String, List<OrgMemberMappingInfo.OrgCenterInfo>>) view.getTag();
                Log.e("programData", ""+sectionData.get("centers"));


                 if(sectionData.get("centers") != null){
                     statusType = "PROGRAM";
                     programId = programCentersList.get(position-1).id;
                     statusTypeId = programId;
                     centersLayout.setVisibility(View.VISIBLE);
                     centersList1.clear();
                     centersList1.addAll(sectionData.get("centers"));
                     TextView textView = new TextView(AddFeedActivity.this);
                     textView.setText("Centers");
                     textView.setTextColor(getResources().getColor(R.color.black));
                     textView.setTextSize(18);
                     centersLayout.addView(textView);
                     for(int i = 0 ; i<centersList1.size();i++){
                         OrgMemberMappingInfo.OrgCenterInfo center = centersList1.get(i);
                         CheckBox checkBox = new CheckBox(AddFeedActivity.this);
                         checkBox.setText(center.name);
                         checkBox.setTag(i);
                         centersLayout.addView(checkBox);
                         checkBox.setOnCheckedChangeListener(AddFeedActivity.this);
                     }
                 }else{

                     centersLayout.removeAllViewsInLayout();
                     centersLayout.setVisibility(View.GONE);
                 }

//

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        organizationTextView.setText(organizationName);

        uploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                selectImage();
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

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(discussionEditText.getText().toString() != null && !discussionEditText.getText().toString().trim().isEmpty()) {
                    new PostFeedAsyncTask(sessionManager, null, discussionEditText.getText().toString(), statusType, statusTypeId, selectedCentersList, imageAttached, uuid, imageUrl).executeTask(false);
                }else{
                    Toast.makeText(AddFeedActivity.this, "Please enter some text", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        if(isChecked){
            OrgMemberMappingInfo.OrgCenterInfo center = centersList1.get((int)(((CheckBox)buttonView).getTag()));
            Log.e("center", center.toString());
            selectedCentersList.add(center);
        }
    }

    final public class ProgramSectionsAdapter extends ArrayAdapter<String> {

        private List<String> items;
        private int  layoutResource;
        private LayoutInflater inflater;
//        private List<OrgMemberMappingInfo.OrgCenterInfo> centersList = new ArrayList<OrgMemberMappingInfo.OrgCenterInfo>();
        private HashMap<String, List<OrgMemberMappingInfo.OrgCenterInfo>> centersList;
        private List<OrgMemberMappingInfo.OrgProgramBasicInfo> programCentersList;

        public ProgramSectionsAdapter(Context context, int layoutResource, List<String> items,
                                      HashMap<String, List<OrgMemberMappingInfo.OrgCenterInfo>> centersList, List<OrgMemberMappingInfo.OrgProgramBasicInfo> programCentersList) {

            super(context, layoutResource, items);
            this.items = items;
            this.layoutResource = layoutResource;
            this.centersList=centersList;
            this.programCentersList = new ArrayList<OrgMemberMappingInfo.OrgProgramBasicInfo>();
            this.programCentersList.add(new OrgMemberMappingInfo.OrgProgramBasicInfo());
            this.programCentersList.addAll(programCentersList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view = convertView;
            if (view == null) {
                inflater = (LayoutInflater) getContext().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(layoutResource, parent, false);
            }
            TextView sectionView = (TextView) view;
            String progCenterSecName = items.get(position);
            sectionView.setText(progCenterSecName);
            OrgMemberMappingInfo.OrgProgramBasicInfo program = programCentersList.get(position);
//            Log.e("section ", section.toString());
            Map<String, List<OrgMemberMappingInfo.OrgCenterInfo>> programDataMap = new HashMap<String, List<OrgMemberMappingInfo.OrgCenterInfo>>();
//            sectionDataMap.put("courses", section.courses);
//
            if(program != null) {
               Log.e("programID..", ""+program.id);
                programDataMap.put("centers", program.centers);
            }


            view.setTag(programDataMap);
            return view;
        }
    }

    public class PostFeedAsyncTask extends AbstractVedantuJSONAsyncTask {

        private ProgressDialog pDialog;
//        private int position;
        private List<Actor> upVotedActorsList = new ArrayList<Actor>();
        private String message, statusType, statusTypeId;
        private List<OrgMemberMappingInfo.OrgCenterInfo> centersList;
        private String uuid, imageUrl;
        private boolean imageAttached = false;

        public PostFeedAsyncTask(SessionManager session, ProgressBar progressUpdater, String message, String statusType, String statusTypeId, List<OrgMemberMappingInfo.OrgCenterInfo> centersList, boolean attached, String uuid, String imageUrl) {
            super(session, progressUpdater);
            this.url = session.getApiUrl("addStatusFeed");
            this.message = message;
            this.statusType = statusType;
            this.statusTypeId = statusTypeId;
            this.centersList = centersList;
            imageAttached = attached;
            this.uuid = uuid;
            this.imageUrl = imageUrl;
            pDialog = new ProgressDialog(AddFeedActivity.this);
            pDialog.setMessage("Please wait..");
            pDialog.show();

            Log.e("postFeedAsyncTask", "call"+statusType);
        }

        @Override
        protected JSONObject doInBackground(String... params) {


//            session.addSessionParams(httpParams);
            httpParams.put("callingApp", "TapApp");
            httpParams.put("userRole", session.getOrgMemberInfo().profile);
            httpParams.put("callingUserId", session.getOrgMemberInfo().userId);
//            httpParams.put("entity.type", "STATUSFEED");
            httpParams.put("callingAppId", "TapApp");
            httpParams.put("statusMessage", message);
            httpParams.put("orgId",session.getOrgMemberInfo().orgId);
            httpParams.put("userId",session.getOrgMemberInfo().userId);
            httpParams.put("myUserId",session.getOrgMemberInfo().userId);
            httpParams.put("memberId",session.getOrgMemberInfo().memberId);
//            httpParams.put("entity.id","");

//            For sharing with all programs
            httpParams.put("with[0].type",statusType);
            httpParams.put("with[0].id",statusTypeId);


            if(centersList != null && centersList.size()>0){
                for(int i = 0 ;i<centersList.size();i++){
                    httpParams.put("with[0].centers["+i+"].type","CENTER");
                    httpParams.put("with[0].centers["+i+"].id",centersList.get(i).id);
                }
            }

            if(imageAttached){
               httpParams.put("entity.type", "STATUSFEED");
                httpParams.put("entity.id","");
                httpParams.put("source.linkType","UPLOADED");
                httpParams.put("source.type","IMAGE");
                httpParams.put("source.uuid",uuid);
                httpParams.put("source.image",imageUrl);
                httpParams.put("source.url",imageUrl);

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
                       Log.e("result", ".." + result.getJSONArray("list"));
                       Intent intent = new Intent();
                       Parcel p = new Parcel(result.getJSONArray("list").getJSONObject(0));
                       Bundle args = new Bundle();
                       args.putSerializable("events", p);
                       intent.putExtra("bundle", result.getJSONArray("list").getJSONObject(0).toString());
                       setResult(200, intent);
                       finish();
                   }else{
                       Toast.makeText(AddFeedActivity.this, "Some thing went wrong, Please try again later.", Toast.LENGTH_SHORT).show();
                   }

            } catch (JSONException e1) {
                e1.printStackTrace();
            }



            pDialog.dismiss();

        }
    }


   /* private void cameraIntent()
    {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void galleryIntent()
    {
        Intent intent = new Intent();
        intent.setType("image*//*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_FILE);
    }
*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);     } }


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
                    }, "uploadStatusImage");
            if (requestCode == GALLERY_REQUEST_CODE && data != null) {
                imageUri = data.getData();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){

                    imagePath =  RealPathUtil.getRealPathFromURI_API19(AddFeedActivity.this, imageUri);

                } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH && Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT){

                    imagePath =  RealPathUtil.getRealPathFromURI_API11to18(AddFeedActivity.this, imageUri);

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

    private void onCaptureImageResult(Uri imageUri) {
        Bitmap bm=null;
//        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
//        }
//        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
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


        Log.e("image url", bm.toString());
        imageView.setImageBitmap(bm);
        imageLayout.setVisibility(View.VISIBLE);
        uploadImageButton.setVisibility(View.GONE);
    }


    // Convert the image URI to the direct file system path of the image file
  private String getRealPathFromURI(Uri contentUri) {
      Cursor cursor = null;
      try {
          String[] proj = {MediaStore.Images.Media.DATA};
          cursor = getContentResolver().query(contentUri, proj, null, null,
                  null);
          int column_index = cursor
                  .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
          cursor.moveToFirst();
          return cursor.getString(column_index);
      } finally {
          if (cursor != null) {
              cursor.close();
          }
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


}
