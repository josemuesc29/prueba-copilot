package com.imaginamos.farmatodo.model.order

class CreateOrderResponseBackend3{
    var code: String? = null
    var message: String? = null
    var data: Data? = null

    inner class Data {
        var id: Int? = null
        var createDate: Long? = null
        var address: String? = null
        var updateShopping: Boolean? = null
        var changePaymentCreditCard: Boolean? = null
        var qrCode: String? = null
        var typePersonPSE: String? = null
        var ipAddress: String? = null
        var financialInstitutions: Long? = null
        var transactionDetails: TransactionDetails? = null


        fun dataIsValid(): Boolean {
            return id != null && createDate !=null && address!=null && updateShopping !=null
        }

        override fun toString(): String {
            return "Data(id=$id, createDate=$createDate, address=$address, updateShopping=$updateShopping, changePaymentCreditCard=$changePaymentCreditCard, qrCode=$qrCode, transactionDetails=$transactionDetails)"
        }



    }

    override fun toString(): String {
        return "CreateOrderResponseBackend3(code=$code, message=$message, data=$data)"
    }


}