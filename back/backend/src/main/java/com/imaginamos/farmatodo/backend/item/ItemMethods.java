package com.imaginamos.farmatodo.backend.item;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Transaction;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.imaginamos.farmatodo.model.item.*;
import com.imaginamos.farmatodo.model.product.Item;
import com.imaginamos.farmatodo.model.product.ItemGroup;
import com.imaginamos.farmatodo.model.product.StoreInformation;
import com.imaginamos.farmatodo.model.util.ItemCreateRequest;
import com.imaginamos.farmatodo.model.util.ItemCreateResponse;
import com.imaginamos.farmatodo.model.util.ItemSubscribeAndSaveResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static com.imaginamos.farmatodo.backend.OfyService.ofy;

public class ItemMethods {

    private static final Logger LOG = Logger.getLogger(ItemMethods.class.getName());

    public boolean updateItem(ItemRequest itemRequest) {

        Item itemSaved;

        try {
            itemSaved = ofy().load().type(Item.class).filter("id", itemRequest.getItem()).first().now();

            if (itemRequest.getBarcode() != null)
                itemSaved.setBarcode(itemRequest.getBarcode());

            if (itemRequest.getBrand() != null)
                itemSaved.setBrand(itemRequest.getBrand());

            if (itemRequest.getGrayDescription() != null)
                itemSaved.setGrayDescription(itemRequest.getGrayDescription());

            if (itemRequest.getMediaDescription() != null)
                itemSaved.setMediaDescription(itemRequest.getMediaDescription());

            itemSaved.setHighlight(itemRequest.isHighlight());

            if (itemRequest.getMediaImageUrl() != null)
                itemSaved.setMediaImageUrl(itemRequest.getMediaImageUrl());

            if (itemRequest.getRequirePrescription() != null)
                itemSaved.setRequirePrescription(itemRequest.getRequirePrescription());

            if (itemRequest.getStatus() != null)
                itemSaved.setStatus(itemRequest.getStatus());

            ofy().save().entity(itemSaved).now();

            return true;

        } catch (Exception e) {
            LOG.warning("error ItemMethods -> " + e.getLocalizedMessage());
            return false;
        }

    }


    public boolean updateItemsPrices(final List<ItemToUpdatePrice> listItemsToUpdate) {

        if (listItemsToUpdate.isEmpty()) {
            return false;
        } else {

            try {

                Item itemSaved;

                for (ItemToUpdatePrice item : listItemsToUpdate) {

                    itemSaved = ofy().load().type(Item.class).filter("id", item.getItem()).first().now();

                    for (StoreItemPrice storeItem : item.getStorePriceList()) {
                        List<StoreInformation> storeInformationList = itemSaved.getStoreInformation();

                        StoreInformation storeInformation = storeInformationList.stream().filter(p -> p.getStoreGroupId() == storeItem.getStore()).findFirst().get();
                        if (storeInformation != null && storeItem.getPrice() > 0) {
                            storeInformation.setFullPrice(storeItem.getPrice());
                            itemSaved.setStoreInformation(storeInformationList);
                            ofy().save().entity(itemSaved).now();
                        }
                    }


                }

            } catch (Exception e) {
                LOG.warning("Error actualizar precios " + e.getMessage());
            }

        }

        return true;
    }

