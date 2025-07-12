package com.imaginamos.farmatodo.networking.kustomer.api;

import com.imaginamos.farmatodo.model.customer.CustomerRequestKustomer;
import com.imaginamos.farmatodo.model.customer.CustomerResponseKustomer;
import com.imaginamos.farmatodo.networking.talonone.model.*;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.Map;

public interface IKustomer {

    @PATCH("kustomer/customer")
    Call<CustomerResponseKustomer> sendCustomerPublisher(@Body CustomerRequestKustomer customerRequestKustomer);

}
