package com.imaginamos.farmatodo.networking.services;

import com.algolia.search.exceptions.AlgoliaException;
import com.google.api.server.spi.response.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.imaginamos.farmatodo.model.OptimalRoute.*;
import com.imaginamos.farmatodo.model.algolia.ItemAlgolia;
import com.imaginamos.farmatodo.model.algolia.PrimeConfig;
import com.imaginamos.farmatodo.model.algolia.RecommendResponse;
import com.imaginamos.farmatodo.model.braze.BrazeEventCreate;
import com.imaginamos.farmatodo.model.categories.Department;
import com.imaginamos.farmatodo.model.categories.Shortcut;
import com.imaginamos.farmatodo.model.city.CityJSON;
import com.imaginamos.farmatodo.model.coupon.CouponValidation;
import com.imaginamos.farmatodo.model.coupon.ValidFirstCouponRes;
import com.imaginamos.farmatodo.model.coupon.ValidFirstCouponResData;
import com.imaginamos.farmatodo.model.customer.SelfCheckout;
import com.imaginamos.farmatodo.model.customer.*;
import com.imaginamos.farmatodo.model.firebase.AddFirebaseCodeLoginRequest;
import com.imaginamos.farmatodo.model.firebase.FirebaseLoginCodeResponse;
import com.imaginamos.farmatodo.model.home.BannersDTFRes;
import com.imaginamos.farmatodo.model.item.ItemReq;
import com.imaginamos.farmatodo.model.item.ItemRes;
import com.imaginamos.farmatodo.model.microCharge.MicroCharge;
import com.imaginamos.farmatodo.model.offer.Offer;
import com.imaginamos.farmatodo.model.order.*;
import com.imaginamos.farmatodo.model.payment.OrderChargeRes;
import com.imaginamos.farmatodo.model.payment.PaymentMethodV2DTFRequest;
import com.imaginamos.farmatodo.model.payment.PaymentMethodV2FTDResponse;
import com.imaginamos.farmatodo.model.payment.ResponseStatusEnum;
import com.imaginamos.farmatodo.model.product.*;
import com.imaginamos.farmatodo.model.provider.ProviderCreate;
import com.imaginamos.farmatodo.model.provider.ProviderRes;
import com.imaginamos.farmatodo.model.store.Store;
import com.imaginamos.farmatodo.model.store.StoreJSON;
import com.imaginamos.farmatodo.model.talonone.CustomerSessionExternalRequest;
import com.imaginamos.farmatodo.model.talonone.DiscountTalon;
import com.imaginamos.farmatodo.model.user.GoogleAuth;
import com.imaginamos.farmatodo.model.util.Answer;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.model.util.HttpStatusCode;
import com.imaginamos.farmatodo.model.util.URLConnections;
import com.imaginamos.farmatodo.networking.algolia.APIAlgolia;
import com.imaginamos.farmatodo.networking.api.ApiGateway;
import com.imaginamos.farmatodo.networking.base.ApiBuilder;
import com.imaginamos.farmatodo.networking.models.addresses.*;
import com.imaginamos.farmatodo.networking.models.amplitude.*;
import com.imaginamos.farmatodo.networking.models.authentication.LoginFirebaseReq;
import com.imaginamos.farmatodo.networking.models.authentication.LoginFirebaseRes;
import com.imaginamos.farmatodo.networking.models.braze.*;
import com.imaginamos.farmatodo.networking.models.mail.SendBrazeEmailResp;
import com.imaginamos.farmatodo.networking.models.mail.SendMailReq;
import com.imaginamos.farmatodo.networking.talonone.model.TrackEventItemPurchasedRequest;
import com.imaginamos.farmatodo.networking.talonone.model.TrackEventRequest;
import com.imaginamos.farmatodo.networking.talonone.model.TrackEventResponse;
import com.imaginamos.farmatodo.networking.util.Util;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ApiGatewayService {

    private static final Long DOCUMENT_NUMBER_DEFAULT = null;
    private static ApiGatewayService instance;

    private ApiGateway apiGateway;

    static final int TIME_OUT_SECONDS = 35;

    private static final Logger LOG = Logger.getLogger(ApiGatewayService.class.getName());

    private ApiGatewayService() {
        apiGateway = ApiBuilder.get().createBackend30Service(ApiGateway.class);
    }

    public static ApiGatewayService get() {
        if (instance == null) instance = getSync();
        return instance;
    }

    private static synchronized ApiGatewayService getSync() {
        if (instance == null) instance = new ApiGatewayService();
        return instance;
    }

    public DiscountSASByCustomerResponse getDiscountSASByCustomer(final Long customer) throws IOException {
        LOG.info("getDiscountSASByCustomer(" + customer + ")");


        // params path

        Map<String, String> pathVariables = new HashMap();
        pathVariables.put("id", String.valueOf(customer));
        String urlWithParams = Util.buildUrl(URLConnections.URL_GET_DISCOUNT_BY_CUSTOMER_BC_30, pathVariables, null);


        Call<DiscountSASByCustomerResponse> call = apiGateway.getDiscountByCustomer(urlWithParams);
        Response<DiscountSASByCustomerResponse> res = call.execute();

        LOG.info("Response:" + res.toString());

        return res.isSuccessful() ? res.body() : null;
    }

    public Response<ValidateOrderBackend3> validateOrder(ValidateOrderReq validateOrderReq, String traceId) {
        try {
            return executeApipriceDeliveryOrder(validateOrderReq, traceId, 1);
        } catch (Exception e) {
            LOG.warning("Fallo el primer intento del servicio de shopping-cart se intentará por failOver: " + e.getMessage());
            // Realiza una segunda ejecución en caso de fallo
            try {
                return executeApipriceDeliveryOrder(validateOrderReq, traceId, 2);
            } catch (Exception ex) {
                LOG.warning("Fallo el segundo intento del servicio de shopping-cart se intentará por failOver: " + e.getMessage());
                //return Response.error(500, ResponseBody.create(null, ""));
                throw new RuntimeException(ex);
            }
        }
    }

    private Response<ValidateOrderBackend3> executeApipriceDeliveryOrder(ValidateOrderReq validateOrderReq, String traceId, int inte) {
        LOG.info("Request Shopping-cart ->>>>> " + new Gson().toJson(validateOrderReq));

        try {
            Response<ValidateOrderBackend3> response = apiGateway.priceDeliveryOrder(URLConnections.URL_SHOPPING_PRICE_DELIVERY_ORDER, validateOrderReq, traceId).execute();
            LOG.info("Response shopping-cart ->" + (response.body() != null ? response.body().toString() : null));

            if(!response.isSuccessful() && response.errorBody() != null) {
                String error = response.errorBody().string();
                LOG.warning("Error body response Shopping-cart : " + error);
                throw new ServiceUnavailableException("Shopping-cart Service Unavailable");
            }
            return response;
        } catch (IOException e) {
            LOG.warning("Error en intento " + inte + ": " + e.getMessage());
            throw new RuntimeException(e);
        } catch (ServiceUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    public Response<ValidateOrderBackend3> validateOrderV2(ValidateOrderReq validateOrderReq, String traceId) throws IOException {
        LOG.info("Request Shopping-cart failOver->>>>> " + new Gson().toJson(validateOrderReq));

        try {
            Call<ValidateOrderBackend3> call = apiGateway.priceDeliveryOrder(URLConnections.URL_SHOPPING_PRICE_DELIVERY_ORDER_V2 + "testQA", validateOrderReq, traceId);
            Response<ValidateOrderBackend3> response = call.execute();
            LOG.info("Response shopping-cart-failOver ->" + (response.body() != null ? response.body().toString() : null));

            if(!response.isSuccessful() && response.errorBody() != null) {
                String error = response.errorBody().string();
                LOG.warning("Error body response Shopping-cart-failOver : " + error);
                throw new ServiceUnavailableException("Shopping-cart-failOver Service Unavailable");
            }

            return response;
        } catch (IOException e) {
            LOG.warning("IOException shopping-cart-failOver: " + e.getMessage());
            return Response.error(500, ResponseBody.create(null, ""));
        } catch (ServiceUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    public Response<CreateOrderResponseCore> createOrder(CreateOrderRequestCore requestCore) throws IOException, ServiceUnavailableException, BadRequestException, ConflictException, InternalServerErrorException, NotFoundException, UnauthorizedException {
        LOG.info("method createOrder OMS -> ");
        Call<CreateOrderResponseCore> call = apiGateway.createOrder(URLConnections.URL_OMS_CREATE_ORDER, requestCore);
        //LOG.info("URL -> " + URLConnections.URL_OMS_CREATE_ORDER);
        Response<CreateOrderResponseCore> response = call.execute();
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        LOG.info("Request to OMS ->" + gson.toJson(requestCore));
        //LOG.info("Response OMS Code->" + response.code());
        LOG.info("Response OMS ->" + (response.body() != null ? response.body().toString() : null));
        // reintentar peticion en caso de falla.
        if (!response.isSuccessful()) {
            String error = (response.errorBody() != null ? response.errorBody().string() : "code : " + response.code());
            LOG.warning("Alerta!! no se pudo crear la orden -> " + error);
            //LOG.info("Error al crear la orden, se procede a reintentar");
            if (!error.contains(URLConnections.MESSAGE_DECLINED_FOR_ANTIFRAUD)
                    && !error.contains(URLConnections.MESSAGE_DECLINED_FOR_FOUNDS)) {
                response = call.clone().execute();
                //LOG.info("Second attempt request to OMS ->" + gson.toJson(requestCore));
                //LOG.info("Second attempt Response OMS Code->" + response.code());
                LOG.info("Second attempt Response OMS ->" + (response.body() != null ? response.body().toString() : null));
            }
        }
        return response;
    }

    public Response<CreateOrderResponseBackend3> createOrderBck3(CreateOrderRequestCore requestCore, String traceId) throws IOException, ServiceUnavailableException, BadRequestException, ConflictException, InternalServerErrorException, NotFoundException, UnauthorizedException {
        Call<CreateOrderResponseBackend3> call = apiGateway.createOrderBck3(URLConnections.URL_OMS_CREATE_ORDER, requestCore, traceId);
        Response<CreateOrderResponseBackend3> response = call.execute();
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();

        LOG.info("Request to OMS ->" + gson.toJson(requestCore));
        LOG.info("Response OMS ->" + (response.body() != null ? response.body().toString() : null));
        LOG.info("Response OMS HTTP STATUS_CODE ->" + response.code());

        if (!response.isSuccessful()){
            String errorMessage = "";
            if (response.errorBody() != null) {
                errorMessage = response.errorBody().string();
                LOG.warning("Error response from OMS: " + errorMessage);
                Response<CreateOrderResponseBackend3> errorResponseCreateOrder = handleErrorResponseCreateOrder(errorMessage);
                if (errorResponseCreateOrder != null) return errorResponseCreateOrder;
            }
            throw new ServiceUnavailableException("OMS Service Unavailable");
        }

        return response;
    }

    @Nullable
    private Response<CreateOrderResponseBackend3> handleErrorResponseCreateOrder(String errorMessage) {
        BadRequestResponseOms badRequest = getBadRequestResponseOms(errorMessage);
        if(notApplyRetry(badRequest)){
            return Response.error(HttpStatusCode.BAD_REQUEST.getCode(),
                    ResponseBody.create(null, errorMessage));
        }
        return null;
    }

    private boolean notApplyRetry(BadRequestResponseOms badRequestResponseOms) {
        return isPaymentDeclined(badRequestResponseOms);
    }

    private boolean isPaymentDeclined(BadRequestResponseOms badRequestResponseOms) {
        if (badRequestResponseOms == null) {
            return false;
        }
        try {
            LOG.info("isPaymentDeclined.badRequestResponseOms: " + badRequestResponseOms);
            String PAYMENT_DECLINED = "PAYMENT_DECLINED";
            return PAYMENT_DECLINED.equalsIgnoreCase(badRequestResponseOms.getCode());
        } catch (Exception e) {
            LOG.info("Error converting response to Gson in OrderEndpoint.isPaymentDeclined: " + e.getMessage());
        }
        return false;
    }

    private BadRequestResponseOms getBadRequestResponseOms(String errorBody) {
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(errorBody, BadRequestResponseOms.class);
    }

    public Response<CreateOrderResponseBackend3> createOrderBck3V2(CreateOrderRequestCore requestCore, String traceId) throws IOException, ServiceUnavailableException, BadRequestException, ConflictException, InternalServerErrorException, NotFoundException, UnauthorizedException {
        Call<CreateOrderResponseBackend3> call = apiGateway.createOrderBck3(URLConnections.URL_OMS_CREATE_ORDER_V2, requestCore, traceId);
        Response<CreateOrderResponseBackend3> response = call.execute();
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();

        LOG.info("Request to failover OMS ->" + gson.toJson(requestCore));
        LOG.info("Response failover OMS ->" + (response.body() != null ? response.body().toString() : null));
        LOG.info("Response failover OMS HTTP STATUS_CODE ->" + response.code());

        return response;
    }

    public Response <GenericResponse> validateCouponOms(CouponValidation requestCore) throws IOException, ServiceUnavailableException, BadRequestException, ConflictException, InternalServerErrorException, NotFoundException, UnauthorizedException {
        LOG.info("method validateCoupon OMS -> ");
        Call<GenericResponse> call = apiGateway.validateCoupon(URLConnections.URL_OMS_VALIDATE_COUPON, requestCore);
        //LOG.info("URL -> " + URLConnections.URL_OMS_VALIDATE_COUPON);
        Response <GenericResponse> response = call.execute();
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        LOG.info("Request to OMS ->" + gson.toJson(requestCore));
        return response;

    }


    public Response<CreateOrderSubscribeResponse> createOrderPASBck3(CreateOrderSubscribeReq orderSubscribeReq) throws IOException, ServiceUnavailableException, BadRequestException, ConflictException, InternalServerErrorException, NotFoundException, UnauthorizedException {
        LOG.info("method createOrder OMS -> {}"+ orderSubscribeReq.toString());
        Call<CreateOrderSubscribeResponse> call = apiGateway.createOrderPAS(URLConnections.URL_OMS_CREATE_ORDER, orderSubscribeReq);
        //LOG.info("URL -> " + URLConnections.URL_OMS_CREATE_ORDER);
        Response<CreateOrderSubscribeResponse> response = call.execute();
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        LOG.info("Request to OMS ->" + gson.toJson(orderSubscribeReq));
        //LOG.info("Response OMS Code->" + response.code());
        LOG.info("Response OMS ->" + (response.body() != null ? response.body().toString() : null));
        /*** // reintentar peticion en caso de falla.
        if (!response.isSuccessful()) {
            String error = (response.errorBody() != null ? response.errorBody().string() : "code : " + response.code());
            LOG.info("Alerta!! no se pudo crear la orden -> " + error);
            LOG.info("Error al crear la orden, se procede a reintentar");
            if (!error.contains(URLConnections.MESSAGE_DECLINED_FOR_ANTIFRAUD)
                    && !error.contains(URLConnections.MESSAGE_DECLINED_FOR_FOUNDS)) {
                response = call.clone().execute();
                LOG.info("Second attempt request to OMS ->" + gson.toJson(orderSubscribeReq));
                LOG.info("Second attempt Response OMS Code->" + response.code());
                LOG.info("Second attempt Response OMS ->" + (response.body() != null ? response.body().toString() : null));
            }
        }*/
        return response;
    }

    public OrderEditRes putOrdenEditBck3(OrderEdit orderEditBck3) throws IOException {
        OrderEditRes res;
        Call<OrderEditRes> call = apiGateway.putEditOrder(URLConnections.URL_OMS_EDIT_ORDERS, orderEditBck3);
        Response<OrderEditRes> response = call.execute();
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        LOG.info("Request to OMS ->" + gson.toJson(orderEditBck3));
        res = response.body();
        LOG.info("Response to OMS ->" + gson.toJson(res));
        return res;
    }

    public OrderCourier putOrderCourierEdit(OrderEdit orderEdit) throws IOException {
        OrderCourier res;
        Call<OrderCourier> call = apiGateway.putEditOrderCourier(URLConnections.URL_EDIT_ORDERS_COURIER,orderEdit);
        Response<OrderCourier> response = call.execute();
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        LOG.info("Request to OMS ->" + gson.toJson(orderEdit));
        res = response.body();
        LOG.info("Response OMS ->" + gson.toJson(res));
        return  res;
    }


    public OptimalRouteCheckoutOmsRes getOptimalRouteInCheckoutOms(OptimalRouteCheckoutOmsReq requestOms, String traceId) throws IOException {

        LOG.info("Call getOptimalRouteInCheckoutOms");

        Call<OptimalRouteCheckoutOmsRes> call = apiGateway.getOptimalRouteInCheckoutOms(URLConnections.URL_OMS_OPTIMAL_ROUTE_CHECKOUT, requestOms, traceId);
        Response<OptimalRouteCheckoutOmsRes> response = call.execute();
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        LOG.info("Request ->" + gson.toJson(requestOms));
        //LOG.info("Response Code->" + response.code());
        LOG.info("Response ->" + (response.body() != null ? response.body().toString() : null));
        return response.isSuccessful() ? response.body() : new OptimalRouteCheckoutOmsRes();
    }


    public OptimalRoutePopUpResponseData getOptimalRoutePopUp(
            OptimalRoutePopUpRequestDomain request,
            String traceId) throws IOException {

        LOG.info("Call getOptimalRoutePopUp URL: " +
                URLConnections.URL_ROUTER_OPTIMAL_ROUTE_POP_UP);

        Call<OptimalRoutePopUpResponseDomain> call =
                apiGateway.getOptimalRoutePopUp(
                        URLConnections.URL_ROUTER_OPTIMAL_ROUTE_POP_UP,
                        request, traceId);
        Response<OptimalRoutePopUpResponseDomain> response = call.execute();
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        return response.isSuccessful() ?
                response.body().getData() : new OptimalRoutePopUpResponseData();
    }


    public CustomerAddressResponse getCustomerByAddressId(long idAddress) throws IOException {
        LOG.info("method getCustomerByAddressId");

        CustomerAddressResponse res;

        // set params

        Map<String, String> pathVariables = new HashMap<>();
        pathVariables.put("idAddress", String.valueOf(idAddress));
        String urlWithParams = Util.buildUrl(URLConnections.URL_CRM_GET_CUSTOMER_BY_ADDRESS, pathVariables, null);

        Call<CustomerAddressResponse> call = apiGateway.getCustomerByAddressId(urlWithParams);
        Response<CustomerAddressResponse> response = call.execute();
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        LOG.info("Request to CRM ->" + gson.toJson(urlWithParams));
        res = response.body();
        LOG.info("Response CRM ->" + gson.toJson(res));

        return response.isSuccessful() ? response.body() : new CustomerAddressResponse();
    }

    public CustomerAddressResponse deleteCustomerByAddressId(long idAddress) throws IOException {
        LOG.info("method deleteCustomerByAddressId");
        // set params
        Map<String, String> pathVariables = new HashMap<>();
        pathVariables.put("idAddress", String.valueOf(idAddress));
        String urlWithParams = Util.buildUrl(URLConnections.URL_CRM_DELETE_CUSTOMER_ADDRESS_BY_ID, pathVariables, null);
        Call<CustomerAddressResponse> call = apiGateway.deleteCustomerAddressById(urlWithParams);
        Response<CustomerAddressResponse> response = call.execute();
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        LOG.info("Request to CRM ->" + gson.toJson(urlWithParams));
        LOG.info("Response CRM ->" + gson.toJson(response.body()));
        return response.isSuccessful() ? response.body() : new CustomerAddressResponse();
    }

    /**
     * Obtener el numero de paradas de una orden.
     *
     * @param orderId
     * @return Response
     */
    public Response<GetOrderStopsResponse> getOrderStops(final Long orderId) throws IOException {
        final String url = URLConnections.URL_OMS_GET_ORDER_STOPS.replace("{orderId}", String.valueOf(orderId));
        final Call<GetOrderStopsResponse> call = apiGateway.getOrderStops(url);
        final Response<GetOrderStopsResponse> response = call.execute();
        return response;
    }

    /**
     * Obtener items no facturados de una orden.
     *
     * @param orderId
     * @return Response
     */
    public Response<GetUnbilledItemsByOrderResponse> getUnbilledItemsByOrder(final Long orderId) throws IOException {
        final String url = URLConnections.URL_OMS_GET_UNBILLED_ITEMS_BY_ORDER.replace("{orderId}", String.valueOf(orderId));
        final Call<GetUnbilledItemsByOrderResponse> call = apiGateway.getUnbilledItemsByOrder(url);
        final Response<GetUnbilledItemsByOrderResponse> response = call.execute();
        return response;
    }


    public OrderChargeRes chargeOrder(final OrderCharge chargeOrder) throws IOException {
        LOG.info("Call chargeOrder " + URLConnections.URL_OMS_CHARGE_ORDER);
        Call<OrderChargeRes> call = apiGateway.chargeOrderOms(URLConnections.URL_OMS_CHARGE_ORDER, chargeOrder);
        Response<OrderChargeRes> response = call.execute();
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        LOG.info("Request ->" + gson.toJson(chargeOrder));
   
        if (response.isSuccessful()) {
            //LOG.info("Request ->" + gson.toJson(chargeOrder));
            //LOG.info("Response Code->" + response.code());
            LOG.info("Response ->" + (Objects.nonNull(response.body()) ?
                    response.body().toString() :
                    (Objects.nonNull(response.errorBody()) ? response.errorBody().string() : null)));

            return new OrderChargeRes(response.body() != null ? response.body().getCode() : "NO_CODE", (Objects.nonNull(response.body()) ?
                    response.body().getMessage() :
                    (Objects.nonNull(response.errorBody()) ? response.errorBody().string() : null)));

        } else {
            //LOG.info("Request Error ->" + gson.toJson(chargeOrder));
            //LOG.info("Response Code Error ->" + response.code());
            LOG.info("Response Data ->" + (Objects.nonNull(response.errorBody()) ?
                    response.errorBody().toString() :
                    (Objects.nonNull(response.errorBody()) ? response.errorBody().string() : null)));

            OrderChargeRes errorResponse = null;
            if (response.errorBody() != null) {
                errorResponse = gson.fromJson(response.errorBody().string(), OrderChargeRes.class);
            }
            //LOG.info("Response Code ->" + (Objects.nonNull(errorResponse) ? errorResponse.getCode() : "Sin código"));
            LOG.info("Response Code Message ->" + (Objects.nonNull(errorResponse) ? errorResponse.getMessage() : "Sin mensaje"));
            return new OrderChargeRes(ResponseStatusEnum.BAD_REQUEST.name(), (Objects.nonNull(errorResponse) ? errorResponse.getMessage() : "Sin mensaje"));
        }
    }

    public List<CustomerPhotoData> getCustomerPhotos(int idCustomer) throws IOException {
        //LOG.info("Call getCustomerPhoto");
        List<CustomerPhotoData> photoDataList = new ArrayList<>();
        try {
            final String url = URLConnections.URL_CRM_GET_CUSTOMER_PHOTOS.replace("{customerId}", String.valueOf(idCustomer));
            Call<CustomerPhotoDataResponse> call = apiGateway.getCustomerPhotos(url);
            Response<CustomerPhotoDataResponse> response = call.execute();
            return response.isSuccessful() && response.body() != null && response.body().getData() != null ? response.body().getData().getPhotos() : photoDataList;
        } catch (IOException e) {
            LOG.warning("warn error geting customer photos for customer #" + idCustomer);
            return photoDataList;
        }
    }


    /**
     * Obtener las coordenadas de la direccion donde de debe entregar la orden
     * y las coordenadas de las tiendas donde es necesario recoger los productos.
     *
     * @param orderId
     * @return Response
     */
    public GetCoodinatesCustomerAndAddressByOrderResponseData getCustomerAndStoresCoordinatesByOrder(final Long orderId) throws IOException {
        final String url = URLConnections.URL_OMS_GET_ORDER_COORDINATES.replace("{orderId}", String.valueOf(orderId));
        final Call<GetCustomerAndStoresCoordinatesByOrderResponse> call = apiGateway.getOrderCoordinatesByOrder(url);
        final Response<GetCustomerAndStoresCoordinatesByOrderResponse> response = call.execute();
        return response.isSuccessful() ? response.body().getData() : null;
    }

    /**
     * Obtener si el carrito dado aplica o no para envio gratis segun las campanbias.
     * @param cart
     * @return true si aplica, false en caso contrario.
     * */
    public Boolean validateFreeDeliveryByCart(final FreeDeliverySimpleCart cart) throws IOException {
        // Crea un executor con un solo hilo para la ejecución programada
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        try {
            return executeApipriceDeliveryOrderr(cart, 1, executorService);
        } catch (Exception e) {
            // Realiza una segunda ejecución en caso de timeout
            try {
                return executeApipriceDeliveryOrderr(cart, 2, executorService);
            } catch (Exception ex) {
               return false;
            }
        } finally {
            executorService.shutdown();
        }
    }

    private Boolean executeApipriceDeliveryOrderr(final FreeDeliverySimpleCart cart, int inte, ScheduledExecutorService executorService) {
        CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
            try {
                final String url = URLConnections.URL_OMS_VALIDATE_FREE_DELIVERY;
                final Call<ValidateFreeDeliveryByCartResponse> call = apiGateway.validateFreeDeliveryByCart(url, cart);
                final Response<ValidateFreeDeliveryByCartResponse> response = call.execute();

                if (response.isSuccessful() && response.body() != null) {
                    return response.body().getData();
                }
                return false;
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, executorService);

        try {
            // Establece un límite de tiempo para la ejecución del CompletableFuture
            return future.get(TIME_OUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOG.warning("TimeoutException validateFreeDeliveryByCart: " + inte +" " + e.getMessage());
            throw new CompletionException(e);
        }
    }

    public Boolean saveRelationOrdersPrime(OrderRelationPrimeRequest orderRelationPrimeRequest) throws IOException {
        final String url = URLConnections.URL_OMS_RELATION_ORDER_PRIME;
        final Call<OrderRelationPrimeResponse> call = apiGateway.saveRelationOrderPrime(url, orderRelationPrimeRequest);
        final Response<OrderRelationPrimeResponse> response = call.execute();
        if(response.isSuccessful() && response.body() != null){
            return true;
        }
        return false;
    }

    public CustomerResponse getCustomerByEmail(String email) throws IOException {
        LOG.info("method getCustomerByEmail");
        Map<String, String> pathVariables = new HashMap<>();
        pathVariables.put("email", String.valueOf(email));
        String urlWithParams = Util.buildUrl(URLConnections.URL_CRM_GET_CUSTOMER_BY_EMAIL, pathVariables, null);

        Call<GetCustomerResponse<CustomerResponse>> call = apiGateway.getCustomerByEmail(urlWithParams);
        Response<GetCustomerResponse<CustomerResponse>> response = call.execute();
        LOG.info("Request to CRM ->" + urlWithParams);
        LOG.info("Response CRM ->" + (Objects.nonNull(response.body()) ? response.body().getData() : null));
        return response.isSuccessful() && Objects.nonNull(response.body()) ? response.body().getData() : new CustomerResponse();
    }

    public ValidateCustomerEmail getCustomerByEmailLowerCase(String email) throws IOException {
        //LOG.info("method getCustomerByEmailLowerCase");
        Map<String, String> pathVariables = new HashMap<>();
        pathVariables.put("email", String.valueOf(email));
        String urlWithParams = Util.buildUrl(URLConnections.URL_CRM_GET_CUSTOMER_BY_EMAIL_LOWER_CASE, pathVariables, null);

        Call<GetCustomerResponse<ValidateCustomerEmail>> call = apiGateway.getCustomerByEmailLowerCase(urlWithParams);
        Response<GetCustomerResponse<ValidateCustomerEmail>> response = call.execute();
        return response.isSuccessful() && Objects.nonNull(response.body()) ? response.body().getData() : null;
    }

    public CustomerResponse getCustomerResetPassword(String email) throws Exception {
        //LOG.info("method getCustomerResetPassword");
        final Call<CustomerResetPasswordRes> call = apiGateway.customerResetPassword(URLConnections.URL_CRM_RESET_PASSWORD, new CustomerResetPasswordReq(email));
        final Response<CustomerResetPasswordRes> response = call.execute();
        //LOG.info("Request OMS -> "+ URLConnections.URL_CRM_RESET_PASSWORD+" email :"+ email);
        //LOG.info("Response OMS ->"+ response.raw());
        if (!response.isSuccessful()) {
            if (Objects.nonNull(response)) {
                if (Objects.nonNull(response.errorBody())) {
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    Gson gson = gsonBuilder.create();
                    OrderChargeRes errorResponse = gson.fromJson(response.errorBody().string(), OrderChargeRes.class);
                    LOG.warning("Message: " + errorResponse.getMessage());
                    throw new Exception(errorResponse.getMessage());
                } else {
                    throw new Exception("Error general.");
                }
            }
        }
        return response.isSuccessful() ? response.body().getData() : null;
    }

    public CustomerResponse getCustomerChangePassword(final CustomerResetPasswordReq request) throws ConflictException, IOException {
        //LOG.info("method getCustomerChangePassword");
        final Call<CustomerResetPasswordRes> call = apiGateway.customerChangePassword(URLConnections.URL_CRM_PASSWORD_CHANGE, request);
        final Response<CustomerResetPasswordRes> response = call.execute();
        //LOG.info("Request OMS -> "+ URLConnections.URL_CRM_RESET_PASSWORD+" email :"+ email);
        //LOG.info("Response OMS ->"+ response.raw());
        if (!response.isSuccessful()) {
            if (Objects.nonNull(response)) {
                if (Objects.nonNull(response.errorBody())) {
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    Gson gson = gsonBuilder.create();
                    OrderChargeRes errorResponse = gson.fromJson(response.errorBody().string(), OrderChargeRes.class);
                    LOG.warning("Message: " + errorResponse.getMessage());
                    throw new ConflictException(errorResponse.getMessage());
                } else {
                    throw new ConflictException("Error general.");
                }
            }
        }
        return response.isSuccessful() ? response.body().getData() : null;
    }

    public CustomerResponse getCustomerChangePasswordV2(final CustomerResetPasswordReq request) throws ConflictException, IOException {
        //LOG.info("method getCustomerChangePasswordV2");
        final Call<CustomerResetPasswordRes> call = apiGateway.customerChangePassword(URLConnections.URL_CRM_PASSWORD_CHANGEV2, request);
        final Response<CustomerResetPasswordRes> response = call.execute();
        if(!response.isSuccessful()){
            if(Objects.nonNull(response)){
                if(Objects.nonNull(response.errorBody())) {
                    try {
                        GsonBuilder gsonBuilder = new GsonBuilder();
                        Gson gson = gsonBuilder.create();
                        OrderChargeRes errorResponse = gson.fromJson(response.errorBody().string(), OrderChargeRes.class);
                        LOG.warning("Message: "+errorResponse.getMessage());
                        throw new ConflictException(errorResponse.getMessage());
                    } catch (Exception e) {
                        LOG.warning("Error@getCustomerChangePasswordV2: " + e.getMessage());
                        throw new ConflictException("Error general.");
                    }
                }else {
                    throw new ConflictException("Error general.");
                }
            }
        }
        return response.isSuccessful() ? response.body().getData() : null;
    }

    public CustomerDataResponse createCustomer(Customer customer, String traceId) throws Exception {
        CustomerRequest request = buildCustomerCallCenterReq(customer);
        LOG.info("Request -> {" + request + "}");
        //LOG.info("URL Request: " + URLConnections.URL_CRM_CREATE_CUSTOMER);
        final Call<CustomerBackend3> call = apiGateway.createCustomerCallCenter(URLConnections.URL_CRM_CREATE_CUSTOMER, request, traceId);
        final Response<CustomerBackend3> response = call.execute();
//        LOG.info("Response -> {"+ response.body().getData()+"}");

        if (!response.isSuccessful()) {
            LOG.warning("No fue satisfactoria la respuesta de backend3");
            if (Objects.nonNull(response.errorBody())) {
                try {
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    Gson gson = gsonBuilder.create();
                    OrderChargeRes errorResponse = gson.fromJson(response.errorBody().string(), OrderChargeRes.class);
                    LOG.warning("Message: " + errorResponse.getMessage());
                    throw new ConflictException(errorResponse.getMessage());
                } catch (Exception e) {
                    LOG.warning("Error@createCustomerApiGateway: " + e.getMessage());
                    throw new ConflictException("Error general.");
                }
            }
        }
        return response.isSuccessful() && response.body() != null ? response.body().getData() : new CustomerDataResponse();
    }

    public CustomerJSON updateCustomer(final CustomerJSON customer) throws ConflictException, IOException {
        CustomerRequest request = buildCustomerCallCenterReq(customer);
        return updateCustomer(request);
    }

    public CustomerJSON updateCustomer(final Customer customer) throws ConflictException, IOException {
        CustomerRequest request = buildCustomerCallCenterReq(customer);
        return updateCustomer(request);
    }

    private CustomerJSON updateCustomer(CustomerRequest request) throws IOException, ConflictException {
        LOG.info("Request -> {" + request + "}");
        //LOG.info("URL Request: " + URLConnections.URL_CRM_PUT_CUSTOMER);
        final Call<CustomerRes> call = apiGateway.updateCustomer(URLConnections.URL_CRM_PUT_CUSTOMER, request);
        final Response<CustomerRes> response = call.execute();
        //LOG.info("Response code -> " + response.code());
        LOG.info("Response -> " + response.body());
        if (!response.isSuccessful()) {
            LOG.warning("No fue satisfactoria la respuesta de backend3");
            if (Objects.nonNull(response.errorBody())) {
                try {
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    Gson gson = gsonBuilder.create();
                    OrderChargeRes errorResponse = gson.fromJson(response.errorBody().string(), OrderChargeRes.class);
                    LOG.warning("Message: " + errorResponse.getMessage());
                    throw new ConflictException(errorResponse.getMessage());
                } catch (Exception e) {
                    LOG.warning("Error@updateCustomerApiGateway: " + e.getMessage());
                    throw new ConflictException("Error general.");
                }
            }
        }
        return response.isSuccessful() ? response.body().getData() : new CustomerJSON();
    }

    public CustomerJSON createBasicCustomerV3(Customer customer, String traceId) throws Exception {
        CustomerRequest request = buildCustomerCallCenterReq(customer);
        LOG.info("Request -> {" + request + "}");
        //LOG.info("URL Request: " + URLConnections.URL_CRM_CREATE_BASIC_CUSTOMER);
        final Call<CustomerRes> call = apiGateway.createBasicCustomer(URLConnections.URL_CRM_CREATE_BASIC_CUSTOMER, request, traceId);
        final Response<CustomerRes> response = call.execute();
//        LOG.info("Response -> {"+ response.body().getData()+"}");

        if (!response.isSuccessful()) {
            LOG.warning("No fue satisfactoria la respuesta de backend3");
            if (Objects.nonNull(response)) {
                GsonBuilder gsonBuilder = new GsonBuilder();
                Gson gson = gsonBuilder.create();
                OrderChargeRes errorResponse = gson.fromJson(response.errorBody().string(), OrderChargeRes.class);
                LOG.warning("Message: " + errorResponse.getMessage());
                throw new ConflictException(errorResponse.getMessage());
            }
        }
        return response.isSuccessful() ? response.body().getData() : new CustomerJSON();
    }

    public CustomerJSON getCustomerByDocument(final String document) throws IOException, ConflictException {
        Map<String, String> pathVariables = new HashMap<>();
        pathVariables.put("documentNumber", document);
        pathVariables.put("countryId", "CO");
        String urlWithParams = Util.buildUrl(URLConnections.URL_CRM_GET_CUSTOMER_BY_DOCUMENT, pathVariables, null);
        //LOG.info("method getCustomerByDocument URL Request: " + urlWithParams);
        Call<GetCustomerResponse<CustomerJSON>> call = apiGateway.getCustomer(urlWithParams);
        final Response<GetCustomerResponse<CustomerJSON>> response = call.execute();
        if (!response.isSuccessful()) {
            LOG.warning("No fue satisfactoria la respuesta de backend3");
            if (Objects.nonNull(response)) {
                GsonBuilder gsonBuilder = new GsonBuilder();
                Gson gson = gsonBuilder.create();
                OrderChargeRes errorResponse = gson.fromJson(response.errorBody().string(), OrderChargeRes.class);
                LOG.warning("Message: " + errorResponse.getMessage());
                throw new ConflictException(errorResponse.getMessage());
            }
        }
        return response.isSuccessful() ? response.body().getData() : new CustomerJSON();
    }


    public GetOrderSumary getOrderSumary(final Long orderId) throws IOException {
        //LOG.info("method: getOrderSumary");
        final String url = URLConnections.URL_OMS_GET_ORDER_SUMMARY_BY_ORDER_ID.replace("{orderId}", String.valueOf(orderId));
        Call<GetOrderSumary> call = apiGateway.getSummaryByOrderId(url);
        Response<GetOrderSumary> response = call.execute();
        try {
            LOG.info("Request OMS ->" + url);
            //LOG.info("Response OMS successful ->" + response.isSuccessful());
            //if (response.body() != null)
              //  LOG.info("Response OMS ->" + response.body().getData().getInvoiceValue());
        }catch(Exception e){
            LOG.warning("Ocurrio algo imprimendo la data");
        }
        return response.isSuccessful() ? response.body() : new GetOrderSumary();
    }

    public Long getLastStatusOrderProvider(Long orderId, Long providerId){
        try {
            final String url = URLConnections.URL_OMS_GET_LAST_STATUS_ORDER_PROVIDER.replace("{orderId}", String.valueOf(orderId)).replace("{providerId}", String.valueOf(providerId));
            //LOG.info("Request OMS ->" + url);
            Call<GetLastStatusOrderProvider> call = apiGateway.getLastStatusOrderProvider(url);
            Response<GetLastStatusOrderProvider> response = call.execute();
            if(response.isSuccessful() && Objects.nonNull(response.body()) && Objects.nonNull(response.body().getData())){
                return response.body().getData();
            }
        }catch(Exception e){
            LOG.warning("Ocurrio algo imprimendo la data");
        }
        return 1L;
    }

    public OrderInfoDataResponse getTrackingOrder(final Long orderId) throws IOException {
        Call<OrderInfoDataResponse> call = apiGateway.getTrackingOrder(URLConnections.URL_OMS_GET_ORDER_INFO.replace("{orderId}", String.valueOf(orderId)));
        Response<OrderInfoDataResponse> response = call.execute();
        return response.isSuccessful() ? response.body() : new OrderInfoDataResponse();
    }


    public RatingResponse getRatings() throws IOException {
        LOG.info("Call getRatings");
        //LOG.info("Peticion -> " + URLConnections.URL_OMS_GET_RATINGS);
        final Call<RatingResponse> call = apiGateway.getRatings(URLConnections.URL_OMS_GET_RATINGS);
        final Response<RatingResponse> response = call.execute();
        //LOG.info("Response code->" + response.code());
        LOG.info("Response data->" + response.body().toString());
        //LOG.info("Response->" + (response.body() != null ? response.body().toString() : null));
        return response.isSuccessful() ? response.body() : new RatingResponse();
    }

    public ReadOrderResponseBackend3 getReadOrder(final Long orderId) throws IOException {
        final String url = URLConnections.URL_OMS_READ_ORDER.replace("{orderId}", String.valueOf(orderId));
        //LOG.info("URL ->" + url);
        final Call<ReadOrderResponseBackend3> call = apiGateway.getReadOrder(url);
        final Response<ReadOrderResponseBackend3> response = call.execute();
        //LOG.info("Response: ->" + response.body());
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        LOG.info("Request ->" + gson.toJson(orderId));
        //LOG.info("Response Code->" + response.code());
        LOG.info("Response ->" + (response.body() != null ? response.body() : null));
        return response.body();
    }

    public ReadOrderResponseBackend3 getReadActiveOrder(final Long orderId) throws IOException {
        final String url = URLConnections.URL_OMS_GET_ACTIVE_ORDER.replace("{orderId}", String.valueOf(orderId));
        //LOG.info("method getReadActiveOrder URL ->" + url);
        final Call<ReadOrderResponseBackend3> call = apiGateway.getActiveOrder(url);
        final Response<ReadOrderResponseBackend3> response = call.execute();
        //LOG.info("Response: ->" + response.body());
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        LOG.info("Request ->" + gson.toJson(orderId));
        //LOG.info("Response Code->" + response.code());
        LOG.info("Response ->" + (response.body() != null ? response.body() : null));
        return response.body();
    }


    public ValidateCustomerData validateCustomerEmail(final String email) throws IOException {
        final String url = URLConnections.URL_CRM_GET_CUSTOMER_EMAIL_VALIDATE.replace("{emailCustomer}", email);
        //LOG.info("method validateCustomerEmail URL ->" + url);
        final Call<ValidateCustomerData> call = apiGateway.validateCustomerEmail(url);
        final Response<ValidateCustomerData> response = call.execute();
        return response.isSuccessful() ? response.body() : new ValidateCustomerData();
    }

    public ValidateCustomerData validateCustomerEmailCall(final String email) throws IOException {
        final String url = URLConnections.URL_CRM_GET_CUSTOMER_EMAIL_VALIDATE_CALL.replace("{emailCustomer}", email);
        //LOG.info("method validateCustomerEmail URL ->" + url);
        final Call<ValidateCustomerData> call = apiGateway.validateCustomerEmail(url);
        final Response<ValidateCustomerData> response = call.execute();
        return response.isSuccessful() ? response.body() : new ValidateCustomerData();
    }

    public ValidateCustomerResponse validateCustomerDocumentNumber(final Long documentNumber) throws IOException {
        final String url = URLConnections.URL_CRM_GET_CUSTOMER_DOCUMENT_NUMBER_VALIDATE.replace("{documentNumber}", String.valueOf(documentNumber));
        //LOG.info("method validateCustomerDocumentNumber URL ->" + url);
        final Call<ValidateCustomerResponse> call = apiGateway.validateCustomerDocumentNumber(url);
        final Response<ValidateCustomerResponse> response = call.execute();
        LOG.info("method validateCustomerDocumentNumber response: {" + response.body().toString() + "}");
        return response.isSuccessful() ? response.body() : new ValidateCustomerResponse();
    }

    public ValidateGeneralBool validateCreditCardForDelete(final int creditCardId) throws IOException {
        LOG.info("method validateCreditCardForDelete() creditCardId: #" + creditCardId);
        final String url = URLConnections.URL_PREVALIDATE_DELETE_CREDIT_CARD.replace("{creditCardId}", String.valueOf(creditCardId));
        //LOG.info("Request to CRM -> " + url);
        final Call<ValidateGeneralBool> call = apiGateway.validateCreditCardForDelete(url);
        final Response<ValidateGeneralBool> response = call.execute();

        return response.isSuccessful() ? response.body() : new ValidateGeneralBool();
    }

    public Response<CustomerCreditCard> deleteCreditCardByIdAndCustomerId(final Long creditCardId, final Long customerId) throws IOException {
        LOG.info("method deleteCreditCardByIdAndCustomerId creditCard: " + creditCardId + " customerId: " + customerId);
        final String url = URLConnections.URL_CRM_DELETE_CREDIT_CARD_BY_ID_AND_CUSTOMER_ID
                .replace("{creditCardId}", String.valueOf(creditCardId))
                .replace("{customerId}", String.valueOf(customerId));
        // set params
        //LOG.info("Request to CRM -> " + url);
        Call<CustomerCreditCard> call = apiGateway.deleteCreditCardByIdAndCustomerId(url);
        Response<CustomerCreditCard> response = call.execute();
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        LOG.info("Response CRM -> " + response.isSuccessful());
        return response;
    }

    public Response<CustomerResult<CustomerCreditCardToken>> tokenCreditCardByIdAndCustomerId(final String gateway, final Long customerId) throws IOException {
        final String url = URLConnections.URL_CRM_GET_CREDIT_CARD_TOKEN_BY_CUSTOMER_ID_AND_GATEWAY
                .replace("{gateway}", gateway)
                .replace("{customerId}", String.valueOf(customerId));
        Call<CustomerResult<CustomerCreditCardToken>> call = apiGateway.tokenCreditCardByIdAndCustomerId(url);
        Response<CustomerResult<CustomerCreditCardToken>> response = call.execute();

        LOG.info("URL CRM {}" + url + "Response CRM" +
                (response.body() != null ? response.body().toString() : response));
        return response;
    }

    public Response<CustomerResult<CustomerCreditCardGateway>> gatewayActive(final String city) throws IOException {
         String url = URLConnections.URL_CRM_GET_CREDIT_CARD_GATEWAY_ACTIVE;
        if(Objects.nonNull(city)){
            //LOG.info("method gatewayActive city2: " + city );
            url=url+"?city="+city;
        }
        Call<CustomerResult<CustomerCreditCardGateway>> call = apiGateway.gatewayActive(url);
        Response<CustomerResult<CustomerCreditCardGateway>> response = call.execute();

        LOG.info("URL CRM {}" + url + "Response CRM" +
                (response.body() != null ? response.body().toString() : response));
        return response;
    }

    public Response<CustomerCreditCard> setCreditCardDefault(final Long creditCardId, final Long customerId, final String traceId) throws IOException {
        LOG.info("method setCreditCardDefault creditCard: " + creditCardId + " customerId: " + customerId);
        final String url = URLConnections.URL_CRM_POST_CUSTOMER_CREDIT_CARD_DEF;
        final CreditCardDefaultReq req = new CreditCardDefaultReq(creditCardId, customerId);
        // set params
        //LOG.info("Request to CRM -> " + url);
        Call<CustomerCreditCard> call = apiGateway.defaultCreditCard(url, req, traceId);
        Response<CustomerCreditCard> response = call.execute();
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        LOG.info("Response CRM -> " + response.isSuccessful());
        return response;
    }

    public Address validateAddress(final AddAddressRequest addAddressRequest) throws IOException {
        LOG.info("method validateAddress()");
        final String url = URLConnections.URL_VALIDATE_ADDRESS_BCK3;
        //LOG.info("createCustomerAddress : " + "URl -> " + url);
        CustomerAddresReq request = buildCreateCustomerAddress(addAddressRequest);
        Call<AddressResponse> call = apiGateway.validateCustomerAddress(url, request);
        Response<AddressResponse> response = call.execute();
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        LOG.info("Request to Microservice CRM ->" + gson.toJson(request));
        LOG.info("Response CRM ->" + gson.toJson(response.body().getData()));
        final AddresResponseData data = response.body().getData();
        Address addressResponse;
        if (Objects.nonNull(data.getAddress())) {
            addressResponse = new Address(data);
        } else {
            addressResponse = new Address();
        }
        return addressResponse;
    }

    public ValidateGeneralBool updateCustomerAddres(final UpdateAddressRequest addressRequest, final String idCustomer) {
        LOG.info("method updateCustomerAddres() AddressId: #" + addressRequest.getIdAddress() + ", customer: #" + idCustomer);
        final String url = URLConnections.URL_UPDATE_CUSTOMER_ADDRESS.replace("{customerId}", idCustomer);
        //LOG.info("URL -> " + url);
        CustomerAddresReq request = buildUpdateCustomerAddressv2(addressRequest);
        final Call<ValidateGeneralBool> call = apiGateway.updateCustomerAddress(url, request);
        try {
            final Response<ValidateGeneralBool> response = call.execute();
            LOG.info("Response -> " + (response.body() != null ? response.body().toString() : response));
            return response.body();
        } catch (IOException ex) {
            LOG.warning("Ocurrio algo ejecutando el metodo updateCustomerAddres() -> " + ex.getMessage());
            return new ValidateGeneralBool();
        }
    }

    public List<CreditCard> getAllCreditCard(final Integer customerId, final String city,
                                             String sourceEnum ) throws IOException {
         String url = URLConnections.URL_CRM_GET_CUSTOMER_CREDIT_CARD
                .replace("{customerId}", customerId.toString());

        if(Objects.nonNull(city) && Objects.nonNull(sourceEnum)){
            url=url+"?city="+city + "&source=" + sourceEnum;
        }else if(Objects.nonNull(city)){
            url=url+"?city="+city;
        }else if(Objects.nonNull(sourceEnum)){
            url=url+"?source=" + sourceEnum;
        }

        //LOG.info("method getAllCreditCard URL ->" + url);
        final Call<CustomerCreditCard> call = apiGateway.getAllCustomerCreditCard(url);
        final Response<CustomerCreditCard> response = call.execute();
        return response.isSuccessful() && response.body() != null ? response.body().getData() : new ArrayList<>();
    }

    public Response<PSEResponseCode> getAllPSE() throws IOException {
        final String url = URLConnections.URL_GET_ACTIVE_PSE;
        //LOG.info("method getAllCreditCard URL ->" + url);
        final Call<PSEResponseCode> call = apiGateway.getAllPse(url);
        final Response<PSEResponseCode> response = call.execute();
        return response;
    }

    public Optional<PaymentMethodV2FTDResponse> paymentMethodV2FTD(PaymentMethodV2DTFRequest request){
        try {
            final String url = URLConnections.URL_PAYMENT_METHODSV2;
            //LOG.info("method paymentMethodV2FTD URL ->" + url);
            // LOG.info("method paymentMethodV2FTD request -> " + request.toString());
            Call<PaymentMethodV2FTDResponse> call = apiGateway.paymentMethodsV2(url,request);
            Response<PaymentMethodV2FTDResponse> response = call.execute();

            if ( response.isSuccessful() && response.body() !=null ){
                return Optional.of(response.body());
            }

        } catch (Exception e) {
            LOG.warning("Error metodos de pago " + e.getMessage() != null ? e.getMessage() : " es null.");
        }

        return Optional.empty();
    }


    public Optional<UpdateEmailCustomerResponse> updateEmailCustomer(Long customerId, String newEmail) throws IOException {
        final String url = URLConnections.URL_CRM_UPDATE_EMAIL;
        //LOG.info("method updateEmailCustomer URL ->" + url);

        Call<UpdateEmailCustomerResponse> call = apiGateway.updateEmailCustomer(url, customerId, newEmail);

        Response<UpdateEmailCustomerResponse> response = call.execute();
        LOG.info(response.raw().toString());

        if (response.isSuccessful() && response.body() != null){

            return Optional.of(response.body());
        }

        return Optional.empty();

    }

    public ValidateStockRouteRes validateStock(ValidateStockRouteReq validateStockRouteReq) throws IOException {
        //LOG.info("valiateStock Url -> " + URLConnections.URL_DTF_POST_VALIDATE_STOCK);
        Call<ValidateStockRouteRes> call = apiGateway.validateStock(URLConnections.URL_DTF_POST_VALIDATE_STOCK, validateStockRouteReq);
        Response<ValidateStockRouteRes> response = call.execute();
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        LOG.info("Request to Microservice DTF Validate Stock ->" + gson.toJson(validateStockRouteReq));
        return response.isSuccessful() ? response.body() : null;
    }

    public CustomersByIdResponse getCustomerListByIds(CustomerByIdRequest request) throws IOException {
        LOG.info("getCustomerListByIds");
        //LOG.info("URl -> " + URLConnections.URL_CRM_POST_GET_ALL_CUSTOMERS);

        Call<CustomersByIdResponse> call = apiGateway.getCustomerListByIds(URLConnections.URL_CRM_POST_GET_ALL_CUSTOMERS, request);
        Response<CustomersByIdResponse> response = call.execute();
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        LOG.info("Request to Microservice CRM ->" + gson.toJson(request));
        LOG.info("Response CRM ->" + gson.toJson(response.body()));
        return response.isSuccessful() ? response.body() : null;
    }

    private CustomerAddresReq buildCreateCustomerAddress(final AddAddressRequest addAddressRequest) {
        CustomerAddresReq request = new CustomerAddresReq();
        request.setCustomerId(Long.valueOf(addAddressRequest.getIdCustomer()));
        request.setNickname(addAddressRequest.getNickname());
        request.setCityId(addAddressRequest.getCity());
        request.setAddress(addAddressRequest.getAddress());
        request.setDeliveryType(addAddressRequest.getDeliveryType());

        if (Objects.nonNull(addAddressRequest.getLatitude())) {
            request.setLatitude(addAddressRequest.getLatitude());
        } else {
            request.setLatitude(0D);
        }

        if (Objects.nonNull(addAddressRequest.getLongitude())) {
            request.setLongitude(addAddressRequest.getLongitude());
        } else {
            request.setLongitude(0D);
        }

        request.setCloserStoreId(addAddressRequest.getAssignedStore());
        request.setComments(addAddressRequest.getComments());
        return request;
    }

    private CustomerAddresReq buildUpdateCustomerAddressv2(final UpdateAddressRequest address) {
        CustomerAddresReq request = new CustomerAddresReq();
        request.setCustomerAddressId(Long.valueOf(address.getIdAddress()));
        request.setNickname(address.getNickname());
        request.setCityId(address.getCity());
        request.setAddress(address.getAddress());
        request.setDeliveryType(address.getDeliveryType());
        request.setTags(address.getTags());

        if (Objects.nonNull(address.getLatitude())) {
            request.setLatitude(address.getLatitude());
        } else {
            request.setLatitude(0D);
        }

        if (Objects.nonNull(address.getLongitude())) {
            request.setLongitude(address.getLongitude());
        } else {
            request.setLongitude(0D);
        }

        request.setCloserStoreId(address.getAssignedStore());
        request.setComments(address.getComments());
        request.setAddressWithRestriction(address.isAddressWithRestriction());
        request.setRedZoneId(address.getRedZoneId());
        return request;
    }

/*
    OLD BANNERS
/*
    OLD BANNERS
    public BannersDTFRes getBannersDTF(final BannersDTFReq bannersDTFReq) throws IOException {
        LOG.info("method getBannersDTF");
        LOG.info(" getBannersDTF URL -> " + URLConnections.URL_DTF_GET_BANNERS);
        Call<BannersDTFRes> call = api.getBannersDTF(URLConnections.URL_DTF_GET_BANNERS, bannersDTFReq);
        Response<BannersDTFRes> response = call.execute();
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        LOG.info("Request to Microservice DTF ->" + gson.toJson(bannersDTFReq));
        LOG.info("Response DTF ->" + gson.toJson(response.body()));
        return  response.isSuccessful() ? response.body() : null;
    }
*/

    public boolean validateCustomerPhone(ValidateCustomerPhoneReq validateCustomerPhoneReq) throws IOException {
        //LOG.info("validateCustomerPhoneReq Url -> " + URLConnections.URL_GET_CUSTOMER_BY_PHONE);
        Call<ValidateCustomerPhoneResp> call = apiGateway.validateCustomerPhone(URLConnections.URL_GET_CUSTOMER_BY_PHONE, validateCustomerPhoneReq);
        Response<ValidateCustomerPhoneResp> response = call.execute();
        LOG.info("Response validateCustomerPhoneReq ->" + (response.body() != null ? response.body().toString() : null));
        if ((response.isSuccessful()) && (response.body().getCode().equalsIgnoreCase("OK"))) {
            //LOG.info("response.body().getCode()-->" + response.body().getCode());
            //LOG.info("response.body().getMessage()-->" + response.body().getMessage());
            LOG.info("response.body().getData()-->" + response.body().getData());
            return false;
        }
        return true;
    }

    public CustomerOnlyData getCustomerOnlyById(Integer customerId) throws IOException {
        //LOG.info("method getCustomerOnlyById");
        Map<String, String> pathVariables = new HashMap<>();
        pathVariables.put("customerId", String.valueOf(customerId));
        String urlWithParams = Util.buildUrl(URLConnections.URL_CRM_CUSTOMER_ONLY, pathVariables, null);

        Call<GetCustomerResponse<CustomerOnlyData>> call = apiGateway.getCustomerOnlyById(urlWithParams);
        Response<GetCustomerResponse<CustomerOnlyData>> response = call.execute();
        LOG.info("Request to CRM ->" + urlWithParams);
        LOG.info("Response CRM ->" + (Objects.nonNull(response.body()) ? response.body().getData() : null));
        return response.isSuccessful() ? response.body().getData() : new CustomerOnlyData();
    }

    public Optional<String> getMessengerNameByOrderId(Long orderId) {
        try{
            Map<String, String> pathVariables = new HashMap<>();
            pathVariables.put("orderId", String.valueOf(orderId));
            String urlWithParams = Util.buildUrl(URLConnections.URL_OMS_MESSENGER_NAME, pathVariables, null);

            Call<MessengerNameResponse> call = apiGateway.getMessengerNameByOrderId(urlWithParams);
            Response<MessengerNameResponse> response = call.execute();

            return response.isSuccessful() && response.body() != null ? Optional.of(response.body().getData()) :
                    Optional.empty();
        }catch (Exception e){
            LOG.warning("Ocurrio un error getMessengerNameByOrderId -> "+e.getMessage());
        }
      return Optional.empty();
    }

    public Optional<CustomerJSON> getCustomerById(Integer customerId){
        try {
            LOG.info("method getCustomerById {}" + customerId);

            if (customerId == null || customerId <= 0){
                return Optional.empty();
            }

            String urlWithParams = URLConnections.URL_CRM_GET_CUSTOMER + "/" + customerId;
            Call<GetCustomerResponse<CustomerJSON>> call = apiGateway.getCustomer(urlWithParams);
            Response<GetCustomerResponse<CustomerJSON>> response = call.execute();
            LOG.info("Request to CRM ->" + urlWithParams);
            LOG.info("Response to CRM -> Status" + response.code());
            //LOG.info("Response CRM ->" + (Objects.nonNull(response.body()) ? response.body().getData() : null));

            if ( response.isSuccessful() && response.body() != null && response.body() != null){
                return Optional.of(response.body().getData());
            }

            return Optional.empty();
        }catch (Exception e){
            LOG.warning("Ocurrio un error getCustomerById -> "+e.getMessage());
        }
        return Optional.empty();
    }

    public Suggesteds getSuggestedById(Integer segmentId) throws IOException {
        //LOG.info("method getSuggestedById {}" + segmentId);
        String urlWithParams = URLConnections.URL_CRM_GET_SUGGESTED.replace("{idSegment}", "" + segmentId);
        Call<GetCustomerResponse<List<SuggestedObject>>> call = apiGateway.getSuggested(urlWithParams);
        Response<GetCustomerResponse<List<SuggestedObject>>> response = call.execute();
        LOG.info("Request to CRM ->" + urlWithParams);
        //LOG.info("Response CRM ->" + (Objects.nonNull(response.body()) ? response.body().getData() : null));
        return response.isSuccessful() && response.body() != null ? new Suggesteds(response.body().getData()) : null;
    }

    public AddressesRes getAddressesByCustomerId(Integer customerId) throws IOException {
        //LOG.info("method getAddressesByCustomerId");
        Map<String, String> pathVariables = new HashMap<>();
        pathVariables.put("customerId", String.valueOf(customerId));
        String urlWithParams = Util.buildUrl(URLConnections.URL_CRM_GET_ADDRESS_BY_CUSTOMER, pathVariables, null);

        Call<GetCustomerResponse<AddressesRes>> call = apiGateway.getAddressByCustomerId(urlWithParams);
        Response<GetCustomerResponse<AddressesRes>> response = null;
        try {
            response = call.execute();
            LOG.info("Request to CRM ->" + urlWithParams);
            //LOG.info("Response CRM ->" + (response != null && Objects.nonNull(response.code()) ? response.code() : null));
            LOG.info("Response CRM ->" + (response != null && Objects.nonNull(response.body()) ? response.body().toString() : null));
        } catch (Exception e) {
            LOG.warning("Error@getAddressesByCustomerId calling apiGatewayService -> " + e.getMessage());
        }
        return response != null && response.isSuccessful() && response.body() != null ? response.body().getData() : new AddressesRes();
    }

    public List<CustomerCallResponseData> getCustomerCallCenter(final CustomerCallReq request) throws IOException {
        LOG.info("getCustomerCallCenter");
        //LOG.info("URl -> " + URLConnections.URL_CRM_GET_CUSTOMER_CALL);
        Call<CustomerCallResponse> call = apiGateway.getCustomerCallCenter(URLConnections.URL_CRM_GET_CUSTOMER_CALL, request);
        Response<CustomerCallResponse> response = call.execute();
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        LOG.info("Request to Microservice CRM ->" + gson.toJson(request));
        LOG.info("Response CRM ->" + gson.toJson(response.body()));
        return response.isSuccessful() ? response.body().getData() : null;
    }

    public Optional<Address> createCustomerAddress(final AddAddressRequest addressRequest, final String traceId) throws IOException {
        final String url = URLConnections.URL_CRM_CREATE_CUSTOMER_ADDRESS.replace("{customerId}", String.valueOf(addressRequest.getIdCustomer()));
        //LOG.info("createCustomerAddress : " + "URl -> " + url);
        CreateAddresReq request = buildCreateCustomerAddressV3(addressRequest);
        Call<AddressResponse> call = apiGateway.createCustomerAddress(url, request, traceId);
        Response<AddressResponse> response = call.execute();

        if (response.isSuccessful() && response.body() != null && response.body().getData() != null){
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            LOG.info("Request to Microservice CRM ->" + gson.toJson(request));
            LOG.info("Response CRM ->" + gson.toJson(response.body()));
            final AddresResponseData data = response.body().getData();
            Address addressResponse = new Address(data);
            return Optional.of(addressResponse);
        }

        return Optional.empty();

    }
    
    public Address updateCustomerAddress(final AddAddressRequest addressRequest) throws IOException {
        final String url = URLConnections.URL_CRM_PUT_CUSTOMER_ADDRESS.replace("{customerId}", String.valueOf(addressRequest.getIdCustomer()));
        //LOG.info("updateCustomerAddress : " + "URl -> " + url);
        CreateAddresReq request = buildCreateCustomerAddressV3(addressRequest);
        Call<AddressResponse> call = apiGateway.updateCustomerAddress(url, request);
        Response<AddressResponse> response = call.execute();
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        LOG.info("Request to Microservice CRM ->" + gson.toJson(request));
        LOG.info("Response CRM ->" + gson.toJson(response.body()));
        if (response.body() == null) {
            LOG.warning("Error@updateCustomerAddress, CRM response is NULL");
        }
        final AddresResponseData data = response.body() != null ? response.body().getData() : new AddresResponseData();
        Address addressResponse = new Address(data);
        return addressResponse;
    }

    public CustomerJSON customerLoginEmail(final String url, final CustomerLoginReq customer, final String traceId) throws ConflictException, IOException {
        LOG.info("Request -> " + customer.toString());
        //LOG.info("URL Request: " + url);
        final Call<GetCustomerResponse<CustomerJSON>> call = apiGateway.customerLoginEmail(url, customer, traceId);
        final Response<GetCustomerResponse<CustomerJSON>> response = call.execute();
//        LOG.info("Response -> {"+ response.body().getData()+"}");
        if (!response.isSuccessful()) {
            LOG.warning("No fue satisfactoria la respuesta de backend3");
            if (Objects.nonNull(response.errorBody())) {
                try {
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    Gson gson = gsonBuilder.create();
                    OrderChargeRes errorResponse = gson.fromJson(response.errorBody().string(), OrderChargeRes.class);
                    LOG.warning("Message: " + errorResponse.getMessage());
                    throw new ConflictException(errorResponse.getMessage());
                } catch (Exception e) {
                    LOG.warning("Error@customerLoginEmailApiGateway: " + e.getMessage());

                    throw new ConflictException("Error general.");
                }
            }
        }
        return response.isSuccessful() ? response.body().getData() : new CustomerJSON();
    }

    public CustomerJSON customerLoginDocument(final String url, final SelfCheckout selfCheckout) throws ConflictException, IOException {
        LOG.info("Request -> " + selfCheckout);
        //LOG.info("URL Request: " + url);
        final Call<GetCustomerResponse<CustomerJSON>> call = apiGateway.customerLoginDocument(url, selfCheckout);
        final Response<GetCustomerResponse<CustomerJSON>> response = call.execute();
//        LOG.info("Response -> {"+ response.body().getData()+"}");
        if (!response.isSuccessful()) {
            LOG.warning("No fue satisfactoria la respuesta de backend3");
            if (Objects.nonNull(response.errorBody())) {
                try {
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    Gson gson = gsonBuilder.create();
                    OrderChargeRes errorResponse = gson.fromJson(response.errorBody().string(), OrderChargeRes.class);
                    LOG.warning("Message: " + errorResponse.getMessage());
                    throw new ConflictException(errorResponse.getMessage());
                } catch (Exception e) {
                    LOG.warning("Error@customerLoginEmailApiGateway: " + e.getMessage());
                    throw new ConflictException("Error general.");
                }
            }
        }
        return response.isSuccessful() ? response.body().getData() : new CustomerJSON();
    }


    public CreditCard createCreditCard(final CreditCardReq request, final String traceId) throws IOException {
        final String url = URLConnections.URL_CRM_GET_CUSTOMER_CREDIT_CARD;
        //LOG.info("method createCreditCard URL ->" + url);
        final Call<GetCustomerResponse<CreditCard>> call = apiGateway.createCustomerCreditCar(url, request, traceId);
        final Response<GetCustomerResponse<CreditCard>> response = call.execute();
        return response.isSuccessful() ? response.body().getData() : new CreditCard();
    }

    public CustomerJSON handleCustomerLifeMiles(final CustomerLifeMilesReq request) throws IOException, ConflictException {
        final String url = URLConnections.URL_CRM_POST_LIFEMILES_CUSTOMER;
        //LOG.info("method handleCustomerLifeMiles URL ->" + url);
        final Call<GetCustomerResponse<CustomerJSON>> call = apiGateway.handleCustomerLifeMiles(url, request);
        final Response<GetCustomerResponse<CustomerJSON>> response = call.execute();
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        LOG.info("Request to Microservice CRM ->" + gson.toJson(request));
        LOG.info("Response CRM ->" + (Objects.nonNull(response) ? gson.toJson(response.body()) : ""));
        //return response.isSuccessful() ? response.body().getData() : new CustomerJSON();

        if (!response.isSuccessful()) {
            LOG.warning("No fue satisfactoria la respuesta de backend3");
            if (Objects.nonNull(response)) {
                OrderChargeRes errorResponse = gson.fromJson(response.errorBody().string(), OrderChargeRes.class);
                LOG.warning("Message: " + errorResponse.getMessage());
                throw new ConflictException(errorResponse.getMessage());
            }
        }
        return response.isSuccessful() ? response.body().getData() : new CustomerJSON();
    }

    public CustomerLifeMileJSON calculateLifeMiles(final CustomerLifeMilesReq request) throws IOException {
        return customerLifeMiles(URLConnections.URL_CRM_POST_LIFEMILES_CALCULATE, request);
    }

    public CustomerLifeMileJSON getCustomerLifeMilesNumber(final CustomerLifeMilesReq request) throws IOException {
        return customerLifeMiles(URLConnections.URL_CRM_POST_LIFEMILES_NUMBER, request);
    }

    private CustomerLifeMileJSON customerLifeMiles(final String url, final CustomerLifeMilesReq request) throws IOException {
        //LOG.info("method numberLifeMiles URL ->" + url);
        final Call<GetCustomerResponse<CustomerLifeMileJSON>> call = apiGateway.customerLifeMiles(url, request);
        final Response<GetCustomerResponse<CustomerLifeMileJSON>> response = call.execute();
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        LOG.info("Request to Microservice CRM ->" + gson.toJson(request));
        LOG.info("Response CRM ->" + (Objects.nonNull(response) ? gson.toJson(response.body()) : ""));
        return response.isSuccessful() ? response.body().getData() : new CustomerLifeMileJSON();
    }

    public CustomerLifeMileJSON inactiveLifeMiles(final CustomerLifeMilesReq request) throws IOException {
        final String url = URLConnections.URL_CRM_PUT_LIFEMILES_INACTIVE;
        //LOG.info("method inactiveLifeMiles URL ->" + url);
        final Call<GetCustomerResponse<CustomerLifeMileJSON>> call = apiGateway.inactiveCustomerLifeMiles(url, request);
        final Response<GetCustomerResponse<CustomerLifeMileJSON>> response = call.execute();
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        LOG.info("Request to Microservice CRM ->" + gson.toJson(request));
        LOG.info("Response CRM ->" + (Objects.nonNull(response) ? gson.toJson(response.body()) : ""));
        return response.isSuccessful() ? response.body().getData() : new CustomerLifeMileJSON();
    }

    public AnswerGetUserOrigin getOriginByPhone(final CustomerOriginReq request) throws IOException, ConflictException {
        //LOG.info("method getOriginByPhone");
        return getUserOrigin(URLConnections.URL_CRM_POST_ORIGIN_BY_PHONE, request);
    }

    public AnswerGetUserOrigin getOriginByEmail(final CustomerOriginReq request) throws IOException, ConflictException {
        //LOG.info("method getOriginByEmail");
        return getUserOrigin(URLConnections.URL_CRM_POST_ORIGIN_BY_EMAIL, request);
    }

    public AnswerGetUserOrigin getOriginByUID(final CustomerOriginReq request) throws IOException, ConflictException {
        //LOG.info("method getOriginByUID");
        return getUserOrigin(URLConnections.URL_CRM_POST_ORIGIN_BY_UID, request);
    }

    private AnswerGetUserOrigin getUserOrigin(final String url, final CustomerOriginReq request) throws IOException, ConflictException {
        final String messageWhenError = "Si es la primera vez que ingresas deberás registrarte, si no, verifica tu correo o ingresa con tu número celular.";
        try {
            //LOG.info("method getUserOrigin URL ->" + url + " request: " + (Objects.nonNull(request) ? request.toString() : "null"));
            final Call<GetCustomerResponse<AnswerGetUserOrigin>> call = apiGateway.getOrigin(url, request);
            final Response<GetCustomerResponse<AnswerGetUserOrigin>> response = call.execute();
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            LOG.info("Request to Microservice CRM ->" + gson.toJson(request.toString()));
            LOG.info("Response CRM ->" + (Objects.nonNull(response) && Objects.nonNull(response.body()) ? gson.toJson(response.body()) : ""));
            if (response.isSuccessful() && Objects.nonNull(response.body())) {
                AnswerGetUserOrigin answerGetUserOrigin = response.body().getData();
                answerGetUserOrigin.setStatusCode(response.code());
                answerGetUserOrigin.setStatus("" + response.code());

//                final String message = Util.buildCustomOriginMessageBack30(request, response.body().getData());
                String message = Constants.EMAIL_EXISTS_TO_TFD;

                final String messageFinal = message.replace("{customerEmail}", request.getEmail()).replace("{origin}", response.body().getData().getOrigin());
//                answerGetUserOrigin.setMessage(message);
                answerGetUserOrigin.setMessage(messageFinal);
                return answerGetUserOrigin;
            }
            LOG.warning("No fue satisfactoria la respuesta de backend3");
            if (Objects.nonNull(response)) {
                OrderChargeRes errorResponse = gson.fromJson(Objects.nonNull(response.errorBody()) ? response.errorBody().string() : "", OrderChargeRes.class);
                LOG.warning("Message: " + errorResponse.getMessage());
                throw new ConflictException(errorResponse.getMessage());
            }
            throw new ConflictException("No fue posible obtener respuesta del backend 3");
        }catch (Exception ex){
            throw new ConflictException("No fue posible obtener respuesta del backend 3");
        }
    }

    public CustomerJSON readCustomerMonitor(final CustomerData request) throws IOException, ConflictException {
        final String url = URLConnections.URL_CRM_POST_CUSTOMER_MONITOR;
        //LOG.info("method readCustomerMonitor URL ->" + url + " request: " + request);
        final Call<GetCustomerResponse<CustomerJSON>> call = apiGateway.getCustomerMonitor(url, request);
        final Response<GetCustomerResponse<CustomerJSON>> response = call.execute();
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        LOG.info("Request to Microservice CRM ->" + gson.toJson(request));
        LOG.info("Response CRM ->" + (Objects.nonNull(response) ? gson.toJson(response.body()) : ""));
        if (!response.isSuccessful()) {
            LOG.warning("No fue satisfactoria la respuesta de backend3");
            if (Objects.nonNull(response)) {
                OrderChargeRes errorResponse = gson.fromJson(response.errorBody().string(), OrderChargeRes.class);
                LOG.warning("Message: " + errorResponse.getMessage());
                throw new ConflictException(errorResponse.getMessage());
            }
        }
        return response.isSuccessful() ? response.body().getData() : new CustomerJSON();
    }

    public Response<GetCustomerResponse> deleteLogicCustomer(final Long customerId) throws IOException, ConflictException {
        Map<String, String> pathVariables = new HashMap<>();
        pathVariables.put("customerId", String.valueOf(customerId));
        String urlWithParams = Util.buildUrl(URLConnections.URL_CRM_DELETE_LOGIC_CUSTOMER, pathVariables, null);
        //LOG.info("method deleteLogicCustomer URL ->" + urlWithParams);
        final Call<GetCustomerResponse> call = apiGateway.deleteLogicCustomer(urlWithParams);
        final Response<GetCustomerResponse> response = call.execute();
        LOG.info("Request to Microservice CRM ->" + urlWithParams);
        LOG.info("Response CRM -> " + response.code());
        if (!response.isSuccessful()) {
            LOG.warning("No fue satisfactoria la respuesta de backend3");
            if (Objects.nonNull(response)) {
                GsonBuilder gsonBuilder = new GsonBuilder();
                Gson gson = gsonBuilder.create();
                OrderChargeRes errorResponse = gson.fromJson(response.errorBody().string(), OrderChargeRes.class);
                LOG.warning("Message: " + errorResponse.getMessage());
                throw new ConflictException(errorResponse.getMessage());
            }
        }
        return response;
    }

    public Response<GetCustomerResponse> orderQualify(final Qualification qualification) throws IOException, ConflictException {
        //LOG.info("method orderQualify URL ->" + qualification);
        final Call<GetCustomerResponse> call = apiGateway.orderQualify(URLConnections.URL_OMS_ORDER_QUALIFY, qualification);
        final Response<GetCustomerResponse> response = call.execute();
        LOG.info("Request to Microservice OMS ->");
        LOG.info("Response OMS -> " + response.code());
        return response;
    }

    public List<CourierRes> getCourierAll() throws IOException {
        //LOG.info("method getCourierAll URL ->" + URLConnections.URL_OMS_GET_COURIER_ALL);
        final Call<GenericResponse<List<CourierRes>>> call = apiGateway.getCourierAll(URLConnections.URL_OMS_GET_COURIER_ALL);
        final Response<GenericResponse<List<CourierRes>>> response = call.execute();
        LOG.info("Request to Microservice OMS ->");
        LOG.info("Response OMS -> " + response.code());
        if (response.isSuccessful() && Objects.nonNull(response.body()) && Objects.nonNull(response.body().getData()))
            return response.body().getData();
        return null;
    }

    public OrderStatusResponse orderStatusUpdate(final DeliveryOrderStatus deliveryOrderStatus) throws IOException, ConflictException {
        //LOG.info("method orderStatusUpdate URL ->" + URLConnections.URL_OMS_PUT_ORDER_STATUS_UPDATE);
        final Call<GenericResponse> call = apiGateway.putOrderStatusUpdate(URLConnections.URL_OMS_PUT_ORDER_STATUS_UPDATE, deliveryOrderStatus);
        final Response<GenericResponse> response = call.execute();
        LOG.info("Response OMS -> " + response.code());
        OrderStatusResponse orderResponse = new OrderStatusResponse();
        orderResponse.setStatus(Objects.nonNull(response) ? "" + response.code() : "");
        orderResponse.setMessage(Objects.nonNull(response) ? response.message() : "");
        return orderResponse;
    }

    public OrderStatusResponse orderPickingUpdate(final UpdatePickingDateReq updatePickingDateReq) throws IOException, ConflictException {
        //LOG.info("method orderStatusUpdate URL ->" + URLConnections.URL_OMS_PUT_ORDER_PICKING_DATE);
        final Call<GenericResponse> call = apiGateway.putOrderPickingDateUpdate(URLConnections.URL_OMS_PUT_ORDER_PICKING_DATE, updatePickingDateReq);
        final Response<GenericResponse> response = call.execute();
        LOG.info("Response OMS -> " + response.code());
        OrderStatusResponse orderResponse = new OrderStatusResponse();
        orderResponse.setStatus(Objects.nonNull(response) ? "" + response.code() : "");
        orderResponse.setMessage(Objects.nonNull(response) ? response.message() : "");
        return orderResponse;
    }

    public ClientResponse orderProviderStatusUpdate(final String token, final OrderProviderStatus orderProviderStatus) throws IOException, ConflictException {
        //LOG.info("method orderProviderStatusUpdate URL ->" + URLConnections.URL_OMS_POST_ORDER_PROVIDER_STATUS_UPDATE);
        final Call<GenericResponse> call = apiGateway.postOrderProviderStatusUpdate(URLConnections.URL_OMS_POST_ORDER_PROVIDER_STATUS_UPDATE, token, orderProviderStatus);
        final Response<GenericResponse> response = call.execute();
        return getClientResponse(response.code(), response.isSuccessful(), response.message(), response.body(), response.errorBody());
    }

    public ClientResponse orderProvider(final String token) throws IOException, ConflictException {
        //LOG.info("method orderProvider URL ->" + URLConnections.URL_OMS_GET_ORDER_PROVIDER);
        final Call<GenericResponse<List<Object>>> call = apiGateway.getOrderProvider(URLConnections.URL_OMS_GET_ORDER_PROVIDER, token);
        final Response<GenericResponse<List<Object>>> response = call.execute();
        return getClientResponse(response.code(), response.isSuccessful(), response.message(), response.body(), response.errorBody());
    }

    public ClientResponse orderProviderStockUpdate(final String token, final ItemStock itemStock) throws IOException, ConflictException {
        //LOG.info("method orderProvider URL ->" + URLConnections.URL_DTF_ORDER_PROVIDER_STOCK_UPDATE);
        final Call<GenericResponse> call = apiGateway.orderProviderStockUpdate(URLConnections.URL_DTF_ORDER_PROVIDER_STOCK_UPDATE, token, itemStock);
        final Response<GenericResponse> response = call.execute();
        LOG.info("Response OMS -> " + response.code());
        //ClientResponse orderResponse = new ClientResponse();
        //orderResponse.setStatus(Objects.nonNull(response) ? response.code() : 500);
        //orderResponse.setMessage(Objects.nonNull(response) ? response.message() : "");
        //orderResponse.setData(Objects.nonNull(response) && Objects.nonNull(response.body()) ? response.body().getData() : null);
        return getClientResponse(response.code(), response.isSuccessful(), response.message(), response.body(), response.errorBody());
    }

    @NotNull
    private ClientResponse getClientResponse(int code, boolean successful, String message, GenericResponse<List<Object>> body, ResponseBody responseBody) throws IOException {
        LOG.info("Response OMS -> " + code);
        ClientResponse orderResponse = new ClientResponse();
        if(successful) {
            orderResponse.setStatus(code);
            orderResponse.setMessage(message);
            orderResponse.setData(body.getData());
        }else if(!successful && Objects.nonNull(responseBody)){
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            OrderChargeRes errorResponse = gson.fromJson(responseBody.string(), OrderChargeRes.class);
            orderResponse.setStatus(code);
            orderResponse.setMessage(errorResponse.getMessage());
        }else{
            orderResponse.setStatus(500);
        }
        return orderResponse;
    }



    public Response<GenericResponse> cancelOrderToCourier(final SendOrder sendOrder) throws IOException, ConflictException {
        //LOG.info("method cancelOrderToCourier URL ->" + URLConnections.URL_OMS_CANCEL_ORDER_TO_COURIER + " sendOrder: " + sendOrder);
        final Call<GenericResponse> call = apiGateway.cancelOrderToCourier(URLConnections.URL_OMS_CANCEL_ORDER_TO_COURIER, sendOrder);
        final Response<GenericResponse> response = call.execute();
        LOG.info("Response OMS -> " + response.code());
        return response;
    }

    public Response<GenericResponse> updateOrderPaymentMethod(final DeliveryOrderStatus orderRequest) throws IOException {
        LOG.info("method cancelOrderToCourier URL ->" + URLConnections.URL_OMS_PUT_ORDER_PAYMENT_METHOD + " sendOrder: " + orderRequest);
        final Call<GenericResponse> call = apiGateway.updateOrderPaymentMethod(URLConnections.URL_OMS_PUT_ORDER_PAYMENT_METHOD, orderRequest);
        final Response<GenericResponse> response = call.execute();
        LOG.info("Response OMS -> " + response.code());
        return response;
    }

    public List<CustomerNewLoginRes> getDataForLogin(final CustomerNewLoginReq request){
        try {
            final String url = URLConnections.URL_CRM_GET_DATA_CUSTOMER_LOGIN;
            //LOG.info("getDataForLogin : " + "URl -> " + url);
            Call<CustomerLoginFinalRes> call = apiGateway.getCustomers(url, request);
            Response<CustomerLoginFinalRes> response = call.execute();
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            if (Objects.nonNull(response.body().getData())) {
                LOG.info("Request to Microservice CRM ->" + gson.toJson(request));
                LOG.info("Response CRM ->" + gson.toJson(response.body()));
                final List<CustomerNewLoginRes> data = response.body().getData();
                return data;
            }else {
                return null;
            }
        }catch (Exception e){
            LOG.warning("Error getDataForLogin() -> " + e.getMessage());
            return null;
        }
    }



    public Boolean sendMailCodeLogin(final SendMailCodeLoginReq request){
        try {
            final String url = URLConnections.URL_CRM_SEND_MAIL_CODE_LOGIN;
            //LOG.info("createCustomerAddress : " + "URl -> " + url);
            Call<SendMailCodeLoginRes> call = apiGateway.sendMailCode(url, request);
            Response<SendMailCodeLoginRes> response = call.execute();
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            LOG.info("Request to Microservice CRM ->" + gson.toJson(request));
            LOG.info("Response CRM ->" + gson.toJson(response));

            if (response.body().getCode().equals("OK")) {
                return true;
            }else {
                return false;
            }
        }catch (IOException e){
            LOG.warning("Error sendMailCodeLogin() -> " + e.getMessage());
            return false;
        }
    }

    public String getCustomerPhoneNumber(final Long idCustomer){
        try {
            final String url = URLConnections.URL_CRM_GET_PHONE_NUMBER.replace("{idCustomer}", String.valueOf(idCustomer));
            //LOG.info("createCustomerAddress : " + "URl -> " + url);
            Call<CustomerPhoneNumberRes> call = apiGateway.customerPhoneNumber(url);
            Response<CustomerPhoneNumberRes> response = call.execute();
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            LOG.info("Request to Microservice CRM ->" + url);
            LOG.info("Response CRM ->" + gson.toJson(response));

            if (Objects.nonNull(response.body().getData())){
                return response.body().getData();
            }else {
                return "NO_CONTENT";
            }
        }catch (IOException e){
            LOG.warning("Error sendMailCodeLogin() -> " + e.getMessage());
            return "";
        }
    }

    private CreateAddresReq buildCreateCustomerAddressV3(final AddAddressRequest addAddressRequest){
        CreateAddresReq request = new CreateAddresReq();
        request.setNickname(addAddressRequest.getNickname());
        request.setCityId(addAddressRequest.getCity());
        request.setAddress(addAddressRequest.getAddress());
        request.setDeliveryType(addAddressRequest.getDeliveryType());
        request.setTags(addAddressRequest.getTags());

        if (Objects.nonNull(addAddressRequest.getLatitude())) {
            request.setLatitude(addAddressRequest.getLatitude());
        } else {
            request.setLatitude(0D);
        }

        if (Objects.nonNull(addAddressRequest.getLongitude())) {
            request.setLongitude(addAddressRequest.getLongitude());
        } else {
            request.setLongitude(0D);
        }

        request.setAssignedStore(addAddressRequest.getAssignedStore());
        request.setComments(addAddressRequest.getComments());
        request.setAddressWithRestriction(addAddressRequest.getAddressWithRestriction());
        request.setRedZoneId(addAddressRequest.getRedZoneId());
        return request;
    }

    private CustomerRequest buildCustomerCallCenterReq(Customer customer) {
        CustomerRequest request = new CustomerRequest();

        if (Objects.nonNull(customer.getId()) && customer.getId() > 0) {
            request.setId((long) customer.getId());
        }

        if (Objects.nonNull(customer.getGender())) {
            request.setGender(customer.getGender());
        }

        if (Objects.nonNull(customer.getCountryId())) {
            request.setCountry(customer.getCountryId());
        } else {
            request.setCountry("CO");
        }
        request.setDocumentTypeId(customer.getDocumentType() != null ?
                customer.getDocumentType().longValue() :
                DocumentType.CEDULA_CIUDADANIA.getId());
        request.setDocumentNumber(parseDocumentNumber(customer.getDocumentNumber()));
        request.setFirstName(customer.getFirstName());
        request.setLastName(customer.getLastName());
        request.setPhone(customer.getPhone());


        if (Objects.nonNull(customer.getIdFacebook()))
            request.setFacebookId(customer.getIdFacebook());

        if (Objects.nonNull(customer.getIdGoogle()))
            request.setGoogleId(customer.getIdGoogle());

         if (Objects.nonNull(customer.getRegisteredBy())) {
            request.setRegisteredBy(customer.getRegisteredBy());
        }
         LOG.info("data -->" + customer.getRegisteredByCall());
         if(Objects.nonNull(customer.getRegisteredByCall()) &&  Constants.REGISTEREDBYCALL.equals(customer.getRegisteredByCall())){
             //LOG.info("Entro ?");
            request.setRegisteredBy(Constants.REGISTEREDBYCALL);
        }

        if (customer.getUidFirebase() != null && !customer.getUidFirebase().isEmpty()){
            request.setUidFirebase(customer.getUidFirebase());
        }
        if(request.getRegisteredBy() == null && customer.getIdGoogle() == null && customer.getIdFacebook() == null && customer.getUidFirebase() == null){
            request.setRegisteredBy(Constants.REGISTEREDBYEMAIL);
        }

        request.setEmail(customer.getEmail());
        request.setPassword(customer.getPassword());
        request.setSource(customer.getSource());
        request.setPhoneNumbers(customer.getPhoneNumbers());
        return request;
    }

    private Long parseDocumentNumber(String documentNumber) {
        if (Objects.nonNull(documentNumber)) {
            try {
                return Long.parseLong(documentNumber);
            } catch (NumberFormatException e) {
                return DOCUMENT_NUMBER_DEFAULT;
            }
        }
        return DOCUMENT_NUMBER_DEFAULT;
    }

    private CustomerRequest buildCustomerCallCenterReq(CustomerJSON customer) {
        CustomerRequest request = new CustomerRequest();

        if (Objects.nonNull(customer.getId()) && customer.getId() > 0) {
            request.setId((long) customer.getId());
        }

        if (Objects.nonNull(customer.getGender())) {
            request.setGender(customer.getGender());
        }

        if (Objects.nonNull(customer.getCountry())) {
            request.setCountry(customer.getCountry());
        } else {
            request.setCountry("CO");
        }
        request.setDocumentTypeId(parseDocumentTypeId(customer.getDocumentType()));
         if (Objects.nonNull(customer.getDocumentNumber()))
            request.setDocumentNumber(customer.getDocumentNumber().longValue());
        request.setFirstName(customer.getFirstName());
        request.setLastName(customer.getLastName());
        request.setPhone(customer.getPhone());


        if (Objects.nonNull(customer.getRegisteredBy()))
            request.setRegisteredBy(customer.getRegisteredBy());

        request.setEmail(customer.getEmail());

        return request;
    }

    private Long parseDocumentTypeId(String documentType) {
        if (documentType != null && !documentType.isEmpty()) {
            try {
                return Long.parseLong(documentType);
            } catch (NumberFormatException e) {
            }
        }
        return DocumentType.CEDULA_CIUDADANIA.getId();
    }

    public List<PaymentMethodRes> getPaymentMethodActive() throws IOException {
        //LOG.info("URL_PAYMENT_METHODS:" + URLConnections.URL_PAYMENT_METHODS_ACTIVE);
        Call<GenericResponse<List<PaymentMethodRes>>> call = apiGateway.getPaymentMethodActive(URLConnections.URL_PAYMENT_METHODS_ACTIVE);
        Response<GenericResponse<List<PaymentMethodRes>>> response = call.execute();

        if (response.isSuccessful()) {
            //LOG.info("Responsive " + response.body().getData());
            return response.body().getData();
        }
        return null;
    }

    public Answer sendMailSubscribeAndSaveCendisBack3(final Integer orderId) throws IOException {
        final String url = URLConnections.URL_OMS_ORDER_SUBSCRIPTION_CENDIS_MAIL.replace("{id}", orderId.toString());
        //LOG.info("method sendMailSubscribeAndSaveCendisBack3 --->" );
        final Call<GenericResponse> call = apiGateway.sendMailSubscribeAndSaveCendisBack3(url);
        final Response<GenericResponse> response = call.execute();
        return response.isSuccessful() ? new Answer(true) : new Answer(false);
    }


    public OrderInfoDataResponseMonitor getOrderInfoTracingBck3(final Long orderId) throws IOException, ConflictException {
        final String messageWhenError = "Orden No encontrada";
        try {
            Call<OrderInfoDataResponseMonitor> call = apiGateway.getOrderInfoTracingBck3(URLConnections.URL_GET_ORDER_INFO.replace("{orderId}", String.valueOf(orderId)));
            Response<OrderInfoDataResponseMonitor> response = call.execute();
            LOG.info("String code ->" + response.code());
            //LOG.info("String  -> " + response.body());
            if (response.isSuccessful() && Objects.nonNull(response.body())){
                OrderInfoStatus orderInfoDataResponseMonitor = response.body().getData();
                orderInfoDataResponseMonitor.setStatusCode(response.code());
                orderInfoDataResponseMonitor.getStatusId();

                return response.isSuccessful() ? response.body() : new OrderInfoDataResponseMonitor();
            }
            return new OrderInfoDataResponseMonitor(200, "OK",  messageWhenError);
        } catch (Exception ex) {
            return new OrderInfoDataResponseMonitor(200, "OK",  messageWhenError);
        }

    }

    public List<StoreJSON> getStoreActive() throws IOException {
        //LOG.info("URL_GET_STORE_ACTIVE:" + URLConnections.URL_GET_STORE_ACTIVE);
        Call<GenericResponse<List<StoreJSON>>> call = apiGateway.getStoreActive(URLConnections.URL_GET_STORE_ACTIVE);
        Response<GenericResponse<List<StoreJSON>>> response = call.execute();
        if (response.isSuccessful()) {
            //LOG.info("Response " + response.body().getData());
            return response.body().getData();
        }
        return null;
    }


    public List<CityJSON> getCityActive() throws IOException {
        //LOG.info("URL_GET_STORE_ACTIVE:" + URLConnections.URL_GET_CITY_ACTIVE);
        Call<GenericResponse<List<CityJSON>>> call = apiGateway.getCityActive(URLConnections.URL_GET_CITY_ACTIVE);
        Response<GenericResponse<List<CityJSON>>> response = call.execute();
        if (response.isSuccessful()) {
            //LOG.info("Response " + response.body().getData());
            return response.body().getData();
        }
        return null;
    }

    public List<Item> postGetItems(ItemReq itemReq) throws IOException {
        //LOG.info("postGetItems:" + URLConnections.URL_POST_GET_ITEMS);
        Call<GenericResponse<List<Item>>> call = apiGateway.postGetItems(URLConnections.URL_POST_GET_ITEMS, itemReq);
        Response<GenericResponse<List<Item>>> response = call.execute();
        if (response.isSuccessful()) {
            //LOG.info("Response " + response.body().getData());
            return response.body().getData();
        }
        return null;
    }

    public ItemRes postItemStart(ItemReq itemReq) throws IOException {
        //LOG.info("postItemStart:" + URLConnections.URL_POST_ITEM_START);
        Call<GenericResponse<ItemRes>> call = apiGateway.postItemStart(URLConnections.URL_POST_ITEM_START, itemReq);
        Response<GenericResponse<ItemRes>> response = call.execute();
        if (response.isSuccessful()) {
            //LOG.info("Response " + response.body().getData());
            return response.body().getData();
        }
        return null;
    }

    public ItemRes postItemStartDone(ItemReq itemReq) throws IOException {
        //LOG.info("postItemStartDone:" + URLConnections.URL_POST_ITEM_START_DONE);
        Call<GenericResponse<ItemRes>> call = apiGateway.postItemStart(URLConnections.URL_POST_ITEM_START_DONE, itemReq);
        Response<GenericResponse<ItemRes>> response = call.execute();
        if (response.isSuccessful()) {
            //LOG.info("Response " + response.body().getData());
            return response.body().getData();
        }
        return null;
    }

    public List<Department> getCategoryActive() throws IOException {
        //LOG.info("getCategoryActive:" + URLConnections.URL_GET_CLASSIFICATION_ACTIVE);
        Call<GenericResponse<List<Department>>> call = apiGateway.getCategoryActive(URLConnections.URL_GET_CLASSIFICATION_ACTIVE);
        Response<GenericResponse<List<Department>>> response = call.execute();
        if (response.isSuccessful()) {
            //LOG.info("Response " + response.body().getData());
            return response.body().getData();
        }
        return null;
    }

    public List<ItemCross> getCrossSales(Integer page) throws IOException {
        //LOG.info("getCategoryActive:" + URLConnections.URL_GET_CROSS_SALES);
        Map<String, String> pathVariables = new HashMap();
        pathVariables.put("page", String.valueOf(page));
        String urlWithParams = Util.buildUrl(URLConnections.URL_GET_CROSS_SALES, pathVariables, null);
        Call<GenericResponse<List<ItemCross>>> call = apiGateway.getCrossSales(urlWithParams);
        Response<GenericResponse<List<ItemCross>>> response = call.execute();
        if (response.isSuccessful()) {
            //LOG.info("Response " + response.body().getData());
            return response.body().getData();
        }
        return null;
    }

    public List<Highlight> getActiveHighLight() throws IOException {
        //LOG.info("getActiveHighLight:" + URLConnections.URL_GET_HIGHLIGHT);
        Call<GenericResponse<List<Highlight>>> call = apiGateway.getHighlightActive(URLConnections.URL_GET_HIGHLIGHT);
        Response<GenericResponse<List<Highlight>>> response = call.execute();
        if (response.isSuccessful()) {
            //LOG.info("Response " + response.body().getData());
            return response.body().getData();
        }
        return null;
    }

    public List<Offer> getActiveOffer()throws IOException {
        //LOG.info("getActiveHighLight:" + URLConnections.URL_GET_OFFER_ACTIVE);
        Call<GenericResponse<List<Offer>>> call = apiGateway.getOfferActive(URLConnections.URL_GET_OFFER_ACTIVE);
        Response<GenericResponse<List<Offer>>> response = call.execute();
        if (response.isSuccessful()) {
            //LOG.info("Response " + response.body().getData());
            return response.body().getData();
        }
        return null;
    }

    public Store getCloserCoordinates(Double latitude, Double longitude) throws IOException {
        //LOG.info("getCategoryActive: " + URLConnections.URL_GET_CLOSER_COORDINATES+"/"+ latitude+"/"+longitude);
        Map<String, String> pathVariables = new HashMap();
        pathVariables.put("latitude", String.valueOf(latitude));
        pathVariables.put("longitude", String.valueOf(longitude));
        String urlWithParams = Util.buildUrl(URLConnections.URL_GET_CLOSER_COORDINATES, pathVariables, null);

        Call<GenericResponse<Store>> call = apiGateway.getStoreCloserCoordinates(urlWithParams);
        Response<GenericResponse<Store>> response = call.execute();
        if (response.isSuccessful()) {
            //LOG.info("Response " + response.body().getData());
            return response.body().getData();
        }
        return null;
    }


    public List<Shortcut> getShortcutActive() throws IOException {
        //LOG.info("getCategoryActive:" + URLConnections.URL_GET_SHORTCUT_ACTIVE);
        try{
            Call<GenericResponse<List<Shortcut>>> call = apiGateway.getShortcutActive(URLConnections.URL_GET_SHORTCUT_ACTIVE);
            Response<GenericResponse<List<Shortcut>>> response = call.execute();
            if (response.isSuccessful()) {
                //LOG.info("Response " + response.body().getData());
                return response.body().getData();
            }
        }
        catch (Exception e){
            LOG.warning("Error getShortcutActive() -> " + e.getMessage());
        }

        return null;
    }

    public  ClientResponse putTokenByClientIdAndClientSecret(String clientId, String clientSecret){
        try {
            //LOG.info("method putTokenByClientIdAndClientSecret:" + URLConnections.URL_PUT_AUTH_BY_CLIENT_ID_SECRET);
            Call<GenericResponse<ClientResponse>> call = apiGateway.getTokenByClientIdAndClientSecret(URLConnections.URL_PUT_AUTH_BY_CLIENT_ID_SECRET, clientId, clientSecret);
            Response<GenericResponse<ClientResponse>> response = call.execute();
            if (response.isSuccessful()) {
                //LOG.info("Response " + response.body().getData());
                return response.body().getData();
            }
        }catch(Exception ex){
            LOG.warning("method putTokenByClientIdAndClientSecret not found: "+ ex.getMessage());
        }
        return null;
    }

    public List<ProviderRes> postCreateProvider(ProviderCreate providerCreate){
        try {
            //LOG.info("method postCreateProvider:" + URLConnections.URL_POST_CREATE_PROVIDER);
            Call<GenericResponse<ClientResponse>> call = apiGateway.postProviderCreate(URLConnections.URL_POST_CREATE_PROVIDER, providerCreate);
            Response<GenericResponse<ClientResponse>> response = call.execute();
            if (response.isSuccessful() && Objects.nonNull(response.body().getData())) {
                //LOG.info("Response " + response.body().getData());
                return (List<ProviderRes>) response.body().getData().getData();
            }
        }catch(Exception ex){
            LOG.warning("method postCreateProvider not create: "+ ex.getMessage());
        }
        return null;
    }

    public Item getItemByBarcodeAndStore(ItemReq itemReq){
        try {
            //LOG.info("method getItemByBarcodeAndStore:" + URLConnections.URL_GET_ITEM_BARCODE);
            Call<GenericResponse<Item>> call = apiGateway.getItemByBarcodeAndStore(URLConnections.URL_GET_ITEM_BARCODE, itemReq);
            Response<GenericResponse<Item>> response = call.execute();
            if (response.isSuccessful()) {
                //LOG.info("Response " + response.body().getData());
                return response.body().getData();
            }
        }catch(Exception ex){
            LOG.warning("method getItemByBarcodeAndStore not found: "+ ex.getMessage());
        }
        return null;
    }

    public Item getItemById(Long id){
        try {
            //LOG.info("method getItemById:" + URLConnections.URL_GET_ITEM_BY_ID);
            Map<String, String> pathVariables = new HashMap();
            pathVariables.put("id", String.valueOf(id));
            String urlWithParams = Util.buildUrl(URLConnections.URL_GET_ITEM_BY_ID, pathVariables, null);
            Call<GenericResponse<Item>> call = apiGateway.getItemById(urlWithParams);
            Response<GenericResponse<Item>> response = call.execute();
            if (response.isSuccessful()) {
                //LOG.info("Response " + response.body().getData());
                return response.body().getData();
            }
        }catch(Exception ex){
            LOG.warning("method getItemById not found: "+ ex.getMessage());
        }
        return null;
    }

    public List<Item> getItemSubstituteByIdAndStore(Long itemId, Integer storeId){
        try {
            List<Suggested> substituteList = APIAlgolia.getSubstitutesByItem(itemId);
            if (!substituteList.isEmpty()) {
                int idStoreGroup = APIAlgolia.getDefaultStoreIdByStoreId(storeId);
                return getItemsBySuggestedIdsAndStoreGroup(substituteList, idStoreGroup);
            }
        } catch (AlgoliaException e) {
            LOG.warning("method getItemSubstituteByIdAndStore not found: "+ e.getMessage());
        }
        return null;
    }


    private List<Item> getItemsBySuggestedIdsAndStoreGroup(List<Suggested> suggesteds, long idStoreGroup) {
        // log.warning("method getItemsBySuggestedIdsAndStoreGroup -> " + suggesteds + " storeid- > " + idStoreGroup);
        if(Objects.nonNull(suggesteds) && !suggesteds.isEmpty()) {
            List<ItemQuery> listItemQuery = suggesteds.stream().map(item -> new ItemQuery("" + item.getItem() + (idStoreGroup > 0 ? idStoreGroup : URLConnections.MAIN_ID_STORE))).collect(Collectors.toList());
            List<ItemAlgolia> itemAlgoliaList = APIAlgolia.findItemByIdList(listItemQuery);
            if(Objects.nonNull(itemAlgoliaList) && !itemAlgoliaList.isEmpty()){
                return itemAlgoliaList.stream().filter(itemAlgolia -> Objects.nonNull(itemAlgolia) && Objects.nonNull(itemAlgolia.getId()) && !itemAlgolia.getId().isEmpty())
                        .map(itemAlgolia -> APIAlgolia.getItemToItemAlgolia(new Item(), itemAlgolia)).collect(Collectors.toList());
            }
        }
        return null;
    }

    public List<OrderQuantityItem> getQuantityItemsByIdOrder(final Long idOrder){
        try {
            final String url = URLConnections.URL_OMS_GET_ORDER_QUANTITY_ITEM.replace("{orderId}", String.valueOf(idOrder));
            final Call<GenericResponse<OrderQuantityItemResp>> call = apiGateway.getQuantityItemsByIdOrder(url);
            Response<GenericResponse<OrderQuantityItemResp>> response = call.execute();
            if (response.body() != null && response.isSuccessful() && Objects.nonNull(response.body().getData())) {
                //LOG.info("la respuesta es correcta");
                return response.body().getData().getItemQuantity();
            }

        }catch (Exception e){
            LOG.severe("Ocurrio un error -> " + e.getMessage());
        }
        return null;
    }

    public List<ItemsOrderDomain> getInfoItemsByIdOrder(final Long idOrder){
        try {
            final String url = URLConnections.URL_OMS_GET_ORDER_INFO_ITEM.replace("{orderId}", String.valueOf(idOrder));
            final Call<GenericResponse<List<ItemsOrderDomain>>> call = apiGateway.getInfoItemsByIdOrder(url);
            Response<GenericResponse<List<ItemsOrderDomain>>> response = call.execute();
            if (response.body() != null && response.isSuccessful() && Objects.nonNull(response.body().getData())) {
                return response.body().getData();
            }

        }catch (Exception e){
            LOG.severe("Ocurrio un error -> " + e.getMessage());
        }

        return null;
    }


    public boolean sendEmailBraze(SendMailReq sendMailReq) {

        try {
            final String url = URLConnections.URL_SEND_EMAIL_BRAZE;
            final Call<SendBrazeEmailResp> call = apiGateway.sendMailBraze(url, sendMailReq);
            Response<SendBrazeEmailResp> response = call.execute();
            return response.isSuccessful();
        }catch (Exception e){
            LOG.severe("Ocurrio un error -> " + e.getMessage());
        }
        return false;
    }

    public ItemAlgolia createItemAlgoliaSag(String  barcode, String idStoreGroup) throws IOException {

        String urlWithParams = URLConnections.URL_CREATE_ITEM_ALGOLIA_SAG.replace("v1",barcode).replace("v2",idStoreGroup);
        //LOG.info("URL con parametros ->" + urlWithParams);
        Call<GenericResponse<ItemAlgolia>> call = apiGateway.createItemAlgolia(urlWithParams);
        Response<GenericResponse<ItemAlgolia>> response = call.execute();
        if (response.isSuccessful()) {
            return response.body().getData();
        } else {
            LOG.info(response.toString());
        }
        return null;
    }

    public Optional<String> getUUIDFromBrazeCreateUser(String email, String document, String phone){

        if (email == null || email.isEmpty()){
            return Optional.empty();
        }
        try {
            final String URI = URLConnections.GET_UUID_BRAZE_CREATE_USER;
            //LOG.info("URL con parametros__braze ->" + URI);

            CreateUserOnBrazeRequest request = new CreateUserOnBrazeRequest(email,document, phone);
            LOG.info("getUUIDFromBrazeCreateUser {document},{emial},{phone} -> " + document + "," + email + "," + phone);
            Call<CreateUserOnBrazeResponse> call = apiGateway.createUserBrazeByEmail(URI,request);

            Response<CreateUserOnBrazeResponse> response = call.execute();

            if (!response.isSuccessful()){
                return Optional.empty();
            }

            if (response.body() == null
                    || response.body().getData() == null
                    || response.body().getData().getAttributes() == null
                    || response.body().getData().getAttributes().isEmpty()){
                return Optional.empty();
            }

            Optional<AttributesBraze> optionalAttributesBraze = response.body().getData().getAttributes().stream().findFirst();

            if (!optionalAttributesBraze.isPresent()){
                return Optional.empty();
            }

            AttributesBraze attributesBraze = optionalAttributesBraze.get();

            if (attributesBraze.getExternalID() == null || attributesBraze.getExternalID().isEmpty()){
                return Optional.empty();
            }

            return Optional.of(attributesBraze.getExternalID());

        } catch (IOException e) {
            e.printStackTrace();
        }

        return Optional.empty();

    }

    public Optional<String> getUUIDFromBraze(final String email){
        if (email == null || email.isEmpty()){
            return Optional.empty();
        }

        try {
            final String URI = URLConnections.GET_UUID_BRAZE;
            //LOG.info("URL con getUUIDFromBraze ->" + URI);

            CreateUserOnBrazeRequest request = new CreateUserOnBrazeRequest(email);

            Call<GetUserByEmailBrazeResponse> call = apiGateway.getUserIdBrazeByEmail(URI,request);

            Response<GetUserByEmailBrazeResponse> response = null;

            response = call.execute();

            if (!response.isSuccessful()){
                return Optional.empty();
            }

            if (response.body() == null
                    || response.body().getData() == null
                    || response.body().getData().getBrazeId() == null
                    || response.body().getData().getBrazeId().isEmpty()){
                return Optional.empty();
            }

            String brazeId = response.body().getData().getBrazeId();
            return Optional.of(brazeId);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return Optional.empty();

    }

    public GeoCoderResponse geoCoder(AddressPredictionReq addressPredictionReq) {

        GeoCoderResponse geoCoderResponse = new GeoCoderResponse();

        try {
            if (addressPredictionReq != null) {
                //LOG.info("geo coder lupap-servi call -> " + addressPredictionReq.toStringJson());
                Call<GeoCoderResponse> call = apiGateway.geoCoder(URLConnections.GEOCODER,addressPredictionReq);
                LOG.info("request geocoder -> " + addressPredictionReq.toStringJson());
                Response<GeoCoderResponse> response = call.execute();
                if (response.isSuccessful()) {
                    LOG.info("executing retrofit call: response: " + response.raw());
                    geoCoderResponse = response.body();
                }

            }
        } catch (Exception e) {
            LOG.warning("error executing retrofit call: " + e.getMessage());
            e.printStackTrace();
        }

        return geoCoderResponse;
    }
    public GeoCoderResponse geoCoderV2(AddressPredictionReq addressPredictionReq) {

        GeoCoderResponse geoCoderResponse = new GeoCoderResponse();

        try {

            Call<GeoCoderResponse> call = apiGateway.geoCoder(URLConnections.GEOCODER_V2, addressPredictionReq);
            LOG.info("request geocoder -> " + addressPredictionReq.toStringJson());

            Response<GeoCoderResponse> response = call.execute();

            if (response.isSuccessful()) {
                //LOG.info("executing retrofit call: response: " + response.raw());
                geoCoderResponse = response.body();
            }

        } catch (Exception e) {
            LOG.warning("error executing retrofit call: " + e.getMessage());
            e.printStackTrace();
        }

        return geoCoderResponse;
    }
    public GeoCoderResponse geoCoderByLupapServi(AddressPredictionReq addressPredictionReq) {

        GeoCoderResponse geoCoderResponse = new GeoCoderResponse();

        try {

            Call<GeoCoderResponse> call = apiGateway.geoCoder(URLConnections.GEOCODER_BY_LUPAP_SERVI, addressPredictionReq);
            LOG.info("request geocoder -> " + addressPredictionReq.toStringJson());

            Response<GeoCoderResponse> response = call.execute();

            if (response.isSuccessful()) {
                //LOG.info("executing retrofit call: response: " + response.raw());
                geoCoderResponse = response.body();
            }

        } catch (Exception e) {
            LOG.warning("error executing retrofit call: " + e.getMessage());
            e.printStackTrace();
        }

        return geoCoderResponse;
    }

    public ReverseGeoRes geoInverse(ReverseGeoReq reverseGeoReq) {
        ReverseGeoRes reverseGeoRes = new ReverseGeoRes();

        try {
            if (reverseGeoReq != null) {
                Call<ReverseGeoRes> call = apiGateway.geoInverse(URLConnections.GEOCODER_INVERSO, reverseGeoReq);
                LOG.info("request geoinverse -> " + reverseGeoReq.toStringJson());
                Response<ReverseGeoRes> response = call.execute();
                if (response.isSuccessful()){
                    LOG.info("executing retrofit call: response: " + response.raw());
                    reverseGeoRes = response.body();
                }
            }
        } catch (IOException e) {
            LOG.warning("error executing retrofit call: " + e.getMessage());
            e.printStackTrace();
        }
        return reverseGeoRes;
    }

    public ReverseGeoRes geoInverseV2(ReverseGeoReq reverseGeoReq) {
        ReverseGeoRes reverseGeoRes = new ReverseGeoRes();

        try {
            if (reverseGeoReq != null) {
                Call<ReverseGeoRes> call = apiGateway.geoInverse(URLConnections.GEOCODER_INVERSO_V3, reverseGeoReq);
                LOG.info("request geoinverse -> " + reverseGeoReq.toStringJson());
                Response<ReverseGeoRes> response = call.execute();
                if (response.isSuccessful()){
                    //LOG.info("executing retrofit call: response: " + response.raw());
                    reverseGeoRes = response.body();
                }
            }
        } catch (IOException e) {
            LOG.warning("error executing retrofit call: " + e.getMessage());
            e.printStackTrace();
        }
        return reverseGeoRes;
    }

    public AutocompleteLupapRes lupapAutocomplete(AutocompleteLupapReq lupapRequest) {
        AutocompleteLupapRes lupapResponse = new AutocompleteLupapRes();

        try {

            if (lupapRequest != null && lupapRequest.getAddress() != null && lupapRequest.getCity() != null) {

                Call<AutocompleteLupapRes> call = apiGateway.lupapAutocomplete(URLConnections.AUTOCOMPLETE, lupapRequest);
                LOG.info("request autocomplete lupap -> " + lupapRequest.toStringJson());

                Response<AutocompleteLupapRes> response = call.execute();

                if (response.isSuccessful()) {
                    LOG.info("executing retrofit call: response: " + response.raw());
                    lupapResponse = response.body();
                } else {
                    LOG.info("Error autocomplete response body-> " + response.raw());
                }
            }
        } catch (Exception e) {
            LOG.warning("error executing retrofit call: " + e.getMessage());
            e.printStackTrace();
        }

        return lupapResponse;
    }

    public GeoCoderResponse geoCoderLupapPlaceId(final String placeId) throws IOException {

        if (placeId != null && !placeId.isEmpty()) {
            final String url = URLConnections.GEOCODER_LUPAP_PLACEID.replace("{placeId}",placeId);

            //LOG.info("PLACE ID CALL LUPAP -> " + url);

            final Call<GeoCoderResponse> call = apiGateway.geoCoderLupapPlaceId(url);
            final Response<GeoCoderResponse> response = call.execute();

            if (response.isSuccessful() && response.body() != null) {
                LOG.info("Response: ->" + response.body().toString());
                return response.body();
            }
        }

        return null;
    }

    public GeoCoderResponse geoCoderLupapPlaceIdV2(final String placeId) throws IOException {

        if (placeId != null && !placeId.isEmpty()) {
            final String url = URLConnections.GEOCODER_LUPAP_PLACEID_V2.replace("{placeId}", placeId);

            //LOG.info("PLACE ID CALL LUPAP -> " + url);

            final Call<GeoCoderResponse> call = apiGateway.geoCoderLupapPlaceId(url);
            final Response<GeoCoderResponse> response = call.execute();

            if (response.isSuccessful() && response.body() != null) {
                LOG.info("Response geoCoderLupapPlaceIdV2: ->" + response.body());
                return response.body();
            }
        }

        return null;

    }

    public void  updateStratumBraze(final String customerId) throws IOException {

        if (customerId != null && !customerId.isEmpty()) {
            final String url = URLConnections.URL_UPLOAD_STRATUM_BRAZE.replace("{customerId}",customerId);

            Call<GenericResponse<String>> call = apiGateway.updateStratumBraze(url);
            call.enqueue(new Callback<GenericResponse<String>>() {

                @Override
                public void onResponse(Call<GenericResponse<String>> call, Response<GenericResponse<String>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        LOG.info("Response NestUtilities: ->" + response.body().toString());
                    }
                }

                @Override
                public void onFailure(Call<GenericResponse<String>> call, Throwable t) {
                    LOG.warning("Error " + t.getMessage());
                }
            });


        }
    }

    public CancelOrderCourierRes  cancelOrder(OrderCourierCancelReq orderCourierCancelReq) throws IOException {

        CancelOrderCourierRes cancelOrderCourierRes = new CancelOrderCourierRes();

        try {
            if (orderCourierCancelReq != null) {
                LOG.info("method cancelOrderCourierReq -> " + orderCourierCancelReq.getOrderId());

                /*OrderCourierCancelReq courierCancelReq = new OrderCourierCancelReq();
                courierCancelReq.setOrderId(orderCourierCancelReq.getOrderId());*/

                Call<CancelOrderCourierRes> call = apiGateway.cancelOrderCourier(URLConnections.URL_OMS_ORDER_CANCEL, orderCourierCancelReq);
                Response<CancelOrderCourierRes> response = call.execute();
                if (response.isSuccessful()) {
                    LOG.info("executing retrofit call: response: " + response.raw());
                    cancelOrderCourierRes = response.body();
                }

            }
        } catch (Exception e) {
            LOG.warning("error executing retrofit call: " + e.getMessage());
            e.printStackTrace();
        }

        return cancelOrderCourierRes;
    }

    public void cancelStatusRx(CancelStatusRx cancelStatusRx){
        try{
            apiGateway.cancelStatusRx(URLConnections.URL_OMS_ORDER_CANCEL_RX ,cancelStatusRx).execute();
        }
        catch (Exception e){
            LOG.warning("Error cancelStatusRx " + e.getMessage());
        }
    }

    public Optional<LoginFirebaseRes> loginFirebaseByUid(LoginFirebaseReq loginFirebaseReq, final String traceId) throws IOException {

        if (loginFirebaseReq != null  && loginFirebaseReq.getUidFirebase() != null){
            final Call<LoginFirebaseRes> call = apiGateway.loginFirebaseByUid(URLConnections.URL_LOGINBYUID,loginFirebaseReq, traceId);
            final Response<LoginFirebaseRes> response = call.execute();

            if (response.isSuccessful() && response.body() != null) {
                return Optional.of(response.body());
            }

        }

        return Optional.empty();
    }

    /**
     * create Event Product Bought
     * @param request
     */
    public void createEventProductBougth(EventRequest request){
        if (request != null) {
            LOG.info("EVENTREQUEST -> " + request);
            Call<GenericResponse<String>> call = apiGateway.createEventProductBought(URLConnections.URL_AMPLITUDE_PRODUCT_BOUGHT, request);
            call.enqueue(new Callback<GenericResponse<String>>() {

                @Override
                public void onResponse(Call<GenericResponse<String>> call, Response<GenericResponse<String>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        LOG.info("Response createEvent: ->" + response.body().toString());
                    }
                }

                @Override
                public void onFailure(Call<GenericResponse<String>> call, Throwable t) {
                    LOG.warning("Error createEvent " + t.getMessage());
                }
            });
        }
    }

    /**
     * create Event Product Bought Async
     * @author Jhon Chaparro
     * @param request
     */
    public void createEventProductBoughtAsync(EventRequest request) {
        try {
            if (request != null) {
                LOG.info("createEventProductBoughtAsync -> " + request);
                Call<GenericResponse<String>> call = apiGateway.createEventProductBought(URLConnections.URL_AMPLITUDE_PRODUCT_BOUGHT, request);
                Response<GenericResponse<String>> response = call.execute();
                if (response.isSuccessful() && response.body() != null) {
                    Optional.of(response.body());
                }
            }
        }catch(Exception e){
            LOG.info("createEventProductBoughtAsync -> " + e);
        }
    }

    /**
     * create Event Order Completed
     * @author Jhon Chaparro
     * @param request
     */
    public void createEventOrderCompleted(EventRequest request){
        if (request != null) {
            LOG.info("EVENTREQUEST -> " + request);
            Call<GenericResponse<String>> call = apiGateway.createEventProductBought(URLConnections.URL_AMPLITUDE_ORDER_COMPLETE, request);
            call.enqueue(new Callback<GenericResponse<String>>() {
                @Override
                public void onResponse(Call<GenericResponse<String>> call, Response<GenericResponse<String>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        LOG.info("Response createEvent OrderComplete: ->" + response.body().toString());
                    }
                }

                @Override
                public void onFailure(Call<GenericResponse<String>> call, Throwable t) {
                    LOG.warning("Error createEvent OrderComplete " + t.getMessage());
                }
            });


        }
    }

    /**
     * create Event Order Completed Async
     * @author Jhon Chaparro
     * @param request
     */
    public void createEventOrderCompletedAsync(EventRequest request){
        try {
        if (request != null) {
            LOG.info("createEventOrderCompletedAsync -> " + request);
            Call<GenericResponse<String>> call = apiGateway.createEventProductBought(URLConnections.URL_AMPLITUDE_ORDER_COMPLETE, request);
            Response<GenericResponse<String>> response = call.execute();
            if (response.isSuccessful() && response.body() != null) {
                Optional.of(response.body());
            }
        }
        }catch(Exception e){
            LOG.info("createEventOrderCompletedAsync -> " + e);
        }
    }



    public Optional<BannersDTFRes> getBannerHome(Integer id, String email, String type, Integer category, String city, boolean isMobile){
        try {

             String url = URLConnections.URL_BANNERS_CMS_V2;

            int isMobileService = 0;

            if (isMobile){
                isMobileService = 1;
            }

            if (Objects.equals(0, id)){
                email = "undefined";
            }

            if (type == null || type.isEmpty()) {
                type = "MAIN_BANNER";
            }

            if (category == null){
                category = 0;
            }

            if (city == null || city.isEmpty()){
                city = "BOG";
            }

            url = url.replace("{city}",city)
                    .replace("{category}",category.toString())
                    .replace("{emailUser}",email)
                    .replace("{isMobile}",isMobile+"" );

            //LOG.info("method Banners URL ->" + url);
            Call<BannersDTFRes> call = apiGateway.getBannerV2(url);
            Response<BannersDTFRes> response = call.execute();

            if ( response.isSuccessful() && response.body() !=null ){
                return Optional.of(response.body() );
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public Optional<BannersDTFRes> getBannerHomeMinLeft(Integer id, String city, String email){
        try {

            String url = URLConnections.URL_BANNERS_MIN_LEFT_CMS_V2;


            if (Objects.equals(0, id)){
                email = "undefined";
            }

            if (city == null || city.isEmpty()){
                city = "BOG";
            }

            url = url.replace("{city}",city)
                    .replace("{emailUser}",email);

            //LOG.info("method Banners URL ->" + url);
            Call<BannersDTFRes> call = apiGateway.getBannerMinLefV2(url);
            Response<BannersDTFRes> response = call.execute();

            if ( response.isSuccessful() && response.body() !=null ){
                return Optional.of(response.body() );
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * get order in OMS
     * @param orderId
     * @return
     * @throws IOException
     */
    public OrderInfoAmplitude getOrderInfo(String orderId) throws IOException {
        if(orderId != null){
            final String url = URLConnections.URL_OMS_ORDER_INFO.replace("{orderId}",orderId);
            Call<OrderInfoResponse> call = apiGateway.getOrderInfo(url);
            final Response<OrderInfoResponse> response = call.execute();

            if(response.isSuccessful() && response.body() != null){
                return response.body().getData();
            }
        }
        //LOG.info("No se encontro informacion");
        return null;
    }

    public BusinessItem getClassificationBusinessItem(String idItem) {

        try {
            if (idItem != null) {
                final String url = URLConnections.URL_RMS_BUSINESS_ITEM.replace("{id}", idItem);
                Call<ClassificationBusiness<BusinessItem>> call = apiGateway.classificationBusinessItem(url);
                final Response<ClassificationBusiness<BusinessItem>> response = call.execute();

                if (response.isSuccessful() && response.body() != null) {
                    return response.body().getData();
                }
            }
        }catch (Exception e){
            LOG.warning("Error al consumir el servicio getClassificationBusinessItem" + e.getMessage());
        }

        return null;
    }

    public BusinessOrder getClassificationBusinessOrder(BusinessOrderRequest request) {

        try {
            if (request != null) {
                Call<ClassificationBusiness<BusinessOrder>> call = apiGateway.classificationBusinessOrder(URLConnections.URL_RMS_BUSINESS_ORDER, request);
                final Response<ClassificationBusiness<BusinessOrder>> response = call.execute();

                if (response.isSuccessful() && response.body() != null) {
                    return response.body().getData();
                }
            }
        } catch (Exception e) {
            LOG.warning("Error al consumir el servicio" + e.getMessage());
        }
        return null;
    }

    public void saveAmplitudeSessionId (AmplitudeSessionRequest request){

        if(request != null){

            Call<GenericResponse<String>> call = apiGateway.saveAmplitudeSessionId(URLConnections.URL_AMPLITUDE_SESSION_ID, request);
            call.enqueue(new Callback<GenericResponse<String>>() {

                @Override
                public void onResponse(Call<GenericResponse<String>> call, Response<GenericResponse<String>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        LOG.info("Response saveAmplitudeSessionId: ->" + response.body().toString());
                    }
                }

                @Override
                public void onFailure(Call<GenericResponse<String>> call, Throwable t) {
                    LOG.warning("Error saveAmplitudeSessionId" + t.getMessage());
                }
            });
        }
    }

    /**
     * Get services of order in oracle used for amplitude
     * @author: Jhon chaparro
     * @param orderId
     * @return
     * @throws IOException
     */
    public OrderInfoAmplitudeOMS getOrderInfoAmplitudeOMS(String orderId) throws IOException {
        if(orderId != null){
            final String url = URLConnections.URL_OMS_ORDER_INFO_AMPL.replace("{orderId}",orderId);
            Call<OrderInfoAmpOMSResponse> call = apiGateway.getOrderInfoAmplitudeOMS(url);
            final Response<OrderInfoAmpOMSResponse> response = call.execute();

            if(response.isSuccessful() && response.body() != null){
                return response.body().getData();
            }
        }
        //LOG.info("No se encontro informacion");
        return null;
    }

    public OrderInfoDataAmplitudeBraze getOrderInfoAmplitudeBraze(String orderId) throws IOException {
        if(orderId != null){
            final String url = URLConnections.URL_OMS_ORDER_INFO_AMPLITUDE_BRAZE.replace("{orderId}",orderId);
            Call<OrderInfoAmplitudeBraze> call = apiGateway.getOrderInfoAmplitudeBraze(url);
            final Response<OrderInfoAmplitudeBraze> response = call.execute();

            if(response.isSuccessful() && response.body() != null){
                return response.body().getData();
            }
        }
        //LOG.info("No se encontro informacion");
        return null;
    }


    public Boolean updateBrazeNotificationPreferences(NotificationAndEmailBrazeRequest request) throws ConflictException, IOException {
        final String url = URLConnections.URL_UPDATE_NOTIFICATION_PREFERENCES_BRAZE;
        final Call<GenericResponse<Boolean>> call = apiGateway.updateBrazeNotificationsPreferences(url, request);
        Response<GenericResponse<Boolean>> response = call.execute();
        if (response.isSuccessful() && response.body().getData()) {
            LOG.info("Preferencias de notificación en braze actualizadas satisfactoriamente -> user:" + request.getEmail());
        } else {
            LOG.severe("Ocurrio un problema al actualizar las preferencias de notificaciones en braze -> user:" + request.getEmail());
            throw new ConflictException("Ocurrio un problema al actualizar las preferencias de notificaciones en braze -> user:" + request.getEmail());
        }
        return response.isSuccessful();
    }

    public Boolean updateBrazeUserProfile(UpdateUserOnBrazeRequest request) throws BadRequestException, IOException {
        final String url = URLConnections.URL_UPDATE_USER_PROFILE_BRAZE;
        final Call<GenericResponse<Boolean>> call = apiGateway.updateBrazeUserProfile(url, request);
        Response<GenericResponse<Boolean>> response = call.execute();
        if (response.isSuccessful() && response.body().getData()) {
            return response.isSuccessful();
        } else {
            throw new BadRequestException("Ocurrio un problema al actualizar las preferencias de notificaciones en braze -> user:" + request.getFirstName());
        }
    }

    public NotificationBrazeRequest getBrazeNotificationPreferences(NotificationAndEmailBrazeRequest request) throws ConflictException, IOException {
        final String url = URLConnections.URL_UPDATE_NOTIFICATION_PREFERENCES_BRAZE;
        final Call<NotificationBrazeRequest> call = apiGateway.getBrazeNotificationsPreferences(url, request);
        Response<NotificationBrazeRequest> response = call.execute();
        if (response.isSuccessful() && response.body() != null) {
            LOG.info("Preferencias de notificación en braze encontradas satisfactoriamente -> user:" + request.getEmail());
        } else {
            LOG.severe("Ocurrio un problema al buscar las preferencias de notificaciones en braze -> user:" + request.getEmail());
            throw new ConflictException("Ocurrio un problema al buscar las preferencias de notificaciones en braze -> user:" + request.getEmail());
        }
        return response.body();
    }

    public Optional<Boolean> addNonStockItemInBraze(String email, String itemId){

        if (email == null || email.isEmpty() || itemId == null || itemId.isEmpty()){
            return Optional.empty();
        }
        try {
            final String URI = URLConnections.URL_ADD_NON_STOCK_ITEM_BRAZE;
            //LOG.info("URL con parametros__braze ->" + URI);

            AddNonStockItemBrazeRequest request = new AddNonStockItemBrazeRequest(email, itemId);

            Call<Boolean> call = apiGateway.addNonStockItemUserBraze(URI,request);

            Response<Boolean> response = call.execute();

            if (!response.isSuccessful()){
                return Optional.empty();
            }

            return Optional.of(response.body());

        } catch (IOException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public void sendPushNotificationBraze(PushNotificationRequest request) throws ConflictException, IOException {
        final String url = URLConnections.URL_SEND_PUSH_NOTIFICATION_BRAZE;
        LOG.info(new Gson().toJson(request));
        final Call<GenericResponse<Object>> call = apiGateway.sendPushNotificationBraze(url, request);
        Response<GenericResponse<Object>> response = call.execute();
        String message = Objects.nonNull(request.getBody()) ? request.getBody(): "No body";
        if (response.isSuccessful()) {
            LOG.info("Push enviado satisfactoriamente a" + request.getEmail() + " , message:" + message);
        } else {
            LOG.info(response.message());
            LOG.info("Ocurrio un problema al enviar el push notification -> user: " + request.getEmail());
        }
    }

    public CustomerResponseCart getCustomerCreditCardPrimeData(Long idCustomer) throws IOException, BadRequestException {
        //LOG.info("Inicia el llamado el servicio de Backend3");
        final String url = URLConnections.URL_CRM_GET_CUSTOMER_DATA_CREDIT_CARD.replace("{customerId}", String.valueOf(idCustomer));
        Call<GetCustomerPrimeCartResponse> call = apiGateway.getCustomerPrimeCart(url);
        Response<GetCustomerPrimeCartResponse> response = call.execute();

        if ( response.body() == null)  {
                LOG.warning("Error al consumir el servicio");
                throw new BadRequestException(Constants.CUSTOMER_NOT_FOUND);
        }
        return mapCustomerPrimeCart(Objects.requireNonNull(response.body()).getData());
    }

    public CustomerResponseCart isCustomerPrime(Long idCustomer) throws IOException, BadRequestException {
        final String url = URLConnections.URL_CRM_GET_IS_CUSTOMER_PRIME.replace("{customerId}", String.valueOf(idCustomer));
        Call<GetCustomerPrimeCartResponse> call = apiGateway.getCustomerPrimeCart(url);
        Response<GetCustomerPrimeCartResponse> response = call.execute();

        if ( response.body() == null)  {
            LOG.warning("Error al consumir el servicio");
            throw new BadRequestException(Constants.CUSTOMER_NOT_FOUND);
        }
        return mapCustomerPrimeCart(Objects.requireNonNull(response.body()).getData());
    }

    private CustomerResponseCart mapCustomerPrimeCart(CustomerCreditCardPrimeData data) {
        CustomerResponseCart customerResponseCart = new CustomerResponseCart();
        customerResponseCart.setActive(data.isActive());
        customerResponseCart.setSavings(data.getSavings());
        customerResponseCart.setMinimum_purchase(data.getMinimumPurchase());
        customerResponseCart.setMissing_purchase(data.getMissingPurchase());
        customerResponseCart.setSavingUserPrime(data.getSavingUserPrime());
        return customerResponseCart;
    }

    public CustomerPrimeSubscriptionDomainData getCustomerPrimeSubscription(Long customerId) throws IOException, BadRequestException {
        CustomerPrimeSubscriptionDomainData customerPrimeSubscription;
        String urlWithParams = URLConnections.URL_CRM_GET_CUSTOMER_PRIME.replace("{customerId}", "" + customerId);
        LOG.info("URL con parametros ->" + urlWithParams);
        Call<CustomerPrimeSubscriptionDomainRes> call = apiGateway.getCustomerPrimeSubscription(urlWithParams);
        Response<CustomerPrimeSubscriptionDomainRes> response = call.execute();
        if ( response.body() == null)  {
            LOG.warning("Error al consumir el servicio");
            throw new BadRequestException(Constants.CUSTOMER_NOT_FOUND);
        } else {
            customerPrimeSubscription = mapCustomerPrimeSubscription(response.body());
        }

        return customerPrimeSubscription;
    }

    public GenericResponse subscribePrimeFreeDays(final long userId, final CustomerPrimeFreeDays customerPrimeFreeDays) throws IOException, BadRequestException {
        String urlWithParams = URLConnections.URL_CRM_SUBSCRIBE_PRIME_FREE_DAYS;

        PrimeConfig primeConfigV2 = APIAlgolia.primeConfigV2();

        if (primeConfigV2 == null) {
            throw new BadRequestException("No se pudo obtener la configuración de Prime");
        }

        if (!primeConfigV2.freePrimeEnabled){
            throw new BadRequestException("No se puede suscribir a Prime Free Days, Feature Disabled");
        }

        if (primeConfigV2.deviceIdValidation){
            if (customerPrimeFreeDays.getDeviceId() == null || customerPrimeFreeDays.getDeviceId().isEmpty()){
                throw new BadRequestException("No se puede suscribir a Prime Free Days, DeviceId no puede ser null");
            }
        }

        String deviceId = customerPrimeFreeDays.getDeviceId() == null
                || customerPrimeFreeDays.getDeviceId().isEmpty() ? "" : customerPrimeFreeDays.getDeviceId();

        long freeDays = 30L;

        if (primeConfigV2.getFreeDaysForPrime() != null) {
            //LOG.info("Free days from algolia: " + primeConfigV2.getFreeDaysForPrime());
            freeDays = primeConfigV2.getFreeDaysForPrime();
        }

        SubscribePrimeFreeDaysRequest subscribePrimeFreeDaysRequest = new SubscribePrimeFreeDaysRequest(
                customerPrimeFreeDays.getCreditCardId(),
                freeDays,
                customerPrimeFreeDays.getPlanType(),
                userId
        );

        subscribePrimeFreeDaysRequest.setDeviceId(deviceId);

        //LOG.info("REQUEST CRM: " + new Gson().toJson(subscribePrimeFreeDaysRequest));
        Call<GenericResponse> call = apiGateway.subscribePrimeFreeDays(urlWithParams, subscribePrimeFreeDaysRequest);
        try {
            Response<GenericResponse> response = call.execute();

            if (!response.isSuccessful()) {

                if (response.errorBody() != null) {
                    String errorResponse = response.errorBody().string();
                    LOG.warning("Error RESPONSE CRM: " + errorResponse);

                    JsonObject jsonObject = JsonParser.parseString(errorResponse).getAsJsonObject();
                    String message = jsonObject.get("message").getAsString();
                    String code = jsonObject.get("code").getAsString();
                    throw new ConflictException(message,code);
                }

                LOG.warning("Error al consumir el servicio");
                throw new BadRequestException(Constants.CUSTOMER_NOT_FOUND);
            }
            return response.body();
        } catch (IOException e) {
            LOG.warning(e.getMessage());
            throw new BadRequestException(Constants.CUSTOMER_NOT_FOUND);
        } catch (ConflictException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateOrCreateSaving(SavingCustomer request) throws IOException, BadRequestException {
//        LOG.info("Inicia el llamado el servicio de Backend3"+request.getCustomerId()+""+request.getPrimeSaving()+""+request.getUpdateTypeSavingEnum());
        String urlWithParams = URLConnections.URL_CRM_PUT_SAVING_PRIME;
        Call<GenericResponse> call = apiGateway.updateCustomerSaving(urlWithParams, request);
        Response<GenericResponse> response = call.execute();
        if ( response.body() == null)  {
            throw new BadRequestException(Constants.CUSTOMER_NOT_FOUND);
        }
    }



    private CustomerPrimeSubscriptionDomainData mapCustomerPrimeSubscription(CustomerPrimeSubscriptionDomainRes data) {
        CustomerPrimeSubscriptionDomainData customerResponsePrime = new CustomerPrimeSubscriptionDomainData();
        customerResponsePrime.setActive(data.isActive());
        customerResponsePrime.setExpirationDate(data.getExpirationDate());
        customerResponsePrime.setSubscriptionDate(data.getSubscriptionDate());
        customerResponsePrime.setCurrentPlanSku(data.getCurrent_plan_sku());
        customerResponsePrime.setPaymentMethodUsed(data.getPayment_method_used());
        customerResponsePrime.setPaymentMethodCardMaskUsed(data.getPayment_method_card_mask_used());
        customerResponsePrime.setTotalSaved(data.getTotal_saved());
        customerResponsePrime.setPrimeId(data.getPrimeId());
        customerResponsePrime.setSavingCustomerNoPrime(data.getSavingCustomerNoPrime());
        customerResponsePrime.setFranchise(data.getFranchise());
        return customerResponsePrime;
    }

    public ValidFirstCouponRes validFirstCoupon(Long customerId) throws IOException, BadRequestException {
        ValidFirstCouponRes validFirstCouponRes;

        String urlWithParams = URLConnections.URL_OMS_VALID_COUPON.replace("{customerId}", "" + customerId);
        Call<ValidFirstCouponResData> call = apiGateway.validFirstCoupon(urlWithParams);
        Response<ValidFirstCouponResData> response = call.execute();
        LOG.info("response " + response.body());

        if ( response.body() == null)  {
            LOG.warning("Error al consumir el servicio");
            throw new BadRequestException(Constants.CUSTOMER_NOT_FOUND);
        } else {
            validFirstCouponRes = response.body().getData();
        }

        return validFirstCouponRes;
    }

    public RecommendResponse getTrendingItemsByDepartment(String department) throws ConflictException, IOException {
        final String url = URLConnections.URL_TRENDING_ITEMS + "?department=" + department;
        final Call<RecommendResponse> call = apiGateway.getTrendingItemsByDepartment(url);
        Response<RecommendResponse> response = call.execute();
        if (response.isSuccessful() && response.body() != null) {
            return response.body();
        } else {
            LOG.severe("Problema consultando trending items");
            throw new ConflictException("Problema consultando trending items");
        }
    }

    public void sendEventCreate(BrazeEventCreate request) throws IOException {
        final String url = URLConnections.URL_SEND_EVENT_BRAZE;
        LOG.info(new Gson().toJson(request));
        final Call<GenericResponse> call = apiGateway.sendEventCreate(url, request);
        Response<GenericResponse> response = call.execute();
        if (response.isSuccessful()) {
            LOG.info("Evento enviado satisfactoriamente a Braze");
        } else {
            LOG.info(response.message());
            LOG.info("Ocurrio un problema al enviar el evento a Braze");
        }

    }
    public void sendSavingNoPrime(SavingCustomer request)  {
        try {
            //LOG.info("Inicia el llamado el servicio de Backend3");
            final String url = URLConnections.URL_POST_SAVING_DATASTORE;
            Call<GenericResponse> call = apiGateway.updateCustomerSaving(url, request);
            Response<GenericResponse> response = call.execute();
            if (response.isSuccessful()) {
                LOG.info("Saving actualizado satisfactoriamente");
            } else {
                LOG.info("Ocurrio un problema al actualizar el saving");
            }
        }
        catch (Exception e) {
            LOG.severe("Error al consumir el servicio");
        }
    }

    public GenericResponse updateCustomerAtom (final DataAtomUtilities data) throws IOException {
        final String url = URLConnections.URL_UPDATE_ATOM;
        Call<GenericResponse> call = apiGateway.updateCustomerAtom(url, data);
        Response<GenericResponse> response = call.execute();
        return  response.body();
    }

    public CustomerFraudResponse searchFraudCustomer (final Long userId)  {
        if (userId == 0)
            return new CustomerFraudResponse(userId, false);

        Map<String, String> pathVariables = new HashMap<>();
        pathVariables.put("userId", String.valueOf(userId));

        Response<GetCustomerResponse<CustomerFraudResponse>> response;

        try {
            String urlWithParams = Util.buildUrl(URLConnections.URL_CRM_CUSTOMER_FRAUD, pathVariables, null);
            Call<GetCustomerResponse<CustomerFraudResponse>> call = apiGateway.searchFraudCustomer(urlWithParams);
            response = call.execute();
        }catch (Exception e){
            return new CustomerFraudResponse(userId, false);
        }

        if(response != null && response.isSuccessful() && response.body() != null && response.body().getData() != null){
            return response.body().getData();
        }else{
            return new CustomerFraudResponse(userId, false);
        }
    }

    //Micro charge

    public GenericResponse<Boolean> validAntifraud(Long idCustomer,Long numberCard,String source) throws IOException {
        final String url = URLConnections.URL_OMS__GET_ANTIFRAUDE_VALIDATE.replace("{idCustomer}", idCustomer.toString())
                .replace("{numberCard}",numberCard.toString());
        //LOG.info("method validAntifraud URL ->" + url);
        final Call<GenericResponse<Boolean>> call = apiGateway.getAntifraudValiate(url,source);
        final Response<GenericResponse<Boolean>> response = call.execute();
        if(!response.isSuccessful()){
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            GenericResponse errorResponse = gson.fromJson(response.errorBody().string(), GenericResponse.class);
            return errorResponse;
        }
        return response.body();
    }

    public GenericResponse<PayMicroCharge> generateMicroCharge(MicroCharge microCharge) throws IOException {
        final String url = URLConnections.URL_OMS_POST_MICRO_CHARGE_GENERATE;
        //LOG.info("method generateMicroCharge URL ->" + url+" : "+microCharge);
        final Call<GenericResponse<PayMicroCharge>> call= apiGateway.getGenerateMicroCharge(url,microCharge);
        final Response<GenericResponse<PayMicroCharge>> response = call.execute();
        if(!response.isSuccessful()){
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            GenericResponse errorResponse = gson.fromJson(response.errorBody().string(), GenericResponse.class);
            return errorResponse;
        }
        return response.body();
    }

    public GenericResponse<Boolean> validateMicroCharge(MicroCharge microCharge) throws IOException {
        final String url = URLConnections.URL_OMS_POST_MICRO_CHARGE_VALIDATE;
        LOG.info("method validateMicroCharge URL ->" + url+" : "+microCharge);
        final Call<GenericResponse<Boolean>>call = apiGateway.getValidateMicroCharge(url,microCharge);
        final Response<GenericResponse<Boolean>> response = call.execute();
        if(!response.isSuccessful()){
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            GenericResponse errorResponse = gson.fromJson(response.errorBody().string(), GenericResponse.class);
            return errorResponse;
        }
        return response.body();
    }
    public CustomerFraudResponse antifraudCreditCard (final Long creditCardId) throws IOException {
        Map<String, String> pathVariables = new HashMap<>();
        pathVariables.put("creditCardId", String.valueOf(creditCardId));
        String urlWithParams = Util.buildUrl(URLConnections.URL_CRM_CUSTOMER_CREDIT_CARD_FRAUD, pathVariables, null);
        Call<GetCustomerResponse<CustomerFraudResponse>> call = apiGateway.antifraudCreditCard(urlWithParams);
        Response<GetCustomerResponse<CustomerFraudResponse>> response = call.execute();
        if(response.isSuccessful()){
            return response.body().getData();
        }else{
            throw new IOException("Error al consumir el servicio antifraud credit card CRM");
        }
    }

    public ValidateCustomerOracle getCustomerOracle(final String email) throws IOException {
        String url = URLConnections.URL_CRM_VALIDATE_CUSTOMER.replace("{email}", email);
        Call<GetCustomerResponse<ValidateCustomerOracle>> call = apiGateway.getCustomerOracle(url);
        Response<GetCustomerResponse<ValidateCustomerOracle>> response = call.execute();

        return response.body().getData();
    }

    public Boolean isSamePasswordDataBase(final ValidatePasswordDataBase request){
        try {
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            final String url = URLConnections.URL_CRM_IS_SAME_PASSWORD;
            Call<ValidatePasswordDataBaseResponse> call = apiGateway.iSamePasswordDataBase(url, request);
            Response<ValidatePasswordDataBaseResponse> response = call.execute();
            if (Objects.nonNull(response.body()) && Objects.nonNull(response.body().getData())) {
                return response.body().getData();
            }else {
                return false;
            }
        }catch (Exception e){
            LOG.warning("Error getDataForLogin() -> " + e.getMessage());
            return false;
        }
    }

    public CustomerGoogle validateEmailGoogle(GoogleAuth request) {
        Response<CustomerGoogle> response = null;
        try {
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            final String url = URLConnections.URL_CRM_VALIDATE_CUSTOMER_GOOGLE;
            Call<CustomerGoogle> call = apiGateway.validateEmailGoogle(url, request);
            response = call.execute();
            if (Objects.nonNull(response.body()) && Objects.nonNull(response.body().getData())) {
                return response.body();
            } else {
                return response.body();
            }
        } catch (Exception e) {
            LOG.warning("Error getDataForLogin() -> " + e.getMessage());
            return response.body();
        }
    }

    public String getCodeLogin(String customerId) throws IOException {
        String url = URLConnections.URL_FIREBASE_GET_CODE_LOGIN.replace("{userId}", customerId);
        Call<FirebaseLoginCodeResponse> call = apiGateway.getCodeLoginSecure(url);
        Response<FirebaseLoginCodeResponse> response = call.execute();
        if(response.isSuccessful() && response.body() != null && response.body().getCodeLogin() != null ){
            return response.body().getCodeLogin();
        }else{
            throw new IOException("Error al consumir el getCodeLogin");
        }
    }

    public void addCodeLogin(String customerId, String codelogin) throws IOException {
        String url = URLConnections.URL_FIREBASE_ADD_CODE_LOGIN;
        AddFirebaseCodeLoginRequest addFirebaseCodeLoginRequest = new AddFirebaseCodeLoginRequest();
        addFirebaseCodeLoginRequest.setCode(codelogin);
        addFirebaseCodeLoginRequest.setCustomerId(customerId);
        Call<FirebaseLoginCodeResponse> call = apiGateway.addCodeLoginSecure(url, addFirebaseCodeLoginRequest);
        Response<FirebaseLoginCodeResponse> response = call.execute();
    }

    public void deleteCodeLogin(String customerId) throws IOException {
        String url = URLConnections.URL_FIREBASE_DELETE_CODE_LOGIN.replace("{userId}", customerId);
        Call<FirebaseLoginCodeResponse> call = apiGateway.deleteCodeLoginSecure(url);
        Response<FirebaseLoginCodeResponse> response = call.execute();
    }

    public void addDeliveryOrderItemAsync (final String query) throws IOException {
        String urlService = URLConnections.ADD_DELIVERY_ORDER_ITEM_ASYNC.concat("?" + query);
        Call<Void> call = apiGateway.addDeliveryOrderItemAsync(urlService);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                System.out.println("Respuesta");
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                System.out.println("Fallo");
            }
        } );
    }


    public GenericResponse<Long> validateOrderStatusOracle(final Long orderId) throws IOException {
        LOG.info("method validateLastOrderStatusOracle() orderId: #" + orderId);
        final String url = URLConnections.URL_VALIDATE_ORDER_STATUS_ORACLE.replace("{orderId}", String.valueOf(orderId));
        //LOG.info("Request to OMS -> " + url);
        final Call<GenericResponse<Long>> call = apiGateway.validateOrderLastStatusOracle(url);
        final Response<GenericResponse<Long>> response = call.execute();
        LOG.info("Response to OMS -> " + response);
        if(response.isSuccessful()){
            return response.body();
        }else{
            throw new IOException("Error al consumir el servicio de OMS -> validateLastStatus/{orderId}");
        }
    }

    public List<DiscountTalon> updateCustomerSession(CustomerSessionExternalRequest customerSessionRequest, String idSession){
        //LOG.info("Talon One method updateCustomerSession() idSession: #" + idSession);
        final String url = URLConnections.URL_TALON_ONE_CUSTOMER_SESSION.replace("{id}", idSession);
        //LOG.info("Request to Talon One -> " + url);
        try {
            final Call<List<DiscountTalon>> call = apiGateway.updateCustomerSession(url, customerSessionRequest);
            final Response<List<DiscountTalon>> response = call.execute();
            if(response.isSuccessful())
                return response.body();
        }catch(Exception e){
            LOG.warning("Error al consumir el servicio de Talon One -> talonone/customer-session/{id}"+Arrays.toString(e.getStackTrace()));
        }
        return new ArrayList<DiscountTalon>();
    }

    public TrackEventResponse getTrackEventCustom(TrackEventRequest trackEventRequest) {
        //LOG.info("Talon One method getTrackEventCustom(): #");
        final String url = URLConnections.URL_TALON_ONE_TRACK_EVENT;
        //LOG.info("Request to Talon One -> " + url);
        try {
            final Call<TrackEventResponse> call = apiGateway.getTrackEventCustom(url, trackEventRequest);
            final Response<TrackEventResponse> response = call.execute();
            if (response.isSuccessful())
                return response.body();
        } catch (Exception e) {
            LOG.warning("Error al consumir el servicio de Talon One -> talonone/track-event/get " + Arrays.toString(e.getStackTrace()));
        }
        return new TrackEventResponse();
    }

    public TrackEventResponse getTrackEventPurchased(TrackEventItemPurchasedRequest trackEventRequest){
        //LOG.info("Talon One method getTrackEventPurchased(): #");
        final String url = URLConnections.URL_TALON_ONE_TRACK_EVENT_ITEM_PURCHASED;
        LOG.info("Request to Talon One -> " + url + " request -> " + trackEventRequest.toStringJson());
        try{
            final Call<TrackEventResponse> call = apiGateway.getTrackEventItemPurchased(url, trackEventRequest);
            final Response<TrackEventResponse> response = call.execute();
            if(response.isSuccessful())
                return response.body();
        }catch (Exception e){
            LOG.warning("Error al consumir el servicio de Talon One -> talonone/track-event/getTrackEventPurchased"+Arrays.toString(e.getStackTrace()));
        }
        return new TrackEventResponse();
    }
    public void createEventOrderCompletedV2(final String orderId) throws IOException {
        LOG.info("method createEventOrderCompletedV2() orderId: #" + orderId);
        final String url = URLConnections.URL_AMPLITUDE_ORDER_COMPLETE_V2;
        //LOG.info("Request to utilitis -> " + url);
        final Call<AmplitudeEventsResponse> call = apiGateway.orderCompletedV2(url, new OrderCompletedV2Request(orderId));
        final Response<AmplitudeEventsResponse> response = call.execute();
        if(!response.isSuccessful()){
            LOG.info("method sendEventOrderComplete -> La respuesta de utilities no es satisfactoria, Response utilities -> "+ response.toString());
        }
    }

    public String getTokenFirebase(){
        //LOG.info("method getTokenFirebase()");
        final String url = URLConnections.URL_OMS_GET_TOKEN_FIREBASE;
        try{
            final Call<GenericResponse<String>> call = apiGateway.getTokenFirebase(url);
            final Response<GenericResponse<String>> response = call.execute();
            if(response.isSuccessful() && Objects.nonNull(response.body()) && Objects.nonNull(response.body().getData()))
                return response.body().getData();
        }catch (Exception e){
            LOG.warning("Error al consumir el servicio para traer el token de firebase method getTokenFirebase() error: " + Arrays.toString(e.getStackTrace()));
        }
        return null;
    }

    public GenericResponse<String> findCustomerDaneCodeCityByIdCity(final String idCity){

        try{
            //LOG.info("IN -> findCustomerDaneCodeCityByIdCity() -> " + idCity);
            final String url = URLConnections.URL_CRM_FIND_CUSTOMER_DANE_CODE_CITY_BY_ID_CITY
                                    .replace("{idCity}", String.valueOf(idCity));
            //LOG.info("URL -> findCustomerDaneCodeCityByIdCity() -> " + url);
            final Call<GenericResponse<String>> call = apiGateway.findCustomerDaneCodeCityByIdCity(url);
            final Response<GenericResponse<String>>  response = call.execute();
            if(!response.isSuccessful()){
                GsonBuilder gsonBuilder = new GsonBuilder();
                Gson gson = gsonBuilder.create();
                GenericResponse errorResponse = gson.fromJson(response.errorBody().string(), GenericResponse.class);
                LOG.info("error -> findCustomerDaneCodeCityByIdCity() -> " + response.errorBody().string());
                return errorResponse;
            }
            //LOG.info("OUT -> findCustomerDaneCodeCityByIdCity() -> " + response.body().getData());
            return response.body();
        }catch (IOException e){
            LOG.warning("Error findCustomerDaneCodeCityByIdCity() -> " + e.getMessage());
            return null;
        }
    }

    public Optional<String> getCourierUuid(Long orderId){
        final String url = URLConnections.URL_OMS_GET_COURIER_UUID.replace("{orderId}", String.valueOf(orderId));
        try{
            final Response<String> response = apiGateway.getCourierUuid(url).execute();
            if(response.isSuccessful() && Objects.nonNull(response.body())) {
                return Optional.of(response.body());
            }
        }catch (Exception e){
            LOG.warning("Error al consumir el servicio para traer el uuid del courrier order getCourierUuid() error: " + e.getMessage());
            return Optional.empty();
        }
        return Optional.empty();
    }

    public DeliveryOrderOms getOrderOMS(String orderId) {
        final String url = URLConnections.URL_OMS_GET_ORDER_DELIVERY.replace("{orderId}", orderId);
   
        try {
            final Call<GenericResponse<DeliveryOrderOms>> call = apiGateway.getOrderOMS(url);
            final Response<GenericResponse<DeliveryOrderOms>> response = call.execute();

            if (response.isSuccessful()) {
                GenericResponse<DeliveryOrderOms> responseBody = response.body();
                if (responseBody != null && responseBody.getData() != null) {
                    return responseBody.getData();
                } else {
                    LOG.warning("La respuesta fue exitosa, pero el cuerpo o los datos de la respuesta están vacíos para orderId: " + orderId);
                }
            } else {
                LOG.warning("Respuesta no exitosa del servicio OMS para orderId: " + orderId + ", código de estado: " + response.code());
            }
        } catch (IOException e) {
            LOG.severe("Error de IO al consumir el servicio OMS para orderId: " + orderId + ": " + e.getMessage());
        } catch (Exception e) {
            LOG.warning("Error inesperado al consumir el servicio OMS para orderId: " + orderId + ": " + e.getMessage());
        }
        return null;
    }

    public GeoCoderResponse validateGeoZone(ValidateGeoZoneReq validateGeoZoneReq) {

        GeoCoderResponse geoCoderResponse = new GeoCoderResponse();

        try {

            Call<GeoCoderResponse> call = apiGateway.validateGeoZone(URLConnections.GEO_ZONE_BY_LAT_LNG, validateGeoZoneReq);
            LOG.info("request validateGeoZone -> " + validateGeoZoneReq.toString());

            Response<GeoCoderResponse> response = call.execute();
            if (response.isSuccessful()) {
                //LOG.info("executing retrofit call: response: " + response.raw());
                geoCoderResponse = response.body();
                LOG.info("request geoCoderResponse -> " + response.body().toString());
            }

        } catch (Exception e) {
            LOG.warning("error executing retrofit call: " + e.getMessage());
            e.printStackTrace();
        }

        return geoCoderResponse;
    }



    public GenericResponse<GetOrdersOMSResponse> releaseActiveOrders(long idCustomer) throws IOException {
        // Build the JSON payload
        GetOrdersPayloadOMS payloadOMS = new GetOrdersPayloadOMS();
        payloadOMS.setIdCustomer(idCustomer);

        // Define the endpoint URL
        String url = URLConnections.URL_OMS_RELEASE_ACTIVE_ORDERS;

        // Make the POST request
        Call<GenericResponse<GetOrdersOMSResponse>> call = apiGateway.postReleaseOrders(url, payloadOMS);
        Response<GenericResponse<GetOrdersOMSResponse>> response = call.execute();

        if (!response.isSuccessful()) {
            throw new IOException("Failed to release active orders: " + response.code() + " - " + response.message());
        }

        return response.body() != null ? response.body() : new GenericResponse<>();
    }

    public GenericResponse<GetOrdersOMSResponse> releasePreviousOrders(long idCustomer, String cursor) throws IOException {
        // Build the JSON payload
        GetOrdersPayloadOMS payloadOMS = new GetOrdersPayloadOMS();
        payloadOMS.setIdCustomer(idCustomer);
        payloadOMS.setCursor(cursor);

        // Define the endpoint URL
        String url = URLConnections.URL_OMS_RELEASE_PREVIOUS_ORDERS;

        // Make the POST request
        Call<GenericResponse<GetOrdersOMSResponse>> call = apiGateway.postReleaseOrders(url, payloadOMS);
        Response<GenericResponse<GetOrdersOMSResponse>> response = call.execute();

        if (!response.isSuccessful()) {
            throw new IOException("Failed to release previous orders: " + response.code() + " - " + response.message());
        }

        return response.body() != null ? response.body() : new GenericResponse<>();
    }
}
