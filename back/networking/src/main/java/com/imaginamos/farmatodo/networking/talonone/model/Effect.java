package com.imaginamos.farmatodo.networking.talonone.model;

import com.google.gson.internal.LinkedTreeMap;

import java.util.Objects;

public class Effect {
    private Integer campaignId;
    private Integer rulesetId;
    private Integer ruleIndex;
    private String ruleName;
    private String effectType;

    private Boolean isPrimeDiscount;

    private Integer triggeredByCoupon;
    private LinkedTreeMap<String, String> props;

    public Integer getCampaignId() {
        return campaignId;
    }

    public Boolean getPrimeDiscount() {
        return isPrimeDiscount;
    }

    public void setPrimeDiscount(Boolean primeDiscount) {
        isPrimeDiscount = primeDiscount;
    }

    public void setCampaignId(Integer campaignId) {
        this.campaignId = campaignId;
    }

    public Integer getRulesetId() {
        return rulesetId;
    }

    public void setRulesetId(Integer rulesetId) {
        this.rulesetId = rulesetId;
    }

    public Integer getRuleIndex() {
        return ruleIndex;
    }

    public void setRuleIndex(Integer ruleIndex) {
        this.ruleIndex = ruleIndex;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getEffectType() {
        return effectType;
    }

    public void setEffectType(String effectType) {
        this.effectType = effectType;
    }

    public Integer getTriggeredByCoupon() {
        return triggeredByCoupon;
    }

    public void setTriggeredByCoupon(Integer triggeredByCoupon) {
        this.triggeredByCoupon = triggeredByCoupon;
    }

    public LinkedTreeMap<String, String> getProps() {
        return props;
    }

    public void setProps(LinkedTreeMap<String, String> props) {
        this.props = props;
    }

    @Override
    public String toString() {
        return "Effect{" +
                "campaignId=" + campaignId +
                ", rulesetId=" + rulesetId +
                ", ruleIndex=" + ruleIndex +
                ", ruleName='" + ruleName + '\'' +
                ", effectType='" + effectType + '\'' +
                ", triggeredByCoupon=" + triggeredByCoupon +
                ", props=" + props +
                '}';
    }

    public boolean isDiscountPerItem() {
        return this.getEffectType().equals("setDiscountPerItem") && this.getProps().get("subPosition").equals("0.0");
    }

    public boolean isItemFree(){
        return this.getEffectType().equals("addFreeItem");
    }

    public boolean isDiscountPerCouponAutomatic(){
        return this.getEffectType().equals("setDiscount") && Objects.isNull(this.getTriggeredByCoupon());
    }

    public Double getValueDiscount(){
        return Objects.nonNull(this.getProps().get("value")) ? Double.valueOf(this.getProps().get("value")) : 0D;
    }
    public boolean hasNotificationError(){
        return this.getEffectType().equals("showNotification") && this.getProps().get("notificationType").equals("Error");
    }

    public boolean isCouponTalonValid(){
        return this.getEffectType().equals("acceptCoupon");
    }

    public boolean hasNotificationsCouponTalon(){
        return this.getEffectType().equals("showNotification") && Objects.nonNull(this.getTriggeredByCoupon());
    }

    public boolean couponRejectedByNotReachThreshold(){
        return this.getEffectType().equals("rejectCoupon") && this.getProps().get("rejectionReason").equals("CouponRejectedByCondition");
    }

    public boolean couponRejectedNotFound(){
        return this.getEffectType().equals("rejectCoupon") && this.getProps().get("rejectionReason").equals("CouponNotFound");
    }

    public boolean couponRejectedForAnything(){
        return this.getEffectType().equals("rejectCoupon")
                && !this.getProps().get("rejectionReason").isEmpty()
                && !this.getProps().get("rejectionReason").equals("CouponRejectedByCondition");
    }
    public boolean isCouponAutomaticValid(){
        return this.getEffectType().equals("setDiscount") && !this.getProps().get("value").isEmpty() && Objects.nonNull(this.getTriggeredByCoupon());
    }
}
