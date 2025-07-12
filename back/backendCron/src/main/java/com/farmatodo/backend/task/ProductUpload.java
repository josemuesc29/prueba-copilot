package com.farmatodo.backend.task;

import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.ConflictException;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
//import com.google.appengine.repackaged.com.google.gson.JsonObject;
import com.google.gson.JsonObject;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.imaginamos.farmatodo.model.categories.Category;
import com.imaginamos.farmatodo.model.categories.Classification;
import com.imaginamos.farmatodo.model.categories.Filter;
import com.imaginamos.farmatodo.model.categories.FilterName;
import com.imaginamos.farmatodo.model.item.ItemReq;
import com.imaginamos.farmatodo.model.item.ItemRes;
import com.imaginamos.farmatodo.model.product.Item;
import com.imaginamos.farmatodo.model.product.ItemGroup;
import com.imaginamos.farmatodo.model.store.Store;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.model.util.URLConnections;
import com.imaginamos.farmatodo.model.util.VersionControl;
import com.imaginamos.farmatodo.networking.services.ApiGatewayService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import static com.farmatodo.backend.OfyService.ofy;

/**
 * Created by mileniopc on 12/6/16.
 * Property of Imaginamos.
 */

public class ProductUpload extends HttpServlet {
  private static final Logger log = Logger.getLogger(ProductUpload.class.getName());
  private Queue queue = QueueFactory.getQueue("productmanager");
  private Queue queue1 = QueueFactory.getQueue("crossmanager");
  private Queue queue2 = QueueFactory.getQueue("substitutesmanager");
  private Queue queue3 = QueueFactory.getQueue("offermanager");


  private List<Item> itemsToSave = new ArrayList<>();

