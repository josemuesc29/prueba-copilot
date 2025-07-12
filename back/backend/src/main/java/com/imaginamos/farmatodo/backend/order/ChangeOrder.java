package com.imaginamos.farmatodo.backend.order;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.imaginamos.farmatodo.model.order.DeliveryOrder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import static com.imaginamos.farmatodo.backend.OfyService.ofy;

public class ChangeOrder extends HttpServlet {
  private static final Logger log = Logger.getLogger(DeliveryOrder.class.getName());
  private static Queue queue = QueueFactory.getQueue("crossmanager");


  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, InvalidParameterException, ServletException {
    int limitResultsClient = 300;
    int countLimit = 0;
    int cont = 0;

    com.googlecode.objectify.cmd.Query<DeliveryOrder> query = ofy().load().type(DeliveryOrder.class).filter("currentStatus", 0);
    query.limit(limitResultsClient);
    //Validate cursor

    QueryResultIterator<DeliveryOrder> iterator = query.iterator();

    List<DeliveryOrder> deliveryOrders = new ArrayList<>();

    //  Load fields of the query
    while (iterator.hasNext() && countLimit < limitResultsClient) {
      DeliveryOrder deliveryOrder = iterator.next();

      if (deliveryOrder.getLastStatus() == null || deliveryOrder.getLastStatus().equals("")) {
        deliveryOrder.setLastStatus("order_delivered");
        deliveryOrder.setActive(false);
        deliveryOrders.add(deliveryOrder);
        cont++;
      } else {
        deliveryOrder.setActive(false);
        deliveryOrders.add(deliveryOrder);
      }
      countLimit++;
    }
    //log.warning(String.valueOf(cont));
    ofy().save().entities(deliveryOrders).now();

    Cursor cursorIter = iterator.getCursor();
    String newCursor = cursorIter.toWebSafeString();

    queue.add(ofy().getTransaction(),
        TaskOptions.Builder.withUrl("/changeOrder").param("cursor", newCursor));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, InvalidParameterException, ServletException {
    String cursor = request.getParameter("cursor");
    int limitResultsClient = 300;
    int countLimit = 0;
    int cont = 0;

    com.googlecode.objectify.cmd.Query<DeliveryOrder> query = ofy().load().type(DeliveryOrder.class).filter("currentStatus", 0);
    query.limit(limitResultsClient);
    //Validate cursor
    if (cursor != null)
      query = query.startAt(Cursor.fromWebSafeString(cursor));

    QueryResultIterator<DeliveryOrder> iterator = query.iterator();

    List<DeliveryOrder> deliveryOrders = new ArrayList<>();

    //  Load fields of the query
    while (iterator.hasNext() && countLimit < limitResultsClient) {
      DeliveryOrder deliveryOrder = iterator.next();
      deliveryOrder.setCreateDate(deliveryOrder.getCreateDate());

      deliveryOrders.add(deliveryOrder);

      countLimit++;
    }
    //log.warning(String.valueOf(cont));
    ofy().save().entities(deliveryOrders).now();

    Cursor cursorIter = iterator.getCursor();
    String newCursor = cursorIter.toWebSafeString();
    if (!Objects.equals(cursor, newCursor)) {
      queue.add(ofy().getTransaction(),
          TaskOptions.Builder.withUrl("/changeOrder").param("cursor", newCursor));
    } else
      log.warning("DONE");
  }
}
