package com.imaginamos.farmatodo.backend.order.create_order.domain;

import com.algolia.search.exceptions.AlgoliaException;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.response.*;
import com.google.gson.Gson;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Ref;
import com.imaginamos.farmatodo.backend.Prime.PrimeUtil;
import com.imaginamos.farmatodo.backend.algolia.GuardAlgolia;
import com.imaginamos.farmatodo.backend.customer.CustomerEndpoint;
import com.imaginamos.farmatodo.backend.order.DateConstants;
import com.imaginamos.farmatodo.backend.order.create_order.infraestructure.OrderEndpoint;
import com.imaginamos.farmatodo.backend.product.ProductsMethods;
import com.imaginamos.farmatodo.backend.user.Users;
import com.imaginamos.farmatodo.model.algolia.*;
import com.imaginamos.farmatodo.model.algolia.cuponFilters.Campaign;
import com.imaginamos.farmatodo.model.algolia.cuponFilters.CouponFiltersConfig;
import com.imaginamos.farmatodo.model.algolia.cuponFilters.Variables;
import com.imaginamos.farmatodo.model.algolia.tips.DefaultTipsByCity;
import com.imaginamos.farmatodo.model.algolia.tips.ItemTip;
import com.imaginamos.farmatodo.model.algolia.tips.Tip;
import com.imaginamos.farmatodo.model.algolia.tips.TipConfig;
import com.imaginamos.farmatodo.model.coupon.Coupon;
import com.imaginamos.farmatodo.model.coupon.CustomerCoupon;
import com.imaginamos.farmatodo.model.customer.Customer;
import com.imaginamos.farmatodo.model.customer.CustomerAddressResponse;
import com.imaginamos.farmatodo.model.customer.CustomerOnlyData;
import com.imaginamos.farmatodo.model.customer.CustomerResponseCart;
import com.imaginamos.farmatodo.model.item.AddDeliveryOrderItemRequest;
import com.imaginamos.farmatodo.model.item.OpticalItemFilter;
import com.imaginamos.farmatodo.model.optics.ShipmentType;
import com.imaginamos.farmatodo.model.optics.StoreIdDefaultOptics;
import com.imaginamos.farmatodo.model.order.*;
import com.imaginamos.farmatodo.model.payment.PaymentTypeEnum;
import com.imaginamos.farmatodo.model.product.Item;
import com.imaginamos.farmatodo.model.order.ValidateOrderReq;
import com.imaginamos.farmatodo.model.provider.ProviderOrder;
import com.imaginamos.farmatodo.model.user.User;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.model.util.DeliveryType;
import com.imaginamos.farmatodo.model.util.URLConnections;
import com.imaginamos.farmatodo.networking.algolia.APIAlgolia;
import com.imaginamos.farmatodo.networking.growthbook.GrowthBookConfigLoader;
import com.imaginamos.farmatodo.networking.services.ApiGatewayService;
import com.imaginamos.farmatodo.networking.talonone.model.ExtendedBagPropertiesTalonOne;
import com.imaginamos.farmatodo.networking.util.Util;
import org.apache.velocity.runtime.directive.Break;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.springframework.util.ObjectUtils;
import retrofit2.Response;


import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import static com.imaginamos.farmatodo.backend.OfyService.ofy;

public class OrderUtil {
    private static final Logger LOG = Logger.getLogger(OrderUtil.class.getName());

    private OrderEndpoint orderEndpoint;

    private Users users;

    private PrimeUtil primeUtil;

    public OrderUtil() {
        orderEndpoint = new OrderEndpoint();
        primeUtil = new PrimeUtil();
        users = new Users();
    }

    public static boolean orderHasCoupon(List<DeliveryOrderItem> deliveryOrderItemList) {
        if (Objects.isNull(deliveryOrderItemList) || deliveryOrderItemList.isEmpty())
            return false;

        try {
            return deliveryOrderItemList.stream().anyMatch(it -> Objects.nonNull(it.getCoupon()) && it.getCoupon());
        } catch (Exception e) {
            return false;
        }
    }


    public static String couponNameOf(CustomerCoupon customerCoupon) {
        try {
            return customerCoupon.getCouponId().get().getName();
        } catch (Exception e) {
            return null;
        }
    }


    public static void validateCouponBySourceAndDeliveryType(String couponName, RequestSourceEnum source, String deliveryType) throws BadRequestException, ConflictException {

        Optional<CouponFiltersConfig> couponsFilter = APIAlgolia.getCouponFilterConfig();
        ;
        if (!couponsFilter.isPresent()) {
            throw new BadRequestException(Constants.ERROR_COUPON_DATA_FILTER);
        }
        if (couponsFilter.get().getCampaigns() == null) {
            throw new BadRequestException(Constants.ERROR_COUPON_DATA_FILTER);
        }
        for (Campaign campaign : couponsFilter.get().getCampaigns()) {
            if (verifyCampaingCoupon(campaign, couponName)) {
                if (!couponFilter(campaign, source, deliveryType)) {
                    throw new ConflictException(Constants.ERROR_COUPON_FILTER_ORDER);
                }
            }
        }
    }


    public static Boolean isScanAndGo(final DeliveryOrder order) {
        return Objects.nonNull(order) && Objects.nonNull(order.getDeliveryType()) && isScanAndGo(order.getDeliveryType().getDeliveryType());
    }


    public static Boolean isScanAndGo(final String deliveryType) {
        return Objects.nonNull(deliveryType) && !deliveryType.isEmpty() && DeliveryType.SCANANDGO.getDeliveryType().equals(deliveryType);
    }


    public static Boolean isScanAndGo(final DeliveryType deliveryType) {
        return Objects.nonNull(deliveryType) && isScanAndGo(deliveryType.getDeliveryType());
    }


    private static boolean verifyCampaingCoupon(Campaign campaign, String couponRequest) {
        if (campaign == null) {
            return false;
        }
        if (campaign.getActive() == null) {
            return false;
        }
        if (!campaign.getActive()) {
            return false;
        }
        if (campaign.getCoupons() == null) {
            return false;
        }
        for (String coupon : campaign.getCoupons()) {
            if (coupon == null) {
                return false;
            }
            if (!coupon.trim().equalsIgnoreCase(couponRequest.trim())) {
                return false;
            }
        }
        return true;
    }


    private static boolean couponFilter(Campaign campaign, RequestSourceEnum requestSourceEnum, String deliveryType) {
        boolean source = false;
        boolean couponExistsForDeliveryType = false;

        if (campaign.getVariables() == null) {
            return false;
        }
        for (Variables variable : campaign.getVariables()) {
            if (variable == null) {
                return false;
            }
            if (variable.getValues() == null) {
                return false;
            }
            if (variable.getKey() == null) {
                return false;
            }

            if (variable.getKey().trim().equalsIgnoreCase("SOURCE")) {
                source = couponForSource(variable, requestSourceEnum);
            }
            if (variable.getKey().trim().equalsIgnoreCase("DELIVERY_TYPE")) {
                couponExistsForDeliveryType = couponsForDt(variable, deliveryType);
            }
        }
        return source && couponExistsForDeliveryType;

    }


    private static boolean couponForSource(Variables variable, RequestSourceEnum requestSourceEnum) {
        if (variable.getValues() == null) {
            return false;
        }
        if (variable.getKey() == null) {
            return false;
        }
        for (String value : variable.getValues()) {
            if (value.trim().equalsIgnoreCase(requestSourceEnum.name())) {
                return true;
            }
        }
        return false;
    }


    private static boolean couponsForDt(Variables variable, String deliveryType) {
        if (variable.getValues() == null) {
            return false;
        }
        if (variable.getKey() == null) {
            return false;
        }
        if (variable.getKey().trim().equalsIgnoreCase("DELIVERY_TYPE")) {
            for (String value : variable.getValues()) {
                if (value.trim().equalsIgnoreCase(deliveryType)) {
                    return true;
                }
            }
        }
        return false;
    }


    public static void addMarcaCategorySubcategorieAndItemUrl(CreatedOrder orderJson) {
        try {
            for (int i = 0; i < orderJson.getItems().size(); i++) {
                final ItemAlgolia item = orderJson.getItems().get(i);
                final ItemAlgolia itemAlgolia = APIAlgolia.getItemAlgolia(item.getId() + "" + URLConnections.MAIN_ID_STORE);
                if (itemAlgolia != null) {
                    orderJson.getItems().get(i).setSubCategory(itemAlgolia.getSubCategory());
                    orderJson.getItems().get(i).setCategorie(itemAlgolia.getCategorie());
                    orderJson.getItems().get(i).setMarca(itemAlgolia.getMarca());
                    orderJson.getItems().get(i).setItemUrl(itemAlgolia.getItemUrl());
                }
            }
        } catch (Exception e) {
            LOG.severe("method: addMarcaCategorySubcategorieAndItemUrl(orderJson) Message: " + e.getMessage());
        }
    }


    public static void deleteItemCoupon(DeliveryOrder deliveryOrder, List<DeliveryOrderItem> deliveryOrderItemList) {
        if (deliveryOrder != null && deliveryOrder.getItemList() != null && deliveryOrderItemList != null && !deliveryOrder.getItemList().isEmpty() && !deliveryOrderItemList.isEmpty()) {
            deliveryOrder.getItemList().removeIf(it -> (it.getFullPrice() <= 0.0D || it.getFullPrice() <= 0D) && it.getCoupon() == null);
            deliveryOrderItemList.removeIf(itList -> (itList.getFullPrice() <= 0.0D || itList.getFullPrice() <= 0D) && itList.getCoupon() == null);
        }
    }


    public static CustomerCoupon getCustomerCouponByCustomerKey(final Key<Customer> customerKey) {
        try {
            final List<CustomerCoupon> customerCoupons = OrderService.getCustomerCouponsByCustomerKey(customerKey);
            if (customerCoupons != null && !customerCoupons.isEmpty()) {
                customerCoupons.sort(Comparator.comparing(CustomerCoupon::getUseTime));
                final int positionLastCupon = customerCoupons.size() - 1;
                final CustomerCoupon couponToRedim = customerCoupons.get(positionLastCupon);
                if (couponToRedim != null) {
                    return couponToRedim;
                }
                return null;
            }
            return null;
        } catch (Exception e) {
            LOG.warning("Error al obtener cupon cuando el cliente elimina todo el carrito... Mensaje: " + e.getMessage());
            return null;
        }

    }


    public static RequestSourceEnum getSourceFromRequestHeader(HttpServletRequest request) {
        if (request.getHeader("source") == null)
            return RequestSourceEnum.WEB;

        return RequestSourceEnum.valueOf(request.getHeader("source"));
    }


    public static boolean createOrderViaBackend3(CreateOrderRequestCore requestCore, DeliveryOrder order, CreatedOrder orderJSON, String traceId, String idCustomerWebSafe) {
        // TODO: Este metodo toca mucho a la infraestructura. Mejorar.
        Response<CreateOrderResponseBackend3> responseBck3;
        try {
            requestCore.setIdCustomerWebSafe(idCustomerWebSafe);
//            LOG.info("method createOrderViaBackend3: -> customerId" + requestCore.getCustomerId());
            responseBck3 = ApiGatewayService.get().createOrderBck3(requestCore, traceId);
//            LOG.info("IF (!responseBck3.isSuccessful()) : [" + (!responseBck3.isSuccessful()) + "]");
            if (!responseBck3.isSuccessful()) {
                String error = (responseBck3.errorBody() != null ? responseBck3.errorBody().string() : "code : " + responseBck3.code());
                LOG.info("Alerta!! no se pudo crear la orden -> " + error);

                CompletableFuture.runAsync(() -> {
                    try {
                        final AlertConfigMessage alertConfigMessage = APIAlgolia.getAlertMessage();
                        for (String number : alertConfigMessage.getPhoneNumbers()) {
                            CompletableFuture.runAsync(() -> Util.sendAlertCreateOrder(number,
                                    "BK3 - Usuario : " + order.getIdFarmatodo() + alertConfigMessage.getMessage() + " " + error));
                        }
                    } catch (AlgoliaException e) {
                        e.printStackTrace();
                    }
                });

                return false;

            } else {
                // success!
                CreateOrderResponseBackend3 createOrderResponseBackend3 = responseBck3.body();
                if (createOrderResponseBackend3 != null && createOrderResponseBackend3.getData() != null && createOrderResponseBackend3.getData().dataIsValid()) {
                    orderJSON.setId(Objects.requireNonNull(createOrderResponseBackend3.getData().getId()));
                    orderJSON.setCreateDate((createOrderResponseBackend3.getData().getCreateDate() == null) ? 0 : createOrderResponseBackend3.getData().getCreateDate());
                    orderJSON.setAddress(createOrderResponseBackend3.getData().getAddress());
                    orderJSON.setUpdateShopping(Objects.requireNonNull(createOrderResponseBackend3.getData().getUpdateShopping()));
                    orderJSON.setChangePaymentCreditCard(createOrderResponseBackend3.getData().getChangePaymentCreditCard());
                    if (Objects.nonNull(Objects.requireNonNull(createOrderResponseBackend3.getData()).getQrCode()))
                        orderJSON.setQrCode(createOrderResponseBackend3.getData().getQrCode());
                    return true;
                }

            }

        } catch (ServiceUnavailableException | UnauthorizedException | NotFoundException |
                 InternalServerErrorException |
                 IOException | BadRequestException | ConflictException e) {
            LOG.severe("Error al crear la orden! " + e.getMessage());
            CompletableFuture.runAsync(() -> sendFatalErrorAlert(order, e));
        }

        return false;

    }

    public static void sendFatalErrorAlert(DeliveryOrder order, Exception e) {
        try {
            AlertConfigMessage alertConfigMessage = APIAlgolia.getAlertMessage();
            alertConfigMessage.getPhoneNumbers().forEach(number ->
                    Util.sendAlertCreateOrder(number,
                            "Usuario : " + order.getIdFarmatodo() + alertConfigMessage.getMessage() + " " + e.getMessage()));
        } catch (Exception ex) {
            LOG.warning("Error, el mensaje de alerta no se pudo enviar. causa:" + e.getMessage());
        }
    }


    public static void createDeliveryOrderWithItems(Key<Customer> customerKey, DeliveryType deliveryType, final List<DeliveryOrderItem> items) {
        DeliveryOrder deliveryOrder = new DeliveryOrder();
        deliveryOrder.setIdDeliveryOrder(UUID.randomUUID().toString());
        deliveryOrder.setIdCustomer(Ref.create(customerKey));
        deliveryOrder.setCurrentStatus(1);
        deliveryOrder.setCreateDate(new Date());
        deliveryOrder.setDeliveryType(deliveryType);

        final Key<DeliveryOrder> newDeliveryOrderKey = OrderService.saveDeliveryOrder(deliveryOrder);
        saveDeliveryOrderItemsToOrder(items, newDeliveryOrderKey);
    }


    public static void saveDeliveryOrderItemsToOrder(final List<DeliveryOrderItem> itemsNewOrder, final Key<DeliveryOrder> keyDeliveryOrder) {
        itemsNewOrder.parallelStream().forEach(deliveryOrderItem -> {
            deliveryOrderItem.setIdDeliveryOrderItem(UUID.randomUUID().toString());
            deliveryOrderItem.setIdDeliveryOrder(Ref.create(keyDeliveryOrder));
            Key<DeliveryOrderItem> deliveryOrderItemKey = Key.create(keyDeliveryOrder, DeliveryOrderItem.class, deliveryOrderItem.getIdDeliveryOrderItem());
            deliveryOrderItem.setIdDeliveryOrderItemWebSafe(deliveryOrderItemKey.toWebSafeString());
        });
        OrderService.saveDeliveryOrderItems(itemsNewOrder);
    }

