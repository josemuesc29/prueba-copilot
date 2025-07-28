package co.farmatodo.cv.adapter.shoppingcart.controller;

import co.farmatodo.cv.core.api.domain.oms.ShoppingCartDomainV2;
import co.farmatodo.cv.core.api.domain.oms.ShoppingCartResponseDomain;
import co.farmatodo.cv.core.api.events.ResponseEvent;
import co.farmatodo.cv.core.api.manager.oms.ShoppingCartManager;
import co.farmatodo.cv.core.application.util.response.ResponseEntityUtility;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/shopping-cart/v1/")
@Api(value = "ShoppingCart microservice", description = "This api allows the shopping cart service")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartManager manager;

    @PostMapping
    @ResponseBody
    @CrossOrigin
    @ApiOperation(value = "Calculate price delivery order")
    public ResponseEntity<ResponseEvent<ShoppingCartResponseDomain>> priceDelivery(@ApiParam @RequestBody ShoppingCartDomainV2 shoppingCartDomain) {
        log.debug("method: priceDelivery request ({})", shoppingCartDomain);
        ResponseEvent<ShoppingCartResponseDomain> responseEvent = manager.priceDelivery(shoppingCartDomain);
        return ResponseEntityUtility.buildHttpResponse(responseEvent);
    }
}