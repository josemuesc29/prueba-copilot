package com.imaginamos.farmatodo.backend.item;


import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.ConflictException;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.imaginamos.farmatodo.model.categories.Department;
import com.imaginamos.farmatodo.model.item.*;
import com.imaginamos.farmatodo.model.item.Item;
import com.imaginamos.farmatodo.model.product.*;
import com.imaginamos.farmatodo.model.util.*;

import java.util.*;
import java.util.logging.Logger;

import static com.imaginamos.farmatodo.backend.OfyService.ofy;


@Api(name = "itemEndpoint",
        version = "v1",
        scopes = {Constants.EMAIL_SCOPE},
        apiKeyRequired = AnnotationBoolean.TRUE,
        clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID_DEVELOPER, com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID},
        audiences = {Constants.ANDROID_AUDIENCE},
        namespace = @ApiNamespace(ownerDomain = Constants.OWNER_DOMAIN, ownerName = Constants.OWNER_NAME, packagePath = ""),
        description = "Update item")
public class ItemEndPoint {

    private ItemMethods itemMethods;
    public ItemEndPoint() {
        itemMethods = new ItemMethods();
    }

    private static final Logger LOG = Logger.getLogger(ItemEndPoint.class.getName());

    @ApiMethod(name = "itemUpload", path = "/itemEndpoint/itemUpload", httpMethod = ApiMethod.HttpMethod.POST)
    public ItemAnswer itemUpload(final ItemRequest itemRequest) throws ConflictException {

        ItemAnswer answer = new ItemAnswer();
        answer.setConfirmation(false);

        if (itemRequest == null)
            throw new ConflictException(Constants.PRODUCT_NOT_EXISTS);

        if(itemMethods.updateItem(itemRequest)){
            answer.setConfirmation(true);
            answer.setMessage(Constants.TRUE_CONFIRMATION + " ID -> "+itemRequest.getItem());
        }else {
            throw new ConflictException(Constants.PRODUCT_NOT_EXISTS);

        }
        return answer;
    }

    @ApiMethod(name ="updateMostSales", path = "/itemEndpoint/updateMostSales", httpMethod = ApiMethod.HttpMethod.PUT)
    public ItemAnswer updateMostSales(final MostSalesReq mostSalesRequest) throws BadRequestException {

        ItemAnswer itemAnswer = new ItemAnswer();
        itemAnswer.setConfirmation(false);
        itemAnswer.setMessage("Update Error");
        if (!mostSalesRequest.requestIsValid()){
            throw new BadRequestException("Bad Request");
        }
        // get all departments

        List<Department> departmentList = ofy().load().type(Department.class).list();
        if ( Objects.nonNull(departmentList) && !departmentList.isEmpty() ){
//            LOG.info("departmentList size -> " + departmentList.size());
            for (Department department : departmentList) {
                updateItemsMostSalesByDept(department, mostSalesRequest);
            }
            itemAnswer.setConfirmation(true);
            itemAnswer.setMessage("Update Successfully");
        }

        return itemAnswer;
    }

