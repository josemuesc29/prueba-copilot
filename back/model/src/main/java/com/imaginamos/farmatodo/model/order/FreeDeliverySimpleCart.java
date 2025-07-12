package com.imaginamos.farmatodo.model.order;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;


public class FreeDeliverySimpleCart implements Serializable {

    private Long customerId;
    private String cityId;
    private String deliveryType;
    private String source;
    private List<FreeDeliveryItem> items;

    private List<FreeDeliveryCoupon> coupons;

    public FreeDeliverySimpleCart(Long customerId, String cityId, String deliveryType, String source, List<FreeDeliveryItem> items) {
        this.customerId = customerId;
        this.cityId = cityId;
        this.deliveryType = deliveryType;
        this.source = source;
        this.items = items;
    }

    public FreeDeliverySimpleCart(Long customerId, String cityId, String deliveryType, String source, List<FreeDeliveryItem> items, List<FreeDeliveryCoupon> coupons) {
        this.customerId = customerId;
        this.cityId = cityId;
        this.deliveryType = deliveryType;
        this.source = source;
        this.items = items;
        this.coupons = coupons;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCityId() {
        return cityId;
    }

    public void setCityId(String cityId) {
        this.cityId = cityId;
    }

    public String getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(String deliveryType) {
        this.deliveryType = deliveryType;
    }

    public List<FreeDeliveryItem> getItems() {
        return items;
    }

    public void setItems(List<FreeDeliveryItem> items) {
        this.items = items;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public List<FreeDeliveryCoupon> getCoupons() {
        return coupons;
    }

    public void setCoupons(List<FreeDeliveryCoupon> coupons) {
        this.coupons = coupons;
    }

    @Override
    public String toString() {
        StringBuilder itemsStr = new StringBuilder();
        if ( Objects.nonNull(items) ){
            for (FreeDeliveryItem item : items){
                itemsStr.append("{ item="+item.getItemId()+", quantity="+item.getQuantity()+"}");
            }
        }
        StringBuilder couponsStr = new StringBuilder();
        if ( Objects.nonNull(coupons) ){
            for (FreeDeliveryCoupon coupon : coupons){
                couponsStr.append("{ couponType="+coupon.getCouponType()+", offerId="+coupon.getOfferId()+"}");
            }
        }
        return "cart : {" +
                "customerId = " + customerId +
                ", cityId = '" + cityId + '\'' +
                ", deliveryType = '" + deliveryType + '\'' +
                ", items = [" + itemsStr.toString() + "]" +
                ", source = '" + source + '\'' +
                ", coupons = [" + couponsStr.toString() + "]" +
                '}';
    }
}
