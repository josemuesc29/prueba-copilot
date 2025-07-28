package co.farmatodo.cv.adapter.shoppingcart.controller;

import co.farmatodo.cv.core.api.domain.shoppingcart.IndividualStoreStockResponseDomain;
import co.farmatodo.cv.core.api.domain.shoppingcart.StoreStockRequestDomain;
import co.farmatodo.cv.core.api.domain.shoppingcart.StoreStockResponseDomain;
import co.farmatodo.cv.core.api.events.ResponseEvent;
import co.farmatodo.cv.core.api.manager.shoppingcart.StoreStockManager;
import co.farmatodo.cv.core.application.util.response.ResponseEntityUtility;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/store-stock/v1")
@Api(value = "Store Stock controller")
public class StoreStockController {

    @Autowired
    private StoreStockManager storeStockManager;

    /**
     * Get total stock for an item in a list of stores.
     * @param storeStockRequestDomain Request domain with the item id and the list of store ids.
     * @return ResponseEvent with the total stock for the item in the stores.
     */
    @PostMapping("/total-stock")
    public ResponseEntity<ResponseEvent<StoreStockResponseDomain>> getTotalStock(
            @RequestBody StoreStockRequestDomain storeStockRequestDomain) {
        try {
            log.info("Get total stock. Request: {}", storeStockRequestDomain);
            ResponseEvent<StoreStockResponseDomain> responseEvent = storeStockManager.getTotalStock(storeStockRequestDomain);
            log.info("Total stock retrieved successfully. Response: {}", responseEvent);
            return ResponseEntityUtility.buildHttpResponse(responseEvent);
        }catch (Exception e){
            log.error("Error getting total stock. Request: {}. Error: {}", storeStockRequestDomain, e.getMessage(), e);
            return ResponseEntityUtility
                    .buildHttpResponse(new ResponseEvent<StoreStockResponseDomain>()
                            .bussinesError("Error getting total stock for item in stores"));
        }

    }

    @PostMapping("/individual-stock")
    public ResponseEntity<ResponseEvent<IndividualStoreStockResponseDomain>> getIndividualStock(
            @RequestBody StoreStockRequestDomain storeStockRequestDomain) {
        try {
            log.info("Get individual stock. Request: {}", storeStockRequestDomain);
            ResponseEvent<IndividualStoreStockResponseDomain> responseEvent = storeStockManager.getIndividualStock(storeStockRequestDomain);
            log.info("Individual stock retrieved successfully. Response: {}", responseEvent);
            return ResponseEntityUtility.buildHttpResponse(responseEvent);
        } catch (Exception e) {
            log.error("Error getting individual stock. Request: {}. Error: {}", storeStockRequestDomain, e.getMessage(), e);
            return ResponseEntityUtility
                    .buildHttpResponse(new ResponseEvent<IndividualStoreStockResponseDomain>()
                            .bussinesError("Error getting individual stock for item in stores"));
        }
    }
}
