package com.imaginamos.farmatodo.backend.algolia;

import com.algolia.search.exceptions.AlgoliaException;
import com.google.api.server.spi.config.*;
import com.google.api.server.spi.response.BadRequestException;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.imaginamos.farmatodo.backend.product.ProductsMethods;
import com.imaginamos.farmatodo.model.algolia.*;
import com.imaginamos.farmatodo.model.algolia.autocomplete.AutocompleteByCityConfig;
import com.imaginamos.farmatodo.model.algolia.redis.RedisDeleteKeyRequest;
import com.imaginamos.farmatodo.model.customer.Customer;
import com.imaginamos.farmatodo.model.order.DeliveryOrder;
import com.imaginamos.farmatodo.model.order.DeliveryOrderItem;
import com.imaginamos.farmatodo.model.order.FulfilOrdColDescDomain;
import com.imaginamos.farmatodo.model.product.Item;
import com.imaginamos.farmatodo.model.util.Answer;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.networking.algolia.APIAlgolia;
import com.imaginamos.farmatodo.networking.cache.CachedDataManager;
import com.imaginamos.farmatodo.networking.models.algolia.ItemMultiqueryList;
import migration.algolia.AlgoliaManager;
import migration.algolia.AlgoliaManagerHandler;
import retrofit2.http.Body;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.imaginamos.farmatodo.backend.OfyService.ofy;

/**
 * Created by JPuentes on 17/10/2018.
 */
@Api(name = "algoliaEndpoint",
        version = "v1",
        scopes = {Constants.EMAIL_SCOPE},
        apiKeyRequired = AnnotationBoolean.TRUE,
        clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID_DEVELOPER, com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID},
        audiences = {Constants.ANDROID_AUDIENCE},
        namespace = @ApiNamespace(ownerDomain = Constants.OWNER_DOMAIN, ownerName = Constants.OWNER_NAME),
        description = "Algolia API from AppEngine")
public class AlgoliaEndpoint {
    private ProductsMethods productsMethods;
    private AlgoliaManager algoliaManager;

    public AlgoliaEndpoint() {
        productsMethods = new ProductsMethods();
        algoliaManager = new AlgoliaManagerHandler();
    }
    private static final Logger LOG = Logger.getLogger(AlgoliaEndpoint.class.getName());

    @ApiMethod(name = "getRecomendedItem", path = "/algoliaEndpoint/getRecomendedItem", httpMethod = ApiMethod.HttpMethod.GET)
    public GetRecommendedItemResponse getRecommendedItem(@Named("store") final Integer store){
        GetRecommendedItemResponse response = null;
        try{
            if(store==null)
                return new GetRecommendedItemResponse("Bad Request",400,"store required",null);

            if(store<1)
                return new GetRecommendedItemResponse("Bad Request",400,"store must be greater than zero",null);

            List<AlgoliaItem> recommededItems = new ArrayList<>();

//            LOG.info("result from algolia");
//            LOG.info("ApacheAPIClientBuilder");
            List<RecommendedItem> items = APIAlgolia.getChoiseProd(store);
//            LOG.info("algoliaClient.initIndex(AlgoliaIndexEnum.FARMATODO_CHOICE_PROD");
            if(items!=null){
                if(!items.isEmpty()){
                    for(RecommendedItem item : items){
                        List<AlgoliaItem> hits = APIAlgolia.getItemAlgolia(item.getStore());
                        if(hits!=null){
                            if(!hits.isEmpty()){
                                for(AlgoliaItem i : hits){
                                    if(i.getId().equals(String.valueOf(item.getItemId()))){
                                        recommededItems.add(i);
                                    }
                                }
                            }
                        }
                    }

                    if(recommededItems.isEmpty()){
                        return new GetRecommendedItemResponse("Not Content",204,"No item recommended for the specified store",null);
                    }
                    return new GetRecommendedItemResponse("Ok",200,"success",recommededItems);
                }else {
                    return new GetRecommendedItemResponse("Not Content",204,"No recommended items",null);
                }
            }else{
                return new GetRecommendedItemResponse("Not Content",204,"No recommended items",null);
            }
        }catch (Exception e){
            return new GetRecommendedItemResponse("Internal Server Error",500,"Unexpected error. Message:"+e.getMessage(),null);
        }
    }

