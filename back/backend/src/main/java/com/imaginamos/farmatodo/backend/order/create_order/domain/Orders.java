package com.imaginamos.farmatodo.backend.order.create_order.domain;

import com.google.api.server.spi.response.ConflictException;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.googlecode.objectify.Key;
import com.imaginamos.farmatodo.model.algolia.ImageTrackingConfigAlgolia;
import com.imaginamos.farmatodo.model.algolia.MessageValidateCouponAlgolia;
import com.imaginamos.farmatodo.model.algolia.SelfCheckoutAlgolia;
import com.imaginamos.farmatodo.model.algolia.SelfCheckoutListAlgolia;
import com.imaginamos.farmatodo.model.coupon.Coupon;
import com.imaginamos.farmatodo.model.coupon.CustomerCoupon;
import com.imaginamos.farmatodo.model.customer.CreditCard;
import com.imaginamos.farmatodo.model.customer.CreditCardJwt;
import com.imaginamos.farmatodo.model.customer.Customer;
import com.imaginamos.farmatodo.model.item.EyeDirectionEnum;
import com.imaginamos.farmatodo.model.item.OpticalItemFilter;
import com.imaginamos.farmatodo.model.order.*;
import com.imaginamos.farmatodo.model.payment.PaymentTypeEnum;
import com.imaginamos.farmatodo.model.util.Answer;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.model.util.DeliveryType;
import com.imaginamos.farmatodo.model.util.URLConnections;
import com.imaginamos.farmatodo.networking.algolia.APIAlgolia;
import com.imaginamos.farmatodo.networking.models.amplitude.AmplitudeSessionRequest;
import com.imaginamos.farmatodo.networking.services.ApiGatewayService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.imaginamos.farmatodo.backend.OfyService.ofy;

/**
 * Created by Eric on 27/02/2017.
 */

public class Orders {
  private static final Logger LOG = Logger.getLogger(DeliveryOrder.class.getName());

  @SuppressWarnings("ALL")
  public static JSONObject createValidateOrderJson(long idCustomer,
                                            int idStore,
                                            List<DeliveryOrderItem> deliveryOirderItemList,
                                            String source,
                                            DeliveryType deliveryType,
                                            Map<String, Object> talonOneData) {
    //LOG.warning("method: createValidateOrderJson()");
    JSONObject orderJSON = new JSONObject();
    orderJSON.put("customerId", idCustomer);
    orderJSON.put("storeId", idStore);
    if (!source.equals("APP") && !source.equals("IOs"))
      orderJSON.put("source", source);
    else
      orderJSON.put("source", "IOS");

    if (deliveryType != null) {
      orderJSON.put("deliveryType", deliveryType.getDeliveryType());
    } else {
      orderJSON.put("deliveryType", deliveryType.EXPRESS.toString());
    }

    JSONArray items = new JSONArray();
    JSONArray coupons = new JSONArray();

    for (DeliveryOrderItem item : deliveryOirderItemList) {
      if (item.getCoupon() == null || !item.getCoupon()) {
        JSONObject itemJson = new JSONObject();
        itemJson.put("itemId", item.getId());
        itemJson.put("quantityRequested", item.getQuantitySold());
        items.add(itemJson);
      } else {
        Coupon coupon = ofy().load().type(Coupon.class).filter("itemId", item.getIdItem()).first().now();
        if (validateCoupon(coupon, idCustomer)) {
          JSONObject couponJson = new JSONObject();
          couponJson.put("couponType", coupon.getCouponType().toString());
          couponJson.put("offerId", coupon.getOfferId());
          coupons.add(couponJson);
        }
      }
    }

    orderJSON.put("items", items);
    orderJSON.put("coupons", coupons);

    if (Objects.nonNull(talonOneData)) {
      orderJSON.put("talonOneData", talonOneData);
    }

    return orderJSON;
  }


