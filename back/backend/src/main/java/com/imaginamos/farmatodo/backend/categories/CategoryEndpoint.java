package com.imaginamos.farmatodo.backend.categories;

import com.algolia.search.exceptions.AlgoliaException;
import com.google.api.server.spi.config.*;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.ConflictException;
import com.google.api.server.spi.response.InternalServerErrorException;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.LoadException;
import com.imaginamos.farmatodo.backend.cache.CachedDataManager;
import com.imaginamos.farmatodo.backend.user.Users;
import com.imaginamos.farmatodo.model.dto.EnableForEnum;
import com.imaginamos.farmatodo.model.order.RequestSourceEnum;
import com.imaginamos.farmatodo.model.user.User;
import com.imaginamos.farmatodo.model.util.CategoryResponse;
import com.imaginamos.farmatodo.networking.algolia.APIAlgolia;
import com.imaginamos.farmatodo.backend.categorie.CategorieMethods;
import com.imaginamos.farmatodo.backend.product.ProductsMethods;
import com.imaginamos.farmatodo.backend.user.Authenticate;
import com.imaginamos.farmatodo.model.algolia.HitsItemsAlgolia;
import com.imaginamos.farmatodo.model.algolia.ItemAlgolia;
import com.imaginamos.farmatodo.model.categories.*;
import com.imaginamos.farmatodo.model.cms.CategoryPhoto;
import com.imaginamos.farmatodo.model.product.*;
import com.imaginamos.farmatodo.model.util.CollectionResponseModel;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.networking.services.ApiGatewayService;
import com.imaginamos.farmatodo.networking.talonone.TalonOneService;
import com.imaginamos.farmatodo.networking.talonone.model.TalonAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.imaginamos.farmatodo.backend.OfyService.ofy;
import static java.util.Comparator.nullsLast;

/**
 * Created by mileniopc on 12/13/16.
 * Property of Imaginamos.
 */


@Api(name = "categoryEndpoint",
    version = "v1",
    apiKeyRequired = AnnotationBoolean.TRUE,
    scopes = {Constants.EMAIL_SCOPE},
    clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID_DEVELOPER, com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID},
    audiences = {Constants.ANDROID_AUDIENCE},
    namespace = @ApiNamespace(ownerDomain = Constants.OWNER_DOMAIN, ownerName = Constants.OWNER_NAME),
    description = "Queries categories.")
public class CategoryEndpoint {
  private static final Logger log = Logger.getLogger(CategoryEndpoint.class.getName());

  private CategorieMethods categorieMethods ;

  private Authenticate authenticate;
  private ProductsMethods productsMethods;
  private Users users;


  public CategoryEndpoint() {
    authenticate = new Authenticate();
    productsMethods = new ProductsMethods();
    categorieMethods = new CategorieMethods();
    users=new Users();
  }

  /**
   * Selection of categories of class "Department",
   * each with its categorization of level 2, and level 3 in the event that "isWeb" is true
   *
   * @param token          User's token.
   * @param tokenIdWebSafe Identification of the User's token.
   * @return JSON of Objects of class "Department".
   * @throws ConflictException
   */
  @ApiMethod(name = "getCategories", path = "/categoryEndpoint/getCategories", httpMethod = ApiMethod.HttpMethod.GET)
  public CollectionResponseModel getCategories(@Named("token") final String token,
                                               @Named("tokenIdWebSafe") final String tokenIdWebSafe)
      throws ConflictException, BadRequestException {

    if (!authenticate.isValidToken(token, tokenIdWebSafe))
      throw new ConflictException(Constants.INVALID_TOKEN);

    CollectionResponseModel collectionResponseModel = new CollectionResponseModel();
    List<Department> departmentList = ofy().load().type(Department.class).order("priority").list();
    updateUrlOldVersions(departmentList);
    collectionResponseModel.setDepartmentList(departmentList);
    collectionResponseModel.setTimeStamp(new Date().getTime());
    return collectionResponseModel;

  }

