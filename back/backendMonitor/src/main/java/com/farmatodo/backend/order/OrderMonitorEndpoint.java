package com.farmatodo.backend.order;

import com.farmatodo.backend.algolia.APIAlgolia;
import com.farmatodo.backend.user.Authenticate;
import com.farmatodo.backend.user.Users;
import com.farmatodo.backend.util.CoreConnection;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.ConflictException;
import com.google.api.server.spi.response.InternalServerErrorException;
import com.google.gson.Gson;
import com.google.zxing.WriterException;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.imaginamos.farmatodo.model.algolia.ItemAlgolia;
import com.imaginamos.farmatodo.model.algolia.WebSocketProperties;
import com.imaginamos.farmatodo.model.customer.CustomerJSON;
import com.imaginamos.farmatodo.model.monitor.*;
import com.imaginamos.farmatodo.model.order.*;
import com.imaginamos.farmatodo.model.product.Item;
import com.imaginamos.farmatodo.model.product.ItemGroup;
import com.imaginamos.farmatodo.model.provider.ProviderOrder;
import com.imaginamos.farmatodo.model.user.User;
import com.imaginamos.farmatodo.model.util.*;
import com.imaginamos.farmatodo.networking.services.ApiGatewayService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import retrofit2.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.farmatodo.backend.OfyService.ofy;

/**
 * @Api - Google indicator that the present class ins an Api.
 * Every further indication of @ApiMethod includes this as a Google API.
 */
@Api(name = "orderMonitorEndpoint",
        version = "v1",
        scopes = {Constants.EMAIL_SCOPE},
        clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID_DEVELOPER, com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID},
        audiences = {Constants.ANDROID_AUDIENCE},
        namespace = @ApiNamespace(ownerDomain = Constants.OWNER_DOMAIN, ownerName = Constants.OWNER_NAME, packagePath = ""),
        description = "Get orders information for monitor.")
public class OrderMonitorEndpoint {
  private static final Logger log = Logger.getLogger(OrderMonitorEndpoint.class.getName());
  private Authenticate authenticate;

  private Orders orders;
  private ProductsMethods productsMethods;
    private Users users;
  private final String ORDER_CANCELED = "order_canceled";

  public OrderMonitorEndpoint() {
    authenticate = new Authenticate();
    orders = new Orders();
    users = new Users();
    productsMethods = new ProductsMethods();
  }

  /**
   * Get orders
   * @param orderRequest
   * @return json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getOrders", path = "/orderMonitorEndpoint/getOrders", httpMethod = ApiMethod.HttpMethod.POST)
  public List<JSONObject> getOrders(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getEmployeeNumber() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NUMBER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    return CoreConnection.postListRequest(URLConnections.URL_GET_ORDERS, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Validate bill order
   * @param billedOrderRequest
   * @return json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "validateBillOrder", path = "/orderMonitorEndpoint/validateBillOrder", httpMethod = ApiMethod.HttpMethod.POST)
  public BilledOrder validateBillOrder(
          final BilledOrderRequest billedOrderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (billedOrderRequest == null || billedOrderRequest.getOrderId() == null)
      throw new ConflictException(Constants.ERROR_ID_ORDER);

    if (billedOrderRequest == null || billedOrderRequest.getStoreId() == null)
      throw new ConflictException(Constants.ERROR_ORDER_BILL_STORE_ID);

    if (billedOrderRequest == null || billedOrderRequest.getBillId() == null)
      throw new ConflictException(Constants.ERROR_ORDER_BILL_ID);

    if (billedOrderRequest == null || billedOrderRequest.getBillDate() == null)
      throw new ConflictException(Constants.ERROR_ORDER_BILL_DATE);

    JSONObject customerJson = new JSONObject();
    customerJson.put("orderId", billedOrderRequest.getOrderId());
    customerJson.put("storeId", billedOrderRequest.getStoreId());
    customerJson.put("billId", billedOrderRequest.getBillId());
    customerJson.put("billDate", billedOrderRequest.getBillDate());

    BilledOrder billedOrder = CoreConnection.postRequest(URLConnections.URL_VALIDATE_BILL_ORDER, customerJson.toJSONString(), BilledOrder.class);
    if (billedOrder != null && billedOrder.getListDetail() != null && billedOrder.getListDetail().size() > 0) {
      billedOrder.getListDetail().stream().forEach(x -> {
        x.setId(x.getItemID());
        if (x.getBarcode() == null)
          x.setBarcode("");
        if (x.getImage() == null)
          x.setImage("");
        if (x.getItemName() == null)
          x.setItemName("");
        if (x.getPrice() == null)
          x.setPrice(Double.valueOf(0));
        if (x.getStoreName() == null)
          x.setStoreName("");
        if (x.getStoreAddress() == null)
          x.setStoreAddress("");
      });
    }

    return billedOrder;
  }

  /**
   * Get order detail
   * @param orderRequest
   * @return List order detail
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getOrderDetail", path = "/orderMonitorEndpoint/getOrderDetail", httpMethod = ApiMethod.HttpMethod.POST)
  public JSONObject getOrderDetail(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getOrderId() == null)
      throw new ConflictException(Constants.ERROR_ID_ORDER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("orderId", orderRequest.getOrderId());

    return CoreConnection.postRequest(URLConnections.URL_GET_DETAIL_ORDER, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Cancer order
   * @param orderRequest
   * @return Message
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "cancelOrder", path = "/orderMonitorEndpoint/cancelOrder", httpMethod = ApiMethod.HttpMethod.POST)
  public CancelOrderResponse cancelOrder(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {
    if (orderRequest.getOrderId() == null || orderRequest.getOrderId() <= 0)
      throw new ConflictException(Constants.ERROR_ID_ORDER);
    if (orderRequest == null || orderRequest.getRol() == null)
      throw new ConflictException(Constants.ERROR_USER_ROL);
    if (orderRequest == null || orderRequest.getCorreoUsuario() == null)
      throw new ConflictException(Constants.ERROR_USER_EMAIL);
    if (orderRequest == null || orderRequest.getCancellationReasonId() == null)
      throw new ConflictException(Constants.ERROR_CANCELLATION_REASON);
    JSONObject customerJson = new JSONObject();
    customerJson.put("orderId", orderRequest.getOrderId());
    customerJson.put("observation", orderRequest.getObservation());
    customerJson.put("rol", orderRequest.getRol());
    customerJson.put("correoUsuario", orderRequest.getCorreoUsuario());
    customerJson.put("cancellationReasonId", orderRequest.getCancellationReasonId());
    if (orderRequest.getEmployeeNumber() != null && !orderRequest.getEmployeeNumber().isEmpty())
      customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());
    //JSONObject url = CoreConnection.postRequest(URLConnections.URL_CANCEL_ORDER, customerJson.toJSONString(), JSONObject.class);
    CancelOrderResponse cancelOrderResponse = CoreConnection.postRequest(URLConnections.URL_CANCEL_ORDER, customerJson.toJSONString(), CancelOrderResponse.class);
    if (cancelOrderResponse != null
            && cancelOrderResponse.getCode() == null
            && cancelOrderResponse.getMessage() != null
            && !cancelOrderResponse.getMessage().isEmpty()){
      DeliveryOrder deliveryOrder = ofy().load().type(DeliveryOrder.class).filter("idOrder", orderRequest.getOrderId()).first().now();
      if(deliveryOrder != null) {
        deliveryOrder.setLastStatus(ORDER_CANCELED);
        deliveryOrder.setActive(false);
        ofy().save().entities(deliveryOrder);
      }
    }
    log.info("request: " + customerJson.toJSONString());
    //return CoreConnection.postRequest(URLConnections.URL_CANCEL_ORDER, customerJson.toJSONString(), JSONObject.class);
    return cancelOrderResponse;
  }

  /**
   * Validate order token
   * @param orderRequest
   * @return Message
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "validateOrderToken", path = "/orderMonitorEndpoint/validateOrderToken", httpMethod = ApiMethod.HttpMethod.POST)
  public JSONObject validateOrderToken(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getOrderId() == null)
      throw new ConflictException(Constants.ERROR_ID_ORDER);

    if (orderRequest == null || orderRequest.getToken() == null)
      throw new ConflictException(Constants.ERROR_ORDER_TOKEN);

    JSONObject customerJson = new JSONObject();
    customerJson.put("orderId", orderRequest.getOrderId());
    customerJson.put("token", orderRequest.getToken());

    return CoreConnection.postRequest(URLConnections.URL_VALIDATE_ORDER_TOKEN, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Save order observation
   * @param orderRequest
   * @return Message
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "saveOrderObservation", path = "/orderMonitorEndpoint/saveOrderObservation", httpMethod = ApiMethod.HttpMethod.POST)
  public JSONObject saveOrderObservation(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getOrderId() == null)
      throw new ConflictException(Constants.ERROR_ID_ORDER);

    if (orderRequest == null || orderRequest.getObservation() == null)
      throw new ConflictException(Constants.ERROR_ORDER_OBSERVATION);

    if (orderRequest == null || orderRequest.getEmployeeNumber() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NUMBER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("orderId", orderRequest.getOrderId());
    customerJson.put("observation", orderRequest.getObservation());
    customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    return CoreConnection.postRequest(URLConnections.URL_SAVE_ORDER_OBSERVATION, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Get order observations
   * @param orderRequest
   * @return Order observations
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getOrderObservations", path = "/orderMonitorEndpoint/getOrderObservations", httpMethod = ApiMethod.HttpMethod.POST)
  public List<JSONObject> getOrderObservations(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getOrderId() == null)
      throw new ConflictException(Constants.ERROR_ID_ORDER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("orderId", orderRequest.getOrderId());

    return CoreConnection.postListRequest(URLConnections.URL_GET_ORDER_OBSERVATIONS, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Search orders by filters
   * @param orderRequest
   * @return List orders
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "searchOrders", path = "/orderMonitorEndpoint/searchOrders", httpMethod = ApiMethod.HttpMethod.POST)
  public List<JSONObject> searchOrders(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getEmployeeNumber() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NUMBER);

    if (orderRequest.getStartDate() == null && orderRequest.getEndDate() == null
            && orderRequest.getDocumentNumberClient() == null
            && (orderRequest.getOrders() == null || orderRequest.getOrders().size() == 0)
            && orderRequest.getOrderId() == null)
      throw new ConflictException(Constants.ERROR_SEARCH_ORDER_FILTER);


    JSONObject customerJson = new JSONObject();
    customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    if (orderRequest.getStartDate() != null && orderRequest.getEndDate() != null) {
      customerJson.put("startDate", orderRequest.getStartDate());
      customerJson.put("endDate", orderRequest.getEndDate());
    }

    if (orderRequest.getDocumentNumberClient() != null){
      customerJson.put("documentNumberClient", orderRequest.getDocumentNumberClient());
    }

    if (orderRequest.getOrderId() != null){
      customerJson.put("orderId", orderRequest.getOrderId());
    }

    if (orderRequest.getOrders() != null && orderRequest.getOrders().size() > 0){
      JSONArray orderArray = new JSONArray();

      for (Order order: orderRequest.getOrders()) {
        if (order.getOrderId() != null) {
          JSONObject orderJson = new JSONObject();
          orderJson.put("orderId", order.getOrderId());
          orderArray.add(orderJson);
        }
      }

      customerJson.put("orders", orderArray);
    }

    return CoreConnection.postListRequest(URLConnections.URL_SEARCH_ORDERS, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Validate delivery order
   * @param shoppingCartJson
   * @return DeliveryOrder
   * @throws ConflictException
   * @throws IOException
   * @throws BadRequestException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "validateDeliveryOrder", path = "/orderMonitorEndpoint/validateDeliveryOrder", httpMethod = ApiMethod.HttpMethod.POST)
  public DeliveryOrder validateDeliveryOrder(final ShoppingCartJson shoppingCartJson) throws ConflictException, IOException,
          BadRequestException, InternalServerErrorException {

    log.warning("method: validateDeliveryOrder()");

    if (shoppingCartJson.getOrderItems() == null || shoppingCartJson.getOrderItems().size() == 0) {
      throw new ConflictException("NO HAY ITEMS EN LA ORDEN");
    }

    User user = users.findUserByIdCustomer(shoppingCartJson.getId());
    Key<User> customerKey = Key.create(User.class, user.getId());

    DeliveryOrder deliveryOrder = ofy().load().type(DeliveryOrder.class).filter("idOrder", shoppingCartJson.getIdOrder()).first().now();
    if (deliveryOrder == null) {
      log.info("No existe un carrito activo para el cliente.");
      deliveryOrder = getEmptyDeliveryOrder(new DeliveryOrder());
    } else {
      log.info("deliveryOrder.deliveryType :" + deliveryOrder.getDeliveryType());
      log.info("Existe un carrito activo para el cliente, se procede a validar.");
      final List<DeliveryOrderItem> deliveryOrderItemList = ofy().load().type(DeliveryOrderItem.class).ancestor(deliveryOrder).list();
      final List<DeliveryOrderProvider> deliveryOrderProviderList = ofy().load().type(DeliveryOrderProvider.class).ancestor(deliveryOrder).list();

      if (Objects.isNull(deliveryOrderItemList) && Objects.isNull(deliveryOrderProviderList)) {
        log.warning("deliveryOrderItemList is null");
        throw new ConflictException("NO HAY ITEMS AGREGADOS");
      }
      //log.warning("items size: [" + (Objects.nonNull(deliveryOrderItemList) ? deliveryOrderItemList.size() : 0) + "]");
      //log.warning("providers size: [" + (Objects.nonNull(deliveryOrderProviderList) ? deliveryOrderProviderList.size() : 0) + "]");
      if (deliveryOrderItemList.isEmpty() && deliveryOrderProviderList.isEmpty()) {
        deliveryOrder = getEmptyDeliveryOrder(new DeliveryOrder());
      } else {
        final List<DeliveryOrderItem> deliveryOrderItemListToSave = new ArrayList<>();
        final List<DeliveryOrderProvider> deliveryOrderProviderListToSave = new ArrayList<>();
        boolean hasItems = false;
        for (DeliveryOrderItem deliveryOrderItem : deliveryOrderItemList) {
          log.warning("itemId: [" + deliveryOrderItem.getId() + "] is coupon: [" + deliveryOrderItem.getCoupon() + "]");
          if (deliveryOrderItem.getCoupon() == null || !deliveryOrderItem.getCoupon()) {
            hasItems = true;
          }
        }
        if (hasItems) {
          //log.warning("Enviar a validar el carro de compras al Backend3.");
          String orderRequest = orders.createValidateOrderMonitorJson(shoppingCartJson.getId(), shoppingCartJson.getIdStoreGroup(),
                  shoppingCartJson.getOrderItems(), shoppingCartJson.getSource(), shoppingCartJson.getDeliveryType()).toJSONString();
          log.warning(orderRequest);
          //OrderJson orderJSON = CoreConnection.postRequest(URLConnections.URL_ORDER_VALIDATE, orderRequest, OrderJson.class);
          Gson gson = new Gson();
          ValidateOrderReq validateOrderReq = gson.fromJson(orderRequest, ValidateOrderReq.class);
          validateOrderReq.setIdCustomerWebSafe(shoppingCartJson.getIdCustomerWebSafe());
          Response<ValidateOrderBackend3> response = ApiGatewayService.get().validateOrder(validateOrderReq, null);
          OrderJson orderJSON = Objects.nonNull(response) && Objects.nonNull(response.body()) ? response.body().getData() : null;


          Key<DeliveryOrder> deliveryOrderKey = Key.create(customerKey, DeliveryOrder.class, deliveryOrder.getIdDeliveryOrder());

          deliveryOrder.setSubTotalPrice(0d);
          deliveryOrder.setOfferPrice(0d);

          for (ItemAlgolia itemOrder : orderJSON.getItems()) {
            addDeliveryItemOrder(itemOrder, shoppingCartJson, deliveryOrderKey, deliveryOrderItemListToSave, deliveryOrder, null);
          }
          for (DeliveryOrderItem item : deliveryOrderItemList) {
            if (item.getCoupon() != null && item.getCoupon()) {
              deliveryOrderItemListToSave.add(item);
            }
          }

          if (Objects.nonNull(orderJSON.getProviders()) && !orderJSON.getProviders().isEmpty()) {
            for (ProviderOrder provider : orderJSON.getProviders()) {
              DeliveryOrderProvider providerOrder = new DeliveryOrderProvider(provider.getId(), provider.getName(), provider.getEmail(), provider.getDeliveryPrice());
              for (ItemAlgolia deliveryOrderItem : provider.getItems()) {
                addDeliveryItemOrder(deliveryOrderItem, shoppingCartJson, deliveryOrderKey, providerOrder.getItemList(), deliveryOrder, providerOrder);
              }
              providerOrder.setQuantityItem(provider.getItems().stream().mapToInt(item -> item.getQuantityRequested()).sum());
              deliveryOrderProviderListToSave.add(providerOrder);
            }
          }
          deliveryOrder.setOfferPrice(deliveryOrder.getOfferPrice() + orderJSON.getGlobalDiscount());
          deliveryOrder.setWeight(orderJSON.getWeight());
          deliveryOrder.setLowerRangeWeight(orderJSON.getLowerRangeWeight());
          deliveryOrder.setTopRangeWeight(orderJSON.getTopRangeWeight());
          deliveryOrder.setDeliveryPrice(orderJSON.getDeliveryValue());
          deliveryOrder.setRegisteredOffer(orderJSON.getRegisteredDiscount());
          deliveryOrder.setTotalPrice(deliveryOrder.getSubTotalPrice() - deliveryOrder.getOfferPrice() + (orderJSON.getDeliveryValue() + orderJSON.getProviderDeliveryValue()));
          // Campos nuevos proveedores
          deliveryOrder.setProviderDeliveryPrice(orderJSON.getProviderDeliveryValue());
          deliveryOrder.setTotalDelivery(orderJSON.getDeliveryValue() + orderJSON.getProviderDeliveryValue());
          if (Objects.nonNull(deliveryOrderProviderListToSave) && !deliveryOrderProviderListToSave.isEmpty()) {
            log.warning(" Asigna cantidad de Items de proveedor. ");
            deliveryOrder.setQuantityProviders(deliveryOrderProviderListToSave.stream().mapToInt(provider -> provider.getQuantityItem()).sum());
          } else {
            deliveryOrder.setQuantityProviders(0);
          }
          deliveryOrder.setQuantityFarmatodo(Objects.nonNull(deliveryOrderItemList) ? deliveryOrderItemList.stream().mapToInt(item -> item.getQuantitySold()).sum() : 0);
          deliveryOrder.setQuantityFarmatodo(deliveryOrder.getQuantityFarmatodo() > 0 ? deliveryOrder.getQuantityFarmatodo() - deliveryOrder.getQuantityProviders() : deliveryOrder.getQuantityFarmatodo());
          deliveryOrder.setTotalQuantity(deliveryOrder.getQuantityFarmatodo() + deliveryOrder.getQuantityProviders());

        } else {
          log.warning("el carro de compras por el momento solo contiene cupones, no es necesario mandarlo a validar.");
          for (DeliveryOrderItem item : deliveryOrderItemList) {
            if (item.getCoupon() != null && item.getCoupon()) {
              deliveryOrderItemListToSave.add(item);
            }
          }
          deliveryOrder = getEmptyDeliveryOrder(deliveryOrder);
        }
        Key<DeliveryOrder> deliveryOrderKey = ofy().save().entity(deliveryOrder).now();
        deliveryOrder.setItemList(deliveryOrderItemListToSave);
        deliveryOrder.setProviderList(deliveryOrderProviderListToSave);
        log.warning("Key delivery Order with Provider: " + deliveryOrderKey);
      }
    }
    deliveryOrder.setDeliveryType(shoppingCartJson.getDeliveryType());
    return deliveryOrder;
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

  private void addDeliveryItemOrder(final ItemAlgolia itemOrder,
                                    final ShoppingCartJson shoppingCartJson,
                                    final Key<DeliveryOrder> deliveryOrderKey,
                                    final List<DeliveryOrderItem> deliveryOrderItemListToSave,
                                    DeliveryOrder deliveryOrder,
                                    DeliveryOrderProvider providerOrder) throws BadRequestException {
    if (itemOrder.getPrice() >= 0) {
      Key<ItemGroup> itemGroupKey = Key.create(ItemGroup.class, "1");
      log.warning("method: addDeliveryItemOrder : "+Key.create(itemGroupKey, Item.class, Key.create(itemGroupKey, Item.class, Long.toString(itemOrder.getItem())).toWebSafeString()));
      Item item = ofy().load().key(Key.create(itemGroupKey, Item.class, Long.toString(itemOrder.getItem()))).now();
      if (item == null)
        throw new BadRequestException("ITEM NO ENCONTRADO");

      if (item != null)
        item = productsMethods.setStoreInfo(item, shoppingCartJson.getIdStoreGroup());

      DeliveryOrderItem deliveryOrderItem = new DeliveryOrderItem();
      deliveryOrderItem.setIdDeliveryOrderItem(UUID.randomUUID().toString());
      deliveryOrderItem.setIdDeliveryOrder(Ref.create(deliveryOrderKey));
      deliveryOrderItem.setIdItem(Key.create(Item.class, item.getItemId()));
      deliveryOrderItem.setCreateDate(new Date());
      deliveryOrderItem.setQuantitySold(itemOrder.getQuantityRequested());
      if (itemOrder.getPrice() != itemOrder.getDiscount())
        deliveryOrderItem.setFullPrice((double) itemOrder.getPrice());
      else
        deliveryOrderItem.setFullPrice((double) 0);
      if (itemOrder.getDiscount() != 0)
        deliveryOrderItem.setOfferPrice((double) (itemOrder.getPrice() - (itemOrder.getDiscount() / itemOrder.getQuantityRequested())));
      else
        deliveryOrderItem.setOfferPrice((double) 0);
      deliveryOrderItem.setChangeQuantity(itemOrder.getAccess());
      deliveryOrderItem = this.deliveryOrderItemReturn(deliveryOrderItem, item);
      deliveryOrderItemListToSave.add(deliveryOrderItem);
      deliveryOrder.setSubTotalPrice(deliveryOrder.getSubTotalPrice()+itemOrder.getFullPrice());
      deliveryOrder.setOfferPrice(deliveryOrder.getOfferPrice()+itemOrder.getDiscount());

      if(Objects.nonNull(providerOrder)){
        saveProviderItem(deliveryOrderKey, providerOrder, deliveryOrderItem);
      }
    } else {
      deliveryOrder.setOfferPrice(deliveryOrder.getOfferPrice()+Math.abs(itemOrder.getPrice()));
    }
  }

  private void saveProviderItem(final Key<DeliveryOrder> deliveryOrderKey, DeliveryOrderProvider providerOrder, final DeliveryOrderItem deliveryOrderItemBase){
    log.warning("method: saveProviderItem Init");
    String uuiKey = generateProviderKey(Key.create(deliveryOrderKey, DeliveryOrderProvider.class, Long.toString(providerOrder.getId())).toWebSafeString(),
            providerOrder.getId());
    Key<DeliveryOrderProvider> findKey = Key.create(deliveryOrderKey, DeliveryOrderProvider.class, uuiKey);
    DeliveryOrderProvider deliveryOrderProvider = ofy().load().type(DeliveryOrderProvider.class).filterKey("=", findKey).first().now();
    log.warning("method: saveProviderItem Init Key : "+uuiKey);
    log.warning("method: saveProviderItem Init Find Key : "+findKey);
    if(Objects.isNull(deliveryOrderProvider)){
      providerOrder.setIdDeliveryOrder(Ref.create(deliveryOrderKey));
      providerOrder.setIdDeliveryOrderProvider(uuiKey);
      providerOrder.setItemList(new ArrayList<>());
    }else{
      providerOrder = deliveryOrderProvider;
    }
    // si el item existe se reemplaza por la ultima version
    Optional<DeliveryOrderItem> optionalDeliveryOrderItem = providerOrder.getItemList().stream()
            .filter(itemProvider -> itemProvider.getId() == deliveryOrderItemBase.getId()).findFirst();
    if(optionalDeliveryOrderItem.isPresent()) {
      log.warning("method: saveProviderItem Encuentra Item");
      providerOrder.getItemList().remove(optionalDeliveryOrderItem.get());
    }
    // Se agrega item al proveedor
    providerOrder.getItemList().add(deliveryOrderItemBase);
    Key<DeliveryOrderProvider> deliveryOrderProviderKey = ofy().save().entity(providerOrder).now();
    log.warning("method: saveProviderItem Crea Proveedor Key: "+ deliveryOrderProviderKey);
  }

  private String generateProviderKey(String providerKey, Long providerId){
    return UUID.nameUUIDFromBytes((providerKey+providerId).getBytes()).toString();
  }

  private DeliveryOrder getEmptyDeliveryOrder(DeliveryOrder deliveryOrder){
    deliveryOrder.setSubTotalPrice(0);
    deliveryOrder.setOfferPrice(0);
    deliveryOrder.setDeliveryPrice(0);
    deliveryOrder.setRegisteredOffer(0);
    deliveryOrder.setTotalPrice(0);
    deliveryOrder.setProviderDeliveryPrice(0);
    deliveryOrder.setTotalDelivery(0);
    deliveryOrder.setQuantityFarmatodo(0);
    deliveryOrder.setQuantityProviders(0);
    deliveryOrder.setWeight(0);
    return deliveryOrder;
  }

  /**
   * Get orders 1.5
   * @param orderRequest
   * @return json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getOrders15", path = "/orderMonitorEndpoint/getOrders15", httpMethod = ApiMethod.HttpMethod.POST)
  public List<JSONObject> getOrders15(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getEmployeeNumber() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NUMBER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    return CoreConnection.postListRequest(URLConnections.URL_GET_ORDERS_1_5, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Search orders 1.5 by filters
   * @param orderRequest
   * @return List orders
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "searchOrders15", path = "/orderMonitorEndpoint/searchOrders15", httpMethod = ApiMethod.HttpMethod.POST)
  public List<JSONObject> searchOrders15(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getEmployeeNumber() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NUMBER);

    if (orderRequest.getStartDate() == null
            && orderRequest.getEndDate() == null
            && orderRequest.getDocumentNumberClient() == null
            && (orderRequest.getOrders() == null || orderRequest.getOrders().size() == 0)
            && orderRequest.getOrderId() == null)
      throw new ConflictException(Constants.ERROR_SEARCH_ORDER_FILTER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    if (orderRequest.getStartDate() != null && orderRequest.getEndDate() != null) {
      customerJson.put("startDate", orderRequest.getStartDate());
      customerJson.put("endDate", orderRequest.getEndDate());
    }

    if (orderRequest.getDocumentNumberClient() != null){
      customerJson.put("documentNumberClient", orderRequest.getDocumentNumberClient());
    }

    if (orderRequest.getOrderId() != null){
      customerJson.put("orderId", orderRequest.getOrderId());
    }

    if (orderRequest.getOrders() != null && orderRequest.getOrders().size() > 0){
      JSONArray orderArray = new JSONArray();

      for (Order order: orderRequest.getOrders()) {
        if (order.getOrderId() != null) {
          JSONObject orderJson = new JSONObject();
          orderJson.put("orderId", order.getOrderId());
          orderArray.add(orderJson);
        }
      }

      customerJson.put("orders", orderArray);
    }

    return CoreConnection.postListRequest(URLConnections.URL_SEARCH_ORDERS_1_5, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Save order 1.5 observation
   * @param orderRequest
   * @return Message
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "saveOrderObservation15", path = "/orderMonitorEndpoint/saveOrderObservation15", httpMethod = ApiMethod.HttpMethod.POST)
  public JSONObject saveOrderObservation15(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getOrderId() == null)
      throw new ConflictException(Constants.ERROR_ID_ORDER);

    if (orderRequest == null || orderRequest.getObservation() == null)
      throw new ConflictException(Constants.ERROR_ORDER_OBSERVATION);

    if (orderRequest == null || orderRequest.getEmployeeNumber() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NUMBER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("orderId", orderRequest.getOrderId());
    customerJson.put("observation", orderRequest.getObservation());
    customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    return CoreConnection.postRequest(URLConnections.URL_SAVE_ORDER_OBSERVATION_1_5, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Get order 1.5 observations
   * @param orderRequest
   * @return Order observations
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getOrderObservations15", path = "/orderMonitorEndpoint/getOrderObservations15", httpMethod = ApiMethod.HttpMethod.POST)
  public List<JSONObject> getOrderObservations15(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getOrderId() == null)
      throw new ConflictException(Constants.ERROR_ID_ORDER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("orderId", orderRequest.getOrderId());

    return CoreConnection.postListRequest(URLConnections.URL_GET_ORDER_OBSERVATIONS_1_5, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Get order 1.5 detail
   * @param orderRequest
   * @return List order detail
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getOrderDetail15", path = "/orderMonitorEndpoint/getOrderDetail15", httpMethod = ApiMethod.HttpMethod.POST)
  public JSONObject getOrderDetail15(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getOrderId() == null)
      throw new ConflictException(Constants.ERROR_ID_ORDER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("orderId", orderRequest.getOrderId());

    return CoreConnection.postRequest(URLConnections.URL_GET_DETAIL_ORDER_1_5, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Cancer order 1.5
   * @param orderRequest
   * @return Message
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "cancelOrder15", path = "/orderMonitorEndpoint/cancelOrder15", httpMethod = ApiMethod.HttpMethod.POST)
  public JSONObject cancelOrder15(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getOrderId() == null)
      throw new ConflictException(Constants.ERROR_ID_ORDER);

    if (orderRequest == null || orderRequest.getRol() == null)
      throw new ConflictException(Constants.ERROR_USER_ROL);

    if (orderRequest == null || orderRequest.getCorreoUsuario() == null)
      throw new ConflictException(Constants.ERROR_USER_EMAIL);

    JSONObject customerJson = new JSONObject();
    customerJson.put("orderId", orderRequest.getOrderId());
    customerJson.put("observation", orderRequest.getObservation());
    customerJson.put("rol", orderRequest.getRol());
    customerJson.put("correoUsuario", orderRequest.getCorreoUsuario());

    if (orderRequest.getEmployeeNumber() != null && !orderRequest.getEmployeeNumber().isEmpty())
      customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    return CoreConnection.postRequest(URLConnections.URL_CANCEL_ORDER_1_5, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Get couries
   * @return json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getCouries", path = "/orderMonitorEndpoint/getCouries", httpMethod = ApiMethod.HttpMethod.GET)
  public List<CourierRes> getCouries() throws ConflictException, BadRequestException, IOException, InternalServerErrorException {
    return ApiGatewayService.get().getCourierAll();
    //return CoreConnection.getListRequest(URLConnections.URL_GET_COURIES, JSONObject.class);
  }

  /**
   * Reassign order
   * @param orderRequest
   * @return json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "reassignOrder", path = "/orderMonitorEndpoint/reassignOrder", httpMethod = ApiMethod.HttpMethod.POST)
  public JSONObject reassignOrder(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getOrderId() == null)
      throw new ConflictException(Constants.ERROR_ID_ORDER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("orderId", orderRequest.getOrderId());
    customerJson.put("courierId", orderRequest.getCourierId());

    if (orderRequest.getEmployeeNumber() != null && !orderRequest.getEmployeeNumber().isEmpty())
      customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    return CoreConnection.postRequest(URLConnections.URL_REASSIGN_ORDER, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Reassign order 1.5
   * @param orderRequest
   * @return json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "reassignOrder15", path = "/orderMonitorEndpoint/reassignOrder15", httpMethod = ApiMethod.HttpMethod.POST)
  public JSONObject reassignOrder15(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getOrderId() == null)
      throw new ConflictException(Constants.ERROR_ID_ORDER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("orderId", orderRequest.getOrderId());
    customerJson.put("courierId", orderRequest.getCourierId());

    if (orderRequest.getEmployeeNumber() != null && !orderRequest.getEmployeeNumber().isEmpty())
      customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    return CoreConnection.postRequest(URLConnections.URL_REASSIGN_ORDER_1_5, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Get stores by courier
   * @param courierId
   * @return json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getStoresByCourier", path = "/orderMonitorEndpoint/getStoresByCourier", httpMethod = ApiMethod.HttpMethod.GET)
  public List<JSONObject> getStoresByCourier(
          @Named("courierId") final String courierId
  ) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    String url = URLConnections.URL_GET_STORES_COURIER + "/" + courierId;
    return CoreConnection.getListRequest(url, JSONObject.class);
  }

  /**
   * Get stores without courier
   * @param courierId
   * @return jeson response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getStoresWithOutCourier", path = "/orderMonitorEndpoint/getStoresWithOutCourier", httpMethod = ApiMethod.HttpMethod.GET)
  public List<JSONObject> getStoresWithOutCourier(
          @Named("courierId") final String courierId
  ) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    String url = URLConnections.URL_GET_STORES_WITHOUT_COURIER + "/" + courierId;
    return CoreConnection.getListRequest(url, JSONObject.class);
  }

  /**
   * Update store courier
   * @param storeCourierRequest
   * @return json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "updateStoresWithCourier", path = "/orderMonitorEndpoint/updateStoresWithCourier", httpMethod = ApiMethod.HttpMethod.PUT)
  public JSONObject updateStoresWithCourier(
          final StoreCourierRequest storeCourierRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (storeCourierRequest == null || storeCourierRequest.getStoresByCourier() == null || storeCourierRequest.getStoresByCourier().size() == 0)
      throw new ConflictException(Constants.ERROR_LIST_STORE_COURIER);

    JSONArray stores = new JSONArray();
    for (StoreCourier storeCourier : storeCourierRequest.getStoresByCourier()) {
      JSONObject storeJson = new JSONObject();
      storeJson.put("storeId", storeCourier.getStoreId());
      storeJson.put("courierId", storeCourier.getCourierId());
      stores.add(storeJson);
    }

    JSONObject objectJson = new JSONObject();
    objectJson.put("storesByCourier", stores);

    return CoreConnection.postRequest(URLConnections.URL_UPDATE_STORES_COURIER, objectJson.toJSONString(), JSONObject.class);
  }

  /**
   * Update store courier
   * @param storeCourierRequest
   * @return json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "updateStoresWithOutCourier", path = "/orderMonitorEndpoint/updateStoresWithOutCourier", httpMethod = ApiMethod.HttpMethod.PUT)
  public JSONObject updateStoresWithOutCourier(
          final StoreCourierRequest storeCourierRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (storeCourierRequest == null || storeCourierRequest.getStoresByCourier() == null || storeCourierRequest.getStoresByCourier().size() == 0)
      throw new ConflictException(Constants.ERROR_LIST_STORE_COURIER);

    JSONArray stores = new JSONArray();
    for (StoreCourier storeCourier : storeCourierRequest.getStoresByCourier()) {
      JSONObject storeJson = new JSONObject();
      storeJson.put("storeId", storeCourier.getStoreId());
      storeJson.put("courierId", storeCourier.getCourierId());
      stores.add(storeJson);
    }

    JSONObject objectJson = new JSONObject();
    objectJson.put("storesByCourier", stores);

    return CoreConnection.postRequest(URLConnections.URL_UPDATE_STORES_WITHOUT_COURIER, objectJson.toJSONString(), JSONObject.class);
  }

  /**
   * Update order
   * @param orderRequest
   * @return json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "updateOrder", path = "/orderMonitorEndpoint/updateOrder", httpMethod = ApiMethod.HttpMethod.PUT)
  public Answer updateOrder(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getOrderId() == null)
      throw new ConflictException(Constants.ERROR_ID_ORDER);

    if (orderRequest == null || orderRequest.getItems() == null || orderRequest.getItems().size() == 0)
      throw new ConflictException(Constants.ERROR_LIST_ITEMS_ORDER);

    if (orderRequest == null || orderRequest.getDomicilio() == null || orderRequest.getDomicilio().isEmpty())
      throw new ConflictException(Constants.ERROR_DOMICILIO_ORDER);

    Answer answer = new Answer();
    answer.setConfirmation(false);
    answer.setMessage(UnsupportedOperationException.class.toString());
    return answer;
  }

  /*
   * Validate delivery order 1.5
   * @param shoppingCartJson
   * @return json response
   * @throws ConflictException
   * @throws IOException
   * @throws BadRequestException
   * @throws InternalServerErrorException
   */
  /*
  @ApiMethod(name = "validateDeliveryOrder15", path = "/orderMonitorEndpoint/validateDeliveryOrder15", httpMethod = ApiMethod.HttpMethod.POST)
  public DeliveryOrder validateDeliveryOrder15(final ShoppingCartJson shoppingCartJson) throws ConflictException, IOException,
          BadRequestException, InternalServerErrorException {

    log.warning("method: validateDeliveryOrder15()");

    if (shoppingCartJson.getOrderItems() == null || shoppingCartJson.getOrderItems().size() == 0) {
      throw new ConflictException("NO HAY ITEMS EN LA ORDEN");
    }

    log.warning("Enviar a validar el carro de compras al CORE.");

    DeliveryOrder deliveryOrder = new DeliveryOrder();
    final List<DeliveryOrderItem> deliveryOrderItemListToSave = new ArrayList<>();
    String orderRequest = orders.createValidateOrderMonitorJson(shoppingCartJson.getId(), shoppingCartJson.getIdStoreGroup(),
            shoppingCartJson.getOrderItems(), shoppingCartJson.getSource(), shoppingCartJson.getDeliveryType()).toJSONString();
    log.warning(orderRequest);
    OrderJson orderJSON = CoreConnection.postRequest(URLConnections.URL_ORDER_VALIDATE, orderRequest, OrderJson.class);

    deliveryOrder.setSubTotalPrice(0d);
    deliveryOrder.setOfferPrice(0d);

    for (ItemAlgolia itemOrder : orderJSON.getItems()) {
      addDeliveryItemOrder15(itemOrder, shoppingCartJson, deliveryOrderItemListToSave, deliveryOrder);
    }

    deliveryOrder.setOfferPrice(deliveryOrder.getOfferPrice() + orderJSON.getGlobalDiscount());
    deliveryOrder.setWeight(orderJSON.getWeight());
    deliveryOrder.setLowerRangeWeight(orderJSON.getLowerRangeWeight());
    deliveryOrder.setTopRangeWeight(orderJSON.getTopRangeWeight());
    deliveryOrder.setDeliveryPrice(orderJSON.getDeliveryValue());
    deliveryOrder.setRegisteredOffer(orderJSON.getRegisteredDiscount());
    deliveryOrder.setTotalPrice(deliveryOrder.getSubTotalPrice() - deliveryOrder.getOfferPrice() + (orderJSON.getDeliveryValue() + orderJSON.getProviderDeliveryValue()));
    // Campos nuevos proveedores
    deliveryOrder.setProviderDeliveryPrice(orderJSON.getProviderDeliveryValue());
    deliveryOrder.setTotalDelivery(orderJSON.getDeliveryValue() + orderJSON.getProviderDeliveryValue());
    deliveryOrder.setQuantityProviders(0);
    deliveryOrder.setQuantityFarmatodo(0);
    deliveryOrder.setQuantityFarmatodo(deliveryOrder.getQuantityFarmatodo() > 0 ? deliveryOrder.getQuantityFarmatodo() - deliveryOrder.getQuantityProviders() : deliveryOrder.getQuantityFarmatodo());
    deliveryOrder.setTotalQuantity(deliveryOrder.getQuantityFarmatodo() + deliveryOrder.getQuantityProviders());
    deliveryOrder.setDeliveryType(shoppingCartJson.getDeliveryType());
    deliveryOrder.setItemList(deliveryOrderItemListToSave);
    return deliveryOrder;
  }*/

/*
  private void addDeliveryItemOrder15(final ItemAlgolia itemOrder,
                                      final ShoppingCartJson shoppingCartJson,
                                      final List<DeliveryOrderItem> deliveryOrderItemListToSave,
                                      DeliveryOrder deliveryOrder) throws BadRequestException, IOException {
    if (itemOrder.getPrice() >= 0) {
      log.warning("method: addDeliveryItemOrder15");
      String url = URLConnections.URL_ITEM_ID + "/" + itemOrder.getItem();
      JSONObject jsonObject = CoreConnection.getRequest(url, JSONObject.class);
      if (jsonObject == null)
        throw new BadRequestException("ITEM NO ENCONTRADO");

      Item item = new Item();
      if (jsonObject.get("anywaySelling") != null)
        item.setAnywaySelling((Boolean) jsonObject.get("anywaySelling"));
      if (jsonObject.get("barcode") != null)
        item.setBarcode((String) jsonObject.get("barcode"));
      if (jsonObject.get("brand") != null)
        item.setBrand((String) jsonObject.get("brand"));
      if (jsonObject.get("id") != null)
        item.setId((int) jsonObject.get("id"));
      if (jsonObject.get("isGeneric") != null)
        item.setIsGeneric((Boolean) jsonObject.get("isGeneric"));
      if (jsonObject.get("firstDescription") != null)
        item.setMediaDescription((String) jsonObject.get("firstDescription"));
      if (jsonObject.get("secondDescription") != null)
        item.setGrayDescription((String) jsonObject.get("secondDescription"));
      if (jsonObject.get("starProduct") != null)
        item.setHighlight((Boolean) jsonObject.get("starProduct"));
      if (jsonObject.get("mediaImageUrl") != null)
        item.setMediaImageUrl((String) jsonObject.get("mediaImageUrl"));
      item.setIdStoreGroup(shoppingCartJson.getIdStoreGroup());

      DeliveryOrderItem deliveryOrderItem = new DeliveryOrderItem();
      deliveryOrderItem.setCreateDate(new Date());
      deliveryOrderItem.setQuantitySold(itemOrder.getQuantityRequested());
      if (itemOrder.getPrice() != itemOrder.getDiscount())
        deliveryOrderItem.setFullPrice((double) itemOrder.getPrice());
      else
        deliveryOrderItem.setFullPrice((double) 0);
      if (itemOrder.getDiscount() != 0)
        deliveryOrderItem.setOfferPrice((double) (itemOrder.getPrice() - (itemOrder.getDiscount() / itemOrder.getQuantityRequested())));
      else
        deliveryOrderItem.setOfferPrice((double) 0);
      deliveryOrderItem.setChangeQuantity(itemOrder.getAccess());
      deliveryOrderItem = this.deliveryOrderItemReturn(deliveryOrderItem, item);
      deliveryOrderItemListToSave.add(deliveryOrderItem);
      deliveryOrder.setSubTotalPrice(deliveryOrder.getSubTotalPrice()+itemOrder.getFullPrice());
      deliveryOrder.setOfferPrice(deliveryOrder.getOfferPrice()+itemOrder.getDiscount());

    } else {
      deliveryOrder.setOfferPrice(deliveryOrder.getOfferPrice()+Math.abs(itemOrder.getPrice()));
    }
  }*/

