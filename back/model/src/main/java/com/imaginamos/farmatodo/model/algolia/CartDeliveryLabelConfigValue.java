package com.imaginamos.farmatodo.model.algolia;

public class CartDeliveryLabelConfigValue {

    private Integer storeId;

    private String city;

    private String label;

    private Long dateFrom;

    private Long dateUntil;

    private String couponName;

    private Integer scheduleOrderStart;

    private Integer scheduleDuration;

    private Boolean mandatory;

    private String highDemandMessage;

    private String generalMessage;

    public Integer getStoreId() {
        return storeId;
    }

    public void setStoreId(Integer storeId) {
        this.storeId = storeId;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Long getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(Long dateFrom) {
        this.dateFrom = dateFrom;
    }

    public Long getDateUntil() {
        return dateUntil;
    }

    public void setDateUntil(Long dateUntil) {
        this.dateUntil = dateUntil;
    }

    public String getCouponName() {
        return couponName;
    }

    public void setCouponName(String couponName) {
        this.couponName = couponName;
    }

    public Boolean getMandatory() {
        return mandatory;
    }

    public void setMandatory(Boolean mandatory) {
        this.mandatory = mandatory;
    }

    public Integer getScheduleDuration() {
        return scheduleDuration;
    }

    public void setScheduleDuration(Integer scheduleDuration) {
        this.scheduleDuration = scheduleDuration;
    }

    public void setScheduleOrderStart(Integer scheduleOrderStart) { this.scheduleOrderStart = scheduleOrderStart; }

    public Integer getScheduleOrderStart() { return scheduleOrderStart; }

    public String getHighDemandMessage() { return highDemandMessage; }

    public void setHighDemandMessage(String highDemandMessage) { this.highDemandMessage = highDemandMessage; }

    public String getGeneralMessage() { return generalMessage; }

    public void setGeneralMessage(String generalMessage) { this.generalMessage = generalMessage; }

}
