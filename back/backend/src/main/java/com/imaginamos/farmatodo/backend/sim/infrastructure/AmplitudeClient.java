package com.imaginamos.farmatodo.backend.sim.infrastructure;

import com.imaginamos.farmatodo.backend.order.create_order.infraestructure.BrazeClient;
import com.imaginamos.farmatodo.backend.order.create_order.infraestructure.SendOrderPush;
import com.imaginamos.farmatodo.backend.sim.domain.AmplitudeOrderStatusEnum;
import com.imaginamos.farmatodo.backend.sim.domain.AmplitudeUtils;
import com.imaginamos.farmatodo.model.order.*;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.networking.algolia.APIAlgolia;
import com.imaginamos.farmatodo.networking.models.amplitude.*;
import com.imaginamos.farmatodo.networking.services.ApiGatewayService;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author Jhon Chaparro
 * @since 2022
 */
public class AmplitudeClient {
    private static final Logger LOG = Logger.getLogger(AmplitudeClient.class.getName());

    /**
     * send events to amplitude async
     *
     * @param fulfilOrdColDescDomain
     * @author Jhon Chaparro
     */
    public void sendInfoAmplitude(FulfilOrdColDescDomain fulfilOrdColDescDomain, boolean asyncActive) {
        String idBraze = AmplitudeUtils.userExistInBraze(fulfilOrdColDescDomain);
        if (idBraze != null) {
//            LOG.info("usuario ok::se sube data " + idBraze);
            AmplitudeClient dataAmplService = new AmplitudeClient();
            dataAmplService.saveItemOnAmplitude(fulfilOrdColDescDomain.getFulfilOrdDesc(), idBraze, asyncActive);
            dataAmplService.saveOrderCompletedAmplitude(fulfilOrdColDescDomain.getFulfilOrdDesc(), idBraze, asyncActive);
        }
    }

