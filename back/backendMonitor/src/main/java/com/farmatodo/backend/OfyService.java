package com.farmatodo.backend;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;
import com.imaginamos.farmatodo.model.coupon.CustomerCoupon;
import com.imaginamos.farmatodo.model.customer.AnonymousUser;
import com.imaginamos.farmatodo.model.order.DeliveryOrder;
import com.imaginamos.farmatodo.model.order.DeliveryOrderItem;
import com.imaginamos.farmatodo.model.order.DeliveryOrderProvider;
import com.imaginamos.farmatodo.model.product.Item;
import com.imaginamos.farmatodo.model.user.BlockedUser;
import com.imaginamos.farmatodo.model.user.Credential;
import com.imaginamos.farmatodo.model.user.User;

/**
 * Created by lelal on 11/10/2016.
 * Created by Imaginamos
 */

public class OfyService {

  private OfyService() {
  }

  static {
    ObjectifyService.register(AnonymousUser.class);
    ObjectifyService.register(User.class);
    ObjectifyService.register(DeliveryOrder.class);
    ObjectifyService.register(DeliveryOrderItem.class);
    ObjectifyService.register(Item.class);
    ObjectifyService.register(DeliveryOrderProvider.class);
    ObjectifyService.register(BlockedUser.class);
    ObjectifyService.register(Credential.class);
    ObjectifyService.register(CustomerCoupon.class);
  }

  public static Objectify ofy() {
    return ObjectifyService.ofy();
  }

  public static ObjectifyFactory factory() {
    return ObjectifyService.factory();
  }
}
