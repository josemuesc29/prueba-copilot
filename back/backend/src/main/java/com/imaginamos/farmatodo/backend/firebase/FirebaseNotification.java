package com.imaginamos.farmatodo.backend.firebase;


import com.google.api.server.spi.response.ConflictException;
import com.googlecode.objectify.Key;
import com.imaginamos.farmatodo.backend.user.Users;
import com.imaginamos.farmatodo.model.algolia.ScanAndGoPushNotificationProperty;
import com.imaginamos.farmatodo.model.customer.Customer;
import com.imaginamos.farmatodo.model.user.DeviceRegistry;
import com.imaginamos.farmatodo.model.user.PushNotification;
import com.imaginamos.farmatodo.model.user.User;
import com.imaginamos.farmatodo.model.util.Answer;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.model.util.URLConnections;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.simple.JSONObject;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.imaginamos.farmatodo.backend.OfyService.ofy;

/**
 * Created by eric on 18/04/17.
 */
public class FirebaseNotification {
  private static final Logger log = Logger.getLogger(Customer.class.getName());
  private static final Users usersFunctions = new Users();

  public static void notificationervice(Integer id, String title, String message,
                                        Integer minutes, String type, long idOrder) {
    User user = ofy().load().type(User.class).filter("id", id).first().now();
    List<DeviceRegistry> deviceRegistryList = ofy().load().type(DeviceRegistry.class).ancestor(user).list();
    for (DeviceRegistry deviceRegistry : deviceRegistryList) {
      //log.info("Request notificationervice(): " + title+" "+message+" "+ user+" "+minutes+" "+type+" "+idOrder);
      sendNotification(deviceRegistry, title, message, Key.create(user), minutes, type, idOrder);
    }
  }

  public static void sendNotification(DeviceRegistry registrationRecord, String title, String message,
                                      Key<User> userKey, Integer minutes, String type, Long idOrder) {
    HttpClient httpClient = HttpClientBuilder.create().build();
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("to", registrationRecord.getFirebaseTokenDevice());
    jsonObject.put("title", title);
    JSONObject jsonElementToken = new JSONObject();
    jsonElementToken.put("title", title);
    jsonElementToken.put("sound", "default");
    jsonElementToken.put("body", message);
    jsonElementToken.put("type", type);
    jsonElementToken.put("id_order", idOrder);
    jsonElementToken.put("minutes", minutes);
    jsonElementToken.put("click_action", ".main.activity.ActivityMain");

    jsonObject.put("notification", jsonElementToken);
    jsonObject.put("data", jsonElementToken);
    jsonObject.put("priority", "high");
    /*jsonObject.put("content_available", true);
    jsonObject.put("content-available", true);*/
    jsonObject.put("badge", "1");

    try {

      HttpURLConnection httpURLConnection = URLConnections.getConnection(URLConnections.POST, Constants.URL_FIREBASE);
      httpURLConnection.setRequestProperty("Authorization", "key=" + Constants.FIREBASE_SERVER_KEY);
      httpURLConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
      httpURLConnection.setRequestProperty("Accept-Charset", "UTF-8");

      OutputStreamWriter wr = new OutputStreamWriter(httpURLConnection.getOutputStream(), "UTF-8");
//      log.info(jsonObject.toJSONString());
      wr.write(jsonObject.toJSONString());
      wr.flush();
      int responseCode = httpURLConnection.getResponseCode();
      if (responseCode != 200) {
        log.warning("Push error");
      }
    } catch (Exception ex) {
      log.log(Level.SEVERE, ex.getMessage(), ex);
    }
  }

  public static void generalNotificationService(Integer userId, String title, String message, String type) {
    User user = ofy().load().type(User.class).filter("id", userId).first().now();
    List<DeviceRegistry> deviceRegistryList = ofy().load().type(DeviceRegistry.class).ancestor(user).list();
    for (DeviceRegistry deviceRegistry : deviceRegistryList) {
      sendGeneralNotification(deviceRegistry, title, message, Key.create(user), type);
    }
  }

