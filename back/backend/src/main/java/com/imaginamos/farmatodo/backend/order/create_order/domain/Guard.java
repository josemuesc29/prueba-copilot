package com.imaginamos.farmatodo.backend.order.create_order.domain;

import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.ConflictException;
import com.imaginamos.farmatodo.backend.user.Authenticate;
import com.imaginamos.farmatodo.model.order.*;
import com.imaginamos.farmatodo.model.payment.PaymentTypeEnum;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.model.util.DeliveryType;
import com.imaginamos.farmatodo.networking.cache.CachedDataManager;

import java.util.List;
import java.util.Objects;

public class Guard {
    public static void againtsNull(Object object, String objectName) throws BadRequestException {
        if (Objects.isNull(object))
            throw new BadRequestException("BadRequest ["+ objectName + "] is required.");

    }

    public static void againtsNullOrEmpty(Object object, String objectName) throws BadRequestException {
        if (Objects.isNull(object) || String.valueOf(object).isEmpty())
            throw new BadRequestException("BadRequest ["+ objectName + "] is required.");
    }
    public static void againtsZero(Object object, String objectName) throws BadRequestException {
        if (Objects.isNull(object) || (int)object == 0 ) {
            throw new BadRequestException("BadRequest [" + objectName + "] is required.");
        }
    }
    public static boolean isWebWithoutDeliveryType(final ShoppingCartJson shoppingCartJson) {
        return shoppingCartJson.getSource().equals("WEB") && shoppingCartJson.getDeliveryType() == null;
    }

    public static boolean isDeliveryTypePresent(final ShoppingCartJson shoppingCartJson) {
        return Objects.isNull(shoppingCartJson.getDeliveryType()) || Objects.isNull(shoppingCartJson.getDeliveryType().getDeliveryType());
    }

    public static boolean isNationalOrEnvialoYa(final ShoppingCartJson shoppingCartJson) {
        return shoppingCartJson.getDeliveryType() == DeliveryType.NATIONAL || shoppingCartJson.getDeliveryType() == DeliveryType.ENVIALOYA;
    }

    public static void againstInvalidTokens(final String token, final String tokenIdWebSafe, final int idFarmatodo) throws ConflictException, BadRequestException {
        if (!Authenticate.isValidToken(token, tokenIdWebSafe)){
            CachedDataManager.deleteKeyIndex(Objects.requireNonNull(String.valueOf(idFarmatodo)), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
            throw new ConflictException(Constants.INVALID_TOKEN);
        }

    }


    public static void againstInvalidAddress(final boolean isScanAndGo, final int addressID, final int idFarmatodo) throws ConflictException {
        if (not(isScanAndGo) && addressID == 0){
            CachedDataManager.deleteKeyIndex(Objects.requireNonNull(String.valueOf(idFarmatodo)), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
            throw new ConflictException("Direccion invalida");
        }

    }


    public static boolean not(boolean data) {
        return !data;
    }


    public static void againstInvalidPaymentMethod(final DeliveryOrder order) throws ConflictException {
        if (order.getPaymentType().getId() == PaymentTypeEnum.TRANSACCIONES_EN_LINEA.getId() && order.getPaymentCardId() <= 0){
            CachedDataManager.deleteKeyIndex(String.valueOf(order.getIdFarmatodo()), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
            throw new ConflictException("PaymentCardId es obligatorio.");
        }

    }


    public static void againstBlockedOrFraudulentUser(final int IdFarmatodo) throws ConflictException{
        if (OrderService.userIsBlockedOrFraudulent(IdFarmatodo))
            throw new ConflictException("Usuario bloqueado no puede continuar");
    }


    public static void againstNullDeliveryOrder(final DeliveryOrder deliveryOrderSaved, final int idFarmatodo) throws ConflictException {
        if (deliveryOrderSaved == null){
            CachedDataManager.deleteKeyIndex(String.valueOf(idFarmatodo), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
            throw new ConflictException(Constants.ERROR_CREATE_ORDER);
        }
    }


    public static void againstOrderWithoutItems(final List<DeliveryOrderItem> deliveryOrderItemList, final int idFarmatodo) throws ConflictException {
        if (deliveryOrderItemList == null){
            CachedDataManager.deleteKeyIndex(String.valueOf(idFarmatodo), Constants.ID_INDEX_SAVE_AND_GET_REDIS);
            throw new ConflictException(Constants.ERROR_CREATE_ORDER);
        }
    }

    public static void validateSourceAndDeliveryType(ShoppingCartJson shoppingCartJson) throws BadRequestException {
        if (shoppingCartJson.getSource().equals("WEB")) {
            if (shoppingCartJson.getDeliveryType() == null) {
                throw new BadRequestException("BadRequest [deliveryType es requerido]");
            }
        } else {
            if (Objects.isNull(shoppingCartJson.getDeliveryType()) || Objects.isNull(shoppingCartJson.getDeliveryType().getDeliveryType())) {
                shoppingCartJson.setDeliveryType(DeliveryType.EXPRESS);
            }
        }
    }

    public static void validateInputs(String token, String tokenIdWebSafe, Qualification qualification) throws BadRequestException, ConflictException {
        if (token == null || token.isEmpty()) {
            throw new BadRequestException("BadRequest [token es requerido]");
        }
        if (tokenIdWebSafe == null || tokenIdWebSafe.isEmpty()) {
            throw new BadRequestException("BadRequest [tokenIdWebSafe es requerido]");
        }
        isValidTokens(token, tokenIdWebSafe);
        if (qualification == null) {
            throw new BadRequestException("BadRequest [qualification es requerido]");
        }
        if (qualification.getOrderId() == null) {
            throw new BadRequestException("BadRequest [qualification.orderId es requerido]");
        }
        if (qualification.getRatingId() == null) {
            throw new BadRequestException("BadRequest [qualification.ratingId es requerido]");
        }
    }

    public static void isValidTokens(final String token, final String tokenIdWebSafe) throws ConflictException, BadRequestException {
        againstInvalidTokens(token, tokenIdWebSafe, 0);
    }

    public static void isValidIdOrder(String idOrder) throws BadRequestException {
        if (idOrder == null || idOrder.trim().isEmpty()) {
            throw new BadRequestException("Order ID no puede ser nulo o vacio");
        }
    }

    public static void isValidUpdatePickingDateReq(UpdatePickingDateReq updatePickingDateReq) throws BadRequestException {
        if (Objects.isNull(updatePickingDateReq)) {
            throw new BadRequestException("BadRequest [event is required]");
        } else if (Objects.isNull(updatePickingDateReq.getOrderId())) {
            throw new BadRequestException("BadRequest [orderStatus.getOrderNo() is required]");
        } else if (Objects.isNull(updatePickingDateReq.getPickingDate())) {
            throw new BadRequestException("BadRequest [orderStatus.getUuid() is required]");
        }
    }

    public static void isValidCancelOrderReq(CancelOrderReq cancelOrderReq) throws ConflictException {
        if (cancelOrderReq.getOrderId() == null || cancelOrderReq.getOrderId().trim().isEmpty())
            throw new ConflictException(Constants.ERROR_ID_ORDER);
        if (cancelOrderReq.getRol() == null)
            throw new ConflictException(Constants.ERROR_USER_ROL);
        if (cancelOrderReq.getCancellationReasonId() == null)
            throw new ConflictException(Constants.ERROR_CANCELLATION_REASON);
        if (cancelOrderReq.getCorreoUsuario() == null) {
            cancelOrderReq.setCorreoUsuario(Constants.USER_CANCEL_ORDER_DEFAULT);
        }
    }


}
