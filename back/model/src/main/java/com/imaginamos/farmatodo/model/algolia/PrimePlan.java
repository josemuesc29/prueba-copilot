package com.imaginamos.farmatodo.model.algolia;

import java.util.List;

public class PrimePlan {

    private String title;
    private String type_enum;
    private String band_text;
    private String main_price;
    private String term;
    private String short_term_price;
    private String bottom_label;
    private String actual_plan_redirect_url;
    private String bottom_label_trial;
    private List<String> payment_methods;
    private List<PaymentMethodsV2> payment_methods_v2;
    private List<String> free_trial_payment_methods;
    private String priority;
    private String product_id;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType_enum() {
        return type_enum;
    }

    public void setType_enum(String type_enum) {
        this.type_enum = type_enum;
    }

    public String getBand_text() {
        return band_text;
    }

    public void setBand_text(String band_text) {
        this.band_text = band_text;
    }

    public String getMain_price() {
        return main_price;
    }

    public void setMain_price(String main_price) {
        this.main_price = main_price;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getShort_term_price() {
        return short_term_price;
    }

    public void setShort_term_price(String short_term_price) {
        this.short_term_price = short_term_price;
    }

    public String getBottom_label() {
        return bottom_label;
    }

    public void setBottom_label(String bottom_label) {
        this.bottom_label = bottom_label;
    }

    public String getActual_plan_redirect_url() {
        return actual_plan_redirect_url;
    }

    public void setActual_plan_redirect_url(String actual_plan_redirect_url) {
        this.actual_plan_redirect_url = actual_plan_redirect_url;
    }

    public String getBottom_label_trial() {
        return bottom_label_trial;
    }

    public void setBottom_label_trial(String bottom_label_trial) {
        this.bottom_label_trial = bottom_label_trial;
    }

    public List<String> getPayment_methods() {
        return payment_methods;
    }

    public void setPayment_methods(List<String> payment_methods) {
        this.payment_methods = payment_methods;
    }

    public List<PaymentMethodsV2> getPayment_methods_v2() {
        return payment_methods_v2;
    }

    public void setPayment_methods_v2(List<PaymentMethodsV2> payment_methods_v2) {
        this.payment_methods_v2 = payment_methods_v2;
    }

    public List<String> getFree_trial_payment_methods() {
        return free_trial_payment_methods;
    }

    public void setFree_trial_payment_methods(List<String> free_trial_payment_methods) {
        this.free_trial_payment_methods = free_trial_payment_methods;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    @Override
    public String toString() {
        return "prime_plans{" +
                '}';
    }
}
