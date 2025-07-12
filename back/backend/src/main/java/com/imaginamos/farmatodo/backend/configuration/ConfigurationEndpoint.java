package com.imaginamos.farmatodo.backend.configuration;

/**
 * Created by Admin on 20/06/2017.
 */

import com.algolia.search.exceptions.AlgoliaException;
import com.google.api.server.spi.config.*;
import com.google.api.server.spi.response.ConflictException;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.gson.Gson;
import com.imaginamos.farmatodo.backend.cache.CachedDataManager;
import com.imaginamos.farmatodo.model.algolia.DeliveryTypeTime;
import com.imaginamos.farmatodo.model.algolia.DeliveryTypeTimeResponse;
import com.imaginamos.farmatodo.model.algolia.OriginProperties;
import com.imaginamos.farmatodo.model.algolia.PropertiesAlgoliaFacets;
import com.imaginamos.farmatodo.model.algolia.flag.FlagRegistry;
import com.imaginamos.farmatodo.model.city.GeoGridsConfigAlgolia;
import com.imaginamos.farmatodo.model.order.GenericResponse;
import com.imaginamos.farmatodo.model.provider.ProviderConfigAlgolia;
import com.imaginamos.farmatodo.model.util.Answer;
import com.imaginamos.farmatodo.model.util.AppVersion;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.model.util.HttpStatusCode;
import com.imaginamos.farmatodo.networking.algolia.APIAlgolia;
import com.imaginamos.farmatodo.networking.models.addresses.osrm.RouteOSRMResponse;
import com.imaginamos.farmatodo.networking.services.ApiGatewayService;
import com.imaginamos.farmatodo.networking.services.OSRMService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import static com.imaginamos.farmatodo.backend.OfyService.ofy;

/**
 * @Api - Google indicator that the present class ins an Api.
 * Every further indication of @ApiMethod includes this as a Google API.
 */
@Api(name = "configurationEndpoint",
        version = "v1",
        apiKeyRequired = AnnotationBoolean.TRUE,
        scopes = {Constants.EMAIL_SCOPE},
        clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID_DEVELOPER, com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID},
        audiences = {Constants.ANDROID_AUDIENCE},
        namespace = @ApiNamespace(ownerDomain = Constants.OWNER_DOMAIN, ownerName = Constants.OWNER_NAME, packagePath = ""),
        description = "Stores configuration for apps for all pages.")

public class ConfigurationEndpoint  {

  private static final Logger LOGGER = Logger.getLogger(ConfigurationEndpoint.class.getName());


  public ConfigurationEndpoint() {
  }

  @ApiMethod(name = "updateVersion", path = "/configurationEndpoint/updateVersion", httpMethod = ApiMethod.HttpMethod.PUT)
  public Answer updateVersion(final AppVersion appVersion) throws UnauthorizedException {
    LOGGER.info("method: ConfigurationEndpoint.updateVersion()");
      if (Objects.isNull(appVersion) || Objects.isNull(appVersion.getKeyClient()) || !appVersion.getKeyClient().equals(Constants.KEY_SECURE_CLIENT))
      throw new UnauthorizedException(Constants.ERROR_ACCESS_DENIED);

    AppVersion appVersionSaved = ofy().load().type(AppVersion.class).filter("platform", appVersion.getPlatform()).first().now();
    if (appVersionSaved == null) {
      appVersionSaved = new AppVersion();
      appVersionSaved.setAppVersionId(UUID.randomUUID().toString());
    }
    appVersionSaved.setPlatform(appVersion.getPlatform());
    appVersionSaved.setUpdate(appVersion.getUpdate());
    appVersionSaved.setAppVersion(appVersion.getAppVersion());
    appVersionSaved.setUpdateCopy(appVersion.getUpdateCopy());
    appVersionSaved.setIntVersion(appVersion.getIntVersion());
    ofy().save().entity(appVersionSaved);
    Answer answer = new Answer();
    answer.setConfirmation(true);
    return answer;
  }

