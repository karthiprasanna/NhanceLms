package com.nhance.android.enums;

import org.apache.commons.lang.StringUtils;

public enum ModuleEntryCompletionRuleType {
	NONE, VIEW;
	public static ModuleEntryCompletionRuleType valueOfKey(String key) {

		ModuleEntryCompletionRuleType moduleEntryCompletionRuleType = NONE;
		try {
			moduleEntryCompletionRuleType = valueOf(StringUtils.upperCase(key
					.trim()));
		} catch (Exception e) {
		}
		return moduleEntryCompletionRuleType;
	}
}
