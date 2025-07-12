package com.imaginamos.farmatodo.model.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.*;
import com.imaginamos.farmatodo.model.algolia.DeliveryTimeLabelTemplate;
import com.imaginamos.farmatodo.model.algolia.tips.Tip;
import com.imaginamos.farmatodo.model.callcenter.PhoneCall;
import com.imaginamos.farmatodo.model.customer.Customer;
import com.imaginamos.farmatodo.model.location.City;
import com.imaginamos.farmatodo.model.payment.PaymentType;
import com.imaginamos.farmatodo.model.product.Item;
import com.imaginamos.farmatodo.model.provider.ShippingCostItemsMarkeplaceRequest;
import com.imaginamos.farmatodo.model.provider.SupplierShippingCostMarketplace;
import com.imaginamos.farmatodo.model.talonone.Coupon;
import com.imaginamos.farmatodo.model.talonone.CouponAutomaticTalon;
import com.imaginamos.farmatodo.model.talonone.DeductDiscount;
import com.imaginamos.farmatodo.model.util.DeliveryType;
import main.java.com.imaginamos.farmatodo.model.order.ShoppingCartCourierCostResp;

import java.math.BigInteger;
import java.util.*;


/**
 * Created by mileniopc on 10/26/16.
 * Property of Imaginamos.
 */

public class DeliveryOrderV2 {
    
    
    private String idDeliveryOrder;
    




    private String securityToken;
    private String source; //Pendiente
    
    private int customerType; //Pendiente
    private String customerDocumentNumber;
    private String customerName;
    private String customerPhone;
    
 
    private int currentStatus;
    private String closerStoreSelectMode;
    private int idAddress;
    private String address;
    private String addressDetails;
    private PaymentType paymentType;
    private String paymentDetails;
    private double weight;

    private double lowerRangeWeight;

    private double topRangeWeight;
    private double subTotalPrice;
    private double offerPrice;
    private double deliveryPrice;
    private double registeredOffer;
    private String orderDetails;
    private String courierOrderNumber;
    private String courierResponsibleImageURL;
    private CancellationReason cancellationReason;
    private String cancellationComments;

    private int customerReview;
    private String customerReviewComments;
 
    private Date createDate;
    private City city;
    private double totalPrice;

    private String idDeliveryOrderWebSafe;
 
    private int idFarmatodo;
    private int minutes;
    private String idStoreGroup;
    private int paymentCardId;

    private List<DeliveryOrderItem> itemList;

    private List<Item> highlightedItems;
 
    private long idOrder;
 
    private String lastStatus;
    private Integer quotas;
 
    private DeliveryType deliveryType;
 
    private Boolean isActive;
 
    private BigInteger createdDate;
    private String push;
    private Date pickingDate;

    //  Customer call center

    private Long customerIdCallCenter;


    private Tip tip;

    private Double tipPrice;

    // Proveedores externos

    private double providerDeliveryPrice;
    private double totalDelivery;
    private int quantityFarmatodo;
    private int quantityProviders;
    private int totalQuantity;

    private int containsItemQuantity;

    //Suscribete y ahorra

    private float discountRate;

    //Dynamic deliveryLabel

    private String deliveryLabel;


    private DeliveryTimeLabelTemplate deliveryTimeLabel;

    private List<DeliveryOrderProvider> providerList;

    private Boolean isHighDemand;

    private String qrCode;

    private String creditCardToken;

    private String typePersonPSE;

    private String ipAddress;

    private Long financialInstitutions;

    private Identification identification;

    private Long reassignmentOrder;

    private SelfCheckout selfCheckout;

    private double savingPrime;

    private boolean isTalonOneDiscount = false;
    private boolean talonOneItemFree = false;

    private boolean primeDiscountFlag;

    private String primeDeliveryValue;

    private Long orderPrimeId = 0L;

    private String urlPrescription;

    private Coupon coupon;
    private DeductDiscount deductDiscount;
    private List<CouponAutomaticTalon> couponAutomaticTalonList;
    private String deliveryHome;

