package com.imaginamos.farmatodo.backend.coupon.validate_coupon.infraestructure;

import com.algolia.search.exceptions.AlgoliaException;
import com.google.api.server.spi.config.*;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.ConflictException;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.datastore.Query;
import com.google.gson.Gson;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.imaginamos.farmatodo.backend.Prime.PrimeUtil;
import com.imaginamos.farmatodo.backend.coupon.CouponService;
import com.imaginamos.farmatodo.backend.coupon.validate_coupon.domain.OrderService;
import com.imaginamos.farmatodo.backend.user.Authenticate;
import com.imaginamos.farmatodo.backend.user.Users;
import com.imaginamos.farmatodo.model.algolia.CouponPopUpData;
import com.imaginamos.farmatodo.model.algolia.cuponFilters.*;
import com.imaginamos.farmatodo.model.coupon.*;
import com.imaginamos.farmatodo.model.customer.CustomerCallReq;
import com.imaginamos.farmatodo.model.customer.CustomerCallResponseData;
import com.imaginamos.farmatodo.model.customer.Customer;
import com.imaginamos.farmatodo.model.order.*;
import com.imaginamos.farmatodo.model.product.Item;
import com.imaginamos.farmatodo.model.product.ItemGroup;
import com.imaginamos.farmatodo.model.user.Token;
import com.imaginamos.farmatodo.model.user.User;
import com.imaginamos.farmatodo.model.util.*;
import com.imaginamos.farmatodo.networking.algolia.APIAlgolia;
import com.imaginamos.farmatodo.networking.cache.CachedDataManager;
import com.imaginamos.farmatodo.networking.services.ApiGatewayService;
import com.imaginamos.farmatodo.networking.talonone.TalonOneCouponValidator;
import com.imaginamos.farmatodo.networking.talonone.TalonOneService;
import org.apache.oltu.oauth2.as.issuer.MD5Generator;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuer;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.jasypt.util.password.StrongPasswordEncryptor;
import retrofit2.Response;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.imaginamos.farmatodo.backend.OfyService.ofy;

/**
 * Created by USUARIO on 10/07/2017.
 */

@Api(name = "couponEndpoint",
    version = "v1",
    apiKeyRequired = AnnotationBoolean.TRUE,
    scopes = {Constants.EMAIL_SCOPE},
    clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID_DEVELOPER, com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID},
    audiences = {Constants.ANDROID_AUDIENCE},
    namespace = @ApiNamespace(ownerDomain = Constants.OWNER_DOMAIN, ownerName = Constants.OWNER_NAME),
    description = "Stores, deletes, edits and queries for coupon")
public class CouponEndpoint {
  private static final Logger LOG = Logger.getLogger(Coupon.class.getName());
  private final Authenticate authenticate;
  private final Users users;
  private final CouponService couponService;
  private final FTDUtilities ftdUtilities;

  private final PrimeUtil primeUtil;

  public CouponEndpoint() {
    couponService = new CouponService();
    authenticate = new Authenticate();
    ftdUtilities = new FTDUtilities();
    users = new Users();
    primeUtil = new PrimeUtil();
  }


//  @ApiMethod(name = "coupons", path = "/couponEndpoint/v1/coupons", httpMethod = ApiMethod.HttpMethod.GET)
//  public GenericResponse<List<Coupon>> getCouponsInfo(
//          @Named("token") final String token,
//          @Named("tokenIdWebSafe") final String tokenIdWebSafe,
//          @Named("idCustomerWebSafe") final String idCustomerWebSafe,
//          @Nullable @Named("coupon") String couponName
//  ) throws ConflictException, BadRequestException {
//
//    if (token == null || tokenIdWebSafe == null
//            || idCustomerWebSafe == null || token.isEmpty()
//            || tokenIdWebSafe.isEmpty() || idCustomerWebSafe.isEmpty()) {
//      throw new ConflictException(Constants.INVALID_TOKEN);
//    }
//
//    if (!authenticate.isValidToken(token, tokenIdWebSafe))
//      throw new ConflictException(Constants.INVALID_TOKEN);
//
//    Key<User> userKey = Key.create(idCustomerWebSafe);
//    long currentDate = new Date().getTime();
//    Query.Filter filter = new Query.FilterPredicate("expirationDate",
//            Query.FilterOperator.GREATER_THAN_OR_EQUAL,
//            currentDate);
//
////    filter by name
//    if (couponName != null && !couponName.isEmpty()) {
//      couponName = couponName.toUpperCase().trim();
//      Query.Filter filterName = new Query.FilterPredicate("name", Query.FilterOperator.EQUAL, couponName);
//      filter = Query.CompositeFilterOperator.and(filterName, filter);
//    }
//
//    com.googlecode.objectify.cmd.Query<Coupon> queryCoupon = ofy().load().type(Coupon.class).filter(filter);
//
//    if (queryCoupon.count() == 0) {
//      throw new ConflictException(Constants.ERROR_COUPON_NOT_FOUND);
//    }
//
//    GenericResponse<List<Coupon>> response = new GenericResponse<>();
//
//    List<Coupon> couponList = queryCoupon.list();
//
//    if (couponList == null || couponList.isEmpty()) {
//      throw new ConflictException(Constants.ERROR_COUPON_NOT_FOUND);
//    }
//
//    couponList = couponService.setStatusCouponsForCustomer(userKey, couponList);
//
//    response.setMessage(Constants.SUCCESS);
//    response.setCode(Constants.CODE_SUCCESS);
//    response.setData(couponList);
//
//    return response;
//  }

  @ApiMethod(name = "coupons", path = "/couponEndpoint/v1/coupons", httpMethod = ApiMethod.HttpMethod.GET)
  public GenericResponse<List<Coupon>> getCouponsInfoV2(
          @Named("token") final String token,
          @Named("tokenIdWebSafe") final String tokenIdWebSafe,
          @Named("idCustomerWebSafe") final String idCustomerWebSafe,
          @Nullable @Named("coupon") String couponName
  ) throws ConflictException, BadRequestException {

    if (token == null || tokenIdWebSafe == null
            || idCustomerWebSafe == null || token.isEmpty()
            || tokenIdWebSafe.isEmpty() || idCustomerWebSafe.isEmpty()) {
      throw new ConflictException(Constants.INVALID_TOKEN);
    }

    if (!authenticate.isValidToken(token, tokenIdWebSafe))
      throw new ConflictException(Constants.INVALID_TOKEN);

    Key<Customer> customerDS = Key.create(idCustomerWebSafe);
    CustomerCoupon customerCoupon = obtainCustomerCoupon(customerDS);

    if(Objects.nonNull(customerCoupon) && Objects.nonNull(customerCoupon.getCouponId())){
//      LOG.info("Cupon: " + customerCoupon.getCouponId());
      final Coupon coupon = ofy().load().type(Coupon.class).ancestor(customerCoupon.getCouponId().getKey()).first().now();

      if (Objects.isNull(coupon)){
        return genericResponseEmpty();
      }

      GenericResponse<List<Coupon>> response = new GenericResponse<>();
      List<Coupon> couponList = new ArrayList<>();
      couponList.add(coupon);
      Key<User> userKey = Key.create(idCustomerWebSafe);
      couponList = couponService.setStatusCouponsForCustomer(userKey, couponList);
      response.setMessage(Constants.SUCCESS);
      response.setCode(Constants.CODE_SUCCESS);
      response.setData(couponList.stream().filter(Objects::nonNull).collect(Collectors.toList()));

      return response;
    }

    return genericResponseEmpty();
  }

  public GenericResponse<List<Coupon>> genericResponseEmpty(){
    GenericResponse<List<Coupon>> response = new GenericResponse<>();
    response.setMessage(Constants.SUCCESS);
    response.setCode(Constants.CODE_SUCCESS);
    response.setData(new ArrayList<>());
    return response;
  }


  /**
   * Creating Coupon. Insertion or association of a user to register through the platform, in their database.
   * In the process, a security token for Firebase is returned and a token for petitions through the platform.
   *
   * @param coupon Object of class 'Coupon' that contain data to store or associate of a new user (coupon).
   * @return answer object of class CustomerJSOn that contain the created customer information
   * @throws UnauthorizedException father class of all exceptions
   * @throws BadRequestException   father class of all exceptions
   */
  @ApiMethod(name = "createCoupon", path = "/couponEndpoint/createCoupon", httpMethod = ApiMethod.HttpMethod.POST)
  public Answer createCoupon(final Coupon coupon) throws UnauthorizedException, BadRequestException {
    //LOG.warning("method: createCoupon()");

    // Exceptions when has a wrong request
    if (coupon == null)
      throw new BadRequestException(Constants.BODY_NOT_INITIALIZED);

    if (coupon.getKeyClient() == null)
      throw new BadRequestException(Constants.ERROR_KEY_CLIENT);
    if (!coupon.getKeyClient().equals(Constants.KEY_SECURE_CLIENT))
      throw new UnauthorizedException(Constants.ERROR_ACCESS_DENIED);

    if (coupon.getCouponType() == null)
      throw new BadRequestException(Constants.ERROR_COUPON_TYPE);

    if (coupon.getOfferId() == null)
      throw new BadRequestException(Constants.ERROR_COUPON_OFFER);

    if (coupon.getName() == null)
      throw new BadRequestException(Constants.ERROR_COUPON_NAME_NULL);

    if (coupon.getFirstDescription() == null)
      throw new BadRequestException(Constants.ERROR_COUPON_DESCRIPTION_NULL);

    if (coupon.getStartsLater() == null)
      throw new BadRequestException(Constants.ERROR_COUPON_STARTS);
    if (coupon.getStartsLater() && coupon.getStartDate() == null)
      throw new BadRequestException(Constants.ERROR_COUPON_DATE_START);

    if (coupon.getExpires() == null)
      throw new BadRequestException(Constants.ERROR_COUPON_EXPIRES);
    if (coupon.getExpires() && coupon.getExpirationDate() == null)
      throw new BadRequestException(Constants.ERROR_COUPON_DATE_EXPIRES);
    if (coupon.getExpires() && coupon.getStartsLater() && coupon.getStartDate() > coupon.getExpirationDate())
      throw new BadRequestException(Constants.ERROR_COUPON_DATE_WRONG);

    if (coupon.getHasLimit() == null)
      throw new BadRequestException(Constants.ERROR_COUPON_LIMIT);
    if (coupon.getHasLimit() && coupon.getMaximumNumber() == null)
      throw new BadRequestException(Constants.ERROR_COUPON_MAX_NUMBER);

    Query.Filter filter = new Query.FilterPredicate("name",
            Query.FilterOperator.EQUAL, coupon.getName());
    com.googlecode.objectify.cmd.Query<Coupon> query = ofy().load().type(Coupon.class).filter(filter);

    if (query.count() != 0)
      throw new BadRequestException(Constants.ERROR_COUPON_NAME);
    coupon.setCouponId(UUID.randomUUID().toString());
    coupon.setCountUses((long) 0);
    Key<ItemGroup> itemGroupKey = Key.create(ItemGroup.class, "1");
    Item item = new Item();
    item.setItemId(UUID.randomUUID().toString());
    item.setItemGroupRef(Ref.create(itemGroupKey));
    item.setMediaDescription(coupon.getFirstDescription());
    if (coupon.getSecondDescription() != null)
      item.setGrayDescription(coupon.getSecondDescription());
    if (coupon.getPhotoUrl() != null)
      item.setMediaImageUrl(coupon.getPhotoUrl());
    item.setCoupon(true);
    coupon.setItemId(Key.create(itemGroupKey, Item.class, item.getItemId()));
    ofy().save().entities(coupon, item).now();
    Answer answer = new Answer();
    answer.setConfirmation(true);
    return answer;
  }

