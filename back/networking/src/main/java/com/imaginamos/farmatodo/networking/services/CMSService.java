package com.imaginamos.farmatodo.networking.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.imaginamos.farmatodo.model.home.BannersDTFRes;
import com.imaginamos.farmatodo.model.util.URLConnections;
import com.imaginamos.farmatodo.networking.api.ApiCMS;
import com.imaginamos.farmatodo.networking.base.ApiBuilder;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.logging.Logger;

public class CMSService {
    private static final Logger LOG = Logger.getLogger(ServinformacionService.class.getName());

    private static CMSService instance;

    private ApiCMS api;

    public CMSService() {
        api = ApiBuilder.get().createCMSService(ApiCMS.class);
    }

    public static CMSService get() {
        if (instance == null) instance = new CMSService();
        return instance;
    }

    public BannersDTFRes getBannersCMS(String email, String type, Integer category, String city, boolean isMobile ) throws IOException {
        //LOG.info("method getBannersCMS");

        String apiUrlCMS = URLConnections.URL_BANNERS_CMS;
        int isMobileService = 0;

        if (isMobile){
//            apiUrlCMS = "https://prod-mobile-dot-cms-dot-stunning-base-164402.uc.r.appspot.com/backend/flexible/v2/cms/getBannersHome";
            isMobileService = 1;
        }


        if (email == null || email.isEmpty() || email.equals("cliente.anonimo@farmatodo.com")){
            email = "undefined";
        }

        if (type == null || type.isEmpty()) {
            type = "MAIN_BANNER";
        }

        if (category == null){
            category = 0;
        }

        if (city == null || city.isEmpty()){
            city = "BOG";
        }

        Call<BannersDTFRes> call = api.getBannersCMS(apiUrlCMS, email, type, category , city, isMobileService);
        LOG.info(" getBannersCMS req URL -> " + call.request().url().toString());
        Response<BannersDTFRes> response = call.execute();
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
//        LOG.info("Response cms ->" + gson.toJson(response.body()));
        return  response.isSuccessful() ? response.body() : null;

    }



}
