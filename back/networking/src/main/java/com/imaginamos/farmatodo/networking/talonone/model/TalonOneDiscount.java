package com.imaginamos.farmatodo.networking.talonone.model;

import com.google.gson.Gson;

public class TalonOneDiscount {
    private Long orderId;
    private Long campaignId;
    private String campaignName;
    private Double totalDiscount;

    public TalonOneDiscount(Long orderId, Long campaignId, String campaignName, Double totalDiscount) {
        this.orderId = orderId;
        this.campaignId = campaignId;
        this.campaignName = campaignName;
        this.totalDiscount = totalDiscount;
    }

    public TalonOneDiscount(){
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(Long campaignId) {
        this.campaignId = campaignId;
    }

    public String getCampaignName() {
        return campaignName;
    }

    public void setCampaignName(String campaignName) {
        this.campaignName = campaignName;
    }

    public Double getTotalDiscount() {
        return totalDiscount;
    }

    public void setTotalDiscount(Double totalDiscount) {
        this.totalDiscount = totalDiscount;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
