package com.imaginamos.farmatodo.backend.location;

import com.algolia.search.exceptions.AlgoliaException;
import com.google.api.server.spi.config.*;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.ConflictException;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.datastore.Query;
import com.google.gson.Gson;
import com.googlecode.objectify.Key;
import com.imaginamos.farmatodo.backend.cache.CachedDataManager;
import com.imaginamos.farmatodo.backend.customer.Customers;
import com.imaginamos.farmatodo.backend.firebase.FirebaseNotification;
import com.imaginamos.farmatodo.backend.user.Authenticate;
import com.imaginamos.farmatodo.backend.user.Users;
import com.imaginamos.farmatodo.backend.util.FTDUtil;
import com.imaginamos.farmatodo.model.Address.ConfigGoogleGeoReferencingCoRes;
import com.imaginamos.farmatodo.model.Address.CountryEnum;
import com.imaginamos.farmatodo.model.algolia.ScanAndGoPushNotificationProperty;
import com.imaginamos.farmatodo.model.algolia.SupportNumber;
import com.imaginamos.farmatodo.model.city.CityGrid;
import com.imaginamos.farmatodo.model.city.GeoGridsConfigAlgolia;
import com.imaginamos.farmatodo.model.customer.Address;
import com.imaginamos.farmatodo.model.customer.CustomerOnlyData;
import com.imaginamos.farmatodo.model.location.City;
import com.imaginamos.farmatodo.model.location.Country;
import com.imaginamos.farmatodo.model.location.Prefix;
import com.imaginamos.farmatodo.model.location.StoreList;
import com.imaginamos.farmatodo.model.store.Store;
import com.imaginamos.farmatodo.model.store.StoreGroup;
import com.imaginamos.farmatodo.model.user.PushNotification;
import com.imaginamos.farmatodo.model.user.User;
import com.imaginamos.farmatodo.model.util.*;
import com.imaginamos.farmatodo.networking.algolia.APIAlgolia;
import com.imaginamos.farmatodo.networking.growthbook.GrowthBookConfigLoader;
import com.imaginamos.farmatodo.networking.managers.AddressPredictionManager;
import com.imaginamos.farmatodo.networking.models.addresses.*;
import com.imaginamos.farmatodo.networking.models.addresses.autocompletefarmatodo.PlaceAlgolia;
import com.imaginamos.farmatodo.networking.models.addresses.geocodingfarmatodo.AddressAlgolia;
import com.imaginamos.farmatodo.networking.models.addresses.geocodingfarmatodo.FTDDataAddressPredictionRes;
import com.imaginamos.farmatodo.networking.models.braze.DefaultStoreForTemplate;
import com.imaginamos.farmatodo.networking.services.ApiGatewayService;
import com.imaginamos.farmatodo.networking.services.GeoGoogleService;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import javax.inject.Named;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static autovalue.shaded.org.apache.commons.lang.StringUtils.EMPTY;
import static com.imaginamos.farmatodo.backend.OfyService.ofy;

/**
 * Created by mileniopc on 11/24/16.
 * Property of Imaginamos.
 */

@Api(name = "locationEndpoint",
    version = "v1",
    scopes = {Constants.EMAIL_SCOPE},
    apiKeyRequired = AnnotationBoolean.TRUE,
    clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID_DEVELOPER, com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID},
    audiences = {Constants.ANDROID_AUDIENCE},
    namespace = @ApiNamespace(ownerDomain = Constants.OWNER_DOMAIN, ownerName = Constants.OWNER_NAME, packagePath = ""),
    description = "Queries cities, store groups and stores.")
public class LocationEndpoint {
    private Authenticate authenticate;
    private Users users;
    private Customers customers;

  private static final Logger LOG = Logger.getLogger(LocationEndpoint.class.getName());


    public LocationEndpoint() {
        authenticate = new Authenticate();
        users = new Users();
        customers = new Customers();
    }




  /**
   * Selection of all cities
   *
   * @param token          User's token.
   * @param tokenIdWebSafe Identification of the User's token.
   * @return List of Object of class 'City'.
   * @throws ConflictException
   */
  @ApiMethod(name = "getCitiesV2", path = "/locationEndpoint/v2/getCities", httpMethod = ApiMethod.HttpMethod.GET)
  public ItemsResponse getCitiesV2(@Named("token") final String token,@Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                            @Nullable @Named("deliveryType") String deliveryType) throws ConflictException, BadRequestException {

    List<City> cities = getCitiesDataStore(token, tokenIdWebSafe, deliveryType);

    if (deliveryType.equals(DeliveryType.EXPRESS.getDeliveryType())){
      setGridCoordinates(cities);
    }

    ItemsResponse response = new ItemsResponse();

    response.setItems(cities);

    return response;

  }



  /**
   * Selection of all cities
   *
   * @param token          User's token.
   * @param tokenIdWebSafe Identification of the User's token.
   * @return List of Object of class 'City'.
   * @throws ConflictException
   */
  @ApiMethod(name = "getCities", path = "/locationEndpoint/getCities", httpMethod = ApiMethod.HttpMethod.GET)
  public CollectionResponse<City> getCities(@Named("token") final String token,@Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                            @Nullable @Named("deliveryType") String deliveryType) throws ConflictException, BadRequestException {

    List<City> orderCities = getCitiesDataStore(token, tokenIdWebSafe, deliveryType);

    return CollectionResponse.<City>builder().setItems(orderCities).build();
  }

  @NotNull
  private List<City> getCitiesDataStore(@Named("token") String token, @Named("tokenIdWebSafe") String tokenIdWebSafe, @Named("deliveryType") @Nullable String deliveryType) throws ConflictException, BadRequestException {
//    LOG.info("IF(!authenticate.isValidToken(token, tokenIdWebSafe)) : ["+(!authenticate.isValidToken(token, tokenIdWebSafe))+"]");
    if (!authenticate.isValidToken(token, tokenIdWebSafe)) {
      throw new ConflictException(Constants.INVALID_TOKEN);
    }

    if(deliveryType == null || deliveryType.isEmpty()){
      deliveryType="EXPRESS";
    }


    // List<City> cityList = ofy().load().type(City.class).filter("deliveryType", deliveryType).list();
    final String deliveryTypeToFilter = deliveryType;
    List<City> cityList = new ArrayList<>();
    try {
      cityList = CachedDataManager.getCities().stream().filter(city -> city != null && city.getDeliveryType().name().equals(deliveryTypeToFilter)).collect(Collectors.toList());
      return cityList.stream().sorted(Comparator.comparing(City::getName)).collect(Collectors.toList());
    } catch (Exception e) {
      LOG.warning("Error@getCitiesDataStore => " + e.getMessage());
      e.printStackTrace();
      return cityList;
    }
  }

  /**
   * Set grid Express Cities
   * @param expressCities
   */
  private void setGridCoordinates(List<City> expressCities) {

    if (expressCities != null && expressCities.size() > 0){

      GeoGridsConfigAlgolia gridsConfig = APIAlgolia.getGeoGridsAlgolia();

      if (gridsConfig != null && gridsConfig.getCities()!= null && gridsConfig.getCities().size() > 0){

        for (City expressCity : expressCities) {
          for (CityGrid geoCity : gridsConfig.getCities()) {
            if (Objects.equals(geoCity.getId(), expressCity.getId()) && geoCity.getMunicipalityList() != null) {
              // set coordinates of algolia
//              LOG.info("Coordenadas city " + expressCity.getId() + " -> ");
//              geoCity.getMunicipalityList().forEach(municipalityList -> {
//                LOG.info("DATACITY: " + municipalityList.toString());
//              });
              expressCity.setMunicipalList(geoCity.getMunicipalityList());
            }
          }
        }

      }

    }

  }

  @ApiMethod(name = "getCountries", path = "/locationEndpoint/getCountries", httpMethod = ApiMethod.HttpMethod.GET)
  public CollectionResponse<Country> getCountries(@Named("token") final String token,
                                                  @Named("tokenIdWebSafe") final String tokenIdWebSafe) throws ConflictException, BadRequestException {
    if (!authenticate.isValidToken(token, tokenIdWebSafe))
      throw new ConflictException(Constants.INVALID_TOKEN);

    List<Country> countryList = ofy().load().type(Country.class).list();
    return CollectionResponse.<Country>builder().setItems(countryList).build();
  }

  @ApiMethod(name = "getCity", path = "/locationEndpoint/getCity", httpMethod = ApiMethod.HttpMethod.GET)
  public City getCity(@Named("token") final String token,
                      @Named("tokenIdWebSafe") final String tokenIdWebSafe, @Named("idCity") final String idCity) throws ConflictException, BadRequestException {
    if (!authenticate.isValidToken(token, tokenIdWebSafe))
      throw new ConflictException(Constants.INVALID_TOKEN);


    return ofy().load().type(City.class).filter("id", idCity).first().now();
  }

