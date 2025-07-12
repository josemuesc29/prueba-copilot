package com.imaginamos.farmatodo.backend.product;


import com.algolia.search.exceptions.AlgoliaException;
import com.google.api.server.spi.response.ConflictException;
import com.google.gson.Gson;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.imaginamos.farmatodo.backend.cache.CachedDataManager;
import com.imaginamos.farmatodo.backend.util.FTDUtil;
import com.imaginamos.farmatodo.backend.util.SubscribeAndSaveUtil;
import com.imaginamos.farmatodo.model.algolia.HitsItemsAlgolia;
import com.imaginamos.farmatodo.model.algolia.ItemAlgolia;
import com.imaginamos.farmatodo.model.algolia.ItemOfferAlgolia;
import com.imaginamos.farmatodo.model.algolia.*;
import com.imaginamos.farmatodo.model.categories.HighlightClass;
import com.imaginamos.farmatodo.model.customer.Customer;
import com.imaginamos.farmatodo.model.customer.SuggestedObject;
import com.imaginamos.farmatodo.model.customer.Suggesteds;
import com.imaginamos.farmatodo.model.dto.ComponentTypeEnum;
import com.imaginamos.farmatodo.model.dto.DynamicResponse;
import com.imaginamos.farmatodo.model.item.OpticalItemFilter;
import com.imaginamos.farmatodo.model.optics.AdditionalInformation;
import com.imaginamos.farmatodo.model.order.*;
import com.imaginamos.farmatodo.model.product.Highlight;
import com.imaginamos.farmatodo.model.product.Item;
import com.imaginamos.farmatodo.model.productDetail.ItemInfoConfigData;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.networking.algolia.APIAlgolia;
import com.imaginamos.farmatodo.model.product.*;
import com.imaginamos.farmatodo.model.util.URLConnections;
import com.imaginamos.farmatodo.networking.services.ApiGatewayService;
import com.imaginamos.farmatodo.networking.services.OpticsServices;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.imaginamos.farmatodo.backend.OfyService.ofy;

/**
 * Created by eric on 9/05/17.
 */

public class ProductsMethods {

  private static final Logger log = Logger.getLogger(Item.class.getName());
  private static final int MINIMUM_STOCK = 10;

  public List<Item> getItemsByIds(List<Suggested> suggesteds, long idStoreGroup) {
//      log.info("method getItemsByIds -> suggesteds storeid- > " +  idStoreGroup);
    return getItemsBySuggestedIdsAndStoreGroup(suggesteds, idStoreGroup);
  }

  public Item setStoreInfo(Item item, long idStoreGroup) {
    if(Objects.nonNull(item.getStoreInformation())){
        item.getStoreInformation().stream()
              .filter(storeInformation -> storeInformation.getStoreGroupId() == idStoreGroup)
              .forEach(storeInformation -> {
                item.setFullPrice(storeInformation.getFullPrice());
                item.setOfferPrice(storeInformation.getOfferPrice());
                item.setOfferText(storeInformation.getOfferText());
                item.setOfferDescription(storeInformation.getOfferDescription());
                item.setTotalStock((int) storeInformation.getStock());
      });
    }
    return item;
  }

    @org.jetbrains.annotations.Nullable
    public Suggesteds getSuggesteds(int idStoreGroup, Integer segmentId) throws IOException {
        //Suggesteds suggesteds = CoreService.get().getSuggestsBySegment(0);

        if (segmentId == null){
            segmentId = 0;
        }

        Suggesteds suggesteds = ApiGatewayService.get().getSuggestedById(segmentId);

        if (suggesteds == null){
            return null;
        }
        //log.info("data_suggesteds -> " + suggesteds);

        if(Objects.nonNull(suggesteds)) {
            List<SuggestedObject> suggestedObjects = new ArrayList<>();
            suggestedObjects.addAll(suggesteds.getSuggestsList().stream().filter(suggestion -> !suggestion.getType().equals("UNIQUE")).collect(Collectors.toList()));
            List<SuggestedObject> listIdSuggested = suggesteds.getSuggestsList().stream().filter(suggestion -> suggestion.getType().equals("UNIQUE") && Objects.nonNull(suggestion.getId()) && !suggestion.getId().isEmpty()).collect(Collectors.toList());
//            log.info("method:  getSuggesteds idStoreGroup: " + idStoreGroup + " Date: " + new DateTime());
            List<SuggestedObject> result = this.getItemAlgoliaByIdSuggested(listIdSuggested, idStoreGroup);
            Stream<SuggestedObject> combinedResult = Objects.nonNull(suggestedObjects) && Objects.nonNull(result) ? Stream.concat(suggestedObjects.stream(), result.stream()) :
                    (Objects.nonNull(suggestedObjects)) ? suggestedObjects.stream() :
                            Objects.nonNull(result) ? result.stream() : null;

            suggesteds.getSuggestsList().clear();
            suggesteds.getSuggestsList().addAll(combinedResult.collect(Collectors.toList()));
            if(Objects.nonNull(suggesteds.getSuggestsList()) && !suggesteds.getSuggestsList().isEmpty()){
                suggesteds.getSuggestsList().removeIf(suggest -> suggest.getType().equals("UNIQUE") && (Objects.isNull(suggest.getProduct()) ||  suggest.getProduct().isEmpty()));
            }

//            log.info("method: END1 getSuggesteds idStoreGroup: " + idStoreGroup + " Date: " + new DateTime());
            if (suggesteds == null || suggesteds.getSuggestsList() == null || suggesteds.getSuggestsList().isEmpty()){
                return null;
            }




            setItemUrlToSuggesteds(suggesteds);
            //log.warning("setea items prime sugeridos");
            setItemPrimeToSuggesteds (suggesteds);

//            log.info("method: END2  getSuggesteds idStoreGroup: " + idStoreGroup + " Date: " + new DateTime());
            return suggesteds;
        }
        return null;
    }

    private void setItemUrlToSuggesteds(Suggesteds suggesteds) {

      if (suggesteds != null && suggesteds.getSuggestsList() != null && !suggesteds.getSuggestsList().isEmpty()){
          try {
              for (int i = 0; i < suggesteds.getSuggestsList().size(); i++) {
                  suggesteds.getSuggestsList().get(i).getProduct().forEach(product -> {
                      final ItemAlgolia itemAlgolia = APIAlgolia.getItemAlgolia(product.getId() + "" + URLConnections.MAIN_ID_STORE);
                      product.setItemUrl(itemAlgolia.getItemUrl()
                      );
                  });
              }
          } catch (Exception e) {
              log.warning("ERROR No grave en setItemUrlToSuggesteds. Message: " + e.getMessage());
          }
      }

    }



    private void setItemPrimeToSuggesteds(Suggesteds suggesteds) {

        if (suggesteds != null && suggesteds.getSuggestsList() != null && !suggesteds.getSuggestsList().isEmpty()){
            try {
                for (int i = 0; i < suggesteds.getSuggestsList().size(); i++) {
                    suggesteds.getSuggestsList().get(i).getProduct().forEach(product -> {
                        //PRIME test
                        product.setPrimeTextDiscount("10%");
                        product.setPrimeDescription("prime");
                        double discount= product.getFullPrice()-product.getFullPrice()*0.1;
                        product.setPrimePrice(discount);
                    });
                }
            } catch (Exception e) {
                log.warning("ERROR No grave en setItemPrimeToSuggesteds. Message: " + e.getMessage());
            }
        }

    }


    public List<CrossSales> itemToCrossSales(List<Item> items) {
        List<CrossSales> crossSales = new ArrayList<>();

        if (Objects.nonNull(items) && !items.isEmpty()) {
            for (Item item : items) {
                try {
                    if (item.getTotalStock() > 0) {
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

                        crossSale.setMarca(item.getMarca());
                        crossSale.setCategorie(item.getCategorie());
                        crossSale.setDepartments(item.getDepartments());
                        crossSale.setSubCategory(item.getSubCategory());
                        crossSale.setSupplier(item.getSupplier());

                        // pre-render
                        crossSale.setItemUrl(item.getItemUrl());

                        crossSales.add(crossSale);
                    }
                } catch (Exception e) {
                    log.info("El item No tiene el campo TotalStock en Algolia -> " + e.getMessage());
                }
            }
        }
        return crossSales;
    }

