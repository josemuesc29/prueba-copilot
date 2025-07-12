package com.imaginamos.farmatodo.backend.customer.async;

import com.google.gson.Gson;
import com.imaginamos.farmatodo.backend.customer.async.models.DatasourcesIds;
import com.imaginamos.farmatodo.model.algolia.ItemAlgolia;
import com.imaginamos.farmatodo.model.customer.Customer;
import com.imaginamos.farmatodo.model.dto.EnableForEnum;
import com.imaginamos.farmatodo.model.home.BannerDataCMSType;
import com.imaginamos.farmatodo.model.home.BannersDTFRes;
import com.imaginamos.farmatodo.model.home.HomeInfoConfig;
import com.imaginamos.farmatodo.model.home.ItemCarrouselAsync;
import com.imaginamos.farmatodo.networking.algolia.APIAlgolia;
import com.imaginamos.farmatodo.networking.services.ApiGatewayService;
import migration.algolia.AlgoliaManager;
import migration.algolia.AlgoliaManagerHandler;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

public class ExternalDataAsync {

    private static final Logger log = Logger.getLogger(ExternalDataAsync.class.getName());

    private AlgoliaManager algoliaManager;

    public ExternalDataAsync() {
        algoliaManager = new AlgoliaManagerHandler();
    }

    public void getCarrouselsAsync(HomeInfoConfig homeInfoConfig, DatasourcesIds datasourcesIds) throws ExecutionException, InterruptedException {
        log.info(new Gson().toJson(datasourcesIds));
//        log.info("Inicio llamado asincrono");
        CompletableFuture<List<ItemAlgolia>> favoriteItemAlgolia = CompletableFuture
                .supplyAsync(() -> algoliaManager.getItemListAlgoliaFromStringList(datasourcesIds.getFavoriteIds()));
        CompletableFuture<List<ItemAlgolia>> viewedItemAlgolia = CompletableFuture
                .supplyAsync(() -> algoliaManager.getItemListAlgoliaFromStringList(datasourcesIds.getViewedIds()));
        CompletableFuture<List<ItemAlgolia>> purchasesItemAlgolia = CompletableFuture
                .supplyAsync(() -> algoliaManager.getItemListAlgoliaFromStringList(datasourcesIds.getPurchasesIds()));
        CompletableFuture<BannersDTFRes> bannersAsync = CompletableFuture
                .supplyAsync(() -> this.getBannersDTFRes(homeInfoConfig));
        CompletableFuture<BannersDTFRes> bannersMinLeftAsync = CompletableFuture
                .supplyAsync(() -> this.getBannersDTFMinLeftV1(homeInfoConfig));
        CompletableFuture<Void> futures = CompletableFuture.allOf(favoriteItemAlgolia,viewedItemAlgolia, purchasesItemAlgolia, bannersAsync, bannersMinLeftAsync);

        while(!futures.isDone()) {
            Thread.sleep(50);
        }
//        log.info("Fin llamado asincrono");

        homeInfoConfig.setCarrouselItemListAsync(new ItemCarrouselAsync());
        homeInfoConfig.getCarrouselItemListAsync().setFavorites(favoriteItemAlgolia.get());
        homeInfoConfig.getCarrouselItemListAsync().setPurchases(purchasesItemAlgolia.get());
        homeInfoConfig.getCarrouselItemListAsync().setViewed(viewedItemAlgolia.get());
        homeInfoConfig.setAsyncBannersDTFRes(bannersAsync.get());
        homeInfoConfig.setAsyncBannersDTFResponseMinLeft(bannersMinLeftAsync.get());
//        log.info("Finalized");
    }

    private BannersDTFRes getBannersDTFRes(HomeInfoConfig homeInfoConfig) {
        // duartion time  response banners
        Instant start = Instant.now();

        BannersDTFRes bannersDTFResponse = new BannersDTFRes();


        EnableForEnum sourceEnum = homeInfoConfig.getHomeRequest().getSource();

        switch (sourceEnum) {
            case ANDROID:
            case IOS:
            case RESPONSIVE:
                bannersDTFResponse = getBannersDTFResMobile(homeInfoConfig);
                break;
            default:
                bannersDTFResponse = getBannersDTFResDesktop(homeInfoConfig);
                break;
        }

        Instant finish = Instant.now();
        //log.info("Banners DURATION in millis : " + Duration.between(start, finish).toMillis());

        // Validate Default Banners:

        if (bannersDTFResponse == null || !bannersDTFResponse.isValid()) {

            // set default banners
            //log.warning("Not found Banners! use default!!");

            Optional<BannerDataCMSType> bannerDataCMSTypeOptional = APIAlgolia.getDefaultBannersHome();

            if (bannerDataCMSTypeOptional.isPresent()) {

                bannersDTFResponse = new BannersDTFRes();
                bannersDTFResponse.setCode("200");
                bannersDTFResponse.setMessage("Success");
                bannersDTFResponse.setData(bannerDataCMSTypeOptional.get());

            }

        }
        return bannersDTFResponse;
    }

