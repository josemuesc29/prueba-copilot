package com.imaginamos.farmatodo.backend.customer;

import com.googlecode.objectify.Ref;
import com.imaginamos.farmatodo.backend.util.FTDUtil;
import com.imaginamos.farmatodo.model.user.Credential;
import com.imaginamos.farmatodo.model.user.EmailChangeUser;
import com.imaginamos.farmatodo.model.util.Answer;
import com.imaginamos.farmatodo.networking.algolia.APIAlgolia;
import com.google.api.server.spi.response.*;
import com.google.appengine.api.datastore.Query;
import com.googlecode.objectify.Key;
import com.imaginamos.farmatodo.backend.product.ProductsMethods;
import com.imaginamos.farmatodo.backend.user.Users;
import com.imaginamos.farmatodo.model.categories.Department;
import com.imaginamos.farmatodo.model.cms.Banner;
import com.imaginamos.farmatodo.model.customer.*;
import com.imaginamos.farmatodo.model.location.City;
import com.imaginamos.farmatodo.model.product.Highlight;
import com.imaginamos.farmatodo.model.product.Item;
import com.imaginamos.farmatodo.model.product.ItemGroup;
import com.imaginamos.farmatodo.model.product.Suggestion;
import com.imaginamos.farmatodo.model.user.User;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.networking.services.ApiGatewayService;
import org.json.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.imaginamos.farmatodo.backend.OfyService.ofy;

/**
 * Created by mileniopc on 10/28/16.
 * Property of Imaginamos.
 */
public class Customers {

  private ProductsMethods products = new ProductsMethods();
  private Users users = new Users();
  private static final Logger log = Logger.getLogger(Customer.class.getName());

  public Optional<CustomerData> setCustomerInformation(User user, int idStoreGroup, boolean isHome)throws BadRequestException, IOException, InternalServerErrorException {
    Optional<CustomerJSON> optionalCustomerJSON = ApiGatewayService.get().getCustomerById(user.getId());

    if (!optionalCustomerJSON.isPresent()){
      return Optional.empty();
    }

    CustomerJSON customerJSON  = optionalCustomerJSON.get();
    CustomerData customerData = new CustomerData(customerJSON);

    //Key<Department> classificationLevel1 = Key.create(Department.class, 1L);
    //List<Banner> banners = ofy().load().type(Banner.class).filter("bannerWeb", false).filterKey("<", classificationLevel1)
    //        .filter("directionBanner", false).list();

    List<com.imaginamos.farmatodo.model.algolia.Banner> bannerList = APIAlgolia.getBanners(null, false, false);
    List<Banner> banners = bannerList.stream().map(banner -> {
      Banner banner1 = new Banner();
      banner1.setRedirectUrl(banner.getRedirectUrl());
      banner1.setRedirectType(banner.getRedirectType());
      banner1.setRedirectId(banner.getRedirectId());
      banner1.setUrlBanner(banner.getUrlBanner());
      banner1.setDirectionBanner(banner.isDirectionBanner());
      banner1.setBannerWeb(banner.isBannerWeb());
      banner1.setOrder(banner.getOrder());
      banner1.setCampaignName(banner.getCampaignName());
      banner1.setCreative(banner.getCreative());
      banner1.setPosition(banner.getPosition());
      banner1.setIdWebSafeBanner(banner.getIdWebSafeBanner());
      //log.warning("method: getBanners - "+ banner1.getBannerWeb() +" : "+banner1.getIdBanner()+" : "+ banner1.getRedirectUrl() +" : "+ banner1.getUrlBanner());
      return banner1;
    }).collect(Collectors.toList());


    List<Item> previousItems;
    if (Objects.nonNull(customerData) && Objects.nonNull(customerData.getPurchases())) {
      previousItems = products.getItemsByIds(customerData.getPurchases(), idStoreGroup);
    } else {
      previousItems = null;
    }

    if (Objects.nonNull(customerData)) {
      if (Objects.nonNull(banners)) {
        customerData.setBanners(banners);
      }
      customerData.setPreviousItems(previousItems);
      customerData.setPurchases(null);

      if (customerData.getAddresses() != null && !customerData.getAddresses().isEmpty()) {
        for (Address address : customerData.getAddresses()) {
          //log.info("Address coordinates before update -> " + address.getLatitude() + ", " + address.getLongitude());
          if (address.getLatitude() == 0 || address.getLongitude() == 0) {
            address.setLatitude(4.6730450);
            address.setLongitude(-74.0583310);
          }
        }
      }
    }
    return Optional.of(customerData);

  }

