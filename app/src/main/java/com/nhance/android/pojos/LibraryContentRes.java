package com.nhance.android.pojos;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


import com.nhance.android.db.models.entity.Content;
import com.nhance.android.enums.EntityType;
import com.nhance.android.pojos.content.infos.IContentInfo;
import com.nhance.android.pojos.content.infos.ModuleExtendedInfo;
import com.nhance.android.pojos.slpmodules.ModuleEntryInfo;

public class LibraryContentRes extends Content {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public String             linkId;
    public boolean            downloadable;
    public String             linkTime;             // creation time of the link(i.e makeVisible
    // time)
    public String             encLevel;
    public String             passphrase;
    public List<SrcEntity>    downloadableEntities;
    public long               position;

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{starred:").append(starred).append(", lastViewed:").append(lastViewed)
                .append(", position:").append(position).append("}");
        return builder.toString();
    }

    @Override
    public IContentInfo toContentExtendedInfo() {

        IContentInfo info = super.toContentExtendedInfo();
        EntityType eType = EntityType.valueOfKey(type);
        if (eType == EntityType.MODULE) {
            ModuleExtendedInfo mExtendedInfo = (ModuleExtendedInfo) info;
            Set<SrcEntity> dEntities = this.downloadableEntities != null ? new HashSet<SrcEntity>(
                    this.downloadableEntities) : new HashSet<SrcEntity>();
            for (ModuleEntryInfo mEntryInfo : mExtendedInfo.children) {
                if (dEntities.contains(mEntryInfo.entity)) {
                    mEntryInfo.downloadable = true;
                }
            }
        }
        return info;
    }

    @Override
    public boolean equalsIgnoreCase(String attempted) {
        return false;
    }
    // @Override
    // public String toString() {
    //
    // StringBuilder builder = new StringBuilder();
    // builder.append("{linkId:").append(linkId).append(", downloadable:").append(downloadable)
    // .append(", linkTime:").append(linkTime).append(", encLevel:").append(encLevel)
    // .append(", passphrase:").append(passphrase).append(", id:").append(id)
    // .append(", type:").append(type).append(", name:").append(name).append(", desc:")
    // .append(desc).append(", info:").append(info).append(", subType:").append(subType)
    // .append(", thumb:").append(thumb).append(", file:").append(file)
    // .append(", userId:").append(userId).append(", ownerId:").append(ownerId)
    // .append(", ownerName:").append(ownerName).append(", lastUpdated:")
    // .append(lastUpdated).append(", downloaded:").append(downloaded)
    // .append(", starred:").append(starred).append(", lastViewed:").append(lastViewed)
    // .append(", brdIds:").append(brdIds).append(", tags:").append(tags)
    // .append(", targetIds:").append(targetIds).append(", targetNames:")
    // .append(targetNames).append(", passphrase:").append(passphrase).append(", _id:")
    // .append(_id).append(", orgKeyId:").append(orgKeyId).append(", timeCreated:")
    // .append(timeCreated).append("}");
    // return builder.toString();
    // }

}
