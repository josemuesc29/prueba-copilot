package com.imaginamos.farmatodo.model.customer

class MergeAnonymousCartReq {
    var idCustomerWebSafeAnonymous: String = ""
    var idCustomerWebSafe: String = ""
    var token: String = ""
    var tokenIdWebSafe: String = ""
    var idStoreGroup: Int = 0
    var deliveryType: String = ""

    fun isValid(): Boolean{
        return !idCustomerWebSafe.isEmpty()
                && !idCustomerWebSafeAnonymous.isEmpty()
                && !token.isEmpty()
                && !tokenIdWebSafe.isEmpty()
                && !deliveryType.isEmpty()
                && idStoreGroup > 0

    }

    override fun toString(): String {
        return "MergeAnonymousCartReq(idCustomerWebSafeAnonymous='$idCustomerWebSafeAnonymous', idCustomerWebSafe='$idCustomerWebSafe')"
    }

}


class MergeAnonymousCartRes {
    var confirmation: Boolean = false
    var message: String = "Error"
}