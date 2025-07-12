package com.imaginamos.farmatodo.model.braze;


public class BrazeProperties {
    private String item_id;
    private String item_name;
    private String item_variant;
    private String item_category;
    private String item_category2;
    private String item_department;
    private String brand;
    private Integer item_quantity;
    private Double item_price;
    private String item_rms_group;
    private String item_rms_deparment;
    private String item_rms_class;
    private String item_rms_subclass;
    private Double item_price_prime;
    private String order_id;

    private Long payment_method;

    public String getItem_id() {
        return item_id;
    }

    public void setItem_id(String item_id) {
        this.item_id = item_id;
    }

    public String getItem_name() {
        return item_name;
    }

    public void setItem_name(String item_name) {
        this.item_name = item_name;
    }

    public String getItem_variant() {
        return item_variant;
    }

    public void setItem_variant(String item_variant) {
        this.item_variant = item_variant;
    }

    public String getItem_category() {
        return item_category;
    }

    public void setItem_category(String item_category) {
        this.item_category = item_category;
    }

    public String getItem_category2() {
        return item_category2;
    }

    public void setItem_category2(String item_category2) {
        this.item_category2 = item_category2;
    }

    public String getItem_department() {
        return item_department;
    }

    public void setItem_department(String item_department) {
        this.item_department = item_department;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public Integer getItem_quantity() {
        return item_quantity;
    }

    public Double getItem_price_prime() {
        return item_price_prime;
    }

    public void setItem_price_prime(Double item_price_prime) {
        this.item_price_prime = item_price_prime;
    }

    public void setItem_quantity(Integer item_quantity) {
        this.item_quantity = item_quantity;
    }

    public Double getItem_price() {
        return item_price;
    }

    public void setItem_price(Double item_price) {
        this.item_price = item_price;
    }

    public String getItem_rms_group() {
        return item_rms_group;
    }

    public void setItem_rms_group(String item_rms_group) {
        this.item_rms_group = item_rms_group;
    }

    public String getItem_rms_deparment() {
        return item_rms_deparment;
    }

    public void setItem_rms_deparment(String item_rms_deparment) {
        this.item_rms_deparment = item_rms_deparment;
    }

    public String getItem_rms_class() {
        return item_rms_class;
    }

    public void setItem_rms_class(String item_rms_class) {
        this.item_rms_class = item_rms_class;
    }

    public String getItem_rms_subclass() {
        return item_rms_subclass;
    }

    public void setItem_rms_subclass(String item_rms_subclass) {
        this.item_rms_subclass = item_rms_subclass;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public Long getPayment_method() {
        return payment_method;
    }

    public void setPayment_method(Long payment_method) {
        this.payment_method = payment_method;
    }
}
