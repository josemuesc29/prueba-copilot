package com.imaginamos.farmatodo.backend.sim.domain;

import com.google.api.server.spi.response.ConflictException;
import com.google.gson.Gson;
import com.imaginamos.farmatodo.backend.cache.CachedDataManager;
import com.imaginamos.farmatodo.backend.order.create_order.domain.OrderUtil;
import com.imaginamos.farmatodo.model.customer.CustomerResponseCart;
import com.imaginamos.farmatodo.model.order.DeliveryOrder;
import com.imaginamos.farmatodo.model.order.FulfilOrdColDescDomain;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.networking.services.ApiGatewayService;

import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * @author Jhon Chaparro
 * @since 2022
 */
public class AmplitudeUtils {

    private static final Logger LOG = Logger.getLogger(AmplitudeUtils.class.getName());

    /**
     * validation of create user or it is exist
     *
     * @param fulfilOrdColDescDomain
     * @return String
     */
    public static String userExistInBraze(final FulfilOrdColDescDomain fulfilOrdColDescDomain) {
        String idBraze = getIdBraze(fulfilOrdColDescDomain.getFulfilOrdDesc());
        if (idBraze == null) {
//            LOG.info("usuario no existe::se crea");
            idBraze = getIdBrazeCreateUser(fulfilOrdColDescDomain.getFulfilOrdDesc());
        }
        return idBraze;
    }

    /**
     * get user in braze uuid
     *
     * @param fulfilOrdDesc
     * @return
     */
    public static String getIdBraze(FulfilOrdColDescDomain.FulfilOrdDescDomain[] fulfilOrdDesc) {
        FulfilOrdColDescDomain.FulfilOrdDescDomain.FulfilOrdCustDescDomain customer = Arrays.stream(fulfilOrdDesc).findAny().get().getFulfilOrdCustDesc();
        try {
            return ApiGatewayService.get().getUUIDFromBraze(customer != null ? customer.getEmail() : "").get();
        } catch (Exception e) {
            LOG.warning("method: getIdBraze() --> Error: al buscar usuarios " + customer);
            LOG.warning("method: getIdBraze() --> Error: " + e.fillInStackTrace());
            return null;
        }
    }

    /**
     * Create User in braze
     *
     * @param fulfilOrdDesc
     * @return String
     */
    public static String getIdBrazeCreateUser(FulfilOrdColDescDomain.FulfilOrdDescDomain[] fulfilOrdDesc) {
        FulfilOrdColDescDomain.FulfilOrdDescDomain.FulfilOrdCustDescDomain customer = Arrays.stream(fulfilOrdDesc).findAny().get().getFulfilOrdCustDesc();
        try {
            return ApiGatewayService.get().getUUIDFromBrazeCreateUser(customer != null ? customer.getEmail() : "",
                    null, null).get();
        } catch (Exception e) {
            LOG.warning("method: getIdBrazeCreateUser() --> Error: al Crear usuarios  en braze" + customer);
            return null;
        }
    }

    public static DeliveryOrder getOrder(String orderId) throws ConflictException {
        return OrderUtil.getOrderMethod(null, Long.parseLong(orderId), false, false);
    }

    /***
     * validateUserPrimeCache
     * @author JhonChaparro
     * @param customerId
     * @return Optional<Boolean> True or false
     */
    public static Optional<Boolean> validateUserPrimeCache(Long customerId) {
        try {
            Optional<String> jsonCachedOptional = CachedDataManager.getClientPrime(customerId + AmplitudeConstant.PRIME);
            if (jsonCachedOptional.isPresent()) {
                return Optional.of(new Gson().fromJson(jsonCachedOptional.get(), CustomerResponseCart.class).isActive());
            }
            CustomerResponseCart customerResponseCart = ApiGatewayService.get().getCustomerCreditCardPrimeData(customerId);
            if (customerResponseCart != null) {
                String jsonToCache = new Gson().toJson(customerResponseCart);
                CachedDataManager.saveClientPrime(customerId + AmplitudeConstant.PRIME, jsonToCache);
                return Optional.of(customerResponseCart.isActive());
            }
        } catch (Exception e) {
            LOG.info("No se pudo obtener el customer");
            return Optional.of(Boolean.FALSE);
        }
        return Optional.of(Boolean.FALSE);
    }

    /***
     * change name source
     * @author JhonChaparro
     * @param source
     * @return
     */
    public static String ValidateSource(String source) {
        if (Constants.SOURCE_CALL_CENTER.equalsIgnoreCase(source)) {
            return "Call Center";
        } else if (Constants.SOURCE_ANDROID.equalsIgnoreCase(source)) {
            return "Android";
        } else if (Constants.SOURCE_IOS.equalsIgnoreCase(source)) {
            return "iOS";
        } else if (Constants.SOURCE_WEB.equalsIgnoreCase(source)) {
            return "Web";
        } else {
            return "Web";
        }
    }

    /**
     * get Payment Method
     * @param id
     * @return String
     */
    public String getPaymentMethod(Long id) {
        if (id != null) {
            switch (id.intValue()) {
                case 1:
                    return "Efectivo";
                case 2:
                    return "Datáfonos";
                case 3:
                    return "Transacciones en línea";
                case 4:
                    return "Pago en Tienda";
                case 5:
                    return "Seguros Bolivar";
                case 6:
                    return "PSE";
                default:
                    return "";
            }
        }
        return "";
    }

    /**
     * get delivery order
     * @param orderDeliveryType
     * @return boolean
     */
    public static boolean  isNotExpress(String orderDeliveryType) {
        return orderDeliveryType != null && !(orderDeliveryType.equalsIgnoreCase("EXPRESS"));
    }

}

