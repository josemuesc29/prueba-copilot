package com.imaginamos.farmatodo.backend.order.create_order.infraestructure;


import com.algolia.search.exceptions.AlgoliaException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.ConflictException;
import com.imaginamos.farmatodo.backend.order.create_order.domain.DeliveryTypeEnum;
import com.imaginamos.farmatodo.backend.order.create_order.domain.Guard;
import com.imaginamos.farmatodo.backend.order.create_order.domain.OrderStoreService;
import com.imaginamos.farmatodo.model.order.DeliveryOrder;
import com.imaginamos.farmatodo.model.order.ShoppingCartJson;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.model.util.DeliveryType;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * @author Santiago Garzon
 *
 *
 * @since 2022
 *
 * REST API para calcular el carrito.
 *
 * */

@Api(name = "priceDelivery",
        version = "v1",
        scopes = {Constants.EMAIL_SCOPE},
        clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID_DEVELOPER, com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID},
        audiences = {Constants.ANDROID_AUDIENCE},
        namespace = @ApiNamespace(ownerDomain = Constants.OWNER_DOMAIN, ownerName = Constants.OWNER_NAME),
        description = "cart calculation Endpoint")
public class PriceDelivery {

    private static final Logger LOG = Logger.getLogger(PriceDelivery.class.getName());

    @ApiMethod(name = "priceDeliveryOrder", path = "/v2/shoppingCart", httpMethod = ApiMethod.HttpMethod.POST)
    public DeliveryOrder priceDeliveryOrder(final ShoppingCartJson shoppingCartJson, HttpServletRequest request) throws BadRequestException, AlgoliaException, ConflictException, IOException {
        LOG.info("method: priceDeliveryOrder request->" + shoppingCartJson.toStringJson());
        Guard.againtsNull(shoppingCartJson, "shoppingCartJson" );
        Guard.againtsNullOrEmpty(shoppingCartJson.getToken(), "Token" );
        Guard.againtsNullOrEmpty(shoppingCartJson.getTokenIdWebSafe(), "TokenIdWebSafe" );
        Guard.againtsNullOrEmpty(shoppingCartJson.getIdCustomerWebSafe(), "IdCustomerWebSafe" );
        Guard.againtsZero(shoppingCartJson.getIdStoreGroup(), "IdStoreGroup" );
        Guard.againtsNullOrEmpty(shoppingCartJson.getSource(), "Source" );
        Guard.validateSourceAndDeliveryType(shoppingCartJson);
        if (Guard.isWebWithoutDeliveryType(shoppingCartJson)) {
            throw new BadRequestException("BadRequest [deliveryType] is required for web");
        }
        if (Guard.isDeliveryTypePresent(shoppingCartJson)){
            shoppingCartJson.setDeliveryType(DeliveryType.EXPRESS);
        }
        //Cambiar mas adelante
        OrderStoreService orderStoreService = new OrderStoreService();
        shoppingCartJson.setDeliveryType(orderStoreService.putStore26withExpress(shoppingCartJson));


        return Objects.requireNonNull(DeliveryTypeEnum.getDeliveryType(shoppingCartJson.getDeliveryType().name())).calculateCart().priceDeliveryOrder(shoppingCartJson, request);
    }
}