    /***
     * Create event to Amplitude Product bought
     * @param fulfilOrdDesc
     * @param idBrazeCreate
     */
    public void saveItemOnAmplitude(FulfilOrdColDescDomain.FulfilOrdDescDomain[] fulfilOrdDesc, String idBrazeCreate, boolean asyncActive) {
        try {
            Optional<Boolean> optionalIsActive = APIAlgolia.isActiveLoadDataAmplitude();
            if (optionalIsActive.isPresent() && optionalIsActive.get()) {
//                LOG.info("SAVE_ITEM_ON_AMPLITUDE:Feature Load Data Amplitude is Active");
                String getIdBraze = AmplitudeUtils.getIdBraze(fulfilOrdDesc);
                String idBraze = (getIdBraze == null) ? idBrazeCreate : AmplitudeUtils.getIdBraze(fulfilOrdDesc);

                if (idBraze != null) {
                    FulfilOrdColDescDomain.FulfilOrdDescDomain order = Arrays.stream(fulfilOrdDesc).findAny().get();
                    String orderId = order.getCustomerOrderNo();
                    OrderInfoAmplitude infoAmplitude = ApiGatewayService.get().getOrderInfo(orderId);
                    DeliveryOrder deliveryOrder = AmplitudeUtils.getOrder(orderId);
                    UserPropertiesOrder userProperties = new UserPropertiesOrder();

                    if (infoAmplitude != null) {
                        userProperties.setCityCode(infoAmplitude.getCityCode() != null ? infoAmplitude.getCityCode() : "");
                        userProperties.setEmail(infoAmplitude.getEmail() != null ? infoAmplitude.getEmail() : "");
                        userProperties.setFirstName(infoAmplitude.getFirstName() != null ? infoAmplitude.getFirstName() : "");
                        userProperties.setLastName(infoAmplitude.getLastName() != null ? infoAmplitude.getLastName() : "");
                        userProperties.setPhone(infoAmplitude.getPhone() != null ? infoAmplitude.getPhone() : "");
                        userProperties.setPrime(AmplitudeUtils.validateUserPrimeCache(Long.valueOf(deliveryOrder.getIdFarmatodo())).get());
                        BrazeClient.sendOrderPushNotification(new SendOrderPush(infoAmplitude.getEmail(), 5, String.valueOf(deliveryOrder.getIdOrder()), null));
                    }

                    List<Event> events = new ArrayList<>();
                    if (deliveryOrder != null && !deliveryOrder.getItemList().isEmpty()) {
                        for (DeliveryOrderItem item : deliveryOrder.getItemList()) {

                            EventProperties eventProperties = new EventProperties();
                            eventProperties.setId(item.getId());
                            eventProperties.setName(item.getMediaDescription());
                            eventProperties.setVariant(item.getGrayDescription());
                            eventProperties.setDepartment(item.getDepartments() != null ? item.getDepartments().get(0) : "");
                            eventProperties.setCategory(item.getCategorie() != null ? item.getCategorie() : "");
                            eventProperties.setSubCategory(item.getSubCategory() != null ? item.getSubCategory() : "");
                            eventProperties.setBrand(item.getMarca() != null ? item.getMarca() : "");
                            eventProperties.setQuantity(item.getQuantitySold());
                            eventProperties.setFullPrice(item.getFullPrice());
                            eventProperties.setOfferPrice(item.getOfferPrice());
                            eventProperties.setOrderId("" + deliveryOrder.getIdOrder());
                            eventProperties.setOrderDeliveryType(deliveryOrder.getDeliveryType() != null ? deliveryOrder.getDeliveryType().getDeliveryType() : "");
                            eventProperties.setOrderChannel("Online");
                            eventProperties.setStatusOrder(AmplitudeOrderStatusEnum.FACTURADA.toString());
                            setDataCallCenterUser(infoAmplitude, eventProperties);

                            if (Constants.SOURCE_CALL_CENTER.equalsIgnoreCase(deliveryOrder.getSource())) {
                                eventProperties.setOrderChannel("Call Center");
                            }
                            eventProperties.setPrime(Objects.nonNull(infoAmplitude) && userProperties.getPrime() ? userProperties.getPrime() : false);
                            eventProperties.setSelfCheckout(Objects.nonNull(infoAmplitude) && infoAmplitude.isSelfCheckout() ? infoAmplitude.isSelfCheckout() : false);
                            eventProperties.setBilled(item.isBilled());

                            BusinessItem businessItem = ApiGatewayService.get().getClassificationBusinessItem("" + item.getId());
                            if (businessItem != null) {
                                eventProperties.setCclass(businessItem.getCclass());
                                eventProperties.setSubclass(businessItem.getSubclass());
                                eventProperties.setProvider(businessItem.getProvider());
                                eventProperties.setDepartmentBusiness(businessItem.getDepartment());
                                eventProperties.setGroup(businessItem.getGroup());
                                eventProperties.setDivision(businessItem.getDivision());
                            }

                            if (item.getOfferPrice() > 0 && item.getFullPrice() > 0) {

                                double discount = item.getFullPrice() - item.getOfferPrice();
                                if (discount < item.getFullPrice()) {
                                    eventProperties.setDiscount(discount);
                                }
                            } else {
                                eventProperties.setDiscount(0.0);
                            }

                            if (eventProperties.getDiscount() > 0) {
                                eventProperties.setPxq(eventProperties.getOfferPrice() * eventProperties.getQuantity());
                            } else {
                                eventProperties.setPxq(eventProperties.getFullPrice() * eventProperties.getQuantity());
                            }

                            Event<EventProperties> event = new Event<>();
                            event.setUser_Properties(userProperties);
                            event.setUser_id(idBraze);
                            event.setEvent_type("Product Bought (Verified.v2)");
                            event.setTime(new Date().getTime());
                            event.setEvent_properties(eventProperties);
                            event.setPlatform(AmplitudeUtils.ValidateSource(deliveryOrder.getSource()));
                            events.add(event);
                        }
                    }

                    EventRequest eventRequest = new EventRequest();
                    eventRequest.setEvents(events);
//                    LOG.info("SAVE ITEM ON AMPLITUDE  EVENT-> " + eventRequest);
                    createEventProductBought(eventRequest, asyncActive);

                }
            }
        } catch (Exception e) {
            LOG.warning("method: saveItemOnAmplitude() --> Error: " + e.fillInStackTrace());
            searchOrderLossInOracleProductBougth(fulfilOrdDesc, idBrazeCreate, asyncActive);
        }
    }

