package com.imaginamos.farmatodo.networking.services;

import com.imaginamos.farmatodo.networking.api.ApiCloudFunctions;
import com.imaginamos.farmatodo.networking.api.ApiCloudFunctionsVen;
import com.imaginamos.farmatodo.networking.base.ApiBuilder;
import com.imaginamos.farmatodo.networking.models.SendSMSCloudFunctionVenReq;
import com.imaginamos.farmatodo.networking.models.addresses.SendSMSCloudFunctionReq;
import com.imaginamos.farmatodo.networking.models.addresses.SendSmsCloudFunctionRes;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.logging.Logger;

public class CloudFunctionsServiceSMSVen {

    private static final Logger LOG = Logger.getLogger(ServinformacionService.class.getName());

    private static CloudFunctionsServiceSMSVen instance;

    private ApiCloudFunctionsVen apiCloudFunctions;

    private CloudFunctionsServiceSMSVen() {
        apiCloudFunctions = ApiBuilder.get().createCloudFunctionServiceVen(ApiCloudFunctionsVen.class);
    }

    public static CloudFunctionsServiceSMSVen get() {
        if (instance == null) instance = getSync();
        return instance;
    }

    private static synchronized CloudFunctionsServiceSMSVen getSync() {
        if (instance == null) instance = new CloudFunctionsServiceSMSVen();
        return instance;
    }

    public SendSmsCloudFunctionRes postSendSms(SendSMSCloudFunctionVenReq request) throws IOException {
        LOG.info("request SendSmsCloudFunctionRes toString" + request.toString());
        Call<SendSmsCloudFunctionRes> call = apiCloudFunctions.postSendSms(request);
        Response<SendSmsCloudFunctionRes> res = call.execute();
        LOG.info("res SendSmsCloudFunctionRes " + res.toString());
        return res.isSuccessful() ? res.body() : null;

    }
}
