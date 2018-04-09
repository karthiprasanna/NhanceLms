package com.nhance.android.async.tasks;

import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.nhance.android.async.tasks.socials.WebCommunicatorTask;
import com.nhance.android.async.tasks.socials.WebCommunicatorTask.ReqAction;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.utils.StringUtils;
import com.nhance.android.R;

public class AccessCodeHandler {

    private View                         accessCodeContainer;
    private ITaskProcessor<JSONObject>   taskProcessor;
    private Context                      context;
    private String                       entityId;
    private String                       entityType;
    private OnAccessButtonClickedHandler onAccessButtonClickedHandler;

    public AccessCodeHandler(View accessCodeContainer, ITaskProcessor<JSONObject> taskProcessor,
            Context context, String entityId, String entityType,
            OnAccessButtonClickedHandler onAccessButtonClickedHandler) {

        super();
        this.accessCodeContainer = accessCodeContainer;
        this.taskProcessor = taskProcessor;
        this.context = context;
        this.entityId = entityId;
        this.entityType = entityType;
        this.onAccessButtonClickedHandler = onAccessButtonClickedHandler;
    }

    public void setUpAccessCodeHandler() {

        Button activateButton = (Button) accessCodeContainer
                .findViewById(R.id.access_code_activate_button);
        activateButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (!SessionManager.isOnline()) {
                    Toast.makeText(context, R.string.no_internet_msg, Toast.LENGTH_LONG).show();
                    return;
                }

                EditText email = (EditText) accessCodeContainer
                        .findViewById(R.id.access_code_email);
                if (!StringUtils.isValidEmail(email.getText())) {
                    Toast.makeText(context, R.string.error_invalid_email_id, Toast.LENGTH_LONG)
                            .show();
                    return;
                }

                EditText accessCode = (EditText) accessCodeContainer
                        .findViewById(R.id.access_code_input);
                if (TextUtils.isEmpty(accessCode.getText())) {
                    Toast.makeText(context, R.string.error_access_code_invalid, Toast.LENGTH_LONG)
                            .show();
                    return;
                }

                WebCommunicatorTask task = new WebCommunicatorTask(SessionManager
                        .getInstance(context), null, ReqAction.VERIFY_ACCESS_CODE, taskProcessor);
                task.addExtraParams(ConstantGlobal.CODE, accessCode.getText().toString());
                task.addExtraParams(ConstantGlobal.EMAIL, email.getText().toString());
                task.addExtraParams("entity.id", entityId);
                task.addExtraParams("entity.type", entityType);
                if (onAccessButtonClickedHandler != null) {
                    onAccessButtonClickedHandler.onButtonClicked();
                }
                task.executeTask(false);
            }
        });
    }

    public interface OnAccessButtonClickedHandler {

        public void onButtonClicked();
    }
}