  /**
   * Search ids orders monitor 2.0
   * @param orderRequest
   * @return Json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "searchIdOrders", path = "/orderMonitorEndpoint/searchIdOrders", httpMethod = ApiMethod.HttpMethod.POST)
  public List<JSONObject> searchIdOrders(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getEmployeeNumber() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NUMBER);

    if (orderRequest.getStartDate() == null && orderRequest.getEndDate() == null
            && orderRequest.getDocumentNumberClient() == null && orderRequest.getFilters() == null
            && orderRequest.getFilters().getOrderGuide() == null)
      throw new ConflictException(Constants.ERROR_SEARCH_ORDER_FILTER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    if (orderRequest.getStartDate() != null && orderRequest.getEndDate() != null) {
      customerJson.put("startDate", orderRequest.getStartDate());
      customerJson.put("endDate", orderRequest.getEndDate());
    } else {
      customerJson.put("documentNumberClient", orderRequest.getDocumentNumberClient());
    }

    //Filtros
    JSONObject filtersJson = new JSONObject();

    if (orderRequest.getFilters() != null) {
      FilterOrder filters = orderRequest.getFilters();

      //Filtro de tiendas
      if (filters.getStores() != null && filters.getStores().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (StoreFilter filter: filters.getStores()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("storeId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("stores", jsonArray);
      }

      //Filtro de ciudades
      if (filters.getCities() != null && filters.getCities().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (CityFilter filter: filters.getCities()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("cityId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("cities", jsonArray);
      }

      //Filtro de couries
      if (filters.getCouriers() != null && filters.getCouriers().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (CourierFilter filter: filters.getCouriers()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("courierId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("couriers", jsonArray);
      }

      //Filtro de estados de la orden
      if (filters.getOrderStatusFilters() != null && filters.getOrderStatusFilters().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (OrderStatusFilter filter: filters.getOrderStatusFilters()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("orderStatus", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("orderStatus", jsonArray);
      }

      //Filtro de metodos de pago
      if (filters.getPaymentMethods() != null && filters.getPaymentMethods().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (PaymentMethodFilter filter: filters.getPaymentMethods()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("paymentMethodId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("paymentMethods", jsonArray);
      }

      //Filtro de ruta optima
      if (filters.getOptimalRoute() != null && !filters.getOptimalRoute().isEmpty()) {
        filtersJson.put("optimalRoute", filters.getOptimalRoute());
      }

      //Filtro de numero de guia
      if (filters.getOrderGuide() != null && !filters.getOrderGuide().isEmpty()) {
        filtersJson.put("orderGuide", filters.getOrderGuide());
      }

      customerJson.put("filters", filtersJson);
    }

    return CoreConnection.postListRequest(URLConnections.URL_SEARCH_ID_ORDERS, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Search ids orders monitor 1.5
   * @param orderRequest
   * @return Json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "searchIdOrders15", path = "/orderMonitorEndpoint/searchIdOrders15", httpMethod = ApiMethod.HttpMethod.POST)
  public List<JSONObject> searchIdOrders15(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getEmployeeNumber() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NUMBER);

    if (orderRequest.getStartDate() == null && orderRequest.getEndDate() == null && orderRequest.getDocumentNumberClient() == null)
      throw new ConflictException(Constants.ERROR_SEARCH_ORDER_FILTER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    if (orderRequest.getStartDate() != null && orderRequest.getEndDate() != null) {
      customerJson.put("startDate", orderRequest.getStartDate());
      customerJson.put("endDate", orderRequest.getEndDate());
    } else {
      customerJson.put("documentNumberClient", orderRequest.getDocumentNumberClient());
    }

    //Filtros
    JSONObject filtersJson = new JSONObject();

    if (orderRequest.getFilters() != null) {
      FilterOrder filters = orderRequest.getFilters();

      //Filtro de tiendas
      if (filters.getStores() != null && filters.getStores().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (StoreFilter filter: filters.getStores()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("storeId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("stores", jsonArray);
      }

      //Filtro de ciudades
      if (filters.getCities() != null && filters.getCities().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (CityFilter filter: filters.getCities()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("cityId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("cities", jsonArray);
      }

      //Filtro de couries
      if (filters.getCouriers() != null && filters.getCouriers().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (CourierFilter filter: filters.getCouriers()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("courierId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("couriers", jsonArray);
      }

      //Filtro de estados de la orden
      if (filters.getOrderStatusFilters() != null && filters.getOrderStatusFilters().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (OrderStatusFilter filter: filters.getOrderStatusFilters()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("orderStatus", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("orderStatus", jsonArray);
      }

      //Filtro de metodos de pago
      if (filters.getPaymentMethods() != null && filters.getPaymentMethods().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (PaymentMethodFilter filter: filters.getPaymentMethods()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("paymentMethodId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("paymentMethods", jsonArray);
      }

      //Filtro de ruta optima
      if (filters.getOptimalRoute() != null && !filters.getOptimalRoute().isEmpty()) {
        filtersJson.put("optimalRoute", filters.getOptimalRoute());
      }

      customerJson.put("filters", filtersJson);
    }

    return CoreConnection.postListRequest(URLConnections.URL_SEARCH_ID_ORDERS_1_5, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Get ids orders 2.0
   * @param orderRequest
   * @return Json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getIdOrders", path = "/orderMonitorEndpoint/getIdOrders", httpMethod = ApiMethod.HttpMethod.POST)
  public List<JSONObject> getIdOrders(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getEmployeeNumber() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NUMBER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    //Filtros
    JSONObject filtersJson = new JSONObject();

    if (orderRequest.getFilters() != null) {
      FilterOrder filters = orderRequest.getFilters();

      //Filtro de tiendas
      if (filters.getStores() != null && filters.getStores().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (StoreFilter filter: filters.getStores()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("storeId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("stores", jsonArray);
      }

      //Filtro de ciudades
      if (filters.getCities() != null && filters.getCities().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (CityFilter filter: filters.getCities()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("cityId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("cities", jsonArray);
      }

      //Filtro de couries
      if (filters.getCouriers() != null && filters.getCouriers().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (CourierFilter filter: filters.getCouriers()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("courierId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("couriers", jsonArray);
      }

      //Filtro de estados de la orden
      if (filters.getOrderStatusFilters() != null && filters.getOrderStatusFilters().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (OrderStatusFilter filter: filters.getOrderStatusFilters()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("orderStatus", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("orderStatus", jsonArray);
      }

      //Filtro de metodos de pago
      if (filters.getPaymentMethods() != null && filters.getPaymentMethods().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (PaymentMethodFilter filter: filters.getPaymentMethods()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("paymentMethodId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("paymentMethods", jsonArray);
      }

      //Filtro de ruta optima
      if (filters.getOptimalRoute() != null && !filters.getOptimalRoute().isEmpty()) {
        filtersJson.put("optimalRoute", filters.getOptimalRoute());
      }

      customerJson.put("filters", filtersJson);
    }

    return CoreConnection.postListRequest(URLConnections.URL_GET_ID_ORDERS, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Get ids orders 1.5
   * @param orderRequest
   * @return Json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getIdOrders15", path = "/orderMonitorEndpoint/getIdOrders15", httpMethod = ApiMethod.HttpMethod.POST)
  public List<JSONObject> getIdOrders15(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getEmployeeNumber() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NUMBER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    //Filtros
    JSONObject filtersJson = new JSONObject();

    if (orderRequest.getFilters() != null) {
      FilterOrder filters = orderRequest.getFilters();

      //Filtro de tiendas
      if (filters.getStores() != null && filters.getStores().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (StoreFilter filter: filters.getStores()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("storeId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("stores", jsonArray);
      }

      //Filtro de ciudades
      if (filters.getCities() != null && filters.getCities().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (CityFilter filter: filters.getCities()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("cityId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("cities", jsonArray);
      }

      //Filtro de couries
      if (filters.getCouriers() != null && filters.getCouriers().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (CourierFilter filter: filters.getCouriers()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("courierId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("couriers", jsonArray);
      }

      //Filtro de estados de la orden
      if (filters.getOrderStatusFilters() != null && filters.getOrderStatusFilters().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (OrderStatusFilter filter: filters.getOrderStatusFilters()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("orderStatus", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("orderStatus", jsonArray);
      }

      //Filtro de metodos de pago
      if (filters.getPaymentMethods() != null && filters.getPaymentMethods().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (PaymentMethodFilter filter: filters.getPaymentMethods()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("paymentMethodId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("paymentMethods", jsonArray);
      }

      //Filtro de ruta optima
      if (filters.getOptimalRoute() != null && !filters.getOptimalRoute().isEmpty()) {
        filtersJson.put("optimalRoute", filters.getOptimalRoute());
      }

      customerJson.put("filters", filtersJson);
    }

    return CoreConnection.postListRequest(URLConnections.URL_GET_ID_ORDERS_1_5, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Get ids orders programmed
   * @param orderRequest
   * @return Json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getIdOrdersProgrammed", path = "/orderMonitorEndpoint/getIdOrdersProgrammed", httpMethod = ApiMethod.HttpMethod.POST)
  public List<JSONObject> getIdOrdersProgrammed(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getEmployeeNumber() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NUMBER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    return CoreConnection.postListRequest(URLConnections.URL_GET_ID_ORDERS_PROGRAMMED, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Get ids orders provide
   * @param orderRequest
   * @return Json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getIdOrdersProvider", path = "/orderMonitorEndpoint/getIdOrdersProvider", httpMethod = ApiMethod.HttpMethod.POST)
  public List<JSONObject> getIdOrdersProvider(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getEmployeeNumber() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NUMBER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    return CoreConnection.postListRequest(URLConnections.URL_GET_ID_ORDERS_PROVIDE, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Repush order
   * @param orderRequest
   * @return json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "repushOrder", path = "/orderMonitorEndpoint/repushOrder", httpMethod = ApiMethod.HttpMethod.POST)
  public JSONObject repushOrder(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getOrderId() == null)
      throw new ConflictException(Constants.ERROR_ID_ORDER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("orderId", orderRequest.getOrderId());
    customerJson.put("courierId", 0);

    if (orderRequest.getEmployeeNumber() != null && !orderRequest.getEmployeeNumber().isEmpty())
      customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    return CoreConnection.postRequest(URLConnections.URL_REPUSH_ORDER, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Get bill order
   * @param orderRequest
   * @return List bill
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getOrderTicketStore", path = "/orderMonitorEndpoint/getOrderTicketStore", httpMethod = ApiMethod.HttpMethod.POST)
  public OrderBillResponse getOrderTicketStore(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getOrderId() == null)
      throw new ConflictException(Constants.ERROR_ID_ORDER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("orderId", orderRequest.getOrderId());

    if(orderRequest.getDeliveryType() != null && !orderRequest.getDeliveryType().isEmpty()) {
      customerJson.put("deliveryType", orderRequest.getDeliveryType());
    }

    List<OrderTicketStore> orderTicketStores = CoreConnection.postListRequest(URLConnections.URL_GET_ORDER_TICKET_STORE, customerJson.toJSONString(), OrderTicketStore.class);

    OrderBillResponse orderBillResponse = new OrderBillResponse();

    if (orderTicketStores == null || orderTicketStores.size() == 0) {
      orderBillResponse.setCode("201");
      return orderBillResponse;
    }

    Boolean errorBill = false;
    for (OrderTicketStore orderTicketStore : orderTicketStores) {
      if (orderTicketStore.getTicket() == null || orderTicketStore.getTicket() <= 0) {
        errorBill = true;
        break;
      }
    }

    if (errorBill)
      orderBillResponse.setCode("201");
    else {
      orderBillResponse.setCode("200");
      orderBillResponse.setOrderTicketStores(orderTicketStores);
    }

    return orderBillResponse;
  }

  /**
   * Get order by id
   * @param orderId
   * @return json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   * @throws WriterException
   */
  @ApiMethod(name = "getOrder", path = "/orderMonitorEndpoint/getOrder", httpMethod = ApiMethod.HttpMethod.GET)
  public ReadOrderResponse getOrder(@Named("orderId") final Long orderId) throws IOException, ConflictException{
      try {
          if (orderId <= 0)
              throw new ConflictException(Constants.ERROR_ID_ORDER);

          return ApiGatewayService.get().getReadOrder(orderId).getData();
      }catch (Exception e){
          throw new  ConflictException(e.getMessage(), e.getCause());
      }
  }

