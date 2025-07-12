package com.imaginamos.farmatodo.networking.api.customerAddress;

import com.imaginamos.farmatodo.model.customer.AddressResponse;
import retrofit2.Call;
import retrofit2.http.PUT;
import retrofit2.http.Url;

public interface CustomerAddressApiGateway {

    @PUT
    Call<AddressResponse> updateCustomerAddressDefault(@Url String url);
}
