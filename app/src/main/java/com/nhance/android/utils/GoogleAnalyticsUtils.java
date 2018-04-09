package com.nhance.android.utils;

import org.apache.commons.lang.StringUtils;

import android.util.Log;

import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.managers.VApp;

public class GoogleAnalyticsUtils {

    public static void sendPageViewDataToGA() {

        Log.d("Google analytics", "Sending page view");
        VApp.getGaTracker().send(MapBuilder.createAppView().build());
    }

    public static void setScreenName(String screenName) {

        VApp.getGaTracker().set(Fields.SCREEN_NAME, screenName);
    }

    public static void setCustomDimensions(String entityId, String entityName, String entityType) {

        Tracker tracker = VApp.getGaTracker();

        if (StringUtils.isNotEmpty(entityId)) {
            tracker.set(Fields.customDimension(ConstantGlobal.GA_CUSTOM_DIMENSION_ENTITY_ID_INDEX),
                    entityId);
        }
        if (StringUtils.isNotEmpty(entityName)) {
            tracker.set(
                    Fields.customDimension(ConstantGlobal.GA_CUSTOM_DIMENSION_ENTITY_NAME_INDEX),
                    entityName);
        }
        if (StringUtils.isNotEmpty(entityType)) {
            tracker.set(
                    Fields.customDimension(ConstantGlobal.GA_CUSTOM_DIMENSION_ENTITY_TYPE_INDEX),
                    entityType);
        }
    }
}
