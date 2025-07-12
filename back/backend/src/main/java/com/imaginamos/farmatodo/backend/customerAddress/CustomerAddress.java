package com.imaginamos.farmatodo.backend.customerAddress;

import com.google.api.server.spi.response.ConflictException;
import com.imaginamos.farmatodo.model.customer.Address;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.networking.services.customerAddress.CustomerAddressApiService;

import java.util.Optional;
import java.util.logging.Logger;

public class CustomerAddress {

    private static final Logger log = Logger.getLogger(CustomerAddress.class.getName());

    public static Address updateAddressDefaultByCustomerAndAddress(Long customerId,Long addressId) throws ConflictException {
        Optional<Address> optionalAddress;
        try {
            optionalAddress = CustomerAddressApiService.get().updateCustomerAddressDefault(customerId, addressId);
        } catch (Exception ex) {
            throw new ConflictException(Constants.DEFAULT_MESSAGE);
        }
        if (optionalAddress.isEmpty()) throw new ConflictException(Constants.DEFAULT_MESSAGE);
        return optionalAddress.get();
    }

}
