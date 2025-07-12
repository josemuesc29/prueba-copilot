package com.imaginamos.farmatodo.backend.product;

import com.algolia.search.exceptions.AlgoliaException;
import com.google.api.server.spi.config.*;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.ConflictException;
import com.imaginamos.farmatodo.networking.algolia.APIAlgolia;
import com.imaginamos.farmatodo.model.algolia.Filter;
import com.imaginamos.farmatodo.model.util.Constants;

import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

@Api(name = "filtersEndpoint",
        version = "v1",
        apiKeyRequired = AnnotationBoolean.TRUE,
        scopes = {Constants.EMAIL_SCOPE},
        clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID_DEVELOPER, Constants.ANDROID_CLIENT_ID_RELEASE, Constants.ANDROID_CLIENT_ID_DEBUG, com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID},
        audiences = {Constants.WEB_CLIENT_ID, Constants.ANDROID_AUDIENCE, Constants.ANDROID_CLIENT_ID_DEBUG },
        namespace = @ApiNamespace(ownerDomain = Constants.OWNER_DOMAIN, ownerName = Constants.OWNER_NAME, packagePath = ""),
        description = "Stores order for all pages.")
public class FiltersEndPoint {
    private static final Logger log = Logger.getLogger(FiltersEndPoint.class.getName());

    @ApiMethod(name = "getFiltersItems", path = "/filtersEndpoint/getFiltersItems", httpMethod = ApiMethod.HttpMethod.GET)
    @Deprecated
    public List<Filter> getFiltersItems() throws ConflictException, BadRequestException, AlgoliaException {
        List<Filter> filters = APIAlgolia.getFilterList();
        if(Objects.nonNull(filters)) {
            filters.stream().forEach(filter ->  filter.getValues().stream().forEach(filterDetail -> {
                log.info("method: getFiltersItems() --> "+filterDetail);
                })
            );
        }
        return Objects.nonNull(filters) ? filters : null;
    }
}