    @ApiMethod(name = "addRecomendedItem", path = "/algoliaEndpoint/addRecomendedItem", httpMethod = ApiMethod.HttpMethod.POST)
    public AddRecommendedItemResponse addRecommendedItem(final AddRecommendedItemRequest request) {
        GetRecommendedItemResponse response = null;
        try {
            if (request == null)
                return new AddRecommendedItemResponse("Bad Request", 400, "request required");

            if (request.getStore() == null)
                return new AddRecommendedItemResponse("Bad Request", 400, "store required");

            if (request.getStore() < 1)
                return new AddRecommendedItemResponse("Bad Request", 400, "store must be greater than zero");

            if (request.getItemId() == null)
                return new AddRecommendedItemResponse("Bad Request", 400, "itemId required");

            if (request.getItemId() < 1)
                return new AddRecommendedItemResponse("Bad Request", 400, "itemId must be greater than zero");

            RecommendedItem recommendedItem = new RecommendedItem();
            recommendedItem.setItemId(request.getItemId());
            recommendedItem.setStore(request.getStore());
            recommendedItem.setObjectID(Integer.toString(request.getItemId())+Integer.toString(request.getStore()));
            APIAlgolia.addFarmatodoChoiseProd(recommendedItem);

            return new AddRecommendedItemResponse("Ok", 200, "Recommended Item added");

        }catch (Exception e){
            return new AddRecommendedItemResponse("Internal Server Error",500,"Unexpected error. Message:"+e.getMessage());
        }
    }

    @Deprecated //use getAdvisedFullItem
    @ApiMethod(name = "getAdvisedItem", path = "/algoliaEndpoint/getAdvisedItem", httpMethod = ApiMethod.HttpMethod.GET)
    public GetRecommendedItemResponse getAdvisedItem(@Nullable @Named("idCustomerWebSafe") final String idCustomerWebSafe,
                                                     @Nullable @Named("token") final String token,
                                                     @Nullable @Named("tokenIdWebSafe") final String tokenIdWebSafe){

        try {
            Optional<RecommendConfig> recommendConfig = APIAlgolia.getAlgoliaRecommendConfig();
            if(recommendConfig.isPresent() && recommendConfig.get().isAdvisedItems() &&idCustomerWebSafe!=null) {
                List<DeliveryOrderItem> listItems = deliveryOrderItemListInCart(idCustomerWebSafe);
                if (listItems != null && !listItems.isEmpty()) {
                    List<Item> itemInfo = findInformationItem(listItems);
                    String department = obtainDepartment(itemInfo);
                    AdvisedItem advisedItem = getAdvisedItemFromRecommended(department);
                    if (advisedItem != null && advisedItem.getItems() != null && !advisedItem.getItems().isEmpty()) {
                        return new GetRecommendedItemResponse("Ok", 200, "Success", advisedItem);
                    }
                }
            }
        } catch (Exception e) {
            LOG.warning("Error findInformationItem{}"+e.getMessage());
        }
        try {
            AdvisedItem advisedItem = APIAlgolia.getAdvisedItems();

            if(advisedItem!=null)
                return new GetRecommendedItemResponse("Ok",200,"Success",advisedItem);

            return new GetRecommendedItemResponse("Not Content",204,"No advised items found",null);

        }catch(Exception e){
            return new GetRecommendedItemResponse("Internal Server Error",500,"Error",null);
        }
    }

    @ApiMethod(name = "getAdvisedFullItem", path = "/algoliaEndpoint/getAdvisedFullItem", httpMethod = ApiMethod.HttpMethod.GET)
    public GetRecommendedItemResponse getAdvisedFullItem( @Named("idStoreGroup") final long idStoreGroup) {
        try {
            AdvisedItem advisedItem = APIAlgolia.getAdvisedItems();
            List<String> listItems = new ArrayList<>();
            if (advisedItem != null && advisedItem.getItems() != null) {
                advisedItem.getItems().forEach(x -> listItems.add(String.valueOf(x).concat(String.valueOf(idStoreGroup))));
                List<ItemAlgolia> listItemsAlgolia = APIAlgolia.getItemsFromIds(listItems);
                if (listItemsAlgolia != null) {
                    List<ItemAlgolia> listWithStock = listItemsAlgolia.stream().filter(x -> x.getStock() > 0).collect(Collectors.toList());
                    return new GetRecommendedItemResponse("Ok", 200, "Success", listWithStock);
                }
            }
            return new GetRecommendedItemResponse("Not Content",204,"No advised items found",null);
        } catch(Exception e) {
            return new GetRecommendedItemResponse("Internal Server Error",500,"Error",null);
        }
    }

