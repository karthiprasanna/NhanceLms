package com.nhance.android.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.http.AndroidHttpClient;
import android.os.Environment;
import android.provider.MediaStore.Video.Thumbnails;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

import com.nhance.android.async.tasks.AbstractVedantuAsynTask;
import com.nhance.android.async.tasks.ITaskProcessor;

public class ThumbnailDownloader {

    private static final String             TAG            = "ThumbnailDownloader";
    private static final String             CACHE_FILE_DIR = Environment
                                                                   .getExternalStorageDirectory()
                                                                   .getAbsolutePath()
                                                                   + File.separator
                                                                   + "nhance"
                                                                   + File.separator + "thumb";

    private static LruCache<String, Bitmap> mMemoryCache   = new LruCache<String, Bitmap>(10);

    public void downloadVideoThumb(String filePath, ImageView imageView) {

        if (cancelPotentialDownload(filePath, imageView)) {
            VideoThumbDownloaderTask task = new VideoThumbDownloaderTask(imageView);
            DownloadedDrawable downloadedDrawable = new DownloadedDrawable(task);
            imageView.setImageDrawable(downloadedDrawable);
            task.execute(filePath);
        }
    }

    public void download(String url, ImageView imageView, boolean useFileCache,
            ITaskProcessor<Bitmap> taskProcessor) {

        if (cancelPotentialDownload(url, imageView)) {
            // check if this image exist in lruCache
            if (url != null && mMemoryCache.get(url) != null) {
                Bitmap imageBitmap = mMemoryCache.get(url);
                imageView.setImageBitmap(imageBitmap);
                if (taskProcessor != null) {
                    taskProcessor.onTaskPostExecute(true, imageBitmap);
                }
                return;
            }

            BitmapDownloaderTask task = new BitmapDownloaderTask(imageView, useFileCache,
                    taskProcessor);
            DownloadedDrawable downloadedDrawable = new DownloadedDrawable(task);
            imageView.setImageDrawable(downloadedDrawable);
            task.executeTask(false, url);
        }
    }

    private static boolean cancelPotentialDownload(String url, ImageView imageView) {

        BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);

