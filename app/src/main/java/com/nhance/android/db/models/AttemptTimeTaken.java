package com.nhance.android.db.models;

public class AttemptTimeTaken extends AbstractDataModel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public int                timeTaken;
    public String             key;

    public AttemptTimeTaken() {

        super();
    }

    public AttemptTimeTaken(int orgKeyId, int timeTaken, String key) {

        super(orgKeyId);
        this.timeTaken = timeTaken;
        this.key = key;
    }

}
