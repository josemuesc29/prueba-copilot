package com.imaginamos.farmatodo.networking.services;

import com.imaginamos.farmatodo.networking.api.ApiOSRM;
import com.imaginamos.farmatodo.networking.base.ApiBuilder;
import com.imaginamos.farmatodo.networking.models.addresses.osrm.RouteOSRMResponse;
import com.imaginamos.farmatodo.networking.models.addresses.osrm.TableOSRMResponse;
import retrofit2.Call;
import retrofit2.Response;

import java.util.Optional;
import java.util.logging.Logger;

public class OSRMService {
    private static final Logger LOG = Logger.getLogger(OSRMService.class.getName());

    private static OSRMService instance;

    private ApiOSRM apiOSRM;

    private OSRMService() {
        apiOSRM = ApiBuilder.get().createOSRMService(ApiOSRM.class);
    }

    public static OSRMService get() {
        if (instance == null) instance = getSync();
        return instance;
    }

    private static synchronized OSRMService getSync() {
        if (instance == null) instance = new OSRMService();
        return instance;
    }

    public Optional<RouteOSRMResponse> getORSMRoute(Double lngOrigin,
                                                    Double latOrigin,
                                                    Double lngDestination,
                                                    Double latDestination) {
        if (lngOrigin == null || latOrigin == null || lngDestination == null || latDestination == null)
            return Optional.empty();

        Call<RouteOSRMResponse> call = apiOSRM.getRoute(lngOrigin, latOrigin, lngDestination, latDestination);

        try {
            Response<RouteOSRMResponse> response = call.execute();
            if (response.isSuccessful()) {
                return Optional.ofNullable(response.body());
            } else {

                if (response.errorBody() != null) {
                    LOG.warning("Error getORSMRoute() -> "+response.errorBody().string());
                }
            }
        } catch (Exception e) {
            LOG.warning("Error getORSMRoute() -> "+e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Retrieves a distance matrix for multiple coordinates using OSRM's table service.
     *
     * @param coordinates A semicolon-separated string of longitude,latitude pairs
     *                   Format: "lon1,lat1;lon2,lat2;lon3,lat3"
     * annotations Type of annotations to return. Valid values: "duration", "distance"
     * @return A Call object containing the TableOSRMResponse
     * @throws IllegalArgumentException if coordinates format is invalid
     */

    public Optional<TableOSRMResponse> getDistanceMatrix(String coordinates) {
        if (coordinates == null || coordinates.trim().isEmpty()) {
            LOG.warning("Invalid coordinates parameter: null or empty");
            return Optional.empty();
        }

        if (!coordinates.matches("^(-?\\d+\\.\\d+,-?\\d+\\.\\d+;?)+$")) {
            LOG.warning("Invalid coordinates format: " + coordinates);
            return Optional.empty();
        }
        try {
            Call<TableOSRMResponse> call = apiOSRM.getDistanceMatrix(coordinates, "distance");
            Response<TableOSRMResponse> response = call.execute();
            if (response.isSuccessful() && response.body() != null) {
                return Optional.of(response.body());
            }
            if (response.errorBody() != null) {
                LOG.warning("Error response from OSRM: " + response.errorBody().string());
            }
        } catch (Exception e) {
            LOG.warning("Error getting OSRM distance matrix: " + e.getMessage());
        }
        return Optional.empty();
    }
}
