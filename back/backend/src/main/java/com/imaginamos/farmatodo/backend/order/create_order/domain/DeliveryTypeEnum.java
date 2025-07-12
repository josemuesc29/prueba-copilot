package com.imaginamos.farmatodo.backend.order.create_order.domain;

import com.imaginamos.farmatodo.backend.order.create_order.application.*;

public enum DeliveryTypeEnum {

    EXPRESS("EXPRESS") {
        @Override
        public OrderCreator getOrderCreator() {
            return new ExpressOrderCreator();
        }

        @Override
        public PriceDeliveryOrder calculateCart() {
            return new CalculateCartExpress();
        }
    },
    NATIONAL("NATIONAL"){
        @Override
        public OrderCreator getOrderCreator() {
            return new NationalOrderCreator();
        }

        @Override
        public PriceDeliveryOrder calculateCart() {
            return new CalculateCartNational();
        }
    },
    ENVIALOYA("ENVIALOYA"){
        @Override
        public OrderCreator getOrderCreator() {
            return new EnvialoYaOrderCreator();
        }

        @Override
        public PriceDeliveryOrder calculateCart() {
            return new CalculateCartEnvialoYa();
        }
    },
    SCANANDGO("SCANANDGO") {
        @Override
        public OrderCreator getOrderCreator() {
            return new ScanAndGoOrderCreator();
        }

        @Override
        public PriceDeliveryOrder calculateCart() {
            return new CalculateCartSelf();
        }
    };

    DeliveryTypeEnum(String deliveryType) {
        this.deliveryType = deliveryType;
    }

    private String deliveryType;

    public String getDeliveryType() {
        return this.deliveryType;
    }

    public static DeliveryTypeEnum getDeliveryType(String deliveryType){
        for (DeliveryTypeEnum record : values()) {
            if (record.getDeliveryType().equals(deliveryType)) {
                return record;
            }
        }
        return null;
    }

    public void setDeliveryType(String deliveryType) {
        this.deliveryType = deliveryType;
    }

    public String toString() {
        return this.deliveryType;
    }

    public abstract OrderCreator getOrderCreator();

    public abstract PriceDeliveryOrder calculateCart();
}
