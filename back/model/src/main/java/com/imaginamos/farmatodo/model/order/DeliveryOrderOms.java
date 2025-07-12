package com.imaginamos.farmatodo.model.order;

import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.appengine.repackaged.com.google.gson.GsonBuilder;
import com.imaginamos.farmatodo.model.algolia.DeliveryTimeLabelTemplate;
import com.imaginamos.farmatodo.model.payment.PaymentType;
import com.imaginamos.farmatodo.model.talonone.Coupon;
import com.imaginamos.farmatodo.model.talonone.CouponAutomaticTalon;
import com.imaginamos.farmatodo.model.talonone.DeductDiscount;

import java.util.List;
import java.util.Map;

public class DeliveryOrderOms {
    private static final Gson GSON = new GsonBuilder().create();

    private String source;
    private String customerName;
    private String customerPhone;
    private Long idAddress;
    private String address;
    private String addressDetails;
    private PaymentType paymentType;
    private double weight;
    private double lowerRangeWeight;
    private double topRangeWeight;
    private double subTotalPrice;
    private double offerPrice;
    private double deliveryPrice;
    private double registeredOffer;
    private int customerReview;
    private String createDate;
    private double totalPrice;
    private int idFarmatodo;
    private int minutes;
    private String idStoreGroup;
    private int paymentCardId;
    private List<DeliveryOrderItemOms> itemList;
    private long idOrder;
    private String lastStatus;
    private String deliveryType;
    private Long createdDate;
    private String pickingDate;
    private Double tipPrice;

    // Proveedores externos
    private double providerDeliveryPrice;
    private double totalDelivery;
    private int quantityFarmatodo;
    private int quantityProviders;
    private int totalQuantity;
    private int containsItemQuantity;
    private int discountRate;


    //Dynamic deliveryLabel
    private String deliveryLabel;
    private DeliveryTimeLabelTemplate deliveryTimeLabel;

    private List<DeliveryOrderProviderOms> providerList;
    private String qrCode;
    private double savingPrime;
    private boolean isTalonOneDiscount = false;
    private boolean talonOneItemFree = false;
    private boolean primeDiscountFlag;
    private String primeDeliveryValue;
    private Long orderPrimeId = 0L;
    private Coupon coupon;
    private DeductDiscount deductDiscount;
    private List<CouponAutomaticTalon> couponAutomaticTalonList;
    private String deliveryHome;
    private Double usedCredits;
    private Long farmaCredits;
    private Map<String, Object> talonOneData;
    private Boolean isActive;
    @Override
    public String toString() {
        return GSON.toJson(this);
    }

    // Getters and Setters

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public Long getIdAddress() {
        return idAddress;
    }

