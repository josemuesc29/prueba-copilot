package com.imaginamos.farmatodo.backend.order.create_order.infraestructure;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.imaginamos.farmatodo.backend.order.create_order.domain.OrderRepository;
import com.imaginamos.farmatodo.model.coupon.CustomerCoupon;
import com.imaginamos.farmatodo.model.customer.Customer;
import com.imaginamos.farmatodo.model.order.DeliveryOrder;
import com.imaginamos.farmatodo.model.order.DeliveryOrderItem;
import com.imaginamos.farmatodo.model.order.DeliveryOrderProvider;
import com.imaginamos.farmatodo.model.order.Tracing;
import com.imaginamos.farmatodo.model.user.BlockedUser;
import com.imaginamos.farmatodo.model.user.User;

import java.util.List;

import static com.imaginamos.farmatodo.backend.OfyService.ofy;

/**
 * @author Santiago Garzon
 * @author Jhon Puentes
 *
 * @since 2022
 *
 * Repositorio de acceso a datos en el datastore.
 */
public class DatastoreOrderRepository  implements OrderRepository {

    public boolean existsBlockedUserById(final int customerId) {
        return ofy().load().type(BlockedUser.class).filter("idUser", customerId).first().now() != null;
    }


    @Override
    public DeliveryOrder findActiveDeliveryOrderByidCustomerKey(final Key<Customer> customerKey ) {
        return ofy().load().type(DeliveryOrder.class)
                .filter("currentStatus", 1).ancestor(Ref.create(customerKey)).first().now();
    }

    @Override
    public DeliveryOrder findActiveDeliveryOrderByidCustomerKey(final Key<Customer> customerKey, int retryMS) {
        if(retryMS > 0){
            return  RetryUtil.retry( () -> findActiveDeliveryOrderByidCustomerKey(customerKey)
                    , 3, retryMS, "findActiveDeliveryOrderByidCustomerKey");
        }else {
            return findActiveDeliveryOrderByidCustomerKey(customerKey);
        }
    }


    public List<DeliveryOrderItem> findDeliveryOrderItemByDeliveryOrder(final DeliveryOrder deliveryOrder) {
        return ofy().load().type(DeliveryOrderItem.class).ancestor(deliveryOrder).list();
    }


    public List<DeliveryOrderProvider> findDeliveryOrderProviderByDeliveryOrder(final DeliveryOrder deliveryOrder) {
        return ofy().load().type(DeliveryOrderProvider.class).ancestor(deliveryOrder).list();
    }

    @Override
    public DeliveryOrder findActiveDeliveryOrderByRefCustomer(final Key<Customer> refCustomer) {
        return ofy().load().type(DeliveryOrder.class)
                .filter("currentStatus", 1).ancestor(refCustomer).first().now();
    }

    @Override
    public DeliveryOrder findActiveDeliveryOrderByRefCustomer(Key<Customer> refCustomer, int retryMS) {
        if(retryMS > 0){
            return  RetryUtil.retry( () -> findActiveDeliveryOrderByRefCustomer(refCustomer)
                    , 3, retryMS, "findActiveDeliveryOrderByRefCustomer");
        }else {
            return findActiveDeliveryOrderByRefCustomer(refCustomer);
        }
    }

    @Override
    public Key<DeliveryOrder> saveDeliveryOrder(final DeliveryOrder deliveryOrder) {
        return ofy().save().entity(deliveryOrder).now();
    }

    @Override
    public void saveOrderTracing(final Tracing tracing) {
        ofy().save().entity(tracing);
    }

    @Override
    public List<CustomerCoupon> getCustomerCouponsByCustomerKey(Key<Customer> customerKey) {
        return ofy().load().type(CustomerCoupon.class)
                .filter("customerKey", customerKey).orderKey(false).list();
    }

    @Override
    public void saveDeliveryOrderItems(List<DeliveryOrderItem> deliveryOrderItems) {
        ofy().save().entities(deliveryOrderItems).now();
    }

    @Override
    public DeliveryOrder findDeliveryOrderByOrderId(long orderId) {
        return ofy().load().type(DeliveryOrder.class).filter("idOrder", orderId).first().now();
    }

    @Override
    public DeliveryOrder findDeliveryOrderByOrderId(long orderId, int retryMS) {
        if(retryMS > 0){
            return  RetryUtil.retry( () -> findDeliveryOrderByOrderId(orderId)
                    , 3, retryMS, "findDeliveryOrderByOrderId");
        }else {
            return findDeliveryOrderByOrderId(orderId);
        }
    }

    @Override
    public void deleteDeliveryOrderItems(List<DeliveryOrderItem> deliveryOrderItems) {
        ofy().delete().entities(deliveryOrderItems);
    }

    @Override
    public void deleteDeliveryOrderItems(List<DeliveryOrderItem> deliveryOrderItems, int retryMS) {
        if(retryMS > 0){
            RetryUtil.retry( () ->ofy().delete().entities(deliveryOrderItems).now()
                    , 3, retryMS, "deleteDeliveryOrderItems");
        }else {
            deleteDeliveryOrderItems(deliveryOrderItems);
        }
    }

    @Override
    public void deleteItemsSampling(List<DeliveryOrderItem> deliveryOrderItems) {
        deliveryOrderItems.stream().filter(itm -> (itm.getFullPrice() == 1.0D)).forEach(itemSampling -> ofy().delete().entity(itemSampling).now());
    }

    @Override
    public Key<User> getUserKey(String idCustomerWebSafe) {
       return  Key.create(idCustomerWebSafe);
    }

    @Override
    public Key<DeliveryOrder> getDeliveryOrderKey( Key<User> customerKey, String idDeliveryOrder) {
        return  Key.create(customerKey, DeliveryOrder.class, idDeliveryOrder);
    }

    @Override
    public Key<DeliveryOrder> getDeliveryOrderKey(Key<User> customerKey, String idDeliveryOrder, int retryMS) {
        if(retryMS > 0){
            return  RetryUtil.retry( () -> getDeliveryOrderKey(customerKey,idDeliveryOrder)
                    , 3, retryMS, "getDeliveryOrderKey");
        }else {
            return getDeliveryOrderKey(customerKey,idDeliveryOrder);
        }
    }
}
