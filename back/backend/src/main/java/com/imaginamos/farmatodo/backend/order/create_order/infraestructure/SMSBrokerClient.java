package com.imaginamos.farmatodo.backend.order.create_order.infraestructure;

import com.imaginamos.farmatodo.networking.models.addresses.SendSMSCloudFunctionReq;
import com.imaginamos.farmatodo.networking.services.CloudFunctionsService;

import java.io.IOException;
import java.util.logging.Logger;

public class SMSBrokerClient {

    private static final Logger LOG = Logger.getLogger(SMSBrokerClient.class.getName());

    public static void sendSMS(final String phoneNumber, final String message) {
        try {
            CloudFunctionsService.get().postSendSms(new SendSMSCloudFunctionReq(phoneNumber, message));
        } catch (IOException e) {
            LOG.warning("No fue posible enviar el mensaje ["+message+"] al numero ["+phoneNumber+"]");
        }
    }
}
