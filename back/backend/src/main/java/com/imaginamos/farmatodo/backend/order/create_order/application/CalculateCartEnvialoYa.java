package com.imaginamos.farmatodo.backend.order.create_order.application;

import com.algolia.search.exceptions.AlgoliaException;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.ConflictException;
import com.google.gson.Gson;
import com.googlecode.objectify.Key;
import com.imaginamos.farmatodo.backend.customer.CustomerEndpoint;
import com.imaginamos.farmatodo.backend.order.create_order.domain.*;
import com.imaginamos.farmatodo.backend.order.create_order.infraestructure.OrderEndpointValidation;
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
import com.imaginamos.farmatodo.model.util.FTDUtilities;
import com.imaginamos.farmatodo.networking.algolia.APIAlgolia;
import com.imaginamos.farmatodo.networking.growthbook.GrowthBookConfigLoader;
import com.imaginamos.farmatodo.networking.services.ApiGatewayService;
import com.imaginamos.farmatodo.networking.services.OpticsServices;
import com.imaginamos.farmatodo.networking.talonone.TalonOneService;
import com.imaginamos.farmatodo.networking.talonone.FarmaCredits;
import retrofit2.Response;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.logging.Logger;

public class CalculateCartEnvialoYa implements PriceDeliveryOrder{

    private static final Logger LOG = Logger.getLogger(PriceDeliveryOrder.class.getName());
    public static final String APP_BUILD_CODE_HEADER_NAME = "appbuildcode";

    private final FTDUtilities ftdUtilities;
    private final ExperienceCoupons experienceCoupons;

    public CalculateCartEnvialoYa(){
        ftdUtilities = new FTDUtilities();
        experienceCoupons = new ExperienceCoupons();
    }

