package com.imaginamos.farmatodo.payments;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.ConflictException;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.datastore.Query;
import com.googlecode.objectify.Key;
import com.imaginamos.farmatodo.model.algolia.ChargeOrderAlgolia;
import com.imaginamos.farmatodo.model.algolia.NextPaymentAttemptProperties;
import com.imaginamos.farmatodo.model.order.CreatedOrder;
import com.imaginamos.farmatodo.model.order.OrderCharge;
import com.imaginamos.farmatodo.model.order.RequestSourceEnum;
import com.imaginamos.farmatodo.model.order.Tracing;
import com.imaginamos.farmatodo.model.payment.LogIntentPayment;
import com.imaginamos.farmatodo.model.payment.OrderChargeRes;
import com.imaginamos.farmatodo.model.payment.OrderStatusEnum;
import com.imaginamos.farmatodo.model.payment.ResponseStatusEnum;
import com.imaginamos.farmatodo.model.util.Answer;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.networking.algolia.APIAlgolia;
import com.imaginamos.farmatodo.networking.services.ApiGatewayService;
import org.apache.http.HttpStatus;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

import static com.imaginamos.farmatodo.backend.OfyService.ofy;
import static java.time.Duration.between;


@Api(name = "chargeEndpoint",
        version = "v1",
        scopes = {Constants.EMAIL_SCOPE},
        clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID_DEVELOPER, com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID},
        audiences = {Constants.ANDROID_AUDIENCE},
        namespace = @ApiNamespace(ownerDomain = Constants.OWNER_DOMAIN, ownerName = Constants.OWNER_NAME, packagePath = ""),
        description = "Stores order for all pages.")
public class ChargeEndpoint {

    private static final Logger LOG = Logger.getLogger(ChargeEndpoint.class.getName());

    @ApiMethod(name = "chargeOrder", path = "/chargeEndpoint/chargeOrder", httpMethod = ApiMethod.HttpMethod.PUT)
    public OrderChargeRes chargeOrder(final OrderCharge orderCharge) throws IOException {
        LOG.info("method: chargeOrder() -> " + orderCharge.toString());
        // Valida si ya se intento cobrar esa orden
        //ChargeOrderAlgolia chargeOrderAlgolia = new ChargeOrderAlgolia();
        Optional<ChargeOrderAlgolia> chargeOrderAlgolia = APIAlgolia.getChargeOrderActive();
        if (chargeOrderAlgolia.isPresent() && chargeOrderAlgolia.get().getActive() && !orderCharge.getSource().equals(RequestSourceEnum.COURIER)){
            return paymentAttempt(orderCharge);
        }else {
            LOG.warning("Alerta: Se encuentra deshabilitado el cobro por parte del courier" );
            return new OrderChargeRes(String.valueOf(HttpStatus.SC_CONFLICT), "En este momento no es posible realizar esta peticion.");
        }

    }

    @ApiMethod(name = "createLogTracing", path = "/chargeEndpoint/createLogTracing", httpMethod = ApiMethod.HttpMethod.POST)
    public Answer createTracing(final CreatedOrder orderFarmatodo) throws UnauthorizedException, BadRequestException, ConflictException {
        LOG.info("Request createLogTracing(): " + orderFarmatodo);
        LOG.warning("Request createLogTracing() Objects.isNull(orderFarmatodo.getTracing()): " + Objects.isNull(orderFarmatodo.getTracing()));
        LOG.warning("Request createLogTracing() Objects.nonNull(orderFarmatodo.getIdOrder()): " + Objects.nonNull(orderFarmatodo.getIdOrder()));
        if (Objects.isNull(orderFarmatodo.getTracing()) || Objects.isNull(orderFarmatodo.getIdOrder())) {
            throw new BadRequestException(Constants.TRACING_INITIALIZATION);
        }
        LOG.warning("OrderId: "+String.valueOf(orderFarmatodo.getIdOrder()));
        Tracing tracing = orderFarmatodo.getTracing().get(0);
        LOG.warning("UUID: "+String.valueOf(tracing.getUuid()));
        saveLogIntentPayment(orderFarmatodo.getIdOrder(), tracing.getUuid(), tracing.getStatusCode(), tracing.getComments());

        Answer answer = new Answer();
        answer.setConfirmation(true);
        return answer;
    }

