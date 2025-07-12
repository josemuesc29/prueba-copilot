package com.imaginamos.farmatodo.backend.order.create_order.infraestructure;

import com.algolia.search.exceptions.AlgoliaException;
import com.google.api.server.spi.response.ConflictException;
import com.google.gson.Gson;
import com.imaginamos.farmatodo.model.braze.BrazeEventCreate;
import com.imaginamos.farmatodo.model.braze.BrazeProperties;
import com.imaginamos.farmatodo.model.order.CreatedOrder;
import com.imaginamos.farmatodo.model.order.DeliveryOrderItem;
import com.imaginamos.farmatodo.networking.algolia.APIAlgolia;
import com.imaginamos.farmatodo.networking.growthbook.NotificationOrderConfigService;
import com.imaginamos.farmatodo.networking.models.algolia.CancellationReason;
import com.imaginamos.farmatodo.networking.models.algolia.OrderMessageConfiguration;
import com.imaginamos.farmatodo.networking.models.algolia.StatusMessageConfig;
import com.imaginamos.farmatodo.networking.models.braze.PushNotificationRequest;
import com.imaginamos.farmatodo.networking.models.growthbook.NotificationConfig;
import com.imaginamos.farmatodo.networking.services.ApiGatewayService;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

public class BrazeClient {

    private static final Logger LOG = Logger.getLogger(BrazeClient.class.getName());
    public static void sendEventCreate(CreatedOrder order, Long userId) {
        try {
            BrazeEventCreate braze = mapBraze(order, userId);
            ApiGatewayService.get().sendEventCreate(braze);
        }catch (Exception e) {}
    }


