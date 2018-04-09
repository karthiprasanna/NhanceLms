package com.nhance.android.db.datamanagers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.models.Organization;
import com.nhance.android.db.models.PendingTranscation;
import com.nhance.android.pojos.OrgMemberInfo;
import com.nhance.android.pojos.ProgCenterSecInfo;
import com.nhance.android.pojos.OrgMemberMappingInfo.OrgCenterInfo;
import com.nhance.android.pojos.OrgMemberMappingInfo.OrgProgramBasicInfo;
import com.nhance.android.pojos.OrgMemberMappingInfo.OrgProgramCenterSectionIds;
import com.nhance.android.pojos.OrgMemberMappingInfo.OrgSectionInfo;
import com.nhance.android.utils.SQLDBUtil;

public class OrgDataManager extends AbstractDataManager {

    private static final String TABLE                     = "organization";
    private static final String TABLE_PENDING_TRANSACTION = "pending_transaction";
    private static final String TAG                       = "OrgDataManager";

    public OrgDataManager(Context context) {

        super(context);
    }

    @Override
    public void createDummyData() {

    }

    public void insertOrganization(Organization organization) throws Exception {

        Log.d(TAG, "inserting into Organization table");
        ContentValues values = organization.toContentValues();
        organization._id = (int) insert(TABLE, values);
    }

    public int updateOrganization(Organization organization) {

        ContentValues values = null;
        try {
            values = organization.toContentValues();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        if (values == null) {
            return -1;
        }
        return update(TABLE, values, "_id=" + organization._id, null);
    }

    public Organization getOrganization(String orgId, String cmdsUrl) {

        return getOrganization(orgId, cmdsUrl, null);
    }

    public Organization getOrganization(String orgId, String cmdsUrl, String slug) {

        if (orgId == null && slug == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        addSelectQuery(sb, TABLE);
        sb.append("WHERE ");

        if (addStringEqualSQLQuery(ConstantGlobal.CMDS_URL, cmdsUrl, sb, true)) {
            sb.append("AND ");
        }

        if (addStringEqualSQLQuery(ConstantGlobal.ORG_ID, orgId, sb, false)
                && !TextUtils.isEmpty(slug)) {
            sb.append("AND ");
        }
        addStringEqualSQLQuery(ConstantGlobal.SLUG, slug, sb, true);

        Cursor cursor = rawQuery(sb.toString(), null);
        Organization org = null;
        if (cursor != null && cursor.moveToFirst()) {
            org = SQLDBUtil.convertToValues(cursor, Organization.class, null);
        }
        closeCursor(cursor);
        Log.d(TAG, "org : " + org + " for orgId:" + orgId);
        return org;
    }

    public List<Organization> getOrganizations() {

        Cursor cursor = rawQuery("select * from " + TABLE, null);
        List<Organization> orgs = new ArrayList<Organization>();
        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                Organization org = SQLDBUtil.convertToValues(cursor, Organization.class, null);
                orgs.add(org);
                cursor.moveToNext();
            }
        }
        closeCursor(cursor);
        Log.d(TAG, "orgs : " + orgs);
        return orgs;
    }

    public byte[] getOrgPublicKey(String orgId, String cmdsUrl) {

        Organization org = getOrganization(orgId, cmdsUrl);
        if (org != null) {
            return org.key;
        }
        return null;
    }

    public void insertPendingTransaction(PendingTranscation pendingTranscation) throws Exception {

        Log.d(TAG, "inserting into endingTransaction table");
        ContentValues values = pendingTranscation.toContentValues();
        pendingTranscation._id = (int) insert(TABLE_PENDING_TRANSACTION, values);
    }

