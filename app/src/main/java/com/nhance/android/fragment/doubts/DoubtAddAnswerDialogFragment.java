package com.nhance.android.fragment.doubts;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.nhance.android.jsinterfaces.DoubtJSInterface;
import com.nhance.android.web.VedantuWebChromeClient;
import com.nhance.android.R;

@SuppressLint("SetJavaScriptEnabled")
public class DoubtAddAnswerDialogFragment extends DialogFragment {

    private View                       popupLayout;
    private IDoubtAddAnswerJSInterface activityInstance;

    @Override
    public void onAttach(Activity activity) {

        if (getActivity() instanceof IDoubtAddAnswerJSInterface) {
            activityInstance = (IDoubtAddAnswerJSInterface) getActivity();
        }
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //TODO set width
        setStyle(STYLE_NO_TITLE, 0);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View
            onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        popupLayout = inflater.inflate(R.layout.doubt_add_answer, container, false);
        return popupLayout;
    }

    public interface IDoubtAddAnswerJSInterface {

        public DoubtJSInterface getJSInterface();
    }

    private DoubtJSInterface popupJsInterface;

    @Override
    public void onActivityCreated(Bundle arg0) {

        super.onActivityCreated(arg0);

        WebView addAnswersWebView = (WebView) popupLayout.findViewById(R.id.add_doubt_answer_input);
        addAnswersWebView.getSettings().setJavaScriptEnabled(true);
        addAnswersWebView.setWebChromeClient(new VedantuWebChromeClient());
        addAnswersWebView.loadUrl("file:///android_asset/html/doubt_add_answer.html");
        if (activityInstance != null) {
            popupJsInterface = activityInstance.getJSInterface();
            addAnswersWebView.addJavascriptInterface(popupJsInterface, "doubtJSInterface");
        } else {
            Toast.makeText(getActivity(), "Some error occured", Toast.LENGTH_SHORT).show();
            dismiss();
        }
        final View loader = popupLayout.findViewById(R.id.add_answer_loader);
        addAnswersWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {

                super.onPageFinished(view, url);
                popupJsInterface.loadDimensionFiles();
                loader.setVisibility(View.GONE);
            }
        });
    }
}
