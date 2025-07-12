package com.imaginamos.farmatodo.backend.order.create_order.infraestructure;

/**
 * Created by USUARIO on 20/01/2017
 */

import com.algolia.search.exceptions.AlgoliaException;
import com.google.api.server.spi.config.*;
import com.google.api.server.spi.response.*;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.apphosting.api.ApiProxy;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Ref;
import com.imaginamos.farmatodo.backend.Prime.PrimeUtil;
import com.imaginamos.farmatodo.backend.algolia.GuardAlgolia;
import com.imaginamos.farmatodo.backend.customer.CustomerCreditCard;
import com.imaginamos.farmatodo.backend.customer.CustomerEndpoint;
import com.imaginamos.farmatodo.backend.customer.Customers;
import com.imaginamos.farmatodo.backend.customerAddress.RedZoneService;
import com.imaginamos.farmatodo.backend.firebase.FirebaseNotification;
import com.imaginamos.farmatodo.backend.firebase.api.FirebaseService;
import com.imaginamos.farmatodo.backend.location.LocationMethods;
import com.imaginamos.farmatodo.backend.order.DateConstants;
import com.imaginamos.farmatodo.backend.order.OrderFinalizeRes;
import com.imaginamos.farmatodo.backend.order.PSEResponseData;
import com.imaginamos.farmatodo.backend.order.create_order.domain.*;
import com.imaginamos.farmatodo.backend.product.ProductsMethods;
import com.imaginamos.farmatodo.backend.stock.StockMethods;
import com.imaginamos.farmatodo.backend.user.Authenticate;
import com.imaginamos.farmatodo.backend.user.Users;
import com.imaginamos.farmatodo.backend.util.CoreConnection;
import com.imaginamos.farmatodo.backend.util.MsgUtilAlgolia;
import com.imaginamos.farmatodo.backend.util.ResilienceManager;
import com.imaginamos.farmatodo.backend.util.TraceUtil;
import com.imaginamos.farmatodo.model.OptimalRoute.*;
import com.imaginamos.farmatodo.model.algolia.*;
import com.imaginamos.farmatodo.model.algolia.cuponFilters.Campaign;
import com.imaginamos.farmatodo.model.algolia.cuponFilters.CouponFiltersConfig;
import com.imaginamos.farmatodo.model.algolia.cuponFilters.MessagesError;
import com.imaginamos.farmatodo.model.algolia.cuponFilters.Variables;
import com.imaginamos.farmatodo.model.algolia.delivery.CampaignFree;
import com.imaginamos.farmatodo.model.algolia.delivery.DeliveryFree;
import com.imaginamos.farmatodo.model.algolia.delivery.VariablesFree;
import com.imaginamos.farmatodo.model.algolia.eta.ETAConfig;
import com.imaginamos.farmatodo.model.algolia.messageconfig.MsgSmsEnum;
import com.imaginamos.farmatodo.model.algolia.tips.DefaultTipsByCity;
import com.imaginamos.farmatodo.model.algolia.tips.ItemTip;
import com.imaginamos.farmatodo.model.algolia.tips.Tip;
import com.imaginamos.farmatodo.model.algolia.tips.TipConfig;
import com.imaginamos.farmatodo.model.braze.BrazeEventCreate;
import com.imaginamos.farmatodo.model.braze.BrazeProperties;
import com.imaginamos.farmatodo.model.coupon.Coupon;
import com.imaginamos.farmatodo.model.coupon.CouponValidation;
import com.imaginamos.farmatodo.model.coupon.CustomerCoupon;
import com.imaginamos.farmatodo.model.coupon.ErrorCouponMsg;
import com.imaginamos.farmatodo.model.customer.*;
import com.imaginamos.farmatodo.model.item.AddDeliveryOrderItemRequest;
import com.imaginamos.farmatodo.model.item.OpticalItemFilter;
import com.imaginamos.farmatodo.model.item.TtlCacheAlgoliaRecommendRes;
import com.imaginamos.farmatodo.model.location.City;
import com.imaginamos.farmatodo.model.optics.ShipmentType;
import com.imaginamos.farmatodo.model.order.CustomerInfo;
import com.imaginamos.farmatodo.model.order.*;
import com.imaginamos.farmatodo.model.payment.*;
import com.imaginamos.farmatodo.model.product.Item;
import com.imaginamos.farmatodo.model.product.Substitutes;
import com.imaginamos.farmatodo.model.provider.ProviderOrder;
import com.imaginamos.farmatodo.model.store.Store;
import com.imaginamos.farmatodo.model.talonone.CouponAutomaticTalon;
import com.imaginamos.farmatodo.model.talonone.DeductDiscount;
import com.imaginamos.farmatodo.model.user.BlockedUser;
import com.imaginamos.farmatodo.model.user.Credential;
import com.imaginamos.farmatodo.model.user.Token;
import com.imaginamos.farmatodo.model.user.User;
import com.imaginamos.farmatodo.model.util.*;
import com.imaginamos.farmatodo.networking.algolia.APIAlgolia;
import com.imaginamos.farmatodo.networking.cache.CachedBack3DataManager;
import com.imaginamos.farmatodo.networking.cache.CachedDataManager;
import com.imaginamos.farmatodo.networking.growthbook.GrowthBookConfigLoader;
import com.imaginamos.farmatodo.networking.models.addresses.SendSMSCloudFunctionReq;
import com.imaginamos.farmatodo.networking.models.algolia.OrderMessageConfiguration;
import com.imaginamos.farmatodo.networking.models.algolia.StatusMessageConfig;
import com.imaginamos.farmatodo.networking.models.amplitude.AmplitudeSessionRequest;
import com.imaginamos.farmatodo.networking.models.braze.PushNotificationRequest;
import com.imaginamos.farmatodo.networking.services.ApiGatewayService;
import com.imaginamos.farmatodo.networking.services.CloudFunctionsService;
import com.imaginamos.farmatodo.networking.services.OpticsServices;
import com.imaginamos.farmatodo.networking.talonone.FarmaCredits;
import com.imaginamos.farmatodo.backend.order.create_order.domain.TalonOneComboService;
import com.imaginamos.farmatodo.networking.talonone.TalonOneService;
import com.imaginamos.farmatodo.networking.talonone.model.*;
import com.imaginamos.farmatodo.networking.util.Util;
import io.jsonwebtoken.*;
import org.apache.oltu.oauth2.as.issuer.MD5Generator;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuer;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.jasypt.util.password.StrongPasswordEncryptor;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.simple.JSONObject;
import org.springframework.util.StringUtils;
import retrofit2.Response;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.vavr.control.Try;


import javax.servlet.http.HttpServletRequest;
import java.io.EOFException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.imaginamos.farmatodo.backend.OfyService.ofy;
import static com.imaginamos.farmatodo.backend.order.create_order.domain.OrderUtil.getOrderMethod;


/**
 * @Api - Google indicator that the present class ins an Api.
 * Every further indication of @ApiMethod includes this as a Google API.
 */
@Api(name = "orderEndpoint",
        version = "v1",
        apiKeyRequired = AnnotationBoolean.TRUE,
        scopes = {Constants.EMAIL_SCOPE},
        clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID_DEVELOPER, com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID},
        audiences = {Constants.ANDROID_AUDIENCE},
        namespace = @ApiNamespace(ownerDomain = Constants.OWNER_DOMAIN, ownerName = Constants.OWNER_NAME, packagePath = ""),
        description = "Stores order for all pages.")
public class OrderEndpoint {

    private static final Logger LOG = Logger.getLogger(OrderEndpoint.class.getName());
    public static final int ID_BD_REDIS_DISCOUNT = 14;
    public static final String APP_BUILD_CODE_HEADER_NAME = "appbuildcode";
    public static final String IS_INTEGER_POSITIVE_REGEX = "^[1-9]\\d*$";
    private static final String TIMEZONE_OFFSET_CO = "-0500";

    private Authenticate authenticate;
    private Users users;
    private Customers customers;
    private Orders orders;
    private ProductsMethods productsMethods;
    private StockMethods stockMethods;
    private OrderMethods orderMethods;
    private final FTDUtilities ftdUtilities;
    private final TalonOneComboService talonOneComboService;

    private PrimeUtil primeUtil;

    private static String ETA_IN_MINUTES = "ETAInMinutes";
    private static String ETA_IN_LONG_TIMESTAMP = "ETAInLongTimestamp";

    private final String ORDER_CREATED = "order_created";
    private final String ORDER_ASSIGNED = "order_assigned";
    private final String ORDER_INCOMING = "order_incoming";
    private final String ORDER_DELIVERED = "order_delivered";
    private final String ORDER_FINALIZED = "order_finalized";
    private final String ORDER_CANCELED = "order_canceled";
    private final String ORDER_RETURNED = "order_returned";
    private final String ORDER_ON_THE_WAY = "order_on_the_way";

    // status order tracking
    private final Long STATUS_PAYMENT_SUCCESS = 12L;
    private final Long STATUS_ORDER_CREATED = 0L;

    private static final int SINGLE_ITEM = 1;
    private static final int FIRST_ITEM_INDEX = 0;

    private static final int DEFAULT_ITEM_ID = 0;
    private static final int DEFAULT_SCAN_AND_GO_STOCK = 1000;
    private static final int MIN_VALID_SCAN_AND_GO_STOCK = 1;
    private static final int MAX_SUBSTITUTES = 10;
    private static final int PERCENTAGE_MIN_FOR_NOT_SUBSTITUTE = 100;

    public OrderEndpoint() {
        authenticate = new Authenticate();
        orders = new Orders();
        users = new Users();
        customers = new Customers();
        productsMethods = new ProductsMethods();
        stockMethods = new StockMethods();
        orderMethods = new OrderMethods();
        ftdUtilities = new FTDUtilities();
        primeUtil = new PrimeUtil();
        talonOneComboService = new TalonOneComboService(this);
    }

    @ApiMethod(name = "createOrder", path = "/orderEndpoint/createOrder", httpMethod = ApiMethod.HttpMethod.POST)
    public CreatedOrder createOrder(
            @Named("idCustomerWebSafe") final String idCustomerWebSafe,
            @Named("token") final String token,
            @Named("tokenIdWebSafe") final String tokenIdWebSafe,
            DeliveryOrder order,
            HttpServletRequest httpServletRequest) throws BadRequestException, ConflictException, IOException, AlgoliaException, UnauthorizedException {
        LOG.info("request -> " + order.toStringJson());


        Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCacheIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
        if (jsonCachedOptional.isPresent()) {
            throw new ConflictException("Tu solicitud ya se encuentra en proceso.");
        }
        CachedDataManager.saveJsonInCacheIndexTime(String.valueOf(order.getIdFarmatodo()), new Gson().toJson(order), Constants.ID_INDEX_SAVE_AND_GET_REDIS, Constants.TIME_SAVE_REDIS_AND_SECONDS);

        //el sleep es para replicar los casos donde sucede el problema, (cuando se demora el servicio) se quitara cuando se terminen las pruebas
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (!authenticate.isValidToken(token, tokenIdWebSafe)) {
            CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
            throw new ConflictException(Constants.INVALID_TOKEN);
        }


        AlgoliaExploStore algoliaExploStore = APIAlgolia.getActiveStoreExpo();
        if (Objects.equals(order.getIdStoreGroup(), "83") && algoliaExploStore.isEnable()) {
            order.setIdStoreGroup("86");
        }

        boolean isScanAndGo = isOrdenScanAndGo(order);
        boolean isPrime = isOrderPrime(order);
        boolean isPrimeMixed = isOrderPrimeMixed(order);
        final Long typeSubscription = getTypeSubscriptionPrime(order);
        LOG.info("es orden prime -> " + isPrime + " mixta ? -> " + isPrimeMixed);

        CreatedOrder createdOrderOnlyPrime = null;

        if (!isScanAndGo && order.getIdAddress() == 0 && !isPrime) {
            CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
            throw new ConflictException("Direccion invalida");
        }


        if (order.getPaymentType().getId() == PaymentTypeEnum.TRANSACCIONES_EN_LINEA.getId() && order.getPaymentCardId() <= 0) {
            CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
            throw new ConflictException("PaymentCardId es obligatorio.");
        }


        DeliveryOrder deliveryOrderData = new DeliveryOrder();

        Key<Customer> customerKey = Key.create(idCustomerWebSafe);
        CreatedOrder orderJSON = new CreatedOrder();
        //LOG.warning(order.getCustomerPhone());
        DeliveryOrder deliveryOrderSavedShoppingCart = ofy()
                .load()
                .type(DeliveryOrder.class)
                .filter("currentStatus", 1)
                .ancestor(Ref.create(customerKey))
                .first()
                .now();

        //prepare talon one data
        new TalonAttributes().addTalonAttributes(order);

        double saving = 0;
        boolean validateUserPrime = false;
        try {
            validateUserPrime = validateUserPrime((long) order.getIdFarmatodo());
            deliveryOrderSavedShoppingCart.setFarmaCredits(order.getFarmaCredits());
            deliveryOrderSavedShoppingCart.setTalonOneData(order.getTalonOneData());
            saving = getSaving(
                    deliveryOrderSavedShoppingCart, httpServletRequest,
                    order.getIdFarmatodo(), order.getDeliveryType(),
                    order.getIdStoreGroup(),idCustomerWebSafe,
                    token,
                    tokenIdWebSafe);
        } catch (Exception e) {
            LOG.warning("Error saving order " + e.getMessage());
        }

        // validar fraude de cc, email, phone and TC

        CustomerFraudResponse customerFraudResponse = ApiGatewayService.get().searchFraudCustomer(Long.valueOf(order.getIdFarmatodo()));
        Boolean isfraud = Objects.isNull(customerFraudResponse.getFraud()) ? false : customerFraudResponse.getFraud();

        if (isfraud)
            throw new ConflictException("Usuario bloqueado no puede continuar");

        if (order.getPaymentType().getId() == PaymentTypeEnum.TRANSACCIONES_EN_LINEA.getId()) {
            CustomerFraudResponse customerFraudCreditCardResponse = ApiGatewayService.get().antifraudCreditCard(Long.valueOf(order.getPaymentCardId()));
            Boolean isfraudCreditCard = Objects.isNull(customerFraudCreditCardResponse.getFraud()) ? false : customerFraudCreditCardResponse.getFraud();
            if (isfraudCreditCard) {
                CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
                throw new ConflictException("Usuario bloqueado no puede continuar");
            }

        }

        if (isPrimeMixed) {
            boolean isFreeDeliveryOrder = isFreeDeliveryOrderPrime(idCustomerWebSafe, order, deliveryOrderSavedShoppingCart);
            DeliveryOrder orderPrime = order;
            orderPrime.getItemList().removeIf(item -> Objects.equals(item.getId(), 0L));
            List<DeliveryOrderItem> deliveryOrderItemList = ofy().load().type(DeliveryOrderItem.class).ancestor(deliveryOrderSavedShoppingCart).list();

            int primeQuantity = getTypeSubscriptionPrimeQuantity(deliveryOrderItemList);
            List<OrderItemsQuantity> itemsInCart = itemsInShoppingCart(deliveryOrderItemList);

            try {
                createdOrderOnlyPrime = createOrderPrime(idCustomerWebSafe, token, tokenIdWebSafe, orderPrime, httpServletRequest);
            } catch (Exception e) {
                addItemsNoPrime(idCustomerWebSafe, token, tokenIdWebSafe, itemsInCart, order, httpServletRequest);
                CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
                throw new ConflictException(Constants.ERROR_CREATE_ORDER);
            }

            DeliveryOrder orderNoPrime = new DeliveryOrder();
            orderNoPrime = order;
            orderNoPrime.setIdDeliveryOrder(UUID.randomUUID().toString());
            orderNoPrime.setIdCustomer(Ref.create(customerKey));
            orderNoPrime.setCurrentStatus(1);
            orderNoPrime.setCreateDate(new Date());
            orderNoPrime.setDeliveryType(order.getDeliveryType());
//             agregar de nuevo los items que no son prime
            LOG.info("Llamando metodo para agregar los items no PRIME idCustomerWebSafe-> " + idCustomerWebSafe);
//           agregar de nuevo los items que no son prime
            addItemsNoPrime(idCustomerWebSafe, token, tokenIdWebSafe, itemsInCart, order, httpServletRequest);

            LOG.info("SAVING -> " + saving);
            final Long orderPrimeID = Objects.isNull(createdOrderOnlyPrime.getId()) ? 0 : createdOrderOnlyPrime.getId();

            CreatedOrder createdOrderNoPrime = createOrderNoPrime(idCustomerWebSafe, token, tokenIdWebSafe, saving, validateUserPrime, typeSubscription, orderPrimeID, primeQuantity, isFreeDeliveryOrder, orderNoPrime, httpServletRequest);
            createdOrderNoPrime.setIdOrderPrime(orderPrimeID);
            try {
                //agrega el id de la orden prime para poder listar la orden prime en getOrders
                deliveryOrderSavedShoppingCart.setOrderPrimeId(orderPrimeID);
                ofy().save().entity(deliveryOrderSavedShoppingCart).now();
            } catch (Exception e) {
                LOG.info("No se pudo guardar el id de la orden prime -> " + orderPrimeID + "en datastore. Por -> " + e.getMessage());
            }

            FirebaseService.get().setOrderPrimeMix(String.valueOf(createdOrderOnlyPrime.getId()), String.valueOf(createdOrderNoPrime.getId()));
            //agrega el item prime nuevamente para que se mantega en el carrito
            addItemPrime(idCustomerWebSafe, token, tokenIdWebSafe, itemsInCart);

            LOG.info("para la orden mixta: " +
                    createdOrderNoPrime.getId() +
                    " primeGeneral es: " +
                    createdOrderNoPrime.isPrimeGeneral() +
                    " y el prime es: " + createdOrderNoPrime.isPrime() +
                    " y el id de la orden Prime es: " + createdOrderNoPrime.getIdOrderPrime());

            return createdOrderNoPrime;
        }

        LOG.info("Order isScanAndGo >>  " + isScanAndGo);
        LOG.info("Order isPrime >>  " + isPrime);

        if (deliveryOrderSavedShoppingCart != null) {
            LOG.warning("deliveryOrderSaved ->  " + deliveryOrderSavedShoppingCart.getIdDeliveryOrder());
        }
//        LOG.info("deliveryOrderSaved is null ->  " + (deliveryOrderSavedShoppingCart == null));
//        LOG.info("customerKey ->  " + customerKey.getString());

        // validar si el cliente esta bloqueado
        BlockedUser blockedUser = ofy().load().type(BlockedUser.class).filter("idUser", order.getIdFarmatodo()).first().now();

        boolean userBlocked = blockedUser != null;
//        LOG.info("usuario bloqueado -> " + userBlocked);


        customerFraudResponse = ApiGatewayService.get().searchFraudCustomer(Long.valueOf(order.getIdFarmatodo()));
        isfraud = Objects.isNull(customerFraudResponse.getFraud()) ? false : customerFraudResponse.getFraud();

        if (userBlocked || isfraud) {
            CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
            throw new ConflictException("Usuario bloqueado no puede continuar");
        }


        //TODO Se obtiene el id de la tienda mas cercana segun la direccion
        //List<Address> listAddress = customers.getAddressesFromCustomer(order.getIdFarmatodo());
        int closerStoreId = 0;
        if (!isScanAndGo || !isPrime) {
            AddressesRes addressesRes = ApiGatewayService.get().getAddressesByCustomerId(order.getIdFarmatodo());
            List<Address> listAddress = addressesRes.getAddresses();

            if (Objects.nonNull(listAddress)) {
                DeliveryOrder finalOrder = order;
                DeliveryOrder finalOrder2 = order;
                closerStoreId = listAddress.stream().filter(address -> Objects.nonNull(address) && Objects.nonNull(finalOrder.getDeliveryType()) && Objects.nonNull(address.getDeliveryType()) &&
                                address.getCloserStoreId() > 0 &&
                                finalOrder.getDeliveryType().getDeliveryType().equals(address.getDeliveryType().getDeliveryType()) &&
                                finalOrder.getIdAddress() == address.getIdAddress())
                        .mapToInt(address -> address.getCloserStoreId()).findFirst().isPresent() ? listAddress.stream().filter(address -> address.getCloserStoreId() > 0 &&
                                finalOrder2.getDeliveryType().getDeliveryType().equals(address.getDeliveryType().getDeliveryType()) &&
                                finalOrder2.getIdAddress() == address.getIdAddress())
                        .mapToInt(address -> address.getCloserStoreId()).findFirst().getAsInt() : 0;
            }
        } else {
            closerStoreId = order.getIdStoreGroup() == null ? 26 : Integer.valueOf(order.getIdStoreGroup());
        }

        LOG.info("closerStoreId prueba => " + closerStoreId);
        String storeIdToValidate = closerStoreId > 0 ? String.valueOf(closerStoreId) : order.getIdStoreGroup();
//        LOG.info("storeid: " + closerStoreId);

        final Long finalStoreToValidate = Long.parseLong(storeIdToValidate);
        boolean validateStores = false;

        //exclude stores
        ExcludeStoresCreateOrder excludeStoresCreateOrder = APIAlgolia.getStoresToExcludeCreateOrder();

        if (Objects.nonNull(excludeStoresCreateOrder.getEnableStores()) && !excludeStoresCreateOrder.getEnableStores().isEmpty()) {
//            LOG.info("stores to exclude -> " + excludeStoresCreateOrder.toString());
            for (Long storeExclude : excludeStoresCreateOrder.getEnableStores()) {
                if (storeExclude.equals(finalStoreToValidate)) {
                    validateStores = true;
                }
            }
        }

        // siempre estaran disponibles a menos que se encuentre en la lista
        Boolean isStoreAvailable = true;
//        LOG.info("go to validateStores if open or close -> " + validateStores);
        if (validateStores && !isScanAndGo) {
            isStoreAvailable = this.isStoreAvailable(storeIdToValidate, order.getPickingDate());
        }

        // Fix Validacion PickingDate
        if (Objects.nonNull(order.getPickingDate())) {
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            now = now.minus(5, ChronoUnit.HOURS);
            OffsetDateTime pickingDate = order.getPickingDate().toInstant().atOffset(ZoneOffset.UTC);
            if (pickingDate.isBefore(now)) {
                CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
                throw new ConflictException("La fecha de programación de la orden no es válida ");//+pickingDate+ " - "+now.toString());
            }
        }

        if (!isStoreAvailable)
            throw new ConflictException(Constants.CLOSED_STORE);

        if (!Objects.isNull(order.getDeliveryType())) {
            if (order.getDeliveryType().getDeliveryType().equals(DeliveryType.NATIONAL.getDeliveryType())
                    && closerStoreId != URLConnections.NATIONAL_ID_STORE) {
                CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
                throw new ConflictException("Tienda no coincide con tipo de envio Nacional");
            }

            if (order.getDeliveryType().getDeliveryType().equals(DeliveryType.ENVIALOYA.getDeliveryType())
                    && closerStoreId != URLConnections.ENVIALOYA_ID_STORE) {
                CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
                throw new ConflictException("Tienda no coincide con tipo de envio Envialo ya");
            }
        }

        if (deliveryOrderSavedShoppingCart == null) {
            CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
            throw new ConflictException(Constants.ERROR_CREATE_ORDER);
        }

        List<DeliveryOrderItem> deliveryOrderItemList = ofy().load().type(DeliveryOrderItem.class).ancestor(deliveryOrderSavedShoppingCart).list();

        if (deliveryOrderItemList == null || deliveryOrderItemList.isEmpty()) {
            CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
            throw new ConflictException(Constants.ERROR_CREATE_ORDER);
        }


        // Fix items Duplicados
        validateDuplicateItems(deliveryOrderItemList);

        // NEW FIX

        //  obtener store and delivery type from address.!
        if (!isScanAndGo && !isPrime) {
            Key<User> userKey = Key.create(idCustomerWebSafe);
            User user = users.findUserByKey(userKey);
            if (user == null || user.getId() == 0) {
                CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
                throw new UnauthorizedException(Constants.USER_NOT_FOUND);
            }


            List<Address> allAddresses = this.customers.getAddressesByIdCustomer(user);

            Optional<Address> optionalAddress = Optional.empty();
            try {
                DeliveryOrder finalOrder1 = order;
                optionalAddress = allAddresses
                        .stream()
                        .filter(address -> address.getIdAddress().equals(finalOrder1.getIdAddress()))
                        .findFirst();
            } catch (Exception e) {
                LOG.warning("Error@createOrder filtering and finding address " + e.getMessage());
            }

            if (optionalAddress.isPresent()) {

                Address addressToCreateOrder = optionalAddress.get();
//                LOG.info("address -> " + addressToCreateOrder.toString());
                if (addressToCreateOrder.getDeliveryType() != null && addressToCreateOrder.getCloserStoreId() > 0) {
                    // SET NEW DELIVERY_TYPE AND STORE
                    order.setDeliveryType(addressToCreateOrder.getDeliveryType());
                    order.setIdStoreGroup(String.valueOf(addressToCreateOrder.getCloserStoreId()));

                } else {
                    CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
                    throw new ConflictException(Constants.ERROR_ADDRESS_CREATE_ORDER);
                }
            } else {
                CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
                throw new ConflictException(Constants.ERROR_ADDRESS_CREATE_ORDER);
            }

            LOG.info("fix new deliveryType -> " + order.getDeliveryType() + ", new store -> " + order.getIdStoreGroup());
        }

        // ScanAndGo Order
        List<DeliveryOrderItem> itemsScanAndGo = new ArrayList<>(deliveryOrderItemList);
        List<DeliveryOrderItem> itemsNewOrder = new ArrayList<>();
        if (isScanAndGo) {
            //set store princial when is scan and go
            itemsNewOrder = Objects.nonNull(itemsScanAndGo) ? itemsScanAndGo.stream().filter(it -> (Objects.isNull(it.getScanAndGo()) || !it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon())).collect(Collectors.toList()) : null;
            if (Objects.nonNull(itemsScanAndGo) && itemsScanAndGo.stream()
                    .filter(it -> (Objects.isNull(it.getScanAndGo()) || !it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon())).findFirst().isPresent()) {
                itemsScanAndGo.removeIf(it -> ((Objects.isNull(it.getScanAndGo()) || !it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon())));
            }
        } else {
            itemsNewOrder = Objects.nonNull(itemsScanAndGo) ? itemsScanAndGo.stream().filter(it -> (Objects.nonNull(it.getScanAndGo()) && it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon())).collect(Collectors.toList()) : null;
            if (Objects.nonNull(itemsScanAndGo) && itemsScanAndGo.stream()
                    .filter(it -> ((Objects.nonNull(it.getScanAndGo()) && it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon()))).findFirst().isPresent()) {
                itemsScanAndGo.removeIf(it -> ((Objects.nonNull(it.getScanAndGo()) && it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon())));
            }
        }

//        LOG.info("Items scan and go -> " + itemsScanAndGo.size());
        if (Objects.nonNull(itemsNewOrder)) {
            LOG.info("Items itemsNewOrder -> " + itemsNewOrder.size());
        }

        // fix provider and scan and go/
        List<DeliveryOrderProvider> deliveryOrderProvidersList = new ArrayList<>();
        if (!isScanAndGo) {
            // Providers
            deliveryOrderProvidersList = ofy().load().type(DeliveryOrderProvider.class).ancestor(deliveryOrderSavedShoppingCart).list();
            //LOG.warning("deliveryOrderProvidersList ->  " + (Objects.nonNull(deliveryOrderProvidersList) ? deliveryOrderProvidersList.size() : 0));
        }
        // Fix elimina items que no corresponden al tipo de envio actual
        if (Objects.nonNull(itemsNewOrder) && !itemsNewOrder.isEmpty()) {
            ofy().delete().entities(itemsNewOrder);
        }
        deliveryOrderItemList = itemsScanAndGo;

        boolean orderHasCoupon = false;
        try {
            if (Objects.nonNull(deliveryOrderItemList) && !deliveryOrderItemList.isEmpty()) {
                orderHasCoupon = deliveryOrderItemList.stream().anyMatch(it -> Objects.nonNull(it.getCoupon()) && it.getCoupon());
//                LOG.info("orderHasCoupon -> " + orderHasCoupon);
            }
        } catch (Exception e) {
            LOG.warning("error finding coupon ");
        }

        CustomerCoupon customerCoupon = obtainCustomerCoupon(customerKey);
        String couponName = null;
        if (customerCoupon != null && Objects.nonNull(customerCoupon.getCouponId())) {
            final Ref<Coupon> coupon = customerCoupon.getCouponId();
            if (coupon.get() != null && coupon.get().getName() != null) {
                couponName = coupon.get().getName();
            }
        }

        if (couponName != null && orderHasCoupon) {
            boolean flag = true;
//                LOG.info("couponName -> " + couponName);
            RequestSourceEnum sourceEnum = ftdUtilities.getSourceFromRequestHeaderForPays(httpServletRequest);
            Optional<CouponFiltersConfig> couponsFilter = APIAlgolia.getCouponFilterConfig();
            if (!couponsFilter.isPresent()) {
                LOG.warning("Error: [" + Constants.ERROR_COUPON_DATA_FILTER + "]");
                CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
                throw new BadRequestException(Constants.ERROR_COUPON_DATA_FILTER);
            }
            if (couponsFilter.get().getCampaigns() == null) {
                LOG.warning("Error: [" + Constants.ERROR_COUPON_DATA_FILTER + "]");
                CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
                throw new BadRequestException(Constants.ERROR_COUPON_DATA_FILTER);
            }
            Long paymethod = order.getPaymentType().getId();
            for (Campaign campaign : couponsFilter.get().getCampaigns()) {
                if (verifyCampaingCoupon(campaign, couponName)) {
                    if (!couponFilter(campaign, sourceEnum, order)) {
                        LOG.warning("Error: [" + Constants.ERROR_COUPON_FILTER_ORDER + "]");
                        CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
                        throw new ConflictException(Constants.ERROR_COUPON_FILTER_ORDER);
                    }
                    if (!validatePayMethodCouponFilter(paymethod, campaign)) {
                        LOG.warning("Error: [" + Constants.ERROR_COUPON_FILTER_ORDER + "]");
                        CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
                        throw new ConflictException
                                (campaign.getMessagePaymethod() != null ? campaign.getMessagePaymethod() : Constants.ERROR_COUPON_PAYMETHOD_FILTER_ORDER);
                    }
                }
            }
            if (iscustomerPaymentCard(order) && iscustomerPaymentCardId(order) && couponName != null) {
                GenericResponse validateCoupon = validateCoupon(order, couponName);
                if (validateCoupon != null && validateCoupon.getMessage() != null && validateCoupon.getMessage().equals(ErrorCouponMsg.ERROR_COUPON_FILTER_CARD_BIN.name())) {
                    LOG.info("Coupon not valid for card bin" + sourceEnum.name());
                    CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
                    throw new ConflictException(getMessageError(validateCoupon.getMessage()));
                }
            }
        }


        OrderUtil.deleteTipPriceZero(deliveryOrderItemList);
        checkTipsQuantity(deliveryOrderItemList);

        // TODO: Validacion para evitar cobrar productos que no esten visibles al usuario (APPS - Provedores externos)
        if (Objects.nonNull(order.getItemList()) && !order.getItemList().isEmpty()) {
//            valida Items de dataStore y los items parametro
//            LOG.info("deliveryOrderItemList 1 ->  " + deliveryOrderItemList.size());
            Map<Long, Integer> mapValidationItem = order.getItemList().stream().collect(ImmutableMap.toImmutableMap(DeliveryOrderItem::getId, DeliveryOrderItem::getQuantitySold));
//            LOG.info("deliveryOrderItemList 2 ->  " + deliveryOrderItemList.size());
            if (Objects.nonNull(deliveryOrderProvidersList) && !deliveryOrderProvidersList.isEmpty()) {
//                method createOrder ->  Validando Items
                deliveryOrderProvidersList.forEach(provider ->
                        provider.getItemList().removeIf(item -> !mapValidationItem.containsKey(item.getId())));
                // Validación para evitar que los items se dupliquen
                Map<Long, Integer> mapProviderItem = new HashMap<>();
                deliveryOrderProvidersList.stream()
                        .filter(provider -> (Objects.nonNull(provider.getItemList()) && !provider.getItemList().isEmpty()))
                        .forEach(provider -> provider.getItemList().parallelStream().forEach(itemProvider -> {
                                    mapProviderItem.put(itemProvider.getId(), itemProvider.getQuantitySold());
                                })
                        );
                // Elimina item de proveedores del listado normal de items
                deliveryOrderItemList.removeIf(item -> mapProviderItem.containsKey(item.getId()));
            }
        }

//        LOG.info("createOrder: has picking date ---> " + order.getPickingDate());
        if (!Objects.isNull(order.getPickingDate()) && verifyHighDemand(order, closerStoreId)) {
//            LOG.info("createOrder: isHigh Demand -> TRUE");
            setCufPerHighDemand(deliveryOrderItemList, closerStoreId);
        }

//        deliveryOrderItemList.forEach(item -> {
//            LOG.info("Item -> " + item.getId());
//        });

        String orderRequest = Orders.createOrderJson(order, deliveryOrderItemList, deliveryOrderProvidersList).toString();
        LOG.info("createOrderJson ->  " + (orderRequest != null ? orderRequest : " es null"));

        // Retrofit method:

        CreateOrderRequestCore requestCore;
        Gson gson = new Gson();
        requestCore = gson.fromJson(orderRequest, CreateOrderRequestCore.class);

        if (Objects.nonNull(deliveryOrderSavedShoppingCart.getUrlPrescription())) {
            requestCore.setUrlPrescriptionOptics(deliveryOrderSavedShoppingCart.getUrlPrescription());
        }

//        LOG.info("createOrder customer id before ----> " + requestCore.getCustomerId());
        if (requestCore.getCustomerId() != null && requestCore.getCustomerId() == 0) {
            // call service oms get customer id.
            CustomerAddressResponse customerAddressResponse = ApiGatewayService.get().getCustomerByAddressId(requestCore.getCustomerAddressId());
            if (customerAddressResponse != null && customerAddressResponse.getData() != null) {
                long customerIdAux = (long) customerAddressResponse.getData().getIdCustomer();
                requestCore.setCustomerId(customerIdAux);
            }

        }

        if (requestCore.getCustomerId() <= 0) {
            CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
            throw new ConflictException(Constants.ERROR_CREATE_ORDER);
        }

        try {
            if(!isScanAndGo) {
                final int storeId = closerStoreId;
                requestCore.getItems().forEach(item -> {
                    final ItemAlgolia itemAlgolia = APIAlgolia.getItemAlgolia(item.getItemId() + "" + storeId);
                    if (itemAlgolia != null) {
                        final int totalStock = itemAlgolia.getTotalStock();
                        if (totalStock > 0 && item.getQuantityRequested() > totalStock) {
//                        LOG.info("QuantityRequested ANTES:" + item.getQuantityRequested());
                            item.setQuantityRequested(totalStock);
//                        LOG.info("QuantityRequested DESPUES:" + item.getQuantityRequested());
                        }
                    }
                });
            }
        } catch (Exception e) {
            LOG.warning("Error al intentar actualizar la canrtidad solictada al maximo stock de la tienda.");
        }

        if (Objects.nonNull(order.getCustomerIdCallCenter())) {
//            Insertando el customer del call
            requestCore.setCustomerIdCallCenter(order.getCustomerIdCallCenter());
            requestCore.setSource(Constants.SOURCE_CALL_CENTER);
        }

        Boolean isPSE = false;

        if (PaymentTypeEnum.PSE.getId() == order.getPaymentType().getId()) {
            isPSE = true;
            requestCore.setFinancialInstitutions(order.getFinancialInstitutions());
            requestCore.setTypePersonPSE(order.getTypePersonPSE());
            requestCore.setIpAddress(order.getIpAddress());
            /*CreateOrderRequestCore identification = new CreateOrderRequestCore();
            identification.setIdentification(order.getIdentification());*/
            requestCore.setIdentification(order.getIdentification());
        }

        if (order.getSelfCheckout() != null && order.getSelfCheckout().getIsSelfCheckout() && order.getSelfCheckout().getIdBox() != null) {
            requestCore.setSelfCheckout(order.getSelfCheckout());
        }

        if (customerCoupon != null && Objects.nonNull(customerCoupon.getCouponId())) {
            final Ref<Coupon> coupon = customerCoupon.getCouponId();
            if (coupon.get() != null && coupon.get().getName() != null) {
                couponName = coupon.get().getName();
            }
        }

        if (isPrime && !isPrimeMixed) {
            requestCore.setPrimeMixedPSE(false);
        }

        validateStockZero(requestCore);

        if (createOrderViaBackend3(requestCore, order, orderJSON, TraceUtil.getXCloudTraceId(httpServletRequest), idCustomerWebSafe)) {

            if (orderJSON.getId() == 0 && PaymentTypeEnum.TRANSACCIONES_EN_LINEA.getId() == order.getPaymentType().getId()) {
                CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
                throw new ConflictException(Constants.ERROR_PAYMENT_ONLINE);
            }

            if (orderJSON.getId() <= 0) {
                CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
                throw new ConflictException(Constants.ERROR_CREATE_ORDER);
            }
//            LOG.warning("New Order ->  " + orderRequest);
            LOG.info("Result New Order ->  " + orderJSON);

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
            orderJSON.setItems(itemOrders);

            List<ProviderOrder> providers = new ArrayList<>();
            ProviderOrder provider = new ProviderOrder();
            provider.setName("");
            provider.setEmail("");
            provider.setDeliveryPrice(0);
            provider.setItems(itemOrders);
            providers.add(provider);
            orderJSON.setProviders(providers);

            //LOG.warning(String.valueOf(orderJSON.getId()));
            DeliveryOrder deliveryOrder = ofy().load().type(DeliveryOrder.class).filter("currentStatus", 1).ancestor(Ref.create(customerKey)).first().now();
            if (deliveryOrder == null) {
                CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
                throw new ConflictException(Constants.DELIVERY_ORDER_NOT_FOUND);
            }

            deliveryOrder.setIdOrder(orderJSON.getId());
            deliveryOrder.setAddress(orderJSON.getAddress());
            deliveryOrder.setIdAddress(order.getIdAddress());
            deliveryOrder.setAddressDetails(order.getAddressDetails());
            deliveryOrder.setPaymentType(order.getPaymentType());

            //
            if (order.getCustomerIdCallCenter() != null) {
                deliveryOrder.setSource(Constants.SOURCE_CALL_CENTER);
            } else {
                deliveryOrder.setSource(order.getSource());
            }

            if (!PaymentTypeEnum.PSE.getId().equals(order.getPaymentType().getId())) {
                deliveryOrder.setCurrentStatus(0);
            }
            deliveryOrder.setIdFarmatodo(order.getIdFarmatodo());
            deliveryOrder.setLastStatus(ORDER_CREATED);
            deliveryOrder.setActive(true);

            if (!isPrimeMixed && !isPrime) {
                deliveryOrder.setDeliveryType(order.getDeliveryType());
            } else if (isPrimeMixed) {
                deliveryOrder.setDeliveryType(order.getDeliveryType());
            } else {
                deliveryOrder.setDeliveryType(DeliveryType.SCANANDGO);
            }

            if (Objects.nonNull(orderJSON.getQrCode())) {
                deliveryOrder.setQrCode(orderJSON.getQrCode());
            }
            //Consultar el resumen de la orden creada, para obtener valor de domicilio
            try {
//                LOG.info("method getOrderSumary: -> orderID:" + orderJSON.getId());
                GetOrderSumary orderSumary = ApiGatewayService.get().getOrderSumary(orderJSON.getId());
                if (orderSumary != null && orderSumary.getData() != null) {
//                    LOG.info("orderJSON.getDeliveryPrice() -> " + orderSumary.getData().getDeliveryValue());
                    deliveryOrder.setDeliveryPrice(orderSumary.getData().getDeliveryValue());
                }
            } catch (Exception e) {
                e.printStackTrace();
                LOG.severe("Error al consulatar resumen de orden! " + e.getMessage());
            }

            // if ScanAndGO
            if (isOrdenScanAndGo(order) && !isPrime) {
                if (Objects.nonNull(order.getPaymentType()) && PaymentTypeEnum.DATAFONOS.getId().equals(order.getPaymentType().getId())) {
                    deliveryOrder.setLastStatus(ORDER_CREATED);

                } else {
                    deliveryOrder.setLastStatus(ORDER_DELIVERED);
                    deliveryOrder.setActive(false);
                }
            }

            if (isPrime && deliveryOrder.getDeliveryType().equals(DeliveryType.SCANANDGO) && !isPrimeMixed) {
                deliveryOrder.setLastStatus(ORDER_DELIVERED);
                deliveryOrder.setActive(false);
            }

            Timestamp stamp = new Timestamp(orderJSON.getCreateDate());
            Date date = new Date(stamp.getTime());
            deliveryOrder.setCreateDate(date);
            //if (orderJSON.getUpdateShopping()) {
            //    String getOrderURL = URLConnections.URL_ORDER + "/" + orderJSON.getId();
            //    DeliveryOrder processedOrder = CoreConnection.getRequest(getOrderURL, DeliveryOrder.class);
            //    deliveryOrder.updateShopping(processedOrder);
            //}
            if (order.getPickingDate() != null) {
                deliveryOrder.setPickingDate(order.getPickingDate());
            }

            //Fix para borrar proveedor si no tiene items
            List<DeliveryOrderProvider> deleteProviders = new ArrayList<>();
//            LOG.info("tamaño proveedores : " + deliveryOrderProvidersList.size() + " list " + deliveryOrderProvidersList);
            for (DeliveryOrderProvider oderProvider : deliveryOrderProvidersList) {
                if (oderProvider.getItemList() == null || oderProvider.getItemList().isEmpty()) {
                    deleteProviders.add(oderProvider);
                }
            }

            ofy().delete().entities(deleteProviders);
//            LOG.info("savin pre save delivery {}" + saving);
            if (saving > 0) {
                deliveryOrder.setSavingPrime(saving);
            }
            Key<DeliveryOrder> deliveryOrderKey = ofy().save().entity(deliveryOrder).now();

            if (Objects.nonNull(itemsNewOrder) && !itemsNewOrder.isEmpty()) {
                try {
                    // Crea una orden con lo items sobrantes
//                    LOG.info("Crea una nueva orden para los items ->  " + itemsNewOrder.size());
                    createDeliveryOrder(customerKey, deliveryOrder.getDeliveryType().getDeliveryType(), itemsNewOrder, deliveryOrderProvidersList);
                } catch (Exception ex) {
                    LOG.warning("No fue posible crear el nuevo Carrito con los items no utilizados " + ex.getMessage());
                }
            }

            // Save tracing state
            //TODO verificar si es necesario o esta muerto codigo se puede eliminar
            if (Objects.nonNull(orderJSON.getTracing()) && !orderJSON.getTracing().isEmpty()) {
                CreatedOrder finalOrderJSON = orderJSON;
                DeliveryOrder finalOrder3 = order;
                orderJSON.getTracing().forEach(tracing -> {
                    tracing.setIdTracing(UUID.randomUUID().toString());
                    tracing.setDeliveryOrderId(Ref.create(deliveryOrderKey));
                    if (tracing.getStatus() == 12) {
                        int responseSms = 0;
                        try {
                        /*responseSms = supportMethods.sendSms(order.getCustomerPhone(),
                                "Bienvenido a farmatodo. La transaccion asociada a tu orden "∫
                                        + finalOrderJSON.getId() + " fue aprobada");
*/
                            final SendSMSCloudFunctionReq request = new SendSMSCloudFunctionReq(order.getCustomerPhone(),
                                    MsgUtilAlgolia.obtainMsgAlgolia(MsgSmsEnum.MESSAGE_ORDER_CODE).replace("{orderId}", Long.toString(finalOrderJSON.getId())));
                            CloudFunctionsService.get().postSendSms(request);
                        } catch (IOException | BadRequestException e) {
                            LOG.warning("Error");
                        }
                        //LOG.warning("Response sms " + responseSms);
                    }
                });
                Tracing tracing = orderJSON.getTracing().get(0);
                ofy().save().entity(tracing);
            }

            addMarcaCategorySubcategorieAndItemUrl(orderJSON);

            if (requestCore.getSelfCheckout() != null && requestCore.getSelfCheckout().getIsSelfCheckout() && requestCore.getSelfCheckout().getIdBox() != null) {
                SelfCheckoutAlgolia selfCheckoutAlgolia = APIAlgolia.getSelfCheckout();
                if (!Objects.isNull(selfCheckoutAlgolia) && !Objects.isNull(selfCheckoutAlgolia.getCustomerByStoresAndBox())) {
                    Optional<SelfCheckoutListAlgolia> selfCheckoutListAlgolia = selfCheckoutAlgolia.getCustomerByStoresAndBox().stream()
                            .filter(checkoutListAlgolia -> Objects.equals(checkoutListAlgolia.getStore(), requestCore.getStoreId()) && Objects.equals(checkoutListAlgolia.getBox(), requestCore.getSelfCheckout().getIdBox()))
                            .limit(1).findFirst();
                    if (selfCheckoutListAlgolia.isPresent()) {
                        deleteCoupon(customerKey);
                    }
                }
            }
            deliveryOrderData = getOrderMethod(deliveryOrder, orderJSON.getId(), false, false);
            orderJSON.setOrderData(deliveryOrderData);
            ApiGatewayService.get().updateStratumBraze(requestCore.getCustomerId().toString());


//            if ( saving > 0) {
//                SavingCustomer requestSaving = getSavingCustomerRequest((long) order.getIdFarmatodo(), saving, UpdateTypeSavingEnum.ADD);
//                try {
//                    ApiGatewayService.get().updateOrCreateSaving(requestSaving);
//                } catch (Exception e) {
//                    LOG.warning("Error al guardar el saving");
//                }
//            }else if(saving > 0){
//                SavingCustomer requestSaving = getSavingCustomerRequest((long) order.getIdFarmatodo(), saving, UpdateTypeSavingEnum.ADD);
//                ApiGatewayService.get().sendSavingNoPrime(requestSaving);
//            }
            if (saving > 0) {
                ApiGatewayService
                        .get().sendSavingNoPrime(getSavingCustomerRequest((long) order.getIdFarmatodo(), saving, UpdateTypeSavingEnum.ADD));
            }
        } else {
            DeliveryOrder deliveryOrderOrder = ofy().load().type(DeliveryOrder.class).filter("currentStatus", 0).ancestor(Ref.create(customerKey)).first().now();
            deleteCoupon(customerKey);
            if (deliveryOrderOrder == null) {
                CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
                throw new ConflictException(Constants.ERROR_CREATE_ORDER);
            } else {
                ofy().delete().entity(deliveryOrderOrder).now();
                CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
                throw new ConflictException(Constants.ERROR_CREATE_ORDER);
            }

        }
        try {
            Long sessionId = Long.parseLong(httpServletRequest.getHeader("amplitudeSessionId"));
            if (sessionId != null) {
                AmplitudeSessionRequest request = new AmplitudeSessionRequest();
                request.setOrderId(orderJSON.getId());
                request.setSessionId(sessionId);
//                LOG.info("request -> " + request.toString());
                ApiGatewayService.get().saveAmplitudeSessionId(request);
            }
        } catch (Exception e) {
            LOG.warning(e.getMessage());
        }

        if ((isPrime || isPrimeMixed)) {
            try {
                final Long orderPrime = Objects.isNull(createdOrderOnlyPrime.getId()) ? 0 : createdOrderOnlyPrime.getId();
                orderJSON.setIdOrderPrime(orderPrime);
            } catch (Exception e) {
                LOG.info("Ocurrio un problema -> " + e.getMessage());
            }

            orderJSON.setPrimeGeneral(true);

            if (!isPSE) {
                orderJSON.setPrime(true);
                orderJSON.setPrimeGeneral(false);
            }
        } else {
            orderJSON.setPrimeGeneral(false);
            orderJSON.setPrime(false);
        }

        if (order.getPaymentType().getName().equals(PaymentTypeEnum.EFECTIVO.name()) || order.getPaymentType().getName().equals(PaymentTypeEnum.DATAFONOS.name()) && Objects.nonNull(deliveryOrderData.getCustomerPhone()) && Objects.nonNull(deliveryOrderData.getDeliveryType()) && deliveryOrderData.getDeliveryType().equals(DeliveryType.EXPRESS)) {
            try {
                final SendSMSCloudFunctionReq request = new SendSMSCloudFunctionReq(deliveryOrderData.getCustomerPhone(),
                        MsgUtilAlgolia.obtainMsgAlgolia(MsgSmsEnum.MESSAGE_ALERT_METHOD_PAY));
                CloudFunctionsService.get().postSendSms(request);
            } catch (Exception e) {
                LOG.info("No se pudo enviar sms a " + deliveryOrderData.getCustomerPhone());
            }
        }
        addRMSclasses(orderJSON);
        sendEventCreate(orderJSON, requestCore.getCustomerId(), order.getPaymentType().getId());

        try {
            LOG.info("CreateOrderResponse:#" + requestCore.getCustomerId() + " Gson -> " + new Gson().toJson(orderJSON));
        } catch (Exception e) {
            LOG.info("No se pudo serializar Json de respuesta de creación de orden.");
        }
        CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
        return orderJSON;
    }


    @ApiMethod(name = "createOrder", path = "/orderEndpoint/v2/createOrder", httpMethod = ApiMethod.HttpMethod.POST)
    public CreateOrderResponse<CreatedOrder> createOrderV2(
            @Named("idCustomerWebSafe") final String idCustomerWebSafe,
            @Named("token") final String token,
            @Named("tokenIdWebSafe") final String tokenIdWebSafe,
            DeliveryOrder order,
            HttpServletRequest httpServletRequest) throws BadRequestException, ConflictException, IOException, AlgoliaException, UnauthorizedException, InternalServerErrorException, InterruptedException {

        LOG.info("request -> " + order.toStringJson());
        LOG.info("method: -> /orderEndpoint/v2/createOrder");

        // mensajes parametrizados de error
        boolean isActiveMessagesAlgolia;
        String invalidToken = Constants.INVALID_TOKEN;
        String multipleCallForService = Constants.MULTIPLE_CALL_FOR_SERVICE;
        String invalidAddress = Constants.INVALID_ADDRESS;
        String mandatoryPaymentCardId = Constants.MANDATORY_PAYMENTCARD_ID;
        String userBlockedMessage = Constants.USER_BLOCKED;
        String creditCardBlocked = Constants.CREDIT_CARD_BLOCKED;
        String errorCreateOrderPrime = Constants.ERROR_CREATE_ORDER_PRIME;
        String errorCreateOrderNoPrime = Constants.ERROR_CREATE_ORDER_NO_PRIME;
        String invalidProgramming = Constants.INVALID_PROGRAMMING;
        String closedStoreMessage = Constants.CLOSED_STORE;
        String invalidNationalStore = Constants.INVALID_NATIONAL_STORE;
        String invalidEnvialoyaStore = Constants.INVALID_ENVIALOYA_STORE;
        String emptyShoppingCart = Constants.EMPTY_SHOPPING_CART;
        String userNotFound = Constants.USER_NOT_FOUND;
        String errorAddressCreateOrder = Constants.ERROR_ADDRESS_CREATE_ORDER;
        String errorCouponDataFilter = Constants.ERROR_COUPON_DATA_FILTER;
        String errorCouponPaymethodFilterOrder = Constants.ERROR_COUPON_PAYMETHOD_FILTER_ORDER;
        String errorCouponProblem = Constants.ERROR_COUPON_PROBLEM;
        String errorPaymentOnline = Constants.ERROR_PAYMENT_ONLINE;
        String errorCreateOrderAlgolia = Constants.ERROR_CREATE_ORDER_ALGOLIA;
        String errorCreateOrderOms = Constants.ERROR_CREATE_ORDER_OMS;
        String deliveryOrderNotFound = Constants.DELIVERY_ORDER_NOT_FOUND;


        AlgoliaMessageConfigCreateOrder algoliaMessageConfigCreateOrder = APIAlgolia.getMessagesCreateOrder();
        if (Objects.nonNull(algoliaMessageConfigCreateOrder)) {
            isActiveMessagesAlgolia = Objects.nonNull(algoliaMessageConfigCreateOrder.getActive()) && algoliaMessageConfigCreateOrder.getActive();
            if (isActiveMessagesAlgolia) {
                invalidToken = algoliaMessageConfigCreateOrder.getInvalidToken();
                invalidAddress = algoliaMessageConfigCreateOrder.getInvalidAddress();
                multipleCallForService = algoliaMessageConfigCreateOrder.getMultipleCallForService();
                mandatoryPaymentCardId = algoliaMessageConfigCreateOrder.getMandatoryPaymentCardId();
                userBlockedMessage = algoliaMessageConfigCreateOrder.getUserBlocked();
                creditCardBlocked = algoliaMessageConfigCreateOrder.getCreditCardBlocked();
                errorCreateOrderPrime = algoliaMessageConfigCreateOrder.getErrorCreatedOrderPrime();
                errorCreateOrderNoPrime = algoliaMessageConfigCreateOrder.getErrorCreatedOrderNoPrime();
                invalidProgramming = algoliaMessageConfigCreateOrder.getInvalidOrderScheduled();
                closedStoreMessage = algoliaMessageConfigCreateOrder.getClosedStore();
                invalidNationalStore = algoliaMessageConfigCreateOrder.getInvalidNationalStore();
                invalidEnvialoyaStore = algoliaMessageConfigCreateOrder.getInvalidEnvialoyaStore();
                emptyShoppingCart = algoliaMessageConfigCreateOrder.getEmptyShoppingCart();
                userNotFound = algoliaMessageConfigCreateOrder.getUserNotFound();
                errorAddressCreateOrder = algoliaMessageConfigCreateOrder.getErrorAddressInvalid();
                errorCouponDataFilter = algoliaMessageConfigCreateOrder.getErrorCouponDataFilter();
                errorCouponPaymethodFilterOrder = algoliaMessageConfigCreateOrder.getErrorCouponInvalidPaymentMethod();
                errorCouponProblem = algoliaMessageConfigCreateOrder.getErrorCouponProblem();
                errorPaymentOnline = algoliaMessageConfigCreateOrder.getErrorPaymentOnline();
                errorCreateOrderAlgolia = algoliaMessageConfigCreateOrder.getErrorCreatedOrderItemsAlgolia();
                errorCreateOrderOms = algoliaMessageConfigCreateOrder.getErrorCreatedOrderOms();
                deliveryOrderNotFound = algoliaMessageConfigCreateOrder.getDeliveryOrderNotFound();
            }
        }

        Optional<String> jsonCachedOptional = CachedDataManager.getJsonFromCacheIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
        if (jsonCachedOptional.isPresent()) {
            return new CreateOrderResponse<>(HttpStatusCode.TOO_MANY_REQUESTS.getCode(), HttpStatusCode.TOO_MANY_REQUESTS.getStatusName(), multipleCallForService, null);
        }
        CachedDataManager.saveJsonInCacheIndexTime(String.valueOf(order.getIdFarmatodo()), new Gson().toJson(order), Constants.ID_INDEX_SAVE_AND_GET_REDIS, Constants.TIME_SAVE_REDIS_AND_SECONDS);

        Key<Customer> customerKey = Key.create(idCustomerWebSafe);

        try {
            if (!authenticate.isValidToken(token, tokenIdWebSafe)) {
                LOG.warning("return " + HttpStatusCode.BAD_REQUEST.getCode() + " " + invalidToken);
                CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
                return new CreateOrderResponse<>(HttpStatusCode.BAD_REQUEST.getCode(), HttpStatusCode.BAD_REQUEST.getStatusName(), invalidToken, null);
            }

            if(isNationalOrEnvialoYa(order.getDeliveryType()) && isEfectivoOrDataphone(order.getPaymentType())){
                CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
                return new CreateOrderResponse<>(HttpStatusCode.BAD_REQUEST.getCode(), HttpStatusCode.BAD_REQUEST.getStatusName(), Constants.INVALID_ADDRESS_NATIONAL_0R_ENVIALOYA, null);
            }

            AlgoliaExploStore algoliaExploStore = APIAlgolia.getActiveStoreExpo();
            if (Objects.equals(order.getIdStoreGroup(), "83") && algoliaExploStore.isEnable()) {
                order.setIdStoreGroup("86");
            }

            boolean isScanAndGo = isOrdenScanAndGo(order);
            boolean isPrime = isOrderPrime(order);
            boolean isPrimeMixed = isOrderPrimeMixed(order);
            final Long typeSubscription = getTypeSubscriptionPrime(order);

            CreateOrderResponse<CreatedOrder> createdOrderOnlyPrimeV2 = null;

//        LOG.info("isPrimeMixed ? -> " + isPrimeMixed);

            if (!isScanAndGo && order.getIdAddress() == 0 && !isPrime) {
                LOG.warning("return " + HttpStatusCode.BAD_REQUEST.getCode() + " " + invalidAddress);
                CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
                return new CreateOrderResponse<>(HttpStatusCode.BAD_REQUEST.getCode(), HttpStatusCode.BAD_REQUEST.getStatusName(), invalidAddress, null);
            }


            if (order.getPaymentType().getId() == PaymentTypeEnum.TRANSACCIONES_EN_LINEA.getId() && order.getPaymentCardId() <= 0) {
                LOG.warning("return " + HttpStatusCode.BAD_REQUEST.getCode() + " " + mandatoryPaymentCardId);
                CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
                return new CreateOrderResponse<>(HttpStatusCode.BAD_REQUEST.getCode(), HttpStatusCode.BAD_REQUEST.getStatusName(), mandatoryPaymentCardId, null);
            }


            DeliveryOrder deliveryOrderData = new DeliveryOrder();

            customerKey = Key.create(idCustomerWebSafe);

            CreatedOrder orderJSON = new CreatedOrder();
            //LOG.warning(order.getCustomerPhone());
            DeliveryOrder deliveryOrderSavedShoppingCart = ofy()
                    .load()
                    .type(DeliveryOrder.class)
                    .filter("currentStatus", 1)
                    .ancestor(Ref.create(customerKey))
                    .first()
                    .now();


            double saving = 0;
            boolean validateUserPrime = false;
            try {
                validateUserPrime = validateUserPrime((long) order.getIdFarmatodo());
                deliveryOrderSavedShoppingCart.setFarmaCredits(order.getFarmaCredits());
                deliveryOrderSavedShoppingCart.setTalonOneData(order.getTalonOneData());
                saving = getSaving(deliveryOrderSavedShoppingCart, httpServletRequest, order.getIdFarmatodo(),
                        order.getDeliveryType(), order.getIdStoreGroup(), idCustomerWebSafe, token, tokenIdWebSafe);
            } catch (Exception e) {
                LOG.warning("Error saving order " + e.getMessage());
            }

            // validar fraude de cc, email, phone and TC

            CustomerFraudResponse customerFraudResponse = ApiGatewayService.get().searchFraudCustomer(Long.valueOf(order.getIdFarmatodo()));
            Boolean isfraud = Objects.isNull(customerFraudResponse.getFraud()) ? false : customerFraudResponse.getFraud();

            if (isfraud) {
                LOG.warning("return " + HttpStatusCode.FORBIDDEN.getCode() + " " + userBlockedMessage);
                CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
                return new CreateOrderResponse<>(HttpStatusCode.FORBIDDEN.getCode(), HttpStatusCode.FORBIDDEN.getStatusName(), userBlockedMessage, null);
            }

            if (order.getPaymentType().getId() == PaymentTypeEnum.TRANSACCIONES_EN_LINEA.getId()) {
                CustomerFraudResponse customerFraudCreditCardResponse = ApiGatewayService.get().antifraudCreditCard(Long.valueOf(order.getPaymentCardId()));
                Boolean isfraudCreditCard = Objects.isNull(customerFraudCreditCardResponse.getFraud()) ? false : customerFraudCreditCardResponse.getFraud();
                if (isfraudCreditCard) {
                    LOG.warning("return " + HttpStatusCode.FORBIDDEN.getCode() + " " + creditCardBlocked);
                    CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
                    return new CreateOrderResponse<>(HttpStatusCode.FORBIDDEN.getCode(), HttpStatusCode.FORBIDDEN.getStatusName(), creditCardBlocked, null);
                }
            }

            if (isPrimeMixed) {
                boolean isFreeDeliveryOrder = isFreeDeliveryOrderPrime(idCustomerWebSafe, order, deliveryOrderSavedShoppingCart);
                DeliveryOrder orderPrime = order;
                orderPrime.getItemList().removeIf(item -> Objects.equals(item.getId(), 0L));
                List<DeliveryOrderItem> deliveryOrderItemList = ofy().load().type(DeliveryOrderItem.class).ancestor(deliveryOrderSavedShoppingCart).list();

                int primeQuantity = getTypeSubscriptionPrimeQuantity(deliveryOrderItemList);
                List<OrderItemsQuantity> itemsInCart = itemsInShoppingCart(deliveryOrderItemList);

                try {
                    createdOrderOnlyPrimeV2 = createOrderPrimeV2(idCustomerWebSafe, token, tokenIdWebSafe, orderPrime, httpServletRequest);
                    if (Objects.isNull(createdOrderOnlyPrimeV2.getData())) {
                        addItemsNoPrime(idCustomerWebSafe, token, tokenIdWebSafe, itemsInCart, order, httpServletRequest);
                        CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
                        return createdOrderOnlyPrimeV2;
                    }
                } catch (Exception e) {
                    addItemsNoPrime(idCustomerWebSafe, token, tokenIdWebSafe, itemsInCart, order, httpServletRequest);
                    LOG.severe("return " + HttpStatusCode.INTERNAL_SERVER_ERROR.getCode() + " " + errorCreateOrderPrime);
                    CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
                    return new CreateOrderResponse<>(HttpStatusCode.INTERNAL_SERVER_ERROR.getCode(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusName(), errorCreateOrderPrime, null);
                }

                DeliveryOrder orderNoPrime;
                orderNoPrime = order;
                orderNoPrime.setIdDeliveryOrder(UUID.randomUUID().toString());
                orderNoPrime.setIdCustomer(Ref.create(customerKey));
                orderNoPrime.setCurrentStatus(1);
                orderNoPrime.setCreateDate(new Date());
                orderNoPrime.setDeliveryType(order.getDeliveryType());
//             agregar de nuevo los items que no son prime
                LOG.info("Llamando metodo para agregar los items no PRIME idCustomerWebSafe-> " + idCustomerWebSafe);
//           agregar de nuevo los items que no son prime
                addItemsNoPrime(idCustomerWebSafe, token, tokenIdWebSafe, itemsInCart, order, httpServletRequest);

                LOG.info("SAVING -> " + saving);
                final Long orderPrimeID = Objects.isNull(createdOrderOnlyPrimeV2.getData().getId()) ? 0 : createdOrderOnlyPrimeV2.getData().getId();
                CreateOrderResponse<CreatedOrder> createdOrderNoPrimeV2;
                try {
                    createdOrderNoPrimeV2 = createOrderNoPrimeV2(idCustomerWebSafe, token, tokenIdWebSafe, saving, validateUserPrime, typeSubscription, orderPrimeID, primeQuantity, isFreeDeliveryOrder, orderNoPrime, httpServletRequest);
                    if (Objects.isNull(createdOrderNoPrimeV2.getData())) {
                        return createdOrderOnlyPrimeV2;
                    }
                } catch (Exception e) {
                    LOG.severe("return " + HttpStatusCode.INTERNAL_SERVER_ERROR.getCode() + " " + errorCreateOrderNoPrime);
                    CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
                    return new CreateOrderResponse<>(HttpStatusCode.INTERNAL_SERVER_ERROR.getCode(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusName(), errorCreateOrderNoPrime, null);
                }

                createdOrderNoPrimeV2.getData().setIdOrderPrime(orderPrimeID);
                //agrega el id de la orden prime para poder listar la orden prime en getOrders
                deliveryOrderSavedShoppingCart.setOrderPrimeId(orderPrimeID);
                ofy().save().entity(deliveryOrderSavedShoppingCart).now();

                try {
                    LOG.info("primer llamado copyClosedSession");
                    TalonOneService talonOneService = new TalonOneService();
                    talonOneService.copyClosedSession(String.valueOf(createdOrderNoPrimeV2.getData().getOrderData().getIdFarmatodo()), String.valueOf(createdOrderNoPrimeV2.getData().getId()), createdOrderNoPrimeV2.getData(), idCustomerWebSafe);
                    deleteCouponTalonOne(createdOrderNoPrimeV2.getData().getOrderData().getIdFarmatodo(), idCustomerWebSafe);
                    Key<User> userKey = Key.create(idCustomerWebSafe);
                    User user = users.findUserByKey(userKey);
                    deleteCacheDeductDiscount(idCustomerWebSafe,String.valueOf(user.getId()));
                } catch (Exception e) {
                    LOG.info("Error total de Talon One: " + e);
                }
                FirebaseService.get().setOrderPrimeMix(String.valueOf(createdOrderOnlyPrimeV2.getData().getId()), String.valueOf(createdOrderNoPrimeV2.getData().getId()));
                //agrega el item prime nuevamente para que se mantega en el carrito
                addItemPrime(idCustomerWebSafe, token, tokenIdWebSafe, itemsInCart);

                OrderRelationPrimeRequest orderRelationPrimeRequest = new OrderRelationPrimeRequest();
                orderRelationPrimeRequest.setOrderIdExpress(createdOrderNoPrimeV2.getData().getId());
                orderRelationPrimeRequest.setOrderIdPrime(createdOrderNoPrimeV2.getData().getIdOrderPrime());
                ApiGatewayService.get().saveRelationOrdersPrime(orderRelationPrimeRequest);

                LOG.info("para la orden mixta: " +
                        createdOrderNoPrimeV2.getData().getId() +
                        " primeGeneral es: " +
                        createdOrderNoPrimeV2.getData().isPrimeGeneral() +
                        " y el prime es: " + createdOrderNoPrimeV2.getData().isPrime() +
                        " y el id de la orden Prime es: " + createdOrderNoPrimeV2.getData().getIdOrderPrime());
                return createdOrderNoPrimeV2;
            }

            LOG.info("Order isScanAndGo >>  " + isScanAndGo);
            LOG.info("Order isPrime >>  " + isPrime);

            if (deliveryOrderSavedShoppingCart != null) {
                LOG.warning("deliveryOrderSaved ->  " + deliveryOrderSavedShoppingCart.getIdDeliveryOrder());
            }
//        LOG.info("deliveryOrderSaved is null ->  " + (deliveryOrderSavedShoppingCart == null));
//        LOG.info("customerKey ->  " + customerKey.getString());

            // validar si el cliente esta bloqueado
            BlockedUser blockedUser = ofy().load().type(BlockedUser.class).filter("idUser", order.getIdFarmatodo()).first().now();

            boolean userBlocked = blockedUser != null;
//        LOG.info("usuario bloqueado -> " + userBlocked);


            customerFraudResponse = ApiGatewayService.get().searchFraudCustomer(Long.valueOf(order.getIdFarmatodo()));
            isfraud = Objects.isNull(customerFraudResponse.getFraud()) ? false : customerFraudResponse.getFraud();

            if (userBlocked || isfraud) {
                LOG.warning("return " + HttpStatusCode.FORBIDDEN.getCode() + " " + userBlockedMessage);
                CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
                return new CreateOrderResponse<>(HttpStatusCode.FORBIDDEN.getCode(), HttpStatusCode.FORBIDDEN.getStatusName(), userBlockedMessage, null);
            }


            //TODO Se obtiene el id de la tienda mas cercana segun la direccion
            //List<Address> listAddress = customers.getAddressesFromCustomer(order.getIdFarmatodo());
            int closerStoreId = 0;
            if (!isScanAndGo || !isPrime) {
                AddressesRes addressesRes = ApiGatewayService.get().getAddressesByCustomerId(order.getIdFarmatodo());
                List<Address> listAddress = addressesRes.getAddresses();

                if (Objects.nonNull(listAddress)) {
                    DeliveryOrder finalOrder = order;
                    DeliveryOrder finalOrder2 = order;
                    closerStoreId = listAddress.stream().filter(address -> Objects.nonNull(address) && Objects.nonNull(finalOrder.getDeliveryType()) && Objects.nonNull(address.getDeliveryType()) &&
                                    address.getCloserStoreId() > 0 &&
                                    finalOrder.getDeliveryType().getDeliveryType().equals(address.getDeliveryType().getDeliveryType()) &&
                                    finalOrder.getIdAddress() == address.getIdAddress())
                            .mapToInt(address -> address.getCloserStoreId()).findFirst().isPresent() ? listAddress.stream().filter(address -> address.getCloserStoreId() > 0 &&
                                    finalOrder2.getDeliveryType().getDeliveryType().equals(address.getDeliveryType().getDeliveryType()) &&
                                    finalOrder2.getIdAddress() == address.getIdAddress())
                            .mapToInt(address -> address.getCloserStoreId()).findFirst().getAsInt() : 0;
                }
            } else {
                closerStoreId = order.getIdStoreGroup() == null ? 26 : Integer.valueOf(order.getIdStoreGroup());
            }

            LOG.info("closerStoreId prueba => " + closerStoreId);
            String storeIdToValidate = closerStoreId > 0 ? String.valueOf(closerStoreId) : order.getIdStoreGroup();
//        LOG.info("storeid: " + closerStoreId);

            final Long finalStoreToValidate = Long.parseLong(storeIdToValidate);
            boolean validateStores = false;

            //exclude stores
            ExcludeStoresCreateOrder excludeStoresCreateOrder = APIAlgolia.getStoresToExcludeCreateOrder();

            if (Objects.nonNull(excludeStoresCreateOrder.getEnableStores()) && !excludeStoresCreateOrder.getEnableStores().isEmpty()) {
//            LOG.info("stores to exclude -> " + excludeStoresCreateOrder.toString());
                for (Long storeExclude : excludeStoresCreateOrder.getEnableStores()) {
                    if (storeExclude.equals(finalStoreToValidate)) {
                        validateStores = true;
                    }
                }
            }

            // siempre estaran disponibles a menos que se encuentre en la lista
            Boolean isStoreAvailable = true;
//        LOG.info("go to validateStores if open or close -> " + validateStores);
            if (validateStores && !isScanAndGo) {
                isStoreAvailable = this.isStoreAvailable(storeIdToValidate, order.getPickingDate());
            }

            // Fix Validacion PickingDate
            if (Objects.nonNull(order.getPickingDate())) {
                OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
                now = now.minus(5, ChronoUnit.HOURS);
                OffsetDateTime pickingDate = order.getPickingDate().toInstant().atOffset(ZoneOffset.UTC);
                if (pickingDate.isBefore(now)) {
                    LOG.warning("return " + HttpStatusCode.BAD_REQUEST.getCode() + " " + invalidProgramming);
                    CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
                    return new CreateOrderResponse<>(HttpStatusCode.BAD_REQUEST.getCode(), HttpStatusCode.BAD_REQUEST.getStatusName(), invalidProgramming, null); //+pickingDate+ " - "+now.toString());
                }

            }

            if (!isStoreAvailable) {
                LOG.warning("return " + HttpStatusCode.BAD_REQUEST.getCode() + " " + closedStoreMessage);
                CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
                return new CreateOrderResponse<>(HttpStatusCode.BAD_REQUEST.getCode(), HttpStatusCode.BAD_REQUEST.getStatusName(), closedStoreMessage, null);
            }


            if (!Objects.isNull(order.getDeliveryType())) {
                if (order.getDeliveryType().getDeliveryType().equals(DeliveryType.NATIONAL.getDeliveryType())
                        && closerStoreId != URLConnections.NATIONAL_ID_STORE) {
                    LOG.warning("return " + HttpStatusCode.BAD_REQUEST.getCode() + " " + invalidNationalStore);
                    CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
                    return new CreateOrderResponse<>(HttpStatusCode.BAD_REQUEST.getCode(), HttpStatusCode.BAD_REQUEST.getStatusName(), invalidNationalStore, null);
                }

                if (order.getDeliveryType().getDeliveryType().equals(DeliveryType.ENVIALOYA.getDeliveryType())
                        && closerStoreId != URLConnections.ENVIALOYA_ID_STORE) {
                    LOG.warning("return " + HttpStatusCode.BAD_REQUEST.getCode() + " " + invalidEnvialoyaStore);
                    CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
                    return new CreateOrderResponse<>(HttpStatusCode.BAD_REQUEST.getCode(), HttpStatusCode.BAD_REQUEST.getStatusName(), invalidEnvialoyaStore, null);
                }
            }

            RequestSourceEnum sourceE = ftdUtilities.getSourceFromRequestHeaderForPays(httpServletRequest);

            if (deliveryOrderSavedShoppingCart == null) {
                return handleEmptyCart(emptyShoppingCart, sourceE, order.getIdFarmatodo());
            }

            List<DeliveryOrderItem> deliveryOrderItemList = ofy().load().type(DeliveryOrderItem.class).ancestor(deliveryOrderSavedShoppingCart).list();


            this.removeItemsMarketplaceFromItemsExpressList(deliveryOrderItemList);


            if (deliveryOrderItemList == null || deliveryOrderItemList.isEmpty()) {
                return handleEmptyCart(emptyShoppingCart, sourceE, order.getIdFarmatodo());
            }

            // Fix items Duplicados
            validateDuplicateItems(deliveryOrderItemList);

            // NEW FIX

            //  obtener store and delivery type from address.!
            if (!isScanAndGo && !isPrime) {
                Key<User> userKey = Key.create(idCustomerWebSafe);
                User user = users.findUserByKey(userKey);
                if (user == null || user.getId() == 0) {
                    LOG.warning("return " + HttpStatusCode.UNAUTHORIZED.getCode() + " " + userNotFound);
                    CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
                    return new CreateOrderResponse<>(HttpStatusCode.UNAUTHORIZED.getCode(), HttpStatusCode.UNAUTHORIZED.getStatusName(), userNotFound, null);
                }


                List<Address> allAddresses = this.customers.getAddressesByIdCustomer(user);

                Optional<Address> optionalAddress = Optional.empty();
                try {
                    DeliveryOrder finalOrder1 = order;
                    optionalAddress = allAddresses
                            .stream()
                            .filter(address -> address.getIdAddress().equals(finalOrder1.getIdAddress()))
                            .findFirst();
                } catch (Exception e) {
                    LOG.warning("Error@createOrder filtering and finding address " + e.getMessage());
                }

                if (optionalAddress.isPresent()) {

                    Address addressToCreateOrder = optionalAddress.get();
//                LOG.info("address -> " + addressToCreateOrder.toString());
                    if (addressToCreateOrder.getDeliveryType() != null && addressToCreateOrder.getCloserStoreId() > 0) {
                        // SET NEW DELIVERY_TYPE AND STORE
                        if(!order.getDeliveryType().equals(DeliveryType.PROVIDER)){
                            order.setDeliveryType(addressToCreateOrder.getDeliveryType());
                        }
                        order.setIdStoreGroup(String.valueOf(addressToCreateOrder.getCloserStoreId()));
                    } else {
                        LOG.warning("return " + HttpStatusCode.CONFLICT.getCode() + " " + errorAddressCreateOrder);
                        CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
                        return new CreateOrderResponse<>(HttpStatusCode.CONFLICT.getCode(), HttpStatusCode.CONFLICT.getStatusName(), errorAddressCreateOrder, null);
                    }
                } else {
                    LOG.warning("return " + HttpStatusCode.CONFLICT.getCode() + " " + errorAddressCreateOrder);
                    CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
                    return new CreateOrderResponse<>(HttpStatusCode.CONFLICT.getCode(), HttpStatusCode.CONFLICT.getStatusName(), errorAddressCreateOrder, null);
                }

                LOG.info("fix new deliveryType -> " + order.getDeliveryType() + ", new store -> " + order.getIdStoreGroup());
            }

            // ScanAndGo Order
            List<DeliveryOrderItem> itemsScanAndGo = new ArrayList<>(deliveryOrderItemList);
            List<DeliveryOrderItem> itemsNewOrder = new ArrayList<>();
            if (isScanAndGo) {
                //set store princial when is scan and go
                itemsNewOrder = Objects.nonNull(itemsScanAndGo) ? itemsScanAndGo.stream().filter(it -> (Objects.isNull(it.getScanAndGo()) || !it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon())).collect(Collectors.toList()) : null;
                if (Objects.nonNull(itemsScanAndGo) && itemsScanAndGo.stream()
                        .filter(it -> (Objects.isNull(it.getScanAndGo()) || !it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon())).findFirst().isPresent()) {
                    itemsScanAndGo.removeIf(it -> ((Objects.isNull(it.getScanAndGo()) || !it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon())));
                }
            } else {
                itemsNewOrder = Objects.nonNull(itemsScanAndGo) ? itemsScanAndGo.stream().filter(it -> (Objects.nonNull(it.getScanAndGo()) && it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon())).collect(Collectors.toList()) : null;
                if (Objects.nonNull(itemsScanAndGo) && itemsScanAndGo.stream()
                        .filter(it -> ((Objects.nonNull(it.getScanAndGo()) && it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon()))).findFirst().isPresent()) {
                    itemsScanAndGo.removeIf(it -> ((Objects.nonNull(it.getScanAndGo()) && it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon())));
                }
            }

//        LOG.info("Items scan and go -> " + itemsScanAndGo.size());
            if (Objects.nonNull(itemsNewOrder)) {
                LOG.info("Items itemsNewOrder -> " + itemsNewOrder.size());
            }

            // fix provider and scan and go/
            List<DeliveryOrderProvider> deliveryOrderProvidersList = new ArrayList<>();
            if (!isScanAndGo) {
                // Providers
                deliveryOrderProvidersList = ofy().load().type(DeliveryOrderProvider.class).ancestor(deliveryOrderSavedShoppingCart).list();
                //LOG.warning("deliveryOrderProvidersList ->  " + (Objects.nonNull(deliveryOrderProvidersList) ? deliveryOrderProvidersList.size() : 0));
            }
            // Fix elimina items que no corresponden al tipo de envio actual
            if (Objects.nonNull(itemsNewOrder) && !itemsNewOrder.isEmpty()) {
                ofy().delete().entities(itemsNewOrder);
            }
            deliveryOrderItemList = itemsScanAndGo;

            boolean orderHasCoupon = false;
            try {
                if (Objects.nonNull(deliveryOrderItemList) && !deliveryOrderItemList.isEmpty()) {
                    orderHasCoupon = deliveryOrderItemList.stream().anyMatch(it -> Objects.nonNull(it.getCoupon()) && it.getCoupon());
//                LOG.info("orderHasCoupon -> " + orderHasCoupon);
                }
            } catch (Exception e) {
                LOG.warning("error finding coupon ");
            }

            CustomerCoupon customerCoupon = obtainCustomerCoupon(customerKey);
            String couponName = null;
            if (customerCoupon != null && Objects.nonNull(customerCoupon.getCouponId())) {
                final Ref<Coupon> coupon = customerCoupon.getCouponId();
                if (coupon.get() != null && coupon.get().getName() != null) {
                    couponName = coupon.get().getName();
                }
            }

            if (couponName != null && orderHasCoupon) {
                boolean flag = true;
//                LOG.info("couponName -> " + couponName);
                RequestSourceEnum sourceEnum = ftdUtilities.getSourceFromRequestHeaderForPays(httpServletRequest);
                Optional<CouponFiltersConfig> couponsFilter = APIAlgolia.getCouponFilterConfig();
                if (!couponsFilter.isPresent()) {
                    LOG.warning("Error: [" + Constants.ERROR_COUPON_DATA_FILTER + "]");
                    CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
                    return new CreateOrderResponse<>(HttpStatusCode.CONFLICT.getCode(), HttpStatusCode.CONFLICT.getStatusName(), errorCouponDataFilter, null);
                }
                if (couponsFilter.get().getCampaigns() == null) {
                    LOG.warning("return " + HttpStatusCode.CONFLICT.getCode() + " " + errorCouponDataFilter);
                    CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
                    return new CreateOrderResponse<>(HttpStatusCode.CONFLICT.getCode(), HttpStatusCode.CONFLICT.getStatusName(), errorCouponDataFilter, null);
                }
                Long paymethod = order.getPaymentType().getId();
                for (Campaign campaign : couponsFilter.get().getCampaigns()) {
                    if (verifyCampaingCoupon(campaign, couponName)) {
                        if (!couponFilter(campaign, sourceEnum, order)) {
                            LOG.warning("return " + HttpStatusCode.CONFLICT.getCode() + " " + errorCouponDataFilter);
                            CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
                            return new CreateOrderResponse<>(HttpStatusCode.CONFLICT.getCode(), HttpStatusCode.CONFLICT.getStatusName(), errorCouponDataFilter, null);
                        }
                        if (!validatePayMethodCouponFilter(paymethod, campaign)) {
                            LOG.warning("return " + HttpStatusCode.CONFLICT.getCode() + " " + errorCouponPaymethodFilterOrder);
                            CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
                            return new CreateOrderResponse<>(HttpStatusCode.CONFLICT.getCode(), HttpStatusCode.CONFLICT.getStatusName(), errorCouponPaymethodFilterOrder, null);
                        }
                    }
                }
                if (iscustomerPaymentCard(order) && iscustomerPaymentCardId(order) && couponName != null) {
                    GenericResponse validateCoupon = validateCoupon(order, couponName);
                    if (validateCoupon != null && validateCoupon.getMessage() != null && validateCoupon.getMessage().equals(ErrorCouponMsg.ERROR_COUPON_FILTER_CARD_BIN.name())) {
                        LOG.warning("return " + HttpStatusCode.CONFLICT.getCode() + " " + errorCouponProblem);
                        CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
                        return new CreateOrderResponse<>(HttpStatusCode.CONFLICT.getCode(), HttpStatusCode.CONFLICT.getStatusName(), errorCouponProblem, null);
                    }
                }
            }


            OrderUtil.deleteTipPriceZero(deliveryOrderItemList);
            checkTipsQuantity(deliveryOrderItemList);

            // TODO: Validacion para evitar cobrar productos que no esten visibles al usuario (APPS - Provedores externos)
            if (Objects.nonNull(order.getItemList()) && !order.getItemList().isEmpty()) {
//            valida Items de dataStore y los items parametro
//            LOG.info("deliveryOrderItemList 1 ->  " + deliveryOrderItemList.size());
                Map<Long, Integer> mapValidationItem = order.getItemList().stream().collect(ImmutableMap.toImmutableMap(DeliveryOrderItem::getId, DeliveryOrderItem::getQuantitySold));
//            LOG.info("deliveryOrderItemList 2 ->  " + deliveryOrderItemList.size());
                if (Objects.nonNull(deliveryOrderProvidersList) && !deliveryOrderProvidersList.isEmpty()) {
//                method createOrder ->  Validando Items
                    deliveryOrderProvidersList.forEach(provider ->
                            provider.getItemList().removeIf(item -> !mapValidationItem.containsKey(item.getId())));
                    // Validación para evitar que los items se dupliquen
                    Map<Long, Integer> mapProviderItem = new HashMap<>();
                    deliveryOrderProvidersList.stream()
                            .filter(provider -> (Objects.nonNull(provider.getItemList()) && !provider.getItemList().isEmpty()))
                            .forEach(provider -> provider.getItemList().parallelStream().forEach(itemProvider -> {
                                        mapProviderItem.put(itemProvider.getId(), itemProvider.getQuantitySold());
                                    })
                            );
                    // Elimina item de proveedores del listado normal de items
                    deliveryOrderItemList.removeIf(item -> mapProviderItem.containsKey(item.getId()));
                }
            }


//        LOG.info("createOrder: has picking date ---> " + order.getPickingDate());
            if (!Objects.isNull(order.getPickingDate()) && verifyHighDemand(order, closerStoreId)) {
//            LOG.info("createOrder: isHigh Demand -> TRUE");
                setCufPerHighDemand(deliveryOrderItemList, closerStoreId);
            }

//        deliveryOrderItemList.forEach(item -> {
//            LOG.info("Item -> " + item.getId());
//        });

            String orderRequest = Orders.createOrderJson(order, deliveryOrderItemList, deliveryOrderProvidersList).toString();
            LOG.info("createOrderJson ->  " + (orderRequest != null ? orderRequest : " es null"));

            CreateOrderRequestCore requestCore;
            Gson gson = new Gson();
            requestCore = gson.fromJson(orderRequest, CreateOrderRequestCore.class);

            if (Objects.nonNull(order.getShippingCostItemsMarkeplaceRequest())) {
                requestCore.setShippingCostItemsMarkeplaceRequest(order.getShippingCostItemsMarkeplaceRequest());
            }

            if (Objects.nonNull(deliveryOrderSavedShoppingCart.getUrlPrescription())) {
                requestCore.setUrlPrescriptionOptics(deliveryOrderSavedShoppingCart.getUrlPrescription());
            }

//        LOG.info("createOrder customer id before ----> " + requestCore.getCustomerId());
            if (requestCore.getCustomerId() != null && requestCore.getCustomerId() == 0) {
                // call service oms get customer id.
                CustomerAddressResponse customerAddressResponse = ApiGatewayService.get().getCustomerByAddressId(requestCore.getCustomerAddressId());
                if (customerAddressResponse != null && customerAddressResponse.getData() != null) {
                    long customerIdAux = (long) customerAddressResponse.getData().getIdCustomer();
                    requestCore.setCustomerId(customerIdAux);
                }

            }

            if (requestCore.getCustomerId() <= 0) {
                LOG.warning("return " + HttpStatusCode.NOT_FOUND.getCode() + " " + userNotFound);
                CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
                return new CreateOrderResponse<>(HttpStatusCode.NOT_FOUND.getCode(), HttpStatusCode.NOT_FOUND.getStatusName(), userNotFound, null);
            }

            try {
                final int storeId = closerStoreId;
                LOG.info("STOREID REQUEST ALGOLIA => " + storeId);
                requestCore.getItems().forEach(item -> {
                    LOG.info("ID REQUEST ALGOLIA => " + item.getItemId());
                    final ItemAlgolia itemAlgolia = APIAlgolia.getItemAlgolia(item.getItemId() + "" + storeId);
                    if (itemAlgolia != null) {
                        final int totalStock = itemAlgolia.getTotalStock();
                        if (totalStock > 0 && item.getQuantityRequested() > totalStock && !isScanAndGo) {
//                        LOG.info("QuantityRequested ANTES:" + item.getQuantityRequested());
                            item.setQuantityRequested(totalStock);
//                        LOG.info("QuantityRequested DESPUES:" + item.getQuantityRequested());
                        }
                    }
                });
            } catch (Exception e) {
                LOG.warning(errorCreateOrderAlgolia);
            }

            if (Objects.nonNull(order.getCustomerIdCallCenter())) {
//            Insertando el customer del call
                requestCore.setCustomerIdCallCenter(order.getCustomerIdCallCenter());
                requestCore.setSource(Constants.SOURCE_CALL_CENTER);
            }

            Boolean isPSE = false;

            if (PaymentTypeEnum.PSE.getId() == order.getPaymentType().getId()) {
                isPSE = true;
                requestCore.setFinancialInstitutions(order.getFinancialInstitutions());
                requestCore.setTypePersonPSE(order.getTypePersonPSE());
                requestCore.setIpAddress(order.getIpAddress());
            /*CreateOrderRequestCore identification = new CreateOrderRequestCore();
            identification.setIdentification(order.getIdentification());*/
                requestCore.setIdentification(order.getIdentification());
            }

            if (order.getSelfCheckout() != null && order.getSelfCheckout().getIsSelfCheckout() && order.getSelfCheckout().getIdBox() != null) {
                requestCore.setSelfCheckout(order.getSelfCheckout());
            }

            if (customerCoupon != null && Objects.nonNull(customerCoupon.getCouponId())) {
                final Ref<Coupon> coupon = customerCoupon.getCouponId();
                if (coupon.get() != null && coupon.get().getName() != null) {
                    couponName = coupon.get().getName();
                }
            }

            if (isPrime && !isPrimeMixed) {
                requestCore.setPrimeMixedPSE(false);
            }

            validateStockZero(requestCore);

            validateSetOptimalRoutePopInformation(order, requestCore);

            this.validBuildCodeNumberApp(httpServletRequest, requestCore);

            CreateOrderResponse<CreatedOrder> createOrderResponse = createOrderViaBackend3V2(requestCore, order, orderJSON, TraceUtil.getXCloudTraceId(httpServletRequest), idCustomerWebSafe);

            if (Objects.nonNull(createOrderResponse.getCode()) && createOrderResponse.getCode().equals("Created")) {

                orderJSON = createOrderResponse.getData();
                if (orderJSON.getId() == 0 && PaymentTypeEnum.TRANSACCIONES_EN_LINEA.getId() == order.getPaymentType().getId()) {
                    CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
                    LOG.warning("return " + HttpStatusCode.CONFLICT.getCode() + " " + errorPaymentOnline);
                    return new CreateOrderResponse<>(HttpStatusCode.CONFLICT.getCode(), HttpStatusCode.OK.getStatusName(), errorPaymentOnline, null);
                }

                if (orderJSON.getId() <= 0) {
                    LOG.warning("return " + HttpStatusCode.CONFLICT.getCode() + " " + errorCreateOrderOms);
                    CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
                    return new CreateOrderResponse<>(HttpStatusCode.CONFLICT.getCode(), HttpStatusCode.CONFLICT.getStatusName(), errorCreateOrderOms, null);
                }
//            LOG.warning("New Order ->  " + orderRequest);
                LOG.info("Result New Order ->  " + orderJSON);

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
                orderJSON.setItems(itemOrders);

                List<ProviderOrder> providers = new ArrayList<>();
                ProviderOrder provider = new ProviderOrder();
                provider.setName("");
                provider.setEmail("");
                provider.setDeliveryPrice(0);
                provider.setItems(itemOrders);
                providers.add(provider);
                orderJSON.setProviders(providers);

                //LOG.warning(String.valueOf(orderJSON.getId()));
                DeliveryOrder deliveryOrder = savedDeliveryOrderInDataStore(customerKey);

                if (deliveryOrder == null) {
                    LOG.warning("return " + HttpStatusCode.CONFLICT.getCode() + " " + deliveryOrderNotFound);
                    CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
                    return new CreateOrderResponse<>(HttpStatusCode.CONFLICT.getCode(), HttpStatusCode.CONFLICT.getStatusName(), deliveryOrderNotFound, null);
                }

                deliveryOrder.setIdOrder(orderJSON.getId());
                deliveryOrder.setAddress(orderJSON.getAddress());
                deliveryOrder.setIdAddress(order.getIdAddress());
                deliveryOrder.setAddressDetails(order.getAddressDetails());
                deliveryOrder.setPaymentType(order.getPaymentType());

                //
                if (order.getCustomerIdCallCenter() != null) {
                    deliveryOrder.setSource(Constants.SOURCE_CALL_CENTER);
                } else {
                    deliveryOrder.setSource(order.getSource());
                }

                if (!PaymentTypeEnum.PSE.getId().equals(order.getPaymentType().getId())) {
                    deliveryOrder.setCurrentStatus(0);
                }
                deliveryOrder.setIdFarmatodo(order.getIdFarmatodo());
                deliveryOrder.setLastStatus(ORDER_CREATED);
                deliveryOrder.setActive(true);

                if (!isPrimeMixed && !isPrime) {
                    deliveryOrder.setDeliveryType(order.getDeliveryType());
                } else if (isPrimeMixed) {
                    deliveryOrder.setDeliveryType(order.getDeliveryType());
                } else {
                    deliveryOrder.setDeliveryType(DeliveryType.SCANANDGO);
                }

                if (Objects.nonNull(orderJSON.getQrCode())) {
                    deliveryOrder.setQrCode(orderJSON.getQrCode());
                }
                //Consultar el resumen de la orden creada, para obtener valor de domicilio
                try {
//                LOG.info("method getOrderSumary: -> orderID:" + orderJSON.getId());
                    GetOrderSumary orderSumary = ApiGatewayService.get().getOrderSumary(orderJSON.getId());
                    if (orderSumary != null && orderSumary.getData() != null) {
//                    LOG.info("orderJSON.getDeliveryPrice() -> " + orderSumary.getData().getDeliveryValue());
                        deliveryOrder.setDeliveryPrice(orderSumary.getData().getDeliveryValue());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    LOG.severe("Error al consulatar resumen de orden! " + e.getMessage());
                }

                // if ScanAndGO
                if (isOrdenScanAndGo(order) && !isPrime) {
                    if (Objects.nonNull(order.getPaymentType()) && PaymentTypeEnum.DATAFONOS.getId().equals(order.getPaymentType().getId())) {
                        deliveryOrder.setLastStatus(ORDER_CREATED);

                    } else {
                        deliveryOrder.setLastStatus(ORDER_DELIVERED);
                        deliveryOrder.setActive(false);
                    }
                }

                if (isPrime && deliveryOrder.getDeliveryType().equals(DeliveryType.SCANANDGO) && !isPrimeMixed) {
                    deliveryOrder.setLastStatus(ORDER_DELIVERED);
                    deliveryOrder.setActive(false);
                }

                Timestamp stamp = new Timestamp(orderJSON.getCreateDate());
                Date date = new Date(stamp.getTime());
                deliveryOrder.setCreateDate(date);
                //if (orderJSON.getUpdateShopping()) {
                //    String getOrderURL = URLConnections.URL_ORDER + "/" + orderJSON.getId();
                //    DeliveryOrder processedOrder = CoreConnection.getRequest(getOrderURL, DeliveryOrder.class);
                //    deliveryOrder.updateShopping(processedOrder);
                //}
                if (order.getPickingDate() != null) {
                    deliveryOrder.setPickingDate(order.getPickingDate());
                }

                //Fix para borrar proveedor si no tiene items
                List<DeliveryOrderProvider> deleteProviders = new ArrayList<>();
//            LOG.info("tamaño proveedores : " + deliveryOrderProvidersList.size() + " list " + deliveryOrderProvidersList);
                for (DeliveryOrderProvider oderProvider : deliveryOrderProvidersList) {
                    if (oderProvider.getItemList() == null || oderProvider.getItemList().isEmpty()) {
                        deleteProviders.add(oderProvider);
                    }
                }

                ofy().delete().entities(deleteProviders);
//            LOG.info("savin pre save delivery {}" + saving);
                if (saving > 0) {
                    deliveryOrder.setSavingPrime(saving);
                }

                if(order.getDeliveryType().equals(DeliveryType.PROVIDER)){
                    deliveryOrder.setDeliveryType(DeliveryType.PROVIDER);
                }

                Key<DeliveryOrder> deliveryOrderKey = ofy().save().entity(deliveryOrder).now();

                if (Objects.nonNull(itemsNewOrder) && !itemsNewOrder.isEmpty()) {
                    try {
                        // Crea una orden con lo items sobrantes
//                    LOG.info("Crea una nueva orden para los items ->  " + itemsNewOrder.size());
                        createDeliveryOrder(customerKey, deliveryOrder.getDeliveryType().getDeliveryType(), itemsNewOrder, deliveryOrderProvidersList);
                    } catch (Exception ex) {
                        LOG.warning("No fue posible crear el nuevo Carrito con los items no utilizados " + ex.getMessage());
                    }
                }

                // Save tracing state
                //TODO verificar si es necesario o esta muerto codigo se puede eliminar
                if (Objects.nonNull(orderJSON.getTracing()) && !orderJSON.getTracing().isEmpty()) {
                    CreatedOrder finalOrderJSON = orderJSON;
                    DeliveryOrder finalOrder3 = order;
                    orderJSON.getTracing().forEach(tracing -> {
                        tracing.setIdTracing(UUID.randomUUID().toString());
                        tracing.setDeliveryOrderId(Ref.create(deliveryOrderKey));
                        if (tracing.getStatus() == 12) {
                            int responseSms = 0;
                            try {
                        /*responseSms = supportMethods.sendSms(order.getCustomerPhone(),
                                "Bienvenido a farmatodo. La transaccion asociada a tu orden "∫
                                        + finalOrderJSON.getId() + " fue aprobada");
*/
                                final SendSMSCloudFunctionReq request = new SendSMSCloudFunctionReq(order.getCustomerPhone(),
                                        MsgUtilAlgolia.obtainMsgAlgolia(MsgSmsEnum.MESSAGE_ORDER_CODE).replace("{orderId}", Long.toString(finalOrderJSON.getId())));
                                CloudFunctionsService.get().postSendSms(request);
                            } catch (IOException | BadRequestException e) {
                                LOG.warning("Error");
                            }
                            //LOG.warning("Response sms " + responseSms);
                        }
                    });
                    Tracing tracing = orderJSON.getTracing().get(0);
                    ofy().save().entity(tracing);
                }

                addMarcaCategorySubcategorieAndItemUrl(orderJSON);

                if (requestCore.getSelfCheckout() != null && requestCore.getSelfCheckout().getIsSelfCheckout() && requestCore.getSelfCheckout().getIdBox() != null) {
                    SelfCheckoutAlgolia selfCheckoutAlgolia = APIAlgolia.getSelfCheckout();
                    if (!Objects.isNull(selfCheckoutAlgolia) && !Objects.isNull(selfCheckoutAlgolia.getCustomerByStoresAndBox())) {
                        Optional<SelfCheckoutListAlgolia> selfCheckoutListAlgolia = selfCheckoutAlgolia.getCustomerByStoresAndBox().stream()
                                .filter(checkoutListAlgolia -> Objects.equals(checkoutListAlgolia.getStore(), requestCore.getStoreId()) && Objects.equals(checkoutListAlgolia.getBox(), requestCore.getSelfCheckout().getIdBox()))
                                .limit(1).findFirst();
                        if (selfCheckoutListAlgolia.isPresent()) {
                            deleteCoupon(customerKey);
                        }
                    }
                }

                deliveryOrderData = getOrderMethod(deliveryOrder, orderJSON.getId(), false, false);
                orderJSON.setOrderData(deliveryOrderData);
                ApiGatewayService.get().updateStratumBraze(requestCore.getCustomerId().toString());
                if (saving > 0) {
                    ApiGatewayService
                            .get().sendSavingNoPrime(getSavingCustomerRequest((long) order.getIdFarmatodo(), saving, UpdateTypeSavingEnum.ADD));
                }

                try {
                    Long sessionId = Long.parseLong(httpServletRequest.getHeader("amplitudeSessionId"));
                    if (sessionId != null) {
                        AmplitudeSessionRequest request = new AmplitudeSessionRequest();
                        request.setOrderId(orderJSON.getId());
                        request.setSessionId(sessionId);
//                LOG.info("request -> " + request.toString());
                        ApiGatewayService.get().saveAmplitudeSessionId(request);
                    }
                } catch (Exception e) {
                    LOG.warning(e.getMessage());
                }

                if ((isPrime)) {
                    try {
                        orderJSON.setIdOrderPrime(orderJSON.getId());
                    } catch (Exception e) {
                        LOG.info("Ocurrio un problema -> " + e.getMessage());
                    }

                    orderJSON.setPrimeGeneral(true);

                    if (!isPSE) {
                        orderJSON.setPrime(true);
                        orderJSON.setPrimeGeneral(false);
                    }
                } else {
                    orderJSON.setPrimeGeneral(false);
                    orderJSON.setPrime(false);
                }

                if (order.getPaymentType().getName().equals(PaymentTypeEnum.EFECTIVO.name()) || order.getPaymentType().getName().equals(PaymentTypeEnum.DATAFONOS.name()) && Objects.nonNull(deliveryOrderData.getCustomerPhone()) && Objects.nonNull(deliveryOrderData.getDeliveryType()) && deliveryOrderData.getDeliveryType().equals(DeliveryType.EXPRESS)) {
                    try {
                        final SendSMSCloudFunctionReq request = new SendSMSCloudFunctionReq(deliveryOrderData.getCustomerPhone(),
                                MsgUtilAlgolia.obtainMsgAlgolia(MsgSmsEnum.MESSAGE_ALERT_METHOD_PAY));
                        CloudFunctionsService.get().postSendSms(request);
                    } catch (Exception e) {
                        LOG.info("No se pudo enviar sms a " + deliveryOrderData.getCustomerPhone());
                    }
                }
                addRMSclasses(orderJSON);
                sendEventCreate(orderJSON, requestCore.getCustomerId(), order.getPaymentType().getId());
                try {
                    LOG.info("segundo llamado copyClosedSession");
                    TalonOneService talonOneService = new TalonOneService();
                    talonOneService.copyClosedSession(String.valueOf(orderJSON.getOrderData().getIdFarmatodo()), String.valueOf(orderJSON.getId()), orderJSON, idCustomerWebSafe);
                    deleteCouponTalonOne(orderJSON.getOrderData().getIdFarmatodo(), idCustomerWebSafe);
                    Key<User> userKey = Key.create(idCustomerWebSafe);
                    User user = users.findUserByKey(userKey);
                    deleteCacheDeductDiscount(idCustomerWebSafe,String.valueOf(user.getId()));
                } catch (Exception e) {
                    LOG.info("Error total de Talon One: " + e);
                }
                try {
                    LOG.info("CreateOrderResponse:#" + requestCore.getCustomerId() + " Gson -> " + new Gson().toJson(orderJSON));
                } catch (Exception e) {
                    LOG.info("No se pudo serializar Json de respuesta de creación de orden.");
                }
                createOrderResponse.setData(orderJSON);
                LOG.info("createOrderResponse -> " + createOrderResponse.toString());
            } else {
//                LOG.info("createOrderResponse no Created-> " + createOrderResponse.toString());
//                DeliveryOrder deliveryOrderOrder = ofy().load().type(DeliveryOrder.class).filter("currentStatus", 0).ancestor(Ref.create(customerKey)).first().now();
//                deleteCoupon(customerKey);
//                if (deliveryOrderOrder != null) {
//                    ofy().delete().entity(deliveryOrderOrder).now();
//                }
                LOG.info("createOrderResponse no Created-> " + createOrderResponse.toString());
                CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
                return createOrderResponse;
            }
            CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
            return createOrderResponse;
        } catch (Exception e) {
            LOG.severe("Ocurrio un error al crear la orden => " + Arrays.toString(e.getStackTrace()));
            CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
//            DeliveryOrder deliveryOrderOrder = ofy().load().type(DeliveryOrder.class).filter("currentStatus", 0).ancestor(Ref.create(customerKey)).first().now();
//            deleteCoupon(customerKey);
//            if (deliveryOrderOrder != null) {
//                ofy().delete().entity(deliveryOrderOrder).now();
//            }
            return new CreateOrderResponse<>(HttpStatusCode.INTERNAL_SERVER_ERROR.getCode(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusName(), errorCreateOrderOms, null);
        }
    }

    private void validBuildCodeNumberApp(HttpServletRequest httpServletRequest, CreateOrderRequestCore requestCore) {
        String buildCodeNumberApp = httpServletRequest.getHeader(APP_BUILD_CODE_HEADER_NAME);
        if (buildCodeNumberApp != null && buildCodeNumberApp.matches(IS_INTEGER_POSITIVE_REGEX)) {
            requestCore.setBuildCodeNumberApp(Integer.valueOf(buildCodeNumberApp));
        }

    }

    private void removeItemsMarketplaceFromItemsExpressList(List<DeliveryOrderItem> deliveryOrderItemList) {

        boolean hasItemsExpress = deliveryOrderItemList.stream().anyMatch(deliveryOrderItem -> !deliveryOrderItem.isOnlyOnline());

        if(hasItemsExpress){
            deliveryOrderItemList.removeIf(deliveryOrderItem -> deliveryOrderItem.isOnlyOnline() &&  !StringUtils.isEmpty(deliveryOrderItem.getUuidItem()));
        }

    }

    private CreateOrderResponse<CreatedOrder> handleEmptyCart(String emptyShoppingCart, RequestSourceEnum sourceE, int idFarmatodo) {
        CreatedOrder createdEmptyCartOrder = null;
        if (isAndroidOrIOS(sourceE)) {
            createdEmptyCartOrder = createEmptyCartOrder();
        }

        CachedDataManager.deleteKeyIndex(String.valueOf(idFarmatodo), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
        return new CreateOrderResponse<>(HttpStatusCode.BAD_REQUEST.getCode(), HttpStatusCode.BAD_REQUEST.getStatusName(), emptyShoppingCart, createdEmptyCartOrder);
    }

    private boolean isAndroidOrIOS(RequestSourceEnum sourceE){
        return sourceE.equals(RequestSourceEnum.ANDROID) || sourceE.equals(RequestSourceEnum.IOS);
    }

    private CreatedOrder createEmptyCartOrder(){
        CreatedOrder createdOrder = new CreatedOrder();
        createdOrder.setIsEmptyCart();
        createdOrder.setAddress("");
        return createdOrder;
    }

    private static DeliveryOrder savedDeliveryOrderInDataStore(Key<Customer> customerKey) {
        final int MAX_RETRIES=2;
        int retries=0;
        boolean success=false;
        DeliveryOrder deliveryOrder = null;
        int waitTime = 100; // milliseconds
        while(!success && retries < MAX_RETRIES) {
            try {
                deliveryOrder = ofy().load().type(DeliveryOrder.class).filter("currentStatus", 1).ancestor(Ref.create(customerKey)).first().now();
                success=true;
            } catch (Exception e) {
                LOG.severe("No fue cargada la entidad DeliveryOrder en Datastore/error "+e.getMessage());
                retries++;
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException ex) {
                    LOG.severe("error en tiempo de espera:"+ ex.getMessage());
                    Thread.currentThread().interrupt();
                }
            }
        }
        return deliveryOrder;
    }

    private void validateStockZero(CreateOrderRequestCore requestCore) {
        if (Objects.nonNull(requestCore) && Objects.nonNull(requestCore.getItems()) && !requestCore.getItems().isEmpty()) {
            requestCore.getItems().removeIf(items -> Objects.equals(items.getQuantityRequested(), Constants.STOCK_ZERO_ALGOLIA));
        }
    }

    private boolean isNationalOrEnvialoYa(DeliveryType deliveryType){
        return deliveryType.equals(DeliveryType.NATIONAL) || deliveryType.equals(DeliveryType.ENVIALOYA);
    }

    private boolean isEfectivoOrDataphone(PaymentType paymentType){
        return paymentType != null && (paymentType.getId() == PaymentTypeEnum.EFECTIVO.getId() || paymentType.getId() == PaymentTypeEnum.DATAFONOS.getId());
    }

    private boolean isFreeDeliveryOrderPrime(String idCustomerWebSafe, DeliveryOrder order, DeliveryOrder deliveryOrderSavedShoppingCart) {
        try {
            List<DeliveryOrderProvider> deliveryOrderProviderList = new ArrayList<>();
            deliveryOrderProviderList = ofy().load().type(DeliveryOrderProvider.class).ancestor(deliveryOrderSavedShoppingCart).list();
            ShoppingCartJson shoppingCartJson = new ShoppingCartJson();
            shoppingCartJson.setIdCustomerWebSafe(idCustomerWebSafe);
            shoppingCartJson.setDeliveryType(order.getDeliveryType());
            shoppingCartJson.setIdStoreGroup(26);
            if (checkIfAppliesForFreeDelivery(order, order.getItemList(), shoppingCartJson, order.getSource())) {
                return Boolean.TRUE;
            } else if (isOrderOnlyPrime(order.getItemList(), deliveryOrderProviderList)) {
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }

        } catch (Exception e) {
            LOG.warning("Ocurrio un problema validando si es de domicilio gratis -> " + e.getMessage());
            return false;
        }
    }

    private int getTypeSubscriptionPrimeQuantity(List<DeliveryOrderItem> items) {
        int quantity = 0;
        for (DeliveryOrderItem deliveryOrderItem : items) {
            Long item = deliveryOrderItem.getId();
            if (primeUtil.isItemPrime(item)) {
                quantity = deliveryOrderItem.getQuantitySold();
            }
        }
        return quantity;
    }

    private Long getTypeSubscriptionPrime(DeliveryOrder order) {
        Long response = null;

        List<DeliveryOrderItem> items = order.getItemList();

        try {
            OrderUtil.deleteTipPriceZero(items);
        } catch (Exception e) {
            LOG.info("Ocurrio un problema eliminando la propina 0 error -> " + e.getMessage());
        }

        int itemSize = items.size();

        for (int i = 0; i < itemSize; i++) {
            Long item = items.get(i).getId();
            if (primeUtil.isItemPrime(item)) {
                response = item;
                break;
            }
        }

        return response;
    }


    private double getSaving(DeliveryOrder deliveryOrderSavedShoppingCart, HttpServletRequest httpServletRequest,
                             long customerId, DeliveryType deliveryType, String storeId, String idCustomerWebSafe,
                             String token, String tokenIdWebSafe) throws BadRequestException, IOException {

        RequestSourceEnum sourceEnum = ftdUtilities.getSourceFromRequestHeaderForPays(httpServletRequest);

        if (sourceEnum == RequestSourceEnum.DEFAULT) {
            sourceEnum = RequestSourceEnum.IOS;
        }


        if (deliveryOrderSavedShoppingCart == null) {
            LOG.info("No existe el deliveryOrderSavedShoppingCart" + deliveryOrderSavedShoppingCart);
            return 0.0;
        }

        if (deliveryType == null) {
            LOG.info("No existe el deliveryType" + deliveryType);
        }

        double deliveryValue = Objects.nonNull(deliveryOrderSavedShoppingCart.getDeliveryPrice()) ? deliveryOrderSavedShoppingCart.getDeliveryPrice() : 0D;

        boolean isScanAndGoFordelivery = isOrdenScanAndGo(deliveryOrderSavedShoppingCart);
        boolean isScanAndGo = isOrdenScanAndGo(deliveryOrderSavedShoppingCart);

        talonOneComboService.validAndSaveItemsCombo(storeId, idCustomerWebSafe, token, tokenIdWebSafe, deliveryOrderSavedShoppingCart, deliveryType);

        List<DeliveryOrderItem> deliveryOrderItemList = ofy().load().type(DeliveryOrderItem.class).ancestor(deliveryOrderSavedShoppingCart).list();

        if (deliveryOrderItemList == null) {
            LOG.info("No existe el deliveryOrderItemList" + deliveryOrderItemList);
            return 0.0;
        }

        // Fix items Duplicados
        validateDuplicateItems(deliveryOrderItemList);

        List<DeliveryOrderItem> itemsScanAndGo = new ArrayList<>(deliveryOrderItemList);
        List<DeliveryOrderItem> itemsNewOrder = new ArrayList<>();

        if (isScanAndGo) {
            //set store princial when is scan and go
            itemsNewOrder = Objects.nonNull(itemsScanAndGo) ? itemsScanAndGo.stream().filter(it -> (Objects.isNull(it.getScanAndGo()) || !it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon())).collect(Collectors.toList()) : null;
            if (Objects.nonNull(itemsScanAndGo) && itemsScanAndGo.stream()
                    .filter(it -> (Objects.isNull(it.getScanAndGo()) || !it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon())).findFirst().isPresent()) {
                itemsScanAndGo.removeIf(it -> ((Objects.isNull(it.getScanAndGo()) || !it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon())));
            }
        } else {
            itemsNewOrder = Objects.nonNull(itemsScanAndGo) ? itemsScanAndGo.stream().filter(it -> (Objects.nonNull(it.getScanAndGo()) && it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon())).collect(Collectors.toList()) : null;
            if (Objects.nonNull(itemsScanAndGo) && itemsScanAndGo.stream()
                    .filter(it -> ((Objects.nonNull(it.getScanAndGo()) && it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon()))).findFirst().isPresent()) {
                itemsScanAndGo.removeIf(it -> ((Objects.nonNull(it.getScanAndGo()) && it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon())));
            }
        }

//        if (Objects.nonNull(itemsNewOrder)) {
////            LOG.info("Items itemsNewOrder -> " + itemsNewOrder.size());
//        }

        deliveryOrderItemList = itemsScanAndGo;

        List<DeliveryOrderProvider> deliveryOrderProvidersList = ofy().load().type(DeliveryOrderProvider.class).ancestor(deliveryOrderSavedShoppingCart).list();
        if (deliveryOrderProvidersList != null && !deliveryOrderProvidersList.isEmpty()) {
            deliveryOrderItemList.removeIf(it ->
                    deliveryOrderProvidersList.stream().
                            anyMatch(it2 -> it2.getItemList().stream().anyMatch(it3 -> it3.getId() == it.getId())));
        }
        if (deliveryOrderItemList == null) {
            LOG.info("No existe el deliveryOrderItemList" + deliveryOrderItemList);
            return 0.0;
        }

        // validar los tips
        Optional<TipConfig> tipConfigOptional = GrowthBookConfigLoader.getTipsConfig(Constants.CUSTOMER_ANONYMOUS);

        // delete tips in memory for prime
        if (tipConfigOptional.isPresent()) {
            deleteTipsPrimeCalc(deliveryOrderItemList, tipConfigOptional.get());
        }

        List<Long> toRemove = new ArrayList<>();
        for (DeliveryOrderItem deliveryOrderItem : deliveryOrderItemList) {
            long id = getIdItemPrime(deliveryOrderItem);
            if (id != 0) {
                if (deliveryOrderItemList.size() == 1) {
                    return 0.0;
                } else if (deliveryOrderItemList.size() == 2) {
                    if (id == deliveryOrderItem.getId()) {
                        toRemove.add(id);
                    }
                }
            }
        }

        if (toRemove.size() == 2) {
            deliveryOrderItemList.removeIf(it -> toRemove.contains(it.getId()));
        }


        double totalPrime = 0.0;
        double total = 0.0;
        for (int i = 0; i < deliveryOrderItemList.size(); i++) {
            DeliveryOrderItem item = deliveryOrderItemList.get(i);
            if (item != null) {
                final String objectID = item.getId() + "26";
                try {
                    ItemAlgolia itemAlgolia = APIAlgolia.getItemAlgolia(objectID);
                    if (Objects.nonNull(itemAlgolia) && Objects.nonNull(itemAlgolia.getPrimePrice()) && Objects.nonNull(itemAlgolia.getFullPrice()) && itemAlgolia.getPrimePrice().doubleValue() > 0.0) {
                        double amount = item.getQuantitySold() * itemAlgolia.getPrimePrice();

                        double fullAmount;
                        if (Objects.nonNull(itemAlgolia.getOfferPrice()) && itemAlgolia.getOfferPrice() > 0.0) {
                            fullAmount = item.getQuantitySold() * itemAlgolia.getOfferPrice();

                        } else {
                            fullAmount = item.getQuantitySold() * itemAlgolia.getFullPrice();

                        }
                        totalPrime = totalPrime + amount;
                        total = total + fullAmount;
                    } else if (Objects.nonNull(itemAlgolia) && Objects.nonNull(itemAlgolia.getFullPrice())) {

                        double amount;
                        double fullAmount;
                        if (Objects.nonNull(itemAlgolia.getOfferPrice()) && itemAlgolia.getOfferPrice() > 0.0) {
                            amount = item.getQuantitySold() * itemAlgolia.getOfferPrice();
                            fullAmount = item.getQuantitySold() * itemAlgolia.getOfferPrice();
                        } else {
                            amount = item.getQuantitySold() * itemAlgolia.getFullPrice();
                            fullAmount = item.getQuantitySold() * itemAlgolia.getFullPrice();
                        }
                        totalPrime = totalPrime + amount;
                        total = total + fullAmount;
                    }
                } catch (Exception e) {
                    LOG.info("e: " + e);
                }
            }
        }

        if (deliveryValue == 0.0) {
            ValidateOrderReq validateOrderReq = new ValidateOrderReq();
            validateOrderReq.setSource(sourceEnum.toString());
            validateOrderReq.setCustomerId((int) customerId);
            validateOrderReq.setStoreId(26);
            validateOrderReq.setDeliveryType(deliveryType.getDeliveryType());
            if(Objects.nonNull(deliveryOrderSavedShoppingCart) && Objects.nonNull(deliveryOrderSavedShoppingCart.getFarmaCredits())) {
                validateOrderReq.setFarmaCredits(deliveryOrderSavedShoppingCart.getFarmaCredits());
            }
            validateOrderReq.setTalonOneData(deliveryOrderSavedShoppingCart.getTalonOneData());
            validateOrderReq.setItems(new ArrayList<>());
            //enviar deliveryOrderItemList por  items
            for (DeliveryOrderItem deliveryOrderItem : deliveryOrderItemList) {
                ValidateOrderReq.Item item = new ValidateOrderReq().new Item();
                item.setItemId((int) deliveryOrderItem.getId());
                item.setQuantityRequested(deliveryOrderItem.getQuantitySold());
                validateOrderReq.getItems().add(item);
            }
            validateOrderReq.setIdCustomerWebSafe(idCustomerWebSafe);

            Response<ValidateOrderBackend3> response = ApiGatewayService.get().validateOrder(validateOrderReq, "");
            if (response != null && response.code() == 200) {
                ValidateOrderBackend3 validateOrderBackend3 = response.body();
                if (validateOrderBackend3 != null && validateOrderBackend3.getData() != null) {
                    deliveryValue = validateOrderBackend3.getData().getDeliveryValue();

                }
            }
        }
        if (deliveryOrderItemList == null || deliveryOrderItemList.isEmpty()) {
            return 0.0;
        }

        if (!deliveryType.equals(DeliveryType.EXPRESS)) {
            deliveryValue = 0.0;
        }
        double saving = total - totalPrime;

        try {
            String deliveryMinAmount = obtainMinAmount();
            if (deliveryMinAmount == null || deliveryMinAmount.equals("")) {
                throw new BadRequestException("BadRequest [deliveryMinAmount is null]");
            }
            double deliveryMinAmountDouble = Double.parseDouble(deliveryMinAmount);
            if (total >= deliveryMinAmountDouble) {
                saving = saving + deliveryValue;
            }
        } catch (Exception e) {
            LOG.warning("Error al obtener el deliveryMinAmount" + e);
            return 0.0;
        }

        return saving;
    }


    private BrazeEventCreate mapBraze(CreatedOrder order, Long userId, Long payMethod) {
        BrazeEventCreate brazeEventCreate = new BrazeEventCreate();
        brazeEventCreate.setUserId(String.valueOf(userId));
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            List<BrazeProperties> itemsData = new ArrayList<>();
            for (DeliveryOrderItem item : order.getOrderData().getItemList()) {
                BrazeProperties brazeProperties = new BrazeProperties();
                //validar null con ternario
                brazeProperties.setItem_id(String.valueOf(item.getId()));
                brazeProperties.setItem_name(item.getMediaDescription() == null ? "" : item.getMediaDescription());
                brazeProperties.setItem_variant(item.getGrayDescription() == null ? "" : item.getGrayDescription());
                brazeProperties.setItem_category(item.getCategorie() == null ? "" : item.getCategorie());
                brazeProperties.setItem_category2(item.getSubCategory() == null ? "" : item.getSubCategory());
                brazeProperties.setItem_department(item.getDepartments() == null || item.getDepartments().isEmpty() ? "" : item.getDepartments().get(0));
                brazeProperties.setBrand(item.getMarca() == null ? "" : item.getMarca());
                brazeProperties.setItem_quantity(item.getQuantitySold());
                brazeProperties.setItem_price(item.getFullPrice());
                brazeProperties.setItem_rms_group(item.getRms_group() == null ? "" : item.getRms_group());
                brazeProperties.setItem_rms_deparment(item.getRms_deparment() == null ? "" : item.getRms_deparment());
                brazeProperties.setItem_rms_class(item.getRms_class() == null ? "" : item.getRms_class());
                brazeProperties.setItem_rms_subclass(item.getRms_subclass() == null ? "" : item.getRms_subclass());
                brazeProperties.setOrder_id(String.valueOf(order.getId()));
                brazeProperties.setItem_price_prime(item.getPrimePrice());
                brazeProperties.setPayment_method(payMethod);

                itemsData.add(brazeProperties);

            }
            brazeEventCreate.setItemsData(itemsData);

        }
        return brazeEventCreate;
    }

    private void sendEventCreate(CreatedOrder order, Long userId, Long payMethod) {
        try {
            BrazeEventCreate braze = mapBraze(order, userId, payMethod);
            ApiGatewayService.get().sendEventCreate(braze);
        } catch (Exception e) {
            LOG.warning("Error al enviar a braze");
        }
    }


    private void deleteTipsPrimeCalc(List<DeliveryOrderItem> deliveryOrderItemList, TipConfig tipConfig) {
        deliveryOrderItemList.removeIf(item -> {

            if (tipConfig == null || tipConfig.getItemTips() == null) {
                return false;
            }
            return tipConfig.getItemTips()
                    .stream()
                    .anyMatch(itemTip -> itemTip.getItemId() != null && itemTip.getItemId().longValue() == item.getId());
        });
    }

    private long getIdItemPrime(DeliveryOrderItem deliveryOrderItemListr) {
        AtomicLong idItemPrime = new AtomicLong();
        try {
            if (primeUtil.isItemPrime(deliveryOrderItemListr.getId())) {
                idItemPrime.set(deliveryOrderItemListr.getId());

            }
        } catch (Exception e) {
            LOG.severe(e.getMessage());
        }

        return idItemPrime.get();

    }

    private String obtainMinAmount() throws BadRequestException {
        Optional<DeliveryFree> deliveryFree = APIAlgolia.getFreeDelivery();
        if (!deliveryFree.isPresent()) {
            throw new BadRequestException("BadRequest [deliveryFree is null]");
        }
        if (deliveryFree.get().getCampaigns() == null || deliveryFree.get().getCampaigns().isEmpty()) {
            throw new BadRequestException("BadRequest [deliveryFree.campaigns is null or empty]");
        }
        for (CampaignFree campaignFree : deliveryFree.get().getCampaigns()) {
            if (validateCampaign(campaignFree)) {
                if (campaignFree.getVariables() == null || campaignFree.getVariables().isEmpty()) {
                    throw new BadRequestException("BadRequest [deliveryFree.campaigns.variables is null or empty]");
                }
                for (VariablesFree variablesFree : campaignFree.getVariables()) {
                    if (!validateMinAmount(variablesFree).equalsIgnoreCase("")) {
                        return validateMinAmount(variablesFree);
                    }
                }


            }
        }
        return null;
    }

    //validate campaing
    private boolean validateCampaign(CampaignFree campaign) {
        if (campaign == null) {
            return false;
        }
        if (campaign.getCombinationToApply() == null || campaign.getCombinationToApply().isEmpty()) {
            return false;
        }
        if (campaign.getVariables() == null || campaign.getVariables().isEmpty()) {
            return false;
        }
        return campaign.getCombinationToApply().trim().equalsIgnoreCase("FREE_DELIVERY_FOR_CUSTOMER_PRIME");
    }

    private String validateMinAmount(VariablesFree variables) {
        if (variables == null) {
            return "";
        }
        if (variables.getKey() == null || variables.getKey().isEmpty()) {
            return "";
        }
        if (variables.getValues() == null || variables.getValues().isEmpty()) {
            return "";
        }
        if (variables.getKey().trim().equalsIgnoreCase("MIN_AMOUNT")) {
            if (variables.getValues().get(0) != null && !variables.getValues().get(0).isEmpty()) {
//                LOG.info("variables.values.get(0) = " + variables.getValues().get(0));
                return variables.getValues().get(0);
            }
        }
        return "";
    }

    private void checkTipsQuantity(List<DeliveryOrderItem> deliveryOrderItemList) {
        deliveryOrderItemList.forEach(this::checkTip);
    }
    

    private List<OrderItemsQuantity> itemsInShoppingCart(List<DeliveryOrderItem> itemList) {
        List<OrderItemsQuantity> responseList = new ArrayList<>();
        itemList.forEach(item -> {
            LOG.info("itemsInShoppingCart Item -> " + item);
            OrderItemsQuantity orderItemsQuantity = new OrderItemsQuantity();
            orderItemsQuantity.setId(item.getId());
            orderItemsQuantity.setQuantity(item.getQuantitySold());
            responseList.add(orderItemsQuantity);
        });

        return responseList;
    }

    private void addItemsNoPrimeV2(String idCustomerWebSafe, String token, String tokenIdWebSafe,
                                   List<OrderItemsQuantity> itemsInCart, DeliveryOrder order, HttpServletRequest httpServletRequest) {

        try {
            itemsInCart.forEach(item -> {
                final String itemSTR = String.valueOf(item.getId());
                if (!primeUtil.isItemPrime(item.getId())) {
                    try {
//                            Add item of DeliveryOrder original
                        addDeliveryOrderItem(token, tokenIdWebSafe, idCustomerWebSafe, Integer.valueOf(itemSTR), item.getQuantity(), 26, true,
                                "EXPRESS", null, null, null, null, null, null, null);
                    } catch (Exception e) {
                        LOG.info("Error -> " + e.getMessage());
                    }
                }

                if (!primeUtil.isItemPrime(item.getId()) && isTip(item.getId())) {
                    try {
                        this.addDeliveryOrderItem(token,
                                tokenIdWebSafe,
                                idCustomerWebSafe, Integer.valueOf(itemSTR), 1, 26,
                                false,
                                "EXPRESS",
                                "ADDED_FROM_SEARCH",
                                "TIP",
                                false,
                                null,
                                null,
                                null,
                                null
                        );
                    } catch (Exception e) {
                        LOG.severe("No se encuentra item en Algolia o Datastore o el producto no tiene stock segun la config de propina-item para el valor de:  "
                                + itemSTR);
                    }
                }
            });
        } catch (Exception e) {
            LOG.info("Error -> " + e.getMessage());
            LOG.warning("Ocurrio un error agregando items para dejar solo items normales.");
        }
    }

    private void addItemsNoPrime(String idCustomerWebSafe, String token, String tokenIdWebSafe,
                                 List<OrderItemsQuantity> itemsInCart, DeliveryOrder order, HttpServletRequest httpServletRequest) {
        OrderItemsQuantity itemTip = null;
        for (OrderItemsQuantity item : itemsInCart) {
            if (isTip(item.getId())) {
                itemTip = item;
            }
        }
        itemsInCart.remove(itemTip);
        try {
            itemsInCart.forEach(item -> {
                final String itemSTR = String.valueOf(item.getId());
                if (!primeUtil.isItemPrime(item.getId()) && !isTip(item.getId())) {
                    try {
                        addDeliveryOrderItem(token, tokenIdWebSafe, idCustomerWebSafe, Integer.valueOf(itemSTR), item.getQuantity(), 26, true,
                                "EXPRESS", null, null, null, null, null, null, null);
                    } catch (Exception e) {
                        LOG.info("Error -> " + e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            LOG.info("Ocurrio un error agregando items para dejar solo items que no son prime o tips. Error -> " + e.getMessage());
        }
        if (itemTip != null) {
            try {
                this.addDeliveryOrderItem(token,
                        tokenIdWebSafe,
                        idCustomerWebSafe, Integer.valueOf(String.valueOf(itemTip.getId())), 1, 26,
                        false,
                        "EXPRESS",
                        "ADDED_FROM_SEARCH",
                        "TIP",
                        false,
                        null,
                        null,
                        null,
                        null
                );
            } catch (Exception e) {
                LOG.severe("No se encuentra item en Algolia o Datastore o el producto no tiene stock segun la config de propina-item para el valor de:  "
                        + itemTip.getId());
            }
        }
    }

    private void addItemPrime(String idCustomerWebSafe,
                              String token,
                              String tokenIdWebSafe,
                              List<OrderItemsQuantity> itemsInCart
    ) {
        try {
            itemsInCart.forEach(item -> {
                final String itemSTR = String.valueOf(item.getId());
                if (primeUtil.isItemPrime(item.getId())
                        && !isTip(item.getId())
                ) {
                    try {
                        addDeliveryOrderItem(token, tokenIdWebSafe, idCustomerWebSafe, Integer.valueOf(itemSTR), item.getQuantity(), 26, true,
                                "EXPRESS", null, null, null, null, null, null, null);
                    } catch (Exception e) {
                        LOG.info("Error -> " + e.getMessage());
                    }
                }

            });
        } catch (Exception e) {
            LOG.warning("Error agregando item prime al carrito nuevamente -> " + e.getMessage());
        }
    }

    private boolean isOrderPrimeMixed(DeliveryOrder order) {
        List<DeliveryOrderItem> items = order.getItemList();
        List<DeliveryOrderItem> primeItems = new ArrayList<>();

        try {
            OrderUtil.deleteTipPriceZero(items);
        } catch (Exception e) {
            LOG.info("Ocurrio un problema eliminando la propina 0 error -> " + e.getMessage());
        }

        for (DeliveryOrderItem item : items) {
            Long itemID = item.getId();

            if (primeUtil.isItemPrime(itemID)) {
                primeItems.add(item);
            }
        }

        if (!primeItems.isEmpty() && primeItems.size() < items.size()) {
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    private void validateDeliveryTypeForPrime(DeliveryOrder order) {
        List<DeliveryOrderItem> items = order.getItemList();

        for (int i = 0; i < items.size(); i++) {
            Long item = items.get(i).getId();
            if (primeUtil.isItemPrime(item)) {
                order.setDeliveryType(DeliveryType.PRIME);
            }
        }
    }

    private Boolean isOrdenScanAndGo(final DeliveryOrder order) {
        return Objects.nonNull(order) && Objects.nonNull(order.getDeliveryType()) && isScanAndGo(order.getDeliveryType().getDeliveryType());
    }

    private boolean iscustomerPaymentCardId(final DeliveryOrder order) {
        return Objects.nonNull(order) && Objects.nonNull(order.getPaymentCardId());
    }

    private boolean iscustomerPaymentCard(final DeliveryOrder order) {
        return Objects.nonNull(order) && Objects.nonNull(order.getPaymentType()) && Objects.nonNull(order.getPaymentType().getId()) && order.getPaymentType().getId() == 3;
    }

    private boolean isOrderPrime(DeliveryOrder order) {

        boolean response = Boolean.FALSE;

        List<DeliveryOrderItem> items = order.getItemList();

        for (int i = 0; i < items.size(); i++) {
            Long item = items.get(i).getId();
            if (primeUtil.isItemPrime(item)) {
                response = Boolean.TRUE;
            }
        }
        return response;
    }

    private boolean isOrderOnlyPrime(List<DeliveryOrderItem> items,
                                     List<DeliveryOrderProvider> deliveryOrderProviderListToSave
    ) {
        boolean response = Boolean.FALSE;
        if (Objects.nonNull(deliveryOrderProviderListToSave)) {
            deliveryOrderProviderListToSave.forEach((providerItem) -> {
                items.removeIf(orderItem -> orderItem.getId() == providerItem.getItemList().get(0).getId());

            });
        }

        try {
            if (Objects.nonNull(items) && !items.isEmpty() && items.size() > 0) {
//                    Inicia la validación si es un carrito PRIME
                for (int i = 0; i < items.size(); i++) {
                    Long item = items.get(i).getId();
                    if (primeUtil.isItemPrime(item) || item.equals(236650616L)) {
                        response = Boolean.TRUE;
                    } else {
                        response = Boolean.FALSE;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            LOG.warning("Ocurrrio un problema validando los items en el metodo isOrderOnlyPrime().");
            return Boolean.FALSE;
        }

        return response;
    }

    private boolean isOrderPrimeV2(DeliveryOrder order) {
        List<DeliveryOrderItem> items = null;

        try {
            items = ofy().load().type(DeliveryOrderItem.class).ancestor(order).list();
        } catch (Exception e) {
            LOG.info("Al parecer el cliente no tiene items agregados al carrito");
            return false;
        }

        if (Objects.nonNull(items) && !items.isEmpty()) {
            for (DeliveryOrderItem item : items) {
                if (primeUtil.isItemPrime(item.getId())) {
                    return true;
                }
            }
        }

        return false;
    }

    private Boolean isScanAndGo(final DeliveryType deliveryType) {
        return Objects.nonNull(deliveryType) && isScanAndGo(deliveryType.getDeliveryType());
    }

    private Boolean isScanAndGo(final String deliveryType) {
        return Objects.nonNull(deliveryType) && !deliveryType.isEmpty() && DeliveryType.SCANANDGO.getDeliveryType().equals(deliveryType);
    }


    private void createDeliveryOrder(Key<Customer> customerKey, String deliveryType, final List<DeliveryOrderItem> itemsNewOrder, final List<DeliveryOrderProvider> deliveryOrderProviders) {
        DeliveryOrder deliveryOrder = new DeliveryOrder();
        deliveryOrder.setIdDeliveryOrder(UUID.randomUUID().toString());
        deliveryOrder.setIdCustomer(Ref.create(customerKey));
        deliveryOrder.setCurrentStatus(1);
        deliveryOrder.setCreateDate(new Date());
        if (deliveryType != null) {
            if (deliveryType == "EXPRESS" || deliveryType.equals("EXPRESS")) {
                deliveryOrder.setDeliveryType(DeliveryType.EXPRESS);
            } else if (deliveryType == "NATIONAL" || deliveryType.equals("NATIONAL")) {
                deliveryOrder.setDeliveryType(DeliveryType.NATIONAL);
            } else if (deliveryType == "ENVIALOYA" || deliveryType.equals("ENVIALOYA")) {
                deliveryOrder.setDeliveryType(DeliveryType.ENVIALOYA);
            } else if (deliveryType.equals("SCANANDGO")) {
                deliveryOrder.setDeliveryType(DeliveryType.SCANANDGO);
            }
        }
        final Key<DeliveryOrder> newDeliveryOrderKey = ofy().save().entity(deliveryOrder).now();
//        LOG.info("method: addItemsToDeliveryOrder INICIO Items to new CAR ->  " + itemsNewOrder.size());
        itemsNewOrder.parallelStream().forEach(deliveryOrderItem -> {
            deliveryOrderItem.setIdDeliveryOrderItem(UUID.randomUUID().toString());
            deliveryOrderItem.setIdDeliveryOrder(Ref.create(newDeliveryOrderKey));
            Key<DeliveryOrderItem> deliveryOrderItemKey = Key.create(newDeliveryOrderKey, DeliveryOrderItem.class, deliveryOrderItem.getIdDeliveryOrderItem());
            deliveryOrderItem.setIdDeliveryOrderItemWebSafe(deliveryOrderItemKey.toWebSafeString());
        });
        ofy().save().entities(itemsNewOrder).now();
//        LOG.info("method: addItemsToDeliveryOrder FIN");
        /*
        if (Objects.nonNull(deliveryOrderProviders) && !deliveryOrderProviders.isEmpty()) {
        }*/
    }

    /**
     * @author Dani rivera
     * @info metodo que se encarga de crear una orden  del item de suscripcion prime
     * @date 2022-05-16
     */

    public CreatedOrder createOrderPrime(
            @Named("idCustomerWebSafe") final String idCustomerWebSafe,
            @Named("token") final String token,
            @Named("tokenIdWebSafe") final String tokenIdWebSafe,
            DeliveryOrder order,
            HttpServletRequest httpServletRequest) throws BadRequestException, ConflictException, IOException, AlgoliaException, UnauthorizedException {

        if (!authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);

        boolean isScanAndGo = isOrdenScanAndGo(order);
        boolean isPrime = isOrderPrime(order);
        //boolean isPrimeMixed = isOrderPrimeMixed(order);


        order = deleteItemsNoPrime(idCustomerWebSafe, token, tokenIdWebSafe, order);

        if (!isScanAndGo && order.getIdAddress() == 0)
            throw new ConflictException("Direccion invalida");

        if (order.getPaymentType().getId() == PaymentTypeEnum.TRANSACCIONES_EN_LINEA.getId() && order.getPaymentCardId() <= 0)
            throw new ConflictException("PaymentCardId es obligatorio.");

        Key<Customer> customerKey = Key.create(idCustomerWebSafe);
        CreatedOrder orderJSON = new CreatedOrder();
        LOG.info(order.getCustomerPhone());
        DeliveryOrder deliveryOrderSavedShoppingCart = ofy()
                .load()
                .type(DeliveryOrder.class)
                .filter("currentStatus", 1)
                .ancestor(Ref.create(customerKey))
                .first()
                .now();
        if (deliveryOrderSavedShoppingCart != null) {
            LOG.info("deliveryOrderSaved ->  " + deliveryOrderSavedShoppingCart.getIdDeliveryOrder());
        }

        // validar si el cliente esta bloqueado
        BlockedUser blockedUser = ofy().load().type(BlockedUser.class).filter("idUser", order.getIdFarmatodo()).first().now();

        boolean userBlocked = blockedUser != null;

//        LOG.info("usuario bloqueado -> " + userBlocked);
        if (userBlocked)
            throw new ConflictException("Usuario bloqueado no puede continuar");

        //TODO Se obtiene el id de la tienda mas cercana segun la direccion
        //List<Address> listAddress = customers.getAddressesFromCustomer(order.getIdFarmatodo());
        int closerStoreId = 0;
        if (!isScanAndGo) {
            AddressesRes addressesRes = ApiGatewayService.get().getAddressesByCustomerId(order.getIdFarmatodo());
            List<Address> listAddress = addressesRes.getAddresses();

            if (Objects.nonNull(listAddress)) {
                DeliveryOrder finalOrder = order;
                DeliveryOrder finalOrder1 = order;
                closerStoreId = listAddress.stream().filter(address -> Objects.nonNull(address) && Objects.nonNull(finalOrder.getDeliveryType()) && Objects.nonNull(address.getDeliveryType()) &&
                                address.getCloserStoreId() > 0 &&
                                finalOrder.getDeliveryType().getDeliveryType().equals(address.getDeliveryType().getDeliveryType()) &&
                                finalOrder.getIdAddress() == address.getIdAddress())
                        .mapToInt(address -> address.getCloserStoreId()).findFirst().isPresent() ? listAddress.stream().filter(address -> address.getCloserStoreId() > 0 &&
                                finalOrder1.getDeliveryType().getDeliveryType().equals(address.getDeliveryType().getDeliveryType()) &&
                                finalOrder1.getIdAddress() == address.getIdAddress())
                        .mapToInt(address -> address.getCloserStoreId()).findFirst().getAsInt() : 0;
            }
        } else {
            closerStoreId = order.getIdStoreGroup() == null ? 26 : Integer.valueOf(order.getIdStoreGroup());
        }

        String storeIdToValidate = closerStoreId > 0 ? String.valueOf(closerStoreId) : order.getIdStoreGroup();
//        LOG.info("storeid: " + closerStoreId);

        final Long finalStoreToValidate = Long.parseLong(storeIdToValidate);
        boolean validateStores = false;
        //exclude stores
        ExcludeStoresCreateOrder excludeStoresCreateOrder = APIAlgolia.getStoresToExcludeCreateOrder();

        if (Objects.nonNull(excludeStoresCreateOrder.getEnableStores()) && !excludeStoresCreateOrder.getEnableStores().isEmpty()) {
//            LOG.info("stores to exclude -> " + excludeStoresCreateOrder.toString());
            for (Long storeExclude : excludeStoresCreateOrder.getEnableStores()) {
                if (storeExclude.equals(finalStoreToValidate)) {
                    validateStores = true;
                }
            }
        }

        // siempre estaran disponibles a menos que se encuentre en la lista
        Boolean isStoreAvailable = true;
//        LOG.info("go to validateStores if open or close -> " + validateStores);
        if (validateStores && !isScanAndGo) {
            isStoreAvailable = this.isStoreAvailable(storeIdToValidate, order.getPickingDate());
        }

        // Fix Validacion PickingDate
        if (Objects.nonNull(order.getPickingDate())) {
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            now = now.minus(5, ChronoUnit.HOURS);
            OffsetDateTime pickingDate = order.getPickingDate().toInstant().atOffset(ZoneOffset.UTC);
            if (pickingDate.isBefore(now))
                throw new ConflictException("La fecha de programación de la orden no es válida ");//+pickingDate+ " - "+now.toString());
        }

        if (!isStoreAvailable)
            throw new ConflictException(Constants.CLOSED_STORE);

        if (!Objects.isNull(order.getDeliveryType())) {
            if (order.getDeliveryType().getDeliveryType().equals(DeliveryType.NATIONAL.getDeliveryType())
                    && closerStoreId != URLConnections.NATIONAL_ID_STORE) {
                throw new ConflictException("Tienda no coincide con tipo de envio Nacional");
            }

            if (order.getDeliveryType().getDeliveryType().equals(DeliveryType.ENVIALOYA.getDeliveryType())
                    && closerStoreId != URLConnections.ENVIALOYA_ID_STORE) {
                throw new ConflictException("Tienda no coincide con tipo de envio Envialo ya");
            }
        }

        if (deliveryOrderSavedShoppingCart == null)
            throw new ConflictException(Constants.ERROR_CREATE_ORDER);
        List<DeliveryOrderItem> deliveryOrderItemList = order.getItemList();
        if (deliveryOrderItemList == null)
            throw new ConflictException(Constants.ERROR_CREATE_ORDER);

        // Fix items Duplicados
        validateDuplicateItems(deliveryOrderItemList);
        // Fix items Duplicados

        // NEW FIX

        //  obtener store and delivery type from address.!

        if (!isScanAndGo) {
            Key<User> userKey = Key.create(idCustomerWebSafe);
            User user = users.findUserByKey(userKey);
            if (user == null || user.getId() == 0)
                throw new UnauthorizedException(Constants.USER_NOT_FOUND);

            List<Address> allAddresses = this.customers.getAddressesByIdCustomer(user);

            Optional<Address> optionalAddress = Optional.empty();
            try {
                DeliveryOrder finalOrder2 = order;
                optionalAddress = allAddresses
                        .stream()
                        .filter(address -> address.getIdAddress().equals(finalOrder2.getIdAddress()))
                        .findFirst();
            } catch (Exception e) {
                LOG.warning("Error@createOrder filtering and finding address " + e.getMessage());
            }

            if (optionalAddress.isPresent()) {

                Address addressToCreateOrder = optionalAddress.get();
//                LOG.info("address -> " + addressToCreateOrder.toString());
                if (addressToCreateOrder.getDeliveryType() != null && addressToCreateOrder.getCloserStoreId() > 0) {
                    // SET NEW DELIVERY_TYPE AND STORE
                    order.setDeliveryType(addressToCreateOrder.getDeliveryType());
                    order.setIdStoreGroup(String.valueOf(addressToCreateOrder.getCloserStoreId()));

                } else {
                    throw new ConflictException(Constants.ERROR_ADDRESS_CREATE_ORDER);
                }
            } else {
                throw new ConflictException(Constants.ERROR_ADDRESS_CREATE_ORDER);
            }
        }
        // end fix

        // ScanAndGo Order
        List<DeliveryOrderItem> itemsScanAndGo = new ArrayList<>(deliveryOrderItemList);
        List<DeliveryOrderItem> itemsNewOrder = new ArrayList<>();
        if (isScanAndGo) {
            //set store princial when is scan and go
            itemsNewOrder = Objects.nonNull(itemsScanAndGo) ? itemsScanAndGo.stream().filter(it -> (Objects.isNull(it.getScanAndGo()) || !it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon())).collect(Collectors.toList()) : null;
            if (Objects.nonNull(itemsScanAndGo) && itemsScanAndGo.stream()
                    .filter(it -> (Objects.isNull(it.getScanAndGo()) || !it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon())).findFirst().isPresent()) {
                itemsScanAndGo.removeIf(it -> ((Objects.isNull(it.getScanAndGo()) || !it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon())));
            }
        } else {
            itemsNewOrder = Objects.nonNull(itemsScanAndGo) ? itemsScanAndGo.stream().filter(it -> (Objects.nonNull(it.getScanAndGo()) && it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon())).collect(Collectors.toList()) : null;
            if (Objects.nonNull(itemsScanAndGo) && itemsScanAndGo.stream()
                    .filter(it -> ((Objects.nonNull(it.getScanAndGo()) && it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon()))).findFirst().isPresent()) {
                itemsScanAndGo.removeIf(it -> ((Objects.nonNull(it.getScanAndGo()) && it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon())));
            }
        }

//        LOG.info("Items scan and go -> " + itemsScanAndGo.size());
        if (Objects.nonNull(itemsNewOrder)) {
            LOG.info("Items itemsNewOrder -> " + itemsNewOrder.size());
        }

        // fix provider and scan and go
        List<DeliveryOrderProvider> deliveryOrderProvidersList = new ArrayList<>();
        if (!isScanAndGo) {
            // Providers
            deliveryOrderProvidersList = ofy().load().type(DeliveryOrderProvider.class).ancestor(deliveryOrderSavedShoppingCart).list();
            //LOG.warning("deliveryOrderProvidersList ->  " + (Objects.nonNull(deliveryOrderProvidersList) ? deliveryOrderProvidersList.size() : 0));
        }
        // Fix elimina items que no corresponden al tipo de envio actual
        if (Objects.nonNull(itemsNewOrder) && !itemsNewOrder.isEmpty()) {
            ofy().delete().entities(itemsNewOrder);
        }
        deliveryOrderItemList = itemsScanAndGo;

        OrderUtil.deleteTipPriceZero(deliveryOrderItemList);

        // TODO: Validacion para evitar cobrar productos que no esten visibles al usuario (APPS - Provedores externos)
        if (Objects.nonNull(order.getItemList()) && !order.getItemList().isEmpty()) {
//            valida Items de dataStore y los items parametro
            Map<Long, Integer> mapValidationItem = order.getItemList().stream().collect(ImmutableMap.toImmutableMap(DeliveryOrderItem::getId, DeliveryOrderItem::getQuantitySold));
//            LOG.info("deliveryOrderItemList 2 ->  " + deliveryOrderItemList.size());
            if (Objects.nonNull(deliveryOrderProvidersList) && !deliveryOrderProvidersList.isEmpty()) {
//                LOG.info("method createOrder ->  Validando Items");
                deliveryOrderProvidersList.forEach(provider ->
                        provider.getItemList().removeIf(item -> !mapValidationItem.containsKey(item.getId())));
                // Validación para evitar que los items se dupliquen
                Map<Long, Integer> mapProviderItem = new HashMap<>();
                deliveryOrderProvidersList.stream()
                        .filter(provider -> (Objects.nonNull(provider.getItemList()) && !provider.getItemList().isEmpty()))
                        .forEach(provider -> provider.getItemList().parallelStream().forEach(itemProvider -> {
                                    mapProviderItem.put(itemProvider.getId(), itemProvider.getQuantitySold());
                                })
                        );
                // Elimina item de proveedores del listado normal de items
                deliveryOrderItemList.removeIf(item -> mapProviderItem.containsKey(item.getId()));
            }
        }
//        LOG.info("deliveryOrderItemList 3 ->  " + deliveryOrderItemList.size());
//        LOG.info("createOrder: has picking date ---> " + order.getPickingDate());
        if (!Objects.isNull(order.getPickingDate()) && verifyHighDemand(order, closerStoreId)) {
//            createOrder: isHigh Demand -> TRUE
            setCufPerHighDemand(deliveryOrderItemList, closerStoreId);
        }


        /**
         * Eliminar los item de propina para pasar la orden
         * Tener cuidado ya que sin esto no se crean suscripciones PRIME.
         */
        Optional<TipConfig> tipConfigOptional = GrowthBookConfigLoader.getTipsConfig(Constants.CUSTOMER_ANONYMOUS);

        if (tipConfigOptional.isPresent()) {
//            Eliminar propinas para la orden
            deleteTipsForPrimeOrder(deliveryOrderItemList, tipConfigOptional.get());
        }

        /** Hay que eliminar los items que no sean prime de la lista que se carga de datastore **/
        deliveryOrderItemList = deleteDeliveryOrderItemsNoPrime(idCustomerWebSafe, token, tokenIdWebSafe, deliveryOrderItemList);

        String orderRequest = orders.createOrderJson(order, deliveryOrderItemList, deliveryOrderProvidersList).toString();
        LOG.info("createOrderJson ->  " + (orderRequest != null ? orderRequest : " es null"));

        // Retrofit method:

        CreateOrderRequestCore requestCore;
        Gson gson = new Gson();
        requestCore = gson.fromJson(orderRequest, CreateOrderRequestCore.class);


        if (requestCore.getCustomerId() != null && requestCore.getCustomerId() == 0) {
            // call service oms get customer id.
//            LOG.info("createOrder customer id before ----> " + requestCore.getCustomerId());
            CustomerAddressResponse customerAddressResponse = ApiGatewayService.get().getCustomerByAddressId(requestCore.getCustomerAddressId());
            if (customerAddressResponse != null && customerAddressResponse.getData() != null) {
                long customerIdAux = (long) customerAddressResponse.getData().getIdCustomer();
                requestCore.setCustomerId(customerIdAux);
            }
        }


        if (requestCore.getCustomerId() <= 0) {
            throw new ConflictException(Constants.ERROR_CREATE_ORDER);
        }

        try {
            final int storeId = closerStoreId;
            requestCore.getItems().forEach(item -> {
                final ItemAlgolia itemAlgolia = APIAlgolia.getItemAlgolia(item.getItemId() + "" + storeId);
                if (itemAlgolia != null) {
                    final int totalStock = itemAlgolia.getTotalStock();
//                    LOG.info("Validando cantidad solicitada > total stock");
                    if (totalStock > 0 && item.getQuantityRequested() > totalStock) {
                        LOG.info("QuantityRequested ANTES:" + item.getQuantityRequested());
                        item.setQuantityRequested(totalStock);
                        LOG.info("QuantityRequested DESPUES:" + item.getQuantityRequested());
                    }
                }
            });
        } catch (Exception e) {
            LOG.warning("Error al intentar actualizar la canrtidad solictada al maximo stock de la tienda.");
        }

        if (Objects.nonNull(order.getCustomerIdCallCenter())) {
            LOG.info("Insertando el customer del call");
            requestCore.setCustomerIdCallCenter(order.getCustomerIdCallCenter());
            requestCore.setSource(Constants.SOURCE_CALL_CENTER);
        }

        if (PaymentTypeEnum.PSE.getId() == order.getPaymentType().getId()) {
            requestCore.setFinancialInstitutions(order.getFinancialInstitutions());
            requestCore.setTypePersonPSE(order.getTypePersonPSE());
            requestCore.setIpAddress(order.getIpAddress());
            requestCore.setIdentification(order.getIdentification());
        }

        if (order.getSelfCheckout() != null && order.getSelfCheckout().getIsSelfCheckout() && order.getSelfCheckout().getIdBox() != null) {
            requestCore.setSelfCheckout(order.getSelfCheckout());
        }

        //Dejar el deliveryType como scanandgo para orden solo prime
        requestCore.setDeliveryType(DeliveryType.SCANANDGO.getDeliveryType());

//        create order in BACKEND3
        if (PaymentTypeEnum.PSE.getId() == order.getPaymentType().getId()) {
            requestCore.setPrimeMixedPSE(true);
        }

        LOG.info("create order in BACKEND3");
        if (createOrderViaBackend3(requestCore, order, orderJSON, TraceUtil.getXCloudTraceId(httpServletRequest), idCustomerWebSafe)) {
            LOG.info("response backend3 create order --> " + orderJSON.toString());

            if (orderJSON.getId() == 0 && PaymentTypeEnum.TRANSACCIONES_EN_LINEA.getId() == order.getPaymentType().getId()) {
                throw new ConflictException(Constants.ERROR_PAYMENT_ONLINE);
            }

            if (orderJSON.getId() <= 0) {
                throw new ConflictException(Constants.ERROR_CREATE_ORDER);
            }

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
            orderJSON.setItems(itemOrders);

            List<ProviderOrder> providers = new ArrayList<>();
            ProviderOrder provider = new ProviderOrder();
            provider.setName("");
            provider.setEmail("");
            provider.setDeliveryPrice(0);
            provider.setItems(itemOrders);
            providers.add(provider);
            orderJSON.setProviders(providers);

            LOG.info(String.valueOf(orderJSON.getId()));
            DeliveryOrder deliveryOrder = ofy().load().type(DeliveryOrder.class).filter("currentStatus", 1).ancestor(Ref.create(customerKey)).first().now();
            if (deliveryOrder == null)
                throw new ConflictException(Constants.DELIVERY_ORDER_NOT_FOUND);
            deliveryOrder.setIdOrder(orderJSON.getId());
            deliveryOrder.setAddress(orderJSON.getAddress());
            deliveryOrder.setIdAddress(order.getIdAddress());
            deliveryOrder.setAddressDetails(order.getAddressDetails());
            deliveryOrder.setPaymentType(order.getPaymentType());
            if (order.getCustomerIdCallCenter() != null) {
                deliveryOrder.setSource(Constants.SOURCE_CALL_CENTER);
            } else {
                deliveryOrder.setSource(order.getSource());
            }


            deliveryOrder.setCurrentStatus(0);

            deliveryOrder.setIdFarmatodo(order.getIdFarmatodo());
            deliveryOrder.setLastStatus(ORDER_DELIVERED);
            deliveryOrder.setActive(true);

            deliveryOrder.setDeliveryType(DeliveryType.SCANANDGO);

            if (Objects.nonNull(orderJSON.getQrCode())) {
                deliveryOrder.setQrCode(orderJSON.getQrCode());
            }
            //Consultar el resumen de la orden creada, para obtener valor de domicilio
            try {
//                LOG.info("method getOrderSumary: -> orderID:" + orderJSON.getId());
                GetOrderSumary orderSumary = ApiGatewayService.get().getOrderSumary(orderJSON.getId());
                if (orderSumary != null && orderSumary.getData() != null) {
//                    LOG.info("orderJSON.getDeliveryPrice() -> " + orderSumary.getData().getDeliveryValue());
                    deliveryOrder.setDeliveryPrice(orderSumary.getData().getDeliveryValue());
                }
            } catch (Exception e) {
                e.printStackTrace();
                LOG.severe("Error al consulatar resumen de orden! " + e.getMessage());
            }

            // if ScanAndGO
            if (isOrdenScanAndGo(order)) {
                if (Objects.nonNull(order.getPaymentType()) && PaymentTypeEnum.DATAFONOS.getId().equals(order.getPaymentType().getId())) {
                    deliveryOrder.setLastStatus(ORDER_CREATED);

                } else {
                    deliveryOrder.setLastStatus(ORDER_DELIVERED);
                    deliveryOrder.setActive(false);
                }
            }

            if (isPrime && deliveryOrder.getDeliveryType().equals(DeliveryType.SCANANDGO)) {
                deliveryOrder.setLastStatus(ORDER_DELIVERED);
                deliveryOrder.setActive(false);
            }

            Timestamp stamp = new Timestamp(orderJSON.getCreateDate());
            Date date = new Date(stamp.getTime());
            deliveryOrder.setCreateDate(date);
            //if (orderJSON.getUpdateShopping()) {
            //    String getOrderURL = URLConnections.URL_ORDER + "/" + orderJSON.getId();
            //    DeliveryOrder processedOrder = CoreConnection.getRequest(getOrderURL, DeliveryOrder.class);
            //    deliveryOrder.updateShopping(processedOrder);
            //}
            if (order.getPickingDate() != null) {
                deliveryOrder.setPickingDate(order.getPickingDate());
                //LOG.warning("Picking Date to DS -> " + deliveryOrder.toString());
            }

            //Fix para borrar proveedor si no tiene items
            List<DeliveryOrderProvider> deleteProviders = new ArrayList<>();
//            LOG.info("tamaño proveedores : " + deliveryOrderProvidersList.size() + " list " + deliveryOrderProvidersList);
            for (DeliveryOrderProvider oderProvider : deliveryOrderProvidersList) {
//                LOG.info("key " + oderProvider.getIdDeliveryOrder() + " list " + oderProvider.getItemList());
                if (oderProvider.getItemList() == null || oderProvider.getItemList().isEmpty()) {
                    deleteProviders.add(oderProvider);
                }
            }

            ofy().delete().entities(deleteProviders);
            Key<DeliveryOrder> deliveryOrderKey = ofy().save().entity(deliveryOrder).now();

            if (Objects.nonNull(itemsNewOrder) && !itemsNewOrder.isEmpty()) {
                try {
                    // Crea una orden con lo items sobrantes
//                    LOG.info("Crea una nueva orden para los items ->  " + itemsNewOrder.size());
                    createDeliveryOrder(customerKey, deliveryOrder.getDeliveryType().getDeliveryType(), itemsNewOrder, deliveryOrderProvidersList);
                } catch (Exception ex) {
                    LOG.warning("No fue posible crear el nuevo Carrito con los items no utilizados " + ex.getMessage());
                }
            }

            // Save tracing state
            if (Objects.nonNull(orderJSON.getTracing()) && !orderJSON.getTracing().isEmpty()) {
                CreatedOrder finalOrderJSON = orderJSON;
                DeliveryOrder finalOrder3 = order;
                orderJSON.getTracing().forEach(tracing -> {
                    tracing.setIdTracing(UUID.randomUUID().toString());
                    tracing.setDeliveryOrderId(Ref.create(deliveryOrderKey));
                    if (tracing.getStatus() == 12) {
                        int responseSms = 0;
                        try {
                        /*responseSms = supportMethods.sendSms(order.getCustomerPhone(),
                                "Bienvenido a farmatodo. La transaccion asociada a tu orden "
                                        + finalOrderJSON.getId() + " fue aprobada");
*/
                            final SendSMSCloudFunctionReq request = new SendSMSCloudFunctionReq(finalOrder3.getCustomerPhone(),
                                    "Bienvenido a farmatodo. La transaccion asociada a tu orden "
                                            + finalOrderJSON.getId() + " fue aprobada");
                            CloudFunctionsService.get().postSendSms(request);
                        } catch (IOException e) {
                            LOG.warning("Error");
                        }
                        //LOG.warning("Response sms " + responseSms);
                    }
                });
                Tracing tracing = orderJSON.getTracing().get(0);
                ofy().save().entity(tracing);
            }

            addMarcaCategorySubcategorieAndItemUrl(orderJSON);

            if (requestCore.getSelfCheckout() != null && requestCore.getSelfCheckout().getIsSelfCheckout() && requestCore.getSelfCheckout().getIdBox() != null) {
                SelfCheckoutAlgolia selfCheckoutAlgolia = APIAlgolia.getSelfCheckout();
                if (!Objects.isNull(selfCheckoutAlgolia) && !Objects.isNull(selfCheckoutAlgolia.getCustomerByStoresAndBox())) {
                    Optional<SelfCheckoutListAlgolia> selfCheckoutListAlgolia = selfCheckoutAlgolia.getCustomerByStoresAndBox().stream()
                            .filter(checkoutListAlgolia -> Objects.equals(checkoutListAlgolia.getStore(), requestCore.getStoreId()) && Objects.equals(checkoutListAlgolia.getBox(), requestCore.getSelfCheckout().getIdBox()))
                            .limit(1).findFirst();
                    if (selfCheckoutListAlgolia.isPresent()) {
                        deleteCoupon(customerKey);
                    }
                }
            }
            DeliveryOrder deliveryOrderData = getOrderMethod(deliveryOrder, orderJSON.getId(), false, false);

            orderJSON.setOrderData(deliveryOrderData);
            ApiGatewayService.get().updateStratumBraze(requestCore.getCustomerId().toString());


        } else {
            DeliveryOrder deliveryOrderOrder = ofy().load().type(DeliveryOrder.class).filter("currentStatus", 0).ancestor(Ref.create(customerKey)).first().now();
            deleteCoupon(customerKey);
            if (deliveryOrderOrder == null) {
                throw new ConflictException(Constants.ERROR_CREATE_ORDER);
            } else {
                ofy().delete().entity(deliveryOrderOrder).now();
                LOG.info("SE elimina la orden del DataStore ya que no se pudo crear en OMS");
                throw new ConflictException(Constants.ERROR_CREATE_ORDER);
            }

        }
        try {
            Long sessionId = Long.parseLong(httpServletRequest.getHeader("amplitudeSessionId"));
            if (sessionId != null) {
                AmplitudeSessionRequest request = new AmplitudeSessionRequest();
                request.setOrderId(orderJSON.getId());
                request.setSessionId(sessionId);
//                LOG.info("request -> " + request.toString());
                ApiGatewayService.get().saveAmplitudeSessionId(request);
            }
        } catch (Exception e) {
            LOG.warning(e.getMessage());
        }

        orderJSON.setPrime(true);
        orderJSON.setPrimeGeneral(true);


        addRMSclasses(orderJSON);
        sendEventCreate(orderJSON, requestCore.getCustomerId(), order.getPaymentType().getId());
        try {
            LOG.info("CreateOrderResponse:#" + requestCore.getCustomerId() + " Gson -> " + new Gson().toJson(orderJSON));
        } catch (Exception e) {
            LOG.info("No se pudo serializar Json de respuesta de creación de orden.");
        }
        return orderJSON;
    }


    public CreateOrderResponse<CreatedOrder> createOrderPrimeV2(
            @Named("idCustomerWebSafe") final String idCustomerWebSafe,
            @Named("token") final String token,
            @Named("tokenIdWebSafe") final String tokenIdWebSafe,
            DeliveryOrder order,
            HttpServletRequest httpServletRequest) throws BadRequestException, ConflictException, IOException, AlgoliaException, UnauthorizedException, InternalServerErrorException {

        try {
            AlgoliaMessageConfigCreateOrder algoliaMessageConfigCreateOrder = APIAlgolia.getMessagesCreateOrder();
            if (Objects.isNull(algoliaMessageConfigCreateOrder)) {
                LOG.severe("method: orderEndpoint/v2/createOrder -> fallo algolia en traer la configuracion de mensajes del create order.");
            }
            boolean isActiveMessagesAlgolia = Objects.nonNull(algoliaMessageConfigCreateOrder.getActive()) ? algoliaMessageConfigCreateOrder.getActive() : false;

            if (!authenticate.isValidToken(token, tokenIdWebSafe))
                return new CreateOrderResponse<>(HttpStatusCode.BAD_REQUEST.getCode(), HttpStatusCode.BAD_REQUEST.getStatusName(), Objects.nonNull(algoliaMessageConfigCreateOrder.getInvalidToken()) && isActiveMessagesAlgolia ? algoliaMessageConfigCreateOrder.getInvalidToken() : Constants.INVALID_TOKEN, null);

            boolean isScanAndGo = isOrdenScanAndGo(order);
            boolean isPrime = isOrderPrime(order);
            boolean isPrimeMixed = isOrderPrimeMixed(order);


            order = deleteItemsNoPrime(idCustomerWebSafe, token, tokenIdWebSafe, order);

            if (!isScanAndGo && order.getIdAddress() == 0)
                return new CreateOrderResponse<>(HttpStatusCode.BAD_REQUEST.getCode(), HttpStatusCode.BAD_REQUEST.getStatusName(), Objects.nonNull(algoliaMessageConfigCreateOrder.getInvalidAddress()) && isActiveMessagesAlgolia ? algoliaMessageConfigCreateOrder.getInvalidAddress() : Constants.INVALID_ADDRESS, null);

            if (order.getPaymentType().getId() == PaymentTypeEnum.TRANSACCIONES_EN_LINEA.getId() && order.getPaymentCardId() <= 0)
                return new CreateOrderResponse<>(HttpStatusCode.BAD_REQUEST.getCode(), HttpStatusCode.BAD_REQUEST.getStatusName(), Objects.nonNull(algoliaMessageConfigCreateOrder.getMandatoryPaymentCardId()) && isActiveMessagesAlgolia ? algoliaMessageConfigCreateOrder.getMandatoryPaymentCardId() : Constants.MANDATORY_PAYMENTCARD_ID, null);

            Key<Customer> customerKey = Key.create(idCustomerWebSafe);
            CreatedOrder orderJSON = new CreatedOrder();
            LOG.info(order.getCustomerPhone());
            DeliveryOrder deliveryOrderSavedShoppingCart = ofy()
                    .load()
                    .type(DeliveryOrder.class)
                    .filter("currentStatus", 1)
                    .ancestor(Ref.create(customerKey))
                    .first()
                    .now();
            if (deliveryOrderSavedShoppingCart != null) {
                LOG.info("deliveryOrderSaved ->  " + deliveryOrderSavedShoppingCart.getIdDeliveryOrder());
            }

            // validar si el cliente esta bloqueado
            BlockedUser blockedUser = ofy().load().type(BlockedUser.class).filter("idUser", order.getIdFarmatodo()).first().now();

            boolean userBlocked = blockedUser != null;

//        LOG.info("usuario bloqueado -> " + userBlocked);
            if (userBlocked)
                return new CreateOrderResponse<>(HttpStatusCode.FORBIDDEN.getCode(), HttpStatusCode.FORBIDDEN.getStatusName(), Objects.nonNull(algoliaMessageConfigCreateOrder.getUserBlocked()) && isActiveMessagesAlgolia ? algoliaMessageConfigCreateOrder.getUserBlocked() : Constants.USER_BLOCKED, null);

            //TODO Se obtiene el id de la tienda mas cercana segun la direccion
            //List<Address> listAddress = customers.getAddressesFromCustomer(order.getIdFarmatodo());
            int closerStoreId = 0;
            if (!isScanAndGo) {
                AddressesRes addressesRes = ApiGatewayService.get().getAddressesByCustomerId(order.getIdFarmatodo());
                List<Address> listAddress = addressesRes.getAddresses();

                if (Objects.nonNull(listAddress)) {
                    DeliveryOrder finalOrder = order;
                    DeliveryOrder finalOrder1 = order;
                    closerStoreId = listAddress.stream().filter(address -> Objects.nonNull(address) && Objects.nonNull(finalOrder.getDeliveryType()) && Objects.nonNull(address.getDeliveryType()) &&
                                    address.getCloserStoreId() > 0 &&
                                    finalOrder.getDeliveryType().getDeliveryType().equals(address.getDeliveryType().getDeliveryType()) &&
                                    finalOrder.getIdAddress() == address.getIdAddress())
                            .mapToInt(address -> address.getCloserStoreId()).findFirst().isPresent() ? listAddress.stream().filter(address -> address.getCloserStoreId() > 0 &&
                                    finalOrder1.getDeliveryType().getDeliveryType().equals(address.getDeliveryType().getDeliveryType()) &&
                                    finalOrder1.getIdAddress() == address.getIdAddress())
                            .mapToInt(address -> address.getCloserStoreId()).findFirst().getAsInt() : 0;
                }
            } else {
                closerStoreId = order.getIdStoreGroup() == null ? 26 : Integer.valueOf(order.getIdStoreGroup());
            }

            String storeIdToValidate = closerStoreId > 0 ? String.valueOf(closerStoreId) : order.getIdStoreGroup();
//        LOG.info("storeid: " + closerStoreId);

            final Long finalStoreToValidate = Long.parseLong(storeIdToValidate);
            boolean validateStores = false;
            //exclude stores
            ExcludeStoresCreateOrder excludeStoresCreateOrder = APIAlgolia.getStoresToExcludeCreateOrder();

            if (Objects.nonNull(excludeStoresCreateOrder.getEnableStores()) && !excludeStoresCreateOrder.getEnableStores().isEmpty()) {
//            LOG.info("stores to exclude -> " + excludeStoresCreateOrder.toString());
                for (Long storeExclude : excludeStoresCreateOrder.getEnableStores()) {
                    if (storeExclude.equals(finalStoreToValidate)) {
                        validateStores = true;
                    }
                }
            }

            // siempre estaran disponibles a menos que se encuentre en la lista
            Boolean isStoreAvailable = true;
//        LOG.info("go to validateStores if open or close -> " + validateStores);
            if (validateStores && !isScanAndGo) {
                isStoreAvailable = this.isStoreAvailable(storeIdToValidate, order.getPickingDate());
            }

            // Fix Validacion PickingDate
            if (Objects.nonNull(order.getPickingDate())) {
                OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
                now = now.minus(5, ChronoUnit.HOURS);
                OffsetDateTime pickingDate = order.getPickingDate().toInstant().atOffset(ZoneOffset.UTC);
                if (pickingDate.isBefore(now))
                    return new CreateOrderResponse<>(HttpStatusCode.BAD_REQUEST.getCode(), HttpStatusCode.BAD_REQUEST.getStatusName(), Objects.nonNull(algoliaMessageConfigCreateOrder.getInvalidOrderScheduled()) && isActiveMessagesAlgolia ? algoliaMessageConfigCreateOrder.getInvalidOrderScheduled() : Constants.INVALID_PROGRAMMING, null); //+pickingDate+ " - "+now.toString());
            }

            if (!isStoreAvailable)
                return new CreateOrderResponse<>(HttpStatusCode.OK.getCode(), HttpStatusCode.OK.getStatusName(), Objects.nonNull(algoliaMessageConfigCreateOrder.getClosedStore()) && isActiveMessagesAlgolia ? algoliaMessageConfigCreateOrder.getClosedStore() : Constants.CLOSED_STORE, null);

            if (!Objects.isNull(order.getDeliveryType())) {
                if (order.getDeliveryType().getDeliveryType().equals(DeliveryType.NATIONAL.getDeliveryType())
                        && closerStoreId != URLConnections.NATIONAL_ID_STORE) {
                    return new CreateOrderResponse<>(HttpStatusCode.BAD_REQUEST.getCode(), HttpStatusCode.BAD_REQUEST.getStatusName(), Objects.nonNull(algoliaMessageConfigCreateOrder.getInvalidNationalStore()) && isActiveMessagesAlgolia ? algoliaMessageConfigCreateOrder.getInvalidNationalStore() : Constants.INVALID_NATIONAL_STORE, null);
                }

                if (order.getDeliveryType().getDeliveryType().equals(DeliveryType.ENVIALOYA.getDeliveryType())
                        && closerStoreId != URLConnections.ENVIALOYA_ID_STORE) {
                    return new CreateOrderResponse<>(HttpStatusCode.BAD_REQUEST.getCode(), HttpStatusCode.BAD_REQUEST.getStatusName(), Objects.nonNull(algoliaMessageConfigCreateOrder.getInvalidEnvialoyaStore()) && isActiveMessagesAlgolia ? algoliaMessageConfigCreateOrder.getInvalidEnvialoyaStore() : Constants.INVALID_ENVIALOYA_STORE, null);
                }
            }

            if (deliveryOrderSavedShoppingCart == null)
                return new CreateOrderResponse<>(HttpStatusCode.BAD_REQUEST.getCode(), HttpStatusCode.BAD_REQUEST.getStatusName(), Objects.nonNull(algoliaMessageConfigCreateOrder.getEmptyShoppingCart()) && isActiveMessagesAlgolia ? algoliaMessageConfigCreateOrder.getEmptyShoppingCart() : Constants.EMPTY_SHOPPING_CART, null);
            List<DeliveryOrderItem> deliveryOrderItemList = ofy().load().type(DeliveryOrderItem.class).ancestor(deliveryOrderSavedShoppingCart).list();
            if (deliveryOrderItemList == null)
                return new CreateOrderResponse<>(HttpStatusCode.BAD_REQUEST.getCode(), HttpStatusCode.BAD_REQUEST.getStatusName(), Objects.nonNull(algoliaMessageConfigCreateOrder.getEmptyShoppingCart()) && isActiveMessagesAlgolia ? algoliaMessageConfigCreateOrder.getEmptyShoppingCart() : Constants.EMPTY_SHOPPING_CART, null);

            // Fix items Duplicados
            validateDuplicateItems(deliveryOrderItemList);
            // Fix items Duplicados

//        LOG.info("deliveryOrderItemList ->  " + deliveryOrderItemList.size());

            // NEW FIX

            //  obtener store and delivery type from address.!


            if (!isScanAndGo) {
                Key<User> userKey = Key.create(idCustomerWebSafe);
                User user = users.findUserByKey(userKey);
                if (user == null || user.getId() == 0)
                    return new CreateOrderResponse<>(HttpStatusCode.UNAUTHORIZED.getCode(), HttpStatusCode.UNAUTHORIZED.getStatusName(), Objects.nonNull(algoliaMessageConfigCreateOrder.getUserNotFound()) && isActiveMessagesAlgolia ? algoliaMessageConfigCreateOrder.getUserNotFound() : Constants.USER_NOT_FOUND, null);

                List<Address> allAddresses = this.customers.getAddressesByIdCustomer(user);

                Optional<Address> optionalAddress = Optional.empty();
                try {
                    DeliveryOrder finalOrder2 = order;
                    optionalAddress = allAddresses
                            .stream()
                            .filter(address -> address.getIdAddress().equals(finalOrder2.getIdAddress()))
                            .findFirst();
                } catch (Exception e) {
                    LOG.warning("Error@createOrder filtering and finding address " + e.getMessage());
                }

                if (optionalAddress.isPresent()) {

                    Address addressToCreateOrder = optionalAddress.get();
//                LOG.info("address -> " + addressToCreateOrder.toString());
                    if (addressToCreateOrder.getDeliveryType() != null && addressToCreateOrder.getCloserStoreId() > 0) {
                        // SET NEW DELIVERY_TYPE AND STORE
                        order.setDeliveryType(addressToCreateOrder.getDeliveryType());
                        order.setIdStoreGroup(String.valueOf(addressToCreateOrder.getCloserStoreId()));

                    } else {
                        return new CreateOrderResponse<>(HttpStatusCode.CONFLICT.getCode(), HttpStatusCode.CONFLICT.getStatusName(), Objects.nonNull(algoliaMessageConfigCreateOrder.getErrorAddressInvalid()) && isActiveMessagesAlgolia ? algoliaMessageConfigCreateOrder.getErrorAddressInvalid() : Constants.ERROR_ADDRESS_CREATE_ORDER, null);
                    }
                } else {
                    return new CreateOrderResponse<>(HttpStatusCode.CONFLICT.getCode(), HttpStatusCode.CONFLICT.getStatusName(), Objects.nonNull(algoliaMessageConfigCreateOrder.getErrorAddressInvalid()) && isActiveMessagesAlgolia ? algoliaMessageConfigCreateOrder.getErrorAddressInvalid() : Constants.ERROR_ADDRESS_CREATE_ORDER, null);
                }

//            LOG.info("fix new deliveryType -> " + order.getDeliveryType() + ", new store -> " + order.getIdStoreGroup());
            }

            // end fix


            // ScanAndGo Order
            List<DeliveryOrderItem> itemsScanAndGo = new ArrayList<>(deliveryOrderItemList);
            List<DeliveryOrderItem> itemsNewOrder = new ArrayList<>();
            if (isScanAndGo) {
                //set store princial when is scan and go
                itemsNewOrder = Objects.nonNull(itemsScanAndGo) ? itemsScanAndGo.stream().filter(it -> (Objects.isNull(it.getScanAndGo()) || !it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon())).collect(Collectors.toList()) : null;
                if (Objects.nonNull(itemsScanAndGo) && itemsScanAndGo.stream()
                        .filter(it -> (Objects.isNull(it.getScanAndGo()) || !it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon())).findFirst().isPresent()) {
                    itemsScanAndGo.removeIf(it -> ((Objects.isNull(it.getScanAndGo()) || !it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon())));
                }
            } else {
                itemsNewOrder = Objects.nonNull(itemsScanAndGo) ? itemsScanAndGo.stream().filter(it -> (Objects.nonNull(it.getScanAndGo()) && it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon())).collect(Collectors.toList()) : null;
                if (Objects.nonNull(itemsScanAndGo) && itemsScanAndGo.stream()
                        .filter(it -> ((Objects.nonNull(it.getScanAndGo()) && it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon()))).findFirst().isPresent()) {
                    itemsScanAndGo.removeIf(it -> ((Objects.nonNull(it.getScanAndGo()) && it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon())));
                }
            }

//        LOG.info("Items scan and go -> " + itemsScanAndGo.size());
            if (Objects.nonNull(itemsNewOrder)) {
                LOG.info("Items itemsNewOrder -> " + itemsNewOrder.size());
            }

            // fix provider and scan and go
            List<DeliveryOrderProvider> deliveryOrderProvidersList = new ArrayList<>();
            if (!isScanAndGo) {
                // Providers
                deliveryOrderProvidersList = ofy().load().type(DeliveryOrderProvider.class).ancestor(deliveryOrderSavedShoppingCart).list();
                //LOG.warning("deliveryOrderProvidersList ->  " + (Objects.nonNull(deliveryOrderProvidersList) ? deliveryOrderProvidersList.size() : 0));
            }
            // Fix elimina items que no corresponden al tipo de envio actual
            if (Objects.nonNull(itemsNewOrder) && !itemsNewOrder.isEmpty()) {
                ofy().delete().entities(itemsNewOrder);
            }
            deliveryOrderItemList = itemsScanAndGo;

            OrderUtil.deleteTipPriceZero(deliveryOrderItemList);

            // TODO: Validacion para evitar cobrar productos que no esten visibles al usuario (APPS - Provedores externos)
            if (Objects.nonNull(order.getItemList()) && !order.getItemList().isEmpty()) {
//            valida Items de dataStore y los items parametro
                Map<Long, Integer> mapValidationItem = order.getItemList().stream().collect(ImmutableMap.toImmutableMap(DeliveryOrderItem::getId, DeliveryOrderItem::getQuantitySold));
//            LOG.info("deliveryOrderItemList 2 ->  " + deliveryOrderItemList.size());
                if (Objects.nonNull(deliveryOrderProvidersList) && !deliveryOrderProvidersList.isEmpty()) {
//                LOG.info("method createOrder ->  Validando Items");
                    deliveryOrderProvidersList.forEach(provider ->
                            provider.getItemList().removeIf(item -> !mapValidationItem.containsKey(item.getId())));
                    // Validación para evitar que los items se dupliquen
                    Map<Long, Integer> mapProviderItem = new HashMap<>();
                    deliveryOrderProvidersList.stream()
                            .filter(provider -> (Objects.nonNull(provider.getItemList()) && !provider.getItemList().isEmpty()))
                            .forEach(provider -> provider.getItemList().parallelStream().forEach(itemProvider -> {
                                        mapProviderItem.put(itemProvider.getId(), itemProvider.getQuantitySold());
                                    })
                            );
                    // Elimina item de proveedores del listado normal de items
                    deliveryOrderItemList.removeIf(item -> mapProviderItem.containsKey(item.getId()));
                }
            }
//        LOG.info("deliveryOrderItemList 3 ->  " + deliveryOrderItemList.size());
//        LOG.info("createOrder: has picking date ---> " + order.getPickingDate());
            if (!Objects.isNull(order.getPickingDate()) && verifyHighDemand(order, closerStoreId)) {
//            createOrder: isHigh Demand -> TRUE
                setCufPerHighDemand(deliveryOrderItemList, closerStoreId);
            }


            /**
             * Eliminar los item de propina para pasar la orden
             * Tener cuidado ya que sin esto no se crean suscripciones PRIME.
             */
            Optional<TipConfig> tipConfigOptional = GrowthBookConfigLoader.getTipsConfig(Constants.CUSTOMER_ANONYMOUS);

            if (tipConfigOptional.isPresent()) {
//            Eliminar propinas para la orden
                deleteTipsForPrimeOrder(deliveryOrderItemList, tipConfigOptional.get());
            }

            /** Hay que eliminar los items que no sean prime de la lista que se carga de datastore **/
            deliveryOrderItemList = deleteDeliveryOrderItemsNoPrime(idCustomerWebSafe, token, tokenIdWebSafe, deliveryOrderItemList);

            String orderRequest = orders.createOrderJson(order, deliveryOrderItemList, deliveryOrderProvidersList).toString();
            LOG.info("createOrderJson ->  " + (orderRequest != null ? orderRequest : " es null"));

            // Retrofit method:

            CreateOrderRequestCore requestCore;
            Gson gson = new Gson();
            requestCore = gson.fromJson(orderRequest, CreateOrderRequestCore.class);


            if (requestCore.getCustomerId() != null && requestCore.getCustomerId() == 0) {
                // call service oms get customer id.
//            LOG.info("createOrder customer id before ----> " + requestCore.getCustomerId());
                CustomerAddressResponse customerAddressResponse = ApiGatewayService.get().getCustomerByAddressId(requestCore.getCustomerAddressId());
                if (customerAddressResponse != null && customerAddressResponse.getData() != null) {
                    long customerIdAux = (long) customerAddressResponse.getData().getIdCustomer();
                    requestCore.setCustomerId(customerIdAux);
                }
            }


            if (requestCore.getCustomerId() <= 0) {
                return new CreateOrderResponse<>(HttpStatusCode.NOT_FOUND.getCode(), HttpStatusCode.NOT_FOUND.getStatusName(), Objects.nonNull(algoliaMessageConfigCreateOrder.getUserNotFound()) && isActiveMessagesAlgolia ? algoliaMessageConfigCreateOrder.getUserNotFound() : Constants.USER_NOT_FOUND, null);
            }

            try {
                final int storeId = closerStoreId;
                requestCore.getItems().forEach(item -> {
                    final ItemAlgolia itemAlgolia = APIAlgolia.getItemAlgolia(item.getItemId() + "" + storeId);
                    if (itemAlgolia != null) {
                        final int totalStock = itemAlgolia.getTotalStock();
//                    LOG.info("Validando cantidad solicitada > total stock");
                        if (totalStock > 0 && item.getQuantityRequested() > totalStock) {
                            LOG.info("QuantityRequested ANTES:" + item.getQuantityRequested());
                            item.setQuantityRequested(totalStock);
                            LOG.info("QuantityRequested DESPUES:" + item.getQuantityRequested());
                        }
                    }
                });
            } catch (Exception e) {
                LOG.warning("Error al intentar actualizar la canrtidad solictada al maximo stock de la tienda.");
            }

            if (Objects.nonNull(order.getCustomerIdCallCenter())) {
                LOG.info("Insertando el customer del call");
                requestCore.setCustomerIdCallCenter(order.getCustomerIdCallCenter());
                requestCore.setSource(Constants.SOURCE_CALL_CENTER);
            }

            if (PaymentTypeEnum.PSE.getId() == order.getPaymentType().getId()) {
                requestCore.setFinancialInstitutions(order.getFinancialInstitutions());
                requestCore.setTypePersonPSE(order.getTypePersonPSE());
                requestCore.setIpAddress(order.getIpAddress());
                requestCore.setIdentification(order.getIdentification());
            }

            if (order.getSelfCheckout() != null && order.getSelfCheckout().getIsSelfCheckout() && order.getSelfCheckout().getIdBox() != null) {
                requestCore.setSelfCheckout(order.getSelfCheckout());
            }

            //Dejar el deliveryType como scanandgo para orden solo prime
            requestCore.setDeliveryType(DeliveryType.SCANANDGO.getDeliveryType());

//        create order in BACKEND3
            if (PaymentTypeEnum.PSE.getId() == order.getPaymentType().getId()) {
                requestCore.setPrimeMixedPSE(true);
            }

            LOG.info("create order in BACKEND3");

            CreateOrderResponse<CreatedOrder> createOrderResponse = createOrderViaBackend3V2(requestCore, order, orderJSON, TraceUtil.getXCloudTraceId(httpServletRequest), idCustomerWebSafe);

            orderJSON = createOrderResponse.getData();
            if (Objects.nonNull(createOrderResponse.getCode()) && createOrderResponse.getCode().equals("Created")) {
                LOG.info("response backend3 create order --> " + orderJSON.toString());

                if (orderJSON.getId() == 0 && PaymentTypeEnum.TRANSACCIONES_EN_LINEA.getId() == order.getPaymentType().getId()) {
                    return new CreateOrderResponse<>(HttpStatusCode.OK.getCode(), HttpStatusCode.OK.getStatusName(), Objects.nonNull(algoliaMessageConfigCreateOrder.getErrorPaymentOnline()) && isActiveMessagesAlgolia ? algoliaMessageConfigCreateOrder.getErrorPaymentOnline() : Constants.ERROR_PAYMENT_ONLINE, null);
                }

                if (orderJSON.getId() <= 0) {
                    return new CreateOrderResponse<>(HttpStatusCode.OK.getCode(), HttpStatusCode.OK.getStatusName(), Objects.nonNull(algoliaMessageConfigCreateOrder.getErrorCreatedOrderOms()) && isActiveMessagesAlgolia ? algoliaMessageConfigCreateOrder.getErrorCreatedOrderOms() : Constants.ERROR_CREATE_ORDER_OMS, null);
                }

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
                orderJSON.setItems(itemOrders);

                List<ProviderOrder> providers = new ArrayList<>();
                ProviderOrder provider = new ProviderOrder();
                provider.setName("");
                provider.setEmail("");
                provider.setDeliveryPrice(0);
                provider.setItems(itemOrders);
                providers.add(provider);
                orderJSON.setProviders(providers);

                LOG.info(String.valueOf(orderJSON.getId()));
                DeliveryOrder deliveryOrder = ofy().load().type(DeliveryOrder.class).filter("currentStatus", 1).ancestor(Ref.create(customerKey)).first().now();
                if (deliveryOrder == null)
                    return new CreateOrderResponse<>(HttpStatusCode.CONFLICT.getCode(), HttpStatusCode.CONFLICT.getStatusName(), Objects.nonNull(algoliaMessageConfigCreateOrder.getDeliveryOrderNotFound()) && isActiveMessagesAlgolia ? algoliaMessageConfigCreateOrder.getDeliveryOrderNotFound() : Constants.DELIVERY_ORDER_NOT_FOUND, null);
                deliveryOrder.setIdOrder(orderJSON.getId());
                deliveryOrder.setAddress(orderJSON.getAddress());
                deliveryOrder.setIdAddress(order.getIdAddress());
                deliveryOrder.setAddressDetails(order.getAddressDetails());
                deliveryOrder.setPaymentType(order.getPaymentType());
                if (order.getCustomerIdCallCenter() != null) {
                    deliveryOrder.setSource(Constants.SOURCE_CALL_CENTER);
                } else {
                    deliveryOrder.setSource(order.getSource());
                }


                deliveryOrder.setCurrentStatus(0);

                deliveryOrder.setIdFarmatodo(order.getIdFarmatodo());
                deliveryOrder.setLastStatus(ORDER_DELIVERED);
                deliveryOrder.setActive(true);

                deliveryOrder.setDeliveryType(DeliveryType.SCANANDGO);

                if (Objects.nonNull(orderJSON.getQrCode())) {
                    deliveryOrder.setQrCode(orderJSON.getQrCode());
                }
                //Consultar el resumen de la orden creada, para obtener valor de domicilio
                try {
//                LOG.info("method getOrderSumary: -> orderID:" + orderJSON.getId());
                    GetOrderSumary orderSumary = ApiGatewayService.get().getOrderSumary(orderJSON.getId());
                    if (orderSumary != null && orderSumary.getData() != null) {
//                    LOG.info("orderJSON.getDeliveryPrice() -> " + orderSumary.getData().getDeliveryValue());
                        deliveryOrder.setDeliveryPrice(orderSumary.getData().getDeliveryValue());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    LOG.severe("Error al consulatar resumen de orden! " + e.getMessage());
                }

                // if ScanAndGO
                if (isOrdenScanAndGo(order)) {
                    if (Objects.nonNull(order.getPaymentType()) && PaymentTypeEnum.DATAFONOS.getId().equals(order.getPaymentType().getId())) {
                        deliveryOrder.setLastStatus(ORDER_CREATED);

                    } else {
                        deliveryOrder.setLastStatus(ORDER_DELIVERED);
                        deliveryOrder.setActive(false);
                    }
                }

                if (isPrime && deliveryOrder.getDeliveryType().equals(DeliveryType.SCANANDGO)) {
                    deliveryOrder.setLastStatus(ORDER_DELIVERED);
                    deliveryOrder.setActive(false);
                }

                Timestamp stamp = new Timestamp(orderJSON.getCreateDate());
                Date date = new Date(stamp.getTime());
                deliveryOrder.setCreateDate(date);
                //if (orderJSON.getUpdateShopping()) {
                //    String getOrderURL = URLConnections.URL_ORDER + "/" + orderJSON.getId();
                //    DeliveryOrder processedOrder = CoreConnection.getRequest(getOrderURL, DeliveryOrder.class);
                //    deliveryOrder.updateShopping(processedOrder);
                //}
                if (order.getPickingDate() != null) {
                    deliveryOrder.setPickingDate(order.getPickingDate());
                    //LOG.warning("Picking Date to DS -> " + deliveryOrder.toString());
                }

                //Fix para borrar proveedor si no tiene items
                List<DeliveryOrderProvider> deleteProviders = new ArrayList<>();
//            LOG.info("tamaño proveedores : " + deliveryOrderProvidersList.size() + " list " + deliveryOrderProvidersList);
                for (DeliveryOrderProvider oderProvider : deliveryOrderProvidersList) {
//                LOG.info("key " + oderProvider.getIdDeliveryOrder() + " list " + oderProvider.getItemList());
                    if (oderProvider.getItemList() == null || oderProvider.getItemList().isEmpty()) {
                        deleteProviders.add(oderProvider);
                    }
                }

                ofy().delete().entities(deleteProviders);
                Key<DeliveryOrder> deliveryOrderKey = ofy().save().entity(deliveryOrder).now();

                if (Objects.nonNull(itemsNewOrder) && !itemsNewOrder.isEmpty()) {
                    try {
                        // Crea una orden con lo items sobrantes
//                    LOG.info("Crea una nueva orden para los items ->  " + itemsNewOrder.size());
                        createDeliveryOrder(customerKey, deliveryOrder.getDeliveryType().getDeliveryType(), itemsNewOrder, deliveryOrderProvidersList);
                    } catch (Exception ex) {
                        LOG.warning("No fue posible crear el nuevo Carrito con los items no utilizados " + ex.getMessage());
                    }
                }

                // Save tracing state
                if (Objects.nonNull(orderJSON.getTracing()) && !orderJSON.getTracing().isEmpty()) {
                    CreatedOrder finalOrderJSON = orderJSON;
                    DeliveryOrder finalOrder3 = order;
                    orderJSON.getTracing().forEach(tracing -> {
                        tracing.setIdTracing(UUID.randomUUID().toString());
                        tracing.setDeliveryOrderId(Ref.create(deliveryOrderKey));
                        if (tracing.getStatus() == 12) {
                            int responseSms = 0;
                            try {
                        /*responseSms = supportMethods.sendSms(order.getCustomerPhone(),
                                "Bienvenido a farmatodo. La transaccion asociada a tu orden "
                                        + finalOrderJSON.getId() + " fue aprobada");
*/
                                final SendSMSCloudFunctionReq request = new SendSMSCloudFunctionReq(finalOrder3.getCustomerPhone(),
                                        "Bienvenido a farmatodo. La transaccion asociada a tu orden "
                                                + finalOrderJSON.getId() + " fue aprobada");
                                CloudFunctionsService.get().postSendSms(request);
                            } catch (IOException e) {
                                LOG.warning("Error");
                            }
                            //LOG.warning("Response sms " + responseSms);
                        }
                    });
                    Tracing tracing = orderJSON.getTracing().get(0);
                    ofy().save().entity(tracing);
                }

                addMarcaCategorySubcategorieAndItemUrl(orderJSON);

                if (requestCore.getSelfCheckout() != null && requestCore.getSelfCheckout().getIsSelfCheckout() && requestCore.getSelfCheckout().getIdBox() != null) {
                    SelfCheckoutAlgolia selfCheckoutAlgolia = APIAlgolia.getSelfCheckout();
                    if (!Objects.isNull(selfCheckoutAlgolia) && !Objects.isNull(selfCheckoutAlgolia.getCustomerByStoresAndBox())) {
                        Optional<SelfCheckoutListAlgolia> selfCheckoutListAlgolia = selfCheckoutAlgolia.getCustomerByStoresAndBox().stream()
                                .filter(checkoutListAlgolia -> Objects.equals(checkoutListAlgolia.getStore(), requestCore.getStoreId()) && Objects.equals(checkoutListAlgolia.getBox(), requestCore.getSelfCheckout().getIdBox()))
                                .limit(1).findFirst();
                        if (selfCheckoutListAlgolia.isPresent()) {
                            deleteCoupon(customerKey);
                        }
                    }
                }
                DeliveryOrder deliveryOrderData = getOrderMethod(deliveryOrder, orderJSON.getId(), false, false);

                orderJSON.setOrderData(deliveryOrderData);
                ApiGatewayService.get().updateStratumBraze(requestCore.getCustomerId().toString());

                try {
                    Long sessionId = Long.parseLong(httpServletRequest.getHeader("amplitudeSessionId"));
                    if (sessionId != null) {
                        AmplitudeSessionRequest request = new AmplitudeSessionRequest();
                        request.setOrderId(orderJSON.getId());
                        request.setSessionId(sessionId);
//                LOG.info("request -> " + request.toString());
                        ApiGatewayService.get().saveAmplitudeSessionId(request);
                    }
                } catch (Exception e) {
                    LOG.warning(e.getMessage());
                }

                orderJSON.setPrime(true);
                orderJSON.setPrimeGeneral(true);


                addRMSclasses(orderJSON);
                sendEventCreate(orderJSON, requestCore.getCustomerId(), order.getPaymentType().getId());
                try {
                    LOG.info("CreateOrderResponse:#" + requestCore.getCustomerId() + " Gson -> " + new Gson().toJson(orderJSON));
                } catch (Exception e) {
                    LOG.info("No se pudo serializar Json de respuesta de creación de orden.");
                }
                createOrderResponse.setData(orderJSON);
                LOG.info("createOrderResponse -> " + createOrderResponse.toString());
                return createOrderResponse;
            } else {
                DeliveryOrder deliveryOrderOrder = ofy().load().type(DeliveryOrder.class).filter("currentStatus", 0).ancestor(Ref.create(customerKey)).first().now();
                deleteCoupon(customerKey);
                if (deliveryOrderOrder != null) {
                    ofy().delete().entity(deliveryOrderOrder).now();
                }
                LOG.info("createOrderResponse no Created-> " + createOrderResponse.toString());
                return createOrderResponse;
            }
        } catch (Exception e) {
            LOG.severe("Ocurrio un error al crear la orden => " + e.getMessage());
            return new CreateOrderResponse<>(HttpStatusCode.INTERNAL_SERVER_ERROR.getCode(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusName(), Constants.ERROR_CREATE_ORDER_OMS, null);
        }
    }

    private void deleteTipsForPrimeOrder(List<DeliveryOrderItem> deliveryOrderItemList, TipConfig tipConfig) {
        deliveryOrderItemList.removeIf(item -> {
            if (tipConfig == null || tipConfig.getItemTips() == null) {
                return false;
            }
            return tipConfig.getItemTips()
                    .stream()
                    .anyMatch(itemTip -> itemTip.getItemId() != null && itemTip.getItemId().longValue() == item.getId());
        });
    }


    private DeliveryOrder deleteItemsNoPrime(String idCustomerWebSafe, String token, final String tokenIdWebSafe, DeliveryOrder order) {
        try {
            List<Long> toRemove = new ArrayList<>();
            order.getItemList().forEach(item -> {
                if (!primeUtil.isItemPrime(item.getId())) {
                    final String itemSTR = String.valueOf(item.getId());
                    try {
                        deleteAllShoppingCart(idCustomerWebSafe, token, tokenIdWebSafe, itemSTR);
//                            Removing item of DeliveryOrder original
                        toRemove.add(item.getId());
                        LOG.info("Se elimino el item no prime -> " + item.getId());
                    } catch (Exception e) {
                        LOG.info("Error tratando de eliminar el item no prime -> " + itemSTR);
                        LOG.info("Mensaje de error -> " + e.getMessage());
                        try {
                            throw new ConflictException(e);
                        } catch (ConflictException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
            });
            order.getItemList().removeIf(item -> toRemove.contains(item.getId()));
        } catch (Exception e) {
            LOG.warning(
                    "Error eliminando items no prime, descripción -> " + e.getMessage()
            );
        }
        return order;
    }

    private List<DeliveryOrderItem> deleteDeliveryOrderItemsNoPrime(
            String idCustomerWebSafe,
            String token,
            final String tokenIdWebSafe,
            List<DeliveryOrderItem> deliveryOrderItems
    ) {
        try {
            List<Long> toRemove = new ArrayList<>();
            deliveryOrderItems.forEach(item -> {
                if (!primeUtil.isItemPrime(item.getId())) {
                    final String itemSTR = String.valueOf(item.getId());
                    try {
                        deleteAllShoppingCart(idCustomerWebSafe, token, tokenIdWebSafe, itemSTR);
                        toRemove.add(item.getId());
                    } catch (Exception e) {
                        LOG.warning("Ocurrio un error en deleteDeliveryOrderItemsNoPrime tratando de eliminar el item -> " + itemSTR);
                        LOG.warning(e.getMessage());
                        try {
                            throw new ConflictException(e);
                        } catch (ConflictException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }

            });
            deliveryOrderItems.removeIf(item -> toRemove.contains(item.getId()));
        } catch (Exception e) {
            LOG.warning("Ocurrio un error eliminando items para dejar solo la suscripción en deleteDeliveryOrderItemsNoPrime -> " + e.getMessage());
        }
        return deliveryOrderItems;
    }

    private void deleteAllShoppingCart(String idCustomerWebSafe, String token, String tokenIdWebSafe, String itemSTR) {
        try {
            deleteDeliveryOrderItem(token, tokenIdWebSafe, idCustomerWebSafe, 26, Integer.valueOf(itemSTR), "");
        } catch (ConflictException e) {
            throw new RuntimeException(e);
        } catch (BadRequestException e) {
            throw new RuntimeException(e);
        } catch (AlgoliaException e) {
            throw new RuntimeException(e);
        }
    }


    public CreatedOrder createOrderNoPrime(
            @Named("idCustomerWebSafe") final String idCustomerWebSafe,
            @Named("token") final String token,
            @Named("tokenIdWebSafe") final String tokenIdWebSafe,
            @Named("saving") final double saving,
            @Named("validateUserPrime") final boolean validateUserPrime,
            @Named("typeSubscription") final Long typeSubscription,
            @Named("orderPrimeID") final Long orderPrimeID,
            @Named("typeSubscriptionQuantity") final int typeSubscriptionQuantity,
            @Named("isFree") final boolean isFree,
            DeliveryOrder order,
            HttpServletRequest httpServletRequest
    ) throws BadRequestException, ConflictException, IOException, AlgoliaException, UnauthorizedException {

        if (!authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);

        boolean isScanAndGo = isOrdenScanAndGo(order);
        boolean isPrime = isOrderPrime(order);
        boolean isPrimeMixed = isOrderPrimeMixed(order);
        order.getItemList().removeIf(item -> Objects.equals(item.getId(), 0L));
        if (isPrimeMixed) {
            order = deleteItemsPrime(idCustomerWebSafe, token, tokenIdWebSafe, order);
        }

        if (!isScanAndGo && order.getIdAddress() == 0)
            throw new ConflictException("Direccion invalida");

        if (order.getPaymentType().getId() == PaymentTypeEnum.TRANSACCIONES_EN_LINEA.getId() && order.getPaymentCardId() <= 0)
            throw new ConflictException("PaymentCardId es obligatorio.");

        Key<Customer> customerKey = Key.create(idCustomerWebSafe);
        CreatedOrder orderJSON = new CreatedOrder();

        DeliveryOrder deliveryOrderSavedShoppingCart = ofy()
                .load()
                .type(DeliveryOrder.class)
                .filter("currentStatus", 1)
                .ancestor(Ref.create(customerKey))
                .first()
                .now();
        if (deliveryOrderSavedShoppingCart != null) {
            LOG.info("Para el usuario " + idCustomerWebSafe + "el id deliveryOrderSaved en shoppingCart es -> " + deliveryOrderSavedShoppingCart.getIdDeliveryOrder());
        }

        // validar si el cliente esta bloqueado
        BlockedUser blockedUser = ofy().load().type(BlockedUser.class).filter("idUser", order.getIdFarmatodo()).first().now();

        boolean userBlocked = blockedUser != null;

        if (userBlocked)
            throw new ConflictException("Usuario bloqueado no puede continuar");

        //TODO Se obtiene el id de la tienda mas cercana segun la direccion
        //List<Address> listAddress = customers.getAddressesFromCustomer(order.getIdFarmatodo());
        int closerStoreId = 0;
        if (!isScanAndGo) {
            AddressesRes addressesRes = ApiGatewayService.get().getAddressesByCustomerId(order.getIdFarmatodo());
            List<Address> listAddress = addressesRes.getAddresses();

            if (Objects.nonNull(listAddress)) {
                DeliveryOrder finalOrder = order;
                DeliveryOrder finalOrder1 = order;
                closerStoreId = listAddress.stream().filter(address -> Objects.nonNull(address) && Objects.nonNull(finalOrder.getDeliveryType()) && Objects.nonNull(address.getDeliveryType()) &&
                                address.getCloserStoreId() > 0 &&
                                finalOrder.getDeliveryType().getDeliveryType().equals(address.getDeliveryType().getDeliveryType()) &&
                                finalOrder.getIdAddress() == address.getIdAddress())
                        .mapToInt(address -> address.getCloserStoreId()).findFirst().isPresent() ? listAddress.stream().filter(address -> address.getCloserStoreId() > 0 &&
                                finalOrder1.getDeliveryType().getDeliveryType().equals(address.getDeliveryType().getDeliveryType()) &&
                                finalOrder1.getIdAddress() == address.getIdAddress())
                        .mapToInt(address -> address.getCloserStoreId()).findFirst().getAsInt() : 0;
            }
        } else {
            closerStoreId = order.getIdStoreGroup() == null ? 26 : Integer.valueOf(order.getIdStoreGroup());
        }

        String storeIdToValidate = closerStoreId > 0 ? String.valueOf(closerStoreId) : order.getIdStoreGroup();

        final Long finalStoreToValidate = Long.parseLong(storeIdToValidate);
        boolean validateStores = false;
        //exclude stores
        ExcludeStoresCreateOrder excludeStoresCreateOrder = APIAlgolia.getStoresToExcludeCreateOrder();

        if (Objects.nonNull(excludeStoresCreateOrder.getEnableStores()) && !excludeStoresCreateOrder.getEnableStores().isEmpty()) {
            for (Long storeExclude : excludeStoresCreateOrder.getEnableStores()) {
                if (storeExclude.equals(finalStoreToValidate)) {
                    validateStores = true;
                }
            }
        }

        // siempre estaran disponibles a menos que se encuentre en la lista
        Boolean isStoreAvailable = true;

        if (validateStores && !isScanAndGo) {
            isStoreAvailable = this.isStoreAvailable(storeIdToValidate, order.getPickingDate());
        }

        // Fix Validacion PickingDate
        if (Objects.nonNull(order.getPickingDate())) {
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            now = now.minus(5, ChronoUnit.HOURS);
            OffsetDateTime pickingDate = order.getPickingDate().toInstant().atOffset(ZoneOffset.UTC);
            if (pickingDate.isBefore(now))
                throw new ConflictException("La fecha de programación de la orden no es válida ");//+pickingDate+ " - "+now.toString());
        }

        if (!isStoreAvailable)
            throw new ConflictException(Constants.CLOSED_STORE);

        if (!Objects.isNull(order.getDeliveryType())) {
            if (order.getDeliveryType().getDeliveryType().equals(DeliveryType.NATIONAL.getDeliveryType())
                    && closerStoreId != URLConnections.NATIONAL_ID_STORE) {
                throw new ConflictException("Tienda no coincide con tipo de envio Nacional");
            }

            if (order.getDeliveryType().getDeliveryType().equals(DeliveryType.ENVIALOYA.getDeliveryType())
                    && closerStoreId != URLConnections.ENVIALOYA_ID_STORE) {
                throw new ConflictException("Tienda no coincide con tipo de envio Envialo ya");
            }
        }

        if (deliveryOrderSavedShoppingCart == null)
            throw new ConflictException(Constants.ERROR_CREATE_ORDER);
        List<DeliveryOrderItem> deliveryOrderItemList = ofy().load().type(DeliveryOrderItem.class).ancestor(deliveryOrderSavedShoppingCart).list();
        if (deliveryOrderItemList == null)
            throw new ConflictException(Constants.ERROR_CREATE_ORDER);

        // Fix items Duplicados
        validateDuplicateItems(deliveryOrderItemList);

        // NEW FIX
        //  obtener store and delivery type from address.!

        if (!isScanAndGo) {
            Key<User> userKey = Key.create(idCustomerWebSafe);
            User user = users.findUserByKey(userKey);
            if (user == null || user.getId() == 0)
                throw new UnauthorizedException(Constants.USER_NOT_FOUND);

            List<Address> allAddresses = this.customers.getAddressesByIdCustomer(user);

            Optional<Address> optionalAddress = Optional.empty();
            try {
                DeliveryOrder finalOrder2 = order;
                optionalAddress = allAddresses
                        .stream()
                        .filter(address -> address.getIdAddress().equals(finalOrder2.getIdAddress()))
                        .findFirst();
            } catch (Exception e) {
                LOG.warning("Customer: " + idCustomerWebSafe + " Error@createOrder filtering and finding address " + e.getMessage());
            }

            if (optionalAddress.isPresent()) {

                Address addressToCreateOrder = optionalAddress.get();
                if (addressToCreateOrder.getDeliveryType() != null && addressToCreateOrder.getCloserStoreId() > 0) {
                    // SET NEW DELIVERY_TYPE AND STORE
                    order.setDeliveryType(addressToCreateOrder.getDeliveryType());
                    order.setIdStoreGroup(String.valueOf(addressToCreateOrder.getCloserStoreId()));

                } else {
                    throw new ConflictException(Constants.ERROR_ADDRESS_CREATE_ORDER);
                }
            } else {
                throw new ConflictException(Constants.ERROR_ADDRESS_CREATE_ORDER);
            }

            LOG.info("Para el usuario " + idCustomerWebSafe + " fix new deliveryType -> " + order.getDeliveryType() + ", new store -> " + order.getIdStoreGroup());
        }

        // end fix


        // ScanAndGo Order
        List<DeliveryOrderItem> itemsScanAndGo = new ArrayList<>(deliveryOrderItemList);
        List<DeliveryOrderItem> itemsNewOrder = new ArrayList<>();
        if (isScanAndGo) {
            //set store princial when is scan and go
            itemsNewOrder = Objects.nonNull(itemsScanAndGo) ? itemsScanAndGo.stream().filter(it -> (Objects.isNull(it.getScanAndGo()) || !it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon())).collect(Collectors.toList()) : null;
            if (Objects.nonNull(itemsScanAndGo) && itemsScanAndGo.stream()
                    .filter(it -> (Objects.isNull(it.getScanAndGo()) || !it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon())).findFirst().isPresent()) {
                itemsScanAndGo.removeIf(it -> ((Objects.isNull(it.getScanAndGo()) || !it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon())));
            }
        } else {
            itemsNewOrder = Objects.nonNull(itemsScanAndGo) ? itemsScanAndGo.stream().filter(it -> (Objects.nonNull(it.getScanAndGo()) && it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon())).collect(Collectors.toList()) : null;
            if (Objects.nonNull(itemsScanAndGo) && itemsScanAndGo.stream()
                    .filter(it -> ((Objects.nonNull(it.getScanAndGo()) && it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon()))).findFirst().isPresent()) {
                itemsScanAndGo.removeIf(it -> ((Objects.nonNull(it.getScanAndGo()) && it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon())));
            }
        }

//        LOG.info("Items scan and go -> " + itemsScanAndGo.size());
//        if (Objects.nonNull(itemsNewOrder)) {
//            LOG.info("Items itemsNewOrder -> " + itemsNewOrder.size());
//        }


        // fix provider and scan and go

        List<DeliveryOrderProvider> deliveryOrderProvidersList = new ArrayList<>();
        if (!isScanAndGo) {
            // Providers
            deliveryOrderProvidersList = ofy().load().type(DeliveryOrderProvider.class).ancestor(deliveryOrderSavedShoppingCart).list();
            LOG.info("deliveryOrderProvidersList ->  " + (Objects.nonNull(deliveryOrderProvidersList) ? deliveryOrderProvidersList.size() : 0));
        }
        // Fix elimina items que no corresponden al tipo de envio actual
        if (Objects.nonNull(itemsNewOrder) && !itemsNewOrder.isEmpty()) {
            ofy().delete().entities(itemsNewOrder);
        }
        deliveryOrderItemList = itemsScanAndGo;

        OrderUtil.deleteTipPriceZero(deliveryOrderItemList);

        // TODO: Validacion para evitar cobrar productos que no esten visibles al usuario (APPS - Provedores externos)
        if (Objects.nonNull(order.getItemList()) && !order.getItemList().isEmpty()) {
//            LOG.info("method createOrder ->  valida Items de dataStore y los items parametro");
//            LOG.info("deliveryOrderItemList 1 ->  " + deliveryOrderItemList.size());
            Map<Long, Integer> mapValidationItem = order.getItemList().stream().collect(ImmutableMap.toImmutableMap(DeliveryOrderItem::getId, DeliveryOrderItem::getQuantitySold));
//            LOG.info("deliveryOrderItemList 2 ->  " + deliveryOrderItemList.size());
            if (Objects.nonNull(deliveryOrderProvidersList) && !deliveryOrderProvidersList.isEmpty()) {
//                LOG.info("method createOrder ->  Validando Items");
                deliveryOrderProvidersList.forEach(provider ->
                        provider.getItemList().removeIf(item -> !mapValidationItem.containsKey(item.getId())));
                // Validación para evitar que los items se dupliquen
                Map<Long, Integer> mapProviderItem = new HashMap<>();
                deliveryOrderProvidersList.stream()
                        .filter(provider -> (Objects.nonNull(provider.getItemList()) && !provider.getItemList().isEmpty()))
                        .forEach(provider -> provider.getItemList().parallelStream().forEach(itemProvider -> {
                                    mapProviderItem.put(itemProvider.getId(), itemProvider.getQuantitySold());
                                })
                        );
                // Elimina item de proveedores del listado normal de items
                deliveryOrderItemList.removeIf(item -> mapProviderItem.containsKey(item.getId()));
            }
        }
//        LOG.info("deliveryOrderItemList 3 ->  " + deliveryOrderItemList.size());
//        LOG.info("createOrder: has picking date ---> " + order.getPickingDate());
        if (!Objects.isNull(order.getPickingDate()) && verifyHighDemand(order, closerStoreId)) {
//            LOG.info("createOrder: isHigh Demand -> TRUE");
            setCufPerHighDemand(deliveryOrderItemList, closerStoreId);
        }

        String orderRequest = orders.createOrderJson(order, deliveryOrderItemList, deliveryOrderProvidersList).toString();
        LOG.info("createOrderJson ->  " + (orderRequest != null ? orderRequest : " es null"));

        // Retrofit method:

        CreateOrderRequestCore requestCore;
        Gson gson = new Gson();
        requestCore = gson.fromJson(orderRequest, CreateOrderRequestCore.class);

        if (requestCore.getCustomerId() != null && requestCore.getCustomerId() == 0) {
            // call service oms get customer id.
            CustomerAddressResponse customerAddressResponse = ApiGatewayService.get().getCustomerByAddressId(requestCore.getCustomerAddressId());
            if (customerAddressResponse != null && customerAddressResponse.getData() != null) {
                long customerIdAux = (long) customerAddressResponse.getData().getIdCustomer();
                requestCore.setCustomerId(customerIdAux);
            }
        }

        if (requestCore.getCustomerId() <= 0) {
            throw new ConflictException(Constants.ERROR_CREATE_ORDER);
        }

        try {
            final int storeId = closerStoreId;
            requestCore.getItems().forEach(item -> {
                final ItemAlgolia itemAlgolia = APIAlgolia.getItemAlgolia(item.getItemId() + "" + storeId);
                if (itemAlgolia != null) {
                    final int totalStock = itemAlgolia.getTotalStock();
//                    Validando cantidad solicitada > total stock
                    if (totalStock > 0 && item.getQuantityRequested() > totalStock) {
//                        LOG.info("QuantityRequested ANTES:" + item.getQuantityRequested());
                        item.setQuantityRequested(totalStock);
//                        LOG.info("QuantityRequested DESPUES:" + item.getQuantityRequested());
                    }
                }
            });
        } catch (Exception e) {
            LOG.warning("Error al intentar actualizar la canrtidad solictada al maximo stock de la tienda.");
        }

        if (Objects.nonNull(order.getCustomerIdCallCenter())) {
//            LOG.info("Insertando el customer del call");
            requestCore.setCustomerIdCallCenter(order.getCustomerIdCallCenter());
            requestCore.setSource(Constants.SOURCE_CALL_CENTER);
        }

        Boolean isPSE = false;

        if (PaymentTypeEnum.PSE.getId() == order.getPaymentType().getId()) {
            isPSE = true;
            requestCore.setFinancialInstitutions(order.getFinancialInstitutions());
            requestCore.setTypePersonPSE(order.getTypePersonPSE());
            requestCore.setIpAddress(order.getIpAddress());
            /*CreateOrderRequestCore identification = new CreateOrderRequestCore();
            identification.setIdentification(order.getIdentification());*/
            requestCore.setIdentification(order.getIdentification());
        }

        if (order.getSelfCheckout() != null && order.getSelfCheckout().getIsSelfCheckout() && order.getSelfCheckout().getIdBox() != null) {
            requestCore.setSelfCheckout(order.getSelfCheckout());
        }

        if (Objects.nonNull(typeSubscription)) {
            requestCore.setTypeSubscription(typeSubscription);
            requestCore.setTypeSubscriptionQuantity(typeSubscriptionQuantity);
            requestCore.setFreeDelivery(isFree);
            requestCore.setOrderPrimeId(orderPrimeID);
        }


        //create order in BACKEND3
        if (createOrderViaBackend3(requestCore, order, orderJSON, TraceUtil.getXCloudTraceId(httpServletRequest), idCustomerWebSafe)) {
            LOG.info("response backend3 --> " + orderJSON.toString());

            if (orderJSON.getId() == 0 && PaymentTypeEnum.TRANSACCIONES_EN_LINEA.getId() == order.getPaymentType().getId()) {
                throw new ConflictException(Constants.ERROR_PAYMENT_ONLINE);
            }

            if (orderJSON.getId() <= 0) {
                throw new ConflictException(Constants.ERROR_CREATE_ORDER);
            }
//            LOG.warning("New Order ->  " + orderRequest);
//            LOG.warning("Result New Order ->  " + orderJSON);

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
            orderJSON.setItems(itemOrders);

            List<ProviderOrder> providers = new ArrayList<>();
            ProviderOrder provider = new ProviderOrder();
            provider.setName("");
            provider.setEmail("");
            provider.setDeliveryPrice(0);
            provider.setItems(itemOrders);
            providers.add(provider);
            orderJSON.setProviders(providers);

            LOG.info(String.valueOf(orderJSON.getId()));
            DeliveryOrder deliveryOrder = ofy().load().type(DeliveryOrder.class).filter("currentStatus", 1).ancestor(Ref.create(customerKey)).first().now();
            if (deliveryOrder == null)
                throw new ConflictException(Constants.DELIVERY_ORDER_NOT_FOUND);
            deliveryOrder.setIdOrder(orderJSON.getId());
            deliveryOrder.setAddress(orderJSON.getAddress());
            deliveryOrder.setIdAddress(order.getIdAddress());
            deliveryOrder.setAddressDetails(order.getAddressDetails());
            deliveryOrder.setPaymentType(order.getPaymentType());

            if (order.getCustomerIdCallCenter() != null) {
                deliveryOrder.setSource(Constants.SOURCE_CALL_CENTER);
            } else {
                deliveryOrder.setSource(order.getSource());
            }

            if (!PaymentTypeEnum.PSE.getId().equals(order.getPaymentType().getId())) {
                deliveryOrder.setCurrentStatus(0);
            }
            deliveryOrder.setIdFarmatodo(order.getIdFarmatodo());
            deliveryOrder.setLastStatus(ORDER_CREATED);
            deliveryOrder.setActive(true);

            deliveryOrder.setDeliveryType(order.getDeliveryType());

            if (Objects.nonNull(orderJSON.getQrCode())) {
                deliveryOrder.setQrCode(orderJSON.getQrCode());
            }
            //Consultar el resumen de la orden creada, para obtener valor de domicilio
            try {
//                LOG.info("method getOrderSumary: -> orderID:" + orderJSON.getId());
                GetOrderSumary orderSumary = ApiGatewayService.get().getOrderSumary(orderJSON.getId());
                if (orderSumary != null && orderSumary.getData() != null) {
//                    LOG.info("orderJSON.getDeliveryPrice() -> " + orderSumary.getData().getDeliveryValue());
                    deliveryOrder.setDeliveryPrice(orderSumary.getData().getDeliveryValue());
                }
            } catch (Exception e) {
                e.printStackTrace();
                LOG.severe("Error al consulatar resumen de orden! " + e.getMessage());
            }

            // if ScanAndGO
            if (isOrdenScanAndGo(order)) {
                if (Objects.nonNull(order.getPaymentType()) && PaymentTypeEnum.DATAFONOS.getId().equals(order.getPaymentType().getId())) {
                    deliveryOrder.setLastStatus(ORDER_CREATED);

                } else {
                    deliveryOrder.setLastStatus(ORDER_DELIVERED);
                    deliveryOrder.setActive(false);
                }
            }

            if (isPrime && deliveryOrder.getDeliveryType().equals(DeliveryType.SCANANDGO)) {
                deliveryOrder.setLastStatus(ORDER_DELIVERED);
                deliveryOrder.setActive(false);
            }

            Timestamp stamp = new Timestamp(orderJSON.getCreateDate());
            Date date = new Date(stamp.getTime());
            deliveryOrder.setCreateDate(date);
            //if (orderJSON.getUpdateShopping()) {
            //    String getOrderURL = URLConnections.URL_ORDER + "/" + orderJSON.getId();
            //    DeliveryOrder processedOrder = CoreConnection.getRequest(getOrderURL, DeliveryOrder.class);
            //    deliveryOrder.updateShopping(processedOrder);
            //}
            if (order.getPickingDate() != null) {
                deliveryOrder.setPickingDate(order.getPickingDate());
                //LOG.warning("Picking Date to DS -> " + deliveryOrder.toString());
            }

            //Fix para borrar proveedor si no tiene items
            List<DeliveryOrderProvider> deleteProviders = new ArrayList<>();
//            LOG.info("tamaño proveedores : " + deliveryOrderProvidersList.size() + " list " + deliveryOrderProvidersList);
            for (DeliveryOrderProvider oderProvider : deliveryOrderProvidersList) {
//                LOG.info("key " + oderProvider.getIdDeliveryOrder() + " list " + oderProvider.getItemList());
                if (oderProvider.getItemList() == null || oderProvider.getItemList().isEmpty()) {
                    deleteProviders.add(oderProvider);
                }
            }

            ofy().delete().entities(deleteProviders);
//            LOG.info("savin pre save delivery prime {}"+ saving);
            deliveryOrder.setSavingPrime(saving);
            Key<DeliveryOrder> deliveryOrderKey = ofy().save().entity(deliveryOrder).now();

            if (Objects.nonNull(itemsNewOrder) && !itemsNewOrder.isEmpty()) {
                try {
                    // Crea una orden con lo items sobrantes
//                    LOG.info("Crea una nueva orden para los items ->  " + itemsNewOrder.size());
                    createDeliveryOrder(customerKey, deliveryOrder.getDeliveryType().getDeliveryType(), itemsNewOrder, deliveryOrderProvidersList);
                } catch (Exception ex) {
                    LOG.warning("No fue posible crear el nuevo Carrito con los items no utilizados " + ex.getMessage());
                }
            }

            // Save tracing state
            if (Objects.nonNull(orderJSON.getTracing()) && !orderJSON.getTracing().isEmpty()) {
                CreatedOrder finalOrderJSON = orderJSON;
                DeliveryOrder finalOrder3 = order;
                orderJSON.getTracing().forEach(tracing -> {
                    tracing.setIdTracing(UUID.randomUUID().toString());
                    tracing.setDeliveryOrderId(Ref.create(deliveryOrderKey));
                    if (tracing.getStatus() == 12) {
                        int responseSms = 0;
                        try {
                        /*responseSms = supportMethods.sendSms(order.getCustomerPhone(),
                                "Bienvenido a farmatodo. La transaccion asociada a tu orden "
                                        + finalOrderJSON.getId() + " fue aprobada");
*/
                            final SendSMSCloudFunctionReq request = new SendSMSCloudFunctionReq(finalOrder3.getCustomerPhone(),
                                    "Bienvenido a farmatodo. La transaccion asociada a tu orden "
                                            + finalOrderJSON.getId() + " fue aprobada");
                            CloudFunctionsService.get().postSendSms(request);
                        } catch (IOException e) {
                            LOG.warning("Error");
                        }
                        //LOG.warning("Response sms " + responseSms);
                    }
                });
                Tracing tracing = orderJSON.getTracing().get(0);
                ofy().save().entity(tracing);
            }

            addMarcaCategorySubcategorieAndItemUrl(orderJSON);

            if (requestCore.getSelfCheckout() != null && requestCore.getSelfCheckout().getIsSelfCheckout() && requestCore.getSelfCheckout().getIdBox() != null) {
                SelfCheckoutAlgolia selfCheckoutAlgolia = APIAlgolia.getSelfCheckout();
                if (!Objects.isNull(selfCheckoutAlgolia) && !Objects.isNull(selfCheckoutAlgolia.getCustomerByStoresAndBox())) {
                    Optional<SelfCheckoutListAlgolia> selfCheckoutListAlgolia = selfCheckoutAlgolia.getCustomerByStoresAndBox().stream()
                            .filter(checkoutListAlgolia -> Objects.equals(checkoutListAlgolia.getStore(), requestCore.getStoreId()) && Objects.equals(checkoutListAlgolia.getBox(), requestCore.getSelfCheckout().getIdBox()))
                            .limit(1).findFirst();
                    if (selfCheckoutListAlgolia.isPresent()) {
                        deleteCoupon(customerKey);
                    }
                }
            }
            DeliveryOrder deliveryOrderData = getOrderMethod(deliveryOrder, orderJSON.getId(), false, false);

            orderJSON.setOrderData(deliveryOrderData);
            ApiGatewayService.get().updateStratumBraze(requestCore.getCustomerId().toString());


//            LOG.info("saving order"+saving);
            if (saving > 0) {
                SavingCustomer requestSaving = getSavingCustomerRequest((long) order.getIdFarmatodo(), saving, UpdateTypeSavingEnum.ADD);
                try {
                    ApiGatewayService.get().sendSavingNoPrime(requestSaving);
                } catch (Exception e) {
                    LOG.warning("Error al guardar el saving");
                }
            }
            if ((isPrime || isPrimeMixed)) {
                orderJSON.setPrimeGeneral(true);
                if (!isPSE) {
                    orderJSON.setPrime(true);
                    orderJSON.setPrimeGeneral(false);
                }
            } else {
                orderJSON.setPrimeGeneral(false);
                orderJSON.setPrime(false);
            }

        } else {
            DeliveryOrder deliveryOrderOrder = ofy().load().type(DeliveryOrder.class).filter("currentStatus", 0).ancestor(Ref.create(customerKey)).first().now();
            deleteCoupon(customerKey);
            if (deliveryOrderOrder == null) {
                throw new ConflictException(Constants.ERROR_CREATE_ORDER);
            } else {
                ofy().delete().entity(deliveryOrderOrder).now();
                LOG.info("SE elimina la orden del DataStore ya que no se pudo crear en OMS");
                throw new ConflictException(Constants.ERROR_CREATE_ORDER);
            }

        }
        try {
            Long sessionId = Long.parseLong(httpServletRequest.getHeader("amplitudeSessionId"));
            if (sessionId != null) {
                AmplitudeSessionRequest request = new AmplitudeSessionRequest();
                request.setOrderId(orderJSON.getId());
                request.setSessionId(sessionId);
//                LOG.info("request -> " + request.toString());
                ApiGatewayService.get().saveAmplitudeSessionId(request);
            }
        } catch (Exception e) {
            LOG.warning(e.getMessage());
        }


        addRMSclasses(orderJSON);
        sendEventCreate(orderJSON, requestCore.getCustomerId(), order.getPaymentType().getId());
        try {
            LOG.info("CreateOrderResponse:#" + requestCore.getCustomerId() + " Gson -> " + new Gson().toJson(orderJSON));
        } catch (Exception e) {
            LOG.info("No se pudo serializar Json de respuesta de creación de orden.");
        }
        return orderJSON;
    }


    public CreateOrderResponse<CreatedOrder> createOrderNoPrimeV2(
            @Named("idCustomerWebSafe") final String idCustomerWebSafe,
            @Named("token") final String token,
            @Named("tokenIdWebSafe") final String tokenIdWebSafe,
            @Named("saving") final double saving,
            @Named("validateUserPrime") final boolean validateUserPrime,
            @Named("typeSubscription") final Long typeSubscription,
            @Named("orderPrimeID") final Long orderPrimeID,
            @Named("typeSubscriptionQuantity") final int typeSubscriptionQuantity,
            @Named("isFree") final boolean isFree,
            DeliveryOrder order,
            HttpServletRequest httpServletRequest
    ) throws BadRequestException, ConflictException, IOException, AlgoliaException, UnauthorizedException, InternalServerErrorException {

        try {
            AlgoliaMessageConfigCreateOrder algoliaMessageConfigCreateOrder = APIAlgolia.getMessagesCreateOrder();
            if (Objects.isNull(algoliaMessageConfigCreateOrder)) {
                LOG.severe("method: orderEndpoint/v2/createOrder -> fallo algolia en traer la configuracion de mensajes del create order.");
            }
            boolean isActiveMessagesAlgolia = Objects.nonNull(algoliaMessageConfigCreateOrder.getActive()) && algoliaMessageConfigCreateOrder.getActive();

            if (!authenticate.isValidToken(token, tokenIdWebSafe))
                return new CreateOrderResponse<>(HttpStatusCode.BAD_REQUEST.getCode(), HttpStatusCode.BAD_REQUEST.getStatusName(), Objects.nonNull(algoliaMessageConfigCreateOrder.getInvalidToken()) && isActiveMessagesAlgolia ? algoliaMessageConfigCreateOrder.getInvalidToken() : Constants.INVALID_TOKEN, null);

            boolean isScanAndGo = isOrdenScanAndGo(order);
            boolean isPrime = isOrderPrime(order);
            boolean isPrimeMixed = isOrderPrimeMixed(order);
            order.getItemList().removeIf(item -> Objects.equals(item.getId(), 0L));
            if (isPrimeMixed) {
                order = deleteItemsPrime(idCustomerWebSafe, token, tokenIdWebSafe, order);
            }

            if (!isScanAndGo && order.getIdAddress() == 0)
                return new CreateOrderResponse<>(HttpStatusCode.BAD_REQUEST.getCode(), HttpStatusCode.BAD_REQUEST.getStatusName(), Objects.nonNull(algoliaMessageConfigCreateOrder.getInvalidAddress()) && isActiveMessagesAlgolia ? algoliaMessageConfigCreateOrder.getInvalidAddress() : Constants.INVALID_ADDRESS, null);

            if (order.getPaymentType().getId() == PaymentTypeEnum.TRANSACCIONES_EN_LINEA.getId() && order.getPaymentCardId() <= 0)
                return new CreateOrderResponse<>(HttpStatusCode.BAD_REQUEST.getCode(), HttpStatusCode.BAD_REQUEST.getStatusName(), Objects.nonNull(algoliaMessageConfigCreateOrder.getMandatoryPaymentCardId()) && isActiveMessagesAlgolia ? algoliaMessageConfigCreateOrder.getMandatoryPaymentCardId() : Constants.MANDATORY_PAYMENTCARD_ID, null);

            Key<Customer> customerKey = Key.create(idCustomerWebSafe);
            CreatedOrder orderJSON = new CreatedOrder();

            DeliveryOrder deliveryOrderSavedShoppingCart = ofy()
                    .load()
                    .type(DeliveryOrder.class)
                    .filter("currentStatus", 1)
                    .ancestor(Ref.create(customerKey))
                    .first()
                    .now();
            if (deliveryOrderSavedShoppingCart != null) {
                LOG.info("Para el usuario " + idCustomerWebSafe + "el id deliveryOrderSaved en shoppingCart es -> " + deliveryOrderSavedShoppingCart.getIdDeliveryOrder());
            }

            // validar si el cliente esta bloqueado
            BlockedUser blockedUser = ofy().load().type(BlockedUser.class).filter("idUser", order.getIdFarmatodo()).first().now();

            boolean userBlocked = blockedUser != null;

            if (userBlocked)
                return new CreateOrderResponse<>(HttpStatusCode.FORBIDDEN.getCode(), HttpStatusCode.FORBIDDEN.getStatusName(), Objects.nonNull(algoliaMessageConfigCreateOrder.getUserBlocked()) && isActiveMessagesAlgolia ? algoliaMessageConfigCreateOrder.getUserBlocked() : Constants.USER_BLOCKED, null);

            //TODO Se obtiene el id de la tienda mas cercana segun la direccion
            //List<Address> listAddress = customers.getAddressesFromCustomer(order.getIdFarmatodo());
            int closerStoreId = 0;
            if (!isScanAndGo) {
                AddressesRes addressesRes = ApiGatewayService.get().getAddressesByCustomerId(order.getIdFarmatodo());
                List<Address> listAddress = addressesRes.getAddresses();

                if (Objects.nonNull(listAddress)) {
                    DeliveryOrder finalOrder = order;
                    DeliveryOrder finalOrder1 = order;
                    closerStoreId = listAddress.stream().filter(address -> Objects.nonNull(address) && Objects.nonNull(finalOrder.getDeliveryType()) && Objects.nonNull(address.getDeliveryType()) &&
                                    address.getCloserStoreId() > 0 &&
                                    finalOrder.getDeliveryType().getDeliveryType().equals(address.getDeliveryType().getDeliveryType()) &&
                                    finalOrder.getIdAddress() == address.getIdAddress())
                            .mapToInt(address -> address.getCloserStoreId()).findFirst().isPresent() ? listAddress.stream().filter(address -> address.getCloserStoreId() > 0 &&
                                    finalOrder1.getDeliveryType().getDeliveryType().equals(address.getDeliveryType().getDeliveryType()) &&
                                    finalOrder1.getIdAddress() == address.getIdAddress())
                            .mapToInt(address -> address.getCloserStoreId()).findFirst().getAsInt() : 0;
                }
            } else {
                closerStoreId = order.getIdStoreGroup() == null ? 26 : Integer.valueOf(order.getIdStoreGroup());
            }

            String storeIdToValidate = closerStoreId > 0 ? String.valueOf(closerStoreId) : order.getIdStoreGroup();

            final Long finalStoreToValidate = Long.parseLong(storeIdToValidate);
            boolean validateStores = false;
            //exclude stores
            ExcludeStoresCreateOrder excludeStoresCreateOrder = APIAlgolia.getStoresToExcludeCreateOrder();

            if (Objects.nonNull(excludeStoresCreateOrder.getEnableStores()) && !excludeStoresCreateOrder.getEnableStores().isEmpty()) {
                for (Long storeExclude : excludeStoresCreateOrder.getEnableStores()) {
                    if (storeExclude.equals(finalStoreToValidate)) {
                        validateStores = true;
                    }
                }
            }

            // siempre estaran disponibles a menos que se encuentre en la lista
            Boolean isStoreAvailable = true;

            if (validateStores && !isScanAndGo) {
                isStoreAvailable = this.isStoreAvailable(storeIdToValidate, order.getPickingDate());
            }

            // Fix Validacion PickingDate
            if (Objects.nonNull(order.getPickingDate())) {
                OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
                now = now.minus(5, ChronoUnit.HOURS);
                OffsetDateTime pickingDate = order.getPickingDate().toInstant().atOffset(ZoneOffset.UTC);
                if (pickingDate.isBefore(now))
                    return new CreateOrderResponse<>(HttpStatusCode.BAD_REQUEST.getCode(), HttpStatusCode.BAD_REQUEST.getStatusName(), Objects.nonNull(algoliaMessageConfigCreateOrder.getInvalidOrderScheduled()) && isActiveMessagesAlgolia ? algoliaMessageConfigCreateOrder.getInvalidOrderScheduled() : Constants.INVALID_PROGRAMMING, null); //+pickingDate+ " - "+now.toString());
            }

            if (!isStoreAvailable)
                return new CreateOrderResponse<>(HttpStatusCode.OK.getCode(), HttpStatusCode.OK.getStatusName(), Objects.nonNull(algoliaMessageConfigCreateOrder.getClosedStore()) && isActiveMessagesAlgolia ? algoliaMessageConfigCreateOrder.getClosedStore() : Constants.CLOSED_STORE, null);

            if (!Objects.isNull(order.getDeliveryType())) {
                if (order.getDeliveryType().getDeliveryType().equals(DeliveryType.NATIONAL.getDeliveryType())
                        && closerStoreId != URLConnections.NATIONAL_ID_STORE) {
                    return new CreateOrderResponse<>(HttpStatusCode.BAD_REQUEST.getCode(), HttpStatusCode.BAD_REQUEST.getStatusName(), Objects.nonNull(algoliaMessageConfigCreateOrder.getInvalidNationalStore()) && isActiveMessagesAlgolia ? algoliaMessageConfigCreateOrder.getInvalidNationalStore() : Constants.INVALID_NATIONAL_STORE, null);
                }

                if (order.getDeliveryType().getDeliveryType().equals(DeliveryType.ENVIALOYA.getDeliveryType())
                        && closerStoreId != URLConnections.ENVIALOYA_ID_STORE) {
                    return new CreateOrderResponse<>(HttpStatusCode.BAD_REQUEST.getCode(), HttpStatusCode.BAD_REQUEST.getStatusName(), Objects.nonNull(algoliaMessageConfigCreateOrder.getInvalidEnvialoyaStore()) && isActiveMessagesAlgolia ? algoliaMessageConfigCreateOrder.getInvalidEnvialoyaStore() : Constants.INVALID_ENVIALOYA_STORE, null);
                }
            }

            if (deliveryOrderSavedShoppingCart == null)
                return new CreateOrderResponse<>(HttpStatusCode.BAD_REQUEST.getCode(), HttpStatusCode.BAD_REQUEST.getStatusName(), Objects.nonNull(algoliaMessageConfigCreateOrder.getEmptyShoppingCart()) && isActiveMessagesAlgolia ? algoliaMessageConfigCreateOrder.getEmptyShoppingCart() : Constants.EMPTY_SHOPPING_CART, null);
            List<DeliveryOrderItem> deliveryOrderItemList = ofy().load().type(DeliveryOrderItem.class).ancestor(deliveryOrderSavedShoppingCart).list();
            if (deliveryOrderItemList == null)
                return new CreateOrderResponse<>(HttpStatusCode.BAD_REQUEST.getCode(), HttpStatusCode.BAD_REQUEST.getStatusName(), Objects.nonNull(algoliaMessageConfigCreateOrder.getEmptyShoppingCart()) && isActiveMessagesAlgolia ? algoliaMessageConfigCreateOrder.getEmptyShoppingCart() : Constants.EMPTY_SHOPPING_CART, null);

            // Fix items Duplicados
            validateDuplicateItems(deliveryOrderItemList);

            // NEW FIX
            //  obtener store and delivery type from address.!

            if (!isScanAndGo) {
                Key<User> userKey = Key.create(idCustomerWebSafe);
                User user = users.findUserByKey(userKey);
                if (user == null || user.getId() == 0)
                    return new CreateOrderResponse<>(HttpStatusCode.UNAUTHORIZED.getCode(), HttpStatusCode.UNAUTHORIZED.getStatusName(), Objects.nonNull(algoliaMessageConfigCreateOrder.getUserNotFound()) && isActiveMessagesAlgolia ? algoliaMessageConfigCreateOrder.getUserNotFound() : Constants.USER_NOT_FOUND, null);

                List<Address> allAddresses = this.customers.getAddressesByIdCustomer(user);

                Optional<Address> optionalAddress = Optional.empty();
                try {
                    DeliveryOrder finalOrder2 = order;
                    optionalAddress = allAddresses
                            .stream()
                            .filter(address -> address.getIdAddress().equals(finalOrder2.getIdAddress()))
                            .findFirst();
                } catch (Exception e) {
                    LOG.warning("Customer: " + idCustomerWebSafe + " Error@createOrder filtering and finding address " + e.getMessage());
                }

                if (optionalAddress.isPresent()) {

                    Address addressToCreateOrder = optionalAddress.get();
                    if (addressToCreateOrder.getDeliveryType() != null && addressToCreateOrder.getCloserStoreId() > 0) {
                        // SET NEW DELIVERY_TYPE AND STORE
                        order.setDeliveryType(addressToCreateOrder.getDeliveryType());
                        order.setIdStoreGroup(String.valueOf(addressToCreateOrder.getCloserStoreId()));

                    } else {
                        return new CreateOrderResponse<>(HttpStatusCode.CONFLICT.getCode(), HttpStatusCode.CONFLICT.getStatusName(), Objects.nonNull(algoliaMessageConfigCreateOrder.getErrorAddressInvalid()) && isActiveMessagesAlgolia ? algoliaMessageConfigCreateOrder.getErrorAddressInvalid() : Constants.ERROR_ADDRESS_CREATE_ORDER, null);
                    }
                } else {
                    return new CreateOrderResponse<>(HttpStatusCode.CONFLICT.getCode(), HttpStatusCode.CONFLICT.getStatusName(), Objects.nonNull(algoliaMessageConfigCreateOrder.getErrorAddressInvalid()) && isActiveMessagesAlgolia ? algoliaMessageConfigCreateOrder.getErrorAddressInvalid() : Constants.ERROR_ADDRESS_CREATE_ORDER, null);
                }

                LOG.info("Para el usuario " + idCustomerWebSafe + " fix new deliveryType -> " + order.getDeliveryType() + ", new store -> " + order.getIdStoreGroup());
            }

            // end fix


            // ScanAndGo Order
            List<DeliveryOrderItem> itemsScanAndGo = new ArrayList<>(deliveryOrderItemList);
            List<DeliveryOrderItem> itemsNewOrder = new ArrayList<>();
            if (isScanAndGo) {
                //set store princial when is scan and go
                itemsNewOrder = Objects.nonNull(itemsScanAndGo) ? itemsScanAndGo.stream().filter(it -> (Objects.isNull(it.getScanAndGo()) || !it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon())).collect(Collectors.toList()) : null;
                if (Objects.nonNull(itemsScanAndGo) && itemsScanAndGo.stream()
                        .filter(it -> (Objects.isNull(it.getScanAndGo()) || !it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon())).findFirst().isPresent()) {
                    itemsScanAndGo.removeIf(it -> ((Objects.isNull(it.getScanAndGo()) || !it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon())));
                }
            } else {
                itemsNewOrder = Objects.nonNull(itemsScanAndGo) ? itemsScanAndGo.stream().filter(it -> (Objects.nonNull(it.getScanAndGo()) && it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon())).collect(Collectors.toList()) : null;
                if (Objects.nonNull(itemsScanAndGo) && itemsScanAndGo.stream()
                        .filter(it -> ((Objects.nonNull(it.getScanAndGo()) && it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon()))).findFirst().isPresent()) {
                    itemsScanAndGo.removeIf(it -> ((Objects.nonNull(it.getScanAndGo()) && it.getScanAndGo()) && (Objects.isNull(it.getCoupon()) || !it.getCoupon())));
                }
            }

//        LOG.info("Items scan and go -> " + itemsScanAndGo.size());
//        if (Objects.nonNull(itemsNewOrder)) {
//            LOG.info("Items itemsNewOrder -> " + itemsNewOrder.size());
//        }


            // fix provider and scan and go

            List<DeliveryOrderProvider> deliveryOrderProvidersList = new ArrayList<>();
            if (!isScanAndGo) {
                // Providers
                deliveryOrderProvidersList = ofy().load().type(DeliveryOrderProvider.class).ancestor(deliveryOrderSavedShoppingCart).list();
                LOG.info("deliveryOrderProvidersList ->  " + (Objects.nonNull(deliveryOrderProvidersList) ? deliveryOrderProvidersList.size() : 0));
            }
            // Fix elimina items que no corresponden al tipo de envio actual
            if (Objects.nonNull(itemsNewOrder) && !itemsNewOrder.isEmpty()) {
                ofy().delete().entities(itemsNewOrder);
            }
            deliveryOrderItemList = itemsScanAndGo;

            OrderUtil.deleteTipPriceZero(deliveryOrderItemList);

            // TODO: Validacion para evitar cobrar productos que no esten visibles al usuario (APPS - Provedores externos)
            if (Objects.nonNull(order.getItemList()) && !order.getItemList().isEmpty()) {
//            LOG.info("method createOrder ->  valida Items de dataStore y los items parametro");
//            LOG.info("deliveryOrderItemList 1 ->  " + deliveryOrderItemList.size());
                Map<Long, Integer> mapValidationItem = order.getItemList().stream().collect(ImmutableMap.toImmutableMap(DeliveryOrderItem::getId, DeliveryOrderItem::getQuantitySold));
//            LOG.info("deliveryOrderItemList 2 ->  " + deliveryOrderItemList.size());
                if (Objects.nonNull(deliveryOrderProvidersList) && !deliveryOrderProvidersList.isEmpty()) {
//                LOG.info("method createOrder ->  Validando Items");
                    deliveryOrderProvidersList.forEach(provider ->
                            provider.getItemList().removeIf(item -> !mapValidationItem.containsKey(item.getId())));
                    // Validación para evitar que los items se dupliquen
                    Map<Long, Integer> mapProviderItem = new HashMap<>();
                    deliveryOrderProvidersList.stream()
                            .filter(provider -> (Objects.nonNull(provider.getItemList()) && !provider.getItemList().isEmpty()))
                            .forEach(provider -> provider.getItemList().parallelStream().forEach(itemProvider -> {
                                        mapProviderItem.put(itemProvider.getId(), itemProvider.getQuantitySold());
                                    })
                            );
                    // Elimina item de proveedores del listado normal de items
                    deliveryOrderItemList.removeIf(item -> mapProviderItem.containsKey(item.getId()));
                }
            }
//        LOG.info("deliveryOrderItemList 3 ->  " + deliveryOrderItemList.size());
//        LOG.info("createOrder: has picking date ---> " + order.getPickingDate());
            if (!Objects.isNull(order.getPickingDate()) && verifyHighDemand(order, closerStoreId)) {
//            LOG.info("createOrder: isHigh Demand -> TRUE");
                setCufPerHighDemand(deliveryOrderItemList, closerStoreId);
            }

            String orderRequest = orders.createOrderJson(order, deliveryOrderItemList, deliveryOrderProvidersList).toString();
            LOG.info("createOrderJson ->  " + (orderRequest != null ? orderRequest : " es null"));

            // Retrofit method:

            CreateOrderRequestCore requestCore;
            Gson gson = new Gson();
            requestCore = gson.fromJson(orderRequest, CreateOrderRequestCore.class);

            if (requestCore.getCustomerId() != null && requestCore.getCustomerId() == 0) {
                // call service oms get customer id.
                CustomerAddressResponse customerAddressResponse = ApiGatewayService.get().getCustomerByAddressId(requestCore.getCustomerAddressId());
                if (customerAddressResponse != null && customerAddressResponse.getData() != null) {
                    long customerIdAux = (long) customerAddressResponse.getData().getIdCustomer();
                    requestCore.setCustomerId(customerIdAux);
                }
            }

            if (requestCore.getCustomerId() <= 0) {
                return new CreateOrderResponse<>(HttpStatusCode.NOT_FOUND.getCode(), HttpStatusCode.NOT_FOUND.getStatusName(), Objects.nonNull(algoliaMessageConfigCreateOrder.getUserNotFound()) && isActiveMessagesAlgolia ? algoliaMessageConfigCreateOrder.getUserNotFound() : Constants.USER_NOT_FOUND, null);
            }

            try {
                final int storeId = closerStoreId;
                requestCore.getItems().forEach(item -> {
                    final ItemAlgolia itemAlgolia = APIAlgolia.getItemAlgolia(item.getItemId() + "" + storeId);
                    if (itemAlgolia != null) {
                        final int totalStock = itemAlgolia.getTotalStock();
//                    Validando cantidad solicitada > total stock
                        if (totalStock > 0 && item.getQuantityRequested() > totalStock) {
//                        LOG.info("QuantityRequested ANTES:" + item.getQuantityRequested());
                            item.setQuantityRequested(totalStock);
//                        LOG.info("QuantityRequested DESPUES:" + item.getQuantityRequested());
                        }
                    }
                });
            } catch (Exception e) {
                LOG.warning("Error al intentar actualizar la canrtidad solictada al maximo stock de la tienda.");
            }

            if (Objects.nonNull(order.getCustomerIdCallCenter())) {
//            LOG.info("Insertando el customer del call");
                requestCore.setCustomerIdCallCenter(order.getCustomerIdCallCenter());
                requestCore.setSource(Constants.SOURCE_CALL_CENTER);
            }

            Boolean isPSE = false;

            if (PaymentTypeEnum.PSE.getId() == order.getPaymentType().getId()) {
                isPSE = true;
                requestCore.setFinancialInstitutions(order.getFinancialInstitutions());
                requestCore.setTypePersonPSE(order.getTypePersonPSE());
                requestCore.setIpAddress(order.getIpAddress());
            /*CreateOrderRequestCore identification = new CreateOrderRequestCore();
            identification.setIdentification(order.getIdentification());*/
                requestCore.setIdentification(order.getIdentification());
            }

            if (order.getSelfCheckout() != null && order.getSelfCheckout().getIsSelfCheckout() && order.getSelfCheckout().getIdBox() != null) {
                requestCore.setSelfCheckout(order.getSelfCheckout());
            }

            if (Objects.nonNull(typeSubscription)) {
                requestCore.setTypeSubscription(typeSubscription);
                requestCore.setTypeSubscriptionQuantity(typeSubscriptionQuantity);
                requestCore.setFreeDelivery(isFree);
                requestCore.setOrderPrimeId(orderPrimeID);
            }

            //farmacredits
            if (Objects.nonNull(order.getFarmaCredits()) && order.getFarmaCredits() > 0) {
                requestCore.setFarmaCredits(order.getFarmaCredits());
            }

            if (Objects.nonNull(order.getTalonOneData())) {
                requestCore.setTalonOneData(order.getTalonOneData());
            }

            validateSetOptimalRoutePopInformation(order, requestCore);

            LOG.info("requestCore --> " + new Gson().toJson(requestCore));

            CreateOrderResponse<CreatedOrder> createOrderResponse = createOrderViaBackend3V2(requestCore, order, orderJSON, TraceUtil.getXCloudTraceId(httpServletRequest), idCustomerWebSafe);

            orderJSON = createOrderResponse.getData();
            LOG.info("response backend3 --> " + orderJSON.toString());
            if (Objects.nonNull(createOrderResponse.getCode()) && createOrderResponse.getCode().equals("Created")) {
                if (orderJSON.getId() == 0 && PaymentTypeEnum.TRANSACCIONES_EN_LINEA.getId() == order.getPaymentType().getId()) {
                    return new CreateOrderResponse<>(HttpStatusCode.OK.getCode(), HttpStatusCode.OK.getStatusName(), Objects.nonNull(algoliaMessageConfigCreateOrder.getErrorPaymentOnline()) && isActiveMessagesAlgolia ? algoliaMessageConfigCreateOrder.getErrorPaymentOnline() : Constants.ERROR_PAYMENT_ONLINE, null);
                }

                if (orderJSON.getId() <= 0) {
                    return new CreateOrderResponse<>(HttpStatusCode.CONFLICT.getCode(), HttpStatusCode.CONFLICT.getStatusName(), Objects.nonNull(algoliaMessageConfigCreateOrder.getErrorCreatedOrderOms()) && isActiveMessagesAlgolia ? algoliaMessageConfigCreateOrder.getErrorCreatedOrderOms() : Constants.ERROR_CREATE_ORDER_OMS, null);
                }
//            LOG.warning("New Order ->  " + orderRequest);
//            LOG.warning("Result New Order ->  " + orderJSON);

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
                orderJSON.setItems(itemOrders);

                List<ProviderOrder> providers = new ArrayList<>();
                ProviderOrder provider = new ProviderOrder();
                provider.setName("");
                provider.setEmail("");
                provider.setDeliveryPrice(0);
                provider.setItems(itemOrders);
                providers.add(provider);
                orderJSON.setProviders(providers);

                LOG.info(String.valueOf(orderJSON.getId()));
                DeliveryOrder deliveryOrder = ofy().load().type(DeliveryOrder.class).filter("currentStatus", 1).ancestor(Ref.create(customerKey)).first().now();
                if (deliveryOrder == null)
                    return new CreateOrderResponse<>(HttpStatusCode.CONFLICT.getCode(), HttpStatusCode.CONFLICT.getStatusName(), Objects.nonNull(algoliaMessageConfigCreateOrder.getDeliveryOrderNotFound()) && isActiveMessagesAlgolia ? algoliaMessageConfigCreateOrder.getDeliveryOrderNotFound() : Constants.DELIVERY_ORDER_NOT_FOUND, null);
                deliveryOrder.setIdOrder(orderJSON.getId());
                deliveryOrder.setAddress(orderJSON.getAddress());
                deliveryOrder.setIdAddress(order.getIdAddress());
                deliveryOrder.setAddressDetails(order.getAddressDetails());
                deliveryOrder.setPaymentType(order.getPaymentType());

                if (order.getCustomerIdCallCenter() != null) {
                    deliveryOrder.setSource(Constants.SOURCE_CALL_CENTER);
                } else {
                    deliveryOrder.setSource(order.getSource());
                }

                if (!PaymentTypeEnum.PSE.getId().equals(order.getPaymentType().getId())) {
                    deliveryOrder.setCurrentStatus(0);
                }
                deliveryOrder.setIdFarmatodo(order.getIdFarmatodo());
                deliveryOrder.setLastStatus(ORDER_CREATED);
                deliveryOrder.setActive(true);

                deliveryOrder.setDeliveryType(order.getDeliveryType());

                if (Objects.nonNull(orderJSON.getQrCode())) {
                    deliveryOrder.setQrCode(orderJSON.getQrCode());
                }
                //Consultar el resumen de la orden creada, para obtener valor de domicilio
                try {
//                LOG.info("method getOrderSumary: -> orderID:" + orderJSON.getId());
                    GetOrderSumary orderSumary = ApiGatewayService.get().getOrderSumary(orderJSON.getId());
                    if (orderSumary != null && orderSumary.getData() != null) {
//                    LOG.info("orderJSON.getDeliveryPrice() -> " + orderSumary.getData().getDeliveryValue());
                        deliveryOrder.setDeliveryPrice(orderSumary.getData().getDeliveryValue());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    LOG.severe("Error al consulatar resumen de orden! " + e.getMessage());
                }

                // if ScanAndGO
                if (isOrdenScanAndGo(order)) {
                    if (Objects.nonNull(order.getPaymentType()) && PaymentTypeEnum.DATAFONOS.getId().equals(order.getPaymentType().getId())) {
                        deliveryOrder.setLastStatus(ORDER_CREATED);

                    } else {
                        deliveryOrder.setLastStatus(ORDER_DELIVERED);
                        deliveryOrder.setActive(false);
                    }
                }

                if (isPrime && deliveryOrder.getDeliveryType().equals(DeliveryType.SCANANDGO)) {
                    deliveryOrder.setLastStatus(ORDER_DELIVERED);
                    deliveryOrder.setActive(false);
                }

                Timestamp stamp = new Timestamp(orderJSON.getCreateDate());
                Date date = new Date(stamp.getTime());
                deliveryOrder.setCreateDate(date);
                //if (orderJSON.getUpdateShopping()) {
                //    String getOrderURL = URLConnections.URL_ORDER + "/" + orderJSON.getId();
                //    DeliveryOrder processedOrder = CoreConnection.getRequest(getOrderURL, DeliveryOrder.class);
                //    deliveryOrder.updateShopping(processedOrder);
                //}
                if (order.getPickingDate() != null) {
                    deliveryOrder.setPickingDate(order.getPickingDate());
                    //LOG.warning("Picking Date to DS -> " + deliveryOrder.toString());
                }

                //Fix para borrar proveedor si no tiene items
                List<DeliveryOrderProvider> deleteProviders = new ArrayList<>();
//            LOG.info("tamaño proveedores : " + deliveryOrderProvidersList.size() + " list " + deliveryOrderProvidersList);
                for (DeliveryOrderProvider oderProvider : deliveryOrderProvidersList) {
//                LOG.info("key " + oderProvider.getIdDeliveryOrder() + " list " + oderProvider.getItemList());
                    if (oderProvider.getItemList() == null || oderProvider.getItemList().isEmpty()) {
                        deleteProviders.add(oderProvider);
                    }
                }

                ofy().delete().entities(deleteProviders);
//            LOG.info("savin pre save delivery prime {}"+ saving);
                deliveryOrder.setSavingPrime(saving);

                if (order.getDeliveryType().equals(DeliveryType.PROVIDER)) {
                    deliveryOrder.setDeliveryType(DeliveryType.PROVIDER);
                }

                Key<DeliveryOrder> deliveryOrderKey = ofy().save().entity(deliveryOrder).now();

                if (Objects.nonNull(itemsNewOrder) && !itemsNewOrder.isEmpty()) {
                    try {
                        // Crea una orden con lo items sobrantes
//                    LOG.info("Crea una nueva orden para los items ->  " + itemsNewOrder.size());
                        createDeliveryOrder(customerKey, deliveryOrder.getDeliveryType().getDeliveryType(), itemsNewOrder, deliveryOrderProvidersList);
                    } catch (Exception ex) {
                        LOG.warning("No fue posible crear el nuevo Carrito con los items no utilizados " + ex.getMessage());
                    }
                }

                // Save tracing state
                if (Objects.nonNull(orderJSON.getTracing()) && !orderJSON.getTracing().isEmpty()) {
                    CreatedOrder finalOrderJSON = orderJSON;
                    DeliveryOrder finalOrder3 = order;
                    orderJSON.getTracing().forEach(tracing -> {
                        tracing.setIdTracing(UUID.randomUUID().toString());
                        tracing.setDeliveryOrderId(Ref.create(deliveryOrderKey));
                        if (tracing.getStatus() == 12) {
                            int responseSms = 0;
                            try {
                        /*responseSms = supportMethods.sendSms(order.getCustomerPhone(),
                                "Bienvenido a farmatodo. La transaccion asociada a tu orden "
                                        + finalOrderJSON.getId() + " fue aprobada");
*/
                                final SendSMSCloudFunctionReq request = new SendSMSCloudFunctionReq(finalOrder3.getCustomerPhone(),
                                        "Bienvenido a farmatodo. La transaccion asociada a tu orden "
                                                + finalOrderJSON.getId() + " fue aprobada");
                                CloudFunctionsService.get().postSendSms(request);
                            } catch (IOException e) {
                                LOG.warning("Error");
                            }
                            //LOG.warning("Response sms " + responseSms);
                        }
                    });
                    Tracing tracing = orderJSON.getTracing().get(0);
                    ofy().save().entity(tracing);
                }

                addMarcaCategorySubcategorieAndItemUrl(orderJSON);

                if (requestCore.getSelfCheckout() != null && requestCore.getSelfCheckout().getIsSelfCheckout() && requestCore.getSelfCheckout().getIdBox() != null) {
                    SelfCheckoutAlgolia selfCheckoutAlgolia = APIAlgolia.getSelfCheckout();
                    if (!Objects.isNull(selfCheckoutAlgolia) && !Objects.isNull(selfCheckoutAlgolia.getCustomerByStoresAndBox())) {
                        Optional<SelfCheckoutListAlgolia> selfCheckoutListAlgolia = selfCheckoutAlgolia.getCustomerByStoresAndBox().stream()
                                .filter(checkoutListAlgolia -> Objects.equals(checkoutListAlgolia.getStore(), requestCore.getStoreId()) && Objects.equals(checkoutListAlgolia.getBox(), requestCore.getSelfCheckout().getIdBox()))
                                .limit(1).findFirst();
                        if (selfCheckoutListAlgolia.isPresent()) {
                            deleteCoupon(customerKey);
                        }
                    }
                }
                DeliveryOrder deliveryOrderData = getOrderMethod(deliveryOrder, orderJSON.getId(), false, false);

                orderJSON.setOrderData(deliveryOrderData);
                ApiGatewayService.get().updateStratumBraze(requestCore.getCustomerId().toString());


//            LOG.info("saving order"+saving);
                if (saving > 0) {
                    SavingCustomer requestSaving = getSavingCustomerRequest((long) order.getIdFarmatodo(), saving, UpdateTypeSavingEnum.ADD);
                    try {
                        ApiGatewayService.get().sendSavingNoPrime(requestSaving);
                    } catch (Exception e) {
                        LOG.warning("Error al guardar el saving");
                    }
                }
                if ((isPrime || isPrimeMixed)) {
                    orderJSON.setPrimeGeneral(true);
                    if (!isPSE) {
                        orderJSON.setPrime(true);
                        orderJSON.setPrimeGeneral(false);
                    }
                } else {
                    orderJSON.setPrimeGeneral(false);
                    orderJSON.setPrime(false);
                }

                try {
                    Long sessionId = Long.parseLong(httpServletRequest.getHeader("amplitudeSessionId"));
                    if (sessionId != null) {
                        AmplitudeSessionRequest request = new AmplitudeSessionRequest();
                        request.setOrderId(orderJSON.getId());
                        request.setSessionId(sessionId);
//                LOG.info("request -> " + request.toString());
                        ApiGatewayService.get().saveAmplitudeSessionId(request);
                    }
                } catch (Exception e) {
                    LOG.warning(e.getMessage());
                }


                addRMSclasses(orderJSON);
                sendEventCreate(orderJSON, requestCore.getCustomerId(), order.getPaymentType().getId());
                LOG.info("Customer ID # " + requestCore.getCustomerId() + ", createOrderResponse -> " + createOrderResponse);
                createOrderResponse.setData(orderJSON);
                LOG.info("createOrderResponse -> " + createOrderResponse.toString());
                return createOrderResponse;

            } else {
                DeliveryOrder deliveryOrderOrder = ofy().load().type(DeliveryOrder.class).filter("currentStatus", 0).ancestor(Ref.create(customerKey)).first().now();
                deleteCoupon(customerKey);
                if (deliveryOrderOrder != null) {
                    ofy().delete().entity(deliveryOrderOrder).now();
                }
                LOG.info("createOrderResponse no Created-> " + createOrderResponse.toString());
                return createOrderResponse;
            }
        } catch (Exception e) {
            LOG.severe("Ocurrio un error al crear la orden => " + e.getMessage());
            return new CreateOrderResponse<>(HttpStatusCode.INTERNAL_SERVER_ERROR.getCode(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusName(), Constants.ERROR_CREATE_ORDER_OMS, null);
        }
    }

    private static void validateSetOptimalRoutePopInformation(DeliveryOrder order, CreateOrderRequestCore requestCore) {
        if (Objects.nonNull(order.getIdOptimalRoute()) && Objects.nonNull(order.getOptionSelectedPopUp())) {
            requestCore.setIdOptimalRoute(order.getIdOptimalRoute());
            requestCore.setOptionSelectedPopUp(order.getOptionSelectedPopUp().getOptionSelectPopUp());
        }
    }

    private DeliveryOrder deleteItemsPrime(String idCustomerWebSafe, String token, final String tokenIdWebSafe, DeliveryOrder order) {
        try {
            order.getItemList().forEach(item -> {

                if (primeUtil.isItemPrime(item.getId())) {
                    final String itemSTR = String.valueOf(item.getId());
                    try {
                        deleteDeliveryOrderItem(
                                token,
                                tokenIdWebSafe,
                                idCustomerWebSafe,
                                26,
                                Integer.valueOf(itemSTR),
                                ""
                        );
                        order.getItemList().removeIf(itemCart -> (itemCart.getId() == item.getId()));
                    } catch (ConflictException e) {
                        throw new RuntimeException(e);
                    } catch (BadRequestException e) {
                        throw new RuntimeException(e);
                    } catch (AlgoliaException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } catch (Exception e) {
            LOG.warning("Ocurrio un error eliminando items para dejar solo la suscripción");
        }
        return order;
    }


    private String getOrderType(final DeliveryType deliveryType, final boolean hasProviderItems, final boolean hasItems) {
        if (hasProviderItems && hasItems) {
            return "MIXED";
        } else if (hasProviderItems && !hasItems) {
            return "PROVIDER";
        } else {
            return Objects.nonNull(deliveryType) ? deliveryType.name() : DeliveryType.EXPRESS.name();
        }
    }

    private OrderConfigEnum getCreateOrderConfig(String deliveryType) {
        CreateOrderConfig createOrderConfig = APIAlgolia.getCreateOrderConfig(deliveryType);
//        LOG.info("method: getCreateOrderConfig - isCreateOrderInCoreWhenFailed " + (Objects.nonNull(createOrderConfig) ? createOrderConfig.isCreateOrderInCoreWhenFailed() : "--false"));
//        LOG.info("method: getCreateOrderConfig - isCreateOrderInCore " + (Objects.nonNull(createOrderConfig) ? createOrderConfig.isCreateOrderInCore() : "--false"));
        if (Objects.nonNull(createOrderConfig)) {
            boolean createOrderInCoreWhenFailed = createOrderConfig.isCreateOrderInCoreWhenFailed();
            boolean createOrderInCore = createOrderConfig.isCreateOrderInCore();


            if (createOrderInCoreWhenFailed && !createOrderInCore) {
                // go to core only fail backend 3
                return OrderConfigEnum.BACKEND3_FAIL_USE_CORE;

            } else if (!createOrderInCoreWhenFailed && createOrderInCore) {
                // all orders via core
                return OrderConfigEnum.CORE;

            } else if (!createOrderInCoreWhenFailed) {
                // all orders via backend 3
                return OrderConfigEnum.BACKEND3;
            } else {
                // go to core only fail backend 3
                return OrderConfigEnum.BACKEND3_FAIL_USE_CORE;
            }

        }

        return OrderConfigEnum.BACKEND3_FAIL_USE_CORE;

    }

    private boolean validateProvidersOneItem(CreateOrderRequestCore requestCore){
        if(Objects.isNull(requestCore.getProviders()) || requestCore.getProviders().isEmpty()){
            return false;
        }

        int totalItems = requestCore.getProviders().stream()
                .filter(provider -> provider.getItems() != null)
                .mapToInt(provider -> provider.getItems().size())
                .sum();

        return totalItems == SINGLE_ITEM;
    }

    private boolean validateOneItem(CreateOrderRequestCore requestCore){
        if(Objects.isNull(requestCore.getItems()) || requestCore.getItems().isEmpty()){
            return false;
        }

        return requestCore.getItems().size() == SINGLE_ITEM;
    }

    private int getItemId(CreateOrderRequestCore requestCore){
        if(validateOneItem(requestCore)){
            if(requestCore.getItems().get(FIRST_ITEM_INDEX).getItemId() != null){
                return requestCore.getItems().get(FIRST_ITEM_INDEX).getItemId();
            }
            return DEFAULT_ITEM_ID;
        }

        List<CreateOrderRequestCore.OrderDetailRequest> items = requestCore.getProviders().stream()
                .filter(provider -> provider.getItems() != null && !provider.getItems().isEmpty())
                .map(provider -> provider.getItems().get(FIRST_ITEM_INDEX))
                .collect(Collectors.toList());

        if(!items.isEmpty() && Objects.nonNull(items.get(FIRST_ITEM_INDEX).getItemId())){
            return items.get(FIRST_ITEM_INDEX).getItemId();
        }
        return DEFAULT_ITEM_ID;
    }

    private boolean containsOnlyTipItem(CreateOrderRequestCore requestCore){
        try {

            if(Objects.isNull(requestCore)){
                return false;
            }

            //solo continuamos si solo una de las listas contiene un unico item.
            if (validateProvidersOneItem(requestCore) == validateOneItem(requestCore)) {
                return false;
            }

            Optional<TipConfig> tipConfigOptional = GrowthBookConfigLoader.getTipsConfig(Constants.CUSTOMER_ANONYMOUS);
            if(!tipConfigOptional.isPresent()){
                return false;
            }

            List<ItemTip> itemTipList = tipConfigOptional.get().getItemTips();
            if(Objects.isNull(itemTipList)){
                return false;
            }

            int itemId = getItemId(requestCore);

            //si el unico item que llega es de propina retorna true.
            return itemTipList.stream().anyMatch(itemTip -> itemTip != null && itemTip.getItemId() != null && itemTip.getItemId() == itemId);

        } catch (Exception e) {
            return false;
        }
    }

    private boolean createOrderViaBackend3(CreateOrderRequestCore requestCore, DeliveryOrder order, CreatedOrder orderJSON, String traceId, String idCustomerWebSafe) throws AlgoliaException {
        Response<CreateOrderResponseBackend3> responseBck3;
        try {
            requestCore.setIdCustomerWebSafe(idCustomerWebSafe);
//            LOG.info("method createOrderViaBackend3: -> customerId" + requestCore.getCustomerId());
            requestCore.getItems().removeIf(items -> Objects.equals(items.getItemId(), 1053709));

            if(containsOnlyTipItem(requestCore)){
                return false;
            }

            responseBck3 = ApiGatewayService.get().createOrderBck3(requestCore, traceId);
            // LOG.info("Respuesta Orden: "+responseBck3.body().toString());
//            LOG.info("IF (!responseBck3.isSuccessful()) : [" + (!responseBck3.isSuccessful()) + "]");
            if (!responseBck3.isSuccessful()) {
                //LOG.warning("retrying call OMS-failOver");
                responseBck3 = ApiGatewayService.get().createOrderBck3V2(requestCore, traceId);
            }

//            LOG.info("IF (!responseBck3.isSuccessful()) : [" + (!responseBck3.isSuccessful()) + "]");
            if (!responseBck3.isSuccessful()) {
                String error = (responseBck3.errorBody() != null ? responseBck3.errorBody().string() : "code : " + responseBck3.code());
                String body = responseBck3.errorBody().string();
                LOG.info("Alerta!! no se pudo crear la orden -> " + error);
                AlertConfigMessage alertConfigMessage = APIAlgolia.getAlertMessage();
                alertConfigMessage.getPhoneNumbers().forEach(number ->
                        Util.sendAlertCreateOrder(number,
                                "BK3 - Usuario : " + order.getIdFarmatodo() + alertConfigMessage.getMessage() + " " + error));
                return false;

            } else {
                // success!
                CreateOrderResponseBackend3 createOrderResponseBackend3 = responseBck3.body();
                if (createOrderResponseBackend3 != null && createOrderResponseBackend3.getData() != null && createOrderResponseBackend3.getData().dataIsValid()) {
                    orderJSON.setId(createOrderResponseBackend3.getData().getId());
                    orderJSON.setCreateDate((createOrderResponseBackend3.getData().getCreateDate() == null) ? 0 : createOrderResponseBackend3.getData().getCreateDate());
                    orderJSON.setAddress(createOrderResponseBackend3.getData().getAddress());
                    orderJSON.setUpdateShopping(createOrderResponseBackend3.getData().getUpdateShopping());
                    orderJSON.setChangePaymentCreditCard(createOrderResponseBackend3.getData().getChangePaymentCreditCard());
                    if (PaymentTypeEnum.PSE.getId() == order.getPaymentType().getId()) {
                        orderJSON.setTransactionDetails(createOrderResponseBackend3.getData().getTransactionDetails());

                    }
                    if (Objects.nonNull(createOrderResponseBackend3.getData().getQrCode()))
                        orderJSON.setQrCode(createOrderResponseBackend3.getData().getQrCode());
                    return true;
                }
            }

        } catch (ServiceUnavailableException | UnauthorizedException | NotFoundException |
                 InternalServerErrorException | IOException | BadRequestException | ConflictException |
                 AlgoliaException e) {
            e.printStackTrace();
            LOG.severe("Error al crear la orden! " + e.getMessage());
            AlertConfigMessage alertConfigMessage = APIAlgolia.getAlertMessage();
            alertConfigMessage.getPhoneNumbers().forEach(number ->
                    Util.sendAlertCreateOrder(number,
                            "Usuario : " + order.getIdFarmatodo() + alertConfigMessage.getMessage() + " " + e.getMessage()));
        }

        return false;

    }
    private CreateOrderResponse<CreatedOrder> createOrderViaBackend3V2(CreateOrderRequestCore requestCore, DeliveryOrder order, CreatedOrder orderJSON, String traceId, String idCustomerWebSafe) throws AlgoliaException, ConflictException, InternalServerErrorException {
        LOG.info("Sent order to back 3 -> "+ new Gson().toJson(requestCore));
        Gson gson = new GsonBuilder().create();
        BadRequestResponseOms badRequestResponseOms;

        // mensajes parametrizados de error
        boolean isActiveMessagesAlgolia;
        String errorCreateOrderOms = Constants.ERROR_CREATE_ORDER_OMS;
        String paymentDeclinedCreateOrder = Constants.PAYMENT_DECLINED_CREATE_ORDER;
        String messageErrorTipOnly = Constants.ERROR_CREATE_ORDER_TIP_ONLY;

        AlgoliaMessageConfigCreateOrder algoliaMessageConfigCreateOrder = APIAlgolia.getMessagesCreateOrder();
        if (Objects.nonNull(algoliaMessageConfigCreateOrder)) {
            isActiveMessagesAlgolia = Objects.nonNull(algoliaMessageConfigCreateOrder.getActive()) && algoliaMessageConfigCreateOrder.getActive();
            if (isActiveMessagesAlgolia) {
                errorCreateOrderOms = algoliaMessageConfigCreateOrder.getErrorCreatedOrderOms();
                paymentDeclinedCreateOrder = algoliaMessageConfigCreateOrder.getPaymentDeclinedCreateOrder();
            }
        }

        try {
            requestCore.setIdCustomerWebSafe(idCustomerWebSafe);
            requestCore.getItems().removeIf(items -> Objects.equals(items.getItemId(), 1053709));
            if(Objects.nonNull(order.getDeliveryHome()) && !order.getDeliveryHome().isEmpty()){
                requestCore.setDeliveryHome(order.getDeliveryHome());
            }

            if(containsOnlyTipItem(requestCore)){
                LOG.severe("Alerta!! no se pudo crear la orden -> " + messageErrorTipOnly);
                CreateOrderResponse<CreatedOrder> createOrderResponse = new CreateOrderResponse<>();
                createOrderResponse.setStatusCode(HttpStatusCode.CONFLICT.getCode());
                createOrderResponse.setCode(HttpStatusCode.CONFLICT.getStatusName());
                createOrderResponse.setMessage(messageErrorTipOnly);
                return createOrderResponse;
            }

            CircuitBreaker circuitBreaker = ResilienceManager.getCircuitBreaker(ResilienceManager.CREATE_ORDER_SERVICE);
            Retry retry = ResilienceManager.getRetry(ResilienceManager.CREATE_ORDER_SERVICE);

            // Función para llamar al servicio principal
            Supplier<Response<CreateOrderResponseBackend3>> primaryServiceCall = CircuitBreaker.decorateSupplier(
                    circuitBreaker,
                    () -> {
                        try {
                            LOG.info("Intentando llamar al servicio principal (OMS)");
                            return ApiGatewayService.get().createOrderBck3(requestCore, traceId);
                        } catch (Exception e) {
                            LOG.severe("Error en llamada al servicio principal: " + e.getMessage());
                            throw new RuntimeException(e);
                        }
                    }
            );

            // Función para llamar al servicio de respaldo
            Supplier<Response<CreateOrderResponseBackend3>> fallbackServiceCall = () -> {
                try {
                    LOG.info("Intentando llamar al servicio de respaldo (OMS-Direct)");
                    return ApiGatewayService.get().createOrderBck3V2(requestCore, traceId);
                } catch (Exception e) {
                    LOG.severe("Error en llamada al servicio de respaldo: " + e.getMessage());
                    throw new RuntimeException(e);
                }
            };

            // Aplicar retry al servicio principal
            Supplier<Response<CreateOrderResponseBackend3>> retryingServiceCall = Retry.decorateSupplier(
                    retry,
                    primaryServiceCall
            );

            // Ejecutar con fallback
            Response<CreateOrderResponseBackend3> responseBck3;
            try {
                responseBck3 = Try.ofSupplier(retryingServiceCall)
                        .recover(throwable -> {
                            LOG.warning("Servicio principal falló después de reintentos, usando fallback: " + throwable.getMessage());
                            return fallbackServiceCall.get();
                        })
                        .get();
            } catch (Exception e) {
                LOG.severe("Ambos servicios (principal y fallback) fallaron: " + e.getMessage());
                CreateOrderResponse<CreatedOrder> createOrderResponse = new CreateOrderResponse<>();
                sendErrorAlert(order, "Ambos servicios (principal y fallback) fallaron: " + e.getMessage(), false);
                return buildErrorResponse(createOrderResponse, HttpStatusCode.INTERNAL_SERVER_ERROR,
                        e.getMessage(), errorCreateOrderOms);
            }

            CreateOrderResponseBackend3 createOrderResponseBackend3 = responseBck3.body();
            CreateOrderResponse<CreatedOrder> createOrderResponse = new CreateOrderResponse<>();
            if (!responseBck3.isSuccessful()) {
                badRequestResponseOms = gson.fromJson(responseBck3.errorBody().string(), BadRequestResponseOms.class);
                String errorMessage = badRequestResponseOms.getMessage();
                LOG.severe("Alerta!! no se pudo crear la orden -> " + errorMessage);

                switch (responseBck3.code()) {
                    case 503:
                        sendErrorAlert(order, errorMessage, true);
                        return buildErrorResponse(createOrderResponse, HttpStatusCode.SERVICE_UNAVAILABLE,
                                errorMessage, paymentDeclinedCreateOrder
                        );

                    case 400:
                        return buildErrorResponse(createOrderResponse, HttpStatusCode.BAD_REQUEST,
                                errorMessage, errorCreateOrderOms
                        );

                    case 409:
                        return buildErrorResponse(createOrderResponse, HttpStatusCode.CONFLICT,
                                errorMessage, errorCreateOrderOms
                        );

                    case 500:
                    default:
                        sendErrorAlert(order, errorMessage, false);
                        return buildErrorResponse(createOrderResponse, HttpStatusCode.INTERNAL_SERVER_ERROR,
                                errorMessage, errorCreateOrderOms
                        );
                }
            } else {
                if (responseBck3.code() == 201) {
                    // success!
                    if (createOrderResponseBackend3 != null && createOrderResponseBackend3.getData() != null && createOrderResponseBackend3.getData().dataIsValid()) {
                        orderJSON.setId(createOrderResponseBackend3.getData().getId());
                        orderJSON.setCreateDate((createOrderResponseBackend3.getData().getCreateDate() == null) ? 0 : createOrderResponseBackend3.getData().getCreateDate());
                        orderJSON.setAddress(createOrderResponseBackend3.getData().getAddress());
                        orderJSON.setUpdateShopping(createOrderResponseBackend3.getData().getUpdateShopping());
                        orderJSON.setChangePaymentCreditCard(createOrderResponseBackend3.getData().getChangePaymentCreditCard());
                        if (PaymentTypeEnum.PSE.getId() == order.getPaymentType().getId()) {
                            orderJSON.setTransactionDetails(createOrderResponseBackend3.getData().getTransactionDetails());
                        }
                        if (Objects.nonNull(createOrderResponseBackend3.getData().getQrCode()))
                            orderJSON.setQrCode(createOrderResponseBackend3.getData().getQrCode());
                        createOrderResponse.setStatusCode(HttpStatusCode.CREATED.getCode());
                        createOrderResponse.setCode(HttpStatusCode.CREATED.getStatusName());
                        createOrderResponse.setData(orderJSON);
                        createOrderResponse.setMessage(createOrderResponseBackend3.getMessage());
                        return createOrderResponse;
                    }
                }
                if (responseBck3.code() == 200) {
                    createOrderResponse.setStatusCode(HttpStatusCode.OK.getCode());
                    createOrderResponse.setCode(HttpStatusCode.OK.getStatusName());
                    createOrderResponse.setMessage(Objects.nonNull(createOrderResponseBackend3) && Objects.nonNull(createOrderResponseBackend3.getMessage()) ? createOrderResponseBackend3.getMessage() : Constants.PAYMENT_DECLINED_CREATE_ORDER);
                    return createOrderResponse;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            LOG.severe("Error al crear la orden! " + e.getMessage());
            AlertConfigMessage alertConfigMessage = APIAlgolia.getAlertMessage();
            alertConfigMessage.getPhoneNumbers().forEach(number ->
                    Util.sendAlertCreateOrder(number,
                            "Usuario : " + order.getIdFarmatodo() + alertConfigMessage.getMessage() + " " + e.getMessage()));
            return new CreateOrderResponse<>(HttpStatusCode.INTERNAL_SERVER_ERROR.getCode(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusName(), errorCreateOrderOms, null);
        }
        return null;
    }

    private void sendErrorAlert(DeliveryOrder order, String errorMessage, boolean isPaymentError) throws AlgoliaException {
        try {
            String date = DateConstants.SIMPLE_DATE_FORMAT.format(new Date());
            AlertConfigMessage alertConfig = isPaymentError
                    ? APIAlgolia.getAlertMessageErrorPayu()
                    : APIAlgolia.getAlertMessageErrorOms();

            String paymentMethod = getPaymentMethodDescription(order.getPaymentType());

            String alertMessage = String.format("BK3 - Usuario: %d%s%s\nMétodo de pago: %s\nFecha: %s",
                    order.getIdFarmatodo(),
                    isPaymentError ? alertConfig.getMessage() : " - " + alertConfig.getMessage() + ": " + errorMessage,
                    isPaymentError ? "\nMás detalle del error: " + errorMessage : "",
                    paymentMethod,
                    date);

            alertConfig.getPhoneNumbers().forEach(number -> Util.sendAlertCreateOrder(number, alertMessage));

            List<String> listNumberVen = OrderUtil.filterNumbersStartingWith58(alertConfig.getPhoneNumbers());
            listNumberVen.forEach(numberVen -> Util.sendAlertCreateOrderVen(numberVen, alertMessage));
        } catch (Exception e) {
            LOG.severe("Error al enviar alerta SMS de error en la creación de la orden! " + e.getMessage());
        }
    }

    private String getPaymentMethodDescription(PaymentType paymentType) {
        if (paymentType != null && paymentType.getId() != 0L) {
            return PaymentTypeEnum.find(paymentType.getId()).name();
        }
        return "No se encontró método de pago";
    }

    private CreateOrderResponse<CreatedOrder> buildErrorResponse(CreateOrderResponse<CreatedOrder> createOrderResponse,
                                                                 HttpStatusCode statusCode,
                                                                 String errorMessage,
                                                                 String defaultMessage
    ) {
        createOrderResponse.setStatusCode(statusCode.getCode());
        createOrderResponse.setCode(statusCode.getStatusName());
        createOrderResponse.setMessage(Objects.nonNull(errorMessage) ? errorMessage : defaultMessage);
        return createOrderResponse;
    }


    @ApiMethod(name = "addDeliveryOrder", path = "/orderEndpoint/addDeliveryOrder", httpMethod = ApiMethod.HttpMethod.POST)
    public Answer addDeliveryOrder(@Named("token") final String token,
                                   @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                   @Named("idCustomerWebSafe") final String idCustomerWebSafe,
                                   @Named("idStoreGroup") final int idStoreGroupFromRequest,
                                   final DeliveryOrderJson deliveryOrderJson,
                                   @Named("deliveryType") final String deliveryType,
                                   @Nullable @Named("origin") final String origin,
                                   @Nullable @Named("filters") final String filters)
            throws ConflictException, BadRequestException, IOException, InternalServerErrorException, AlgoliaException, NotFoundException {

        if (!authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);

        int idStoreGroup = APIAlgolia.getDefaultStoreIdByStoreId(idStoreGroupFromRequest);


        int contador = 0, contadorTip = 0;

        for (ItemOnShop itemOnShop : deliveryOrderJson.getItems()) {
            //LOG.warning(String.valueOf(itemOnShop.getId()));
            contador++;
            if (!this.isTip(itemOnShop.getId())) {
                this.addDeliveryOrderItem(token, tokenIdWebSafe, idCustomerWebSafe, (int) itemOnShop.getId(), (int) itemOnShop.getQuantitySold(), idStoreGroup, false, deliveryType, origin, itemOnShop.getObservations(), false, null, filters, null, null);
                //LOG.warning("Agrega item normal");
            } else {
                LOG.warning("No deja agregar propina");
                contadorTip++;
            }
        }
        if (contadorTip > 0 && contadorTip == contador)
            throw new BadRequestException("No se pueden agregar propinas");

        Answer answer = new Answer();
        answer.setConfirmation(true);
        Key<User> userKey = Key.create(idCustomerWebSafe);
        User user = users.findUserByKey(userKey);
        deleteCouponTalonOne(user.getId(), idCustomerWebSafe);
        deleteCacheDeductDiscount(idCustomerWebSafe,String.valueOf(user.getId()));
        return answer;
    }

    private boolean isTip(long id) {
        //revision de items de propina
        Optional<TipConfig> tipConfigOptional = GrowthBookConfigLoader.getTipsConfig(Constants.CUSTOMER_ANONYMOUS);

        return tipConfigOptional.map(tipConfig ->
                !Objects.isNull(tipConfig.getItemTips()) && tipConfig.getItemTips().stream().anyMatch(itemTip ->
                        !Objects.isNull(itemTip) && !Objects.isNull(itemTip.getItemId()) && itemTip.getItemId() == id
                )
        ).orElse(false);

        // -----------fin de revision de items de propina
    }

    private GenericResponse parseError(String errorBodyy) {
        Gson gson = new Gson();
        return gson.fromJson(errorBodyy, GenericResponse.class);
    }

    @ApiMethod(name = "addDeliveryOrderItemAsync", path = "/orderEndpoint/addDeliveryOrderItemAsync", httpMethod = ApiMethod.HttpMethod.POST)
    public GenericResponse addDeliveryOrderItemAsync(@Named("token") final String token,
                                                     @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                                     @Named("idCustomerWebSafe") final String idCustomerWebSafe,
                                                     @Nullable @Named("id") final Integer id,
                                                     @Named("quantity") Integer quantity,
                                                     @Named("idStoreGroup") final int idStoreGroupFromRequest,
                                                     @Nullable @Named("isInShoppingCart") Boolean isInShoppingCart,
                                                     @Nullable @Named("deliveryType") final String deliveryType,
                                                     @Nullable @Named("origin") final String origin,
                                                     @Nullable @Named("observations") final String observations,
                                                     @Nullable @Named("isSubstitute") final Boolean isSubstitute,
                                                     @Nullable @Named("itemBarcode") final String itemBarcode,
                                                     @Nullable @Named("filters") final String filters, HttpServletRequest request) throws AlgoliaException, ConflictException, BadRequestException {


//        LOG.info(request.getPathInfo());
//        LOG.info(request.getRequestURI());
//        LOG.info(request.getRequestURL().toString());
//        LOG.info(request.getQueryString());
        try {
            ApiGatewayService.get().addDeliveryOrderItemAsync(request.getQueryString());
            Thread.sleep(400);
        } catch (Exception e) {
            throw new ConflictException("Error General" + e);
        }
        GenericResponse responseV2 = new GenericResponse();
        responseV2.setMessage(Constants.SUCCESS);
        responseV2.setCode(Constants.CODE_SUCCESS);
        responseV2.setStatus(200);
        return responseV2;

    }

    @ApiMethod(name = "addDeliveryOrderItem", path = "/orderEndpoint/addDeliveryOrderItem", httpMethod = ApiMethod.HttpMethod.POST)
    public DeliveryOrderItem addDeliveryOrderItem(
                                                    @Named("token") final String token,
                                                    @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                                    @Named("idCustomerWebSafe") final String idCustomerWebSafe,
                                                    @Nullable @Named("id") final Integer id,
                                                    @Named("quantity") Integer quantity,
                                                    @Named("idStoreGroup") final int idStoreGroupFromRequest,
                                                    @Nullable @Named("isInShoppingCart") Boolean isInShoppingCart,
                                                    @Nullable @Named("deliveryType") final String deliveryType,
                                                    @Nullable @Named("origin") final String origin,
                                                    @Nullable @Named("observations") final String observations,
                                                    @Nullable @Named("isSubstitute") final Boolean isSubstitute,
                                                    @Nullable @Named("itemBarcode") final String itemBarcode,
                                                    @Nullable @Named("filters") final String filters,
                                                    final AddDeliveryOrderItemRequest addDeliveryOrderItemRequest,
                                                    HttpServletRequest request)
                                                    throws ConflictException, BadRequestException, AlgoliaException, NotFoundException {

        LOG.info("Step 1: Inicio de addDeliveryOrderItem - " + new DateTime());

        // Validación del token y cantidad
        if (!authenticate.isValidToken(token, tokenIdWebSafe)) {
            throw new ConflictException(Constants.INVALID_TOKEN);
        }
        if (quantity == null || quantity < 1) {
            throw new ConflictException(Constants.INVALID_QUANTITY);
        }

        // Determinación del origen de la solicitud
        RequestSourceEnum sourceEnum = getRequestSource(request);

        // Inicialización de servicios y resolución del grupo de tienda por defecto
        OpticsServices opticsServices = new OpticsServices();
        int idStoreGroup = APIAlgolia.getDefaultStoreIdByStoreId(idStoreGroupFromRequest);
        LOG.info("Step 2: Grupo de tienda resuelto - " + new DateTime());

        // Obtención de la clave del cliente y carga o creación de la orden de entrega
        Key<Customer> customerKey = Key.create(idCustomerWebSafe);
        DeliveryOrder deliveryOrder = ofy().load().type(DeliveryOrder.class)
                .filter("currentStatus", 1)
                .ancestor(Ref.create(customerKey))
                .first()
                .now();

        if (deliveryOrder == null) {
            deliveryOrder = new DeliveryOrder();
            deliveryOrder.setIdDeliveryOrder(UUID.randomUUID().toString());
            deliveryOrder.setIdCustomer(Ref.create(customerKey));
            deliveryOrder.setCurrentStatus(1);
            deliveryOrder.setCreateDate(new Date());
            deliveryOrder.setDeliveryType(DeliveryType.valueOf(deliveryType));
            LOG.info("Step 3: Se crea nueva orden de entrega.");
        } else {
            deliveryOrder.setDeliveryType(DeliveryType.getDeliveryType(deliveryType));
            LOG.info("Step 3: Se usa la orden de entrega existente.");
        }
        Optional<TipConfig> tipConfigOptional = GrowthBookConfigLoader.getTipsConfig(Constants.CUSTOMER_ANONYMOUS);// fix delete tips

        if (tipConfigOptional.isPresent()) {
            deleteTips(tipConfigOptional.get(), deliveryOrder, Key.create(idCustomerWebSafe));
        }
        LOG.info("Step 4: Procesada configuración de tips - " + new DateTime());

        // Si el item es prime, eliminar cupón asociado
        try {
            Long parseId = Long.valueOf(id);
            if (primeUtil.isItemPrime(parseId)) {
                deleteDeliveryOrderCoupon(token, tokenIdWebSafe, idCustomerWebSafe);
            }
        } catch (Exception ignored) {
            LOG.warning("Step 5: Error al eliminar cupón - " + ignored);
        }
        LOG.info("Step 5: Proceso de cupones completado - " + new DateTime());

        // Establecer URL de prescripción si está presente en la solicitud
        if (addDeliveryOrderItemRequest != null && addDeliveryOrderItemRequest.getUrlPrescription() != null) {
            LOG.info("URL de prescripción: " + addDeliveryOrderItemRequest.getUrlPrescription());
            deliveryOrder.setUrlPrescription(addDeliveryOrderItemRequest.getUrlPrescription());
        }

        // Guardar o actualizar la orden de entrega
        ofy().save().entity(deliveryOrder).now();

        // Procesar obtención de item(s)
        Item item = null;
        List<Item> opticalItems = new ArrayList<>();
        boolean isScanAndGo = isScanAndGo(deliveryType);

        if (isScanAndGo && itemBarcode != null && !itemBarcode.isEmpty()) {
            item = productsMethods.setFindInformationToAlgoliaByBarcode(itemBarcode, Constants.DEFAULT_STORE_CO);
            if (item == null) {
                throw new ConflictException(Constants.PRODUCT_NOT_EXISTS);
            }
            // Validar stock mínimo para ScanAndGo
            if (item.getTotalStock() < MIN_VALID_SCAN_AND_GO_STOCK) {
                item.setTotalStock(DEFAULT_SCAN_AND_GO_STOCK);
            }
        } else if (id == null || id <= 0) {
            throw new ConflictException(Constants.PRODUCT_NOT_EXISTS);
        } else {
            if (isOpticalItem(addDeliveryOrderItemRequest)) {
                // Procesa items ópticos (puede devolver más de un item en la lista opticalItems)
                item = getItemOptics(id, addDeliveryOrderItemRequest, opticsServices, idStoreGroup, item, opticalItems, sourceEnum);
            } else {
                // Procesa item normal
                item = processNormalItem(id, idStoreGroup, quantity, addDeliveryOrderItemRequest, isScanAndGo);
            }
        }
        LOG.info("Step 6: Procesamiento del item completado - " + new DateTime());

        // Crear la clave para la orden de entrega
        final Key<DeliveryOrder> deliveryOrderKey = Key.create(customerKey, DeliveryOrder.class, deliveryOrder.getIdDeliveryOrder());

        // Si se han procesado items ópticos, iterar y generar la respuesta a partir de ellos
        if (opticalItems != null && !opticalItems.isEmpty()) {
            DeliveryOrderItem deliveryOrderItemResponse = null;
            for (Item opticalItem : opticalItems) {
                LOG.info("Item óptico " + opticalItem.getItem() + " solicitado con cantidad " + opticalItem.getQuantityRequest());
                Integer opticalItemQuantity = opticalItem.getQuantityRequest();
                deliveryOrderItemResponse = getDeliveryOrderItem(opticalItemQuantity, isInShoppingCart, origin,
                        observations, isSubstitute, opticalItem, isScanAndGo, deliveryOrderKey,
                        opticalItem.getDirectionItem(), true);
            }
            return deliveryOrderItemResponse;
        }
        LOG.info("Step 7: Procesamiento de item estándar - " + new DateTime());

        // Ajustar cantidad en función del stock disponible
        quantity = adjustQuantityBasedOnStock(quantity, addDeliveryOrderItemRequest, item);
        LOG.info("Step 8: Cantidad ajustada - " + new DateTime());

        return getDeliveryOrderItem(quantity, isInShoppingCart, origin, observations, isSubstitute, item,
                isScanAndGo, deliveryOrderKey, null, false);
    }

    /**
     * Determina el origen de la solicitud a partir del header.
     */
    private RequestSourceEnum getRequestSource(HttpServletRequest request) {
        RequestSourceEnum sourceEnum = null;
        if (request != null) {
            sourceEnum = ftdUtilities.getSourceFromRequestHeaderForPays(request);
        }
        // Si no se pudo determinar o es DEFAULT, se asume IOS por defecto
        return (sourceEnum == null || sourceEnum == RequestSourceEnum.DEFAULT) ? RequestSourceEnum.IOS : sourceEnum;
    }

    /**
     * Adjusts the requested quantity of an item based on the available stock only with nearby stores.
     * If the requested quantity exceeds the available stock in nearby stores, the quantity is adjusted
     *
     * @param quantity the requested quantity of the item
     * @param addDeliveryOrderItemRequest the request containing details about the delivery order, including nearby stores
     * @param item the item for which the quantity is being adjusted
     * @return the adjusted quantity of the item
     */
    private static Integer adjustQuantityBasedOnStock(Integer quantity, AddDeliveryOrderItemRequest addDeliveryOrderItemRequest, Item item) {

        if (quantity == null) {
            throw new IllegalArgumentException("Quantity cannot be null");
        }
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }

        if (quantity < 0) {
            quantity = 0;
        }

        if (item.getTotalStock() < 0) {
            throw new IllegalArgumentException("Item total stock cannot be negative");
        }

        if (item.getTotalStock() == 0) {
            return 0;
        }

        boolean containsNearbyStores = Optional.ofNullable(addDeliveryOrderItemRequest)
                .map(AddDeliveryOrderItemRequest::getNearbyStores)
                .map(stores -> !stores.isEmpty())
                .orElse(false);

        item.setQuantityRequested(quantity);

        if (shouldAdjustQuantity(containsNearbyStores, quantity, item.getTotalStock())) {
            item.setQuantitySold(item.getTotalStock());
            quantity = item.getTotalStock();
        }
        return quantity;
    }

    private static boolean shouldAdjustQuantity(boolean containsNearbyStores, int quantity, int totalStock) {
        return containsNearbyStores && quantity > totalStock;
    }
    /**
     * Procesa el stock de los items normales, para validar sus tiendas cercanas y stock total.
     * Si no se envía la lista de tiendas cercanas, se valida el stock del item en la tienda por defecto.
     */
    private Item processNormalItem(int id, int idStoreGroup, Integer quantity, AddDeliveryOrderItemRequest request, boolean isScanAndGo) throws ConflictException {
        try {
            Item item = productsMethods.setFindInformationToAlgoliaByIdItem(Integer.toString(id), idStoreGroup, quantity);

            if (item == null ) {
                throw new ConflictException(Constants.PRODUCT_NOT_EXISTS);
            }

            Optional<ExtendedBagPropertiesTalonOne> extendedPropertiesBagItem = APIAlgolia.getExtendedBagItem();
                if (extendedPropertiesBagItem.isPresent() &&  Arrays.stream(extendedPropertiesBagItem.get().getSku()).toList().contains(String.valueOf(item.getId()))){
                    item.setTotalStock(Integer.parseInt(extendedPropertiesBagItem.get().getFixedStock()));
                    return item;
            }

            boolean useTotalStock = Objects.nonNull(request) && Objects.nonNull(request.getNearbyStores()) && !request.getNearbyStores().isEmpty();

            if (useTotalStock) {
                item = stockMethods.validateStockItem(item, request.getNearbyStores(), id, isScanAndGo);
            } else {
                item = productsMethods.validateItemAlgolia(item, idStoreGroup, id, isScanAndGo, quantity);
            }

            item = talonOneComboService.totalStockIfIsItemComboTalon(id, idStoreGroup, quantity, request, isScanAndGo, item, useTotalStock);

            return item;
        } catch (ConflictException e) {
            LOG.log(Level.SEVERE, "Error procesando item " + id + ": " + e.getMessage(), e);
            throw new ConflictException("Error procesando item: " + e.getMessage(), e);
        }
    }

    /**
     * Verifica si el item es de optica
     */
    private boolean isOpticalItem(AddDeliveryOrderItemRequest request) {
        return request != null && request.getOpticalItemFilterList() != null && !request.getOpticalItemFilterList().isEmpty();
    }

    private Item getItemOptics(Integer id, AddDeliveryOrderItemRequest addDeliveryOrderItemRequest, OpticsServices opticsServices, int idStoreGroup, Item item, List<Item> items, RequestSourceEnum sourceEnum) throws ConflictException {

        if (addDeliveryOrderItemRequest.getOpticalItemFilterList().size() == 2) {
            OpticalItemFilter opticalItemFilterFirstPosition = addDeliveryOrderItemRequest.getOpticalItemFilterList().get(0);
            OpticalItemFilter opticalItemFilterSecondPosition = addDeliveryOrderItemRequest.getOpticalItemFilterList().get(1);
            if (opticsServices.isEqualsFilters(opticalItemFilterFirstPosition, opticalItemFilterSecondPosition)) {
                OpticalItemFilter filtersOptical = opticsServices.getFiltersOptical(opticalItemFilterFirstPosition, opticalItemFilterSecondPosition);
                filtersOptical.setMainItem(String.valueOf(id));
                item = getItemOpticsWithFilters(id, addDeliveryOrderItemRequest, opticsServices, idStoreGroup, item, items, sourceEnum, filtersOptical);
            } else {
                item = getItemOpticsWithFilters(id, addDeliveryOrderItemRequest, opticsServices, idStoreGroup, item, items, sourceEnum, null);
            }
        } else {
            item = getItemOpticsWithFilters(id, addDeliveryOrderItemRequest, opticsServices, idStoreGroup, item, items, sourceEnum, null);
        }
        LOG.info("ITEM -> " + item);
        return item;
    }

    private Item getItemOpticsWithFilters(Integer id, AddDeliveryOrderItemRequest addDeliveryOrderItemRequest, OpticsServices opticsServices, int idStoreGroup, Item item, List<Item> items, RequestSourceEnum sourceEnum, OpticalItemFilter filtersOptical) throws ConflictException {

        if (Objects.nonNull(filtersOptical)) {
            item = getItemSameEyeDirection(id, opticsServices, idStoreGroup, filtersOptical, sourceEnum);
        } else {
            item = getItemDifferentEyeDirection(id, addDeliveryOrderItemRequest, opticsServices, idStoreGroup, item, items, sourceEnum);
        }
        return item;
    }

    private Item getItemSameEyeDirection(Integer id, OpticsServices opticsServices, int idStoreGroup, OpticalItemFilter filtersOptical, RequestSourceEnum sourceEnum) throws ConflictException {
        Item item;
        int quantity = filtersOptical.getQuantity() + filtersOptical.getQuantitySecondPosition();
        int idItem = id;
        if (Objects.nonNull(filtersOptical.getMainItem())) {
            idItem = Integer.parseInt(filtersOptical.getMainItem());
        }
        LOG.info("Filters: " + filtersOptical.toString());
        item = productsMethods.setFindInformationOpticalToAlgoliaByIdItem(Integer.toString(idItem), idStoreGroup, filtersOptical, quantity);

        // verifica que el item de optica exista en Algolia
        if (Objects.isNull(item)) {
            throw new ConflictException(opticsServices.getMessageConfigOptics(filtersOptical, true, false, sourceEnum));
        }
        // Se valida que el item de optica tenga stock en Algolia
        try {
            item = productsMethods.validateItemAlgolia(item, idStoreGroup, id, false, filtersOptical.getQuantity());
        } catch (ConflictException e) {
            throw new ConflictException(opticsServices.getMessageConfigOptics(filtersOptical, true, true, sourceEnum));
        }
        return item;
    }

    private Item getItemDifferentEyeDirection(Integer id, AddDeliveryOrderItemRequest addDeliveryOrderItemRequest, OpticsServices opticsServices, int idStoreGroup, Item item, List<Item> items, RequestSourceEnum sourceEnum) throws ConflictException {
        List<Item> listVerifyQuantityItems = new ArrayList<>();
        List<Boolean> listExistsItems = new ArrayList<>();
        List<Boolean> listWithoutStockItems = new ArrayList<>();

        for (OpticalItemFilter opticalItemFilter : addDeliveryOrderItemRequest.getOpticalItemFilterList()) {
            int idItem = id;
            if (Objects.nonNull(opticalItemFilter.getMainItem())) {
                idItem = Integer.parseInt(opticalItemFilter.getMainItem());
            }
            LOG.info("Filters: " + opticalItemFilter.toString());
            opticalItemFilter.setMainItem(Integer.toString(idItem));
            item = productsMethods.setFindInformationOpticalToAlgoliaByIdItem(Integer.toString(idItem), idStoreGroup, opticalItemFilter, opticalItemFilter.getQuantity());
            if (Objects.isNull(item) || item.getItemId() == null) {
                listExistsItems.add(true);
            } else {
                listExistsItems.add(false);
                try {
                    item = productsMethods.validateItemAlgolia(item, idStoreGroup, id, false, opticalItemFilter.getQuantity());
                    listWithoutStockItems.add(false);
                } catch (ConflictException e) {
                    listWithoutStockItems.add(true);
                }
                if (!listVerifyQuantityItems.isEmpty()) {
                    validateItemForQuantityRequest(item, listVerifyQuantityItems);
                }
                items.add(item);
                listVerifyQuantityItems.add(item);
            }
        }

        // verifica que los items de optica existan en Algolia
        opticsServices.existAndStockItemsOptics(addDeliveryOrderItemRequest, listExistsItems, sourceEnum, false);
        // Se valida que los items de optica tengan stock en Algolia
        opticsServices.existAndStockItemsOptics(addDeliveryOrderItemRequest, listWithoutStockItems, sourceEnum, true);
        return item;
    }

    private void validateItemForQuantityRequest(Item item, List<Item> listVerifyQuantityItems) {
        Optional<Item> optionalItem = listVerifyQuantityItems.stream().filter(i -> Objects.equals(item.getId(), i.getId())).findFirst();
        optionalItem.ifPresent(value -> item.setQuantityRequest(item.getQuantityRequest() + value.getQuantityRequest()));
    }

    private DeliveryOrderItem getDeliveryOrderItem(Integer quantity, Boolean isInShoppingCart, String origin, String observations,
                                                   Boolean isSubstitute, Item item, boolean isScanAndGo, Key<DeliveryOrder> deliveryOrderKey, String eyeDirection, final boolean isOptical) {
        DeliveryOrderItem deliveryOrderItem1 = ofy().load().type(DeliveryOrderItem.class)
                .filter("idItem", Key.create(Item.class, item.getItemId()))
                .filter("changeQuantity", true)
                .ancestor(Ref.create(deliveryOrderKey)).first().now();


        final List<DeliveryOrderItem> deliverOrderExtra = ofy().load().type(DeliveryOrderItem.class)
                .filter("idItem", Key.create(Item.class, item.getItemId()))
                .filter("changeQuantity", false)
                .ancestor(Ref.create(deliveryOrderKey)).list();

        ofy().delete().entities(deliverOrderExtra).now();

//        LOG.info("deliveryOrderItem1() is null en datastore ----> "+deliveryOrderItem1);
        deliveryOrderItem1 = deliveryOrderItem1 == null ? new DeliveryOrderItem() : deliveryOrderItem1;
        deliveryOrderItem1 = this.deliveryOrderItemReturn(deliveryOrderItem1, item);

        final DeliveryOrderItem finalDeliveryOrderItem = deliveryOrderItem1;
        final Integer quantityFinal = quantity;
        Item finalItem = item;
        return ofy().transact(() -> responseAddDeliveryOrderItem(observations, isScanAndGo, isInShoppingCart, isSubstitute, origin, quantityFinal, deliveryOrderKey, finalDeliveryOrderItem, finalItem, deliverOrderExtra, isOptical));
    }

    @ApiMethod(name = "addDeliveryOrderItem", path = "/orderEndpoint/v2/addDeliveryOrderItem", httpMethod = ApiMethod.HttpMethod.POST)
    public DeliveryOrderItem addDeliveryOrderItemV2(@Named("token") final String token,
                                                    @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                                    @Named("idCustomerWebSafe") final String idCustomerWebSafe,
                                                    @Nullable @Named("id") final Integer id,
                                                    @Named("quantity") Integer quantity,
                                                    @Named("idStoreGroup") final int idStoreGroupFromRequest,
                                                    @Nullable @Named("isInShoppingCart") Boolean isInShoppingCart,
                                                    @Nullable @Named("deliveryType") final String deliveryType,
                                                    @Nullable @Named("origin") final String origin,
                                                    @Nullable @Named("observations") final String observations,
                                                    @Nullable @Named("isSubstitute") final Boolean isSubstitute,
                                                    @Nullable @Named("itemBarcode") final String itemBarcode)
            throws ConflictException, BadRequestException, AlgoliaException {

        if (!Authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);

        if (Objects.isNull(quantity) || quantity < 1) {
            throw new ConflictException(Constants.INVALID_QUANTITY);
        }
        int idStoreGroup = APIAlgolia.getDefaultStoreIdByStoreId(idStoreGroupFromRequest);
        Key<Customer> customerKey = Key.create(idCustomerWebSafe);
        DeliveryOrder deliveryOrder = ofy().load().type(DeliveryOrder.class).filter("currentStatus", 1).ancestor(Ref.create(customerKey)).first().now();

        if (deliveryOrder == null) {
            deliveryOrder = new DeliveryOrder();
            deliveryOrder.setIdDeliveryOrder(UUID.randomUUID().toString());
            deliveryOrder.setIdCustomer(Ref.create(customerKey));
            deliveryOrder.setCurrentStatus(1);
            deliveryOrder.setCreateDate(new Date());
            deliveryOrder.setDeliveryType(DeliveryType.valueOf(deliveryType));
            ofy().save().entity(deliveryOrder).now();
        } else {
            deliveryOrder.setDeliveryType(DeliveryType.getDeliveryType(deliveryType));
            ofy().save().entity(deliveryOrder).now();
        }
        Item item = null;
        boolean isScanAndGo = OrderUtil.isScanAndGo(deliveryType);
        if (isScanAndGo && itemBarcode != null && !itemBarcode.isEmpty()) {
            LOG.info("--> Se realiza busqueda a algolia con el barcode ");
            item = getItemOptics(itemBarcode);
            item.setTotalStock(item.getTotalStock() < 1 ? 1000 : item.getTotalStock());
        } else if (Objects.isNull(id) || id <= 0) {
            throw new ConflictException(Constants.PRODUCT_NOT_EXISTS);
        } else {
            LOG.info("--> Se realiza busqueda a algolia con el Id del Item ");
            if (isScanAndGo) {
                item = productsMethods.setFindInformationToAlgoliaByIdItem(Integer.toString(id), 26, null);
            } else {
                item = productsMethods.setFindInformationToAlgoliaByIdItem(Integer.toString(id), idStoreGroup, null);
            }
            AlgoliaItem algoliaItem = restrictItemsAlgolia(id.longValue(), idStoreGroup, item.getTotalStock());
            if (Objects.nonNull(algoliaItem) && Objects.nonNull(algoliaItem.getTotalStock()) && algoliaItem.getTotalStock().intValue() > 0) {
                item.setTotalStock(algoliaItem.getTotalStock());
                quantity = quantity > algoliaItem.getTotalStock() ? algoliaItem.getTotalStock() : quantity;
            }
            LOG.info("--> " + id + " --> Stock: " + item.getTotalStock() + " --> Quantity: " + quantity);
            if (!isScanAndGo && (item == null || item.getTotalStock() < 1)) {
                throw new ConflictException(Constants.PRODUCT_NOT_EXISTS);
            }
            if (isScanAndGo) {
                if (Objects.isNull(item)) {
                    throw new ConflictException(Constants.PRODUCT_NOT_EXISTS);
                } else {
                    item.setTotalStock(item.getTotalStock() < 1 ? 1000 : item.getTotalStock());
                }
            }
        }
        final Key<DeliveryOrder> deliveryOrderKey = Key.create(customerKey, DeliveryOrder.class, deliveryOrder.getIdDeliveryOrder());
        DeliveryOrderItem deliveryOrderItem1 = ofy().load().type(DeliveryOrderItem.class).
                filter("idItem", Key.create(Item.class, item.getItemId())).
                filter("changeQuantity", true).
                ancestor(Ref.create(deliveryOrderKey)).first().now();

        final List<DeliveryOrderItem> deliverOrderExtra = ofy().load().type(DeliveryOrderItem.class).
                filter("idItem", Key.create(Item.class, item.getItemId())).
                filter("changeQuantity", false).
                ancestor(Ref.create(deliveryOrderKey)).list();

        ofy().delete().entities(deliverOrderExtra).now();

        deliveryOrderItem1 = deliveryOrderItem1 == null ? new DeliveryOrderItem() : deliveryOrderItem1;
        deliveryOrderItem1 = this.deliveryOrderItemReturn(deliveryOrderItem1, item);

        final DeliveryOrderItem finalDeliveryOrderItem = deliveryOrderItem1;
        final Integer quantityFinal = quantity;


        Item finalItem = item;
        return ofy().transact(() -> responseAddDeliveryOrderItem(observations, isScanAndGo, isInShoppingCart, isSubstitute, origin, quantityFinal, deliveryOrderKey, finalDeliveryOrderItem, finalItem, deliverOrderExtra, false));
    }

    @NotNull
    private Item getItemOptics(String itemBarcode) throws ConflictException {
        Item item;
        item = productsMethods.setFindInformationToAlgoliaByBarcode(itemBarcode, 26);
        if (item == null) {
            item = productsMethods.setFindInformationToAlgoliaByIdItem(itemBarcode, 26, null);
        }
        if (item == null || (Objects.isNull(item.getFullPrice()) || item.getFullPrice() <= 0.0)) {
            throw new ConflictException(Constants.PRODUCT_NOT_EXISTS);
        }
        return item;
    }

    /***
     *  response addDeliveryOrderItem
     * @param observations
     * @param isScanAndGo
     * @param isInShoppingCart
     * @param isSubstitute
     * @param origin
     * @param quantityFinal
     * @param deliveryOrderKey
     * @param deliveryOrderItem1
     * @param item
     * @param deliverOrderExtra
     * @return DeliveryOrderItem
     */
        private DeliveryOrderItem responseAddDeliveryOrderItem(String observations, boolean isScanAndGo, Boolean isInShoppingCart,
                                                           Boolean isSubstitute, String origin, Integer quantityFinal,
                                                           Key<DeliveryOrder> deliveryOrderKey, DeliveryOrderItem deliveryOrderItem1,
                                                           Item item, List<DeliveryOrderItem> deliverOrderExtra, Boolean isOptical) {

        final DeliveryOrderItem finalDeliveryOrderItem = deliveryOrderItem1;
        final Item finalItem = item;
        Item finalItem1 = item;
        final DeliveryOrderItem finalDeliveryOrderItem1 = deliveryOrderItem1;

        DeliveryOrderItem deliveryOrderItem = finalDeliveryOrderItem;
        if (finalDeliveryOrderItem.getIdDeliveryOrderItem() == null) {
            //LOG.warning("strange");
            deliveryOrderItem.setIdDeliveryOrderItem(UUID.randomUUID().toString());
            deliveryOrderItem.setIdDeliveryOrder(Ref.create(deliveryOrderKey));
            deliveryOrderItem.setIdItem(Key.create(Item.class, finalItem.getItemId()));
            deliveryOrderItem.setQuantitySold(quantityFinal);
            deliveryOrderItem.setCreateDate(new Date());
            deliveryOrderItem.setFullPrice(finalItem.getFullPrice());
            deliveryOrderItem.setOfferPrice(finalItem.getOfferPrice());
            deliveryOrderItem.setTotalStock(finalItem.getTotalStock());
            deliveryOrderItem.setChangeQuantity(true);
            deliveryOrderItem.setDepartments(finalItem.getDepartments());
            deliveryOrderItem.setDeliveryPrice(finalItem.getDeliveryPrice());// Campos adicionales proveedores externos
            deliveryOrderItem.setOrigin(origin);// Agrega el origin de adición al carrito
            deliveryOrderItem.setObservations(observations);
            deliveryOrderItem.setHandleQuantity(finalItem.getHandleQuantity());
            deliveryOrderItem.setOnlyOnline(finalItem.isOnlyOnline());
            deliveryOrderItem.setItemUrl(finalItem.getItemUrl());
            Key<DeliveryOrderItem> deliveryOrderItemKey = Key.create(deliveryOrderKey, DeliveryOrderItem.class, deliveryOrderItem.getIdDeliveryOrderItem());// pre-render
            deliveryOrderItem.setIdDeliveryOrderItemWebSafe(deliveryOrderItemKey.toWebSafeString());
            deliveryOrderItem.setPrimePrice(finalItem.getPrimePrice());//primePrice
            deliveryOrderItem.setPrimeTextDiscount(finalItem.getPrimeTextDiscount());//primeTextDiscount
            deliveryOrderItem.setPrimeDescription(finalItem.getPrimeDescription());//primeDescription
            deliveryOrderItem.setRms_class(finalItem.getRms_class());
            deliveryOrderItem.setRms_subclass(finalItem.getRms_subclass());
        } else {
            //FIX: Scan and go: Valida si el item que existe es del mismo tipo del actual si , no aumenta la cantidad
            if ((isScanAndGo && (Objects.isNull(deliveryOrderItem.getScanAndGo()) || !deliveryOrderItem.getScanAndGo())) ||
                    (!isScanAndGo && (Objects.nonNull(deliveryOrderItem.getScanAndGo()) && deliveryOrderItem.getScanAndGo()))) {
//                LOG.info("isScanAndGo " + isScanAndGo);
                deliveryOrderItem.setTotalStock(deliveryOrderItem.getTotalStock() < 1 ? 1000 : deliveryOrderItem.getTotalStock());
                deliveryOrderItem.setQuantitySold(quantityFinal);
            } else {
                //LOG.warning("bool " + isInShoppingCart);
                if (isInShoppingCart != null && isInShoppingCart) {
                    //LOG.warning("InShop" + quantityFinal);
                    deliveryOrderItem.setQuantitySold(quantityFinal);
                } else {
                    int lastQuantity = 0;
                    if (!isOptical) {
                        for (DeliveryOrderItem orderItem : deliverOrderExtra) {
                            lastQuantity += orderItem.getQuantitySold();
                        }
                        lastQuantity += finalDeliveryOrderItem1.getQuantitySold();
                    }
                    deliveryOrderItem.setQuantitySold(quantityFinal + lastQuantity);
                    //LOG.warning("NoShop " + (quantityFinal + lastQuantity));
                }
                if (Objects.nonNull(observations) && !observations.isEmpty()) {
                    deliveryOrderItem.setObservations(observations);
                }
            }
            deliveryOrderItem.setChangeQuantity(true);
        }
        deliveryOrderItem.setRequirePrescription(finalItem.getRequirePrescription());
        deliveryOrderItem.setBarcode(finalItem.getBarcode());
        if (Objects.nonNull(isSubstitute)) {
            deliveryOrderItem.setSubstitute(isSubstitute);
        }
        deliveryOrderItem.setScanAndGo(isScanAndGo ? true : false); // save if scan and is true
        deliveryOrderItem.setDeliveryTime(finalItem1.getDeliveryTime());//Set deliveryTime.
        deliveryOrderItem.setDepartments(finalItem1.getDepartments()); // Set marca, subcategory, category
        deliveryOrderItem.setMarca(finalItem1.getMarca());
        deliveryOrderItem.setCategorie(finalItem1.getCategorie());
        deliveryOrderItem.setSubCategory(finalItem1.getSubCategory());
        //Se agrega los filtros de optica if(Objects.nonNull(filters)){ //deliveryOrderItem.setFilters(filters);}
        deliveryOrderItem.setItemUrl(finalItem1.getItemUrl());// pre-render
        deliveryOrderItem.setPrimePrice(finalItem1.getPrimePrice());//primePrice
        deliveryOrderItem.setPrimeTextDiscount(finalItem1.getPrimeTextDiscount());//primeTextDiscount
        deliveryOrderItem.setPrimeDescription(finalItem1.getPrimeDescription());//primeDescriptio


        itemOfferComboPopUp(deliveryOrderItem);//Mostrar pop-up de oferta
        deliveryOrderItem = checkTip(deliveryOrderItem);//valida que la propina sea solo 1 y de cantidad 1

        //add filters optical
        deliveryOrderItem.setFiltersOptical(finalItem.getFiltersOptical());
        if (Objects.nonNull(finalItem.getItemOpticsComplete()) && Objects.nonNull(finalItem.getItemOpticsComplete().getItemOptics().getShipment())) {
            deliveryOrderItem.setShipment(finalItem.getItemOpticsComplete().getItemOptics().getShipment());
        }
        LOG.info("DeliveryOrderItem Optical: " + deliveryOrderItem.toStringJson());

        final Integer quantityRequested = finalItem.getQuantityRequested();
        if (shouldSetQuantityRequested(isScanAndGo, quantityRequested)) {
            deliveryOrderItem.setQuantityRequested(quantityRequested);
        }

        deliveryOrderItem.setSupplier(finalItem1.getSupplier());
        deliveryOrderItem.setOnlyOnline(finalItem1.isOnlyOnline());
        deliveryOrderItem.setUuidItem(finalItem1.getUuidItem());

        if(deliveryOrderItem.getTotalStock() == 0){
            return deliveryOrderItem;
        }

        ofy().save().entity(deliveryOrderItem).now();

        LOG.info("responseAddDeliveryOrderItem-> FIN" +  new DateTime());
        return deliveryOrderItem;
    }


    private static boolean shouldSetQuantityRequested(boolean isScanAndGo, Integer quantityRequested) {
        return !isScanAndGo && Optional.ofNullable(quantityRequested).orElse(0) > 0;
    }

    private DeliveryOrderItem checkTip(DeliveryOrderItem deliveryOrderItem) {
        if (isTip(deliveryOrderItem.getId())) {
//            LOG.info("Method: checkTip () Modificar cantidad de propina a 1 ID -> " + deliveryOrderItem.getId() + " cantidad -> " + deliveryOrderItem.getQuantitySold());
            deliveryOrderItem.setQuantitySold(1);
        }
        return deliveryOrderItem;
    }


    @ApiMethod(name = "addDeliveryOrderItems", path = "/orderEndpoint/addDeliveryOrderItems", httpMethod = ApiMethod.HttpMethod.POST)
    public List<DeliveryOrderItem> addDeliveryOrderItems(final UpdateDeliveryOrderRequest updateDeliveryOrderRequest)
            throws ConflictException, BadRequestException, AlgoliaException {
        //LOG.warning(" method: addDeliveryOrderItems");
        if (!authenticate.isValidToken(updateDeliveryOrderRequest.getToken(), updateDeliveryOrderRequest.getTokenIdWebSafe()))
            throw new ConflictException(Constants.INVALID_TOKEN);

        if (Objects.isNull(updateDeliveryOrderRequest.getIdStoreGroupFromRequest()) || updateDeliveryOrderRequest.getIdStoreGroupFromRequest() == 0) {
            throw new ConflictException(Constants.ERROR_ID_STORE_GROUP_NULL);
        }

        int idStoreGroup = APIAlgolia.getDefaultStoreIdByStoreId(updateDeliveryOrderRequest.getIdStoreGroupFromRequest());
        Key<Customer> customerKey = Key.create(updateDeliveryOrderRequest.getIdCustomerWebSafe());
        DeliveryOrder deliveryOrderBase = ofy().load().type(DeliveryOrder.class).filter("currentStatus", 1).ancestor(Ref.create(customerKey)).first().now();
        final DeliveryOrder deliveryOrder = Objects.nonNull(deliveryOrderBase) ? deliveryOrderBase : new DeliveryOrder();

        // fix delete tips
        Optional<TipConfig> tipConfigOptional = GrowthBookConfigLoader.getTipsConfig(Constants.CUSTOMER_ANONYMOUS);
        if (tipConfigOptional.isPresent() && deliveryOrder.getIdOrder() > 0) {
            Key<User> customerKeyUser = Key.create(updateDeliveryOrderRequest.getIdCustomerWebSafe());
            deleteTips(tipConfigOptional.get(), deliveryOrder, customerKeyUser);
        }

        // delete items for tips.

        if (tipConfigOptional.isPresent()) {
            updateDeliveryOrderRequest.getItems();

            TipConfig tipConfig = tipConfigOptional.get();

            if (tipConfig.getItemTips() != null) {
                Optional<UpdateDeliveryOrderRequest.Item> optItemTipCart = updateDeliveryOrderRequest
                        .getItems().stream().filter(itemCart -> {

                            Optional<ItemTip> optItemTip = tipConfig
                                    .getItemTips()
                                    .stream()
                                    .filter(itemTip -> itemTip != null && itemTip.getItemId() != null && itemTip.getItemId() == itemCart.getItemId())
                                    .findFirst();

                            return optItemTip.isPresent();
                        })
                        .findFirst();

                if (optItemTipCart.isPresent()) {
                    LOG.info("ITEM a BORRAR -> " + optItemTipCart.get().getItemId());

                    updateDeliveryOrderRequest
                            .getItems()
                            .removeIf(itemRequest -> itemRequest.getItemId() == optItemTipCart.get().getItemId());
                }

                LOG.info("new request after delete tips-> " + updateDeliveryOrderRequest.toString());
            }
        }

        if (Objects.isNull(deliveryOrderBase)) {
//            Se crea un carrito de compras
            deliveryOrder.setIdDeliveryOrder(UUID.randomUUID().toString());
            deliveryOrder.setIdCustomer(Ref.create(customerKey));
            deliveryOrder.setCurrentStatus(1);
            deliveryOrder.setCreateDate(new Date());
            if (Objects.nonNull(updateDeliveryOrderRequest.getDeliveryType())) {
                if (updateDeliveryOrderRequest.getDeliveryType() == "EXPRESS" || "EXPRESS".equals(updateDeliveryOrderRequest.getDeliveryType())) {
                    deliveryOrder.setDeliveryType(DeliveryType.EXPRESS);
                } else if (updateDeliveryOrderRequest.getDeliveryType() == "NATIONAL" || "NATIONAL".equals(updateDeliveryOrderRequest.getDeliveryType())) {
                    deliveryOrder.setDeliveryType(DeliveryType.NATIONAL);
                } else if (updateDeliveryOrderRequest.getDeliveryType() == "ENVIALOYA" || "ENVIALOYA".equals(updateDeliveryOrderRequest.getDeliveryType())) {
                    deliveryOrder.setDeliveryType(DeliveryType.ENVIALOYA);
                } else if (updateDeliveryOrderRequest.getDeliveryType() == "SCANANDGO" || "SCANANDGO".equals(updateDeliveryOrderRequest.getDeliveryType())) {
                    deliveryOrder.setDeliveryType(DeliveryType.SCANANDGO);
                }
            }
            ofy().save().entity(deliveryOrder).now();
        } else {
            //LOG.warning("Se actualiza el carrito de compras existente.");
            deliveryOrder.setDeliveryType(DeliveryType.getDeliveryType(updateDeliveryOrderRequest.getDeliveryType()));
            ofy().save().entity(deliveryOrder).now();
        }

        List<DeliveryOrderItem> deliveryItems = updateDeliveryOrderRequest.getItems().stream()
                .filter(item -> Objects.nonNull(item))
                .map(item -> {
                    try {
//                        LOG.info("isShop -> " + updateDeliveryOrderRequest.isInShoppingCart());
                        return productsMethods.buildDeliveryOrderItem(deliveryOrder, customerKey, item, updateDeliveryOrderRequest.isInShoppingCart(), idStoreGroup);
                    } catch (ConflictException e) {
                        LOG.warning("Error al generar los items a agregar. " + e.getMessage() + " - " + e.fillInStackTrace());
                        return null;
                    }
                }).collect(Collectors.toList());

        if (Objects.nonNull(deliveryItems) && !deliveryItems.isEmpty()) {
            try {
                return ofy().transact(() -> {
                    ofy().save().entities(deliveryItems);
                    return deliveryItems;
                });
            } catch (Exception e) {
                throw new ConflictException(Constants.PRODUCT_NOT_EXISTS);
            }
        } else {
            throw new ConflictException(Constants.PRODUCT_NOT_EXISTS);
        }
    }

    @ApiMethod(name = "handleDeliveryOrderItems", path = "/orderEndpoint/handleDeliveryOrderItems", httpMethod = ApiMethod.HttpMethod.POST)
    public List<DeliveryOrderItem> handleDeliveryOrderItems(final UpdateDeliveryOrderRequest updateDeliveryOrderRequest)
            throws ConflictException, BadRequestException, AlgoliaException {
//        LOG.info("method: handleDeliveryOrderItems");
        Answer answer = null;
        List<DeliveryOrderItem> result = new ArrayList<>();
        if (Objects.nonNull(updateDeliveryOrderRequest)) {
            if (Objects.nonNull(updateDeliveryOrderRequest.getItemsToDelete()) && !updateDeliveryOrderRequest.getItemsToDelete().isEmpty()) {
                answer = deleteDeliveryOrderItems(updateDeliveryOrderRequest);
            }
            if (Objects.nonNull(updateDeliveryOrderRequest.getItems()) && !updateDeliveryOrderRequest.getItems().isEmpty()) {
                result = addDeliveryOrderItems(updateDeliveryOrderRequest);
            }
            return result;

        } else {
            throw new ConflictException(Constants.INVALID_TOKEN);
        }
    }


    @ApiMethod(name = "orderProcessedBy", path = "/orderEndpoint/orderProcessedBy", httpMethod = ApiMethod.HttpMethod.POST)
    public Answer orderProcessedBy(ProcessOrderReq processOrderReq) throws BadRequestException {

        Answer answer = new Answer();
        answer.setConfirmation(false);
        answer.setMessage("Upload processed Order ailed");

//        LOG.info("Request processed Order -> " + processOrderReq.toString());

        if (!processOrderReq.getOrderId().isEmpty() && !processOrderReq.getProcessedBy().isEmpty()) {

            ProcessedOrder processedOrder = new ProcessedOrder();
            processedOrder.setOrderId(processOrderReq.getOrderId());
            processedOrder.setProcessedBy(processOrderReq.getProcessedBy());

            ofy().save().entity(processedOrder).now();

            answer.setConfirmation(true);
            answer.setMessage("Upload processed Order Success ");

        } else {
            throw new BadRequestException(HttpStatusCode.BAD_REQUEST.getStatusName());
        }

        return answer;
    }

    @ApiMethod(name = "deleteDeliveryOrderItem", path = "/orderEndpoint/deleteDeliveryOrderItem", httpMethod = ApiMethod.HttpMethod.DELETE)
    public Answer deleteDeliveryOrderItem(@Named("token") final String token,
                                          @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                          @Named("idCustomerWebSafe") final String idCustomerWebSafe,
                                          @Named("idStoreGroup") final int idStoreGroupFromRequest,
                                          @Named("idProduct") final Integer idProductInformation,
                                          @Nullable @Named("deliveryType") final String deliveryType)
            throws ConflictException, BadRequestException, AlgoliaException {
        //LOG.warning(" method: deleteDeliveryOrderItem");
        if (!authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);

        int idStoreGroup = APIAlgolia.getDefaultStoreIdByStoreId(idStoreGroupFromRequest);

        Key<Customer> customerKey = Key.create(idCustomerWebSafe);
        DeliveryOrder deliveryOrder = ofy().load().type(DeliveryOrder.class).filter("currentStatus", 1)
                .ancestor(Ref.create(customerKey)).first().now();

        if (deliveryOrder == null)
            throw new ConflictException(Constants.DELIVERY_ORDER_NOT_FOUND);

        boolean isScanAndGo = isScanAndGo(deliveryType);

        Item item = productsMethods.setFindInformationToAlgoliaByIdItem(Integer.toString(idProductInformation), idStoreGroup, null);

        if (item == null)
            throw new ConflictException(Constants.PRODUCT_NOT_EXISTS);

        Optional<String> itemFreeRecognizeIfDelete = CachedDataManager.getJsonFromCacheIndex(idCustomerWebSafe + "-itf", 14);

        if (primeUtil.isItemPrime(Long.valueOf(idProductInformation)) ||
                item.getRms_subclass().equals(Constants.NAME_SAMPLING.toUpperCase()) ||
                (itemFreeRecognizeIfDelete.isPresent() && itemFreeRecognizeIfDelete.get().equals(item.getId()))
        ) {
            CachedDataManager.saveJsonInCacheIndexTime(idCustomerWebSafe, Integer.toString(idProductInformation), 14, 600);
        }

        Optional<BagItem> bagItem = APIAlgolia.getBagItem();
        try {
            if (bagItem.isPresent() && Objects.nonNull(item) && Objects.nonNull(item.getId()) && bagItem.get().getId().equals(String.valueOf(item.getId()))) {
                CachedDataManager.saveJsonInCacheIndexTime(Constants.BAG_CACHE_DELETION_INDEX + idCustomerWebSafe, Integer.toString(idProductInformation), 14, 300);
                CachedBack3DataManager.deleteKeyIndex(idCustomerWebSafe + Constants.CORE_LIB_TALON_CACHE_KEY, 14);
                CachedBack3DataManager.deleteKeyIndex(tokenIdWebSafe + Constants.CORE_LIB_TALON_CACHE_KEY, 14);
                Key<User> userKey = Key.create(idCustomerWebSafe);
                User user = users.findUserByKey(userKey);
                if (Objects.nonNull(user) && Objects.nonNull(user.getId())) {
                    CachedBack3DataManager.deleteKeyIndex(user.getId() + Constants.CORE_LIB_TALON_CACHE_KEY, 14);
                }
            }
        } catch (Exception ex) {
            LOG.warning("Error al procesar el cache de la bolsa. " + ex.getMessage());
        }

        Key<DeliveryOrder> deliveryOrderKey = Key.create(customerKey, DeliveryOrder.class, deliveryOrder.getIdDeliveryOrder());
        /**DeliveryOrderItem deliveryOrderItem = ofy().load().type(DeliveryOrderItem.class).filter("idItem", Key.create(Item.class, item.getItemId()))
         .filter("scanAndGo", Objects.isNull(isScanAndGo) ? false: isScanAndGo)
         .ancestor(Ref.create(deliveryOrderKey)).first().now();*/
        DeliveryOrderItem deliveryOrderItem = ofy().load().type(DeliveryOrderItem.class).filter("idItem", Key.create(Item.class, item.getItemId()))
                .ancestor(Ref.create(deliveryOrderKey)).first().now();
        deliveryOrderItem = (isScanAndGo && Objects.nonNull(deliveryOrderItem) && deliveryOrderItem.getScanAndGo()) ? deliveryOrderItem :
                (!isScanAndGo && Objects.nonNull(deliveryOrderItem) && (Objects.isNull(deliveryOrderItem.getScanAndGo()) || !deliveryOrderItem.getScanAndGo()) ? deliveryOrderItem : null);

        if (deliveryOrderItem == null && bagItem.get().getId().equals(item.getItemId())) {
            Answer answer = new Answer();
            answer.setConfirmation(true);
            return answer;
        }


        if (deliveryOrderItem == null)
            throw new ConflictException("ITEM NO AGREGADO");

        ofy().delete().entity(deliveryOrderItem).now();
        // Elimina en Proveedores
        DeliveryOrderProvider deliveryOrderProvider = ofy().load().type(DeliveryOrderProvider.class).ancestor(Ref.create(deliveryOrderKey)).first().now();
        if (Objects.nonNull(deliveryOrderProvider) &&
                Objects.nonNull(deliveryOrderProvider.getItemList()) &&
                !deliveryOrderProvider.getItemList().isEmpty()) {
            //LOG.warning(" Ide validado: Id: " + item.getId());
            //LOG.warning(" Num items : " + deliveryOrderProvider.getItemList().size());
            deliveryOrderProvider.getItemList().removeIf(itemProvider -> item.getId() == itemProvider.getId());
            //LOG.warning(" Num items Despues de eliminar: " + deliveryOrderProvider.getItemList().size());
            if (deliveryOrderProvider.getItemList().isEmpty()) {
                //LOG.warning(" Eliminando order provider ");
                ofy().delete().entity(deliveryOrderProvider).now();
            } else {
                ofy().save().entity(deliveryOrderProvider).now();
            }

            //LOG.warning(" Finaliza deleteDeliveryOrderItem");
        }
        Answer answer = new Answer();
        answer.setConfirmation(true);
        return answer;
    }

    @ApiMethod(name = "deleteDeliveryOrderItems", path = "/orderEndpoint/deleteDeliveryOrderItems", httpMethod = ApiMethod.HttpMethod.DELETE)
    public Answer deleteDeliveryOrderItems(final UpdateDeliveryOrderRequest updateDeliveryOrderRequest)
            throws ConflictException, BadRequestException, AlgoliaException {
        //LOG.warning(" method: deleteDeliveryOrderItem");
        if (!authenticate.isValidToken(updateDeliveryOrderRequest.getToken(), updateDeliveryOrderRequest.getTokenIdWebSafe()))
            throw new ConflictException(Constants.INVALID_TOKEN);

        Key<Customer> customerKey = Key.create(updateDeliveryOrderRequest.getIdCustomerWebSafe());
        //LOG.warning("method: customerKey -> " + customerKey);
        DeliveryOrder deliveryOrder = ofy().load().type(DeliveryOrder.class).filter("currentStatus", 1).ancestor(Ref.create(customerKey)).first().now();

        /**
         * Se comentan estas lineas para que no genere error cuando no existe el carrito y se agrega la validación en el siguiente if...
         */
//        if (deliveryOrder == null)
//            throw new ConflictException(Constants.DELIVERY_ORDER_NOT_FOUND);

        if (Objects.nonNull(updateDeliveryOrderRequest.getItemsToDelete()) && !updateDeliveryOrderRequest.getItemsToDelete().isEmpty() && Objects.nonNull(deliveryOrder)) {

            Key<DeliveryOrder> deliveryOrderKey = Key.create(customerKey, DeliveryOrder.class, deliveryOrder.getIdDeliveryOrder());
            // Borrado asincrono de imagenes
            List<DeliveryOrderItem> itemsToDelete = updateDeliveryOrderRequest.getItemsToDelete().stream().map(itemRequest -> {
                DeliveryOrderItem deliveryOrderItem = ofy().load().type(DeliveryOrderItem.class).filter("idItem", Key.create(Item.class, String.valueOf(itemRequest.getItemId()))).ancestor(Ref.create(deliveryOrderKey)).first().now();
                //LOG.warning("method: deliveryOrderItem -> " + (Objects.nonNull(deliveryOrderItem) ? deliveryOrderItem.getId() : "Sin Item") + " Item: " + Key.create(Item.class, itemRequest.getItemId()));
                //LOG.warning("method: getIdDeliveryOrder -> " + deliveryOrder.getIdDeliveryOrder() + " - deliveryOrderKey: " + deliveryOrderKey);
                return deliveryOrderItem;
            }).collect(Collectors.toList());

            if (Objects.nonNull(itemsToDelete) && !itemsToDelete.isEmpty()) {
                itemsToDelete.removeIf(item -> Objects.isNull(item));
                ofy().delete().entities(itemsToDelete);
            }
            // Elimina en Proveedores
            DeliveryOrderProvider deliveryOrderProvider = ofy().load().type(DeliveryOrderProvider.class).ancestor(Ref.create(deliveryOrderKey)).first().now();

            if (Objects.nonNull(deliveryOrderProvider) && Objects.nonNull(deliveryOrderProvider.getItemList()) && !deliveryOrderProvider.getItemList().isEmpty()) {
                //LOG.warning(" Num items Antes de eliminar: " + deliveryOrderProvider.getItemList().size());
                deliveryOrderProvider.getItemList()
                        .removeIf(itemProvider -> updateDeliveryOrderRequest.getItemsToDelete().stream().filter(itemDelete -> itemDelete.getItemId() == itemProvider.getId()).findFirst().isPresent());
                //LOG.warning(" Num items Despues de eliminar: " + deliveryOrderProvider.getItemList().size());

                if (deliveryOrderProvider.getItemList().isEmpty()) {
                    //LOG.warning(" Eliminando order provider ");
                    ofy().delete().entity(deliveryOrderProvider);
                } else {
                    ofy().save().entity(deliveryOrderProvider);
                }

                //LOG.warning(" Finaliza deleteDeliveryOrderItem");
            }
        }
        /**
         * Se comenta el else para no enviar error en el api...
         */
//        else {
//            throw new ConflictException(Constants.PRODUCT_NOT_EXISTS);
//        }
        Answer answer = new Answer();
        answer.setConfirmation(true);
        return answer;
    }

    @ApiMethod(name = "deleteDeliveryOrderCoupon", path = "/orderEndpoint/deleteDeliveryOrderCoupon", httpMethod = ApiMethod.HttpMethod.DELETE)
    public Answer deleteDeliveryOrderCoupon(@Named("token") final String token,
                                            @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                            @Named("idCustomerWebSafe") final String idCustomerWebSafe)
            throws ConflictException, BadRequestException, AlgoliaException {
        //LOG.warning(" method: deleteDeliveryOrderCoupon");
        if (!authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);

        Key<Customer> customerKey = Key.create(idCustomerWebSafe);
        DeliveryOrder deliveryOrder = ofy().load().type(DeliveryOrder.class).filter("currentStatus", 1).ancestor(Ref.create(customerKey)).first().now();
        if (deliveryOrder == null)
            throw new ConflictException(Constants.DELIVERY_ORDER_NOT_FOUND);

        Key<DeliveryOrder> deliveryOrderKey = Key.create(customerKey, DeliveryOrder.class, deliveryOrder.getIdDeliveryOrder());
        List<DeliveryOrderItem> deliveryOrderItems = ofy().load().type(DeliveryOrderItem.class).ancestor(Ref.create(deliveryOrderKey)).list();

        deliveryOrderItems = deliveryOrderItems.stream()
                .filter(item -> (Objects.nonNull(item.getCoupon()) && item.getCoupon()))
                .collect(Collectors.toList());
        deleteCoupon(customerKey);
        ofy().delete().entities(deliveryOrderItems).now();
        //Delete coupon TalonOne
        if (idCustomerWebSafe != null) {
            Key<User> userKey = Key.create(idCustomerWebSafe);
            User user = users.findUserByKey(userKey);
            deleteResponseRequestTalon(user, Constants.INDEX_REDIS_FOURTEEN);
            deleteCouponTalonOne(user.getId(), idCustomerWebSafe);
            deleteCacheDeductDiscount(idCustomerWebSafe, String.valueOf(user.getId()));
        }
        Answer answer = new Answer();
        answer.setConfirmation(true);
        return answer;
    }

    private static void deleteResponseRequestTalon(User user, Integer NUM_BD_REDIS) {
        CachedDataManager.deleteKeyIndex(user.getId() + "request", NUM_BD_REDIS);
        CachedDataManager.deleteKeyIndex(user.getId() + "response", NUM_BD_REDIS);
    }

    private void deleteCouponTalonOne(Integer userId, String idCustomerWebSafe) {
        TalonOneService talonOneService = new TalonOneService();
        talonOneService.deleteCouponTalonOne(userId, idCustomerWebSafe);
    }

    public static void deleteCacheDeductDiscount(String idCustomerWebSafe, String idUser){
        CachedDataManager.deleteKeyIndex(idCustomerWebSafe + Constants.KEY_DISCOUNT_RPM, Constants.INDEX_REDIS_FOURTEEN);
        CachedDataManager.deleteKeyIndex(idCustomerWebSafe + Constants.KEY_DISCOUNT_PRIME, Constants.INDEX_REDIS_FOURTEEN);
        CachedDataManager.deleteKeyIndex(idCustomerWebSafe + Constants.KEY_DISCOUNT_TALON, Constants.INDEX_REDIS_FOURTEEN);
        CachedDataManager.deleteKeyIndex(idCustomerWebSafe + Constants.KEY_TOTAL_SAVE, Constants.INDEX_REDIS_FOURTEEN);
        CachedDataManager.deleteKeyIndex(idCustomerWebSafe + Constants.KEY_OFFER_PRICE, Constants.INDEX_REDIS_FOURTEEN);
        CachedDataManager.deleteKeyIndex(idCustomerWebSafe + Constants.KEY_COUPON_CACHE, Constants.INDEX_REDIS_FOURTEEN);
        CachedDataManager.deleteKeyIndex(idUser + Constants.KEY_COUPON_CACHE, Constants.INDEX_REDIS_FOURTEEN);
        CachedDataManager.deleteKeyIndex(idCustomerWebSafe + Constants.KEY_COUPON_AUTOMATIC, Constants.INDEX_REDIS_FOURTEEN);
        CachedDataManager.deleteKeyIndex(idUser + Constants.KEY_REQUEST_TALON, Constants.INDEX_REDIS_FOURTEEN);
        CachedDataManager.deleteKeyIndex(idUser + Constants.KEY_RESPONSE_TALON, Constants.INDEX_REDIS_FOURTEEN);
    }


    @ApiMethod(name = "deleteDeliveryOrder", path = "/orderEndpoint/deleteDeliveryOrder", httpMethod = ApiMethod.HttpMethod.DELETE)
    public Answer deleteDeliveryOrder(@Named("token") final String token,
                                      @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                      @Named("idCustomerWebSafe") final String idCustomerWebSafe,
                                      @Nullable @Named("hasCoupon") final Boolean hasCoupon,
                                      @Nullable @Named("deliveryType") final String deliveryType,
                                      @Nullable @Named("selfCheckout") final Boolean selfCheckout)
            throws ConflictException, BadRequestException {

        if (!authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);

        if (idCustomerWebSafe == null || idCustomerWebSafe.isEmpty())
            throw new BadRequestException(Constants.ERROR_BAD_REQUEST);

        Key<Customer> customerKey = Key.create(idCustomerWebSafe);
        DeliveryOrder deliveryOrder = ofy().load().type(DeliveryOrder.class).filter("currentStatus", 1).ancestor(Ref.create(customerKey)).first().now();

        if (deliveryOrder == null && Objects.nonNull(selfCheckout) && selfCheckout == true) {
            Answer answer = new Answer();
            answer.setConfirmation(true);
            return answer;
        } else if (deliveryOrder == null) {
            throw new ConflictException(Constants.DELIVERY_ORDER_NOT_FOUND);
        }

        boolean isScanAndGo = isScanAndGo(deliveryType);
        Key<DeliveryOrder> deliveryOrderKey = Key.create(customerKey, DeliveryOrder.class, deliveryOrder.getIdDeliveryOrder());
        List<DeliveryOrderItem> deliveryOrderItems = ofy().load().type(DeliveryOrderItem.class).ancestor(Ref.create(deliveryOrderKey)).list();

        // Valida que la orden no tenga más items que no sean ScanAndGo
        boolean deleteOrder = Boolean.TRUE;


        if (!deliveryOrderItems.isEmpty()) {
            Integer tamTotal = deliveryOrderItems.size();


            deliveryOrderItems = deliveryOrderItems.stream()
                    .filter(item -> isScanAndGo ? (Objects.nonNull(item.getScanAndGo()) && item.getScanAndGo()) || (Objects.nonNull(item.getCoupon()) && item.getCoupon())
                            : (Objects.isNull(item.getScanAndGo()) || !item.getScanAndGo()))
                    .collect(Collectors.toList());

            if (tamTotal != deliveryOrderItems.size()) {
                deleteOrder = Boolean.FALSE;
            }
        }

        List<DeliveryOrderProvider> deliveryOrderProviders = ofy().load().type(DeliveryOrderProvider.class).ancestor(Ref.create(deliveryOrderKey)).list();
        if (deliveryOrderItems == null)
            throw new ConflictException("ITEM NO AGREGADO");

        if (Objects.nonNull(deliveryOrderProviders)) {
            ofy().delete().entities(deliveryOrderProviders).now();
        }
        // Fix eliminar cupon carrito
        if ((Objects.nonNull(hasCoupon) && hasCoupon) || isScanAndGo) {
            deleteCoupon(customerKey);
        }
        ofy().delete().entities(deliveryOrderItems).now();

        if (deleteOrder) {
            ofy().delete().entity(deliveryOrder).now();
        }

        Key<User> userKey = Key.create(idCustomerWebSafe);
        User user = users.findUserByKey(userKey);
        deleteCouponTalonOne(user.getId(), idCustomerWebSafe);
        deleteCacheDeductDiscount(idCustomerWebSafe, String.valueOf(user.getId()));
        Answer answer = new Answer();
        answer.setConfirmation(true);
        return answer;
    }


    @ApiMethod(name = "updateDeliveryOrder", path = "/orderEndpoint/updateDeliveryOrder", httpMethod = ApiMethod.HttpMethod.PUT)
    public ResponseUpdateDeliveryOrder updateDeliveryOrder(
            @Named("token") final String token,
            @Named("tokenIdWebSafe") final String tokenIdWebSafe,
            @Named("idCustomerWebSafe") final String idCustomerWebSafe,
            final AmountDeliveryOrderItem amountDeliveryOrderItem,
            @Named("target") final int target) throws ConflictException, BadRequestException {

        if (!authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);

        Key<User> key = Key.create(idCustomerWebSafe);
        User user = ofy().load().key(key).now();

        if (user == null)
            throw new ConflictException(Constants.CUSTOMER_NOT_FOUND);

        DeliveryOrder deliveryOrder = ofy().load().type(DeliveryOrder.class).filter("currentStatus", 1).ancestor(Ref.create(key)).first().now();
        List<DeliveryOrderItem> deliveryOrderItemList;

        deliveryOrderItemList = ofy().load().type(DeliveryOrderItem.class).ancestor(deliveryOrder).list();

        //AmountDeliveryOrderItem amountDeliveryOrderItem1 = new AmountDeliveryOrderItem();
        for (String _key : amountDeliveryOrderItem.getAmount().keySet()) {
            for (DeliveryOrderItem deliveryOrderItem : deliveryOrderItemList) {
                if (deliveryOrderItem.getIdItem().toWebSafeString().equals(_key)) {
                    deliveryOrderItem.setQuantitySold(amountDeliveryOrderItem.getAmount().get(_key));
                    ofy().save().entity(deliveryOrderItem).now();
                }
            }
        }

        ResponseUpdateDeliveryOrder responseUpdateDeliveryOrder = new ResponseUpdateDeliveryOrder();

        switch (target) {
            case 1: {
                responseUpdateDeliveryOrder.setStatus(1);
                break;
            }
            case 2: {

                responseUpdateDeliveryOrder.setStatus(2);
                break;
            }
            case 3: {
                responseUpdateDeliveryOrder.setStatus(3);
                break;
            }
        }
        return responseUpdateDeliveryOrder;
    }

    @ApiMethod(name = "updateStatusDeliveryOrder", path = "/orderEndpoint/updateStatusDeliveryOrder", httpMethod = ApiMethod.HttpMethod.PUT)
    public void updateStatusDeliveryOrder(
            @Named("token") final String token,
            @Named("tokenIdWebSafe") final String tokenIdWebSafe,
            @Named("idFarmatodo") final int idFarmatodo,
            @Named("minutes") final int minutes,
            @Named("status") final int status) throws ConflictException, BadRequestException {

        if (!authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);

        DeliveryOrder deliveryOrder = ofy().load().type(DeliveryOrder.class).filter("idFarmatodo", idFarmatodo).first().now();

        if (deliveryOrder == null)
            throw new ConflictException(Constants.DELIVERY_ORDER_NOT_FOUND);

        deliveryOrder.setMinutes(minutes);
        deliveryOrder.setCurrentStatus(status);
        ofy().save().entity(deliveryOrder).now();
    }

    @ApiMethod(name = "priceDeliveryOrder", path = "/orderEndpoint/priceDeliveryOrder", httpMethod = ApiMethod.HttpMethod.POST)
    public DeliveryOrder priceDeliveryOrder(final ShoppingCartJson shoppingCartJson, HttpServletRequest request) throws ConflictException, IOException, BadRequestException, InternalServerErrorException, NotFoundException, UnauthorizedException, AlgoliaException {
//        LOG.info("method: priceDeliveryOrder(" +  shoppingCartJson.toStringJson() + ")");
        try {
            com.imaginamos.farmatodo.model.talonone.Coupon traditionalCoupon = new com.imaginamos.farmatodo.model.talonone.Coupon();
            Guard.againtsNull(shoppingCartJson, "shoppingCartJson");
            Guard.againtsNullOrEmpty(shoppingCartJson.getToken(), "Token");
            Guard.againtsNullOrEmpty(shoppingCartJson.getTokenIdWebSafe(), "TokenIdWebSafe");
            Guard.againtsNullOrEmpty(shoppingCartJson.getIdCustomerWebSafe(), "IdCustomerWebSafe");
            Guard.againtsZero(shoppingCartJson.getIdStoreGroup(), "IdStoreGroup");
            Guard.againtsNullOrEmpty(shoppingCartJson.getSource(), "Source");

            if (Guard.isWebWithoutDeliveryType(shoppingCartJson)) {
                throw new BadRequestException("BadRequest [deliveryType] is required for web");
            }

            if (Guard.isDeliveryTypePresent(shoppingCartJson))
                shoppingCartJson.setDeliveryType(DeliveryType.EXPRESS);

            if (!authenticate.isValidToken(shoppingCartJson.getToken(), shoppingCartJson.getTokenIdWebSafe())) {
//                LOG.info("method: priceDeliveryOrder() --> ConflictException [bad credentials]");
                throw new ConflictException(Constants.INVALID_TOKEN);
            }
            Optional<TipConfig> tipConfigOptional = GrowthBookConfigLoader.getTipsConfig(Constants.CUSTOMER_ANONYMOUS);

            OrderStoreService orderStoreService = new OrderStoreService();
            shoppingCartJson.setDeliveryType(orderStoreService.putStore26withExpress(shoppingCartJson));

            shoppingCartJson.setIdStoreGroup(APIAlgolia.getDefaultStoreIdByStoreId(shoppingCartJson.getIdStoreGroup()));

            final List<DeliveryOrderItem> deliveryOrderItemListToSave = new ArrayList<>();
            List<DeliveryOrderProvider> deliveryOrderProviderListToSave = new ArrayList<>();
            List<DeliveryOrderItem> deliveryOrderItemList = new ArrayList<>();

            if (Guard.isNationalOrEnvialoYa(shoppingCartJson)) {
                shoppingCartJson.setIdStoreGroup(shoppingCartJson.getDeliveryType().getDefaultStore());
            }
//            LOG.info("idStoreGroup assigned -> " + shoppingCartJson.getIdStoreGroup() +" Request changue to -> " + shoppingCartJson.toStringJson());

            boolean isScanAndGo = isScanAndGo(shoppingCartJson.getDeliveryType());
            Key<User> customerKey = Key.create(shoppingCartJson.getIdCustomerWebSafe());
            DeliveryOrder deliveryOrder = null;
            try {
                deliveryOrder = getCourrentDeliveryOrder(customerKey);
            } catch (ApiProxy.ApiDeadlineExceededException apiDeadlineExceededException) {
                deliveryOrder = getCourrentDeliveryOrder(customerKey);
            } catch (Exception e) {
                throw e;
            }

            if (deliveryOrder == null) {
//                LOG.info("No existe un carrito activo para el cliente.");
                deliveryOrder = getEmptyDeliveryOrder(new DeliveryOrder());
            } else {
//                LOG.info("Data + Carrito " + deliveryOrder);
//                LOG.info("Existe un carrito activo para el cliente, se procede a validar.Datastore deliveryOrder.deliveryType :" + deliveryOrder.getDeliveryType());
                deliveryOrderItemList = ofy().load().type(DeliveryOrderItem.class).ancestor(deliveryOrder).list();
                validateDuplicateItems(deliveryOrderItemList);// Fix items duplicados

                List<DeliveryOrderProvider> deliveryOrderProviderList = new ArrayList<>();
                if (!isScanAndGo) {// solo si no es scan-go
                    deliveryOrderProviderList = ofy().load().type(DeliveryOrderProvider.class).ancestor(deliveryOrder).list();
                }
                validateRequestedQuantityVsRealStockInStore(deliveryOrder, shoppingCartJson.getIdStoreGroup());

                // ADD TIP TO cart delete tips if exists in cart and is not express delivery type.
                if (tipConfigOptional.isPresent() && !shoppingCartJson.getDeliveryType().equals(DeliveryType.EXPRESS)) {
//                    Eliminar propinas
                    deleteTips(tipConfigOptional.get(), deliveryOrder, customerKey);
                }
                // if city not use TIP, delete TIPS.
                deleteTipsIfCityNotConfig(tipConfigOptional, deliveryOrder, customerKey, shoppingCartJson.getIdStoreGroup());

                if (tipConfigOptional.isPresent() && shoppingCartJson.getTip() != null && shoppingCartJson.getTip() >= 0 && shoppingCartJson.getDeliveryType().equals(DeliveryType.EXPRESS)) {
                    boolean cartHaveTip = addTipToOrder(tipConfigOptional.get(), shoppingCartJson, deliveryOrder, customerKey);
//                    LOG.info("Se agrega propina al carrito?? => " + cartHaveTip);
                }

                if (Objects.isNull(deliveryOrderItemList) && Objects.isNull(deliveryOrderProviderList)) {
                    LOG.warning("deliveryOrderItemList is null NO HAY ITEMS AGREGADOS");
                    throw new ConflictException("NO HAY ITEMS AGREGADOS");
                }
                //LOG.warning("items size: [" + (Objects.nonNull(deliveryOrderItemList) ? deliveryOrderItemList.size() : 0) + "]"+"providers size: [" + (Objects.nonNull(deliveryOrderProviderList) ? deliveryOrderProviderList.size() : 0) + "]");

                if (deliveryOrderItemList.isEmpty() && deliveryOrderProviderList.isEmpty()) {
                    deliveryOrder = getEmptyDeliveryOrder(new DeliveryOrder());
                } else {
                    boolean hasItems;
                    deliveryOrderItemList = deleteCouponExpired(deliveryOrder);// validate Coupon
                    if (Objects.isNull(deliveryOrderItemList)) {
                        LOG.warning("deliveryOrderItemList is null NO HAY ITEMS AGREGADOS");
                        throw new ConflictException("NO HAY ITEMS AGREGADOS");
                    }

                    if (isScanAndGo) {
                        hasItems = deliveryOrderItemList.stream().filter(deliveryOrderItem -> Objects.nonNull(deliveryOrderItem)
                                && (Objects.isNull(deliveryOrderItem.getCoupon()) || !deliveryOrderItem.getCoupon())
                                && (Objects.nonNull(deliveryOrderItem.getScanAndGo()) && deliveryOrderItem.getScanAndGo())).findFirst().isPresent();
                    } else {
                        hasItems = deliveryOrderItemList.stream().filter(deliveryOrderItem -> Objects.nonNull(deliveryOrderItem) && (Objects.isNull(deliveryOrderItem.getCoupon()) || !deliveryOrderItem.getCoupon())).findFirst().isPresent();
                    }

//                    LOG.info("Busqueda Coupon. hasItems" + hasItems);
                    if (hasItems) {
//                        LOG.warning("Enviar a validar el carro de compras a Backend 3. " + new DateTime());
//                        LOG.info("idStoreGroup to core -> " + shoppingCartJson.getIdStoreGroup());
                        // validate scan and go
                        if (!deliveryOrderItemList.isEmpty()) {
                            // delete items scan and go if cart is normal
                            if (!isScanAndGo) {
                                deliveryOrderItemList.removeIf(item -> (item.getScanAndGo() != null && item.getScanAndGo()));
                            } else {
                                // only show items scan and go if scan and go is selected
                                deliveryOrder.setDeliveryType(DeliveryType.SCANANDGO);
                                shoppingCartJson.setDeliveryType(DeliveryType.SCANANDGO);
                                deliveryOrderItemList.removeIf(item -> ((Objects.isNull(item.getScanAndGo()) || !item.getScanAndGo()) && (Objects.isNull(item.getCoupon()) || !item.getCoupon())));
                            }
                        }

                        String orderRequest = Orders.createValidateOrderJson(shoppingCartJson.getId(), shoppingCartJson.getIdStoreGroup(), deliveryOrderItemList, shoppingCartJson.getSource(), shoppingCartJson.getDeliveryType(), shoppingCartJson.getTalonOneData()).toJSONString();
                        LOG.info(orderRequest);

                        Gson gson = new Gson();
                        ValidateOrderReq validateOrderReq = gson.fromJson(orderRequest, ValidateOrderReq.class);
                        OrderJson orderJSON = null;

                        if (Objects.nonNull(deliveryOrderItemList) && !deliveryOrderItemList.isEmpty()) {
                            //LOG.warning("Call backend 3 INIT: " + new DateTime());
                            // fix delivery type express and store 1000 OR 1001
                            if (validateOrderReq != null
                                    && validateOrderReq.getDeliveryType() != null
                                    && validateOrderReq.getDeliveryType().equals(DeliveryType.EXPRESS.getDeliveryType())) {
                                if (validateOrderReq.getStoreId() == 1000) {
                                    validateOrderReq.setDeliveryType(DeliveryType.NATIONAL.getDeliveryType());
                                } else if (validateOrderReq.getStoreId() == 1001) {
                                    validateOrderReq.setDeliveryType(DeliveryType.ENVIALOYA.getDeliveryType());
                                }
                            }

//                            inicio validando stock real
                            try {
                                Key<DeliveryOrder> deliveryOrderKey = Key.create(customerKey, DeliveryOrder.class, deliveryOrder.getIdDeliveryOrder());
                                for (int i = 0; i < validateOrderReq.getItems().size(); i++) {
                                    final int itemId = validateOrderReq.getItems().get(i).getItemId();
                                    final ItemAlgolia itemAlgolia = APIAlgolia.getItemAlgolia(itemId + "" + validateOrderReq.getStoreId());
                                    if (Objects.isNull(itemAlgolia.getTotalStock())) {
                                        LOG.info("order item to delete total stock is null ->" + itemId + validateOrderReq.getStoreId());
                                        validateOrderReq.getItems().remove(i);

                                        DeliveryOrderItem orderItem = deliveryOrderItemList
                                                .stream()
                                                .filter(item -> Objects.equals(item.getId(), Long.valueOf(itemId)))
                                                .findFirst().get();

                                        if (Objects.nonNull(orderItem)) {
                                            ofy().delete().entities(orderItem);
                                            deliveryOrderItemList.remove(orderItem);
                                        }

                                        continue;
                                    }
                                    final int quantitySold = validateOrderReq.getItems().get(i).getQuantityRequested();
                                    final int totalStock = itemAlgolia.getTotalStock();
//                                    LOG.info("IF ( quantitySold > totalStock ) : (" + quantitySold + " > " + totalStock + ") => [" + (quantitySold > totalStock) + "]");
                                    if (totalStock > 0 && quantitySold > totalStock) {
                                        try {
                                            DeliveryOrderItem deliveryOrderItem = ofy().load().type(DeliveryOrderItem.class).filter("idItem", Key.create(Item.class, itemId)).ancestor(Ref.create(deliveryOrderKey)).first().now();
//                                            LOG.info("deliveryOrderItem.getQuantitySold() : " + deliveryOrderItem.getQuantitySold());
                                            deliveryOrderItem.setQuantitySold(totalStock);
                                            ofy().save().entity(deliveryOrderItem);
                                        } catch (Exception e) {
                                            LOG.warning("Error No grave. al actualizar el detalle de la orden. Mensaje: " + e.getMessage());
                                        }
                                        validateOrderReq.getItems().get(i).setQuantityRequested(totalStock);
                                    }
                                }
                                // Validacion cantidades solicitadas vs stock real en tienda.
                                validateRequestedQuantityVsRealStockInStore(deliveryOrder, validateOrderReq.getStoreId());
                            } catch (Exception e) {
                                LOG.warning("Error al validar el stock vs la cantidad doslitada. " + e.getMessage());
                            }
//                            FIN validando stock real
                            if (Objects.isNull(validateOrderReq) || validateOrderReq.getItems().isEmpty()) {
                                return getEmptyDeliveryOrder(deliveryOrder);
                            }
                            validateOrderReq.setIdCustomerWebSafe(shoppingCartJson.getIdCustomerWebSafe());
                            Response<ValidateOrderBackend3> response = ApiGatewayService.get().validateOrder(validateOrderReq, TraceUtil.getXCloudTraceId(request));
                            orderJSON = Objects.nonNull(response) && Objects.nonNull(response.body()) ? response.body().getData() : null;
                            String encodedString = Base64.getEncoder().encodeToString(Objects.requireNonNull(orderJSON).toString().getBytes());
                            //LOG.warning("orderJSON Result: " + encodedString);
                            //LOG.warning("Call backend 3 END: " + new DateTime());
                        } else {
                            //LOG.warning("No hay items para el tipo de envio seleccionado.");
                            setQuantityInOrder(deliveryOrder, deliveryOrderProviderListToSave, deliveryOrderItemList, isScanAndGo);
                            return deliveryOrder;
                        }

                        if (Objects.isNull(orderJSON)) {
                            LOG.warning("El servicio [validateOrder] de validacion de orden devolvio una respuesta nula. NO HAY ITEMS AGREGADOS");
                            throw new ConflictException(Constants.NO_ADDED_ITEMS);
                        }
//                        LOG.info("orderJSON -> " + orderJSON.toStringJson());
                        if ((Objects.isNull(orderJSON.getItems()) && Objects.isNull(orderJSON.getProviders())) ||
                                (Objects.nonNull(orderJSON.getItems()) && orderJSON.getItems().isEmpty() && Objects.nonNull(orderJSON.getProviders()) && orderJSON.getProviders().isEmpty())) {
                            return getDeliveryOrderEmpty(shoppingCartJson, tipConfigOptional, customerKey, deliveryOrder);
                        }
                        //LOG.warning("Call Add item Init: " + new DateTime());
                        Key<DeliveryOrder> deliveryOrderKey = Key.create(customerKey, DeliveryOrder.class, deliveryOrder.getIdDeliveryOrder());
                        deliveryOrder.setSubTotalPrice(0d);
                        deliveryOrder.setOfferPrice(0d);

                        for (ItemAlgolia itemOrder : orderJSON.getItems()) {
//                            LOG.info("FIX_DS --- TEST ");
//                            LOG.info(itemOrder.toStringJson());
                            addDeliveryItemOrder(itemOrder, shoppingCartJson, deliveryOrderKey, deliveryOrderItemListToSave, deliveryOrder, null, deliveryOrderItemList);
                        }
                        // Agregar Coupon
                        deliveryOrderItemList.stream().filter(item -> item.getCoupon() != null && item.getCoupon()).forEach(
                                item -> {
                                    deliveryOrderItemListToSave.add(item);
                                    traditionalCoupon.setNameCoupon(Objects.nonNull(item.getMediaDescription()) ? item.getMediaDescription() : "Cupon");
                                    traditionalCoupon.setCouponMessage("Cupon");
                                    traditionalCoupon.setTalonOneOfferDescription(item.getMediaDescription());
                                    traditionalCoupon.setTypeNotificacion("Info");
                                }
                        );

                        if (Objects.nonNull(orderJSON.getProviders()) && !orderJSON.getProviders().isEmpty()) {
                            for (ProviderOrder provider : orderJSON.getProviders()) {
                                DeliveryOrderProvider providerOrder = new DeliveryOrderProvider(provider.getId(), provider.getName(), provider.getEmail(), provider.getDeliveryPrice());
                                for (ItemAlgolia deliveryOrderItem : provider.getItems()) {
                                    addDeliveryItemOrder(deliveryOrderItem, shoppingCartJson, deliveryOrderKey, providerOrder.getItemList(), deliveryOrder, providerOrder, deliveryOrderItemList);
                                }
                                providerOrder.setQuantityItem(provider.getItems().stream().mapToInt(item -> item.getQuantityRequested()).sum());
                                deliveryOrderProviderListToSave.add(providerOrder);
                            }
                        }
                        // delete delivery value if enable scan and go
                        if (isScanAndGo && (orderJSON.getDeliveryValue() > 0 || orderJSON.getProviderDeliveryValue() > 0)) {
                            orderJSON.setDeliveryValue(0);
                            orderJSON.setProviderDeliveryValue(0);
                        }

                        RequestSourceEnum source = ftdUtilities.getSourceFromRequestHeader(request);
                        // Set delivery value if applies for free delivery.
                        if (checkIfAppliesForFreeDelivery(deliveryOrder, deliveryOrderItemList, shoppingCartJson, source.name())) {
//                            LOG.info("El pedido APLICA para envio gratis.");
                            orderJSON.setDeliveryValue(0);
                        } else {
                            LOG.info("El pedido NO APLICA para envio gratis.");
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
//                            Asigna cantidad de Items de proveedor
                            deliveryOrder.setQuantityProviders(deliveryOrderProviderListToSave.stream().mapToInt(provider -> provider.getQuantityItem()).sum());
                        } else {
                            deliveryOrder.setQuantityProviders(0);
                        }
                        deliveryOrder.setQuantityFarmatodo(Objects.nonNull(deliveryOrderItemList) ? deliveryOrderItemList.stream().mapToInt(item -> item.getQuantitySold()).sum() : 0);

                        deliveryOrder.setQuantityFarmatodo(deliveryOrder.getQuantityFarmatodo() > 0 ? deliveryOrder.getQuantityFarmatodo() - deliveryOrder.getQuantityProviders() : deliveryOrder.getQuantityFarmatodo());


                        deliveryOrder.setTotalQuantity(deliveryOrder.getQuantityFarmatodo() + deliveryOrder.getQuantityProviders());
                        //LOG.warning("END call shopping cart : " + new DateTime());
                    } else {
                        //LOG.warning("el carro de compras por el momento solo contiene cupones, no es necesario mandarlo a validar.");
                        for (DeliveryOrderItem item : deliveryOrderItemList) {
                            if (item.getCoupon() != null && item.getCoupon()) {
                                deliveryOrderItemListToSave.add(item);
                            }
                        }
                        deliveryOrder = getEmptyDeliveryOrder(deliveryOrder);
                    }
                    // delete duplicates
                    deleteDeliveryOrderDuplicates(deliveryOrderItemList, deliveryOrderProviderListToSave);

                    if (!isScanAndGo) {
                        restrictItemsAndSave(deliveryOrderItemListToSave, shoppingCartJson.getIdStoreGroup());
                    }
                    ofy().save().entity(deliveryOrder);
//                    LOG.info("data --> " + deliveryOrder);
                    deliveryOrder.setItemList(deliveryOrderItemListToSave);
                    deliveryOrder.setProviderList(deliveryOrderProviderListToSave);
                }
            }
            //LOG.warning("INIT calculate totals : " + new DateTime());
            deliveryOrder.setDeliveryType(shoppingCartJson.getDeliveryType());
            deliveryOrder.setHighlightedItems(new ArrayList<>());      // Hot Fix se eliminan los item highlight del servicio

            //Set Dynamic delivery label...
            int idStoreGroup = 0;
            try {
                idStoreGroup = APIAlgolia.getDefaultStoreIdByStoreId(shoppingCartJson.getIdStoreGroup());
                deliveryOrder.setDeliveryLabel(APIAlgolia.getDynamicDeliveryLabel(idStoreGroup));
            } catch (AlgoliaException e) {
                LOG.warning("No fue posible consultar la configuracion del Label del Carrito: " + e);
                deliveryOrder.setDeliveryLabel("Domicilio");
            }
            //Set dynamic delivery Time Label...
            try {
                DeliveryTimeLabelTemplate deliveryTimeLabelTemplate = getCustomDeliveryTimeLabelTemplateForThisOrder(deliveryOrder);
                deliveryOrder.setDeliveryTimeLabel(deliveryTimeLabelTemplate);
            } catch (Exception e) {
                LOG.warning("No fue posible consultar la configuracion del DeliveryTimeLabelTemplate del Carrito: " + e);
            }
            validateIfOrderOnlyCouponProviders(deliveryOrder);
            // add quantities
            setQuantityInOrder(deliveryOrder, deliveryOrderProviderListToSave, deliveryOrderItemList, isScanAndGo);
            // add barcode Response
            addBarcodeInShoppingCart(deliveryOrder.getItemList(), String.valueOf(shoppingCartJson.getIdStoreGroup()));
            //LOG.warning("END calculate totals : " + new DateTime());

            boolean isFullProvider = isFullProvider(deliveryOrder, tipConfigOptional);
//            LOG.info("Is fullProvider? -> " + isFullProvider);
            // delete tip if is full provider and pre-selected tip
            if (isFullProvider && tipConfigOptional.isPresent()) {
                deleteTips(tipConfigOptional.get(), deliveryOrder, customerKey);
                hideTipItemList(deliveryOrder, tipConfigOptional);
            }

            // add tip
            if (!isScanAndGo && shoppingCartJson.getDeliveryType().equals(DeliveryType.EXPRESS) && !isFullProvider) {
                Optional<Tip> optionalTip = getOptionalTip(shoppingCartJson.getIdStoreGroup(), tipConfigOptional);

                if (optionalTip.isPresent() && tipConfigOptional.isPresent()) {
                    double tipPrice = getTipPriceForOrder(deliveryOrder, tipConfigOptional.get()); // add tip price/
                    hideTipItemList(deliveryOrder, tipConfigOptional);
                    deliveryOrder.setTip(optionalTip.get());
                    if (tipPrice > 0) {  // rest tip item
                        deliveryOrder.setTipPrice(tipPrice);
                        // calculate subtotal with tip
                        deliveryOrder.setSubTotalPrice(deliveryOrder.getSubTotalPrice() - tipPrice);
                        deliveryOrder.setTotalQuantity(deliveryOrder.getTotalQuantity() - 1);
                        deliveryOrder.setQuantityFarmatodo(deliveryOrder.getQuantityFarmatodo() - 1);
                    }
                }
            }
            try {
                TalonOneService talonOneService = new TalonOneService();
                String keyCache = shoppingCartJson.getIdCustomerWebSafe() + Constants.KEY_COUPON_CACHE;
                AnswerDeduct coupon = existCouponInRedis(shoppingCartJson.getIdCustomerWebSafe(), keyCache, Constants.INDEX_REDIS_FOURTEEN);
                deliveryOrder = talonOneService.sendOrderToTalonOne(deliveryOrder, shoppingCartJson, coupon);
            } catch (Exception e) {
                LOG.info("Error total de Talon One: " + e);
            }
            User user = users.findUserByKey(customerKey);
            deductDiscount(shoppingCartJson.getIdCustomerWebSafe(), deliveryOrder, user.getId(), traditionalCoupon);
            new FarmaCredits().calculateNewPriceWithCredits(deliveryOrder, (long) shoppingCartJson.getId());
            return deliveryOrder;
        } catch (Exception e) {
            LOG.warning("method priceDelivery: Error: " + e);
            try {
                sendPriceDeliveryMessage(shoppingCartJson);
                if (APIAlgolia.getDeleteCartConfig()) {
                    CustomerEndpoint customerEndpoint = new CustomerEndpoint();
                    customerEndpoint.deleteShoppingCart("12345", shoppingCartJson.getId());
                }

            } catch (Exception ex) {
                LOG.warning("method priceDelivery: No fue posible envia la notificacion de SMS: " + ex);
            }
            throw new ConflictException("Error al realizar el calculo en el carrito.");
        }
    }


    @ApiMethod(name = "priceDeliveryOrderV2", path = "/orderEndpoint/v2/priceDeliveryOrder", httpMethod = ApiMethod.HttpMethod.POST)
    public DeliveryOrder priceDeliveryOrderV2(final ShoppingCartJson shoppingCartJson, HttpServletRequest request) throws ConflictException, IOException, BadRequestException, InternalServerErrorException, NotFoundException, UnauthorizedException, AlgoliaException {
        LOG.info("method: priceDeliveryOrder request->" + shoppingCartJson.toStringJson());
        try {
            com.imaginamos.farmatodo.model.talonone.Coupon traditionalCoupon = new com.imaginamos.farmatodo.model.talonone.Coupon();
            Guard.againtsNull(shoppingCartJson, "shoppingCartJson");
            Guard.againtsNullOrEmpty(shoppingCartJson.getToken(), "Token");
            Guard.againtsNullOrEmpty(shoppingCartJson.getTokenIdWebSafe(), "TokenIdWebSafe");
            Guard.againtsNullOrEmpty(shoppingCartJson.getIdCustomerWebSafe(), "IdCustomerWebSafe");
            Guard.againtsZero(shoppingCartJson.getIdStoreGroup(), "IdStoreGroup");
            Guard.againtsNullOrEmpty(shoppingCartJson.getSource(), "Source");

            if (Guard.isWebWithoutDeliveryType(shoppingCartJson)) {
                throw new BadRequestException("BadRequest [deliveryType] is required for web");
            }

            if (Guard.isDeliveryTypePresent(shoppingCartJson))
                shoppingCartJson.setDeliveryType(DeliveryType.EXPRESS);

            if (!authenticate.isValidToken(shoppingCartJson.getToken(), shoppingCartJson.getTokenIdWebSafe())) {
//                LOG.info("method: priceDeliveryOrder() --> ConflictException [bad credentials]");
                throw new ConflictException(Constants.INVALID_TOKEN);
            }
            //LOG.warning((shoppingCartJson.getTip() == null) ? "PRIME - TIP -> Propina es null o no se envia" : "PRIME - TIP -> Propina tiene un valor de >>>> " + shoppingCartJson.getTip());

            OrderStoreService orderStoreService = new OrderStoreService();
            shoppingCartJson.setDeliveryType(orderStoreService.putStore26withExpress(shoppingCartJson));

            Optional<TipConfig> tipConfigOptional = GrowthBookConfigLoader.getTipsConfig(Constants.CUSTOMER_ANONYMOUS);
            shoppingCartJson.setIdStoreGroup(APIAlgolia.getDefaultStoreIdByStoreId(shoppingCartJson.getIdStoreGroup()));
//            LOG.info("idStoreGroup request -> " + shoppingCartJson.getIdStoreGroup()+" Request changue to -> " + shoppingCartJson.toStringJson());

            final List<DeliveryOrderItem> deliveryOrderItemListToSave = new ArrayList<>();
            List<DeliveryOrderProvider> deliveryOrderProviderListToSave = new ArrayList<>();
            List<DeliveryOrderItem> deliveryOrderItemList = new ArrayList<>();

            if (Guard.isNationalOrEnvialoYa(shoppingCartJson)) {
                shoppingCartJson.setIdStoreGroup(shoppingCartJson.getDeliveryType().getDefaultStore());
            }
//            LOG.info("idStoreGroup assigned -> " + shoppingCartJson.getIdStoreGroup());

            boolean isScanAndGo = isScanAndGo(shoppingCartJson.getDeliveryType());
            Key<User> customerKey = Key.create(shoppingCartJson.getIdCustomerWebSafe());
            DeliveryOrder deliveryOrder = null;
            try {
                deliveryOrder = getCourrentDeliveryOrder(customerKey);
            } catch (ApiProxy.ApiDeadlineExceededException apiDeadlineExceededException) {
                deliveryOrder = getCourrentDeliveryOrder(customerKey);
            } catch (Exception e) {
                throw e;
            }

            if (deliveryOrder == null) {
//                LOG.info("No existe un carrito activo para el cliente.");
                deliveryOrder = getEmptyDeliveryOrder(new DeliveryOrder());
            } else {
//                LOG.info("Data + Carrito " + deliveryOrder);
//                LOG.info("Existe un carrito activo para el cliente, se procede a validar.Datastore deliveryOrder.deliveryType :" + deliveryOrder.getDeliveryType());
                deliveryOrderItemList = ofy().load().type(DeliveryOrderItem.class).ancestor(deliveryOrder).list();
                List<DeliveryOrderItem> deliveryOrderItemListSampling;
                if (Objects.nonNull(deliveryOrderItemList)) {
                    deliveryOrderItemListSampling = deliveryOrderItemList.stream().filter(itm -> (itm.getFullPrice() == 1.0D)).collect(Collectors.toList());
                    if (Objects.nonNull(deliveryOrderItemListSampling)) {
                        deliveryOrderItemListSampling.stream().forEach(itemSampling -> ofy().delete().entity(itemSampling).now());
                        deliveryOrderItemList = ofy().load().type(DeliveryOrderItem.class).ancestor(deliveryOrder).list();
                    }
                }


                validateDuplicateItems(deliveryOrderItemList);  // Fix items duplicators

                List<DeliveryOrderProvider> deliveryOrderProviderList = new ArrayList<>();
                if (!isScanAndGo) {// solo si no es scan-go
                    deliveryOrderProviderList = ofy().load().type(DeliveryOrderProvider.class).ancestor(deliveryOrder).list();
                    // Validacion cantidades solicitadas vs stock real en tienda, exceptuando scan and go.
                    validateRequestedQuantityVsRealStockInStore(deliveryOrder, shoppingCartJson.getIdStoreGroup());
                }

                // ADD TIP TO cart  delete tips if exists in cart and is not express delivery type.
                if (tipConfigOptional.isPresent() && !shoppingCartJson.getDeliveryType().equals(DeliveryType.EXPRESS)) {
                    //LOG.warning("Eliminar propinas !EXPRESS");
                    deleteTips(tipConfigOptional.get(), deliveryOrder, customerKey);
                }

                // si el carrito ya tiene propina y no viene valor, obtener el valor de esta y eliminar todas las propinas y agregar solo 1.
                if (tipConfigOptional.isPresent()) {
                    Optional<Double> optionalTip = fixTipMaxQuantity(tipConfigOptional.get(), deliveryOrder, customerKey);
                    if (optionalTip.isPresent() && optionalTip.get() > 0 && (shoppingCartJson.getTip() == null || shoppingCartJson.getTip() == 0)) {
                        deleteTips(tipConfigOptional.get(), deliveryOrder, customerKey);
                        shoppingCartJson.setTip(optionalTip.get().floatValue());
//                        LOG.info("Fix_quantity TIP, new price => " + shoppingCartJson.getTip());
                    }
                }


                // if city not use TIP, delete TIPS.

                deleteTipsIfCityNotConfig(tipConfigOptional, deliveryOrder, customerKey, shoppingCartJson.getIdStoreGroup());

                if (notAllowedTips(Long.valueOf(shoppingCartJson.getId())) && tipConfigOptional.isPresent()) {
//                    Eliminar propinas notAllowedTips
                    deleteTips(tipConfigOptional.get(), deliveryOrder, customerKey);
                }

//                if (isOrderPrimeV2(deliveryOrder) && tipConfigOptional.isPresent()) {
//                    LOG.info("Eliminar propinas isOrderPrimeV2");
//                    deleteTips(tipConfigOptional.get(), deliveryOrder, customerKey);
//                }

//
                if (tipConfigOptional.isPresent() && shoppingCartJson.getTip() != null && shoppingCartJson.getTip() >= 0 && shoppingCartJson.getDeliveryType().equals(DeliveryType.EXPRESS)) {
                    boolean cartHaveTip = addTipToOrder(tipConfigOptional.get(), shoppingCartJson, deliveryOrder, customerKey);
//                    LOG.info("Se agrega propina al carrito?? => " + cartHaveTip);
                }

                if (Objects.isNull(deliveryOrderItemList) && Objects.isNull(deliveryOrderProviderList)) {
//                    LOG.warning("deliveryOrderItemList is null");
                    throw new ConflictException("NO HAY ITEMS AGREGADOS");
                }
                //LOG.warning("items size: [" + (Objects.nonNull(deliveryOrderItemList) ? deliveryOrderItemList.size() : 0) + "]  " + new DateTime());
                //LOG.warning("providers size: [" + (Objects.nonNull(deliveryOrderProviderList) ? deliveryOrderProviderList.size() : 0) + "]");
                if (deliveryOrderItemList.isEmpty() && deliveryOrderProviderList.isEmpty()) {
                    deliveryOrder = getEmptyDeliveryOrder(new DeliveryOrder());
                } else {
                    boolean hasItems;
                    // validate Coupon

                    deliveryOrderItemList = deleteCouponExpired(deliveryOrder);

                    if (Objects.isNull(deliveryOrderItemList)) {
//                        LOG.warning("deliveryOrderItemList is null NO HAY ITEMS AGREGADOS");
                        throw new ConflictException("NO HAY ITEMS AGREGADOS");
                    }

                    if (isScanAndGo) {
                        hasItems = deliveryOrderItemList.stream().filter(deliveryOrderItem -> Objects.nonNull(deliveryOrderItem)
                                && (Objects.isNull(deliveryOrderItem.getCoupon()) || !deliveryOrderItem.getCoupon())
                                && (Objects.nonNull(deliveryOrderItem.getScanAndGo()) && deliveryOrderItem.getScanAndGo())).findFirst().isPresent();
                    } else {
                        hasItems = deliveryOrderItemList.stream().filter(deliveryOrderItem -> Objects.nonNull(deliveryOrderItem) && (Objects.isNull(deliveryOrderItem.getCoupon()) || !deliveryOrderItem.getCoupon())).findFirst().isPresent();
                    }

//                    LOG.info("Busqueda Coupon. hasItems" + hasItems);
                    if (hasItems) {
                        //LOG.warning("Enviar a validar el carro de compras a Backend 3. " + new DateTime());
//                        LOG.info("idStoreGroup to core -> " + shoppingCartJson.getIdStoreGroup());

                        // validate scan and go

                        if (!deliveryOrderItemList.isEmpty()) {
                            // delete items scan and go if cart is normal
                            if (!isScanAndGo) {

                                deliveryOrderItemList.removeIf(item -> (item.getScanAndGo() != null && item.getScanAndGo()));
                            } else {
                                // only show items scan and go if scan and go is selected
                                deliveryOrder.setDeliveryType(DeliveryType.SCANANDGO);
                                shoppingCartJson.setDeliveryType(DeliveryType.SCANANDGO);
                                deliveryOrderItemList.removeIf(item -> ((Objects.isNull(item.getScanAndGo()) || !item.getScanAndGo()) && (Objects.isNull(item.getCoupon()) || !item.getCoupon())));
                            }
                        }

                        String orderRequest = Orders.createValidateOrderJson(shoppingCartJson.getId(), shoppingCartJson.getIdStoreGroup(), deliveryOrderItemList, shoppingCartJson.getSource(), shoppingCartJson.getDeliveryType(), shoppingCartJson.getTalonOneData()).toJSONString();
                        LOG.info(orderRequest);

                        Gson gson = new Gson();
                        ValidateOrderReq validateOrderReq = gson.fromJson(orderRequest, ValidateOrderReq.class);
                        OrderJson orderJSON = null;


                        if (Objects.nonNull(deliveryOrderItemList) && !deliveryOrderItemList.isEmpty()) {
                            //LOG.warning("Call backend 3 INIT: " + new DateTime());


                            // fix delivery type express and store 1000 OR 1001
                            if (validateOrderReq != null
                                    && validateOrderReq.getDeliveryType() != null
                                    && validateOrderReq.getDeliveryType().equals(DeliveryType.EXPRESS.getDeliveryType())) {

                                if (validateOrderReq.getStoreId() == 1000) {
                                    validateOrderReq.setDeliveryType(DeliveryType.NATIONAL.getDeliveryType());
                                } else if (validateOrderReq.getStoreId() == 1001) {
                                    validateOrderReq.setDeliveryType(DeliveryType.ENVIALOYA.getDeliveryType());
                                }
                            }

//                            INICIO validando stock real
                            try {
                                Key<DeliveryOrder> deliveryOrderKey = Key.create(customerKey, DeliveryOrder.class, deliveryOrder.getIdDeliveryOrder());

                                for (int i = 0; i < validateOrderReq.getItems().size(); i++) {
                                    final int itemId = validateOrderReq.getItems().get(i).getItemId();

                                    final ItemAlgolia itemAlgolia = APIAlgolia.getItemAlgolia(itemId + "" + validateOrderReq.getStoreId());

                                    if (Objects.isNull(itemAlgolia.getTotalStock()) || GuardAlgolia.validationItemsAlgoliaCart(itemAlgolia)) {
//                                        LOG.info("order item to delete total stock is null ->" + itemId + validateOrderReq.getStoreId());
                                        validateOrderReq.getItems().remove(i);

                                        DeliveryOrderItem orderItem = deliveryOrderItemList
                                                .stream()
                                                .filter(item -> Objects.equals(item.getId(), Long.valueOf(itemId)))
                                                .findFirst().get();

                                        if (Objects.nonNull(orderItem)) {
                                            ofy().delete().entities(orderItem);
                                            deliveryOrderItemList.remove(orderItem);
                                        }

                                        continue;
                                    }
                                    final int quantitySold = validateOrderReq.getItems().get(i).getQuantityRequested();
                                    final int totalStock = itemAlgolia.getTotalStock();

//                                    LOG.info("IF ( quantitySold > totalStock ) : (" + quantitySold + " > " + totalStock + ") => [" + (quantitySold > totalStock) + "]");
                                    if (totalStock > 0 && quantitySold > totalStock && !isScanAndGo) {
                                        try {
                                            DeliveryOrderItem deliveryOrderItem = ofy().load().type(DeliveryOrderItem.class).filter("idItem", Key.create(Item.class, itemId)).ancestor(Ref.create(deliveryOrderKey)).first().now();
//                                            LOG.info("deliveryOrderItem.getQuantitySold() : " + deliveryOrderItem.getQuantitySold());
                                            deliveryOrderItem.setQuantitySold(totalStock);

                                            ofy().save().entity(deliveryOrderItem);
                                        } catch (Exception e) {
                                            LOG.warning("Error No grave. al actualizar el detalle de la orden. Mensaje: " + e.getMessage());
                                        }

                                        validateOrderReq.getItems().get(i).setQuantityRequested(totalStock);
                                    }
                                }

                                // Validacion cantidades solicitadas vs stock real en tienda, exceptuando scan and go.
                                if (!isScanAndGo) {
                                    validateRequestedQuantityVsRealStockInStore(deliveryOrder, validateOrderReq.getStoreId());
                                }

                            } catch (Exception e) {
                                LOG.warning("Error al validar el stock vs la cantidad solicitada. " + e.getMessage());
                            }
//                           FIN validando stock real
                            //TODO: shopping-cart priceDelivery
                            if (Objects.isNull(validateOrderReq) || validateOrderReq.getItems().isEmpty()) {
                                return getEmptyDeliveryOrder(deliveryOrder);
                            }

                            //validar propina cuando solo hay prime
                            validateTipAndPrime(tipConfigOptional, deliveryOrderItemList, customerKey, shoppingCartJson.getIdStoreGroup(), deliveryOrder, validateOrderReq);

                            validateOrderReq.setIdCustomerWebSafe(shoppingCartJson.getIdCustomerWebSafe());
                            validateOrderReq.setDaneCodeCustomer(Objects.nonNull(shoppingCartJson.getDaneCodeCustomer()) ? shoppingCartJson.getDaneCodeCustomer() : "");
                            validateOrderReq.setAddressCustomer(Objects.nonNull(shoppingCartJson.getAddressCustomer()) ? shoppingCartJson.getAddressCustomer() : "");

                            if (Objects.nonNull(shoppingCartJson.getFarmaCredits())) {
                                validateOrderReq.setFarmaCredits(shoppingCartJson.getFarmaCredits());
                            }

                            //set payment card id
                            validateOrderReq.setPaymentCardId(Objects.isNull(shoppingCartJson.getPaymentCardId()) ? 0 : shoppingCartJson.getPaymentCardId());

                            new TalonAttributes().addTalonAttributes(shoppingCartJson, validateOrderReq);

                            if (Objects.nonNull(shoppingCartJson.getTalonOneData())) {
                                validateOrderReq.setTalonOneData(shoppingCartJson.getTalonOneData());
                            }

                            if (shoppingCartJson.getNearbyStores() != null && !shoppingCartJson.getNearbyStores().isEmpty()) {
                                validateOrderReq.setNearbyStores(shoppingCartJson.getNearbyStores());
                            }

                            Response<ValidateOrderBackend3> response = ApiGatewayService.get().validateOrder(validateOrderReq, TraceUtil.getXCloudTraceId(request));
                            
                            if (!response.isSuccessful()) {
                                response = ApiGatewayService.get().validateOrderV2(validateOrderReq, TraceUtil.getXCloudTraceId(request));
                                
                                if (!response.isSuccessful()){
                                    //LOG.warning("Entra response 2: " +  response.isSuccessful());
                                    throw new ConflictException("Error al validar la orden.");
                                }
                                
                            }
                            LOG.info("response priceDeliveryOrder bck3 2:" + new Gson().toJson(response.body().getData()));
                            orderJSON = Objects.nonNull(response) && Objects.nonNull(response.body()) && Objects.nonNull(response.body().getData()) ? response.body().getData() : null;
                            Object item4 = Objects.nonNull(response) && Objects.nonNull(response.body()) ? response.body().getData() : null;
                            //LOG.warning("orderJSON Result: " + item4);
                            //LOG.warning("Call backend 3 END: " + new DateTime());
                        } else {
                            //LOG.warning("No hay items para el tipo de envio seleccionado.");
                            setQuantityInOrder(deliveryOrder, deliveryOrderProviderListToSave, deliveryOrderItemList, isScanAndGo);
                            return deliveryOrder;
                        }

                        if (Objects.isNull(orderJSON)) {
                            LOG.warning("El servicio [validateOrder shopping cart] devolvio una respuesta ok pero los items no estan disponibles.");
                            if (APIAlgolia.getDeleteCartConfig()) {
                                CustomerEndpoint customerEndpoint = new CustomerEndpoint();
                                customerEndpoint.deleteShoppingCart("12345", shoppingCartJson.getId());
                            }
                            return new DeliveryOrder();
                        }
//                        LOG.info("orderJSON -> " + orderJSON.toStringJson());

                        if ((Objects.isNull(orderJSON.getItems()) && Objects.isNull(orderJSON.getProviders())) ||
                                (Objects.nonNull(orderJSON.getItems()) && orderJSON.getItems().isEmpty() && Objects.nonNull(orderJSON.getProviders()) && orderJSON.getProviders().isEmpty())) {
                            return getDeliveryOrderEmpty(shoppingCartJson, tipConfigOptional, customerKey, deliveryOrder);
                        }
                        //LOG.warning("Call Add item Init: " + new DateTime());
                        Key<DeliveryOrder> deliveryOrderKey = Key.create(customerKey, DeliveryOrder.class, deliveryOrder.getIdDeliveryOrder());
                        deliveryOrder.setSubTotalPrice(0d);
                        deliveryOrder.setOfferPrice(0d);

                        deliveryOrder.setShoppingCartCourierCost(orderJSON.getShoppingCartCourierCost());
                        
                        for (ItemAlgolia itemOrder : orderJSON.getItems()) {
                            ItemAlgolia itAlgolia = APIAlgolia.getItemAlgolia(itemOrder.getId() + "" + validateOrderReq.getStoreId());
                            if (itAlgolia != null) {
                                itemOrder.setColor(itAlgolia.getColor());
                                itemOrder.setUrl(itAlgolia.getUrl());
                                itemOrder.setCustomTag(itAlgolia.getCustomTag());
                                itemOrder.setFilter(itAlgolia.getFilter());
                                itemOrder.setFilterType(itAlgolia.getFilterType());
                                itemOrder.setFilterCategories(Objects.nonNull(itAlgolia.getFilterCategories()) ? itAlgolia.getFilterCategories() : "");
                                itemOrder.setPrimePrice(Objects.nonNull(itAlgolia.getPrimePrice()) ? itAlgolia.getPrimePrice() : 0.0);
                                itemOrder.setPrimeDescription(Objects.nonNull(itAlgolia.getPrimeDescription()) ? itAlgolia.getPrimeDescription() : "");
                                itemOrder.setPrimeTextDiscount(Objects.nonNull(itAlgolia.getPrimeTextDiscount()) ? itAlgolia.getPrimeTextDiscount() : "");
                                itemOrder.setRms_deparment(Objects.nonNull(itAlgolia.getRms_deparment()) ? itAlgolia.getRms_deparment() : "");
                                itemOrder.setRms_class(Objects.nonNull(itAlgolia.getRms_class()) ? itAlgolia.getRms_class() : "");
                                itemOrder.setRms_subclass(Objects.nonNull(itAlgolia.getRms_subclass()) ? itAlgolia.getRms_subclass() : "");
                                itemOrder.setRms_group(Objects.nonNull(itAlgolia.getRms_group()) ? itAlgolia.getRms_group() : "");
                                itemOrder.setDepartments(Objects.nonNull(itAlgolia.getDepartments()) ? itAlgolia.getDepartments() : Collections.emptyList());
                                itemOrder.setRequirePrescriptionImage(Objects.nonNull(itAlgolia.isRequirePrescriptionImage()) ? itAlgolia.isRequirePrescriptionImage() : false);
                            }
//                            LOG.info( "FIX_DS --- TEST " + itemOrder);
                            addDeliveryItemOrder(itemOrder, shoppingCartJson, deliveryOrderKey, deliveryOrderItemListToSave, deliveryOrder, null, deliveryOrderItemList);
                        }


                        // Agregar Coupon

                        deliveryOrderItemList.stream().filter(item -> item.getCoupon() != null && item.getCoupon()).forEach(
                                item -> {
                                    deliveryOrderItemListToSave.add(item);
                                    traditionalCoupon.setNameCoupon(Objects.nonNull(item.getMediaDescription()) ? item.getMediaDescription() : "Cupon");
                                    traditionalCoupon.setCouponMessage("Cupon");
                                    traditionalCoupon.setTalonOneOfferDescription(item.getMediaDescription());
                                    traditionalCoupon.setTypeNotificacion("Info");
                                }
                        );


                        if (Objects.nonNull(orderJSON.getProviders()) && !orderJSON.getProviders().isEmpty()) {
                            for (ProviderOrder provider : orderJSON.getProviders()) {
                                DeliveryOrderProvider providerOrder = new DeliveryOrderProvider(provider.getId(), provider.getName(), provider.getEmail(), provider.getDeliveryPrice());
                                for (ItemAlgolia deliveryOrderItem : provider.getItems()) {
                                    //add filters optical
                                    String filters = null;
                                    try {
                                        filters = deliveryOrderItemList.stream().filter(item -> Objects.equals(item.getId(), Long.parseLong(deliveryOrderItem.getId()))).findFirst().get().getFiltersOptical();
                                        LOG.info("Filtros -> " + filters);
                                    } catch (Exception e) {
                                        LOG.info("Ocurrio un problema al traer los filtros. ERROR -> " + e.getMessage());
                                    }
                                    if (Objects.nonNull(filters)) {
                                        deliveryOrderItem.setFiltersOptical(filters);
                                    }

                                    addDeliveryItemOrder(deliveryOrderItem, shoppingCartJson, deliveryOrderKey, providerOrder.getItemList(), deliveryOrder, providerOrder, deliveryOrderItemList);
                                }
                                providerOrder.setQuantityItem(provider.getItems().stream().mapToInt(item -> item.getQuantityRequested()).sum());
                                deliveryOrderProviderListToSave.add(providerOrder);
                            }

                        }
                        // delete delivery value if enable scan and go
                        if (isScanAndGo && (orderJSON.getDeliveryValue() > 0 || orderJSON.getProviderDeliveryValue() > 0)) {
                            orderJSON.setDeliveryValue(0);
                            orderJSON.setProviderDeliveryValue(0);
                        }

                        RequestSourceEnum source = ftdUtilities.getSourceFromRequestHeader(request);

                        deliveryOrder.setPrimeDeliveryValue(String.valueOf(orderJSON.getDeliveryValue()));


                        // Set delivery value if applies for free delivery.
                        if (checkIfAppliesForFreeDelivery(deliveryOrder, deliveryOrderItemList, shoppingCartJson, source.name())) {
//                          El pedido APLICA para envio gratis.
                            orderJSON.setDeliveryValue(0);
                        } else if (isOrderOnlyPrime(deliveryOrderItemList, deliveryOrderProviderList)) {
//                          El pedido APLICA para envio gratis ya que tiene solo items de SUSCRIPCION PRIME.
                            orderJSON.setDeliveryValue(0);
                        } else {
                            LOG.info("El pedido NO APLICA para envio gratis.");
                        }

                        deliveryOrder.setOfferPrice(deliveryOrder.getOfferPrice() + orderJSON.getGlobalDiscount());
                        deliveryOrder.setWeight(orderJSON.getWeight());
                        deliveryOrder.setLowerRangeWeight(orderJSON.getLowerRangeWeight());
                        deliveryOrder.setTopRangeWeight(orderJSON.getTopRangeWeight());
                        deliveryOrder.setDeliveryPrice(orderJSON.getDeliveryValue());
                        LOG.info("deliveryOrder.getDeliveryPrice() -> " + deliveryOrder.getDeliveryPrice());
                        deliveryOrder.setRegisteredOffer(orderJSON.getRegisteredDiscount());

                        double shippingCostTotal = Objects.nonNull(orderJSON.getShoppingCartCourierCost()) ? orderJSON.getShoppingCartCourierCost().getShippingCostTotal() : 0;

                        deliveryOrder.setTotalPrice(deliveryOrder.getSubTotalPrice() - deliveryOrder.getOfferPrice() + (orderJSON.getDeliveryValue() + orderJSON.getProviderDeliveryValue()) + shippingCostTotal);
                        // Campos nuevos proveedores
                        deliveryOrder.setProviderDeliveryPrice(orderJSON.getProviderDeliveryValue());
                        LOG.info("deliveryOrder.getProviderDeliveryPrice() -> " + deliveryOrder.getProviderDeliveryPrice());
                        deliveryOrder.setTotalDelivery(orderJSON.getDeliveryValue() + orderJSON.getProviderDeliveryValue());
                        if (Objects.nonNull(deliveryOrderProviderListToSave) && !deliveryOrderProviderListToSave.isEmpty()) {
                            //LOG.warning(" Asigna cantidad de Items de proveedor. ");
                            deliveryOrder.setQuantityProviders(deliveryOrderProviderListToSave.stream().mapToInt(provider -> provider.getQuantityItem()).sum());
                        } else {
                            deliveryOrder.setQuantityProviders(0);
                        }
                        deliveryOrder.setQuantityFarmatodo(Objects.nonNull(deliveryOrderItemList) ? deliveryOrderItemList.stream().mapToInt(item -> item.getQuantitySold()).sum() : 0);

                        deliveryOrder.setQuantityFarmatodo(deliveryOrder.getQuantityFarmatodo() > 0 ? deliveryOrder.getQuantityFarmatodo() - deliveryOrder.getQuantityProviders() : deliveryOrder.getQuantityFarmatodo());


                        deliveryOrder.setTotalQuantity(deliveryOrder.getQuantityFarmatodo() + deliveryOrder.getQuantityProviders());
                        //LOG.warning("END call shopping cart : " + new DateTime());
                    } else {
                        //LOG.warning("el carro de compras por el momento solo contiene cupones, no es necesario mandarlo a validar.");
                        for (DeliveryOrderItem item : deliveryOrderItemList) {
                            if (item.getCoupon() != null && item.getCoupon()) {
                                deliveryOrderItemListToSave.add(item);
                            }
                        }
                        deliveryOrder = getEmptyDeliveryOrder(deliveryOrder);
                    }

                    // delete duplicates
                    deleteDeliveryOrderDuplicates(deliveryOrderItemList, deliveryOrderProviderListToSave);

                    // TODO: Pronado si funciona mejor con el guardado asincrono
                    //Key<DeliveryOrder> deliveryOrderKey = ofy().save().entity(deliveryOrder);
                    if (!isScanAndGo) {
                        restrictItemsAndSave(deliveryOrderItemListToSave, shoppingCartJson.getIdStoreGroup());
                    }

                    ofy().save().entity(deliveryOrder);
//                    LOG.info("data --> " + deliveryOrder);
                    deliveryOrder.setItemList(deliveryOrderItemListToSave);
                    deliveryOrder.setProviderList(deliveryOrderProviderListToSave);

                    // add delivery time optics
                    if (!deliveryOrder.getProviderList().isEmpty()) {
                        OpticsServices opticsServices = new OpticsServices();
                        int mainIdStore = APIAlgolia.getDefaultStoreIdByStoreId(shoppingCartJson.getIdStoreGroup());
                        opticsServices.getDeliveryTimeOpticsShoppingCart(deliveryOrder, mainIdStore);
                        try {
                            // add opticalFilter optics
                            LOG.info("Seteando los filtros a los items de proveedor.");
                            setOpticalItemFiltersProvider(deliveryOrder.getProviderList());
                        } catch (Exception e) {
                            LOG.info("Ocurrio un error seteando los filtros.");
                        }
                    }
                }
            }
            //LOG.warning("INIT calculate totals : " + new DateTime());
            deliveryOrder.setDeliveryType(shoppingCartJson.getDeliveryType());

            // Hot Fix se eliminan los item highlight del servicio
            deliveryOrder.setHighlightedItems(new ArrayList<>());

            //Set Dynamic delivery label...
            int idStoreGroup = 0;
            try {
                idStoreGroup = APIAlgolia.getDefaultStoreIdByStoreId(shoppingCartJson.getIdStoreGroup());
                deliveryOrder.setDeliveryLabel(APIAlgolia.getDynamicDeliveryLabel(idStoreGroup));
            } catch (AlgoliaException e) {
                LOG.warning("No fue posible consultar la configuracion del Label del Carrito: " + e.getMessage());
                deliveryOrder.setDeliveryLabel("Domicilio");
            }


            //Set dynamic delivery Time Label...
            try {
                DeliveryTimeLabelTemplate deliveryTimeLabelTemplate = getCustomDeliveryTimeLabelTemplateForThisOrder(deliveryOrder);
                deliveryOrder.setDeliveryTimeLabel(deliveryTimeLabelTemplate);
            } catch (Exception e) {
                LOG.warning("No fue posible consultar la configuracion del DeliveryTimeLabelTemplate del Carrito: " + e.getMessage());
            }
            validateIfOrderOnlyCouponProviders(deliveryOrder);
            // add quantities
            //-----Revisa si cantidad de Tips es 1
            checkTipsQuantity(deliveryOrderItemList);

            setQuantityInOrder(deliveryOrder, deliveryOrderProviderListToSave, deliveryOrderItemList, isScanAndGo);
            // add barcode Response
            addBarcodeInShoppingCart(deliveryOrder.getItemList(), String.valueOf(shoppingCartJson.getIdStoreGroup()));
            //LOG.warning("END calculate totals : " + new DateTime());


            boolean isFullProvider = isFullProvider(deliveryOrder, tipConfigOptional);

            // delete tip if is full provider and pre-selected tip

//            LOG.info("Is fullProvider? -> " + isFullProvider);

            if (isFullProvider && tipConfigOptional.isPresent()) {
                deleteTips(tipConfigOptional.get(), deliveryOrder, customerKey);
                hideTipItemList(deliveryOrder, tipConfigOptional);
            }

            OrderEndpointValidation orderEndpointValidation = new OrderEndpointValidation();
            boolean isUserPrime = validateUserPrime(Long.valueOf(shoppingCartJson.getId()));
            boolean hasPrimeDiscount = orderEndpointValidation.isPrimeDiscountFlag(
                    isUserPrime,
                    deliveryOrderItemList
            );
//            LOG.info("hasPrimeDiscount -> "+ hasPrimeDiscount);
            deliveryOrder.setPrimeDiscountFlag(hasPrimeDiscount);

            // add tip
            //Eliminar propina para ciertos usuarios
            if (tipConfigOptional.isPresent()) {
                if (!notAllowedTips(Long.valueOf(shoppingCartJson.getId()))) {
                    if (!isOrderOnlyPrime(deliveryOrderItemList, deliveryOrderProviderListToSave)) {
                        if (!isScanAndGo && shoppingCartJson.getDeliveryType().equals(DeliveryType.EXPRESS) && !isFullProvider) {
                            Optional<Tip> optionalTip = getOptionalTip(shoppingCartJson.getIdStoreGroup(), tipConfigOptional);

                            if (optionalTip.isPresent() && tipConfigOptional.isPresent()) {
                                // add tip price/

                                Optional<DeliveryOrderItem> itemTipOpt = getItemTipInCart(deliveryOrder, tipConfigOptional.get());

                                double tipPrice = getTipPriceForOrder(deliveryOrder, tipConfigOptional.get());
                                hideTipItemList(deliveryOrder, tipConfigOptional);
                                deliveryOrder.setTip(optionalTip.get());

                                // rest tip item
                                if (itemTipOpt.isPresent()) {
                                    deliveryOrder.setTipPrice(tipPrice);
                                    // calculate subtotal with tip
//                                    LOG.info("ENTRO_RESTA_TIP");
                                    deliveryOrder.setSubTotalPrice(deliveryOrder.getSubTotalPrice() - tipPrice);
                                    deliveryOrder.setTotalQuantity(deliveryOrder.getTotalQuantity() - 1);
                                    deliveryOrder.setQuantityFarmatodo(deliveryOrder.getQuantityFarmatodo() - 1);
                                }

                    /*if (shoppingCartJson.getTip() == null){
                        deliveryOrder.setTipPrice(null);
                    }*/
                            }

                            // fix para sergio
                            if (shoppingCartJson.getTip() != null && shoppingCartJson.getTip() == 0.0) {
                                deliveryOrder.setTipPrice(0.0);
                            }

                        }
                    }

                }
            }
            try {
                LOG.info("Response ->>>>> " + new Gson().toJson(deliveryOrder));
                String keyCache = shoppingCartJson.getIdCustomerWebSafe() + Constants.KEY_COUPON_CACHE;
                AnswerDeduct coupon = existCouponInRedis(shoppingCartJson.getIdCustomerWebSafe(), keyCache, Constants.INDEX_REDIS_FOURTEEN);
                LOG.info("coupon validadate ->>>>> " + new Gson().toJson(coupon));
                TalonOneService talonOneService = new TalonOneService();
                deliveryOrder = talonOneService.sendOrderToTalonOne(deliveryOrder, shoppingCartJson, coupon);
            } catch (Exception e) {
                LOG.warning("Error total de Talon One" + Arrays.toString(e.getStackTrace()));
            }
            try {
                deliveryOrder = addFreeItemTalonOne(shoppingCartJson, customerKey, deliveryOrder);
            } catch (Exception e) {
                LOG.warning("Error FreeItem Talon One:" + Arrays.toString(e.getStackTrace()));
            }
            if (Objects.nonNull(deliveryOrder) && Objects.nonNull(deliveryOrder.getItemList())) {
                if (isOnlyPrimeForDeliveryOrder(deliveryOrder)) {
                    deliveryOrder.setDeliveryType(DeliveryType.EXPRESS);
                }
            }
            User user = users.findUserByKey(customerKey);

            deductDiscount(shoppingCartJson.getIdCustomerWebSafe(), deliveryOrder, user.getId(), traditionalCoupon);

            if (deliveryOrder.isPrimeDiscountFlag()) {
                deliveryOrder.getItemList().stream()
                        .filter(item -> item.getPrimePrice() != null &&
                                item.getOfferPrice() != null &&
                                item.getPrimePrice().equals(item.getOfferPrice()))
                        .forEach(item -> item.setOfferText(item.getPrimeTextDiscount()));
            }
            return deliveryOrder;
        } catch (Exception e) {
            LOG.warning("method priceDelivery: Error Message: " + e.getMessage());
            LOG.warning("method priceDelivery: Error Cause: " + e.getCause());
            if(Objects.nonNull(e.getMessage()) && e.getMessage().equals("Error al validar la orden.")){
                throw new ConflictException("Ha ocurrido un error al validar la orden, intentelo de nuevo");
            }
            try {
                sendPriceDeliveryMessage(shoppingCartJson);
                if (APIAlgolia.getDeleteCartConfig()) {
                    CustomerEndpoint customerEndpoint = new CustomerEndpoint();
                    customerEndpoint.deleteShoppingCart("12345", shoppingCartJson.getId());
                }
            } catch (Exception ex) {
                LOG.warning("method priceDelivery: No fue posible envia la notificacion de SMS: " + ex.getMessage());
            }
            throw new ConflictException("Error al realizar el calculo en el carrito v2.");
        }
    }

    /**
     * elaborates the Objet Coupon and deduct the discount
     * deductDiscount
     *
     * @param idCustomerWebSafe
     * @param deliveryOrder
     * @param idUser
     * @return Answer
     */
    private void deductDiscount(String idCustomerWebSafe, DeliveryOrder deliveryOrder, Integer idUser, com.imaginamos.farmatodo.model.talonone.Coupon traditionalCoupon) {
        if (Objects.nonNull(deliveryOrder.getItemList())) {
            DeductDiscount deductDiscount = new DeductDiscount();
            deductDiscountWithoutCoupon(deliveryOrder, deductDiscount);
            equaliceValuesOfferPrice(deliveryOrder, deductDiscount, traditionalCoupon);
            if (!deliveryOrder.hasCouponTalon()) {
                String keyNameCoupon = Constants.KEY_COUPON_CACHE;
                AnswerDeduct couponMediosDigitales = existCouponInRedis(idCustomerWebSafe, keyNameCoupon, Constants.INDEX_REDIS_FOURTEEN);
                if (Objects.nonNull(couponMediosDigitales)) {
                    //LOG.info("entro a coupon DIGITAL MEDIA");
                    com.imaginamos.farmatodo.model.talonone.Coupon coupon = mapAnswerToCouponTalonOrRPM(couponMediosDigitales, deliveryOrder);
                    deliveryOrder.setCoupon(coupon);
                    if (deliveryOrder.hasCouponDigitalMedia() && Objects.isNull(deliveryOrder.getCouponAutomaticTalonList())){
                        deliveryOrder.setOfferPrice(deliveryOrder.getOfferPrice() - coupon.getDiscountCoupon());
                    }
                    else if(deliveryOrder.hasCouponDigitalMedia()
                            && Objects.nonNull(deliveryOrder.getCouponAutomaticTalonList())
                            && deliveryOrder.getCouponAutomaticTalonList().get(0).getDiscountCoupon()>0){
                        Double totalOrder=deliveryOrder.getSubTotalPrice() -(deliveryOrder.getOfferPrice()+deliveryOrder.getCoupon().getDiscountCoupon());
                        if(totalOrder<0){
                            totalOrder=0.0;
                        }
                        totalOrder+=(Objects.nonNull(deliveryOrder.getTipPrice()) ? deliveryOrder.getTipPrice() : 0.0);
                        deliveryOrder.setTotalPrice(totalOrder);
                    }
                }
            }
            savedInRedisDeductDiscount(idCustomerWebSafe, deliveryOrder);
        }
    }

    private static void equaliceValuesOfferPrice(DeliveryOrder deliveryOrder, DeductDiscount deductDiscount, com.imaginamos.farmatodo.model.talonone.Coupon traditionalCoupon) {

        LOG.info("equaliceValuesOfferPrice - deliveryOrder -> " + new Gson().toJson(deliveryOrder));
        Double offerPriceInitial = deliveryOrder.getOfferPrice();

        if (Objects.nonNull(deliveryOrder.getCouponAutomaticTalonList())
                && !deliveryOrder.getCouponAutomaticTalonList().isEmpty()
                && Objects.nonNull(deductDiscount)
                && Objects.nonNull(deliveryOrder.getCouponAutomaticTalonList().get(0).getDiscountCoupon())) {
            Double discountCouponAutomatic = deliveryOrder.getCouponAutomaticTalonList().get(0).getDiscountCoupon();
            Double suma = 0.0;
            if(deliveryOrder.isPrimeDiscountFlag()){
                suma += deductDiscount.getTotalSaveProducts()
                        + (Objects.nonNull(deductDiscount.getDiscountProductsPrime()) ? deductDiscount.getDiscountProductsPrime() : 0D)
                        + discountCouponAutomatic;
            }else{
                suma += deductDiscount.getTotalSaveProducts() + discountCouponAutomatic;
            }

            if (Objects.nonNull(deliveryOrder.getCoupon())
                    && Objects.nonNull(deliveryOrder.getCoupon().getDiscountCoupon())
                    && deliveryOrder.getCoupon().getDiscountCoupon() > 0) {
                Double totalOrder = deliveryOrder.getSubTotalPrice() -
                        (((suma>0D)?suma:deliveryOrder.getCouponAutomaticTalonList().get(0).getDiscountCoupon()) + deliveryOrder.getCoupon().getDiscountCoupon());
                if(totalOrder<0){
                    totalOrder=0.0;
                }
                totalOrder+=(Objects.nonNull(deliveryOrder.getTipPrice()) ? deliveryOrder.getTipPrice() : 0.0)
                        +(Objects.nonNull(deliveryOrder.getDeliveryPrice()) ? deliveryOrder.getDeliveryPrice() : 0.0);
                //LOG.info("entro a coupon totalOrder: " + totalOrder);
                deliveryOrder.setOfferPrice(((suma>0D)?suma:deliveryOrder.getCouponAutomaticTalonList().get(0).getDiscountCoupon()));
                deliveryOrder.setTotalPrice(totalOrder);
            } else {
                Double totalOrder = deliveryOrder.getSubTotalPrice() -
                        ((suma>0D)?suma:deliveryOrder.getCouponAutomaticTalonList().get(0).getDiscountCoupon());
                if(totalOrder<0){
                    totalOrder=0.0;
                }
                totalOrder+=(Objects.nonNull(deliveryOrder.getTipPrice()) ? deliveryOrder.getTipPrice() : 0.0)
                        +(Objects.nonNull(deliveryOrder.getDeliveryPrice()) ? deliveryOrder.getDeliveryPrice() : 0.0);
                deliveryOrder.setOfferPrice(((suma>0D)?suma:deliveryOrder.getCouponAutomaticTalonList().get(0).getDiscountCoupon()));

                if(Objects.nonNull(traditionalCoupon) && Objects.nonNull(traditionalCoupon.getNameCoupon())){
                    if(deliveryOrder.getTotalPrice() < totalOrder){
                        traditionalCoupon.setDiscountCoupon(deliveryOrder.getTotalPrice() - totalOrder);
                        deliveryOrder.setCoupon(traditionalCoupon);
                    }
                }else
                    deliveryOrder.setTotalPrice(totalOrder);
            }
        }
        else if(Objects.nonNull(deliveryOrder.getCoupon()) && Objects.nonNull(deductDiscount)){
            //LOG.info("entro a coupon y deductDiscount");
            Double offerPrice=0.0;
            if(deliveryOrder.isPrimeDiscountFlag()){
                offerPrice += deductDiscount.getTotalSaveProducts()
                        + (Objects.nonNull(deductDiscount.getDiscountProductsPrime()) ? deductDiscount.getDiscountProductsPrime() : 0D);
            }else{
                offerPrice += deductDiscount.getTotalSaveProducts();
            }
            LOG.info("offerPrice: " + offerPrice + " deliveryOrder.getCoupon().getDiscountCoupon(): "
                    + deliveryOrder.getCoupon().getDiscountCoupon()
                    + " deliveryOrder.getSubTotalPrice(): " +deliveryOrder.getSubTotalPrice()
                    + " deliveryOrder.getTipPrice(): " + (Objects.nonNull(deliveryOrder.getTipPrice()) ? deliveryOrder.getTipPrice() : 0.0));
            Double totalOrder = deliveryOrder.getSubTotalPrice() - (offerPrice
                    + (Objects.nonNull(deliveryOrder.getCoupon().getDiscountCoupon())?deliveryOrder.getCoupon().getDiscountCoupon():0.0));
            if(totalOrder<0){
                totalOrder=0.0;
            }
            totalOrder+=(Objects.nonNull(deliveryOrder.getTipPrice()) ? deliveryOrder.getTipPrice() : 0.0)
                    +(Objects.nonNull(deliveryOrder.getDeliveryPrice()) ? deliveryOrder.getDeliveryPrice() : 0.0);
            deliveryOrder.setOfferPrice(offerPrice);
            deliveryOrder.setTotalPrice(totalOrder);
        }
        else{
            if (Objects.nonNull(deductDiscount) && Objects.isNull(deliveryOrder.getCoupon())
                    && Objects.isNull(deliveryOrder.getCouponAutomaticTalonList())
                    && ((deliveryOrder.isPrimeDiscountFlag() && Objects.nonNull(deductDiscount.getDiscountProductsPrime()) && deductDiscount.getDiscountProductsPrime() > 0)
                    || (Objects.nonNull(deductDiscount.getDiscountProductsOfferPrice()) && deductDiscount.getDiscountProductsOfferPrice() > 0))) {
                Double offerPrice = 0.0;
                if (deliveryOrder.isPrimeDiscountFlag()
                        && Objects.nonNull(deductDiscount.getDiscountProductsPrime())
                        && deductDiscount.getDiscountProductsPrime() > 0) {
                    offerPrice += deductDiscount.getTotalSaveProducts() + deductDiscount.getDiscountProductsPrime();
                } else if (Objects.nonNull(deductDiscount.getDiscountProductsOfferPrice())
                        && deductDiscount.getDiscountProductsOfferPrice() > 0) {
                    offerPrice += deductDiscount.getTotalSaveProducts();
                }
                Double totalOrder = deliveryOrder.getSubTotalPrice() - offerPrice;
                if (totalOrder < 0) {
                    totalOrder = 0.0;
                }
                totalOrder += (Objects.nonNull(deliveryOrder.getTipPrice()) ? deliveryOrder.getTipPrice() : 0.0)
                        + (Objects.nonNull(deliveryOrder.getDeliveryPrice()) ? deliveryOrder.getDeliveryPrice() : 0.0);
                deliveryOrder.setOfferPrice(offerPrice);
               // deliveryOrder.setTotalPrice(totalOrder);
            }
            else if(Objects.nonNull(deductDiscount) && deductDiscount.getTotalSaveProducts()>0) {
               //LOG.info("entro solamente deductDiscount y prime con cupon tradicional");

                double totalDiscount = 0.0;
                if (deliveryOrder.isPrimeDiscountFlag()) {
                    totalDiscount += deductDiscount.getTotalSaveProducts() + deductDiscount.getDiscountProductsPrime();
                } else totalDiscount += deductDiscount.getTotalSaveProducts();

                if(offerPriceInitial > totalDiscount)
                    totalDiscount = offerPriceInitial;

                double totalOrder = deliveryOrder.getSubTotalPrice() - totalDiscount;
                if (Objects.nonNull(traditionalCoupon) && Objects.nonNull(traditionalCoupon.getNameCoupon())) {
                    if (deliveryOrder.getTotalPrice() < totalOrder) {
                        traditionalCoupon.setDiscountCoupon(totalOrder
                                + deliveryOrder.getTotalDelivery()
                                + (Objects.nonNull(deliveryOrder.getTipPrice()) ? deliveryOrder.getTipPrice() : 0.0)
                                - deliveryOrder.getTotalPrice());
                        deliveryOrder.setCoupon(traditionalCoupon);
                    }
                }
                deliveryOrder.setOfferPrice(totalDiscount);
                deliveryOrder.setTotalPrice(totalOrder + deliveryOrder.getTotalDelivery() + (Objects.nonNull(deliveryOrder.getTipPrice()) ? deliveryOrder.getTipPrice() : 0.0));
            }
        }

    }

    /**
     * save in Redis the coupons Talon-RPM and deduct discount
     * savedInRedisDeductDiscountAndCoupon
     *
     * @param idCustomerWebSafe
     * @param deliveryOrder
     */
    private static void savedInRedisDeductDiscount(String idCustomerWebSafe, DeliveryOrder deliveryOrder) {
        if (Objects.nonNull(deliveryOrder)) {
            if (Objects.nonNull(deliveryOrder.getDeductDiscount().getDiscountProductsRPM())) {
                CachedDataManager.saveJsonInCacheIndexTime(idCustomerWebSafe + Constants.KEY_DISCOUNT_RPM, String.valueOf(deliveryOrder.getDeductDiscount().getDiscountProductsRPM()), 14, 600);
            }
            if (Objects.nonNull(deliveryOrder.getDeductDiscount().getDiscountProductsPrime())) {
                CachedDataManager.saveJsonInCacheIndexTime(idCustomerWebSafe + Constants.KEY_DISCOUNT_PRIME, String.valueOf(deliveryOrder.getDeductDiscount().getDiscountProductsPrime()), 14, 600);
            }
            if (Objects.nonNull(deliveryOrder.getDeductDiscount().getDiscountProductsCampaignTalon())) {
                CachedDataManager.saveJsonInCacheIndexTime(idCustomerWebSafe + Constants.KEY_DISCOUNT_TALON, String.valueOf(deliveryOrder.getDeductDiscount().getDiscountProductsCampaignTalon()), 14, 600);
            }
            if (Objects.nonNull(deliveryOrder.getDeductDiscount().getTotalSaveProducts())) {
                CachedDataManager.saveJsonInCacheIndexTime(idCustomerWebSafe + Constants.KEY_TOTAL_SAVE, String.valueOf(deliveryOrder.getDeductDiscount().getTotalSaveProducts()), 14, 600);
            }
            if (Objects.nonNull(deliveryOrder.getDeductDiscount().getDiscountProductsOfferPrice())) {
                CachedDataManager.saveJsonInCacheIndexTime(idCustomerWebSafe + Constants.KEY_OFFER_PRICE, String.valueOf(deliveryOrder.getDeductDiscount().getDiscountProductsOfferPrice()), 14, 600);
            }
        }
    }

    private com.imaginamos.farmatodo.model.talonone.Coupon mapAnswerToCouponTalonOrRPM(AnswerDeduct answerDeduct, DeliveryOrder deliveryOrder) {
        com.imaginamos.farmatodo.model.talonone.Coupon coupon = new com.imaginamos.farmatodo.model.talonone.Coupon();
        coupon.setCouponMessage(answerDeduct.getMessage());
        coupon.setNameCoupon(answerDeduct.getNameCoupon());
        coupon.setDiscountCoupon(answerDeduct.getDiscount());
        coupon.setTalonOneOfferDescription(answerDeduct.getNotificationMessage());
        LOG.info("answer:" + new Gson().toJson(answerDeduct));
        if (Objects.nonNull(answerDeduct.getRestrictionValue()) && answerDeduct.getRestrictionValue() < deliveryOrder.getSubTotalPrice()) {
            coupon.setTypeNotificacion(answerDeduct.getTypeNotifcation());
        } else {
            coupon.setTypeNotificacion("Error");
        }
        LOG.info("coupon:" + new Gson().toJson(coupon));
        return coupon;
    }

    /**
     * validate if exist coupon de MD in Redis
     * existInRedis
     *
     * @param idCustomerWebSafe
     */
    private static AnswerDeduct existCouponInRedis(String idCustomerWebSafe, String nameKey, Integer NUM_BD_REDIS) {
        LOG.info("nameKey: "+nameKey);
        AnswerDeduct answerCouponTalonOrMD = null;
        if (Objects.nonNull(idCustomerWebSafe)) {
            Optional<String> couponTalonOrRPM = CachedDataManager.getJsonFromCacheIndex( nameKey, NUM_BD_REDIS);
            if (couponTalonOrRPM.isPresent()) {
                answerCouponTalonOrMD = new Gson().fromJson(couponTalonOrRPM.get(), AnswerDeduct.class);
            }
        }
        return answerCouponTalonOrMD;
    }

    /**
     * filter items by discount
     * deductDiscountWithoutCoupon
     *
     * @param deliveryOrder
     * @param deductDiscount
     */
    private static void deductDiscountWithoutCoupon(DeliveryOrder deliveryOrder, DeductDiscount deductDiscount) {
        //sum discount product prime
        deductDiscount.setDiscountProductsPrime(Double.valueOf(deliveryOrder.getItemList().stream()
                .filter(item -> Objects.nonNull(item) && Objects.nonNull(item.getPrimePrice())
                        && Objects.nonNull(item.getFullPrice()) && item.getPrimePrice() > 0.0)
                .mapToDouble(item -> (item.getFullPrice() * item.getQuantitySold()) - (item.getPrimePrice() * item.getQuantitySold())).sum()));

        //sum discount product RPM
        deductDiscount.setDiscountProductsRPM(Double.valueOf(deliveryOrder.getItemList().stream()
                .filter(item -> Objects.nonNull(item) && Objects.nonNull(item.getOfferPrice())
                        && Objects.nonNull(item.getFullPrice()) && Objects.nonNull(item.isTalonDiscount())
                        && Objects.nonNull(item.getPrimePrice()) && !item.isTalonDiscount()
                        && item.getPrimePrice().equals(0.0) && item.getOfferPrice() > 0.0)
                .mapToDouble(item -> (item.getFullPrice() * item.getQuantitySold()) - (item.getOfferPrice() * item.getQuantitySold())).sum()));

        //sum discount product TalonOne
        deductDiscount.setDiscountProductsCampaignTalon(Double.valueOf(deliveryOrder.getItemList().stream()
                .filter(item -> Objects.nonNull(item) && Objects.nonNull(item.getOfferPrice())
                        && Objects.nonNull(item.getFullPrice()) && Objects.nonNull(item.isTalonDiscount())
                        && item.getOfferPrice() > 0.0 && item.isTalonDiscount())
                .mapToDouble(item -> (item.getFullPrice() * item.getQuantitySold()) - (item.getOfferPrice() * item.getQuantitySold())).sum()));

        //sum discount product offerPrice
        deductDiscount.setDiscountProductsOfferPrice(Double.valueOf(deliveryOrder.getItemList().stream()
                .filter(item -> Objects.nonNull(item) && Objects.nonNull(item.getOfferPrice())
                        && Objects.nonNull(item.getFullPrice()) && Objects.nonNull(item.isTalonDiscount())
                        && item.getOfferPrice() > 0.0 && !item.isTalonDiscount()
                        && Objects.nonNull(item.getPrimePrice()) && item.getPrimePrice() > 0.0)
                .mapToDouble(item -> (item.getFullPrice() * item.getQuantitySold()) - (item.getOfferPrice() * item.getQuantitySold())).sum()));

        Double sumDiscountFtd = 0D;
        if (!deliveryOrder.isPrimeDiscountFlag()) {
            sumDiscountFtd = deductDiscount.getDiscountProductsRPM() +
                    deductDiscount.getDiscountProductsCampaignTalon() +
                    (Objects.nonNull(deductDiscount.getDiscountProductsOfferPrice()) ? deductDiscount.getDiscountProductsOfferPrice() : 0D);
        } else {
            sumDiscountFtd = deductDiscount.getDiscountProductsRPM()
                    + deductDiscount.getDiscountProductsCampaignTalon();
        }

        deductDiscount.setTotalSaveProducts(sumDiscountFtd);
        deliveryOrder.setDeductDiscount(deductDiscount);
    }

    private boolean isOnlyPrimeForDeliveryOrder(DeliveryOrder deliveryOrder) {
        return deliveryOrder.getItemList().size() == 1 && isOrderPrime(deliveryOrder);
    }

    private void validateTipAndPrime(Optional<TipConfig> tipConfig, List<DeliveryOrderItem> deliveryOrderItemList, Key<User> customerKey, int idStoreGroup, DeliveryOrder deliveryOrder, ValidateOrderReq validateOrderReq) {
        boolean isPrime = isOrderPrimeV2(deliveryOrder);
        Optional<Tip> optionalTip = getOptionalTip(idStoreGroup, tipConfig);
        int itemSize = deliveryOrderItemList.size();
        if (optionalTip.isPresent() && tipConfig.isPresent() && isPrime && itemSize == 2) {
            List<ItemTip> itemTipList = tipConfig.get().getItemTips();
            if (!Objects.isNull(itemTipList) && !validateOrderReq.getItems().isEmpty()) {
                for (ItemTip itemTipV1 : itemTipList) {
                    validateOrderReq.getItems().removeIf(tipsListAlgolia -> Objects.equals(tipsListAlgolia.getItemId(), itemTipV1.getItemId()));
                }
                deleteTips(tipConfig.get(), deliveryOrder, customerKey);
            }
        }
    }

    private void setOpticalItemFiltersProvider(List<DeliveryOrderProvider> deliveryOrderProviderListToSave) {

        for (DeliveryOrderProvider provider : deliveryOrderProviderListToSave) {
            if (provider.getId() == 1207) { // id del proveedor de optica
                provider.getItemList().forEach(itemProvider -> {
                    OpticalItemFilter opticalItemFilter = new Gson().fromJson(itemProvider.getFiltersOptical(), OpticalItemFilter.class);
                    ShipmentType shipment = Objects.nonNull(itemProvider.getShipment()) ? itemProvider.getShipment() : ShipmentType.EXTENDIDO;
                    LOG.info("Filtos de optica: " + opticalItemFilter.toString());
                    itemProvider.setOpticalFilter(opticalItemFilter);
                    itemProvider.setShipment(shipment);
                });
            }
        }
    }


    @NotNull
    private DeliveryOrder addFreeItemTalonOne(ShoppingCartJson shoppingCartJson, Key<User> customerKey, DeliveryOrder deliveryOrder) {
        if (deliveryOrder.isTalonOneItemFree()) {
            Optional<String> itemIdDeleteOptional = CachedDataManager.getJsonFromCacheIndex(shoppingCartJson.getIdCustomerWebSafe(), 14);
            String itemIdDelete = "";
            if (itemIdDeleteOptional.isPresent()) {
                itemIdDelete = itemIdDeleteOptional.get();
            }
            if (deliveryOrder.getItemList().size() == 1 &&
                    (primeUtil.isItemPrime(deliveryOrder.getItemList().get(0).getId())
                            || deliveryOrder.getItemList().get(0).getRms_subclass().equals(Constants.NAME_SAMPLING.toUpperCase())
                            || deliveryOrder.getItemList().get(0).isTalonItemFree())
            ) {
                deleteAllShoppingCart(shoppingCartJson, deliveryOrder);
            } else {
                DeliveryOrder deliveryOrderData = ObjectifyService.ofy().load().type(DeliveryOrder.class)
                        .filter("currentStatus", 1)
                        .ancestor(Ref.create(customerKey)).first().now();
                List<DeliveryOrderItem> deliveryOrderItemListBefore = ObjectifyService.ofy().load().type(DeliveryOrderItem.class).ancestor(deliveryOrderData).list();
                if (deliveryOrderItemListBefore.size() < deliveryOrder.getItemList().size()) {
                    String finalItemIdDelete = itemIdDelete;
                    deliveryOrder.getItemList().stream().filter(deliveryOrderItem -> deliveryOrderItem.isTalonItemFree()).forEach(deliveryOrderItem -> {
                        try {
                            List<DeliveryOrderItem> deliveryOrderItemListAux;
                            if (!finalItemIdDelete.trim().equals(String.valueOf(deliveryOrderItem.getId()).trim())) {
                                LOG.info("deliveryOrderItem entro if:" + deliveryOrderItem.getId() + "-finalItemIdDelete: " + finalItemIdDelete);
                                this.addDeliveryOrderItem(shoppingCartJson.getToken(), shoppingCartJson.getTokenIdWebSafe(), shoppingCartJson.getIdCustomerWebSafe(), (int) deliveryOrderItem.getId(), deliveryOrderItem.getQuantitySold(), shoppingCartJson.getIdStoreGroup(), true, shoppingCartJson.getDeliveryType().getDeliveryType(), shoppingCartJson.getSource(), deliveryOrderItem.getObservations(), false, null, "", null, null);
                            }
                            if (finalItemIdDelete.trim().equals(String.valueOf(deliveryOrderItem.getId()).trim())) {
                                deliveryOrderItemListAux = deliveryOrder.getItemList();
                                deliveryOrderItemListAux.remove(deliveryOrderItem);
                                deliveryOrder.setItemList(deliveryOrderItemListAux);
                                LOG.info("deliveryOrder.salio: " + new Gson().toJson(deliveryOrder));
                            }
                        } catch (Exception e) {
                            LOG.info("No se encuentra item en Algolia o Datastore o el producto no tiene stock para item free:  "
                                    + deliveryOrderItem.getId());
                            try {
                                deleteDeliveryOrderItem(shoppingCartJson.getToken(), shoppingCartJson.getTokenIdWebSafe(),
                                        shoppingCartJson.getIdCustomerWebSafe(), shoppingCartJson.getIdStoreGroup(),
                                        (int) deliveryOrderItem.getId(), shoppingCartJson.getDeliveryType().toString());
                            } catch (ConflictException | AlgoliaException | BadRequestException ex) {
                                LOG.warning("Error al eliminar item free: " + ex.getMessage());
                            }
                        }
                    });
                } else if (deliveryOrderItemListBefore.size() == deliveryOrder.getItemList().size()) {
                    String finalItemIdDelete = itemIdDelete;
                    Key<DeliveryOrder> deliveryOrderKey = Key.create(customerKey, DeliveryOrder.class, deliveryOrder.getIdDeliveryOrder());
                    deliveryOrder.getItemList().stream().filter(deliveryOrderItem -> deliveryOrderItem.isTalonItemFree()).forEach(deliveryOrderItem -> {
                        if (!finalItemIdDelete.equals(deliveryOrderItem.getId())) {
                            DeliveryOrderItem deliveryOrderItem1 = ofy().load().type(DeliveryOrderItem.class)
                                    .filter("idItem", Key.create(Item.class, String.valueOf(deliveryOrderItem.getId())))
                                    .ancestor(Ref.create(deliveryOrderKey)).first().now();
                            if (deliveryOrderItem1.getQuantitySold() < deliveryOrderItem.getQuantitySold()) {
                                deliveryOrderItem1.setQuantitySold(deliveryOrderItem.getQuantitySold());
                            }
                            deliveryOrderItem1.setOfferText(deliveryOrderItem.getOfferText());
                            deliveryOrderItem1.setOfferDescription(deliveryOrderItem.getOfferDescription());
                            deliveryOrderItem1.setOfferPrice(deliveryOrderItem.getOfferPrice());
                            deliveryOrderItem1.setPrimePrice(deliveryOrderItem.getPrimePrice());
                            deliveryOrderItem1.setPrimeDescription(deliveryOrderItem.getPrimeDescription());
                            deliveryOrderItem1.setPrimeTextDiscount(deliveryOrderItem.getPrimeTextDiscount());
                            deliveryOrderItem1.setTalonDiscount(deliveryOrderItem.isTalonDiscount());
                            deliveryOrderItem1.setTalonItemFree(deliveryOrderItem.isTalonItemFree());
                            ofy().save().entity(deliveryOrderItem1);
                        }
                    });
                }
            }
        }
        LOG.info("deliveryOrder.final: " + new Gson().toJson(deliveryOrder));
        return deliveryOrder;
    }

    private void deleteAllShoppingCart(ShoppingCartJson shoppingCartJson, DeliveryOrder deliveryOrder) {
        CustomerEndpoint customerEndpoint = new CustomerEndpoint();
        try {
            customerEndpoint.deleteShoppingCart("12345", shoppingCartJson.getId());
            deliveryOrder.getItemList().clear();
            deliveryOrder.setOfferPrice(0.0);
            deliveryOrder.setQuantityFarmatodo(0);
            deliveryOrder.setTotalQuantity(0);
            deliveryOrder.setDeliveryLabel("");
            deliveryOrder.setTalonOneItemFree(false);
            deliveryOrder.setTotalPrice(0.0);
            deliveryOrder.setCreateDate(null);
        } catch (UnauthorizedException e) {
            LOG.warning("method priceDelivery: No fue posible eliminar el carrito");
        }
    }

    private DeliveryOrder getCourrentDeliveryOrder(final Key<User> customerKey) throws ApiProxy.ApiDeadlineExceededException, Exception {
        return ofy().load().type(DeliveryOrder.class)
                .filter("currentStatus", 1)
                .ancestor(Ref.create(customerKey)).first().now();
    }

    @NotNull
    private DeliveryOrder getDeliveryOrderEmpty(ShoppingCartJson shoppingCartJson, Optional<TipConfig> tipConfigOptional, Key<User> customerKey, DeliveryOrder deliveryOrder) {
        //LOG.warning("deliveryOrderItemList is null NO HAY ITEMS AGREGADOS");
        try {
            if (APIAlgolia.getDeleteCartConfig()) {
                CustomerEndpoint customerEndpoint = new CustomerEndpoint();
                customerEndpoint.deleteShoppingCart("12345", shoppingCartJson.getId());
            }
            deleteTips(tipConfigOptional.get(), deliveryOrder, customerKey);
        } catch (Exception e) {
            LOG.info("Error eliminando el carrito del cliente.");
        }
        return getEmptyDeliveryOrder(new DeliveryOrder());
    }

    private Boolean notAllowedTips(Long id) {
        //LOG.info("entra a la valifdacion: " );
//        LOG.info("entra a la valifdacion customer: " + id);
        Optional<NotAllowedTips> notAllowedTips = APIAlgolia.getNotAllowedTipsConfig();

        if (notAllowedTips.isPresent()) {
            Optional<Long> notAllowedTipsCustomer = notAllowedTips.get().getCustomerId().stream()
                    .filter(tipsListAlgolia -> Objects.equals(tipsListAlgolia, id))
                    .limit(1).findFirst();

            return notAllowedTipsCustomer.isPresent();
        }
        return false;
    }

    private Optional<Double> fixTipMaxQuantity(TipConfig tipConfig, DeliveryOrder deliveryOrder, Key<User> customerKey) {
        // si el carrito ya tiene propina y no viene valor, obtener el valor de esta y eliminar todas las propinas y agregar solo 1.

        if (deliveryOrder == null || tipConfig.getItemTips() == null) {
            return Optional.empty();
        }

        List<DeliveryOrderItem> deliveryOrderItemList = ofy().load().type(DeliveryOrderItem.class).ancestor(deliveryOrder).list();

        if (deliveryOrderItemList == null || deliveryOrderItemList.isEmpty()) {
            return Optional.empty();
        }

        for (DeliveryOrderItem itemCart : deliveryOrderItemList) {
            for (ItemTip itemTip : tipConfig.getItemTips()) {
                if (itemTip != null && itemTip.getItemId() != null && itemCart.getId() == itemTip.getItemId()) {
//                    LOG.info("Propina Encontrada item -> " + itemCart.getMediaDescription());
                    if (itemCart.getQuantitySold() > 1) {
                        return Optional.of(itemCart.getFullPrice());
                    }
                }
            }
        }

        return Optional.empty();

    }

    /**
     * validate if order is full provider.
     *
     * @param deliveryOrder
     * @param tipConfigOptional
     * @return
     */
    private boolean isFullProvider(DeliveryOrder deliveryOrder, Optional<TipConfig> tipConfigOptional) {

        if (deliveryOrder == null && !tipConfigOptional.isPresent()) {
            return false;
        }

        double tipPrice = getTipPriceForOrder(deliveryOrder, tipConfigOptional.get());


        int quantityFarmatodoWithoutTip = deliveryOrder.getQuantityFarmatodo();
        if (tipPrice > 0) {
            quantityFarmatodoWithoutTip = quantityFarmatodoWithoutTip > 0 ? deliveryOrder.getQuantityFarmatodo() - 1 : deliveryOrder.getQuantityFarmatodo();
        }

        return deliveryOrder.getQuantityProviders() > 0 && quantityFarmatodoWithoutTip == 0;
    }

    private void deleteTipsIfCityNotConfig(Optional<TipConfig> tipConfigOpt, DeliveryOrder deliveryOrder, Key<User> customerKey, int idStoreGroup) {

        if (!tipConfigOpt.isPresent()) {
            return;
        }

        TipConfig tipConfig = tipConfigOpt.get();

        String cityIdOrder = getCityIdByStoreId(idStoreGroup);

        if (cityIdOrder == null || cityIdOrder.isEmpty()) {
            return;
        }

        if (tipConfig.getDefaultTipsByCity() == null) {
            return;
        }

        Optional<DefaultTipsByCity> cityIdWithTip = tipConfig
                .getDefaultTipsByCity()
                .stream()
                .filter(cityIdAux -> cityIdAux != null && cityIdAux.getCityId() != null && cityIdAux.getCityId().equals(cityIdOrder))
                .findFirst();

        if (!cityIdWithTip.isPresent()) {
            LOG.info("No se encuentra config de propina para la ciudad => " + cityIdOrder +
                    " Se eliminan las propinas del carrito");
            deleteTips(tipConfig, deliveryOrder, customerKey);
        }

    }

    private double getTipPriceForOrder(DeliveryOrder deliveryOrder, TipConfig tipConfig) {


        if (tipConfig == null || tipConfig.getItemTips() == null || deliveryOrder.getItemList() == null) {
            return 0;
        }

        Optional<DeliveryOrderItem> itemTipOpt = getItemTipInCart(deliveryOrder, tipConfig);

        if (!itemTipOpt.isPresent()) {
            return 0;
        }

        return itemTipOpt.get().getFullPrice();

    }

    @NotNull
    private Optional<DeliveryOrderItem> getItemTipInCart(DeliveryOrder deliveryOrder, TipConfig tipConfig) {

        if (tipConfig == null || tipConfig.getItemTips() == null) {
            return Optional.empty();
        }

        if (deliveryOrder == null || deliveryOrder.getItemList() == null || deliveryOrder.getItemList().isEmpty()) {
            return Optional.empty();
        }

        return deliveryOrder.getItemList().stream().filter(itemCart -> {
            Optional<ItemTip> itemTipInCartOpt = tipConfig
                    .getItemTips()
                    .stream()
                    .filter(itemTipAux -> itemTipAux != null
                            && itemTipAux.getItemId() != null
                            && itemTipAux.getItemId().longValue() == itemCart.getId())
                    .findFirst();

            return itemTipInCartOpt.isPresent();
        }).findFirst();
    }

    private void hideTipItemList(DeliveryOrder deliveryOrder, Optional<TipConfig> tipConfigOpt) {

        if (!tipConfigOpt.isPresent()) {
            return;
        }

        TipConfig tipConfig = tipConfigOpt.get();
        ;

        if (tipConfig.getItemTips() == null || deliveryOrder.getItemList() == null) {
            return;
        }

        deliveryOrder.getItemList().removeIf(itemCart -> {
            Optional<ItemTip> itemTipInCartOpt = tipConfig
                    .getItemTips()
                    .stream()
                    .filter(itemTipAux -> itemTipAux != null
                            && itemTipAux.getItemId() != null
                            && itemTipAux.getItemId().longValue() == itemCart.getId())
                    .findFirst();

            return itemTipInCartOpt.isPresent();
        });

    }

    private boolean addTipToOrder(TipConfig tipConfig, ShoppingCartJson shoppingCartJson, DeliveryOrder deliveryOrder, Key<User> customerKey) throws AlgoliaException, ConflictException, BadRequestException {


        if (shoppingCartJson == null || shoppingCartJson.getTip() == null || shoppingCartJson.getTip() < 0) {
            return false;
        }

        if (tipConfig.getItemTips() == null) {
            LOG.warning("No se encuentra configuracion de propina-item para el valor de:  "
                    + shoppingCartJson.getTip());
            return false;
        }

        String cityIdOrder = getCityIdByStoreId(shoppingCartJson.getIdStoreGroup());

        if (cityIdOrder == null || cityIdOrder.isEmpty()) {
            return false;
        }

        if (tipConfig.getDefaultTipsByCity() == null) {
            return false;
        }

        Optional<DefaultTipsByCity> cityIdWithTip = tipConfig
                .getDefaultTipsByCity()
                .stream()
                .filter(cityIdAux -> cityIdAux != null && cityIdAux.getCityId() != null && cityIdAux.getCityId().equals(cityIdOrder))
                .findFirst();

        if (!cityIdWithTip.isPresent()) {
            return false;
        }


        Float tip = shoppingCartJson.getTip();

        Optional<ItemTip> itemTipOptional = tipConfig
                .getItemTips()
                .stream()
                .filter(itemTip -> itemTip != null && itemTip.getValue() != null && itemTip.getValue().equals(tip))
                .findFirst();

        /*if (shoppingCartJson.getTip() <= 0){
            // elimina las propinas ya q no existe propina con valor 0
            deleteTips(tipConfig, deliveryOrder, customerKey);
            return false;
        }*/

        if (!itemTipOptional.isPresent()) {
            LOG.warning("No se encuentra item en la configuracion de propina-item para el valor de:  "
                    + shoppingCartJson.getTip());
            return false;
        }

        deleteTips(tipConfig, deliveryOrder, customerKey);


        ItemTip itemTip = itemTipOptional.get();

//        LOG.info("item-tip =>> " + itemTip.getItemId() + "Value: " + itemTip.getValue());
        try {
            this.addDeliveryOrderItem(shoppingCartJson.getToken(),
                    shoppingCartJson.getTokenIdWebSafe(),
                    shoppingCartJson.getIdCustomerWebSafe(), itemTip.getItemId(), 1, shoppingCartJson.getIdStoreGroup(),
                    false,
                    shoppingCartJson.getDeliveryType().getDeliveryType(),
                    "ADDED_FROM_SEARCH",
                    "TIP",
                    false,
                    null,
                    null,
                    null,
                    null
            );
        } catch (Exception e) {
            LOG.severe("No se encuentra item en Algolia o Datastore o el producto no tiene stock segun la config de propina-item para el valor de:  "
                    + shoppingCartJson.getTip());
            return false;
        }


        return true;
    }

    private void deleteTips(TipConfig tipConfig, DeliveryOrder deliveryOrder, Key<User> customerKey) {
        // Verificar que exista una configuración válida de tips
        if (tipConfig == null || tipConfig.getItemTips() == null || tipConfig.getItemTips().isEmpty()) {
            return;
        }

        // Extraer en un Set los IDs de los item tips válidos para facilitar la búsqueda
        Set<Integer> tipItemIds = tipConfig.getItemTips().stream()
                .filter(Objects::nonNull)
                .map(ItemTip::getItemId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Cargar la lista de items del carrito asociados a la orden
        List<DeliveryOrderItem> deliveryOrderItemList = ofy().load()
                .type(DeliveryOrderItem.class)
                .ancestor(deliveryOrder)
                .list();

        if (deliveryOrderItemList == null || deliveryOrderItemList.isEmpty()) {
            return;
        }

        // Iterar sobre los items del carrito y eliminar aquellos cuyo ID coincida con algún tip
        deliveryOrderItemList.stream()
                .filter(itemCart -> tipItemIds.contains(itemCart.getId()))
                .forEach(itemCart -> deleteItemInCart(deliveryOrder, itemCart.getId(), customerKey));
    }

    private List<DeliveryOrderItem> deleteTipsForPrime(TipConfig tipConfig, DeliveryOrder deliveryOrder, Key<User> customerKey) {
        List<DeliveryOrderItem> deliveryOrderItemList = ofy().load().type(DeliveryOrderItem.class).ancestor(deliveryOrder).list();

        List<DeliveryOrderItem> deliveryOrderItemsAux = new ArrayList<>();
        deliveryOrderItemList.forEach(itemCart -> {
            if (tipConfig != null && tipConfig.getItemTips() != null) {
                tipConfig.getItemTips().forEach(itemTip -> {
                    if (itemTip == null || itemTip.getItemId() == null || itemCart.getId() != itemTip.getItemId()) {
                        deliveryOrderItemsAux.add(itemCart);
                    }
                });
            }
        });

        return deliveryOrderItemsAux;
    }

    private void deleteItemInCart(DeliveryOrder deliveryOrder, long itemId, Key<User> customerKey) {
        if (deliveryOrder == null || itemId <= 0) {
            return;
        }
        Key<DeliveryOrder> deliveryOrderKey = Key.create(customerKey, DeliveryOrder.class, deliveryOrder.getIdDeliveryOrder());
        DeliveryOrderItem deliveryOrderItem = ofy()
                .load()
                .type(DeliveryOrderItem.class)
                .filter("idItem", Key.create(Item.class, String.valueOf(itemId)))
                .ancestor(Ref.create(deliveryOrderKey))
                .first().now();
        if (deliveryOrderItem == null) {
            LOG.warning("No se encuentra el item a eliminar , ITEM: " + itemId);
            return;
        }
//        LOG.info("ITEM a eliminar -> " + itemId);
        ofy().delete().entity(deliveryOrderItem).now();
    }

    private Optional<Tip> getOptionalTip(int idStoreGroup, Optional<TipConfig> tipConfigOptional) {

        if (idStoreGroup <= 0) {
            return Optional.empty();
        }

        if (!tipConfigOptional.isPresent()) {
            return Optional.empty();
        }

        TipConfig tipConfig = tipConfigOptional.get();

        if (tipConfig.getTip() == null
                || tipConfig.getDefaultTipsByCity() == null
                || tipConfig.getDefaultTipsByCity().isEmpty()) {
            return Optional.empty();
        }

        String cityId = getCityIdByStoreId(idStoreGroup);

        if (cityId == null || cityId.isEmpty()) {
            return Optional.empty();
        }

        Optional<DefaultTipsByCity> defaultTipsByCityOptional = tipConfig
                .getDefaultTipsByCity()
                .stream()
                .filter(defaultCity ->
                        defaultCity != null && defaultCity.getCityId() != null && defaultCity.getCityId().equals(cityId)
                )
                .findFirst();

        if (!defaultTipsByCityOptional.isPresent() || defaultTipsByCityOptional.get().getDefaultTip() == null) {
            return Optional.empty();
        }

        // set config by city
        tipConfig.getTip().setDefaultTip(defaultTipsByCityOptional.get().getDefaultTip());

        return Optional.of(tipConfig.getTip());
    }

    private void validateDuplicateItems(List<DeliveryOrderItem> deliveryOrderItemList) {
        if (Objects.nonNull(deliveryOrderItemList) && !deliveryOrderItemList.isEmpty()) {
            Map<Long, DeliveryOrderItem> listItemResult = new HashMap<>();
            deliveryOrderItemList.stream().filter(item -> Objects.nonNull(item)).forEach(item -> {
                if (listItemResult.containsKey(item.getId())) {
                    if (listItemResult.get(item.getId()).getQuantitySold() < item.getQuantitySold()) {
                        listItemResult.get(item.getId()).setQuantitySold(item.getQuantitySold());
                    }
                    ofy().delete().entities(item);
                } else {
                    listItemResult.put(item.getId(), item);
                }
            });
            deliveryOrderItemList = listItemResult.values().stream().collect(Collectors.toList());
        }
    }

    private void addMarcaCategorySubcategorieAndItemUrl(DeliveryOrder deliveryOrder) {
//        LOG.info("method: addMarcaCategorySubcategorieAndItemUrl()");
        try {
            for (int i = 0; i < deliveryOrder.getItemList().size(); i++) {
                final DeliveryOrderItem item = deliveryOrder.getItemList().get(i);
                final ItemAlgolia itemAlgolia = APIAlgolia.getItemAlgolia(item.getId() + "" + URLConnections.MAIN_ID_STORE);
                if (itemAlgolia != null) {
                    deliveryOrder.getItemList().get(i).setSubCategory(itemAlgolia.getSubCategory());
                    deliveryOrder.getItemList().get(i).setCategorie(itemAlgolia.getCategorie());
                    deliveryOrder.getItemList().get(i).setMarca(itemAlgolia.getMarca());
                    deliveryOrder.getItemList().get(i).setItemUrl(itemAlgolia.getItemUrl());
                }
            }
        } catch (Exception e) {
            LOG.severe("method: addMarcaCategorySubcategorieAndItemUrl() Message: " + e.getMessage());
        }
    }

    private void addMarcaCategorySubcategorieAndItemUrl(CreatedOrder orderJson) {
//        LOG.info("method: addMarcaCategorySubcategorieAndItemUrl(orderJson)");
        try {
            for (int i = 0; i < orderJson.getItems().size(); i++) {
                final ItemAlgolia item = orderJson.getItems().get(i);
                final ItemAlgolia itemAlgolia = APIAlgolia.getItemAlgolia(item.getId() + "" + URLConnections.MAIN_ID_STORE);
                if (itemAlgolia != null) {
                    orderJson.getItems().get(i).setSubCategory(itemAlgolia.getSubCategory());
                    orderJson.getItems().get(i).setCategorie(itemAlgolia.getCategorie());
                    orderJson.getItems().get(i).setMarca(itemAlgolia.getMarca());
                    orderJson.getItems().get(i).setItemUrl(itemAlgolia.getItemUrl());
                }
            }
        } catch (Exception e) {
            LOG.severe("method: addMarcaCategorySubcategorieAndItemUrl(orderJson) Message: " + e.getMessage());
        }
    }

    private void validateRequestedQuantityVsRealStockInStore(DeliveryOrder deliveryOrder, final int idStoreGroup) {
        try {
            if (deliveryOrder != null && deliveryOrder.getItemList() != null
                    && !deliveryOrder.getItemList().isEmpty() && idStoreGroup > 0) {
//                LOG.info("Validando que la cantidad solicitada no supere el stock disponible en la tienda: " + idStoreGroup);
                // si quantitySold > a stock -> quantitySold = stock
                for (int i = 0; i < deliveryOrder.getItemList().size(); i++) {
                    final ItemAlgolia itemAlgolia = APIAlgolia.getItemAlgolia(deliveryOrder.getItemList().get(i).getId() + "" + idStoreGroup);
                    final int quantitySold = deliveryOrder.getItemList().get(i).getQuantitySold();
                    final int totalStock = itemAlgolia.getTotalStock();
                    if (deliveryOrder.getItemList().get(i).getQuantitySold() > itemAlgolia.getTotalStock()) {
                        deliveryOrder.getItemList().get(i).setQuantitySold(itemAlgolia.getTotalStock());
                        deliveryOrder.getItemList().get(i).setQuantitySoldGreaterThanStock(true);
                        deliveryOrder.getItemList().get(i).setMessageWhenQuantitySoldIsGreaterThanStock("Ha solicitado "
                                + quantitySold + " unidades pero solo hay " + totalStock);
                    }
                }
                ofy().save().entity(deliveryOrder);
            }
        } catch (Exception e) {
            LOG.warning("Validando que la cantidad solicitada no supere el stock " +
                    "disponible en la tienda: " + idStoreGroup + ". Mensaje:" + e.getMessage());
        }
    }

    private void restrictItemsAndSave(List<DeliveryOrderItem> deliveryOrderItemList, int idStoreGroup) {
        if (deliveryOrderItemList != null && !deliveryOrderItemList.isEmpty() && idStoreGroup > 0) {
            RestrictionItemConfig restrictionItemConfig = APIAlgolia.getRestrictionQuantityItems();

            deliveryOrderItemList.forEach(itemInList -> {

                if (restrictionItemConfig.getRestrictionItems() != null && !restrictionItemConfig.getRestrictionItems().isEmpty()) {
//                    LOG.info("getRestrictionItems idStoreGroup -> " + idStoreGroup);

                    restrictionItemConfig.getRestrictionItems().forEach(restrictionItem -> {
//                        LOG.info("restrict -> id item restrict -> " + restrictionItem.getItemId() +
//                                ", " + itemInList.getId());
                        if (restrictionItem.getItemId() == itemInList.getId() && itemInList.getQuantitySold() > restrictionItem.getRestrictionQuantity()) {
//                            LOG.info("RESTRINGIR -> " + itemInList.getId() + "a, : " + restrictionItem.getRestrictionQuantity());
                            itemInList.setQuantitySold(Math.toIntExact(restrictionItem.getRestrictionQuantity()));
                        }
                    });
                }
            });
        }
    }

    private void sendPriceDeliveryMessage(final ShoppingCartJson shoppingCartJson) throws AlgoliaException {
        AlertConfigMessage alertConfigMessage = APIAlgolia.getAlertMessage();
        alertConfigMessage.getPhoneNumbers().forEach(number ->
                Util.sendAlertCreateOrder(number,
                        "priceDelivery - getIdCustomerWebSafe : " + shoppingCartJson.getIdCustomerWebSafe() + " Error al calcular el carrito."));

    }

    private Boolean checkIfAppliesForFreeDelivery(final DeliveryOrder deliveryOrder,
                                                  List<DeliveryOrderItem> deliveryOrderItemList,
                                                  final ShoppingCartJson shoppingCartJson,
                                                  final String source) {

        if (Objects.isNull(deliveryOrder) || Objects.isNull(deliveryOrderItemList) || deliveryOrderItemList.isEmpty()) {
            //LOG.warning("method: checkIfAppliesForFreeDelivery() deliveryOrder or deliveryOrderItemList are null or empty..");
            return Boolean.FALSE;
        }

        Key<User> userKey = Key.create(shoppingCartJson.getIdCustomerWebSafe());
        User user = users.findUserByKey(userKey);

//        Building request to send to OMS...
        final Long customerId = Long.valueOf(Objects.isNull(user) ? 0 : user.getId());
        final String cityId = getCityIdByStoreId(shoppingCartJson.getIdStoreGroup());
        final String deliveryType = Objects.isNull(shoppingCartJson.getDeliveryType()) ? "" : shoppingCartJson.getDeliveryType().getDeliveryType();

        final List<FreeDeliveryItem> itemsToValidate = new ArrayList<>();

        Optional<TipConfig> tipConfigOptional = GrowthBookConfigLoader.getTipsConfig(Constants.CUSTOMER_ANONYMOUS);

        if (tipConfigOptional.isPresent()) {
//            Eliminar propinas para que no lo tenga en cuenta en el calculo de domicilio gratis.
            deleteTipsForPrimeOrder(deliveryOrderItemList, tipConfigOptional.get());
        }

        deliveryOrderItemList.forEach(item -> {
            final FreeDeliveryItem freeDeliveryItem = new FreeDeliveryItem(item.getId(), item.getQuantitySold());
            itemsToValidate.add(freeDeliveryItem);
        });

        Key<Customer> customerDS = Key.create(shoppingCartJson.getIdCustomerWebSafe());
        CustomerCoupon customerCoupon = obtainCustomerCoupon(customerDS);

        List<FreeDeliveryCoupon> listCouponFree = getFreeDeliveryCoupons(customerCoupon);

        FreeDeliverySimpleCart cart;
//        LOG.info("source -> " + source);
        if (!listCouponFree.isEmpty() && listCouponFree.size() >= 1) {
            cart = new FreeDeliverySimpleCart(customerId, cityId, deliveryType, source, itemsToValidate, listCouponFree);
        } else {
            cart = new FreeDeliverySimpleCart(customerId, cityId, deliveryType, source, itemsToValidate);
        }

        LOG.info("FreeDeliverySimpleCart to send to OMS => " + new Gson().toJson(cart));
        final Boolean applies = validateFreeDeliveryByCart(cart);
        LOG.info("applies => " + new Gson().toJson(applies));

        return applies;
    }

    @NotNull
    private List<FreeDeliveryCoupon> getFreeDeliveryCoupons(CustomerCoupon customerCoupon) {
        List<FreeDeliveryCoupon> listCouponFree = new ArrayList<>();
        if (customerCoupon != null && Objects.nonNull(customerCoupon.getCouponId())) {
//           Validando los cupones del cliente
            final Ref<Coupon> coupon = customerCoupon.getCouponId();
            if (coupon.get() != null && coupon.get().getName() != null) {
//                LOG.info(String.format("OferId -> %d couponType -> %s", coupon.get().getOfferId(), coupon.get().getCouponType().getCouponType()));
                FreeDeliveryCoupon couponFree = new FreeDeliveryCoupon();
                couponFree.setOfferId(coupon.get().getOfferId());
                couponFree.setCouponType(coupon.get().getCouponType().getCouponType());
                listCouponFree.add(couponFree);
            }
        }
        return listCouponFree;
    }

    private String getCityIdByStoreId(final Integer storeId) {
        final String DEFAULT_CITY = "";
        try {
            final StoresAlgolia storesAlgolia = APIAlgolia.getStoresAlgolia();
            if (Objects.isNull(storesAlgolia) || Objects.isNull(storesAlgolia.getStores()) || storesAlgolia.getStores().isEmpty()) {
                LOG.warning("El objeto de Algolia FTD.STORES esta vacio o nulo. No se encontrara ninguna ciudad para la tienda #" + storeId);
                return DEFAULT_CITY;
            }

            for (StoreAlgolia store : storesAlgolia.getStores()) {
                if (Objects.equals(store.getId(), storeId)) {
                    return store.getCity();
                }
            }

            return DEFAULT_CITY;
        } catch (Exception e) {
            LOG.warning("Ocurrio un error al buscar el cityID por storeID. Se retornara vacio.");
            return DEFAULT_CITY;
        }
    }

    private List<DeliveryOrderItem> deleteCouponExpired(DeliveryOrder deliveryOrder) {
        if (Objects.nonNull(deliveryOrder)) {
            List<DeliveryOrderItem> deliveryOrderItemListDS = ofy().load().type(DeliveryOrderItem.class).ancestor(deliveryOrder).list();
            if (Objects.nonNull(deliveryOrderItemListDS) && !deliveryOrderItemListDS.isEmpty()) {
                boolean deleteCoupons = false;
                DeliveryOrderItem deliveryOrderItemToDelete = null;
                for (DeliveryOrderItem item : deliveryOrderItemListDS) {
//                    LOG.info("item in cart -> " + item.toString() + " isCoupon? -> " + item.getCoupon() + ", itemId-> " + item.getIdItem().toString() + " Item Id:" + item.getIdItem());
                    if (Objects.nonNull(item.getCoupon()) && item.getCoupon()) {
                        Coupon coupon = ofy().load().type(Coupon.class).filter("itemId", item.getIdItem()).first().now();
                        if (Objects.nonNull(coupon)) {
//                            LOG.info("Coupon in cart -> " + coupon.getCouponId() + ", " + coupon.getExpirationDate() + ", couponExpire? " + coupon.getExpires());
                            Calendar calendarNow = GregorianCalendar.getInstance(TimeZone.getTimeZone("America/Bogota"));
                            calendarNow.setTime(new Date());
//                            LOG.info("actual date -> " + calendarNow.getTime());
                            if (Objects.nonNull(coupon.getExpirationDate())) {
                                Calendar calendarExpireCoupon = GregorianCalendar.getInstance(TimeZone.getTimeZone("America/Bogota"));
                                calendarExpireCoupon.setTime(new Date(coupon.getExpirationDate()));
//                                LOG.info("cupon expire -> " + calendarExpireCoupon.getTime());
                                if (calendarExpireCoupon.after(calendarNow)) {
//                                    LOG.info("Cupon valido no se debe eliminar..");
                                } else {
//                                    LOG.info("Cupon invalido se procede a elimnar del carrito");
                                    deleteCoupons = true;
                                    deliveryOrderItemToDelete = item;
                                }
                            }
                        } else {
//                            LOG.info("Cupon no encontrado se procede a elimnar del carrito");
                            deleteCoupons = true;
                            deliveryOrderItemToDelete = item;
                        }
                    }
                }
                try {
                    if (deleteCoupons) {
                        ofy().delete().entity(deliveryOrderItemToDelete).now();
                        deliveryOrderItemListDS.remove(deliveryOrderItemToDelete);
                    }
                } catch (Exception e) {
                    LOG.warning("Error al remover cupon: " + e.getMessage());
                }
            }
            return deliveryOrderItemListDS;
        }
        return new ArrayList<>();
    }

    @NotNull
    private DeliveryOrder scanAndGoValidateCart(boolean isScanAndGo, ShoppingCartJson shoppingCartJson, DeliveryOrder deliveryOrder) {
        // delete items scan and go if cart is normal
        if (deliveryOrder.getItemList() != null && !deliveryOrder.getItemList().isEmpty()) {
            if (!isScanAndGo) {
                deliveryOrder.getItemList().removeIf(item -> (item.getScanAndGo() != null && item.getScanAndGo()));
            } else {
                // only show items scan and go if scan and go is selected
                deliveryOrder.getItemList().removeIf(item -> (item.getScanAndGo() == null || !item.getScanAndGo()));
            }
        }
        addBarcodeInShoppingCart(deliveryOrder.getItemList(), String.valueOf(shoppingCartJson.getIdStoreGroup()));
        return deliveryOrder;
    }

    //  validar si la orden no tiene items express, pero si tiene cupon con proveedor

    /**
     * validate order items farmatodo with coupon and items provider
     *
     * @param deliveryOrder
     */
    private void validateIfOrderOnlyCouponProviders(DeliveryOrder deliveryOrder) {

        if (deliveryOrder.getProviderList() != null && !deliveryOrder.getProviderList().isEmpty()) {
            boolean hasItemsExpress =
                    deliveryOrder.getItemList().stream()
                            .anyMatch(item -> item.getCoupon() == null || !item.getCoupon());

            if (!hasItemsExpress) {

                Optional<DeliveryOrderItem> optionalItemIsCoupon =
                        deliveryOrder.getItemList().stream()
                                .filter(item -> (item.getCoupon() != null && item.getCoupon()))
                                .findFirst();
                if (optionalItemIsCoupon.isPresent()) {
                    DeliveryOrderItem coupon = optionalItemIsCoupon.get();
                    deliveryOrder.getItemList().removeIf(DeliveryOrderItem::getCoupon);
                    deliveryOrder.getProviderList().stream()
                            .findFirst()
                            .ifPresent(deliveryOrderProvider -> deliveryOrderProvider.getItemList().add(coupon));
                    double totalPrice = deliveryOrder.getTotalPrice() - deliveryOrder.getDeliveryPrice();
                    deliveryOrder.setTotalPrice(totalPrice);
                    deliveryOrder.setDeliveryPrice(0);

                    if (deliveryOrder.getQuantityFarmatodo() > 0) deliveryOrder.setQuantityFarmatodo(0);
                }
            }
        }
    }

    private DeliveryTimeLabelTemplate getCustomDeliveryTimeLabelTemplateForThisOrder(final DeliveryOrder deliveryOrder) {
//        LOG.info("method: getCustomDeliveryTimeLabelTemplateForThisOrder()");
        Optional<DeliveryTimeLabelTemplate> optionalDeliveryTimeLabelTemplate = APIAlgolia.getDeliveryTimeLabelTemplate();
        DeliveryTimeLabelTemplate deliveryTimeLabelTemplate = optionalDeliveryTimeLabelTemplate.isPresent() ? optionalDeliveryTimeLabelTemplate.get() : null;


//        LOG.info("IF(deliveryTimeLabelTemplate != null) => [" + (deliveryTimeLabelTemplate != null) + "]");
        if (deliveryTimeLabelTemplate != null) {

            String deliveryType;

            if (deliveryOrder == null || deliveryOrder.getDeliveryType() == null || deliveryOrder.getDeliveryType().getDeliveryType() == null) {
//                LOG.info("La orden no tiene deliveryType. Se enviara como EXPRESS por defecto.");
                deliveryType = "EXPRESS";
            } else {
                deliveryType = deliveryOrder.getDeliveryType().getDeliveryType();
            }

            Map<String, String> timeAndLabel = getLabelAndTimeByDeliveryType(deliveryType);

            final String time = timeAndLabel.get("TIME");
            final String label = timeAndLabel.get("LABEL");

            final String TAG_DELIVERY_TYPE = "{DELIVERY_TYPE}";
            final String TAG_DELIVERY_TIME = "{DELIVERY_TIME}";


            //Para Web:
            final String cartLabel = deliveryTimeLabelTemplate.getDeliveryTimeLabelWeb().getCartLabel().replace(TAG_DELIVERY_TYPE, label);
//            LOG.info("cartLabel => " + cartLabel);
            deliveryTimeLabelTemplate.getDeliveryTimeLabelWeb().setCartLabel(cartLabel);

            final String cartLabelTime = deliveryTimeLabelTemplate.getDeliveryTimeLabelWeb().getCartLabelTime().replace(TAG_DELIVERY_TIME, time);
//            LOG.info("cartLabelTime => " + cartLabelTime);
            deliveryTimeLabelTemplate.getDeliveryTimeLabelWeb().setCartLabelTime(cartLabelTime);

            final String summaryLabel = deliveryTimeLabelTemplate.getDeliveryTimeLabelWeb().getSummaryLabel().replace(TAG_DELIVERY_TYPE, label);
//            LOG.info("summaryLabel => " + summaryLabel);
            deliveryTimeLabelTemplate.getDeliveryTimeLabelWeb().setSummaryLabel(summaryLabel);

            //para Apps:
            final String cartLabelApp = deliveryTimeLabelTemplate.getDeliveryTimeLabelMobile().getCartLabel().replace(TAG_DELIVERY_TYPE, label);
//            LOG.info("cartLabelApp => " + cartLabelApp);
            deliveryTimeLabelTemplate.getDeliveryTimeLabelMobile().setCartLabel(cartLabelApp);

            final String cartLabelTimeApp = deliveryTimeLabelTemplate.getDeliveryTimeLabelMobile().getCartLabelTime().replace(TAG_DELIVERY_TIME, time);
//            LOG.info("cartLabelTimeApp => " + cartLabelTimeApp);
            deliveryTimeLabelTemplate.getDeliveryTimeLabelMobile().setCartLabelTime(cartLabelTimeApp);

            final String summaryLabelApp = deliveryTimeLabelTemplate.getDeliveryTimeLabelMobile().getSummaryLabel().replace(TAG_DELIVERY_TYPE, label);
//            LOG.info("summaryLabelApp => " + summaryLabelApp);
            deliveryTimeLabelTemplate.getDeliveryTimeLabelMobile().setSummaryLabel(summaryLabelApp);

        }
        return deliveryTimeLabelTemplate;
    }

    private Map<String, String> getLabelAndTimeByDeliveryType(final String deliveryType) {
//        LOG.info("method: getLabelAndTimeByDeliveryType()");

        Optional<DeliveryTimeForDeliveryTypeObject> optionalDeliveryTimeForDeliveryTypeObject = APIAlgolia.getDeliveryTimeForDeliveryTypeObject();
        DeliveryTimeForDeliveryTypeObject deliveryTimeForDeliveryTypeObject = optionalDeliveryTimeForDeliveryTypeObject.isPresent() ? optionalDeliveryTimeForDeliveryTypeObject.get() : null;

        Map<String, String> result = new HashMap<>();

        if (deliveryTimeForDeliveryTypeObject != null) {
            switch (deliveryType) {
                case "NATIONAL":
                    result = getMapByDeliveryType(deliveryTimeForDeliveryTypeObject, "NATIONAL");
                    break;
                case "ENVIALOYA":
                    result = getMapByDeliveryType(deliveryTimeForDeliveryTypeObject, "ENVIALOYA");
                    break;
                case "EXPRESS":
                default:
                    result = getMapByDeliveryType(deliveryTimeForDeliveryTypeObject, "EXPRESS");
                    break;
            }
        }

        return result;

    }

    private Map<String, String> getMapByDeliveryType(DeliveryTimeForDeliveryTypeObject deliveryTimeForDeliveryTypeObject, final String deliveryType) {
//        LOG.info("method: getMapByDeliveryType()");
        Map<String, String> result = new HashMap<>();

        deliveryTimeForDeliveryTypeObject.getDeliveryTimeForDeliveryType().stream()
                .filter(element -> Objects.nonNull(element) && Objects.nonNull(element.getType()))
                .forEach(element -> {
                    //LOG.info(element.getType() + "==" + deliveryType);
                    if (element.getType().equalsIgnoreCase(deliveryType)) {
                        result.put("TIME", element.getTime());
                        result.put("LABEL", element.getLabel());
                    }
                });
//        LOG.info("Result:" + result.toString());
        return result;
    }

    private void addDeliveryItemOrder(final ItemAlgolia itemOrder,
                                      final ShoppingCartJson shoppingCartJson,
                                      final Key<DeliveryOrder> deliveryOrderKey,
                                      final List<DeliveryOrderItem> deliveryOrderItemListToSave,
                                      DeliveryOrder deliveryOrder,
                                      DeliveryOrderProvider providerOrder,
                                      final List<DeliveryOrderItem> deliveryOrderItemListOrigin) throws BadRequestException {
        if (itemOrder != null && itemOrder.getPrice() >= 0 && itemOrder.getItem() != 1053709L) {
//            LOG.info("method: addDeliveryItemOrder : " + itemOrder.getItem() + " getIdStoreGroup:" + shoppingCartJson.getIdStoreGroup() + " - " + new DateTime());
            //Consulta item en Algolia
            //Item item = productsMethods.setFindInformationToAlgoliaByIdItem(Long.toString(itemOrder.getItem()), shoppingCartJson.getIdStoreGroup());
            Item item = APIAlgolia.getItemToItemAlgolia(new Item(), itemOrder);

            if (item == null) {
//                LOG.info("fuenulol");
                throw new BadRequestException("ITEM NO ENCONTRADO");

            }
            DeliveryOrderItem deliveryOrderItem = new DeliveryOrderItem();
            deliveryOrderItem.setRequirePrescriptionImage(itemOrder.isRequirePrescriptionImage());
            deliveryOrderItem.setIdDeliveryOrderItem(UUID.randomUUID().toString());
            deliveryOrderItem.setIdItem(Key.create(Item.class, item.getItemId()));
            deliveryOrderItem.setCreateDate(new Date());
            deliveryOrderItem.setQuantitySold(itemOrder.getQuantityRequested());
            for (DeliveryOrderItem itOrigin : deliveryOrderItemListOrigin) {
                if (itOrigin.getId() == itemOrder.getItem()) {
                    if (itOrigin.getHandleQuantity() != null) {
                        deliveryOrderItem.setHandleQuantity(itOrigin.getHandleQuantity());
                    }

                    if (itOrigin.getFilters() != null) {
                        deliveryOrderItem.setFilters(itOrigin.getFilters());
                    }
                    break;
                }
            }
            if (itemOrder.isTalonDiscount() && itemOrder.isTalonItemFree()) {
                item.setPrimePrice(0D);
                item.setPrimeDescription(null);
                item.setPrimeTextDiscount(null);
            }
            //LOG.warning("method: addDeliveryItemOrder : " + itemOrder);
            if (itemOrder.getPrice() != itemOrder.getDiscount()) {
                //LOG.warning("method: addDeliveryItemOrder FullPrice: " +itemOrder.getFullPrice()+" "+ itemOrder.getPrice());
                deliveryOrderItem.setFullPrice(itemOrder.getPrice());
            } else
                deliveryOrderItem.setFullPrice(0D);
            //LOG.warning("method: addDeliveryItemOrder : getDiscount" + itemOrder.getDiscount());
            if (itemOrder.getDiscount() != 0)
                deliveryOrderItem.setOfferPrice(itemOrder.getPrice() - (itemOrder.getDiscount() / itemOrder.getQuantityRequested()));
            else
                deliveryOrderItem.setOfferPrice(0D);
            deliveryOrderItem.setChangeQuantity(itemOrder.getAccess());
            // Prime
            deliveryOrderItem.setPrimePrice(item.getPrimePrice());
            deliveryOrderItem.setPrimeDescription(item.getPrimeDescription());
            deliveryOrderItem.setPrimeTextDiscount(item.getPrimeTextDiscount());
            deliveryOrderItem.setRms_deparment(Objects.nonNull(item.getRms_deparment()) ? item.getRms_deparment() : "");
            deliveryOrderItem.setRms_class(Objects.nonNull(item.getRms_class()) ? item.getRms_class() : "");
            deliveryOrderItem.setRms_subclass(Objects.nonNull(item.getRms_subclass()) ? item.getRms_subclass() : "");
            deliveryOrderItem.setRms_group(Objects.nonNull(item.getRms_group()) ? item.getRms_group() : "");

            //Talon
            deliveryOrderItem.setTalonDiscount(itemOrder.isTalonDiscount());
            deliveryOrderItem.setTalonItemFree(itemOrder.isTalonItemFree());

            deliveryOrderItem = this.deliveryOrderItemReturn(deliveryOrderItem, item);

            //mark onlyOnline via Algolia
            if (Objects.nonNull(itemOrder.getColor())) {
                deliveryOrderItem.setColor(itemOrder.getColor());
            }
            if (Objects.nonNull(itemOrder.getFilter())) {
                deliveryOrderItem.setFilter(itemOrder.getFilter());
            }
            if (Objects.nonNull(itemOrder.getFilterType())) {
                deliveryOrderItem.setFilterType(itemOrder.getFilterType());
            }

            if (Objects.nonNull(itemOrder.getUrl())) {
                deliveryOrderItem.setUrl(itemOrder.getUrl());
            }
            if (Objects.nonNull(itemOrder.getFilterCategories())) {
                deliveryOrderItem.setFilterCategories(itemOrder.getFilterCategories());
            }

            if (Objects.nonNull(item.getCustomTag())) {
                deliveryOrderItem.setCustomTag(item.getCustomTag());
            }

            deliveryOrderItem.setOnlyOnline(item.isOnlyOnline());
            deliveryOrderItem.setDeliveryTime(item.getDeliveryTime());
            deliveryOrderItem.setCategorie(item.getCategorie());
            deliveryOrderItem.setMarca(item.getMarca());
            deliveryOrderItem.setDepartments(item.getDepartments());
            deliveryOrderItem.setSubCategory(item.getSubCategory());
            deliveryOrderItem.setSupplier(item.getSupplier());
            deliveryOrderItem.setRequirePrescription(item.getRequirePrescription());
//            LOG.info("ARREGLO CARRITO : " + deliveryOrderItem.toStringJson());

            // Elimina metodo addMarcaCategorySubcategorieAndItemUrl
//            LOG.info("Agrega info URL Item ");
            deliveryOrderItem.setSubCategory(item.getSubCategory());
            deliveryOrderItem.setCategorie(item.getCategorie());
            deliveryOrderItem.setMarca(item.getMarca());
            deliveryOrderItem.setItemUrl(item.getItemUrl());
//            deliveryOrderItem.setDepartments(item.getDepartments());
//            LOG.info("Agrega info URL Item " + item.getSubCategory() + " " + item.getCategorie() + " " + item.getMarca() + " " + item.getItemUrl());

            //LOG.warning("deliveryOrderItem: " + deliveryOrderItem.toStringJson());
            // Busca el origen del Item
            if (Objects.nonNull(deliveryOrderItemListOrigin)) {
                final DeliveryOrderItem deliveryOrderFind = deliveryOrderItem;
                if (deliveryOrderItemListOrigin.stream()
                        .filter(itemOrigin -> Objects.nonNull(deliveryOrderFind.getId()) && Objects.nonNull(itemOrigin.getId()) &&
                                itemOrigin.getId() == deliveryOrderFind.getId()).findFirst().isPresent()) {
                    deliveryOrderItem.setOrigin(deliveryOrderItemListOrigin.stream().filter(itemOrigin -> itemOrigin.getId() == deliveryOrderFind.getId()).findFirst().get().getOrigin());
                    deliveryOrderItem.setObservations(deliveryOrderItemListOrigin.stream().filter(itemOrigin -> itemOrigin.getId() == deliveryOrderFind.getId()).findFirst().get().getObservations());
                    deliveryOrderItem.setSubstitute(deliveryOrderItemListOrigin.stream().filter(itemOrigin -> itemOrigin.getId() == deliveryOrderFind.getId()).findFirst().get().getSubstitute());
                }
            }

            if (Objects.nonNull(itemOrder.getFiltersOptical())) {
                deliveryOrderItem.setFiltersOptical(itemOrder.getFiltersOptical());
            }

            //Data Marketplace
            deliveryOrderItem.setDimensions(itemOrder.getDimensions());

            if (Objects.nonNull(itemOrder.getVariants())) {
                deliveryOrderItem.setVariants(itemOrder.getVariants());
            }

            if(Objects.nonNull(itemOrder.getSellerAddresses())) {
                deliveryOrderItem.setSellerAddresses(itemOrder.getSellerAddresses());
            }

            if(Objects.nonNull(itemOrder.getDeliveryDays())){
                deliveryOrderItem.setDeliveryDays(itemOrder.getDeliveryDays());
            }

            if(Objects.nonNull(itemOrder.getUuidItem())){
                deliveryOrderItem.setUuidItem(itemOrder.getUuidItem());
            }

            deliveryOrderItemListToSave.add(deliveryOrderItem);
            deliveryOrder.setSubTotalPrice(deliveryOrder.getSubTotalPrice() + itemOrder.getFullPrice());
            deliveryOrder.setOfferPrice(deliveryOrder.getOfferPrice() + itemOrder.getDiscount());
            if(Objects.nonNull(itemOrder.isRequirePrescriptionMedical())){
                deliveryOrderItem.setRequirePrescriptionMedical(itemOrder.isRequirePrescriptionMedical());
            }
            if (Objects.nonNull(providerOrder)) {
                saveProviderItem(deliveryOrderKey, providerOrder, deliveryOrderItem);
            }
        } else {
            deliveryOrder.setOfferPrice(deliveryOrder.getOfferPrice() + Math.abs(itemOrder.getPrice()));
        }
    }

    private DeliveryOrder getEmptyDeliveryOrder(DeliveryOrder deliveryOrder) {
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

    private void saveProviderItem(final Key<DeliveryOrder> deliveryOrderKey, DeliveryOrderProvider providerOrder, final DeliveryOrderItem deliveryOrderItemBase) {
        //LOG.warning("method: saveProviderItem Init");
        String uuiKey = generateProviderKey(Key.create(deliveryOrderKey, DeliveryOrderProvider.class, Long.toString(providerOrder.getId())).toWebSafeString(),
                providerOrder.getId());
        Key<DeliveryOrderProvider> findKey = Key.create(deliveryOrderKey, DeliveryOrderProvider.class, uuiKey);
        DeliveryOrderProvider deliveryOrderProvider = ofy().load().type(DeliveryOrderProvider.class).filterKey("=", findKey).first().now();
        //LOG.warning("method: saveProviderItem Init Key : " + uuiKey);
        //LOG.warning("method: saveProviderItem Init Find Key : " + findKey);
        if (Objects.isNull(deliveryOrderProvider)) {
            providerOrder.setIdDeliveryOrder(Ref.create(deliveryOrderKey));
            providerOrder.setIdDeliveryOrderProvider(uuiKey);
            providerOrder.setItemList(new ArrayList<>());
        } else {
            providerOrder = deliveryOrderProvider;
        }
        // si el item existe se reemplaza por la ultima version
        Optional<DeliveryOrderItem> optionalDeliveryOrderItem = providerOrder.getItemList().stream()
                .filter(itemProvider -> itemProvider.getId() == deliveryOrderItemBase.getId()).findFirst();
        if (optionalDeliveryOrderItem.isPresent()) {
            //LOG.warning("method: saveProviderItem Encuentra Item");
            providerOrder.getItemList().remove(optionalDeliveryOrderItem.get());
        }
        // Se agrega item al proveedor
        providerOrder.getItemList().add(deliveryOrderItemBase);

        //Key<DeliveryOrderProvider> deliveryOrderProviderKey = ofy().save().entity(providerOrder).now();
        //LOG.warning("method: saveProviderItem Crea Proveedor Key: " + deliveryOrderProviderKey);
        //Async Save
        ofy().save().entity(providerOrder).now();
    }

    private String generateProviderKey(String providerKey, Long providerId) {
        return UUID.nameUUIDFromBytes((providerKey + providerId).getBytes()).toString();
    }

    @ApiMethod(name = "getOrderv2", path = "/orderEndpoint/v2/getOrder", httpMethod = ApiMethod.HttpMethod.GET)
    public GetOrderResponse getOrderV2(@Named("token") final String token,
                                  @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                  @Nullable @Named("idCustomerWebSafe") final String idCustomerWebSafe,
                                  @Named("idOrder") final long idOrder, final HttpServletRequest req) throws ConflictException, BadRequestException, NotFoundException {

        GetOrderResponse getOrderResponse = new GetOrderResponse();

        try {
            getOrderResponse.setData(getOrder(token, tokenIdWebSafe, idCustomerWebSafe ,idOrder, req));
            getOrderResponse.setCode(HttpStatusCode.OK.getStatusName());
            getOrderResponse.setMessage("Success");
        } catch (ConflictException e) {
            getOrderResponse.setCode(HttpStatusCode.CONFLICT.getStatusName());
            getOrderResponse.setMessage(e.getMessage());
        }
        return getOrderResponse;
    }

    @ApiMethod(name = "getOrder", path = "/orderEndpoint/getOrder", httpMethod = ApiMethod.HttpMethod.GET)
    public DeliveryOrder getOrder(@Named("token") final String token,
                                  @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                  @Nullable @Named("idCustomerWebSafe") final String idCustomerWebSafe,
                                  @Named("idOrder") final long idOrder, final HttpServletRequest req) throws ConflictException, BadRequestException, NotFoundException {


        //LOG.warning("method: getOrder idOrder: " + idOrder);
        if (!authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);

        DeliveryOrder deliveryOrder = getOrderMethod(null, idOrder, false, true);

        // fix order status.

//        LOG.info("DeliveryType --> " + deliveryOrder.getDeliveryType().getDeliveryType());
        if (deliveryOrder.getDeliveryType().equals(DeliveryType.EXPRESS)
                || deliveryOrder.getDeliveryType().equals(DeliveryType.SCANANDGO)) {
            Optional<String> optLastStatusDS = getLastStatus(deliveryOrder,null);

            if (optLastStatusDS.isPresent()) {
//                LOG.info("Nuevo_Estado order: " + deliveryOrder.getIdOrder() + "  ---> " + optLastStatusDS.get());
                deliveryOrder.setLastStatus(optLastStatusDS.get());
            }
        }

        RequestSourceEnum source = ftdUtilities.getSourceFromRequestHeader(req);
        deliveryOrder.getItemList().removeIf(DeliveryOrderItem::isSampling);
        if (source.equals(RequestSourceEnum.ANDROID) || source.equals(RequestSourceEnum.IOS)) {
            callDiscountTalon(deliveryOrder);
            LOG.info("ED2 android deliveryOrder: " + new Gson().toJson(deliveryOrder));
            return deliveryOrder;
        }

        // Validate Security FOR WEB
        boolean webSecurityIsEnabled = webSecurityIsEnabled();

        if (!webSecurityIsEnabled) {
            callDiscountTalon(deliveryOrder);
            return deliveryOrder;
        }


        if (idCustomerWebSafe == null || idCustomerWebSafe.isEmpty()) {
            throw new ConflictException(Constants.INVALID_TOKEN);
        }

        Key<User> userKey = Key.create(idCustomerWebSafe);
        User user = users.findUserByKey(userKey);
        /* Solicitar informacion de usuario a farmatodo */

//        LOG.info("user_ftd -> " + user.toStringJson());

        int customerId = user.getId();
        if (customerId <= 0 || deliveryOrder.getIdFarmatodo() <= 0) {
            throw new ConflictException(Constants.INVALID_TOKEN_ID_WEBSAFE);
        }

        if (deliveryOrder.getIdFarmatodo() != customerId) {
            throw new NotFoundException(Constants.NOT_CONTENT);
        }

        try {
            TalonOneService talonOneService = new TalonOneService();
            ShoppingCartJson shoppingCartJson = talonOneService.getShoppingCartJson(deliveryOrder);
            deliveryOrder = talonOneService.sendOrderToTalonOne(deliveryOrder, shoppingCartJson, null);
            TalonOneDeductDiscount talonOneDeductDiscount = talonOneService.retireveDeductDiscount(deliveryOrder.getIdOrder());
            callDeductDiscount(talonOneDeductDiscount, deliveryOrder);
        } catch (Exception e) {
            LOG.info("Error total de Talon One: " + e);
        }
        return deliveryOrder;
    }

    private static void callDiscountTalon(DeliveryOrder deliveryOrder) {
        TalonOneService talonOneService = new TalonOneService();
        TalonOneDeductDiscount talonOneDeductDiscount = talonOneService.retireveDeductDiscount(deliveryOrder.getIdOrder());
        callDeductDiscount(talonOneDeductDiscount, deliveryOrder);
    }

    private static void callDeductDiscount(String idCustomerWebSafe, DeliveryOrder deliveryOrder) {
        Optional<String> discountRpm = CachedDataManager.getJsonFromCacheIndex(idCustomerWebSafe + Constants.KEY_DISCOUNT_RPM, ID_BD_REDIS_DISCOUNT);
        Optional<String> discountPrime = CachedDataManager.getJsonFromCacheIndex(idCustomerWebSafe + Constants.KEY_DISCOUNT_PRIME, ID_BD_REDIS_DISCOUNT);
        Optional<String> discountTalon = CachedDataManager.getJsonFromCacheIndex(idCustomerWebSafe + Constants.KEY_DISCOUNT_TALON, ID_BD_REDIS_DISCOUNT);
        Optional<String> couponCache = CachedDataManager.getJsonFromCacheIndex(idCustomerWebSafe + Constants.KEY_COUPON_CACHE, ID_BD_REDIS_DISCOUNT);
        Optional<String> couponAutomatic = CachedDataManager.getJsonFromCacheIndex(idCustomerWebSafe + Constants.KEY_COUPON_AUTOMATIC, ID_BD_REDIS_DISCOUNT);
        Optional<String> totalSaveProducts = CachedDataManager.getJsonFromCacheIndex(idCustomerWebSafe + Constants.KEY_TOTAL_SAVE, ID_BD_REDIS_DISCOUNT);
        Optional<String> productsTotalOffer = CachedDataManager.getJsonFromCacheIndex(idCustomerWebSafe + Constants.KEY_OFFER_PRICE, ID_BD_REDIS_DISCOUNT);
        LOG.info("couponTalonOrRPM: " + totalSaveProducts + " - " + discountPrime + " - " + discountTalon + " - " + couponAutomatic + " - " + productsTotalOffer + " - " + couponCache);
        DeductDiscount deductDiscount = new DeductDiscount();
        deductDiscount.setDiscountProductsRPM(discountRpm.isPresent() ? Double.valueOf(discountRpm.get()) : 0D);
        deductDiscount.setDiscountProductsPrime(discountPrime.isPresent() ? Double.valueOf(discountPrime.get()) : 0D);
        deductDiscount.setDiscountProductsCampaignTalon(discountTalon.isPresent() ? Double.valueOf(discountTalon.get()) : 0D);
        deductDiscount.setTotalSaveProducts(totalSaveProducts.isPresent() ? Double.valueOf(totalSaveProducts.get()) : 0D);
        deductDiscount.setDiscountProductsOfferPrice(productsTotalOffer.isPresent() ? Double.valueOf(productsTotalOffer.get()) : 0D);
        deliveryOrder.setDeductDiscount(deductDiscount);
        if (couponAutomatic.isPresent()) {
            List<CouponAutomaticTalon> couponAutomaticTalonList = new Gson().fromJson(couponAutomatic.get(), new TypeToken<List<CouponAutomaticTalon>>() {
            }.getType());
            deliveryOrder.setCouponAutomaticTalonList(couponAutomaticTalonList);
        }

        if (couponCache.isPresent()) {
            com.imaginamos.farmatodo.model.talonone.Coupon coupon = new com.imaginamos.farmatodo.model.talonone.Coupon();
            AnswerDeduct couponSave = new Gson().fromJson(couponCache.get(), AnswerDeduct.class);
            coupon.setNameCoupon(couponSave.getNameCoupon());
            coupon.setDiscountCoupon(couponSave.getDiscount());
            coupon.setTypeNotificacion(couponSave.getTypeNotifcation());
            coupon.setTalonOneOfferDescription(couponSave.getNotificationMessage());
            deliveryOrder.setCoupon(coupon);
            Double sumTotalItems = 0D;
            sumTotalItems = deliveryOrder.getItemList().stream().filter(item -> item.getId() != 1053709L).mapToDouble(item -> item.getFullPrice() * item.getQuantitySold()).sum();
            deliveryOrder.setSubTotalPrice(sumTotalItems);
            Double sumDeductDiscount = 0D;
            sumDeductDiscount = deliveryOrder.getDeductDiscount().getTotalSaveProducts() + deliveryOrder.getDeductDiscount().getDiscountProductsPrime();
            Double sumCouponAutomatic = 0D;
            sumCouponAutomatic = deliveryOrder.getCouponAutomaticTalonList().stream().mapToDouble(couponAutomaticTalon -> couponAutomaticTalon.getDiscountCoupon()).sum();
            deliveryOrder.setOfferPrice(sumDeductDiscount + sumCouponAutomatic);

        }
    }

    private static void callDeductDiscount(TalonOneDeductDiscount talonOneDeductDiscount, DeliveryOrder deliveryOrder) {
        if (Objects.nonNull(talonOneDeductDiscount)) {
            Double sumTotalItems = 0D;
            Double sumDeductDiscount = 0D;
            Double sumCouponAutomatic = 0D;
            Double discountCouponTalon = 0D;
            if (Objects.nonNull(talonOneDeductDiscount.getNameAutomaticCoupon()) && Objects.nonNull(talonOneDeductDiscount.getDiscountAutomaticCoupon())) {
                List<CouponAutomaticTalon> couponAutomaticTalonList = new ArrayList<>();
                CouponAutomaticTalon couponAutomaticTalon = new CouponAutomaticTalon();
                couponAutomaticTalon.setNameCoupon(talonOneDeductDiscount.getNameAutomaticCoupon());
                couponAutomaticTalon.setDiscountCoupon(talonOneDeductDiscount.getDiscountAutomaticCoupon());
                couponAutomaticTalon.setTypeNotificacion("Info");
                couponAutomaticTalonList.add(couponAutomaticTalon);
                deliveryOrder.setCouponAutomaticTalonList(couponAutomaticTalonList);
                sumCouponAutomatic = talonOneDeductDiscount.getDiscountAutomaticCoupon();
            }
            if (Objects.nonNull(talonOneDeductDiscount.getNameCoupon()) && Objects.nonNull(talonOneDeductDiscount.getDiscountCoupon())) {
                com.imaginamos.farmatodo.model.talonone.Coupon coupon = new com.imaginamos.farmatodo.model.talonone.Coupon();
                coupon.setNameCoupon(talonOneDeductDiscount.getNameCoupon());
                coupon.setDiscountCoupon(talonOneDeductDiscount.getDiscountCoupon());
                coupon.setTypeNotificacion("Info");
                deliveryOrder.setCoupon(coupon);
                discountCouponTalon = talonOneDeductDiscount.getDiscountCoupon();
            }
            DeductDiscount deductDiscount = new DeductDiscount();
            if (Objects.nonNull(talonOneDeductDiscount.getDiscountProductRpm())) {
                deductDiscount.setTotalSaveProducts(talonOneDeductDiscount.getDiscountProductRpm());
            }
            if (Objects.nonNull(talonOneDeductDiscount.getDiscountProductPrime())) {
                deductDiscount.setDiscountProductsPrime(talonOneDeductDiscount.getDiscountProductPrime());
            }
            deliveryOrder.setDeductDiscount(deductDiscount);
            sumDeductDiscount = deliveryOrder.getDeductDiscount().getTotalSaveProducts() + deliveryOrder.getDeductDiscount().getDiscountProductsPrime();
            LOG.info("sumDeductDiscount: " + sumDeductDiscount + " - sumCouponAutomatic: " + sumCouponAutomatic + " - discountCouponTalon: " + discountCouponTalon);
            if(Objects.nonNull(deliveryOrder.getItemList()) && !deliveryOrder.getItemList().isEmpty()  && deliveryOrder.getProviderList().size() == 0){
                sumTotalItems = deliveryOrder.getItemList().stream().filter(item -> item.getId() != 1053709L).mapToDouble(item -> item.getFullPrice() * item.getQuantitySold()).sum();
                deliveryOrder.setSubTotalPrice(sumTotalItems);
            }else{
                deliveryOrder.setSubTotalPrice((Objects.nonNull(deliveryOrder.getSubTotalPrice()) ? deliveryOrder.getSubTotalPrice() : 0) + sumCouponAutomatic + discountCouponTalon );
            }
            deliveryOrder.setOfferPrice(sumDeductDiscount + sumCouponAutomatic);
            Double totalPrice=deliveryOrder.getSubTotalPrice() - (deliveryOrder.getOfferPrice() + discountCouponTalon)
                    +(Objects.nonNull(deliveryOrder.getDeliveryPrice())?deliveryOrder.getDeliveryPrice():0D);

            if (Objects.nonNull(talonOneDeductDiscount.getUsedFarmacredits())) {
                deliveryOrder.setUsedCredits(Double.valueOf(talonOneDeductDiscount.getUsedFarmacredits()));
                //deliveryOrder.setOfferPrice(deliveryOrder.getOfferPrice() + talonOneDeductDiscount.getUsedFarmacredits());
            }
            //LOG.info("totalPrice: " + deliveryOrder.getTotalPrice());
            //LOG.info("totalPrice2: " + totalPrice);
            /*if(totalPrice>0){
                deliveryOrder.setTotalPrice(totalPrice);
            }*/
        }
    }


    @ApiMethod(name = "getOrderSelfCheckout", path = "/orderEndpoint/getOrderSelfCheckout", httpMethod = ApiMethod.HttpMethod.GET)
    public DeliveryOrder getOrderSelfCheckout(@Named("idOrder") final long idOrder) throws ConflictException, BadRequestException, NotFoundException, IOException {


        DeliveryOrder deliveryOrder = new DeliveryOrder();

        List<ItemsOrderDomain> itemsOrderDomainList = ApiGatewayService.get().getInfoItemsByIdOrder(idOrder);
        ReadOrderResponse readOrderResponse = ApiGatewayService.get().getReadOrder(idOrder).getData();

        if (itemsOrderDomainList != null && readOrderResponse != null) {
            List<DeliveryOrderItem> deliveryOrderItemList = validateItemsSelf(itemsOrderDomainList);
            deliveryOrder.setItemList(deliveryOrderItemList);
            deliveryOrder.setIdFarmatodo(Integer.parseInt(readOrderResponse.getCustomer()));
        }


        return deliveryOrder;
    }

    private List<DeliveryOrderItem> validateItemsSelf(List<ItemsOrderDomain> itemsOrderDomainList) {

        List<DeliveryOrderItem> deliveryOrderItemList = new ArrayList<>();
        if (!itemsOrderDomainList.isEmpty()) {
            for (ItemsOrderDomain item : itemsOrderDomainList) {
                DeliveryOrderItem deliveryOrderItem = new DeliveryOrderItem();
                deliveryOrderItem.setQuantitySold(item.getQuantitySold());
                deliveryOrderItem.setAnywaySelling(item.isAnywaySelling());
                deliveryOrderItem.setFullPrice(item.getFullPrice());
                deliveryOrderItem.setGrayDescription(item.getGrayDescription());
                deliveryOrderItem.setHighlight(item.isHighlight());
                deliveryOrderItem.setMediaDescription(item.getMediaDescription());
                deliveryOrderItem.setMediaImageUrl(item.getMediaImageUrl());
                deliveryOrderItem.setOutstanding(item.isOutstanding());
                deliveryOrderItem.setSales(Long.parseLong(item.getSales()));
                deliveryOrderItem.setTotalStock(Integer.parseInt(String.valueOf(item.getTotalStock())));
                deliveryOrderItem.setId(Long.parseLong(item.getId()));
                deliveryOrderItem.setChangeQuantity(item.isChangeQuantity());
                deliveryOrderItem.setOnlyOnline(item.isOnlyOnline());
                deliveryOrderItem.setGeneric(item.isGeneric());
                deliveryOrderItemList.add(deliveryOrderItem);
            }
        }
        return deliveryOrderItemList;
    }

    private boolean webSecurityIsEnabled() {
        Optional<SecurityWebConfig> optionalSecurityWebConfig = APIAlgolia.getSecurityConfig();

        if (optionalSecurityWebConfig.isPresent() && optionalSecurityWebConfig.get().getEnable() != null) {

            return optionalSecurityWebConfig.get().getEnable();
        }

        return false;
    }

    private boolean notAllowedTracking(int statusId) {

        return statusId < 3;
    }


    void setBarcodesForItems(List<DeliveryOrderItem> deliveryOrderItemList) {
        deliveryOrderItemList.forEach(item -> {
            try {
                ItemAlgolia itemAlgolia = APIAlgolia.getItemAlgoliaById(item.getId(), 26L);

                if (Objects.nonNull(itemAlgolia.getBarcode()))
                    item.setBarcode(itemAlgolia.getBarcode());

                if (Objects.nonNull(itemAlgolia.getBarcodeList()))
                    item.setBarcodeList(itemAlgolia.getBarcodeList());

            } catch (Exception e) {
                LOG.warning("El item -> " + item.getId() + " tiene problemas.");
            }
        });
    }

    private void setNamePaymentType(DeliveryOrder order) {
        if (order.getPaymentType() != null) {
            Integer id = ((int) order.getPaymentType().getId());
            switch (id) {
                case 1:
                    order.getPaymentType().setName(PaymentTypeEnum.EFECTIVO.name());
                    break;
                case 2:
                    order.getPaymentType().setName(PaymentTypeEnum.DATAFONOS.name());
                    break;
                case 3:
                    order.getPaymentType().setName(PaymentTypeEnum.TRANSACCIONES_EN_LINEA.name());
                    break;
                case 6:
                    order.getPaymentType().setName(PaymentTypeEnum.PSE.name());
                    break;
                default:
                    order.getPaymentType().setName("");

            }
        }
    }

    /**
     * Metodo para verificar los items facturados realmente contra los que vienen de DS.
     *
     * @param deliveryOrderItemListAux
     * @param itemsBilled
     */
    private void validateItemsBilled(List<DeliveryOrderItem> deliveryOrderItemListAux, List<OrderQuantityItem> itemsBilled) {
        try {
            if (!deliveryOrderItemListAux.isEmpty()) {
                deliveryOrderItemListAux.forEach(item -> {
                    itemsBilled.forEach(itemBuild -> {
                        if (item.getId() == itemBuild.getItemId()) {
                            item.setQuantitySold(itemBuild.getQuantity().intValue());
                        }
                    });
                });
            }
        } catch (Exception e) {
            LOG.warning("Ocurrio un error -> " + e.getMessage());
        }
    }


    /**
     * Valida si la orden fue resmplazada por otra
     *
     * @param deliveryOrder
     */
    private void validateOrderCanceled(DeliveryOrder deliveryOrder) {
        if (Objects.nonNull(deliveryOrder) && Objects.nonNull(deliveryOrder.getLastStatus()) && ORDER_CANCELED.equals(deliveryOrder.getLastStatus())) {
            try {
                ReadOrderResponseBackend3 orderResponseBackend3 = ApiGatewayService.get().getReadActiveOrder(deliveryOrder.getIdOrder());
                if (Objects.nonNull(orderResponseBackend3) && Objects.nonNull(orderResponseBackend3.getData()) && Objects.nonNull(orderResponseBackend3.getData().getPickingDate())) {
                    deliveryOrder.setPickingDate(new Date(orderResponseBackend3.getData().getPickingDate()));
                }
            } catch (IOException e) {
                LOG.info("No fue posible consultar las ordenes activas. " + e.getMessage());
            }
        }
    }

    private void generateOrderTotals(DeliveryOrder deliveryOrder) {
        try {
            GetOrderSumary orderSummary = ApiGatewayService.get().getOrderSumary(deliveryOrder.getIdOrder());

            if (orderSummary == null || orderSummary.getData() == null
                    || orderSummary.getData().getOrderId() == null ||
                    orderSummary.getData().getInvoiceValue() == null) {
                //LOG.warning(" Order Summary no encontro Nada para la orden");
                return;
            }

            if (Objects.isNull(orderSummary.getData())) {
                // Realiza el calculo de totales de la orden
                deliveryOrder.setSubTotalPrice(Objects.nonNull(deliveryOrder.getSubTotalPrice()) ? deliveryOrder.getSubTotalPrice() : 0);
                deliveryOrder.setOfferPrice(Objects.nonNull(deliveryOrder.getOfferPrice()) ? deliveryOrder.getOfferPrice() : 0);
                deliveryOrder.setDeliveryPrice(Objects.nonNull(deliveryOrder.getDeliveryPrice()) ? deliveryOrder.getDeliveryPrice() : 0);
                deliveryOrder.setProviderDeliveryPrice(Objects.nonNull(deliveryOrder.getProviderDeliveryPrice()) ? deliveryOrder.getProviderDeliveryPrice() : 0);
                deliveryOrder.setTotalPrice(deliveryOrder.getSubTotalPrice() - deliveryOrder.getOfferPrice() + (deliveryOrder.getDeliveryPrice() + deliveryOrder.getProviderDeliveryPrice()));
            } else {
                deliveryOrder.setTotalPrice(orderSummary.getData().getInvoiceValue());
                deliveryOrder.setSubTotalPrice(orderSummary.getData().getOrderValue());
                deliveryOrder.setOfferPrice(orderSummary.getData().getDiscountValue());
                deliveryOrder.setDeliveryPrice(orderSummary.getData().getDeliveryValue());
                if (Objects.nonNull(orderSummary.getData().getProviderDeliveryValue()) && orderSummary.getData().getProviderDeliveryValue() > 0) {
                    deliveryOrder.setProviderDeliveryPrice(orderSummary.getData().getProviderDeliveryValue());
                }
            }

            double tipPrice = 0D;
            Optional<TipConfig> tipConfigOptional = GrowthBookConfigLoader.getTipsConfig(Constants.CUSTOMER_ANONYMOUS);

            if (tipConfigOptional.isPresent()) {
                // add tip price/
                tipPrice = getTipPriceForOrder(deliveryOrder, tipConfigOptional.get());
            }

            if (tipPrice > 0 && deliveryOrder.getSubTotalPrice() > 0) {
                deliveryOrder.setSubTotalPrice(deliveryOrder.getSubTotalPrice() - tipPrice);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @ApiMethod(name = "getOrders", path = "/orderEndpoint/getOrders", httpMethod = ApiMethod.HttpMethod.POST)
    public CollectionResponseModel myOrders(final Customer customer) throws ConflictException, IOException, BadRequestException, InternalServerErrorException {

        if (customer.getToken() != null && !authenticate.isValidToken(customer.getToken().getToken(), customer.getToken().getTokenIdWebSafe())) {
            throw new ConflictException(Constants.INVALID_TOKEN);
        }
       return getMyOrders(customer.getCursor(),customer.getIdCustomerWebSafe());
    }

    @ApiMethod(name = "getOrders", path = "/orderEndpoint/v2/getOrders", httpMethod = ApiMethod.HttpMethod.POST)
    public Result<CollectionResponseModel> myOrdersV2(final CustomerV2 customer) throws ConflictException, IOException, BadRequestException, InternalServerErrorException {

        if (customer.getToken() != null && !authenticate.isValidToken(customer.getToken(), customer.getTokenIdWebSafe())) {
            throw new ConflictException(Constants.INVALID_TOKEN);
        }

        CollectionResponseModel collectionResponseModel = getMyOrders(customer.getCursor(),customer.getIdCustomerWebSafe());

        Result<CollectionResponseModel> resultOrder= new Result<>();
        resultOrder.setCode(HttpStatusCode.OK.getStatusName());
        resultOrder.setMessage("Success");
        resultOrder.setData(collectionResponseModel);
        return resultOrder;
    }

    @NotNull
    private CollectionResponseModel getMyOrders(final String cursor,final String idCustomerWebSafe) throws IOException, ConflictException {
        int limitResultsClient = 12;
        int countLimit = 0;
        Answer answer = new Answer();

        Query.Filter filterPrevious = new Query.FilterPredicate("isActive", Query.FilterOperator.EQUAL, false);
        Key<Customer> customerKey = Key.create(idCustomerWebSafe);
        CollectionResponseModel collectionResponseModel = new CollectionResponseModel();
        com.googlecode.objectify.cmd.Query<DeliveryOrder> query = ofy().load().type(DeliveryOrder.class)
                .ancestor(customerKey)
                .filter("currentStatus", 0)
                .filter(filterPrevious).order("-createDate");


        if (Objects.isNull(cursor) || cursor.equals("")) {
            Query.Filter filterActive = new Query.FilterPredicate("isActive",
                    Query.FilterOperator.EQUAL, true);
            List<DeliveryOrder> activeOrders = ofy().load().type(DeliveryOrder.class)
                    .ancestor(customerKey)
                    .filter("currentStatus", 0)
                    .filter(filterActive)
                    .order("-createDate").list();

            List<DeliveryOrder> ordersActive = new ArrayList<>();
            for (DeliveryOrder deliveryOrder : activeOrders) {

                validateStatusScandAndGo(answer, deliveryOrder);

                if (Objects.nonNull(deliveryOrder.getDeliveryType())) {
                    LOG.info("DeliveryType " + deliveryOrder.getDeliveryType().toString() + " Order ID:" + deliveryOrder.getIdOrder());
                }

                deliveryOrder = getOrderMethod(null, deliveryOrder.getIdOrder(), true, false);
                Optional<String> optLastStatusDS = validateOrderStatusCanceled(deliveryOrder);
                if (optLastStatusDS.isPresent()) {
                    deliveryOrder.setLastStatus(optLastStatusDS.get());
                }
                OrderUtil.deleteTipPriceZero(deliveryOrder.getItemList());
                ordersActive.add(deliveryOrder);
            }

            // elimina ordenes activas de sag y que sean pagadas con datafono y si las ordenes ya estan finalizadas en oracle
            ordersActive.removeIf(orderActiveAux -> isOrdenScanAndGo(orderActiveAux)
                    && orderActiveAux.getPaymentType().getId() == PaymentTypeEnum.DATAFONOS.getId()
                    && Objects.equals(orderActiveAux.getLastStatus(), ORDER_FINALIZED));

            collectionResponseModel.setActiveOrders(ordersActive);
        }

        query.limit(limitResultsClient);
        //Validate cursor
        if ( Objects.nonNull(cursor))
            query = query.startAt(Cursor.fromWebSafeString(cursor));

        QueryResultIterator<DeliveryOrder> iterator = query.iterator();

        //  Save in list the query candidate
        List<DeliveryOrder> previousOrders = new ArrayList<>();

        //  Load fields of the query
        while (iterator.hasNext() && countLimit < limitResultsClient) {
            DeliveryOrder order = iterator.next();
            List<DeliveryOrderItem> deliveryOrderItemList;
            deliveryOrderItemList = ofy().load().type(DeliveryOrderItem.class).ancestor(order).list();
            order.setCreatedDate(order.getCreateDate().getTime());
            if (isOrdenScanAndGo(order)) {
                deliveryOrderItemList.removeIf(deliveryOrderItem -> Objects.nonNull(deliveryOrderItem.getScanAndGo()) && !deliveryOrderItem.getScanAndGo());
                order.setItemList(deliveryOrderItemList);
                generateOrderTotals(order);
            } else {
                generateOrderTotals(order);
                deliveryOrderItemList.removeIf(deliveryOrderItem -> Objects.nonNull(deliveryOrderItem.getScanAndGo()) && deliveryOrderItem.getScanAndGo());
                order.setItemList(deliveryOrderItemList);
            }
            countLimit++;
            previousOrders.add(order);
        }

        // revalidate real state .
        if (collectionResponseModel.getActiveOrders() != null) {
            collectionResponseModel.getActiveOrders().forEach(activeOrder -> {
                try {
                    if (activeOrder.getDeliveryType().equals(DeliveryType.EXPRESS) || activeOrder.getDeliveryType().equals(DeliveryType.SCANANDGO) || activeOrder.getDeliveryType().equals(DeliveryType.PROVIDER)) {

                        Optional<String> optLastStatusDS = getLastStatus(activeOrder,null);

                        if (optLastStatusDS.isPresent()) {
                            activeOrder.setLastStatus(optLastStatusDS.get());
                        }
                    }
                } catch (Exception e) {
                    LOG.info("La orden " + activeOrder.getIdOrder() + " no se puede traer " + activeOrder.toString());
                }
            });
        }

        List<DeliveryOrder> previousOrdersWithOrderProviderActiveList = new ArrayList<>();

        if (previousOrders != null) {
            previousOrders.forEach(previousOrder -> {

                Optional<Long> firstStatusOrderProviderActive = OrderUtil.getFirstStatusOrderProviderActive(previousOrder.getProviderList(), previousOrder.getIdOrder());

                if (isScanAndGoOrExpress(previousOrder)) {
                    Optional<String> optLastStatusDS = getLastStatus(previousOrder,null);

                    if (optLastStatusDS.isPresent()) {

                        this.validPreviousOrderStatusCancelAndOrderProviderAssociateActive(
                                previousOrder,
                                optLastStatusDS,
                                firstStatusOrderProviderActive,
                                previousOrdersWithOrderProviderActiveList
                        );

                    }
                }

                OrderUtil.deleteTipPriceZero(previousOrder.getItemList());

            });

            this.validPreviousOrdersWithOrderProviderActive(previousOrdersWithOrderProviderActiveList, previousOrders, collectionResponseModel);


        }

        Cursor cursorIter = iterator.getCursor();
        collectionResponseModel.setNextPageToken(cursorIter.toWebSafeString());
        collectionResponseModel.setPreviousOrders(previousOrders);
        return collectionResponseModel;
    }

    private static void validPreviousOrdersWithOrderProviderActive(
            List<DeliveryOrder> previousOrdersWithOrderProviderActiveList,
            List<DeliveryOrder> previousOrders,
            CollectionResponseModel collectionResponseModel
    ) {

        if (!previousOrdersWithOrderProviderActiveList.isEmpty() && !previousOrders.isEmpty() ) {

            Set<Long> idsOrderToDelete = previousOrdersWithOrderProviderActiveList.stream()
                    .map(DeliveryOrder::getIdOrder)
                    .collect(Collectors.toSet());

            previousOrders.removeIf(deliveryOrder -> idsOrderToDelete.contains(deliveryOrder.getIdOrder()));

            List<DeliveryOrder> activeOrders = collectionResponseModel.getActiveOrders();

            if (activeOrders != null) {

                activeOrders.addAll(previousOrdersWithOrderProviderActiveList);

                activeOrders.sort(Comparator.comparing(DeliveryOrder::getIdOrder).reversed());

                collectionResponseModel.setActiveOrders(activeOrders);

            }

        }
    }

    private void validPreviousOrderStatusCancelAndOrderProviderAssociateActive(
            DeliveryOrder previousOrder,
            Optional<String> optLastStatusDS,
            Optional<Long> firstStatusOrderProviderActive,
            List<DeliveryOrder> previousOrdersWithOrderProviderActiveList
    ) {

        if(optLastStatusDS.get().equals(ORDER_CANCELED) && firstStatusOrderProviderActive.isPresent() ){

            DeliveryOrder deliveryOrder = new DeliveryOrder();
            deliveryOrder.setIdOrder(previousOrder.getIdOrder());
            deliveryOrder.setPaymentType(previousOrder.getPaymentType());
            deliveryOrder.setDeliveryType(DeliveryType.PROVIDER);
            Integer statusId = firstStatusOrderProviderActive.get().intValue();
            Optional<String> lastStatusStirngOrderProvider = getLastStatus(deliveryOrder,statusId);
            previousOrder.setLastStatus(lastStatusStirngOrderProvider.get());
            previousOrdersWithOrderProviderActiveList.add(previousOrder);

        } else {
            previousOrder.setLastStatus(optLastStatusDS.get());
        }

    }


    private static boolean isScanAndGoOrExpress(DeliveryOrder previousOrder) {
        return Objects.nonNull(previousOrder) && Objects.nonNull(previousOrder.getDeliveryType()) &&
                Objects.equals(DeliveryType.EXPRESS, previousOrder.getDeliveryType()) || Objects.equals(DeliveryType.SCANANDGO, previousOrder.getDeliveryType()) || Objects.equals(DeliveryType.PROVIDER, previousOrder.getDeliveryType());
    }

    private void validateStatusScandAndGo(Answer answer, DeliveryOrder deliveryOrder) throws IOException {
        GenericResponse<Long> responseLastStatus = ApiGatewayService.get().validateOrderStatusOracle(deliveryOrder.getIdOrder());
        Long lastOrderStatus = responseLastStatus.getData();

        if (Objects.nonNull(lastOrderStatus) && lastOrderStatus == 5 && Boolean.TRUE.equals(isOrdenScanAndGo(deliveryOrder))) {
            changeStatusOrderDataStore(answer, deliveryOrder, ORDER_FINALIZED);
        }
    }

    /**
     * get last status.
     */
    private Optional<String> getLastStatus(DeliveryOrder deliveryOrder,Integer statusOrderProviderActive) {

        if (deliveryOrder == null || deliveryOrder.getIdOrder() <= 0) {
            return Optional.empty();
        }

        Long idOrder = deliveryOrder.getIdOrder();

        try {

            Optional<OrderInfoStatus> optTracing;

            if(statusOrderProviderActive != null){

                OrderInfoStatus orderInfoStatus = new OrderInfoStatus();

                orderInfoStatus.setStatusId(statusOrderProviderActive);

                optTracing = Optional.of(orderInfoStatus);

            }else{
                 optTracing = Optional.ofNullable(ApiGatewayService.get().getTrackingOrder(idOrder).getData());
            }


            if (!optTracing.isPresent()) {
                return Optional.empty();
            }

            OrderInfoStatus orderInfoStatus = optTracing.get();


            if (orderInfoStatus.getStatusId() == OrderStatusEnum.DEVOLUCION.getId() ||
                    orderInfoStatus.getStatusId() == OrderStatusEnum.DEVOLUCION_EXITOSA.getId()) {
                return Optional.of(ORDER_RETURNED);
            }

            if (orderInfoStatus.getStatusId() > OrderStatusEnum.CANCELADA.getId() && !deliveryOrder.getDeliveryType().equals(DeliveryType.SCANANDGO)  && !deliveryOrder.getDeliveryType().equals(DeliveryType.PROVIDER)) {
                return Optional.of(ORDER_ASSIGNED);
            }

            if (deliveryOrder.getPaymentType().getId() == PaymentTypeEnum.PSE.getId() &&
                    orderInfoStatus.getStatusId() == OrderStatusEnum.PAGADA.getId()) {
                return Optional.of(ORDER_CREATED);
            }

            switch (orderInfoStatus.getStatusId()) {
                case 3:
                case 4:
                case 37:
                    return Optional.of(ORDER_ASSIGNED);
                case 5:
                    if (deliveryOrder.getDeliveryType().equals(DeliveryType.SCANANDGO))
                        return Optional.of(ORDER_FINALIZED);
                    return Optional.of(ORDER_INCOMING);
                case 12:
                case 33:
                    return Optional.of(ORDER_INCOMING);
                case 6:
                case 7:
                    return Optional.of(ORDER_DELIVERED);
                case 14:
                    return Optional.of(ORDER_CANCELED);
                case 53:
                case 54:
                    return Optional.of(ORDER_RETURNED);
                case 48:
                    if (deliveryOrder.getDeliveryType().equals(DeliveryType.SCANANDGO))
                        return Optional.of(ORDER_FINALIZED);
                case 32:
                        return Optional.of(ORDER_ON_THE_WAY);
                default:
                    return Optional.of(ORDER_CREATED);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        return Optional.empty();
    }


    private Boolean statusOrder(Long idOrder) throws IOException, BadRequestException {
        try {
            DeliveryOrder deliveryOrder = ofy().load().type(DeliveryOrder.class).filter("idOrder", idOrder).first().now();
//            LOG.info("ordenes " + idOrder);
            Optional<OrderInfoStatus> optTracing = Optional.ofNullable(ApiGatewayService.get().getTrackingOrder(idOrder).getData());
            if (!optTracing.isPresent())
                throw new BadRequestException(HttpStatusCode.BAD_REQUEST.getStatusName());
            switch (optTracing.get().getStatusId()) {
                case 3:
                case 4:
                    deliveryOrder.setLastStatus(ORDER_ASSIGNED);
                    deliveryOrder.setActive(false);
                    ofy().save().entities(deliveryOrder);
                    break;
                case 5:
                case 12:
                    deliveryOrder.setLastStatus(ORDER_INCOMING);
                    deliveryOrder.setActive(true);
                    ofy().save().entities(deliveryOrder);
                    if (isOrdenScanAndGo(deliveryOrder)) {
                        deliveryOrder.setLastStatus(ORDER_DELIVERED);
                        deliveryOrder.setActive(false);
                        ofy().save().entities(deliveryOrder);
                    }
                    break;
                case 6:
                case 7:
                    deliveryOrder.setLastStatus(ORDER_DELIVERED);
                    deliveryOrder.setActive(false);
                    ofy().save().entities(deliveryOrder);
                    break;
                case 14:
                    deliveryOrder.setLastStatus(ORDER_CANCELED);
                    deliveryOrder.setActive(false);
                    ofy().save().entities(deliveryOrder);
                    break;
                default:
                    deliveryOrder.setLastStatus(ORDER_CREATED);
                    deliveryOrder.setActive(true);
                    ofy().save().entities(deliveryOrder);
            }
            return true;

        } catch (EOFException e) {
            LOG.warning("warn error to update  order :#" + idOrder);
        }
        return false;

    }

    private Optional<String> validateOrderStatusCanceled(DeliveryOrder deliveryOrder) throws IOException {

        Optional<OrderInfoStatus> optTracing = Optional.ofNullable(ApiGatewayService.get().getTrackingOrder(deliveryOrder.getIdOrder()).getData());
        if (optTracing.isPresent() && optTracing.get().getStatusId() == 14){
            deliveryOrder.setLastStatus(ORDER_CANCELED);
            deliveryOrder.setActive(false);
            ofy().save().entities(deliveryOrder);
            return Optional.of(ORDER_CANCELED);
        }
        return Optional.empty();
    }

    private void validateItemsFromProviderInOrder(DeliveryOrder deliveryOrder, List<DeliveryOrderProvider> deliveryOrderProviderList, List<DeliveryOrderItem> deliveryOrderItemList) {
        if (Objects.nonNull(deliveryOrderProviderList) && !deliveryOrderProviderList.isEmpty()) {
            // Sumatoria de valor de valor de entrega de proveedores
            deliveryOrder.setProviderDeliveryPrice(deliveryOrderProviderList.parallelStream()
                    .mapToDouble(DeliveryOrderProvider::getDeliveryPrice)
                    .sum());
            deliveryOrderItemList.removeIf(item -> deliveryOrderProviderList.stream()
                    .filter(provider ->
                            provider.getItemList().stream()
                                    .filter(itemProvider -> itemProvider.getId() == item.getId()).findAny().isPresent())
                    .findAny().isPresent());

            // Coloa el estado de la orden en el item
            deliveryOrderProviderList.stream().filter(providerOrder -> Objects.nonNull(providerOrder.getDeliveryStatus()) && providerOrder.getDeliveryStatus() > 0)
                    .forEach(providerOrder -> {
                        providerOrder.getItemList().stream().forEach(itemProvider -> {
                            itemProvider.setDeliveryStatus(providerOrder.getDeliveryStatus());
                        });
                    });

        }
    }

    @ApiMethod(name = "validateOrders", path = "/orderEndpoint/validateOrders", httpMethod = ApiMethod.HttpMethod.GET)
    public Answer validateOrders(
            @Named("token") final String token,
            @Named("tokenIdWebSafe") final String tokenIdWebSafe,
            @Named("idCustomerWebSafe") final String idCustomerWebSafe)
            throws ConflictException, IOException, BadRequestException, InternalServerErrorException {

        if (!authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);

        if (Objects.isNull(idCustomerWebSafe)) {
            throw new ConflictException(Constants.INVALID_TOKEN_ID_WEBSAFE);
        }

        Key<Customer> customerKey = Key.create(idCustomerWebSafe);
//        LOG.info("customerKey Datastore -> " + customerKey);
        Answer answer = new Answer();
        answer.setConfirmation(false);
        List<DeliveryOrder> deliveryOrderList = ofy().load().type(DeliveryOrder.class).ancestor(customerKey).list();

        if (deliveryOrderList != null && !deliveryOrderList.isEmpty()) {
            for (DeliveryOrder deliveryOrder : deliveryOrderList) {

                if (deliveryOrder.getLastStatus() != null && (deliveryOrder.getLastStatus().equals(ORDER_INCOMING) || deliveryOrder.getLastStatus().equals(ORDER_CREATED) || deliveryOrder.getLastStatus().equals(ORDER_ASSIGNED))) {
                    answer.setConfirmation(true);
//                    LOG.info("DeliveryOrder -> " + deliveryOrder.getIdOrder());
//
//                    LOG.info("lastStatus DataStore -> " + deliveryOrder.getLastStatus());
                    GenericResponse<Long> responseLastStatus = ApiGatewayService.get().validateOrderStatusOracle(deliveryOrder.getIdOrder());
                    Long lastOrderStatus = responseLastStatus.getData();

                    if (Objects.nonNull(lastOrderStatus)) {
//                        LOG.info("lastStatus oracle -> " + lastOrderStatus);
                        if (lastOrderStatus == 6 || lastOrderStatus == 7) {
                            changeStatusOrderDataStore(answer, deliveryOrder, ORDER_FINALIZED);
                        } else if (lastOrderStatus == 14) {
                            changeStatusOrderDataStore(answer, deliveryOrder, ORDER_CANCELED);
                        } else if (lastOrderStatus == 5 && Boolean.TRUE.equals(isOrdenScanAndGo(deliveryOrder))) {
                            changeStatusOrderDataStore(answer, deliveryOrder, ORDER_FINALIZED);
                        }
                    }
                }
            }

        } else {
            answer.setConfirmation(false);
        }
        return answer;
    }

    private void changeStatusOrderDataStore(Answer answer, DeliveryOrder deliveryOrder, String status) {
        deliveryOrder.setLastStatus(status);
        deliveryOrder.setActive(false);
        ofy().save().entities(deliveryOrder);
        answer.setConfirmation(false);
    }

    @ApiMethod(name = "getPaymentMethods", path = "/orderEndpoint/getPaymentMethods", httpMethod = ApiMethod.HttpMethod.POST)
    public CollectionResponse<PaymentType> getPaymentMethods(final HttpServletRequest req,
                                                             final Customer customer
    ) throws ConflictException, BadRequestException, IOException, InternalServerErrorException, AlgoliaException {
        LOG.info("Method: getPaymentMethods customer: " + customer);
        if (!authenticate.isValidToken(customer.getToken().getToken(), customer.getToken().getTokenIdWebSafe()))
            throw new ConflictException(Constants.INVALID_TOKEN);
        final String source = Objects.nonNull(req.getHeader("source")) ? req.getHeader("source") : "DEFAULT";
        String versionApp = Objects.nonNull(req.getHeader("version")) ? req.getHeader("version") : "1.1.1";
        if (source.equals("WEB") || source.equals("RESPONSIVE") || source.equals("DEFAULT")) {
            versionApp = "1.1.1";
        }
//        LOG.info("source -> " + source + " , version -> " + versionApp);
        return getGeneralPaymentMethod(customer, null, source, versionApp);
    }

    @ApiMethod(name = "paymentMethods", path = "/orderEndpoint/v2/paymentMethods", httpMethod = ApiMethod.HttpMethod.POST)
    public PaymentMethodV2FTDResponse paymentMethods(
            final HttpServletRequest req, final Customer customer
    ) throws ConflictException, BadRequestException {

        LOG.info("Method: getPaymentMethods customer: " + customer);
        if (!authenticate.isValidToken(customer.getToken().getToken(), customer.getToken().getTokenIdWebSafe()))
            throw new ConflictException(Constants.INVALID_TOKEN);

        Key<User> customerKey = Key.create(customer.getIdCustomerWebSafe());
        DeliveryOrder deliveryOrder = ofy().load().type(DeliveryOrder.class).filter("currentStatus", 1).ancestor(Ref.create(customerKey)).first().now();

        RequestSourceEnum sourceEnum = ftdUtilities.getSourceFromRequestHeaderForPays(req);

        PaymentMethodV2DTFRequest request = new PaymentMethodV2DTFRequest();

        request.setOs(sourceEnum.name());

        if (req.getHeader("version") != null) {
            request.setVersion(req.getHeader("version"));
        }

        RequestSourceEnum source = null;
        try {
            source = ftdUtilities.getSourceFromRequestHeader(req);
        } catch (Exception e) {
            LOG.warning("Ocurrio un error verificando el source");
        }

        request.setCustomerId(customer.getId());
        request.setDeliveryType(customer.getDeliveryType());

        boolean containsItemsProvider = containsItemsForProvider(customer);

        if (containsItemsProvider && !DeliveryType.SCANANDGO.name().equalsIgnoreCase(customer.getDeliveryType())) {
            request.setDeliveryType(DeliveryType.PROVIDER.name());
        }

        LOG.info("Request payment methods -> " + request.toString());

        Optional<PaymentMethodV2FTDResponse> optionalResponse = ApiGatewayService.get().paymentMethodV2FTD(request);

        if (!optionalResponse.isPresent()) {
            throw new ConflictException(Constants.NOT_CONTENT);
        }

        PrimeConfig primeConfigV2 = APIAlgolia.primeConfigV2();
        boolean isItemPrime = isOrderPrimeV2(deliveryOrder);
        if (isItemPrime && primeConfigV2 != null) {
            boolean isScanAndGo = DeliveryType.SCANANDGO.name().equalsIgnoreCase(customer.getDeliveryType());
            deletePaymentPrime(optionalResponse, primeConfigV2, deliveryOrder, request.getOs(), isScanAndGo);
        }

        PaymentMethodV2FTDResponse response = optionalResponse.get();

        response.getData().sort(Comparator.comparing(PaymentMethodsV2Data::getPositionIndex));

        setCreditCardByDefault(response, customer.getId(),null, sourceEnum.name());

        setDefaultPaymentMethod(response);

        cleanResponseForFrontend(response);


        return response;

    }

    private void deletePaymentPrime(
            Optional<PaymentMethodV2FTDResponse> optionalResponse,
            PrimeConfig primeConfigV2,
            DeliveryOrder order,
            String source,
            boolean isScanAndGo
    ) {
        if (!isScanAndGo) {
            if (optionalResponse.isPresent() && !optionalResponse.get().getData().isEmpty() && Objects.nonNull(source)) {
                if (Objects.nonNull(order)) {
                    List<DeliveryOrderItem> items = null;

                    try {
                        items = ofy().load().type(DeliveryOrderItem.class).ancestor(order).list();
                    } catch (Exception e) {
                        LOG.info("Al parecer el cliente no tiene items agregados al carrito");
                    }

                    if (Objects.nonNull(items) && !items.isEmpty()) {
                        Optional<PrimePlan> primePlan = primeUtil.getPrimePlan(items);
                        if (primePlan.isPresent()) {
                            Optional<PaymentMethodsV2> paymentMethodsV2Optional = primePlan.get().getPayment_methods_v2().stream().filter(
                                    paymentMethodsV2 -> source.toLowerCase().equals(paymentMethodsV2.getSource())
                            ).findFirst();

                            if (paymentMethodsV2Optional.isPresent()) {
                                List<String> enablePayments = paymentMethodsV2Optional.get().getEnable_payments();
                                optionalResponse.get().getData().removeIf(paymentMethod -> !enablePayments.contains(paymentMethod.getDescription()));
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * return if contains provider item in cart
     *
     * @param customer
     * @return
     */
    private boolean containsItemsForProvider(Customer customer) {

        if (customer == null || customer.getIdCustomerWebSafe() == null) {
            return false;
        }
        Key<Customer> customerKey = Key.create(customer.getIdCustomerWebSafe());
        DeliveryOrder deliveryOrder = ofy().load().type(DeliveryOrder.class)
                .filter("currentStatus", 1)
                .ancestor(Ref.create(customerKey))
                .first()
                .now();

        if (deliveryOrder == null) {
            return false;
        }

        // Providers
        List<DeliveryOrderProvider> deliveryOrdersProvider = ofy()
                .load()
                .type(DeliveryOrderProvider.class).
                ancestor(deliveryOrder)
                .list();

        List<DeliveryOrderItem> deliveryItems = ofy()
                .load()
                .type(DeliveryOrderItem.class).
                ancestor(deliveryOrder)
                .list();

        List<Long> itemsScanAndGo = new ArrayList<>();
        if (deliveryItems != null) {
            for (DeliveryOrderItem item : deliveryItems) {
                if (item.getScanAndGo() != null && item.getScanAndGo()) {
                    itemsScanAndGo.add(item.getId());
                }
            }
        }

        if (!DeliveryType.SCANANDGO.equals(customer.getDeliveryType()) && deliveryOrdersProvider != null) {

            deliveryOrdersProvider.removeIf(item -> Objects.nonNull(item.getItemList())
                    && Objects.nonNull(item.getItemList().get(0))
                    && itemsScanAndGo.contains(item.getItemList().get(0).getId()));
        }

        if (deliveryOrdersProvider == null || deliveryOrdersProvider.isEmpty()) {
            return false;
        }

        return true;
    }

    private void setCreditCardByDefault(PaymentMethodV2FTDResponse response, int customerId,String city, String source) {


        response.getData().forEach(paymentMethodAux -> {

            Long paymentMethodId = Long.parseLong(paymentMethodAux.getId());

            if (Objects.equals(PaymentTypeEnum.TRANSACCIONES_EN_LINEA.getId(), paymentMethodId)) {
                List<CreditCard> creditCards = getCustomerCreditCardsCRM(customerId,city, source);

                if (creditCards != null) {
                    Optional<CreditCard> optionalCreditCard = creditCards
                            .stream()
                            .filter(CreditCard::getDefaultCard).findFirst();

                    // set credit card by default
                    optionalCreditCard.ifPresent(paymentMethodAux::setCreditCard);

                }
            }

        });
    }

    private void cleanResponseForFrontend(PaymentMethodV2FTDResponse response) {
        response.getData().forEach(paymentAux -> {

            final PaymentTypeEnum paymentTypeEnum = PaymentTypeEnum.find(Long.parseLong(paymentAux.getId()));

            if (paymentTypeEnum != null) {
                paymentAux.setPaymentMethod(paymentTypeEnum.name());
            }
            paymentAux.setStatus(null);
            paymentAux.setDefaultPaymentMethod(null);
        });

    }

    private void setDefaultPaymentMethod(PaymentMethodV2FTDResponse response) {
        try {
            response.getData().forEach(paymentAux -> {

                if (paymentAux.getDefaultPaymentMethod() != null && paymentAux.getDefaultPaymentMethod()) {
                    response.setDefaultPaymentMethod(paymentAux);
                }

            });
            // si no existe ningun metodo por defecto se setea segun el orden.
            if (response.getDefaultPaymentMethod() == null) {

                final Optional<PaymentMethodsV2Data> optFirstPaymentMethod = response.getData().stream().findFirst();

                optFirstPaymentMethod.ifPresent(paymentDataAux -> {

                    if (paymentDataAux.getPaymentMethod() != null && !paymentDataAux.getPaymentMethod().isEmpty()) {
                        response.setDefaultPaymentMethod(paymentDataAux);
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
            LOG.warning("ERROR: No fue posible seleccionar metodo de pago por defecto");
        }

    }

    @ApiMethod(name = "getPaymentMethod", path = "/orderEndpoint/getPaymentMethod", httpMethod = ApiMethod.HttpMethod.POST)
    public CollectionResponse<PaymentType> getPaymentMethodV2Provisionally(final HttpServletRequest req, final Customer customer) throws ConflictException, BadRequestException {
        LOG.info("Method: getPaymentMethods customer: " + customer);
        if (!authenticate.isValidToken(customer.getToken().getToken(), customer.getToken().getTokenIdWebSafe()))
            throw new ConflictException(Constants.INVALID_TOKEN);

        final String source = Objects.nonNull(req.getHeader("source")) ? req.getHeader("source") : "DEFAULT";
        String versionApp = Objects.nonNull(req.getHeader("version")) ? req.getHeader("version") : "1.1.1";
        if (source.equals("WEB") || source.equals("RESPONSIVE") || source.equals("DEFAULT")) {
            versionApp = "1.1.1";
        }
//        LOG.info("source -> " + source + " , version -> " + versionApp);


        PaymentMethodV2DTFRequest request = new PaymentMethodV2DTFRequest();
        request.setOs(source);
        request.setVersion(versionApp);
        request.setCustomerId(customer.getId());
        request.setDeliveryType(customer.getDeliveryType());

        boolean containsItemsProvider = containsItemsForProvider(customer);

        if (containsItemsProvider) {
            request.setDeliveryType(DeliveryType.PROVIDER.name());
        }

        Optional<PaymentMethodV2FTDResponse> optionalResponse = ApiGatewayService.get().paymentMethodV2FTD(request);

        if (!optionalResponse.isPresent()) {
            throw new ConflictException(Constants.NOT_CONTENT);
        }

        List<PaymentType> paymentTypeList = new ArrayList<>();

        optionalResponse.get().getData().forEach(paymentData -> {

            PaymentType paymentType = new PaymentType();

            paymentType.setId(Long.parseLong(paymentData.getId()));
            paymentType.setDescription(paymentData.getMediaDescription());
            paymentType.setPositionIndex(paymentData.getPositionIndex());
            paymentType.setStatus(paymentData.getStatus() != null ? paymentData.getStatus() : false);

            if (paymentType.getId() == PaymentTypeEnum.PSE.getId()) {
                paymentType.setPse(paymentData.getDataForPSE());
            }

            if (paymentType.getId() == PaymentTypeEnum.TRANSACCIONES_EN_LINEA.getId()) {
                List<CreditCard> creditCards = getCustomerCreditCardsCRM(customer.getId(),null, null);

                if (creditCards != null) {
                    Optional<CreditCard> optionalCreditCard = creditCards
                            .stream()
                            .filter(CreditCard::getDefaultCard).findFirst();

                    // set credit card by default
                    optionalCreditCard.ifPresent(paymentType::setCreditCard);

                }

            }

            paymentTypeList.add(paymentType);

        });

        paymentTypeList.sort(Comparator.comparing(PaymentType::getPositionIndex));

        return CollectionResponse.<PaymentType>builder().setItems(paymentTypeList).build();

    }

    ;


    @Deprecated
    @ApiMethod(name = "getPaymentMethod", path = "/orderEndpoint/getPaymentMethod/deprecated", httpMethod = ApiMethod.HttpMethod.POST)
    public CollectionResponse<PaymentType> getPaymentMethod(final HttpServletRequest req,
                                                            final Customer customer
    ) throws ConflictException, BadRequestException, IOException, InternalServerErrorException, AlgoliaException {
        //LOG.warning("Method: getPaymentMethods customer: " + customer);
        if (!authenticate.isValidToken(customer.getToken().getToken(), customer.getToken().getTokenIdWebSafe()))
            throw new ConflictException(Constants.INVALID_TOKEN);

        final String source = Objects.nonNull(req.getHeader("source")) ? req.getHeader("source") : "DEFAULT";
        String versionApp = Objects.nonNull(req.getHeader("version")) ? req.getHeader("version") : "1.1.1";
        if (source.equals("WEB") || source.equals("RESPONSIVE") || source.equals("DEFAULT")) {
            versionApp = "1.1.1";
        }
//        LOG.info("source -> " + source + " , version -> " + versionApp);

        Key<Customer> customerKey = Key.create(customer.getIdCustomerWebSafe());
        DeliveryOrder deliveryOrder = ofy().load().type(DeliveryOrder.class).filter("currentStatus", 1).ancestor(Ref.create(customerKey)).first().now();
        //LOG.warning("deliveryOrder ->  " + (Objects.nonNull(deliveryOrder) ? deliveryOrder : "Sin ordenes"));

        // Providers
        List<DeliveryOrderProvider> deliveryOrdersProvider = Objects.nonNull(deliveryOrder) ? ofy().load().type(DeliveryOrderProvider.class).ancestor(deliveryOrder).list() : null;
        //LOG.warning("deliveryOrderProvidersList ->  " + (Objects.nonNull(deliveryOrdersProvider) ? deliveryOrdersProvider.size() : 0));

        return getGeneralPaymentMethod(customer, deliveryOrdersProvider, source, versionApp);
    }

    private CollectionResponse<PaymentType> getGeneralPaymentMethod(final Customer customer, List<DeliveryOrderProvider> deliveryOrdersProvider, String source, final String versionApp) throws ConflictException, BadRequestException, IOException, InternalServerErrorException, AlgoliaException {
        User user = users.findUserByIdCustomer(customer.getId());
        if (user == null)
            throw new BadRequestException(Constants.CUSTOMER_NOT_FOUND);
        PaymentMethodsProperties properties = APIAlgolia.getPaymentMethods();
//        LOG.info("PROPERTIE PAYMENT  METHOD: EFECTIVO: " + properties.getEfectivo() + ", DATAFONO: " + properties.getDatafono() + ", ENLINEA: " + properties.getEnLinea() + ", PSE: " + properties.getPse());
        List<PaymentType> paymentTypeList = ofy().load().type(PaymentType.class).filter("status", true).order("positionIndex").list();
        PaymentType dumy = new PaymentType();
        dumy.setId(6L);
        dumy.setStatus(true);
        dumy.setDescription("PSE");
        dumy.setPositionIndex(4);
        paymentTypeList.add(dumy);
        List<PaymentType> paymentTypeListNew = paymentTypeList;

        Optional<CustomerJSON> optionalCustomerJSON = customers.customerInformation(user, customer.getIdStoreGroup(), false);

        if (source == null || source.isEmpty()) {
            source = "Source default";
        }

        if (!optionalCustomerJSON.isPresent()) {
            throw new ConflictException(Constants.CUSTOMER_NOT_FOUND);
        }
        CustomerJSON customerJSON = optionalCustomerJSON.get();
        List<PaymentType> paymentTypesRemove = new ArrayList<>();
        PaymentType paymentTypeRemove = null;

        PaymentMethodsAlgolia paymentMethodsAlgolia = APIAlgolia.getPaymentMethodsByDeliveryType();
        List<PaymentMethod> listPayments = getPaymentMethodsBySource(paymentMethodsAlgolia, source);
        List<String> versions = new ArrayList<>();

        //Traer las versiones configuradas en algolia segun el source
        if (source != null || source.isEmpty()) {
            if (source.equals("ANDROID")) {
                versions = paymentMethodsAlgolia.getVersions().getAndroidNationalVersion();
            } else if (source.equals("IOS") || source.equals("Source default")) {
                versions = paymentMethodsAlgolia.getVersions().getIosNationalVersion();
            } else if (source.equals("WEB") || source.equals("RESPONSIVE") || source.equals("DEFAULT")) {
                List<String> versionsWeb = new ArrayList<>();
                versionsWeb.add("1.1.1");
                versions = versionsWeb;
            }
        }

        if (Objects.nonNull(listPayments) && !listPayments.isEmpty()) {
            List<String> finalVersions = versions;
            listPayments.stream()
                    .filter(paymentMethod -> paymentMethod.getDeliveryType().equals(customer.getDeliveryType()))
                    .forEach(paymentMethod -> {
                        if (paymentMethod.isCreditCard()) {
                            getPaymentCreditCard(paymentTypeList, customerJSON);
                        }
                        if (!finalVersions.contains(versionApp) || paymentMethod.isPSE()) {
                            getPaymentPSE(paymentTypeList);
                        }
                    });
        }


        List<PaymentType> paymentTypeListAux = new ArrayList<>(paymentTypeListNew);

        // DeliveryType EnvialoYa
        if (Objects.nonNull(paymentTypeListNew) && customer.getDeliveryType().equals("ENVIALOYA")) {

            if (Objects.nonNull(listPayments) && !listPayments.isEmpty()) {
                List<String> finalVersions = versions;
//                versions.forEach(data -> {
//                    LOG.info("versión -> " + data);
//                    LOG.info("versión constain? -> " + finalVersions.contains(versionApp));
//                });
                listPayments.stream()
                        .filter(paymentMethod -> paymentMethod.getDeliveryType().equals("ENVIALOYA"))
                        .forEach(paymentMethod -> {
                            if (!paymentMethod.isCash()) {
                                //LOG.warning("Elimina Efectivo " + paymentMethod.isCash());
                                paymentTypeListAux.removeIf(p2 -> p2.getId() == Constants.ID_EFECTIVO);
                            }
                            if (!paymentMethod.isDataphone()) {
                                //LOG.warning("Elimina Datafono " + paymentMethod.isDataphone());
                                paymentTypeListAux.removeIf(p2 -> p2.getId() == Constants.ID_DATAFONO);
                            }
                            if (!paymentMethod.isCreditCard()) {
                                //LOG.warning("Elimina Tarjeta " + paymentMethod.isCreditCard());
                                paymentTypeListAux.removeIf(p2 -> p2.getId() == Constants.ID_CARD);
                            }
                            if (!finalVersions.contains(versionApp) || !paymentMethod.isPSE()) {
                                //LOG.warning("Elimina PSE " + paymentMethod.isPSE());
                                paymentTypeListAux.removeIf(p2 -> p2.getId() == Constants.ID_PSE);
                            }
                        });
            }

            if (!paymentTypeListAux.isEmpty()) {
                paymentTypeListAux.stream().forEach(paymentType -> LOG.warning(paymentType.getDescription()));
            }
            return CollectionResponse.<PaymentType>builder().setItems(paymentTypeListAux).build();
        }

        // DeliveryType Subscrition
        if (Objects.nonNull(paymentTypeListNew) && customer.getDeliveryType().equals("SUBSCRIPTION")) {

            if (Objects.nonNull(listPayments) && !listPayments.isEmpty()) {
                List<String> finalVersions1 = versions;
//                versions.forEach(data -> {
//                    LOG.info("versión -> " + data);
//                    LOG.info("versión constain? -> " + finalVersions1.contains(versionApp));
//                });
                listPayments.stream()
                        .filter(paymentMethod -> paymentMethod.getDeliveryType().equals("SUBSCRIPTION"))
                        .forEach(paymentMethod -> {
                            if (!paymentMethod.isCash()) {
                                //LOG.warning("Elimina Efectivo " + paymentMethod.isCash());
                                paymentTypeListAux.removeIf(p2 -> p2.getId() == Constants.ID_EFECTIVO);
                            }
                            if (!paymentMethod.isDataphone()) {
                                //LOG.warning("Elimina Datafono " + paymentMethod.isDataphone());
                                paymentTypeListAux.removeIf(p2 -> p2.getId() == Constants.ID_DATAFONO);
                            }
                            if (!paymentMethod.isCreditCard()) {
                                //LOG.warning("Elimina Tarjeta " + paymentMethod.isCreditCard());
                                paymentTypeListAux.removeIf(p2 -> p2.getId() == Constants.ID_CARD);
                            }
                            if (!finalVersions1.contains(versionApp) || !paymentMethod.isPSE()) {
                                //LOG.warning("Elimina PSE " + paymentMethod.isPSE());
                                paymentTypeListAux.removeIf(p2 -> p2.getId() == Constants.ID_PSE);
                            }
                        });
            }

            if (!paymentTypeListAux.isEmpty()) {
                paymentTypeListAux.stream().forEach(paymentType -> LOG.warning(paymentType.getDescription()));
            }
            return CollectionResponse.<PaymentType>builder().setItems(paymentTypeListAux).build();
        }


        // DeliveryType Provider
        if (Objects.nonNull(deliveryOrdersProvider) && !deliveryOrdersProvider.isEmpty()) {
            //LOG.warning("Order a Proveedores externos");
            if (Objects.nonNull(listPayments) && !listPayments.isEmpty() &&
                    Objects.nonNull(paymentTypeListAux) && !paymentTypeListAux.isEmpty()) {
                List<String> finalVersions2 = versions;
//                versions.forEach(data -> {
//                    LOG.info("versión -> " + data);
//                    LOG.info("versión constain? -> " + finalVersions2.contains(versionApp));
//                });
                listPayments.stream()
                        .filter(paymentMethod -> paymentMethod.getDeliveryType().equals("PROVIDER"))
                        .forEach(paymentMethod -> {
                            if (!paymentMethod.isCash()) {
                                //LOG.warning("Elimina Efectivo " + paymentMethod.isCash());
                                paymentTypeListAux.removeIf(p2 -> p2.getId() == Constants.ID_EFECTIVO);
                            }
                            if (!paymentMethod.isDataphone()) {
                                //LOG.warning("Elimina Datafono " + paymentMethod.isDataphone());
                                paymentTypeListAux.removeIf(p2 -> p2.getId() == Constants.ID_DATAFONO);
                            }
                            if (!paymentMethod.isCreditCard()) {
                                //LOG.warning("Elimina Tarjeta " + paymentMethod.isCreditCard());
                                paymentTypeListAux.removeIf(p2 -> p2.getId() == Constants.ID_CARD);
                            }
                            if (!finalVersions2.contains(versionApp) || !paymentMethod.isPSE()) {
                                //LOG.warning("Elimina PSE " + paymentMethod.isPSE());
                                paymentTypeListAux.removeIf(p2 -> p2.getId() == Constants.ID_PSE);
                            }
                        });
            }
            if (!paymentTypeListAux.isEmpty()) {
                paymentTypeListAux.stream().forEach(paymentType -> LOG.warning(paymentType.getDescription()));
            }
            return CollectionResponse.<PaymentType>builder().setItems(paymentTypeListAux).build();
        }

        if (Objects.nonNull(paymentTypeListNew)) {

            if (Objects.nonNull(listPayments) && !listPayments.isEmpty()) {
                List<String> finalVersions3 = versions;
//                versions.forEach(data -> {
//                    LOG.info("versión -> " + data);
//                    LOG.info("versión constain? -> " + finalVersions3.contains(versionApp));
//                });
                listPayments.stream()
                        .filter(paymentMethod -> paymentMethod.getDeliveryType().equals(customer.getDeliveryType()))
                        .forEach(paymentMethod -> {
                            if (!paymentMethod.isCash()) {
                                //LOG.warning("Elimina Efectivo " + paymentMethod.isCash());
                                paymentTypeListAux.removeIf(p2 -> p2.getId() == Constants.ID_EFECTIVO);
                            }
                            if (!paymentMethod.isDataphone()) {
                                //LOG.warning("Elimina Datafono " + paymentMethod.isDataphone());
                                paymentTypeListAux.removeIf(p2 -> p2.getId() == Constants.ID_DATAFONO);
                            }
                            if (!paymentMethod.isCreditCard()) {
                                //LOG.warning("Elimina Tarjeta " + paymentMethod.isCreditCard());
                                paymentTypeListAux.removeIf(p2 -> p2.getId() == Constants.ID_CARD);
                            }
                            if (!finalVersions3.contains(versionApp) || !paymentMethod.isPSE()) {
                                //LOG.warning("Elimina PSE " + paymentMethod.isPSE());
                                paymentTypeListAux.removeIf(p2 -> p2.getId() == Constants.ID_PSE);
                            }
                        });
            }

            if (!paymentTypeListAux.isEmpty()) {
                paymentTypeListAux.stream().forEach(paymentType -> LOG.warning(paymentType.getDescription()));
            }
            return CollectionResponse.<PaymentType>builder().setItems(paymentTypeListAux).build();
        }

        return CollectionResponse.<PaymentType>builder().setItems(paymentTypeList).build();
    }

    private void getPaymentCreditCard(List<PaymentType> paymentTypeList, CustomerJSON customerJSON) {
        for (PaymentType paymentType : paymentTypeList) {
            if (paymentType.getId() == Constants.ID_CARD) {
                //List<CreditCard> creditCards = customerJSON.getCreditCards();
                List<CreditCard> creditCards = getCustomerCreditCardsCRM(customerJSON.getId(),null, null);
//                LOG.info("creditCards  ->> " + creditCards);
                if (creditCards != null && !creditCards.isEmpty()) {

                    for (CreditCard creditCard : creditCards) {
                        if (creditCard.getDefaultCard()) {
                            paymentType.setCreditCard(creditCard);
                        }
                    }
                }
            }
        }
    }

    private void getPaymentPSE(List<PaymentType> paymentTypeList) {
        for (PaymentType paymentType : paymentTypeList) {
//            LOG.info("Id -> " + paymentType.getId());
            if (paymentType.getId() == Constants.ID_PSE) {
                PSEResponse pseResponseList = getAllPSE();
//                LOG.info("pseResponseList is != null? " + (pseResponseList != null));
                if (pseResponseList != null && !pseResponseList.getFinancialInstitutions().isEmpty()) {
                    paymentType.setPse(pseResponseList);
                } else {
                    paymentTypeList.removeIf(p -> p.getId() == Constants.ID_PSE);
                }
            }
        }
    }

    private List<PaymentMethod> getPaymentMethodsBySource(PaymentMethodsAlgolia paymentMethodsAlgolia, String source) {
        List<PaymentMethod> response = new ArrayList();

        if (Objects.nonNull(paymentMethodsAlgolia)) {
            switch (source) {
                case "WEB":
//                    LOG.info("Source WEB");
                    response = paymentMethodsAlgolia.getWebPayments().getPaymentMethods();
                    return response;
                case "RESPONSIVE":
//                    LOG.info("Source RESPONSIVE");
                    response = paymentMethodsAlgolia.getResponsivePayments().getPaymentMethods();
                    return response;
                case "ANDROID":
//                    LOG.info("Source ANDROID");
                    response = paymentMethodsAlgolia.getAndroidPayments().getPaymentMethods();
                    return response;
                case "IOS":
//                    LOG.info("Source IOS");
                    response = paymentMethodsAlgolia.getIosPayments().getPaymentMethods();
                    return response;
                default:
//                    LOG.info("Source default");
                    response = paymentMethodsAlgolia.getDefaultPayment().getPaymentMethods();
                    return response;
            }
        }
        LOG.info("Source default");
        response = paymentMethodsAlgolia.getDefaultPayment().getPaymentMethods();
        return response;
    }

    private List<CreditCard> getCustomerCreditCardsCRM(int userID,String city, String source) {
        try {
            CustomerCreditCard customerCreditCard =
                    new CustomerCreditCard(ApiGatewayService.get().getAllCreditCard(userID,city, source));
//            LOG.info("customerCreditCard --> " + customerCreditCard);
            return customerCreditCard.getCreditCardList();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private PSEResponse getAllPSE() {
        try {
            PSEResponseData pseResponseCode = new PSEResponseData(ApiGatewayService.get().getAllPSE());
            if (Objects.nonNull(pseResponseCode.getPseResponses().body().getData().getFinancialInstitutions()) && !pseResponseCode.getPseResponses().body().getData().getFinancialInstitutions().isEmpty()) {
                return pseResponseCode.getPseResponses().body().getData();
            } else {
                return new PSEResponse();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new PSEResponse();
    }


    @ApiMethod(name = "getAllPSE", path = "/orderEndpoint/getAllPSE", httpMethod = ApiMethod.HttpMethod.GET)
    public PSEResponse getAllPSEV2() throws ConflictException {
        try {
            PSEResponseData pseResponseCode = new PSEResponseData(ApiGatewayService.get().getAllPSE());
            return pseResponseCode.getPseResponses().body().getData();
        } catch (SocketTimeoutException e) {
            throw new ConflictException("SocketTimeoutException@getAllPSE -> " + e.getMessage());
        } catch (Exception e) {
            throw new ConflictException("Error@getAllPSE -> " + e.getMessage());
        }
    }

    private void removePaymentsType(List<PaymentType> paymentTypesRemove, PaymentMethod paymentMethod, List<PaymentType> paymentTypeListAux) {
        if (!paymentMethod.isCash()) {
            //LOG.warning("Elimina Efectivo " + paymentMethod.isCash());
            paymentTypesRemove.add(paymentTypeListAux.stream().filter(paymentBase -> paymentBase.getId() == Constants.ID_EFECTIVO).findFirst().get());
        }
        if (!paymentMethod.isDataphone()) {
            //LOG.warning("Elimina Datafono " + paymentMethod.isDataphone());
            paymentTypesRemove.add(paymentTypeListAux.stream().filter(paymentBase -> paymentBase.getId() == Constants.ID_DATAFONO).findFirst().get());
        }
        if (!paymentMethod.isCreditCard()) {
            //LOG.warning("Elimina Tarjeta " + paymentMethod.isCreditCard());
            paymentTypesRemove.add(paymentTypeListAux.stream().filter(paymentBase -> paymentBase.getId() == Constants.ID_CARD).findFirst().get());
        }
        if (!paymentMethod.isCreditCard()) {
            //LOG.warning("Elimina PSE " + paymentMethod.isPSE());
            paymentTypesRemove.add(paymentTypeListAux.stream().filter(paymentBase -> paymentBase.getId() == Constants.ID_PSE).findFirst().get());
        }
    }


    @ApiMethod(name = "validatePaymentMethod", path = "/orderEndpoint/validatePaymentMethod", httpMethod = ApiMethod.HttpMethod.GET)
    public Answer validatePaymentMethod(@Named("idCustomerWebSafe") final String idCustomerWebSafe,
                                        @Named("token") final String token,
                                        @Named("tokenIdWebSafe") final String tokenIdWebSafe)
            throws ConflictException, BadRequestException {

        if (!authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);

        Key<User> userKey = Key.create(idCustomerWebSafe);
        // TODO(jorge.garcia) Comentar log para produccion
        //LOG.warning("validatePaymentMethod -> userKey: [" + userKey + "]");

        User user = ofy().load().key(userKey).now();
        // TODO(jorge.garcia) Comentar log para produccion
        //LOG.warning("validatePaymentMethod -> user.id: [" + user.getId() + "]");

        DeliveryOrder deliveryOrder = ofy().load().type(DeliveryOrder.class).filter("currentStatus", 1).ancestor(Ref.create(userKey)).first().now();
        if (deliveryOrder == null)
            throw new ConflictException(Constants.DELIVERY_ORDER_NOT_FOUND);
        // TODO(jorge.garcia) Comentar log para produccion
        //LOG.warning("validatePaymentMethod -> order.id: [" + deliveryOrder.getIdFarmatodo() + "]");

        Answer answerDefault = new Answer();
        answerDefault.setConfirmation(true);

        final Answer[] answer = {answerDefault};
        List<DeliveryOrderItem> deliveryOrderItemList = ofy().load().type(DeliveryOrderItem.class).ancestor(deliveryOrder).list();
        if (deliveryOrderItemList != null) {
            //LOG.warning("items.size: [" + deliveryOrderItemList.size() + "]");
            deliveryOrderItemList.stream().filter(deliveryOrderItem -> deliveryOrderItem.getCoupon() != null).filter(DeliveryOrderItem::getCoupon).forEach(deliveryOrderItem -> {
                        Coupon coupon = ofy().load().type(Coupon.class).filter("itemId", deliveryOrderItem.getIdItem()).first().now();
                        if (coupon != null) {
                            //LOG.warning("Coupon => name: [" + coupon.getName() + "], type: [" + coupon.getCouponType().getCouponType() + "]");
                            switch (coupon.getCouponType()) {
                                case PAYMETHOD:
                                    answer[0] = orders.validatePaymentMethod(coupon, (long) user.getId());
                                    break;
                                case FIRSTPURCHASE:
                                case VALUE:
                                case PERCENTAGE:
                                case BRAND:
                                    answer[0] = orders.validateCouponRestrictionValue(deliveryOrder, coupon);
                                    break;
                                default:
                                    answer[0] = answerDefault;
                            }
                        }
                    }
            );
        }

        LOG.info("Answer: [" + String.valueOf(answer[0].isConfirmation()) + "]");
        return answer[0];
    }

    @ApiMethod(name = "cardTokenization", path = "/orderEndpoint/cardTokenization", httpMethod = ApiMethod.HttpMethod.POST)
    public CreditCardTokenized cardTokenization(@Named("token") final String token,
                                                @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                                @Named("tokenCard") final String tokenCard) throws BadRequestException, ConflictException, IOException {
        if (!authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);

        Jws<Claims> claims;
        try {
            claims = Jwts.parser()
                    .setSigningKey(Constants.JWT_SECRET.getBytes("UTF-8"))
                    .parseClaimsJws(tokenCard);
        } catch (ExpiredJwtException | UnsupportedEncodingException expiredJwtException) {
            LOG.log(Level.SEVERE, expiredJwtException.getMessage(), expiredJwtException);
            throw new BadRequestException(Constants.ERROR_DE_CREDENTIAL);
        } catch (SignatureException | MalformedJwtException signatureException) {
            LOG.log(Level.SEVERE, signatureException.getMessage(), signatureException);
            throw new ConflictException(Constants.ERROR_DE_CREDENTIAL);
        }

        CreditCardJwt creditCardJwt = new CreditCardJwt();
        creditCardJwt.setPayerId((String) claims.getBody().get("payerId"));
        creditCardJwt.setName((String) claims.getBody().get("name"));
        creditCardJwt.setIdentificationNumber((String) claims.getBody().get("identificationNumber"));
        creditCardJwt.setPaymentMethod((String) claims.getBody().get("paymentMethod"));
        creditCardJwt.setNumber((String) claims.getBody().get("number"));
        creditCardJwt.setExpirationDate((String) claims.getBody().get("expirationDate"));

        String request = orders.tokenCreditCard(creditCardJwt).toJSONString();
        CreditCardTokenized creditCardTokenized = null;
        try {
            creditCardTokenized = CoreConnection.postRequest(URLConnections.URL_PAYU, request, CreditCardTokenized.class);
        } catch (Exception e) {
            LOG.warning(e.getMessage());
        }
        return creditCardTokenized;
    }

    @ApiMethod(name = "createTracing", path = "/orderEndpoint/createTracing", httpMethod = ApiMethod.HttpMethod.POST)
    public Answer createTracing(final CreatedOrder orderFarmatodo) throws UnauthorizedException, BadRequestException, ConflictException, AlgoliaException, IOException {

        if (!Constants.KEY_SECURE_CLIENT.equals(orderFarmatodo.getKeyClient()))
            throw new UnauthorizedException(Constants.ERROR_ACCESS_DENIED);

        if (orderFarmatodo.getTracing() == null)
            throw new BadRequestException(Constants.TRACING_INITIALIZATION);

        DeliveryOrder deliveryOrder = ofy().load().type(DeliveryOrder.class).filter("idOrder", orderFarmatodo.getIdOrder()).first().now();

        if (deliveryOrder == null)
            throw new ConflictException(Constants.DELIVERY_ORDER_NOT_FOUND);

        User user = deliveryOrder.getIdCustomer().get();
        Key<Customer> userKey = deliveryOrder.getIdCustomer().getKey();
        Tracing tracing = orderFarmatodo.getTracing().get(0);
        tracing.setIdTracing(UUID.randomUUID().toString());
        tracing.setDeliveryOrderId(Ref.create(Key.create(userKey, DeliveryOrder.class, deliveryOrder.getIdDeliveryOrder())));

        if (Objects.nonNull(tracing.getUuid())) {
            saveLogIntentPayment(orderFarmatodo.getIdOrder(), tracing.getUuid(), tracing.getStatusCode(), tracing.getComments());
        }

        String type;
        boolean isDeliveryFast;
        String statusBraze;

        OrderMessageConfiguration config = APIAlgolia.getNotificationOrderMessage();
        Optional<StatusMessageConfig> messageConfig = config.getConfig().stream().filter(c -> c.getStatus().equals(String.valueOf(tracing.getStatus()))).findFirst();
        String qualifyMessage = Objects.nonNull(config.getQualifyMessage()) ? config.getQualifyMessage() : "No olvides calificar tu orden!";
        String qualifyTitle = Objects.nonNull(config.getQualifyTitle()) ? config.getQualifyTitle() : "No olvides calificar tu orden!";
        String message = messageConfig.map(StatusMessageConfig::getMessage).orElse(null);
        String title = messageConfig.map(StatusMessageConfig::getTitle).orElse(null);


        switch (tracing.getStatus()) {
            case 1:
                deliveryOrder.setLastStatus(ORDER_CREATED);
                statusBraze = BrazeStatusEnum.CREATED.getValue();
                deliveryOrder.setActive(true);
                deliveryOrder.setPush(message);
                type = ORDER_CREATED;
                isDeliveryFast = false;
                break;
            case 3:
                type = ORDER_ASSIGNED;
                statusBraze = BrazeStatusEnum.ASSIGNED.getValue();
                isDeliveryFast = false;
                deliveryOrder.setActive(true);
                break;
            case 5:
                type = ORDER_INCOMING;
                statusBraze = BrazeStatusEnum.BILLED.getValue();
                isDeliveryFast = false;
                deliveryOrder.setActive(true);
                if (Boolean.TRUE.equals(isOrdenScanAndGo(deliveryOrder))) {
                    type = ORDER_DELIVERED;
                    deliveryOrder.setLastStatus(ORDER_DELIVERED);
                    deliveryOrder.setActive(false);
                    tracing.setStatus(OrderStatusEnum.FINALIZADA.getId().intValue());
                    sendScanAndGoNotificacion(user.getId(), deliveryOrder);
                }
                break;
            case 6:
                Boolean wasFast = isDeliveryFast(orderFarmatodo.getIdOrder());

                if (Boolean.TRUE.equals(wasFast)) {
                    Optional<DeliveryFast> deliveryFast = APIAlgolia.getDeliveryFastProperties();
                    deliveryOrder.setLastStatus(ORDER_DELIVERED);
                    type = ORDER_DELIVERED;
                    message = deliveryFast.get().getMessage();
                    isDeliveryFast = true;
                    deliveryOrder.setActive(false);
                } else {
                    type = ORDER_DELIVERED;
                    isDeliveryFast = false;
                    deliveryOrder.setActive(false);
                }
                statusBraze = BrazeStatusEnum.DELIVERY.getValue();
                break;
            case 7:
                deliveryOrder.setLastStatus(ORDER_FINALIZED);
                statusBraze = BrazeStatusEnum.FINISH.getValue();
                type = null;
                isDeliveryFast = false;
                deliveryOrder.setActive(false);
                notificationBrazeFinalized(user, deliveryOrder, qualifyTitle, qualifyMessage, statusBraze);
                break;
            case 14:
                deliveryOrder.setLastStatus(ORDER_CANCELED);
                statusBraze = BrazeStatusEnum.CANCEL.getValue();
                deliveryOrder.setActive(false);
                type = null;
                message = null;
                isDeliveryFast = false;
                break;
            default:
                type = null;
                message = null;
                isDeliveryFast = false;
                statusBraze = "";

        }

        updateStatusPushNotification(message, deliveryOrder, config, user, isDeliveryFast, title, statusBraze);

        deliveryOrder.setLastStatus(type);
        deliveryOrder.setPush(message);
        ofy().save().entities(tracing, deliveryOrder, deliveryOrder);
        Answer answer = new Answer();
        answer.setConfirmation(true);
        return answer;
    }

    private static void notificationBrazeFinalized(
            User user, DeliveryOrder deliveryOrder, String qualifyTitle, String qualifyMessage, String statusBraze) {

        CustomerOnlyData customerOnlyData;
        try {
            String messengerName = null;
            customerOnlyData = ApiGatewayService.get().getCustomerOnlyById(user.getId());

            Optional<String> optionalMessengerName = ApiGatewayService.get().getMessengerNameByOrderId(deliveryOrder.getIdOrder());
            if (optionalMessengerName.isPresent()) {
                messengerName = optionalMessengerName.get();
            }

            if (customerOnlyData != null && customerOnlyData.getEmail() != null && !customerOnlyData.getEmail().isEmpty()) {
                PushNotificationRequest request = new PushNotificationRequest(customerOnlyData.getEmail(),
                        qualifyTitle, qualifyMessage,
                        Constants.URL_ORDER_REVIEW + deliveryOrder.getIdOrder(),
                        deliveryOrder.getIdOrder(), statusBraze, messengerName);
                LOG.info("request: " + request);
                ApiGatewayService.get().sendPushNotificationBraze(request);
            }
        } catch (Exception e) {
            LOG.info("No se pudo enviar push notification " + e.getMessage());
        }
    }

    private void updateStatusPushNotification(
            String message, DeliveryOrder deliveryOrder, OrderMessageConfiguration config, User user,
            Boolean isDeliveryFast, String title, String statusBraze) {

        CustomerOnlyData customerOnlyData;
        if (message != null && !isOrdenScanAndGo(deliveryOrder)) {
            Date currentDate = new Date();
            int min = (int) ((currentDate.getTime() / 60000) - (deliveryOrder.getCreateDate().getTime() / 60000));
            String messageForFastDelivery = config.getTurboMessage().replace("{min}", String.valueOf(min));

            try {
                String messengerName = null;
                customerOnlyData = ApiGatewayService.get().getCustomerOnlyById(user.getId());

                Optional<String> optionalMessengerName = ApiGatewayService.get().getMessengerNameByOrderId(
                        deliveryOrder.getIdOrder());
                if (optionalMessengerName.isPresent()) {
                    messengerName = optionalMessengerName.get();
                }

                LocalDateTime nowDate = LocalDateTime.now();
                LocalDateTime timeOrderCreate= deliveryOrder.getCreateDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                Long hoursDiffCreate = Duration.between(timeOrderCreate, nowDate).toHours();
                LocalDateTime timeOrderPicking= null;
                Long hoursDiffPicking =0L;
                if(Objects.nonNull(deliveryOrder.getPickingDate())){
                    timeOrderPicking= deliveryOrder.getPickingDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                    hoursDiffPicking = Duration.between(timeOrderPicking, nowDate).toHours();
                }
                if (customerOnlyData != null && Objects.nonNull(customerOnlyData.getEmail()) && !customerOnlyData.getEmail().isEmpty() && isaOrderCurrent(hoursDiffCreate, hoursDiffPicking)) {
                    PushNotificationRequest request = new PushNotificationRequest(customerOnlyData.getEmail(),
                            isDeliveryFast.equals(Boolean.TRUE) ? messageForFastDelivery : title, message,
                            Constants.URL_ORDER_DETAIL + deliveryOrder.getIdOrder(),
                            deliveryOrder.getIdOrder(), statusBraze, messengerName);
                    ApiGatewayService.get().sendPushNotificationBraze(request);
                }
            } catch (Exception e) {
                LOG.warning("No se pudo enviar push notification a braze para la actualizacion de estados " + e.getMessage());
            }
        }
    }
    private boolean isaOrderCurrent(Long hoursDiffCreate, Long hoursDiffPicking) {
        final int HOURS_MIN_PICKING_DATE = 0;
        final int LIMIT_HOURS_PICKING_DATE = 72;
        final int LIMIT_HOURS_CREATE_DATE = 24;
        return hoursDiffCreate <= LIMIT_HOURS_CREATE_DATE || (hoursDiffPicking > HOURS_MIN_PICKING_DATE && hoursDiffPicking <= LIMIT_HOURS_PICKING_DATE);
    }
    private Boolean isDeliveryFast(Long orderId) {
        final SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("America/Bogota"));

        Date currentDate = new Date();
        DeliveryOrder deliveryOrder = ofy().load().type(DeliveryOrder.class).filter("idOrder", orderId).first().now();
        int min = (int) ((currentDate.getTime() / 60000) - (deliveryOrder.getCreateDate().getTime() / 60000));
        Optional<DeliveryFast> optionalDeliveryFast = APIAlgolia.getDeliveryFastProperties();

        return optionalDeliveryFast.isPresent()
                && deliveryOrder.getDeliveryType() == DeliveryType.EXPRESS
                && optionalDeliveryFast.get().isActive()
                && min < optionalDeliveryFast.get().getMinDeliveryTime();
    }


    private Boolean isScanAndGo(DeliveryOrder deliveryOrder) {
        return Objects.nonNull(deliveryOrder.getDeliveryType()) && DeliveryType.SCANANDGO.getDeliveryType().equals(deliveryOrder.getDeliveryType().getDeliveryType());
    }

    private Boolean isDatafonoScanAndGo(DeliveryOrder deliveryOrder) {
        return Objects.nonNull(deliveryOrder.getPaymentType()) && PaymentTypeEnum.DATAFONOS.getId() == deliveryOrder.getPaymentType().getId();
    }

    private void sendScanAndGoNotificacion(final Integer userId, final DeliveryOrder deliveryOrder) {
//        LOG.info("Request sendScanAndGoNotificacion() orderId: " + deliveryOrder.getIdOrder());
        try {
            if (isDatafonoScanAndGo(deliveryOrder)) {
                Optional<ScanAndGoPushNotificationProperty> optionalScanAndGoPushNotificationProperty = APIAlgolia.getScanAndGoPushNotificationProperty(URLConnections.ALGOLIA_SCAN_AND_GO_PUSH_REGISTERED_ORDER);
                FirebaseNotification.sendScanAndGoPushNotification(userId, optionalScanAndGoPushNotificationProperty.get(), deliveryOrder.getIdOrder());
            }
        } catch (Exception ex) {
            LOG.warning("Request sendScanAndGoNotificacion() error al enviar el psuh de la orden " + deliveryOrder.getIdOrder() + " Error: " + ex.getMessage());
        }
    }

    @ApiMethod(name = "pushNotifyV2", path = "/orderEndpoint/v2/pushNotify", httpMethod = ApiMethod.HttpMethod.POST)
    public Answer pushNotifyV2(final NotifyOrderV2 data) {
        Answer response = new Answer();
        response.setConfirmation(true);

        if (data.getMessage() == null || data.getTitle() == null) {
            response.setMessage("No se encontro mensaje o titulo en el request");
            response.setConfirmation(false);
            return response;
        }

        if (data.getIdUser() == null) {
            response.setMessage("No se encontro usuario en el request");
            response.setConfirmation(false);
            return response;
        }

        try {
            final String type = ORDER_CREATED;

            String beginningMessage = "Estado de tu pedido ";
            String messageForFastDelivery = "Wow, llegamos en 10 min \uD83D\uDEF5 \uD83D\uDCA8";

            FirebaseNotification.notificationervice(data.getIdUser().intValue(),
                    beginningMessage,
                    messageForFastDelivery,
                    0, type,
                    0L);
            response.setMessage("Success");
            response.setConfirmation(true);
            return response;
        } catch (Exception ex) {
            LOG.warning("Request sendScanAndGoNotificacion() error al enviar el push Error: " + ex.getMessage());
        }
        return response;
    }

    @ApiMethod(name = "pushNotify", path = "/orderEndpoint/pushNotify", httpMethod = ApiMethod.HttpMethod.POST)
    public Answer pushNotify(final NotifyOrder data) {

        Answer response = new Answer();
        response.setConfirmation(true);

        if (Objects.isNull(data.getIdOrder())) {
            response.setMessage("No se encontro orden en el request");
            response.setConfirmation(false);
            return response;
        }

        if (Objects.isNull(data.getMessage()) || Objects.isNull(data.getTitle())) {
            response.setMessage("No se encontro mensaje o titulo en el request");
            response.setConfirmation(false);
            return response;
        }

        if (Objects.isNull(data.getIdUser())) {
            response.setMessage("No se encontro usuario en el request");
            response.setConfirmation(false);
            return response;
        }
        try {
            final String type = ORDER_CREATED;
            FirebaseNotification.notificationervice(data.getIdUser().intValue(), data.getTitle(), data.getMessage(), 0, type, data.getIdOrder());
            response.setMessage("Success");
            response.setConfirmation(true);
            return response;
        } catch (Exception ex) {
            LOG.warning("Request sendScanAndGoNotificacion() error al enviar el psuh de la orden " + data.getIdOrder() + " Error: " + ex.getMessage());
        }

        return response;
    }


    @ApiMethod(name = "createLogTracing", path = "/orderEndpoint/createLogTracing", httpMethod = ApiMethod.HttpMethod.POST)
    public Answer createLogTracing(final CreatedOrder orderFarmatodo) throws UnauthorizedException, BadRequestException, ConflictException {
//        LOG.info("Request createLogTracing(): " + orderFarmatodo);
        //LOG.warning("Request createLogTracing() Objects.isNull(orderFarmatodo.getTracing()): " + Objects.isNull(orderFarmatodo.getTracing()));
        //LOG.warning("Request createLogTracing() Objects.nonNull(orderFarmatodo.getIdOrder()): " + Objects.nonNull(orderFarmatodo.getIdOrder()));
        if (Objects.isNull(orderFarmatodo.getTracing()) || Objects.isNull(orderFarmatodo.getIdOrder())) {
            throw new BadRequestException(Constants.TRACING_INITIALIZATION);
        }
        //LOG.warning("OrderId: " + String.valueOf(orderFarmatodo.getIdOrder()));
        Tracing tracing = orderFarmatodo.getTracing().get(0);
        //LOG.warning("UUID: " + String.valueOf(tracing.getUuid()));
        saveLogIntentPayment(orderFarmatodo.getIdOrder(), tracing.getUuid(), tracing.getStatusCode(), tracing.getComments());

        Answer answer = new Answer();
        answer.setConfirmation(true);
        return answer;
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
        deliveryOrderItem.setItemUrl(item.getItemUrl());
        deliveryOrderItem.setFiltersOptical(item.getFiltersOptical());
        deliveryOrderItem.setOnlyOnline(item.isOnlyOnline());
        deliveryOrderItem.setUuidItem(item.getUuidItem());

        return deliveryOrderItem;
    }

    @ApiMethod(name = "getPhoneByCity", path = "/orderEndpoint/getPhoneByCity", httpMethod = ApiMethod.HttpMethod.GET)
    public City getPhoneByCity(@Named("token") final String token,
                               @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                               @Nullable @Named("idStoreGroup") final int idStoreGroupFromRequest,
                               @Nullable @Named("idCity") final String idCity) throws ConflictException, BadRequestException, AlgoliaException {
        if (!authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);
        int idStoreGroup = APIAlgolia.getDefaultStoreIdByStoreId(idStoreGroupFromRequest);
        if (idCity == null && idStoreGroup > 0) {
            Store store = ofy().load().type(Store.class).filter("id", idStoreGroup).first().now();
            if (store == null) {
                //LOG.warning("Store is null!");
                return null;
            } else if (store.getOwner().get() == null) {
                //LOG.warning("Store.getOwner is null!");
                return null;
            } else if (store.getOwner().get().getOwner().get() == null) {
                //LOG.warning("Store.getOwner.getOwner is null!");
                return null;
            }
            return store.getOwner().get().getOwner().get();
        } else if (idCity != null)
            return ofy().load().type(City.class).filter("id", idCity).first().now();
        else
            return null;

    }

    @ApiMethod(
            name = "getRatings",
            path = "/orderEndpoint/getRatings",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Rating> getRatings(@Named("token") final String token, @Named("tokenIdWebSafe") final String tokenIdWebSafe) throws BadRequestException, UnauthorizedException, ConflictException, InternalServerErrorException, ServiceUnavailableException, IOException {
//        LOG.info("method: getRatings()");
        if (token == null || token.isEmpty()) {
//            LOG.info("method: getRatings() --> BadRequest [token is required]");
            throw new BadRequestException("BadRequest [token is required]");
        }
        if (tokenIdWebSafe == null || tokenIdWebSafe.isEmpty()) {
//            LOG.info("method: getRatings() --> BadRequest [tokenIdWebSafe is required]");
            throw new BadRequestException("BadRequest [tokenIdWebSafe is required]");
        }
        if (!authenticate.isValidToken(token, tokenIdWebSafe)) {
//            LOG.info("method: getRatings() --> ConflictException [bad credentials]");
            throw new UnauthorizedException(Constants.INVALID_TOKEN);
        }

        List<Rating> ratings = ApiGatewayService.get().getRatings().getData().getOrderReviewDomain();
//        LOG.info("RatingResponse->" + ratings.toString());
//        LOG.info("method: getRatings() --> setear la lista en el objeto de respuesta");
        return CollectionResponse.<Rating>builder().setItems(ratings).build();
    }

    @ApiMethod(
            name = "qualifyService",
            path = "/orderEndpoint/qualifyService",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public Answer qualifyService(@Named("token") final String token, @Named("tokenIdWebSafe") final String tokenIdWebSafe, final Qualification qualification) throws BadRequestException, UnauthorizedException, ConflictException, InternalServerErrorException, ServiceUnavailableException {
        // Validar inputs
        Guard.validateInputs(token, tokenIdWebSafe, qualification);

        // Obtener resumen del pedido
        DeliveryOrder deliveryOrder = ofy().load().type(DeliveryOrder.class).filter("idOrder", qualification.getOrderId()).first().now();
        if (deliveryOrder == null) {
            throw new ConflictException(Constants.DELIVERY_ORDER_NOT_FOUND);
        }

        // Calificar el pedido
        return qualifyOrder(qualification,"v1",deliveryOrder);
    }

    @ApiMethod(
            name = "qualifyServicev3",
            path = "/orderEndpoint/v3/qualifyService",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public Answer qualifyServicev3(@Named("token") final String token, @Named("tokenIdWebSafe") final String tokenIdWebSafe, final Qualification qualification) throws BadRequestException, UnauthorizedException, ConflictException, InternalServerErrorException, ServiceUnavailableException {
        // Validar inputs
        Guard.validateInputs(token, tokenIdWebSafe, qualification);

        // Obtener resumen del pedido
        GetOrderSumary orderSummary = getOrderSummary(Long.valueOf(qualification.getOrderId()));
        if (orderSummary == null) {
            throw new ConflictException(Constants.DELIVERY_ORDER_NOT_FOUND);
        }

        // Calificar el pedido
        return qualifyOrder(qualification,"v3",null);
    }

    private GetOrderSumary getOrderSummary(Long orderId) throws ConflictException {
        try {
            return ApiGatewayService.get().getOrderSumary(orderId);
        } catch (IOException e) {
            LOG.warning("Error converting Integer to Long: " + e.getMessage());
            throw new ConflictException("Error retrieving order summary for id: " + orderId, e);
        }
    }

    private Answer qualifyOrder(Qualification qualification,String version,DeliveryOrder deliveryOrder)
            throws InternalServerErrorException, ServiceUnavailableException {
        try {
            Response<GetCustomerResponse> response = ApiGatewayService.get().orderQualify(qualification);
            Answer answer = new Answer();
            if (response.isSuccessful()) {
                if(version.equals("v1")){
                    deliveryOrder.setCustomerReview(qualification.getRatingId());
                    deliveryOrder.setCustomerReviewComments(qualification.getComments());
                    ofy().save().entity(deliveryOrder);
                    answer.setConfirmation(true);
                    return answer;
                }else if(version.equals("v3")) {
                    answer.setConfirmation(true);
                    return answer;
                }
            }
            throw new ServiceUnavailableException("Back3 Service Unavailable Exception");
        } catch (IOException ex) {
            throw new InternalServerErrorException(ex.getMessage());
        } catch (ConflictException e) {
            LOG.warning("Conflict occurred while qualifying order: " + e.getMessage());
            throw new ServiceUnavailableException("Unable to qualify order due to conflict", e);
        }
    }

    @ApiMethod(name = "editOrder", path = "/orderEndpoint/editOrder", httpMethod = ApiMethod.HttpMethod.PUT)
    public OrderEditRes editOrder(final OrderEdit orderEditBck3) throws IOException, ServiceUnavailableException, BadRequestException {
//        LOG.info("String " + orderEditBck3.toString());
        if (!orderEditBck3.isValid())
            throw new BadRequestException("BadRequest [not valid values for request or values are null]");
        // validar cupones -- customer coupon
        long idCustomer;

        DeliveryOrder deliveryOrderSaved = ofy().load().type(DeliveryOrder.class).filter("idOrder", orderEditBck3.getOrder_no()).first().now();

        // agregar y validar cupones de la orden
        if (deliveryOrderSaved != null) {
            final List<DeliveryOrderItem> deliveryOrderItemList = ofy().load().type(DeliveryOrderItem.class).ancestor(deliveryOrderSaved).list();
            for (DeliveryOrderItem itemSaved : deliveryOrderItemList) {
                if (itemSaved.getCoupon() != null && itemSaved.getCoupon()) {
                    Coupon coupon = ofy().load().type(Coupon.class).filter("itemId", itemSaved.getIdItem()).first().now();
                    if (orders.validateCoupon(coupon, (long) deliveryOrderSaved.getIdFarmatodo())) {
                        ArrayList<CouponRequest> coupons = new ArrayList<>();
                        coupons.add(new CouponRequest(coupon.getCouponType(), coupon.getOfferId(), false));
                        orderEditBck3.setCoupons(coupons);
                    }
                }
            }
        }


        OrderEditRes res;
        try {
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
//            LOG.info("Request -> " + gson.toJson(orderEditBck3));
            res = ApiGatewayService.get().putOrdenEditBck3(orderEditBck3);
            //LOG.info("Request -> " + gson.toJson(orderEdit));
            //res = CoreService.get().putOrderEdit(orderEdit);
            if (res != null) {
                return res;
            } else {
                throw new ServiceUnavailableException("No fue posible editar la orden");
            }
        } catch (Exception e) {
            LOG.info("ERROR -> " + e.getMessage());
            throw new ServiceUnavailableException("No fue posible editar la orden");
        }
    }

    @ApiMethod(name = "validateCloseStores", path = "/orderEndpoint/validateCloseStores", httpMethod = ApiMethod.HttpMethod.POST)
    public ListAvaliableStoresRes validateCloseStores(final ListPossibleStores listPossibleStores) {
        //LOG.warning("method: validateCloseStores " + listPossibleStores);
        List<Long> storeList = listPossibleStores.getListStores();
        List<Long> storeListAvailable = new ArrayList<>();

        if (Objects.nonNull(storeList) && !storeList.isEmpty()) {
            storeList.forEach(store -> {
                if (isStoreAvailable(String.valueOf(store), null)) {
                    //LOG.warning("method: validateCloseStores isStoreAvailable storeId:" + store);
                    storeListAvailable.add(store);
                }
            });
        }

        ListAvaliableStoresRes response = new ListAvaliableStoresRes();
        response.setListAvaliableStoresRes(storeListAvailable);
        return response;
    }


    /**
     * Validación de horarios de las tiendas basado en la configuración de Algoia en el indie Store_Config.
     */
    private Boolean isStoreAvailable(final String storeID, final Date pickingDate) {
        return orderMethods.isStoreAvailable(storeID, pickingDate);
    }

    @ApiMethod(name = "testHour", path = "/orderEndpoint/testHour", httpMethod = ApiMethod.HttpMethod.POST)
    public Answer testHour(final DeliveryOrder order) {
        Calendar calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("America/Bogota"));
        calendar.setTime(new Date());
        Answer a = new Answer();
        a.setMessage("" + calendar.get(Calendar.HOUR_OF_DAY));
        return a;
    }

    @ApiMethod(name = "updateOrderStatus", path = "/orderEndpoint/order/status/update", httpMethod = ApiMethod.HttpMethod.POST)
    public OrderStatusResponse updateOrderStatus(final DeliveryOrderStatus orderStatus) throws IOException, ServiceUnavailableException, BadRequestException, UnauthorizedException, InternalServerErrorException, NotFoundException, ConflictException {
//        LOG.info("method: updateOrderStatus(" + orderStatus + ")");
        if (Objects.isNull(orderStatus)) {
//            LOG.info("method: updateOrderStatus() --> BadRequest [event is required]");
            throw new BadRequestException("BadRequest [event is required]");
        } else if (Objects.isNull(orderStatus.getOrder_no())) {
//            LOG.info("method: updateOrderStatus() --> BadRequest [orderStatus.getOrderNo() is required]");
            throw new BadRequestException("BadRequest [orderStatus.getOrderNo() is required]");
        } else if (Objects.isNull(orderStatus.getUuid())) {
//            LOG.info("method: updateOrderStatus() --> BadRequest [orderStatus.getUuid() is required]");
            throw new BadRequestException("BadRequest [orderStatus.getUuid() is required]");
        }
        if (Objects.isNull(orderStatus.getStatus())) {
//            LOG.info("method: updateOrderStatus() --> BadRequest [orderStatus.getStatus() is required]");
            throw new BadRequestException("BadRequest [orderStatus.getStatus() is required]");
        }
        return ApiGatewayService.get().orderStatusUpdate(orderStatus);
    }

    @ApiMethod(name = "updatePickingDate", path = "/orderEndpoint/order/orderUpdate/pickingDate", httpMethod = ApiMethod.HttpMethod.PUT)
    public OrderStatusResponse updatePickingDate(final UpdatePickingDateReq updatePickingDateReq) throws IOException, ServiceUnavailableException, BadRequestException, UnauthorizedException, InternalServerErrorException, NotFoundException, ConflictException {
        return processPickingDate(updatePickingDateReq, false);
    }

    @ApiMethod(name = "updatePickingDatev3", path = "/orderEndpoint/order/orderUpdate/v3/pickingDate", httpMethod = ApiMethod.HttpMethod.PUT)
    public OrderStatusResponse updatePickingDatev3(final UpdatePickingDateReq updatePickingDateReq) throws IOException, ServiceUnavailableException, BadRequestException, UnauthorizedException, InternalServerErrorException, NotFoundException, ConflictException {
        return processPickingDate(updatePickingDateReq, true);
    }

    private OrderStatusResponse processPickingDate(UpdatePickingDateReq updatePickingDateReq, boolean isV3) throws  InternalServerErrorException  {
        try {
            // Validar entrada
            Guard.isValidUpdatePickingDateReq(updatePickingDateReq);

            // Obtener y actualizar la orden
            DeliveryOrder deliveryOrder = isV3
                    ? getDeliveryOrderFromOms(updatePickingDateReq)
                    : getDeliveryOrderFromDataStoreAndSave(updatePickingDateReq);


            // Actualizar OMS y eliminar seguimiento en Firebase
            OrderStatusResponse orderStatusResponse = updateOMSAndFirebase(updatePickingDateReq);

            // Enviar notificación push
            sendPushBraze(deliveryOrder, updatePickingDateReq);
            return orderStatusResponse;
        } catch (Exception e) {
            LOG.warning("Error al procesar la actualización de picking date"+ e.getMessage());
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    private DeliveryOrder getDeliveryOrderFromDataStoreAndSave(UpdatePickingDateReq req) throws NotFoundException {
        DeliveryOrder deliveryOrder = ofy().load().type(DeliveryOrder.class)
                .filter("idOrder", req.getOrderId()).first().now();

        if (deliveryOrder == null) {
            throw new NotFoundException("Orden no encontrada: " + req.getOrderId());
        }

        if (req.getPickingDate() != null) {
            deliveryOrder.setPickingDate(req.getPickingDate());
            deliveryOrder.setLastStatus(OrderStatus.ORDER_CREATED); // Constante/Enum
            if (!deliveryOrder.getActive()) {
                deliveryOrder.setActive(true);
            }
        }
        ofy().save().entity(deliveryOrder).now();
        return deliveryOrder;
    }

    private DeliveryOrder getDeliveryOrderFromOms(UpdatePickingDateReq req) throws ServiceUnavailableException {
        try {
            DeliveryOrderOms deliveryOrderOms = OrderUtil.getOrderMethodv2(String.valueOf(req.getOrderId()));
            if (deliveryOrderOms == null) {
                throw new NotFoundException("Order no encontrada en OMS: " + req.getOrderId());
            }
            DeliveryOrder deliveryOrder = new DeliveryOrder();
            deliveryOrder.setIdFarmatodo(deliveryOrderOms.getIdFarmatodo());
            return deliveryOrder;
        }catch (Exception e) {
            LOG.warning("Fallo al conseguir la orden desde OMS:" + e.getMessage());
            throw new ServiceUnavailableException("Fallo al conseguir la orden desde OMS ", e);
        }
    }

    private OrderStatusResponse updateOMSAndFirebase(UpdatePickingDateReq req) {

        OrderStatusResponse orderStatusResponse = null;
        try {
            orderStatusResponse = callUpdatePickingDateOMS(req);
            if (orderStatusResponse != null) {
                FirebaseService.get().deleteTrackingOrder(String.valueOf(req.getOrderId()));
            }
        } catch (Exception e) {
            LOG.warning("Error al actualizar la fecha de picking en OMS: " + e.getMessage());
        }
        return orderStatusResponse;
    }

    private static OrderStatusResponse callUpdatePickingDateOMS(UpdatePickingDateReq updatePickingDateReq) throws IOException, ConflictException {
        JSONObject orderJSON = new JSONObject();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        orderJSON.put("pickingDate", format.format(updatePickingDateReq.getPickingDate()) + "-0500");
        OrderStatusResponse orderStatusResponse = ApiGatewayService.get().orderPickingUpdate(updatePickingDateReq);
        return orderStatusResponse;
    }

    private static void sendPushBraze(DeliveryOrder deliveryOrder, UpdatePickingDateReq updatePickingDateReq) {
        try {
            CustomerOnlyData customerOnlyData = null;
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy");
            SimpleDateFormat formatHour = new SimpleDateFormat("hh:mm a");
            String rescheduleMessageFinish = "El pedido se ha programado para el " + format.format(updatePickingDateReq.getPickingDate()) + " a las " + formatHour.format(updatePickingDateReq.getPickingDate()) + " para más información de tu pedido ingresa aquí";
            customerOnlyData = ApiGatewayService.get().getCustomerOnlyById(deliveryOrder.getIdFarmatodo());
            OrderMessageConfiguration config = APIAlgolia.getNotificationOrderMessage();
            String rescheduleMessage = Objects.nonNull(config.getRescheduleMessage()) ? config.getRescheduleMessage().replace("{hour}", formatHour.format(updatePickingDateReq.getPickingDate())).replace("{date}", format.format(updatePickingDateReq.getPickingDate())) : rescheduleMessageFinish;
            String rescheduleTitle = Objects.nonNull(config.getRescheduleTitle()) ? config.getRescheduleTitle() : "Tu pedido ha sido reprogramado";
            if (Objects.nonNull(customerOnlyData)) {
                PushNotificationRequest request = new PushNotificationRequest(customerOnlyData.getEmail(), rescheduleTitle, rescheduleMessage, Constants.URL_ORDER_DETAIL + updatePickingDateReq.getOrderId());
                ApiGatewayService.get().sendPushNotificationBraze(request);
            }
        } catch (Exception e) {
            LOG.warning("Error sending push notification to Braze: " + e.getMessage());
        }

    }

    @ApiMethod(name = "pickingDateDelete", path = "/orderEndpoint/order/orderUpdate/pickingDateDelete", httpMethod = ApiMethod.HttpMethod.POST)
    public void pickingDateDelete(final UpdatePickingDateReq updatePickingDateReq) {
//        LOG.info(new Gson().toJson(updatePickingDateReq));
        FirebaseService.get().deleteTrackingOrder(String.valueOf(updatePickingDateReq.getOrderId()));
    }

    /**
     * ccrodriguez
     *
     * @param idOrder
     * @param token
     * @param tokenIdWebSafe
     * @return
     * @throws IOException
     * @throws ConflictException
     * @throws BadRequestException return tracking info by order
     */
    @ApiMethod(name = "getTrackingOrder", path = "/orderEndpoint/getTrackingOrder", httpMethod = ApiMethod.HttpMethod.GET)
    public OrderInfoStatus getTrackingOrder(@Named("idOrder") final String idOrder, @Named("token") final String token,
                                            @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                            @Nullable @Named("idCustomerWebSafe") final String idCustomerWebSafe, final HttpServletRequest req) throws IOException, ConflictException, BadRequestException {

        //LOG.warning("method: getTrackingOrder: " + idOrder);
        if (!authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);

        if (idOrder == null || idOrder.isEmpty()) {
            throw new ConflictException(Constants.PARAM_IS_EMPTY);
        }

        /* TODO: Habilitar validacion cuando los front actualicen la version
        if(Objects.isNull(idCustomerWebSafe) || idCustomerWebSafe.isEmpty()){
            throw new ConflictException(Constants.CUSTOMER_INITIALIZATION);
        }
        Key<User> userKey = Key.create(idCustomerWebSafe);
        User user = users.findUserByKey(userKey);
        if(user.getId() == 0){
            throw new ConflictException(Constants.CUSTOMER_INITIALIZATION);
        }*/

        Optional<OrderInfoStatus> optTracing = Optional.ofNullable(ApiGatewayService.get().getTrackingOrder(Long.parseLong(idOrder)).getData());
        if (!optTracing.isPresent())
            throw new BadRequestException(HttpStatusCode.BAD_REQUEST.getStatusName());

        if (optTracing == null)
            throw new BadRequestException(HttpStatusCode.BAD_REQUEST.getStatusName());

        /* TODO: Habilitar validacion cuando los front actualicen la version
        if(optTracing.get().getCustomerId() != user.getId()){
            throw new ConflictException(Constants.CUSTOMER_INITIALIZATION);
        }*/
        // validate is active Algolia
        Integer isActive = (Objects.nonNull(APIAlgolia.getActiveCourierTrackingSocket())) ?
                Objects.requireNonNull(APIAlgolia.getActiveCourierTrackingSocket()).
                        getActiveCouriers()
                        .stream()
                        .filter(courier -> optTracing.get().getCourierId() == courier)
                        .findAny()
                        .orElse(null) : null;
        // Consulta Url en Algolia
        WebSocketProperties httpsWebSocketProperties = APIAlgolia.getHttpsWebSocketUrl();
        optTracing.get().setHttpsWebSocketUrl(Objects.nonNull(httpsWebSocketProperties) && Objects.nonNull(httpsWebSocketProperties.getUrl()) && !httpsWebSocketProperties.getUrl().isEmpty() ? httpsWebSocketProperties.getUrl() : null);
        WebSocketProperties httpWebSocketProperties = APIAlgolia.getHttpWebSocketUrl();
        optTracing.get().setHttpWebSocketUrl(Objects.nonNull(httpWebSocketProperties) && Objects.nonNull(httpWebSocketProperties.getUrl()) && !httpWebSocketProperties.getUrl().isEmpty() ? httpWebSocketProperties.getUrl() : null);

        //LOG.warning("method: getTrackingOrder httpsUrlWebSocket: " + optTracing.get().getHttpsWebSocketUrl());
        //LOG.warning("method: getTrackingOrder httpUrlWebSocket: " + optTracing.get().getHttpWebSocketUrl());

        // fix status id
        optTracing.get().setStatusId((optTracing.get().getStatusId() == 6) ? 5 : optTracing.get().getStatusId());

        optTracing.get().setActiveSocket(Objects.nonNull(isActive));

        // ETA Basico.
        final Map<String, String> basicEta = getEstimatedTimeArrival(Long.parseLong(idOrder), optTracing.get().isTransfer());
        optTracing.get().setETALongTime(String.valueOf(basicEta.get(ETA_IN_LONG_TIMESTAMP)));
        optTracing.get().setETAMinutes(String.valueOf(basicEta.get(ETA_IN_MINUTES)));

        try {
            final GetCoodinatesCustomerAndAddressByOrderResponseData data = getCustomerAndStoresCoordinatesByOrderResponseData(Long.valueOf(idOrder));
            if (Objects.nonNull(data)) {
                optTracing.get().setCustomerAddress(data.getCustomerAddress());
                optTracing.get().setStores(data.getStores());
            }
        } catch (Exception e) {
            LOG.warning("Error. method: getTrackingOrder() -> It was not posible to get coordinate of customer and stores by order. Message: " + e.getMessage());
        }

        // fix PSE status 12

        DeliveryOrder deliveryOrder = getOrderMethod(null, Long.parseLong(idOrder), false, false);

        // fix ajuste al cancelar orden re-programada
        Boolean isGreaterthanNow = Objects.nonNull(deliveryOrder.getPickingDate()) ? deliveryOrder.getPickingDate().after(Calendar.getInstance().getTime()) : false;

        if (optTracing.get().getStatusId() == 0 || isGreaterthanNow == true) {
            deliveryOrder.setActive(true);
            deliveryOrder.setLastStatus(ORDER_CREATED);
            ofy().save().entity(deliveryOrder).now();
        }

        if (optTracing.get().getStatusId() == 14) {
            deliveryOrder.setActive(false);
            deliveryOrder.setLastStatus(ORDER_CANCELED);
            ofy().save().entity(deliveryOrder).now();
        }

        if (deliveryOrder.getPaymentType().getId() == PaymentTypeEnum.PSE.getId()) {

            if (optTracing.get().getStatusId() == STATUS_PAYMENT_SUCCESS) {
                optTracing.get().setStatusId(STATUS_ORDER_CREATED.intValue());
            }

        }

        optTracing.ifPresent(orderInfoStatus -> orders.getImagesTrackingOrder(orderInfoStatus));

        RequestSourceEnum source = ftdUtilities.getSourceFromRequestHeader(req);
        if (source.equals(RequestSourceEnum.ANDROID) || source.equals(RequestSourceEnum.IOS)) {
            return optTracing.orElseThrow(() -> new ConflictException(HttpStatusCode.NO_CONTENT.getStatusName()));
        }

        // Validate Security FOR WEB
        boolean webSecurityIsEnabled = webSecurityIsEnabled();

        if (!webSecurityIsEnabled) {
//            LOG.info("no se envia idCustomerWebSafe ");
            return optTracing.orElseThrow(() -> new ConflictException(HttpStatusCode.NO_CONTENT.getStatusName()));
        }

        boolean notAllowedTracking = notAllowedTracking(optTracing.get().getStatusId());

        if (notAllowedTracking) {
            return optTracing.orElseThrow(() -> new ConflictException(HttpStatusCode.NO_CONTENT.getStatusName()));
        }

        if (idCustomerWebSafe == null || idCustomerWebSafe.isEmpty()) {
            throw new ConflictException(Constants.INVALID_TOKEN);
        }

        Key<User> userKey = Key.create(idCustomerWebSafe);
        User user = users.findUserByKey(userKey);
        /* Solicitar informacion de usuario a farmatodo */

//        LOG.info("user_ftd -> " + user.toStringJson());
        //DeliveryOrder deliveryOrder = ofy().load().type(DeliveryOrder.class).filter("idOrder", idOrder).first().now();

        int customerId = user.getId();
        if (customerId <= 0 || deliveryOrder.getIdFarmatodo() <= 0) {
            throw new ConflictException(Constants.INVALID_TOKEN_ID_WEBSAFE);
        }

        if (deliveryOrder.getIdFarmatodo() != customerId) {
            throw new ConflictException(Constants.NOT_CONTENT);
        }

        return optTracing.orElseThrow(() -> new ConflictException(HttpStatusCode.NO_CONTENT.getStatusName()));
    }

    @ApiMethod(name = "getTrackingOrderV3", path = "/orderEndpoint/v3/getTrackingOrder", httpMethod = ApiMethod.HttpMethod.GET)
    public OrderInfoStatus getTrackingOrderV3(@Named("idOrder") final String idOrder, @Named("token") final String token,
                                              @Named("tokenIdWebSafe") final String tokenIdWebSafe, final HttpServletRequest req,
                                              @Named("idCustomerWebSafe") final String idCustomerWebSafe)
            throws IOException, ConflictException, BadRequestException {

        if (!authenticate.isValidToken(token, tokenIdWebSafe)) {
            throw new ConflictException(Constants.INVALID_TOKEN);
        }

        if (idOrder == null || idOrder.isEmpty()) {
            throw new ConflictException(Constants.PARAM_IS_EMPTY);
        }

        return processTrackingOrder(idOrder, req, "v3", idCustomerWebSafe);
    }

    @ApiMethod(name = "getTrackingOrderV2", path = "/orderEndpoint/v2/getTrackingOrder", httpMethod = ApiMethod.HttpMethod.GET)
    public OrderInfoStatus getTrackingOrderV2(@Named("idOrder") final String idOrder, @Named("token") final String token,
                                              @Named("tokenIdWebSafe") final String tokenIdWebSafe, final HttpServletRequest req,
                                              @Named("idCustomerWebSafe") final String idCustomerWebSafe)
            throws IOException, ConflictException, BadRequestException {

        if (!authenticate.isValidToken(token, tokenIdWebSafe)) {
            throw new ConflictException(Constants.INVALID_TOKEN);
        }

        if (idOrder == null || idOrder.isEmpty()) {
            throw new ConflictException(Constants.PARAM_IS_EMPTY);
        }

        return processTrackingOrder(idOrder, req, "v2", idCustomerWebSafe);
    }


    public OrderInfoStatus processTrackingOrder(@Named("idOrder") String idOrder, HttpServletRequest req, @Named("version") String version,
                                                @Named("idCustomerWebSafe") final String idCustomerWebSafe) throws IOException, ConflictException, BadRequestException {

        Optional<OrderInfoStatus> optTracing = Optional.ofNullable(ApiGatewayService.get().getTrackingOrder(Long.parseLong(idOrder)).getData());

        if (!optTracing.isPresent())
            throw new BadRequestException(HttpStatusCode.BAD_REQUEST.getStatusName());

        if (optTracing == null)
            throw new BadRequestException(HttpStatusCode.BAD_REQUEST.getStatusName());

        RequestSourceEnum source = ftdUtilities.getSourceFromRequestHeader(req);

        // validate is active Algolia
        Integer isActive = (Objects.nonNull(APIAlgolia.getActiveCourierTrackingSocket())) ?
                Objects.requireNonNull(APIAlgolia.getActiveCourierTrackingSocket()).
                        getActiveCouriers()
                        .stream()
                        .filter(courier -> optTracing.get().getCourierId() == courier)
                        .findAny()
                        .orElse(null) : null;

        optTracing.get().setStatusCode(HttpStatusCode.OK.getCode());
        optTracing.get().setStatusId((optTracing.get().getStatusId() == 6) ? 5 : optTracing.get().getStatusId());
        optTracing.get().setActiveSocket(Objects.nonNull(isActive));
        optTracing.get().setUuid(null);

        final Map<String, String> basicEta = getEstimatedTimeArrival(Long.parseLong(idOrder), optTracing.get().isTransfer());
        optTracing.get().setETALongTime(String.valueOf(basicEta.get(ETA_IN_LONG_TIMESTAMP)));
        optTracing.get().setETAMinutes(String.valueOf(basicEta.get(ETA_IN_MINUTES)));

        try {
            final GetCoodinatesCustomerAndAddressByOrderResponseData data = getCustomerAndStoresCoordinatesByOrderResponseData(Long.valueOf(idOrder));
            if (Objects.nonNull(data)) {
                optTracing.get().setCustomerAddress(data.getCustomerAddress());
                optTracing.get().setStores(data.getStores());
            }
        } catch (Exception e) {
            LOG.warning("Error. method: getTrackingOrder() -> It was not posible to get coordinate of customer and stores by order. Message: " + e.getMessage());
        }


        Object orderObject = getDeliveryOrder(version, idOrder);
        boolean isFuturePickingDate = isPickingDateInFuture(orderObject);

        if (optTracing.isPresent()) {
            int statusId = optTracing.get().getStatusId();
            updateOrderStatus(orderObject, statusId, isFuturePickingDate);
            handlePSEPayment(orderObject, statusId, optTracing);
        }

        if (version.equals("v2") && orderObject instanceof DeliveryOrder) {
            DeliveryOrder deliveryOrder = (DeliveryOrder) orderObject;
            ofy().save().entity(deliveryOrder).now();
        }

        optTracing.ifPresent(orderInfoStatus -> orders.getImagesTrackingOrder(orderInfoStatus));
        if(!source.equals(RequestSourceEnum.CALLCENTER)) {
            if(Objects.isNull(idCustomerWebSafe) || idCustomerWebSafe.isEmpty()){
                throw new ConflictException(Constants.INVALID_TOKEN);
            }
            Key<User> userKey = Key.create(idCustomerWebSafe);
            User user = users.findUserByKey(userKey);
            if (user.getId() == 0 || user.getId() != optTracing.get().getCustomerId()) {
                throw new ConflictException(Constants.CUSTOMER_INITIALIZATION);
            }
        }

        if (source.equals(RequestSourceEnum.ANDROID) || source.equals(RequestSourceEnum.IOS)) {
            return optTracing.orElseThrow(() -> new ConflictException(HttpStatusCode.NO_CONTENT.getStatusName()));
        }

        boolean webSecurityIsEnabled = webSecurityIsEnabled();

        if (!webSecurityIsEnabled) {
            return optTracing.orElseThrow(() -> new ConflictException(HttpStatusCode.NO_CONTENT.getStatusName()));
        }

        boolean notAllowedTracking = notAllowedTracking(optTracing.get().getStatusId());

        if (notAllowedTracking) {
            return optTracing.orElseThrow(() -> new ConflictException(HttpStatusCode.NO_CONTENT.getStatusName()));
        }

        return optTracing.orElseThrow(() -> new ConflictException(HttpStatusCode.NO_CONTENT.getStatusName()));
    }
    private void updateOrderStatus(Object order, int statusId, boolean isFuturePickingDate) {
        boolean isActive = statusId == OrderStatusEnum.RECIBIDA.getId() || isFuturePickingDate;
        String status = isActive ? ORDER_CREATED : ORDER_CANCELED;

        if (order instanceof DeliveryOrder) {
            DeliveryOrder deliveryOrder=(DeliveryOrder) order;
            deliveryOrder.setActive(isActive);
            deliveryOrder.setLastStatus(status);
        } else if (order instanceof DeliveryOrderOms) {
            DeliveryOrderOms deliveryOrderOms=(DeliveryOrderOms) order;
            deliveryOrderOms.setActive(isActive);
            deliveryOrderOms.setLastStatus(status);
        }
    }

    private void handlePSEPayment(Object objectDelivery,int statusId, Optional<OrderInfoStatus> optTracing) {
        if (objectDelivery instanceof DeliveryOrder) {
            DeliveryOrder deliveryOrder = (DeliveryOrder) objectDelivery;
            if (deliveryOrder.getPaymentType().getId() == PaymentTypeEnum.PSE.getId() && statusId == STATUS_PAYMENT_SUCCESS) {
                optTracing.get().setStatusId(STATUS_ORDER_CREATED.intValue());
            }
        }else if (objectDelivery instanceof DeliveryOrderOms) {
            DeliveryOrderOms deliveryOrderOms = (DeliveryOrderOms) objectDelivery;
            if (deliveryOrderOms.getPaymentType().getId() == PaymentTypeEnum.PSE.getId() && statusId == STATUS_PAYMENT_SUCCESS) {
                optTracing.get().setStatusId(STATUS_ORDER_CREATED.intValue());
            }
        }
    }

    private Object getDeliveryOrder(String version, String idOrder) throws ConflictException, BadRequestException {
        if ("v3".equals(version)) {
            return OrderUtil.getOrderMethodv2(idOrder);
        } else if ("v2".equals(version)) {
            return getOrderMethod(null, Long.parseLong(idOrder), false, false);
        }
        throw new IllegalArgumentException("Unsupported version: " + version);
    }

    private boolean isPickingDateInFuture(Object order) {
        if (order instanceof DeliveryOrder) {
            DeliveryOrder deliveryOrder = (DeliveryOrder) order;
            Date pickingDate = deliveryOrder.getPickingDate();
            if (pickingDate == null) return false;
            return pickingDate.after(Calendar.getInstance().getTime());
        } else if (order instanceof DeliveryOrderOms) {
            try {
                DeliveryOrderOms deliveryOrderOms = (DeliveryOrderOms) order;
                String pickingDateStr = deliveryOrderOms.getPickingDate();
                if (pickingDateStr == null) return false;
                Date pickingDate = OrderUtil.formatStringToDate(pickingDateStr);
                return pickingDate != null && pickingDate.after(Calendar.getInstance().getTime());
            } catch (Exception e) {
                LOG.warning("Error parsear picking date:" + e.getMessage());
                return false;
            }
        }
        return false;
    }


    /**
     * Create order offline
     *
     * @param keyClient
     * @param order
     * @return Created order
     * @throws BadRequestException
     * @throws ConflictException
     * @throws IOException
     * @throws InternalServerErrorException
     * @throws NotFoundException
     * @throws UnauthorizedException
     */
    @Deprecated
    @ApiMethod(name = "createOrderOffline", path = "/orderEndpoint/createOrderOffline", httpMethod = ApiMethod.HttpMethod.POST)
    public CreatedOrder createOrderOffline(
            @Named("keyClient") final String keyClient,
            final DeliveryOrder order) throws BadRequestException, ConflictException,
            IOException, InternalServerErrorException, NotFoundException, UnauthorizedException, ServiceUnavailableException {
        //Se valida obligatoriedad
        orders.validateOrder(order);
        //Se valida KeyClient
        orders.validateKeyClient(keyClient, order);

        CreatedOrder orderJSON = new CreatedOrder();

        String orderRequest = orders.createOrderJson(order, order.getItemList(), order.getProviderList()).toString();
        LOG.info("createOrderJson ->  " + (orderRequest != null ? orderRequest : " es null"));

        CreateOrderSubscribeReq orderSubRequest;
        Gson gson = new Gson();
        orderSubRequest = gson.fromJson(orderRequest, CreateOrderSubscribeReq.class);

        // call service
        //CreateOrderSubscribeData resCreateOrder = CoreService.get().createOrderSubscription(orderSubRequest);
        Response<CreateOrderSubscribeResponse> resCreateOrder = ApiGatewayService.get().createOrderPASBck3(orderSubRequest);

        if (Objects.isNull(resCreateOrder) || Objects.isNull(resCreateOrder.body()) || Objects.isNull(resCreateOrder.body().getData()) || resCreateOrder.body().getData().getId() == 0) {
            throw new ConflictException(Constants.DEFAULT_MESSAGE);
        }

        orderJSON.setId(resCreateOrder.body().getData().getId());
        orderJSON.setCreateDate(resCreateOrder.body().getData().getCreateDate());
        orderJSON.setAddress(resCreateOrder.body().getData().getAddress());
        orderJSON.setUpdateShopping(resCreateOrder.body().getData().isUpdateShopping());

        List<Tracing> tracingList = new ArrayList<>();

        try {
            if (resCreateOrder.body().getData().getTracing() != null && !resCreateOrder.body().getData().getTracing().isEmpty()) {
                resCreateOrder.body().getData().getTracing().forEach(tracing -> {
                    Tracing tracingAux = new Tracing();
                    tracingAux.setId(tracing.getId());
                    tracingAux.setCreateDate(tracing.getCreateDate());
                    tracingAux.setComments(tracing.getComments());
                    tracingAux.setCancellationReason(tracing.getCancellationReason());
                    tracingAux.setCourier(tracing.getCourier());
                    tracingAux.setStatus(tracing.getStatus());

                    tracingList.add(tracingAux);
                });

            }
        } catch (Exception e) {
            LOG.warning("Error NO grave al intentar setear el tracing... Mensaje: " + e.getMessage());
        }
        orderJSON.setTracing(tracingList);

        //LOG.warning("New Order to core ->  " + orderRequest);
        //LOG.warning("Result New Order to core ->  " + orderJSON);

        List<ItemAlgolia> itemOrders = new ArrayList<>();
        ItemAlgolia itemOrder = new ItemAlgolia();
        itemOrder.setAccess(true);
        itemOrder.setCalculatedPrice(0);
        itemOrder.setDiscount(0.0);
        itemOrder.setFullPrice(0D);
        itemOrder.setItem(orderJSON.getId());
        itemOrder.setPrice(0D);
        itemOrder.setQuantityBonus(0);
        itemOrder.setQuantityRequested(0);
        itemOrder.setItemDeliveryPrice(0);
        itemOrders.add(itemOrder);
        orderJSON.setItems(itemOrders);

        List<ProviderOrder> providers = new ArrayList<>();
        ProviderOrder provider = new ProviderOrder();
        provider.setName("");
        provider.setEmail("");
        provider.setDeliveryPrice(0);
        provider.setItems(itemOrders);
        providers.add(provider);
        orderJSON.setProviders(providers);

        //LOG.warning(String.valueOf(orderJSON.getId()));

        return orderJSON;
    }

    @ApiMethod(name = "sendMailSubscribeAndSaveCendis", path = "/orderEndpoint/sendMailSubscribeAndSaveCendis", httpMethod = ApiMethod.HttpMethod.POST)
    public Answer sendMailSubscribeAndSaveCendis(@Named("orderId") final Integer orderId) throws IOException {
        return ApiGatewayService.get().sendMailSubscribeAndSaveCendisBack3(orderId);
    }

    @ApiMethod(name = "createProviderTracing", path = "/orderEndpoint/createProviderTracing", httpMethod = ApiMethod.HttpMethod.POST)
    public Answer createProviderTracing(final CreatedOrder orderFarmatodo) throws UnauthorizedException, BadRequestException, ConflictException {
        if (!orderFarmatodo.getKeyClient().equals(Constants.KEY_SECURE_CLIENT))
            throw new UnauthorizedException(Constants.ERROR_ACCESS_DENIED);
        if (orderFarmatodo.getTracing() == null)
            throw new BadRequestException(Constants.TRACING_INITIALIZATION);
        //LOG.warning(" method createProviderTracing: " + String.valueOf(orderFarmatodo.getIdOrder()));
        Answer answer = new Answer();
        DeliveryOrder deliveryOrder = ofy().load().type(DeliveryOrder.class).filter("idOrder", orderFarmatodo.getIdOrder()).first().now();
        if (deliveryOrder != null) {
            // Consulta proveedores
            List<DeliveryOrderProvider> deliveryOrderProviderList = ofy().load().type(DeliveryOrderProvider.class).ancestor(deliveryOrder).list();
            deliveryOrder.setProviderList(deliveryOrderProviderList);
            Tracing tracing = (Objects.nonNull(orderFarmatodo.getTracing()) && !orderFarmatodo.getTracing().isEmpty()) ? orderFarmatodo.getTracing().get(0) : null;
            if (Objects.nonNull(tracing)) {
                DeliveryOrderProvider deliveryProvider = deliveryOrderProviderList.stream().filter(provider -> provider.getId() == tracing.getProviderId()).findFirst().get();
                deliveryProvider.setDeliveryStatus(tracing.getDeliveryStatus());
                ofy().save().entity(deliveryProvider).now();
                answer.setConfirmation(true);
                return answer;
            }
        }
        answer.setConfirmation(false);
        return answer;
    }

    @ApiMethod(name = "priceDeliveryOrderAgile", path = "/orderEndpoint/priceDeliveryOrderAgile", httpMethod = ApiMethod.HttpMethod.POST)
    public DeliveryOrder priceDeliveryOrderAgile(final ShoppingCartJson shoppingCartJson) throws ConflictException, IOException, BadRequestException, InternalServerErrorException, NotFoundException, UnauthorizedException, AlgoliaException {
        //LOG.warning("method: priceDeliveryOrderAgile");
        //LOG.warning("method: priceDeliveryOrderAgile(" + shoppingCartJson + ")");
        if (shoppingCartJson.getToken() == null || shoppingCartJson.getToken().isEmpty()) {
//            LOG.info("method: priceDeliveryOrderAgile() --> BadRequest [token is required]");
            throw new BadRequestException("BadRequest [token is required]");
        }
        if (shoppingCartJson.getTokenIdWebSafe() == null || shoppingCartJson.getTokenIdWebSafe().isEmpty()) {
//            LOG.info("method: priceDeliveryOrderAgile() --> BadRequest [tokenIdWebSafe is required]");
            throw new BadRequestException("BadRequest [tokenIdWebSafe is required]");
        }
        if (shoppingCartJson.getIdCustomerWebSafe() == null || shoppingCartJson.getIdCustomerWebSafe().isEmpty()) {
//            LOG.info("method: priceDeliveryOrderAgile() --> BadRequest [idCustomerWebSafe is required]");
            throw new BadRequestException("BadRequest [idCustomerWebSafe is required]");
        }
        if (shoppingCartJson.getIdStoreGroup() == 0) {
//            LOG.info("method: priceDeliveryOrderAgile() --> BadRequest [idStoreGroup is required]");
            throw new BadRequestException("BadRequest [idStoreGroup is required]");
        }
        if (shoppingCartJson.getSource() == null || shoppingCartJson.getSource().isEmpty()) {
//            LOG.info("method: priceDeliveryOrderAgile() --> BadRequest [source is required]");
            throw new BadRequestException("BadRequest [source is required]");
        } else {
            if (shoppingCartJson.getSource().equals("WEB")) {
                if (shoppingCartJson.getDeliveryType() == null) {
//                    LOG.info("method: priceDeliveryOrderAgile() --> BadRequest [deliveryType is required]");
                    throw new BadRequestException("BadRequest [deliveryType is required]");
                }
            } else {
                if (shoppingCartJson.getDeliveryType() == null) {
                    shoppingCartJson.setDeliveryType(DeliveryType.EXPRESS);
                }
            }
        }
        if (!authenticate.isValidToken(shoppingCartJson.getToken(), shoppingCartJson.getTokenIdWebSafe())) {
//            LOG.info("method: priceDeliveryOrderAgile() --> ConflictException [bad credentials]");
            throw new ConflictException(Constants.INVALID_TOKEN);
        }
        shoppingCartJson.setIdStoreGroup(APIAlgolia.getDefaultStoreIdByStoreId(shoppingCartJson.getIdStoreGroup()));

        List<DeliveryOrderItem> deliveryOrderItemListToSave = new ArrayList<>();
        List<DeliveryOrderProvider> deliveryOrderProviderListToSave = new ArrayList<>();
        List<DeliveryOrderItem> deliveryOrderItemList = new ArrayList<>();
        List<DeliveryOrderProvider> deliveryOrderProviderList;

//        LOG.info("idStoreGroup request -> " + shoppingCartJson.getIdStoreGroup()+" Request changue to -> " + shoppingCartJson.toStringJson());
        boolean isScanAndGo = isScanAndGo(shoppingCartJson.getDeliveryType());
        if (!isScanAndGo) {
            restrictItemsAlgolia(shoppingCartJson.getIdStoreGroup());
        }

        Key<User> customerKey = Key.create(shoppingCartJson.getIdCustomerWebSafe());
        DeliveryOrder deliveryOrder = ofy().load().type(DeliveryOrder.class)
                .filter("currentStatus", 1)
                .ancestor(Ref.create(customerKey)).first().now();

        if (deliveryOrder == null) {
//            LOG.info("No existe un carrito activo para el cliente.");
            deliveryOrder = getEmptyDeliveryOrder(new DeliveryOrder());
        } else {
//            LOG.info("Existe un carrito activo para el cliente, se procede a validar.Datastore deliveryOrder.deliveryType :" + deliveryOrder.getDeliveryType());
            deliveryOrderItemList = ofy().load().type(DeliveryOrderItem.class).ancestor(deliveryOrder).list();
            deliveryOrderProviderList = ofy().load().type(DeliveryOrderProvider.class).ancestor(deliveryOrder).list();

            if (Objects.isNull(deliveryOrderItemList) && Objects.isNull(deliveryOrderProviderList)) {
                LOG.warning("deliveryOrderItemList is null");
                throw new ConflictException("NO HAY ITEMS AGREGADOS");
            }
            //LOG.warning("items size: [" + (Objects.nonNull(deliveryOrderItemList) ? deliveryOrderItemList.size() : 0) + "]"+"providers size: [" + (Objects.nonNull(deliveryOrderProviderList) ? deliveryOrderProviderList.size() : 0) + "]");
            if (deliveryOrderItemList.isEmpty() && deliveryOrderProviderList.isEmpty()) {
                deliveryOrder = getEmptyDeliveryOrder(new DeliveryOrder());
            } else {
                // Valida la existencia de item que no sean cupones
                if (Objects.nonNull(deliveryOrderItemList) && deliveryOrderItemList.stream().filter(deliveryOrderItem -> Objects.isNull(deliveryOrderItem.getCoupon()) || !deliveryOrderItem.getCoupon()).findFirst().isPresent()) {
                    //Agrega los items a la Orden
                    deliveryOrder.setItemList(deliveryOrderItemList);
                    deliveryOrderItemList.stream().forEach(itemOrder -> {
                        deliveryOrderItemListToSave.add(itemOrder);
                    });

                    deliveryOrder.setOfferPrice(deliveryOrder.getOfferPrice());
                    deliveryOrder.setTotalPrice(deliveryOrder.getSubTotalPrice() + deliveryOrder.getTotalDelivery() - deliveryOrder.getOfferPrice());
                    // Campos nuevos proveedores

                    setQuantityInOrder(deliveryOrder, deliveryOrderProviderListToSave, deliveryOrderItemList, isScanAndGo);

                } else {
                    //LOG.warning("el carro de compras por el momento solo contiene cupones, no es necesario mandarlo a validar.");
                    for (DeliveryOrderItem item : deliveryOrderItemList) {
                        if (item.getCoupon() != null && item.getCoupon()) {
                            deliveryOrderItemListToSave.add(item);
                        }
                    }
                    deliveryOrder = getEmptyDeliveryOrder(deliveryOrder);
                }
                //Key<DeliveryOrder> deliveryOrderKey = ofy().save().entity(deliveryOrder).now();

                // delete duplicates
                deleteDeliveryOrderDuplicates(deliveryOrderItemListToSave, deliveryOrderProviderListToSave);
                //Async Call
//                LOG.info("TalonOne send delivery order: " + deliveryOrder.getOfferPrice());

                OrderUtil.deleteTipPriceZero(deliveryOrder.getItemList());
                ofy().save().entity(deliveryOrder);
                OrderUtil.deleteTipPriceZero(deliveryOrderItemListToSave);
                deliveryOrder.setItemList(deliveryOrderItemListToSave);
                addMarcaCategorySubcategorieAndItemUrl(deliveryOrder);
                deliveryOrder.setProviderList(deliveryOrderProviderListToSave);
                //LOG.warning("Key delivery Order with Provider: " + deliveryOrderKey);
            }
        }

        Optional<TipConfig> tipConfigOptional = GrowthBookConfigLoader.getTipsConfig(Constants.CUSTOMER_ANONYMOUS);
        // Validar si el item existe en el carrito
        if (shoppingCartJson.getIdItem() != 0 && Objects.nonNull(deliveryOrder.getItemList()) &&
                !deliveryOrder.getItemList().isEmpty() && deliveryOrder.getItemList().stream().filter(orderItem -> orderItem.getId() == shoppingCartJson.getIdItem()).findFirst().isPresent()) {
            deliveryOrder.setContainsItemQuantity(deliveryOrder.getItemList().stream().filter(orderItem -> orderItem.getId() == shoppingCartJson.getIdItem()).findFirst().get().getQuantitySold());
        }

        deliveryOrder.setDeliveryType(shoppingCartJson.getDeliveryType());

        //Set dynamic delivery Time Label...
        try {
            DeliveryTimeLabelTemplate deliveryTimeLabelTemplate = getCustomDeliveryTimeLabelTemplateForThisOrder(deliveryOrder);
            deliveryOrder.setDeliveryTimeLabel(deliveryTimeLabelTemplate);
        } catch (Exception e) {
            LOG.warning("No fue posible consultar la configuracion del DeliveryTimeLabelTemplate del Carrito: " + e);
        }

        // fix onlyonline
        if (Objects.nonNull(deliveryOrder.getItemList()) && !deliveryOrder.getItemList().isEmpty()) {

            List<Long> itemsToSearchAlgolia = new ArrayList<>();

            deliveryOrder.getItemList().forEach(it -> itemsToSearchAlgolia.add(it.getId()));

            List<Item> itemsAlgolia = productsMethods.getItemsByIdsAndStore(itemsToSearchAlgolia, shoppingCartJson.getIdStoreGroup());

            deliveryOrder.getItemList().forEach(it -> itemsAlgolia.forEach(itAlgolia -> {
                if (itAlgolia != null && itAlgolia.getId() == it.getId()) {
                    try {
                        it.setOnlyOnline(itAlgolia.isOnlyOnline());
                    } catch (Exception e) {
                        LOG.warning("Warn, onlyonline Not Found, se pondra en false para el item " + it.getId());
                        it.setOnlyOnline(false);
                    }
                }
            }));
        }

        // scan and go validate
        if (!deliveryOrderItemList.isEmpty()) {
            DeliveryOrder deliveryOrderResponse = scanAndGoValidateCart(isScanAndGo, shoppingCartJson, deliveryOrder);
            setQuantityInOrder(deliveryOrder, deliveryOrderProviderListToSave, deliveryOrderItemList, isScanAndGo);
            try {
                TalonOneService talonOneService = new TalonOneService();
                deliveryOrderResponse = talonOneService.sendOrderToTalonOne(deliveryOrderResponse, shoppingCartJson, null);
                new FarmaCredits().calculateNewPriceWithCredits(deliveryOrderResponse, (long) shoppingCartJson.getId());
            } catch (Exception e) {
                LOG.info("Error total de Talon One: " + e);
            }
            return deliveryOrderResponse;
        }

        if (!isScanAndGo) {
            if (Objects.nonNull(deliveryOrder.getItemList()) &&
                    deliveryOrder.getItemList().parallelStream().filter(item -> (Objects.nonNull(item.getScanAndGo()) && item.getScanAndGo())).findFirst().isPresent()) {
                deliveryOrder.getItemList().removeIf(item -> (Objects.nonNull(item.getScanAndGo()) && item.getScanAndGo()));
            }
        } else {
            // only show items scan and go if scan and go is selected
            if (Objects.nonNull(deliveryOrder.getItemList()) &&
                    deliveryOrder.getItemList().parallelStream()
                            .filter(item -> ((Objects.isNull(item.getScanAndGo()) || !item.getScanAndGo()) && (Objects.isNull(item.getCoupon()) || !item.getCoupon()))).findFirst().isPresent()) {
                deliveryOrderItemList.removeIf(item -> ((Objects.isNull(item.getScanAndGo()) || !item.getScanAndGo()) && (Objects.isNull(item.getCoupon()) || !item.getCoupon())));
            }
            deliveryOrder.setItemList(deliveryOrderItemList);


            deliveryOrder.getItemList().forEach(item -> {
                if (item.getTotalStock() < 1) {
                    item.setTotalStock(1000);
                }
            });
        }


        // tip delete if is full provider.
        if (!isScanAndGo && deliveryOrder.getDeliveryType().equals(DeliveryType.EXPRESS)) {

            boolean isFullProvider = isFullProvider(deliveryOrder, tipConfigOptional);

            if (isFullProvider && tipConfigOptional.isPresent()) {
                deleteTips(tipConfigOptional.get(), deliveryOrder, customerKey);
            }

        }

//        try {
//            TalonOneService talonOneService = new TalonOneService();
//            deliveryOrder = talonOneService.sendOrderToTalonOne(deliveryOrder, shoppingCartJson);
//        } catch (Exception e) {
//            LOG.info("Error total de Talon One: " + e);
//        }



        return deliveryOrder;
    }

    private void restrictItemsAlgolia(long idStoreGroup) {
//        LOG.info("getRestrictionItems");
        RestrictionItemConfig restrictionItemConfig = APIAlgolia.getRestrictionQuantityItems();

        if (idStoreGroup > 0 && restrictionItemConfig.getRestrictionItems() != null && !restrictionItemConfig.getRestrictionItems().isEmpty()) {
//            LOG.info("idStoreGroup -> " + idStoreGroup);

            restrictionItemConfig.getRestrictionItems().forEach(restrictionItem -> {
                if (restrictionItem != null
                        && restrictionItem.getItemId() != null
                        && restrictionItem.getRestrictionQuantity() != null
                        && restrictionItem.getItemId() > 0
                        && restrictionItem.getRestrictionQuantity() > 0) {
                    APIAlgolia.changeTotalStockItems((int) idStoreGroup, restrictionItem.getItemId(), restrictionItem.getRestrictionQuantity());
                }
            });
        }
    }

    private AlgoliaItem restrictItemsAlgolia(Long itemId, Integer idStoreGroup, Integer actualTotalStock) {
//        LOG.info("getRestrictionItems itemId:" + itemId + " idStoreGroup: " + idStoreGroup);
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
//                LOG.info("restrictionItemResult -> Present 2.0 actualTotalStock " + actualTotalStock);
                if (actualTotalStock != restrictionItemResult.get().getRestrictionQuantity().intValue()) {
                    List<AlgoliaItem> algoliaItemList = APIAlgolia.changeListTotalStockItems(idStoreGroup, restrictionItemResult.get().getItemId(), restrictionItemResult.get().getRestrictionQuantity());
//                    LOG.info("algoliaItemList -> algoliaItemList " + algoliaItemList);
                    return Objects.nonNull(algoliaItemList) && !algoliaItemList.isEmpty() ?
                            algoliaItemList.stream()
                                    .filter(item -> Objects.nonNull(item) && (Long.compare(itemId, Long.parseLong(item.getId())) == 0) &&
                                            Integer.compare(item.getIdStoreGroup(), idStoreGroup) == 0)
                                    .findFirst().orElse(null) :
                            null;
                } else {
                    LOG.info("restrictionItemResult -> Same Stock " + itemId.toString() + " -- " + idStoreGroup + " -- " + actualTotalStock);
                    return new AlgoliaItem(itemId.toString(), idStoreGroup, actualTotalStock);
                }
            }
        }
        return null;
    }

    private void setQuantityInOrder(DeliveryOrder deliveryOrder, List<DeliveryOrderProvider> deliveryOrderProviderListToSave, List<DeliveryOrderItem> deliveryOrderItemList, boolean isScanAndGo) {

        if (!isScanAndGo) {
            deliveryOrderItemList = deliveryOrder.getItemList();
            if (Objects.nonNull(deliveryOrderItemList) && !deliveryOrderItemList.isEmpty()) {
                deliveryOrder.setQuantityFarmatodo(deliveryOrderItemList.stream().mapToInt(DeliveryOrderItem::getQuantitySold).sum());

            }
        } else {
            deliveryOrder.setDeliveryType(DeliveryType.SCANANDGO);
            deliveryOrderItemList = deliveryOrder.getItemList();
            if (Objects.nonNull(deliveryOrderItemList) && !deliveryOrderItemList.isEmpty()) {
                deliveryOrderItemList.removeIf(it -> (Objects.nonNull(it.getScanAndGo()) && !it.getScanAndGo()));
            }
            int quantitySum = 0;

            if (Objects.nonNull(deliveryOrderItemList) && !deliveryOrderItemList.isEmpty()) {
                quantitySum = deliveryOrderItemList.stream().mapToInt(
                        DeliveryOrderItem::getQuantitySold
                ).sum();
            }
            deliveryOrder.setQuantityFarmatodo(quantitySum);
            if (deliveryOrder != null && deliveryOrder.getItemList() != null && !deliveryOrder.getItemList().isEmpty()) {
                deliveryOrder.getItemList().forEach(item -> {
                    if (item.getTotalStock() < 1) {
                        item.setTotalStock(1000);
                    }
                });
            }


        }
        if (Objects.nonNull(deliveryOrderProviderListToSave) && !deliveryOrderProviderListToSave.isEmpty()) {
            //LOG.warning(" Asigna cantidad de Items de proveedor. ");
            deliveryOrder.setQuantityProviders(deliveryOrderProviderListToSave.stream().mapToInt(DeliveryOrderProvider::getQuantityItem).sum());
        } else {
            deliveryOrder.setQuantityProviders(0);
        }
        deliveryOrder.setQuantityFarmatodo(Objects.nonNull(deliveryOrderItemList) ? deliveryOrderItemList.stream().mapToInt(item -> item.getQuantitySold()).sum() : 0);
        //deliveryOrder.setQuantityFarmatodo(deliveryOrder.getQuantityFarmatodo() > 0 ? deliveryOrder.getQuantityFarmatodo() - deliveryOrder.getQuantityProviders() : deliveryOrder.getQuantityFarmatodo());
        deliveryOrder.setTotalQuantity(deliveryOrder.getQuantityFarmatodo() + deliveryOrder.getQuantityProviders());

    }


    private void addBarcodeInShoppingCart(List<DeliveryOrderItem> deliveryOrderItemList, String idStoreGroup) {
        // add barcode Response
//        LOG.info("method: addBarcodeInShoppingCart INIT: " + new DateTime());
        if (Objects.nonNull(deliveryOrderItemList) && !deliveryOrderItemList.isEmpty()) {
            List<String> itemsId = new ArrayList<>();
            deliveryOrderItemList.forEach(itemCart -> itemsId.add(String.valueOf(itemCart.getId()).concat(idStoreGroup)));
            List<ItemAlgolia> itemsAlgoliaList = APIAlgolia.getItemsFromIds(itemsId);
            if (Stream.of(itemsAlgoliaList, deliveryOrderItemList).allMatch(Objects::nonNull) && !itemsAlgoliaList.isEmpty() && !deliveryOrderItemList.isEmpty()) {
                for (ItemAlgolia itemAlgolia : itemsAlgoliaList) {
                    for (DeliveryOrderItem deliveryOrderItem : deliveryOrderItemList) {
                        if ((Objects.isNull(deliveryOrderItem.getCoupon()) || !deliveryOrderItem.getCoupon()) &&
                                (Objects.nonNull(deliveryOrderItem) && Objects.nonNull(itemAlgolia)) &&
                                Stream.of(deliveryOrderItem, deliveryOrderItem.getId(), itemAlgolia, itemAlgolia.getId()).allMatch(Objects::nonNull)) {
                            //LOG.info("itemAlgolia -> " + itemAlgolia.getId());
                            if ((deliveryOrderItem.getId() == Long.parseLong(itemAlgolia.getId())) && (Objects.isNull(deliveryOrderItem.getCoupon()) || !deliveryOrderItem.getCoupon())) {
                                //LOG.info("barcode found -> " + itemAlgolia.getBarcode());
                                deliveryOrderItem.setBarcode(itemAlgolia.getBarcode());
                            }
                        }
                    }
                }
            }
        }
//        LOG.info("method: addBarcodeInShoppingCart END: " + new DateTime());
    }

    /**
     * remove items duplicates in shopping cart
     *
     * @param itemList
     * @param itemListProvider
     */
    private void deleteDeliveryOrderDuplicates(List<DeliveryOrderItem> itemList, List<DeliveryOrderProvider> itemListProvider) {

//        LOG.info("FIX_DS SIZE deleteDeliveryOrderDuplicates CARRITO -->" + itemListProvider.size());
//        itemListProvider.forEach(item -> {
//            LOG.info("FIX_DS --> " + item.getId());
//        });

        // items farmatodo
        if (itemList != null && !itemList.isEmpty()) {
            Set<Long> itemsAlreadySeen = new HashSet<>();
            itemList.removeIf(item -> !itemsAlreadySeen.add(item.getId()));

            Set<Long> itemsAlreadySend = new HashSet<>();
            itemList.stream().filter(item -> Objects.nonNull(item.getCoupon()) && item.getCoupon()).forEach(item -> {
                //Coupon coupon = ofy().load().type(Coupon.class).filter("itemId", item.getId()).first().now();
                itemList.removeIf(itemCoupon -> itemCoupon.getId() == item.getId() && !itemsAlreadySend.add(itemCoupon.getId()));
            });

            // items provider
        } else if (itemListProvider != null && !itemListProvider.isEmpty()) {
            Set<Long> itemsProviderAlreadySeen = new HashSet<>();

            itemListProvider.stream().findFirst()
                    .ifPresent(itemsProvider -> itemsProvider
                            .getItemList()
                            .removeIf(item -> !itemsProviderAlreadySeen.add(
                                    item.getId())));

        }

    }

    @ApiMethod(name = "orderProviderStatus", path = "/orderEndpoint/order/provider/status", httpMethod = ApiMethod.HttpMethod.POST)
    public ClientResponse orderProviderStatus(HttpServletRequest request,
                                              final OrderProviderStatus orderProviderStatus) throws BadRequestException, ConflictException,
            IOException, InternalServerErrorException, NotFoundException, UnauthorizedException {
        final String token = request.getHeader("token");
        //LOG.warning("method: providerStatusUpdate() -> params: token : " + token + " orderProviderStatus : " + orderProviderStatus);
        if (Objects.isNull(token) || token.isEmpty()) {
            LOG.warning("method: providerStatusUpdate() --> BadRequest [token is required]");
            throw new BadRequestException("BadRequest [token is required]");
        } else if (Objects.isNull(orderProviderStatus)) {
            LOG.warning("method: providerStatusUpdate() --> BadRequest [orderProviderStatus is required]");
            throw new BadRequestException("BadRequest [orderProviderStatus is required]");
        } else if (Objects.isNull(orderProviderStatus.getOrder())) {
            LOG.warning("method: providerStatusUpdate() --> BadRequest [order is required]");
            throw new BadRequestException("BadRequest [order is required]");
        } else if (Objects.isNull(orderProviderStatus.getStatus())) {
            LOG.warning("method: providerStatusUpdate() --> BadRequest [status is required]");
            throw new BadRequestException("BadRequest [status is required]");
        }

        try {
            return ApiGatewayService.get().orderProviderStatusUpdate(token, orderProviderStatus);
        } catch (IOException e) {
            LOG.warning("method: webServiceClientCreate() Error--> " + e.fillInStackTrace());
        }
        return new ClientResponse();
    }

    @ApiMethod(name = "getOrderProvider", path = "/orderEndpoint/order/provider", httpMethod = ApiMethod.HttpMethod.GET)
    public ClientResponse orderProvider(HttpServletRequest request) throws BadRequestException, ConflictException,
            IOException, InternalServerErrorException, NotFoundException, UnauthorizedException {
        final String token = request.getHeader("token");
//        LOG.info("method: orderProvider() -> params: token : " + token);
        if (Objects.isNull(token) || token.isEmpty()) {
            LOG.warning("method: orderProvider() --> BadRequest [token is required]");
            throw new BadRequestException("BadRequest [token is required]");
        }
//        LOG.info("method: orderProvider() ---> ?token=" + token);
        return ApiGatewayService.get().orderProvider(token);
    }

    @ApiMethod(name = "sendCancelOrderToCourier", path = "/orderEndpoint/order/sendCancelOrderToCourier", httpMethod = ApiMethod.HttpMethod.POST)
    public Response sendCancelOrderToCourier(final SendOrder sendOrder) throws BadRequestException, ConflictException,
            IOException, InternalServerErrorException, NotFoundException, UnauthorizedException {
        //LOG.warning("method: sendCancelOrderToCourier() -> params: token : " + sendOrder);
        if (Objects.isNull(sendOrder)) {
            LOG.warning("method: sendCancelOrderToCourier() --> BadRequest [sendOrder is required]");
            throw new BadRequestException("BadRequest [sendOrder is required]");
        } else if (Objects.isNull(sendOrder.getOrderId())) {
            LOG.warning("method: sendCancelOrderToCourier() --> BadRequest [orderId is required]");
            throw new BadRequestException("BadRequest [orderId is required]");
        }
        try {
            /*
            JSONObject orderJSON = new JSONObject();
            orderJSON.put("orderId", sendOrder.getOrderId());
            String orderStatusString = orderJSON.toString();
            LOG.warning("method: sendCancelOrderToCourier() ---> " + orderStatusString);
            return CoreConnection.putRequest(URLConnections.URL_SEND_CANCEL_ORDER_TO_COURIER, orderStatusString, SendOrderStatus.class);*/
            return ApiGatewayService.get().cancelOrderToCourier(sendOrder);
        } catch (Exception e) {
            LOG.warning("method: sendCancelOrderToCourier() Error--> " + e.fillInStackTrace());
            throw new ConflictException(e.getMessage());
        }
    }

    @ApiMethod(name = "orderCourier", path = "/orderEndpoint/order/courier", httpMethod = ApiMethod.HttpMethod.PUT)
    @Deprecated
    public OrderCourier orderCourier(final OrderEdit orderEdit) throws IOException, ServiceUnavailableException, BadRequestException {
        if (!orderEdit.isValid())
            throw new BadRequestException("BadRequest [not valid values for request or values are null]");
        // validar cupones -- customer coupon
        long idCustomer;
        DeliveryOrder deliveryOrderSaved = ofy().load().type(DeliveryOrder.class).filter("idOrder", orderEdit.getOrder_no()).first().now();
        // agregar y validar cupones de la orden
        if (deliveryOrderSaved != null) {
            final List<DeliveryOrderItem> deliveryOrderItemList = ofy().load().type(DeliveryOrderItem.class).ancestor(deliveryOrderSaved).list();
            for (DeliveryOrderItem itemSaved : deliveryOrderItemList) {
                if (itemSaved.getCoupon() != null && itemSaved.getCoupon()) {
                    Coupon coupon = ofy().load().type(Coupon.class).filter("itemId", itemSaved.getIdItem()).first().now();
                    if (orders.validateCoupon(coupon, (long) deliveryOrderSaved.getIdFarmatodo())) {
                        ArrayList<CouponRequest> coupons = new ArrayList<>();
                        coupons.add(new CouponRequest(coupon.getCouponType(), coupon.getOfferId(), false));
                        orderEdit.setCoupons(coupons);
                    }
                }
            }
        }
        OrderCourier res;
        try {
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
//            LOG.info("Request -> " + gson.toJson(orderEdit));
            res = ApiGatewayService.get().putOrderCourierEdit(orderEdit);
            if (res != null) {
                //res.setCode("200");
                //res.setMessage("La orden "+res.getUuid()+" fue modificada exitosamente.");
                return res;
            } else {
                throw new ServiceUnavailableException("No fue posible editar la orden");
            }
        } catch (Exception e) {
            LOG.info("ERROR -> " + e.getMessage());
            throw new ServiceUnavailableException("No fue posible editar la orden");
        }
    }
    /*
    @ApiMethod(
        name = "sendOrderToRMS",
        path = "/orderEndpoint/sendOrderToRMS",
        httpMethod = ApiMethod.HttpMethod.POST)
    public CoreEventResponse sendOrderToRMS(
        final SendOrder request)
        throws BadRequestException, ConflictException, InternalServerErrorException, NotFoundException, UnauthorizedException {
        LOG.warning("method: sendOrderToRMS() -> request: " + request);
        if (Objects.isNull(request)) {
            LOG.warning("method: sendOrderToRMS() --> BadRequest [request is required]");
            throw new BadRequestException("BadRequest [sendOrder is required]");
        }
        if (Objects.isNull(request.getOrderId())) {
            LOG.warning("method: sendOrderToRMS() --> BadRequest [orderId is required]");
            throw new BadRequestException("BadRequest [orderId is required]");
        }
        JSONObject requestCoreJSON = new JSONObject();
        requestCoreJSON.put("orderId", request.getOrderId());
        requestCoreJSON.put("postfix", request.getPostfix());
        requestCoreJSON.put("skipAttemptsValidation", request.isSkipAttemptsValidation());
        LOG.warning("method: sendOrderToRMS() --> CORE request: " + requestCoreJSON.toString());
        CoreEventResponse response;
        try {
            response = CoreConnection.postRequest(URLConnections.URL_SEND_ORDER_TO_RMS, requestCoreJSON.toString(), CoreEventResponse.class, "No fue posible cominicarse con el CORE");
        } catch (Exception e) {
            LOG.warning("method: sendOrderToRMS() --> Error: " + e.fillInStackTrace());
            throw new InternalServerErrorException(e.getMessage());
        }
        return processCoreEventResponse(response);
    }*/

    @ApiMethod(
            name = "sendOrderToSIM",
            path = "/orderEndpoint/sendOrderToSIM",
            httpMethod = ApiMethod.HttpMethod.POST)
    public CoreEventResponse sendOrderToSIM(
            final SendOrder request)
            throws BadRequestException, ConflictException, InternalServerErrorException, NotFoundException, UnauthorizedException {
        //LOG.warning("method: sendOrderToSIM() -> request: " + request);
        if (Objects.isNull(request)) {
            LOG.warning("method: sendOrderToSIM() --> BadRequest [request is required]");
            throw new BadRequestException("BadRequest [sendOrder is required]");
        }
        if (Objects.isNull(request.getOrderId())) {
            LOG.warning("method: sendOrderToSIM() --> BadRequest [orderId is required]");
            throw new BadRequestException("BadRequest [orderId is required]");
        }
        JSONObject requestCoreJSON = new JSONObject();
        requestCoreJSON.put("orderId", request.getOrderId());
        requestCoreJSON.put("postfix", request.getPostfix());
        requestCoreJSON.put("skipAttemptsValidation", request.isSkipAttemptsValidation());
        //LOG.warning("method: sendOrderToSIM() --> CORE request: " + requestCoreJSON.toString());
        CoreEventResponse response;
        try {
            response = CoreConnection.postRequest(URLConnections.URL_SEND_ORDER_TO_SIM, requestCoreJSON.toString(), CoreEventResponse.class, "No fue posible cominicarse con el CORE");
        } catch (Exception e) {
            LOG.warning("method: sendOrderToSIM() --> Error: " + e.fillInStackTrace());
            throw new InternalServerErrorException(e.getMessage());
        }
        return processCoreEventResponse(response);
    }


    @ApiMethod(
            name = "getOptimalRouteInCheckout",
            path = "/orderEndpoint/v2/getOptimalRouteInCheckout",
            httpMethod = ApiMethod.HttpMethod.POST)
    public OptimalRouteCheckoutResponse getOptimalRouteInCheckoutV2(
            final OptimalRouteCheckoutRequest request,
            @Named("keyClient") final String keyClient,
            HttpServletRequest servletRequest)
            throws UnauthorizedException, AlgoliaException, IOException, ForbiddenException, BadRequestException {

        final int ID_BD_REDIS_POPUP_RO = 5;
        final int TIME_TO_CACHE = 300;


        if (Objects.isNull(keyClient) || !keyClient.equals(Constants.KEY_SECURE_CLIENT)
                || request.getIdCustomerWebSafe().isEmpty())
            throw new UnauthorizedException(Constants.ERROR_ACCESS_DENIED);

        if (request.getIdCustomerWebSafe().isEmpty() || request.getIdAddress() <= 0) {
            throw new BadRequestException("BadRequest [request is required]");
        }

        Key<User> userKey = Key.create(request.getIdCustomerWebSafe());
        User user = users.findUserByKey(userKey);
        String customerId = Constants.CUSTOMER_ANONYMOUS;

        if (user != null && user.getId() >= 0){
            customerId = String.valueOf(user.getId());
        }
        request.setCustomerId(customerId);

        OptimalRouteDistance configDistance = GrowthBookConfigLoader.getDistancesOptimalRoute(customerId);

        if (!configDistance.getCacheable()){
            LOG.warning("Redis Cache is disabled for OptimalRouteCheckoutRequest");
            return processOptimalRouteRequest(request, configDistance, servletRequest);
        }

        Optional<String> optionalCacheKey = generateCacheKeyForOptimalRoutePopUp(request);

        if (optionalCacheKey.isPresent()) {
            LOG.info("getOptimalRouteInCheckoutV2 keyCache -> " + optionalCacheKey.get());
            String cacheKey = optionalCacheKey.get();
            Optional<String> cachedResponse = CachedDataManager.getJsonFromCacheIndex(cacheKey, ID_BD_REDIS_POPUP_RO);
            if (cachedResponse.isPresent()) {
                try {
                    return new Gson().fromJson(cachedResponse.get(), OptimalRouteCheckoutResponse.class);
                } catch (Exception e) {
                    LOG.warning("Error deserializando respuesta del cache: " + e.getMessage());
                }
            }
        }

        OptimalRouteCheckoutResponse response = processOptimalRouteRequest(request, configDistance, servletRequest);

        enrichResponseWithRedZoneInfo(request, response);

        if (optionalCacheKey.isPresent()) {
            try {
                String cacheKey = optionalCacheKey.get();
                CachedDataManager.saveJsonInCacheIndexTime(
                        cacheKey,
                        new Gson().toJson(response),
                        ID_BD_REDIS_POPUP_RO,
                        TIME_TO_CACHE
                );
            } catch (Exception e) {
                LOG.warning("Error guardando respuesta en cache: " + e.getMessage());
            }

        }
        return response;
    }

    private static void enrichResponseWithRedZoneInfo(OptimalRouteCheckoutRequest request, OptimalRouteCheckoutResponse response) throws IOException {
        CustomerAddressResponse customerAddressResponse;
        try {
            customerAddressResponse = ApiGatewayService.get().getCustomerByAddressId(request.getIdAddress());
        } catch (IOException e) {
            LOG.warning("Error fetching customer address for red zone validation: " + e.getMessage());
            return;
        }

        if(Objects.isNull(customerAddressResponse)
                || Objects.isNull(customerAddressResponse.getData())
                || Objects.isNull(customerAddressResponse.getData().getRedZoneId())
                || Objects.isNull(customerAddressResponse.getData().getCityId())){
            return;
        }

        String redZoneId = customerAddressResponse.getData().getRedZoneId();
        String cityId = customerAddressResponse.getData().getCityId();
        if(RedZoneService.isRedZoneActive(redZoneId, cityId)){

            response.setAddressIsRedZone(true);

            if(RedZoneService.isOutsideRedZoneHoursColombia()){
                response.setForceToSchedule(true);
            }
        }
    }

    private OptimalRouteCheckoutResponse processOptimalRouteRequest(OptimalRouteCheckoutRequest request,
                                                                    OptimalRouteDistance configDistance,
                                                                    HttpServletRequest servletRequest)
            throws UnauthorizedException, IOException, BadRequestException, AlgoliaException {


        OptimalRouteCheckoutResponse response = new OptimalRouteCheckoutResponse();

        // Valida que la funcionalidad de ruta optima en el carrito este activa
        if (!configDistance.getActive()) {
            response.getItemsToSubstitute().clear();
            response.setPosibleStoreToAssign(0);
            response.setDistance("0 kms");
            response.getToSubstitutes().clear();
            response.setOptimalRouteIsValid(false);
            response.setShowTransferOption(false);
            response.setForceToSchedule(false);
            return response;
        }
        request.setOptimalRouteDistance(configDistance);

        //validate request items requestQuantity > 0
        request.getItems();
        if (request.getItems().isEmpty()) {
            throw new BadRequestException("BadRequest [items is required]");
        }

        request.getItems().forEach(item -> {
            if (item.getRequestQuantity() <= 0) {
                try {
                    throw new BadRequestException("BadRequest [requestQuantity is required or must be greater than 0]");
                } catch (BadRequestException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        buildRequestIdAddress(request);

        if (!configDistance.getNewFlowOptimalRouteDistance())
            return getOptimalRouteCheckoutResponseV1(request, servletRequest, configDistance);

        return getOptimalRouteCheckoutResponseForPopUp(request, servletRequest, configDistance, response);
    }

    private Optional<String> generateCacheKeyForOptimalRoutePopUp(OptimalRouteCheckoutRequest request) {


        if (request == null || request.getIdCustomerWebSafe().isBlank() || request.getCustomerId().isBlank()) {
            return Optional.empty();
        }

        if (request.getIdAddress() == 0) {
            return Optional.empty();
        }

        if (request.getItems().isEmpty()) {
            return Optional.empty();
        }

        try {
            List<OptimalRouteCheckoutRequest.Item> sortedItems = new ArrayList<>(request.getItems());
            sortedItems.sort(Comparator.comparingInt(OptimalRouteCheckoutRequest.Item::getItemId));

            StringBuilder keyBuilder = new StringBuilder("optimal_route:");
            keyBuilder.append(request.getIdCustomerWebSafe())
                    .append(":")
                    .append(request.getIdAddress())
                    .append(":")
                    .append(request.getCustomerId());

            for (OptimalRouteCheckoutRequest.Item item : sortedItems) {
                keyBuilder.append(":")
                        .append(item.getItemId())
                        .append("-")
                        .append(item.getRequestQuantity());
            }

            String cacheKey = "optimal_route_" + Math.abs(keyBuilder.toString().hashCode());

            return Optional.of(cacheKey);
        } catch (Exception e) {
            LOG.warning("Error generando cache key para OptimalRoutePopUp: " + e.getMessage());
            return Optional.empty();
        }

    }

    @NotNull
    private OptimalRouteCheckoutResponse getOptimalRouteCheckoutResponseForPopUp(
            OptimalRouteCheckoutRequest request,
            HttpServletRequest servletRequest,
            OptimalRouteDistance configDistance,
            OptimalRouteCheckoutResponse response
    ) throws AlgoliaException {
        removeItemsToIgnore(request, configDistance);

        List<Integer> nearestStores = LocationMethods.getNearestStoresByAddress(
                request.getAddressLat(),
                request.getAddressLon(),
                configDistance.getSecondDistance(),
                request
        );

        int nearStore = getNearStore(nearestStores);

        if (nearStore > 0) {
            response.setNearestStore(nearStore);
        }

        boolean allStoresIsClosed = checkAllStoresClosed(nearestStores, request.getCity());
        if (allStoresIsClosed) {
            return getOptimalRouteCheckoutResponseFail(nearStore, configDistance);
        }

        List<ItemToSubstitute> itemToSubstitutesResList = new ArrayList<>();

        validateUnitPriceItems(request, nearestStores);

        final OptimalRoutePopUpResponseData optimalRoutePopUpResponseData = validateRoutePopUp(request, servletRequest, configDistance);
        LOG.info("optimalRoutePopUpResponseData.getItemsToSubstitutePercentage() -> " + optimalRoutePopUpResponseData.getItemsPercentageCompleteness().size());

        optimalRoutePopUpResponseData.getItemsPercentageCompleteness().forEach(item -> {
            if (item.getPercentage() == PERCENTAGE_MIN_FOR_NOT_SUBSTITUTE) {
                return;
            }
            ItemToSubstitute itemSubAux = getItemToReplace(request, configDistance, item.getItemId(), optimalRoutePopUpResponseData, nearestStores, item.getPercentage());
            itemToSubstitutesResList.add(itemSubAux);
        });
        response.setToSubstitutes(itemToSubstitutesResList);
        response.setItemsToSubstitute(getItemsToSubstitute(itemToSubstitutesResList));

        setShowTransferOption(response);

        final int storeToAssign = getStorePossibleToAssign(optimalRoutePopUpResponseData);

        response.setIdOptimalRoute(optimalRoutePopUpResponseData.getUuid());
        response.setOptimalRouteIsValid(true);
        response.setPosibleStoreToAssign(storeToAssign);
        response.setDistance(optimalRoutePopUpResponseData.getDistance() + " kms");
        response.setRouteHasTransfer(getOptionTransfer(optimalRoutePopUpResponseData));

        return response;
    }

    /**
     * Get the nearest store
     * @param nearestStores
     * @return the nearest store
     */
    private int getNearStore(List<Integer> nearestStores) {
        if (nearestStores == null || nearestStores.isEmpty()) {
            return 0;
        }
        return nearestStores.get(0);
    }

    private boolean checkAllStoresClosed(List<Integer> nearestStores, String city) {
        if (nearestStores == null || nearestStores.isEmpty()) {
            return false;
        }
        List<Integer> availableStores = new ArrayList<>();
        availableStores = nearestStores.parallelStream()
                .filter(store -> isStoreAvailable(String.valueOf(store), null))
                .toList();
        return availableStores.isEmpty();
    }
    private List<Integer> getItemsToSubstitute(List<ItemToSubstitute> itemToSubstitutesResList) {
        List<Integer> itemsToSubstitute = new ArrayList<>();
        itemToSubstitutesResList.forEach(item -> {
            itemsToSubstitute.add(item.getItemId());
        });
        return itemsToSubstitute;
    }

    private boolean getOptionTransfer(OptimalRoutePopUpResponseData optimalRoutePopUpResponseData) {
        if (optimalRoutePopUpResponseData.getCalculatedBy().equals("TRANSFER")) {
            return true;
        } else if (optimalRoutePopUpResponseData.getPercentageStock() < 100) {
            return true;
        }
        return false;
    }

    private int getStorePossibleToAssign(OptimalRoutePopUpResponseData optimalRoutePopUpResponseData) {
        int possibleStoreToAssign = 0;
        if (Objects.nonNull(optimalRoutePopUpResponseData.getBestStore())) {
            possibleStoreToAssign = optimalRoutePopUpResponseData.getBestStore();
        } else {
            possibleStoreToAssign = optimalRoutePopUpResponseData.getNearestStoreOpen();
        }
        return possibleStoreToAssign;
    }

    @NotNull
    private ItemToSubstitute getItemToReplace(
            OptimalRouteCheckoutRequest request, OptimalRouteDistance configDistance,
            Integer itemId, OptimalRoutePopUpResponseData optimalRoutePopUpResponseData,
            List<Integer> possibleStores, float percentage
    ) {
        int requestQuantity = getRequestQuantity(itemId, request);
        int minimumQuantity = getMinimumQuantityPopUp(percentage, itemId, requestQuantity);

        int missingQuantity = requestQuantity - minimumQuantity;
        int stockToSearch = missingQuantity == 0 ? requestQuantity : missingQuantity;
        LOG.info("item: " + itemId + " percentage: " + percentage + " requestQuantity: " + requestQuantity + " minimumQuantity: " + minimumQuantity + " missingQuantity: " + missingQuantity + " stockToSearch: " + stockToSearch);
        List<Substitutes> substitutes = new ArrayList<>();
        try {
            Optional<RecommendConfig> recommendConfig = APIAlgolia.getAlgoliaRecommendConfig();
            if (recommendConfig.isPresent() && recommendConfig.get().isOptimalRoute()) {
                substitutes = productsMethods.getSubstitutesFromRelatedProducts(
                        itemId.longValue(), optimalRoutePopUpResponseData.getNearestStoreOpen(), true, possibleStores, stockToSearch, MAX_SUBSTITUTES
                );
            }
        } catch (Exception e) {
            LOG.warning("Error al obtener la información de los items a sustituir: " + e.getMessage());
            substitutes = new ArrayList<>();
        }
        // If there are no substitutes, get the substitutes from Algolia using manual search
        if (substitutes.isEmpty()){
            substitutes.addAll(productsMethods.getItemSubstitutesFromAlgolia(
                    itemId.longValue(), stockToSearch, optimalRoutePopUpResponseData.getNearestStoreOpen(), true, configDistance.getPercentagePrice(), possibleStores
            ));
        }
        Set<Long> nameSet = new HashSet<>();
        substitutes = substitutes.stream().filter(itemList -> nameSet.add(itemList.getId())).collect(Collectors.toList());

        /**
         * Remove the item to be replaced from the list of substitutes
         */
        if (Objects.nonNull(substitutes) && !substitutes.isEmpty()) {
            substitutes.removeIf(substitute -> Objects.equals(substitute.getId(), itemId.longValue()));
        }

        ItemToSubstitute itemSubAux = new ItemToSubstitute(itemId, minimumQuantity, substitutes);
        if (minimumQuantity > 0) {
            itemSubAux.setMissingQuantity(stockToSearch);
        }
        return itemSubAux;
    }

    private static void validateUnitPriceItems(OptimalRouteCheckoutRequest request, List<Integer> possibleStores) {
        request.getItems().forEach(item -> {
            if (item.getUnitPrice() <= 0) {
                String nearbyStores = possibleStores.stream().map(String::valueOf).collect(Collectors.joining(","));
                LOG.info("item id " + item.getItemId() + " unit price " + item.getUnitPrice() + " nearbyStores " + nearbyStores);
                Optional<ItemAlgolia> optionalItemAlgolia = APIAlgolia.getItemAlgoliaRestAPI(String.valueOf(item.getItemId()), nearbyStores);
                if (optionalItemAlgolia.isPresent()) {
                    LOG.info("ItemAlgolia " + optionalItemAlgolia.get().toStringJson());
                    Double unitPrice = optionalItemAlgolia
                            .map(itemAlgolia -> itemAlgolia.getOfferPrice())
                            .filter(price -> price != null && price > 0D)
                            .orElseGet(() -> {
                                Double fullPrice = optionalItemAlgolia.get().getFullPrice();
                                return (fullPrice != null && fullPrice > 0D) ? fullPrice : 0D;
                            });
                    if(unitPrice==0D){
                        LOG.warning("ItemAlgolia unit price is 0 for item id " + item.getItemId());
                    }
                    item.setUnitPrice(unitPrice);
                }else{
                    LOG.warning("ItemAlgolia not found for item id " + item.getItemId());
                }
            }
            LOG.info("item id " + item.getItemId() + " unit price " + item.getUnitPrice());
        });
    }

    private OptimalRoutePopUpResponseData validateRoutePopUp(
            OptimalRouteCheckoutRequest request,
            HttpServletRequest servletRequest,
            OptimalRouteDistance configDistance
    ) {
        OptimalRoutePopUpRequestDomain optimalRoutePopUpRequestDomain = buildOptimalRouteRequestDomain(request);
        OptimalRoutePopUpResponseData optimalRoutePopUpResponseData = new OptimalRoutePopUpResponseData();
        String traceId = TraceUtil.getXCloudTraceId(servletRequest);
        try {
            optimalRoutePopUpResponseData = ApiGatewayService.get().getOptimalRoutePopUp(optimalRoutePopUpRequestDomain, traceId);
        } catch (Exception e) {
            LOG.warning("Error al obtener la ruta optima en el carrito: " + e.getMessage());
        }
        LOG.info("optimalRoutePopUpResponseData -> " + optimalRoutePopUpResponseData.toString());
        return optimalRoutePopUpResponseData;
    }

    private OptimalRoutePopUpRequestDomain buildOptimalRouteRequestDomain(OptimalRouteCheckoutRequest request) {
        OptimalRoutePopUpRequestDomain optimalRoutePopUpRequestDomain = new OptimalRoutePopUpRequestDomain();

        CustomerAddressLocator customerAddressLocator = new CustomerAddressLocator();
        customerAddressLocator.setCustomerAddressId(request.getIdAddress());
        customerAddressLocator.setAddress(request.getAddress());
        customerAddressLocator.setCityId(request.getCity());
        customerAddressLocator.setLatitude(request.getAddressLat());
        customerAddressLocator.setLongitude(request.getAddressLon());

        CustomerLocator customerLocator = new CustomerLocator();
        customerLocator.setCustomerId(Long.valueOf(request.getCustomerId()));
        customerLocator.setCustomerAddress(customerAddressLocator);

        ShoppingCartLocator shoppingCartLocator = new ShoppingCartLocator();
        List<ItemDomain> items = new ArrayList<>();
        request.getItems().forEach(item -> {
            ItemDomain itemDomain = new ItemDomain();
            itemDomain.setItemId(Long.valueOf(item.getItemId()));
            itemDomain.setQuantity(Long.valueOf(item.getRequestQuantity()));
            itemDomain.setUnitPrice(item.getUnitPrice());
            items.add(itemDomain);
        });
        shoppingCartLocator.setItems(items);

        optimalRoutePopUpRequestDomain.setCustomer(customerLocator);
        optimalRoutePopUpRequestDomain.setShoppingCart(shoppingCartLocator);

        LOG.info("optimalRoutePopUpRequestDomain -> " + optimalRoutePopUpRequestDomain.toString());
        return optimalRoutePopUpRequestDomain;
    }

    private int getMinimumQuantityPopUp(float percentage, Integer it, Integer requestQuantity) {

        int minimQuantity = requestQuantity;

        if (Stream.of(percentage, it, requestQuantity).allMatch(Objects::nonNull)
                && requestQuantity > 0
        ) {
            minimQuantity = (int) Math.floor(requestQuantity * (percentage / 100));
        }

        return minimQuantity;
    }

    /**
     * @deprecated Use {@link #getOptimalRouteInCheckoutV2} instead.
     * This method is scheduled for removal in Q2 2025 as part of the optimal route
     * calculation upgrade. The new implementation provides improved store assignment
     * and distance calculation logic.
     */
    @NotNull
    @Deprecated
    private OptimalRouteCheckoutResponse getOptimalRouteCheckoutResponseV1(OptimalRouteCheckoutRequest request, HttpServletRequest servletRequest, OptimalRouteDistance configDistance) throws BadRequestException, UnauthorizedException, IOException, AlgoliaException {
//        LOG.info("addres ---§ " + request);
        // llamar metodo de ruta optima..
        //OLD OptimalRouteCheckoutResponse resOptimalRoute = validateRoute(request);

        OptimalRouteCheckoutResponse resOptimalRoute = validateRouteBck3(request,
                TraceUtil.getXCloudTraceId(servletRequest),
                OptimalRouteVersionEnum.V2
        );

        return resOptimalRoute;
    }

    private void buildRequestIdAddress(OptimalRouteCheckoutRequest request) throws UnauthorizedException, IOException {
        AddressesRes addressesRes = customers.getAddressByCustomerWebSafe(
                request.getIdCustomerWebSafe()
                , request.getDeliveryType());


        for (Address address : addressesRes.getAddresses()) {
            if (request.getIdAddress() == address.getIdAddress()) {
                request.setAddressLat((float) address.getLatitude());
                request.setAddressLon((float) address.getLongitude());
                request.setCity(address.getCity());
                request.setAddress(address.getAddress());
                break;
            }

        }

    }


    private int getRequestQuantity(int itemId, OptimalRouteCheckoutRequest request) {
//        LOG.info("method  getRequestQuantity-> itemId: " + itemId);
        int quantity = 0;
        for (OptimalRouteCheckoutRequest.Item itemToSubstitute : request.getItems()) {
//            LOG.info("method  getRequestQuantity-> validate: " + itemId + " itemToSubstitute.getItemId(): " + itemToSubstitute.getItemId());
            if (itemId == itemToSubstitute.getItemId()) {
                quantity = itemToSubstitute.getRequestQuantity();
//                LOG.info("method  getRequestQuantity-> itemId: " + itemId + " quantity: " + quantity);
            }
        }
        return quantity;
    }

    /**
     * @deprecated Internal implementation used by legacy optimal route calculation.
     * Will be removed along with {@link #getOptimalRouteCheckoutResponseV1} in Q2 2025.
     * New implementations should use {@link #getOptimalRouteInCheckoutV2}.
     */
    @SuppressWarnings("Duplicates")
    @Deprecated
    private OptimalRouteCheckoutResponse validateRouteBck3(
            OptimalRouteCheckoutRequest request,
            String traceId,
            OptimalRouteVersionEnum optimalRouteVersionEnum
    )
            throws AlgoliaException, IOException, BadRequestException {

//        LOG.info("method validateRouteBck3");

        OptimalRouteDistance optimalRouteConfig = request.getOptimalRouteDistance();

        if (optimalRouteConfig == null) {
            throw new BadRequestException("BadRequest [optimalRouteConfig is required]");
        }

//        LOG.info("distances to validate -> " + distances.toString() + "enable -> " + distances.getActive());

        // remove items from request if exist in optimalRouteConfig.getItemsToIgnore()
        removeItemsToIgnore(request, optimalRouteConfig);


        OptimalRouteCheckoutResponse response = new OptimalRouteCheckoutResponse();

        setShowTransferOption(request, optimalRouteConfig, response);

        int storeAsigne = orderMethods.getStoreAssigned(optimalRouteConfig, request);
//        LOG.info("Store asigned -> " + storeAsigne);
        boolean allStoresIsClosed = orderMethods.allStoreIsClosed(optimalRouteConfig, request);

//        LOG.info("Todas las tiendas estan cerradas? -> " + allStoresIsClosed);


        if (allStoresIsClosed) {
            return getOptimalRouteCheckoutResponseFail(storeAsigne, optimalRouteConfig);
        }


        List<Integer> possibleStores = getPossibleStoreByDistancesConfig(request, optimalRouteConfig);

        // create Request to oms
        OptimalRouteCheckoutOmsReq routeCheckoutOmsReq = new OptimalRouteCheckoutOmsReq();
        //LOG.info("SIZE STORES1 -> " + possibleStores.size());

        routeCheckoutOmsReq.setCity(request.getCity());
        routeCheckoutOmsReq.setPossibleStores(possibleStores);

        List<OptimalRouteCheckoutOmsReq.shoppingCartObj> shoppingCartList = new ArrayList<>();
        request.getItems().forEach(cart -> {
            OptimalRouteCheckoutOmsReq.shoppingCartObj shoppingCartItem = new OptimalRouteCheckoutOmsReq.shoppingCartObj();
            shoppingCartItem.setItem(cart.getItemId());
            shoppingCartItem.setRequestQuantity(cart.getRequestQuantity());
            shoppingCartList.add(shoppingCartItem);
        });

        routeCheckoutOmsReq.setShoppingCart(shoppingCartList);

        //LOG.info("SIZE STORES2 -> " + (Objects.nonNull(routeCheckoutOmsReq.getPossibleStores()) ? routeCheckoutOmsReq.getPossibleStores().size() : 0));

        // call optimalRoute in checkout OMS

        OptimalRouteCheckoutOmsRes routeCheckoutOmsRes = ApiGatewayService.get().getOptimalRouteInCheckoutOms(routeCheckoutOmsReq, traceId);


        int possibleStore = routeCheckoutOmsRes != null && routeCheckoutOmsRes.getData() != null && routeCheckoutOmsRes.getData().getPossibleStoreToAssing() != null
                ? routeCheckoutOmsRes.getData().getPossibleStoreToAssing() : 0;

        response.setOptimalRouteIsValid(true);
        response.setPosibleStoreToAssign(possibleStore);
        response.setDistance(LocationMethods
                .getDistanceAddressToStore(request.getAddressLat(), request.getAddressLon(), possibleStore) + " Kms");
        response.setRouteHasTransfer(Objects.nonNull(routeCheckoutOmsRes) && Objects.nonNull(routeCheckoutOmsRes.getData()) ? routeCheckoutOmsRes.getData().getRouteHasTransfer() : false);
        response.setToSubstitutes(new ArrayList<>());
        response.setItemsToSubstitute(new ArrayList<>());
        //response = null;
        LOG.info("method validateRouteBck3 setDistance : " + response.getDistance());

        if (possibleStore > 0) {
            //obtener items que se deben sustituir o eliminar cantidades.
            List<Integer> itemsToSubstitute = getItemsToSubstituteV2(routeCheckoutOmsRes.getData().getItemsPercentage());

            response.setItemsToSubstitute(itemsToSubstitute);

            // traer items a sustituir..


//            LOG.info("itemsToSubstitute -> " + itemsToSubstitute.size());

            ProductsMethods productsMethods = new ProductsMethods();

            // llenar response con los sutitutos
            List<ItemToSubstitute> itemToSubstitutesResList = new ArrayList<>();


            if (!itemsToSubstitute.isEmpty()) {
                int finalPossibleStore = response.getPosibleStoreToAssign();

                int limit = 10;

                possibleStores.forEach(store -> LOG.info("posibleStoreToValidateStock -> " + store));

                List<Integer> finalPossibleStores = possibleStores;
                itemsToSubstitute.forEach(it -> {
                    int requestQuantity = getRequestQuantity(it, request);
//                    LOG.info("ItemToSubstitute: " + it + " finalPossibleStore: " + finalPossibleStore + " requestQuantity: " + requestQuantity);


                    int minimumQuantity = getMinimumQuantity(routeCheckoutOmsRes.getData().getItemsPercentage(), it, requestQuantity);

                    // limit 10 items to response
                    if (itemToSubstitutesResList.size() < limit) {
                        int missingQuantity = requestQuantity - minimumQuantity;
                        int stockToSearch = missingQuantity == 0 ? requestQuantity : missingQuantity;
                        List<Substitutes> substitutes = new ArrayList<>();
                        try {
                            Optional<RecommendConfig> recommendConfig = APIAlgolia.getAlgoliaRecommendConfig();
//                            LOG.info("Recommend optimalRoute->" + (recommendConfig.isPresent() && recommendConfig.get().isOptimalRoute()));
                            if (recommendConfig.isPresent() && recommendConfig.get().isOptimalRoute()) {
                                substitutes = productsMethods.getSubstitutesFromRelatedProducts(it.longValue(), finalPossibleStore, true, finalPossibleStores, stockToSearch, 10);
                            }
                        } catch (Exception e) {
//                            e.printStackTrace();
                            substitutes = new ArrayList<>();
                        }
                        substitutes.addAll(productsMethods.getItemSubstitutesFromAlgolia(it.longValue(), stockToSearch, finalPossibleStore, true, optimalRouteConfig.getPercentagePrice(), finalPossibleStores));
                        Set<Long> nameSet = new HashSet<>();
                        substitutes = substitutes.stream().filter(itemList -> nameSet.add(itemList.getId())).collect(Collectors.toList());
//                        LOG.info("substitutes! -> " + substitutes.size());


                        ItemToSubstitute itemSubAux = new ItemToSubstitute(it, minimumQuantity, substitutes);

                        if (minimumQuantity > 0) {
                            itemSubAux.setMissingQuantity(stockToSearch);
                        }
                        itemToSubstitutesResList.add(itemSubAux);
                    }


                });

            }

//            LOG.info("a sustituir! -> " + itemToSubstitutesResList.size());
            response.setToSubstitutes(itemToSubstitutesResList);

        }

        // if don't exists substitutes for all items to substitute, then showTransferOption = false

        if (optimalRouteVersionEnum.equals(OptimalRouteVersionEnum.V2)) {
            setShowTransferOption(response);
        }

        return response;

    }

    private void setShowTransferOption(OptimalRouteCheckoutResponse response) {
        boolean someItemIsNotAvailable = someItemIsNotAvailable(response.getToSubstitutes());

        response.setShowTransferOption(someItemIsNotAvailable);
    }

    @NotNull
    private static List<Integer> getPossibleStoreByDistancesConfig(OptimalRouteCheckoutRequest request, OptimalRouteDistance optimalRouteConfig) throws AlgoliaException {
        return LocationMethods.getNearestStoresByAddress(
                    request.getAddressLat(),
                    request.getAddressLon(),
                    optimalRouteConfig.getSecondDistance(), request);
    }

    private void setShowTransferOption(OptimalRouteCheckoutRequest request, OptimalRouteDistance optimalRouteConfig, OptimalRouteCheckoutResponse response) {
        boolean showTransferOption = orderMethods.showTransferOption(optimalRouteConfig.getDistancePopUp(), request);

        response.setShowTransferOption(showTransferOption);
    }

    private static void removeItemsToIgnore(OptimalRouteCheckoutRequest request, OptimalRouteDistance optimalRouteConfig) {
        List<String> idItemsToIgnore = optimalRouteConfig.getItemsToIgnore();
        if (idItemsToIgnore != null && !idItemsToIgnore.isEmpty()) {
            request.getItems().removeIf(itemAux -> {
                if (idItemsToIgnore.contains(String.valueOf(itemAux.getItemId()))) {
                    return true;
                }
                return false;
            });
        }
    }

    @NotNull
    private OptimalRouteCheckoutResponse getOptimalRouteCheckoutResponseFail(int storeAsigne, OptimalRouteDistance optimalRouteConfig) {
        OptimalRouteCheckoutResponse responseFail = new OptimalRouteCheckoutResponse();
        responseFail.getItemsToSubstitute().clear();
        responseFail.setPosibleStoreToAssign(0);
        responseFail.setDistance("0 kms");
        responseFail.getToSubstitutes().clear();
        responseFail.setOptimalRouteIsValid(false);
        responseFail.setForceToSchedule(true);
        responseFail.setShowTransferOption(false);

        if (storeAsigne != 0) {
//                LOG.info("Hora de apertura -> " + orderMethods.getHourStoreConfig(String.valueOf(storeAsigne)));
            optimalRouteConfig.setHourToNextSchedule(orderMethods.getHourStoreConfig(String.valueOf(storeAsigne)));
        }

        if (optimalRouteConfig.getHourToNextSchedule() != null && optimalRouteConfig.getHourToNextSchedule() > 0) {

//                LOG.info("Hora de apertura -> " + responseFail.getDateFirstSchedule());
            responseFail.setDateFirstSchedule(
                    orderMethods.getNextDateToSchedule(optimalRouteConfig.getHourToNextSchedule()));

//                LOG.info("Hora de apertura -> " + responseFail.getDateFirstSchedule());
        }

        return responseFail;
    }

    @NotNull
    private static List<Integer> getPossibleStores(OptimalRouteCheckoutRequest request, Float distanceToValidate) throws AlgoliaException {
        List<Integer> possibleStores = LocationMethods.getNearestStoresByAddress(
                request.getAddressLat(),
                request.getAddressLon(),
                distanceToValidate, request);
        return possibleStores;
    }

    /**
     * Si alguno item no esta disponible, (missingQuantity <= 0)
     * si algun item <= 0 return false ==> entonces no se muestra la opcion de transferencia
     * si todos los items > 0 return true ==> entonces se muestra la opcion de transferencia
     */
    private Boolean someItemIsNotAvailable(List<ItemToSubstitute> toSubstitutes) {
        for (ItemToSubstitute item : toSubstitutes) {
            if (item != null && item.getMinQuantity() != null && item.getMinQuantity() <= 0) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("ConstantConditions")
    private int getMinimumQuantity(List<OptimalRouteCheckoutOmsRes.Data.ItemPercentageObj> itemsPercentage, Integer it, Integer requestQuantity) {

        int minimQuantity = requestQuantity;

        if (Stream.of(itemsPercentage, it, requestQuantity).allMatch(Objects::nonNull)
                && !itemsPercentage.isEmpty() && requestQuantity > 0
        ) {

            Optional<OptimalRouteCheckoutOmsRes.Data.ItemPercentageObj> percentageAux =
                    itemsPercentage.stream().filter(itemPercentageObj -> Objects
                                    .equals(itemPercentageObj.getItem(), it))
                            .findFirst();

            if (percentageAux.isPresent()) {

                float percentageAuxValue = 0;
                if (Stream.of(percentageAux.get().getItem(), percentageAux.get().getPercentage()).anyMatch(Objects::nonNull)) {
                    percentageAuxValue = (percentageAux.get().getPercentage() != null) ? percentageAux.get().getPercentage() : 0;
                }


                minimQuantity = (int) Math.floor(requestQuantity * (percentageAuxValue / 100));
            }

        }

        return minimQuantity;

    }


/*    private OptimalRouteCheckoutResponse validateRoute(OptimalRouteCheckoutRequest request) throws IOException, AlgoliaException, UnauthorizedException, ForbiddenException {

        OptimalRouteDistance distances = APIAlgolia.getDistancesOptimalRoute();
        OptimalRouteCheckoutResponse routeCheckoutResponse = new OptimalRouteCheckoutResponse();

        LOG.info("distances to validate -> " + distances.toString() + "enable -> " + distances.getActive());


        List<Integer> possibleStores = LocationMethods.getNearestStoresByAddress(
                request.getAddressLat(),
                request.getAddressLon(),
                request.getCity(),
                distances.getFirstDistance());
        // if the distance is null proceed to validate with the second distance max distance possibles stores nearby customer address
        if (possibleStores.isEmpty()) {
            possibleStores = LocationMethods.getNearestStoresByAddress(
                    request.getAddressLat(),
                    request.getAddressLon(),
                    request.getCity(),
                    distances.getSecondDistance());
        }

        LOG.info("Request Optimal Route -> " + request.toString());

        if (!request.requestIsValid())
            throw new UnauthorizedException(Constants.ERROR_BAD_REQUEST);

        //build request to validate stock CORE.

        ValidateStockRouteReq requestValidateStock = new ValidateStockRouteReq();

        requestValidateStock.setStores(possibleStores);

        // getItems To Validate Stock
        List<ValidateStockRouteReq.Item> itemsToValidateStock = getItemsToValidateStock(request);
        // set items to validate stock
        requestValidateStock.setItems(itemsToValidateStock);

        // call Core
        ValidateStockRouteRes resCore = CoreService.get().validateStock(requestValidateStock);

        boolean firstDistanceIsValid = false;
        boolean secondDistanceIsValid = false;


        // First Radio Distance is not Valid!
        if (validateAllStoresInRadio(resCore)) {
            firstDistanceIsValid = true;

        } else {
            LOG.info("No se encontro Tienda para el primer radio de: -> "
                    + distances.getFirstDistance() +
                    "Se procede a validar a un radio de : -> " +
                    distances.getSecondDistance());
            //stores in second radio
            possibleStores = LocationMethods.getNearestStoresByAddress(
                    request.getAddressLat(),
                    request.getAddressLon(),
                    request.getCity(),
                    distances.getSecondDistance());

            requestValidateStock.setStores(possibleStores);

            // getItems To Validate Stock
            itemsToValidateStock = getItemsToValidateStock(request);
            // set items to validate stock
            requestValidateStock.setItems(itemsToValidateStock);

            // call Core
            resCore = CoreService.get().validateStock(requestValidateStock);
            if (Objects.isNull(resCore)) {
                throw new ForbiddenException(Constants.ROUTE_NOT_FOUND);
            }

            secondDistanceIsValid = validateAllStoresInRadio(resCore);
        }

        if (Objects.isNull(resCore)) {
            routeCheckoutResponse.setOptimalRouteIsValid(false);
            routeCheckoutResponse.setRouteHasTransfer(false);
            return routeCheckoutResponse;
            //throw new ForbiddenException(Constants.ROUTE_NOT_FOUND);
        }

        int possibleStoreToAssign = getFirstStoreValid(resCore);
        routeCheckoutResponse.setPosibleStoreToAssign(possibleStoreToAssign);
        LOG.info("Possible store to asing -> " + possibleStoreToAssign);
        routeCheckoutResponse.setRouteHasTransfer(false);


        if (firstDistanceIsValid) {
            // Es valida en el primer radio crear response
            LOG.info("Se encontro tienda a un radio de " + distances.getFirstDistance() + "KMS");
            routeCheckoutResponse.setOptimalRouteIsValid(true);
            routeCheckoutResponse.setDistance(LocationMethods
                    .getDistanceAddressToStore(request.getAddressLat(), request.getAddressLon(), possibleStoreToAssign) + " Kms");
            routeCheckoutResponse.setItemsToSubstitute(null);


        } else if (secondDistanceIsValid) {
            // No es valido en el primer radio pero en el seguno si lo es
            LOG.info("Se encontro tienda a un radio de " + distances.getSecondDistance() + "KMS");
            routeCheckoutResponse.setOptimalRouteIsValid(false);
            routeCheckoutResponse.setDistance(LocationMethods
                    .getDistanceAddressToStore(request.getAddressLat(), request.getAddressLon(), possibleStoreToAssign) + " Kms");
            routeCheckoutResponse.setItemsToSubstitute(getItemsToSubstitute(possibleStores, resCore));
        } else {
            // Se va por transeferencia:
            LOG.info("No se encontro una posible tienda y el pedido se ira por transferencia");
            routeCheckoutResponse.setOptimalRouteIsValid(false);
            routeCheckoutResponse.setPosibleStoreToAssign(null);
            routeCheckoutResponse.setDistance(null);
            routeCheckoutResponse.setRouteHasTransfer(true);
            routeCheckoutResponse.setItemsToSubstitute(getItemsToSubstitute(possibleStores, resCore));
        }
        return routeCheckoutResponse;
    }*/


    /**
     * get items with < 100% stock, by sustitute or delete quantity
     *
     * @param itemsPercentage
     * @return
     */

    private List<Integer> getItemsToSubstituteV2(List<OptimalRouteCheckoutOmsRes.Data.ItemPercentageObj> itemsPercentage) {

        List<Integer> itemsToSubstitute = new ArrayList<>();

        // algolia percent validate

        OptimalRouteCheckoutConfig checkoutConfig = APIAlgolia.getOptimalRouteCheckoutConfig();

        if (Objects.nonNull(checkoutConfig)
                && Objects.nonNull(checkoutConfig.getMinPercentItem())
                && itemsPercentage != null
                && !itemsPercentage.isEmpty()) {

//            LOG.info("method getItemsToSubstituteV2() Config Algolia ->  " + checkoutConfig.toString());
            itemsPercentage.forEach(item -> {
                if (item.getPercentage() != null && item.getPercentage() < checkoutConfig.getMinPercentItem()) {
                    itemsToSubstitute.add(item.getItem());
                }

            });
        }
        return itemsToSubstitute;
    }

//    private List<Integer> getItemsToSubstitute(final List<Integer> possibleStores, final ValidateStockRouteRes resCore) {
//        LOG.info("method: getItemsToSubstitute AnyWaySelling");
//        List<Integer> itemsIsNotValid = new ArrayList<>();
//        Integer idStoreGroup = Objects.nonNull(possibleStores) && !possibleStores.isEmpty() ? possibleStores.get(0) : 0;
//        List<ItemQuery> listItemQuery = Objects.requireNonNull(Objects.requireNonNull(resCore.getResult()).get(0).getItems()).stream()
//                .map(item -> new ItemQuery("" + item.getId() + (idStoreGroup > 0 ? idStoreGroup : URLConnections.MAIN_ID_STORE))).collect(Collectors.toList());
//        List<ItemAlgolia> itemRequestList = APIAlgolia.findItemByIdList(listItemQuery);
//
//        Optional<ItemAlgolia> itemAnyWaySelling =  Objects.nonNull(itemRequestList) && !itemRequestList.isEmpty() ?
//                itemRequestList.stream()
//                        .filter(itemAlgolia -> Objects.nonNull(itemAlgolia.isAnywaySelling()) &&itemAlgolia.isAnywaySelling()).findFirst(): null;
//
//        if(Objects.nonNull(itemAnyWaySelling) && itemAnyWaySelling.isPresent()) {
//            LOG.info("method: getItemsToSubstitute AnyWaySelling Contiene itemAnyWaySelling ");
//            Objects.requireNonNull(Objects.requireNonNull(resCore.getResult()).get(0).getItems()).forEach(item -> {
//                if (!item.isValid()) {
//                    LOG.info("method: getItemsToSubstitute AnyWaySelling Items Not valid: " + item.getId());
//                    itemsIsNotValid.add(item.getId());
//                }
//            });
//        }
//        return itemsIsNotValid;
//    }


//    private Integer getFirstStoreValid(ValidateStockRouteRes resCore) {
//
//        for (ValidateStockRouteRes.Result store : Objects.requireNonNull(resCore.getResult())) {
//            if (store.isValid()) {
//                return store.getStore();
//            }
//        }
//        return 0;
//    }

    private List<ValidateStockRouteReq.Item> getItemsToValidateStock(OptimalRouteCheckoutRequest request) {

        List<ValidateStockRouteReq.Item> itemsToValidateStock = new ArrayList<>();

        request.getItems().forEach(item -> {
            ValidateStockRouteReq.Item itemAux = new ValidateStockRouteReq().new Item();

            itemAux.setItemId((long) item.getItemId());
            itemAux.setRequestQuantity(item.getRequestQuantity());
            itemsToValidateStock.add(itemAux);

        });

        return itemsToValidateStock;
    }

//    private boolean validateAllStoresInRadio(ValidateStockRouteRes stockRouteRes) {
//        if (stockRouteRes != null && stockRouteRes.getResult() != null) {
//            int sizeStore = stockRouteRes.getResult().size();
//            int countStoresNotValid = (int) stockRouteRes.getResult().stream().filter(validStore -> !validStore.isValid()).count();
//            if (sizeStore == countStoresNotValid) {
//                return false;
//            }
//        }
//        return true;
//    }

    @NotNull
    private CoreEventResponse processCoreEventResponse(CoreEventResponse response)
            throws BadRequestException, ConflictException, InternalServerErrorException, NotFoundException, UnauthorizedException {
        if (Objects.isNull(response) || Objects.isNull(response.getCode())) {
            throw new InternalServerErrorException("Error general.");
        }

        switch (response.getCode()) {
            case OK:
                return response;
            case BAD_REQUEST:
                throw new BadRequestException(response.getMessage());
            case BUSSINESS_ERROR:
                throw new ConflictException(response.getMessage());
            case APPLICATION_ERROR:
                throw new InternalServerErrorException(response.getMessage());
            case NO_CONTENT:
                throw new NotFoundException(response.getMessage());
            case UNAUTHORIZED:
                throw new UnauthorizedException(response.getMessage());
            default:
                throw new InternalServerErrorException(response.getMessage());
        }
    }

    private Item setOfferPriceSubscribeAndSave(Item item, final int customerID) {
        //Obtener el cliente:


        //TODO: consultar descuento del cliente.

        return item;
    }


    private Key<LogIntentPayment> saveLogIntentPayment(Long orderId, String uuid, String statusCode, String message) {
        LogIntentPayment newIntentPayment =
                new LogIntentPayment(orderId, uuid, LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), statusCode, message);
        return ofy().save().entity(newIntentPayment).now();
    }


    @ApiMethod(name = "cancelOrder", path = "/orderEndpoint/cancelOrder", httpMethod = ApiMethod.HttpMethod.POST)
    public com.imaginamos.farmatodo.backend.order.OrderFinalizeRes cancelOrderCourier(final com.imaginamos.farmatodo.backend.order.OrderFinalize orderFinalize) throws UnauthorizedException {

//        LOG.info(orderFinalize.toString());
        if (!orderFinalize.getKeyClient().equals(Constants.KEY_SECURE_CLIENT))
            throw new UnauthorizedException(Constants.ERROR_ACCESS_DENIED);

        com.imaginamos.farmatodo.backend.order.OrderFinalizeRes orderFinalizeRes = null;

        try {
            List<DeliveryOrder> deliveryOrderSavedList = ofy().load().type(DeliveryOrder.class).filter("idOrder =", orderFinalize.getOrderId()).list();

            for (DeliveryOrder deliveryOrderSaved : deliveryOrderSavedList) {
                orderFinalizeRes = new com.imaginamos.farmatodo.backend.order.OrderFinalizeRes();

//                LOG.info("order to finish -> " + deliveryOrderSaved.toString());
                deliveryOrderSaved.setLastStatus(ORDER_CANCELED);
                deliveryOrderSaved.setActive(false);
                ofy().save().entity(deliveryOrderSaved).now();

                orderFinalizeRes.setMessage(deliveryOrderSaved.toString());
                List<Long> orders = new ArrayList<>();
                orders.add(deliveryOrderSaved.getIdOrder());
                orderFinalizeRes.setMessage("Se finalizo la orden -> " + orderFinalizeRes.toString());
                orderFinalizeRes.setOrders(orders);
            }

        } catch (Exception e) {
//            LOG.info("Error finalizeOrder -> " + e.getMessage());
            orderFinalizeRes.setMessage("Error al intentar finalizar la orden");
        }


        return orderFinalizeRes;
    }

    /**
     * Elimniar ultimo cupon usado por el cliente.
     */
    private boolean deleteCoupon(final Key<Customer> customerKey) {
        try {
            final List<CustomerCoupon> customerCoupons = ofy().load().type(CustomerCoupon.class).filter("customerKey", customerKey).orderKey(false).list();
            if (customerCoupons != null && !customerCoupons.isEmpty()) {
                customerCoupons.sort(Comparator.comparing(a -> a.getUseTime()));
                final int positionLastCupon = customerCoupons.size() - 1;
                final CustomerCoupon couponToDelete = customerCoupons.get(positionLastCupon);
                if (couponToDelete != null) {
                    ofy().delete().entity(couponToDelete).now();
                    try {
                        Coupon coupon = couponToDelete.getCouponId().get();
                        subtractCountUses(coupon);
                    } catch (Exception e) {
                        LOG.info("Ocurrio un error trayendo el cupon " + e.getMessage());
                    }
                    return true;
                }
                return false;
            }
            return false;
        } catch (Exception e) {
            LOG.warning("Error al eliminar cupon cuando el cliente elimina todo el carrito... Mensaje: " + e.getMessage());
            return false;
        }
    }

    private boolean verifyHighDemand(DeliveryOrder order, int storeId) {
//        LOG.info("VALIDATE HIGH DEMAND: storeId: " + storeId);
        CartDeliveryLabelConfig cartDeliveryLabelConfig =
                APIAlgolia.getCartDeliveryLabelConfig(storeId);
        if (Objects.isNull(cartDeliveryLabelConfig) || Objects.isNull(cartDeliveryLabelConfig.getValues()))
            return false;
        CartDeliveryLabelConfigValue configValue = cartDeliveryLabelConfig.getValues().get(0);
//        LOG.info("VALIDATE HIGH DEMAND: storeConfigurationOptional: " + configValue);

        if (!Objects.isNull(configValue) && !Objects.isNull(configValue.getDateUntil())
                && !Objects.isNull(configValue.getDateFrom()) && !Objects.isNull(configValue.getScheduleDuration())) {
            DateTime nowDate = new DateTime(System.currentTimeMillis(), DateTimeZone.forID("America/Bogota"));
//            LOG.info("VALIDATE HIGH DEMAND: storeConfiguration: " + configValue);

            if (nowDate.getMillis() < configValue.getDateUntil() &&
                    nowDate.getMillis() > configValue.getDateFrom()) {
                DateTime startProgramDate = new DateTime(configValue.getDateUntil(), DateTimeZone.forID("America/Bogota"));
                Long timeToProgram = startProgramDate.plusHours(configValue.getScheduleOrderStart()).getMillis();
                DateTime untilProgramDate = new DateTime(timeToProgram, DateTimeZone.forID("America/Bogota"));
                Long timeUntilProgram = untilProgramDate.plusHours(configValue.getScheduleDuration()).getMillis();
                DateTime pickingDate = new DateTime(order.getPickingDate());
                pickingDate = pickingDate.plusHours(5);// SUM 5 hours for UTC to colombia hour
//                LOG.info("VALIDATE HIGH DEMAND: pickingDate: " + pickingDate.getMillis()
//                        + " timeUntil: " + timeUntilProgram + " timeFrom " + timeToProgram);
                if (pickingDate.getMillis() <= timeUntilProgram && pickingDate.getMillis() >= timeToProgram) {
                    LOG.info("VALIDATE HIGH DEMAND: TRUE");
                    return true;
                }
            }
        }
        LOG.info("VALIDATE HIGH DEMAND: FALSE");
        return false;
    }

    private void setCufPerHighDemand(List<DeliveryOrderItem> deliveryOrderItemList, int storeId) {
        try {
//            LOG.info("setCufPerHighDemand(" + storeId + ")");
            final CartDeliveryLabelConfig cartDeliveryLabelConfig = APIAlgolia.getCartDeliveryLabelConfig(storeId);
            if (cartDeliveryLabelConfig != null && !cartDeliveryLabelConfig.getValues().isEmpty()) {
                DeliveryOrderItem deliveryOrderItem = new DeliveryOrderItem();
                deliveryOrderItem.setQuantitySold(1);
                deliveryOrderItem.setCoupon(true);

                if (!cartDeliveryLabelConfig.getValues().isEmpty() &&
                        cartDeliveryLabelConfig.getValues().get(0) != null &&
                        cartDeliveryLabelConfig.getValues().get(0).getCouponName() != null &&
                        !cartDeliveryLabelConfig.getValues().get(0).getCouponName().isEmpty()) {
                    String coupon = cartDeliveryLabelConfig.getValues().get(0).getCouponName();
                    Query.Filter filterName = new Query.FilterPredicate("name",
                            Query.FilterOperator.EQUAL, coupon.toUpperCase().trim());
                    com.googlecode.objectify.cmd.Query<Coupon> queryCoupon = ofy().load().type(Coupon.class).filter(filterName);
                    Coupon couponSaved = queryCoupon.first().now();
                    deliveryOrderItem.setIdItem(couponSaved.getItemId());
                    deliveryOrderItemList.add(deliveryOrderItem);
                }
            }
        } catch (Exception e) {
            LOG.warning("Error al setear el cuf de cupon a la orden por alta demanda. Mensaje: " + e.getMessage());
        }
    }


    @ApiMethod(name = "etaUnitTest", path = "/orderEndpoint/etaUnitTest", httpMethod = ApiMethod.HttpMethod.GET)
    public Object etaUnitTest(@Named("idOrder") final String idOrder) throws IOException, ConflictException, BadRequestException {

        if (Objects.isNull(idOrder))
            throw new ConflictException("idOrder required");

        OrderInfoStatus orderInfoStatus = new OrderInfoStatus();

        final boolean hasTransfer = true;

        Map<String, String> eta = getEstimatedTimeArrival(Long.parseLong(idOrder), hasTransfer);

        orderInfoStatus.setETALongTime(String.valueOf(eta.get(ETA_IN_LONG_TIMESTAMP)));
        orderInfoStatus.setETAMinutes(String.valueOf(eta.get(ETA_IN_MINUTES))); // Plantear resta now-etaMinutes

        return orderInfoStatus;

    }

    /**
     * Otener ETA.
     *
     * @param idOrder
     */
    private Map<String, String> getEstimatedTimeArrival(final Long idOrder, final boolean hasTransfer) {
//        LOG.info("getEstimatedTimeArrival(" + idOrder + ")");

        Integer eta = 0;
        long etaTimeStamp = 0;
        Map<String, String> etaResults = new HashMap<>();

        if (idOrder != null && idOrder > 0) {
            final DeliveryOrder order = ofy().load().type(DeliveryOrder.class).filter("idOrder", idOrder).first().now();

            if (order != null) {
                Date creationDate = order.getCreateDate();
                final int idAddres = order.getIdAddress();
                String cityId = "BOG"; //Default.

                if (Objects.nonNull(order.getPickingDate())) {
                    creationDate = order.getPickingDate();
                }

                CustomerAddressResponse customerAddressResponse;
                try {
                    customerAddressResponse = ApiGatewayService.get().getCustomerByAddressId(Long.parseLong(String.valueOf(idAddres)));
                    if (customerAddressResponse != null && customerAddressResponse.getData() != null && customerAddressResponse.getData().getCityId() != null) {
                        cityId = customerAddressResponse.getData().getCityId();
//                        LOG.info("cityId_from_address=>" + cityId);
                    }
                } catch (Exception e) {
                }

                if (creationDate != null) {
                    final Integer numberOfStops = getNumberOfStopsForThisOrder(idOrder);
                    final int currentOrderStatus = order.getCurrentStatus();
                    Optional<ETAConfig> etaConfig = Optional.empty();
                    try {
                        etaConfig = APIAlgolia.getETAConfig();
                    } catch (Exception e) {
                        LOG.warning("Error@getEstimatedTimeArrival -> " + e.getMessage());
                    }
//                    LOG.info("idOrder:" + idOrder + ", ");
                    eta = ETAUtil.getTimeToArrive(idOrder, cityId, hasTransfer, creationDate, currentOrderStatus, etaConfig, numberOfStops);
                    final long timeCreateDate = creationDate.getTime();
                    etaTimeStamp = timeCreateDate + (eta * 60000); //Sumar en Milis.
                }
            }
        }

        etaResults.put(ETA_IN_MINUTES, String.valueOf(eta));
        etaResults.put(ETA_IN_LONG_TIMESTAMP, String.valueOf(etaTimeStamp));

        return etaResults;
    }

    /**
     * Obtener el numero de paradas de una orden.
     *
     * @param orderId
     */
    private Integer getNumberOfStopsForThisOrder(final Long orderId) {
        try {
            Response<GetOrderStopsResponse> response = ApiGatewayService.get().getOrderStops(orderId);
            if (response.isSuccessful())
                return response.body().getData().getStops();
        } catch (Exception e) {
            return 1;
        }
        return 1;
    }

    @ApiMethod(name = "deliveryOrdersScanAndGo", path = "/orderEndpoint/deliveryOrdersScanAndGo", httpMethod = ApiMethod.HttpMethod.POST)
    public ScanAndGoResponse deliveryOrdersScanAndGo(final ScanAndGoRequest request) throws ConflictException, IOException {
        ScanAndGoResponse response = new ScanAndGoResponse();
        if (Objects.isNull(request))
            throw new ConflictException(Constants.USER_NOT_FOUND);

        if (request.getCustomerIds().isEmpty()) {
            response.setResponseCode("BadRequest");
            response.setData(new ArrayList<>());
            return response;
        } else {
            // ofy().load().type(UserPass.class).filter("active", true).ancestor(userKey).first().now();

            response.setResponseCode("Success");

            // set deliveryOrder from users
            try {
                List<CustomerInfo> customerInfoList = getCustomerInfoList(request.getCustomerIds());

                buildDeliveryOrders(customerInfoList);
                // set data
                response.setData(customerInfoList);

            } catch (Exception e) {
                LOG.warning("Error -> " + e.getMessage());
            }

            return response;
        }
    }

    private void buildDeliveryOrders(List<CustomerInfo> customerInfoList) {
        if (Objects.nonNull(customerInfoList) && !customerInfoList.isEmpty()) {
            customerInfoList.forEach(customer -> {
                if (Objects.nonNull(customer.getCustomerId())) {
                    User user = users.findUserByIdCustomerLastLogin(Math.toIntExact(customer.getCustomerId()));
                    if (Objects.nonNull(user)) {
                        DeliveryOrderInfo deliveryOrderInfoAux = new DeliveryOrderInfo();

                        DeliveryOrder deliveryOrder = ofy().load().type(DeliveryOrder.class).ancestor(user)
                                .filter("deliveryType", "SCANANDGO")
                                .filter("currentStatus", 1).first().now();

                        if (Objects.nonNull(deliveryOrder)) {
                            deliveryOrderInfoAux.setSubTotalPrice(deliveryOrder.getSubTotalPrice());
                            deliveryOrderInfoAux.setOfferPrice(deliveryOrder.getOfferPrice());
                            deliveryOrderInfoAux.setCreateDate(deliveryOrder.getCreateDate());
                            deliveryOrderInfoAux.setTotalPrice(deliveryOrder.getTotalPrice());


                            deliveryOrderInfoAux.setItems(getDeliveryOrderItems(deliveryOrder));

                            // set delivery order
                            customer.setDeliveryOrder(deliveryOrderInfoAux);
                        }
                    }
                }

            });
        }
    }

    private List<DeliveryOrderInfo.ItemInfo> getDeliveryOrderItems(DeliveryOrder deliveryOrder) {
        List<DeliveryOrderInfo.ItemInfo> itemInfoList = new ArrayList<>();
        if (Objects.nonNull(deliveryOrder)) {

            List<DeliveryOrderItem> itemsScanAndGo = new ArrayList<>();
            itemsScanAndGo = ofy()
                    .load()
                    .type(DeliveryOrderItem.class)
                    .ancestor(deliveryOrder)
                    .list();

            if (Objects.nonNull(itemsScanAndGo)) {
                itemsScanAndGo.removeIf(item -> (Objects.isNull(item.getScanAndGo()) || !item.getScanAndGo()));
            }

            if (Objects.nonNull(itemsScanAndGo) && !itemsScanAndGo.isEmpty()) {
                // Multiquery Implementation
                itemsScanAndGo.forEach(itemSGO -> {
                    DeliveryOrderInfo.ItemInfo itemInfoAux = new DeliveryOrderInfo.ItemInfo();
                    ItemAlgolia itemAlgolia = APIAlgolia.getItemAlgoliaById(itemSGO.getId(), null);

                    itemInfoAux.setId(itemSGO.getId());
                    itemInfoAux.setQuantitySold(itemSGO.getQuantitySold());

                    // Algolia
                    if (Objects.nonNull(itemAlgolia)) {

                        if (Objects.nonNull(itemAlgolia.getMediaDescription()))
                            itemInfoAux.setFirstDescription(itemAlgolia.getMediaDescription());

                        if (Objects.nonNull(itemAlgolia.getDetailDescription()))
                            itemInfoAux.setSecondDescription(itemAlgolia.getDetailDescription());

                        if (Objects.nonNull(itemAlgolia.getMediaImageUrl()))
                            itemInfoAux.setMediaImageURL(itemAlgolia.getMediaImageUrl());

                        if (Objects.nonNull(itemAlgolia.getRequirePrescription()))
                            itemInfoAux.setRequirePrescription(itemAlgolia.getRequirePrescription());

                        if (Objects.nonNull(itemAlgolia.getFullPrice()))
                            itemInfoAux.setPrice(itemAlgolia.getFullPrice());

                        if (Objects.nonNull(itemAlgolia.getBarcode()))
                            itemInfoAux.setBarcode(itemAlgolia.getBarcode());
                    }

                    itemInfoList.add(itemInfoAux);
                });
            }

        }

        return itemInfoList;

    }


    private List<CustomerInfo> getCustomerInfoList(List<Integer> customerIds) throws IOException {
        List<CustomerInfo> customerList = new ArrayList<>();


        if (Objects.nonNull(customerIds)
                && !customerIds.isEmpty()) {


            CustomerByIdRequest request = new CustomerByIdRequest();
            request.setCustomerIds(customerIds);
            CustomersByIdResponse customersByIdList = ApiGatewayService.get().getCustomerListByIds(request);

            customerIds.forEach(idCustomer -> {
                CustomerInfo customerAux = new CustomerInfo();
                customerAux.setCustomerId(Long.valueOf(idCustomer));

                buildCustomer(customerAux, customersByIdList.getData());

                customerList.add(customerAux);
            });
        }
        return customerList;
    }

    private void buildCustomer(CustomerInfo customerAux, ArrayList<CustomerResponse> customersByIdList) {
        if (Objects.nonNull(customerAux)
                && Objects.nonNull(customersByIdList)
                && !customersByIdList.isEmpty()
                && Objects.nonNull(customerAux.getCustomerId())) {

            Integer idCustomer = Math.toIntExact(customerAux.getCustomerId());
            Optional<CustomerResponse> optionalCustomerData = customersByIdList
                    .stream()
                    .filter(customerResponse -> customerResponse.getId().equals(idCustomer)).findFirst();

            optionalCustomerData.ifPresent(customerResponse -> {


                BlockedUser blockedUserSaved = ofy().load().type(BlockedUser.class).filter("idUser", idCustomer).first().now();

                boolean userBlocked = blockedUserSaved != null;

//                LOG.info("usuario bloqueado -> " + userBlocked);
                customerAux.setBlocked(userBlocked);
                customerAux.setFirstName(customerResponse.getFirstname());
                customerAux.setLastName(customerResponse.getLastname());
                customerAux.setGender(customerResponse.getGender());
                customerAux.setDocumentNumber(Long.valueOf(customerResponse.getDocumentNumber()));
                customerAux.setEmail(customerResponse.getEmail());
                customerAux.setPhone(customerResponse.getPhone());

            });

        }
    }


    /**
     * Get old orders to cancel.
     */
    @ApiMethod(name = "finalizeOldOrders", path = "/orderEndpoint/finalizeOldOrders", httpMethod = ApiMethod.HttpMethod.GET)
    public com.imaginamos.farmatodo.backend.order.OrderFinalizeRes getOldOrders(@Named("keyClient") final String keyClient) throws UnauthorizedException {
//        LOG.info("API: /orderEndpoint/finalizeOldOrders");

        if (!keyClient.equals(Constants.KEY_SECURE_CLIENT))
            throw new UnauthorizedException(Constants.ERROR_ACCESS_DENIED);

        com.imaginamos.farmatodo.backend.order.OrderFinalizeRes res = new com.imaginamos.farmatodo.backend.order.OrderFinalizeRes();
        try {
            // Constants:
            Optional<TimeFinalizeOrders> timeFinalizeOrdersOptional = APIAlgolia.getTimeFinalizeOrder();
            final int time = timeFinalizeOrdersOptional.get().getTime();

            if (Objects.nonNull(timeFinalizeOrdersOptional)) {
                // Get active orders:
                final List<DeliveryOrder> orders = ofy().load().type(DeliveryOrder.class).filter("deliveryType", "EXPRESS").filter("isActive", true).limit(500).list();
                // Get date - days:
                Date date = new Date();
//                LOG.info("Date Base " + date);
                if (timeFinalizeOrdersOptional.get().getTimeVariable().equals(Constants.DAYS)) {
//                    LOG.info("Se substrae el tiempo en dias");
                    date = ftdUtilities.addSubtractDaysDate(date, time);
                } else {
//                    LOG.info("Se substrae el tiempo en horas");
                    date = ftdUtilities.addHoursDate(date, -time);
                }

                final Date finalDate = date;
//                LOG.info("Date base :" + date + " Se encontraron [" + (orders != null ? orders.size() : "0") + "]");
                // Select unique orders:
                Set<Long> ordersId = new HashSet<>();

                List<DeliveryOrder> result = orders.stream().filter(deliveryOrderSaved -> deliveryOrderSaved.getCreateDate().before(finalDate))
                        .map(deliveryOrderSaved -> {
                            deliveryOrderSaved.setLastStatus(ORDER_DELIVERED);
                            deliveryOrderSaved.setActive(false);
                            ordersId.add(deliveryOrderSaved.getIdOrder());
                            return deliveryOrderSaved;
                        }).collect(Collectors.toList());

                if (Objects.nonNull(result) && !result.isEmpty()) {
//                    LOG.info("Actualiza " + result.size() + " Ordenes ");
                    ofy().save().entities(result);
                }

                // Response:
                List<Long> ids = new ArrayList<>();
                ids.addAll(ordersId);
                res.setOrders(ids);
                res.setMessage("Date: [" + date + "] quantity finalized orders : [" + ids.size() + "]");
            }
            return res;
        } catch (Exception e) {
            res.setMessage("Error al finalizar ordenes " + e.getMessage());
            LOG.warning("Error finalize order -> " + e.getMessage());
            return res;
        }
    }

    @ApiMethod(name = "finalizeOldOrders",
            path = "/orderEndpoint/finalizeOrders",
            httpMethod = ApiMethod.HttpMethod.POST)
    public com.imaginamos.farmatodo.backend.order.OrderFinalizeRes finalizeOrders(final OrderFinalizedReq request) {
//        LOG.info("API: /orderEndpoint/finalizeOldOrders ");

        com.imaginamos.farmatodo.backend.order.OrderFinalizeRes res = new OrderFinalizeRes();
        try {
            // Constants:
            Optional<TimeFinalizeOrders> timeFinalizeOrdersOptional = APIAlgolia.getTimeFinalizeOrder();
            final int time = timeFinalizeOrdersOptional.get().getTime();

            if (Objects.nonNull(timeFinalizeOrdersOptional)) {
                // Get active orders:

                List<Long> listFinalized = new ArrayList<>();
                List<Long> listCanceled = new ArrayList<>();

                request.getOrderFinalized().forEach(orders -> {
                    if (orders.getStatus() == 7 || orders.getStatus() == 6) {
                        listFinalized.add(orders.getIdOrder());
                    } else {
                        listCanceled.add(orders.getIdOrder());
                    }
                });

                //LOG.info("ordenes para finalizar: " + listFinalized);
                //LOG.info("ordenes para cancelar: " + listCanceled);

                final List<DeliveryOrder> orders_finalized = ofy().load().type(DeliveryOrder.class).filter("idOrder in", listFinalized).filter("isActive", true).limit(500).list();
                final List<DeliveryOrder> orders_canceled = ofy().load().type(DeliveryOrder.class).filter("idOrder in", listCanceled).filter("isActive", true).limit(500).list();
                // Get date - days:
                Date date = new Date();
                LOG.info("Date Base " + date);
                if (timeFinalizeOrdersOptional.get().getTimeVariable().equals(Constants.DAYS)) {
                    LOG.info("Se substrae el tiempo en dias");
                    date = ftdUtilities.addSubtractDaysDate(date, time);
                } else {
                    LOG.info("Se substrae el tiempo en horas");
                    date = ftdUtilities.addHoursDate(date, -time);
                }

                final Date finalDate = date;
//                LOG.info("Date base :"+date+" Se encontraron [" + (orders != null ? orders.size() : "0") + "]");
                // Select unique orders:
                Set<Long> ordersId = new HashSet<>();
                Set<Long> ordersIdCanceled = new HashSet<>();

                List<DeliveryOrder> result = orders_finalized.stream().map(deliveryOrderSaved -> {
                    deliveryOrderSaved.setLastStatus(ORDER_DELIVERED);
                    deliveryOrderSaved.setActive(false);
                    ordersId.add(deliveryOrderSaved.getIdOrder());
                    return deliveryOrderSaved;
                }).collect(Collectors.toList());

                List<DeliveryOrder> result_aux = orders_canceled.stream().map(deliveryOrderSaved -> {
                    deliveryOrderSaved.setLastStatus(ORDER_CANCELED);
                    deliveryOrderSaved.setActive(false);
                    ordersIdCanceled.add(deliveryOrderSaved.getIdOrder());
                    return deliveryOrderSaved;
                }).collect(Collectors.toList());

                if (Objects.nonNull(result) && !result.isEmpty()) {
//                    LOG.info("Actualiza " + result.size() + " Ordenes ");
                    ofy().save().entities(result);
                    ofy().save().entities(result_aux);
                }

                // Response:
                List<Long> ids = new ArrayList<>();
                List<Long> idsCanceled = new ArrayList<>();
                idsCanceled.addAll(ordersIdCanceled);
                ids.addAll(ordersId);
                res.setOrders(ids);
                res.setMessage("Date: [" + date + "] quantity finalized orders : [" + ids.size() + "] " + "queantity cancel orders : [" + idsCanceled.size() + "]");
            }
            return res;
        } catch (Exception e) {
            res.setMessage("Error al finalizar ordenes " + e.getMessage());
            LOG.warning("Error finalize order -> " + e.getMessage());
            return res;
        }
    }

    /**
     * Check if there are items not billed.
     *
     * @param deliveryOrder
     */
    private void checkIfHasUnbilledItems(DeliveryOrder deliveryOrder) {
        try {
            if (Objects.nonNull(deliveryOrder)) {
//                LOG.info("checkIfHasUnbilledItems(" + deliveryOrder.getIdOrder() + ")");

                final List<Long> itemsNotBilled = getUnbilledItemsByOrderId(deliveryOrder.getIdOrder());

                if (Objects.nonNull(itemsNotBilled) && !itemsNotBilled.isEmpty()) {
                    final String messageForItemsNotBilled = APIAlgolia.getMessageForItemsNotBilled();
                    List<DeliveryOrderItem> requestedItems = deliveryOrder.getItemList();

                    if (Objects.nonNull(requestedItems) && !requestedItems.isEmpty()) {
                        requestedItems.forEach(requestedItem -> {
                            if (itemsNotBilled.contains(requestedItem.getId()))
                                requestedItem.setBilled(false);
                        });
                    }
                    deliveryOrder.setMessageWhenHasItemsNotBilled(messageForItemsNotBilled);
                }
            }
        } catch (Exception e) {
            LOG.warning("Error (no fatal) when checking if the order has unbilled items.");
        }
    }

    /**
     * Obtener los items no facturados de una orden.
     *
     * @param orderId
     */
    private List<Long> getUnbilledItemsByOrderId(final Long orderId) {
//        LOG.info("getUnbilledItemsByOrderId(" + orderId + ")");
        if (Objects.isNull(orderId) || orderId <= 0)
            return new ArrayList<>();

        try {
            final Response<GetUnbilledItemsByOrderResponse> response = ApiGatewayService.get().getUnbilledItemsByOrder(orderId);
            return response.isSuccessful() ? response.body().getData().getItems() : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Obtener las coordenadas de la direccion de la orden y de las tiendas.
     */
    private GetCoodinatesCustomerAndAddressByOrderResponseData getCustomerAndStoresCoordinatesByOrderResponseData(final Long orderId) {
//        LOG.info("metohd: getCustomerAndStoresCoordinatesByOrderResponseData(" + orderId + ")");

        if (Objects.isNull(orderId) || orderId <= 0)
            return null;

        try {
            final GetCoodinatesCustomerAndAddressByOrderResponseData data = ApiGatewayService.get().getCustomerAndStoresCoordinatesByOrder(orderId);
            return data;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Obtener las coordenadas de la direccion de la orden y de las tiendas.
     */
    private Boolean validateFreeDeliveryByCart(final FreeDeliverySimpleCart cart) {
        if (Objects.isNull(cart))
            return Boolean.FALSE;

        try {
            return ApiGatewayService.get().validateFreeDeliveryByCart(cart);
        } catch (Exception e) {
            return false;
        }
    }

    private void itemOfferComboPopUp(DeliveryOrderItem deliveryOrderItem) {
        if (!Objects.isNull(deliveryOrderItem.getOfferText()) && !deliveryOrderItem.getOfferText().isEmpty() && (Constants.OFFER_TEXT_THREE_FOR_TWO.equals(deliveryOrderItem.getOfferText()) || (Constants.OFFER_TEXT_FIVE_FOR_FOUR.equals(deliveryOrderItem.getOfferText())))) {
//            LOG.info("Este item tiene oferta 3X2 " + deliveryOrderItem.getId());
            Optional<OfferComboPopUp> optionalOfferComboPopUp = APIAlgolia.getAlgoliaConfigPopUp(URLConnections.ALGOLIA_CONFIG_ITEM_OFFER_POPUP3X2);
            if (optionalOfferComboPopUp.isPresent()) {
                deliveryOrderItem.setOfferComboPopUp(optionalOfferComboPopUp.get());
            }
        } else if (!Objects.isNull(deliveryOrderItem.getOfferText()) && !deliveryOrderItem.getOfferText().isEmpty() && (Constants.OFFER_TEXT_TWO_FOR_ONE.equals(deliveryOrderItem.getOfferText()))) {
            Optional<OfferComboPopUp> optionalOfferComboPopUp = APIAlgolia.getAlgoliaConfigPopUp(URLConnections.ALGOLIA_CONFIG_ITEM_OFFER_POPUP2X1);
            if (optionalOfferComboPopUp.isPresent()) {
                deliveryOrderItem.setOfferComboPopUp(optionalOfferComboPopUp.get());
            }
        }
    }

    /*
    private CreateOrderSubscribeData createOrderSubscribeData(OrderConfigEnum via, CreateOrderSubscribeReq orderSubRequest) throws ServiceUnavailableException, BadRequestException, IOException, ConflictException, InternalServerErrorException, NotFoundException, UnauthorizedException {
        CreateOrderSubscribeData resCreateOrder = new CreateOrderSubscribeData();
        Response<CreateOrderSubscribeResponse> resCreateOrderBackend3 = null;

        switch (via){
            case CORE:
                resCreateOrder = CoreService.get().createOrderSubscription(orderSubRequest);
                break;
            case BACKEND3:
                resCreateOrderBackend3 = ApiBackend30Service.get().createOrderPASBck3(orderSubRequest);
                break;
            case BACKEND3_FAIL_USE_CORE:
                resCreateOrderBackend3= ApiBackend30Service.get().createOrderPASBck3(orderSubRequest);
                if (!resCreateOrderBackend3.isSuccessful())
                    resCreateOrder = CoreService.get().createOrderSubscription(orderSubRequest);
                break;
            default:
                resCreateOrderBackend3 = ApiBackend30Service.get().createOrderPASBck3(orderSubRequest);
                if (!resCreateOrderBackend3.isSuccessful())
                    resCreateOrder = CoreService.get().createOrderSubscription(orderSubRequest);
                break;
        }

        if (resCreateOrder.getId() != 0){
            return resCreateOrder;
        }else {
            if (resCreateOrderBackend3.isSuccessful()){
                CreateOrderSubscribeResponse r = resCreateOrderBackend3.body();
                return r.getData();
            }
            return null;
        }
    }*/

    @ApiMethod(name = "cancelOrderCourierDataStore", path = "/orderEndpoint/cancelOrderCourierDataStore", httpMethod = ApiMethod.HttpMethod.POST)
    public CancelOrderResponse cancelOrderCourierDataStore(@Nullable @Named("idCustomerWebSafe") String idCustomerWebSafe,
                                                           CancelOrderReq cancelOrderReq) throws ConflictException, AlgoliaException {
        return cancelOrder(idCustomerWebSafe, cancelOrderReq, "v2");
    }

    @ApiMethod(name = "cancelOrderCourierDataStorev3", path = "/orderEndpoint/v3/cancelOrderCourierDataStore", httpMethod = ApiMethod.HttpMethod.POST)
    public CancelOrderResponse cancelOrderCourierDataStorev3(@Nullable @Named("idCustomerWebSafe") String idCustomerWebSafe,
                                                           CancelOrderReq cancelOrderReq) throws ConflictException, AlgoliaException {
        return cancelOrder(idCustomerWebSafe, cancelOrderReq, "v3");
    }

    public CancelOrderResponse cancelOrder(@Nullable @Named("idCustomerWebSafe") String idCustomerWebSafe,
                                           CancelOrderReq cancelOrderReq,
                                           @Named("version") String version) {

        CancelOrderResponse response = new CancelOrderResponse();
        try {
            // Validar y preparar entrada
            idCustomerWebSafe = Optional.ofNullable(idCustomerWebSafe).orElse("");
            Guard.isValidCancelOrderReq(cancelOrderReq);

            DeliveryOrderOms deliveryOrderOms = null;
            DeliveryOrder deliveryOrderDataStore = null;
            if ("v3".equals(version)) {
                deliveryOrderOms = fetchDeliveryOrderOms(cancelOrderReq);
            } else {
                deliveryOrderDataStore = fetchDeliveryOrderDataStore(cancelOrderReq);
            }

            // Llamar a cancelar courier y si tiene cupones borrarlos
            OrderCourierCancelReq courierReq = mapCancelOrderReqToOrderCourierCancelReq(cancelOrderReq);
            callCancelOrderCourierOMS(cancelOrderReq, courierReq, version);
            cancelOrderDataStore(Long.valueOf(cancelOrderReq.getOrderId()));
            validateUserPrimeAndSaving(idCustomerWebSafe, cancelOrderReq, version, deliveryOrderOms, deliveryOrderDataStore);
            cancelStatusRx(cancelOrderReq.getOrderId());

            // Construir respuesta y enviar notificaciones
            createResponse(response);
            sendNotification(cancelOrderReq, courierReq);


        } catch (BadRequestException e) {
            LOG.warning("Validación fallida para la cancelación de la orden: " + e.getMessage());
            response.setCode("VALIDATION_ERROR");
            response.setMessage("Los datos de la solicitud no son válidos.");
        } catch (IOException e) {
            LOG.severe("Error al llamar a un servicio externo: " + e.getMessage());
            response.setCode("SERVICE_ERROR");
            response.setMessage("Error en servicios externos durante la cancelación.");
        } catch (Exception e) {
            LOG.warning("Error inesperado al cancelar la orden: " + e.getMessage());
            response.setCode("ERROR");
            response.setMessage("Ocurrió un error inesperado.");
        }

        return response;
    }

    private DeliveryOrderOms fetchDeliveryOrderOms(CancelOrderReq cancelOrderReq) throws ConflictException, BadRequestException {
        DeliveryOrderOms deliveryOrderOms = OrderUtil.getOrderMethodv2(cancelOrderReq.getOrderId());
        LOG.info("deliveryOrderOms:"+ new Gson().toJson(deliveryOrderOms));
        validCancelOrderMarketplacev3(deliveryOrderOms);
        return deliveryOrderOms;
    }

    private DeliveryOrder fetchDeliveryOrderDataStore(CancelOrderReq cancelOrderReq) throws ConflictException {
        DeliveryOrder deliveryOrder = ofy()
                .load()
                .type(DeliveryOrder.class)
                .filter("idOrder", Integer.valueOf(cancelOrderReq.getOrderId()))
                .first()
                .now();
        validCancelOrderMarketplace(deliveryOrder);
        return deliveryOrder;
    }


    private static void createResponse(CancelOrderResponse response) {
        CancelOrderResponseData cancelOrderResponseData = new CancelOrderResponseData();
        cancelOrderResponseData.setMessage("Se cancela la orden exitosamente");
        response.setCode(HttpStatusCode.OK.getStatusName());
        response.setMessage("Success");
        response.setData(cancelOrderResponseData);
    }

    private void validateUserPrimeAndSaving(String idCustomerWebSafe, CancelOrderReq cancelOrderReq, String version, DeliveryOrderOms deliveryOrderOms, DeliveryOrder deliveryOrderDataStore) {
        if (cancelOrderReq == null || cancelOrderReq.getCorreoUsuario() == null) {
            LOG.warning("Invalida la cancelación request faltante de datos");
            return;
        }
        Credential credential = users.findUserByEmail(cancelOrderReq.getCorreoUsuario().toLowerCase());
        User user = null;
        if(credential!=null){
            user = users.findUserByKey(credential.getOwner().getKey());
        }
        if (user == null) {
            LOG.warning("Usuario no encontrado para la cancelación de la orden");
            return;
        }
        int userID = user.getId();
        long userIDLong = (long) userID;

        deleteCouponTalonOne(userID, idCustomerWebSafe);
        deleteCacheDeductDiscount(idCustomerWebSafe, String.valueOf(userID));

        double saving = getSavingAmount(version, deliveryOrderOms, deliveryOrderDataStore);

        if (saving > 0) {
            boolean isUserPrime = validateUserPrime(userIDLong);
            processSaving(userIDLong, saving);
        }
    }

    private void cancelStatusRx(String orderId){
        CancelStatusRx cancelStatusRx = new CancelStatusRx();
        cancelStatusRx.setOrderId(orderId);
        cancelStatusRx.setStatus(OrderStatusEnum.CANCELADA);
        ApiGatewayService.get().cancelStatusRx(cancelStatusRx);
    }

    private double getSavingAmount(String version, DeliveryOrderOms deliveryOrderOms, DeliveryOrder deliveryOrderDataStore) {
        if ("v3".equals(version)) {
            return deliveryOrderOms != null ? deliveryOrderOms.getSavingPrime() : 0;
        } else {
            return deliveryOrderDataStore != null ? deliveryOrderDataStore.getSavingPrime() : 0;
        }
    }

    private void processSaving(long userID, double saving) {
        UpdateTypeSavingEnum updateType = UpdateTypeSavingEnum.SUBTRACT;
        SavingCustomer requestSaving = getSavingCustomerRequest(userID, saving, updateType);

        try {
            ApiGatewayService.get().sendSavingNoPrime(requestSaving);
        } catch (Exception e) {
            LOG.info("Error al procesar el ahorro para el usuario con ID: " + userID);
        }
    }

    @NotNull
    private CancelOrderCourierRes callCancelOrderCourierOMS(CancelOrderReq cancelOrderReq,
                                                            OrderCourierCancelReq courierReq,
                                                            String version) throws IOException {
        CancelOrderCourierRes courierRes = ApiGatewayService.get().cancelOrder(courierReq);

        // Manejo de versiones diferente a "v3"
        if (!"v3".equals(version) && shouldReturnCoupon(courierRes)) {
            processCouponReturn(cancelOrderReq);
        }

        return courierRes;
    }

    private boolean shouldReturnCoupon(CancelOrderCourierRes courierRes) {
        return courierRes != null
                && courierRes.getData() != null
                && Boolean.TRUE.equals(courierRes.getData().getDeleteCoupon());
    }

    private void processCouponReturn(CancelOrderReq cancelOrderReq) {
        try {
            Long orderId = Long.valueOf(cancelOrderReq.getOrderId());
            returnCouponV2(orderId);
        } catch (NumberFormatException e) {
            LOG.warning("Error al procesar el ID de la orden para el retorno del cupón: " + e.getMessage());
            throw new IllegalArgumentException("ID de la orden no válido: " + cancelOrderReq.getOrderId(), e);
        }
    }

    private static void sendNotification(CancelOrderReq cancelOrderReq, OrderCourierCancelReq courierReq) throws AlgoliaException, ConflictException, IOException {
        BrazeClient.sendOrderPushNotification(new SendOrderPush(cancelOrderReq.getCorreoUsuario(), 14,
                BrazeStatusEnum.CANCEL.getValue(), cancelOrderReq.getOrderId(),
                courierReq.getCancellationReasonId()));
    }

    private OrderCourierCancelReq mapCancelOrderReqToOrderCourierCancelReq(CancelOrderReq req) {
        Long DEFAULT_CANCELLATION_REASON_ID = 10L;
        OrderCourierCancelReq courierReq = new OrderCourierCancelReq();
        courierReq.setOrderId(Long.valueOf(req.getOrderId()));
        courierReq.setRol(req.getRol());
        courierReq.setCorreoUsuario(req.getCorreoUsuario());
        courierReq.setCancellationReasonId(
                Optional.ofNullable(req.getCancellationReasonId()).orElse(DEFAULT_CANCELLATION_REASON_ID));
        courierReq.setEmployeeNumber(req.getEmployeeNumber());
        return courierReq;
    }


    private static void validCancelOrderMarketplace(DeliveryOrder deliveryOrder) throws ConflictException {
        List<DeliveryOrderProvider> deliveryOrderProviderList = ofy().load().type(DeliveryOrderProvider.class).ancestor(deliveryOrder).list();

        boolean isOrderMarketplace = deliveryOrderProviderList.stream().allMatch(deliveryOrderProvider -> deliveryOrderProvider.getId() == Constants.PROVIDER_ID_MARKETPALCE);

        if(deliveryOrder.getDeliveryType().equals(DeliveryType.PROVIDER) && isOrderMarketplace ){
            
            throw new ConflictException(Constants.ERROR_MESSAGE_CANNOT_CANCEL_MARKETPLACE_ORDERS);

        }
    }

    private static void validCancelOrderMarketplacev3(DeliveryOrderOms deliveryOrder) {
        if(deliveryOrder.getProviderList()!=null) {
            deliveryOrder.getProviderList().forEach(deliveryOrderProvider -> {
                if (deliveryOrder.getDeliveryType().equals(DeliveryType.PROVIDER) && deliveryOrderProvider.getId() == Constants.PROVIDER_ID_MARKETPALCE) {
                    throw new IllegalStateException(Constants.ERROR_MESSAGE_CANNOT_CANCEL_MARKETPLACE_ORDERS);
                }
            });
        }
    }


    private void cancelOrderDataStore(Long orderId) {

        try {
            DeliveryOrder deliveryOrder = ofy().load().type(DeliveryOrder.class).filter("idOrder", orderId).first().now();
            if (deliveryOrder != null) {
                deliveryOrder.setLastStatus(ORDER_CANCELED);
                deliveryOrder.setActive(false);
                ofy().save().entities(deliveryOrder);
//                LOG.info("delivery order canceled" + deliveryOrder);
            }
        } catch (Exception e) {
            LOG.warning("Error en cancelOrderDataStore. Mensaje: " + e.getMessage());
        }
    }

    private SavingCustomer getSavingCustomerRequest(Long idUser, double saving, UpdateTypeSavingEnum updateTypeSavingEnum) {
        SavingCustomer savingCustomer = new SavingCustomer();
        savingCustomer.setCustomerId(idUser);
        savingCustomer.setPrimeSaving(saving);
        savingCustomer.setUpdateTypeSavingEnum(updateTypeSavingEnum);
        return savingCustomer;
    }


    //revision
    private boolean returnCouponV2(final Long idOrder) {

//        LOG.info("request:" + idOrder);
        GenericResponse<String> response = new GenericResponse<>();
        response.setCode("200");

        DeliveryOrder deliveryOrder = ofy().load().type(DeliveryOrder.class).filter("idOrder", idOrder).first().now();
//        LOG.info("Order: " + deliveryOrder);
        if (deliveryOrder != null) {

            List<DeliveryOrderItem> deliveryOrderItemList = ofy().load().type(DeliveryOrderItem.class).ancestor(deliveryOrder).list();

            for (DeliveryOrderItem deliveryOrderItem : deliveryOrderItemList) {

                if (deliveryOrderItem.getCoupon() != null && deliveryOrderItem.getCoupon()) {

                    Coupon coupon = ofy().load().type(Coupon.class).filter("itemId", deliveryOrderItem.getIdItem()).first().now();
                    Key<Customer> customerKey = deliveryOrder.getIdCustomer().key();

                    if (coupon != null && customerKey != null) {
                        CustomerCoupon customerCoupon = ofy().load().type(CustomerCoupon.class).ancestor(coupon).filter("customerKey", customerKey).first().now();
                        if (customerCoupon != null) {
                            response.setMessage("CustomerCoupon encontrado");
                            response.setData("Id: " + customerCoupon.getCouponId() + " UseTime:" + customerCoupon.getUseTime());
                            ofy().delete().entities(customerCoupon);
                            try {
                                subtractCountUses(coupon);
                            } catch (Exception e) {
                                LOG.info("Ocurrio un error trayendo el cupon " + e.getMessage());
                            }
                            return true;
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            }
        } else {
            return false;
        }
        return false;
    }

    private static void subtractCountUses(Coupon coupon) {
        coupon.setCountUses(coupon.getCountUses() - 1);
        ofy().save().entity(coupon).now();
    }


    private CustomerCoupon obtainCustomerCoupon(final Key<Customer> customerKey) {
        try {
//            LOG.info("obtainCustomerCoupon(" + customerKey.toString() + ")");
            final List<CustomerCoupon> customerCoupons = ofy().load().type(CustomerCoupon.class).filter("customerKey", customerKey).orderKey(false).list();
//            LOG.info("IF(customerCoupons!=null && !customerCoupons.isEmpty()) : [" + (customerCoupons != null && !customerCoupons.isEmpty()) + "]");
            if (customerCoupons != null && !customerCoupons.isEmpty()) {
                customerCoupons.sort(Comparator.comparing(a -> a.getUseTime()));
                final int positionLastCupon = customerCoupons.size() - 1;
                final CustomerCoupon couponToRedim = customerCoupons.get(positionLastCupon);
                if (couponToRedim != null) {
//                    LOG.info("obtainCustomerCoupon cupon encontrado(" + couponToRedim.getCustomerCouponId() + ")");
                    return couponToRedim;
                }
                return null;
            }
            return null;
        } catch (Exception e) {
            LOG.warning("Error al obtener cupon cuando el cliente elimina todo el carrito... Mensaje: " + e.getMessage());
            return null;
        }

    }

    private boolean verifyCampaingCoupon(Campaign campaign, String couponRequest) {
//        LOG.info("verifyCampaingCoupon(" + couponRequest + ")");
        if (campaign == null) {
            LOG.info("Campaña no encontrada");
            return false;
        }
        if (campaign.getActive() == null) {
            LOG.info("No se encontro campana ");
            return false;
        }
        if (!campaign.getActive()) {
            LOG.info("No se encontro campana activa");
            return false;
        }
        if (campaign.getCoupons() == null) {
            LOG.info("No se encontro ningun cupon");
        }
        for (String coupon : campaign.getCoupons()) {
            if (coupon == null) {
                LOG.info("No se encontro ningun cupon");
                return false;
            }
            if (coupon.trim().equalsIgnoreCase(couponRequest.trim())) {
                LOG.info("Cupon encontrado");
                return true;
            }
        }
        LOG.info("Cupon  no  encontrado");
        return false;
    }

    private boolean couponFilter(Campaign campaign, RequestSourceEnum requestSourceEnum, DeliveryOrder order) {
        boolean source = false;
        boolean deliveryType = false;

        if (campaign.getVariables() == null) {
//            LOG.info("las variables de la campaña es nula");
            return false;
        }
        for (Variables variable : campaign.getVariables()) {
            if (variable == null) {
//                LOG.info("variable es nula");
                return false;
            }
            if (variable.getValues() == null) {
//                LOG.info("valores de la variable es nula");
                return false;
            }
            if (variable.getKey() == null) {
//                LOG.info("key de la variable es nula");
                return false;
            }

            if (variable.getKey().trim().equalsIgnoreCase("SOURCE")) {
                source = couponForSource(variable, requestSourceEnum);
            }
            if (variable.getKey().trim().equalsIgnoreCase("DELIVERY_TYPE")) {
                deliveryType = couponsForDt(variable, order);
            }
        }
        return source && deliveryType;

    }

    private boolean couponForSource(Variables variable, RequestSourceEnum requestSourceEnum) {
        if (variable.getValues() == null) {
            LOG.info("No se encontro valores para la campana");
        }
        if (variable.getKey() == null) {
            LOG.info("No se encontro valores para la campana");
        }
        for (String value : variable.getValues()) {
            if (value.trim().equalsIgnoreCase(requestSourceEnum.name())) {
                LOG.info("Se encontro source para la campana");
                return true;
            }
        }

        return false;
    }


    private boolean couponsForDt(Variables variable, DeliveryOrder order) {
        if (variable.getValues() == null) {
            LOG.info("No se encontro valores para la campana");
        }
        if (variable.getKey() == null) {
            LOG.info("No se encontro valores para la campana");
        }
        for (String value : variable.getValues()) {
            if (value.trim().equalsIgnoreCase(order.getDeliveryType().name())) {
                LOG.info("Se encontro delivery type para la campana{}" + value);
                return true;
            }
        }
        return false;
    }

    @ApiMethod(name = "testMessageAlgolia", path = "/orderEndpoint/testMessageAlgolia", httpMethod = ApiMethod.HttpMethod.GET)
    public void testMessageAlgoliaOrder(@Named("orderId") Long orderId, @Named("code") String code, @Named("phone") String phone) {
//        LOG.info("request:"+ orderId);
        CreatedOrder finalOrderJSON = new CreatedOrder();
        finalOrderJSON.setId(orderId);
//        LOG.info("finalOrderJSON:"+ finalOrderJSON.getId());
        String orderIdString = Long.toString(orderId);
//        LOG.info("orderIdString:"+ orderId);
        int responseSms = 0;
        try {
            final SendSMSCloudFunctionReq request = new SendSMSCloudFunctionReq(phone,
                    MsgUtilAlgolia.obtainMsgAlgolia(MsgSmsEnum.MESSAGE_KEY_SMS)

            );
            CloudFunctionsService.get().postSendSms(request);
            LOG.info("response:" + request.getBody());
        } catch (IOException | BadRequestException e) {
            LOG.warning("Error");
        }
        //LOG.warning("Response sms " + responseSms);
    }

    //revision

    /**
     * Miguel Angel Claros Quintero 2022-03-21
     *
     * @param idOrder
     * @return
     * @throws IOException
     * @throws ConflictException
     */

    @ApiMethod(name = "returnCouponV2", path = "/orderEndpoint/returnCouponV2", httpMethod = ApiMethod.HttpMethod.GET)
    public Answer CouponV2Rest(@Named("idOrder") String idOrder) throws ConflictException {

        Answer dto = new Answer();
        //LOG.warning("Method: returnCouponV2: " + idOrder);
        if (idOrder == null || idOrder.isEmpty()) {
            throw new ConflictException(Constants.PARAM_IS_EMPTY);
        }
        dto.setMessage("SUCCESS");
        dto.setConfirmation(this.returnCouponV2(Long.valueOf(idOrder)));
        return dto;
    }

    private boolean validateUserPrime(Long customerId) {
        boolean isPrime = false;
        try {
            PrimeConfig primeConfig = APIAlgolia.primeConfigV2();
//            LOG.info("flag prime: "+ primeConfig.featureValidateUserPrime);
            if (primeConfig.featureValidateUserPrime) {
                CustomerResponseCart customerResponseCart = ApiGatewayService.get().getCustomerCreditCardPrimeData(customerId);
                if (customerResponseCart != null && customerResponseCart.isActive()) {
                    isPrime = true;
                }
            }
        } catch (Exception e) {
            LOG.info("No se pudo obtener el customer");
        }
//        LOG.info("isPrime: " + isPrime);
        return isPrime;
    }

    private void addRMSclasses(CreatedOrder orderJson) {
//        LOG.info("method: addRMSclasses(orderJson)");
        try {
            for (int i = 0; i < orderJson.getOrderData().getItemList().size(); i++) {
                DeliveryOrderItem item = orderJson.getOrderData().getItemList().get(i);
                ItemAlgolia itemAlgolia = APIAlgolia.getItemAlgolia(item.getId() + "" + URLConnections.MAIN_ID_STORE);
                if (itemAlgolia != null) {
                    // add RMS business class
                    orderJson.getOrderData().getItemList().get(i).setRms_subclass(Objects.nonNull(itemAlgolia.getRms_subclass()) ? itemAlgolia.getRms_subclass() : "");
                    orderJson.getOrderData().getItemList().get(i).setRms_group(Objects.nonNull(itemAlgolia.getRms_group()) ? itemAlgolia.getRms_group() : "");
                    orderJson.getOrderData().getItemList().get(i).setRms_deparment(Objects.nonNull(itemAlgolia.getRms_deparment()) ? itemAlgolia.getRms_deparment() : "");
                    orderJson.getOrderData().getItemList().get(i).setRms_class(Objects.nonNull(itemAlgolia.getRms_class()) ? itemAlgolia.getRms_class() : "");
                }
            }
        } catch (Exception e) {
            LOG.severe("method: addMarcaCategorySubcategorieAndItemUrl(orderJson) Message: " + e.getMessage());
        }
    }

    private GenericResponse validateCoupon(DeliveryOrder order, String couponName) throws IOException {
        Response<GenericResponse> responseBck3 = null;
        GenericResponse genericResponse = null;
        if (iscustomerPaymentCard(order) && iscustomerPaymentCardId(order) && couponName != null) {
//            LOG.info("customerPaymentCardId -> " + order.getPaymentCardId());
            CouponValidation couponValidation = new CouponValidation();
            couponValidation.setCouponName(couponName);
            couponValidation.setPaymentCardId(order.getPaymentCardId());

            try {
                responseBck3 = ApiGatewayService.get().validateCouponOms(couponValidation);
            } catch (Exception e) {
                LOG.info("Error@validateCouponOms " + e.getMessage());
            }
            if (responseBck3 != null && !responseBck3.isSuccessful()) {
                String error = (responseBck3.errorBody() != null ? responseBck3.errorBody().string() : "code : " + responseBck3.code());
                ;
                try {
                    genericResponse = parseError(error);
//                    LOG.info("genericResponse: " + genericResponse.toString());
                    return genericResponse;
                } catch (Exception e) {
                    LOG.warning("Ocurrio un error parseando el error de la respuesta de backend 3" + e.getMessage());
                }

            }

        }
        return genericResponse;
    }

    private String getMessageError(String error) {
        Optional<CouponFiltersConfig> couponsFilter = APIAlgolia.getCouponFilterConfig();
        String messageError = "Debes seleccionar la tarjeta del banco indicado en el beneficio del cupon";
        if (error == null || error.isEmpty()) {
            return messageError;
        }
        if (couponsFilter.isPresent()) {
            CouponFiltersConfig couponFiltersConfig = couponsFilter.get();
            List<MessagesError> messagesError = couponFiltersConfig.getMessagesError();
            if (messagesError == null) {
                return messageError;
            }
            for (MessagesError messagesError1 : messagesError) {
                if (messagesError1.getTypeError() != null && messagesError1.getTypeError().equals(error) && messagesError1.getCouponTypeMessage() != null &&
                        messagesError1.getCouponTypeMessage().equals("GENERIC_MESSAGE")) {
                    messageError = messagesError1.getValue();
                    break;
                }

            }
        }
        return messageError;
    }


    private boolean validatePayMethodCouponFilter(Long paymethod, Campaign campaign) {
        boolean validate = true;
        try {
            if (campaign != null && campaign.getPayMethods() != null && !campaign.getPayMethods().isEmpty()) {
                if (!campaign.getPayMethods().contains(paymethod)) {
                    return false;
                }
            }
        } catch (Exception e) {
            LOG.info("Error@validatePayMethodCouponFilter " + e.getMessage());
        }
        return validate;

    }

    private Token generateToken() {
        Token tokenClient = new Token();
        OAuthIssuer oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator()); //investigar encriptacion
        try {
            tokenClient.setToken(oauthIssuerImpl.accessToken());
            tokenClient.setRefreshToken(oauthIssuerImpl.refreshToken());
            tokenClient.setTokenExp(7);
        } catch (OAuthSystemException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
        return tokenClient;
    }

    private void encryptToken(Token tokenClient) {
        tokenClient.setRefreshToken(encryptToken(tokenClient.getRefreshToken()));
        tokenClient.setToken(encryptToken(tokenClient.getToken()));
    }

    private String encryptToken(String tokenClient) {
        StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
        return passwordEncryptor.encryptPassword(tokenClient);
    }

    @ApiMethod(name = "getQrCode", path = "/orderEndpoint/getQrCode", httpMethod = ApiMethod.HttpMethod.GET)
    public Object getQrCode(@Named("idOrder") Long idOrder) throws NotFoundException {
        if (idOrder == null || idOrder==0L) {
            throw new IllegalArgumentException("El ID de la orden no puede ser nulo o cero.");
        }

        DeliveryOrder deliveryOrder =OrderService.findDeliveryOrderById(idOrder);

        if (deliveryOrder == null) {
            throw new NotFoundException("No se encontró una orden con el ID especificado.");
        }

        return Optional.ofNullable(deliveryOrder.getQrCode())
                .filter(code -> !code.isEmpty())
                .orElse("");

    }


    @ApiMethod(name = "getOrdersV3", path = "/orderEndpoint/v3/getOrders", httpMethod = ApiMethod.HttpMethod.POST)
    public Result<CollectionResponseModelV2> myOrdersV3(final CustomerV2 customer) throws ConflictException, IOException, BadRequestException, InternalServerErrorException {

        if (customer.getToken() != null && !authenticate.isValidToken(customer.getToken(), customer.getTokenIdWebSafe())) {
            throw new ConflictException(Constants.INVALID_TOKEN);
        }

        CollectionResponseModel collectionResponseModel = getMyOrders(customer.getCursor(),customer.getIdCustomerWebSafe());
        CollectionResponseModelV2 collectionResponseModelV2 = new CollectionResponseModelV2();
        collectionResponseModelV2.setNextPageToken(collectionResponseModel.getNextPageToken());
        if(Objects.nonNull(collectionResponseModel.getPreviousOrders()) && !collectionResponseModel.getPreviousOrders().isEmpty()) {
            List<DeliveryOrderV2> previousOrdersFixed = collectionResponseModel.getPreviousOrders().stream().map(o -> new DeliveryOrderV2(o)).collect(Collectors.toList());
            collectionResponseModelV2.setPreviousOrders(previousOrdersFixed);
        }
        if(Objects.nonNull(collectionResponseModel.getActiveOrders()) && !collectionResponseModel.getActiveOrders().isEmpty()) {
            List<DeliveryOrderV2> activeOrdersFixed = collectionResponseModel.getActiveOrders().stream().map(o -> new DeliveryOrderV2(o)).collect(Collectors.toList());
            collectionResponseModelV2.setActiveOrders(activeOrdersFixed);
        }
        Result<CollectionResponseModelV2> resultOrder= new Result<>();
        resultOrder.setCode(HttpStatusCode.OK.getStatusName());
        resultOrder.setMessage("Success");
        resultOrder.setData(collectionResponseModelV2);
        return resultOrder;
    }


    @ApiMethod(name = "getOrderv3", path = "/orderEndpoint/v3/getOrder", httpMethod = ApiMethod.HttpMethod.GET)
    public GetOrderResponseV2 getOrderV3(@Named("token") final String token,
                                       @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                       @Nullable @Named("idCustomerWebSafe") final String idCustomerWebSafe,
                                       @Named("idOrder") final long idOrder, final HttpServletRequest req) throws ConflictException, BadRequestException, NotFoundException {

        GetOrderResponseV2 getOrderResponse = new GetOrderResponseV2();

        try {
            getOrderResponse.setData(new DeliveryOrderV2(getOrder(token, tokenIdWebSafe, idCustomerWebSafe ,idOrder, req)));
            getOrderResponse.setCode(HttpStatusCode.OK.getStatusName());
            getOrderResponse.setMessage("Success");
        } catch (ConflictException e) {
            getOrderResponse.setCode(HttpStatusCode.CONFLICT.getStatusName());
            getOrderResponse.setMessage(e.getMessage());
        }
        return getOrderResponse;
    }

    @ApiMethod(name = "deliveryTipeTime", path = "/orderEndpoint/deliveryTypeTime", httpMethod = ApiMethod.HttpMethod.POST)
    public DeliveryTypeTimeResponse getDeliveryTypeTimeGB(RequestDeliveryTypeTime request) {
        String customerId = Constants.CUSTOMER_ANONYMOUS;
        try{
            if (request.getIdCustomerWebSafe() != null && !request.getIdCustomerWebSafe().isEmpty()) {
                Key<User> userKey = Key.create(request.getIdCustomerWebSafe());
                User user = users.findUserByKey(userKey);
                if (user != null && user.getId() >= 0){
                    customerId = String.valueOf(user.getId());
                }
            }
        }catch (Exception e) {
            LOG.warning("Error al retornar usuario customer ID: " + request.getIdCustomerWebSafe() + ". Usando anonymous ID. Error: " + e.getMessage());
        }
        try {
            DeliveryTimesConfig deliveryTimesConfig = GrowthBookConfigLoader.getDeliveryTypeTime(customerId, request.getCity());
            return  new DeliveryTypeTimeResponse(HttpStatusCode.OK.getCode(), HttpStatusCode.OK.getStatusName(),
                    Constants.SUCCESS, deliveryTimesConfig);
        }catch (Exception e) {
            LOG.warning("Error al retornar la configuración delivery time: " + e.getMessage());
            return new DeliveryTypeTimeResponse(HttpStatusCode.INTERNAL_SERVER_ERROR.getCode(),
                    HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusName(),
                    "Error al retornar la configuración delivery time", null);
        }
    }

    @ApiMethod(name = "previousOrders", path = "/orderEndpoint/orders/previous", httpMethod = ApiMethod.HttpMethod.GET)
    public GenericResponse<GetOrdersOMSResponse> previousOrders(@Named("idCustomerWebSafe") String idCustomerWebSafe, @Named("cursor") @Nullable String cursor) throws BadRequestException, ConflictException, IOException {
        if (idCustomerWebSafe == null || idCustomerWebSafe.isEmpty()) {
            throw new BadRequestException("idCustomerWebSafe is required");
        }

        Key<User> userKey = Key.create(idCustomerWebSafe);
        User user = users.findUserByKey(userKey);

        if (user == null || user.getId() == 0) {
            throw new ConflictException("CustomerNotFound");
        }
        return ApiGatewayService.get().releasePreviousOrders(Long.valueOf(user.getId()), cursor);
    }

    @ApiMethod(name = "activeOrders", path = "/orderEndpoint/orders/active", httpMethod = ApiMethod.HttpMethod.GET)
    public GenericResponse<GetOrdersOMSResponse>  activeOrders(@Named("idCustomerWebSafe") String idCustomerWebSafe) throws BadRequestException, ConflictException, IOException {
        if (idCustomerWebSafe == null || idCustomerWebSafe.isEmpty()) {
            throw new BadRequestException("idCustomerWebSafe is required");
        }

        Key<User> userKey = Key.create(idCustomerWebSafe);
        User user = users.findUserByKey(userKey);

        if (user == null || user.getId() == 0) {
            throw new ConflictException("CustomerNotFound");
        }
        return ApiGatewayService.get().releaseActiveOrders(Long.valueOf(user.getId()));
    }
}