    /**
     * Convert item request in item entity for Datastore.
     */
    public static com.imaginamos.farmatodo.model.product.Item itemToEntity(ItemCreateRequest dataObject) {
//        LOG.info("INI-itemToEntity()");

        if (dataObject == null) {
            return null;
        }
        if (dataObject.getItem() == null) {
            return null;
        }
        try {
            com.imaginamos.farmatodo.model.product.Item itemEntity = new Item();
            com.imaginamos.farmatodo.model.item.Item itemParam = dataObject.getItem();

//            LOG.info("Item.toSring() => "+itemParam.toString());

            if(itemParam!=null) {

                itemEntity.setItemGroupRef(Ref.create(Key.create(ItemGroup.class, Integer.toString(1))));

//                LOG.info("A");
                itemEntity.setItemId(Long.toString(itemParam.getItemId()));
                itemEntity.setId(itemParam.getItemId());
                itemEntity.setAnywaySelling(itemParam.getAnywaySelling());
                itemEntity.setBarcode(itemParam.getBarcode());
                itemEntity.setBrand(itemParam.getBrand());
//                LOG.info("B");
                itemEntity.setFullPrice(itemParam.getFullPrice());
                itemEntity.setGrayDescription(itemParam.getGrayDesc());
                itemEntity.setHighlight(itemParam.getHighlight());
                itemEntity.setIdStoreGroup(itemParam.getIdStoreGroup());
                itemEntity.setCoupon(itemParam.getCoupon());
//                LOG.info("C");
                itemEntity.setGeneric(itemParam.getGeneric()==null?false:itemParam.getGeneric());
//                LOG.info("C1");
                itemEntity.setLargeDescription(itemParam.getLargeDesc());
//                LOG.info("C2");
                itemEntity.setMediaDescription(itemParam.getMediaDesc());
//                LOG.info("C3");
                itemEntity.setMediaImageUrl(itemParam.getUrlImage());
//                LOG.info("C4");
                itemEntity.setOfferDescription(itemParam.getOfferDesc());
//                LOG.info("D");
                itemEntity.setOfferPrice(itemParam.getOfferPrice());
                itemEntity.setOfferText(itemParam.getOfferText());
                itemEntity.setOutstanding(itemParam.getOutstanding());
                itemEntity.setRequirePrescription(String.valueOf(itemParam.getRequirePresc()));
//                LOG.info("E");
                itemEntity.setSales(itemParam.getSales());
                itemEntity.setSpaces(itemParam.getSpaces());
                itemEntity.setStatus(itemParam.getStatus());
                itemEntity.setTaxRate(itemParam.getTaxRate());
//                LOG.info("F");
                itemEntity.setToDelete(itemParam.getToDelete());
                itemEntity.setToIndexInAlgolia(itemParam.getToIndexAlg());
                itemEntity.setTotalStock(itemParam.getTotalStock());
//                LOG.info("G");
                itemEntity.setFilterList(dataObject.getFilterList());
//                LOG.info("H");
                itemEntity.setSubCategories(dataObject.getSubCategories());
//                LOG.info("I");
                List<StoreInformation> storeInfo = storeInformationToEntity(dataObject.getStoreInformation());
//                LOG.info("J");
                itemEntity.setStoreInformation(storeInfo);
//                LOG.info("END-itemToEntity()");
                return itemEntity;
            }else{
                return null;
            }
        }catch (Exception e){
            LOG.warning("Error casting item to EntityItem. Message => "+e.getMessage());
            return null;
        }
    }


    /**
     * Convert store information param in StoreInformation entity.
     */
    private static List<StoreInformation> storeInformationToEntity(List<com.imaginamos.farmatodo.model.item.StoreInformation> storeInfo) {
//        LOG.info("INI-storeInformationToEntity()");
//        LOG.info("storeInfo.lenght => ["+storeInfo.size()+"]");

        List<StoreInformation> storeInformations = new ArrayList<>();

        for (com.imaginamos.farmatodo.model.item.StoreInformation si : storeInfo) {
//            LOG.info("Inicia FOR");
            StoreInformation storeInformation = new StoreInformation();

            storeInformation.setStoreGroupId(si.getStoreId());
//            LOG.info("a");
            storeInformation.setStock(si.getStock());
//            LOG.info("b");
            storeInformation.setFullPrice(si.getFullPrice());
//            LOG.info("c");
            storeInformation.setOfferText(si.getOfferText());
//            LOG.info("d");
            storeInformation.setOfferPrice(si.getOfferPrice());
//            LOG.info("e");
            storeInformation.setOfferDescription(si.getOfferDesc());
//            LOG.info("f");

            storeInformations.add(storeInformation);
        }

//        LOG.info("END-storeInformationToEntity()");

        return storeInformations;
    }

    /**
     * Save item entity in Datastore.
     */
    public static Key<Item> saveNewItem(com.imaginamos.farmatodo.model.product.Item item) throws Exception {
//        LOG.info("INI-saveNewItem()");

        if (item == null) {
            return null;
        }

        Key<ItemGroup> itemGroupKey = Key.create(ItemGroup.class, "1");
        Item itemSaved = ofy().load().key(Key.create(itemGroupKey, Item.class, Long.toString(item.getId()))).now();

        if (itemSaved != null) {
            throw new Exception("Item with id : " + Long.toString(item.getId()) + " already exists. It is no possible to create an item with the same id.");
        }

//        LOG.info("END-saveNewItem()");

        return ofy().save().entity(item).now();
    }