    @ApiMethod(name = "saveCartDeliveryLabelConfig", path = "/algoliaEndpoint/saveCartDeliveryLabelConfig", httpMethod = ApiMethod.HttpMethod.POST)
    public CartDeliveryLabelConfigResponse saveCartDeliveryLabelConfig(final CartDeliveryLabelConfig cartDeliveryLabelConfig) {
        try {
            APIAlgolia.saveCartDeliveryLabelConfig(cartDeliveryLabelConfig);
            return new CartDeliveryLabelConfigResponse("OK", 200, "success", cartDeliveryLabelConfig);
        } catch (Exception e) {
            return new CartDeliveryLabelConfigResponse("Internal Server Error",500,"Error",null);
        }
    }

    @ApiMethod(name = "getCartDeliveryLabelConfig", path = "/algoliaEndpoint/getCartDeliveryLabelConfig", httpMethod = ApiMethod.HttpMethod.GET)
    public CartDeliveryLabelConfigResponse getCartDeliveryLabelConfig(@Named("storeId") final Integer storeId) {
        try {
            Optional<CartDeliveryLabelConfig> cartDeliveryLabelConfigOptional = APIAlgolia.getCartDeliveryLabelConfig();
            if (cartDeliveryLabelConfigOptional.isPresent()) {
                List<CartDeliveryLabelConfigValue> values = cartDeliveryLabelConfigOptional.get().getValues().stream().filter(config -> config.getStoreId() == storeId).
                        collect(Collectors.toList());
                CartDeliveryLabelConfig response = new CartDeliveryLabelConfig();
                response.setValues(values);
                return new CartDeliveryLabelConfigResponse("OK", 200, "success", response);
            }
        } catch (Exception e) {
            return new CartDeliveryLabelConfigResponse("Internal Server Error",500,"Error",null);
        }
        return new CartDeliveryLabelConfigResponse("No Content",204,"Doesn't exist the configuration",null);
    }

    @ApiMethod(name = "getCartDeliveryLabelConfigByStoreId", path = "/algoliaEndpoint/getCartDeliveryLabelConfigByStoreId", httpMethod = ApiMethod.HttpMethod.GET)
    public CartDeliveryLabelConfigResponse getCartDeliveryLabelConfigByStoreId(@Named("storeId") final Integer storeId) {
        try {
            CartDeliveryLabelConfigValueResp cartDeliveryLabelConfig = APIAlgolia.getCartDeliveryLabelConfigByStoreId(storeId);
            return Objects.nonNull(cartDeliveryLabelConfig) ?
                    new CartDeliveryLabelConfigResponse("OK", 200, "success", cartDeliveryLabelConfig)
                    : new CartDeliveryLabelConfigResponse("Doesn't exist configuration",200,"Error",null);
        } catch (Exception e) {
            return new CartDeliveryLabelConfigResponse("Internal Server Error",500,"Error",null);
        }
    }

    @ApiMethod(name = "getDeliveryTimeLabelConfig", path = "/algoliaEndpoint/getDeliveryTimeLabelConfig", httpMethod = ApiMethod.HttpMethod.GET)
    public DeliveryTimeLabelConfigResponse getDeliveryTimeLabelConfig() {
        try {
            Optional<DeliveryTimeLabelConfig> deliveryTimeLabelConfigOptional = APIAlgolia.getDeliveryTimeLabelConfig();
            DeliveryTimeLabelConfig data = null;


            if (deliveryTimeLabelConfigOptional.isPresent()) {
                data =  deliveryTimeLabelConfigOptional.get();
                return new DeliveryTimeLabelConfigResponse("OK", 200, "success", data);
            }

            return new DeliveryTimeLabelConfigResponse("OK", 204, "Doesn't exist the configuration", null);

        } catch (Exception e) {
            return new DeliveryTimeLabelConfigResponse("Internal Server Error", 500, "Error", null);
        }
    }

    @ApiMethod(name = "getAutocompleteByCityConfig", path = "/algolia/autocomplete", httpMethod = ApiMethod.HttpMethod.GET)
    public AutocompleteByCityConfig getAutocompleteByCityConfig() {
        try {
//            LOG.info("method: getAutocompleteByCityConfig()");
            AutocompleteByCityConfig config = APIAlgolia.getAutocompleteConfig();
            //LOG.info("AutocompleteByCityConfig : " + config);
            return config;
        } catch (Exception e) {
            if (e!=null) {
                e.printStackTrace();
                LOG.warning("Exception in getAutocompleteByCityConfig(). Message: " + e.getMessage());
            }
            return null;
        }
    }

