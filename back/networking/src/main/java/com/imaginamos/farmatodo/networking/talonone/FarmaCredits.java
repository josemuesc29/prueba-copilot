package com.imaginamos.farmatodo.networking.talonone;

import com.imaginamos.farmatodo.model.order.DeliveryOrder;
import com.imaginamos.farmatodo.model.talonone.Coupon;
import com.imaginamos.farmatodo.model.talonone.CouponAutomaticTalon;
import com.imaginamos.farmatodo.model.talonone.DeductDiscount;
import java.util.logging.Logger;

import java.util.List;
import java.util.Objects;

public class FarmaCredits {

    private static final Logger LOG = Logger.getLogger(FarmaCredits.class.getName());

    public void calculateNewPriceWithCredits(DeliveryOrder deliveryOrder, Long userId) {

        if (!deliveryOrderHasUsedCredits(deliveryOrder)) {
           return;
        }

        boolean isUserPrime = Objects.nonNull(userId) ? new TalonOneService().validateUserPrime(userId) : false;

        double oldOfferPrice = deliveryOrder.getOfferPrice();

        double offerPrice = computeNewOfferPrice(deliveryOrder, isUserPrime);

        double totalPrice = computeTotalOrder(deliveryOrder);

        double newOfferPrice = offerPrice;

        if ((offerPrice + deliveryOrder.getUsedCredits()) < oldOfferPrice) {
                newOfferPrice = oldOfferPrice - deliveryOrder.getUsedCredits();
        }

        totalPrice = computeNewTotalPrice(newOfferPrice, totalPrice, deliveryOrder.getUsedCredits(), getTotalDiscountCoupon(deliveryOrder.getCoupon()));

        deliveryOrder.setOfferPrice(newOfferPrice);
        deliveryOrder.setTotalPrice(totalPrice);
    }

    private double computeNewTotalPrice(double offerPrice, double totalPrice, double usedCredits, double totalDiscountCoupon) {
        double totalDiscounts = offerPrice + usedCredits + totalDiscountCoupon;
        return totalPrice - totalDiscounts;
    }

    private double computeTotalOrder(DeliveryOrder deliveryOrder) {
        Double tip = Objects.nonNull(deliveryOrder.getTipPrice()) ? deliveryOrder.getTipPrice() : 0.0;
        Double delivery = Objects.nonNull(deliveryOrder.getDeliveryPrice()) ? deliveryOrder.getDeliveryPrice() : 0.0;
        Double deliveryProvider = Objects.nonNull(deliveryOrder.getProviderDeliveryPrice()) ? deliveryOrder.getProviderDeliveryPrice() : 0.0;
        return deliveryOrder.getSubTotalPrice() + tip + delivery + deliveryProvider;
    }

    private boolean deliveryOrderHasUsedCredits(DeliveryOrder deliveryOrder) {
        return Objects.nonNull(deliveryOrder.getUsedCredits()) && deliveryOrder.getUsedCredits() > 0;
    }

    private double getTotalSavedProducts(DeductDiscount deductDiscount) {
        if (Objects.nonNull(deductDiscount) && Objects.nonNull(deductDiscount.getTotalSaveProducts())) {
            return deductDiscount.getTotalSaveProducts();
        }
        return 0.0;
    }

    private double getTotalSavedProductsPrime(DeductDiscount deductDiscount) {
        //check if user is prime
        if (Objects.nonNull(deductDiscount) && Objects.nonNull(deductDiscount.getDiscountProductsPrime())) {
            return deductDiscount.getDiscountProductsPrime();
        }
        return 0.0;
    }

    private double computeTotalDiscountAutomaticCoupons(List<CouponAutomaticTalon> couponAutomaticTalonList) {
        if (Objects.nonNull(couponAutomaticTalonList) && couponAutomaticTalonList.size() > 0) {
            return couponAutomaticTalonList.stream().mapToDouble(CouponAutomaticTalon::getDiscountCoupon).sum();
        }
        return 0.0;
    }

    private double getTotalDiscountCoupon(Coupon coupon) {
        if (Objects.nonNull(coupon) && Objects.nonNull(coupon.getDiscountCoupon())) {
            return coupon.getDiscountCoupon();
        }
        return 0.0;
    }

    private double computeNewOfferPrice(DeliveryOrder deliveryOrder, boolean isUserPrime) {
        double offerPrice = getTotalSavedProducts(deliveryOrder.getDeductDiscount()) + computeTotalDiscountAutomaticCoupons(deliveryOrder.getCouponAutomaticTalonList());
        if (isUserPrime){
            offerPrice += getTotalSavedProductsPrime(deliveryOrder.getDeductDiscount());
        }
        return offerPrice;
    }
}