  /**
   * Validate Coupon. Insertion or association of a user to register through the platform, in their database.
   *
   * @param coupon Object of class 'Coupon' that contain data to store or associate of a new user (coupon).
   * @return answer object of class CustomerJSOn that contain the created customer information
   * @throws UnauthorizedException Exception supported from google code, to show an unauthorized access
   * @throws BadRequestException   Exception supported from google code to show the client an incorrect request
   * @throws ConflictException     Exception supported from google code to show the user a conflict.
   */
  @ApiMethod(name = "validateCoupon", path = "/couponEndpoint/validateCoupon", httpMethod = ApiMethod.HttpMethod.POST)
  public Answer validateCoupon(final HttpServletRequest req, final Coupon coupon) throws UnauthorizedException, BadRequestException, ConflictException, AlgoliaException, IOException {
    //LOG.warning("method: validateCoupon()");
    // Exceptions when has a wrong request
    if (coupon == null) {
      LOG.warning("Error: [" + Constants.BODY_NOT_INITIALIZED + "]");
      throw new BadRequestException(Constants.BODY_NOT_INITIALIZED);
    }
    if (!authenticate.isValidToken(coupon.getToken(), coupon.getTokenIdWebSafe())) {
      LOG.warning("Error: [" + Constants.INVALID_TOKEN + "]");
      throw new ConflictException(Constants.INVALID_TOKEN);
    }
    if (coupon.getIdCustomerWebSafe() == null) {
      LOG.warning("Error: [" + Constants.ERROR_COUPON_ID_CUSTOMER + "]");
      throw new BadRequestException(Constants.ERROR_COUPON_ID_CUSTOMER);
    }
    if (coupon.getName() == null) {
      LOG.warning("Error: [" + Constants.ERROR_COUPON_NAME_NULL + "]");
      throw new BadRequestException(Constants.ERROR_COUPON_NAME_NULL);
    }

    Key<User> userKey = Key.create(coupon.getIdCustomerWebSafe());
    Key<Customer> customerKey = Key.create(coupon.getIdCustomerWebSafe());
    User user = users.findUserByKey(userKey);

    OrderService orderService=new OrderService(new DataStoreOrderRepository());
    DeliveryOrder deliveryOrder =orderService.findActiveDeliveryOrderByidCustomerWebSafe(coupon.getIdCustomerWebSafe());
    List<DeliveryOrderItem> deliveryOrderItemList = new ArrayList<>();
    if(Objects.nonNull(deliveryOrder)){
      deliveryOrderItemList = orderService.findDeliveryOrderItemByDeliveryOrder(deliveryOrder);
      deliveryOrder.setItemList(deliveryOrderItemList);
    }

    boolean isPrime = false;
    try {
      isPrime = isOrderPrime(deliveryOrder);
    } catch (Exception e) {
      LOG.warning("Error: [" + Constants.ERROR_COUPON_VALIDATE + "]");
    }
    if (isPrime) {
      LOG.warning("Error: [" + Constants.ERROR_COUPON_FOR_PRIME + "]");
      throw new ConflictException(Constants.ERROR_COUPON_FOR_PRIME);
    }

    TalonOneCouponValidator talonOneCouponValidator = new TalonOneCouponValidator(new TalonOneService());
    String keyCache = coupon.getIdCustomerWebSafe() + Constants.KEY_COUPON_CACHE;
    String keyCache2 = user.getId() + Constants.KEY_COUPON_CACHE;
    LOG.info("keychache: "+keyCache+" - keychache2: "+keyCache2);
    Optional<String> couponInCache = CachedDataManager.getJsonFromCacheIndex(keyCache, Constants.INDEX_REDIS_FOURTEEN);
    if (couponInCache.isPresent()) {
      AnswerDeduct answerDeduct = new Gson().fromJson(couponInCache.get(), AnswerDeduct.class);
      CachedDataManager.deleteKeyIndex(keyCache, Constants.INDEX_REDIS_FOURTEEN);
      CachedDataManager.deleteKeyIndex(keyCache2, Constants.INDEX_REDIS_FOURTEEN);
      if (Objects.nonNull(answerDeduct.getTypeNotifcation()) && Objects.nonNull(answerDeduct.getNotificationMessage())) {
        throw new ConflictException(Constants.ALREADY_EXIST_COUPON);
      }
    } else {
      AnswerDeduct answerTalon = talonOneCouponValidator.validateCouponInTalonOne(user, coupon, deliveryOrder);
      if (answerTalon.hasError()) {

        Optional<Map<String, String>> rejectionReasonMessage = APIAlgolia.getRejectReasonCouponTalonOne();
        if(rejectionReasonMessage.isPresent()){
          if(Objects.nonNull(rejectionReasonMessage.get().get(answerTalon.getRejectionReason()))){
            LOG.warning("Cupon rechazado en Talon->" + new Gson().toJson(answerTalon));
            TalonOneService talonOneService = new TalonOneService();
            talonOneService.deleteCouponTalonOne(user.getId(), coupon.getIdCustomerWebSafe());
            throw new ConflictException(rejectionReasonMessage.get().get(answerTalon.getRejectionReason()));
          }
        }
      }
      if (answerTalon.isNotRejected()) {
        String bodyCache = new Gson().toJson(answerTalon);
        CachedDataManager.saveJsonInCacheIndexTime(keyCache, bodyCache, Constants.INDEX_REDIS_FOURTEEN, Constants.TIME_EXPIRE_IN_SECONDS);
        CachedDataManager.saveJsonInCacheIndexTime(keyCache2, bodyCache, Constants.INDEX_REDIS_FOURTEEN, Constants.TIME_EXPIRE_IN_SECONDS);
        return answerTalon;
      }
    }
    // validar source.

    // validate source name with properties algolia

    RequestSourceEnum sourceEnum = ftdUtilities.getSourceFromRequestHeaderForPays(req);
//    LOG.info("Source:{}" + sourceEnum.name());
    Optional<CouponFiltersConfig> couponsFilter = APIAlgolia.getCouponFilterConfig();

    if (!couponsFilter.isPresent()) {
      LOG.warning("Error: [" + Constants.ERROR_COUPON_DATA_FILTER + "]");
      throw new BadRequestException(Constants.ERROR_COUPON_DATA_FILTER);
    }

    if (couponsFilter.get().getCampaigns() == null) {
      LOG.warning("Error: [" + Constants.ERROR_COUPON_DATA_FILTER + "]");
      throw new BadRequestException(Constants.ERROR_COUPON_DATA_FILTER);
    }
    for (Campaign campaign : couponsFilter.get().getCampaigns()) {
      if (verifyCampaingCoupon(campaign, coupon)) {
        if (!couponFilter(campaign, sourceEnum)) {
          LOG.warning("Error: [" + Constants.ERROR_COUPON_FILTER + "]");
          throw new ConflictException(Constants.ERROR_COUPON_FILTER);
        }
      }
    }
    if(allItemsAreMarketplace(deliveryOrder.getItemList())){
      LOG.warning("Error: [" + Constants.INFO_COUPON_NO_APPLY_MARKETPLACE + "]");
      throw new ConflictException(Constants.INFO_COUPON_NO_APPLY_MARKETPLACE);
    }

    //LOG.warning(coupon.getName().trim());
    // 1. validar q el cupón exista

    Query.Filter filterName = new Query.FilterPredicate("name", Query.FilterOperator.EQUAL, coupon.getName().toUpperCase().trim());
    com.googlecode.objectify.cmd.Query<Coupon> queryCoupon = ofy().load().type(Coupon.class).filter(filterName);

    if (queryCoupon.count() == 0) {
      LOG.warning("Error: [" + Constants.ERROR_COUPON_EXISTS + "]");
      throw new ConflictException(Constants.ERROR_COUPON_EXISTS);
    }
    // 2. obtengo la orden que se encuentra en el carrito para validar si ya tiene algún cupón
    /*Key<User> userKey = Key.create(coupon.getIdCustomerWebSafe());
    Key<Customer> customerKey = Key.create(coupon.getIdCustomerWebSafe());
    DeliveryOrder deliveryOrder = ofy().load().type(DeliveryOrder.class).filter("currentStatus", 1).ancestor(Ref.create(userKey)).first().now();

    User user = users.findUserByKey(userKey);*/

    if (deliveryOrder != null) {

      if (Objects.isNull(coupon.getReplace()) && Objects.isNull(coupon.getOldName())) {
        //List<DeliveryOrderItem> deliveryOrderItemList = ofy().load().type(DeliveryOrderItem.class).ancestor(deliveryOrder).list();

        for (DeliveryOrderItem deliveryOrderItem : deliveryOrderItemList) {
          //LOG.info("itemId: [" + deliveryOrderItem.getId() + "] is coupon: [" + deliveryOrderItem.getCoupon() + " ]");

          if (deliveryOrderItem.getCoupon() != null && deliveryOrderItem.getCoupon()) {
            LOG.warning("Error: [" + Constants.ERROR_COUPON_VALIDATE + "]");
            throw new ConflictException(Constants.ERROR_COUPON_VALIDATE);
          }
        }

      } else {
        if (coupon.getReplace() && Objects.nonNull(coupon.getOldName())) {
//          LOG.info("Cupon anterior : " + coupon.getOldName() + " cupon nuevo: " + coupon.getName());

          Key<DeliveryOrder> deliveryOrderKey = Key.create(userKey, DeliveryOrder.class, deliveryOrder.getIdDeliveryOrder());
          List<DeliveryOrderItem> deliveryOrderItems = ofy().load().type(DeliveryOrderItem.class).ancestor(Ref.create(deliveryOrderKey)).list();

          deliveryOrderItems = deliveryOrderItems.stream()
                  .filter(item -> (Objects.nonNull(item.getCoupon()) && item.getCoupon()))
                  .collect(Collectors.toList());
          deleteCoupon(customerKey);
          ofy().delete().entities(deliveryOrderItems).now();
//          LOG.info("Cupon anterior eliminado.");
        }
      }
    }



    // 3. validar el cupón
    //LOG.warning("Milis" + new Date().getTime());
    Query.Filter filterExpires = new Query.FilterPredicate("expires", Query.FilterOperator.EQUAL, true);
    Query.Filter filterCouponExpires = Query.CompositeFilterOperator.and(filterName, filterExpires);
    queryCoupon = ofy().load().type(Coupon.class).filter(filterCouponExpires);
    if (queryCoupon.count() != 0) {
      Query.Filter filterDate = new Query.FilterPredicate("expirationDate", Query.FilterOperator.GREATER_THAN_OR_EQUAL, new Date().getTime());
      Query.Filter filter = Query.CompositeFilterOperator.and(filterName, filterDate);
      queryCoupon = ofy().load().type(Coupon.class).filter(filter);
      if (queryCoupon.count() == 0) {
        LOG.warning("Error: [" + Constants.ERROR_COUPON_EXPIRED + "]");
        throw new ConflictException(Constants.ERROR_COUPON_EXPIRED);
      }
    } else {
      queryCoupon = ofy().load().type(Coupon.class).filter(filterName);
    }
    Coupon couponSaved = queryCoupon.first().now();
    if (couponSaved.getStartsLater()) {
      if (couponSaved.getStartDate() > new Date().getTime()) {
        LOG.warning("Error: [" + Constants.ERROR_COUPON_STARTED + "]");
        throw new ConflictException(Constants.ERROR_COUPON_STARTED);
      }
    }
    com.googlecode.objectify.cmd.Query<CustomerCoupon> queryCustomer = ofy().load().type(CustomerCoupon.class).ancestor(couponSaved).filter("customerKey", userKey);
    if (queryCustomer.count() != 0) {
      LOG.warning("Error: [" + Constants.ERROR_COUPON_CLAIMED + "]");
      throw new ConflictException(Constants.ERROR_COUPON_CLAIMED);
    }
    if (couponSaved.getHasLimit()) {
      if (couponSaved.getCountUses() >= couponSaved.getMaximumNumber()) {
        LOG.warning("Error: [" + Constants.ERROR_COUPON_SOLD + "]");
        throw new ConflictException(Constants.ERROR_COUPON_SOLD);
      }
    }
    //validateType(couponSaved.getCouponType(), customerKey, deliveryOrder);
    validateTypeV2(couponSaved.getCouponType(), user);
    // asociar el cupon al cliente
    return ofy().transact(() ->
    {
      String message;
      if (couponSaved.getCouponType() != null) {
        Key<Item> itemKey = couponSaved.getItemId();
        try {
          this.addDiscount(itemKey, customerKey);
        } catch (ConflictException e) {
          LOG.warning(e.getMessage());
          e.printStackTrace();
        }
      }
      //message = Constants.COUPON_VALIDATED;
      CustomerCoupon customerCoupon = new CustomerCoupon();
      customerCoupon.setCustomerCouponId(UUID.randomUUID().toString());
      customerCoupon.setCouponId(Ref.create(Key.create(Coupon.class, couponSaved.getCouponId())));
      customerCoupon.setCustomerKey(userKey);
      customerCoupon.setUseTime(new Date().getTime());
      couponSaved.setCountUses(couponSaved.getCountUses() + 1);
      ofy().save().entities(couponSaved, customerCoupon).now();
      AnswerDeduct answer = new AnswerDeduct();
      answer.setConfirmation(true);
      answer.setMessage("Cupón "+coupon.getName()+" "+couponSaved.getDiscountValue());
      answer.setDiscount(Double.valueOf(couponSaved.getDiscountValue()));
      answer.setNameCoupon(coupon.getName());
      answer.setTypeNotifcation("Info");
      answer.setNotificationMessage("Para hacer uso de este cupón " +coupon.getName()+" la compra mínima es de $"+couponSaved.getRestrictionValue());
      answer.setRestrictionValue(Double.valueOf(couponSaved.getRestrictionValue()));
      //LOG.info("method: validateCoupon(); response -> confirmation: [" + answer.isConfirmation() + "], message: [" + answer.getMessage() + "]");
      String bodyCache=new Gson().toJson(answer);
      CachedDataManager.saveJsonInCacheIndexTime(keyCache,bodyCache , Constants.INDEX_REDIS_FOURTEEN, Constants.TIME_EXPIRE_IN_SECONDS);
      Optional<CouponPopUpData> optionalCoupon = APIAlgolia.getCouponPopUp(coupon.getName());
      if (optionalCoupon.isPresent()) {
        //LOG.info("Se agrega data para popUp:");
        answer.setCouponPopUp(optionalCoupon.get());
      } else {
        LOG.info("no se envia popUp ");
      }
      return answer;
    });

  }

