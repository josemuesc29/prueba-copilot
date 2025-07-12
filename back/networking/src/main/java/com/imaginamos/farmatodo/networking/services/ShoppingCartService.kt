package com.imaginamos.farmatodo.networking.services

import com.google.api.client.http.HttpStatusCodes
import com.imaginamos.farmatodo.model.stock.TotalStockRequest
import com.imaginamos.farmatodo.model.stock.TotalStockResponse
import com.imaginamos.farmatodo.networking.api.ShoppingCartApi
import com.imaginamos.farmatodo.networking.base.ApiBuilder
import java.util.*
import java.util.logging.Logger

class ShoppingCartService private constructor() {

    private val shoppingCartApi: ShoppingCartApi = ApiBuilder.get()
        .createShoppingCartService(ShoppingCartApi::class.java)

    companion object {
        private val LOG: Logger = Logger.getLogger(ShoppingCartService::class.java.name)

        @Volatile
        private var instance: ShoppingCartService? = null

        fun getInstance(): ShoppingCartService {
            return instance ?: synchronized(this) {
                instance ?: ShoppingCartService().also { instance = it }
            }
        }
    }


    fun getTotalStock(totalStockRequest: TotalStockRequest) : Optional<TotalStockResponse> {
        return try {
            if (totalStockRequest.itemId == 0L) {
                LOG.severe("Error getting total stock: itemId is required")
                return Optional.empty()
            }

            if (totalStockRequest.storeIds.isEmpty()) {
                LOG.severe("Error getting total stock: storeIds is required")
                return Optional.empty()
            }

            val call = shoppingCartApi.getTotalStock(totalStockRequest)
            val response = call.execute()

            if (response.isSuccessful) {
                response.body()?.let {
                    return Optional.of(it)
                }
            } else {
                response.errorBody()?.string()?.let { error ->
                    LOG.severe("Error getting total stock: $error - url: ${call.request().url()}")
                }
            }

            Optional.empty()
        } catch (e: Exception) {
            LOG.severe("Error getting total stock: ${e.message}")
            Optional.empty()
        }
    }

}