    /**
     * Create event to amplitude Order completed
     *
     * @param fulfilOrdDesc
     * @param idBrazeCreate
     */
    public void saveOrderCompletedAmplitude(FulfilOrdColDescDomain.FulfilOrdDescDomain[] fulfilOrdDesc, String idBrazeCreate, boolean asyncActive) {
        try {
            Optional<Boolean> optionalIsActive = APIAlgolia.isActiveLoadDataAmplitudeOrder();

            if (optionalIsActive.isPresent() && optionalIsActive.get()) {
//                LOG.info( "Feature Load Data OrderComplete Amplitude  is Active");
                String getIdBraze = AmplitudeUtils.getIdBraze(fulfilOrdDesc);
                String idBraze = (getIdBraze == null) ? idBrazeCreate : AmplitudeUtils.getIdBraze(fulfilOrdDesc);

                if (idBraze != null) {

                    FulfilOrdColDescDomain.FulfilOrdDescDomain order = Arrays.stream(fulfilOrdDesc).findAny().get();
                    String orderId = order.getCustomerOrderNo();
                    OrderInfoAmplitude infoAmplitude = ApiGatewayService.get().getOrderInfo(orderId);
                    FulfilOrdColDescDomain.FulfilOrdDescDomain.FulfilOrdDtlDomain[] orderDetailArray = order.getFulfilOrdDtl();
                    DeliveryOrder deliveryOrder = AmplitudeUtils.getOrder(orderId);

                    BusinessOrderRequest businessOrderRequest = new BusinessOrderRequest();
                    List<String> items = new ArrayList<>();
                    for (FulfilOrdColDescDomain.FulfilOrdDescDomain.FulfilOrdDtlDomain orderDetail : orderDetailArray) {
                        if (orderDetail.getItemIdOR() != null && !orderDetail.getItemIdOR().isEmpty()) {
                            items.add(orderDetail.getItemIdOR());
                        }
                    }
                    businessOrderRequest.setItems(items);

                    BusinessOrder businessOrder = ApiGatewayService.get().getClassificationBusinessOrder(businessOrderRequest);

                    if (infoAmplitude != null) {

                        UserPropertiesOrder userProperties = new UserPropertiesOrder();
                        userProperties.setCityCode(infoAmplitude.getCityCode() != null ? infoAmplitude.getCityCode() : "");
                        userProperties.setEmail(infoAmplitude.getEmail() != null ? infoAmplitude.getEmail() : "");
                        userProperties.setFirstName(infoAmplitude.getFirstName() != null ? infoAmplitude.getFirstName() : "");
                        userProperties.setLastName(infoAmplitude.getLastName() != null ? infoAmplitude.getLastName() : "");
                        userProperties.setPhone(infoAmplitude.getPhone() != null ? infoAmplitude.getPhone() : "");
                        userProperties.setCountry(infoAmplitude.getCountry() != null ? infoAmplitude.getCountry() : "");
                        userProperties.setPrime(AmplitudeUtils.validateUserPrimeCache(Long.valueOf(deliveryOrder.getIdFarmatodo())).get());

                        EventOrderProperties properties = new EventOrderProperties();
                        properties.setOrderId(infoAmplitude.getOrderId());
                        properties.setOrderDeliveryType(infoAmplitude.getOrderDeliveryType());
                        properties.setTotalOrderPrice(infoAmplitude.getTotalOrderPrice());
                        properties.setTotalOrderItems(infoAmplitude.getTotalOrderItems());
                        properties.setTotalOrderDiscount(infoAmplitude.getTotalOrderDiscount());
                        properties.setOrderChannel("Online");
                        properties.setOrderShippingCost(infoAmplitude.getOrderShippingCost());
                        properties.setOrderPaymentMethod(infoAmplitude.getOrderPaymentMethod());
                        properties.setStoreId(infoAmplitude.getStoreId());
                        properties.setStoreName(infoAmplitude.getStoreName());
                        properties.setOrderCoupon(infoAmplitude.getOrderCoupon());
                        properties.setCourierName(infoAmplitude.getCourierName());
                        properties.setMessengerName(infoAmplitude.getMessengerName());
                        properties.setDistrictName(infoAmplitude.getDistrictName());
                        properties.setRegionName(infoAmplitude.getRegionName());
                        properties.setGender(infoAmplitude.getGender());
                        properties.setStatusOrder(AmplitudeOrderStatusEnum.FACTURADA.toString());
                        setDataCallCenterUser(infoAmplitude, properties);

                        if (Constants.SOURCE_CALL_CENTER.equalsIgnoreCase(infoAmplitude.getSource())) {
                            properties.setOrderChannel("Call Center");
                        }
                        properties.setPrime(Objects.nonNull(infoAmplitude) && userProperties.getPrime() ? userProperties.getPrime() : false);
                        properties.setSelfCheckout(Objects.nonNull(infoAmplitude) && infoAmplitude.isSelfCheckout() ? infoAmplitude.isSelfCheckout() : false);

                        if (Objects.nonNull(infoAmplitude) && Objects.nonNull(infoAmplitude.getCreditCardBin())) {
                            properties.setCreditCardBin(infoAmplitude.getCreditCardBin());
                            properties.setCreditCardLastNumber(infoAmplitude.getCreditCardLastNumber());
                        }

                        if (businessOrder != null) {
                            properties.setCclass(businessOrder.getCclass());
                            properties.setSubclass(businessOrder.getSubclass());
                            properties.setProviders(businessOrder.getProviders());
                            properties.setGroups(businessOrder.getGroups());
                            properties.setDepartments(businessOrder.getDepartments());
                            properties.setDivisions(businessOrder.getDivisions());
                        }

                        Set<String> brandList = new HashSet<>();
                        if (deliveryOrder != null && !deliveryOrder.getItemList().isEmpty()) {
                            for (DeliveryOrderItem item : deliveryOrder.getItemList()) {
                                brandList.add(item.getMarca());
                            }
                            properties.setBrandList(brandList);
                        }

                        Event<EventOrderProperties> event = new Event<>();
                        event.setUser_Properties(userProperties);
                        event.setUser_id(idBraze);
                        event.setEvent_type("Order Completed (Verified.v2)");
                        event.setTime(new Date().getTime());
                        event.setEvent_properties(properties);
                        event.setPlatform(AmplitudeUtils.ValidateSource(deliveryOrder.getSource()));


                        List<Event> events = new ArrayList<>();
                        events.add(event);

                        EventRequest eventRequest = new EventRequest();
                        eventRequest.setEvents(events);
//                        LOG.info("SAVE ORDER COMPLETED AMPLITUDE -> " + eventRequest);
                        createEventOrderCompleted(eventRequest, asyncActive);
                    }
                }
            }
        } catch (Exception e) {
            LOG.warning("method: saveOrderCompletedAmplitude() --> Error: " + e.fillInStackTrace());
            searchOrderLossInOracleOrderCompleted(fulfilOrdDesc, idBrazeCreate, asyncActive);
        }
    }


