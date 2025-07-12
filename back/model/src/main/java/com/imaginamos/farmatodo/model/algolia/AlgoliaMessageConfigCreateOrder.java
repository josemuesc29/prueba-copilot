package com.imaginamos.farmatodo.model.algolia;

public class AlgoliaMessageConfigCreateOrder {

    private Boolean active;
    private String invalidToken;
    private String invalidAddress;

    private String multipleCallForService;
    private String mandatoryPaymentCardId;
    private String userBlocked;
    private String creditCardBlocked;
    private String errorCreatedOrderPrime;
    private String errorCreatedOrderNoPrime;
    private String invalidOrderScheduled;
    private String invalidNationalStore;
    private String invalidEnvialoyaStore;
    private String emptyShoppingCart;
    private String userNotFound;
    private String errorAddressInvalid;
    private String errorCouponDataFilter;
    private String errorCouponInvalidOrder;
    private String errorCouponInvalidPaymentMethod;
    private String errorCouponProblem;
    private String errorCreatedOrderItemsAlgolia;
    private String errorPaymentOnline;
    private String errorCreatedOrderOms;
    private String deliveryOrderNotFound;

    private String paymentDeclinedCreateOrder;
    private String successfullyCreateOrder;
    private String closedStore;


    public AlgoliaMessageConfigCreateOrder() {
    }

