package com.imaginamos.farmatodo.networking.services

import com.imaginamos.farmatodo.model.algolia.AlgoliaItem
import com.imaginamos.farmatodo.model.algolia.ItemAlgolia
import com.imaginamos.farmatodo.networking.api.ApiAlgoliaProxy
import com.imaginamos.farmatodo.networking.base.ApiBuilder
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.Optional
import java.util.logging.Logger

class ApiAlgoliaProxyService private constructor(){

    private val apiAlgoliaProxy: ApiAlgoliaProxy = ApiBuilder
        .get()
        .createAlgoliaProxyService(ApiAlgoliaProxy::class.java)

    companion object {
        private val LOG: Logger = Logger.getLogger(ApiAlgoliaProxyService::class.java.name)
        @Volatile
        private var instance: ApiAlgoliaProxyService? = null

        fun getInstance(): ApiAlgoliaProxyService {
            return instance ?: synchronized(this) {
                instance ?: ApiAlgoliaProxyService().also { instance = it }
            }
        }
    }


    /**
     * Get item by objectID
     * @param productIndex
     * @param objectID
     * @param nearbyStores
     * @return Optional<ItemAlgolia>
     */
    fun getItemByObjectID(productIndex: String, objectID: String, nearbyStores: String): Optional<ItemAlgolia> {
        // LOG.info("Starting getItemByObjectID - productIndex: $productIndex, objectID: $objectID, nearbyStores: $nearbyStores")

        return try {
            if (productIndex.isEmpty()) {
                LOG.severe("Error: productIndex is empty")
                return Optional.empty()
            }

            if (objectID.isEmpty()) {
                LOG.severe("Error: objectID is empty")
                return Optional.empty()
            }

            val call = apiAlgoliaProxy.getItemByObjectID(productIndex, objectID, nearbyStores)
            val response = call.execute()

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Optional.of(body)
                } else {
                    Optional.empty()
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                LOG.severe("Error response: Code ${response.code()}, Error: $errorBody, URL: ${call.request().url()}")
                Optional.empty()
            }

        } catch (e: Exception) {
            when (e) {
                is SocketTimeoutException -> LOG.severe("Network timeout: ${e.message}")
                is UnknownHostException -> LOG.severe("Unknown host: ${e.message}. Check DNS or network connectivity.")
                is HttpException -> LOG.severe("HTTP error ${e.code()}: ${e.message()}")
                else -> LOG.severe("Unexpected error in getItemByObjectID: $e")
            }
            Optional.empty()
        }
    }
}