package com.imaginamos.farmatodo.networking.api;

import com.imaginamos.farmatodo.model.home.BannersDTFRes;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface ApiCMS {
    @GET
    Call<BannersDTFRes> getBannersCMS(@Url String url,
                                      @Query("emailUser") String email,
                                      @Query("type") String typeBanner ,
                                      @Query("category") Integer category,
                                      @Query("city") String city,
                                      @Query("isMobile") Integer mobile );
}
