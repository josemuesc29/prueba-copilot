/*
 * Farmatodo Colombia.
 * Copyright 2018.
 */
package co.farmatodo.cv.core.application;

import co.farmatodo.cv.core.api.constants.CouponType;
import co.farmatodo.cv.core.api.constants.DeliveryTypeEnum;
import co.farmatodo.cv.core.api.constants.SourceEnum;
import co.farmatodo.cv.core.api.domain.oms.ShoppingCartDomainV2;
import co.farmatodo.cv.core.api.domain.oms.ShoppingCartResponseDomain;
import co.farmatodo.cv.core.api.events.ResponseEvent;
import co.farmatodo.cv.core.api.manager.oms.ShoppingCartManager;
import co.farmatodo.cv.core.application.components.oms.OfferCalculateComponent;
import co.farmatodo.cv.core.application.entity.oms.ShoppingCart;
import co.farmatodo.cv.core.application.entity.oms.ShoppingCartRequestItem;
import co.farmatodo.cv.core.application.services.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * Service handler for Shopping Cart.
 *
 * @author <a href="mailto:diego.poveda@farmatodo.com">Diego Alejandro Poveda Sanchez</a>
 * @version 3.0-SNAPSHOT
 * @since 1.8
 */
@Service
public class ShoppingCartManagerHandler implements ShoppingCartManager {

  private static final Logger LOG = LoggerFactory.getLogger(ShoppingCartManagerHandler.class);

  @Autowired
  private OfferCalculateComponent offerCalculateComponent;

  @Override
  @Transactional(readOnly = true)
  public ResponseEvent<ShoppingCartResponseDomain> priceDelivery(final ShoppingCartDomainV2 shoppingCartDomain) {
    LOG.info("method: priceDelivery INIT {} , request -> {}",  LocalDateTime.now(), shoppingCartDomain.toString());
    final String validationResult = validate(shoppingCartDomain);
    if (Objects.nonNull(shoppingCartDomain.getTalonOneData())) {
      String couponOfferId = getCouponOfferId(shoppingCartDomain);
      if (Objects.nonNull(couponOfferId)) {
        shoppingCartDomain.getTalonOneData().put(Constants.COUPON_OFFER_ID_KEY, couponOfferId);
      }
    }
    if(!validationResult.isEmpty())
      return new ResponseEvent<ShoppingCartResponseDomain>().badRequest(validationResult);
    return offerCalculateComponent.priceDelivery(shoppingCartDomain,null);
  }

  private String getCouponOfferId(ShoppingCartDomainV2 shoppingCartDomain) {
    return Optional.ofNullable(shoppingCartDomain.getCoupons())
            .map(coupons -> coupons.stream()
                    .filter(c -> Objects.nonNull(c.getCouponType()) && c.getCouponType().equals(CouponType.VALUE))
                    .filter(c -> Objects.nonNull(c.getOfferId()))
                    .findFirst()
                    .map(coupon -> coupon.getOfferId().toString())
                    .orElse(null))
            .orElse(null);
  }

  private String validate(ShoppingCartDomainV2 shoppingCartDomain){
    StringBuilder errors = new StringBuilder();

    if(Objects.isNull(shoppingCartDomain))
      return "Event is null.";
    if(Objects.isNull(shoppingCartDomain.getStoreId()))
      errors.append("Event.storeId is null.");
    if(Objects.isNull(shoppingCartDomain.getSource()))
      errors.append("Event.source is null.");
    if(Objects.isNull(shoppingCartDomain.getDeliveryType()))
      errors.append("Event.deliveryType is null.");
    if(Objects.isNull(shoppingCartDomain.getCustomerId()))
      errors.append("Event.customerId is null.");

    if(Objects.isNull(shoppingCartDomain.getItems())) {
      errors.append("Event.items is null.");
    }else if(Objects.nonNull(shoppingCartDomain.getItems()) && shoppingCartDomain.getItems().isEmpty()){
      errors.append("Event.items is empty.");
    }

    // Validate Store
    validateStore(shoppingCartDomain);

    return errors.toString();
  }

  private void validateStore(ShoppingCartDomainV2 shoppingCartDomain){
    boolean isWrongDeliveryType = Objects.nonNull(shoppingCartDomain) ? this.isExpressForStore(shoppingCartDomain) : false;
    if (isWrongDeliveryType) {
      LOG.info("cambia deliveryType a express para " + shoppingCartDomain.getDeliveryType() + "--" + shoppingCartDomain.getStoreId());
      shoppingCartDomain.setDeliveryType(DeliveryTypeEnum.EXPRESS);
    }

    if (Objects.nonNull(shoppingCartDomain) && Objects.nonNull(shoppingCartDomain.getDeliveryType())) {
      //FIX deliveryType
      if (DeliveryTypeEnum.EXPRESS.equals(shoppingCartDomain.getDeliveryType())) {
        Long storeId = Objects.nonNull(shoppingCartDomain.getStoreId()) ? shoppingCartDomain.getStoreId() : 0L;
        if (Long.compare(storeId, 1000L) == 0) {
          shoppingCartDomain.setDeliveryType(DeliveryTypeEnum.NATIONAL);
        } else if (Long.compare(storeId, 1001L) == 0) {
          shoppingCartDomain.setDeliveryType(DeliveryTypeEnum.ENVIALOYA);
        }
      }
    }
  }

  @Override
  public ResponseEvent<ShoppingCart> loadShoppingCart(Long customerToken, Long storeId, SourceEnum source) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ResponseEvent<ShoppingCart> clearShoppingCart(Long customerId, Long storeId, SourceEnum source) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ResponseEvent<ShoppingCartRequestItem> getItemFromShoppingCart(Long customerId, Long productId, SourceEnum source) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ResponseEvent<ShoppingCart> addItemToShoppingCart(Long customerId, Long storeId, Long productId, Integer quantity, boolean add, SourceEnum source) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ResponseEvent<ShoppingCart> deleteItemFromShoppingCart(Long customerId, Long productId, SourceEnum source) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ResponseEvent<ShoppingCart> redeemCoupon(Long customerId, String coupon, SourceEnum source) {
    throw new UnsupportedOperationException();
  }

  private boolean isExpressForStore(ShoppingCartDomainV2 shoppingCartJson) {
    return Objects.nonNull(shoppingCartJson)
            && Objects.nonNull(shoppingCartJson.getDeliveryType())
            && shoppingCartJson.getStoreId() != 1000
            && shoppingCartJson.getDeliveryType().equals(DeliveryTypeEnum.NATIONAL);

  }

  @Override
  public ResponseEvent ping() {
    return null;
  }
}