    private void updateItemsMostSalesByDept(Department departmentDs, MostSalesReq mostSalesRequest) {
//        LOG.info("method: updateItemsMostSalesByDept() ");
        if ( Objects.nonNull(departmentDs) && departmentDs.getId() > 0 ){

            Optional<com.imaginamos.farmatodo.model.util.Department> optionalDepartment = mostSalesRequest
                    .getDepartments()
                    .stream()
                    .filter( deptoReq -> Objects.nonNull(deptoReq.getDepartmentId()) && deptoReq.getDepartmentId() == departmentDs.getId())
                    .findFirst();

            optionalDepartment.ifPresent(departmentReq -> {
//                LOG.info("method: updateItemsMostSalesByDept() -> is present to update " + departmentReq.getDepartmentId());
                if (departmentReq.isValid()){

//                    LOG.info("department to replaces most sales -> "
//                            + departmentDs.getName() + " idDep: "
//                            + departmentReq.getDepartmentId());



                    // get itemMostSales for this dept
                    ItemMostSales itemMostSales = ofy()
                            .load()
                            .type(ItemMostSales.class)
                            .ancestor(departmentDs)
                            .first()
                            .now();

                    List<Suggested> suggestedListAux = new ArrayList<>();

                    if (Objects.isNull(itemMostSales)){
                       // product.setItemGroupRef(Ref.create(Key.create(ItemGroup.class, Integer.toString(1))));
//                        LOG.info("itemMostSales is null");
                        itemMostSales = new ItemMostSales();
                        itemMostSales.setDepartmentRef(Ref.create(Key.create( departmentDs )));
                        itemMostSales.setItemMostSalesId(UUID.randomUUID().toString());
                        ofy().save().entity(itemMostSales).now();
                    }

                    departmentReq.getItemsMostSales().forEach(itemsMostSaleReq -> {
                        if (itemsMostSaleReq.isValid()){
                            Suggested suggestedAux = new Suggested(itemsMostSaleReq.getItemId());
                            suggestedListAux.add(suggestedAux);
                        }
                    } );


                    if (!suggestedListAux.isEmpty() && Objects.nonNull(itemMostSales)){
//                        LOG.info("ItemMostSales update for -> " + itemMostSales.toStringJson());
                        itemMostSales.setSuggested(suggestedListAux);
                        ofy().save().entity(itemMostSales).now();
//                        LOG.info("ItemMostSales result -> " + itemMostSales.toStringJson());
                    }
                }

            });
        }

    }


    @ApiMethod(name = "itemPriceUpload", path = "/itemEndpoint/itemPriceUpload", httpMethod = ApiMethod.HttpMethod.POST)
    public ItemAnswer itemPriceUpload(final ListItemToUpdatePriceRequest listItems) throws ConflictException {
        ItemAnswer answer = new ItemAnswer();
        answer.setConfirmation(false);

        if(listItems == null)
            throw new ConflictException("Error request vacio");

        try {
            itemMethods.updateItemsPrices(listItems.getItems());
            answer.setConfirmation(true);
            answer.setMessage("Items Actualizados -> "+listItems.getItems().size());

        } catch (Exception e){
            throw new ConflictException("Error al actualizar precios  " + e.getMessage());
        }

        if(!answer.isConfirmation())
            throw new ConflictException(Constants.PRODUCT_NOT_EXISTS);


        return answer;
    }

