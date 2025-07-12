package com.imaginamos.farmatodo.model.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.imaginamos.farmatodo.model.algolia.ItemAlgolia;
import com.imaginamos.farmatodo.model.provider.ProviderOrder;

import java.util.List;

/**
 * Created by Eric on 15/03/2017.
 */

public class CreatedOrder {
  private long id;
  private String keyClient;
  private String address;
  private long idOrder;
  private long createDate;
  private List<Tracing> tracing;
  private List<ItemAlgolia> items;
  private DeliveryOrder orderData;
  private boolean updateShopping;
  private String securityToken;
  private List<ProviderOrder> providers;
  private boolean changePaymentCreditCard;
  private Long code;
  private String message;
  private  TransactionDetails transactionDetails;

  private String qrCode;

  private boolean isPrime;
  private boolean primeGeneral;

  private long idOrderPrime;
  private boolean isEmptyCart;

  public boolean isPrimeGeneral() {
    return primeGeneral;
  }

  public void setPrimeGeneral(boolean primeGeneral) {
    this.primeGeneral = primeGeneral;
  }

  public boolean isPrime() {
    return isPrime;
  }

  public void setPrime(boolean prime) {
    isPrime = prime;
  }

  public long getIdOrderPrime() {
    return idOrderPrime;
  }

  public void setIdOrderPrime(long idOrderPrime) {
    this.idOrderPrime = idOrderPrime;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getIdOrder() {
    return idOrder;
  }

  public void setIdOrder(long idOrder) {
    this.idOrder = idOrder;
  }

  public long getCreateDate() {
    return createDate;
  }

  public void setCreateDate(long createDate) {
    this.createDate = createDate;
  }

  public List<Tracing> getTracing() {
    return tracing;
  }

  public void setTracing(List<Tracing> tracing) {
    this.tracing = tracing;
  }

  public String getKeyClient() {
    return keyClient;
  }

  public void setKeyClient(String keyClient) {
    this.keyClient = keyClient;
  }

  public List<ItemAlgolia> getItems() {
    return items;
  }

  public void setItems(List<ItemAlgolia> items) {
    this.items = items;
  }

  public boolean getUpdateShopping() {
    return updateShopping;
  }

  public void setUpdateShopping(boolean updateShopping) {
    this.updateShopping = updateShopping;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public List<ProviderOrder> getProviders() { return providers; }

  public void setProviders(List<ProviderOrder> providers) { this.providers = providers; }

  public Boolean isChangePaymentCreditCard() {
    return changePaymentCreditCard;
  }

  public void setChangePaymentCreditCard(Boolean changePaymentCreditCard) {
    this.changePaymentCreditCard = changePaymentCreditCard;
  }

  public String getQrCode() {
    return qrCode;
  }

  public void setQrCode(String qrCode) {
    this.qrCode = qrCode;
  }

  public TransactionDetails getTransactionDetails() {
    return transactionDetails;
  }

  public void setTransactionDetails(TransactionDetails transactionDetails) {
    this.transactionDetails = transactionDetails;
  }

  public DeliveryOrder getOrderData() {
    return orderData;
  }

  public void setOrderData(DeliveryOrder orderData) {
    this.orderData = orderData;
  }



  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Long getCode() {
    return code;
  }

  public void setCode(Long code) {
    this.code = code;
  }

  public void setIsEmptyCart(){
    isEmptyCart = true;
  }

  @JsonProperty("isEmptyCart")
  public boolean isEmptyCart(){
    return isEmptyCart;
  }

  @Override
  public String toString() {
    return "CreatedOrder{" +
            "id=" + id +
            ", keyClient='" + keyClient + '\'' +
            ", address='" + address + '\'' +
            ", idOrder=" + idOrder +
            ", createDate=" + createDate +
            ", tracing=" + tracing +
            ", items=" + items +
            ", updateShopping=" + updateShopping +
            ", securityToken='" + securityToken + '\'' +
            ", providers=" + providers +
            ", changePaymentCreditCard=" + changePaymentCreditCard +
            ", transactionDetails=" + transactionDetails +
            ", qrCode='" + qrCode + '\'' +
            ", isEmptyCart='" + isEmptyCart + '\'' +
            '}';
  }
}
