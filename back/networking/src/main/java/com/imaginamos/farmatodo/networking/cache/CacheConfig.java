package com.imaginamos.farmatodo.networking.cache;

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
    private final static int HEAP_SIZE = 15000;


    private Cache<String, Object> cachePropertiesAlgolia;
    private JedisPool jedisPool;



    private final static String ALGOLIA_PROPERTIES ="algoliaProperties";
    //    private final static String REDIS_DB_IP = "10.89.109.251";
    private final static String REDIS_DB_IP = URLConnections.REDIS_DB_IP;
    private final static Integer REDIS_DB_PORT = URLConnections.REDIS_DB_PORT;
//    private final static Integer REDIS_DB_PORT = 6379;


    public CacheConfig() {
        LOG.info("constructor CacheConfig called...");
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build();
        cacheManager.init();
        this.cachingData();
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

    /**
     * Metodo q se encarga de cachear cualquier Data
     */
    private void cachingData() {
        this.cachingPropetiesAlgolia();
        this.initCachingDataRedisPool();
    }

    private void initCachingDataRedisPool() {
        try {
            this.jedisPool = new JedisPool(REDIS_DB_IP,REDIS_DB_PORT);
        } catch (Exception e) {
            this.jedisPool = null;
        }
    }


    private void cachingPropetiesAlgolia() {
        cachePropertiesAlgolia = cacheManager.createCache(ALGOLIA_PROPERTIES,
                CacheConfigurationBuilder.newCacheConfigurationBuilder(
                                String.class, Object.class,
                                ResourcePoolsBuilder.heap(HEAP_SIZE))
                        .withExpiry(Expirations.timeToLiveExpiration(Duration.of(5, TimeUnit.MINUTES)))
                        .build());
        LOG.info("cachingAlgoliaProperties initialization was OK.");

    }

    public Cache<String, Object> cachePropertiesAlgolia() {
        //LOG.info("method CacheConfig.categoriesAndSubcategories called");
        return this.cacheManager.getCache(ALGOLIA_PROPERTIES, String.class, Object.class);
    }


}