    @ApiMethod(name = "itemCreate", path = "/itemEndpoint/v1/item/create", httpMethod = ApiMethod.HttpMethod.POST)
    public ItemCreateResponse createItem(final ItemCreateRequest request){
//        LOG.info("INI-createItem()");

        if(request==null){
            ItemCreateResponse response = new ItemCreateResponse();
            ArrayList<String> messages  = new ArrayList<>();
            messages.add("Request object is null.");
            response.setStatus_code(400);
            response.setMessage("Bad Request");
            response.setMessages(messages);
            return response;
        }

        if(request.getItem()==null){
            ItemCreateResponse response = new ItemCreateResponse();
            ArrayList<String> messages  = new ArrayList<>();
            messages.add("Item object is null.");
            response.setStatus_code(400);
            response.setMessage("Bad Request");
            response.setMessages(messages);
            return response;
        }

        if(request.getStoreInformation()==null){
            ItemCreateResponse response = new ItemCreateResponse();
            ArrayList<String> messages  = new ArrayList<>();
            messages.add("Store Information List is null");
            response.setStatus_code(400);
            response.setMessage("Bad Request");
            response.setMessages(messages);
            return response;
        }

        if(request.getStoreInformation().size()==0){
            ItemCreateResponse response = new ItemCreateResponse();
            ArrayList<String> messages  = new ArrayList<>();
            messages.add("Store Information List is empty");
            response.setStatus_code(400);
            response.setMessage("Bad Request");
            response.setMessages(messages);
            return response;
        }

        if(request.getFilterList()==null){
            ItemCreateResponse response = new ItemCreateResponse();
            ArrayList<String> messages  = new ArrayList<>();
            messages.add("Filter List is null or empty");
            response.setStatus_code(400);
            response.setMessage("Bad Request");
            response.setMessages(messages);
            return response;
        }

        if(request.getFilterList().size()==0){
            ItemCreateResponse response = new ItemCreateResponse();
            ArrayList<String> messages  = new ArrayList<>();
            messages.add("Filter List is empty");
            response.setStatus_code(400);
            response.setMessage("Bad Request");
            response.setMessages(messages);
            return response;
        }

        if(request.getSubCategories()==null){
            ItemCreateResponse response = new ItemCreateResponse();
            ArrayList<String> messages  = new ArrayList<>();
            messages.add("Subcategorie List is null or empty");
            response.setStatus_code(400);
            response.setMessage("Bad Request");
            response.setMessages(messages);
            return response;
        }

        if(request.getSubCategories().size()==0){
            ItemCreateResponse response = new ItemCreateResponse();
            ArrayList<String> messages  = new ArrayList<>();
            messages.add("Subcategorie List is empty");
            response.setStatus_code(400);
            response.setMessage("Bad Request");
            response.setMessages(messages);
            return response;
        }

        ItemCreateResponse response = itemMethods.createNewItem(request);

//        LOG.info("END-createItem()");

        return response;
    }

