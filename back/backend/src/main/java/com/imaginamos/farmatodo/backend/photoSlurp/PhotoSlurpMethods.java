package com.imaginamos.farmatodo.backend.photoSlurp;

import com.imaginamos.farmatodo.model.customer.Customer;
import com.imaginamos.farmatodo.model.dto.Component;
import com.imaginamos.farmatodo.model.dto.DynamicSection;
import com.imaginamos.farmatodo.model.home.HomeInfoConfig;
import com.imaginamos.farmatodo.model.photoSlurp.PhotoSlurpConfigAlgolia;
import com.imaginamos.farmatodo.model.productDetail.ItemInfoConfigData;
import com.imaginamos.farmatodo.networking.algolia.APIAlgolia;

import java.util.Objects;
import java.util.logging.Logger;

public class PhotoSlurpMethods {

    private static final Logger log = Logger.getLogger(PhotoSlurpMethods.class.getName());

    public static void setPhotoSlurpData(Component sectionAlgolia, DynamicSection dynamicSectionAux, HomeInfoConfig homeInfoConfig, ItemInfoConfigData itemInfoConfigData) {

        if (Objects.nonNull(sectionAlgolia.getDataFrom()) &&
                Objects.nonNull(sectionAlgolia.getDataFrom().getFrom())) {

            PhotoSlurpConfigAlgolia photoSlurpConfigAlgolia = APIAlgolia.PhotoSlurpConfig();


            if (photoSlurpConfigAlgolia != null) {
                dynamicSectionAux.setAlbumId(photoSlurpConfigAlgolia.getAlbumId());
                dynamicSectionAux.setTitle(photoSlurpConfigAlgolia.getTitlePhotoSlurp());
                dynamicSectionAux.setDescription(photoSlurpConfigAlgolia.getDescription());


                switch (sectionAlgolia.getDataFrom().getFrom()) {
                    case GET_ITEM:
                        dynamicSectionAux.setFilterProductId(Objects.requireNonNull(itemInfoConfigData.getItemData()).getItemId());
                        dynamicSectionAux.setFilterCategory(null);
                        break;
                    case GET_HOME:
                        dynamicSectionAux.setFilterProductId(photoSlurpConfigAlgolia.getFilterProductIdHome());
                        dynamicSectionAux.setFilterCategory(photoSlurpConfigAlgolia.getFilterCategoryHome());
                        break;
                    case GET_LANDING:
                        dynamicSectionAux.setFilterProductId(null);
                        dynamicSectionAux.setFilterCategory(Objects.requireNonNull(homeInfoConfig.getLandingPagesRequest()).getProvider());
                }
            }
        }
    }
    public static void setPhotoSlurpGridData( Component sectionAlgolia, DynamicSection dynamicSectionAux, HomeInfoConfig homeInfoConfig, ItemInfoConfigData itemInfoConfigData) {

        if (Objects.nonNull(sectionAlgolia.getDataFrom()) &&
                Objects.nonNull(sectionAlgolia.getDataFrom().getFrom())) {

            PhotoSlurpConfigAlgolia photoSlurpConfigAlgolia = APIAlgolia.PhotoSlurpConfig();

            if (photoSlurpConfigAlgolia != null) {
                dynamicSectionAux.setAlbumId(photoSlurpConfigAlgolia.getAlbumId());
                dynamicSectionAux.setTitle(photoSlurpConfigAlgolia.getTitlePhotoSlurpGrid());
                dynamicSectionAux.setDescription(null);


                switch (sectionAlgolia.getDataFrom().getFrom()) {
                    case GET_ITEM:
                        dynamicSectionAux.setFilterProductId(Objects.requireNonNull(itemInfoConfigData.getItemData()).getItemId());
                        dynamicSectionAux.setFilterCategory(null);
                        break;
                    case GET_HOME:
                        dynamicSectionAux.setFilterProductId(photoSlurpConfigAlgolia.getFilterProductIdHome());
                        dynamicSectionAux.setFilterCategory(photoSlurpConfigAlgolia.getFilterCategoryHome());
                        break;
                    case GET_LANDING:
                        dynamicSectionAux.setFilterProductId(null);
                        dynamicSectionAux.setFilterCategory(Objects.requireNonNull(homeInfoConfig.getLandingPagesRequest()).getProvider());
                }
            }
        }
    }
}
