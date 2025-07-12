package com.imaginamos.farmatodo.backend.coupon.validate_coupon.domain;


import com.googlecode.objectify.Key;
import com.imaginamos.farmatodo.backend.coupon.validate_coupon.infraestructure.DataStoreOrderRepository;
import com.imaginamos.farmatodo.model.order.DeliveryOrder;
import com.imaginamos.farmatodo.model.order.DeliveryOrderItem;

import java.util.List;

public class OrderService {
    private OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository){
        this.orderRepository = orderRepository;
    }
    public DeliveryOrder findActiveDeliveryOrderByidCustomerWebSafe(final String idCustomerWebSafe) {
        return this.orderRepository.findActiveDeliveryOrderByidCustomerKey(Key.create(idCustomerWebSafe));
    }

    public List<DeliveryOrderItem> findDeliveryOrderItemByDeliveryOrder(final DeliveryOrder deliveryOrder) {
        return this.orderRepository.findDeliveryOrderItemByDeliveryOrder(deliveryOrder);
    }
}
