package com.imaginamos.farmatodo.model.coupon;

public class ValidationMessage {
    private boolean isMandatoryFilter;
    private String title;
    private String firstOption;
    private String secondOption;
    private String message;
    private ErrorCouponMsg errorCode;
    private String iconImage;

    private String firstAction;

    private String secondAction;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFirstOption() {
        return firstOption;
    }

    public void setFirstOption(String firstOption) {
        this.firstOption = firstOption;
    }

    public String getSecondOption() {
        return secondOption;
    }

    public void setSecondOption(String secondOption) {
        this.secondOption = secondOption;
    }

    public ErrorCouponMsg getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCouponMsg errorCode) {
        this.errorCode = errorCode;
    }

    public String getIconImage() {
        return iconImage;
    }

    public void setIconImage(String iconImage) {
        this.iconImage = iconImage;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isMandatoryFilter() {
        return isMandatoryFilter;
    }

    public void setMandatoryFilter(boolean mandatoryFilter) {
        isMandatoryFilter = mandatoryFilter;
    }

    public String getFirstAction() {
        return firstAction;
    }

    public void setFirstAction(String firstAction) {
        this.firstAction = firstAction;
    }

    public String getSecondAction() {
        return secondAction;
    }

    public void setSecondAction(String secondAction) {
        this.secondAction = secondAction;
    }
}
