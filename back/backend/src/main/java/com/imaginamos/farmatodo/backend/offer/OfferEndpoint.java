package com.imaginamos.farmatodo.backend.offer;

import com.algolia.search.exceptions.AlgoliaException;
import com.google.api.server.spi.config.*;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.ConflictException;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.googlecode.objectify.Key;
import com.imaginamos.farmatodo.networking.algolia.APIAlgolia;
import com.imaginamos.farmatodo.backend.product.ProductsMethods;
import com.imaginamos.farmatodo.backend.user.Authenticate;
import com.imaginamos.farmatodo.model.categories.CategoryJson;
import com.imaginamos.farmatodo.model.categories.SubCategory;
import com.imaginamos.farmatodo.model.offer.Offer;
import com.imaginamos.farmatodo.model.product.Item;
import com.imaginamos.farmatodo.model.product.ItemGroup;
import com.imaginamos.farmatodo.model.product.Suggested;
import com.imaginamos.farmatodo.model.util.CollectionResponseModel;
import com.imaginamos.farmatodo.model.util.Constants;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.imaginamos.farmatodo.backend.OfyService.ofy;

/**
 * Created by Eric on 23/2/2017.
 */

@Api(name = "offerEndpoint",
        version = "v1",
        apiKeyRequired = AnnotationBoolean.TRUE,
        scopes = {Constants.EMAIL_SCOPE},
        clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID_DEVELOPER, com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID},
        audiences = {Constants.ANDROID_AUDIENCE},
        namespace = @ApiNamespace(ownerDomain = Constants.OWNER_DOMAIN, ownerName = Constants.OWNER_NAME),
        description = "Stores, deletes, edits and queries for discount information")
public class OfferEndpoint {


  private static final Logger LOG = Logger.getLogger(OfferEndpoint.class.getName());
  private Authenticate authenticate;
  private ProductsMethods products;


  public OfferEndpoint() {
    this.authenticate = new Authenticate();
    this.products = new ProductsMethods();
  }

  @ApiMethod(name = "getCategories", path = "offerEndpoint/getCategories", httpMethod = ApiMethod.HttpMethod.GET)
  public CollectionResponseModel getCategories(@Named("token") final String token,
                                               @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                               @Nullable @Named("getCategories") final Boolean getCategories) throws ConflictException, BadRequestException {
    if (!authenticate.isValidToken(token, tokenIdWebSafe))
      throw new ConflictException(Constants.INVALID_TOKEN);

    CollectionResponseModel collectionResponseModel = new CollectionResponseModel();
    List<SubCategory> subCategories = ofy().load().type(SubCategory.class).list();
    List<SubCategory> subCategoryList = new ArrayList<>();

    subCategoryList.addAll(subCategories);

    collectionResponseModel.setSubCategoryList(subCategoryList);
    collectionResponseModel.setTimeStamp(new Date().getTime());

    return collectionResponseModel;
  }

