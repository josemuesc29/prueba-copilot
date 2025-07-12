package com.imaginamos.farmatodo.networking.talonone;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.imaginamos.farmatodo.model.algolia.ItemAlgolia;
import com.imaginamos.farmatodo.model.algolia.PrimeConfig;
import com.imaginamos.farmatodo.model.coupon.Coupon;
import com.imaginamos.farmatodo.model.customer.CustomerJSON;
import com.imaginamos.farmatodo.model.customer.CustomerResponseCart;
import com.imaginamos.farmatodo.model.dto.ComponentTypeEnum;
import com.imaginamos.farmatodo.model.dto.DynamicSection;
import com.imaginamos.farmatodo.model.dto.Element;
import com.imaginamos.farmatodo.model.dto.EnableForEnum;
import com.imaginamos.farmatodo.model.environment.Enviroment;
import com.imaginamos.farmatodo.model.order.CreatedOrder;
import com.imaginamos.farmatodo.model.order.DeliveryOrder;
import com.imaginamos.farmatodo.model.order.DeliveryOrderItem;
import com.imaginamos.farmatodo.model.order.DeliveryOrderProvider;
import com.imaginamos.farmatodo.model.order.ShoppingCartJson;
import com.imaginamos.farmatodo.model.product.Item;
import com.imaginamos.farmatodo.model.talonone.BestOfferEnum;
import com.imaginamos.farmatodo.model.talonone.CouponAutomaticTalon;
import com.imaginamos.farmatodo.model.talonone.PetalComplexOffer;
import com.imaginamos.farmatodo.model.user.User;
import com.imaginamos.farmatodo.model.util.AnswerDeduct;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.model.util.URLConnections;
import com.imaginamos.farmatodo.networking.algolia.APIAlgolia;
import com.imaginamos.farmatodo.networking.cache.CachedDataManager;
import com.imaginamos.farmatodo.networking.services.ApiGatewayService;
import com.imaginamos.farmatodo.networking.talonone.api.ITalonOne;
import com.imaginamos.farmatodo.networking.talonone.model.CartItem;
import com.imaginamos.farmatodo.networking.talonone.model.ComboDetail;
import com.imaginamos.farmatodo.networking.talonone.model.CustomerProfileRequest;
import com.imaginamos.farmatodo.networking.talonone.model.CustomerSessionRequest;
import com.imaginamos.farmatodo.networking.talonone.model.CustomerSessionResponse;
import com.imaginamos.farmatodo.networking.talonone.model.Effect;
import com.imaginamos.farmatodo.networking.talonone.model.ItemCombo;
import com.imaginamos.farmatodo.networking.talonone.model.TalonOneDeductDiscount;
import com.imaginamos.farmatodo.networking.talonone.model.TalonOneDiscount;
import org.jetbrains.annotations.NotNull;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.imaginamos.farmatodo.model.util.Constants.DEFAULT_PAYMENT_CARD_ID;

public class TalonOneService {
    private static final Logger LOG = Logger.getLogger(TalonOneService.class.getName());
    private final ITalonOne iTalonOne;

    private boolean userPrime = false;

