package com.farmatodo.backend.task;

/**
 * Created by eric on 11/05/17.
 */

import com.fasterxml.jackson.databind.ObjectMapper;
//import com.google.appengine.repackaged.com.google.gson.JsonObject;
import com.google.gson.JsonObject;
import com.imaginamos.farmatodo.model.payment.PaymentType;
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
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static com.farmatodo.backend.OfyService.ofy;


/**
 * Created by Eric on 15/03/2017.
 */

public class PaymentMethodUpload extends HttpServlet {
  private static final Logger log = Logger.getLogger(Store.class.getName());

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, InvalidParameterException, ServletException {
    final String METHOD = "[PaymentMethodUpload.doGet]";

    log.info("[INI]-"+METHOD);

    log.info("Inicianado conexion a URL : ["+URLConnections.URL_PAYMENT_METHODS_ACTIVE+"]");

    URL url = new URL(URLConnections.URL_PAYMENT_METHODS_ACTIVE);
    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
    httpURLConnection.setRequestMethod("GET");
    httpURLConnection.setRequestProperty("Accept-Charset", "UTF-8");
    httpURLConnection.setConnectTimeout(150000);
    httpURLConnection.setReadTimeout(150000);
    response.setContentType("application/json");
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
        log.info("Respuesta del servicio: responseJson => "+responseJson.toString());
        log.info("Mapeando respuesta del servicio de [responseJson] a [List<PaymentType> itemList]");
        List<PaymentType> itemList = objectMapper.readValue(responseJson.toString(), objectMapper.getTypeFactory().constructCollectionType(List.class, PaymentType.class));

        this.savePaymentTypes(itemList);

        response.setStatus(HttpServletResponse.SC_OK);
        jsonObject.addProperty("Message", "");
        jsonObject.addProperty("Code", responseCode);

        log.info("[FIN]-[case 200]");
        break;
      case 204:
        jsonObject.addProperty("Message", "");
        jsonObject.addProperty("Code", responseCode);
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        break;
      case 400:
        jsonObject.addProperty("Message", "");
        jsonObject.addProperty("Code", responseCode);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        break;
      case 500:
        jsonObject.addProperty("Message", "");
        jsonObject.addProperty("Code", responseCode);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        break;
      default:
        jsonObject.addProperty("Message", "");
        jsonObject.addProperty("Code", responseCode);
        response.setStatus(HttpServletResponse.SC_CONFLICT);
        break;
    }
    PrintWriter out = response.getWriter();
    out.print(jsonObject);
    out.flush();
    out.close();

    log.info("[FIN]-"+METHOD);
  }

  private void savePaymentTypes(List<PaymentType> paymentTypeList) {
    final String METHOD = "[PaymentMethodUpload.savePaymentTypes]";
    log.info("[INI]-"+METHOD);

    List<PaymentType> paymentTypes = new ArrayList<>();

    log.info("FOR (PaymentType paymentType : paymentTypeList)");
    for (PaymentType paymentType : paymentTypeList) {

      log.info("Consultando los tipos de metodos de pago en el Datastore con id : ["+paymentType.getId()+"]");
      PaymentType paymentType1 = ofy().load().type(PaymentType.class).filter("id", paymentType.getId()).first().now();

      log.info("IF (paymentType1 == null) : ["+(paymentType1 == null)+"]");
      if (paymentType1 == null) {
        paymentType1 = new PaymentType();
        paymentType1.setIdPaymentType(UUID.randomUUID().toString());
      }

      log.info("Adicionando el metodo de pago a lista de metodos de pago...");
      paymentType1.setId(paymentType.getId());
      paymentType1.setDescription(paymentType.getDescription());
      paymentType1.setPositionIndex(paymentType.getPositionIndex());
      paymentType1.setStatus(paymentType.getStatus());
      paymentType1.setShowPayments(paymentType.getShowPayments());
      paymentTypes.add(paymentType1);
    }

    log.info("Guardando los metodos de pago en el Datastore...");
    ofy().save().entities(paymentTypes);

    log.info("[FIN]-"+METHOD);
  }
}
