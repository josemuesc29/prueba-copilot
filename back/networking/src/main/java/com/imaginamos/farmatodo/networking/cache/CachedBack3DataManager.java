package com.imaginamos.farmatodo.networking.cache;

import com.imaginamos.farmatodo.model.util.URLConnections;
import redis.clients.jedis.Jedis;

import java.util.Optional;
import java.util.logging.Logger;

public class CachedBack3DataManager {

    private static CacheBack3Config cache = new CacheBack3Config();

    private static final Logger LOG = Logger.getLogger(CachedBack3DataManager.class.getName());

    private static final long MAX_REDIS_CACHE_SECONDS = 86400;

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
