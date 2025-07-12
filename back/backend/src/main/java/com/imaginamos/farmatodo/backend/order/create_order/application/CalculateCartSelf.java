package com.imaginamos.farmatodo.backend.order.create_order.application;

import com.algolia.search.exceptions.AlgoliaException;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.ConflictException;
import com.google.gson.Gson;
import com.googlecode.objectify.Key;
import com.imaginamos.farmatodo.backend.order.create_order.domain.Guard;
import com.imaginamos.farmatodo.backend.order.create_order.domain.OrderService;
import com.imaginamos.farmatodo.backend.order.create_order.domain.OrderUtil;
import com.imaginamos.farmatodo.backend.order.create_order.domain.Orders;
import com.imaginamos.farmatodo.backend.order.create_order.infraestructure.OrderEndpointValidation;
import com.imaginamos.farmatodo.backend.user.Authenticate;
import com.imaginamos.farmatodo.backend.util.TraceUtil;
import com.imaginamos.farmatodo.model.algolia.DeliveryTimeLabelTemplate;
import com.imaginamos.farmatodo.model.algolia.ItemAlgolia;
import com.imaginamos.farmatodo.model.algolia.tips.TipConfig;
import com.imaginamos.farmatodo.model.customer.Customer;
import com.imaginamos.farmatodo.model.order.*;
import com.imaginamos.farmatodo.model.user.User;
import com.imaginamos.farmatodo.model.util.AnswerDeduct;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.model.util.DeliveryType;
import com.imaginamos.farmatodo.networking.algolia.APIAlgolia;
import com.imaginamos.farmatodo.networking.growthbook.GrowthBookConfigLoader;
import com.imaginamos.farmatodo.networking.services.ApiGatewayService;
import com.imaginamos.farmatodo.networking.talonone.TalonOneService;
import com.imaginamos.farmatodo.networking.talonone.FarmaCredits;
import retrofit2.Response;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

public class CalculateCartSelf implements PriceDeliveryOrder{

    private Authenticate authenticate;

    private OrderUtil orderUtil;
    private final ExperienceCoupons experienceCoupons;

    public CalculateCartSelf() {
        orderUtil = new OrderUtil();
        experienceCoupons = new ExperienceCoupons();
    }

    private static final Logger LOG = Logger.getLogger(PriceDeliveryOrder.class.getName());