  public CustomerOnlyData setCustomerOnlyData(User user, int idStoreGroup, boolean isHome) throws IOException {
    CustomerOnlyData customerOnlyData = ApiGatewayService.get().getCustomerOnlyById(user.getId());
//    log.info("is user -> "+ user.getId());
    if (Objects.nonNull(customerOnlyData)) {
      customerOnlyData.setPreviousItems(products.getItemsByIds(customerOnlyData.getPurchases(), idStoreGroup));
      customerOnlyData.setPurchases(null);
      if (user.getId() == 0){
          customerOnlyData.setPreviousItems(null);
      }
      return customerOnlyData;
    }
    return  null;
  }

  public Optional<CustomerJSON> customerInformation(User user, int idStoreGroup, boolean isHome)
      throws BadRequestException, IOException, InternalServerErrorException {
    Optional<CustomerJSON> optionalCustomerJSON = ApiGatewayService.get().getCustomerById(user.getId());

    if (!optionalCustomerJSON.isPresent()){
      return Optional.empty();
    }

    CustomerJSON customerJSON = optionalCustomerJSON.get();
    if (isHome) {
      Key<Department> classificationLevel1 = Key.create(Department.class, 1L);
      List<Banner> banners = ofy().load().type(Banner.class).filter("bannerWeb", false).filterKey("<", classificationLevel1)
          .filter("directionBanner", false).list();

      List<Item> previousItems;
      if (Objects.nonNull(customerJSON) && Objects.nonNull(customerJSON.getPurchases())) {
        previousItems = products.getItemsByIds(customerJSON.getPurchases(), idStoreGroup);
      } else {
        previousItems = null;
      }
      Query.Filter filter = new Query.FilterPredicate("categories.clasification",
          Query.FilterOperator.EQUAL, 0);
      List<Highlight> highlightedItems = ofy().load().type(Highlight.class).filter(filter).order("orderingNumber").list();
      List<Highlight> highlightList = new ArrayList<>();
      long current = System.currentTimeMillis();
      Key<ItemGroup> itemGroupKey = Key.create(ItemGroup.class, "1");


        for (Highlight highlight : highlightedItems) {
          if (highlight.getStartDate() < current && current <= highlight.getEndDate()) {
            if (highlight.getType().equals("UNIQUE")) {
              Key<Item> itemKey = Key.create(itemGroupKey, Item.class, Long.toString(highlight.getItems().get(0).getItem()));
              Item item = ofy().load().key(itemKey).now();
              List<Item> unique = new ArrayList<>();
              //products.setStoreInfo(item, idStoreGroup);
              try {
                products.setFindInformationToAlgolia(item, idStoreGroup);
              unique.add(item);
              highlight.setProduct(unique);
              highlight.setItem(highlight.getItems().get(0).getItem());
            }catch (Exception e){
              log.warning("Error! -> "  + Arrays.toString(e.getStackTrace()));
            }
            }
            highlightList.add(highlight);
          }
        }

        if (Objects.requireNonNull(customerJSON).getSuggested() != null && !customerJSON.getSuggested().isEmpty()) {

          for (Suggestion suggestion : customerJSON.getSuggested()) {
            if (suggestion.getType().equals("UNIQUE")) {
              if (suggestion.getItems() != null && !suggestion.getItems().isEmpty()) {
                Key<Item> itemKey = Key.create(itemGroupKey, Item.class, Long.toString(suggestion.getItems().get(0).getItem()));
                Item item = ofy().load().key(itemKey).now();
                if (item != null) {
                  List<Item> unique = new ArrayList<>();
                  //products.setStoreInfo(item, idStoreGroup);
                  try {
                  products.setFindInformationToAlgolia(item, idStoreGroup);
                  unique.add(item);
                  suggestion.setProduct(unique);
                  }catch (Exception e){
                    log.warning("Error! -> "  + Arrays.toString(e.getStackTrace()));
                  }
                }
              }
            }
          }
        }

      Objects.requireNonNull(customerJSON).setProfileImageUrl(null);
      customerJSON.setBanners(banners);
      customerJSON.setSuggestedProducts(customerJSON.getSuggested());
      customerJSON.setPreviousItems(previousItems);
      customerJSON.setHighlightedItems(highlightList);
    }

    Objects.requireNonNull(customerJSON).setComponents(null);
    customerJSON.setSuggested(null);
    customerJSON.setPurchases(null);
    if (customerJSON.getAddresses() != null && !customerJSON.getAddresses().isEmpty()) {
      for (Address address : customerJSON.getAddresses()) {
        address.setLatitude(4.6730450);
        address.setLongitude(-74.0583310);
      }
    }
    customerJSON.setPhotos(ApiGatewayService.get().getCustomerPhotos(user.getId()));
    return Optional.of(customerJSON);
  }

