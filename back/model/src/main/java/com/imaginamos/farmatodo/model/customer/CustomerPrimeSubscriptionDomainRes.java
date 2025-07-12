package com.imaginamos.farmatodo.model.customer;



public class CustomerPrimeSubscriptionDomainRes {
    private Boolean active;
    private Long expirationDate;
    private Long subscriptionDate;
    private Long current_plan_sku;
    private String payment_method_used;
    private String payment_method_card_mask_used;
    private Double total_saved;

    private Double savingCustomerNoPrime;

    private String primeId;

    private String franchise;

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Long getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Long expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Long getSubscriptionDate() {
        return subscriptionDate;
    }

    public void setSubscriptionDate(Long subscriptionDate) {
        this.subscriptionDate = subscriptionDate;
    }

    public Long getCurrent_plan_sku() {
        return current_plan_sku;
    }

    public void setCurrent_plan_sku(Long current_plan_sku) {
        this.current_plan_sku = current_plan_sku;
    }

    public String getPayment_method_used() {
        return payment_method_used;
    }

    public void setPayment_method_used(String payment_method_used) {
        this.payment_method_used = payment_method_used;
    }

    public String getPayment_method_card_mask_used() {
        return payment_method_card_mask_used;
    }

    public void setPayment_method_card_mask_used(String payment_method_card_mask_used) {
        this.payment_method_card_mask_used = payment_method_card_mask_used;
    }

    public Double getTotal_saved() {
        return total_saved;
    }

    public void setTotal_saved(Double total_saved) {
        this.total_saved = total_saved;
    }

    public Double getSavingCustomerNoPrime() {
        return savingCustomerNoPrime;
    }

    public void setSavingCustomerNoPrime(Double savingCustomerNoPrime) {
        this.savingCustomerNoPrime = savingCustomerNoPrime;
    }

    public String getPrimeId() {
        return primeId;
    }

    public void setPrimeId(String primeId) {
        this.primeId = primeId;
    }

    public String getFranchise() {
        return franchise;
    }

    public void setFranchise(String franchise) {
        this.franchise = franchise;
    }
}
