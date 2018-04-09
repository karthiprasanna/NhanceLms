package com.nhance.android.enums;

public enum ContextType {
	GLOBAL,LIBRARY,CONTENT; /** LIBRARY and CONTENT are used along with CDP */
	 
	   public static ContextType valueOfKey(String key) {
		ContextType contextType = GLOBAL;
        try {
        	contextType = valueOf(key.trim().toUpperCase());
        } catch (Exception e) {
        }
        return contextType;
    }

}