  @ApiMethod(name = "getCategoriesAndSubCategories", path = "/categoryEndpoint/getCategoriesAndSubCategories", httpMethod = ApiMethod.HttpMethod.GET)
  public CollectionResponseModel getCategoriesAndSubCategories(HttpServletRequest req,
                                                               @Named("token") final String token,
                                                               @Named("tokenIdWebSafe") final String tokenIdWebSafe

  ) throws ConflictException, BadRequestException {

    String version = req.getHeader("version");
    String source = req.getHeader("source");

    if (source == null || source.isEmpty()){
      source = RequestSourceEnum.WEB.name();
    }

    if (!authenticate.isValidToken(token, tokenIdWebSafe))
      throw new ConflictException(Constants.INVALID_TOKEN);

    CollectionResponseModel categoriesAndSubCategories = CachedDataManager.categoriesAndSubcategories();

    if (source.equals(RequestSourceEnum.RESPONSIVE.name())){
      return categoriesAndSubCategories;
    }

    if( version == null || ("null").equalsIgnoreCase(version) ) {
      updateUrlOldVersions(categoriesAndSubCategories.getDepartmentList());
    }

    return categoriesAndSubCategories;

  }

  private void updateUrlOldVersions(List<Department> departmentList){
    List<Department> urlDepartments = APIAlgolia.getUrlDepartments();
    for (Department department : departmentList) {
      List<Category> categoryList = ofy().load().type(Category.class).ancestor(department).order("priority").list();
      department.setChildren(categoryList);
      department.setImages(null);
      department.setImages(null);
      Department departmentAlgolia = urlDepartments.stream()
              .filter(p -> p.getId() == department.getId())
              .findFirst().get();
      if(departmentAlgolia != null){
        department.setUrl(departmentAlgolia.getUrl());
      }
    }
  }

  private List<Category> categoryPhotos(List<Category> categoryList, List<CategoryPhoto> categoryPhotos) {
    for (int i = 0; i < categoryPhotos.size(); i++){
        Category category = new Category();
        category.setIdClassification(UUID.randomUUID().toString());
        category.setImage(true);
        category.setCategoryPhoto(categoryPhotos.get(i));
        categoryList.add(category);
    }
    return categoryList;
  }

  @ApiMethod(name = "getSubCategoriesAndFilters", path = "/categoryEndpoint/getSubCategoriesAndFilters", httpMethod = ApiMethod.HttpMethod.GET)
  public CollectionResponseModel getSubCategoriesAndFilters(@Named("token") final String token,
                                                            @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                                            @Named("idCategory") final int idCategory) throws ConflictException, BadRequestException {
    if (!authenticate.isValidToken(token, tokenIdWebSafe))
      throw new ConflictException(Constants.INVALID_TOKEN);

    List<SubCategory> subCategoryList = ofy().load().type(SubCategory.class).filter("parent", idCategory).order("priority").list();
    for (SubCategory subCategory : subCategoryList) {
      //log.warning(Long.toString(subCategory.getId()));
      List<FilterName> filterNameList = ofy().load().type(FilterName.class).filter("idCategory", subCategory.getId()).order("filter").list();
      subCategory.setFilters(filterNameList);
      for (FilterName filterName : filterNameList) {
        List<Filter> filterList = ofy().load().type(Filter.class).ancestor(filterName).order("value").list();
        filterName.setValues(filterList);
      }
    }

    CollectionResponseModel collectionResponseModel = new CollectionResponseModel();
    collectionResponseModel.setSubCategoryList(subCategoryList);
    collectionResponseModel.setTimeStamp(new Date().getTime());
    return collectionResponseModel;
  }


