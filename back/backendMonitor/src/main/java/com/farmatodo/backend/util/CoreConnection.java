package com.farmatodo.backend.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.InternalServerErrorException;
import com.imaginamos.farmatodo.model.order.CreatedOrder;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.model.util.ErrorFarmatodo;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

/**
 * Created by savilag <david.avila@imaginamos.co> on 16/08/2017
 * Property of Imaginamos
 */
public abstract class CoreConnection {

  private static final Logger log = Logger.getLogger(CoreConnection.class.getName());

  private CoreConnection() throws BadRequestException {
  }

  private static <T> T mapObject(String responseJson, Class<T> entityClass) throws IOException {
    T entity;
    log.warning("Response:\n" + responseJson);
    ObjectMapper objectMapper = new ObjectMapper();
    entity = objectMapper.readValue(responseJson, entityClass);
    return entity;
  }

  public static <T> T getRequest(String url, Class<T> entityClass)
      throws BadRequestException, IOException {
    try {
      RestTemplate restTemplate = new RestTemplate();
      restTemplate.setErrorHandler(new CoreExceptions());
      ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
      HttpStatus responseCode = responseEntity.getStatusCode();
      T entity = null;
      if (responseCode == OK) {
        if (!entityClass.equals(Void.class) && responseEntity.getBody() != null)
          entity = mapObject(responseEntity.getBody(), entityClass);
      } else if (responseCode == NO_CONTENT) {
        return null;
      } else {
        throw new BadRequestException(Constants.DEFAULT_MESSAGE);
      }
      return entity;
    } catch (IOException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
      throw new BadRequestException(Constants.DEFAULT_MESSAGE);
    }
  }

  public static <T> List<T> getListRequest(String url, Class<T> entityClass)
      throws BadRequestException {
    try {
      RestTemplate restTemplate = new RestTemplate();
      ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
      HttpStatus responseCode = responseEntity.getStatusCode();
      List<T> entity;
      switch (responseCode) {
        case OK:
          ObjectMapper objectMapper = new ObjectMapper();
          entity = objectMapper.readValue(responseEntity.getBody(), objectMapper.getTypeFactory().constructCollectionType(List.class, entityClass));
          return entity;
        case NO_CONTENT:
          return null;
        default:
          throw new BadRequestException(Constants.DEFAULT_MESSAGE);
      }
    } catch (IOException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
      throw new BadRequestException(Constants.DEFAULT_MESSAGE);
    }
  }

  public static <T> T postRequest(String url, String request, Class<T> entityClass)
      throws BadRequestException, InternalServerErrorException, IOException {
    return postRequest(url, request, entityClass, Constants.DEFAULT_MESSAGE);
  }

  public static <T> T postRequest(String url, String request, Class<T> entityClass, String defaultMessage)
      throws BadRequestException, InternalServerErrorException, IOException {

    log.warning("Request:\n" + request);
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> entityRequest = new HttpEntity<>(request, headers);
    restTemplate.getMessageConverters()
        .add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
    restTemplate.setErrorHandler(new CoreExceptions());
    ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, entityRequest, String.class);
    HttpStatus responseCode = responseEntity.getStatusCode();
    ErrorFarmatodo errorFarmatodo;
    T entity = null;
    switch (responseCode) {
      case OK:
        log.warning("OK");
        if (!entityClass.equals(Void.class) && responseEntity.getBody() != null)
          entity = mapObject(responseEntity.getBody(), entityClass);
        break;
      case ACCEPTED:
        log.warning("ACCEPTED");
        errorFarmatodo = mapObject(responseEntity.getBody(), ErrorFarmatodo.class);
        if (errorFarmatodo != null) {
          log.warning("ERROR");
          if (entityClass.equals(CreatedOrder.class)) {
          }
          throw new BadRequestException(errorFarmatodo.getMessage());
        } else
          throw new BadRequestException(Constants.DEFAULT_MESSAGE);
      case BAD_REQUEST:
        log.warning("BAD_REQUEST");
        errorFarmatodo = mapObject(responseEntity.getBody(), ErrorFarmatodo.class);
        if (errorFarmatodo != null) {
          log.warning("ERROR");
          throw new BadRequestException(errorFarmatodo.getMessage());
        } else
          throw new BadRequestException(Constants.DEFAULT_MESSAGE);
      case INTERNAL_SERVER_ERROR:
        log.warning("INTERNAL_SERVER_ERROR");
        errorFarmatodo = mapObject(responseEntity.getBody(), ErrorFarmatodo.class);
        if (errorFarmatodo != null) {
          log.warning("ERROR");
          throw new BadRequestException(errorFarmatodo.getMessage());
        } else
          throw new BadRequestException(Constants.DEFAULT_MESSAGE);
      default:
        throw new BadRequestException(defaultMessage);
    }
    return entity;

  }

  public static void putRequest(String url, String request)
      throws BadRequestException {
    log.warning("Request:\n" + request);
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> entityRequest = new HttpEntity<>(request, headers);
    restTemplate.getMessageConverters()
        .add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
    restTemplate.setErrorHandler(new CoreExceptions());
    restTemplate.put(url, entityRequest);
  }

  public static void deleteRequest(String url)
      throws BadRequestException {
    RestTemplate restTemplate = new RestTemplate();
    restTemplate.setErrorHandler(new CoreExceptions());
    restTemplate.delete(url);
  }

  public static <T> List<T> postListRequest(String url, String request, Class<T> entityClass)
          throws BadRequestException, InternalServerErrorException, IOException {

    log.warning("Request:\n" + request);
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> entityRequest = new HttpEntity<>(request, headers);
    restTemplate.getMessageConverters()
            .add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
    restTemplate.setErrorHandler(new CoreExceptions());
    ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, entityRequest, String.class);
    HttpStatus responseCode = responseEntity.getStatusCode();
    ErrorFarmatodo errorFarmatodo;
    List<T> entity;
    switch (responseCode) {
      case OK:
        log.warning("OK");
        if (!entityClass.equals(Void.class)) {
          ObjectMapper objectMapper = new ObjectMapper();
          entity = objectMapper.readValue(responseEntity.getBody(), objectMapper.getTypeFactory().constructCollectionType(List.class, entityClass));
          return entity;
        }
        return null;
      case ACCEPTED:
        log.warning("ACCEPTED");
        errorFarmatodo = mapObject(responseEntity.getBody(), ErrorFarmatodo.class);
        if (errorFarmatodo != null) {
          log.warning("ERROR");
          if (entityClass.equals(CreatedOrder.class)) {
          }
          throw new BadRequestException(errorFarmatodo.getMessage());
        } else
          throw new BadRequestException(Constants.DEFAULT_MESSAGE);
      case NO_CONTENT:
        return null;
      default:
        throw new BadRequestException(Constants.DEFAULT_MESSAGE);
    }
  }

}
