package com.imaginamos.farmatodo.networking.api

import com.imaginamos.farmatodo.model.stock.TotalStockRequest
import com.imaginamos.farmatodo.model.stock.TotalStockResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ShoppingCartApi {

    @Headers("Content-Type: application/json")
    @POST("total-stock")
    fun getTotalStock(@Body totalStockRequest: TotalStockRequest): Call<TotalStockResponse>

}