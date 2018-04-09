package com.nhance.android.pojos;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.enums.SectionAccessScope;
import com.nhance.android.enums.SectionRevenueModel;
import com.nhance.android.utils.JSONAware;
import com.nhance.android.utils.JSONUtils;

public class OrgMemberMappingInfo implements JSONAware {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public List<OrgProgramBasicInfo>      programs;
    private static OrgBasicInfoComparator comparator = new OrgBasicInfoComparator();

    @SuppressWarnings("unchecked")
    @Override
    public void fromJSON(JSONObject json) {

        programs = (List<OrgProgramBasicInfo>) JSONUtils.getJSONAwareCollection(
                OrgProgramBasicInfo.class, json, "programs");
        Collections.sort(programs, comparator);

    }

    @Override
    public JSONObject toJSON() {

        return null;
    }

    public Set<String> _getSectionIds() {

        Set<String> ids = new HashSet<String>();
        if (programs == null) {
            return ids;
        }
        for (OrgProgramBasicInfo peInfo : programs) {
            ids.addAll(peInfo._getSectionIds());
        }
        return ids;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{programs:").append(programs).append("}");
        return builder.toString();
    }

    public static class OrgProgramBasicInfo extends OrgStructureBasicInfo {

        /**
         * 
         */
        private static final long  serialVersionUID = 1L;
        public String              departmentId;
        public String              departmentName;
        public String              departmentCode;
        public List<OrgCenterInfo> centers;

        @SuppressWarnings("unchecked")
        @Override
        public void fromJSON(JSONObject json) {

            super.fromJSON(json);
            departmentCode = JSONUtils.getString(json, "departmentCode");
            departmentId = JSONUtils.getString(json, "departmentId");
            departmentName = JSONUtils.getString(json, "departmentName");
            centers = (List<OrgCenterInfo>) JSONUtils.getJSONAwareCollection(OrgCenterInfo.class,
                    json, "centers");
            Collections.sort(centers, comparator);
        }

        public Set<String> _getSectionIds() {

            Set<String> ids = new HashSet<String>();
            if (centers == null) {
                return ids;
            }
            for (OrgCenterInfo ceInfo : centers) {
                ids.addAll(ceInfo._getSectionIds());
            }
            return ids;
        }

        @Override
        public String toString() {

            StringBuilder builder = new StringBuilder();
            builder.append("{departmentId:").append(departmentId).append(", departmentName:")
                    .append(departmentName).append(", departmentCode:").append(departmentCode)
                    .append(", centers:").append(centers).append(", id:").append(id)
                    .append(", name:").append(name).append(", code:").append(code)
                    .append(", type:").append(type).append(", desc:").append(desc).append("}");
            return builder.toString();
        }

    }

    public static class OrgCenterInfo extends OrgStructureBasicInfo {

        /**
         * 
         */
        private static final long   serialVersionUID = 1L;
        public List<OrgSectionInfo> sections;

        @SuppressWarnings("unchecked")
        @Override
        public void fromJSON(JSONObject json) {

            super.fromJSON(json);
            sections = (List<OrgSectionInfo>) JSONUtils.getJSONAwareCollection(
                    OrgSectionInfo.class, json, "sections");
            Collections.sort(sections, comparator);
        }

        @Override
        public String toString() {

            StringBuilder builder = new StringBuilder();
            builder.append("{sections:").append(sections).append(", id:").append(id)
                    .append(", name:").append(name).append(", code:").append(code)
                    .append(", type:").append(type).append(", desc:").append(desc).append("}");
            return builder.toString();
        }

        public Set<String> _getSectionIds() {

            Set<String> ids = new HashSet<String>();
            if (sections == null) {
                return ids;
            }
            for (OrgSectionInfo section : sections) {
                ids.add(section.id);
            }
            return ids;
        }

    }

    public static class OrgProgramCenterSectionIds implements Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        public String programId;
        public String centerId;
        public String sectionId;

        public OrgProgramCenterSectionIds(String programId, String centerId, String sectionId) {

            super();
            this.programId = programId;
            this.centerId = centerId;
            this.sectionId = sectionId;
        }

