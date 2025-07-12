package com.imaginamos.farmatodo.model.algolia

class CouponAlgoliaPopUp {
    var couponsToValidate: List<CouponPopUpData>? = null

    override fun toString(): String {
        return "CouponAlgoliaPopUp(couponsToValidate=$couponsToValidate)"
    }

}

class CouponPopUpData{
    var couponKey: String = ""
    var confirmation: String = ""

    override fun toString(): String {
        return "CouponPopUpData(couponKey='$couponKey', confirmation='$confirmation')"
    }

}