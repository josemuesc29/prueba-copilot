package com.imaginamos.farmatodo.backend.util;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.imaginamos.farmatodo.model.store.Store;
import com.imaginamos.farmatodo.model.util.Segment;
import com.imaginamos.farmatodo.model.util.URLConnections;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.imaginamos.farmatodo.backend.OfyService.ofy;

/**
 * Created by mileniopc on 12/6/16.
 * Property of Imaginamos.
 */

public class SegmentUpload extends HttpServlet {
  private static final Logger log = Logger.getLogger(Store.class.getName());

  /**
   * Brings the Segments of the database of "Farmatodo"
   *
   * @param request  Object of class "HttpServletRequest"
   * @param response Object of class "HttpServletResponse"
   * @throws IOException
   * @throws InvalidParameterException
   * @throws ServletException
   */
  @Deprecated
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) {
    throw new UnsupportedOperationException("Operación no soportada");
    /*
    try {

      URL url = new URL(URLConnections.URL_SEGMENTS);
      HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
      httpURLConnection.setRequestMethod(URLConnections.GET);
      httpURLConnection.setRequestProperty("Accept-Charset", "ISO-8859-1");

      response.setContentType(URLConnections.CONTENT_TYPE_JSON);
      response.addHeader("Access-Control-Allow-Origin", "*");
      JsonObject jsonObject = new JsonObject();

      int responseCode = httpURLConnection.getResponseCode();

      switch (responseCode) {
        case 200:
          BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "ISO-8859-1"));
          String inputLine;
          StringBuilder responseJson = new StringBuilder();

          while ((inputLine = bufferedReader.readLine()) != null) {
            responseJson.append(inputLine);
          }
          bufferedReader.close();
          ObjectMapper objectMapper = new ObjectMapper();
          List<Segment> segmentList = objectMapper.readValue(responseJson.toString(), objectMapper.getTypeFactory().constructCollectionType(List.class, Segment.class));
          this.saveSegments(segmentList);
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
      }

      PrintWriter out = response.getWriter();
      out.print(jsonObject);
      out.flush();
      out.close();
    } catch (Exception ex) {
      log.log(Level.SEVERE, ex.getMessage(), ex);
    }
     */
  }

  /**
   * Create a new record for each segment.
   *
   * @param segmentList Array of Object of class "Segment"
   */
  private void saveSegments(List<Segment> segmentList) {
    for (Segment segment : segmentList) {
      segment.setIdSegment(UUID.randomUUID().toString());
      ofy().save().entity(segment).now();
    }
  }
}
