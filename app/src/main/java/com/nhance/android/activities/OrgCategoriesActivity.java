package com.nhance.android.activities;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.nhance.android.activities.baseclasses.NhanceBaseActivity;
import com.nhance.android.async.tasks.ITaskProcessor;
import com.nhance.android.async.tasks.socials.WebCommunicatorTask;
import com.nhance.android.async.tasks.socials.WebCommunicatorTask.ReqAction;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.customviews.CustomListView;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.utils.JSONUtils;
import com.nhance.android.utils.VedantuWebUtils;
import com.nhance.android.R;

public class OrgCategoriesActivity extends NhanceBaseActivity {

    private static final String TAG        = "OrgCategoriesActivity";
    private List<String>        categories = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_org_categories);
        loadCategories();
        getSupportActionBar().hide();
    }

    WebCommunicatorTask orgCategoriesReq;

    private void loadCategories() {

        final ProgressDialog dialog = new ProgressDialog(OrgCategoriesActivity.this);
        dialog.setMessage("Fetching Categories info...");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.cancel();

                    }
                });
        dialog.show();
        orgCategoriesReq = new WebCommunicatorTask(
                SessionManager.getInstance(getApplicationContext()), null,
                ReqAction.GET_ORG_CATEGORIES, new ITaskProcessor<JSONObject>() {

                    @Override
                    public void onTaskStart(JSONObject result) {

                    }

                    @Override
                    public void onTaskPostExecute(boolean success, JSONObject result) {

                        dialog.cancel();
                        if (!success) {
                            Toast.makeText(OrgCategoriesActivity.this,
                                    getResources().getString(R.string.error_general),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        final JSONArray categoriesList = JSONUtils.getJSONArray(
                                JSONUtils.getJSONObject(result, VedantuWebUtils.KEY_RESULT),
                                VedantuWebUtils.KEY_LIST);
                        Log.d(TAG, "Categories JSONArray are " + categoriesList.toString());
                        if (categoriesList.length() > 0) {
                            CustomListView categoriesListView = (CustomListView) findViewById(R.id.org_categories_list_view);
                            for (int k = 0; k < categoriesList.length(); k++) {
                                try {
                                    JSONObject category = categoriesList.getJSONObject(k);
                                    categories.add(JSONUtils.getString(category,
                                            ConstantGlobal.NAME));
                                } catch (JSONException e) {
                                    Log.d(TAG, "Some error in adding category" + e.getMessage());
                                }
                            }
                            Log.d(TAG, "Categories are " + categories.toString());
                            categoriesListView.setAdapter(new ArrayAdapter<String>(
                                    getBaseContext(), R.layout.list_item_view_org_category,
                                    categories));
                            categoriesListView
                                    .setOnItemClickListener(new AdapterView.OnItemClickListener() {

                                        @Override
                                        public void onItemClick(AdapterView<?> parent, View view,
                                                int position, long id) {

                                            Intent intent = new Intent(OrgCategoriesActivity.this,
                                                    MyProgramsActivity.class);
                                            try {
                                                JSONObject catObj = categoriesList
                                                        .getJSONObject(position);
                                                intent.putExtra(ConstantGlobal.CATEGORY_ID,
                                                        JSONUtils.getString(catObj,
                                                                ConstantGlobal.ID));
                                            } catch (JSONException e) {
                                                Log.e(TAG, e.getMessage(), e);
                                            }
                                            intent.putExtra("fromSignUpRoute", true);
                                            startActivity(intent);
                                            finish();
                                        }
                                    });
                        } else {
                            findViewById(R.id.no_categories_found).setVisibility(View.VISIBLE);
                        }
                    }
                }, null, null, 0, 50);
        // TODO load more for categories
        orgCategoriesReq.executeTask(false);
    }

    @Override
    protected void onStop() {

        super.onStop();
        if (orgCategoriesReq != null) {
            orgCategoriesReq.cancel(true);
        }
    }

}
