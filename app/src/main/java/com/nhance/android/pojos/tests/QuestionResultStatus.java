package com.nhance.android.pojos.tests;

public enum QuestionResultStatus {

	ACTIVE, CANCELED,CANCELLED, BONUS; //REMOVE CANCELED

	public static QuestionResultStatus valueOfKey(String key) {
		QuestionResultStatus status = ACTIVE;
		try {
			status = valueOf(key.trim().toUpperCase());
		} catch (Exception e) {

		}
		return status;
	}
}
