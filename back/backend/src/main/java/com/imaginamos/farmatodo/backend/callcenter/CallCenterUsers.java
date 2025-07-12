package com.imaginamos.farmatodo.backend.callcenter;

import com.googlecode.objectify.Key;
import com.imaginamos.farmatodo.model.callcenter.CallCenterProfile;
import com.imaginamos.farmatodo.model.callcenter.CallCenterUser;
import com.imaginamos.farmatodo.model.user.User;

import static com.imaginamos.farmatodo.backend.OfyService.ofy;

/**
 * Created by mileniopc on 10/28/16.
 * Property of Imaginamos.
 */

public class CallCenterUsers {

  public CallCenterUser findCallCenterUserByKey(Key<CallCenterUser> key) {
    return ofy().load().key(key).now();
  }

  public CallCenterUser findCustomerByLogin(String login) {
    return ofy().load().type(CallCenterUser.class).filter("login", login).first().now();
  }

  public CallCenterProfile findProfileByKey(Key<CallCenterProfile> callCenterProfileKey) {
    return ofy().load().key(callCenterProfileKey).now();
  }

  public CallCenterUser findCallCenterUserByUserKey(Key<User> key) {
    return ofy().load().type(CallCenterUser.class).filter("userKey", key).first().now();
  }
}