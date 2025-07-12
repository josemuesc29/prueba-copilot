package com.farmatodo.backend.crons;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

import static com.farmatodo.backend.OfyService.ofy;

/**
 * Created by USUARIO on 08/07/2017.
 */
public class OffersAndHighlights extends HttpServlet {

  private static final Logger LOGGER = Logger.getLogger(OffersAndHighlights.class.getName());
  private Queue queue = QueueFactory.getQueue("cronqueue");

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    final String METHOD = "[OffersAndHighlights.doGet]";
    LOGGER.info("[INI]-"+METHOD);

    LOGGER.info("Se adiciona tarea a la cola [cronqueue] con url: [/offerUpload]. Params: page: {"+Integer.toString(0)+"}");
    queue.add(ofy().getTransaction(), TaskOptions.Builder.withUrl("/offerUpload").param("page", Integer.toString(0)));

    LOGGER.info("Se adiciona tarea a la cola [cronqueue] con url: [/highlightUpload]. Params: page: {"+Integer.toString(0)+"}");
    queue.add(ofy().getTransaction(), TaskOptions.Builder.withUrl("/highlightUpload").param("page", Integer.toString(0)));

    LOGGER.info("[FIN]-"+METHOD);
  }
}
