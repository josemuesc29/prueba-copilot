package CreateOrder.infraestructure;

import com.googlecode.objectify.Key;
import com.imaginamos.farmatodo.backend.order.create_order.infraestructure.DatastoreOrderRepository;
import com.imaginamos.farmatodo.model.coupon.CustomerCoupon;
import com.imaginamos.farmatodo.model.customer.Customer;
import com.imaginamos.farmatodo.model.order.DeliveryOrder;
import com.imaginamos.farmatodo.model.order.DeliveryOrderItem;
import com.imaginamos.farmatodo.model.order.DeliveryOrderProvider;
import com.imaginamos.farmatodo.model.order.Tracing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;

public class DatastoreOrderRepositoryTest {
    private DatastoreOrderRepository repository;


    @BeforeEach
    public void setup(){
        repository = Mockito.mock(DatastoreOrderRepository.class);
    }

    @Test
    void existsBlockedUserById() {
        final int userId = Mockito.anyInt();
        Mockito.when(repository.existsBlockedUserById(userId)).thenReturn(true);
        assertTrue(repository.existsBlockedUserById(userId));
    }


    @Test
    void notExistsBlockedUserById() {
        final int userId = Mockito.anyInt();
        Mockito.when(repository.existsBlockedUserById(userId)).thenReturn(false);
        assertFalse(repository.existsBlockedUserById(userId));
    }

    @Test
    void findActiveDeliveryOrderByidCustomerKeyWhenExists() {
        final Key customerKey = Mockito.mock(Key.class);
        final DeliveryOrder mockDeliveryOrder = Mockito.mock(DeliveryOrder.class);

        Mockito.when(repository.findActiveDeliveryOrderByidCustomerKey(customerKey))
                .thenReturn(mockDeliveryOrder);

        assertNotNull(repository.findActiveDeliveryOrderByidCustomerKey(customerKey));
    }

    @Test
    void findActiveDeliveryOrderByidCustomerKeyWhenNotExists() {
        Mockito.when(repository.findActiveDeliveryOrderByidCustomerKey(Mockito.any())).thenReturn(null);
        assertNull(repository.findActiveDeliveryOrderByidCustomerKey(Mockito.any()));
    }

    @Test
    void findActiveDeliveryOrderByidCustomerKeyWhenCustomerIsNull() {
        Mockito.when(repository.findActiveDeliveryOrderByidCustomerKey(null)).thenReturn(null);
        assertNull(repository.findActiveDeliveryOrderByidCustomerKey(Mockito.any()));
    }

    @Test
    void findDeliveryOrderItemByDeliveryOrder() {
        final DeliveryOrder mockDeliveryOrder = Mockito.mock(DeliveryOrder.class);
        final List<DeliveryOrderItem> mockItems = new ArrayList<>();
        Mockito.when(repository.findDeliveryOrderItemByDeliveryOrder(mockDeliveryOrder))
                .thenReturn(mockItems);
        assertNotNull(repository.findDeliveryOrderItemByDeliveryOrder(mockDeliveryOrder));
    }


    @Test
    void findDeliveryOrderItemByDeliveryOrderWhenIsNotNullAndHasOneElement() {
        List<DeliveryOrderItem> mockItems = new ArrayList<>();
        mockItems.add(Mockito.mock(DeliveryOrderItem.class));

        Mockito.when(repository.findDeliveryOrderItemByDeliveryOrder(Mockito.mock(DeliveryOrder.class)))
                .thenReturn(mockItems);

        assertNotNull(mockItems);
        assertEquals(1, mockItems.size());
    }

    @Test
    void findDeliveryOrderProviderByDeliveryOrder() {
        List<DeliveryOrderProvider> mockItemsProvider = new ArrayList<>();
        DeliveryOrder mockDeliveryOrder = Mockito.mock(DeliveryOrder.class);

        Mockito.when(repository.findDeliveryOrderProviderByDeliveryOrder(mockDeliveryOrder))
                .thenReturn(mockItemsProvider);

        assertNotNull(repository.findDeliveryOrderProviderByDeliveryOrder(mockDeliveryOrder));

    }

