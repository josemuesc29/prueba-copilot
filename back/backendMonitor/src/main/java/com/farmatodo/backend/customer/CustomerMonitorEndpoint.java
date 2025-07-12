package com.farmatodo.backend.customer;

import com.farmatodo.backend.customerCoupon.application.CustomerCouponManagerHandler;
import com.farmatodo.backend.customerCoupon.application.CustomerCouponManagerInterface;
import com.farmatodo.backend.customerCoupon.infrastructure.CustomerCouponRepositoryDatastore;
import com.farmatodo.backend.user.Authenticate;
import com.farmatodo.backend.user.Users;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.ConflictException;
import com.google.api.server.spi.response.InternalServerErrorException;
import com.googlecode.objectify.Key;
import com.imaginamos.farmatodo.model.coupon.CustomerCoupon;
import com.imaginamos.farmatodo.model.customer.*;
import com.imaginamos.farmatodo.model.user.BlockedUser;
import com.imaginamos.farmatodo.model.user.Credential;
import com.imaginamos.farmatodo.model.user.User;
import com.imaginamos.farmatodo.model.util.Answer;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.networking.api.ApiCore;
import com.imaginamos.farmatodo.networking.services.ApiGatewayService;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import retrofit2.Response;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import static com.farmatodo.backend.OfyService.ofy;

/**
 * @Api - Google indicator that the present class ins an Api.
 * Every further indication of @ApiMethod includes this as a Google API.
 */
@Api(name = "customerMonitorEndpoint",
        version = "v1",
        scopes = {Constants.EMAIL_SCOPE},
        clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID_DEVELOPER, com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID},
        audiences = {Constants.ANDROID_AUDIENCE},
        namespace = @ApiNamespace(ownerDomain = Constants.OWNER_DOMAIN, ownerName = Constants.OWNER_NAME, packagePath = ""),
        description = "Get customer information for monitor.")
public class CustomerMonitorEndpoint {
  private static final Logger log = Logger.getLogger(CustomerMonitorEndpoint.class.getName());
  private Authenticate authenticate;
  private ApiCore coreApi;
  private Users users;

  private CustomerCouponManagerInterface customerCouponManager;

  public CustomerMonitorEndpoint() {
    authenticate = new Authenticate();
    users = new Users();
    CustomerCouponRepositoryDatastore customerCouponRepository = new CustomerCouponRepositoryDatastore();
    this.customerCouponManager = new CustomerCouponManagerHandler(users, customerCouponRepository);
  }


