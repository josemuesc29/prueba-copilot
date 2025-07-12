package com.imaginamos.farmatodo.networking.cache;

import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheManagerBuilder;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Objects;
import java.util.logging.Logger;

public class CacheConfigDataStore {

    private static final Logger LOG = Logger.getLogger(CacheBack3Config.class.getName());

    private CacheManager cacheManager;

    private JedisPool jedisPool;

    private final static String REDIS_DB_IP = "10.143.171.227";
    private final static Integer REDIS_DB_PORT = 6379;


    public CacheConfigDataStore() {
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
        this.initCachingDataRedisPool();
    }

    private void initCachingDataRedisPool() {
        try {
            this.jedisPool = new JedisPool(REDIS_DB_IP,REDIS_DB_PORT);
        } catch (Exception e) {
            this.jedisPool = null;
        }
    }
}