    @ApiMethod(name = "testUpdateStockSIM", path = "/algolia/testUpdateStockSIM", httpMethod = ApiMethod.HttpMethod.POST)
    public Answer testUpdateStockSIM(final FulfilOrdColDescDomain fulfilOrdColDescDomain) throws AlgoliaException {
        Answer answer = new Answer();

        Optional<Boolean> updateSTOCK = APIAlgolia.updateAlgoliaStock(fulfilOrdColDescDomain.getFulfilOrdDesc());

        answer.setConfirmation(updateSTOCK.isPresent() && updateSTOCK.get());
        answer.setMessage("Success");

        return answer;
    }

    @ApiMethod(name = "getAlgoliaItemsByCity", path = "/algolia/getAlgoliaItemsByCity", httpMethod = ApiMethod.HttpMethod.GET)
    public List<ItemAlgolia> getAlgoliaItemsByCity(@Named("city") String city, @Named("text") String text) throws AlgoliaException {

        if(city != null && text != null){
            return APIAlgolia.itemsByCity(city,text);
        }

    return new ArrayList<>();
    }


    @ApiMethod(name = "getAlgoliaItemByBarcode", path = "/algolia/getItemByBarcode", httpMethod = ApiMethod.HttpMethod.GET)
    public ItemAlgolia getAlgoliaItemByBarcode(@Named("barcode") String barcode, @Named("idStoreGroup") int idStoreGroup) throws BadRequestException {

        if ( barcode == null || barcode.isEmpty() || idStoreGroup <= 0 ){
            throw new BadRequestException(Constants.INVALID_TOKEN);
        }
        return APIAlgolia.getItemAlgoliaByBarcode(barcode, idStoreGroup);
    }

    @ApiMethod(name = "getAlgoliaItemV3", path = "/algoliaEndpoint/getItem", httpMethod = ApiMethod.HttpMethod.GET)
    public ItemAlgolia getAlgoliaItem(@Named("id") String id) throws BadRequestException {

        if ( id == null || id.isEmpty() ){
            throw new BadRequestException(Constants.INVALID_TOKEN);
        }
        ItemAlgolia itemAlgolia;
        itemAlgolia = APIAlgolia.getItemAlgoliaByBarcode(id, 26L);
        if (itemAlgolia != null){
            return itemAlgolia;
        }
        itemAlgolia = APIAlgolia.getItemAlgolia(id + 26L);

        if (itemAlgolia == null){
            throw  new BadRequestException(Constants.PRODUCT_NOT_EXISTS);
        }
        GuardAlgolia.validationItemsAlgolia(itemAlgolia);
        return itemAlgolia;
    }

/*    @ApiMethod(name = "getSeoTest", path = "/algolia/getSeoTest", httpMethod = ApiMethod.HttpMethod.GET)
    public  List<Object> getSeoTest() throws BadRequestException {

        List<String> seoIds = Arrays.asList("94","93");

        List<JSONObject> jsonObjectList = APIAlgolia
                .getJsonListObjByObjectIDs(seoIds, "categories_seo");

        List<Object> departmentsObjectSeo = new ArrayList<>();

        jsonObjectList.forEach( jsonObject -> {

            Optional<Object> testData = Optional.ofNullable(jsonObject.get("test"));
            if (testData.isPresent()) {
                LOG.info("item ->  " + testData.get());
                departmentsObjectSeo.add(jsonObject);
            }

        });



        return departmentsObjectSeo;

    }*/


    @ApiMethod(name = "getAlgoliaItemsMultiquery", path = "/algolia/getAlgoliaItemsMultiquery", httpMethod = ApiMethod.HttpMethod.POST)
    public List<ItemAlgolia> getAlgoliaItemsMultiquery(@Body ItemMultiqueryList itemList) throws AlgoliaException, IOException, IOException {
        return APIAlgolia.getItemsMultiquery(itemList.getIds(), itemList.getIdStore());
    }

    @ApiMethod(name = "refreshProperty", path = "/algoliaEndpoint/refreshProperty", httpMethod = ApiMethod.HttpMethod.POST)
    public Answer refreshProperty(final RedisDeleteKeyRequest redisDeleteKeyRequest) {
        Answer answer = new Answer();
        answer.setConfirmation(Boolean.FALSE);
        if (redisDeleteKeyRequest == null)
            return answer;

        if (redisDeleteKeyRequest.getObjectId() == null || redisDeleteKeyRequest.getObjectId().isEmpty())
            return answer;

        boolean deletedKey = CachedDataManager.deleteKey(redisDeleteKeyRequest.getObjectId());
        answer.setConfirmation(deletedKey);

        if (deletedKey){
            answer.setMessage("Refresh Success");
        }

        return answer;
    }