    @ApiMethod(name = "updateItem", path = "/itemEndpoint/v1/item/update", httpMethod = ApiMethod.HttpMethod.POST)
    public UpdateItemResponse updateItem(final UpdateItemRequest request) {
//        LOG.info("INI-updateItem()");

        if(request==null){
            UpdateItemResponse response = new UpdateItemResponse();
            ArrayList<String> messages  = new ArrayList<>();
            messages.add("Request object is null.");
            response.setStatus_code(400);
            response.setMessage("Bad Request");
            response.setMessages(messages);
            return response;
        }

        if(request.getId()==null){
            UpdateItemResponse response = new UpdateItemResponse();
            ArrayList<String> messages  = new ArrayList<>();
            messages.add("ItemId is null.");
            response.setStatus_code(400);
            response.setMessage("Bad Request");
            response.setMessages(messages);
            return response;
        }

        if(request.getBarcode()==null){
            UpdateItemResponse response = new UpdateItemResponse();
            ArrayList<String> messages  = new ArrayList<>();
            messages.add("Barcode is null.");
            response.setStatus_code(400);
            response.setMessage("Bad Request");
            response.setMessages(messages);
            return response;
        }

        if(request.getBrand()==null){
            UpdateItemResponse response = new UpdateItemResponse();
            ArrayList<String> messages  = new ArrayList<>();
            messages.add("Barcode is null.");
            response.setStatus_code(400);
            response.setMessage("Bad Request");
            response.setMessages(messages);
            return response;
        }

        if(request.getGray_description()==null){
            UpdateItemResponse response = new UpdateItemResponse();
            ArrayList<String> messages  = new ArrayList<>();
            messages.add("Gray description is null.");
            response.setStatus_code(400);
            response.setMessage("Bad Request");
            response.setMessages(messages);
            return response;
        }

        if(request.getMedia_description()==null){
            UpdateItemResponse response = new UpdateItemResponse();
            ArrayList<String> messages  = new ArrayList<>();
            messages.add("Media description is null.");
            response.setStatus_code(400);
            response.setMessage("Bad Request");
            response.setMessages(messages);
            return response;
        }

        if(request.getHighlight()==null){
            UpdateItemResponse response = new UpdateItemResponse();
            ArrayList<String> messages  = new ArrayList<>();
            messages.add("Highlight is null.");
            response.setStatus_code(400);
            response.setMessage("Bad Request");
            response.setMessages(messages);
            return response;
        }

        if(request.getImage_url()==null){
            UpdateItemResponse response = new UpdateItemResponse();
            ArrayList<String> messages  = new ArrayList<>();
            messages.add("ImageUrl is null.");
            response.setStatus_code(400);
            response.setMessage("Bad Request");
            response.setMessages(messages);
            return response;
        }

        if(request.getRequire_prescription()==null){
            UpdateItemResponse response = new UpdateItemResponse();
            ArrayList<String> messages  = new ArrayList<>();
            messages.add("RequirePrescription is null.");
            response.setStatus_code(400);
            response.setMessage("Bad Request");
            response.setMessages(messages);
            return response;
        }

        if(request.getRequire_prescription()==null){
            UpdateItemResponse response = new UpdateItemResponse();
            ArrayList<String> messages  = new ArrayList<>();
            messages.add("Highlight is null.");
            response.setStatus_code(400);
            response.setMessage("Bad Request");
            response.setMessages(messages);
            return response;
        }

        if(request.getStatus()==null){
            UpdateItemResponse response = new UpdateItemResponse();
            ArrayList<String> messages  = new ArrayList<>();
            messages.add("Status is null.");
            response.setStatus_code(400);
            response.setMessage("Bad Request");
            response.setMessages(messages);
            return response;
        }

        if(request.getCategories()==null){
            UpdateItemResponse response = new UpdateItemResponse();
            ArrayList<String> messages  = new ArrayList<>();
            messages.add("Categorie list is null.");
            response.setStatus_code(400);
            response.setMessage("Bad Request");
            response.setMessages(messages);
            return response;
        }


        if(request.getCategories().length==0){
            UpdateItemResponse response = new UpdateItemResponse();
            ArrayList<String> messages  = new ArrayList<>();
            messages.add("Categorie list is empty.");
            response.setStatus_code(400);
            response.setMessage("Bad Request");
            response.setMessages(messages);
            return response;
        }

        if(request.getFilters()==null){
            UpdateItemResponse response = new UpdateItemResponse();
            ArrayList<String> messages  = new ArrayList<>();
            messages.add("Filter list is null.");
            response.setStatus_code(400);
            response.setMessage("Bad Request");
            response.setMessages(messages);
            return response;
        }


        if(request.getFilters().length==0){
            UpdateItemResponse response = new UpdateItemResponse();
            ArrayList<String> messages  = new ArrayList<>();
            messages.add("Filter list is empty.");
            response.setStatus_code(400);
            response.setMessage("Bad Request");
            response.setMessages(messages);
            return response;
        }

        UpdateItemResponse response = itemMethods.updateFullItem(request);

//        LOG.info("END-updateItem()");

        return response;

    }