  /**
   * Selection of products of category level class|
   *
   * @param
   * @return Object of class 'CollectionResponseModel'.
   * @throws ConflictException
   */
  @ApiMethod(name = "getProductsFromCategory", path = "/categoryEndpoint/getProductsFromCategory", httpMethod = ApiMethod.HttpMethod.GET)
  public CollectionResponseModel getProductsFromCategory(@Named("token") final String token,
                                                         @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                                         @Named("categoryId") final int categoryId,
                                                         @Named("idStoreGroup") final int idStoreGroupFromRequest,
                                                         @Named("subscribeAndSave") final Boolean subscribeAndSave,
                                                         @Nullable @Named("cursor") String cursor,
                                                         @Nullable @Named("order") Boolean order,
                                                         @Nullable @Named("orderDirection") Boolean orderDirection,
                                                         @Nullable @Named("isWeb") Boolean isWeb)
          throws ConflictException, NoSuchMethodException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, BadRequestException, AlgoliaException {
    if (!authenticate.isValidToken(token, tokenIdWebSafe)) {
      throw new ConflictException(Constants.INVALID_TOKEN);
    }
    int countLimit = 0;

    int idStoreGroup = APIAlgolia.getDefaultStoreIdByStoreId(idStoreGroupFromRequest);
    //log.warning("method: getProductsFromCategory "+idStoreGroup);
    if (order == null)
      order = false;
    if (orderDirection == null)
      orderDirection = false;
    final Integer limitResultsClient = 12;

    //  Query for list items
    Query.Filter filter = new Query.FilterPredicate("subCategories", Query.FilterOperator.EQUAL, categoryId);
    com.googlecode.objectify.cmd.Query<Item> query;
    //log.warning("method: getProductsFromCategory categoryId "+categoryId);
    /*if (subscribeAndSave != null && subscribeAndSave) {
        Query.Filter filterSubscribeAndSave = new Query.FilterPredicate("subscribeAndSave",
              Query.FilterOperator.EQUAL, subscribeAndSave);
        filter = Query.CompositeFilterOperator.and(filter, filterSubscribeAndSave);
    }*/
    if (order) {
      if (orderDirection) {
        query = ofy().load().type(Item.class).filter(filter).order("fullPrice");
      } else {
        query = ofy().load().type(Item.class).filter(filter).order("-fullPrice");
      }
    } else {
      query = ofy().load().type(Item.class).filter(filter).order("-sales");
    }

    CollectionResponseModel collectionResponseModel = new CollectionResponseModel();
    if (isWeb != null && isWeb) {
      //List<Item> totalItems = query.list();
      collectionResponseModel.setTotalProducts((long) query.count());
    }
    query.limit(limitResultsClient);
    //Validate cursor
    if (cursor != null)
      query = query.startAt(Cursor.fromWebSafeString(cursor));

    QueryResultIterator<Item> iterator = query.iterator();

    // Ajuste para mejora del servicio
    List<ItemQuery> listItemQuery = new ArrayList<>();
//    log.info("size -> " + iterator.getIndexList().size());
//    log.info("size limitResultsClient -> " + limitResultsClient);
//    log.info("cout limit  -> " + countLimit);
    //log.info("iterator.hasNext()  -> " + iterator.hasNext());
    boolean iteratorNext = false;
    try {
      iteratorNext = iterator.hasNext();
    }catch (LoadException e){
      log.warning("ObjectifyError -> " + e.getMessage());
    }
    while (iteratorNext && countLimit < limitResultsClient) {
      Item job = iterator.next();
      if (Objects.nonNull(job) && Objects.nonNull(job.getStoreInformation()) && !job.getStoreInformation().isEmpty()) {
        listItemQuery.add(new ItemQuery("" + job.getItemId() + idStoreGroup));
        //log.warning(" Busqueda : "+job.getItemId() + idStoreGroup);
        countLimit++;
      }
      iteratorNext = iterator.hasNext();
    }
    List<Item> items = new ArrayList<>();
    // Consulta todos lo items en algolia
    //log.warning(" Busqueda : Add Item");
    if(Objects.nonNull(listItemQuery) && !listItemQuery.isEmpty()) {
      //log.warning(" Busqueda : Add Item true" );
      List<ItemAlgolia> itemAlgoliaList;
      if (subscribeAndSave != null && subscribeAndSave) {
        itemAlgoliaList = APIAlgolia.findItemByIdList(listItemQuery, subscribeAndSave);
      } else {
        itemAlgoliaList = APIAlgolia.findItemByIdList(listItemQuery);
      }
      items = itemAlgoliaList.stream().filter(itemAlgolia -> Objects.nonNull(itemAlgolia)).map(itemAlgolia -> productsMethods.getItemToItemAlgolia(new Item(), itemAlgolia)).collect(Collectors.toList());
      items.stream().forEach(item -> item.setIdClassification(categoryId));
    }

    Cursor cursorIter = iterator.getCursor();
    cursor = cursorIter.toWebSafeString();
    collectionResponseModel.setNextPageToken(cursor);
    //Collections.sort(items, Item.StockComparator);
    collectionResponseModel.setItems(items);
    return collectionResponseModel;//CollectionResponse.<Item>builder().setItems(items).setNextPageToken(cursor).build();
  }

