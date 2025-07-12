package com.imaginamos.farmatodo.networking.talonone.api;

import com.imaginamos.farmatodo.networking.talonone.model.*;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.Map;

public interface ITalonOne {

    @PUT("customer-session/{sessiontId}")
    Call<CustomerSessionResponse> createCustomerSession(@Path("sessiontId") String sessiontId, @Body CustomerSessionRequest customerSessionRequest, @HeaderMap Map<String, String> headers);

    @POST("customer-session/copy/{sessiontId}/{newSessiontId}/{state}")
    Call<CustomerSessionResponse> copyCustomerSession(@Path("sessiontId") String sessiontId, @Path("newSessiontId") String newSessiontId, @Path("state") String state, @HeaderMap Map<String, String> headers);

    @PUT("customer_profile/{sessiontId}")
    Call<Object> createCustomer(@Path("sessiontId") String sessiontId, @Body CustomerProfileRequest customerProfileRequest, @HeaderMap Map<String, String> headers);

    @POST("discount/persist")
    Call<Object> persistDiscounts(@Body TalonOneDiscount talonOneDiscount);

    @POST("discount/saveDeduct")
    Call<Object> deductDiscount(@Body TalonOneDeductDiscount talonOneDeductDiscount);

    @GET
    Call<TalonOneDeductDiscount> retrieveDeductDiscount(@Url String url);

}