        @Override
        public String toString() {

            return "ProgramId = " + programId + ", centerId = " + centerId + ", sectionId = "
                    + sectionId;
        }
    }

    public static class OrgSectionInfo extends OrgStructureBasicInfo {

        /**
         * 
         */
        private static final long          serialVersionUID = 1L;
        public List<OrgStructureBasicInfo> courses;
        public CostRate                    costRate;
        public SectionRevenueModel         revenueModel;
        public SectionAccessScope          accessScope;
        public long                        timeJoined;
        public String                      orderId;

        @SuppressWarnings("unchecked")
        @Override
        public void fromJSON(JSONObject json) {

            super.fromJSON(json);
            courses = (List<OrgStructureBasicInfo>) JSONUtils.getJSONAwareCollection(
                    OrgStructureBasicInfo.class, json, "courses");
            Collections.sort(courses, comparator);
            JSONObject cRate = JSONUtils.getJSONObject(json, ConstantGlobal.COST_RATE);
            costRate = new CostRate();
            if (cRate != null) {
                costRate.fromJSON(cRate);
            }
            revenueModel = SectionRevenueModel.valueOfKey(JSONUtils.getString(json,
                    ConstantGlobal.REVENUE_MODEL));
            accessScope = SectionAccessScope.valueOfKey(JSONUtils.getString(json,
                    ConstantGlobal.ACCESS_SCOPE));
            timeJoined = JSONUtils.getLong(json, "timeJoined");
            orderId = JSONUtils.getString(json, ConstantGlobal.ORDER_ID);
        }

        @Override
        public String toString() {

            StringBuilder builder = new StringBuilder();
            builder.append("{id:").append(id).append(", name:").append(name).append(", code:")
                    .append(code).append(", type:").append(type).append(", desc:").append(desc)
                    .append("}");
            return builder.toString();
        }

    }

    public static class OrgStructureBasicInfo implements JSONAware, Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        public String             id;
        public String             name;
        public String             code;
        public String             type;
        public String             desc;

        public OrgStructureBasicInfo() {

            super();

        }

        public OrgStructureBasicInfo(String id, String name, String code, String type, String desc) {

            super();
            this.id = id;
            this.name = name;
            this.code = code;
            this.type = type;
            this.desc = desc;
        }

        @Override
        public void fromJSON(JSONObject json) {

            id = JSONUtils.getString(json, ConstantGlobal.ID);
            name = JSONUtils.getString(json, ConstantGlobal.NAME);
            code = JSONUtils.getString(json, ConstantGlobal.CODE);
            type = JSONUtils.getString(json, ConstantGlobal.TYPE);
            desc = StringUtils.defaultString(JSONUtils.getString(json, ConstantGlobal.DESC));
            // TODO: remove this hard check
            if (desc != null && desc.equals("null")) {
                desc = StringUtils.EMPTY;
            }
        }

        @Override
        public JSONObject toJSON() {

            return null;
        }

        @Override
        public String toString() {

            StringBuilder builder = new StringBuilder();
            builder.append("{id:").append(id).append(", name:").append(name).append(", code:")
                    .append(code).append(", type:").append(type).append(", desc:").append(desc)
                    .append("}");
            return builder.toString();
        }

        @Override
        public int hashCode() {

            return ((id == null) ? 0 : id.hashCode()) + ((type == null) ? 0 : type.hashCode());
        }

        @Override
        public boolean equals(Object obj) {

            OrgStructureBasicInfo other;
            return obj != null
                    && getClass() == obj.getClass()
                    && ((other = (OrgStructureBasicInfo) obj).id.equals(id) && other.type
                            .equals(type));
        }
    }

    private static class OrgBasicInfoComparator implements Comparator<OrgStructureBasicInfo> {

        @Override
        public int compare(OrgStructureBasicInfo lhs, OrgStructureBasicInfo rhs) {

            return StringUtils.lowerCase(lhs.name).compareTo(StringUtils.lowerCase(rhs.name));
        }

    }
}
