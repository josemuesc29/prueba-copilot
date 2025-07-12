package com.imaginamos.farmatodo.backend.callcenter;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.*;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.Work;
import com.imaginamos.farmatodo.backend.util.CoreConnection;
import com.imaginamos.farmatodo.model.callcenter.CallCenterProfile;
import com.imaginamos.farmatodo.model.callcenter.CallCenterUser;
import com.imaginamos.farmatodo.model.callcenter.UserEmployer;
import com.imaginamos.farmatodo.model.user.Credential;
import com.imaginamos.farmatodo.model.user.User;
import com.imaginamos.farmatodo.model.user.UserPass;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.model.util.URLConnections;
import org.jasypt.util.password.StrongPasswordEncryptor;
import org.json.simple.JSONObject;

import javax.inject.Named;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Logger;

import static com.imaginamos.farmatodo.backend.OfyService.ofy;

/**
 * Created by lelal on 1/11/2016.
 * Created by Imaginamos
 */

@Api(name = "callCenterEndpoint",
    version = "v1",
    scopes = {Constants.EMAIL_SCOPE},
    apiKeyRequired = AnnotationBoolean.TRUE,
    clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID_DEVELOPER, com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID},
    audiences = {Constants.ANDROID_AUDIENCE},
    namespace = @ApiNamespace(ownerDomain = Constants.OWNER_DOMAIN, ownerName = Constants.OWNER_NAME, packagePath = ""),
    description = "Stores, deletes, edits and queries for call center users and credentials.")
public class CallCenterEndpoint {

  private static final Logger log = Logger.getLogger(CallCenterUser.class.getName());

  /**
   * Creation of chat assistant. The service saves the assistant for the chat in the Datastore.
   *
   * @param keyClient      client's token
   * @param profileName    CallCenterProfile's name
   * @param callCenterUser Object of class 'CallCenterUser' that contain data to store of a new CallCenterUser.
   * @return Object of class 'CallCenterUser' stored.
   * @throws UnauthorizedException
   */
  @ApiMethod(name = "createCallCenterUser", path = "/callCenterEndpoint/createUser", httpMethod = ApiMethod.HttpMethod.POST)
  public CallCenterUser createCallCenterUser(@Named("keyClient") final String keyClient,
                                             @Named("profileName") final String profileName,
                                             final CallCenterUser callCenterUser) throws UnauthorizedException {
    if (!keyClient.equals(Constants.KEY_SECURE_CLIENT))
      throw new UnauthorizedException(Constants.ERROR_ACCESS_DENIED);

    final CallCenterProfile callCenterProfile = ofy().load().type(CallCenterProfile.class).filter("name", profileName).first().now();
    //log.warning(callCenterProfile.getIdCallCenterProfile());
    final Key<CallCenterProfile> callCenterProfileKey = Key.create(CallCenterProfile.class, callCenterProfile.getIdCallCenterProfile());

    return ofy().transact(new Work<CallCenterUser>() {
      @Override
      public CallCenterUser run() {
        String passEnc = encrypt(callCenterUser.getPassword());
        User user = new User();
        user.setIdUser(UUID.randomUUID().toString());
        user.setRole("Client");
        Key<User> userKey = Key.create(User.class, user.getIdUser());
        user.setIdUserWebSafe(userKey.toWebSafeString());

        Credential credential = new Credential();
        credential.setIdCredential(UUID.randomUUID().toString());
        credential.setConfirmed(true);
        credential.setCreateAt(new Date());
        credential.setEmail(callCenterUser.getLogin());
        credential.setLastLogin(new Date());
        credential.setOwner(Ref.create(userKey));
        credential.setStatus(true);
        Key<Credential> credentialKey = ofy().save().entity(credential).now();
        credential.setIdCredentialWebSafe(credentialKey.toWebSafeString());

        UserPass userPass = new UserPass();
        userPass.setIdUserPass(UUID.randomUUID().toString());
        userPass.setOwner(Ref.create(userKey));
        userPass.setCreateAt(new Date());
        userPass.setActive(true);
        userPass.setPassword(passEnc);
        Key<UserPass> userPassKey = ofy().save().entity(userPass).now();
        userPass.setIdUserPassWebSafe(userPassKey.toWebSafeString());
        user.setIdUserWebSafe(userKey.toWebSafeString());

        CallCenterUser callCenterUser2 = new CallCenterUser();
        callCenterUser2.setIdUser(UUID.randomUUID().toString());
        callCenterUser2.setIdCallCenterProfile(Ref.create(callCenterProfileKey));
        callCenterUser2.setUserKey(userKey);
        callCenterUser2.setLogin(credential.getEmail());
        callCenterUser2.setName(callCenterUser.getName());
        callCenterUser2.setPassword(encrypt(callCenterUser.getPassword()));
        callCenterUser2.setStatus(callCenterUser.isStatus());
        callCenterUser2.setTitle(callCenterUser.getTitle());
        callCenterUser2.setPhotoUrl(callCenterUser.getPhotoUrl());
        callCenterUser2.setLastUpdateDate(new Date());

        ofy().save().entity(user).now();
        Key<CallCenterUser> callCenterUserKey = ofy().save().entity(callCenterUser2).now();

        callCenterUser2.setIdCallCenterUserWebSafe(callCenterUserKey.toWebSafeString());
        return callCenterUser2;
      }
    });
  }

