package com.imaginamos.farmatodo.backend.service;

import com.google.api.server.spi.config.*;
import com.google.api.server.spi.response.*;
import com.imaginamos.farmatodo.backend.user.Authenticate;
import com.imaginamos.farmatodo.model.city.CityJSON;
import com.imaginamos.farmatodo.model.customer.CustomerToken;
import com.imaginamos.farmatodo.model.order.ClientResponse;
import com.imaginamos.farmatodo.model.order.ProcessedOrder;
import com.imaginamos.farmatodo.model.provider.ProviderCreate;
import com.imaginamos.farmatodo.model.provider.ProviderRes;
import com.imaginamos.farmatodo.model.provider.WebServiceClient;
import com.imaginamos.farmatodo.model.store.StoreJSON;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.networking.services.ApiGatewayService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import static com.imaginamos.farmatodo.backend.OfyService.ofy;
//revision
/**
 * @Api - Google indicator that the present class ins an Api.
 * Every further indication of @ApiMethod includes this as a Google API.
 */
@Api(name = "serviceEndpoint",
        version = "v1",
        apiKeyRequired = AnnotationBoolean.TRUE,
        scopes = {Constants.EMAIL_SCOPE},
        clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID_DEVELOPER, com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID},
        audiences = {Constants.ANDROID_AUDIENCE},
        namespace = @ApiNamespace(ownerDomain = Constants.OWNER_DOMAIN, ownerName = Constants.OWNER_NAME, packagePath = ""),
        description = "Stores order for all pages.")
public class ServiceEndpoint {

    private static final Logger LOG = Logger.getLogger(ServiceEndpoint.class.getName());

    private Authenticate authenticate;

    public ServiceEndpoint(){
        authenticate = new Authenticate();
    }
    //ServicesWS/v1/store/active
    @ApiMethod(name = "activeStore", path = "serviceEndpoint/activeStore", httpMethod = ApiMethod.HttpMethod.GET)
    public ResponseEntity getActiveStores() throws IOException, BadRequestException {
        //List<StoreJSON> listStore = CoreConnection.getListRequest(URLConnections.URL_STORES,StoreJSON.class);
        List<StoreJSON> listStore = ApiGatewayService.get().getStoreActive();
    return new ResponseEntity(listStore, HttpStatus.OK);
    }

    @ApiMethod(name = "activeCity", path = "serviceEndpoint/activeCity", httpMethod = ApiMethod.HttpMethod.GET)
    public ResponseEntity getActiveCities() throws IOException, BadRequestException {
        //List<CityJSON> listCity = CoreConnection.getListRequest(URLConnections.URL_CITIES,CityJSON.class);
        List<CityJSON> listCity = ApiGatewayService.get().getCityActive();
        return new ResponseEntity(listCity, HttpStatus.OK);
    }

    @ApiMethod(name = "token", path = "serviceEndpoint/oauth/token", httpMethod = ApiMethod.HttpMethod.PUT)
    public ClientResponse oauthToken(HttpServletRequest request,
                                     @Named("client_id") final String clientId,
                                     @Named("client_secret") final String clientSecret) throws BadRequestException {
        String contentType = request.getHeader("Content-Type");
        LOG.warning("method: oauthToken(clientId, client_secret) -> params: clientId : "+ clientId +" client_secret : "+ clientSecret);
        if (Objects.isNull(clientId) || clientId.isEmpty()) {
//            LOG.info("method: oauthToken() --> BadRequest [client_id is required]");
            throw new BadRequestException("BadRequest [client_id is required]");
        } else if (Objects.isNull(clientSecret) || clientSecret.isEmpty()) {
//            LOG.info("method: oauthToken() --> BadRequest [client_secret is required]");
            throw new BadRequestException("BadRequest [client_secret is required]");
        } else if (Objects.isNull(contentType) || contentType.isEmpty()) {
//            LOG.info("method: oauthToken() --> BadRequest [Content-Type is required]");
            throw new BadRequestException("BadRequest [Content-Type is required]");
        } else if (!MediaType.APPLICATION_FORM_URLENCODED_VALUE.toUpperCase().equals(contentType.toUpperCase())){
//            LOG.info("method: oauthToken() --> BadRequest [Content-Type application/x-www-form-urlencoded is required]");
            throw new BadRequestException("BadRequest [Content-Type application/x-www-form-urlencoded is required]");
        }
        //ClientResponse response = CoreConnection.putRequestAuth(URLConnections.URL_TOKEN, clientId, clientSecret, ClientResponse.class);
        //return response;
        return ApiGatewayService.get().putTokenByClientIdAndClientSecret(clientId, clientSecret);
    }

