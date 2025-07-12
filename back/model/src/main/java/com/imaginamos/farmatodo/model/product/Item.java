package com.imaginamos.farmatodo.model.product;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.*;
import com.imaginamos.farmatodo.model.categories.Classification;
import com.imaginamos.farmatodo.model.optics.ItemOpticsComplete;
import com.imaginamos.farmatodo.model.provider.ProviderOrder;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Created by mileniopc on 12/6/16.
 * Property of Imaginamos.
 */

@Entity
public class Item implements Comparable<Item>, com.imaginamos.farmatodo.model.intefaces.Item  {

  private static final Logger LOG = Logger.getLogger(Item.class.getName());

  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Id
  private String itemId;
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Parent
  private Ref<ItemGroup> itemGroupRef;
  private boolean anywaySelling;
  private String barcode;
  private String brand;

  private String offerText;
  private String offerDescription;
  @Index
  private Double fullPrice;
  private String grayDescription;
  @Index
  private boolean highlight;
  @Index
  private long id;

  private String directionItem;
  private boolean isGeneric;
  private String mediaDescription;
  private String largeDescription;
  private String mediaImageUrl;
  private Double offerPrice;

  @IgnoreSave
  private List<ColorPicker> colors;
  @IgnoreSave
  private ColorPicker color;
  @IgnoreSave
  private Capacity filter;
  @IgnoreSave
  private String filterType;
  @IgnoreSave
  private List<Capacity> filtersLoreal;
  private String filtersOptical;

  @Index
  private boolean outstanding = false;
  private String requirePrescription;
  @Index
  private long sales;
  private long spaces;
  private String status;
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Index
  private int idStoreGroup;
  private int taxRate;
  @Index
  private int totalStock;
  @IgnoreSave
  private List<Classification> categories;
  @IgnoreSave
  private List<CrossSales> crossSales;
  @IgnoreSave
  private List<Substitutes> substitutes;
  @IgnoreSave
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private int item;
  @IgnoreSave
  private List<Stock> stock;
  @IgnoreSave
  private int quantitySold;
  @IgnoreSave
  private String firstDescription;
  @IgnoreSave
  private String secondDescription;
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @IgnoreSave
  private boolean starProduct;
  @Index
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private boolean toIndexInAlgolia;
  @Index
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private boolean toDelete;
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @IgnoreSave
  private String idItemWebSafe;
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Index
  private List<Integer> filterList;
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Index
  private List<Long> subCategories;
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private List<StoreInformation> storeInformation;
  @IgnoreSave
  private int idClassification;
  @Index
  private Boolean isCoupon;
  @Index
  private Boolean subscribeAndSave;
  private String standardDuration;
  private boolean expressWithSubscription;

  @IgnoreSave
  private Long posGroup = 100L;

  // new images

  private List<String> listUrlImages;

  private String pum;

  @IgnoreSave
  private String categorie;

  @IgnoreSave
  private String marca;

  @IgnoreSave
  private List<String> departments;

  @IgnoreSave
  private String subCategory;

  @IgnoreSave
  private String supplier;

  @IgnoreSave
  private Integer quantityRequested;

  // TODO Campos sugeridos proveedores externos
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Index
  private Key<ProviderOrder> provider;
  private int deliveryPrice;  
  @IgnoreSave
  private ItemSeo seo;

  @IgnoreSave
  private String textSEO;

  @IgnoreSave
  private boolean onlyOnline;

  @IgnoreSave
  private String deliveryTime;

  @IgnoreSave
  private List<ItemFilter> filters;

  private Boolean handleQuantity;

  @IgnoreSave
  private Integer globalStock;

  @IgnoreSave
  private String itemUrl;

  @IgnoreSave
  private Boolean itemOfferPopUp;

  @IgnoreSave
  private Boolean outofstore;

  @IgnoreSave
  private boolean isFlashOffer;

  @IgnoreSave
  private long offerStartDate;

  @IgnoreSave
  private long offerEndDate;

