package com.imaginamos.farmatodo.backend.algolia;

import com.google.api.server.spi.response.BadRequestException;
import com.imaginamos.farmatodo.backend.sim.domain.SimEndpointGuard;
import com.imaginamos.farmatodo.model.algolia.ItemAlgolia;
import com.imaginamos.farmatodo.model.product.Item;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.networking.growthbook.GrowthBookConfigLoader;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * @author Jhon Chaparro
 * @since 2022
 */
public class GuardAlgolia {
    public final static String CUSTOMER_ANONYMOUS = "ANONYMOUS";
    private static final Logger LOG = Logger.getLogger(SimEndpointGuard.class.getName());

    /***
     * validate minimal information
     * @param itemAlgolia
     * @throws BadRequestException
     */
    public static void validationItemsAlgolia(ItemAlgolia itemAlgolia) throws BadRequestException {
        if (Objects.isNull(itemAlgolia.getStock())) {
            LOG.warning("method: validationItemsAlgolia() --> BadRequest [itemAlgolia.getStock() is null]");
            throw  new BadRequestException(Constants.PRODUCT_NOT_EXISTS);
        }
        if (Objects.isNull(itemAlgolia.getTotalStock())) {
            LOG.warning("method: validationItemsAlgolia() --> BadRequest [itemAlgolia.getTotalStock() is null]");
            throw  new BadRequestException(Constants.PRODUCT_NOT_EXISTS);
        }
        if (Objects.isNull(itemAlgolia.getSales())) {
            LOG.warning("method: validationItemsAlgolia() --> BadRequest [itemAlgolia.getSales() is null]");
            throw  new BadRequestException(Constants.PRODUCT_NOT_EXISTS);
        }
        if (Objects.isNull(itemAlgolia.getMediaImageUrl())) {
            LOG.warning("method: validationItemsAlgolia() --> BadRequest [itemAlgolia.getMediaImageUrl() is null]");
            throw  new BadRequestException(Constants.PRODUCT_NOT_EXISTS);
        }
        if (Objects.isNull(itemAlgolia.getDescription())) {
            LOG.warning("method: validationItemsAlgolia() --> BadRequest [itemAlgolia.getDescription() is null]");
            throw  new BadRequestException(Constants.PRODUCT_NOT_EXISTS);
        }
        if (Objects.isNull(itemAlgolia.getFullPrice())) {
            LOG.warning("method: validationItemsAlgolia() --> BadRequest [itemAlgolia.getFullPrice() is null]");
            throw  new BadRequestException(Constants.PRODUCT_NOT_EXISTS);
        }
        if (Objects.isNull(itemAlgolia.getMediaDescription())) {
            LOG.warning("method: validationItemsAlgolia() --> BadRequest [itemAlgolia.getMediaDescription() is null]");
            throw  new BadRequestException(Constants.PRODUCT_NOT_EXISTS);
        }
        if (Objects.isNull(itemAlgolia.getBarcode())) {
            LOG.warning("method: validationItemsAlgolia() --> BadRequest [itemAlgolia.getBarcode() is null]");
            throw  new BadRequestException(Constants.PRODUCT_NOT_EXISTS);
        }
        if (Objects.isNull(itemAlgolia.getBrand())) {
            LOG.warning("method: validationItemsAlgolia() --> BadRequest [itemAlgolia.getBrand() is null]");
            throw  new BadRequestException(Constants.PRODUCT_NOT_EXISTS);
        }
        if (Objects.isNull(itemAlgolia.getDetailDescription())) {
            LOG.warning("method: validationItemsAlgolia() --> BadRequest [itemAlgolia.getDetailDescription() is null]");
            throw  new BadRequestException(Constants.PRODUCT_NOT_EXISTS);
        }
        if (Objects.isNull(itemAlgolia.getOfferPrice())) {
            LOG.warning("method: validationItemsAlgolia() --> BadRequest [itemAlgolia.getOfferPrice() is null]");
            throw  new BadRequestException(Constants.PRODUCT_NOT_EXISTS);
        }
        if (Objects.isNull(itemAlgolia.getId())) {
            LOG.warning("method: validationItemsAlgolia() --> BadRequest [itemAlgolia.getId() is null]");
            throw  new BadRequestException(Constants.PRODUCT_NOT_EXISTS);
        }
        if (Objects.isNull(itemAlgolia.getIdStoreGroup())) {
            LOG.warning("method: validationItemsAlgolia() --> BadRequest [itemAlgolia.getIdStoreGroup() is null]");
            throw  new BadRequestException(Constants.PRODUCT_NOT_EXISTS);
        }
        if (Objects.isNull(itemAlgolia.getSpaces())) {
            LOG.warning("method: validationItemsAlgolia() --> BadRequest [itemAlgolia.getSpaces() is null]");
            throw  new BadRequestException(Constants.PRODUCT_NOT_EXISTS);
        }
        if (Objects.isNull(itemAlgolia.getStatus())) {
            LOG.warning("method: validationItemsAlgolia() --> BadRequest [itemAlgolia.getStatus() is null]");
            throw  new BadRequestException(Constants.PRODUCT_NOT_EXISTS);
        }
        if (Objects.isNull(itemAlgolia.getTaxRate())) {
            LOG.warning("method: validationItemsAlgolia() --> BadRequest [itemAlgolia.getTaxRate() is null]");
            throw  new BadRequestException(Constants.PRODUCT_NOT_EXISTS);
        }
        if (Objects.isNull(itemAlgolia.getItem())) {
            LOG.warning("method: validationItemsAlgolia() --> BadRequest [itemAlgolia.getItem() is null]");
            throw  new BadRequestException(Constants.PRODUCT_NOT_EXISTS);
        }
        if (Objects.isNull(itemAlgolia.getDeliveryTime())) {
            LOG.warning("method: validationItemsAlgolia() --> BadRequest [itemAlgolia.getDeliveryTime() is null]");
            throw  new BadRequestException(Constants.PRODUCT_NOT_EXISTS);
        }
        if (Objects.isNull(itemAlgolia.getLargeDescription()) || Objects.isNull(itemAlgolia.getTextoSEO())) {
            LOG.warning("method: validationItemsAlgolia() --> BadRequest [getLargeDescription or getTextoSEO is null]");
            throw  new BadRequestException(Constants.PRODUCT_NOT_EXISTS);
        }
    }



