package com.imaginamos.farmatodo.model.order

import java.util.*

class ScanAndGoResponse {
    var responseCode: String? = ""
    var data: List<CustomerInfo>? = ArrayList()

}
class CustomerInfo {
    var customerId: Long? = 0
    var blocked: Boolean? = false
    var firstName: String? = ""
    var lastName: String? = ""
    var gender: String? = ""
    var documentNumber: Long? = 0
    var documentType: String? = ""
    var email: String? = ""
    var phone: String? = ""
    var deliveryOrder: DeliveryOrderInfo? = DeliveryOrderInfo()

}

class DeliveryOrderInfo {
    var subTotalPrice: Double = 0.0
    var offerPrice:  Double = 0.0
    var createDate: Date? = Date()
    var totalPrice:  Double = 0.0
    var items: List<ItemInfo> = ArrayList()
    class ItemInfo {
        var id: Long = 0
        var firstDescription: String = ""
        var secondDescription: String = ""
        var mediaImageURL: String = ""
        var requirePrescription: Boolean = false
        var price:  Double = 0.0
        var barcode: String = ""
        var quantitySold: Long = 0
    }
}






