package com.imaginamos.farmatodo.model.stock

data class TotalStockResponse(
    val code: String,
    val message: String,
    val data: StockData
)
data class StockData(
    val totalStock: Int
)