  private boolean isOrderPrime(DeliveryOrder deliveryOrder) {
    boolean response = Boolean.FALSE;

      if (deliveryOrder == null) {
        LOG.info("method: isOrderPrime(); deliveryOrder is null");
        return false;
      }
      boolean isScanAndGo = isOrdenScanAndGo(deliveryOrder);
      List<DeliveryOrderItem> deliveryOrderItemList = ofy().load().type(DeliveryOrderItem.class).ancestor(deliveryOrder).list();

      if (deliveryOrderItemList == null) {
        LOG.info("method: isOrderPrime(); deliveryOrderItemList is null");
        return false;
      }

      // validar los tips
      List<DeliveryOrderItem> itemsScanAndGo = new ArrayList<>(deliveryOrderItemList);
      List<DeliveryOrderItem> itemsNewOrder = new ArrayList<>();

      if (isScanAndGo) {
        //set store princial when is scan and go
        itemsNewOrder = Objects.nonNull(itemsScanAndGo) ? itemsScanAndGo.stream().filter(it -> (Objects.isNull(it.getScanAndGo()) || !it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon())).collect(Collectors.toList()) : null;
        if (Objects.nonNull(itemsScanAndGo) && itemsScanAndGo.stream()
                .filter(it -> (Objects.isNull(it.getScanAndGo()) || !it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon())).findFirst().isPresent()) {
          itemsScanAndGo.removeIf(it -> ((Objects.isNull(it.getScanAndGo()) || !it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon())));
        }
      } else {
        itemsNewOrder = Objects.nonNull(itemsScanAndGo) ? itemsScanAndGo.stream().filter(it -> (Objects.nonNull(it.getScanAndGo()) && it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon())).collect(Collectors.toList()) : null;
        if (Objects.nonNull(itemsScanAndGo) && itemsScanAndGo.stream()
                .filter(it -> ((Objects.nonNull(it.getScanAndGo()) && it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon()))).findFirst().isPresent()) {
          itemsScanAndGo.removeIf(it -> ((Objects.nonNull(it.getScanAndGo()) && it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon())));
        }
      }

      deliveryOrderItemList = itemsScanAndGo;

      if (deliveryOrderItemList == null) {
        return false;
      }

      for (DeliveryOrderItem deliveryOrderItem : deliveryOrderItemList) {
        if (deliveryOrderItem == null) {
          LOG.info("method: isOrderPrime(); deliveryOrderItem is null");
          return false;
        }
        if (primeUtil.isItemPrime(deliveryOrderItem.getId())) {
          response = true;
        }
      }

    return response;
  }

