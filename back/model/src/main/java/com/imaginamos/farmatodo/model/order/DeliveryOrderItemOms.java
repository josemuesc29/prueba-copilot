package com.imaginamos.farmatodo.model.order;

import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.appengine.repackaged.com.google.gson.GsonBuilder;
import com.imaginamos.farmatodo.model.item.OpticalItemFilter;
import com.imaginamos.farmatodo.model.product.ColorPicker;

import java.util.List;

public class DeliveryOrderItemOms {
    private static final Gson GSON = new GsonBuilder().create();
    private int quantitySold;

    //Item information
    private String createDate;
    private boolean anywaySelling;
    private String barcode;
    private String brand;
    private String filtersOptical;
    private OpticalItemFilter opticalFilter;
    private Long idClassification;
    private Double fullPrice;
    private String grayDescription;
    private boolean highlight;
    private String mediaDescription;
    private String mediaImageUrl;
    private int offerPercentage;
    private boolean outstanding;
    private String requirePrescription;
    private long sales;
    private long spaces;
    private int taxRate;
    private int totalStock;
    private int stock;
    private long id;
    private ColorPicker color;
    private String filterType;
    private boolean changeQuantity;
    private String offerText;
    private String offerDescription;
    private Boolean coupon;
    private Boolean isSubstitute;

    // Campos sugeridos proveedores externos
    private int deliveryPrice;
    private boolean onlyOnline;
    private long deliveryStatus;
    private String categorie;
    private String marca;
    private List<String> departments;
    private String subCategory;
    private String supplier;
    private String origin;

    // Show items no billed|delivered
    private boolean billed = true;
    private boolean isQuantitySoldGreaterThanStock;
    private String messageWhenQuantitySoldIsGreaterThanStock;

    //Prime
    private Double primePrice;
    private String primeTextDiscount;
    private String primeDescription;

    //RMS Classes
    private String rms_class;
    private String rms_department;
    private String rms_group;
    private String rms_subclass;

    //Talon One
    private boolean isTalonDiscount;
    private boolean talonItemFree;
    private String url;

    public int getQuantitySold() {
        return quantitySold;
    }

