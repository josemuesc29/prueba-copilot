package com.imaginamos.farmatodo.model.order

import com.google.appengine.repackaged.com.google.gson.Gson
import com.imaginamos.farmatodo.model.item.EyeDirectionEnum
import com.imaginamos.farmatodo.model.provider.ShippingCostItemsMarkeplaceRequest
import java.util.Date

class CreateOrderRequestCore {


    var source: String? = null
    var customerId: Long? = null
    var customerIdCallCenter: Long? = null
    var storeId: Long? = null
    var storeSelectMode: String? = null
    var customerAddressId: Long? = null
    var customerAddressDetails: String? = null
    var paymentMethodId: Long? = null
    var customerPaymentCardId: Long? = null
    var paymentCard: Long? = null
    var quotas: Int? = null
    var paymentDetails: String? = null
    var orderDetails: String? = null
    var pickingDate: String? = null
    var deliveryType: String? = null
    var items: List<OrderDetailRequest>? = null
    var coupons: List<OrderCouponRequest>? = null
    var providers: List<OrderProviderRequest>? = null
    var urlPrescriptionOptics: String? = null
    var creditCardToken: String? = null
    var typePersonPSE: String? = null
    var ipAddress: String? = null
    var financialInstitutions: Long? = null
    var identification: Identification? = null
    var selfCheckout: SelfCheckout? = null
    var coupon: String? = null
    var idCustomerWebSafe: String? = null
    var isPrimeMixedPSE: Boolean? = false
    var isFreeDelivery: Boolean? = false
    var typeSubscription: Long? = null
    var typeSubscriptionQuantity: Int? = 0
    var orderPrimeId: Long? = 0L
    var farmaCredits: Long? = 0L
    var talonOneData: Map<String, Object>? = null
    var deliveryHome: String? = null
    var shippingCostItemsMarkeplaceRequest: ShippingCostItemsMarkeplaceRequest? = null
    var idOptimalRoute: String? = null
    var optionSelectedPopUp: String? = null
    var buildCodeNumberApp: Int = 0

    inner class OrderDetailRequest {

        var itemId: Int? = null
        var quantityRequested: Int? = null
        var observations: String? = null
        var providerDeliveryValue: Double? = null
        var opticalFilters: OpticalItemFilter? = null
        var filters: String? = null

        override fun toString(): String {
            return Gson().toJson(this)
        }

    }

     inner class OrderCouponRequest {
        var couponType: String? = null
        var offerId: Int? = null

         override fun toString(): String {
             return Gson().toJson(this)
         }
    }


    inner class OpticalItemFilter {
        var power: Double? = null
        var cylinder: Double? = null
        var axle: Int? = null
        var addition: String? = null
        var lensColor: String? = null
        var eyeDirection: EyeDirectionEnum? = null

        override fun toString(): String {
            return Gson().toJson(this)
        }

    }

     inner class OrderProviderRequest {
        var id: Int? = null
        var deliveryPrice: Double? = null
        var items: List<OrderDetailRequest>? = null
         override fun toString(): String {
             return Gson().toJson(this)
         }
    }





}
