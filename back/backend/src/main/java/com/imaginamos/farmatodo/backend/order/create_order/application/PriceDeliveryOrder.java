package com.imaginamos.farmatodo.backend.order.create_order.application;

import com.algolia.search.exceptions.AlgoliaException;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.ConflictException;
import com.imaginamos.farmatodo.model.order.DeliveryOrder;
import com.imaginamos.farmatodo.model.order.ShoppingCartJson;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public interface PriceDeliveryOrder {

    DeliveryOrder priceDeliveryOrder(ShoppingCartJson shoppingCartJson, HttpServletRequest request) throws ConflictException, BadRequestException, AlgoliaException, IOException;

}
