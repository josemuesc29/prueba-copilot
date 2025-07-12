package com.imaginamos.farmatodo.backend.firebase.api;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.imaginamos.farmatodo.backend.firebase.constans.FirebaseLocationEnum;
import com.imaginamos.farmatodo.backend.firebase.models.AuthenticationRequest;
import com.imaginamos.farmatodo.backend.firebase.models.AuthenticationResponse;
import com.imaginamos.farmatodo.backend.firebase.models.CodeLoginFactory;
import com.imaginamos.farmatodo.backend.firebase.models.NewOrderPrimePSEMixed;
import com.imaginamos.farmatodo.backend.firebase.models.NotifyCodeLogin;
import com.imaginamos.farmatodo.model.util.URLConnections;
import com.imaginamos.farmatodo.networking.algolia.APIAlgolia;
import com.imaginamos.farmatodo.networking.base.ApiBuilder;
import com.imaginamos.farmatodo.networking.services.ApiGatewayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.Objects;

@Component
public class FirebaseService {

    private static FirebaseService instance;

    private static final Logger LOG = LoggerFactory.getLogger(FirebaseService.class.getName());

    private final IFirebase iFirebase;

//    private final IFirebase iFirebaseAuth;

    @Autowired
    public FirebaseService()  {
        iFirebase = ApiBuilder.get().createService(IFirebase.class, FirebaseLocationEnum.LOGIN_CODE_LOCATION.baseUrl());
//        iFirebaseAuth = ApiBuilder.get().createService(IFirebase.class, "");
    }

    private static synchronized FirebaseService getSync() {
        if (instance == null) instance = new FirebaseService();
        return instance;
    }

    public static FirebaseService get() {
        if (instance == null) instance = getSync();
        return instance;
    }

    public void notifyNewCodeLogin(final NotifyCodeLogin request){
        try {
//            LOG.info("method: notifyNewCodeLogin({})", request);

            if (CodeLoginFactory.isValid(request)){
                final String url = FirebaseLocationEnum.LOGIN_CODE_LOCATION.location();
                final String ulrFinal = generateUrlWithTokenQuery(url);
                final JsonObject body = CodeLoginFactory.buildBodyForNewCodeLogin(request);

                Call<Void> call = iFirebase.notifyLoginNewCode(ulrFinal, body);
                Response<Void> response = call.execute();

//                LOG.info("method: notifyNewCodeLogin({}) Response: {}", request, response);
            }

        }catch (IOException e){
            LOG.warn("No fue posible notificar a Firebase el nuevo codigo. Mensaje: {}", e.getMessage());
        }
    }

    public String getLoginCode(final String code, final String idCustomer){
        try {
            if (Objects.nonNull(code)){
                FirebaseLocationEnum firebaseLocationEnum = FirebaseLocationEnum.LOGIN_GET_LOCATION;
                final String url = firebaseLocationEnum.location().replace(firebaseLocationEnum.keyToReplace(), idCustomer);
                final String ulrFinal = generateUrlWithTokenQuery(url);
                Call<String> call = iFirebase.loginCode(ulrFinal);
                Response<String> response = call.execute();

                return response.body();
            }
        }catch (IOException e){
            LOG.warn("No fue posible traer el codigo de Firebase. Mensaje: {}", e.getMessage());
        }
        return null;
    }

    public void deleteLoginCode(final String idCustomer){
        try {
            FirebaseLocationEnum firebaseLocationEnum = FirebaseLocationEnum.LOGIN_GET_LOCATION;
            final String url = firebaseLocationEnum.location().replace(firebaseLocationEnum.keyToReplace(), idCustomer);
            final String ulrFinal = generateUrlWithTokenQuery(url);
            Call<Void> call = iFirebase.deleteLoginCode(ulrFinal);
            Response<Void> response = call.execute();
            return;
        }catch (IOException e){
            LOG.warn("No fue posible traer el codigo de Firebase. Mensaje: {}", e.getMessage());
        }
        return;
    }