    /***
     * search Order Loss In Oracle ProductBougth
     * @param fulfilOrdDesc
     */
    public void searchOrderLossInOracleProductBougth(FulfilOrdColDescDomain.FulfilOrdDescDomain[] fulfilOrdDesc, String idBrazeCreate, boolean asyncActive) {
        try {
            if (fulfilOrdDesc != null) {
                String getIdBraze = AmplitudeUtils.getIdBraze(fulfilOrdDesc);
                String idBraze = (getIdBraze == null) ? idBrazeCreate : AmplitudeUtils.getIdBraze(fulfilOrdDesc);
                if (idBraze != null) {
                    FulfilOrdColDescDomain.FulfilOrdDescDomain order = Arrays.stream(fulfilOrdDesc).findAny().get();
                    String orderId = order.getCustomerOrderNo();
                    OrderInfoAmplitudeOMS infoAmplitude = ApiGatewayService.get().getOrderInfoAmplitudeOMS(orderId);
                    UserPropertiesOrder userProperties = new UserPropertiesOrder();

                    if (infoAmplitude != null) {
                        userProperties.setCityCode(infoAmplitude.getCityCode() != null ? infoAmplitude.getCityCode() : "");
                        userProperties.setEmail(infoAmplitude.getEmail() != null ? infoAmplitude.getEmail() : "");
                        userProperties.setFirstName(infoAmplitude.getFirstName() != null ? infoAmplitude.getFirstName() : "");
                        userProperties.setLastName(infoAmplitude.getLastName() != null ? infoAmplitude.getLastName() : "");
                        userProperties.setPhone(infoAmplitude.getPhone() != null ? infoAmplitude.getPhone() : "");
                        userProperties.setPrime(AmplitudeUtils.validateUserPrimeCache(infoAmplitude.getCustomerId().longValue()).get());
                        BrazeClient.sendOrderPushNotification(new SendOrderPush(infoAmplitude.getEmail(), 5, String.valueOf(orderId), null));
                    }

                    List<Event> events = new ArrayList<>();
                    if (infoAmplitude != null && !infoAmplitude.getItems().isEmpty()) {
                        for (OrderInfoItemsAmplitudeOMS item : infoAmplitude.getItems()) {

                            EventProperties eventProperties = new EventProperties();
                            eventProperties.setId(Long.valueOf(item.getId()));
                            eventProperties.setName(item.getMediaDescription());
                            eventProperties.setVariant(item.getGrayDescription());
                            eventProperties.setDepartment(item.getDepartments() != null ? item.getDepartments() : "");
                            eventProperties.setCategory(item.getCategorie() != null ? item.getCategorie() : "");
                            eventProperties.setSubCategory(item.getSubCategory() != null ? item.getSubCategory() : "");
                            eventProperties.setBrand(item.getBrand() != null ? item.getBrand() : "");
                            eventProperties.setQuantity(item.getQuantitySold());
                            eventProperties.setFullPrice(item.getFullPrice());
                            eventProperties.setOfferPrice(item.getOfferPrice());
                            eventProperties.setOrderId(infoAmplitude.getOrderId());
                            eventProperties.setOrderDeliveryType(item.getDeliveryType() != null ? item.getDeliveryType() : "");
                            eventProperties.setOrderChannel("Online");
                            eventProperties.setStatusOrder(AmplitudeOrderStatusEnum.FACTURADA.toString());
                            setDataCallCenterUser(infoAmplitude, eventProperties);

                            if (Constants.SOURCE_CALL_CENTER.equalsIgnoreCase(item.getSource())) {
                                eventProperties.setOrderChannel("Call Center");
                            }
                            eventProperties.setPrime(Objects.nonNull(infoAmplitude) && userProperties.getPrime() ? userProperties.getPrime() : false);
                            eventProperties.setSelfCheckout(Objects.nonNull(infoAmplitude) && infoAmplitude.isSelfCheckout() ? infoAmplitude.isSelfCheckout() : false);
                            eventProperties.setBilled(item.isBilled());

                            BusinessItem businessItem = ApiGatewayService.get().getClassificationBusinessItem("" + item.getId());
                            if (businessItem != null) {
                                eventProperties.setCclass(businessItem.getCclass());
                                eventProperties.setSubclass(businessItem.getSubclass());
                                eventProperties.setProvider(businessItem.getProvider());
                                eventProperties.setDepartmentBusiness(businessItem.getDepartment());
                                eventProperties.setGroup(businessItem.getGroup());
                                eventProperties.setDivision(businessItem.getDivision());
                            }

                            if (item.getOfferPrice() > 0 && item.getFullPrice() > 0) {
                                double discount = item.getFullPrice() - item.getOfferPrice();
                                if (discount < item.getFullPrice()) {
                                    eventProperties.setDiscount(discount);
                                }
                            } else {
                                eventProperties.setDiscount(0.0);
                            }

                            if (eventProperties.getDiscount() > 0) {
                                eventProperties.setPxq(eventProperties.getOfferPrice() * eventProperties.getQuantity());
                            } else {
                                eventProperties.setPxq(eventProperties.getFullPrice() * eventProperties.getQuantity());
                            }

                            Event<EventProperties> event = new Event<>();
                            event.setUser_Properties(userProperties);
                            event.setUser_id(idBraze);
                            event.setEvent_type("Product Bought (Verified.v2)");
                            event.setTime(new Date().getTime());
                            event.setEvent_properties(eventProperties);
                            event.setPlatform(AmplitudeUtils.ValidateSource(item.getSource()));
                            events.add(event);
                        }
                    }

                    EventRequest eventRequest = new EventRequest();
                    eventRequest.setEvents(events);
//                    LOG.info("SAVE ITEM ON AMPLITUDE::searchOrderLossInOracleProductBougth> " + eventRequest);
                    createEventProductBought(eventRequest, asyncActive);
                }
            }
        } catch (Exception e) {
            LOG.warning("method: searchOrderLossInOracleProductBougth() --> Error: " + e.fillInStackTrace());
        }
    }