  /**
   * Selection of products of category level class|
   *
   * @param
   * @return Object of class 'CollectionResponseModel'.
   * @throws ConflictException
   */
  @ApiMethod(
      name = "getProductsFromCategoryAlgolia",
      path = "/categoryEndpoint/getProductsFromCategoryAlgolia",
      httpMethod = ApiMethod.HttpMethod.GET)
  public CollectionResponseModel getProductsFromCategoryAlgolia(
      @Named("token") final String token,
      @Named("tokenIdWebSafe") final String tokenIdWebSafe,
      @Nullable @Named("idCustomerWebSafe") final String idCustomerWebSafe,
      @Named("categoryId") final int categoryId,
      @Named("idStoreGroup") final int idStoreGroupFromRequest,
      @Nullable @Named("subscribeAndSave") final Boolean subscribeAndSave,
      @Nullable @Named("order") Boolean order,
      @Nullable @Named("hitsPerPage") int hitsPerPage,
      @Nullable @Named("page") int page,
      @Nullable @Named("source") final EnableForEnum source,
      @Nullable @Named("isWeb") Boolean isWeb,
      @Nullable @Named("storeId") final String storeId,
      @Nullable @Named("deliveryType") final String deliveryType,
      @Nullable @Named("city") final String city)
      throws ConflictException, BadRequestException, AlgoliaException {

    if (!authenticate.isValidToken(token, tokenIdWebSafe)) {
      throw new ConflictException(Constants.INVALID_TOKEN);
    }

    int idStoreGroup = APIAlgolia.getDefaultStoreIdByStoreId(idStoreGroupFromRequest);
    //log.warning("method: getProductsFromCategory " + idStoreGroup);

    if (isWeb != null && !isWeb) {
      // List<Item> totalItems = query.list();
      hitsPerPage = 1000;
      page = 0;
    }

    HitsItemsAlgolia hitsAllItemsAlgolia = categorieMethods.getAllItemsByCategory(categoryId, idStoreGroup, subscribeAndSave);
//    log.info("Items para esta categoria -> " +
//            (Objects.nonNull(hitsAllItemsAlgolia) && Objects.nonNull(hitsAllItemsAlgolia.getItemAlgoliaList()) ? hitsAllItemsAlgolia.getItemAlgoliaList().size() : 0));

    List<Item> allItems;
    // Consulta todos lo items en algolia

    CollectionResponseModel collectionResponseModel = new CollectionResponseModel();
    if(Objects.nonNull(hitsAllItemsAlgolia) && Objects.nonNull(hitsAllItemsAlgolia.getItemAlgoliaList())) {

      allItems = hitsAllItemsAlgolia.getItemAlgoliaList().stream()
                      .filter(itemAlgolia -> Objects.nonNull(itemAlgolia) && Objects.nonNull(itemAlgolia.getDeliveryPrice()))
                      .map( itemAlgolia -> APIAlgolia.getItemToItemAlgolia(new Item(), itemAlgolia))
                      .collect(Collectors.toList());
      if(Objects.isNull(allItems) || allItems.isEmpty()) {
        collectionResponseModel.setItems(null);
        collectionResponseModel.setPages(0L);
        collectionResponseModel.setCurrentPage(0L);
        collectionResponseModel.setHitsPerPage(0L);
        collectionResponseModel.setCode(hitsAllItemsAlgolia.getCategoryCode());
        return collectionResponseModel;
      }

      allItems.stream().filter(item -> Objects.nonNull(item)).forEach(item -> item.setIdClassification(categoryId));

//      allItems.removeIf(item -> item.getTotalStock() <= 0);

      collectionResponseModel.setTotalProducts(hitsAllItemsAlgolia.getNbHits());

//      log.info("Tamaño lista  -> " + allItems.size());

      //Verificar si hay que ordenar de mayor a menor

      if (Objects.nonNull(order)){
        if (Boolean.TRUE.equals(order)){
//          log.info("Ordernado");
//          allItems = allItems.stream().filter(Objects::nonNull).sorted(Comparator.comparingDouble(Item :: getFullPrice).reversed()).collect(Collectors.toList());
          allItems = allItems.stream().filter(Objects::nonNull).sorted((o1,o2) ->  {
            Double c1 = Objects.nonNull(o1.getOfferPrice()) && o1.getOfferPrice() > 0 ? o1.getOfferPrice() : Optional.of(o1.getFullPrice()).orElse(0.0);
            Double c2 = Objects.nonNull(o2.getOfferPrice()) && o2.getOfferPrice() > 0 ? o2.getOfferPrice() : Optional.of(o2.getFullPrice()).orElse(0.0);
            return c1.compareTo(c2);
          }).collect(Collectors.toList());
        }else {
//            allItems = allItems.stream().filter(Objects::nonNull).sorted(Comparator.comparing(Item :: getFullPrice)).collect(Collectors.toList());
          allItems = allItems.stream().filter(Objects::nonNull).sorted((o1,o2) ->  {
            Double c1 = Objects.nonNull(o1.getOfferPrice()) && o1.getOfferPrice() > 0 ? o1.getOfferPrice() : Optional.of(o1.getFullPrice()).orElse(0.0);
            Double c2 = Objects.nonNull(o2.getOfferPrice()) && o2.getOfferPrice() > 0 ? o2.getOfferPrice() : Optional.of(o2.getFullPrice()).orElse(0.0);
            return c2.compareTo(c1);
          }).collect(Collectors.toList());
        }
      }


      List<Item> nonStockItems = allItems.stream().filter(Objects::nonNull).filter(i -> Objects.nonNull(i.getFullPrice())).filter(i -> Objects.nonNull(i.getOutofstore()) ? (i.getTotalStock() == 0 || i.getOutofstore()) : (i.getTotalStock() == 0 )).collect(Collectors.toList());
      allItems = allItems.stream().filter(Objects::nonNull).filter(i -> Objects.nonNull(i.getFullPrice())).filter(i ->  Objects.nonNull(i.getOutofstore()) ?  i.getTotalStock() != 0 && !i.getOutofstore() : i.getTotalStock() != 0).collect(Collectors.toList());
      // Agregar los items sin stock al final del array
      allItems.addAll(nonStockItems);


//      allItems.stream().filter(item -> Objects.nonNull(item)).sorted(Comparator.comparing(Item::getTotalStock).reversed());

      final int totalPages = Math.round(allItems.size()/hitsPerPage);

      List<Item> items = categorieMethods.splitAndGetPage(allItems, hitsPerPage, page);
      if(Objects.nonNull(idCustomerWebSafe))
      {
        try{
          TalonOneService talonOneService=new TalonOneService();
          Key<User> userKey = Key.create(idCustomerWebSafe);
          User user = users.findUserByKey(userKey);
          talonOneService.sendItemsDirectToTalon(items,user.getId(),tokenIdWebSafe, idCustomerWebSafe,source, TalonAttributes.getTalonOneAttributes(storeId, deliveryType, city, source));
        }catch (Exception e){
          log.warning("Error sending order to TalonOneService: " + Arrays.toString(e.getStackTrace()));
        }
      }

      collectionResponseModel.setItems(items);
      collectionResponseModel.setPages(Long.valueOf(totalPages));
      collectionResponseModel.setCurrentPage((long) page);
      collectionResponseModel.setHitsPerPage(Long.valueOf(hitsPerPage));
      collectionResponseModel.setCode(hitsAllItemsAlgolia.getCategoryCode());
    }
    return collectionResponseModel;
  }

