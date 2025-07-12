package com.imaginamos.farmatodo.model.order

class DeliveryTimesConfig {
    var deliveryTimes: List<DeliveryTimeProvider> = emptyList()
}

data class DeliveryTimeProvider(
        val type: String,
        val title: String,
        val description: String,
        val estimatedTime: String
)