  /**
   * Brings the Products of the database of "Farmatodo"
   *
   * @param request  Object of class "HttpServletRequest"
   * @param response Object of class "HttpServletResponse"
   * @throws IOException
   * @throws InvalidParameterException
   * @throws ServletException
   */
  @Deprecated
  @Override
  public synchronized void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, InvalidParameterException, ServletException {

    final String METHOD = "[ProductUpload.doGet]";
    log.info("[INI]-"+METHOD);

    Long token = null;
    JsonObject jsonObject = new JsonObject();
    response.setContentType(URLConnections.CONTENT_TYPE_JSON);
    response.addHeader("Access-Control-Allow-Origin", "*");

    try {
      token = this.itemStart();
    } catch (ConflictException | BadRequestException e) {
      log.warning(e.getMessage());
      jsonObject.addProperty("Message", URLConnections.SERVER_ERROR);
      jsonObject.addProperty("Code", 500);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    log.info("IF (token != null) : ["+(token != null)+"]");
    if (token != null) {
      // Backend 3
      List<Item> itemList = ApiGatewayService.get().postGetItems(new ItemReq(Constants.DIGITAL, token, 0));
      saveItemsList(response, token, jsonObject, itemList, 0);
    }else{
      jsonObject.addProperty("Message", URLConnections.DEFAULT);
      jsonObject.addProperty("Code", HttpServletResponse.SC_CONFLICT);
      response.setStatus(HttpServletResponse.SC_CONFLICT);
    }
    // Backend 3
/*




      String urlString = URLConnections.URL_PRODUCTS;

      log.info("Iniciando conexion a URL : ["+urlString+"]");

      HttpURLConnection httpURLConnection = URLConnections.getConnection(URLConnections.POST, urlString);
      OutputStreamWriter wr = new OutputStreamWriter(httpURLConnection.getOutputStream(), "UTF-8");
      JSONObject itemRequest = new JSONObject();
      itemRequest.put("source", Constants.DIGITAL);
      itemRequest.put("token", token);
      itemRequest.put("page", 0);

      log.info("request para la conexion : "+itemRequest.toJSONString());

      wr.write(itemRequest.toJSONString());
      wr.flush();

      int responseCode = httpURLConnection.getResponseCode();

      log.info("responseCode : ["+responseCode+"]");

      switch (responseCode) {
        case 200:
          log.info("[INI]-[case 200]");
          log.info("Se crea buffer para leer datos de respuesta...");
          BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8"));
          String inputLine;
          StringBuilder responseJson = new StringBuilder();

          log.info("Leyendo buffer...WHILE((inputLine = bufferedReader.readLine()) != null)");

          while ((inputLine = bufferedReader.readLine()) != null) {
            responseJson.append(inputLine);
          }

          log.info("Termino de lectura de buffer...");

          bufferedReader.close();
          ObjectMapper objectMapper = new ObjectMapper();

          log.info("responseJson obtenido : "+responseJson.toString());

          log.info("Convirtiendo [responseJson] a [List<Item>]...");
          List<Item> itemList = objectMapper.readValue(responseJson.toString(), objectMapper.getTypeFactory().constructCollectionType(List.class, Item.class));

          log.info("Recorriendo [List<Item> itemList] FOR (Item productInformation : itemList)..");
          for (Item productInformation : itemList) {
            saveItem(productInformation);
          }

          final List<Item> itemListFinal = this.itemsToSave;

          log.info("Guardando entities de la lista [this.itemsToSave] en el DataStore...");
          for(Item item:itemListFinal){
            ofy().save().entity(item).now();
          }
          //ofy().save().entities(itemListFinal).now();
          log.info("Finalizo guardado de entities de la lista [this.itemsToSave] en el DataStore...");

          log.info("Se adiciona transaccion [/productUpload] a la cola [productmanager]... page: 1, token: "+Long.toString(token));
          queue.add(ofy().getTransaction(),TaskOptions.Builder.withUrl("/productUpload").param("page", Integer.toString(1)).param("token", Long.toString(token)));

          response.setStatus(HttpServletResponse.SC_OK);
          jsonObject.addProperty("Message", URLConnections.SUCCESS_MESSAGE);
          jsonObject.addProperty("Code", responseCode);

          log.info("[FIN]-[case 200]");

          break;
        case 204:
          try {
            this.itemDone(token);
          } catch (BadRequestException e) {
            log.warning(e.getMessage());
            //e.printStackTrace();
          }
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
    }

 */
    PrintWriter out = response.getWriter();
    out.print(jsonObject);
    out.flush();
    out.close();
    log.info("[FIN]-"+METHOD);
  }

  private void saveItemsList(HttpServletResponse response, Long token, JsonObject jsonObject, List<Item> itemList, Integer page) {
    if (Objects.nonNull(itemList) && !itemList.isEmpty()) {

      log.info("[INI]-[case 200]");

      log.info("Recorriendo [List<Item> itemList] FOR (Item productInformation : itemList)..");
      for (Item productInformation : itemList) {
        saveItem(productInformation);
      }

      final List<Item> itemListFinal = this.itemsToSave;

      log.info("Guardando entities de la lista [this.itemsToSave] en el DataStore...");
      for (Item item : itemListFinal) {
        ofy().save().entity(item).now();
      }
      //ofy().save().entities(itemListFinal).now();
      log.info("Finalizo guardado de entities de la lista [this.itemsToSave] en el DataStore...");

      log.info("Se adiciona transaccion [/productUpload] a la cola [productmanager]... page: 1, token: " + Long.toString(token));
      queue.add(ofy().getTransaction(), TaskOptions.Builder.withUrl("/productUpload").param("page", Integer.toString(page)).param("token", Long.toString(token)));

      response.setStatus(HttpServletResponse.SC_OK);
      jsonObject.addProperty("Message", URLConnections.SUCCESS_MESSAGE);
      jsonObject.addProperty("Code", HttpServletResponse.SC_OK);
      log.info("[FIN]-[case 200]");
    }
  }

  private void saveItem(Item product) {
    final String METHOD = "[ProductUpload.saveItem]";

    log.info("[INI]-"+METHOD);

    log.info("Creando listas de [List<Integer> filters], [List<Long> subCategories]...");
    List<Integer> filters = new ArrayList<>();
    List<Long> subCategories = new ArrayList<>();

    log.info("IF (product.getStatus().equals(A)) : ["+(product.getStatus().equals("A"))+"]");
    if (product.getStatus().equals(Constants.ACTIVO) || product.getStatus().equals(Constants.BLOQUEADO)) {

      log.info("IF (product.getCategories() != null) : ["+(product.getCategories() != null)+"]");
      if (product.getCategories() != null) {
        for (Classification classification : product.getCategories()) {

          log.info("IF (classification.getFilters() != null) : ["+(classification.getFilters() != null)+"]");
          if (classification.getFilters() != null) {
            for (FilterName filterName : classification.getFilters()) {

              log.info("IF (filterName.getValues() != null) : ["+(filterName.getValues() != null)+"]");
              if (filterName.getValues() != null) {
                for (Filter filter : filterName.getValues()) {
                  filters.add((int) filter.getId());
                }
              }
            }
          }
          subCategories.add(classification.getId());
        }
        product.setSubCategories(subCategories);
        product.setFilterList(filters);
      }
      product.setToDelete(false);
    } else {

      log.info("IF (product.getStatus().equals(B) || product.getStatus().equals(E)) : ["+(product.getStatus().equals("B") || product.getStatus().equals("E"))+"]");
      //if (product.getStatus().equals("B") || product.getStatus().equals("E")) {
      if (product.getStatus().equals(Constants.ELIMINADO)) {
        product.setToDelete(true);
      }
    }

    log.info("Creando itemGroupKey para el producto con ID: ["+Long.toString(product.getId())+"]");
    Key<ItemGroup> itemGroupKey = Key.create(ItemGroup.class, "1");
    Key<Item> itemKey = Key.create(itemGroupKey, Item.class, Long.toString(product.getId()));

    log.info("Guardando itemGroup en el Datastore...");
    Item itemSaved = ofy().load().key(itemKey).now();

    log.info("IF (itemSavedInDataStore != null) : ["+(itemSaved != null)+"]");
    if (itemSaved != null) {
      product.setStoreInformation(itemSaved.getStoreInformation());
      log.info("IF (itemSaved.getStoreInformation() != null && !itemSaved.getStoreInformation().isEmpty()) : ["+(itemSaved.getStoreInformation() != null && !itemSaved.getStoreInformation().isEmpty())+"]");
      if (itemSaved.getStoreInformation() != null && !itemSaved.getStoreInformation().isEmpty()) {
        product.setFullPrice(itemSaved.getStoreInformation().get(0).getFullPrice());
      }
    }
    log.info("Item con ID ["+product.getId()+"] se indexara en Algolia...");
    product.setToIndexInAlgolia(true);
    product.setItemId(Long.toString(product.getId()));
    product.setItemGroupRef(Ref.create(Key.create(ItemGroup.class, Integer.toString(1))));
    product.setHighlight(product.isStarProduct());
    product.setCoupon(false);
    saveProduct(product);

    log.info("[FIN]-"+METHOD);
  }


  private synchronized void saveProduct(Item current) {
    final String METHOD = "[ProductUpload.saveProduct]";

    log.info("[INI]-"+METHOD);
    current.setToIndexInAlgolia(true);
    current.setMediaDescription(current.getFirstDescription());
    current.setGrayDescription(current.getSecondDescription());
    this.itemsToSave.add(current);
    log.info("Item agregado a la lista [this.itemsToSave]...");

    log.info("[FIN]-"+METHOD);
  }

  @Deprecated
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, InvalidParameterException, ServletException {

    final String METHOD = "[ProductUpload.doPost]";
    log.info("[INI]-"+METHOD);

    String pageS = request.getParameter("page");
    int page;

    log.info("IF (pageS == null) : ["+(pageS == null)+"]");
    if (pageS == null) {
      page = 197;
    }else {
      page = Integer.parseInt(pageS);
    }

    String tokenS = request.getParameter("token");
    long token;

    log.info("IF (tokenS == null) : ["+(tokenS == null)+"]");
    if (tokenS == null) {
      token = 1501570801118L;
    } else {
      token = Long.parseLong(tokenS);
    }
    log.info("Procesando pagina : ["+page+"] con token : ["+token+"]");

    List<Item> itemList = ApiGatewayService.get().postGetItems(new ItemReq(Constants.DIGITAL, token, page));
    if(Objects.nonNull(itemList) && !itemList.isEmpty()) {
      saveItemsList(response, token, new JsonObject(), itemList, page+1);
    }else{
      try {
        this.itemDone(token);
      } catch (BadRequestException e) {
        log.warning("ERROR - Lanzado al invocar el metodo: [this.itemDone(token)] clase: [ProductUpload.java]. Mensaje : "+e.getMessage());
        e.printStackTrace();
      }
      log.info("**************** INICIA ADICION DE TRANSACCIONES A LAS COLAS ****************");

      log.info("PASO [1/6] : Se adiciona transaccion [/crossSalesUpload] a la cola [crossmanager]");
      queue1.add(ofy().getTransaction(),TaskOptions.Builder.withUrl("/crossSalesUpload").param("page", Integer.toString(0)));

      log.info("PASO [2/6] : Se adiciona transaccion [/substitutesUpload] a la cola [substituteManager]");
      queue2.add(ofy().getTransaction(),TaskOptions.Builder.withUrl("/substitutesUpload").param("page", Integer.toString(0)));

      log.info("PASO [3/6] : Se adiciona transaccion [/offerUpload] a la cola [offerManager]");
      queue3.add(ofy().getTransaction(),TaskOptions.Builder.withUrl("/offerUpload").param("page", Integer.toString(0)));

      log.info("PASO [4/6] : Se adiciona transaccion [/crossSalesUpload] a la cola [offerManager]");
      queue3.add(ofy().getTransaction(),TaskOptions.Builder.withUrl("/highlightUpload").param("page", Integer.toString(0)));

      log.info("PASO [5/6] : Se adiciona transaccion [/mostSalesByCategory] a la cola [offerManager]");
      queue3.add(ofy().getTransaction(),TaskOptions.Builder.withUrl("/mostSalesByCategory"));

      log.info("PASO [6/6] : Se adiciona transaccion [/crossSalesUpload] a la cola [productManager]");
      queue.add(ofy().getTransaction(),TaskOptions.Builder.withUrl("/deleteItems"));

      log.info("**************** TERMINA ADICION DE TRANSACCIONES A LAS COLAS ****************");
    }


    /*
    String urlString = URLConnections.URL_PRODUCTS;
    log.info("Inicia conexion a URL ["+urlString+"]");

    HttpURLConnection httpURLConnection = URLConnections.getConnection(URLConnections.POST, urlString);
    OutputStreamWriter wr  = new OutputStreamWriter(httpURLConnection.getOutputStream(), "UTF-8");
    log.info("Conexion a ["+urlString+"] exitosa.");

    JSONObject itemRequest = new JSONObject();

    itemRequest.put("source", Constants.DIGITAL);
    itemRequest.put("token", token);
    itemRequest.put("page", page);

    log.info("Request: ["+itemRequest.toJSONString()+"]");

    wr.write(itemRequest.toJSONString());
    wr.flush();

    int responseCode = httpURLConnection.getResponseCode();

    log.info("responseCode : ["+responseCode+"]");

    switch (responseCode) {
      case 200:
        log.info("[INI]-[case 200]");
        log.info("Creando buffer para obtener respuesta...");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8"));
        String inputLine;
        StringBuilder responseJson = new StringBuilder();

        log.info("Leyendo buffer...");
        log.info("WHILE ((inputLine = bufferedReader.readLine()) != null)");
        while ((inputLine = bufferedReader.readLine()) != null) {
          responseJson.append(inputLine);
        }
        bufferedReader.close();

        log.info("Respuesta obtenida: [responseJson] = "+responseJson.toString());

        ObjectMapper objectMapper = new ObjectMapper();

        log.info("Mapeando [responseJson] a [List<Item> itemList]...");
        List<Item> itemList = objectMapper.readValue(responseJson.toString(), objectMapper.getTypeFactory().constructCollectionType(List.class, Item.class));

        log.info("FOR (Item productInformation : itemList)");
        for (Item productInformation : itemList) {
          saveItem(productInformation);
        }
        //ofy().save().entities(this.itemsToSave).now();
        saveItemsInDataStore();

        log.info("Se adiciona transaccion [/productUpload] a la cola [productmanager] => page: ["+Integer.toString(page + 1)+"]");
        queue.add(ofy().getTransaction(),TaskOptions.Builder.withUrl("/productUpload").param("page", Integer.toString(page + 1)).param("token", Long.toString(token)));

        log.info("[FIN]-[case 200]");
      case 204:
        try {
          this.itemDone(token);
        } catch (BadRequestException e) {
          log.warning("ERROR - Lanzado al invocar el metodo: [this.itemDone(token)] clase: [ProductUpload.java]. Mensaje : "+e.getMessage());
          e.printStackTrace();
        }
        log.info("**************** INICIA ADICION DE TRANSACCIONES A LAS COLAS ****************");

        log.info("PASO [1/6] : Se adiciona transaccion [/crossSalesUpload] a la cola [crossmanager]");
        queue1.add(ofy().getTransaction(),TaskOptions.Builder.withUrl("/crossSalesUpload").param("page", Integer.toString(0)));

        log.info("PASO [2/6] : Se adiciona transaccion [/substitutesUpload] a la cola [substituteManager]");
        queue2.add(ofy().getTransaction(),TaskOptions.Builder.withUrl("/substitutesUpload").param("page", Integer.toString(0)));

        log.info("PASO [3/6] : Se adiciona transaccion [/offerUpload] a la cola [offerManager]");
        queue3.add(ofy().getTransaction(),TaskOptions.Builder.withUrl("/offerUpload").param("page", Integer.toString(0)));

        log.info("PASO [4/6] : Se adiciona transaccion [/crossSalesUpload] a la cola [offerManager]");
        queue3.add(ofy().getTransaction(),TaskOptions.Builder.withUrl("/highlightUpload").param("page", Integer.toString(0)));

        log.info("PASO [5/6] : Se adiciona transaccion [/mostSalesByCategory] a la cola [offerManager]");
        queue3.add(ofy().getTransaction(),TaskOptions.Builder.withUrl("/mostSalesByCategory"));

        log.info("PASO [6/6] : Se adiciona transaccion [/crossSalesUpload] a la cola [productManager]");
        queue.add(ofy().getTransaction(),TaskOptions.Builder.withUrl("/deleteItems"));

        log.info("**************** TERMINA ADICION DE TRANSACCIONES A LAS COLAS ****************");

        break;
      case 400:
        log.warning("400");
        break;
      case 500:
        log.warning("500");
        break;
      default:
        log.warning("ERROR");
        break;
    }
    log.info("[FIN]-"+METHOD);
  }

  private synchronized void saveItemsInDataStore(){
    final String METHOD = "[ProductUpload.saveItemsInDataStore]";
    log.info("[INI]-"+METHOD);

    //log.info("Se van a guardar ["+this.itemsToSave.size()+"] items en el DataStore...");
    if(this.itemsToSave!=null && !this.itemsToSave.isEmpty()){
      List<Item> itemsToSaveDatastore = this.itemsToSave;
      for(Item item : itemsToSaveDatastore){
        ofy().save().entity(item).now();
      }
    }
    //ofy().save().entities(this.itemsToSave).now();
    //log.info("["+this.itemsToSave.size()+"] items guardados en el DataStore...");
     */
    log.info("[FIN]-"+METHOD);
  }

  @SuppressWarnings("ALL")
  private long itemStart() throws IOException, ConflictException, BadRequestException {
    final String METHOD = "[ProductUpload.itemStart]";

    log.info("[INI]-"+METHOD);

    /*
    String urlString = URLConnections.URL_START_PRODUCTS;
    log.info("Iniciando conexion a URL : ["+urlString+"]");
    HttpURLConnection httpURLConnection = URLConnections.getConnection(URLConnections.POST, urlString);
    OutputStreamWriter wr = new OutputStreamWriter(httpURLConnection.getOutputStream(), "UTF-8");
    JSONObject itemRequest = new JSONObject();
     */

    log.info("Consultando el numero de control de version en el Datastore configurado para [Item]...");
    VersionControl versionControl = ofy().load().type(VersionControl.class).filter("tableControl", "Item").first().now();

    log.info("IF (versionControl == null) : ["+(versionControl == null)+"]");
    if (versionControl == null) {
      throw new ConflictException(Constants.NO_VERSION);
    }

    ItemRes response = ApiGatewayService.get().postItemStart(new ItemReq(Constants.DIGITAL, versionControl.getControlNumber()));
    if(Objects.nonNull(response) && Objects.nonNull(response.getToken())){
      log.info("token a retornar : ["+response.getToken()+"]");
      log.info("FIN-"+METHOD);
      return response.getToken();
    }
    throw new BadRequestException(Constants.DEFAULT_MESSAGE);

    /*

    itemRequest.put("source", Constants.DIGITAL);
    itemRequest.put("revision", versionControl.getControlNumber());
    wr.write(itemRequest.toJSONString());
    wr.flush();

    int responseCode = httpURLConnection.getResponseCode();

    log.info("ResponseCode : ["+responseCode+"]");

    long token;
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

        log.info("Mapeando responseJson de [Json] a [VersionControl version]...");
        VersionControl version = objectMapper.readValue(responseJson.toString(), VersionControl.class);
        token = version.getToken();

        log.info("[FIN]-[case 200]");

        break;
      default:
        throw new BadRequestException(Constants.DEFAULT_MESSAGE);
    }

    log.info("Token obtenido : ["+token+"]");
    log.info("[FIN]-"+METHOD);
    return token;
     */
  }

  //@SuppressWarnings("ALL")
  private void itemDone(long token) throws IOException, BadRequestException {
    final String METHOD = "[ProductUpload.itemDone]";
    log.info("[INI]-"+METHOD);

    ItemRes response = ApiGatewayService.get().postItemStartDone(new ItemReq(Constants.DIGITAL, token));

    if(Objects.nonNull(response) && Objects.nonNull(response.getRevision())) {
      log.info("Consultando entidad [VersionControl] del DataStore donde el campo [tableControl] = [Item]...");
      VersionControl versionControl = ofy().load().type(VersionControl.class).filter("tableControl", "Item").first().now();
      versionControl.setControlNumber(response.getRevision());
      log.info("Guardando entidad [VersionControl] en el DataStore con el nuevo numero de control de version...");
      ofy().save().entity(versionControl).now();
      log.info("Entidad [VersionControl] guardada en el DataStore con el nuevo numero de control de version exitosamente...");
    }
    throw new BadRequestException(Constants.DEFAULT_MESSAGE);

    /*
    String urlString = URLConnections.URL_FINAL_PRODUCTS;
    log.info("Inicia conexion a URL : ["+urlString+"]");
    HttpURLConnection httpURLConnection = URLConnections.getConnection(URLConnections.POST, urlString);
    OutputStreamWriter wr = new OutputStreamWriter(httpURLConnection.getOutputStream(), "UTF-8");
    JSONObject itemRequest = new JSONObject();
    itemRequest.put("source", Constants.DIGITAL);
    itemRequest.put("token", token);
    wr.write(itemRequest.toJSONString());
    wr.flush();
    int responseCode = httpURLConnection.getResponseCode();
    log.info("responseCode : ["+responseCode+"]");
    switch (responseCode) {
      case 200:
        log.info("[INI]-[case 200]");
        log.info("Consultando entidad [VersionControl] del DataStore donde el campo [tableControl] = [Item]...");
        VersionControl versionControl = ofy().load().type(VersionControl.class).filter("tableControl", "Item").first().now();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8"));
        String inputLine;
        StringBuilder responseJson = new StringBuilder();

        log.info("Leyendo buffer de lo devuelto por el servicio...");
        while ((inputLine = bufferedReader.readLine()) != null) {
          responseJson.append(inputLine);
        }
        bufferedReader.close();

        log.info("responseJson.toString : "+responseJson.toString());

        ObjectMapper objectMapper = new ObjectMapper();

        log.info("Convirtiendo [responseJson] a una entidad de tipo: [VersionControl]");
        VersionControl version = objectMapper.readValue(responseJson.toString(), VersionControl.class);

        log.info("Actualizando ControlNumber a la entidad [VersionControl]. Nuevo controlNumber : ["+version.getRevision()+"]");
        versionControl.setControlNumber(version.getRevision());

        log.info("Guardando entidad [VersionControl] en el DataStore con el nuevo numero de control de version...");
        ofy().save().entity(versionControl).now();
        log.info("Entidad [VersionControl] guardada en el DataStore con el nuevo numero de control de version exitosamente...");

        log.info("[INI]-[case 200]");

        break;

      default:
        throw new BadRequestException(Constants.DEFAULT_MESSAGE);
    }
    log.info("[FIN]"+METHOD);
     */
  }

  private void updateCategoriesOfItemInAlgolia(final List<Item> itemsToUpdateAlgolia){
    final String METHOD = "[ProductUpload.updateCategoriesOfItemInAlgolia]";
    log.info("[INI]-"+METHOD);

    List<com.imaginamos.farmatodo.model.algolia.Item> listItemForUpdatingInAlgolia = new ArrayList<>();

    log.info("IF(itemsToUpdateAlgolia!=null && !itemsToUpdateAlgolia.isEmpty()) : ["+(itemsToUpdateAlgolia!=null && !itemsToUpdateAlgolia.isEmpty())+"]");
    if(itemsToUpdateAlgolia!=null && !itemsToUpdateAlgolia.isEmpty()){
      // Setear tienda y clasificacion...
      log.info("FOR(Item item : itemsToUpdateAlgolia) : ["+itemsToUpdateAlgolia.size()+"]");
      for(Item item : itemsToUpdateAlgolia){
        com.imaginamos.farmatodo.model.algolia.Item itemToUpdateInAlgolia = getItemWithClassification(item);

        log.info("if(itemToUpdateInAlgolia!=null) : ["+(itemToUpdateInAlgolia!=null)+"]");
        if(itemToUpdateInAlgolia!=null){
          // Construir objectID...
          log.info("Consultando tiendas en datastore para construir objectID...");
          List<Store> stores = ofy().load().type(Store.class).filter("status", 1).list();
          log.info("IF(stores!=null && !stores.isEmpty()) : ["+(stores!=null && !stores.isEmpty())+"]");
          if(stores!=null && !stores.isEmpty()) {
            log.info("FOR (Store store : stores) : ["+stores.size()+"]");
            for (Store store : stores) {
              String idStore = store.getIdStore();
              log.info("IF(idStore != null) : ["+(idStore != null)+"]");
              if (idStore != null) {
                log.info("objectID.construido : ["+itemToUpdateInAlgolia.getItemID() + "" + idStore+"]");
                itemToUpdateInAlgolia.setObjectID(itemToUpdateInAlgolia.getItemID() + "" + idStore);
                listItemForUpdatingInAlgolia.add(itemToUpdateInAlgolia);
              }
            }
          }
        }
      }

      log.info("IF(!listItemForUpdatingInAlgolia.isEmpty()) : ["+(!listItemForUpdatingInAlgolia.isEmpty())+"]");
      if (!listItemForUpdatingInAlgolia.isEmpty()) {
        updateItemsInAlgolia(listItemForUpdatingInAlgolia);
      }

    }
    log.info("[FIN]-"+METHOD);
  }

  private com.imaginamos.farmatodo.model.algolia.Item getItemWithClassification(final Item item){
    final String METHOD = "[ProductUpload.getItemWithClassification]";
    log.info("[INI]-"+METHOD);

    com.imaginamos.farmatodo.model.algolia.Item itemWithClassification = null;
    log.info("IF(item!=null) : ["+(item!=null)+"]");
    if(item!=null) {
      List<Long> idCategories = item.getSubCategories();
      log.info("IF (idCategories != null && !idCategories.isEmpty()) : ["+(idCategories != null && !idCategories.isEmpty())+"]");
      if (idCategories != null && !idCategories.isEmpty()) {
        Long firstCategorie = idCategories.get(0);
        log.info("IF(firstCategorie != null && firstCategorie > 0) : ["+(firstCategorie != null && firstCategorie > 0)+"]");
        if (firstCategorie != null && firstCategorie > 0) {
          log.info("Consultando la categoria para con id : ["+firstCategorie+"]");
          Category category = ofy().load().type(Category.class).filter("id", firstCategorie).first().now();
          log.info("IF(category!=null) : ["+(category!=null)+"]");
          if(category!=null){
            String categorieName = category.getName();
            log.info("IF(categorieName!=null) : ["+(categorieName!=null)+"]");
            if(categorieName!=null){
              itemWithClassification = new com.imaginamos.farmatodo.model.algolia.Item();
              itemWithClassification.setClassification(categorieName);
              itemWithClassification.setItemID(String.valueOf(item.getId()));
            }
          }
        }
      }
    }
    return itemWithClassification;
  }

  private void updateItemsInAlgolia(final List<com.imaginamos.farmatodo.model.algolia.Item> listItemForUpdatingInAlgolia){
    final String METHOD = "[ProductUpload.updateItemsInAlgolia]";
    log.info("[INI]-"+METHOD);

    //Actualizar en Algolia...
    log.info("IF (listItemForUpdatingInAlgolia!=null && !listItemForUpdatingInAlgolia.isEmpty()) : ["+(listItemForUpdatingInAlgolia!=null && !listItemForUpdatingInAlgolia.isEmpty())+"]");
    if (listItemForUpdatingInAlgolia!=null && !listItemForUpdatingInAlgolia.isEmpty()) {
      List<Object> objectsForAlgolia = new ArrayList<Object>(listItemForUpdatingInAlgolia);
      log.info("IF (objectsForAlgolia!=null) : ["+(objectsForAlgolia!=null)+"]");
      if(objectsForAlgolia!=null){
        /**
        try {
          // Obtener conexion y el indice de Algolia.
          log.info("Obteniendo conexion con Algolia...");
          APIClient client = new AppEngineAPIClientBuilder(Constants.APP_ID,Constants.API_KEY).build();
          Index<com.imaginamos.farmatodo.model.algolia.Item> index = client.initIndex(Constants.INDEX,com.imaginamos.farmatodo.model.algolia.Item.class);
          log.info("Index obtenido? => ["+(index!=null)+"]");
          if(index!=null){
            log.info("Actualizando ["+objectsForAlgolia.size()+"] items en Algolia...");
            index.partialUpdateObjects(objectsForAlgolia, false);
            log.info("Actualizados ["+objectsForAlgolia.size()+"] items en Algolia...");
          }
        }catch (AlgoliaException e){
          log.warning("Error actualizando la clasificacion de los items en Algolia. Mensaje:"+e.getMessage());
        }
        */
      }
    }
    log.info("[FIN]-"+METHOD);
  }

}
