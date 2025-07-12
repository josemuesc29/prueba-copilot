package com.imaginamos.farmatodo.model.util;

import java.util.Objects;

public class AnswerDeduct extends Answer{
    private Double discount;
    private String nameCoupon;
    private String notificationMessage;
    private String typeNotifcation;
    private Double restrictionValue;

    private String rejectionReason;
    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public String getNameCoupon() {
        return nameCoupon;
    }

    public void setNameCoupon(String nameCoupon) {
        this.nameCoupon = nameCoupon;
    }

    public String getNotificationMessage() {
        return notificationMessage;
    }

    public void setNotificationMessage(String notificationMessage) {
        this.notificationMessage = notificationMessage;
    }

    public String getTypeNotifcation() {
        return typeNotifcation;
    }

    public void setTypeNotifcation(String typeNotifcation) {
        this.typeNotifcation = typeNotifcation;
    }
    public boolean isNotRejected() {
        return Objects.nonNull(this)
                && (Objects.nonNull(this.getMessage()) && !this.getMessage().equalsIgnoreCase(Constants.WORD_REJECTED)
                || Objects.nonNull(this.getNotificationMessage()) && !this.getNotificationMessage().equalsIgnoreCase(Constants.WORD_REJECTED));
    }
    public Double getRestrictionValue() {
        return restrictionValue;
    }

    public void setRestrictionValue(Double restrictionValue) {
        this.restrictionValue = restrictionValue;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public boolean hasError() {
        return Objects.nonNull(this) && Objects.nonNull(this.getRejectionReason());
    }

}
