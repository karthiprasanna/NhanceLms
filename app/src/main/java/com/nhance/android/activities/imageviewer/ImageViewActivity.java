package com.nhance.android.activities.imageviewer;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.nhance.android.activities.baseclasses.NhanceBaseActivity;
import com.nhance.android.async.tasks.ITaskProcessor;
import com.nhance.android.managers.LocalManager;
import com.nhance.android.R;

public class ImageViewActivity extends NhanceBaseActivity implements ITaskProcessor<Bitmap>{
	public static final String KEY_IMAGE_URL = "IMAGE_VIEWER_URL";
	public static final String KEY_IMAGE_DESC = "IMAGE_VIEWER_DESC";
	
	private final String TAG = "IMAGE_VIEWER";
	private ProgressDialog dialog;
	private Bitmap image = null;
	private TouchImageView imageView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_view);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		imageView = (TouchImageView) findViewById(R.id.image_viewer);
		imageView.setMaxZoom(4);
		Intent intent = getIntent();
		String url = intent.getStringExtra(KEY_IMAGE_URL);
		if(TextUtils.isEmpty(url)){
			finish();
		}else{
			@SuppressWarnings("deprecation")
			Object tempImageObj = getLastNonConfigurationInstance(); 
			if(tempImageObj == null){
				dialog = new ProgressDialog(ImageViewActivity.this);
				dialog.setCancelable(true);
				dialog.setOnCancelListener(new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						finish();
					}
				});
				dialog.setMessage(getResources().getString(R.string.loading));
				dialog.show();
				Log.d(TAG,"OPENING / FETCHING IMAGE URL ================ "+url);
				LocalManager.downloadImage(url, imageView, false, this);
			}else if(imageView!=null){
				Bitmap tmpImage = (Bitmap) tempImageObj;
				imageView.setImageBitmap(tmpImage);
				image = tmpImage;
			}
		}
		String desc = intent.getStringExtra(KEY_IMAGE_DESC);
		if(!TextUtils.isEmpty(desc)){
			imageView.setContentDescription(desc);
			TextView imgDesc = (TextView) findViewById(R.id.image_desc);
			imgDesc.setText(desc);
			imgDesc.bringToFront();
		}
	}
	/*private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
		TouchImageView imageView;

	    public DownloadImageTask(TouchImageView bmImage) {
	        this.imageView = bmImage;
	    }

	    protected Bitmap doInBackground(String... urls) {
	        String urldisplay = urls[0];
	        Bitmap mIcon11 = null;
	        try {
	            InputStream in = new java.net.URL(urldisplay).openStream();
	            mIcon11 = BitmapFactory.decodeStream(in);
	        } catch (Exception e) {
	            Log.e("Error", e.getMessage());
	            e.printStackTrace();
	        }
	        return mIcon11;
	    }

	    protected void onPostExecute(Bitmap result) {
	        imageView.setImageBitmap(result);
	    }
	    @SuppressLint("NewApi")
	    public DownloadImageTask executeTask(boolean singleThread, String url) {
	        if (singleThread || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
	            Log.d("ImageAsyncTask", "calling execute");
	            execute(url);
	        } else {
	            Log.d("ImageAsyncTask", "calling executeOnExecutor");
	            executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
	        }
	        return this;
	    }
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.image_view, menu);
		return true;
	}*/
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            	finish();
            	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
	@Override
	public void onTaskStart(Bitmap result) {
	}
	@Override
	public void onTaskPostExecute(boolean success, Bitmap result) {
		if(dialog!=null){
			dialog.dismiss();
		}
		if(!success || result == null){
			String text = getResources().getString(R.string.image_load_failed);
			Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
			finish();
		}else{
			image = result;
		}
	}

	@Override
	public Object onRetainCustomNonConfigurationInstance() {
		return image;
	}

	/*@Override
	public Object onRetainNonConfigurationInstance() {
	    return image;
	}*/


	@Override
	public void onDestroy(){
		if(dialog!=null){
			dialog.dismiss();
		}
		image = null;
		if(imageView != null){
			imageView.destroyDrawingCache();
		}
		super.onDestroy();
	}
}
