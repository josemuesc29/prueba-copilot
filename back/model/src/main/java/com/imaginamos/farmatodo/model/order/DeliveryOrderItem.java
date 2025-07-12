package com.imaginamos.farmatodo.model.order;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.*;
import com.imaginamos.farmatodo.model.algolia.OfferComboPopUp;
import com.imaginamos.farmatodo.model.item.OpticalItemFilter;
import com.imaginamos.farmatodo.model.optics.ShipmentType;
import com.imaginamos.farmatodo.model.product.Capacity;
import com.imaginamos.farmatodo.model.product.ColorPicker;
import com.imaginamos.farmatodo.model.product.Item;
import com.imaginamos.farmatodo.model.product.ItemMarketplaceSellerAddress;
import com.imaginamos.farmatodo.model.product.ItemMarketplaceDimensions;
import com.imaginamos.farmatodo.model.product.ItemMarketplaceVariant;
import java.util.Date;
import java.util.List;

import static com.imaginamos.farmatodo.model.util.Constants.SAMPLING_PRICE;

/**
 * Created by mileniopc on 10/26/16.
 * Property of Imaginamos.
 */

@Entity
public class DeliveryOrderItem implements com.imaginamos.farmatodo.model.intefaces.Item {
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Id
  private String idDeliveryOrderItem;
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Parent
  private Ref<DeliveryOrder> idDeliveryOrder;
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Index
  private Key<Item> idItem;
  private int quantitySold;
  private Date createDate;
  @IgnoreSave
  private String idDeliveryOrderItemWebSafe;
  //Item information
  private boolean anywaySelling;
  private String barcode;
  private List<String> barcodeList;

  private String brand;
  @Index
  private String filtersOptical;

  @IgnoreSave
  private OpticalItemFilter opticalFilter;

  @Index
  private Long idClassification;
  private Double fullPrice;
  private String grayDescription;
  @Index
  private boolean highlight;
  private boolean isGeneric;
  private String mediaDescription;
  private String mediaImageUrl;
  private int offerPercentage;
  private Double offerPrice;
  @Index
  private boolean outstanding = false;
  private String requirePrescription;
  private long sales;
  private long spaces;
  private String status;
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Index
  private int idStoreGroup;
  private int taxRate;
  @Index
  private int totalStock;
  private int stock;
  @Index
  private long id;
  @IgnoreSave
  private List<ColorPicker> colors;
  @IgnoreSave
  private ColorPicker color;
  @IgnoreSave
  private Capacity filter;
  @IgnoreSave
  private List<Capacity> filters;
  @IgnoreSave
  private String filterType;
  @Index
  private boolean changeQuantity;
  private String offerText;
  private String offerDescription;
  private Boolean coupon;
  private Boolean isSubstitute;

  // Campos sugeridos proveedores externos
  @Index
  private int deliveryPrice;


  private boolean onlyOnline;

  @IgnoreSave
  private String deliveryTime;

  @IgnoreSave
  private long deliveryStatus;

  @IgnoreSave
  private String categorie;

  @IgnoreSave
  private String marca;

  @IgnoreSave
  private List<String> departments;

  @IgnoreSave
  private boolean requirePrescriptionImage;

  @IgnoreSave
  private String subCategory;

  @IgnoreSave
  private String supplier;

  @Index
  private Boolean scanAndGo;

  private String origin;

  private String observations;

  private Boolean handleQuantity;

  @IgnoreSave
  private Boolean itemOfferPopUp;

  // Show items no billed|delivered
  @IgnoreSave
  private boolean isBilled = true;

  @IgnoreSave
  private boolean isQuantitySoldGreaterThanStock;

  @IgnoreSave
  private String messageWhenQuantitySoldIsGreaterThanStock;

  @IgnoreSave
  private String itemUrl;

  @IgnoreSave
  private OfferComboPopUp offerComboPopUp;

  @IgnoreSave
  private Integer quantityRequested;

  //Prime
  private Double primePrice;
  private String primeTextDiscount;
  private String primeDescription;

  //RMS Classes
  private String rms_class;
  private String rms_deparment;
  private String rms_group;
  private String rms_subclass;

  private String filterCategories;

