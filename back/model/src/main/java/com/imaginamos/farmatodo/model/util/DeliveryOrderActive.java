package com.imaginamos.farmatodo.model.util;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

@Entity
public class DeliveryOrderActive {
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    @Id
    private String deliveryOrder;
    @Index
    private Long idFarmatodo;
    private boolean isActive;

    public String getDeliveryOrder() {
        return deliveryOrder;
    }

    public void setDeliveryOrder(String deliveryOrder) {
        this.deliveryOrder = deliveryOrder;
    }

    public Long getIdFarmatodo() {
        return idFarmatodo;
    }

    public void setIdFarmatodo(Long idFarmatodo) {
        this.idFarmatodo = idFarmatodo;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
