package com.imaginamos.farmatodo.networking.api;

import com.imaginamos.farmatodo.networking.models.SendSMSCloudFunctionVenReq;
import com.imaginamos.farmatodo.networking.models.addresses.SendSMSCloudFunctionReq;
import com.imaginamos.farmatodo.networking.models.addresses.SendSmsCloudFunctionRes;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiCloudFunctionsVen {

    @POST("smsVen/send")
    Call<SendSmsCloudFunctionRes> postSendSms(@Body SendSMSCloudFunctionVenReq cloudFunctionReq);
}
