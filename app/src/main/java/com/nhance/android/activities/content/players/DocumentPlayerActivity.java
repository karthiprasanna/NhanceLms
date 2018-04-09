package com.nhance.android.activities.content.players;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.nhance.android.activities.baseclasses.NhanceBaseActivity;
import com.nhance.android.async.tasks.AbstractDownloadProcessor;
import com.nhance.android.async.tasks.AbstractVedantuAsynTask;
import com.nhance.android.async.tasks.IDownloadCompleteProcessor;
import com.nhance.android.async.tasks.RemoteDocumentFileDownloader;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.ContentDataManager;
import com.nhance.android.db.datamanagers.DownloadHistoryManager;
import com.nhance.android.db.datamanagers.ModuleStatusDataManager;
import com.nhance.android.db.models.DownloadHistory;
import com.nhance.android.downloader.DownloadInfo.DownloadState;
import com.nhance.android.enums.EncryptionLevel;
import com.nhance.android.enums.EntityType;
import com.nhance.android.managers.FileManager;
import com.nhance.android.managers.LocalManager;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.pojos.LibraryContentRes;
import com.nhance.android.pojos.content.infos.DocumentExtendedInfo;
import com.nhance.android.pojos.content.infos.FileExtendedInfo;
import com.nhance.android.pojos.content.infos.IContentInfo;
import com.nhance.android.processors.FileMaskProcessor;
import com.nhance.android.processors.OnFileDownloadCompleteProcessor;
import com.nhance.android.utils.GoogleAnalyticsUtils;
import com.nhance.android.utils.IOUtils;
import com.nhance.android.utils.server.VedantuFileInputStream;
import com.nhance.android.R;



public class DocumentPlayerActivity extends NhanceBaseActivity {



    private static final String       TAG = "DocumentPlayerActivity";
    private LibraryContentRes         content;
    RemoteDocumentDownloaderProcessor docDownloadProcessor;
    ContentDataManager                cDataManager;
    IContentInfo                      mContentExtendInfo;
    private String extension;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        GoogleAnalyticsUtils.setScreenName(ConstantGlobal.GA_SCREEN_DOCUMENT);

