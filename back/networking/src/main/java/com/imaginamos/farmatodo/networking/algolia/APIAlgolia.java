package com.imaginamos.farmatodo.networking.algolia;


import com.algolia.search.APIClient;
import com.algolia.search.AppEngineAPIClientBuilder;
import com.algolia.search.Index;
import com.algolia.search.exceptions.AlgoliaException;
import com.algolia.search.iterators.IndexIterable;
import com.algolia.search.objects.IndexQuery;
import com.algolia.search.objects.Query;
import com.algolia.search.objects.RequestOptions;
import com.algolia.search.objects.tasks.sync.TaskSingleIndex;
import com.algolia.search.responses.SearchResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.server.spi.response.NotFoundException;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.imaginamos.farmatodo.model.algolia.Highlight;
import com.imaginamos.farmatodo.model.algolia.*;
import com.imaginamos.farmatodo.model.algolia.autocomplete.AutocompleteByCityConfig;
import com.imaginamos.farmatodo.model.algolia.cuponFilters.CouponFiltersConfig;
import com.imaginamos.farmatodo.model.algolia.delivery.DeliveryFree;
import com.imaginamos.farmatodo.model.algolia.eta.ETAConfig;
import com.imaginamos.farmatodo.model.algolia.filters.GenericFiltersConfig;
import com.imaginamos.farmatodo.model.algolia.flag.FlagCountries;
import com.imaginamos.farmatodo.model.algolia.flag.FlagRegistry;
import com.imaginamos.farmatodo.model.algolia.login.AlgoliaEmailConfig;
import com.imaginamos.farmatodo.model.algolia.login.TimeOut;
import com.imaginamos.farmatodo.model.algolia.messageconfig.MessageSmsConfig;
import com.imaginamos.farmatodo.model.categories.Department;
import com.imaginamos.farmatodo.model.city.GeoGridsConfigAlgolia;
import com.imaginamos.farmatodo.model.customer.SuggestedObject;
import com.imaginamos.farmatodo.model.dto.EnableForEnum;
import com.imaginamos.farmatodo.model.dto.VideoData;
import com.imaginamos.farmatodo.model.home.BannerDataCMSType;
import com.imaginamos.farmatodo.model.home.HomeConfigAlgolia;
import com.imaginamos.farmatodo.model.item.OpticalItemFilter;
import com.imaginamos.farmatodo.model.item.TtlCacheAlgoliaRecommendRes;
import com.imaginamos.farmatodo.model.optics.*;
import com.imaginamos.farmatodo.model.order.FulfilOrdColDescDomain;
import com.imaginamos.farmatodo.model.photoSlurp.PhotoSlurpConfigAlgolia;
import com.imaginamos.farmatodo.model.product.Item;
import com.imaginamos.farmatodo.model.product.*;
import com.imaginamos.farmatodo.model.productDetail.ItemConfigAlgolia;
import com.imaginamos.farmatodo.model.provider.LandingProviderAndActiveAlgolia;
import com.imaginamos.farmatodo.model.provider.ProviderConfigAlgolia;
import com.imaginamos.farmatodo.model.provider.ProviderResponse;
import com.imaginamos.farmatodo.model.stock.SubtractStockConfigAlgolia;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.model.util.URLConnections;
import com.imaginamos.farmatodo.networking.cache.CachedDataManager;
import com.imaginamos.farmatodo.networking.growthbook.GrowthBookConfigLoader;
import com.imaginamos.farmatodo.networking.models.addresses.ConfigValidateAddress;
import com.imaginamos.farmatodo.networking.models.addresses.GAutocompleteRes;
import com.imaginamos.farmatodo.networking.models.addresses.GPlacePredictionRes;
import com.imaginamos.farmatodo.networking.models.addresses.KeyWordsCityConfig;
import com.imaginamos.farmatodo.networking.models.addresses.autocompletefarmatodo.PlaceAlgolia;
import com.imaginamos.farmatodo.networking.models.addresses.geocodingfarmatodo.AddressAlgolia;
import com.imaginamos.farmatodo.networking.models.algolia.CarruselRecommendConfig;
import com.imaginamos.farmatodo.networking.models.algolia.OrderMessageConfiguration;
import com.imaginamos.farmatodo.networking.models.algolia.TalonOneConfig;
import com.imaginamos.farmatodo.networking.models.algolia.TalonOnePetalConfig;
import com.imaginamos.farmatodo.networking.models.algolia.WhatsAapSendMessageConfig;
import com.imaginamos.farmatodo.networking.services.ApiAlgoliaProxyService;
import com.imaginamos.farmatodo.networking.services.ApiAlgoliaRecommendService;
import com.imaginamos.farmatodo.networking.services.OpticsServices;
import com.imaginamos.farmatodo.networking.talonone.model.BagItem;
import com.imaginamos.farmatodo.networking.talonone.model.ExtendedBagPropertiesTalonOne;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Integer.parseInt;
import static java.util.Comparator.nullsLast;

/**
 * Created by JPuentes on 30/10/2018.
 */
public class APIAlgolia {

    private static final Logger LOG = Logger.getLogger(APIAlgolia.class.getName());
    private static final Comparator<ItemAlgolia> ORDER_ITEM_BY_STORE_ID = (item1, item2) -> Integer.compare(item1.getIdStoreGroup(), item2.getIdStoreGroup());
    private static final int MAX_ITEMS_HISTORY = 15;

    private static final int ID_SUBCATEGORY_CONTACT_LENSES = 451;
    private static final String CATEGORIE_CONTACT_LENSES = "Cuidado de la vista";
    private static final int MIN_HAS_STOCK = 20;
    public static APIClient algoliaClient;

    static{
        algoliaClient = new AppEngineAPIClientBuilder(URLConnections.ALGOLIA_APP_ID,URLConnections.ALGOLIA_API_KEY).build();
    }

    public static boolean itemIsOnlyOnline(final String objectId) {
        try {
            Index<ItemAlgolia> index = algoliaClient.initIndex(URLConnections.ALGOLIA_PRODUCTS, ItemAlgolia.class);
            Optional<ItemAlgolia> item = index.getObject(objectId);
            //LOG.info("item to search -> " + objectId);
            //LOG.info("item encontrado -> " + item.get().getDescription());
            return item.get().isOnlyOnline();
        } catch (AlgoliaException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static ItemAlgolia getItemAlgolia(final String objectId) {
        try {
//            LOG.info("item to search -> " + objectId);
            Index<ItemAlgolia> index = algoliaClient.initIndex(URLConnections.ALGOLIA_PRODUCTS, ItemAlgolia.class);
            return index.getObject(objectId).orElse(null);
        } catch (AlgoliaException | NoSuchElementException e) {
            LOG.log(Level.WARNING, "getItemAlgolia() Error obteniendo item en algolia", e);
        }
        return null;
    }

    /**
     * get item by object id using new index and REST API
     * @param objectId object id "item id"
     * @param nearbyStores nearby stores to search
     * @return ItemAlgolia
     */
    public static Optional<ItemAlgolia> getItemAlgoliaRestAPI(final String objectId, final String nearbyStores) {
        try {
            return ApiAlgoliaProxyService
                    .Companion
                    .getInstance()
                    .getItemByObjectID(Constants.PRODUCTS_COL_INDEX, objectId, nearbyStores);
        } catch (Exception e) {
            LOG.severe("Error getting item from Algolia REST API: " + e.getMessage());
        }
        return Optional.empty();
    }

    public static ItemOfferAlgolia getItemOfferAlgolia(final String objectId) {
        try {
            Index<ItemOfferAlgolia> index = algoliaClient.initIndex(URLConnections.ALGOLIA_PRODUCTS, ItemOfferAlgolia.class);
            ItemOfferAlgolia itemAlgolia = index.getObject(objectId).orElse(null);
            return itemAlgolia;
        } catch (AlgoliaException | NoSuchElementException e) {
            e.printStackTrace();
        }
        return null;
    }

    // CACHED
    public static PaymentMethodsAlgolia getPaymentMethodsByDeliveryType() throws AlgoliaException {

        PaymentMethodsAlgolia paymentMethodsAlgolia = new PaymentMethodsAlgolia();

        Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_PAYMENTMETHODS_DELIVERYTYPE);
        Optional<PaymentMethodsAlgolia> optionalPaymentMethodsAlgolia;
        // algolia
        if (!jsonCachedOptional.isPresent()){
            Index<PaymentMethodsAlgolia> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, PaymentMethodsAlgolia.class);
            optionalPaymentMethodsAlgolia = index.getObject(URLConnections.ALGOLIA_PAYMENTMETHODS_DELIVERYTYPE);

            if (optionalPaymentMethodsAlgolia.isPresent()){
                String json = new Gson().toJson(optionalPaymentMethodsAlgolia.get());
                CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_PAYMENTMETHODS_DELIVERYTYPE, json);
                paymentMethodsAlgolia = optionalPaymentMethodsAlgolia.get();
            }
            return paymentMethodsAlgolia;
        }
        // cache
        paymentMethodsAlgolia = new Gson().fromJson(jsonCachedOptional.get(), PaymentMethodsAlgolia.class);

