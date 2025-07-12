package com.farmatodo.backend.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

//import com.google.appengine.repackaged.com.google.gson.JsonObject;
import com.google.gson.JsonObject;
import com.google.gson.JsonObject;
import com.google.gson.JsonObject;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.imaginamos.farmatodo.model.product.*;
import com.imaginamos.farmatodo.model.store.Store;
import com.imaginamos.farmatodo.model.util.URLConnections;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.rmi.UnexpectedException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static com.farmatodo.backend.OfyService.ofy;

/**
 * Created by Eric on 24/02/2017.
 */

public class SubstitutesUpload extends HttpServlet {
  private List<Substitutes> substitutesToSave = new ArrayList<>();
  private Queue queue = QueueFactory.getQueue("productmanager");
  private static final Logger log = Logger.getLogger(SubstitutesUpload.class.getName());

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    final String METHOD = "[SubstitutesUpload.doGet]";
    log.info("[INI]-"+METHOD);

    throw new UnsupportedOperationException("Deprecated");

    /*
    HttpURLConnection httpURLConnection = URLConnections.getConnection(URLConnections.GET, URLConnections.URL_SUBSTITUTES + "0");
    log.info("Iniciando conexion a URL : ["+URLConnections.URL_SUBSTITUTES+"0]");

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

        log.info("Mapeando respuesta del servicio de [Json] a [List<ItemCross> itemList]...");
        List<ItemCross> itemList = objectMapper.readValue(responseJson.toString(), objectMapper.getTypeFactory().constructCollectionType(List.class, ItemCross.class));

        log.info("FOR (ItemCross productInformation : itemList)");
        for (ItemCross productInformation : itemList) {
          log.info("if (productInformation.getSubstitutes() != null && !productInformation.getSubstitutes().isEmpty()) " +
                  ": ["+(productInformation.getSubstitutes() != null && !productInformation.getSubstitutes().isEmpty())+"]");
          if (productInformation.getSubstitutes() != null && !productInformation.getSubstitutes().isEmpty()) {
            this.substitutes(productInformation);
          }
        }

        log.info("IF (!this.substitutesToSave.isEmpty()) : ["+(!this.substitutesToSave.isEmpty())+"]");
        if (!this.substitutesToSave.isEmpty()) {
          log.info("Guardando substitutos en el Datastore...");
          ofy().save().entities(this.substitutesToSave).now();
        }

        log.info("Se adiciona tarea a la cola [productmanager] con URL : [/substitutesUpload]. Params: page=>["+Integer.toString(1)+"]");
        queue.add(ofy().getTransaction(), TaskOptions.Builder.withUrl("/substitutesUpload").param("page", Integer.toString(1)));

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

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      InvalidParameterException, ServletException {
    throw new UnsupportedOperationException("Deprecated");

    /*
    final String METHOD = "[SubstitesUpload.doPost]";
    log.info("[INI]"+METHOD);

    this.substitutesToSave = new ArrayList<>();
    int page = Integer.parseInt(request.getParameter("page"));

    log.info("Procesando pagina ["+page+"]");

    URL url = new URL(URLConnections.URL_SUBSTITUTES + page);
    log.info("Inicia conexion  a URL : ["+URLConnections.URL_SUBSTITUTES+"]");

    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
    httpURLConnection.setRequestMethod(URLConnections.GET);
    httpURLConnection.setRequestProperty("Accept-Charset", "UTF-8");
    httpURLConnection.setConnectTimeout(100000);

    int responseCode = httpURLConnection.getResponseCode();

    log.info("ResponseCode : ["+responseCode+"]");

    switch (responseCode) {
      case 200:

        log.info("[INI]-[case 200]");

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8"));
        String inputLine;
        StringBuffer responseJson = new StringBuffer();

        while ((inputLine = bufferedReader.readLine()) != null) {
          responseJson.append(inputLine);
        }
        bufferedReader.close();
        ObjectMapper objectMapper = new ObjectMapper();

        log.info("Mapeando respuesta del servicio de [Json] a [List<ItemCross> itemList]...");
        List<ItemCross> itemList = objectMapper.readValue(responseJson.toString(), objectMapper.getTypeFactory().constructCollectionType(List.class, ItemCross.class));

        log.info("FOR (ItemCross productInformation : itemList)");
        for (ItemCross productInformation : itemList) {
          log.info("IF (productInformation.getSubstitutes() != null && !productInformation.getSubstitutes().isEmpty()) : " +
                   "["+(productInformation.getSubstitutes() != null && !productInformation.getSubstitutes().isEmpty())+"]");
          if (productInformation.getSubstitutes() != null && !productInformation.getSubstitutes().isEmpty()) {
            this.substitutes(productInformation);
          }
        }

        log.info("IF (!this.substitutesToSave.isEmpty()) : ["+(!this.substitutesToSave.isEmpty())+"]");
        if (!this.substitutesToSave.isEmpty()) {
          log.info("Guardando substitutes en el Datastore...");
          ofy().save().entities(this.substitutesToSave).now();
        }

        log.info("Se adiciona tarea a la cola [productmanager] con URL : [/substitutesUpload]. Parametros : page=>["+Integer.toString(page + 1)+"]");
        queue.add(ofy().getTransaction(), TaskOptions.Builder.withUrl("/substitutesUpload").param("page", Integer.toString(page + 1)));

        log.info("[INI]-[case 200]");

        break;
      case 204:
        break;
      case 400:
        break;
      case 500:
        break;
      default:
        break;
    }
    log.info("[FIN]-"+METHOD);

     */
  }

  private void substitutes(ItemCross item) {
    final String METHOD = "[Substitutes.substitutes]";
    log.info("[INI]-"+METHOD);

    Key<ItemGroup> itemGroupKey = Key.create(ItemGroup.class, "1");

    log.info("Consultando Item en el Datastore con Item.id = ["+Long.toString(item.getId())+"]");
    Item item1 = ofy().load().key(Key.create(itemGroupKey, Item.class, Long.toString(item.getId()))).now();

    log.info("IF (item1 != null) : ["+(item1 != null)+"]");
    if (item1 != null) {
      List<Substitutes> substitutesDelete = ofy().load().type(Substitutes.class).ancestor(item1).list();

      log.info("Eliminando subtitutos del item consultado en el Datastore...");
      ofy().delete().entities(substitutesDelete).now();

      List<Suggested> substitutesCreate = item.getSubstitutes();

      log.info("IF (substitutesCreate != null) : ["+(substitutesCreate != null)+"]");
      if (substitutesCreate != null) {
        Substitutes substitutes = new Substitutes();
        substitutes.setIdItem(UUID.randomUUID().toString());
        substitutes.setIdItemParent(Ref.create(Key.create(itemGroupKey, Item.class, item1.getItemId())));
        substitutes.setSuggested(item.getSubstitutes());
        this.substitutesToSave.add(substitutes);
      }
    }
    log.info("[FIN]-"+METHOD);
  }
}
