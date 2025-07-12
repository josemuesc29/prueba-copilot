package com.imaginamos.farmatodo.backend.product;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.imaginamos.farmatodo.model.product.Item;
import com.imaginamos.farmatodo.model.product.StoreInformation;
import com.imaginamos.farmatodo.model.store.Store;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.logging.Logger;

import static com.imaginamos.farmatodo.backend.OfyService.ofy;

public class DeleteStocks extends HttpServlet {
  private static final Logger log = Logger.getLogger(Store.class.getName());
  private static Queue queue = QueueFactory.getQueue("crossmanager");


  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, InvalidParameterException, ServletException {
    int limitResultsClient = 300;
    int countLimit = 0;
    int cont = 0;

    com.googlecode.objectify.cmd.Query<Item> query = ofy().load().type(Item.class);
    query.limit(limitResultsClient);
    //Validate cursor

    QueryResultIterator<Item> iterator = query.iterator();

    List<Item> items = new ArrayList<>();

    //  Load fields of the query
    while (iterator.hasNext() && countLimit < limitResultsClient) {
      Item item = iterator.next();
      item = setStores(item);
      items.add(item);

      countLimit++;
    }
    //log.warning(String.valueOf(cont));
    ofy().save().entities(items).now();

    Cursor cursorIter = iterator.getCursor();
    String newCursor = cursorIter.toWebSafeString();

    queue.add(ofy().getTransaction(),
        TaskOptions.Builder.withUrl("/deleteStocks").param("cursor", newCursor));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, InvalidParameterException, ServletException {
    String cursor = request.getParameter("cursor");
    int limitResultsClient = 300;
    int countLimit = 0;
    int cont = 0;

    com.googlecode.objectify.cmd.Query<Item> query = ofy().load().type(Item.class);
    query.limit(limitResultsClient);
    //Validate cursor
    if (cursor != null)
      query = query.startAt(Cursor.fromWebSafeString(cursor));

    QueryResultIterator<Item> iterator = query.iterator();

    List<Item> items = new ArrayList<>();

    //  Load fields of the query
    while (iterator.hasNext() && countLimit < limitResultsClient) {
      Item item = iterator.next();
      item = setStores(item);
      items.add(item);
      countLimit++;
    }
    //log.warning(String.valueOf(cont));
    ofy().save().entities(items).now();

    Cursor cursorIter = iterator.getCursor();
    String newCursor = cursorIter.toWebSafeString();
    if (!Objects.equals(cursor, newCursor)) {
      queue.add(ofy().getTransaction(),
          TaskOptions.Builder.withUrl("/deleteStocks").param("cursor", newCursor));
    } else
      log.warning("DONE");

  }

  private Item setStores(Item item) {
    List<StoreInformation> storeInformations = item.getStoreInformation();
    if (storeInformations != null && !storeInformations.isEmpty()) {
      Set<Long> subtitles = new HashSet<>();
      storeInformations.removeIf(storeInformation -> !subtitles.add(storeInformation.getStoreGroupId()));
      item.setStoreInformation(storeInformations);
    }
    return item;
  }
        /*
        List<StoreInformation> storeInformations = new ArrayList<>();
        storeInformations.addAll(item.getStoreInformation());
        List<StoreInformation> storeInformationsCopy = new ArrayList<>();
        storeInformationsCopy.addAll(item.getStoreInformation());
        storeInformationsCopy.forEach(storeInformation -> {
            int cont = 0;
            for(StoreInformation storeInformation1 : item.getStoreInformation()){
                if(storeInformation.getStoreGroupId()==storeInformation1.getStoreGroupId())
                {
                    cont++;
                    if(cont>1)
                    {
                        storeInformations.remove(storeInformation1);
                    }
                }
            }
        });
        item.setStoreInformation(storeInformations);
        return item;
    }
    */
}
