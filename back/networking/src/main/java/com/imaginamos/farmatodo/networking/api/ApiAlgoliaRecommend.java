package com.imaginamos.farmatodo.networking.api;

import com.imaginamos.farmatodo.model.algolia.RecommendRequest;
import com.imaginamos.farmatodo.model.algolia.RecommendResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface ApiAlgoliaRecommend {

    @Headers({"X-Algolia-Api-Key: e6f5ccbcdea95ff5ccb6fda5e92eb25c","X-Algolia-Application-ID: VCOJEYD2PO"})
    @POST
    Call<RecommendResponse> recommendAPI(@Url String url, @Body RecommendRequest request);

}