    @ApiMethod(name = "webServiceClientCreate", path = "serviceEndpoint/webServiceClient/create", httpMethod = ApiMethod.HttpMethod.POST)
    public ResponseEntity webServiceClientCreate(final WebServiceClient webServiceClient) throws BadRequestException {
        LOG.warning("method: webServiceClientCreate() -> params: name : "+ webServiceClient.getName() +" email : "+ webServiceClient.getEmail());
        if (Objects.isNull(webServiceClient.getName()) || webServiceClient.getName().isEmpty()) {
            LOG.warning("method: webServiceClientCreate() --> BadRequest [name is required]");
            throw new BadRequestException("BadRequest [name is required]");
        } else if (Objects.isNull(webServiceClient.getEmail()) || webServiceClient.getEmail().isEmpty()) {
            LOG.warning("method: webServiceClientCreate() --> BadRequest [email is required]");
            throw new BadRequestException("BadRequest [email is required]");
        }

        //ClientResponse response = null;
        try {

            ProviderCreate providerCreate = new ProviderCreate();
            providerCreate.getProviders().add(webServiceClient);
            List<ProviderRes> response = ApiGatewayService.get().postCreateProvider(providerCreate);
            if(Objects.nonNull(response)) {
                return new ResponseEntity(response, HttpStatus.OK);
            }

            /*
            JSONObject orderJSON = new JSONObject();
            JSONArray providers = new JSONArray();
            JSONObject client = new JSONObject();
            client.put("name", webServiceClient.getName());
            client.put("email", webServiceClient.getEmail());
            providers.put(client);

            orderJSON.put("providers", providers);
            String webServiceString = orderJSON.toString();
            LOG.warning("method: webServiceClientCreate() --> request:"+webServiceString);
            response = CoreConnection.postRequest(URLConnections.URL_PROVIDER_CREATE, webServiceString, ClientResponse.class);
            */
        } catch (Exception e) {
            LOG.warning("method: webServiceClientCreate() Error--> "+e.getMessage());
        }
        return new ResponseEntity(null, HttpStatus.BAD_REQUEST);
    }


    @ApiMethod(name = "getDataProccesedOrder", path = "serviceEndpoint/webServiceClient/create", httpMethod = ApiMethod.HttpMethod.GET)
    public List<ProcessedOrder> processedOrderList(@Named("keyClient") final String keyClient) throws UnauthorizedException {
        if (!keyClient.equals(Constants.KEY_SECURE_CLIENT))
            throw new UnauthorizedException(Constants.ERROR_ACCESS_DENIED);

        return ofy().load().type(ProcessedOrder.class).list();

    }

    @ApiMethod(name = "validateTokenAndTokenIdWebSafe", path = "serviceEndpoint/validate/tokenAndTokenIdWebSafe", httpMethod = ApiMethod.HttpMethod.POST)
    public ResponseEntity validateTokenAndTokenIdWebSafe(final CustomerToken customerToken) throws BadRequestException, ConflictException {
        if (!authenticate.isValidToken(customerToken.getToken(), customerToken.getTokenIdWebSafe()))
            throw new ConflictException(Constants.INVALID_TOKEN);
        return new ResponseEntity(true, HttpStatus.OK);
    }

}