  @ApiMethod(name = "getProductsFromFilter", path = "/categoryEndpoint/getProductsFromFilter", httpMethod = ApiMethod.HttpMethod.POST)
  public CollectionResponseModel getProductsFromFilter(final CategoryJson categoryJson,
                                                       @Nullable @Named("idCustomerWebSafe") final String idCustomerWebSafe,
                                                       @Nullable @Named("isWeb") Boolean isWeb,
                                                       @Nullable @Named("source") final EnableForEnum source,
                                                       @Nullable @Named("storeId") final String storeId,
                                                       @Nullable @Named("deliveryType") final String deliveryType,
                                                       @Nullable @Named("city") final String city)
      throws ConflictException, NoSuchMethodException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, BadRequestException {

    final String METHOD = "[getProductsFromFilter]";
//    log.info("[INI]-"+METHOD);
//
//    log.info("IF (!authenticate.isValidToken(categoryJson.getToken(), categoryJson.getTokenIdWebSafe())) : " +
//                  "["+(!authenticate.isValidToken(categoryJson.getToken(), categoryJson.getTokenIdWebSafe()))+"]");
    if (!authenticate.isValidToken(categoryJson.getToken(), categoryJson.getTokenIdWebSafe())) {
      throw new ConflictException(Constants.INVALID_TOKEN);
    }

    boolean order;
    boolean orderDirection;
    String cursor = categoryJson.getCursor();

    if (categoryJson.getOrder() == null)
      order = false;
    else
      order = categoryJson.getOrder();
    if (categoryJson.getOrderDirection() == null)
      orderDirection = false;
    else
      orderDirection = categoryJson.getOrderDirection();
    final Integer limitResultsClient = 12;

    Query.Filter filterFilter;
    List<Query.Filter> filterList = new ArrayList<>();
    Query.Filter filterFilters;
    if (categoryJson.getFilterIdList().size() > 1) {
      for (Integer integer : categoryJson.getFilterIdList()) {
        filterFilter = new Query.FilterPredicate("filterList", Query.FilterOperator.EQUAL, integer);
        filterList.add(filterFilter);
      }
      filterFilters = Query.CompositeFilterOperator.and(filterList);
    } else {
      filterFilters = new Query.FilterPredicate("filterList", Query.FilterOperator.EQUAL, categoryJson.getFilterIdList().size() > 0 ? categoryJson.getFilterIdList().get(0) : 26);
    }

    Query.Filter filterCategory = new Query.FilterPredicate("subCategories", Query.FilterOperator.EQUAL, categoryJson.getCategoryId());
    //Query.Filter filterStoreGroup = new Query.FilterPredicate("idStoreGroup", Query. FilterOperator.EQUAL, categoryJson.getIdStoreGroup());
    Query.Filter filter = Query.CompositeFilterOperator.and(filterCategory, filterFilters);
    if (categoryJson.getSubscribeAndSave() != null && categoryJson.getSubscribeAndSave()) {
      Query.Filter filterSubscribeAndSave = new Query.FilterPredicate("subscribeAndSave",
              Query.FilterOperator.EQUAL, categoryJson.getSubscribeAndSave());
      filter = Query.CompositeFilterOperator.and(filter, filterSubscribeAndSave);
    }
    com.googlecode.objectify.cmd.Query<Item> query;
    if (order) {
      if (orderDirection) {
        query = ofy().load().type(Item.class).filter(filter).order("fullPrice");
      } else {
        query = ofy().load().type(Item.class).filter(filter).order("-fullPrice");
      }
    } else
      query = ofy().load().type(Item.class).filter(filter).order("-sales");
    List<Item> items = query.list();
    // Se ajusta par evitar java.util.ConcurrentModificationException
    items.removeIf(item -> Objects.isNull(item.getStoreInformation()));

    Integer start = 0;
    Integer finish;
    if (cursor != null)
      start = Integer.parseInt(cursor);

    finish = start + limitResultsClient;
    if (finish > items.size())
      finish = items.size();
    List<Item> itemsToRetrieve = new ArrayList<>();

    if(Objects.nonNull(items) && !items.isEmpty()) {
        List<Item> listItemAlgolia = productsMethods.getItemsByIdsAndStore(items.stream().map(item -> item.getId()).collect(Collectors.toList()), categoryJson.getIdStoreGroup());
        listItemAlgolia.stream().forEach(itemalgolia -> {
          if (Objects.nonNull(categoryJson.getCategoryId()) && Objects.nonNull(itemalgolia)) {
            itemalgolia.setIdClassification(categoryJson.getCategoryId());
          }
        });
        itemsToRetrieve.addAll(listItemAlgolia);
    }

    /*
    for (int i = start; i < finish; i++) {
      Item item = items.get(i);
      for (StoreInformation storeInformation : item.getStoreInformation()) {
          item = productsMethods.setStoreInfo(item, categoryJson.getIdStoreGroup());
          //item = productsMethods.setFindInformationToAlgolia(item, categoryJson.getIdStoreGroup());
          item.setIdClassification(categoryJson.getCategoryId());
      }
      itemsToRetrieve.add(item);
    }*/


    CollectionResponseModel collectionResponseModel = new CollectionResponseModel();
    //Collections.sort(items, Item.StockComparator);

    if(Objects.nonNull(idCustomerWebSafe))
    {
      try{
        TalonOneService talonOneService=new TalonOneService();
        Key<User> userKey = Key.create(idCustomerWebSafe);
        User user = users.findUserByKey(userKey);
        talonOneService.sendItemsDirectToTalon(itemsToRetrieve,user.getId(),categoryJson.getTokenIdWebSafe(),idCustomerWebSafe,source, TalonAttributes.getTalonOneAttributes(storeId, deliveryType, city, source));
      }catch (Exception e){
        log.warning("Error sending order to TalonOneService: " + Arrays.toString(e.getStackTrace()));
      }
    }

    collectionResponseModel.setItems(itemsToRetrieve);
    collectionResponseModel.setNextPageToken(finish.toString());
        /* se comentarea para agreagar otro parametro debido a que este parametro no esta llegando en el json
        if(categoryJson.getWeb() != null && categoryJson.getWeb())
            collectionResponseModel.setTotalProducts((long) items.size());

        if(categoryJson.getConfiWeb() != null && categoryJson.getConfiWeb())
            collectionResponseModel.setTotalProducts((long) (query.count()-cont));*/
    if (isWeb != null && isWeb) {
      //List<Item> totalItems = query.list();
      collectionResponseModel.setTotalProducts((long) query.count());
    }
    return collectionResponseModel;//CollectionResponse.<Item>builder().setItems(itemsToRetrieve).setNextPageToken(finish.toString()).build();
  }

