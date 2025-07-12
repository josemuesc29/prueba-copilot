package com.imaginamos.farmatodo.model.algolia;

/**
 * Created by JPuentes on 18/10/2018.
 */
public class AlgoliaItem {
    private String mediaImageUrl;
    private String description;
    private Integer fullPrice;
    private String mediaDescription;
    private String barcode;
    private String brand;
    private Integer sales;
    private String requirePrescription;
    private String detailDescription;
    private Integer offerPrice;
    private String offerDescription;
    private Integer stock;
    private Integer totalStock;
    private String id;
    private String offerText;
    private Integer idStoreGroup;
    private String objectID;
    private Boolean subscribeAndSave;
    private String standardDuration;

    public AlgoliaItem(){}

    public AlgoliaItem(String id, Integer idStoreGroup, Integer totalStock){
        this.id = id;
        this.totalStock = totalStock;
        this.idStoreGroup = idStoreGroup;
        this.objectID = id+idStoreGroup;
    }

    public String getMediaImageUrl() {
        return mediaImageUrl;
    }

    public void setMediaImageUrl(String mediaImageUrl) {
        this.mediaImageUrl = mediaImageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getFullPrice() {
        return fullPrice;
    }

    public void setFullPrice(Integer fullPrice) {
        this.fullPrice = fullPrice;
    }

    public String getMediaDescription() {
        return mediaDescription;
    }

    public void setMediaDescription(String mediaDescription) {
        this.mediaDescription = mediaDescription;
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

    public Integer getSales() {
        return sales;
    }

    public void setSales(Integer sales) {
        this.sales = sales;
    }

    public String getRequirePrescription() {
        return requirePrescription;
    }

    public void setRequirePrescription(String requirePrescription) {
        this.requirePrescription = requirePrescription;
    }

    public String getDetailDescription() {
        return detailDescription;
    }

    public void setDetailDescription(String detailDescription) {
        this.detailDescription = detailDescription;
    }

    public Integer getOfferPrice() {
        return offerPrice;
    }

    public void setOfferPrice(Integer offerPrice) {
        this.offerPrice = offerPrice;
    }

    public String getOfferDescription() {
        return offerDescription;
    }

    public void setOfferDescription(String offerDescription) {
        this.offerDescription = offerDescription;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOfferText() {
        return offerText;
    }

    public void setOfferText(String offerText) {
        this.offerText = offerText;
    }

    public Integer getIdStoreGroup() {
        return idStoreGroup;
    }

    public void setIdStoreGroup(Integer idStoreGroup) {
        this.idStoreGroup = idStoreGroup;
    }

    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }

    public Boolean getSubscribeAndSave() {
        return subscribeAndSave;
    }

    public Integer getTotalStock() {
        return totalStock;
    }

    public void setTotalStock(Integer totalStock) {
        this.totalStock = totalStock;
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
