package com.nhance.android.activities.content.players;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.nhance.android.R;
import com.nhance.android.activities.baseclasses.NhanceBaseActivity;
import com.nhance.android.activities.baseclasses.NhanceBaseFragment;
import com.nhance.android.async.tasks.AbstractVedantuAsynTask;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.ContentDataManager;
import com.nhance.android.db.datamanagers.DownloadHistoryManager;
import com.nhance.android.db.datamanagers.ModuleStatusDataManager;
import com.nhance.android.db.models.DownloadHistory;
import com.nhance.android.enums.EncryptionLevel;
import com.nhance.android.enums.EntityType;
import com.nhance.android.managers.FileManager;
import com.nhance.android.managers.ZipManager;
import com.nhance.android.pojos.LibraryContentRes;
import com.nhance.android.pojos.content.infos.DocumentExtendedInfo;
import com.nhance.android.pojos.content.infos.FileExtendedInfo;
import com.nhance.android.pojos.content.infos.HtmlContentExtendedInfo;
import com.nhance.android.pojos.content.infos.IContentInfo;
import com.nhance.android.pojos.content.infos.VideoExtendedInfo;
import com.nhance.android.processors.FileMaskProcessor;
import com.nhance.android.utils.GoogleAnalyticsUtils;
import com.nhance.android.utils.IOUtils;
import com.nhance.android.utils.server.VedantuFileInputStream;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class HTMLContentDisplayActivity extends NhanceBaseActivity {

    private WebView m_cObjWebView;

    private static final String TAG = "HTMLContentDisplayActivity";
    private LibraryContentRes content;
    private ContentDataManager cDataManager;
    private IContentInfo mContentExtendInfo;
    private DownloadHistory dHistory;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        GoogleAnalyticsUtils.setScreenName(ConstantGlobal.GA_SCREEN_HTML_CONTENT);

        GoogleAnalyticsUtils.setCustomDimensions(
                getIntent().getStringExtra(ConstantGlobal.ENTITY_ID),
                getIntent().getStringExtra(ConstantGlobal.NAME), ConstantGlobal.GA_SCREEN_HTML_CONTENT);
        setContentView(R.layout.activity_htmlcontent_display);
        m_cObjWebView = (WebView) findViewById(R.id.html_content_view);

        String linkId = getIntent().getStringExtra(ConstantGlobal.LINK_ID);
        String lName = getIntent().getStringExtra(ConstantGlobal.NAME);
        cDataManager = new ContentDataManager(getBaseContext());
        content = cDataManager.getLibraryContentRes(linkId);
        if (content == null) {
            Toast.makeText(getBaseContext(), "No Content Found", Toast.LENGTH_LONG).show();
            onStop();
            return;
        }
        mContentExtendInfo = content.toContentExtendedInfo();
        dHistory = new DownloadHistoryManager(getApplicationContext())
                .getDownloadHistory(content._id);
        Log.d(TAG, "=== downloadHistory : " + dHistory
                + " == ");

        WebViewClient webViewClient = new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.e("url",url);
                m_cObjWebView.loadUrl(url);
                return true;
            }
        };
        m_cObjWebView.setWebViewClient(webViewClient);
        m_cObjWebView.setWebChromeClient(new WebChromeClient());
        m_cObjWebView.clearCache(true);
        m_cObjWebView.getSettings().setUseWideViewPort(true);
        m_cObjWebView.setInitialScale(1);
     //   m_cObjWebView.getSettings().setBuiltInZoomControls(true);
        m_cObjWebView.clearHistory();
        m_cObjWebView.getSettings().setAllowFileAccess(true);
        m_cObjWebView.getSettings().setDomStorageEnabled(true);
        m_cObjWebView.getSettings().setJavaScriptEnabled(true);
        m_cObjWebView.requestFocusFromTouch();
        m_cObjWebView.getSettings().setLoadWithOverviewMode(true);
        m_cObjWebView.getSettings().setPluginState(WebSettings.PluginState.ON);
      //  m_cObjWebView.getSettings().setUserAgentString("Mozilla/5.0 (Linux; U;Android 2.0; en-us; Droid Build/ESD20) AppleWebKit/530.17 (KHTML, likeGecko) Version/4.0 Mobile Safari/530.17");

        if (!content.downloaded || dHistory == null) {
            // String lUrl = getIntent().getStringExtra(ConstantGlobal.MEDIA_URL);

            //m_cObjWebView.loadUrl(lUrl);

            String url = null;
            if (mContentExtendInfo instanceof FileExtendedInfo) {
                url = ((FileExtendedInfo) mContentExtendInfo).url;
            } else if (mContentExtendInfo instanceof HtmlContentExtendedInfo) {
                url = ((HtmlContentExtendedInfo) mContentExtendInfo).url;
            }

            Log.e("url",url);
            m_cObjWebView.loadUrl( url);
        } else {
            byte[] lKey = content.passphrase.getBytes();
            File file = new File(dHistory.file);
//            file = new File(file.getParent(), lName);
//            ZipManager.unzip(dHistory.file, file.getAbsolutePath());
            EncryptionLevel encLevel = EncryptionLevel.valueOfKey(content.encLevel);
            decryptDocumentFile(encLevel, file, this, content.name);
        }
        if (StringUtils.isNotEmpty(getIntent().getStringExtra(ConstantGlobal.MODULE_ID))) {
            String moduleId = getIntent().getStringExtra(ConstantGlobal.MODULE_ID);
            String entityId = getIntent().getStringExtra(ConstantGlobal.ENTITY_ID);
            try {
                (new ModuleStatusDataManager(HTMLContentDisplayActivity.this)).updateModuleEntryStatus(
                        session.getSessionStringValue(ConstantGlobal.USER_ID),
                        session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID), moduleId, entityId,
                        "HTMLCONTENT");
            } catch (Exception e) {
                Log.d(TAG, "Some error occured in updating id: " + entityId + " Error: "
                        + e.getMessage());
            }
        }
    }

