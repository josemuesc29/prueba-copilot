package com.imaginamos.farmatodo.backend.product;

/**
 * Created by Eric on 28/02/2017.
 */

import com.algolia.search.exceptions.AlgoliaException;
import com.google.api.server.spi.Constant;
import com.google.api.server.spi.config.*;
import com.google.api.server.spi.response.*;
import com.google.appengine.api.datastore.Query;
import com.google.gson.Gson;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.imaginamos.farmatodo.backend.Prime.PrimeUtil;
import com.imaginamos.farmatodo.backend.cache.CachedDataManager;
import com.imaginamos.farmatodo.backend.order.create_order.domain.TalonOneComboService;
import com.imaginamos.farmatodo.backend.user.Authenticate;
import com.imaginamos.farmatodo.backend.user.Users;
import com.imaginamos.farmatodo.backend.util.FTDUtil;
import com.imaginamos.farmatodo.model.algolia.GroupSorted;
import com.imaginamos.farmatodo.model.algolia.ItemAlgolia;
import com.imaginamos.farmatodo.model.algolia.ItemAlgoliaSort;
import com.imaginamos.farmatodo.model.algolia.OptimalRouteDistance;
import com.imaginamos.farmatodo.model.algolia.filters.FiltersConfig;
import com.imaginamos.farmatodo.model.algolia.filters.GenericFiltersConfig;
import com.imaginamos.farmatodo.model.customer.Customer;
import com.imaginamos.farmatodo.model.customer.CustomerJSON;
import com.imaginamos.farmatodo.model.customer.CustomerOnlyData;
import com.imaginamos.farmatodo.model.customer.Suggesteds;
import com.imaginamos.farmatodo.model.dto.*;
import com.imaginamos.farmatodo.model.item.TtlCacheAlgoliaRecommendRes;
import com.imaginamos.farmatodo.model.optics.StoreIdDefaultOptics;
import com.imaginamos.farmatodo.model.order.ClientResponse;
import com.imaginamos.farmatodo.model.order.DeliveryOrder;
import com.imaginamos.farmatodo.model.order.DeliveryOrderItem;
import com.imaginamos.farmatodo.model.product.*;
import com.imaginamos.farmatodo.model.productDetail.ItemConfigAlgolia;
import com.imaginamos.farmatodo.model.productDetail.ItemInfoConfigData;
import com.imaginamos.farmatodo.model.stock.TotalStockRequest;
import com.imaginamos.farmatodo.model.stock.TotalStockResponse;
import com.imaginamos.farmatodo.model.user.User;
import com.imaginamos.farmatodo.model.util.Answer;
import com.imaginamos.farmatodo.model.util.CollectionResponseModel;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.networking.algolia.APIAlgolia;
import com.imaginamos.farmatodo.networking.api.ShoppingCartApi;
import com.imaginamos.farmatodo.networking.base.ApiBuilder;
import com.imaginamos.farmatodo.networking.growthbook.GrowthBookConfigLoader;
import com.imaginamos.farmatodo.networking.services.ApiGatewayService;
import com.imaginamos.farmatodo.networking.talonone.TalonOneService;
import com.imaginamos.farmatodo.networking.talonone.model.TalonAttributes;
import retrofit2.Response;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.imaginamos.farmatodo.backend.OfyService.ofy;
import static com.imaginamos.farmatodo.backend.photoSlurp.PhotoSlurpMethods.setPhotoSlurpData;
import static com.imaginamos.farmatodo.backend.photoSlurp.PhotoSlurpMethods.setPhotoSlurpGridData;

/**
 * @Api - Google indicator that the present class ins an Api.
 * Every further indication of @ApiMethod includes this as a Google API.
 */
@Api(name = "productEndpoint",
        version = "v1",
        apiKeyRequired = AnnotationBoolean.TRUE,
        scopes = {Constants.EMAIL_SCOPE},
        clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID_DEVELOPER, Constant.API_EXPLORER_CLIENT_ID},
        audiences = {Constants.ANDROID_AUDIENCE},
        namespace = @ApiNamespace(ownerDomain = Constants.OWNER_DOMAIN, ownerName = Constants.OWNER_NAME, packagePath = ""),
        description = "Stores order for all pages.")

public class ProductEndpoint {
    private static final Logger log = Logger.getLogger(Customer.class.getName());
    private Authenticate authenticate;
    private ProductsMethods productsMethods;
    private Users users;

    private PrimeUtil primeUtil;
    private ShoppingCartApi shoppingCartApi;
    private static final int MAX_SUBSTITUTES = 10;

    public final static String CUSTOMER_ANONYMOUS = "ANONYMOUS";

    private final Comparator<Highlight> sortHighlight = (h1, h2) -> h1.getOrderingNumber().compareTo(h2.getOrderingNumber());

    public ProductEndpoint() {
        authenticate = new Authenticate();
        productsMethods = new ProductsMethods();
        users = new Users();
        primeUtil = new PrimeUtil();
        shoppingCartApi = ApiBuilder.get().createShoppingCartService(ShoppingCartApi.class);
    }

    @ApiMethod(name = "getItem", path = "/productEndpoint/getItem", httpMethod = ApiMethod.HttpMethod.GET)
    public Item getItem(
            @Named("token") final String token,
            @Named("tokenIdWebSafe") final String tokenIdWebSafe,
            @Nullable @Named("idCustomerWebSafe") final String idCustomerWebSafe,
            @Named("idItem") final long idItem,
            @Named("idStoreGroup") final long idStoreGroupFromRequest) throws ConflictException, BadRequestException, AlgoliaException {
        if (!authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);

        long idStoreGroup = APIAlgolia.getDefaultStoreIdByStoreId((int) idStoreGroupFromRequest);

        int userId = 0;

        if (idCustomerWebSafe != null) {
            Key<User> userKey = Key.create(idCustomerWebSafe);
            User user = users.findUserByKey(userKey);

            if (user != null && user.getId() > 0) {
                APIAlgolia.uploadItemToHistory(String.valueOf(user.getId()), idItem, idStoreGroup);
                userId = user.getId();
            }
        }

        Item item = productsMethods.setFindInformationToAlgoliaByIdItem(Long.toString(idItem), idStoreGroup, null);
        if (item == null) throw new ConflictException(Constants.PRODUCT_NOT_EXISTS);

        List<CrossSales> crossSaless;
        List<Suggested> crossSalesItemsList = APIAlgolia.getCrossSaleByItem(idItem);

        if (!crossSalesItemsList.isEmpty()) {
            List<Item> itemsCrossSales = productsMethods.getItemsByIds(crossSalesItemsList, idStoreGroup);
            crossSaless = productsMethods.itemToCrossSales(itemsCrossSales);
        } else {
            crossSaless = null;
        }

        String customerId = userId > 0 ? String.valueOf(userId) : Constants.CUSTOMER_ANONYMOUS;
        OptimalRouteDistance distances = GrowthBookConfigLoader.getDistancesOptimalRoute(customerId);

        // se quema la tienda 26 x q los fronts se alimenta de esta
        List<Substitutes> substitutesFromAlgolia = productsMethods.getItemSubstitutesFromAlgolia(
                idItem,
                0,
                26,
                false, distances.getPercentagePrice(), new ArrayList<>());


        item.setCrossSales(crossSaless);
        item.setSubstitutes(substitutesFromAlgolia);
        return item;
    }


