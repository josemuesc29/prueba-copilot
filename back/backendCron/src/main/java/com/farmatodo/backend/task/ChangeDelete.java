package com.farmatodo.backend.task;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
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

public class ChangeDelete extends HttpServlet {
  private Queue queue = QueueFactory.getQueue("productmanager");
  private static final Logger log = Logger.getLogger(ChangeDelete.class.getName());

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      InvalidParameterException, ServletException {
    final String METHOD = "[ChangeDelete.doPost]";
    log.info("[INI]-"+METHOD);

    final Integer limitResultsClient = 300;

    log.info("Consultando ["+limitResultsClient+"] Items del Datastore dentro de List<Item> itemsProduct.");
    List<Item> itemsProduct = ofy().load().type(Item.class).limit(limitResultsClient).list();

    log.info("IF (!itemsProduct.isEmpty()) : ["+(!itemsProduct.isEmpty())+"]");
    if (!itemsProduct.isEmpty()) {

      //log.info("FOR (Item item : itemsProduct)");
      //log.info("Configurando atributo [toDelete] a TRUE...");
      for (Item item : itemsProduct) {
        item.setToDelete(true);
      }

      //log.info("Guardando items con nuevo estado en el Datastore...");
      ofy().save().entities(itemsProduct).now();
    }
    log.info("[FIN]-"+METHOD);
  }

}
