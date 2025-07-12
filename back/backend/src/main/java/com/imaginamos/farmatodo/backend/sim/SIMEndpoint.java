package com.imaginamos.farmatodo.backend.sim;

import com.algolia.search.exceptions.AlgoliaException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.ConflictException;
import com.google.api.server.spi.response.InternalServerErrorException;
import com.google.gson.Gson;
import com.imaginamos.farmatodo.backend.order.create_order.infraestructure.BrazeClient;
import com.imaginamos.farmatodo.backend.order.create_order.infraestructure.SendOrderPush;
import com.imaginamos.farmatodo.backend.sim.domain.AmplitudeUtils;
import com.imaginamos.farmatodo.backend.sim.domain.SimEndpointGuard;
import com.imaginamos.farmatodo.backend.sim.infrastructure.AmplitudeClient;
import com.imaginamos.farmatodo.model.order.*;
import com.imaginamos.farmatodo.model.payment.BrazeStatusEnum;
import com.imaginamos.farmatodo.model.payment.OrderStatusEnum;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.model.util.DeliveryType;
import com.imaginamos.farmatodo.model.util.URLConnections;
import com.imaginamos.farmatodo.networking.algolia.APIAlgolia;
import com.imaginamos.farmatodo.networking.services.ApiGatewayService;
import com.imaginamos.farmatodo.networking.services.CoreService;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

@Api(name = "simEndpoint",
        version = "v1",
        scopes = {Constants.EMAIL_SCOPE},
        clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID_DEVELOPER, com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID},
        audiences = {Constants.ANDROID_AUDIENCE},
        namespace = @ApiNamespace(ownerDomain = Constants.OWNER_DOMAIN, ownerName = Constants.OWNER_NAME, packagePath = ""),
        description = "SIM endpoint.")
public class SIMEndpoint {

    private static final Logger LOG = Logger.getLogger(SIMEndpoint.class.getName());
    private static final int STATUS_CONFIG_MESSAGE_CODE_BILLED = 5;
    private static final int STATUS_CONFIG_MESSAGE_CODE_FINISH = 7;


