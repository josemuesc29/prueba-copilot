package com.imaginamos.farmatodo.networking.api;

import com.imaginamos.farmatodo.networking.models.addresses.ReverseGeoReq;
import com.imaginamos.farmatodo.networking.models.addresses.ReverseGeoRes;
import com.imaginamos.farmatodo.networking.models.addresses.GeoCoderResponse;
import com.imaginamos.farmatodo.networking.models.addresses.AddressPredictionReq;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiServinformacion {

    @Headers("Authorization: Token 4279BRY0LPKYQSA59Y7SOLU04XKYGF")
    @POST("multizonificador/geocoder/")
    Call<GeoCoderResponse> postGeocoder(@Body AddressPredictionReq siAddressReq);

    @Headers("Authorization: Token 4279BRY0LPKYQSA59Y7SOLU04XKYGF")
    @POST("multizonificador/geoinverso/")
    Call<ReverseGeoRes> postReverseGeocoder(@Body ReverseGeoReq reverseGeoReq);

}
