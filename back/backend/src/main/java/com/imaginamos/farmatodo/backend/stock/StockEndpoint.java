package com.imaginamos.farmatodo.backend.stock;


import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.ConflictException;
import com.googlecode.objectify.Key;
import com.imaginamos.farmatodo.model.product.Item;
import com.imaginamos.farmatodo.model.product.ItemGroup;
import com.imaginamos.farmatodo.model.product.StoreInformation;
import com.imaginamos.farmatodo.model.stock.*;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.model.util.StoreStockAnswer;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static com.imaginamos.farmatodo.backend.OfyService.ofy;

@Api(name = "stockEndpoint",
        version = "v1",
        apiKeyRequired = AnnotationBoolean.TRUE,
        scopes = {Constants.EMAIL_SCOPE},
        clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID_DEVELOPER, com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID},
        audiences = {Constants.ANDROID_AUDIENCE},
        namespace = @ApiNamespace(ownerDomain = Constants.OWNER_DOMAIN, ownerName = Constants.OWNER_NAME, packagePath = ""),
        description = "Update stock")
public class StockEndpoint {

    private StockMethods stockMethods;

    public StockEndpoint() {
        stockMethods = new StockMethods();
    }

    private static final Logger LOGGER = Logger.getLogger(StockEndpoint.class.getName());

    @ApiMethod(name = "stockUploadOffer", path = "/stockEndpoint/stockUploadOffer", httpMethod = ApiMethod.HttpMethod.POST)
    public StoreStockAnswer stockUploadOffer(final StoreStock storeStock) throws ConflictException {
        StoreStockAnswer answer = new StoreStockAnswer();
        answer.setConfirmation(false);
        answer.setStoreInformation(null);
        Item itemSaved;

        if (storeStock == null){
            throw new ConflictException(Constants.PRODUCT_NOT_EXISTS);
        }

        try {
            itemSaved = ofy().load().type(Item.class).filter("id",storeStock.getItem()).first().now();
            StockMethods stockMethods = new StockMethods();

            List<StoreInformation> listUpdated = stockMethods.updateStoreInformation(itemSaved.getStoreInformation(),storeStock);

            itemSaved.getStoreInformation().clear();

            itemSaved.setStoreInformation(listUpdated);

            if (!itemSaved.getStoreInformation().isEmpty()){
                ofy().save().entity(itemSaved).now();
                StoreInformation storeInformation = listUpdated.stream().filter(p -> p.getStoreGroupId() == storeStock.getStore()).findFirst().get();
                answer.setStoreInformation(storeInformation);
                answer.setConfirmation(true);
                answer.setMessage(Constants.TRUE_CONFIRMATION + " ID -> " + storeStock.getItem());
            }

        } catch (Exception e){
            throw new ConflictException(Constants.PRODUCT_NOT_EXISTS);
        }

        return answer;
    }



    @ApiMethod(name = "deleteOffer", path = "/stockEndpoint/deleteOffer", httpMethod = ApiMethod.HttpMethod.POST)
    public StoreStockAnswer deleteOffer(final DeleteOfferItem deleteOfferItem) throws ConflictException {
        StoreStockAnswer answer = new StoreStockAnswer();
        answer.setConfirmation(false);
        answer.setStoreInformation(null);

        if (deleteOfferItem == null)
            throw new ConflictException(Constants.PRODUCT_NOT_EXISTS);

        Item itemSaved;


        try {

            itemSaved = ofy().load().type(Item.class).filter("id",deleteOfferItem.getItem()).first().now();
            StockMethods stockMethods = new StockMethods();
            if (stockMethods.deleteStoreInformation(itemSaved,deleteOfferItem)){
                answer.setConfirmation(true);
                answer.setStoreInformation(null);
                answer.setMessage(Constants.TRUE_CONFIRMATION_DELETED_OFFER + " ID -> " + deleteOfferItem.getItem());
            }


        }catch (Exception e){
            throw new ConflictException(Constants.PRODUCT_NOT_EXISTS);
        }



        return answer;
    }