    private List<SupplierShippingCostMarketplace> supplierShippingCost;

    private Double marketplaceShippingCostTotal;

    private ShoppingCartCourierCostResp shoppingCartCourierCost;


    private ShippingCostItemsMarkeplaceRequest shippingCostItemsMarkeplaceRequest;


    public ShippingCostItemsMarkeplaceRequest getShippingCostItemsMarkeplaceRequest() {
        return shippingCostItemsMarkeplaceRequest;
    }

    public void setShippingCostItemsMarkeplaceRequest(ShippingCostItemsMarkeplaceRequest shippingCostItemsMarkeplaceRequest) {
        this.shippingCostItemsMarkeplaceRequest = shippingCostItemsMarkeplaceRequest;
    }


    private Double usedCredits;

    private Long farmaCredits;

    private Map<String, Object> talonOneData;

    /**
     * PopUp de ruta optima
     */

    private String idOptimalRoute;

    private OptionSelectPopUpEnum optionSelectedPopUp;

    public String getIdOptimalRoute() {
        return idOptimalRoute;
    }

    public void setIdOptimalRoute(String idOptimalRoute) {
        this.idOptimalRoute = idOptimalRoute;
    }

    public OptionSelectPopUpEnum getOptionSelectedPopUp() {
        return optionSelectedPopUp;
    }

    public void setOptionSelectedPopUp(OptionSelectPopUpEnum optionSelectedPopUp) {
        this.optionSelectedPopUp = optionSelectedPopUp;
    }

    public Map<String, Object> getTalonOneData() {
        return talonOneData;
    }

