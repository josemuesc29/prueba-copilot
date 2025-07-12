package com.imaginamos.farmatodo.backend.user;

import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.ConflictException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.googlecode.objectify.Key;
import com.imaginamos.farmatodo.backend.order.create_order.domain.OrderUtil;
import com.imaginamos.farmatodo.model.user.Token;
import com.imaginamos.farmatodo.model.user.TokenCacheDto;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.networking.cache.CachedDataStoreDataManager;
import org.jasypt.util.password.StrongPasswordEncryptor;

import java.util.Date;
import java.util.Optional;
import java.util.logging.Logger;

import static com.imaginamos.farmatodo.backend.OfyService.ofy;

/**
 * Created by lelal on 1/11/2016.
 * Created by Imaginamos
 */

public class Authenticate {
  private static final Logger LOG = Logger.getLogger(OrderUtil.class.getName());
  public static boolean isValidToken(String token, String tokenIdWebSafe) throws ConflictException, BadRequestException {
    if (token == null || tokenIdWebSafe == null) {
      throw new BadRequestException(Constants.INVALID_TOKEN);
    }

    Key<Token> keyToken = Key.create(tokenIdWebSafe);
    Token tokenValidator = null;
    int attempts = 0;

    while (attempts < Constants.MAX_RETRIES) {
      try {
        Optional<String> tokenInCache = CachedDataStoreDataManager.getJsonFromCacheIndex(
                String.valueOf(keyToken),
                Constants.INDEX_REDIS_KEY_USER_DATA_STORE
        );

        if (tokenInCache.isPresent()) {
          // Deserializar desde caché usando DTO
          TokenCacheDto tokenDto = new Gson().fromJson(tokenInCache.get(), TokenCacheDto.class);
          tokenValidator = tokenDto.toToken();
        } else {
          // Cargar desde datastore
          tokenValidator = ofy().load().key(keyToken).now();

          if (tokenValidator != null) {
            // Convertir a DTO y guardar en caché
            TokenCacheDto tokenDto = TokenCacheDto.fromToken(tokenValidator);
            String jsonToCache = new Gson().toJson(tokenDto);
            CachedDataStoreDataManager.saveJsonInCacheIndexTime(
                    String.valueOf(keyToken),
                    jsonToCache,
                    Constants.INDEX_REDIS_KEY_USER_DATA_STORE,
                    Constants.TIME_REDIS_KEY_USER_DATA_STORE
            );
          }
        }
        // Si llegamos aquí, la operación tuvo éxito
        break;

      } catch (Exception e) {
        attempts++;
        LOG.warning("Error de contención en el datastore al cargar tokenValidator. Reintento " + attempts + " Error ocurrido " + e);

        if (attempts >= Constants.MAX_RETRIES) {
          LOG.severe("No se pudo validar el tokenValidator después de " + Constants.MAX_RETRIES + " intentos.");
        }

        // Espera exponencial entre reintentos
        try {
          Thread.sleep(Math.min(1000 * (long) Math.pow(2, attempts), 10000));
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
          LOG.severe("Interrupción durante espera entre reintentos: " + ie.getMessage());
        }
      }
    }

    if (tokenValidator == null) {
      throw new ConflictException(Constants.INVALID_TOKEN);
    }

    return new StrongPasswordEncryptor().checkPassword(token, tokenValidator.getToken());
  }

  public static boolean isValidTokenCallCenter(String token, String tokenIdWebSafe) {
    Key<Token> keyToken = Key.create(tokenIdWebSafe);
    Token tokenValidator = ofy().load().key(keyToken).now();
    StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
    Date today = new Date();
    if (passwordEncryptor.checkPassword(token, tokenValidator.getToken()) && tokenValidator.getTokenExp().after(today))
      return true;
    return false;
  }
}