  @SuppressWarnings("ALL")
  public JSONObject createValidateOrderJsonForAdd(long idCustomer, int idStore, List<ItemOnShop> deliveryOirderItemList, String source) {
    //LOG.warning("method: createValidateOrderJsonForAdd()");
    JSONObject orderJSON = new JSONObject();
    orderJSON.put("customer", idCustomer);
    orderJSON.put("store", idStore);
    orderJSON.put("source", source);

    JSONArray items = new JSONArray();
    for (ItemOnShop item : deliveryOirderItemList) {
      JSONObject itemJson = new JSONObject();
      itemJson.put("item", item.getId());
      itemJson.put("quantityRequested", item.getQuantitySold());
      items.add(itemJson);
    }
    orderJSON.put("items", items);


    return orderJSON;
  }

  public static JSONObject createOrderJson(DeliveryOrder order, List<DeliveryOrderItem> deliveryOrderItems, List<DeliveryOrderProvider> deliveryOrderProvider) {
    //LOG.warning("method: createOrderJson()");
    JSONObject orderJSON = new JSONObject();
    orderJSON.put("customerId", order.getIdFarmatodo());
    orderJSON.put("storeId", order.getIdStoreGroup());
    if (!"APP".equals(order.getSource()) && !"IOs".equals(order.getSource()))
      orderJSON.put("source", order.getSource());
    else
      orderJSON.put("source", "IOS");

    orderJSON.put("storeSelectMode", "AUTOMATIC");
    orderJSON.put("paymentMethodId", order.getPaymentType().getId());

    if (Objects.nonNull(order.getCreditCardToken()) && !order.getCreditCardToken().isEmpty()) {
      orderJSON.put("creditCardToken", order.getCreditCardToken());
    }

    try {
      if (order.getPaymentType().getId() == 3)
        orderJSON.put("paymentCardId", order.getPaymentCardId());
    }catch (Exception e){
      LOG.warning("Error no grave al setear el id de la TC. Mensaje: "+e.getMessage());
    }

    if (Objects.nonNull(order.getFarmaCredits())) {
      orderJSON.put("farmaCredits", order.getFarmaCredits());
    }

    if (Objects.nonNull(order.getTalonOneData())) {
      orderJSON.put("talonOneData", order.getTalonOneData());
    }

    LOG.info("IF(order.getPaymentType().getId() == Constants.PAYMENT_ONLINE_ID) : ["+(order.getPaymentType().getId() == PaymentTypeEnum.TRANSACCIONES_EN_LINEA.getId())+"]");
    if(order.getPaymentType().getId() == PaymentTypeEnum.TRANSACCIONES_EN_LINEA.getId()) {
      orderJSON.put("customerPaymentCardId", order.getPaymentCardId());
    }

    orderJSON.put("customerAddressId", order.getIdAddress());
    orderJSON.put("customerAddressDetails", order.getAddressDetails());
    if (order.getQuotas() != null)
      orderJSON.put("quotas", order.getQuotas());
    else
      orderJSON.put("quotas", 1);
    if (order.getDeliveryType() != null) {
      orderJSON.put("deliveryType", order.getDeliveryType().getDeliveryType());
    } else {
      orderJSON.put("deliveryType", DeliveryType.EXPRESS.toString());
    }

    // Pedido programado:
    if (order.getPickingDate() != null) {
      if((Objects.nonNull(order.getDeliveryType()) && (DeliveryType.EXPRESS.name().compareTo(order.getDeliveryType().name()) == 0)) || Objects.isNull(order.getDeliveryType())) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        orderJSON.put("pickingDate", format.format(order.getPickingDate()) + "-0500");
        //LOG.warning("add pickingDate " +format.format(order.getPickingDate()) + "-0500");
      }
    }

    JSONArray items = new JSONArray();
    JSONArray providers = new JSONArray();
    JSONArray coupons = new JSONArray();

    for (DeliveryOrderItem item : deliveryOrderItems) {
      addItem(order, item, items, coupons);
    }
    // Agrega items de proveedores
    // TODO Pendiente verificar el si se debe o no hacer el mapeo de objetos a Items
    /*
    if (Objects.nonNull(deliveryOrderProvider) && !deliveryOrderProvider.isEmpty()) {
      deliveryOrderProvider.stream()
              .filter(provider -> Objects.nonNull(provider.getItemList()) && !provider.getItemList().isEmpty())
              .forEach(provider -> provider.getItemList()
                      .forEach(item -> {
                        addItem(order, item, items, coupons);
                      })
              );
    }*/