    public AlgoliaMessageConfigCreateOrder(Boolean active, String invalidToken, String multipleCallForService, String invalidAddress, String mandatoryPaymentCardId, String userBlocked, String creditCardBlocked, String errorCreatedOrderPrime, String errorCreatedOrderNoPrime, String invalidOrderScheduled, String invalidNationalStore, String invalidEnvialoyaStore, String emptyShoppingCart, String userNotFound, String errorAddressInvalid, String errorCouponDataFilter, String errorCouponInvalidOrder, String errorCouponInvalidPaymentMethod, String errorCouponProblem, String errorCreatedOrderItemsAlgolia, String errorPaymentOnline, String errorCreatedOrderOms, String deliveryOrderNotFound, String paymentDeclinedCreateOrder, String successfullyCreateOrder, String closedStore) {
        this.active = active;
        this.invalidToken = invalidToken;
        this.multipleCallForService = multipleCallForService;
        this.invalidAddress = invalidAddress;
        this.mandatoryPaymentCardId = mandatoryPaymentCardId;
        this.userBlocked = userBlocked;
        this.creditCardBlocked = creditCardBlocked;
        this.errorCreatedOrderPrime = errorCreatedOrderPrime;
        this.errorCreatedOrderNoPrime = errorCreatedOrderNoPrime;
        this.invalidOrderScheduled = invalidOrderScheduled;
        this.invalidNationalStore = invalidNationalStore;
        this.invalidEnvialoyaStore = invalidEnvialoyaStore;
        this.emptyShoppingCart = emptyShoppingCart;
        this.userNotFound = userNotFound;
        this.errorAddressInvalid = errorAddressInvalid;
        this.errorCouponDataFilter = errorCouponDataFilter;
        this.errorCouponInvalidOrder = errorCouponInvalidOrder;
        this.errorCouponInvalidPaymentMethod = errorCouponInvalidPaymentMethod;
        this.errorCouponProblem = errorCouponProblem;
        this.errorCreatedOrderItemsAlgolia = errorCreatedOrderItemsAlgolia;
        this.errorPaymentOnline = errorPaymentOnline;
        this.errorCreatedOrderOms = errorCreatedOrderOms;
        this.deliveryOrderNotFound = deliveryOrderNotFound;
        this.paymentDeclinedCreateOrder = paymentDeclinedCreateOrder;
        this.successfullyCreateOrder = successfullyCreateOrder;
        this.closedStore = closedStore;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getClosedStore() {
        return closedStore;
    }

    public void setClosedStore(String closedStore) {
        this.closedStore = closedStore;
    }

    public String getInvalidToken() {
        return invalidToken;
    }

    public void setInvalidToken(String invalidToken) {
        this.invalidToken = invalidToken;
    }

    public String getInvalidAddress() {
        return invalidAddress;
    }

    public void setInvalidAddress(String invalidAddress) {
        this.invalidAddress = invalidAddress;
    }

    public String getMultipleCallForService() {
        return multipleCallForService;
    }

    public void setMultipleCallForService(String multipleCallForService) {
        this.multipleCallForService = multipleCallForService;
    }

    public String getMandatoryPaymentCardId() {
        return mandatoryPaymentCardId;
    }

    public void setMandatoryPaymentCardId(String mandatoryPaymentCardId) {
        this.mandatoryPaymentCardId = mandatoryPaymentCardId;
    }

    public String getUserBlocked() {
        return userBlocked;
    }

    public void setUserBlocked(String userBlocked) {
        this.userBlocked = userBlocked;
    }

    public String getCreditCardBlocked() {
        return creditCardBlocked;
    }

    public void setCreditCardBlocked(String creditCardBlocked) {
        this.creditCardBlocked = creditCardBlocked;
    }

    public String getErrorCreatedOrderPrime() {
        return errorCreatedOrderPrime;
    }

    public void setErrorCreatedOrderPrime(String errorCreatedOrderPrime) {
        this.errorCreatedOrderPrime = errorCreatedOrderPrime;
    }

    public String getErrorCreatedOrderNoPrime() {
        return errorCreatedOrderNoPrime;
    }

    public void setErrorCreatedOrderNoPrime(String errorCreatedOrderNoPrime) {
        this.errorCreatedOrderNoPrime = errorCreatedOrderNoPrime;
    }

    public String getInvalidOrderScheduled() {
        return invalidOrderScheduled;
    }

    public void setInvalidOrderScheduled(String invalidOrderScheduled) {
        this.invalidOrderScheduled = invalidOrderScheduled;
    }

    public String getInvalidNationalStore() {
        return invalidNationalStore;
    }

    public void setInvalidNationalStore(String invalidNationalStore) {
        this.invalidNationalStore = invalidNationalStore;
    }

    public String getInvalidEnvialoyaStore() {
        return invalidEnvialoyaStore;
    }

    public void setInvalidEnvialoyaStore(String invalidEnvialoyaStore) {
        this.invalidEnvialoyaStore = invalidEnvialoyaStore;
    }

    public String getEmptyShoppingCart() {
        return emptyShoppingCart;
    }

    public void setEmptyShoppingCart(String emptyShoppingCart) {
        this.emptyShoppingCart = emptyShoppingCart;
    }

    public String getUserNotFound() {
        return userNotFound;
    }

    public void setUserNotFound(String userNotFound) {
        this.userNotFound = userNotFound;
    }

    public String getErrorAddressInvalid() {
        return errorAddressInvalid;
    }

    public void setErrorAddressInvalid(String errorAddressInvalid) {
        this.errorAddressInvalid = errorAddressInvalid;
    }

    public String getErrorCouponDataFilter() {
        return errorCouponDataFilter;
    }

    public void setErrorCouponDataFilter(String errorCouponDataFilter) {
        this.errorCouponDataFilter = errorCouponDataFilter;
    }

    public String getErrorCouponInvalidOrder() {
        return errorCouponInvalidOrder;
    }

    public void setErrorCouponInvalidOrder(String errorCouponInvalidOrder) {
        this.errorCouponInvalidOrder = errorCouponInvalidOrder;
    }

    public String getErrorCouponInvalidPaymentMethod() {
        return errorCouponInvalidPaymentMethod;
    }

    public void setErrorCouponInvalidPaymentMethod(String errorCouponInvalidPaymentMethod) {
        this.errorCouponInvalidPaymentMethod = errorCouponInvalidPaymentMethod;
    }

    public String getErrorCouponProblem() {
        return errorCouponProblem;
    }

    public void setErrorCouponProblem(String errorCouponProblem) {
        this.errorCouponProblem = errorCouponProblem;
    }

    public String getErrorCreatedOrderItemsAlgolia() {
        return errorCreatedOrderItemsAlgolia;
    }

    public void setErrorCreatedOrderItemsAlgolia(String errorCreatedOrderItemsAlgolia) {
        this.errorCreatedOrderItemsAlgolia = errorCreatedOrderItemsAlgolia;
    }

    public String getErrorPaymentOnline() {
        return errorPaymentOnline;
    }

    public void setErrorPaymentOnline(String errorPaymentOnline) {
        this.errorPaymentOnline = errorPaymentOnline;
    }

    public String getErrorCreatedOrderOms() {
        return errorCreatedOrderOms;
    }

    public void setErrorCreatedOrderOms(String errorCreatedOrderOms) {
        this.errorCreatedOrderOms = errorCreatedOrderOms;
    }

    public String getDeliveryOrderNotFound() {
        return deliveryOrderNotFound;
    }

    public void setDeliveryOrderNotFound(String deliveryOrderNotFound) {
        this.deliveryOrderNotFound = deliveryOrderNotFound;
    }

    public String getPaymentDeclinedCreateOrder() {
        return paymentDeclinedCreateOrder;
    }

    public void setPaymentDeclinedCreateOrder(String paymentDeclinedCreateOrder) {
        this.paymentDeclinedCreateOrder = paymentDeclinedCreateOrder;
    }

    public String getSuccessfullyCreateOrder() {
        return successfullyCreateOrder;
    }

    public void setSuccessfullyCreateOrder(String successfullyCreateOrder) {
        this.successfullyCreateOrder = successfullyCreateOrder;
    }

    @Override
    public String toString() {
        return "AlgoliaMessageConfigCreateOrder{" +
                "active=" + active +
                ", invalidToken='" + invalidToken + '\'' +
                ", invalidAddress='" + invalidAddress + '\'' +
                ", mandatoryPaymentCardId='" + mandatoryPaymentCardId + '\'' +
                ", userBlocked='" + userBlocked + '\'' +
                ", creditCardBlocked='" + creditCardBlocked + '\'' +
                ", errorCreatedOrderPrime='" + errorCreatedOrderPrime + '\'' +
                ", errorCreatedOrderNoPrime='" + errorCreatedOrderNoPrime + '\'' +
                ", invalidOrderScheduled='" + invalidOrderScheduled + '\'' +
                ", invalidNationalStore='" + invalidNationalStore + '\'' +
                ", invalidEnvialoyaStore='" + invalidEnvialoyaStore + '\'' +
                ", emptyShoppingCart='" + emptyShoppingCart + '\'' +
                ", userNotFound='" + userNotFound + '\'' +
                ", errorAddressInvalid='" + errorAddressInvalid + '\'' +
                ", errorCouponDataFilter='" + errorCouponDataFilter + '\'' +
                ", errorCouponInvalidOrder='" + errorCouponInvalidOrder + '\'' +
                ", errorCouponInvalidPaymentMethod='" + errorCouponInvalidPaymentMethod + '\'' +
                ", errorCouponProblem='" + errorCouponProblem + '\'' +
                ", errorCreatedOrderItemsAlgolia='" + errorCreatedOrderItemsAlgolia + '\'' +
                ", errorPaymentOnline='" + errorPaymentOnline + '\'' +
                ", errorCreatedOrderOms='" + errorCreatedOrderOms + '\'' +
                ", deliveryOrderNotFound='" + deliveryOrderNotFound + '\'' +
                ", paymentDeclinedCreateOrder='" + paymentDeclinedCreateOrder + '\'' +
                ", successfullyCreateOrder='" + successfullyCreateOrder + '\'' +
                ", closedStore='" + closedStore + '\'' +
                '}';
    }
}