    /**
     * Valida el estado de los cobros realizados
     * @param orderCharge
     * @return
     * @throws IOException
     */
    private OrderChargeRes paymentAttempt(final OrderCharge orderCharge) throws IOException {
        LOG.info("method: paymentAttempt() -> " + orderCharge.toString());
        // Consulta los itentos de pago de una orden:
        List<LogIntentPayment> logItentPaymentsList = getLogIntentPaymentList(orderCharge.getOrderId());
        NextPaymentAttemptProperties nextAttempt = APIAlgolia.getProperty(Constants.NEXT_PAYMENT_ATTEMPT, NextPaymentAttemptProperties.class);
        if(Objects.isNull(logItentPaymentsList) || (Objects.nonNull(logItentPaymentsList) && logItentPaymentsList.isEmpty())){
            // Registra intentp de cobro

            //OrderChargeRes response = sendOrderToBackend3(orderCharge);

            /// if (Objects.nonNull(response) && response.getCode() )

            //LOG.warning("method: paymentAttempt() -> Primer intento de cobro");
            saveLogIntentPayment(orderCharge.getOrderId(), orderCharge.getUuid());
            //LOG.warning("method: paymentAttempt() -> Generó Primer intento de cobro");
            return sendOrderToBackend3(orderCharge);
        }else if(Objects.nonNull(logItentPaymentsList) && !logItentPaymentsList.isEmpty()){
            final Optional<LogIntentPayment> logIntentPaymentOptional = logItentPaymentsList.stream().filter(logIntentPayment -> ResponseStatusEnum.PAYMENT_APROVED.toString().equals(logIntentPayment.getStatus())).findFirst();
            if(logIntentPaymentOptional.isPresent()){
                //LOG.warning("method: paymentAttempt() -> Order ya pagada en log Intent Payment - NO CORE");
                return getPaymentResponse();
            }else{
                // verifica si hay algun intento de cobro en el tiempo parametrizado
                final Optional<LogIntentPayment> lastLogIntentPaymentOptional =
                        logItentPaymentsList.stream().filter(logIntentPayment ->
                                between(LocalDateTime.ofInstant(Instant.ofEpochMilli(logIntentPayment.getCreatedDate()), ZoneId.systemDefault()),
                                        LocalDateTime.now()
                                ).getSeconds() < (nextAttempt.getMinutes()*60)).findAny();
                if(lastLogIntentPaymentOptional.isPresent()){
                    LOG.warning("method: paymentAttempt() -> Existe intento de Pago generado - NO CORE");
                    return getWaitForPaymentResponse(lastLogIntentPaymentOptional.get());
                }else{
                    // Registra intento de cobro
                    LOG.warning("method: paymentAttempt() -> Nuevo intento de COBRO - CORE");
                    saveLogIntentPayment(orderCharge.getOrderId(), orderCharge.getUuid());
                    return sendOrderToBackend3(orderCharge);
                }
            }
        }
        return getWaitForPaymentResponse(null);
    }

    private OrderChargeRes getPaymentResponse(){
        return new OrderChargeRes(ResponseStatusEnum.PAYMENT_APROVED.name(), "Esta orden ya ha sido cobrada.");
    }

    private OrderChargeRes getWaitForPaymentResponse(LogIntentPayment logIntent){
        if(Objects.nonNull(logIntent) && Objects.nonNull(logIntent.getStatus()) && Objects.nonNull(logIntent.getMessage())){
            return new OrderChargeRes(logIntent.getStatus(), logIntent.getMessage());
        }else {
            return new OrderChargeRes(String.valueOf(HttpStatus.SC_NOT_ACCEPTABLE), "Ya se realizo una solicitud de pago, por favor espere.");
        }
    }

    /*
    private OrderChargeRes sendOrderToCore(final OrderCharge orderCharge) throws IOException {
        return CoreService.get().putChargeOrder(orderCharge);
        //return new OrderChargeRes(String.valueOf(HttpStatus.SC_OK), "Orden enviada al CORE.");
    }*/

    private OrderChargeRes sendOrderToBackend3(final OrderCharge chargeOrder){
        try {
            LOG.info("method: Request sendOrderToBackend3(): " + chargeOrder);
            return ApiGatewayService.get().chargeOrder(chargeOrder);
        } catch (IOException e) {
            LOG.warning("method: sendOrderToBackend3():  Error: " + e.getMessage());
        }
        return new OrderChargeRes(String.valueOf(HttpStatus.SC_BAD_REQUEST), "Esperando respuesta del servicio");
    }

    private List<LogIntentPayment> getLogIntentPaymentList(Long orderId){
        Query.Filter filterOrderId = new Query.FilterPredicate("orderId", Query.FilterOperator.EQUAL, orderId);
        return ofy().load().type(LogIntentPayment.class).filter(filterOrderId).list();
    }

    private Key<LogIntentPayment> saveLogIntentPayment(Long orderId, String uuid){
        LogIntentPayment newIntentPayment =
                new LogIntentPayment(orderId, uuid, LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), OrderStatusEnum.EN_COLA_POR_PAGAR.toString());
        return ofy().save().entity(newIntentPayment).now();
    }

    private Key<LogIntentPayment> saveLogIntentPayment(Long orderId, String uuid, String statusCode, String message){
        LogIntentPayment newIntentPayment =
                new LogIntentPayment(orderId, uuid, LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), statusCode, message);
        return ofy().save().entity(newIntentPayment).now();
    }


}