        return paymentMethodsAlgolia;
    }

    /**
     * getItemAlgoliaByBarcode
     * @param barcode
     * @param idStoreGroup
     * @return ItemAlgolia
     */
    public static ItemAlgolia getItemAlgoliaByBarcode(final String barcode, long idStoreGroup) {
        try {
            StringBuilder filterBufferBarcode = new StringBuilder();
            filterBufferBarcode
                    .append("(")
                    .append("barcode:'")
                    .append(barcode)
                    .append("'")
                    .append(" OR ")
                    .append(" barcodeList: '")
                    .append(barcode)
                    .append("'")
                    .append(")");
            // id store group
            filterBufferBarcode.append(" AND idStoreGroup:'").append(idStoreGroup).append("'");
            String filters = filterBufferBarcode.toString();
            String cacheKey = URLConnections.ALGOLIA_PRODUCTS + filters;

            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(cacheKey);
            if (jsonCachedOptional.isPresent()){
                return new Gson().fromJson(jsonCachedOptional.get(), ItemAlgolia.class);
            }

            List<IndexQuery> queries = new ArrayList<>();
            queries.add(new IndexQuery(URLConnections.ALGOLIA_PRODUCTS, new Query("").setFilters(filters)));
            List<SearchResult<?>> results = algoliaClient.multipleQueries(queries).getResults();
            ItemAlgolia barcodeItem = getItemAlgoliaFromMultiqueries(results.get(0));

            String jsonToCache = new Gson().toJson(barcodeItem);
            CachedDataManager.saveJsonInCache(cacheKey, jsonToCache);
            return barcodeItem;
        } catch (AlgoliaException e) {
            LOG.log(Level.WARNING, "getItemAlgoliaByBarcode() Error obteniendo item en algolia", e);
        }
        return null;
    }

    public static ItemAlgolia getItemAlgoliaByFiltersOptical(final OpticalItemFilter opticalItemFilter, long idStoreGroup) throws NotFoundException {
        Index<ItemAlgolia> index = algoliaClient.initIndex(URLConnections.ALGOLIA_PRODUCTS, ItemAlgolia.class);
        OpticalProductParameters opticalProduct = null;
        try {
            opticalProduct = getOpticalProductWithParameters(opticalItemFilter);
            LOG.info("OpticalProduct -> " + opticalProduct.toString());
        } catch (Exception e) {
            LOG.warning("No se encontro un Item con la combinacion de filtros" + opticalItemFilter.toString() + " ERROR -> " + e.getMessage());
            throw new NotFoundException("No se encontro un Item con la combinacion de filtros");
        }

        if (Objects.nonNull(opticalProduct)) {
            try {
                final String objectID = String.valueOf(opticalProduct.getId()) + idStoreGroup;
                Optional<ItemAlgolia> optionalItemAlgolia = index.getObject(objectID);
                return optionalItemAlgolia.orElseGet(ItemAlgolia::new);
            } catch (Exception e) {
                LOG.warning("Error consultando el item en Algolia " + e.getMessage());

            }
        }
        return null;
    }


    public static OpticalProductParameters getOpticalProductWithParameters(final OpticalItemFilter opticalItemFilter){
        try {
            LOG.info("opticalItemFilter => " + opticalItemFilter.toString());
            OpticalProductParameters opticalProducts = new OpticalProductParameters();
            Index<OpticalProductParameters> indexProducts = algoliaClient.initIndex(URLConnections.ALGOLIA_OPTICAL_PRODUCTS, OpticalProductParameters.class);
            StringBuilder filterBuffer = new StringBuilder();
            filterBuffer
                    .append("fatherId:'")
                    .append(opticalItemFilter.getMainItem())
                    .append("'");

            if(Objects.nonNull(opticalItemFilter.getPower())){
                filterBuffer.append(" AND power:'").append(opticalItemFilter.getPower()).append("'");
            }
            if(Objects.nonNull(opticalItemFilter.getAxle())){
                filterBuffer.append(" AND axle:'").append(opticalItemFilter.getAxle()).append("'");
            }
            if(Objects.nonNull(opticalItemFilter.getCylinder())){
                filterBuffer.append(" AND cylinder:'").append(opticalItemFilter.getCylinder()).append("'");
            }
            if(Objects.nonNull(opticalItemFilter.getAddition())){
                filterBuffer.append(" AND addition:'").append(opticalItemFilter.getAddition()).append("'");
            }
            if(Objects.nonNull(opticalItemFilter.getLensColor())){
                filterBuffer.append(" AND lensColor:'").append(opticalItemFilter.getLensColor()).append("'");
            }

            LOG.info("filterBuffer => " + filterBuffer);
            Query queryItemByParameters = new Query().setFilters(String.valueOf(filterBuffer));

            LOG.info("queryItemByParameters => " + queryItemByParameters.toString());
            SearchResult<OpticalProductParameters> opticalProductsSearchResult = indexProducts.search(queryItemByParameters);
            List<OpticalProductParameters> opticalProductsList = opticalProductsSearchResult.getHits();
            LOG.info("opticalProductsList => " + opticalProductsList.toString());
            if (!opticalProductsList.isEmpty()) {
                opticalProducts = opticalProductsList.get(0);
            }
            LOG.info("opticalProducts => " + opticalProducts.toString());
            return opticalProducts;
        }catch (AlgoliaException e){
            throw new RuntimeException(e);
        }
    }

    public static ItemAlgolia getItemAlgoliaByBarcodeSag(final String barcode, long idStoreGroup) {
        try {
            StringBuilder filterBufferBarcode = new StringBuilder();

            filterBufferBarcode
                    .append("(")
                    .append("barcode:'")
                    .append(barcode)
                    .append("'")
                    .append(" OR ")
                    .append(" barcodeList: '")
                    .append(barcode)
                    .append("'")
                    .append(")");

            filterBufferBarcode.append(" AND idStoreGroup:'").append(idStoreGroup).append("'");
            String filters = filterBufferBarcode.toString();
            String cacheKey = URLConnections.ALGOLIA_PRODUCTS_SCAN_AND_GO + filters;

            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(cacheKey);
            if (jsonCachedOptional.isPresent()) {
                return new Gson().fromJson(jsonCachedOptional.get(), ItemAlgolia.class);
            }

            List<IndexQuery> queries = new ArrayList<>();
            queries.add(new IndexQuery(URLConnections.ALGOLIA_PRODUCTS_SCAN_AND_GO, new Query("").setFilters(filters)));
            List<SearchResult<?>> results = algoliaClient.multipleQueries(queries).getResults();
            ItemAlgolia barcodeItem = getItemAlgoliaFromMultiqueries(results.get(0));

            String jsonToCache = new Gson().toJson(barcodeItem);
            CachedDataManager.saveJsonInCache(cacheKey, jsonToCache);
            return barcodeItem;
        } catch (AlgoliaException e) {
            LOG.log(Level.WARNING, "getItemAlgoliaByBarcodeSag() Error obteniendo item en algolia", e);
        }
        return null;
    }

    public static List<ItemAlgolia> getItemAlgoliaByBrand(final String marca, long idStoreGroup) {
        try {
            StringBuilder filterBufferBrand = new StringBuilder();
            filterBufferBrand
                    .append("(")
                    .append("marca:'")
                    .append(marca)
                    .append("'")
                    .append(")");

            filterBufferBrand.append(" AND idStoreGroup:'")
                    .append(idStoreGroup).append("'")
                    .append(" AND outofstore:false");
            String filters = filterBufferBrand.toString();
            String cacheKey = URLConnections.ALGOLIA_PRODUCTS + filters;

            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(cacheKey);
            if (jsonCachedOptional.isPresent()){
                Type listType = new TypeToken<List<ItemAlgolia>>(){}.getType();
                return new Gson().fromJson(jsonCachedOptional.get(), listType);
            }

            List<IndexQuery> queries = new ArrayList<>();
            queries.add(new IndexQuery(URLConnections.ALGOLIA_PRODUCTS, new Query("").setFilters(filters)));
            List<SearchResult<?>> results = algoliaClient.multipleQueries(queries).getResults();
            List<ItemAlgolia> items = getItemListAlgoliaFromMultiqueriesHits(results.get(0)).stream().limit(20).collect(Collectors.toList());

            String jsonToCache = new Gson().toJson(items);
            CachedDataManager.saveJsonInCache(cacheKey, jsonToCache);
            return items;
        } catch (AlgoliaException e) {
            LOG.log(Level.WARNING, "getItemAlgoliaByBrand() Error obteniendo item en algolia", e);
        }
        return null;
    }

    public static ItemAlgoliaStock getItemAlgoliaByIdStock(final Long idItem, Long idStoreGroup) {
        try {
            if (Objects.isNull(idStoreGroup)) idStoreGroup = 26L;
            StringBuilder filterBufferStock = new StringBuilder();

            filterBufferStock
                    .append("(")
                    .append("id:'")
                    .append(idItem)
                    .append("'")
                    .append(")");

            filterBufferStock.append(" AND idStoreGroup:'").append(idStoreGroup).append("'");
            String filters = filterBufferStock.toString();
            String cacheKey = URLConnections.ALGOLIA_PRODUCTS + filters;

            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(cacheKey);
            if (jsonCachedOptional.isPresent()){
                return new Gson().fromJson(jsonCachedOptional.get(), ItemAlgoliaStock.class);
            }

            List<IndexQuery> queries = new ArrayList<>();
            queries.add(new IndexQuery(URLConnections.ALGOLIA_PRODUCTS, new Query("").setFilters(filters)));
            List<SearchResult<?>> results = algoliaClient.multipleQueries(queries).getResults();
            ItemAlgoliaStock item = getItemAlgoliaFromMultiqueriesStock(results.get(0));

            String jsonToCache = new Gson().toJson(item);
            CachedDataManager.saveJsonInCache(cacheKey, jsonToCache);
            return item;
        } catch (AlgoliaException e) {
            LOG.log(Level.WARNING, "getItemAlgoliaByIdStock() Error obteniendo item en algolia", e);
        }
        return null;
    }

    public static ItemAlgolia getItemAlgoliaById(final Long idItem, Long idStoreGroup) {
        try {
            if (Objects.isNull(idStoreGroup)) idStoreGroup = 26L;
            //Reemplazar codigo innecesario que usa search por un getObject
            ItemAlgolia item = getItemAlgolia("" + idItem + idStoreGroup);
            return Objects.nonNull(item) && Objects.nonNull(item.getId()) ? item : null;
        } catch (Exception e) {
            LOG.warning("Error obteniendo item en algolia usando el id {} " + e.getMessage());
        }
        return null;
    }



    @Nullable
    private static ItemAlgolia getItemAlgolia(SearchResult<ItemAlgolia> search) {
        if (search.getHits() != null && !search.getHits().isEmpty() && search.getHits().get(0)!= null && search.getHits().get(0).isValid()){
            //LOG.info("media image url ->" +search.getHits().get(0).getMediaImageUrl());
            return search.getHits().get(0);
        }
        return null;
    }


    // CACHED
    public static HomeConfigAlgolia getHomeConfig(){
        try{
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_HOME_CONFIG);

            if (jsonCachedOptional.isPresent()){
                return new Gson().fromJson(jsonCachedOptional.get(),HomeConfigAlgolia.class);
            }

            Index<HomeConfigAlgolia> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES,HomeConfigAlgolia.class);
            Optional<HomeConfigAlgolia> optionalHomeConfigAlgolia = index.getObject(URLConnections.ALGOLIA_HOME_CONFIG);

            if (optionalHomeConfigAlgolia.isPresent()){
                String jsonToCache = new Gson().toJson(optionalHomeConfigAlgolia.get());
                CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_HOME_CONFIG, jsonToCache);
                return optionalHomeConfigAlgolia.orElseGet(HomeConfigAlgolia::new);
            }

        }catch (Exception e) {
            LOG.warning("Error in APIAlgolia.getHomeConfig. Message:"+ Arrays.toString(e.getStackTrace()));
            e.getStackTrace();
        }
        return new HomeConfigAlgolia();
    }

    // CACHED
    public static HomeConfigAlgolia getHomeV2Config(){
        String homeBackup = "{\"homeConfig\":{\"headerComponents\":[{\"componentType\":\"SEARCH_FIELD\",\"enableFor\":[\"WEB\",\"IOS\",\"ANDROID\",\"RESPONSIVE\"],\"label\":\"Busca aquí tu producto\",\"position\":1,\"redirectUrl\":\"https://www.farmatodo.com.co/buscar\",\"active\":true,\"list\":[],\"listMobile\":[]},{\"componentType\":\"BANNER_LOGIN\",\"enableFor\":[\"WEB\"],\"position\":2,\"active\":true,\"list\":[],\"listMobile\":[]},{\"componentType\":\"MIN_BANNER_LEFT\",\"enableFor\":[\"WEB\"],\"position\":2,\"active\":true,\"list\":[],\"listMobile\":[]},{\"componentType\":\"BANNER\",\"enableFor\":[\"WEB\"],\"position\":2,\"active\":true,\"list\":[],\"listMobile\":[]},{\"componentType\":\"SQUARE_TWO_BANNER\",\"enableFor\":[\"IOS\",\"ANDROID\",\"RESPONSIVE\"],\"position\":2,\"active\":true,\"list\":[],\"listMobile\":[]},{\"componentType\":\"THIN_BANNER\",\"enableFor\":[\"WEB\",\"IOS\",\"ANDROID\",\"RESPONSIVE\"],\"position\":3,\"urlBanner\":\"https://lh3.googleusercontent.com/3jxmNIoFeJyzEfvULm7j6RB8NP1z56rikr2Sxoinl0CiZaBRp67oSgWd9nBaFpLc-kGkuZl1F7GbcVBXXnowz8QtVF0nSNpRUW0ANV4lr1vpjcCB\",\"redirectUrl\":\"https://www.farmatodo.com.co/prime\",\"active\":true,\"userType\":\"USER_PRIME\",\"list\":[],\"listMobile\":[]},{\"componentType\":\"THIN_BANNER\",\"enableFor\":[\"WEB\",\"IOS\",\"ANDROID\",\"RESPONSIVE\"],\"position\":3,\"urlBanner\":\"https://lh3.googleusercontent.com/TEj2T8Z8DXaaUJIs9l_MYgdZ7L5I2NXtfzI9D7DXbew3obbAGtzSpysPUNToBG-2fb4OrwzXsV0fLKz8Otp57MbQD1oPXkrVYyuk_MgWj5EZq06Y\",\"redirectUrl\":\"https://www.farmatodo.com.co/marcas/pr?utm_source\\u003dbraze\\u0026utm_medium\\u003dbanerslide\\u0026utm_campaign\\u003dpr\\u0026utm_content\\u003dprrafase\\u0026utm_term\\u003dgeneral30may\",\"active\":true,\"userType\":\"USER_NOT_PRIME\",\"list\":[],\"listMobile\":[]},{\"componentType\":\"TRACKING_INFO\",\"enableFor\":[\"IOS\",\"ANDROID\",\"RESPONSIVE\"],\"position\":4,\"urlTracking\":\"https://www.farmatodo.com.co/detalle-orden/\",\"redirectUrl\":\"https://www.farmatodo.com.co/ordenes/\",\"active\":true,\"list\":[],\"listMobile\":[]},{\"componentType\":\"HTML_LABEL\",\"enableFor\":[\"WEB\",\"IOS\",\"ANDROID\",\"RESPONSIVE\"],\"dataFrom\":{\"from\":\"USER_NAME\"},\"label\":\"\\u003cfont color\\u003d\\u0027#418fde\\u0027\\u003e¡Hola {user_name}! \\u003c/font\\u003e Estas son las mejores ofertas\",\"labelWeb\":\"\\u003cfont color\\u003d\\u0027#418fde\\u0027\\u003e¡Hola {user_name}! \\u003c/font\\u003e Estas son las mejores ofertas\",\"position\":5,\"active\":true,\"list\":[],\"listMobile\":[]},{\"componentType\":\"ITEM_LIST\",\"enableFor\":[],\"dataFrom\":{\"from\":\"SUGGESTS\"},\"position\":7,\"active\":false,\"list\":[],\"listMobile\":[]},{\"componentType\":\"BRAZE_CAROUSEL\",\"enableFor\":[\"ANDROID\",\"WEB\",\"RESPONSIVE\",\"IOS\"],\"dataFrom\":{\"from\":\"SUGGESTS\"},\"position\":8,\"active\":true,\"list\":[],\"listMobile\":[]},{\"componentType\":\"BANNER_LEFT_ADVERTISING\",\"enableFor\":[\"WEB\",\"RESPONSIVE\"],\"position\":9,\"active\":true,\"list\":[],\"listMobile\":[]},{\"componentType\":\"BANNER_RIGHT_ADVERTISING\",\"enableFor\":[\"WEB\",\"RESPONSIVE\"],\"position\":10,\"active\":true,\"list\":[],\"listMobile\":[]}],\"bodyComponents\":[{\"componentType\":\"THIN_BANNER\",\"enableFor\":[\"WEB\"],\"position\":1,\"urlBanner\":\"https://lh3.googleusercontent.com/EBR0D-3Zc7_GxHOb1QzA0oIB3Vb9PHfJIJu9Jyzto2TN82tqji2HDD4x1JuQfxPYIOkcT--saOzFTpjBlbpS1xwj_NOCjzfZwXc\",\"redirectUrl\":\"https://www.farmatodo.com.co/destacados/21643\",\"active\":true,\"list\":[],\"listMobile\":[]},{\"componentType\":\"THIN_BANNER\",\"enableFor\":[\"RESPONSIVE\",\"IOS\",\"ANDROID\"],\"position\":2,\"urlBanner\":\"https://lh3.googleusercontent.com/AgRAoUshQ6vKBLCECPa8MAqSljhyKtxY-fyoHO3AqQFBEP9fVvCJxN3VftKfYweUxLxS8M4LIfpvJ2kF3UBtggOsiPmdAiFsRJ0t6GGoPSylf2Rn\",\"redirectUrl\":\"https://www.farmatodo.com.co/tiendas\",\"active\":true,\"list\":[],\"listMobile\":[]},{\"componentType\":\"PRIME_SAVINGS\",\"enableFor\":[\"IOS\",\"ANDROID\",\"RESPONSIVE\"],\"position\":3,\"redirectUrl\":\"https://www.farmatodo.com.co/marcas/pr?utm_source\\u003dbraze\\u0026utm_medium\\u003dbanerslide\\u0026utm_campaign\\u003dpr\\u0026utm_content\\u003dprrafase\\u0026utm_term\\u003dgeneral30may\",\"active\":true,\"userType\":\"USER_NOT_PRIME\",\"list\":[],\"listMobile\":[]},{\"componentType\":\"HTML_LABEL\",\"enableFor\":[\"WEB\",\"ANDROID\",\"RESPONSIVE\",\"IOS\"],\"label\":\"Ofertas Flash\",\"labelWeb\":\"\\u003cspan style\\u003d\\u0027border-bottom: 0.5rem solid #FDD756; position: absolute;\\u0027\\u003eOfertas Flash\\u003c/span\\u003e\",\"position\":3,\"active\":true,\"list\":[],\"listMobile\":[]},{\"componentType\":\"FLASH_OFFERS\",\"enableFor\":[\"WEB\",\"ANDROID\",\"RESPONSIVE\",\"IOS\"],\"dataFrom\":{\"from\":\"FLASH_OFFERS_ITEMS\"},\"position\":4,\"active\":true,\"list\":[],\"listMobile\":[]},{\"componentType\":\"HTML_LABEL\",\"enableFor\":[\"WEB\",\"IOS\",\"ANDROID\",\"RESPONSIVE\"],\"dataFrom\":{\"from\":\"FAVORITES\"},\"label\":\"Estos son tus favoritos\",\"labelWeb\":\"\\u003cspan style\\u003d\\u0027border-bottom: 0.5rem solid #418FDE; position: absolute;\\u0027\\u003eEstos son tus favoritos\\u003c/span\\u003e\",\"position\":5,\"active\":true,\"list\":[],\"listMobile\":[]},{\"componentType\":\"ITEM_LIST\",\"enableFor\":[\"WEB\",\"IOS\",\"ANDROID\",\"RESPONSIVE\"],\"dataFrom\":{\"from\":\"FAVORITES\"},\"position\":6,\"active\":true,\"list\":[],\"listMobile\":[]},{\"componentType\":\"HTML_LABEL\",\"enableFor\":[\"WEB\",\"IOS\",\"ANDROID\",\"RESPONSIVE\"],\"dataFrom\":{\"from\":\"PREVIOUS_ITEMS\"},\"label\":\"Últimas compras\",\"labelWeb\":\"\\u003cspan style\\u003d\\u0027border-bottom: 0.5rem solid #418FDE; position: absolute;\\u0027\\u003eÚltimas compras\\u003c/span\\u003e\",\"position\":7,\"active\":true,\"list\":[],\"listMobile\":[]},{\"componentType\":\"SMALL_ITEM_LIST\",\"enableFor\":[\"WEB\",\"IOS\",\"ANDROID\",\"RESPONSIVE\"],\"dataFrom\":{\"from\":\"PREVIOUS_ITEMS\"},\"position\":8,\"active\":true,\"list\":[],\"listMobile\":[]},{\"componentType\":\"HTML_LABEL\",\"enableFor\":[\"IOS\",\"ANDROID\",\"RESPONSIVE\"],\"label\":\"\\u003cfont color\\u003d\\u0027black\\u0027\\u003eNo te lo puedes perder\\u003c/font\\u003e\",\"labelWeb\":\"\\u003cfont color\\u003d\\u0027black\\u0027\\u003eNo te lo puedes perder\\u003c/font\\u003e\",\"position\":9,\"active\":true,\"list\":[],\"listMobile\":[]},{\"componentType\":\"SQUARE_BANNER\",\"enableFor\":[\"IOS\",\"ANDROID\",\"RESPONSIVE\"],\"dataFrom\":{\"listData\":[{\"position\":0,\"urlBanner\":\"https://lh3.googleusercontent.com/IK2hg2cLs9k8n0iprN8gyj0sKa-FRa-iWGo1FI627lgx-opVDWo2UL9rV319FMJgZtnqufOMQdbm17Y1dPN98gQOxnp-KSTuqdrZzrC3h3J1Hl5H\",\"redirectUrl\":\"https://www.farmatodo.com.co/marcas/pr?utm_source\\u003dbraze\\u0026utm_medium\\u003dbannersecundario\\u0026utm_campaign\\u003dpr\\u0026utm_content\\u003dprrafase\\u0026utm_term\\u003dgeneral30may\",\"active\":false,\"list\":[],\"listMobile\":[]},{\"position\":0,\"urlBanner\":\"https://lh3.googleusercontent.com/OJydJ0nF--fAI3dh_5Q007kiwdc0SxQihT4AyV5d8MXEkjvDyy5LnhDCmVfYip9a_Zpx-IVCZvB6ZhMxhNqWC1nAd_4ciVneAkh0uyQKFFrl6u8\",\"redirectUrl\":\"https://www.farmatodo.com.co/marcas/pr?utm_source\\u003dbraze\\u0026utm_medium\\u003dbannersecundario\\u0026utm_campaign\\u003dpr\\u0026utm_content\\u003dprrafase\\u0026utm_term\\u003dgeneral30may\",\"active\":false,\"list\":[],\"listMobile\":[]},{\"position\":0,\"urlBanner\":\"https://lh3.googleusercontent.com/m7zm_3TQdEDBdRcrJJl9bKXNOnTvxsHxoheKYj9ecFMWZsSvLjHmBIBo9j-zpm6FF0oociOOhmWkFH17sUhQv2jy91zfgcUyQsU9guic6U9vcIw8\",\"redirectUrl\":\"https://www.farmatodo.com.co/destacados/43814?utm_source\\u003dbraze\\u0026utm_medium\\u003dbanner_secundario\\u0026utm_campaign\\u003dsupplier\\u0026utm_content\\u003dfemsa\\u0026utm_term\\u003dmanantial30jun\",\"active\":false,\"list\":[],\"listMobile\":[]},{\"position\":0,\"urlBanner\":\"https://lh3.googleusercontent.com/IYWAygIhRJPLpJWN1BrfBJ1yz0DGUQ5GwNviDFsGyNkPxxzVH0-dmN95IjMfaU5BlNJNpIkJLlrqv1rTObcYMpB4M_rmZK8BqvZnbnEo3EuZ-on0\",\"redirectUrl\":\"https://www.farmatodo.com.co/destacados/43860?utm_source\\u003dbraze\\u0026utm_medium\\u003dbanner_secundario\\u0026utm_campaign\\u003dsupplier\\u0026utm_content\\u003dloreal\\u0026utm_term\\u003dvichyshampo07jul\",\"active\":false,\"list\":[],\"listMobile\":[]}]},\"position\":9,\"active\":true,\"list\":[],\"listMobile\":[]},{\"componentType\":\"HTML_LABEL\",\"enableFor\":[\"WEB\",\"IOS\",\"ANDROID\",\"RESPONSIVE\"],\"dataFrom\":{\"from\":\"RECENTLY_VIEWED\"},\"label\":\"Últimos vistos\",\"labelWeb\":\"\\u003cspan style\\u003d\\u0027border-bottom: 0.5rem solid #418FDE; position: absolute;\\u0027\\u003eÚltimos vistos\\u003c/span\\u003e\",\"position\":11,\"active\":true,\"list\":[],\"listMobile\":[]},{\"componentType\":\"SMALL_ITEM_LIST\",\"enableFor\":[\"WEB\",\"IOS\",\"ANDROID\",\"RESPONSIVE\"],\"dataFrom\":{\"from\":\"RECENTLY_VIEWED\"},\"position\":12,\"active\":true,\"list\":[],\"listMobile\":[]},{\"componentType\":\"HTML_LABEL\",\"enableFor\":[\"IOS\",\"ANDROID\",\"RESPONSIVE\",\"WEB\"],\"label\":\"\",\"position\":13,\"active\":false,\"list\":[],\"listMobile\":[]}],\"footerComponents\":[{\"componentType\":\"HTML_LABEL\",\"enableFor\":[\"WEB\",\"IOS\",\"ANDROID\",\"RESPONSIVE\"],\"label\":\"Destacados\",\"labelWeb\":\"\\u003cspan style\\u003d\\u0027border-bottom: 0.5rem solid #418FDE; position: absolute;\\u0027\\u003eDestacados\\u003c/span\\u003e\",\"position\":1,\"active\":true,\"list\":[],\"listMobile\":[]},{\"componentType\":\"GRID_ITEM_LIST\",\"enableFor\":[\"WEB\",\"IOS\",\"ANDROID\",\"RESPONSIVE\"],\"dataFrom\":{\"from\":\"HIGHLIGHTS\"},\"position\":2,\"active\":true,\"list\":[],\"listMobile\":[]},{\"componentType\":\"HTML_LABEL\",\"enableFor\":[\"IOS\",\"ANDROID\",\"RESPONSIVE\"],\"label\":\"Categorías\",\"labelWeb\":\"\\u003cspan style\\u003d\\u0027border-bottom: 0.5rem solid #418FDE; position: absolute;\\u0027\\u003eCategorías\\u003c/span\\u003e\",\"position\":3,\"active\":true,\"list\":[],\"listMobile\":[]},{\"componentType\":\"CIRCLE_BANNER\",\"enableFor\":[\"IOS\",\"ANDROID\",\"RESPONSIVE\"],\"dataFrom\":{\"listData\":[{\"label\":\"Salud y medicamentos\",\"position\":0,\"urlBanner\":\"https://lh3.googleusercontent.com/ctwg3EfzH36JRGjkcRGeW3pwEeNJPw3rx2JTcFD4DhE4Qzg54hzP-kY1Pm7X2KJMTGgM_9lY6Kw5YJriMZ3psnlJ1amQN6hSpEM\",\"redirectUrl\":\"https://www.farmatodo.com.co/categorias/salud-y-medicamentos\",\"active\":false,\"list\":[],\"listMobile\":[]},{\"label\":\"Belleza\",\"position\":0,\"urlBanner\":\"https://lh3.googleusercontent.com/lbnbTVzQMVWRH4kxHF-RMyivHUQBQsGtmlVEF9Ot9l9ASbLdggqo4Je5yC2RMIvCeps38NSbQdEpJ2h__m-c6RJN-q-4o4KAg-o\",\"redirectUrl\":\"https://www.farmatodo.com.co/categorias/belleza\",\"active\":false,\"list\":[],\"listMobile\":[]},{\"label\":\"Cuidado del bebé\",\"position\":0,\"urlBanner\":\"https://lh3.googleusercontent.com/JYQLSFDes876fO2Y16dGXWeWB8VVEz8mXwp8OvmBbQQXNUIWuxrs-_MgFQc7DzrEWGP0fEb35Zl5zPfljxu4Gr-HW-qsiuMfQn0\",\"redirectUrl\":\"https://www.farmatodo.com.co/categorias/cuidado-del-bebe\",\"active\":false,\"list\":[],\"listMobile\":[]},{\"label\":\"Cuidado personal\",\"position\":0,\"urlBanner\":\"https://lh3.googleusercontent.com/v_1dt5eaTobIqVN4q5nnI6FkNvGB98-79snR_UDGQOQKYt1Blgq3h3lmNSAz28eBWIB2iPxEWv2Oy8ZQYql420rroqGglvesLA\",\"redirectUrl\":\"https://www.farmatodo.com.co/categorias/cuidado-personal\",\"active\":false,\"list\":[],\"listMobile\":[]},{\"label\":\"Alimentos y bebidas\",\"position\":0,\"urlBanner\":\"https://lh3.googleusercontent.com/_Mcid8oaLrlW7XZJxf4ABYih5WoWwWdQit3_uuJhhgRM5JmvoZ-GYV0B2ikUVJ8y6inphZ-qihAQks0WWbRxu5qVnhlqPRqxNw\",\"redirectUrl\":\"https://www.farmatodo.com.co/categorias/alimentos-y-bebidas\",\"active\":false,\"list\":[],\"listMobile\":[]},{\"label\":\"Hogar, mascotas y otros\",\"position\":0,\"urlBanner\":\"https://lh3.googleusercontent.com/RNtm4F_x6iFiULDN_Cbd1ufxNPBNQLUvKqPKo9v8YZYozdo3MO47KGW_kyBBVhPSVsdEu8tic7qf0Av7r0ESLmNJQLxB7K_gWw\",\"redirectUrl\":\"https://www.farmatodo.com.co/categorias/hogar-mascotas-y-otros\",\"active\":false,\"list\":[],\"listMobile\":[]}]},\"position\":4,\"active\":true,\"list\":[],\"listMobile\":[]},{\"componentType\":\"THIN_BANNER\",\"enableFor\":[\"WEB\",\"IOS\",\"ANDROID\",\"RESPONSIVE\"],\"position\":5,\"urlBanner\":\"https://lh3.googleusercontent.com/dGUPyiJgYRXHI45f--q78fcHKgnxHYeNVfIaY8_4K9ER-hpalmpjlq2Dl0CK6yAoWGlLd5o_fHLJqAoqeGOqQH5RAOOaCPbRnMtKiuCBK2YmdV2g\",\"redirectUrl\":\"https://www.farmatodo.com.co/destacados/21643\",\"active\":true,\"list\":[],\"listMobile\":[]},{\"componentType\":\"HTML_LABEL\",\"enableFor\":[\"WEB\",\"IOS\",\"ANDROID\",\"RESPONSIVE\"],\"label\":\"Belleza\",\"labelWeb\":\"\\u003cspan style\\u003d\\u0027border-bottom: 0.5rem solid #B14497; position: absolute;\\u0027\\u003eBelleza\\u003c/span\\u003e\",\"position\":6,\"redirectUrl\":\"https://www.farmatodo.com.co/categorias/belleza\",\"active\":true,\"list\":[],\"listMobile\":[]},{\"componentType\":\"RECT_BANNER\",\"enableFor\":[\"WEB\",\"IOS\",\"ANDROID\",\"RESPONSIVE\"],\"position\":7,\"active\":true,\"list\":[{\"urlBanner\":\"https://lh3.googleusercontent.com/S1ORLp3dKzzElQvkKXFVr40CsI3rD2MS9fPN9ErjwW9sScpzB7gGgUPpdfSZwqB_qQWcB_dn7k3kzBsDqSfgYZnO6TpfBiEkgGp7dcEU_v9lv3d3WQ\",\"redirectUrl\":\"https://www.farmatodo.com.co/categorias/belleza/cosmeticos/ojos\",\"label\":\"Ojos\"},{\"urlBanner\":\"https://lh3.googleusercontent.com/zr_lFEnO0GW4B6bJI3Y1uu5dqkP3yi12UE6Xaso_fPz82fuGy9UVwu923uj9eWQ3rxpoqHsJrmDaUGzbvgW6yg6GpbmdYszrQAEeD-Qw7AuYyc4m\",\"redirectUrl\":\"https://www.farmatodo.com.co/categorias/belleza/cosmeticos/labios\",\"label\":\"Labial\"},{\"urlBanner\":\"https://lh3.googleusercontent.com/pd7P07fRYZ4DqxlDVdUaxyDtLUW57dJUWcSxIvkaDjon8MsOvAWtuXML-A416IE4zEkIJ6Q4OyXanBPZ-513U8dwcuer-hx8ubTcUCZNDamD0O_S\",\"redirectUrl\":\"https://www.farmatodo.com.co/categorias/belleza/cosmeticos/bases-y-polvos\",\"label\":\"Bases\"},{\"urlBanner\":\"https://lh3.googleusercontent.com/IohHBZJDcRBgdms5zhnI3HBgAsStS0T_pRSTQ9ia_C1tAHkOt1gmikXaQ86mxmszqDs6u2GV_LCD3JEbHD-LtKbkwF64LQFKOepEar4yMhKeq-LAag\",\"redirectUrl\":\"https://www.farmatodo.com.co/categorias/belleza/cosmeticos/unas\",\"label\":\"Uñas\"},{\"urlBanner\":\"https://lh3.googleusercontent.com/1ASBnUerA9pDp8HBX1dyZpdAkXMjUdRoPDD04Y5vrtq9C5RMwlzbgTkhQLuRIk4PKTYLfrtw4EznLwUtw5rGr3MPPdKQg69RzG8vPjxp8IX5UWDnwg\",\"redirectUrl\":\"https://www.farmatodo.com.co/categorias/belleza/cosmeticos/lociones-y-siliconas\",\"label\":\"Lociones\"}],\"listMobile\":[{\"urlBanner\":\"https://lh3.googleusercontent.com/1XbqZ8ygET-MOjLJG2gyXrI7Etxuev3A7ifBy2PI5bfZtHrfUhIsuNvZQNETy26L3ALRXoZXZXewUQ9bOH8kNWJnEg1oSzxkcpL_XY_bfY8QQnU\",\"redirectUrl\":\"https://www.farmatodo.com.co/categorias/belleza/cosmeticos/ojos\",\"label\":\"Ojos\"},{\"urlBanner\":\"https://lh3.googleusercontent.com/h5oYBo_u9MDLyBiB8YkVVDvlbiN4Jsbz1RLTPDtu56UPfEydOm2mpdTaGQRe3_EBVDD_rCvBMJdkMrGq-saU6zhgQpiK1jC5nwC_LYDQn6bDJwJjVQ\",\"redirectUrl\":\"https://www.farmatodo.com.co/categorias/belleza/cosmeticos/labios\",\"label\":\"Labial\"},{\"urlBanner\":\"https://lh3.googleusercontent.com/8ycmvKEsxdls8HMXRCM0xo4Ju0kFMDKUGtSKUkDGqdwjMOPaX0vBvMHYCPWykmE5RmiNqKKcETZT8hzdNhRA_Jg4eGmx6YD92-4acTzIW5qU8ZAB\",\"redirectUrl\":\"https://www.farmatodo.com.co/categorias/belleza/cosmeticos/bases-y-polvos\",\"label\":\"Bases\"},{\"urlBanner\":\"https://lh3.googleusercontent.com/m_hy8nZFxj70wYqAq51-bGsdC8IeTk72rpbMYvYg3IK11_NStheKRIZ6kpYL0O14kZFMURTUtj0ka7WdXZFNg-h8EIeEv3asSa8tqbLuo-M0VJbm\",\"redirectUrl\":\"https://www.farmatodo.com.co/categorias/belleza/cosmeticos/unas\",\"label\":\"Uñas\"},{\"urlBanner\":\"https://lh3.googleusercontent.com/c812ukl5MvFyfKq4XUb4-nw-9ja6CFxFl4j9eKsAXgoFM4hgy8u30VRpDct_fEA7b2_nQHiGN1a6WXyTjQFJJOVxCgPXmZ4ZmZJaEyDDPL1jtOR29Q\",\"redirectUrl\":\"https://www.farmatodo.com.co/categorias/belleza/cosmeticos/lociones-y-siliconas\",\"label\":\"Lociones\"}]},{\"componentType\":\"ITEM_LIST\",\"enableFor\":[\"WEB\",\"IOS\",\"ANDROID\",\"RESPONSIVE\"],\"dataFrom\":{\"id\":\"72\",\"from\":\"MOST_SALES\"},\"position\":8,\"active\":true,\"list\":[],\"listMobile\":[]},{\"componentType\":\"SHORTCUT_BANNER\",\"enableFor\":[\"WEB\",\"IOS\",\"ANDROID\",\"RESPONSIVE\"],\"position\":9,\"active\":true,\"list\":[{\"urlBanner\":\"https://lh3.googleusercontent.com/4PojBjlcUk9fOxr62R-RqmSWczQunXzbJLw4jDL3ZwXFUqf76R-xsvsW6IL8cfDhxGkHTbeBzj64nmuT0gbOBVy2rMJrO9UnrHkZKzZlSqu3RY9m\",\"redirectUrl\":\"https://www.farmatodo.com.co/marcas/maybelline\",\"label\":\"Maybelline\"},{\"urlBanner\":\"https://lh3.googleusercontent.com/8BAiKlAk3kFu9XB-W1OkaGvPdhxBeKXcu1FJC62roigRAp0wuTcx7uq9CzkcBwkay2Y7vrNjqkDc5uqrKUiO52CovF0yEEGDaf1ufCDDp4wjN8rj7g\",\"redirectUrl\":\"https://www.farmatodo.com.co/marcas/vogue\",\"label\":\"Vogue\"},{\"urlBanner\":\"https://lh3.googleusercontent.com/EAg-qMQm2Ir5oRLNrwi85ettFgkoxIqqybC9QWsOZNqwI8M2zdQKHCaGem7kouwulQCgb6knBs7TxhRYnH3DmLnA71EBvyoJDTBQ66D6TFjn64PV\",\"redirectUrl\":\"https://www.farmatodo.com.co/marcas/luminous\",\"label\":\"Luminous\"},{\"urlBanner\":\"https://lh3.googleusercontent.com/JecHkonscRjATNeJGG8Cfh8gmcQN55v4MOZeBqmQkOTULHFgElLxSmMmPZwnQHb42Ieyoq7aTROY7n4L9xiAmxtFTW0dWkTtrXxgNWFxiZfdyfBI\",\"redirectUrl\":\"https://www.farmatodo.com.co/marcas/revitalift\",\"label\":\"Loreal\"},{\"urlBanner\":\"https://lh3.googleusercontent.com/y54L1xxZdJSie1GeMBuwhAM9jwxY9bHD_5PLFBUHd7m3sMmbKVHNZWiPGPrt-Qf9YBK3thVg9tKGV0XtJcMu39IeZKIbP1Ju6tpUD6YmnsaK7mg\",\"redirectUrl\":\"https://www.farmatodo.com.co/marcas/neutrogena\",\"label\":\"Neutrogena\"},{\"urlBanner\":\"https://lh3.googleusercontent.com/1IMysNfdvn3e5C3AUtL8iqSyAn6U147JutZJtVlpi4xJ3Jt12Dllz1FK-sumicJkBKsar1kQY6FiDifpW568ZxfmSDvh3F49QH7v6YNI_D5TJTyS\",\"redirectUrl\":\"https://www.farmatodo.com.co/marcas/schwarzkopf\",\"label\":\"Schwarzkopf\"}],\"listMobile\":[{\"urlBanner\":\"https://lh3.googleusercontent.com/4PojBjlcUk9fOxr62R-RqmSWczQunXzbJLw4jDL3ZwXFUqf76R-xsvsW6IL8cfDhxGkHTbeBzj64nmuT0gbOBVy2rMJrO9UnrHkZKzZlSqu3RY9m\",\"redirectUrl\":\"https://www.farmatodo.com.co/marcas/maybelline\",\"label\":\"Maybelline\"},{\"urlBanner\":\"https://lh3.googleusercontent.com/8BAiKlAk3kFu9XB-W1OkaGvPdhxBeKXcu1FJC62roigRAp0wuTcx7uq9CzkcBwkay2Y7vrNjqkDc5uqrKUiO52CovF0yEEGDaf1ufCDDp4wjN8rj7g\",\"redirectUrl\":\"https://www.farmatodo.com.co/marcas/vogue\",\"label\":\"Vogue\"},{\"urlBanner\":\"https://lh3.googleusercontent.com/EAg-qMQm2Ir5oRLNrwi85ettFgkoxIqqybC9QWsOZNqwI8M2zdQKHCaGem7kouwulQCgb6knBs7TxhRYnH3DmLnA71EBvyoJDTBQ66D6TFjn64PV\",\"redirectUrl\":\"https://www.farmatodo.com.co/marcas/luminous\",\"label\":\"Luminous\"},{\"urlBanner\":\"https://lh3.googleusercontent.com/JecHkonscRjATNeJGG8Cfh8gmcQN55v4MOZeBqmQkOTULHFgElLxSmMmPZwnQHb42Ieyoq7aTROY7n4L9xiAmxtFTW0dWkTtrXxgNWFxiZfdyfBI\",\"redirectUrl\":\"https://www.farmatodo.com.co/marcas/revitalift\",\"label\":\"Loreal\"},{\"urlBanner\":\"https://lh3.googleusercontent.com/y54L1xxZdJSie1GeMBuwhAM9jwxY9bHD_5PLFBUHd7m3sMmbKVHNZWiPGPrt-Qf9YBK3thVg9tKGV0XtJcMu39IeZKIbP1Ju6tpUD6YmnsaK7mg\",\"redirectUrl\":\"https://www.farmatodo.com.co/marcas/neutrogena\",\"label\":\"Neutrogena\"},{\"urlBanner\":\"https://lh3.googleusercontent.com/1IMysNfdvn3e5C3AUtL8iqSyAn6U147JutZJtVlpi4xJ3Jt12Dllz1FK-sumicJkBKsar1kQY6FiDifpW568ZxfmSDvh3F49QH7v6YNI_D5TJTyS\",\"redirectUrl\":\"https://www.farmatodo.com.co/marcas/schwarzkopf\",\"label\":\"Schwarzkopf\"}]},{\"componentType\":\"STATIC_BANNER\",\"enableFor\":[\"WEB\",\"RESPONSIVE\"],\"position\":10,\"active\":true,\"list\":[],\"listMobile\":[]},{\"componentType\":\"HTML_LABEL\",\"enableFor\":[\"WEB\",\"IOS\",\"ANDROID\",\"RESPONSIVE\"],\"label\":\"Salud y medicamentos\",\"labelWeb\":\"\\u003cspan style\\u003d\\u0027border-bottom: 0.5rem solid #84D24C; position: absolute;\\u0027\\u003eSalud y medicamentos\\u003c/span\\u003e\",\"position\":11,\"redirectUrl\":\"https://www.farmatodo.com.co/categorias/salud-y-medicamentos\",\"active\":true,\"list\":[],\"listMobile\":[]},{\"componentType\":\"ITEM_LIST\",\"enableFor\":[\"WEB\",\"IOS\",\"ANDROID\",\"RESPONSIVE\"],\"dataFrom\":{\"id\":\"1\",\"from\":\"MOST_SALES\"},\"position\":12,\"active\":true,\"list\":[],\"listMobile\":[]},{\"componentType\":\"HTML_LABEL\",\"enableFor\":[\"IOS\",\"ANDROID\",\"RESPONSIVE\",\"WEB\"],\"label\":\"Tiendas oficiales\",\"labelWeb\":\"\\u003cspan style\\u003d\\u0027border-bottom: 0.5rem solid #FDD756; position: absolute;\\u0027\\u003eTiendas oficiales\\u003c/span\\u003e\",\"position\":13,\"active\":true,\"list\":[],\"listMobile\":[]},{\"componentType\":\"CIRCLE_BANNER\",\"enableFor\":[\"IOS\",\"ANDROID\",\"RESPONSIVE\",\"WEB\"],\"dataFrom\":{\"listData\":[{\"label\":\"Marca Farmatodo\",\"position\":0,\"urlBanner\":\"https://lh3.googleusercontent.com/WNN1Vy_355mgP1gG2_aXfy2g8rmh9P5kPtxx6rDojbAEpndLmlUlQWoRIJdjXIj222EkzXqt75Cg8O846Y5G39IVu8EfcQmV_hdF4vHf-62MBvE\",\"redirectUrl\":\"https://www.farmatodo.com.co/marcas/farmatodo\",\"active\":false,\"list\":[],\"listMobile\":[]},{\"label\":\"UV Defender\",\"position\":0,\"urlBanner\":\"https://lh3.googleusercontent.com/FHkjixMs5tEkKeOqAKr-GKFdCObqAcbCTb7xtEKk31ZZcqjGWBLr_aT4PXcqaDASuxUPQSFr-FD84nPM4GKWDKpDuSRrbj3HoiXTwUb6p6rysWI\",\"redirectUrl\":\"https://www.farmatodo.com.co/marcas/protectoruvfluido\",\"active\":false,\"list\":[],\"listMobile\":[]},{\"label\":\"Bayer\",\"position\":0,\"urlBanner\":\"https://lh3.googleusercontent.com/ehcqhV7Iu6tYV6ZqaHWhOjmFeUS9KA3y-5dghlza1xLnEtuuzvYZ7-9KiYisSDDjVmPc9ErMExCFLG6yy2vOroh4Pxim7OYyEYS6LMfiHqyRCLGb\",\"redirectUrl\":\"https://www.farmatodo.com.co/marcas/bayer\",\"active\":false,\"list\":[],\"listMobile\":[]},{\"label\":\"Loreal\",\"position\":0,\"urlBanner\":\"https://lh3.googleusercontent.com/peXyww0Dyjo6693lugllnWfEyxHIiNivEP739eRU48aEOlHGpJ83PJcMyyVvDtB_0PUzGqHIUcdMlJivZxq4ANkiJcooL61pJjq3daT3yEg3nqYH\",\"redirectUrl\":\"https://www.farmatodo.com.co/marcas/revitalift\",\"active\":false,\"list\":[],\"listMobile\":[]},{\"label\":\"CeraVe\",\"position\":0,\"urlBanner\":\"https://lh3.googleusercontent.com/H-Ob7nawZMBHUo4DbGTP10VE_qlQuoMixNwuq-71zxNcIV9Pt0uCfRssCLgthkbZwf6bgq1hnqnmdSLCWBR_Pyt9HH0zcCQHrYj44u0eb0_RfZQ\",\"redirectUrl\":\"https://www.farmatodo.com.co/marcas/cerave\",\"active\":false,\"list\":[],\"listMobile\":[]},{\"label\":\"Garnier\",\"position\":0,\"urlBanner\":\"https://lh3.googleusercontent.com/9Rpip2b7kXj60WXD_FacH4MVQU1YeUsnTy8vLlVhScqEwA0c7Cl1-PrStvlnL7Jskkn3ZtrPMx5cJ_AeVXCroUHAix8BG9m3Vrg7FLYPo1DsRdY2\",\"redirectUrl\":\"https://www.farmatodo.com.co/marcas/garnier\",\"active\":false,\"list\":[],\"listMobile\":[]},{\"label\":\"La Roche-Posay\",\"position\":0,\"urlBanner\":\"https://lh3.googleusercontent.com/7trF_GsDf-6lkapwjhcLAg0dEmj6UwG1aW_ZTDIhrHDgYhhY22k9eeOWiahCt9XKGkeJtOSqCtVY3uE11bCxk8wBNZw1T1fNzMFop4XkuZOFWok\",\"redirectUrl\":\"https://www.farmatodo.com.co/marcas/la_roche\",\"active\":false,\"list\":[],\"listMobile\":[]},{\"label\":\"Maybelline\",\"position\":0,\"urlBanner\":\"https://lh3.googleusercontent.com/_nZXvOV3FmUg5KkCYpDaYegsmqYBiLrCp-Lnxvpy6wwDylExPFQ6OZX9HNmd7hFKF4UIFeGrlreXDlodhLW1BBbgecrwcyWShkql5fMZCdIipkli\",\"redirectUrl\":\"https://www.farmatodo.com.co/marcas/maybelline\",\"active\":false,\"list\":[],\"listMobile\":[]},{\"label\":\"Vichy\",\"position\":0,\"urlBanner\":\"https://lh3.googleusercontent.com/l9EuVyldJANRlLFEHWhd6audJKMDxo5P4BAvapQRaTQESOit0nL8x0WYXAJ43h-hbYeLRIsQnukT2HdVFf-cscPd3N2HOEfHX0ZnB8OzCjMf2xIf\",\"redirectUrl\":\"https://www.farmatodo.com.co/marcas/vichy\",\"active\":false,\"list\":[],\"listMobile\":[]},{\"label\":\"Vogue\",\"position\":0,\"urlBanner\":\"https://lh3.googleusercontent.com/s_NzDa0u0CmLN51pPyAPnXcWq7riKAgFhM7UOiPORhybIctiFzSgzEythb4IwVNaf0ITp54rj9ZbleFNedxMbMg-oBSayFF0RdySK7Ll4msbMNuB\",\"redirectUrl\":\"https://www.farmatodo.com.co/marcas/vogue\",\"active\":false,\"list\":[],\"listMobile\":[]},{\"label\":\"Elvive\",\"position\":0,\"urlBanner\":\"https://lh3.googleusercontent.com/78glu9IYMU4mqcpGMm83ayl_-lAldPJh5dSOC6c7n1CKdT6EnBqG895NX3o6DrcEYzLiRm-CmsmpAdq1BAsR8BrFk-Rbl0acf2fYYYHLnJdRqUo\",\"redirectUrl\":\"https://www.farmatodo.com.co/marcas/elvive\",\"active\":false,\"list\":[],\"listMobile\":[]},{\"label\":\"P\\u0026G\",\"position\":0,\"urlBanner\":\"https://lh3.googleusercontent.com/TNntTO5WcucvlB6r9H6L8Hwj4lrSFLmCrf92BXqoq9YCkrekTQSjKAZIZu_5iTfWombrGJrjzLApjUMClkUMaS9p4w8GB4ZdhXKqdepE6JQcICQ\",\"redirectUrl\":\"https://www.farmatodo.com.co/marcas/pg\",\"active\":false,\"list\":[],\"listMobile\":[]},{\"label\":\"Sanofi\",\"position\":0,\"urlBanner\":\"https://lh3.googleusercontent.com/j2MqBL9euKYILBK_VyDjuufUTCEz50XaUgY4YJfljulie6CXH_0qucCO7HxpOEt4AUGyLnVnerElEH-m3XS7APwFXmR4RqzhkShtUYWJE_loPg5N\",\"redirectUrl\":\"https://www.farmatodo.com.co/marcas/sanofi\",\"active\":false,\"list\":[],\"listMobile\":[]},{\"label\":\"Schwarzkopf\",\"position\":0,\"urlBanner\":\"https://lh3.googleusercontent.com/K7Xbea9zATB4aO12E2VZ_Us4qILNnhTsepvSgJt6DLjC-UiWlpFj7yppDg6Nv3FAjB37Laj6FuUJWE8Y6q2xi-xV84eXTWnJNmF500MdEFcsyvuz\",\"redirectUrl\":\"https://www.farmatodo.com.co/marcas/schwarzkopf\",\"active\":false,\"list\":[],\"listMobile\":[]},{\"label\":\"Balance\",\"position\":0,\"urlBanner\":\"https://lh3.googleusercontent.com/3YiMjICFu8pWz3kVMNUoToOX96T2t-vYZVBjqZWZ2ugaW9f46RExAV-VlZwodbep7iFEbdjvOk8REZOlT7WVxAbZxgd95wCv22K4WLsQkyxEJdI\",\"redirectUrl\":\"https://www.farmatodo.com.co/marcas/balance\",\"active\":false,\"list\":[],\"listMobile\":[]},{\"label\":\"Avène\",\"position\":0,\"urlBanner\":\"https://lh3.googleusercontent.com/NLNEWFac6iViUC2EuLHKUdWMV9NhumA68ImrnLO6BVHqsEZtoJ0pW3TypK0FbI4YyN4SWA_VKGzH1woT91hzebkSZYyOivQ1h1u2-VTaUJYYVyk\",\"redirectUrl\":\"https://www.farmatodo.com.co/marcas/avene\",\"active\":false,\"list\":[],\"listMobile\":[]},{\"label\":\"Colgate\",\"position\":0,\"urlBanner\":\"https://lh3.googleusercontent.com/ueAXWO8updA2GUnGmJ88Nv09hQXGX_4N-uy0U7yQfYeuA2HyMYZESUF7PrXUxSpfoIYqKHQUOxfidMHIOv17eFb3r7taLS1s5os2ZYjNRTWYwJc\",\"redirectUrl\":\"https://www.farmatodo.com.co/marcas/colgate\",\"active\":false,\"list\":[],\"listMobile\":[]},{\"label\":\"Eucerin\",\"position\":0,\"urlBanner\":\"https://lh3.googleusercontent.com/nxmJ0ZEVj8jRQErO7YRy-mvshIeCT2RT-z2tUwdUKxrAvWiBTCs-8AgFQAJt0lthtAmwVZCRYc_LAm6RMqYJJuGz4GoW06rHUI4dmP-QjhP3FEY\",\"redirectUrl\":\"https://www.farmatodo.com.co/marcas/eucerin\",\"active\":false,\"list\":[],\"listMobile\":[]},{\"label\":\"Ducray\",\"position\":0,\"urlBanner\":\"https://lh3.googleusercontent.com/uXvJqrHNUP61qnSSrZWrw_cWZg6uoaaIatUa1GvjV3EJ5ODbfJtu_zOCAm6G2B89FgzJbs_Fvzrf5K-5WiIwh0k9xZPcLzkt7EtfpXqQrP6Q5NF3pA\",\"redirectUrl\":\"https://www.farmatodo.com.co/marcas/ducray\",\"active\":false,\"list\":[],\"listMobile\":[]},{\"label\":\"Nivea\",\"position\":0,\"urlBanner\":\"https://lh3.googleusercontent.com/kcvR4EU5tmd8I6Mb0r8HSHUChyhAqkB47XMuwlCIgfXMZxcYN--Nx--rVGfMchIn-TcAXYTC4D6dV2AGmc-HZ-4PjiWWBgPb124huMO4I0I8cN5g\",\"redirectUrl\":\"https://www.farmatodo.com.co/marcas/nivea\",\"active\":false,\"list\":[],\"listMobile\":[]},{\"label\":\"Luminous\",\"position\":0,\"urlBanner\":\"https://lh3.googleusercontent.com/w3YnfRKHlSHZPGr7mJ8Ig2aqLLlldJqgkWgCVWsoS2aYjJGPZVIM-KdrNdLK-0PXc4jamcNUaB1melcx6U-9KFs19ZxAX9EGY1XPYfTHBcg2oSBk\",\"redirectUrl\":\"https://www.farmatodo.com.co/marcas/luminous\",\"active\":false,\"list\":[],\"listMobile\":[]},{\"label\":\"Genfar\",\"position\":0,\"urlBanner\":\"https://lh3.googleusercontent.com/UXNifOX8J1LrEcri3KpvhCcERX-jcYMQEZXDyo26-WxDNytz5u0JCAStYiXZ9MdIk_3q0seU4QdQg7p-BpYvgQs9ufiea9jPjuNaHRsHD8zMZso\",\"redirectUrl\":\"https://www.farmatodo.com.co/marcas/genfar\",\"active\":false,\"list\":[],\"listMobile\":[]},{\"label\":\"Neutrogena\",\"position\":0,\"urlBanner\":\"https://lh3.googleusercontent.com/TutpnSaI6dzqseAxW5gJPxPmpC0UUfaFZyfPExFMZqYZoOtcOIc0yMJ8wxD43tZ2q-ZimT5TeEHrlOe7B3HyLy0mMIiKRsDP6vjqBcXL1uZ1EsIPmg\",\"redirectUrl\":\"hhttps://www.farmatodo.com.co/marcas/neutrogena\",\"active\":false,\"list\":[],\"listMobile\":[]},{\"label\":\"Lubriderm\",\"position\":0,\"urlBanner\":\"https://lh3.googleusercontent.com/kbEYE14ee_ZuCbBgYrEzcSPPR12XCazEHZM55YE1rKzNARok9gkWZOPdb36V1Cvye2EGcRm4NVdtYglDybNf0--7lDMR2oxiuBmaxnGBaK7QGotIlw\",\"redirectUrl\":\"https://www.farmatodo.com.co/marcas/lubriderm\",\"active\":false,\"list\":[],\"listMobile\":[]},{\"label\":\"Johnsons\",\"position\":0,\"urlBanner\":\"https://lh3.googleusercontent.com/tR9eN3YahItLZErZ4U7HGPKev5KmbtAHVMFC5Z8J_0tOLBdDA0u3XBSIcLOIl8TL57ceuRZnX5E7M2gwKAPnfmgy0nRKiqSeYSnX25MexjmG-Y0v\",\"redirectUrl\":\"https://www.farmatodo.com.co/marcas/johnsons\",\"active\":false,\"list\":[],\"listMobile\":[]},{\"label\":\"Listerine\",\"position\":0,\"urlBanner\":\"https://lh3.googleusercontent.com/-WEpTOoho4t-16CW-oAhdKeRL95GLA1hOeRMepVGgcSC5MMk8FH-TICS1wkQ47ubc408WbU_v2uf1GYJadXlzMxwrhkWsGk5M4tXKlju9oJ2Syg\",\"redirectUrl\":\"https://www.farmatodo.com.co/marcas/listerine\",\"active\":false,\"list\":[],\"listMobile\":[]}]},\"position\":14,\"active\":true,\"list\":[],\"listMobile\":[]}]}}";
        try{

            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_HOME_V2_CONFIG);

            if (jsonCachedOptional.isPresent()){
                return new Gson().fromJson(jsonCachedOptional.get(),HomeConfigAlgolia.class);
            }

            Index<HomeConfigAlgolia> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES,HomeConfigAlgolia.class);
            Optional<HomeConfigAlgolia> optionalHomeConfigAlgolia = index.getObject(URLConnections.ALGOLIA_HOME_V2_CONFIG);

            if (optionalHomeConfigAlgolia.isPresent()){
                String jsonToCache = new Gson().toJson(optionalHomeConfigAlgolia.get());
                CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_HOME_V2_CONFIG, jsonToCache);
                return optionalHomeConfigAlgolia.orElseGet(HomeConfigAlgolia::new);
            }

            return new Gson().fromJson(homeBackup, HomeConfigAlgolia.class);
        }catch (Exception e) {
            LOG.warning("Error in APIAlgolia.getHomeConfig. Message:"+ e.getMessage() + " Se usa home de backup");
            return new Gson().fromJson(homeBackup, HomeConfigAlgolia.class);
        }
    }


    /**
     * return grids from cities express
     * @return
     */
    // cached
    public static GeoGridsConfigAlgolia getGeoGridsAlgolia(){

        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_GEO_GRIDS);

            if (jsonCachedOptional.isPresent()){
                return new Gson().fromJson(jsonCachedOptional.get(),GeoGridsConfigAlgolia.class);
            }

            Index<GeoGridsConfigAlgolia> index = algoliaClient
                    .initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES,GeoGridsConfigAlgolia.class);
            Optional<GeoGridsConfigAlgolia> configAlgoliaOptional = index.getObject(URLConnections.ALGOLIA_GEO_GRIDS);

            configAlgoliaOptional.ifPresent( data -> {
                String jsonToCache = new Gson().toJson(configAlgoliaOptional.get());
                CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_GEO_GRIDS, jsonToCache);
            } );
            //

            return configAlgoliaOptional.orElseGet(GeoGridsConfigAlgolia::new);
        } catch (AlgoliaException e) {
            e.printStackTrace();
        }

        return new GeoGridsConfigAlgolia();
    }

    /**
     * return landing pages is active
     * @return
     */
    //cached
    public static Boolean isActiveLandingPages(){
        try {

            Optional<String> jsonCachedOptional = CachedDataManager
                    .getJsonFromCache(URLConnections.ALGOLIA_LANDING_PROVIDERS_AND_ACTIVE);

            if (jsonCachedOptional.isPresent()){
                LandingProviderAndActiveAlgolia landingConfig = new Gson()
                        .fromJson(jsonCachedOptional.get(),LandingProviderAndActiveAlgolia.class);
                return landingConfig.isActive();
            }

            Index<LandingProviderAndActiveAlgolia> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, LandingProviderAndActiveAlgolia.class);
            Optional<LandingProviderAndActiveAlgolia> activeAlgoliaOptional = index.getObject(URLConnections.ALGOLIA_LANDING_PROVIDERS_AND_ACTIVE);

            if (activeAlgoliaOptional.isPresent()) {
                String jsonToCache = new Gson().toJson(activeAlgoliaOptional.get());
                CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_LANDING_PROVIDERS_AND_ACTIVE, jsonToCache);
                return activeAlgoliaOptional.get().isActive();
            }

        }catch (AlgoliaException e){
            LOG.warning(e.getMessage());
        }
        return false;
    }

    //cached
    public static Optional<Boolean> isActiveSubtractStock(){
        try {

            Optional<String> jsonCachedOptional = CachedDataManager
                    .getJsonFromCache(URLConnections.ALGOLIA_SUBTRACT_STOCK_CONFIG);

            if (jsonCachedOptional.isPresent()){
                return Optional.of(new Gson()
                        .fromJson(jsonCachedOptional.get(),SubtractStockConfigAlgolia.class).isActive()
                );
            }

            Index<SubtractStockConfigAlgolia> index = algoliaClient
                    .initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, SubtractStockConfigAlgolia.class);

            Optional<SubtractStockConfigAlgolia> configAlgoliaOptional = index
                    .getObject(URLConnections.ALGOLIA_SUBTRACT_STOCK_CONFIG);

            configAlgoliaOptional.ifPresent( data -> {
                String jsonToCache = new Gson().toJson(configAlgoliaOptional.get());
                CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_SUBTRACT_STOCK_CONFIG, jsonToCache);
            } );

            if (!configAlgoliaOptional.isPresent()){
                return Optional.of(Boolean.FALSE);
            }

            if (configAlgoliaOptional.get().isActive()){
                return Optional.of(Boolean.TRUE);
            }

            return Optional.empty();

        } catch (AlgoliaException e) {
            e.printStackTrace();
        }

        return Optional.empty();

    }

    /***
     * active user
     * @return
     */
    public static Optional<Boolean> isActiveLoadDataAmplitude(){
        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_LOAD_DATA_AMPLITUDE);

            if (jsonCachedOptional.isPresent()){
                //LOG.info("Tiene cache: isActiveLoadDataAmplitude");
                return Optional.of(new Gson().fromJson(jsonCachedOptional.get(),LoadDataAmplitudeConfig.class).isActive());
            }

            Index<LoadDataAmplitudeConfig> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, LoadDataAmplitudeConfig.class);
            Optional<LoadDataAmplitudeConfig> configAlgoliaOptional = index.getObject(URLConnections.ALGOLIA_LOAD_DATA_AMPLITUDE );

            //LOG.info("No tiene cache: isActiveLoadDataAmplitude");
            configAlgoliaOptional.ifPresent( data -> {
                String jsonToCache = new Gson().toJson(configAlgoliaOptional.get());
                CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_LOAD_DATA_AMPLITUDE, jsonToCache);
            } );

            if (!configAlgoliaOptional.isPresent()){
                return Optional.of(Boolean.FALSE);
            }

            // active feature
            if (configAlgoliaOptional.get().isActive()){
                return Optional.of(Boolean.TRUE);
            }

            return Optional.empty();

        } catch (AlgoliaException e) {
            e.printStackTrace();
        }

        return Optional.empty();

    }

    //cached
    public static Optional<Boolean> isActiveLoadDataAmplitudeOrder(){
        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_LOAD_DATA_AMPLITUDE);

            if (jsonCachedOptional.isPresent()){
                //LOG.info("Tiene cache: isActiveLoadDataAmplitudeOrder");
                return Optional.of(new Gson().fromJson(jsonCachedOptional.get(),LoadDataAmplitudeConfig.class).isActive());
            }

            Index<LoadDataAmplitudeConfig> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, LoadDataAmplitudeConfig.class);

            Optional<LoadDataAmplitudeConfig> configAlgoliaOptional = index.getObject(URLConnections.ALGOLIA_LOAD_DATA_AMPLITUDE );

            //LOG.info("No tiene cache: isActiveLoadDataAmplitudeOrder");
            configAlgoliaOptional.ifPresent( data -> {
                String jsonToCache = new Gson().toJson(configAlgoliaOptional.get());
                CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_LOAD_DATA_AMPLITUDE, jsonToCache);
            } );

            if (!configAlgoliaOptional.isPresent()){
                return Optional.of(Boolean.FALSE);
            }

            // active feature
            if (configAlgoliaOptional.get().isOrderCompleted()){
                return Optional.of(Boolean.TRUE);
            }

            return Optional.empty();

        } catch (AlgoliaException e) {
            e.printStackTrace();
        }

        return Optional.empty();

    }

    //cached
    public static ProviderConfigAlgolia getLandingConfig(){
        try{
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_LANDING_PAGES);

            if (jsonCachedOptional.isPresent()){
                //LOG.info("Tiene cache: getLandingConfig");
                return new Gson().fromJson(jsonCachedOptional.get(),ProviderConfigAlgolia.class);
            }
            //LOG.info("INIT getLandingConfig()");
            Index<ProviderConfigAlgolia> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES,ProviderConfigAlgolia.class);
            //LOG.info("ObjectId -> " + URLConnections.ALGOLIA_LANDING_PAGES);
            Optional<ProviderConfigAlgolia> optionalProviderConfigAlgolia = index.getObject(URLConnections.ALGOLIA_LANDING_PAGES);
            //LOG.info("Is present? -> " + optionalProviderConfigAlgolia.isPresent());
            //LOG.info("No tiene cache: getLandingConfig");
            optionalProviderConfigAlgolia.ifPresent( data -> {
                String jsonToCache = new Gson().toJson(optionalProviderConfigAlgolia.get());
                CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_LANDING_PAGES, jsonToCache);
            } );


            return optionalProviderConfigAlgolia.orElseGet(ProviderConfigAlgolia::new);
        }catch (Exception e) {
            LOG.warning("Error in APIAlgolia.getLandingConfig. Message:"+ e.getMessage());
            e.getStackTrace();
        }
        return new ProviderConfigAlgolia();
    }

    //cached
    public static ProviderResponse getProviderDataForLandingPage(final String provider){
        try {

            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(provider);

            if (jsonCachedOptional.isPresent()){
                //LOG.info("Tiene cache: getProviderDataForLandingPage");
                return new Gson().fromJson(jsonCachedOptional.get(),ProviderResponse.class);
            }

            //LOG.info("INIT getLandingConfig() -> " + provider);
            final String objectId = provider + URLConnections.ALGOLIA_LANDING_PAGES_PROVIDER;
            //LOG.info("ObjectId -> " + objectId);
            //LOG.info("INDEX -> " + URLConnections.ALGOLIA_INDEX_LANDING_PAGES);
            Index<ProviderResponse> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_LANDING_PAGES, ProviderResponse.class);
            Optional<ProviderResponse> optionalProviderResponse = index.getObject(objectId);
            LOG.info("is present -> " + optionalProviderResponse.isPresent());
            //LOG.info("No tiene cache: getProviderDataForLandingPage");
            optionalProviderResponse.ifPresent( data -> {
                String jsonToCache = new Gson().toJson(optionalProviderResponse.get());
                CachedDataManager.saveJsonInCache(objectId, jsonToCache);
            } );

            return optionalProviderResponse.orElseGet(ProviderResponse::new);
        }catch (AlgoliaException e){
            LOG.warning("Error in getProviderDataForLandingPage. Message:" + Arrays.toString(e.getStackTrace()));
        }
        return null;
    }

    //cached
    public static ItemConfigAlgolia getItemDetailConfig() throws AlgoliaException {
        //try{
            //LOG.info("INIT getItemDetailConfig()");
        Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_PRODUCT_DETAIL);

        if (jsonCachedOptional.isPresent()){
            //LOG.info("Tiene cache: getItemDetailConfig");
            return new Gson().fromJson(jsonCachedOptional.get(),ItemConfigAlgolia.class);
        }

            Index<ItemConfigAlgolia> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, ItemConfigAlgolia.class);
            //LOG.info("ObjectId -> " + URLConnections.ALGOLIA_PRODUCT_DETAIL);
            Optional<ItemConfigAlgolia> optionalItemConfigAlgolia = index.getObject(URLConnections.ALGOLIA_PRODUCT_DETAIL);
            //LOG.info("Is present? -> " + optionalItemConfigAlgolia.isPresent());
        //LOG.info("No tiene cache: getItemDetailConfig");
        optionalItemConfigAlgolia.ifPresent( data -> {
            String jsonToCache = new Gson().toJson(optionalItemConfigAlgolia.get());
            CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_PRODUCT_DETAIL, jsonToCache);
        } );
            return optionalItemConfigAlgolia.orElseGet(ItemConfigAlgolia::new);
        //}catch (Exception e) {
        //    LOG.warning("Error in APIAlgolia.getItemDetailConfi. Message:"+ e.getMessage());
        //    e.getStackTrace();
        //}
        //return new ItemConfigAlgolia();
    }


    //cached
    public static PhotoSlurpConfigAlgolia PhotoSlurpConfig(){
        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_PHOTO_SLURP_CONFIG);

            if (jsonCachedOptional.isPresent()){
                //LOG.info("Tiene cache: getItemDetailConfig");
                return new Gson().fromJson(jsonCachedOptional.get(),PhotoSlurpConfigAlgolia.class);
            }

            Index<PhotoSlurpConfigAlgolia> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, PhotoSlurpConfigAlgolia.class);
            Optional<PhotoSlurpConfigAlgolia> optionalPhotoSlurpConfigAlgolia = index.getObject(URLConnections.ALGOLIA_PHOTO_SLURP_CONFIG);
            //LOG.info("No tiene cache: getItemDetailConfig");
            optionalPhotoSlurpConfigAlgolia.ifPresent( data -> {
                String jsonToCache = new Gson().toJson(optionalPhotoSlurpConfigAlgolia.get());
                CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_PHOTO_SLURP_CONFIG, jsonToCache);
            } );
            return optionalPhotoSlurpConfigAlgolia.orElseGet(PhotoSlurpConfigAlgolia::new);
        } catch (Exception e) {
            LOG.warning("Error in get ALGOLIA_PHOTO_SLURP. Message:" + Arrays.toString(e.getStackTrace()));
        }
        return null;
    }

    public static List<VideoData> getAllVideos(){
        try{
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_INDEX_PROD_VIDEO);
            if (jsonCachedOptional.isPresent()){
                Type listType = new TypeToken<List<VideoData>>(){}.getType();
                return new Gson().fromJson(jsonCachedOptional.get(), listType);
            }

            List<IndexQuery> queries = new ArrayList<>();
            queries.add(new IndexQuery(URLConnections.ALGOLIA_INDEX_PROD_VIDEO, new Query()));
            List<VideoData> videoDataList = getVideoDataFromMultiqueries(algoliaClient.multipleQueries(queries).getResults().get(0));

            String jsonToCache = new Gson().toJson(videoDataList);
            CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_INDEX_PROD_VIDEO, jsonToCache);
            return videoDataList;
        }catch (Exception e) {
            LOG.warning("Error in APIAlgolia.getAllVideos. Message:"+ e.getMessage());
            e.getStackTrace();
        }
        return null;
    }

    public static List<VideoData> getVideosForItem(final Long idItem){
        try{
            List<VideoData> resulVideo = new ArrayList<>();
            List<VideoData> videoDataList = getAllVideos();
            if (!videoDataList.isEmpty()) {
                videoDataList.forEach(videoData -> {
                    if (Objects.nonNull(videoData))
                        videoData.getProducts().forEach(product -> {
                            if (product.equals(idItem))
                                resulVideo.add(videoData);
                        });
                });
            }
            return resulVideo;
        }catch (Exception e) {
            LOG.warning("Error in APIAlgolia.getVideosForItem. Message:"+ e.getMessage());
            e.getStackTrace();
        }
        return null;
    }

    public static Boolean existsInAlgolia(final int idItem){
        List<ItemAlgolia> result;
        try {
            Index<ItemAlgolia> index = algoliaClient.initIndex(URLConnections.ALGOLIA_PRODUCTS, ItemAlgolia.class);
            Query query = new Query().setFilters(String.format("id:'%s'", idItem));
            IndexIterable<ItemAlgolia> indexIterable = index.browse(query);
            result = indexIterable.stream().collect(Collectors.toList());
            if (!result.isEmpty()){
                return true;
            }
        }catch (Exception e) {
            LOG.warning("Error in APIAlgolia.getItemByObjectID. Message:" + Arrays.toString(e.getStackTrace()));
            LOG.warning("Error Message:" + e.getMessage());
        }
        return false;
    }

    public static boolean addImageItemAlgolia(final String idItem, final String urlImage, final Boolean principal) {
        LOG.warning("method: addImageItemAlgolia -> IdItem: " + idItem +" UrlImage:" +  urlImage+ " principal: "+principal);
        List<ItemAlgolia> result = null;
        try {
            String browseFilter = String.format("id:'%s'", idItem);
            String cacheKey = URLConnections.ALGOLIA_PRODUCTS + browseFilter;
            Index<ItemAlgolia> index = algoliaClient.initIndex(URLConnections.ALGOLIA_PRODUCTS, ItemAlgolia.class);

            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(cacheKey);
            if (jsonCachedOptional.isPresent()){
                Type listType = new TypeToken<List<ItemAlgolia>>(){}.getType();
                result = new Gson().fromJson(jsonCachedOptional.get(), listType);
            } else {
                Query query = new Query().setFilters(browseFilter);
                IndexIterable<ItemAlgolia> indexIterable = index.browse(query);
                result = indexIterable.stream().filter(item -> Objects.nonNull(item.getIdStoreGroup())).sorted(ORDER_ITEM_BY_STORE_ID).collect(Collectors.toList());

                String jsonToCache = new Gson().toJson(result);
                CachedDataManager.saveJsonInCache(cacheKey, jsonToCache);
            }

            List<Object> listItemsUpdate = new ArrayList<>();
            for (ItemAlgolia item : result) {
                    if (Objects.nonNull(principal) && principal) {
                        LOG.warning("method: addImageItemAlgolia -> principal: " + urlImage);
                        item.setMediaImageUrl(urlImage);
                        index.partialUpdateObject(item.getObjectID(), item);
                    } else if (Objects.isNull(item.getListUrlImages()) || item.getListUrlImages().isEmpty()) {
                        LOG.warning("method: addImageItemAlgolia -> vacio: " + urlImage);
                        ItemImages itemImage = new ItemImages(item.getObjectID(), new ArrayList<>());
                        itemImage.getListUrlImages().add(urlImage);
                        listItemsUpdate.add(itemImage);
                    } else if (item.getListUrlImages().size() < 5) {
                        LOG.warning("method: addImageItemAlgolia -> addImage: " + urlImage + " getListUrlImages: " + item.getListUrlImages().size());
                        ItemImages itemImage = new ItemImages(item.getObjectID(), item.getListUrlImages());
                        itemImage.getListUrlImages().add(urlImage);
                        listItemsUpdate.add(itemImage);
                    } else if (item.getListUrlImages().size() >= 5) {
                        LOG.warning("method: addImageItemAlgolia -> mayor a 5 " + urlImage);
                        return false;
                    }
            }
        } catch (AlgoliaException e) {
            LOG.warning("Error in APIAlgolia.getItemByObjectID. Message:" + Arrays.toString(e.getStackTrace()));
            LOG.warning("Error Message:" + e.getMessage());
            return false;
        }
        return true;
    }




    /**
     * Obtiene la configuracion de la tienda relacionada con el horario.
     * {
     * "store": 26,
     * "day": "THURSDAY",
     * "open": 0,
     * "close": 0,
     * "objectID": "26.THURSDAY"
     * }
     * */
    public static StoreConfig getStoreConfigByStoreID(final String storeID,final Calendar calendar) {
        StoreConfig storeConfig  = null;
        // cache for 5 minutes
        int cacheTime = 5 * 60;
        try {
            final String[] daysOfWeek = {"SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY","FRIDAY", "SATURDAY"};
            String DAY_OF_WEEK = daysOfWeek[calendar.get(Calendar.DAY_OF_WEEK) - 1];
            final String FILTER = ""+storeID+"."+DAY_OF_WEEK;

            Optional<String> cachedConfig = CachedDataManager.getJsonFromCacheIndex(
                    FILTER,
                    Constants.ID_INDEX_SAVE_AND_GET_REDIS
            );

            if (cachedConfig.isPresent()){
                LOG.info("Tiene cache: getStoreConfigByStoreID , FILTER: "+FILTER);
                return new Gson().fromJson(cachedConfig.get(), StoreConfig.class);
            }

            Index<StoreConfig> index = algoliaClient.initIndex(URLConnections.ALGOLIA_STORE_CONFIG, StoreConfig.class);

            Optional<StoreConfig> searchResult = index.getObject(FILTER);
            storeConfig = searchResult.orElse(null);
            CachedDataManager.saveJsonInCacheIndexTime(FILTER,
                    new Gson().toJson(storeConfig),
                    Constants.ID_INDEX_SAVE_AND_GET_REDIS,
                    cacheTime);
        } catch (AlgoliaException e) {
            LOG.warning("Error in APIAlgolia.getItemByObjectID. Message:"+ Arrays.toString(e.getStackTrace()));
        }
        return storeConfig;
    }

    /**
     * get config optimal route checkout substitutes
     * @return
     */
    //cached
    public static OptimalRouteCheckoutConfig getOptimalRouteCheckoutConfig(){

        Index<OptimalRouteCheckoutConfig> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES,OptimalRouteCheckoutConfig.class);

        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache("CONFIG.OPTIMAL.ROUTE.CHECKOUT");

            if (jsonCachedOptional.isPresent()){
                //LOG.info("Tiene cache: getOptimalRouteCheckoutConfig");
                return new Gson().fromJson(jsonCachedOptional.get(),OptimalRouteCheckoutConfig.class);
            }

            Optional<OptimalRouteCheckoutConfig> optCheckoutConfig = index.getObject("CONFIG.OPTIMAL.ROUTE.CHECKOUT");

            //LOG.info("No tiene cache: getOptimalRouteCheckoutConfig");
            optCheckoutConfig.ifPresent( data -> {
                String jsonToCache = new Gson().toJson(optCheckoutConfig.get());
                CachedDataManager.saveJsonInCache("CONFIG.OPTIMAL.ROUTE.CHECKOUT", jsonToCache);
            } );

            if (optCheckoutConfig.isPresent()) {
                return optCheckoutConfig.get();
            }

        } catch (AlgoliaException e) {
            e.printStackTrace();
        }

        return new OptimalRouteCheckoutConfig();


    }


    // cached
    public static PropertiesAlgoliaFacets getFacetsAlgolia() {
        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_PROPERTIES);

            if (jsonCachedOptional.isPresent()) {
                //LOG.info("Tiene cache: getFacetsAlgolia");
                return new Gson().fromJson(jsonCachedOptional.get(), PropertiesAlgoliaFacets.class);
            }

            Index<PropertiesAlgoliaFacets> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, PropertiesAlgoliaFacets.class);

            Optional<PropertiesAlgoliaFacets> optCheckoutConfig = index.getObject(URLConnections.ALGOLIA_PROPERTIES);

            //LOG.info("No tiene cache: getFacetsAlgolia");
            optCheckoutConfig.ifPresent(data -> {
                String jsonToCache = new Gson().toJson(optCheckoutConfig.get());
                CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_PROPERTIES, jsonToCache);
            });

            return optCheckoutConfig.orElseGet(PropertiesAlgoliaFacets::new);
            //return index.getObject(URLConnections.ALGOLIA_PROPERTIES).get();

        } catch (Exception e) {
            e.getStackTrace();
            return null;
        }

    }

    // cached
    public static DistanceProperties  getDistanceProperties(){
        try {

            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOILIA_DISTANCE_PROPERTIES);

            if (jsonCachedOptional.isPresent()){
                //LOG.info("Tiene cache: getFacetsAlgolia");
                return new Gson().fromJson(jsonCachedOptional.get(),DistanceProperties.class);
            }
            Index<DistanceProperties> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES,DistanceProperties.class);

            Optional<DistanceProperties> optCheckoutConfig = index.getObject(URLConnections.ALGOILIA_DISTANCE_PROPERTIES);
            //LOG.info("No tiene cache: getFacetsAlgolia");
            optCheckoutConfig.ifPresent( data -> {
                String jsonToCache = new Gson().toJson(optCheckoutConfig.get());
                CachedDataManager.saveJsonInCache(URLConnections.ALGOILIA_DISTANCE_PROPERTIES, jsonToCache);
            } );

            return optCheckoutConfig.orElseGet(DistanceProperties::new);
            //return index.getObject(URLConnections.ALGOILIA_DISTANCE_PROPERTIES).get();

        }catch (Exception e){
            e.getStackTrace();
            return null;
        }

    }

    //cached
    public static PaymentMethodsProperties getPaymentMethods(){
        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_PAYMENTMETHODS_PROPERTIES);

            if (jsonCachedOptional.isPresent()){
                //LOG.info("Tiene cache: getPaymentMethods");
                return new Gson().fromJson(jsonCachedOptional.get(),PaymentMethodsProperties.class);
            }
            Index<PaymentMethodsProperties> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES,PaymentMethodsProperties.class);

            Optional<PaymentMethodsProperties> optCheckoutConfig = index.getObject(URLConnections.ALGOLIA_PAYMENTMETHODS_PROPERTIES);
            //LOG.info("No tiene cache: getPaymentMethods");
            optCheckoutConfig.ifPresent( data -> {
                String jsonToCache = new Gson().toJson(optCheckoutConfig.get());
                CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_PAYMENTMETHODS_PROPERTIES, jsonToCache);
            } );

            return optCheckoutConfig.orElseGet(PaymentMethodsProperties::new);
            //return index.getObject(URLConnections.ALGOLIA_PAYMENTMETHODS_PROPERTIES).get();
        }catch (Exception e){
            e.getStackTrace();
            return null;
        }
    }
    /**
     * Obtener la configiracion de tiempos de envio de Algolia.
     * */
    public static DeliveryTypeTime getDeliveryTimeConfig() {
        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(Constants.OBJECT_ID_DELIVERY_TYPE_TIME);
            if (jsonCachedOptional.isPresent()) {
                return new Gson().fromJson(jsonCachedOptional.get(), DeliveryTypeTime.class);
            }

            Index<DeliveryTypeTime> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, DeliveryTypeTime.class);
            Optional<DeliveryTypeTime> deliveryTypeTime = index.getObject(Constants.OBJECT_ID_DELIVERY_TYPE_TIME);

            if (deliveryTypeTime.isPresent()) {
                String jsonToCache = new Gson().toJson(deliveryTypeTime.get());
                CachedDataManager.saveJsonInCache(Constants.OBJECT_ID_DELIVERY_TYPE_TIME, jsonToCache);
            }
            return deliveryTypeTime.orElse(null);
        } catch (AlgoliaException e) {
            LOG.warning("Error in APIAlgolia.getDeliveryTimeConfig. Message:" + Arrays.toString(e.getStackTrace()));
            return null;
        }
    }

    // cached
    public static TypeAutoComplete getTypeAutoComplete(){
        try {

            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_AUTOCOMPLETE_GOOGLE_PROPERTIES);

            if (jsonCachedOptional.isPresent()){
                //LOG.info("Tiene cache: getTypeAutoComplete");
                return new Gson().fromJson(jsonCachedOptional.get(),TypeAutoComplete.class);
            }

            Index<TypeAutoComplete> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES,TypeAutoComplete.class);

            Optional<TypeAutoComplete> optCheckoutConfig = index.getObject(URLConnections.ALGOLIA_AUTOCOMPLETE_GOOGLE_PROPERTIES);
            //LOG.info("No tiene cache: getTypeAutoComplete");
            optCheckoutConfig.ifPresent( data -> {
                String jsonToCache = new Gson().toJson(optCheckoutConfig.get());
                CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_AUTOCOMPLETE_GOOGLE_PROPERTIES, jsonToCache);
            } );

            return optCheckoutConfig.orElseGet(TypeAutoComplete::new);
            //return index.getObject(URLConnections.ALGOLIA_AUTOCOMPLETE_GOOGLE_PROPERTIES).get();

        }catch (Exception e){
            e.getStackTrace();
        }
        return null;
    }

    public static GroupSorted getSortedItemsInGroup(String idGroup){
        try {
            Index<GroupSorted> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_SORTED_HIGHLIGHT,GroupSorted.class);

            return index.getObject("IDGROUPSORT."+idGroup).get();
        }catch (Exception e){
            e.getStackTrace();
        }
        return null;
    }

    public static boolean deleteSortedItemsInGroup(String idGroup){
        try {
            Index<GroupSorted> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_SORTED_HIGHLIGHT,GroupSorted.class);
            index.deleteObject("IDGROUPSORT."+idGroup);
            return true;
        }catch (Exception e){
            LOG.warning("Error in APIAlgolia.deleteSortedItemsInGroup. Message:"+ Arrays.toString(e.getStackTrace()));
            return false;
        }
    }

    public static boolean saveSortedItemsInGroup(ProductSortGroup productSortGroup){
        try {
            Index<GroupSorted> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_SORTED_HIGHLIGHT,GroupSorted.class);
            index.saveObject("IDGROUPSORT."+productSortGroup.getIdGroup(), new GroupSorted(productSortGroup.getItems())).waitForCompletion();
            return true;
        }catch (Exception e){
            LOG.warning("Error in APIAlgolia.saveSortedItemsInGroup. Message:"+ Arrays.toString(e.getStackTrace()));
            return false;
        }
    }

    public static AdvisedItem getAdvisedItems() {
        try {
            Index<AdvisedItem> index = algoliaClient.initIndex(AlgoliaIndexEnum.ADVISED_ITEMS.getIndexName(), AdvisedItem.class);
            return index.getObject(Constants.ADVISED_ITEM_INDEX).orElse(null);
        } catch (Exception e) {
            LOG.warning("Error en el método getAdvisedItems {} => " + e.getMessage());
        }
        return null;
    }

    public static HitsItemsAlgolia getItemsBySubCategorie(String nameCategorie, Integer store, Integer hitsPerPage, Integer page,String categorieType){
        return getItemsBySubCategorie(nameCategorie, store, hitsPerPage, page, categorieType, null);
    }

    public static HitsItemsAlgolia getItemsBySubCategorie(String nameCategorie, Integer store, Integer hitsPerPage,
                                                          Integer page, String categorieType, Boolean subscribeAndSave){
        try {
            Index<ItemAlgolia> indexProducts = algoliaClient.initIndex(AlgoliaIndexEnum.PRODUCTS_PROD.getIndexName(), ItemAlgolia.class);

            LOG.info("Type : " + categorieType);
            String searchBy = "subCategory";
            if (categorieType.equals("Categoria")){
                searchBy = "categorie";
            }else if(categorieType.equals("Departamento")) {
                searchBy  = "Departamentos";
            }

            Query queryItemByID = new Query().setFilters("idStoreGroup="+store + " AND " + searchBy + ":'" + nameCategorie + "'"
                    + (subscribeAndSave != null && subscribeAndSave? " AND subscribeAndSave:'" + subscribeAndSave + "'" : ""));

            LOG.info( "QUERY ---->  TOTAL ITEMS ALL PAGES -> " + "idStoreGroup="+store + " AND " + searchBy + ":'" + nameCategorie + "'"
                    + (subscribeAndSave != null && subscribeAndSave? " AND subscribeAndSave:'" + subscribeAndSave + "'" : ""));


            queryItemByID.setHitsPerPage(hitsPerPage);
            queryItemByID.setPage(page);

            SearchResult<ItemAlgolia> itemAlgolia = indexProducts.search(queryItemByID);
            LOG.info("TOTAL ITEMS ALL PAGES -> " + itemAlgolia.getNbHits());
            LOG.info("TOTAL PAGES -> " + itemAlgolia.getNbPages());

            HitsItemsAlgolia hitsItemsAlgolia = new HitsItemsAlgolia();
            hitsItemsAlgolia.setItemAlgoliaList(itemAlgolia.getHits());
            hitsItemsAlgolia.setNbHits(itemAlgolia.getNbHits());
            hitsItemsAlgolia.setNbHitsPerPage(itemAlgolia.getHitsPerPage());
            hitsItemsAlgolia.setNbPages(itemAlgolia.getNbPages());

            return  hitsItemsAlgolia;
        } catch (AlgoliaException e) {
            LOG.warning("Error in APIAlgolia.getItemAlgolia. Message:"+ Arrays.toString(e.getStackTrace()));
            return null;
        }
    }

    public static HitsItemsAlgolia getAllItemsBySubCategorieId(final int idSubCategory, String nameCategorie, Integer store, String categorieType, Boolean subscribeAndSave){
        try {
            final Integer hitsPerPage = 1000;
            Index<ItemAlgolia> indexProducts = algoliaClient.initIndex(AlgoliaIndexEnum.PRODUCTS_PROD.getIndexName(), ItemAlgolia.class);

            //LOG.info("Type : " + categorieType);
//            String searchBy = "subCategory";
            String searchBy = "SUBCATEGORY";

            if (categorieType.equals("SubCategoria"))
            {
                searchBy = "SUBCATEGORY";
            }else if (categorieType.equals("Categoria")){
                searchBy = "CATEGORY";
            }else if(categorieType.equals("Departamento")) {
                searchBy  = "DEPARTMENT";
            }

            Query queryItemByID;

            String filters = "idStoreGroup:" + store + " AND " + searchBy + ":'" + idSubCategory + "'"
                    + (subscribeAndSave != null && subscribeAndSave ? " AND subscribeAndSave:'" + subscribeAndSave + "'" : "");

            if ("SUBCATEGORY".equals(searchBy)){
                queryItemByID = new Query().setFilters(filters);

                if ( idSubCategory == ID_SUBCATEGORY_CONTACT_LENSES){
                    queryItemByID = new Query().setFilters(filters + " AND " + "outofstore:false");
                }

            }else {
                queryItemByID = new Query().setFilters(filters);
            }

//            LOG.info( "QUERY ---->  TOTAL ITEMS ALL PAGES -> " + "idStoreGroup="+store + " AND " + searchBy + ":'" + idSubCategory + "'"
//                    + (subscribeAndSave != null && subscribeAndSave? " AND subscribeAndSave:'" + subscribeAndSave + "'" : ""));

            //LOG.info("QUERY ----> "+ queryItemByID.getFilters());

            queryItemByID.setHitsPerPage(hitsPerPage);
            SearchResult<ItemAlgolia> itemAlgolia = indexProducts.search(queryItemByID);

//            LOG.info("ITEMS ----> "+itemAlgolia.getHits().size());

            HitsItemsAlgolia hitsItemsAlgolia = new HitsItemsAlgolia();
            hitsItemsAlgolia.setItemAlgoliaList(itemAlgolia.getHits());
            hitsItemsAlgolia.setNbHits(itemAlgolia.getNbHits());
            hitsItemsAlgolia.setNbPages(itemAlgolia.getNbPages());

//            LOG.info("Items: "+hitsItemsAlgolia.getItemAlgoliaList());

            return  hitsItemsAlgolia;
        }catch (AlgoliaException e){
            LOG.warning("Error in APIAlgolia.getItemAlgolia. Message: "+Arrays.toString(e.getStackTrace()));
            return null;
        }
    }

    // cached 
    public static ActiveCourierSocket getActiveCourierTrackingSocket(){
        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_COURIER_SOCKET_ACTIVE);

            if (jsonCachedOptional.isPresent()){
                //LOG.info("Tiene cache: getActiveCourierTrackingSocket");
                return new Gson().fromJson(jsonCachedOptional.get(),ActiveCourierSocket.class);
            }


            Index<ActiveCourierSocket> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES,ActiveCourierSocket.class);
            Optional<ActiveCourierSocket> optCheckoutConfig = index.getObject(URLConnections.ALGOLIA_COURIER_SOCKET_ACTIVE);

            //LOG.info("No tiene cache: getActiveCourierTrackingSocket");
            optCheckoutConfig.ifPresent( data -> {
                String jsonToCache = new Gson().toJson(optCheckoutConfig.get());
                CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_COURIER_SOCKET_ACTIVE, jsonToCache);
            } );

            return optCheckoutConfig.orElseGet(ActiveCourierSocket::new);

            //return index.getObject(URLConnections.ALGOLIA_COURIER_SOCKET_ACTIVE).get();

        }catch (Exception e){
            e.getStackTrace();
        }
        return null;
    }

    public static boolean addFarmatodoChoiseProd(RecommendedItem recommendedItem) {
        try {
            Index<RecommendedItem> indexFarmatodoChoice = algoliaClient.initIndex(AlgoliaIndexEnum.FARMATODO_CHOICE_PROD.getIndexName(), RecommendedItem.class);
            indexFarmatodoChoice.saveObject(recommendedItem.getObjectID(), recommendedItem);
            return true;
        } catch (AlgoliaException e) {
            LOG.warning("Error in APIAlgolia.addFarmatodoChoiseProd. Message:"+ Arrays.toString(e.getStackTrace()));
            return false;
        }
    }

    public static List<RecommendedItem> getChoiseProd(Integer store) {
        try {
            String filters = "store=" + store;
            String cacheKey = AlgoliaIndexEnum.FARMATODO_CHOICE_PROD.getIndexName() + filters;

            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(cacheKey);
            if (jsonCachedOptional.isPresent()) {
                Type listType = new TypeToken<List<RecommendedItem>>() {
                }.getType();
                return new Gson().fromJson(jsonCachedOptional.get(), listType);
            }
            Index<RecommendedItem> indexFarmatodoChoice = algoliaClient.initIndex(AlgoliaIndexEnum.FARMATODO_CHOICE_PROD.getIndexName(), RecommendedItem.class);
            Query queryRecommendedItemByStore = new Query().setFilters(filters);
            SearchResult<RecommendedItem> result = indexFarmatodoChoice.search(queryRecommendedItemByStore);
            List<RecommendedItem> recommendedItems = result.getHits();

            String jsonToCache = new Gson().toJson(recommendedItems);
            CachedDataManager.saveJsonInCache(cacheKey, jsonToCache);
            return recommendedItems;
        } catch (AlgoliaException e) {
            LOG.warning("Error in APIAlgolia.addFarmatodoChoiseProd. Message:" + Arrays.toString(e.getStackTrace()));
            return null;
        }
    }

    public static List<AlgoliaItem> getItemAlgolia(Integer store) {
        try {
            String filters = "idStoreGroup=" + store;
            String cacheKey = AlgoliaIndexEnum.PRODUCTS_PROD.getIndexName() + filters;

            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(cacheKey);
            if (jsonCachedOptional.isPresent()) {
                Type listType = new TypeToken<List<AlgoliaItem>>() {
                }.getType();
                return new Gson().fromJson(jsonCachedOptional.get(), listType);
            }

            Index<AlgoliaItem> indexProducts = algoliaClient.initIndex(AlgoliaIndexEnum.PRODUCTS_PROD.getIndexName(), AlgoliaItem.class);
            Query queryItemByID = new Query().setFilters(filters);
            SearchResult<AlgoliaItem> itemAlgolia = indexProducts.search(queryItemByID);
            List<AlgoliaItem> algoliaItemList = itemAlgolia.getHits();

            String jsonToCache = new Gson().toJson(algoliaItemList);
            CachedDataManager.saveJsonInCache(cacheKey, jsonToCache);
            return algoliaItemList;
        } catch (AlgoliaException e) {
            LOG.warning("Error in APIAlgolia.getItemAlgolia. Message:" + Arrays.toString(e.getStackTrace()));
            return null;
        }
    }

    public static List<AlgoliaItem> changeListTotalStockItems(Integer storeId, Long itemId, Long restrictionQuantity) {
        Index<AlgoliaItem> indexProducts = algoliaClient.initIndex(AlgoliaIndexEnum.PRODUCTS_PROD.getIndexName(), AlgoliaItem.class);
        if (storeId > 0 && itemId != null && itemId > 0 && restrictionQuantity != null && restrictionQuantity > 0) {
            try {
                LOG.info("method -> changeTotalStockItems() item : " + itemId + ", cantidad : " + restrictionQuantity);
                Query queryItemByID = new Query()
                        .setFilters("id:"+itemId.toString());
                SearchResult<AlgoliaItem> itemSearchResult = indexProducts.search(queryItemByID);
                List<AlgoliaItem> algoliaItemList = itemSearchResult.getHits().stream()
                        .filter(itemAlgolia -> Objects.nonNull(itemAlgolia))
                        .map(itemAlgolia -> updateTotalStockItem(itemAlgolia, restrictionQuantity, itemAlgolia.getIdStoreGroup())).collect(Collectors.toList());
                return Objects.nonNull(algoliaItemList) && !algoliaItemList.isEmpty() ? algoliaItemList : null;
            } catch (AlgoliaException e) {
                LOG.warning("Error in APIAlgolia.getItemAlgolia. Message:"+ Arrays.toString(e.getStackTrace()));
            }
        }
        return null;
    }

    public static boolean changeTotalStockItems(int storeId, Long itemId, Long restrictionQuantity) {
        Index<AlgoliaItem> indexProducts = algoliaClient.initIndex(AlgoliaIndexEnum.PRODUCTS_PROD.getIndexName(), AlgoliaItem.class);
        if (storeId > 0 && itemId != null && itemId > 0 && restrictionQuantity != null && restrictionQuantity > 0) {
            try {
                LOG.info("method -> changeTotalStockItems() item : " + itemId + ", cantidad : " + restrictionQuantity);
                Query queryItemByID = new Query()
                        .setFilters("id:"+itemId.toString());
                SearchResult<AlgoliaItem> itemSearchResult = indexProducts.search(queryItemByID);
                List<AlgoliaItem> algoliaItemList = itemSearchResult.getHits().stream()
                        .filter(itemAlgolia -> Objects.nonNull(itemAlgolia) &&
                                (itemAlgolia.getId()+storeId).equals(itemAlgolia.getObjectID()) &&
                                itemAlgolia.getIdStoreGroup() == storeId &&
                                itemAlgolia.getTotalStock() != restrictionQuantity.longValue())
                        .map(itemAlgolia -> updateTotalStockItem(itemAlgolia, restrictionQuantity, itemAlgolia.getIdStoreGroup())).collect(Collectors.toList());
                return true;
            } catch (AlgoliaException e) {
                LOG.warning("Error in APIAlgolia.getItemAlgolia. Message:"+ Arrays.toString(e.getStackTrace()));
            }
        }
        return false;
    }

    /**
     * update stock on algolia.
     * @param fulfilOrdDesc
     * @return
     */
    public static Optional<Boolean> updateAlgoliaStock(FulfilOrdColDescDomain.FulfilOrdDescDomain[] fulfilOrdDesc) throws AlgoliaException {
        // Validate feature Flag.
        Optional<Boolean> optionalIsActive = APIAlgolia.isActiveSubtractStock();

        if ( !optionalIsActive.isPresent() ){
            //LOG.info("1. Feature de actualizacion de stock desactivada.");
            return Optional.of(Boolean.FALSE);
        }

        // si no esta activo
        if (!optionalIsActive.get()){
            //LOG.info("2. Feature de actualizacion de stock desactivada.");
            return Optional.of(Boolean.FALSE);
        }

        //LOG.info("Feature de actualizacion de stock activa.");

        if (fulfilOrdDesc == null || fulfilOrdDesc.length == 0){
            return Optional.of(Boolean.FALSE);
        }

        ItemAlgoliaStock arrayItemsAlgoliaStock[]=new ItemAlgoliaStock[fulfilOrdDesc[0].getFulfilOrdDtl().length];
        for (FulfilOrdColDescDomain.FulfilOrdDescDomain fulfilOrdDescDomain : fulfilOrdDesc) {
            FulfilOrdColDescDomain.FulfilOrdDescDomain.FulfilOrdDtlDomain[] orderDetailArray = fulfilOrdDescDomain.getFulfilOrdDtl();

            String cityId = null;
            if (fulfilOrdDescDomain.getFulfilOrdCustDesc() != null && fulfilOrdDescDomain.getFulfilOrdCustDesc().getCityId() != null){
                cityId = fulfilOrdDescDomain.getFulfilOrdCustDesc().getCityId();
            }

            if (cityId != null){

                Optional<Integer> optionalPrincipalStoreId = getDefaultStoreByCity(cityId);
                if (optionalPrincipalStoreId.isPresent() && orderDetailArray != null && orderDetailArray.length > 0 ){

                    int cont=0;
                    // Implementación multiquery
                    for (FulfilOrdColDescDomain.FulfilOrdDescDomain.FulfilOrdDtlDomain orderDetail : orderDetailArray) {
                        if (orderDetail.getItemIdOR() != null && !orderDetail.getItemIdOR().isEmpty()){
                            Long itemIdOR = Long.parseLong(orderDetail.getItemIdOR());
                            Long storeId = optionalPrincipalStoreId.get().longValue();
                            Long itemIdMD = Long.parseLong(orderDetail.getItemIdOR().substring(2));
                            Long stockToSubtract = orderDetail.getQuantity();
                            ItemAlgoliaStock itemAlgoliaStock = getItemAlgoliaByIdStock(itemIdMD, storeId);
                            if (itemAlgoliaStock == null){
                                LOG.info("updateAlgoliaStockAndTotalStock item no encontra probando con -> " + itemIdMD);
                                itemAlgoliaStock = getItemAlgoliaByIdStock(itemIdOR, storeId);
                            }

                            itemAlgoliaStock = createStockItemAlgolia(itemAlgoliaStock, stockToSubtract);
                            arrayItemsAlgoliaStock[cont]=itemAlgoliaStock;
                            cont++;
                        }
                    }
                }
            }

        }
        updateStockItemsAlgolia(arrayItemsAlgoliaStock);

        return Optional.of(Boolean.TRUE);
    }

    /**
     *
     * @param fulfilOrdDesc
     * @return
     */
    public static List<ItemAlgolia> getItemAlgoliaByFulFilOrder(FulfilOrdColDescDomain.FulfilOrdDescDomain[] fulfilOrdDesc){

        List<ItemAlgolia> itemAlgoliaList = new ArrayList<>();

        for (FulfilOrdColDescDomain.FulfilOrdDescDomain fulfilOrdDescDomain : fulfilOrdDesc) {
            FulfilOrdColDescDomain.FulfilOrdDescDomain.FulfilOrdDtlDomain[] orderDetailArray = fulfilOrdDescDomain.getFulfilOrdDtl();

            String cityId = null;
            if (fulfilOrdDescDomain.getFulfilOrdCustDesc() != null && fulfilOrdDescDomain.getFulfilOrdCustDesc().getCityId() != null){
                cityId = fulfilOrdDescDomain.getFulfilOrdCustDesc().getCityId();
            }

            if (cityId != null){

                Optional<Integer> optionalPrincipalStoreId = getDefaultStoreByCity(cityId);

                if (optionalPrincipalStoreId.isPresent() && orderDetailArray != null && orderDetailArray.length > 0 ){

                    for (FulfilOrdColDescDomain.FulfilOrdDescDomain.FulfilOrdDtlDomain orderDetail : orderDetailArray) {

                        if (orderDetail.getItemIdOR() != null && !orderDetail.getItemIdOR().isEmpty()){
                            Long itemIdOR = Long.parseLong(orderDetail.getItemIdOR());
                            Long storeId = optionalPrincipalStoreId.get().longValue();
                            Long itemIdMD = Long.parseLong(orderDetail.getItemIdOR().substring(2));
                            ItemAlgolia itemAlgolia = getItemAlgoliaById(itemIdMD, storeId);
                            if (itemAlgolia == null){
                                LOG.info("updateAlgoliaStockAndTotalStock item no encontra probando con -> " + itemIdMD);
                                itemAlgolia = getItemAlgoliaById(itemIdOR, storeId);
                            }
                            itemAlgolia.setQuantitySold((int)orderDetail.getQuantity());

                            itemAlgoliaList.add(itemAlgolia);
                        }
                    }
                }
            }
        }

        return itemAlgoliaList;
    }


    /**
     * Update Stock in algolia when billed order
     * @param stockToSubtract
     */
    private static ItemAlgoliaStock createStockItemAlgolia(ItemAlgoliaStock itemAlgoliaStock, Long stockToSubtract) {
        if (stockToSubtract > 0){

            int newStock = (int) (itemAlgoliaStock.getStock() - stockToSubtract);
            int newTotalStock = (int) (itemAlgoliaStock.getTotalStock() - stockToSubtract);

            LOG.info("method updateStockItemAlgolia() item # " + itemAlgoliaStock.getId() + "" +
                    ", currentStock: [" + itemAlgoliaStock.getStock()  + " ] , newStock: " + newStock);

            itemAlgoliaStock.setStock(Math.max(newStock, 0));
            itemAlgoliaStock.setTotalStock(Math.max(newTotalStock, 0));

            return itemAlgoliaStock;
        }

        return itemAlgoliaStock;
    }


    private static void updateStockItemsAlgolia(ItemAlgoliaStock[] listItemsAlgoliaStock) {
        if(listItemsAlgoliaStock.length>0){
            Index<ItemAlgolia> index = algoliaClient.initIndex(URLConnections.ALGOLIA_PRODUCTS, ItemAlgolia.class);

            try {
                index.partialUpdateObjects(Arrays.asList(listItemsAlgoliaStock));
            } catch (AlgoliaException e) {
                LOG.warning("ERROR_ALGOLIA updateStockItemAlgolia: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }


    public static AlgoliaItem updateTotalStockItem(AlgoliaItem algoliaItem, Long totalStock, int storeId) {
        if ( storeId > 0 && algoliaItem != null
                && algoliaItem.getObjectID() != null
                && !algoliaItem.getObjectID().isEmpty()
                && totalStock != null && totalStock > 0 ){
            try {
                Index<AlgoliaItem> indexProducts = algoliaClient.initIndex(AlgoliaIndexEnum.PRODUCTS_PROD.getIndexName(), AlgoliaItem.class);
                algoliaItem.setTotalStock(Math.toIntExact(totalStock));
                String objectId = algoliaItem.getId() + storeId;
                //LOG.info("Object id a actualizar -> " + objectId + " para el item en la tienda: " + algoliaItem.getIdStoreGroup()+ " Total Stock: "+totalStock);
                indexProducts.partialUpdateObject(objectId,algoliaItem);
                return algoliaItem;
            } catch (Exception e) {
                LOG.warning("Error in updateTotalStockItem. Message:"+ Arrays.toString(e.getStackTrace()));
            }
        }
        return null;
    }

    public static String getItemVademecum(final String objectID, final EnableForEnum source) {
        try {
            Index<ItemSeoAlgolia> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_ITEMS_SEO, ItemSeoAlgolia.class);
            Optional<ItemSeoAlgolia> optionalResponseSeo = index.getObject(objectID);

            if (optionalResponseSeo.isEmpty()) {
                return null;
            }

            ItemSeoAlgolia itemSeoAlgolia = optionalResponseSeo.get();

            switch (source) {
                case IOS:
                case ANDROID:
                    String vademecumText = itemSeoAlgolia.getTextoVademecum();
                    Index<ItemSeoConfigAlgolia> indexStores = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, ItemSeoConfigAlgolia.class);
                    Optional<ItemSeoConfigAlgolia> itemSeoconfigOptional = indexStores.getObject(URLConnections.ALGOLIA_INDEX_CONFIG_SEO);

                    if(itemSeoconfigOptional.isEmpty()){
                        return null;
                    }
                    ItemSeoConfigAlgolia itemSeoconfig = itemSeoconfigOptional.get();

                    if ((vademecumText != null && !("").equals(vademecumText)) && (itemSeoconfig != null && !("").equals(itemSeoconfig))) {
                        byte[] decoded = Base64.decodeBase64(vademecumText.getBytes(StandardCharsets.ISO_8859_1));
                        String decodedBodySeo = new String(decoded, StandardCharsets.ISO_8859_1);
                        decodedBodySeo = StringEscapeUtils.unescapeHtml4(decodedBodySeo);
                        decodedBodySeo = toXHTML(decodedBodySeo);

                        itemSeoconfig.setHtmlVademecum(itemSeoconfig.getHtmlVademecum().replace("{CSS}", itemSeoconfig.getCssVademecum()));
                        itemSeoconfig.setHtmlVademecum(itemSeoconfig.getHtmlVademecum().replace("{SEO}", decodedBodySeo));
                        itemSeoconfig.setHtmlVademecum(itemSeoconfig.getHtmlVademecum().replace("<br />", ""));

                        String encodedString = Base64.encodeBase64String(itemSeoconfig.getHtmlVademecum().getBytes(StandardCharsets.UTF_8));

                        return encodedString;

                    }
            }
        } catch (AlgoliaException e) {
            LOG.warning("Error controlado Item Vademecum; no se mostrara ficha tecnica para el item: " + objectID + " Algolia Exception Message: " + e.getMessage());
        } catch (Exception ex) {
            LOG.warning("Error controlado Item Vademecum; no se mostrara ficha tecnica para el item: " + objectID + " Exception Message: " + ex.getMessage());
        }
        return null;
    }

        public static String getItemSeoAux(final String objectID, final EnableForEnum source){
        try {
            Index<ItemSeoAlgolia> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_ITEMS_SEO, ItemSeoAlgolia.class);
            Optional<ItemSeoAlgolia> optionalResponseSeo = index.getObject(objectID);
            if (optionalResponseSeo.isPresent()) {
                ItemSeoAlgolia responseSeo = optionalResponseSeo.get();
                switch (source) {
                    case RESPONSIVE:
                    case WEB:
                        return responseSeo.getTextoSEO();
                    case IOS:
                    case ANDROID:
                        try {
                            return getconsultingConfigSEO(responseSeo.getTextoSEO());
                            //return responseSeo.getTextoSEOApps();
                        } catch (Exception e) {
                            LOG.warning("Ocurrio un problema al consultar el texto Seo de apps");
                            return null;
                        }
                }
            }
        }catch (Exception e){
            LOG.warning("Error --> " + Arrays.toString(e.getStackTrace()));
        }
        return null;
    }

    /***
     * consulting configuration in Algolia for SEO
     * @param bodySeo
     * @return String in Base 64
     */
    public static String getconsultingConfigSEO(String bodySeo){
        try{
            Index<ItemSeoConfigAlgolia> indexStores = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES,ItemSeoConfigAlgolia.class);
            ItemSeoConfigAlgolia  itemSeoconfig = indexStores.getObject(URLConnections.ALGOLIA_INDEX_CONFIG_SEO).get();
//            LOG.info("963 inicia itemSeoconfig "+itemSeoconfig);
//            LOG.info("964 inicia bodySeo "+bodySeo);
            if((bodySeo!=null && !("").equals(bodySeo)) && (itemSeoconfig != null && !("").equals(itemSeoconfig))){
                byte[] decoded = Base64.decodeBase64(bodySeo.getBytes(StandardCharsets.ISO_8859_1));
                String decodedBodySeo=new String(decoded, StandardCharsets.ISO_8859_1);
//                LOG.info("969 decodedBodySeo "+decodedBodySeo);

                decodedBodySeo = StringEscapeUtils.unescapeHtml4(decodedBodySeo);

                decodedBodySeo = toXHTML(decodedBodySeo);

//                LOG.info("HTML_DECODE decodedBodySeo "+decodedBodySeo);

                itemSeoconfig.setHtmlSeo(itemSeoconfig.getHtmlSeo().replace("{CSSSEO}",itemSeoconfig.getCssSeo()));
                itemSeoconfig.setHtmlSeo(itemSeoconfig.getHtmlSeo().replace("{SEO}",decodedBodySeo));
//                LOG.info("971 itemSeoconfig "+itemSeoconfig.getHtmlSeo());
                String encodedString = Base64.encodeBase64String(itemSeoconfig.getHtmlSeo().getBytes(StandardCharsets.UTF_8));
//                LOG.info("973 encodedString "+encodedString);
                return encodedString;
            }else{
                return bodySeo;
            }
        }catch (Exception e) {
            LOG.warning("Error in APIAlgolia.getHomeConfig. Message:"+ Arrays.toString(e.getStackTrace()));
            e.getStackTrace();
        }
        return null;
    }

    private static String toXHTML( String html ) {
        final Document document = Jsoup.parse(html);
        document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        return document.html();
    }



    public static String getItemSeo(final String objectID, final EnableForEnum source){
        try {
            Index<ItemSeoAlgolia> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_ITEMS_SEO, ItemSeoAlgolia.class);
            ItemSeoAlgolia responseSeo = index.getObject(objectID).get();
            switch (source) {
                case ANDROID:
                case IOS:
                    if (Objects.nonNull(responseSeo))
                        return responseSeo.getTextoSEO();
                    break;
                case WEB:
                case RESPONSIVE:
                    if (Objects.nonNull(responseSeo.getTextoSEO()))
                        return responseSeo.getTextoSEO();
                    break;
            }

        }catch (Exception e){
            LOG.warning("Error ");
        }
        return null;
    }

    public static WebSocketProperties getHttpsWebSocketUrl(){
        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_HTTPS_WEBSOCKET_URL);

            if (jsonCachedOptional.isPresent()){
                //LOG.info("tiene cache getHttpsWebSocketUrl" );
                return new Gson().fromJson(jsonCachedOptional.get(),WebSocketProperties.class);
            }

            Index<WebSocketProperties> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES,WebSocketProperties.class);
            Optional<WebSocketProperties> webSocketProperties = index.getObject(URLConnections.ALGOLIA_HTTPS_WEBSOCKET_URL);
            //LOG.info("No tiene cache getHttpsWebSocketUrl" );
            webSocketProperties.ifPresent( data -> {
                String jsonToCache = new Gson().toJson(webSocketProperties.get());
                CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_HTTPS_WEBSOCKET_URL, jsonToCache);
            } );
            return webSocketProperties.get();
        }catch (Exception e){
            LOG.warning("Error in APIAlgolia.getHttpsWebSocketUrl. Message:"+ Arrays.toString(e.getStackTrace()));
            e.getStackTrace();
        }
        return null;
    }

    public static WebSocketProperties getHttpWebSocketUrl(){
        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_HTTP_WEBSOCKET_URL);

            if (jsonCachedOptional.isPresent()){
                //LOG.info("tiene cache getHttpWebSocketUrl" );
                return new Gson().fromJson(jsonCachedOptional.get(),WebSocketProperties.class);
            }

            Index<WebSocketProperties> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES,WebSocketProperties.class);
            Optional<WebSocketProperties> webSocketProperties = index.getObject(URLConnections.ALGOLIA_HTTP_WEBSOCKET_URL);
            //LOG.info("No tiene cache getHttpWebSocketUrl" );
            webSocketProperties.ifPresent( data -> {
                String jsonToCache = new Gson().toJson(webSocketProperties.get());
                CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_HTTP_WEBSOCKET_URL, jsonToCache);
            } );

            return webSocketProperties.get();
        }catch (Exception e){
            LOG.warning("Error in APIAlgolia.getHttpWebSocketUrl. Message:"+ Arrays.toString(e.getStackTrace()));
            e.getStackTrace();
        }
        return null;
    }

    public static List<SupportNumber> getSupportNumbers(){
        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_SUPPORT_NUMBERS);
            if (jsonCachedOptional.isPresent()){
                Type listType = new TypeToken<List<SupportNumber>>(){}.getType();
                return new Gson().fromJson(jsonCachedOptional.get(), listType);
            }

            Index<SupportNumber> indexSuppportNumbers = algoliaClient.initIndex(URLConnections.ALGOLIA_SUPPORT_NUMBERS, SupportNumber.class);
            IndexIterable<SupportNumber> items = indexSuppportNumbers.browse(new Query().setFilters(""));
            List<SupportNumber> supportNumbers = Lists.newArrayList(items);

            String jsonToCache = new Gson().toJson(supportNumbers);
            CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_SUPPORT_NUMBERS, jsonToCache);
            return supportNumbers;
        } catch (Exception e) {
            LOG.warning("Error in APIAlgolia.getItemAlgolia. Message:"+ Arrays.toString(e.getStackTrace()));
            return null;
        }
    }

    public static boolean deleteImagesItemAlgolia(final String idItem) {
        List<ItemAlgolia> result = null;
        try {
            Index<ItemAlgolia> index = algoliaClient.initIndex(URLConnections.ALGOLIA_PRODUCTS, ItemAlgolia.class);
            Query query = new Query().setFilters(String.format("id:'%s'", idItem));
            IndexIterable<ItemAlgolia> indexIterable = index.browse(query);
            if(Objects.nonNull(indexIterable) || Objects.nonNull(indexIterable.stream())) {
                result = indexIterable.stream().filter(item -> Objects.nonNull(item.getIdStoreGroup())).sorted(ORDER_ITEM_BY_STORE_ID).collect(Collectors.toList());
                List<Object> listItemsUpdate = new ArrayList<>();
                if(Objects.nonNull(result) || result.isEmpty()) {
                    for (ItemAlgolia item : result) {
                        ItemImages itemImage = new ItemImages(item.getObjectID(), null);
                        listItemsUpdate.add(itemImage);
                        //LOG.info("method deleteImagesItemAlgolia Imagen borradas: " + itemImage.getObjectID());
                    }
                    index.partialUpdateObjects(listItemsUpdate);
                    return true;
                }else {
                    return false;
                }
            }else {
                return false;
            }
        } catch (AlgoliaException e) {
            LOG.warning("Error in APIAlgolia.getItemByObjectID. Message:" + Arrays.toString(e.getStackTrace()));
            LOG.warning("Error Message:" + e.getMessage());
            return false;
        }
    }

    public static boolean updateHighlightsProducts(RequestUpdateHighlightSuggested requestUpdateHighlightSuggested) {
        List<ItemAlgolia> result = null;
        boolean res = false;
        for (ItemUpdateHighlightSuggested item: requestUpdateHighlightSuggested.getItems()) {
            try {
                Index<ItemAlgolia> index = algoliaClient.initIndex(URLConnections.ALGOLIA_PRODUCTS, ItemAlgolia.class);
                //SearchResult<ItemAlgolia> search = index.search(new Query("").setFilters("objectID:'" + item.getItem() + URLConnections.MAIN_ID_STORE + "'"));
                List<IndexQuery> queries = new ArrayList<>();
                queries.add(new IndexQuery(URLConnections.ALGOLIA_PRODUCTS, new Query("").setFilters("objectID:'" + item.getItem() + URLConnections.MAIN_ID_STORE + "'")));
                List<SearchResult<?>> results = algoliaClient.multipleQueries(queries).getResults();
                LOG.info("item found: " + results.get(0).getHits());
                List<ItemAlgolia> itemAlgoliaList = getItemListAlgoliaFromMultiqueriesHits(results.get(0));
                //if (search.getHits() != null && !search.getHits().isEmpty() && search.getHits().get(0) != null) {
                if (itemAlgoliaList != null && !itemAlgoliaList.isEmpty() && itemAlgoliaList.get(0) != null) {
                    //for (ItemAlgolia itemAlgolia: search.getHits()) {
                    for (ItemAlgolia itemAlgolia: itemAlgoliaList) {
                        LOG.info("set Highlight: idItem: " + itemAlgolia.getObjectID() + ", groups: " + item.getIdGroups());
                        itemAlgolia.setId_highlights(item.getIdGroups());
                        index.partialUpdateObject(itemAlgolia.getObjectID(), itemAlgolia);
                        res = true;
                    }
                }
            } catch (AlgoliaException e) {
                LOG.warning("Error in APIAlgolia.getItemByObjectID. Message:" + Arrays.toString(e.getStackTrace()));
                LOG.warning("Error Message:" + e.getMessage());
                res = false;
            }
        }
        return res;
    }

    public static boolean updateSuggestedProducts(RequestUpdateHighlightSuggested requestUpdateHighlightSuggested) {
        List<ItemAlgolia> result = null;
        boolean res = false;
        for (ItemUpdateHighlightSuggested item: requestUpdateHighlightSuggested.getItems()) {
            try {
                Index<ItemAlgolia> index = algoliaClient.initIndex(URLConnections.ALGOLIA_PRODUCTS, ItemAlgolia.class);
                List<IndexQuery> queries = new ArrayList<>();
                queries.add(new IndexQuery(URLConnections.ALGOLIA_PRODUCTS,new Query("").setFilters("objectID:'" + item.getItem() + URLConnections.MAIN_ID_STORE + "'") ));
                //SearchResult<ItemAlgolia> search = index.search(new Query("").setFilters("objectID:'" + item.getItem() + URLConnections.MAIN_ID_STORE + "'"));
                List<SearchResult<?>> results = algoliaClient.multipleQueries(queries).getResults();
                List<ItemAlgolia> itemAlgoliaList = getItemListAlgoliaFromMultiqueriesHits(results.get(0));
                //LOG.info("item found: " + search.getHits());
                //if (search.getHits() != null && !search.getHits().isEmpty() && search.getHits().get(0) != null) {
                if (itemAlgoliaList != null && !itemAlgoliaList.isEmpty() && itemAlgoliaList.get(0) != null) {
                    //for (ItemAlgolia itemAlgolia: search.getHits()) {
                    for (ItemAlgolia itemAlgolia: itemAlgoliaList) {
                        LOG.info("set Suggested: idItem: " + itemAlgolia.getObjectID() + ", groups: " + item.getIdGroups());
                        itemAlgolia.setId_suggested(item.getIdGroups());
                        index.partialUpdateObject(itemAlgolia.getObjectID(), itemAlgolia);
                        res = true;
                    }
                }
            } catch (AlgoliaException e) {
                LOG.warning("Error in APIAlgolia.getItemByObjectID. Message:" + Arrays.toString(e.getStackTrace()));
                LOG.warning("Error Message:" + e.getMessage());
                return false;
            }
        }
        return res;
    }

    public static boolean uploadOffer(RequestManageOffer requestManageOffer) {
        try {
            Index<PropertiesStoresByCity> indexStores = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES,PropertiesStoresByCity.class);
            PropertiesStoresByCity propertiesStoresByCity = indexStores.getObject(URLConnections.ALGOLIA_DEFAULT_STORES_BY_CITY).get();
            for (Integer item : requestManageOffer.getItems()) {
                for (PropertyCityStore propertyCityStore : propertiesStoresByCity.getDefaultStores()) {
                    Index<ItemAlgolia> index = algoliaClient.initIndex(URLConnections.ALGOLIA_PRODUCTS, ItemAlgolia.class);
                    ItemAlgolia itemAlgolia = index.getObject(item + propertyCityStore.getDefaultStore()).get();
                    LOG.info("set Offer: idItem: " + itemAlgolia.getObjectID() + ", Store: " + propertyCityStore.getDefaultStore());
                    itemAlgolia.setOfferDescription(requestManageOffer.getOfferDescription());
                    itemAlgolia.setOfferText(requestManageOffer.getOfferText());
                    itemAlgolia.setOfferPrice(requestManageOffer.getOfferPrice());
                    index.partialUpdateObject(itemAlgolia.getObjectID(), itemAlgolia);
                }
            }
        }catch (AlgoliaException e) {
            LOG.warning("Error in APIAlgolia.getItemByObjectID. Message:" + Arrays.toString(e.getStackTrace()));
            LOG.warning("Error Message:" + e.getMessage());
            return false;
        }
        return true;
    }

    public static boolean deleteOffer(RequestManageOffer requestManageOffer) {
        try {
            Index<PropertiesStoresByCity> indexStores = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES,PropertiesStoresByCity.class);
            PropertiesStoresByCity propertiesStoresByCity = indexStores.getObject(URLConnections.ALGOLIA_DEFAULT_STORES_BY_CITY).get();
            for (Integer item : requestManageOffer.getItems()) {
                for (PropertyCityStore propertyCityStore : propertiesStoresByCity.getDefaultStores()) {
                    Index<ItemAlgolia> index = algoliaClient.initIndex(URLConnections.ALGOLIA_PRODUCTS, ItemAlgolia.class);
                    ItemAlgolia itemAlgolia = index.getObject(item + propertyCityStore.getDefaultStore()).get();
                    LOG.info("delete Offer: idItem: " + itemAlgolia.getObjectID() + ", Store: " + propertyCityStore.getDefaultStore());
                    itemAlgolia.setOfferDescription(null);
                    itemAlgolia.setOfferText(null);
                    itemAlgolia.setOfferPrice(0D);
                    index.partialUpdateObject(itemAlgolia.getObjectID(), itemAlgolia);
                }
            }
        }catch (AlgoliaException e) {
            LOG.warning("Error in APIAlgolia.getItemByObjectID. Message:" + Arrays.toString(e.getStackTrace()));
            LOG.warning("Error Message:" + e.getMessage());
            return false;
        }
        return true;
    }

    public static Optional<Integer> getDefaultStoreByCity(final String cityId)  {

        if (cityId == null || cityId.isEmpty()){
            return Optional.empty();
        }

        Index<PropertiesStoresById> indexStores = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES,PropertiesStoresById.class);
        Optional<PropertiesStoresById> optionalPropertiesStoresById = Optional.empty();
        try {
            optionalPropertiesStoresById = indexStores.getObject(URLConnections.ALGOLIA_DEFAULT_STORES_BY_ID);
        } catch (AlgoliaException e) {
            e.printStackTrace();
        }

        if (!optionalPropertiesStoresById.isPresent()){
            return Optional.empty();
        }

        PropertiesStoresById propertiesStoresById = optionalPropertiesStoresById.get();

        Optional<PropertyStoreById> optionalStoreFind = propertiesStoresById
                .getStores()
                .stream()
                .filter( store -> store.getStoreCity().equals(cityId) )
                .findFirst();

        if (optionalStoreFind.isPresent()){

            Integer defaultStore = optionalStoreFind.get().getDefaultStore();

            return Optional.of(defaultStore);
        }

        return Optional.empty();

    }

    /**
     * get store id
     * @param storeId
     * @return
     * @throws AlgoliaException
     */
    public static int getDefaultStoreIdByStoreId(final int storeId) throws AlgoliaException {
        PropertiesStoresById propertiesStoresById = null;
        Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_DEFAULT_STORES_BY_ID);
        if (jsonCachedOptional.isPresent()){
            //LOG.info("Tiene cache: getDefaultStoreIdByStoreId");
            propertiesStoresById= new Gson().fromJson(jsonCachedOptional.get(),PropertiesStoresById.class);
        }else{
            Index<PropertiesStoresById> indexStores = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, PropertiesStoresById.class);
            propertiesStoresById = indexStores.getObject(URLConnections.ALGOLIA_DEFAULT_STORES_BY_ID).get();
            //LOG.info("No tiene cache: getDefaultStoreIdByStoreId");
            String jsonToCache = new Gson().toJson(propertiesStoresById);
            CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_DEFAULT_STORES_BY_ID, jsonToCache);
        }
        final PropertyStoreById propertyStoreById = new PropertyStoreById();
        propertyStoreById.setDefaultStore(Integer.parseInt(Constants.MAIN_ID_STORE));
        propertiesStoresById.getStores().forEach(storeAlgolia -> {
            if (storeId == storeAlgolia.getIdStore()) {
                int defaultStore = storeAlgolia.getDefaultStore();
                propertyStoreById.setDefaultStore(defaultStore);
            }
        });
        return propertyStoreById.getDefaultStore();
    }

    public static String getStoreCityByStoreId(final int storeId) throws AlgoliaException {
//        LOG.warning("getDefaultStoreIdByStoreId storeId: "+storeId);
        Index<PropertiesStoresById> indexStores = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES,PropertiesStoresById.class);
        PropertiesStoresById propertiesStoresById = indexStores.getObject(URLConnections.ALGOLIA_DEFAULT_STORES_BY_ID).orElse(null);

        final PropertyStoreById propertyStoreById = new PropertyStoreById();
        propertyStoreById.setStoreCity("BOG");
        if(Objects.nonNull(propertiesStoresById) && Objects.nonNull(propertiesStoresById.getStores()) && !propertiesStoresById.getStores().isEmpty()) {
            final int size = propertiesStoresById.getStores().size();
            for(int i = 0; i < size; i ++){
                PropertyStoreById storeAlgolia = propertiesStoresById.getStores().get(i);
                if (storeId == storeAlgolia.getIdStore()) {
                    String storeCity = storeAlgolia.getStoreCity();
                    propertyStoreById.setStoreCity(storeCity);
                }
            }
        }
        return propertyStoreById.getStoreCity();
    }

    /**
     * Obtener la configiracion de tiempos de envio de Algolia.
     * */
    public static List<OriginProperties> getOriginProperties() {
        try {
            String filters = "type:'" + Constants.ORIGIN_LIST + "'";
            List<IndexQuery> queries = new ArrayList<>();
            queries.add(new IndexQuery(URLConnections.ALGOLIA_INDEX_PROPERTIES, new Query("").setFilters(filters)));
            return getOriginPropertiesFromMultiqueries(algoliaClient.multipleQueries(queries).getResults().get(0));
        } catch (AlgoliaException e) {
            LOG.warning("Error in APIAlgolia.getOriginProperties. Message:" + Arrays.toString(e.getStackTrace()));
            return null;
        }
    }

    public static List<ItemAlgolia> getItemsFromIds(List<String> listItems) {
        List<ItemAlgolia> listItemsAlgolia;
        try {
            Index<ItemAlgolia> index = algoliaClient.initIndex(URLConnections.ALGOLIA_PRODUCTS, ItemAlgolia.class);
            listItemsAlgolia = index.getObjects(listItems);
        } catch (AlgoliaException e) {
            LOG.warning("Error in APIAlgolia.getOriginProperties. Message:"+ Arrays.toString(e.getStackTrace()));
            return null;
        }
        return listItemsAlgolia;
     }

    public static List<ItemAlgolia> findItemByIdList(List<ItemQuery> listItemQuery) {
        //return findItemByIdList(listItemQuery, false);
        // Refactor de código usando objectId y getObject
        return findItemByIdListV2(listItemQuery);
    }

    public static List<ItemAlgolia> findItemByIdList(List<ItemQuery> listItemQuery, Boolean subscribeAndSave) {
        try {
            boolean res = false; // TODO: PREGUNTAR SI SE PUEDE BORRAR
            //Index<ItemAlgolia> index = algoliaClient.initIndex(URLConnections.ALGOLIA_PRODUCTS, ItemAlgolia.class);
            String filter = listItemQuery.stream().map(item -> "objectID:" + item.getItemStore() + "").collect(Collectors.joining(" OR "));
            if (subscribeAndSave) {
                filter = "( " + filter + " ) AND  subscribeAndSave:" + subscribeAndSave;
            }
            List<IndexQuery> queries = new ArrayList<>();
//            SearchResult<ItemAlgolia> search = index.search(new Query("").setFilters(filter).setHitsPerPage(Constants.HITS_PER_PAGE));
            queries.add(new IndexQuery(URLConnections.ALGOLIA_PRODUCTS,new Query("").setFilters(filter).setHitsPerPage(Constants.HITS_PER_PAGE)));
            List<SearchResult<?>> resultsMultiquery = algoliaClient.multipleQueries(queries).getResults();
//            List<ItemAlgolia> result = search.getHits().stream().filter(item -> Objects.nonNull(item.getId()) && !item.getId().isEmpty()).collect(Collectors.toList());ç
            List<ItemAlgolia> result = getItemListAlgoliaFromMultiqueriesHits(resultsMultiquery.get(0)).stream().filter(item -> Objects.nonNull(item.getId()) && !item.getId().isEmpty()).collect(Collectors.toList());
            Comparator<ItemAlgolia> comparator = nullsLast((ItemAlgolia item, ItemAlgolia itemD) -> itemD.getSales().compareTo(item.getSales()));
            Predicate<ItemAlgolia> predicate = e -> e.getSales() != null;

            return result.stream()
                    .filter(predicate)
                    .sorted(comparator)
                    .collect(Collectors.toList());
        } catch (AlgoliaException e) {
            LOG.warning("Error in APIAlgolia.findItemByIdList. Message:" + Arrays.toString(e.getStackTrace()));
            LOG.warning("Error Message:" + e.getMessage());
            return null;
        }
    }

    public static List<SuggestedObject> getItemAlgoliaByIdSuggested(final List<SuggestedObject> suggestedList, int idStoreGroup) {
        try {
//            LOG.warning("method getItemAlgoliaByIdSuggested -> " + suggestedList + " storeid- > " + idStoreGroup);
            Index<ItemAlgolia> index = algoliaClient.initIndex(URLConnections.ALGOLIA_PRODUCTS, ItemAlgolia.class);
            String filter = suggestedList.isEmpty() ? "" : suggestedList.stream().map(suggested -> "id_suggested:'" + suggested.getId() + "'").collect(Collectors.joining(" OR ")) + " AND idStoreGroup:'"+idStoreGroup+"'";
//            LOG.warning("method getItemAlgoliaByIdSuggested -> Filter:" + filter);

            SearchResult<ItemAlgolia> search  = index.search(new Query("").setFilters(filter).setHitsPerPage(100));
//            LOG.warning("method getItemAlgoliaByIdSuggested -> Result: " + search.getHits().size());
            if (search.getHits() != null && !search.getHits().isEmpty()){
                suggestedList.stream().forEach(suggested ->{
                    search.getHits().stream().forEach(item -> {
                        //LOG.warning("method getItemAlgoliaByIdSuggested -> Compare Result: ");
                        if(Objects.nonNull(item.getId_suggested()) && item.getId_suggested().stream()
                                .filter(itemSuggested -> Objects.nonNull(itemSuggested) && Objects.nonNull(suggested) &&
                                        Objects.nonNull(suggested.getId()) &&
                                        itemSuggested.equals(suggested.getId())).findFirst().isPresent()){
//                            LOG.warning("method getItemAlgoliaByIdSuggested -> Compare Result: "+item);
                            List<Item> unique = new ArrayList<>();
                            if (Objects.nonNull(item.getStock()) && item.getStock() > 0) {
                                unique.add(getItemToItemAlgolia(new Item(), item));
                                suggested.setProduct(unique);
                            }
                        }
                    });
                });
                return suggestedList;
            }
            return null;
        } catch (AlgoliaException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static NextPaymentAttemptProperties getNextPaymentAttempt(){
        try {

            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(Constants.NEXT_PAYMENT_ATTEMPT);

            if (jsonCachedOptional.isPresent()){
                //LOG.info("tiene cache getNextPaymentAttempt" );
                return new Gson().fromJson(jsonCachedOptional.get(),NextPaymentAttemptProperties.class);
            }

            Index<NextPaymentAttemptProperties> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, NextPaymentAttemptProperties.class);
            Optional<NextPaymentAttemptProperties> nextPaymentAttemptProperties = index.getObject(Constants.NEXT_PAYMENT_ATTEMPT);
            //LOG.info("No tiene cache getNextPaymentAttempt" );
            nextPaymentAttemptProperties.ifPresent( data -> {
                String jsonToCache = new Gson().toJson(nextPaymentAttemptProperties.get());
                CachedDataManager.saveJsonInCache(Constants.NEXT_PAYMENT_ATTEMPT, jsonToCache);
            } );

            return nextPaymentAttemptProperties.get();
        }catch (Exception e){
            LOG.warning("Error in APIAlgolia.getNextPaymentAttempt. Message:"+ Arrays.toString(e.getStackTrace()));
            e.getStackTrace();
        }
        return null;
    }

    public static <T> T getProperty(String key, Class<T> propertyClass){
        try {
            Index<T> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, propertyClass);
            return index.getObject(key).get();
        }catch (Exception e){
            LOG.warning("Error in APIAlgolia.getProperty. Message:"+ Arrays.toString(e.getStackTrace()));
            e.getStackTrace();
        }
        return null;
    }

    public static boolean updateDescriptionItemAlgolia(final long itemId, final String grayDescription, final String mediaDescription, final String largeDescription){
        try {
            LOG.warning("method updateDescriptionItemAlgolia INIT ");
            Index<ItemAlgolia> indexProducts = algoliaClient.initIndex(URLConnections.ALGOLIA_PRODUCTS, ItemAlgolia.class);
            List<Object> items = Constants.MAIN_STORES.stream().map(storeId ->  new ItemDescriptions(itemId+storeId, grayDescription, mediaDescription, largeDescription)).collect(Collectors.toList());
            if(Objects.nonNull(items) && !items.isEmpty()) {
                //LOG.warning("Listado de items"+ items);
                TaskSingleIndex result = indexProducts.partialUpdateObjects(items, false, new RequestOptions());
                LOG.warning("method updateDescriptionItemAlgolia END  "+result.getObjectIDs());
                return !result.getObjectIDs().isEmpty();
            }
            return false;
        } catch (AlgoliaException e) {
            LOG.warning("Error in APIAlgolia.updateDescriptionItemAlgolia. Message:"+ Arrays.toString(e.getStackTrace()));
            return false;
        }
    }

    public static boolean addHighlightOrSuggestItemAlgolia(final long itemId, final long addId, boolean highlight){
        try {
            LOG.warning("method addHighlightOrSuggestItemAlgolia INIT ");
            Index<ItemAlgolia> indexProducts = algoliaClient.initIndex(URLConnections.ALGOLIA_PRODUCTS, ItemAlgolia.class);
            String objectId = itemId + URLConnections.MAIN_ID_STORE;
            Optional<ItemAlgolia> item = indexProducts.getObject(objectId);

            if(item.isPresent()) {
                ItemAlgolia itemAlgolia = item.get();
                List<Object> items = null;
                LOG.warning("ORIGINAL ITEM: "+ itemAlgolia);
                if (highlight) {
                    if (Objects.isNull(itemAlgolia.getId_highlights())) {
                        itemAlgolia.setId_highlights(new ArrayList<>());
                    }
                    itemAlgolia.getId_highlights().add("" + addId);
                    items = Constants.MAIN_STORES.stream().map(storeId -> new ItemHighlight(itemId+storeId, itemAlgolia.getId_highlights())).collect(Collectors.toList());
                } else {
                    if (Objects.isNull(itemAlgolia.getId_suggested())) {
                        itemAlgolia.setId_suggested(new ArrayList<>());
                    }
                    itemAlgolia.getId_suggested().add("" + addId);
                    items = Constants.MAIN_STORES.stream().map(storeId -> new ItemSuggested(itemId+storeId, itemAlgolia.getId_suggested() )).collect(Collectors.toList());
                }
                if(Objects.nonNull(items) && !items.isEmpty()) {
                    LOG.warning("Listado de items"+ items);
                    TaskSingleIndex result = indexProducts.partialUpdateObjects(items, false, new RequestOptions());
                    LOG.warning("method addHighlightOrSuggestItemAlgolia END  "+result.getObjectIDs());
                    return !result.getObjectIDs().isEmpty();
                }
            }
            return false;
        } catch (AlgoliaException e) {
            LOG.warning("Error in APIAlgolia.addHighlightOrSuggestItemAlgolia. Message:"+ Arrays.toString(e.getStackTrace()));
            return false;
        }
    }

    public static boolean deleteHighlightOrSuggestItemAlgolia(final long itemId, final long removeId, boolean highlight){
        try {
            LOG.warning("method deleteHighlightOrSuggestItemAlgolia INIT ");
            Index<ItemAlgolia> indexProducts = algoliaClient.initIndex(URLConnections.ALGOLIA_PRODUCTS, ItemAlgolia.class);
            String objectId = itemId + URLConnections.MAIN_ID_STORE;
            Optional<ItemAlgolia> item = indexProducts.getObject(objectId);

            if(item.isPresent()) {
                ItemAlgolia itemAlgolia = item.get();
                List<Object> items = null;
                LOG.warning("ORIGINAL ITEM: "+ itemAlgolia);
                if (highlight) {
                    if (Objects.isNull(itemAlgolia.getId_highlights())) {
                        itemAlgolia.setId_highlights(new ArrayList<>());
                    }
                    itemAlgolia.getId_highlights().removeIf(group -> Long.parseLong(group) == removeId);
                    items = Constants.MAIN_STORES.stream().map(storeId -> new ItemHighlight(itemId+storeId, itemAlgolia.getId_highlights())).collect(Collectors.toList());
                } else {
                    if (Objects.isNull(itemAlgolia.getId_suggested())) {
                        itemAlgolia.setId_suggested(new ArrayList<>());
                    }
                    itemAlgolia.getId_suggested().removeIf(group -> Long.parseLong(group) == removeId);
                    items = Constants.MAIN_STORES.stream().map(storeId -> new ItemSuggested(itemId+storeId, itemAlgolia.getId_suggested() )).collect(Collectors.toList());
                }
                if(Objects.nonNull(items) && !items.isEmpty()) {
                    LOG.warning("Listado de items"+ items);
                    TaskSingleIndex result = indexProducts.partialUpdateObjects(items, false, new RequestOptions());
                    LOG.warning("method deleteHighlightOrSuggestItemAlgolia END  "+result.getObjectIDs());
                    return !result.getObjectIDs().isEmpty();
                }
            }
            return false;
        } catch (AlgoliaException e) {
            LOG.warning("Error in APIAlgolia.deleteHighlightOrSuggestItemAlgolia. Message:"+ Arrays.toString(e.getStackTrace()));
            return false;
        }
    }

    public static boolean updateOfferItemAlgolia(final com.imaginamos.farmatodo.model.product.ItemOffer itemOffer){
        try {
            LOG.warning("method updateOfferItemAlgolia INIT ");
            Index<ItemAlgolia> indexProducts = algoliaClient.initIndex(URLConnections.ALGOLIA_PRODUCTS, ItemAlgolia.class);
            List<Object> items = Constants.MAIN_STORES.stream().map(storeId ->  new com.imaginamos.farmatodo.model.product.ItemOffer(itemOffer.getItemId()+storeId, itemOffer.getOfferPrice(), itemOffer.getOfferDescription(), itemOffer.getOfferText())).collect(Collectors.toList());
            if(Objects.nonNull(items) && !items.isEmpty()) {
                //LOG.warning("Listado de items"+ items);
                TaskSingleIndex result = indexProducts.partialUpdateObjects(items, false, new RequestOptions());
                LOG.warning("method updateOfferItemAlgolia END  "+result.getObjectIDs());
                return !result.getObjectIDs().isEmpty();
            }
            return false;
        } catch (AlgoliaException e) {
            LOG.warning("Error in APIAlgolia.updateOfferItemAlgolia. Message:"+ Arrays.toString(e.getStackTrace()));
            return false;
        }
    }

    public static StoresAlgolia getStoresAlgolia() throws AlgoliaException {
        Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_STORES);

        if (jsonCachedOptional.isPresent()){
            //LOG.info("tiene cache getStoresAlgolia" );
            return new Gson().fromJson(jsonCachedOptional.get(),StoresAlgolia.class);
        }

        Index<StoresAlgolia> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, StoresAlgolia.class);
        Optional<StoresAlgolia> stores = index.getObject(URLConnections.ALGOLIA_STORES);
        //LOG.info("No tiene cache getStoresAlgolia" );
        //LOG.info("Stores Algolia found -> " + stores.get().getStores().size());
        stores.ifPresent( data -> {
            String jsonToCache = new Gson().toJson(stores.get());
            CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_STORES, jsonToCache);
        } );

        return stores.get();
    }

    public static Optional<Map<String, String>> getRejectReasonCouponTalonOne(){
        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(Constants.REJECTION_REASON_COUPON_TALON);
            if (jsonCachedOptional.isPresent()){
                return Optional.of(new Gson().fromJson(jsonCachedOptional.get(), RejectReasonCouponTalon.class).getRejectionReason());
            }

            Index<RejectReasonCouponTalon> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, RejectReasonCouponTalon.class);
            Optional<RejectReasonCouponTalon> rejectReason = index.getObject(Constants.REJECTION_REASON_COUPON_TALON);
            if(rejectReason.isPresent()){
                String jsonToCache = new Gson().toJson(rejectReason.get());
                CachedDataManager.saveJsonInCache(Constants.REJECTION_REASON_COUPON_TALON, jsonToCache);
                return Optional.of(rejectReason.get().getRejectionReason());
            }else return Optional.empty();
        }catch(Exception e){
            LOG.warning("Error obteniendo información de razones de rechazo -> " +e.getMessage());
            return Optional.empty();
        }
    }

    public static PropertiesBaseSAGAlgolia getSagBasePropertiesAlgolia() throws AlgoliaException {
        Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_SAG_BASE_PROPERTIES);

        if (jsonCachedOptional.isPresent()){
            //LOG.info("tiene cache getSagBasePropertiesAlgolia" );
            return new Gson().fromJson(jsonCachedOptional.get(),PropertiesBaseSAGAlgolia.class);
        }
        Index<PropertiesBaseSAGAlgolia> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, PropertiesBaseSAGAlgolia.class);
        Optional<PropertiesBaseSAGAlgolia> propertiesBaseSAGAlgolia = index.getObject(URLConnections.ALGOLIA_SAG_BASE_PROPERTIES);
        //LOG.info("No tiene cache getSagBasePropertiesAlgolia" );
        propertiesBaseSAGAlgolia.ifPresent( data -> {
            String jsonToCache = new Gson().toJson(propertiesBaseSAGAlgolia.get());
            CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_SAG_BASE_PROPERTIES, jsonToCache);
        } );

        return propertiesBaseSAGAlgolia.get();
    }

    public static ExcludeStoresCreateOrder getStoresToExcludeCreateOrder() throws AlgoliaException {
        Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_EXCLUDE_STORES_CREATE_ORDER);

        if (jsonCachedOptional.isPresent()){
            //LOG.info("tiene cache getStoresToExcludeCreateOrder" );
            return new Gson().fromJson(jsonCachedOptional.get(),ExcludeStoresCreateOrder.class);
        }

        Index<ExcludeStoresCreateOrder> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, ExcludeStoresCreateOrder.class);
        Optional<ExcludeStoresCreateOrder> optionalExcludeStores = index.getObject(URLConnections.ALGOLIA_EXCLUDE_STORES_CREATE_ORDER);
        //LOG.info("No tiene cache getStoresToExcludeCreateOrder" );
        optionalExcludeStores.ifPresent( data -> {
            String jsonToCache = new Gson().toJson(optionalExcludeStores.get());
            CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_EXCLUDE_STORES_CREATE_ORDER, jsonToCache);
        } );

        if (optionalExcludeStores.isPresent()){
            //LOG.info("method: getStoresToExcludeCreateOrder() -> " + optionalExcludeStores.get().toString());
            return optionalExcludeStores.get();
        }
        return new ExcludeStoresCreateOrder();

    }

    public static CategoriaAlgolia getCategoryNameById(int categoryId) throws AlgoliaException {
        String filters = "id:'" + categoryId + "'";
        String cacheKey = URLConnections.ALGOLIA_INDEX_ALL_CATEGORIES + filters;

        Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(cacheKey);
        if (jsonCachedOptional.isPresent()){
            return new Gson().fromJson(jsonCachedOptional.get(), CategoriaAlgolia.class);
        }

        List<IndexQuery> queries = new ArrayList<>();
        queries.add(new IndexQuery(URLConnections.ALGOLIA_INDEX_ALL_CATEGORIES, new Query("").setFilters(filters)));
        List<CategoriaAlgolia> result = getCategoriaAlgoliaFromMultiqueries(algoliaClient.multipleQueries(queries).getResults().get(0));

        if (result != null && !result.isEmpty()){
            String jsonToCache = new Gson().toJson(result.get(0));
            CachedDataManager.saveJsonInCache(cacheKey, jsonToCache);
            return result.get(0);
        }

        return null;
    }


    public static AlertConfigMessage getAlertMessage() throws AlgoliaException {
        Index<AlertConfigMessage> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_ALERTS_OPERATION_CONFIG,AlertConfigMessage.class);
        AlertConfigMessage alertConfigMessage = index.getObject(URLConnections.ALGOLIA_PHONE_NUMBERS_FOR_ALERT).get();
        //LOG.info("AlertConfigMessage -> " + alertConfigMessage.toString());
        return alertConfigMessage;
    }

    public static AlertConfigMessage getAlertMessageErrorOms() throws AlgoliaException {
        Index<AlertConfigMessage> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_ALERTS_OPERATION_CONFIG,AlertConfigMessage.class);
        AlertConfigMessage alertConfigMessage = index.getObject(URLConnections.ALGOLIA_PHONE_NUMBERS_FOR_ALERT_OMS).get();
        //LOG.info("AlertConfigMessage -> " + alertConfigMessage.toString());
        return alertConfigMessage;
    }

    public static AlertConfigMessage getAlertMessageErrorPayu() throws AlgoliaException {
        Index<AlertConfigMessage> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_ALERTS_OPERATION_CONFIG,AlertConfigMessage.class);
        AlertConfigMessage alertConfigMessage = index.getObject(URLConnections.ALGOLIA_PHONE_NUMBERS_FOR_ALERT_PAYU).get();
        //LOG.info("AlertConfigMessage -> " + alertConfigMessage.toString());
        return alertConfigMessage;
    }

    public static List<Suggested> getCrossSaleByItem(long itemId) throws AlgoliaException {

        Index<CrossSaleAndSubstituteAlgolia> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_CROSS_SALES_AND_SUBSTITUTES, CrossSaleAndSubstituteAlgolia.class);
        String objectId = "CROSS.SALES."+itemId;
        return getSuggesteds(index, objectId);

    }


    public static List<Suggested> getSubstitutesByItem(long itemId) throws AlgoliaException {

        Index<CrossSaleAndSubstituteAlgolia> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_CROSS_SALES_AND_SUBSTITUTES, CrossSaleAndSubstituteAlgolia.class);
        String objectId = "SUBSTITUTE."+itemId;
        return getSuggesteds(index, objectId);

    }

    @NotNull
    private static List<Suggested> getSuggesteds(Index<CrossSaleAndSubstituteAlgolia> index, String objectId) throws AlgoliaException {
        Optional<CrossSaleAndSubstituteAlgolia>  algoliaObj = index.getObject(objectId);
        List<Suggested> suggestedList = new ArrayList<>();
        if (algoliaObj.isPresent()){
            CrossSaleAndSubstituteAlgolia crossSale = algoliaObj.get();

            if (!crossSale.getSuggested().isEmpty()){
                for (int i=0; i < crossSale.getSuggested().size(); i++){
                    Suggested suggested = new Suggested();
                    suggested.setItem(crossSale.getSuggested().get(i).longValue());
                    suggestedList.add(suggested);
                }
            }
        }
        return suggestedList;
    }

    /**
     * Crea o actualiza un highlight
     * @param highlight
     * @return
     */
    public static Boolean handlerHighlight(Highlight highlight) {
        try {
            LOG.warning("method highlight INIT ");
            Index<Highlight> indexHighlight = algoliaClient.initIndex(URLConnections.ALGOLIA_PRODUCTS, Highlight.class);
            Optional<Highlight> highlightAlgolia = indexHighlight.getObject(highlight.getObjectID());
            if (highlightAlgolia.isPresent()) {
                Highlight highlightFound = highlightAlgolia.get();
                highlightFound.setStartDate(highlight.getStartDate());
                highlightFound.setEndDate(highlight.getEndDate());
                highlightFound.setCategories(highlight.getCategories());
                highlightFound.setFirstDescription(highlight.getFirstDescription());
                highlightFound.setSecondDescription(highlight.getSecondDescription());
                highlightFound.setOfferDescription(highlight.getOfferDescription());
                highlightFound.setOrderingNumber(highlight.getOrderingNumber());
                highlightFound.setUrlImage(highlight.getUrlImage());
                highlightFound.setType(Objects.nonNull(highlight.getType()) ? highlight.getType() : highlightFound.getType());
                highlightFound.setHighlightHead(Boolean.TRUE);
                highlightFound.setItems(highlight.getItems());
                highlightFound.setOfferText(highlight.getOfferText());
                indexHighlight.partialUpdateObject(highlightFound.getObjectID(), highlightFound);
                return true;
            } else {
                indexHighlight.saveObject(highlight.getObjectID(), highlight);
                return true;
            }
        } catch (AlgoliaException e) {
            LOG.warning("Error in APIAlgolia.highlight. Message:" + Arrays.toString(e.getStackTrace()));
            return false;
        }
    }

    public static Boolean deleteHighlight(String objectId){
        try {
            LOG.warning("method deleteHighlight INIT "+objectId);
            Index<Highlight> indexHighlight= algoliaClient.initIndex(URLConnections.ALGOLIA_PRODUCTS, Highlight.class);
            indexHighlight.deleteObject(objectId);
            LOG.warning("method deleteHighlight END "+objectId);
            return true;
        } catch (AlgoliaException e) {
            LOG.warning("Error in APIAlgolia.highlight. Message:"+ Arrays.toString(e.getStackTrace()));
            return false;
        }
    }

    public static Boolean addHighlightList(List<Highlight> highlightList){
        try {
            LOG.warning("method addHighlightList INIT ");
            Index<Highlight> indexHighlight= algoliaClient.initIndex(URLConnections.ALGOLIA_PRODUCTS, Highlight.class);
            highlightList.stream().forEach(highlight -> {
                LOG.warning("method Migrnado -> "+highlight);
            });
            indexHighlight.saveObjects(highlightList);
            LOG.warning("method addHighlightList List END ");
            return true;
        } catch (AlgoliaException e) {
            LOG.warning("Error in APIAlgolia.addHighlightList. Message:"+ Arrays.toString(e.getStackTrace()));
            return false;
        }
    }

    public static List<Highlight> getListHighlight(Long categoryId){
        try {
            String filters = "categories:" + categoryId;
            String cacheKey = URLConnections.ALGOLIA_PRODUCTS + filters;

            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(cacheKey);
            if (jsonCachedOptional.isPresent()){
                Type listType = new TypeToken<List<Highlight>>(){}.getType();
                return new Gson().fromJson(jsonCachedOptional.get(), listType);
            }

            Index<Highlight> indexHighlight= algoliaClient.initIndex(URLConnections.ALGOLIA_HIGHLIGHTS_OFFERS, Highlight.class);
            SearchResult<Highlight> search = indexHighlight.search(new Query("").setFilters(filters).setHitsPerPage(100));//setFilters("objectID:'HIGHLIGHT.13762'")); //
            List<Highlight> result = search.getHits();

            String jsonToCache = new Gson().toJson(result);
            CachedDataManager.saveJsonInCache(cacheKey, jsonToCache);
            return result;
        } catch (AlgoliaException e) {
            LOG.warning("Error in APIAlgolia.getListHighlight. Message:"+ Arrays.toString(e.getStackTrace()));
            return null;
        }
    }

    public static Item getItemToItemAlgolia(Item item, ItemAlgolia itemAlgolia){
        //LOG.info("method getItemToItemAlgolia -> itemAlgolia.getId() " + itemAlgolia.getId() +" item.getItemId() -> " +  item.getItemId());
        try {

            if (item == null || itemAlgolia == null) {
                LOG.warning("Item or ItemAlgolia is null.");
                return null;
            }

            if (Objects.isNull(item.getItemId()) || !item.getItemId().isEmpty()) {
                item.setItemId(itemAlgolia.getId());
            }

            try {
                if (Objects.nonNull(itemAlgolia.getTextoSEO()) || !itemAlgolia.getTextoSEO().isEmpty())
                    item.setTextSEO(itemAlgolia.getTextoSEO());
            }catch (Exception e){
                LOG.warning("Error el item no tiene texto SEO.");
            }

            item.setOnlyOnline(Objects.requireNonNull(itemAlgolia).isOnlyOnline());
            item.setDeliveryTime(itemAlgolia.getDeliveryTime());
            item.setId((Objects.nonNull(itemAlgolia.getId()) || Objects.nonNull(item.getItemId())) ? Long.valueOf(Objects.nonNull(itemAlgolia.getId()) ? itemAlgolia.getId() : item.getItemId()) : 0L);
            item.setSales(Objects.nonNull(itemAlgolia.getSales()) ? itemAlgolia.getSales() : 0L);
            item.setTaxRate(Objects.nonNull(itemAlgolia.getTaxRate()) ? itemAlgolia.getTaxRate() : 0);
            item.setMediaDescription(itemAlgolia.getMediaDescription());
            item.setFullPrice(Objects.nonNull(
                    itemAlgolia.getFullPrice()) ? itemAlgolia.getFullPrice().intValue() :
                    (Objects.nonNull(item.getFullPrice()) ? item.getFullPrice() : 0));
            if (Objects.nonNull(itemAlgolia.getOfferPrice())) {
                item.setOfferPrice(itemAlgolia.getOfferPrice());
                item.setOfferText(itemAlgolia.getOfferText());
                item.setOfferDescription(itemAlgolia.getOfferDescription());
            }

            if (Objects.nonNull(itemAlgolia.getColor())) {
                item.setColor(itemAlgolia.getColor());
            }
            if (Objects.nonNull(itemAlgolia.getColors())) {
                item.setColors(itemAlgolia.getColors());
            }
            if (Objects.nonNull(itemAlgolia.getFilter())) {
                item.setFilter(itemAlgolia.getFilter());
            }
            if (Objects.nonNull(itemAlgolia.getFiltersLoreal())) {
                item.setFiltersLoreal(itemAlgolia.getFiltersLoreal());
            }
            if (Objects.nonNull(itemAlgolia.getFilterType())) {
                item.setFilterType(itemAlgolia.getFilterType());
            }

            if (Objects.nonNull(itemAlgolia.getUrl())) {
                item.setUrl(itemAlgolia.getUrl());
            }

            if (Objects.nonNull(itemAlgolia.getFilterCategories())) {
                item.setFilterCategories(itemAlgolia.getFilterCategories());
            }

            item.setRequirePrescription(Objects.nonNull(itemAlgolia.getRequirePrescription()) ? itemAlgolia.getRequirePrescription().toString() : Boolean.FALSE.toString());

            int stockAlgolia = Optional.ofNullable(itemAlgolia.getStock()).orElse(MIN_HAS_STOCK);
            int totalStock = Optional.ofNullable(itemAlgolia.getTotalStock()).orElse(MIN_HAS_STOCK);

            item.setTotalStock(stockAlgolia);
            item.setGlobalStock(totalStock);

            item.setHighlight(itemAlgolia.isHighlight());
            item.setGeneric(itemAlgolia.isGeneric());
            item.setIsGeneric(itemAlgolia.isGeneric());
            item.setLargeDescription(itemAlgolia.getLargeDescription());
            item.setAnywaySelling(itemAlgolia.isAnywaySelling());
            item.setGrayDescription(Optional.ofNullable(itemAlgolia.getDetailDescription()).orElse(itemAlgolia.getGrayDescription()));
            item.setOutstanding(itemAlgolia.isOutstanding());
            item.setSpaces(itemAlgolia.getSpaces() > 1 ? itemAlgolia.getSpaces() : item.getSpaces());
            item.setStatus(Objects.nonNull(itemAlgolia.getStatus()) ? itemAlgolia.getStatus() : item.getStatus());
            item.setTaxRate(itemAlgolia.getTaxRate());
            item.setListUrlImages(itemAlgolia.getListUrlImages());
            item.setMediaImageUrl(Objects.nonNull(itemAlgolia.getMediaImageUrl()) ? itemAlgolia.getMediaImageUrl() : item.getMediaImageUrl());
            item.setLargeDescription(Objects.nonNull(itemAlgolia.getLargeDescription()) ? itemAlgolia.getLargeDescription() : item.getLargeDescription());
            item.setBrand(Objects.nonNull(itemAlgolia.getBrand())? itemAlgolia.getBrand() : null);
            Double pumPrice;
            if (Objects.nonNull(itemAlgolia.getOfferPrice()) && itemAlgolia.getOfferPrice() > 0) {
                pumPrice = itemAlgolia.getOfferPrice();
            } else {
                pumPrice = itemAlgolia.getFullPrice();
            }
            if (Objects.nonNull(itemAlgolia.getMeasurePum()) && itemAlgolia.getMeasurePum() > 0) {
                String pum = generatePum(itemAlgolia.getLabelPum(), itemAlgolia.getMeasurePum(), pumPrice);
                item.setPum(pum);
            }
            item.setCategorie(itemAlgolia.getCategorie());
            item.setSupplier(itemAlgolia.getSupplier());
            item.setMarca(itemAlgolia.getMarca());
            item.setDepartments(itemAlgolia.getDepartments());
            item.setSubCategory(itemAlgolia.getSubCategory());
            item.setSupplier(itemAlgolia.getSupplier());
            item.setFilters(itemAlgolia.getFilters());
            item.setSubscribeAndSave(itemAlgolia.getSubscribeAndSave());
            item.setHandleQuantity(itemAlgolia.getHandleQuantity());

            // pre-render
            item.setItemUrl(itemAlgolia.getItemUrl());

            item.setOutofstore(itemAlgolia.getOutofstore());
            item.setFlashOffer(itemAlgolia.getIsFlashOffer());
            item.setOfferEndDate(itemAlgolia.getOfferEndDate());
            item.setOfferStartDate(itemAlgolia.getOfferStartDate());

            // prime
            item.setPrimePrice(Objects.nonNull(itemAlgolia.getPrimePrice())?itemAlgolia.getPrimePrice().intValue():0);
            item.setPrimeTextDiscount(Objects.nonNull(itemAlgolia.getPrimeTextDiscount())? itemAlgolia.getPrimeTextDiscount() : "");
            item.setPrimeDescription(Objects.nonNull(itemAlgolia.getPrimeDescription())? itemAlgolia.getPrimeDescription() : "");
            item.setRms_deparment(Objects.nonNull(itemAlgolia.getRms_deparment()) ? itemAlgolia.getRms_deparment() : "");
            item.setRms_class(Objects.nonNull(itemAlgolia.getRms_class()) ? itemAlgolia.getRms_class() : "");
            item.setRms_subclass(Objects.nonNull(itemAlgolia.getRms_subclass()) ? itemAlgolia.getRms_subclass(): "");
            item.setRms_group(Objects.nonNull(itemAlgolia.getRms_group()) ? itemAlgolia.getRms_group() : "");
            item.setWithout_stock(itemAlgolia.isWithout_stock());

            if (Objects.nonNull(itemAlgolia.getCustomTag())) {
                item.setCustomTag(itemAlgolia.getCustomTag());
            }

            // Optics add itemOpticsComplete

            if (Objects.nonNull(itemAlgolia.getItemOpticsComplete()) && Objects.nonNull(itemAlgolia.getItemOpticsComplete().getItemOptics())) {
                OpticsServices opticsServices = new OpticsServices();
                ItemOpticsComplete itemOpticsComplete = opticsServices.generateItemOpticsComplete(itemAlgolia);
                if (itemOpticsComplete != null){
                    item.setItemOpticsComplete(itemOpticsComplete);
                }
            }
            if(Objects.nonNull(itemAlgolia.isRequirePrescriptionMedical())){
                item.setRequirePrescriptionMedical(itemAlgolia.isRequirePrescriptionMedical());
            }

            item.setCustomLabelForStockZero(Objects.nonNull(itemAlgolia.getCustomLabelForStockZero())?itemAlgolia.getCustomLabelForStockZero():"");


            // add data marketplace
            if(Objects.nonNull(itemAlgolia.getDimensions())){
                item.setDimensions(itemAlgolia.getDimensions());
            }

            item.setWarranty(Objects.nonNull(itemAlgolia.getWarranty()) ? itemAlgolia.getWarranty() : "");
            item.setWarrantyTerms(Objects.nonNull(itemAlgolia.getWarrantyTerms()) ? itemAlgolia.getWarrantyTerms() : "");
            item.setUuidItem(Objects.nonNull(itemAlgolia.getUuidItem()) ? itemAlgolia.getUuidItem() : "");

            if (Objects.nonNull(itemAlgolia.getVariants())) {
                item.setVariants(itemAlgolia.getVariants());
            }

            if(Objects.nonNull(itemAlgolia.getSellerAddresses())){
                item.setSellerAddresses(itemAlgolia.getSellerAddresses());
            }

            if (itemAlgolia.getHasStock() != null) {
                item.setHasStock(itemAlgolia.getHasStock());
            }

            if (itemAlgolia.getStores_with_stock() != null) {
                item.setStoresWithStock(itemAlgolia.getStores_with_stock());
            }

            return item;
        }catch (Exception e){
            LOG.warning("Error en getItemToItemAlgolia. Message:"+ Arrays.toString(e.getStackTrace()));
            return null;
        }
    }


    private static String generatePum(String label, float pum, Double itemPrice){
        Double pumPrice = Objects.nonNull(pum) ?  itemPrice/pum : null;
        return label.concat(String.format("%.2f", pumPrice));
    }

    public static boolean createBanner(Banner banner){
        try {
            LOG.warning("method createBanner INIT ");
            Index<Banner> indexProducts = algoliaClient.initIndex(URLConnections.ALGOLIA_BANNERS, Banner.class);
            indexProducts.saveObject(banner.getIdBanner(), banner);
            LOG.warning("method createBanner End "+banner);
            return true;
        } catch (AlgoliaException e) {
            LOG.warning("Error in APIAlgolia.createBanner. Message:"+ Arrays.toString(e.getStackTrace()));
            return false;
        }
    }

    public static boolean updateBanner(Banner banner){
       try {
            LOG.warning("method updateBanner INIT ");
            Index<Banner> indexBanner = algoliaClient.initIndex(URLConnections.ALGOLIA_BANNERS, Banner.class);
            SearchResult<Banner> search = indexBanner.search(new Query("").setFilters("idWebSafeBanner:'" + banner.getIdWebSafeBanner()+ "'")).setHitsPerPage(100);
            if(Objects.nonNull(search.getHits()) && !search.getHits().isEmpty()){
                Banner bannerResult =  search.getHits().get(0);
                banner.setIdBanner(bannerResult.getIdBanner());
                banner.setIdCategory(bannerResult.getIdCategory());
                banner.setClassificationLevel(bannerResult.getClassificationLevel());
                indexBanner.partialUpdateObject(banner.getIdBanner(), banner);
                LOG.warning("method updateBanner Actualizado "+banner);
            }else{
                LOG.warning("method updateBanner No encontro el Banner actualizado "+banner);
            }
            return true;
        } catch (AlgoliaException e) {
            LOG.warning("Error in APIAlgolia.createBanner. Message:"+ Arrays.toString(e.getStackTrace()));
            return false;
        }
    }

    public static boolean deleteBanner(String idBanner){
        try {
            LOG.warning("method deleteBanner INIT "+idBanner);
            Index<Banner> indexBanner = algoliaClient.initIndex(URLConnections.ALGOLIA_BANNERS, Banner.class);
            SearchResult<Banner> search = indexBanner.search(new Query("").setFilters("idWebSafeBanner:'" + idBanner+ "'")).setHitsPerPage(100);
            if(Objects.nonNull(search.getHits()) && !search.getHits().isEmpty()){
                indexBanner.deleteObject(search.getHits().get(0).getIdBanner());
                return true;
            }
            LOG.warning("method deleteBanner End "+idBanner);
            return false;
        } catch (AlgoliaException e) {
            LOG.warning("Error in APIAlgolia.deleteBanner. Message:"+ Arrays.toString(e.getStackTrace()));
            return false;
        }
    }

    public static List<Banner> getBanners(Long idCategory, Boolean directionBanner, Boolean bannerWeb){
        try {
            LOG.warning("method getBanners INIT ");
            Index<Banner> indexBanner = algoliaClient.initIndex(URLConnections.ALGOLIA_BANNERS, Banner.class);
            Comparator<Banner> sorderByOrder = (banner1, banner2) -> Integer.compare(banner1.getOrder(), banner2.getOrder());
            List<Banner> bannerList = new ArrayList<>();
            if(Objects.nonNull(idCategory)) {
                LOG.warning("method getBanners: Busqueda con Categoria: "+idCategory);
                SearchResult<Banner> search = indexBanner.search(new Query("").setFilters("idCategory:'" + idCategory+ "'").setHitsPerPage(100));
                bannerList = search.getHits().stream().filter(banner -> banner.isDirectionBanner() == directionBanner).sorted(sorderByOrder).collect(Collectors.toList());
            }else{
                if (Objects.nonNull(bannerWeb)) {
                    LOG.warning("method getBanners: Busqueda con bannerWeb: "+bannerWeb);
                    SearchResult<Banner> search = indexBanner.search(new Query("").setFilters("bannerWeb:'" + bannerWeb+ "'").setHitsPerPage(100));
                    bannerList = search.getHits().stream().filter(banner -> banner.isDirectionBanner() == directionBanner && (Objects.isNull(banner.getIdCategory()) &&
                            (Objects.nonNull(banner.getClassificationLevel()) && banner.getClassificationLevel() == 1))).sorted(sorderByOrder).collect(Collectors.toList());
                } else {
                    LOG.warning("method getBanners: Busqueda por defecto: ");

                    SearchResult<Banner> search = indexBanner.search(new Query("").setFilters("directionBanner:" + directionBanner).setHitsPerPage(100));
                    LOG.warning("method getBanners Registros encontrados: "+search.getHits().size());
                    /*
                    search.getHits().stream().forEach(banner -> {
                        LOG.warning("method getBanners Base: "+banner);
                        LOG.warning("method Conditions: directionBanner "+(banner.isDirectionBanner() == directionBanner)+" - getIdCategory() "+Objects.isNull(banner.getIdCategory()) +" - banner.getIdCategory() " +  (Objects.nonNull(banner.getIdCategory()) && banner.getIdCategory() == 1) + " - banner.getClassificationLevel() "+ (Objects.nonNull(banner.getClassificationLevel()) && banner.getClassificationLevel() == 1));
                        //banner.setRedirectUrl(banner.getRedirectUrl().replace("#/", ""));
                    });*/
                    bannerList = search.getHits().stream().filter(banner -> banner.isDirectionBanner() == directionBanner && ( Objects.isNull(banner.getIdCategory()) && //( Objects.isNull(banner.getIdCategory()) || //Objects.nonNull(banner.getIdCategory()) && banner.getIdCategory() == 1) &&
                             (Objects.nonNull(banner.getClassificationLevel()) && banner.getClassificationLevel() == 1))).sorted(sorderByOrder).collect(Collectors.toList());
                }
            }
            /*
            bannerList.stream().forEach(banner -> {
                LOG.warning("method getBanners: "+banner);
                //banner.setRedirectUrl(banner.getRedirectUrl().replace("#/", ""));
            });*/
            return bannerList;
        } catch (AlgoliaException e) {
            LOG.warning("Error in APIAlgolia.getBanners. Message:"+ Arrays.toString(e.getStackTrace()));
            return null;
        }
    }

    public static boolean createCategoryPhoto(CategoryPhoto categoryPhoto){
        try {
            LOG.warning("method createCategoryPhoto INIT "+categoryPhoto);
            Index<CategoryPhoto> indexCategory = algoliaClient.initIndex(URLConnections.ALGOLIA_CATEGORY_PHOTO, CategoryPhoto.class);
            LOG.warning("method createCategoryPhoto Valid Index "+Objects.isNull(indexCategory));
            indexCategory.saveObject(categoryPhoto.getIdPhotoWebSafe(), categoryPhoto);
            LOG.warning("method createCategoryPhoto End "+categoryPhoto);
            return true;
        } catch (AlgoliaException e) {
            LOG.warning("Error in APIAlgolia.createCategoryPhoto. Message:"+ Arrays.toString(e.getStackTrace()));
            return false;
        }
    }

    public static boolean updateCategoryPhoto(CategoryPhoto categoryPhoto){
        try {
            Index<CategoryPhoto> indexCategoryPhoto = algoliaClient.initIndex(URLConnections.ALGOLIA_CATEGORY_PHOTO, CategoryPhoto.class);
            String objectId = categoryPhoto.getIdPhotoWebSafe();
            Optional<CategoryPhoto> categoryPhotoOptional = indexCategoryPhoto.getObject(objectId);
            if(categoryPhotoOptional.isPresent()){
                CategoryPhoto categoryResult = categoryPhotoOptional.get();
                categoryResult.setImagePosition(calculateCategoryPhotoIndex(categoryPhoto.getImagePosition(), categoryResult.getIdDepartment()));
                categoryResult.setImageUrl(categoryPhoto.getImageUrl());
                categoryResult.setRedirect(categoryPhoto.getRedirect());
                categoryResult.setRedirectUrl(categoryPhoto.getRedirectUrl());
                indexCategoryPhoto.partialUpdateObject(categoryResult.getIdPhotoWebSafe(), categoryResult);
                LOG.warning("method updateCategoryPhoto Actualizado "+categoryPhoto);
            }else{
                LOG.warning("method updateCategoryPhoto No encontro el Banner actualizado "+categoryPhoto);
            }
            return true;
        } catch (AlgoliaException e) {
            LOG.warning("Error in APIAlgolia.createBanner. Message:"+ Arrays.toString(e.getStackTrace()));
            return false;
        }
    }

    public static boolean deleteCategoryPhoto(String idPhotoWebSafe){
        try {
            LOG.warning("method deleteCategoryPhoto INIT ");
            Index<CategoryPhoto> indexBanner = algoliaClient.initIndex(URLConnections.ALGOLIA_CATEGORY_PHOTO, CategoryPhoto.class);
            Optional<CategoryPhoto> categoryPhotoOptional = indexBanner.getObject(idPhotoWebSafe);
            if(categoryPhotoOptional.isPresent()){
                indexBanner.deleteObject(idPhotoWebSafe);
                return true;
            }
            LOG.warning("method deleteCategoryPhoto End "+idPhotoWebSafe);
            return false;
        } catch (AlgoliaException e) {
            LOG.warning("Error in APIAlgolia.deleteCategoryPhoto. Message:"+ Arrays.toString(e.getStackTrace()));
            return false;
        }
    }

    public static List<CategoryPhoto> getCategoryPhotos(Long idDepartment){

        return Collections.emptyList();
    }

    public static Integer calculateCategoryPhotoIndex(int index,  long idDepartment){
        LOG.warning("method calculateCategoryPhotoIndex INIT ");
        try {
            List<IndexQuery> queries = new ArrayList<>();
            queries.add(new IndexQuery(URLConnections.ALGOLIA_CATEGORY_PHOTO, new Query("").setFilters("idDepartment:'" + idDepartment + "'")));
            SearchResult<?> search = algoliaClient.multipleQueries(queries).getResults().get(0);
            int finalIndex = (Objects.nonNull(search.getHits()) ? ((search.getHits().size() == 0) ? 1 : ( index > search.getHits().size() ? search.getHits().size()  : index  )) : 1);
            LOG.warning("method getOriginProperties: count: "+ finalIndex);
            return finalIndex;
        } catch (AlgoliaException e) {
            LOG.warning("Error in APIAlgolia.calculateCategoryPhotoIndex. Message:"+ Arrays.toString(e.getStackTrace()));
            return 1;
        }
    }

    public static List<Filter> getFilterList(){
        LOG.warning("method getFilterList INIT ");
        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_ITEM_FILTERS);
            if (jsonCachedOptional.isPresent()){
                Type listType = new TypeToken<List<Filter>>(){}.getType();
                return new Gson().fromJson(jsonCachedOptional.get(), listType);
            }

            Index<Filter> indexCategory = algoliaClient.initIndex(URLConnections.ALGOLIA_ITEM_FILTERS, Filter.class);
            SearchResult<Filter> search  = indexCategory.search(new Query(""));
            LOG.warning("method getFilterList: count: "+ search.getHits().isEmpty());
            List<Filter> result = search.getHits();

            String jsonToCache = new Gson().toJson(result);
            CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_ITEM_FILTERS, jsonToCache);
            return result;
        } catch (AlgoliaException e) {
            LOG.warning("Error in APIAlgolia.getFilterList. Message:"+ Arrays.toString(e.getStackTrace()) +" AlgoliaException:"+e);
            return null;
        }
    }

    public static String getDynamicDeliveryLabel(final int storeId) {
        //LOG.warning("method: getDynamicDeliveryLabel storeId" + storeId);
        final String DEFAULT_MESSAGE = "Domicilio";
        Optional<DeliveryLabelObject> deliveryLabelObject;
        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_CART_LABEL_DELIVERY_VALUE);

            if (jsonCachedOptional.isPresent()){
                 //LOG.info("tiene cache getDynamicDeliveryLabel" );
                 deliveryLabelObject = Optional.of(new Gson().fromJson(jsonCachedOptional.get(),DeliveryLabelObject.class));
            }else {
                Index<DeliveryLabelObject> indexDeliveryLabelObject = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, DeliveryLabelObject.class);
                deliveryLabelObject = indexDeliveryLabelObject.getObject(URLConnections.ALGOLIA_CART_LABEL_DELIVERY_VALUE);
                //LOG.info("No tiene cache getDynamicDeliveryLabel" );
                deliveryLabelObject.ifPresent( data -> {
                    String jsonToCache = new Gson().toJson(deliveryLabelObject.get());
                    CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_CART_LABEL_DELIVERY_VALUE, jsonToCache);
                } );
            }
            //LOG.info("IF(deliveryLabelObject==null):[" + (deliveryLabelObject == null) + "]");
            if (Objects.nonNull(deliveryLabelObject) && Objects.nonNull(deliveryLabelObject.get().getValues()) && !deliveryLabelObject.get().getValues().isEmpty()) {
                Optional<DeliveryLabelDetail> detailOpt = deliveryLabelObject.get().getValues().stream().filter(detail -> Integer.compare(detail.getStoreId(), storeId) == 0).findFirst();
                if (detailOpt.isPresent()) {
                    LOG.warning("method: getDynamicDeliveryLabel " + detailOpt.get().getLabel());
                    return detailOpt.get().getLabel();
                }
            }
            return DEFAULT_MESSAGE;
        } catch (Exception e) {
            LOG.warning("No se encontró el label el Algolia. Se devolvera uno por defecto con el valor: Domicilio. Error: " + e.getMessage());
            return DEFAULT_MESSAGE;
        }
    }

    public static CartDeliveryLabelConfig saveCartDeliveryLabelConfig(CartDeliveryLabelConfig cartDeliveryLabelConfig) {
        LOG.warning("method addHighlightList INIT");
        try {
            Index<CartDeliveryLabelConfig> indexconfig = algoliaClient.initIndex(URLConnections.ALGOLIA_PROPERTIES,
                    CartDeliveryLabelConfig.class);
            indexconfig.saveObject(URLConnections.ALGOLIA_CART_LABEL_DELIVERY_VALUE, cartDeliveryLabelConfig);
            return cartDeliveryLabelConfig;
        } catch (AlgoliaException e) {
            LOG.warning(e.getMessage());
        }
        return null;
    }

    public static Optional<CartDeliveryLabelConfig> getCartDeliveryLabelConfig() {
        LOG.warning("method getCartDeliveryLabelConfig INIT");
        try {
            Index<CartDeliveryLabelConfig> indexconfig = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, CartDeliveryLabelConfig.class);
            return  indexconfig.getObject(URLConnections.ALGOLIA_CART_LABEL_DELIVERY_VALUE);
        } catch (AlgoliaException e) {
            LOG.warning(e.getMessage());
        }
        return Optional.empty();
    }

    public static CartDeliveryLabelConfig getCartDeliveryLabelConfig(final int storeId) {
        LOG.info("method getCartDeliveryLabelConfig INIT, storeId: " + storeId);
        try {
            Optional<CartDeliveryLabelConfig> cartDeliveryLabelConfigOptional = getCartDeliveryLabelConfig();
            if(cartDeliveryLabelConfigOptional.isPresent()){
                final int idStoreGroup = getDefaultStoreIdByStoreId(storeId);
                Optional<CartDeliveryLabelConfigValue> detailOpt = cartDeliveryLabelConfigOptional.get().getValues().stream().filter(detail -> Integer.compare(detail.getStoreId(), idStoreGroup) == 0).findFirst();
                if (detailOpt.isPresent()) {
                    LOG.warning("method: getDynamicDeliveryLabel " + detailOpt.get().getLabel());
                    CartDeliveryLabelConfig cartDeliveryLabelConfig = new CartDeliveryLabelConfig();
                    List details = new ArrayList<CartDeliveryLabelConfigValue>();
                    details.add(detailOpt.get());
                    cartDeliveryLabelConfig.setValues(details);
                    return cartDeliveryLabelConfig;
                }
            }
        } catch (AlgoliaException e) {
            LOG.warning(e.getMessage());
        }
        return null;
    }

    public static CartDeliveryLabelConfigValueResp getCartDeliveryLabelConfigByStoreId(int storeId) {
        LOG.warning("method getCartDeliveryLabelConfig INIT storeId:"+storeId);
        try {
            Optional<CartDeliveryLabelConfig> cartDeliveryLabelConfig = getCartDeliveryLabelConfig();
            final int idStoreGroup = getDefaultStoreIdByStoreId(storeId);
            if(cartDeliveryLabelConfig.isPresent()){
                Optional<CartDeliveryLabelConfigValue> detailOpt = cartDeliveryLabelConfig.get().getValues().
                        stream().filter(detail -> Integer.compare(detail.getStoreId(), idStoreGroup) == 0).findFirst();

                if (detailOpt.isPresent()) {
                    DateTime nowDate = new DateTime(System.currentTimeMillis(), DateTimeZone.forID("America/Bogota"));
                    CartDeliveryLabelConfigValue deliveryLabelConfigValue = detailOpt.get();
                    LOG.info("Time now -----> " + nowDate.getMillis()+ " dateFrom --->" + deliveryLabelConfigValue.getDateFrom() +
                    " Date until .------> " + deliveryLabelConfigValue.getDateUntil());
                    if (nowDate.getMillis() < deliveryLabelConfigValue.getDateUntil() &&
                            nowDate.getMillis() > deliveryLabelConfigValue.getDateFrom()) {
                        LOG.warning("method: getDynamicDeliveryLabel " + deliveryLabelConfigValue.getLabel());
                        return new CartDeliveryLabelConfigValueResp(deliveryLabelConfigValue);
                    }
                }
            }
        } catch (AlgoliaException e) {
            LOG.warning("method getCartDeliveryLabelConfig error:"+e.fillInStackTrace());
        }
        return null;
    }

    public static Optional<BannerDataCMSType> getDefaultBannersHome(){
        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_HOME_DEFAULT_BANNERS);

            if (jsonCachedOptional.isPresent()){
                //LOG.info("tiene cache getDefaultBannersHome" );
                return Optional.of(new Gson().fromJson(jsonCachedOptional.get(), BannerDataCMSType.class));
            }
            Index<BannerDataCMSType> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, BannerDataCMSType.class);
            Optional<BannerDataCMSType> bannerDataCMSType = index.getObject(URLConnections.ALGOLIA_HOME_DEFAULT_BANNERS);
            //LOG.info("No tiene cache getDefaultBannersHome" );
            bannerDataCMSType.ifPresent( data -> {
                String jsonToCache = new Gson().toJson(bannerDataCMSType.get());
                CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_HOME_DEFAULT_BANNERS, jsonToCache);
            } );
            return bannerDataCMSType;
        } catch (AlgoliaException e) {
            LOG.warning(e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Obtiene la configuración de labels para tipo de envio distintos.
     * Estos se mostraran en carrito, detalle de orden, resumen de pedido y checkout.
     *
     * @return*/
    public static Optional<DeliveryTimeLabelConfig> getDeliveryTimeLabelConfig(){
        try {
            Index<DeliveryTimeLabelConfig> indexconfig = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, DeliveryTimeLabelConfig.class);

            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_DELIVERY_TIME_LABEL_CONFIG);

            Optional<DeliveryTimeLabelConfig> optionalReturn;

            if (!jsonCachedOptional.isPresent()){
                optionalReturn = indexconfig.getObject(URLConnections.ALGOLIA_DELIVERY_TIME_LABEL_CONFIG);
                LOG.warning("FUE_ALGOLIA :(");
                if (optionalReturn.isPresent()){
                    String json = new Gson().toJson(optionalReturn.get());
                    CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_DELIVERY_TIME_LABEL_CONFIG,json);
                    //LOG.info("REDIS SAVE CACHE --> " + json);
                }
                return optionalReturn;
            }

            DeliveryTimeLabelConfig deliveryTimeLabelConfig = new Gson().fromJson(jsonCachedOptional.get(),DeliveryTimeLabelConfig.class);
            optionalReturn = Optional.of(deliveryTimeLabelConfig);

            return  optionalReturn;
        } catch (AlgoliaException e) {
            LOG.warning(e.getMessage());
        }
        return Optional.empty();
    }


    /**
     * Obtiene la configuración del objeto DELIVERY.TIME.LABEL.TEMPLATE
     * @return*/
    public static Optional<DeliveryTimeLabelTemplate> getDeliveryTimeLabelTemplate(){
        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_DELIVERY_TIME_LABEL_TEMPLATE);

            if (jsonCachedOptional.isPresent()){
                //LOG.info("tiene cache getDeliveryTimeLabelTemplate" );
                return Optional.of(new Gson().fromJson(jsonCachedOptional.get(), DeliveryTimeLabelTemplate.class));
            }

            Index<DeliveryTimeLabelTemplate> indexconfig = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, DeliveryTimeLabelTemplate.class);
            Optional<DeliveryTimeLabelTemplate> deliveryTimeLabelTemplate = indexconfig.getObject(URLConnections.ALGOLIA_DELIVERY_TIME_LABEL_TEMPLATE);
            //LOG.info("No tiene cache getDeliveryTimeLabelTemplate" );
            deliveryTimeLabelTemplate.ifPresent( data -> {
                String jsonToCache = new Gson().toJson(deliveryTimeLabelTemplate.get());
                CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_DELIVERY_TIME_LABEL_TEMPLATE, jsonToCache);
            } );

            return deliveryTimeLabelTemplate;
        } catch (AlgoliaException e) {
            LOG.warning(e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Obtiene la configuración del objeto DELIVERY.TIME.FOR.DELIVERY.TYPE
     * @return*/
    public static Optional<DeliveryTimeForDeliveryTypeObject> getDeliveryTimeForDeliveryTypeObject(){
        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_DELIVERY_TIME_FOR_DELIVERY_TYPE);

            if (jsonCachedOptional.isPresent()){
                //LOG.info("tiene cache getDeliveryTimeForDeliveryTypeObject" );
                return Optional.of(new Gson().fromJson(jsonCachedOptional.get(),DeliveryTimeForDeliveryTypeObject.class));
            }
            Index<DeliveryTimeForDeliveryTypeObject> indexconfig = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, DeliveryTimeForDeliveryTypeObject.class);
            Optional<DeliveryTimeForDeliveryTypeObject> deliveryTimeForDeliveryTypeObject = indexconfig.getObject(URLConnections.ALGOLIA_DELIVERY_TIME_FOR_DELIVERY_TYPE);
            //LOG.info("No tiene cache getDeliveryTimeForDeliveryTypeObject" );
            deliveryTimeForDeliveryTypeObject.ifPresent( data -> {
                String jsonToCache = new Gson().toJson(deliveryTimeForDeliveryTypeObject.get());
                CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_DELIVERY_TIME_FOR_DELIVERY_TYPE, jsonToCache);
            } );

            return deliveryTimeForDeliveryTypeObject;
        } catch (AlgoliaException e) {
            LOG.warning(e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Obtiene la configuración del objeto CREATE.ORDER.CONFIG
     * @return*/
    public static CreateOrderConfig getCreateOrderConfig(String deliveryType){
        LOG.warning("---------------->Method getCreateOrderConfig : delivery : "+deliveryType);
        try {
        Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_CREATE_ORDER_CONFIG);

        if (jsonCachedOptional.isPresent()){
            //LOG.info("tiene cache getCreateOrderConfig" );
            Optional<ConfigurationByDeliveryType> configurationByDeliveryType = Optional.of(new Gson().fromJson(jsonCachedOptional.get(), ConfigurationByDeliveryType.class));
            Optional<CreateOrderConfig> result = configurationByDeliveryType.isPresent() ?
                    configurationByDeliveryType.get().getConfiguration().stream().filter(config -> config.getDeliveryType().equals(deliveryType)).findFirst() : null;
            //LOG.warning("---------------->Method getCreateOrderConfig : result: "+(Objects.nonNull(result) ? result.isPresent() : "null"));
            return Objects.nonNull(result) && result.isPresent() ? result.get() : null;
        }

         Index<ConfigurationByDeliveryType> orderConfigIndex = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, ConfigurationByDeliveryType.class);
         LOG.warning("---------------->Method getCreateOrderConfig : query : "+"objectID:'" + URLConnections.ALGOLIA_CREATE_ORDER_CONFIG + "' AND deliveryType:'" + deliveryType + "'");
         Optional<ConfigurationByDeliveryType> search =  orderConfigIndex.getObject(URLConnections.ALGOLIA_CREATE_ORDER_CONFIG);
         //LOG.info("No tiene cache getCreateOrderConfig" );
         search.ifPresent( data -> {
            String jsonToCache = new Gson().toJson(search.get());
            CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_CREATE_ORDER_CONFIG, jsonToCache);
        } );

         Optional<CreateOrderConfig> result = search.isPresent() ?
                 search.get().getConfiguration().stream().filter(config -> config.getDeliveryType().equals(deliveryType)).findFirst() : null;
         //LOG.warning("---------------->Method getCreateOrderConfig : result: "+(Objects.nonNull(result) ? result.isPresent() : "null"));
         return Objects.nonNull(result) && result.isPresent() ? result.get() : null;
        } catch (AlgoliaException e) {
            LOG.severe(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Obtener la configuracion de ETA desde Algolia.
     * */
    public static Optional<ETAConfig> getETAConfig(){
        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_ETA_CONFIG);

            if (jsonCachedOptional.isPresent()){
                //LOG.info("tiene cache getETAConfig" );
                return Optional.of(new Gson().fromJson(jsonCachedOptional.get(), ETAConfig.class));
            }
            Index<ETAConfig> ETAConfigIndex = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, ETAConfig.class);
            Optional<ETAConfig> optional = ETAConfigIndex.getObject(URLConnections.ALGOLIA_ETA_CONFIG);
            //LOG.info("No tiene cache getETAConfig" );
            optional.ifPresent( data -> {
                String jsonToCache = new Gson().toJson(optional.get());
                CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_ETA_CONFIG, jsonToCache);
            } );
            return optional;
        } catch (AlgoliaException e) {
            LOG.severe(e.getMessage());
        }
        //LOG.info("Retornara Vacio.");
        return Optional.empty();
    }

    /**
     * return web config security feature-flag
     * @return SecurityWebConfig
     */
    public static Optional<SecurityWebConfig> getSecurityConfig() {

        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_SECURITY_WEB_CONFIG);

            if (jsonCachedOptional.isPresent()){
                //LOG.info("tiene cache getSecurityConfig" );
                return Optional.of(new Gson().fromJson(jsonCachedOptional.get(),SecurityWebConfig.class));
            }
            Index<SecurityWebConfig> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, SecurityWebConfig.class);
            Optional<SecurityWebConfig> securityWebConfig = index.getObject(URLConnections.ALGOLIA_SECURITY_WEB_CONFIG);
            //LOG.info("No tiene cache getSecurityConfig" );
            securityWebConfig.ifPresent( data -> {
                String jsonToCache = new Gson().toJson(securityWebConfig.get());
                CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_SECURITY_WEB_CONFIG, jsonToCache);
            } );
            return securityWebConfig;

        } catch (AlgoliaException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * GET FILTERS ALL ITEMS CONFIG.
     * @return
     */
    public static Optional<GenericFiltersConfig> getFiltersConfig(){
        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_FILTERS_CONFIG);

            if (jsonCachedOptional.isPresent()){
                //LOG.info("tiene cache getFiltersConfig" );
                return Optional.of(new Gson().fromJson(jsonCachedOptional.get(), GenericFiltersConfig.class));
            }
            Index<GenericFiltersConfig> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES,
                    GenericFiltersConfig.class);
            Optional<GenericFiltersConfig> genericFiltersConfig = index.getObject(URLConnections.ALGOLIA_FILTERS_CONFIG);
            //LOG.info("No tiene cache getFiltersConfig" );
            genericFiltersConfig.ifPresent( data -> {
                String jsonToCache = new Gson().toJson(genericFiltersConfig.get());
                CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_FILTERS_CONFIG, jsonToCache);
            } );
            return genericFiltersConfig;

        } catch (AlgoliaException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * getGenericObjectById
     * @param objectId
     * @return generic objectid
     */
    public static Optional<JSONObject> getGenericObjectById(String objectId){

        if (objectId == null || objectId.isEmpty()){
            return Optional.empty();
        }

        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(objectId);

            if (jsonCachedOptional.isPresent()){
                //LOG.info("tiene cache getGenericObjectById" );
                return Optional.of(new Gson().fromJson(jsonCachedOptional.get(), JSONObject.class));
            }
            Index<JSONObject> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES,
                    JSONObject.class);
            Optional<JSONObject> jsonObject = index.getObject(objectId);
            //LOG.info("No tiene cache getGenericObjectById" );
            jsonObject.ifPresent( data -> {
                String jsonToCache = new Gson().toJson(jsonObject.get());
                CachedDataManager.saveJsonInCache(objectId, jsonToCache);
            } );
            return jsonObject;

        } catch (AlgoliaException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }


    /**
     * config couponS filters
     * @return RETURN CONFIG FILTERS COUPONS ALGOLIA
     */
    public static Optional<CouponFiltersConfig> getCouponFilterConfig() {
        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_COUPON_FILTERS);

            if (jsonCachedOptional.isPresent()){
                //LOG.info("tiene cache filter config" );
                return Optional.of(new Gson().fromJson(jsonCachedOptional.get(), CouponFiltersConfig.class));
            }
            Index<CouponFiltersConfig> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, CouponFiltersConfig.class);
            Optional<CouponFiltersConfig> couponFiltersConfig = index.getObject(URLConnections.ALGOLIA_COUPON_FILTERS);

            //LOG.info("No tiene cache cupons filter config" );
            couponFiltersConfig.ifPresent( data -> {
                String jsonToCache = new Gson().toJson(couponFiltersConfig.get());
                CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_COUPON_FILTERS, jsonToCache);
            } );


            return couponFiltersConfig;

        } catch (AlgoliaException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * config tips
     * @return RETURN NOT ALLOWED TIPS ALGOLIA
     */
    public static Optional<NotAllowedTips> getNotAllowedTipsConfig() {
        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_NOT_ALLOWED_TIPS);

            if (jsonCachedOptional.isPresent()){
                //LOG.info("tiene cache getNotAllowedTipsConfig" );
                return Optional.of(new Gson().fromJson(jsonCachedOptional.get(), NotAllowedTips.class));
            }
            Index<NotAllowedTips> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, NotAllowedTips.class);
            Optional<NotAllowedTips> notAllowedTips = index.getObject(URLConnections.ALGOLIA_NOT_ALLOWED_TIPS);
            //LOG.info("No tiene cache getNotAllowedTipsConfig" );
            notAllowedTips.ifPresent( data -> {
                String jsonToCache = new Gson().toJson(notAllowedTips.get());
                CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_NOT_ALLOWED_TIPS, jsonToCache);
            } );

            return notAllowedTips;

        } catch (AlgoliaException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }




    /**
     * get items by search
     * @param searchText
     * @param idStoreGroup
     * @param hitsPerPage
     * @param page
     * @return
     * @throws AlgoliaException
     */
  @SuppressWarnings("Duplicates")
  public static HitsItemsAlgolia getItemsBySearch(
          String searchText,
          Integer idStoreGroup,
          String nearbyStores,
          Integer hitsPerPage,
          Integer page,
          String category)
          throws AlgoliaException {

      if (Stream.of(searchText, hitsPerPage, page).anyMatch(Objects::isNull) ||
              (idStoreGroup == null && (nearbyStores == null || nearbyStores.isEmpty()))) {
          throw new IllegalArgumentException("Invalid input parameters");
      }

      HitsItemsAlgolia hitsItemsAlgolia = new HitsItemsAlgolia();
      Index<ItemAlgolia> indexProducts = algoliaClient.initIndex(
              nearbyStores != null && !nearbyStores.isEmpty()
                      ? AlgoliaIndexEnum.PRODUCTS_COLOMBIA_PROD.getIndexName()
                      : AlgoliaIndexEnum.PRODUCTS_PROD.getIndexName(),
              ItemAlgolia.class);

      Query query = buildQuerySearchSubstitutes(searchText, hitsPerPage, page, idStoreGroup, nearbyStores, category);

      SearchResult<ItemAlgolia> searchResult = indexProducts.search(query);
      List<ItemAlgolia> filteredResults = searchResult.getHits();
      if (Objects.isNull(nearbyStores) || nearbyStores.isEmpty()) {
          filteredResults = searchResult.getHits().stream()
                  .filter(item -> item.getStock() != null && item.getStock() > 0)
                  .limit(hitsPerPage)
                  .collect(Collectors.toList());
      }

      hitsItemsAlgolia.setItemAlgoliaList(filteredResults);
      hitsItemsAlgolia.setNbHits((long) filteredResults.size());
      hitsItemsAlgolia.setNbHitsPerPage(Long.valueOf(hitsPerPage));
      hitsItemsAlgolia.setNbPages(1L);

      //LOG.info("indexName = " + indexProducts.getName() + "Search completed: " + searchText + ", Hits: " + filteredResults.size() + ", Requested: " + hitsPerPage);

      return hitsItemsAlgolia;
  }

    /**
     * get items with filters and pagination for the search of substitutes
     * @param searchText search text
     * @param idStoreGroup store group id
     * @param hitsPerPage number of items per page
     * @param page page number
     * @return list of items
     */
    private static Query buildQuerySearchSubstitutes(String searchText, Integer hitsPerPage, Integer page,
                                                     Integer idStoreGroup, String nearbyStores, String category) {
        Query query = new Query()
                .setQuery(searchText)
                .setHitsPerPage(hitsPerPage * 2)
                .setPage(page);

        StringBuilder filtersBuilder = new StringBuilder();

        if (nearbyStores != null && !nearbyStores.isEmpty()) {
            String[] storeIds = nearbyStores.split(",");
            filtersBuilder.append("(");
            for (int i = 0; i < storeIds.length; i++) {
                if (i > 0) filtersBuilder.append(" OR ");
                filtersBuilder.append("stores_with_stock:").append(storeIds[i]);
            }
            filtersBuilder.append(")");
        } else {
            filtersBuilder.append("idStoreGroup:'").append(idStoreGroup).append("'");
            filtersBuilder.append(" AND without_stock:false");
        }

        if (category != null && !category.isEmpty()) {
            filtersBuilder.append(" AND categorie:'").append(category).append("'");
        }

        if (CATEGORIE_CONTACT_LENSES.equals(category)) {
            filtersBuilder.append(" AND outofstore:false");
        }

        query.setFilters(filtersBuilder.toString());

        //LOG.info("Query: " + new Gson().toJson(query));

        return query;
    }

    public static Optional<ScanAndGoPushNotificationProperty> getScanAndGoPushNotificationProperty() {
        //LOG.info("method getScanAndGoPushNotificationProperty INIT");
        return getScanAndGoPushNotificationProperty(URLConnections.ALGOLIA_SCAN_AND_GO_PUSH_NOTIFICATION);
    }

    public static Optional<ScanAndGoPushNotificationProperty> getScanAndGoPushNotificationProperty(String scanAndGoProperty) {
        //LOG.info("method getScanAndGoPushNotificationProperty INIT");
        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(scanAndGoProperty);

            if (jsonCachedOptional.isPresent()){
                //LOG.info("tiene cache getScanAndGoPushNotificationProperty" );
                return Optional.of(new Gson().fromJson(jsonCachedOptional.get(), ScanAndGoPushNotificationProperty.class));
            }
            Index<ScanAndGoPushNotificationProperty> indexconfig = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, ScanAndGoPushNotificationProperty.class);
            Optional<ScanAndGoPushNotificationProperty> scanAndGoPushNotificationProperty = indexconfig.getObject(scanAndGoProperty);
            //LOG.info("No tiene cache getScanAndGoPushNotificationProperty" );
            scanAndGoPushNotificationProperty.ifPresent( data -> {
                String jsonToCache = new Gson().toJson(scanAndGoPushNotificationProperty.get());
                CachedDataManager.saveJsonInCache(scanAndGoProperty, jsonToCache);
            } );

            return  scanAndGoPushNotificationProperty;
        } catch (AlgoliaException e) {
            LOG.warning(e.getMessage());
        }
        return Optional.empty();
    }

    public static String getMessageForItemsNotBilled(){
        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_MESSAGE_ITEMS_NOT_BILLED);

            if (jsonCachedOptional.isPresent()){
                //LOG.info("tiene cache getMessageForItemsNotBilled" );
                MessageForItemsNotBilled messageForItemsNotBilled = new Gson().fromJson(jsonCachedOptional.get(), MessageForItemsNotBilled.class);
                return messageForItemsNotBilled.getMessage();
            }

            Index<MessageForItemsNotBilled> indexconfig = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, MessageForItemsNotBilled.class);
            final Optional<MessageForItemsNotBilled> optionalMessageForItemsNotBilled = indexconfig.getObject(URLConnections.ALGOLIA_MESSAGE_ITEMS_NOT_BILLED);
            //LOG.info("No tiene cache getMessageForItemsNotBilled" );
            optionalMessageForItemsNotBilled.ifPresent( data -> {
                String jsonToCache = new Gson().toJson(optionalMessageForItemsNotBilled.get());
                CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_MESSAGE_ITEMS_NOT_BILLED, jsonToCache);
            } );
            return optionalMessageForItemsNotBilled.get().getMessage();
        } catch (AlgoliaException e) {
            LOG.warning(e.getMessage());
        }
        return "";
    }

    public static Optional<DeliveryFast> getDeliveryFastProperties(String deliveryFastProperties) {
      //LOG.info("method getDeliveryFastProperties INIT");
      try {
          Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(deliveryFastProperties);

          if (jsonCachedOptional.isPresent()){
              //LOG.info("tiene cache getDeliveryFastProperties" );
              return Optional.of(new Gson().fromJson(jsonCachedOptional.get(), DeliveryFast.class));
          }
          Index<DeliveryFast> deliveryFastIndex = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, DeliveryFast.class);
          Optional<DeliveryFast> deliveryFast = deliveryFastIndex.getObject(deliveryFastProperties);
          //LOG.info("No tiene cache getDeliveryFastProperties" );
          deliveryFast.ifPresent( data -> {
              String jsonToCache = new Gson().toJson(deliveryFast.get());
              CachedDataManager.saveJsonInCache(deliveryFastProperties, jsonToCache);
          } );

          return deliveryFast;
      }catch (AlgoliaException e){
          LOG.warning(e.getMessage());
      }
      return Optional.empty();
    }

    public static RestrictionItemConfig getRestrictionQuantityItems() {

        Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache("RESTRICTION.QUANTITY.CART");

        if (jsonCachedOptional.isPresent()){
            //LOG.info("tiene cache getRestrictionQuantityItems" );
            return new Gson().fromJson(jsonCachedOptional.get(), RestrictionItemConfig.class);
        }
	    Index<RestrictionItemConfig> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, RestrictionItemConfig.class);
        try {
            Optional<RestrictionItemConfig> optionalRestrictionQuantity = index.getObject("RESTRICTION.QUANTITY.CART");
            //LOG.info("No tiene cache getRestrictionQuantityItems" );
            optionalRestrictionQuantity.ifPresent( data -> {
                String jsonToCache = new Gson().toJson(optionalRestrictionQuantity.get());
                CachedDataManager.saveJsonInCache("RESTRICTION.QUANTITY.CART", jsonToCache);
            } );

            if (optionalRestrictionQuantity.isPresent()){
	            //LOG.info("method() getRestrictionQuantityItems Data found -> " + optionalRestrictionQuantity.get().toString());
                return optionalRestrictionQuantity.get();
            }

        }catch (AlgoliaException e) {
            LOG.warning(e.getMessage());
        }
        return new RestrictionItemConfig();
    }

    public static Optional<DeliveryFast> getDeliveryFastProperties(){
        //LOG.info("method getDeliveryFastProperties INIT");
        return getDeliveryFastProperties(URLConnections.ALGOLIA_DELIVERY_FAST);
    }

    public static Optional<TimeFinalizeOrders> getTimeFinalizeOrder(){
      //LOG.info("method: getTimeFinalizeOrder INIT");
      try {
          Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_TIME_FINALIZE_ORDERS);

          if (jsonCachedOptional.isPresent()){
              //LOG.info("tiene cache getTimeFinalizeOrder" );
              return Optional.of(new Gson().fromJson(jsonCachedOptional.get(), TimeFinalizeOrders.class));
          }
          Index<TimeFinalizeOrders> timeFinalizeOrdersIndex = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, TimeFinalizeOrders.class);
          Optional<TimeFinalizeOrders> timeFinalizeOrdersOptional = timeFinalizeOrdersIndex.getObject(URLConnections.ALGOLIA_TIME_FINALIZE_ORDERS);
          //LOG.info("No tiene cache getTimeFinalizeOrder" );
          timeFinalizeOrdersOptional.ifPresent( data -> {
              String jsonToCache = new Gson().toJson(timeFinalizeOrdersOptional.get());
              CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_TIME_FINALIZE_ORDERS, jsonToCache);
          } );

          return timeFinalizeOrdersOptional;
      }catch (AlgoliaException e){
          LOG.warning(e.getMessage());
      }
      return Optional.empty();
    }

    public static AutocompleteByCityConfig getAutocompleteConfig(){

        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_AUTOCOMPLETE_CONFIG);

            if (jsonCachedOptional.isPresent()){
                //LOG.info(" tiene cache getAutocompleteConfig" );
                return new Gson().fromJson(jsonCachedOptional.get(),AutocompleteByCityConfig.class);
            }

            Index<AutocompleteByCityConfig> config = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, AutocompleteByCityConfig.class);
            Optional<AutocompleteByCityConfig> configOptional = config.getObject(URLConnections.ALGOLIA_AUTOCOMPLETE_CONFIG);
            //LOG.info("No tiene cache getAutocompleteConfig" );
            configOptional.ifPresent( data -> {
                String jsonToCache = new Gson().toJson(configOptional.get());
                CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_AUTOCOMPLETE_CONFIG, jsonToCache);
            } );
            return configOptional.get();
        }catch (AlgoliaException e){
            LOG.warning(e.getMessage());
        }
        return null;
    }

    public static Optional<CustomerCallCenterAlgolia> getUsersCallCenter(){
      //LOG.info("method: getUsersCallCenter() INIT");
      try {
          Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_USERS_CALL_CENTER);

          if (jsonCachedOptional.isPresent()){
              //LOG.info(" tiene cache getUsersCallCenter" );
              return Optional.of(new Gson().fromJson(jsonCachedOptional.get(), CustomerCallCenterAlgolia.class));
          }

          Index<CustomerCallCenterAlgolia> usersCallCenterIndex = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, CustomerCallCenterAlgolia.class);
          Optional<CustomerCallCenterAlgolia> customerCallCenterOptional = usersCallCenterIndex.getObject(URLConnections.ALGOLIA_USERS_CALL_CENTER);
          //LOG.info("No tiene cache getUsersCallCenter" );
          customerCallCenterOptional.ifPresent( data -> {
              String jsonToCache = new Gson().toJson(customerCallCenterOptional.get());
              CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_USERS_CALL_CENTER, jsonToCache);
          } );

          return customerCallCenterOptional;
      }catch (AlgoliaException e){
          LOG.warning(e.getMessage());
      }
      return Optional.empty();
    }

    public static Optional<AlgoliaEmailConfig> getEmailConfigLogin(){
      try {
      Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_LOGIN_EMAIL_CONFIG);

      if (jsonCachedOptional.isPresent()){
          //LOG.info("tiene cache getEmailConfigLogin" );
          return Optional.of(new Gson().fromJson(jsonCachedOptional.get(), AlgoliaEmailConfig.class));
      }
        Index<AlgoliaEmailConfig> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES,AlgoliaEmailConfig.class);
          Optional<AlgoliaEmailConfig> algoliaEmailConfig = index.getObject(URLConnections.ALGOLIA_LOGIN_EMAIL_CONFIG);
          //LOG.info("No tiene cache getEmailConfigLogin" );
          algoliaEmailConfig.ifPresent( data -> {
              String jsonToCache = new Gson().toJson(algoliaEmailConfig.get());
              CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_LOGIN_EMAIL_CONFIG, jsonToCache);
          } );
        return algoliaEmailConfig;
      }catch (AlgoliaException e){
          LOG.severe(e.getMessage());
      }
      return Optional.empty();
    }

    public static String getMessagePasswordIncorrect(){
      try {
          Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_PASSWORD_INCORRECT);

          if (jsonCachedOptional.isPresent()){
              //LOG.info("tiene cache getMessagePasswordIncorrect" );
              MessageAlgolia messageAlgolia = new Gson().fromJson(jsonCachedOptional.get(), MessageAlgolia.class);
              return messageAlgolia.getMessage();
          }
          Index<MessageAlgolia> messageAlgoliaIndex = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, MessageAlgolia.class);
          Optional<MessageAlgolia> optionalMessageAlgolia = messageAlgoliaIndex.getObject(URLConnections.ALGOLIA_PASSWORD_INCORRECT);
          //LOG.info("No tiene cache getMessagePasswordIncorrect" );
          optionalMessageAlgolia.ifPresent( data -> {
              String jsonToCache = new Gson().toJson(optionalMessageAlgolia.get());
              CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_PASSWORD_INCORRECT, jsonToCache);
          } );
          return optionalMessageAlgolia.get().getMessage();
      }catch (AlgoliaException e){
          LOG.warning("Error getMessagePasswordIncorrect() " + e.getMessage());
      }
      return new String();
    }

    public static MessageAlgoliaCode getMessageCodeLogin(){
        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_CONFIG_LOGIN_MESSAGE);

            if (jsonCachedOptional.isPresent()){
                //LOG.info("tiene cache getMessageCodeLogin" );
                return new Gson().fromJson(jsonCachedOptional.get(), MessageAlgoliaCode.class);
            }
            Index<MessageAlgoliaCode> messageAlgoliaIndex = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, MessageAlgoliaCode.class);
            Optional<MessageAlgoliaCode> optionalMessageAlgoliaCode = messageAlgoliaIndex.getObject(URLConnections.ALGOLIA_CONFIG_LOGIN_MESSAGE);
            //LOG.info("No tiene cache getMessageCodeLogin" );
            optionalMessageAlgoliaCode.ifPresent( data -> {
                String jsonToCache = new Gson().toJson(optionalMessageAlgoliaCode.get());
                CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_CONFIG_LOGIN_MESSAGE, jsonToCache);
            } );
            return optionalMessageAlgoliaCode.get();
        }catch (AlgoliaException e){
            LOG.warning("Error getMessagePasswordIncorrect() " + e.getMessage());
        }
        return new MessageAlgoliaCode();
    }

    /**
     * Obtener la configuracion de TimeOut desde Algolia.
     * */
    public static Optional<TimeOut> getTimeOutConfig(){
        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_CONFIG_TIMEOUT);

            if (jsonCachedOptional.isPresent()){
                //LOG.info("tiene cache getTimeOutConfig" );
                return Optional.of(new Gson().fromJson(jsonCachedOptional.get(), TimeOut.class));
            }
            Index<TimeOut> TimeOutConfigIndex = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, TimeOut.class);
            //LOG.info("getTimeOutConfig.getName()=>"+TimeOutConfigIndex.getName());
            Optional<TimeOut> optional = TimeOutConfigIndex.getObject(URLConnections.ALGOLIA_CONFIG_TIMEOUT);
            //LOG.info("No tiene cache getTimeOutConfig" );
            optional.ifPresent( data -> {
                String jsonToCache = new Gson().toJson(optional.get());
                CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_CONFIG_TIMEOUT, jsonToCache);
            } );
            return optional;
        } catch (AlgoliaException e) {
            LOG.severe(e.getMessage());
        }
        return Optional.empty();
    }

    public static Optional<OfferComboPopUp> getAlgoliaConfigPopUp(final String algoliaConfigPopUp){
        Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(algoliaConfigPopUp);

        if (jsonCachedOptional.isPresent()){
            //LOG.info("tiene cache getAlgoliaConfigPopUp" );
            return Optional.of(new Gson().fromJson(jsonCachedOptional.get(), OfferComboPopUp.class));
        }
        Index<OfferComboPopUp> itemOffer = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, OfferComboPopUp.class);
        try {
            Optional<OfferComboPopUp> optional = itemOffer.getObject(algoliaConfigPopUp);
            //LOG.info("No tiene cache getAlgoliaConfigPopUp" );
            optional.ifPresent( data -> {
                String jsonToCache = new Gson().toJson(optional.get());
                CachedDataManager.saveJsonInCache(algoliaConfigPopUp, jsonToCache);
            } );
            return optional;
        } catch (AlgoliaException e) {
            e.printStackTrace();
        }
        return Optional.empty();

    }


    /**
     * Autocomplete con Algolia.
     * Ver index: https://www.algolia.com/apps/VCOJEYD2PO/explorer/browse/data_autocomplete
     * */
    public static List<PlaceAlgolia> findPlaceInAlgolia(final String city, final String placeDescription) {
        //LOG.info("Called findPlaceInAlgolia()");
        List<PlaceAlgolia> results = new ArrayList<>();
        try {
//            Index<PlaceAlgolia> index = algoliaClient.initIndex(URLConnections.ALGOLIA_DATA_AUTOCOMPLETE, PlaceAlgolia.class);

            // Busqueda
//            SearchResult<PlaceAlgolia> search = index.search( new Query( placeDescription + " " + city) );
            List<IndexQuery> queries = new ArrayList<>();
            queries.add(new IndexQuery(URLConnections.ALGOLIA_DATA_AUTOCOMPLETE, new Query( placeDescription + " " + city)));
            SearchResult<?> search = algoliaClient.multipleQueries(queries).getResults().get(0);
            // Validacion
            if (search.getHits() != null && !search.getHits().isEmpty()) {
                //LOG.info("Encontro el lugar en Algolia. Hemos ahorrado un API CALL mas.");
                return getPlaceAlgoliaFromMultiqueries(search);
            }

            return results;

        } catch (AlgoliaException e) {
            LOG.warning(e.getMessage());
            return results;
        }
    }

    public static void saveNewAutocompleteResult(final GAutocompleteRes resultToSave, final String city, final String provider) {
        try {
            LOG.info("called saveNewAutocompleteResult("+city+","+provider+")");
            if (Objects.nonNull(resultToSave) && Objects.nonNull(resultToSave.getPredictions()) && !resultToSave.getPredictions().isEmpty()
                && Objects.nonNull(city) && Objects.nonNull(provider)) {

                List<PlaceAlgolia> places = new ArrayList<>();

                for (int i = 0; i < resultToSave.getPredictions().size(); i++) {
                    //LOG.info("Place " + i);
                    GPlacePredictionRes predictionRes = resultToSave.getPredictions().get(i);
                    PlaceAlgolia newPlace = new PlaceAlgolia();

                    newPlace.setCity(city);
                    newPlace.setDescription(predictionRes.getDescription());
                    newPlace.setId(provider);
                    newPlace.setObjectID(predictionRes.getPlaceId());
                    newPlace.setPlaceId(predictionRes.getPlaceId());

                    places.add(newPlace);
                }

                LOG.info("IF (!places.isEmpty()) : [" + (!places.isEmpty()) + "]");
                if (!places.isEmpty()) {
                    Index<PlaceAlgolia> index = algoliaClient.initIndex(URLConnections.ALGOLIA_DATA_AUTOCOMPLETE, PlaceAlgolia.class);
                    index.saveObjects(places);
                    //LOG.info("Places Saved!!");
                }
            }
        }catch (Exception e) {
            LOG.warning(e.getMessage());
        }
    }

    /**
     * Ver index: https://www.algolia.com/apps/VCOJEYD2PO/explorer/browse/data_geocoding
     * */
    public static AddressAlgolia findAddressInAlgoliaByCityAndAddress(final String cityId, final String city, final String address) {
        // LOG.info("Called findAddressInAlgoliaByCoordsOrAddress("+cityId+","+city+","+address+")");
        try {
//            Index<AddressAlgolia> index = algoliaClient.initIndex(URLConnections.ALGOLIA_DATA_GEOCODING, AddressAlgolia.class);

            // Busqueda
//            SearchResult<AddressAlgolia> search = index.search( new Query( cityId + " " + city + " " + address.replace("#","")) );
            List<IndexQuery> queries = new ArrayList<>();
            queries.add(new IndexQuery(URLConnections.ALGOLIA_DATA_GEOCODING, new Query( cityId + " " + city + " " + address.replace("#",""))));
            List<SearchResult<?>> results = algoliaClient.multipleQueries(queries).getResults();

            // Validacion
//            if (search.getHits() != null && !search.getHits().isEmpty()) {
            if (results.get(0).getHits() != null && !results.get(0).getHits().isEmpty()) {
                // LOG.info("Encontro la direccion en Algolia. Hemos ahorrado un API CALL mas.");
//                return search.getHits().get(0);
                return getAddressAlgoliaFromMultiqueries(results.get(0));
            }

            return null;

        } catch (AlgoliaException e) {
            LOG.warning(e.getMessage());
            return null;
        }
    }


    /**
     * Ver index: https://www.algolia.com/apps/VCOJEYD2PO/explorer/browse/data_geocoding
     * */
    public static AddressAlgolia findAddressInAlgoliaByLatAndLon(final double latitude, final double longitude) {
        // LOG.info("Called findAddressInAlgoliaByLatAndLon(" + latitude + "," + longitude + ")");
        try {
//            Index<AddressAlgolia> index = algoliaClient.initIndex(URLConnections.ALGOLIA_DATA_GEOCODING, AddressAlgolia.class);

            String _latitude = ""+latitude;
            String _longitude = ""+longitude;

            if (_latitude.length() > 6) {
                _latitude = _latitude.substring(0,6);
            }

            if (_longitude.length() > 6) {
                _longitude = _longitude.substring(0,6);
            }

            // Busqueda
//            SearchResult<AddressAlgolia> search = index.search( new Query( _latitude + " " + _longitude ) );
            List<IndexQuery> queries = new ArrayList<>();
            queries.add(new IndexQuery(URLConnections.ALGOLIA_DATA_GEOCODING, new Query( _latitude + " " + _longitude )));
            List<SearchResult<?>> results = algoliaClient.multipleQueries(queries).getResults();
            // Validacion
//            if (search.getHits() != null && !search.getHits().isEmpty()) {
            if (results.get(0).getHits() != null && !results.get(0).getHits().isEmpty()) {
                // LOG.info("Encontro la direccion en Algolia. Hemos ahorrado un API CALL mas.");
//                return search.getHits().get(0);
                return getAddressAlgoliaFromMultiqueries(results.get(0));
            }

            return null;

        } catch (AlgoliaException e) {
            LOG.warning(e.getMessage());
            return null;
        }
    }

    /**
     * Obtiene la lista de items vistos por el usuario
     * @param idUser
     * @return Optional<HistoryUser>
     */

    public static Optional<HistoryUser> getHistoryByUserId(final String idUser){
        //LOG.info("Try getHistoryByUserId -> user: " + idUser);
        try {
            Index<HistoryUser> indexHistoryUser = algoliaClient.initIndex(URLConnections.ALGOLIA_BROWSE_HISTORY, HistoryUser.class);
            //LOG.info("Try getHistoryByUserId -> user: " + indexHistoryUser);
            return indexHistoryUser.getObject(idUser);
        }catch (Exception e) {
            LOG.warning("Error in APIAlgolia.getHistoryByUserId. Message:"+ Arrays.toString(e.getStackTrace()));
        }
        return Optional.empty();
    }


    public static void uploadItemToHistory(final String idUser, final Long itemId, Long idStoreGroup){
        //LOG.info("Try Upload uploadItemToHistory -> user: " + idUser + " , item: " + itemId + " , idStoreGroup: " + idStoreGroup );
        try {

            Index<HistoryUser> indexHistoryUser = algoliaClient.initIndex(URLConnections.ALGOLIA_BROWSE_HISTORY, HistoryUser.class);
            Optional<HistoryUser> optionalHistoryUser = indexHistoryUser.getObject(idUser);

            //getItemAlgoliaById(itemId, idStoreGroup);
            if (Objects.requireNonNull(getItemAlgoliaById(itemId, idStoreGroup)).getStock() > 0) {
                if (optionalHistoryUser.isPresent()) {
                    HistoryUser historyUser = optionalHistoryUser.get();
                    if (historyUser.getItems() != null && historyUser.getObjectID() != null) {
                        if (!historyUser.getItems().contains(itemId)) {
                            //LOG.info("Try Upload uploadItemToHistory ->  " + itemId);
                            historyUser.getItems().add(0, itemId);

                            if (historyUser.getItems().size() > MAX_ITEMS_HISTORY) {
                                historyUser.getItems().remove(historyUser.getItems().size() - 1);
                            }

                            indexHistoryUser.saveObject(historyUser.getObjectID(), historyUser);
                        } else {
                            LOG.info("Try Upload uploadItemToHistory item ya agregado: " + itemId);
                        }

                    }
                } else {

                    HistoryUser historyUser = new HistoryUser();
                    historyUser.setObjectID(idUser);
                    List<Long> items = new ArrayList<>();
                    items.add(itemId);
                    historyUser.setItems(items);
                    indexHistoryUser.saveObject(idUser, historyUser);

                }

            }

        }catch (Exception e) {
            LOG.warning("Error in APIAlgolia.uploadItemToHistory. Message:"+ Arrays.toString(e.getStackTrace()));
        }


    }

    public static List<CategorySeo> getCategoriesSeo(List<String> ids){
        try {
            //LOG.warning("method getCategoriesSeo INIT ");
            Index<CategorySeo> index = algoliaClient.initIndex(URLConnections.ALGOLIA_CATEGORIES_SEO, CategorySeo.class);
            List<CategorySeo> categorySEOList = new ArrayList();
            if (Objects.nonNull(ids) && !ids.isEmpty()) {
                //LOG.warning("method getCategoriesSeo: Busqueda con ids: " + ids.size());
                categorySEOList = index.getObjects(ids).stream().filter(Objects::nonNull).collect(Collectors.toList());
            }
            return categorySEOList;
        } catch (AlgoliaException e) {
            LOG.warning("Error in APIAlgolia.getCategoriesSeo. Message:"+ Arrays.toString(e.getStackTrace()));
            return null;
        }
    }


    // cached
    public static Optional<ChargeOrderAlgolia> getChargeOrderActive(){
        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_CHARGE_ORDER_ACTIVE);

            if (jsonCachedOptional.isPresent()){
                //LOG.info("Tiene cache: getChargeOrderActive");
                return Optional.of(new Gson().fromJson(jsonCachedOptional.get(),ChargeOrderAlgolia.class));
            }
            Index<ChargeOrderAlgolia> chargeOrderAlgoliaIndex = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, ChargeOrderAlgolia.class);
            Optional<ChargeOrderAlgolia> optional = chargeOrderAlgoliaIndex.getObject(URLConnections.ALGOLIA_CHARGE_ORDER_ACTIVE);

            //LOG.info("No tiene cache: getChargeOrderActive");
            optional.ifPresent( data -> {
                String jsonToCache = new Gson().toJson(optional.get());
                CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_CHARGE_ORDER_ACTIVE, jsonToCache);
            } );

            return optional;
        } catch (AlgoliaException e) {
            LOG.severe(e.getMessage());
        }
        return Optional.empty();
    }

    //chached
