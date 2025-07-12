package com.imaginamos.farmatodo.backend.talonone;

import com.google.api.server.spi.config.*;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.ConflictException;
import com.googlecode.objectify.Key;
import com.imaginamos.farmatodo.backend.user.Authenticate;
import com.imaginamos.farmatodo.backend.user.Users;
import com.imaginamos.farmatodo.model.talonone.CustomerSessionExternalRequest;
import com.imaginamos.farmatodo.model.talonone.DiscountTalon;
import com.imaginamos.farmatodo.model.user.User;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.networking.services.ApiGatewayService;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import com.imaginamos.farmatodo.networking.talonone.model.TrackEventItemPurchasedRequest;
import com.imaginamos.farmatodo.networking.talonone.model.TrackEventRequest;
import com.imaginamos.farmatodo.networking.talonone.model.TrackEventResponse;

@Api(name = "talonOneEndpoint",
        version = "v1",
        scopes = {Constants.EMAIL_SCOPE},
        apiKeyRequired = AnnotationBoolean.TRUE,
        clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID_DEVELOPER, com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID},
        audiences = {Constants.ANDROID_AUDIENCE},
        namespace = @ApiNamespace(ownerDomain = Constants.OWNER_DOMAIN, ownerName = Constants.OWNER_NAME),
        description = "Talon One API from AppEngine")
public class TalonOneEndpoint {

    private Authenticate authenticate;
    private Users users;

    public TalonOneEndpoint() {
        authenticate = new Authenticate();
        users = new Users();
    }

    @Deprecated
    @ApiMethod(name = "getDiscounts", path = "/talonOneEndpoint/getDiscounts", httpMethod = ApiMethod.HttpMethod.POST)
    public List<DiscountTalon> getTalonCustomerSession(@Nullable @Named("token") final String token,
                                        @Nullable @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                        @Nullable @Named("idSession") final String idSession,
                                        @Nullable @Named("idCustomerWebSafe") final String idCustomerWebSafe,
                                        final CustomerSessionExternalRequest customerSessionRequest) throws ConflictException, BadRequestException, IOException {
        if (!authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);

        if(Objects.isNull(customerSessionRequest.getItems()))
            throw new ConflictException(Constants.ERROR_CUSTOMER_SESSION);

        Key<User> userKey = Key.create(idCustomerWebSafe);
        User user = users.findUserByKey(userKey);

        String sessionId=tokenIdWebSafe;

        if (Objects.nonNull(user) && user.getId() != 0){
            sessionId = String.valueOf(user.getId());
        }

        List<DiscountTalon> discountsTalon = ApiGatewayService.get().updateCustomerSession(customerSessionRequest, sessionId);

        return discountsTalon;
     }

    @ApiMethod(name = "getTrackEvent", path = "/talonOneEndpoint/getTrackEvent", httpMethod = ApiMethod.HttpMethod.POST)
    public TrackEventResponse getTalonTrackEvent(@Nullable @Named("idCustomerWebSafe") final String idCustomerWebSafe,
                                                 @Nullable @Named("token") final String token,
                                                 @Nullable @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                                 @Nullable @Named("idSession") final String idSession,
                                                 final TrackEventRequest trackEventRequest) throws ConflictException, BadRequestException, IOException {
        if (!authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);

        TrackEventResponse trackEventResponse = ApiGatewayService.get().getTrackEventCustom(trackEventRequest);

        return trackEventResponse;
    }

    @ApiMethod(name = "trackEventItemPurchased", path = "/talonOneEndpoint/getTrackEventItemPurchased", httpMethod = ApiMethod.HttpMethod.POST)
    public TrackEventResponse getTrackEventItemPurchased(@Nullable @Named("idCustomerWebSafe") final String idCustomerWebSafe,
                                                 @Nullable @Named("token") final String token,
                                                 @Nullable @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                                 @Nullable @Named("idSession") final String idSession,
                                                 final TrackEventItemPurchasedRequest trackEventRequest) throws ConflictException, BadRequestException, IOException {
        if (!authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);

        TrackEventResponse trackEventResponse = ApiGatewayService.get().getTrackEventPurchased(trackEventRequest);

        return trackEventResponse;
    }
}
