package com.imaginamos.farmatodo.model.algolia;


public class RateLimitConfig {

    private String objectID;
    private Long max_tries;
    private Long time_in_seconds;


    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }

    public Long getMax_tries() {
        return max_tries;
    }

    public void setMax_tries(Long max_tries) {
        this.max_tries = max_tries;
    }

    public Long getTime_in_seconds() {
        return time_in_seconds;
    }

    public void setTime_in_seconds(Long time_in_seconds) {
        this.time_in_seconds = time_in_seconds;
    }
}
