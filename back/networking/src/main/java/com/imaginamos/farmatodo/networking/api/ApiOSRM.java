package com.imaginamos.farmatodo.networking.api;

import com.imaginamos.farmatodo.networking.models.addresses.osrm.RouteOSRMResponse;
import com.imaginamos.farmatodo.networking.models.addresses.osrm.TableOSRMResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiOSRM {

    @Headers("Content-Type: application/json")
    @GET("route/v1/car/{longitudeOrigin},{latitudeOrigin};{longitudeDestination},{latitudeDestination}")
    Call<RouteOSRMResponse> getRoute(@Path("longitudeOrigin") Double longitudeOrigin,
                                     @Path("latitudeOrigin") Double latitudeOrigin,
                                     @Path("longitudeDestination") Double longitudeDestination,
                                     @Path("latitudeDestination") Double latitudeDestination);

    @GET("table/v1/car/{coordinates}")
    Call<TableOSRMResponse> getDistanceMatrix(@Path("coordinates") String coordinates,
                                              @Query("annotations") String annotations);
}
