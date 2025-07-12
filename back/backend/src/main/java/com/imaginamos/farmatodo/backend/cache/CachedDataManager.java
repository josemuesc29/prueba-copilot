package com.imaginamos.farmatodo.backend.cache;

import com.algolia.search.exceptions.AlgoliaException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.imaginamos.farmatodo.backend.cache.datasources.DatastoreAPI;
import com.imaginamos.farmatodo.backend.product.ProductConstant;
import com.imaginamos.farmatodo.model.algolia.OriginProperties;
import com.imaginamos.farmatodo.model.algolia.RateLimitConfig;
import com.imaginamos.farmatodo.model.algolia.StoresAlgolia;
import com.imaginamos.farmatodo.model.copyright.Copyright;
import com.imaginamos.farmatodo.model.dto.DynamicResponse;
import com.imaginamos.farmatodo.model.home.HomeConfigAlgolia;
import com.imaginamos.farmatodo.model.location.City;
import com.imaginamos.farmatodo.model.util.CollectionResponseModel;
import com.imaginamos.farmatodo.networking.algolia.APIAlgolia;
import io.swagger.models.auth.In;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.*;
import java.util.logging.Logger;

public class CachedDataManager {

    public static final String ORIGIN_PROPERTIES = "ORIGIN_PROPERTIES";

    private static CacheConfig cache = new CacheConfig();

    private static final Logger LOG = Logger.getLogger(CachedDataManager.class.getName());

    private static final String ALL_CITIES = "ALL_CITIES";

    private static final String ALL_STORES = "ALL_STORES";

    private static final String ALL_COPY_RIGHT = "COPY_RIGHT";

    private static final String CATEGORIES_SUBCATEGORIES = "CATEGORIES_SUBCATEGORIES";

    private static final String ALGOLIA_CONFIG_HOME ="ALGOLIA_CONFIG_HOME";

    private static final String ALGOLIA_DEFAULT_STORE ="ALGOLIA_DEFAULT_STORE";

    private static final String ALGOLIA_STORE_CITY ="ALGOLIA_STORE_CITY";

    // Memoria en bytes, 20 MB primer umbral puesto
    private static final long MINIMUM_MEMORY_CACHE = 15000000;
    private static final long MAX_REDIS_CACHE_SECONDS = 86400;

    private static final long MAX_REDIS_RETRY_SECONDS = 300;

    private static final long MAX_RETRY_LOGIN = 10;

    // Tiempo de expiración para getItem en cache
    private static final long GET_ITEM_CACHE_EXPIRATION_SECONDS = 5400; // 90 mins


    public static List<City> getCities() {
        //LOG.info("method CachedDataManager.getCities called...");

        if (cache.citiesCache().containsKey(ALL_CITIES)) {
//            LOG.info("Las ciudades estan en cache. Se usaran.");
            return cache.citiesCache().get(ALL_CITIES);
        }

        //LOG.info("Las ciudades NO estan en cache. Esnecesario consultar la fuente de datos...");
        final List<City> cities = DatastoreAPI.findAllSortedCities();
        cache.citiesCache().put(ALL_CITIES, cities);

        return cities;
    }

    public static StoresAlgolia getStoresAlgoliaCached() throws AlgoliaException {
        //LOG.info("method CachedDataManager.StoresAlgolia called...");

        if (cache.storesAlgoliaCache().containsKey(ALL_STORES)){
//            LOG.info("Las stores estan en cache. Se usaran.");
            return cache.storesAlgoliaCache().get(ALL_STORES);
        }

//        LOG.info("Las STORES NO estan en cache. Esnecesario consultar la fuente de datos...");

        StoresAlgolia storesAlgolia = APIAlgolia.getStoresAlgolia();
        cache.storesAlgoliaCache().put(ALL_STORES,storesAlgolia);

        return storesAlgolia;
    }