    public void setQuantitySold(int quantitySold) {
        this.quantitySold = quantitySold;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public boolean isAnywaySelling() {
        return anywaySelling;
    }

    public void setAnywaySelling(boolean anywaySelling) {
        this.anywaySelling = anywaySelling;
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

    public String getFiltersOptical() {
        return filtersOptical;
    }

    public void setFiltersOptical(String filtersOptical) {
        this.filtersOptical = filtersOptical;
    }

    public OpticalItemFilter getOpticalFilter() {
        return opticalFilter;
    }

    public void setOpticalFilter(OpticalItemFilter opticalFilter) {
        this.opticalFilter = opticalFilter;
    }

    public Long getIdClassification() {
        return idClassification;
    }

    public void setIdClassification(Long idClassification) {
        this.idClassification = idClassification;
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

    public int getOfferPercentage() {
        return offerPercentage;
    }

    public void setOfferPercentage(int offerPercentage) {
        this.offerPercentage = offerPercentage;
    }

    public boolean isOutstanding() {
        return outstanding;
    }

    public void setOutstanding(boolean outstanding) {
        this.outstanding = outstanding;
    }

    public String getRequirePrescription() {
        return requirePrescription;
    }

    public void setRequirePrescription(String requirePrescription) {
        this.requirePrescription = requirePrescription;
    }

    public long getSales() {
        return sales;
    }

    public void setSales(long sales) {
        this.sales = sales;
    }

    public long getSpaces() {
        return spaces;
    }

    public void setSpaces(long spaces) {
        this.spaces = spaces;
    }

    public int getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(int taxRate) {
        this.taxRate = taxRate;
    }

    public int getTotalStock() {
        return totalStock;
    }

    public void setTotalStock(int totalStock) {
        this.totalStock = totalStock;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ColorPicker getColor() {
        return color;
    }

    public void setColor(ColorPicker color) {
        this.color = color;
    }

    public String getFilterType() {
        return filterType;
    }

    public void setFilterType(String filterType) {
        this.filterType = filterType;
    }

    public boolean isChangeQuantity() {
        return changeQuantity;
    }

    public void setChangeQuantity(boolean changeQuantity) {
        this.changeQuantity = changeQuantity;
    }

    public String getOfferText() {
        return offerText;
    }

    public void setOfferText(String offerText) {
        this.offerText = offerText;
    }

    public String getOfferDescription() {
        return offerDescription;
    }

    public void setOfferDescription(String offerDescription) {
        this.offerDescription = offerDescription;
    }

    public Boolean getCoupon() {
        return coupon;
    }

    public void setCoupon(Boolean coupon) {
        this.coupon = coupon;
    }

    public Boolean getSubstitute() {
        return isSubstitute;
    }

    public void setSubstitute(Boolean substitute) {
        isSubstitute = substitute;
    }

    public int getDeliveryPrice() {
        return deliveryPrice;
    }

    public void setDeliveryPrice(int deliveryPrice) {
        this.deliveryPrice = deliveryPrice;
    }

    public boolean isOnlyOnline() {
        return onlyOnline;
    }

    public void setOnlyOnline(boolean onlyOnline) {
        this.onlyOnline = onlyOnline;
    }

    public long getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(long deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public List<String> getDepartments() {
        return departments;
    }

    public void setDepartments(List<String> departments) {
        this.departments = departments;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public boolean isBilled() {
        return billed;
    }

    public void setBilled(boolean billed) {
        this.billed = billed;
    }

    public boolean isQuantitySoldGreaterThanStock() {
        return isQuantitySoldGreaterThanStock;
    }

    public void setQuantitySoldGreaterThanStock(boolean quantitySoldGreaterThanStock) {
        isQuantitySoldGreaterThanStock = quantitySoldGreaterThanStock;
    }

    public String getMessageWhenQuantitySoldIsGreaterThanStock() {
        return messageWhenQuantitySoldIsGreaterThanStock;
    }

    public void setMessageWhenQuantitySoldIsGreaterThanStock(String messageWhenQuantitySoldIsGreaterThanStock) {
        this.messageWhenQuantitySoldIsGreaterThanStock = messageWhenQuantitySoldIsGreaterThanStock;
    }

    public Double getPrimePrice() {
        return primePrice;
    }

    public void setPrimePrice(Double primePrice) {
        this.primePrice = primePrice;
    }

    public String getPrimeTextDiscount() {
        return primeTextDiscount;
    }

    public void setPrimeTextDiscount(String primeTextDiscount) {
        this.primeTextDiscount = primeTextDiscount;
    }

    public String getPrimeDescription() {
        return primeDescription;
    }

    public void setPrimeDescription(String primeDescription) {
        this.primeDescription = primeDescription;
    }

    public String getRms_class() {
        return rms_class;
    }

    public void setRms_class(String rms_class) {
        this.rms_class = rms_class;
    }

    public String getRms_department() {
        return rms_department;
    }

    public void setRms_department(String rms_department) {
        this.rms_department = rms_department;
    }

    public String getRms_group() {
        return rms_group;
    }

    public void setRms_group(String rms_group) {
        this.rms_group = rms_group;
    }

    public String getRms_subclass() {
        return rms_subclass;
    }

    public void setRms_subclass(String rms_subclass) {
        this.rms_subclass = rms_subclass;
    }

    public boolean isTalonDiscount() {
        return isTalonDiscount;
    }

    public void setTalonDiscount(boolean talonDiscount) {
        isTalonDiscount = talonDiscount;
    }

    public boolean isTalonItemFree() {
        return talonItemFree;
    }

    public void setTalonItemFree(boolean talonItemFree) {
        this.talonItemFree = talonItemFree;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return GSON.toJson(this);
    }
}
