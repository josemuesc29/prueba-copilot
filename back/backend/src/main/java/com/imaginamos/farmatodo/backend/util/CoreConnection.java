package com.imaginamos.farmatodo.backend.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.InternalServerErrorException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.gson.Gson;
import com.imaginamos.farmatodo.model.customer.Customer;
import com.imaginamos.farmatodo.model.order.CreatedOrder;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.model.util.ErrorFarmatodo;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

/**
 * Created by savilag <david.avila@imaginamos.co> on 16/08/2017
 * Property of Imaginamos
 */
public abstract class CoreConnection {

  private static final Logger log = Logger.getLogger(Customer.class.getName());

  private CoreConnection() throws BadRequestException {
  }

  private static <T> T mapObject(String responseJson, Class<T> entityClass) throws IOException {
    T entity;
    log.warning("Response:\n" + responseJson);
    //ObjectMapper objectMapper = new ObjectMapper();
    //entity = objectMapper.readValue(responseJson, entityClass);
    Gson gson = new Gson();
    entity = gson.fromJson(responseJson, entityClass);
    return entity;
  }

  public static <T> T getRequest(String url, Class<T> entityClass)
      throws BadRequestException, IOException {
    try {
      RestTemplate restTemplate = new RestTemplate();
      restTemplate.setErrorHandler(new CoreExceptions());
      ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
      HttpStatus responseCode = responseEntity.getStatusCode();
      T entity;
      if (responseCode == OK) {
        entity = mapObject(responseEntity.getBody(), entityClass);
      } else if (responseCode == NO_CONTENT) {
        return null;
      } else {
        throw new BadRequestException(Constants.DEFAULT_MESSAGE);
      }
      return entity;
    } catch (IOException e) {
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

  public static <T> T request(String url, Class<T> entityClass, String token, HttpMethod operation, String request) throws BadRequestException {
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.set("token", token);
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity entity = Objects.nonNull(request) ? new HttpEntity(request, headers) : new HttpEntity(headers);
      RestTemplate restTemplate = new RestTemplate();
      restTemplate.setErrorHandler(new CoreExceptions());
      ResponseEntity<String> responseEntity = restTemplate.exchange(url, operation, entity, String.class, new HashMap<>());
      return mapObject(responseEntity.getBody(), entityClass);
    } catch (IOException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
      throw new BadRequestException(Constants.DEFAULT_MESSAGE);
    }
  }

  public static <T> T postRequest(String url, String request, Class<T> entityClass)
      throws BadRequestException, InternalServerErrorException, IOException, NotFoundException, UnauthorizedException {
    return postRequest(url, request, entityClass, Constants.DEFAULT_MESSAGE);
  }

  public static <T> T postRequest(String url, String request, Class<T> entityClass, String defaultMessage)
      throws BadRequestException, InternalServerErrorException, IOException, NotFoundException, UnauthorizedException {
    return postRequest(url, null, request, entityClass, defaultMessage);
  }

  public static <T> T postRequest(String url, String token, String request, Class<T> entityClass, String defaultMessage)
      throws BadRequestException, InternalServerErrorException, IOException, NotFoundException, UnauthorizedException {

    log.warning("Request:\n" + request);
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    if (Objects.nonNull(token) && !token.isEmpty()) {
      headers.set("token", token);
    }
    HttpEntity<String> entityRequest = Objects.nonNull(request) && !request.isEmpty() ? new HttpEntity<>(request, headers) : new HttpEntity<>(headers);
    restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
    restTemplate.setErrorHandler(new CoreExceptions());

    T entity = null;
    try {
      log.warning("Pre Peticion Response:\n" + entityRequest);
      ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, entityRequest, String.class);
      log.warning("Response:\n" + responseEntity);
      HttpStatus responseCode = responseEntity.getStatusCode();
      ErrorFarmatodo errorFarmatodo;
      switch (responseCode) {
        case OK:
          log.warning("OK");
          if (!entityClass.equals(Void.class))
            entity = mapObject(responseEntity.getBody(), entityClass);
          break;
        case CREATED:
          log.warning("CREATED");
          if (!entityClass.equals(Void.class))
            entity = mapObject(responseEntity.getBody(), entityClass);
          break;
        case NO_CONTENT:
          log.warning("NO_CONTENT");
          errorFarmatodo = mapObject(responseEntity.getBody(), ErrorFarmatodo.class);
          if (errorFarmatodo != null) {
            log.warning("ERROR");
            throw new NotFoundException(errorFarmatodo.getMessage());
          }
          throw new NotFoundException(Constants.DEFAULT_MESSAGE);
        case UNAUTHORIZED:
          log.warning("UNAUTHORIZED");
          errorFarmatodo = mapObject(responseEntity.getBody(), ErrorFarmatodo.class);
          if (errorFarmatodo != null) {
            log.warning("ERROR");
            throw new UnauthorizedException(errorFarmatodo.getMessage());
          }
          throw new UnauthorizedException(Constants.DEFAULT_MESSAGE);
        case ACCEPTED:
          log.warning("ACCEPTED");
          errorFarmatodo = mapObject(responseEntity.getBody(), ErrorFarmatodo.class);
          getTransaction(entityClass, errorFarmatodo);
        case INTERNAL_SERVER_ERROR:
            log.warning("INTERNAL_SERVER_ERROR");
            errorFarmatodo = mapObject(responseEntity.getBody(), ErrorFarmatodo.class);
          return getTransaction(entityClass, errorFarmatodo);
        default:
          if (!entityClass.equals(Void.class)) {
            log.warning("Default with Entity");
            entity = mapObject(responseEntity.getBody(), entityClass);
            break;
          }
          log.warning("Default");
          throw new BadRequestException(defaultMessage);
      }
    } catch (Exception ex) {
      log.warning("Error: -->" + ex.fillInStackTrace() + " = " + ex.getMessage());
      throw new BadRequestException(ex.getMessage());
    }
    return entity;
  }

