package com.imaginamos.farmatodo.backend.images;

public class UploadImageRequest {
    private Integer item;
    private String imageBase64;
    private String fileName;
    private Integer position;
    private Boolean principal;

    public Integer getItem() {
        return item;
    }

    public void setItem(Integer item) {
        this.item = item;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Boolean getPrincipal() { return principal; }

    public void setPrincipal(Boolean principal) { this.principal = principal; }
}