    public void deleteTrackingOrder(final String idOrder) {
//        FirebaseLocationEnum firebaseLocationEnum = FirebaseLocationEnum.GET_TRACKING_ORDER;
//        LOG.info("Se elimina el tracking" + firebaseLocationEnum);
//        final String url = firebaseLocationEnum.location().replace(firebaseLocationEnum.keyToReplace(), idOrder);
//        LOG.info("URL final " + url);
//        Call<Void> call = iFirebase.deleteLoginCode(url);
//        LOG.info("Se elimina el tracking" + call.toString());
        FirebaseLocationEnum firebaseLocationEnum = FirebaseLocationEnum.GET_TRACKING_ORDER;
        //LOG.warn("Se elimina el tracking" + firebaseLocationEnum);
        final String url = firebaseLocationEnum.location().replace(firebaseLocationEnum.keyToReplace(), idOrder);
        final String ulrFinal = generateUrlWithTokenQuery(url);
        //LOG.warn("URL final " + ulrFinal);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.delete(ulrFinal);
        //LOG.warn("Se elimina el tracking");
    }

    private String generateUrlWithTokenQuery(String url) {
        String token = ApiGatewayService.get().getTokenFirebase();
        if (!Objects.nonNull(token)) {
            token = getTokenAuthFirebase();
        }
        return url + "?access_token=" + token;
    }

    public void notifyNewCodeLoginV2(final NotifyCodeLogin request){
        try {
            ApiGatewayService.get().addCodeLogin(request.getKey(), request.getCode());
        }catch (IOException e){
            LOG.warn("No fue posible notificar a Firebase el nuevo codigo. Mensaje: {}", e.getMessage());
        }
    }

    public String getTokenAuthFirebase() {
        AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        authenticationRequest.setEmail(URLConnections.FIREBASE_AUTHENTICATION_EMAIL);
        authenticationRequest.setPassword(URLConnections.FIREBASE_AUTHENTICATION_PASSWORD);
        authenticationRequest.setReturnSecureToken(true);
        String url = FirebaseLocationEnum.AUTH_FIREBASE.baseUrl().concat(URLConnections.FIREBASE_API_KEY);
//        log.info(url);
//        log.info(new Gson().toJson(authenticationRequest));
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<AuthenticationResponse> responseH = restTemplate.postForEntity(url, authenticationRequest, AuthenticationResponse.class);


//        log.info(new Gson().toJson(responseH));
        if(responseH.getStatusCode().is2xxSuccessful() && Objects.nonNull(responseH.getBody()) && Objects.nonNull(responseH.getBody().getIdToken())) {
            return responseH.getBody().getIdToken();
        }
        return "";
    }

    public String getLoginCodeV2(final String code, final String idCustomer) {
        try {
           return ApiGatewayService.get().getCodeLogin(idCustomer);
        }catch (IOException e){
            LOG.warn("No fue posible traer el codigo de Firebase. Mensaje: {}", e.getMessage());
        }
        return null;
    }

    public void deleteLoginCodeV2(final String idCustomer){
        try {
//            ApiGatewayService.get().deleteCodeLogin(idCustomer);
//            LOG.info("Borrando codigo");
        }catch (Exception e){
            LOG.warn("No fue posible traer el codigo de Firebase. Mensaje: {}", e.getMessage());
        }
    }


    public void setOrderPrimeMix(final String idOrderPrime, final String idOrder){
        try {
            LOG.info("Se envia orden de prime" + idOrderPrime + "a la orden principal " + idOrder);

            NewOrderPrimePSEMixed request = new NewOrderPrimePSEMixed(idOrderPrime, true);
            if (Objects.nonNull(request)) {
                FirebaseLocationEnum firebaseLocationEnum = FirebaseLocationEnum.SET_ORDER_PRIME_MIXED;
                final String url = firebaseLocationEnum.location().replace(firebaseLocationEnum.keyToReplace(), idOrder);
                final String ulrFinal = generateUrlWithTokenQuery(url);
                //LOG.warn("URL final " + url + " request -> " + request.toString());
                Call<Void> call = iFirebase.setOrderPrimeMixed(ulrFinal, request);
                Response<Void> response = call.execute();
                LOG.info("Se inserta orden en firebase.");
            }

        } catch (IOException e){
            LOG.warn("No fue posible traer el codigo de Firebase. Mensaje: {}", e.getMessage());
        }
    }

}