    @ApiMethod(name = "getItem", path = "/productEndpoint/v2/getItem", httpMethod = ApiMethod.HttpMethod.GET)
    public DynamicResponse getItemV2(
            @Named("token") final String token,
            @Named("tokenIdWebSafe") final String tokenIdWebSafe,
            @Named("idItem") final long idItem,
            @Named("idStoreGroup") final long idStoreGroupFromRequest,
            @Named("source") final EnableForEnum source,
            @Nullable @Named("idCustomerWebSafe") final String idCustomerWebSafe,
            @Nullable @Named("nearbyStores") final String nearbyStores,
            @Nullable @Named("storeId") final String storeId,
            @Nullable @Named("deliveryType") final String deliveryType,
            @Nullable @Named("city") final String city
    ) throws ConflictException, AlgoliaException, NotFoundException, BadRequestException {

        if (Objects.isNull(source))
            throw new ConflictException(Constants.ERROR_SOURCE_NULL);
        User user = null;
        if (idCustomerWebSafe != null){
            Key<User> userKey = Key.create(idCustomerWebSafe);
            user = users.findUserByKey(userKey);

            if (user != null && user.getId() > 0) {
                if (!primeUtil.isItemPrime(idItem)){
                    APIAlgolia.uploadItemToHistory(String.valueOf(user.getId()), idItem, idStoreGroupFromRequest);
                }

            }
        }
        /**
         * se activa nuevamente el guardado del cache
         */
        List<Long> nearbyStoresList = parseNearbyStores(nearbyStores);

        DynamicResponse cachedResponse = getCacheRedis( idItem, idStoreGroupFromRequest, source, nearbyStoresList );
        if (Objects.nonNull(cachedResponse)) {
            callTalonOneAndBraze(tokenIdWebSafe, source, idCustomerWebSafe, user, cachedResponse, TalonAttributes.getTalonOneAttributes(storeId, deliveryType, city, source));
            // Verificar si el item esta marcado con el parametro starProduct
            cachedResponse = productsMethods.avoidBazaarVoice(idItem, idStoreGroupFromRequest, cachedResponse);
            return cachedResponse;
        }

        ItemConfigAlgolia itemConfigAlgolia = APIAlgolia.getItemDetailConfig();

        if (!itemConfigAlgolia.isValid()) {
            log.warning("info algolia is null");
            throw new ConflictException(Constants.DEFAULT_MESSAGE);
        }

//        log.info("Config Algolia -> " + itemConfigAlgolia.toString());

        getOnlyDataFromSource(source, itemConfigAlgolia);

        //CustomerOnlyData customerOnlyData = new CustomerOnlyData();
        List<DeliveryOrderItem> deliveryOrderItemList = new ArrayList<>();
        int quantitySold = 0;
//*prime*//

        try {
            Key<User> userKey = Key.create(idCustomerWebSafe);
            //User user = users.findUserByKey(userKey);

            //log.info("usuario -> " + user.getId());
            //customerOnlyData = ApiBackend30Service.get().getCustomerOnlyById(user.getId());
            //log.info("Customer -> " + customerOnlyData.getEmail());

            try {
                DeliveryOrder deliveryOrder = ofy().load().type(DeliveryOrder.class).filter("currentStatus", 1).ancestor(Ref.create(userKey)).first().now();
                if (deliveryOrder != null) {
                    deliveryOrderItemList = ofy().load().type(DeliveryOrderItem.class).ancestor(deliveryOrder).list();
                    if (!deliveryOrderItemList.isEmpty()) {
                        //validateDuplicateItems(deliveryOrderItemList);
                        quantitySold = setQuantitySoldItem(deliveryOrderItemList, idItem);
                    }
                }
            } catch (Exception e) {
                log.warning("Error al traer el carrito del cliente.");
            }

        } catch (Exception e) {
            log.warning("Error al consultar el cliente.");
        }

        Item item = getItemFromDetail(Long.toString(idItem), idStoreGroupFromRequest, nearbyStores);
//        log.info("QuantitySold = " + quantitySold);
        item.setQuantitySold(quantitySold);
        ItemInfoConfigData itemInfoConfigData = new ItemInfoConfigData();
        //itemInfoConfigData.setCustomerOnlyData(customerOnlyData);
        if (item.getStoresWithStock() != null && !item.getStoresWithStock().isEmpty() && !nearbyStoresList.isEmpty() && Objects.isNull(item.getUuidItem())) {
            updateTotalStock(item, nearbyStoresList);
        }
        if (!nearbyStoresList.isEmpty()){
            TalonOneComboService talonOneComboService = new TalonOneComboService(productsMethods);
            talonOneComboService.ifComboSetTotalStock(nearbyStoresList, item, (int) idStoreGroupFromRequest);
        }
        itemInfoConfigData.setItemData(item);
        itemInfoConfigData.setItemConfigAlgolia(itemConfigAlgolia);
        itemInfoConfigData.setSource(source);

        if (!nearbyStoresList.isEmpty()) {
            itemInfoConfigData.setNearbyStores(nearbyStoresList);
        }

        /*
        itemInfoConfigData.setHeaderComponents(itemConfigAlgolia.getItemConfig().getHeaderComponents());
        itemInfoConfigData.setBodyComponents(itemConfigAlgolia.getItemConfig().getBodyComponents());
        itemInfoConfigData.setFooterComponents(itemConfigAlgolia.getItemConfig().getFooterComponents());
         */

        if (!itemConfigAlgolia.isValid()) {
            log.warning("info algolia is null");
            throw new ConflictException(Constants.DEFAULT_MESSAGE);
        }

        DynamicResponse dynamicResponse = new DynamicResponse();
        // sections

//        log.info("INIT set section , config data -> " + itemInfoConfigData);
        List<DynamicSection> dynamicSectionList = new ArrayList<>();

        List<DynamicSection> headerSectionAuxList = getHeaderSections(itemInfoConfigData);

        // get body sections.

        List<DynamicSection> bodySectionAuxList = getBodySections(itemInfoConfigData);

        // get footer sections

        List<DynamicSection> footerSectionAuxList = getFooterSections(itemInfoConfigData);

        // set header sections in response
        dynamicSectionList.addAll(headerSectionAuxList);

        // set body sections in response
        dynamicSectionList.addAll(bodySectionAuxList);

        // set footer sections in response
        dynamicSectionList.addAll(footerSectionAuxList);

        // set sections.
        dynamicResponse.setItemSection(dynamicSectionList);
        try {
            if (item.getTotalStock() == 0 && user != null && user.getId() > 0 && Objects.nonNull(item.getMediaDescription())) {
                Optional<CustomerJSON> customerJSON = ApiGatewayService.get().getCustomerById(user.getId());
                if(customerJSON.isPresent() && customerJSON.get().getEmail() != null && !customerJSON.get().getEmail().isEmpty()) {
                    ApiGatewayService.get().addNonStockItemInBraze(customerJSON.get().getEmail(), String.valueOf(item.getId()));
                }
            }
        } catch (Exception e) {
            log.severe("No se pudo agregar item sin stock en braze");
        }

//        log.info("El item no existe?  ->" + Objects.isNull(item.getMediaDescription()));
        if(Objects.nonNull(item.getMediaDescription())) {
            TtlCacheAlgoliaRecommendRes ttlCacheAlgoliaRecommendRes = GrowthBookConfigLoader.ttlCacheAlgoliaRecommend(CUSTOMER_ANONYMOUS);
            if (Objects.nonNull(ttlCacheAlgoliaRecommendRes.getGetItemTtlSeconds()))
                CachedDataManager.algoliaSaveItemInCache(getCacheKey(idItem, idStoreGroupFromRequest, source, nearbyStoresList), dynamicResponse, ttlCacheAlgoliaRecommendRes.getGetItemTtlSeconds());
        }
        try {
            if(Objects.nonNull(idCustomerWebSafe))
                callTalonOne(tokenIdWebSafe, idCustomerWebSafe, user, dynamicResponse,source, TalonAttributes.getTalonOneAttributes(storeId, deliveryType, city, source));
        } catch (Exception e) {
            log.info("Error al llamar el metodo de TalonOne ERROR(" + e.getMessage() + ")");
        }
        // Verificar si el item esta marcado con el parametro starProduct
        dynamicResponse = productsMethods.avoidBazaarVoice(idItem, idStoreGroupFromRequest, dynamicResponse);
        return dynamicResponse;
    }

    /**
     * get real stock from item and nearby stores
     * @param item item to get stock
     * @param nearbyStoresList list of nearby stores
     */
    private void updateTotalStock(Item item, List<Long> nearbyStoresList) {

        List<Integer> nearbyStores = nearbyStoresList.stream().map(Long::intValue).collect(Collectors.toList());
        TotalStockRequest totalStockRequest = new TotalStockRequest(item.getId(), nearbyStores);
        try {
            Response<TotalStockResponse> response = shoppingCartApi.getTotalStock(totalStockRequest).execute();
            if (response.isSuccessful() && response.body() != null) {
                TotalStockResponse totalStockResponse = response.body();
                item.setTotalStock(totalStockResponse.getData().getTotalStock());
            } else {
                log.warning("Failed to get total stock: " + response.errorBody());
            }
        } catch (IOException e) {
            log.severe("Network error while getting total stock: " + e.getMessage());
        }

    }

    /**
     * Retrieves the item from the Redis cache
     * @param itemID ID of the item
     * @param storeGroupID ID of the store group
     * @param source Enablement enumeration
     * @param nearbyStoresList List of nearby stores
     * @return DynamicResponse from the cache or null if not found
     */
    private static DynamicResponse getCacheRedis(long itemID, long storeGroupID, EnableForEnum source, List<Long> nearbyStoresList) {
        String cacheKey = getCacheKey(itemID, storeGroupID, source, nearbyStoresList);

        if (cacheKey.isEmpty()) {
            return null;
        }

        DynamicResponse cachedResponse = CachedDataManager.algoliaGetItemFromCache(cacheKey);

        if (isOmittedStore(storeGroupID) || Objects.isNull(cachedResponse)) {
            return null;
        }

        return cachedResponse;
    }



    /**
     * Checks if the store group ID is in the list of omitted groups
     * @param storeGroupID ID of the store group
     * @return true if the ID is in the omitted list, false otherwise
     */
    private static boolean isOmittedStore(long storeGroupID) {
        int[] omittedStoreIDs = {1000, 1001};
        return Arrays.stream(omittedStoreIDs).anyMatch(omittedStoreId -> omittedStoreId == storeGroupID);
    }

    /**
     * Se desconoce el proposito de las invocaciones a talonOne y Braze, se extrae esta funcionalidad para mejor organización del código.
     *
     * @param tokenId
     * @param source
     * @param customerWebSafeId
     * @param user
     * @param cachedResponse
     */
    private static void callTalonOneAndBraze(String tokenId, EnableForEnum source, String customerWebSafeId, User user, DynamicResponse cachedResponse, Map<String, Object> talonOneData) {
        try {
            DynamicSection itemComponent = cachedResponse.getItemSection().stream().filter(c -> c.getComponentType().equals(ComponentTypeEnum.MAIN_ITEM)).findFirst().get();
            if(Objects.nonNull(customerWebSafeId))
                callTalonOne(tokenId, customerWebSafeId, user, cachedResponse, source, talonOneData);

            Item item = itemComponent.getList().get(0).getProduct().get(0);
            log.info(Long.toString(item.getId()));
            log.info("item description" + item.getMediaDescription());
            if (item.getTotalStock() == 0 && user != null && user.getId() > 0 && !item.getMediaDescription().isEmpty()) {
                Optional<CustomerJSON> customerJSON = ApiGatewayService.get().getCustomerById(user.getId());
                if(customerJSON.isPresent() && customerJSON.get().getEmail() != null && !customerJSON.get().getEmail().isEmpty()) {
                    ApiGatewayService.get().addNonStockItemInBraze(customerJSON.get().getEmail(), String.valueOf(item.getId()));
                }
            }
        } catch (Exception e) {
            log.severe("No se pudo agregar item sin stock en braze");
        }
    }

    /**
     * get cache key for item using storeGroupID, nearbyStores and source
     * @param itemID itemId
     * @param storeGroupID  storeGroupID ID de la tienda principal
     * @param source WEB, IOS, ANDROID, RESPONSIVE enum
     * @return String with the cache key
     */
    private static String getCacheKey(long itemID, long storeGroupID, EnableForEnum source, List<Long> nearbyStoresList) {
        StringBuilder key = new StringBuilder(itemID + "-" + source.toValue());

        Optional.ofNullable(nearbyStoresList)
                .filter(list -> !list.isEmpty())
                .ifPresentOrElse(
                        list -> {
                            List<Long> sortedList = new ArrayList<>(list);
                            Collections.sort(sortedList);
                            key.append("-nearby-").append(sortedList.stream()
                                            .map(Object::toString)
                                            .collect(Collectors.joining("-")));
                        },
                        () -> key.append("-").append(storeGroupID)
                );

        return key.toString();
    }

