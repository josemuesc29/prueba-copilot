package co.farmatodo.cv.core.application;

import co.farmatodo.cv.core.api.constants.DeliveryTypeEnum;
import co.farmatodo.cv.core.api.domain.oms.OrderDetailRequestDomainV2;
import co.farmatodo.cv.core.api.domain.oms.ShoppingCartDomainV2;
import co.farmatodo.cv.core.api.domain.oms.ShoppingCartResponseDomain;
import co.farmatodo.cv.core.api.events.ResponseCode;
import co.farmatodo.cv.core.api.events.ResponseEvent;
import co.farmatodo.cv.core.api.manager.oms.ShoppingCartManager;
import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class ShoppingCartManagerHandlerTest extends TestCase {
    @Autowired
    private ShoppingCartManager shoppingCartManagerHandler;

    @Before
    public void setup() {
    }

    @Test
    public void getItemTest() {
        ResponseEvent<ShoppingCartResponseDomain> responseDomainResponseEvent = shoppingCartManagerHandler.priceDelivery(null);
        Assert.assertEquals(null, responseDomainResponseEvent.getData());
    }

    @Test
    public void priceDelivery() {
        final ShoppingCartDomainV2 cart = new ShoppingCartDomainV2();
        cart.setSource("WEB");
        cart.setStoreId(47L);
        cart.setDeliveryType(DeliveryTypeEnum.EXPRESS);
        cart.setCustomerId(1292191L);
        OrderDetailRequestDomainV2 item = new OrderDetailRequestDomainV2();
        item.setQuantityRequested(1);
        item.setItemId(264650023L);
        List<OrderDetailRequestDomainV2> items = new ArrayList<>();
        items.add(item);
        cart.setItems(items);
        ResponseEvent<ShoppingCartResponseDomain> responseDomainResponseEvent = shoppingCartManagerHandler.priceDelivery(cart);
        Assert.assertNotNull(responseDomainResponseEvent);
        log.info("result {} {}", responseDomainResponseEvent.getCode(), responseDomainResponseEvent.getMessage());
        Assert.assertEquals(ResponseCode.OK, responseDomainResponseEvent.getCode());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void loadShoppingCart() {
        shoppingCartManagerHandler.loadShoppingCart(null, null, null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void clearShoppingCart() {
        shoppingCartManagerHandler.clearShoppingCart(null, null, null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getItemFromShoppingCart() {
        shoppingCartManagerHandler.getItemFromShoppingCart(null, null, null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void addItemToShoppingCart() {
        shoppingCartManagerHandler.addItemToShoppingCart(null, null, null, null, false, null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void deleteItemFromShoppingCart() {
        shoppingCartManagerHandler.deleteItemFromShoppingCart(null, null, null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void redeemCoupon() {
        shoppingCartManagerHandler.redeemCoupon(null, null, null);
    }

}