    public void setIdAddress(Long idAddress) {
        this.idAddress = idAddress;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddressDetails() {
        return addressDetails;
    }

    public void setAddressDetails(String addressDetails) {
        this.addressDetails = addressDetails;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getLowerRangeWeight() {
        return lowerRangeWeight;
    }

    public void setLowerRangeWeight(double lowerRangeWeight) {
        this.lowerRangeWeight = lowerRangeWeight;
    }

    public double getTopRangeWeight() {
        return topRangeWeight;
    }

    public void setTopRangeWeight(double topRangeWeight) {
        this.topRangeWeight = topRangeWeight;
    }

    public double getSubTotalPrice() {
        return subTotalPrice;
    }

    public void setSubTotalPrice(double subTotalPrice) {
        this.subTotalPrice = subTotalPrice;
    }

    public double getOfferPrice() {
        return offerPrice;
    }

    public void setOfferPrice(double offerPrice) {
        this.offerPrice = offerPrice;
    }

    public double getDeliveryPrice() {
        return deliveryPrice;
    }

    public void setDeliveryPrice(double deliveryPrice) {
        this.deliveryPrice = deliveryPrice;
    }

    public double getRegisteredOffer() {
        return registeredOffer;
    }

    public void setRegisteredOffer(double registeredOffer) {
        this.registeredOffer = registeredOffer;
    }

    public int getCustomerReview() {
        return customerReview;
    }

    public void setCustomerReview(int customerReview) {
        this.customerReview = customerReview;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public int getIdFarmatodo() {
        return idFarmatodo;
    }

    public void setIdFarmatodo(int idFarmatodo) {
        this.idFarmatodo = idFarmatodo;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public String getIdStoreGroup() {
        return idStoreGroup;
    }

    public void setIdStoreGroup(String idStoreGroup) {
        this.idStoreGroup = idStoreGroup;
    }

    public int getPaymentCardId() {
        return paymentCardId;
    }

    public void setPaymentCardId(int paymentCardId) {
        this.paymentCardId = paymentCardId;
    }

    public List<DeliveryOrderItemOms> getItemList() {
        return itemList;
    }

    public void setItemList(List<DeliveryOrderItemOms> itemList) {
        this.itemList = itemList;
    }

    public long getIdOrder() {
        return idOrder;
    }

    public void setIdOrder(long idOrder) {
        this.idOrder = idOrder;
    }

    public String getLastStatus() {
        return lastStatus;
    }

    public void setLastStatus(String lastStatus) {
        this.lastStatus = lastStatus;
    }

    public String getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(String deliveryType) {
        this.deliveryType = deliveryType;
    }

    public Long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Long createdDate) {
        this.createdDate = createdDate;
    }

    public String getPickingDate() {
        return pickingDate;
    }

    public void setPickingDate(String pickingDate) {
        this.pickingDate = pickingDate;
    }

    public Double getTipPrice() {
        return tipPrice;
    }

    public void setTipPrice(Double tipPrice) {
        this.tipPrice = tipPrice;
    }

    public double getProviderDeliveryPrice() {
        return providerDeliveryPrice;
    }

    public void setProviderDeliveryPrice(double providerDeliveryPrice) {
        this.providerDeliveryPrice = providerDeliveryPrice;
    }

    public double getTotalDelivery() {
        return totalDelivery;
    }

    public void setTotalDelivery(double totalDelivery) {
        this.totalDelivery = totalDelivery;
    }

    public int getQuantityFarmatodo() {
        return quantityFarmatodo;
    }

    public void setQuantityFarmatodo(int quantityFarmatodo) {
        this.quantityFarmatodo = quantityFarmatodo;
    }

    public int getQuantityProviders() {
        return quantityProviders;
    }

    public void setQuantityProviders(int quantityProviders) {
        this.quantityProviders = quantityProviders;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public int getContainsItemQuantity() {
        return containsItemQuantity;
    }

    public void setContainsItemQuantity(int containsItemQuantity) {
        this.containsItemQuantity = containsItemQuantity;
    }

    public int getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(int discountRate) {
        this.discountRate = discountRate;
    }

    public String getDeliveryLabel() {
        return deliveryLabel;
    }

    public void setDeliveryLabel(String deliveryLabel) {
        this.deliveryLabel = deliveryLabel;
    }

    public DeliveryTimeLabelTemplate getDeliveryTimeLabel() {
        return deliveryTimeLabel;
    }

    public void setDeliveryTimeLabel(DeliveryTimeLabelTemplate deliveryTimeLabel) {
        this.deliveryTimeLabel = deliveryTimeLabel;
    }

    public List<DeliveryOrderProviderOms> getProviderList() {
        return providerList;
    }

    public void setProviderList(List<DeliveryOrderProviderOms> providerList) {
        this.providerList = providerList;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public double getSavingPrime() {
        return savingPrime;
    }

    public void setSavingPrime(double savingPrime) {
        this.savingPrime = savingPrime;
    }

    public boolean isTalonOneDiscount() {
        return isTalonOneDiscount;
    }

    public void setTalonOneDiscount(boolean talonOneDiscount) {
        isTalonOneDiscount = talonOneDiscount;
    }

    public boolean isTalonOneItemFree() {
        return talonOneItemFree;
    }

    public void setTalonOneItemFree(boolean talonOneItemFree) {
        this.talonOneItemFree = talonOneItemFree;
    }

    public boolean isPrimeDiscountFlag() {
        return primeDiscountFlag;
    }

    public void setPrimeDiscountFlag(boolean primeDiscountFlag) {
        this.primeDiscountFlag = primeDiscountFlag;
    }

    public String getPrimeDeliveryValue() {
        return primeDeliveryValue;
    }

    public void setPrimeDeliveryValue(String primeDeliveryValue) {
        this.primeDeliveryValue = primeDeliveryValue;
    }

    public Long getOrderPrimeId() {
        return orderPrimeId;
    }

    public void setOrderPrimeId(Long orderPrimeId) {
        this.orderPrimeId = orderPrimeId;
    }

    public Coupon getCoupon() {
        return coupon;
    }

    public void setCoupon(Coupon coupon) {
        this.coupon = coupon;
    }

    public DeductDiscount getDeductDiscount() {
        return deductDiscount;
    }

    public void setDeductDiscount(DeductDiscount deductDiscount) {
        this.deductDiscount = deductDiscount;
    }

    public List<CouponAutomaticTalon> getCouponAutomaticTalonList() {
        return couponAutomaticTalonList;
    }

    public void setCouponAutomaticTalonList(List<CouponAutomaticTalon> couponAutomaticTalonList) {
        this.couponAutomaticTalonList = couponAutomaticTalonList;
    }

    public String getDeliveryHome() {
        return deliveryHome;
    }

    public void setDeliveryHome(String deliveryHome) {
        this.deliveryHome = deliveryHome;
    }

    public Double getUsedCredits() {
        return usedCredits;
    }

    public void setUsedCredits(Double usedCredits) {
        this.usedCredits = usedCredits;
    }

    public Long getFarmaCredits() {
        return farmaCredits;
    }

    public void setFarmaCredits(Long farmaCredits) {
        this.farmaCredits = farmaCredits;
    }

    public Map<String, Object> getTalonOneData() {
        return talonOneData;
    }

    public void setTalonOneData(Map<String, Object> talonOneData) {
        this.talonOneData = talonOneData;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }
}
