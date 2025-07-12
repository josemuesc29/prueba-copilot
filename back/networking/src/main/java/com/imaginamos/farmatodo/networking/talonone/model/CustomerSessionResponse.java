package com.imaginamos.farmatodo.networking.talonone.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CustomerSessionResponse {
    private String created;
    private Integer applicationId;
    private String profileId;
    private List<String> coupons;
    private String referral;
    private String state;
    private List<CartItem> cartItems;
    private Map<String, String> attributes;
    private Boolean firtsSession;
    private BigDecimal total;
    private BigDecimal additionalCostTotal;
    private Boolean dry;
    private List<Effect> effects;

    private List<TriggeredCampaign> triggeredCampaigns;

    private Long availableFarmaCredits;

    private Long usedCredits;

    public Long getUsedCredits() {
        return usedCredits;
    }

    public void setUsedCredits(Long usedCredits) {
        this.usedCredits = usedCredits;
    }

    public Long getAvailableFarmaCredits() {
        return availableFarmaCredits;
    }

    public void setAvailableFarmaCredits(Long availableFarmaCredits) {
        this.availableFarmaCredits = availableFarmaCredits;
    }

    public List<TriggeredCampaign> getTriggeredCampaigns() {
        return triggeredCampaigns;
    }

    public void getTriggeredCampaigns(List<TriggeredCampaign> triggeredCampaigns) {
        this.triggeredCampaigns = triggeredCampaigns;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public Integer getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Integer applicationId) {
        this.applicationId = applicationId;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public List<String> getCoupons() {
        return coupons;
    }

    public void setCoupons(List<String> coupons) {
        this.coupons = coupons;
    }

    public String getReferral() {
        return referral;
    }

    public void setReferral(String referral) {
        this.referral = referral;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public Boolean getFirtsSession() {
        return firtsSession;
    }

    public void setFirtsSession(Boolean firtsSession) {
        this.firtsSession = firtsSession;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getAdditionalCostTotal() {
        return additionalCostTotal;
    }

    public void setAdditionalCostTotal(BigDecimal additionalCostTotal) {
        this.additionalCostTotal = additionalCostTotal;
    }

    public Boolean getDry() {
        return dry;
    }

    public void setDry(Boolean dry) {
        this.dry = dry;
    }

    public List<Effect> getEffects() {
        return effects;
    }

    public void setEffects(List<Effect> effects) {
        this.effects = effects;
    }

    @Override
    public String toString() {
        return "CustomerSessionResponse{" +
                "created='" + created + '\'' +
                ", applicationId=" + applicationId +
                ", profileId='" + profileId + '\'' +
                ", coupons=" + coupons +
                ", referral='" + referral + '\'' +
                ", state='" + state + '\'' +
                ", cartItems=" + cartItems +
                ", attributes=" + attributes +
                ", firtsSession=" + firtsSession +
                ", total=" + total +
                ", additionalCostTotal=" + additionalCostTotal +
                ", dry=" + dry +
                ", effects=" + effects +
                '}';
    }
    public boolean hasEffects() {
        return Objects.nonNull(this.getEffects()) && this.getEffects().size() > 0;
    }
}
