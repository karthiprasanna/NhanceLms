package com.nhance.android.enums;

import org.apache.commons.lang.StringUtils;

import com.nhance.android.R;

public enum EntityType {

    DOCUMENT("doc", R.drawable.icon_book, R.drawable.icon_book) {

        @Override
        public String getDisplayName() {

            return "E-Books";
        }
    },
    TEST("test", R.drawable.icon_test, R.drawable.icon_test) {

        @Override
        public String getDisplayName() {

            return "Assessments";
        }

    },
    ASSIGNMENT("assignment", R.drawable.icon_assignment, R.drawable.icon_assignment) {

        @Override
        public String getDisplayName() {

            return "Assignment";
        }

    },
    VIDEO("vid", R.drawable.icon_video, R.drawable.icon_video) {

        @Override
        public String getDisplayName() {

            return "Videos";
        }

    },

    HTMLCONTENT("htmlc", R.drawable.scorm_icon, R.drawable.scorm_icon) {

        @Override
        public String getDisplayName() {

            return "E-Contents";
        }
    },
    FILE("file", R.drawable.icon_file, R.drawable.icon_file) {

        @Override
        public String getDisplayName() {

            return "Resources";
        }
    },
    MODULE("module", R.drawable.icon_slpcontent_leftnav, R.drawable.icon_slpcontent_leftnav) {

        @Override
        public String getDisplayName() {

            return "Learning Path";
        }
    },
    COMPOUNDMEDIA("cmedia", R.drawable.icon_compound_media, R.drawable.icon_compound_media) {

        @Override
        public String getDisplayName() {

            return "Compound Media";
        }
    },
    QUESTION_SHEET(
    /*
     * R.id.library_search_qsheet_stats_container, R.id.library_search_count_text_view,
     * R.id.library_search_qsheet_view, R.id.library_search_qsheet_show_more
     */
    ) {

        @Override
        public String getDisplayName() {

            return "Question Sheets";
        }
    },
    QUESTION("q", R.drawable.icon_question, R.drawable.icon_question) {

        @Override
        public String getDisplayName() {

            return "Question";
        }

    },
    ORGANIZATION("org") {

        @Override
        public String getDisplayName() {

            return null;
        }

    },
    SECTION("sec") {

        @Override
        public String getDisplayName() {

            return null;
        }

    },
    SDCARD("sdcard") {

        @Override
        public String getDisplayName() {

            return null;
        }
    },
    SDCARDGROUP("sdgroup") {

        @Override
        public String getDisplayName() {

            return null;
        }
    },
    UNKNOWN("unknown") {

        @Override
        public String getDisplayName() {

            return null;
        }

    },
    ALL("all", 0, R.drawable.icon_all) {

        @Override
        public String getDisplayName() {

            return "All";
        }
    };

    public int    icon_res_id;
    public int    filter_icon_res_id;

    // no longer used
    public int    library_search_entity_stats_container;
    public int    library_search_count_text_view;
    public int    library_search_entity_view;
    public int    library_search_entity_show_more;

    public String acronym;

    private EntityType() {

    }

    private EntityType(String acronym) {

        this.acronym = acronym;
    }

    private EntityType(String acronym, int icon_res_id, int filter_icon_res_id) {

        this.acronym = acronym;
        this.icon_res_id = icon_res_id;
        this.filter_icon_res_id = filter_icon_res_id;
    }

    public static EntityType getEntityByDisplayName(String displayName) {

        EntityType entityType = null;
        for (EntityType e : values()) {
            if (e.getDisplayName() != null
                    && e.getDisplayName().equalsIgnoreCase(displayName.trim())) {
                entityType = e;
                break;
            }
        }
        return entityType;
    }

    public abstract String getDisplayName();

    public static EntityType valueOfKey(String value) {

        EntityType type = UNKNOWN;
        try {
            type = valueOf(StringUtils.upperCase(value));
        } catch (Exception e) {}
        return type;
    }
}