  /**
   * Selection of Store group by City
   *
   * @param token          User's token.
   * @param tokenIdWebSafe Identification of the User's token.
   * @param cityId         Identification of a city.
   * @return Array of stores object
   * @throws ConflictException
   */
  @ApiMethod(name = "getStoreGroups", path = "/locationEndpoint/getStoreGroups", httpMethod = ApiMethod.HttpMethod.GET)
  public CollectionResponse<StoreGroup> getStoreGroups(@Named("token") final String token,
                                                       @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                                       @Named("cityId") final String cityId) throws ConflictException, BadRequestException {
    if (!authenticate.isValidToken(token, tokenIdWebSafe))
      throw new ConflictException(Constants.INVALID_TOKEN);

    City city = ofy().load().type(City.class).filter("id", cityId).first().now();
    List<StoreGroup> cityList = ofy().load().type(StoreGroup.class).ancestor(city).list();
    return CollectionResponse.<StoreGroup>builder().setItems(cityList).build();
  }

  /**
   * Selection of store information by store group
   *
   * @param token          User's token.
   * @param tokenIdWebSafe Identification of the User's token.
   * @param storeGroupId   Identification of a store group.
   * @return Object of store information
   * @throws ConflictException
   */
  @ApiMethod(name = "getStoresInStoreGroup", path = "/locationEndpoint/getStoresInStoreGroup", httpMethod = ApiMethod.HttpMethod.GET)
  public CollectionResponse<Store> getStoresInStoreGroup(@Named("token") final String token,
                                                         @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                                         @Named("storeGroupId") final String storeGroupId,
                                                         @Named("cityId") final String cityId) throws ConflictException, BadRequestException {
    if (!authenticate.isValidToken(token, tokenIdWebSafe))
      throw new ConflictException(Constants.INVALID_TOKEN);

    City city = ofy().load().type(City.class).filter("id", cityId).first().now();
    StoreGroup storeGroup = ofy().load().type(StoreGroup.class).filter("storeGroupId", storeGroupId).ancestor(city).first().now();
    List<Store> storeList = ofy().load().type(Store.class).ancestor(storeGroup).list();
    return CollectionResponse.<Store>builder().setItems(storeList).build();
  }

  /**
   * Selection of all store information
   *
   * @param token          User's token.
   * @param tokenIdWebSafe Identification of the User's token.
   * @return Array of Object of store information
   * @throws ConflictException
   */
  @ApiMethod(name = "getAllStores1", path = "/locationEndpoint/getAllStore1s", httpMethod = ApiMethod.HttpMethod.GET)
  public CollectionResponse<Store> getAllStores1(@Named("token") final String token,
                                                 @Named("tokenIdWebSafe") final String tokenIdWebSafe) throws ConflictException, BadRequestException {
    if (!authenticate.isValidToken(token, tokenIdWebSafe))
      throw new ConflictException(Constants.INVALID_TOKEN);

    List<Store> storeList = ofy().load().type(Store.class).list();
    return CollectionResponse.<Store>builder().setItems(storeList).build();
  }

  /**
   * Select all the cities with their respective stores groups and the specific information of each store
   *
   * @param token          User's token.
   * @param tokenIdWebSafe Identification of the User's token.
   * @param cityName       city's name
   * @return Object of city with its stores group
   * @throws ConflictException
   */
  @ApiMethod(name = "getAllStores", path = "/locationEndpoint/getAllStores", httpMethod = ApiMethod.HttpMethod.GET)
  public CollectionResponse<City> getAllStores(@Named("token") final String token,
                                               @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                               @Named("cityName") final String cityName) throws ConflictException, BadRequestException {
    final String METHOD = "[getAllStores]";
//    LOG.info("[INI]"+METHOD);
//    LOG.info("cityName:"+cityName);

    if (!authenticate.isValidToken(token, tokenIdWebSafe)) {
      throw new ConflictException(Constants.INVALID_TOKEN);
    }

//    LOG.info("Consultando la primera ciudad...");
    City city1 = ofy().load().type(City.class).filter("name", cityName).first().now();
//    LOG.info("Creando filtro por cityName... de cache");
    Query.Filter filter = new Query.FilterPredicate("name", Query.FilterOperator.NOT_EQUAL, cityName);

    //List<String> cityId = Arrays.asList(URLConnections.MAIN_CITIES.split(","));
    List<String> cityId = Arrays.asList(APIAlgolia.getMainCities().split(","));
    Query.Filter filterCity = new Query.FilterPredicate("id", Query.FilterOperator.IN, cityId);

//    LOG.info("Consultando el listado de ciudades...");
    List<City> cityList = ofy().load().type(City.class).filter(filter).filter(filterCity).list(); //.filter(filter).order("name").list();
    cityList.sort(Comparator.comparing(city -> city.getName()));//Estudiar el sort
//    LOG.info("cityList.size() : ["+cityList.size()+"]");
    if(city1!=null){
//      LOG.info("city1 NO es null.");
      cityList.add(0, city1);
    }

    for (int i = 0; i < cityList.size(); i++) {
      City city = cityList.get(i);
//      LOG.info("city ---> "+city.getName());
      Key<City> cityKey = Key.create(City.class, city.getIdCity());
      List<StoreGroup> storeGroupList = ofy().load().type(StoreGroup.class).ancestor(cityKey).list();
      for (int j = 0; j < storeGroupList.size(); j++) {
        StoreGroup storeGroup = storeGroupList.get(j);
        Key<StoreGroup> storeGroupKey = Key.create(cityKey, StoreGroup.class, storeGroup.getIdStoreGroup());
        List<Store> storeList = ofy().load().type(Store.class).ancestor(storeGroupKey).order("name").list();
        storeGroupList.get(j).setStoreList(storeList);
      }
      cityList.get(i).setStoreGroupList(storeGroupList);
    }
    return CollectionResponse.<City>builder().setItems(cityList).build();
  }


  @ApiMethod(name = "getAllStoresOperator", path = "/locationEndpoint/getAllStoresOperator", httpMethod = ApiMethod.HttpMethod.GET)
  public CollectionResponse<City> getAllStoresOperator(@Named("keyClient") final String keyClient,
                                                       @Named("cityName") final String cityName) throws ConflictException, BadRequestException, UnauthorizedException {
    if (!keyClient.equals(Constants.KEY_SECURE_CLIENT))
      throw new UnauthorizedException(Constants.ERROR_ACCESS_DENIED);

    City city1 = ofy().load().type(City.class).filter("name", cityName).first().now();
    Query.Filter filter = new Query.FilterPredicate("name", Query.FilterOperator.NOT_EQUAL, cityName);
    List<City> cityList = ofy().load().type(City.class).filter(filter).order("name").list();

    if (city1!=null) {
      cityList.add(0, city1);
    }

    for (int i = 0; i < cityList.size(); i++) {
      City city = cityList.get(i);
      Key<City> cityKey = Key.create(City.class, city.getIdCity());
      List<StoreGroup> storeGroupList = ofy().load().type(StoreGroup.class).ancestor(cityKey).list();
      for (int j = 0; j < storeGroupList.size(); j++) {
        StoreGroup storeGroup = storeGroupList.get(j);
        Key<StoreGroup> storeGroupKey = Key.create(cityKey, StoreGroup.class, storeGroup.getIdStoreGroup());
        List<Store> storeList = ofy().load().type(Store.class).ancestor(storeGroupKey).order("name").list();
        storeGroupList.get(j).setStoreList(storeList);
      }
      cityList.get(i).setStoreGroupList(storeGroupList);
    }
    return CollectionResponse.<City>builder().setItems(cityList).build();
  }

  @ApiMethod(name = "getPrefix", path = "/locationEndpoint/getPrefix", httpMethod = ApiMethod.HttpMethod.GET)
  public CollectionResponseModel getPrefix(@Named("keyClient") final String keyClient)
      throws ConflictException, BadRequestException, UnauthorizedException {
    if (!keyClient.equals(Constants.KEY_SECURE_CLIENT))
      throw new UnauthorizedException(Constants.ERROR_ACCESS_DENIED);
    CollectionResponseModel collectionResponseModel = new CollectionResponseModel();
    List<Prefix> prefixes = ofy().load().type(Prefix.class).list();
    collectionResponseModel.setCountries(prefixes);
    return collectionResponseModel;
  }

  @ApiMethod(name = "getStoreList", path = "/locationEndpoint/getStoreList", httpMethod = ApiMethod.HttpMethod.GET)
  public StoreList getStoreList(@Named("keyClient") final String keyClient,
                                @Named("idStore") final Long idStore)
      throws BadRequestException, UnauthorizedException {
    if (keyClient == null || keyClient.isEmpty() || idStore == null || idStore <= 0 ){
      throw new BadRequestException(Constants.ERROR_BAD_REQUEST);
    }


    if (!keyClient.equals(Constants.KEY_SECURE_CLIENT))
      throw new UnauthorizedException(Constants.ERROR_ACCESS_DENIED);

    return ofy().load().type(StoreList.class).filter("storeList", idStore).first().now();
  }