  /**
   * Search orders providers by filters
   * @param orderRequest
   * @return List orders
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "searchOrdersProvider", path = "/orderMonitorEndpoint/searchOrdersProvider", httpMethod = ApiMethod.HttpMethod.POST)
  public List<JSONObject> searchOrdersProvider(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getEmployeeNumber() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NUMBER);

    if (orderRequest.getStartDate() == null && orderRequest.getEndDate() == null
            && orderRequest.getDocumentNumberClient() == null
            && (orderRequest.getOrders() == null || orderRequest.getOrders().size() == 0)
            && orderRequest.getOrderId() == null)
      throw new ConflictException(Constants.ERROR_SEARCH_ORDER_FILTER);


    JSONObject customerJson = new JSONObject();
    customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    if (orderRequest.getStartDate() != null && orderRequest.getEndDate() != null) {
      customerJson.put("startDate", orderRequest.getStartDate());
      customerJson.put("endDate", orderRequest.getEndDate());
    }

    if (orderRequest.getDocumentNumberClient() != null){
      customerJson.put("documentNumberClient", orderRequest.getDocumentNumberClient());
    }

    if (orderRequest.getOrderId() != null){
      customerJson.put("orderId", orderRequest.getOrderId());
    }

    if (orderRequest.getOrders() != null && orderRequest.getOrders().size() > 0){
      JSONArray orderArray = new JSONArray();

      for (Order order: orderRequest.getOrders()) {
        if (order.getOrderId() != null) {
          JSONObject orderJson = new JSONObject();
          orderJson.put("orderId", order.getOrderId());
          orderArray.add(orderJson);
        }
      }

      customerJson.put("orders", orderArray);
    }

    return CoreConnection.postListRequest(URLConnections.URL_SEARCH_ORDERS_PROVIDERS, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Get order status payu by id
   * @param orderRequest
   * @return json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   * @throws WriterException
   */
  @ApiMethod(name = "getOrderPayUResponse", path = "/orderMonitorEndpoint/getOrderPayUResponse", httpMethod = ApiMethod.HttpMethod.POST)
  public JSONObject getOrderPayUResponse(final OrderRequest orderRequest)
          throws ConflictException, BadRequestException, IOException, InternalServerErrorException, WriterException {
    JSONObject orderJson = new JSONObject();
    orderJson.put("orderId",orderRequest.getOrderId());
    return CoreConnection.postRequest(URLConnections.URL_GET_ORDER_PAYU_RESPONSE, orderJson.toJSONString(), JSONObject.class);
  }

