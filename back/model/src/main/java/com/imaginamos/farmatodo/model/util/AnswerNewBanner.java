package com.imaginamos.farmatodo.model.util;

import com.imaginamos.farmatodo.model.cms.Banner;

public class AnswerNewBanner {

    private Boolean confirmation;
    private String Message;
    private Banner banner;

    public Boolean getConfirmation() {
        return confirmation;
    }

    public void setConfirmation(Boolean confirmation) {
        this.confirmation = confirmation;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public Banner getBanner() {
        return banner;
    }

    public void setBanner(Banner banner) {
        this.banner = banner;
    }
}