  @ApiMethod(name = "predictAddress", path = "/locationEndpoint/predictAddress", httpMethod = ApiMethod.HttpMethod.GET)
  public AddressPredictionRes predictAddress(@Named("address") String address,
                                             @Named("city") String city) {

    AddressPredictionReq addressPredictionReq = new AddressPredictionReq();
    addressPredictionReq.setAddress(address);
    addressPredictionReq.setCity(city);

    //LOG.warning("Call predictAddress");
    String typeAlgolia =  Optional.ofNullable(Objects.requireNonNull(APIAlgolia.getTypeAutoComplete()).getTypeAutoComplete()).orElse("");

    return AddressPredictionManager.get().getAddressPredictions(addressPredictionReq,typeAlgolia);
  }

  @ApiMethod(name = "calculateDistance", path = "/locationEndpoint/calculateDistance", httpMethod = ApiMethod.HttpMethod.GET)
  public Answer calculateDistance(@Named("startLat") double startLati,@Named("startLon") double startLong, @Named("endLat")double endLati, @Named("endLon")double endLong) {

    double distance = LocationMethods.distanceInKm(startLati,startLong,endLati,endLong);

    double maxDistance = Objects.requireNonNull(APIAlgolia.getDistanceProperties()).getKms();

    Answer ans  = new Answer();
    ans.setConfirmation(true);

    NumberFormat formatter = new DecimalFormat("#0.00");
    System.out.println(formatter.format(4.0));

    //ans.setMessage("Current Distance: " + formatter.format(distance) + " kms <-> Max Distance: "+ maxDistance + " kms");

    if (distance >= maxDistance ) {
      ans.setConfirmation(false);
      ans.setMessage("Parece que estas lejos de esta dirección.");
    }

    return ans;
  }

  @ApiMethod(name = "autoCompleteDirections", path = "/locationEndpoint/autoCompleteDirections", httpMethod = ApiMethod.HttpMethod.POST)
  public GAutocompleteRes autoCompleteDirections(final AddressPredictionReq reverseGeoReq){
    return autocompleteAddress(reverseGeoReq);
  }

  @ApiMethod(name = "storeValidWhitCordinates", path = "/locationEndpoint/validateStoreByCoordinates", httpMethod = ApiMethod.HttpMethod.POST)
  public StoreCalculationDistanceRes storeValidWhitCordinates(final StoreValidWhitCordinatesReq request) throws ConflictException, AlgoliaException, BadRequestException {
//    LOG.info("INIT storeValidWhitCordinates()");

    if (request == null || request.getLatitude() == 0 || request.getLongitude() == 0){
      throw new BadRequestException(Constants.ERROR_BAD_REQUEST);
    }

    return LocationMethods.getDistanceToStoreFromLatLng(request.getLatitude(), request.getLongitude());
  }

  /**
   * Estrategias de Autocomplete.
   * */
  private GAutocompleteRes autocompleteAddress(AddressPredictionReq predictionReq) {

    if(validateAddressLength(predictionReq)) {
      return emptyAutocompleteRes();
    }

    final GAutocompleteRes farmatodoAutocomplete = null;

//    LOG.info("Llamando configuración de Keywords");
    KeyWordsCityConfig keyWordsCityConfig = APIAlgolia.getKeyWordsCityConfig();
    if (Objects.nonNull(keyWordsCityConfig) && Objects.nonNull(keyWordsCityConfig.getActive())
            && keyWordsCityConfig.getActive() && Objects.nonNull(keyWordsCityConfig.getKeywords())) {
      validateKeyWordsCitys(keyWordsCityConfig, predictionReq);
    }

    if (Objects.isNull(farmatodoAutocomplete) || Objects.isNull(farmatodoAutocomplete.getPredictions()) || farmatodoAutocomplete.getPredictions().isEmpty() ) {
      /** Llama a Lupap */
      final GAutocompleteRes lupapAutocomplete = lupapAutocomplete(predictionReq);
      if (Objects.isNull(lupapAutocomplete) || Objects.isNull(lupapAutocomplete.getPredictions()) || lupapAutocomplete.getPredictions().isEmpty() ) {

        /** Llama a Google */
        final GAutocompleteRes googleAutocomplete = googleAutocomplete(predictionReq);
        if (Objects.isNull(googleAutocomplete) || Objects.isNull(googleAutocomplete.getPredictions()) || googleAutocomplete.getPredictions().isEmpty() ) {
          return new GAutocompleteRes();
        } else {
          // Fallo Farmatodo y Lupap, Exito en Google
          //APIAlgolia.saveNewAutocompleteResult(googleAutocomplete, predictionReq.getCity(), "google");
          return googleAutocomplete;
        }
      } else {
        // Fallo Farmatodo, Exito en Lupap
        //APIAlgolia.saveNewAutocompleteResult(lupapAutocomplete, predictionReq.getCity(), "lupap");
        return lupapAutocomplete;
      }
    } else {
      // Exito en Farmatodo
//      LOG.info("#AHORRAMOS_UN_API_CALL");
      return farmatodoAutocomplete;
    }

  }

  private boolean validateAddressLength(AddressPredictionReq predictionReq) {
    ConfigValidateAddress configValidateAddress = APIAlgolia.getConfigValidateAddress();
    return predictionReq.getAddress().length() < configValidateAddress.getAddressSize();
  }
  private GAutocompleteRes emptyAutocompleteRes() {
    GAutocompleteRes gAutocompleteRes = new GAutocompleteRes();
    gAutocompleteRes.setStatus("200");
    return gAutocompleteRes;
  }

  private void validateKeyWordsCitys(KeyWordsCityConfig keyWordsCityConfig, AddressPredictionReq predictionReq) {
    if (!keyWordsCityConfig.getKeywords().isEmpty()) {
      keyWordsCityConfig.getKeywords().forEach(keyWord -> {
        if (Objects.equals(keyWord.getKey(), predictionReq.getCity())) {
//          LOG.info("key: " + keyWord.getKey());
//          LOG.info("city: " + predictionReq.getCity());
          predictionReq.setCity(keyWord.getValue());
        }
      });
    }
  }

  private GAutocompleteRes farmatodoAutocomplete(final AddressPredictionReq reverseGeoReq){
//    LOG.info("Called farmatodoAutocomplete()");
    GAutocompleteRes autocompleteRes = new GAutocompleteRes();
    ArrayList<GPlacePredictionRes> predictionList = new ArrayList<>();

    if (reverseGeoReq != null && reverseGeoReq.getCity() != null && reverseGeoReq.getAddress() != null) {

      final List<PlaceAlgolia> placesInAlgolia = APIAlgolia.findPlaceInAlgolia(reverseGeoReq.getCity(), reverseGeoReq.getAddress());

      if (Objects.nonNull(placesInAlgolia) && !placesInAlgolia.isEmpty()){
//        LOG.info("Se encontraron places en algolia.");
        for (int i=0; i<placesInAlgolia.size(); i++){
          final PlaceAlgolia place = placesInAlgolia.get(i);

          GPlacePredictionRes predictionRes = new GPlacePredictionRes();
          predictionRes.setId(place.getId());
          predictionRes.setDescription(place.getDescription());
          predictionRes.setPlaceId(place.getPlaceId());

          predictionList.add(predictionRes);
        }
        autocompleteRes.setPredictions(predictionList);
      }
    }
//    LOG.info("Retorna respuesta de farmatodo.");
    return autocompleteRes;
  }


  private GAutocompleteRes lupapAutocomplete(AddressPredictionReq reverseGeoReq) {

    GAutocompleteRes autocompleteRes = new GAutocompleteRes();
    ArrayList<GPlacePredictionRes> predictionList = new ArrayList<>();
    if (reverseGeoReq != null && reverseGeoReq.getCity() != null && reverseGeoReq.getAddress() != null) {

      AutocompleteLupapReq lupapRequest = new AutocompleteLupapReq();

      lupapRequest.setCity(reverseGeoReq.getCity().toLowerCase());
      lupapRequest.setAddress(reverseGeoReq.getAddress());

      AutocompleteLupapRes response = ApiGatewayService.get().lupapAutocomplete(lupapRequest);

      if (response != null && response.getPredictions() != null  &&!response.getPredictions().isEmpty()) {

        response.getPredictions().forEach( prediction -> {

          GPlacePredictionRes predictionAux = new GPlacePredictionRes();
          predictionAux.setPlaceId(prediction.getPlaceId());
          predictionAux.setId("lupap");
          predictionAux.setDescription(prediction.getDescription());

          predictionList.add(predictionAux);

        } );

        autocompleteRes.setPredictions(predictionList);

      }
    }

    return autocompleteRes;

  }

  // autocomplete with google
  private GAutocompleteRes googleAutocomplete(AddressPredictionReq reverseGeoReq) {
    //LOG.warning("call autocomplete" );

    String typeAlgolia =  Optional.ofNullable(Objects.requireNonNull(APIAlgolia.getTypeAutoComplete()).getTypeAutoComplete()).orElse("");

    final String country = Objects.nonNull(reverseGeoReq.getCountry())? CountryEnum.valueOf(reverseGeoReq.getCountry()).getId() : CountryEnum.valueOf("COL").getId();
//    LOG.info("Country -> "+country);
    reverseGeoReq.setCountry(country);

    return GeoGoogleService.get().getAutocomplete(reverseGeoReq,typeAlgolia);
  }

