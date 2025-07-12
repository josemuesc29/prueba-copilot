package com.farmatodo.backend.task;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
//import com.google.appengine.repackaged.com.google.gson.JsonObject;
import com.google.gson.JsonObject;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.imaginamos.farmatodo.model.product.*;
import com.imaginamos.farmatodo.model.util.URLConnections;
import com.imaginamos.farmatodo.networking.services.ApiGatewayService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;

import static com.farmatodo.backend.OfyService.ofy;


/**
 * Created by Eric on 23/02/2017.
 */

public class CrossSalesUpload extends HttpServlet {
  private List<CrossSales> crossSalesToSave = new ArrayList<>();
  private Queue queue = QueueFactory.getQueue("productmanager");
  private static final Logger log = Logger.getLogger(CrossSalesUpload.class.getName());

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    final String METHOD = "[CrossSalesUpload.doGet]";
    log.info("[INI]-"+METHOD);

    JsonObject jsonObject = new JsonObject();

    List<ItemCross> itemList = ApiGatewayService.get().getCrossSales(0);
    if(Objects.nonNull(itemList) && !itemList.isEmpty()){
      saveCrossSales(0, itemList);
      response.setStatus(HttpServletResponse.SC_OK);
      jsonObject.addProperty("Message", URLConnections.SUCCESS_MESSAGE);
      jsonObject.addProperty("Code", HttpServletResponse.SC_OK);
      log.info("[FIN]-[case 200]");
    }else{
      log.info(METHOD+" Cross Sales is empty");
      jsonObject.addProperty("Message", URLConnections.DEFAULT);
      jsonObject.addProperty("Code", HttpServletResponse.SC_CONFLICT);
      response.setStatus(HttpServletResponse.SC_CONFLICT);
    }

    /*
    HttpURLConnection httpURLConnection = URLConnections.getConnection(URLConnections.GET, URLConnections.URL_CROSS_SALES + "0");
    log.info("Iniciando conexion a URL : ["+URLConnections.URL_CROSS_SALES+"]...");

    response.setContentType(URLConnections.CONTENT_TYPE_JSON);
    response.addHeader("Access-Control-Allow-Origin", "*");


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

        log.info("Mapeando respueta json del servicio de [responseJson] a [List<ItemCross> itemList]...");
        List<ItemCross> itemList = objectMapper.readValue(responseJson.toString(), objectMapper.getTypeFactory().constructCollectionType(List.class, ItemCross.class));

        log.info("FOR (ItemCross productInformation : itemList)");
        for (ItemCross productInformation : itemList) {
          if (productInformation.getCrossSales() != null && !productInformation.getCrossSales().isEmpty()) {
            this.crossSales(productInformation);
          }
        }

        log.info("IF (!this.crossSalesToSave.isEmpty()) : ["+(!this.crossSalesToSave.isEmpty())+"]");
        if (!this.crossSalesToSave.isEmpty()) {
          log.info("Guardando [crossSales] en Datastore...");
          ofy().save().entities(this.crossSalesToSave).now();
        }

        log.info("Se adiciona tarea a la cola [productmanager] con URL : [/crossSalesUpload]...");
        queue.add(ofy().getTransaction(), TaskOptions.Builder.withUrl("/crossSalesUpload").param("page", Integer.toString(1)));

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
    }*/
    PrintWriter out = response.getWriter();
    out.print(jsonObject);
    out.flush();
    out.close();
    log.info("[FIN]-"+METHOD);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      InvalidParameterException, ServletException {

    final String METHOD = "[CrossSalesUpload.doPost]";
    log.info("[INI]-"+METHOD);

    int page = Integer.parseInt(request.getParameter("page"));
    log.info("Procesando datos de la pagina : ["+page+"]");

    this.crossSalesToSave = new ArrayList<>();


    List<ItemCross> itemList = ApiGatewayService.get().getCrossSales(page);
    if(Objects.nonNull(itemList) && !itemList.isEmpty()){
      saveCrossSales(page, itemList);
    }else{
      log.info(METHOD+" Cross Sales is empty");
    }

    /*
    URL url = new URL(URLConnections.URL_CROSS_SALES + page);
    log.info("Iniciando conexion con URL : ["+URLConnections.URL_CROSS_SALES+page+"]...");

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
        StringBuilder responseJson = new StringBuilder();

        while ((inputLine = bufferedReader.readLine()) != null) {
          responseJson.append(inputLine);
        }
        bufferedReader.close();
        ObjectMapper objectMapper = new ObjectMapper();

        log.info("Mapenado respuesta del servicio de [responseJson] a [List<ItemCross> itemList]...");
        List<ItemCross> itemList = objectMapper.readValue(responseJson.toString(), objectMapper.getTypeFactory().constructCollectionType(List.class, ItemCross.class));

        log.info("FOR (ItemCross productInformation : itemList)");
        for (ItemCross productInformation : itemList) {
          if (productInformation.getCrossSales() != null && !productInformation.getCrossSales().isEmpty()) {
            this.crossSales(productInformation);
          }
        }

        log.info("IF (!this.crossSalesToSave.isEmpty()) : ["+(!this.crossSalesToSave.isEmpty())+"]");
        if (!this.crossSalesToSave.isEmpty()) {
          log.info("Guardando ["+this.crossSalesToSave.size()+"] crossSales en el Datastore...");
          ofy().save().entities(this.crossSalesToSave).now();
        }

        log.info("Adicionando tarea a la cola [productmanager] con URL : [/crossSalesUpload]. page: ["+Integer.toString(page + 1)+"].");
        queue.add(ofy().getTransaction(),TaskOptions.Builder.withUrl("/crossSalesUpload").param("page", Integer.toString(page + 1)));

        log.info("[FIN]-[case 200]");

        break;
      case 204:
        break;
      case 400:
        break;
      case 500:
        break;
      default:
        break;
    }*/
    log.info("[FIN]-"+METHOD);
  }