    public static DeliveryOrder getOrderMethod(DeliveryOrder deliveryOrder, @Named("idOrder") long idOrder, @Named("isAllOrders") final Boolean isAllOrders, final Boolean isGetOrder) throws ConflictException {

        if (deliveryOrder == null) {
            //LOG.info("No se encontro la orden, se procede buscar en DATASTORE");
            deliveryOrder = OrderService.findDeliveryOrderById(idOrder);
        }

        if (Objects.isNull(deliveryOrder))
            throw new ConflictException(Constants.DELIVERY_ORDER_NOT_FOUND);


        int idStoreGroupFromOrderDetail = 0;

        setNamePaymentType(deliveryOrder);

        final boolean isScanAndGo = DeliveryType.SCANANDGO.equals(deliveryOrder.getDeliveryType());
        final boolean notIsScanAndGo = !isScanAndGo;

//        LOG.info("deliveryOrder.getDeliveryPrice():" + deliveryOrder.getDeliveryPrice());
        // Consulta proveedores
        List<DeliveryOrderProvider> deliveryOrderProviderList = OrderService.findDeliveryOrderProviderByDeliveryOrder(deliveryOrder);
        deliveryOrder.setProviderList(deliveryOrderProviderList);
        List<DeliveryOrderItem> deliveryOrderItemList = new ArrayList<>();

        List<ItemsOrderDomain> itemsOrderDomainList = ApiGatewayService.get().getInfoItemsByIdOrder(idOrder);

        if (itemsOrderDomainList != null && isGetOrder) {
            deliveryOrderItemList = validateItems(itemsOrderDomainList);
        } else {
            deliveryOrderItemList = OrderService.findDeliveryOrderItemByDeliveryOrder(deliveryOrder);
        }

        //Fix items duplicados y items con stock distinto al facturado.
        if (!isAllOrders) {
            //Eliminando items duplicados.
            List<OrderQuantityItem> itemsBilled = ApiGatewayService.get().getQuantityItemsByIdOrder(idOrder);
            deleteDeliveryOrderDuplicates(deliveryOrderItemList, deliveryOrderProviderList);
            validateItemsBilled(deliveryOrderItemList, itemsBilled);
        }
        PrimeUtil primeUtil1 = new PrimeUtil();
        //FIX Elimina items que no sean del mismo tipode envio
        if (isScanAndGo) {
            deliveryOrderItemList.removeIf(item -> Objects.nonNull(item.getScanAndGo()) && !item.getScanAndGo()
                    && !primeUtil1.isItemPrime(item.getId()));
        } else {
            deliveryOrderItemList.removeIf(item -> Objects.nonNull(item.getScanAndGo()) && item.getScanAndGo());
        }

        //FIX
        validateItemsFromProviderInOrder(deliveryOrder, deliveryOrderProviderList, deliveryOrderItemList);

        setItemsAlgoliaPrimeAndFilter(deliveryOrderItemList);
        // totals
        generateOrderTotals(deliveryOrder, notIsScanAndGo);

        deliveryOrder.setItemList(deliveryOrderItemList);
        deliveryOrder.setCreatedDate(deliveryOrder.getCreateDate().getTime());

        // Campos nuevos proveedores
        deliveryOrder.setTotalDelivery(deliveryOrder.getDeliveryPrice() + deliveryOrder.getProviderDeliveryPrice());
        if (Objects.nonNull(deliveryOrderProviderList) && !deliveryOrderProviderList.isEmpty()) {
            //LOG.warning(" Asigna cantidad de Items de proveedor. Lenght: " + deliveryOrderProviderList.size());
            deliveryOrder.setQuantityProviders(
                    deliveryOrderProviderList.stream()
                            .filter(provider -> Objects.nonNull(provider.getItemList()) && !provider.getItemList().isEmpty())
                            .mapToInt(provider -> provider.getItemList().stream()
                                    .mapToInt(item -> item.getQuantitySold()).sum()).sum());
            //LOG.warning(" Información complementaria item");

            if (Objects.nonNull(deliveryOrder.getItemList()) && !deliveryOrder.getItemList().isEmpty()) {
                //LOG.warning("-- > Agrega información complementaria item");
                deliveryOrderProviderList.forEach(deliveryProvideOrder -> {
                            deliveryProvideOrder.getItemList().stream().filter(deliveryItem -> Objects.nonNull(deliveryItem)).forEach(deliveryItem -> {
                                int storeId = 26;

                                if(Objects.nonNull(deliveryItem.getOpticalFilter()) || Objects.nonNull(deliveryItem.getFiltersOptical())){
                                    Optional<StoreIdDefaultOptics> optionalStoreIdDefaultOptics = APIAlgolia.getStoreIdDefaultOptics();
                                    if(optionalStoreIdDefaultOptics.isPresent()){
                                        storeId = optionalStoreIdDefaultOptics.get().getDefaultStoreId();
                                    }
                                }
                                LOG.info("storeId GET ORDER METHOD WILL: " + storeId);
                                ProductsMethods.setInformationToAlgoliaByDeliveryItem(deliveryItem, deliveryItem.getIdStoreGroup() == 0 ? storeId : deliveryItem.getIdStoreGroup());
                            });
                        }
                );
            }
            //LOG.warning(" Asigna cantidad de Items de proveedor count: " + deliveryOrder.getQuantityProviders());
        } else {
            deliveryOrder.setQuantityProviders(0);
        }
        deliveryOrder.setQuantityFarmatodo(Objects.nonNull(deliveryOrderItemList) ? deliveryOrderItemList.stream().mapToInt(item -> item.getQuantitySold()).sum() : 0);
        deliveryOrder.setTotalQuantity(deliveryOrder.getQuantityFarmatodo() + deliveryOrder.getQuantityProviders());
        // Información complementaria Item
        //LOG.warning(" Información complementaria item");

        if (Objects.nonNull(deliveryOrder) && Objects.nonNull(deliveryOrder.getItemList()) && !deliveryOrder.getItemList().isEmpty()) {
            //LOG.warning("-- > Agrega información complementaria item");
            deliveryOrder.getItemList().stream().filter(deliveryItem -> Objects.nonNull(deliveryItem)).forEach(deliveryItem -> {
                ProductsMethods.setInformationToAlgoliaByDeliveryItem(deliveryItem, deliveryItem.getIdStoreGroup() == 0 ? Long.parseLong(URLConnections.MAIN_ID_STORE) : deliveryItem.getIdStoreGroup());
            });
            // Tomar la tienda del primer item de la orden.
            idStoreGroupFromOrderDetail = deliveryOrder.getItemList().get(0).getIdStoreGroup();
        }


        int idStoreGroup = Integer.parseInt(URLConnections.MAIN_ID_STORE);

        try {
            idStoreGroup = APIAlgolia.getDefaultStoreIdByStoreId(idStoreGroupFromOrderDetail);
        } catch (Exception e) {
            LOG.warning("No fué posible obtener el id de la tienda por defecto. No es grave, se asignará la " + URLConnections.MAIN_ID_STORE);
        }
        deliveryOrder.setIdStoreGroup(String.valueOf(idStoreGroup));

        //Set dynamic delivery Time Label...
        if (notIsScanAndGo) {
            try {
                DeliveryTimeLabelTemplate deliveryTimeLabelTemplate = getCustomDeliveryTimeLabelTemplateForThisOrder(deliveryOrder);
                deliveryOrder.setDeliveryTimeLabel(deliveryTimeLabelTemplate);
            } catch (Exception e) {
                LOG.warning("No fue posible consultar la configuracion del DeliveryTimeLabelTemplate del Carrito: " + e);
            }
        }

        if (Objects.requireNonNull(deliveryOrder).getIdFarmatodo() <= 0 && Objects.requireNonNull(deliveryOrder).getIdAddress() > 0) {
            CustomerAddressResponse responseAddress;
            try {
                responseAddress = ApiGatewayService.get().getCustomerByAddressId(deliveryOrder.getIdAddress());
                if (responseAddress != null && responseAddress.getData() != null && responseAddress.getData().getCustomerId() > 0) {
                    deliveryOrder.setIdFarmatodo(responseAddress.getData().getCustomerId());
                }
            } catch (IOException e) {
                LOG.warning("No fue posible asignar el idFarmatodo al objeto deliveryOrder. Mensaje: " + e.getMessage());
            }
        }

        if (!isAllOrders && !deliveryOrder.isSelfCheckout()) {
            try {
                //Agrega info del cliente para los courriers
                CustomerOnlyData customerOnlyData = ApiGatewayService.get().getCustomerOnlyById(deliveryOrder.getIdFarmatodo());
                if (Objects.nonNull(customerOnlyData.getFirstName()) && Objects.nonNull(customerOnlyData.getPhone())) {
                    deliveryOrder.setCustomerPhone(customerOnlyData.getPhone());
                    deliveryOrder.setCustomerName(customerOnlyData.getFirstName());
                }
            } catch (Exception e) {
                LOG.warning("Ocurrio un error consultando el cliente.");
            }
        }

        // Validate items not delivered-billed
        checkIfHasUnbilledItems(deliveryOrder);
        validateOrderCanceled(deliveryOrder);

        Optional<TipConfig> tipConfigOptional = GrowthBookConfigLoader.getTipsConfig(Constants.CUSTOMER_ANONYMOUS);

        if (Objects.nonNull(deliveryOrder.getDeliveryType()) && deliveryOrder.getDeliveryType().equals(DeliveryType.EXPRESS) && tipConfigOptional.isPresent()) {
            double tipPrice = getTipPriceForOrder(deliveryOrder, tipConfigOptional.get());

            Optional<DeliveryOrderItem> itemTipOpt = getItemTipInCart(deliveryOrder, tipConfigOptional.get());

            if (tipPrice > 0 && itemTipOpt.isPresent()) {
                deliveryOrder.setTipPrice(tipPrice);
            }
        }

        loadDeliveryOrderInDataStore(deliveryOrder);
        return deliveryOrder;
    }

    public static DeliveryOrderOms getOrderMethodv2(String idOrder) throws ConflictException, BadRequestException {
        Guard.isValidIdOrder(idOrder);

        DeliveryOrderOms deliveryOrderOms = ApiGatewayService.get().getOrderOMS(String.valueOf(idOrder));
        if (deliveryOrderOms == null) {
            throw new ConflictException(Constants.DELIVERY_ORDER_NOT_FOUND);
        }
        return deliveryOrderOms;
    }

    public static Date formatStringToDate(String dateToFormat) {
        final String ISO_8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        SimpleDateFormat sdf = new SimpleDateFormat(ISO_8601_FORMAT);
        if (dateToFormat == null) {
            throw new IllegalArgumentException("Date string no puede ser null");
        }
        try {
            return sdf.parse(dateToFormat);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Error al formatear la fecha: " + dateToFormat, e);
        }
    }

