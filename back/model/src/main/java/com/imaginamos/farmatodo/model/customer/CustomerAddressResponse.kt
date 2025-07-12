package com.imaginamos.farmatodo.model.customer

class CustomerAddressResponse {
    var code: String? = null
    var message: String? = null
    var data: Data? = null

    inner class Data {
        var idAddress: Int = 0
        var idCustomer: Int = 0
        var customerAddressId: Int = 0
        var customerId: Int = 0
        var countryId: String? = null
        var cityId: String? = null
        var closerStoreId: Int = 0
        var closerStoreName: String? = null
        var deliveryType: String? = null
        var nickname: String? = null
        var address: String? = null
        var comments: String? = null
        var geoAddress: String? = null
        var longitude: Float = 0.toFloat()
        var latitude: Float = 0.toFloat()
        var active: Boolean = false
        var creationDate: String? = null
        var tags: String? = null
        var addressWithRestriction: Boolean = false
        var redZoneId: String? = null
    }

    override fun toString(): String {
        return "CustomerAddressResponse(code=$code, message=$message, data=$data)"
    }


}