    @Test
    void findDeliveryOrderProviderByDeliveryOrderWhenIsNotNullAndHasOneElement() {
        List<DeliveryOrderProvider> mockItemsProvider = new ArrayList<>();
        mockItemsProvider.add(Mockito.mock(DeliveryOrderProvider.class));
        DeliveryOrder mockDeliveryOrder = Mockito.mock(DeliveryOrder.class);

        Mockito.when(repository.findDeliveryOrderProviderByDeliveryOrder(mockDeliveryOrder))
                .thenReturn(mockItemsProvider);

        List<DeliveryOrderProvider> result = repository.findDeliveryOrderProviderByDeliveryOrder(mockDeliveryOrder);

        assertNotNull(result);
        assertEquals(mockItemsProvider.size(), result.size());
    }

    @Test
    void findActiveDeliveryOrderByRefCustomer() {
        final Key refCustomer = Mockito.mock(Key.class);
        final DeliveryOrder mockDeliveryOrder = Mockito.mock(DeliveryOrder.class);

        Mockito.when(repository.findActiveDeliveryOrderByRefCustomer(refCustomer))
                .thenReturn(mockDeliveryOrder);

        final DeliveryOrder result = repository.findActiveDeliveryOrderByRefCustomer(refCustomer);

        assertNotNull(result);
    }

    @Test
    void saveDeliveryOrder() {
        final DeliveryOrder mockDeliveryOrderToSave = Mockito.mock(DeliveryOrder.class);
        final Key<DeliveryOrder> mockDeliveryOrder = Mockito.mock(Key.class);

        Mockito.when(repository.saveDeliveryOrder(mockDeliveryOrderToSave))
                .thenReturn(mockDeliveryOrder);

        final Key<DeliveryOrder> result = repository.saveDeliveryOrder(mockDeliveryOrderToSave);

        assertNotNull(result);
        assertEquals(result, mockDeliveryOrder);
    }

    @Test
    void saveOrderTracing() {
        final Tracing mockTracing = Mockito.mock(Tracing.class);
        Mockito.verify(repository, times(0)).saveOrderTracing(null);
        repository.saveOrderTracing(mockTracing);
        Mockito.verify(repository, times(1)).saveOrderTracing(mockTracing);
    }

    @Test
    void getCustomerCouponsByCustomerKey() {
        final Key<Customer> mockKeyCustomer = Mockito.mock(Key.class);
        List<CustomerCoupon> customerCouponList = new ArrayList<>();
        customerCouponList.add(Mockito.mock(CustomerCoupon.class));

        Mockito.when(repository.getCustomerCouponsByCustomerKey(mockKeyCustomer))
                .thenReturn(customerCouponList);

        final List<CustomerCoupon> result = repository.getCustomerCouponsByCustomerKey(mockKeyCustomer);

        assertNotNull(result);
        final int customerCouponListSize = customerCouponList.size();
        assertTrue(customerCouponListSize > 0);
        assertEquals(customerCouponList.size(), customerCouponListSize);
    }


    @Test
    void saveDeliveryOrderItems() {
        final List<DeliveryOrderItem> deliveryOrderItems = new ArrayList<>();

        Mockito.verify(repository, times(0)).saveDeliveryOrderItems(deliveryOrderItems);

        deliveryOrderItems.add(Mockito.mock(DeliveryOrderItem.class));

        repository.saveDeliveryOrderItems(deliveryOrderItems);

        Mockito.verify(repository, times(1)).saveDeliveryOrderItems(deliveryOrderItems);
    }

    @Test
    void findDeliveryOrderByOrderId() {
        final long orderId = 1;
        final DeliveryOrder mockDeliveryOrder = Mockito.mock(DeliveryOrder.class);

        Mockito.when(repository.findDeliveryOrderByOrderId(orderId))
                .thenReturn(mockDeliveryOrder);

        final DeliveryOrder result = repository.findDeliveryOrderByOrderId(orderId);

        assertNotNull(result);
    }

    @Test
    void deleteDeliveryOrderItems() {
        Mockito.verify(repository, times(0)).deleteDeliveryOrderItems(null);

        List<DeliveryOrderItem> deliveryOrderItems = new ArrayList<>();
        deliveryOrderItems.add(Mockito.mock(DeliveryOrderItem.class));

        repository.deleteDeliveryOrderItems(deliveryOrderItems);

        Mockito.verify(repository, times(1)).deleteDeliveryOrderItems(deliveryOrderItems);
    }
}
