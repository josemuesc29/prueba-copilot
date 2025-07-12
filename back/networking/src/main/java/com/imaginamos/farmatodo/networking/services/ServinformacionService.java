package com.imaginamos.farmatodo.networking.services;

import com.imaginamos.farmatodo.networking.api.ApiServinformacion;
import com.imaginamos.farmatodo.networking.base.ApiBuilder;
import com.imaginamos.farmatodo.networking.models.addresses.ReverseGeoReq;
import com.imaginamos.farmatodo.networking.models.addresses.ReverseGeoRes;
import com.imaginamos.farmatodo.networking.models.addresses.GeoCoderResponse;
import com.imaginamos.farmatodo.networking.models.addresses.AddressPredictionReq;
import retrofit2.Call;
import retrofit2.Response;

import java.util.logging.Logger;

public class ServinformacionService {

    private static final Logger LOG = Logger.getLogger(ServinformacionService.class.getName());

    private static ServinformacionService instance;

    private ApiServinformacion apiServinformacion;

    private ServinformacionService() {
        apiServinformacion = ApiBuilder.get().createServinfomacionService(ApiServinformacion.class);
    }

    public static ServinformacionService get() {
        if (instance == null) instance = getSync();
        return instance;
    }

    private static synchronized ServinformacionService getSync() {
        if (instance == null) instance = new ServinformacionService();
        return instance;
    }

    public GeoCoderResponse postAddressPrediction(AddressPredictionReq addressReq) {
        Call<GeoCoderResponse> call = apiServinformacion.postGeocoder(addressReq);
        GeoCoderResponse geoCoderResponse = new GeoCoderResponse();
        try {
            Response<GeoCoderResponse> resResponse = call.execute();
            LOG.warning("executing retrofit call: response: " + resResponse.raw());
            geoCoderResponse = resResponse.body();
            LOG.warning("executing retrofit call: " + geoCoderResponse);
        } catch (Exception e) {
            LOG.warning("error executing retrofit call: " + e.getMessage());
        }
        return geoCoderResponse;
    }

    public ReverseGeoRes postReverseGeoCoding(ReverseGeoReq reverseGeoReq) {

        Call<ReverseGeoRes> call = apiServinformacion.postReverseGeocoder(reverseGeoReq);
        ReverseGeoRes reverseGeoRes = new ReverseGeoRes();
        try {
            Response<ReverseGeoRes> response = call.execute();
            reverseGeoRes = response.body();

            LOG.info("request to SI -> " + reverseGeoReq.toString());
            LOG.info("response SI -> " + reverseGeoRes.toString());
        }catch (Exception e){

        }
        return reverseGeoRes;
    }

}
