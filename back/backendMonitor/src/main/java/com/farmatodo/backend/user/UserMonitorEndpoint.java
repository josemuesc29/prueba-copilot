package com.farmatodo.backend.user;

import com.farmatodo.backend.util.CoreConnection;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.ConflictException;
import com.google.api.server.spi.response.InternalServerErrorException;
import com.imaginamos.farmatodo.model.monitor.UserRequest;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.model.util.URLConnections;
import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.logging.Logger;

/**
 * @Api - Google indicator that the present class ins an Api.
 * Every further indication of @ApiMethod includes this as a Google API.
 */
@Api(name = "userMonitorEndpoint",
    version = "v1",
    scopes = {Constants.EMAIL_SCOPE},
    clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID_DEVELOPER, com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID},
    audiences = {Constants.ANDROID_AUDIENCE},
    namespace = @ApiNamespace(ownerDomain = Constants.OWNER_DOMAIN, ownerName = Constants.OWNER_NAME, packagePath = ""),
    description = "Get user information for monitor.")
public class UserMonitorEndpoint {
  private static final Logger log = Logger.getLogger(UserMonitorEndpoint.class.getName());
  private Authenticate authenticate;

  public UserMonitorEndpoint() {
    authenticate = new Authenticate();
  }

  /**
   * Login user monitor
   * @param userRequest
   * @return Json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "loginMonitor", path = "/userMonitorEndpoint/loginMonitor", httpMethod = ApiMethod.HttpMethod.POST)
  public JSONObject loginMonitor(
          final UserRequest userRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (userRequest == null || userRequest.getEmployeeNumber() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NUMBER);

    if (userRequest == null || userRequest.getPassword() == null)
      throw new ConflictException(Constants.ERROR_USER_PASSWORD);

    JSONObject customerJson = new JSONObject();
    customerJson.put("employeeNumber", userRequest.getEmployeeNumber());
    customerJson.put("password", userRequest.getPassword());

    return CoreConnection.postRequest(URLConnections.URL_LOGIN_MONITOR_USER, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Validate token session
   * @param userRequest
   * @return Json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "validateSessionToken", path = "/userMonitorEndpoint/validateSessionToken", httpMethod = ApiMethod.HttpMethod.POST)
  public JSONObject validateSessionToken(
          final UserRequest userRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (userRequest == null || userRequest.getEmployeeNumber() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NUMBER);

    if (userRequest == null || userRequest.getToken() == null)
      throw new ConflictException(Constants.ERROR_TOKEN_SESSION);

    JSONObject customerJson = new JSONObject();
    customerJson.put("employeeNumber", userRequest.getEmployeeNumber());
    customerJson.put("token", userRequest.getToken());

    return CoreConnection.postRequest(URLConnections.URL_VALIDATE_SESSION_TOKEN, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Recovery password
   * @param userRequest
   * @return Json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "recoveryPassword", path = "/userMonitorEndpoint/recoveryPassword", httpMethod = ApiMethod.HttpMethod.POST)
  public JSONObject recoveryPassword(
          final UserRequest userRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (userRequest == null || userRequest.getEmployeeNumber() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NUMBER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("employeeNumber", userRequest.getEmployeeNumber());
    customerJson.put("password", "0");

    return CoreConnection.postRequest(URLConnections.URL_RECOVERY_PASSWORD_USER, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Change password user
   * @param userRequest
   * @return Json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "changePassword", path = "/userMonitorEndpoint/changePassword", httpMethod = ApiMethod.HttpMethod.POST)
  public JSONObject changePassword(
          final UserRequest userRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (userRequest == null || userRequest.getEmployeeNumber() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NUMBER);

    if (userRequest == null || userRequest.getOldPassword() == null)
      throw new ConflictException(Constants.ERROR_USER_OLD_PASSWORD);

    if (userRequest == null || userRequest.getNewPassword() == null)
      throw new ConflictException(Constants.ERROR_USER_NEW_PASSWORD);

    JSONObject customerJson = new JSONObject();
    customerJson.put("employeeNumber", userRequest.getEmployeeNumber());
    customerJson.put("oldPassword", userRequest.getOldPassword());
    customerJson.put("newPassword", userRequest.getNewPassword());

    return CoreConnection.postRequest(URLConnections.URL_CHANGE_PASSWORD_USER, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Create user monitor
   * @param userRequest
   * @return json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "createUser", path = "/userMonitorEndpoint/createUser", httpMethod = ApiMethod.HttpMethod.POST)
  public JSONObject createUser(
          final UserRequest userRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (userRequest == null || userRequest.getEmployeeNumber() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NUMBER);

    if (userRequest == null || userRequest.getRolUser() == null)
      throw new ConflictException(Constants.ERROR_ROL_USER);

    if (userRequest == null || userRequest.getEmployeeName() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NAME);

    if (userRequest == null || userRequest.getEmail() == null)
      throw new ConflictException(Constants.ERROR_EMAIL_USER);

    if (userRequest == null || userRequest.getPassword() == null)
      throw new ConflictException(Constants.ERROR_USER_PASSWORD);

    JSONObject customerJson = new JSONObject();
    customerJson.put("employeeNumber", userRequest.getEmployeeNumber());
    customerJson.put("storeId", userRequest.getStoreId());
    customerJson.put("rolUser", userRequest.getRolUser());
    customerJson.put("employeeName", userRequest.getEmployeeName());
    customerJson.put("email", userRequest.getEmail());
    customerJson.put("password", userRequest.getPassword());

    return CoreConnection.postRequest(URLConnections.URL_CREATE_USER, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Update user
   * @param userRequest
   * @return json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "updateUser", path = "/userMonitorEndpoint/updateUser", httpMethod = ApiMethod.HttpMethod.PUT)
  public JSONObject updateUser(
          final UserRequest userRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (userRequest == null || userRequest.getEmployeeNumber() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NUMBER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("employeeNumber", userRequest.getEmployeeNumber());
    customerJson.put("storeId", userRequest.getStoreId());
    if (userRequest.getRolUser() != null && !userRequest.getRolUser().isEmpty())
      customerJson.put("rolUser", userRequest.getRolUser());
    if (userRequest.getEmployeeName() != null && !userRequest.getEmployeeName().isEmpty())
      customerJson.put("employeeName", userRequest.getEmployeeName());
    if (userRequest.getEmail() != null && !userRequest.getEmail().isEmpty())
      customerJson.put("email", userRequest.getEmail());

      return CoreConnection.postRequest(URLConnections.URL_EDIT_USER, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Delete user
   * @param userRequest
   * @return json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "deleteUser", path = "/userMonitorEndpoint/deleteUser", httpMethod = ApiMethod.HttpMethod.DELETE)
  public JSONObject deleteUser(
          final UserRequest userRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (userRequest == null || userRequest.getEmployeeNumber() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NUMBER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("employeeNumber", userRequest.getEmployeeNumber());
    customerJson.put("password", "0");

    String request = customerJson.toJSONString();
    log.info(request);
    HttpURLConnection httpURLConnection = URLConnections.getConnection(URLConnections.DELETE, URLConnections.URL_DELETE_USER);
    OutputStreamWriter wr = new OutputStreamWriter(httpURLConnection.getOutputStream(), "UTF-8");
    wr.write(request);
    wr.flush();
    int responseCode = httpURLConnection.getResponseCode();
    log.info("Code response delete user [" + responseCode + "]");

    switch (responseCode) {
      case 201:
      case 200:
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8"));
        String inputLine;
        StringBuilder responseJson = new StringBuilder();
        while ((inputLine = bufferedReader.readLine()) != null) {
          responseJson.append(inputLine);
        }
        bufferedReader.close();
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(responseJson.toString(), JSONObject.class);
      default:
        throw new ConflictException(Constants.DEFAULT_MESSAGE);
    }
  }

  /**
   * Get rols user monitor
   * @return json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getRols", path = "/userMonitorEndpoint/getRols", httpMethod = ApiMethod.HttpMethod.GET)
  public List<JSONObject> getRols() throws ConflictException, BadRequestException, IOException, InternalServerErrorException {
    return CoreConnection.getListRequest(URLConnections.URL_GET_ROLS_MONITOR, JSONObject.class);
  }

  /**
   * Get users monitor
   * @return json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getUsers", path = "/userMonitorEndpoint/getUsers", httpMethod = ApiMethod.HttpMethod.GET)
  public List<JSONObject> getUsers() throws ConflictException, BadRequestException, IOException, InternalServerErrorException {
    return CoreConnection.getListRequest(URLConnections.URL_GET_USERS_MONITOR, JSONObject.class);
  }

  /**
   * Update user tokens firebase
   * @param userRequest
   * @return
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "updateTokenFirebase", path = "/userMonitorEndpoint/updateTokenFirebase", httpMethod = ApiMethod.HttpMethod.POST)
  public JSONObject updateTokenFirebase(
          final UserRequest userRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (userRequest == null || userRequest.getEmployeeNumber() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NUMBER);

    if (userRequest == null || userRequest.getTokenFirebaseAuth() == null)
      throw new ConflictException(Constants.ERROR_TOKEN_FIREBASE_AUTH);

    if (userRequest == null || userRequest.getTokenFirebasePush() == null)
      throw new ConflictException(Constants.ERROR_TOKEN_FIREBASE_PUSH);

    JSONObject customerJson = new JSONObject();
    customerJson.put("employeeNumber", userRequest.getEmployeeNumber());
    customerJson.put("tokenFirebaseAuth", userRequest.getTokenFirebaseAuth());
    customerJson.put("tokenFirebasePush", userRequest.getTokenFirebasePush());

    return CoreConnection.postRequest(URLConnections.URL_UPDATE_USER_TOKEN_FIREBASE, customerJson.toJSONString(), JSONObject.class);
  }

}

