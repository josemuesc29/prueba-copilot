package com.imaginamos.farmatodo.backend.util;

import com.google.api.server.spi.response.BadRequestException;
import com.imaginamos.farmatodo.model.algolia.messageconfig.CampaignMessages;
import com.imaginamos.farmatodo.model.algolia.messageconfig.Message;
import com.imaginamos.farmatodo.model.algolia.messageconfig.MessageSmsConfig;
import com.imaginamos.farmatodo.model.algolia.messageconfig.MsgSmsEnum;
import com.imaginamos.farmatodo.networking.algolia.APIAlgolia;

import java.util.Optional;
import java.util.logging.Logger;

public class MsgUtilAlgolia {
    private static final Logger LOG = Logger.getLogger(MsgUtilAlgolia.class.getName());

    public static String obtainMsgAlgolia(MsgSmsEnum msgEnum) throws BadRequestException {
        Optional<MessageSmsConfig> messageSmsConfig = APIAlgolia.getMessageSms();
        if (!messageSmsConfig.isPresent()) {
            LOG.warning("no esta presente el mensaje");
            throw new BadRequestException("No se pudo obtener el mensaje de configuración");
        }
        if (messageSmsConfig.get().getCampaignMessages() == null) {
            LOG.warning("no esta presente la campaña");
            throw new BadRequestException("No se pudo obtener el mensaje de configuración");
        }
        for (CampaignMessages campaignMessages : messageSmsConfig.get().getCampaignMessages()) {
            if(validateMsg(campaignMessages)) {
                return validateMsgForENUM(campaignMessages, msgEnum) == null ? "" : validateMsgForENUM(campaignMessages, msgEnum);

            }
        }
        return "";

    }
    private static Boolean  validateMsg(CampaignMessages campaignMessages) {
        if(campaignMessages.getActive() == null){
            return false;
        }
        if(!campaignMessages.getActive()){
            return false;
        }
        if(campaignMessages.getName() == null){
            return false;
        }
        if(campaignMessages.getType() == null){
            return false;
        }

        if(!campaignMessages.getType().equalsIgnoreCase("BACKEND2")){
            return false;
        }
        return true;
    }
    private static String validateMsgForENUM(CampaignMessages campaignMessages, MsgSmsEnum msg) {
        if (campaignMessages.getMessages() == null) {
            return null;
        }
        for (Message message : campaignMessages.getMessages()) {
            if (message.getKey() == null) {
                return null;
            }
            if (message.getKey().trim().equalsIgnoreCase(msg.name())) {
                return message.getValues();
            }
        }
        return null;
    }
}