  /**
   * @param request
   * @return
   * @throws UnauthorizedException
   * @throws BadRequestException
   * @throws ConflictException
   * @throws AlgoliaException
   */
  @ApiMethod(name = "validateCoupon", path = "/couponEndpoint/validateCouponStore", httpMethod = ApiMethod.HttpMethod.POST)
  public Answer validateCouponStore(final CustomerCouponStoreReq request) throws BadRequestException, ConflictException, IOException {
    //LOG.warning("method: validateCoupon()");

    Coupon coupon = new Coupon();

    if (request.getDocumentNumber() == null) {
      LOG.warning("Error: [" + Constants.ERROR_COUPON_DOCUMENT_NUMBER_NULL + "]");
      throw new BadRequestException(Constants.ERROR_COUPON_DOCUMENT_NUMBER_NULL);
    }

    if (request.getCoupon() == null) {
      LOG.warning("Error: [" + Constants.ERROR_COUPON_NAME_NULL + "]");
      throw new BadRequestException(Constants.ERROR_COUPON_NAME_NULL);
    }

    // 0. Validar que el customer exista.
    //LOG.warning("Customer -> documentNumber { " + request.getDocumentNumber() + " }");
    CustomerCallResponseData customer = null;

    //try {
    CustomerCallReq customerCallReq = new CustomerCallReq();
    customerCallReq.setDocumentNumber(request.getDocumentNumber());

    try {

      customer = ApiGatewayService.get().getCustomerCallCenter(customerCallReq).get(0);

    } catch (Exception e) {
      throw new BadRequestException(Constants.ERROR_COUPON_DOCUMENT_NUMBER_NOT_EXIST);
    }


    if (customer == null)
      throw new BadRequestException(Constants.ERROR_COUPON_DOCUMENT_NUMBER_NOT_EXIST);

//    LOG.info("Customer.email: " + customer.getEmail());
//    LOG.info("Customer.id: " + customer.getId().intValue());
    // Credential credential = users.findUserByEmail(customer.getEmail().toLowerCase());
    User user = users.findUserByIdCustomer(customer.getId().intValue());
    if (user != null) {
      Token tokenTransport = generateToken();

      Token tokenClient = new Token();
      tokenClient.setRefreshToken(tokenTransport.getRefreshToken());
      tokenClient.setToken(tokenTransport.getToken());
      encryptToken(tokenClient);
      tokenClient.setTokenId(UUID.randomUUID().toString());
      // tokenClient.setOwner(Ref.create(credential.getOwner().getKey()));
      tokenClient.setTokenExpDate(tokenTransport.getTokenExp());

      Key<Token> keyToken = ofy().save().entity(tokenClient).now();
      // user.setIdUserWebSafe(credential.getOwner().getKey().toWebSafeString());
      user.setIdUserWebSafe(Key.create(User.class, user.getIdUser()).toWebSafeString());
      tokenTransport.setTokenIdWebSafe(keyToken.toWebSafeString());
      user.setToken(tokenTransport);

      user.setLastLogin(new Date().getTime());
      // credential.setLastLogin(new Date());
      ofy().save().entities(user);
      final String idCustomerWebSafe = user.getIdUserWebSafe();

      coupon.setName(request.getCoupon());
      coupon.setIdCustomerWebSafe(idCustomerWebSafe);
      coupon.setToken(tokenTransport.getToken());
      coupon.setTokenIdWebSafe(tokenTransport.getTokenIdWebSafe());
    }
/*
    } catch (Exception e) {
      throw new BadRequestException(Constants.ERROR_COUPON_SEARCH_CUSTOMER);
    }*/

    // Exceptions when has a wrong request
    if (coupon == null) {
      LOG.warning("Error: [" + Constants.BODY_NOT_INITIALIZED + "]");
      throw new BadRequestException(Constants.BODY_NOT_INITIALIZED);
    }

    if (!authenticate.isValidToken(coupon.getToken(), coupon.getTokenIdWebSafe())) {
      LOG.warning("Error: [" + Constants.INVALID_TOKEN + "]");
      throw new ConflictException(Constants.INVALID_TOKEN);
    }
    if (coupon.getIdCustomerWebSafe() == null) {
      LOG.warning("Error: [" + Constants.ERROR_COUPON_ID_CUSTOMER + "]");
      throw new BadRequestException(Constants.ERROR_COUPON_ID_CUSTOMER);
    }

    //LOG.warning(request.getCoupon());
    // 1. validar q el cupón exista
    Query.Filter filterName = new Query.FilterPredicate("name", Query.FilterOperator.EQUAL, coupon.getName().toUpperCase().trim());
    com.googlecode.objectify.cmd.Query<Coupon> queryCoupon = ofy().load().type(Coupon.class).filter(filterName);
    if (queryCoupon.count() == 0) {
      LOG.warning("Error: [" + Constants.ERROR_COUPON_EXISTS + "]");
      throw new ConflictException(Constants.ERROR_COUPON_EXISTS);
    }
    // 2. obtengo la orden que se encuentra en el carrito para validar si ya tiene algún cupón
    Key<User> userKey = Key.create(coupon.getIdCustomerWebSafe());
    DeliveryOrder deliveryOrder = ofy().load().type(DeliveryOrder.class).filter("currentStatus", 1).ancestor(Ref.create(userKey)).first().now();
    if (deliveryOrder != null) {
      List<DeliveryOrderItem> deliveryOrderItemList = ofy().load().type(DeliveryOrderItem.class).ancestor(deliveryOrder).list();
      for (DeliveryOrderItem deliveryOrderItem : deliveryOrderItemList) {
        LOG.info("itemId: [" + deliveryOrderItem.getId() + "] is coupon: [" + deliveryOrderItem.getCoupon() + "]");

        if (deliveryOrderItem.getCoupon() != null && deliveryOrderItem.getCoupon()) {
          LOG.warning("Error: [" + Constants.ERROR_COUPON_VALIDATE + "]");
          throw new ConflictException(Constants.ERROR_COUPON_VALIDATE);
        }
      }
    }
    // 3. validar el cupón
    //LOG.warning("Milis" + new Date().getTime());
    Query.Filter filterExpires = new Query.FilterPredicate("expires", Query.FilterOperator.EQUAL, true);
    Query.Filter filterCouponExpires = Query.CompositeFilterOperator.and(filterName, filterExpires);
    queryCoupon = ofy().load().type(Coupon.class).filter(filterCouponExpires);
    if (queryCoupon.count() != 0) {
      Query.Filter filterDate = new Query.FilterPredicate("expirationDate", Query.FilterOperator.GREATER_THAN_OR_EQUAL, new Date().getTime());
      Query.Filter filter = Query.CompositeFilterOperator.and(filterName, filterDate);
      queryCoupon = ofy().load().type(Coupon.class).filter(filter);
      if (queryCoupon.count() == 0) {
        LOG.warning("Error: [" + Constants.ERROR_COUPON_EXPIRED + "]");
        throw new ConflictException(Constants.ERROR_COUPON_EXPIRED);
      }
    } else {
      queryCoupon = ofy().load().type(Coupon.class).filter(filterName);
    }
    Coupon couponSaved = queryCoupon.first().now();
    if (couponSaved.getStartsLater()) {
      if (couponSaved.getStartDate() > new Date().getTime()) {
        LOG.warning("Error: [" + Constants.ERROR_COUPON_STARTED + "]");
        throw new ConflictException(Constants.ERROR_COUPON_STARTED);
      }
    }
    Key<Customer> customerKey = Key.create(coupon.getIdCustomerWebSafe());
    com.googlecode.objectify.cmd.Query<CustomerCoupon> queryCustomer = ofy().load().type(CustomerCoupon.class).ancestor(couponSaved).filter("customerKey", userKey);
    if (queryCustomer.count() != 0) {
      LOG.warning("Error: [" + Constants.ERROR_COUPON_CLAIMED + "]");
      throw new ConflictException(Constants.ERROR_COUPON_CLAIMED);
    }
    if (couponSaved.getHasLimit()) {
      if (couponSaved.getCountUses() >= couponSaved.getMaximumNumber()) {
        LOG.warning("Error: [" + Constants.ERROR_COUPON_SOLD + "]");
        throw new ConflictException(Constants.ERROR_COUPON_SOLD);
      }
    }
    validateTypeV2(couponSaved.getCouponType(), user);
    // asociar el cupon al cliente
    return ofy().transact(() ->
    {
      String message;
      /*if (couponSaved.getCouponType() != null) {
        Key<Item> itemKey = couponSaved.getItemId();
        try {
          this.addDiscount(itemKey, customerKey);
        } catch (ConflictException e) {
          LOG.warning(e.getMessage());
          e.printStackTrace();
        }
      }*/
      message = Constants.COUPON_VALIDATED;
      CustomerCoupon customerCoupon = new CustomerCoupon();
      customerCoupon.setCustomerCouponId(UUID.randomUUID().toString());
      customerCoupon.setCouponId(Ref.create(Key.create(Coupon.class, couponSaved.getCouponId())));
      customerCoupon.setCustomerKey(userKey);
      customerCoupon.setUseTime(new Date().getTime());
      couponSaved.setCountUses(couponSaved.getCountUses() + 1);
      ofy().save().entities(couponSaved, customerCoupon).now();
      Answer answer = new Answer();
      answer.setConfirmation(true);
      answer.setMessage(message);
      LOG.info("method: validateCoupon(); response -> confirmation: [" + answer.isConfirmation() + "], message: [" + answer.getMessage() + "]");
//      LOG.info("Nombre del cupon " + coupon.getName());
      Optional<CouponPopUpData> optionalCoupon = APIAlgolia.getCouponPopUp(coupon.getName());
      if (optionalCoupon.isPresent()) {
//        LOG.info("Se agrega data para popUp:");
        answer.setCouponPopUp(optionalCoupon.get());
      } else {
        LOG.info("no se envia popUp ");
      }
      return answer;
    });

  }

