package com.imaginamos.farmatodo.networking.api;

import com.imaginamos.farmatodo.networking.models.addresses.GAutocompleteRes;
import com.imaginamos.farmatodo.networking.models.addresses.GGeoCodeRes;
import com.imaginamos.farmatodo.networking.models.addresses.GGeoReverseRes;
import com.imaginamos.farmatodo.networking.models.addresses.GPlaceIdRes;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiGeoGoogle {

    @GET("place/autocomplete/json")
    Call<GAutocompleteRes> getAutocomplete(@Query("input") String text, @Query("key") String apiKey, @Query("type") String type, @Query("components") String components);

    @GET("place/autocomplete/json")
    Call<GAutocompleteRes> getAutocomplete(@Query("input") String text,    @Query("key") String apiKey,
                                           @Query("type") String type,     @Query("location") String location,
                                           @Query("radius") String radius, @Query("params") String params,
                                           @Query("components") String components);

    @GET("place/details/json")
    Call<GPlaceIdRes> getPlaceById(@Query("placeid") String placeId, @Query("key") String apiKey);

    @GET("geocode/json")
    Call<GGeoReverseRes> getGeoReverse(@Query("latlng") String latlng, @Query("key") String apiKey);

    @GET("geocode/json")
    Call<GGeoCodeRes> getGeoCode(@Query("address") String address, @Query("key") String apiKey);

}