  @ApiMethod(name = "validateAddress", path = "/locationEndpoint/validateAddress", httpMethod = ApiMethod.HttpMethod.POST)
  public ValidateAddressRes validateAddress( final ValidateAddressReq validateAddressReq, @Nullable @Named("externalClient") final Boolean externalClient)  {

//    LOG.info("method -> validateAddress: toString -> " + validateAddressReq.toString());
    try {

      //Validar tokens y tokenIdWebSafe si no es un cliente frontend externo...
      if(externalClient == null || !externalClient){
        if (!validateAddressReq.isValid()){
          return new ValidateAddressRes(HttpStatusCode.BAD_REQUEST.getCode(),HttpStatusCode.BAD_REQUEST.getStatusName(),HttpStatusCode.FIELD_REQUIRED.getStatusName(),null);
        }

        if (!authenticate.isValidToken(validateAddressReq.getToken(), validateAddressReq.getTokenIdWebSafe()))
          return new ValidateAddressRes(HttpStatusCode.UNAUTHORIZED.getCode(),HttpStatusCode.UNAUTHORIZED.getStatusName(),HttpStatusCode.UNAUTHORIZED.getStatusName(),null);
      }

      // new case coordinates
      if (validateAddressReq.getLat() != null && validateAddressReq.getLng() != null ) {
        return getValidateAddressLatLng(validateAddressReq.getLat(),validateAddressReq.getLng());
      }


      // GeoCoder Unicamente con direccion
      Optional<ValidateAddressRes> optionalValidateAddressRes = Optional.empty();
      if (validateAddressReq.getIdCustomer() != null
              && validateAddressReq.getAddress() != null
              && !validateAddressReq.getAddress().isEmpty()){

        ValidateAddressRes predictionRes = getValidateAddressByAddress(validateAddressReq);
//        if (predictionRes != null) return predictionRes;

        if (predictionRes != null){
          optionalValidateAddressRes = Optional.of(predictionRes);
        }

      }

      if (optionalValidateAddressRes.isPresent() && optionalValidateAddressRes.get().getData() != null){
//        LOG.info("Response -> " + optionalValidateAddressRes.get().toStringJson());
        return optionalValidateAddressRes.get();
      }


      // Case 1: Select from list Autocomplete
      if (validateAddressReq.getPlaceId() != null
              && !validateAddressReq.getPlaceId().isEmpty()
              && validateAddressReq.getAddress() != null
              && !validateAddressReq.getAddress().isEmpty()){

//        LOG.info("Validate place id autocomplete");
        // lupap validateAddress
        // ValidateAddressRes
        ValidateAddressRes validateAddressRes = getValidateAddresLupapPlaceId(validateAddressReq.getPlaceId());

        // try placeID Service Lupap
        if (validateAddressRes != null && validateAddressRes.getData() != null){
//          LOG.info("Validate place id autocomplete LUPAP Success");
          return validateAddressRes;
        }

        // try GeoCoder Manual PlaceId
//        LOG.info("Try place id autocomplete Google");
        validateAddressRes = getValidateAddressGeoCoder(validateAddressReq.getPlaceId());
        if (validateAddressRes != null && validateAddressRes.getData() != null){
          return validateAddressRes;
        }


        // if lupap fail use google
        GPlaceIdRes gPlaceIdRes = GeoGoogleService.get().getPlaceById(validateAddressReq.getPlaceId());

//        LOG.info("place id (GOOGLE) response: " + gPlaceIdRes);

        if ( gPlaceIdRes.getLatitude() != 0 && gPlaceIdRes.getLongitude() != 0 ){

          double lat = gPlaceIdRes.getLatitude();
          double lng = gPlaceIdRes.getLongitude();
          return getValidateAddressLatLng(lat, lng);

        }else {
          return new ValidateAddressRes(HttpStatusCode.NO_CONTENT.getCode(),HttpStatusCode.NO_CONTENT.getStatusName(),Constants.LOCATION_PLACE_ID_NOT_FOUND,null);
        }

      } else if (validateAddressReq.getAddress() != null && !validateAddressReq.getAddress().isEmpty() && validateAddressReq.getPlaceId() == null) {
        //Case 2 : no select from list autocomplete google.

        ValidateAddressRes predictionRes = getValidateAddressByAddress(validateAddressReq);
        if (predictionRes != null) return predictionRes;

      }
      else {
        return new ValidateAddressRes(HttpStatusCode.NO_CONTENT.getCode(),HttpStatusCode.NO_CONTENT.getStatusName(),Constants.LOCATION_PLACE_ID_NOT_FOUND,null);
      }
      // No connection with servinformacion / google
      return new ValidateAddressRes(HttpStatusCode.INTERNAL_SERVER_ERROR.getCode(),HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusName(),Constants.LOCATION_PLACE_ID_NOT_FOUND,null);

    } catch (ConflictException | BadRequestException | IOException e) {
      LOG.warning("Error: " + Arrays.toString(e.getStackTrace()));
      return new ValidateAddressRes(HttpStatusCode.INTERNAL_SERVER_ERROR.getCode(),HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusName(),Constants.LOCATION_PLACE_ID_NOT_FOUND,null);
    }

  }

  @ApiMethod(name = "validateAddressV2", path = "/locationEndpoint/v2/validateAddress", httpMethod = ApiMethod.HttpMethod.POST)
  public ValidateAddressRes validateAddressV2(final ValidateAddressReq validateAddressReq,
                                              @Nullable @Named("externalClient") final Boolean externalClient) {
    LOG.info("method -> validateAddressV2: toString -> " + validateAddressReq);
    Gson gson = new Gson();
    try {
      // Validar tokens y tokenIdWebSafe
      if (isInternalClient(externalClient) && !validateAddressReq.isValid()) {
        return buildResponse(HttpStatusCode.BAD_REQUEST, HttpStatusCode.FIELD_REQUIRED.getStatusName());
      }

      if (isInternalClient(externalClient) && !authenticate.isValidToken(validateAddressReq.getToken(), validateAddressReq.getTokenIdWebSafe())) {
        return buildResponse(HttpStatusCode.UNAUTHORIZED, HttpStatusCode.UNAUTHORIZED.getStatusName());
      }

      // GeoInverso con lat y lng
      if (hasCoordinates(validateAddressReq)) {
        return getValidateAddressLatLngV2(validateAddressReq);
      }

      // Validación por dirección
      Optional<ValidateAddressRes> addressResponse = validateAddressByCustomerAndAddress(validateAddressReq);
      if (addressResponse.isPresent()) {
        LOG.info("Response -> " + addressResponse.get().toStringJson());
        return addressResponse.get();
      }

      // Caso 1: Autocomplete con PlaceId
      if (isPlaceIdAutocomplete(validateAddressReq)) {

        return validateAddressWithPlaceId(validateAddressReq);
      }

      // Caso 2: Dirección sin PlaceId
      if (isAddressOnly(validateAddressReq)) {
        ValidateAddressRes response = getValidateAddressByAddressV2(validateAddressReq);
        if (response != null) return response;
      }

      return LocationMethods.BuildResponseNotContent();

    } catch (Exception e) {
      LOG.warning("Error: " + e.getMessage());
      return buildResponse(HttpStatusCode.INTERNAL_SERVER_ERROR, Constants.LOCATION_PLACE_ID_NOT_FOUND);
    }
  }


  private boolean isInternalClient(@Nullable Boolean externalClient) {
    return externalClient == null || !externalClient;
  }

  private boolean hasCoordinates(ValidateAddressReq req) {
    return req.getLat() != null && req.getLng() != null;
  }

  private Optional<ValidateAddressRes> validateAddressByCustomerAndAddress(ValidateAddressReq req) {
    Gson gson = new Gson();
    if (req.getIdCustomer() != null && isNonEmpty(req.getAddress())) {
      ValidateAddressRes predictionRes = getValidateAddressByAddressV2(req);
      return Optional.ofNullable(predictionRes);
    }
    return Optional.empty();
  }

  private boolean isPlaceIdAutocomplete(ValidateAddressReq req) {
    return isNonEmpty(req.getPlaceId()) && isNonEmpty(req.getAddress());
  }

  private boolean isAddressOnly(ValidateAddressReq req) {
    return isNonEmpty(req.getAddress()) && req.getPlaceId() == null;
  }

  private ValidateAddressRes validateAddressWithPlaceId(ValidateAddressReq req) throws IOException {

    ValidateAddressRes response = getValidateAddresLupapPlaceIdV2(req);
    if (response != null && response.getData() != null) {
      return response;
    }

    response = getValidateAddressGeoCoderV2(req);
    if (response != null && response.getData() != null) {
      return response;
    }

    GPlaceIdRes gPlaceIdRes = GeoGoogleService.get().getPlaceById(req.getPlaceId());
    if (gPlaceIdRes.getLatitude() != 0 && gPlaceIdRes.getLongitude() != 0) {
      req.setLat(gPlaceIdRes.getLatitude());
      req.setLng(gPlaceIdRes.getLongitude());
      req.setIsInsideGeomalla(Boolean.TRUE);
      return getValidateAddressLatLngV2(req);
    }
    return LocationMethods.BuildResponseNotContent();
  }

