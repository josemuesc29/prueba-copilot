package com.imaginamos.farmatodo.backend.copyrigth;

import com.imaginamos.farmatodo.model.util.DeliveryType;
import org.json.simple.JSONObject;

import java.util.logging.Logger;

public class Copyrights {
    private static final Logger LOG = Logger.getLogger(Copyrights.class.getName());


    @SuppressWarnings("ALL")
    public JSONObject createValidateCopyrightJson(long copyrightId, String description, DeliveryType deliveryType, Boolean isActive, Boolean isProvider) {
        //LOG.warning("method: createValidateCopyrightJson()");
        JSONObject copyrightJSON = new JSONObject();
        copyrightJSON.put("copyrightId", copyrightId);
        copyrightJSON.put("description", description);
        if (deliveryType != null) {
            copyrightJSON.put("deliveryType", deliveryType.getDeliveryType());
        } else {
            copyrightJSON.put("deliveryType", deliveryType.EXPRESS.toString());
        }
        copyrightJSON.put("isActive", isActive);
        copyrightJSON.put("isProvider", isProvider);
        return copyrightJSON;
    }
}
