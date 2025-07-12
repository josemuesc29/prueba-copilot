package com.imaginamos.farmatodo.backend.order;

import com.imaginamos.farmatodo.backend.order.create_order.infraestructure.OrderEndpointValidation;
import com.imaginamos.farmatodo.model.customer.CustomerResponseCart;
import com.imaginamos.farmatodo.networking.services.ApiGatewayService;

import java.util.logging.Logger;

public class PrimeValidator {

    private static final Logger LOG = Logger.getLogger(OrderEndpointValidation.class.getName());

    public boolean isUserPrime(Long customerId) {
        boolean isPrime = false;
        try {
            CustomerResponseCart customerResponseCart = ApiGatewayService.get().getCustomerCreditCardPrimeData(customerId);
            if (customerResponseCart != null && customerResponseCart.isActive()) {
                isPrime = true;
            }
        } catch (Exception e){
            LOG.warning("No se pudo obtener el customer");
        }
//        LOG.info( "isPrime ericka : " + isPrime);
        return isPrime;
    }
}
