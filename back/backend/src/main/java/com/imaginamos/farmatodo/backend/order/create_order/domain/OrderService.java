package com.imaginamos.farmatodo.backend.order.create_order.domain;

import com.google.api.server.spi.response.BadRequestException;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.imaginamos.farmatodo.backend.order.create_order.infraestructure.DatastoreOrderRepository;
import com.imaginamos.farmatodo.backend.order.create_order.infraestructure.SMSBrokerClient;
import com.imaginamos.farmatodo.backend.util.MsgUtilAlgolia;
import com.imaginamos.farmatodo.model.algolia.messageconfig.MsgSmsEnum;
import com.imaginamos.farmatodo.model.coupon.CustomerCoupon;
import com.imaginamos.farmatodo.model.customer.Customer;
import com.imaginamos.farmatodo.model.order.CreatedOrder;
import com.imaginamos.farmatodo.model.order.DeliveryOrder;
import com.imaginamos.farmatodo.model.order.DeliveryOrderItem;
import com.imaginamos.farmatodo.model.order.DeliveryOrderProvider;
import com.imaginamos.farmatodo.model.product.Item;
import com.imaginamos.farmatodo.model.user.User;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.networking.services.ApiGatewayService;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import static com.imaginamos.farmatodo.backend.OfyService.ofy;

/**
 * @author Santiago Garzon
 * @author Jhon Puentes
 *
 *
 * @since 2022
 *
 * Service de acceso a datos. para las ordenes.
 *
 * */
public class OrderService {

    private static OrderRepository orderRepository = new DatastoreOrderRepository();
    private static final Logger LOG = Logger.getLogger(OrderUtil.class.getName());

    public static boolean userIsBlockedOrFraudulent(int customerId){
        boolean userBlocked = orderRepository.existsBlockedUserById(customerId);
        boolean isfraud = ApiGatewayService.get().searchFraudCustomer((long) customerId).getFraud();
        return userBlocked || isfraud;
    }


    public static DeliveryOrder findActiveDeliveryOrderByidCustomerWebSafe(final String idCustomerWebSafe) {
        return orderRepository.findActiveDeliveryOrderByidCustomerKey(Key.create(idCustomerWebSafe), 120);
    }


    public static DeliveryOrder findActiveDeliveryOrderByRefCustomer(final Key<Customer> refCustomer) {
        return orderRepository.findActiveDeliveryOrderByRefCustomer(refCustomer, 120);
    }


    public static List<DeliveryOrderItem> findItemsOf(final DeliveryOrder deliveryOrder) {

        int attempts = 0;
        while (attempts < Constants.MAX_RETRIES) {
            try {
                return orderRepository.findDeliveryOrderItemByDeliveryOrder(deliveryOrder);
            } catch (Exception e) {
                attempts++;
                LOG.warning("Error de contención en el datastore al items. Reintento " + attempts);

                if (attempts >= Constants.MAX_RETRIES) {
                    LOG.severe("No se pudo ejecutar items después de " + Constants.MAX_RETRIES + " intentos.");
                    throw new RuntimeException("Error persistente al eliminar entidades", e);
                } else {
                    // Espera exponencial entre reintentos
                    try {
                        Thread.sleep(Math.min(1000 * (long) Math.pow(2, attempts), 10000));
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        LOG.severe("Interrupción durante espera entre reintentos: " + ie.getMessage());
                    }
                }
            }
        }
        return null;
    }


    public static  Key<Customer> getCustomerKeyFromIdCustomerWebSafe(String idCustomerWebSafe) {
        return Key.create(idCustomerWebSafe);
    }


    public static List<DeliveryOrderProvider> deliveryOrderProviderOf(DeliveryOrder deliveryOrder) {
        return orderRepository.findDeliveryOrderProviderByDeliveryOrder(deliveryOrder);
    }


    public static Key<DeliveryOrder> saveDeliveryOrder(final DeliveryOrder deliveryOrder) {
        return orderRepository.saveDeliveryOrder(deliveryOrder);
    }


    public static void saveDeliveryOrderItems(List<DeliveryOrderItem> deliveryOrderItems) {
        orderRepository.saveDeliveryOrderItems(deliveryOrderItems);
    }


    public static void deleteDeliveryOrderItems(List<DeliveryOrderItem> deliveryOrderItems){
        orderRepository.deleteDeliveryOrderItems(deliveryOrderItems, 120);
    }

    public static void deleteItemsSampling(List<DeliveryOrderItem> deliveryOrderItems){
        orderRepository.deleteItemsSampling(deliveryOrderItems);
    }


    public static void saveOrderTracing(DeliveryOrder order, CreatedOrder createdOrder, Key<DeliveryOrder> deliveryOrderKey) throws BadRequestException {

        if (Objects.nonNull(createdOrder.getTracing()) && !createdOrder.getTracing().isEmpty()) {
            final String mensaje = MsgUtilAlgolia.obtainMsgAlgolia(MsgSmsEnum.MESSAGE_ORDER_CODE).replace("{orderId}", Long.toString(createdOrder.getId()));
            createdOrder.getTracing().forEach(tracing -> {
                tracing.setIdTracing(UUID.randomUUID().toString());
                tracing.setDeliveryOrderId(Ref.create(deliveryOrderKey));
                if (tracing.getStatus() == 12) {
                    CompletableFuture.runAsync(() -> SMSBrokerClient.sendSMS(order.getCustomerPhone(), mensaje));
                }
            });
            orderRepository.saveOrderTracing(createdOrder.getTracing().get(0));
        }
    }

    public static DeliveryOrder findDeliveryOrderById(final long orderId) {
        return orderRepository.findDeliveryOrderByOrderId(orderId, 120);
    }


    public static List<CustomerCoupon> getCustomerCouponsByCustomerKey(Key<Customer> customerKey) {
        return orderRepository.getCustomerCouponsByCustomerKey(customerKey);
    }


    public static List<DeliveryOrderProvider> findDeliveryOrderProviderByDeliveryOrder(DeliveryOrder deliveryOrder) {
        return orderRepository.findDeliveryOrderProviderByDeliveryOrder(deliveryOrder);
    }


    public static List<DeliveryOrderItem> findDeliveryOrderItemByDeliveryOrder(final DeliveryOrder deliveryOrder) {
        return orderRepository.findDeliveryOrderItemByDeliveryOrder(deliveryOrder);
    }

    public static Key<User> getUserKey(final String idCustomerWebSafe) {
        return orderRepository.getUserKey(idCustomerWebSafe);
    }

    public static void removeItemsDifferentScanAndGo(final List<DeliveryOrderItem> deliveryOrderItemList) {
        deliveryOrderItemList.removeIf(item -> ((Objects.isNull(item.getScanAndGo()) || !item.getScanAndGo()) && (Objects.isNull(item.getCoupon()) || !item.getCoupon())));
    }

    public static void removeItemsScanAndGo(final List<DeliveryOrderItem> deliveryOrderItemList) {
        deliveryOrderItemList.removeIf(item -> (item.getScanAndGo() != null && item.getScanAndGo()));
    }

    public static  Key<DeliveryOrder> getDeliveryOrderKey(Key<User> customerKey, final String idDeliveryOrder) {
        return orderRepository.getDeliveryOrderKey(customerKey, idDeliveryOrder, 120);
    }

}
