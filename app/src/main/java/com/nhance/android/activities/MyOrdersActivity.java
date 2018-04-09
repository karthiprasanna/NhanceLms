package com.nhance.android.activities;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nhance.android.activities.baseclasses.NhanceBaseActivity;
import com.nhance.android.async.tasks.ITaskProcessor;
import com.nhance.android.async.tasks.socials.WebCommunicatorTask;
import com.nhance.android.async.tasks.socials.WebCommunicatorTask.ReqAction;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.pojos.responses.MyOrdersRes;
import com.nhance.android.pojos.responses.MyOrdersRes.MyOrder;
import com.nhance.android.utils.GoogleAnalyticsUtils;
import com.nhance.android.utils.JSONUtils;
import com.nhance.android.utils.VedantuWebUtils;
import com.nhance.android.R;
public class MyOrdersActivity extends NhanceBaseActivity {

    private ListView        myOrdersListView;
    private MyOrdersAdapter myOrdersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        GoogleAnalyticsUtils.setScreenName(ConstantGlobal.GA_SCREEN_MY_ORDERS);
        setContentView(R.layout.activity_my_orders);
        myOrdersListView = (ListView) findViewById(R.id.my_orders_list_view);
        // TODO scorll listner
        populateMyOrders();
    }

    private WebCommunicatorTask myOrdersReq;
    private ProgressDialog              myOrdersProcessDialog;

    public void populateMyOrders() {

        myOrdersProcessDialog = ProgressDialog.show(this,
                getResources().getString(R.string.loading_orders),
                getResources().getString(R.string.wait_while_loading_orders));
        // moduleContentProcessDialog.setCancelable(true);
        myOrdersProcessDialog.setCanceledOnTouchOutside(false);
        myOrdersReq = new WebCommunicatorTask(
                SessionManager.getInstance(getApplicationContext()), null,
                ReqAction.GET_BUY_ORDERS, new ITaskProcessor<JSONObject>() {

                    @Override
                    public void onTaskStart(JSONObject result) {

                    }

                    @Override
                    public void onTaskPostExecute(boolean success, JSONObject response) {

                        if (!success) {
                            Toast.makeText(MyOrdersActivity.this,
                                    getResources().getString(R.string.error_general),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        MyOrdersRes myOrdersRes = new MyOrdersRes();
                        JSONObject result = JSONUtils.getJSONObject(response,
                                VedantuWebUtils.KEY_RESULT);
                        if (result != null) {
                            myOrdersRes.fromJSON(result);
                        }
                        myOrdersProcessDialog.dismiss();
                        if (myOrdersAdapter == null) {
                            myOrdersAdapter = new MyOrdersAdapter(MyOrdersActivity.this,
                                    R.layout.list_item_view_my_order, myOrdersRes.myOrders);
                            myOrdersListView.setAdapter(myOrdersAdapter);
                        } else {
                            myOrdersAdapter.notifyDataSetChanged();
                        }
                    }
                }, null, null, 0);
        // TODO set req params
        myOrdersReq.executeTask(false);
    }

    class MyOrdersAdapter extends ArrayAdapter<MyOrder> {

        private List<MyOrder>   items;
        private int             layoutResource;
        private OnClickListener myOrderClickListner;

        public MyOrdersAdapter(Context context, int layoutResource, List<MyOrder> items) {

            super(context, layoutResource, items);
            this.items = items;
            this.layoutResource = layoutResource;
            this.myOrderClickListner = new View.OnClickListener() {

                @Override
                public void onClick(View view) {

                    View table = view.findViewById(R.id.my_order_details_table);
                    if (table.getVisibility() == View.GONE) {
                        table.setVisibility(View.VISIBLE);
                    } else {
                        table.setVisibility(View.GONE);
                    }
                }
            };
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {

            View view = convertView;
            if (view == null) {
                layoutResource = R.layout.list_item_view_my_order;
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(layoutResource, parent, false);
            }
            MyOrder myOrder = items.get(position);
            ((TextView) view.findViewById(R.id.my_order_unit_name)).setText(StringUtils
                    .upperCase(myOrder.name));
            ((TextView) view.findViewById(R.id.my_order_id)).setText(myOrder.orderId);

            String amtPaid = myOrder.currencyCode + " " + myOrder.totalAmt;
            ((TextView) view.findViewById(R.id.my_order_amt_paid)).setText(amtPaid);

            ((TextView) view.findViewById(R.id.my_order_state)).setText(StringUtils
                    .capitalize(myOrder.orderState.name()));

            String timeAdded = DateUtils.formatDateTime(MyOrdersActivity.this, myOrder.orderTime,
                    DateUtils.FORMAT_SHOW_DATE);
            ((TextView) view.findViewById(R.id.my_order_date)).setText(timeAdded);

            // TODO Link

            view.setOnClickListener(myOrderClickListner);
            return view;
        }
    }

    @Override
    protected void onStop() {

        super.onStop();
        if (myOrdersReq != null) {
            myOrdersReq.cancel(true);
        }
    }

    @Override
    protected void onResume() {

        GoogleAnalyticsUtils.sendPageViewDataToGA();
        super.onResume();
    }
}
