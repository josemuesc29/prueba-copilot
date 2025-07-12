package com.farmatodo.backend.crons;

import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.ConflictException;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.imaginamos.farmatodo.model.item.ItemReq;
import com.imaginamos.farmatodo.model.item.ItemRes;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.model.util.VersionControl;
import com.imaginamos.farmatodo.networking.services.ApiGatewayService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Logger;

import static com.farmatodo.backend.OfyService.ofy;

/**
 * Created by eric on 11/05/17.
 */

public class CategoriesAndItems extends HttpServlet {

  private final String QUEUE_NAME = "cronqueue";
  private Queue queue = QueueFactory.getQueue(QUEUE_NAME);
  private static final Logger LOGGER = Logger.getLogger(CategoriesAndItems.class.getName());

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    final String METHOD = "[CategoriesAndItems.doGet]";
    LOGGER.info("INI-"+METHOD);

    LOGGER.info("Se adiciona transaccion [/categoryUpload] a la cola ["+QUEUE_NAME+"]");
    queue.add(ofy().getTransaction(),TaskOptions.Builder.withUrl("/categoryUpload"));
    this.products();

    LOGGER.info("FIN-"+METHOD);

  }

  private void products() {
    final String METHOD = "[CategoriesAndItems.products]";
    LOGGER.info("INI-"+METHOD);

    Long token = null;
    try {
      token = this.itemStart();
    } catch (ConflictException | BadRequestException | IOException e) {
      e.printStackTrace();
    }

    LOGGER.info("IF (token != null) : ["+(token != null)+"]");
    if (token != null) {

      final String PAGE_PARAM  = "page";
      final String TOKEN_PARAM = "token";

      LOGGER.info("Se adiciona transaccion [/productUpload] a la cola ["+QUEUE_NAME+"]. params: "+PAGE_PARAM+":["+Integer.toString(0)+"], "+TOKEN_PARAM+":["+Long.toString(token)+"]");
      queue.add(ofy().getTransaction(),
          TaskOptions.Builder.withUrl("/productUpload").param(PAGE_PARAM, Integer.toString(0)).param(TOKEN_PARAM, Long.toString(token)));
    }

    LOGGER.info("FIN-"+METHOD);
  }

  @SuppressWarnings("ALL")
  private long itemStart() throws IOException, ConflictException, BadRequestException {
      final String METHOD = "[CategoriesAndItems.itemStart]";
      LOGGER.info("INI-"+METHOD);

      /*String urlString = URLConnections.URL_START_PRODUCTS;
      LOGGER.info("Inicia conexion a URL : ["+urlString+"]");
      HttpURLConnection httpURLConnection = URLConnections.getConnection(URLConnections.POST, urlString);

      OutputStreamWriter wr = new OutputStreamWriter(httpURLConnection.getOutputStream(), "UTF-8");
      JSONObject itemRequest = new JSONObject();*/

      LOGGER.info("Consultando entidad [VersionControl] en DataStore donde la columna [tableControl]=[Item]");
      VersionControl versionControl = ofy().load().type(VersionControl.class).filter("tableControl", "Item").first().now();

      LOGGER.info("IF (versionControl == null) : ["+(versionControl == null)+"]");
      if (versionControl == null) {
          throw new ConflictException(Constants.NO_VERSION);
      }

      LOGGER.info("Creando request.. params: { source:"+Constants.DIGITAL+", revision:"+versionControl.getControlNumber()+" }");

      /*
      itemRequest.put("source", Constants.DIGITAL);
      itemRequest.put("revision", versionControl.getControlNumber());
      LOGGER.info("Mensaje final a enviar : ["+itemRequest.toJSONString()+"]");
       */

      ItemRes response = ApiGatewayService.get().postItemStart(new ItemReq(Constants.DIGITAL, versionControl.getControlNumber()));
      if(Objects.nonNull(response) && Objects.nonNull(response.getToken())){
          LOGGER.info("token a retornar : ["+response.getToken()+"]");
          LOGGER.info("FIN-"+METHOD);
          return response.getToken();
      }
      throw new BadRequestException(Constants.DEFAULT_MESSAGE);

      /*
      wr.write(itemRequest.toJSONString());
      wr.flush();
      int responseCode = httpURLConnection.getResponseCode();
      long token;

      LOGGER.info("responseCode : ["+responseCode+"]");

      switch (responseCode) {
          case 200:
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8"));
            String inputLine;
            StringBuilder responseJson = new StringBuilder();

            while ((inputLine = bufferedReader.readLine()) != null) {
              responseJson.append(inputLine);
            }
            bufferedReader.close();

            LOGGER.info("responseJson : ["+responseJson.toString()+"]");

            ObjectMapper objectMapper = new ObjectMapper();
            VersionControl version = objectMapper.readValue(responseJson.toString(), VersionControl.class);
            token = version.getToken();
            break;

          default:
            throw new BadRequestException(Constants.DEFAULT_MESSAGE);
        }

        LOGGER.info("token a retornar : ["+token+"]");
        LOGGER.info("FIN-"+METHOD);
        return token;

       */
    }
}