    private BannersDTFRes getBannersDTFResMobile(HomeInfoConfig homeInfoConfig) {
        Optional<BannersDTFRes> bannersDTFResponse;
        try {
            // call service only mobile
            if (homeInfoConfig.getCustomerOnlyData() != null
                    && homeInfoConfig.getCustomerOnlyData().getEmail() != null
                    && !homeInfoConfig.getCustomerOnlyData().getEmail().isEmpty()) {
                bannersDTFResponse = ApiGatewayService.get().getBannerHome(
                        homeInfoConfig.getCustomerOnlyData().getId(),
                        homeInfoConfig.getCustomerOnlyData().getEmail(),
                        null,
                        null,
                        Objects.nonNull(homeInfoConfig.getHomeRequest()) && Objects.nonNull(homeInfoConfig.getHomeRequest().getCity()) ? homeInfoConfig.getHomeRequest().getCity() : null ,
                        true
                );

            } else {
                bannersDTFResponse = ApiGatewayService.get().getBannerHome(null,null, null, null, homeInfoConfig.getHomeRequest().getCity(), true);
            }
        } catch (Exception e) {
            bannersDTFResponse = Optional.empty();
            e.printStackTrace();
        }

        return bannersDTFResponse.orElse(null);
    }

    @org.jetbrains.annotations.Nullable
    private BannersDTFRes getBannersDTFResDesktop(HomeInfoConfig homeInfoConfig) {
        Optional<BannersDTFRes> bannersDTFResponse;
        try {
            //log.info("method() setBannersHome");

            // set mail user for banners
            if (homeInfoConfig.getCustomerOnlyData() != null
                    && homeInfoConfig.getCustomerOnlyData().getEmail() != null
                    && !homeInfoConfig.getCustomerOnlyData().getEmail().isEmpty()) {
                bannersDTFResponse = ApiGatewayService.get().getBannerHome(homeInfoConfig.getCustomerOnlyData().getId(), homeInfoConfig
                        .getCustomerOnlyData()
                        .getEmail(), null, null, Objects.nonNull(homeInfoConfig.getHomeRequest()) && Objects.nonNull(homeInfoConfig.getHomeRequest().getCity()) ? homeInfoConfig.getHomeRequest().getCity() : null, false
                );

            } else {
                bannersDTFResponse = ApiGatewayService.get().getBannerHome(null, null, null, null, Objects.nonNull(homeInfoConfig.getHomeRequest()) && Objects.nonNull(homeInfoConfig.getHomeRequest().getCity()) ? homeInfoConfig.getHomeRequest().getCity() : null, false);
            }
        } catch (Exception e) {
            bannersDTFResponse = Optional.empty();
            e.printStackTrace();
        }
        return bannersDTFResponse.orElse(null);
    }

