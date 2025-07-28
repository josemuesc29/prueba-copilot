package co.farmatodo.cv.core.application;

import co.farmatodo.cv.core.api.domain.shoppingcart.IndividualStoreStockResponseDomain;
import co.farmatodo.cv.core.api.domain.shoppingcart.StoreStockInfo;
import co.farmatodo.cv.core.api.domain.shoppingcart.StoreStockRequestDomain;
import co.farmatodo.cv.core.api.domain.shoppingcart.StoreStockResponseDomain;
import co.farmatodo.cv.core.api.events.ResponseEvent;
import co.farmatodo.cv.core.api.manager.shoppingcart.StoreStockManager;
import co.farmatodo.cv.core.application.entity.datafoundation.MaxStockItem;
import co.farmatodo.cv.core.application.entity.datafoundation.StoreStock;
import co.farmatodo.cv.core.application.repository.datafoundation.MaxStockItemRepository;
import co.farmatodo.cv.core.application.repository.datafoundation.StoreStockRepository;
import co.farmatodo.cv.core.application.services.algolia.AlgoliaService;
import co.farmatodo.cv.core.application.services.algolia.models.ExtendedBagPropertiesTalonOne;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StoreStockManagerHandler implements StoreStockManager {

    private static final Logger LOG = LoggerFactory.getLogger(StoreStockManagerHandler.class);

    @Autowired
    private StoreStockRepository storeStockRepository;
    @Autowired
    private AlgoliaService algoliaService;
    @Autowired
    private MaxStockItemRepository maxStockItemRepository;

    /**
     * Get total stock for an item in a list of stores.
     * @param storeStockRequestDomain Request domain with the item id and the list of store ids.
     * @return ResponseEvent with the total stock for the item in the stores.
     */
    @Override
    public ResponseEvent<StoreStockResponseDomain> getTotalStock(StoreStockRequestDomain storeStockRequestDomain) {

        if (storeStockRequestDomain == null) {
            LOG.error("StoreStockRequestDomain is null");
            return new ResponseEvent<StoreStockResponseDomain>().badRequest("Invalid request: Request body is null");
        }

        Long itemId = storeStockRequestDomain.getItemId();
        List<Long> storeIds = storeStockRequestDomain.getStoreIds();

        if (itemId == null || itemId <= 0) {
            LOG.error("Invalid itemId: {}", itemId);
            return new ResponseEvent<StoreStockResponseDomain>().badRequest("Invalid itemId");
        }

        if (storeIds.isEmpty()) {
            LOG.error("StoreIds list is null or empty");
            return new ResponseEvent<StoreStockResponseDomain>().badRequest("Invalid storeIds: List is null or empty");
        }

        try {
            int total = 0;
            Optional<ExtendedBagPropertiesTalonOne> bagIdTalonOne = algoliaService.getExtendedBagPropertiesTalonOne();
            if (bagIdTalonOne.isPresent() &&  bagIdTalonOne.get().getSku().contains(itemId)) {
                total = bagIdTalonOne.get().getFixedStock();
            }else {
                int maxStockItem = maxStockItemRepository.findById(itemId)
                        .map(MaxStockItem::getMaxStock)
                        .orElse(0);

                int totalStock = storeStockRepository.getTotalStockForItemInStores(itemId, storeIds)
                        .orElse(0);

                total = maxStockItem > 0 ? Math.min(maxStockItem, totalStock) : totalStock;
            }
            StoreStockResponseDomain storeStockResponseDomain = new StoreStockResponseDomain(total);

            return new ResponseEvent<StoreStockResponseDomain>().ok(HttpStatus.OK.name(), storeStockResponseDomain);
        } catch (Exception e) {
            LOG.error("Error fetching total stock for itemId {} in stores {}: {}", itemId, storeIds, e.getMessage(), e);
            return new ResponseEvent<StoreStockResponseDomain>().bussinesError("Error fetching total stock");
        }
    }

    @Override
    public ResponseEvent<IndividualStoreStockResponseDomain> getIndividualStock(StoreStockRequestDomain storeStockRequestDomain) {
        if (storeStockRequestDomain == null) {
            LOG.error("StoreStockRequestDomain is null");
            return new ResponseEvent<IndividualStoreStockResponseDomain>().badRequest("Invalid request: Request body is null");
        }

        Long itemId = storeStockRequestDomain.getItemId();
        List<Long> storeIds = storeStockRequestDomain.getStoreIds();

        if (itemId == null || itemId <= 0) {
            LOG.error("Invalid itemId: {}", itemId);
            return new ResponseEvent<IndividualStoreStockResponseDomain>().badRequest("Invalid itemId");
        }

        if (storeIds.isEmpty()) {
            LOG.error("StoreIds list is null or empty");
            return new ResponseEvent<IndividualStoreStockResponseDomain>().badRequest("Invalid storeIds: List is null or empty");
        }

        try {
            List<StoreStock> stocks = storeStockRepository.getStockByStoresAndItems(Collections.singletonList(itemId), storeIds);
            List<StoreStockInfo> stockInfos = stocks.stream()
                    .map(stock -> new StoreStockInfo(stock.getStore().getId(), Math.toIntExact(stock.getStock())))
                    .collect(Collectors.toList());

            IndividualStoreStockResponseDomain responseDomain = new IndividualStoreStockResponseDomain(stockInfos);
            return new ResponseEvent<IndividualStoreStockResponseDomain>().ok(HttpStatus.OK.name(), responseDomain);
        } catch (Exception e) {
            LOG.error("Error fetching individual stock for itemId {} in stores {}: {}", itemId, storeIds, e.getMessage(), e);
            return new ResponseEvent<IndividualStoreStockResponseDomain>().bussinesError("Error fetching individual stock");
        }
    }
}
