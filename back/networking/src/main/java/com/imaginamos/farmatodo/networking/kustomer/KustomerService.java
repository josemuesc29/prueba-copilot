package com.imaginamos.farmatodo.networking.kustomer;


import com.fasterxml.jackson.core.JsonParseException;
import com.google.gson.Gson;
import com.imaginamos.farmatodo.model.customer.CustomerRequestKustomer;
import com.imaginamos.farmatodo.model.customer.CustomerResponseKustomer;
import com.imaginamos.farmatodo.model.environment.Enviroment;
import com.imaginamos.farmatodo.networking.kustomer.api.IKustomer;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Logger;

public class KustomerService {
    private static final Logger LOG = Logger.getLogger(KustomerService.class.getName());
    private final IKustomer iKustomer;

    public KustomerService() {
        this.iKustomer = new Retrofit.Builder()
                .baseUrl(Enviroment.URL_KUSTOMER)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(IKustomer.class);
    }

    public void sendCustomerPublisher(CustomerRequestKustomer customerRequestKustomer) {

        if (Objects.isNull(customerRequestKustomer)) {
            LOG.warning("CustomerRequestKustomer is null");
            return;
        }

        LOG.info("Sending customer to Kustomer publisher -> " + new Gson().toJson(customerRequestKustomer));

        try {
            Call<CustomerResponseKustomer> call = iKustomer.sendCustomerPublisher(customerRequestKustomer);
            Response<CustomerResponseKustomer> response = call.execute();
            LOG.info("Customer sent to Kustomer publisher -> " + new Gson().toJson(response.body()));
        } catch (IOException ex) {
            LOG.warning("Error sending customer to Kustomer publisher: " + ex.getMessage());
        }
    }
}
