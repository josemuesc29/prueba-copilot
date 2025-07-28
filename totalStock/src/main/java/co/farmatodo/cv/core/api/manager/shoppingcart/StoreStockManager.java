package co.farmatodo.cv.core.api.manager.shoppingcart;

import co.farmatodo.cv.core.api.domain.shoppingcart.IndividualStoreStockResponseDomain;
import co.farmatodo.cv.core.api.domain.shoppingcart.StoreStockRequestDomain;
import co.farmatodo.cv.core.api.domain.shoppingcart.StoreStockResponseDomain;
import co.farmatodo.cv.core.api.events.ResponseEvent;

public interface StoreStockManager {
    ResponseEvent<StoreStockResponseDomain> getTotalStock(StoreStockRequestDomain storeStockRequestDomain);
    ResponseEvent<IndividualStoreStockResponseDomain> getIndividualStock(StoreStockRequestDomain storeStockRequestDomain);
}
