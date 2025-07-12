package com.imaginamos.farmatodo.backend.util;

import com.imaginamos.farmatodo.model.order.DeliveryOrder;
import com.imaginamos.farmatodo.model.order.DiscountSASByCustomerData;
import com.imaginamos.farmatodo.model.order.DiscountSASByCustomerResponse;
import com.imaginamos.farmatodo.model.user.User;
import com.imaginamos.farmatodo.model.util.URLConnections;


import java.util.logging.Logger;

import static com.imaginamos.farmatodo.backend.OfyService.ofy;

public class SubscribeAndSaveUtil {

    private static final Logger LOG = Logger.getLogger(SubscribeAndSaveUtil.class.getName());

    /**
     * Obtener el id del customer desde el objeto de la orden.
     * @param deliveryOrder - La orden.
     * @return customerID
     *
     * */
    public static Long getCustomerIdByDeliveryOrder(final DeliveryOrder deliveryOrder) {
        try {
            if (deliveryOrder == null){
                return 0L;
            }
            final int id = ofy().load().type(User.class).ancestor(deliveryOrder.getIdCustomer().getKey()).first().now().getId();
            return Long.valueOf(id);
        }catch (Exception e){
            return 0L;
        }
    }

    /**
     *
     * */
    public static int getSubscribeAndSaveDiscountCartByCustomer(final Long customerID){
        LOG.info("method getSubscribeAndSaveDiscountCartByCustomer("+customerID+")");
        int percentage = 0;
        try {
            final String urlBackend30 = URLConnections.URL_GET_DISCOUNT_BY_CUSTOMER_BC_30.replace("{idCustomer}", String.valueOf(customerID));
            final DiscountSASByCustomerResponse response = CoreConnection.getRequest(urlBackend30, DiscountSASByCustomerResponse.class);

            LOG.info("IF(response != null) : ["+(response != null)+"]");
            if(response != null){
                DiscountSASByCustomerData data = response.getData();
                LOG.info("IF(date != null) : ["+(data != null)+"]");
                if(data != null){
                    final String discount = data.getPercentageToApply();
                    LOG.info("IF(discount != null) : ["+(discount != null)+"]");
                    if(discount != null){
                        percentage = Integer.parseInt(discount);
                        LOG.info("percentage found : ["+(percentage)+"]");
                    }
                }
            }
        }catch (Exception e){
            percentage = 0;
        }
        return percentage;
    }

    /**
     * Convertir el descuento de Integer a porcentaje en Float.
     * */
    public static Double toDecimalPercentage(final int percentage){
        return 1D - (percentage/100D );
    }

}