    String itemObservation = "";
    String providerOrderObservations = "";


    if (Objects.nonNull(deliveryOrderProvider)) {
      for (DeliveryOrderProvider provider : deliveryOrderProvider) {
        addProvider(providers, provider);
      }
      providerOrderObservations = deliveryOrderProvider.stream().filter(provider -> Objects.nonNull(provider.getItemList()))
              .map(provider -> provider.getItemList().stream()
                      .filter(item -> (Objects.nonNull(item) && Objects.nonNull(item.getObservations()) && !item.getObservations().isEmpty()))
                        .map(item -> item.getObservations()).collect(Collectors.joining(" , "))).collect(Collectors.joining(" , "));
      itemObservation = itemObservation + (Objects.nonNull(providerOrderObservations) ? providerOrderObservations : "");
      //LOG.warning("itemObservation Provider:  " +itemObservation);
    }
    if(Objects.nonNull(deliveryOrderItems) && !deliveryOrderItems.isEmpty()){
      String itemOrderObservations = deliveryOrderItems.stream().filter(item -> (Objects.nonNull(item) && Objects.nonNull(item.getObservations())))
              .map(item -> item.getObservations()).collect(Collectors.joining(" , "));
      itemObservation = itemObservation + (Objects.nonNull(itemOrderObservations) ? itemOrderObservations : "");
    }
    //LOG.warning("itemObservation:  " +itemObservation);

    orderJSON.put("orderDetails", itemObservation+" "+(Objects.nonNull(order.getOrderDetails()) ? order.getOrderDetails() : ""));

    orderJSON.put("providers", providers);
    orderJSON.put("items", items);
    orderJSON.put("coupons", coupons);
    if (order.getDiscountRate() > 0)
        orderJSON.put("discountRate", order.getDiscountRate());

    if (Objects.nonNull(order.getUrlPrescription())){
      orderJSON.put("urlPrescriptionOptics", order.getUrlPrescription());
    }

