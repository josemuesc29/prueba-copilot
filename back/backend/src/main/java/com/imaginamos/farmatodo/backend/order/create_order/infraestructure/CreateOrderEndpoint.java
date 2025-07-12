package com.imaginamos.farmatodo.backend.order.create_order.infraestructure;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.ConflictException;
import com.google.gson.Gson;
import com.imaginamos.farmatodo.backend.order.create_order.domain.*;
import com.imaginamos.farmatodo.model.order.CreatedOrder;
import com.imaginamos.farmatodo.model.order.DeliveryOrder;
import com.imaginamos.farmatodo.model.order.DeliveryOrderItem;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.networking.cache.CachedDataManager;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Santiago Garzon
 * @author Jhon Puentes
 *
 *
 * @since 2022
 *
 * REST API para la creación de ordenes.
 *
 * */

@Api(name = "createOrderEndpoint",
        version = "v1",
        scopes = {Constants.EMAIL_SCOPE},
        clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID_DEVELOPER, com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID},
        audiences = {Constants.ANDROID_AUDIENCE},
        namespace = @ApiNamespace(ownerDomain = Constants.OWNER_DOMAIN, ownerName = Constants.OWNER_NAME),
        description = "Order Creation Endpoint")
public class CreateOrderEndpoint {

    @ApiMethod(name = "createOrder", path = "/v2/orders", httpMethod = ApiMethod.HttpMethod.POST)
    public CreatedOrder createOrder(
            @Named("idCustomerWebSafe") final String idCustomerWebSafe,
            @Named("token") final String token,
            @Named("tokenIdWebSafe") final String tokenIdWebSafe,
            final DeliveryOrder order,
            final HttpServletRequest httpServletRequest)
            throws BadRequestException, ConflictException {

        Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCacheIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
        if (jsonCachedOptional.isPresent()){
            throw new ConflictException("Tu solicitud ya se encuentra en proceso.");
        }
        CachedDataManager.saveJsonInCacheIndexTime(String.valueOf(order.getIdFarmatodo()), new Gson().toJson(order), Constants.ID_INDEX_SAVE_AND_GET_REDIS, Constants.TIME_SAVE_REDIS_AND_SECONDS);

        // Validations
        Guard.againstInvalidTokens(token, tokenIdWebSafe, order.getIdFarmatodo());
        final boolean isScanAndGo = OrderUtil.isScanAndGo(order);
        Guard.againstInvalidAddress(isScanAndGo, order.getIdAddress(), order.getIdFarmatodo());
        Guard.againstInvalidPaymentMethod(order);
        //Guard.againstBlockedOrFraudulentUser(order.getIdFarmatodo());

        // Validate if deliver order exists asn has items
        DeliveryOrder deliveryOrderSaved = OrderService.findActiveDeliveryOrderByidCustomerWebSafe(idCustomerWebSafe);
        Guard.againstNullDeliveryOrder(deliveryOrderSaved, order.getIdFarmatodo());
        List<DeliveryOrderItem> deliveryOrderItemList = OrderService.findItemsOf(deliveryOrderSaved);
        Guard.againstOrderWithoutItems(deliveryOrderItemList, order.getIdFarmatodo());

        // Create Order
        CommandCreateOrder command = new CommandCreateOrder(idCustomerWebSafe, token, tokenIdWebSafe, order, httpServletRequest);
        return Objects.requireNonNull(DeliveryTypeEnum.getDeliveryType(order.getDeliveryType().name())).getOrderCreator().create(command);
    }

}