  @ApiMethod(name = "getLastVersion", path = "/configurationEndpoint/getLastVersion", httpMethod = ApiMethod.HttpMethod.GET)
  public AppVersion getLastVersion(@Named("keyClient") final String keyClient, @Named("platform") final String platform) throws UnauthorizedException {
    LOGGER.info("method: ConfigurationEndpoint.getLastVersion()");
      if (Objects.isNull(keyClient) || !keyClient.equals(Constants.KEY_SECURE_CLIENT))
      throw new UnauthorizedException(Constants.ERROR_ACCESS_DENIED);
    return ofy().load().type(AppVersion.class).filter("platform", platform).first().now();
  }

    @ApiMethod(name = "validateServices", path = "/configurationEndpoint/validateServices", httpMethod = ApiMethod.HttpMethod.GET)
    public Answer validateServices(@Named("keyClient") final String keyClient) throws UnauthorizedException, IOException, ConflictException {
        LOGGER.info("method: ConfigurationEndpoint.validateServices()");
        if (Objects.isNull(keyClient) || !keyClient.equals(Constants.KEY_SECURE_CLIENT))
            throw new UnauthorizedException(Constants.ERROR_ACCESS_DENIED);
        //RequestEmailValidate request = new RequestEmailValidate("correo@correo.com");
        //boolean serviceAvailable = CoreService.get().validateCustomerConection(request);
        boolean serviceAvailable = ApiGatewayService.get().validateCustomerEmail("correo@correo.com").getData();

        Answer answer = new Answer();
        answer.setConfirmation(serviceAvailable);
        return answer;
    }

    @ApiMethod(name = "getFacetsAlgolia", path = "/configurationEndpoint/getFacetsAlgolia", httpMethod = ApiMethod.HttpMethod.GET)
    public PropertiesAlgoliaFacets getFacetsAlgolia(@Named("keyClient") final String keyClient) throws UnauthorizedException {
        LOGGER.info("method: getFacetsAlgolia");
        if (Objects.isNull(keyClient) || !keyClient.equals(Constants.KEY_SECURE_CLIENT))
            throw new UnauthorizedException(Constants.ERROR_ACCESS_DENIED);

        return APIAlgolia.getFacetsAlgolia();
    }

    @ApiMethod(name = "getFacetsAlgoliaV2", path = "/configurationEndpoint/v2/getFacetsAlgolia", httpMethod = ApiMethod.HttpMethod.GET)
    public GenericResponse<PropertiesAlgoliaFacets> getFacetsAlgoliaV2() throws UnauthorizedException {

        PropertiesAlgoliaFacets propertiesAlgoliaFacets = APIAlgolia.getFacetsAlgolia();

        GenericResponse<PropertiesAlgoliaFacets> response = new GenericResponse<>();

        response.setCode("OK");
        response.setMessage("Success");
        response.setData(propertiesAlgoliaFacets);

        return response;
    }


    @ApiMethod(name = "testAlgolia", path = "/configurationEndpoint/testAlgolia", httpMethod = ApiMethod.HttpMethod.GET)
    public ProviderConfigAlgolia testAlgolia(@Named("keyClient") final String keyClient) throws UnauthorizedException {
        LOGGER.info("method: getFacetsAlgolia");
        if (Objects.isNull(keyClient) || !keyClient.equals(Constants.KEY_SECURE_CLIENT))
        throw new UnauthorizedException(Constants.ERROR_ACCESS_DENIED);

     return APIAlgolia.getLandingConfig();
    }

    @ApiMethod(name = "testRedirect", path = "/configurationEndpoint/testRedirect", httpMethod = ApiMethod.HttpMethod.GET )
    public Object testRedirect(HttpServletResponse response) throws IOException {
        LOGGER.info("method: testRedirect");
        String redirectUrl = "https://www.farmatodo.com.co";
        response.sendRedirect(redirectUrl);
        return response;
    }

