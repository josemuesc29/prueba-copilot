package com.imaginamos.farmatodo.model.item;

public class ItemSubscribeAndSaveRequest {
    private Integer id;
    private Boolean subscribeAndSave;
    private String standardDuration;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getSubscribeAndSave() {
        return subscribeAndSave;
    }

    public void setSubscribeAndSave(Boolean subscribeAndSave) {
        this.subscribeAndSave = subscribeAndSave;
    }

    public String getStandardDuration() {
        return standardDuration;
    }

    public void setStandardDuration(String standardDuration) {
        this.standardDuration = standardDuration;
    }

}
