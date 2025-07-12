package com.imaginamos.farmatodo.model.order

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper

class FulfilOrdColDescDomain {

    var fulfilOrdDesc: Array<FulfilOrdDescDomain>? = null

    class FulfilOrdDescDomain {

        var customerOrderNo: String? = null
        var fulfillOrderNo: String? = null
        var sourceLocId: Long = 0
        var sourceLocType: String? = null
        var fulfillLocId: Long = 0
        var fulfillLocType: String? = null
        var deliveryType: String? = null
        var deliveryCharges: Double = 0.toDouble()
        var deliveryChargesCurr: String? = null
        var partialDeliveryInd: String? = null
        var comments: String? = null
        var consumerDeliveryDate: String? = null
        var fulfilOrdCustDesc: FulfilOrdCustDescDomain? = null
        var fulfilOrdDtl: Array<FulfilOrdDtlDomain>? = null

        class FulfilOrdCustDescDomain {
            var customerIdATOM: String? = null
            var firstName: String? = null
            var lastName: String? = null
            var nickname: String? = null
            var address: String? = null
            var address2: String? = null
            var cityId: String? = null
            var stateId: String? = null
            var countryId: String? = null
            var phone: String? = null
            var email: String? = null
        }

        class FulfilOrdDtlDomain {
            var itemIdOR: String? = null
            var unitPrice: Double = 0.toDouble()
            var quantity: Long = 0
            var standardUom: String? = null
            var transactionUom: String? = null
            var substituteInd: String? = null
            var retailCurr: String? = null
        }

    }

    fun toStringJson(): String? {
        val ow = ObjectMapper().writer().withDefaultPrettyPrinter()
        var json: String? = null
        try {
            json = ow.writeValueAsString(this)
        } catch (e: JsonProcessingException) {
            e.printStackTrace()
        }

        return json
    }
}
