package com.imaginamos.farmatodo.backend.cache;

import com.imaginamos.farmatodo.model.algolia.StoreAlgolia;
import com.imaginamos.farmatodo.model.algolia.StoresAlgolia;
import com.imaginamos.farmatodo.model.copyright.Copyright;
import com.imaginamos.farmatodo.model.copyright.CopyrightJson;
import com.imaginamos.farmatodo.model.home.HomeConfigAlgolia;
import com.imaginamos.farmatodo.model.location.City;
import com.imaginamos.farmatodo.model.util.CollectionResponseModel;
import com.imaginamos.farmatodo.model.util.URLConnections;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expirations;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Manejador de caché global.
 * v1.0.0 - 2020-10-16 jhon.puentes@farmatodo.com
 * */
public class CacheConfig {

    private static final Logger LOG = Logger.getLogger(CacheConfig.class.getName());

    private CacheManager cacheManager;
    private JedisPool jedisPool;

    // Objetos a cachear
    private Cache<String, List<City>> citiesCache;
    private Cache<String, StoresAlgolia> storesAlgoliaCache;
    private Cache<String, CopyrightJson> copyRighCache;
    private Cache<String, CollectionResponseModel> categoriesAndSubcategories;
    private Cache<String, HomeConfigAlgolia> homeConfigAlgolia;
    private Cache<String, String> storeCityAlgolia;
    private Cache<String, Integer> defaultStoreAlgolia;


    // configs
    private final static String CITIES_CACHE_ALIAS = "citiesCache";
    private final static String STORES_ALGOLIA_CACHE_ALIAS = "storesAlgoliaCache";
    private final static int HEAP_SIZE = 1500;
    private final static String COPY_RIGHT = "copyRightCache";
    private final static String CATEGORIES_SUBCATEGORIES = "categoriesAndSubcategories";
    private final static String ALGOLIA_CONFIG_HOME ="algoliaConfigHome";
    private final static String ALGOLIA_STORE_CITY ="algoliaStoreCity";
    private final static String ALGOLIA_DEFAULT_STORE ="algoliaDefaultStore";

    private final static String REDIS_DB_IP = URLConnections.REDIS_DB_IP;
    private final static Integer REDIS_DB_PORT = URLConnections.REDIS_DB_PORT;

    public CacheConfig() {
//        LOG.info("constructor CacheConfig called...");
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build();
        cacheManager.init();
        this.cachingData();
    }

    /**
     * Metodo q se encarga de cachear cualquier Data
     */
    private void cachingData() {

        this.cachingCities();
        this.cachingStoresAlgolia();
        this.cachingCopyRight();
        this.cachingCategoriesAndSubcategories();
        this.cachingAlgoliaHomeConfig();
        this.cachingStoreCityAlgolia();
        this.cachingDefaultStore();
        this.cachingAlgoliaItems();
    }

    private void cachingCities() {
        citiesCache = cacheManager.createCache(CITIES_CACHE_ALIAS,
                CacheConfigurationBuilder.newCacheConfigurationBuilder(
                        String.class, (Class<List<City>>)(Object)List.class,
                        ResourcePoolsBuilder.heap(HEAP_SIZE))
                        .withExpiry(Expirations.timeToLiveExpiration(Duration.of(6, TimeUnit.MINUTES)))
                        .build());
        //LOG.info("cachingCities initialization was OK.");
    }

    private void cachingStoresAlgolia(){
        storesAlgoliaCache = cacheManager.createCache(STORES_ALGOLIA_CACHE_ALIAS,
                CacheConfigurationBuilder.newCacheConfigurationBuilder(
                        String.class, (Class<StoresAlgolia>)(Object)Object.class,
                        ResourcePoolsBuilder.heap(HEAP_SIZE))
                        .withExpiry(Expirations.timeToLiveExpiration(Duration.of(60, TimeUnit.MINUTES)))
                        .build());
        //LOG.info("cachingStoresAlgolia initialization was OK.");
    }

    private void cachingCopyRight() {
        copyRighCache = cacheManager.createCache(COPY_RIGHT,
                CacheConfigurationBuilder.newCacheConfigurationBuilder(
                        String.class, (Class<CopyrightJson>)(Object)List.class,
                        ResourcePoolsBuilder.heap(HEAP_SIZE))
                        .withExpiry(Expirations.timeToLiveExpiration(Duration.of(6, TimeUnit.MINUTES)))
                        .build());
        //LOG.info("cachingCopyRight initialization was OK.");
    }


