package com.imaginamos.farmatodo.backend.customerAddress;

import com.google.api.server.spi.config.*;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.ConflictException;
import com.google.api.server.spi.response.UnauthorizedException;
import com.googlecode.objectify.Key;
import com.imaginamos.farmatodo.backend.user.Authenticate;
import com.imaginamos.farmatodo.backend.user.Users;
import com.imaginamos.farmatodo.model.customer.Address;
import com.imaginamos.farmatodo.model.customer.AnswerAddNewAddress;
import com.imaginamos.farmatodo.model.user.User;
import com.imaginamos.farmatodo.model.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.logging.Logger;

@Api(name = "customerAddressEndpoint",
        version = "v1",
        apiKeyRequired = AnnotationBoolean.TRUE,
        scopes = {Constants.EMAIL_SCOPE},
        clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID_DEVELOPER, com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID},
        audiences = {Constants.ANDROID_AUDIENCE},
        namespace = @ApiNamespace(ownerDomain = Constants.OWNER_DOMAIN, ownerName = Constants.OWNER_NAME, packagePath = ""),
        description = "Update customer address.")
public class CustomerAddressEndpoint {

    private static final Logger log = Logger.getLogger(CustomerAddressEndpoint.class.getName());

    private final Users users;

    public CustomerAddressEndpoint() {
        users = new Users();
    }

    @ApiMethod(name = "updateAddressDefault", path = "/customerAddressEndpoint/address/{addressId}/default", httpMethod = ApiMethod.HttpMethod.PUT)
    public AnswerAddNewAddress updateAddressDefault(@Named("token") final String token,
                                                    @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                                    @Named("idCustomerWebSafe") final String idCustomerWebSafe,
                                                    @Named("addressId") Long addressId) throws BadRequestException, ConflictException, UnauthorizedException {

        validateDataToken(token, tokenIdWebSafe, idCustomerWebSafe);
        validateAuthenticateToken(token, tokenIdWebSafe);
        User user = getUserByCustomerWebSafe(idCustomerWebSafe);
        Address address = updateCustomerAddressDefault(addressId, user.getId());
        return buildAnswerAddNewAddressByUpdateAddressDefault(address);
    }

    private static Address updateCustomerAddressDefault(Long addressId, long userId) throws ConflictException {
        return CustomerAddress.updateAddressDefaultByCustomerAndAddress(userId, addressId);
    }

    private AnswerAddNewAddress buildAnswerAddNewAddressByUpdateAddressDefault(Address optionalAddress) {
        AnswerAddNewAddress answer = new AnswerAddNewAddress();
        answer.setConfirmation(true);
        answer.setMessage("Set default success");
        answer.setAddress(optionalAddress);
        return answer;
    }

    private User getUserByCustomerWebSafe(String idCustomerWebSafe) throws UnauthorizedException {
        Key<User> userKey = Key.create(idCustomerWebSafe);
        User user = users.findUserByKey(userKey);
        if (user == null)
            throw new UnauthorizedException(Constants.USER_NOT_FOUND);
        return user;
    }

    private void validateAuthenticateToken(String token, String tokenIdWebSafe) throws ConflictException, BadRequestException {
        if (!Authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);
    }

    private void validateDataToken(String token, String tokenIdWebSafe, String idCustomerWebSafe) throws ConflictException {
        if ((Objects.isNull(token) || Objects.isNull(tokenIdWebSafe) || Objects.isNull(idCustomerWebSafe)) ||
                (token.isEmpty() || tokenIdWebSafe.isEmpty() || idCustomerWebSafe.isEmpty()))
            throw new ConflictException(Constants.INVALID_TOKEN);
    }
}
