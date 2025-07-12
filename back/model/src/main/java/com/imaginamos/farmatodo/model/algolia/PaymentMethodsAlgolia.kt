package com.imaginamos.farmatodo.model.algolia

class PaymentMethodsAlgolia {

    var iosPayments: ios? = null
    var androidPayments: android? = null
    var webPayments: web? = null
    var responsivePayments: responsive? = null
    var defaultPayment: default? = null
    var versions: versions? = null

}

class ios {
    var paymentMethods: List<PaymentMethod>? = null
}

class android {
    var paymentMethods: List<PaymentMethod>? = null
}

class web {
    var paymentMethods: List<PaymentMethod>? = null
}

class responsive {
    var paymentMethods: List<PaymentMethod>? = null
}

class default {
    var paymentMethods: List<PaymentMethod>? = null
}

class versions {
    var androidNationalVersion: List<String>? = null
    var iosNationalVersion: List<String>? = null
}

class PaymentMethod {

    var deliveryType: String = ""
    var isCash: Boolean = false
    var isDataphone: Boolean = false
    var isCreditCard: Boolean = false
    var isPSE: Boolean = false
    override fun toString(): String {
        return "PaymentMethod(deliveryType='$deliveryType', isCash=$isCash, isDataphone=$isDataphone, isCreditCard=$isCreditCard, isPSE=$isPSE)"
    }

}