    public static List<Copyright> getCopyRighCached(final String token, final String tokenIdWebSafe, final String deliveryType, final Boolean provider) {
//        LOG.info("method CachedDataManager. called..." + deliveryType +" --- "+ provider);

        if (cache.copyRightCache().containsKey(ALL_COPY_RIGHT)) {
//            LOG.info("copyRightCache estan en cache. Se usaran.");
            return cache.copyRightCache().get(ALL_COPY_RIGHT);
        }

//        LOG.info("Las copyRightCache NO estan en cache. Esnecesario consultar la fuente de datos...");
        final List<Copyright> copyrights = DatastoreAPI.findCopyRight(token, tokenIdWebSafe, deliveryType, provider);
        cache.copyRightCache().put(ALL_COPY_RIGHT, copyrights);

        return copyrights;
    }

    public static CollectionResponseModel categoriesAndSubcategories() {
        if (cache.categoriesAndSubcategories().containsKey(CATEGORIES_SUBCATEGORIES)) {
            return cache.categoriesAndSubcategories().get(CATEGORIES_SUBCATEGORIES);
        }

        final CollectionResponseModel categoriesAndSubcategories = DatastoreAPI.getCategoriesAndSubcategories();
        cache.categoriesAndSubcategories().put(CATEGORIES_SUBCATEGORIES, categoriesAndSubcategories);

        return categoriesAndSubcategories;
    }

    public static HomeConfigAlgolia algoliaHomeConfig() {
        //LOG.info("method CachedDataManager.algoliaHomeConfig called..." );

        if (cache.homeConfigAlgolia().containsKey(ALGOLIA_CONFIG_HOME)) {
            //LOG.info("AlgoliaHomeConfig estan en cache. Se usaran.");
            return cache.homeConfigAlgolia().get(ALGOLIA_CONFIG_HOME);
        }

//        LOG.info("AlgoliaHomeConfig NO estan en cache. Esnecesario consultar la fuente de datos...");
        HomeConfigAlgolia homeConfigAlgolia = APIAlgolia.getHomeV2Config();
        cache.homeConfigAlgolia().put(ALGOLIA_CONFIG_HOME, homeConfigAlgolia);

        return homeConfigAlgolia;
    }


    public static Integer defaultStoreAlgolia(int idStoreGroup) throws AlgoliaException {
        //LOG.info("method CachedDataManager.defaultStore called..." );

        if (cache.defaultStoreAlgolia().containsKey(ALGOLIA_DEFAULT_STORE + idStoreGroup)) {
            //LOG.info("AlgoliaDefaultStore estan en cache. Se usaran.");
            return cache.defaultStoreAlgolia().get(ALGOLIA_DEFAULT_STORE + idStoreGroup);
        }

//        LOG.info("AlgoliaDefaultStore NO estan en cache. Esnecesario consultar la fuente de datos...");
        Integer defaultStore = APIAlgolia.getDefaultStoreIdByStoreId(idStoreGroup);
        cache.defaultStoreAlgolia().put(ALGOLIA_DEFAULT_STORE + idStoreGroup, defaultStore);

        return defaultStore;
    }

    public static String storeCityAlgolia(int idStoreGroup) throws AlgoliaException {
        //LOG.info("method CachedDataManager.storeCityAlgolia called..." );

        if (cache.storeCityAlgolia().containsKey(ALGOLIA_STORE_CITY + idStoreGroup)) {
            //LOG.info("AlgoliaStoreCity estan en cache. Se usaran.");
            return cache.storeCityAlgolia().get(ALGOLIA_STORE_CITY + idStoreGroup);
        }

//        LOG.info("AlgoliaStoreCity NO estan en cache. Es necesario consultar la fuente de datos...");
        String storeCity = APIAlgolia.getStoreCityByStoreId(idStoreGroup);
        cache.storeCityAlgolia().put(ALGOLIA_STORE_CITY + idStoreGroup, storeCity);

        return storeCity;
    }

