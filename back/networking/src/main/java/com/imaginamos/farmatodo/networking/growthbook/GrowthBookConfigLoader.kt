package com.imaginamos.farmatodo.networking.growthbook

import com.google.gson.Gson
import com.imaginamos.farmatodo.model.Address.ConfigGoogleGeoReferencingCoRes
import com.imaginamos.farmatodo.model.algolia.*
import com.imaginamos.farmatodo.model.algolia.tips.DefaultTipsByCity
import com.imaginamos.farmatodo.model.algolia.tips.ItemTip
import com.imaginamos.farmatodo.model.algolia.tips.Tip
import com.imaginamos.farmatodo.model.algolia.tips.TipConfig
import com.imaginamos.farmatodo.model.item.TtlCacheAlgoliaRecommendRes
import com.imaginamos.farmatodo.model.marketPlace.MarketPlaceResGrowthBook
import com.imaginamos.farmatodo.model.order.DeliveryTimeProvider
import com.imaginamos.farmatodo.model.order.DeliveryTimesConfig
import com.imaginamos.farmatodo.model.util.Constants
import com.imaginamos.farmatodo.model.util.URLConnections
import java.util.*
import java.util.logging.Logger

class GrowthBookConfigLoader {

    companion object {
        private val logger: Logger = Logger.getLogger(GrowthBookConfigLoader::class.java.name)

        @JvmStatic
        fun getStoresEnabledConfig(customerId: String): OptimalRouteStoresConfig {
            val featureKey = URLConnections.GB_KEY_STORES_OPTIMALROUTE_CO
            val growthBookService = GrowthBookService()

            val defaultOptimalRouteStoresConfig = getDefaultOptimalRouteStoresConfig()

            try {
                growthBookService.initialize(customerId)

                val featureValue = growthBookService.getFeatureValue(featureKey, defaultOptimalRouteStoresConfig)
                val gson = Gson()
                val optimalRouteStoresConfig = gson.fromJson(
                    gson.toJson(featureValue),
                    OptimalRouteStoresConfig::class.java
                )

                if (optimalRouteStoresConfig != null) {
                    return optimalRouteStoresConfig
                } else {
                    logger.warning("Failed to parse OptimalRouteStoresConfig for customer $customerId")
                }
            } catch (e: Exception) {
                logger.severe("Error getting OptimalRouteStoresConfig for customer $customerId: ${e.message}")
            }

            logger.warning("Using default OptimalRouteStoresConfig for customer $customerId")
            return defaultOptimalRouteStoresConfig

        }

        private fun getDefaultOptimalRouteStoresConfig(): OptimalRouteStoresConfig {
            return OptimalRouteStoresConfig().apply {
                stores = StoreOptimalRoute().apply {
                    enable = listOf(
                        31L, 43L, 47L, 54L, 74L, 53L, 2L, 80L, 76L, 81L, 3L, 4L, 6L,
                        7L, 9L, 11L, 15L, 51L, 52L, 67L, 85L, 83L, 22L, 23L, 24L,
                        26L, 27L, 28L, 29L, 30L, 37L, 40L, 41L, 44L, 45L, 46L,
                        60L, 62L, 63L, 64L, 69L, 71L, 72L, 73L, 14L, 19L, 20L,
                        94L, 89L, 88L, 87L, 97L, 68L, 93L, 1102L, 1112L, 1118L,
                        1124L, 1123L, 1134L, 1135L, 1119L, 1138L, 1150L, 1155L,
                        1116L, 1156L, 1127L, 90L, 1106L, 92L, 1108L, 1101L, 1103L,
                        1107L, 1113L, 1125L, 42L, 1130L, 1117L, 1141L, 1140L,
                        1126L, 1148L, 1149L, 1160L, 99L, 91L, 1100L, 1109L, 50L,
                        1132L, 1122L, 1110L, 1128L, 1121L, 1131L, 1115L, 1133L,
                        1147L, 96L, 1129L, 1143L, 1139L, 1146L, 1137L, 1151L,
                        1163L, 1158L
                    )
                    disable = listOf(
                        39L, 16L, 98L, 32L, 65L, 1104L, 1105L, 1145L
                    )
                }
            }
        }

        fun getDefaultTipConfig(): TipConfig {
            return TipConfig().apply {
                tip = Tip().apply {
                    values = listOf(0, 1000, 2000, 3000, 4000, 5000)
                    valueMin = 0
                    valueMax = 7000
                    increment = 1000
                    defaultTip = 3000
                    title = "Propina para el domiciliario"
                    description = "El domiciliario recibe el monto total de la propina"
                }

                itemTips = listOf(
                    ItemTip().apply { itemId = 236650616; value = 0f },
                    ItemTip().apply { itemId = 236700011; value = 500f },
                    ItemTip().apply { itemId = 236700046; value = 2000f },
                    ItemTip().apply { itemId = 236700020; value = 1000f },
                    ItemTip().apply { itemId = 236700038; value = 1500f },
                    ItemTip().apply { itemId = 236700054; value = 2500f },
                    ItemTip().apply { itemId = 236700062; value = 3000f },
                    ItemTip().apply { itemId = 236700071; value = 3500f },
                    ItemTip().apply { itemId = 236700089; value = 4000f },
                    ItemTip().apply { itemId = 236700097; value = 4500f },
                    ItemTip().apply { itemId = 236700100; value = 5000f },
                    ItemTip().apply { itemId = 236700118; value = 5500f },
                    ItemTip().apply { itemId = 236700126; value = 6000f },
                    ItemTip().apply { itemId = 236700134; value = 6500f },
                    ItemTip().apply { itemId = 236700142; value = 7000f }
                )

                defaultTipsByCity = listOf(
                    DefaultTipsByCity().apply { cityId = "BOG"; defaultTip = 3000 },
                    DefaultTipsByCity().apply { cityId = "BAR"; defaultTip = 3000 },
                    DefaultTipsByCity().apply { cityId = "ALI"; defaultTip = 3000 },
                    DefaultTipsByCity().apply { cityId = "BUC"; defaultTip = 3000 },
                    DefaultTipsByCity().apply { cityId = "CHI"; defaultTip = 3000 },
                    DefaultTipsByCity().apply { cityId = "CTG"; defaultTip = 3000 },
                    DefaultTipsByCity().apply { cityId = "MED"; defaultTip = 3000 },
                    DefaultTipsByCity().apply { cityId = "SMR"; defaultTip = 3000 },
                    DefaultTipsByCity().apply { cityId = "SOA"; defaultTip = 3000 },
                    DefaultTipsByCity().apply { cityId = "SOL"; defaultTip = 3000 },
                    DefaultTipsByCity().apply { cityId = "VUP"; defaultTip = 3000 },
                    DefaultTipsByCity().apply { cityId = "VVC"; defaultTip = 3000 },
                    DefaultTipsByCity().apply { cityId = "LAC"; defaultTip = 3000 },
                    DefaultTipsByCity().apply { cityId = "CUT"; defaultTip = 3000 },
                    DefaultTipsByCity().apply { cityId = "CTA"; defaultTip = 3000 },
                    DefaultTipsByCity().apply { cityId = "TER"; defaultTip = 3000 },
                    DefaultTipsByCity().apply { cityId = "IDA"; defaultTip = 3000 },
                    DefaultTipsByCity().apply { cityId = "IRO"; defaultTip = 3000 },
                    DefaultTipsByCity().apply { cityId = "PIE"; defaultTip = 3000 },
                    DefaultTipsByCity().apply { cityId = "ENV"; defaultTip = 3000 },
                    DefaultTipsByCity().apply { cityId = "TAG"; defaultTip = 3000 },
                    DefaultTipsByCity().apply { cityId = "ION"; defaultTip = 3000 },
                    DefaultTipsByCity().apply { cityId = "JIC"; defaultTip = 3000 },
                    DefaultTipsByCity().apply { cityId = "PAQ"; defaultTip = 3000 },
                    DefaultTipsByCity().apply { cityId = "AMU"; defaultTip = 3000 },
                    DefaultTipsByCity().apply { cityId = "VRO"; defaultTip = 3000 },
                    DefaultTipsByCity().apply { cityId = "GAI"; defaultTip = 3000 },
                    DefaultTipsByCity().apply { cityId = "ARM"; defaultTip = 3000 },
                    DefaultTipsByCity().apply { cityId = "NIZ"; defaultTip = 3000 },
                    DefaultTipsByCity().apply { cityId = "VLL"; defaultTip = 3000 },
                    DefaultTipsByCity().apply { cityId = "ELL"; defaultTip = 3000 },
                    DefaultTipsByCity().apply { cityId = "LAR"; defaultTip = 3000 },
                    DefaultTipsByCity().apply { cityId = "CIR"; defaultTip = 3000 },
                    DefaultTipsByCity().apply { cityId = "RET"; defaultTip = 3000 }
                )
            }
        }

        /**
         * Get Config for Optimal Route from growthbook
         * @param customerId customer id
         * @return getOptimalRouteConfig from growthbook
         */
        @JvmStatic
        fun getDistancesOptimalRoute(customerId: String): OptimalRouteDistance {
            val featureKey = URLConnections.ALGOLIA_OPTIMAL_ROUTE_DISTANCES
            val growthBookService = GrowthBookService()

            val defaultOptimalRouteDistance = getDefaultOptimalRouteDistance()

            try {
                growthBookService.initialize(customerId)

                val featureValue = growthBookService.getFeatureValue(featureKey, defaultOptimalRouteDistance)
                val gson = Gson()
                val optimalRouteDistance = gson.fromJson(
                    gson.toJson(featureValue),
                    OptimalRouteDistance::class.java
                )

                if (optimalRouteDistance != null) {
                    return optimalRouteDistance
                } else {
                    logger.warning("Failed to parse OptimalRouteDistance for customer $customerId")
                }
            } catch (e: Exception) {
                logger.severe("Error getting OptimalRouteDistance for customer $customerId: ${e.message}")
            }

            logger.warning("Using default OptimalRouteDistance for customer $customerId")
            return defaultOptimalRouteDistance
        }

        private fun getDefaultOptimalRouteDistance(): OptimalRouteDistance {
            return OptimalRouteDistance().apply {
                firstDistance = OptimalRouteConstants.DEFAULT_FIRST_DISTANCE
                secondDistance = OptimalRouteConstants.DEFAULT_SECOND_DISTANCE
                percentagePrice = OptimalRouteConstants.DEFAULT_PERCENTAGE_PRICE
                distancePopUp = OptimalRouteConstants.DEFAULT_DISTANCE_POPUP
                hourToNextSchedule = OptimalRouteConstants.DEFAULT_HOUR_TO_NEXT_SCHEDULE
                active = OptimalRouteConstants.DEFAULT_ACTIVE
                ruteoApi = OptimalRouteConstants.DEFAULT_RUTEO_API
                itemsToIgnore = OptimalRouteConstants.DEFAULT_ITEMS_TO_IGNORE
                newFlowOptimalRouteDistance = OptimalRouteConstants.DEFAULT_NEW_FLOW_OPTIMAL_ROUTE_DISTANCE
                cacheable = false
            }
        }


        @JvmStatic
        fun configGoogleGeoReferencingCo(customerId: String): ConfigGoogleGeoReferencingCoRes {
            val featureKey = URLConnections.GB_CONFIG_GOOGLE_GEO_REFERENCING_CO
            val growthBookService = GrowthBookService()
            val defaultConfig = configGoogleGeoReferencingCoResDefault()
            return try {
                // Inicializar GrowthBook y obtener el valor de la característica
                growthBookService.initialize(customerId)
                val featureValue = growthBookService.getFeatureValue(featureKey, defaultConfig)

                val gson = Gson()
                val referencingCoRes = gson.fromJson(
                        gson.toJson(featureValue),
                        ConfigGoogleGeoReferencingCoRes::class.java
                )
                // Verificar si el valor es un entero
                if (referencingCoRes != null) {
                   return referencingCoRes
                } else {
                    logger.warning("Failed to parse ConfigGoogleGeoReferencingCoRes for customer $customerId")
                    return defaultConfig
                }
            } catch (e: Exception) {
                // Log de excepción
                logger.severe("Error retrieving feature $featureKey: ${e.message}. Using default value: $defaultConfig")
                return defaultConfig
            }
        }

        private fun configGoogleGeoReferencingCoResDefault(): ConfigGoogleGeoReferencingCoRes {
            return ConfigGoogleGeoReferencingCoRes().apply {
                active = false
                maxRetries = 3
            }
        }

        @JvmStatic
        fun getTipsConfig(customerId: String): Optional<TipConfig> {
            val featureKey = URLConnections.GB_KEY_TIPS_CONFIG_CO
            val growthBookService = GrowthBookService()

            val defaultTipsConfig = getDefaultTipConfig()

            try {
                growthBookService.initialize(customerId)

                val featureValue = growthBookService.getFeatureValue(featureKey, defaultTipsConfig)
                val gson = Gson()
                val tipsConfig = gson.fromJson(
                    gson.toJson(featureValue),
                    TipConfig::class.java
                )

                if (tipsConfig != null) {
                    return Optional.of(tipsConfig)
                } else {
                    logger.warning("Failed to parse TipsConfig for customer $customerId")
                }
            } catch (e: Exception) {
                logger.severe("Error getting TipsConfig for customer $customerId: ${e.message}")
            }

            logger.warning("Using default TipsConfig for customer $customerId")
            return Optional.of(defaultTipsConfig)

        }


        // o simplemente inicializar la propiedad directamente
        private val DEFAULT_VALUE_FEATURE_MARKETPLACE: HashMap<String, Boolean> = hashMapOf(
                "WEB" to true,
                "IOS" to false,
                "ANDROID" to false,
                "RESPONSIVE" to true
        )

        @JvmStatic
        fun isMarketplaceActiveBySource(customerId: String, source: String, buildCodeNumberApp: Int?): Boolean {
            try {

                val growthBookService = GrowthBookService()
                growthBookService.initializeMarketplaceActiveBySource(customerId, buildCodeNumberApp)
                val isActiveMarketplace: Any = growthBookService.getFeatureValue(URLConnections.FEATURE_MARKETPLACE_ACTIVE, DEFAULT_VALUE_FEATURE_MARKETPLACE)
                val gson = Gson()
                val marketPlaceResGrowthBook = gson.fromJson(
                        gson.toJson(isActiveMarketplace),
                        MarketPlaceResGrowthBook::class.java
                )

                if (marketPlaceResGrowthBook != null) {
                    return marketPlaceResGrowthBook[source] ?: false
                } else {
                    logger.warning("Failed to parse ConfigGoogleGeoReferencingCoRes for customer $customerId")
                    return DEFAULT_VALUE_FEATURE_MARKETPLACE[Constants.SOURCE_ANDROID]!!
                }

            } catch (e: java.lang.Exception) {
                logger.severe("Error loading feature flag is-marketplace-active from GrowthBook for customer $customerId. Returning default configuration: ${e.message}")
                return DEFAULT_VALUE_FEATURE_MARKETPLACE[Constants.SOURCE_ANDROID]!!
            }
        }


        @JvmStatic
        fun ttlCacheAlgoliaRecommend(customerId: String): TtlCacheAlgoliaRecommendRes {
            val featureKey = URLConnections.CACHE_TIME_SETTINGS
            val growthBookService = GrowthBookService()
            val geoReferencingCoResDefault = getTtlCacheAlgoliaRecommend()

            try {
                growthBookService.initialize(customerId)

                val featureValue = growthBookService.getFeatureValue(featureKey, geoReferencingCoResDefault)
                val gson = Gson()
                val recommendRes = gson.fromJson(
                        gson.toJson(featureValue),
                        TtlCacheAlgoliaRecommendRes::class.java
                )

                if (recommendRes != null) {
                    return recommendRes
                } else {
                    logger.warning("Failed to parse TipsConfig for customer $customerId")
                }
            } catch (e: Exception) {
                logger.severe("Error getting TipsConfig for customer $customerId: ${e.message}")
            }

            logger.warning("Using default TipsConfig for customer $customerId")
            return geoReferencingCoResDefault

        }

        fun getTtlCacheAlgoliaRecommend(): TtlCacheAlgoliaRecommendRes {
            return TtlCacheAlgoliaRecommendRes().apply {
                algoliaRecommendTtlSeconds = 172800
                getItemTtlSeconds = 5400
                algoliaRecommendEnabled = false
            }
        }

        @JvmStatic
        fun getDeliveryTypeTime(customerId: String, city: String): DeliveryTimesConfig {
            val featureKey = URLConnections.GB_KEY_DELIVERY_TYPE_TIME
            val growthBookService = GrowthBookService()

            val defaultDeliveryTypeTime = buildDeliveryTypeTimeDefault()

            try {
                growthBookService.initializeDeliveryTypeTime(customerId,city)

                val featureValue = growthBookService.getFeatureValue(featureKey, defaultDeliveryTypeTime)
                val gson = Gson()
                val deliveryTypeTime = gson.fromJson(
                        gson.toJson(featureValue),
                        DeliveryTimesConfig::class.java
                )

                if (deliveryTypeTime != null) {
                    return deliveryTypeTime
                } else {
                    logger.warning("Fallo al parsear DeliveryTypeTime para el cliente $customerId")
                }
            } catch (e: Exception) {
                logger.severe("Error al conseguir DeliveryTypeTime para el cliente $customerId: ${e.message}")
            }

            logger.warning("Using default DeliveryTypeTime for customer $customerId")
            return defaultDeliveryTypeTime

        }


        fun buildDeliveryTypeTimeDefault(): DeliveryTimesConfig {
            return DeliveryTimesConfig().apply {
                deliveryTimes = listOf(
                        DeliveryTimeProvider("envialoya","Pedido a Venezuela 8 - 12 días", "de 8 a 12 días en promedio el último mes", "8-12 dias"),
                        DeliveryTimeProvider("national","Pedido Nacional 5 días", "de 5 días en promedio el último mes", "5 días"),
                        DeliveryTimeProvider("express", "Pedido Express 35 mins","En 35 mins promedio", "35 mins"),
                        DeliveryTimeProvider("provider","Pedido Marketplace", "De 4 a 5 dias hábiles", "4-5 dias hábiles")
                )
            }
        }

    }

}