    return orderJSON;
  }

  private static void addProvider(JSONArray providers, DeliveryOrderProvider orderProvider) {
    JSONObject itemJson = new JSONObject();
    itemJson.put("id", orderProvider.getId());
    itemJson.put("deliveryPrice", orderProvider.getDeliveryPrice());

    JSONArray items = new JSONArray();

    orderProvider.getItemList().stream().forEach(item -> {
      addItem(item, items);
    });
    itemJson.put("items", items);
    providers.add(itemJson);
  }

  private static void addItem(DeliveryOrderItem item, JSONArray items) {
    JSONObject itemJson = new JSONObject();
    itemJson.put("itemId", item.getId());
    itemJson.put("quantityRequested", item.getQuantitySold());
    itemJson.put("providerDeliveryValue", item.getDeliveryPrice());
    itemJson.put("observations", item.getObservations());
    OpticalItemFilter opticalItemFilter = new Gson().fromJson(item.getFiltersOptical(), OpticalItemFilter.class);
    if(Objects.nonNull(opticalItemFilter) && Objects.nonNull(opticalItemFilter.getEyeDirectionSecondPosition())){
      opticalItemFilter.setEyeDirection(EyeDirectionEnum.SAME);
    }
    itemJson.put("opticalFilters",opticalItemFilter);
    itemJson.put("filters", item.getFilters());
    items.add(itemJson);
  }

  private static void addItem(DeliveryOrder order, DeliveryOrderItem item, JSONArray items, JSONArray coupons) {
    if (item.getCoupon() == null || !item.getCoupon()) {
      JSONObject itemJson = new JSONObject();
      itemJson.put("itemId", item.getId());
      itemJson.put("quantityRequested", item.getQuantitySold());
      itemJson.put("observations", item.getObservations());
      itemJson.put("filters", item.getFilters());
      items.add(itemJson);
    } else {
      Coupon coupon = ofy().load().type(Coupon.class).filter("itemId", item.getIdItem()).first().now();
      if (validateCoupon(coupon, (long) order.getIdFarmatodo())) {
        JSONObject couponJson = new JSONObject();
        couponJson.put("couponType", coupon.getCouponType().toString());
        couponJson.put("offerId", coupon.getOfferId());
        coupons.add(couponJson);
      }
    }
  }

  @SuppressWarnings("ALL")
  public JSONObject createOrderJson(DeliveryOrder order, List<DeliveryOrderItem> deliveryOrderItems) {
    //LOG.warning("method: createOrderJson()");
    return createOrderJson(order, deliveryOrderItems, null);
  }

  @SuppressWarnings("ALL")
  public JSONObject tokenCreditCard(CreditCardJwt creditCardJwt) {
    //LOG.warning("method: tokenCreditCard()");
    JSONObject payuJson = new JSONObject();
    payuJson.put("language", "es");
    payuJson.put("command", "CREATE_TOKEN");
    JSONObject merchantJson = new JSONObject();
      merchantJson.put("apiLogin", URLConnections.PAYU_API_LOGIN);
      merchantJson.put("apiKey", URLConnections.PAYU_API_KEY);
    // merchantJson.put("apiLogin", "VeGmD6jC8bUhEvk"); // PRODUCCION
    // merchantJson.put("apiKey", "19s6Afo75yW1BAZ3ILk3ZreYAd"); // PRODUCCION
    //merchantJson.put("apiLogin", "pRRXKOl8ikMmt9u"); // SANDBOX
    //merchantJson.put("apiKey", "4Vj8eK4rloUd272L48hsrarnUA"); // SANDBOX
    payuJson.put("merchant", merchantJson);
    JSONObject creditCardToken = new JSONObject();
    creditCardToken.put("payerId", creditCardJwt.getPayerId());
    creditCardToken.put("name", creditCardJwt.getName());
    creditCardToken.put("identificationNumber", creditCardJwt.getIdentificationNumber());
    creditCardToken.put("paymentMethod", creditCardJwt.getPaymentMethod());
    creditCardToken.put("number", creditCardJwt.getNumber());
    creditCardToken.put("expirationDate", creditCardJwt.getExpirationDate());
    payuJson.put("creditCardToken", creditCardToken);
    return payuJson;
  }

  public static boolean validateCoupon(Coupon coupon, Long idCustomer) {
    //LOG.warning("method: validateCoupon()");
    if (coupon == null || coupon.getCouponType() == null){
      return false;
    }
    switch (coupon.getCouponType()) {
      case PAYMETHOD:
        return validatePaymentMethod(coupon, idCustomer).isConfirmation();
      default:
        return true;
    }
  }

  public static Answer validatePaymentMethod(Coupon coupon, Long idCustomer) {
    //LOG.warning("method: validatePaymentMethod()");
    final boolean couponFranchise[] = {false};
    final CreditCard[] creditCard = {null};
    final boolean[] hasCoupon = {false};

/*
        try {
            LOG.warning("customerId: [" + idCustomer + "]");
            List<CreditCard> creditCards = CoreConnection.getListRequest(URLConnections.URL_CUSTOMER + idCustomer + "/creditcard", CreditCard.class);
            if (creditCards != null && !creditCards.isEmpty()) {
                creditCards.stream().filter(CreditCard::getDefaultCard).forEach(creditCard1 -> {
                            LOG.warning("Payment Method: credit card -> [" + creditCard1.getPaymentMethod() + "] || coupon : [" + coupon.getPayMethodType().getPayMethodType() + "]");
                            if (creditCard1.getPaymentMethod().equals(coupon.getPayMethodType().getPayMethodType())) {
                                couponFranchise[0] = true;
                            }
                            creditCard[0] = creditCard1;
                        });
            }
        } catch (BadRequestException e) {
            e.printStackTrace();
            LOG.warning(e.getMessage());
        }
*/

    //hasCoupon[0] = true;
    Answer answer = new Answer();
    answer.setConfirmation(true);
//        if (!hasCoupon[0]) {
//            answer.setConfirmation(true);
//        }
//        if (couponFranchise[0]) {
//            answer.setConfirmation(true);
//        } else {
//            answer.setConfirmation(false);
//            StringBuilder message = new StringBuilder();
//            message.append(Constants.FRANCHISE_MESSAGE);
//            message.append(coupon.getPayMethodType().getPayMethodType());
//            message.append(".");
//            answer.setMessage(message.toString());
//            LOG.warning(message.toString());
//        }
    LOG.warning("por el momento siempre respondere: [true]");
    return answer;
  }

  public Answer validateCouponRestrictionValue(DeliveryOrder deliveryOrder, Coupon coupon) {
    //LOG.warning("method: validateCouponRestrictionValue()");
    Answer answer = new Answer();
    BigDecimal subtotalValue = new BigDecimal(deliveryOrder.getSubTotalPrice());
    //LOG.warning("Subtotal de la orden: $" + subtotalValue);
    Long couponRestrictionValue = coupon.getRestrictionValue() == null ? 0L : coupon.getRestrictionValue();
    BigDecimal restrictionValue = new BigDecimal(couponRestrictionValue);
    //LOG.warning("Valor de la restriccion del cupon: $" + restrictionValue);
    if (coupon.getHasRestriction() != null && coupon.getHasRestriction()) {
      if (restrictionValue.compareTo(subtotalValue) == 1) {

        MessageValidateCouponAlgolia messageValidateCouponAlgolia = APIAlgolia.getMessageValidateCoupon();

        if(messageValidateCouponAlgolia != null && !messageValidateCouponAlgolia.getMessage().isEmpty()){
          String message = messageValidateCouponAlgolia.getMessage();
          String couponName = coupon.getName();
          String restrictValue = String.valueOf(restrictionValue.intValue());
          String missing = String.valueOf(restrictionValue.intValue() - subtotalValue.intValue());

          String messageOne = message.replace("NAMECOUPON",couponName);
          String messageTwo = messageOne.replace("RESTRICTIONCOUPON",restrictValue);
          String messageComplete = messageTwo.replace("MISSING",missing);

          answer.setConfirmation(false);
          answer.setMessage(messageComplete);
          //LOG.warning(messageComplete);
        }

      } else {
        LOG.info("El cupon cumple la restriccion de valor.");
        answer.setConfirmation(true);
      }
    } else {
      LOG.info("La orden cumple las condiciones para aplicar el cupon.");
      answer.setConfirmation(true);
    }
    return answer;
  }

  public JSONObject createOrderStatusUpdate(final DeliveryOrderStatus orderStatus) {
    //LOG.warning("method: createOrderStatusUpdate()");
    JSONObject orderStatusJson = new JSONObject();
    orderStatusJson.put("uuid", orderStatus.getUuid());
    orderStatusJson.put("order_no", orderStatus.getOrder_no());
    orderStatusJson.put("status", orderStatus.getStatus());
    orderStatusJson.put("messenger", orderStatus.getMessenger());
    orderStatusJson.put("phone", orderStatus.getPhone());
    orderStatusJson.put("url", orderStatus.getUrl());
    orderStatusJson.put("paymentTerms", orderStatus.getPaymentTerms());
    orderStatusJson.put("paymentMeans", orderStatus.getPaymentMeans());
    orderStatusJson.put("deliveryTerms", orderStatus.getDeliveryTerms());
    return orderStatusJson;
  }

  /**
   * Validar obligatoriedad de la orden
   * @param order
   * @throws ConflictException
   */
  public void validateOrder(DeliveryOrder order) throws ConflictException {
    if (order.getIdAddress() <= 0)
      throw new ConflictException(Constants.ERROR_ADDRESS_NULL);
    if (order.getPaymentType() == null || order.getPaymentType().getId() <= 0)
      throw new ConflictException(Constants.ERROR_PAYMENT_TYPE_INVALID);
    if (order.getIdFarmatodo() <= 0)
      throw new ConflictException(Constants.ERROR_ID_CLIENTE_NULL);
    if (order.getIdStoreGroup() == null || order.getIdStoreGroup().isEmpty())
      throw new ConflictException(Constants.ERROR_ID_STORE_GROUP_NULL);
    if (order.getSource() == null || order.getSource().isEmpty())
      throw new ConflictException(Constants.ERROR_SOURCE_NULL);
    if (order.getPaymentType().getId() == 3 && order.getPaymentCardId() <= 0)
      throw new ConflictException(Constants.ERROR_PAYMENT_CARD_ID_INVALID);
    if (order.getItemList() == null || order.getItemList().size() == 0)
      throw new ConflictException(Constants.ERROR_ITEM_LIST_NULL);
    if (order.getQuotas() <= 0)
      throw new ConflictException(Constants.ERROR_QUOTAS_NULL);
    if (order.getCustomerPhone() == null || order.getCustomerPhone().isEmpty())
      throw new ConflictException(Constants.ERROR_CUSTOMER_PHONE_NULL);
    if (order.getDiscountRate() <= 0)
      throw new ConflictException(Constants.ERROR_DISCOUNT_INVALID);
  }

  public void validateKeyClient(String keyClient, DeliveryOrder order) throws ConflictException {
    String[] parts = keyClient.split("\\.");
    LOG.info("keyClient: " + keyClient);
    if (parts.length == 0)
      throw new ConflictException(Constants.INVALID_KEY_CLIENT);
    try {
      long idAddress = Long.parseLong(parts[0]) + 1;
      long idFarmatodo = Long.parseLong(parts[1]) + 2;
      //long customerPhone = Long.parseLong(parts[2]) + 3;

      //if (order.getIdAddress() != idAddress || order.getIdFarmatodo() != idFarmatodo || Long.parseLong(order.getCustomerPhone()) != customerPhone)
      if (order.getIdAddress() != idAddress || order.getIdFarmatodo() != idFarmatodo)
        throw new ConflictException(Constants.INVALID_KEY_CLIENT);

    } catch (Exception e){
      throw new ConflictException(Constants.INVALID_KEY_CLIENT);
    }
  }


  public void getImagesTrackingOrder(OrderInfoStatus orderInfoStatus){


    ImageTrackingConfigAlgolia imageTrackingConfigAlgolia = APIAlgolia.getImageTracking();


    if (Objects.nonNull(imageTrackingConfigAlgolia)) {

      if (Objects.nonNull(imageTrackingConfigAlgolia.getImageCustomerHouse())) {

        if (Objects.equals(orderInfoStatus.getCustomerGender(), "F") && imageTrackingConfigAlgolia.getImageCustomerHouse().getUrlImageWoman() != null) {
          orderInfoStatus.setImageCustomerHouse(imageTrackingConfigAlgolia.getImageCustomerHouse().getUrlImageWoman());
        }else if (Objects.equals(orderInfoStatus.getCustomerGender(), "M") && imageTrackingConfigAlgolia.getImageCustomerHouse().getUrlImageMan() != null){
          orderInfoStatus.setImageCustomerHouse(imageTrackingConfigAlgolia.getImageCustomerHouse().getUrlImageMan());
        }else{
          orderInfoStatus.setImageCustomerHouse(imageTrackingConfigAlgolia.getImageCustomerHouse().getUrlImageDefault());
        }
      }

      if (Objects.nonNull(imageTrackingConfigAlgolia.getImageStore())) {

        if (Objects.equals(orderInfoStatus.getCustomerGender(), "F") && imageTrackingConfigAlgolia.getImageStore().getUrlImageWoman() != null) {
          orderInfoStatus.setImageStore(imageTrackingConfigAlgolia.getImageStore().getUrlImageWoman());
        }else if (Objects.equals(orderInfoStatus.getCustomerGender(), "M") && imageTrackingConfigAlgolia.getImageCustomerHouse().getUrlImageMan() != null){
          orderInfoStatus.setImageStore(imageTrackingConfigAlgolia.getImageStore().getUrlImageMan());
        }else{
          orderInfoStatus.setImageStore(imageTrackingConfigAlgolia.getImageStore().getUrlImageDefault());
        }
      }

      if (Objects.nonNull(imageTrackingConfigAlgolia.getImageMotorbikeDelivery())) {

        if (Objects.equals(orderInfoStatus.getCustomerGender(), "F") && imageTrackingConfigAlgolia.getImageMotorbikeDelivery().getUrlImageWoman() != null) {
          orderInfoStatus.setImageMotorbikeDelivery(imageTrackingConfigAlgolia.getImageMotorbikeDelivery().getUrlImageWoman());
        }else if (Objects.equals(orderInfoStatus.getCustomerGender(), "M") && imageTrackingConfigAlgolia.getImageCustomerHouse().getUrlImageMan() != null){
          orderInfoStatus.setImageMotorbikeDelivery(imageTrackingConfigAlgolia.getImageMotorbikeDelivery().getUrlImageMan());
        }else{
          orderInfoStatus.setImageMotorbikeDelivery(imageTrackingConfigAlgolia.getImageMotorbikeDelivery().getUrlImageDefault());
        }
      }
      }

    }

  public static void deleteCouponForCustomerDefault(Key<Customer> customerKey, CreateOrderRequestCore requestCore) {
    if (requestCore.getSelfCheckout() != null && requestCore.getSelfCheckout().getIsSelfCheckout() && requestCore.getSelfCheckout().getIdBox() != null) {
      SelfCheckoutAlgolia selfCheckoutAlgolia = APIAlgolia.getSelfCheckout();
      if (!Objects.isNull(selfCheckoutAlgolia) && !Objects.isNull(selfCheckoutAlgolia.getCustomerByStoresAndBox())) {
        Optional<SelfCheckoutListAlgolia> selfCheckoutListAlgolia = selfCheckoutAlgolia.getCustomerByStoresAndBox().stream()
                .filter(checkoutListAlgolia -> Objects.equals(checkoutListAlgolia.getStore(), requestCore.getStoreId()) && Objects.equals(checkoutListAlgolia.getBox(), requestCore.getSelfCheckout().getIdBox()))
                .limit(1).findFirst();
        if (selfCheckoutListAlgolia.isPresent()) {
          deleteCouponV2(customerKey);
        }
      }
    }
  }

  public static boolean deleteCouponV2(final Key<Customer> customerKey) {
    try {
      LOG.info("deleteCoupon(" + customerKey.toString() + ")");
      final List<CustomerCoupon> customerCoupons = ofy().load().type(CustomerCoupon.class).filter("customerKey", customerKey).orderKey(false).list();
      LOG.info("IF(customerCoupons!=null && !customerCoupons.isEmpty()) : [" + (customerCoupons != null && !customerCoupons.isEmpty()) + "]");
      if (customerCoupons != null && !customerCoupons.isEmpty()) {
        customerCoupons.sort(Comparator.comparing(a -> a.getUseTime()));
        final int positionLastCupon = customerCoupons.size() - 1;
        final CustomerCoupon couponToDelete = customerCoupons.get(positionLastCupon);
        if (couponToDelete != null) {
          LOG.info("deleteCoupon cupon Eliminado(" + couponToDelete.getCustomerCouponId() + ")");
          ofy().delete().entity(couponToDelete).now();
          return true;
        }
        return false;
      }
      return false;
    } catch (Exception e) {
      LOG.warning("Error al eliminar cupon cuando el cliente elimina todo el carrito... Mensaje: " + e.getMessage());
      return false;
    }
  }

  public static void saveAmplitudeSessionId(CommandCreateOrder commandCreateOrder, CreatedOrder createdOrder) {
    try {
      Long sessionId = Long.parseLong(commandCreateOrder.getHttpServletRequest().getHeader("amplitudeSessionId"));
      if (sessionId != null) {
        AmplitudeSessionRequest request = new AmplitudeSessionRequest();
        request.setOrderId(createdOrder.getId());
        request.setSessionId(sessionId);
        LOG.info("request -> " + request.toString());
        CompletableFuture.runAsync(() -> ApiGatewayService.get().saveAmplitudeSessionId(request));
      }
    } catch (Exception e) {
      LOG.warning(e.getMessage());
    }
  }


  }


