package com.imaginamos.farmatodo.model.optics;

public class ConfigDeliveryTimeOptics {

    private String deliveryTimeExpress;

    private String deliveryTimeExtendido;

    private String deliveryTimeDefault;

    private String deliveryTimeOrderExpress;

    private String deliveryTimeOrderExtendido;

    private String deliveryTimeOrderDefault;

    public ConfigDeliveryTimeOptics() {
    }

    public ConfigDeliveryTimeOptics(String deliveryTimeExpress, String deliveryTimeExtendido, String deliveryTimeDefault, String deliveryTimeOrderExpress, String deliveryTimeOrderExtendido, String deliveryTimeOrderDefault) {
        this.deliveryTimeExpress = deliveryTimeExpress;
        this.deliveryTimeExtendido = deliveryTimeExtendido;
        this.deliveryTimeDefault = deliveryTimeDefault;
        this.deliveryTimeOrderExpress = deliveryTimeOrderExpress;
        this.deliveryTimeOrderExtendido = deliveryTimeOrderExtendido;
        this.deliveryTimeOrderDefault = deliveryTimeOrderDefault;
    }

    public String getDeliveryTimeExpress() {
        return deliveryTimeExpress;
    }

    public void setDeliveryTimeExpress(String deliveryTimeExpress) {
        this.deliveryTimeExpress = deliveryTimeExpress;
    }

    public String getDeliveryTimeExtendido() {
        return deliveryTimeExtendido;
    }

    public void setDeliveryTimeExtendido(String deliveryTimeExtendido) {
        this.deliveryTimeExtendido = deliveryTimeExtendido;
    }

    public String getDeliveryTimeDefault() {
        return deliveryTimeDefault;
    }

    public void setDeliveryTimeDefault(String deliveryTimeDefault) {
        this.deliveryTimeDefault = deliveryTimeDefault;
    }

    public String getDeliveryTimeOrderExpress() {
        return deliveryTimeOrderExpress;
    }

    public void setDeliveryTimeOrderExpress(String deliveryTimeOrderExpress) {
        this.deliveryTimeOrderExpress = deliveryTimeOrderExpress;
    }

    public String getDeliveryTimeOrderExtendido() {
        return deliveryTimeOrderExtendido;
    }

    public void setDeliveryTimeOrderExtendido(String deliveryTimeOrderExtendido) {
        this.deliveryTimeOrderExtendido = deliveryTimeOrderExtendido;
    }

    public String getDeliveryTimeOrderDefault() {
        return deliveryTimeOrderDefault;
    }

    public void setDeliveryTimeOrderDefault(String deliveryTimeOrderDefault) {
        this.deliveryTimeOrderDefault = deliveryTimeOrderDefault;
    }

    @Override
    public String toString() {
        return "ConfigDeliveryTimeOptics{" +
                "deliveryTimeExpress='" + deliveryTimeExpress + '\'' +
                ", deliveryTimeExtendido='" + deliveryTimeExtendido + '\'' +
                ", deliveryTimeDefault='" + deliveryTimeDefault + '\'' +
                ", deliveryTimeOrderExpress='" + deliveryTimeOrderExpress + '\'' +
                ", deliveryTimeOrderExtendido='" + deliveryTimeOrderExtendido + '\'' +
                ", deliveryTimeOrderDefault='" + deliveryTimeOrderDefault + '\'' +
                '}';
    }
}