    /**
     * Obtener el tiempo de entrega por un tipo de envio.
     * */
    @ApiMethod(name = "getDeliveryTypeTime", path = "/configurationEndpoint/getDeliveryTypeTime", httpMethod = ApiMethod.HttpMethod.GET)
    public DeliveryTypeTimeResponse getDeliveryTypeTime(@Named("keyClient") final String keyClient) {
        try {
            if (Objects.isNull(keyClient) || !keyClient.equals(Constants.KEY_SECURE_CLIENT)){
                return new DeliveryTypeTimeResponse(HttpStatusCode.UNAUTHORIZED.getCode(),
                        HttpStatusCode.UNAUTHORIZED.getStatusName(),
                        Constants.KEY_CLIENT_UNAUTHORIZED);
            }

            DeliveryTypeTime config = APIAlgolia.getDeliveryTimeConfig();

            if (config != null) {
                return new DeliveryTypeTimeResponse(HttpStatusCode.OK.getCode(), HttpStatusCode.OK.getStatusName(),
                        Constants.SUCCESS, config);
            }

            return new DeliveryTypeTimeResponse(HttpStatusCode.NO_CONTENT.getCode(),
                    HttpStatusCode.NO_CONTENT.getStatusName(),
                    Constants.DELIVERY_TIME_NOT_FOUND);

        } catch (Exception e) {
            LOGGER.warning("Error in ConfigurationEndpoint.getDeliveryTypeTime. Message:"+ Arrays.toString(e.getStackTrace()));
            return new DeliveryTypeTimeResponse(HttpStatusCode.INTERNAL_SERVER_ERROR.getCode(),
                    HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusName(),
                    Constants.UNEXPECTED_ERROR,e.getMessage());
        }
    }

    @ApiMethod(name = "getOriginProperties", path = "/configurationEndpoint/getOriginProperties", httpMethod = ApiMethod.HttpMethod.GET)
    public List<OriginProperties> getOriginProperties() throws AlgoliaException {
        List<OriginProperties> propsCached = CachedDataManager.getOriginPropertiesCached();
        if(Objects.nonNull(propsCached)) {
            return propsCached;
        }
        propsCached = APIAlgolia.getOriginProperties();
        CachedDataManager.saveOriginProperties(new Gson().toJson(propsCached));
        return propsCached;
    }

    @ApiMethod(name = "getGridProperties", path = "/configurationEndpoint/getGridProperties", httpMethod = ApiMethod.HttpMethod.GET)
    public GeoGridsConfigAlgolia getGridProperties() throws AlgoliaException {
        return APIAlgolia.getGeoGridsAlgolia();
    }

    @ApiMethod(name = "getFlagProperties", path = "/configurationEndpoint/getFlagProperties", httpMethod = ApiMethod.HttpMethod.GET)
    public FlagRegistry getFlagProperties() throws AlgoliaException {
        return APIAlgolia.getFlagRegistry();
    }

    @ApiMethod(name = "test", path = "/configurationEndpoint/test", httpMethod = ApiMethod.HttpMethod.POST)
    public Answer test(HttpServletRequest req) {
//        SendMailReq sendMailReq = new SendMailReq();
//        sendMailReq.setTo("cristhian.rodriguez@farmatodo.com");
//        sendMailReq.setSubject("email test via backend3");
//        sendMailReq.setText("<br> email enviado via backend3 !!</br>");
//        ApiGatewayService.get().sendEmailBraze(sendMailReq);
//        return sendMailReq;
        Answer answer = new Answer();
        answer.setMessage("Headers -> " + req.getHeader("version"));
        return answer;
    }

    @ApiMethod(name = "warmUp", path = "/warmup", httpMethod = ApiMethod.HttpMethod.GET)
    public Answer warmup() {
        Answer answer = new Answer();
        return answer;
    }

    @ApiMethod(name = "osrm", path = "/configurationEndpoint/osrm", httpMethod = ApiMethod.HttpMethod.GET)
    public Answer osrmTest() {
        Answer answer = new Answer();

        Optional<RouteOSRMResponse> osrmResponseOptional = OSRMService.get()
                .getORSMRoute(-74.0318, 4.7254, -74.0322954, 4.7174225);
        answer.setConfirmation(osrmResponseOptional.isPresent());
        osrmResponseOptional.ifPresent(routeOSRMResponse -> answer.setMessage(Objects.requireNonNull(routeOSRMResponse.getRoutes().get(0).getDistance()).toString()));
        return answer;
    }

}
