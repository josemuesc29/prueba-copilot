package com.imaginamos.farmatodo.model.algolia;

import com.google.appengine.repackaged.com.google.gson.Gson;
import com.imaginamos.farmatodo.model.optics.ItemOpticsComplete;
import com.imaginamos.farmatodo.model.optics.ShipmentType;
import com.imaginamos.farmatodo.model.product.Capacity;
import com.imaginamos.farmatodo.model.product.ColorPicker;
import com.imaginamos.farmatodo.model.product.ItemFilter;
import com.imaginamos.farmatodo.model.product.ItemMarketplaceVariant;
import com.imaginamos.farmatodo.model.product.ItemMarketplaceSellerAddress;
import com.imaginamos.farmatodo.model.product.ItemMarketplaceDimensions;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

public class ItemAlgolia implements Serializable {


    private static final Logger LOG = Logger.getLogger(ItemAlgolia.class.getName());

    private String mediaImageUrl;
    private String description;
    private Double fullPrice;
    private String mediaDescription;
    private String grayDescription;
    private String barcode;
    private String brand;
    private Integer sales;
    private Boolean requirePrescription;
    private String detailDescription;
    private Double offerPrice;
    private String offerDescription;
    private Integer stock;
    private String id;
    private String offerText;
    private Integer idStoreGroup;
    private String marca;
    private String objectID;
    private boolean onlyOnline;
    private String deliveryTime;
    private List<ItemMarketplaceVariant> variants;
    private List<ItemMarketplaceSellerAddress> sellerAddresses;
    private ItemMarketplaceDimensions dimensions;
    private String uuidItem;
    private String warranty;
    private String warrantyTerms;

    // Campos adicionales para el detalle
    private boolean highlight;
    private boolean generic;
    private String largeDescription;
    private boolean anywaySelling;
    private int deliveryPrice;
    private int idClassification;
    private boolean outstanding = false;
    private int quantitySold;
    private long spaces;
    private String status;
    private int taxRate;
    private List<String> listUrlImages;
    private float measurePum;
    private String labelPum;
    private Boolean subscribeAndSave;
    private String standardDuration;
    private boolean expressWithSubscription;
    private String categorie;
    private List<String> id_highlights;
    private List<String> id_suggested;
    private List<String> barcodeList;

    private ColorPicker color;
    private List<ColorPicker> colors;
    private String filterType;
    private Capacity filter;
    private List<Capacity> filtersLoreal;

    private String filtersOptical;
    private int deliveryDays;

    // Nuevos campos creados para el tag del portal
    private List<String> departments;

    private boolean requirePrescriptionImage;

    private String subCategory;
    private String supplier;
    private List<ItemFilter> filters;
    private Boolean handleQuantity;

    // Objetos para el manejo del carrito
    private long item;
    private int quantityRequested;
    private Double discount;
    private Double price;
    private int calculatedPrice;
    private int quantityBonus;
    private boolean access;
    private int itemDeliveryPrice;
    private Integer totalStock;
    private double newPrice;

    //SEO
    /**
     * TODO: Revisar de donde se trae la info de seo
     */
    private String textoSEO;

    // Pre-render
    private String itemUrl;

    private Boolean outofstore;

    //Flash offers
    private boolean isFlashOffer;
    private long offerStartDate;
    private long offerEndDate;

    //Prime
    private Double primePrice;
    private String primeTextDiscount;
    private String primeDescription;

    //RMS Classes
    private String rms_class;
    private String rms_deparment;
    private String rms_group;
    private String rms_subclass;
    private boolean without_stock;

    // Talon One
    private boolean isTalonDiscount = false;
    private boolean talonItemFree = false;
    private String filterCategories;

    private String url;

    private ItemOpticsComplete itemOpticsComplete;

    private String customTag;

    private String starProduct;
    private boolean requirePrescriptionMedical;

    private String customLabelForStockZero;

    private Boolean hasStock;

    private List<Long> stores_with_stock;

    public ItemAlgolia() {
    }

    public String getStarProduct() {
        return starProduct;
    }

    public void setStarProduct(String starProduct) {
        this.starProduct = starProduct;
    }

    public float getMeasurePum() {
        return measurePum;
    }

    public void setMeasurePum(float measurePum) {
        this.measurePum = measurePum;
    }

    public String getLabelPum() {
        return labelPum;
    }

