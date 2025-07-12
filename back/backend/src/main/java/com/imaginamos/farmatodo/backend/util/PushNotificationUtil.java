package com.imaginamos.farmatodo.backend.util;

import com.google.api.server.spi.response.ConflictException;
import com.googlecode.objectify.Key;
import com.imaginamos.farmatodo.backend.firebase.FirebaseNotification;
import com.imaginamos.farmatodo.backend.user.Users;
import com.imaginamos.farmatodo.model.algolia.ScanAndGoPushNotificationProperty;
import com.imaginamos.farmatodo.model.user.PushNotification;
import com.imaginamos.farmatodo.model.user.User;
import com.imaginamos.farmatodo.model.util.Answer;
import com.imaginamos.farmatodo.model.util.Constants;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;

import static com.imaginamos.farmatodo.backend.OfyService.ofy;

public class PushNotificationUtil {
    private Users users;
    private static final Logger LOG = Logger.getLogger(PushNotificationUtil.class.getName());

    public PushNotificationUtil() {
        users = new Users();
    }

    public Answer senPushNotification(final String idCustomerWebSafe,
                                       final ScanAndGoPushNotificationProperty scanAndGoPushNotificationProperty){
        try {
            LOG.info("method senPushNotification: Start send notification scan and go service...");
            Key<User> userKey = Key.create(idCustomerWebSafe);

            LOG.info("Key user created..." + userKey.toString());
            User user = users.findUserByKey(userKey);
            if (user == null)
                throw new ConflictException(Constants.USER_NOT_FOUND);
            if (Objects.nonNull(scanAndGoPushNotificationProperty)) {
                LOG.info("pushNotificationProperty -> Hours:" + scanAndGoPushNotificationProperty.getTimeToPushInHours()
                        + ", message: " + scanAndGoPushNotificationProperty.getMessage());
                PushNotification pushNotification = ofy().load().type(PushNotification.class).ancestor(user).first().now();

                if (Objects.isNull(pushNotification)) {
                    pushNotification = new PushNotification();
                    pushNotification.setUser(userKey);
                    pushNotification.setIdPushNotification(UUID.randomUUID().toString());
                }
                LOG.info("pushNotification -> timeLastPush:" + pushNotification.getTimeLastPush());
                //Verify last notification
                if (Objects.isNull(pushNotification.getTimeLastPush()) || verifyTimeToPush(pushNotification.getTimeLastPush(),
                        scanAndGoPushNotificationProperty.getTimeToPushInHours())) {
                    // SEND NOTIFICATION
                    LOG.info("Sending notification...");
                    FirebaseNotification.generalNotificationService(user.getId(), scanAndGoPushNotificationProperty.getTitle(),
                            scanAndGoPushNotificationProperty.getMessage(), null);
                    pushNotification.setTimeLastPush(DateTime.now().getMillis());
                    LOG.info("Notification sended");
                }
                ofy().save().entity(pushNotification).now();
            }
        } catch (Exception e) {
            LOG.warning("ERROR -> " + e.toString() + " , " + e.getCause());
            return new Answer(false);
        }
        return new Answer(true);
    }

    private boolean verifyTimeToPush(Long timeLast, int nextTime) {
        DateTime lastDate = new DateTime(timeLast, DateTimeZone.forID("America/Bogota"));
        DateTime nextDate = lastDate.plusHours(nextTime);
        return nextDate.isBeforeNow();
    }
}
