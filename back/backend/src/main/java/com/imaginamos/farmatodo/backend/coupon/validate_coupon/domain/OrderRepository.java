package com.imaginamos.farmatodo.backend.coupon.validate_coupon.domain;

import com.googlecode.objectify.Key;
import com.imaginamos.farmatodo.model.customer.Customer;
import com.imaginamos.farmatodo.model.order.DeliveryOrder;
import com.imaginamos.farmatodo.model.order.DeliveryOrderItem;

import java.util.List;

public interface OrderRepository {
    DeliveryOrder findActiveDeliveryOrderByidCustomerKey(final Key<Customer> customerKey);
    List<DeliveryOrderItem> findDeliveryOrderItemByDeliveryOrder(final DeliveryOrder deliveryOrder);
}
