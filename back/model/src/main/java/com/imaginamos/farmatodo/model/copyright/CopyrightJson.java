package com.imaginamos.farmatodo.model.copyright;

public class CopyrightJson {
    private String id;
    private String description;
    private String deliveryType;
    private Boolean active;
    private Boolean provider;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(String deliveryType) {
        this.deliveryType = deliveryType;
    }

    public void setId(String id) { this.id = id; }

    public String getId() { return id; }

    public Boolean getActive() { return active; }

    public void setActive(Boolean active) { this.active = active; }

    public Boolean getProvider() { return provider; }

    public void setProvider(Boolean provider) { this.provider = provider; }

    @Override
    public String toString() {
        return "CopyrightJson{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", deliveryType='" + deliveryType + '\'' +
                ", isActive=" + active +
                ", isProvider=" + provider +
                '}';
    }
}