  public static void sendGeneralNotification(DeviceRegistry registrationRecord, String title, String message,
                                      Key<User> userKey, String type) {
    HttpClient httpClient = HttpClientBuilder.create().build();
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("to", registrationRecord.getFirebaseTokenDevice());
    jsonObject.put("title", title);
    JSONObject jsonElementToken = new JSONObject();
    jsonElementToken.put("title", title);
    jsonElementToken.put("sound", "default");
    jsonElementToken.put("body", message);
    jsonElementToken.put("type", type);
    jsonElementToken.put("click_action", ".main.activity.ActivityMain");

    jsonObject.put("notification", jsonElementToken);
    jsonObject.put("data", jsonElementToken);
    jsonObject.put("priority", "high");
    jsonObject.put("content_available", true);
    jsonObject.put("content-available", true);
    jsonObject.put("badge", "1");
    try {

      HttpURLConnection httpURLConnection = URLConnections.getConnection(URLConnections.POST, Constants.URL_FIREBASE);
      httpURLConnection.setRequestProperty("Authorization", "key=" + Constants.FIREBASE_SERVER_KEY);
      httpURLConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
      httpURLConnection.setRequestProperty("Accept-Charset", "UTF-8");

      OutputStreamWriter wr = new OutputStreamWriter(httpURLConnection.getOutputStream(), "UTF-8");
      //log.warning(jsonObject.toJSONString());
      wr.write(jsonObject.toJSONString());
      wr.flush();
      int responseCode = httpURLConnection.getResponseCode();
      if (responseCode != 200) {
        log.warning("Push error");
      }
    } catch (Exception ex) {
      log.log(Level.SEVERE, ex.getMessage(), ex);
    }
  }

  public static Answer sendScanAndGoPushNotification(final Integer id,
                                    final ScanAndGoPushNotificationProperty scanAndGoPushNotificationProperty,
                                    final Long orderId){
    try {
//      log.info("method senPushNotification: Start send notification scan and go service...");
      User user = ofy().load().type(User.class).filter("id", id).first().now();
      if (user == null)
        throw new ConflictException(Constants.USER_NOT_FOUND);
      if (Objects.nonNull(scanAndGoPushNotificationProperty)) {
//        log.info("pushNotificationProperty -> Hours:" + scanAndGoPushNotificationProperty.getTimeToPushInHours()
//                + ", message: " + scanAndGoPushNotificationProperty.getMessage());
        PushNotification pushNotification = ofy().load().type(PushNotification.class).ancestor(user).first().now();

        if (Objects.isNull(pushNotification)) {
          pushNotification = new PushNotification();
          pushNotification.setUser(Key.create(user));
          pushNotification.setIdPushNotification(UUID.randomUUID().toString());
        }
//        log.info("pushNotification -> timeLastPush:" + pushNotification.getTimeLastPush());
        //Verify last notification
        if (Objects.isNull(pushNotification.getTimeLastPush()) || verifyTimeToPush(pushNotification.getTimeLastPush(),
                scanAndGoPushNotificationProperty.getTimeToPushInHours())) {
          // SEND NOTIFICATION
//          log.info("Sending notification...");
          // TODO: pendiente definir nuevo tipo para facturado con las apps
          notificationervice(user.getId(), scanAndGoPushNotificationProperty.getTitle()+orderId, scanAndGoPushNotificationProperty.getMessage(),0,"rder_incoming", orderId);
          pushNotification.setTimeLastPush(DateTime.now().getMillis());
//          log.info("Notification sended");
        }
        ofy().save().entity(pushNotification).now();
      }
    } catch (Exception e) {
      log.warning("ERROR -> " + e.toString() + " , " + e.getCause());
      return new Answer(false);
    }
    return new Answer(true);
  }

  private static boolean verifyTimeToPush(Long timeLast, int nextTime) {
    DateTime lastDate = new DateTime(timeLast, DateTimeZone.forID("America/Bogota"));
    DateTime nextDate = lastDate.plusHours(nextTime);
    return nextDate.isBeforeNow();
  }
}
