package com.nhance.android.utils;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

public class FontUtils {
    public static enum FontTypes {

        ROBOTO_LIGHT {
            @Override
            public String getTTFFileName() {
                return "Roboto-Light.ttf";
            }
        },
        ROBOTO_THIN {
            @Override
            public String getTTFFileName() {

                return "Roboto-Thin.ttf";
            }
        },
        ROBOTO_CONDENSED {
            @Override
            public String getTTFFileName() {

                return "Roboto-Condensed.ttf";
            }
        };

        public abstract String getTTFFileName();
    }

    /**
     * 
     * map of font types to font paths in assets
     */

    private static Map<FontTypes, Typeface> typefaceCache = new HashMap<FontUtils.FontTypes, Typeface>();

    /**
     * 
     * Creates Roboto typeface and puts it into cache
     * 
     * @param context
     * 
     * @param fontType
     * 
     * @return
     */

    public static Typeface getRobotoTypeface(Context context, FontTypes fontType) {

        if (fontType == null) {
            return Typeface.DEFAULT;
        }
        Typeface typeface = typefaceCache.get(fontType);

        if (typeface == null) {
            typeface = Typeface.createFromAsset(context.getAssets(),
                    "fonts/" + fontType.getTTFFileName());
            typefaceCache.put(fontType, typeface);
        }
        return typeface;
    }

    public static void setTypeface(View view, FontTypes fontTypes) {
        if (view != null && view instanceof TextView) {
            Typeface typeface = getRobotoTypeface(view.getContext(), fontTypes);
            ((TextView) view).setTypeface(typeface);
        }
    }

}