    public TalonOneService() {
        this.iTalonOne = new Retrofit.Builder()
                .baseUrl(Enviroment.URL_TALON_ONE)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ITalonOne.class);
    }

    public List<ComboDetail> sendOrderComboToTalonOne(List<DeliveryOrderItem> deliveryOrderItemList) {
        deliveryOrderItemList.forEach(item -> {
            LOG.warning("COMBO VALIDATION: initial processing: item: " + item.getId()  +  " SUBCLASS " + item.getRms_subclass() + " CLASS " + item.getRms_class());
        });
        boolean hasCategoryOrSubCategory = deliveryOrderItemList.stream()
                .anyMatch(item -> (Objects.nonNull(item.getRms_class()) && Constants.COMBO_VIRTUAL.trim().equals(item.getRms_class().trim()))
                        || (Objects.nonNull(item.getRms_subclass()) && Constants.COMBO_VIRTUAL.trim().equals(item.getRms_subclass().trim())));
        List<ComboDetail> itemsCombo = new ArrayList<>();
        if (!hasCategoryOrSubCategory){

            for ( DeliveryOrderItem deliveryOrderItem : deliveryOrderItemList) {
                LOG.warning("COMBO VALIDATION: processing: item: " + deliveryOrderItem.getId()  +  " SUBCLASS " + deliveryOrderItem.getRms_subclass() + " CLASS " + deliveryOrderItem.getRms_class());
                try{
                    ItemAlgolia algoliaItem =  APIAlgolia.getItemAlgolia(deliveryOrderItem.getId() + Constants.DEFAULT_STORE_CO.toString());
                    if (algoliaItem != null && (
                            (algoliaItem.getRms_class() != null  && Constants.COMBO_VIRTUAL.trim().equals(algoliaItem.getRms_class()))
                                    ||
                                    (algoliaItem.getRms_subclass() != null && Constants.COMBO_VIRTUAL.trim().equals(algoliaItem.getRms_subclass()))
                    ))  {
                        LOG.warning("COMBO VALIDATION: ALGOLIA VALIDATION: SUBCLASS" + deliveryOrderItem.getRms_subclass() + " CLASS " + deliveryOrderItem.getRms_class());
                        hasCategoryOrSubCategory = true;
                    }
                } catch (Exception e){
                    LOG.warning("COMBO VALIDATION:  UNEXPECTED ERROR: " + e.getMessage());
                }

            }
        }

        if (APIAlgolia.isTalonActive() && hasCategoryOrSubCategory) {
            if (!deliveryOrderItemList.isEmpty()) {
                deliveryOrderItemList = deliveryOrderItemList.stream().sorted(Comparator.comparing(DeliveryOrderItem::getId)).collect(Collectors.toList());
                CustomerSessionRequest customerSessionRequest = new CustomerSessionRequest();
                customerSessionRequest.setProfileId(UUID.randomUUID().toString());
                customerSessionRequest.setEmail(Constants.EMAIL_COMBO);
                customerSessionRequest.setState(Constants.OPEN_STATUS_TALON_ONE);
                customerSessionRequest.setCartItems(new ArrayList<>());
                deliveryOrderItemList.forEach(item -> {
                    CartItem cartItem = getCartItem(item);
                    customerSessionRequest.getCartItems().add(cartItem);
                });
                String sessionId = String.valueOf(UUID.randomUUID().toString());
                Call<CustomerSessionResponse> call = iTalonOne.createCustomerSession(sessionId, customerSessionRequest, new HashMap<>());
                try {
                    Response<CustomerSessionResponse> response = call.execute();
                    CustomerSessionResponse customerSessionResponse = response.body();
                    if (customerSessionResponse != null) {
                        setItemsComboIfExist(customerSessionResponse, itemsCombo);
                    }
                } catch (IOException e) {
                    LOG.warning("Error sending order to TalonOne: " + Arrays.toString(e.getStackTrace()));
                }
            }
            return itemsCombo;
        }
        return itemsCombo;
    }

    private static void setItemsComboIfExist(CustomerSessionResponse customerSessionResponse, List<ComboDetail> comboDetails) {
        // Mapas para almacenar ItemsCombo y ComboSku por sufijo
        Map<String, String> itemsComboMap = new HashMap<>();
        Map<String, String> comboSkuMap = new HashMap<>();

        // Definir patrones como constantes
        final String ITEMS_COMBO_KEY = "ItemsCombo";
        final String COMBO_SKU_KEY = "ComboSku";
        final String COMBO_PATTERN = "(\\d+):(\\d+)";

        for (Map.Entry<String, String> entry : customerSessionResponse.getAttributes().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            // Dividir la clave en base y sufijo
            String[] keyParts = key.split("_", 2); // Dividir en máximo 2 partes
            String keyBase = keyParts[0];
            String suffix = keyParts.length > 1 ? keyParts[1] : "";

            if (ITEMS_COMBO_KEY.equals(keyBase)) {
                itemsComboMap.put(suffix, value);
            } else if (COMBO_SKU_KEY.equals(keyBase)) {
                comboSkuMap.put(suffix, value.trim());
            }
        }

        // Asociar ItemsCombo con ComboSku utilizando el sufijo
        for (String suffix : itemsComboMap.keySet()) {
            String itemsComboValue = itemsComboMap.get(suffix);
            String comboSkuValue = comboSkuMap.get(suffix);

            if (comboSkuValue == null) {
                LOG.warning("No se encontró ComboSku para el sufijo: " + suffix);
                continue;
            }

            List<ItemCombo> itemsComboList = new ArrayList<>();
            String[] combos = itemsComboValue.split(",");
            for (String combo : combos) {
                Matcher matcher = Pattern.compile(COMBO_PATTERN).matcher(combo.trim());
                if (matcher.matches()) {
                    ItemCombo itemCombo = new ItemCombo();
                    itemCombo.setId(matcher.group(1));
                    itemCombo.setQuantity(matcher.group(2));
                    itemsComboList.add(itemCombo);
                } else {
                    LOG.warning("Formato de combo inválido: " + combo);
                }
            }

            // Crear un ComboDetail y agregarlo a la lista
            ComboDetail comboDetail = new ComboDetail(comboSkuValue, itemsComboList);
            comboDetails.add(comboDetail);
        }
    }

    public DeliveryOrder sendOrderToTalonOne(DeliveryOrder deliveryOrder, ShoppingCartJson shoppingCartJson, AnswerDeduct coupon) {
        if (APIAlgolia.isTalonActive() && deliveryOrder != null && shoppingCartJson != null) {
            this.userPrime = validateUserPrime((long) shoppingCartJson.getId());
            final Optional<String> uuidBraze = getUuidBraze(shoppingCartJson.getId());
            if (uuidBraze.isPresent() && deliveryOrder.getItemList() != null) {
                deliveryOrder.setItemList(deliveryOrder.getItemList().stream().sorted(Comparator.comparing(DeliveryOrderItem::getId)).collect(Collectors.toList()));
                CustomerSessionRequest customerSessionRequest = getCustomerSessionRequest(deliveryOrder, uuidBraze, shoppingCartJson, null);
                Gson gson = new Gson();
                String requestText = gson.toJson(customerSessionRequest);
                String sessionId = String.valueOf(shoppingCartJson.getId());
                Optional<String> requestCache = CachedDataManager.getJsonFromCacheIndex(sessionId + Constants.KEY_REQUEST_TALON, Constants.INDEX_REDIS_FOURTEEN);
                if (requestCache.isPresent() && requestCache.get().equals(requestText)) {
                    Optional<String> response = CachedDataManager.getJsonFromCacheIndex(sessionId + Constants.KEY_RESPONSE_TALON, Constants.INDEX_REDIS_FOURTEEN);
                    CustomerSessionResponse customerSessionResponse = gson.fromJson(response.get(), CustomerSessionResponse.class);
                    deliveryOrder = getDeliveryOrderWithDiscountsTalon(deliveryOrder, customerSessionResponse, shoppingCartJson.getIdCustomerWebSafe());
                } else {
                    LOG.info("customerSessionRequest: " + new Gson().toJson(customerSessionRequest));
                    Call<CustomerSessionResponse> call = iTalonOne.createCustomerSession(sessionId, customerSessionRequest, new HashMap<>());
                    try {
                        Response<CustomerSessionResponse> response = call.execute();
                        CustomerSessionResponse customerSessionResponse = response.body();
                        LOG.info("customerSessionResponse response: " + new Gson().toJson(customerSessionResponse));

                        deliveryOrder.setTalonOneItemFree(false);
                        deliveryOrder = getDeliveryOrderWithDiscountsTalon(deliveryOrder, customerSessionResponse, shoppingCartJson.getIdCustomerWebSafe());
                        LOG.info("couponAutomatic: " + new Gson().toJson(deliveryOrder.getCouponAutomaticTalonList()));
                        if (Objects.isNull(coupon) && Objects.nonNull(deliveryOrder.getCouponAutomaticTalonList()) && deliveryOrder.getCouponAutomaticTalonList().isEmpty()) {
                            final int timeExpired = 900;
                            CachedDataManager.saveJsonInCacheIndexTime(sessionId + Constants.KEY_REQUEST_TALON, requestText, Constants.INDEX_REDIS_FOURTEEN, timeExpired);
                            CachedDataManager.saveJsonInCacheIndexTime(sessionId + Constants.KEY_RESPONSE_TALON, gson.toJson(customerSessionResponse), Constants.INDEX_REDIS_FOURTEEN, timeExpired);
                        }
                    } catch (IOException e) {
                        LOG.warning("Error sending order to TalonOne: " + Arrays.toString(e.getStackTrace()));
                    }
                }
            }
            if (uuidBraze.isPresent() && Objects.nonNull(deliveryOrder.getProviderList()) && !deliveryOrder.getProviderList().isEmpty()) {
                for (DeliveryOrderProvider provider : deliveryOrder.getProviderList()) {
                    DeliveryOrder deliveryOrderProductsOnline = new DeliveryOrder();
                    List<DeliveryOrderItem> itemListAllProducts = new ArrayList<>(provider.getItemList());

                    deliveryOrderProductsOnline.setItemList(itemListAllProducts.stream().sorted(Comparator.comparing(DeliveryOrderItem::getId)).collect(Collectors.toList()));
                    CustomerSessionRequest customerSessionRequestProductsOnline = getCustomerSessionRequest(deliveryOrderProductsOnline, uuidBraze, shoppingCartJson, null);
                    Gson gson = new Gson();
                    String requestTextProductOnline = gson.toJson(customerSessionRequestProductsOnline);
                    String sessionIdProductOnline = String.valueOf(shoppingCartJson.getId());
                    Optional<String> requestCache = CachedDataManager.getJsonFromCacheIndex(sessionIdProductOnline + "request-po", 14);
                    if (requestCache.isPresent() && requestCache.get().equals(requestTextProductOnline)) {
                        Optional<String> response = CachedDataManager.getJsonFromCacheIndex(sessionIdProductOnline + "response-po", 14);
                        CustomerSessionResponse customerSessionResponse = gson.fromJson(response.get(), CustomerSessionResponse.class);
                        LOG.info("customerSessionResponse cache: " + new Gson().toJson(customerSessionResponse));
                        deliveryOrderProductsOnline = getDeliveryOrderWithDiscountsTalon(deliveryOrderProductsOnline, customerSessionResponse,shoppingCartJson.getIdCustomerWebSafe());
                    }else {
                        Call<CustomerSessionResponse> call = iTalonOne.createCustomerSession(sessionIdProductOnline, customerSessionRequestProductsOnline, new HashMap<>());
                        try {
                            Response<CustomerSessionResponse> response = call.execute();
                            CustomerSessionResponse customerSessionResponse = response.body();
                            LOG.info("customerSessionResponse talon: " + new Gson().toJson(customerSessionResponse));
                            CachedDataManager.saveJsonInCacheIndexTime(sessionIdProductOnline + "request-po", requestTextProductOnline, 14, 900);
                            CachedDataManager.saveJsonInCacheIndexTime(sessionIdProductOnline + "response-po", gson.toJson(customerSessionResponse), 14, 900);
                            deliveryOrderProductsOnline = getDeliveryOrderWithDiscountsTalon(deliveryOrderProductsOnline, customerSessionResponse, shoppingCartJson.getIdCustomerWebSafe());
                        } catch (IOException e) {
                            LOG.warning("Error sending order to TalonOne: " + Arrays.toString(e.getStackTrace()));
                        }
                    }
                    provider.setItemList(deliveryOrderProductsOnline.getItemList());
                }
            }
            LOG.info("deliveryOrder: " + new Gson().toJson(deliveryOrder));
        } else {
            LOG.info("Talon One is disabled");
        }
        LOG.info("deliveryOrder after talon: " + new Gson().toJson(deliveryOrder));
        return deliveryOrder;
    }

    private DeliveryOrder getDeliveryOrderWithDiscountsTalon(DeliveryOrder deliveryOrder, CustomerSessionResponse customerSessionResponse, String idCustomerWebSafe) {
        Integer NUM_BD_REDIS = 14;
        CachedDataManager.deleteKeyIndex(idCustomerWebSafe + "_AUTOMATIC_COUPON", NUM_BD_REDIS);

        if (customerSessionResponse != null) {
            if (customerSessionResponse.hasEffects()) {
                Double totalDiscount = 0.0;
                Optional<Effect> objectsAcceptCoupon = filterFirstAcceptCoupon(customerSessionResponse);
                Optional<Effect> objectsRejectCoupon = filterFirstRejectCouponTalon(customerSessionResponse);
                Optional<Effect> allObjectsRejectCoupon = filterFirstRejectCoupon(customerSessionResponse);
                List<Effect> objectsNotification = filterByNotificationsCouponsTalon(customerSessionResponse);
                List<CouponAutomaticTalon> couponAutomaticTalonList = new ArrayList<>();
                com.imaginamos.farmatodo.model.talonone.Coupon couponTalon = new com.imaginamos.farmatodo.model.talonone.Coupon();
                for (Effect effect : customerSessionResponse.getEffects()) {
                    deliveryOrder = getCouponAutomatic(deliveryOrder, effect, objectsNotification, couponAutomaticTalonList);
                    mapNotificationError(deliveryOrder, effect, objectsNotification, objectsRejectCoupon, couponAutomaticTalonList, idCustomerWebSafe);
                    validRejectCouponTalon(deliveryOrder, effect, allObjectsRejectCoupon);
                    if (effect.isDiscountPerItem()) {
                        talonOneDiscountItem(deliveryOrder, effect, totalDiscount);
                    }
                    if (effect.isItemFree()) {
                        deliveryOrder.setTalonOneItemFree(true);
                    }

                    hasCouponTalonValid(deliveryOrder, effect, objectsAcceptCoupon, couponTalon, idCustomerWebSafe);
                }
                if (Objects.nonNull(couponTalon) && Objects.nonNull(couponTalon.getNameCoupon())) {
                    savedCouponTalonInRedis(couponTalon, idCustomerWebSafe);
                }
                couponAutomaticTalonList = getCouponAutomaticTalonHigher(couponAutomaticTalonList);
                deliveryOrder.setCouponAutomaticTalonList(couponAutomaticTalonList);
                saveInRedisCouponAutomatic(idCustomerWebSafe, couponAutomaticTalonList, NUM_BD_REDIS);
            }
            applyCredits(deliveryOrder, customerSessionResponse.getUsedCredits());
        }
        setPrimeFlag(deliveryOrder, this.userPrime);
        return deliveryOrder;
    }

    private void setPrimeFlag(DeliveryOrder deliveryOrder, boolean isUserPrime) {
        deliveryOrder.setPrimeDiscountFlag(false);
        if (isUserPrime) {
            for (DeliveryOrderItem item : deliveryOrder.getItemList()) {
                if (Objects.nonNull(item.getPrimePrice()) && item.getPrimePrice() > 0) {
                    deliveryOrder.setPrimeDiscountFlag(true);
                }
            }
        }
    }

    @NotNull
    private static List<CouponAutomaticTalon> getCouponAutomaticTalonHigher(List<CouponAutomaticTalon> couponAutomaticTalonList) {
        if (!couponAutomaticTalonList.isEmpty() && Objects.nonNull(couponAutomaticTalonList) && Objects.nonNull(couponAutomaticTalonList.get(0).getDiscountCoupon())) {
            Double higherDiscount = couponAutomaticTalonList.stream().filter(coupon -> coupon.getDiscountCoupon() > 0).map(CouponAutomaticTalon::getDiscountCoupon).max(Double::compareTo).orElse(0.0);
            couponAutomaticTalonList = couponAutomaticTalonList.stream().filter(coupon -> coupon.getDiscountCoupon() == higherDiscount).collect(Collectors.toList());
        }
        //code by notifications
        if (!couponAutomaticTalonList.isEmpty() && Objects.nonNull(couponAutomaticTalonList)) {
            CouponAutomaticTalon couponAutomaticTalonLast = couponAutomaticTalonList.get(couponAutomaticTalonList.size() - 1);
            couponAutomaticTalonList.clear();
            couponAutomaticTalonList.add(couponAutomaticTalonLast);
        }
        return couponAutomaticTalonList;
    }

    @NotNull
    private static List<Effect> filterByNotificationsCouponsTalon(CustomerSessionResponse customerSessionResponse) {
        List<Effect> objectsNotification = customerSessionResponse.getEffects().stream()
                .filter(effect -> effect.getEffectType().equals("showNotification") && Objects.isNull(effect.getTriggeredByCoupon()))
                .collect(Collectors.toList());
        return objectsNotification;
    }

    @NotNull
    private static Optional<Effect> filterFirstRejectCoupon(CustomerSessionResponse customerSessionResponse) {
        Optional<Effect> allObjectsRejectCoupon = customerSessionResponse.getEffects().stream()
                .filter(effect -> effect.getEffectType().equals("rejectCoupon"))
                .findFirst();
        return allObjectsRejectCoupon;
    }

    @NotNull
    private static Optional<Effect> filterFirstRejectCouponTalon(CustomerSessionResponse customerSessionResponse) {
        Optional<Effect> objectsRejectCoupon = customerSessionResponse.getEffects().stream()
                .filter(effect -> effect.getEffectType().equals("rejectCoupon") && Objects.nonNull(effect.getTriggeredByCoupon()))
                .findFirst();
        return objectsRejectCoupon;
    }

    @NotNull
    private static Optional<Effect> filterFirstAcceptCoupon(CustomerSessionResponse customerSessionResponse) {
        Optional<Effect> objectsAcceptCoupon = customerSessionResponse.getEffects().stream()
                .filter(effect -> effect.getEffectType().equals("acceptCoupon") && Objects.nonNull(effect.getTriggeredByCoupon()))
                .findFirst();
        return objectsAcceptCoupon;
    }

    private static void validRejectCouponTalon(DeliveryOrder deliveryOrder, Effect effect, Optional<Effect> objectsRejectCoupon) {
        if (objectsRejectCoupon.isPresent()
                && effect.getCampaignId().equals(objectsRejectCoupon.get().getCampaignId())
                && !objectsRejectCoupon.get().getProps().get("rejectionReason").equalsIgnoreCase("CouponRejectedByCondition")
                && !objectsRejectCoupon.get().getProps().get("rejectionReason").equalsIgnoreCase("CouponNotFound")) {
            com.imaginamos.farmatodo.model.talonone.Coupon couponError = new com.imaginamos.farmatodo.model.talonone.Coupon();
            couponError.setNameCoupon(!objectsRejectCoupon.get().getRuleName().isEmpty() ? objectsRejectCoupon.get().getRuleName() : "");
            deliveryOrder.setCoupon(couponError);
            LOG.info("Cupon rechazado por: " + objectsRejectCoupon.get().getProps().get("rejectionReason"));
        }
    }

    private static void saveInRedisCouponAutomatic(String idCustomerWebSafe, List<CouponAutomaticTalon> couponAutomaticTalonList, Integer NUM_BD_REDIS) {
        if (Objects.nonNull(idCustomerWebSafe) && couponAutomaticTalonList.size() > 0) {
            Integer TIME_EXPIRED_IN_SECOND = 600;
            CachedDataManager.deleteKeyIndex(idCustomerWebSafe + "_AUTOMATIC_COUPON", NUM_BD_REDIS);
            CachedDataManager.saveJsonInCacheIndexTime(idCustomerWebSafe + "_AUTOMATIC_COUPON", new Gson().toJson(couponAutomaticTalonList), NUM_BD_REDIS, TIME_EXPIRED_IN_SECOND);
        }
    }

    private DeliveryOrder getCouponAutomatic(DeliveryOrder deliveryOrder,
                                             Effect effect,
                                             List<Effect> objectsNotification,
                                             List<CouponAutomaticTalon> couponAutomaticTalonList) {
        if (effect.isDiscountPerCouponAutomatic()) {
            deliveryOrder = talonOneDiscount(deliveryOrder, effect);
            CouponAutomaticTalon couponAutomaticTalon = new CouponAutomaticTalon();
            couponAutomaticTalon.setNameCoupon(!effect.getRuleName().isEmpty() ? effect.getRuleName() : "");
            couponAutomaticTalon.setDiscountCoupon(effect.getValueDiscount());
            if (Objects.nonNull(objectsNotification) && !objectsNotification.isEmpty()) {
                objectsNotification.stream().forEach(notification -> {
                    if (notification.getCampaignId().equals(effect.getCampaignId())) {
                        couponAutomaticTalon.setTalonOneOfferDescription(notification.getProps().get("body"));
                        couponAutomaticTalon.setTypeNotificacion(notification.getProps().get("notificationType"));
                    }
                });
            }
            couponAutomaticTalonList.add(couponAutomaticTalon);
        }
        return deliveryOrder;
    }

    private static void mapNotificationError(DeliveryOrder deliveryOrder,
                                             Effect effect, List<Effect> objectsNotification,
                                             Optional<Effect> objectsRejectCoupon,
                                             List<CouponAutomaticTalon> couponAutomaticTalonList,
                                             String idCustomerWebSafe) {
        if (effect.hasNotificationError()) {
            CachedDataManager.deleteKeyIndex(idCustomerWebSafe + Constants.KEY_COUPON_CACHE, Constants.INDEX_REDIS_FOURTEEN);
            DeliveryOrder finalDeliveryOrder = deliveryOrder;
            if (Objects.nonNull(objectsNotification) && !objectsNotification.isEmpty()) {
                objectsNotification.stream().forEach(notification -> {
                    if (objectsRejectCoupon.isPresent()) {
                        if (notification.getCampaignId().equals(effect.getCampaignId())
                                && effect.getCampaignId().equals(objectsRejectCoupon.get().getCampaignId())) {
                            com.imaginamos.farmatodo.model.talonone.Coupon couponError = new com.imaginamos.farmatodo.model.talonone.Coupon();
                            couponError.setNameCoupon(!objectsRejectCoupon.get().getRuleName().isEmpty() ? objectsRejectCoupon.get().getRuleName() : "");
                            couponError.setTalonOneOfferDescription(notification.getProps().get("body"));
                            couponError.setTypeNotificacion(notification.getProps().get("notificationType"));
                            finalDeliveryOrder.setCoupon(couponError);
                        }
                        if (notification.getCampaignId().equals(effect.getCampaignId()) && !effect.getCampaignId().equals(objectsRejectCoupon.get().getCampaignId())) {
                            createCouponAutomaticMessageError(effect, couponAutomaticTalonList, notification);
                        }
                    } else if (notification.getCampaignId().equals(effect.getCampaignId())) {
                        createCouponAutomaticMessageError(effect, couponAutomaticTalonList, notification);
                    }
                });
            }
        }
    }

    private static void createCouponAutomaticMessageError(Effect effect, List<CouponAutomaticTalon> couponAutomaticTalonList, Effect notification) {
        CouponAutomaticTalon couponAutomaticTalon = new CouponAutomaticTalon();
        couponAutomaticTalon.setNameCoupon(!effect.getRuleName().isEmpty() ? effect.getRuleName() : "");
        couponAutomaticTalon.setTalonOneOfferDescription(notification.getProps().get("body"));
        couponAutomaticTalon.setTypeNotificacion(notification.getProps().get("notificationType"));
        couponAutomaticTalonList.add(couponAutomaticTalon);
    }

    private static void hasCouponTalonValid(DeliveryOrder deliveryOrder,
                                            Effect effect,
                                            Optional<Effect> objectsAcceptCoupon,
                                            com.imaginamos.farmatodo.model.talonone.Coupon couponTalon,
                                            String idCustomerWebSafe) {
        if (objectsAcceptCoupon.isPresent()) {
            if (effect.getEffectType().equals("setDiscount") &&
                    objectsAcceptCoupon.get().getCampaignId().equals(effect.getCampaignId()) &&
                    Objects.nonNull(effect.getTriggeredByCoupon())) {
                couponTalon.setNameCoupon(effect.getRuleName());
                couponTalon.setDiscountCoupon(Double.parseDouble(effect.getProps().get("value")));
            }
            if (effect.getEffectType().equals("showNotification") && objectsAcceptCoupon.get().getCampaignId().equals(effect.getCampaignId())) {
                couponTalon.setTalonOneOfferDescription(effect.getProps().get("body"));
                couponTalon.setTypeNotificacion(effect.getProps().get("notificationType"));
            }
            deliveryOrder.setCoupon(couponTalon);
        }
    }

    private static void savedCouponTalonInRedis(com.imaginamos.farmatodo.model.talonone.Coupon couponTalon, String idCustomerWebSafe) {
        String keyCache = idCustomerWebSafe + Constants.KEY_COUPON_CACHE;
        AnswerDeduct answerDeduct = new AnswerDeduct();
        answerDeduct.setNameCoupon(couponTalon.getNameCoupon());
        answerDeduct.setDiscount(couponTalon.getDiscountCoupon());
        answerDeduct.setTypeNotifcation(couponTalon.getTypeNotificacion());
        answerDeduct.setNotificationMessage(couponTalon.getTalonOneOfferDescription());
        String bodyCache = new Gson().toJson(answerDeduct);
        CachedDataManager.saveJsonInCacheIndexTime(keyCache, bodyCache, Constants.INDEX_REDIS_FOURTEEN, Constants.TIME_EXPIRE_IN_SECONDS);
    }

    private Optional<String> getUuidBraze(int userId) {
        Optional<String> uuidCache = CachedDataManager.getUiidBrazeFromCache(String.valueOf(userId));
        if (uuidCache.isPresent()) {
            return uuidCache;
        }
        Optional<CustomerJSON> customerJSON = ApiGatewayService.get().getCustomerById(userId);
        Optional<String> uuidBraze = Optional.empty();
        if (customerJSON.isPresent()) {
            final String email = customerJSON.get().getEmail();
            uuidBraze = ApiGatewayService.get().getUUIDFromBraze(email);
            if (uuidBraze.isPresent()) {
                CachedDataManager.saveUiidBrazeInCache(String.valueOf(userId), uuidBraze.get());
            }
        }
        return uuidBraze;
    }

    @NotNull
    private CustomerSessionRequest getCustomerSessionRequest(DeliveryOrder deliveryOrder, Optional<String> uuidBraze, ShoppingCartJson shoppingCartJson, Coupon coupon) {
        int totalFullPrice = 0;
        int totalWithDiscount = 0;
        boolean isUserPrime = false;
        if (Objects.nonNull(deliveryOrder.getIdFarmatodo()))
            isUserPrime = validateUserPrime((long) deliveryOrder.getIdFarmatodo());


        CustomerSessionRequest customerSessionRequest = new CustomerSessionRequest();
        customerSessionRequest.setProfileId(uuidBraze.get());
        customerSessionRequest.setState("OPEN");

        if (Objects.nonNull(shoppingCartJson) && Objects.nonNull(shoppingCartJson.getFarmaCredits())) {
            customerSessionRequest.setFarmaCredits(shoppingCartJson.getFarmaCredits());
        }

        if (Objects.isNull(shoppingCartJson)) {
            customerSessionRequest.setPaymentCardId(DEFAULT_PAYMENT_CARD_ID);
        } else {
            Integer paymentCardId = Optional.ofNullable(shoppingCartJson.getPaymentCardId()).orElse(DEFAULT_PAYMENT_CARD_ID);
            customerSessionRequest.setPaymentCardId(paymentCardId);
        }

        if (Objects.nonNull(shoppingCartJson) && Objects.nonNull(shoppingCartJson.getTalonOneData())) {
            customerSessionRequest.setTalonOneData(shoppingCartJson.getTalonOneData());
        }

        List<String> coupons = new ArrayList<>();
        if (Objects.nonNull(coupon)) {
            coupons.add(coupon.getName());
            customerSessionRequest.setCouponCodes(coupons);
        }
        LOG.info("customerSessionRequest0: " + new Gson().toJson(customerSessionRequest));
        LinkedTreeMap<String, Object> attributesSession = new LinkedTreeMap<>();
        List<CartItem> cartItems = new ArrayList<>();
        if (deliveryOrder.getItemList() != null) {
            for (DeliveryOrderItem item : deliveryOrder.getItemList()) {
                totalFullPrice += (int) (item.getFullPrice() * item.getQuantitySold());
                if (isUserPrime) {
                    totalWithDiscount += (int) ((item.getPrimePrice() == 0 ? item.getFullPrice() : item.getPrimePrice()) * item.getQuantitySold());
                } else
                    totalWithDiscount += (int) ((item.getOfferPrice() == 0 ? item.getFullPrice() : item.getOfferPrice()) * item.getQuantitySold());

                cartItems.add(getCartItem(item));
            }
        }
        addSourceToCustomerSessionRequest(deliveryOrder, shoppingCartJson, customerSessionRequest, attributesSession, totalFullPrice, totalWithDiscount);
        customerSessionRequest.setCartItems(cartItems);
        LOG.info("customerSessionRequest2: " + new Gson().toJson(customerSessionRequest));
        return customerSessionRequest;
    }

    @NotNull
    private CartItem getCartItem(com.imaginamos.farmatodo.model.intefaces.Item item) {
        CartItem cartItem = new CartItem();
        cartItem.setName(item.getMediaDescription());
        cartItem.setSku(String.valueOf(item.getId()));
        cartItem.setQuantity(item.getQuantitySold() == 0 ? 1 : item.getQuantitySold());
        cartItem.setPrice(item.getFullPrice());
        cartItem.setCategory(item.getCategorie());
        LinkedTreeMap<String, String> attributes = new LinkedTreeMap<>();
        attributes.put("Brand", Objects.nonNull(item.getMarca()) ? item.getMarca() : item.getBrand());
        attributes.put("SubCategory", Objects.nonNull(item.getSubCategory()) ? item.getSubCategory() : "");
        cartItem.setAttributes(attributes);
        return cartItem;
    }

    private void addSourceToCustomerSessionRequest(DeliveryOrder deliveryOrder, ShoppingCartJson shoppingCartJson, CustomerSessionRequest customerSessionRequest, LinkedTreeMap<String, Object> attributesSession,
                                                   int totalFullPrice, int totalWithDiscount) {
        if (Objects.nonNull(shoppingCartJson)) {
            if (Objects.nonNull(shoppingCartJson.getSource()) && shoppingCartJson.getSource() != "")
                attributesSession.put("source", shoppingCartJson.getSource());
            else if (Objects.nonNull(deliveryOrder.getSource()) && deliveryOrder.getSource() != "")
                attributesSession.put("source", deliveryOrder.getSource());
            if (Objects.nonNull(deliveryOrder.getDeliveryType()))
                attributesSession.put("deliveryType", deliveryOrder.getDeliveryType().name());
            else if (Objects.nonNull(shoppingCartJson.getDeliveryType()))
                attributesSession.put("deliveryType", shoppingCartJson.getDeliveryType().name());

            attributesSession.put("totalFullPrice", totalFullPrice);
            attributesSession.put("totalWithDiscount", totalWithDiscount);
            customerSessionRequest.setAttributes(attributesSession);
        }
    }

    private Double talonOneDiscountItem(DeliveryOrder deliveryOrder, Effect effect, Double totalDiscount) {
        if (deliveryOrder.getItemList() != null) {
            Double count = 0.0;
            List<DeliveryOrderItem> itemList = new ArrayList<>();

            for (DeliveryOrderItem item : deliveryOrder.getItemList()) {
                BestOfferEnum bestOffer = getBestDealV1(item.getPrimePrice(), item.getFullPrice() - Double.valueOf(effect.getProps().get("value")), item.getOfferPrice(), this.userPrime);

                if (effect.getProps().get("position").equals(String.valueOf(count)) && Double.valueOf(effect.getProps().get("value")) > 0D && bestOffer.equals(BestOfferEnum.TALON_ONE)) {
                    totalDiscount = getTotalDiscount(totalDiscount, (Double.valueOf(effect.getProps().get("value")) * item.getQuantitySold()));
                    item = getDeliveryOrderItem(effect, item);
                    if (!validatePrimeEffect(effect) && this.userPrime) {
                        item = setDefaultPetal(item);
                    }

                }

                if (bestOffer.equals(BestOfferEnum.RPM) && this.userPrime) {
                    item = setDefaultPetal(item);
                }

                itemList.add(item);
                count += 1.0;
            }
        }
        return totalDiscount;
    }

    private DeliveryOrderItem setDefaultPetal(DeliveryOrderItem item) {
        item.setPrimePrice(0.0);
        item.setPrimeTextDiscount("");
        item.setPrimeDescription("");
        return item;
    }

    private Item setDefaultPetal(com.imaginamos.farmatodo.model.product.Item item) {
        item.setPrimePrice(0.0);
        item.setPrimeTextDiscount("");
        item.setPrimeDescription("");
        return item;
    }

    /**
     * This method returns the best offer for the client, comparing the discounts of the RPM, Prime and TalonOne offers.
     *
     * @param primeDiscount
     * @param talonDiscount
     * @param rpmDiscount
     * @param isUserPrime
     * @return BestOfferEnum
     */
    private BestOfferEnum getBestDealV1(Double primeDiscount, Double talonDiscount, Double rpmDiscount, Boolean isUserPrime) {
        boolean existsRPMDiscount = Objects.nonNull(rpmDiscount) && !(rpmDiscount.isNaN()) && rpmDiscount > 0D;
        boolean existsPrimeDiscount = Objects.nonNull(primeDiscount) && !(primeDiscount.isNaN()) && primeDiscount > 0D;

        if (existsPrimeDiscount && isUserPrime) {
            if (existsRPMDiscount) {
                boolean isPrimeDiscountBest = primeDiscount <= rpmDiscount && primeDiscount <= talonDiscount;
                if (isPrimeDiscountBest)
                    return BestOfferEnum.PRIME;
            } else {
                if ((primeDiscount <= talonDiscount) && ((talonDiscount - primeDiscount) > 0.1))
                    return BestOfferEnum.PRIME;
            }
        }

        if (existsRPMDiscount) {
            boolean isRPMDiscountBest = (rpmDiscount < talonDiscount) && ((talonDiscount - rpmDiscount) > 0.1);
            if (isRPMDiscountBest)
                return BestOfferEnum.RPM;
        }

        return BestOfferEnum.TALON_ONE;
    }

    @NotNull
    private DeliveryOrderItem getDeliveryOrderItem(Effect effect, DeliveryOrderItem item) {
        setItemValues(item, effect);
        return item;
    }

    private void setItemValues(DeliveryOrderItem item, Effect effect) {
        int percentage = (int) Math.round((Double.valueOf(effect.getProps().get("value")) * 100) / item.getFullPrice());
        PetalComplexOffer valComplexOffer = validateComplexOffer(effect, percentage);
        if (validatePrimeEffect(effect)) {
            item.setPrimeTextDiscount(valComplexOffer.getOfferText());
            item.setPrimeDescription(valComplexOffer.getOfferDescription());
            item.setPrimePrice(item.getFullPrice() - Double.valueOf(effect.getProps().get("value")));
        } else {
            item.setOfferText(valComplexOffer.getOfferText());
            item.setOfferDescription(valComplexOffer.getOfferDescription());
            item.setOfferPrice(item.getFullPrice() - Double.valueOf(effect.getProps().get("value")));
            item.setTalonDiscount(true);
        }
    }

    private void setItemValues(Item item, Effect effect) {
        int percentage = (int) Math.round((Double.valueOf(effect.getProps().get("value")) * 100) / item.getFullPrice());
        PetalComplexOffer valComplexOffer = validateComplexOffer(effect, percentage);
        if (validatePrimeEffect(effect)) {
            item.setPrimeTextDiscount(valComplexOffer.getOfferText());
            item.setPrimeDescription(valComplexOffer.getOfferDescription());
            item.setPrimePrice(item.getFullPrice() - Double.valueOf(effect.getProps().get("value")));
        } else {
            item.setOfferText(valComplexOffer.getOfferText());
            item.setOfferDescription(valComplexOffer.getOfferDescription());
            item.setOfferPrice(item.getFullPrice() - Double.valueOf(effect.getProps().get("value")));
        }

    }

    private static PetalComplexOffer validateComplexOffer(Effect effect, int percentage) {
        PetalComplexOffer petalComplexOffer = new PetalComplexOffer();
        Matcher matcher = isMatcherPattern(effect.getRuleName(), Constants.PATTERN_COMPLEX_OFFER);
        if (matcher.find()) {
            petalComplexOffer.setComplexOffer(true);
            petalComplexOffer.setOfferText(matcher.group(1));
            petalComplexOffer.setOfferDescription(effect.getRuleName().replaceAll(Constants.PATTERN_COMPLEX_OFFER, ""));
        } else {
            petalComplexOffer.setComplexOffer(false);
            petalComplexOffer.setOfferText(percentage + "%");
            petalComplexOffer.setOfferDescription(effect.getRuleName());
        }
        return petalComplexOffer;
    }

    private static Matcher isMatcherPattern(String textToValidate, String pattern) {
        Pattern patternIn = Pattern.compile(pattern);
        return patternIn.matcher(textToValidate);
    }

    @NotNull
    private Double getTotalDiscount(Double totalDiscount, double effect) {
        totalDiscount += effect;
        return totalDiscount;
    }

    private DeliveryOrder talonOneDiscount(DeliveryOrder deliveryOrder, Effect effect) {
        if (!deliveryOrder.isTalonOneDiscount()) {
            if (Double.parseDouble(effect.getProps().get("value")) > deliveryOrder.getOfferPrice()) {
                deliveryOrder.setOfferPrice(Double.parseDouble(effect.getProps().get("value")));
                deliveryOrder.setTotalPrice(deliveryOrder.getTotalPrice() - Double.parseDouble(effect.getProps().get("value")));
                deliveryOrder.setTalonOneDiscount(true);
            }
        } else {
            deliveryOrder.setOfferPrice(Double.parseDouble(effect.getProps().get("value")));
            deliveryOrder.setTotalPrice(deliveryOrder.getTotalPrice() - Double.parseDouble(effect.getProps().get("value")));
        }
        return deliveryOrder;
    }

    private DeliveryOrder applyCredits (DeliveryOrder deliveryOrder, Long usedCredits) {
        if (Objects.nonNull(usedCredits) && usedCredits > 0) {
             deliveryOrder.setUsedCredits(usedCredits.doubleValue());
        }
        return deliveryOrder;
    }

    public void copyClosedSession(String sessionId, String newSessionId, CreatedOrder order, String idCustomerWebSafe) {
        if (APIAlgolia.isTalonActive()) {
            deleteCacheSession(sessionId);
            Call<CustomerSessionResponse> call = iTalonOne.copyCustomerSession(sessionId, newSessionId, "CLOSED", new HashMap<>());
            try {
                Response<CustomerSessionResponse> response = call.execute();

                CustomerSessionResponse customerSessionResponse = response.body();

                if (Objects.nonNull(customerSessionResponse)) {
                    saveDiscountCampaignTalonInBD(newSessionId, order, customerSessionResponse);
                    saveDeductDiscountOrderinBD(newSessionId, idCustomerWebSafe);
                    deleteUsedFarmacreditsCache(sessionId);
                    deleteCouponTalonOne(Integer.valueOf(sessionId), idCustomerWebSafe);
                }
                LOG.info("Response from TalonOne: " + response.toString());
            } catch (IOException e) {
                LOG.warning("Error sending order to TalonOneService: " + e.getMessage() + " - " + Arrays.toString(e.getStackTrace()));
            }
        }
    }

    private void deleteUsedFarmacreditsCache(String sessionId) {
        CachedDataManager.deleteKeyIndex(Constants.FARMA_CREDITS_USED_CACHE_KEY_CUSTOMER + sessionId, 14);
    }

    private void saveDiscountCampaignTalonInBD(String newSessionId, CreatedOrder order, CustomerSessionResponse customerSessionResponse) {
        try {
            //send request to store in oracle
            TalonOneDiscount talonOneDiscount = new TalonOneDiscount();
            talonOneDiscount.setOrderId(Long.parseLong(newSessionId));
            talonOneDiscount.setCampaignId(customerSessionResponse.getTriggeredCampaigns().get(0).getId());
            talonOneDiscount.setCampaignName(customerSessionResponse.getTriggeredCampaigns().get(0).getName());
            talonOneDiscount.setTotalDiscount(getTotalEffectsSum(customerSessionResponse, order.getOrderData().getItemList()));
            Call<Object> persistDiscountsCall = iTalonOne.persistDiscounts(talonOneDiscount);
            persistDiscountsCall.execute();
        } catch (Exception ex) {
            LOG.warning(" message:" + ex.getMessage() + " Error persisting discounts on oracle: " + Arrays.toString(ex.getStackTrace()));
        }
    }

    private void saveDeductDiscountOrderinBD(String newSessionId, String idCustomerWebSafe) {
        try {
            boolean isPrime = false;
            TalonOneDeductDiscount talonOneDeductDiscount = new TalonOneDeductDiscount();
            Optional<String> totalSaveProducts = CachedDataManager.getJsonFromCacheIndex(idCustomerWebSafe + Constants.KEY_TOTAL_SAVE, 14);
            Optional<String> discountPrime = CachedDataManager.getJsonFromCacheIndex(idCustomerWebSafe + Constants.KEY_DISCOUNT_PRIME, 14);
            Optional<String> discountTalon = CachedDataManager.getJsonFromCacheIndex(idCustomerWebSafe + Constants.KEY_DISCOUNT_TALON, 14);
            Optional<String> couponTalonOrRPM = CachedDataManager.getJsonFromCacheIndex(idCustomerWebSafe + Constants.KEY_COUPON_CACHE, 14);
            Optional<String> couponAutomatic = CachedDataManager.getJsonFromCacheIndex(idCustomerWebSafe + Constants.KEY_COUPON_AUTOMATIC, 14);
            Optional<String> discountOfferPrice = CachedDataManager.getJsonFromCacheIndex(idCustomerWebSafe + Constants.KEY_OFFER_PRICE, 14);
            if (discountPrime.isPresent() && discountOfferPrice.isPresent()) {
                int roundDiscountPrime = (int) Math.ceil(Double.parseDouble(discountPrime.get()));
                int roundDiscountOfferPrice = (int) Math.ceil(Double.parseDouble(discountOfferPrice.get()));
                if (roundDiscountPrime == roundDiscountOfferPrice) {
                    isPrime = true;
                }
            }
            talonOneDeductDiscount.setOrderId(Long.parseLong(newSessionId));
            talonOneDeductDiscount.setDiscountProductRpm(totalSaveProducts.isPresent() ? Double.parseDouble(totalSaveProducts.get()) : 0D);
            talonOneDeductDiscount.setDiscountProductPrime(discountPrime.isPresent() && isPrime ? Double.parseDouble(discountPrime.get()) : 0D);
            talonOneDeductDiscount.setDiscountProductTalon(discountTalon.isPresent() ? Double.parseDouble(discountTalon.get()) : 0D);
            AnswerDeduct answerCouponTalonOrRPM = null;
            LOG.info("couponTalonOrRPM: " + totalSaveProducts + " - " + discountPrime + " - " + discountTalon + " - " + couponTalonOrRPM + " - " + couponAutomatic);
            if (couponTalonOrRPM.isPresent()) {
                answerCouponTalonOrRPM = new Gson().fromJson(couponTalonOrRPM.get(), AnswerDeduct.class);
                talonOneDeductDiscount.setNameCoupon(answerCouponTalonOrRPM.getNameCoupon());
                talonOneDeductDiscount.setDiscountCoupon(answerCouponTalonOrRPM.getDiscount());
            }

            if (couponAutomatic.isPresent()) {
                List<CouponAutomaticTalon> couponAutomaticTalonList = new Gson().fromJson(couponAutomatic.get(), new TypeToken<List<CouponAutomaticTalon>>() {
                }.getType());
                Double totalDiscountAutomaticCoupon = couponAutomaticTalonList.stream().mapToDouble(CouponAutomaticTalon::getDiscountCoupon).sum();
                String namesAutomaticCoupon = "";
                if (couponAutomaticTalonList.size() > 0) {
                    namesAutomaticCoupon = couponAutomaticTalonList.stream().map(CouponAutomaticTalon::getNameCoupon).collect(Collectors.joining(","));
                } else {
                    namesAutomaticCoupon = couponAutomaticTalonList.get(0).getNameCoupon();
                }
                talonOneDeductDiscount.setNameAutomaticCoupon(namesAutomaticCoupon);
                talonOneDeductDiscount.setDiscountAutomaticCoupon(totalDiscountAutomaticCoupon);
            }

            Call<Object> persistDeductDiscount = iTalonOne.deductDiscount(talonOneDeductDiscount);
            persistDeductDiscount.execute();
        } catch (Exception ex) {
            LOG.warning(" message:" + ex.getMessage() + " Error persisting deduct discounts on oracle: " + Arrays.toString(ex.getStackTrace()));
        }
    }

    private static void deleteCacheSession(String sessionId) {
        CachedDataManager.deleteKeyIndex(sessionId, 14);
        CachedDataManager.deleteKeyIndex(sessionId + "request", 14);
        CachedDataManager.deleteKeyIndex(sessionId + "response", 14);
        CachedDataManager.deleteKeyIndex(sessionId + "request-po", 14);
        CachedDataManager.deleteKeyIndex(sessionId + "response-po", 14);
    }

    private Integer findEffectPosition(List<CartItem> items, DeliveryOrderItem item) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getSku().equals(String.valueOf(item.getId()))) {
                return i;
            }
        }
        return -1;
    }

    private Double getTotalEffectsSum(CustomerSessionResponse customerSessionResponse, List<DeliveryOrderItem> deliveryOrderItems) {
        Double totalDiscount = 0.0;

        if (Objects.isNull(customerSessionResponse) || Objects.isNull(customerSessionResponse.getEffects()) || customerSessionResponse.getEffects().isEmpty())
            return totalDiscount;

        try {
            for (DeliveryOrderItem item : deliveryOrderItems) {
                if (Objects.isNull(item.getPrimePrice()) || (item.getPrimePrice().equals(0.0))) {
                    Integer effectPosition = findEffectPosition(customerSessionResponse.getCartItems(), item);
                    if (effectPosition != -1) {
                        Effect effect = customerSessionResponse.getEffects().get(effectPosition);
                        if (Objects.isNull(item.getOfferPrice()) || Double.valueOf(effect.getProps().get("value")) > item.getOfferPrice()) {
                            totalDiscount += Double.valueOf(effect.getProps().get("value"));
                        }
                    }
                }
            }

        } catch (Exception ex) {
            LOG.info("Error when getting effects total: " + new Gson().toJson(customerSessionResponse));
            return totalDiscount;
        }

        return totalDiscount;
    }


    public void updateCustomer(CustomerJSON customerJSON) {
        if (APIAlgolia.isTalonActive()) {
            CustomerProfileRequest customerProfileRequest = new CustomerProfileRequest();
            customerProfileRequest.setRunRuleEngine(true);
            customerProfileRequest.setDry(false);
            LinkedTreeMap<String, String> atributes = new LinkedTreeMap<>();
            final String email = customerJSON.getEmail();
            Optional<String> uuidBraze;
            if (customerJSON.getAnalyticsUUID() == null || customerJSON.getAnalyticsUUID().equals("")) {
                uuidBraze = ApiGatewayService.get().getUUIDFromBraze(email);
            } else {
                uuidBraze = Optional.ofNullable(customerJSON.getAnalyticsUUID());
            }
            atributes.put("Name", customerJSON.getFirstName() + " " + customerJSON.getLastName());
            atributes.put("Email", customerJSON.getEmail());
            atributes.put("SignupDate", String.valueOf(customerJSON.getCreationDate()));
            atributes.put("Gender", customerJSON.getGender());
            customerProfileRequest.setAttributes(atributes);
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            if (uuidBraze.isPresent()) {
                CachedDataManager.deleteKeyIndex(customerJSON.getId() + uuidBraze.get() + "rq", 14);
                CachedDataManager.deleteKeyIndex(customerJSON.getId() + uuidBraze.get() + "rp", 14);
                Call<Object> call = iTalonOne.createCustomer(uuidBraze.get(), customerProfileRequest, headers);
                try {
                    Response<Object> response = call.execute();
                    if (call.isCanceled()) {
                        LOG.warning("Excecute createCustomer is Canceled");
                    }
                    LOG.info("Execute create Customer Profile in TalonOneService: " + response.raw());
                } catch (IOException e) {
                    LOG.warning("Error create Customer Profile in TalonOneService: " + e.getMessage());
                }
            }
        }
    }

    private static boolean equalsTwoListObjectValues(List<CartItem> l1, List<CartItem> l2) {
        if (l1.size() == 0 || l2.size() == 0) {
            return false;
        }
        // make a copy of the list so the original list is not changed, and remove() is supported
        ArrayList<CartItem> cp = new ArrayList<>(l1);
        for (CartItem o : l2) {
            if (!cp.removeIf(ob -> ob.getSku().equals(o.getSku()) && ob.getQuantity() == o.getQuantity())) {
                return false;
            }
        }
        return cp.isEmpty();
    }

    public ShoppingCartJson getShoppingCartJson(DeliveryOrder deliveryOrder) {
        ShoppingCartJson shoppingCartJson = new ShoppingCartJson();
        shoppingCartJson.setId(deliveryOrder.getIdCustomer().get().getId());
        return shoppingCartJson;
    }

    private CustomerSessionRequest getCustomerSessionRequest(List<Item> items, Optional<String> uuidBraze, String idCustomerWebSafe) {
        CustomerSessionRequest customerSessionRequest = new CustomerSessionRequest();
        if (uuidBraze.isPresent())
            customerSessionRequest.setProfileId(uuidBraze.get());
        else
            customerSessionRequest.setProfileId(idCustomerWebSafe);
        customerSessionRequest.setState("OPEN");
        List<CartItem> cartItems = new ArrayList<>();
        if (items != null) {
            for (Item item : items) {
                try {
                    cartItems.add(getCartItem(item));
                } catch (Exception e) {
                    LOG.info("No se logro agregar el item, ERROR -> " + e.getMessage());
                }

            }
        }
        customerSessionRequest.setCartItems(cartItems);
        return customerSessionRequest;
    }

    private void saveItemsForComponentType(ComponentTypeEnum componentTypeEnum, List<Item> items, DynamicSection dynamicSection) {
        try {
            if (dynamicSection.getComponentType() == componentTypeEnum) {
                for (Element element : dynamicSection.getList()) {
                    if (Objects.nonNull(element.getProduct())) {
                        for (Item item : element.getProduct()) {
                            items.add(item);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.warning("Error saveItemsForComponentType TalonOneService: " + Arrays.toString(e.getStackTrace()));
        }
    }

    public void sendItemsToTalon(List<DynamicSection> dynamicSections, int userId, String idTokenIdWebSafe, String idCustomerWebSafe,EnableForEnum source, Map<String, Object> talonOneData) {
        ComponentTypeEnum mainItem = ComponentTypeEnum.MAIN_ITEM;
        ComponentTypeEnum itemList = ComponentTypeEnum.ITEM_LIST;
        ComponentTypeEnum itemListTwoRows = ComponentTypeEnum.ITEM_LIST_TWO_ROWS;
        ComponentTypeEnum smallItemList = ComponentTypeEnum.SMALL_ITEM_LIST;
        ComponentTypeEnum brazeCarousel = ComponentTypeEnum.BRAZE_CAROUSEL;
        this.userPrime = validateUserPrime((long) userId);
        if (APIAlgolia.isTalonPetalActive(source)) {
            String sessionId = userId != 0 ? String.valueOf(userId) : idTokenIdWebSafe;
            Optional<String> uuidBraze = userId != 0 ? getUuidBraze(userId) : Optional.ofNullable(idCustomerWebSafe);
            List<Item> items = new ArrayList<>();
            for (DynamicSection dynamicSection : dynamicSections) {
                saveItemsForComponentType(mainItem, items, dynamicSection);
                saveItemsForComponentType(itemList, items, dynamicSection);
                saveItemsForComponentType(itemListTwoRows, items, dynamicSection);
                saveItemsForComponentType(smallItemList, items, dynamicSection);
                saveItemsForComponentType(brazeCarousel, items, dynamicSection);
            }
            CustomerSessionRequest customerSessionRequest = getCustomerSessionRequest(items, uuidBraze, idCustomerWebSafe);
            customerSessionRequest.setDry(true);
            if (Objects.nonNull(talonOneData)) {
                customerSessionRequest.setTalonOneData(talonOneData);
            }
            LOG.warning("sessionId: " + sessionId);
            LOG.info("JP ----> customerSessionRequest: " + new Gson().toJson(customerSessionRequest));
            Call<CustomerSessionResponse> call = iTalonOne.createCustomerSession(sessionId, customerSessionRequest, new HashMap<>());
            try {
                Response<CustomerSessionResponse> response = call.execute();
                CustomerSessionResponse customerSessionResponse = response.body();
                if (Objects.nonNull(response)) {
                    if (Objects.nonNull(response.body().getEffects())) {
                        boolean valuePetals = isPetalsActive(response);
                        if (valuePetals) {
                            loadDiscountToDynamicResponse(dynamicSections, customerSessionResponse);
                        }
                    }
                }
            } catch (IOException e) {
                LOG.warning("Error sending order to TalonOneService: " + Arrays.toString(e.getStackTrace()));
            }
        }
    }

    private void loadDiscountToDynamicResponse(List<DynamicSection> dynamicSections, CustomerSessionResponse customerSessionResponse) {
        ComponentTypeEnum mainItem = ComponentTypeEnum.MAIN_ITEM;
        ComponentTypeEnum itemList = ComponentTypeEnum.ITEM_LIST;
        ComponentTypeEnum smallItemList = ComponentTypeEnum.SMALL_ITEM_LIST;
        ComponentTypeEnum itemListTwoRows = ComponentTypeEnum.ITEM_LIST_TWO_ROWS;
        ComponentTypeEnum brazeCarousel = ComponentTypeEnum.BRAZE_CAROUSEL;
        if (customerSessionResponse != null && customerSessionResponse.getEffects().size() > 0) {
            for (Effect effect : customerSessionResponse.getEffects()) {
                if (effect.getEffectType().equals("setDiscountPerItem") && effect.getProps().get("subPosition").equals("0.0")) {
                    double countItem = 0.0;
                    for (DynamicSection dynamicSection : dynamicSections) {
                        countItem = setDiscountForComponentType(mainItem, countItem, effect, dynamicSection);
                        countItem = setDiscountForComponentType(itemList, countItem, effect, dynamicSection);
                        countItem = setDiscountForComponentType(itemListTwoRows, countItem, effect, dynamicSection);
                        countItem = setDiscountForComponentType(smallItemList, countItem, effect, dynamicSection);
                        countItem = setDiscountForComponentType(brazeCarousel, countItem, effect, dynamicSection);
                    }
                }
            }
        }
    }

    private double setDiscountForComponentType(ComponentTypeEnum componentType, double countItem, Effect effect, DynamicSection dynamicSection) {
        try {
            if (dynamicSection.getComponentType() == componentType) {
                for (Element element : dynamicSection.getList()) {
                    if (Objects.nonNull(element.getProduct())) {
                        for (Item item : element.getProduct()) {
                            if (countItem == Double.parseDouble(effect.getProps().get("position"))) {
                                setDiscountItemIfValid(effect, item);
                            }
                            countItem = countItem + 1.0;
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.warning("Error setDiscountForComponentType TalonOneService: " + Arrays.toString(e.getStackTrace()));
        }
        return countItem;
    }

    private void setDiscountItemIfValid(Effect effect, Item item) {
        try {
            if (Objects.nonNull(item.getTotalStock())) {
                if (item.getTotalStock() > 0) {

                    BestOfferEnum bestOffer = getBestDealV1(item.getPrimePrice(), item.getFullPrice() - Double.valueOf(effect.getProps().get("value")), item.getOfferPrice(), this.userPrime);

                    if (bestOffer.equals(BestOfferEnum.TALON_ONE)) {
                        setItemValues(item, effect);
                        if (!validatePrimeEffect(effect) && this.userPrime) {
                            item = setDefaultPetal(item);
                        }
                    }

                    if (bestOffer.equals(BestOfferEnum.RPM) && this.userPrime) {
                        item = setDefaultPetal(item);
                    }
                }
            }
        } catch (Exception e) {
            LOG.warning("Exception setDiscountItemIfValid: " + Arrays.toString(e.getStackTrace()));
        }
    }

    public void sendItemsDirectToTalon(List<Item> items, int userId, String idTokenIdWebSafe, String idCustomerWebSafe, EnableForEnum source, Map<String, Object> talonOneData) {
        if (APIAlgolia.isTalonPetalActive(source)) {
            final Optional<String> uuidBraze = getUuidBraze(userId);
            CustomerSessionRequest customerSessionRequest = getCustomerSessionRequest(items, uuidBraze, idCustomerWebSafe);
            customerSessionRequest.setDry(true);
            String sessionId = userId != 0 ? String.valueOf(userId) : idTokenIdWebSafe;
            LOG.info("sessionId: " + sessionId);
            Call<CustomerSessionResponse> call = iTalonOne.createCustomerSession(sessionId, customerSessionRequest, new HashMap<>());
            try {
                Response<CustomerSessionResponse> response = call.execute();
                CustomerSessionResponse customerSessionResponse = response.body();
                if (Objects.nonNull(response.body().getEffects())) {
                    boolean valuePetals = isPetalsActive(response);
                    if (valuePetals)
                        loadDiscountToItem(items, customerSessionResponse);
                }
            } catch (IOException e) {
                LOG.warning("Error sending order to TalonOneService: " + Arrays.toString(e.getStackTrace()));
            }
        }
    }

    private static boolean isPetalsActive(Response<CustomerSessionResponse> response) {
        List<Effect> listEffects = response.body().getEffects();
        boolean valuePetals = true;
        for (Effect effect : listEffects) {
            LinkedTreeMap<String, String> listProps = effect.getProps();
            if (Objects.nonNull(listProps.get("path")) && listProps.get("path").contains("Attributes.petalo")) {
                valuePetals = Boolean.parseBoolean(listProps.get("value"));
            }
        }
        return valuePetals;
    }

    public void loadDiscountToItem(List<Item> items, CustomerSessionResponse customerSessionResponse) {
        if (Objects.nonNull(customerSessionResponse) && !customerSessionResponse.getEffects().isEmpty()) {
            for (Effect effect : customerSessionResponse.getEffects()) {
                if (effect.getEffectType().equals("setDiscountPerItem") && effect.getProps().get("subPosition").equals("0.0")) {
                    double countItem = 0.0;
                    for (Item item : items) {
                        if (countItem == Double.parseDouble(effect.getProps().get("position"))) {
                            setDiscountItemIfValid(effect, item);
                        }
                        countItem = countItem + 1.0;
                    }
                }
            }
        }
    }

    /**
     * validate coupon Talon
     * validCoupon
     *
     * @param user
     * @param coupon
     * @param deliveryOrder
     * @return AnswerDeduct
     */
    public AnswerDeduct validateCouponTalonOne(User user, Coupon coupon, DeliveryOrder deliveryOrder) {
        if (Objects.nonNull(user.getId())) {
            coupon.setIdUser(user.getId());
        }
        TalonOneService talonOneService = new TalonOneService();
        AnswerDeduct answerCouponTalon = talonOneService.validCouponTalon(deliveryOrder, coupon);
        LOG.info("Respuesta validación de cupon en Talon:  " + new Gson().toJson(answerCouponTalon));
        if (Objects.nonNull(answerCouponTalon.getMessage()) && !answerCouponTalon.getMessage().equalsIgnoreCase(Constants.WORD_REJECTED)) {
            answerCouponTalon.setConfirmation(true);
            answerCouponTalon.setMessage(answerCouponTalon.getMessage());
            answerCouponTalon.setDiscount(answerCouponTalon.getDiscount());
            answerCouponTalon.setTypeNotifcation(Objects.nonNull(answerCouponTalon.getTypeNotifcation()) ? answerCouponTalon.getTypeNotifcation() : Constants.WORD_ERROR);
            answerCouponTalon.setNotificationMessage(Objects.nonNull(answerCouponTalon.getNotificationMessage()) ? answerCouponTalon.getNotificationMessage() : Constants.WORD_REJECTED);
            answerCouponTalon.setNameCoupon(answerCouponTalon.getNameCoupon());
            return answerCouponTalon;
        }
        return answerCouponTalon;
    }

    public boolean validateUserPrime(Long customerId) {

        String cacheKey = Constants.KEY_CUSTOMER_PRIME_TALON_ONE + customerId;

        Optional<String> cacheCustomerPrime = CachedDataManager.getJsonFromCacheIndex(cacheKey, 14);

        if (cacheCustomerPrime.isPresent()) {
            return Boolean.parseBoolean(cacheCustomerPrime.get());
        }

        boolean isPrime = false;
        try {
            PrimeConfig primeConfig = APIAlgolia.primeConfigV2();
            if (primeConfig.featureValidateUserPrime) {
                CustomerResponseCart customerResponseCart = ApiGatewayService.get().getCustomerCreditCardPrimeData(customerId);
                if (customerResponseCart != null && customerResponseCart.isActive()) {
                    isPrime = true;
                }
            }

            CachedDataManager.saveJsonInCacheIndexTime(cacheKey, String.valueOf(isPrime), 14, 1800);
        } catch (Exception e) {
            LOG.info("No se pudo obtener el customer" + customerId);
        }
        return isPrime;
    }


    /**
     * elaborates the request to TalonOne when validate coupon
     * validCouponTalon
     *
     * @param deliveryOrder
     * @param coupon
     * @return AnswerDeduct
     */
    public AnswerDeduct validCouponTalon(DeliveryOrder deliveryOrder, Coupon coupon) {
        if (APIAlgolia.isTalonActive() && deliveryOrder != null && coupon != null) {
            final Optional<String> uuidBraze = getUuidBraze(coupon.getIdUser());
            if (uuidBraze.isPresent()) {
                orderedDeliveryOrder(deliveryOrder);
                CustomerSessionRequest customerSessionRequest = getCustomerSessionRequest(deliveryOrder, uuidBraze, null, coupon);
                LOG.info("deliveryOrder.hasItems(): " + deliveryOrder.hasItems());

                Gson gson = new Gson();
                String requestText = gson.toJson(customerSessionRequest);
                String sessionId = String.valueOf(coupon.getIdUser());
                Optional<String> requestCache = CachedDataManager.getJsonFromCacheIndex(sessionId + "request", 14);
                if (requestCache.isPresent() && requestCache.get().equals(requestText)) {
                    Optional<String> response = CachedDataManager.getJsonFromCacheIndex(sessionId + "response", 14);
                    CustomerSessionResponse customerSessionResponse = gson.fromJson(response.get(), CustomerSessionResponse.class);
                    return validCoupon(customerSessionResponse);
                } else {
                    Call<CustomerSessionResponse> call = iTalonOne.createCustomerSession(sessionId, customerSessionRequest, new HashMap<>());
                    try {
                        Response<CustomerSessionResponse> response = call.execute();
                        CustomerSessionResponse customerSessionResponse = response.body();
                        CachedDataManager.saveJsonInCacheIndexTime(sessionId + "request", requestText, 14, 900);
                        CachedDataManager.saveJsonInCacheIndexTime(sessionId + "response", gson.toJson(customerSessionResponse), 14, 900);
                        return validCoupon(customerSessionResponse);
                    } catch (IOException e) {
                        LOG.warning("Error sending order to TalonOne: " + e.getMessage());
                    }
                }
            }
        } else {
            LOG.info("Talon One is disabled or delivery or coupon is empty");
            if (Objects.isNull(deliveryOrder) || !deliveryOrder.hasItems()) {
                AnswerDeduct emptyCarItem = new AnswerDeduct();
                emptyCarItem.setNotificationMessage(Constants.VALID_COUPON_ITEMS_EMPTY);
                emptyCarItem.setTypeNotifcation("Error");
                emptyCarItem.setMessage(Constants.VALID_COUPON_ITEMS_EMPTY);
                emptyCarItem.setDiscount(0D);
                return emptyCarItem;
            }
        }
        return new AnswerDeduct();
    }


    /**
     * extract value of the response in TalonOne when validate coupon
     * validCoupon
     *
     * @param customerSessionResponse
     * @return AnswerDeduct
     */
    private static AnswerDeduct validCoupon(CustomerSessionResponse customerSessionResponse) {
        StringBuffer sbRpta = new StringBuffer();
        AnswerDeduct answer = new AnswerDeduct();
        if (Objects.nonNull(customerSessionResponse)) {
            if (Objects.nonNull(customerSessionResponse.getEffects()) && customerSessionResponse.getEffects().size() > 0) {
                List<Effect> objectsNotification = customerSessionResponse.getEffects().stream().filter(effect -> effect.getEffectType().equals("showNotification")).collect(Collectors.toList());
                for (Effect effect : customerSessionResponse.getEffects()) {
                    if (effect.isCouponTalonValid()) {
                        answer.setNameCoupon(effect.getRuleName());
                        sbRpta.append(effect.getRuleName());
                    }
                    if (effect.hasNotificationsCouponTalon()) {
                        answer.setNotificationMessage(effect.getProps().get("body"));
                        answer.setTypeNotifcation(effect.getProps().get("notificationType"));
                    }
                    if (effect.couponRejectedByNotReachThreshold()) {
                        answer.setNameCoupon(effect.getRuleName());
                        sbRpta.append(effect.getRuleName() + " ").append("pendiente");
                        answer.setDiscount(0D);
                        if (Objects.nonNull(objectsNotification) && !objectsNotification.isEmpty()) {
                            objectsNotification.stream().forEach(notification -> {
                                if (notification.getCampaignId().equals(effect.getCampaignId())) {
                                    answer.setNotificationMessage(notification.getProps().get("body"));
                                    answer.setTypeNotifcation("Info");
                                }
                            });
                        }
                    }
                    if (effect.couponRejectedForAnything()) {
                        sbRpta.append("rechazado");
                        answer.setRejectionReason(effect.getProps().get("rejectionReason"));
                    }
                    if (effect.isCouponAutomaticValid()) {
                        sbRpta.append(" -$").append(Double.parseDouble(effect.getProps().get("value")));
                        answer.setDiscount(Double.parseDouble(effect.getProps().get("value")));
                    }
                }
            }
        }
        answer.setMessage(sbRpta.toString());
        return answer;
    }

    private static void orderedDeliveryOrder(DeliveryOrder deliveryOrder) {
        if (deliveryOrder.getItemList().size() > 0) {
            deliveryOrder.setItemList(deliveryOrder.getItemList().stream().sorted(Comparator.comparing(DeliveryOrderItem::getId)).collect(Collectors.toList()));
        }
    }

    public void deleteCouponTalonOne(Integer userId, String idCustomerWebSafe) {
        try {
            Optional<String> uuidBraze = userId != 0 ? getUuidBraze(userId) : Optional.ofNullable(idCustomerWebSafe);
            CustomerSessionRequest customerSessionRequest = buildCustomerSessionRequestWithCouponCodeEmpty(uuidBraze);
            Call<CustomerSessionResponse> call = iTalonOne.createCustomerSession(String.valueOf(userId), customerSessionRequest, new HashMap<>());
            call.execute();
        } catch (Exception e) {
            LOG.severe("Error exception: " + e);
        }
    }

    private CustomerSessionRequest buildCustomerSessionRequestWithCouponCodeEmpty(Optional<String> profileId) {
        CustomerSessionRequest customerSessionRequest = new CustomerSessionRequest();
        customerSessionRequest.setProfileId(profileId.orElse(null));
        customerSessionRequest.setState("OPEN");
        customerSessionRequest.setCartItems(Collections.singletonList(buildCartItem()));
        customerSessionRequest.setCouponCodes(Collections.emptyList());
        return customerSessionRequest;
    }

    private CartItem buildCartItem() {
        CartItem itemCoupon = new CartItem();
        itemCoupon.setName("coupon");
        itemCoupon.setSku("12345");
        itemCoupon.setPrice(1.0);
        LinkedTreeMap<String, String> attributes = new LinkedTreeMap<>();
        attributes.put("Brand", "2008M-0008623");
        itemCoupon.setAttributes(attributes);
        itemCoupon.setQuantity(1);
        return itemCoupon;
    }

    public TalonOneDeductDiscount retireveDeductDiscount(Long orderId) {
        final String url = URLConnections.URL_TALON_ONE_RETRIEVE_DEDUCT_DISCOUNT.replace("{orderId}", String.valueOf(orderId));
        //LOG.info("url: " + url+ " - orderId: " + orderId);
        Call<TalonOneDeductDiscount> call = iTalonOne.retrieveDeductDiscount(url);
        TalonOneDeductDiscount talonOneDeductDiscountResponse = new TalonOneDeductDiscount();
        try {
            Response<TalonOneDeductDiscount> response = call.execute();
            talonOneDeductDiscountResponse = response.body();
        } catch (IOException e) {
            LOG.warning("Error sending order to TalonOneService: " + Arrays.toString(e.getStackTrace()));
        }
        return talonOneDeductDiscountResponse;
    }

    private boolean validatePrimeEffect(Effect effect) {
        return Objects.nonNull(effect.getPrimeDiscount()) && effect.getPrimeDiscount();
    }

}