  @ApiMethod(name = "deleteCouponStore", path = "/couponEndpoint/deleteCouponStore", httpMethod = ApiMethod.HttpMethod.POST)
  public Answer deleteCouponStore(final CustomerCouponStoreReq request) throws BadRequestException, ConflictException {
    Answer answer = new Answer();
    String message;
    //LOG.warning("method: deleteCouponStore()");

    if (request.getDocumentNumber() == null) {
      LOG.warning("Error: [" + Constants.ERROR_COUPON_DOCUMENT_NUMBER_NULL + "]");
      throw new BadRequestException(Constants.ERROR_COUPON_DOCUMENT_NUMBER_NULL);
    }

    if (request.getCoupon() == null) {
      LOG.warning("Error: [" + Constants.ERROR_COUPON_NAME_NULL + "]");
      throw new BadRequestException(Constants.ERROR_COUPON_NAME_NULL);
    }

    //LOG.warning("Customer -> documentNumber { " + request.getDocumentNumber() + " }");
    CustomerCallResponseData customer = null;

    CustomerCallReq customerCallReq = new CustomerCallReq();
    customerCallReq.setDocumentNumber(request.getDocumentNumber());

    try {
      customer = ApiGatewayService.get().getCustomerCallCenter(customerCallReq).get(0);
    } catch (Exception e) {
      throw new BadRequestException(Constants.ERROR_COUPON_DOCUMENT_NUMBER_NOT_EXIST);
    }

    //LOG.warning(request.getCoupon());
    // 1. validar q el cupón exista
    Query.Filter filterName = new Query.FilterPredicate("name", Query.FilterOperator.EQUAL, request.getCoupon().toUpperCase().trim());
    com.googlecode.objectify.cmd.Query<Coupon> queryCoupon = ofy().load().type(Coupon.class).filter(filterName);
    if (queryCoupon.count() == 0) {
      LOG.warning("Error: [" + Constants.ERROR_COUPON_EXISTS + "]");
      throw new ConflictException(Constants.ERROR_COUPON_EXISTS);
    }

    if (customer == null)
      throw new BadRequestException(Constants.ERROR_COUPON_DOCUMENT_NUMBER_NOT_EXIST);

//    LOG.info("Customer.email: " + customer.getEmail());
//    LOG.info("Customer.id: " + customer.getId().intValue());
    User user = users.findUserByIdCustomer(customer.getId().intValue());

    if (user != null) {

      final String idCustomerWebSafe = Key.create(User.class, user.getIdUser()).toWebSafeString();
//      LOG.info("idCustomerWebSafe = " + idCustomerWebSafe);
      Key<Customer> customerKey = Key.create(idCustomerWebSafe);
      final Boolean deleted = deleteCoupon(customerKey);
      if (deleted) {
        message = Constants.COUPON_DELETE;
        answer.setConfirmation(true);
        answer.setMessage(message);
        return answer;
      } else {
//        message = Constants.COUPON_NOT_DELETE;
//        answer.setMessage(message);
//        answer.setConfirmation(false);
        throw new ConflictException(Constants.COUPON_NOT_DELETE);
      }
    }

//    message = Constants.COUPON_NOT_DELETE;
//    answer.setMessage(message);
//    answer.setConfirmation(false);
//    return answer;

    throw new ConflictException(Constants.COUPON_NOT_DELETE);
  }


  /**
   * Validate Coupon. Insertion or association of a user to register through the platform, in their database.
   *
   * @param coupon Object of class 'Coupon' that contain data to store or associate of a new user (coupon).
   * @return answer object of class CustomerJSOn that contain the created customer information
   * @throws UnauthorizedException Exception supported from google code, to show an unauthorized access
   * @throws BadRequestException   Exception supported from google code to show the client an incorrect request
   * @throws ConflictException     Exception supported from google code to show the user a conflict.
   */
  @ApiMethod(name = "countCouponByTime", path = "/couponEndpoint/countCouponByTime", httpMethod = ApiMethod.HttpMethod.POST)
  public Answer countCouponByTime(final Coupon coupon) throws UnauthorizedException, BadRequestException, ConflictException {

    // Exceptions when has a wrong request
    if (coupon == null)
      throw new BadRequestException(Constants.BODY_NOT_INITIALIZED);

    if (coupon.getKeyClient() == null)
      throw new BadRequestException(Constants.ERROR_KEY_CLIENT);
    if (!coupon.getKeyClient().equals(Constants.KEY_SECURE_CLIENT))
      throw new UnauthorizedException(Constants.ERROR_ACCESS_DENIED);

    if (coupon.getName() == null)
      throw new BadRequestException(Constants.ERROR_COUPON_NAME_NULL);

    if (coupon.getStartTime() == null)
      throw new BadRequestException(Constants.ERROR_COUPON_START_DATE);
    if (coupon.getEndTime() == null)
      throw new BadRequestException(Constants.ERROR_COUPON_END_DATE);

    ;

    Query.Filter filterName = new Query.FilterPredicate("name",
            Query.FilterOperator.EQUAL, coupon.getName());
    com.googlecode.objectify.cmd.Query<Coupon> queryCoupon = ofy().load().type(Coupon.class).filter(filterName);
    if (queryCoupon.count() == 0)
      throw new ConflictException(Constants.ERROR_COUPON_EXISTS);

    Coupon couponSaved = queryCoupon.first().now();
    long timeStampStart = coupon.getStartTime().getTime();
    long timeStampEnd = coupon.getEndTime().getTime();

    Query.Filter filterTimeStart = new Query.FilterPredicate("useTime",
            Query.FilterOperator.GREATER_THAN_OR_EQUAL, timeStampStart);

    Query.Filter filterTimeEnd = new Query.FilterPredicate("useTime",
            Query.FilterOperator.LESS_THAN_OR_EQUAL, timeStampEnd);
    Query.Filter filterTimes = Query.CompositeFilterOperator.and(filterTimeStart, filterTimeEnd);

    com.googlecode.objectify.cmd.Query<CustomerCoupon> queryCustomer = ofy().load().type(CustomerCoupon.class).ancestor(couponSaved).filter(filterTimes);

    Answer answer = new Answer();
    answer.setConfirmation(true);
    answer.setCount((long) queryCustomer.count());
    return answer;
  }

  private void addDiscount(Key<Item> itemKey, Key<Customer> customerKey) throws ConflictException {
    DeliveryOrder deliveryOrder = ofy().load().type(DeliveryOrder.class).filter("currentStatus", 1).ancestor(Ref.create(customerKey)).first().now();
    if (deliveryOrder == null) {
      deliveryOrder = new DeliveryOrder();
      deliveryOrder.setIdDeliveryOrder(UUID.randomUUID().toString());
      deliveryOrder.setIdCustomer(Ref.create(customerKey));
      deliveryOrder.setCurrentStatus(1);
      deliveryOrder.setCreateDate(new Date());
      //LOG.info("save deliveryOrder"+new Gson().toJson(deliveryOrder));
      ofy().save().entity(deliveryOrder);
    }
    Item item = ofy().load().key(itemKey).now();
    if (item == null)
      throw new ConflictException(Constants.PRODUCT_NOT_EXISTS);
    final Key<DeliveryOrder> deliveryOrderKey = Key.create(customerKey, DeliveryOrder.class, deliveryOrder.getIdDeliveryOrder());
    DeliveryOrderItem deliveryOrderItem = new DeliveryOrderItem();
    deliveryOrderItem.setIdDeliveryOrderItem(UUID.randomUUID().toString());
    deliveryOrderItem.setIdDeliveryOrder(Ref.create(deliveryOrderKey));
    deliveryOrderItem.setIdItem(itemKey);
    deliveryOrderItem.setQuantitySold(1);
    deliveryOrderItem.setCreateDate(new Date());
    deliveryOrderItem.setFullPrice(0D);
    deliveryOrderItem.setOfferPrice(0D);
    deliveryOrderItem.setTotalStock(1);
    deliveryOrderItem.setChangeQuantity(false);
    deliveryOrderItem.setCoupon(true);
    deliveryOrderItem.setMediaDescription(item.getMediaDescription());
    deliveryOrderItem.setGrayDescription(item.getGrayDescription());
    deliveryOrderItem.setMediaImageUrl(item.getMediaImageUrl());
    //LOG.info("save deliveryOrderItem"+new Gson().toJson(deliveryOrderItem));
    ofy().save().entity(deliveryOrderItem);
  }

  private void validateType(Coupon.CouponType couponType, Key<Customer> customerKey) throws ConflictException {
    switch (couponType) {
      case FIRSTPURCHASE:
        if (ofy().load().type(DeliveryOrder.class).ancestor(customerKey).filter("currentStatus", 0).count() >= 1) {
          throw new ConflictException(Constants.COUPON_FIRST_PURCHASE);
        }
        break;
    }
  }

  private void validateTypeV2(Coupon.CouponType couponType, User user) throws IOException, BadRequestException, ConflictException {
    switch (couponType) {
      case FIRSTPURCHASE:
//        LOG.info("response " + user);
        ValidFirstCouponRes validFirstCouponRes = ApiGatewayService.get().validFirstCoupon((long) user.getId());
        if (!validFirstCouponRes.isValidFirstCoupon()) {
          throw new ConflictException(Constants.COUPON_FIRST_PURCHASE);
        }
        break;
    }
  }

  @ApiMethod(name = "getCouponByName", path = "/couponEndpoint/v1/coupon/detail", httpMethod = ApiMethod.HttpMethod.GET)
  public GetCouponByNameResponse getCouponByName(@Nullable @Named("couponName") final String couponName) {
    try {

      if (Objects.isNull(couponName) || couponName.isEmpty()) {
        return new GetCouponByNameResponse(false, "couponName is required", 400, null);
      }

      // Find in UpperCase
      Coupon coupon = ofy().load().type(Coupon.class).filter("name", couponName.toUpperCase()).first().now();
      if (Objects.nonNull(coupon)) {
        GetCouponByNameData data = new GetCouponByNameData(coupon.getCouponType().name(), coupon.getOfferId());
        return new GetCouponByNameResponse(true, "OK", 200, data);
      }

      // Find in LowerCase
      coupon = ofy().load().type(Coupon.class).filter("name", couponName.toLowerCase()).first().now();
      if (Objects.nonNull(coupon)) {
        GetCouponByNameData data = new GetCouponByNameData(coupon.getCouponType().name(), coupon.getOfferId());
        return new GetCouponByNameResponse(true, "OK", 200, data);
      }

      return new GetCouponByNameResponse(false, "No Content", 204, null);

    } catch (Exception e) {
      LOG.warning("Error in method getCouponByName(" + couponName + ") Message : " + e.getMessage());
      return new GetCouponByNameResponse(false, "Internal Server Error", 500, null);
    }
  }