  @ApiMethod(name = "getShortcuts", path = "/categoryEndpoint/getShortcuts", httpMethod = ApiMethod.HttpMethod.GET)
  public CollectionResponseModel getShortcuts(@Named("deparmentId") final int deparmentId)
      throws ConflictException, NoSuchMethodException, ClassNotFoundException, InternalServerErrorException,
      InstantiationException, IllegalAccessException, InvocationTargetException, BadRequestException, IOException {
//    log.info("method: getShortcuts");

    //List<Shortcut> itemList = CoreConnection.getListRequest(URLConnections.SHORTCUT_URL, Shortcut.class);
    List<Shortcut> itemList = ApiGatewayService.get().getShortcutActive();
    CollectionResponseModel collectionResponseModel = new CollectionResponseModel();

    List<Shortcut> shortcutList = new ArrayList<>();
    try {
      for (Shortcut shortcut : itemList) {
        if (shortcut.getClasificationId() == deparmentId) {
//          log.info("method: getShortcuts "+shortcut.getRedirectURL() +" - " +shortcut.getImageURL()+" - " +shortcut.getDescription());
          shortcut.setRedirectURL(shortcut.getRedirectURL().replace("#", ""));
//          log.info("method: getShortcuts "+shortcut.getRedirectURL() +" - " +shortcut.getImageURL()+" - " +shortcut.getDescription());
          shortcutList.add(shortcut);
        }
      }
    } catch (Exception e) {
      log.warning("Error@getShortcuts " + e.getMessage());
    }
    collectionResponseModel.setShortCutList(shortcutList);
    return collectionResponseModel;
  }