        GoogleAnalyticsUtils.setCustomDimensions(
                getIntent().getStringExtra(ConstantGlobal.ENTITY_ID),
                getIntent().getStringExtra(ConstantGlobal.NAME), ConstantGlobal.GA_SCREEN_DOCUMENT);
        setContentView(R.layout.activity_document_player);
        String linkId = getIntent().getStringExtra(ConstantGlobal.LINK_ID);
        cDataManager = new ContentDataManager(getBaseContext());
        content = cDataManager.getLibraryContentRes(linkId);
        if (content == null) {
            Toast.makeText(getBaseContext(), "No Content Found", Toast.LENGTH_LONG).show();
            onStop();
            return;
        }
        mContentExtendInfo = content.toContentExtendedInfo();
        if (StringUtils.isNotEmpty(getIntent().getStringExtra(ConstantGlobal.MODULE_ID))) {
            String moduleId = getIntent().getStringExtra(ConstantGlobal.MODULE_ID);
            String entityId = getIntent().getStringExtra(ConstantGlobal.ENTITY_ID);
            try {
                (new ModuleStatusDataManager(this)).updateModuleEntryStatus(
                        session.getSessionStringValue(ConstantGlobal.USER_ID),
                        session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID), moduleId, entityId,
                        content.__getEntityType().name());
            } catch (Exception e) {
                Log.d(TAG, "Some error occured in updating document of id: " + entityId
                        + " Error: " + e.getMessage());
            }
        }
        DownloadHistory dHistory = new DownloadHistoryManager(getApplicationContext())
                .getDownloadHistory(content._id);
        Log.d(TAG, "=================== downloadHistory : " + dHistory
                + " ========================= ");
        if (!content.downloaded || dHistory == null) {
            if (content.downloaded) {
                ContentValues values = new ContentValues();
                values.put(ConstantGlobal.DOWNLOADED, 0);
                cDataManager.updateContent(content._id, values);
            }

            if (dHistory != null && dHistory.status == DownloadState.FINISHED.toInt()) {
                File file = new File(dHistory.file);
                boolean started = file.exists()
                        && startContentInNativeReader(content,
                                EncryptionLevel.valueOfKey(content.encLevel), file, this);
                if (started) {
                    recordStudyHistory();
                    finish();
                    return;
                }
            }

            String url = null;
            if (mContentExtendInfo instanceof FileExtendedInfo) {
                url = ((FileExtendedInfo) mContentExtendInfo).url;
            } else if (mContentExtendInfo instanceof DocumentExtendedInfo) {
                url = ((DocumentExtendedInfo) mContentExtendInfo).url;
            }

            // check if the file is present in sd card
            if (!TextUtils.isEmpty(url)) {
                String filePath = FileManager.getSDCardPath(content, FileManager
                        .getEncryptedFileName(StringUtils.substringAfterLast(url, File.separator)),
                        null, null, getApplicationContext(), false);
                if (!TextUtils.isEmpty(filePath)) {
                    File file = new File(filePath);
                    Log.d(TAG, "filePath: " + filePath + ", exists: " + file.exists());
                    boolean started = file.exists()
                            && startContentInNativeReader(content,
                                    EncryptionLevel.valueOfKey(content.encLevel), file, this);
                    Log.d(TAG, "opened file: " + started);
                    if (started) {
                        recordStudyHistory();
                        finish();
                    }

                    if (file.exists()) {
                        return;
                    }
                }
            }

            if (!SessionManager.isOnline()) {
                Toast.makeText(getApplicationContext(), R.string.no_internet_msg,
                        Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            ProgressBar pBar = (ProgressBar) findViewById(R.id.document_loader_progressBar);
            TextView pCount = (TextView) findViewById(R.id.document_loader_progressCount);

            OnFileDownloadCompleteProcessor downloadCompleteProcessor = new OnFileDownloadCompleteProcessor(
                    content, this, true);

            if (docDownloadProcessor == null) {
                docDownloadProcessor = new RemoteDocumentDownloaderProcessor(this, content, pBar,
                        pCount);
            } else {
                docDownloadProcessor.progressBar = pBar;
                docDownloadProcessor.percentageView = pCount;
            }
            docDownloadProcessor.setDownloadCompleteProcessor(downloadCompleteProcessor);
            boolean started = docDownloadProcessor.startDownload(true);
            Log.d(TAG, "Download started status: " + started);
            if (!started) {
                finish();
                return;
            }
        } else {


            // TODO: check if the dHistory is null or is in progress
            // String filePath = TextUtils.join(File.separator,
            // new String[] {
            // ContentDataManager.getContentDir(doc.type.toLowerCase()),
            // StringUtils.substringAfterLast(dInfo.url, File.separator) });

            EncryptionLevel encLevel = EncryptionLevel.valueOfKey(content.encLevel);
            Log.d(TAG, "encryptionLevel: " + encLevel);
            File file = new File(dHistory.file);
            if (!file.exists()) {
                Toast.makeText(getApplicationContext(), R.string.error_file_not_present,
                        Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            Log.d(TAG, "content[file] local path : " + file.getAbsolutePath() + ", available: "
                    + file.exists());
            boolean started = startContentInNativeReader(content, encLevel, file, this);
            Log.d(TAG, "Download started status2: " + started);
            if (started) {
                recordStudyHistory();
                finish();
            }
        }
    }

    private void recordStudyHistory() {

        LocalManager.recordStudyHistory(
                getApplicationContext(),
                content.orgKeyId,
                SessionManager.getInstance(getApplicationContext()).getSessionStringValue(
                        ConstantGlobal.USER_ID), content._id, content.linkId, content.id,
                EntityType.valueOfKey(content.type));
        cDataManager.updateLastViewed(content._id);
    }

    public boolean startContentInNativeReader(LibraryContentRes doc, EncryptionLevel encLevel,
            File file, Context context) {

        if (encLevel != EncryptionLevel.NA) {
            // return flase from here and start background decryption and handle
            // the result there
            Log.d(TAG, "Starting background decryption and handle the result there");
            decryptDocumentFile(encLevel, file, context, content.name);
            return false;

        } else {
            Log.d(TAG, "Launching File " + content.name);
            return launchFile(file, context, content.name, null);
        }
    }

    public boolean launchFile(File file, Context context, String displayFileName, String mPassword) {




        Log.e("jjj","........"+file);
        Log.e("jjjggg","........"+displayFileName);
        String uri = file.toString();
        extension = uri.substring(uri.lastIndexOf("."));

        if (extension!=null  && (extension.contains(".pdf"))) {
            Intent intent = new Intent(this, PdfActivity
                    .class);

            ArrayList<File> pdffile = new ArrayList<>();
            pdffile.add(file);


            intent.putExtra("pdffile", pdffile);
            intent.putExtra("displayFileName", displayFileName);

            startActivity(intent);

        }else {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        boolean useVedantuReader = EntityType.valueOfKey(content.type).equals(EntityType.DOCUMENT);
        if (useVedantuReader) {
            intent.setPackage(ConstantGlobal.PACKAGE_NAME_VEDANTU_READER);
        }

        if (!TextUtils.isEmpty(mPassword)) {
            intent.putExtra("VEDANTU_READER_PASSWORD", mPassword);
            intent.putExtra("VEDANTU_READER_IS_LINK_BUTTON_ACTIVE", true);
            intent.putExtra("VEDANTU_READER_HIDE_BUTTONS", new String[] {
                    "VEDANTU_READER_MORE_BUTTON", "VEDANTU_READER_PRINT_BUTTON",
                    "VEDANTU_READER_COPY_TEXT_BUTTON", "VEDANTU_READER_REFLOW_BUTTON" });
        }

        final String fileExtention = MimeTypeMap.getFileExtensionFromUrl(file.getAbsolutePath());
//        Uri apkURI = FileProvider.getUriForFile(
//                context,
//                context.getApplicationContext()
//                        .getPackageName() + ".provider", file);
//        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//        Log.e("apkURI....", ""+apkURI);
        intent.setDataAndType(Uri.fromFile(file), MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(fileExtention));
        try {
            if (findTargetAppPackage(intent) == null) {
                throw new ActivityNotFoundException();
            }
            context.startActivity(intent);
       } catch (ActivityNotFoundException e) {
            Log.e(TAG, e.getMessage(), e);
            if (displayFileName == null) {
                displayFileName = file.getName();
            }
            try {
                final String playStoreIntentUlr = useVedantuReader ? ("market://details?id=" + ConstantGlobal.PACKAGE_NAME_VEDANTU_READER)
                        : ("market://search?q=" + fileExtention);

                String errorMgs = useVedantuReader ? "This is a secure document. Install Vedantu Reader from Play Store to open it."
                        : "Could not find an app that opens \"" + displayFileName + "."
                                + fileExtention
                                + "\", do you want to search for supporting app on play store ?";
                String neutralButton = useVedantuReader ? "Install Now" : "Yes";
                AlertDialog alertDialog = new AlertDialog.Builder(context).setMessage(errorMgs)
                        .setNeutralButton(neutralButton, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int id) {

                                dialog.cancel();
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                intent.setData(Uri.parse(playStoreIntentUlr));
                                startActivity(intent);

                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int id) {

                                dialog.cancel();
                            }
                        }).create();
                alertDialog.setOnDismissListener(new OnDismissListener() {

                    @Override
                    public void onDismiss(DialogInterface dialog) {

                        dialog.cancel();
                        finish();
                    }
                });
                alertDialog.show();
            } catch (Throwable e1) {
                Log.e(TAG, e1.getMessage(), e1);
                finish();
            }
            return false;
        }
Toast.makeText(this,"content",Toast.LENGTH_SHORT).show();

}


       return true;
    }



    @Override
    public void onBackPressed() {

        super.onBackPressed();
        if (docDownloadProcessor != null) {
            docDownloadProcessor.cancle(true);
        }

        finish();
    }

    class RemoteDocumentDownloaderProcessor extends AbstractDownloadProcessor {

        private static final String              TAG = "RemoteDocumentDownloaderProcessor";
        public ProgressBar                       progressBar;
        public TextView                          percentageView;
        private IDownloadCompleteProcessor<File> downloadCompleteProcessor;
        private RemoteDocumentFileDownloader     dTask;

        public RemoteDocumentDownloaderProcessor(Context context, LibraryContentRes content,
                ProgressBar progressBar, TextView percentageView) {

            super(context, content);
            this.progressBar = progressBar;
            this.percentageView = percentageView;
        }

        public void setDownloadCompleteProcessor(
                IDownloadCompleteProcessor<File> downloadCompleteProcessor) {

            this.downloadCompleteProcessor = downloadCompleteProcessor;
        }//

        @Override
        protected void setUpDownloaderTask(boolean skipUrlCheck, String downloadUrl) {

            if (TextUtils.isEmpty(downloadUrl)) {
                Log.e(TAG, "error occured while fetching data from server downloadUrl: "
                        + downloadUrl);
                Toast.makeText(context, "error occured while fetching data from server ",
                        Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            String filePath = TextUtils.join(
                    File.separator,
                    new String[] { ContentDataManager.getTempContentDir(),
                            StringUtils.substringAfterLast(downloadUrl, File.separator) });

            if (dTask == null) {
                dTask = new RemoteDocumentFileDownloader(content, context, filePath, downloadUrl,
                        progressBar, percentageView, docDownloadProcessor,
                        downloadCompleteProcessor);
                Log.d(TAG, "submiting task to THREAD_POOL_EXECUTOR");
                dTask.executeTask(false);
            } else {
                dTask.progressBar = progressBar;
                dTask.percentageView = percentageView;
            }
        }

        @Override
        public void onTaskPostExecute(boolean success, File result) {

            super.onTaskPostExecute(success, result);
            if (result == null || !success) {
                Toast.makeText(context, "error while downloading data from server",
                        Toast.LENGTH_SHORT).show();
                finish();
                dTask = null;
                return;
            }
            Log.d(TAG, "document local path : " + result.getAbsolutePath());
            dTask = null;
            boolean started = startContentInNativeReader(content,
                    EncryptionLevel.valueOfKey(content.encLevel), result, context);
            if (started) {
                finish();
                recordStudyHistory();
            }

        }

        public void cancle(boolean mayInterruptIfRunning) {

            if (dTask != null) {
                dTask.cancel(mayInterruptIfRunning);
            }
        }

        @Override
        public void onTaskStart(File result) {

        }
    }

    @Override
    public void onResume() {

        GoogleAnalyticsUtils.sendPageViewDataToGA();
        super.onResume();
    }

    ProgressDialog decrypProgressdialog;

    private void decryptDocumentFile(EncryptionLevel encLevel, File file, Context context,
            String displayFileName) {

        decrypProgressdialog = new ProgressDialog(context);
        decrypProgressdialog.setMessage(getString(R.string.document_decrypting_msg));
        decrypProgressdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        decrypProgressdialog.setCancelable(false);
        decrypProgressdialog.setCanceledOnTouchOutside(false);
        decrypProgressdialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.cancel();
                        finish();
                    }
                });
        decrypProgressdialog.show();
        new ContentDecrypter(context, content, encLevel, file).executeTask(false);

    }

    class ContentDecrypter extends AbstractVedantuAsynTask<Void, Void, Boolean> {

        Context           context;
        LibraryContentRes doc;
        EncryptionLevel   encLevel;
        File              file;
        private File      tempFile;
        private String    rPassword;

        private ContentDecrypter(Context context, LibraryContentRes doc, EncryptionLevel encLevel,
                File file) {


            super();
            this.context = context;
            this.doc = doc;
            this.encLevel = encLevel;
            this.file = file;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                FileManager.addDecryptedPassPhraseToCache(context, doc.passphrase, encLevel,
                        file.getAbsolutePath());
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
            String decryptedPassphrase = FileMaskProcessor.getPassPhrase(file.getAbsolutePath());
            if (decryptedPassphrase == null) {
                Log.e(TAG, "can not display the content");
                return false;
            }

            tempFile = new File(ContentDataManager.getTempContentDir(), file.getName());

            if (tempFile.getAbsolutePath().equals(file.getAbsolutePath())) {
                tempFile = new File(ContentDataManager.getTempContentDir(), "tmp." + file.getName());
                tempFile.deleteOnExit();
            }

            Log.d(TAG, "temp output file is: " + tempFile.getAbsolutePath());
            if (tempFile.exists()) {
                tempFile.delete();
            }

            if (content.type.equalsIgnoreCase(EntityType.DOCUMENT.name())) {
                FileOutputStream fio = null;
                try {

                    rPassword = RandomStringUtils.randomAlphanumeric(5);
                    Log.d(TAG, "generated password:" + rPassword);
                    fio = new FileOutputStream(tempFile);
                    PdfReader pReader = new PdfReader(new VedantuFileInputStream(file,
                            file.length()));
                    PdfStamper pStamper = new PdfStamper(pReader, fio);
                   // pStamper.setEncryption(rPassword.getBytes(), rPassword.getBytes(),

                     // PdfWriter.ALLOW_SCREENREADERS, PdfWriter.STANDARD_ENCRYPTION_128);
                    pStamper.close();
                    pReader.close();
                } catch (Throwable e) {
                    Log.e(TAG, e.getMessage(), e);
                } finally {
                    IOUtils.closeStream(fio);
                }

            } else {
                // if the file is already decrypted then do not decrypt it again
                FileMaskProcessor fileMaskProcessor = new FileMaskProcessor(decryptedPassphrase);
                fileMaskProcessor.process(file, 1024 * 16, tempFile);
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {

            super.onPostExecute(result);
            boolean launched = false;
            if (result) {
                launched = launchFile(tempFile, context, content.name, rPassword);
            }

            recordStudyHistory();
            if (decrypProgressdialog != null) {
                decrypProgressdialog.cancel();
            }
            if (launched) {
                finish();
            }
        }
    }

    private ResolveInfo findTargetAppPackage(Intent intent) {

        PackageManager pm = this.getPackageManager();
        List<ResolveInfo> availableApps = pm.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        if (availableApps != null && availableApps.size() > 0) {
            return availableApps.get(0);
        }
        return null;
    }

}
