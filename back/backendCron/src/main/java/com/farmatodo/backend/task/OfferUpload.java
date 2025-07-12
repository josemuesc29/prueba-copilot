package com.farmatodo.backend.task;

//import com.google.appengine.repackaged.com.google.gson.JsonObject;
import com.google.gson.JsonObject;
import com.imaginamos.farmatodo.model.offer.Offer;
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
 * Created by Eric on 21/02/2017.
 */

public class OfferUpload extends HttpServlet {

  private static final Logger log = Logger.getLogger(OfferUpload.class.getName());

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, InvalidParameterException, ServletException {
    final String METHOD = "[OfferUpload.doPost]";
    log.info("[INI]-"+METHOD);

    JsonObject jsonObject = new JsonObject();
    List<Offer> listOffer = ApiGatewayService.get().getActiveOffer();
    if(Objects.nonNull(listOffer) && !listOffer.isEmpty()) {
      //Eliminar ofertas existentes...
      deleteExistingOffers();
      //Guardar las nuevas ofertas...
      saveNewOffers(listOffer);
      //Comprobar que no queden repetidas...
      List<String> duplicatedIDS = checkDuplicatedOffers();
      if (duplicatedIDS != null && !duplicatedIDS.isEmpty()) {
        deleteDuplicatedOffers(duplicatedIDS);
      }
      response.setStatus(HttpServletResponse.SC_OK);
      jsonObject.addProperty("Message", URLConnections.SUCCESS_MESSAGE);
      jsonObject.addProperty("Code", HttpServletResponse.SC_OK);
    }else{
      jsonObject.addProperty("Message", URLConnections.DEFAULT);
      jsonObject.addProperty("Code", HttpServletResponse.SC_CONFLICT);
      response.setStatus(HttpServletResponse.SC_CONFLICT);
    }

    /*

    URL url = new URL(URLConnections.URL_OFFER);
    log.info("Iniciando conexion a URL : ["+URLConnections.URL_OFFER+"]");

    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
    httpURLConnection.setRequestMethod(URLConnections.GET);
    httpURLConnection.setRequestProperty("Accept-Charset", "UTF-8");
    httpURLConnection.setConnectTimeout(1000000);
    httpURLConnection.setReadTimeout(1000000);

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

        List<Offer> offerList = objectMapper.readValue(responseJson.toString(),objectMapper.getTypeFactory().constructCollectionType(List.class, Offer.class));

        //Eliminar ofertas existentes...
        deleteExistingOffers();

        //Guardar las nuevas ofertas...
        saveNewOffers(offerList);

        //Comprobar que no queden repetidas...
        List<String> duplicatedIDS = checkDuplicatedOffers();

        if(duplicatedIDS!=null && !duplicatedIDS.isEmpty()){
          deleteDuplicatedOffers(duplicatedIDS);
        }

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

     */

    PrintWriter out = response.getWriter();
    out.print(jsonObject);
    out.flush();
    out.close();

    log.info("[FIN]-"+METHOD);
  }

  /**
   * Persistir las ofertas en el Datastore, entidad: Offer
   * @param offerList
   * */
  private void saveNewOffers(List<Offer> offerList) {
    if(offerList!=null && !offerList.isEmpty()){
      for (Offer offer : offerList) {
        if (offer.getItems() != null && !offer.getItems().isEmpty()) {
          offer.setOfferId(UUID.randomUUID().toString());
          ofy().save().entity(offer).now();
        }
      }
    }
  }

  /**
   * Comprobar que no se repitan las ofertas.
   * */
  private List<String> checkDuplicatedOffers(){
    List<Offer> offers = ofy().load().type(Offer.class).list();
    List<String> duplicatedIDS = new ArrayList<>();

    for (int j=0;j<offers.size();j++) {
      for (int k = j + 1; k < offers.size(); k++) {
        if (k != j && offers.get(k).getId() == offers.get(j).getId()) {
          duplicatedIDS.add(offers.get(k).getOfferId());
        }
      }
    }
    return duplicatedIDS;
  }

  /**
   * Eliminar las ofertas existentes.
   * */
  private void deleteExistingOffers(){
    List<Offer> offerListToDelete = ofy().load().type(Offer.class).list();
    if(offerListToDelete!=null && !offerListToDelete.isEmpty()){
      int sizeToDelete = offerListToDelete.size();
      int countDeleted = 0;
      for(Offer offer: offerListToDelete){
        deleteOfferByOfferID(offer.getOfferId());
        countDeleted++;
      }
      log.info("Se elimino ["+countDeleted+"/"+sizeToDelete+"] ofertas...");
    }
  }

  /**
   * Eliminar ofertas por id en un listado.
   * */
  private void deleteDuplicatedOffers(List<String> duplicatedOfferIds){
    for(String id:duplicatedOfferIds){
      deleteOfferByOfferID(id);
    }
  }

  /**
   * Eliminar uana oferta por ID.
   * */
  private void deleteOfferByOfferID(String offerId){
    ofy().delete().type(Offer.class).id(offerId).now();
  }

}
