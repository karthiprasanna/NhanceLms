package com.nhance.android.pojos;

import java.io.Serializable;

import com.nhance.android.pojos.OrgMemberMappingInfo.OrgProgramBasicInfo;
import com.nhance.android.pojos.OrgMemberMappingInfo.OrgStructureBasicInfo;

public class PackageInfo implements Serializable {

    /**
     * 
     */
    private static final long    serialVersionUID = 1L;

    public String                name;
    public OrgProgramBasicInfo   programInfo;
    public OrgStructureBasicInfo centerInfo;
    public OrgStructureBasicInfo sectionInfo;
    public OrgStructureBasicInfo orgInfo;

    // in bytes
    public long                  totalSize;

}
