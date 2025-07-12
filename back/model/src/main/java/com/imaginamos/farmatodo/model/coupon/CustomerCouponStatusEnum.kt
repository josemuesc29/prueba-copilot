package com.imaginamos.farmatodo.model.coupon

enum class CustomerCouponStatusEnum

(private var customerCouponStatus: String) {
    IN_SHOPPING_CART("IN_SHOPPING_CART"),
    USED("USED"),
    AVAILABLE("AVAILABLE");

    override fun toString(): String {
        return "CustomerCouponStatusEnum(customerCouponStatus='$customerCouponStatus')"
    }


}