  private void saveCrossSales(int page, List<ItemCross> itemList) {
    log.info("FOR (ItemCross productInformation : itemList)");
    for (ItemCross productInformation : itemList) {
      if (productInformation.getCrossSales() != null && !productInformation.getCrossSales().isEmpty()) {
        this.crossSales(productInformation);
      }
    }
    log.info("IF (!this.crossSalesToSave.isEmpty()) : ["+(!this.crossSalesToSave.isEmpty())+"]");
    if (!this.crossSalesToSave.isEmpty()) {
      log.info("Guardando ["+this.crossSalesToSave.size()+"] crossSales en el Datastore...");
      ofy().save().entities(this.crossSalesToSave).now();
    }
    log.info("Adicionando tarea a la cola [productmanager] con URL : [/crossSalesUpload]. page: ["+Integer.toString(page + 1)+"].");
    queue.add(ofy().getTransaction(), TaskOptions.Builder.withUrl("/crossSalesUpload").param("page", Integer.toString(page + 1)));
  }

  private void crossSales(ItemCross item) {
    final String METHOD = "[CrossSalesUpload.crossSales]";
    log.info("[INI]-"+METHOD);

    Key<ItemGroup> itemGroupKey = Key.create(ItemGroup.class, "1");

    log.info("Consultando Item con Id : ["+item.getId()+"] en el Datastore...");
    Item item1 = ofy().load().key(Key.create(itemGroupKey, Item.class, Long.toString(item.getId()))).now();

    log.info("IF (ItemConsultado != null) : ["+(item1 != null)+"]");
    if (item1 != null) {
      log.info("Consultando crossSales en el Datastore para ELIMINAR...");
      List<CrossSales> crossSalesDelete = ofy().load().type(CrossSales.class).ancestor(item1).list();
      ofy().delete().entities(crossSalesDelete).now();
      log.info("crossSales eliminadas del Datastore.");

      log.info("Consultando crossSales en el Datastore para CREAR...");
      List<Suggested> crossSalesCreate = item.getCrossSales();

      log.info("IF (crossSalesCreate != null) : ["+(crossSalesCreate != null)+"]");
      if (crossSalesCreate != null) {
        CrossSales crossSales = new CrossSales();
        crossSales.setIdItem(UUID.randomUUID().toString());
        crossSales.setIdItemParent(Ref.create(Key.create(itemGroupKey, Item.class, item1.getItemId())));
        crossSales.setSuggested(item.getCrossSales());
        this.crossSalesToSave.add(crossSales);
      }
    }
    log.info("[FIN]-"+METHOD);
  }
}