  //Talon One
  private boolean isTalonDiscount = false;
  private boolean talonItemFree = false;

  private String url;

  @Index
  private ShipmentType shipment;

  private String customTag;

  //data Marketplace
  @IgnoreSave
  private List<ItemMarketplaceVariant> variants;
  @IgnoreSave
  private List<ItemMarketplaceSellerAddress> sellerAddresses;
  @IgnoreSave
  private ItemMarketplaceDimensions dimensions;
  @IgnoreSave
  private int deliveryDays;

  private String uuidItem;

  @IgnoreSave
  private boolean requirePrescriptionMedical;


    public String getCustomTag() {
        return customTag;
    }

    public void setCustomTag(String customTag) {
        this.customTag = customTag;
    }


  public ShipmentType getShipment() {
    return shipment;
  }

  public void setShipment(ShipmentType shipment) {
    this.shipment = shipment;
  }

  public Capacity getFilter() {
    return filter;
  }

  public void setFilter(Capacity filter) {
    this.filter = filter;
  }

  public ColorPicker getColor() {
    return color;
  }

  public void setColor(ColorPicker color) {
    this.color = color;
  }

  public String getFilterType() { return filterType; }

  public void setFilterType(String filterType) { this.filterType = filterType; }

  public DeliveryOrderItem() { }

  public DeliveryOrderItem(long id, int quantitySold) {
    this.id = id;
    this.quantitySold = quantitySold;
  }

  public Boolean getItemOfferPopUp() {
    return itemOfferPopUp;
  }

  public void setItemOfferPopUp(Boolean itemOfferPopUp) {
    itemOfferPopUp = itemOfferPopUp;
  }

  public String getDeliveryTime() {
    return deliveryTime;
  }

  public void setDeliveryTime(String deliveryTime) {
    this.deliveryTime = deliveryTime;
  }

  public boolean isOnlyOnline() {
    return onlyOnline;
  }

  public void setOnlyOnline(boolean onlyOnline) {
    this.onlyOnline = onlyOnline;
  }

  public String getIdDeliveryOrderItem() {
    return idDeliveryOrderItem;
  }

  public void setIdDeliveryOrderItem(String idDeliveryOrderItem) {
    this.idDeliveryOrderItem = idDeliveryOrderItem;
  }

  public Ref<DeliveryOrder> getIdDeliveryOrder() {
    return idDeliveryOrder;
  }

  public void setIdDeliveryOrder(Ref<DeliveryOrder> idDeliveryOrder) {
    this.idDeliveryOrder = idDeliveryOrder;
  }

  public Key<Item> getIdItem() {
    return idItem;
  }

  public void setIdItem(Key<Item> idItem) {
    this.idItem = idItem;
  }

  public int getQuantitySold() {
    return quantitySold;
  }

  public void setQuantitySold(int quantitySold) {
    this.quantitySold = quantitySold;
  }

  public Date getCreateDate() {
    return createDate;
  }

  public void setCreateDate(Date createDate) {
    this.createDate = createDate;
  }

  public String getIdDeliveryOrderItemWebSafe() {
    return idDeliveryOrderItemWebSafe;
  }