  private static <T> T getTransaction(Class<T> entityClass, ErrorFarmatodo errorFarmatodo) throws BadRequestException {
    if (errorFarmatodo != null) {
        log.warning("ERROR");
        if (entityClass.equals(CreatedOrder.class)) {
        }
        throw new BadRequestException(errorFarmatodo.getMessage());
    } else
        throw new BadRequestException(Constants.DEFAULT_MESSAGE);
  }

  public static <T> T putRequestAuth(String url, String clientId, String clientSecret, Class<T> entityClass) throws BadRequestException {
    log.warning("method: putRequestAuth clientId:" + clientId + " clientSecret:" + clientSecret);
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    ErrorFarmatodo errorFarmatodo;
    try {
      headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
      headers.set("client_id", clientId);
      headers.set("client_secret", clientSecret);
      HttpEntity<String> entityRequest = new HttpEntity<>(headers);
      restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
      restTemplate.setErrorHandler(new CoreExceptions());
      ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.PUT, entityRequest, String.class, new HashMap<>());
      return mapObject(responseEntity.getBody(), entityClass);
    } catch (Exception ex) {
      log.warning("method putRequestAuth -> Error: -->" + ex.fillInStackTrace() + " = " + ex.getMessage());
      throw new BadRequestException(ex.getMessage());
    }
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

  /*public static void simplePutRequest(String url) throws BadRequestException {
    log.info("method: simplePutRequest() -> " + url);
    RestTemplate restTemplate = new RestTemplate();
    restTemplate.setErrorHandler(new CoreExceptions());
    restTemplate.put(url, null);
  }*/

  public static <T> T putRequest(String url, String request, Class<T> entityClass) throws BadRequestException {
    log.warning("method: putRequest request:" + request);
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    ErrorFarmatodo errorFarmatodo;
    try {
      headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
      HttpEntity<String> entityRequest = new HttpEntity<>(request, headers);
      restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
      restTemplate.setErrorHandler(new CoreExceptions());
      ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.PUT, entityRequest, String.class, new HashMap<>());
      return mapObject(responseEntity.getBody(), entityClass);
    } catch (Exception ex) {
      log.warning("method putRequest -> Error: -->" + ex.fillInStackTrace() + " = " + ex.getMessage());
      throw new BadRequestException(ex.getMessage());
    }
  }

  public static void deleteRequest(String url)
      throws BadRequestException {
    RestTemplate restTemplate = new RestTemplate();
    restTemplate.setErrorHandler(new CoreExceptions());
    restTemplate.delete(url);
  }

}