    @ApiMethod(
            name = "ping",
            path = "/simEndpoint/ping",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CoreEventResponse ping() throws InternalServerErrorException {

//        LOG.info(" method: ping()");
        try {
            CoreEventResponse response = CoreService.get().pingSIM();
//            LOG.info("method: ping() -> Response: " + response.toString());
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
            name = "createFulfillmentOrderDetail",
            path = "/simEndpoint/fulfillment",
            httpMethod = ApiMethod.HttpMethod.POST)
    public CoreEventResponse createFulfillmentOrderDetail(
            final FulfilOrdColDescDomain fulfilOrdColDescDomain)
            throws BadRequestException, InternalServerErrorException {

        if (Objects.isNull(fulfilOrdColDescDomain)) {
            LOG.warning("method: createFulfillmentOrderDetail() --> BadRequest [fulfilOrdColDescDomain is null]");
            throw new BadRequestException("BadRequest [fulfilOrdColDescDomain is null]");
        }
        if (Objects.isNull(fulfilOrdColDescDomain.getFulfilOrdDesc()) || fulfilOrdColDescDomain.getFulfilOrdDesc().length == 0) {
            LOG.warning("method: createFulfillmentOrderDetail() --> BadRequest [fulfilOrdColDescDomain.fulfilOrdDesc is null or empty]");
            throw new BadRequestException("BadRequest [fulfilOrdColDescDomain.fulfilOrdDesc is null or empty]");
        }

//        LOG.info("method: createFulfillmentOrderDetail() --> CORE request: " + fulfilOrdColDescDomain.toStringJson());
        try {
            CoreEventResponse response = CoreService.get().createFulfillmentOrderDetail(fulfilOrdColDescDomain);
//            LOG.info("method: createFulfillmentOrderDetail() --> CORE response: " + response.toString());
            switch (response.getCode()) {
                case OK:
                    updateAlgoliaItemsStock(fulfilOrdColDescDomain.getFulfilOrdDesc());
                case BAD_REQUEST:
                case REJECTED:
                case DUPLICATED:
                case ERROR:
                    return response;
                default:
                    throw new InternalServerErrorException(response.getMessage());
            }
        } catch (Exception e) {
            LOG.warning("method: createFulfillmentOrderDetail() --> Error: " + e.fillInStackTrace());
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    @ApiMethod(
            name = "updateStock",
            path = "/simEndpoint/stockAlgolia",
            httpMethod = ApiMethod.HttpMethod.POST)
    public void stockAlgolia(final FulfilOrdColDescDomain fulfilOrdColDescDomain) throws BadRequestException, InternalServerErrorException {
        LOG.info("method: stockAlgolia() --> request: " + (Objects.isNull(fulfilOrdColDescDomain.toStringJson()) ? "Es null" : fulfilOrdColDescDomain.toStringJson()));
        SimEndpointGuard.validationDomain(fulfilOrdColDescDomain);
        //amplitude
        try {
            sendEventAmplitude(fulfilOrdColDescDomain);
            sendStatusBilled(fulfilOrdColDescDomain);
        }catch (Exception e){
            LOG.info("method: stockAlgolia(), sendEventAmplitude --> Error: " + e.getMessage());
        }
        try {
            updateAlgoliaItemsStock(fulfilOrdColDescDomain.getFulfilOrdDesc());
        } catch (Exception e) {
            LOG.warning("method: stockAlgolia(), updateAlgoliaItemsStock --> Error: " + e.fillInStackTrace());
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    private static void sendStatusBilled(FulfilOrdColDescDomain fulfilOrdColDescDomain) throws IOException, ConflictException{
        FulfilOrdColDescDomain.FulfilOrdDescDomain[] fulfilOrdDesc = fulfilOrdColDescDomain.getFulfilOrdDesc();
        if (fulfilOrdDesc == null){
            return;
        }

        FulfilOrdColDescDomain.FulfilOrdDescDomain order = Arrays.stream(fulfilOrdDesc).findAny().get();
        if(Objects.nonNull(order.getCustomerOrderNo())){
            Optional<String> uuidCourier = ApiGatewayService.get().getCourierUuid(Long.valueOf(order.getCustomerOrderNo()));
            DeliveryOrderStatus deliveryOrderStatus = new DeliveryOrderStatus();
            deliveryOrderStatus.setOrder_no(order.getCustomerOrderNo());
            deliveryOrderStatus.setStatus(OrderStatusEnum.FACTURADA.toString());
            deliveryOrderStatus.setUuid(uuidCourier.orElse(""));
            sendStatus(deliveryOrderStatus);
        }
    }

    private static void sendStatus(DeliveryOrderStatus deliveryOrderStatus) throws IOException, ConflictException {
        if(isValidateDeliveryOrderStatus(deliveryOrderStatus)){
            ApiGatewayService.get().orderStatusUpdate(deliveryOrderStatus);
        }
    }

    private static boolean isValidateDeliveryOrderStatus(DeliveryOrderStatus deliveryOrderStatus) {
        return Objects.nonNull(deliveryOrderStatus.getOrder_no()) &&
                Objects.nonNull(deliveryOrderStatus.getUuid()) &&
                !deliveryOrderStatus.getUuid().isEmpty() &&
                Objects.nonNull(deliveryOrderStatus.getStatus());
    }

    /**
     * update Algolia Items Stock
     *
     * @param fulfilOrdDesc
     * @throws AlgoliaException
     */
    private void updateAlgoliaItemsStock(FulfilOrdColDescDomain.FulfilOrdDescDomain[] fulfilOrdDesc) throws AlgoliaException {
        Optional<Boolean> optionalStockItemsUpdate = APIAlgolia.updateAlgoliaStock(fulfilOrdDesc);
        optionalStockItemsUpdate.ifPresent(stockUpdated -> LOG.info("method: optionalStockItemsUpdate() --> " + stockUpdated));
    }

    /**
     * @author Jhon Chaparro
     * send Event Amplitude
     * @param fulfilOrdColDescDomain
     */
    private void sendEventAmplitude(final FulfilOrdColDescDomain fulfilOrdColDescDomain) throws IOException {
        if (fulfilOrdColDescDomain == null){
            return;
        }
        FulfilOrdColDescDomain.FulfilOrdDescDomain[] fulfilOrdDesc = fulfilOrdColDescDomain.getFulfilOrdDesc();
        if (fulfilOrdDesc == null){
            return;
        }
        FulfilOrdColDescDomain.FulfilOrdDescDomain order = Arrays.stream(fulfilOrdDesc).findAny().get();
        String orderId = order.getCustomerOrderNo();

        OrderInfoDataAmplitudeBraze infoAmplitude = ApiGatewayService.get().getOrderInfoAmplitudeBraze(orderId);
        try {
            sendAmplitudEvent(infoAmplitude.getDeliveryType(), orderId);
        }catch (Exception e){
            LOG.info("method: sendEventAmplitude(), Envio evento amplitude --> Error: " + e.getMessage());
        }
        try {
            buildPushBraze(infoAmplitude.getEmailCustomer(), orderId);
        }catch (Exception e){
            LOG.info("method: sendEventAmplitude(), sendPushBraze --> Error: " + e.getMessage());
        }
    }

    private void buildPushBraze(String emailCustomer, String orderId) {
        try {
            DeliveryOrder deliveryOrder = AmplitudeUtils.getOrder(orderId);
            if(deliveryOrder != null) {
                DeliveryType deliveryType = deliveryOrder.getDeliveryType();
                sendPushBraze(emailCustomer, deliveryType, deliveryOrder);
            }
        } catch (Exception e) {
            LOG.warning("method: sendPushBraze() --> Error: " + e);
        }
    }

    private static void sendPushBraze(String emailCustomer, DeliveryType deliveryType, DeliveryOrder deliveryOrder) {
        if (!emailCustomer.isEmpty()) {
            int notificationCode = deliveryType == DeliveryType.EXPRESS ? STATUS_CONFIG_MESSAGE_CODE_BILLED : STATUS_CONFIG_MESSAGE_CODE_FINISH;
            String status = (deliveryType == DeliveryType.EXPRESS) ? BrazeStatusEnum.BILLED.getValue() : BrazeStatusEnum.FINISH.getValue();
            BrazeClient.sendPushNotifications(new SendOrderPush(emailCustomer, notificationCode, status, String.valueOf(deliveryOrder.getIdOrder()), null), String.valueOf(deliveryOrder.getIdFarmatodo()));
        }
    }

    private void sendAmplitudEvent(String deliveryType, String orderId) {
        try{
            if (AmplitudeUtils.isNotExpress(deliveryType)) {
                    ApiGatewayService.get().createEventOrderCompletedV2(orderId);
            }
        }catch (Exception e) {
            LOG.warning("method: sendAmplitudEvent() --> Error: " + e);
        }
    }


}



