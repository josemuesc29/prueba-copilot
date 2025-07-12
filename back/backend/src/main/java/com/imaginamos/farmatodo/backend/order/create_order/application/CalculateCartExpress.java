package com.imaginamos.farmatodo.backend.order.create_order.application;

import com.algolia.search.exceptions.AlgoliaException;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.ConflictException;
import com.google.gson.Gson;
import com.googlecode.objectify.Key;
import com.imaginamos.farmatodo.backend.algolia.GuardAlgolia;
import com.imaginamos.farmatodo.backend.customer.CustomerEndpoint;
import com.imaginamos.farmatodo.backend.order.create_order.domain.*;
import com.imaginamos.farmatodo.backend.order.create_order.infraestructure.OrderEndpointValidation;
import com.imaginamos.farmatodo.backend.util.ResilienceManager;
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
import com.imaginamos.farmatodo.networking.talonone.FarmaCredits;
import com.imaginamos.farmatodo.networking.talonone.TalonOneService;
import retrofit2.Response;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.vavr.control.Try;

import java.util.function.Supplier;

import javax.servlet.http.HttpServletRequest;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.imaginamos.farmatodo.backend.OfyService.ofy;

public class CalculateCartExpress implements  PriceDeliveryOrder{
    private static final Logger LOG = Logger.getLogger(PriceDeliveryOrder.class.getName());
    public static final String APP_BUILD_CODE_HEADER_NAME = "appbuildcode";

    private final FTDUtilities ftdUtilities;
    private final ExperienceCoupons experienceCoupons;