  @SuppressWarnings("ALL")
  public JSONObject createCustomerJson(Customer customer, String idGoogle, String idFacebook) {
    JSONObject customerJson = new JSONObject();
    customerJson.put("firstName", customer.getFirstName());
    customerJson.put("lastName", customer.getLastName());
    customerJson.put("gender", customer.getGender());
    customerJson.put("documentNumber", customer.getDocumentNumber());

    if (!customer.getSource().equals("APP") && !customer.getSource().equals("IOs"))
      customerJson.put("source", customer.getSource());
    else
      customerJson.put("source", "IOS");
    if (idFacebook == null && idGoogle == null)
      customerJson.put("password", customer.getPassword());
    else if (idGoogle == null)
      customerJson.put("facebookId", idFacebook);
    else
      customerJson.put("googleId", idGoogle);

    customerJson.put("email", customer.getEmail().toLowerCase());
    customerJson.put("phone", customer.getPhone());
    return customerJson;
  }

  public JSONObject createCustomerJson(Customer customer) {
    JSONObject customerJson = new JSONObject();
    customerJson.put("firstName", customer.getFirstName());
    customerJson.put("lastName", customer.getLastName());
    customerJson.put("email", customer.getEmail());
    customerJson.put("documentNumber", customer.getDocumentNumber());
    customerJson.put("phone", customer.getPhone());
    customerJson.put("gender", customer.getGender());
    customerJson.put("source", customer.getSource());
    JSONArray interestJsonArray = new JSONArray();
    for (Interests interest : customer.getInterests()) {
      JSONObject interestJson = new JSONObject();
      interestJson.put("id", interest.getId());
      interestJsonArray.put(interestJson);
    }
    customerJson.put("interests", interestJsonArray);
    return customerJson;
  }

  public CustomerJSON customerUpdate(final Customer customer) throws ConflictException, IOException {
    return ApiGatewayService.get().updateCustomer(customer);
  }

  public Customer findCustomerByDocumentNumber(String documentNumber) throws IOException, ConflictException {
    CustomerJSON customerJSON = ApiGatewayService.get().getCustomerByDocument(documentNumber);
    Customer customer = null;

    if (customerJSON != null) {
      customer = new Customer();
      customer.setId(customerJSON.getId());
      customer.setRegisteredBy(customerJSON.getRegisteredBy());
      customer.setEmail(customerJSON.getEmail());
    }
    return customer;
  }

  @SuppressWarnings("ALL")
  public Boolean findCustomerByEmail(String email, boolean isCall)
          throws UnauthorizedException, BadRequestException, InternalServerErrorException, ConflictException, IOException, NotFoundException {
    JSONObject customer = new JSONObject();
    customer.put("email", email.toLowerCase());
//    log.info("isCall -> " + isCall);
    if (isCall){
      return ApiGatewayService.get().validateCustomerEmailCall(email).getData();
    } else {
      return ApiGatewayService.get().validateCustomerEmail(email).getData();
    }
  }