public static Optional<CouponPopUpData> getCouponPopUp(String couponKey) {
    try {
        Optional<CouponAlgoliaPopUp> optionalCouponAlgoliaPopUp;
        Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_COUPON_POPUP);

        if (jsonCachedOptional.isPresent()){
            //LOG.info("Tiene cache: getCouponPopUp");
            optionalCouponAlgoliaPopUp = Optional.of(new Gson().fromJson(jsonCachedOptional.get(), CouponAlgoliaPopUp.class));
        }else {
            Index<CouponAlgoliaPopUp> couponPopUpIndex = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, CouponAlgoliaPopUp.class);
            //LOG.info("couponPopUpIndex ()=> " + couponPopUpIndex.getName());
            //Optional<CouponAlgoliaPopUp> optionalCouponAlgoliaPopUp = null;
            optionalCouponAlgoliaPopUp = couponPopUpIndex.getObject(URLConnections.ALGOLIA_COUPON_POPUP);
            //LOG.info("No tiene cache: getCouponPopUp");
            optionalCouponAlgoliaPopUp.ifPresent( data -> {
                String jsonToCache = new Gson().toJson(optionalCouponAlgoliaPopUp.get());
                CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_COUPON_POPUP, jsonToCache);
            } );
        }

        //LOG.info("optionalCouponAlgoliaPopUp ()=> " + optionalCouponAlgoliaPopUp.isPresent());
