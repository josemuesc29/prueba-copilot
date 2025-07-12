package com.imaginamos.farmatodo.networking.services;

import com.imaginamos.farmatodo.model.util.URLConnections;
import com.imaginamos.farmatodo.networking.api.ApiCloudFunctions;
import com.imaginamos.farmatodo.networking.api.ApiServinformacion;
import com.imaginamos.farmatodo.networking.base.ApiBuilder;
import com.imaginamos.farmatodo.networking.models.addresses.*;
import com.imaginamos.farmatodo.networking.models.authentication.LoginFirebaseReq;
import com.imaginamos.farmatodo.networking.models.authentication.LoginFirebaseRes;
import com.imaginamos.farmatodo.networking.models.mail.SendMailReq;
import com.imaginamos.farmatodo.networking.models.shorturl.ShortUrlReq;
import com.imaginamos.farmatodo.networking.models.shorturl.ShortUrlRes;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

public class CloudFunctionsService {
    private static final Logger LOG = Logger.getLogger(ServinformacionService.class.getName());

    private static CloudFunctionsService instance;

    private ApiCloudFunctions apiCloudFunctions;

    private CloudFunctionsService() {
        apiCloudFunctions = ApiBuilder.get().createCloudFunctionService(ApiCloudFunctions.class);
    }

    public static CloudFunctionsService get() {
        if (instance == null) instance = getSync();
        return instance;
    }

    private static synchronized CloudFunctionsService getSync() {
        if (instance == null) instance = new CloudFunctionsService();
        return instance;
    }

    public SendSmsCloudFunctionRes postSendSms(final SendSMSCloudFunctionReq request) throws IOException {

        Call<SendSmsCloudFunctionRes> call = apiCloudFunctions.postSendSms(request);
        Response<SendSmsCloudFunctionRes> res = call.execute();
        return res.isSuccessful() ? res.body() : null;

    }

    public SendWhatsappCloudFunctionRes postSendWhatsapp(final SendWhatsappCloudFunctionReq request) throws IOException {

        Call<SendWhatsappCloudFunctionRes> call = apiCloudFunctions.postSendWhatsapp(request);
        Response<SendWhatsappCloudFunctionRes> res = call.execute();
        return res.isSuccessful() ? res.body() : null;

    }

    public SendWhatsappCloudFunctionCodeRes sendWhatsappCode(final SendWhatsappCloudFunctionCodeReq request) throws IOException {

        Call<SendWhatsappCloudFunctionCodeRes> call = apiCloudFunctions.sendWhatsappCode(request);
        Response<SendWhatsappCloudFunctionCodeRes> res = call.execute();
        return res.isSuccessful() ? res.body() : null;

    }

    public SendWhatsappCloudFunctionRes postSendWhatsappV2(final SendWhatsappCloudFunctionReq request) throws IOException {
        final String url = URLConnections.URL_BASE_CLOUD_FUNCTIONSV2.concat("whatsApp2/send");
        //LOG.info("Url send Whatsapp message -> " + url);
        Call<SendWhatsappCloudFunctionRes> call = apiCloudFunctions.postSendWhatsappV2(url, request);
        Response<SendWhatsappCloudFunctionRes> res = call.execute();
        return res.isSuccessful() ? res.body() : null;
    }


    public Optional<ShortUrlRes> shortUrl(final ShortUrlReq shortUrlReq) throws IOException {
        //LOG.info("call cloud function service shortUrl");

        if (shortUrlReq != null && shortUrlReq.isValid()){
            final Call<ShortUrlRes> call = apiCloudFunctions.shortUrl(shortUrlReq);
            final Response<ShortUrlRes> response = call.execute();
            if (response.isSuccessful() && response.body() != null ){
                return Optional.of(response.body());
            }
        }
        return Optional.empty();
    }

    public SendSmsCloudFunctionRes postSendCodeByCall(final SendSMSCloudFunctionReq request) throws IOException {

        Call<SendSmsCloudFunctionRes> call = apiCloudFunctions.postSendCodeByCall(request);
        Response<SendSmsCloudFunctionRes> res = call.execute();
        return res.isSuccessful() ? res.body() : null;

    }


}
