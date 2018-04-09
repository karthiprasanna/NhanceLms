package com.nhance.android.enums;

import java.util.HashMap;
import java.util.Map;

public enum FileCategory {
    UNSPECIFIED(null), ORIGINAL("orig"), CONVERTED("conv"), ENCRYPTED("enc");

    private static Map<String, FileCategory> mapAcronym = null;

    private final String acronym;

    private FileCategory(final String acronym) {
        this.acronym = acronym;
    }

    public String getAcronym() {
        return acronym;
    }

    public static FileCategory getByAcronym(String acronym) {
        if (null == mapAcronym) {
            synchronized (FileCategory.class) {
                if (null == mapAcronym) {
                    mapAcronym = new HashMap<String, FileCategory>();
                    for (FileCategory fileCategory : FileCategory.values()) {
                        mapAcronym.put(fileCategory.acronym, fileCategory);
                    }
                }
            }
        }
        return null != acronym && null != mapAcronym ? mapAcronym.get(acronym)
                : UNSPECIFIED;
    }

    public static void main(String[] args) {
        System.out.println("=================================");
        System.out.println(FileCategory.getByAcronym("orig"));
        System.out.println(FileCategory.getByAcronym("conv"));
        System.out.println(FileCategory.getByAcronym(null));
        System.out.println("=================================");
    }
}