    private void cachingCategoriesAndSubcategories() {
        categoriesAndSubcategories = cacheManager.createCache(CATEGORIES_SUBCATEGORIES,
                CacheConfigurationBuilder.newCacheConfigurationBuilder(
                                String.class, (Class<CollectionResponseModel>)(Object)Object.class,
                                ResourcePoolsBuilder.heap(HEAP_SIZE))
                        .withExpiry(Expirations.timeToLiveExpiration(Duration.of(6, TimeUnit.MINUTES)))
                        .build());
        //LOG.info("cachingCategories and Subcategories  initialization was OK.");
    }

    private void cachingAlgoliaHomeConfig() {
        homeConfigAlgolia = cacheManager.createCache(ALGOLIA_CONFIG_HOME,
                CacheConfigurationBuilder.newCacheConfigurationBuilder(
                                String.class, (Class<HomeConfigAlgolia>)(Object)Object.class,
                                ResourcePoolsBuilder.heap(HEAP_SIZE))
                        .withExpiry(Expirations.timeToLiveExpiration(Duration.of(6, TimeUnit.MINUTES)))
                        .build());
        //LOG.info("cachingAlgoliaHomeConfig initialization was OK.");
    }

    private void cachingStoreCityAlgolia() {
        storeCityAlgolia = cacheManager.createCache(ALGOLIA_STORE_CITY,
                CacheConfigurationBuilder.newCacheConfigurationBuilder(
                                String.class, (Class<String>)(Object)Object.class,
                                ResourcePoolsBuilder.heap(HEAP_SIZE))
                        .withExpiry(Expirations.timeToLiveExpiration(Duration.of(6, TimeUnit.MINUTES)))
                        .build());
        //LOG.info("cachingStoreCityAlgolia initialization was OK.");
    }

    private void cachingDefaultStore() {
        defaultStoreAlgolia = cacheManager.createCache(ALGOLIA_DEFAULT_STORE,
                CacheConfigurationBuilder.newCacheConfigurationBuilder(
                                String.class, (Class<Integer>)(Object)Object.class,
                                ResourcePoolsBuilder.heap(HEAP_SIZE))
                        .withExpiry(Expirations.timeToLiveExpiration(Duration.of(6, TimeUnit.MINUTES)))
                        .build());
        //LOG.info("cachingDefaultStore initialization was OK.");
    }

    private void cachingAlgoliaItems() {
        try {
            this.jedisPool = new JedisPool(REDIS_DB_IP,REDIS_DB_PORT);
        } catch (Exception e) {
            this.jedisPool = null;
        }
    }


    public Cache<String, List<City>> citiesCache() {
        //LOG.info("mnethod CacheConfig.citiesCache called");
        return this.cacheManager.getCache(CITIES_CACHE_ALIAS, String.class, (Class<List<City>>)(Object)List.class );
    }

    public Cache<String, StoresAlgolia> storesAlgoliaCache(){
        //LOG.info("mnethod CacheConfig.StoresAlgolia called");
        return this.cacheManager.getCache(STORES_ALGOLIA_CACHE_ALIAS, String.class,(Class<StoresAlgolia>)(Object)Object.class );
    }

    public Cache<String, List<Copyright>> copyRightCache() {
        //LOG.info("method CacheConfig.copyRightCache called");
        return this.cacheManager.getCache(COPY_RIGHT, String.class, (Class<List<Copyright>>)(Object)List.class );
    }

    public Cache<String, CollectionResponseModel> categoriesAndSubcategories() {
        //LOG.info("method CacheConfig.categoriesAndSubcategories called");
        return this.cacheManager.getCache(CATEGORIES_SUBCATEGORIES, String.class, (Class<CollectionResponseModel>)(Object)Object.class );
    }

    public Cache<String, HomeConfigAlgolia> homeConfigAlgolia() {
        //LOG.info("method CacheConfig.categoriesAndSubcategories called");
        return this.cacheManager.getCache(ALGOLIA_CONFIG_HOME, String.class, (Class<HomeConfigAlgolia>)(Object)Object.class );
    }

    public Cache<String, String> storeCityAlgolia() {
        //LOG.info("method CacheConfig.categoriesAndSubcategories called");
        return this.cacheManager.getCache(ALGOLIA_STORE_CITY, String.class, (Class<String>)(Object)Object.class );
    }

    public Cache<String, Integer> defaultStoreAlgolia() {
        //LOG.info("method CacheConfig.categoriesAndSubcategories called");
        return this.cacheManager.getCache(ALGOLIA_DEFAULT_STORE, String.class, (Class<Integer>)(Object)Object.class );
    }

    public Jedis getJedisClient() {
        Jedis client = null;
        if(Objects.nonNull(jedisPool)) {
            try {
                client = jedisPool.getResource();
            } catch (Exception e) {
                return null;
            }
        }
        return client;
    }





}