        if (bitmapDownloaderTask != null) {
            String bitmapUrl = bitmapDownloaderTask.url;
            if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
                bitmapDownloaderTask.cancel(true);
            } else {
                // URL is already being downloaded.
                return false;
            }
        }
        return true;
    }

    private static BitmapDownloaderTask getBitmapDownloaderTask(ImageView imageView) {

        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof DownloadedDrawable) {
                DownloadedDrawable downloadedDrawable = (DownloadedDrawable) drawable;
                return downloadedDrawable.getBitmapDownloaderTask();
            }
        }
        return null;
    }

    /**
     * @param url
     *            -> the image need to be downloaded
     * @param useFileCache
     *            -> weather to store this image locally of not, if true then it will 1st check
     *            weather the image is available locally is not it will download and store locally
     * @return
     */
    @SuppressWarnings("deprecation")
    private Bitmap downloadBitmap(String url, boolean useFileCache) {

        Bitmap bitmap = null;
        if (useFileCache) {
            bitmap = getBitmapFromLocal(url);
            if (bitmap != null) {
                return bitmap;
            }
        }
        final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
        final HttpGet getRequest = new HttpGet(url);

        try {
            HttpResponse response = client.execute(getRequest);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                Log.w(TAG, "Error " + statusCode + " while downloading bitmap from " + url);
                return null;
            }

            final HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream inputStream = null;
                try {
                    inputStream = new BufferedInputStream(entity.getContent());
                    if (useFileCache) {
                        int count = 0;
                        byte data[] = new byte[1024 * 16];
                        File file = new File(CACHE_FILE_DIR, StringUtils.substringAfterLast(url,
                                File.separator));
                        if (!file.getParentFile().exists()) {
                            file.getParentFile().mkdirs();
                        }
                        OutputStream output = null;
                        try {
                            output = new FileOutputStream(file);
                            Log.d(TAG, "userFileCache: " + useFileCache + ", saving file to  : "
                                    + file.getAbsolutePath());
                            while ((count = inputStream.read(data)) != -1) {
                                // writing data to file
                                output.write(data, 0, count);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage(), e);
                        } finally {
                            if (output != null) {
                                output.flush();
                                output.close();
                            }
                            if (inputStream != null) {
                                inputStream.close();
                            }
                        }
                        return BitmapFactory.decodeFile(file.getAbsolutePath());
                    }

                    inputStream.mark(inputStream.available());
                    final BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeStream(inputStream, null, options);
                    // Calculate inSampleSize
                    options.inSampleSize = calculateInSampleSize(options, 550, 900);
                    // Decode bitmap with inSampleSize set false
                    options.inJustDecodeBounds = false;

                    try {
                        // this gives error for larger images specially in SDK version 4.1+
                        inputStream.reset();
                    } catch (IOException e) {
                        IOUtils.closeStream(inputStream);
                        options.inPreferredConfig = Config.RGB_565;
                        options.inSampleSize = calculateInSampleSize(options, 550, 900);
                        response = client.execute(getRequest);
                        inputStream = new BufferedInputStream(response.getEntity().getContent());
                    }

                    bitmap = BitmapFactory.decodeStream(inputStream, null, options);
                    return bitmap;
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    entity.consumeContent();
                }
            }
        } catch (Throwable e) {
            getRequest.abort();
            Log.e(TAG,
                    "Error while downloading bitmap from " + url + ", errorMessage:"
                            + e.getMessage(), e);
        } finally {
            if (client != null) {
                client.close();
            }
        }
        return null;
    }

    private Bitmap getBitmapFromLocal(String url) {

        String fileName = StringUtils.substringAfterLast(url, File.separator);
        File file = new File(CACHE_FILE_DIR, fileName);
        if (!file.exists()) {
            Log.e(TAG, "file : " + file.getAbsolutePath() + " does not exist");
            return null;
        }
        final Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        return bitmap;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqMaxWidth,
            int reqMaxHeight) {

        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqMaxHeight || width > reqMaxWidth) {

            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float) height / (float) reqMaxHeight);
            final int widthRatio = Math.round((float) width / (float) reqMaxWidth);

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    class VideoThumbDownloaderTask extends BitmapDownloaderTask {

        public VideoThumbDownloaderTask(ImageView imageView) {

            super(imageView, false, null);
        }

        @Override
        protected Bitmap doInBackground(String... params) {

            Bitmap thumb = ThumbnailUtils.createVideoThumbnail(params[0], Thumbnails.MINI_KIND);
            return thumb;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {

            super.onPostExecute(bitmap);
            if (isCancelled()) {
                bitmap = null;
            }
            if (imageViewReference != null) {
                ImageView imageView = imageViewReference.get();
                BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);
                // Change bitmap only if this process is still associated with
                // it
                if (this == bitmapDownloaderTask) {
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                    }
                }
            }
        }

    }

    class BitmapDownloaderTask extends AbstractVedantuAsynTask<String, Void, Bitmap> {

        protected String                         url;
        protected boolean                        useFileCache;
        protected final WeakReference<ImageView> imageViewReference;
        ITaskProcessor<Bitmap>                   taskProcessor;

        public BitmapDownloaderTask(ImageView imageView, boolean useFileCache,
                ITaskProcessor<Bitmap> taskProcessor) {

            imageViewReference = new WeakReference<ImageView>(imageView);
            this.useFileCache = useFileCache;
            this.taskProcessor = taskProcessor;
        }

        @Override
        protected Bitmap doInBackground(String... params) {

            // params[0] is the url.
            this.url = params[0];
            return downloadBitmap(params[0], useFileCache);
        }

        // Once the image is downloaded, associates it to the imageView
        @Override
        protected void onPostExecute(Bitmap bitmap) {

            if (isCancelled()) {
                bitmap = null;
            }

            if (imageViewReference != null) {
                ImageView imageView = imageViewReference.get();
                BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);
                // Change bitmap only if this process is still associated with
                // it
                if (this == bitmapDownloaderTask) {
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                        if (!useFileCache) {
                            mMemoryCache.put(this.url, bitmap);
                        }
                    }
                }
            }

            if (taskProcessor != null) {
                taskProcessor.onTaskPostExecute(bitmap != null, bitmap);
            }
        }
    }

    static class DownloadedDrawable extends BitmapDrawable {

        private final WeakReference<BitmapDownloaderTask> bitmapDownloaderTaskReference;

        @SuppressWarnings("deprecation")
        public DownloadedDrawable(BitmapDownloaderTask bitmapDownloaderTask) {

            super(drawableToBitmap(bitmapDownloaderTask.imageViewReference.get().getDrawable()));
            bitmapDownloaderTaskReference = new WeakReference<BitmapDownloaderTask>(
                    bitmapDownloaderTask);
        }

        public BitmapDownloaderTask getBitmapDownloaderTask() {

            return bitmapDownloaderTaskReference.get();
        }

    }

    public static Bitmap drawableToBitmap(Drawable drawable) {

        if (drawable == null) {
            return null;
        }
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        width = width > 0 ? width : 1;
        int height = drawable.getIntrinsicHeight();
        height = height > 0 ? height : 1;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

}
