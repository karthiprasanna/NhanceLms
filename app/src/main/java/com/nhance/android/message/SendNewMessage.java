package com.nhance.android.message;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;

import com.nhance.android.R;
import com.nhance.android.assignment.StudentPerformance.NetUtils;
import com.nhance.android.async.tasks.ITaskProcessor;
import com.nhance.android.async.tasks.ImageUploaderTask;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.message.Adapter.MessageAdapter;
import com.nhance.android.message.Utils.Utility;
import com.nhance.android.utils.JSONUtils;
import com.nhance.android.utils.VedantuWebUtils;

import org.json.JSONArray;
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

import static com.nhance.android.R.id.autoComplete;


public class SendNewMessage extends AppCompatActivity {


    private static final String TAG = "SendNewMessage";
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;

    protected String              url;

    private SessionManager session;

    private JSONArray list;
    private JSONObject result, message, result2,message1;



    public String userConversationId, conversationId, firstMessageId,userId;
    ChatItem chatItem = new ChatItem();
    private String msg;

    private String chat_content;
    private String firstName;
    private String totalHits;
    private String thumbnail;
    private String profile;
    private long timestamp;
    private String userChoosenTask;
    public static final int SELECT_FILE = 101;
    public static final int REQUEST_CAMERA = 100;

    protected static final int    GALLERY_REQUEST_CODE = 101;
    protected static final int    CAMERA_REQUEST_CODE  = 102;

    protected static final String CAMERA_IMG_PREFIX    = "IMG_V_";
    protected File                cameraFileName;
    private String uuid, imageUrl;
    private boolean imageAttached = false;
    private String imagePath;
    private Uri imageUri;