    private static void callTalonOne(String tokenIdWebSafe, String idCustomerWebSafe, User user, DynamicResponse dynamicResponse,EnableForEnum source, Map<String, Object> talonOneData) {
        try {
            processDynamicResponse(dynamicResponse);
        }catch (Exception e) {
            log.warning("Error eliminando producto principal en productos relacionados");
        }
        TalonOneService talonOneService = new TalonOneService();
        talonOneService.sendItemsToTalon(dynamicResponse.getItemSection(), user.getId(), tokenIdWebSafe, idCustomerWebSafe,source, talonOneData);
    }

    private static void processDynamicResponse(DynamicResponse dynamicResponse) {
        String itemIdMain = null;
        for (DynamicSection ds : Objects.requireNonNull(dynamicResponse.getItemSection())) {
            if (ds.getComponentType() == ComponentTypeEnum.MAIN_ITEM) {
                if (Objects.nonNull(ds.getList())) {
                    itemIdMain = ds.getList().get(0).getId();
                }
            } else {
                removeRelatedItems(itemIdMain, Objects.requireNonNull(ds.getList()));
            }
        }
    }

    private static void removeRelatedItems(String itemIdMain, List<Element> relatedItems) {
        relatedItems.removeIf(item -> Objects.equals(item.getId(), itemIdMain));
    }

    private Optional<String> getFilterConfigForItem(long idItem) {
//        log.info("method: getFilterConfigForItem()");
        if (idItem <= 0) {
            return Optional.empty();
        }

        String strItemId = String.valueOf(idItem);

        Optional<GenericFiltersConfig> optionalGenericFiltersConfig = APIAlgolia.getFiltersConfig();

        if (!optionalGenericFiltersConfig.isPresent()) {
            return Optional.empty();
        }

        GenericFiltersConfig filtersConfig = optionalGenericFiltersConfig.get();

        if (filtersConfig.getFiltersConfig() == null){
            log.warning("No se encuentra configuracion de filtros");
            return Optional.empty();
        }

        Optional<String> filterConfigObjectIdOpt = Optional.empty();
        for (FiltersConfig configAux : filtersConfig.getFiltersConfig()) {

            if (configAux != null && configAux.getItemsWithFilters() != null){
                Optional<String> itemIdOpt = configAux
                        .getItemsWithFilters()
                        .stream()
                        .filter( itemWithFilters -> itemWithFilters.equals(strItemId))
                        .findFirst();

                if (itemIdOpt.isPresent() && configAux.getConfigFilters() != null && !configAux.getConfigFilters().isEmpty()){
                    filterConfigObjectIdOpt = Optional.of(configAux.getConfigFilters());
                    break;
                }
            }

        }

        return filterConfigObjectIdOpt;
    }

    /**
     * Devuelve la cantidad comprada por el cliente segun la lista de items que tiene en el carrito
     *
     * @param deliveryOrderItemList
     * @return
     */
    private int setQuantitySoldItem(List<DeliveryOrderItem> deliveryOrderItemList, long idItem) {
        int quantitySold = 0;
        for (DeliveryOrderItem item : deliveryOrderItemList) {
            if (Objects.nonNull(item.getId())
                    && Objects.nonNull(item.getQuantitySold())
                    && item.getId() == idItem) {
                int quantitySoldAux = item.getQuantitySold();
                return quantitySoldAux;
            }
        }
        return quantitySold;
    }


    private Item getItemFromDetail(String idItem, long idStoreGroupFromRequest, String nearbyStores) throws ConflictException, AlgoliaException {
        long idStoreGroup = APIAlgolia.getDefaultStoreIdByStoreId((int) idStoreGroupFromRequest);
        Item item;

        if (nearbyStores == null || nearbyStores.isEmpty()) {
            item = productsMethods.setFindInformationToAlgoliaByIdItem(idItem, idStoreGroup, null);
        } else {
            Optional<Item> optItem = productsMethods.fillItemFromAlgolia(idItem, nearbyStores);
            item = optItem.orElse(null);
        }

        if (Objects.isNull(item)) {
            throw new ConflictException(Constants.PRODUCT_NOT_EXISTS);
        }

        List<CrossSales> crossSaless;
        List<Suggested> crossSalesItemsList = APIAlgolia.getCrossSaleByItem(Long.parseLong(idItem));

        if (!crossSalesItemsList.isEmpty()) {
            List<Item> itemsCrossSales = productsMethods.getItemsByIds(crossSalesItemsList, idStoreGroup);
            crossSaless = productsMethods.itemToCrossSales(itemsCrossSales);
        } else {
            crossSaless = null;
        }

        item.setCrossSales(crossSaless);
        return item;
    }

    /**
     * validate source data -> WEB,IOS,ANDROID,RESPONSIVE
     *
     * @param source
     * @param itemConfigAlgolia
     */
    private void getOnlyDataFromSource(EnableForEnum source, ItemConfigAlgolia itemConfigAlgolia) {

        //log.warning("source -> " + source.toValue());
        // remove header components
        if (Objects.nonNull(itemConfigAlgolia)
                && Objects.nonNull(itemConfigAlgolia.getItemConfig())
                && Objects.nonNull(itemConfigAlgolia.getItemConfig().getHeaderComponents())
                && Objects.nonNull(itemConfigAlgolia.getItemConfig().getBodyComponents())
                && Objects.nonNull(itemConfigAlgolia.getItemConfig().getFooterComponents())) {

            /* remove components according source */
            itemConfigAlgolia.getItemConfig().getHeaderComponents().removeIf(component -> componentIsEnableForSource(component, source));
            itemConfigAlgolia.getItemConfig().getBodyComponents().removeIf(component -> componentIsEnableForSource(component, source));
            itemConfigAlgolia.getItemConfig().getFooterComponents().removeIf(component -> componentIsEnableForSource(component, source));

        }


    }

    private boolean componentIsEnableForSource(Component component, EnableForEnum source) {
        if (Objects.nonNull(component.getEnableFor())) {
            Optional<EnableForEnum> optionalEnableForEnum = component.getEnableFor().stream().filter(sourceAux -> sourceAux.equals(source)).findFirst();
            return !optionalEnableForEnum.isPresent();
        }
        return false;
    }

    /**
     * get header sections from algolia
     *
     * @param itemInfoConfigData
     * @return sections
     */
    private List<DynamicSection> getHeaderSections(ItemInfoConfigData itemInfoConfigData) throws NotFoundException {
//        log.info("INIT getHeaderSections");
        ItemConfigAlgolia itemConfigAlgolia = itemInfoConfigData.getItemConfigAlgolia();
        if (itemConfigAlgolia != null && itemConfigAlgolia.isValid()
                && Objects.nonNull(itemConfigAlgolia.getItemConfig())
                && itemConfigAlgolia.getItemConfig().isValid()
                && Objects.nonNull(itemConfigAlgolia.getItemConfig().getHeaderComponents())) {
            List<Component> headerSectionsAlgolia = itemConfigAlgolia.getItemConfig().getHeaderComponents();
            List<DynamicSection> headerSectionsList = getSections(headerSectionsAlgolia, itemInfoConfigData);

            if (headerSectionsList != null) return headerSectionsList;

        }
        return new ArrayList<>();
    }

    /**
     * get body sections from algolia
     *
     * @param itemInfoConfigData
     * @return
     * @version v1
     */
    private List<DynamicSection> getBodySections(ItemInfoConfigData itemInfoConfigData) throws NotFoundException {
        //log.info("INIT getBodySections");
        ItemConfigAlgolia itemConfigAlgolia = itemInfoConfigData.getItemConfigAlgolia();
        if (itemConfigAlgolia != null && itemConfigAlgolia.isValid()
                && Objects.nonNull(itemConfigAlgolia.getItemConfig())
                && itemConfigAlgolia.getItemConfig().isValid()
                && Objects.nonNull(itemConfigAlgolia.getItemConfig().getBodyComponents())) {
            List<Component> bodySectionsAlgolia = itemConfigAlgolia.getItemConfig().getBodyComponents();
            List<DynamicSection> bodySectionsList = getSections(bodySectionsAlgolia, itemInfoConfigData);

            if (bodySectionsList != null) return bodySectionsList;

        }
        return new ArrayList<>();
    }

    /**
     * get body sections from algolia
     *
     * @param itemInfoConfigData
     * @return
     * @version v1
     */
    private List<DynamicSection> getFooterSections(ItemInfoConfigData itemInfoConfigData) throws NotFoundException {
        //log.info("INIT getBodySections");
        ItemConfigAlgolia itemConfigAlgolia = itemInfoConfigData.getItemConfigAlgolia();
        if (itemConfigAlgolia != null && itemConfigAlgolia.isValid()
                && Objects.nonNull(itemConfigAlgolia.getItemConfig())
                && itemConfigAlgolia.getItemConfig().isValid()
                && Objects.nonNull(itemConfigAlgolia.getItemConfig().getFooterComponents())) {
            List<Component> bodySectionsAlgolia = itemConfigAlgolia.getItemConfig().getFooterComponents();
            List<DynamicSection> bodySectionsList = getSections(bodySectionsAlgolia, itemInfoConfigData);

            if (bodySectionsList != null) return bodySectionsList;

        }
        return new ArrayList<>();
    }

