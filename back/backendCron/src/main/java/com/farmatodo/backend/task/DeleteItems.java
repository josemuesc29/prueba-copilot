package com.farmatodo.backend.task;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.imaginamos.farmatodo.model.location.StoreList;
import com.imaginamos.farmatodo.model.product.Item;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.logging.Logger;

import static com.farmatodo.backend.OfyService.ofy;

/**
 * Created by Admin on 25/05/2017.
 */

public class DeleteItems extends HttpServlet {
  private Queue queue = QueueFactory.getQueue("productmanager");
  private Queue queue1 = QueueFactory.getQueue("algolia");
  private static final Logger log = Logger.getLogger(DeleteItems.class.getName());


  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, InvalidParameterException, ServletException {
    final String METHOD = "[DeleteItems.doGet]";
    log.info("[INI]-"+METHOD);
    this.sendToAlgolia();
    log.info("[FIN]-"+METHOD);
  }


  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      InvalidParameterException, ServletException {
    final String METHOD = "[DeleteItems.doPost]";
    log.info("[INI]-"+METHOD);
    this.sendToAlgolia();
    log.info("[FIN]-"+METHOD);
  }


  private void sendToAlgolia() {
    final String METHOD = "[DeleteItems.sendToAlgolia]";
    log.info("[INI]-"+METHOD);

    final Integer limitResultsClient = 1;
    StringBuilder items = new StringBuilder();

    //log.info("Consultando ["+limitResultsClient+"] item(s) del Datastore con el campo toDelete=true...");
    List<Item> itemsProduct = ofy().load().type(Item.class).filter("toDelete", true).limit(limitResultsClient).list();

    //log.info("Consultando todas las tiendas del Datastore...");
    List<StoreList> storeList = ofy().load().type(StoreList.class).list();

    log.info("IF (!itemsProduct.isEmpty()) : ["+(!itemsProduct.isEmpty())+"]");
    int counterItemsToDelete = 0;
    if (!itemsProduct.isEmpty()) {
      //log.info("FOR (Item item : itemsProduct)");
      for (Item item : itemsProduct) {
        //log.info("FOR (StoreList store : storeList)");
        for (StoreList store : storeList) {
          items.append(",").append(item.getId()).append(store.getId());
          counterItemsToDelete++;
        }
      }

      //log.info("Se ha construido cadena de datos [item+tienda] para eliminar en algolia...");

      //log.info("Eliminando items del Datastore...");
      ofy().delete().entities(itemsProduct).now();

      log.info("se van a eliminar ["+counterItemsToDelete+"] items en algolia a traves de la cola.");
      //log.info("Se adiciona tarea en la cola [algolia]. params: webSafe=>{"+items.toString()+"}, type=>{DELETE}");
      queue1.add(ofy().getTransaction(),TaskOptions.Builder.withUrl("/algoliaCall").param("webSafe", items.toString()).param("type", "DELETE"));

      //log.info("Se adiciona tarea a la cola [productmanager] con url : [/deleteItem]...");
      queue.add(ofy().getTransaction(),TaskOptions.Builder.withUrl("/deleteItems"));
    }
    log.info("[FIN]-"+METHOD);
  }
}