    public static DynamicResponse algoliaGetItemFromCache(String key)  {
        Gson gson = new Gson();
        try (Jedis jedis = cache.getJedisClient()) {
            // Debug: Imprimir el índice actual de Redis
            int currentDb = jedis.getDB();
            // LOG.info("Redis DB Index usado para cache: " + currentDb + " | Key: " + key);

            String json = jedis.get(key);
            if(json != null) {
                // LOG.info("Item en Cache -> " + key + " (DB: " + currentDb + ")");
                return gson.fromJson(json, DynamicResponse.class);
            }
        } catch (Exception e) {
            LOG.warning("No se pudo obtener item del cache, problema con redis: " + e.getMessage());
            return null;
        }
        return null;
    }


    public static void algoliaSaveItemInCache(String key, DynamicResponse value, long time)  {
        Gson gson = new Gson();
        try (Jedis jedis = cache.getJedisClient()) {
            jedis.setex(key,time, gson.toJson(value));
            //LOG.info("Se agrega al cache item -> " + key );
        } catch (Exception e) {
            LOG.warning("No se pudo agregar item al cache, problema con redis");
        }
    }


    public static String algoliaGetItemListMostSales(String key)  {
        try (Jedis jedis = cache.getJedisClient()) {
//            return null; // TODO comentar
            jedis.select(10);
            return jedis.get(key);
            //TODO descomentar
        } catch (Exception e) {
            LOG.warning("No se pudo obtener item del cache, problema con redis");
            return null;
        }
    }

    public static void algoliaSetItemListMostSales(String key, String value)  {
        try (Jedis jedis = cache.getJedisClient()) {
            jedis.select(10);
            jedis.setex(key,86400, value);
            //TODO descomentar
//            LOG.info("Se agrega al cache item-list-sales -> " + key );
        } catch (Exception e) {
            LOG.warning("No se pudo agregar item al cache, problema con redis");
        }
    }

    public static String getSuggestsCache(String key)  {
        if(Objects.isNull(key)){
            return null;
        }

        try (Jedis jedis = cache.getJedisClient()) {
            jedis.select(ProductConstant.CACHE_DATABASE);
            return jedis.get(key);
        } catch (Exception e) {
            LOG.info("No se pudo obtener Suggests del cache, problema con redis");
            return null;
        }
    }

    public static void setSuggestsCache(String key, String value){
        if(Objects.isNull(key) || Objects.isNull(value)){
            return;
        }

        try (Jedis jedis = cache.getJedisClient()) {
            jedis.select(ProductConstant.CACHE_DATABASE);
            jedis.setex(key,ProductConstant.SUGGESTS_EXPIRATION_TIME, value);
        } catch (Exception e) {
            LOG.info("No se pudo agregar Suggests al cache, problema con redis");
        }
    }

    /**
     * @author JhonChaparro
     * save userPrime
     * @param key
     * @return
     */
    public static  Optional<String> getClientPrime(String key)  {
        try (Jedis jedis = cache.getJedisClient()) {
            jedis.select(8);
            String json = jedis.get(key);
            if (json == null)
                return Optional.empty();
//            LOG.info("GET_CLIENT_PRIME -> "+ key);
            return Optional.of(json);
        } catch (Exception e) {
            LOG.warning("No se pudo obtener item del cache, problema con redis");
            return Optional.empty();
        }
    }

    /**
     * get user prime
     * @author JhonChaparro
     * @param key
     * @param json
     */
    public static void saveClientPrime(String key, String json)  {
        if (key == null || key.isEmpty())
            return;
        if (json == null || json.isEmpty())
            return;
        try (Jedis jedis = cache.getJedisClient()) {
            jedis.select(8);
            jedis.setex(key,MAX_REDIS_CACHE_SECONDS, json);
//            LOG.info("Se agrega al cache getClientPrime -> " + key );
        } catch (Exception e) {
            LOG.warning("No se pudo agregar getClientPrime, problema con redis");
        }
    }