    /***
     * search Order Loss In Oracle OrderCompleted
     * @param fulfilOrdDesc
     */
    public void searchOrderLossInOracleOrderCompleted(FulfilOrdColDescDomain.FulfilOrdDescDomain[] fulfilOrdDesc, String idBrazeCreate, boolean asyncActive) {
        try {
            if (fulfilOrdDesc != null) {
                String getIdBraze = AmplitudeUtils.getIdBraze(fulfilOrdDesc);
                String idBraze = (getIdBraze == null) ? idBrazeCreate : AmplitudeUtils.getIdBraze(fulfilOrdDesc);
                if (idBraze != null) {
                    FulfilOrdColDescDomain.FulfilOrdDescDomain order = Arrays.stream(fulfilOrdDesc).findAny().get();
                    String orderId = order.getCustomerOrderNo();
                    FulfilOrdColDescDomain.FulfilOrdDescDomain.FulfilOrdDtlDomain[] orderDetailArray = order.getFulfilOrdDtl();
                    OrderInfoAmplitudeOMS infoAmplitude = ApiGatewayService.get().getOrderInfoAmplitudeOMS(orderId);

                    BusinessOrderRequest businessOrderRequest = new BusinessOrderRequest();
                    List<String> items = new ArrayList<>();
                    for (FulfilOrdColDescDomain.FulfilOrdDescDomain.FulfilOrdDtlDomain orderDetail : orderDetailArray) {
                        if (orderDetail.getItemIdOR() != null && !orderDetail.getItemIdOR().isEmpty()) {
                            items.add(orderDetail.getItemIdOR());
                        }
                    }
                    businessOrderRequest.setItems(items);
                    BusinessOrder businessOrder = ApiGatewayService.get().getClassificationBusinessOrder(businessOrderRequest);

                    if (infoAmplitude != null) {
                        UserPropertiesOrder userProperties = new UserPropertiesOrder();
                        userProperties.setCityCode(infoAmplitude.getCityCode() != null ? infoAmplitude.getCityCode() : "");
                        userProperties.setEmail(infoAmplitude.getEmail() != null ? infoAmplitude.getEmail() : "");
                        userProperties.setFirstName(infoAmplitude.getFirstName() != null ? infoAmplitude.getFirstName() : "");
                        userProperties.setLastName(infoAmplitude.getLastName() != null ? infoAmplitude.getLastName() : "");
                        userProperties.setPhone(infoAmplitude.getPhone() != null ? infoAmplitude.getPhone() : "");
                        userProperties.setCountry(infoAmplitude.getCountry() != null ? infoAmplitude.getCountry() : "");
                        userProperties.setPrime(AmplitudeUtils.validateUserPrimeCache(infoAmplitude.getCustomerId().longValue()).get());

                        EventOrderProperties properties = new EventOrderProperties();
                        properties.setOrderId(infoAmplitude.getOrderId());
                        properties.setOrderDeliveryType(infoAmplitude.getOrderDeliveryType());
                        properties.setTotalOrderPrice(infoAmplitude.getTotalOrderPrice());
                        properties.setTotalOrderItems(infoAmplitude.getTotalOrderItems());
                        properties.setTotalOrderDiscount(infoAmplitude.getTotalOrderDiscount());
                        properties.setOrderChannel("Online");
                        properties.setOrderShippingCost(infoAmplitude.getOrderShippingCost());
                        properties.setOrderPaymentMethod(infoAmplitude.getOrderPaymentMethod());
                        properties.setStoreId(infoAmplitude.getStoreId());
                        properties.setStoreName(infoAmplitude.getStoreName());
                        properties.setOrderCoupon(infoAmplitude.getOrderCoupon());
                        properties.setCourierName(infoAmplitude.getCourierName());
                        properties.setMessengerName(infoAmplitude.getMessengerName());
                        properties.setDistrictName(infoAmplitude.getDistrictName());
                        properties.setRegionName(infoAmplitude.getRegionName());
                        properties.setGender(infoAmplitude.getGender());
                        properties.setStatusOrder(AmplitudeOrderStatusEnum.FACTURADA.toString());
                        setDataCallCenterUser(infoAmplitude, properties);

                        if (Constants.SOURCE_CALL_CENTER.equalsIgnoreCase(infoAmplitude.getSource())) {
                            properties.setOrderChannel("Call Center");
                        }
                        properties.setPrime(Objects.nonNull(infoAmplitude) && userProperties.getPrime() ? userProperties.getPrime() : false);
                        properties.setSelfCheckout(Objects.nonNull(infoAmplitude) && infoAmplitude.isSelfCheckout() ? infoAmplitude.isSelfCheckout() : false);

                        if (Objects.nonNull(infoAmplitude) && Objects.nonNull(infoAmplitude.getCreditCardBin())) {
                            properties.setCreditCardBin(infoAmplitude.getCreditCardBin());
                            properties.setCreditCardLastNumber(infoAmplitude.getCreditCardLastNumber());
                        }

                        if (businessOrder != null) {
                            properties.setCclass(businessOrder.getCclass());
                            properties.setSubclass(businessOrder.getSubclass());
                            properties.setProviders(businessOrder.getProviders());
                            properties.setGroups(businessOrder.getGroups());
                            properties.setDepartments(businessOrder.getDepartments());
                            properties.setDivisions(businessOrder.getDivisions());
                        }
                        Set<String> brandList = new HashSet<>();
                        if (infoAmplitude != null && !infoAmplitude.getItems().isEmpty()) {
                            for (OrderInfoItemsAmplitudeOMS item : infoAmplitude.getItems()) {
                                brandList.add(item.getBrand());
                            }
                            properties.setBrandList(brandList);
                        }

                        Event<EventOrderProperties> event = new Event<>();
                        event.setUser_Properties(userProperties);
                        event.setUser_id(idBraze);
                        event.setEvent_type("Order Completed (Verified.v2)");
                        event.setTime(new Date().getTime());
                        event.setEvent_properties(properties);
                        event.setPlatform(AmplitudeUtils.ValidateSource(infoAmplitude.getSource()));

                        List<Event> events = new ArrayList<>();
                        events.add(event);

                        EventRequest eventRequest = new EventRequest();
                        eventRequest.setEvents(events);
//                        LOG.info("SAVE ORDER COMPLETED AMPLITUDE::searchOrderLossInOracleOrderCompleted -> " + eventRequest);
                        createEventOrderCompleted(eventRequest, asyncActive);
                    }
                }
            }
        } catch (IOException e) {
            LOG.warning("method: searchOrderLossInOracleOrderCompleted() --> Error: " + e.fillInStackTrace());
        }
    }

