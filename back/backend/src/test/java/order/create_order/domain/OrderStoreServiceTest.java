package order.create_order.domain;

import com.imaginamos.farmatodo.backend.order.create_order.domain.OrderStoreService;
import com.imaginamos.farmatodo.model.algolia.tips.ItemTip;
import com.imaginamos.farmatodo.model.algolia.tips.TipConfig;
import com.imaginamos.farmatodo.model.order.DeliveryOrder;
import com.imaginamos.farmatodo.model.order.DeliveryOrderItem;
import com.imaginamos.farmatodo.model.order.ShoppingCartJson;
import com.imaginamos.farmatodo.model.util.DeliveryType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class OrderStoreServiceTest {

    @Test
    public void putStore26withExpressTest() {
        OrderStoreService orderStoreService = new OrderStoreService();
        ShoppingCartJson shoppingCartJson = new ShoppingCartJson();
        shoppingCartJson.setIdStoreGroup(26);
        shoppingCartJson.setDeliveryType(DeliveryType.NATIONAL);

        DeliveryType deliveryTypeHope = DeliveryType.EXPRESS;
        DeliveryType deliveryTypeReal = orderStoreService.putStore26withExpress(shoppingCartJson);
        Assertions.assertEquals(deliveryTypeHope, deliveryTypeReal);
    }

    @Test
    public void putStore26withExpressTestWithDifferentStore() {
        OrderStoreService orderStoreService = new OrderStoreService();
        ShoppingCartJson shoppingCartJson = new ShoppingCartJson();
        shoppingCartJson.setIdStoreGroup(28);
        shoppingCartJson.setDeliveryType(DeliveryType.NATIONAL);

        DeliveryType deliveryTypeHope = DeliveryType.EXPRESS;
        DeliveryType deliveryTypeReal = orderStoreService.putStore26withExpress(shoppingCartJson);
        Assertions.assertEquals(deliveryTypeHope, deliveryTypeReal);
    }

    @Test
    public void putStore26withExpressTestIsNational() {
        OrderStoreService orderStoreService = new OrderStoreService();
        ShoppingCartJson shoppingCartJson = new ShoppingCartJson();
        shoppingCartJson.setIdStoreGroup(1000);
        shoppingCartJson.setDeliveryType(DeliveryType.NATIONAL);

        DeliveryType deliveryTypeExpected = DeliveryType.NATIONAL;
        DeliveryType deliveryTypeActual = orderStoreService.putStore26withExpress(shoppingCartJson);
        Assertions.assertEquals(deliveryTypeActual, deliveryTypeExpected);
    }

    @Test
    public void putStore26withExpressTestIsEnvialoYa() {
        OrderStoreService orderStoreService = new OrderStoreService();
        ShoppingCartJson shoppingCartJson = new ShoppingCartJson();
        shoppingCartJson.setIdStoreGroup(1001);
        shoppingCartJson.setDeliveryType(DeliveryType.ENVIALOYA);

        DeliveryType deliveryTypeExpected = DeliveryType.ENVIALOYA;
        DeliveryType deliveryTypeActual = orderStoreService.putStore26withExpress(shoppingCartJson);
        Assertions.assertEquals(deliveryTypeActual, deliveryTypeExpected);
    }

    @Test
    public void putStore26withExpressTestIsScanAndGo() {
        OrderStoreService orderStoreService = new OrderStoreService();
        ShoppingCartJson shoppingCartJson = new ShoppingCartJson();
        shoppingCartJson.setIdStoreGroup(26);
        shoppingCartJson.setDeliveryType(DeliveryType.SCANANDGO);

        DeliveryType deliveryTypeExpected = DeliveryType.SCANANDGO;
        DeliveryType deliveryTypeActual = orderStoreService.putStore26withExpress(shoppingCartJson);
        Assertions.assertEquals(deliveryTypeActual, deliveryTypeExpected);
    }

    @Test
    public void testRemoveIf() {
        List<DeliveryOrderItem> deliveryOrderItemList = new ArrayList<>();
        DeliveryOrderItem orderItem = new DeliveryOrderItem();
        orderItem.setId(1234L);
        deliveryOrderItemList.add(orderItem);

        TipConfig tipConfig = new TipConfig();
        List<ItemTip> itemTips = new ArrayList<>();
        ItemTip tip = new ItemTip();
        tip.setItemId(9876);
        tip.setItemId(1234);
        itemTips.add(tip);
        tipConfig.setItemTips(itemTips);

        System.out.println("antes " + deliveryOrderItemList);

        boolean response = deliveryOrderItemList.removeIf( item -> {
            if (tipConfig == null || tipConfig.getItemTips() == null ){
                return false;
            }
            return tipConfig.getItemTips()
                    .stream()
                    .anyMatch(itemTip -> itemTip.getItemId() != null && itemTip.getItemId().longValue() == item.getId());
        });

        Assertions.assertEquals(true, response);
        System.out.println("después " + deliveryOrderItemList);
    }
}
