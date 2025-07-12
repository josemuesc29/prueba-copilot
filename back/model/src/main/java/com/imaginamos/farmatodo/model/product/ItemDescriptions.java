package com.imaginamos.farmatodo.model.product;

import java.util.Objects;

public class ItemDescriptions {
    private Long itemId;
    private String grayDescription;
    private String mediaDescription;
    private String largeDescription;

    private String objectID;
    private String description;
    private String detailDescription;

    public ItemDescriptions(){}

    public ItemDescriptions(String objectID, String grayDescripcion, String mediaDescription, String largeDescription) {
        this.objectID = objectID;
        if(Objects.nonNull(grayDescripcion) && !grayDescripcion.isEmpty()) {
            this.detailDescription = grayDescripcion;
        }
        if(Objects.nonNull(mediaDescription) && !mediaDescription.isEmpty()) {
            this.description = mediaDescription;
            this.mediaDescription = mediaDescription;
        }
        if(Objects.nonNull(largeDescription) && !largeDescription.isEmpty()) {
            this.largeDescription = largeDescription;
        }
    }

    public Long getItemId() { return itemId; }

    public void setItemId(Long itemId) { this.itemId = itemId; }

    public String getGrayDescription() { return grayDescription; }

    public void setGrayDescription(String grayDescription) { this.grayDescription = grayDescription; }

    public String getMediaDescription() { return mediaDescription; }

    public void setMediaDescription(String mediaDescription) { this.mediaDescription = mediaDescription; }

    public String getLargeDescription() { return largeDescription; }

    public void setLargeDescription(String largeDescription) { this.largeDescription = largeDescription; }

    public String getObjectID() { return objectID; }

    public void setObjectID(String objectID) { this.objectID = objectID; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public String getDetailDescription() { return detailDescription; }

    public void setDetailDescription(String detailDescription) { this.detailDescription = detailDescription; }

    @Override
    public String toString() {
        return "ItemDescriptions{" +
                "itemId=" + itemId +
                ", grayDescription='" + grayDescription + '\'' +
                ", mediaDescription='" + mediaDescription + '\'' +
                ", largeDescription='" + largeDescription + '\'' +
                ", objectID='" + objectID + '\'' +
                ", description='" + description + '\'' +
                ", detailDescription='" + detailDescription + '\'' +
                '}';
    }
}
