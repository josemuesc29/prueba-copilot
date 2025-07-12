package com.imaginamos.farmatodo.backend.firebase;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.JsonObject;
import com.googlecode.objectify.Key;
import com.imaginamos.farmatodo.backend.user.Users;
import com.imaginamos.farmatodo.model.customer.Customer;
import com.imaginamos.farmatodo.model.user.User;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.imaginamos.farmatodo.backend.OfyService.ofy;

/**
 * Created by mileniopc on 11/10/16.
 * Property of Imaginamos.
 */

public class FirebaseServlet extends HttpServlet {
  private final Users users = new Users();
  private static final Logger log = Logger.getLogger(Customer.class.getName());

  @Override
  public void init(ServletConfig config) {
    String credential = config.getInitParameter("credential");
    //log.warning(credential);
    String databaseUrl = config.getInitParameter("databaseUrl");
    //log.warning(databaseUrl);

    FirebaseOptions options = new FirebaseOptions.Builder()
        .setServiceAccount(config.getServletContext().getResourceAsStream(credential))
        .setDatabaseUrl(databaseUrl)
        .build();
    FirebaseApp.initializeApp(options);
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) {
    doPost(req, resp);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) {
    try {
      String idCustomerWebSafe = req.getHeader("idCustomerWebSafe");
      //log.warning(idCustomerWebSafe);

      Key<User> userKey = Key.create(idCustomerWebSafe);
      User user = users.findUserByKey(userKey);
      String uid = user.getIdUser();
      //log.warning(uid);
      String tokenFirebase = FirebaseAuth.getInstance().createCustomToken(uid);

      user.setTokenFirebase(tokenFirebase);
      ofy().save().entity(user).now();
      user.setIdUserWebSafe(userKey.toWebSafeString());

      //log.warning(tokenFirebase);

      resp.setStatus(HttpServletResponse.SC_OK);
      resp.setContentType("application/json");
      resp.addHeader("Access-Control-Allow-Origin", "*");
      JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty("tokenFirebase", tokenFirebase);
      PrintWriter out = resp.getWriter();
      out.print(jsonObject);
      out.flush();
      out.close();
    } catch (IOException ex) {
      log.log(Level.SEVERE, ex.getMessage(), ex);
    }
  }

  @Override
  public void destroy() {

  }
}