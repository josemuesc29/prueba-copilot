package com.imaginamos.farmatodo.backend.copyrigth;

import com.google.api.server.spi.config.*;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.ConflictException;
import com.google.api.server.spi.response.InternalServerErrorException;
import com.google.appengine.api.datastore.Query;
import com.googlecode.objectify.Key;
import com.imaginamos.farmatodo.backend.cache.CachedDataManager;
import com.imaginamos.farmatodo.backend.user.Authenticate;
import com.imaginamos.farmatodo.model.copyright.Copyright;
import com.imaginamos.farmatodo.model.copyright.CopyrightJson;
import com.imaginamos.farmatodo.model.util.Constants;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import static com.imaginamos.farmatodo.backend.OfyService.ofy;

@Api(name = "copyrightEndPoint",
        version = "v1",
        apiKeyRequired = AnnotationBoolean.TRUE,
        scopes = {Constants.EMAIL_SCOPE},
        clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID_DEVELOPER, com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID},
        audiences = {Constants.ANDROID_AUDIENCE},
        namespace = @ApiNamespace(ownerDomain = Constants.OWNER_DOMAIN, ownerName = Constants.OWNER_NAME, packagePath = ""),
        description = "Copyright by Delivery orders.")
public class CopyrightEndPoint {
    private static CachedDataManager cachedDataManager;
    private static final Logger LOG = Logger.getLogger(CopyrightEndPoint.class.getName());
    private Authenticate authenticate;
    private Copyrights copyrights;

    public CopyrightEndPoint() {
        authenticate = new Authenticate();
        copyrights = new Copyrights();
    }

    @ApiMethod(name = "createCopyright", path = "/copyrightEndPoint/createCopyright", httpMethod = ApiMethod.HttpMethod.POST)
    public CopyrightJson createCopyright(
            @Named("token") final String token,
            @Named("tokenIdWebSafe") final String tokenIdWebSafe,
            final CopyrightJson copyrightJson) throws BadRequestException, ConflictException,
            IOException, InternalServerErrorException {
        // TODO: inhabilitado para pruebas
        //if (!authenticate.isValidToken(token, tokenIdWebSafe))
        //    throw new ConflictException(Constants.INVALID_TOKEN);

        //LOG.warning(" method: createCopyright (copyrightJson) "+copyrightJson);
        if (Objects.isNull(copyrightJson.getId())) {
            //LOG.warning(" method: createCopyright (copyrightJson) --> BadRequest[id is required] "+copyrightJson);
            throw new BadRequestException("BadRequest [id is required]");
        }
        if (Objects.isNull(copyrightJson.getDescription())) {
            //LOG.warning(" method: createCopyright (copyrightJson) --> BadRequest[description is required] "+copyrightJson);
            throw new BadRequestException("BadRequest [description is required]");
        }

        Copyright copyright = new Copyright(copyrightJson.getId(), copyrightJson.getDescription(), copyrightJson.getDeliveryType(), copyrightJson.getActive(), copyrightJson.getProvider());
        //LOG.warning(" method: createCopyright (copyright) "+copyright);
        Key<Copyright> copyrightKey = ofy().save().entity(copyright).now();
        //LOG.warning(" method: Key (copyright) "+copyright);
        return copyrightJson;
    }

    @ApiMethod(name = "getCopyrights", path = "/copyrightEndPoint/getCopyrights", httpMethod = ApiMethod.HttpMethod.GET)
    public List<Copyright> getCopyrights (
            @Named("token") final String token,
            @Named("tokenIdWebSafe") final String tokenIdWebSafe,
            @Named("deliveryType") final String deliveryType,
            @Named("isProvider") final Boolean provider) throws BadRequestException, ConflictException,
            IOException, InternalServerErrorException {
        if (!authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);

        //LOG.warning(" method: getCopyrightByDeliveryType (deliveryType) "+deliveryType+ " (isProvider) "+provider);
        if (Objects.isNull(deliveryType) || deliveryType.isEmpty()) {
            //LOG.warning(" method: getCopyrightByDeliveryType (copyrightJson) --> BadRequest[deliveryType is required] "+deliveryType);
            throw new BadRequestException("BadRequest [deliveryType is required]");
        }
        Query.Filter filterActive = new Query.FilterPredicate("active", Query.FilterOperator.EQUAL, true);
        List<Copyright> copyrights = ofy().load().type(Copyright.class).filter("deliveryType", deliveryType).filter(filterActive).order("id").list();
        if(provider) {
            Query.Filter filterProvider = new Query.FilterPredicate("provider", Query.FilterOperator.EQUAL, provider);
            List<Copyright> providers = ofy().load().type(Copyright.class).filter(filterProvider).filter(filterActive).order("id").list();
            if(Objects.nonNull(providers) && !providers.isEmpty()) {
                copyrights.addAll(providers);
            }
        }
        //List<Copyright> copyrights = CachedDataManager.getCopyRighCached(token, tokenIdWebSafe, deliveryType, provider);

        return copyrights;
    }
}
