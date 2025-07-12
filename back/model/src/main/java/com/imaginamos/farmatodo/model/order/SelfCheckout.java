package com.imaginamos.farmatodo.model.order;

public class SelfCheckout {

    private Boolean isSelfCheckout;
    private Long idBox;

    public SelfCheckout() {
    }

    public Boolean getIsSelfCheckout() {
        return this.isSelfCheckout;
    }

    public Long getIdBox() {
        return this.idBox;
    }

    public void setIsSelfCheckout(Boolean isSelfCheckout) {
        this.isSelfCheckout = isSelfCheckout;
    }

    public void setIdBox(Long idBox) {
        this.idBox = idBox;
    }
}
