package com.imaginamos.farmatodo.networking.services.customerAddress;

import com.google.api.server.spi.response.BadRequestException;
import com.imaginamos.farmatodo.model.customer.Address;
import com.imaginamos.farmatodo.model.customer.AddressResponse;
import com.imaginamos.farmatodo.model.util.URLConnections;
import com.imaginamos.farmatodo.networking.api.customerAddress.CustomerAddressApiGateway;
import com.imaginamos.farmatodo.networking.services.ApiGateway;
import retrofit2.Call;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;


public class CustomerAddressApiService extends ApiGateway<CustomerAddressApiGateway> {

    private static final Logger log = Logger.getLogger(CustomerAddressApiService.class.getName());

    private static CustomerAddressApiService instance;

    protected CustomerAddressApiService() {
        super(CustomerAddressApiGateway.class);
    }

    public static CustomerAddressApiService get() {
        if (instance == null) {
            synchronized (CustomerAddressApiService.class) {
                if (instance == null) {
                    instance = new CustomerAddressApiService();
                }
            }
        }
        return instance;
    }

    public Optional<Address> updateCustomerAddressDefault(final Long customerId, final Long addressId) throws IOException, BadRequestException {
        validateData(customerId, addressId);
        String url = getUrlUpdateCustomerAddressDefault(customerId, addressId);
        Call<AddressResponse> call = apiGateway.updateCustomerAddressDefault(url);
        AddressResponse addressResponse = executeApiCall(call);
        if (addressResponse != null && addressResponse.getData() != null) {
            return Optional.of(new Address(addressResponse.getData()));
        }
        return Optional.empty();
    }

    private static String getUrlUpdateCustomerAddressDefault(Long customerId, Long addressId) {
        return URLConnections.URL_CRM_UPDATE_CUSTOMER_ADDRESS_DEFAULT
                .replace("{customerId}", String.valueOf(customerId))
                .replace("{addressId}", String.valueOf(addressId));
    }

    private void validateData(Long customerId, Long addressId) {
        if (customerId == null || addressId == null) {
            throw new IllegalArgumentException("Customer id and address id must not be null");
        }
    }
}
