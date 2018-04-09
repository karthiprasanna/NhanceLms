package com.nhance.android.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nhance.android.activities.about.AboutActivity;
import com.nhance.android.activities.baseclasses.NhanceBaseFragment;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.jsinterfaces.AbstractJSInterface;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.web.VedantuWebChromeClient;
import com.nhance.android.R;

@SuppressLint({ "SetJavaScriptEnabled", "JavascriptInterface" })
public class AboutPagesFragment extends NhanceBaseFragment {

    private int                 currentPos;
    private View                fragmentRootView;
    private static final String TAG = "AboutPagesFragment";

    public static AboutPagesFragment newInstance(int position) {

        AboutPagesFragment f = new AboutPagesFragment();
        Bundle args = new Bundle();
        args.putInt(ConstantGlobal.POSITION, position);
        f.setArguments(args);

        return f;
    }

    private IAboutPage iAboutPageInstance;

    public interface IAboutPage {
        public void updateFragment(int position);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof IAboutPage) {
            iAboutPageInstance = (IAboutPage) activity;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ((AboutActivity) getActivity()).getSupportActionBar().hide();
        if (getArguments() != null) {
            currentPos = getArguments().getInt(ConstantGlobal.POSITION);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int aboutPageResId = -1;
        int webViewResId = -1;
        String htmlFile = "";
        switch (currentPos) {
        case 0:
            aboutPageResId = R.layout.fragment_about_main;
            break;
        case 1:
            aboutPageResId = R.layout.fragment_terms_and_conditions;
            webViewResId = R.id.webview_terms;
            htmlFile = "terms_and_conditions.html";
            break;
        case 2:
            aboutPageResId = R.layout.fragment_privacy_policy;
            webViewResId = R.id.webview_privacy;
            htmlFile = "privacy_policy.html";
            break;
        case 3:
            aboutPageResId = R.layout.fragment_credits;
            break;
        default:
            aboutPageResId = R.layout.fragment_about_main;
            break;
        }
        fragmentRootView = inflater.inflate(aboutPageResId, container, false);
        if (webViewResId != -1 && htmlFile.length() > 0) {
            WebView webView = (WebView) fragmentRootView.findViewById(webViewResId);
            webView.getSettings().setJavaScriptEnabled(true);

            final AboutPagesJSInterface jsInterface = new AboutPagesJSInterface(webView);

            webView.addJavascriptInterface(jsInterface, "doubtJSInterface");
            webView.setWebChromeClient(new VedantuWebChromeClient());
            webView.loadUrl("file:///android_asset/html/" + htmlFile);
            webView.setWebViewClient(new WebViewClient() {

                @Override
                public void onPageFinished(WebView view, String url) {

                    super.onPageFinished(view, url);
                    jsInterface.loadStyles();
                }
            });
        }
        return fragmentRootView;
    }

    public class AboutPagesJSInterface extends AbstractJSInterface {

        private WebView   webView;
        protected Handler mHandler = new Handler();

        private AboutPagesJSInterface(WebView webView) {

            super();
            this.webView = webView;
        }

        @SuppressLint("SetJavaScriptEnabled")
        public void loadStyles() {

            mHandler.post(new Runnable() {

                @Override
                public void run() {

                    int dimension = (int) getResources().getDimension(
                            R.dimen.device_dimension_value);
                    webView.loadUrl("javascript:loadStyles(" + dimension + ")");
                }
            });
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (iAboutPageInstance != null && currentPos == 0) {
            SessionManager sessionManager = SessionManager.getInstance(m_cObjNhanceBaseActivity
                    .getApplicationContext());
            ((TextView) fragmentRootView.findViewById(R.id.about_institute_name))
                    .setText(sessionManager.getSessionStringValue(ConstantGlobal.ORG_NAME));
            try {
                PackageInfo pInfo = m_cObjNhanceBaseActivity.getPackageManager().getPackageInfo(
                        m_cObjNhanceBaseActivity.getPackageName(), 0);

                ((TextView) fragmentRootView.findViewById(R.id.about_app_version)).setText(pInfo.versionName);
            } catch (NameNotFoundException e) {
                Log.d(TAG, "Error in fetching in app version");
            }

            OnClickListener aboutItemClickListner = new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    int targetIndex = 0;
                    if (fragmentRootView == null) {
                        return;
                    }
                    if (v == fragmentRootView.findViewById(R.id.take_to_terms)) {
                        targetIndex = 1;
                    } else if (v == fragmentRootView.findViewById(R.id.take_to_privacy)) {
                        targetIndex = 2;
                    } else if (v == fragmentRootView.findViewById(R.id.take_to_credits)) {
                        targetIndex = 3;
                    }
                    iAboutPageInstance.updateFragment(targetIndex);
                }
            };
            fragmentRootView.findViewById(R.id.take_to_terms).setOnClickListener(
                    aboutItemClickListner);
            fragmentRootView.findViewById(R.id.take_to_privacy).setOnClickListener(
                    aboutItemClickListner);
            fragmentRootView.findViewById(R.id.take_to_credits).setOnClickListener(
                    aboutItemClickListner);
        } else if (currentPos == 3) {
            String[] libraries = m_cObjNhanceBaseActivity.getResources().getStringArray(
                    R.array.third_party_library_credits);
            LayoutInflater inflater = (LayoutInflater) m_cObjNhanceBaseActivity.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout holder = (LinearLayout) fragmentRootView
                    .findViewById(R.id.third_party_library_credits_holder);
            for (int i = 0; i < libraries.length; i++) {
                TextView view = (TextView) inflater.inflate(
                        R.layout.list_item_view_third_party_library, holder, false);
                view.setText(Html.fromHtml(libraries[i]));
                view.setMovementMethod(LinkMovementMethod.getInstance());
                holder.addView(view);
            }
        }
    }

}
