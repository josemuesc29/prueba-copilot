package com.imaginamos.farmatodo.model.item;

public class ItemRequest {
    private long item;
    private String barcode;
    private String brand;
    private String grayDescription;
    private String mediaDescription;
    private boolean isHighlight;
    private String mediaImageUrl;
    private String requirePrescription;
    private String status;

    public long getItem() {
        return item;
    }

    public void setItem(long item) {
        this.item = item;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getGrayDescription() {
        return grayDescription;
    }

    public void setGrayDescription(String grayDescription) {
        this.grayDescription = grayDescription;
    }

    public String getMediaDescription() {
        return mediaDescription;
    }

    public void setMediaDescription(String mediaDescription) {
        this.mediaDescription = mediaDescription;
    }

    public boolean isHighlight() {
        return isHighlight;
    }

    public void setHighlight(boolean highlight) {
        isHighlight = highlight;
    }

    public String getMediaImageUrl() {
        return mediaImageUrl;
    }

    public void setMediaImageUrl(String mediaImageUrl) {
        this.mediaImageUrl = mediaImageUrl;
    }

    public String getRequirePrescription() {
        return requirePrescription;
    }

    public void setRequirePrescription(String requirePrescription) {
        this.requirePrescription = requirePrescription;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