    private static BrazeEventCreate mapBraze(CreatedOrder order, Long userId) {
        BrazeEventCreate brazeEventCreate = new BrazeEventCreate();
        brazeEventCreate.setUserId(String.valueOf(userId));
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            List<BrazeProperties> itemsData = new ArrayList<>();
            for (DeliveryOrderItem item : order.getOrderData().getItemList()) {
                BrazeProperties brazeProperties = new BrazeProperties();
                //validar null con ternario
                brazeProperties.setItem_id( String.valueOf(item.getId()));
                brazeProperties.setItem_name(item.getMediaDescription() == null ? "" : item.getMediaDescription());
                brazeProperties.setItem_variant(item.getGrayDescription() == null ? "" : item.getGrayDescription());
                brazeProperties.setItem_category(item.getCategorie() == null ? "" : item.getCategorie());
                brazeProperties.setItem_category2(item.getSubCategory() == null ? "" : item.getSubCategory());
                brazeProperties.setItem_department(item.getDepartments() == null || item.getDepartments().isEmpty() ? "" : item.getDepartments().get(0));
                brazeProperties.setBrand(item.getBrand() == null ? "" : item.getBrand());
                brazeProperties.setItem_quantity(item.getQuantitySold());
                brazeProperties.setItem_price(item.getFullPrice());
                brazeProperties.setItem_rms_group(item.getRms_group() == null ? "" : item.getRms_group());
                brazeProperties.setItem_rms_deparment(item.getRms_group() == null ? "" : item.getRms_group());
                brazeProperties.setItem_rms_class(item.getRms_class() == null ? "" : item.getRms_class());
                brazeProperties.setItem_rms_subclass(item.getRms_subclass() == null ? "" : item.getRms_subclass());
                brazeProperties.setOrder_id(String.valueOf(order.getId()));
//        brazeProperties.setItem_price_prime(item.getPrimePrice()); TODO DESCOMENTAR CUANDO LLEGUE PRIME
                itemsData.add(brazeProperties);

            }
            brazeEventCreate.setItemsData(itemsData);

        }
        return brazeEventCreate;
    }


    public static void sendOrderPushNotification(SendOrderPush sendOrderPush) throws AlgoliaException, ConflictException, IOException {
        try {
            Optional<StatusMessageConfig> messageConfig = getStatusMessageConfig(sendOrderPush.getStatus());
            String message = messageConfig.map(StatusMessageConfig::getMessage).orElse(null);
            String title = messageConfig.map(StatusMessageConfig::getTitle).orElse(null);

            String messengerName = null;
            Optional<String> optionalMessengerName =
                    ApiGatewayService.get().getMessengerNameByOrderId(Long.valueOf(sendOrderPush.getOrderId()));
            if (optionalMessengerName.isPresent()) {
                messengerName = optionalMessengerName.get();
            }

            if(message != null && title != null) {
                message = message.replace("{id}", String.valueOf(sendOrderPush.getOrderId()));
                title = title.replace("{id}", String.valueOf(sendOrderPush.getOrderId()));
                String cancellationReason = getCancellationReason(sendOrderPush.getCancellationReason());
                PushNotificationRequest request = new PushNotificationRequest(sendOrderPush.getEmail(),
                        title,
                        Objects.nonNull(cancellationReason) ?  cancellationReason : message,
                        "https://www.farmatodo.com.co/detalle-orden/" + sendOrderPush.getOrderId(),
                        Long.valueOf(sendOrderPush.getOrderId()), sendOrderPush.getStatusBraze(), messengerName);
                ApiGatewayService.get().sendPushNotificationBraze(request);
            }
        } catch (Exception e) {
            LOG.warning("method: sendOrderPushNotification(), Error sending push notification: " + e.getMessage());
        }
    }

    @NotNull
    private static Optional<StatusMessageConfig> getStatusMessageConfig(int status) throws AlgoliaException {
        OrderMessageConfiguration config = APIAlgolia.getNotificationOrderMessage();
        LOG.info(new Gson().toJson(config));
        return config.getConfig().stream().filter(c -> c.getStatus().equals(String.valueOf(status))).findFirst();
    }


    private static String getCancellationReason(Long status) throws AlgoliaException {
        OrderMessageConfiguration config = APIAlgolia.getNotificationOrderMessage();
        Optional<CancellationReason> optionalCancellationReason = config.getCancellationReasons().stream().filter(c -> Objects.equals(c.getId(), status)).findFirst();
        return optionalCancellationReason.map(CancellationReason::getDescription).orElse(null);
    }

    public static void sendPushNotifications(SendOrderPush sendOrderPush, String customerId){
        try {
            Optional<NotificationConfig> configOpt = findNotificationConfig(customerId, sendOrderPush.getStatus());
            sendPushNotification(sendOrderPush, Objects.requireNonNull(configOpt.orElse(null)));
        } catch (Exception e) {
            LOG.warning("method: sendPushNotifications(), Error sending push: " + e.getMessage());
        }
    }

    private static Optional<NotificationConfig> findNotificationConfig(String customerId, int status) {
        return NotificationOrderConfigService.getNotificationOrderConfig(customerId)
                .flatMap(orderConfig -> orderConfig.getConfig().stream()
                        .filter(notification -> notification.getStatus() == status)
                        .findFirst());
    }

    private static void sendPushNotification(SendOrderPush sendOrderPush, NotificationConfig config) throws ConflictException, IOException {
        if (validateDataConfig(config)) {
            String message = config.getMessage();
            String title = config.getTitle();
            PushNotificationRequest request = buildPushNotificationRequest(sendOrderPush, message, title);
            ApiGatewayService.get().sendPushNotificationBraze(request);
        }
    }

    private static boolean validateDataConfig(NotificationConfig config) {
        return Objects.nonNull(config) &&
                Objects.nonNull(config.getMessage()) && !config.getMessage().isEmpty() &&
                Objects.nonNull(config.getTitle()) && !config.getTitle().isEmpty();
    }

    private static PushNotificationRequest buildPushNotificationRequest(SendOrderPush sendOrderPush, String message, String title) {
        return new PushNotificationRequest(sendOrderPush.getEmail(), title, message, "https://www.farmatodo.com.co/detalle-orden/" + sendOrderPush.getOrderId(),
                Long.valueOf(sendOrderPush.getOrderId()), sendOrderPush.getStatusBraze(),null);
    }
}