  private ValidateAddressRes buildResponse(HttpStatusCode statusCode, String message) {
    return new ValidateAddressRes(statusCode.getCode(), statusCode.getStatusName(), message, null);
  }


  /**
   * GeoCoder Service Cloud Function Lupap-Servinformacion.
   * @param validateAddressReq
   * @return
   */
  private ValidateAddressRes getValidateAddressByAddress(ValidateAddressReq validateAddressReq) {

    City city = ofy().load().type(City.class).filter("id",validateAddressReq.getCity()).first().now();

    String cityName = ( city.getName() != null ) ? city.getName() : validateAddressReq.getCity();

    /** Buscar en direcciones ya registradas anteriormente. (para ahorrar) */
    AddressAlgolia addressAlgolia = APIAlgolia.findAddressInAlgoliaByCityAndAddress(validateAddressReq.getCity(), cityName, validateAddressReq.getAddress());

    if( Objects.nonNull(addressAlgolia) ) {
      return buildAddressAlgoliaToValidateAddresRes(addressAlgolia);
    }

    AddressPredictionReq geoCoderReq = new AddressPredictionReq( cityName , validateAddressReq.getAddress() );

    // lupap-servi
    GeoCoderResponse predictionRes = ApiGatewayService.get().geoCoder(geoCoderReq);

    LOG.warning("geocoder v1 -> " + predictionRes.toString());

    if(Objects.nonNull(predictionRes) && Objects.nonNull(predictionRes.getData())) {
      if ((Objects.nonNull(predictionRes.getData().getStoreName()) && predictionRes.getData().getStoreName().equals(Constants.ERROR_ZONE_OUT)) ||
              (Objects.nonNull(predictionRes.getData().getIdStore()) && predictionRes.getData().getIdStore().equals("00"))) {
        return new ValidateAddressRes(HttpStatusCode.NO_CONTENT.getCode(), HttpStatusCode.NO_CONTENT.getStatusName(), Constants.LOCATION_ZONE_OUT, null);
      }


      /** Validation for NATIONAL */
      if (Objects.nonNull(city) && Objects.nonNull(city.getDeliveryType())
              && city.getDeliveryType() == DeliveryType.NATIONAL
              && Objects.nonNull(predictionRes.getData())) {
          predictionRes.getData().setIdStore(String.valueOf(city.getDefaultStore() != 0 ? city.getDefaultStore() : 1000));
          predictionRes.getData().setStoreName(DeliveryType.NATIONAL.name());
      }

      if (Objects.nonNull(predictionRes.getData()) && Objects.nonNull(predictionRes.getData().getAddress())
              && !predictionRes.getData().getAddress().isEmpty()
              && predictionRes.getData().getIdStore() != null
              && !predictionRes.getData().getIdStore().isEmpty()) {
        if (validateStatusServinformacion(predictionRes.getData().getStatus())) {
          predictionRes.getData().setNeighborhood("");
          return new ValidateAddressRes(HttpStatusCode.OK.getCode(), HttpStatusCode.OK.getStatusName(), Constants.LOCATION_FOUND, predictionRes.getData());
        } else {
          return new ValidateAddressRes(HttpStatusCode.NO_CONTENT.getCode(), HttpStatusCode.NO_CONTENT.getStatusName(), Constants.LOCATION_PLACE_ID_NOT_FOUND, null);
        }
      }
    } else {
      String customerId = String.valueOf(validateAddressReq.getIdCustomer());
      ConfigGoogleGeoReferencingCoRes configGoogleGeoReferencingCo = GrowthBookConfigLoader.configGoogleGeoReferencingCo(customerId);

      int retryCount = getRetryCountFromCache(customerId);
      saveRetryCountInCache(customerId, retryCount + 1);

      if (configGoogleGeoReferencingCo.getActive() && retryCount <= configGoogleGeoReferencingCo.getMaxRetries()) {

        GGeoCodeRes geoCode = GeoGoogleService.get().getGeoCode(validateAddressReq.getAddress() + ", " + cityName);

        if (isGeoCodeValid(geoCode)) {
          double lat = geoCode.getResults().get(0).getGeometry().getLocation().getLat();
          double lng = geoCode.getResults().get(0).getGeometry().getLocation().getLng();

          LOG.warning("geocodeGoogle v1 -> " + geoCode.toString());

          ValidateGeoZoneReq geoZoneReq = buildGeoZoneRequest(validateAddressReq, city, lat, lng);
          GeoCoderResponse geoCoderResponse = ApiGatewayService.get().validateGeoZone(geoZoneReq);

          return processGeoCoderResponse(geoCoderResponse, geoCode, validateAddressReq, cityName, lat, lng);
        } else {
          return LocationMethods.BuildResponseNotContent();
        }
      }
    }
    return null;
  }
  private ValidateAddressRes getValidateAddressByAddressV2(ValidateAddressReq validateAddressReq) {
    try {
      String keyCache = validateAddressReq.getAddress();
      City city = ofy().load().type(City.class).filter("id", validateAddressReq.getCity()).first().now();

      String cityName = ( city.getName() != null ) ? city.getName() : validateAddressReq.getCity();

      // Buscar en direcciones ya registradas anteriormente
      AddressAlgolia addressAlgolia = APIAlgolia.findAddressInAlgoliaByCityAndAddress(validateAddressReq.getCity(), cityName,
              validateAddressReq.getAddress());

      if( Objects.nonNull(addressAlgolia) ) {
        return buildAddressAlgoliaToValidateAddresRes(addressAlgolia);
      }

      AddressPredictionReq geoCoderReq = new AddressPredictionReq(cityName, validateAddressReq.getAddress(),
              city.getDeliveryType().getDeliveryType(), "col");

      // Llamada al servicio
      GeoCoderResponse predictionRes = ApiGatewayService.get().geoCoderV2(geoCoderReq);
      LOG.warning("geocoder v2 -> " + predictionRes);

      // Validación de respuesta y lógica principal
      if (isValidPredictionResponse(predictionRes) && validateStatusServinformacion(predictionRes.getData().getStatus())) {

        if (isErrorZoneOut(predictionRes.getData())) {
          return buildValidateAddressResponse(HttpStatusCode.NO_CONTENT, Constants.LOCATION_ZONE_OUT, null, keyCache);
        }

        // Configurar respuesta con datos de ciudad
        predictionRes.getData().setCityCode(validateAddressReq.getCity());
        predictionRes.getData().setCity(cityName);

        ValidateAddressRes res = buildValidateAddressResponse(HttpStatusCode.OK, Constants.LOCATION_FOUND, predictionRes.getData(), keyCache);

        return res;
      }
      else {
        String customerId = String.valueOf(validateAddressReq.getIdCustomer());
        ConfigGoogleGeoReferencingCoRes configGoogleGeoReferencingCo = GrowthBookConfigLoader.configGoogleGeoReferencingCo(customerId);

        int retryCount = getRetryCountFromCache(customerId);
        saveRetryCountInCache(customerId, retryCount + 1);

        if (configGoogleGeoReferencingCo.getActive() && retryCount <= configGoogleGeoReferencingCo.getMaxRetries()) {

          GGeoCodeRes geoCode = GeoGoogleService.get().getGeoCode(validateAddressReq.getAddress() + ", " + cityName);

          if (isGeoCodeValid(geoCode)) {
            double lat = geoCode.getResults().get(0).getGeometry().getLocation().getLat();
            double lng = geoCode.getResults().get(0).getGeometry().getLocation().getLng();

            LOG.warning("geocodeGoogle v1 -> " + geoCode.toString());

            ValidateGeoZoneReq geoZoneReq = buildGeoZoneRequest(validateAddressReq, city, lat, lng);
            GeoCoderResponse geoCoderResponse = ApiGatewayService.get().validateGeoZone(geoZoneReq);

            return processGeoCoderResponse(geoCoderResponse, geoCode, validateAddressReq, cityName, lat, lng);
          } else {
            return LocationMethods.BuildResponseNotContent();
          }
        }
      }

    } catch (Exception e) {
      LOG.warning(e.toString());
    }
    return LocationMethods.BuildResponseNotContent();
  }

  private int getRetryCountFromCache(String customerId) {
    return com.imaginamos.farmatodo.networking.cache.CachedDataManager
            .getJsonFromCacheIndex(customerId +"_"+ Constants.REDIS_ATTEMPTS_FOR_GOOGLE, Constants.INDEX_REDIS_ATTEMPTS_FOR_GOOGLE)
            .map(Integer::parseInt)
            .orElse(0);
  }

  private void saveRetryCountInCache(String customerId, int count) {
    com.imaginamos.farmatodo.networking.cache.CachedDataManager
            .saveJsonInCacheIndexTime(customerId +"_"+ Constants.REDIS_ATTEMPTS_FOR_GOOGLE, String.valueOf(count), Constants.INDEX_REDIS_ATTEMPTS_FOR_GOOGLE, Constants.TIME_EXPIRE_IN_SECONDS);
  }