    /**
     * send event asynchronicity  and synchronicity
     *
     * @param eventRequest
     * @param isActiveAsync
     */
    private void createEventProductBought(EventRequest eventRequest, boolean isActiveAsync) {
        if (isActiveAsync) {
//            LOG.info("createEventProductBought::Active");
            ApiGatewayService.get().createEventProductBoughtAsync(eventRequest);
        } else {
//            LOG.info("createEventProductBought::False");
            ApiGatewayService.get().createEventProductBougth(eventRequest);
        }
    }


    /**
     * create Event Order Completed
     *
     * @param eventRequest
     * @param isActiveAsync
     * @author Jhon Chaparro
     */
    private void createEventOrderCompleted(EventRequest eventRequest, boolean isActiveAsync) {
        if (isActiveAsync) {
//            LOG.info("createEventOrderCompleted::Active");
            ApiGatewayService.get().createEventOrderCompletedAsync(eventRequest);
        } else {
//            LOG.info("createEventOrderCompleted::false");
            ApiGatewayService.get().createEventOrderCompleted(eventRequest);
        }
    }

    /**
     * set data only if callcenter in items
     *
     * @param infoAmplitude
     * @param eventProperties
     * @author Jhon Chaparro
     */
    private void setDataCallCenterUser(OrderInfoAmplitude infoAmplitude, EventProperties eventProperties) {
        if (Objects.nonNull(infoAmplitude) && Objects.nonNull(infoAmplitude.getCallCenterId())) {
            eventProperties.setCallCenterId(infoAmplitude.getCallCenterId());
            eventProperties.setCallCenterDoc(infoAmplitude.getCallCenterDoc());
            eventProperties.setCallCenterName(infoAmplitude.getCallCenterName());
        }
    }

