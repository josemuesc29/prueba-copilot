package com.imaginamos.farmatodo.backend.firebase.api;

import com.google.gson.JsonObject;
import com.imaginamos.farmatodo.backend.firebase.models.AuthenticationRequest;
import com.imaginamos.farmatodo.backend.firebase.models.AuthenticationResponse;
import com.imaginamos.farmatodo.backend.firebase.models.NewOrderPrimePSEMixed;
import retrofit2.Call;
import retrofit2.http.*;

public interface IFirebase {

    @PATCH
    @Headers({"Content-Type: application/json"})
    Call<Void> notifyLoginNewCode(@Url String location, @Body JsonObject request);

    @GET
    Call<String> loginCode(@Url String location);


    @DELETE
    Call<Void> deleteLoginCode(@Url String location);

    @PATCH
    Call<Void> setOrderPrimeMixed(@Url String location, @Body NewOrderPrimePSEMixed request);

    @POST
    @Headers({"Content-Type: application/json"})
    Call<AuthenticationResponse> authenticateClient(@Url String location, @Body AuthenticationRequest request);

}
