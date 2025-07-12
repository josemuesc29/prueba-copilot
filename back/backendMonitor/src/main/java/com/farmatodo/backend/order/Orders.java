package com.farmatodo.backend.order;

import com.imaginamos.farmatodo.model.coupon.Coupon;
import com.imaginamos.farmatodo.model.customer.CreditCard;
import com.imaginamos.farmatodo.model.customer.CreditCardJwt;
import com.imaginamos.farmatodo.model.customer.CustomerJSON;
import com.imaginamos.farmatodo.model.monitor.OrderItem;
import com.imaginamos.farmatodo.model.order.DeliveryOrder;
import com.imaginamos.farmatodo.model.order.DeliveryOrderItem;
import com.imaginamos.farmatodo.model.order.ItemOnShop;
import com.imaginamos.farmatodo.model.util.Answer;
import com.imaginamos.farmatodo.model.util.DeliveryType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Logger;

import static com.farmatodo.backend.OfyService.ofy;

/**
 * Created by Eric on 27/02/2017.
 */

public class Orders {
  private static final Logger LOG = Logger.getLogger(DeliveryOrder.class.getName());


  @SuppressWarnings("ALL")
  public JSONObject createValidateOrderJson(long idCustomer,
                                            int idStore,
                                            List<DeliveryOrderItem> deliveryOirderItemList,
                                            String source,
                                            DeliveryType deliveryType) {
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

    return orderJSON;
  }

  public JSONObject createValidateOrderMonitorJson(long idCustomer,
                                            int idStore,
                                            List<OrderItem> orderItems,
                                            String source,
                                            DeliveryType deliveryType) {
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

    for (OrderItem item : orderItems) {
      JSONObject itemJson = new JSONObject();
      itemJson.put("itemId", item.getItemId());
      itemJson.put("quantityRequested", item.getQuantityRequested());
      items.add(itemJson);
    }

    orderJSON.put("items", items);
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

  @SuppressWarnings("ALL")
  public JSONObject createOrderJson(DeliveryOrder order, List<DeliveryOrderItem> deliveryOrderItems, String tokenCardId) {
    //LOG.warning("method: createOrderJson()");
    JSONObject orderJSON = new JSONObject();
    orderJSON.put("customerId", order.getIdFarmatodo());
    orderJSON.put("storeId", order.getIdStoreGroup());
    if (!order.getSource().equals("APP") && !order.getSource().equals("IOs"))
      orderJSON.put("source", order.getSource());
    else
      orderJSON.put("source", "IOS");

    orderJSON.put("storeSelectMode", "AUTOMATIC");
    orderJSON.put("paymentMethodId", order.getPaymentType().getId());
    orderJSON.put("paymentCard", order.getPaymentCardId());
    orderJSON.put("tokenCardId", tokenCardId);
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
    orderJSON.put("orderDetails", order.getOrderDetails());

    JSONArray items = new JSONArray();
    JSONArray coupons = new JSONArray();

    for (DeliveryOrderItem item : deliveryOrderItems) {
      if (item.getCoupon() == null || !item.getCoupon()) {
        JSONObject itemJson = new JSONObject();
        itemJson.put("itemId", item.getId());
        itemJson.put("quantityRequested", item.getQuantitySold());
        items.add(itemJson);
      } else {

        Coupon coupon = ofy().load().type(Coupon.class).filter("itemId", item.getIdItem()).first().now();
        if (validateCoupon(coupon, (long) order.getIdFarmatodo())) {
          JSONObject couponJson = new JSONObject();
          couponJson.put("couponType", coupon.getCouponType().toString());
          //if(!coupon.getCouponType().equals(VALUE))
          couponJson.put("offerId", coupon.getOfferId());
                    /*else
                        couponJson.put("discountValue", coupon.getDiscountValue());
                    couponJson.put("hasRestriction", coupon.getHasRestriction());
                    if(coupon.getHasRestriction())
                        couponJson.put("restrictionValue", coupon.getRestrictionValue());*/
          coupons.add(couponJson);
        }
      }

    }
    orderJSON.put("items", items);
    orderJSON.put("coupons", coupons);

    return orderJSON;
  }

  @SuppressWarnings("ALL")
  public JSONObject tokenCreditCard(CreditCardJwt creditCardJwt) {
    //LOG.warning("method: tokenCreditCard()");
    JSONObject payuJson = new JSONObject();
    payuJson.put("language", "es");
    payuJson.put("command", "CREATE_TOKEN");
    JSONObject merchantJson = new JSONObject();
    merchantJson.put("apiLogin", "VeGmD6jC8bUhEvk");
    merchantJson.put("apiKey", "19s6Afo75yW1BAZ3ILk3ZreYAd");
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


  public JSONObject tokenCustomerMercadoPago(CustomerJSON customerJSON, String typeDocument) {
    LOG.warning("method: tokenCustomerMercadoPago()");
    JSONObject mercadoPagoJson = new JSONObject();
    mercadoPagoJson.put("email", customerJSON.getEmail());
    mercadoPagoJson.put("first_name", customerJSON.getFirstName());
    mercadoPagoJson.put("last_name", customerJSON.getLastName());
    JSONObject phoneJson = new JSONObject();
    phoneJson.put("number", customerJSON.getPhone());
    mercadoPagoJson.put("phone", phoneJson);
    JSONObject cardDocumentJson = new JSONObject();
    cardDocumentJson.put("number", customerJSON.getDocumentNumber().toString());
    cardDocumentJson.put("type", typeDocument);
    mercadoPagoJson.put("identification", cardDocumentJson);
    return mercadoPagoJson;
  }

  public boolean validateCoupon(Coupon coupon, Long idCustomer) {
    LOG.warning("method: validateCoupon()");
    switch (coupon.getCouponType()) {
      case PAYMETHOD:
        return validatePaymentMethod(coupon, idCustomer).isConfirmation();
      default:
        return true;
    }
  }

  Answer validatePaymentMethod(Coupon coupon, Long idCustomer) {
    LOG.warning("method: validatePaymentMethod()");
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

  Answer validateCouponRestrictionValue(DeliveryOrder deliveryOrder, Coupon coupon) {
    LOG.warning("method: validateCouponRestrictionValue()");
    Answer answer = new Answer();
    BigDecimal subtotalValue = new BigDecimal(deliveryOrder.getSubTotalPrice());
    LOG.warning("Subtotal de la orden: $" + subtotalValue);
    BigDecimal restrictionValue = new BigDecimal(coupon.getRestrictionValue());
    LOG.warning("Valor de la restriccion del cupon: $" + restrictionValue);
    if (coupon.getHasRestriction() != null && coupon.getHasRestriction()) {
      if (restrictionValue.compareTo(subtotalValue) == 1) {
        StringBuilder message = new StringBuilder();
        message.append("El cupon [");
        message.append(coupon.getName());
        message.append("] aplica para compras superiores a $");
        message.append(coupon.getRestrictionValue());
        message.append(".");
        answer.setConfirmation(false);
        answer.setMessage(message.toString());
        LOG.warning(message.toString());
      } else {
        LOG.warning("El cupon cumple la restriccion de valor.");
        answer.setConfirmation(true);
      }
    } else {
      LOG.warning("La orden cumple las condiciones para aplicar el cupon.");
      answer.setConfirmation(true);
    }
    return answer;
  }

}