  @ApiMethod(name = "getOfferUniverse", path = "offerEndpoint/getOfferUniverse", httpMethod = ApiMethod.HttpMethod.GET)
  public CollectionResponseModel getOfferUniverse(@Named("token") final String token,
                                                  @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                                  @Nullable @Named("idStoreGroup") final long idStoreGroupFromRequest,
                                                  @Nullable @Named("isWeb") final Boolean isWeb,
                                                  @Nullable @Named("cursor") String cursor) throws ConflictException, BadRequestException, AlgoliaException {
//    LOG.warning("method: getOfferUniverse()");

    if (!authenticate.isValidToken(token, tokenIdWebSafe))
      throw new ConflictException(Constants.INVALID_TOKEN);

    long idStoreGroup = APIAlgolia.getDefaultStoreIdByStoreId((int)idStoreGroupFromRequest);

    CollectionResponseModel collectionResponseModel = new CollectionResponseModel();
    int countLimit = 0;
    final Integer limitResultsClient = 12;
    List<Offer> offerList = new ArrayList<>();
    long current = System.currentTimeMillis();
    Key<ItemGroup> itemGroupKey = Key.create(ItemGroup.class, "1");
    com.googlecode.objectify.cmd.Query<Offer> query = ofy().load().type(Offer.class).order("orderingNumber");
    query.limit(limitResultsClient);

    if (isWeb != null && isWeb) {
      collectionResponseModel.setTotalProducts((long) query.count());
    }

    //Validate cursor
    if (cursor != null)
      query = query.startAt(Cursor.fromWebSafeString(cursor));

    QueryResultIterator<Offer> iterator = query.iterator();

    while (iterator.hasNext() && countLimit < limitResultsClient) {
      Offer offer = iterator.next();
      countLimit++;
      if (offer.getStartDate() < current && current <= offer.getEndDate()) {
        if (offer.getType().equals("UNIQUE")) {
          Item item =  products.setFindInformationToAlgoliaByIdItem(Long.toString(offer.getItems().get(0).getItem()) , idStoreGroup, null);
          if (item != null) {
            List<Item> unique = new ArrayList<>();
            unique.add(item);
            offer.setProduct(unique);
            offer.setItem(offer.getItems().get(0).getItem());
          }
        }
        offerList.add(offer);
      }
    }
    Cursor cursorIter = iterator.getCursor();
    cursor = cursorIter.toWebSafeString();
    collectionResponseModel.setNextPageToken(cursor);
    collectionResponseModel.setOfferList(offerList);
    //LOG.warning("method: getOfferUniverse() -> Success");
    return collectionResponseModel;
  }


  @ApiMethod(name = "getOffers", path = "offerEndpoint/getOffers", httpMethod = ApiMethod.HttpMethod.GET)
  public CollectionResponse<Offer> getOffers(@Named("token") final String token,
                                             @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                             @Nullable @Named("idStoreGroup") final long idStoreGroupFromRequest) throws ConflictException, BadRequestException, AlgoliaException {
    if (!authenticate.isValidToken(token, tokenIdWebSafe))
      throw new ConflictException(Constants.INVALID_TOKEN);

    long idStoreGroup = APIAlgolia.getDefaultStoreIdByStoreId((int)idStoreGroupFromRequest);

    List<Offer> offers = ofy().load().type(Offer.class).order("orderingNumber").list();
    List<Offer> offerList = new ArrayList<>();
    long current = System.currentTimeMillis();
    Key<ItemGroup> itemGroupKey = Key.create(ItemGroup.class, "1");
    for (Offer offer : offers) {
      if (offer.getStartDate() < current && current <= offer.getEndDate()) {
        if (offer.getType().equals("UNIQUE")) {
          Item item =  products.setFindInformationToAlgoliaByIdItem(Long.toString(offer.getItems().get(0).getItem()) , idStoreGroup, null);
          List<Item> unique = new ArrayList<>();
          unique.add(item);
          offer.setProduct(unique);
          offer.setItem(offer.getItems().get(0).getItem());
        }
        offerList.add(offer);
      }
    }

    return CollectionResponse.<Offer>builder().setItems(offerList).setNextPageToken(null).build();
  }


