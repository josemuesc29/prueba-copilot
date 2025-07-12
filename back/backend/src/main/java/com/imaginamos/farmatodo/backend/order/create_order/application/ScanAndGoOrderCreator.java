package com.imaginamos.farmatodo.backend.order.create_order.application;

import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.ConflictException;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.googlecode.objectify.Key;
import com.imaginamos.farmatodo.backend.order.create_order.domain.Orders;
import com.imaginamos.farmatodo.backend.order.create_order.domain.CommandCreateOrder;
import com.imaginamos.farmatodo.backend.order.create_order.domain.OrderService;
import com.imaginamos.farmatodo.backend.order.create_order.domain.OrderStatus;
import com.imaginamos.farmatodo.backend.order.create_order.domain.OrderUtil;
import com.imaginamos.farmatodo.backend.order.create_order.infraestructure.BrazeClient;
import com.imaginamos.farmatodo.backend.util.TraceUtil;
import com.imaginamos.farmatodo.model.algolia.ItemAlgolia;
import com.imaginamos.farmatodo.model.coupon.CustomerCoupon;
import com.imaginamos.farmatodo.model.customer.Customer;
import com.imaginamos.farmatodo.model.order.*;
import com.imaginamos.farmatodo.model.payment.PaymentTypeEnum;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.networking.algolia.APIAlgolia;
import com.imaginamos.farmatodo.networking.cache.CachedDataManager;
import com.imaginamos.farmatodo.networking.talonone.TalonOneService;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ScanAndGoOrderCreator implements OrderCreator {

    private static final Logger LOG = Logger.getLogger(ScanAndGoOrderCreator.class.getName());

    @Override
    public CreatedOrder create(CommandCreateOrder commandCreateOrder) throws ConflictException, BadRequestException {

        final String idCustomerWebSafe       = commandCreateOrder.getIdCustomerWebSafe();
        final DeliveryOrder order            = commandCreateOrder.getOrder();
        final HttpServletRequest httpRequest = commandCreateOrder.getHttpServletRequest();
        final boolean isScanAndGo            = OrderUtil.isScanAndGo(order);


        DeliveryOrder deliveryOrderSaved = OrderService.findActiveDeliveryOrderByidCustomerWebSafe(idCustomerWebSafe);
        if (deliveryOrderSaved != null) {
            LOG.info("deliveryOrderSaved ->  " + deliveryOrderSaved);
        }
//        LOG.info("deliveryOrderSaved is null ->  " + (deliveryOrderSaved == null));
        final Key<Customer> customerKey = OrderService.getCustomerKeyFromIdCustomerWebSafe(idCustomerWebSafe);
//        LOG.info("customerKey ->  " + customerKey.getString());

        List<DeliveryOrderItem> deliveryOrderItemList = OrderService.findItemsOf(deliveryOrderSaved);

        // ScanAndGo Order
        List<DeliveryOrderItem> itemsScanAndGo = new ArrayList<>(deliveryOrderItemList);
        List<DeliveryOrderItem> itemsNewOrder = new ArrayList<>();
        if (isScanAndGo) {
            //set store princial when is scan and go
            itemsNewOrder = Objects.nonNull(itemsScanAndGo) ? itemsScanAndGo.stream().filter(it -> (Objects.isNull(it.getScanAndGo()) || !it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon())).collect(Collectors.toList()) : null;
            if (Objects.nonNull(itemsScanAndGo) && itemsScanAndGo.stream()
                    .filter(it -> (Objects.isNull(it.getScanAndGo()) || !it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon())).findFirst().isPresent()) {
                itemsScanAndGo.removeIf(it -> ((Objects.isNull(it.getScanAndGo()) || !it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon())));
            }
        } else {
            itemsNewOrder = Objects.nonNull(itemsScanAndGo) ? itemsScanAndGo.stream().filter(it -> (Objects.nonNull(it.getScanAndGo()) && it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon())).collect(Collectors.toList()) : null;
            if (Objects.nonNull(itemsScanAndGo) && itemsScanAndGo.stream()
                    .filter(it -> ((Objects.nonNull(it.getScanAndGo()) && it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon()))).findFirst().isPresent()) {
                itemsScanAndGo.removeIf(it -> ((Objects.nonNull(it.getScanAndGo()) && it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon())));
            }
        }

        // fix provider and scan and go
        List<DeliveryOrderProvider> deliveryOrderProvidersList = new ArrayList<>();

        // Fix elimina items que no corresponden al tipo de envio actual
        if (Objects.nonNull(itemsNewOrder) && !itemsNewOrder.isEmpty()) {
            final List<DeliveryOrderItem> deliveryOrderItemsToDelete = itemsNewOrder;
            CompletableFuture.runAsync(() -> OrderService.deleteDeliveryOrderItems(deliveryOrderItemsToDelete));
        }

        deliveryOrderItemList = itemsScanAndGo;

        //validacion de cupones
        CustomerCoupon customerCoupon = OrderUtil.getCustomerCouponByCustomerKey(OrderService.getCustomerKeyFromIdCustomerWebSafe(idCustomerWebSafe));
        String couponName = OrderUtil.couponNameOf(customerCoupon);
        RequestSourceEnum source = OrderUtil.getSourceFromRequestHeader(httpRequest);

        if (couponName != null && OrderUtil.orderHasCoupon(deliveryOrderItemList) && Objects.nonNull(order)) {
            OrderUtil.validateCouponBySourceAndDeliveryType(couponName, source, order.getDeliveryType().getDeliveryType());
        }

        // TODO: Validacion para evitar cobrar productos que no esten visibles al usuario (APPS - Provedores externos)
        if (Objects.requireNonNull(order).hasItems()) {
//            LOG.info("method createOrder ->  valida Items de dataStore y los items parametro");
            Map<Long, Integer> mapValidationItem = order.getItemList().stream().collect(ImmutableMap.toImmutableMap(DeliveryOrderItem::getId, DeliveryOrderItem::getQuantitySold));
            if (Objects.nonNull(deliveryOrderProvidersList) && !deliveryOrderProvidersList.isEmpty()) {
//                LOG.info("method createOrder ->  Validando Items");
                deliveryOrderProvidersList.forEach(provider ->
                        provider.getItemList().removeIf(item -> !mapValidationItem.containsKey(item.getId())));
                // Validación para evitar que los items se dupliquen
                Map<Long, Integer> mapProviderItem = new HashMap<>();
                deliveryOrderProvidersList.stream()
                        .filter(provider -> (Objects.nonNull(provider.getItemList()) && !provider.getItemList().isEmpty()))
                        .forEach(provider -> provider.getItemList().parallelStream().forEach(itemProvider -> {
                                    mapProviderItem.put(itemProvider.getId(), itemProvider.getQuantitySold());
                                })
                        );
                // Elimina item de proveedores del listado normal de items
                deliveryOrderItemList.removeIf(item -> mapProviderItem.containsKey(item.getId()));
            }
        }
//        LOG.info("createOrder: has picking date ---> " + order.getPickingDate());

        String orderRequest = Orders.createOrderJson(order, deliveryOrderItemList, deliveryOrderProvidersList).toString();
//        LOG.info("createOrderJson ->  " + (orderRequest != null ? orderRequest : " es null"));

        CreateOrderRequestCore requestCore;
        Gson gson = new Gson();
        requestCore = gson.fromJson(orderRequest, CreateOrderRequestCore.class);
        requestCore.setCustomerId((long) order.getIdFarmatodo());

        if (requestCore != null && requestCore.getCustomerId() <= 0) {
            throw new ConflictException(Constants.ERROR_CREATE_ORDER);
        }

        if (order.isSelfCheckout())
            requestCore.setSelfCheckout(order.getSelfCheckout());

        requestCore.setSource(source.name());

        if (PaymentTypeEnum.PSE.getId() == order.getPaymentType().getId()) {
            requestCore.setFinancialInstitutions(order.getFinancialInstitutions());
            requestCore.setTypePersonPSE(order.getTypePersonPSE());
            requestCore.setIpAddress(order.getIpAddress());
            requestCore.setIdentification(order.getIdentification());
        }

//        LOG.info("create order in BACKEND3");
        CreatedOrder createdOrder = new CreatedOrder();
        OrderUtil.createOrderViaBackend3(requestCore, order, createdOrder, TraceUtil.getXCloudTraceId(commandCreateOrder.getHttpServletRequest()), idCustomerWebSafe);

        LOG.info("response backend3 --> " + createdOrder.toString());

        if (isScanAndGo && createdOrder.getId() == 0 && PaymentTypeEnum.TRANSACCIONES_EN_LINEA.getId() == order.getPaymentType().getId()) {
            CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
            throw new ConflictException(Constants.ERROR_PAYMENT_ONLINE);
        }

        if (createdOrder.getId() <= 0) {
            CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
            throw new ConflictException(Constants.ERROR_CREATE_ORDER);
        }
//        LOG.warning("New Order ->  " + orderRequest);
//        LOG.warning("Result New Order ->  " + createdOrder);

        createdOrder.setItems(emptyListOfItems());
        createdOrder.setProviders(emptyProviderOrder());

        DeliveryOrder deliveryOrder = OrderService.findActiveDeliveryOrderByRefCustomer(customerKey);
        if (deliveryOrder == null){
            CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
            throw new ConflictException(Constants.DELIVERY_ORDER_NOT_FOUND);
        }

        deliveryOrder.setIdOrder(createdOrder.getId());
        deliveryOrder.setAddress(createdOrder.getAddress());
        deliveryOrder.setIdAddress(order.getIdAddress());
        deliveryOrder.setAddressDetails(order.getAddressDetails());
        deliveryOrder.setPaymentType(order.getPaymentType());
        deliveryOrder.setSource(order.getSource());
        deliveryOrder.setCurrentStatus(0);
        deliveryOrder.setIdFarmatodo(order.getIdFarmatodo());
        deliveryOrder.setLastStatus(OrderStatus.ORDER_CREATED);
        deliveryOrder.setActive(true);
        deliveryOrder.setDeliveryType(order.getDeliveryType());
        deliveryOrder.setCreateDate(new Date(new Timestamp(createdOrder.getCreateDate()).getTime()));

        if (!PaymentTypeEnum.PSE.getId().equals(order.getPaymentType().getId())) {
            deliveryOrder.setCurrentStatus(0);
        }

        // if ScanAndGO
        if (OrderUtil.isScanAndGo(order)) {
            if (Objects.nonNull(createdOrder.getQrCode())) {
                deliveryOrder.setQrCode(createdOrder.getQrCode());
            }

            if (Objects.nonNull(order.getPaymentType()) && PaymentTypeEnum.DATAFONOS.getId().equals(order.getPaymentType().getId())) {
                deliveryOrder.setLastStatus(OrderStatus.ORDER_CREATED);
            } else {
                deliveryOrder.setLastStatus(OrderStatus.ORDER_DELIVERED);
                deliveryOrder.setActive(false);
            }
        }


        if (!itemsNewOrder.isEmpty()) {
            try {
                // Crea una orden con lo items sobrantes
//                LOG.info("Crea una nueva orden para los items ->  " + itemsNewOrder.size());
                final List<DeliveryOrderItem> deliveryOrderItemsToDelete = itemsNewOrder;
                CompletableFuture.runAsync(() -> {
                    OrderUtil.createDeliveryOrderWithItems(customerKey, deliveryOrder.getDeliveryType(), deliveryOrderItemsToDelete);
                });
            } catch (Exception ex) {
                LOG.warning("No fue posible crear el nuevo Carrito con los items no utilizados." + ex.getMessage());
            }
        }

        // Save order tracing and add others attributes
        final Key<DeliveryOrder> deliveryOrderKey = OrderService.saveDeliveryOrder(deliveryOrder);
        CompletableFuture.runAsync(() -> {
            try {
                OrderService.saveOrderTracing(order, createdOrder, deliveryOrderKey);
            } catch (BadRequestException e) {
                CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
                throw new RuntimeException(e);
            }
        });
        OrderUtil.addMarcaCategorySubcategorieAndItemUrl(createdOrder);

        Orders.deleteCouponForCustomerDefault(customerKey, requestCore);

        DeliveryOrder deliveryOrderData = OrderUtil.getOrderMethod(deliveryOrder,createdOrder.getId(), false, false);
        createdOrder.setOrderData(deliveryOrderData);

        Orders.saveAmplitudeSessionId(commandCreateOrder, createdOrder);

        if (!order.isSelfCheckout())
            OrderUtil.addRMSClassesToOrder(createdOrder);

        CompletableFuture.runAsync(() -> BrazeClient.sendEventCreate(createdOrder, requestCore.getCustomerId()));

        TalonOneService talonOneService = new TalonOneService();
        CompletableFuture.runAsync(() -> talonOneService.copyClosedSession(String.valueOf(createdOrder.getOrderData().getIdFarmatodo()),String.valueOf(createdOrder.getId()), createdOrder,idCustomerWebSafe));
        CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);

        return createdOrder;
    }

}
