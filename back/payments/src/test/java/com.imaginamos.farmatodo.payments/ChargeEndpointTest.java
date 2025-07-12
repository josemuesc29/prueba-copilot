package com.imaginamos.farmatodo.payments;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.imaginamos.farmatodo.model.payment.OrderStatusEnum;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import static java.time.Duration.between;
import static org.junit.Assert.assertEquals;

public class ChargeEndpointTest {
    private static final Logger LOG = Logger.getLogger(ChargeEndpointTest.class.getName());

    private final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    @Before
    public void setUp() {
        helper.setUp();
    }

    @After
    public void tearDown() {
        helper.tearDown();
    }


    @Test
    public void addLogIntentPayment() throws EntityNotFoundException {
        LOG.info("method: addLogIntentPayment");
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

        Entity logIntentPayment = new Entity("LogIntentPayment", "0001");
        logIntentPayment.setProperty("orderId", 1L);
        logIntentPayment.setProperty("uuid", "uuid");
        logIntentPayment.setProperty("createdDate", LocalDateTime.now().minus(5, ChronoUnit.MINUTES).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        logIntentPayment.setProperty("status", OrderStatusEnum.EN_COLA_POR_PAGAR.name());

        LOG.info("method: addLogIntentPayment put Entity");
        ds.put(logIntentPayment);
        LOG.info("method: addLogIntentPayment query Entity");
        Entity result = ds.get(logIntentPayment.getKey());

        List<Entity> logItentPaymentsList = new ArrayList<>();
        logItentPaymentsList.add(result);

        LOG.info("method: addLogIntentPayment Time: "+LocalDateTime.ofInstant(Instant.ofEpochMilli((Long)result.getProperty("createdDate")), ZoneId.systemDefault()));
        LOG.info("method: addLogIntentPayment Actual Time: "+ LocalDateTime.now());

        LOG.info("method: addLogIntentPayment between Time: "+ between(LocalDateTime.ofInstant(Instant.ofEpochMilli((Long)result.getProperty("createdDate")), ZoneId.systemDefault()),
                LocalDateTime.now()));
        LOG.info("method: addLogIntentPayment between Time: "+ between(LocalDateTime.ofInstant(Instant.ofEpochMilli((Long)result.getProperty("createdDate")), ZoneId.systemDefault()),
                LocalDateTime.now()).getSeconds());

        LOG.info("method: addLogIntentPayment Diff Time: "+ (5*60));

        final Optional<Entity> lastLogIntentPaymentOptional =
                logItentPaymentsList.stream().filter(logIntentPaymentObjects ->
                        between(LocalDateTime.ofInstant(Instant.ofEpochMilli((Long)logIntentPaymentObjects.getProperty("createdDate")), ZoneId.systemDefault()),
                                LocalDateTime.now()
                        ).getSeconds() <=
                                (5*60)).findAny();
        assertEquals(true, lastLogIntentPaymentOptional.isPresent());




        LOG.info("method: testAddition");
        assertEquals(4, 2 + 2);
    }


}