    public int updatePendingTransaction(PendingTranscation pendingTranscation) {

        ContentValues values = null;
        try {
            values = pendingTranscation.toContentValues();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        if (values == null) {
            return -1;
        }
        return update(TABLE_PENDING_TRANSACTION, values, "_id=" + pendingTranscation._id, null);
    }

    public PendingTranscation getPendingTranscation(String transactionId) {

        StringBuilder sb = new StringBuilder();
        addSelectQuery(sb, TABLE_PENDING_TRANSACTION);
        sb.append("WHERE ");
        addStringEqualSQLQuery(ConstantGlobal.TRANSACTION_ID, transactionId, sb, true);

        Cursor cursor = rawQuery(sb.toString(), null);
        PendingTranscation pendingTranscation = null;
        if (cursor != null && cursor.moveToFirst()) {
            pendingTranscation = SQLDBUtil.convertToValues(cursor, PendingTranscation.class, null);
        }
        closeCursor(cursor);
        Log.d(TAG, "pendingTranscation : " + pendingTranscation + " for transcationId:"
                + transactionId);
        return pendingTranscation;
    }

    public List<PendingTranscation> getPendingTransactions() {

        Cursor cursor = rawQuery("select * from " + TABLE_PENDING_TRANSACTION, null);
        List<PendingTranscation> transactions = new ArrayList<PendingTranscation>();
        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                PendingTranscation org = SQLDBUtil.convertToValues(cursor,
                        PendingTranscation.class, null);
                transactions.add(org);
                cursor.moveToNext();
            }
        }
        closeCursor(cursor);
        Log.d(TAG, "pending transactions : " + transactions);
        return transactions;
    }

    public void deletePendingTransaction(String transactionId) {

        delete(TABLE_PENDING_TRANSACTION, ConstantGlobal.TRANSACTION_ID + "='" + transactionId
                + "'", null);
    }

    @Override
    public void cleanData() {

        try {
            execSQL("DROP TABLE IF EXISTS " + TABLE);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public static void createTable(List<String> createTables) {

        StringBuilder sb = new StringBuilder();
        addCreateTableQuery(sb, TABLE);
        addAbstractAbstractDataModelFeildsRow(sb);
        sb.append(ConstantGlobal.NAME + " text not null,");
        sb.append(ConstantGlobal.FULL_NAME + " text,");
        sb.append(ConstantGlobal.DESC + " text,");
        sb.append(ConstantGlobal.THUMB + " text,");
        sb.append(ConstantGlobal.WEB_SITE + " text,");
        sb.append(ConstantGlobal.CONTACT_NO + " text,");
        sb.append(ConstantGlobal.ORG_ID + " text not null,");
        sb.append(ConstantGlobal.CMDS_URL + " text not null,");
        sb.append(ConstantGlobal.SLUG + " text not null,");
        sb.append(ConstantGlobal.AUTH_TYPE + " text,");
        sb.append("autoLogin" + " integer,");
        sb.append(ConstantGlobal.KEY + " blob");
        endCreateTableQuery(sb);
        createTables.add(sb.toString());

        createTables.add(createIndexQuery(TABLE, "org_id", true, ConstantGlobal.CMDS_URL,
                ConstantGlobal.ORG_ID));
        createPendingTransactionTable(createTables);

    }

    private static void createPendingTransactionTable(List<String> createTables) {

        StringBuilder sb = new StringBuilder();
        addCreateTableQuery(sb, TABLE_PENDING_TRANSACTION);
        addAbstractAbstractDataModelFeildsRow(sb);
        sb.append(ConstantGlobal.TRANSACTION_ID + " text not null,");
        sb.append(ConstantGlobal.ORDER_ID + " text not null,");
        sb.append(ConstantGlobal.TRANSACTION_STATUS + " text not null,");
        sb.append("transactionInfo" + " text,");
        sb.append("step" + " integer,");
        sb.append("progCenterSecInfo" + " text");
        endCreateTableQuery(sb);
        createTables.add(sb.toString());
        createTables.add(createIndexQuery(TABLE_PENDING_TRANSACTION, "transcation_id", true,
                ConstantGlobal.TRANSACTION_ID));
    }

    public static HashMap<String, OrgProgramCenterSectionIds> getProgramCenterSectionIds(
            OrgMemberInfo orgMemberInfo) {

        HashMap<String, OrgProgramCenterSectionIds> list = new HashMap<String, OrgProgramCenterSectionIds>();
        if (orgMemberInfo.mappings == null || orgMemberInfo.mappings.programs == null) {
            return null;
        }
        for (OrgProgramBasicInfo program : orgMemberInfo.mappings.programs) {
            String programId = program.id;
            for (OrgCenterInfo center : program.centers) {
                String centerId = center.id;
                for (OrgSectionInfo section : center.sections) {
                    String sectionId = section.id;
                    OrgProgramCenterSectionIds ids = new OrgProgramCenterSectionIds(programId,
                            centerId, sectionId);
                    list.put(sectionId, ids);
                }
            }
        }
        return list;
    }

    public static List<String> getCenterSectionString(OrgMemberInfo orgMemberInfo,
            List<OrgSectionInfo> sectionsList) {

        return getCenterSectionString(orgMemberInfo, sectionsList, null);
    }

    public static List<String> getCenterSectionString(OrgMemberInfo orgMemberInfo,
            List<OrgSectionInfo> sectionsList, List<ProgCenterSecInfo> progCenterSectionInfos) {

        List<String> progCenterSections = new ArrayList<String>();
        if (orgMemberInfo.mappings == null || orgMemberInfo.mappings.programs == null) {
            return progCenterSections;
        }

        for (OrgProgramBasicInfo program : orgMemberInfo.mappings.programs) {
            String programName = program.name;
            for (OrgCenterInfo center : program.centers) {
                String centerName = center.name;
                for (OrgSectionInfo section : center.sections) {
                    String sectionName = section.name;
                    String finalSecName = programName + ", " + centerName + ", " + sectionName;
                    progCenterSections.add(finalSecName);
                    if (sectionsList != null) {
                        if (progCenterSectionInfos != null) {
                            ProgCenterSecInfo progCenterInfo = new ProgCenterSecInfo(program.id,
                                    center.id, section.id, section.accessScope,
                                    section.revenueModel, section.costRate);
                            progCenterSectionInfos.add(progCenterInfo);
                        }
                        sectionsList.add(section);
                    }
                }
            }
        }
        return progCenterSections;
    }

    public static List<String> getProgramCenterSectionString(OrgMemberInfo orgMemberInfo,
                                                             List<OrgSectionInfo> sectionsList, List<ProgCenterSecInfo> progCenterSectionInfos) {

        List<String> progCenterSections = new ArrayList<String>();
        if (orgMemberInfo.mappings == null || orgMemberInfo.mappings.programs == null) {
            return progCenterSections;
        }

        for (OrgProgramBasicInfo program : orgMemberInfo.mappings.programs) {
            String programName = program.name;
            for (OrgCenterInfo center : program.centers) {
                String centerName = center.name;
                for (OrgSectionInfo section : center.sections) {
//                    String sectionName = section.name;
                    String finalSecName = programName + ", " + centerName ;
                    progCenterSections.add(finalSecName);
                    if (sectionsList != null) {
                        if (progCenterSectionInfos != null) {
                            ProgCenterSecInfo progCenterInfo = new ProgCenterSecInfo(program.id,
                                    center.id, section.id, section.accessScope,
                                    section.revenueModel, section.costRate);
                            progCenterSectionInfos.add(progCenterInfo);
                        }
                        sectionsList.add(section);
                    }
                }
            }
        }
        return progCenterSections;
    }
}
