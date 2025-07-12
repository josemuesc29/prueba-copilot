package com.imaginamos.farmatodo.networking.services;

import com.imaginamos.farmatodo.model.algolia.autocomplete.AutocompleteByCityConfig;
import com.imaginamos.farmatodo.model.algolia.autocomplete.City;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.networking.algolia.APIAlgolia;
import com.imaginamos.farmatodo.networking.api.ApiGeoGoogle;
import com.imaginamos.farmatodo.networking.base.ApiBuilder;
import com.imaginamos.farmatodo.networking.models.addresses.*;
import retrofit2.Call;
import retrofit2.Response;

import java.util.List;
import java.util.logging.Logger;

public class GeoGoogleService {

    private static final Logger LOG = Logger.getLogger(GeoGoogleService.class.getName());

    private static GeoGoogleService instance;

    private ApiGeoGoogle apiGeoGoogle;

    private GeoGoogleService() {
        apiGeoGoogle = ApiBuilder.get().createGeoGoogleService(ApiGeoGoogle.class);
    }

    public static GeoGoogleService get() {
        if (instance == null) instance = getSync();
        return instance;
    }

    private static synchronized GeoGoogleService getSync() {
        if (instance == null) instance = new GeoGoogleService();
        return instance;
    }

    public GAutocompleteRes getAutocomplete(AddressPredictionReq siAddressReq, String typeAlgolia) {
        LOG.info("request getAutocomplete() -> "+siAddressReq.toString());
//        final String text = siAddressReq.getAddress() + "+in+" + siAddressReq.getCity() + ",+"+siAddressReq.getCountry();
        final String text = siAddressReq.getAddress()+" "+siAddressReq.getCity()+" "+siAddressReq.getCity();
        GAutocompleteRes gAutocompleteRes = new GAutocompleteRes();

        final AutocompleteByCityConfig autocompleteByCityConfig = APIAlgolia.getAutocompleteConfig();
        Call<GAutocompleteRes> call;
        final City cityForSearch = autoCompleteWithRestriction(siAddressReq.getCity(), autocompleteByCityConfig);

        /** ¿Autocomplete with restrictions or not? */
        if(autocompleteByCityConfig != null && autocompleteByCityConfig.getUseFastAutocomplete()) {
            call = apiGeoGoogle.getAutocomplete(text, Constants.GEO_APIKEY, typeAlgolia, "country:"+siAddressReq.getCountry());
            //LOG.info("URL Google -> "+call.request().url().toString());
        } else {
            if (cityForSearch != null) {
                call = apiGeoGoogle.getAutocomplete(text, Constants.GEO_APIKEY, typeAlgolia,
                        cityForSearch.getLocation(), cityForSearch.getRadiusInMeters(),
                        cityForSearch.isUseStrictBounds() ? "strictbounds" : "", "country:"+siAddressReq.getCountry());
                //LOG.info("URL Google -> "+call.request().url().toString());
            } else {
                call = apiGeoGoogle.getAutocomplete(text, Constants.GEO_APIKEY, typeAlgolia, "country:"+siAddressReq.getCountry());
                //LOG.info("URL Google -> "+call.request().url().toString());
            }
        }

        try {
            Response<GAutocompleteRes> response = call.execute();

            /** If not got results then, query again by establishments */
            if (response != null && response.body() != null && response.body().getPredictions().size() == 0) {
                call = apiGeoGoogle.getAutocomplete(text, Constants.GEO_APIKEY, "establishment",
                        cityForSearch.getLocation(), cityForSearch.getRadiusInMeters(),
                        cityForSearch.isUseStrictBounds() ? "strictbounds" : "", "country:"+siAddressReq.getCountry());
                response = call.execute();
                //LOG.info("URL Google -> "+call.request().url().toString());
            }

            gAutocompleteRes = response.body();
        } catch (Exception e) {
            LOG.warning("Warning buscando con autocomplete. mensaje: "+ (e != null ? e.getMessage() : "error sin mensaje"));
        }

        return gAutocompleteRes;
    }

    private City autoCompleteWithRestriction(final String cityId, final AutocompleteByCityConfig autocompleteByCityConfig){
        try{
            if (autocompleteByCityConfig != null) {
                final City cityIsIn = cityIsInList(autocompleteByCityConfig, cityId);
                return cityIsIn != null ? cityIsIn : null;
            } else {
                return null;
            }
        }catch (Exception e) {
            LOG.warning("Error en autoCompleteWithRestriction(.) Mensaje: " + (e != null ? e.getMessage() : "error sin mensaje"));
            return null;
        }
    }

    private City cityIsInList(final AutocompleteByCityConfig config, final String cityId){
        try {
            List<City> cities = config.getCities();
            if (cities != null && cities.size() > 0 && cityId != null && !cityId.isEmpty()) {
                for (City city : cities) {
                    if (city.getId().equals(cityId) && city.getActive()) {
                        return city;
                    }
                }
                return null;
            } else {
                return null;
            }
        } catch (Exception e) {
            LOG.warning("Error en cityIsInList(..) Mensaje: "+ ((e != null) ? e.getMessage() : "sin mensaje"));
            return null;
        }
    }
    public GPlaceIdRes getPlaceById(String placeId) {

        GPlaceIdRes gPlaceIdRes = new GPlaceIdRes();
        Call<GPlaceIdRes> call = apiGeoGoogle.getPlaceById(placeId, Constants.GEO_APIKEY_ANDROID);

        try {
            //LOG.info("Call Google -> " + call.request().toString());
            Response<GPlaceIdRes> response = call.execute();

            if (response.isSuccessful()){
                LOG.info("Google PlaceId Success -->" + response.body());
                gPlaceIdRes = response.body();

            }else {
                LOG.warning("Error call placeId Google" + response.raw().toString());
            }

        }catch (Exception e){
            LOG.warning(e.getMessage());
        }
        return gPlaceIdRes;
    }

    public GGeoReverseRes getGeoReverse(String latlng) {

        GGeoReverseRes gGeoReverseRes = new GGeoReverseRes();
        Call<GGeoReverseRes> call = apiGeoGoogle.getGeoReverse(latlng,Constants.GEO_APIKEY_ANDROID);

        try {
            Response<GGeoReverseRes> response = call.execute();
            gGeoReverseRes = response.body();
        }catch (Exception e){

        }
        return gGeoReverseRes;
    }

    public GGeoCodeRes getGeoCode(String address) {

        GGeoCodeRes gGeoCodeRes = new GGeoCodeRes();
        try {
        Call<GGeoCodeRes> call = apiGeoGoogle.getGeoCode(address,Constants.GEO_APIKEY_ANDROID);
            Response<GGeoCodeRes> response = call.execute();
            gGeoCodeRes = response.body();
        }catch (Exception e){
            LOG.warning(e.getMessage());
        }
        return gGeoCodeRes;
    }

}
