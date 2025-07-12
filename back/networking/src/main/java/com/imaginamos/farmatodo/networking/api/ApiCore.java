package com.imaginamos.farmatodo.networking.api;

import com.imaginamos.farmatodo.model.customer.*;
import com.imaginamos.farmatodo.model.order.*;
import com.imaginamos.farmatodo.model.payment.OrderChargeRes;
import com.imaginamos.farmatodo.model.util.URLConnections;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

/**
 * Created by ccrodriguez
 */

public interface ApiCore {

    @PUT
    Call<OrderChargeRes> putOrderCharge(@Url String url,@Body OrderCharge orderCharge);

    @PUT
    Call<OrderEditRes> putEditOrder(@Url String url,@Body OrderEdit orderEdit);

    @POST
    Call<AnswerGetUserOrigin> postUserOrigin(@Url String url,@Body RequestGetUserOrigin userOriginRequest);

    @GET
    Call<OrderInfoStatus> getOrderTrackingInfo(@Url String url);

    @GET
    Call<CustomerData> getCustomerOnly(@Url String url);

    @GET
    Call<CustomerOnlyData> getCustomer(@Url String url);

    @GET
    Call<CustomerJSON> getAllDataCustomer(@Url String url);

    @GET
    Call<Suggesteds> getSuggestsBySegment(@Url String url);

    @POST
    Call<AnswerGetUserOrigin> postUserOriginPhone(@Url String url,@Body RequestGetUserOrigin userOriginRequest);

    @POST
    Call<AnswerGetUserOrigin> postUserOriginSocialNetwork(@Url String url,@Body RequestGetUserOrigin userOriginRequest);

    @POST
    Call<AnswerEmailValidate> validateCustomerConection(@Url String url,@Body RequestEmailValidate requestEmailValidate);

    @GET
    Call<AddressesRes> getAddressesByCudtomerId(@Url String url);

    @POST
    Call<Address> createAddress(@Url String url,@Body AddAddressRequest addAddressRequest);

    @DELETE
    Call<Void> deleteAddress(@Url String url);

    @DELETE
    Call<Void> deleteCreditCardByCustomer(@Url String url);

    @PUT
    Call<Void> updateAddress(@Url String url,@Body UpdateAddressRequest addressRequest);

    @GET
    Call<List<CreditCard>> getCreditCardByCustomer(@Url String url);

    @PUT
    Call<OrderCourier> putEditOrderCourier(@Url String url,@Body OrderEdit orderEdit);

    @GET
    Call<OrderInfoStatus> getOrder15TrackingInfo(@Url String url);

    @POST
    Call<CreateOrderResponseCore> createOrder(@Url String url,@Body CreateOrderRequestCore requestCore);


    @POST
    Call<CreateOrderSubscribeData> createOrderSubscription(@Url String url, @Body CreateOrderSubscribeReq createOrderSubscribeReq);


    //@POST(URLConnections.URL_VALIDATE_STOCK_API)
    //Call<ValidateStockRouteRes> validateStock(@Body ValidateStockRouteReq validateStockRouteReq);

    //@POST(URLConnections.URL_ORDER_VALIDATE_V2)
    //Call<OrderJson> validateOrder(@Body ValidateOrderReq validateOrderReq);

    @GET
    Call<CoreEventResponse> pingRMS(@Url String url);

    @POST
    Call<CoreEventResponse> createFulfilOrdColDesc(@Url String url, @Body FulfilOrdColDescDomain request);

    @GET
    Call<CoreEventResponse> pingSIM(@Url String url);

    @POST
    Call<CoreEventResponse> createFulfillmentOrderDetail(@Url String url, @Body FulfilOrdColDescDomain request);

}