  @IgnoreSave
  private double primePrice;

  @IgnoreSave
  private String primeTextDiscount;

  @IgnoreSave
  private String primeDescription;


  //RMS Classes
  @IgnoreSave
  private String rms_class;
  @IgnoreSave
  private String rms_deparment;
  @IgnoreSave
  private String rms_group;
  @IgnoreSave
  private String rms_subclass;

  @IgnoreSave
  private boolean without_stock;

  @IgnoreSave
  private Integer quantityRequest;
  @IgnoreSave
  private String filterCategories;

  //parameters of optics
  private ItemOpticsComplete itemOpticsComplete;

  private String customTag;

  @IgnoreSave
  private String url;
  @IgnoreSave
  private boolean requirePrescriptionMedical;

  //data Marketplace
  @IgnoreSave
  private List<ItemMarketplaceVariant> variants;
  @IgnoreSave
  private List<ItemMarketplaceSellerAddress> sellerAddresses;
  @IgnoreSave
  private ItemMarketplaceDimensions dimensions;

  private String uuidItem;
  @IgnoreSave
  private String warranty;
  @IgnoreSave
  private String warrantyTerms;

  @IgnoreSave
  private String customLabelForStockZero;

  private Boolean hasStock;

  @IgnoreSave
  private List<Long> storesWithStock;

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

  public String getItemId() {
    return itemId;
  }

