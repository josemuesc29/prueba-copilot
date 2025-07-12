package com.imaginamos.farmatodo.model.customer;

public class CustomerPhotoData {
    private String photoUrl;
    private Long photoType;

    public CustomerPhotoData(String photoUrl, Long photoType) {
        this.photoUrl = photoUrl;
        this.photoType = photoType;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Long getPhotoType() {
        return photoType;
    }

    public void setPhotoType(Long photoType) {
        this.photoType = photoType;
    }
}
