package com.imaginamos.farmatodo.networking.util;

import com.google.appengine.api.modules.ModulesService;
import com.google.appengine.api.modules.ModulesServiceFactory;
import com.imaginamos.farmatodo.model.customer.AnswerGetUserOrigin;
import com.imaginamos.farmatodo.model.customer.CustomerOriginReq;
import com.imaginamos.farmatodo.model.customer.RequestGetUserOrigin;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.model.util.URLConnections;
import com.imaginamos.farmatodo.networking.models.SendSMSCloudFunctionVenReq;
import com.imaginamos.farmatodo.networking.models.addresses.SendSMSCloudFunctionReq;
import com.imaginamos.farmatodo.networking.services.CloudFunctionsService;
import com.imaginamos.farmatodo.networking.services.CloudFunctionsServiceSMSVen;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by JPuentes on 16/10/2018.
 */
public class Util {

    private static final Logger LOG = Logger.getLogger(Util.class.getName());

    /**
     * Construye un mensaje personalizado para el usuario segun su origen de registro.
     * Ofusca el correo electronico por seguridad.
     * */
    public static String buildCustomOriginMessage(final RequestGetUserOrigin request,final String origin){

        StringBuilder customMessage = new StringBuilder();
        String obfuscatedEmail = request.getEmail();

        try {
            obfuscatedEmail = Util.obfuscateEmail(request.getEmail(), Constants.MASK_6X);
        }catch (Exception e){
            LOG.warning(Constants.MESSAGE_ERROR_OBFUSCAR);
        }

        customMessage.append(Constants.MESSAGE_YOUR_REGISTER+obfuscatedEmail+Constants.MESSAGE_BY);

        switch (origin){
            case Constants.MANUAL:
                customMessage.append(Constants.MESSAGE_MANUAL);
                break;
            case Constants.FACEBOOK:
                customMessage.append(Constants.MESSAGE_FACEBOOK);
                break;
            case Constants.GMAIL:
                customMessage.append(Constants.MESSAGE_GMAIL);
                break;
        }
        return customMessage.toString();
    }


    /**
     * Construye un mensaje personalizado para el usuario segun su origen de registro.
     * Ofusca el correo electronico por seguridad.
     * @param request
     * @param response
     * */
    public static String buildCustomOriginMessageBack30(final CustomerOriginReq request, final AnswerGetUserOrigin response){
        final String origin = response.getOrigin();

        StringBuilder customMessage = new StringBuilder();
        String obfuscatedEmail = request.getEmail();

        try {
            obfuscatedEmail = Util.obfuscateEmail(request.getEmail(), Constants.MASK_6X);
        }catch (Exception e){
            LOG.warning(Constants.MESSAGE_ERROR_OBFUSCAR);
        }

        customMessage.append(Constants.MESSAGE_YOUR_REGISTER+obfuscatedEmail+Constants.MESSAGE_BY);

        switch (origin){
            case Constants.MANUAL:
                customMessage.append(Constants.MESSAGE_MANUAL);
                break;
            case Constants.FACEBOOK:
                customMessage.append(Constants.MESSAGE_FACEBOOK);
                break;
            case Constants.GMAIL:
                customMessage.append(Constants.MESSAGE_GMAIL);
                break;
        }
        return customMessage.toString();
    }

    /**
     * Ofuscar un correo electronico si tiene mas de tres caracteres.
     * @param email
     * @param caracter
     * */
    public static String obfuscateEmail(final String email, final String caracter) throws Exception{
        String obfuscatedEmail = email;

        if(email!=null){

            if(isValidEmailAddress(email)){
                String[] splitedEmail = email.split(Constants.ARROBA);
                String userEmail      = splitedEmail[0];
                String domain         = Constants.ARROBA+splitedEmail[1];
                int userEmailSize     = userEmail.length();

                if(userEmailSize>3){
                    String partOfEmailWithoutDomain = splitedEmail[0].substring(0,3);
                    obfuscatedEmail = partOfEmailWithoutDomain.concat(caracter)+domain;
                }

            }else{
                throw new Exception(Constants.INVALID_EMAIL);
            }
        }else{
            throw new Exception(Constants.REQUIRED_EMAIL);
        }
        return obfuscatedEmail;
    }

    /**
     * Validar si un correo electronico es valido o no.
     * @param email
     * */
    public static boolean isValidEmailAddress(final String email) {
        try {
            if (email == null)
                return false;

            if (email.isEmpty())
                return false;

            final String ePattern = Constants.mailRegexIsValid;
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
            java.util.regex.Matcher m = p.matcher(email);
            return m.matches();
        }catch(Exception e){
            LOG.warning("Error al validar email. Mensaje:"+e.getMessage());
            return false;
        }
    }

    /**
     * Retorna URL con respectivos parametros
     */
    public static String buildUrl(String baseUrl, Map<String,String> pathVariables, Map<String,String> requestParams){

        if((pathVariables==null || pathVariables.isEmpty()) && (requestParams==null || requestParams.isEmpty()) ) {
            return baseUrl;
        }

        for (Map.Entry<String, String> entry : pathVariables.entrySet()) {
            baseUrl = baseUrl.replace("{"+entry.getKey()+"}", entry.getValue());
        }

        if(requestParams!=null && !requestParams.isEmpty()) {
            int size = requestParams.size();
            int index = 0;
            baseUrl = baseUrl.concat("?");

            for (Map.Entry<String, String> entry : requestParams.entrySet()) {
                index++;
                baseUrl = baseUrl.concat(entry.getKey());
                baseUrl = baseUrl.concat("=");
                baseUrl = baseUrl.concat(entry.getValue());
                if(index<size) {
                    baseUrl = baseUrl.concat("&");
                }
            }
        }
        LOG.info(baseUrl);
        return baseUrl;
    }

    public static void sendAlertCreateOrder(String phone,String message) {
        ModulesService modulesApi = ModulesServiceFactory.getModulesService();
        String msg = modulesApi.getCurrentVersion() + " " + message;
        if (URLConnections.SEND_ERROR_BY_TEXT_SMS) {
            try {
                CloudFunctionsService.get().postSendSms(
                        new SendSMSCloudFunctionReq(
                                phone,
                                msg));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void sendAlertCreateOrderVen(String phone,String message) {
        ModulesService modulesApi = ModulesServiceFactory.getModulesService();
        String msg = modulesApi.getCurrentVersion() + " " + message;
        if (URLConnections.SEND_ERROR_BY_TEXT_SMS) {
            try {
                CloudFunctionsServiceSMSVen.get().postSendSms(
                        new SendSMSCloudFunctionVenReq(
                                phone,
                                msg));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