/* @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

    }*/
    private void decryptDocumentFile(EncryptionLevel encLevel, File file, Context context,
                                     String displayFileName) {

        /*decrypProgressdialog = new ProgressDialog(context);
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
        decrypProgressdialog.show();*/
        new ContentDecrypter(context, content, encLevel, file).executeTask(false);

    }

    class ContentDecrypter extends AbstractVedantuAsynTask<Void, Void, Boolean> {

        Context           context;
        LibraryContentRes doc;
        EncryptionLevel   encLevel;
        File              file;
        private File      tempFile;
        private String    rPassword;
        private File decryptFile;
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
                FileManager.addDecryptedPassPhraseToCache(context, doc.passphrase, encLevel, file.getAbsolutePath());
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
                    PdfReader pReader = new PdfReader(new VedantuFileInputStream(file, file.length()));
                    PdfStamper pStamper = new PdfStamper(pReader, fio);
                    pStamper.setEncryption(rPassword.getBytes(), rPassword.getBytes(),
                            PdfWriter.ALLOW_SCREENREADERS, PdfWriter.STANDARD_ENCRYPTION_128);
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

                File file = new File(dHistory.file);
                decryptFile = new File(file.getParent(), content.name);
                decryptFile.deleteOnExit();
                ZipManager.unzip(tempFile.getAbsolutePath(), decryptFile.getAbsolutePath(), decryptedPassphrase);
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if(decryptFile != null) {
                Log.e("url", "file://" + decryptFile.getAbsolutePath() + "/index.html");
                m_cObjWebView.loadUrl("file://" + decryptFile.getAbsolutePath() + "/index.html");
            }else{
                Toast.makeText(HTMLContentDisplayActivity.this, "Cannot display scorm content", Toast.LENGTH_SHORT).show();
            }
        }
    }
}