  /**
   * Verify customer user is blocked
   *
   * @param customerId
   * @return Json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "verifyUser", path = "/customerMonitorEndpoint/verifyUser", httpMethod = ApiMethod.HttpMethod.GET)
  public Answer verifyUser(
          @Named("customerId") final long customerId) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (customerId <= 0)
      throw new BadRequestException("Bad Request");

    Answer answer = new Answer();
    answer.setConfirmation(false);

    // validar si el cliente ya a sido bloqueado
    BlockedUser blockedUserSaved = ofy().load().type(BlockedUser.class).filter("idUser", customerId).first().now();

    log.info("User -> " + (blockedUserSaved != null ? blockedUserSaved.toString() : null));
    boolean userBlocked = blockedUserSaved != null;

    if (userBlocked) {
      answer.setConfirmation(true);
      answer.setMessage("El usuario esta bloqueado");
    } else {
      answer.setMessage("El usuario no esta bloqueado");
      answer.setConfirmation(false);
    }
    return answer;
  }

  /**
   * Block customer user
   *
   * @param blockUserReq
   * @return Answer response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "blockUser", path = "/customerMonitorEndpoint/blockUser", httpMethod = ApiMethod.HttpMethod.POST)
  public Answer blockUser(
          final BlockUserReq blockUserReq) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (!blockUserReq.isValid())
      throw new BadRequestException(Constants.ERROR_BLOCK_USER);

    Answer answer = new Answer();
    answer.setConfirmation(false);

    //Validar si el cliente ya a sido bloqueado
    BlockedUser blockedUserSaved = ofy().load().type(BlockedUser.class).filter("idUser", blockUserReq.getIdUser()).first().now();

    boolean userBlocked = blockedUserSaved != null;

    log.info("Usuario bloqueado -> " + userBlocked);

    if (userBlocked) {
      answer.setMessage("Error el usuario ya fue bloqueado anteriormente");
    } else {

      try {
        BlockedUser blockedUser = new BlockedUser();
        blockedUser.setIdBlockedUser(UUID.randomUUID().toString());
        blockedUser.setIdUser(blockUserReq.getIdUser());
        blockedUser.setReasonBlock(blockUserReq.getReasonBlock());
        ofy().save().entity(blockedUser).now();

        answer.setMessage("Usuario bloqueado correctamente");
        answer.setConfirmation(true);

      } catch (Exception e) {
        log.info("Error -> " + e.getMessage());
        throw new BadRequestException("Error al intentar bloquear el usuario");
      }
    }
    return answer;
  }

  /**
   * Unblock customer user
   *
   * @param blockUserReq
   * @return Answer response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "unblockUser", path = "/customerMonitorEndpoint/unblockUser", httpMethod = ApiMethod.HttpMethod.POST)
  public Answer unblockUser(
          final BlockUserReq blockUserReq) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    Answer answer = new Answer();
    answer.setConfirmation(false);

    //Validar si el cliente ya a sido bloqueado
    BlockedUser blockedUserSaved = ofy().load().type(BlockedUser.class).filter("idUser", blockUserReq.getIdUser()).first().now();

    boolean userBlocked = blockedUserSaved != null;

    log.info("usuario bloqueado -> " + userBlocked);

    if (userBlocked) {
      ofy().delete().entity(blockedUserSaved).now();
      answer.setMessage("Usuario desbloqueado correctamente");
      answer.setConfirmation(true);
    } else {
      answer.setMessage("El usuario no esta bloqueado");
      answer.setConfirmation(false);
    }
    return answer;
  }

  /**
   * Get customer info
   *
   * @return
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getCustomer", path = "/customerMonitorEndpoint/getCustomer", httpMethod = ApiMethod.HttpMethod.GET)
  public CustomerOnlyData getCustomer(
          @Named("customerId") final int customerId
  ) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    //return CoreService.get().getCustomer(customerId);
    return ApiGatewayService.get().getCustomerOnlyById(customerId);
  }


  @ApiMethod(name = "customerById", path = "/customerMonitorEndpoint/customerById", httpMethod = ApiMethod.HttpMethod.GET)
  public CustomerJSON customerById(@Named("customerId") final Long customerId)
          throws BadRequestException, IOException {
    log.info("method: customerById({},{})" + customerId);
    //String url = URLConnections.URL_CUSTOMER + "/" + customerId;
    //return CoreConnection.getRequest(url, JSONObject.class);

    Optional<CustomerJSON> optionalCustomerJSON = ApiGatewayService.get().getCustomerById(customerId.intValue());

    return optionalCustomerJSON.orElseGet(CustomerJSON::new);
  }

  @ApiMethod(name = "customerByDocument", path = "/customerMonitorEndpoint/customerByDocument", httpMethod = ApiMethod.HttpMethod.GET)
  public CustomerJSON customerByDocument(@Named("document") final String document)
          throws BadRequestException, IOException, ConflictException {
    log.info("method: customerByDocument({},{})" + document);
    //String url = URLConnections.URL_CUSTOMER_BY_DOCUMENT + "/" + document;
    //return CoreConnection.getRequest(url, JSONObject.class);
    return ApiGatewayService.get().getCustomerByDocument(document);
  }

  @ApiMethod(name = "addressesByIdCustomer", path = "/customerMonitorEndpoint/addressesByIdCustomer", httpMethod = ApiMethod.HttpMethod.GET)
  public AddressesRes addressesByIdCustomer(@Named("customerId") final Long customerId)
          throws BadRequestException, IOException {
    log.info("method: addressesByIdCustomer({},{})" + customerId);
    Map<String, String> pathVariables = new HashMap();
    pathVariables.put("idCustomer", String.valueOf(customerId));
    //String url = Util.buildUrl(URLConnections.URL_GET_CUSTOMER_ADDRESSES, pathVariables, null);
    //log.info("url=" + url);
    //return CoreConnection.getRequest(url, JSONObject.class);
    try {
      return ApiGatewayService.get().getAddressesByCustomerId(customerId.intValue());
    }catch(Exception e){
      log.info("Cliente no existe");
    }
    return new AddressesRes();
  }

  @ApiMethod(name = "lifemilesByIdCustomer", path = "/customerMonitorEndpoint/lifemilesByIdCustomer", httpMethod = ApiMethod.HttpMethod.POST)
  public CustomerLifeMileJSON lifemilesByIdCustomer(final CustomerData data)
          throws BadRequestException, IOException, InternalServerErrorException {
    JSONObject objectJson = new JSONObject();
    objectJson.put("customer", String.valueOf(data.getId()));
    log.info("toJSONString:" + objectJson.toJSONString());
    //log.info("URLConnections.URL_CUSTOMER_LIFEMILE_NUMBER:" + URLConnections.URL_CUSTOMER_LIFEMILE_NUMBER);
    //return CoreConnection.postRequest(URLConnections.URL_CUSTOMER_LIFEMILE_NUMBER, objectJson.toJSONString(), JSONObject.class);
    return ApiGatewayService.get().getCustomerLifeMilesNumber(new CustomerLifeMilesReq(new Long(data.getId())));
  }

  @ApiMethod(name = "getCustomerByIdDocEmail", path = "/customerMonitorEndpoint/getCustomerByIdDocEmail", httpMethod = ApiMethod.HttpMethod.POST)
  public CustomerJSON getCustomerByIdDocEmail(final CustomerData data)
          throws BadRequestException, IOException, InternalServerErrorException, ConflictException {
    log.info("method: getCustomerByIdDocEmail({})" + data);
    try {
      return ApiGatewayService.get().readCustomerMonitor(data);
    }catch (Exception ex){
      log.warning("No fue posible consultar el usuario: "+data);
      return new CustomerJSON();
    }
  }

  @ApiMethod(name = "updateCustomer", path = "/customerMonitorEndpoint/updateCustomer", httpMethod = ApiMethod.HttpMethod.PUT)
  public void updateCustomer(final CustomerJSON data)
          throws BadRequestException, IOException, InternalServerErrorException, ConflictException {
    log.info("method: updateCustomer({},{})" + data);
    //log.info("URLConnections.URL_CUSTOMER:" + URLConnections.URL_CUSTOMER);
    //Gson g = new Gson();
    //String json = g.toJson(data);
    //log.info("method: updateCustomer JSON:" + json);
    //CoreConnection.putRequest(URLConnections.URL_CUSTOMER, json);
    ApiGatewayService.get().updateCustomer(data);
    /*JSONObject j = new JSONObject();
    j.put("message","Cliente actualizado correctamente");
    return j;*/
  }

  @ApiMethod(name = "deleteCustomer", path = "/customerMonitorEndpoint/deleteCustomer", httpMethod = ApiMethod.HttpMethod.DELETE)
  public Response deleteCustomer(@Named("id") final String id)
          throws BadRequestException, IOException, InternalServerErrorException, ConflictException {
    log.info("method: deleteCustomer({},{})" + id);
    //log.info("URLConnections.URL_DELETE_CUSTOMER:" + URLConnections.URL_DELETE_CUSTOMER);
    //JSONObject json = new JSONObject();
    //log.warning("method: request: " + json.toJSONString());
    //return CoreConnection.postRequest(URLConnections.URL_DELETE_CUSTOMER+id, "", JSONObject.class);
    return ApiGatewayService.get().deleteLogicCustomer(new Long(id));
  }

  @ApiMethod(name = "inactiveCustomerLifeMile", path = "/customerMonitorEndpoint/inactiveCustomerLifeMile", httpMethod = ApiMethod.HttpMethod.PUT)
  public CustomerLifeMileJSON inactiveCustomerLifeMile(@RequestBody final CustomerLifeMile customerLifeMile)
          throws BadRequestException, IOException, InternalServerErrorException {
    log.warning("method: customerLifeMile: " + customerLifeMile.toString());

    //JSONObject customerLifeMileJson = new JSONObject();
    //customerLifeMileJson.put("customer", customerLifeMile.getIdCustomer());
    //return CoreConnection.postRequest(URLConnections.URL_CUSTOMER_LIFEMILE_INACTIVE, customerLifeMileJson.toJSONString(), CustomerLifeMileJSON.class);
    return ApiGatewayService.get().inactiveLifeMiles(new CustomerLifeMilesReq(new Long(customerLifeMile.getIdCustomer())));
  }

  @ApiMethod(name = "crearLifemile", path = "/customerMonitorEndpoint/crearLifemile", httpMethod = ApiMethod.HttpMethod.POST)
  public CustomerJSON crearLifemile(final CustomerLifeMile customerLifeMile)
          throws BadRequestException, IOException, InternalServerErrorException, ConflictException {
    log.warning("method: handlerCustomerLifeMile");

    //JSONObject customerLifeMileJson = new JSONObject();
    //customerLifeMileJson.put("customer", customerLifeMile.getIdCustomer());
    //customerLifeMileJson.put("lifeMilesNumber", customerLifeMile.getLifeMileNumber());

    //log.warning("method: crearLifemile: " + customerLifeMileJson.toJSONString());
    //log.warning("method: crearLifemile: Se conecta al CORE de  -> " + URLConnections.URL_CUSTOMER_LIFEMILE);
    //return CoreConnection.postRequest(URLConnections.URL_CUSTOMER_LIFEMILE, customerLifeMileJson.toJSONString(), CustomerJSON.class);
    return ApiGatewayService.get().handleCustomerLifeMiles(new CustomerLifeMilesReq(new Long(customerLifeMile.getIdCustomer()), customerLifeMile.getLifeMileNumber()));
  }

  /**
   * Get couries
   * @return json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getBlockedUsers", path = "/customerMonitorEndpoint/getBlockedUsers", httpMethod = ApiMethod.HttpMethod.GET)
  public List<BlockedUser> getBlockedUsers() throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    List<BlockedUser> blockedUser;
    blockedUser = ofy().load().type(BlockedUser.class).list();
    return blockedUser;
  }

  @ApiMethod(name = "deleteCouponByCustomerId", path = "/customerMonitorEndpoint/delete-coupon/{customer_id}", httpMethod = ApiMethod.HttpMethod.DELETE)
  public Answer deleteCouponByCustomerID(@Named("customer_id") int customerID) {

    try {
      Answer answer = new Answer();
      String message = "";
      answer = this.customerCouponManager.deleteCouponByCustomerID(customerID);
//      User user = users.findUserByIdCustomer(customerID);
//      if (Objects.nonNull(user)) {
//        Key<User> userKey = Key.create(User.class, user.getIdUser());
//        String messageUser = String.format("userKey -> %s ", userKey);
//        log.info(messageUser);
//
//        final List<CustomerCoupon> customerCoupons = getCustomerCoupons(userKey);
//        if (customerCoupons != null && !customerCoupons.isEmpty()) {
//          CustomerCoupon couponToDelete = getLastCoupon(customerCoupons);
//          message = String.format("Se elimina el cupon: %s, del customer %s ", couponToDelete.getCouponId(), String.valueOf(customerID));
//          deleteCustomerCoupon(couponToDelete);
//        }
//      }
//
//      answer.setConfirmation(true);
//      answer.setMessage(message);
     return answer;

    } catch(Exception e) {
      String message = "Error al intentar eliminar coupon de una orden cancelada " + e.getMessage();
      log.warning(message);
      Answer answer = new Answer();
      answer.setConfirmation(true);
      answer.setMessage(message);
      return answer;
    }
  }

  private CustomerCoupon getLastCoupon(List<CustomerCoupon> customerCoupons) {
    customerCoupons.sort(Comparator.comparing(a -> a.getUseTime()));
    final int positionLastCoupon = customerCoupons.size() - 1;
    return customerCoupons.get(positionLastCoupon);
  }
  private void deleteCustomerCoupon(CustomerCoupon customerCoupon) {
    ofy().delete().entity(customerCoupon).now();
  }

  private List<CustomerCoupon> getCustomerCoupons(Key<User> userKey) {
    return ofy().load().type(CustomerCoupon.class)
            .filter("customerKey", userKey)
            .orderKey(false)
            .list();
  }

  @ApiMethod(name = "isBlockedUserForId", path = "/customerMonitorEndpoint/isBlockedUserForId", httpMethod = ApiMethod.HttpMethod.GET)
  public Answer isBlockedUserForId(@Named("idCustomer") final Integer idCustomer) {
    try {
      final BlockedUser blockedUser = ofy().load().type(BlockedUser.class).filter("idUser", idCustomer).first().now();
      Answer answer = new Answer(blockedUser != null);
      answer.setMessage(blockedUser.getReasonBlock());
      return answer;
    } catch (Exception e) {
      return new Answer(false);
    }
  }

  @ApiMethod(name = "deleteLogicUser", path = "/customerMonitorEndpoint/deleteLogicUser", httpMethod = ApiMethod.HttpMethod.DELETE)
  public Answer deleteLogicUser(@Named("id") final String id) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    Answer answer = new Answer();
    answer.setConfirmation(false);

    User user = ofy().load().type(User.class).filter("id", Integer.parseInt(id)).first().now();

    if (user != null) {
      Key<User> userKey = Key.create(User.class, user.getIdUser());
      Credential credential = ofy().load().type(Credential.class).ancestor(userKey).first().now();
      if (credential != null) {
        ofy().delete().entity(credential).now();
      }
      ofy().delete().entity(user).now();

      answer.setMessage("Usuario eliminado correctamente");
      answer.setConfirmation(true);
    } else {
      answer.setMessage("El usuario no se puede eliminar");
    }

    return answer;
  }



}