    /**
     * create and get data from all sections
     *
     * @param sectionsAlgolia
     * @param itemInfoConfigData
     * @return
     */
    @org.jetbrains.annotations.Nullable
    private List<DynamicSection> getSections(List<Component> sectionsAlgolia, ItemInfoConfigData itemInfoConfigData) {
//        log.info("INIT getSections()");
        if (Objects.nonNull(sectionsAlgolia)) {
            // sort
            sectionsAlgolia.sort(Comparator.comparing(Component::getPosition));
            //log.info("section algolia -> " + sectionsAlgolia.toString());
            // create data
            List<DynamicSection> responseDynamicSectionList = new ArrayList<>();

            try {
                sectionsAlgolia.forEach(
                        sectionAlgolia -> {

                            DynamicSection dynamicSectionAux = new DynamicSection();

                            dynamicSectionAux.setRedirectURL(sectionAlgolia.getRedirectUrl());
                            dynamicSectionAux.setLabel(sectionAlgolia.getLabel());
                            dynamicSectionAux.setComponentType(sectionAlgolia.getComponentType());
                            dynamicSectionAux.setUrlBanner(sectionAlgolia.getUrlBanner());
                            dynamicSectionAux.setMaxLines(sectionAlgolia.getMaxLines());

                            //log.info("Seteando componente -> " + dinamicSectionAux.toString());
                            // ** GET INFO FROM SECTION **
                            getDataFromSection(sectionAlgolia, dynamicSectionAux, itemInfoConfigData);
                            //
                            if (sectionAlgolia.getActive() != null && sectionAlgolia.getActive()) {

                                if (dynamicSectionAux.getComponentType() != null) {

                                    if (componentIsValid(dynamicSectionAux)) {
                                        //log.info("add_component -> " + dynamicSectionAux.getComponentType().toString());
                                        responseDynamicSectionList.add(dynamicSectionAux);
                                    }
                                }
                            }
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }
            return responseDynamicSectionList;
        }
        return null;
    }

    /**
     * valida si el componente tiene todos los requerimientos para mostrase en la respuesta del servicio
     *
     * @param dynamicSectionAux
     * @return
     */
    private boolean componentIsValid(DynamicSection dynamicSectionAux) {

        if (dynamicSectionAux != null && dynamicSectionAux.getComponentType() != null) {

            switch (dynamicSectionAux.getComponentType()) {
                case ITEM_LIST:
                    return dynamicSectionAux.getList() != null && !dynamicSectionAux.getList().isEmpty();
                case BANNER_LEFT_ADVERTISING:
                    //log.warning("verificando banners BANNER_LEFT_ADVERTISING");
                    return dynamicSectionAux.getList() != null && !dynamicSectionAux.getList().isEmpty();
                case BANNER_RIGHT_ADVERTISING:
                    //log.warning("verificando banners BANNER_RIGHT_ADVERTISING");
                    return dynamicSectionAux.getList() != null && !dynamicSectionAux.getList().isEmpty();
                case STATIC_BANNER:
                    //log.warning("verificando banners STATIC_BANNER");
                    return dynamicSectionAux.getList() != null && !dynamicSectionAux.getList().isEmpty();
                case VIDEO_LIST:
                    //log.warning("verificando VIDEO_LIST");
                    return dynamicSectionAux.getList() != null && !dynamicSectionAux.getList().isEmpty();
                case HTML_LABEL:
                    //log.warning("verificando HTML_LABEL");
                    return dynamicSectionAux.getLabel() != null && !dynamicSectionAux.getLabel().isEmpty();
                case ITEM_SEO:
                    //log.warning("verificando ITEM_SEO" + (dynamicSectionAux.getHtml() != null && !dynamicSectionAux.getHtml().isEmpty()));
                    return dynamicSectionAux.getHtml() != null && !dynamicSectionAux.getHtml().isEmpty();
            }
        }
        return true;
    }

    /**
     * generate data from section.
     *
     * @param sectionAlgolia
     * @param dynamicSectionAux
     * @param itemInfoConfigData
     */
    private void getDataFromSection(
            Component sectionAlgolia, DynamicSection dynamicSectionAux, ItemInfoConfigData itemInfoConfigData) {

        if (Objects.nonNull(sectionAlgolia)
                && Objects.nonNull(sectionAlgolia.getComponentType())
                && Objects.nonNull(dynamicSectionAux)) {
            //log.info("Component type: " + sectionAlgolia.getComponentType());

            switch (sectionAlgolia.getComponentType()) {
                case HTML_LABEL:
                    setLabelHtml(sectionAlgolia, dynamicSectionAux, itemInfoConfigData);
                    break;
                case ITEM_LIST:
                case MAIN_ITEM:
                    setItemListData(sectionAlgolia, dynamicSectionAux, itemInfoConfigData);
                    break;
                case VIDEO_LIST:
                    setTutorials(dynamicSectionAux, itemInfoConfigData, sectionAlgolia);
                    break;
                case ITEM_SEO:
                    setItemSEOData(sectionAlgolia, dynamicSectionAux, itemInfoConfigData);
                    break;
                case PHOTOSLURP:
                    setPhotoSlurpData(sectionAlgolia, dynamicSectionAux, null, itemInfoConfigData);
                    break;
                case PHOTOSLURPGRID:
                    setPhotoSlurpGridData(sectionAlgolia, dynamicSectionAux,null, itemInfoConfigData);
                    break;
            }
        }
    }


    /**
     * set data SEO
     *
     * @param sectionAlgolia
     * @param dynamicSectionAux
     * @param itemInfoConfigData
     */
    private void setItemSEOData(Component sectionAlgolia, DynamicSection dynamicSectionAux, ItemInfoConfigData itemInfoConfigData) {
        if (Objects.nonNull(itemInfoConfigData.getItemData().getItemId()) &&
                Objects.nonNull(sectionAlgolia.getDataFrom()) &&
                Objects.nonNull(sectionAlgolia.getDataFrom().getFrom())) {
            String dataSeo = null;

            switch (sectionAlgolia.getDataFrom().getFrom()) {
                case TEXT_SEO:
                    dataSeo = APIAlgolia.getItemSeoAux(itemInfoConfigData.getItemData().getItemId(), itemInfoConfigData.getSource());
                    break;
                case TEXT_VADEMECUM:
                    dataSeo = APIAlgolia.getItemVademecum(itemInfoConfigData.getItemData().getItemId(), itemInfoConfigData.getSource());
                    break;
            }
            dynamicSectionAux.setHtml(dataSeo);
        }
    }

    /**
     * setLabelHtml
     *
     * @param sectionAlgolia
     * @param dynamicSectionAux
     * @param itemInfoConfigData
     */
    private void setLabelHtml(Component sectionAlgolia, DynamicSection dynamicSectionAux, ItemInfoConfigData itemInfoConfigData) {
        if (sectionAlgolia.getDataFrom() != null && sectionAlgolia.getDataFrom().getFrom() != null) {

            switch (sectionAlgolia.getDataFrom().getFrom()) {
                case USER_NAME:
                    setUserNameInLabel(sectionAlgolia, dynamicSectionAux, itemInfoConfigData);
                    break;
                case VIDEO_LIST:
                    setVideoLabel(sectionAlgolia, dynamicSectionAux, itemInfoConfigData);
                    break;
                case SAME_BRAND_ITEMS:
                    setSameBrandItemsLabel(sectionAlgolia, dynamicSectionAux, itemInfoConfigData);
                    break;
            }
        }
    }


    /**
     * set label videos
     *
     * @param sectionAlgolia
     * @param dynamicSectionAux
     * @param itemInfoConfigData
     */
    private void setVideoLabel(Component sectionAlgolia, DynamicSection dynamicSectionAux, ItemInfoConfigData itemInfoConfigData) {
        try {
            List<VideoData> tutorials = APIAlgolia.getVideosForItem(itemInfoConfigData.getItemData().getId());
            if (!tutorials.isEmpty()) {
                dynamicSectionAux.setLabel(sectionAlgolia.getLabel());
            } else {
                dynamicSectionAux.setLabel(null);
            }
        } catch (Exception e) {
            log.warning("Error al agregar el label de videos. Error ---> " + e.getMessage());
        }
    }

    /**
     * set label sambe brands items
     *
     * @param sectionAlgolia
     * @param dynamicSectionAux
     * @param itemInfoConfigData
     */
    private void setSameBrandItemsLabel(Component sectionAlgolia, DynamicSection dynamicSectionAux, ItemInfoConfigData itemInfoConfigData) {
        try {
            if (Objects.nonNull(Objects.requireNonNull(itemInfoConfigData.getItemData()).getMarca())) {
                List<ItemAlgolia> itemAlgoliaList = APIAlgolia.getItemAlgoliaByBrand(itemInfoConfigData.getItemData().getMarca(), 26L);
                if (Objects.nonNull(itemAlgoliaList) && !itemAlgoliaList.isEmpty()) {
                    //log.info("tamaño de los items de la misma marca -> " + itemAlgoliaList.size());
                    dynamicSectionAux.setLabel(sectionAlgolia.getLabel());
                } else {
                    dynamicSectionAux.setLabel(null);
                }
            } else {
                dynamicSectionAux.setLabel(null);
            }
        } catch (Exception e) {
            log.warning("Error al agregar el label de sambe brands items. Error ---> " + e.getMessage());
        }
    }

    /**
     * setUserNameInLabel
     *
     * @param sectionAlgolia
     * @param dynamicSectionAux
     * @param itemInfoConfigData
     */
    private void setUserNameInLabel(Component sectionAlgolia, DynamicSection dynamicSectionAux, ItemInfoConfigData itemInfoConfigData) {

        //CustomerOnlyData customerOnlyData = itemInfoConfigData.getCustomerOnlyData();

        //if (sectionAlgolia.getDataFrom() != null && sectionAlgolia.getDataFrom().getFrom() != null && customerOnlyData != null) {

        if (sectionAlgolia.getLabel() != null) {
            //log.info("method setUserNameInLabel customerOnlyData ->" + customerOnlyData.toString());
            String labelOld = sectionAlgolia.getLabel();
            String newLabel = FTDUtil.replaceStringVar(labelOld, "user_name", "");
                /*if (customerOnlyData.getId() > 0) {
                    newLabel = FTDUtil.replaceStringVar(labelOld, "user_name", customerOnlyData.getFirstName());
                }
                 */
            dynamicSectionAux.setLabel(newLabel);
            dynamicSectionAux.setLabelWeb(newLabel);
        }

        //}
    }

    /**
     * set items list in section.
     *
     * @param sectionAlgolia
     * @param dynamicSectionAux
     * @param itemInfoConfigData
     */
    private void setItemListData(Component sectionAlgolia, DynamicSection dynamicSectionAux, ItemInfoConfigData itemInfoConfigData) {
        // log.info("method() setItemListInGrid component section data from -> " + sectionAlgolia.getDataFrom().toString());
        try {
            if (sectionAlgolia.getDataFrom() != null && sectionAlgolia.getDataFrom().getFrom() != null) {

                switch (sectionAlgolia.getDataFrom().getFrom()) {
                    case RELATED_ITEMS:
                        setRelatedItems(dynamicSectionAux, itemInfoConfigData);
                        break;
                    case SAME_BRAND_ITEMS:
                        setSameBrandItems(dynamicSectionAux, itemInfoConfigData);
                        break;
                    case MAIN_ITEM:
                        setMainItem(dynamicSectionAux, itemInfoConfigData);
                        break;
                }

            }

        } catch (Exception e) {
            log.warning("Error no idStoreGroup From algolia");
            e.printStackTrace();
        }
    }

    /**
     * set main item
     *
     * @param dynamicSectionAux
     * @param itemInfoConfigData
     */
    private void setMainItem(DynamicSection dynamicSectionAux, ItemInfoConfigData itemInfoConfigData) {
        try {
            if (Objects.nonNull(Objects.requireNonNull(itemInfoConfigData.getItemData()))) {

                List<Element> finalelementList = new ArrayList<>();
                List<Item> items = new ArrayList<>();

                items.add(itemInfoConfigData.getItemData());

                if (!items.isEmpty()) {
                    items.forEach(productAux -> {
                        Element elementAux = new Element();
                        List<Item> itemTempList = new ArrayList<>();
                        itemTempList.add(productAux);
                        if (productAux.getId() > 0) elementAux.setId(String.valueOf(productAux.getId()));
                        elementAux.setProduct(itemTempList);
                        elementAux.setType(ProductTypeEnum.UNIQUE);
                        elementAux.setAction(ActionEnum.SUGGESTED);
                        finalelementList.add(elementAux);
                    });
                }

                dynamicSectionAux.setList(finalelementList);
            }
        } catch (Exception e) {
            log.warning("Error al agregar item principal setMainItem() -> " + e.getMessage());
        }
    }

    /**
     * set data same brand items
     *
     * @param dynamicSectionAux
     * @param itemInfoConfigData
     */
    private void setSameBrandItems(DynamicSection dynamicSectionAux, ItemInfoConfigData itemInfoConfigData) {
        try {
            if (Objects.nonNull(Objects.requireNonNull(itemInfoConfigData.getItemData()).getMarca())) {
                List<ItemAlgolia> itemAlgoliaList = APIAlgolia.getItemAlgoliaByBrand(itemInfoConfigData.getItemData().getMarca(), 26L);
                List<ItemAlgolia> itemAlgoliaListLimit = itemAlgoliaList.stream().limit(20).collect(Collectors.toList());

                List<Element> finalelementList = new ArrayList<>();
                List<Item> items = new ArrayList<>();

                if (Objects.nonNull(itemAlgoliaListLimit) && !itemAlgoliaListLimit.isEmpty()) {
                    items = itemAlgoliaListLimit.stream().filter(itemAlgolia -> Objects.nonNull(itemAlgolia) && Objects.nonNull(itemAlgolia.getId()) && !itemAlgolia.getId().isEmpty())
                            .map(itemAlgolia -> APIAlgolia.getItemToItemAlgolia(new Item(), itemAlgolia)).collect(Collectors.toList());
                }

                if (!items.isEmpty()) {
                    items.forEach(productAux -> {
                        Element elementAux = new Element();
                        List<Item> itemTempList = new ArrayList<>();
                        itemTempList.add(productAux);
                        if (productAux.getId() > 0) elementAux.setId(String.valueOf(productAux.getId()));
                        elementAux.setProduct(itemTempList);
                        elementAux.setType(ProductTypeEnum.UNIQUE);
                        elementAux.setAction(ActionEnum.SUGGESTED);
                        finalelementList.add(elementAux);
                    });
                }

                dynamicSectionAux.setList(finalelementList);
            }
        } catch (Exception e) {
            log.warning("El item no tiene marca y ocaciono un ERROR --> " + e.getMessage());
        }
    }

    private void setRelatedItemsNearbyStores(DynamicSection dynamicSectionAux, ItemInfoConfigData itemInfoConfigData) throws AlgoliaException {
        try {

            String customerId = Optional.ofNullable(itemInfoConfigData)
                    .map(ItemInfoConfigData::getCustomerOnlyData)
                    .map(CustomerOnlyData::getId)
                    .map(String::valueOf)
                    .orElse(Constants.CUSTOMER_ANONYMOUS);

            OptimalRouteDistance distances = GrowthBookConfigLoader.getDistancesOptimalRoute(customerId);

            List<Item> items = productsMethods.getSubstitutesByItemAndNearbyStores(
                    itemInfoConfigData,
                    0,
                    false,
                    distances.getPercentagePrice(),
                    new ArrayList<>());

            //  log.info("items size -> " + items.size());

            List<Element> finalelementList = new ArrayList<>();
            if (!items.isEmpty()) {
                items.forEach(productAux -> {
                    Element elementAux = new Element();
                    List<Item> itemTempList = new ArrayList<>();
                    itemTempList.add(productAux);
                    if (productAux.getId() > 0) elementAux.setId(String.valueOf(productAux.getId()));
                    elementAux.setProduct(itemTempList);
                    elementAux.setType(ProductTypeEnum.UNIQUE);
                    elementAux.setAction(ActionEnum.FAVORITE);
                    finalelementList.add(elementAux);
                });
            }

            dynamicSectionAux.setList(finalelementList);
        }
        catch (Exception e) {
            log.log(Level.SEVERE, "Ocurrió un error en setRelatedItemsNearbyStores. Error: " + e.getMessage(), e);
        }
    }

    /**
     * set data related items
     *
     * @param dynamicSectionAux
     * @param itemInfoConfigData
     */
    private void setRelatedItems(DynamicSection dynamicSectionAux, ItemInfoConfigData itemInfoConfigData) {
        try {
            int storeId = 26;

            if(Objects.nonNull(itemInfoConfigData.getItemData()) && Objects.nonNull(itemInfoConfigData.getItemData().getItemOpticsComplete())){
                Optional<StoreIdDefaultOptics> optionalStoreIdDefaultOptics = APIAlgolia.getStoreIdDefaultOptics();
                if(optionalStoreIdDefaultOptics.isPresent()){
                    storeId = optionalStoreIdDefaultOptics.get().getDefaultStoreId();
                }
            }

            itemInfoConfigData.getItemData();
            List<Element> finalelementList = new ArrayList<>();

            String customerId = Optional.of(itemInfoConfigData)
                    .map(ItemInfoConfigData::getCustomerOnlyData)
                    .map(CustomerOnlyData::getId)
                    .map(String::valueOf)
                    .orElse(Constants.CUSTOMER_ANONYMOUS);

            OptimalRouteDistance distances = GrowthBookConfigLoader.getDistancesOptimalRoute(customerId);

            TtlCacheAlgoliaRecommendRes ttlCacheAlgoliaRecommendRes = GrowthBookConfigLoader.ttlCacheAlgoliaRecommend(CUSTOMER_ANONYMOUS);
//            log.info("TTL Cache Algolia Recommend Enabled: " + ttlCacheAlgoliaRecommendRes.getAlgoliaRecommendEnabled());
            List<Item> relatedItems = null;
            if (Boolean.TRUE.equals(ttlCacheAlgoliaRecommendRes.getAlgoliaRecommendEnabled())){
                relatedItems = productsMethods.getRelatedItemsAlgoliaRecommend(
                        itemInfoConfigData.getItemData(),
                        storeId,
                        itemInfoConfigData.getNearbyStores(),
                        MAX_SUBSTITUTES);
            }

            List<Item> items;
            if (relatedItems == null || relatedItems.isEmpty()) {
                log.log(Level.INFO, "No se encontraron items relacionados, se buscarán sustitutos manuales o se encuentra desactivado AlgoliaRecommend");
                items = productsMethods.getSubstitutesFromAlgoliaItem(
                        itemInfoConfigData.getItemData().getId(),
                        0,
                        storeId,
                        false, distances.getPercentagePrice(), new ArrayList<>());
            } else {
                items = relatedItems;
            }

            if (!items.isEmpty()) {
                items.forEach(productAux -> {
                    Element elementAux = new Element();
                    List<Item> itemTempList = new ArrayList<>();
                    itemTempList.add(productAux);
                    if (productAux.getId() > 0) elementAux.setId(String.valueOf(productAux.getId()));
                    elementAux.setProduct(itemTempList);
                    elementAux.setType(ProductTypeEnum.UNIQUE);
                    elementAux.setAction(ActionEnum.FAVORITE);
                    finalelementList.add(elementAux);
                });
            }

            dynamicSectionAux.setList(finalelementList);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Ocurrió un error en setRelatedItems. Error: " + e.getMessage(), e);
        }

    }

    /**
     * set tutorias
     *
     * @param dynamicSectionAux
     * @param itemInfoConfigData
     * @param sectionAlgolia
     */
    private void setTutorials(DynamicSection dynamicSectionAux, ItemInfoConfigData itemInfoConfigData, Component sectionAlgolia) {
        List<Element> elementBannerList = new ArrayList<>();
        if (Objects.nonNull(itemInfoConfigData.getItemData())) {
            try {

                List<VideoData> tutorials = APIAlgolia.getVideosForItem(itemInfoConfigData.getItemData().getId());
                if (!tutorials.isEmpty()) {
                    tutorials.forEach(tutorial -> {
                        Element elementBannerAux = new Element();
                        elementBannerAux.setUrl(tutorial.getUrl());
                        elementBannerAux.setOrderingNumber(tutorial.getPosition());
                        elementBannerAux.setTitle(tutorial.getTitle());
                        elementBannerAux.setThumbnail(tutorial.getThumbnail());
                        elementBannerAux.setProducts(tutorial.getProducts());
                        elementBannerAux.setAuthor(tutorial.getAuthor());
                        elementBannerList.add(elementBannerAux);
                    });
                    dynamicSectionAux.setList(elementBannerList);
                }
            } catch (Exception e) {
                log.warning("Error insertando los tutoriales.");
            }
        }
    }

    @ApiMethod(name = "getItemSubstitutes", path = "/productEndpoint/getItemSubstitutes", httpMethod = ApiMethod.HttpMethod.GET)
    public List<Substitutes> getItemSubstitutesByItemIdAndRequestedAndStoreId(@Named("itemId") final Long itemId, @Named("requestedQuantity") final Integer requestQuantity, @Named("storeId") final Integer storeId) {
        return productsMethods.getItemSubstitutesByItemIdAndRequestedAndStoreId(itemId, requestQuantity, storeId);
    }

    @ApiMethod(name = "getProductInGroup", path = "/productEndpoint/getProductInGroup", httpMethod = ApiMethod.HttpMethod.POST)
    public CollectionResponse<Item> getProductInGroup(
            final ProductGroup productGroup) throws ConflictException, BadRequestException {
        if (!authenticate.isValidToken(productGroup.getToken(), productGroup.getTokenIdWebSafe()))
            throw new ConflictException(Constants.INVALID_TOKEN);

        List<Item> suggestedItems = null;

        if (productGroup.getItems() != null) {
            suggestedItems = productsMethods.getItemsByIds(productGroup.getItems(), productGroup.getIdStoreGroup());
        }
        if (suggestedItems != null)
            suggestedItems.sort(Item.StockComparator);

        return CollectionResponse.<Item>builder().setItems(suggestedItems).build();
    }

    @ApiMethod(name = "getHighlightItems", path = "/productEndpoint/getHighlightItems", httpMethod = ApiMethod.HttpMethod.GET)
    @Deprecated
    public CollectionResponseModel getHighlightItems(@Named("token") final String token,
                                                     @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                                     @Named("idHighlight") final long idHighlight,
                                                     @Named("idStoreGroup") final long idStoreGroupFromRequest,
                                                     @Nullable @Named("isWeb") final Boolean isWeb,
                                                     @Nullable @Named("cursor") String cursor) throws ConflictException, BadRequestException, AlgoliaException {
        if (!authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);
        Highlight highlight = ofy().load().type(Highlight.class).filter("id", idHighlight).first().now();

        long idStoreGroup = APIAlgolia.getDefaultStoreIdByStoreId((int) idStoreGroupFromRequest);

        final Integer limitResultsClient = 12;

        List<Suggested> itemsHighlight = highlight.getItems();
        List<Long> idItems = new ArrayList<>();

        for (Suggested suggestedProducts : itemsHighlight) {
            idItems.add(suggestedProducts.getItem());
        }
        Query.Filter filterId = new Query.FilterPredicate("id", Query.FilterOperator.IN, idItems);
        com.googlecode.objectify.cmd.Query<Item> query = ofy().load().type(Item.class).filter(filterId);
        List<Item> items = query.list();

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
            item = productsMethods.setStoreInfo(item, idStoreGroup);
            itemsToRetrieve.add(item);
        }
        CollectionResponseModel collectionResponseModel = new CollectionResponseModel();
        Collections.sort(itemsToRetrieve, Item.StockComparator);

        // get order by algolia
        GroupSorted groupSorted = APIAlgolia.getSortedItemsInGroup(String.valueOf(idHighlight));
//        log.info("Validar Algolia items ordenados.");
        if (groupSorted != null && groupSorted.getItems() != null && !groupSorted.getItems().isEmpty()) {
            Objects.requireNonNull(groupSorted).getItems().forEach(item -> log.info("item -> " + item.getId() + "pos -> " + item.getPos()));

            // sorted items by algolia
            for (ItemAlgoliaSort itemAlgolia : groupSorted.getItems()) {
                for (Item itemRetrieve : itemsToRetrieve) {
                    if (itemRetrieve.getId() == itemAlgolia.getId()) {
                        itemRetrieve.setPosGroup(itemAlgolia.getPos());
                    }
                }

            }
            itemsToRetrieve.sort(Comparator.comparing(Item::getPosGroup));

            itemsToRetrieve.forEach(item -> log.info("item -> " + item.getId() + " pos final -> " + item.getPosGroup()));


        } else {
            log.info("items sorted is null");
        }


        collectionResponseModel.setItems(itemsToRetrieve);
        collectionResponseModel.setNextPageToken(finish.toString());
        if (isWeb != null && isWeb)
            collectionResponseModel.setTotalProducts((long) items.size());
        return collectionResponseModel;
    }

    @ApiMethod(name = "getHighlightHead", path = "/productEndpoint/getHighlightHead", httpMethod = ApiMethod.HttpMethod.GET)
    public Highlight getHighlight(@Named("token") final String token,
                                  @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                  @Named("idHighlight") final long idHighlight)
            throws ConflictException, BadRequestException {
        if (!authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);
        return ofy().load().type(Highlight.class).filter("id", idHighlight).first().now();
    }

    @ApiMethod(name = "getSuggests", path = "/productEndpoint/getSuggests", httpMethod = ApiMethod.HttpMethod.GET)
    public Suggesteds getSuggests(@Named("token") final String token,
                                  @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                  @Named("idStoreGroup") final int idStoreGroupFromRequest)
            throws ConflictException, BadRequestException, IOException, AlgoliaException {
        if (!authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);

        Suggesteds suggestedsCache = getSuggestedsCache(idStoreGroupFromRequest);
        if(Objects.nonNull(suggestedsCache)){
            return suggestedsCache;
        }

        int idStoreGroup = APIAlgolia.getDefaultStoreIdByStoreId(idStoreGroupFromRequest);
        Suggesteds suggestedsAux = productsMethods.getSuggesteds(idStoreGroup, null);

        if (Objects.nonNull(suggestedsAux)) {
            saveSuggestsCache(suggestedsAux, idStoreGroupFromRequest);
        }

        if (suggestedsAux != null) return suggestedsAux;
        return new Suggesteds();
    }

    /**
     * Saves the suggests data to cache.
     * @param suggesteds The suggests data to cache
     * @param idStoreGroupFromRequest The store group ID
     */
    private void saveSuggestsCache(Suggesteds suggesteds, int idStoreGroupFromRequest){
        try {
            String jsonToCache = new Gson().toJson(suggesteds);
            CachedDataManager.setSuggestsCache(ProductConstant.SUGGESTS_CACHE_KEY + "-" + idStoreGroupFromRequest, jsonToCache);
        }catch (Exception e){
            log.warning("saveSuggestsCache Exception "+e.getMessage());
        }
    }

    /**
     * Retrieves the suggests data from cache.
     * @param idStoreGroupFromRequest The store group ID
     * @return The cached suggests data or null if not found
     */
    private Suggesteds getSuggestedsCache(int idStoreGroupFromRequest){
        String cachedJson = CachedDataManager.getSuggestsCache(ProductConstant.SUGGESTS_CACHE_KEY+"-"+idStoreGroupFromRequest);
        if(Objects.isNull(cachedJson)){
            return null;
        }

        try {
            return new Gson().fromJson(cachedJson, Suggesteds.class);
        }catch (Exception e){
            log.warning("getSuggestedsCache Exception "+e.getMessage());
            return null;
        }
    }


    @ApiMethod(name = "getHighlightsByDepartment", path = "/productEndpoint/getHighlightsByDepartment", httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponseModel getHighlightsByDepartment(@Named("token") final String token,
                                                             @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                                             @Named("idDepartment") final long idDepartment,
                                                             @Named("idStoreGroup") final long idStoreGroupFromRequest)
            throws ConflictException, BadRequestException, AlgoliaException {
//        log.info("method:  getHighlightsByDepartment");
        return getGenericHighlights(token, tokenIdWebSafe, idDepartment, idStoreGroupFromRequest);
    }

    @ApiMethod(name = "wgetHighlights", path = "/productEndpoint/getHighlights", httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponseModel getHighlights(@Named("token") final String token,
                                                 @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                                 @Named("idStoreGroup") final long idStoreGroupFromRequest)
            throws ConflictException, BadRequestException, AlgoliaException {
//        log.info("method:  getHighlights");
        return getGenericHighlights(token, tokenIdWebSafe, 0L, idStoreGroupFromRequest);
    }


    /**
     * Servicio encargado de Asignar el orden a los items desde BackOffice
     *
     * @param productSortGroup
     * @return
     * @throws ConflictException
     * @throws BadRequestException
     */
    @ApiMethod(name = "setSortHighlightItems", path = "/productEndpoint/setSortHighlightItems", httpMethod = ApiMethod.HttpMethod.POST)
    public Answer setSortHighlightItems(final ProductSortGroup productSortGroup) throws ConflictException, BadRequestException {
        Answer answer = new Answer();
        if (Objects.isNull(productSortGroup)) {
//            log.info("method: setSortHighlightItems() --> BadRequest [productSortGroup is required]");
            throw new BadRequestException("BadRequest [productSortGroup is required]");
        } else if (Objects.isNull(productSortGroup.getIdGroup())) {
//            log.info("method: setSortHighlightItems() --> BadRequest [productSortGroup.getIdGroup is required]");
            throw new BadRequestException("BadRequest [productSortGroup.getIdGroup is required]");
        } else if (Objects.isNull(productSortGroup.getItems()) || productSortGroup.getItems().isEmpty()) {
//            log.info("method: setSortHighlightItems() --> BadRequest [productSortGroup.getItems is required]");
            throw new BadRequestException("BadRequest [productSortGroup.getItems is required]");
        }
        try {
            GroupSorted groupSorted = APIAlgolia.getSortedItemsInGroup(String.valueOf(productSortGroup.getIdGroup()));
//            log.info("method: setSortHighlightItems() --> update group " + productSortGroup.getIdGroup());
            answer.setConfirmation(APIAlgolia.saveSortedItemsInGroup(productSortGroup));
            return answer;
        } catch (Exception e) {
            log.warning("method: setSortHighlightItems() --> error: " + e.fillInStackTrace());
            throw new BadRequestException("Bad Request");
        }
    }

    @ApiMethod(name = "updateHighlight", path = "/productEndpoint/updateHighlight", httpMethod = ApiMethod.HttpMethod.POST)
    public Answer updateHighlight(RequestUpdateHighlightSuggested requestUpdateHighlightSuggested)
            throws ConflictException, BadRequestException {
        return new Answer(APIAlgolia.updateHighlightsProducts(requestUpdateHighlightSuggested));
    }

    @ApiMethod(name = "updateSuggests", path = "/productEndpoint/updateSuggests", httpMethod = ApiMethod.HttpMethod.POST)
    public Answer updateSuggests(RequestUpdateHighlightSuggested requestUpdateHighlightSuggested)
            throws ConflictException, BadRequestException {
        return new Answer(APIAlgolia.updateSuggestedProducts(requestUpdateHighlightSuggested));
    }

    @ApiMethod(name = "uploadOffer", path = "/productEndpoint/uploadOffer", httpMethod = ApiMethod.HttpMethod.POST)
    public Answer updateOffer(RequestManageOffer requestManageOffer)
            throws ConflictException, BadRequestException {
        return new Answer(APIAlgolia.uploadOffer(requestManageOffer));
    }

    @ApiMethod(name = "downloadOffer", path = "/productEndpoint/downloadOffer", httpMethod = ApiMethod.HttpMethod.POST)
    public Answer downloadOffer(RequestManageOffer requestManageOffer)
            throws ConflictException, BadRequestException {
        return new Answer(APIAlgolia.deleteOffer(requestManageOffer));
    }

    @ApiMethod(name = "itemStockUpdate", path = "/productEndpoint/item/stock/update", httpMethod = ApiMethod.HttpMethod.PUT)
    public ClientResponse itemStockUpdate(HttpServletRequest request,
                                          final ItemStock itemStock) throws BadRequestException, ConflictException,
            IOException, InternalServerErrorException, NotFoundException, UnauthorizedException {
        String token = request.getHeader("token");
//        log.info("method: itemStockUpdate() -> params: token : " + token);
        if (Objects.isNull(token) || token.isEmpty()) {
            log.warning("method: itemStockUpdate() --> BadRequest [token is required]");
            throw new BadRequestException("BadRequest [token is required]");
        } else if (Objects.isNull(itemStock) || Objects.isNull(itemStock.getItems()) || itemStock.getItems().isEmpty()) {
            log.warning("method: itemStockUpdate() --> BadRequest [items is required]");
            throw new BadRequestException("BadRequest [items is required]");
        }
        try {
            return ApiGatewayService.get().orderProviderStockUpdate(token, itemStock);
        } catch (Exception e) {
            log.warning("method: itemStockUpdate() Error--> " + e.fillInStackTrace());
            return new ClientResponse(400, e.getMessage());
        }
    }

    @ApiMethod(name = "descriptionsItem", path = "/productEndpoint/descriptionsItem", httpMethod = ApiMethod.HttpMethod.PUT)
    public Answer descriptionsItem(final ItemDescriptions itemDescriptions) throws BadRequestException {
        log.info("method: descriptionsItem() -> params: itemId : " + itemDescriptions.getItemId() + " grayDescription: " + itemDescriptions.getGrayDescription() + " mediaDescription: " + itemDescriptions.getMediaDescription() + " largeDescription: " + itemDescriptions.getLargeDescription());
        if (Objects.isNull(itemDescriptions.getItemId()) || itemDescriptions.getItemId() <= 0) {
            log.warning("method: descriptionsItem() --> BadRequest [itemId is required]");
            throw new BadRequestException("BadRequest [itemId is required]");
        } else if ((Objects.isNull(itemDescriptions.getGrayDescription()) || Objects.isNull(itemDescriptions.getMediaDescription()) || Objects.isNull(itemDescriptions.getLargeDescription())) ||
                (itemDescriptions.getGrayDescription().isEmpty() || itemDescriptions.getMediaDescription().isEmpty() || itemDescriptions.getLargeDescription().isEmpty())) {
            log.warning("method: descriptionsItem() --> BadRequest [descriptions is required]");
            throw new BadRequestException("BadRequest [descriptions is required]");
        }
        return new Answer(APIAlgolia.updateDescriptionItemAlgolia(itemDescriptions.getItemId(), itemDescriptions.getGrayDescription(), itemDescriptions.getMediaDescription(), itemDescriptions.getLargeDescription()));
    }

    @ApiMethod(name = "highlight", path = "/productEndpoint/highlight", httpMethod = ApiMethod.HttpMethod.POST)
    public Answer highlight(final com.imaginamos.farmatodo.model.algolia.Highlight highlight) throws BadRequestException {
        log.info("method: highlight() -> params: highlight : " + highlight);
        if (Objects.isNull(highlight)) {
            log.warning("method: highlight() --> BadRequest [highlight is required]");
            throw new BadRequestException("BadRequest [highlight is required]");
        } else if (Objects.isNull(highlight.getId())) {
            log.warning("method: highlight() --> BadRequest [highlight - id is required]");
            throw new BadRequestException("BadRequest [highlight id is required]");
        } else if (Objects.isNull(highlight.getFirstDescription())) {
            log.warning("method: highlight() --> BadRequest [highlight - FirstDescription is required]");
            throw new BadRequestException("BadRequest [highlight FirstDescription is required]");
        } else if (Objects.isNull(highlight.getUrlImage())) {
            log.warning("method: highlight() --> BadRequest [highlight - UrlImage is required]");
            throw new BadRequestException("BadRequest [highlight UrlImage is required]");
        } else if (Objects.isNull(highlight.getType())) {
            log.warning("method: highlight() --> BadRequest [highlight - Type is required]");
            throw new BadRequestException("BadRequest [highlight Type is required]");
        } else if (Objects.nonNull(highlight.getCategories())) {
            log.warning("method: highlight() --> BadRequest [Categories - Type is required]");
            throw new BadRequestException("BadRequest [Categories Type is required]");
        } else if (Objects.nonNull(highlight.getItems())) {
            log.warning("method: highlight() --> BadRequest [Items - Type is required]");
            throw new BadRequestException("BadRequest [Items Type is required]");
        }
        highlight.setHighlightHead(Boolean.TRUE);
        return new Answer(APIAlgolia.handlerHighlight(highlight));
    }

    @ApiMethod(name = "updateHighlightHead", path = "/productEndpoint/highlight", httpMethod = ApiMethod.HttpMethod.PUT)
    public Answer updateHighlightHead(final com.imaginamos.farmatodo.model.algolia.Highlight highlight) throws BadRequestException {
        log.info("method: highlight() -> params: highlight : " + highlight);
        if (Objects.isNull(highlight)) {
            log.warning("method: highlight() --> BadRequest [highlight is required]");
            throw new BadRequestException("BadRequest [highlight is required]");
        } else if (Objects.isNull(highlight.getId())) {
            log.warning("method: highlight() --> BadRequest [highlight - id is required]");
            throw new BadRequestException("BadRequest [highlight id is required]");
        } else if (Objects.isNull(highlight.getFirstDescription())) {
            log.warning("method: highlight() --> BadRequest [highlight - FirstDescription is required]");
            throw new BadRequestException("BadRequest [highlight FirstDescription is required]");
        } else if (Objects.isNull(highlight.getUrlImage())) {
            log.warning("method: highlight() --> BadRequest [highlight - UrlImage is required]");
            throw new BadRequestException("BadRequest [highlight UrlImage is required]");
        } else if (Objects.isNull(highlight.getType())) {
            log.warning("method: highlight() --> BadRequest [highlight - Type is required]");
            throw new BadRequestException("BadRequest [highlight Type is required]");
        } else if (Objects.nonNull(highlight.getCategories())) {
            log.warning("method: highlight() --> BadRequest [Categories - Type is required]");
            throw new BadRequestException("BadRequest [Categories Type is required]");
        } else if (Objects.nonNull(highlight.getItems())) {
            log.warning("method: highlight() --> BadRequest [Items - Type is required]");
            throw new BadRequestException("BadRequest [Items Type is required]");
        }
        highlight.setHighlightHead(Boolean.TRUE);
        return new Answer(APIAlgolia.handlerHighlight(highlight));
    }

    @ApiMethod(name = "deleteHighlightHead", path = "/productEndpoint/highlight", httpMethod = ApiMethod.HttpMethod.DELETE)
    public Answer deleteHighlightHead(@Named("idHighlight") final String idHighlight) throws BadRequestException {
        log.info("method: highlight() -> params: highlight : " + idHighlight);
        if (Objects.isNull(idHighlight) || idHighlight.isEmpty()) {
            log.warning("method: highlight() --> BadRequest [idHighlight is required]");
            throw new BadRequestException("BadRequest [idHighlight is required]");
        }
        return new Answer(APIAlgolia.deleteHighlight(com.imaginamos.farmatodo.model.algolia.Highlight.OBJECT_ID + idHighlight));
    }

    @ApiMethod(name = "migrateHighlightToAlgolia", path = "/productEndpoint/migrateHighlightToAlgolia", httpMethod = ApiMethod.HttpMethod.POST)
    public Answer migrateHighlightToAlgolia() throws BadRequestException {
//        log.info("method:  migrateHighlightToAlgolia");
        List<Highlight> highlightedItems = ofy().load().type(Highlight.class).order("orderingNumber").list();
        long current = System.currentTimeMillis();

        List<com.imaginamos.farmatodo.model.algolia.Highlight> highlightAlgList = highlightedItems.stream().filter(highlight -> (highlight.getStartDate() < current && current <= highlight.getEndDate())).map(highlight -> {
//            log.info("Item Base:  migrateHighlightToAlgolia highlight.getId()" + highlight.getId() + " - " + highlight.getStartDate() + " - " + current + " - " + highlight.getEndDate());
            com.imaginamos.farmatodo.model.algolia.Highlight highlightAlg =
                    new com.imaginamos.farmatodo.model.algolia.Highlight(highlight.getId(), highlight.getFirstDescription(), highlight.getSecondDescription(), highlight.getOfferDescription(), highlight.getOfferText(),
                            highlight.getType(), highlight.getUrlImage(), new Date(highlight.getStartDate()), new Date(highlight.getEndDate()), highlight.getOrderingNumber(),
                            ((Objects.nonNull(highlight.getItems()) && !highlight.getItems().isEmpty()) ? highlight.getItems().stream().map(item -> item.getItem()).collect(Collectors.toList()) : null),
                            highlight.getItem(),
                            ((Objects.nonNull(highlight.getProduct()) && !highlight.getProduct().isEmpty()) ? highlight.getProduct().stream().map(product -> product.getId()).collect(Collectors.toList()) : null),
                            ((Objects.nonNull(highlight.getCategories()) && !highlight.getCategories().isEmpty()) ? highlight.getCategories().stream().map(category -> category.getClasification()).collect(Collectors.toList()) : null));
            return highlightAlg;
        }).collect(Collectors.toList());
        APIAlgolia.addHighlightList(highlightAlgList);
        return new Answer(true);
    }

    @ApiMethod(name = "highlightsItem", path = "/productEndpoint/highlightsItem", httpMethod = ApiMethod.HttpMethod.PUT)
    public Answer highlightsItem(final ItemGroupToAdd itemGroupToAdd) throws BadRequestException {
        log.info("method: highlightsItem() -> params: itemId : " + itemGroupToAdd.getItemId() + " getGroupToAddId: " + itemGroupToAdd.getGroupToAddId());
        if (Objects.isNull(itemGroupToAdd.getItemId()) || itemGroupToAdd.getItemId() <= 0) {
            log.warning("method: highlightsItem() --> BadRequest [itemId is required]");
            throw new BadRequestException("BadRequest [itemId is required]");
        } else if (Objects.isNull(itemGroupToAdd.getGroupToAddId()) || itemGroupToAdd.getGroupToAddId() <= 0) {
            log.warning("method: highlightsItem() --> BadRequest [getGroupToAddId is required]");
            throw new BadRequestException("BadRequest [getGroupToAddId is required]");
        }
        return new Answer(APIAlgolia.addHighlightOrSuggestItemAlgolia(itemGroupToAdd.getItemId(), itemGroupToAdd.getGroupToAddId(), true));
    }

    @ApiMethod(name = "deleteHighlightsItem", path = "/productEndpoint/highlightsItem", httpMethod = ApiMethod.HttpMethod.DELETE)
    public Answer deleteHighlightsItem(@Named("itemId") final Long itemId, @Named("groupId") final Long groupId) throws BadRequestException {
        log.info("method: deleteHighlightsItem() -> params: itemId : " + itemId + " groupId: " + groupId);
        if (Objects.isNull(itemId) || itemId <= 0) {
            log.warning("method: deleteHighlightsItem() --> BadRequest [itemId is required]");
            throw new BadRequestException("BadRequest [itemId is required]");
        } else if (Objects.isNull(groupId) || groupId <= 0) {
            log.warning("method: deleteHighlightsItem() --> BadRequest [getGroupToAddId is required]");
            throw new BadRequestException("BadRequest [getGroupToAddId is required]");
        }
        return new Answer(APIAlgolia.deleteHighlightOrSuggestItemAlgolia(itemId, groupId, true));
    }

    @ApiMethod(name = "suggestedItem", path = "/productEndpoint/suggestedItem", httpMethod = ApiMethod.HttpMethod.PUT)
    public Answer suggestedItem(final ItemGroupToAdd itemGroupToAdd) throws BadRequestException {
        log.info("method: suggestedItem() -> params: itemId : " + itemGroupToAdd.getItemId() + " getGroupToAddId: " + itemGroupToAdd.getGroupToAddId());
        if (Objects.isNull(itemGroupToAdd.getItemId()) || itemGroupToAdd.getItemId() <= 0) {
            log.warning("method: suggestedItem() --> BadRequest [itemId is required]");
            throw new BadRequestException("BadRequest [itemId is required]");
        } else if (Objects.isNull(itemGroupToAdd.getGroupToAddId()) || itemGroupToAdd.getGroupToAddId() <= 0) {
            log.warning("method: suggestedItem() --> BadRequest [getGroupToAddId is required]");
            throw new BadRequestException("BadRequest [getGroupToAddId is required]");
        }
        return new Answer(APIAlgolia.addHighlightOrSuggestItemAlgolia(itemGroupToAdd.getItemId(), itemGroupToAdd.getGroupToAddId(), false));
    }

    @ApiMethod(name = "deleteSuggestedItem", path = "/productEndpoint/suggestedItem", httpMethod = ApiMethod.HttpMethod.DELETE)
    public Answer deleteSuggestedItem(@Named("itemId") final Long itemId, @Named("groupId") final Long groupId) throws BadRequestException {
        log.info("method: deleteSuggestedItem() -> params: itemId : " + itemId + " groupId: " + groupId);
        if (Objects.isNull(itemId) || itemId <= 0) {
            log.warning("method: deleteSuggestedItem() --> BadRequest [itemId is required]");
            throw new BadRequestException("BadRequest [itemId is required]");
        } else if (Objects.isNull(groupId) || groupId <= 0) {
            log.warning("method: deleteSuggestedItem() --> BadRequest [groupId is required]");
            throw new BadRequestException("BadRequest [groupId is required]");
        }
        return new Answer(APIAlgolia.deleteHighlightOrSuggestItemAlgolia(itemId, groupId, false));
    }

    @ApiMethod(name = "offerItem", path = "/productEndpoint/offerItem", httpMethod = ApiMethod.HttpMethod.PUT)
    public Answer offerItem(final ItemOffer itemOffer) throws BadRequestException {
        log.info("method: offerItem() -> params: itemId : " + itemOffer.getItemId() + " getGroupToAddId: " + itemOffer.getOfferDescription());
        if (Objects.isNull(itemOffer.getItemId()) || itemOffer.getItemId() <= 0) {
            log.warning("method: offerItem() --> BadRequest [itemId is required]");
            throw new BadRequestException("BadRequest [itemId is required]");
        } else if ((Objects.isNull(itemOffer.getOfferDescription()) || Objects.isNull(itemOffer.getOfferText()) || Objects.isNull(itemOffer.getOfferPrice())) ||
                (itemOffer.getOfferDescription().isEmpty() || itemOffer.getOfferText().isEmpty()) || itemOffer.getOfferPrice() <= 0) {
            log.warning("method: offerItem() --> BadRequest [offerItem information is required]");
            throw new BadRequestException("BadRequest [offerItem information is required]");
        }
        return new Answer(APIAlgolia.updateOfferItemAlgolia(itemOffer));
    }

    private CollectionResponseModel getGenericHighlights(final String token, final String tokenIdWebSafe, final long idDepartment, final long idStoreGroupFromRequest)
            throws ConflictException, BadRequestException, AlgoliaException {
        if (!authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);
        long idStoreGroup = APIAlgolia.getDefaultStoreIdByStoreId((int) idStoreGroupFromRequest);
        List<Highlight> highlightList = productsMethods.getHighlightsFromDeptAndStore(idDepartment, idStoreGroup);
        //log.info("method:  getGenericHighlights idStoreGroup: " + idStoreGroup + " Date: " + new DateTime());
        CollectionResponseModel collectionResponseModel = new CollectionResponseModel();
        collectionResponseModel.setHighlightList(highlightList.stream().sorted(sortHighlight).collect(Collectors.toList()));
        return collectionResponseModel;
    }

    /**
     * Quita los items duplicados de la lista.
     *
     * @param deliveryOrderItemList
     */
    private void validateDuplicateItems(List<DeliveryOrderItem> deliveryOrderItemList) {
        if (Objects.nonNull(deliveryOrderItemList) && !deliveryOrderItemList.isEmpty()) {
            Map<Long, DeliveryOrderItem> listItemResult = new HashMap<>();
            deliveryOrderItemList.stream().filter(item -> Objects.nonNull(item)).forEach(item -> {
                if (listItemResult.containsKey(item.getId())) {
                    if (listItemResult.get(item.getId()).getQuantitySold() < item.getQuantitySold()) {
                        listItemResult.get(item.getId()).setQuantitySold(item.getQuantitySold());
                    }
                    ofy().delete().entities(item);
                } else {
                    listItemResult.put(item.getId(), item);
                }
            });
            deliveryOrderItemList = listItemResult.values().stream().collect(Collectors.toList());
        }
    }

    /**
     * Parses the nearbyStores to a list of Long.
     * @param nearbyStores String with nearbyStores separated by commas.
     * @return List of Long with the nearbyStores.
     * @throws BadRequestException If nearbyStores does not meet the format.
     */
    private List<Long> parseNearbyStores(String nearbyStores) throws BadRequestException {
        if (Objects.nonNull(nearbyStores) && !nearbyStores.isEmpty()) {
            if (!FTDUtil.isValidRegex(nearbyStores, Constants.NEARBY_STORES_GET_REGEX)) {
                throw new BadRequestException(Constants.NEARBY_STORES_REGEX_GET_ERROR);
            }
            try {
                return Arrays.stream(nearbyStores.split(","))
                        .map(Long::parseLong)
                        .collect(Collectors.toList());
            } catch (NumberFormatException e) {
                throw new BadRequestException("Invalid store ID in nearby stores: " + e.getMessage());
            }
        }
        return new ArrayList<>();
    }

}
