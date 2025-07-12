package com.farmatodo.backend.algolia;

//import com.google.appengine.repackaged.com.google.gson.Gson;

import com.google.gson.Gson;
import com.googlecode.objectify.Key;
import com.imaginamos.farmatodo.model.product.Item;
import com.imaginamos.farmatodo.model.product.Products;
import com.imaginamos.farmatodo.model.util.Constants;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


import static com.farmatodo.backend.OfyService.ofy;


/**
 * Created by RentAdvisor on 2/9/17.
 */

public class AlgoliaCall extends HttpServlet {

  private static final Logger log = Logger.getLogger(AlgoliaCall.class.getName());


  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, InvalidParameterException, ServletException {
    final String METHOD = "[AlgoliaCall.doPost]";

    log.info("[INI]-" + METHOD);

    String items = request.getParameter("webSafe");
    String type = request.getParameter("type");
    Products products = new Products();
    products.setKeyClient(Constants.KEY_SECURE_CLIENT);
    String[] ids = items.split(",");
    log.info("Cantidad de items a procesar : ["+String.valueOf(ids.length)+"]. Accion a ejecutar : ["+type+"]");
    log.info(ids[0]);
    log.info(ids[1]);

    log.info("SWITCH("+type+")...");

    switch (type) {
      case "DELETE":
        log.info("[INI]-[Eliminacion de Items]");
        Long[] itemsList = new Long[ids.length - 1];

        log.info("FOR (int i=1; i<ids.length; i++)");
        for (int i = 1; i < ids.length; i++) {
          itemsList[i - 1] = Long.parseLong(ids[i]);
        }
        products.setItems(itemsList);
        deleteProducts(jsonConversion(products));
        log.info("[FIN]-[Eliminacion de Items]");
        break;
      case "UPDATE":
        log.info("[INI]-[Actualizacion de Items]");
        List<Item> itemList = new ArrayList<>();

        log.info("FOR (String id : ids)");
        for (String id : ids) {
          if (id.length() != 0) {
            Key<Item> itemKey = Key.create(id);
            Item item = ofy().load().key(itemKey).now();

            item.setIdItemWebSafe(itemKey.toWebSafeString());
            itemList.add(item);
          }
        }
        products.setItemList(itemList);
        uploadProduct(this.jsonConversion(products));
        log.info("[FIN]-[Actualizacion de Items]");
        break;
      default:
        break;
    }
    log.info("[FIN]-"+METHOD);
  }

  private String jsonConversion(Object object) {
    Gson gson = new Gson();
    String jsonResulted = gson.toJson(object);
    return jsonResulted;
  }

  public void uploadProduct(String jsonString) throws IOException {
    final String METHOD = "[AlgoliaCall.uploadProduct]";
    log.info("[INI]-"+METHOD);

    log.info("jsonString with products to UPLOAD in Algolia : "+jsonString);

    String urlString = "https://algoliasync-dot-stunning-base-164402.appspot.com/updateProducts";
    log.info("Inicia conexion a URL : ["+urlString+"]");

    URL url = new URL(urlString);
    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
    httpURLConnection.setRequestMethod("POST");
    httpURLConnection.setRequestProperty("Content-Type", "application/json");
    httpURLConnection.setRequestProperty("Accept", "application/json");
    httpURLConnection.setDoInput(true);
    httpURLConnection.setDoOutput(true);
    httpURLConnection.setReadTimeout(100000);
    httpURLConnection.setConnectTimeout(100000);
    DataOutputStream outputStream = new DataOutputStream(httpURLConnection.getOutputStream());
    outputStream.writeBytes(jsonString);
    outputStream.flush();
    outputStream.close();

    int responseCode = httpURLConnection.getResponseCode();

    log.info("Response.code.Algolia : ["+responseCode+"]");
    log.info("[FIN]-"+METHOD);
  }

  public void deleteProducts(String jsonString) throws IOException {
    final String METHOD = "[AlgoliaCall.deleteProducts]";
    log.info("[INI]-"+METHOD);
    log.info("Parametro de entrada: productos a eliminar => "+jsonString);

    final String urlString = "https://algoliasync-dot-stunning-base-164402.appspot.com/deleteProducts";
    log.info("Inicia conexion a URL : ["+urlString+"]");

    URL url = new URL(urlString);
    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
    httpURLConnection.setRequestMethod("POST");
    httpURLConnection.setRequestProperty("Content-Type", "application/json");
    httpURLConnection.setRequestProperty("Accept", "application/json");
    httpURLConnection.setDoInput(true);
    httpURLConnection.setDoOutput(true);
    httpURLConnection.setReadTimeout(100000);
    httpURLConnection.setConnectTimeout(100000);
    DataOutputStream outputStream = new DataOutputStream(httpURLConnection.getOutputStream());
    outputStream.writeBytes(jsonString);
    outputStream.flush();
    outputStream.close();

    int responseCode = httpURLConnection.getResponseCode();

    log.info("Response.code.Algolia: [" + responseCode+"]");
    log.info("[FIN]-"+METHOD);
  }
}