  private boolean isGeoCodeValid(GGeoCodeRes geoCode) {
    return geoCode != null && geoCode.getResults() != null && !geoCode.getResults().isEmpty()
            && geoCode.getResults().get(0).getGeometry() != null
            && geoCode.getResults().get(0).getGeometry().getLocation() != null
            && geoCode.getResults().get(0).getGeometry().getLocation().getLat() != 0
            && geoCode.getResults().get(0).getGeometry().getLocation().getLng() != 0;
  }

  private ValidateGeoZoneReq buildGeoZoneRequest(ValidateAddressReq req, City city, double lat, double lng) {
    ValidateGeoZoneReq geoZoneReq = new ValidateGeoZoneReq();
    geoZoneReq.setAddressLat(lat);
    geoZoneReq.setAddressLng(lng);
    geoZoneReq.setCityId(req.getCity());
    geoZoneReq.setDeliveryType(city.getDeliveryType().getDeliveryType());
    return geoZoneReq;
  }

  private ValidateAddressRes processGeoCoderResponse(GeoCoderResponse response, GGeoCodeRes geoCode,
                                                     ValidateAddressReq req, String cityName, double lat, double lng) {
    if (response.getData() == null || !response.getData().getDeliveryType().equals(DeliveryType.EXPRESS.getDeliveryType())) {
      return new ValidateAddressRes(HttpStatusCode.NO_CONTENT.getCode(), HttpStatusCode.NO_CONTENT.getStatusName(),
              Constants.LOCATION_ZONE_OUT, null);
    }

    response.getData().setLatitude(lat);
    response.getData().setLongitude(lng);
    response.getData().setCityCode(req.getCity());
    response.getData().setCity(cityName);
    response.getData().setAddress(geoCode.getResults().get(0).getFormattedAddress());

    ValidateAddressRes res = new ValidateAddressRes(HttpStatusCode.OK.getCode(), HttpStatusCode.OK.getStatusName(),
            Constants.LOCATION_FOUND, response.getData());

    String jsonResponse = new Gson().toJson(res);
    com.imaginamos.farmatodo.networking.cache.CachedDataManager.saveJsonInCacheIndexTime(
            String.valueOf(req.getAddress()), jsonResponse, Constants.INDEX_REDIS_RES_FOR_ADDRESS, Constants.TIME_EXPIRE_RES_FOR_ADDRESS);

    return res;
  }

  private boolean isValidPredictionResponse(GeoCoderResponse predictionRes) {
    return Objects.nonNull(predictionRes) &&
            Objects.nonNull(predictionRes.getData()) &&
            isNonEmpty(predictionRes.getData().getAddress()) &&
            isNonEmpty(predictionRes.getData().getIdStore());
  }

  private boolean isErrorZoneOut(SIDataAddressPredictionRes data) {
    return Constants.ERROR_ZONE_OUT.equals(data.getStoreName()) || "00".equals(data.getIdStore());
  }

  private ValidateAddressRes buildValidateAddressResponse(HttpStatusCode statusCode, String statusMessage, SIDataAddressPredictionRes data,String keyCache) {
    ValidateAddressRes validateAddressRes =new ValidateAddressRes(statusCode.getCode(), statusCode.getStatusName(), statusMessage, data);
    saveResponseInCache(keyCache, validateAddressRes);
    return validateAddressRes;
  }

  private void saveResponseInCache(String address, ValidateAddressRes response) {
    String jsonResponse = new Gson().toJson(response);
    com.imaginamos.farmatodo.networking.cache.CachedDataManager.saveJsonInCacheIndexTime(
            String.valueOf(address), jsonResponse,
            Constants.INDEX_REDIS_RES_FOR_ADDRESS, Constants.TIME_EXPIRE_RES_FOR_ADDRESS);
  }

  private boolean isNonEmpty(String value) {
    return Objects.nonNull(value) && !value.isEmpty();
  }

  private ValidateAddressRes getValidateAddressGeoCoder(String placeId) {
//    LOG.info(" method getValidateAddressGeoCoder placeId: " + placeId);
    if (placeId != null && !placeId.isEmpty()){

      try {
        String placeData [] = placeId.split(";");

        if (placeData.length >= 4){
          String address = placeData[0];
          String city = placeData[1];
          String country = placeData[2];
          String type = placeData[3];

          if (type != null && type.equals("address") && city != null && address != null) {

            /**  Nueva busqueda en algolia. (Ahorro) */
            final AddressAlgolia addressAlgolia = APIAlgolia.findAddressInAlgoliaByCityAndAddress("", city, address);

            if (Objects.nonNull(addressAlgolia)) {
              return buildAddressAlgoliaToValidateAddresRes(addressAlgolia);
            }

            AddressPredictionReq geoCoderReq = new AddressPredictionReq( city, address );
//            LOG.info("Request lupap geocoder -> " + geoCoderReq.toStringJson());
            GeoCoderResponse geoCoder = ApiGatewayService.get().geoCoder(geoCoderReq);
            if (geoCoder != null && geoCoder.getData() != null){
              geoCoder.getData().setNeighborhood("");
              return new ValidateAddressRes(HttpStatusCode.OK.getCode(), HttpStatusCode.OK.getStatusName(), Constants.LOCATION_FOUND, geoCoder.getData());
            }
          }
        }

      }catch (Exception e) {
        LOG.warning("Error getValidateAddressGeoCoder -> " + e.getMessage());
        return null;
      }
    }
    return null;
  }

  private ValidateAddressRes getValidateAddressGeoCoderV2(ValidateAddressReq validateAddressReq) {
    ValidateAddressRes validateAddressRes = null;

    try {
      String[] placeData = validateAddressReq.getPlaceId().split(";");
      if (isPlaceDataValid(placeData)) {
        String address = placeData[0];
        String city = placeData[1];
        String country = placeData[2];
        String type = placeData[3];

        if ("address".equals(type) && isNonEmpty(city) && isNonEmpty(address)) {
          // Nueva búsqueda en Algolia (ahorro)
          AddressAlgolia addressAlgolia = APIAlgolia.findAddressInAlgoliaByCityAndAddress("", city, address);
          if (Objects.nonNull(addressAlgolia)) {
            return buildAddressAlgoliaToValidateAddresRes(addressAlgolia);
          }

          GeoCoderResponse geoCoderResponse = isGeoCoderValid(city, address);
          // Búsqueda en GeoCoder
          if ( Objects.nonNull(geoCoderResponse) && Objects.nonNull(geoCoderResponse.getData()) && geoCoderResponse.getData().getLatitude() > 0) {
            validateAddressReq.setLat(geoCoderResponse.getData().getLatitude());
            validateAddressReq.setLng(geoCoderResponse.getData().getLongitude());
            validateAddressReq.setIsInsideGeomalla(Boolean.TRUE);
            validateAddressRes = getValidateAddressLatLngV2(validateAddressReq);
          }
        }
      }
    } catch (Exception e) {
      LOG.warning("Error en getValidateAddressGeoCoderV2 --> " + e.getMessage());
    }

    return validateAddressRes;
  }


  private boolean isPlaceDataValid(String[] placeData) {
    return Objects.nonNull(placeData) && placeData.length >= 4;
  }

  private GeoCoderResponse isGeoCoderValid(String city, String address) {
    AddressPredictionReq geoCoderReq = new AddressPredictionReq(city, address);
    return ApiGatewayService.get().geoCoderByLupapServi(geoCoderReq);
  }


  private ValidateAddressRes getValidateAddresLupapPlaceId(String placeId) throws IOException {
    GeoCoderResponse geoCoderResponse = ApiGatewayService.get().geoCoderLupapPlaceId(placeId);
    // TODO OPORTUNIDAD DE AHORRO. GUARDANDO LOS DETAILS POR PLACE_ID
    if (geoCoderResponse == null ||
            geoCoderResponse.getData() == null ||
            geoCoderResponse.getData().getAddress() == null ||
            geoCoderResponse.getData().getAddress().isEmpty()){
      //LOG.warning("Not found response geocoder lupap");
      return null;
    }

    geoCoderResponse.getData().setNeighborhood("");
    return new ValidateAddressRes(HttpStatusCode.OK.getCode(),HttpStatusCode.OK.getStatusName(),Constants.LOCATION_FOUND,geoCoderResponse.getData());
  }

  private ValidateAddressRes getValidateAddresLupapPlaceIdV2( ValidateAddressReq validateAddressReq) throws IOException {
    ValidateAddressRes validateAddressRes = null;
    try {
      GeoCoderResponse geoCoderResponse = ApiGatewayService.get().geoCoderLupapPlaceIdV2(validateAddressReq.getPlaceId());
      //LOG.info(geoCoderResponse.toString());

      // Si trae coordenadas es un sitio encontrado por Lupap
      if (geoCoderResponse == null ||
              geoCoderResponse.getData() == null ||
              geoCoderResponse.getData().getStatus() == null ||
              geoCoderResponse.getData().getStatus().isEmpty()) {
        //LOG.warning("Not found response geocoder lupap V2");
        return validateAddressRes;
      }

      validateAddressReq.setLat(geoCoderResponse.getData().getLatitude());
      validateAddressReq.setLng(geoCoderResponse.getData().getLongitude());
      validateAddressReq.setIsInsideGeomalla(Boolean.TRUE);
      validateAddressRes = getValidateAddressLatLngV2(validateAddressReq);

    } catch (Exception e) {
    LOG.warning("getValidateAddresLupapPlaceIdV2 -->" + e.toString());
    }
    return validateAddressRes;
  }


