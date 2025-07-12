package com.farmatodo.backend.item;

import com.farmatodo.backend.user.Authenticate;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.ConflictException;
import com.google.api.server.spi.response.InternalServerErrorException;
import com.imaginamos.farmatodo.model.item.ItemReq;
import com.imaginamos.farmatodo.model.monitor.ItemRequest;
import com.imaginamos.farmatodo.model.product.Item;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.networking.services.ApiGatewayService;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * @Api - Google indicator that the present class ins an Api.
 * Every further indication of @ApiMethod includes this as a Google API.
 */
@Api(name = "itemMonitorEndpoint",
    version = "v1",
    scopes = {Constants.EMAIL_SCOPE},
    clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID_DEVELOPER, com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID},
    audiences = {Constants.ANDROID_AUDIENCE},
    namespace = @ApiNamespace(ownerDomain = Constants.OWNER_DOMAIN, ownerName = Constants.OWNER_NAME, packagePath = ""),
    description = "Get items information for monitor.")
public class ItemMonitorEndpoint {
  private static final Logger log = Logger.getLogger(ItemMonitorEndpoint.class.getName());
  private Authenticate authenticate;

  public ItemMonitorEndpoint() {
    authenticate = new Authenticate();
  }

  /**
   * Get item by barcode
   * @param itemRequest
   * @return item
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getItemByBarcode", path = "/itemMonitorEndpoint/getItemByBarcode", httpMethod = ApiMethod.HttpMethod.POST)
  public Item getItemByBarcode(
          final ItemRequest itemRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (itemRequest == null || itemRequest.getBarcode() == null)
      throw new ConflictException(Constants.ERROR_BARCODE_PRODUCT);

    if (itemRequest == null || itemRequest.getStoreId() == null)
      throw new ConflictException(Constants.ERROR_STORE_ID_PRODUCT);
    /*
    JSONObject customerJson = new JSONObject();
    customerJson.put("barcode", itemRequest.getBarcode());
    customerJson.put("storeId", itemRequest.getStoreId());
    return CoreConnection.postRequest(URLConnections.URL_ITEM_BARCODE, customerJson.toJSONString(), JSONObject.class);
    */

    ItemReq itemReq = new ItemReq();
    itemReq.setBarcode(itemRequest.getBarcode());
    itemReq.setStoreId(Long.parseLong(itemRequest.getStoreId()));
    return ApiGatewayService.get().getItemByBarcodeAndStore(itemReq);
  }

  /**
   * Get item by id
   * @param itemRequest
   * @return id
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getItemById", path = "/itemMonitorEndpoint/getItemById", httpMethod = ApiMethod.HttpMethod.POST)
  public Item getItemById(
          final ItemRequest itemRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (itemRequest == null || itemRequest.getIdItem() == 0)
      throw new ConflictException(Constants.ERROR_ID_PRODUCT);

    /*
    String url = URLConnections.URL_ITEM_ID + "/" + itemRequest.getIdItem();
    JSONObject jsonObject = CoreConnection.getRequest(url, JSONObject.class);
    if (jsonObject == null)
      throw new BadRequestException(Constants.DEFAULT_MESSAGE);
    return  jsonObject;*/

    Item item = ApiGatewayService.get().getItemById(itemRequest.getIdItem());
    if (Objects.isNull(item))
      throw new BadRequestException(Constants.DEFAULT_MESSAGE);
    return item;
  }

  /**
   * Get list substitutes by item
   * @param itemRequest
   * @return Json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getItemSubstitutes", path = "/itemMonitorEndpoint/getItemSubstitutes", httpMethod = ApiMethod.HttpMethod.POST)
  public List<Item> getItemSubstitutes(
          final ItemRequest itemRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (itemRequest == null || itemRequest.getIdItem() == 0)
      throw new ConflictException(Constants.ERROR_ID_PRODUCT);

    if (itemRequest == null || itemRequest.getStoreId() == null || itemRequest.getStoreId().isEmpty())
      throw new ConflictException(Constants.ERROR_STORE_ID);

    /*JSONObject objectJson = new JSONObject();
    objectJson.put("itemId", itemRequest.getIdItem());
    objectJson.put("storeId", itemRequest.getStoreId());
    //return CoreConnection.postListRequest(URLConnections.URL_GET_ITEM_SUBSTITUTES, objectJson.toJSONString(), JSONObject.class);*/

    return ApiGatewayService.get().getItemSubstituteByIdAndStore(itemRequest.getIdItem(), Integer.parseInt(itemRequest.getStoreId()));


  }

}