/*            Optional<CouponPopUpAlgolia> result = optional.isPresent() ?
            Objects.requireNonNull(optional.get().getCouponsToValidate()).stream().filter(config -> config.getCouponKey().equals(couponKey)).findFirst() : null;*/
        if(optionalCouponAlgoliaPopUp.isPresent() && optionalCouponAlgoliaPopUp.get().getCouponsToValidate() != null){
            CouponAlgoliaPopUp couponAlgoliaPopUp = optionalCouponAlgoliaPopUp.get();
            //LOG.info("couponAlgoliaPopUp --> " + couponAlgoliaPopUp);
            return couponAlgoliaPopUp.getCouponsToValidate().stream().filter(coupon -> coupon.getCouponKey().equals(couponKey)).findFirst();

        }else {
            LOG.info("No se pudo obtener la data del object");
        }

    } catch (AlgoliaException e) {
        e.printStackTrace();
    }
    return Optional.empty();
}


    public static ItemAlgolia getItemAlgoliaScanAndGo(final String objectId) {
        try {
            Index<ItemAlgolia> index = algoliaClient.initIndex(URLConnections.ALGOLIA_PRODUCTS_SCAN_AND_GO, ItemAlgolia.class);
            ItemAlgolia itemAlgolia = index.getObject(objectId).orElse(null);
            return itemAlgolia;
        } catch (AlgoliaException | NoSuchElementException e) {
            e.printStackTrace();
        }
        return null;
    }

    //cached
    public static String getAppVersionConfig(String version, String platform) {

        //LOG.info("Version ->" + version);
        //LOG.info("Platform ->" + platform);
        String componentType = "";
        try {

            AppsVersionConfig propertiesAppsVersion;

            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_APPS_VERSION_CONFIG);

            if (jsonCachedOptional.isPresent()){
                //LOG.info("Tiene cache: getAppVersionConfig");
                propertiesAppsVersion = new Gson().fromJson(jsonCachedOptional.get(), AppsVersionConfig.class);

            }else {
                //LOG.info("No tiene cache: getAppVersionConfig");
                Index<AppsVersionConfig> indexStores = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, AppsVersionConfig.class);
                propertiesAppsVersion = indexStores.getObject(URLConnections.ALGOLIA_APPS_VERSION_CONFIG).get();

                String jsonToCache = new Gson().toJson(propertiesAppsVersion);
                CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_CHARGE_ORDER_ACTIVE, jsonToCache);
            }

            if (Objects.isNull(version) || Objects.isNull(platform)) {
                componentType = getDefaultComponentType(propertiesAppsVersion);
            } else {
                for (VersionConfig versionConfig : propertiesAppsVersion.getVersions()) {
                    if (versionConfig.getPlatform().equals(platform)) {
                        if(checkVersion(versionConfig.getVersion(), version)){
                            componentType = versionConfig.getComponentType();
                        } else {
                            componentType = getDefaultComponentType(propertiesAppsVersion);
                        }
                    }
                }
            }
        } catch (AlgoliaException e) {
            LOG.warning("Error in APIAlgolia.getAppVersionConfig. Message:" + Arrays.toString(e.getStackTrace()));
            LOG.warning("Error Message:" + e.getMessage());
        }
        //LOG.info("Return - >"+ componentType);
        return componentType;
    }

    /**
     * Return default componentType
     * @param propertiesAppsVersion
     * @return
     */
    private static String getDefaultComponentType(AppsVersionConfig  propertiesAppsVersion){
        for (VersionConfig versionConfig : propertiesAppsVersion.getVersions()) {
            if (versionConfig.getPlatform().equals("DEFAULT")) {
                return versionConfig.getComponentType();
            }
        }
        return "";
    }

    /**
     * Validates if the version is greater than or equal
     * @param algolia
     * @param version
     * @return
     */
    private static boolean checkVersion(String algolia, String version) {

        String[] algoliaN = algolia.split("\\.");
        String[] versionN = version.split("\\.");

        boolean response = false;

        for (int i = 0; i < algoliaN.length; i++) {

            int numberAlgolia;
            int numberVersion;
            try {
                numberAlgolia = Integer.parseInt(algoliaN[i]);
                numberVersion = Integer.parseInt(versionN[i]);
            } catch (Exception e) {
                return (i+1 <= algoliaN.length) ? false : response;
            }

            if (numberVersion >= numberAlgolia) {
                response = true;
            } else {
                return false;
            }
        }
        return response;
    }

    // cached
    public static List<Department> getUrlDepartments(){
        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_APPS_URL_DEPARTMENTS);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            if (jsonCachedOptional.isPresent()){
                try {
                //LOG.info("Tiene cache: getUrlDepartments");
                    DepartmentsAlgolia departmentsAlgolia = objectMapper.readValue(jsonCachedOptional.get(), DepartmentsAlgolia.class);
                    Optional<DepartmentsAlgolia> departmentsAlgoliaOptional = Optional.of(departmentsAlgolia);
                    return departmentsAlgoliaOptional.get().getUrlDepartments();
                } catch(Exception e) {
                    // error
                }
            }

            Index<DepartmentsAlgolia> indexStores = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES,DepartmentsAlgolia.class);
            Optional<DepartmentsAlgolia> departmentsAlgolia = indexStores.getObject(URLConnections.ALGOLIA_APPS_URL_DEPARTMENTS);

            //LOG.info("No tiene cache: getUrlDepartments");
            departmentsAlgolia.ifPresent(data -> {
//                String jsonToCache = new Gson().toJson(departmentsAlgolia.get());
                String jsonToCache = null;
                try {
                    jsonToCache = objectMapper.writeValueAsString(departmentsAlgolia.get());
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_APPS_URL_DEPARTMENTS, jsonToCache);
            });

            return departmentsAlgolia.get().getUrlDepartments();
        }catch (AlgoliaException e) {
            LOG.warning("Error in APIAlgolia.get. Message:" + Arrays.toString(e.getStackTrace()));
            LOG.warning("Error Message:" + e.getMessage());
            return null;
        }

    }

    public static List<PrimePlan> primeConfig(){
        try {

            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_FTD_PRIME_CONFIG);

            if (jsonCachedOptional.isPresent()){
                //LOG.info("tiene cache getTimeOutConfig" );
                return new Gson().fromJson(jsonCachedOptional.get(), PrimeConfigV2.class).getPrimePlans();
            }

            Index<PrimeConfigV2> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES,PrimeConfigV2.class);
            PrimeConfigV2 primeConfigRes = index.getObject(URLConnections.ALGOLIA_FTD_PRIME_CONFIG).get();

            if (primeConfigRes.getPrimePlans() != null){
                String jsonToCache = new Gson().toJson(primeConfigRes);
                CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_FTD_PRIME_CONFIG, jsonToCache);
            }

            return primeConfigRes.getPrimePlans();
        }catch (AlgoliaException e) {
            LOG.warning("Error in APIAlgolia.get. Message:" + Arrays.toString(e.getStackTrace()));
            LOG.warning("Error Message:" + e.getMessage());
            return null;
        }
    }

    public static PrimeConfig primeConfigV2(){
        try {
            Index<PrimeConfig> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES,PrimeConfig.class);
            PrimeConfig primeConfigRes = index.getObject(URLConnections.ALGOLIA_FTD_PRIME_CONFIG).get();

            return primeConfigRes;
        }catch (AlgoliaException e) {
            LOG.warning("Error in APIAlgolia.get. Message:" + Arrays.toString(e.getStackTrace()));
            LOG.warning("Error Message:" + e.getMessage());
            return null;
        }
    }

    /**
     * Update or Create if not exists
     * @param itemAlgolia
     */
    public static void updateItemAlgolia(ItemAlgolia itemAlgolia){
        try {
            Index<ItemAlgolia> indexProducts = algoliaClient.initIndex(URLConnections.ALGOLIA_PRODUCTS, ItemAlgolia.class);

            if(Objects.nonNull(itemAlgolia)){

                indexProducts.partialUpdateObjects(Arrays.asList(itemAlgolia),new RequestOptions() );
                LOG.info("method updateItemAlgolia objectID" + itemAlgolia.getObjectID());
            }

        } catch (AlgoliaException e) {
            LOG.warning("Error in APIAlgolia.updateItemAlgolia. Message:"+ Arrays.toString(e.getStackTrace()));
        }
    }

    public static List<ItemAlgolia> itemsByCity(String city, String textItem) throws AlgoliaException {

        Index<PropertiesStoresByCity> indexStores = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES,PropertiesStoresByCity.class);
        PropertiesStoresByCity propertiesStoresByCity = indexStores.getObject(URLConnections.ALGOLIA_DEFAULT_STORES_BY_CITY).get();
        String idStore = null;
        for(PropertyCityStore propertyCityStore:propertiesStoresByCity.getDefaultStores()){
            if(city.equalsIgnoreCase(propertyCityStore.getId()) || city.equalsIgnoreCase(propertyCityStore.getGeoCityCode())){
                idStore = propertyCityStore.getDefaultStore();
            }
        }

        if(idStore != null){
            //LOG.info(idStore);
            Index<ItemAlgolia> indexProducts = algoliaClient.initIndex(URLConnections.ALGOLIA_PRODUCTS, ItemAlgolia.class);
            SearchResult<ItemAlgolia> search = indexProducts.search(new Query()
                    .setQuery(textItem)
                    .setFilters("idStoreGroup=" + idStore));
            return search.getHits();
        }

        return new ArrayList<>();
    }

    public static List<ItemAlgolia> getFlashOffers(){
        try {
//            Index<ItemAlgolia> indexHighlight= algoliaClient.initIndex(URLConnections.ALGOLIA_PRODUCTS, ItemAlgolia.class);
            List<IndexQuery> queries = new ArrayList<>();
//            SearchResult<ItemAlgolia> search = indexHighlight.search(new Query("").setFilters("isFlashOffer:true"));
            queries.add(new IndexQuery(URLConnections.ALGOLIA_PRODUCTS, new Query("").setFilters("isFlashOffer:true")));
            List<SearchResult<?>> results = algoliaClient.multipleQueries(queries).getResults();
            return getItemListAlgoliaFromMultiqueriesHits(results.get(0));
            //return search.getHits();
        } catch (AlgoliaException e) {
            LOG.warning("Error in APIAlgolia.getFlashOffers. Message:"+ Arrays.toString(e.getStackTrace()));
            return null;
        }
    }

    public static boolean getDeleteCartConfig(){

        DeleteCartConfig deleteCartConfig = APIAlgolia.getProperty("DELETE.CART.CONFIG", DeleteCartConfig.class);

        if(Objects.nonNull(deleteCartConfig) && Objects.nonNull(deleteCartConfig.getActive())){
            return deleteCartConfig.getActive();
        }

        return false;
    }

    public static OptimalRouteStoresConfig getOptimalRouteConfigStores(){

        return getProperty("STORES.OPTIMALROUTE", OptimalRouteStoresConfig.class);
    }

    //cached
    public static SelfCheckoutAlgolia getSelfCheckout() {
        try {

            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_SELF_CHECKOUT);

            if (jsonCachedOptional.isPresent()){
                //LOG.info("Tiene cache: getSelfCheckout");
                return new Gson().fromJson(jsonCachedOptional.get(), SelfCheckoutAlgolia.class);
            }
            Index<SelfCheckoutAlgolia> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, SelfCheckoutAlgolia.class);
            Optional<SelfCheckoutAlgolia> selfCheckoutAlgolia = index.getObject(URLConnections.ALGOLIA_SELF_CHECKOUT);

            //LOG.info("No tiene cache: getSelfCheckout");

            selfCheckoutAlgolia.ifPresent(data -> {
                String jsonToCache = new Gson().toJson(selfCheckoutAlgolia.get());
                CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_SELF_CHECKOUT, jsonToCache);
            });

            return selfCheckoutAlgolia.orElseGet(SelfCheckoutAlgolia::new);

        } catch (AlgoliaException | NoSuchElementException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static SelfCheckoutAlgolia getFacets() {
        try {

            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_FACETS);

            if (jsonCachedOptional.isPresent()){
                //LOG.info("Tiene cache: getSelfCheckout");
                return new Gson().fromJson(jsonCachedOptional.get(), SelfCheckoutAlgolia.class);
            }
            Index<SelfCheckoutAlgolia> index = algoliaClient.initIndex(URLConnections.ALGOLIA_SUBSCRIBE_AND_SAVE_CONFIG_INDEX, SelfCheckoutAlgolia.class);
            Optional<SelfCheckoutAlgolia> selfCheckoutAlgolia = index.getObject(URLConnections.ALGOLIA_FACETS);

            //LOG.info("No tiene cache: getSelfCheckout");

            selfCheckoutAlgolia.ifPresent(data -> {
                String jsonToCache = new Gson().toJson(selfCheckoutAlgolia.get());
                CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_FACETS, jsonToCache);
            });

            return selfCheckoutAlgolia.orElseGet(SelfCheckoutAlgolia::new);

        } catch (AlgoliaException | NoSuchElementException e) {
            e.printStackTrace();
        }
        return null;
    }


    //cached
    public static WhatsAapSendMessageConfig getWhatsappConfigMessage() {
        try {

            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_WHATSAPP_CONFIG);

            if (jsonCachedOptional.isPresent()){
                //LOG.info("Tiene cache: getWhatsappConfigMessageCode");
                return new Gson().fromJson(jsonCachedOptional.get(), WhatsAapSendMessageConfig.class);
            }
            Index<WhatsAapSendMessageConfig> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, WhatsAapSendMessageConfig.class);
            Optional<WhatsAapSendMessageConfig> whatsaapSendMessageConfig = index.getObject(URLConnections.ALGOLIA_WHATSAPP_CONFIG);

            //LOG.info("No tiene cache: getWhatsappConfigMessageCode");

            whatsaapSendMessageConfig.ifPresent(data -> {
                String jsonToCache = new Gson().toJson(whatsaapSendMessageConfig.get());
                CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_WHATSAPP_CONFIG, jsonToCache);
            });

            return whatsaapSendMessageConfig.orElseGet(WhatsAapSendMessageConfig::new);

        } catch (AlgoliaException | NoSuchElementException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<ItemAlgolia> getItemsMultiquery(List<Long> idItems, Long idStoreGroup) throws AlgoliaException, IOException {
        if (Objects.isNull(idStoreGroup)) idStoreGroup = 26L;
        List<IndexQuery> queries = new ArrayList<>();

//        Index<ItemAlgolia> index = algoliaClient.initIndex(URLConnections.ALGOLIA_PRODUCTS, ItemAlgolia.class);
        for(Long id: idItems) {

            StringBuilder filterBufferBarcode = new StringBuilder();

            filterBufferBarcode
                    .append("(")
                    .append("id:'")
                    .append(id)
                    .append("'")
                    .append(")");
            // id store group
            filterBufferBarcode.append(" AND idStoreGroup:'").append(idStoreGroup).append("'");

            //LOG.info("filter algolia list ->  " + filterBufferBarcode.toString() );
            //SearchResult<ItemAlgolia> search = index.search(new Query("").setFilters("barcode:'"+barcode+"'  AND  idStoreGroup:'"+idStoreGroup+"'"));
            queries.add(new IndexQuery(URLConnections.ALGOLIA_PRODUCTS, new Query("").setFilters(filterBufferBarcode.toString())));
        }
        List<SearchResult<?>> results = algoliaClient.multipleQueries(queries).getResults();
        return results.stream().map(APIAlgolia::getItemAlgoliaFromMultiqueries).collect(Collectors.toList());
    }

    private static ItemAlgoliaStock getItemAlgoliaFromMultiqueriesStock(SearchResult<?> search) {
        if (search.getHits() != null && !search.getHits().isEmpty() && search.getHits().get(0) != null){
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                return objectMapper.convertValue(search.getHits().get(0), ItemAlgoliaStock.class);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private static ItemAlgolia getItemAlgoliaFromMultiqueries(SearchResult<?> search) {
        if (search.getHits() != null && !search.getHits().isEmpty() && search.getHits().get(0) != null){
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                return objectMapper.convertValue(search.getHits().get(0), ItemAlgolia.class);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private static List<ItemAlgolia> getItemListAlgoliaFromMultiqueriesHits(SearchResult<?> search) {
        if (search.getHits() != null && !search.getHits().isEmpty() && search.getHits().get(0) != null){
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            List<ItemAlgolia> items = new ArrayList<>();
            search.getHits().forEach(h -> {
                try {
                    items.add(objectMapper.convertValue(h, ItemAlgolia.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            return items;
        }
        return new ArrayList<>();
    }

    private static List<Holiday> getHolidaysFromMultiqueries(SearchResult<?> search) {
        if (search.getHits() != null && !search.getHits().isEmpty() && search.getHits().get(0) != null){
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                return search.getHits().stream().map(h -> objectMapper.convertValue(h, Holiday.class)).collect(Collectors.toList());
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private static AddressAlgolia getAddressAlgoliaFromMultiqueries(SearchResult<?> search) {
        if (search.getHits() != null && !search.getHits().isEmpty() && search.getHits().get(0) != null){
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                return objectMapper.convertValue(search.getHits().get(0), AddressAlgolia.class);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private static List<VideoData> getVideoDataFromMultiqueries(SearchResult<?> search) {
        if (search.getHits() != null && !search.getHits().isEmpty() && search.getHits().get(0) != null){
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                return search.getHits().stream().map(h -> objectMapper.convertValue(h, VideoData.class)).collect(Collectors.toList());
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
//                    PlaceAlgolia
    private static DeliveryTypeTime getDeliveryTypeTimeFromMultiqueries(SearchResult<?> search) {
        if (search.getHits() != null && !search.getHits().isEmpty() && search.getHits().get(0) != null){
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                return objectMapper.convertValue(search.getHits().get(0), DeliveryTypeTime.class);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private static List<AdvisedItem> getAdvisedItemFromMultiqueries(SearchResult<?> search) {
        if (search.getHits() != null && !search.getHits().isEmpty() && search.getHits().get(0) != null){
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                return search.getHits().stream().map(h -> objectMapper.convertValue(h, AdvisedItem.class)).collect(Collectors.toList());
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private static List<CategoriaAlgolia> getCategoriaAlgoliaFromMultiqueries(SearchResult<?> search) {
        if (search.getHits() != null && !search.getHits().isEmpty() && search.getHits().get(0) != null){
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                return search.getHits().stream().map(h -> objectMapper.convertValue(h, CategoriaAlgolia.class)).collect(Collectors.toList());
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private static List<PlaceAlgolia> getPlaceAlgoliaFromMultiqueries(SearchResult<?> search) {
        if (search.getHits() != null && !search.getHits().isEmpty() && search.getHits().get(0) != null){
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                return search.getHits().stream().map(h -> objectMapper.convertValue(h, PlaceAlgolia.class)).collect(Collectors.toList());
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }


    private static List<OriginProperties> getOriginPropertiesFromMultiqueries(SearchResult<?> search) {
        if (search.getHits() != null && !search.getHits().isEmpty() && search.getHits().get(0) != null){
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                return search.getHits().stream().map(h -> objectMapper.convertValue(h, OriginProperties.class)).collect(Collectors.toList());
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    /**
     * get flags of registries
     * @return
     */
    public static FlagRegistry getFlagRegistry() {
        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_FLAG_REGISTRY);
            if (jsonCachedOptional.isPresent()) {
                //LOG.info("tiene cache getFlagRegistry");
                return new Gson().fromJson(jsonCachedOptional.get(), FlagRegistry.class);
            }
            //LOG.info("No tiene cache getFlagRegistry");
            Index<FlagRegistry> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, FlagRegistry.class);
            Optional<FlagRegistry> configAlgoliaOptional = index.getObject(URLConnections.ALGOLIA_FLAG_REGISTRY);
            FlagRegistry flagRegistry = new FlagRegistry();
            if (configAlgoliaOptional.isPresent()) {
                List<FlagCountries> countries = configAlgoliaOptional.get().getCountries().stream()
                        .filter(FlagCountries::isActive).collect(Collectors.toList());
                flagRegistry.setCountries(countries);
                String jsonToCache = new Gson().toJson(flagRegistry);
                CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_FLAG_REGISTRY, jsonToCache);
            }
            return flagRegistry;
        } catch (AlgoliaException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Optional<DeliveryFree> getFreeDelivery() {
        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_FREE_DELIVERY);

            if (jsonCachedOptional.isPresent()){
                //LOG.info("tiene cache filter config" );
                return Optional.of(new Gson().fromJson(jsonCachedOptional.get(), DeliveryFree.class));
            }
            Index<DeliveryFree> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, DeliveryFree.class);
            Optional<DeliveryFree> deliveryFree = index.getObject(URLConnections.ALGOLIA_FREE_DELIVERY);

            //LOG.info("No tiene cache cupons filter config" );
            deliveryFree.ifPresent( data -> {
                String jsonToCache = new Gson().toJson(deliveryFree.get());
                CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_FREE_DELIVERY, jsonToCache);
            } );


            return deliveryFree;

        } catch (AlgoliaException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }


    private static void saveHolidaysInCache() throws JsonProcessingException {
        Index<Holiday> indexHolidays = algoliaClient.initIndex(URLConnections.ALGOLIA_HOLIDAYS, Holiday.class);
        List<Holiday> holidays = indexHolidays.browse(new Query()).stream().collect(Collectors.toList());
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        CachedDataManager.saveHolidaysInCache(objectMapper.writeValueAsString(holidays));
    }

    private static boolean isTodayHolidaysFromCache (Date date, String jsonHolidays)  {
        try {
            List<Holiday> holidays = Arrays.stream(new Gson().fromJson(jsonHolidays, Holiday[].class)).collect(Collectors.toList());
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            SimpleDateFormat format1 = new SimpleDateFormat("yyyy.MM.dd");
            return holidays.stream().anyMatch(h -> h.getObjectID().equals(format1.format(cal.getTime())));
        } catch(Exception e) {
            return false;
        }
    }

    public static Optional<MessageSmsConfig> getMessageSms() {
        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_SMS_MESSAGGES);

            if (jsonCachedOptional.isPresent()){
                //LOG.info("tiene sms config" );
                return Optional.of(new Gson().fromJson(jsonCachedOptional.get(), MessageSmsConfig.class));
            }
            Index<MessageSmsConfig> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, MessageSmsConfig.class);
            Optional<MessageSmsConfig> couponFiltersConfig = index.getObject(URLConnections.ALGOLIA_SMS_MESSAGGES);

            //LOG.info("No tiene cache tiene sms config" );
            couponFiltersConfig.ifPresent( data -> {
                String jsonToCache = new Gson().toJson(couponFiltersConfig.get());
                CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_SMS_MESSAGGES, jsonToCache);
            } );

            return couponFiltersConfig;

        } catch (AlgoliaException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public static OrderMessageConfiguration getNotificationOrderMessage() throws AlgoliaException {

        Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_ORDER_MESSAGES);
        Optional<OrderMessageConfiguration> optionalMessageOrderAlgolia;
        // algolia
        if (!jsonCachedOptional.isPresent()){
            Index<OrderMessageConfiguration> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, OrderMessageConfiguration.class);
            optionalMessageOrderAlgolia = index.getObject(URLConnections.ALGOLIA_ORDER_MESSAGES);

            if (optionalMessageOrderAlgolia.isPresent()){
                String json = new Gson().toJson(optionalMessageOrderAlgolia.get());
                CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_ORDER_MESSAGES, json);
            }
            return optionalMessageOrderAlgolia.orElse(new OrderMessageConfiguration());
        }
        // cache
        return  new Gson().fromJson(jsonCachedOptional.get(), OrderMessageConfiguration.class);
    }

    public static Optional<CarruselRecommendConfig> getCarruselRecommendConfig() {
        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_CARRUSEL_RECOMMEND);

            if (jsonCachedOptional.isPresent()){
                return Optional.of(new Gson().fromJson(jsonCachedOptional.get(), CarruselRecommendConfig.class));
            }
            Index<CarruselRecommendConfig> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, CarruselRecommendConfig.class);
            Optional<CarruselRecommendConfig> carruselRecommendConfig = index.getObject(URLConnections.ALGOLIA_CARRUSEL_RECOMMEND);

            carruselRecommendConfig.ifPresent( data -> {
                String jsonToCache = new Gson().toJson(carruselRecommendConfig.get());
                CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_CARRUSEL_RECOMMEND, jsonToCache);
            } );

            return carruselRecommendConfig;

        } catch (AlgoliaException e) {
            LOG.warning("No se pudo consultar configuración");
        }
        return Optional.empty();
    }
    public static Optional<BannerPrimeConfig> getBannerPrimeConfig() {
        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_BANNER_PRIME);

            if (jsonCachedOptional.isPresent()){
                //LOG.info("tiene  config" );
                return Optional.of(new Gson().fromJson(jsonCachedOptional.get(), BannerPrimeConfig.class));
            }
            Index<BannerPrimeConfig> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, BannerPrimeConfig.class);
            Optional<BannerPrimeConfig> bannerPrimeConfig = index.getObject(URLConnections.ALGOLIA_BANNER_PRIME);
            bannerPrimeConfig.ifPresent( data -> {
                String jsonToCache = new Gson().toJson(bannerPrimeConfig.get());
                CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_BANNER_PRIME, jsonToCache);
            } );

            return bannerPrimeConfig;

        } catch (AlgoliaException e) {
            LOG.warning("No se pudo consultar configuración");
        }
        return Optional.empty();
    }



    public static MessageValidateCouponAlgolia getMessageValidateCoupon(){
        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.MESSAGE_VALIDATE_COUPON_CONFIG);

            if (jsonCachedOptional.isPresent()){
                //LOG.info("Tiene cache: MessageValidateCouponAlgolia");
                return new Gson().fromJson(jsonCachedOptional.get(),MessageValidateCouponAlgolia.class);
            }

            Index<MessageValidateCouponAlgolia> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, MessageValidateCouponAlgolia.class);
            Optional<MessageValidateCouponAlgolia> message = index.getObject(URLConnections.MESSAGE_VALIDATE_COUPON_CONFIG);
            //LOG.info("No tiene cache: MessageValidateCouponAlgolia");
            message.ifPresent( data -> {
                String jsonToCache = new Gson().toJson(message.get());
                CachedDataManager.saveJsonInCache(URLConnections.MESSAGE_VALIDATE_COUPON_CONFIG, jsonToCache);
            } );
            return message.orElseGet(MessageValidateCouponAlgolia::new);
        } catch (Exception e) {
            LOG.warning("Error in get MessageValidateCouponAlgolia. Message:" + Arrays.toString(e.getStackTrace()));
        }
        return null;
    }
    public static Optional<RecommendConfig> getAlgoliaRecommendConfig() {
        try {
            String cacheKey = URLConnections.ALGOLIA_RECOMMEND_CONFIG + "V2";
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(cacheKey);
            if (jsonCachedOptional.isPresent()) {
                return Optional.of(new Gson().fromJson(jsonCachedOptional.get(), RecommendConfig.class));
            }
            // Get configuration from GrowthBook instead of Algolia
            TtlCacheAlgoliaRecommendRes ttlCacheAlgoliaRecommendRes = GrowthBookConfigLoader.ttlCacheAlgoliaRecommend(Constants.CUSTOMER_ANONYMOUS);

            // Create and populate RecommendConfig from GrowthBook data
            RecommendConfig recommendConfig = new RecommendConfig();
            recommendConfig.setObjectID(URLConnections.ALGOLIA_RECOMMEND_CONFIG);
            recommendConfig.setOptimalRoute(ttlCacheAlgoliaRecommendRes.getOptimalRoute());
            recommendConfig.setDepartmentsCarrousel(ttlCacheAlgoliaRecommendRes.getDepartmentsCarrousel());
            recommendConfig.setAdvisedItems(ttlCacheAlgoliaRecommendRes.getAdvisedItems());
            recommendConfig.setSuggestedItemsFlag(ttlCacheAlgoliaRecommendRes.getSuggestedItemsFlag());
            recommendConfig.setDepartmentsAfinity(ttlCacheAlgoliaRecommendRes.getDepartmentsAfinity());

            // Cache the configuration
            String jsonToCache = new Gson().toJson(recommendConfig);
            CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_RECOMMEND_CONFIG + "V2", jsonToCache);

            return Optional.of(recommendConfig);

        } catch (Exception e) {
            LOG.warning("Error getting RecommendConfig from GrowthBook: " + e.getMessage());
        }
        return Optional.empty();
    }

    public static List<ItemAlgolia> getRelatedProductsSubstitutes(long itemId, int storeId) {
        try {
            // Get the item information to extract the category
            ItemAlgolia item = getItemAlgoliaById(itemId, (long)storeId);
            if (item == null) {
                LOG.warning("getRelatedProductsSubstitutes() Item not found in Algolia: " + itemId);
                return new ArrayList<>();
            }

            String category = item.getCategorie();
            if (category == null || category.isEmpty()) {
                LOG.warning("getRelatedProductsSubstitutes() Item has no category: " + itemId);
                return new ArrayList<>();
            }

            RecommendResponse response = ApiAlgoliaRecommendService.get().getRelatedProductsColombia(itemId, category);
            if(Objects.nonNull(response.getResults()) && !response.getResults().isEmpty()) {
                List<ItemAlgolia> itemAlgoliaList = response.getResults().get(0).getHits();
                itemAlgoliaList = itemAlgoliaList.stream()
                        .filter(i -> i.getTotalStock() != null && i.getTotalStock() != 0)
                        .collect(Collectors.toList());

                return itemAlgoliaList;
            }
        } catch (Exception e) {
            LOG.warning("Error getting related products substitutes: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    public static List<ItemAlgolia> getRelatedItems(Item item) {
        try {
            if (item == null) {
                return new ArrayList<>();
            }
            RecommendResponse response = ApiAlgoliaRecommendService.get().getRelatedProductsColombia(item.getId(), item.getCategorie());
            return response.getResults().stream()
                    .findFirst()
                    .map(RecommendResponse.Results::getHits)
                    .orElseGet(ArrayList::new);
        } catch (Exception e) {
            LOG.warning("Error getRelatedProducts: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    public static List<ItemAlgolia> getTrendingItems(String department) {
        RecommendResponse response = ApiAlgoliaRecommendService.get().getTrendingItems(department);
        List<ItemAlgolia> itemAlgoliaList = response.getResults().get(0).getHits();
        itemAlgoliaList = itemAlgoliaList.stream().filter(i -> i.getTotalStock()!=null && i.getTotalStock() != 0)
                .collect(Collectors.toList());
        return itemAlgoliaList;
    }

    public static List<Integer> getAdvisedItemFromRecommended(String department) {
        List<ItemAlgolia>  items =getTrendingItems(department);
        return items.stream().map(i -> parseInt(i.getId())).collect(Collectors.toList());
    }

    public static List<ItemAlgolia> getItemListAlgoliaFromStringList(List<String> objectIds) {
        try {
            //LOG.info("item to search -> " + objectId);
            Index<ItemAlgolia> index = algoliaClient.initIndex(URLConnections.ALGOLIA_PRODUCTS, ItemAlgolia.class);
            return Objects.nonNull(index.getObjects(objectIds)) ? index.getObjects(objectIds) : new ArrayList<>();
        } catch (AlgoliaException | NoSuchElementException e) {
//            e.printStackTrace();
            LOG.warning("Problema consultando algolia");
        }
        return new ArrayList<>();
    }

    public static Optional<AlgoliaReminder> getRemindersByUserId(final String idUser){
        try {
            Index<AlgoliaReminder> indexReminders = algoliaClient.initIndex(URLConnections.ALGOLIA_REMINDERS, AlgoliaReminder.class);
            return indexReminders.getObject(idUser);
        }catch (Exception e) {
            LOG.warning("Error in APIAlgolia.getRemindersByUserId. Message:"+ Arrays.toString(e.getStackTrace()));
        }
        return Optional.empty();
    }


    public static ImageTrackingConfigAlgolia getImageTracking(){
        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_IMAGE_TRACKING);

            if (jsonCachedOptional.isPresent()){
                return new Gson().fromJson(jsonCachedOptional.get(), ImageTrackingConfigAlgolia.class);
            }

            Index<ImageTrackingConfigAlgolia> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, ImageTrackingConfigAlgolia.class);
            Optional<ImageTrackingConfigAlgolia> optionalImageTrackingConfig = index.getObject(URLConnections.ALGOLIA_IMAGE_TRACKING);

            optionalImageTrackingConfig.ifPresent( data -> {
                String jsonToCache = new Gson().toJson(optionalImageTrackingConfig.get());
                CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_IMAGE_TRACKING, jsonToCache);
            } );
            return optionalImageTrackingConfig.get();
        }catch (Exception e){
            LOG.warning("Error in APIAlgolia.getImageTracking. Message:"+ Arrays.toString(e.getStackTrace()));
            e.getStackTrace();
        }
        return null;
    }

    public static AlgoliaMessageConfigCreateOrder getMessagesCreateOrder(){
        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_MESSAGE_CONFIG_CREATE_ORDER);
            if (jsonCachedOptional.isPresent()){
                //LOG.info("tiene cache getMessagesCreateOrder");
                return new Gson().fromJson(jsonCachedOptional.get(), AlgoliaMessageConfigCreateOrder.class);
            }
            Index<AlgoliaMessageConfigCreateOrder> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, AlgoliaMessageConfigCreateOrder.class);
            Optional<AlgoliaMessageConfigCreateOrder> optionalAlgoliaMessageConfigCreateOrder = index.getObject(URLConnections.ALGOLIA_MESSAGE_CONFIG_CREATE_ORDER);
            //LOG.info("No tiene cache getMessagesCreateOrder");
            optionalAlgoliaMessageConfigCreateOrder.ifPresent( data -> {
                String jsonToCache = new Gson().toJson(optionalAlgoliaMessageConfigCreateOrder.get());
                CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_MESSAGE_CONFIG_CREATE_ORDER, jsonToCache);
            } );
            return optionalAlgoliaMessageConfigCreateOrder.get();
        }catch (Exception e){
            LOG.warning("Error in APIAlgolia.getMessagesCreateOrder. Message:"+ Arrays.toString(e.getStackTrace()));
            e.getStackTrace();
        }
        return null;
    }

    public static Optional<RateLimitConfig> getRateLimitingLoginRegister(){
        try {
            Index<RateLimitConfig> index = algoliaClient.initIndex("properties", RateLimitConfig.class);
            return index.getObject("RATE.LIMIT.LOGIN.CONFIG");
        }catch (Exception e) {
            LOG.warning("Error in APIAlgolia.getRateLimitingLoginRegister. Message:"+ Arrays.toString(e.getStackTrace()));
        }
        return Optional.empty();
    }

    public static boolean isTalonActive(){
        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCacheIndex(URLConnections.ALGOLIA_TALON_ONE, 2);

            if (jsonCachedOptional.isPresent()){
                return Boolean.parseBoolean(jsonCachedOptional.get());
            }

            Index<TalonOneConfig> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, TalonOneConfig.class);
            Optional<TalonOneConfig> optionalIsActiveTalon = index.getObject(URLConnections.ALGOLIA_TALON_ONE);

            optionalIsActiveTalon.ifPresent( data -> {
                String jsonToCache = String.valueOf(optionalIsActiveTalon.get().isEnable());
                CachedDataManager.saveJsonInCacheIndex(URLConnections.ALGOLIA_TALON_ONE, jsonToCache, 2);
            } );
            return optionalIsActiveTalon.get().isEnable();
        }catch (Exception e){
            LOG.warning("Error in APIAlgolia.TALON_ONE. Message:"+ Arrays.toString(e.getStackTrace()));
            e.getStackTrace();
        }
        return false;
    }

    public static boolean isTalonPetalActive(EnableForEnum source) {
        AtomicBoolean response = new AtomicBoolean(false);
        try {
            if (Objects.nonNull(source)) {
                Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCacheIndex(URLConnections.ALGOLIA_PETALO_TALON_ONE, 2);

                final String[] jsonToCache = {null};
                if (jsonCachedOptional.isPresent()) {
                    return validateSource(source, jsonCachedOptional.get(), jsonToCache);
                }

                Index<TalonOnePetalConfig> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, TalonOnePetalConfig.class);
                Optional<TalonOnePetalConfig> optionalIsActiveTalon = index.getObject(URLConnections.ALGOLIA_PETALO_TALON_ONE);

                optionalIsActiveTalon.ifPresent(data -> {
                    String jsonResponseAlgolia = new Gson().toJson(optionalIsActiveTalon.get());
                    CachedDataManager.saveJsonInCacheIndex(URLConnections.ALGOLIA_PETALO_TALON_ONE, jsonResponseAlgolia, 2);
                    response.set(validateSource(source, jsonResponseAlgolia, jsonToCache));
                });
            }
        } catch (Exception e) {
            LOG.severe("Error in APIAlgolia.TALON_ONE. Message:" + Arrays.toString(e.getStackTrace()));
            e.getStackTrace();
        }
        return response.get();
    }

    private static boolean validateSource(EnableForEnum source, String jsonResponseAlgolia, String[] jsonToCache) {
        TalonOnePetalConfig talonOnePetalConfig = new Gson().fromJson(jsonResponseAlgolia, TalonOnePetalConfig.class);
        if(source.toValue().equalsIgnoreCase("WEB") && talonOnePetalConfig.isWeb())
            jsonToCache[0] =String.valueOf(talonOnePetalConfig.isWeb());
        if(source.toValue().equalsIgnoreCase("RESPONSIVE") && talonOnePetalConfig.isResponsive())
            jsonToCache[0] =String.valueOf(talonOnePetalConfig.isResponsive());
        if(source.toValue().equalsIgnoreCase("ANDROID") && talonOnePetalConfig.isAndroid())
            jsonToCache[0] =String.valueOf(talonOnePetalConfig.isAndroid());
        if(source.toValue().equalsIgnoreCase("IOS") && talonOnePetalConfig.isIos())
            jsonToCache[0] =String.valueOf(talonOnePetalConfig.isIos());
        LOG.info("jsonToCache[0]:"+ jsonToCache[0]);
        return Boolean.parseBoolean(jsonToCache[0]);
    }

    /***
     * active user
     * @author Jhon Chaparro
     * @return Optional<Boolean>
     */
    public static Optional<Boolean> isActiveSyncEventsAmplitude(){
        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_ASYNC_AMPLITUDE);
            if (jsonCachedOptional.isPresent()){
                //LOG.info("Tiene cache: isActiveSyncEventsAmplitude");
                return Optional.of(new Gson().fromJson(jsonCachedOptional.get(),LoadDataAsyncAmplitudeConfig.class).isActive());
            }

            Index<LoadDataAsyncAmplitudeConfig> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, LoadDataAsyncAmplitudeConfig.class);
            Optional<LoadDataAsyncAmplitudeConfig> configAlgoliaOptional = index.getObject(URLConnections.ALGOLIA_ASYNC_AMPLITUDE );

            //LOG.info("No tiene cache: isActiveSyncEventsAmplitude");
            configAlgoliaOptional.ifPresent( data -> {
                String jsonToCache = new Gson().toJson(configAlgoliaOptional.get());
                CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_ASYNC_AMPLITUDE, jsonToCache);
            } );

            if (!configAlgoliaOptional.isPresent()){
                return Optional.of(Boolean.FALSE);
            }
            // active feature
            if (configAlgoliaOptional.get().isActive()){
                return Optional.of(Boolean.TRUE);
            }
            return Optional.empty();
        } catch (AlgoliaException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public static Optional<AdditionalInformationOptics> getAdditionalInformationOptics() {
        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.OPTICS_ADDITIONAL_INFORMATION_CONFIG);
            if (jsonCachedOptional.isPresent()){
                return Optional.of(new Gson().fromJson(jsonCachedOptional.get(), AdditionalInformationOptics.class));
            }
            Index<AdditionalInformationOptics> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, AdditionalInformationOptics.class);
            Optional<AdditionalInformationOptics> additionalInformationOptics = index.getObject(URLConnections.OPTICS_ADDITIONAL_INFORMATION_CONFIG);
            additionalInformationOptics.ifPresent( data -> {
                String jsonToCache = new Gson().toJson(additionalInformationOptics.get());
                CachedDataManager.saveJsonInCache(URLConnections.OPTICS_ADDITIONAL_INFORMATION_CONFIG, jsonToCache);
            } );
            return additionalInformationOptics;
        } catch (AlgoliaException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public static Optional<ParametersLabelsOptics> getLabelsParametersOptics() {
        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.PARAMETERS_LABELS_OPTICS);
            if (jsonCachedOptional.isPresent()){
                return Optional.of(new Gson().fromJson(jsonCachedOptional.get(), ParametersLabelsOptics.class));
            }
            Index<ParametersLabelsOptics> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, ParametersLabelsOptics.class);
            Optional<ParametersLabelsOptics> additionalInformationOptics = index.getObject(URLConnections.PARAMETERS_LABELS_OPTICS);
            additionalInformationOptics.ifPresent( data -> {
                String jsonToCache = new Gson().toJson(additionalInformationOptics.get());
                CachedDataManager.saveJsonInCache(URLConnections.PARAMETERS_LABELS_OPTICS, jsonToCache);
            } );
            return additionalInformationOptics;
        } catch (AlgoliaException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public static Optional<MessageConfigOptics> getMessageConfigOptics() {
        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.MESSAGE_CONFIG_OPTICS);
            if (jsonCachedOptional.isPresent()){
                return Optional.of(new Gson().fromJson(jsonCachedOptional.get(), MessageConfigOptics.class));
            }
            Index<MessageConfigOptics> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, MessageConfigOptics.class);
            Optional<MessageConfigOptics> messageConfigOpticsOptional = index.getObject(URLConnections.MESSAGE_CONFIG_OPTICS);
            messageConfigOpticsOptional.ifPresent( data -> {
                String jsonToCache = new Gson().toJson(messageConfigOpticsOptional.get());
                CachedDataManager.saveJsonInCache(URLConnections.MESSAGE_CONFIG_OPTICS, jsonToCache);
            } );
            return messageConfigOpticsOptional;
        } catch (AlgoliaException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public static Optional<MessageConfigOptics> getMessageConfigOpticsApps() {
        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.MESSAGE_CONFIG_OPTICS_APPS);
            if (jsonCachedOptional.isPresent()){
                return Optional.of(new Gson().fromJson(jsonCachedOptional.get(), MessageConfigOptics.class));
            }
            Index<MessageConfigOptics> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, MessageConfigOptics.class);
            Optional<MessageConfigOptics> messageConfigOpticsOptional = index.getObject(URLConnections.MESSAGE_CONFIG_OPTICS_APPS);
            messageConfigOpticsOptional.ifPresent( data -> {
                String jsonToCache = new Gson().toJson(messageConfigOpticsOptional.get());
                CachedDataManager.saveJsonInCache(URLConnections.MESSAGE_CONFIG_OPTICS_APPS, jsonToCache);
            } );
            return messageConfigOpticsOptional;
        } catch (AlgoliaException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public static Optional<StoreIdDefaultOptics> getStoreIdDefaultOptics() {
        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.STORE_ID_DEFAULT_OPTICS);
            if (jsonCachedOptional.isPresent()){
                return Optional.of(new Gson().fromJson(jsonCachedOptional.get(), StoreIdDefaultOptics.class));
            }
            Index<StoreIdDefaultOptics> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, StoreIdDefaultOptics.class);
            Optional<StoreIdDefaultOptics> storeIdDefaultOpticsOptional = index.getObject(URLConnections.STORE_ID_DEFAULT_OPTICS);
            storeIdDefaultOpticsOptional.ifPresent( data -> {
                String jsonToCache = new Gson().toJson(storeIdDefaultOpticsOptional.get());
                CachedDataManager.saveJsonInCache(URLConnections.STORE_ID_DEFAULT_OPTICS, jsonToCache);
            } );
            return storeIdDefaultOpticsOptional;
        } catch (AlgoliaException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }


    public static Optional<ConfigDeliveryTimeOptics> getConfigDeliveryTime() {
        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.OPTICS_CONFIG_DELIVERY_TIME);
            if (jsonCachedOptional.isPresent()){
                //LOG.info("tiene cache getConfigDeliveryTime" );
                return Optional.of(new Gson().fromJson(jsonCachedOptional.get(), ConfigDeliveryTimeOptics.class));
            }
            Index<ConfigDeliveryTimeOptics> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, ConfigDeliveryTimeOptics.class);
            Optional<ConfigDeliveryTimeOptics> configDeliveryTimeOptics = index.getObject(URLConnections.OPTICS_CONFIG_DELIVERY_TIME);
            //LOG.info("No tiene cache getConfigDeliveryTime" );
            configDeliveryTimeOptics.ifPresent( data -> {
                String jsonToCache = new Gson().toJson(configDeliveryTimeOptics.get());
                CachedDataManager.saveJsonInCache(URLConnections.OPTICS_CONFIG_DELIVERY_TIME, jsonToCache);
            } );
            return configDeliveryTimeOptics;
        } catch (AlgoliaException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public static KeyWordsCityConfig getKeyWordsCityConfig() {
        KeyWordsCityConfig response = new KeyWordsCityConfig();
        try {
            Index<KeyWordsCityConfig> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES,KeyWordsCityConfig.class);
            Optional<KeyWordsCityConfig> keyWordsCityConfig = index.getObject(URLConnections.ALGOLIA_KEYWORDS_CONFIG);
            keyWordsCityConfig.ifPresent( data -> {
                String jsonToCache = new Gson().toJson(keyWordsCityConfig.get());
                CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_KEYWORDS_CONFIG, jsonToCache);
            } );
            return keyWordsCityConfig.get();
        } catch (Exception e) {
            LOG.warning(e.getMessage());
        }
        return response;
    }

    public static ConfigValidateAddress getConfigValidateAddress() {
        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_CONFIG_ADDRESS);
            if (jsonCachedOptional.isPresent()) {
                return new Gson().fromJson(jsonCachedOptional.get(), ConfigValidateAddress.class);
            }
            Index<ConfigValidateAddress> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, ConfigValidateAddress.class);
            Optional<ConfigValidateAddress> configValidateAddress = index.getObject(URLConnections.ALGOLIA_CONFIG_ADDRESS);

            configValidateAddress.ifPresent(data -> {
                String jsonToCache = new Gson().toJson(configValidateAddress.get());
                CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_CONFIG_ADDRESS, jsonToCache);
            });
            return configValidateAddress.get();
        } catch (Exception e) {
            LOG.warning(e.getMessage());
        }

        return null;
    }

    public static String getMainCities (){
        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.ALGOLIA_MAIN_CITIES);
            if (jsonCachedOptional.isPresent()){
                //LOG.info("Tiene cache: getMainCities");
                //LOG.info(jsonCachedOptional.get());
                return new Gson().fromJson(jsonCachedOptional.get(), MainCities.class).getMainCities();
            }

            Index<MainCities> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, MainCities.class);
            Optional<MainCities> mainCities = index.getObject(URLConnections.ALGOLIA_MAIN_CITIES);

            //LOG.info("No tiene cache: getMainCities");
            mainCities.ifPresent( data -> {
                String jsonToCache = new Gson().toJson(mainCities.get());
                CachedDataManager.saveJsonInCache(URLConnections.ALGOLIA_MAIN_CITIES, jsonToCache);
            } );
            return mainCities.get().getMainCities();

        } catch (Exception e) {
            LOG.warning(e.getMessage());
        }

        return null;
    }

    public static AlgoliaExploStore getActiveStoreExpo() {
        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(URLConnections.CONFIG_EXPLO_STORE);
            if (jsonCachedOptional.isPresent()){
                //LOG.info("tiene cache getTipsConfig" );
                return new Gson().fromJson(jsonCachedOptional.get(), AlgoliaExploStore.class);
            }
            Index<AlgoliaExploStore> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, AlgoliaExploStore.class);
            Optional<AlgoliaExploStore> exploStore = index.getObject(URLConnections.CONFIG_EXPLO_STORE);
            //LOG.info("No tiene cache getTipsConfig" );
            exploStore.ifPresent( data -> {
                String jsonToCache = new Gson().toJson(exploStore.get());
                CachedDataManager.saveJsonInCache(URLConnections.CONFIG_EXPLO_STORE, jsonToCache);
            } );
            return exploStore.get();
        } catch (AlgoliaException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Refactor para obtener una lista de items usando los objectsIds por medio de getObject
     * Get Object no consume searchs
     *
     * @param listItemQuery Lista de items que se requiere consultar
     * @return Lista de items
     */
    public static List<ItemAlgolia> findItemByIdListV2(List<ItemQuery> listItemQuery) {
        List<String> objectIds = listItemQuery.stream().map(ItemQuery::getItemStore).collect(Collectors.toList());
        Index<ItemAlgolia> index = algoliaClient.initIndex(URLConnections.ALGOLIA_PRODUCTS, ItemAlgolia.class);
        try {
            List<ItemAlgolia> result = index.getObjects(objectIds)
                    .stream().filter(Objects::nonNull).filter(item -> Objects.nonNull(item.getId()) && !item.getId().isEmpty())
                    .collect(Collectors.toList());
            Comparator<ItemAlgolia> comparator = nullsLast((ItemAlgolia item, ItemAlgolia itemD) -> itemD.getSales().compareTo(item.getSales()));
            Predicate<ItemAlgolia> predicate = e -> e.getSales() != null;
            return result.stream()
                    .filter(predicate)
                    .sorted(comparator)
                    .collect(Collectors.toList());
        } catch (AlgoliaException e) {
            LOG.warning("Ocurrio un error el metodo findItemByIdListV2 {} " + e.getMessage());
            return new ArrayList<>();
        }
    }


    /**
     *
     * Get a list of items using his objectIDs
     *
     * @param objectIds Object ids from the items that you want to search
     * @return List of Items for the objectIds entered
     */
    public static List<ItemAlgolia> getItemsByObjectIds(List<String> objectIds) {
        try {
            return getItemsAlgolia(objectIds);
        } catch (Exception e) {
            try{
                LOG.info("Error en getItemsByObjectIds, se intenta nuevamente");
                return getItemsAlgolia(objectIds);
            }catch (Exception ex){
                LOG.warning("Error method getItemsByObjectIds {}" + e.getMessage());
            }
        }
        return new ArrayList<>();
    }

    private static List<ItemAlgolia> getItemsAlgolia(List<String> objectIds) throws AlgoliaException {
        Index<ItemAlgolia> index = algoliaClient.initIndex(URLConnections.ALGOLIA_PRODUCTS, ItemAlgolia.class);
        return index.getObjects(objectIds);
    }

    public static Optional<BagItem> getBagItem(){
        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(Constants.ALGOLIA_TALON_ONE_BAG_ITEM);
            if (jsonCachedOptional.isPresent()){
                return Optional.of(new Gson().fromJson(jsonCachedOptional.get(), BagItem.class));
            }

            Index<BagItem> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, BagItem.class);
            Optional<BagItem> bagItem = index.getObject(Constants.ALGOLIA_TALON_ONE_BAG_ITEM);
            if(bagItem.isPresent()){
                String jsonToCache = new Gson().toJson(bagItem.get());
                CachedDataManager.saveJsonInCache(Constants.ALGOLIA_TALON_ONE_BAG_ITEM, jsonToCache);
                return Optional.of(bagItem.get());
            }else return Optional.empty();
        }catch(Exception e){
            LOG.warning("Error obteniendo bolsa desde algolia -> " +e.getMessage());
            return Optional.empty();
        }
    }

    public static Optional<ExtendedBagPropertiesTalonOne> getExtendedBagItem(){
        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCache(Constants.TALON_ONE_EXTENDED_BAG_ITEM);
            if (jsonCachedOptional.isPresent()){
                return Optional.of(new Gson().fromJson(jsonCachedOptional.get(), ExtendedBagPropertiesTalonOne.class));
            }
            Index<ExtendedBagPropertiesTalonOne> index = algoliaClient.initIndex(URLConnections.ALGOLIA_INDEX_PROPERTIES, ExtendedBagPropertiesTalonOne.class);
            Optional<ExtendedBagPropertiesTalonOne> listBagItem = index.getObject(Constants.TALON_ONE_EXTENDED_BAG_ITEM);
            if(listBagItem.isPresent()){
                String jsonToCache = new Gson().toJson(listBagItem.get());
                CachedDataManager.saveJsonInCache(Constants.TALON_ONE_EXTENDED_BAG_ITEM, jsonToCache);
                return listBagItem;
            }else return Optional.empty();
        }catch(Exception e){
            LOG.warning("Error obteniendo propiedades de la bolsa desde algolia -> " +e.getMessage());
            return Optional.empty();
        }
    }

}
