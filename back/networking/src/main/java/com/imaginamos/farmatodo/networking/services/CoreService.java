package com.imaginamos.farmatodo.networking.services;

import com.algolia.search.exceptions.AlgoliaException;
import com.google.api.server.spi.response.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.imaginamos.farmatodo.model.algolia.AlertConfigMessage;
import com.imaginamos.farmatodo.model.customer.*;
import com.imaginamos.farmatodo.model.order.*;
import com.imaginamos.farmatodo.model.payment.OrderChargeRes;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.model.util.HttpStatusCode;
import com.imaginamos.farmatodo.model.util.URLConnections;
import com.imaginamos.farmatodo.networking.algolia.APIAlgolia;
import com.imaginamos.farmatodo.networking.api.ApiCore;
import com.imaginamos.farmatodo.networking.base.ApiBuilder;
import com.imaginamos.farmatodo.networking.util.Util;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

public class CoreService {

    private static CoreService instance;

    private ApiCore coreApi;

    private static final Logger LOG = Logger.getLogger(CoreService.class.getName());

    private CoreService() {
        coreApi = ApiBuilder.get().createCoreSerive(ApiCore.class);
    }

    public static CoreService get() {
        if (instance == null) instance = getSync();
        return instance;
    }

    private static synchronized CoreService getSync() {
        if (instance == null) instance = new CoreService();
        return instance;
    }

    private void validateSocialNetwork(RequestGetUserOrigin requestGetUserOrigin){
        LOG.info("Method: validateSocialNetwork ->" + requestGetUserOrigin);
        String base = requestGetUserOrigin.getProvider();
        if(Objects.nonNull(base) && !base.isEmpty()){
            if(requestGetUserOrigin.getProvider().toUpperCase().contains("GOOGLE")){
                requestGetUserOrigin.setProvider("GOOGLE");
            }else if(requestGetUserOrigin.getProvider().toUpperCase().contains("FACEBOOK")){
                requestGetUserOrigin.setProvider("FACEBOOK");
            }
            requestGetUserOrigin.setUid(getSocialNetworkId(base));
        }
        LOG.info("Method: validateSocialNetwork Provider->" + requestGetUserOrigin.getProvider());
        LOG.info("Method: validateSocialNetwork UID->" + requestGetUserOrigin.getUid());
    }

    private String getSocialNetworkId(final String socialNetwork){
        LOG.info("Method: getSocialNetworkId ->" + socialNetwork);
        String[] split = socialNetwork.split("/");
        return  (Objects.nonNull(split) && split.length >= 3) ? split[3] : "";
    }

    private boolean validateResponse(int responseCode, AnswerGetUserOrigin response, Response<AnswerGetUserOrigin> res){
        if (responseCode == 200) {
            response.setOrigin(res.body().getOrigin());
            response.setStatusCode(res.code());
            response.setStatus("Ok");
            return true;
        }
        return false;
    }

    private AnswerGetUserOrigin returnErrorResponse(int responseCode, AnswerGetUserOrigin response, Response<AnswerGetUserOrigin> res){
        if(responseCode == 405){
                response.setStatusCode(404);
                response.setStatus("No Content");
                response.setOrigin(null);
        }else {
                response.setStatusCode(responseCode);
                response.setStatus(res.message());
                response.setOrigin(null);
        }
        response.setMessage(res.message());
        return response;
    }

    private AnswerGetUserOrigin returnErrorResponse(int responseCode, AnswerGetUserOrigin response, String message){
        response.setStatusCode(responseCode);
        response.setStatus(message);
        response.setOrigin(null);
        return response;
    }

    private String getUserUid(String userUid) throws IOException {
        LOG.info("Method: getUserId Request to firebase.");
        String urlStringFirebase = URLConnections.URL_GET_USER_FIREBASE + "?uid="+userUid;
        HttpURLConnection httpURLConnectionFirebase = URLConnections.getConnection(URLConnections.GET, urlStringFirebase);
        LOG.info("Response. "+httpURLConnectionFirebase);
        String wrFirebase = httpURLConnectionFirebase.getResponseCode()+" - " +httpURLConnectionFirebase.getResponseMessage();
        LOG.info("Message:  "+ wrFirebase);
        return wrFirebase;
    }

