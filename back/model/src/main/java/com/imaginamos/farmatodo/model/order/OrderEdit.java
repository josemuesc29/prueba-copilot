package com.imaginamos.farmatodo.model.order;

import com.google.appengine.repackaged.com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class OrderEdit {

    @SerializedName("order_no")
    private Long order_no;
    private String uuid;
    private ArrayList<ItemRequest> items;
    private ArrayList<CouponRequest> coupons;

    public Long getOrder_no() {
        return order_no;
    }

    public void setOrder_no(Long order_no) {
        this.order_no = order_no;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public ArrayList<ItemRequest> getItems() {
        return items;
    }

    public void setItems(ArrayList<ItemRequest> items) {
        this.items = items;
    }

    public ArrayList<CouponRequest> getCoupons() {
        return coupons;
    }

    public void setCoupons(ArrayList<CouponRequest> coupons) {
        this.coupons = coupons;
    }

    @Override
    public String toString() {
        return "OrderEdit{" +
                "order_no=" + order_no +
                ", uuid='" + uuid + '\'' +
                ", items=" + items +
                ", coupons=" + coupons +
                '}';
    }

    public boolean isValid(){
        return order_no != null && uuid != null && items != null && items.size() > 0;
    }


}