    @ApiMethod(name = "deleteItem", path = "/itemEndpoint/v1/item/delete", httpMethod = ApiMethod.HttpMethod.DELETE)
    public DeleteItemResponse deleteItem(final DeleteItemRequest request) {
//        LOG.info("INI-deleteItem()");
        try {

            if (request == null) {
                DeleteItemResponse response = new DeleteItemResponse();
                ArrayList<String> messages = new ArrayList<>();
                messages.add("Request object is null.");
                response.setStatus_code(400);
                response.setMessage("Bad Request");
                response.setMessages(messages);
                return response;
            }

            if (request.getId() == null) {
                DeleteItemResponse response = new DeleteItemResponse();
                ArrayList<String> messages = new ArrayList<>();
                messages.add("Item id is null.");
                response.setStatus_code(400);
                response.setMessage("Bad Request");
                response.setMessages(messages);
                return response;
            }

            if (request.getId() < 1) {
                DeleteItemResponse response = new DeleteItemResponse();
                ArrayList<String> messages = new ArrayList<>();
                messages.add("Id must be bigger than 0.");
                response.setStatus_code(400);
                response.setMessage("Bad Request");
                response.setMessages(messages);
                return response;
            }

            com.imaginamos.farmatodo.model.product.Item itemSaved = null;
            itemSaved = ofy().load().type(com.imaginamos.farmatodo.model.product.Item.class).filter("id", request.getId()).first().now();

            if (itemSaved != null) {
                try {
                    ofy().delete().entity(itemSaved).now();

                    DeleteItemResponse response = new DeleteItemResponse();
                    response.setStatus("OK");
                    response.setStatus_code(200);
                    response.setMessage("Item eliminado con exito.");
                    return response;

                }catch(Exception e){
                    DeleteItemResponse response = new DeleteItemResponse();
                    ArrayList<String> messages = new ArrayList<>();
                    messages.add("Ocurrio un error inesperado.Se encontro el item pero no fue posible eliminarlo.");
                    messages.add("Mensaje de error : "+e.getMessage());
                    response.setStatus_code(500);
                    response.setMessage("Internal Server Error");
                    response.setMessages(messages);
                    return response;
                }
            }else{
                DeleteItemResponse response = new DeleteItemResponse();
                ArrayList<String> messages = new ArrayList<>();
                messages.add("Item no encontrado.");
                response.setStatus_code(204);
                response.setMessage("No Content");
                response.setMessages(messages);
                return response;
            }

        }catch(Exception e){
            DeleteItemResponse response = new DeleteItemResponse();
            ArrayList<String> messages = new ArrayList<>();
            messages.add("Ocurrio un error inesperado al tratar de consultar y eliminar el item.");
            messages.add("Mensaje de error : "+e.getMessage());
            response.setStatus_code(500);
            response.setMessage("Internal Server Error");
            response.setMessages(messages);
            return response;
        }
    }

    /**
     * Update subscribe and save item
     * @param request
     * @return ItemSubscribeAndSaveResponse
     * @throws ConflictException
     */
    @ApiMethod(name = "subscribeAndSave", path = "/itemEndpoint/v1/item/subscribeAndSave", httpMethod = ApiMethod.HttpMethod.POST)
    public ItemSubscribeAndSaveResponse subscribeAndSave(final ItemSubscribeAndSaveRequest request) throws ConflictException {
//        LOG.info("INI-itemSubscribeAndSaveUpload()");

        if(request==null){
            ItemSubscribeAndSaveResponse response = new ItemSubscribeAndSaveResponse();
            ArrayList<String> messages  = new ArrayList<>();
            messages.add("Request object is null.");
            response.setStatus_code(400);
            response.setMessage("Bad Request");
            response.setMessages(messages);
            return response;
        }

        if(request.getId()==null){
            ItemSubscribeAndSaveResponse response = new ItemSubscribeAndSaveResponse();
            ArrayList<String> messages  = new ArrayList<>();
            messages.add("ItemId is null.");
            response.setStatus_code(400);
            response.setMessage("Bad Request");
            response.setMessages(messages);
            return response;
        }

        if(request.getSubscribeAndSave()==null){
            ItemSubscribeAndSaveResponse response = new ItemSubscribeAndSaveResponse();
            ArrayList<String> messages  = new ArrayList<>();
            messages.add("SubscribeAndSave is null.");
            response.setStatus_code(400);
            response.setMessage("Bad Request");
            response.setMessages(messages);
            return response;
        }

        /*if(request.getStandardDuration()==null){
            ItemSubscribeAndSaveResponse response = new ItemSubscribeAndSaveResponse();
            ArrayList<String> messages  = new ArrayList<>();
            messages.add("StandardDuration is null.");
            response.setStatus_code(400);
            response.setMessage("Bad Request");
            response.setMessages(messages);
            return response;
        }*/

        ItemSubscribeAndSaveResponse response = itemMethods.updateSubscribeAndSaveItem(request);

//        LOG.info("END-itemSubscribeAndSaveUpload()");

        return response;
    }

}

























