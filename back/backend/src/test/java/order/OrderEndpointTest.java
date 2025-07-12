
package order;

import com.imaginamos.farmatodo.backend.order.create_order.infraestructure.OrderEndpoint;
import com.imaginamos.farmatodo.backend.order.create_order.infraestructure.OrderEndpointValidation;
import com.imaginamos.farmatodo.model.order.DeliveryOrderItem;
import org.mockito.*;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@PrepareForTest(OrderEndpoint.class)
public class OrderEndpointTest {

    //ApiGatewayService apiGatewayService = Mockito.mock(ApiGatewayService.class);

    private final long CUSTOMER_ID = 1234;
    private final double PRIME_PRICE_CERO = 0.0;

    private final double PRIME_PRICE = 2500.0;
    OrderEndpointValidation orderEndpointValidation = Mockito.mock(OrderEndpointValidation.class);

    private OrderEndpoint orderEndpoint;

    @BeforeEach
    public void setUp() {
        orderEndpoint = spy(new OrderEndpoint());
    }

    @Test
    public void isPrimeDiscountFlagFalse() {
        DeliveryOrderItem deliveryOrderItem = new DeliveryOrderItem();
        deliveryOrderItem.setPrimePrice(PRIME_PRICE);
        List<DeliveryOrderItem> listDeliveryOrderItem = new ArrayList<DeliveryOrderItem>();
        listDeliveryOrderItem.add(deliveryOrderItem);
        boolean expected = false;
        boolean isPrimeUser = false;

        OrderEndpointValidation orderEndpointValidation = new OrderEndpointValidation();
        boolean response = orderEndpointValidation.isPrimeDiscountFlag(isPrimeUser, listDeliveryOrderItem);

        Assertions.assertEquals(expected, response);
    }

    @Test
    public void isPrimeDiscountFlagTrue() {
        DeliveryOrderItem deliveryOrderItem = new DeliveryOrderItem();
        deliveryOrderItem.setPrimePrice(PRIME_PRICE);
        List<DeliveryOrderItem> listDeliveryOrderItem = new ArrayList<DeliveryOrderItem>();
        listDeliveryOrderItem.add(deliveryOrderItem);
        boolean expected = true;
        boolean isPrimeUser = true;

        OrderEndpointValidation orderEndpointValidation = new OrderEndpointValidation();
        boolean response = orderEndpointValidation.isPrimeDiscountFlag(isPrimeUser, listDeliveryOrderItem);

        Assertions.assertEquals(expected, response);
    }

    @Test
    public void isPrimeDiscountFlagPrimePriceCero() {
        DeliveryOrderItem deliveryOrderItem = new DeliveryOrderItem();
        deliveryOrderItem.setPrimePrice(PRIME_PRICE_CERO);
        List<DeliveryOrderItem> listDeliveryOrderItem = new ArrayList<DeliveryOrderItem>();
        listDeliveryOrderItem.add(deliveryOrderItem);
        boolean expected = false;
        boolean isPrimeUser = true;

        OrderEndpointValidation orderEndpointValidation = new OrderEndpointValidation();
        boolean response = orderEndpointValidation.isPrimeDiscountFlag(isPrimeUser, listDeliveryOrderItem);

        Assertions.assertEquals(expected, response);
    }
}