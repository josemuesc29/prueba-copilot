package com.farmatodo.backend.user;

import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.ConflictException;
import com.googlecode.objectify.Key;
import com.imaginamos.farmatodo.model.customer.Customer;
import com.imaginamos.farmatodo.model.user.Token;
import com.imaginamos.farmatodo.model.util.Constants;
import org.jasypt.util.password.StrongPasswordEncryptor;

import java.util.Date;
import java.util.logging.Logger;

import static com.farmatodo.backend.OfyService.ofy;

public class Authenticate {
  private static final Logger log = Logger.getLogger(Customer.class.getName());

  public boolean isValidToken(String token, String tokenIdWebSafe) throws ConflictException, BadRequestException {
    if (token == null || tokenIdWebSafe == null) {
      log.warning("Token vacios");
      throw new BadRequestException(Constants.INVALID_TOKEN);
    }
    Key<Token> keyToken = Key.create(tokenIdWebSafe);
    Token tokenValidator = ofy().load().key(keyToken).now();
    if (tokenValidator == null)
      throw new ConflictException(Constants.INVALID_TOKEN);
    StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
    Date today = new Date();

    return passwordEncryptor.checkPassword(token, tokenValidator.getToken());
    //return passwordEncryptor.checkPassword(token, tokenValidator.getToken()) && tokenValidator.getTokenExp().after(today);
  }

  public boolean isValidTokenCallCenter(String token, String tokenIdWebSafe) {
    Key<Token> keyToken = Key.create(tokenIdWebSafe);
    Token tokenValidator = ofy().load().key(keyToken).now();
    StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
    Date today = new Date();
    if (passwordEncryptor.checkPassword(token, tokenValidator.getToken()) && tokenValidator.getTokenExp().after(today))
      return true;
    return false;
  }
}
