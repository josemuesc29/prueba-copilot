package com.imaginamos.farmatodo.networking.services;

import com.google.gson.Gson;
import com.imaginamos.farmatodo.model.algolia.QueryParametersAlgoliaRecommend;
import com.imaginamos.farmatodo.model.algolia.RecommendRequest;
import com.imaginamos.farmatodo.model.algolia.RecommendResponse;
import com.imaginamos.farmatodo.model.algolia.RequestRecommend;
import com.imaginamos.farmatodo.model.item.TtlCacheAlgoliaRecommendRes;
import com.imaginamos.farmatodo.model.product.Item;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.model.util.URLConnections;
import com.imaginamos.farmatodo.networking.api.ApiAlgoliaRecommend;
import com.imaginamos.farmatodo.networking.base.ApiBuilder;
import com.imaginamos.farmatodo.networking.cache.CachedDataManager;
import com.imaginamos.farmatodo.networking.growthbook.GrowthBookConfigLoader;
import retrofit2.Call;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

public class ApiAlgoliaRecommendService {

    private static ApiAlgoliaRecommendService instance;

    private ApiAlgoliaRecommend apiAlgoliaRecommend;
    private static final String RELATED_PRODUCTS = "related-products";
    private static final String PRODUCTS = URLConnections.ALGOLIA_PRODUCTS;
    private static final String TRENDING_ITEMS="trending-items";
    public static final int THRESHOLD = 0;
    public static final int MAX_RECOMMENDATIONS = 15;
    public static final String CATEGORY_PREFIX = "categorie:";
    public static final String ENDPOINT_RECOMMENDATIONS = "recommendations";

    // Caching Properties for Redis
    public static final long ALGOLIA_RECOMMEND_CACHE_TIME = 172800; // 48 hours

    public final static String CUSTOMER_ANONYMOUS = "ANONYMOUS";
    private static final int ALGOLIA_RECOMMEND_CACHE_INDEX = 11;
    private static final String RELATED_PRODUCTS_CO_CACHE_KEY_PREFIX = "related_products_co_";
    private static final String RELATED_PRODUCTS_GENERAL_CACHE_KEY_PREFIX = "related_products_general_";
    private static final String TRENDING_ITEMS_CO_CACHE_KEY_PREFIX = "trending_items_co_";
    private static final Gson gson = new Gson();


    private static final Logger LOG = Logger.getLogger(ApiAlgoliaRecommendService.class.getName());

    private ApiAlgoliaRecommendService() {
        apiAlgoliaRecommend = ApiBuilder.get().createAlgoliaRecommendService(ApiAlgoliaRecommend.class);
    }

    public static ApiAlgoliaRecommendService get() {
        if (instance == null) instance = getSync();
        return instance;
    }

    private static synchronized ApiAlgoliaRecommendService getSync() {
        if (instance == null) instance = new ApiAlgoliaRecommendService();
        return instance;
    }


    @Deprecated
    public RecommendResponse getRelatedProducts(long itemId, int store) {
        try {
            if (itemId > 0 && store > 0) {

                String cacheKey = RELATED_PRODUCTS_GENERAL_CACHE_KEY_PREFIX + itemId + "_" + store;

                Optional<String> cachedData = CachedDataManager.getJsonFromCacheIndex(cacheKey, ALGOLIA_RECOMMEND_CACHE_INDEX);
                if (cachedData.isPresent()) {
                        LOG.info("Retrieved related products from cache for item: " + itemId + " and store: " + store);
                    return gson.fromJson(cachedData.get(), RecommendResponse.class);
                }

                RecommendRequest request = new RecommendRequest();
                List<RequestRecommend> requestList = new ArrayList<>();
                RequestRecommend req = new RequestRecommend();
                req.setIndexName(PRODUCTS);
                req.setModel(RELATED_PRODUCTS);
                req.setThreshold(0);
                LOG.info("Recommend objectId -> " +String.valueOf(itemId).concat(String.valueOf(store)));
                req.setObjectID(String.valueOf(itemId).concat(String.valueOf(store)));
                requestList.add(req);
                request.setRequests(requestList);
                Call<RecommendResponse> call = apiAlgoliaRecommend.recommendAPI("recommendations",request);
                final Response<RecommendResponse> response = call.execute();

                if (response.isSuccessful() && response.body() != null) {
                    RecommendResponse result = response.body();
                    TtlCacheAlgoliaRecommendRes ttlCacheAlgoliaRecommendRes = GrowthBookConfigLoader.ttlCacheAlgoliaRecommend(CUSTOMER_ANONYMOUS);
                    if (Objects.nonNull(ttlCacheAlgoliaRecommendRes.getAlgoliaRecommendTtlSeconds()))
                        CachedDataManager.saveJsonInCacheIndexTime(cacheKey, gson.toJson(result),
                            ALGOLIA_RECOMMEND_CACHE_INDEX, ttlCacheAlgoliaRecommendRes.getAlgoliaRecommendTtlSeconds());

                    return result;
                }
            }
        } catch (Exception e) {
            LOG.warning("Error al consumir el servicio" + e.getMessage());
        }
        return new RecommendResponse();
    }