    public void setLabelPum(String labelPum) {
        this.labelPum = labelPum;
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

    public Double getFullPrice() {
        return fullPrice;
    }

    public void setFullPrice(Double fullPrice) {
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

    public Integer getSales() {
        return sales;
    }

    public void setSales(Integer sales) {
        this.sales = sales;
    }

    public Boolean getRequirePrescription() {
        return requirePrescription;
    }

    public void setRequirePrescription(Boolean requirePrescription) {
        this.requirePrescription = requirePrescription;
    }

    public String getDetailDescription() {
        return detailDescription;
    }

    public void setDetailDescription(String detailDescription) {
        this.detailDescription = detailDescription;
    }

    public Double getOfferPrice() {
        return offerPrice;
    }

    public void setOfferPrice(Double offerPrice) {
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

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }

    public boolean isOnlyOnline() {
        return onlyOnline;
    }

    public void setOnlyOnline(boolean onlyOnline) {
        this.onlyOnline = onlyOnline;
    }

    public String getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(String deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public boolean isHighlight() {
        return highlight;
    }

    public void setHighlight(boolean highlight) {
        this.highlight = highlight;
    }

    public boolean isGeneric() {
        return generic;
    }

    public void setGeneric(boolean generic) {
        this.generic = generic;
    }

    public String getLargeDescription() {
        return largeDescription;
    }

    public void setColor(ColorPicker color){this.color = color;}

    public void  setColors(List<ColorPicker> colors) {this.colors = colors;}

    public Capacity getFilter() {
        return filter;
    }

    public void setFilter(Capacity filter) {
        this.filter = filter;
    }

    public List<Capacity> getFiltersLoreal() {
        return filtersLoreal;
    }

    public void setFiltersLoreal(List<Capacity> filtersLoreal) {
        this.filtersLoreal = filtersLoreal;
    }

    public ColorPicker getColor(){return color;}

    public List<ColorPicker>  getColors() {return colors;}

    public String getFilterType() { return filterType; }

    public void setFilterType(String filterType) {
        this.filterType = filterType;
    }

    public void setLargeDescription(String largeDescription) {
        this.largeDescription = largeDescription;
    }

    public boolean isAnywaySelling() {
        return anywaySelling;
    }

    public void setAnywaySelling(boolean anywaySelling) {
        this.anywaySelling = anywaySelling;
    }

    public int getDeliveryPrice() {
        return deliveryPrice;
    }

    public void setDeliveryPrice(int deliveryPrice) {
        this.deliveryPrice = deliveryPrice;
    }

    public int getIdClassification() {
        return idClassification;
    }

    public void setIdClassification(int idClassification) {
        this.idClassification = idClassification;
    }

    public boolean isOutstanding() {
        return outstanding;
    }

    public void setOutstanding(boolean outstanding) {
        this.outstanding = outstanding;
    }

    public int getQuantitySold() {
        return quantitySold;
    }

    public void setQuantitySold(int quantitySold) {
        this.quantitySold = quantitySold;
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

    public int getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(int taxRate) {
        this.taxRate = taxRate;
    }

    public List<String> getListUrlImages() {
        return listUrlImages;
    }

    public void setListUrlImages(List<String> listUrlImages) {
        this.listUrlImages = listUrlImages;
    }

    public Boolean getSubscribeAndSave() {
        return subscribeAndSave;
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

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public boolean isValid() {
        return (mediaImageUrl != null && fullPrice != null && description != null && mediaDescription != null && stock != null && id != null);
    }

    public List<String> getId_highlights() {
        return id_highlights;
    }

    public void setId_highlights(List<String> id_highlights) {
        this.id_highlights = id_highlights;
    }

    public List<String> getId_suggested() {
        return id_suggested;
    }

    public void setId_suggested(List<String> id_suggested) {
        this.id_suggested = id_suggested;
    }


    public boolean isExpressWithSubscription() {
        return expressWithSubscription;
    }

    public void setExpressWithSubscription(boolean expressWithSubscription) {
        this.expressWithSubscription = expressWithSubscription;
    }

    public List<String> getDepartments() { return departments; }

    public void setDepartments(List<String> departments) { this.departments = departments; }

    public String getSubCategory() { return subCategory; }

    public void setSubCategory(String subCategory) { this.subCategory = subCategory; }

    public String getSupplier() { return supplier; }

    public void setSupplier(String supplier) { this.supplier = supplier; }

    public List<ItemFilter> getFilters() { return filters; }

    public void setFilters(List<ItemFilter> filters) { this.filters = filters; }

    public Boolean getHandleQuantity() { return handleQuantity; }

    public void setHandleQuantity(Boolean handleQuantity) { this.handleQuantity = handleQuantity; }

    public long getItem() { return item; }

    public void setItem(long item) { this.item = item; }

    public int getQuantityRequested() { return quantityRequested; }

    public void setQuantityRequested(int quantityRequested) { this.quantityRequested = quantityRequested; }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public Double getPrice() { return price; }

    public void setPrice(Double price) { this.price = price; }

    public int getCalculatedPrice() { return calculatedPrice; }

    public void setCalculatedPrice(int calculatedPrice) { this.calculatedPrice = calculatedPrice; }

    public int getQuantityBonus() { return quantityBonus; }

    public void setQuantityBonus(int quantityBonus) { this.quantityBonus = quantityBonus; }

    public boolean getAccess() { return access; }

    public void setAccess(boolean access) { this.access = access; }

    public int getItemDeliveryPrice() { return itemDeliveryPrice; }

    public void setItemDeliveryPrice(int itemDeliveryPrice) { this.itemDeliveryPrice = itemDeliveryPrice; }

    public Integer getTotalStock() { return totalStock; }

    public void setTotalStock(Integer totalStock) { this.totalStock = totalStock; }

    public String getItemUrl() {
        return itemUrl;
    }

    public void setItemUrl(String itemUrl) {
        this.itemUrl = itemUrl;
    }

    public String getTextoSEO() {
        return textoSEO;
    }

    public void setTextoSEO(String textoSEO) {
        this.textoSEO = textoSEO;
    }

    public Boolean getOutofstore() {
        return outofstore;
    }

    public void setOutofstore(Boolean outofstore) {
        this.outofstore = outofstore;
    }

    public boolean getIsFlashOffer() { return isFlashOffer; }

    public void setFlashOffer(boolean flashOffer) { isFlashOffer = flashOffer; }

    public long getOfferStartDate() { return offerStartDate; }

    public void setOfferStartDate(long offerStartDate) { this.offerStartDate = offerStartDate; }

    public long getOfferEndDate() { return offerEndDate; }

    public void setOfferEndDate(long offerEndDate) { this.offerEndDate = offerEndDate; }

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

    public double getNewPrice() {
        return newPrice;
    }

    public void setNewPrice(double newPrice) {
        this.newPrice = newPrice;
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

    public String getFilterCategories() {
        return filterCategories;
    }

    public void setFilterCategories(String filterCategories) {
        this.filterCategories = filterCategories;
    }

    public boolean isWithout_stock() {
        return without_stock;
    }

    public void setWithout_stock(boolean without_stock) {
        this.without_stock = without_stock;
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

    public ItemOpticsComplete getItemOpticsComplete() {
        return itemOpticsComplete;
    }

    public void setItemOpticsComplete(ItemOpticsComplete itemOpticsComplete) {
        this.itemOpticsComplete = itemOpticsComplete;
    }

    public String getFiltersOptical() {
        return filtersOptical;
    }

    public void setFiltersOptical(String filtersOptical) {
        this.filtersOptical = filtersOptical;
    }

    public String getCustomTag() {
        return customTag;
    }

    public void setCustomTag(String customTag) {
        this.customTag = customTag;
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

    

    public String getUuidItem() {
        return uuidItem;
    }

    public void setUuidItem(String uuidItem) {
        this.uuidItem = uuidItem;
    }

    public String getWarranty() {
        return warranty;
    }

    public void setWarranty(String warranty) {
        this.warranty = warranty;
    }

    public String getWarrantyTerms() {
        return warrantyTerms;
    }

    public void setWarrantyTerms(String warrantyTerms) {
        this.warrantyTerms = warrantyTerms;
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
    
    public String getCustomLabelForStockZero() {
        return customLabelForStockZero;
    }

    public void setCustomLabelForStockZero(String customLabelForStockZero) {
        this.customLabelForStockZero = customLabelForStockZero;
    }
    
    public Boolean getHasStock() {
        return hasStock;
    }

    public void setHasStock(Boolean hasStock) {
        this.hasStock = hasStock;
    }

    public List<Long> getStores_with_stock() {
        return stores_with_stock;
    }

    public void setStores_with_stock(List<Long> stores_with_stock) {
        this.stores_with_stock = stores_with_stock;

    }

    public String getGrayDescription() {
        return grayDescription;
    }

    public void setGrayDescription(String grayDescription) {
        this.grayDescription = grayDescription;
    }

    public boolean isRequirePrescriptionImage() {
        return requirePrescriptionImage;
    }

    public void setRequirePrescriptionImage(boolean requirePrescriptionImage) {
        this.requirePrescriptionImage = requirePrescriptionImage;
    }

    public boolean isAccess() {
        return access;
    }

    @Override
    public String toString() {
        return "ItemAlgolia{" +
                "mediaImageUrl='" + mediaImageUrl + '\'' +
                ", description='" + description + '\'' +
                ", fullPrice=" + fullPrice +
                ", mediaDescription='" + mediaDescription + '\'' +
                ", barcode='" + barcode + '\'' +
                ", brand='" + brand + '\'' +
                ", sales=" + sales +
                ", requirePrescription=" + requirePrescription +
                ", detailDescription='" + detailDescription + '\'' +
                ", offerPrice=" + offerPrice +
                ", offerDescription='" + offerDescription + '\'' +
                ", stock=" + stock +
                ", id='" + id + '\'' +
                ", offerText='" + offerText + '\'' +
                ", idStoreGroup=" + idStoreGroup +
                ", marca='" + marca + '\'' +
                ", objectID='" + objectID + '\'' +
                ", onlyOnline=" + onlyOnline +
                ", deliveryTime='" + deliveryTime + '\'' +
                ", highlight=" + highlight +
                ", generic=" + generic +
                ", largeDescription='" + largeDescription + '\'' +
                ", anywaySelling=" + anywaySelling +
                ", deliveryPrice=" + deliveryPrice +
                ", idClassification=" + idClassification +
                ", outstanding=" + outstanding +
                ", quantitySold=" + quantitySold +
                ", spaces=" + spaces +
                ", status='" + status + '\'' +
                ", taxRate=" + taxRate +
                ", listUrlImages=" + listUrlImages +
                ", measurePum=" + measurePum +
                ", labelPum='" + labelPum + '\'' +
                ", subscribeAndSave=" + subscribeAndSave +
                ", standardDuration='" + standardDuration + '\'' +
                ", expressWithSubscription=" + expressWithSubscription +
                ", categorie='" + categorie + '\'' +
                ", id_highlights=" + id_highlights +
                ", id_suggested=" + id_suggested +
                ", barcodeList=" + barcodeList +
                ", color=" + color +
                ", colors=" + colors +
                ", filterType='" + filterType + '\'' +
                ", filter=" + filter +
                ", filtersLoreal=" + filtersLoreal +
                ", departments=" + departments +
                ", subCategory='" + subCategory + '\'' +
                ", supplier='" + supplier + '\'' +
                ", filters=" + filters +
                ", handleQuantity=" + handleQuantity +
                ", item=" + item +
                ", quantityRequested=" + quantityRequested +
                ", discount=" + discount +
                ", price=" + price +
                ", calculatedPrice=" + calculatedPrice +
                ", quantityBonus=" + quantityBonus +
                ", access=" + access +
                ", itemDeliveryPrice=" + itemDeliveryPrice +
                ", totalStock=" + totalStock +
                ", newPrice=" + newPrice +
                ", textoSEO='" + textoSEO + '\'' +
                ", itemUrl='" + itemUrl + '\'' +
                ", outofstore=" + outofstore +
                ", isFlashOffer=" + isFlashOffer +
                ", offerStartDate=" + offerStartDate +
                ", offerEndDate=" + offerEndDate +
                ", primePrice=" + primePrice +
                ", primeTextDiscount='" + primeTextDiscount + '\'' +
                ", primeDescription='" + primeDescription + '\'' +
                ", rms_class='" + rms_class + '\'' +
                ", rms_deparment='" + rms_deparment + '\'' +
                ", rms_group='" + rms_group + '\'' +
                ", rms_subclass='" + rms_subclass + '\'' +
                ", without_stock=" + without_stock +
                ", isTalonDiscount=" + isTalonDiscount +
                ", itemOpticsComplete=" + itemOpticsComplete +
                ",customTag=" + customTag + '\'' +
                '}';
    }

    public String toStringJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
