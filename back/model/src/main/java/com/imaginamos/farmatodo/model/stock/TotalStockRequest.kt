package com.imaginamos.farmatodo.model.stock

data class TotalStockRequest(
    val itemId: Long,
    val storeIds: List<Int>
)