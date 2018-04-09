package com.nhance.android.enums;

import android.content.pm.ActivityInfo;

public enum ContentType {
    PDF, PPT {
        @Override
        public int getActivityOrientation() {
            return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        }
    },
    BOOK {
        @Override
        public String getDisplayName() {
            return "Book";
        }
    },
    ANIMATION {
        @Override
        public String getDisplayName() {
            return "Animation";
        }
    },
    VIDEO {
        @Override
        public String getDisplayName() {
            return "Video";
        }
    },
    TEST { // test is added here, as it required to show Test in programme
           // metadata..
        @Override
        public String getDisplayName() {
            return "Test";
        }
    },
    ASSIGNMENT {
        @Override
        public String getDisplayName() {
            return "Assignment";
        }
    };

    public static ContentType valueOfKey(String key) {
        ContentType contentType = BOOK;
        try {
            contentType = valueOf(key.trim().toUpperCase());
        } catch (Exception e) {
        }
        return contentType;
    }

    public int getActivityOrientation() {
        return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    }

    public String getDisplayName() {
        return this.name();
    }

}
