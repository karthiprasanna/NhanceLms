package com.nhance.android.db.models.entity;


public class BoardModel extends AbstractEntity {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public String             id;
    public int                parentId;
    public String             type;
    public String             code;

    public BoardModel() {

        this(null);
    }

    public BoardModel(String name) {

        super(name);
    }

    public BoardModel(int orgKeyId, String name, String id, int parentId, String type, String code) {

        super(name);
        this.orgKeyId = orgKeyId;
        this.id = id;
        this.parentId = parentId;
        this.type = type;
        this.code = code;
    }

    @Override
    public int hashCode() {

        return ((id == null) ? 0 : id.hashCode()) + ((type == null) ? 0 : type.hashCode());
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;

        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BoardModel other = (BoardModel) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{id:").append(id).append(", parentId:").append(parentId).append(", type:")
                .append(type).append(", code:").append(code).append(", name:").append(name)
                .append(", _id:").append(_id).append(", orgKeyId:").append(orgKeyId)
                .append(", timeCreated:").append(timeCreated).append("}");
        return builder.toString();
    }

}
