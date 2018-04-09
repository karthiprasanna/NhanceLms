package com.nhance.android.enums;

import android.annotation.SuppressLint;

public enum ModuleRun {
	SEQUENTIAL("Sequential"), NON_SEQUENTIAL("Non Sequential");

	@SuppressLint("DefaultLocale")
	public static ModuleRun valueOfKey(String key) {

		ModuleRun moduleRun = NON_SEQUENTIAL;
		try {
			moduleRun = valueOf(key.trim().toUpperCase());
		} catch (Exception e) {
		}
		return moduleRun;
	}

	public String displayName;

	private ModuleRun(String displayName) {
		this.displayName = displayName;
	}
}