  /**
   * Manager orders
   * @param orderRequest
   * @return json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   * @throws WriterException
   */
  @ApiMethod(name = "manageOrder", path = "/orderMonitorEndpoint/manageOrder", httpMethod = ApiMethod.HttpMethod.POST)
  public JSONObject manageOrder(final OrderRequest orderRequest)
          throws ConflictException, BadRequestException, IOException, InternalServerErrorException, WriterException {
    if (orderRequest == null || orderRequest.getOrderId() == null)
      throw new ConflictException(Constants.ERROR_ID_ORDER);

    if (orderRequest == null || orderRequest.getRol() == null)
      throw new ConflictException(Constants.ERROR_USER_ROL);

    if (orderRequest == null || orderRequest.getCorreoUsuario() == null)
      throw new ConflictException(Constants.ERROR_USER_EMAIL);

    JSONObject customerJson = new JSONObject();
    customerJson.put("orderId", orderRequest.getOrderId());
    customerJson.put("observation", orderRequest.getObservation());
    customerJson.put("rol", orderRequest.getRol());
    customerJson.put("correoUsuario", orderRequest.getCorreoUsuario());

    if (orderRequest.getEmployeeNumber() != null && !orderRequest.getEmployeeNumber().isEmpty())
      customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    return CoreConnection.postRequest(URLConnections.URL_MANAGER_ORDER, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Manager orders 1.5
   * @param orderRequest
   * @return json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   * @throws WriterException
   */
  @ApiMethod(name = "manageOrder15", path = "/orderMonitorEndpoint/manageOrder15", httpMethod = ApiMethod.HttpMethod.POST)
  public JSONObject manageOrder15(final OrderRequest orderRequest)
          throws ConflictException, BadRequestException, IOException, InternalServerErrorException, WriterException {
    if (orderRequest == null || orderRequest.getOrderId() == null)
      throw new ConflictException(Constants.ERROR_ID_ORDER);

    if (orderRequest == null || orderRequest.getRol() == null)
      throw new ConflictException(Constants.ERROR_USER_ROL);

    if (orderRequest == null || orderRequest.getCorreoUsuario() == null)
      throw new ConflictException(Constants.ERROR_USER_EMAIL);

    JSONObject customerJson = new JSONObject();
    customerJson.put("orderId", orderRequest.getOrderId());
    customerJson.put("observation", orderRequest.getObservation());
    customerJson.put("rol", orderRequest.getRol());
    customerJson.put("correoUsuario", orderRequest.getCorreoUsuario());

    if (orderRequest.getEmployeeNumber() != null && !orderRequest.getEmployeeNumber().isEmpty())
      customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    return CoreConnection.postRequest(URLConnections.URL_MANAGER_ORDER_1_5, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Get id orders managers 15
   * @param orderRequest
   * @return
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getIdOrdersGestionadas15", path = "/orderMonitorEndpoint/getIdOrdersGestionadas15", httpMethod = ApiMethod.HttpMethod.POST)
  public List<JSONObject> getIdOrdersGestionadas15(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getEmployeeNumber() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NUMBER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    return CoreConnection.postListRequest(URLConnections.URL_GET_ID_ORDERS_1_5_MANAGERS, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Get id orders managers
   * @param orderRequest
   * @return
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getIdOrdersGestionadas", path = "/orderMonitorEndpoint/getIdOrdersGestionadas", httpMethod = ApiMethod.HttpMethod.POST)
  public List<JSONObject> getIdOrdersGestionadas(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getEmployeeNumber() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NUMBER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    return CoreConnection.postListRequest(URLConnections.URL_GET_ID_ORDERS_MANAGERS, customerJson.toJSONString(), JSONObject.class);
  }


  /**
   * Get id orders billed
   * @param orderRequest
   * @return
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getIdOrdersFacturadas", path = "/orderMonitorEndpoint/getIdOrdersFacturadas", httpMethod = ApiMethod.HttpMethod.POST)
  public List<JSONObject> getIdOrdersFacturadas(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getEmployeeNumber() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NUMBER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    return CoreConnection.postListRequest(URLConnections.URL_GET_ID_ORDERS_BILLED, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Order sumary
   * @param orderRequest
   * @return
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "manageOrderSummary", path = "/orderMonitorEndpoint/manageOrderSummary", httpMethod = ApiMethod.HttpMethod.POST)
  public JSONObject manageOrderSummary(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getOrderId() == null)
      throw new ConflictException(Constants.ERROR_ID_ORDER);

    if (orderRequest == null || orderRequest.getEmployeeNumber() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NUMBER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("orderId", orderRequest.getOrderId());
    customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    return CoreConnection.postRequest(URLConnections.URL_MANAGER_ORDER_SUMARY, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Set notification creation new order
   * @param orderId
   * @param deliveryType
   * @param store
   * @return Answer
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "notifyOrder", path = "/orderMonitorEndpoint/notifyOrder", httpMethod = ApiMethod.HttpMethod.GET)
  public Answer notifyOrder(
          @Named("orderId") final String orderId,
          @Named("deliveryType") final String deliveryType,
          @Named("store") final String store,
          @Named("orderType") final String orderType) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    log.info("Order: " + orderId);
    log.info("DeliveryType: " + deliveryType);
    log.info("Store: " + store);
    log.info("OrderType: " + orderType);

    if (orderId == null || orderId.isEmpty())
      throw new ConflictException(Constants.ERROR_ID_ORDER);

    if (deliveryType == null || deliveryType.isEmpty())
      throw new ConflictException(Constants.ERROR_DELIVERY_TYPE);

    if (store == null || store.isEmpty())
      throw new ConflictException(Constants.ERROR_STORE_ID);

    String url = URLConnections.URL_PUSH_NOTIFICATION_NEW_ORDER + store + "/" + orderId + "/" + deliveryType;

    CoreConnection.getRequest(url, Void.class);

    if(orderType != null && !orderType.isEmpty() && orderType.equals("1.5")) {
      JSONObject customerJson = new JSONObject();
      customerJson.put("orderId", orderId);
      customerJson.put("deliveryType", deliveryType);
      customerJson.put("orderType", "1.5");
      CoreConnection.postRequest(URLConnections.URL_ADD_ORDER_SUMMARY, customerJson.toJSONString(), Void.class);
    }

    Answer answer = new Answer();
    answer.setConfirmation(true);

    return answer;
  }

  /**
   * Get order summary by type
   * @param deliveryType
   * @return
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getOrdersSummaryByType", path = "/orderMonitorEndpoint/getOrdersSummaryByType", httpMethod = ApiMethod.HttpMethod.GET)
  public JSONObject getOrdersSummaryByType(
          @Named("deliveryType") final String deliveryType,
          @Named("orderType") final String orderType) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    JSONObject requestJson = new JSONObject();
    requestJson.put("deliveryType", deliveryType);
    requestJson.put("orderType", orderType);

    return CoreConnection.postRequest(URLConnections.URL_GET_ORDERS_SUMARY_BY_TYPE, requestJson.toJSONString(), JSONObject.class);
  }

  /**
   * Reassign order manual 2.0
   * @param orderRequest
   * @return json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "reassignOrderManual", path = "/orderMonitorEndpoint/reassignOrderManual", httpMethod = ApiMethod.HttpMethod.POST)
  public JSONObject reassignOrderManual(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getOrderId() == null)
      throw new ConflictException(Constants.ERROR_ID_ORDER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("orderId", orderRequest.getOrderId());
    customerJson.put("courierId", orderRequest.getCourierId());

    if (orderRequest.getEmployeeNumber() != null && !orderRequest.getEmployeeNumber().isEmpty())
      customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    return CoreConnection.postRequest(URLConnections.URL_REASSIGN_ORDER_MANUAL, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Reassign order manual 1.5
   * @param orderRequest
   * @return json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "reassignOrderManual15", path = "/orderMonitorEndpoint/reassignOrderManual15", httpMethod = ApiMethod.HttpMethod.POST)
  public JSONObject reassignOrderManual15(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getOrderId() == null)
      throw new ConflictException(Constants.ERROR_ID_ORDER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("orderId", orderRequest.getOrderId());
    customerJson.put("courierId", orderRequest.getCourierId());

    if (orderRequest.getEmployeeNumber() != null && !orderRequest.getEmployeeNumber().isEmpty())
      customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    return CoreConnection.postRequest(URLConnections.URL_REASSIGN_ORDER_MANUAL_1_5, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Finalizar orden 2.0
   * @param orderRequest
   * @return
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "finalizeOrder", path = "/orderMonitorEndpoint/finalizeOrder", httpMethod = ApiMethod.HttpMethod.POST)
  public JSONObject finalizeOrder(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getOrderId() == null)
      throw new ConflictException(Constants.ERROR_ID_ORDER);

    if (orderRequest == null || orderRequest.getRol() == null)
      throw new ConflictException(Constants.ERROR_USER_ROL);

    if (orderRequest == null || orderRequest.getCorreoUsuario() == null)
      throw new ConflictException(Constants.ERROR_USER_EMAIL);

    JSONObject customerJson = new JSONObject();
    customerJson.put("orderId", orderRequest.getOrderId());
    customerJson.put("observation", orderRequest.getObservation());
    customerJson.put("rol", orderRequest.getRol());
    customerJson.put("correoUsuario", orderRequest.getCorreoUsuario());

    if (orderRequest.getEmployeeNumber() != null && !orderRequest.getEmployeeNumber().isEmpty())
      customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    return CoreConnection.postRequest(URLConnections.URL_FINALIZE_ORDER, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Finalizar orden 1.5
   * @param orderRequest
   * @return
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "finalizeOrder15", path = "/orderMonitorEndpoint/finalizeOrder15", httpMethod = ApiMethod.HttpMethod.POST)
  public JSONObject finalizeOrder15(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getOrderId() == null)
      throw new ConflictException(Constants.ERROR_ID_ORDER);

    if (orderRequest == null || orderRequest.getRol() == null)
      throw new ConflictException(Constants.ERROR_USER_ROL);

    if (orderRequest == null || orderRequest.getCorreoUsuario() == null)
      throw new ConflictException(Constants.ERROR_USER_EMAIL);

    JSONObject customerJson = new JSONObject();
    customerJson.put("orderId", orderRequest.getOrderId());
    customerJson.put("observation", orderRequest.getObservation());
    customerJson.put("rol", orderRequest.getRol());
    customerJson.put("correoUsuario", orderRequest.getCorreoUsuario());

    if (orderRequest.getEmployeeNumber() != null && !orderRequest.getEmployeeNumber().isEmpty())
      customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    return CoreConnection.postRequest(URLConnections.URL_FINALIZE_ORDER_1_5, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Get payments methods
   * @return json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getPaymentMethods", path = "/orderMonitorEndpoint/getPaymentMethods", httpMethod = ApiMethod.HttpMethod.GET)
  public List<PaymentMethodRes> getPaymentMethods() throws ConflictException, BadRequestException, IOException, InternalServerErrorException {
    log.info("URL_PAYMENT_METHODS:"+URLConnections.URL_PAYMENT_METHODS_ACTIVE);
    return ApiGatewayService.get().getPaymentMethodActive();

  }

  /**
   * Get count resumen orders 2.0getPaymentMethods
   * @return
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getCountResumenOrders20", path = "/orderMonitorEndpoint/getCountResumenOrders20", httpMethod = ApiMethod.HttpMethod.GET)
  public JSONObject getCountResumenOrders20() throws ConflictException, BadRequestException, IOException, InternalServerErrorException {
    return CoreConnection.getRequest(URLConnections.URL_COUNT_RESUME_ORDERS_2_0, JSONObject.class);
  }

  /**
   * Obtener tracking de order
   * @param idOrder
   * @return
   * @throws IOException
   * @throws ConflictException
   * @throws BadRequestException
   *
   * return tracking info by order
   */
  @ApiMethod(name = "getTrackingOrder", path = "/orderMonitorEndpoint/getTrackingOrder", httpMethod = ApiMethod.HttpMethod.GET)
  public OrderInfoStatus getTrackingOrder(@Named("idOrder") final String idOrder) throws IOException, ConflictException, BadRequestException {

    log.warning("method: getTrackingOrder: "+idOrder);

    //Optional<OrderInfoStatus> optTracing = Optional.ofNullable(CoreService.get().getOrderInfoTracing(idOrder));
    Optional<OrderInfoStatus> optTracing = Optional.ofNullable(ApiGatewayService.get().getOrderInfoTracingBck3(Long.parseLong(idOrder)).getData());
    if (!optTracing.isPresent()){
      throw new BadRequestException(HttpStatusCode.BAD_REQUEST.getStatusName());
    }

    // validate is active Algolia
    Integer isActive = Objects.requireNonNull(APIAlgolia.getActiveCourierTrackingSocket()).
            getActiveCouriers()
            .stream()
            .filter(courier -> optTracing.get().getCourierId() == courier)
            .findAny()
            .orElse(null);
    // Consulta Url en Algolia
    WebSocketProperties httpsWebSocketProperties = APIAlgolia.getHttpsWebSocketUrl();
    optTracing.get().setHttpsWebSocketUrl(Objects.nonNull(httpsWebSocketProperties) && Objects.nonNull(httpsWebSocketProperties.getUrl()) && !httpsWebSocketProperties.getUrl().isEmpty() ? httpsWebSocketProperties.getUrl() : null);
    WebSocketProperties httpWebSocketProperties = APIAlgolia.getHttpWebSocketUrl();
    optTracing.get().setHttpWebSocketUrl(Objects.nonNull(httpWebSocketProperties) && Objects.nonNull(httpWebSocketProperties.getUrl()) && !httpWebSocketProperties.getUrl().isEmpty() ? httpWebSocketProperties.getUrl() : null);

    log.warning("method: getTrackingOrder httpsUrlWebSocket: "+ optTracing.get().getHttpsWebSocketUrl());
    log.warning("method: getTrackingOrder httpUrlWebSocket: "+ optTracing.get().getHttpWebSocketUrl());

    //optTracing.get().setStatusCode(HttpStatusCode.OK.getCode());
    // fix status id
    optTracing.get().setStatusId( (optTracing.get().getStatusId() == 6) ?  5 : optTracing.get().getStatusId() );

    optTracing.get().setActiveSocket(Objects.nonNull(isActive));

    return optTracing.orElseThrow(() -> new ConflictException(HttpStatusCode.NO_CONTENT.getStatusName()));
  }

  /**
   * Obtener tracking de order 1.5
   * @param idOrder
   * @return
   * @throws IOException
   * @throws ConflictException
   * @throws BadRequestException
   *
   * return tracking info by order
   */
  /*
  @ApiMethod(name = "getTrackingOrder15", path = "/orderMonitorEndpoint/getTrackingOrder15", httpMethod = ApiMethod.HttpMethod.GET)
  public OrderInfoStatus getTrackingOrder15(@Named("idOrder") final String idOrder) throws IOException, ConflictException, BadRequestException {

    log.warning("method: getTrackingOrder15: "+idOrder);

    Optional<OrderInfoStatus> optTracing = Optional.ofNullable(CoreService.get().getOrder15InfoTracing(idOrder));
    if (!optTracing.isPresent()){
      throw new BadRequestException(HttpStatusCode.BAD_REQUEST.getStatusName());
    }

    // validate is active Algolia
    Integer isActive = Objects.requireNonNull(APIAlgolia.getActiveCourierTrackingSocket()).
            getActiveCouriers()
            .stream()
            .filter(courier -> optTracing.get().getCourierId() == courier)
            .findAny()
            .orElse(null);
    // Consulta Url en Algolia
    WebSocketProperties httpsWebSocketProperties = APIAlgolia.getHttpsWebSocketUrl();
    optTracing.get().setHttpsWebSocketUrl(Objects.nonNull(httpsWebSocketProperties) && Objects.nonNull(httpsWebSocketProperties.getUrl()) && !httpsWebSocketProperties.getUrl().isEmpty() ? httpsWebSocketProperties.getUrl() : null);
    WebSocketProperties httpWebSocketProperties = APIAlgolia.getHttpWebSocketUrl();
    optTracing.get().setHttpWebSocketUrl(Objects.nonNull(httpWebSocketProperties) && Objects.nonNull(httpWebSocketProperties.getUrl()) && !httpWebSocketProperties.getUrl().isEmpty() ? httpWebSocketProperties.getUrl() : null);

    log.warning("method: getTrackingOrder15 httpsUrlWebSocket: "+ optTracing.get().getHttpsWebSocketUrl());
    log.warning("method: getTrackingOrder15 httpUrlWebSocket: "+ optTracing.get().getHttpWebSocketUrl());

    optTracing.get().setStatusCode(HttpStatusCode.OK.getCode());
    // fix status id
    optTracing.get().setStatusId( (optTracing.get().getStatusId() == 6) ?  5 : optTracing.get().getStatusId() );

    optTracing.get().setActiveSocket(Objects.nonNull(isActive));

    return optTracing.orElseThrow(() -> new ConflictException(HttpStatusCode.NO_CONTENT.getStatusName()));
  }
  */


  /**
   * Get ids orders nationals y envialo ya
   * @param orderRequest
   * @return Json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getIdOrdersNoExpress20", path = "/orderMonitorEndpoint/getIdOrdersNoExpress20", httpMethod = ApiMethod.HttpMethod.POST)
  public List<JSONObject> getIdOrdersNoExpress20(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getEmployeeNumber() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NUMBER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    //Filtros
    JSONObject filtersJson = new JSONObject();

    if (orderRequest.getFilters() != null) {
      FilterOrder filters = orderRequest.getFilters();

      //Filtro de tiendas
      if (filters.getStores() != null && filters.getStores().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (StoreFilter filter: filters.getStores()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("storeId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("stores", jsonArray);
      }

      //Filtro de ciudades
      if (filters.getCities() != null && filters.getCities().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (CityFilter filter: filters.getCities()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("cityId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("cities", jsonArray);
      }

      //Filtro de couries
      if (filters.getCouriers() != null && filters.getCouriers().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (CourierFilter filter: filters.getCouriers()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("courierId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("couriers", jsonArray);
      }

      //Filtro de estados de la orden
      if (filters.getOrderStatusFilters() != null && filters.getOrderStatusFilters().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (OrderStatusFilter filter: filters.getOrderStatusFilters()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("orderStatus", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("orderStatus", jsonArray);
      }

      //Filtro de metodos de pago
      if (filters.getPaymentMethods() != null && filters.getPaymentMethods().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (PaymentMethodFilter filter: filters.getPaymentMethods()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("paymentMethodId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("paymentMethods", jsonArray);
      }

      //Filtro de ruta optima
      if (filters.getOptimalRoute() != null && !filters.getOptimalRoute().isEmpty()) {
        filtersJson.put("optimalRoute", filters.getOptimalRoute());
      }

      //Filtro de numero de guia
      if (filters.getOrderGuide() != null && !filters.getOrderGuide().isEmpty()) {
        filtersJson.put("orderGuide", filters.getOrderGuide());
      }

      customerJson.put("filters", filtersJson);
    }

    return CoreConnection.postListRequest(URLConnections.URL_GET_ID_ORDERS_NO_EXPRESS, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Get ids orders suscribe and save
   * @param orderRequest
   * @return Json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getIdOrdersSubscribeAndSave", path = "/orderMonitorEndpoint/getIdOrdersSubscribeAndSave", httpMethod = ApiMethod.HttpMethod.POST)
  public List<JSONObject> getIdOrdersSubscribeAndSave(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getEmployeeNumber() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NUMBER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    return CoreConnection.postListRequest(URLConnections.URL_GET_ID_ORDERS_SS, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Get orders 2.0 assigned manual
   * @param orderRequest
   * @return List orders
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getOrdersAssignedManual20", path = "/orderMonitorEndpoint/getOrdersAssignedManual20", httpMethod = ApiMethod.HttpMethod.POST)
  public List<OrderReport> getOrdersAssignedManual20(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getEmployeeNumber() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NUMBER);

    if (orderRequest.getStartDate() == null && orderRequest.getEndDate() == null)
      throw new ConflictException(Constants.ERROR_SEARCH_ORDER_FILTER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    if (orderRequest.getStartDate() != null && orderRequest.getEndDate() != null) {
      customerJson.put("startDate", orderRequest.getStartDate());
      customerJson.put("endDate", orderRequest.getEndDate());
    }

    List<OrderResponse> ordersReponse = CoreConnection.postListRequest(URLConnections.URL_GET_ORDERS_ASSIGNED_MANUAL, customerJson.toJSONString(), OrderResponse.class);

    log.info("Response: " + ordersReponse.toString());

    List<OrderReport> orders = new ArrayList<>();
    DecimalFormat df2 = new DecimalFormat(".##");

    if (ordersReponse != null && ordersReponse.size() > 0) {
      List<OrderResponse> ordersCreate = ordersReponse.stream()
              .filter(x -> x.getStatusName().trim().equals("RECIBIDA")).collect(Collectors.toList());
      List<OrderResponse> ordersAssigned = ordersReponse.stream()
              .filter(x -> x.getStatusName().trim().equals("REASIGNADA MANUAL")).collect(Collectors.toList());
      ordersCreate.stream().forEach(x -> {
        Optional<OrderResponse> orderOption = ordersAssigned.stream().filter(y -> y.getOrderId().equals(x.getOrderId())).findFirst();
        if (orderOption != null && orderOption.isPresent()) {
          OrderResponse order = orderOption.get();
          OrderReport report = new OrderReport();
          Date startDate = java.sql.Timestamp.valueOf(x.getCreateDate());
          Date endDate = java.sql.Timestamp.valueOf(order.getCreateDate());
          report.setOrderId(x.getOrderId());
          report.setStatusName(order.getStatusName());
          report.setAssignedDate(endDate);
          double minutes = ((endDate.getTime() - startDate.getTime()) / (1000.0 * 60)) % 60;
          report.setMinutes(Double.parseDouble(df2.format(minutes)));
          orders.add(report);
        }
      });
    }

    return orders;
  }

  /**
   * Get orders 1.5 assigned manual
   * @param orderRequest
   * @return List orders
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getOrdersAssignedManual15", path = "/orderMonitorEndpoint/getOrdersAssignedManual15", httpMethod = ApiMethod.HttpMethod.POST)
  public List<OrderReport> getOrdersAssignedManual15(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getEmployeeNumber() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NUMBER);

    if (orderRequest.getStartDate() == null && orderRequest.getEndDate() == null)
      throw new ConflictException(Constants.ERROR_SEARCH_ORDER_FILTER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    if (orderRequest.getStartDate() != null && orderRequest.getEndDate() != null) {
      customerJson.put("startDate", orderRequest.getStartDate());
      customerJson.put("endDate", orderRequest.getEndDate());
    }

    List<OrderResponse> ordersReponse = CoreConnection.postListRequest(URLConnections.URL_GET_ORDERS_ASSIGNED_MANUAL_15, customerJson.toJSONString(), OrderResponse.class);

    log.info("Response: " + ordersReponse.toString());

    List<OrderReport> orders = new ArrayList<>();
    DecimalFormat df2 = new DecimalFormat(".##");

    if (ordersReponse != null && ordersReponse.size() > 0) {
      List<OrderResponse> ordersCreate = ordersReponse.stream()
              .filter(x -> x.getStatusName().trim().equals("EMITIDO")).collect(Collectors.toList());
      List<OrderResponse> ordersAssigned = ordersReponse.stream()
              .filter(x -> x.getStatusName().trim().equals("REASIGNADA MANUAL")).collect(Collectors.toList());
      ordersCreate.stream().forEach(x -> {
        Optional<OrderResponse> orderOption = ordersAssigned.stream().filter(y -> y.getOrderId().equals(x.getOrderId())).findFirst();
        if (orderOption != null && orderOption.isPresent()) {
          OrderResponse order = orderOption.get();
          OrderReport report = new OrderReport();
          Date startDate = java.sql.Timestamp.valueOf(x.getCreateDate());
          Date endDate = java.sql.Timestamp.valueOf(order.getCreateDate());
          report.setOrderId(x.getOrderId());
          report.setStatusName(order.getStatusName());
          report.setAssignedDate(endDate);
          double minutes = ((endDate.getTime() - startDate.getTime()) / (1000.0 * 60)) % 60;
          report.setMinutes(Double.parseDouble(df2.format(minutes)));
          orders.add(report);
        }
      });
    }

    return orders;
  }

  /**
   * Get order detail no express
   * @param orderRequest
   * @return List order detail
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getOrderDetailNoExpress", path = "/orderMonitorEndpoint/getOrderDetailNoExpress", httpMethod = ApiMethod.HttpMethod.POST)
  public JSONObject getOrderDetailNoExpress(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getOrderId() == null)
      throw new ConflictException(Constants.ERROR_ID_ORDER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("orderId", orderRequest.getOrderId());

    return CoreConnection.postRequest(URLConnections.URL_GET_DETAIL_ORDER_NO_EXPRESS, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Get credit note by order id
   * @param orderId
   * @return json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   * @throws WriterException
   */
  @ApiMethod(name = "getOrderCreditNoteTicket", path = "/orderMonitorEndpoint/getOrderCreditNoteTicket", httpMethod = ApiMethod.HttpMethod.GET)
  public JSONObject getOrderCreditNoteTicket(@Named("orderId") final Long orderId)
          throws ConflictException, BadRequestException, IOException, InternalServerErrorException, WriterException {
    String url = URLConnections.URL_GET_ORDER_CREDIT_NOTE_TICKET + "/" + orderId;
    return CoreConnection.getRequest(url, JSONObject.class);
  }

  /**
   * Modify status orden 2.0
   * @param orderRequest
   * @return
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "modifyOrderStatus20", path = "/orderMonitorEndpoint/modifyOrderStatus20", httpMethod = ApiMethod.HttpMethod.POST)
  public JSONObject modifyOrderStatus20(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {
    log.info("orderResquet" );
    if (orderRequest == null || orderRequest.getOrderId() == null)
      throw new ConflictException(Constants.ERROR_ID_ORDER);

    if (orderRequest == null || orderRequest.getRol() == null)
      throw new ConflictException(Constants.ERROR_USER_ROL);

    if (orderRequest == null || orderRequest.getCorreoUsuario() == null)
      throw new ConflictException(Constants.ERROR_USER_EMAIL);

    if (orderRequest == null || orderRequest.getStatusId() == null)
      throw new ConflictException(Constants.ERROR_STATUS_ORDER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("orderId", orderRequest.getOrderId());
    customerJson.put("observation", orderRequest.getObservation());
    customerJson.put("rol", orderRequest.getRol());
    customerJson.put("correoUsuario", orderRequest.getCorreoUsuario());
    customerJson.put("statusId", orderRequest.getStatusId());
    log.info("data " + customerJson);

    if (orderRequest.getEmployeeNumber() != null && !orderRequest.getEmployeeNumber().isEmpty())
      customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    return CoreConnection.postRequest(URLConnections.URL_MODIFY_STATUS_ORDER, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Update guide and courier orden 2.0
   * @param orderRequest
   * @return
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "addOrderGuide", path = "/orderMonitorEndpoint/addOrderGuide", httpMethod = ApiMethod.HttpMethod.POST)
  public JSONObject addOrderGuide(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getOrderId() == null)
      throw new ConflictException(Constants.ERROR_ID_ORDER);

    if (orderRequest == null || orderRequest.getOrderGuide() == null)
      throw new ConflictException(Constants.ERROR_ORDER_GUIDE);

    JSONObject customerJson = new JSONObject();
    customerJson.put("orderId", orderRequest.getOrderId());
    customerJson.put("orderGuide", orderRequest.getOrderGuide());
    customerJson.put("courierId", orderRequest.getCourierId());
    customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    JSONObject jsonObject = CoreConnection.postRequest(URLConnections.URL_ADD_ORDER_GUIDE, customerJson.toJSONString(), JSONObject.class);

    //Cambio de estado
    JSONObject orderJson = new JSONObject();
    orderJson.put("orderId", orderRequest.getOrderId());
    orderJson.put("observation", orderRequest.getObservation());
    orderJson.put("rol", orderRequest.getRol());
    orderJson.put("correoUsuario", orderRequest.getCorreoUsuario());
    orderJson.put("statusId", orderRequest.getStatusId());
    orderJson.put("employeeNumber", orderRequest.getEmployeeNumber());
    orderJson.put("orderGuide", orderRequest.getOrderGuide());

    CoreConnection.postRequest(URLConnections.URL_MODIFY_STATUS_ORDER, orderJson.toJSONString(), Void.class);

    String courierName = null;
    String tracking = "";
    String trackingEmail = "";

    switch (orderRequest.getCourierId()) {
      case 5:
        courierName = "SERVIENTREGA";
        tracking = " " + Constants.TRACKING_SERVIENTREGA;
        trackingEmail = Constants.TRACKING_SERVIENTREGA;
        break;
      case 11:
        courierName = "LIBERTY";
        tracking = " " + Constants.TRACKING_LIBERTY;
        trackingEmail = Constants.TRACKING_LIBERTY;
        break;
      case 12:
        courierName = "CARGONAM";
        trackingEmail = "";
        break;
    }

    //SEND SMS
    if (orderRequest.getPhoneCustomer() != null && !orderRequest.getPhoneCustomer().isEmpty()
      && orderRequest.getCourierId() > 0 && courierName != null && !courierName.isEmpty()) {
      sendSmsSendOrder(orderRequest.getPhoneCustomer(), orderRequest.getOrderId().toString(), orderRequest.getOrderGuide(), courierName, tracking);
    }

    //SEND EMAIL
    if (courierName != null && !courierName.isEmpty()) {
      JSONObject mailJson = new JSONObject();
      mailJson.put("orderId", orderRequest.getOrderId());
      mailJson.put("orderGuide", orderRequest.getOrderGuide());
      mailJson.put("courier", courierName);
      mailJson.put("urlTracking", trackingEmail);
      mailJson.put("employeeNumber", orderRequest.getEmployeeNumber());
      CoreConnection.postRequest(URLConnections.URL_SEND_MAIL_NO_EXPRESS, mailJson.toJSONString(), Void.class);
    }

    return jsonObject;
  }

  private void sendSmsSendOrder(final String number, final String order,
                                final String guide, final String courierName, final String tracking) throws BadRequestException, IOException {
    String text = Constants.SMS_TEXT_SEND_ORDER.replace(":order", order)
            .replace(":guide", guide).replace(":courier", courierName).replace(":tracking", tracking);
    text = URLEncoder.encode(text, "UTF-8");
    String url = URLConnections.URL_SEND_SMS_HABLAME + "&numero=" + number + "&sms=" + text;
    CoreConnection.getRequest(url, Void.class);
  }

  /**
   * Order insert click whatsapp client
   * @param orderRequest
   * @return
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "insertClickWhatsApp", path = "/orderMonitorEndpoint/insertClickWhatsApp", httpMethod = ApiMethod.HttpMethod.POST)
  public JSONObject insertClickWhatsApp(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getOrderId() == null)
      throw new ConflictException(Constants.ERROR_ID_ORDER);

    if (orderRequest == null || orderRequest.getEmployeeNumber() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NUMBER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("orderId", orderRequest.getOrderId());
    customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    return CoreConnection.postRequest(URLConnections.URL_INSERT_CLICK_WHATSAPP, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Get orders for report
   * @param orderRequest
   * @return
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getReportManagamentMonitor20", path = "/orderMonitorEndpoint/getReportManagamentMonitor20", httpMethod = ApiMethod.HttpMethod.POST)
  public List<JSONObject> getReportManagamentMonitor20(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getEmployeeNumber() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NUMBER);

    if (orderRequest.getStartDate() == null && orderRequest.getEndDate() == null
            && orderRequest.getDocumentNumberClient() == null
            && (orderRequest.getOrders() == null || orderRequest.getOrders().size() == 0)
            && orderRequest.getOrderId() == null)
      throw new ConflictException(Constants.ERROR_SEARCH_ORDER_FILTER);


    JSONObject customerJson = new JSONObject();
    customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    if (orderRequest.getStartDate() != null && orderRequest.getEndDate() != null) {
      customerJson.put("startDate", orderRequest.getStartDate());
      customerJson.put("endDate", orderRequest.getEndDate());
    }

    if (orderRequest.getDocumentNumberClient() != null){
      customerJson.put("documentNumberClient", orderRequest.getDocumentNumberClient());
    }

    if (orderRequest.getOrderId() != null){
      customerJson.put("orderId", orderRequest.getOrderId());
    }

    if (orderRequest.getOrders() != null && orderRequest.getOrders().size() > 0){
      JSONArray orderArray = new JSONArray();

      for (Order order: orderRequest.getOrders()) {
        if (order.getOrderId() != null) {
          JSONObject orderJson = new JSONObject();
          orderJson.put("orderId", order.getOrderId());
          orderArray.add(orderJson);
        }
      }

      customerJson.put("orders", orderArray);
    }

    return CoreConnection.postListRequest(URLConnections.URL_GET_REPORT_MANAGAMENT, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Get orders 1.5 for report
   * @param orderRequest
   * @return
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getReportManagamentMonitor15", path = "/orderMonitorEndpoint/getReportManagamentMonitor15", httpMethod = ApiMethod.HttpMethod.POST)
  public List<JSONObject> getReportManagamentMonitor15(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getEmployeeNumber() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NUMBER);

    if (orderRequest.getStartDate() == null && orderRequest.getEndDate() == null
            && orderRequest.getDocumentNumberClient() == null
            && (orderRequest.getOrders() == null || orderRequest.getOrders().size() == 0)
            && orderRequest.getOrderId() == null)
      throw new ConflictException(Constants.ERROR_SEARCH_ORDER_FILTER);


    JSONObject customerJson = new JSONObject();
    customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    if (orderRequest.getStartDate() != null && orderRequest.getEndDate() != null) {
      customerJson.put("startDate", orderRequest.getStartDate());
      customerJson.put("endDate", orderRequest.getEndDate());
    }

    if (orderRequest.getDocumentNumberClient() != null){
      customerJson.put("documentNumberClient", orderRequest.getDocumentNumberClient());
    }

    if (orderRequest.getOrderId() != null){
      customerJson.put("orderId", orderRequest.getOrderId());
    }

    if (orderRequest.getOrders() != null && orderRequest.getOrders().size() > 0){
      JSONArray orderArray = new JSONArray();

      for (Order order: orderRequest.getOrders()) {
        if (order.getOrderId() != null) {
          JSONObject orderJson = new JSONObject();
          orderJson.put("orderId", order.getOrderId());
          orderArray.add(orderJson);
        }
      }

      customerJson.put("orders", orderArray);
    }

    return CoreConnection.postListRequest(URLConnections.URL_GET_REPORT_MANAGAMENT_15, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Get Dataphone number
   * @param orderRequest
   * @return
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   * @throws WriterException
   */
  @ApiMethod(name = "getPOSNumberOrder", path = "/orderMonitorEndpoint/getPOSNumberOrder", httpMethod = ApiMethod.HttpMethod.POST)
  public JSONObject getPOSNumberOrder(final OrderRequest orderRequest)
          throws ConflictException, BadRequestException, IOException, InternalServerErrorException, WriterException {
    JSONObject orderJson = new JSONObject();
    orderJson.put("orderId",orderRequest.getOrderId());
    return CoreConnection.postRequest(URLConnections.URL_GET_POS_NUMBER_ORDER, orderJson.toJSONString(), JSONObject.class);
  }

  /**
   * Get order pay evidences
   * @param orderRequest
   * @return
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   * @throws WriterException
   */
  @ApiMethod(name = "getOrderEvidences", path = "/orderMonitorEndpoint/getOrderEvidences", httpMethod = ApiMethod.HttpMethod.POST)
  public JSONObject getOrderEvidences(final OrderRequest orderRequest)
          throws ConflictException, BadRequestException, IOException, InternalServerErrorException, WriterException {
    JSONObject orderJson = new JSONObject();
    orderJson.put("orderId",orderRequest.getOrderId());
    orderJson.put("courierId", orderRequest.getCourierId());
    if (orderRequest.getEmployeeNumber() == null || !orderRequest.getEmployeeNumber().isEmpty())
      orderRequest.setEmployeeNumber("0");
    //return CoreConnection.postRequest(URLConnections.URL_GET_ORDER_EVIDENCES, orderJson.toJSONString(), JSONObject.class);
    return null;
  }

  /**
   * Get order 15 pay evidences
   * @param orderRequest
   * @return
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   * @throws WriterException
   */
  @ApiMethod(name = "getOrderEvidences15", path = "/orderMonitorEndpoint/getOrderEvidences15", httpMethod = ApiMethod.HttpMethod.POST)
  public JSONObject getOrderEvidences15(final OrderRequest orderRequest)
          throws ConflictException, BadRequestException, IOException, InternalServerErrorException, WriterException {
    JSONObject orderJson = new JSONObject();
    orderJson.put("orderId",orderRequest.getOrderId());
    orderJson.put("courierId", orderRequest.getCourierId());
    if (orderRequest.getEmployeeNumber() == null || !orderRequest.getEmployeeNumber().isEmpty())
      orderRequest.setEmployeeNumber("0");
    orderJson.put("employeeNumber", orderRequest.getEmployeeNumber());
    //return CoreConnection.postRequest(URLConnections.URL_GET_ORDER_EVIDENCES_15, orderJson.toJSONString(), JSONObject.class);
    return null;
  }

  /**
   * Search orders no express by filters
   * @param orderRequest
   * @return List orders
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "searchOrdersNoExpress", path = "/orderMonitorEndpoint/searchOrdersNoExpress", httpMethod = ApiMethod.HttpMethod.POST)
  public List<JSONObject> searchOrdersNoExpress(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getEmployeeNumber() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NUMBER);

    if (orderRequest.getStartDate() == null && orderRequest.getEndDate() == null
            && orderRequest.getDocumentNumberClient() == null
            && (orderRequest.getOrders() == null || orderRequest.getOrders().size() == 0)
            && orderRequest.getOrderId() == null)
      throw new ConflictException(Constants.ERROR_SEARCH_ORDER_FILTER);


    JSONObject customerJson = new JSONObject();
    customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    if (orderRequest.getStartDate() != null && orderRequest.getEndDate() != null) {
      customerJson.put("startDate", orderRequest.getStartDate());
      customerJson.put("endDate", orderRequest.getEndDate());
    }

    if (orderRequest.getDocumentNumberClient() != null){
      customerJson.put("documentNumberClient", orderRequest.getDocumentNumberClient());
    }

    if (orderRequest.getOrderId() != null){
      customerJson.put("orderId", orderRequest.getOrderId());
    }

    if (orderRequest.getOrders() != null && orderRequest.getOrders().size() > 0){
      JSONArray orderArray = new JSONArray();

      for (Order order: orderRequest.getOrders()) {
        if (order.getOrderId() != null) {
          JSONObject orderJson = new JSONObject();
          orderJson.put("orderId", order.getOrderId());
          orderArray.add(orderJson);
        }
      }

      customerJson.put("orders", orderArray);
    }

    return CoreConnection.postListRequest(URLConnections.URL_SEARCH_ORDERS_NO_EXPRESS, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Gei id orders messenger assigned
   * @param orderRequest
   * @return json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getOrdersMessengersAssigned20", path = "/orderMonitorEndpoint/getOrdersMessengersAssigned20", httpMethod = ApiMethod.HttpMethod.POST)
  public List<JSONObject> getOrdersMessengersAssigned20(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getEmployeeNumber() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NUMBER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    //Filtros
    JSONObject filtersJson = new JSONObject();

    if (orderRequest.getFilters() != null) {
      FilterOrder filters = orderRequest.getFilters();

      //Filtro de tiendas
      if (filters.getStores() != null && filters.getStores().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (StoreFilter filter: filters.getStores()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("storeId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("stores", jsonArray);
      }

      //Filtro de ciudades
      if (filters.getCities() != null && filters.getCities().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (CityFilter filter: filters.getCities()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("cityId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("cities", jsonArray);
      }

      //Filtro de couries
      if (filters.getCouriers() != null && filters.getCouriers().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (CourierFilter filter: filters.getCouriers()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("courierId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("couriers", jsonArray);
      }

      //Filtro de estados de la orden
      if (filters.getOrderStatusFilters() != null && filters.getOrderStatusFilters().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (OrderStatusFilter filter: filters.getOrderStatusFilters()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("orderStatus", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("orderStatus", jsonArray);
      }

      //Filtro de metodos de pago
      if (filters.getPaymentMethods() != null && filters.getPaymentMethods().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (PaymentMethodFilter filter: filters.getPaymentMethods()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("paymentMethodId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("paymentMethods", jsonArray);
      }

      //Filtro de ruta optima
      if (filters.getOptimalRoute() != null && !filters.getOptimalRoute().isEmpty()) {
        filtersJson.put("optimalRoute", filters.getOptimalRoute());
      }

      customerJson.put("filters", filtersJson);
    }

    List<OrderResponse> responses = CoreConnection.postListRequest(URLConnections.URL_GET_ID_ORDERS, customerJson.toJSONString(), OrderResponse.class);

    JSONObject ordersJson = new JSONObject();
    ordersJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    if (responses != null && responses.size() > 0){
      JSONArray orderArray = new JSONArray();

      responses.stream().forEach(x -> {
        JSONObject orderJson = new JSONObject();
        orderJson.put("orderId", x.getOrderId());
        orderArray.add(orderJson);
      });

      ordersJson.put("orders", orderArray);
    }

    return CoreConnection.postListRequest(URLConnections.URL_GET_ORDERS_MESSENGER_ASSIGNED, ordersJson.toJSONString(), JSONObject.class);

  }

  /**
   * Gei id orders 15 messenger assigned
   * @param orderRequest
   * @return json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getOrdersMessengersAssigned15", path = "/orderMonitorEndpoint/getOrdersMessengersAssigned15", httpMethod = ApiMethod.HttpMethod.POST)
  public List<JSONObject> getOrdersMessengersAssigned15(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getEmployeeNumber() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NUMBER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    //Filtros
    JSONObject filtersJson = new JSONObject();

    if (orderRequest.getFilters() != null) {
      FilterOrder filters = orderRequest.getFilters();

      //Filtro de tiendas
      if (filters.getStores() != null && filters.getStores().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (StoreFilter filter: filters.getStores()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("storeId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("stores", jsonArray);
      }

      //Filtro de ciudades
      if (filters.getCities() != null && filters.getCities().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (CityFilter filter: filters.getCities()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("cityId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("cities", jsonArray);
      }

      //Filtro de couries
      if (filters.getCouriers() != null && filters.getCouriers().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (CourierFilter filter: filters.getCouriers()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("courierId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("couriers", jsonArray);
      }

      //Filtro de estados de la orden
      if (filters.getOrderStatusFilters() != null && filters.getOrderStatusFilters().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (OrderStatusFilter filter: filters.getOrderStatusFilters()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("orderStatus", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("orderStatus", jsonArray);
      }

      //Filtro de metodos de pago
      if (filters.getPaymentMethods() != null && filters.getPaymentMethods().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (PaymentMethodFilter filter: filters.getPaymentMethods()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("paymentMethodId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("paymentMethods", jsonArray);
      }

      //Filtro de ruta optima
      if (filters.getOptimalRoute() != null && !filters.getOptimalRoute().isEmpty()) {
        filtersJson.put("optimalRoute", filters.getOptimalRoute());
      }

      customerJson.put("filters", filtersJson);
    }

    List<OrderResponse> responses = CoreConnection.postListRequest(URLConnections.URL_GET_ID_ORDERS_1_5, customerJson.toJSONString(), OrderResponse.class);

    JSONObject ordersJson = new JSONObject();
    ordersJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    if (responses != null && responses.size() > 0){
      JSONArray orderArray = new JSONArray();

      responses.stream().forEach(x -> {
        JSONObject orderJson = new JSONObject();
        orderJson.put("orderId", x.getOrderId());
        orderArray.add(orderJson);
      });

      ordersJson.put("orders", orderArray);
    }

    return CoreConnection.postListRequest(URLConnections.URL_GET_ORDERS_MESSENGER_ASSIGNED_15, ordersJson.toJSONString(), JSONObject.class);
  }

  /**
   * Get orders 2.0 by number order
   * @param orderRequest
   * @return
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getIdOrdersLimit", path = "/orderMonitorEndpoint/getIdOrdersLimit", httpMethod = ApiMethod.HttpMethod.POST)
  public List<JSONObject> getIdOrdersLimit(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getEmployeeNumber() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NUMBER);

    if (orderRequest.getNumberOrders() <= 0)
      throw new ConflictException(Constants.ERROR_NUMBER_ORDERS);

    JSONObject customerJson = new JSONObject();
    customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    //Filtros
    JSONObject filtersJson = new JSONObject();

    if (orderRequest.getFilters() != null) {
      FilterOrder filters = orderRequest.getFilters();

      //Filtro de tiendas
      if (filters.getStores() != null && filters.getStores().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (StoreFilter filter: filters.getStores()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("storeId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("stores", jsonArray);
      }

      //Filtro de ciudades
      if (filters.getCities() != null && filters.getCities().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (CityFilter filter: filters.getCities()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("cityId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("cities", jsonArray);
      }

      //Filtro de couries
      if (filters.getCouriers() != null && filters.getCouriers().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (CourierFilter filter: filters.getCouriers()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("courierId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("couriers", jsonArray);
      }

      //Filtro de estados de la orden
      if (filters.getOrderStatusFilters() != null && filters.getOrderStatusFilters().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (OrderStatusFilter filter: filters.getOrderStatusFilters()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("orderStatus", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("orderStatus", jsonArray);
      }

      //Filtro de metodos de pago
      if (filters.getPaymentMethods() != null && filters.getPaymentMethods().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (PaymentMethodFilter filter: filters.getPaymentMethods()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("paymentMethodId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("paymentMethods", jsonArray);
      }

      //Filtro de ruta optima
      if (filters.getOptimalRoute() != null && !filters.getOptimalRoute().isEmpty()) {
        filtersJson.put("optimalRoute", filters.getOptimalRoute());
      }

      customerJson.put("filters", filtersJson);
    }

    List<OrderResponse> responses = CoreConnection.postListRequest(URLConnections.URL_GET_ID_ORDERS, customerJson.toJSONString(), OrderResponse.class);

    JSONObject ordersJson = new JSONObject();
    ordersJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    if (responses != null && responses.size() > 0){
      JSONArray orderArray = new JSONArray();

      responses.stream().limit(orderRequest.getNumberOrders()).forEach(x -> {
        JSONObject orderJson = new JSONObject();
        orderJson.put("orderId", x.getOrderId());
        orderArray.add(orderJson);
      });

      ordersJson.put("orders", orderArray);

      return CoreConnection.postListRequest(URLConnections.URL_SEARCH_ORDERS, ordersJson.toJSONString(), JSONObject.class);
      //return CoreConnection.postListRequest(URLConnections.URL_SEARCH_ORDERS_REPORTS, ordersJson.toJSONString(), JSONObject.class);
    } else {
      return new ArrayList<>();
    }
  }

  /**
   * Get orders 1.5 by number order
   * @param orderRequest
   * @return
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getIdOrdersLimit15", path = "/orderMonitorEndpoint/getIdOrdersLimit15", httpMethod = ApiMethod.HttpMethod.POST)
  public List<JSONObject> getIdOrdersLimit15(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getEmployeeNumber() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NUMBER);

    if (orderRequest.getNumberOrders() <= 0)
      throw new ConflictException(Constants.ERROR_NUMBER_ORDERS);

    JSONObject customerJson = new JSONObject();
    customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    //Filtros
    JSONObject filtersJson = new JSONObject();

    if (orderRequest.getFilters() != null) {
      FilterOrder filters = orderRequest.getFilters();

      //Filtro de tiendas
      if (filters.getStores() != null && filters.getStores().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (StoreFilter filter: filters.getStores()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("storeId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("stores", jsonArray);
      }

      //Filtro de ciudades
      if (filters.getCities() != null && filters.getCities().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (CityFilter filter: filters.getCities()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("cityId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("cities", jsonArray);
      }

      //Filtro de couries
      if (filters.getCouriers() != null && filters.getCouriers().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (CourierFilter filter: filters.getCouriers()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("courierId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("couriers", jsonArray);
      }

      //Filtro de estados de la orden
      if (filters.getOrderStatusFilters() != null && filters.getOrderStatusFilters().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (OrderStatusFilter filter: filters.getOrderStatusFilters()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("orderStatus", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("orderStatus", jsonArray);
      }

      //Filtro de metodos de pago
      if (filters.getPaymentMethods() != null && filters.getPaymentMethods().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (PaymentMethodFilter filter: filters.getPaymentMethods()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("paymentMethodId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("paymentMethods", jsonArray);
      }

      //Filtro de ruta optima
      if (filters.getOptimalRoute() != null && !filters.getOptimalRoute().isEmpty()) {
        filtersJson.put("optimalRoute", filters.getOptimalRoute());
      }

      customerJson.put("filters", filtersJson);
    }

    List<OrderResponse> responses = CoreConnection.postListRequest(URLConnections.URL_GET_ID_ORDERS_1_5, customerJson.toJSONString(), OrderResponse.class);

    JSONObject ordersJson = new JSONObject();
    ordersJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    if (responses != null && responses.size() > 0){
      JSONArray orderArray = new JSONArray();

      responses.stream().limit(orderRequest.getNumberOrders()).forEach(x -> {
        JSONObject orderJson = new JSONObject();
        orderJson.put("orderId", x.getOrderId());
        orderArray.add(orderJson);
      });

      ordersJson.put("orders", orderArray);

      return CoreConnection.postListRequest(URLConnections.URL_SEARCH_ORDERS_1_5, ordersJson.toJSONString(), JSONObject.class);
    } else {
      return new ArrayList<>();
    }
  }

  /**
   * Search ids orders monitor provider
   * @param orderRequest
   * @return Json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "searchIdOrdersProviders", path = "/orderMonitorEndpoint/searchIdOrdersProviders", httpMethod = ApiMethod.HttpMethod.POST)
  public List<JSONObject> searchIdOrdersProviders(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getEmployeeNumber() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NUMBER);

    if (orderRequest.getStartDate() == null && orderRequest.getEndDate() == null && orderRequest.getDocumentNumberClient() == null)
      throw new ConflictException(Constants.ERROR_SEARCH_ORDER_FILTER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    if (orderRequest.getStartDate() != null && orderRequest.getEndDate() != null) {
      customerJson.put("startDate", orderRequest.getStartDate());
      customerJson.put("endDate", orderRequest.getEndDate());
    } else {
      customerJson.put("documentNumberClient", orderRequest.getDocumentNumberClient());
    }

    //Filtros
    JSONObject filtersJson = new JSONObject();

    if (orderRequest.getFilters() != null) {
      FilterOrder filters = orderRequest.getFilters();

      //Filtro de tiendas
      if (filters.getStores() != null && filters.getStores().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (StoreFilter filter: filters.getStores()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("storeId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("stores", jsonArray);
      }

      //Filtro de ciudades
      if (filters.getCities() != null && filters.getCities().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (CityFilter filter: filters.getCities()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("cityId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("cities", jsonArray);
      }

      //Filtro de couries
      if (filters.getCouriers() != null && filters.getCouriers().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (CourierFilter filter: filters.getCouriers()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("courierId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("couriers", jsonArray);
      }

      //Filtro de estados de la orden
      if (filters.getOrderStatusFilters() != null && filters.getOrderStatusFilters().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (OrderStatusFilter filter: filters.getOrderStatusFilters()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("orderStatus", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("orderStatus", jsonArray);
      }

      //Filtro de metodos de pago
      if (filters.getPaymentMethods() != null && filters.getPaymentMethods().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (PaymentMethodFilter filter: filters.getPaymentMethods()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("paymentMethodId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("paymentMethods", jsonArray);
      }

      //Filtro de ruta optima
      if (filters.getOptimalRoute() != null && !filters.getOptimalRoute().isEmpty()) {
        filtersJson.put("optimalRoute", filters.getOptimalRoute());
      }

      customerJson.put("filters", filtersJson);
    }

    return CoreConnection.postListRequest(URLConnections.URL_SEARCH_ID_ORDERS_PROVIDERS, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Search orders reports by filters
   * @param orderRequest
   * @return List orders
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "searchOrdersReports20", path = "/orderMonitorEndpoint/searchOrdersReports20", httpMethod = ApiMethod.HttpMethod.POST)
  public List<JSONObject> searchOrdersReports20(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getEmployeeNumber() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NUMBER);

    if (orderRequest.getStartDate() == null && orderRequest.getEndDate() == null
            && orderRequest.getDocumentNumberClient() == null
            && (orderRequest.getOrders() == null || orderRequest.getOrders().size() == 0)
            && orderRequest.getOrderId() == null)
      throw new ConflictException(Constants.ERROR_SEARCH_ORDER_FILTER);


    JSONObject customerJson = new JSONObject();
    customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    if (orderRequest.getStartDate() != null && orderRequest.getEndDate() != null) {
      customerJson.put("startDate", orderRequest.getStartDate());
      customerJson.put("endDate", orderRequest.getEndDate());
    }

    if (orderRequest.getDocumentNumberClient() != null){
      customerJson.put("documentNumberClient", orderRequest.getDocumentNumberClient());
    }

    if (orderRequest.getOrderId() != null){
      customerJson.put("orderId", orderRequest.getOrderId());
    }

    if (orderRequest.getOrders() != null && orderRequest.getOrders().size() > 0){
      JSONArray orderArray = new JSONArray();

      for (Order order: orderRequest.getOrders()) {
        if (order.getOrderId() != null) {
          JSONObject orderJson = new JSONObject();
          orderJson.put("orderId", order.getOrderId());
          orderArray.add(orderJson);
        }
      }

      customerJson.put("orders", orderArray);
    }

    return CoreConnection.postListRequest(URLConnections.URL_SEARCH_ORDERS_REPORTS, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Get orders 2.0 cancelled
   * @param orderRequest
   * @return
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getIdOrdersCanceladas20", path = "/orderMonitorEndpoint/getIdOrdersCanceladas20", httpMethod = ApiMethod.HttpMethod.POST)
  public List<JSONObject> getIdOrdersCanceladas20(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getEmployeeNumber() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NUMBER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    return CoreConnection.postListRequest(URLConnections.URL_GET_ID_ORDERS_CANCELLED, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Get orders 1.5 cancelled
   * @param orderRequest
   * @return
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getIdOrdersCanceladas15", path = "/orderMonitorEndpoint/getIdOrdersCanceladas15", httpMethod = ApiMethod.HttpMethod.POST)
  public List<JSONObject> getIdOrdersCanceladas15(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getEmployeeNumber() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NUMBER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    return CoreConnection.postListRequest(URLConnections.URL_GET_ID_ORDERS_1_5_CANCELLED, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Get orders 2.0 glued
   * @return
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getOrdersEncoladas20", path = "/orderMonitorEndpoint/getOrdersEncoladas20", httpMethod = ApiMethod.HttpMethod.GET)
  public JSONObject getOrdersEncoladas20() throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    return CoreConnection.getRequest(URLConnections.URL_GET_ORDERS_GLUED, JSONObject.class);
  }

  /**
   * Get orders 1.5 glued
   * @return
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getOrdersEncoladas15", path = "/orderMonitorEndpoint/getOrdersEncoladas15", httpMethod = ApiMethod.HttpMethod.GET)
  public JSONObject getOrdersEncoladas15() throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    return CoreConnection.getRequest(URLConnections.URL_GET_ORDERS_1_5_GLUED, JSONObject.class);
  }

  /**
   * Verify order 2.0 is reassigned
   * @param orderId
   * @return json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "isOrderReassigned20", path = "/orderMonitorEndpoint/isOrderReassigned20", httpMethod = ApiMethod.HttpMethod.GET)
  public JSONObject isOrderReassigned20(
          @Named("orderId") final String orderId) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    return CoreConnection.getRequest(URLConnections.URL_GET_IS_ORDER_REASSIGNED + "/" + orderId, JSONObject.class);
  }

  /**
   * Verify order 1.5 is reassigned
   * @param orderId
   * @return json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "isOrderReassigned15", path = "/orderMonitorEndpoint/isOrderReassigned15", httpMethod = ApiMethod.HttpMethod.GET)
  public JSONObject isOrderReassigned15(
          @Named("orderId") final String orderId) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    return CoreConnection.getRequest(URLConnections.URL_GET_IS_ORDER_REASSIGNED_1_5 + "/" + orderId, JSONObject.class);
  }

  /**
   * Search orders report 1.5 by filters
   * @param orderRequest
   * @return List orders
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "searchOrdersReports15", path = "/orderMonitorEndpoint/searchOrdersReports15", httpMethod = ApiMethod.HttpMethod.POST)
  public List<JSONObject> searchOrdersReports15(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getEmployeeNumber() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NUMBER);

    if (orderRequest.getStartDate() == null
            && orderRequest.getEndDate() == null
            && orderRequest.getDocumentNumberClient() == null
            && (orderRequest.getOrders() == null || orderRequest.getOrders().size() == 0)
            && orderRequest.getOrderId() == null)
      throw new ConflictException(Constants.ERROR_SEARCH_ORDER_FILTER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    if (orderRequest.getStartDate() != null && orderRequest.getEndDate() != null) {
      customerJson.put("startDate", orderRequest.getStartDate());
      customerJson.put("endDate", orderRequest.getEndDate());
    }

    if (orderRequest.getDocumentNumberClient() != null){
      customerJson.put("documentNumberClient", orderRequest.getDocumentNumberClient());
    }

    if (orderRequest.getOrderId() != null){
      customerJson.put("orderId", orderRequest.getOrderId());
    }

    if (orderRequest.getOrders() != null && orderRequest.getOrders().size() > 0){
      JSONArray orderArray = new JSONArray();

      for (Order order: orderRequest.getOrders()) {
        if (order.getOrderId() != null) {
          JSONObject orderJson = new JSONObject();
          orderJson.put("orderId", order.getOrderId());
          orderArray.add(orderJson);
        }
      }

      customerJson.put("orders", orderArray);
    }

    return CoreConnection.postListRequest(URLConnections.URL_SEARCH_ORDERS_REPORTS_1_5, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Get ids orders 2.0 json
   * @return Json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getOrders20JSON", path = "/orderMonitorEndpoint/getOrders20JSON", httpMethod = ApiMethod.HttpMethod.GET)
  public List<JSONObject> getOrders20JSON(
          @Named("orderType") final String orderType
  ) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    return CoreConnection.getListRequest(URLConnections.URL_GET_ID_ORDERS_2_0_JSON + '/' + orderType, JSONObject.class);
  }

  /**
   * Get ids orders 1.5 json
   * @return Json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getOrders15JSON", path = "/orderMonitorEndpoint/getOrders15JSON", httpMethod = ApiMethod.HttpMethod.GET)
  public List<JSONObject> getOrders15JSON(
          @Named("orderType") final String orderType
  ) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    return CoreConnection.getListRequest(URLConnections.URL_GET_ID_ORDERS_1_5_JSON + '/' + orderType, JSONObject.class);
  }

  /**
   * Update data for dashboard 2.0
   * @return
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "updateDashboard20", path = "/orderMonitorEndpoint/updateDashboard20", httpMethod = ApiMethod.HttpMethod.GET)
  public JSONObject updateDashboard20(
          @Named("orderType") final String orderType
  ) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    JSONObject customerJson = new JSONObject();
    customerJson.put("employeeNumber", "0");

    List<OrderResponse> responses;

    switch (orderType){
      case Constants.PARAM_EXPRESS:
        responses = CoreConnection.postListRequest(URLConnections.URL_GET_ID_ORDERS, customerJson.toJSONString(), OrderResponse.class);
        break;
      case Constants.PARAM_PROGRAMMED:
        responses = CoreConnection.postListRequest(URLConnections.URL_GET_ID_ORDERS_PROGRAMMED, customerJson.toJSONString(), OrderResponse.class);
        break;
      case Constants.PARAM_SUBSCRIPTION:
        responses = CoreConnection.postListRequest(URLConnections.URL_GET_ID_ORDERS_SS, customerJson.toJSONString(), OrderResponse.class);
        break;
      case Constants.PARAM_MESSENGERS:
        List<OrderResponse> orderResponses = CoreConnection.postListRequest(URLConnections.URL_GET_ID_ORDERS, customerJson.toJSONString(), OrderResponse.class);

        JSONObject ordersMJson = new JSONObject();
        ordersMJson.put("employeeNumber", "0");

        if (orderResponses != null && orderResponses.size() > 0){
          JSONArray orderArray = new JSONArray();

          orderResponses.stream().forEach(x -> {
            JSONObject orderJson = new JSONObject();
            orderJson.put("orderId", x.getOrderId());
            orderArray.add(orderJson);
          });

          ordersMJson.put("orders", orderArray);
        }

        responses = CoreConnection.postListRequest(URLConnections.URL_GET_ORDERS_MESSENGER_ASSIGNED, ordersMJson.toJSONString(), OrderResponse.class);
        responses.forEach( x -> x.setOrderId(x.getId()));
        break;
      case Constants.PARAM_GESTIONADAS:
        responses = CoreConnection.postListRequest(URLConnections.URL_GET_ID_ORDERS_MANAGERS, customerJson.toJSONString(), OrderResponse.class);
        break;
      case Constants.PARAM_FACTURADAS:
        responses = CoreConnection.postListRequest(URLConnections.URL_GET_ID_ORDERS_BILLED, customerJson.toJSONString(), OrderResponse.class);
        break;
      case Constants.PARAM_CANCELED:
        responses = CoreConnection.postListRequest(URLConnections.URL_GET_ID_ORDERS_CANCELLED, customerJson.toJSONString(), OrderResponse.class);
        break;
      case Constants.PARAM_CANCELED_SUBSCRIPTION:
        responses = CoreConnection.postListRequest(URLConnections.URL_GET_ID_ORDERS_CANCELLED_SUBSCRIPTION, customerJson.toJSONString(), OrderResponse.class);
        break;
      default:
        throw new ConflictException(Constants.ERROR_ORDER_TYPE);
    }

    JSONObject ordersJson = new JSONObject();
    ordersJson.put("employeeNumber", "0");
    ordersJson.put("orderType", orderType);

    if (responses != null && responses.size() > 0){
      JSONArray orderArray = new JSONArray();

      responses.stream().forEach(x -> {
        JSONObject orderJson = new JSONObject();
        orderJson.put("orderId", x.getOrderId());
        orderArray.add(orderJson);
      });

      ordersJson.put("orders", orderArray);

      return CoreConnection.postRequest(URLConnections.URL_UPDATE_ORDERS_2_0_IN_TEMP, ordersJson.toJSONString(), JSONObject.class);
    }

    return new JSONObject();
  }

  /**
   * Update data for dashboard 1.5
   * @return
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "updateDashboard15", path = "/orderMonitorEndpoint/updateDashboard15", httpMethod = ApiMethod.HttpMethod.GET)
  public JSONObject updateDashboard15(
          @Named("orderType") final String orderType
  ) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    JSONObject customerJson = new JSONObject();
    customerJson.put("employeeNumber", "0");

    List<OrderResponse> responses;

    switch (orderType){
      case Constants.PARAM_EXPRESS:
        responses = CoreConnection.postListRequest(URLConnections.URL_GET_ID_ORDERS_1_5, customerJson.toJSONString(), OrderResponse.class);
        break;
      case Constants.PARAM_MESSENGERS:
        List<OrderResponse> orderResponses = CoreConnection.postListRequest(URLConnections.URL_GET_ID_ORDERS_1_5, customerJson.toJSONString(), OrderResponse.class);

        JSONObject ordersMJson = new JSONObject();
        ordersMJson.put("employeeNumber", "0");

        if (orderResponses != null && orderResponses.size() > 0){
          JSONArray orderArray = new JSONArray();

          orderResponses.stream().forEach(x -> {
            JSONObject orderJson = new JSONObject();
            orderJson.put("orderId", x.getOrderId());
            orderArray.add(orderJson);
          });

          ordersMJson.put("orders", orderArray);
        }

        responses = CoreConnection.postListRequest(URLConnections.URL_GET_ORDERS_MESSENGER_ASSIGNED_15, ordersMJson.toJSONString(), OrderResponse.class);
        responses.forEach( x -> x.setOrderId(x.getId()));
        break;
      case Constants.PARAM_GESTIONADAS:
        responses = CoreConnection.postListRequest(URLConnections.URL_GET_ID_ORDERS_1_5_MANAGERS, customerJson.toJSONString(), OrderResponse.class);
        break;
      case Constants.PARAM_CANCELED:
        responses = CoreConnection.postListRequest(URLConnections.URL_GET_ID_ORDERS_1_5_CANCELLED, customerJson.toJSONString(), OrderResponse.class);
        break;
      default:
        throw new ConflictException(Constants.ERROR_ORDER_TYPE);
    }

    JSONObject ordersJson = new JSONObject();
    ordersJson.put("employeeNumber", "0");
    ordersJson.put("orderType", orderType);

    if (responses != null && responses.size() > 0){
      JSONArray orderArray = new JSONArray();

      responses.stream().forEach(x -> {
        JSONObject orderJson = new JSONObject();
        orderJson.put("orderId", x.getOrderId());
        orderArray.add(orderJson);
      });

      ordersJson.put("orders", orderArray);

      return CoreConnection.postRequest(URLConnections.URL_UPDATE_ORDERS_1_5_IN_TEMP, ordersJson.toJSONString(), JSONObject.class);
    }

    return new JSONObject();
  }

  /**
   * Update data for dashboard provider
   * @return
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "updateDashboardProvider", path = "/orderMonitorEndpoint/updateDashboardProvider", httpMethod = ApiMethod.HttpMethod.GET)
  public JSONObject updateDashboardProvider(
          @Named("orderType") final String orderType
  ) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    JSONObject customerJson = new JSONObject();
    customerJson.put("employeeNumber", "0");

    List<OrderResponse> responses;

    switch (orderType){
      case Constants.PARAM_PROVIDER:
        responses = CoreConnection.postListRequest(URLConnections.URL_GET_ID_ORDERS_PROVIDE, customerJson.toJSONString(), OrderResponse.class);
        break;
      case Constants.PARAM_CANCELED_PROVIDER:
        responses = CoreConnection.postListRequest(URLConnections.URL_GET_ID_ORDERS_CANCELLED_PROVIDER, customerJson.toJSONString(), OrderResponse.class);
        break;
      default:
        throw new ConflictException(Constants.ERROR_ORDER_TYPE);
    }

    JSONObject ordersJson = new JSONObject();
    ordersJson.put("employeeNumber", "0");
    ordersJson.put("orderType", orderType);

    if (responses != null && responses.size() > 0){
      JSONArray orderArray = new JSONArray();

      responses.stream().forEach(x -> {
        JSONObject orderJson = new JSONObject();
        orderJson.put("orderId", x.getOrderId());
        orderArray.add(orderJson);
      });

      ordersJson.put("orders", orderArray);

      return CoreConnection.postRequest(URLConnections.URL_UPDATE_ORDERS_PROVIDER_IN_TEMP, ordersJson.toJSONString(), JSONObject.class);
    }

    return new JSONObject();
  }

  /**
   * Update data for dashboard no express
   * @return
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "updateDashboardNoExpress", path = "/orderMonitorEndpoint/updateDashboardNoExpress", httpMethod = ApiMethod.HttpMethod.GET)
  public JSONObject updateDashboardNoExpress(
          @Named("orderType") final String orderType
  ) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    JSONObject customerJson = new JSONObject();
    customerJson.put("employeeNumber", "0");

    List<OrderResponse> responses;

    switch (orderType){
      case Constants.PARAM_NO_EXPRESS:
        responses = CoreConnection.postListRequest(URLConnections.URL_GET_ID_ORDERS_NO_EXPRESS, customerJson.toJSONString(), OrderResponse.class);
        break;
      case Constants.PARAM_CANCELED_NO_EXPRESS:
        responses = CoreConnection.postListRequest(URLConnections.URL_GET_ID_ORDERS_CANCELLED_NO_EXPRESS, customerJson.toJSONString(), OrderResponse.class);
        break;
      case Constants.PARAM_INCOMPLETED_NO_EXPRESS:
        responses = CoreConnection.postListRequest(URLConnections.URL_GET_ID_ORDERS_INCOMPLETES_NO_EXPRESS, customerJson.toJSONString(), OrderResponse.class);
        break;
      default:
        throw new ConflictException(Constants.ERROR_ORDER_TYPE);
    }

    JSONObject ordersJson = new JSONObject();
    ordersJson.put("employeeNumber", "0");
    ordersJson.put("orderType", orderType);

    if (responses != null && responses.size() > 0){
      JSONArray orderArray = new JSONArray();

      responses.stream().forEach(x -> {
        JSONObject orderJson = new JSONObject();
        orderJson.put("orderId", x.getOrderId());
        orderArray.add(orderJson);
      });

      ordersJson.put("orders", orderArray);

      return CoreConnection.postRequest(URLConnections.URL_UPDATE_ORDERS_NO_EXPRESS_IN_TEMP, ordersJson.toJSONString(), JSONObject.class);
    }

    return new JSONObject();
  }

  /**
   * Search ids orders monitor 2.0 without employee number
   * @param orderRequest
   * @return Json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "searchIdOrdersReports20", path = "/orderMonitorEndpoint/searchIdOrdersReports20", httpMethod = ApiMethod.HttpMethod.POST)
  public List<JSONObject> searchIdOrdersReports20(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest.getStartDate() == null && orderRequest.getEndDate() == null
            && orderRequest.getDocumentNumberClient() == null && orderRequest.getFilters() == null
            && orderRequest.getFilters().getOrderGuide() == null)
      throw new ConflictException(Constants.ERROR_SEARCH_ORDER_FILTER);

    JSONObject customerJson = new JSONObject();

    if (orderRequest.getStartDate() != null && orderRequest.getEndDate() != null) {
      customerJson.put("startDate", orderRequest.getStartDate());
      customerJson.put("endDate", orderRequest.getEndDate());
    } else {
      customerJson.put("documentNumberClient", orderRequest.getDocumentNumberClient());
    }

    //Filtros
    JSONObject filtersJson = new JSONObject();

    if (orderRequest.getFilters() != null) {
      FilterOrder filters = orderRequest.getFilters();

      //Filtro de tiendas
      if (filters.getStores() != null && filters.getStores().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (StoreFilter filter: filters.getStores()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("storeId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("stores", jsonArray);
      }

      //Filtro de ciudades
      if (filters.getCities() != null && filters.getCities().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (CityFilter filter: filters.getCities()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("cityId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("cities", jsonArray);
      }

      //Filtro de couries
      if (filters.getCouriers() != null && filters.getCouriers().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (CourierFilter filter: filters.getCouriers()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("courierId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("couriers", jsonArray);
      }

      //Filtro de estados de la orden
      if (filters.getOrderStatusFilters() != null && filters.getOrderStatusFilters().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (OrderStatusFilter filter: filters.getOrderStatusFilters()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("orderStatus", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("orderStatus", jsonArray);
      }

      //Filtro de metodos de pago
      if (filters.getPaymentMethods() != null && filters.getPaymentMethods().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (PaymentMethodFilter filter: filters.getPaymentMethods()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("paymentMethodId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("paymentMethods", jsonArray);
      }

      //Filtro de ruta optima
      if (filters.getOptimalRoute() != null && !filters.getOptimalRoute().isEmpty()) {
        filtersJson.put("optimalRoute", filters.getOptimalRoute());
      }

      //Filtro de numero de guia
      if (filters.getOrderGuide() != null && !filters.getOrderGuide().isEmpty()) {
        filtersJson.put("orderGuide", filters.getOrderGuide());
      }

      customerJson.put("filters", filtersJson);
    }

    return CoreConnection.postListRequest(URLConnections.URL_SEARCH_ID_ORDERS_REPORT_2_0, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Search ids orders monitor 1.5 without employee number
   * @param orderRequest
   * @return Json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "searchIdOrdersReports15", path = "/orderMonitorEndpoint/searchIdOrdersReports15", httpMethod = ApiMethod.HttpMethod.POST)
  public List<JSONObject> searchIdOrdersReports15(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest.getStartDate() == null && orderRequest.getEndDate() == null && orderRequest.getDocumentNumberClient() == null)
      throw new ConflictException(Constants.ERROR_SEARCH_ORDER_FILTER);

    JSONObject customerJson = new JSONObject();

    if (orderRequest.getStartDate() != null && orderRequest.getEndDate() != null) {
      customerJson.put("startDate", orderRequest.getStartDate());
      customerJson.put("endDate", orderRequest.getEndDate());
    } else {
      customerJson.put("documentNumberClient", orderRequest.getDocumentNumberClient());
    }

    //Filtros
    JSONObject filtersJson = new JSONObject();

    if (orderRequest.getFilters() != null) {
      FilterOrder filters = orderRequest.getFilters();

      //Filtro de tiendas
      if (filters.getStores() != null && filters.getStores().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (StoreFilter filter: filters.getStores()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("storeId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("stores", jsonArray);
      }

      //Filtro de ciudades
      if (filters.getCities() != null && filters.getCities().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (CityFilter filter: filters.getCities()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("cityId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("cities", jsonArray);
      }

      //Filtro de couries
      if (filters.getCouriers() != null && filters.getCouriers().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (CourierFilter filter: filters.getCouriers()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("courierId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("couriers", jsonArray);
      }

      //Filtro de estados de la orden
      if (filters.getOrderStatusFilters() != null && filters.getOrderStatusFilters().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (OrderStatusFilter filter: filters.getOrderStatusFilters()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("orderStatus", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("orderStatus", jsonArray);
      }

      //Filtro de metodos de pago
      if (filters.getPaymentMethods() != null && filters.getPaymentMethods().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (PaymentMethodFilter filter: filters.getPaymentMethods()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("paymentMethodId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("paymentMethods", jsonArray);
      }

      //Filtro de ruta optima
      if (filters.getOptimalRoute() != null && !filters.getOptimalRoute().isEmpty()) {
        filtersJson.put("optimalRoute", filters.getOptimalRoute());
      }

      customerJson.put("filters", filtersJson);
    }

    return CoreConnection.postListRequest(URLConnections.URL_SEARCH_ID_ORDERS_REPORT_1_5, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Update guide and courier orden provider
   * @param orderRequest
   * @return
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "addOrderGuideProvider", path = "/orderMonitorEndpoint/addOrderGuideProvider", httpMethod = ApiMethod.HttpMethod.POST)
  public JSONObject addOrderGuideProvider(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getOrderId() == null)
      throw new ConflictException(Constants.ERROR_ID_ORDER);

    if (orderRequest == null || orderRequest.getOrderGuide() == null)
      throw new ConflictException(Constants.ERROR_ORDER_GUIDE);

    //Cambio de estado
    JSONObject orderJson = new JSONObject();
    orderJson.put("orderId", orderRequest.getOrderId());
    orderJson.put("observation", orderRequest.getObservation());
    orderJson.put("rol", orderRequest.getRol());
    orderJson.put("correoUsuario", orderRequest.getCorreoUsuario());
    orderJson.put("statusId", orderRequest.getStatusId());
    orderJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    CoreConnection.postRequest(URLConnections.URL_MODIFY_STATUS_ORDER_PROVIDER, orderJson.toJSONString(), Void.class);

    JSONObject customerJson = new JSONObject();
    customerJson.put("orderId", orderRequest.getOrderId());
    customerJson.put("orderGuide", orderRequest.getOrderGuide());
    customerJson.put("courierId", orderRequest.getCourierId());

    if (orderRequest.getEmployeeNumber() != null && !orderRequest.getEmployeeNumber().isEmpty())
      customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    JSONObject jsonObject = CoreConnection.postRequest(URLConnections.URL_ADD_ORDER_GUIDE_PROVIDER, customerJson.toJSONString(), JSONObject.class);

    String courierName = null;
    String tracking = "";
    String trackingEmail = "";

    switch (orderRequest.getCourierId()) {
      case 5:
        courierName = "SERVIENTREGA";
        tracking = " " + Constants.TRACKING_SERVIENTREGA;
        trackingEmail = Constants.TRACKING_SERVIENTREGA;
        break;
      case 11:
        courierName = "LIBERTY";
        tracking = " " + Constants.TRACKING_LIBERTY;
        trackingEmail = Constants.TRACKING_LIBERTY;
        break;
      case 12:
        courierName = "CARGONAM";
        trackingEmail = "";
        break;
    }

    //SEND SMS
    if (orderRequest.getPhoneCustomer() != null && !orderRequest.getPhoneCustomer().isEmpty()
            && orderRequest.getCourierId() > 0 && courierName != null && !courierName.isEmpty()) {
      sendSmsSendOrder(orderRequest.getPhoneCustomer(), orderRequest.getOrderId().toString(), orderRequest.getOrderGuide(), courierName, tracking);
    }

    //SEND EMAIL
    if (courierName != null && !courierName.isEmpty()) {
      JSONObject mailJson = new JSONObject();
      mailJson.put("orderId", orderRequest.getOrderId());
      mailJson.put("orderGuide", orderRequest.getOrderGuide());
      mailJson.put("courier", courierName);
      mailJson.put("urlTracking", trackingEmail);
      mailJson.put("employeeNumber", orderRequest.getEmployeeNumber());
      CoreConnection.postRequest(URLConnections.URL_SEND_MAIL_NO_EXPRESS, mailJson.toJSONString(), Void.class);
    }

    return jsonObject;
  }

  /**
   * Get orders provider cancelled
   * @param orderRequest
   * @return
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getIdCanceledOrderForProvider", path = "/orderMonitorEndpoint/getIdCanceledOrderForProvider", httpMethod = ApiMethod.HttpMethod.POST)
  public List<JSONObject> getIdCanceledOrderForProvider(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getEmployeeNumber() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NUMBER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    return CoreConnection.postListRequest(URLConnections.URL_GET_ID_ORDERS_CANCELLED_PROVIDER, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Get orders no express cancelled
   * @param orderRequest
   * @return
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getIdCanceledOrdersNoExpress", path = "/orderMonitorEndpoint/getIdCanceledOrdersNoExpress", httpMethod = ApiMethod.HttpMethod.POST)
  public List<JSONObject> getIdCanceledOrdersNoExpress(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getEmployeeNumber() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NUMBER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    return CoreConnection.postListRequest(URLConnections.URL_GET_ID_ORDERS_CANCELLED_NO_EXPRESS, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Get orders no express cancelled
   * @param orderRequest
   * @return
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getIdCanceledOrdersSubscription", path = "/orderMonitorEndpoint/getIdCanceledOrdersSubscription", httpMethod = ApiMethod.HttpMethod.POST)
  public List<JSONObject> getIdCanceledOrdersSubscription(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getEmployeeNumber() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NUMBER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    return CoreConnection.postListRequest(URLConnections.URL_GET_ID_ORDERS_CANCELLED_SUBSCRIPTION, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Get ids orders incompletes nationals y envialo ya
   * @param orderRequest
   * @return Json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getIdIncompletedOrdersNoExpress", path = "/orderMonitorEndpoint/getIdIncompletedOrdersNoExpress", httpMethod = ApiMethod.HttpMethod.POST)
  public List<JSONObject> getIdIncompletedOrdersNoExpress(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getEmployeeNumber() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NUMBER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    //Filtros
    JSONObject filtersJson = new JSONObject();

    if (orderRequest.getFilters() != null) {
      FilterOrder filters = orderRequest.getFilters();

      //Filtro de tiendas
      if (filters.getStores() != null && filters.getStores().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (StoreFilter filter: filters.getStores()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("storeId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("stores", jsonArray);
      }

      //Filtro de ciudades
      if (filters.getCities() != null && filters.getCities().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (CityFilter filter: filters.getCities()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("cityId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("cities", jsonArray);
      }

      //Filtro de couries
      if (filters.getCouriers() != null && filters.getCouriers().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (CourierFilter filter: filters.getCouriers()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("courierId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("couriers", jsonArray);
      }

      //Filtro de estados de la orden
      if (filters.getOrderStatusFilters() != null && filters.getOrderStatusFilters().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (OrderStatusFilter filter: filters.getOrderStatusFilters()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("orderStatus", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("orderStatus", jsonArray);
      }

      //Filtro de metodos de pago
      if (filters.getPaymentMethods() != null && filters.getPaymentMethods().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (PaymentMethodFilter filter: filters.getPaymentMethods()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("paymentMethodId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("paymentMethods", jsonArray);
      }

      //Filtro de ruta optima
      if (filters.getOptimalRoute() != null && !filters.getOptimalRoute().isEmpty()) {
        filtersJson.put("optimalRoute", filters.getOptimalRoute());
      }

      //Filtro de numero de guia
      if (filters.getOrderGuide() != null && !filters.getOrderGuide().isEmpty()) {
        filtersJson.put("orderGuide", filters.getOrderGuide());
      }

      customerJson.put("filters", filtersJson);
    }

    return CoreConnection.postListRequest(URLConnections.URL_GET_ID_ORDERS_INCOMPLETES_NO_EXPRESS, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Get order detail incomplete no express
   * @param orderRequest
   * @return List order detail
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getOrderDetailNoExpressIncompleted", path = "/orderMonitorEndpoint/getOrderDetailNoExpressIncompleted", httpMethod = ApiMethod.HttpMethod.POST)
  public JSONObject getOrderDetailNoExpressIncompleted(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getOrderId() == null)
      throw new ConflictException(Constants.ERROR_ID_ORDER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("orderId", orderRequest.getOrderId());

    return CoreConnection.postRequest(URLConnections.URL_GET_DETAIL_ORDER_INCOMPLETED_NO_EXPRESS, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Update order incomplete
   * @param orderRequest
   * @return json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "insertItemOrderIncomplete", path = "/orderMonitorEndpoint/insertItemOrderIncomplete", httpMethod = ApiMethod.HttpMethod.PUT)
  public Answer insertItemOrderIncomplete(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getOrderId() == null)
      throw new ConflictException(Constants.ERROR_ID_ORDER);

    /*if (orderRequest == null || orderRequest.getItems() == null || orderRequest.getItems().size() == 0)
      throw new ConflictException(Constants.ERROR_LIST_ITEMS_ORDER);*/

    Answer answer = new Answer();
    answer.setConfirmation(false);

    JSONArray items = new JSONArray();
    JSONArray coupons = new JSONArray();
    for (OrderItem orderItem : orderRequest.getItems()) {
      JSONObject itemJson = new JSONObject();
      itemJson.put("item", orderItem.getItemId());
      itemJson.put("quantityRequested", orderItem.getQuantityRequested());
      items.add(itemJson);
    }

    JSONObject objectJson = new JSONObject();
    objectJson.put("order_no", orderRequest.getOrderId());
    objectJson.put("uuid", orderRequest.getUuid());
    objectJson.put("items", items);
    objectJson.put("coupons", coupons);

    HttpURLConnection httpURLConnection = URLConnections.getConnection(URLConnections.PUT, URLConnections.URL_INSERT_ITEM_ORDER_INCOMPLETED);
    log.info(objectJson.toJSONString());
    OutputStreamWriter wr = new OutputStreamWriter(httpURLConnection.getOutputStream(), "UTF-8");
    wr.write(objectJson.toJSONString());
    wr.flush();
    int responseCode = httpURLConnection.getResponseCode();
    log.info("Code response insertItemOrderIncomplete order [" + responseCode + "]");
    switch (responseCode) {
      case 201:
      case 200:
        answer.setConfirmation(true);
        break;
      default:
        answer.setMessage("No se pudo editar la orden, por favor intente más tarde!");
        if (httpURLConnection.getErrorStream() != null) {
          BufferedReader bufferedReaderError = new BufferedReader(new InputStreamReader(httpURLConnection.getErrorStream(), "UTF-8"));
          if (bufferedReaderError != null) {
            String inputLineError;
            StringBuilder responseJsonError = new StringBuilder();
            while ((inputLineError = bufferedReaderError.readLine()) != null) {
              responseJsonError.append(inputLineError);
            }
            bufferedReaderError.close();
            ObjectMapper objectMapperError = new ObjectMapper();
            ErrorFarmatodo responseError = objectMapperError.readValue(responseJsonError.toString(), ErrorFarmatodo.class);
            if (responseError != null && responseError.getMessage() != null && !responseError.getMessage().isEmpty()) {
              answer.setMessage(responseError.getMessage().substring(0, 1).toUpperCase()
                      + responseError.getMessage().substring(1));
            }
          }
        }
        answer.setConfirmation(false);
        break;
    }
    return answer;
  }

  /**
   * Modify status orden provider
   * @param orderRequest
   * @return
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "modifyOrderStatus20Provider", path = "/orderMonitorEndpoint/modifyOrderStatus20Provider", httpMethod = ApiMethod.HttpMethod.POST)
  public JSONObject modifyOrderStatus20Provider(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getOrderId() == null)
      throw new ConflictException(Constants.ERROR_ID_ORDER);

    if (orderRequest == null || orderRequest.getRol() == null)
      throw new ConflictException(Constants.ERROR_USER_ROL);

    if (orderRequest == null || orderRequest.getCorreoUsuario() == null)
      throw new ConflictException(Constants.ERROR_USER_EMAIL);

    if (orderRequest == null || orderRequest.getStatusId() == null)
      throw new ConflictException(Constants.ERROR_STATUS_ORDER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("orderId", orderRequest.getOrderId());
    customerJson.put("observation", orderRequest.getObservation());
    customerJson.put("rol", orderRequest.getRol());
    customerJson.put("correoUsuario", orderRequest.getCorreoUsuario());
    customerJson.put("statusId", orderRequest.getStatusId());

    if (orderRequest.getEmployeeNumber() != null && !orderRequest.getEmployeeNumber().isEmpty())
      customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    return CoreConnection.postRequest(URLConnections.URL_MODIFY_STATUS_ORDER_PROVIDER, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Get order provider e delivery by id
   * @param orderId
   * @return json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   * @throws WriterException
   */
  @ApiMethod(name = "getDeliveryValueProvider", path = "/orderMonitorEndpoint/getDeliveryValueProvider", httpMethod = ApiMethod.HttpMethod.GET)
  public JSONObject getDeliveryValueProvider(@Named("orderId") final Long orderId)
          throws ConflictException, BadRequestException, IOException, InternalServerErrorException, WriterException {
    String url = URLConnections.URL_ORDER_PROVIDER_MONITOR + "/" + orderId;
    return CoreConnection.getRequest(url, JSONObject.class);
  }

  /**
   * Get orders average 2.0
   * @param orderRequest
   * @return Json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getOrdersStuckAverage20", path = "/orderMonitorEndpoint/getOrdersStuckAverage20", httpMethod = ApiMethod.HttpMethod.POST)
  public List<JSONObject> getOrdersStuckAverage20(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || (orderRequest.getStartDate() == null && orderRequest.getEndDate() == null
            && orderRequest.getDocumentNumberClient() == null))
      throw new ConflictException(Constants.ERROR_SEARCH_GRAPH_FILTER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("startDate", orderRequest.getStartDate());
    customerJson.put("endDate", orderRequest.getEndDate());

    if (orderRequest.getCity() != null && !orderRequest.getCity().isEmpty()) {
      customerJson.put("cityId", orderRequest.getCity());
      return CoreConnection.postListRequest(URLConnections.URL_ORDER_STUCK_AVERAGE20_CITY, customerJson.toJSONString(), JSONObject.class);
    } else {
      return CoreConnection.postListRequest(URLConnections.URL_ORDER_STUCK_AVERAGE20, customerJson.toJSONString(), JSONObject.class);
    }
  }

  /**
   * Get orders delivery average 2.0
   * @param orderRequest
   * @return Json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getOrdersDeliveryTimeAverage20", path = "/orderMonitorEndpoint/getOrdersDeliveryTimeAverage20", httpMethod = ApiMethod.HttpMethod.POST)
  public List<JSONObject> getOrdersDeliveryTimeAverage20(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || (orderRequest.getStartDate() == null && orderRequest.getEndDate() == null
            && orderRequest.getDocumentNumberClient() == null))
      throw new ConflictException(Constants.ERROR_SEARCH_GRAPH_FILTER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("startDate", orderRequest.getStartDate());
    customerJson.put("endDate", orderRequest.getEndDate());

    if (orderRequest.getCity() != null && !orderRequest.getCity().isEmpty()) {
      customerJson.put("cityId", orderRequest.getCity());
      return CoreConnection.postListRequest(URLConnections.URL_ORDER_DELIVEY_AVERAGE20_CITY, customerJson.toJSONString(), JSONObject.class);
    } else {
      return CoreConnection.postListRequest(URLConnections.URL_ORDER_DELIVEY_AVERAGE20, customerJson.toJSONString(), JSONObject.class);
    }
  }

  /**
   * Get orders average 1.5
   * @param orderRequest
   * @return Json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getOrdersStuckAverage15", path = "/orderMonitorEndpoint/getOrdersStuckAverage15", httpMethod = ApiMethod.HttpMethod.POST)
  public List<JSONObject> getOrdersStuckAverage15(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || (orderRequest.getStartDate() == null && orderRequest.getEndDate() == null
            && orderRequest.getDocumentNumberClient() == null))
      throw new ConflictException(Constants.ERROR_SEARCH_GRAPH_FILTER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("startDate", orderRequest.getStartDate());
    customerJson.put("endDate", orderRequest.getEndDate());

    if (orderRequest.getCity() != null && !orderRequest.getCity().isEmpty()) {
      customerJson.put("cityId", orderRequest.getCity());
      return CoreConnection.postListRequest(URLConnections.URL_ORDER_STUCK_AVERAGE15_CITY, customerJson.toJSONString(), JSONObject.class);
    } else {
      return CoreConnection.postListRequest(URLConnections.URL_ORDER_STUCK_AVERAGE15, customerJson.toJSONString(), JSONObject.class);
    }
  }

  /**
   * Get orders delivery average 1.5
   * @param orderRequest
   * @return Json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getOrdersDeliveryTimeAverage15", path = "/orderMonitorEndpoint/getOrdersDeliveryTimeAverage15", httpMethod = ApiMethod.HttpMethod.POST)
  public List<JSONObject> getOrdersDeliveryTimeAverage15(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || (orderRequest.getStartDate() == null && orderRequest.getEndDate() == null
            && orderRequest.getDocumentNumberClient() == null))
      throw new ConflictException(Constants.ERROR_SEARCH_GRAPH_FILTER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("startDate", orderRequest.getStartDate());
    customerJson.put("endDate", orderRequest.getEndDate());

    if (orderRequest.getCity() != null && !orderRequest.getCity().isEmpty()) {
      customerJson.put("cityId", orderRequest.getCity());
      return CoreConnection.postListRequest(URLConnections.URL_ORDER_DELIVEY_AVERAGE15_CITY, customerJson.toJSONString(), JSONObject.class);
    } else {
      return CoreConnection.postListRequest(URLConnections.URL_ORDER_DELIVEY_AVERAGE15, customerJson.toJSONString(), JSONObject.class);
    }
  }

  /**
   * Update orders stuck average
   * @return
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "putOrdersStuckAverage", path = "/orderMonitorEndpoint/putOrdersStuckAverage", httpMethod = ApiMethod.HttpMethod.GET)
  public JSONObject putOrdersStuckAverage(
          @Named("type") final int type
  ) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    final Calendar now = GregorianCalendar.getInstance(TimeZone.getTimeZone("America/Bogota"));
    int hour = now.get(Calendar.HOUR_OF_DAY);
    int year = now.get(Calendar.YEAR);
    int month = now.get(Calendar.MONTH) + 1;
    int day = now.get(Calendar.DAY_OF_MONTH);

    String startDate = null;
    String endDate = null;

    String hourString = String.valueOf(hour);
    if (hour < 10)
      hourString = "0" + hourString;

    String dayString = String.valueOf(day);
    if (day < 10)
      dayString = "0" + dayString;

    String monthString = String.valueOf(month);
    if (month < 10)
      monthString = "0" + monthString;

    switch (type){
      case 1:
        endDate = year + "/" + monthString + "/" + dayString + " " + hourString + ":00:00";
        final Calendar endDateMinus = GregorianCalendar.getInstance(TimeZone.getTimeZone("America/Bogota"));
        endDateMinus.set(year, month -1, day,hour, 0);
        endDateMinus.add((GregorianCalendar.MINUTE), -20);
        int hourMinus = endDateMinus.get(Calendar.HOUR_OF_DAY);
        int yearMinus = endDateMinus.get(Calendar.YEAR);
        int monthMinus = endDateMinus.get(Calendar.MONTH) + 1;
        int dayMinus = endDateMinus.get(Calendar.DAY_OF_MONTH);

        String hourMinusString = String.valueOf(hourMinus);
        if (hourMinus < 10)
          hourMinusString = "0" + hourMinusString;

        String dayMinusString = String.valueOf(dayMinus);
        if (dayMinus < 10)
          dayMinusString = "0" + dayMinusString;

        String monthMinusString = String.valueOf(monthMinus);
        if (month < 10)
          monthMinusString = "0" + monthMinusString;

        startDate = yearMinus + "/" + monthMinusString + "/" + dayMinusString + " " + hourMinusString + ":40:01";
        break;
      case 2:
        startDate = year + "/" + monthString + "/" + dayString + " " + hourString + ":00:01";
        endDate = year + "/" + monthString + "/" + dayString + " " + hourString + ":20:00";
        break;
      case 3:
        startDate = year + "/" + monthString + "/" + dayString + " " + hourString + ":20:01";
        endDate = year + "/" + monthString + "/" + dayString + " " + hourString + ":40:00";
        break;
    }

    JSONObject customerJson = new JSONObject();
    customerJson.put("startDate", startDate);
    customerJson.put("endDate", endDate);

    return CoreConnection.postRequest(URLConnections.URL_PUT_ORDERS_STUCK_AVERAGE, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Update orders stuck average by city
   * @return
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "putOrdersStuckAverageByCity", path = "/orderMonitorEndpoint/putOrdersStuckAverageByCity", httpMethod = ApiMethod.HttpMethod.GET)
  public JSONObject putOrdersStuckAverageByCity(
          @Named("type") final int type,
          @Named("city") final String city
  ) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    final Calendar now = GregorianCalendar.getInstance(TimeZone.getTimeZone("America/Bogota"));
    int hour = now.get(Calendar.HOUR_OF_DAY);
    int year = now.get(Calendar.YEAR);
    int month = now.get(Calendar.MONTH) + 1;
    int day = now.get(Calendar.DAY_OF_MONTH);

    String startDate = null;
    String endDate = null;

    String hourString = String.valueOf(hour);
    if (hour < 10)
      hourString = "0" + hourString;

    String dayString = String.valueOf(day);
    if (day < 10)
      dayString = "0" + dayString;

    String monthString = String.valueOf(month);
    if (month < 10)
      monthString = "0" + monthString;

    switch (type){
      case 1:
        endDate = year + "/" + monthString + "/" + dayString + " " + hourString + ":00:00";
        final Calendar endDateMinus = GregorianCalendar.getInstance(TimeZone.getTimeZone("America/Bogota"));
        endDateMinus.set(year, month -1, day,hour, 0);
        endDateMinus.add((GregorianCalendar.MINUTE), -20);
        int hourMinus = endDateMinus.get(Calendar.HOUR_OF_DAY);
        int yearMinus = endDateMinus.get(Calendar.YEAR);
        int monthMinus = endDateMinus.get(Calendar.MONTH) + 1;
        int dayMinus = endDateMinus.get(Calendar.DAY_OF_MONTH);

        String hourMinusString = String.valueOf(hourMinus);
        if (hourMinus < 10)
          hourMinusString = "0" + hourMinusString;

        String dayMinusString = String.valueOf(dayMinus);
        if (dayMinus < 10)
          dayMinusString = "0" + dayMinusString;

        String monthMinusString = String.valueOf(monthMinus);
        if (month < 10)
          monthMinusString = "0" + monthMinusString;

        startDate = yearMinus + "/" + monthMinusString + "/" + dayMinusString + " " + hourMinusString + ":40:01";
        break;
      case 2:
        startDate = year + "/" + monthString + "/" + dayString + " " + hourString + ":00:01";
        endDate = year + "/" + monthString + "/" + dayString + " " + hourString + ":20:00";
        break;
      case 3:
        startDate = year + "/" + monthString + "/" + dayString + " " + hourString + ":20:01";
        endDate = year + "/" + monthString + "/" + dayString + " " + hourString + ":40:00";
        break;
    }

    JSONObject customerJson = new JSONObject();
    customerJson.put("startDate", startDate);
    customerJson.put("endDate", endDate);
    customerJson.put("cityId", city);

    return CoreConnection.postRequest(URLConnections.URL_PUT_ORDERS_STUCK_AVERAGE_CITY, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Update orders delivery time average
   * @return
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "putOrdersDeliveryTimeAverage", path = "/orderMonitorEndpoint/putOrdersDeliveryTimeAverage", httpMethod = ApiMethod.HttpMethod.GET)
  public JSONObject putOrdersDeliveryTimeAverage() throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    final Calendar now = GregorianCalendar.getInstance(TimeZone.getTimeZone("America/Bogota"));
    int hour = now.get(Calendar.HOUR_OF_DAY);
    int year = now.get(Calendar.YEAR);
    int month = now.get(Calendar.MONTH);
    int day = now.get(Calendar.DAY_OF_MONTH);

    String startDate = null;
    String endDate = null;

    final Calendar endDateMinus = GregorianCalendar.getInstance(TimeZone.getTimeZone("America/Bogota"));
    endDateMinus.set(year, month, day,hour, 0);
    endDateMinus.add((GregorianCalendar.HOUR), -1);
    int hourMinus = endDateMinus.get(Calendar.HOUR_OF_DAY);
    int yearMinus = endDateMinus.get(Calendar.YEAR);
    int monthMinus = endDateMinus.get(Calendar.MONTH) + 1;
    int dayMinus = endDateMinus.get(Calendar.DAY_OF_MONTH);

    String hourMinusString = String.valueOf(hourMinus);
    if (hourMinus < 10)
      hourMinusString = "0" + hourMinusString;

    String dayMinusString = String.valueOf(dayMinus);
    if (dayMinus < 10)
      dayMinusString = "0" + dayMinusString;

    String monthMinusString = String.valueOf(monthMinus);
    if (month < 10)
      monthMinusString = "0" + monthMinusString;

    startDate = yearMinus + "/" + monthMinusString + "/" + dayMinusString + " " + hourMinusString + ":00:00";
    endDate = yearMinus + "/" + monthMinusString + "/" + dayMinusString + " " + hourMinusString + ":59:59";

    JSONObject customerJson = new JSONObject();
    customerJson.put("startDate", startDate);
    customerJson.put("endDate", endDate);

    return CoreConnection.postRequest(URLConnections.URL_PUT_ORDERS_DELIVERY_TIME_AVERAGE, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Update orders stuck average by city
   * @return
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "putOrdersDeliveryTimeAverageByCity", path = "/orderMonitorEndpoint/putOrdersDeliveryTimeAverageByCity", httpMethod = ApiMethod.HttpMethod.GET)
  public JSONObject putOrdersDeliveryTimeAverageByCity(
          @Named("city") final String city
  ) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    final Calendar now = GregorianCalendar.getInstance(TimeZone.getTimeZone("America/Bogota"));
    int hour = now.get(Calendar.HOUR_OF_DAY);
    int year = now.get(Calendar.YEAR);
    int month = now.get(Calendar.MONTH);
    int day = now.get(Calendar.DAY_OF_MONTH);

    String startDate = null;
    String endDate = null;

    final Calendar endDateMinus = GregorianCalendar.getInstance(TimeZone.getTimeZone("America/Bogota"));
    endDateMinus.set(year, month, day,hour, 0);
    endDateMinus.add((GregorianCalendar.HOUR), -1);
    int hourMinus = endDateMinus.get(Calendar.HOUR_OF_DAY);
    int yearMinus = endDateMinus.get(Calendar.YEAR);
    int monthMinus = endDateMinus.get(Calendar.MONTH) + 1;
    int dayMinus = endDateMinus.get(Calendar.DAY_OF_MONTH);

    String hourMinusString = String.valueOf(hourMinus);
    if (hourMinus < 10)
      hourMinusString = "0" + hourMinusString;

    String dayMinusString = String.valueOf(dayMinus);
    if (dayMinus < 10)
      dayMinusString = "0" + dayMinusString;

    String monthMinusString = String.valueOf(monthMinus);
    if (month < 10)
      monthMinusString = "0" + monthMinusString;

    startDate = yearMinus + "/" + monthMinusString + "/" + dayMinusString + " " + hourMinusString + ":00:00";
    endDate = yearMinus + "/" + monthMinusString + "/" + dayMinusString + " " + hourMinusString + ":59:59";

    JSONObject customerJson = new JSONObject();
    customerJson.put("startDate", startDate);
    customerJson.put("endDate", endDate);
    customerJson.put("cityId", city);

    return CoreConnection.postRequest(URLConnections.URL_PUT_ORDERS_DELIVERY_TIME_AVERAGE_CITY, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Get orders average 2.0
   * @param orderRequest
   * @return Json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getOrdersStuckAverage", path = "/orderMonitorEndpoint/getOrdersStuckAverage", httpMethod = ApiMethod.HttpMethod.POST)
  public List<JSONObject> getOrdersStuckAverage(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || (orderRequest.getStartDate() == null && orderRequest.getEndDate() == null
            && orderRequest.getDocumentNumberClient() == null))
      throw new ConflictException(Constants.ERROR_SEARCH_GRAPH_FILTER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("startDate", orderRequest.getStartDate());
    customerJson.put("endDate", orderRequest.getEndDate());

    if (orderRequest.getCity() != null && !orderRequest.getCity().isEmpty()) {
      customerJson.put("cityId", orderRequest.getCity());
      return CoreConnection.postListRequest(URLConnections.URL_GET_ORDERS_STUCK_AVERAGE_CITY, customerJson.toJSONString(), JSONObject.class);
    } else {
      return CoreConnection.postListRequest(URLConnections.URL_GET_ORDERS_STUCK_AVERAGE, customerJson.toJSONString(), JSONObject.class);
    }
  }

  /**
   * Get orders delivery average
   * @param orderRequest
   * @return Json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getOrdersDeliveryTimeAverage", path = "/orderMonitorEndpoint/getOrdersDeliveryTimeAverage", httpMethod = ApiMethod.HttpMethod.POST)
  public List<JSONObject> getOrdersDeliveryTimeAverage(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || (orderRequest.getStartDate() == null && orderRequest.getEndDate() == null
            && orderRequest.getDocumentNumberClient() == null))
      throw new ConflictException(Constants.ERROR_SEARCH_GRAPH_FILTER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("startDate", orderRequest.getStartDate());
    customerJson.put("endDate", orderRequest.getEndDate());

    if (orderRequest.getCity() != null && !orderRequest.getCity().isEmpty()) {
      customerJson.put("cityId", orderRequest.getCity());
      return CoreConnection.postListRequest(URLConnections.URL_GET_ORDERS_DELIVERY_TIME_AVERAGE_CITY, customerJson.toJSONString(), JSONObject.class);
    } else {
      return CoreConnection.postListRequest(URLConnections.URL_GET_ORDERS_DELIVERY_TIME_AVERAGE, customerJson.toJSONString(), JSONObject.class);
    }
  }

  /**
   * Get id orders issued 2.0
   * @param orderRequest
   * @return json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getIdOrdersEmitidas20", path = "/orderMonitorEndpoint/getIdOrdersEmitidas20", httpMethod = ApiMethod.HttpMethod.POST)
  public List<JSONObject> getIdOrdersEmitidas20(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getEmployeeNumber() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NUMBER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    //Filtros
    JSONObject filtersJson = new JSONObject();

    if (orderRequest.getFilters() != null) {
      FilterOrder filters = orderRequest.getFilters();

      //Filtro de tiendas
      if (filters.getStores() != null && filters.getStores().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (StoreFilter filter : filters.getStores()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("storeId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("stores", jsonArray);
      }

      //Filtro de ciudades
      if (filters.getCities() != null && filters.getCities().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (CityFilter filter : filters.getCities()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("cityId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("cities", jsonArray);
      }

      //Filtro de couries
      if (filters.getCouriers() != null && filters.getCouriers().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (CourierFilter filter : filters.getCouriers()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("courierId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("couriers", jsonArray);
      }

      //Filtro de estados de la orden
      if (filters.getOrderStatusFilters() != null && filters.getOrderStatusFilters().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (OrderStatusFilter filter : filters.getOrderStatusFilters()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("orderStatus", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("orderStatus", jsonArray);
      }

      //Filtro de metodos de pago
      if (filters.getPaymentMethods() != null && filters.getPaymentMethods().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (PaymentMethodFilter filter : filters.getPaymentMethods()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("paymentMethodId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("paymentMethods", jsonArray);
      }

      //Filtro de ruta optima
      if (filters.getOptimalRoute() != null && !filters.getOptimalRoute().isEmpty()) {
        filtersJson.put("optimalRoute", filters.getOptimalRoute());
      }

      customerJson.put("filters", filtersJson);
    }

    return CoreConnection.postListRequest(URLConnections.URL_GET_ID_ORDERS_ISSUED20, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Get id orders issued 1.5
   * @param orderRequest
   * @return json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getIdOrdersEmitidas15", path = "/orderMonitorEndpoint/getIdOrdersEmitidas15", httpMethod = ApiMethod.HttpMethod.POST)
  public List<JSONObject> getIdOrdersEmitidas15(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getEmployeeNumber() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NUMBER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    //Filtros
    JSONObject filtersJson = new JSONObject();

    if (orderRequest.getFilters() != null) {
      FilterOrder filters = orderRequest.getFilters();

      //Filtro de tiendas
      if (filters.getStores() != null && filters.getStores().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (StoreFilter filter : filters.getStores()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("storeId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("stores", jsonArray);
      }

      //Filtro de ciudades
      if (filters.getCities() != null && filters.getCities().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (CityFilter filter : filters.getCities()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("cityId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("cities", jsonArray);
      }

      //Filtro de couries
      if (filters.getCouriers() != null && filters.getCouriers().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (CourierFilter filter : filters.getCouriers()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("courierId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("couriers", jsonArray);
      }

      //Filtro de estados de la orden
      if (filters.getOrderStatusFilters() != null && filters.getOrderStatusFilters().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (OrderStatusFilter filter : filters.getOrderStatusFilters()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("orderStatus", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("orderStatus", jsonArray);
      }

      //Filtro de metodos de pago
      if (filters.getPaymentMethods() != null && filters.getPaymentMethods().size() > 0) {
        JSONArray jsonArray = new JSONArray();
        for (PaymentMethodFilter filter : filters.getPaymentMethods()) {
          if (filter.getId() != null) {
            JSONObject json = new JSONObject();
            json.put("paymentMethodId", filter.getId());
            jsonArray.add(json);
          }
        }
        filtersJson.put("paymentMethods", jsonArray);
      }

      //Filtro de ruta optima
      if (filters.getOptimalRoute() != null && !filters.getOptimalRoute().isEmpty()) {
        filtersJson.put("optimalRoute", filters.getOptimalRoute());
      }

      customerJson.put("filters", filtersJson);
    }

    return CoreConnection.postListRequest(URLConnections.URL_GET_ID_ORDERS_ISSUED15, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Get distance in KM order 2.0
   * @param orderId
   * @return distance
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   * @throws WriterException
   */
  @ApiMethod(name = "getOrderCoordinates20", path = "/orderMonitorEndpoint/getOrderCoordinates20", httpMethod = ApiMethod.HttpMethod.GET)
  public OrderCoordinatesResponse getOrderCoordinates20(@Named("orderId") final Long orderId)
          throws ConflictException, BadRequestException, IOException, InternalServerErrorException, WriterException {

    OrderCoordinatesResponse response = new OrderCoordinatesResponse();
    final double[] distance = {0};

    JSONObject orderJson = new JSONObject();
    orderJson.put("orderId", orderId);

    List<OrderCoordinates> orderCoordinatesList = CoreConnection.postListRequest(URLConnections.URL_GET_ORDERS_COODINATES20,
            orderJson.toJSONString(), OrderCoordinates.class);

    if (orderCoordinatesList != null && orderCoordinatesList.size() > 0) {

      Collections.sort(orderCoordinatesList, (z1, z2) -> {
        if (z1.getNumber() > z2.getNumber())
          return 1;
        if (z1.getNumber() < z2.getNumber())
          return -1;
        return 0;
      });

      final OrderCoordinates[] coordinates = {null};

      orderCoordinatesList.forEach( x -> {
        if (coordinates[0] == null)
          coordinates[0] = x;
        else if (coordinates[0].getLatitude() != null &&
                coordinates[0].getLongitude() != null && x.getLatitude() != null && x.getLongitude() != null) {
          double result = LocationMethods.distanceInKm(Double.parseDouble(coordinates[0].getLatitude()),
                   Double.parseDouble(coordinates[0].getLongitude()), Double.parseDouble(x.getLatitude()),
                   Double.parseDouble(x.getLongitude()));
          distance[0] = distance[0] + result;
          coordinates[0] = x;
        }
      });
    }
    response.setDistance(round(distance[0], 3));
    return response;
  }

  /**
   * Get distance in KM order 1.5
   * @param orderId
   * @return distance
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   * @throws WriterException
   */
  @ApiMethod(name = "getOrderCoordinates15", path = "/orderMonitorEndpoint/getOrderCoordinates15", httpMethod = ApiMethod.HttpMethod.GET)
  public OrderCoordinatesResponse getOrderCoordinates15(@Named("orderId") final Long orderId)
          throws ConflictException, BadRequestException, IOException, InternalServerErrorException, WriterException {

    OrderCoordinatesResponse response = new OrderCoordinatesResponse();
    final double[] distance = {0};

    JSONObject orderJson = new JSONObject();
    orderJson.put("orderId", orderId);

    List<OrderCoordinates> orderCoordinatesList = CoreConnection.postListRequest(URLConnections.URL_GET_ORDERS_COODINATES15,
            orderJson.toJSONString(), OrderCoordinates.class);

    if (orderCoordinatesList != null && orderCoordinatesList.size() > 0) {

      Collections.sort(orderCoordinatesList, (z1, z2) -> {
        if (z1.getNumber() > z2.getNumber())
          return 1;
        if (z1.getNumber() < z2.getNumber())
          return -1;
        return 0;
      });

      final OrderCoordinates[] coordinates = {null};

      orderCoordinatesList.forEach( x -> {
        if (coordinates[0] == null)
          coordinates[0] = x;
        else if (coordinates[0].getLatitude() != null &&
                coordinates[0].getLongitude() != null && x.getLatitude() != null && x.getLongitude() != null) {
          double result = LocationMethods.distanceInKm(Double.parseDouble(coordinates[0].getLatitude()),
                  Double.parseDouble(coordinates[0].getLongitude()), Double.parseDouble(x.getLatitude()),
                  Double.parseDouble(x.getLongitude()));
          distance[0] = distance[0] + result;
          coordinates[0] = x;
        }
      });
    }
    response.setDistance(round(distance[0], 3));
    return response;
  }

  public static double round(double value, int places) {
    if (places < 0) throw new IllegalArgumentException();

    long factor = (long) Math.pow(10, places);
    value = value * factor;
    long tmp = Math.round(value);
    return (double) tmp / factor;
  }

  /**
   * Activate order canceled
   * @param orderRequest
   * @return json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "activateOrderCanceled", path = "/orderMonitorEndpoint/activateOrderCanceled", httpMethod = ApiMethod.HttpMethod.POST)
  public JSONObject activateOrderCanceled(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getEmployeeNumber() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NUMBER);

    if (orderRequest == null || orderRequest.getOrderId() == null)
      throw new ConflictException(Constants.ERROR_ID_ORDER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("orderId", orderRequest.getOrderId());
    customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());

    OrderActived orderActived = CoreConnection.postRequest(URLConnections.URL_ACTIVATE_ORDER_CANCELED, customerJson.toJSONString(), OrderActived.class);

    if (orderActived != null) {
      JSONObject orderJson = new JSONObject();
      orderJson.put("employeeNumber", orderRequest.getEmployeeNumber());
      orderJson.put("orderIdCanceled", orderRequest.getOrderId());
      orderJson.put("orderIdActivated", orderActived.getId());

      return CoreConnection.postRequest(URLConnections.URL_SAVE_ORDER_ACTIVATED_ASSOCIATION, orderJson.toJSONString(), JSONObject.class);
    }
    return new JSONObject();
  }

  /**
   * Get order activated association
   * @param orderRequest
   * @return
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getOrderActivedAssociation", path = "/orderMonitorEndpoint/getOrderActivedAssociation", httpMethod = ApiMethod.HttpMethod.POST)
  public JSONObject getOrderActivedAssociation(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getEmployeeNumber() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NUMBER);

    if (orderRequest == null || orderRequest.getOrderId() == null)
      throw new ConflictException(Constants.ERROR_ID_ORDER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());
    customerJson.put("orderId", orderRequest.getOrderId());

    return CoreConnection.postRequest(URLConnections.URL_GET_ORDERS_ACTIVATED_ASOCIATION, customerJson.toJSONString(), JSONObject.class);
  }

  /**
   * Get order canceled association
   * @param orderRequest
   * @return
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getOrderCanceledAssociation", path = "/orderMonitorEndpoint/getOrderCanceledAssociation", httpMethod = ApiMethod.HttpMethod.POST)
  public JSONObject getOrderCanceledAssociation(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (orderRequest == null || orderRequest.getEmployeeNumber() == null)
      throw new ConflictException(Constants.ERROR_EMPLOYEE_NUMBER);

    if (orderRequest == null || orderRequest.getOrderId() == null)
      throw new ConflictException(Constants.ERROR_ID_ORDER);

    JSONObject customerJson = new JSONObject();
    customerJson.put("employeeNumber", orderRequest.getEmployeeNumber());
    customerJson.put("orderId", orderRequest.getOrderId());

    return CoreConnection.postRequest(URLConnections.URL_GET_ORDERS_CANCELED_ASOCIATION, customerJson.toJSONString(), JSONObject.class);
  }

    /**
     * Send mail no express
     * @param orderRequest
     * @return answer
     * @throws ConflictException
     * @throws BadRequestException
     * @throws IOException
     * @throws InternalServerErrorException
     */
  @ApiMethod(name = "sendMailNoExpress", path = "/orderMonitorEndpoint/sendMailNoExpress", httpMethod = ApiMethod.HttpMethod.POST)
  public Answer sendMailNoExpress(
            final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

        if (orderRequest == null || orderRequest.getOrderId() == null)
            throw new ConflictException(Constants.ERROR_ID_ORDER);

        if (orderRequest == null || orderRequest.getOrderGuide() == null)
            throw new ConflictException(Constants.ERROR_ORDER_GUIDE);

        Answer answer = new Answer();
        answer.setConfirmation(false);

        String courierName = null;
        String tracking = "";
        String trackingEmail = "";

        switch (orderRequest.getCourierId()) {
            case 5:
                courierName = "SERVIENTREGA";
                tracking = " " + Constants.TRACKING_SERVIENTREGA;
                trackingEmail = Constants.TRACKING_SERVIENTREGA;
                break;
            case 11:
                courierName = "LIBERTY";
                tracking = " " + Constants.TRACKING_LIBERTY;
                trackingEmail = Constants.TRACKING_LIBERTY;
                break;
            case 12:
                courierName = "CARGONAM";
                trackingEmail = "";
                break;
        }

        //SEND EMAIL
        if (courierName != null && !courierName.isEmpty()) {
            JSONObject mailJson = new JSONObject();
            mailJson.put("orderId", orderRequest.getOrderId());
            mailJson.put("orderGuide", orderRequest.getOrderGuide());
            mailJson.put("courier", courierName);
            mailJson.put("urlTracking", trackingEmail);
            mailJson.put("employeeNumber", orderRequest.getEmployeeNumber());
            CoreConnection.postRequest(URLConnections.URL_SEND_MAIL_NO_EXPRESS, mailJson.toJSONString(), Void.class);
            answer.setConfirmation(true);
        }

        return answer;
  }

  /**
   * Update payment method order
   * @param orderRequest
   * @return Answer
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "updateOrderPaymentMethod", path = "/orderMonitorEndpoint/updateOrderPaymentMethod", httpMethod = ApiMethod.HttpMethod.PUT)
  public Answer updateOrderPaymentMethod(
          final OrderRequest orderRequest) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    if (Objects.isNull(orderRequest) || Objects.isNull(orderRequest.getOrderId()))
      throw new ConflictException(Constants.ERROR_ID_ORDER);
    else if (Objects.isNull(orderRequest.getUuid()) || orderRequest.getUuid().isEmpty())
      throw new ConflictException(Constants.ERROR_ORDER_UUID);
    else if (Objects.isNull(orderRequest.getPaymentMethodId()))
      throw new ConflictException(Constants.ERROR_ORDER_PAYMENT_METHOD);

    Answer answer = new Answer();
    answer.setConfirmation(false);

    JSONObject objectJson = new JSONObject();
    objectJson.put("order_no", orderRequest.getOrderId());
    objectJson.put("uuid", orderRequest.getUuid());
    objectJson.put("payment", orderRequest.getPaymentMethodId());



    Response<GenericResponse> response= ApiGatewayService.get().updateOrderPaymentMethod(new DeliveryOrderStatus(orderRequest.getOrderId().toString(), orderRequest.getUuid(), orderRequest.getPaymentMethodId()));

    if(response.isSuccessful()){
      answer.setConfirmation(true);
    }else{
      answer.setMessage("No se pudo cambiar el método de pago, por favor intente más tarde!");
      if(Objects.nonNull(response.message()) && !response.message().isEmpty()){
        answer.setMessage(response.message());
      }
      answer.setConfirmation(false);
    }
    return answer;
  }

  @ApiMethod(name = "updateOrderExpressPickingDate", path = "/orderMonitorEndpoint/updateOrderExpressPickingDate", httpMethod = ApiMethod.HttpMethod.POST)
  public JSONObject updateOrderExpressPickingDate(final PickingDateRequestDomain request)
          throws BadRequestException, IOException, InternalServerErrorException, ParseException, ConflictException {
    log.info("method: updateOrderExpressPickingDate({},{})" + request);
    log.info("URLConnections.URL_CUSTOMER:" + URLConnections.URL_UPDATE_ORDER_EXPRESS_PICKING_DATE);

    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
    now = now.minus(5, ChronoUnit.HOURS);
    Date date1=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(request.getPickingDate());
    OffsetDateTime pickingDate = date1.toInstant().atOffset(ZoneOffset.UTC);

    if(pickingDate.isBefore(now))
      throw new BadRequestException(Constants.INVALID_DATE);//+pickingDate+ " - "+now.toString());

    JSONObject json = new JSONObject();
    json.put("orderId", request.getOrderId());
    json.put("employeeNumber", request.getEmployeeNumber());
    json.put("correoUsuario", request.getCorreoUsuario());
    json.put("rol", request.getRol());
    json.put("employeeName", request.getEmployeeName());
    json.put("pickingDate", request.getPickingDate());

    log.info("method: updateOrderExpressPickingDate JSON:" + json);
    JSONObject object = CoreConnection.postRequest(URLConnections.URL_UPDATE_ORDER_EXPRESS_PICKING_DATE, json.toJSONString(), JSONObject.class);
    Gson gson = new Gson();
    TracingResponse tracingResponse = gson.fromJson(object.toString(), TracingResponse.class);

    if (tracingResponse != null) {
      JSONObject orderJson = new JSONObject();
      orderJson.put("employeeNumber", request.getEmployeeNumber());
      orderJson.put("orderIdCanceled", request.getOrderId());
      orderJson.put("orderIdActivated", tracingResponse.getId());

      CoreConnection.postRequest(URLConnections.URL_SAVE_ORDER_ACTIVATED_ASSOCIATION, orderJson.toJSONString(), JSONObject.class);
    }

    return object;
  }


  /**
   * Get couries
   * @return json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getDeliveryOrdersScanAndGo", path = "/orderMonitorEndpoint/getDeliveryOrdersScanAndGo", httpMethod = ApiMethod.HttpMethod.GET)
  public List<OrderScanAndGo> getDeliveryOrdersScanAndGo(@Named("customerId") final int customerId) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    User user = ofy().load().type(User.class).filter("id", customerId).first().now();
    if (user == null || user.getIdUser() == null)
      throw new ConflictException(Constants.USER_NOT_FOUND);

    Key<User> userKeys = Key.create(User.class, user.getIdUser());
    List<OrderScanAndGo> listOrderScanAndGo  = new ArrayList<>();
    List<DeliveryOrder> deliveryOrders;
    //COMO SE HACIA ANTERIORMENTE
    /*deliveryOrders = ofy().load().type(DeliveryOrder.class).filter("deliveryType", "SCANANDGO")
            .filter("idOrder", 0)
            .list();*/

    deliveryOrders = ofy().load().type(DeliveryOrder.class).filter("deliveryType", "SCANANDGO")
            .filter("idOrder", 0).ancestor(Ref.create(userKeys)).list();

    for (DeliveryOrder order: deliveryOrders) {
      OrderScanAndGo orderScanAndGo = new OrderScanAndGo();
      Key<DeliveryOrder> deliveryOrderKey = Key.create(order.getIdCustomer().getKey(), DeliveryOrder.class, order.getIdDeliveryOrder());
      List<DeliveryOrderItem> deliveryOrderItem = new ArrayList<>();
      deliveryOrderItem = ofy().load().type(DeliveryOrderItem.class).filter("scanAndGo",true)
              .ancestor(Ref.create(deliveryOrderKey)).list();

      log.info("### DeliveryOrderItem key -> "+deliveryOrderItem);
      List<OrderItemScanAndGo> listaItems = new ArrayList<>();
      for (DeliveryOrderItem itemOrder : deliveryOrderItem) {
        // Migrate service To backend 3
        //String url = URLConnections.URL_ITEM_ID + "/" + itemOrder.getId();
        //JSONObject jsonObject = CoreConnection.getRequest(url, JSONObject.class);
        Item item = ApiGatewayService.get().getItemById(itemOrder.getId());
        OrderItemScanAndGo itemScanAndGo = new OrderItemScanAndGo();
        itemScanAndGo.setItem(item);
        itemScanAndGo.setQuantitySold(itemOrder.getQuantitySold());
        listaItems.add(itemScanAndGo);
      }

      //String urlString = URLConnections.URL_CUSTOMER + user.getId();
      Optional<CustomerJSON> optionalCustomerJSON = ApiGatewayService.get().getCustomerById(user.getId());

      if (!optionalCustomerJSON.isPresent()){
        throw new ConflictException(Constants.USER_NOT_FOUND);
      }
      CustomerJSON customerJSON = optionalCustomerJSON.get();
      //customerJSON = CoreConnection.getRequest(urlString, CustomerJSON.class);
      orderScanAndGo.setDeliveryOrder(order);
      orderScanAndGo.setCustomer(customerJSON);
      orderScanAndGo.setListOrderItem(listaItems);
      listOrderScanAndGo.add(orderScanAndGo);
    }

    return listOrderScanAndGo;
  }

  /**
   * Get couries
   * @return json response
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   */
  @ApiMethod(name = "getDeliveryOrderItemsScanAndGo", path = "/orderMonitorEndpoint/getDeliveryOrderItemsScanAndGo", httpMethod = ApiMethod.HttpMethod.GET)
  public List<DeliveryOrderItem> getDeliveryOrderItemsScanAndGo() throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

    List<DeliveryOrderItem> DeliveryOrderItem;
    DeliveryOrderItem = ofy().load().type(DeliveryOrderItem.class).filter("scanAndGo", true).list();
    return DeliveryOrderItem;
  }


  @ApiMethod(name = "getEvidencesOrder20", path = "/orderMonitorEndpoint/getEvidencesOrder20", httpMethod = ApiMethod.HttpMethod.POST)
  public JSONObject getEvidencesOrder20(final GetEvidencesOrderRequestDomain request)
          throws BadRequestException, IOException, InternalServerErrorException, ParseException, ConflictException {
    log.info("method: getEvidencesOrder20({},{})" + request);
    log.info("URLConnections.URL_GET_EVIDENCES_ORDER20:" + URLConnections.URL_GET_EVIDENCES_ORDER20);


    JSONObject json = new JSONObject();
    json.put("orderId", request.getOrderId());
    json.put("courierId", request.getCourierId());
    json.put("employeeNumber", request.getEmployeeNumber());

    log.info("method: updateOrderExpressPickingDate JSON:" + json);
    return CoreConnection.postRequest(URLConnections.URL_GET_EVIDENCES_ORDER20, json.toJSONString(), JSONObject.class);
  }



}

