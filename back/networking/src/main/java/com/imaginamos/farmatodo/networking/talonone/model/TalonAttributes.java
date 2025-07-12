package com.imaginamos.farmatodo.networking.talonone.model;

import com.imaginamos.farmatodo.model.dto.EnableForEnum;
import com.imaginamos.farmatodo.model.order.DeliveryOrder;
import com.imaginamos.farmatodo.model.order.ShoppingCartJson;
import com.imaginamos.farmatodo.model.order.ValidateOrderReq;

import java.util.*;
import java.util.logging.Logger;

public class TalonAttributes {

    private static final Logger LOG = Logger.getLogger(TalonAttributes.class.getName());
    private final String DELIVERY_TYPE = "deliveryType";
    private final String SOURCE = "source";
    private final String PAYMENT_CARD_ID = "paymentCardId";
    private final String FARMA_CREDITS = "farmaCredits";

    /**
     *
     * @param shoppingCartJson
     * @param validateOrderReq
     * add attributes to shoppingCartJSON
     */
    public void addTalonAttributes(ShoppingCartJson shoppingCartJson, ValidateOrderReq validateOrderReq) {

        try {
            if (!checkIfTalonOneDataExists(shoppingCartJson.getTalonOneData())) {
                return;
            }

            if (Objects.nonNull(validateOrderReq.getDeliveryType())) {
                shoppingCartJson.getTalonOneData().put(DELIVERY_TYPE, validateOrderReq.getDeliveryType());
            }

            if (Objects.nonNull(validateOrderReq.getSource())) {
                shoppingCartJson.getTalonOneData().put(SOURCE, validateOrderReq.getSource());
            }

            if (Objects.nonNull(shoppingCartJson.getPaymentCardId())) {
                shoppingCartJson.getTalonOneData().put(PAYMENT_CARD_ID, shoppingCartJson.getPaymentCardId());
            }

            if (Objects.isNull(shoppingCartJson.getTalonOneData().get(FARMA_CREDITS)) && Objects.nonNull(shoppingCartJson.getFarmaCredits())) {
                shoppingCartJson.getTalonOneData().put(FARMA_CREDITS, shoppingCartJson.getFarmaCredits());
            }
        } catch (Exception exception) {
            LOG.warning("Error adding talon attributes to shopping cart -> " + exception.getMessage());
        }
    }

    /**
     *
     * @param order
     * add attributes to delivery order
     */
    public void addTalonAttributes(DeliveryOrder order) {
        try {
            if (!checkIfTalonOneDataExists(order.getTalonOneData())) {
                return;
            }

            if (Objects.nonNull(order.getDeliveryType())) {
                order.getTalonOneData().put(DELIVERY_TYPE, order.getDeliveryType());
            }

            if (Objects.nonNull(order.getSource())) {
                order.getTalonOneData().put(SOURCE, order.getSource());
            }

            if (Objects.nonNull(order.getPaymentCardId())) {
                order.getTalonOneData().put(PAYMENT_CARD_ID, order.getPaymentCardId());
            }

            if (Objects.isNull(order.getTalonOneData().get(FARMA_CREDITS)) && Objects.nonNull(order.getFarmaCredits())) {
                order.getTalonOneData().put(FARMA_CREDITS, order.getFarmaCredits());
            }
        } catch (Exception exception) {
            LOG.warning("Error adding talon attributes to order -> " + exception.getMessage());
        }
    }

    public boolean checkIfTalonOneDataExists(Map<String, Object> talonOneData) {
        return Objects.nonNull(talonOneData);
    }

    public static Map<String, Object> getTalonOneAttributes(String storeId, String deliveryType, String city, EnableForEnum source) {
        Map<String, Object> talonOneData = new HashMap<>();
        talonOneData.put("storeId", Objects.nonNull(storeId) ? storeId : null);
        talonOneData.put("deliveryType", Objects.nonNull(deliveryType) ? deliveryType : null);
        talonOneData.put("city", Objects.nonNull(city) ? city : null);
        talonOneData.put("source", Objects.nonNull(source) ? source.toValue() : null);
        return talonOneData;
    }
}
