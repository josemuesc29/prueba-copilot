package com.farmatodo.backend.task;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.googlecode.objectify.Key;
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
 * Created by eric on 10/04/17.
 *
 * @version 1.1
 * @author JPuentes
 * @implNote Se disminuye la cantidad de tareas que se agregan a las colas. Pasa de 200 a 10.
 *
 */

public class ProductUpdateAlgolia extends HttpServlet {

  private Queue queue = QueueFactory.getQueue("algoliamanager");
  private Queue queue1 = QueueFactory.getQueue("algolia");
  private static final Logger log = Logger.getLogger(ProductUpdateAlgolia.class.getName());

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, InvalidParameterException, ServletException {
    final String METHOD = "[ProductUpdateAlgolia.doGet]";
    log.info("[INI]-"+METHOD);
    this.sendToAlgolia();
    log.info("[FIN]-"+METHOD);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, InvalidParameterException, ServletException {
    final String METHOD = "[ProductUpdateAlgolia.doPost]";
    log.info("[INI]-"+METHOD);
    this.sendToAlgolia();
    log.info("[FIN]-"+METHOD);
  }

  private void sendToAlgolia() {
    final String METHOD = "[ProductUpdateAlgolia.sendToAlgolia]";
    log.info("[INI]-"+METHOD);

    final Integer limitResultsClient = 10;// Cada cola permite MAX: 100KB, cada item pesa 9KB. TOTAL: 90KB.
    StringBuilder items = new StringBuilder();
    log.info("Consultando ["+limitResultsClient+"] entidades del Datastore donde toIndexInAlgolia=true...");
    List<Item> itemsProduct = ofy().load().type(Item.class).filter("toIndexInAlgolia", true).limit(limitResultsClient).list();
    //List<Item> itemsProduct = ofy().load().type(Item.class).filter("toIndexInAlgolia", true).list();

    log.info("IF (!itemsProduct.isEmpty() : ["+(!itemsProduct.isEmpty())+"]");
    if (!itemsProduct.isEmpty()) {
      for (Item item : itemsProduct) {
        Key<Item> candidateKey = Key.create(item);
        items.append(",").append(candidateKey.toWebSafeString());
        item.setToIndexInAlgolia(false);
        //Mejora, guardar entidad por entidad y no en bloques...
        ofy().save().entity(item).now();
      }

      //log.info("Guardando ["+itemsProduct.size()+"] entidades en el Datastore con indexToAlgolia=false...");
      //ofy().save().entities(itemsProduct).now();

      log.info("Se adiciona tarea a la cola [algolia]. params: webSafe=>"+items.toString()+", type=>UPDATE");
      queue1.add(ofy().getTransaction(), TaskOptions.Builder.withUrl("/algoliaCall").param("webSafe", items.toString()).param("type", "UPDATE"));
      log.info("Se adiciona tarea a la cola [algoliaManager].");
      queue.add(ofy().getTransaction(),  TaskOptions.Builder.withUrl("/productUpdateAlgolia"));
    }
    log.info("[INI]-"+METHOD);
  }
}