  @SuppressWarnings("ALL")
  public ValidateCustomerDocumentNumber findCustomerDocumentNumber(Long documentNumber)
          throws UnauthorizedException, BadRequestException, InternalServerErrorException, ConflictException, IOException, NotFoundException {
    JSONObject customer = new JSONObject();
    customer.put("documentNumber", documentNumber);
    return ApiGatewayService.get().validateCustomerDocumentNumber(documentNumber).getData();
  }


  @SuppressWarnings("ALL")
  public JSONObject createCreditCardJson(CreditCardToken creditCardToken) {
    JSONObject creditCardJson = new JSONObject();
    creditCardJson.put("customerId", creditCardToken.getId());
    creditCardJson.put("token", creditCardToken.getCreditCardTokenId());
    creditCardJson.put("customerName", creditCardToken.getCustomerName());
    creditCardJson.put("customerDocumentNumber", creditCardToken.getDocumentNumber());
    creditCardJson.put("paymentMethod", creditCardToken.getPaymentMethod());
    creditCardJson.put("number", null);
    creditCardJson.put("maskedNumber", creditCardToken.getMaskedNumber());
    creditCardJson.put("expirationDate", null);
    creditCardJson.put("creationDate", null);
    return creditCardJson;
  }

  public CreditCardReq createCreditCardReq(CreditCardToken creditCardToken){
    return new CreditCardReq(creditCardToken.getId(), creditCardToken.getCreditCardTokenId(),
            creditCardToken.getCustomerName(), creditCardToken.getDocumentNumber(), creditCardToken.getPaymentMethod(),
            null, creditCardToken.getMaskedNumber(), null, null);
  }

  @SuppressWarnings("ALL")
  public JSONObject defaultCard(long id, int idCard) {
    JSONObject creditCardJson = new JSONObject();
    creditCardJson.put("customerId", id);
    creditCardJson.put("creditCardId", idCard);
    return creditCardJson;
  }

  public JSONObject validateAddress(String city, String address) {
    JSONObject addressJson = new JSONObject();
    addressJson.put("city", city);
    addressJson.put("address", address);
    return addressJson;
  }

  @SuppressWarnings("ALL")
  public JSONObject changePasswordJson(Customer customer) {
    JSONObject changePasswordJson = new JSONObject();
    changePasswordJson.put("customerId", customer.getId());
    changePasswordJson.put("oldpassword", customer.getOldPassword());
    changePasswordJson.put("newpassword", customer.getPassword());
    return changePasswordJson;
  }

  @SuppressWarnings("ALL")
  public JSONObject resetPasswordJson(String email) {
    JSONObject changePasswordJson = new JSONObject();
    changePasswordJson.put("email", email);
    return changePasswordJson;
  }

  public List<Address> getAddressesByIdCustomer(User user) throws IOException {
    List<Address> addresses = null;
    try {
      AddressesRes res = ApiGatewayService.get().getAddressesByCustomerId(user.getId());
      if (res == null || res.getAddresses() == null || res.getAddresses().isEmpty()) {
        addresses = new ArrayList<>();
      } else {
        addresses = res.getAddresses();
      }
    }catch (Exception ex){
      log.warning("Error al mapear el resultado: "+ex.getMessage());
    }
    return addresses;
  }


