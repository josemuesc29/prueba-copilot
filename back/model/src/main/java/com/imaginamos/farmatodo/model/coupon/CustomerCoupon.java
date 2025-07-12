package com.imaginamos.farmatodo.model.coupon;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;
import com.imaginamos.farmatodo.model.user.User;

/**
 * Created by USUARIO on 11/07/2017.
 */
@Entity
public class CustomerCoupon {
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Id
  private String customerCouponId;
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Parent
  private Ref<Coupon> couponId;
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Index
  private Key<User> customerKey;
  @Index
  private Long useTime;

  public String getCustomerCouponId() {
    return customerCouponId;
  }

  public void setCustomerCouponId(String customerCouponId) {
    this.customerCouponId = customerCouponId;
  }

  public Ref<Coupon> getCouponId() {
    return couponId;
  }

  public void setCouponId(Ref<Coupon> couponId) {
    this.couponId = couponId;
  }

  public Key<User> getCustomerKey() {
    return customerKey;
  }

  public void setCustomerKey(Key<User> customerKey) {
    this.customerKey = customerKey;
  }

  public Long getUseTime() {
    return useTime;
  }

  public void setUseTime(Long useTime) {
    this.useTime = useTime;
  }

  @Override
  public String toString() {
    return "CustomerCoupon{" +
            "customerCouponId='" + customerCouponId + '\'' +
            ", couponId=" + couponId +
            ", customerKey=" + customerKey +
            ", useTime=" + useTime +
            '}';
  }
}
