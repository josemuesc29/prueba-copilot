package com.imaginamos.farmatodo.model.order;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class DeliveryOrderProvider {
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    @Id
    private String idDeliveryOrderProvider;
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    @Parent
    private Ref<DeliveryOrder> idDeliveryOrder;
    @Index
    private long id;
    @Index
    private String deliveryKey;
    private String name;
    private String email;
    private int deliveryPrice;
    private String deliveryTimeOptics;
    private List<DeliveryOrderItem> itemList;
    private int quantityItem;
    private long deliveryStatus;




    public String getDeliveryTimeOptics() {
        return deliveryTimeOptics;
    }

    public void setDeliveryTimeOptics(String deliveryTimeOptics) {
        this.deliveryTimeOptics = deliveryTimeOptics;
    }

    public DeliveryOrderProvider(){
        this.itemList = new ArrayList<>();
    }

    public DeliveryOrderProvider(long id, String name, String email, int deliveryPrice) {
        this();
        this.id = id;
        this.name = name;
        this.email = email;
        this.deliveryPrice = deliveryPrice;
    }

    public String getIdDeliveryOrderProvider() { return idDeliveryOrderProvider; }

    public void setIdDeliveryOrderProvider(String idDeliveryOrderProvider) { this.idDeliveryOrderProvider = idDeliveryOrderProvider; }

    public Ref<DeliveryOrder> getIdDeliveryOrder() { return idDeliveryOrder; }

    public void setIdDeliveryOrder(Ref<DeliveryOrder> idDeliveryOrder) { this.idDeliveryOrder = idDeliveryOrder; }

    public long getId() { return id; }

    public void setId(long id) { this.id = id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public int getDeliveryPrice() { return deliveryPrice; }

    public void setDeliveryPrice(int deliveryPrice) { this.deliveryPrice = deliveryPrice; }

    public List<DeliveryOrderItem> getItemList() { return itemList; }

    public void setItemList(List<DeliveryOrderItem> itemList) { this.itemList = itemList; }

    public String getDeliveryKey() {    return deliveryKey; }

    public void setDeliveryKey(String deliveryKey) {    this.deliveryKey = deliveryKey; }

    public int getQuantityItem() { return quantityItem; }

    public void setQuantityItem(int quantityItem) { this.quantityItem = quantityItem; }

    public long getDeliveryStatus() { return Objects.nonNull(deliveryStatus) && deliveryStatus > 0L ? deliveryStatus : 1L; }

    public void setDeliveryStatus(long deliveryStatus) { this.deliveryStatus = deliveryStatus; }

    @Override
    public String toString() {
        return "DeliveryOrderProvider{" +
                "idDeliveryOrderProvider='" + idDeliveryOrderProvider + '\'' +
                ", idDeliveryOrder=" + idDeliveryOrder +
                ", id=" + id +
                ", deliveryKey='" + deliveryKey + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", deliveryPrice=" + deliveryPrice +
                ", itemList=" + itemList +
                ", quantityItem=" + quantityItem +
                ", deliveryStatus=" + deliveryStatus +
                ", deliveryTimeOptics='" + deliveryTimeOptics + '\'' +
                '}';
    }
}
