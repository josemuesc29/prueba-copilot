package com.imaginamos.farmatodo.backend.coupon.validate_coupon.infraestructure;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.imaginamos.farmatodo.backend.coupon.validate_coupon.domain.OrderRepository;
import com.imaginamos.farmatodo.model.customer.Customer;
import com.imaginamos.farmatodo.model.order.DeliveryOrder;
import com.imaginamos.farmatodo.model.order.DeliveryOrderItem;

import java.util.List;

import static com.imaginamos.farmatodo.backend.OfyService.ofy;

public class DataStoreOrderRepository implements OrderRepository {

    @Override
    public DeliveryOrder findActiveDeliveryOrderByidCustomerKey(Key<Customer> customerKey) {
        final Integer STATE_ACTIVE = 1;
        DeliveryOrder deliveryOrderSaved = ofy().load().type(DeliveryOrder.class)
                .filter("currentStatus", STATE_ACTIVE).ancestor(Ref.create(customerKey)).first().now();
        return deliveryOrderSaved;
    }

    @Override
    public List<DeliveryOrderItem> findDeliveryOrderItemByDeliveryOrder(final DeliveryOrder deliveryOrder) {
        return ofy().load().type(DeliveryOrderItem.class).ancestor(deliveryOrder).list();
    }
}
