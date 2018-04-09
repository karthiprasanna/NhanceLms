package com.nhance.android.enums;

import android.content.Context;
import android.util.Log;

import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.OrgDataManager;
import com.nhance.android.db.datamanagers.UserDataManager;
import com.nhance.android.db.models.entity.User;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.utils.VedantuEncrypter;

public enum EncryptionLevel {

    NA, P, P_O, P_O_U;

    private static final String TAG = "EncryptionLevel";

    public static EncryptionLevel valueOfKey(String level) {

        EncryptionLevel encLevel = NA;
        try {
            encLevel = valueOf(level.trim().toUpperCase());
        } catch (Exception e) {}
        return encLevel;
    }

    public static enum EncryptionLevelOrder {
        NA {

            @Override
            public String getDecryptedPassPhrase(String passphrase, Context context) {

                return null;
            }
        },
        P {

            @Override
            public String getDecryptedPassPhrase(String passphrase, Context context) {

                return passphrase;
            }
        },
        O {

            @Override
            public String getDecryptedPassPhrase(String passphrase, Context context) {

                // using organization publicKey
                byte[] publicKey = new OrgDataManager(context).getOrgPublicKey(
                        SessionManager.getInstance(context).getSessionStringValue(
                                ConstantGlobal.ORG_ID),
                        SessionManager.getInstance(context).getSessionStringValue(
                                ConstantGlobal.CMDS_URL));
                if (publicKey == null) {
                    Log.e(TAG, "key for organization not found");
                    return null;
                }
                return VedantuEncrypter.INSTANCE.decryptWithPublicKey(passphrase, publicKey);
            }
        },
        U {

            @Override
            public String getDecryptedPassPhrase(String passphrase, Context context) {

                User user = new UserDataManager(context).getUser(
                        SessionManager.getInstance(context).getSessionIntValue(
                                ConstantGlobal.ORG_KEY_ID),
                        SessionManager.getInstance(context).getSessionStringValue(
                                ConstantGlobal.USER_ID));
                if (user == null) {
                    return null;
                }
                byte[] userPrivateKey = user.key;

                return VedantuEncrypter.INSTANCE.decryptWithPrivateKey(passphrase, userPrivateKey);
            }
        };

        // P==passPhrase, O=organization, U=user level
        public static EncryptionLevelOrder valueOfKey(String level) {

            EncryptionLevelOrder encOrder = NA;
            try {
                encOrder = valueOf(level.trim().toUpperCase());
            } catch (Exception e) {}
            return encOrder;
        }

        public abstract String getDecryptedPassPhrase(String passphrase, Context context);
    }
}