    public CalculateCartExpress(){
        experienceCoupons = new ExperienceCoupons();
        ftdUtilities = new FTDUtilities();
    }
    @Override
    public DeliveryOrder priceDeliveryOrder(ShoppingCartJson shoppingCartJson, HttpServletRequest request) throws ConflictException, BadRequestException, AlgoliaException {
        try {
            if (Guard.isDeliveryTypePresent(shoppingCartJson)){
                shoppingCartJson.setDeliveryType(DeliveryType.EXPRESS);
            }

            Guard.againstInvalidTokens(shoppingCartJson.getToken(), shoppingCartJson.getTokenIdWebSafe(), Constants.ID_CUSTOMER_ANONYMOUS);

            Optional<TipConfig> tipConfigOptional = GrowthBookConfigLoader.getTipsConfig(Constants.CUSTOMER_ANONYMOUS);
            final List<DeliveryOrderItem> deliveryOrderItemListToSave = new ArrayList<>();
            List<DeliveryOrderProvider> deliveryOrderProviderListToSave = new ArrayList<>();
            List<DeliveryOrderItem> deliveryOrderItemList = new ArrayList<>();
            double shippingCostTotal = 0;

            shoppingCartJson.setIdStoreGroup(APIAlgolia.getDefaultStoreIdByStoreId(shoppingCartJson.getIdStoreGroup()));

            final boolean isScanAndGo  = OrderUtil.isScanAndGo(shoppingCartJson.getDeliveryType());

            LOG.info("Obtener el customerKey, userKey y deliveryOrder");

            final Key<Customer> customerKey = OrderService.getCustomerKeyFromIdCustomerWebSafe(shoppingCartJson.getIdCustomerWebSafe());
            Key<User> userKey = OrderService.getUserKey(shoppingCartJson.getIdCustomerWebSafe());
            DeliveryOrder deliveryOrder = OrderService.findActiveDeliveryOrderByRefCustomer(customerKey);

            LOG.info("Finalizo la busqueda de customerKey, userKey y deliveryOrder");

            if (deliveryOrder == null) {
                deliveryOrder = OrderUtil.getEmptyDeliveryOrder(new DeliveryOrder());
            }else {

                deliveryOrderItemList = OrderService.findItemsOf(deliveryOrder);

                validAndFixFullPriceNull(shoppingCartJson, deliveryOrderItemList);

                OrderService.deleteItemsSampling(deliveryOrderItemList);
                OrderUtil.validateDuplicateItems(deliveryOrderItemList);

                List<DeliveryOrderProvider> deliveryOrderProviderList = new ArrayList<>();
                if (!isScanAndGo){
                    int attempts = 0;
                    while (attempts < Constants.MAX_RETRIES) {
                        try {
                            deliveryOrderProviderList = OrderService.findDeliveryOrderProviderByDeliveryOrder(deliveryOrder);
                            break;
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
                }

                LOG.info("Finalizo la busqueda de deliveryOrderItemList y deliveryOrderProviderList");

                LOG.info("request.getHeader(APP_BUILD_CODE_HEADER_NAME); -> " + request.getHeader(APP_BUILD_CODE_HEADER_NAME));
                String buildCodeNumberApp = request.getHeader(APP_BUILD_CODE_HEADER_NAME);
                int validBuildCodeNumberApp = OrderUtil.validBuildCodeNumberApp(buildCodeNumberApp);
                OrderUtil.validateRequestedQuantityVsRealStockInStore(deliveryOrder, shoppingCartJson.getIdStoreGroup());

                if (tipConfigOptional.isPresent() && !shoppingCartJson.getDeliveryType().equals(DeliveryType.EXPRESS) ||
                        OrderUtil.notAllowedTips(Long.valueOf(shoppingCartJson.getId())) && tipConfigOptional.isPresent()) {
                    OrderUtil.deleteTips(tipConfigOptional.get(), deliveryOrder, userKey);
                }

                DeliveryOrder finalDeliveryOrder = deliveryOrder;
                tipConfigOptional.ifPresent(tipConfig -> { OrderUtil.fixTipMaxQuantity(tipConfig, finalDeliveryOrder, userKey) .filter(tipAmount -> tipAmount > 0 && (shoppingCartJson.getTip() == null ||
                        shoppingCartJson.getTip() == 0)) .ifPresent(tipAmount -> { OrderUtil.deleteTips(tipConfig, finalDeliveryOrder, userKey);
                            shoppingCartJson.setTip(tipAmount.floatValue()); }); });

                OrderUtil.deleteTipsIfCityNotConfig(tipConfigOptional, deliveryOrder, userKey, shoppingCartJson.getIdStoreGroup());

                if (tipConfigOptional.isPresent() && shoppingCartJson.getTip() != null && shoppingCartJson.getTip() >= 0 && shoppingCartJson.getDeliveryType().equals(DeliveryType.EXPRESS)) {
                    boolean cartHaveTip = OrderUtil.addTipToOrder(tipConfigOptional.get(), shoppingCartJson, deliveryOrder, userKey);
                }

                if (Objects.isNull(deliveryOrderItemList) && Objects.isNull(deliveryOrderProviderList)) {
                    deliveryOrder =  OrderUtil.getEmptyDeliveryOrder(new DeliveryOrder());
                }

                if (deliveryOrderItemList.isEmpty() && deliveryOrderProviderList.isEmpty()) {
                    deliveryOrder = OrderUtil.getEmptyDeliveryOrder(new DeliveryOrder());
                } else {

                    deliveryOrderItemList = OrderUtil.deleteCouponExpired(deliveryOrder);

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
                            validateOrderReq.setDaneCodeCustomer(Objects.nonNull(shoppingCartJson.getDaneCodeCustomer()) ? shoppingCartJson.getDaneCodeCustomer() : "");
                            validateOrderReq.setAddressCustomer(Objects.nonNull(shoppingCartJson.getAddressCustomer()) ? shoppingCartJson.getAddressCustomer() : "");

                            if (shoppingCartJson.getNearbyStores() != null && !shoppingCartJson.getNearbyStores().isEmpty()) {
                                validateOrderReq.setNearbyStores(shoppingCartJson.getNearbyStores());
                            }

                            this.validBuildCodeNumberApp(buildCodeNumberApp, validateOrderReq);

                            LOG.info("Realiza la peticion a validateOrder  a shopping cart");

                            CircuitBreaker circuitBreaker = ResilienceManager.getCircuitBreaker(ResilienceManager.SHOPPING_CART_SERVICE);
                            Retry retry = ResilienceManager.getRetry(ResilienceManager.SHOPPING_CART_SERVICE);

                            // Función para llamar al servicio principal
                            Supplier<Response<ValidateOrderBackend3>> primaryServiceCall = CircuitBreaker.decorateSupplier(
                                    circuitBreaker,
                                    () -> {
                                        try {
                                            LOG.info("Intentando llamar al servicio principal validateOrder");
                                            return ApiGatewayService.get().validateOrder(validateOrderReq, TraceUtil.getXCloudTraceId(request));
                                        } catch (Exception e) {
                                            LOG.severe("Error en llamada al servicio principal validateOrder: " + e.getMessage());
                                            throw new RuntimeException(e);
                                        }
                                    }
                            );

                            // Función para llamar al servicio de respaldo
                            Supplier<Response<ValidateOrderBackend3>> fallbackServiceCall = () -> {
                                try {
                                    LOG.info("Intentando llamar al servicio de respaldo validateOrderV2 shopping-cart fail-over");
                                    return ApiGatewayService.get().validateOrderV2(validateOrderReq, TraceUtil.getXCloudTraceId(request));
                                } catch (Exception e) {
                                    LOG.severe("Error en llamada al servicio de respaldo validateOrderV2 shopping-cart fail-over: " + e.getMessage());
                                    throw new RuntimeException(e);
                                }
                            };

                            // Aplicar retry al servicio principal
                            Supplier<Response<ValidateOrderBackend3>> retryingServiceCall = Retry.decorateSupplier(
                                    retry,
                                    primaryServiceCall
                            );

                            // Ejecutar con fallback
                            Response<ValidateOrderBackend3> response;
                            try {
                                response = Try.ofSupplier(retryingServiceCall)
                                        .recover(throwable -> {
                                            LOG.warning("Servicio shopping-cart falló después de reintentos, usando fallback validateOrderV2: " + throwable.getMessage());
                                            return fallbackServiceCall.get();
                                        })
                                        .get();
                            } catch (Exception e) {
                                LOG.severe("Ambos servicios (validateOrder y validateOrderV2) fallaron: " + e.getMessage());
                                if (APIAlgolia.getDeleteCartConfig()) {
                                    CustomerEndpoint customerEndpoint = new CustomerEndpoint();
                                    customerEndpoint.deleteShoppingCart("12345", shoppingCartJson.getId());
                                }
                                return new DeliveryOrder();
                            }

                            orderJSON = Objects.nonNull(response) && Objects.nonNull(response.body()) ? response.body().getData() : null;

                            OrderUtil.validateResponseShoppingCart(orderJSON);
                            shippingCostTotal = Objects.nonNull(orderJSON) && Objects.nonNull(orderJSON.getShoppingCartCourierCost()) ? orderJSON.getShoppingCartCourierCost().getShippingCostTotal() : 0;

                            LOG.info("Finaliza la peticion a validateOrder  a shopping cart");

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
                        deliveryOrder.setShoppingCartCourierCost(orderJSON.getShoppingCartCourierCost());


                        for (ItemAlgolia itemOrder : orderJSON.getItems()) {
                            ItemAlgolia itAlgolia = APIAlgolia.getItemAlgolia(itemOrder.getId() + "" + validateOrderReq.getStoreId());
                            OrderUtil.validateFilterToItemsAlgolia(itemOrder, itAlgolia);
                            OrderUtil.addDeliveryItemOrder(itemOrder, shoppingCartJson, deliveryOrderKey, deliveryOrderItemListToSave, deliveryOrder, null, deliveryOrderItemList);
                        }
                        // Agregar Coupon
                        deliveryOrderItemList.stream().filter(item -> item.getCoupon() != null && item.getCoupon()).forEach(item -> deliveryOrderItemListToSave.add(item));


                        if (Objects.nonNull(orderJSON.getProviders()) && !orderJSON.getProviders().isEmpty()) {
                            OrderUtil.validateItemsProvider(shoppingCartJson, deliveryOrderProviderListToSave, deliveryOrderItemList, deliveryOrder, orderJSON, deliveryOrderKey);
                        }else{
                            int attempts = 0;
                            while (attempts < Constants.MAX_RETRIES) {
                                try {
                                    ofy().delete().entities(deliveryOrderProviderList);
                                    break;
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
                        }

                        RequestSourceEnum source = ftdUtilities.getSourceFromRequestHeader(request);

                        deliveryOrder.setPrimeDeliveryValue(String.valueOf(orderJSON.getDeliveryValue()));
                        // Set delivery value if applies for free delivery.
                        if (OrderUtil.checkIfAppliesForFreeDelivery(deliveryOrder, deliveryOrderItemList, shoppingCartJson, source.name(),deliveryOrderProviderListToSave)) {
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


                    OrderUtil.deleteTipPriceZero(deliveryOrderItemListToSave);

                    LOG.info("Guarda los items y los providers en el deliveryOrder");

                    saveDeliveryOrder(deliveryOrder);

                    deliveryOrder.setItemList(deliveryOrderItemListToSave);
                    deliveryOrder.setProviderList(deliveryOrderProviderListToSave);

                    // add delivery time optics
                    if(!deliveryOrder.getProviderList().isEmpty()){
                        OpticsServices opticsServices = new OpticsServices();
                        int mainIdStore = APIAlgolia.getDefaultStoreIdByStoreId(shoppingCartJson.getIdStoreGroup());
                        try {
                            opticsServices.getDeliveryTimeOpticsShoppingCart(deliveryOrder, mainIdStore);
                            OrderUtil.setOpticalItemFiltersProvider(deliveryOrder.getProviderList());
                        } catch (Exception e) {
                            LOG.warning("Ocurrio un error seteando los filtros de optica para el usuario" + shoppingCartJson.getTokenIdWebSafe()+ " Error ->" +  e.toString());
                        }
                    }
                }
            }
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

            LOG.info("Consultar el DeliveryTimeLabelTemplate del Carrito");

            try {
                DeliveryTimeLabelTemplate deliveryTimeLabelTemplate = OrderUtil.getCustomDeliveryTimeLabelTemplateForThisOrder(deliveryOrder);
                deliveryOrder.setDeliveryTimeLabel(deliveryTimeLabelTemplate);
            } catch (Exception e) {
                LOG.warning("No fue posible consultar la configuracion del DeliveryTimeLabelTemplate del Carrito: " + e);
            }

            OrderUtil.validateIfOrderOnlyCouponProviders(deliveryOrder);
            OrderUtil.setQuantityInOrder(deliveryOrder, deliveryOrderProviderListToSave, deliveryOrderItemList, isScanAndGo);

            //validar si es necesario enviar los barcode
            //OrderUtil.addBarcodeInShoppingCart(deliveryOrder.getItemList(), String.valueOf(shoppingCartJson.getIdStoreGroup()));

            boolean isFullProvider = OrderUtil.isFullProvider(deliveryOrder, tipConfigOptional);

            if (isFullProvider && tipConfigOptional.isPresent()) {
                OrderUtil.deleteTips(tipConfigOptional.get(), deliveryOrder, userKey);
                OrderUtil.hideTipItemList(deliveryOrder, tipConfigOptional);
            }

            //validacion para las ordenes provider quitar el cupon ya que no se debe aplicar
            if(isFullProvider){
                deliveryOrder.getProviderList().forEach(provider->{
                    List<DeliveryOrderItem> filteredItems = provider.getItemList().stream()
                            .filter(item -> Objects.isNull(item.getCoupon()))
                            .collect(Collectors.toList());
                    provider.setItemList(filteredItems);
                });
            }

            OrderEndpointValidation orderEndpointValidation = new OrderEndpointValidation();
            boolean isUserPrime = OrderUtil.validateUserPrime(Long.valueOf(shoppingCartJson.getId()));
            boolean hasPrimeDiscount = orderEndpointValidation.isPrimeDiscountFlag(
                    isUserPrime,
                    deliveryOrderItemList
            );

            deliveryOrder.setPrimeDiscountFlag(hasPrimeDiscount);

            OrderUtil.validateTips(shoppingCartJson, tipConfigOptional, deliveryOrderProviderListToSave, deliveryOrderItemList, isScanAndGo, deliveryOrder, isFullProvider);

            LOG.info("Se valida carrito en Talon One");

            try {
                LOG.info("Response ->>>>> " + new Gson().toJson(deliveryOrder));
                if(Objects.nonNull(deliveryOrder.getCoupon())){
                    deliveryOrder.setCoupon(null);
                }
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

            deliveryOrder.setTotalPrice(deliveryOrder.getTotalPrice() + shippingCostTotal);

            LOG.info("Calcula farmaCredits");

            try {
                new FarmaCredits().calculateNewPriceWithCredits(deliveryOrder, (long) shoppingCartJson.getId());
            } catch (Exception e) {
                LOG.warning("Error al calcular el precio con creditos: " + e.getMessage());
            }

            if (deliveryOrder.isPrimeDiscountFlag() && isUserPrime) {
                deliveryOrder.getItemList().stream()
                        .filter(item -> item.getPrimePrice() != null &&
                                item.getOfferPrice() != null &&
                                item.getPrimePrice().equals(item.getOfferPrice()))
                        .forEach(item -> item.setOfferPrice(0D));
            }
            return deliveryOrder;

        }
        catch (Exception e) {
            LOG.log(java.util.logging.Level.SEVERE, "method priceDelivery: ", e); // Logs the full stack trace
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

    private void saveDeliveryOrder(DeliveryOrder deliveryOrder) {
        final int MAX_RETRIES = 3;
        int attempts = 0;

        while (attempts < MAX_RETRIES) { // Cambiado de "<=" a "<"
            try {
                OrderService.saveDeliveryOrder(deliveryOrder);
                LOG.info("DeliveryOrder guardado exitosamente: " + deliveryOrder.getIdDeliveryOrder());
                return; // Éxito, salir del método
            } catch (Exception e) {
                attempts++;
                LOG.warning("Error de contención en el datastore al guardar el DeliveryOrder. Reintento " + attempts);
                if (attempts >= MAX_RETRIES) {
                    LOG.severe("No se pudo guardar el DeliveryOrder después de " + MAX_RETRIES + " intentos.");
                } else {
                    // Agregar espera exponencial entre reintentos
                    try {
                        long waitTime = Math.min(1000 * (long) Math.pow(2, attempts), 10000);
                        LOG.info("Esperando " + waitTime + "ms antes del siguiente reintento");
                        Thread.sleep(waitTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        LOG.severe("Interrupción durante espera entre reintentos: " + ie.getMessage());
                    }
                }
            }
        }
    }

    private static void validAndFixFullPriceNull(ShoppingCartJson shoppingCartJson, List<DeliveryOrderItem> deliveryOrderItemList) {
        if (Objects.isNull(deliveryOrderItemList)) {
            return;
        }
        if (!deliveryOrderItemList.isEmpty()) {
            for (DeliveryOrderItem deliveryOrderItem : deliveryOrderItemList) {
                if (Objects.nonNull(deliveryOrderItem) && Objects.isNull(deliveryOrderItem.getFullPrice())) {
                    LOG.warning("PriceDelivery: Item " + deliveryOrderItem.getId() + " no tiene FullPrice se obtiene de Algolia");
                    ItemAlgolia itemAlgolia = APIAlgolia.getItemAlgolia(deliveryOrderItem.getId() + "" + shoppingCartJson.getIdStoreGroup());
                    if (Objects.nonNull(itemAlgolia)) {
                        deliveryOrderItem.setFullPrice(itemAlgolia.getFullPrice());
                    }
                }
            }
        }
    }

    private  void validBuildCodeNumberApp(String buildCodeNumberApp, ValidateOrderReq validateOrderReq) {
        if (buildCodeNumberApp != null && buildCodeNumberApp.matches("^[1-9]\\d*$")) {
            validateOrderReq.setBuildCodeNumberApp(Integer.valueOf(buildCodeNumberApp));
        }
    }


}
