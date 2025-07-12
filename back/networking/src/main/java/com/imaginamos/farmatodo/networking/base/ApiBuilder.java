package com.imaginamos.farmatodo.networking.base;

import com.google.gson.*;
import com.imaginamos.farmatodo.model.algolia.login.TimeOut;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.model.util.URLConnections;
import com.imaginamos.farmatodo.networking.algolia.APIAlgolia;
import com.imaginamos.farmatodo.networking.api.ApiAlgoliaProxy;
import com.imaginamos.farmatodo.networking.api.ApiOSRM;
import com.imaginamos.farmatodo.networking.api.ShoppingCartApi;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;


public class ApiBuilder {

    private static ApiBuilder instance;
    private static final String BASE_SI_URL = Constants.BASE_SI_URL;
    private static final String BASE_GEO_GOOGLE_URL = Constants.BASE_GEO_GOOGLE_URL;
    private static final String BASE_OSRM_URL = Constants.BASE_URL_OSRM;
    private static final String BASE_SHOPPING_CART_URL = URLConnections.URL_SHOPPING_CART_STORE_STOCK;
    private static final String BASE_CORE_URL = URLConnections.URL_BASE;
    private static final String BASE_BACKEND30_URL = URLConnections.BACKEND_CORE_30_HOST_BASE;
    private static final String BASE_URL_CLOUD_FUNCTIONS = URLConnections.URL_BASE_CLOUD_FUNCTIONS;
    private static final String BASE_URL_CMS = URLConnections.URL_BASE_BANNERS_CMS;
    private static final String BASE_URL_ALGOLIA_RECOMMEND = Constants.URL_ALGOLIA_RECOMMEND;
    private static final String BASE_URL_ALGOLIA_PROXY = Constants.URL_ALGOLIA_PROXY;

    private static final String BASE_CLOUD_FUNCTION_SERVICE_VEN = Constants.CLOUD_FUNCTION_SERVICE_VEN;

    private static final int SI_SERVICE = 0;
    private static final int GEO_GOOGLE_SERVICE = 1;
    private static final int BASE_URL_CORE_SERVICE = 2;
    private static final int BASE_URL_BACKEND30_SERVICE = 3;
    private static final int CLOUD_FUNCTION_SERVICE = 4;
    private static final int BASE_SERVICE_CMS = 5;
    private static final int BASE_SERVICE_ALGOLIA_RECOMMEND = 6;

    private static final int BASE_SERVICE_OSRM = 7;
    private static final int BASE_SERVICE_SHOPPING_CART = 8;
    private static final int BASE_SERVICE_ALGOLIA_PROXY = 9;
    private static final int CLOUD_FUNCTION_SERVICE_VEN = 10;

    //private static final Retrofit.Builder siBuilder = createRetrofitBuilder(BASE_SI_URL);
    private static final Retrofit.Builder siBuilder = createRetrofitBuilder(BASE_SI_URL);
    private static final Retrofit.Builder geoGoogleBuilder = createRetrofitBuilder(BASE_GEO_GOOGLE_URL);
    private static final Retrofit.Builder baseOSRMBuilder = createRetrofitBuilder(BASE_OSRM_URL);
    private static final Retrofit.Builder baseCoreBuilder = createRetrofitBuilder(BASE_CORE_URL);
    private static final Retrofit.Builder baseBackend30Builder = createRetrofitBuilder(BASE_BACKEND30_URL);
    private static final Retrofit.Builder baseCloudFunction = createRetrofitBuilder(BASE_URL_CLOUD_FUNCTIONS);
    private static final Retrofit.Builder baseCMSBuilder = createRetrofitBuilderCMS();
    private static final Retrofit.Builder baseAlgoliaReccommendBuilder = createRetrofitBuilder(BASE_URL_ALGOLIA_RECOMMEND);
    private static final Retrofit.Builder baseShoppingCartBuilder = createRetrofitBuilder(BASE_SHOPPING_CART_URL);
    private static final Retrofit.Builder baseAlgoliaProxyBuilder = createRetrofitBuilder(BASE_URL_ALGOLIA_PROXY);
    private static final Retrofit.Builder baseCloudFunctionVen = createRetrofitBuilder(BASE_CLOUD_FUNCTION_SERVICE_VEN);
    private ApiBuilder() {

    }

    public static ApiBuilder get() {
        if (instance == null) instance = getSync();
        return instance;
    }

    private static synchronized ApiBuilder getSync() {
        if (instance == null) instance = new ApiBuilder();
        return instance;
    }
    private static Long getTimeOut(){
        Optional<TimeOut> optionalTimeOut = APIAlgolia.getTimeOutConfig();
        if(Objects.nonNull(optionalTimeOut) && optionalTimeOut.isPresent()){
            return optionalTimeOut.get().getSeg();
        }else {
            return 60L;
        }
    }

