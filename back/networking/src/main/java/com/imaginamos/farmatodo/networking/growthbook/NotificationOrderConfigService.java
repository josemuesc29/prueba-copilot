package com.imaginamos.farmatodo.networking.growthbook;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.networking.models.growthbook.NotificationOrderConfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Optional;
import java.util.logging.Logger;

public class NotificationOrderConfigService {
    private static final Logger logger = Logger.getLogger(NotificationOrderConfigService.class.getName());

    public static Optional<NotificationOrderConfig> getNotificationOrderConfig(String customerId) {
        String featureKey = Constants.GROWTHBOOK_FEATURE_NOTIFICATIONS_ORDER;
        GrowthBookService growthBookService = new GrowthBookService();
        NotificationOrderConfig defaultConfig = getDefaultNotificationOrderConfig();

        try {
            growthBookService.initialize(customerId);
            Object featureValue = growthBookService.getFeatureValue(featureKey, defaultConfig);
            Gson gson = new Gson();
            NotificationOrderConfig notificationOrderConfig = gson.fromJson(
                    gson.toJson(featureValue),
                    NotificationOrderConfig.class
            );

            if (notificationOrderConfig != null) {
                return Optional.of(notificationOrderConfig);
            }
        } catch (Exception e) {
            logger.severe("Error getting NotificationOrderConfig for customer " + customerId + ": " + e.getMessage());
        }
        return Optional.of(defaultConfig);
    }


    public static NotificationOrderConfig getDefaultNotificationOrderConfig() {
        String jsonFileName = Constants.DEFAULT_FEATURE_NOTIFICATIONS_ORDER;
        InputStream inputStream = NotificationOrderConfigService.class.getClassLoader().getResourceAsStream(jsonFileName);

        if (inputStream != null) {
            try (InputStreamReader reader = new InputStreamReader(inputStream)) {
                Gson gson = new Gson();
                NotificationOrderConfig config = gson.fromJson(reader, NotificationOrderConfig.class);

                if (config == null || config.getConfig() == null || config.getConfig().isEmpty()) {
                    return new NotificationOrderConfig(new ArrayList<>());
                }

                return config;
            } catch (JsonSyntaxException e) {
                return new NotificationOrderConfig(new ArrayList<>());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            return new NotificationOrderConfig(new ArrayList<>());
        }
    }
}
