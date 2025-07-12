package com.imaginamos.farmatodo.backend.rms;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.InternalServerErrorException;
import com.imaginamos.farmatodo.model.order.Bck3EventResponse;
import com.imaginamos.farmatodo.model.order.CoreEventResponse;
import com.imaginamos.farmatodo.model.order.FulfilOrdColDescDomain;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.networking.services.CoreService;

import java.util.Objects;
import java.util.logging.Logger;

@Api(name = "rmsEndpoint",
    version = "v1",
    scopes = {Constants.EMAIL_SCOPE},
    clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID_DEVELOPER, com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID},
    audiences = {Constants.ANDROID_AUDIENCE},
    namespace = @ApiNamespace(ownerDomain = Constants.OWNER_DOMAIN, ownerName = Constants.OWNER_NAME, packagePath = ""),
    description = "RMS endpoint.")
public class RMSEndpoint {

  private static final Logger LOG = Logger.getLogger(RMSEndpoint.class.getName());

  @ApiMethod(
      name = "ping",
      path = "/rmsEndpoint/ping",
      httpMethod = ApiMethod.HttpMethod.GET)
  @Deprecated
  public CoreEventResponse ping() throws InternalServerErrorException {
//    LOG.info(" method: ping() - Service migrated to backend 3");
    try {
      CoreEventResponse response = CoreService.get().pingRMS();
//      LOG.info("method: ping() -> Response: " + response.toString());
      switch (response.getCode()) {
        case OK:
          return response;
        case APPLICATION_ERROR:
        default:
          throw new InternalServerErrorException(response.getMessage());
      }
    } catch (Exception e) {
      LOG.warning("method: ping() --> Error: " + e.fillInStackTrace());
      throw new InternalServerErrorException(e.getMessage());
    }
  }


  @ApiMethod(
      name = "createFulfilOrdColDesc",
      path = "/rmsEndpoint/fulfillment",
      httpMethod = ApiMethod.HttpMethod.POST)
  @Deprecated
  public CoreEventResponse createFulfilOrdColDesc(
      final FulfilOrdColDescDomain fulfilOrdColDescDomain)
      throws BadRequestException, InternalServerErrorException {
//    LOG.info(" method: createFulfilOrdColDesc() - Service migrated to backend 3");
    if (Objects.isNull(fulfilOrdColDescDomain)) {
      LOG.warning("method: createFulfilOrdColDesc() --> BadRequest [fulfilOrdColDescDomain is null]");
      throw new BadRequestException("BadRequest [fulfilOrdColDescDomain is null]");
    }
    if (Objects.isNull(fulfilOrdColDescDomain.getFulfilOrdDesc()) || fulfilOrdColDescDomain.getFulfilOrdDesc().length == 0) {
      LOG.warning("method: createFulfilOrdColDesc() --> BadRequest [fulfilOrdColDescDomain.fulfilOrdDesc is null or empty]");
      throw new BadRequestException("BadRequest [fulfilOrdColDescDomain.fulfilOrdDesc is null or empty]");
    }
//    LOG.info("method: createFulfilOrdColDesc() --> CORE request: " + fulfilOrdColDescDomain.toStringJson());
    try {
      CoreEventResponse response = CoreService.get().createFulfilOrdColDesc(fulfilOrdColDescDomain);
//      LOG.info("method: createFulfilOrdColDesc() --> CORE response: " + response.toString());
      switch (response.getCode()) {
        case OK:
        case BAD_REQUEST:
        case REJECTED:
        case DUPLICATED:
        case ERROR:
          return response;
        default:
          throw new InternalServerErrorException(response.getMessage());
      }
    } catch (Exception e) {
      LOG.warning("method: createFulfilOrdColDesc() --> Error: " + e.fillInStackTrace());
      throw new InternalServerErrorException(e.getMessage());
    }
  }

}