    private static Retrofit.Builder createRetrofitBuilder(String baseUrl) {
        Long time = getTimeOut();
        OkHttpClient.Builder client = new OkHttpClient.Builder();
        client.connectTimeout(time, TimeUnit.SECONDS);
        client.readTimeout(time, TimeUnit.SECONDS);
        client.writeTimeout(time, TimeUnit.SECONDS);
        return getGsonBuilder(baseUrl, client);
    }

    private static Retrofit.Builder createRetrofitBuilderCMS() {
        Long time = 5L;
        OkHttpClient.Builder client = new OkHttpClient.Builder();
        client.connectTimeout(time, TimeUnit.SECONDS);
        client.readTimeout(time, TimeUnit.SECONDS);
        client.writeTimeout(time, TimeUnit.SECONDS);
        return getGsonBuilder(ApiBuilder.BASE_URL_CMS, client);
    }

    @NotNull
    private static Retrofit.Builder getGsonBuilder(String baseUrl, OkHttpClient.Builder client) {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
            @Override
            public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                return new Date(json.getAsJsonPrimitive().getAsLong());
            }
        });
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client.build())
                .addConverterFactory(GsonConverterFactory.create(builder.create()));
    }

    public <S> S createGeoGoogleService(Class<S> serviceClass) {
        return createService(serviceClass, GEO_GOOGLE_SERVICE);
    }
    public ApiOSRM createOSRMService(Class<ApiOSRM> apiOSRMClass) {
        return createService(apiOSRMClass, BASE_SERVICE_OSRM);
    }

    public ShoppingCartApi createShoppingCartService(Class<ShoppingCartApi> shoppingCartApiClass) {
        return createService(shoppingCartApiClass, BASE_SERVICE_SHOPPING_CART);
    }

    public ApiAlgoliaProxy createAlgoliaProxyService(Class<ApiAlgoliaProxy> serviceClass) {
        return createService(serviceClass, BASE_SERVICE_ALGOLIA_PROXY);
    }

    public <S> S createServinfomacionService(Class<S> serviceClass) {
        return createService(serviceClass, SI_SERVICE);
    }

    public <S> S createCoreSerive(Class<S> serviceClass) {
        return createService(serviceClass, BASE_URL_CORE_SERVICE);
    }

    public <S> S createBackend30Service(Class<S> serviceClass) {
        return createService(serviceClass, BASE_URL_BACKEND30_SERVICE);
    }

    public <S> S createCloudFunctionService(Class<S> serviceClass) {
        return createService(serviceClass, CLOUD_FUNCTION_SERVICE);
    }

    public <S> S createCloudFunctionServiceVen(Class<S> serviceClass) {
        return createService(serviceClass, CLOUD_FUNCTION_SERVICE_VEN);
    }

    public <S> S createCMSService(Class<S> serviceClass) {
        return createService(serviceClass, BASE_SERVICE_CMS);
    }

    public <S> S createAlgoliaRecommendService(Class<S> serviceClass) {
        return createService(serviceClass, BASE_URL_ALGOLIA_RECOMMEND);
    }

    private <S> S createService(Class<S> serviceClass, int serviceType) {

        Retrofit.Builder builder;
        switch (serviceType) {
            case GEO_GOOGLE_SERVICE:
                builder = geoGoogleBuilder;
                break;
            case SI_SERVICE:
                builder = siBuilder;
                break;
            case BASE_URL_CORE_SERVICE:
                builder = baseCoreBuilder;
                break;
            case BASE_URL_BACKEND30_SERVICE:
                builder = baseBackend30Builder;
                break;
            case CLOUD_FUNCTION_SERVICE:
                builder = baseCloudFunction;
                break;
            case BASE_SERVICE_CMS:
                builder = baseCMSBuilder;
                break;
            case BASE_SERVICE_ALGOLIA_RECOMMEND:
                builder = baseAlgoliaReccommendBuilder;
                break;
            case BASE_SERVICE_OSRM:
                builder = baseOSRMBuilder;
                break;
            case BASE_SERVICE_SHOPPING_CART:
                builder = baseShoppingCartBuilder;
                break;
            case BASE_SERVICE_ALGOLIA_PROXY:
                builder = baseAlgoliaProxyBuilder;
                break;
            case CLOUD_FUNCTION_SERVICE_VEN:
                builder = baseCloudFunctionVen;
                break;
            default:
                throw new RuntimeException("Should select a valid service type");
        }

        Retrofit retrofit = builder.build();
        return retrofit.create(serviceClass);
    }

    public <S> S createService(Class<S> serviceClass, String baseURL) {

        Retrofit.Builder builder = createRetrofitBuilder(baseURL);

        Retrofit retrofit = builder.build();
        return retrofit.create(serviceClass);
    }
}