    @Override
    public DeliveryOrder priceDeliveryOrder(ShoppingCartJson shoppingCartJson, HttpServletRequest request) throws ConflictException, BadRequestException, AlgoliaException {
        try {

            Guard.againstInvalidTokens(shoppingCartJson.getToken(), shoppingCartJson.getTokenIdWebSafe(), Constants.ID_CUSTOMER_ANONYMOUS);

            shoppingCartJson.setIdStoreGroup(APIAlgolia.getDefaultStoreIdByStoreId(shoppingCartJson.getIdStoreGroup()));

            Optional<TipConfig> tipConfigOptional = GrowthBookConfigLoader.getTipsConfig(Constants.CUSTOMER_ANONYMOUS);
            final List<DeliveryOrderItem> deliveryOrderItemListToSave = new ArrayList<>();
            List<DeliveryOrderProvider> deliveryOrderProviderListToSave = new ArrayList<>();
            List<DeliveryOrderItem> deliveryOrderItemList = new ArrayList<>();

            if (Guard.isNationalOrEnvialoYa(shoppingCartJson)) {
                shoppingCartJson.setIdStoreGroup(shoppingCartJson.getDeliveryType().getDefaultStore());
            }

            final boolean isScanAndGo  = OrderUtil.isScanAndGo(shoppingCartJson.getDeliveryType());

            final Key<Customer> customerKey = OrderService.getCustomerKeyFromIdCustomerWebSafe(shoppingCartJson.getIdCustomerWebSafe());
            Key<User> userKey = OrderService.getUserKey(shoppingCartJson.getIdCustomerWebSafe());
            DeliveryOrder deliveryOrder = OrderService.findActiveDeliveryOrderByRefCustomer(customerKey);

            if (deliveryOrder == null) {
                deliveryOrder = OrderUtil.getEmptyDeliveryOrder(new DeliveryOrder());
            }else {

                deliveryOrderItemList = OrderService.findItemsOf(deliveryOrder);
                OrderService.deleteItemsSampling(deliveryOrderItemList);
                OrderUtil.validateDuplicateItems(deliveryOrderItemList);

                List<DeliveryOrderProvider> deliveryOrderProviderList = new ArrayList<>();
                deliveryOrderProviderList = OrderService.findDeliveryOrderProviderByDeliveryOrder(deliveryOrder);
                String buildCodeNumberApp = request.getHeader(APP_BUILD_CODE_HEADER_NAME);
                int validBuildCodeNumberApp = OrderUtil.validBuildCodeNumberApp(buildCodeNumberApp);



                OrderUtil.validateRequestedQuantityVsRealStockInStore(deliveryOrder, shoppingCartJson.getIdStoreGroup());
                if (tipConfigOptional.isPresent() && !shoppingCartJson.getDeliveryType().equals(DeliveryType.EXPRESS)) {
                    OrderUtil.deleteTips(tipConfigOptional.get(), deliveryOrder, userKey);
                }


                if (Objects.isNull(deliveryOrderItemList) ) {
                    deliveryOrder =  OrderUtil.getEmptyDeliveryOrder(new DeliveryOrder());
                }

                if (deliveryOrderItemList.isEmpty()) {
                    deliveryOrder = OrderUtil.getEmptyDeliveryOrder(new DeliveryOrder());
                } else {

                    if (OrderUtil.validateHasItems(deliveryOrderItemList)){
                        if (!deliveryOrderItemList.isEmpty()) {
                            OrderService.removeItemsScanAndGo(deliveryOrderItemList);
                        }

                        String orderRequest = Orders.createValidateOrderJson(shoppingCartJson.getId(), shoppingCartJson.getIdStoreGroup(), deliveryOrderItemList, shoppingCartJson.getSource(), shoppingCartJson.getDeliveryType(), shoppingCartJson.getTalonOneData()).toJSONString();
                        LOG.info(orderRequest);
                        
                        Gson gson = new Gson();
                        ValidateOrderReq validateOrderReq = gson.fromJson(orderRequest, ValidateOrderReq.class);
                        OrderJson orderJSON = null;

                        if (Objects.nonNull(deliveryOrderItemList) && !deliveryOrderItemList.isEmpty()) {

                            OrderUtil.validateItemsAlgolia(validateOrderReq, shoppingCartJson.getIdStoreGroup(), shoppingCartJson.getSource(),
                                    validBuildCodeNumberApp, deliveryOrder, userKey);
                            OrderUtil.validateTipAndPrime(tipConfigOptional, deliveryOrderItemList, userKey, shoppingCartJson.getIdStoreGroup(), deliveryOrder, validateOrderReq);

                            if (validateOrderReq.getItems().isEmpty()) {
                                return OrderUtil.getEmptyDeliveryOrder(deliveryOrder);
                            }

                            validateOrderReq.setIdCustomerWebSafe(shoppingCartJson.getIdCustomerWebSafe());

                            Response<ValidateOrderBackend3> response = ApiGatewayService.get().validateOrder(validateOrderReq, TraceUtil.getXCloudTraceId(request));
                            if (!response.isSuccessful()){
                                response = ApiGatewayService.get().validateOrderV2(validateOrderReq, TraceUtil.getXCloudTraceId(request));
                            }
                            orderJSON = Objects.nonNull(response) && Objects.nonNull(response.body()) ? response.body().getData() : null;
                            Object item4 = Objects.nonNull(response) && Objects.nonNull(response.body()) ? response.body().getData() : null;
                            OrderUtil.validateResponseShoppingCart(orderJSON);
                        }else {
                            OrderUtil.setQuantityInOrder(deliveryOrder, deliveryOrderProviderListToSave, deliveryOrderItemList, isScanAndGo);
                            return deliveryOrder;
                        }

                        if (Objects.isNull(orderJSON)) {
                            if (APIAlgolia.getDeleteCartConfig()) {
                                CustomerEndpoint customerEndpoint = new CustomerEndpoint();
                                customerEndpoint.deleteShoppingCart("12345", shoppingCartJson.getId());
                            }
                            return new DeliveryOrder();
                        }

                        if ((Objects.isNull(orderJSON.getItems()) && Objects.isNull(orderJSON.getProviders())) ||
                                (Objects.nonNull(orderJSON.getItems()) && orderJSON.getItems().isEmpty() && Objects.nonNull(orderJSON.getProviders()) && orderJSON.getProviders().isEmpty())) {
                            return OrderUtil.getDeliveryOrderEmpty(shoppingCartJson, tipConfigOptional, userKey, deliveryOrder);
                        }

                        Key<DeliveryOrder> deliveryOrderKey = OrderService.getDeliveryOrderKey(userKey, deliveryOrder.getIdDeliveryOrder());
                        deliveryOrder.setSubTotalPrice(0d);
                        deliveryOrder.setOfferPrice(0d);

                        for (ItemAlgolia itemOrder : orderJSON.getItems()) {
                            ItemAlgolia itAlgolia = APIAlgolia.getItemAlgolia(itemOrder.getId() + "" + validateOrderReq.getStoreId());
                            OrderUtil.validateFilterToItemsAlgolia(itemOrder, itAlgolia);
                            OrderUtil.addDeliveryItemOrder(itemOrder, shoppingCartJson, deliveryOrderKey, deliveryOrderItemListToSave, deliveryOrder, null, deliveryOrderItemList);
                        }
                        // Agregar Coupon
                        deliveryOrderItemList.stream().filter(item -> item.getCoupon() != null && item.getCoupon()).forEach(item -> deliveryOrderItemListToSave.add(item));
                        if (Objects.nonNull(orderJSON.getProviders()) && !orderJSON.getProviders().isEmpty()) {
                            OrderUtil.validateItemsProvider(shoppingCartJson, deliveryOrderProviderListToSave, deliveryOrderItemList, deliveryOrder, orderJSON, deliveryOrderKey);
                        }

                        RequestSourceEnum source = ftdUtilities.getSourceFromRequestHeader(request);

                        // Set delivery value if applies for free delivery.
                        if (OrderUtil.checkIfAppliesForFreeDelivery(deliveryOrder, deliveryOrderItemList, shoppingCartJson, source.name(), deliveryOrderProviderListToSave)) {
                            orderJSON.setDeliveryValue(0);
                        } else if (OrderUtil.isOrderOnlyPrime(deliveryOrderItemList, deliveryOrderProviderList)){
                            orderJSON.setDeliveryValue(0);
                        } else {
                            LOG.info("El pedido NO APLICA para envio gratis.");
                        }

                        OrderUtil.setResponsePriceDelivery(deliveryOrderProviderListToSave, deliveryOrderItemList, deliveryOrder, orderJSON);
                    }else {
                        for (DeliveryOrderItem item : deliveryOrderItemList) {
                            if (item.getCoupon() != null && item.getCoupon()) {
                                deliveryOrderItemListToSave.add(item);
                            }
                        }
                        deliveryOrder = OrderUtil.getEmptyDeliveryOrder(deliveryOrder);

                    }

                    OrderUtil.deleteDeliveryOrderDuplicates(deliveryOrderItemList, deliveryOrderProviderListToSave);
                    OrderUtil.restrictItemsAndSave(deliveryOrderItemListToSave, shoppingCartJson.getIdStoreGroup());

                    OrderService.saveDeliveryOrder(deliveryOrder);
                    deliveryOrder.setItemList(deliveryOrderItemListToSave);
                    deliveryOrder.setProviderList(deliveryOrderProviderListToSave);

                    // add delivery time optics
                    if(!deliveryOrder.getProviderList().isEmpty()){
                        OpticsServices opticsServices = new OpticsServices();
                        int mainIdStore = APIAlgolia.getDefaultStoreIdByStoreId(shoppingCartJson.getIdStoreGroup());
                        opticsServices.getDeliveryTimeOpticsShoppingCart(deliveryOrder, mainIdStore);
                        try {
                            OrderUtil.setOpticalItemFiltersProvider(deliveryOrder.getProviderList());
                        } catch (Exception e) {
                            LOG.warning("Ocurrio un error seteando los filtros de optica");
                        }
                    }
                }
            }
            ;
            deliveryOrder.setDeliveryType(shoppingCartJson.getDeliveryType());
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

            OrderEndpointValidation orderEndpointValidation = new OrderEndpointValidation();
            boolean isUserPrime = OrderUtil.validateUserPrime(Long.valueOf(shoppingCartJson.getId()));
            boolean hasPrimeDiscount = orderEndpointValidation.isPrimeDiscountFlag(
                    isUserPrime,
                    deliveryOrderItemList
            );

            deliveryOrder.setPrimeDiscountFlag(hasPrimeDiscount);

            try {
                LOG.info("Response ->>>>> " + new Gson().toJson(deliveryOrder));
                String keyCache = shoppingCartJson.getIdCustomerWebSafe() + Constants.KEY_COUPON_CACHE;
                AnswerDeduct coupon = experienceCoupons.existCouponInRedis(shoppingCartJson.getIdCustomerWebSafe(), keyCache, Constants.INDEX_REDIS_FOURTEEN);
                TalonOneService talonOneService = new TalonOneService();
                deliveryOrder = talonOneService.sendOrderToTalonOne(deliveryOrder, shoppingCartJson, coupon);
                experienceCoupons.deductDiscount(shoppingCartJson.getIdCustomerWebSafe(), deliveryOrder, coupon);
            } catch (Exception e){
                LOG.warning("Error total de Talon One" + Arrays.toString(e.getStackTrace()));
            }

            try{
                deliveryOrder = OrderUtil.addFreeItemTalonOne(shoppingCartJson, userKey, deliveryOrder);
            }catch(Exception e){
                LOG.warning("Error FreeItem Talon One:"+Arrays.toString(e.getStackTrace()));
            }

            if (Objects.nonNull(deliveryOrder) && Objects.nonNull(deliveryOrder.getItemList())) {
                if (OrderUtil.isOnlyPrimeForDeliveryOrder(deliveryOrder)) {
                    deliveryOrder.setDeliveryType(DeliveryType.EXPRESS);
                }
            }

            try {
                new FarmaCredits().calculateNewPriceWithCredits(deliveryOrder, (long) shoppingCartJson.getId());
            } catch (Exception e) {
                LOG.warning("Error al calcular el precio con creditos: " + e.getMessage());
            }

            return deliveryOrder;

        }
        catch (Exception e) {
            LOG.warning("method priceDelivery: Error Message: " + e.getMessage());
            LOG.warning("method priceDelivery: Error Cause: " + e.getCause());
            try {
                OrderUtil.sendPriceDeliveryMessage(shoppingCartJson);
                if (APIAlgolia.getDeleteCartConfig()) {
                    CustomerEndpoint customerEndpoint = new CustomerEndpoint();
                    customerEndpoint.deleteShoppingCart("12345", shoppingCartJson.getId());
                }
            } catch (Exception ex) {
                LOG.warning("method priceDelivery: No fue posible envia la notificacion de SMS: " + ex.getMessage());
            }
            throw new ConflictException("Error al realizar el calculo en el carrito v2.");
        }
    }
}
