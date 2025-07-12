package com.imaginamos.farmatodo.model.product;

public class ItemOffer {
    private String objectID;
    private Long itemId;
    private Integer offerPrice;
    private String offerDescription;
    private String offerText;

    public ItemOffer() { }

    public ItemOffer(String objectID, Integer offerPrice, String offerDescription, String offerText) {
        this.objectID = objectID;
        this.offerPrice = offerPrice;
        this.offerDescription = offerDescription;
        this.offerText = offerText;
    }

    public Long getItemId() { return itemId; }

    public void setItemId(Long itemId) { this.itemId = itemId; }

    public String getObjectID() { return objectID; }

    public void setObjectID(String objectID) { this.objectID = objectID; }

    public Integer getOfferPrice() { return offerPrice; }

    public void setOfferPrice(Integer offerPrice) { this.offerPrice = offerPrice; }

    public String getOfferDescription() { return offerDescription; }

    public void setOfferDescription(String offerDescription) { this.offerDescription = offerDescription; }

    public String getOfferText() { return offerText; }

    public void setOfferText(String offerText) { this.offerText = offerText; }
}
