package com.farmatodo.backend.task;

//import com.google.appengine.repackaged.com.google.gson.JsonObject;
import com.google.gson.JsonObject;
import com.imaginamos.farmatodo.model.product.Highlight;
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
 * Created by eric on 8/05/17.
 */

public class HighlightProductUpload extends HttpServlet {

  private static final Logger log = Logger.getLogger(HighlightProductUpload.class.getName());
  private ArrayList<Highlight> highlights = new ArrayList<>();

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, InvalidParameterException, ServletException {
    final String METHOD = "[HighlightProductUpload.doPost]";
    log.info("[INI]-"+METHOD);

    JsonObject jsonObject = new JsonObject();
    List<Highlight> highlightItemList = ApiGatewayService.get().getActiveHighLight();
    if(Objects.nonNull(highlightItemList) && !highlightItemList.isEmpty()){
      this.saveHighlightItem(highlightItemList);
      response.setStatus(HttpServletResponse.SC_OK);
      jsonObject.addProperty("Message", URLConnections.SUCCESS_MESSAGE);
      jsonObject.addProperty("Code", HttpServletResponse.SC_OK);
    }else{
      jsonObject.addProperty("Message", URLConnections.DEFAULT);
      jsonObject.addProperty("Code", HttpServletResponse.SC_CONFLICT);
      response.setStatus(HttpServletResponse.SC_CONFLICT);
    }

  /*
    log.info("Iniciando conexion a URLConnections.URL_HIGHLIGHT : ["+URLConnections.URL_HIGHLIGHT+"]...");
    URL url = new URL(URLConnections.URL_HIGHLIGHT);
    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
    httpURLConnection.setRequestMethod(URLConnections.GET);
    httpURLConnection.setRequestProperty("Accept-Charset", "UTF-8");
    httpURLConnection.setConnectTimeout(1000000);
    httpURLConnection.setReadTimeout(1000000);

    response.setContentType(URLConnections.CONTENT_TYPE_JSON);
    response.addHeader("Access-Control-Allow-Origin", "*");

    int responseCode = httpURLConnection.getResponseCode();

    log.info("ResponseCode : ["+responseCode+"]");

    switch (responseCode) {
      case 200:
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8"));
        String inputLine;
        StringBuilder responseJson = new StringBuilder();

        while ((inputLine = bufferedReader.readLine()) != null) {
          responseJson.append(inputLine);
        }
        bufferedReader.close();

        ObjectMapper objectMapper = new ObjectMapper();
        log.info("Mapeando destacados de [responseJson] a List<Highlight>....");
        List<Highlight> highlightItemList = objectMapper.readValue(responseJson.toString(), objectMapper.getTypeFactory().constructCollectionType(List.class, Highlight.class));

        this.saveHighlightItem(highlightItemList);

        response.setStatus(HttpServletResponse.SC_OK);
        jsonObject.addProperty("Message", URLConnections.SUCCESS_MESSAGE);
        jsonObject.addProperty("Code", responseCode);
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

  private void saveHighlightItem(List<Highlight> highlightList) {
    final String METHOD = "[HighlightProductUpload.saveHighlightItem]";
    log.info("[INI]-"+METHOD);

    List<Highlight> highlightListDelete = ofy().load().type(Highlight.class).list();
    log.info("Cantidad de destacados a eliminar: [" + highlightListDelete.size()+"]");

    for(Highlight h:highlightListDelete){
      ofy().delete().type(Highlight.class).id(h.getHighlightId()).now();
    }
   // ofy().delete().entities(highlightListDelete).now();
    log.info("Destacados eliminados del Datastore.");

    for (Highlight highlight : highlightList) {
      if (highlight.getItems() != null && !highlight.getItems().isEmpty()) {
        highlight.setHighlightId(UUID.randomUUID().toString());
        //this.highlights.add(highlight);
        ofy().save().entity(highlight).now();
      }
    }
    //log.info("Creando [" + this.highlights.size()+"] destacados en el Datastore...");
    //ofy().save().entities(this.highlights).now();

    log.info("[FIN]-"+METHOD);
  }
}
