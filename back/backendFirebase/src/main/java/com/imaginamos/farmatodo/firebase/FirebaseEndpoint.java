package com.imaginamos.farmatodo.firebase;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.imaginamos.farmatodo.model.customer.Customer;
import com.imaginamos.farmatodo.model.util.Answer;
import com.imaginamos.farmatodo.model.util.Constants;

import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

/**
 * Created by USUARIO on 06/07/2017.
 */

@Api(name = "firebaseEndpoint",
    version = "v1",
    scopes = {Constants.EMAIL_SCOPE, Constants.PHONE_SCOPE},
    clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID_DEVELOPER, com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID},
    audiences = {Constants.ANDROID_AUDIENCE},
    namespace = @ApiNamespace(ownerDomain = Constants.OWNER_DOMAIN, ownerName = Constants.OWNER_NAME),
    description = "Firebase interaction")
public class FirebaseEndpoint {
  private static final Logger log = Logger.getLogger(Customer.class.getName());

  public FirebaseEndpoint() {
  }

  @ApiMethod(name = "changePassword", path = "/firebaseEndpoint/changePassword", httpMethod = ApiMethod.HttpMethod.POST)
  public Answer changePassword(final Customer customer) {

    FirebaseAuth.getInstance().getUserByEmail(customer.getEmail())
        .addOnSuccessListener(userRecord -> {
          // See the UserRecord reference doc for the contents of userRecord.
          //log.info("Successfully fetched user data: " + userRecord.getUid());
          //log.info("Successfully fetched user data: " + userRecord.getEmail());
          UserRecord.UpdateRequest updateRequest = userRecord.updateRequest().setPassword(customer.getPassword());
          FirebaseAuth.getInstance().updateUser(updateRequest).addOnSuccessListener(userRecord2 -> {
            // See the UserRecord reference doc for the contents of userRecord.
            log.info("Successfully fetched user 2 data: " + userRecord2.getUid());
            log.info("Successfully fetched user 2 data: " + userRecord2.getEmail());
            log.info("Successfully fetched user 2 data: " + userRecord2.getProviderId());

          }).addOnFailureListener(e -> {
              log.info("Error fetching user data: " + e.getMessage());
          });


        })
        .addOnFailureListener(e -> {
          System.out.println("Error fetching user data: " + e.getMessage());
        });
    Answer answer = new Answer();
    answer.setConfirmation(true);
    return answer;
  }

    @ApiMethod(name = "getUserUid", path = "/firebaseEndpoint/getUserUid", httpMethod = ApiMethod.HttpMethod.GET)
    public Answer getUserByUid(@Named("uid") final String uid) {
        log.info("Method: getUserUid uid: " + uid);
        UserRecord taskRecord = null;
        try {
            taskRecord = FirebaseAuth.getInstance().getUserAsync(uid).get();
        } catch (InterruptedException | ExecutionException e) {
            log.info("Error fetching user data: " + e.getMessage());
        }
        log.info("Method: getUserUid finaliza email : " + taskRecord.getEmail());
        log.info("Method: getUserUid finaliza phone : " + taskRecord.getPhoneNumber());
        Answer answer = new Answer();
        answer.setMessage(taskRecord.getPhoneNumber());
        return answer;
    }
}