    @org.jetbrains.annotations.Nullable
    private BannersDTFRes getBannersDTFMinLeftV1(HomeInfoConfig homeInfoConfig) {
        // duartion time  response banners

        BannersDTFRes bannersDTFResponse = new BannersDTFRes();

        EnableForEnum sourceEnum = homeInfoConfig.getHomeRequest().getSource();

        if (sourceEnum != null) {
            switch (sourceEnum) {
                case WEB:
                    bannersDTFResponse = getBannersDTFMinLeft(homeInfoConfig);
//                    log.info("getBannersDTFMinLeft -> " + bannersDTFResponse);
                    break;
            }
        }
        if (homeInfoConfig.getCustomerOnlyData() != null
                && Objects.requireNonNull(homeInfoConfig.getCustomerOnlyData()).getId() != null
                && homeInfoConfig.getCustomerOnlyData().getId() == 0
                && bannersDTFResponse != null
                && bannersDTFResponse.getData() != null
                && bannersDTFResponse.getData().getDesktop() != null
                && bannersDTFResponse.getData().getDesktop().getMainBanner() != null){

            bannersDTFResponse.getData().getDesktop().getMainBanner().removeIf(data -> Objects.equals(data.getOrder(), 0));
        }else if (bannersDTFResponse == null || bannersDTFResponse.getData() == null || bannersDTFResponse.getData().getDesktop() == null || bannersDTFResponse.getData().getDesktop().getMainBanner() == null){
            // Poner un banner por defecto min left cuando es null
//            log.info("homeInfoConfig.getCustomerOnlyData().getEmail() -> " + homeInfoConfig.getCustomerOnlyData().getEmail());
//            log.info("homeInfoConfig.getCustomerOnlyData().getDocumentType() -> " + homeInfoConfig.getCustomerOnlyData().getDocumentType());
//            log.info("homeInfoConfig.getCustomerOnlyData().getFirstName() -> " + homeInfoConfig.getCustomerOnlyData().getFirstName());
            String json = "{\"code\":\"OK\",\"message\":\"Success\",\"data\":{\"desktop\":{\"mainBanner\":[{\"idBanner\":\"11757\",\"urlBanner\":\"https://lh3.googleusercontent.com/xMhspwTY9A0ZNVic86k3ZdmBZTKkpQCzq6lhY0dreOl7_aMQ0v8YROnuI_QsHBJ0MYz-584qMnYbKkiUTsCgX3EFJK2si9zpVOPJhlAF33QCeI0\",\"redirectUrl\":\"https://www.farmatodo.com.co/destacados/44497?utm_source=braze&utm_medium=banner_home&utm_campaign=saleoff&utm_content=panini&utm_term=general29ago\",\"idWebSafeBanner\":\"nsdfsbndfiojsdu84932o29430\",\"order\":1,\"directionBanner\":true,\"bannerWeb\":true,\"campaignName\":\"Isdin_ago22\",\"creative\":\"Isdin_ago22\",\"position\":\"MAIN_BANNER_ABOVE\",\"listClusteres\":[0 ],\"category\":0,\"home\":true },{\"idBanner\":\"11727\",\"urlBanner\":\"https://lh3.googleusercontent.com/hDctmT11gfP5D6x0LT90Dhs0jG8LgKbzjwttPqnA_PfTMuc2dijhovtO6Zx36zI1QGPVWqpCewHjO6W21xprZUG_lr6Kso-yhcpEN6OBUuf1jP8i\",\"redirectUrl\":\"https://www.farmatodo.com.co/destacados/42994?utm_source=braze&utm_medium=banner_home&utm_campaign=supplier&utm_content=bdf&utm_term=nivealuminous02ago\",\"idWebSafeBanner\":\"nsdfsbndfiojsdu84932o29430\",\"order\":1,\"directionBanner\":true,\"bannerWeb\":true,\"campaignName\":\"Nivea_AGO22\",\"creative\":\"Nivea_AGO22\",\"position\":\"MAIN_BANNER_BELOW\",\"listClusteres\":[0 ],\"category\":0,\"home\":true }],\"leftAdvertising\":[],\"rightAdvertising\":[],\"staticBanner\":[],\"categoriesAdvertising\":[]}}}";
            bannersDTFResponse = new Gson().fromJson(json, BannersDTFRes.class);
        }
        return bannersDTFResponse;
    }

    private BannersDTFRes getBannersDTFMinLeft(HomeInfoConfig homeInfoConfig) {

        Optional<BannersDTFRes> bannersDTFResponse;
        String city = Objects.nonNull(homeInfoConfig.getHomeRequest()) ? homeInfoConfig.getHomeRequest().getCity() : "BOG";
        Integer id = Objects.nonNull(homeInfoConfig.getCustomerOnlyData()) ? homeInfoConfig.getCustomerOnlyData().getId() : 0;
        try {
            if (homeInfoConfig.getCustomerOnlyData() != null
                    && homeInfoConfig.getCustomerOnlyData().getEmail() != null
                    && !homeInfoConfig.getCustomerOnlyData().getEmail().isEmpty()) {


                bannersDTFResponse = ApiGatewayService.get().getBannerHomeMinLeft(id, city,homeInfoConfig
                        .getCustomerOnlyData()
                        .getEmail()
                );

            } else {
                bannersDTFResponse = ApiGatewayService.get().getBannerHomeMinLeft(id, city, null);
            }
        } catch (Exception e) {
            bannersDTFResponse = null;
            e.printStackTrace();
        }
        if(bannersDTFResponse.isPresent()){
            return bannersDTFResponse.get();
        }else{
            return null;
        }
    }


}
