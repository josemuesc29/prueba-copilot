package com.imaginamos.farmatodo.backend.order.create_order.infraestructure;

import com.imaginamos.farmatodo.model.order.DeliveryOrderItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class OrderEndpointValidation {
    private static final Logger LOG = Logger.getLogger(OrderEndpointValidation.class.getName());

    public boolean isPrimeDiscountFlag(boolean isUserPrime, List<DeliveryOrderItem> deliveryOrderItemList)
    {
        final double PRIME_PRICE_CERO = 0.0;
        final int DOUBLE_COMPARE_RESULT = 0;
        boolean hasPrimeDiscount = false;

        try {
            if (isUserPrime) {
                List<DeliveryOrderItem> listPrimeItemList = new ArrayList<DeliveryOrderItem>();
                if (!deliveryOrderItemList.isEmpty()) {
                    // Si tiene algún item con precio prime
                    listPrimeItemList = deliveryOrderItemList.stream()
                            .filter(item -> Objects.nonNull(item.getPrimePrice())
                                    && Double.compare(item.getPrimePrice(), PRIME_PRICE_CERO) > DOUBLE_COMPARE_RESULT
                            )
                            .collect(Collectors.toList());
                    if (!listPrimeItemList.isEmpty()) {
                        hasPrimeDiscount = true;
                    }
                }
            }
        } catch (Exception e) {
            LOG.warning("se está generando error-> " + e.getMessage());
        }
        return hasPrimeDiscount;
    }
}