    public AddressesRes getAddressByCustomerWebSafe(String idCustomerWebSafe, String deliveryType) throws UnauthorizedException, IOException {
        Key<User> userKey = Key.create(idCustomerWebSafe);
        User user = users.findUserByKey(userKey);
        if (user == null || user.getId() == 0)
            throw new UnauthorizedException(Constants.USER_NOT_FOUND);


        List<Address> allAddresses = this.getAddressesByIdCustomer(user);
        List<Address> addressesResList = new ArrayList<>();
        if (allAddresses != null && !allAddresses.isEmpty()) {
            for (Address address : allAddresses) {
                City city = ofy().load().type(City.class).filter("deliveryType", address.getDeliveryType())
                        .filter("id", address.getCity())
                        .first().now();
                if (address.getLatitude() == 0 || address.getLongitude() == 0 ) {
                    double lat = city != null ? city.getLatitude() : 4.6730450;
                    double lng = city != null ? city.getLongitude() : -74.0583310;
                    address.setLatitude(lat);
                    address.setLongitude(lng);
                }
                // add default store
                if (city != null) {
                  address.setDefaultStore(city.getDefaultStore());
                  if (city.getGeoCityCode() != null ) {
                    address.setCityName(FTDUtil.toTitleCase(city.getGeoCityCode().toLowerCase()));
                  }
                  if (city.getCountry() != null ) {
                    switch (city.getCountry()) {
                      case "CO":
                        address.setCountryName("Colombia");
                        break;
                      case "VE":
                        address.setCountryName("Venezuela");
                        break;
                      default:
                        break;
                    }
                  }
                }

                if (deliveryType != null && !deliveryType.isEmpty()
                        && address.getDeliveryType().getDeliveryType().equalsIgnoreCase(deliveryType)) {
                    addressesResList.add(address);
                }
            }
        }else {
            throw new UnauthorizedException(Constants.NOT_CONTENT_ADDRESS);
        }

        addressesResList = addressesResList.isEmpty() ? allAddresses : addressesResList;

        AddressesRes addressesRes = new AddressesRes();
        addressesRes.setAddresses(addressesResList);

        return addressesRes;

    }

    public CustomerOnlyData setCustomerOnlyData(User user) throws IOException {
      CustomerOnlyData customerOnlyData = ApiGatewayService.get().getCustomerOnlyById(user.getId());
      return customerOnlyData;
    }

  public Boolean findCustomerByPhone(String phone) throws  IOException {
    ValidateCustomerPhoneReq req = new ValidateCustomerPhoneReq();
    req.setPhone(phone);
    boolean resp = ApiGatewayService.get().validateCustomerPhone(req);
//    log.info("findCustomerByPhone: resp! -> "  + resp);
    return resp;
  }

  public Answer changeEmailCustomerClick(String idCustomerWebSafe, String newEmail) throws UnauthorizedException, ConflictException, IOException {
    Key<User> userKey = Key.create(idCustomerWebSafe);
    User user = users.findUserByKey(userKey);

    if (userKey == null || user == null ){
//      log.info("method: getFavorites() --> ConflictException [IdCustomerWebSafe Nor Found]");
      throw new UnauthorizedException(Constants.DEFAULT_MESSAGE);
    }

    EmailChangeUser emailChangeUserExists = ofy().load().type(EmailChangeUser.class).filter("userId", user.getId()).first().now();

    if (emailChangeUserExists != null){
      throw new ConflictException(Constants.ERROR_MAIL_CHANGE);
    }

    Ref<User> referenceUser = Ref.create(userKey);
//    log.info("reference user -> " + referenceUser.toString());
    Credential credential = users.findCredentialByKey(referenceUser);

    Answer answer = new Answer();
    if ( credential != null) {
      credential.setEmail(newEmail);
      ofy().save().entity(credential).now();
      answer.setConfirmation(true);
      answer.setMessage(Constants.SUCCESS);

      Optional<UpdateEmailCustomerResponse> optionalResponseUpdateEmail = ApiGatewayService
              .get()
              .updateEmailCustomer((long) user.getId(),newEmail);

//      log.info("update email CRM -> " + newEmail);

      optionalResponseUpdateEmail.ifPresent(res -> {
        log.info("response -> " + res.toString());
      });

      EmailChangeUser emailChangeUser = new EmailChangeUser();
      emailChangeUser.setIdEmailChangeUser(UUID.randomUUID().toString());
      emailChangeUser.setUserId((long) user.getId());
//      emailChangeUser.setEmailChanged(true);
      ofy().save().entity(emailChangeUser).now();

    }else {
      answer.setConfirmation(false);
      answer.setMessage(Constants.ERROR_EMAIL_USER);
    }
    return answer;
  }

}

