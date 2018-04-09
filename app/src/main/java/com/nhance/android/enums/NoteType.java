package com.nhance.android.enums;

import android.content.Context;

public enum NoteType {
    IMPORTANT {

        @Override
        public int getDrawableLeft() {

            return 0/* R.drawable.important */;
        }

        @Override
        public String getDisplayName(Context context) {

            return ""/* context.getResources().getString(R.string.important) */;
        }
    },
    DOUBT {

        @Override
        public int getDrawableLeft() {

            return 0/* R.drawable.doubt */;
        }

        @Override
        public String getDisplayName(Context context) {

            return ""/* context.getResources().getString(R.string.doubt) */;
        }
    },
    REVISION {

        @Override
        public int getDrawableLeft() {

            return 0/* R.drawable.for_revision_white */;
        }

        @Override
        public String getDisplayName(Context context) {

            return ""/* context.getResources().getString(R.string.revision) */;
        }
    }
    /*
     * enable note type personal, PERSONAL {
     * 
     * @Override public int getDrawableLeft() { return R.drawable.personal; }
     * 
     * @Override public String getDisplayName(Context context) { return
     * context.getResources().getString(R.string.personal_note); } }
     */;

    public abstract int getDrawableLeft();

    public abstract String getDisplayName(Context context);

    public static NoteType valueOfKey(String key) {

        NoteType noteType = IMPORTANT;
        try {
            noteType = valueOf(key.trim().toUpperCase());
        } catch (Exception e) {}
        return noteType;
    }
}