  @ApiMethod(name = "getMostSales", path = "/categoryEndpoint/getMostSales", httpMethod = ApiMethod.HttpMethod.GET)
  public CollectionResponseModel getMostSales(@Named("token") final String token,
                                              @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                              @Nullable @Named("idCustomerWebSafe") final String idCustomerWebSafe,
                                              @Nullable @Named("source") final EnableForEnum source,
                                              @Named("departmentId") final long departmentId,
                                              @Named("idStoreGroup") final long idStoreGroupFromRequest,
                                              @Nullable @Named("storeId") final String storeId,
                                              @Nullable @Named("deliveryType") final String deliveryType,
                                              @Nullable @Named("city") final String city)
          throws ConflictException, NoSuchMethodException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, BadRequestException, AlgoliaException {


    if (!authenticate.isValidToken(token, tokenIdWebSafe)) {
      throw new ConflictException(Constants.INVALID_TOKEN);
    }
//    log.info("method: getMostSales departmentId: " + departmentId + " - idStoreGroupFromRequest: " + idStoreGroupFromRequest);
    if(departmentId < 1){
      throw new ConflictException("method getMostSales: departmentId debe ser mayor a CERO");
    }else if (idStoreGroupFromRequest < 1){
      throw new ConflictException("method getMostSales: idStoreGroupFromRequest debe ser mayor a CERO");
    }

    long idStoreGroup = APIAlgolia.getDefaultStoreIdByStoreId((int) idStoreGroupFromRequest);
    Department department = ofy().load().type(Department.class).filter("id", departmentId).first().now();
    if (Objects.nonNull(department)) {
      ItemMostSales itemMostSales = ofy().load().type(ItemMostSales.class).ancestor(department).first().now();
      List<Item> itemsFinal = null;
      if (itemMostSales != null) {
        itemsFinal = productsMethods.getItemsBySuggestedIdsAndStoreGroup(itemMostSales.getSuggested(), idStoreGroup);
      }
      if (itemsFinal == null || itemsFinal.isEmpty()) {
        throw new ConflictException(Constants.NOT_CONTENT);
      }
      CollectionResponseModel collectionResponseModel = new CollectionResponseModel();

      if(Objects.nonNull(idCustomerWebSafe))
      {
        try{
          TalonOneService talonOneService=new TalonOneService();
          Key<User> userKey = Key.create(idCustomerWebSafe);
          User user = users.findUserByKey(userKey);
          talonOneService.sendItemsDirectToTalon(itemsFinal,user.getId(),tokenIdWebSafe,idCustomerWebSafe,source, TalonAttributes.getTalonOneAttributes(storeId, deliveryType, city, source));
        }catch (Exception e){
          log.warning("Error sending order to TalonOneService: " + Arrays.toString(e.getStackTrace()));
        }
      }
      collectionResponseModel.setItems(itemsFinal);

      return collectionResponseModel;
    } else {
      throw new ConflictException(Constants.NOT_CONTENT);
    }
  }

  @ApiMethod(name = "updateCategories", path = "/categoryEndpoint/updateCategories", httpMethod = ApiMethod.HttpMethod.POST)
  public CategoryResponse updateCategories(CategoriesJson categories){

    CategoryResponse response = new CategoryResponse();
    response.setStatus(Constants.FAIL);
    if(Objects.nonNull(categories.getCategories())){

      categories.getCategories().forEach(category -> {

        if(categorieMethods.updateCategory(category)){
          response.addCategoryUpdated(category.getId());
        }else{
          response.addCategoryFail(category.getId());
        }
      });
        response.setStatus(Constants.SUCCESS);
    }

    return response;
  }

}
