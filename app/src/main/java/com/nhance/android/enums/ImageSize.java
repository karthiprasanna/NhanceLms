package com.nhance.android.enums;


import java.util.HashMap;
import java.util.Map;

public enum ImageSize {
    EXTRA_SMALL("xsmall"), SMALL("small"), MEDIUM("medium"), LARGE("large"), ORIGINAL("original");

    private static Map<String, ImageSize> mapAcronym = null;

    private final String acronym;

    private ImageSize(final String acronym) {
        this.acronym = acronym;
    }

    public String getAcronym() {
        return acronym;
    }

    public static ImageSize getByAcronym(String acronym) {
        if (null == mapAcronym) {
            synchronized (ImageSize.class) {
                if (null == mapAcronym) {
                    mapAcronym = new HashMap<String, ImageSize>();
                    for (ImageSize imageSize : ImageSize.values()) {
                        mapAcronym.put(imageSize.acronym, imageSize);
                    }
                }
            }
        }
        return null != acronym && null != mapAcronym ? mapAcronym.get(acronym)
                : null;
    }

	
    public static void main(String[] args) {
        System.out.println("=================================");
        System.out.println(ImageSize.getByAcronym("xsmall"));
        System.out.println(ImageSize.getByAcronym("small"));
        System.out.println(ImageSize.getByAcronym(null));
        System.out.println("=================================");
    }

}
