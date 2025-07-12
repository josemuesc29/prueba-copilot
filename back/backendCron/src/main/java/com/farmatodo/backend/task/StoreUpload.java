package com.farmatodo.backend.task;

import com.farmatodo.backend.OfyService;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.imaginamos.farmatodo.model.location.City;
import com.imaginamos.farmatodo.model.location.Net;
import com.imaginamos.farmatodo.model.store.Store;
import com.imaginamos.farmatodo.model.store.StoreGroup;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;


/**
 * Created by mileniopc on 11/23/16.
 * Property of Imaginamos.
 */

public class StoreUpload extends HttpServlet {
  private static final Logger log = Logger.getLogger(StoreUpload.class.getName());

  @Deprecated
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, InvalidParameterException, ServletException {

    new UnsupportedOperationException("Operacion no disponible");

    /*
    final String METHOD = "[StoreUpload.doPost]";
    log.info("[INI]-"+METHOD);

    URL url = new URL(URLConnections.URL_STORES);
    log.info("Iniciando conexion a URL : ["+URLConnections.URL_STORES+"]");

    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
    httpURLConnection.setRequestMethod(URLConnections.GET);
    httpURLConnection.setRequestProperty("Accept-Charset", "UTF-8");
    response.setContentType(URLConnections.CONTENT_TYPE_JSON);
    response.addHeader("Access-Control-Allow-Origin", "*");
    JsonObject jsonObject = new JsonObject();

    int responseCode = httpURLConnection.getResponseCode();
    log.info("ResponseCode : ["+responseCode+"]");

    switch (responseCode) {
      case 200:

        log.info("[INI]-[case 200]");

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8"));
        String inputLine;
        StringBuilder responseJson = new StringBuilder();

        while ((inputLine = bufferedReader.readLine()) != null) {
          responseJson.append(inputLine);
        }
        bufferedReader.close();

        ObjectMapper objectMapper = new ObjectMapper();

        log.info("Mapeando respuesta del servicio de [Json] a [List<Store> storeList]...");
        List<Store> storeList = objectMapper.readValue(responseJson.toString(), objectMapper.getTypeFactory().constructCollectionType(List.class, Store.class));
        this.saveStores(storeList);

        response.setStatus(HttpServletResponse.SC_OK);
        jsonObject.addProperty("Message", URLConnections.SUCCESS_MESSAGE);
        jsonObject.addProperty("Code", responseCode);

        log.info("[FIN]-[case 200]");

        break;
      case 204:
        jsonObject.addProperty("Message", URLConnections.NO_CONTENT);
        jsonObject.addProperty("Code", responseCode);
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        break;
      case 400:
        jsonObject.addProperty("Message", URLConnections.BAD_REQUEST);
        jsonObject.addProperty("Code", responseCode);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        break;
      case 500:
        jsonObject.addProperty("Message", URLConnections.SERVER_ERROR);
        jsonObject.addProperty("Code", responseCode);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        break;
      default:
        jsonObject.addProperty("Message", URLConnections.DEFAULT);
        jsonObject.addProperty("Code", responseCode);
        response.setStatus(HttpServletResponse.SC_CONFLICT);
        break;
    }
    PrintWriter out = response.getWriter();
    out.print(jsonObject);
    out.flush();
    out.close();

    log.info("[FIN]-"+METHOD);

     */
  }

  private void saveStores(List<Store> storeList) {
    final String METHOD = "[StoreUpload.saveStores]";
    log.info("[INI]-"+METHOD);

    //log.info("FOR (Store store : storeList)");
    for (Store store : storeList) {
      City city = OfyService.ofy().load().type(City.class).filter("id", store.getCity()).first().now();

      //log.info("FOR (Net net : store.getNets())");
      for (Net net : store.getNets()) {
        Key<StoreGroup> storeGroup = this.saveStoreGroup(net.getStoreGroupId(), net.getStoreGroupName(), city);
        Store store1 = OfyService.ofy().load().type(Store.class).filter("id", store.getId()).ancestor(storeGroup).first().now();
        if (store1 != null) {
          store.setIdStore(store1.getIdStore());
          store.setOwner(store1.getOwner());
        } else {
          store.setIdStore(UUID.randomUUID().toString());
          store.setOwner(Ref.create(storeGroup));
        }
        //log.info("Guardando informacion de la tienda en el Datastore...");
        OfyService.ofy().save().entity(store).now();
      }
    }

    log.info("[FIN]-"+METHOD);
  }

  private Key<StoreGroup> saveStoreGroup(Long storeGroupId, String storeGroupName, City city) {
    final String METHOD = "[StoreUpload.saveStoreGroup]";
    log.info("[INI]-"+METHOD);

    //log.info("Consultando [StoreGroup] en el Datastore por ciudades...");
    StoreGroup storeGroup = OfyService.ofy().load().type(StoreGroup.class).filter("storeGroupId", storeGroupId).ancestor(city).first().now();

    log.info("IF (storeGroup != null): ["+(storeGroup != null)+"]");
    if (storeGroup != null) {
      storeGroup.setStoreGroupName(storeGroupName);
    } else {
      storeGroup = new StoreGroup();
      storeGroup.setIdStoreGroup(UUID.randomUUID().toString());
      storeGroup.setOwner(Ref.create(Key.create(City.class, city.getIdCity())));
      storeGroup.setStoreGroupId(storeGroupId);
      storeGroup.setStoreGroupName(storeGroupName);
    }

    log.info("[FIN]-"+METHOD);

    return OfyService.ofy().save().entity(storeGroup).now();
  }
}
