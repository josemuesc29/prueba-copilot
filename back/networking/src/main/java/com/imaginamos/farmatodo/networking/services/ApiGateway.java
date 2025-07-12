package com.imaginamos.farmatodo.networking.services;

import com.google.api.server.spi.response.BadRequestException;
import com.google.gson.Gson;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.model.util.ErrorResponse;
import com.imaginamos.farmatodo.networking.base.ApiBuilder;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.logging.Logger;

public class ApiGateway<T> {

    protected T apiGateway;

    private static final Logger log = Logger.getLogger(ApiGateway.class.getName());

    protected ApiGateway(Class<T> apiInterface) {
        apiGateway = ApiBuilder.get().createBackend30Service(apiInterface);
    }

    public <R> R executeApiCall(Call<R> call) throws IOException, BadRequestException {
        Response<R> response = call.execute();
        if (response.isSuccessful()) {
            return response.body();
        } else {
            return buildErrorBody(response);
        }
    }

    private <R> R buildErrorBody(Response<R> response) throws BadRequestException {
        String errorMessage = Constants.DEFAULT_MESSAGE;
        try {
            if (response != null && response.errorBody() != null) {
                ErrorResponse errorResponse = new Gson().fromJson(response.errorBody().charStream(), ErrorResponse.class);
                errorMessage = errorResponse.getMessage();
            }
            throw new BadRequestException(errorMessage);
        } catch (Exception e) {
            throw new BadRequestException(errorMessage);
        }
    }

}