  public void setItemId(String itemId) {
    this.itemId = itemId;
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

  public boolean isGeneric() {
    return isGeneric;
  }

  public void setGeneric(boolean generic) {
    isGeneric = generic;
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

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public boolean getIsGeneric() {
    return isGeneric;
  }

  public void setIsGeneric(boolean generic) {
    isGeneric = generic;
  }

  public String getMediaDescription() {
    return mediaDescription;
  }

  public void setMediaDescription(String mediaDescription) {
    this.mediaDescription = mediaDescription;
  }

  public String getLargeDescription() {
    return largeDescription;
  }

  public void setLargeDescription(String largeDescription) {
    this.largeDescription = largeDescription;
  }

  public String getMediaImageUrl() {
    return mediaImageUrl;
  }

  public void setMediaImageUrl(String mediaImageUrl) {
    this.mediaImageUrl = mediaImageUrl;
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

  public List<Classification> getCategories() {
    return categories;
  }

  public void setCategories(List<Classification> categories) {
    this.categories = categories;
  }

  public List<ColorPicker> getColors(){
    return colors;
  }

  public void setColors(List<ColorPicker> colors){ this.colors = colors;}

  public void setColor(ColorPicker color){this.color = color;}

  public ColorPicker getColor(){
    return color;
  }

  public void setFilter(Capacity filter){this.filter = filter;}

  public Capacity getFilter(){
    return filter;
  }

  public List<CrossSales> getCrossSales() {

    return crossSales;
  }

  public List<Capacity> getFiltersLoreal() {
    return filtersLoreal;
  }

  public void setFiltersLoreal(List<Capacity> filtersLoreal) {
    this.filtersLoreal = filtersLoreal;
  }

  public String getFiltersOptical() {
    return filtersOptical;
  }

  public void setFiltersOptical(String filtersOptical) {
    this.filtersOptical = filtersOptical;
  }

  public void setCrossSales(List<CrossSales> crossSales) {
    this.crossSales = crossSales;
  }

  public List<Substitutes> getSubstitutes() {
    return substitutes;
  }

  public void setSubstitutes(List<Substitutes> substitutes) {
    this.substitutes = substitutes;
  }

  public int getItem() {
    return item;
  }

  public void setItem(int item) {
    this.item = item;
  }

  public List<Stock> getStock() {
    return stock;
  }

  public void setStock(List<Stock> stock) {
    this.stock = stock;
  }

  public int getQuantitySold() {
    return quantitySold;
  }

  public void setQuantitySold(int quantitySold) {
    this.quantitySold = quantitySold;
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

  public String getFirstDescription() {
    return firstDescription;
  }

  public void setFirstDescription(String firstDescription) {
    this.firstDescription = firstDescription;
  }

  public String getSecondDescription() {
    return secondDescription;
  }

  public void setSecondDescription(String secondDescription) {
    this.secondDescription = secondDescription;
  }

  public boolean isStarProduct() {
    return starProduct;
  }

  public void setStarProduct(boolean starProduct) {
    this.starProduct = starProduct;
  }

  public boolean isToIndexInAlgolia() {
    return toIndexInAlgolia;
  }

  public void setToIndexInAlgolia(boolean toIndexInAlgolia) {
    this.toIndexInAlgolia = toIndexInAlgolia;
  }

  public String getIdItemWebSafe() {
    return idItemWebSafe;
  }

  public void setIdItemWebSafe(String idItemWebSafe) {
    this.idItemWebSafe = idItemWebSafe;
  }

  public List<Integer> getFilterList() {
    return filterList;
  }

  public void setFilterList(List<Integer> filterList) {
    this.filterList = filterList;
  }

  public List<Long> getSubCategories() {
    return subCategories;
  }

  public void setSubCategories(List<Long> subCategories) {
    this.subCategories = subCategories;
  }

  public List<StoreInformation> getStoreInformation() {
    return storeInformation;
  }

  public void setStoreInformation(List<StoreInformation> storeInformation) {
    this.storeInformation = storeInformation;
  }

  public Ref<ItemGroup> getItemGroupRef() {
    return itemGroupRef;
  }

  public void setItemGroupRef(Ref<ItemGroup> itemGroupRef) {
    this.itemGroupRef = itemGroupRef;
  }

  public int getIdClassification() {
    return idClassification;
  }

  public void setIdClassification(int idClassification) {
    this.idClassification = idClassification;
  }

  public boolean isToDelete() {
    return toDelete;
  }

  public void setToDelete(boolean toDelete) {
    this.toDelete = toDelete;
  }

  public Boolean getCoupon() {
    return isCoupon;
  }

  public void setCoupon(Boolean coupon) {
    isCoupon = coupon;
  }

  public Key<ProviderOrder> getProvider() { return provider; }

  public void setProvider(Key<ProviderOrder> provider) { this.provider = provider; }

  public int getDeliveryPrice() { return deliveryPrice; }

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

  public void setDeliveryPrice(int deliveryPrice) { this.deliveryPrice = deliveryPrice; }

  public String getCategorie() { return categorie; }

  public void setCategorie(String categorie) { this.categorie = categorie; }

  public String getMarca() { return marca; }

  public void setMarca(String marca) { this.marca = marca; }

  public List<String> getDepartments() { return departments; }

  public void setDepartments(List<String> departments) { this.departments = departments; }

  public String getSubCategory() { return subCategory; }

  public void setSubCategory(String subCategory) { this.subCategory = subCategory; }

  public List<ItemFilter> getFilters() { return filters; }

  public void setFilters(List<ItemFilter> filters) { this.filters = filters; }

  public Boolean getHandleQuantity() { return handleQuantity; }

  public void setHandleQuantity(Boolean handleQuantity) { this.handleQuantity = handleQuantity; }

  public Integer getGlobalStock() { return globalStock; }

  public void setGlobalStock(Integer globalStock) {
    this.globalStock = globalStock;
  }

  public Boolean getItemOfferPopUp() {
    return itemOfferPopUp;
  }

  public void setItemOfferPopUp(Boolean itemOfferPopUp) {
    this.itemOfferPopUp = itemOfferPopUp;
  }


  public Integer getQuantityRequested() {
    return quantityRequested;
  }

  public void setQuantityRequested(Integer quantityRequested) {
    this.quantityRequested = quantityRequested;
  }

  public ItemSeo getSeo() {
    if(Objects.nonNull(this.offerPrice) && this.offerPrice > 0) {
      return new ItemSeo(this.mediaDescription, this.mediaImageUrl, this.grayDescription, Long.toString(this.id),
              new ItemSeo.Offers(Double.toString(this.offerPrice), Double.toString(Objects.nonNull(this.fullPrice) ? this.fullPrice : 0D),
                      new Date().toString(),
                      Objects.nonNull(this.totalStock) && this.totalStock > 1 ? ItemSeo.IN_STOCK : ItemSeo.OUT_OF_STOCK));
    }else{
      return new ItemSeo(this.mediaDescription, this.mediaImageUrl, this.grayDescription, Long.toString(this.id),
              new ItemSeo.Offers(Double.toString(Objects.nonNull(this.fullPrice) ? this.fullPrice : 0D),
                      "",
                      Objects.nonNull(this.totalStock) && this.totalStock > 1 ? ItemSeo.IN_STOCK : ItemSeo.OUT_OF_STOCK));
    }
  }

  public String getTextSEO() {
    return textSEO;
  }

  public void setTextSEO(String textSEO) {
    this.textSEO = textSEO;
  }

  @Override
  public int compareTo(Item o) {
    if (sales < o.sales) {
      return -1;
    }
    if (sales > o.sales) {
      return 1;
    }
    return 0;
  }

  public static Comparator<Item> StockComparator
      = new Comparator<Item>() {

    public int compare(Item fruit1, Item fruit2) {

      int stock1 = fruit1.getTotalStock();
      int stock2 = fruit2.getTotalStock();

      if (stock1 < stock2) {
        return 1;
      }
      if (stock1 > stock2) {
        return -1;
      }
      return 0;
    }

  };

  public String getPum() {
    return pum;
  }

  public Long getPosGroup() {
    return posGroup;
  }

  public void setPosGroup(Long posGroup) {
    this.posGroup = posGroup;
  }

  public void setPum(String pum) {
    this.pum = pum;
  }

  public boolean getExpressWithSubscription() {
    return expressWithSubscription;
  }

  public void setExpressWithSubscription(boolean expressWithSubscription) {
    this.expressWithSubscription = expressWithSubscription;
  }

  public String getFilterType() { return filterType; }

  public void setFilterType(String filterType) { this.filterType = filterType; }

  public String getSupplier() { return supplier; }

  public void setSupplier(String supplier) { this.supplier = supplier; }

  public String getItemUrl() {
    return itemUrl;
  }

  public void setItemUrl(String itemUrl) {
    this.itemUrl = itemUrl;
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

  public double getPrimePrice() {
    return primePrice;
  }

  public void setPrimePrice(double primePrice) {
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

  public boolean isWithout_stock() {
    return without_stock;
  }

  public void setWithout_stock(boolean without_stock) {
    this.without_stock = without_stock;
  }

  public ItemOpticsComplete getItemOpticsComplete() {
    return itemOpticsComplete;
  }

  public void setItemOpticsComplete(ItemOpticsComplete itemOpticsComplete) {
    this.itemOpticsComplete = itemOpticsComplete;
  }

  public Integer getQuantityRequest() {
    return quantityRequest;
  }

  public void setQuantityRequest(Integer quantityRequest) {
    this.quantityRequest = quantityRequest;
  }

  public String getDirectionItem() {
    return directionItem;
  }

  public void setDirectionItem(String directionItem) {
    this.directionItem = directionItem;
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

  public String getCustomTag() {
    return customTag;
  }

  public void setCustomTag(String customTag) {
    this.customTag = customTag;
  }

  public boolean isRequirePrescriptionMedical() {
    return requirePrescriptionMedical;
  }

  public void setRequirePrescriptionMedical(boolean requirePrescriptionMedical) {
    this.requirePrescriptionMedical = requirePrescriptionMedical;
  }
  
  public void setCustomLabelForStockZero(String customLabelForStockZero) {
    this.customLabelForStockZero = customLabelForStockZero;
  }
  
  public String getCustomLabelForStockZero() {
    return customLabelForStockZero;
  }

  public Boolean getHasStock() {
      return hasStock;
  }

  public void setHasStock(Boolean hasStock) {
      this.hasStock = hasStock;
  }

  public List<Long> getStoresWithStock() {
      return storesWithStock;
  }

  public void setStoresWithStock(List<Long> storesWithStock) {
      this.storesWithStock = storesWithStock;
  }

  public String toStringJson() {
    return new Gson().toJson(this);
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

  @Override
  public String toString() {
    return "Item{" +
            "itemId='" + itemId + '\'' +
            ", itemGroupRef=" + itemGroupRef +
            ", anywaySelling=" + anywaySelling +
            ", barcode='" + barcode + '\'' +
            ", brand='" + brand + '\'' +
            ", offerText='" + offerText + '\'' +
            ", offerDescription='" + offerDescription + '\'' +
            ", fullPrice=" + fullPrice +
            ", grayDescription='" + grayDescription + '\'' +
            ", highlight=" + highlight +
            ", id=" + id +
            ", directionItem='" + directionItem + '\'' +
            ", isGeneric=" + isGeneric +
            ", mediaDescription='" + mediaDescription + '\'' +
            ", largeDescription='" + largeDescription + '\'' +
            ", mediaImageUrl='" + mediaImageUrl + '\'' +
            ", offerPrice=" + offerPrice +
            ", colors=" + colors +
            ", color=" + color +
            ", filter=" + filter +
            ", filterType='" + filterType + '\'' +
            ", filtersLoreal=" + filtersLoreal +
            ", filtersOptical='" + filtersOptical + '\'' +
            ", outstanding=" + outstanding +
            ", requirePrescription='" + requirePrescription + '\'' +
            ", sales=" + sales +
            ", spaces=" + spaces +
            ", status='" + status + '\'' +
            ", idStoreGroup=" + idStoreGroup +
            ", taxRate=" + taxRate +
            ", totalStock=" + totalStock +
            ", categories=" + categories +
            ", crossSales=" + crossSales +
            ", substitutes=" + substitutes +
            ", item=" + item +
            ", stock=" + stock +
            ", quantitySold=" + quantitySold +
            ", firstDescription='" + firstDescription + '\'' +
            ", secondDescription='" + secondDescription + '\'' +
            ", starProduct=" + starProduct +
            ", toIndexInAlgolia=" + toIndexInAlgolia +
            ", toDelete=" + toDelete +
            ", idItemWebSafe='" + idItemWebSafe + '\'' +
            ", filterList=" + filterList +
            ", subCategories=" + subCategories +
            ", storeInformation=" + storeInformation +
            ", idClassification=" + idClassification +
            ", isCoupon=" + isCoupon +
            ", subscribeAndSave=" + subscribeAndSave +
            ", standardDuration='" + standardDuration + '\'' +
            ", expressWithSubscription=" + expressWithSubscription +
            ", posGroup=" + posGroup +
            ", listUrlImages=" + listUrlImages +
            ", pum='" + pum + '\'' +
            ", categorie='" + categorie + '\'' +
            ", marca='" + marca + '\'' +
            ", departments=" + departments +
            ", subCategory='" + subCategory + '\'' +
            ", supplier='" + supplier + '\'' +
            ", provider=" + provider +
            ", deliveryPrice=" + deliveryPrice +
            ", seo=" + seo +
            ", textSEO='" + textSEO + '\'' +
            ", onlyOnline=" + onlyOnline +
            ", deliveryTime='" + deliveryTime + '\'' +
            ", filters=" + filters +
            ", handleQuantity=" + handleQuantity +
            ", globalStock=" + globalStock +
            ", itemUrl='" + itemUrl + '\'' +
            ", itemOfferPopUp=" + itemOfferPopUp +
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
            ", quantityRequest=" + quantityRequest +
            ", filterCategories='" + filterCategories + '\'' +
            ", itemOpticsComplete=" + itemOpticsComplete + '\'' +
            ",customTag=" + customTag + '\'' +
            ", url='" + url + '\'' +
            ", uuidItem='" + uuidItem + '\'' +
            '}';
  }
}
