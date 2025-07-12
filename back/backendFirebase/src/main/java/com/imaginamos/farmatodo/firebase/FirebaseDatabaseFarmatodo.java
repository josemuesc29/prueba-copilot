package com.imaginamos.farmatodo.firebase;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseCredentials;
import com.google.firebase.auth.UserRecord;
import com.google.gson.JsonObject;
import com.imaginamos.farmatodo.model.customer.Customer;
import com.imaginamos.farmatodo.model.util.URLConnections;
import org.json.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.security.InvalidParameterException;
import java.util.logging.Logger;

/**
 * Created by Eric on 15/06/2017.
 */

public class FirebaseDatabaseFarmatodo extends HttpServlet {
  private static final Logger log = Logger.getLogger(FirebaseDatabaseFarmatodo.class.getName());

  @Override
  public void init(ServletConfig config) {
    final String METHOD = "[FirebaseDatabaseFarmatodo.init]";

    log.info("[INI]-"+METHOD);

    String credential = config.getInitParameter("credential");
    String databaseUrl = config.getInitParameter("databaseUrl");

    log.info("[credential] : ["+credential+"]");
    log.info("[databaseUrl] : ["+databaseUrl+"]");

    InputStream resourceAsStream = config.getServletContext().getResourceAsStream(credential);
    FirebaseOptions options = null;

    try {
      options = new FirebaseOptions.Builder()
          .setCredential(FirebaseCredentials.fromCertificate(resourceAsStream))
          .setDatabaseUrl(databaseUrl)
          .build();
      log.info("Parametros Firebase creados correctamente...");
    } catch (IOException e) {
      log.warning("[Error al crear parametros de Firebase] => "+e.getMessage());
      e.printStackTrace();
    }
    FirebaseApp firebaseApp = FirebaseApp.initializeApp(options);
    log.info("firebaseApp : ["+firebaseApp.getName()+"]");
    log.info("[FIN]-"+METHOD);
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, InvalidParameterException, ServletException {
    final String METHOD = "[FirebaseDatabaseFarmatodo.doGet]";

    log.info("[INI]-"+METHOD);

    response.setContentType(URLConnections.CONTENT_TYPE_JSON);
    response.addHeader("Access-Control-Allow-Origin", "*");
    JsonObject jsonObject = new JsonObject();

    FirebaseAuth.getInstance().getUserByEmail("eric.david23@hotmail.com")
        .addOnSuccessListener(userRecord -> {
          // See the UserRecord reference doc for the contents of userRecord.
          log.info("Successfully fetched user data : [" + userRecord.getUid()+"]");
          log.info("Successfully fetched user data : [" + userRecord.getEmail()+"]");
        })
        .addOnFailureListener(e -> {
          log.info("Error fetching user data. Mensaje : " + e.getMessage());
        });

        /*
        DatabaseReference ref = FirebaseDatabase
                .getInstance()
                .getReference("restricted_access/secret_document");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Object document = dataSnapshot.getValue();
                System.out.println(document);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });*/
    PrintWriter out = response.getWriter();
    out.print(jsonObject);
    out.flush();
    out.close();
    log.info("[FIN]-"+METHOD);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, InvalidParameterException, ServletException {
    final String METHOD = "[Firebase.DatabaseFarmatodo.doPost]";

    log.info("[INI]-"+METHOD);

    StringBuilder jb = new StringBuilder();
    String line;
    JSONObject bodyJson = null;
    try {
      BufferedReader reader = request.getReader();
      while ((line = reader.readLine()) != null)
        jb.append(line);
    } catch (Exception e) { /*report an error*/ }

    try {

      JSONObject jsonObject = HTTP.toJSONObject(jb.toString());
      String method = jsonObject.getString("Method");
      bodyJson = new JSONObject(method);

    } catch (JSONException e) {
      log.info("Error al convertir request a json String. Mensaje : "+e.getMessage());
      throw new IOException("Error parsing JSON request string");
    }

    String email = bodyJson.getString("email");
    String password = bodyJson.getString("password");

    FirebaseAuth.getInstance().getUserByEmail(email)
        .addOnSuccessListener(userRecord -> {
          // See the UserRecord reference doc for the contents of userRecord.
          //System.out.println("Successfully fetched user data: " + userRecord.getUid());
          //System.out.println("Successfully fetched user data: " + userRecord.getEmail());
          UserRecord.UpdateRequest updateRequest = userRecord.updateRequest().setPassword(password);
          FirebaseAuth.getInstance().updateUser(updateRequest).addOnSuccessListener(userRecord2 -> {
            // See the UserRecord reference doc for the contents of userRecord.
            //System.out.println("Successfully fetched user 2 data: " + userRecord2.getUid());
            //System.out.println("Successfully fetched user 2 data: " + userRecord2.getEmail());
            //System.out.println("Successfully fetched user 2 data: " + userRecord2.getProviderId());

          }).addOnFailureListener(e -> {
            System.out.println("Error fetching user data: " + e.getMessage());
          });


        })
        .addOnFailureListener(e -> {
          System.out.println("Error fetching user data: " + e.getMessage());
        });
    log.info("[FIN]-"+METHOD);
  }
}