  public void setIdDeliveryOrderItemWebSafe(String idDeliveryOrderItemWebSafe) {
    this.idDeliveryOrderItemWebSafe = idDeliveryOrderItemWebSafe;
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

  public List<String> getBarcodeList() {
    return barcodeList;
  }

  public void setBarcodeList(List<String> barcodeList) {
    this.barcodeList = barcodeList;
  }

  public String getBrand() {
    return brand;
  }

  public void setBrand(String brand) {
    this.brand = brand;
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

  public boolean isGeneric() {
    return isGeneric;
  }

  public void setGeneric(boolean generic) {
    isGeneric = generic;
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

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public int getIdStoreGroup() {
    return idStoreGroup;
  }

  public void setIdStoreGroup(int idStoreGroup) {
    this.idStoreGroup = idStoreGroup;
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

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public boolean getChangeQuantity() {
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

  public void setCoupon(Boolean coupon) {    this.coupon = coupon;  }

  public int getDeliveryPrice() { return deliveryPrice; }

  public void setDeliveryPrice(int deliveryPrice) { this.deliveryPrice = deliveryPrice; }

  public long getDeliveryStatus() { return deliveryStatus; }

  public void setDeliveryStatus(long deliveryStatus) { this.deliveryStatus = deliveryStatus; }

  public String getCategorie() { return categorie; }

  public void setCategorie(String categorie) { this.categorie = categorie; }

  public String getMarca() { return marca; }

  public void setMarca(String marca) { this.marca = marca; }

  public List<String> getDepartments() { return departments; }

  public void setDepartments(List<String> departments) { this.departments = departments; }

  public String getSubCategory() { return subCategory; }

  public void setSubCategory(String subCategory) { this.subCategory = subCategory; }

  public String getSupplier() { return supplier; }

  public void setSupplier(String supplier) { this.supplier = supplier; }

  public String getOrigin() { return origin; }

  public void setOrigin(String origin) { this.origin = origin; }

  public String getObservations() { return observations; }

  public void setObservations(String observations) { this.observations = observations;}

  public Boolean getHandleQuantity() { return handleQuantity;  }

  public void setHandleQuantity(Boolean handleQuantity) { this.handleQuantity = handleQuantity; }

  public Boolean getScanAndGo() {
    return scanAndGo;
  }

  public void setScanAndGo(Boolean scanAndGo) {
    this.scanAndGo = scanAndGo;
  }

  public Boolean getSubstitute() {
    return isSubstitute;
  }

  public void setSubstitute(Boolean substitute) {
    isSubstitute = substitute;
  }

  public boolean isBilled() {
    return isBilled;
  }

  public void setBilled(boolean billed) {
    isBilled = billed;
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
  public List<ColorPicker> getColors() {
    return colors;
  }

  public void setColors(List<ColorPicker> colors) {
    this.colors = colors;
  }

  public List<Capacity> getFilters() {
    return filters;
  }

  public void setFilters(List<Capacity> filters) {
    this.filters = filters;
  }

  public String getItemUrl() {
    return itemUrl;
  }

  public void setItemUrl(String itemUrl) {
    this.itemUrl = itemUrl;
  }

  public OfferComboPopUp getOfferComboPopUp() {
    return offerComboPopUp;
  }

  public void setOfferComboPopUp(OfferComboPopUp offerComboPopUp) {
    this.offerComboPopUp = offerComboPopUp;
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

  public String getRms_deparment() {
    return rms_deparment;
  }

  public void setRms_deparment(String rms_deparment) {
    this.rms_deparment = rms_deparment;
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

  public String getFilterCategories() {
    return filterCategories;
  }

  public void setFilterCategories(String filterCategories) {
    this.filterCategories = filterCategories;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public int getStock() {
    return stock;
  }

  public void setStock(int stock) {
    this.stock = stock;
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

  public List<ItemMarketplaceVariant> getVariants() {
    return variants;
  }

  public void setVariants(List<ItemMarketplaceVariant> variants) {
    this.variants = variants;
  }

  public List<ItemMarketplaceSellerAddress> getSellerAddresses() {
    return sellerAddresses;
  }

  public void setSellerAddresses(List<ItemMarketplaceSellerAddress> sellerAddresses) {
    this.sellerAddresses = sellerAddresses;
  }

  public ItemMarketplaceDimensions getDimensions() {
    return dimensions;
  }

  public void setDimensions(ItemMarketplaceDimensions dimensions) {
    this.dimensions = dimensions;
  }

  
  public int getDeliveryDays() {
    return deliveryDays;
  }

  public void setDeliveryDays(int deliveryDays) {
    this.deliveryDays = deliveryDays;
  }

  public boolean isRequirePrescriptionMedical() {
      return requirePrescriptionMedical;
  }
  public void setRequirePrescriptionMedical(boolean requirePrescriptionMedical) {
      this.requirePrescriptionMedical = requirePrescriptionMedical;
  }
  public Integer getQuantityRequested() {
    return quantityRequested;
  }

  public void setQuantityRequested(Integer quantityRequested) {
    this.quantityRequested = quantityRequested;
  }

  public String getUuidItem() {
    return uuidItem;
  }

  public void setUuidItem(String uuidItem) {
    this.uuidItem = uuidItem;
  }

  public boolean isSampling(){
      return fullPrice != null && SAMPLING_PRICE.equals(fullPrice);
  }

  public boolean isRequirePrescriptionImage() {
    return requirePrescriptionImage;
  }

  public void setRequirePrescriptionImage(boolean requirePrescriptionImage) {
    this.requirePrescriptionImage = requirePrescriptionImage;
  }

  @Override
  public String toString() {
    return "DeliveryOrderItem{" +
            "idDeliveryOrderItem='" + idDeliveryOrderItem + '\'' +
            ", idDeliveryOrder=" + idDeliveryOrder +
            ", idItem=" + idItem +
            ", quantitySold=" + quantitySold +
            ", createDate=" + createDate +
            ", idDeliveryOrderItemWebSafe='" + idDeliveryOrderItemWebSafe + '\'' +
            ", anywaySelling=" + anywaySelling +
            ", barcode='" + barcode + '\'' +
            ", barcodeList=" + barcodeList +
            ", brand='" + brand + '\'' +
            ", filtersOptical='" + filtersOptical + '\'' +
            ", opticalFilter=" + opticalFilter +
            ", idClassification=" + idClassification +
            ", fullPrice=" + fullPrice +
            ", grayDescription='" + grayDescription + '\'' +
            ", highlight=" + highlight +
            ", isGeneric=" + isGeneric +
            ", mediaDescription='" + mediaDescription + '\'' +
            ", mediaImageUrl='" + mediaImageUrl + '\'' +
            ", offerPercentage=" + offerPercentage +
            ", offerPrice=" + offerPrice +
            ", outstanding=" + outstanding +
            ", requirePrescription='" + requirePrescription + '\'' +
            ", sales=" + sales +
            ", spaces=" + spaces +
            ", status='" + status + '\'' +
            ", idStoreGroup=" + idStoreGroup +
            ", taxRate=" + taxRate +
            ", totalStock=" + totalStock +
            ", stock=" + stock +
            ", id=" + id +
            ", colors=" + colors +
            ", color=" + color +
            ", filter=" + filter +
            ", filters=" + filters +
            ", filterType='" + filterType + '\'' +
            ", changeQuantity=" + changeQuantity +
            ", offerText='" + offerText + '\'' +
            ", offerDescription='" + offerDescription + '\'' +
            ", coupon=" + coupon +
            ", isSubstitute=" + isSubstitute +
            ", deliveryPrice=" + deliveryPrice +
            ", onlyOnline=" + onlyOnline +
            ", deliveryTime='" + deliveryTime + '\'' +
            ", deliveryStatus=" + deliveryStatus +
            ", categorie='" + categorie + '\'' +
            ", marca='" + marca + '\'' +
            ", departments=" + departments +
            ", subCategory='" + subCategory + '\'' +
            ", supplier='" + supplier + '\'' +
            ", scanAndGo=" + scanAndGo +
            ", origin='" + origin + '\'' +
            ", observations='" + observations + '\'' +
            ", handleQuantity=" + handleQuantity +
            ", itemOfferPopUp=" + itemOfferPopUp +
            ", isBilled=" + isBilled +
            ", isQuantitySoldGreaterThanStock=" + isQuantitySoldGreaterThanStock +
            ", messageWhenQuantitySoldIsGreaterThanStock='" + messageWhenQuantitySoldIsGreaterThanStock + '\'' +
            ", itemUrl='" + itemUrl + '\'' +
            ", offerComboPopUp=" + offerComboPopUp +
            ", primePrice=" + primePrice +
            ", primeTextDiscount='" + primeTextDiscount + '\'' +
            ", primeDescription='" + primeDescription + '\'' +
            ", rms_class='" + rms_class + '\'' +
            ", rms_deparment='" + rms_deparment + '\'' +
            ", rms_group='" + rms_group + '\'' +
            ", rms_subclass='" + rms_subclass + '\'' +
            ", filterCategories='" + filterCategories + '\'' +
            ", isTalonDiscount=" + isTalonDiscount +
            ", talonItemFree=" + talonItemFree +
            ", url='" + url + '\'' +
            ", uuidItem='" + uuidItem + '\'' +
            ", shipment=" + shipment +
            '}';
  }

  public String toStringJson(){
    return new Gson().toJson(this);
  }
}
