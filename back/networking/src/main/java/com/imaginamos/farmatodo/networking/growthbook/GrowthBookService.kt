package com.imaginamos.farmatodo.networking.growthbook

import com.google.appengine.repackaged.com.google.gson.Gson
import com.imaginamos.farmatodo.model.util.Constants
import com.imaginamos.farmatodo.networking.cache.SecretsCache
import growthbook.sdk.java.GBContext
import growthbook.sdk.java.GrowthBook
import org.json.JSONObject
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.logging.Logger

class GrowthBookService {
    private lateinit var growthBook: GrowthBook
    private val logger: Logger = Logger.getLogger(GrowthBookService::class.java.name)

    companion object {
        const val FEATURES_JSON_KEY = "features"
        const val CUSTOMER_ID_KEY = "id"
        const val CACHE_EXPIRATION_TIME = 5 * 60 * 1000
        const val BUILD_CODE_NUMBER_APP = "build-code-number-app";
        const val CITY_DELIVERY_TYPE_TIME = "city";

        @Volatile
        private var cachedFeatures: String? = null
        private var lastFetchTime: Long = 0
    }

    fun initializeDeliveryTypeTime(customerId: String, city: String) {
        val mapObjects: MutableMap<String, Any> = java.util.HashMap()
        mapObjects[CUSTOMER_ID_KEY] = customerId
        mapObjects[CITY_DELIVERY_TYPE_TIME] = city
        this.initializeFromMapObjects(mapObjects)
    }

    fun initializeMarketplaceActiveBySource(customerId: String, appBuildCode: Int?) {
        var appBuildCode = appBuildCode
        if (appBuildCode == null) appBuildCode = 0

        val mapObjects: MutableMap<String, Any> = java.util.HashMap()
        mapObjects[CUSTOMER_ID_KEY] = customerId
        mapObjects[BUILD_CODE_NUMBER_APP] = appBuildCode

        this.initializeFromMapObjects(mapObjects)
    }

    fun initialize(customerId: String?) {
        try {
            val featuresJson = getCachedOrFetchFeatures()

            val featuresObject = JSONObject(featuresJson).getJSONObject(FEATURES_JSON_KEY)

            val contextBuilder = GBContext.builder()
                .featuresJson(featuresObject.toString())
                .enabled(true)

            customerId?.let {
                val userAttributes = JSONObject().put(CUSTOMER_ID_KEY, customerId).toString()
                contextBuilder.attributesJson(userAttributes)
            }

            growthBook = GrowthBook(contextBuilder.build())
        } catch (e: Exception) {
            logger.severe("Error initializing GrowthBook: ${e.message}\n${e.stackTraceToString()}")
            throw e
        }
    }


    private fun initializeFromMapObjects(mapObjects: Map<String, Any>) {
        try {
            val featuresJson = getCachedOrFetchFeatures()
            val featuresObject = JSONObject(featuresJson).getJSONObject(FEATURES_JSON_KEY)
            val gbContextBuilder = GBContext.builder()
                    .featuresJson(featuresObject.toString())
                    .enabled(true)

            val attributesJsonObjectString = Gson().toJson(mapObjects)
            gbContextBuilder.attributesJson(attributesJsonObjectString)

            growthBook = GrowthBook(gbContextBuilder.build())

        } catch (e: Exception) {
            logger.severe("Error initializing GrowthBook: ${e.message}")
            throw RuntimeException(e)
        }
    }

    @Synchronized
    private fun getCachedOrFetchFeatures(): String {
        val currentTime = System.currentTimeMillis()

        if (cachedFeatures == null || currentTime - lastFetchTime > CACHE_EXPIRATION_TIME) {
            cachedFeatures = fetchFeatureFlags()
            lastFetchTime = currentTime
            logger.info("Features fetched from API")
        } else {
            logger.info("Using cached features")
        }

        return cachedFeatures!!
    }

    /**
     * Warn: Only for features type boolean
     */
    fun isFeatureEnabled(featureKey: String): Boolean {
        return try {
            growthBook.isOn(featureKey)
        } catch (e: Exception) {
            logger.warning("Error checking if feature $featureKey is enabled: ${e.message}")
            false
        }
    }

    fun getFeatureValue(featureKey: String, defaultValue: Any): Any {
        return try {
            growthBook.getFeatureValue(featureKey, defaultValue)
        } catch (e: Exception) {
            logger.warning("Error getting feature value for $featureKey: ${e.message}")
            defaultValue
        }
    }

    private fun fetchFeatureFlags(): String {
        val apikey = SecretsCache
            .getInstance(Constants.GCP_PROJECT_ID)
            .getSecret(Constants.GCP_GROWTHBOOK_SECRET_ID)
        val featuresEndpoint = URI.create("${Constants.GROWTHBOOK_URI}/$apikey")
        val request = HttpRequest.newBuilder().uri(featuresEndpoint).GET().build()
        return HttpClient.newBuilder().build().send(request, HttpResponse.BodyHandlers.ofString()).body()
    }
}