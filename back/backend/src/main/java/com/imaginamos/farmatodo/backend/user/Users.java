package com.imaginamos.farmatodo.backend.user;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.imaginamos.farmatodo.model.customer.Customer;
import com.imaginamos.farmatodo.model.user.Credential;
import com.imaginamos.farmatodo.model.user.User;
import com.imaginamos.farmatodo.model.user.UserPass;

import java.io.Serializable;
import java.util.*;
import java.util.logging.Logger;

import static com.imaginamos.farmatodo.backend.OfyService.ofy;

/**
 * Created by mileniopc on 11/10/16.
 * Property of Imaginamos.
 */

public class Users implements Serializable {
  private static final Logger log = Logger.getLogger(Customer.class.getName());

  public User findUserByIdCustomer(int idCustomer) {
    return ofy().load().type(User.class).filter("id", idCustomer).first().now();
  }

  public User findUserByIdCustomerLastLogin(int idCustomer) {
    List<User> userList = ofy().load().type(User.class).filter("id", idCustomer).list();
    List<Long> datesUser = new ArrayList<>();
    if ( Objects.nonNull(userList) && !userList.isEmpty() ){
      userList.forEach(user -> datesUser.add(user.getLastLogin()));
    }

    if (!datesUser.isEmpty()){
      Long maxDate = Collections.min(datesUser);

      Optional<User> optionalUser = userList.stream().filter(userAux -> userAux.getLastLogin() == maxDate).findFirst();

      if (optionalUser.isPresent()){
        return optionalUser.get();
      }
    }

   return null;

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

  public Credential findCredentialByKey(Ref<User> userKey) {
    return ofy().load().type(Credential.class).ancestor(userKey).first().now();
  }

  public List<Credential> findUserByEmailList(String email) {
    return ofy().load().type(Credential.class).filter("email", email).filter("status", true).list();
  }

  public void deleteCredencialDataStore(Credential credential){
    ofy().delete().entity(credential).now();
  }

  public void deleteUserDataStore(User user){
    ofy().delete().entity(user).now();
  }

  public List<User> findUserByKeyList(Integer userId) {
    return ofy().load().type(User.class).filter("id", userId).list();
  }



}