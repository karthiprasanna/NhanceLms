package com.nhance.android.pojos.responses;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.enums.OrderState;
import com.nhance.android.utils.JSONAware;
import com.nhance.android.utils.JSONUtils;
import com.nhance.android.utils.VedantuWebUtils;

public class MyOrdersRes implements JSONAware {

    private static final long   serialVersionUID = 1L;
    private static final String TAG              = "MyOrdersRes";

    public int                  totalHits;
    public List<MyOrder>        myOrders;

    @Override
    public void fromJSON(JSONObject result) {

        totalHits = JSONUtils.getInt(result, VedantuWebUtils.KEY_TOTAL_HITS, 0);
        JSONArray ordersArray = JSONUtils.getJSONArray(result, VedantuWebUtils.KEY_LIST);

        if (ordersArray.length() > 0) {
            for (int k = 0, l = ordersArray.length(); k < l; k++) {
                try {
                    JSONObject order = ordersArray.getJSONObject(k);
                    MyOrder myOrder = new MyOrder();
                    myOrder.name = order.getJSONArray("items").getJSONObject(0)
                            .getString(ConstantGlobal.NAME);
                    myOrder.orderId = order.getString(ConstantGlobal.ORDER_ID);
                    myOrder.orderState = OrderState.valueOf(order
                            .getString(ConstantGlobal.ORDER_STATE));
                    myOrder.consumed = order.getBoolean(ConstantGlobal.CONSUMED);
                    myOrder.orderTime = order.getLong(ConstantGlobal.ORDER_TIME);
                    JSONObject invoiceInfo = order.getJSONObject("invoiceInfo");
                    myOrder.totalAmt = invoiceInfo.getInt(ConstantGlobal.TOTAL);
                    myOrder.currencyCode = invoiceInfo.getString(ConstantGlobal.CURRENCY_CODE);
                    myOrders.add(myOrder);
                } catch (Exception e) {
                    Log.d(TAG, "Error in parsing from my orders response");
                }
            }
        } else {
            myOrders = new ArrayList<MyOrdersRes.MyOrder>();
        }
    }

    @Override
    public JSONObject toJSON() {

        return null;
    }

    public class MyOrder {

        public String     name;
        public String     orderId;
        public OrderState orderState;
        public boolean    consumed;
        public long       orderTime;
        public int        totalAmt;
        public String     currencyCode;

    }
}
