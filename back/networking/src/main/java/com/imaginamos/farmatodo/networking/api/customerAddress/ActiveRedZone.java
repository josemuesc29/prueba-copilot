package com.imaginamos.farmatodo.networking.api.customerAddress;

import com.imaginamos.farmatodo.model.customer.ActiveRedZoneResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ActiveRedZone {
    @GET("catalog/r/CO/v1/cities/active/red-zone/{cityId}")
    Call<ActiveRedZoneResponse> getActiveRedZoneData(@Path("cityId") String cityId);
}
