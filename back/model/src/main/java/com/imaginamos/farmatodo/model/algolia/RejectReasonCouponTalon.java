package com.imaginamos.farmatodo.model.algolia;

import java.util.Map;

public class RejectReasonCouponTalon {

    private String about;
    private boolean active;
    private Map<String, String> rejectionReason;
    private String objectID;

    public RejectReasonCouponTalon() {
    }

    // Getters y Setters
    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Map<String, String> getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(Map<String, String> rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }

    // Método toString para imprimir la información de la clase
    @Override
    public String toString() {
        return "RejectReasonCouponTalon{" +
                "about='" + about + '\'' +
                ", active=" + active +
                ", rejectionReason=" + rejectionReason +
                ", objectID='" + objectID + '\'' +
                '}';
    }
}