  @ApiMethod(name = "getOfferItems", path = "offerEndpoint/getOfferItems", httpMethod = ApiMethod.HttpMethod.GET)
  public CollectionResponseModel getOfferItems(@Named("token") final String token,
                                               @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                               @Named("idOffer") final long idOffer,
                                               @Named("idStoreGroup") final long idStoreGroupFromRequest,
                                               @Nullable @Named("isWeb") final Boolean isWeb,
                                               @Nullable @Named("cursor") String cursor) throws ConflictException, BadRequestException, AlgoliaException {

    if (!authenticate.isValidToken(token, tokenIdWebSafe))
      throw new ConflictException(Constants.INVALID_TOKEN);
    Offer offer = ofy().load().type(Offer.class).filter("id", idOffer).first().now();
    if (offer == null)
      throw new ConflictException(Constants.OFFER_NOT_FOUND);
    final Integer limitResultsClient = 12;

    long idStoreGroup = APIAlgolia.getDefaultStoreIdByStoreId((int)idStoreGroupFromRequest);
    //LOG.warning("method: getOfferItems idOffer:"+idOffer+" idStoreGroup:"+idStoreGroup);

    List<Item> items = getItemsByIdOffer(idOffer, idStoreGroup, true);
    Integer start = 0;
    Integer finish;
    if (cursor != null)
      start = Integer.parseInt(cursor);

    finish = start + limitResultsClient;
    if (finish > items.size())
      finish = items.size();
    List<Item> itemsToRetrieve = new ArrayList<>();
    for (int i = start; i < finish; i++) {
      Item item = items.get(i);
      itemsToRetrieve.add(item);
    }
    CollectionResponseModel collectionResponseModel = new CollectionResponseModel();
    collectionResponseModel.setItems(itemsToRetrieve);
    collectionResponseModel.setNextPageToken(finish.toString());
    if (isWeb != null && isWeb)
      collectionResponseModel.setTotalProducts((long) items.size());
    return collectionResponseModel;
  }

  @ApiMethod(name = "getOffer", path = "offerEndpoint/getOffer", httpMethod = ApiMethod.HttpMethod.GET)
  public Offer getOffer(@Named("token") final String token,
                        @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                        @Named("idOffer") final long idOffer)
          throws ConflictException, BadRequestException {
    if (!authenticate.isValidToken(token, tokenIdWebSafe))
      throw new ConflictException(Constants.INVALID_TOKEN);
    return ofy().load().type(Offer.class).filter("id", idOffer).first().now();
  }

