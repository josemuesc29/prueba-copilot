package com.imaginamos.farmatodo.networking.services;

import com.google.api.server.spi.response.BadRequestException;
import com.imaginamos.farmatodo.model.customer.ActiveRedZoneResponse;
import com.imaginamos.farmatodo.model.customer.RedZone;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.networking.api.customerAddress.ActiveRedZone;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

public class ActiveRedZoneService extends ApiGateway<ActiveRedZone> {

    private static final Logger LOG = Logger.getLogger(ActiveRedZoneService.class.getName());

    private static final String BASE_URL = Constants.URL_TRANSACTIONAL;
    private static Retrofit retrofit = null;

    private static ActiveRedZoneService instance;

    protected ActiveRedZoneService() {
        super(ActiveRedZone.class);
    }

    public static ActiveRedZoneService get() {
        if (instance == null) {
            synchronized (ActiveRedZoneService.class) {
                if (instance == null) {
                    instance = new ActiveRedZoneService();
                }
            }
        }
        return instance;
    }

    private static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public Optional<List<RedZone>> fetchRedZoneData(String cityId) throws BadRequestException, IOException {
        ActiveRedZone apiService = getClient().create(ActiveRedZone.class);

        Call<ActiveRedZoneResponse> call = apiService.getActiveRedZoneData(cityId);

        ActiveRedZoneResponse apiResponse = executeApiCall(call);
        if(Objects.isNull(apiResponse) || Objects.isNull(apiResponse.getData())){
            LOG.warning("fetchRedZoneData: empty response for cityId: " +cityId);
            return Optional.empty();
        }

        return Optional.ofNullable(apiResponse.getData().getRedZones());
    }
}