    public void setTalonOneData(Map<String, Object> talonOneData) {
        this.talonOneData = talonOneData;
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

    public String getTypePersonPSE() {
        return typePersonPSE;
    }

    public void setTypePersonPSE(String typePersonPSE) {
        this.typePersonPSE = typePersonPSE;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Long getFinancialInstitutions() {
        return financialInstitutions;
    }

    public void setFinancialInstitutions(Long financialInstitutions) {
        this.financialInstitutions = financialInstitutions;
    }

    public Identification getIdentification() {
        return identification;
    }

    public void setIdentification(Identification identification) {
        this.identification = identification;
    }

    public String getCreditCardToken() {
        return creditCardToken;
    }

    public void setCreditCardToken(String creditCardToken) {
        this.creditCardToken = creditCardToken;
    }


    private String messageWhenHasItemsNotBilled;

    public Date getPickingDate() {
        return pickingDate;
    }

    public void setPickingDate(Date pickingDate) {
        this.pickingDate = pickingDate;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public int getIdFarmatodo() {
        return idFarmatodo;
    }

    public void setIdFarmatodo(int idFarmatodo) {
        this.idFarmatodo = idFarmatodo;
    }

    public String getIdDeliveryOrder() {
        return idDeliveryOrder;
    }

    public void setIdDeliveryOrder(String idDeliveryOrder) {
        this.idDeliveryOrder = idDeliveryOrder;
    }


    public String getSecurityToken() {
        return securityToken;
    }

    public void setSecurityToken(String securityToken) {
        this.securityToken = securityToken;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getCustomerType() {
        return customerType;
    }

    public void setCustomerType(int customerType) {
        this.customerType = customerType;
    }

    public String getCustomerDocumentNumber() {
        return customerDocumentNumber;
    }

    public void setCustomerDocumentNumber(String customerDocumentNumber) {
        this.customerDocumentNumber = customerDocumentNumber;
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

    public int getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(int currentStatus) {
        this.currentStatus = currentStatus;
    }

    public String getCloserStoreSelectMode() {
        return closerStoreSelectMode;
    }

    public void setCloserStoreSelectMode(String closerStoreSelectMode) {
        this.closerStoreSelectMode = closerStoreSelectMode;
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

    public String getPaymentDetails() {
        return paymentDetails;
    }

    public void setPaymentDetails(String paymentDetails) {
        this.paymentDetails = paymentDetails;
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

    public String getOrderDetails() {
        return orderDetails;
    }

    public void setOrderDetails(String orderDetails) {
        this.orderDetails = orderDetails;
    }

    public String getCourierOrderNumber() {
        return courierOrderNumber;
    }

    public void setCourierOrderNumber(String courierOrderNumber) {
        this.courierOrderNumber = courierOrderNumber;
    }

    public String getCourierResponsibleImageURL() {
        return courierResponsibleImageURL;
    }

    public void setCourierResponsibleImageURL(String courierResponsibleImageURL) {
        this.courierResponsibleImageURL = courierResponsibleImageURL;
    }

    public CancellationReason getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(CancellationReason cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public String getCancellationComments() {
        return cancellationComments;
    }

    public void setCancellationComments(String cancellationComments) {
        this.cancellationComments = cancellationComments;
    }
    public int getCustomerReview() {
        return customerReview;
    }

    public void setCustomerReview(int customerReview) {
        this.customerReview = customerReview;
    }

    public String getCustomerReviewComments() {
        return customerReviewComments;
    }

    public void setCustomerReviewComments(String customerReviewComments) {
        this.customerReviewComments = customerReviewComments;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getIdDeliveryOrderWebSafe() {
        return idDeliveryOrderWebSafe;
    }

    public void setIdDeliveryOrderWebSafe(String idDeliveryOrderWebSafe) {
        this.idDeliveryOrderWebSafe = idDeliveryOrderWebSafe;
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

    public List<DeliveryOrderItem> getItemList() {
        return itemList;
    }

    public void setItemList(List<DeliveryOrderItem> itemList) {
        this.itemList = itemList;
    }

    public long getIdOrder() {
        return idOrder;
    }

    public void setIdOrder(long idOrder) {
        this.idOrder = idOrder;
    }

    public int getIdAddress() {
        return idAddress;
    }

    public void setIdAddress(int idAddress) {
        this.idAddress = idAddress;
    }

    public List<Item> getHighlightedItems() {
        return highlightedItems;
    }

    public void setHighlightedItems(List<Item> highlightedItems) {
        this.highlightedItems = highlightedItems;
    }

    public String getLastStatus() {
        return lastStatus;
    }

    public void setLastStatus(String lastStatus) {
        this.lastStatus = lastStatus;
    }

    public Integer getQuotas() {
        return quotas;
    }

    public void setQuotas(Integer quotas) {
        this.quotas = quotas;
    }

    public DeliveryType getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(DeliveryType deliveryType) {
        this.deliveryType = deliveryType;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public BigInteger getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(BigInteger createdDate) {
        this.createdDate = createdDate;
    }

    public String getPush() {
        return push;
    }

    public void setPush(String push) {
        this.push = push;
    }

    public Long getCustomerIdCallCenter() {
        return customerIdCallCenter;
    }

    public void setCustomerIdCallCenter(Long customerIdCallCenter) {
        this.customerIdCallCenter = customerIdCallCenter;
    }

    public double getProviderDeliveryPrice() {
        return providerDeliveryPrice;
    }

    public void setProviderDeliveryPrice(double providerDeliveryPrice) {
        this.providerDeliveryPrice = providerDeliveryPrice;
    }

    public List<DeliveryOrderProvider> getProviderList() {
        return providerList;
    }

    public void setProviderList(List<DeliveryOrderProvider> providerList) {
        this.providerList = providerList;
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

    public float getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(float discountRate) {
        this.discountRate = discountRate;
    }

    public int getContainsItemQuantity() {
        return containsItemQuantity;
    }

    public void setContainsItemQuantity(int containsItemQuantity) {
        this.containsItemQuantity = containsItemQuantity;
    }

    public String getDeliveryLabel() {
        return deliveryLabel;
    }

    public void setDeliveryLabel(String deliveryLabel) {
        this.deliveryLabel = deliveryLabel;
    }

    public Boolean getHighDemand() {
        return isHighDemand;
    }

    public void setHighDemand(Boolean highDemand) {
        isHighDemand = highDemand;
    }

    public DeliveryTimeLabelTemplate getDeliveryTimeLabel() {
        return deliveryTimeLabel;
    }

    public Tip getTip() {
        return tip;
    }

    public Double getTipPrice() {
        return tipPrice;
    }

    public void setTipPrice(Double tipPrice) {
        this.tipPrice = tipPrice;
    }

    public void setTip(Tip tip) {
        this.tip = tip;
    }

    public void setDeliveryTimeLabel(DeliveryTimeLabelTemplate deliveryTimeLabel) {
        this.deliveryTimeLabel = deliveryTimeLabel;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public String getMessageWhenHasItemsNotBilled() {
        return messageWhenHasItemsNotBilled;
    }

    public void setMessageWhenHasItemsNotBilled(String messageWhenHasItemsNotBilled) {
        this.messageWhenHasItemsNotBilled = messageWhenHasItemsNotBilled;
    }

    public Long getReassignmentOrder() {
        return reassignmentOrder;
    }

    public void setReassignmentOrder(Long reassignmentOrder) {
        this.reassignmentOrder = reassignmentOrder;
    }

    public SelfCheckout getSelfCheckout() {
        return selfCheckout;
    }

    public void setSelfCheckout(SelfCheckout selfCheckout) {
        this.selfCheckout = selfCheckout;
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

    public static Comparator<DeliveryOrderV2> getDateComparator() {
        return DateComparator;
    }

    public static void setDateComparator(Comparator<DeliveryOrderV2> dateComparator) {
        DateComparator = dateComparator;
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

    public String getUrlPrescription() {
        return urlPrescription;
    }

    public void setUrlPrescription(String urlPrescription) {
        this.urlPrescription = urlPrescription;
    }

    public Long getOrderPrimeId() {
        return orderPrimeId;
    }

    public void setOrderPrimeId(Long orderPrimeId) {
        this.orderPrimeId = orderPrimeId;
    }

    public DeductDiscount getDeductDiscount() {
        return deductDiscount;
    }

    public void setDeductDiscount(DeductDiscount deductDiscount) {
        this.deductDiscount = deductDiscount;
    }

    public Coupon getCoupon() {
        return coupon;
    }

    public void setCoupon(Coupon coupon) {
        this.coupon = coupon;
    }

    public List<CouponAutomaticTalon> getCouponAutomaticTalonList() {
        return couponAutomaticTalonList;
    }

    public void setCouponAutomaticTalonList(List<CouponAutomaticTalon> couponAutomaticTalonList) {
        this.couponAutomaticTalonList = couponAutomaticTalonList;
    }

    public List<SupplierShippingCostMarketplace> getSupplierShippingCost() {
        return supplierShippingCost;
    }

    

    public Double getMarketplaceShippingCostTotal() {
        return marketplaceShippingCostTotal;
    }

    public void setMarketplaceShippingCostTotal(Double marketplaceShippingCostTotal) {
        this.marketplaceShippingCostTotal = marketplaceShippingCostTotal;
    }

    public void setSupplierShippingCost(List<SupplierShippingCostMarketplace> supplierShippingCost) {
        this.supplierShippingCost = supplierShippingCost;
    }

    public ShoppingCartCourierCostResp getShoppingCartCourierCost() {
        return this.shoppingCartCourierCost;
    }

    public void setShoppingCartCourierCost(ShoppingCartCourierCostResp shoppingCartCourierCost) {
        this.shoppingCartCourierCost = shoppingCartCourierCost;
    }
    public String getDeliveryHome() {
        return deliveryHome;
    }

    public void setDeliveryHome(String deliveryHome) {
        this.deliveryHome = deliveryHome;
    }

    public static Comparator<DeliveryOrderV2> DateComparator
            = new Comparator<DeliveryOrderV2>() {

        public int compare(DeliveryOrderV2 order1, DeliveryOrderV2 order2) {

            Date date1 = order1.getCreateDate();
            Date date2 = order2.getCreateDate();

            if (date1.before(date2)) {
                return 1;
            }
            if (date1.after(date2)) {
                return -1;
            }
            return 0;
        }

    };

    /**
     * Update actual order with processed order to ensure integrity
     *
     * @param processedOrder processed order in CORE
     */
    public void updateShopping(DeliveryOrderV2 processedOrder) {
        if (!analogousOrders(processedOrder)) {
            this.deliveryPrice = processedOrder.getDeliveryPrice();
            this.subTotalPrice = processedOrder.getSubTotalPrice();
            this.offerPrice = processedOrder.getOfferPrice();
            this.totalPrice = processedOrder.getTotalPrice();
            this.providerDeliveryPrice = processedOrder.getProviderDeliveryPrice();
        }
    }

    /**
     * Compare actual order with processed order to ensure integrity
     *
     * @param processedOrder processed order in CORE
     * @return true if orders are analogous, false otherwise
     */
    private boolean analogousOrders(DeliveryOrderV2 processedOrder) {
        return this.deliveryPrice != processedOrder.getDeliveryPrice() ||
                this.subTotalPrice != processedOrder.getSubTotalPrice() ||
                this.offerPrice != processedOrder.getOfferPrice() ||
                this.totalPrice != processedOrder.getTotalPrice();
    }

    public boolean isSelfCheckout() {
        return Objects.nonNull(this.getSelfCheckout()) &&
                this.getSelfCheckout().getIsSelfCheckout() &&
                Objects.nonNull(this.getSelfCheckout().getIdBox());
    }

    public boolean hasItems() {
        return Objects.nonNull(this.itemList) && !this.itemList.isEmpty();
    }

    @Override
    public String toString() {
        return "DeliveryOrder{" +
                "orderid='" + idOrder + '\'' +
                ", createDate=" + createDate +
                ", lastStatus='" + lastStatus + '\'' +
                ", pickingDate=" + pickingDate +
                '}';
    }

    public String toStringJson() {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = null;
        try {
            json = ow.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return json;
    }

    public boolean hasCouponTalon() {
        return Objects.nonNull(this.getCoupon());
    }

    public boolean hasCouponDigitalMedia() {
        return Objects.nonNull(this.getOfferPrice()) && this.getOfferPrice()>0 && Objects.nonNull(this.coupon) && this.coupon.getTypeNotificacion().equals("Info");
    }

    public DeliveryOrderV2(DeliveryOrder order) {
        if (order != null) {
            this.idDeliveryOrder = order.getIdDeliveryOrder();
            this.securityToken = order.getSecurityToken();
            this.source = order.getSource();
            this.customerType = order.getCustomerType();
            this.customerDocumentNumber = order.getCustomerDocumentNumber();
            this.customerName = order.getCustomerName();
            this.customerPhone = order.getCustomerPhone();
            this.currentStatus = order.getCurrentStatus();
            this.closerStoreSelectMode = order.getCloserStoreSelectMode();
            this.idAddress = order.getIdAddress();
            this.address = order.getAddress();
            this.addressDetails = order.getAddressDetails();
            this.paymentType = order.getPaymentType();
            this.paymentDetails = order.getPaymentDetails();
            this.weight = order.getWeight();
            this.lowerRangeWeight = order.getLowerRangeWeight();
            this.topRangeWeight = order.getTopRangeWeight();
            this.subTotalPrice = order.getSubTotalPrice();
            this.offerPrice = order.getOfferPrice();
            this.deliveryPrice = order.getDeliveryPrice();
            this.registeredOffer = order.getRegisteredOffer();
            this.orderDetails = order.getOrderDetails();
            this.courierOrderNumber = order.getCourierOrderNumber();
            this.courierResponsibleImageURL = order.getCourierResponsibleImageURL();
            this.cancellationReason = order.getCancellationReason();
            this.cancellationComments = order.getCancellationComments();
            this.customerReview = order.getCustomerReview();
            this.customerReviewComments = order.getCustomerReviewComments();
            this.createDate = order.getCreateDate();
            this.city = order.getCity();
            this.totalPrice = order.getTotalPrice();
            this.idDeliveryOrderWebSafe = order.getIdDeliveryOrderWebSafe();
            this.idFarmatodo = order.getIdFarmatodo();
            this.minutes = order.getMinutes();
            this.idStoreGroup = order.getIdStoreGroup();
            this.paymentCardId = order.getPaymentCardId();
            this.itemList = order.getItemList();
            this.highlightedItems = order.getHighlightedItems();
            this.idOrder = order.getIdOrder();
            this.lastStatus = order.getLastStatus();
            this.quotas = order.getQuotas();
            this.deliveryType = order.getDeliveryType();
            this.isActive = order.getActive();
            this.createdDate = Objects.nonNull(order.getCreatedDate()) ? BigInteger.valueOf(order.getCreatedDate()) : null;
            this.push = order.getPush();
            this.pickingDate = order.getPickingDate();
            this.customerIdCallCenter = order.getCustomerIdCallCenter();
            this.tip = order.getTip();
            this.tipPrice = order.getTipPrice();
            this.providerDeliveryPrice = order.getProviderDeliveryPrice();
            this.totalDelivery = order.getTotalDelivery();
            this.quantityFarmatodo = order.getQuantityFarmatodo();
            this.quantityProviders = order.getQuantityProviders();
            this.totalQuantity = order.getTotalQuantity();
            this.containsItemQuantity = order.getContainsItemQuantity();
            this.discountRate = order.getDiscountRate();
            this.deliveryLabel = order.getDeliveryLabel();
            this.deliveryTimeLabel = order.getDeliveryTimeLabel();
            this.providerList = order.getProviderList();
            this.isHighDemand = order.getHighDemand();
            this.qrCode = order.getQrCode();
            this.creditCardToken = order.getCreditCardToken();
            this.typePersonPSE = order.getTypePersonPSE();
            this.ipAddress = order.getIpAddress();
            this.financialInstitutions = order.getFinancialInstitutions();
            this.identification = order.getIdentification();
            this.reassignmentOrder = order.getReassignmentOrder();
            this.selfCheckout = order.getSelfCheckout();
            this.savingPrime = order.getSavingPrime();
            this.isTalonOneDiscount = order.isTalonOneDiscount();
            this.talonOneItemFree = order.isTalonOneItemFree();
            this.primeDiscountFlag = order.isPrimeDiscountFlag();
            this.primeDeliveryValue = order.getPrimeDeliveryValue();
            this.orderPrimeId = order.getOrderPrimeId();
            this.urlPrescription = order.getUrlPrescription();
            this.coupon = order.getCoupon();
            this.deductDiscount = order.getDeductDiscount();
            this.couponAutomaticTalonList = order.getCouponAutomaticTalonList();
            this.deliveryHome = order.getDeliveryHome();
            this.supplierShippingCost = order.getSupplierShippingCost();
            this.marketplaceShippingCostTotal = order.getMarketplaceShippingCostTotal();
            this.shoppingCartCourierCost = order.getShoppingCartCourierCost();
            this.shippingCostItemsMarkeplaceRequest = order.getShippingCostItemsMarkeplaceRequest();
            this.usedCredits = order.getUsedCredits();
            this.farmaCredits = order.getFarmaCredits();
            this.talonOneData = order.getTalonOneData();
            this.idOptimalRoute = order.getIdOptimalRoute();
            this.optionSelectedPopUp = order.getOptionSelectedPopUp();
            this.messageWhenHasItemsNotBilled = order.getMessageWhenHasItemsNotBilled();
        }
    }
}