  private ValidateAddressRes buildAddressAlgoliaToValidateAddresRes(final AddressAlgolia addressAlgolia){
//    LOG.info("Called buildAddressAlgoliaToValidateAddresRes()");
    final FTDDataAddressPredictionRes resultFarmatodo = new FTDDataAddressPredictionRes(
            addressAlgolia.getStatus(),
            "",
            addressAlgolia.getAddress(),
            Double.parseDouble(addressAlgolia.getLatitude()),
            Double.parseDouble(addressAlgolia.getLongitude()),
            "",
            addressAlgolia.getStoreName(),
            addressAlgolia.getIdStore()
    );
//    LOG.info("#AHORRAMOS_UN_API_CALL");
    resultFarmatodo.setNeighborhood("");
    return new ValidateAddressRes(HttpStatusCode.OK.getCode(), HttpStatusCode.OK.getStatusName(), Constants.LOCATION_FOUND, resultFarmatodo);
  }


  @NotNull
  private ValidateAddressRes getValidateAddressLatLng(double latitude, double longitude) {

    /** Buscar en direcciones ya registradas anteriormente. (para ahorrar) */
    final AddressAlgolia addressAlgolia = APIAlgolia.findAddressInAlgoliaByLatAndLon(latitude, longitude);

    if( Objects.nonNull(addressAlgolia) ) {
      return buildAddressAlgoliaToValidateAddresRes(addressAlgolia);
    }

    // NEW
    ReverseGeoRes reverseGeoRes = ApiGatewayService.get().geoInverse(new ReverseGeoReq(latitude,longitude));

    // validate address
    if (reverseGeoRes != null
            && reverseGeoRes.getData() != null
            && reverseGeoRes.getData().getAddress() != null
            && !reverseGeoRes.getData().getAddress().isEmpty()
            && reverseGeoRes.getData().getIdStore() != null
            && !reverseGeoRes.getData().getIdStore().isEmpty())  {

      if (reverseGeoRes.getData().getStoreName().equals(Constants.ERROR_ZONE_OUT) || reverseGeoRes.getData().getIdStore().equals("00")){
        return new ValidateAddressRes(HttpStatusCode.NO_CONTENT.getCode(),HttpStatusCode.NO_CONTENT.getStatusName(),Constants.LOCATION_ZONE_OUT,null);
      }

      if (validateStatusServinformacion(reverseGeoRes.getData().getStatus())){
        // set coordinates
        reverseGeoRes.getData().setLatitude(latitude);
        reverseGeoRes.getData().setLongitude(longitude);

        if (reverseGeoRes.getData() != null && reverseGeoRes.getData().getCity() != null && !reverseGeoRes.getData().getCity().isEmpty()) {

          List<City> cityDsList = ofy().load().type(City.class).list();
          City cityAux = null;
          if (cityDsList != null && !cityDsList.isEmpty()) {
            for (City cityDs : cityDsList) {
              if (cityDs.getGeoCityCode().equalsIgnoreCase(reverseGeoRes.getData().getCity())) {
                cityAux = cityDs;
                break;
              }
            }
          }

          if (cityAux!= null && cityAux.getId() != null && !cityAux.getId().isEmpty() ) {
//            LOG.info(cityAux.toString());
            if (cityAux.getId().equals("JIC")) reverseGeoRes.getData().setIdStore("28");

            reverseGeoRes.getData().setCityCode(cityAux.getId());
          }

        }

        reverseGeoRes.getData().setNeighborhood("");
        return new ValidateAddressRes(HttpStatusCode.OK.getCode(),HttpStatusCode.OK.getStatusName(),Constants.LOCATION_FOUND,reverseGeoRes.getData());
      }else {
        return new ValidateAddressRes(HttpStatusCode.NO_CONTENT.getCode(),HttpStatusCode.NO_CONTENT.getStatusName(),Constants.LOCATION_PLACE_ID_NOT_FOUND,null);
      }

    }else {
      return new ValidateAddressRes(HttpStatusCode.NO_CONTENT.getCode(),HttpStatusCode.NO_CONTENT.getStatusName(),Constants.LOCATION_PLACE_ID_NOT_FOUND,null);
    }
  }
  @NotNull
  private ValidateAddressRes getValidateAddressLatLngV2(ValidateAddressReq validateAddressReq) {
    try {
      // Buscar en direcciones ya registradas
      final AddressAlgolia addressAlgolia = APIAlgolia.findAddressInAlgoliaByLatAndLon(validateAddressReq.getLat(), validateAddressReq.getLng());
      if (Objects.nonNull(addressAlgolia)) {
        return buildAddressAlgoliaToValidateAddresRes(addressAlgolia);
      }

      String keyCache = String.valueOf((validateAddressReq.getLat() + validateAddressReq.getLng()));
      City city = ofy().load().type(City.class).filter("id", validateAddressReq.getCity()).first().now();

      ReverseGeoRes reverseGeoRes = ApiGatewayService.get().geoInverseV2(
              new ReverseGeoReq(validateAddressReq.getLat(), validateAddressReq.getLng(),
                      validateAddressReq.getIsInsideGeomalla(), validateAddressReq.getCity(),
                      city.getDeliveryType().getDeliveryType()));

      if (isReverseGeoResValid(reverseGeoRes)) {
        LOG.warning("reverseGeoRes v1 -> " + reverseGeoRes);

        if (isErrorZoneOut(reverseGeoRes.getData())) {
          return buildValidateAddressRes(HttpStatusCode.NO_CONTENT, Constants.LOCATION_ZONE_OUT, null, keyCache);
        }

        if (validateStatusServinformacion(reverseGeoRes.getData().getStatus())) {
          LOG.info(String.format("Response utilities --> %s", reverseGeoRes));
          processGeoResCityData(reverseGeoRes, validateAddressReq);

          return buildValidateAddressRes(HttpStatusCode.OK, Constants.LOCATION_FOUND, reverseGeoRes.getData(), keyCache);
        }
      }else if(city.getDeliveryType().getDeliveryType().equals(DeliveryType.NATIONAL.name())) {
        ReverseGeoDataRes data = getReverseGeoDataResNationalDefault(validateAddressReq, city);

        return buildValidateAddressRes(HttpStatusCode.OK, Constants.LOCATION_FOUND, data, keyCache);
      }

      LOG.warning(String.format("Error en el response geoinverso utilities con response --> %s", reverseGeoRes));
    } catch (Exception e) {
      LOG.warning("Error en getValidateAddressLatLngV2: " + e.getMessage());
    }
    return LocationMethods.BuildResponseNotContent();
  }

  @NotNull
  private static ReverseGeoDataRes getReverseGeoDataResNationalDefault(ValidateAddressReq validateAddressReq, City city) {
    ReverseGeoDataRes data = new ReverseGeoDataRes();
    data.setStatus(Constants.BLOQUEADO);
    data.setNeighborhood(EMPTY);
    data.setAddress(validateAddressReq.getAddress());
    data.setStoreName(Constants.ENVIO_NACIONAL.toLowerCase());
    data.setIdStore(String.valueOf(DeliveryType.NATIONAL.getDefaultStore()));
    data.setCity(city.getName());
    data.setDeliveryType(city.getDeliveryType().getDeliveryType());
    data.setIsMultiOrigin(false);
    data.setPlaceName(EMPTY);
    data.setCityCode(validateAddressReq.getCity());
    data.setLatitude(validateAddressReq.getLat());
    data.setLongitude(validateAddressReq.getLng());

    return data;
  }


  private boolean isReverseGeoResValid(ReverseGeoRes reverseGeoRes) {
    return Objects.nonNull(reverseGeoRes) &&
            Objects.nonNull(reverseGeoRes.getData()) &&
            isNonEmpty(reverseGeoRes.getData().getAddress()) &&
            isNonEmpty(reverseGeoRes.getData().getIdStore()) &&
            isNonEmpty(reverseGeoRes.getData().getCity()) &&
            isNonEmpty(reverseGeoRes.getData().getDeliveryType());
  }

  private boolean isErrorZoneOut(ReverseGeoDataRes data) {
    return Constants.ERROR_ZONE_OUT.equals(data.getStoreName()) || "00".equals(data.getIdStore());
  }