  /**
   * Creation of a CallCenterProfile
   *
   * @param keyClient         client's secure key
   * @param callCenterProfile Object of class 'CallCenterProfile' that contain data to store of a new CallCenterProfile.
   * @return Object of class 'CallCenterProfile' stored.
   * @throws UnauthorizedException
   */

  @ApiMethod(name = "createCallCenterProfile", path = "/callCenterEndpoint/createProfile", httpMethod = ApiMethod.HttpMethod.POST)
  public CallCenterProfile createCallCenterProfile(@Named("keyClient") final String keyClient,
                                                   final CallCenterProfile callCenterProfile) throws UnauthorizedException {
    if (!keyClient.equals(Constants.KEY_SECURE_CLIENT))
      throw new UnauthorizedException(Constants.ERROR_ACCESS_DENIED);

    return ofy().transact(new Work<CallCenterProfile>() {
      @Override
      public CallCenterProfile run() {
        CallCenterProfile callCenterProfile2 = new CallCenterProfile();
        callCenterProfile2.setIdCallCenterProfile(UUID.randomUUID().toString());
        callCenterProfile2.setName(callCenterProfile.getName());
        callCenterProfile2.setDescription(callCenterProfile.getDescription());
        Key<CallCenterProfile> callCenterProfileKey = ofy().save().entity(callCenterProfile2).now();
        callCenterProfile2.setIdCallCenterProfileWebSafe(callCenterProfileKey.toWebSafeString());
        return callCenterProfile2;
      }
    });
  }

  @SuppressWarnings("ALL")
  @ApiMethod(name = "loginOperator", path = "/callCenterEndpoint/loginOperator", httpMethod = ApiMethod.HttpMethod.GET)
  public UserEmployer loginOperator(@Named("keyClient") final String keyClient,
                                    @Named("email") final String email,
                                    @Named("password") final String password)
          throws UnauthorizedException, ConflictException, IOException, BadRequestException, InternalServerErrorException, NotFoundException {
    JSONObject customerJson = new JSONObject();
    customerJson.put("email", email);
    customerJson.put("password", password);

    return CoreConnection.postRequest(URLConnections.URL_LOGIN_EMPLOYER, customerJson.toJSONString(), UserEmployer.class);
  }

  /* Support methods */
  private String encrypt(String password) {
    StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
    return passwordEncryptor.encryptPassword(password);
  }

}
