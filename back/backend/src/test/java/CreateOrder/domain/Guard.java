package CreateOrder.domain;

import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.ConflictException;
import com.imaginamos.farmatodo.model.order.DeliveryOrder;
import com.imaginamos.farmatodo.model.payment.PaymentType;
import com.imaginamos.farmatodo.model.payment.PaymentTypeEnum;
import com.imaginamos.farmatodo.model.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Guard {
    @BeforeEach
    protected void setUp() {
    }

    @Test
    void againstInvalidTokens() {
        assertThrows(BadRequestException.class, () -> com.imaginamos.farmatodo.backend.order.create_order.domain.Guard.againstInvalidTokens(null, null, Constants.ID_CUSTOMER_ANONYMOUS));
        assertThrows(IllegalArgumentException.class, () -> com.imaginamos.farmatodo.backend.order.create_order.domain.Guard.againstInvalidTokens("","", Constants.ID_CUSTOMER_ANONYMOUS));
    }


    @Test
    void againstInvalidAddress() throws ConflictException {
        assertThrows(ConflictException.class, () -> com.imaginamos.farmatodo.backend.order.create_order.domain.Guard.againstInvalidAddress(false, 0, Constants.ID_CUSTOMER_ANONYMOUS));
        com.imaginamos.farmatodo.backend.order.create_order.domain.Guard.againstInvalidAddress(true, 0, Constants.ID_CUSTOMER_ANONYMOUS);
    }


    @Test
    void not() {
        assertTrue(com.imaginamos.farmatodo.backend.order.create_order.domain.Guard.not(false));
        assertTrue(!com.imaginamos.farmatodo.backend.order.create_order.domain.Guard.not(true));
    }

    @Test
    void againstInvalidPaymentMethod() throws ConflictException {
        DeliveryOrder orderMock = new DeliveryOrder();
        PaymentType paymentType = new PaymentType();
        paymentType.setId(PaymentTypeEnum.TRANSACCIONES_EN_LINEA.getId());
        orderMock.setPaymentType(paymentType);
        orderMock.setPaymentCardId(0);

        assertThrows(ConflictException.class, () -> com.imaginamos.farmatodo.backend.order.create_order.domain.Guard.againstInvalidPaymentMethod(orderMock));

        orderMock.setPaymentCardId(1111);
        com.imaginamos.farmatodo.backend.order.create_order.domain.Guard.againstInvalidPaymentMethod(orderMock);
    }


    @Test
    void againstNullDeliveryOrder() throws ConflictException {
        assertThrows(ConflictException.class, () -> com.imaginamos.farmatodo.backend.order.create_order.domain.Guard.againstNullDeliveryOrder(null, Constants.ID_CUSTOMER_ANONYMOUS));
        com.imaginamos.farmatodo.backend.order.create_order.domain.Guard.againstNullDeliveryOrder(new DeliveryOrder(), Constants.ID_CUSTOMER_ANONYMOUS);
    }

    @Test
    void againstOrderWithoutItems() throws ConflictException {
        assertThrows(ConflictException.class, () -> com.imaginamos.farmatodo.backend.order.create_order.domain.Guard.againstOrderWithoutItems(null, Constants.ID_CUSTOMER_ANONYMOUS));
        com.imaginamos.farmatodo.backend.order.create_order.domain.Guard.againstOrderWithoutItems(new ArrayList<>(), Constants.ID_CUSTOMER_ANONYMOUS);
    }
}
