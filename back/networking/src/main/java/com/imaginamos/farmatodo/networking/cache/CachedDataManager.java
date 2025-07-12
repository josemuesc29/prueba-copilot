package com.imaginamos.farmatodo.networking.cache;

import com.imaginamos.farmatodo.model.util.URLConnections;
import redis.clients.jedis.Jedis;

import java.util.Optional;
import java.util.logging.Logger;

public class CachedDataManager {

    private static CacheConfig cache = new CacheConfig();

    private static final Logger LOG = Logger.getLogger(CachedDataManager.class.getName());

    private static final long MAX_REDIS_CACHE_SECONDS = 86400;


    public static Object getAlgoliaProperties(String key) {
        if (cache.cachePropertiesAlgolia().containsKey(key)) {
            //LOG.info("AlgoliaProperties estan en cache. Se usaran." + key);
            return cache.cachePropertiesAlgolia().get(key);
        }
        return null;
    }

    public static void setAlgoliaProperties(String key, Object value) {
        cache.cachePropertiesAlgolia().put(key, value);
        //LOG.info("No hay cache, guardando objeto:" + key);
    }

    public static Optional<String> getJsonFromCache(String key){
        try (Jedis jedis = cache.getJedisClient()) {
            String json = jedis.get(key);

            if (json == null)
                return Optional.empty();

            //LOG.info("REDIS_CACHE_FOUND -> "+ key);
            return Optional.of(json);

        } catch (Exception e) {
            //LOG.info("No se pudo obtener item del cache, problema con redis");
            return Optional.empty();
        }

    }

    public static void saveJsonInCache(String key, String json){
        if (key == null || key.isEmpty())
            return;

        if (json == null || json.isEmpty())
            return;

        try (Jedis jedis = cache.getJedisClient()) {
            jedis.setex(key, MAX_REDIS_CACHE_SECONDS ,json);
            //LOG.info("SAVE_REDIS_CACHE -> " + key );
        } catch (Exception e) {
            LOG.warning("No se pudo agregar al cache, problema con redis" + e.getMessage());
        }

    }

    public static boolean deleteKey(String key){
        if (key == null || key.isEmpty())
            return false;

        try (Jedis jedis = cache.getJedisClient()) {
            long keysDeleted = jedis.del(key);
            //LOG.info("DELETE_KEY_REDIS -> " + key + "KeysDeleted -> " + keysDeleted);
            return keysDeleted >= 1;
        } catch (Exception e) {
            LOG.warning("No se pudo eliminar del cache, problema con redis" + e.getMessage());
        }
        return false;
    }

    public static void saveHolidaysInCache(String json){

        if (json == null || json.isEmpty())
            return;

        try (Jedis jedis = cache.getJedisClient()) {
            jedis.select(5);
            jedis.set(URLConnections.ALGOLIA_HOLIDAYS, json);
            //LOG.info("Save Holidays in cache");
        } catch (Exception e) {
            LOG.warning("No se pudo agregar al cache, problema con redis" + e.getMessage());
        }
    }

    public static String getHolidaysFromCache(){
        try (Jedis jedis = cache.getJedisClient()) {
            jedis.select(5);
            return jedis.get(URLConnections.ALGOLIA_HOLIDAYS);
        } catch (Exception e) {
            //LOG.warning("No se pudo obtener Holidays en cache" + e.getMessage());
            return null;
        }
    }

    public static Optional<String> getUiidBrazeFromCache(String key){
        try (Jedis jedis = cache.getJedisClient()) {
            jedis.select(14);
            String json = jedis.get(key);

            if (json == null)
                return Optional.empty();

            //LOG.info("REDIS_CACHE_FOUND -> "+ key);
            return Optional.of(json);

        } catch (Exception e) {
            //LOG.info("No se pudo obtener item del cache, problema con redis");
            return Optional.empty();
        }

    }

    public static void saveUiidBrazeInCache(String key, String json){
        if (key == null || key.isEmpty())
            return;

        if (json == null || json.isEmpty())
            return;

        try (Jedis jedis = cache.getJedisClient()) {
            jedis.select(14);
            jedis.setex(key, MAX_REDIS_CACHE_SECONDS ,json);
            //LOG.info("SAVE_REDIS_CACHE -> " + key );
        } catch (Exception e) {
            LOG.warning("No se pudo agregar al cache, problema con redis" + e.getMessage());
        }

    }

    public static Optional<String> getJsonFromCacheIndex(String key, int index){
        try (Jedis jedis = cache.getJedisClient()) {
            jedis.select(index);
            String json = jedis.get(key);

            if (json == null)
                return Optional.empty();

            //LOG.info("REDIS_CACHE_FOUND -> "+ key);
            return Optional.of(json);

        } catch (Exception e) {
            //LOG.info("No se pudo obtener item del cache, problema con redis");
            return Optional.empty();
        }

    }

    public static void saveJsonInCacheIndex(String key, String json, int index){
        if (key == null || key.isEmpty())
            return;

        if (json == null || json.isEmpty())
            return;

        try (Jedis jedis = cache.getJedisClient()) {
            jedis.select(index);
            jedis.setex(key, MAX_REDIS_CACHE_SECONDS ,json);
            //LOG.info("SAVE_REDIS_CACHE -> " + key );
        } catch (Exception e) {
            LOG.warning("No se pudo agregar al cache, problema con redis" + e.getMessage());
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

    public static boolean deleteKeyIndex(String key, int index){
        if (key == null || key.isEmpty())
            return false;

        try (Jedis jedis = cache.getJedisClient()) {
            jedis.select(index);
            long keysDeleted = jedis.del(key);
            //LOG.info("DELETE_KEY_REDIS -> " + key + "KeysDeleted -> " + keysDeleted);
            return keysDeleted >= 1;
        } catch (Exception e) {
            LOG.warning("No se pudo eliminar del cache, problema con redis" + e.getMessage());
        }
        return false;
    }

}
