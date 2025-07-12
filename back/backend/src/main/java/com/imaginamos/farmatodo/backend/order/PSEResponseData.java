package com.imaginamos.farmatodo.backend.order;

import com.imaginamos.farmatodo.model.order.PSEResponseCode;
import com.imaginamos.farmatodo.model.payment.PSEResponse;
import retrofit2.Response;

import java.util.List;

public class PSEResponseData {
    private Response<PSEResponseCode> pseResponses;

    public PSEResponseData(Response<PSEResponseCode> allPSE) {
        this.pseResponses = allPSE;
    }


    public Response<PSEResponseCode> getPseResponses() {
        return pseResponses;
    }

    public void setPseResponses(Response<PSEResponseCode> pseResponses) {
        this.pseResponses = pseResponses;
    }

    @Override
    public String toString() {
        return "PSEResponseData{" +
                "pseResponses=" + pseResponses +
                '}';
    }
}
