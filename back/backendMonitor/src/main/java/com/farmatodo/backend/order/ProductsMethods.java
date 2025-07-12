package com.farmatodo.backend.order;


import com.google.appengine.api.datastore.Query;
import com.imaginamos.farmatodo.model.product.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static com.farmatodo.backend.OfyService.ofy;

/**
 * Created by eric on 9/05/17.
 */

public class ProductsMethods {

  private static final Logger log = Logger.getLogger(Item.class.getName());

  public List<Item> getItemsByIds(List<Suggested> suggesteds, long idStoreGroup) {
    List<Long> idItems = new ArrayList<>();

    for (Suggested suggestedProducts : suggesteds) {
      idItems.add(suggestedProducts.getItem());
    }
    Query.Filter filterId = new Query.FilterPredicate("id", Query.FilterOperator.IN, idItems);

    List<Item> items = ofy().load().type(Item.class).filter(filterId).list();
    for (Item item : items) {
      this.setStoreInfo(item, idStoreGroup);
    }

    return items;
  }

  public Item setStoreInfo(Item item, long idStoreGroup) {
    if (item.getStoreInformation() != null) {
      for (StoreInformation storeInformation : item.getStoreInformation()) {
        if (storeInformation.getStoreGroupId() == idStoreGroup) {
          item.setFullPrice(storeInformation.getFullPrice());
          item.setOfferPrice(storeInformation.getOfferPrice());
          item.setOfferText(storeInformation.getOfferText());
          item.setOfferDescription(storeInformation.getOfferDescription());
          item.setTotalStock((int) storeInformation.getStock());
        }
      }
    }
    return item;
  }

  @Deprecated
  public List<CrossSales> itemToCrossSales(List<Item> items) {
    List<CrossSales> crossSales = new ArrayList<>();
    for (Item item : items) {
      CrossSales crossSale = new CrossSales();
      crossSale.setId(item.getId());
      crossSale.setIdStoreGroup(item.getIdStoreGroup());
      crossSale.setAnywaySelling(item.isAnywaySelling());
      crossSale.setBarcode(item.getBarcode());
      crossSale.setBrand(item.getBrand());
      crossSale.setFullPrice(item.getFullPrice());
      crossSale.setGeneric(item.getIsGeneric());
      crossSale.setGrayDescription(item.getGrayDescription());
      crossSale.setHighlight(item.isHighlight());
      crossSale.setMediaDescription(item.getMediaDescription());
      crossSale.setMediaImageUrl(item.getMediaImageUrl());
      crossSale.setOfferPrice(item.getOfferPrice());
      crossSale.setOutstanding(item.isOutstanding());
      crossSale.setRequirePrescription(item.getRequirePrescription());
      crossSale.setSales(item.getSales());
      crossSale.setSpaces(item.getSpaces());
      crossSale.setStatus(item.getStatus());
      crossSale.setTaxRate(item.getTaxRate());
      crossSale.setTotalStock(item.getTotalStock());
      crossSale.setOfferText(item.getOfferText());
      crossSale.setOfferDescription(item.getOfferDescription());
      crossSales.add(crossSale);
    }
    return crossSales;
  }

  @Deprecated
  public List<Substitutes> itemTosubstitutes(List<Item> items) {
    List<Substitutes> substitutes = new ArrayList<>();
    for (Item item : items) {
      Substitutes substitute = new Substitutes();
      substitute.setId(item.getId());
      substitute.setIdStoreGroup(item.getIdStoreGroup());
      substitute.setAnywaySelling(item.isAnywaySelling());
      substitute.setBarcode(item.getBarcode());
      substitute.setBrand(item.getBrand());
      substitute.setFullPrice(item.getFullPrice());
      substitute.setGeneric(item.getIsGeneric());
      substitute.setGrayDescription(item.getGrayDescription());
      substitute.setHighlight(item.isHighlight());
      substitute.setMediaDescription(item.getMediaDescription());
      substitute.setMediaImageUrl(item.getMediaImageUrl());
      substitute.setOfferPrice(item.getOfferPrice());
      substitute.setOutstanding(item.isOutstanding());
      substitute.setRequirePrescription(item.getRequirePrescription());
      substitute.setSales(item.getSales());
      substitute.setSpaces(item.getSpaces());
      substitute.setStatus(item.getStatus());
      substitute.setTaxRate(item.getTaxRate());
      substitute.setTotalStock(item.getTotalStock());
      substitute.setOfferText(item.getOfferText());
      substitute.setOfferDescription(item.getOfferDescription());
      substitutes.add(substitute);
    }
    return substitutes;
  }

}