    private static void loadDeliveryOrderInDataStore(DeliveryOrder deliveryOrder) {
        final int MAX_RETRIES=2;
        int retries=0;
        boolean success=false;
        int waitTime = 100; // milliseconds
        while(!success && retries < MAX_RETRIES) {
            try {
                if (Objects.isNull(deliveryOrder.getDeliveryType())) {
                    deliveryOrder.setDeliveryType(DeliveryType.EXPRESS);
                    ofy().save().entity(deliveryOrder);
                }
                success=true;
            } catch (Exception e) {
                LOG.severe("No fue guardada la entidad DeliveryOrder en Datastore/error "+ e.getMessage());
                retries++;
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException ex) {
                    LOG.severe("error en tiempo de espera:"+ ex.getMessage());
                }
            }
        }
    }

    private static List<DeliveryOrderItem> validateItems(List<ItemsOrderDomain> itemsOrderDomainList) {

        List<DeliveryOrderItem> deliveryOrderItemList = new ArrayList<>();
        if (!itemsOrderDomainList.isEmpty()) {
            for (ItemsOrderDomain item : itemsOrderDomainList) {
                DeliveryOrderItem deliveryOrderItem = new DeliveryOrderItem();
                deliveryOrderItem.setQuantitySold(item.getQuantitySold());
                deliveryOrderItem.setAnywaySelling(item.isAnywaySelling());
                deliveryOrderItem.setFullPrice(item.getFullPrice());
                deliveryOrderItem.setGrayDescription(item.getGrayDescription());
                deliveryOrderItem.setHighlight(item.isHighlight());
                deliveryOrderItem.setMediaDescription(item.getMediaDescription());
                deliveryOrderItem.setMediaImageUrl(item.getMediaImageUrl());
                deliveryOrderItem.setOutstanding(item.isOutstanding());
                deliveryOrderItem.setSales(Long.parseLong(item.getSales()));
                deliveryOrderItem.setTotalStock(Integer.parseInt(String.valueOf(item.getTotalStock())));
                deliveryOrderItem.setId(Long.parseLong(item.getId()));
                deliveryOrderItem.setChangeQuantity(item.isChangeQuantity());
                deliveryOrderItem.setOnlyOnline(item.isOnlyOnline());
                deliveryOrderItem.setGeneric(item.isGeneric());
                deliveryOrderItemList.add(deliveryOrderItem);
            }
        }
        return deliveryOrderItemList;
    }


    private static void setItemsAlgoliaPrimeAndFilter(List<DeliveryOrderItem> deliveryOrderItemList) {
        for (DeliveryOrderItem itemOrder : deliveryOrderItemList) {
            ItemAlgolia itAlgolia = APIAlgolia.getItemAlgolia(itemOrder.getId() + "26");
            if (itAlgolia != null) {
                itemOrder.setColor(itAlgolia.getColor());
                itemOrder.setUrl(itAlgolia.getUrl());
                itemOrder.setFilter(itAlgolia.getFilter());
                itemOrder.setFilterType(itAlgolia.getFilterType());
                itemOrder.setFilterCategories(Objects.nonNull(itAlgolia.getFilterCategories()) ? itAlgolia.getFilterCategories(): "");
                itemOrder.setPrimePrice(Objects.nonNull(itAlgolia.getPrimePrice()) ? itAlgolia.getPrimePrice() : 0.0);
                itemOrder.setPrimeDescription(Objects.nonNull(itAlgolia.getPrimeDescription()) ? itAlgolia.getPrimeDescription() : "");
                itemOrder.setPrimeTextDiscount(Objects.nonNull(itAlgolia.getPrimeTextDiscount()) ? itAlgolia.getPrimeTextDiscount() : "");
            }
        }
    }


    public static void setNamePaymentType(DeliveryOrder order) {
        if (order.getPaymentType() != null) {
            Integer id = ((int) order.getPaymentType().getId());
            switch (id) {
                case 1:
                    order.getPaymentType().setName(PaymentTypeEnum.EFECTIVO.name());
                    break;
                case 2:
                    order.getPaymentType().setName(PaymentTypeEnum.DATAFONOS.name());
                    break;
                case 3:
                    order.getPaymentType().setName(PaymentTypeEnum.TRANSACCIONES_EN_LINEA.name());
                    break;
                case 6:
                    order.getPaymentType().setName(PaymentTypeEnum.PSE.name());
                    break;
                default:
                    order.getPaymentType().setName("");

            }
        }
    }

    public static void deleteDeliveryOrderDuplicates(List<DeliveryOrderItem> itemList, List<DeliveryOrderProvider> itemListProvider) {
        // items farmatodo
        if (itemList != null && !itemList.isEmpty()) {
            Set<Long> itemsAlreadySeen = new HashSet<>();
            itemList.removeIf(item -> !itemsAlreadySeen.add(item.getId()));
            Set<Long> itemsAlreadySend = new HashSet<>();
            itemList.stream().filter(item -> Objects.nonNull(item.getCoupon()) && item.getCoupon()).forEach(item -> {
                itemList.removeIf(itemCoupon -> itemCoupon.getId() == item.getId() && !itemsAlreadySend.add(itemCoupon.getId()));
            });
        } else if (itemListProvider != null && !itemListProvider.isEmpty()) {// items provider
            Set<Long> itemsProviderAlreadySeen = new HashSet<>();
            itemListProvider.stream().findFirst()
                    .ifPresent(itemsProvider -> itemsProvider
                            .getItemList()
                            .removeIf(item -> !itemsProviderAlreadySeen.add(item.getId())));
        }
    }


    private static void validateItemsBilled(List<DeliveryOrderItem> deliveryOrderItemListAux, List<OrderQuantityItem> itemsBilled) {
        try {
            if (!deliveryOrderItemListAux.isEmpty()) {
                deliveryOrderItemListAux.forEach(item -> {
                    itemsBilled.forEach(itemBuild -> {
                        if (item.getId() == itemBuild.getItemId()) {
                            item.setQuantitySold(itemBuild.getQuantity().intValue());
                        }
                    });
                });
            }
        } catch (Exception e) {
            LOG.warning("Ocurrio un error -> " + e.getMessage());
        }
    }


    private static void validateItemsFromProviderInOrder(DeliveryOrder deliveryOrder, List<DeliveryOrderProvider> deliveryOrderProviderList, List<DeliveryOrderItem> deliveryOrderItemList) {
        if (Objects.nonNull(deliveryOrderProviderList) && !deliveryOrderProviderList.isEmpty()) {
            // Sumatoria de valor de valor de entrega de proveedores
            deliveryOrder.setProviderDeliveryPrice(deliveryOrderProviderList.parallelStream()
                    .mapToDouble(DeliveryOrderProvider::getDeliveryPrice)
                    .sum());
            deliveryOrderItemList.removeIf(item -> deliveryOrderProviderList.stream()
                    .filter(provider ->
                            provider.getItemList().stream()
                                    .filter(itemProvider -> itemProvider.getId() == item.getId()).findAny().isPresent())
                    .findAny().isPresent());

            // Coloa el estado de la orden en el item
            deliveryOrderProviderList.stream().filter(providerOrder -> Objects.nonNull(providerOrder.getDeliveryStatus()) && providerOrder.getDeliveryStatus() > 0)
                    .forEach(providerOrder -> providerOrder.getItemList().forEach(itemProvider -> itemProvider.setDeliveryStatus(providerOrder.getDeliveryStatus())));
        }
    }

    public static Optional<Long> getFirstStatusOrderProviderActive(List<DeliveryOrderProvider> deliveryOrderProviderList, long idOrder) {

        Optional<Long>  firstStatusOrderProviderActiveOptional = Optional.empty();

        if (Objects.nonNull(deliveryOrderProviderList) && !deliveryOrderProviderList.isEmpty()) {

            firstStatusOrderProviderActiveOptional =   deliveryOrderProviderList.stream()
                  .map(deliveryOrderProvider ->
                       ApiGatewayService.get().getLastStatusOrderProvider(idOrder, deliveryOrderProvider.getId())

                  ).filter(firstStatusOrderProviderActive -> firstStatusOrderProviderActive != 14L && firstStatusOrderProviderActive !=  1L)
                    .findFirst();

        }

        return firstStatusOrderProviderActiveOptional;
    }


    private static void generateOrderTotals(DeliveryOrder deliveryOrder, boolean notIsScanAndGo) {
        try {
            GetOrderSumary orderSummary = ApiGatewayService.get().getOrderSumary(deliveryOrder.getIdOrder());

            if (Objects.isNull(orderSummary) || Objects.isNull(orderSummary.getData())) {
                // Realiza el calculo de totales de la orden
                deliveryOrder.setSubTotalPrice(Objects.nonNull(deliveryOrder.getSubTotalPrice()) ? deliveryOrder.getSubTotalPrice() : 0);
                deliveryOrder.setOfferPrice(Objects.nonNull(deliveryOrder.getOfferPrice()) ? deliveryOrder.getOfferPrice() : 0);
                deliveryOrder.setDeliveryPrice(Objects.nonNull(deliveryOrder.getDeliveryPrice()) ? deliveryOrder.getDeliveryPrice() : 0);
                deliveryOrder.setTotalPrice(deliveryOrder.getSubTotalPrice() - deliveryOrder.getOfferPrice() + (deliveryOrder.getDeliveryPrice()));
            } else {
                deliveryOrder.setTotalPrice(orderSummary.getData().getInvoiceValue());
                deliveryOrder.setSubTotalPrice(orderSummary.getData().getOrderValue());
                deliveryOrder.setOfferPrice(orderSummary.getData().getDiscountValue());
                deliveryOrder.setDeliveryPrice(orderSummary.getData().getDeliveryValue());
                if (deliveryOrder.getDeliveryType().equals(DeliveryType.EXPRESS) || deliveryOrder.getDeliveryType().equals(DeliveryType.NATIONAL) || deliveryOrder.getDeliveryType().equals(DeliveryType.PROVIDER)){
                    deliveryOrder.setProviderDeliveryPrice(Objects.nonNull(orderSummary.getData().getProviderDeliveryValue()) ? orderSummary.getData().getProviderDeliveryValue() : 0);
                }
            }

            if (notIsScanAndGo)
                validateTips(deliveryOrder);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void validateTips(DeliveryOrder deliveryOrder) {
        double tipPrice = 0D;
        Optional<TipConfig> tipConfigOptional = GrowthBookConfigLoader.getTipsConfig(Constants.CUSTOMER_ANONYMOUS);

        if (tipConfigOptional.isPresent()) {
            // add tip price/
            tipPrice = getTipPriceForOrder(deliveryOrder, tipConfigOptional.get());
        }

        if (tipPrice > 0 && deliveryOrder.getSubTotalPrice() > 0) {
            deliveryOrder.setSubTotalPrice(deliveryOrder.getSubTotalPrice() - tipPrice);
        }
    }

    public static double getTipPriceForOrder(DeliveryOrder deliveryOrder, TipConfig tipConfig) {


        if (tipConfig == null || tipConfig.getItemTips() == null || deliveryOrder.getItemList() == null) {
            return 0;
        }

        Optional<DeliveryOrderItem> itemTipOpt = getItemTipInCart(deliveryOrder, tipConfig);

        if (!itemTipOpt.isPresent()) {
            return 0;
        }

        return itemTipOpt.get().getFullPrice();

    }

    @NotNull
    public static Optional<DeliveryOrderItem> getItemTipInCart(DeliveryOrder deliveryOrder, TipConfig tipConfig) {

        if (tipConfig == null || tipConfig.getItemTips() == null) {
            return Optional.empty();
        }

        if (deliveryOrder == null || deliveryOrder.getItemList() == null || deliveryOrder.getItemList().isEmpty()) {
            return Optional.empty();
        }

        return deliveryOrder.getItemList().stream().filter(itemCart -> {
            Optional<ItemTip> itemTipInCartOpt = tipConfig
                    .getItemTips()
                    .stream()
                    .filter(itemTipAux -> itemTipAux != null
                            && itemTipAux.getItemId() != null
                            && itemTipAux.getItemId().longValue() == itemCart.getId())
                    .findFirst();

            return itemTipInCartOpt.isPresent();
        }).findFirst();
    }


    public static DeliveryTimeLabelTemplate getCustomDeliveryTimeLabelTemplateForThisOrder(final DeliveryOrder deliveryOrder) {
        Optional<DeliveryTimeLabelTemplate> optionalDeliveryTimeLabelTemplate = APIAlgolia.getDeliveryTimeLabelTemplate();
        DeliveryTimeLabelTemplate deliveryTimeLabelTemplate = optionalDeliveryTimeLabelTemplate.isPresent() ? optionalDeliveryTimeLabelTemplate.get() : null;

        if (deliveryTimeLabelTemplate != null) {
            String deliveryType;
            if (deliveryOrder == null || deliveryOrder.getDeliveryType() == null || deliveryOrder.getDeliveryType().getDeliveryType() == null) {
                deliveryType = "EXPRESS";
            } else {
                deliveryType = deliveryOrder.getDeliveryType().getDeliveryType();
            }
            Map<String, String> timeAndLabel = getLabelAndTimeByDeliveryType(deliveryType);
            final String time = timeAndLabel.get("TIME");
            final String label = timeAndLabel.get("LABEL");
            final String TAG_DELIVERY_TYPE = "{DELIVERY_TYPE}";
            final String TAG_DELIVERY_TIME = "{DELIVERY_TIME}";

            //Para Web:
            final String cartLabel = deliveryTimeLabelTemplate.getDeliveryTimeLabelWeb().getCartLabel().replace(TAG_DELIVERY_TYPE, label);
            deliveryTimeLabelTemplate.getDeliveryTimeLabelWeb().setCartLabel(cartLabel);

            final String cartLabelTime = deliveryTimeLabelTemplate.getDeliveryTimeLabelWeb().getCartLabelTime().replace(TAG_DELIVERY_TIME, time);
            deliveryTimeLabelTemplate.getDeliveryTimeLabelWeb().setCartLabelTime(cartLabelTime);

            final String summaryLabel = deliveryTimeLabelTemplate.getDeliveryTimeLabelWeb().getSummaryLabel().replace(TAG_DELIVERY_TYPE, label);
            deliveryTimeLabelTemplate.getDeliveryTimeLabelWeb().setSummaryLabel(summaryLabel);

            //para Apps:
            final String cartLabelApp = deliveryTimeLabelTemplate.getDeliveryTimeLabelMobile().getCartLabel().replace(TAG_DELIVERY_TYPE, label);
            deliveryTimeLabelTemplate.getDeliveryTimeLabelMobile().setCartLabel(cartLabelApp);

            final String cartLabelTimeApp = deliveryTimeLabelTemplate.getDeliveryTimeLabelMobile().getCartLabelTime().replace(TAG_DELIVERY_TIME, time);
            deliveryTimeLabelTemplate.getDeliveryTimeLabelMobile().setCartLabelTime(cartLabelTimeApp);

            final String summaryLabelApp = deliveryTimeLabelTemplate.getDeliveryTimeLabelMobile().getSummaryLabel().replace(TAG_DELIVERY_TYPE, label);
            deliveryTimeLabelTemplate.getDeliveryTimeLabelMobile().setSummaryLabel(summaryLabelApp);

        }
        return deliveryTimeLabelTemplate;
    }


    private static Map<String, String> getLabelAndTimeByDeliveryType(final String deliveryType) {
        return APIAlgolia.getDeliveryTimeForDeliveryTypeObject() .map(deliveryTimeForDeliveryTypeObject -> getMapByDeliveryType(deliveryTimeForDeliveryTypeObject, deliveryType)) .orElseGet(HashMap::new); // Devuelve un hashmap vacío si el Optional está vacío o nulo

    }


    private static Map<String, String> getMapByDeliveryType(DeliveryTimeForDeliveryTypeObject deliveryTimeForDeliveryTypeObject, final String deliveryType) {
        Map<String, String> result = new HashMap<>();

        deliveryTimeForDeliveryTypeObject.getDeliveryTimeForDeliveryType().stream()
                .filter(element -> Objects.nonNull(element) && Objects.nonNull(element.getType()))
                .forEach(element -> {
//                    LOG.info(element.getType() + "==" + deliveryType);
                    if (element.getType().equalsIgnoreCase(deliveryType)) {
                        result.put("TIME", element.getTime());
                        result.put("LABEL", element.getLabel());
                    }
                });
        return result;
    }


    /**
     * Check if there are items not billed.
     *
     * @param deliveryOrder
     */
    private static void checkIfHasUnbilledItems(DeliveryOrder deliveryOrder) {
        try {
            if (Objects.nonNull(deliveryOrder)) {
//                LOG.info("checkIfHasUnbilledItems(" + deliveryOrder.getIdOrder() + ")");

                final List<Long> itemsNotBilled = getUnbilledItemsByOrderId(deliveryOrder.getIdOrder());

                if (Objects.nonNull(itemsNotBilled) && !itemsNotBilled.isEmpty()) {
                    final String messageForItemsNotBilled = APIAlgolia.getMessageForItemsNotBilled();
                    List<DeliveryOrderItem> requestedItems = deliveryOrder.getItemList();

                    if (Objects.nonNull(requestedItems) && !requestedItems.isEmpty()) {
                        requestedItems.forEach(requestedItem -> {
                            if (itemsNotBilled.contains(requestedItem.getId()))
                                requestedItem.setBilled(false);
                        });
                    }
                    deliveryOrder.setMessageWhenHasItemsNotBilled(messageForItemsNotBilled);
                }
            }
        } catch (Exception e) {
            LOG.warning("Error (no fatal) when checking if the order has unbilled items.");
        }
    }


    /**
     * Obtener los items no facturados de una orden.
     *
     * @param orderId
     */
    private static List<Long> getUnbilledItemsByOrderId(final Long orderId) {
        if (Objects.isNull(orderId) || orderId <= 0)
            return new ArrayList<>();

        try {
            final Response<GetUnbilledItemsByOrderResponse> response = ApiGatewayService.get().getUnbilledItemsByOrder(orderId);
            return response.isSuccessful() ? response.body().getData().getItems() : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }


    private static void validateOrderCanceled(DeliveryOrder deliveryOrder) {
        if (Objects.nonNull(deliveryOrder) && Objects.nonNull(deliveryOrder.getLastStatus()) && OrderStatus.ORDER_CANCELED.equals(deliveryOrder.getLastStatus())) {
            try {
                ReadOrderResponseBackend3 orderResponseBackend3 = ApiGatewayService.get().getReadActiveOrder(deliveryOrder.getIdOrder());
                if (Objects.nonNull(orderResponseBackend3) && Objects.nonNull(orderResponseBackend3.getData()) && Objects.nonNull(orderResponseBackend3.getData().getPickingDate())) {
                    deliveryOrder.setPickingDate(new Date(orderResponseBackend3.getData().getPickingDate()));
                }
            } catch (IOException e) {
                LOG.info("No fue posible consultar las ordenes activas. " + e.getMessage());
            }
        }
    }


    public static void addRMSClassesToOrder(CreatedOrder orderJson) {
        try {
            for (int i = 0; i < orderJson.getOrderData().getItemList().size(); i++) {
                DeliveryOrderItem item = orderJson.getOrderData().getItemList().get(i);
                ItemAlgolia itemAlgolia = APIAlgolia.getItemAlgolia(item.getId() + "" + URLConnections.MAIN_ID_STORE);
                if (itemAlgolia != null) {
                    // add RMS business class
                    orderJson.getOrderData().getItemList().get(i).setRms_subclass(Objects.nonNull(itemAlgolia.getRms_subclass()) ? itemAlgolia.getRms_subclass() : "");
                    orderJson.getOrderData().getItemList().get(i).setRms_group(Objects.nonNull(itemAlgolia.getRms_group()) ? itemAlgolia.getRms_group() : "");
                    orderJson.getOrderData().getItemList().get(i).setRms_deparment(Objects.nonNull(itemAlgolia.getRms_deparment()) ? itemAlgolia.getRms_deparment() : "");
                    orderJson.getOrderData().getItemList().get(i).setRms_class(Objects.nonNull(itemAlgolia.getRms_class()) ? itemAlgolia.getRms_class() : "");
                }
            }
        } catch (Exception e) {
            LOG.severe("method: addMarcaCategorySubcategorieAndItemUrl(orderJson) Message: " + e.getMessage());
        }
    }


    public static DeliveryOrder getEmptyDeliveryOrder(DeliveryOrder deliveryOrder) {
        deliveryOrder.setSubTotalPrice(0);
        deliveryOrder.setOfferPrice(0);
        deliveryOrder.setDeliveryPrice(0);
        deliveryOrder.setRegisteredOffer(0);
        deliveryOrder.setTotalPrice(0);
        deliveryOrder.setQuantityFarmatodo(0);
        deliveryOrder.setWeight(0);
        return deliveryOrder;

    }

    public static void validateDuplicateItems(List<DeliveryOrderItem> deliveryOrderItemList) {
        if (Objects.nonNull(deliveryOrderItemList) && !deliveryOrderItemList.isEmpty()) {
            Map<Long, DeliveryOrderItem> listItemResult = new HashMap<>();
            deliveryOrderItemList.stream().filter(item -> Objects.nonNull(item)).forEach(item -> {
                if (listItemResult.containsKey(item.getId())) {
                    if (listItemResult.get(item.getId()).getQuantitySold() < item.getQuantitySold()) {
                        listItemResult.get(item.getId()).setQuantitySold(item.getQuantitySold());
                    }
                    int attempts = 0;
                    while (attempts < Constants.MAX_RETRIES) {
                        try {
                            ofy().delete().entities(item);
                            // Si llega aquí, la operación fue exitosa
                            break;
                        } catch (Exception e) {
                            attempts++;
                            LOG.warning("Error de contención en el datastore al delete().entities(item). Reintento " + attempts);

                            if (attempts >= Constants.MAX_RETRIES) {
                                LOG.severe("No se pudo ejecutar delete().entities(item) después de " + Constants.MAX_RETRIES + " intentos.");
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

                } else {
                    listItemResult.put(item.getId(), item);
                }
            });
            deliveryOrderItemList = listItemResult.values().stream().collect(Collectors.toList());
        }
    }


    public static void validateRequestedQuantityVsRealStockInStore(DeliveryOrder deliveryOrder, int idStoreGroup) {
        // Validar condiciones iniciales
        if (deliveryOrder == null || deliveryOrder.getItemList() == null ||
                deliveryOrder.getItemList().isEmpty() || idStoreGroup <= 0) {
            return;
        }

        // Obtener la lista de items una sola vez
        List<DeliveryOrderItem> items = deliveryOrder.getItemList();

        // Recorremos cada item para validar la cantidad solicitada contra el stock real
        for (DeliveryOrderItem orderItem : items) {
            // Construir la clave del item según el id y el idStoreGroup
            String key = orderItem.getId() + String.valueOf(idStoreGroup);

            // Si es posible, aquí podrías reemplazar llamadas individuales por una consulta en lote
            ItemAlgolia itemAlgolia = APIAlgolia.getItemAlgolia(key);
            if (itemAlgolia == null) {
                // Podrías registrar el hecho de que no se encontró el item y continuar
                continue;
            }

            int availableStock = itemAlgolia.getStock();
            int positiveStock = availableStock < 0 ? -availableStock : availableStock;

            // Validar y ajustar la cantidad solicitada si es mayor que el stock disponible
            int quantitySold = orderItem.getQuantitySold();
            if (quantitySold > positiveStock) {
                orderItem.setQuantitySold(positiveStock);
                orderItem.setQuantitySoldGreaterThanStock(true);
                orderItem.setMessageWhenQuantitySoldIsGreaterThanStock(
                        "Ha solicitado " + quantitySold + " unidades pero solo hay " + positiveStock);
            }

        }
        int attempts = 0;
        while (attempts < Constants.MAX_RETRIES) {
            try {
                ofy().save().entity(deliveryOrder);
                // Si llega aquí, la operación fue exitosa
                break;
            } catch (Exception e) {
                attempts++;
                LOG.warning("Error de contención en el datastore al save().entity(deliveryOrder);. Reintento " + attempts);

                if (attempts >= Constants.MAX_RETRIES) {
                    LOG.severe("No se pudo ejecutar save().entity(deliveryOrder); después de " + Constants.MAX_RETRIES + " intentos.");
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

    private static boolean validateInfoItemsAlgolia(ItemAlgolia itemAlgolia, List<DeliveryOrderItem> deliveryOrderItemList, long itemId, DeliveryOrder deliveryOrder) throws BadRequestException {
        if (GuardAlgolia.validationItemsAlgoliaCart(itemAlgolia)) {
            DeliveryOrderItem orderItem = deliveryOrderItemList
                    .stream()
                    .filter(item -> Objects.equals(item.getId(), itemId))
                    .findFirst().get();

            if (Objects.nonNull(orderItem)) {
                deliveryOrder.getItemList().remove(orderItem);
                ofy().delete().entities(orderItem);
                deliveryOrderItemList.remove(orderItem);
                return false;
            }
        }
        return true;
    }

    public static void deleteTips(TipConfig tipConfig, DeliveryOrder deliveryOrder, Key<User> customerKey) {
        if (tipConfig == null || tipConfig.getItemTips() == null || deliveryOrder == null || customerKey == null) {
            LOG.warning("Parámetros inválidos: tipConfig, deliveryOrder o customerKey son nulos.");
            return;
        }
        List<DeliveryOrderItem> deliveryOrderItemList = null;
        int attempts = 0;
        while (attempts < Constants.MAX_RETRIES) {
            try {
                deliveryOrderItemList = ofy().load().type(DeliveryOrderItem.class).ancestor(deliveryOrder).list();
                break;
            } catch (Exception e) {
                attempts++;
                LOG.warning("Error de contención en el datastore al deliveryOrderItemList. Reintento " + attempts);

                if (attempts >= Constants.MAX_RETRIES) {
                    LOG.severe("No se pudo ejecutar deliveryOrderItemList después de " + Constants.MAX_RETRIES + " intentos.");
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

        if (deliveryOrderItemList == null || deliveryOrderItemList.isEmpty()) {
            LOG.info("No se encontraron items en el pedido.");
            return;
        }

        deliveryOrderItemList.stream()
                .filter(itemCart -> tipConfig.getItemTips().stream()
                        .anyMatch(itemTip -> itemTip != null
                                && itemTip.getItemId() != null
                                && itemTip.getItemId()==itemCart.getId()))
                .forEach(itemCart -> {
                    LOG.info("Propina encontrada para el item -> " + itemCart.getId());
                    attemptToDeleteItem(deliveryOrder, itemCart.getId(), customerKey);
                });
    }

    private static void attemptToDeleteItem(DeliveryOrder deliveryOrder, long itemId, Key<User> customerKey) {
        final int MAX_RETRIES = 3;
        int attempts = 0;

        while (attempts <= MAX_RETRIES) {
            try {
                deleteItemInCart(deliveryOrder, itemId, customerKey);
                LOG.info("Item eliminado exitosamente -> " + itemId);
                return;
            } catch (ConcurrentModificationException e) {
                attempts++;
                LOG.warning("Error de contención en el datastore al eliminar el item -> " + itemId + ". Reintento " + attempts);
                if (attempts > MAX_RETRIES) {
                    LOG.severe("No se pudo eliminar el item después de " + MAX_RETRIES + " intentos -> " + itemId);
                }
            }
        }
    }

    public static void deleteItemInCart(DeliveryOrder deliveryOrder, long itemId, Key<User> customerKey) {
        if (deliveryOrder == null || itemId <= 0) {
            return;
        }
        Key<DeliveryOrder> deliveryOrderKey = Key.create(customerKey, DeliveryOrder.class, deliveryOrder.getIdDeliveryOrder());
        DeliveryOrderItem deliveryOrderItem = null;
        int attempts = 0;
        while (attempts < Constants.MAX_RETRIES) {
            try {
                deliveryOrderItem = ofy()
                        .load()
                        .type(DeliveryOrderItem.class)
                        .filter("idItem", Key.create(Item.class, String.valueOf(itemId)))
                        .ancestor(Ref.create(deliveryOrderKey))
                        .first().now();
                break;
            } catch (Exception e) {
                attempts++;
                LOG.warning("Error de contención en el datastore al deliveryOrderItemList. Reintento " + attempts);

                if (attempts >= Constants.MAX_RETRIES) {
                    LOG.severe("No se pudo ejecutar deliveryOrderItemList después de " + Constants.MAX_RETRIES + " intentos.");
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

        if (deliveryOrderItem == null) {
            LOG.warning("No se encuentra el item a eliminar , ITEM: " + itemId);
            return;
        }
        while (attempts < Constants.MAX_RETRIES) {
            try {
                ofy().delete().entity(deliveryOrderItem).now();
                break;
            } catch (Exception e) {
                attempts++;
                LOG.warning("Error de contención en el datastore al deliveryOrderItemList. Reintento " + attempts);

                if (attempts >= Constants.MAX_RETRIES) {
                    LOG.severe("No se pudo ejecutar deliveryOrderItemList después de " + Constants.MAX_RETRIES + " intentos.");
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

    public static Optional<Double> fixTipMaxQuantity(TipConfig tipConfig, DeliveryOrder deliveryOrder, Key<User> customerKey) {
        // si el carrito ya tiene propina y no viene valor, obtener el valor de esta y eliminar todas las propinas y agregar solo 1.
        if (deliveryOrder == null || tipConfig == null || tipConfig.getItemTips() == null) {
            return Optional.empty();
        }
        List<DeliveryOrderItem> deliveryOrderItemList = null;

        int attempts = 0;
        while (attempts < Constants.MAX_RETRIES) {
            try {
                deliveryOrderItemList = ofy().load().type(DeliveryOrderItem.class).ancestor(deliveryOrder).list();
                break;
            } catch (Exception e) {
                attempts++;
                LOG.warning("Error de contención en el datastore al deliveryOrderItemList. Reintento " + attempts);

                if (attempts >= Constants.MAX_RETRIES) {
                    LOG.severe("No se pudo ejecutar deliveryOrderItemList después de " + Constants.MAX_RETRIES + " intentos.");
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

        if (deliveryOrderItemList == null || deliveryOrderItemList.isEmpty()) {
            return Optional.empty();
        }
//      Devolver el precio de la propina que ya tiene agregada
        return deliveryOrderItemList.stream()
                .flatMap(itemCart -> tipConfig.getItemTips().stream()
                        .filter(itemTip -> itemTip != null && itemTip.getItemId() != null && itemCart.getId() == itemTip.getItemId())
                        .filter(itemTip -> itemCart.getQuantitySold() > 1)
                        .map(itemTip -> itemCart.getFullPrice()))
                .findFirst();

    }

    public static List<DeliveryOrderItem> deleteCouponExpired(DeliveryOrder deliveryOrder) {
        if (deliveryOrder == null) {
            return Collections.emptyList();
        }

        List<DeliveryOrderItem> deliveryOrderItemListDS = null;

        int attempts = 0;
        while (attempts < Constants.MAX_RETRIES) {
            try {
                deliveryOrderItemListDS = ofy()
                        .load()
                        .type(DeliveryOrderItem.class)
                        .ancestor(deliveryOrder)
                        .list();
                break;
            } catch (Exception e) {
                attempts++;
                LOG.warning("Error de contención en el datastore al deliveryOrderItemList. Reintento " + attempts);

                if (attempts >= Constants.MAX_RETRIES) {
                    LOG.severe("No se pudo ejecutar deliveryOrderItemList después de " + Constants.MAX_RETRIES + " intentos.");
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


        if (deliveryOrderItemListDS == null || deliveryOrderItemListDS.isEmpty()) {
            return deliveryOrderItemListDS;
        }

        // Lista para acumular los items que contienen cupones expirados
        List<DeliveryOrderItem> itemsToDelete = new ArrayList<>();
        Date now = new Date();

        for (DeliveryOrderItem item : deliveryOrderItemListDS) {
            Boolean hasCoupon = item.getCoupon();
            if (Boolean.TRUE.equals(hasCoupon)) {
                // Cargar el cupón correspondiente
                Coupon coupon = ofy().load()
                        .type(Coupon.class)
                        .filter("itemId", item.getIdItem())
                        .first()
                        .now();

                boolean isExpired = false;
                if (coupon != null && coupon.getExpirationDate() != null) {
                    // Convertir el Long a Date
                    Date expirationDate = new Date(coupon.getExpirationDate());
                    if (expirationDate.before(now)) {
                        isExpired = true;
                    }
                } else {
                    // Si no se encuentra el cupón o no tiene fecha de expiración, se considera expirado
                    isExpired = true;
                }

                if (isExpired) {
                    LOG.info("Cupón expirado detectado para el item: " + item.getIdItem()
                            + ". Se procederá a eliminarlo del carrito.");
                    itemsToDelete.add(item);
                } else {
                    LOG.info("Cupón válido para el item: " + item.getIdItem());
                }
            }
        }

        // Eliminar en lote los items que contienen cupones expirados
        if (!itemsToDelete.isEmpty()) {
            try {
                ofy().delete().entities(itemsToDelete).now();
                deliveryOrderItemListDS.removeAll(itemsToDelete);
            } catch (Exception e) {
                LOG.warning("Error al remover cupones expirados: " + e.getMessage());
            }
        }

        return deliveryOrderItemListDS;
    }

    public static boolean validateHasItemsScanAndGo(List<DeliveryOrderItem> deliveryOrderItemList) {
        return deliveryOrderItemList.stream().filter(deliveryOrderItem -> Objects.nonNull(deliveryOrderItem)
                && (Objects.isNull(deliveryOrderItem.getCoupon()) || !deliveryOrderItem.getCoupon())
                && (Objects.nonNull(deliveryOrderItem.getScanAndGo()) && deliveryOrderItem.getScanAndGo())).findFirst().isPresent();
    }

    public static boolean validateHasItems(List<DeliveryOrderItem> deliveryOrderItemList) {
        return deliveryOrderItemList.stream().filter(deliveryOrderItem -> Objects.nonNull(deliveryOrderItem)
                && (Objects.isNull(deliveryOrderItem.getCoupon()) || !deliveryOrderItem.getCoupon())).findFirst().isPresent();
    }



    public static void validateStockItems(List<DeliveryOrderItem> deliveryOrderItemList, ValidateOrderReq validateOrderReq, Key<DeliveryOrder> deliveryOrderKey) {
        for (int i = 0; i < validateOrderReq.getItems().size(); i++) {
            final int itemId = validateOrderReq.getItems().get(i).getItemId();

            final ItemAlgolia itemAlgolia = APIAlgolia.getItemAlgolia(itemId + "" + validateOrderReq.getStoreId());

            if (Objects.isNull(itemAlgolia.getTotalStock())) {
//                LOG.info("order item to delete total stock is null ->" + itemId + validateOrderReq.getStoreId());
                validateOrderReq.getItems().remove(i);

                DeliveryOrderItem orderItem = deliveryOrderItemList
                        .stream()
                        .filter(item -> Objects.equals(item.getId(), Long.valueOf(itemId)))
                        .findFirst().get();

                if (Objects.nonNull(orderItem)) {
                    ofy().delete().entities(orderItem);
                    deliveryOrderItemList.remove(orderItem);
                }

                continue;
            }
            final int quantitySold = validateOrderReq.getItems().get(i).getQuantityRequested();
            final int totalStock = itemAlgolia.getTotalStock();

//            LOG.info("IF ( quantitySold > totalStock ) : (" + quantitySold + " > " + totalStock + ") => [" + (quantitySold > totalStock) + "]");
            if (totalStock > 0 && quantitySold > totalStock) {
                try {
                    DeliveryOrderItem deliveryOrderItem = ofy().load().type(DeliveryOrderItem.class).filter("idItem", Key.create(Item.class, itemId)).ancestor(Ref.create(deliveryOrderKey)).first().now();
//                    LOG.info("deliveryOrderItem.getQuantitySold() : " + deliveryOrderItem.getQuantitySold());
                    deliveryOrderItem.setQuantitySold(totalStock);

                    ofy().save().entity(deliveryOrderItem);
                } catch (Exception e) {
                    LOG.warning("Error No grave. al actualizar el detalle de la orden. Mensaje: " + e.getMessage());
                }

                validateOrderReq.getItems().get(i).setQuantityRequested(totalStock);
            }
        }
    }

    public static void setQuantityInOrder(DeliveryOrder deliveryOrder, List<DeliveryOrderProvider> deliveryOrderProviderListToSave, List<DeliveryOrderItem> deliveryOrderItemList, boolean isScanAndGo) {

        if (!isScanAndGo) {
            deliveryOrderItemList = deliveryOrder.getItemList();
            if (Objects.nonNull(deliveryOrderItemList) && !deliveryOrderItemList.isEmpty()) {
                deliveryOrder.setQuantityFarmatodo(deliveryOrderItemList.stream().mapToInt(DeliveryOrderItem::getQuantitySold).sum());

            }
        } else {
            deliveryOrder.setDeliveryType(DeliveryType.SCANANDGO);
            deliveryOrderItemList = deliveryOrder.getItemList();
            if (Objects.nonNull(deliveryOrderItemList) && !deliveryOrderItemList.isEmpty()) {
                deliveryOrderItemList.removeIf(it -> (Objects.nonNull(it.getScanAndGo()) && !it.getScanAndGo()));
            }
            int quantitySum = 0;

            if (Objects.nonNull(deliveryOrderItemList) && !deliveryOrderItemList.isEmpty()) {
                quantitySum = deliveryOrderItemList.stream().mapToInt(
                        DeliveryOrderItem::getQuantitySold
                ).sum();
            }
            deliveryOrder.setQuantityFarmatodo(quantitySum);
            if (deliveryOrder != null && deliveryOrder.getItemList() != null && !deliveryOrder.getItemList().isEmpty()) {
                deliveryOrder.getItemList().forEach(item -> {
                    if (item.getTotalStock() < 1) {
                        item.setTotalStock(1000);
                    }
                });
            }


        }
        if (Objects.nonNull(deliveryOrderProviderListToSave) && !deliveryOrderProviderListToSave.isEmpty()) {
//            LOG.warning(" Asigna cantidad de Items de proveedor. ");
            deliveryOrder.setQuantityProviders(deliveryOrderProviderListToSave.stream().mapToInt(DeliveryOrderProvider::getQuantityItem).sum());
        } else {
            deliveryOrder.setQuantityProviders(0);
        }
        deliveryOrder.setQuantityFarmatodo(Objects.nonNull(deliveryOrderItemList) ? deliveryOrderItemList.stream().mapToInt(item -> item.getQuantitySold()).sum() : 0);
        //deliveryOrder.setQuantityFarmatodo(deliveryOrder.getQuantityFarmatodo() > 0 ? deliveryOrder.getQuantityFarmatodo() - deliveryOrder.getQuantityProviders() : deliveryOrder.getQuantityFarmatodo());
        deliveryOrder.setTotalQuantity(deliveryOrder.getQuantityFarmatodo() + deliveryOrder.getQuantityProviders());

    }

    public static void validateResponseShoppingCart(OrderJson orderJSON) throws ConflictException {
        if (Objects.isNull(orderJSON)) {
            LOG.info("El servicio [validateOrder] de validacion de orden devolvio una respuesta nula.");
            throw new ConflictException(Constants.NO_ADDED_ITEMS);
        }

        if ((Objects.isNull(orderJSON.getItems()) && Objects.isNull(orderJSON.getProviders())) ||
                (Objects.nonNull(orderJSON.getItems()) && orderJSON.getItems().isEmpty() && Objects.nonNull(orderJSON.getProviders()) && orderJSON.getProviders().isEmpty())) {
            throw new ConflictException(Constants.NO_ADDED_ITEMS);
        }
    }

    public static void validateFilterToItemsAlgolia(ItemAlgolia itemOrder, ItemAlgolia itAlgolia) {
        if (itAlgolia != null) {
            itemOrder.setColor(itAlgolia.getColor());
            itemOrder.setUrl(itAlgolia.getUrl());
            itemOrder.setCustomTag(itAlgolia.getCustomTag());
            itemOrder.setSupplier(Objects.nonNull(itAlgolia.getSupplier()) ? itAlgolia.getSupplier() : Constants.ITEM_ALGOLIA_WITHOUT_SUPPLIER);
            itemOrder.setFilter(itAlgolia.getFilter());
            itemOrder.setFilterType(itAlgolia.getFilterType());
            itemOrder.setFilterCategories(Objects.nonNull(itAlgolia.getFilterCategories()) ? itAlgolia.getFilterCategories(): "");
            itemOrder.setPrimePrice(Objects.nonNull(itAlgolia.getPrimePrice()) ? itAlgolia.getPrimePrice() : 0.0);
            itemOrder.setPrimeDescription(Objects.nonNull(itAlgolia.getPrimeDescription()) ? itAlgolia.getPrimeDescription() : "");
            itemOrder.setPrimeTextDiscount(Objects.nonNull(itAlgolia.getPrimeTextDiscount()) ? itAlgolia.getPrimeTextDiscount() : "");
            itemOrder.setRms_deparment(Objects.nonNull(itAlgolia.getRms_deparment()) ? itAlgolia.getRms_deparment() : "");
            itemOrder.setRms_class(Objects.nonNull(itAlgolia.getRms_class()) ? itAlgolia.getRms_class() : "");
            itemOrder.setRms_subclass(Objects.nonNull(itAlgolia.getRms_subclass()) ? itAlgolia.getRms_subclass(): "");
            itemOrder.setRms_group(Objects.nonNull(itAlgolia.getRms_group()) ? itAlgolia.getRms_group() : "");
            itemOrder.setRequirePrescriptionImage(Objects.nonNull(itAlgolia.isRequirePrescriptionImage()) ? itAlgolia.isRequirePrescriptionImage() : false);
        }
    }

    public static void addDeliveryItemOrder(final ItemAlgolia itemOrder,
                                            final ShoppingCartJson shoppingCartJson,
                                            final Key<DeliveryOrder> deliveryOrderKey,
                                            final List<DeliveryOrderItem> deliveryOrderItemListToSave,
                                            DeliveryOrder deliveryOrder,
                                            DeliveryOrderProvider providerOrder,
                                            final List<DeliveryOrderItem> deliveryOrderItemListOrigin) throws BadRequestException {
        if (itemOrder != null && itemOrder.getPrice() >= 0 && itemOrder.getItem() != 1053709L) {
            ItemAlgolia itemAlgolia = APIAlgolia.getItemAlgolia(itemOrder.getItem() + "" + shoppingCartJson.getIdStoreGroup());

            Item item = APIAlgolia.getItemToItemAlgolia(new Item(), itemOrder);

            if (item == null) {
                //LOG.warning("El item no se encuentra completo en algolia" + itemOrder);
                return;
            }
            DeliveryOrderItem deliveryOrderItem = new DeliveryOrderItem();
            deliveryOrderItem.setIdDeliveryOrderItem(UUID.randomUUID().toString());
            deliveryOrderItem.setIdItem(Key.create(Item.class, item.getItemId()));
            deliveryOrderItem.setCreateDate(new Date());
            deliveryOrderItem.setQuantitySold(itemOrder.getQuantityRequested());
            for (DeliveryOrderItem itOrigin : deliveryOrderItemListOrigin) {
                if (itOrigin.getId() == itemOrder.getItem()) {
                    if (itOrigin.getHandleQuantity() != null) {
                        deliveryOrderItem.setHandleQuantity(itOrigin.getHandleQuantity());
                    }

                    if (itOrigin.getFilters() != null) {
                        deliveryOrderItem.setFilters(itOrigin.getFilters());
                    }
                    break;
                }
            }
            if (itemOrder.isTalonDiscount() && itemOrder.isTalonItemFree()){
                item.setPrimePrice(0D);
                item.setPrimeDescription(null);
                item.setPrimeTextDiscount(null);
            }
            //LOG.warning("method: addDeliveryItemOrder : " + itemOrder);
            if (itemOrder.getPrice() != itemOrder.getDiscount()) {
                //LOG.warning("method: addDeliveryItemOrder FullPrice: " +itemOrder.getFullPrice()+" "+ itemOrder.getPrice());
                deliveryOrderItem.setFullPrice(itemOrder.getPrice());
            } else
                deliveryOrderItem.setFullPrice(0D);
            //LOG.warning("method: addDeliveryItemOrder : getDiscount" + itemOrder.getDiscount());
            if (itemOrder.getDiscount() != 0)
                deliveryOrderItem.setOfferPrice(itemOrder.getPrice() - (itemOrder.getDiscount() / itemOrder.getQuantityRequested()));
            else
                deliveryOrderItem.setOfferPrice(0D);
            deliveryOrderItem.setChangeQuantity(itemOrder.getAccess());
            // Prime
            deliveryOrderItem.setPrimePrice(item.getPrimePrice());
            deliveryOrderItem.setPrimeDescription(item.getPrimeDescription());
            deliveryOrderItem.setPrimeTextDiscount(item.getPrimeTextDiscount());
            deliveryOrderItem.setRms_deparment(Objects.nonNull(item.getRms_deparment()) ? item.getRms_deparment(): "" );
            deliveryOrderItem.setRms_class(Objects.nonNull(item.getRms_class()) ? item.getRms_class(): "");
            deliveryOrderItem.setRms_subclass(Objects.nonNull(item.getRms_subclass()) ? item.getRms_subclass(): "");
            deliveryOrderItem.setRms_group(Objects.nonNull(item.getRms_group()) ? item.getRms_group(): "");

            //Talon
            deliveryOrderItem.setTalonDiscount(itemOrder.isTalonDiscount());
            deliveryOrderItem.setTalonItemFree(itemOrder.isTalonItemFree());

            deliveryOrderItem = deliveryOrderItemReturn(deliveryOrderItem, item);

            //mark onlyOnline via Algolia
            if (Objects.nonNull(itemOrder.getColor())) {
                deliveryOrderItem.setColor(itemOrder.getColor());
            }
            if (Objects.nonNull(itemOrder.getFilter())) {
                deliveryOrderItem.setFilter(itemOrder.getFilter());
            }
            if (Objects.nonNull(itemOrder.getFilterType())) {
                deliveryOrderItem.setFilterType(itemOrder.getFilterType());
            }

            if (Objects.nonNull(itemOrder.getUrl())) {
                deliveryOrderItem.setUrl(itemOrder.getUrl());
            }
            if (Objects.nonNull(itemOrder.getFilterCategories())) {
                deliveryOrderItem.setFilterCategories(itemOrder.getFilterCategories());
            }

            if (Objects.nonNull(item.getCustomTag())) {
                deliveryOrderItem.setCustomTag(item.getCustomTag());
            }

            deliveryOrderItem.setOnlyOnline(item.isOnlyOnline());
            deliveryOrderItem.setDeliveryTime(item.getDeliveryTime());
            deliveryOrderItem.setCategorie(item.getCategorie());
            deliveryOrderItem.setMarca(item.getMarca());
            deliveryOrderItem.setSupplier(item.getSupplier());
            deliveryOrderItem.setDepartments(Objects.nonNull(itemAlgolia) ?
                    itemAlgolia.getDepartments() : item.getDepartments());
            deliveryOrderItem.setSubCategory(item.getSubCategory());
            deliveryOrderItem.setSupplier(item.getSupplier());
            deliveryOrderItem.setRequirePrescription(item.getRequirePrescription());
            deliveryOrderItem.setSubCategory(item.getSubCategory());
            deliveryOrderItem.setCategorie(item.getCategorie());
            deliveryOrderItem.setMarca(item.getMarca());
            deliveryOrderItem.setItemUrl(item.getItemUrl());
            // Busca el origen del Item
            if (Objects.nonNull(deliveryOrderItemListOrigin)) {
                final DeliveryOrderItem deliveryOrderFind = deliveryOrderItem;
                if (deliveryOrderItemListOrigin.stream()
                        .filter(itemOrigin -> Objects.nonNull(deliveryOrderFind.getId()) && Objects.nonNull(itemOrigin.getId()) &&
                                itemOrigin.getId() == deliveryOrderFind.getId()).findFirst().isPresent()) {
                    deliveryOrderItem.setOrigin(deliveryOrderItemListOrigin.stream().filter(itemOrigin -> itemOrigin.getId() == deliveryOrderFind.getId()).findFirst().get().getOrigin());
                    deliveryOrderItem.setObservations(deliveryOrderItemListOrigin.stream().filter(itemOrigin -> itemOrigin.getId() == deliveryOrderFind.getId()).findFirst().get().getObservations());
                    deliveryOrderItem.setSubstitute(deliveryOrderItemListOrigin.stream().filter(itemOrigin -> itemOrigin.getId() == deliveryOrderFind.getId()).findFirst().get().getSubstitute());
                }
            }

            if (Objects.nonNull(itemOrder.getFiltersOptical())){
                deliveryOrderItem.setFiltersOptical(itemOrder.getFiltersOptical());
            }
            if (Objects.nonNull(itemOrder.isRequirePrescriptionMedical())){
                deliveryOrderItem.setRequirePrescriptionMedical(itemOrder.isRequirePrescriptionMedical());
            }

            deliveryOrderItemListToSave.add(deliveryOrderItem);
            deliveryOrder.setSubTotalPrice(deliveryOrder.getSubTotalPrice() + itemOrder.getFullPrice());
            deliveryOrder.setOfferPrice(deliveryOrder.getOfferPrice() + itemOrder.getDiscount());
            if (Objects.nonNull(providerOrder)) {
                saveProviderItem(deliveryOrderKey, providerOrder, deliveryOrderItem);
            }

            //Data Marketplace
            deliveryOrderItem.setDimensions(itemOrder.getDimensions());

            if (Objects.nonNull(itemOrder.getVariants())) {
                deliveryOrderItem.setVariants(itemOrder.getVariants());
            }

            if(Objects.nonNull(itemOrder.getSellerAddresses())) {
                deliveryOrderItem.setSellerAddresses(itemOrder.getSellerAddresses());
            }


            if(Objects.nonNull(itemOrder.getUuidItem())) {
                deliveryOrderItem.setUuidItem(itemOrder.getUuidItem());
            }

            if(Objects.nonNull(itemOrder.getDeliveryDays())){
                deliveryOrderItem.setDeliveryDays(itemOrder.getDeliveryDays());
            }

            if(Objects.nonNull(itemOrder.getUuidItem())){
                deliveryOrderItem.setUuidItem(itemOrder.getUuidItem());
            }

            deliveryOrderItem.setRequirePrescriptionImage(itemOrder.isRequirePrescriptionImage());

        } else {
            deliveryOrder.setOfferPrice(deliveryOrder.getOfferPrice() + Math.abs(itemOrder.getPrice()));
        }
    }

    public static DeliveryOrderItem deliveryOrderItemReturn(DeliveryOrderItem deliveryOrderItem, Item item) {
        deliveryOrderItem.setAnywaySelling(item.isAnywaySelling());
        deliveryOrderItem.setBarcode(item.getBarcode());
        deliveryOrderItem.setBrand(item.getBrand());
        deliveryOrderItem.setId(item.getId());
        deliveryOrderItem.setGeneric(item.isGeneric());
        deliveryOrderItem.setGrayDescription(item.getGrayDescription());
        deliveryOrderItem.setMediaDescription(item.getMediaDescription());
        deliveryOrderItem.setHighlight(item.isHighlight());
        deliveryOrderItem.setMediaImageUrl(item.getMediaImageUrl());
        deliveryOrderItem.setOutstanding(item.isOutstanding());
        deliveryOrderItem.setTotalStock(item.getTotalStock());
        deliveryOrderItem.setOfferText(item.getOfferText());
        deliveryOrderItem.setOfferDescription(item.getOfferDescription());
        deliveryOrderItem.setIdStoreGroup(item.getIdStoreGroup());
        deliveryOrderItem.setItemUrl(item.getItemUrl());

        return deliveryOrderItem;
    }

    public static void saveProviderItem(final Key<DeliveryOrder> deliveryOrderKey, DeliveryOrderProvider providerOrder, final DeliveryOrderItem deliveryOrderItemBase) {
        String uuiKey = generateProviderKey(Key.create(deliveryOrderKey, DeliveryOrderProvider.class, Long.toString(providerOrder.getId())).toWebSafeString(),
                providerOrder.getId());
        Key<DeliveryOrderProvider> findKey = Key.create(deliveryOrderKey, DeliveryOrderProvider.class, uuiKey);
        DeliveryOrderProvider deliveryOrderProvider = ofy().load().type(DeliveryOrderProvider.class).filterKey("=", findKey).first().now();
        if (Objects.isNull(deliveryOrderProvider)) {
            providerOrder.setIdDeliveryOrder(Ref.create(deliveryOrderKey));
            providerOrder.setIdDeliveryOrderProvider(uuiKey);
            providerOrder.setItemList(new ArrayList<>());
        } else {
            providerOrder = deliveryOrderProvider;
        }
        // si el item existe se reemplaza por la ultima version
        Optional<DeliveryOrderItem> optionalDeliveryOrderItem = providerOrder.getItemList().stream()
                .filter(itemProvider -> itemProvider.getId() == deliveryOrderItemBase.getId()).findFirst();
        if (optionalDeliveryOrderItem.isPresent()) {
            providerOrder.getItemList().remove(optionalDeliveryOrderItem.get());
        }

        // Se agrega item al proveedor
        providerOrder.getItemList().add(deliveryOrderItemBase);

        int attempts = 0;
        while (attempts < Constants.MAX_RETRIES) {
            try {
                ofy().save().entity(providerOrder).now();
                break;
            } catch (Exception e) {
                attempts++;
                LOG.warning("Error de contención en el datastore al deliveryOrderItemList. Reintento " + attempts);

                if (attempts >= Constants.MAX_RETRIES) {
                    LOG.severe("No se pudo ejecutar deliveryOrderItemList después de " + Constants.MAX_RETRIES + " intentos.");
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

    public static String generateProviderKey(String providerKey, Long providerId) {
        return UUID.nameUUIDFromBytes((providerKey + providerId).getBytes()).toString();
    }

    public static void validateIfOrderOnlyCouponProviders(DeliveryOrder deliveryOrder) {

        if (deliveryOrder.getProviderList() != null && !deliveryOrder.getProviderList().isEmpty()) {
            boolean hasItemsExpress =
                    deliveryOrder.getItemList().stream()
                            .anyMatch(item -> item.getCoupon() == null || !item.getCoupon());

            if (!hasItemsExpress) {

                Optional<DeliveryOrderItem> optionalItemIsCoupon =
                        deliveryOrder.getItemList().stream()
                                .filter(item -> (item.getCoupon() != null && item.getCoupon()))
                                .findFirst();
                if (optionalItemIsCoupon.isPresent()) {
                    DeliveryOrderItem coupon = optionalItemIsCoupon.get();
                    deliveryOrder.getItemList().removeIf(DeliveryOrderItem::getCoupon);
                    deliveryOrder.getProviderList().stream()
                            .findFirst()
                            .ifPresent(deliveryOrderProvider -> deliveryOrderProvider.getItemList().add(coupon));
                    double totalPrice = deliveryOrder.getTotalPrice() - deliveryOrder.getDeliveryPrice();
                    deliveryOrder.setTotalPrice(totalPrice);
                    deliveryOrder.setDeliveryPrice(0);

                    if (deliveryOrder.getQuantityFarmatodo() > 0) deliveryOrder.setQuantityFarmatodo(0);
                }
            }
        }
    }

    public static void addBarcodeInShoppingCart(List<DeliveryOrderItem> deliveryOrderItemList, String idStoreGroup) {
        // add barcode Response
//        LOG.info("method: addBarcodeInShoppingCart INIT: " + new DateTime());
        if (Objects.nonNull(deliveryOrderItemList) && !deliveryOrderItemList.isEmpty()) {
            List<String> itemsId = new ArrayList<>();
            deliveryOrderItemList.forEach(itemCart -> itemsId.add(String.valueOf(itemCart.getId()).concat(idStoreGroup)));
            List<ItemAlgolia> itemsAlgoliaList = APIAlgolia.getItemsFromIds(itemsId);
            if (Stream.of(itemsAlgoliaList, deliveryOrderItemList).allMatch(Objects::nonNull) && !itemsAlgoliaList.isEmpty() && !deliveryOrderItemList.isEmpty()) {
                for (ItemAlgolia itemAlgolia : itemsAlgoliaList) {
                    for (DeliveryOrderItem deliveryOrderItem : deliveryOrderItemList) {
                        if ((Objects.isNull(deliveryOrderItem.getCoupon()) || !deliveryOrderItem.getCoupon()) &&
                                (Objects.nonNull(deliveryOrderItem) && Objects.nonNull(itemAlgolia)) &&
                                Stream.of(deliveryOrderItem, deliveryOrderItem.getId(), itemAlgolia, itemAlgolia.getId()).allMatch(Objects::nonNull)) {
                            //LOG.info("itemAlgolia -> " + itemAlgolia.getId());
                            if ((deliveryOrderItem.getId() == Long.parseLong(itemAlgolia.getId())) && (Objects.isNull(deliveryOrderItem.getCoupon()) || !deliveryOrderItem.getCoupon())) {
                                //LOG.info("barcode found -> " + itemAlgolia.getBarcode());
                                deliveryOrderItem.setBarcode(itemAlgolia.getBarcode());
                            }
                        }
                    }
                }
            }
        }
        LOG.info("method: addBarcodeInShoppingCart END: " + new DateTime());
    }

    public static boolean validateUserPrime(Long customerId) {
        boolean isPrime = false;
        try {
            PrimeConfig primeConfig = APIAlgolia.primeConfigV2();
            if (primeConfig.featureValidateUserPrime) {
                CustomerResponseCart customerResponseCart = ApiGatewayService.get().isCustomerPrime(customerId);
                if (customerResponseCart != null && customerResponseCart.isActive()) {
                    isPrime = true;
                }
            }
        } catch (Exception e) {
            LOG.info("No se pudo obtener el customer");
        }
        LOG.info("isPrime: " + isPrime);
        return isPrime;
    }

    @NotNull
    public static DeliveryOrder addFreeItemTalonOne(
            ShoppingCartJson shoppingCartJson, Key<User> customerKey, DeliveryOrder deliveryOrder) {

        if(deliveryOrder.isTalonOneItemFree()) {

            DeliveryOrder deliveryOrderData = null;
            List<DeliveryOrderItem> deliveryOrderItemListBefore = null;
            int attempts = 0;
            while (attempts < Constants.MAX_RETRIES) {
                try {
                    deliveryOrderData = ObjectifyService.ofy().load().type(DeliveryOrder.class)
                            .filter("currentStatus", 1)
                            .ancestor(Ref.create(customerKey)).first().now();

                    deliveryOrderItemListBefore = ObjectifyService.ofy().load().type(DeliveryOrderItem.class).ancestor(deliveryOrderData).list();
                    break;
                } catch (Exception e) {
                    attempts++;
                    LOG.warning("Error de contención en el datastore al deliveryOrderItemList. Reintento " + attempts);

                    if (attempts >= Constants.MAX_RETRIES) {
                        LOG.severe("No se pudo ejecutar deliveryOrderItemList después de " + Constants.MAX_RETRIES + " intentos.");
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
            
            if (deliveryOrderItemListBefore.size() < deliveryOrder.getItemList().size()) {
                deliveryOrder.getItemList().stream().filter(deliveryOrderItem -> deliveryOrderItem.isTalonItemFree()).forEach(deliveryOrderItem -> {
                    AddDeliveryOrderItemRequest addDeliveryOrderItemRequest = new AddDeliveryOrderItemRequest();
                    addDeliveryOrderItemRequest.setNearbyStores(shoppingCartJson.getNearbyStores());
                    OrderEndpoint orderEndpoint1 = new OrderEndpoint();
                    try {
                        orderEndpoint1.addDeliveryOrderItem(shoppingCartJson.getToken(),
                                shoppingCartJson.getTokenIdWebSafe(), shoppingCartJson.getIdCustomerWebSafe(), (int) deliveryOrderItem.getId(),
                                deliveryOrderItem.getQuantitySold(), shoppingCartJson.getIdStoreGroup(), true,
                                shoppingCartJson.getDeliveryType().getDeliveryType(), shoppingCartJson.getSource(), deliveryOrderItem.getObservations(),
                                false, null, "", addDeliveryOrderItemRequest, null);
                    } catch (Exception e) {
                        deliveryOrder.getItemList().remove(deliveryOrderItem);
                        try {
                            orderEndpoint1.deleteDeliveryOrderItem(shoppingCartJson.getToken(), shoppingCartJson.getTokenIdWebSafe(),
                                    shoppingCartJson.getIdCustomerWebSafe(), shoppingCartJson.getIdStoreGroup(),
                                    (int) deliveryOrderItem.getId(), shoppingCartJson.getDeliveryType().toString());
                        } catch (ConflictException | AlgoliaException | BadRequestException ex) {
                            LOG.warning("Error al eliminar item free: " + ex.getMessage());
                        }
                    }
                });
            } else if (deliveryOrderItemListBefore.size() == deliveryOrder.getItemList().size()) {
                Key<DeliveryOrder> deliveryOrderKey = Key.create(customerKey, DeliveryOrder.class, deliveryOrder.getIdDeliveryOrder());
                deliveryOrder.getItemList().stream().filter(deliveryOrderItem -> deliveryOrderItem.isTalonItemFree()).forEach(deliveryOrderItem -> {
                    DeliveryOrderItem deliveryOrderItem1 = null;
                    int attempts1 = 0;
                    while (attempts1 < Constants.MAX_RETRIES) {
                        try {
                            deliveryOrderItem1 = ofy().load().type(DeliveryOrderItem.class)
                                    .filter("idItem", Key.create(Item.class, String.valueOf(deliveryOrderItem.getId())))
                                    .ancestor(Ref.create(deliveryOrderKey)).first().now();
                            break;
                        } catch (Exception e) {
                            attempts1++;
                            LOG.warning("Error de contención en el datastore al deliveryOrderItemList. Reintento " + attempts1);

                            if (attempts1 >= Constants.MAX_RETRIES) {
                                LOG.severe("No se pudo ejecutar deliveryOrderItemList después de " + Constants.MAX_RETRIES + " intentos.");
                                throw new RuntimeException("Error persistente al eliminar entidades", e);
                            } else {
                                // Espera exponencial entre reintentos
                                try {
                                    Thread.sleep(Math.min(1000 * (long) Math.pow(2, attempts1), 10000));
                                } catch (InterruptedException ie) {
                                    Thread.currentThread().interrupt();
                                    LOG.severe("Interrupción durante espera entre reintentos: " + ie.getMessage());
                                }
                            }
                        }
                    }
                   
                    if (Objects.nonNull(deliveryOrderItem) && Objects.nonNull(deliveryOrderItem1)) {
                        if (deliveryOrderItem1.getQuantitySold() < deliveryOrderItem.getQuantitySold()) {
                            deliveryOrderItem1.setQuantitySold(deliveryOrderItem.getQuantitySold());
                        }
                        deliveryOrderItem1.setOfferText(deliveryOrderItem.getOfferText());
                        deliveryOrderItem1.setOfferDescription(deliveryOrderItem.getOfferDescription());
                        deliveryOrderItem1.setOfferPrice(deliveryOrderItem.getOfferPrice());
                        deliveryOrderItem1.setPrimePrice(deliveryOrderItem.getPrimePrice());
                        deliveryOrderItem1.setPrimeDescription(deliveryOrderItem.getPrimeDescription());
                        deliveryOrderItem1.setPrimeTextDiscount(deliveryOrderItem.getPrimeTextDiscount());
                        deliveryOrderItem1.setTalonDiscount(deliveryOrderItem.isTalonDiscount());
                        deliveryOrderItem1.setTalonItemFree(deliveryOrderItem.isTalonItemFree());

                        int attempts2 = 0;
                        while (attempts2 < Constants.MAX_RETRIES) {
                            try {
                                ofy().save().entity(deliveryOrderItem1);
                                break;
                            } catch (Exception e) {
                                attempts2++;
                                LOG.warning("Error de contención en el datastore al deliveryOrderItemList. Reintento " + attempts2);

                                if (attempts2 >= Constants.MAX_RETRIES) {
                                    LOG.severe("No se pudo ejecutar deliveryOrderItemList después de " + Constants.MAX_RETRIES + " intentos.");
                                    throw new RuntimeException("Error persistente al eliminar entidades", e);
                                } else {
                                    // Espera exponencial entre reintentos
                                    try {
                                        Thread.sleep(Math.min(1000 * (long) Math.pow(2, attempts2), 10000));
                                    } catch (InterruptedException ie) {
                                        Thread.currentThread().interrupt();
                                        LOG.severe("Interrupción durante espera entre reintentos: " + ie.getMessage());
                                    }
                                }
                            }
                        }
                    }
                });
            }
        } else {
            List<DeliveryOrderItem> deliveryOrderItems =  deliveryOrder.getItemList();

            deliveryOrderItems.forEach(deliveryOrderItem -> {
                if(deliveryOrderItem.isTalonItemFree()) {
                    AddDeliveryOrderItemRequest addDeliveryOrderItemRequest = new AddDeliveryOrderItemRequest();
                    addDeliveryOrderItemRequest.setNearbyStores(shoppingCartJson.getNearbyStores());
                    OrderEndpoint orderEndpoint = new OrderEndpoint();
                    try {
                        orderEndpoint.addDeliveryOrderItem(shoppingCartJson.getToken(),
                                shoppingCartJson.getTokenIdWebSafe(), shoppingCartJson.getIdCustomerWebSafe(),
                                (int) deliveryOrderItem.getId(), deliveryOrderItem.getQuantitySold(),
                                shoppingCartJson.getIdStoreGroup(), true,
                                shoppingCartJson.getDeliveryType().getDeliveryType(), shoppingCartJson.getSource(),
                                deliveryOrderItem.getObservations(), false, null, "",
                                addDeliveryOrderItemRequest, null);
                    } catch (Exception e) {
                        deliveryOrderItems.remove(deliveryOrderItem);
                        deliveryOrder.setItemList(deliveryOrderItems);
                    }
                }
            });
        }
        Optional<ExtendedBagPropertiesTalonOne> extendedPropertiesBagItem = APIAlgolia.getExtendedBagItem();
        deliveryOrder.getItemList().forEach(deliveryOrderItem -> {
            if (extendedPropertiesBagItem.isPresent() &&  Arrays.stream(extendedPropertiesBagItem.get().getSku()).toList().contains(String.valueOf(deliveryOrderItem.getId()))){
                deliveryOrderItem.setTotalStock(Integer.parseInt(extendedPropertiesBagItem.get().getFixedStock()));
            }
        });

        return deliveryOrder;
    }

    public static String compressString(String input) throws IOException {
        byte[] inputData = input.getBytes();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(outputStream, new Deflater());
        deflaterOutputStream.write(inputData);
        deflaterOutputStream.close();

        byte[] compressedData = outputStream.toByteArray();

        return Base64.getEncoder().encodeToString(compressedData);
    }

    public static void sendPriceDeliveryMessage(final ShoppingCartJson shoppingCartJson) throws AlgoliaException {
        AlertConfigMessage alertConfigMessage = APIAlgolia.getAlertMessage();

        String date = DateConstants.SIMPLE_DATE_FORMAT.format(new Date());
        alertConfigMessage.getPhoneNumbers().forEach(number ->
                Util.sendAlertCreateOrder(number,
                        "priceDelivery - getIdCustomerWebSafe : " + shoppingCartJson.getIdCustomerWebSafe() + " Error al calcular el carrito. \n"
                                + "Fecha: " + date));

        List<String> listNumberVen = filterNumbersStartingWith58(alertConfigMessage.getPhoneNumbers());
        listNumberVen.forEach(numberVen -> Util.sendAlertCreateOrderVen(numberVen,
                "priceDelivery - getIdCustomerWebSafe : " + shoppingCartJson.getIdCustomerWebSafe() + " Error al calcular el carrito. \n"
                        + "Fecha: " + date));

    }

    // Método para filtrar números que comienzan con "58"
    public static List<String> filterNumbersStartingWith58(List<String> phoneNumbers) {
        List<String> filteredNumbers = new ArrayList<>();
        for (String number : phoneNumbers) {
            if (number.startsWith("58")) {
                filteredNumbers.add(number);
            }
        }
        return filteredNumbers;
    }

    public static void deleteTipsIfCityNotConfig(Optional<TipConfig> tipConfigOpt, DeliveryOrder deliveryOrder, Key<User> customerKey, int idStoreGroup) {

        if (!tipConfigOpt.isPresent()) {
            return;
        }

        TipConfig tipConfig = tipConfigOpt.get();

        String cityIdOrder = getCityIdByStoreId(idStoreGroup);

        if (cityIdOrder == null || cityIdOrder.isEmpty()) {
            return;
        }

        if (tipConfig.getDefaultTipsByCity() == null) {
            return;
        }

        Optional<DefaultTipsByCity> cityIdWithTip = tipConfig
                .getDefaultTipsByCity()
                .stream()
                .filter(cityIdAux -> cityIdAux != null && cityIdAux.getCityId() != null && cityIdAux.getCityId().equals(cityIdOrder))
                .findFirst();

        if (!cityIdWithTip.isPresent()) {
            LOG.info("No se encuentra config de propina para la ciudad => " + cityIdOrder +
                    " Se eliminan las propinas del carrito");
            deleteTips(tipConfig, deliveryOrder, customerKey);
        }

    }

    private static String getCityIdByStoreId(final Integer storeId) {
        final String DEFAULT_CITY = "";
        try {
            final StoresAlgolia storesAlgolia = APIAlgolia.getStoresAlgolia();
            if (Objects.isNull(storesAlgolia) || Objects.isNull(storesAlgolia.getStores()) || storesAlgolia.getStores().isEmpty()) {
                //LOG.warning("El objeto de Algolia FTD.STORES esta vacio o nulo. No se encontrara ninguna ciudad para la tienda #" + storeId);
                return DEFAULT_CITY;
            }

            for (StoreAlgolia store : storesAlgolia.getStores()) {
                if (Objects.equals(store.getId(), storeId)) {
                    return store.getCity();
                }
            }

            return DEFAULT_CITY;
        } catch (Exception e) {
            LOG.warning("Ocurrio un error al buscar el cityID por storeID. Se retornara vacio.");
            return DEFAULT_CITY;
        }
    }

    public static Boolean notAllowedTips(Long id) {
        Optional<NotAllowedTips> notAllowedTips = APIAlgolia.getNotAllowedTipsConfig();

        if (notAllowedTips.isPresent()) {
            Optional<Long> notAllowedTipsCustomer = notAllowedTips.get().getCustomerId().stream()
                    .filter(tipsListAlgolia -> Objects.equals(tipsListAlgolia, id))
                    .limit(1).findFirst();

            return notAllowedTipsCustomer.isPresent();
        }
        return false;
    }

    public static boolean addTipToOrder(TipConfig tipConfig, ShoppingCartJson shoppingCartJson, DeliveryOrder deliveryOrder, Key<User> customerKey) {


        if (shoppingCartJson == null || shoppingCartJson.getTip() == null || shoppingCartJson.getTip() < 0) {
            return false;
        }

        if (tipConfig.getItemTips() == null) {
            LOG.warning("No se encuentra configuracion de propina-item para el valor de:  "
                    + shoppingCartJson.getTip());
            return false;
        }

        String cityIdOrder = getCityIdByStoreId(shoppingCartJson.getIdStoreGroup());

        if (cityIdOrder == null || cityIdOrder.isEmpty()) {
            return false;
        }

        if (tipConfig.getDefaultTipsByCity() == null) {
            return false;
        }

        Optional<DefaultTipsByCity> cityIdWithTip = tipConfig
                .getDefaultTipsByCity()
                .stream()
                .filter(cityIdAux -> cityIdAux != null && cityIdAux.getCityId() != null && cityIdAux.getCityId().equals(cityIdOrder))
                .findFirst();

        if (!cityIdWithTip.isPresent()) {
            return false;
        }

        Float tip = shoppingCartJson.getTip();

        Optional<ItemTip> itemTipOptional = tipConfig
                .getItemTips()
                .stream()
                .filter(itemTip -> itemTip != null && itemTip.getValue() != null && itemTip.getValue().equals(tip))
                .findFirst();


        if (!itemTipOptional.isPresent()) {
            LOG.warning("No se encuentra item en la configuracion de propina-item para el valor de:  "
                    + shoppingCartJson.getTip());
            return false;
        }

        deleteTips(tipConfig, deliveryOrder, customerKey);

        ItemTip itemTip = itemTipOptional.get();

        try {
            OrderEndpoint orderEndpoint1 = new OrderEndpoint();
            orderEndpoint1.addDeliveryOrderItem(shoppingCartJson.getToken(),
                    shoppingCartJson.getTokenIdWebSafe(),
                    shoppingCartJson.getIdCustomerWebSafe(), itemTip.getItemId(), 1, shoppingCartJson.getIdStoreGroup(),
                    false,
                    shoppingCartJson.getDeliveryType().getDeliveryType(),
                    "ADDED_FROM_SEARCH",
                    "TIP",
                    false,
                    null,
                    null,
                    null,
                    null
            );
        } catch (Exception e) {
            LOG.severe("No se encuentra item en Algolia o Datastore o el producto no tiene stock segun la config de propina-item para el valor de:  "
                    + shoppingCartJson.getTip());
            return false;
        }


        return true;
    }

    public static void validateTipAndPrime(Optional<TipConfig> tipConfigOptional,
                                           List<DeliveryOrderItem> deliveryOrderItemList,
                                           Key<User> customerKey,
                                           int idStoreGroup,
                                           DeliveryOrder deliveryOrder,
                                           ValidateOrderReq validateOrderReq) {
        // Determinar si el pedido es "prime"
        boolean isPrime = isOrderPrimeV2(deliveryOrder);

        // Obtener el tip asociado, si existe, a partir del idStoreGroup y la configuración de tip
        Optional<Tip> optionalTip = getOptionalTip(idStoreGroup, tipConfigOptional);

        // Validar: existe tip, existe tipConfig, el pedido es prime y el carrito tiene exactamente 2 items
        if (optionalTip.isPresent() && tipConfigOptional.isPresent() && isPrime && deliveryOrderItemList.size() == 2) {
            TipConfig tipConfig = tipConfigOptional.get();
            List<ItemTip> itemTipList = tipConfig.getItemTips();

            // Si existen configuraciones de tip y la lista de items en la solicitud no está vacía
            if (itemTipList != null && !validateOrderReq.getItems().isEmpty()) {
                // Para cada item tip, remover de la lista de items de la solicitud aquellos que coincidan en itemId
                for (ItemTip itemTip : itemTipList) {
                    validateOrderReq.getItems().removeIf(item -> Objects.equals(item.getItemId(), itemTip.getItemId()));
                }
                // Eliminar los tips asociados al pedido y al cliente
                deleteTips(tipConfig, deliveryOrder, customerKey);
            }
        }
    }
    private static boolean isOrderPrimeV2(DeliveryOrder order) {
        List<DeliveryOrderItem> items = null;

        int attempts = 0;
        while (attempts < Constants.MAX_RETRIES) {
            try {
                items = ofy().load().type(DeliveryOrderItem.class).ancestor(order).list();
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


        if (items == null || items.isEmpty()) {
            return false;
        }

        PrimeUtil primeUtil = new PrimeUtil();

        // Usando streams para verificar si existe algún item prime
        return items.stream().anyMatch(item -> primeUtil.isItemPrime(item.getId()));
    }

    public static Optional<Tip> getOptionalTip(int idStoreGroup, Optional<TipConfig> tipConfigOptional) {

        if (idStoreGroup <= 0) {
            return Optional.empty();
        }

        if (!tipConfigOptional.isPresent()) {
            return Optional.empty();
        }

        TipConfig tipConfig = tipConfigOptional.get();

        if (tipConfig.getTip() == null
                || tipConfig.getDefaultTipsByCity() == null
                || tipConfig.getDefaultTipsByCity().isEmpty()) {
            return Optional.empty();
        }

        String cityId = getCityIdByStoreId(idStoreGroup);

        if (cityId == null || cityId.isEmpty()) {
            return Optional.empty();
        }

        Optional<DefaultTipsByCity> defaultTipsByCityOptional = tipConfig
                .getDefaultTipsByCity()
                .stream()
                .filter(defaultCity ->
                        defaultCity != null && defaultCity.getCityId() != null && defaultCity.getCityId().equals(cityId)
                )
                .findFirst();

        if (!defaultTipsByCityOptional.isPresent() || defaultTipsByCityOptional.get().getDefaultTip() == null) {
            return Optional.empty();
        }

        // set config by city
        tipConfig.getTip().setDefaultTip(defaultTipsByCityOptional.get().getDefaultTip());

        return Optional.of(tipConfig.getTip());
    }

    @NotNull
    public static DeliveryOrder getDeliveryOrderEmpty(ShoppingCartJson shoppingCartJson, Optional<TipConfig> tipConfigOptional, Key<User> customerKey, DeliveryOrder deliveryOrder) {
        //LOG.warning("deliveryOrderItemList is null NO HAY ITEMS AGREGADOS");
        try {
            if (APIAlgolia.getDeleteCartConfig()) {
                CustomerEndpoint customerEndpoint = new CustomerEndpoint();
                customerEndpoint.deleteShoppingCart("12345", shoppingCartJson.getId());
            }
            deleteTips(tipConfigOptional.get(), deliveryOrder, customerKey);
        } catch (Exception e) {
            LOG.info("Error eliminando el carrito del cliente.");
        }
        return getEmptyDeliveryOrder(new DeliveryOrder());
    }

    public static void validateItemsProvider(ShoppingCartJson shoppingCartJson, List<DeliveryOrderProvider> deliveryOrderProviderListToSave,
                                              List<DeliveryOrderItem> deliveryOrderItemList, DeliveryOrder deliveryOrder,
                                              OrderJson orderJSON, Key<DeliveryOrder> deliveryOrderKey) throws BadRequestException {
        for (ProviderOrder provider : orderJSON.getProviders()) {
            DeliveryOrderProvider providerOrder = new DeliveryOrderProvider(provider.getId(), provider.getName(), provider.getEmail(), provider.getDeliveryPrice());
            for (ItemAlgolia deliveryOrderItem : provider.getItems()) {
                //add filters optical
                String filters = null;
                try {
                    filters = deliveryOrderItemList.stream().filter(item -> Objects.equals(item.getId(), Long.parseLong(deliveryOrderItem.getId()))).findFirst().get().getFiltersOptical();
                    LOG.info("Filtros -> " + filters);
                } catch (Exception e) {
                    LOG.info("Ocurrio un problema al traer los filtros. ERROR -> " + e.getMessage());
                }
                if (Objects.nonNull(filters)) {
                    deliveryOrderItem.setFiltersOptical(filters);
                }

                addDeliveryItemOrder(deliveryOrderItem, shoppingCartJson, deliveryOrderKey, providerOrder.getItemList(), deliveryOrder, providerOrder, deliveryOrderItemList);
            }
            providerOrder.setQuantityItem(provider.getItems().stream().mapToInt(item -> item.getQuantityRequested()).sum());
            deliveryOrderProviderListToSave.add(providerOrder);
        }
    }

    public static Boolean checkIfAppliesForFreeDelivery(final DeliveryOrder deliveryOrder,
                                                        List<DeliveryOrderItem> deliveryOrderItemList,
                                                        final ShoppingCartJson shoppingCartJson,
                                                        final String source,
                                                        List<DeliveryOrderProvider> deliveryOrderProviderListToSave) {

        if (Objects.isNull(deliveryOrder) || Objects.isNull(deliveryOrderItemList) || deliveryOrderItemList.isEmpty()) {
            //LOG.warning("method: checkIfAppliesForFreeDelivery() deliveryOrder or deliveryOrderItemList are null or empty..");
            return Boolean.FALSE;
        }

        Key<User> userKey = Key.create(shoppingCartJson.getIdCustomerWebSafe());
        Users users = new Users();
        User user = new User();
        int attempts = 0;
        while (attempts < Constants.MAX_RETRIES) {
            try {
                user = users.findUserByKey(userKey);
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

//        Building request to send to OMS...
        final Long customerId = Long.valueOf(Objects.isNull(user) ? 0 : user.getId());
        final String cityId = getCityIdByStoreId(shoppingCartJson.getIdStoreGroup());
        final String deliveryType = Objects.isNull(shoppingCartJson.getDeliveryType()) ? "" : shoppingCartJson.getDeliveryType().getDeliveryType();

        final List<FreeDeliveryItem> itemsToValidate = new ArrayList<>();

        Optional<TipConfig> tipConfigOptional = GrowthBookConfigLoader.getTipsConfig(Constants.CUSTOMER_ANONYMOUS);

        if (tipConfigOptional.isPresent()) {
//            Eliminar propinas para que no lo tenga en cuenta en el calculo de domicilio gratis.
            deleteTipsForPrimeOrder(deliveryOrderItemList, tipConfigOptional.get());
        }

        List<Long> idsItemProviderList = getIdsItemProviderList(deliveryOrderProviderListToSave);

        List<DeliveryOrderItem> deliveryOrderItemListWhitOutItemsProvider = deliveryOrderItemList.stream()
                .filter(deliveryOrderItem -> !idsItemProviderList.contains(deliveryOrderItem.getId()))
                .collect(Collectors.toList());

        deliveryOrderItemListWhitOutItemsProvider.forEach(item -> {
            final FreeDeliveryItem freeDeliveryItem = new FreeDeliveryItem(item.getId(), item.getQuantitySold());
            itemsToValidate.add(freeDeliveryItem);
        });

        Key<Customer> customerDS = Key.create(shoppingCartJson.getIdCustomerWebSafe());
        CustomerCoupon customerCoupon = obtainCustomerCoupon(customerDS);

        List<FreeDeliveryCoupon> listCouponFree = getFreeDeliveryCoupons(customerCoupon);

        FreeDeliverySimpleCart cart;
        if (!listCouponFree.isEmpty() && listCouponFree.size() >= 1){
            cart = new FreeDeliverySimpleCart(customerId, cityId, deliveryType, source, itemsToValidate, listCouponFree);
        } else {
            cart = new FreeDeliverySimpleCart(customerId, cityId, deliveryType, source, itemsToValidate);
        }
        final Boolean applies = validateFreeDeliveryByCart(cart);

        return applies;
    }


    private static List<Long> getIdsItemProviderList(List<DeliveryOrderProvider> deliveryOrderProviderListToSave) {

        if (ObjectUtils.isEmpty(deliveryOrderProviderListToSave))
            return Collections.emptyList();

        return deliveryOrderProviderListToSave.stream()
                .flatMap(deliveryOrderProvider -> deliveryOrderProvider.getItemList().stream())
                .map(deliveryOrderItem -> deliveryOrderItem.getId())
                .collect(Collectors.toList());
    }

    private static void deleteTipsForPrimeOrder(List<DeliveryOrderItem> deliveryOrderItemList, TipConfig tipConfig) {
        deliveryOrderItemList.removeIf( item -> {
            if (tipConfig == null || tipConfig.getItemTips() == null ){
                return false;
            }
            return tipConfig.getItemTips()
                    .stream()
                    .anyMatch(itemTip -> itemTip.getItemId() != null && itemTip.getItemId().longValue() == item.getId());
        });
    }

    private static CustomerCoupon obtainCustomerCoupon(final Key<Customer> customerKey) {
        try {
            final List<CustomerCoupon> customerCoupons = ofy().load().type(CustomerCoupon.class).filter("customerKey", customerKey).orderKey(false).list();
            if (customerCoupons != null && !customerCoupons.isEmpty()) {
                customerCoupons.sort(Comparator.comparing(a -> a.getUseTime()));
                final int positionLastCupon = customerCoupons.size() - 1;
                final CustomerCoupon couponToRedim = customerCoupons.get(positionLastCupon);
                if (couponToRedim != null) {
                    return couponToRedim;
                }
                return null;
            }
            return null;
        } catch (Exception e) {
            LOG.warning("Error al obtener cupon cuando el cliente elimina todo el carrito... Mensaje: " + e.getMessage());
            return null;
        }
    }

    @NotNull
    private static List<FreeDeliveryCoupon> getFreeDeliveryCoupons(CustomerCoupon customerCoupon) {
        List<FreeDeliveryCoupon> listCouponFree = new ArrayList<>();
        if (customerCoupon != null && Objects.nonNull(customerCoupon.getCouponId())) {
            final Ref<Coupon> coupon = customerCoupon.getCouponId();
            if (coupon.get() != null && coupon.get().getName() != null) {
                FreeDeliveryCoupon couponFree = new FreeDeliveryCoupon();
                couponFree.setOfferId(coupon.get().getOfferId());
                couponFree.setCouponType(coupon.get().getCouponType().getCouponType());
                listCouponFree.add(couponFree);
            }
        }
        return listCouponFree;
    }

    private static Boolean validateFreeDeliveryByCart(final FreeDeliverySimpleCart cart) {
        if (Objects.isNull(cart))
            return Boolean.FALSE;

        try {
            return ApiGatewayService.get().validateFreeDeliveryByCart(cart);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isOrderOnlyPrime(List<DeliveryOrderItem> items,
                                     List<DeliveryOrderProvider> deliveryOrderProviderListToSave
    ) {

        if (Objects.nonNull(deliveryOrderProviderListToSave)) {
            deliveryOrderProviderListToSave.forEach((providerItem) -> {
                items.removeIf(orderItem -> orderItem.getId() == providerItem.getItemList().get(0).getId());

            });
        }

        try {
            if (Objects.nonNull(items) && !items.isEmpty() && items.size() > 0) {
//                    Inicia la validación si es un carrito PRIME
                for (int i = 0; i < items.size(); i++) {
                    Long item = items.get(i).getId();
                    PrimeUtil primeUtil = new PrimeUtil();
                    if (primeUtil.isItemPrime(item) || item.equals(236650616L)) {
                        return Boolean.TRUE;
                    } else {
                        return Boolean.FALSE;
                    }
                }
            }
        } catch (Exception e) {
            LOG.warning("Ocurrrio un problema validando los items en el metodo isOrderOnlyPrime().");
            return Boolean.FALSE;
        }
        return  Boolean.FALSE;
    }

    public static void setResponsePriceDelivery(List<DeliveryOrderProvider> deliveryOrderProviderListToSave, List<DeliveryOrderItem> deliveryOrderItemList,
                                         DeliveryOrder deliveryOrder, OrderJson orderJSON) {

        deliveryOrder.setOfferPrice(deliveryOrder.getOfferPrice() + orderJSON.getGlobalDiscount());
        deliveryOrder.setWeight(orderJSON.getWeight());
        deliveryOrder.setLowerRangeWeight(orderJSON.getLowerRangeWeight());
        deliveryOrder.setTopRangeWeight(orderJSON.getTopRangeWeight());
        deliveryOrder.setDeliveryPrice(orderJSON.getDeliveryValue());
        deliveryOrder.setPrimeDeliveryValue(String.valueOf(orderJSON.getDeliveryValue()));
        deliveryOrder.setRegisteredOffer(orderJSON.getRegisteredDiscount());
        deliveryOrder.setTotalPrice(deliveryOrder.getSubTotalPrice() - deliveryOrder.getOfferPrice() + (orderJSON.getDeliveryValue() + orderJSON.getProviderDeliveryValue()));
        // Campos nuevos proveedores
        deliveryOrder.setProviderDeliveryPrice(orderJSON.getProviderDeliveryValue());
        deliveryOrder.setTotalDelivery(orderJSON.getDeliveryValue() + orderJSON.getProviderDeliveryValue());
        if (Objects.nonNull(deliveryOrderProviderListToSave) && !deliveryOrderProviderListToSave.isEmpty()) {
            deliveryOrder.setQuantityProviders(deliveryOrderProviderListToSave.stream().mapToInt(provider -> provider.getQuantityItem()).sum());
        } else {
            deliveryOrder.setQuantityProviders(0);
        }
        deliveryOrder.setQuantityFarmatodo(Objects.nonNull(deliveryOrderItemList) ? deliveryOrderItemList.stream().mapToInt(item -> item.getQuantitySold()).sum() : 0);
        deliveryOrder.setQuantityFarmatodo(deliveryOrder.getQuantityFarmatodo() > 0 ? deliveryOrder.getQuantityFarmatodo() - deliveryOrder.getQuantityProviders() : deliveryOrder.getQuantityFarmatodo());
        deliveryOrder.setTotalQuantity(deliveryOrder.getQuantityFarmatodo() + deliveryOrder.getQuantityProviders());
    }

    public static void restrictItemsAndSave(List<DeliveryOrderItem> deliveryOrderItemList, int idStoreGroup) {
        if (deliveryOrderItemList != null && !deliveryOrderItemList.isEmpty() && idStoreGroup > 0) {
            RestrictionItemConfig restrictionItemConfig = APIAlgolia.getRestrictionQuantityItems();

            deliveryOrderItemList.forEach(itemInList -> {

                if (restrictionItemConfig.getRestrictionItems() != null && !restrictionItemConfig.getRestrictionItems().isEmpty()) {
                    restrictionItemConfig.getRestrictionItems().forEach(restrictionItem -> {
                        if (restrictionItem.getItemId() == itemInList.getId() && itemInList.getQuantitySold() > restrictionItem.getRestrictionQuantity()) {
                            itemInList.setQuantitySold(Math.toIntExact(restrictionItem.getRestrictionQuantity()));
                        }
                    });
                }
            });
        }
    }

    public static void setOpticalItemFiltersProvider(List<DeliveryOrderProvider> deliveryOrderProviderListToSave) {

        for (DeliveryOrderProvider provider : deliveryOrderProviderListToSave) {
            if (provider.getId() == 1207) { // id del proveedor de optica
                provider.getItemList().forEach(itemProvider -> {
                    OpticalItemFilter opticalItemFilter = new Gson().fromJson(itemProvider.getFiltersOptical(), OpticalItemFilter.class);
                    ShipmentType shipment = Objects.nonNull(itemProvider.getShipment()) ? itemProvider.getShipment() : ShipmentType.EXTENDIDO;
                    LOG.info("Filtos de optica: " + opticalItemFilter.toString());
                    itemProvider.setOpticalFilter(opticalItemFilter);
                    itemProvider.setShipment(shipment);
                });
            }
        }
    }

    public void checkTipsQuantity(List<DeliveryOrderItem> deliveryOrderItemList) {
        deliveryOrderItemList.forEach(this::checkTip);
    }

    private DeliveryOrderItem checkTip(DeliveryOrderItem deliveryOrderItem) {
        if (isTip(deliveryOrderItem.getId())){
            deliveryOrderItem.setQuantitySold(1);
        }
        return deliveryOrderItem;
    }

    private boolean isTip(long id) {
        Optional<TipConfig> tipConfigOptional = GrowthBookConfigLoader.getTipsConfig(Constants.CUSTOMER_ANONYMOUS);

        return tipConfigOptional.map(tipConfig ->
                !Objects.isNull(tipConfig.getItemTips()) && tipConfig.getItemTips().stream().anyMatch(itemTip ->
                        !Objects.isNull(itemTip) && !Objects.isNull(itemTip.getItemId()) && itemTip.getItemId() == id
                )
        ).orElse(false);
    }

    public static boolean isFullProvider(DeliveryOrder deliveryOrder, Optional<TipConfig> tipConfigOptional) {

        if (deliveryOrder == null && !tipConfigOptional.isPresent()) {
            return false;
        }

        double tipPrice = getTipPriceForOrder(deliveryOrder, tipConfigOptional.get());


        int quantityFarmatodoWithoutTip = deliveryOrder.getQuantityFarmatodo();
        if (tipPrice > 0) {
            quantityFarmatodoWithoutTip = quantityFarmatodoWithoutTip > 0 ? deliveryOrder.getQuantityFarmatodo() - 1 : deliveryOrder.getQuantityFarmatodo();
        }

        return deliveryOrder.getQuantityProviders() > 0 && quantityFarmatodoWithoutTip == 0;
    }

    public static void hideTipItemList(DeliveryOrder deliveryOrder, Optional<TipConfig> tipConfigOpt) {

        if (!tipConfigOpt.isPresent()) {
            return;
        }

        TipConfig tipConfig = tipConfigOpt.get();;

        if (tipConfig.getItemTips() == null || deliveryOrder.getItemList() == null) {
            return;
        }

        deliveryOrder.getItemList().removeIf(itemCart -> {
            Optional<ItemTip> itemTipInCartOpt = tipConfig
                    .getItemTips()
                    .stream()
                    .filter(itemTipAux -> itemTipAux != null
                            && itemTipAux.getItemId() != null
                            && itemTipAux.getItemId().longValue() == itemCart.getId())
                    .findFirst();

            return itemTipInCartOpt.isPresent();
        });

    }

    public static void validateTips(ShoppingCartJson shoppingCartJson, Optional<TipConfig> tipConfigOptional, List<DeliveryOrderProvider> deliveryOrderProviderListToSave, List<DeliveryOrderItem> deliveryOrderItemList, boolean isScanAndGo, DeliveryOrder deliveryOrder, boolean isFullProvider) {

        //Validarlo en un solo if
        if (tipConfigOptional.isPresent() && !notAllowedTips(Long.valueOf(shoppingCartJson.getId()))
                                            && !isOrderOnlyPrime(deliveryOrderItemList, deliveryOrderProviderListToSave) && !isScanAndGo
                                            && shoppingCartJson.getDeliveryType().equals(DeliveryType.EXPRESS) && !isFullProvider){

                        Optional<Tip> optionalTip = getOptionalTip(shoppingCartJson.getIdStoreGroup(), tipConfigOptional);

                        if (optionalTip.isPresent() && tipConfigOptional.isPresent()){

                            // add tip price/
                            Optional<DeliveryOrderItem> itemTipOpt = OrderUtil.getItemTipInCart(deliveryOrder, tipConfigOptional.get());

                            double tipPrice = OrderUtil.getTipPriceForOrder(deliveryOrder, tipConfigOptional.get());
                            hideTipItemList(deliveryOrder, tipConfigOptional);
                            deliveryOrder.setTip(optionalTip.get());

                            // rest tip item
                            if (itemTipOpt.isPresent()){
                                deliveryOrder.setTipPrice(tipPrice);
                                // calculate subtotal with tip
                                deliveryOrder.setSubTotalPrice(deliveryOrder.getSubTotalPrice() - tipPrice);
                                deliveryOrder.setTotalQuantity( deliveryOrder.getTotalQuantity() - 1 );
                                deliveryOrder.setQuantityFarmatodo( deliveryOrder.getQuantityFarmatodo() - 1 );
                            }
                        }
                        // fix para sergio
                        if (shoppingCartJson.getTip() != null && shoppingCartJson.getTip() == 0.0){
                            deliveryOrder.setTipPrice(0.0);
                        }
        }
    }

    public static boolean isOnlyPrimeForDeliveryOrder(DeliveryOrder deliveryOrder) {
        return deliveryOrder.getItemList().size() == 1 && isOrderPrime(deliveryOrder);
    }

    private static boolean isOrderPrime(DeliveryOrder order) {

        boolean response = Boolean.FALSE;

        List<DeliveryOrderItem> items = order.getItemList();

        for(int i = 0; i < items.size(); i++ ){
            Long item = items.get(i).getId();
            PrimeUtil primeUtil1 = new PrimeUtil();
            if (primeUtil1.isItemPrime(item)){
                response = Boolean.TRUE;
            }
        }
        return response;
    }

    public static void validateItemsAlgolia(
            ValidateOrderReq validateOrderReq,
            final int idStoreGroup,
            String source,
            int validBuildCodeNumberApp,
            DeliveryOrder deliveryOrder,
            Key<User> customerKey
    ) throws BadRequestException {

        List<ValidateOrderReq.Item> itemsToRemove = new ArrayList<>();

        // Crear la lista de objectIds a partir de los items del request
        List<String> objectIdList = validateOrderReq.getItems().stream()
                .map(item -> String.valueOf(item.getItemId()) + idStoreGroup)
                .collect(Collectors.toList());

        // Obtener la lista de ItemAlgolia correspondiente
        List<ItemAlgolia> itemAlgoliaList = APIAlgolia.getItemsByObjectIds(objectIdList);

        // Iterar usando índices para evitar problemas de tamaño de listas
        for (int i = 0; i < validateOrderReq.getItems().size(); i++) {
            ValidateOrderReq.Item item = validateOrderReq.getItems().get(i);
            // Si el índice es mayor o igual que el tamaño de la lista de Algolia, se asume null
            ItemAlgolia itemAlgolia = i < itemAlgoliaList.size() ? itemAlgoliaList.get(i) : null;

            if (itemAlgolia == null || GuardAlgolia.validationItemsAlgoliaCart(itemAlgolia)) {
                itemsToRemove.add(item);
                // Remover los items correspondientes del deliveryOrderItemList
                deleteItemInCart(deliveryOrder, item.getItemId(), customerKey);
            }
            LOG.info("validBuildCodeNumberApp -> " + validBuildCodeNumberApp);
            if (!GuardAlgolia.validationMarketplaceActiveBySource(source, validBuildCodeNumberApp)) {
                if (itemAlgolia == null || itemAlgolia.getUuidItem() != null) {
                    itemsToRemove.add(item);
                    // Remover los items correspondientes del deliveryOrderItemList
                    deleteItemInCart(deliveryOrder, item.getItemId(), customerKey);
                }
            }
        }

        // Eliminar los items no válidos del request
        validateOrderReq.getItems().removeAll(itemsToRemove);
    }


    public static void deleteTipPriceZero(List<DeliveryOrderItem> deliveryOrderItemList) {
        if (deliveryOrderItemList == null) {
            return;
        }
        Optional<Long> optTipZeroId = getTipZero();
//        Revisar propina cero
        optTipZeroId.ifPresent(tipZeroId -> deliveryOrderItemList.removeIf(deliveryOrderItem -> deliveryOrderItem.getId() == tipZeroId));
    }


    private static Optional<Long> getTipZero() {
        //revision de items de propina
        Optional<TipConfig> tipConfigOptional = GrowthBookConfigLoader.getTipsConfig(Constants.CUSTOMER_ANONYMOUS);

        if (!tipConfigOptional.isPresent()) {
            return Optional.empty();
        }
        List<ItemTip> tipList = tipConfigOptional.get().getItemTips();
        if (tipList == null || tipList.isEmpty()) {
            return Optional.empty();
        }
        Optional<ItemTip> tipZeroOpt = tipList.stream().filter(itemTip -> itemTip != null && itemTip.getValue() != null && itemTip.getValue() == 0).findFirst();

        if (tipZeroOpt.isPresent()) {
            Long itemTipId = (tipZeroOpt.get().getItemId() == null ? 0L : tipZeroOpt.get().getItemId());
            return Optional.of(itemTipId);
        }

        return Optional.empty();
        // -----------fin de revision de items de propina
    }

    public static int validBuildCodeNumberApp(String buildCodeNumberApp) {
        try {
            if (buildCodeNumberApp != null && buildCodeNumberApp.matches("^[1-9]\\d*$")) {
                return Integer.valueOf(buildCodeNumberApp);
            }

        }catch (Exception e){
            LOG.warning("Fallo al convertir buildCodeNumberApp" + e);
            return 0;
        }
        return 0;
    }

}