  public List<Substitutes> itemTosubstitutes(List<Item> items) {
    List<Substitutes> substitutes = new ArrayList<>();
    if(Objects.nonNull(items) && !items.isEmpty()) {
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

            substitute.setMarca(item.getMarca());
            substitute.setCategorie(item.getCategorie());
            substitute.setDepartments(item.getDepartments());
            substitute.setSubCategory(item.getSubCategory());
            substitute.setSupplier(item.getSupplier());
            substitute.setFilters(item.getFilters());
            substitute.setOnlyOnline(item.isOnlyOnline());
            substitute.setSubscribeAndSave(Objects.isNull(item.getSubscribeAndSave()) ? false : item.getSubscribeAndSave());

            // pre-render
            substitute.setItemUrl(item.getItemUrl());

            substitutes.add(substitute);

        }
    }
    return substitutes;
  }

  public Item setStockAndFullPriceToStoreInfo(Item item, long idStoreGroup) {
    if(Objects.nonNull(item.getStoreInformation())){
      item.getStoreInformation().stream()
              .filter(storeInformation -> storeInformation.getStoreGroupId() == idStoreGroup)
              .forEach(storeInformation -> {
                item.setFullPrice(storeInformation.getFullPrice());
                item.setTotalStock((int) storeInformation.getStock());
              });
    }
    return item;
  }

    @NotNull
    public List<Highlight> getHighlightsFromDeptAndStore(long idDepartment, long idStoreGroup) {
//        log.info("method:  getHighlightsFromDeptAndStore idStoreGroup: " + idStoreGroup +" idDepartment: "+idDepartment + " Date: " + new DateTime());
        List<Highlight> highlightedItems = getHighlightsItems(idDepartment);
        List<Highlight> highlightList = new ArrayList<>();
        Long current = System.currentTimeMillis();
        highlightList.addAll(highlightedItems.stream().filter(highlight -> highlight.getStartDate() < current && current <= highlight.getEndDate() && !highlight.getType().equals("UNIQUE")).collect(Collectors.toList()));
        List<Long> listIdItem = getItemsLongListFromHighlights(highlightedItems, current);
        List<Item> listItem = getItemsByIdsAndStore(listIdItem, idStoreGroup);
        highlightedItems.stream().filter(highlight -> highlight.getStartDate() < current && current <= highlight.getEndDate() && highlight.getType().equals("UNIQUE")).forEach(highlight -> {
            List<Item> unique = new ArrayList<>();
            Optional<Item> item = listItem.stream().filter(itemResult -> itemResult.getId() == highlight.getItems().get(0).getItem()).findFirst();
            if (item.isPresent() && item.get().getTotalStock() > 0) {
                unique.add(item.get());
                highlight.setProduct(unique);
                highlight.setItem(item.get().getId());
                highlightList.add(highlight);
            }
        });
        if(Objects.nonNull(highlightedItems) && !highlightedItems.isEmpty()){
            highlightedItems.removeIf(highlight -> highlight.getType().equals("UNIQUE") && (Objects.isNull(highlight.getProduct()) || highlight.getProduct().isEmpty()));
        }
//        log.info("method: END getHighlightsFromDeptAndStore idStoreGroup: " + idStoreGroup +" idDepartment: "+idDepartment + " Date: " + new DateTime());
        return highlightList;
    }

  public Item setFindInformationToAlgolia(Item item, long idStoreGroup){
//    log.info("method setFindInformationToAlgolia -> " + item.getItemId()+"storeid- > " +  idStoreGroup);
    return setItemToItemAlgolia(item, idStoreGroup, null);
  }

  public Item setFindInformationToAlgoliaByIdItem(String idItem, long idStoreGroup, Integer quantity){
    Item item = new Item();
    item.setId(Long.parseLong(idItem));
    item.setItemId(idItem);
    return setItemToItemAlgolia(item, idStoreGroup, quantity);
  }

    /**
     * Method to fill item from Algolia via API
     * @param idItem id item
     * @param nearbyStores nearby stores
     * @return item
     */
    public Optional<Item> fillItemFromAlgolia(String idItem, String nearbyStores) {
        try {
            Item item = new Item();
            item.setId(Long.parseLong(idItem));
            item.setItemId(idItem);
            Optional<ItemAlgolia> optionalItemAlgolia = APIAlgolia.getItemAlgoliaRestAPI(idItem, nearbyStores);
            if (optionalItemAlgolia.isEmpty()) {
                log.warning("No item found for id: " + idItem);
                return Optional.empty();
            }
            ItemAlgolia itemAlgolia  = optionalItemAlgolia.get();
            APIAlgolia.getItemToItemAlgolia(item, itemAlgolia);
            addDeliveryTimeOptics(item, itemAlgolia);
            return Optional.of(item);
        } catch (Exception e) {
            log.severe("ERROR_ALGOLIA -> fillItemFromAlgolia " + e);
            return Optional.empty();
        }
    }

    private static void addDeliveryTimeOptics(Item item, ItemAlgolia itemAlgolia) {
        final long DEFAULT_STORE = 26L;
        if(Objects.nonNull(itemAlgolia.getItemOpticsComplete())){
            List<AdditionalInformation> additionalInformationList = item.getItemOpticsComplete().getAdditionalInformation().getAdditionalInformationList();
            OpticsServices opticsServices = new OpticsServices();
            AdditionalInformation deliveryTimeOptics =
                    opticsServices.getDeliveryTimeOptics(itemAlgolia, DEFAULT_STORE);
            opticsServices.addFiltersOptical(itemAlgolia, item, null);
            additionalInformationList.add(deliveryTimeOptics);
        }
    }

    public Item setFindInformationOpticalToAlgoliaByIdItem(String idItem, long idStoreGroup, OpticalItemFilter opticalItemFilter, Integer quantity){
        Item item = new Item();
        item.setId(Long.parseLong(idItem));
        item.setItemId(idItem);
        final String filters = opticalItemFilter.toString();
        item.setFiltersOptical(filters);
        return setItemOpticalToItemAlgolia(item, idStoreGroup, opticalItemFilter, quantity);
    }

  public Item setFindInformationToAlgoliaByBarcode(String barcode, long idStoreGroup){
//      log.info("method setFindInformationToAlgoliaByBarcode -> " + barcode+" storeid- > " +  idStoreGroup);
      return getItemAlgoliaByBarcode(barcode,idStoreGroup);
  }

  public Item setFindInformationToAlgoliaByBarcodeSag(String barcode, long idStoreGroup){
//      log.info("method setFindInformationToAlgoliaByBarcodeSag -> " + barcode+" storeid- > " +  idStoreGroup);
      return getItemAlgoliaByBarcodeSag(barcode,idStoreGroup);
  }



  public static DeliveryOrderItem setInformationToAlgoliaByDeliveryItem(DeliveryOrderItem deliveryItem, long idStoreGroup){
    //log.warning("method setFindInformationToAlgolia -> " + deliveryItem.getId()+"storeid- > " +  idStoreGroup);
    ItemAlgolia itemAlgolia = APIAlgolia.getItemAlgolia(""+deliveryItem.getId()+idStoreGroup);
    if(Objects.nonNull(itemAlgolia)) {
        deliveryItem.setCategorie(itemAlgolia.getCategorie());
        deliveryItem.setMarca(itemAlgolia.getMarca());
        deliveryItem.setDepartments(itemAlgolia.getDepartments());
        deliveryItem.setSubCategory(itemAlgolia.getSubCategory());
        deliveryItem.setSupplier(itemAlgolia.getSupplier());
        deliveryItem.setBarcode(itemAlgolia.getBarcode());

        try {
            deliveryItem.setBarcodeList(itemAlgolia.getBarcodeList());
        }catch (Exception e){
            log.warning("Error -> El item tiene probblemas con la lista de barcodes " + itemAlgolia.getId());
        }

    }
    return deliveryItem;
  }
  public DeliveryOrderItem itemOfferPopUp(DeliveryOrderItem deliveryItem){
      ItemOfferAlgolia itemOfferAlgolia = APIAlgolia.getItemOfferAlgolia(deliveryItem.getOfferText());
      if(Objects.nonNull(itemOfferAlgolia)) {
          deliveryItem.setOfferText(itemOfferAlgolia.getOfferTextTwoForOne());
      }
          /*if(deliveryItem.setOfferText(itemOfferAlgolia.getOfferTextTwoForOne()){

              return deliveryItem;
          }*/
      return deliveryItem;


  }

  public List<Item> getItemsBySuggestedIdsAndStoreGroup(List<Suggested> suggesteds, long idStoreGroup) {
    // log.warning("method getItemsBySuggestedIdsAndStoreGroup -> " + suggesteds  + " storeid- > " + idStoreGroup);
    if(Objects.nonNull(suggesteds) && !suggesteds.isEmpty()) {
        List<String> ids = suggesteds
                .stream()
                .map(Suggested::getItem)
                .map(String::valueOf)
                .distinct()
                .collect(Collectors.toList());

        List<ItemQuery> listItemQuery = suggesteds.stream().map(item -> new ItemQuery("" + item.getItem() + (idStoreGroup > 0 ? idStoreGroup : URLConnections.MAIN_ID_STORE))).collect(Collectors.toList());
        List<ItemAlgolia> itemAlgoliaList = APIAlgolia.findItemByIdList(listItemQuery);
        itemAlgoliaList.sort(Comparator.comparing(v -> ids.indexOf(v.getId())));
        if(Objects.nonNull(itemAlgoliaList) && !itemAlgoliaList.isEmpty()){
            return itemAlgoliaList.stream().filter(itemAlgolia -> Objects.nonNull(itemAlgolia) && Objects.nonNull(itemAlgolia.getId()) && !itemAlgolia.getId().isEmpty())
                .map(itemAlgolia -> APIAlgolia.getItemToItemAlgolia(new Item(), itemAlgolia)).collect(Collectors.toList());
        }
    }
    return null;
  }

  public List<Item> getItemsByIdsAndStore(List<Long> items, long idStoreGroup) {
    if(Objects.nonNull(items) && !items.isEmpty()) {
        List<ItemQuery> listItemQuery = items.stream().map(item -> new ItemQuery("" + item + (idStoreGroup > 0 ? idStoreGroup : URLConnections.MAIN_ID_STORE))).collect(Collectors.toList());
        List<ItemAlgolia> itemAlgoliaList = APIAlgolia.findItemByIdList(listItemQuery);
        return itemAlgoliaList.stream().filter(itemAlgolia -> Objects.nonNull(itemAlgolia) && Objects.nonNull(itemAlgolia.getId()) && !itemAlgolia.getId().isEmpty())
            .map(itemAlgolia -> APIAlgolia.getItemToItemAlgolia(new Item(), itemAlgolia)).collect(Collectors.toList());
    }
    //log.warning("getItemsByIdsAndStore return null");
    return null;
  }
    @NotNull
    public List<Long> getItemsLongListFromHighlights(List<Highlight> highlightedItems, Long current) {
        return highlightedItems.stream().filter(highlight -> highlight.getStartDate() < current && current <= highlight.getEndDate() && highlight.getType().equals("UNIQUE") && Objects.nonNull(highlight.getItems()) && !highlight.getItems().isEmpty()).map(highlight -> highlight.getItems().get(0).getItem()).collect(Collectors.toList());
    }

    @NotNull
    public List<Highlight> getHighlightsItems(long idDepartment) {
        List<com.imaginamos.farmatodo.model.algolia.Highlight> highlightListAlgolia = APIAlgolia.getListHighlight(idDepartment);
//        log.info("Algolia : " + highlightListAlgolia);
        return highlightListAlgolia.stream().map(highlightAlg -> {
            Highlight highlight = new Highlight();
            highlight.setHighlightId(highlightAlg.getObjectID());
            highlight.setId(highlightAlg.getId());
            highlight.setFirstDescription(highlightAlg.getFirstDescription());
            highlight.setSecondDescription(highlightAlg.getSecondDescription());
            highlight.setOfferDescription(highlightAlg.getOfferDescription());
            highlight.setOfferText(highlightAlg.getOfferText());
            highlight.setType(highlightAlg.getType());
            highlight.setUrlImage(highlightAlg.getUrlImage());
            highlight.setStartDate(highlightAlg.getStartDate().getTime());
            highlight.setEndDate(highlightAlg.getEndDate().getTime());
            highlight.setOrderingNumber(highlightAlg.getOrderingNumber());
            highlight.setItem(highlightAlg.getItem());
            highlight.setItems((Objects.nonNull(highlightAlg.getItems()) && !highlightAlg.getItems().isEmpty()) ? highlightAlg.getItems().stream().map(item -> new Suggested(item)).collect(Collectors.toList()) : null);
            highlight.setCategories((Objects.nonNull(highlightAlg.getCategories()) && !highlightAlg.getCategories().isEmpty()) ? highlightAlg.getCategories().stream().map(category -> new HighlightClass(category)).collect(Collectors.toList()) : null);
            return highlight;
        }).collect(Collectors.toList());
    }

  public List<SuggestedObject>getItemAlgoliaByIdSuggested(final List<SuggestedObject> idSuggestedList, final int idStoreGroup) {
      // log.warning("method getItemAlgoliaByIdSuggested -> " + idSuggestedList + " storeid- > " + idStoreGroup);
    return APIAlgolia.getItemAlgoliaByIdSuggested(idSuggestedList, idStoreGroup);
  }


  private Item setItemToItemAlgolia(Item item, long idStoreGroup, Integer quantity){
    try{
      //log.debug("method setItemToItemAlgolia -> " + item.getItemId() +" storeid- > " +  idStoreGroup);
      ItemAlgolia itemAlgolia = APIAlgolia.getItemAlgolia(item.getItemId()+idStoreGroup);
//      log.info("itemAlgolia::"+itemAlgolia);
      if(Objects.nonNull(itemAlgolia)) {
          item = APIAlgolia.getItemToItemAlgolia(item, itemAlgolia);
          // optics delivery time and filters
          if(Objects.nonNull(item) && Objects.nonNull(item.getItemOpticsComplete())){
             List<AdditionalInformation> additionalInformationList = item.getItemOpticsComplete().getAdditionalInformation().getAdditionalInformationList();
              OpticsServices opticsServices = new OpticsServices();
              AdditionalInformation deliveryTimeOptics = opticsServices.getDeliveryTimeOptics(itemAlgolia, idStoreGroup);
              opticsServices.addFiltersOptical(itemAlgolia, item, quantity);
              additionalInformationList.add(deliveryTimeOptics);
          }
      }
      return item;
    }catch (Exception e) {
        log.info("ERROR-> " + e);
        return null;
    }

  }

    private Item setItemOpticalToItemAlgolia(Item item, long idStoreGroup, final OpticalItemFilter opticalItemFilter, Integer quantity){
        try{
            //log.debug("method setItemToItemAlgolia -> " + item.getItemId() +" storeid- > " +  idStoreGroup);
            ItemAlgolia itemAlgolia = APIAlgolia.getItemAlgoliaByFiltersOptical(opticalItemFilter, idStoreGroup);
            log.info("itemAlgolia::"+itemAlgolia);
            if(Objects.nonNull(itemAlgolia)) {
                item = APIAlgolia.getItemToItemAlgolia(item, itemAlgolia);
                if(Objects.nonNull(item)){
                    item.setQuantityRequest(quantity);
                    item.setDirectionItem(opticalItemFilter.getEyeDirection().name());
                }
            }
            log.info("quantityRequest::"+item.getQuantityRequest());
            return item;
        }catch (Exception e) {
            log.info("ERROR-> " + e);
            return null;
        }

    }

    private Item getItemAlgoliaByBarcode(String barcode, long idStoreGroup) {
      try{
          ItemAlgolia itemAlgolia = APIAlgolia.getItemAlgoliaByBarcode(barcode,idStoreGroup);
          Item item = new Item();
          if (itemAlgolia != null && !validateItemAlgolia(itemAlgolia)){
              item.setItemId(itemAlgolia.getId());
              getItemToItemAlgolia(item,itemAlgolia);
          }
          return (item.getId() > 0) ? item : null ;
      }catch (Exception e) {
          log.info("ERROR_ALGOLIA GETITEMALGOLIABYBARCODE-> " + e);
          return null;
      }
    }

    private Item getItemAlgoliaByBarcodeSag(String barcode, long idStoreGroup) {
        try{
            ItemAlgolia itemAlgolia = APIAlgolia.getItemAlgoliaByBarcodeSag(barcode,idStoreGroup);
            if(itemAlgolia == null || validateItemAlgolia(itemAlgolia)){
                itemAlgolia = ApiGatewayService.get().createItemAlgoliaSag(barcode,""+idStoreGroup);
                log.info(itemAlgolia != null ? "ItemID: " + itemAlgolia.getId():"Item null");
            }
            Item item = new Item();
            if (itemAlgolia != null){
                item.setItemId(itemAlgolia.getId());
                getItemToItemAlgolia(item,itemAlgolia);
                updateItemAlgoliaFromSag(itemAlgolia);
            }
            return (item.getId() > 0) ? item : null ;
        }catch (Exception e) {
            log.info("ERROR_ALGOLIA -> GETITEMALGOLIABYBARCODESAG " + e);
            return null;
        }

    }

    private void updateItemAlgoliaFromSag(ItemAlgolia itemAlgolia){

        ItemAlgolia algoliaProduct = APIAlgolia.getItemAlgolia(itemAlgolia.getObjectID());
        if(algoliaProduct != null){
            algoliaProduct.setBarcode(itemAlgolia.getBarcode());
            algoliaProduct.setTotalStock(itemAlgolia.getStock());
            algoliaProduct.setDescription(itemAlgolia.getDescription());
            algoliaProduct.setMediaDescription(itemAlgolia.getMediaDescription());
            algoliaProduct.setId(itemAlgolia.getId());
            algoliaProduct.setFullPrice(itemAlgolia.getFullPrice());
            algoliaProduct.setDetailDescription(itemAlgolia.getDetailDescription());
            algoliaProduct.setIdStoreGroup(itemAlgolia.getIdStoreGroup());
            algoliaProduct.setMediaImageUrl(itemAlgolia.getMediaImageUrl());

            APIAlgolia.updateItemAlgolia(algoliaProduct);

        } else {
            APIAlgolia.updateItemAlgolia(itemAlgolia);
        }
    }

    private boolean validateItemAlgolia(ItemAlgolia itemAlgolia){
      return (itemAlgolia != null && (itemAlgolia.getStock() == null
              || itemAlgolia.getFullPrice() == null || itemAlgolia.getMediaDescription() == null
              || itemAlgolia.getDescription() == null || itemAlgolia.getId() == null));
    }

  public Item getItemToItemAlgolia(Item item, ItemAlgolia itemAlgolia){
//      log.info("method getItemToItemAlgolia -> itemAlgolia.getId() " + itemAlgolia.getId() +" item.getItemId() -> " +  item.getItemId());
      item.setOnlyOnline(Objects.requireNonNull(itemAlgolia).isOnlyOnline());
      item.setDeliveryTime(itemAlgolia.getDeliveryTime());
      item.setId((Objects.nonNull(itemAlgolia.getId()) || Objects.nonNull(item.getItemId())) ?  Long.valueOf(Objects.nonNull(itemAlgolia.getId())?itemAlgolia.getId(): item.getItemId()) : 0L);
      item.setSales(itemAlgolia.getSales());
      item.setTaxRate(Objects.nonNull(itemAlgolia.getTaxRate()) ? itemAlgolia.getTaxRate() : 0);
      item.setMediaDescription(itemAlgolia.getMediaDescription());
      item.setFullPrice(Objects.nonNull(itemAlgolia.getFullPrice()) ? itemAlgolia.getFullPrice().intValue() : item.getFullPrice());
      if(Objects.nonNull(itemAlgolia.getOfferPrice())) {
          item.setOfferPrice(itemAlgolia.getOfferPrice());
          item.setOfferText(itemAlgolia.getOfferText());
          item.setOfferDescription(itemAlgolia.getOfferDescription());
      }
      item.setRequirePrescription(Objects.nonNull(itemAlgolia.getRequirePrescription()) ? itemAlgolia.getRequirePrescription().toString() : Boolean.FALSE.toString());
      item.setTotalStock(Objects.nonNull(itemAlgolia.getStock()) ? itemAlgolia.getStock() : item.getTotalStock());
      item.setHighlight(itemAlgolia.isHighlight());
      item.setGeneric(itemAlgolia.isGeneric());
      item.setIsGeneric(itemAlgolia.isGeneric());
      item.setLargeDescription(itemAlgolia.getLargeDescription());
      item.setAnywaySelling(itemAlgolia.isAnywaySelling());
      item.setGrayDescription(itemAlgolia.getDetailDescription());
      item.setOutstanding(itemAlgolia.isOutstanding());
      item.setSpaces(itemAlgolia.getSpaces() > 1 ? itemAlgolia.getSpaces() : item.getSpaces());
      item.setStatus(Objects.nonNull(itemAlgolia.getStatus()) ? itemAlgolia.getStatus() : item.getStatus());
      item.setTaxRate(itemAlgolia.getTaxRate());
      item.setListUrlImages(itemAlgolia.getListUrlImages());
      item.setMediaImageUrl(Objects.nonNull(itemAlgolia.getMediaImageUrl()) ? itemAlgolia.getMediaImageUrl() : item.getMediaImageUrl());
      item.setLargeDescription(Objects.nonNull(itemAlgolia.getLargeDescription()) ? itemAlgolia.getLargeDescription() : item.getLargeDescription());
      Double pumPrice;
      if (Objects.nonNull(itemAlgolia.getOfferPrice()) && itemAlgolia.getOfferPrice() > 0) {
          pumPrice = itemAlgolia.getOfferPrice();
      } else {
          pumPrice = itemAlgolia.getFullPrice();
      }
      if (Objects.nonNull(itemAlgolia.getMeasurePum()) && itemAlgolia.getMeasurePum() > 0) {
          String pum = generatePum(itemAlgolia.getLabelPum(), itemAlgolia.getMeasurePum(), pumPrice);
          item.setPum(pum);
      }
      item.setCategorie(itemAlgolia.getCategorie());
      item.setMarca(itemAlgolia.getMarca());

      //Subscribe And Save...
      item.setExpressWithSubscription(Objects.requireNonNull(itemAlgolia).isExpressWithSubscription());
      item.setSubscribeAndSave(Objects.requireNonNull(itemAlgolia).getSubscribeAndSave());
      item.setStandardDuration(itemAlgolia.getStandardDuration());

      item.setDepartments(itemAlgolia.getDepartments());
      item.setSubCategory(itemAlgolia.getSubCategory());
      item.setSupplier(itemAlgolia.getSupplier());
      item.setBarcode(itemAlgolia.getBarcode());
      return item;
  }


  private String generatePum(String label, float pum, Double itemPrice){
    Double pumPrice = Objects.nonNull(pum) ?  itemPrice/pum : null;
    return label.concat(String.format("%.2f", pumPrice));
  }

    public List<Substitutes> getItemSubstitutesByItemIdAndRequestedAndStoreId(final Long idItem, final Integer requestQuantity, final Integer storeId) {
        log.info("method  getItemSubstitutesByItemIdAndRequestedAndStoreId: idItem: " + idItem + " requestQuantity: " + requestQuantity + " storeId: " + storeId);
        try {
            List<Suggested> substituteList = APIAlgolia.getSubstitutesByItem(idItem);
            if (!substituteList.isEmpty()) {
                //log.warning("method  getItemSubstitutesByItemIdAndRequestedAndStoreId: Substitutes encontrados");
                // Se utiliza la tienda Principal de la ciudad que no todas las tiendas estan en algolia
                int idStoreGroup = APIAlgolia.getDefaultStoreIdByStoreId(storeId);
                List<Item> itemsSubstitutes = getItemsByIds(substituteList, idStoreGroup);
                //Valida los items con stock para la tienda
                //log.warning("method  getItemSubstitutesByItemIdAndRequestedAndStoreId: Inicia validación en tienda");
                ValidateStockRouteReq requestValidateStock = new ValidateStockRouteReq();
                requestValidateStock.setStores(Arrays.asList(storeId));

                List<ValidateStockRouteReq.Item> itemsToValidateStock =
                        substituteList.stream().map(item -> {
                            ValidateStockRouteReq.Item itemAux = new ValidateStockRouteReq().new Item();
                            itemAux.setItemId(item.getItem());
                            itemAux.setRequestQuantity(requestQuantity);
                            return itemAux;
                        }).collect(Collectors.toList());

                // set items to validate stock
                requestValidateStock.setItems(itemsToValidateStock);
                // call Core
                // ValidateStockRouteRes resCore = CoreService.get().validateStock(requestValidateStock); // old

                ValidateStockRouteRes responseStock = ApiGatewayService.get().validateStock(requestValidateStock);


                //log.warning("method  getItemSubstitutesByItemIdAndRequestedAndStoreId: Recibe respuesta de CORE: itemsSubstitutes: " + (Objects.nonNull(itemsSubstitutes) ? itemsSubstitutes.size() : 0));
                if (Objects.nonNull(itemsSubstitutes) && Objects.nonNull(responseStock.getData()) && Objects.nonNull(responseStock) && Objects.nonNull(responseStock.getData().getResult()) && !responseStock.getData().getResult().isEmpty()) {
                    //log.warning("method  getItemSubstitutesByItemIdAndRequestedAndStoreId: Valida respuesta de CORE");
                    List<Item> semiFinalSubstituteList = itemsSubstitutes.stream().filter(suggested ->
                            responseStock.getData().getResult().get(0).getItems().stream().filter(result -> result.getId() == suggested.getId() && result.isValid()).findFirst().isPresent()).collect(Collectors.toList());
                    //log.warning("method  getItemSubstitutesByItemIdAndRequestedAndStoreId: Valida respuesta de CORE");

                    // Ordena resultados por Proveedor
                    ItemAlgolia itemPrincipal = APIAlgolia.getItemAlgolia(idItem + URLConnections.MAIN_ID_STORE);
                    if (Objects.nonNull(itemPrincipal) && Objects.nonNull(itemPrincipal.getSupplier())) {
                        List<Item> orderSubstituteList = semiFinalSubstituteList.stream().filter(item -> itemPrincipal.getSupplier().equals(item.getSupplier())).collect(Collectors.toList());
                        semiFinalSubstituteList.removeIf(item -> itemPrincipal.getSupplier().equals(item.getSupplier()));
                        orderSubstituteList.addAll(semiFinalSubstituteList);
                        return itemTosubstitutes(orderSubstituteList);
                    }

                    return itemTosubstitutes(semiFinalSubstituteList);
                }
                log.warning("method  getItemSubstitutesByItemIdAndRequestedAndStoreId: Sin resultados del CORE");
                //return itemTosubstitutes(itemsSubstitutes);
            }
            return new ArrayList<>();
        } catch (IOException | AlgoliaException e) {
            log.warning("Error al consulta la disponibilidad de los items substitutos: " + e.fillInStackTrace());
            return null;
        }
    }

  public List<Substitutes> getItemSubstitutesFromAlgolia(
          long itemId, int requestQuantity, int storeId, boolean validateStock, float percentagePrice, List<Integer> finalPossibleStores) {

    // obtener item algolia....
    int idStoreGroup = 0;
    try {
      idStoreGroup = APIAlgolia.getDefaultStoreIdByStoreId(storeId);

      String descForSearch = "";

      ItemAlgolia itemAlgolia = APIAlgolia.getItemAlgolia(itemId + "" + idStoreGroup);

      if (itemAlgolia != null) {
        if (itemAlgolia.getDescription() != null && !itemAlgolia.getDescription().isEmpty()) {
          descForSearch = itemAlgolia.getDescription();
        } else if (itemAlgolia.getMediaDescription() != null && !itemAlgolia.getMediaDescription().isEmpty()) {
          descForSearch = itemAlgolia.getMediaDescription();
        }

        // add detail description.
        if (itemAlgolia.getDetailDescription() != null && !itemAlgolia.getDetailDescription().isEmpty()) {
          descForSearch = descForSearch + " " + itemAlgolia.getDetailDescription();
        }
      }
      String categorie =  itemAlgolia.getCategorie();
      List<Item> substitutesItems = getItemsBySearchDesc(descForSearch, idStoreGroup,null, itemId, requestQuantity ,validateStock ,finalPossibleStores, categorie);



      if (Objects.nonNull(substitutesItems) && !substitutesItems.isEmpty()){
          // order by brand.

          // log.info("item class validos para sustituir size-> " + substitutesItems.size());

//          substitutesItems.forEach( it -> {
//              log.info("sub->" + it.toStringJson());
//          });
          substitutesItems.sort((item1, item2) -> {
              if (Stream.of(item1,item2,item1.getSupplier(),item2.getSupplier()).allMatch(Objects::nonNull)){
                  if (item1.getSupplier().equalsIgnoreCase(itemAlgolia != null ? itemAlgolia.getSupplier() : "")){
                      return -1;
                  }else if (item2.getSupplier().equalsIgnoreCase(itemAlgolia != null ? itemAlgolia.getSupplier() : "")){
                      return 1;
                  }
              }
              return 0;
          } );

          // remove items with price does not match the parameter

          if (Stream.of(itemAlgolia, Objects.requireNonNull(itemAlgolia).getFullPrice()).allMatch(Objects::nonNull)
                  && itemAlgolia.getFullPrice() > 0){

              Double fullPrice = itemAlgolia.getFullPrice();

              Double maxValue = fullPrice + (fullPrice * percentagePrice / 100);
              Double minValue = fullPrice - (fullPrice * percentagePrice / 100);

              substitutesItems.removeIf(item -> (item.getFullPrice() > maxValue || item.getFullPrice() < minValue));

          }

          return itemTosubstitutes(substitutesItems);

      }

    } catch (AlgoliaException e) {
      e.printStackTrace();
    }

    return new ArrayList<>();
  }

    /**
     * Method to get substitutes by item and nearby stores separated by comma
     * Uses Algolia API and concatenates description, media description and detail description for search
     * @param itemInfoConfigData item info config data
     * @param requestQuantity request quantity
     * @param validateStock validate stock flag
     * @param percentagePrice percentage price to validate
     * @param finalPossibleStores final possible stores
     * @return list of items substitutes
     */
    public List<Item> getSubstitutesByItemAndNearbyStores(
            ItemInfoConfigData itemInfoConfigData,
            int requestQuantity,
            boolean validateStock,
            float percentagePrice,
            List<Integer> finalPossibleStores) {

        Optional<ItemInfoConfigData> OptionalItemInfoConfig = validateItemInfoConfigData(itemInfoConfigData);

        if (OptionalItemInfoConfig.isEmpty()) {
            return new ArrayList<>();
        }

        ItemInfoConfigData infoConfigData = OptionalItemInfoConfig.get();

        try {
            String itemId = infoConfigData.getItemData().getItemId();
            String nearbyStores = infoConfigData.getNearbyStores().stream().map(String::valueOf).collect(Collectors.joining(","));

            Optional<ItemAlgolia> optionalItemAlgolia = APIAlgolia.getItemAlgoliaRestAPI(itemId, nearbyStores);

            if (optionalItemAlgolia.isEmpty()) {
                log.warning("No item found for id: " + itemId);
                return new ArrayList<>();
            }

            String descForSearch = "";
            String category;

            ItemAlgolia itemAlgolia = optionalItemAlgolia.get();

            if (itemAlgolia.getDescription() != null && !itemAlgolia.getDescription().isEmpty()) {
                descForSearch = itemAlgolia.getDescription();
            } else if (itemAlgolia.getMediaDescription() != null && !itemAlgolia.getMediaDescription().isEmpty()) {
                descForSearch = itemAlgolia.getMediaDescription();
            }

            if (itemAlgolia.getDetailDescription() != null && !itemAlgolia.getDetailDescription().isEmpty()) {
                descForSearch = descForSearch + " " + itemAlgolia.getDetailDescription();
            }
            category = itemAlgolia.getCategorie();
            List<Item> substitutesItems = getItemsBySearchDesc(
                    descForSearch,
                    0,
                    nearbyStores,
                    Long.parseLong(itemId),
                    requestQuantity,
                    validateStock,
                    finalPossibleStores,
                    category
            );

            if (!substitutesItems.isEmpty()) {

                substitutesItems.sort((item1, item2) -> {
                    if (Objects.isNull(item1) || Objects.isNull(item2) ||
                            Objects.isNull(item1.getSupplier()) || Objects.isNull(item2.getSupplier())) {
                        return 0;
                    }

                    String itemAlgoliaSupplier = Objects.nonNull(itemAlgolia.getSupplier()) ? itemAlgolia.getSupplier() : "";

                    // Compare the suppliers
                    if (item1.getSupplier().equalsIgnoreCase(itemAlgoliaSupplier)) {
                        return -1;
                    } else if (item2.getSupplier().equalsIgnoreCase(itemAlgoliaSupplier)) {
                        return 1;
                    }

                    return 0;
                });

                // remove items with price does not match the parameter


                if (Stream.of(itemAlgolia, Objects.requireNonNull(itemAlgolia).getFullPrice()).allMatch(Objects::nonNull)
                        && itemAlgolia.getFullPrice() > 0) {

                    Double fullPrice = itemAlgolia.getFullPrice();

                    Double maxValue = fullPrice + (fullPrice * percentagePrice / 100);
                    Double minValue = fullPrice - (fullPrice * percentagePrice / 100);

                    substitutesItems.removeIf(item -> (item.getFullPrice() > maxValue || item.getFullPrice() < minValue));

                }
                return substitutesItems;
            }
        } catch (Exception e) {
            log.warning("Error retrieving substitutes from Algolia: " + e.getMessage());
            return new ArrayList<>();
        }
        return new ArrayList<>();
    }


    /**
     * Validates ItemInfoConfigData and returns an Optional of the data if valid.
     * @param itemInfoConfigData The item info configuration data to validate
     * @return An Optional of ItemInfoConfigData if valid, or an empty Optional if invalid
     */
    private static Optional<ItemInfoConfigData> validateItemInfoConfigData(ItemInfoConfigData itemInfoConfigData) {
        if (Objects.isNull(itemInfoConfigData) ||
                Objects.isNull(itemInfoConfigData.getItemData()) ||
                Objects.isNull(itemInfoConfigData.getNearbyStores()) ||
                itemInfoConfigData.getNearbyStores().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(itemInfoConfigData);
    }

    public List<Item> getSubstitutesFromAlgoliaItem(
            long itemId, int requestQuantity, int storeId, boolean validateStock, float percentagePrice, List<Integer> finalPossibleStores) {

        // obtener item algolia....
        int idStoreGroup = 0;
        try {
            idStoreGroup = APIAlgolia.getDefaultStoreIdByStoreId(storeId);

            String descForSearch = "";

            ItemAlgolia itemAlgolia = APIAlgolia.getItemAlgolia(itemId + "" + idStoreGroup);

            if (itemAlgolia != null) {
                if (itemAlgolia.getDescription() != null && !itemAlgolia.getDescription().isEmpty()) {
                    descForSearch = itemAlgolia.getDescription();
                } else if (itemAlgolia.getMediaDescription() != null && !itemAlgolia.getMediaDescription().isEmpty()) {
                    descForSearch = itemAlgolia.getMediaDescription();
                }

                // add detail description.
                if (itemAlgolia.getDetailDescription() != null && !itemAlgolia.getDetailDescription().isEmpty() && Objects.isNull(itemAlgolia.getUuidItem())) {
                    descForSearch = descForSearch + " " + itemAlgolia.getDetailDescription();
                }
            }

            String categorie = itemAlgolia.getCategorie();
            List<Item> substitutesItems = getItemsBySearchDesc(descForSearch, idStoreGroup,null, itemId, requestQuantity ,validateStock ,finalPossibleStores, categorie);



            if (Objects.nonNull(substitutesItems) && !substitutesItems.isEmpty()){
                // order by brand.

                // log.info("item class validos para sustituir size-> " + substitutesItems.size());

//          substitutesItems.forEach( it -> {
//              log.info("sub->" + it.toStringJson());
//          });
                substitutesItems.sort((item1, item2) -> {
                    if (Stream.of(item1,item2,item1.getSupplier(),item2.getSupplier()).allMatch(Objects::nonNull)){
                        if (item1.getSupplier().equalsIgnoreCase(itemAlgolia != null ? itemAlgolia.getSupplier() : "")){
                            return -1;
                        }else if (item2.getSupplier().equalsIgnoreCase(itemAlgolia != null ? itemAlgolia.getSupplier() : "")){
                            return 1;
                        }
                    }
                    return 0;
                } );

                // remove items with price does not match the parameter

                if (Stream.of(itemAlgolia, Objects.requireNonNull(itemAlgolia).getFullPrice()).allMatch(Objects::nonNull)
                        && itemAlgolia.getFullPrice() > 0){

                    Double fullPrice = itemAlgolia.getFullPrice();

                    Double maxValue = fullPrice + (fullPrice * percentagePrice / 100);
                    Double minValue = fullPrice - (fullPrice * percentagePrice / 100);

                    substitutesItems.removeIf(item -> (item.getFullPrice() > maxValue || item.getFullPrice() < minValue));

                }

                if (substitutesItems.size() > Constants.SUBSTITUTES_ITEMS_LIMIT) {
                    return substitutesItems.subList(0, Constants.SUBSTITUTES_ITEMS_LIMIT);
                } else {
                    return substitutesItems;
                }

            }

        } catch (AlgoliaException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    /**
     * Method to get items by search description
     * @param descForSearch description for search
     * @param idStoreGroup id store group for use old index in algolia
     * @param nearbyStores nearby stores separated by comma using for new index in algolia
     * @param originalItemId original item id for exclude in results
     * @param requestQuantity request quantity for validate stock
     * @param validateStock validate stock flag
     * @param finalPossibleStores final possible stores for validate stock
     * @param categorie search the same category of the original item
     * @return list of items substitutes
     */
    private List<Item> getItemsBySearchDesc(
            String descForSearch,
            int idStoreGroup,
            String nearbyStores,
            long originalItemId,
            int requestQuantity,
            boolean validateStock,
            List<Integer> finalPossibleStores,
            String categorie) {

        List<Item> itemsToSubstitute = new ArrayList<>();

        if (Stream.of(descForSearch).allMatch(Objects::nonNull) &&
                (idStoreGroup != 0 || (nearbyStores != null && !nearbyStores.isEmpty()))) {

            int limit = 10;
            try {
                HitsItemsAlgolia hits = APIAlgolia.getItemsBySearch(descForSearch, idStoreGroup, nearbyStores, 50, 0, categorie);

                if (Stream.of(
                                hits,
                                hits.getNbHits(),
                                hits.getNbHitsPerPage(),
                                hits.getNbPages(),
                                hits.getItemAlgoliaList())
                        .allMatch(Objects::nonNull)) {


                    List<ItemAlgolia> itemsResultSearch = new ArrayList<>(Objects.requireNonNull(hits.getItemAlgoliaList()));
                    log.info("Possible items to substitute size -> " + itemsResultSearch.size());
                    if (Objects.nonNull(hits.getNbHits()) && hits.getNbHits() <= 50) {
                        List<String> itemIdsRelated = CachedDataManager.getListOfKeys("" + originalItemId);
                        if (itemIdsRelated.isEmpty()) {
                            List<String> itemIds = new ArrayList<>();
                            int sizeSubstitute = itemsResultSearch.size();
                            int iterations = 0;
                            String lastWord = descForSearch;
                            try {
                                while (sizeSubstitute <= 50 && iterations <= 50) {
                                    descForSearch = FTDUtil.deleteLastWorld(descForSearch);

                                    if (lastWord.equals(descForSearch)) {
                                        break;
                                    }

                                    List<ItemAlgolia> nextSearch = APIAlgolia.getItemsBySearch(descForSearch, idStoreGroup, nearbyStores, 50, 0, categorie).getItemAlgoliaList();

                                    for (ItemAlgolia itAlgolia : Objects.requireNonNull(nextSearch)) {
                                        boolean itemExists = itemsResultSearch.stream().anyMatch(it -> it.getId().equals(itAlgolia.getId()));

                                        if (!itemExists) {
                                            itemsResultSearch.add(itAlgolia);
                                            itemIds.add(itAlgolia.getId());
                                        }
                                    }

                                    nextSearch.clear();
                                    itemsResultSearch.removeIf(hit -> Objects.nonNull(hit.getId()) && hit.getId().equals(String.valueOf(originalItemId)));

                                    sizeSubstitute = itemsResultSearch.size();
                                    iterations++;
                                    lastWord = descForSearch;
                                }
                            } catch (Exception e) {
                                log.warning("Error controlado en búsqueda de sustitutos. Mensaje: " + e.getMessage());
                            }
                            // use cache to save related items
                            itemIds = itemIds.stream().distinct().filter(id -> !Objects.equals(String.valueOf(originalItemId), id)).collect(Collectors.toList());
                            CachedDataManager.saveListOfKeys("" + originalItemId, itemIds);
                        } else {
                            //log.warning("Se encontraron items relacionados en cache, se procede a buscar en algolia.");
                            List<ItemQuery> itemsRelatedCached = itemIdsRelated.stream().map(id -> new ItemQuery(id + idStoreGroup)).collect(Collectors.toList());
                            itemsResultSearch.addAll(APIAlgolia.findItemByIdListV2(itemsRelatedCached));
                        }
                        if (!itemsResultSearch.isEmpty()) {
                            if (validateStock && !finalPossibleStores.isEmpty()) {
                                // delete items with stock not valid for order
                                ValidateStockRouteReq requestValidateStock = new ValidateStockRouteReq();
                                requestValidateStock.setStores(nearbyStores != null && !nearbyStores.isEmpty()
                                        ? Arrays.stream(nearbyStores.split(",")).map(Integer::parseInt).collect(Collectors.toList())
                                        : finalPossibleStores);

                                List<ValidateStockRouteReq.Item> itemsToValidateStock =
                                        itemsResultSearch.stream()
                                                .map(item -> {
                                                    ValidateStockRouteReq.Item itemAux = new ValidateStockRouteReq().new Item();
                                                    itemAux.setItemId(Long.valueOf(item.getId()));
                                                    itemAux.setRequestQuantity(requestQuantity);
                                                    return itemAux;
                                                })
                                                .collect(Collectors.toList());

                                // set items to validate stock
                                requestValidateStock.setItems(itemsToValidateStock);
                                ValidateStockRouteRes validateStockRes = ApiGatewayService.get().validateStock(requestValidateStock);

                                if (Objects.nonNull(validateStockRes) && Objects.nonNull(validateStockRes.getData())
                                        && Objects.nonNull(validateStockRes.getData().getResult())
                                        && !validateStockRes.getData().getResult().isEmpty()) {

                                    List<Long> idItemsValidList = new ArrayList<>();
                                    if (validateStockRes.getData().responseIsValid()) {
                                        validateStockRes.getData().getResult().forEach(results -> {
                                            if (results.getItems() != null && !results.getItems().isEmpty()) {
                                                results.getItems().forEach(itemAux -> {
                                                    if (itemAux != null && itemAux.isValid()) {
                                                        idItemsValidList.add((long) itemAux.getId());
                                                    }
                                                });
                                            }
                                        });
                                    }

                                    List<Long> idItems = idItemsValidList.stream().limit(limit).distinct().collect(Collectors.toList());

                                    if (!idItems.isEmpty()) {
                                        itemsToSubstitute = getItemsByIdsAndStore(idItems, idStoreGroup);
                                        itemsToSubstitute = itemsToSubstitute.stream().filter(Objects::nonNull).collect(Collectors.toList());
                                        if (!itemsToSubstitute.isEmpty() && !idItems.isEmpty()) {
                                            try {
                                                itemsToSubstitute.sort(Comparator.comparing(item -> idItems.indexOf(item.getId())));
                                            } catch (Exception e) {
                                                log.warning("Error sorting list @getItemsBySearchDesc " + e.getMessage());
                                            }
                                        }
                                    }
                                }
                            } else {
                                // service getItem:
                                List<Long> idItems = new ArrayList<>();
                                itemsResultSearch.forEach(itemAlgolia -> {
                                    if (itemAlgolia != null && itemAlgolia.getStock() != null && itemAlgolia.getId() != null) {
                                        if (itemAlgolia.getStock() > 0 && idItems.size() < limit) {
                                            idItems.add(Long.valueOf(itemAlgolia.getId()));
                                        }
                                    }
                                });

                                itemsToSubstitute.addAll(
                                        itemsResultSearch.stream()
                                                .filter(Objects::nonNull)
                                                .map(itemAlgolia -> APIAlgolia.getItemToItemAlgolia(new Item(), itemAlgolia))
                                                .collect(Collectors.toList())
                                );
                            }
                        }
                    }
                }

            } catch (AlgoliaException | IOException e) {
                log.severe("Error with Algolia get Substitutes @getItemsBySearchDesc " + e.getMessage());
            }
        }

        return itemsToSubstitute;
    }


    public DeliveryOrderItem buildDeliveryOrderItem(final DeliveryOrder deliveryOrder, final Key<Customer> customerKey, final UpdateDeliveryOrderRequest.Item itemRequest, final Boolean isShop, final int idStoreGroup) throws ConflictException {
//        log.info("method: buildDeliveryOrderItem");
//        log.info("idDeliveryOrder -> " + deliveryOrder.getIdDeliveryOrder());
        final Key<DeliveryOrder> deliveryOrderKey = Key.create(customerKey, DeliveryOrder.class, deliveryOrder.getIdDeliveryOrder());
//        log.info("deliveryOrderKey -> " + deliveryOrderKey.toString());
        // Agregar items a la orden
        Item item = new Item();
        try{
            item = setFindInformationToAlgoliaByIdItem(Integer.toString(itemRequest.getItemId()), idStoreGroup, null);
        }catch (Exception e){
            e.printStackTrace();
            log.warning("No se encuentra el item en algolia.");
        }

        if (Objects.isNull(item))
            throw new ConflictException(Constants.PRODUCT_NOT_EXISTS);

//        log.info("item build -> " + item.toStringJson());
        if (item.getTotalStock() < 1) {
            throw new ConflictException(Constants.PRODUCT_NOT_EXISTS);
        }
        //}


//        log.info(" item ->  " + item.getId() + ", " + idStoreGroup);
        //Validar y aplicar descuento si el item es SAS...
        if (item.getSubscribeAndSave() != null && item.getSubscribeAndSave()) {
            final Long customerID = SubscribeAndSaveUtil.getCustomerIdByDeliveryOrder(deliveryOrder);
            if (customerID != 0) {
                final int percentage = SubscribeAndSaveUtil.getSubscribeAndSaveDiscountCartByCustomer(customerID);
                if (percentage > 0) {
                    final Double razon = SubscribeAndSaveUtil.toDecimalPercentage(percentage);
                    final Double offerPrice = item.getFullPrice() * razon;
                    item.setOfferPrice(offerPrice);
                }
            }
        }

        DeliveryOrderItem deliveryOrderItem1 = ofy().load().type(DeliveryOrderItem.class).filter("idItem", Key.create(Item.class, item.getItemId())).filter("changeQuantity", true).ancestor(Ref.create(deliveryOrderKey)).first().now();
        final List<DeliveryOrderItem> deliverOrderExtra = ofy().load().type(DeliveryOrderItem.class).filter("idItem", Key.create(Item.class, item.getItemId())).filter("changeQuantity", false).ancestor(Ref.create(deliveryOrderKey)).list();
        ofy().delete().entities(deliverOrderExtra).now();

        if (deliveryOrderItem1 == null)
            deliveryOrderItem1 = new DeliveryOrderItem();
        deliveryOrderItem1 = this.deliveryOrderItemReturn(deliveryOrderItem1, item);
        final DeliveryOrderItem finalDeliveryOrderItem = deliveryOrderItem1;
        final Item finalItem = item;
        final DeliveryOrderItem finalDeliveryOrderItem1 = deliveryOrderItem1;
        DeliveryOrderItem deliveryOrderItem = finalDeliveryOrderItem;
        if (finalDeliveryOrderItem.getIdDeliveryOrderItem() == null) {
            //log.warning("strange");
            deliveryOrderItem.setIdDeliveryOrderItem(UUID.randomUUID().toString());
            deliveryOrderItem.setIdDeliveryOrder(Ref.create(deliveryOrderKey));
            deliveryOrderItem.setIdItem(Key.create(Item.class, finalItem.getItemId()));
            deliveryOrderItem.setQuantitySold(itemRequest.getQuantityRequested());
            deliveryOrderItem.setCreateDate(new Date());
            deliveryOrderItem.setFullPrice(finalItem.getFullPrice());
            deliveryOrderItem.setOfferPrice(finalItem.getOfferPrice());
            deliveryOrderItem.setTotalStock(finalItem.getTotalStock());
            deliveryOrderItem.setChangeQuantity(true);
            // Campos adicionales proveedores externos
            deliveryOrderItem.setDeliveryPrice(finalItem.getDeliveryPrice());
            // Agrega el origin de adición al carrito
            deliveryOrderItem.setOrigin(itemRequest.getOrigin());
            deliveryOrderItem.setObservations(itemRequest.getObservations());
            deliveryOrderItem.setHandleQuantity(finalItem.getHandleQuantity());
            deliveryOrderItem.setOnlyOnline(finalItem.isOnlyOnline());
            Key<DeliveryOrderItem> deliveryOrderItemKey = Key.create(deliveryOrderKey, DeliveryOrderItem.class, deliveryOrderItem.getIdDeliveryOrderItem());
            deliveryOrderItem.setIdDeliveryOrderItemWebSafe(deliveryOrderItemKey.toWebSafeString());
        } else {
            //log.warning("bool " + isShop);
            if (isShop != null && isShop) {
                //log.warning("InShop" + itemRequest.getQuantityRequested());
                deliveryOrderItem.setQuantitySold(itemRequest.getQuantityRequested());
                deliveryOrderItem.setChangeQuantity(true);
            } else {
                deliveryOrderItem.setChangeQuantity(true);
                int lastQuantity = 0;
                for (DeliveryOrderItem orderItem : deliverOrderExtra) {
                    lastQuantity += orderItem.getQuantitySold();
                }
                lastQuantity += finalDeliveryOrderItem1.getQuantitySold();
                deliveryOrderItem.setQuantitySold(itemRequest.getQuantityRequested() + lastQuantity);
                //log.warning("NoShop" + (itemRequest.getQuantityRequested() + lastQuantity));
            }
            if (Objects.nonNull(itemRequest.getObservations()) && !itemRequest.getObservations().isEmpty()) {
                deliveryOrderItem.setObservations(itemRequest.getObservations());
            }
        }
        //log.warning("itemRequest.isSubstitute() ---> " + itemRequest.isSubstitute());
        if (Objects.nonNull(itemRequest.isSubstitute())){
            deliveryOrderItem.setSubstitute(itemRequest.isSubstitute());
        }

        return deliveryOrderItem;
    }

    private DeliveryOrderItem deliveryOrderItemReturn(DeliveryOrderItem deliveryOrderItem, Item item) {
        deliveryOrderItem.setAnywaySelling(item.isAnywaySelling());
        deliveryOrderItem.setBarcode(item.getBarcode());
        deliveryOrderItem.setBrand(item.getBrand());
        deliveryOrderItem.setId(item.getId());
        deliveryOrderItem.setGeneric(item.isGeneric());
        deliveryOrderItem.setGrayDescription(item.getGrayDescription());
        deliveryOrderItem.setMediaDescription(item.getMediaDescription());
        deliveryOrderItem.setHighlight(item.isHighlight());
        deliveryOrderItem.setMediaImageUrl(item.getMediaImageUrl());
        deliveryOrderItem.setOutstanding(item.isOutstanding());
        deliveryOrderItem.setTotalStock(item.getTotalStock());
        deliveryOrderItem.setOfferText(item.getOfferText());
        deliveryOrderItem.setOfferDescription(item.getOfferDescription());
        deliveryOrderItem.setIdStoreGroup(item.getIdStoreGroup());

        return deliveryOrderItem;
    }

    public Item setFindInformationToAlgoliaByIdItemisScanAndGo(String idItem, long idStoreGroup){
        //log.info("method setFindInformationToAlgolia -> " + idItem+" storeid- > " +  idStoreGroup);
        Item item = new Item();
        item.setId(Long.parseLong(idItem));
        item.setItemId(idItem);
        return setItemToItemAlgoliaScanAndGo(item, idStoreGroup);
    }

    private Item setItemToItemAlgoliaScanAndGo(Item item, long idStoreGroup){
        try{
            //log.debug("method setItemToItemAlgolia -> " + item.getItemId() +" storeid- > " +  idStoreGroup);
            ItemAlgolia itemAlgolia = APIAlgolia.getItemAlgoliaScanAndGo(item.getItemId()+idStoreGroup);
            if(Objects.nonNull(itemAlgolia)) {
                item = APIAlgolia.getItemToItemAlgolia(item, itemAlgolia);
            }
            return item;
        }catch (Exception e) {
            log.info("ERROR-> " + e);
            return null;
        }
    }


    public List<Substitutes> getSubstitutesFromRelatedProducts(long itemId,int idStoreGroup, boolean validateStock, List<Integer> finalPossibleStores, int requestQuantity, int limit)  {
        List<ItemAlgolia> relatedProducts = APIAlgolia.getRelatedProductsSubstitutes(itemId, idStoreGroup);
        if(!relatedProducts.isEmpty()) {
            try {
                return itemTosubstitutes(getItemCheckoutSubstitute(relatedProducts, validateStock, finalPossibleStores, requestQuantity, idStoreGroup, limit));
            } catch (Exception e) {
                log.info("Ocurrio un error consultando los productos relacionados");
            }

        }
        return new ArrayList<>();
    }

    public List<Item> getRelatedItemsAlgoliaRecommend(Item item, int idStoreGroup, List<Long> finalPossibleStores, int limit)  {
        List<ItemAlgolia> relatedProducts = APIAlgolia.getRelatedItems(item);
        if(!relatedProducts.isEmpty()) {
            try {
                return getItemCheckoutSubstituteAlgoliaRecommend(relatedProducts, finalPossibleStores, idStoreGroup, limit);
            } catch (Exception e) {
                log.log(Level.WARNING, "getRelatedItemsAlgoliaRecommend() Ocurrio un error consultando los productos relacionados", e);
            }

        }
        return new ArrayList<>();
    }

    public List<Item> getItemCheckoutSubstitute(List<ItemAlgolia> itemsResultSearch, boolean validateStock, List<Integer> finalPossibleStores, int requestQuantity, int storeId, int limit) throws IOException, AlgoliaException {
        List<Item> itemsToSustitute = new ArrayList<>();
        int idStoreGroup = 0;
        idStoreGroup = APIAlgolia.getDefaultStoreIdByStoreId(storeId);
        if (itemsResultSearch.size() > 0) {

            if (validateStock && !finalPossibleStores.isEmpty()) {
                // delete items with stock not valid for order

                ValidateStockRouteReq requestValidateStock = new ValidateStockRouteReq();
                requestValidateStock.setStores(finalPossibleStores);

                //log.info("search with stock -> " + requestQuantity);
                List<ValidateStockRouteReq.Item> itemsToValidateStock =
                        itemsResultSearch.stream()
                                .map(
                                        item -> {
                                            ValidateStockRouteReq.Item itemAux =
                                                    new ValidateStockRouteReq().new Item();
                                            itemAux.setItemId(Long.valueOf(item.getId()));
                                            itemAux.setRequestQuantity(requestQuantity);
                                            return itemAux;
                                        })
                                .collect(Collectors.toList());

                // set items to validate stock
                requestValidateStock.setItems(itemsToValidateStock);
                ValidateStockRouteRes validateStockRes = ApiGatewayService.get().validateStock(requestValidateStock);
//                log.info("Tamaño " + validateStockRes.getData().getResult().size());
                if (Objects.nonNull(validateStockRes) && Objects.nonNull(validateStockRes.getData())
                        && Objects.nonNull(validateStockRes.getData().getResult())
                        && !validateStockRes.getData().getResult().isEmpty()) {


                    List<Long> idItemsValidList = new ArrayList<>();
                    if (validateStockRes.getData().responseIsValid()){
                        validateStockRes.getData().getResult().forEach( results -> {
                            if (results.getItems() != null && !results.getItems().isEmpty()){
                                results.getItems().forEach( itemAux -> {
                                    if (itemAux.isValid()){
                                        idItemsValidList.add((long) itemAux.getId());
                                    }
                                });
                            }
                        });
                    }

                    List<Long> idItems = idItemsValidList.stream().limit(limit).distinct().collect(Collectors.toList());

                    // log.info("idItems validos para sustituir size-> " + idItems.size());


                    itemsToSustitute = getItemsByIdsAndStore(idItems, idStoreGroup);
                    if (itemsToSustitute != null && !itemsToSustitute.isEmpty() && !idItems.isEmpty()) {
                        itemsToSustitute.sort(Comparator.comparing(item -> idItems.indexOf(item.getId())));
                    }
                }
            }else {
                // service getITem:
                List<Long> idItems = new ArrayList<>();
                itemsResultSearch.forEach( itemAlgolia -> {
                    if(itemAlgolia != null && itemAlgolia.getStock()!=null && itemAlgolia.getId()!=null && idItems!=null){
                        if (itemAlgolia.getStock() > 0 && idItems.size() < limit){
                            idItems.add(Long.valueOf(itemAlgolia.getId()));
                        }
                    }
                });

                itemsToSustitute = getItemsByIdsAndStore(idItems,idStoreGroup);
            }
        }
        log.info(new Gson().toJson(itemsToSustitute));
        return itemsToSustitute;
    }


    public List<Item> getItemCheckoutSubstituteAlgoliaRecommend(List<ItemAlgolia> itemsResultSearch, List<Long> finalPossibleStores, int storeId, int limit) {
        try {
            int idStoreGroup = APIAlgolia.getDefaultStoreIdByStoreId(storeId);

            if (itemsResultSearch.isEmpty()) {
                return new ArrayList<>();
            }

            if (finalPossibleStores == null || finalPossibleStores.isEmpty()) {
                return new ArrayList<>();
            }

            List<Long> idItems = itemsResultSearch.stream()
                    .filter(itemAlgolia -> Objects.nonNull(itemAlgolia) &&
                            Objects.nonNull(itemAlgolia.getId()) &&
                            Objects.nonNull(itemAlgolia.getStores_with_stock()))
                    .filter(itemAlgolia -> finalPossibleStores.stream()
                            .anyMatch(store -> itemAlgolia.getStores_with_stock().contains(store)))
                    .limit(limit)
                    .map(itemAlgolia -> Long.valueOf(itemAlgolia.getId()))
                    .collect(Collectors.toList());

            if (idItems.isEmpty()) {
                return new ArrayList<>();
            }
            List<Item> itemsToSubstitute = getItemsByIdsAndStore(idItems, idStoreGroup);

            if (itemsToSubstitute != null && !itemsToSubstitute.isEmpty()) {

                itemsToSubstitute.sort(Comparator.comparing(item -> idItems.indexOf(item.getId())));

                return itemsToSubstitute.stream()
                        .peek(item -> {
                            if (Objects.nonNull(item) && item.getTotalStock() == 0) {
                                item.setTotalStock(MINIMUM_STOCK);
                                item.setWithout_stock(false);
                            }
                        })
                        .collect(Collectors.toList());
            }

            return new ArrayList<>();
        } catch (Exception e) {
            log.severe("Error in getItemCheckoutSubstituteAlgoliaRecommend: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    public Item validateItemAlgolia(Item item, int idStoreGroup, Integer id, boolean isScanAndGo, Integer quantity) throws ConflictException {
        item =  (isScanAndGo && item == null) ? setFindInformationToAlgoliaByIdItemisScanAndGo(Integer.toString(id), 26) : item;
        log.info((isScanAndGo && item != null) ? "sag nuevo indice-->" + item :"not sag");
        if (item == null) {
            log.warning("ITEM IS NULL");
            throw new ConflictException(Constants.PRODUCT_NOT_EXISTS);
        }
        AlgoliaItem algoliaItem = restrictItemsAlgolia(id.longValue(), idStoreGroup, item.getGlobalStock());
        if (Objects.nonNull(algoliaItem) && Objects.nonNull(algoliaItem.getTotalStock()) && algoliaItem.getTotalStock() > 0) {
            item.setTotalStock(algoliaItem.getTotalStock());
            quantity = quantity > algoliaItem.getTotalStock() ? algoliaItem.getTotalStock() : quantity;
            log.info("---> Item restriction set TotalStock id:" + id + "stock:" + algoliaItem.getTotalStock() + "quantity: " + quantity);
        }
        //log.warning("--> " + id + " --> Stock: " + item.getTotalStock() + " --> Quantity: " + quantity);
        if (!isScanAndGo && (item == null || item.getTotalStock() < 1)) {
            throw new ConflictException(Constants.PRODUCT_NOT_EXISTS);
        }
        if (isScanAndGo) {
            if (Objects.isNull(item)) {
                throw new ConflictException(Constants.PRODUCT_NOT_EXISTS);
            } else {
                item.setTotalStock(item.getTotalStock() < 1 ? 1000: item.getTotalStock());
            }
        }

        return item;
    }

    private AlgoliaItem restrictItemsAlgolia(Long itemId, Integer idStoreGroup, Integer actualTotalStock) {
        log.info("getRestrictionItems itemId:" + itemId + " idStoreGroup: " + idStoreGroup);
        RestrictionItemConfig restrictionItemConfig = APIAlgolia.getRestrictionQuantityItems();

        if (idStoreGroup > 0 && Objects.nonNull(restrictionItemConfig.getRestrictionItems()) && !restrictionItemConfig.getRestrictionItems().isEmpty()) {
            Optional<RestrictionItem> restrictionItemResult = restrictionItemConfig.getRestrictionItems().stream()
                    .filter(restrictionItem -> Objects.nonNull(restrictionItem) &&
                            Objects.nonNull(restrictionItem.getItemId()) &&
                            Objects.nonNull(restrictionItem.getRestrictionQuantity()) &&
                            (restrictionItem.getItemId() > 0L) &&
                            (restrictionItem.getRestrictionQuantity() > 0L) &&
                            restrictionItem.getItemId().longValue() == itemId).findFirst();

            if (restrictionItemResult.isPresent()) {
                log.info("restrictionItemResult -> Present 2.0 actualTotalStock " + actualTotalStock);
                if (actualTotalStock != restrictionItemResult.get().getRestrictionQuantity().intValue()) {
                    List<AlgoliaItem> algoliaItemList = APIAlgolia.changeListTotalStockItems(idStoreGroup, restrictionItemResult.get().getItemId(), restrictionItemResult.get().getRestrictionQuantity());
                    log.info("algoliaItemList -> algoliaItemList " + algoliaItemList);
                    return Objects.nonNull(algoliaItemList) && !algoliaItemList.isEmpty() ?
                            algoliaItemList.stream()
                                    .filter(item -> Objects.nonNull(item) && (Long.compare(itemId, Long.parseLong(item.getId())) == 0) &&
                                            Integer.compare(item.getIdStoreGroup(), idStoreGroup) == 0)
                                    .findFirst().orElse(null) :
                            null;
                } else {
                    log.info("restrictionItemResult -> Same Stock " + itemId.toString() + " -- " + idStoreGroup + " -- " + actualTotalStock);
                    return new AlgoliaItem(itemId.toString(), idStoreGroup, actualTotalStock);
                }
            }
        }
        return null;
    }

    /**
     * Validar si el item buscado esta marcado con el atributo starProduct en "1", si ese es el caso eliminar el componente
     * BAZAARVOICE
     *
     * @param idItem long que representa el id del item
     * @param idStoreGroup tienda sobre la cual se consulta
     * @param response Respuesta del servicio al cual se desea eliminar el componente
     * @return Respuesta sin el componente BAZAARVOICE en caso de que cumpla con las condiciones, en otro caso se deja igual
     * @throws AlgoliaException En caso de que ocurra un error buscando el item en algolia
     */
    public DynamicResponse avoidBazaarVoice(long idItem, long idStoreGroup, DynamicResponse response) throws AlgoliaException {
      long idStoreGroupDefault = APIAlgolia.getDefaultStoreIdByStoreId((int) idStoreGroup);
      ItemAlgolia item = APIAlgolia.getItemAlgoliaById(idItem, idStoreGroupDefault);
      if (Objects.nonNull(item) && Objects.equals(item.getStarProduct(), "1") && Objects.nonNull(response) && Objects.nonNull(response.getItemSection())) {
         response.setItemSection(
                 response.getItemSection()
                         .stream()
                         .filter(component -> !Objects.equals(component.getComponentType(), ComponentTypeEnum.BAZAARVOICE))
                         .collect(Collectors.toList())
         );
      }
      return response;
    }
}
