package com.imaginamos.farmatodo.model.algolia

class ConfigurationByDeliveryType {
    var configuration: List<CreateOrderConfig>? = null
}

class CreateOrderConfig{
    var deliveryType: String = ""
    var isCreateOrderInCoreWhenFailed: Boolean = false
    var isCreateOrderInCore: Boolean = false

}
