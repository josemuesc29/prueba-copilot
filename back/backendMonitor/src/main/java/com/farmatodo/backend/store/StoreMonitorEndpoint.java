package com.farmatodo.backend.store;

import com.farmatodo.backend.user.Authenticate;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.ConflictException;
import com.google.api.server.spi.response.InternalServerErrorException;
import com.imaginamos.farmatodo.model.city.CityJSON;
import com.imaginamos.farmatodo.model.store.StoreJSON;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.networking.services.ApiGatewayService;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * @Api - Google indicator that the present class ins an Api.
 * Every further indication of @ApiMethod includes this as a Google API.
 */
@Api(name = "storeMonitorEndpoint",
    version = "v1",
    scopes = {Constants.EMAIL_SCOPE},
    clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID_DEVELOPER, com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID},
    audiences = {Constants.ANDROID_AUDIENCE},
    namespace = @ApiNamespace(ownerDomain = Constants.OWNER_DOMAIN, ownerName = Constants.OWNER_NAME, packagePath = ""),
    description = "Get user information for monitor.")
public class StoreMonitorEndpoint {
  private static final Logger log = Logger.getLogger(StoreMonitorEndpoint.class.getName());
  private Authenticate authenticate;

  public StoreMonitorEndpoint() {
    authenticate = new Authenticate();
  }

  /**
   * Get stores
   * @return json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getStores", path = "/storeMonitorEndpoint/getStores", httpMethod = ApiMethod.HttpMethod.GET)
  public List<StoreJSON> getStores() throws ConflictException, BadRequestException, IOException, InternalServerErrorException {
    //List<StoreJSON> listStore = ApiBackend30Service.get().getStoreActive();
    //return CoreConnection.getListRequest(URLConnections.URL_STORES, JSONObject.class);
    return ApiGatewayService.get().getStoreActive();
  }

  /**
   * Get cities
   * @return json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getCities", path = "/storeMonitorEndpoint/getCities", httpMethod = ApiMethod.HttpMethod.GET)
  public List<CityJSON> getCities() throws ConflictException, BadRequestException, IOException, InternalServerErrorException {
    return ApiGatewayService.get().getCityActive();
    //return CoreConnection.getListRequest(URLConnections.URL_CITIES, JSONObject.class);
  }
}

