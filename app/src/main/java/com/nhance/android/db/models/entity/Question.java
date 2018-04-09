package com.nhance.android.db.models.entity;

import org.json.JSONObject;

public class Question extends ContentBoardEntity {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    // name--> content of the question
    public String             name;
    public String             id;

    // questionType SCQ/MCQ etc
    public String             type;
    public String             difficulty;

    public String             options;

    // JSONObject--> Map<String, List<String>>
    public JSONObject         matrix;

    // optional field for a question/it will be used only in case of off-line test result upload of
    // an organization
    public String             code;
    public boolean            hasAns;

    // i.e Arihant, H.C. Verma etc;
    public String             source;

    public Question() {

        super();
    }

    @Override
    public boolean equalsIgnoreCase(String attempted) {
        return false;
    }

    public Question(String userId, String timeCreated, int orgKeyId, String name, String id,
                    String type, String difficulty, String options, JSONObject matrix, String code,
                    boolean hasAns, String source, String lastUpdated, String brdIds, String tags) {

        super(timeCreated, orgKeyId, lastUpdated, brdIds, tags);
        this.userId = userId;
        this.name = name;
        this.id = id;
        this.type = type;
        this.difficulty = difficulty;
        this.options = options;
        this.matrix = matrix;
        this.code = code;
        this.hasAns = hasAns;
        this.source = source;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{name:").append(name).append(", id:").append(id).append(", type:")
                .append(type).append(", difficulty:").append(difficulty).append(", options:")
                .append(options).append(", matrix:").append(matrix).append(", code:").append(code)
                .append(", hasAns:").append(hasAns).append(", source:").append(source)
                .append(", userId:").append(userId).append(", ownerId:").append(ownerId)
                .append(", ownerName:").append(ownerName).append(", lastUpdated:")
                .append(lastUpdated).append(", downloaded:").append(downloaded)
                .append(", starred:").append(starred).append(", lastViewed:").append(lastViewed)
                .append(", brdIds:").append(brdIds).append(", tags:").append(tags)
                .append(", targetIds:").append(targetIds).append(", targetNames:")
                .append(targetNames).append(", _id:").append(_id).append(", orgKeyId:")
                .append(orgKeyId).append(", timeCreated:").append(timeCreated).append("}");
        return builder.toString();
    }

}