    public static ItemCreateResponse createNewItem(ItemCreateRequest request){
//        LOG.info("INI-createNewItem()");
        try {

            List<String> messages = new ArrayList<>();

//            LOG.info("Getting item object...");
            com.imaginamos.farmatodo.model.item.Item item = request.getItem();
//            LOG.info("Getting storeInfo object...");
            List<com.imaginamos.farmatodo.model.item.StoreInformation> storeInfo = request.getStoreInformation();
//            LOG.info("Getting filterList object...");
            List<Integer> filterList = request.getFilterList();
//            LOG.info("Getting subcategories object...");
            List<Long> subCategories = request.getSubCategories();

//            LOG.info("IF (item!= null && storeInfo != null) : [" + (item != null) + "] && [" + (storeInfo != null)+"]");
            if (item != null && storeInfo != null) {

                com.imaginamos.farmatodo.model.product.Item itemEntity = ItemMethods.itemToEntity(request);

//                LOG.info("IF (itemEntity!=null): [" + (itemEntity != null) + "]");
                if (itemEntity != null) {
                    Key<Item> itemSavedKey = null;
                    try {
                        itemSavedKey = ItemMethods.saveNewItem(itemEntity);
//                        LOG.info("itemSavedKey => "+itemSavedKey);
                    } catch (Exception e) {
                        LOG.warning(e.getMessage());
                    }

//                    LOG.info("IF (itemSavedKey!=null) : [" + (itemSavedKey != null) + "]");
                    if (itemSavedKey != null) {
                        messages.add(itemSavedKey.toString());
                    }
                } else {
                    messages.add("It nos possible convert data to ItemEntity");
                }

            } else {
                messages.add("1 item null or with empty StoreInformation");
            }

//            LOG.info("Building response...");

            ItemCreateResponse response = new ItemCreateResponse();
            response.setStatus("OK");
            response.setStatus_code(200);
            response.setMessage("Request managed successfully");
            response.setMessages(messages);

//            LOG.info("END-createNewItem()");

            return response;
        }catch(Exception e) {
            ItemCreateResponse response = new ItemCreateResponse();
            response.setStatus("Internal Server Error.");
            response.setStatus_code(503);
            response.setMessage("Unexpected error. Mensaje: " + e.getMessage());
//            LOG.info("END-createNewItem()");
            return response;
        }

    }


    public UpdateItemResponse updateFullItem(UpdateItemRequest request) {

        UpdateItemResponse response = null;

        try {

            Item itemSaved = null;
            itemSaved = ofy().load().type(Item.class).filter("id", request.getId()).first().now();

            if(itemSaved!=null) {
                itemSaved.setBarcode(request.getBarcode());
                itemSaved.setBrand(request.getBrand());
                itemSaved.setGrayDescription(request.getGray_description());
                itemSaved.setMediaDescription(request.getMedia_description());
                itemSaved.setHighlight(request.getHighlight());
                itemSaved.setMediaImageUrl(request.getImage_url());
                itemSaved.setRequirePrescription(request.getRequire_prescription());
                itemSaved.setStatus(request.getStatus());

                if(request.getLarge_description()!=null){
                    itemSaved.setLargeDescription(request.getLarge_description());
                }

                itemSaved.setSubCategories(Arrays.asList(request.getCategories()));
                itemSaved.setFilterList(Arrays.asList(request.getFilters()));

                DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
                Transaction txn = datastore.beginTransaction();
                try {
//                    LOG.info("Transaction STARED");
                    ofy().save().entity(itemSaved).now();
//                    LOG.info("Entity SAVED");
                    txn.commit();
//                    LOG.info("Transaction COMMITED");
                } finally {
                    if (txn.isActive()) {
                        txn.rollback();
                    }
                }

                response = new UpdateItemResponse();
                response.setStatus("OK");
                response.setStatus_code(200);
                response.setMessage("Item updated successfully...");
                return response;
            }

            response = new UpdateItemResponse();
            response.setMessage("Item not found");
            response.setStatus_code(404);
            response.setStatus("Not Found");
            return response;

        } catch (Exception e) {
            LOG.info("Error trying to update item. Message : "+e.getMessage());
            response = new UpdateItemResponse();
            response.setMessage("Internal Server Error when the process tried to update item.");
            response.setStatus_code(500);
            response.setStatus("Internal Server Error");
            return response;
        }

    }

    public ItemSubscribeAndSaveResponse updateSubscribeAndSaveItem(ItemSubscribeAndSaveRequest request) {

        ItemSubscribeAndSaveResponse response = null;

        try {

            Item itemSaved = null;
            itemSaved = ofy().load().type(Item.class).filter("id", request.getId()).first().now();

            if(itemSaved!=null) {
                itemSaved.setSubscribeAndSave(request.getSubscribeAndSave());
                itemSaved.setStandardDuration(request.getStandardDuration());
                DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
                Transaction txn = datastore.beginTransaction();
                try {
//                    LOG.info("Transaction STARED");
                    ofy().save().entity(itemSaved).now();
//                    LOG.info("Entity SAVED");
                    txn.commit();
//                    LOG.info("Transaction COMMITED");
                } finally {
                    if (txn.isActive()) {
                        txn.rollback();
                    }
                }

                response = new ItemSubscribeAndSaveResponse();
                response.setStatus("OK");
                response.setStatus_code(200);
                response.setMessage("Item subscribe and save updated successfully...");
                return response;
            }

            response = new ItemSubscribeAndSaveResponse();
            response.setMessage("Item not found");
            response.setStatus_code(404);
            response.setStatus("Not Found");
            return response;

        } catch (Exception e) {
            LOG.info("Error trying to update item subscribe and save. Message : "+e.getMessage());
            response = new ItemSubscribeAndSaveResponse();
            response.setMessage("Internal Server Error when the process tried to update subscribe and save item.");
            response.setStatus_code(500);
            response.setStatus("Internal Server Error");
            return response;
        }

    }
}