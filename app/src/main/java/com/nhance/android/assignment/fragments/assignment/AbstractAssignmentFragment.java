package com.nhance.android.assignment.fragments.assignment;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.nhance.android.R;
import com.nhance.android.assignment.pojo.TakeAssignmentQuestionWithAnswerGiven;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.ContentDataManager;
import com.nhance.android.db.models.entity.Answer;
import com.nhance.android.db.models.entity.Question;
import com.nhance.android.fragments.AbstractVedantuFragment;
import com.nhance.android.jsinterfaces.SingleQuestionJSInterface;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.utils.JSONUtils;
import com.nhance.android.utils.QuestionImageUtil;
import com.nhance.android.utils.SQLDBUtil;
import com.nhance.android.utils.ViewUtils;

import org.json.JSONObject;

/**
 * Created by Himank Shah on 12/7/2016.
 */

public class AbstractAssignmentFragment extends AbstractVedantuFragment {

    protected ContentDataManager cDataManager;
    protected SingleQuestionJSInterface jsInterface;
    private PopupWindow popup;

    protected View setQuestionViewPopup(final String courseName,
                                        final TakeAssignmentQuestionWithAnswerGiven ansGiven) {

        final View pageBlacker = getLayoutInflater(null).inflate(R.layout.page_blacker, null);
        final PopupWindow pageBlackerPopup = new PopupWindow(pageBlacker,
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, true);

        final View popupQuestionView = getLayoutInflater(null).inflate(
                R.layout.popup_assignment_your_answer_question_view, null);
        popup = new PopupWindow(popupQuestionView, LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT, true);
        popup.setOutsideTouchable(true);
        popup.setBackgroundDrawable(getActivity().getResources().getDrawable(R.color.lightergrey));
        pageBlackerPopup.showAtLocation(pageBlacker, Gravity.CENTER, 0, 0);
        popup.showAtLocation(popupQuestionView, Gravity.CENTER, 0, 0);
        popup.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {

                if (pageBlackerPopup.isShowing()) {
                    pageBlackerPopup.dismiss();
                }
                popup = null;
            }
        });

        final Question question = ansGiven.getQuestion(SessionManager.getInstance(getActivity())
                .getSessionStringValue(ConstantGlobal.USER_ID), cDataManager);

        WebView mWebView = (WebView) popupQuestionView
                .findViewById(R.id.assignment_question_view_container);

        if (question == null || TextUtils.isEmpty(question.name)) {
            mWebView.setVisibility(View.GONE);
            popupQuestionView.findViewById(R.id.popup_close_button);
            TextView qusNoView = (TextView) popupQuestionView.findViewById(R.id.assignment_question_no);
            qusNoView.setVisibility(View.VISIBLE);
            qusNoView.setText(getString(R.string.q) + ansGiven.getQusNo());

            updatePopupQuestiondData(popupQuestionView, courseName, ansGiven, question);

        } else {

            jsInterface = ViewUtils.setUpQuestionWebView(mWebView, new WebViewClient() {

                @Override
                public void onPageFinished(WebView view, String url) {

                    super.onPageFinished(view, url);
                    updatePopupQuestiondData(popupQuestionView, courseName, ansGiven, question);
                }

            });
        }
        popupQuestionView.findViewById(R.id.popup_close_button).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        popup.dismiss();
                    }
                });
        return popupQuestionView;
    }

    protected void updatePopupQuestiondData(View popupQuestionLayout, String courseName,
                                            TakeAssignmentQuestionWithAnswerGiven ansGiven, Question question) {

        ViewUtils.setTextViewValue(popupQuestionLayout, R.id.assignment_question_course_name, courseName);
        TextView yourAnswerText = (TextView) popupQuestionLayout
                .findViewById(R.id.assignment_question_your_answer_text);
        TextView yourAnswerValue = (TextView) popupQuestionLayout
                .findViewById(R.id.assignment_question_your_answer_value);
        yourAnswerValue
                .setText(TextUtils.isEmpty(ansGiven.answerGiven) ? ConstantGlobal.NOT_ANSWERED
                        : ansGiven.answerGiven.replace(SQLDBUtil.SEPARATOR,
                        SQLDBUtil.SEPARATOR_COMMA));
        int color = getResources().getColor(R.color.darkergrey);
        if (ansGiven.correct) {
            color = getResources().getColor(R.color.green);
        } else if (!ansGiven.correct && !TextUtils.isEmpty(ansGiven.answerGiven)) {
            color = getResources().getColor(R.color.darkred);
        }
        yourAnswerText.setTextColor(color);
        yourAnswerValue.setTextColor(color);
        ViewUtils.setTextViewValue(popupQuestionLayout, R.id.assignment_question_correct_answer_value,
                ansGiven.correctAnswer.replace(SQLDBUtil.SEPARATOR, SQLDBUtil.SEPARATOR_COMMA));
        if (question != null && !TextUtils.isEmpty(question.name)) {
            String solution = getSolutionString(question);
            jsInterface.addQuestionData(question, ansGiven.getQusNo(), solution, true);
        }
    }

    protected String getSolutionString(Question question) {

        Answer ans = cDataManager.getAnswer(question.orgKeyId, question.userId, question.id);
        JSONObject jSol = ans == null ? null : ans.solution;
        String solution = jSol == null ? null : JSONUtils.getString(jSol, ConstantGlobal.CONTENT);

        String sol = solution == null ? null : QuestionImageUtil
                .annotateQuestionContentWithHtml(solution);
        return sol;
    }
}
