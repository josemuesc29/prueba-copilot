package com.imaginamos.farmatodo.model.copyright;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.UUID;

@Entity
public class Copyright {
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    @Id
    private String idCopyright;
    @Index
    private String id;
    private String description;
    @Index
    private String deliveryType;
    @Index
    private Boolean active;
    @Index
    private Boolean provider;

    public Copyright(){}

    public Copyright(String id, String description, String deliveryType, Boolean active, Boolean isProvider) {
        this();
        this.id = id;
        this.description = description;
        this.deliveryType = deliveryType; //DeliveryType.getDeliveryType(deliveryType);
        this.active = active;
        this.provider = isProvider;
        this.idCopyright = UUID.randomUUID().toString();
    }

    public String getIdCopyright() { return idCopyright; }

    public void setIdCopyright(String idCopyright) { this.idCopyright = idCopyright; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public String getDeliveryType() { return deliveryType; }

    public void setDeliveryType(String deliveryType) { this.deliveryType = deliveryType; }

    public Boolean getActive() { return active; }

    public void setActive(Boolean active) { this.active = active; }

    public Boolean getProvider() { return provider; }

    public void setProvider(Boolean provider) { this.provider = provider; }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    @Override
    public String toString() {
        return "Copyright{" +
                "  idCopyright='" + idCopyright + '\'' +
                ", id='" + id + '\'' +
                ", description='" + description + '\'' +
                ", deliveryType=" + deliveryType +
                ", isActive=" + active +
                ", isProvider=" + provider +
                '}';
    }
}