    public static boolean validationItemsAlgoliaCart(ItemAlgolia itemAlgolia) {

        if (Objects.isNull(itemAlgolia.getItem())) {
            return true;
        }

        if (Objects.isNull(itemAlgolia.getStock())) {
            return true;
        }
        if (Objects.isNull(itemAlgolia.getTotalStock())) {
            return true;
        }
        if (Objects.isNull(itemAlgolia.getDescription())) {
            return true;
        }
        if (Objects.isNull(itemAlgolia.getFullPrice())) {
            return true;
        }
        if (Objects.isNull(itemAlgolia.getMediaDescription())) {
            return true;
        }
        if (Objects.isNull(itemAlgolia.getId())) {
            return true;
        }
        if (Objects.isNull(itemAlgolia.getIdStoreGroup())) {
            return true;
        }

        return false;
    }

    public static boolean validationMarketplaceActiveBySource( String source, int buildCodeNumberApp) {

        return  GrowthBookConfigLoader.isMarketplaceActiveBySource(CUSTOMER_ANONYMOUS, source, buildCodeNumberApp);

    }


    public static boolean validationItemsAlgoliaAddCart(Item itemAlgolia) {

        if (Objects.isNull(itemAlgolia.getItem())) {
            return true;
        }

        if (Objects.isNull(itemAlgolia.getStock())) {
            return true;
        }
        if (Objects.isNull(itemAlgolia.getTotalStock())) {
            return true;
        }
        if (Objects.isNull(itemAlgolia.getDirectionItem())) {
            return true;
        }
        if (Objects.isNull(itemAlgolia.getFullPrice())) {
            return true;
        }
        if (Objects.isNull(itemAlgolia.getMediaDescription())) {
            return true;
        }
        if (Objects.isNull(itemAlgolia.getId())) {
            return true;
        }
        if (Objects.isNull(itemAlgolia.getIdStoreGroup())) {
            return true;
        }

        return false;
    }
}
