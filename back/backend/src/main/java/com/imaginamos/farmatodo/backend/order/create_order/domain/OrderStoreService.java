package com.imaginamos.farmatodo.backend.order.create_order.domain;

import com.imaginamos.farmatodo.backend.order.create_order.infraestructure.OrderEndpoint;
import com.imaginamos.farmatodo.model.order.ShoppingCartJson;
import com.imaginamos.farmatodo.model.order.ValidateOrderReq;
import com.imaginamos.farmatodo.model.util.DeliveryType;

import java.util.logging.Logger;

public class OrderStoreService {
    private static final Logger LOG = Logger.getLogger(OrderStoreService.class.getName());
    public DeliveryType putStore26withExpress(ShoppingCartJson shoppingCartJson) {
        if (this.isExpressForStore(shoppingCartJson)) {
//            LOG.info("cambia deliveryType a express para " + shoppingCartJson.getDeliveryType() + "--" + shoppingCartJson.getIdStoreGroup());
            return DeliveryType.EXPRESS;
        }

        return shoppingCartJson.getDeliveryType();
    }

    private boolean isExpressForStore(ShoppingCartJson shoppingCartJson) {
        return shoppingCartJson != null
                && shoppingCartJson.getDeliveryType() != null
                && shoppingCartJson.getIdStoreGroup() != 1000
                && shoppingCartJson.getDeliveryType().getDeliveryType().equals(
                        DeliveryType.NATIONAL.getDeliveryType()
                    );

    }
}
