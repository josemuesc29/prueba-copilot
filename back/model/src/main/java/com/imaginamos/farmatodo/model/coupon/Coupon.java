package com.imaginamos.farmatodo.model.coupon;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.IgnoreSave;
import com.googlecode.objectify.annotation.Index;
import com.imaginamos.farmatodo.model.product.Item;
import com.imaginamos.farmatodo.model.util.DeliveryType;

import java.util.Date;

/**
 * Created by USUARIO on 10/07/2017.
 */
@Entity
public class Coupon {
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Id
  private String couponId;
  @Index
  private String name;
  @Index
  private Long expirationDate;
  @Index
  private Long startDate;
  @Index
  private Boolean expires;
  private Long countUses;
  private Boolean hasLimit;
  private Long maximumNumber;
  private Boolean hasDiscount;
  private Long discountValue;
  private Long cufId;
  private Boolean hasRestriction;
  private Long restrictionValue;
  private Boolean startsLater;
  private String photoUrl;
  private String firstDescription;
  private String secondDescription;
  private CouponType couponType;
  private PayMethodType payMethodType;

  @IgnoreSave
  private String qrCode;

  private Long offerId;
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Index
  private Key<Item> itemId;

  @IgnoreSave
  private String keyClient;
  @IgnoreSave
  private String token;
  @IgnoreSave
  private String tokenIdWebSafe;
  @IgnoreSave
  private String idCustomerWebSafe;
  @IgnoreSave
  private Date startTime;
  @IgnoreSave
  private Date endTime;
  @IgnoreSave
  private Boolean replace;
  @IgnoreSave
  private String oldName;

  private int idUser;

  private String source;

  private DeliveryType deliveryType;

  public Boolean getReplace() {
    return replace;
  }

  public void setReplace(Boolean replace) {
    this.replace = replace;
  }

  public String getOldName() {
    return oldName;
  }

  public void setOldName(String oldName) {
    this.oldName = oldName;
  }

  @IgnoreSave
  private CustomerCouponStatusEnum status;

  public String getQrCode() {
    return qrCode;
  }

  public void setQrCode(String qrCode) {
    this.qrCode = qrCode;
  }

  public CustomerCouponStatusEnum getStatus() {
    return status;
  }

  public void setStatus(CustomerCouponStatusEnum status) {
    this.status = status;
  }

  public String getCouponId() {
    return couponId;
  }