    @ApiMethod(name = "updateStockItem", path = "/stockEndpoint/v1/item/stock/update", httpMethod = ApiMethod.HttpMethod.POST)
    public UpdateStockItemResponse updateStockItem(final UpdateStockItemRequest request) {
        try {

            if (request == null) {
                UpdateStockItemResponse response = new UpdateStockItemResponse();
                ArrayList<String> messages = new ArrayList<>();
                messages.add("Request object is null.");
                response.setStatus_code(400);
                response.setMessage("Bad Request");
                response.setMessages(messages);
                return response;
            }

            if(request.getItem()==null || request.getItem()<1){
                UpdateStockItemResponse response = new UpdateStockItemResponse();
                ArrayList<String> messages = new ArrayList<>();
                messages.add("Item must be different to null and greater than zero.");
                response.setStatus_code(400);
                response.setMessage("Bad Request");
                response.setMessages(messages);
                return response;
            }

            if(request.getStock()==null || request.getStock().isEmpty()){
                UpdateStockItemResponse response = new UpdateStockItemResponse();
                ArrayList<String> messages = new ArrayList<>();
                messages.add("Stock list must be different to null and it must have elements.");
                response.setStatus_code(400);
                response.setMessage("Bad Request");
                response.setMessages(messages);
                return response;
            }

            for (StoreStockv2 storeStock : request.getStock()){
                if(storeStock.getStore()==null || storeStock.getStore()<0){
                    UpdateStockItemResponse response = new UpdateStockItemResponse();
                    ArrayList<String> messages = new ArrayList<>();
                    messages.add("Store must be different to null and less than zero.");
                    response.setStatus_code(400);
                    response.setMessage("Bad Request");
                    response.setMessages(messages);
                    return response;
                }
                if(storeStock.getStock()==null || storeStock.getStock()<0) {
                    UpdateStockItemResponse response = new UpdateStockItemResponse();
                    ArrayList<String> messages = new ArrayList<>();
                    messages.add("Store " + storeStock.getStore() + " with stock null or stock less than zero.");
                    response.setStatus_code(400);
                    response.setMessage("Bad Request");
                    response.setMessages(messages);
                    return response;
                }
            }


            Item itemSaved = null;

            try {

                Key<ItemGroup> itemGroupKey = Key.create(ItemGroup.class, "1");
                itemSaved = ofy().load().key(Key.create(itemGroupKey, Item.class, Long.toString(request.getItem()))).now();
                //itemSaved = ofy().load().type(Item.class).filter("id", request.getItem()).first().now();

                if(itemSaved==null){
                    UpdateStockItemResponse response = new UpdateStockItemResponse();
                    ArrayList<String> messages = new ArrayList<>();
                    messages.add("Item not found in datastore.");
                    response.setStatus_code(202);
                    response.setMessage("No Content");
                    response.setMessages(messages);
                    return response;
                }

                try {
                    List<StoreInformation> auxList = itemSaved.getStoreInformation();
                    if(auxList!=null) {

                        List<StoreStockv2> storeStockList = request.getStock();

                        for (StoreStockv2 storeStock : storeStockList) {
                            for (StoreInformation st : auxList) {
                                if (st.getStoreGroupId() == storeStock.getStore()) {
                                    st.setStock(storeStock.getStock());
                                }
                            }
                        }


                        itemSaved.setStoreInformation(auxList);
                        ofy().save().entity(itemSaved).now();

                        UpdateStockItemResponse response = new UpdateStockItemResponse();
                        response.setStatus("OK");
                        response.setStatus_code(200);
                        response.setMessage("Stock updated successfully.");

                        return response;
                    }else{
                        UpdateStockItemResponse response = new UpdateStockItemResponse();
                        ArrayList<String> messages = new ArrayList<>();
                        messages.add("Item does not have storeInformation list in datastore.");
                        response.setStatus_code(202);
                        response.setMessage("No Content");
                        response.setMessages(messages);
                        return response;
                    }

                }catch(Exception e){
                    UpdateStockItemResponse response = new UpdateStockItemResponse();
                    ArrayList<String> messages = new ArrayList<>();
                    messages.add("Error when the process tried updating the store information list in datastore.");
                    response.setStatus_code(500);
                    response.setMessage("Interval Server Error. Message: "+e.getMessage());
                    response.setMessages(messages);
                    return response;
                }

            }catch(Exception e){
                UpdateStockItemResponse response = new UpdateStockItemResponse();
                ArrayList<String> messages = new ArrayList<>();
                messages.add("Error when the process tried querying the item in datastore.");
                response.setStatus_code(500);
                response.setMessage("Interval Server Error. Message: "+e.getMessage());
                response.setMessages(messages);
                return response;
            }

        }catch (Exception e){
            UpdateStockItemResponse response = new UpdateStockItemResponse();
            ArrayList<String> messages = new ArrayList<>();
            messages.add("Ocurrio un error inesperado al tratar de consultar y eliminar el item.");
            messages.add("Mensaje de error : "+e.getMessage());
            response.setStatus_code(500);
            response.setMessage("Internal Server Error");
            response.setMessages(messages);
            return response;
        }
    }


}