    EditText editText, editTextsub;
    ImageView imageView;
    ImageView send, uploadImageButton;
    private String submit_editText,message_id;
    private String submit_editTextsub;
    private String autotext;
    private HashMap<String, String> list1;
    MultiAutoCompleteTextView acTextView;
    List<String> responseList;
    private List<ChatItem> feedsList;
    private MessageAdapter mAdapter;
    private String selectedtext;
    private ArrayList<String> selectedArrayList;
    private ImageView deleteImageButton;
    private LinearLayout imageLayout1;
    boolean emptyEditText = false;
    private String imgUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sendnewmessage);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getSupportActionBar().setDisplayShowCustomEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
       // getSupportActionBar().setIcon(R.drawable.ic_launcher);
        //getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_launcher);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
      mRecyclerView.setLayoutManager(mLayoutManager);



       // session = SessionManager.getInstance(getApplicationContext());
        Intent intent = getIntent();
        session = SessionManager.getInstance(this);
        userConversationId = intent.getStringExtra("userConversationId");
        conversationId = intent.getStringExtra("conversationId");
        firstMessageId = intent.getStringExtra("firstMessageId");
        userId = intent.getStringExtra("userId");

        editTextsub = (EditText) findViewById(R.id.Etextsub);
        editText = (EditText) findViewById(R.id.Etext1);
        send = (ImageView) findViewById(R.id.chatSendButton1);
        deleteImageButton = (ImageView) findViewById(R.id.deleteImageButtonmsg);
        uploadImageButton = (ImageView) findViewById(R.id.uploadImageButton);
        imageLayout1 = (LinearLayout)findViewById(R.id.linear1);
        imageView =(ImageView)findViewById(R.id.imageView);
        acTextView=(MultiAutoCompleteTextView)findViewById(R.id.autoComplete);

        list1=new HashMap<String, String>();

        selectedArrayList = new ArrayList<String>();

        new GetmembersAsyncTask().execute();


        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                autotext =acTextView .getText().toString().trim();
               //acTextView.getText().clear();
                String[] selectedStringArray = autotext.split(",");
                Log.e("selectedStringArray", ".........." + autotext.toString());
                //String intermediate_text=acTextView.getText().toString();
                //String final_string=intermediate_text.substring(intermediate_text.lastIndexOf(",")+1);

                //Log.e("intermediate_text", ".........." + intermediate_text);
                Log.e("final_string", ".........." + selectedStringArray.length);

                /*for(String name : selectedStringArray){

                    selectedArrayList.add(list1.get(selectedStringArray));
                    Log.e("auto selectedArrayList", ".........." + selectedArrayList.toString());
                }*/
                for(int i =0; i<selectedStringArray.length;i++){

                    // selectedArrayList.add(selectedStringArray.);
                    selectedArrayList.add(list1.get(selectedStringArray[i].toString().trim()));
                    Log.e("auto selectedArrayList", ".........." + selectedStringArray[i].toString().trim());
                }
                submit_editText = editText.getText().toString().trim();
           //editText.getText().clear();
                Log.e("submit_editText", ".........." + submit_editText);
                Log.e("auto text", ".........." + autotext);

                submit_editTextsub = editTextsub.getText().toString().trim();
             //editTextsub.getText().clear();
                Log.e("submit_editTextsub", ".........." + submit_editTextsub);


                Intent intent = getIntent();
                session = SessionManager.getInstance(getApplicationContext());
                userConversationId = intent.getStringExtra("userConversationId");
                conversationId = intent.getStringExtra("conversationId");
                firstMessageId = intent.getStringExtra("firstMessageId");

                System.out.println("Url ---"+conversationId+ " "+firstMessageId);

                //new ChatAsyncTask2().execute();
               // new ChatAsyncTask2(session,imageAttached, uuid, imageUrl).execute();
                if(NetUtils.isOnline(SendNewMessage.this)) {

                if( !acTextView.getText().toString().trim().isEmpty() &&  !editTextsub.getText().toString().trim().isEmpty() && !editText.getText().toString().trim().isEmpty()) {
                    new ChatAsyncTask2(session,imageAttached, uuid, imageUrl).execute();
                    emptyEditText = true;
                    Toast.makeText(SendNewMessage.this, "Message send successfully", Toast.LENGTH_SHORT).show();
                    clearfield();
                }else {

                    emptyEditText = false;
                    Toast.makeText(SendNewMessage.this, "Please fill the all fields", Toast.LENGTH_SHORT).show();
                }
                }else{
                    Toast.makeText(SendNewMessage.this, R.string.no_internet_msg, Toast.LENGTH_SHORT).show();
                }

                deleteImageButton.setVisibility(View.GONE);

            }


        });

        uploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // selectImage();
                //Toast.makeText(v.getContext(), "Deleted", Toast.LENGTH_LONG).show();




                showSelectedPictureDialogue();
            }
        });


        deleteImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImageButton.setVisibility(View.VISIBLE);
                imageLayout1.setVisibility(View.GONE);
            }
        });
    }

  /*  private void addItemsToList() {

        new ChatAsyncTask2(session,imageAttached, uuid, imgUrl).execute();
        mAdapter.notifyDataSetChanged();
    }*/


    private void clearfield(){

        acTextView.getText().clear();
        editText.getText().clear();
        editTextsub.getText().clear();
        imageView.setImageDrawable(null);
    }
    private void selectImage() {
        final CharSequence[] items = {getResources().getString(R.string.takePhoto), getResources().getString(R.string.chooseFromGallery),
                getResources().getString(R.string.close)};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result = Utility.checkPermission(getApplicationContext());
                if (items[item].equals(getResources().getString(R.string.takePhoto))) {

                    userChoosenTask = getResources().getString(R.string.takePhoto);
                    if (result)
                        cameraIntent();
                } else if (items[item].equals(getResources().getString(R.string.chooseFromGallery))) {
                    userChoosenTask = getResources().getString(R.string.chooseFromGallery);
                    if (result)
                        galleryIntent();
                } else if (items[item].equals(getResources().getString(R.string.close))) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();


    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

/*
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (userChoosenTask.equals(getResources().getString(R.string.takePhoto)))
                        cameraIntent();
                    else if (userChoosenTask.equals(getResources().getString(R.string.chooseFromGallery)))
                        galleryIntent();
                } else {
//code for deny
                }
                break;
        }
    }*/

   /* @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }*/



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
                                         imgUrl = src;
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

                    imagePath =  RealPathUtil.getRealPathFromURI_API19(SendNewMessage.this, imageUri);

                } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH && Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT){

                    imagePath =  RealPathUtil.getRealPathFromURI_API11to18(SendNewMessage.this, imageUri);

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
                imageLayout1.setVisibility(View.VISIBLE);
                uploadImageButton.setVisibility(View.GONE);
//                }



//                imageUri = data.getData();
               /* Log.d(TAG, "file output: " + cameraFileName);
                if (cameraFileName != null) {
                    Log.d(TAG,"file created at : " + cameraFileName + ", exist :"+cameraFileName.exists());
                    upImageUploaderTask.executeTask(false, cameraFileName.getAbsolutePath());
//                    onCaptureImageResult(imageUri);
                }*/
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
        imageLayout1.setVisibility(View.VISIBLE);
        uploadImageButton.setVisibility(View.VISIBLE);
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {

        Bitmap bm = null;
        if (data != null) {

            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
            File imageFile = new File(String.valueOf(Environment.getExternalStorageDirectory()));

            Log.e("URL", imageFile.toString());
            FileOutputStream fo;
            try {
                imageFile.createNewFile();
                fo = new FileOutputStream(imageFile);
                fo.write(bytes.toByteArray());
                fo.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            //uploadFile(imageFile);
            imageView.setImageBitmap(bm);
        }
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
        imageView.setVisibility(View.VISIBLE);
        uploadImageButton.setVisibility(View.VISIBLE);
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

        AlertDialog.Builder getImageFrom = new AlertDialog.Builder(this);
        getImageFrom.setTitle("Select:");
        final CharSequence[] opsChars = { getResources().getString(R.string.take_picture),
                getResources().getString(R.string.choose_gallery),"Cancel"};
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
                else if (which == 2) {
                    dialog.dismiss();
                }


            }
        });
        getImageFrom.create().show();
    }


    /*private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");


        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        File destination = new File(String.valueOf(Environment.getExternalStorageDirectory()));

        Log.e("URL", destination.toString());
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

        // uploadFile(destination);
        imageView.setImageBitmap(thumbnail);
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    //post text to server

    public class ChatAsyncTask2 extends AsyncTask<JSONObject, JSONObject, JSONObject> {

        private String uuid, imageUrl;
        private boolean imageAttached = false;
        private ProgressDialog pDialog;

        public ChatAsyncTask2(SessionManager session, boolean attached, String uuid, String imageUrl) {
            super();
            String url = session.getApiUrl("sendMessage");
            System.out.println("Url ---" + url);
            imageAttached = attached;
            this.uuid = uuid;
            this.imageUrl = imageUrl;
            pDialog = new ProgressDialog(SendNewMessage.this);
            pDialog.setMessage("Please wait..");
            pDialog.show();

        }

        @Override
        protected JSONObject doInBackground(JSONObject... jsonObjects) {

            String url = session.getApiUrl("sendMessage");
            System.out.println("Url ---" + url);


            JSONObject jsonRes = null;

            Map<String, Object> httpParams = new HashMap<String, Object>();
            httpParams.put("callingApp", "TapApp");
            httpParams.put("callingUserId", session.getOrgMemberInfo().userId);
            httpParams.put("userRole", session.getOrgMemberInfo().profile);
            httpParams.put("callingAppId", "TapApp");
            httpParams.put("userId", session.getOrgMemberInfo().userId);

            httpParams.put("orgId", session.getOrgMemberInfo().orgId);
            httpParams.put("memberId", session.getOrgMemberInfo().userId);
            httpParams.put("messageId", message_id);
            httpParams.put("message.conversationId", conversationId);
            httpParams.put("myUserId", session.getOrgMemberInfo().userId);
            httpParams.put("message.content", submit_editText);
            Log.e("message.content", "......." + submit_editText);

            httpParams.put("message.subject", submit_editTextsub);
            Log.e("message.subject", "......." + submit_editTextsub);

            httpParams.put("message.action", "SEND");
            httpParams.put("message.sender.type", "USER");
            httpParams.put("message.sender.id", session.getOrgMemberInfo().userId);

//System.out.println("selectedArrayList  "+selectedArrayList);
            for (int i = 0; i < selectedArrayList.size(); i++) {
                Log.e("id", ""+selectedArrayList.get(i));
                httpParams.put("message.receivers[" + i + "].id", selectedArrayList.get(i));
                httpParams.put("message.receivers[" + i + "].type", "USER");

            }


         if(imageAttached){


          String imageUrl2 = String.format("<div class=\"RTEImageDiv\"> \n <img src=\""+imgUrl+"\" v-uid=\""+uuid+"\" class=\"vUrl\" /> </div \">"+submit_editText+"</div>");

             httpParams.put("message.content", imageUrl2);
            }else {
             httpParams.put("message.content", submit_editText);




            session.addSessionParams(httpParams);
        }
            try {
                jsonRes = VedantuWebUtils.getJSONData(url, VedantuWebUtils.POST_REQ, httpParams);
                Log.e("chatresponse1", "......." + jsonRes);

            } catch (Exception e) {
                e.printStackTrace();
            }



            if (isCancelled()) {

                pDialog.dismiss();
                return null;
            }
            return jsonRes;
        }

        @Override
        protected void onPostExecute (JSONObject jsonObject){
            super.onPostExecute(jsonObject);


            try {
                result2 = jsonObject.getJSONObject("result");
                message = result2.getJSONObject("message");
                Log.e("summary of response", "......." + message);

                JSONObject msg = message.getJSONObject("receiver");

                String firstName = msg.getString("firstName");
                Log.e("receiver response", "......." + firstName);
                String thumbnail = msg.getString("thumbnail");
                Log.e("receiver response", "......." + thumbnail);
                String content = msg.getString("content");
                Log.e("receiver response", "......." + content);
                String profile = msg.getString("profile");
                Log.e("receiver response", "......." + profile);
                Long timestamp = msg.getLong("timestamp");
                Log.e("receiver response", "......." + timestamp);




                feedsList.add(chatItem);
                Log.e("feed item list", ".." + feedsList.size());

            } catch (JSONException e1) {
                e1.printStackTrace();
            }


            //Collections.reverse(feedsList);
            mAdapter = new MessageAdapter(SendNewMessage.this, feedsList);
            mRecyclerView.setAdapter(mAdapter);

            pDialog.dismiss();

        }


    }
    public class GetmembersAsyncTask extends AsyncTask<JSONObject, JSONObject, JSONObject> {

        @Override
        protected JSONObject doInBackground(JSONObject... jsonObjects) {
            String url = session.getApiUrl("getMembers");

            JSONObject jsonRes = null;

            HashMap<String,  Object> httpParams = new HashMap<String, Object>();

            httpParams.put("callingApp", "web-app");
            httpParams.put("callingUserId", session.getOrgMemberInfo().userId);
            // httpParams.put("userRole", session.getOrgMemberInfo().profile);
            httpParams.put("callingAppId", "web-app");
            //  httpParams.put("orgId", session.getOrgMemberInfo().orgId);
            httpParams.put("userId", session.getOrgMemberInfo().userId);
            // httpParams.put("myUserId", session.getOrgMemberInfo().userId);
            httpParams.put("contentSrc.id",  session.getOrgMemberInfo().orgId);
            // httpParams.put("memberId", session.getOrgMemberInfo().memberId);
            httpParams.put("contentSrc.type", "ORGANIZATION");
            httpParams.put("searchAll", "true");
            httpParams.put("start", 0);
            httpParams.put("excludeProfiles", "OFFLINE_USER ");



            session.addSessionParams(httpParams);




            try {
                jsonRes = VedantuWebUtils.getJSONData(url, VedantuWebUtils.GET_REQ, httpParams);
                Log.e("jsonrespgetmembers", "......." + jsonRes);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return jsonRes;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            responseList = new ArrayList<>();


            try {
                result = jsonObject.getJSONObject("result");

                list = result.getJSONArray("list");
                Log.e("list of response", "......." + list);


                for (int i = 0; i < list.length(); i++) {


                    JSONObject msglist = list.getJSONObject(i);


                    String firstName=msglist.getString("firstName");
                    Log.e("1stName", "......." + firstName+".."+msglist.toString());

                    String userId=msglist.getString("userId");
                    Log.e("UserId", "......." + userId);

                    responseList.add(firstName);
                    list1.put(firstName, userId);
                    Log.e("ResponseList", "......." + responseList);
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
                        android.R.layout.simple_dropdown_item_1line, responseList);

                acTextView = (MultiAutoCompleteTextView ) findViewById(autoComplete);

                adapter.notifyDataSetChanged();
                acTextView.setThreshold(1);
                acTextView.setAdapter(adapter);

                acTextView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
                selectedtext=acTextView.getText().toString();

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }


}

