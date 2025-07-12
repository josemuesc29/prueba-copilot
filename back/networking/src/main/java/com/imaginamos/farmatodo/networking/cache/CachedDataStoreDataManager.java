package com.imaginamos.farmatodo.networking.cache;

import redis.clients.jedis.Jedis;

import java.util.Optional;
import java.util.logging.Logger;

public class CachedDataStoreDataManager {

    private static CacheConfigDataStore cache = new CacheConfigDataStore();

    private static final Logger LOG = Logger.getLogger(CachedDataStoreDataManager.class.getName());

    private static final long MAX_REDIS_CACHE_SECONDS = 86400;

    public static Optional<String> getJsonFromCache(String key){
        try (Jedis jedis = cache.getJedisClient()) {
            String json = jedis.get(key);

            if (json == null)
                return Optional.empty();

            return Optional.of(json);

        } catch (Exception e) {
            return Optional.empty();
        }

    }

    public static Optional<String> getJsonFromCacheIndex(String key, int index){
        try (Jedis jedis = cache.getJedisClient()) {
            jedis.select(index);
            String json = jedis.get(key);

            if (json == null)
                return Optional.empty();

            return Optional.of(json);

        } catch (Exception e) {

            return Optional.empty();
        }

    }

    public static void saveJsonInCacheIndexTime(String key, String json, int index, long time){
        if (key == null || key.isEmpty())
            return;

        if (json == null || json.isEmpty())
            return;

        try (Jedis jedis = cache.getJedisClient()) {
            jedis.select(index);
            jedis.setex(key, time ,json);
            LOG.info("SAVE_REDIS_CACHE -> " + key );
        } catch (Exception e) {
            LOG.warning("No se pudo agregar al cache, problema con redis" + e.getMessage());
        }

    }

    public static void saveJsonInCache(String key, String json){
        if (key == null || key.isEmpty())
            return;

        if (json == null || json.isEmpty())
            return;

        try (Jedis jedis = cache.getJedisClient()) {
            jedis.setex(key, MAX_REDIS_CACHE_SECONDS ,json);
        } catch (Exception e) {
            LOG.warning("No se pudo agregar al cache, problema con redis" + e.getMessage());
        }

    }

    public static boolean deleteKeyIndex(String key, int index){
        if (key == null || key.isEmpty())
            return false;

        try (Jedis jedis = cache.getJedisClient()) {
            jedis.select(index);
            long keysDeleted = jedis.del(key);
            return keysDeleted >= 1;
        } catch (Exception e) {
            LOG.warning("No se pudo eliminar del cache, problema con redis" + e.getMessage());
        }
        return false;
    }

}
