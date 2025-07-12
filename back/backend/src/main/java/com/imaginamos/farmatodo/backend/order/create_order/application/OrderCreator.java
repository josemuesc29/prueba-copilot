package com.imaginamos.farmatodo.backend.order.create_order.application;

import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.ConflictException;
import com.imaginamos.farmatodo.backend.order.create_order.domain.CommandCreateOrder;
import com.imaginamos.farmatodo.model.algolia.ItemAlgolia;
import com.imaginamos.farmatodo.model.order.CreatedOrder;
import com.imaginamos.farmatodo.model.provider.ProviderOrder;

import java.util.ArrayList;
import java.util.List;

public interface OrderCreator {

    CreatedOrder create(CommandCreateOrder commandCreateOrder) throws ConflictException, BadRequestException;


    default List<ItemAlgolia> emptyListOfItems() {
        List<ItemAlgolia> itemOrders = new ArrayList<>();
        ItemAlgolia itemOrder = new ItemAlgolia();
        itemOrder.setAccess(true);
        itemOrder.setCalculatedPrice(0);
        itemOrder.setDiscount(0.0);
        itemOrder.setFullPrice(0D);
        itemOrder.setItem(0);
        itemOrder.setPrice(0D);
        itemOrder.setQuantityBonus(0);
        itemOrder.setQuantityRequested(0);
        itemOrder.setItemDeliveryPrice(0);
        itemOrders.add(itemOrder);
        return itemOrders;
    }


    default List<ProviderOrder> emptyProviderOrder(){
        List<ProviderOrder> providers = new ArrayList<>();
        ProviderOrder provider = new ProviderOrder();
        provider.setName("");
        provider.setEmail("");
        provider.setDeliveryPrice(0);
        provider.setItems(emptyListOfItems());
        providers.add(provider);
        return providers;
    }

}
