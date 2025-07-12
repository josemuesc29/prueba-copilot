package com.imaginamos.farmatodo.networking.talonone;

import com.imaginamos.farmatodo.model.util.AnswerDeduct;
import com.imaginamos.farmatodo.model.coupon.Coupon;
import com.imaginamos.farmatodo.model.order.DeliveryOrder;
import com.imaginamos.farmatodo.model.user.User;

public class TalonOneCouponValidator {
    private final TalonOneService talonOneService;
    public TalonOneCouponValidator(TalonOneService talonOneService) {
        this.talonOneService = talonOneService;
    }

    public AnswerDeduct validateCouponInTalonOne(User user, Coupon coupon, DeliveryOrder deliveryOrder) {
        return talonOneService.validateCouponTalonOne(user, coupon, deliveryOrder);
    }
}