  @ApiMethod(name = "returnCoupon", path = "/couponEndpoint/v1/coupon/returnCoupon", httpMethod = ApiMethod.HttpMethod.GET)
  public GenericResponse<String> returnCoupon(@Named("idOrder") final Long idOrder) {

//    LOG.info("request:" + idOrder);
    GenericResponse<String> response = new GenericResponse<>();
    response.setCode("200");

    DeliveryOrder deliveryOrder = ofy().load().type(DeliveryOrder.class).filter("idOrder", idOrder).first().now();
//    LOG.info("Order: " + deliveryOrder);
    if (deliveryOrder != null) {

      List<DeliveryOrderItem> deliveryOrderItemList = ofy().load().type(DeliveryOrderItem.class).ancestor(deliveryOrder).list();

      for (DeliveryOrderItem deliveryOrderItem : deliveryOrderItemList) {

        if (deliveryOrderItem.getCoupon() != null && deliveryOrderItem.getCoupon()) {

          Coupon coupon = ofy().load().type(Coupon.class).filter("itemId", deliveryOrderItem.getIdItem()).first().now();
          Key<Customer> customerKey = deliveryOrder.getIdCustomer().key();

          if (coupon != null && customerKey != null) {
            CustomerCoupon customerCoupon = ofy().load().type(CustomerCoupon.class).ancestor(coupon).filter("customerKey", customerKey).first().now();
            if (customerCoupon != null) {
              response.setMessage("CustomerCoupon encontrado");
              response.setData("Id: " + customerCoupon.getCouponId() + " UseTime:" + customerCoupon.getUseTime());
              ofy().delete().entities(customerCoupon);
            } else {
              response.setMessage("CustomerCoupon no encontrado");
            }
          } else {
            response.setMessage("Coupon no encontrado");
          }
        }
      }
    } else {
      response.setMessage("No se encontro la orden");
    }
    return response;
  }