  @ApiMethod(name = "getOfferItemsByFiltersAndPrice", path = "offerEndpoint/getOfferItemsByFiltersAndPrice", httpMethod = ApiMethod.HttpMethod.POST)
  public CollectionResponseModel getOfferItemsByFiltersAndPrice(final CategoryJson categoryJson,
                                                                @Nullable @Named("isWeb") final Boolean isWeb) throws ConflictException, BadRequestException {
    //LOG.warning("method: getOfferItemsByFiltersAndPrice INIT categoryJson= " + categoryJson);
//    LOG.info("IF (!authenticate.isValidToken(categoryJson.getToken(), categoryJson.getTokenIdWebSafe())) : " +
//            "[" + (!authenticate.isValidToken(categoryJson.getToken(), categoryJson.getTokenIdWebSafe())) + "]");
    if (!authenticate.isValidToken(categoryJson.getToken(), categoryJson.getTokenIdWebSafe())) {
      throw new ConflictException(Constants.INVALID_TOKEN);
    }
    final Integer limitResultsClient = 12;
    boolean orderDirection;
    String cursor = categoryJson.getCursor();

    if (categoryJson.getOrderDirection() == null)
      orderDirection = false;
    else
      orderDirection = categoryJson.getOrderDirection();

    List<Item> items = getItemsByIdOffer(categoryJson.getIdOffer(), categoryJson.getIdStoreGroup(), orderDirection);
    List<Integer> filterListId = categoryJson.getFilterIdList();
    // Valida Filtros
    List<Item> itemsFiltrados = new ArrayList<>();
    if ((Objects.nonNull(filterListId) && !filterListId.isEmpty()) || categoryJson.getLowPrice() > 0 || categoryJson.getHighPrice() > 0) {
      if(Objects.nonNull(filterListId) && !filterListId.isEmpty()) {
        //LOG.warning("method: getOfferItemsByFiltersAndPrice idFiltro= " + filterListId);
        for (Item item : items) {
          for (Integer id : item.getFilterList()) {
            //LOG.warning("method: getOfferItemsByFiltersAndPrice idFiltroItem = " + id);
            if (filterListId.contains(id)) {
              //LOG.warning("method: getOfferItemsByFiltersAndPrice Agrega Item: " + item.getId());
              itemsFiltrados.add(item);
            } else {
              LOG.warning("method: getOfferItemsByFiltersAndPrice No Agrega Item: " + item.getId());
            }
          }
        }
        //LOG.warning("method: getOfferItemsByFiltersAndPrice idFiltro size= " + itemsFiltrados.size());
        //LOG.warning("method: getOfferItemsByFiltersAndPrice filterListId size= " + filterListId.size());
      }
      if (categoryJson.getLowPrice() > 0) {
        //LOG.warning("method: getOfferItemsByFiltersAndPrice filtro Low Price= " + categoryJson.getLowPrice());
        if(Objects.nonNull(itemsFiltrados) && !itemsFiltrados.isEmpty()) {
          itemsFiltrados = itemsFiltrados.stream().filter(item -> item.getFullPrice() >= categoryJson.getLowPrice()).collect(Collectors.toList());
        }else{
          itemsFiltrados = items.stream().filter(item -> item.getFullPrice() >= categoryJson.getLowPrice()).collect(Collectors.toList());
        }
      }
      if (categoryJson.getHighPrice() > 0) {
        //LOG.warning("method: getOfferItemsByFiltersAndPrice Filtro high Price= " + categoryJson.getHighPrice());
        if(Objects.nonNull(itemsFiltrados) && !itemsFiltrados.isEmpty()) {
          itemsFiltrados = itemsFiltrados.stream().filter(item -> item.getFullPrice() <= categoryJson.getHighPrice()).collect(Collectors.toList());
        }else{
          itemsFiltrados = items.stream().filter(item -> item.getFullPrice() <= categoryJson.getHighPrice()).collect(Collectors.toList());
        }
      }
    } else {
      itemsFiltrados = items;
    }
    Integer start = 0;
    Integer finish;
    List<Item> itemsToRetrieve = new ArrayList<>();
    if (cursor != null)
      start = Integer.parseInt(cursor);

    finish = start + limitResultsClient;
    if (finish > itemsFiltrados.size())
      finish = itemsFiltrados.size();
    for (int i = start; i < finish; i++) {
      Item item = itemsFiltrados.get(i);
      itemsToRetrieve.add(item);
    }
    CollectionResponseModel collectionResponseModel = new CollectionResponseModel();
    collectionResponseModel.setItems(itemsToRetrieve);
    collectionResponseModel.setNextPageToken(finish.toString());
    if (isWeb != null && isWeb) {
      collectionResponseModel.setTotalProducts((long) itemsFiltrados.size());
    }
    return collectionResponseModel;
  }

  private List<Item> getItemsByIdOffer(final long idOffer, final long idStoreGroup, final boolean orderDirection) throws ConflictException {
    if (idOffer == 0 || idStoreGroup == 0)
      throw new ConflictException(Constants.OFFER_NOT_FOUND);
    //LOG.warning("method: getItemsByIdOffer idOffer= "+ idOffer);
    Offer offer = ofy().load().type(Offer.class).filter("id", idOffer).first().now();
    if (offer == null)
      throw new ConflictException(Constants.OFFER_NOT_FOUND);
    List<Suggested> itemsOffer = offer.getItems();
    //Busqueda en Algolia
    List<Item> listItem = new ArrayList<>();
    try {
      listItem = products.getItemsBySuggestedIdsAndStoreGroup(itemsOffer,  idStoreGroup);
    } catch (Exception e) {
      LOG.warning("Error@getItemsByIdOffer " + e.getMessage());
      return listItem;
    }
    if(orderDirection) {
      listItem.sort((item1, item2) -> Double.compare(item1.getFullPrice(), item2.getFullPrice()));
    }else {
      listItem.sort((item1, item2) -> Double.compare(item2.getFullPrice(), item1.getFullPrice()));
    }
    return listItem;
  }

}
