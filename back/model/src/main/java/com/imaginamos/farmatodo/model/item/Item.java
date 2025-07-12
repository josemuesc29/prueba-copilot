package com.imaginamos.farmatodo.model.item;

/**
 * Created by JPuentes on 22/06/2018.
 */
public class Item {

    private Boolean outstanding;
    private Integer sales;
    private Double fullPrice;
    private Boolean isCoupon;
    private String largeDesc;
    private Integer spaces;
    private Boolean isGeneric;
    private Integer idStoreGroup;
    private String grayDesc;
    private String offerText;
    private Boolean anywaySelling;
    private String mediaDesc;
    private String status;
    private String offerDesc;
    private String brand;
    private String barcode;
    private Boolean toIndexAlg;
    private Integer taxRate;
    private Integer itemId;
    private String urlImage;
    private Double offerPrice;
    private Integer totalStock;
    private Boolean toDelete;
    private Boolean requirePresc;
    private Boolean highlight;

    public Boolean getOutstanding() {
        return outstanding;
    }

    public void setOutstanding(Boolean outstanding) {
        this.outstanding = outstanding;
    }

    public Integer getSales() {
        return sales;
    }

    public void setSales(Integer sales) {
        this.sales = sales;
    }

    public Double getFullPrice() {
        return fullPrice;
    }

    public void setFullPrice(Double fullPrice) {
        this.fullPrice = fullPrice;
    }

    public Boolean getCoupon() {
        return isCoupon;
    }

    public void setCoupon(Boolean coupon) {
        isCoupon = coupon;
    }

    public String getLargeDesc() {
        return largeDesc;
    }

    public void setLargeDesc(String largeDesc) {
        this.largeDesc = largeDesc;
    }

    public Integer getSpaces() {
        return spaces;
    }

    public void setSpaces(Integer spaces) {
        this.spaces = spaces;
    }

    public Boolean getGeneric() {
        return isGeneric;
    }

    public void setGeneric(Boolean generic) {
        isGeneric = generic;
    }

    public Integer getIdStoreGroup() {
        return idStoreGroup;
    }

    public void setIdStoreGroup(Integer idStoreGroup) {
        this.idStoreGroup = idStoreGroup;
    }

    public String getGrayDesc() {
        return grayDesc;
    }

    public void setGrayDesc(String grayDesc) {
        this.grayDesc = grayDesc;
    }

    public String getOfferText() {
        return offerText;
    }

    public void setOfferText(String offerText) {
        this.offerText = offerText;
    }

    public Boolean getAnywaySelling() {
        return anywaySelling;
    }

    public void setAnywaySelling(Boolean anywaySelling) {
        this.anywaySelling = anywaySelling;
    }

    public String getMediaDesc() {
        return mediaDesc;
    }

    public void setMediaDesc(String mediaDesc) {
        this.mediaDesc = mediaDesc;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOfferDesc() {
        return offerDesc;
    }

    public void setOfferDesc(String offerDesc) {
        this.offerDesc = offerDesc;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public Boolean getToIndexAlg() {
        return toIndexAlg;
    }

    public void setToIndexAlg(Boolean toIndexAlg) {
        this.toIndexAlg = toIndexAlg;
    }

    public Integer getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(Integer taxRate) {
        this.taxRate = taxRate;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public String getUrlImage() {
        return urlImage;
    }

    public void setUrlImage(String urlImage) {
        this.urlImage = urlImage;
    }

    public Double getOfferPrice() {
        return offerPrice;
    }

    public void setOfferPrice(Double offerPrice) {
        this.offerPrice = offerPrice;
    }

    public Integer getTotalStock() {
        return totalStock;
    }

    public void setTotalStock(Integer totalStock) {
        this.totalStock = totalStock;
    }

    public Boolean getToDelete() {
        return toDelete;
    }

    public void setToDelete(Boolean toDelete) {
        this.toDelete = toDelete;
    }

    public Boolean getRequirePresc() {
        return requirePresc;
    }

    public void setRequirePresc(Boolean requirePresc) {
        this.requirePresc = requirePresc;
    }

    public Boolean getHighlight() {
        return highlight;
    }

    public void setHighlight(Boolean highlight) {
        this.highlight = highlight;
    }

    @Override
    public String toString() {
        return "Item{" +
                "outstanding=" + outstanding +
                ", sales=" + sales +
                ", fullPrice=" + fullPrice +
                ", isCoupon=" + isCoupon +
                ", largeDesc='" + largeDesc + '\'' +
                ", spaces=" + spaces +
                ", isGeneric=" + isGeneric +
                ", idStoreGroup=" + idStoreGroup +
                ", grayDesc='" + grayDesc + '\'' +
                ", offerText='" + offerText + '\'' +
                ", anywaySelling=" + anywaySelling +
                ", mediaDesc='" + mediaDesc + '\'' +
                ", status='" + status + '\'' +
                ", offerDesc='" + offerDesc + '\'' +
                ", brand='" + brand + '\'' +
                ", barcode='" + barcode + '\'' +
                ", toIndexAlg=" + toIndexAlg +
                ", taxRate=" + taxRate +
                ", itemId=" + itemId +
                ", urlImage='" + urlImage + '\'' +
                ", offerPrice=" + offerPrice +
                ", totalStock=" + totalStock +
                ", toDelete=" + toDelete +
                ", requirePresc=" + requirePresc +
                ", highlight=" + highlight +
                '}';
    }
}

