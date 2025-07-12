package com.imaginamos.farmatodo.model.order;

public class ItemsOrderDomain {

    private int quantitySold;
    private boolean anywaySelling;
    private Double fullPrice;
    private String grayDescription;
    private boolean highlight;
    private String mediaDescription;
    private String mediaImageUrl;
    private Double offerPercentage;
    private Double offerPrice;
    private boolean outstanding;
    private String sales;
    private String spaces;
    private Long taxRate;
    private Long totalStock;
    private String id;
    private boolean changeQuantity;
    private Double unitPrice;
    private Double taxes;
    private Double deliveryPrice;
    private boolean onlyOnline;
    private boolean availableOnline;
    private boolean noFullPrice;
    private boolean generic;

    public boolean isAnywaySelling() {
        return anywaySelling;
    }

    public void setAnywaySelling(boolean anywaySelling) {
        this.anywaySelling = anywaySelling;
    }

    public int getQuantitySold() {
        return quantitySold;
    }

    public void setQuantitySold(int quantitySold) {
        this.quantitySold = quantitySold;
    }


    public Double getFullPrice() {
        return fullPrice;
    }

    public void setFullPrice(Double fullPrice) {
        this.fullPrice = fullPrice;
    }

    public String getGrayDescription() {
        return grayDescription;
    }

    public void setGrayDescription(String grayDescription) {
        this.grayDescription = grayDescription;
    }

    public boolean isHighlight() {
        return highlight;
    }

    public void setHighlight(boolean highlight) {
        this.highlight = highlight;
    }

    public String getMediaDescription() {
        return mediaDescription;
    }

    public void setMediaDescription(String mediaDescription) {
        this.mediaDescription = mediaDescription;
    }

    public String getMediaImageUrl() {
        return mediaImageUrl;
    }

    public void setMediaImageUrl(String mediaImageUrl) {
        this.mediaImageUrl = mediaImageUrl;
    }

    public Double getOfferPercentage() {
        return offerPercentage;
    }

    public void setOfferPercentage(Double offerPercentage) {
        this.offerPercentage = offerPercentage;
    }

    public Double getOfferPrice() {
        return offerPrice;
    }

    public void setOfferPrice(Double offerPrice) {
        this.offerPrice = offerPrice;
    }

    public boolean isOutstanding() {
        return outstanding;
    }

    public void setOutstanding(boolean outstanding) {
        this.outstanding = outstanding;
    }

    public String getSales() {
        return sales;
    }

    public void setSales(String sales) {
        this.sales = sales;
    }

    public String getSpaces() {
        return spaces;
    }

    public void setSpaces(String spaces) {
        this.spaces = spaces;
    }

    public Long getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(Long taxRate) {
        this.taxRate = taxRate;
    }

    public Long getTotalStock() {
        return totalStock;
    }

    public void setTotalStock(Long totalStock) {
        this.totalStock = totalStock;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isChangeQuantity() {
        return changeQuantity;
    }

    public void setChangeQuantity(boolean changeQuantity) {
        this.changeQuantity = changeQuantity;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Double getTaxes() {
        return taxes;
    }

    public void setTaxes(Double taxes) {
        this.taxes = taxes;
    }

    public Double getDeliveryPrice() {
        return deliveryPrice;
    }

    public void setDeliveryPrice(Double deliveryPrice) {
        this.deliveryPrice = deliveryPrice;
    }

    public boolean isOnlyOnline() {
        return onlyOnline;
    }

    public void setOnlyOnline(boolean onlyOnline) {
        this.onlyOnline = onlyOnline;
    }

    public boolean isAvailableOnline() {
        return availableOnline;
    }

    public void setAvailableOnline(boolean availableOnline) {
        this.availableOnline = availableOnline;
    }

    public boolean isNoFullPrice() {
        return noFullPrice;
    }

    public void setNoFullPrice(boolean noFullPrice) {
        this.noFullPrice = noFullPrice;
    }

    public boolean isGeneric() {
        return generic;
    }

    public void setGeneric(boolean generic) {
        this.generic = generic;
    }
}
