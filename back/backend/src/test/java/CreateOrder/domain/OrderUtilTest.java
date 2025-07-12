package CreateOrder.domain;

import com.googlecode.objectify.Ref;
import com.imaginamos.farmatodo.backend.order.create_order.domain.OrderUtil;
import com.imaginamos.farmatodo.model.coupon.Coupon;
import com.imaginamos.farmatodo.model.coupon.CustomerCoupon;
import com.imaginamos.farmatodo.model.order.DeliveryOrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OrderUtilTest {
    @BeforeEach
    void setUp() {
    }

    @Test
    void orderHasCoupon() {
        assertFalse(OrderUtil.orderHasCoupon(null));
        assertFalse(OrderUtil.orderHasCoupon(new ArrayList<>()));

        List<DeliveryOrderItem> deliveryOrderItemList = new ArrayList<>();
        assertFalse(OrderUtil.orderHasCoupon(deliveryOrderItemList));

        DeliveryOrderItem deliveryOrderItem = new DeliveryOrderItem();
        deliveryOrderItem.setCoupon(false);
        deliveryOrderItemList.add(deliveryOrderItem);
        assertFalse(OrderUtil.orderHasCoupon(deliveryOrderItemList));

        deliveryOrderItem.setCoupon(true);
        deliveryOrderItemList.clear();
        deliveryOrderItemList.add(deliveryOrderItem);

        assertTrue(OrderUtil.orderHasCoupon(deliveryOrderItemList));
    }

    @Test
    void couponNameOf() {
        CustomerCoupon coupon = null;
        assertNull(OrderUtil.couponNameOf(coupon));

        coupon = new CustomerCoupon();
        assertNull(OrderUtil.couponNameOf(coupon));

        final String TEST_COUPON_NAME = "HELLO";
        Ref<Coupon> couponId = new Ref<Coupon>() {
            @Override
            public Coupon get() {
                Coupon testCoupon = new Coupon();
                testCoupon.setName(TEST_COUPON_NAME);
                return testCoupon;
            }

            @Override
            public boolean isLoaded() {
                return true;
            }
        };
        coupon.setCouponId(couponId);
        assertEquals(TEST_COUPON_NAME, OrderUtil.couponNameOf(coupon));
    }

    @Test
    void validateCouponBySourceAndDeliveryType() {
        // TODO Implementar
    }

    @Test
    void isScanAndGo() {
        // TODO Implementar
    }

    @Test
    void isScanAndGo1() {
        // TODO Implementar
    }

    @Test
    void isScanAndGo2() {
        // TODO Implementar
    }

    @Test
    void verifyCampaingCoupon() {
        // TODO Implementar;
    }

    @Test
    void couponFilter() {
        // TODO Implementar
    }

    @Test
    void couponForSource() {
        // TODO Implementar
    }

    @Test
    void couponsForDt() {
        // TODO Implementar
    }

    @Test
    void findActiveDeliveryOrderByidCustomerWebSafe() {
        // TODO Implementar
    }

    @Test
    void findItemsOf() {
        // TODO Implementar
    }

    @Test
    void getCustomerKeyFromIdCustomerWebSafe() {
        // TODO Implementar
    }

    @Test
    void deliveryOrderProviderOf() {
        // TODO Implementar
    }
}
