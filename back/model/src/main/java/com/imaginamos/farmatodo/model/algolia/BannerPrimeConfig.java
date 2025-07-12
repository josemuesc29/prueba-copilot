package com.imaginamos.farmatodo.model.algolia;

public class BannerPrimeConfig {
    private boolean enable;
    private String info;
    private String objectID;

    public BannerPrimeConfig() {
    }


    public boolean isEnable() {
        return enable;
    }
    public void setEnable(boolean enable) {
        this.enable = enable;
    }
    public String getInfo() {
        return info;
    }
    public void setInfo(String info) {
        this.info = info;
    }
    public String getObjectID() {
        return objectID;
    }
    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }


}
