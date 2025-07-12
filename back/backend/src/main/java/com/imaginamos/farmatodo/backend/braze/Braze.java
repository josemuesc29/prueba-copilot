package com.imaginamos.farmatodo.backend.braze;

import com.google.api.server.spi.response.BadRequestException;
import com.imaginamos.farmatodo.model.customer.Customer;
import com.imaginamos.farmatodo.networking.models.braze.UpdateUserOnBrazeRequest;
import com.imaginamos.farmatodo.networking.services.ApiGatewayService;

public final class Braze {
    private Braze() {
        throw new AssertionError("Utility class - do not instantiate");
    }

    private static UpdateUserOnBrazeRequest buildUpdateUserOnBrazeRequest(Customer customer) throws BadRequestException {
        UpdateUserOnBrazeRequest updateUserOnBrazeRequest = new UpdateUserOnBrazeRequest();
        updateUserOnBrazeRequest.setAnalyticsUUID(customer.getAnalyticsUUID());
        updateUserOnBrazeRequest.setPhone(customer.getPhone());
        updateUserOnBrazeRequest.setDocumentNumber(customer.getDocumentNumber());
        updateUserOnBrazeRequest.setFirstName(customer.getFirstName());
        updateUserOnBrazeRequest.setLastName(customer.getLastName());
        updateUserOnBrazeRequest.setGender(customer.getGender());
        updateUserOnBrazeRequest.setCountryId(customer.getCountryId());
        return updateUserOnBrazeRequest;
    }

    private static void validateCustomer(Customer customer) throws BadRequestException {
        if (customer == null) {
            throw new BadRequestException("Customer cannot be null");
        }
    }

    public static void updateUserProfileOnBraze(Customer customer) throws BadRequestException {
        validateCustomer(customer);
        if (customer.hasValidAnalyticsUUID()) {
            try {
                ApiGatewayService.get().updateBrazeUserProfile(buildUpdateUserOnBrazeRequest(customer));
            } catch (Exception e) {
                throw new BadRequestException("Error al actualizar el perfil del usuario en Braze-> " + e.getMessage());
            }
        }
    }
}