  private Token generateToken() {
    Token tokenClient = new Token();
    OAuthIssuer oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator()); //investigar encriptacion
    try {
      tokenClient.setToken(oauthIssuerImpl.accessToken());
      tokenClient.setRefreshToken(oauthIssuerImpl.refreshToken());
      tokenClient.setTokenExp(7);
    } catch (OAuthSystemException e) {
      LOG.log(Level.SEVERE, e.getMessage(), e);
    }
    return tokenClient;
  }

  private void encryptToken(Token tokenClient) {
    tokenClient.setRefreshToken(encrypt(tokenClient.getRefreshToken()));
    tokenClient.setToken(encrypt(tokenClient.getToken()));
  }

  private String encrypt(String password) {
    StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
    return passwordEncryptor.encryptPassword(password);
  }

  /**
   * Elimniar ultimo cupon usado por el cliente.
   */
  private boolean deleteCoupon(final Key<Customer> customerKey) {
    try {
//      LOG.info("deleteCoupon(" + customerKey.toString() + ")");
      final List<CustomerCoupon> customerCoupons = ofy().load().type(CustomerCoupon.class).filter("customerKey", customerKey).orderKey(false).list();
//      LOG.info("IF(customerCoupons!=null && !customerCoupons.isEmpty()) : [" + (customerCoupons != null && !customerCoupons.isEmpty()) + "]");
      if (customerCoupons != null && !customerCoupons.isEmpty()) {
        customerCoupons.sort(Comparator.comparing(a -> a.getUseTime()));
        final int positionLastCupon = customerCoupons.size() - 1;
        final CustomerCoupon couponToDelete = customerCoupons.get(positionLastCupon);
        if (couponToDelete != null) {
          //LOG.info("deleteCoupon cupon Eliminado(" + couponToDelete.getCustomerCouponId() + ")");
          ofy().delete().entity(couponToDelete).now();
          return true;
        }
        return false;
      }
      return false;
    } catch (Exception e) {
      LOG.warning("Error al eliminar cupon cuando el cliente elimina todo el carrito... Mensaje: " + e.getMessage());
      return false;
    }
  }

  private boolean verifyCampaingCoupon(Campaign campaign, Coupon couponRequest) {
    if (campaign == null) {
      LOG.info("Campaña no encontrada");
      return false;
    }
    if (campaign.getActive() == null) {
      LOG.info("No se encontro campana ");
      return false;
    }
    if (!campaign.getActive()) {
      LOG.info("No se encontro campana activa");
      return false;
    }
    if (campaign.getCoupons() == null) {
      LOG.info("No se encontro ningun cupon");
    }
    for (String coupon : campaign.getCoupons()) {
      if (coupon == null) {
        LOG.info("No se encontro ningun cupon");
        return false;
      }
      if (coupon.trim().equalsIgnoreCase(couponRequest.getName().trim())) {
        LOG.info("Cupon encontrado");
        return true;
      }
    }
    LOG.info("Cupon  no  encontrado");
    return false;
  }


  private boolean couponFilter(Campaign campaign, RequestSourceEnum requestSourceEnum) {
    if (campaign.getVariables() == null) {
      LOG.info("No se encontro plataformas para la campana");
    }
    for (Variables variable : campaign.getVariables()) {
      if (variable == null) {
        LOG.info("No se encontro plataformas para la campana");
        return false;
      }
      if (variable.getValues() == null) {
        LOG.info("No se encontro valores para la campana");
      }
      for (String value : variable.getValues()) {
        if (value == null) {
          LOG.info("No se encontro plataformas para la campana");
          return false;
        }
        if (value.trim().equals(requestSourceEnum.name().trim())) {
          LOG.info("Plataforma encontrada");
          return true;
        }
      }
    }
    return false;
  }

  private Boolean isOrdenScanAndGo(DeliveryOrder order) {
    return Objects.nonNull(order) && Objects.nonNull(order.getDeliveryType()) && isScanAndGo(order.getDeliveryType().getDeliveryType());
  }

  private Boolean isScanAndGo(final String deliveryType) {
//    LOG.info("method  isScanAndGo  deliveryType not null ->  " + Objects.nonNull(deliveryType));
    return Objects.nonNull(deliveryType) && !deliveryType.isEmpty() && DeliveryType.SCANANDGO.getDeliveryType().equals(deliveryType);
  }

  @ApiMethod(name = "couponFilterValidate", path = "/couponEndpoint/v1/couponFilterValidate", httpMethod = ApiMethod.HttpMethod.POST)
  public CouponFilterValidateResponse couponFilterValidate(
          @Named("idCustomerWebSafe") final String idCustomerWebSafe,
          @Named("token") final String token,
          @Named("tokenIdWebSafe") final String tokenIdWebSafe,
          @Named("deliveryType") String deliveryType,
          CouponFilterValidateRequest couponFilterValidateRequest,
          HttpServletRequest httpServletRequest) throws BadRequestException, ConflictException, IOException {
    CouponFilterValidateResponse response = new CouponFilterValidateResponse();
    response.setStatus(HttpStatusCode.OK.getCode());
    if (couponFilterValidateRequest == null) {
      throw new BadRequestException("No se encontro el request");
    }


    if (couponFilterValidateRequest == null) {
      throw new BadRequestException("No se encontro el request");
    }


    if (couponFilterValidateRequest.getCustomerId() == null) {
      throw new BadRequestException("No se encontro el customerId");
    }
    if (couponFilterValidateRequest.getPaymentMethodId() == null && couponFilterValidateRequest.getPaymentMethodId() == 0) {
      throw new BadRequestException("No se encontro el metodo de pago");
    }
    if (couponFilterValidateRequest.getPaymentMethodId() == 3 && couponFilterValidateRequest.getPaymentCardId() == null) {
      throw new BadRequestException("No se encontro el la tarjeta");
    }

    DeliveryType deliveryTypeEnum;
    try {
      deliveryTypeEnum = getDeliveryType(deliveryType);
    } catch (Exception e) {
      throw new BadRequestException("No se encontro el deliveryType");
    }
    if (deliveryTypeEnum == null) {
      throw new BadRequestException("No se encontro el deliveryType");
    }

    RequestSourceEnum sourceEnum = ftdUtilities.getSourceFromRequestHeaderForPays(httpServletRequest);

    Key<Customer> customerKey = Key.create(idCustomerWebSafe);
    DeliveryOrder deliveryOrderSavedShoppingCart = ofy()
            .load()
            .type(DeliveryOrder.class)
            .filter("currentStatus", 1)
            .ancestor(Ref.create(customerKey))
            .first()
            .now();


    if (deliveryOrderSavedShoppingCart == null) {
//      LOG.info("No existe el deliveryOrderSavedShoppingCart" + deliveryOrderSavedShoppingCart);
      return response;
    }
    boolean isScanAndGo = isOrdenScanAndGo(deliveryOrderSavedShoppingCart);
    //LOG.info("isScanAndGo en couponEndpoint -> " + isScanAndGo);
    List<DeliveryOrderItem> deliveryOrderItemList = deliveryOrderItemList(deliveryOrderSavedShoppingCart);


    List<DeliveryOrderItem> itemsScanAndGo = new ArrayList<>(deliveryOrderItemList);

    List<DeliveryOrderItem> itemsNewOrder = new ArrayList<>();
    if (isScanAndGo) {
      //set store princial when is scan and go
      itemsNewOrder = Objects.nonNull(itemsScanAndGo) ? itemsScanAndGo.stream().filter(it -> (Objects.isNull(it.getScanAndGo()) || !it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon())).collect(Collectors.toList()) : null;
      if (Objects.nonNull(itemsScanAndGo) && itemsScanAndGo.stream()
              .filter(it -> (Objects.isNull(it.getScanAndGo()) || !it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon())).findFirst().isPresent()) {
        itemsScanAndGo.removeIf(it -> ((Objects.isNull(it.getScanAndGo()) || !it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon())));
      }
    } else {
      itemsNewOrder = Objects.nonNull(itemsScanAndGo) ? itemsScanAndGo.stream().filter(it -> (Objects.nonNull(it.getScanAndGo()) && it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon())).collect(Collectors.toList()) : null;
      if (Objects.nonNull(itemsScanAndGo) && itemsScanAndGo.stream()
              .filter(it -> ((Objects.nonNull(it.getScanAndGo()) && it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon()))).findFirst().isPresent()) {
        itemsScanAndGo.removeIf(it -> ((Objects.nonNull(it.getScanAndGo()) && it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon())));
      }
    }

    List<DeliveryOrderProvider> deliveryOrderProvidersList = new ArrayList<>();
    if (!isScanAndGo) {
      // Providers
      deliveryOrderProvidersList = ofy().load().type(DeliveryOrderProvider.class).ancestor(deliveryOrderSavedShoppingCart).list();
      //LOG.warning("deliveryOrderProvidersList ->  " + (Objects.nonNull(deliveryOrderProvidersList) ? deliveryOrderProvidersList.size() : 0));
    }
    boolean isPrimeItem = false;
    for(DeliveryOrderItem item : itemsNewOrder) {
      if (primeUtil.isItemPrime(item.getId())) {
        isPrimeItem = true;
      }
    }
    // Fix elimina items que no corresponden al tipo de envio actual
    if (Objects.nonNull(itemsNewOrder) && !itemsNewOrder.isEmpty() && !isPrimeItem) {
      ofy().delete().entities(itemsNewOrder);
    }
    deliveryOrderItemList = itemsScanAndGo;
    //LOG.info("deliveryOrderItemList ->  " + deliveryOrderItemList.toString());

    boolean orderHasCoupon = false;
    try {
      if (Objects.nonNull(deliveryOrderItemList) && !deliveryOrderItemList.isEmpty()) {
        orderHasCoupon = deliveryOrderItemList.stream().anyMatch(it -> Objects.nonNull(it.getCoupon()) && it.getCoupon());
//        LOG.info("orderHasCoupon -> " + orderHasCoupon);
      }
    } catch (Exception e) {
      LOG.warning("error finding coupon ");
    }

    CustomerCoupon customerCoupon = obtainCustomerCoupon(customerKey);

    String couponName = null;
    if (customerCoupon != null && Objects.nonNull(customerCoupon.getCouponId())) {
      final Ref<Coupon> coupon = customerCoupon.getCouponId();
      if (coupon.get() != null && coupon.get().getName() != null) {
        couponName = coupon.get().getName();
      }
    }
    if (couponName != null && orderHasCoupon) {

      Optional<CouponFiltersConfig> couponsFilter = APIAlgolia.getCouponFilterConfig();
      if (!couponsFilter.isPresent()) {
        LOG.warning("Error: [" + Constants.ERROR_COUPON_DATA_FILTER + "]");
        throw new BadRequestException(Constants.ERROR_COUPON_DATA_FILTER);
      }
      if (couponsFilter.get().getCampaigns() == null) {
        LOG.warning("Error: [" + Constants.ERROR_COUPON_DATA_FILTER + "]");
        throw new BadRequestException(Constants.ERROR_COUPON_DATA_FILTER);
      }
      for (Campaign campaign : couponsFilter.get().getCampaigns()) {
        if (verifyCampaingCouponOrder(campaign, couponName)) {
          if (!couponFilterOrder(campaign, sourceEnum, deliveryOrderSavedShoppingCart, deliveryTypeEnum)) {
            return buildResponseCoupon(couponName, ErrorCouponMsg.ERROR_COUPON_SOURCE, couponsFilter.get(), couponFilterValidateRequest);
          }
          if (!validateIfCouponHasRestrictionBinDa(campaign, couponFilterValidateRequest) && isCustomerDataphone(couponFilterValidateRequest)) {
            return buildResponseCoupon(couponName, ErrorCouponMsg.ERROR_COUPON_CARD_TERMINAL, couponsFilter.get(), couponFilterValidateRequest);
          }
          if(!validatePayMethodCouponFilter(couponFilterValidateRequest.getPaymentMethodId(),campaign)){
            return buildResponseCoupon(couponName, ErrorCouponMsg.ERROR_COUPON_PAYMENT_METHOD, couponsFilter.get(), couponFilterValidateRequest);
          }
        }
      }
      if (iscustomerPaymentCard(couponFilterValidateRequest) && isCustomerPaymentCardId(couponFilterValidateRequest)) {
        GenericResponse validateCoupon = validateCouponOrder(couponFilterValidateRequest, couponName);
        if (validateCoupon != null && validateCoupon.getMessage() != null && validateCoupon.getMessage().equals(ErrorCouponMsg.ERROR_COUPON_FILTER_CARD_BIN.name())) {
          return buildResponseCoupon(couponName, ErrorCouponMsg.ERROR_COUPON_FILTER_CARD_BIN, couponsFilter.get(), couponFilterValidateRequest);
        }
      }

    }

    return response;
  }


  private boolean verifyCampaingCouponOrder(Campaign campaign, String couponRequest) {
//    LOG.info("verifyCampaingCoupon(" + couponRequest + ")");
    if (campaign == null) {
      LOG.info("Campaña no encontrada");
      return false;
    }
    if (campaign.getActive() == null) {
      LOG.info("No se encontro campana ");
      return false;
    }
    if (!campaign.getActive()) {
      LOG.info("No se encontro campana activa");
      return false;
    }
    if (campaign.getCoupons() == null) {
      LOG.info("No se encontro ningun cupon");
    }
    for (String coupon : campaign.getCoupons()) {
      if (coupon == null) {
        LOG.info("No se encontro ningun cupon");
        return false;
      }
      if (coupon.trim().equalsIgnoreCase(couponRequest.trim())) {
        LOG.info("Cupon encontrado");
        return true;
      }
    }
    LOG.info("Cupon  no  encontrado");
    return false;
  }

  private List<DeliveryOrderItem> deliveryOrderItemList(DeliveryOrder order) {
    return ofy().load().type(DeliveryOrderItem.class).ancestor(order).list();
  }

  private CustomerCoupon obtainCustomerCoupon(final Key<Customer> customerKey) {
    try {
//      LOG.info("obtainCustomerCoupon(" + customerKey.toString() + ")");
      final List<CustomerCoupon> customerCoupons = ofy().load().type(CustomerCoupon.class).filter("customerKey", customerKey).orderKey(false).list();
//      LOG.info("IF(customerCoupons!=null && !customerCoupons.isEmpty()) : [" + (customerCoupons != null && !customerCoupons.isEmpty()) + "]");
      if (customerCoupons != null && !customerCoupons.isEmpty()) {
        customerCoupons.sort(Comparator.comparing(a -> a.getUseTime()));
        final int positionLastCupon = customerCoupons.size() - 1;
        final CustomerCoupon couponToRedim = customerCoupons.get(positionLastCupon);
        if (couponToRedim != null) {
          LOG.info("obtainCustomerCoupon cupon encontrado(" + couponToRedim.getCustomerCouponId() + ")");
          return couponToRedim;
        }
        return null;
      }
      return null;
    } catch (Exception e) {
      LOG.warning("Error al obtener cupon cuando el cliente elimina todo el carrito... Mensaje: " + e.getMessage());
      return null;
    }

  }

  private boolean couponFilterOrder(Campaign campaign, RequestSourceEnum requestSourceEnum, DeliveryOrder order, DeliveryType deliveryTypeEnum) {
    boolean source = false;
    boolean deliveryType = false;

    if (campaign.getVariables() == null) {
      LOG.info("las variables de la campaña es nula");
      return false;
    }
    for (Variables variable : campaign.getVariables()) {
      if (variable == null) {
        LOG.info("variable es nula");
        return false;
      }
      if (variable.getValues() == null) {
        LOG.info("valores de la variable es nula");
        return false;
      }
      if (variable.getKey() == null) {
        LOG.info("key de la variable es nula");
        return false;
      }

      if (variable.getKey().trim().equalsIgnoreCase("SOURCE")) {
        source = couponForSource(variable, requestSourceEnum);
      }
      if (variable.getKey().trim().equalsIgnoreCase("DELIVERY_TYPE")) {
        deliveryType = couponsForDt(variable, deliveryTypeEnum);
      }
    }
    return source && deliveryType;

  }

  private boolean validateIfCouponHasRestrictionBinDa(Campaign campaign, CouponFilterValidateRequest couponFilterValidateRequest) {

    if (campaign.getVariables() == null) {
      LOG.info("las variables de la campaña es nula");
      return false;
    }
    for (Variables variable : campaign.getVariables()) {
      if (variable == null) {
        LOG.info("variable es nula");
        return false;
      }
      if (variable.getValues() == null) {
        LOG.info("valores de la variable es nula");
        return false;
      }
      if (variable.getKey() == null) {
        LOG.info("key de la variable es nula");
        return false;
      }

      if (variable.getKey().trim().equalsIgnoreCase("CARDS_BINS")) {
        if (couponForBin(variable)) {
          return true;
        }

      }
    }
    return false;

  }

  private boolean couponForSource(Variables variable, RequestSourceEnum requestSourceEnum) {
    if (variable.getValues() == null) {
      LOG.info("No se encontro valores para la campana");
    }
    if (variable.getKey() == null) {
      LOG.info("No se encontro valores para la campana");
    }
    for (String value : variable.getValues()) {
      if (value.trim().equalsIgnoreCase(requestSourceEnum.name())) {
        LOG.info("Se encontro source para la campana");
        return true;
      }
    }

    return false;
  }


  private boolean couponsForDt(Variables variable, DeliveryType order) {

    if (variable.getValues() == null) {
      LOG.info("No se encontro valores para la campana");
    }
    if (variable.getKey() == null) {
      LOG.info("No se encontro valores para la campana");
    }
    for (String value : variable.getValues()) {
      if (value.trim().equalsIgnoreCase(order.getDeliveryType())) {
        return true;
      }
    }
    return false;
  }

  private GenericResponse parseError(String errorBodyy) {
    Gson gson = new Gson();
    return gson.fromJson(errorBodyy, GenericResponse.class);
  }

  private boolean iscustomerPaymentCard(final CouponFilterValidateRequest request) {
    return Objects.nonNull(request) && Objects.nonNull(request.getPaymentMethodId()) && Objects.nonNull(request.getPaymentCardId()) && request.getPaymentMethodId() == 3;
  }

  private boolean isCustomerDataphone(final CouponFilterValidateRequest request) {
    return Objects.nonNull(request) && Objects.nonNull(request.getPaymentMethodId()) && request.getPaymentMethodId() == 2;
  }

  private boolean isCustomerPaymentCardId(final CouponFilterValidateRequest request) {
    return Objects.nonNull(request) && Objects.nonNull(request.getPaymentCardId());
  }

  private GenericResponse validateCouponOrder(CouponFilterValidateRequest request, String couponName) throws IOException {
    Response<GenericResponse> responseBck3 = null;
    GenericResponse genericResponse = null;
    if (iscustomerPaymentCard(request) && isCustomerPaymentCardId(request) && couponName != null) {
//      LOG.info("customerPaymentCardId -> " + request.getPaymentMethodId());
      CouponValidation couponValidation = new CouponValidation();
      couponValidation.setCouponName(couponName);
      couponValidation.setPaymentCardId(request.getPaymentCardId());

      try {
        responseBck3 = ApiGatewayService.get().validateCouponOms(couponValidation);
      } catch (Exception e) {
        LOG.warning("Error@validateCouponOms " + e.getMessage());
      }
      if (responseBck3 != null && !responseBck3.isSuccessful()) {
        String error = (responseBck3.errorBody() != null ? responseBck3.errorBody().string() : "code : " + responseBck3.code());
        try {
          genericResponse = parseError(error);
//          LOG.info("genericResponse: " + genericResponse.toString());
          return genericResponse;
        } catch (Exception e) {
          LOG.warning("Ocurrio un error parseando el error de la respuesta de backend 3" + e.getMessage());
        }

      }

    }
    return genericResponse;
  }


  private DeliveryType getDeliveryType(String deliveryType) {
    return DeliveryType.getDeliveryType(deliveryType);
  }

  private CouponFilterValidateResponse buildResponseCoupon(String couponName, ErrorCouponMsg error, CouponFiltersConfig couponsFilter, CouponFilterValidateRequest couponFilterValidateRequest) {
    CouponFilterValidateResponse couponFilterValidateResponse = new CouponFilterValidateResponse();
    couponFilterValidateResponse.setCouponName(couponName);
    couponFilterValidateResponse.setMessage(obtainMessageDataPerCouponNameMessage(Objects.requireNonNull(couponsFilter.getMessagesError()), couponName, error, couponsFilter));
    couponFilterValidateResponse.setStatus(HttpStatusCode.OK.getCode());
    return couponFilterValidateResponse;
  }

  private String getIconImageUrl(ErrorCouponMsg error, List<ErrorCoupon> iconImageUrl) {
    String iconImage = "";
    if (iconImageUrl == null) {
      return iconImage;
    }
    for (ErrorCoupon errorCoupon : iconImageUrl) {
      if (Objects.equals(errorCoupon.getTypeError(), error.name())) {
        iconImage = errorCoupon.getValue();
        break;
      }
    }
    return iconImage;
  }

  private boolean couponForBin(Variables variable) {
    if (variable.getKey() == null) {
      return false;
    }
    if (variable.getValues() == null) {
      return true;
    }
    return variable.getValues().isEmpty();
  }


  private ValidationMessage obtainMessageDataPerCouponNameMessage(List<MessagesError> messagesErrors,
                                                                  String couponName, ErrorCouponMsg error, CouponFiltersConfig couponsFilter) {
    ValidationMessage couponFilterValidateResponse = null;
    couponFilterValidateResponse = builderResponse(messagesErrors, couponName, error, couponsFilter, getPredicate(couponName, error));

    if (couponFilterValidateResponse == null || (couponFilterValidateResponse.getMessage() == null && couponFilterValidateResponse.getTitle()==null) ) {

      couponFilterValidateResponse = builderResponse(messagesErrors, couponName, error, couponsFilter, getPredicatePerCouponName(couponName, error));
    }
    if (couponFilterValidateResponse == null || (couponFilterValidateResponse.getMessage() == null && couponFilterValidateResponse.getTitle()==null)) {
      couponFilterValidateResponse = builderResponse(messagesErrors, couponName, error, couponsFilter, getPredicateDefault(error));
    }
    optionButtons(couponFilterValidateResponse, error, couponsFilter);
    if(couponFilterValidateResponse != null ){
      couponFilterValidateResponse.setMandatoryFilter(isMandatoryFilter(error));
    }

    return couponFilterValidateResponse;
  }

  private void optionButtons(ValidationMessage validationMessage, ErrorCouponMsg error, CouponFiltersConfig couponsFilter) {
    if (couponsFilter.getOptionButtons() != null && !couponsFilter.getOptionButtons().isEmpty()) {
      couponsFilter.getOptionButtons().stream().filter(optionButton -> Objects.equals(optionButton.getTypeError(), error.name()))
              .findFirst().ifPresent(optionButton -> {

                if(optionButton.getFirstOption() != null && optionButton.getFirstAction() != null){
                  validationMessage.setFirstOption(optionButton.getFirstOption());
                  validationMessage.setFirstAction(optionButton.getFirstAction());
                }
                validationMessage.setFirstOption(optionButton.getFirstOption());
                if (optionButton.getSecondOption() != null && optionButton.getSecondAction()!=null) {
                  validationMessage.setSecondOption(optionButton.getSecondOption());
                  validationMessage.setSecondAction(optionButton.getSecondAction());
                }});}
  }

  private boolean validateList(AtomicReference<MessagesError> messagesErrorResponse) {
    return messagesErrorResponse.get() != null && (messagesErrorResponse.get().getValue() != null || messagesErrorResponse.get().getTitle() != null);
  }

  public static Predicate<MessagesError> getPredicate(String couponName, ErrorCouponMsg error) {
    return messageError -> Objects.equals(messageError.getTypeError(), error.name()) &&
            (messageError.getAplicateCoupons() != null && !messageError.getAplicateCoupons().isEmpty() && messageError.getAplicateCoupons().contains(couponName));

  }

  public static Predicate<MessagesError> getPredicatePerCouponName(String couponName, ErrorCouponMsg error) {
    return messageError -> Objects.equals(messageError.getTypeError(), error.name()) &&
            (messageError.getCouponName() != null && messageError.getCouponName().equals(couponName));

  }

  public static Predicate<MessagesError> getPredicateDefault(ErrorCouponMsg error) {
    return messageError -> Objects.equals(messageError.getTypeError(), error.name()) &&
            messageError.getCouponTypeMessage() != null && messageError.getCouponTypeMessage().trim().equalsIgnoreCase("GENERIC_MESSAGE");

  }

  private ValidationMessage builderResponse(List<MessagesError> messagesErrors,
                                            String couponName, ErrorCouponMsg error, CouponFiltersConfig couponsFilter, Predicate<MessagesError> predicate) {
    ValidationMessage couponFilterValidateResponse = new ValidationMessage();
    AtomicReference<MessagesError> messagesErrorResponse = new AtomicReference<>(new MessagesError());

    messagesErrors.stream().filter(predicate)
            .findFirst().ifPresent(messagesErrorResponse::set);
    if (validateList(messagesErrorResponse)) {

      String message = messagesErrorResponse.get().getValue();
      if (message != null && message.contains("{couponName}")) {
        message = message.replace("{couponName}", couponName);
      }
      couponFilterValidateResponse.setMessage(message);
      couponFilterValidateResponse.setIconImage(getIconImageUrl(error, couponsFilter.getIconMessage()));
      couponFilterValidateResponse.setErrorCode(error);
      String title = messagesErrorResponse.get().getTitle();
      if(title != null && title.contains("{couponName}")){
        title = title.replace("{couponName}", couponName);
      }
      couponFilterValidateResponse.setTitle(title);
      return couponFilterValidateResponse;
    }
    return couponFilterValidateResponse;
  }

  private boolean isMandatoryFilter(ErrorCouponMsg error) {
    if (error.equals(ErrorCouponMsg.ERROR_COUPON_CARD_TERMINAL)) {
      return false;
    }
    return true;
  }

  private boolean validatePayMethodCouponFilter(Integer paymethod,Campaign campaign ) {
    boolean validate = true;
    Long payMethod = Long.valueOf(paymethod);
    try {
      if (campaign != null && campaign.getPayMethods() != null && !campaign.getPayMethods().isEmpty()) {
        if(!campaign.getPayMethods().contains(payMethod)) {
          return false;
        }
      }
    } catch (Exception e) {
      LOG.warning("Error@validatePayMethodCouponFilter " + e.getMessage());
    }
    return validate;

  }

  private boolean allItemsAreMarketplace(List<DeliveryOrderItem> deliveryOrderItemList){

    long countItemsMarketplace = deliveryOrderItemList.stream().filter(item -> Objects.nonNull(item.getUuidItem()) && !item.getUuidItem().isEmpty()).count();
    return countItemsMarketplace == deliveryOrderItemList.size();

  }
}