    public RecommendResponse getRelatedProductsColombia(long itemId, String category) {
        if (itemId <= 0) {
            LOG.warning("Invalid item ID provided");
            return new RecommendResponse();
        }

        String cacheKey = RELATED_PRODUCTS_CO_CACHE_KEY_PREFIX + itemId;

        Optional<String> cachedData = CachedDataManager.getJsonFromCacheIndex(cacheKey, ALGOLIA_RECOMMEND_CACHE_INDEX);
        if (cachedData.isPresent()) {
            // LOG.info("Method getRelatedProductsColombia() Retrieved related products from cache for key: " + cacheKey);
            return gson.fromJson(cachedData.get(), RecommendResponse.class);
        }

        // LOG.info("Method getRelatedProductsColombia() - No cache found for item: " + itemId + ", proceeding with API call");

        RecommendRequest request = new RecommendRequest();
        List<RequestRecommend> requestList = new ArrayList<>();
        RequestRecommend req = new RequestRecommend();
        req.setIndexName(Constants.PRODUCTS_COL_INDEX);
        req.setModel(RELATED_PRODUCTS);
        req.setThreshold(THRESHOLD);
        req.setObjectID(String.valueOf(itemId));
        req.setMaxRecommendations(MAX_RECOMMENDATIONS);

        QueryParametersAlgoliaRecommend queryParameters = new QueryParametersAlgoliaRecommend();
        List<List<String>> facetFilters = new ArrayList<>();
        List<String> filter = new ArrayList<>();

        if (category != null && !category.isEmpty()) {
            filter.add(CATEGORY_PREFIX.concat(category));
            facetFilters.add(filter);
            queryParameters.setFacetFilters(facetFilters);
            req.setQueryParameters(queryParameters);
        }

        requestList.add(req);
        request.setRequests(requestList);

        LOG.info("Request to Algolia Recommend: " + gson.toJson(request));
        try {
            Call<RecommendResponse> call = apiAlgoliaRecommend.recommendAPI(ENDPOINT_RECOMMENDATIONS, request);
            Response<RecommendResponse> response = call.execute();

            if (response.isSuccessful() && response.body() != null) {
                RecommendResponse result = response.body();
                TtlCacheAlgoliaRecommendRes ttlCacheAlgoliaRecommendRes = GrowthBookConfigLoader.ttlCacheAlgoliaRecommend(CUSTOMER_ANONYMOUS);
                if (Objects.nonNull(ttlCacheAlgoliaRecommendRes.getAlgoliaRecommendTtlSeconds()))
                    CachedDataManager.saveJsonInCacheIndexTime(cacheKey, gson.toJson(result),
                        ALGOLIA_RECOMMEND_CACHE_INDEX, ttlCacheAlgoliaRecommendRes.getAlgoliaRecommendTtlSeconds());

                return result;
            } else {
                LOG.warning("Unsuccessful response or empty body");
            }
        } catch (Exception e) {
            LOG.warning("Error request to algolia Recommend " + e.getMessage());
        }

        return new RecommendResponse();
    }

    public RecommendResponse getTrendingItems(String department) {
        try {

            String cacheKey = TRENDING_ITEMS_CO_CACHE_KEY_PREFIX + department;

            Optional<String> cachedData = CachedDataManager.getJsonFromCacheIndex(cacheKey, ALGOLIA_RECOMMEND_CACHE_INDEX);
            if (cachedData.isPresent()) {
                LOG.info("Retrieved trending items from cache for department: " + department);
                return gson.fromJson(cachedData.get(), RecommendResponse.class);
            }

            RecommendRequest request = new RecommendRequest();
            List<RequestRecommend> requestList = new ArrayList<>();
            RequestRecommend req = new RequestRecommend();
            req.setIndexName(PRODUCTS);
            req.setModel(TRENDING_ITEMS);
            req.setFacetName("Departamentos");
            req.setFacetValue(department);
            req.setThreshold(0);
            LOG.info(new Gson().toJson(req));
            requestList.add(req);
            request.setRequests(requestList);
            Call<RecommendResponse> call = apiAlgoliaRecommend.recommendAPI("recommendations",request);
            final Response<RecommendResponse> response = call.execute();
            LOG.info(new Gson().toJson(response.body()));

            if (response.isSuccessful() && response.body() != null) {
                RecommendResponse result = response.body();

                TtlCacheAlgoliaRecommendRes ttlCacheAlgoliaRecommendRes = GrowthBookConfigLoader.ttlCacheAlgoliaRecommend(CUSTOMER_ANONYMOUS);
                if (Objects.nonNull(ttlCacheAlgoliaRecommendRes.getAlgoliaRecommendTtlSeconds()))
                    CachedDataManager.saveJsonInCacheIndexTime(cacheKey, gson.toJson(result),
                        ALGOLIA_RECOMMEND_CACHE_INDEX, ttlCacheAlgoliaRecommendRes.getAlgoliaRecommendTtlSeconds());

                return result;
            }
        } catch (Exception e) {
            LOG.warning("Error al consumir el servicio" + e.getMessage());
        }
        return new RecommendResponse();
    }


}