    public static boolean checkTriesLoginAndRegisterInCache(String key) {
        try (Jedis jedis = cache.getJedisClient()) {
            if(jedis == null)
                return false;
            Optional<RateLimitConfig> config = APIAlgolia.getRateLimitingLoginRegister();
            long max_tries = config.isPresent() ? config.get().getMax_tries() : MAX_RETRY_LOGIN;
            long retry_seconds = config.isPresent() ? config.get().getTime_in_seconds() : MAX_REDIS_RETRY_SECONDS;
//            LOG.info("Configuración -> " + max_tries + "-" + retry_seconds);
            jedis.select(13);
            String json = jedis.get(key);
            if (json == null) {
                jedis.setex(key, retry_seconds, String.valueOf(0));
                return false;
            }
            int value = Integer.parseInt(json);
            if (value > max_tries )
                return true;
            value = value + 1;
            jedis.setex(key, retry_seconds, String.valueOf(value));
            return false;
        } catch (Exception e) {
            LOG.warning("Error al verificar intentos de login y registro, problema con redis a la key " +
                    key + " error: " + e.getMessage() != null ? e.getMessage() : " es null.");
            return false;
        }
    }


    /**
     * Método para obtener lista de strings retornando la lista de items relacionados
     *
     * @param key Palabra clave para guardar la lista de items separado por comas.
     * @return Lista de strings que corresponden a los objectIds de los items
     */
    public static List<String> getListOfKeys(String key) {
        try (Jedis jedis = cache.getJedisClient()) {
            jedis.select(6);
            String valueOfKeys = jedis.get(key);
            if (valueOfKeys == null || valueOfKeys.isEmpty()) {
                return new ArrayList<>();
            }
            return Arrays.asList(valueOfKeys.split(","));
        } catch (Exception e) {
            LOG.warning("Problema obteniendo la lista de ids para productos relacionados, problema con redis {}" + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Método para guardar una lista de strings que representan una lista de items relacionados
     *
     * @param key Palabra clave para guardar la lista de items
     * @param stringKeys Lista de strings que representan los items relacionados
     */
    public static void saveListOfKeys(String key, List<String> stringKeys) {
        try (Jedis jedis = cache.getJedisClient()) {
            jedis.select(6);
            jedis.setex(key, MAX_REDIS_CACHE_SECONDS,String.join(",",stringKeys));
        } catch (Exception e) {
            LOG.warning("Problema guardando la lista de ids para productos relacionados, problema con redis {}" + e.getMessage());
        }
    }

    /**
     * Metodo para cachar la lista de origin properties
     * Implementación para ahorrar busquedas en algolia.
     *
     * @return Lista que corresponde a origin properties
     */
    public static List<OriginProperties> getOriginPropertiesCached() {
        try (Jedis jedis = cache.getJedisClient()) {
            jedis.select(6);
            String json = jedis.get(ORIGIN_PROPERTIES);
            if (json != null) {
                return Arrays.asList(new Gson().fromJson(json, OriginProperties[].class));
            }
            return null;
        } catch (Exception e) {
            LOG.warning("Problema obteniendo la lista propiedades de origen cacheadas, problema con redis {}" + e.getMessage());
            return null;
        }
    }

    /**
     * Método para guardar la lista de propiedades de origen en forma de json para optimizar busquedas en algolia
     *
     * @param jsonProperties Json que representa la lista de propiedades
     */
    public static void saveOriginProperties(String jsonProperties) {
        try (Jedis jedis = cache.getJedisClient()) {
            jedis.select(6);
            jedis.setex(ORIGIN_PROPERTIES, MAX_REDIS_CACHE_SECONDS, jsonProperties);
        } catch (Exception e) {
            LOG.warning("Problema guardando lista de propiedades de origen, problema con redis {}" + e.getMessage());
        }
    }

}