    public DeliveryOrder priceDeliveryOrder(final ShoppingCartJson shoppingCartJson, HttpServletRequest request) throws ConflictException, BadRequestException, AlgoliaException, IOException {

        if (Guard.isDeliveryTypePresent(shoppingCartJson))
            shoppingCartJson.setDeliveryType(DeliveryType.EXPRESS);

        Guard.againstInvalidTokens(shoppingCartJson.getToken(), shoppingCartJson.getTokenIdWebSafe(), Constants.ID_CUSTOMER_ANONYMOUS);

        Optional<TipConfig> tipConfigOptional = GrowthBookConfigLoader.getTipsConfig(Constants.CUSTOMER_ANONYMOUS);
        shoppingCartJson.setIdStoreGroup(APIAlgolia.getDefaultStoreIdByStoreId(shoppingCartJson.getIdStoreGroup()));
//        LOG.info("idStoreGroup request -> " + shoppingCartJson.getIdStoreGroup()+" Request changue to -> " + shoppingCartJson.toStringJson());

        final List<DeliveryOrderItem> deliveryOrderItemListToSave = new ArrayList<>();
        final List<DeliveryOrderProvider> deliveryOrderProviderListToSave = new ArrayList<>();
        List<DeliveryOrderItem> deliveryOrderItemList = new ArrayList<>();

        if (Guard.isNationalOrEnvialoYa(shoppingCartJson)) {
            shoppingCartJson.setIdStoreGroup(shoppingCartJson.getDeliveryType().getDefaultStore());
        }

        final boolean isScanAndGo  = OrderUtil.isScanAndGo(shoppingCartJson.getDeliveryType());

        final Key<Customer> customerKey = OrderService.getCustomerKeyFromIdCustomerWebSafe(shoppingCartJson.getIdCustomerWebSafe());
        DeliveryOrder deliveryOrder = OrderService.findActiveDeliveryOrderByRefCustomer(customerKey);
        Key<User> userKey = OrderService.getUserKey(shoppingCartJson.getIdCustomerWebSafe());

        if (deliveryOrder == null) {
//            LOG.info("No existe un carrito activo para el cliente.");
            deliveryOrder = OrderUtil.getEmptyDeliveryOrder(new DeliveryOrder());
        }else {
//            LOG.info("Data + Carrito " + deliveryOrder);
//            LOG.info("Existe un carrito activo para el cliente, se procede a validar.Datastore deliveryOrder.deliveryType :" + deliveryOrder.getDeliveryType());
            deliveryOrderItemList = OrderService.findItemsOf(deliveryOrder);
            OrderUtil.validateDuplicateItems(deliveryOrderItemList);
//            OrderUtil.validateRequestedQuantityVsRealStockInStore(deliveryOrder, shoppingCartJson.getIdStoreGroup());

            if (tipConfigOptional.isPresent() && !shoppingCartJson.getDeliveryType().equals(DeliveryType.EXPRESS)) {
                //LOG.warning("Eliminar propinas !EXPRESS");
                OrderUtil.deleteTips(tipConfigOptional.get(), deliveryOrder, userKey);
            }

            if (Objects.isNull(deliveryOrderItemList) ) {
                deliveryOrder =  OrderUtil.getEmptyDeliveryOrder(new DeliveryOrder());
            }

            if (deliveryOrderItemList.isEmpty()) {
                deliveryOrder = OrderUtil.getEmptyDeliveryOrder(new DeliveryOrder());
            } else {

                LOG.info("Data + Carrito" + deliveryOrder);
                deliveryOrderItemList = OrderUtil.deleteCouponExpired(deliveryOrder);

                if (Objects.isNull(deliveryOrderItemList) || deliveryOrderItemList.isEmpty()) {
                  deliveryOrder =  OrderUtil.getEmptyDeliveryOrder(new DeliveryOrder());
                }

                if (OrderUtil.validateHasItemsScanAndGo(deliveryOrderItemList)){
                    if (!deliveryOrderItemList.isEmpty()) {
                        deliveryOrder.setDeliveryType(DeliveryType.SCANANDGO);
                        shoppingCartJson.setDeliveryType(DeliveryType.SCANANDGO);
                        OrderService.removeItemsDifferentScanAndGo(deliveryOrderItemList);
                    }

                    String orderRequest = Orders.createValidateOrderJson(shoppingCartJson.getId(), shoppingCartJson.getIdStoreGroup(), deliveryOrderItemList, shoppingCartJson.getSource(), shoppingCartJson.getDeliveryType(), shoppingCartJson.getTalonOneData()).toJSONString();
                    LOG.info(orderRequest);

                    Gson gson = new Gson();
                    ValidateOrderReq validateOrderReq = gson.fromJson(orderRequest, ValidateOrderReq.class);
                    OrderJson orderJSON = null;
                    if (Objects.nonNull(deliveryOrderItemList) && !deliveryOrderItemList.isEmpty()) {

//                        try {
//                            Key<DeliveryOrder> deliveryOrderKey = OrderService.getDeliveryOrderKey(userKey, deliveryOrder.getIdDeliveryOrder());
//                            OrderUtil.validateStockItems(deliveryOrderItemList, validateOrderReq, deliveryOrderKey);
//                            OrderUtil.validateRequestedQuantityVsRealStockInStore(deliveryOrder, shoppingCartJson.getIdStoreGroup());
//                        } catch (Exception e) {
//                            LOG.warning("Error al validar el stock vs la cantidad solicitada. " + e.getMessage());
//                        }
                        if (Objects.isNull(validateOrderReq) || validateOrderReq.getItems().isEmpty()) {
                            return OrderUtil.getEmptyDeliveryOrder(deliveryOrder);
                        }

                        if (Objects.nonNull(shoppingCartJson) && Objects.nonNull(shoppingCartJson.getIdCustomerWebSafe())) {
                            validateOrderReq.setIdCustomerWebSafe(shoppingCartJson.getIdCustomerWebSafe());
                        }

                        Response<ValidateOrderBackend3> response = ApiGatewayService.get().validateOrder(validateOrderReq, TraceUtil.getXCloudTraceId(request));
                        orderJSON = Objects.nonNull(response) && Objects.nonNull(response.body()) ? response.body().getData() : null;
                        Object item4 = Objects.nonNull(response) && Objects.nonNull(response.body()) ? response.body().getData() : null;
                        OrderUtil.validateResponseShoppingCart(orderJSON);
                    }else {
                        //LOG.warning("No hay items para el tipo de envio seleccionado.");
                        OrderUtil.setQuantityInOrder(deliveryOrder, deliveryOrderProviderListToSave, deliveryOrderItemList, isScanAndGo);
                        return deliveryOrder;
                    }
                    Key<DeliveryOrder> deliveryOrderKey = OrderService.getDeliveryOrderKey(userKey, deliveryOrder.getIdDeliveryOrder());
                    deliveryOrder.setSubTotalPrice(0d);
                    deliveryOrder.setOfferPrice(0d);

                    for (ItemAlgolia itemOrder : orderJSON.getItems()) {
                        ItemAlgolia itAlgolia = APIAlgolia.getItemAlgolia(itemOrder.getId() + "" + validateOrderReq.getStoreId());
                        OrderUtil.validateFilterToItemsAlgolia(itemOrder, itAlgolia);
//                        LOG.info( "FIX_DS --- TEST " + itemOrder);
                        OrderUtil.addDeliveryItemOrder(itemOrder, shoppingCartJson, deliveryOrderKey, deliveryOrderItemListToSave, deliveryOrder, null, deliveryOrderItemList);
                    }

                    deliveryOrderItemList.stream().filter(item -> item.getCoupon() != null && item.getCoupon()).forEach(item -> deliveryOrderItemListToSave.add(item));
                    orderJSON.setDeliveryValue(0);
                    orderJSON.setProviderDeliveryValue(0);
                    deliveryOrder.setPrimeDeliveryValue(String.valueOf(orderJSON.getDeliveryValue()));
                    deliveryOrder.setOfferPrice(deliveryOrder.getOfferPrice() + orderJSON.getGlobalDiscount());
                    deliveryOrder.setWeight(orderJSON.getWeight());
                    deliveryOrder.setLowerRangeWeight(orderJSON.getLowerRangeWeight());
                    deliveryOrder.setTopRangeWeight(orderJSON.getTopRangeWeight());
                    deliveryOrder.setDeliveryPrice(orderJSON.getDeliveryValue());
                    deliveryOrder.setRegisteredOffer(orderJSON.getRegisteredDiscount());
                    deliveryOrder.setTotalPrice(deliveryOrder.getSubTotalPrice() - deliveryOrder.getOfferPrice() + (orderJSON.getDeliveryValue() + orderJSON.getProviderDeliveryValue()));
                    // Campos nuevos proveedores
                    deliveryOrder.setProviderDeliveryPrice(orderJSON.getProviderDeliveryValue());
                    deliveryOrder.setTotalDelivery(orderJSON.getDeliveryValue() + orderJSON.getProviderDeliveryValue());
                    if (Objects.nonNull(deliveryOrderProviderListToSave) && !deliveryOrderProviderListToSave.isEmpty()) {
                        //LOG.warning(" Asigna cantidad de Items de proveedor. ");
                        deliveryOrder.setQuantityProviders(deliveryOrderProviderListToSave.stream().mapToInt(provider -> provider.getQuantityItem()).sum());
                    } else {
                        deliveryOrder.setQuantityProviders(0);
                    }
                    deliveryOrder.setQuantityFarmatodo(Objects.nonNull(deliveryOrderItemList) ? deliveryOrderItemList.stream().mapToInt(item -> item.getQuantitySold()).sum() : 0);
                    deliveryOrder.setQuantityFarmatodo(deliveryOrder.getQuantityFarmatodo() > 0 ? deliveryOrder.getQuantityFarmatodo() - deliveryOrder.getQuantityProviders() : deliveryOrder.getQuantityFarmatodo());
                    deliveryOrder.setTotalQuantity(deliveryOrder.getQuantityFarmatodo() + deliveryOrder.getQuantityProviders());

                }else {

                    //LOG.warning("el carro de compras por el momento solo contiene cupones, no es necesario mandarlo a validar.");
                    for (DeliveryOrderItem item : deliveryOrderItemList) {
                        if (item.getCoupon() != null && item.getCoupon()) {
                            deliveryOrderItemListToSave.add(item);
                        }
                    }

                    deliveryOrder = OrderUtil.getEmptyDeliveryOrder(deliveryOrder);

                }

                OrderUtil.deleteDeliveryOrderDuplicates(deliveryOrderItemList, deliveryOrderProviderListToSave);
                OrderService.saveDeliveryOrder(deliveryOrder);
                LOG.info("data --> " + deliveryOrder);
                deliveryOrder.setItemList(deliveryOrderItemListToSave);
                deliveryOrder.setProviderList(deliveryOrderProviderListToSave);

            }
        }

        deliveryOrder.setDeliveryType(shoppingCartJson.getDeliveryType());
        // Se eliminan los item highlight del servicio
        deliveryOrder.setHighlightedItems(new ArrayList<>());

        int idStoreGroup = 0;
        try {
            idStoreGroup = APIAlgolia.getDefaultStoreIdByStoreId(shoppingCartJson.getIdStoreGroup());
            deliveryOrder.setDeliveryLabel(APIAlgolia.getDynamicDeliveryLabel(idStoreGroup));
        } catch (AlgoliaException e) {
            LOG.warning("No fue posible consultar la configuracion del Label del Carrito: " + e);
            deliveryOrder.setDeliveryLabel("Domicilio");
        }

        try {
            DeliveryTimeLabelTemplate deliveryTimeLabelTemplate = OrderUtil.getCustomDeliveryTimeLabelTemplateForThisOrder(deliveryOrder);
            deliveryOrder.setDeliveryTimeLabel(deliveryTimeLabelTemplate);
        } catch (Exception e) {
            LOG.warning("No fue posible consultar la configuracion del DeliveryTimeLabelTemplate del Carrito: " + e);
        }

        OrderUtil.validateIfOrderOnlyCouponProviders(deliveryOrder);
        OrderUtil.setQuantityInOrder(deliveryOrder, deliveryOrderProviderListToSave, deliveryOrderItemList, isScanAndGo);
        OrderUtil.addBarcodeInShoppingCart(deliveryOrder.getItemList(), String.valueOf(shoppingCartJson.getIdStoreGroup()));

        OrderEndpointValidation orderEndpointValidation = new OrderEndpointValidation();
        boolean isUserPrime = OrderUtil.validateUserPrime(Long.valueOf(shoppingCartJson.getId()));
        boolean hasPrimeDiscount = orderEndpointValidation.isPrimeDiscountFlag(
                isUserPrime,
                deliveryOrderItemList
        );
//        LOG.info("hasPrimeDiscount -> "+ hasPrimeDiscount);
        deliveryOrder.setPrimeDiscountFlag(hasPrimeDiscount);
        try {
//            LOG.info("Response ->>>>> " + new Gson().toJson(deliveryOrder));
            String keyCache = shoppingCartJson.getIdCustomerWebSafe() + Constants.KEY_COUPON_CACHE;
            AnswerDeduct coupon = experienceCoupons.existCouponInRedis(shoppingCartJson.getIdCustomerWebSafe(), keyCache, Constants.INDEX_REDIS_FOURTEEN);
            TalonOneService talonOneService = new TalonOneService();
            if (deliveryOrder != null &&  Objects.nonNull(deliveryOrder.getItemList()) && !deliveryOrder.getItemList().isEmpty() && userKey != null && shoppingCartJson != null){
                deliveryOrder = talonOneService.sendOrderToTalonOne(deliveryOrder, shoppingCartJson, coupon);
                deliveryOrder = OrderUtil.addFreeItemTalonOne(shoppingCartJson, userKey, deliveryOrder);
            }
            experienceCoupons.deductDiscount(shoppingCartJson.getIdCustomerWebSafe(), deliveryOrder, coupon);

        } catch (Exception e){
            LOG.info("Error total de Talon One" + e.getMessage());
            e.printStackTrace();
        }

        try {
            new FarmaCredits().calculateNewPriceWithCredits(deliveryOrder, (long) shoppingCartJson.getId());
        } catch (Exception e) {
            LOG.warning("Error al calcular el precio con creditos: " + e.getMessage());
        }

        LOG.info("Data + Carrito response" + new Gson().toJson(deliveryOrder));
        return deliveryOrder;
    }

}
