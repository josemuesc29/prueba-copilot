package com.imaginamos.farmatodo.backend.order.create_order.domain;

import com.googlecode.objectify.Key;
import com.imaginamos.farmatodo.model.coupon.CustomerCoupon;
import com.imaginamos.farmatodo.model.customer.Customer;
import com.imaginamos.farmatodo.model.order.DeliveryOrder;
import com.imaginamos.farmatodo.model.order.DeliveryOrderItem;
import com.imaginamos.farmatodo.model.order.DeliveryOrderProvider;
import com.imaginamos.farmatodo.model.order.Tracing;
import com.imaginamos.farmatodo.model.user.User;

import java.util.List;

public interface OrderRepository {

    boolean existsBlockedUserById(final int customerId);

    DeliveryOrder findActiveDeliveryOrderByidCustomerKey(final Key<Customer> customerKey);

    DeliveryOrder findActiveDeliveryOrderByidCustomerKey(final Key<Customer> customerKey, int retryMS);

    List<DeliveryOrderItem> findDeliveryOrderItemByDeliveryOrder(final DeliveryOrder deliveryOrder);

    List<DeliveryOrderProvider> findDeliveryOrderProviderByDeliveryOrder(DeliveryOrder deliveryOrder);

    DeliveryOrder findActiveDeliveryOrderByRefCustomer(final Key<Customer> refCustomer);

    DeliveryOrder findActiveDeliveryOrderByRefCustomer(final Key<Customer> refCustomer, int retryMS);

    Key<DeliveryOrder> saveDeliveryOrder(DeliveryOrder deliveryOrder);

    void saveOrderTracing(Tracing tracing);

    List<CustomerCoupon> getCustomerCouponsByCustomerKey(Key<Customer> customerKey);

    void saveDeliveryOrderItems(List<DeliveryOrderItem> deliveryOrderItems);

    DeliveryOrder findDeliveryOrderByOrderId(long orderId);

    DeliveryOrder findDeliveryOrderByOrderId(long orderId, int retryMS);

    void deleteDeliveryOrderItems(List<DeliveryOrderItem> deliveryOrderItems);

    void deleteDeliveryOrderItems(List<DeliveryOrderItem> deliveryOrderItems, int retryMS);

    void deleteItemsSampling(List<DeliveryOrderItem> deliveryOrderItems);

    Key<User> getUserKey(String idCustomerWebSafe);

    Key<DeliveryOrder> getDeliveryOrderKey( Key<User> customerKey, String idDeliveryOrder);

    Key<DeliveryOrder> getDeliveryOrderKey( Key<User> customerKey, String idDeliveryOrder, int retryMS);
}