    /**
     * set data only if callCenter in order completed
     *
     * @param infoAmplitude
     * @param properties
     * @author Jhon Chaparro
     */
    private void setDataCallCenterUser(OrderInfoAmplitude infoAmplitude, EventOrderProperties properties) {
        if (Objects.nonNull(infoAmplitude) && Objects.nonNull(infoAmplitude.getCallCenterId())) {
            properties.setCallCenterId(infoAmplitude.getCallCenterId());
            properties.setCallCenterDoc(infoAmplitude.getCallCenterDoc());
            properties.setCallCenterName(infoAmplitude.getCallCenterName());
        }
    }

    /***
     * set data only if callcenter in items order not founds
     * @author Jhon Chaparro
     * @param infoAmplitude
     * @param properties
     */
    private void setDataCallCenterUser(OrderInfoAmplitudeOMS infoAmplitude, EventProperties properties) {
        if (Objects.nonNull(infoAmplitude) && Objects.nonNull(infoAmplitude.getCallCenterId())) {
            properties.setCallCenterId(infoAmplitude.getCallCenterId());
            properties.setCallCenterDoc(infoAmplitude.getCallCenterDoc());
            properties.setCallCenterName(infoAmplitude.getCallCenterName());
        }
    }

    /**
     * et data only if callcenter in items  order completed
     *
     * @param infoAmplitude
     * @param properties
     * @author Jhon Chaparro
     */
    private void setDataCallCenterUser(OrderInfoAmplitudeOMS infoAmplitude, EventOrderProperties properties) {
        if (Objects.nonNull(infoAmplitude) && Objects.nonNull(infoAmplitude.getCallCenterId())) {
            properties.setCallCenterId(infoAmplitude.getCallCenterId());
            properties.setCallCenterDoc(infoAmplitude.getCallCenterDoc());
            properties.setCallCenterName(infoAmplitude.getCallCenterName());
        }
    }


}