  public void setCouponId(String couponId) {
    this.couponId = couponId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Long getExpirationDate() {
    return expirationDate;
  }

  public void setExpirationDate(Long expirationDate) {
    this.expirationDate = expirationDate;
  }

  public Boolean getExpires() {
    return expires;
  }

  public void setExpires(Boolean expires) {
    this.expires = expires;
  }

  public Long getCountUses() {
    return countUses;
  }

  public void setCountUses(Long countUses) {
    this.countUses = countUses;
  }

  public Boolean getHasLimit() {
    return hasLimit;
  }

  public void setHasLimit(Boolean hasLimit) {
    this.hasLimit = hasLimit;
  }

  public Long getMaximumNumber() {
    return maximumNumber;
  }

  public void setMaximumNumber(Long maximumNumber) {
    this.maximumNumber = maximumNumber;
  }

  public Boolean getHasDiscount() {
    return hasDiscount;
  }

  public void setHasDiscount(Boolean hasDiscount) {
    this.hasDiscount = hasDiscount;
  }

  public Long getDiscountValue() {
    return discountValue;
  }

  public void setDiscountValue(Long discountValue) {
    this.discountValue = discountValue;
  }

  public Long getCufId() {
    return cufId;
  }

  public void setCufId(Long cufId) {
    this.cufId = cufId;
  }

  public String getKeyClient() {
    return keyClient;
  }

  public void setKeyClient(String keyClient) {
    this.keyClient = keyClient;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String getTokenIdWebSafe() {
    return tokenIdWebSafe;
  }

  public void setTokenIdWebSafe(String tokenIdWebSafe) {
    this.tokenIdWebSafe = tokenIdWebSafe;
  }

  public String getIdCustomerWebSafe() {
    return idCustomerWebSafe;
  }

  public void setIdCustomerWebSafe(String idCustomerWebSafe) {
    this.idCustomerWebSafe = idCustomerWebSafe;
  }

  public Date getStartTime() {
    return startTime;
  }

  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  public Date getEndTime() {
    return endTime;
  }

  public void setEndTime(Date endTime) {
    this.endTime = endTime;
  }

  public Long getStartDate() {
    return startDate;
  }

  public void setStartDate(Long startDate) {
    this.startDate = startDate;
  }

  public Boolean getHasRestriction() {
    return hasRestriction;
  }

  public void setHasRestriction(Boolean hasRestriction) {
    this.hasRestriction = hasRestriction;
  }

  public Long getRestrictionValue() {
    return restrictionValue;
  }

  public void setRestrictionValue(Long restrictionValue) {
    this.restrictionValue = restrictionValue;
  }

  public Boolean getStartsLater() {
    return startsLater;
  }

  public void setStartsLater(Boolean startsLater) {
    this.startsLater = startsLater;
  }

  public String getPhotoUrl() {
    return photoUrl;
  }

  public void setPhotoUrl(String photoUrl) {
    this.photoUrl = photoUrl;
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

  public CouponType getCouponType() {
    return couponType;
  }

  public void setCouponType(CouponType couponType) {
    this.couponType = couponType;
  }

  public Long getOfferId() {
    return offerId;
  }

  public void setOfferId(Long offerId) {
    this.offerId = offerId;
  }

  public Key<Item> getItemId() {
    return itemId;
  }

  public void setItemId(Key<Item> itemId) {
    this.itemId = itemId;
  }

  public PayMethodType getPayMethodType() {
    return payMethodType;
  }

  public void setPayMethodType(PayMethodType payMethodType) {
    this.payMethodType = payMethodType;
  }

  public enum CouponType {
    PERCENTAGE("PERCENTAGE"),
    VALUE("VALUE"),
    PAYMETHOD("PAYMETHOD"),
    BRAND("BRAND"),
    FIRSTPURCHASE("FIRSTPURCHASE");

    CouponType(String couponType) {
      this.couponType = couponType;
    }

    private String couponType;

    public String getCouponType() {
      return couponType;
    }

    public void setCouponType(String couponType) {
      this.couponType = couponType;
    }

    public String toString() {
      return this.couponType;
    }

  }

  public enum PayMethodType {
    VISA("VISA"),
    MASTERCARD("MASTERCARD"),
    AMEX("AMEX"),
    DINERS("DINERS");

    PayMethodType(String payMethodType) {
      this.payMethodType = payMethodType;
    }

    private String payMethodType;

    public String getPayMethodType() {
      return payMethodType;
    }

    public void setPayMethodType(String payMethodType) {
      this.payMethodType = payMethodType;
    }

    public String toString() {
      return this.payMethodType;
    }

  }

  public int getIdUser() {
    return idUser;
  }

  public void setIdUser(int idUser) {
    this.idUser = idUser;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public DeliveryType getDeliveryType() {
    return deliveryType;
  }

  public void setDeliveryType(DeliveryType deliveryType) {
    this.deliveryType = deliveryType;
  }


  @Override
  public String toString() {
    return "Coupon{" +
            "couponId='" + couponId + '\'' +
            ", name='" + name + '\'' +
            ", expirationDate=" + expirationDate +
            ", startDate=" + startDate +
            ", expires=" + expires +
            ", countUses=" + countUses +
            ", hasLimit=" + hasLimit +
            ", maximumNumber=" + maximumNumber +
            ", hasDiscount=" + hasDiscount +
            ", discountValue=" + discountValue +
            ", cufId=" + cufId +
            ", hasRestriction=" + hasRestriction +
            ", restrictionValue=" + restrictionValue +
            ", startsLater=" + startsLater +
            ", photoUrl='" + photoUrl + '\'' +
            ", firstDescription='" + firstDescription + '\'' +
            ", secondDescription='" + secondDescription + '\'' +
            ", couponType=" + couponType +
            ", payMethodType=" + payMethodType +
            ", qrCode='" + qrCode + '\'' +
            ", offerId=" + offerId +
            ", itemId=" + itemId +
            ", keyClient='" + keyClient + '\'' +
            ", token='" + token + '\'' +
            ", tokenIdWebSafe='" + tokenIdWebSafe + '\'' +
            ", idCustomerWebSafe='" + idCustomerWebSafe + '\'' +
            ", startTime=" + startTime +
            ", endTime=" + endTime +
            ", replace=" + replace +
            ", oldName='" + oldName + '\'' +
            ", status=" + status +
            '}';
  }
}