  private void processGeoResCityData(ReverseGeoRes reverseGeoRes, ValidateAddressReq req) {
    String expressFueraDeZona = "EXPRESS FUERA DE ZONA";
    ReverseGeoDataRes data = reverseGeoRes.getData();

    data.setLatitude(req.getLat());
    data.setLongitude(req.getLng());

    if (!expressFueraDeZona.equals(data.getDeliveryType()) &&
            DeliveryType.NATIONAL.name().equalsIgnoreCase(data.getDeliveryType())) {

      List<City> cityDsList = ofy().load().type(City.class).filter("deliveryType", data.getDeliveryType()).list();
      if (cityDsList == null || cityDsList.isEmpty()) {
        throw new IllegalStateException("Error al traer ciudades desde DS");
      }

      String cleanStrCity = FTDUtil.cleanString(data.getCity());
      City cityAux = cityDsList.stream()
              .filter(city -> city.getGeoCityCode().equalsIgnoreCase(cleanStrCity))
              .findFirst()
              .orElseThrow(() -> new IllegalStateException("Error al mapear getCityCode en DS"));

      data.setCityCode(cityAux.getId());
      data.setCity(cityAux.getName());
    }

    if (isNonEmpty(data.getCityCode())) {
      return;
    }
    throw new IllegalStateException("Error al obtener cityCode en direccion");
  }

  private ValidateAddressRes buildValidateAddressRes(HttpStatusCode status, String statusMessage, ReverseGeoDataRes data, String keyCache) {

    ValidateAddressRes validateAddressRes = new ValidateAddressRes(status.getCode(), status.getStatusName(), statusMessage, data);

    String jsonResponse = new Gson().toJson(validateAddressRes);
    com.imaginamos.farmatodo.networking.cache.CachedDataManager.saveJsonInCacheIndexTime(
            String.valueOf(keyCache), String.valueOf(jsonResponse), Constants.INDEX_REDIS_RES_FOR_ADDRESS, Constants.TIME_EXPIRE_RES_FOR_ADDRESS);

    return validateAddressRes;
  }


  private boolean validateStatusServinformacion(String status){

    for (StatusGeoCoder st: StatusGeoCoder.values()) {
        if (st.getStatus().equals(status) && st.isHaveCoordinates()){
            return true;
        }
    }
    return false;
  }

  @ApiMethod(name = "getSupportNumbers", path = "/locationEndpoint/getSupportNumbers", httpMethod = ApiMethod.HttpMethod.GET)
  public List<SupportNumber> getSupportNumbers(@Named("keyClient") final String keyClient)
          throws ConflictException, BadRequestException, UnauthorizedException {
    if (Objects.isNull(keyClient) || !keyClient.equals(Constants.KEY_SECURE_CLIENT))
          throw new UnauthorizedException(Constants.ERROR_ACCESS_DENIED);
      return APIAlgolia.getSupportNumbers();
  }

  @ApiMethod(name = "updateCityDepartmentDS", path = "/locationEndpoint/updateCityDepartmentDS", httpMethod = ApiMethod.HttpMethod.POST)
  public Answer updateCityDepartmentDS(City city) {
    City cityFind = ofy().load().type(City.class).filter("id",city.getId()).first().now();
    cityFind.setDepartment(city.getDepartment());
    ofy().save().entity(cityFind);
    return new Answer(true);
  }

  @ApiMethod(name = "sendNotificationPushScanAndGo", path = "/locationEndpoint/sendNotificationPushScanAndGo", httpMethod = ApiMethod.HttpMethod.GET)
  public Answer sendNotificationPushScanAndGo(@Named("idCustomerWebSafe") final String idCustomerWebSafe,
                                              @Named("idStore") final Long idStore) {

    try {
      List<Store> storeList = ofy().load().type(Store.class).list();
      String storeName = "Farmatodo";
      if (Objects.nonNull(storeList) && !storeList.isEmpty() && storeList.stream().filter(store -> store.getId() == idStore).findFirst().isPresent()) {
        storeName = storeList.stream().filter(store -> store.getId() == idStore).findFirst().get().getName();
      }

      Key<User> userKey = Key.create(idCustomerWebSafe);
//        LOG.info("Key user created..." + userKey.toString());
      CustomerOnlyData customer = customers.setCustomerOnlyData(users.findUserByKey(userKey));
      String userName = "Querido usuario";
      if (Objects.nonNull(customer) && Objects.nonNull(customer.getFirstName()) && !customer.getFirstName().isEmpty()) {
        userName = customer.getFirstName();
      }

      Optional<ScanAndGoPushNotificationProperty> optionalScanAndGoPushNotificationProperty = APIAlgolia.getScanAndGoPushNotificationProperty();
//        LOG.info("optionalScanAndGoPushNotificationProperty -> " + optionalScanAndGoPushNotificationProperty.isPresent());
      if (optionalScanAndGoPushNotificationProperty.isPresent()) {
        ScanAndGoPushNotificationProperty pushNotificationProperty = optionalScanAndGoPushNotificationProperty.get();
        pushNotificationProperty.setTitle(MessageFormat.format(pushNotificationProperty.getTitle(), userName, storeName));
        return senPushNotification(idCustomerWebSafe, pushNotificationProperty);
      }
    }catch (Exception ex){
      LOG.warning("method sendNotificationPushScanAndGo Error no fue posible enviar el PUSH: "+ ex.getMessage());
    }
    return new Answer(true);
  }

    private Answer senPushNotification(final String idCustomerWebSafe,
                                       final ScanAndGoPushNotificationProperty scanAndGoPushNotificationProperty){
      try {
//        LOG.info("method senPushNotification: Start send notification scan and go service...");
        Key<User> userKey = Key.create(idCustomerWebSafe);
//        LOG.info("Key user created..." + userKey.toString());
        User user = users.findUserByKey(userKey);
        if (user == null)
          throw new ConflictException(Constants.USER_NOT_FOUND);
        if (Objects.nonNull(scanAndGoPushNotificationProperty)) {
//          LOG.info("pushNotificationProperty -> Hours:" + scanAndGoPushNotificationProperty.getTimeToPushInHours()
//                  + ", message: " + scanAndGoPushNotificationProperty.getMessage());
          PushNotification pushNotification = ofy().load().type(PushNotification.class).ancestor(user).first().now();

          if (Objects.isNull(pushNotification)) {
            pushNotification = new PushNotification();
            pushNotification.setUser(userKey);
            pushNotification.setIdPushNotification(UUID.randomUUID().toString());
          }
//          LOG.info("pushNotification -> timeLastPush:" + pushNotification.getTimeLastPush());
          //Verify last notification
          if (Objects.isNull(pushNotification.getTimeLastPush()) || verifyTimeToPush(pushNotification.getTimeLastPush(),
                  scanAndGoPushNotificationProperty.getTimeToPushInHours())) {
            // SEND NOTIFICATION
//            LOG.info("Sending notification...");
            FirebaseNotification.generalNotificationService(user.getId(), scanAndGoPushNotificationProperty.getTitle(),
                    scanAndGoPushNotificationProperty.getMessage(), null);
            pushNotification.setTimeLastPush(DateTime.now().getMillis());
//            LOG.info("Notification sended");
          }
          ofy().save().entity(pushNotification).now();
        }
      } catch (Exception e) {
        LOG.warning("ERROR -> " + e.toString() + " , " + e.getCause());
        return new Answer(false);
      }
      return new Answer(true);
    }

    private boolean verifyTimeToPush(Long timeLast, int nextTime) {
        DateTime lastDate = new DateTime(timeLast, DateTimeZone.forID("America/Bogota"));
        DateTime nextDate = lastDate.plusHours(nextTime);
        return nextDate.isBeforeNow();
    }


    public SIDataAddressPredictionRes buildValidateAddressResBackend3(Address address){
      SIDataAddressPredictionRes response = new SIDataAddressPredictionRes();
      response.setAddress(address.getAddress());
      response.setIdStore(String.valueOf(address.getCloserStoreId()));
      response.setLatitude(address.getLatitude());
      response.setLongitude(address.getLongitude());
      response.setStoreName(address.getCityName());
      return response;
    }


  /**
   * Selection of all cities
   *
   * @param cityId Identification of the User's token.
   * @return Id of the defaultStore for the city entered.
   */
  @ApiMethod(name = "getDefaultStoreByCityId", path = "/locationEndpoint/v2/getDefaultStoreByCityId", httpMethod = ApiMethod.HttpMethod.GET)
  public DefaultStoreForTemplate getDefaultStoreByCityId(@Named("cityId") final String cityId) throws ConflictException {

    List<City> cities = getCitiesDataStoreWithoutToken();

    Optional<City> cityOptional = cities.stream().filter(c -> c.getId().equals(cityId.toUpperCase())).findFirst();
//    return cities;
    if(cityOptional.isPresent()) {
      return new DefaultStoreForTemplate(cityOptional.get().getDefaultStore());
    } else {
      LOG.severe("No se encontro el cityId en datastore");
      throw new ConflictException("No se encontro el cityId en datastore");
    }


  }

  @NotNull
  private List<City> getCitiesDataStoreWithoutToken() {
    final String deliveryTypeToFilter = "EXPRESS";
    List<City> cityList = new ArrayList<>();
    try {
      cityList = CachedDataManager.getCities().stream().filter(city -> city != null && city.getDeliveryType().name().equals(deliveryTypeToFilter)).collect(Collectors.toList());
      return cityList.stream().sorted(Comparator.comparing(City::getName)).collect(Collectors.toList());
    } catch (Exception e) {
      LOG.warning("Error@getCitiesDataStore => " + e.getMessage());
      e.printStackTrace();
      return cityList;
    }
  }

}
