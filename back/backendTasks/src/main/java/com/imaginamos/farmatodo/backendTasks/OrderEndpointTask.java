package com.imaginamos.farmatodo.backendTasks;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.common.collect.Lists;
import com.googlecode.objectify.cmd.Query;
import com.imaginamos.farmatodo.model.order.DeliveryOrder;
import com.imaginamos.farmatodo.model.order.OrderFinalize;
import com.imaginamos.farmatodo.model.order.OrderFinalizeRes;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.model.util.FTDUtil;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.imaginamos.farmatodo.model.OfyService.ofy;


@Api(name = "orderEndpointTask",
        version = "v1",
        scopes = {Constants.EMAIL_SCOPE},
        clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID_DEVELOPER, com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID},
        audiences = {Constants.ANDROID_AUDIENCE},
        namespace = @ApiNamespace(ownerDomain = Constants.OWNER_DOMAIN, ownerName = Constants.OWNER_NAME, packagePath = ""),
        description = "endpoint for tasks the orders")
public class OrderEndpointTask {

    private FTDUtil ftdUtil;

    public OrderEndpointTask() {
        ftdUtil = new FTDUtil();
    }

    private static final Logger LOG = Logger.getLogger(OrderEndpointTask.class.getName());

    @ApiMethod(name = "getOldOrders", path = "/orderEndpointTask/getOldOrders", httpMethod = ApiMethod.HttpMethod.GET)
    public OrderFinalizeRes getOldOrders(@Named("keyClient") final String keyClient,@Named("filterBy") final String filterBy) throws UnauthorizedException {
        //LOG.info("API: getOldOrders");
        if (!keyClient.equals(Constants.KEY_SECURE_CLIENT))
            throw new UnauthorizedException(Constants.ERROR_ACCESS_DENIED);

        OrderFinalizeRes res = new OrderFinalizeRes();
        try {
            //List<DeliveryOrder> deliveryOrders ;

            //com.googlecode.objectify.cmd.Query<DeliveryOrder> q = ofy().load().type(DeliveryOrder.class);
            //deliveryOrders = q.filter("lastStatus =", filterBy).list().stream().sorted(Comparator.comparing(DeliveryOrder::getIdOrder).reversed()).filter(DeliveryOrder::getActive).collect(Collectors.toList()).subList(0,1000);

            Query<DeliveryOrder> q = ofy().load().type(DeliveryOrder.class);
            q = q.filter("isActive =",true);
//            q = q.filter("lastStatus =",filterBy);
//            q = q.limit(1000);
            //q = q.order("-idOrder").limit(100000);
            //q = q.order("-idOrder").limit(100).filter("isActive",true);
            //q = q.filter("isActive",true).limit(1000);




            Date date = new Date();
            date = ftdUtil.addSubstractDaysDate(date,-5);
            Date finalDate = date;
            //x.getCreateDate().before(finalDate)
//            List<DeliveryOrder> deliveryOrderList = deliveryOrders.stream().filter(
//                    DeliveryOrder::getActive
//            ).sorted(Comparator.comparing(DeliveryOrder::getIdOrder).reversed()).collect(Collectors.toList());

            //List<DeliveryOrder> deliveryOrderList = deliveryOrders.stream().sorted(Comparator.comparing(DeliveryOrder::getIdOrder).reversed()).collect(Collectors.toList());

            List<Long> orders =new ArrayList<>();
            for (DeliveryOrder ord: q) {
                if (filterBy.equals(ord.getLastStatus()) && ord.getCreateDate().before(date)){
                    orders.add(ord.getIdOrder());
                }

            }
            res.setOrders(orders);
            res.setMessage("Date -> " + finalDate + "\n tamano -> " + orders.size()) ;




        }catch (Exception e){
            res.setMessage(e.getMessage());
            LOG.info("Error finalize order -> " + e.getMessage());
        }

        return res;
    }

    @ApiMethod(name = "finalizeOrder", path = "/orderEndpoint/finalizeOrder", httpMethod = ApiMethod.HttpMethod.POST)
    public OrderFinalizeRes finalizeOrder(final OrderFinalize orderFinalize ) throws UnauthorizedException {

        //LOG.info(orderFinalize.toString());
        if (!orderFinalize.getKeyClient().equals(Constants.KEY_SECURE_CLIENT))
            throw new UnauthorizedException(Constants.ERROR_ACCESS_DENIED);

        OrderFinalizeRes orderFinalizeRes = null;

        try{
            List<DeliveryOrder> deliveryOrderSavedList = ofy().load().type(DeliveryOrder.class).filter("idOrder =", orderFinalize.getOrderId()).list();

            for (DeliveryOrder deliveryOrderSaved: deliveryOrderSavedList){
                orderFinalizeRes = new OrderFinalizeRes();

                LOG.info("order to finish -> " + deliveryOrderSaved.toString());
                deliveryOrderSaved.setLastStatus("order_delivered");
                deliveryOrderSaved.setActive(false);
                ofy().save().entity(deliveryOrderSaved).now();

                orderFinalizeRes.setMessage(deliveryOrderSaved.toString());
                List<Long> orders =new ArrayList<>();
                orders.add(deliveryOrderSaved.getIdOrder());
                orderFinalizeRes.setMessage("Se finalizo la orden -> " + orderFinalizeRes.toString());
                orderFinalizeRes.setOrders(orders);
            }

        }catch (Exception e){
            LOG.info("Error finalizeOrder -> " +  e.getMessage());
            orderFinalizeRes.setMessage("Error al intentar finalizar la orden");
        }


        return orderFinalizeRes;
    }
}
