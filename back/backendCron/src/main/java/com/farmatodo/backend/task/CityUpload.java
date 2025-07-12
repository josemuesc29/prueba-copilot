package com.farmatodo.backend.task;

import com.farmatodo.backend.OfyService;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.gson.JsonObject;
import com.imaginamos.farmatodo.model.city.CityJSON;
import com.imaginamos.farmatodo.model.location.City;
import com.imaginamos.farmatodo.model.util.DeliveryType;
import com.imaginamos.farmatodo.model.util.URLConnections;
import com.imaginamos.farmatodo.networking.services.ApiGatewayService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.farmatodo.backend.OfyService.ofy;


/**
 * Created by mileniopc on 11/24/16.
 * Property of Imaginamos.
 */

public class CityUpload extends HttpServlet {
  private Queue queue = QueueFactory.getQueue("cronqueue");
  private static final Logger log = Logger.getLogger(CityUpload.class.getName());

  /**
   * Brings the cities of the database of "Farmatodo"
   *
   * @param request  Object of class "HttpServletRequest"
   * @param response Object of class "HttpServletResponse"
   * @throws IOException
   * @throws InvalidParameterException
   * @throws ServletException
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, InvalidParameterException, ServletException {
    final String METHOD = "[CityUpload.doGet]";
    log.info("[INI]-"+METHOD);

    List<CityJSON> responseCityList = ApiGatewayService.get().getCityActive();
    JsonObject jsonObject = new JsonObject();

    if(Objects.nonNull(responseCityList) && !responseCityList.isEmpty()){
      List<City> cityList = responseCityList.parallelStream()
              .filter(city -> Objects.nonNull(city))
              .map(city -> new City(city.getId(), city.getName(), city.getGeoCityCode(), (Objects.nonNull(city.getStatus()) ? city.getStatus().longValue() : 0L), city.getCountry(),
                      city.getPhone(), DeliveryType.getDeliveryType(city.getDeliveryType()))).collect(Collectors.toList());
      this.saveCities(cityList);
      jsonObject.addProperty("Message", URLConnections.SUCCESS_MESSAGE);
      jsonObject.addProperty("Code", HttpServletResponse.SC_OK);
      response.setStatus(HttpServletResponse.SC_OK);
      log.info("Adicionando tarea a la cola [cronqueue] con url [/storeUpload]...");
      queue.add(ofy().getTransaction(),TaskOptions.Builder.withUrl("/storeUpload"));
      log.info("[FIN]-[case 200]");
    }else{
        jsonObject.addProperty("Message", URLConnections.DEFAULT);
        jsonObject.addProperty("Code", HttpServletResponse.SC_CONFLICT);
        response.setStatus(HttpServletResponse.SC_CONFLICT);
    }

    PrintWriter out = response.getWriter();
    out.print(jsonObject);
    out.flush();
    out.close();


    /*
    log.info("Inicia conexion a URL : ["+URLConnections.URL_CITIES+"]");
    URL url = new URL(URLConnections.URL_CITIES);
    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
    httpURLConnection.setRequestMethod(URLConnections.GET);

    response.setContentType(URLConnections.CONTENT_TYPE_JSON);
    response.addHeader("Access-Control-Allow-Origin", "*");


    int responseCode = httpURLConnection.getResponseCode();

    log.info("ResponseCode : ["+responseCode+"]");

    switch (responseCode) {
      case 200:
        log.info("[INI]-[case 200]");
        BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8"));
        String inputLine;
        StringBuilder responseJson = new StringBuilder();

        while ((inputLine = reader.readLine()) != null) {
          responseJson.append(inputLine);
        }
        reader.close();

        ObjectMapper objectMapper = new ObjectMapper();

        log.info("Mapeando respuesta del servicio de [responseJson] a [List<City> cityList]...");
        List<City> cityList = objectMapper.readValue(responseJson.toString(), objectMapper.getTypeFactory().constructCollectionType(List.class, City.class));
        this.saveCities(cityList);
        jsonObject.addProperty("Message", URLConnections.SUCCESS_MESSAGE);
        jsonObject.addProperty("Code", responseCode);
        response.setStatus(HttpServletResponse.SC_OK);

        log.info("Adicionando tarea a la cola [cronqueue] con url [/storeUpload]...");
        queue.add(ofy().getTransaction(),TaskOptions.Builder.withUrl("/storeUpload"));

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
    */

    log.info("[FIN]-"+METHOD);
  }

  /**
   * It updates the data of the database "farmatodo" in datastore,
   * in the case of not finding synchrony of some city, creates a new record.
   *
   * @param cityList Array of Object of class "City"
   */
  private void saveCities(List<City> cityList) {
    final String METHOD = "[CityUpload.saveCities]";
    log.info("[INI]-"+METHOD);

    log.info("FOR (City city : cityList)");
    log.info("Actualizando ciudades en el Datastore...Entidad: [City]");
    for (City city : cityList) {
      City city1 = OfyService.ofy().load().type(City.class).filter("id", city.getId()).first().now();
      if (city1 != null) {
        city1.setName(city.getName());
        city1.setStatus(city.getStatus());
        city1.setGeoCityCode(city.getGeoCityCode());
        //city1.setDefaultStore(city.getDefaultStore());
        if (city.getStore() != null) {
          city1.setDefaultStore(city.getStore().getId());
          city1.setLatitude(city.getStore().getLatitude());
          city1.setLongitude(city.getStore().getLongitude());
        }
        city1.setCountry(city.getCountry());
        city1.setPhone(city.getPhone());
        city1.setDeliveryType(city.getDeliveryType());
      } else {
        city1 = new City();
        city1.setIdCity(UUID.randomUUID().toString());
        city1.setName(city.getName());
        city1.setId(city.getId());
        city1.setStatus(city.getStatus());
        city1.setGeoCityCode(city.getGeoCityCode());
        //city1.setDefaultStore(city.getDefaultStore());
        if (city.getStore() != null) {
          city1.setDefaultStore(city.getStore().getId());
          city1.setLatitude(city.getStore().getLatitude());
          city1.setLongitude(city.getStore().getLongitude());
        }
        city1.setCountry(city.getCountry());
        city1.setPhone(city.getPhone());
        city1.setDeliveryType(city.getDeliveryType());


      }
      OfyService.ofy().save().entity(city1).now();
    }
    log.info("[FIN]-"+METHOD);
  }
}
