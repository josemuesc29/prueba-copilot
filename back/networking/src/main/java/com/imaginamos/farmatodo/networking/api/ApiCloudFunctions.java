package com.imaginamos.farmatodo.networking.api;

import com.imaginamos.farmatodo.networking.models.addresses.*;
import com.imaginamos.farmatodo.networking.models.shorturl.ShortUrlReq;
import com.imaginamos.farmatodo.networking.models.shorturl.ShortUrlRes;
import okhttp3.ResponseBody;
import com.imaginamos.farmatodo.networking.models.mail.SendMailReq;
import com.imaginamos.farmatodo.networking.models.authentication.LoginFirebaseReq;
import com.imaginamos.farmatodo.networking.models.authentication.LoginFirebaseRes;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiCloudFunctions {

    @POST("sendSMS")
    Call<SendSmsCloudFunctionRes>  postSendSms(@Body SendSMSCloudFunctionReq cloudFunctionReq);

    @POST("whatsApp/send")
    Call<SendWhatsappCloudFunctionRes>  postSendWhatsapp(@Body SendWhatsappCloudFunctionReq cloudFunctionReq);

    @POST("whatssap")
    Call<SendWhatsappCloudFunctionCodeRes>  sendWhatsappCode(@Body SendWhatsappCloudFunctionCodeReq cloudFunctionReq);

    @POST
    Call<SendWhatsappCloudFunctionRes>  postSendWhatsappV2(@Url String url, @Body SendWhatsappCloudFunctionReq cloudFunctionReq);

//    DEPECRATED - USE API-GATEWAY SERVICE
    @Deprecated
    @POST("sendEmail")
    Call<Void> sendEmail(@Body() SendMailReq sendMailReq);

    @POST("shortUrl")
    Call<ShortUrlRes> shortUrl(@Body() ShortUrlReq shortUrlReq);

    @POST("sendCodeByCall")
    Call<SendSmsCloudFunctionRes>  postSendCodeByCall(@Body SendSMSCloudFunctionReq cloudFunctionReq);


}
