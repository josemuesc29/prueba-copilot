package com.farmatodo.backend.user;

import com.googlecode.objectify.Key;
import com.imaginamos.farmatodo.model.customer.Customer;
import com.imaginamos.farmatodo.model.user.Credential;
import com.imaginamos.farmatodo.model.user.User;
import com.imaginamos.farmatodo.model.user.UserPass;

import java.io.Serializable;
import java.util.logging.Logger;

import static com.farmatodo.backend.OfyService.ofy;

public class Users implements Serializable {
  private static final Logger log = Logger.getLogger(Customer.class.getName());

  public User findUserByIdCustomer(int idCustomer) {
    return ofy().load().type(User.class).filter("id", idCustomer).first().now();
  }

  public User findUserByKey(Key<User> key) {
    return ofy().load().key(key).now();
  }

  public Credential findUserByEmail(String email) {
    return ofy().load().type(Credential.class).filter("email", email).filter("status", true).first().now();
  }

  public UserPass findPassByAncestor(Key<User> userKey) {
    return ofy().load().type(UserPass.class).filter("active", true).ancestor(userKey).first().now();
  }

}
