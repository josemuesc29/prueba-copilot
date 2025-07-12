package com.imaginamos.farmatodo.networking.models.algolia;

public class CarruselRecommendConfig {

    private String objectID;
    private boolean recommend;

    public CarruselRecommendConfig() {
    }

    public CarruselRecommendConfig(boolean recommend) {
        this.recommend = recommend;
    }

    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }

    public boolean isRecommend() {
        return recommend;
    }

    public void setRecommend(boolean recommend) {
        this.recommend = recommend;
    }
}