    @ApiMethod(name = "testAlgoliaCache", path = "/algoliaEndpoint/testAlgoliaCache", httpMethod = ApiMethod.HttpMethod.GET)
    public Object testAlgoliaCache() throws AlgoliaException {
        return  APIAlgolia.getSelfCheckout();
    }

    private List<DeliveryOrderItem> deliveryOrderItemListInCart (String idCustomerWebSafe){
        Key<Customer> customerKey = Key.create(idCustomerWebSafe);

        DeliveryOrder deliveryOrderSaved = ofy().load().type(DeliveryOrder.class).filter("currentStatus", 1).ancestor(Ref.create(customerKey)).first().now();
        if (deliveryOrderSaved == null){
            return null;
        }
        List<DeliveryOrderItem> deliveryOrderItemList = ofy().load().type(DeliveryOrderItem.class).ancestor(deliveryOrderSaved).list();

        if(deliveryOrderItemList==null||deliveryOrderItemList.isEmpty()){
            return null;
        }
//        LOG.info("deliveryOrderItemListInCart -> " + deliveryOrderItemList.size());
        return deliveryOrderItemList;
    }

    private List<com.imaginamos.farmatodo.model.product.Item> findInformationItem(List<DeliveryOrderItem> listItems) {
        List<com.imaginamos.farmatodo.model.product.Item> listItemsInfo = new ArrayList<>();
        for (DeliveryOrderItem deliveryOrderItem : listItems) {
            com.imaginamos.farmatodo.model.product.Item algoliaItems = productsMethods.setFindInformationToAlgoliaByIdItem(String.valueOf(deliveryOrderItem.getId()), 26, null);
            if (algoliaItems != null) {
                listItemsInfo.add(algoliaItems);
            }
        }
        return listItemsInfo;
    }

    private String obtainDepartment(List<Item> listItems) {
        List<String> listDepartament = new ArrayList<>();
        listDepartament =listItems.stream().filter(item -> item.getDepartments() != null &&
                !item.getDepartments().isEmpty()).map(item -> item.getDepartments().get(0)).collect(Collectors.toList());

        Map<String, Integer> map = new HashMap<>();
        for (String s : listDepartament) {
            Integer count = map.get(s);
            map.put(s, (count == null) ? 1 : count + 1);
        }
        String department = "";
        int max = 0;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                department = entry.getKey();
            }
        }
        return department;

    }
    private AdvisedItem getAdvisedItemFromRecommended(String department) {
        List<Integer> listItems = APIAlgolia.getAdvisedItemFromRecommended(department);
        AdvisedItem advisedItem = new AdvisedItem();
        if (listItems == null || listItems.isEmpty()) {
            return null;
        }
        advisedItem.setItems(listItems);
        return advisedItem;
    }

    @ApiMethod(name = "testAlgoliaCache2", path = "/algoliaEndpoint/testAlgoliaCache2", httpMethod = ApiMethod.HttpMethod.GET)
    public Object testAlgoliaCache2() throws AlgoliaException, ExecutionException, InterruptedException {
        CompletableFuture<List<ItemAlgolia>> future1 = CompletableFuture.supplyAsync(() -> this.getListOfProducts());
        CompletableFuture<List<ItemAlgolia>> future2 = CompletableFuture.supplyAsync(() -> this.getListOfProducts());
        CompletableFuture<List<ItemAlgolia>> future3 = CompletableFuture.supplyAsync(() -> this.getListOfProducts());
        CompletableFuture<List<ItemAlgolia>> future4 = CompletableFuture.supplyAsync(() -> this.getListOfProducts());
        CompletableFuture<Void> futures = CompletableFuture.allOf(future1, future2, future3, future4);
        while (!futures.isDone()) {
            Thread.sleep(50);
        }
//        LOG.info("Size of ()" + future1.get().size());
//        LOG.info("Size of ()" + future2.get().size());
//        LOG.info("Size of ()" + future3.get().size());
//        LOG.info("Size of ()" + future4.get().size());
        return  APIAlgolia.getSelfCheckout();
    }

    private List<ItemAlgolia> getListOfProducts() {
        List<String> items = new ArrayList<>();
        items.add("100000126");
        items.add("100000226");
        items.add("100000326");
        items.add("100000426");
        items.add("100000526");
        items.add("100000626");
        items.add("100000726");
        items.add("100000826");
        items.add("100000926");
        items.add("100001126");
        items.add("100001326");
        return algoliaManager.getItemListAlgoliaFromStringList(items);
    }

}