    public static void generateResponse(Response response) throws NotFoundException, IOException, UnauthorizedException, ConflictException, BadRequestException, InternalServerErrorException, ServiceUnavailableException {

        int responseCode = response.code();
        if (responseCode == HttpStatusCode.NO_CONTENT.getCode()){
            if (response.errorBody() != null) {
                throw new NotFoundException(Constants.DEFAULT_MESSAGE + response.errorBody().string());
            }
        }else if (responseCode == HttpStatusCode.UNAUTHORIZED.getCode()){
            if (response.errorBody() != null) {
                throw new UnauthorizedException(Constants.DEFAULT_MESSAGE + response.errorBody().string());
            }
        }else if (responseCode == HttpStatusCode.ACCEPTED.getCode()){
            if (response.errorBody() != null) {
                throw new NotFoundException(Constants.DEFAULT_MESSAGE + response.errorBody().string());
            }
        }else if (responseCode == HttpStatusCode.SERVICE_UNAVAILABLE.getCode()){
            if (response.errorBody() != null) {
                throw new ServiceUnavailableException(Constants.DEFAULT_MESSAGE + response.errorBody().string());
            }
        }
        else if (responseCode == HttpStatusCode.INTERNAL_SERVER_ERROR.getCode()){
            if (response.errorBody() != null) {
                throw new InternalServerErrorException(Constants.DEFAULT_MESSAGE + response.errorBody().string());
            }
        }else if (responseCode == HttpStatusCode.BAD_REQUEST.getCode()) {
            if (response.errorBody() != null) {
                throw new BadRequestException(Constants.DEFAULT_MESSAGE + response.errorBody().string());
            }
        }else {
            if (response.errorBody() != null) {
                throw new ConflictException(Constants.DEFAULT_MESSAGE + response.errorBody().string());
            }
        }
    }

   public CoreEventResponse pingRMS() throws IOException {

        Call<CoreEventResponse> call = coreApi.pingRMS(URLConnections.URL_RMS_PING);
        Response<CoreEventResponse> response = call.execute();
        if (response.isSuccessful())
            return response.body();
        else {
            CoreEventResponse error = new CoreEventResponse();
            error.setCode(CoreEventResponseCode.APPLICATION_ERROR);
            error.setMessage(response.message());
            return error;
        }
    }

   public CoreEventResponse createFulfilOrdColDesc(FulfilOrdColDescDomain fulfilOrdColDescDomain) throws IOException {
        Call<CoreEventResponse> call = coreApi.createFulfilOrdColDesc(URLConnections.URL_RMS_CREATE_FULFIL_ORD_COL_DESC, fulfilOrdColDescDomain);
        Response<CoreEventResponse> response = call.execute();
        if (response.isSuccessful())
            return response.body();
        else {
            CoreEventResponse error = new CoreEventResponse();
            error.setCode(CoreEventResponseCode.APPLICATION_ERROR);
            error.setMessage(response.message());
            return error;
        }
    }

   public CoreEventResponse pingSIM() throws IOException {

        Call<CoreEventResponse> call = coreApi.pingSIM(URLConnections.URL_SIM_PING);
        Response<CoreEventResponse> response = call.execute();
        if (response.isSuccessful())
            return response.body();
        else {
            CoreEventResponse error = new CoreEventResponse();
            error.setCode(CoreEventResponseCode.APPLICATION_ERROR);
            error.setMessage(response.message());
            return error;
        }
    }

    public CoreEventResponse createFulfillmentOrderDetail(FulfilOrdColDescDomain fulfilOrdColDescDomain) throws IOException {
        Call<CoreEventResponse> call = coreApi.createFulfillmentOrderDetail(URLConnections.URL_SIM_CREATE_FULFILLMENT_ORDER_DETAIL, fulfilOrdColDescDomain);
        Response<CoreEventResponse> response = call.execute();
        if (response.isSuccessful())
            return response.body();
        else {
            CoreEventResponse error = new CoreEventResponse();
            error.setCode(CoreEventResponseCode.APPLICATION_ERROR);
            error.setMessage(response.message());
            return error;
        }
    }

}
