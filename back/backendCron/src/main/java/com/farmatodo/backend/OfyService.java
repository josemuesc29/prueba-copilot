package com.farmatodo.backend;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;
import com.imaginamos.farmatodo.model.callcenter.CallCenterProfile;
import com.imaginamos.farmatodo.model.callcenter.CallCenterUser;
import com.imaginamos.farmatodo.model.categories.*;
import com.imaginamos.farmatodo.model.cms.Banner;
import com.imaginamos.farmatodo.model.delivery.DeliveryCost;
import com.imaginamos.farmatodo.model.location.City;
import com.imaginamos.farmatodo.model.location.StoreList;
import com.imaginamos.farmatodo.model.offer.Offer;
import com.imaginamos.farmatodo.model.order.DeliveryOrder;
import com.imaginamos.farmatodo.model.order.DeliveryOrderItem;
import com.imaginamos.farmatodo.model.order.ProcessedOrder;
import com.imaginamos.farmatodo.model.product.*;
import com.imaginamos.farmatodo.model.store.Store;
import com.imaginamos.farmatodo.model.store.StoreGroup;
import com.imaginamos.farmatodo.model.user.*;
import com.imaginamos.farmatodo.model.util.Segment;
import com.imaginamos.farmatodo.model.util.VersionControl;


/**
 * Created by lelal on 11/10/2016.
 * Created by Imaginamos
 */

public class OfyService {
  static {
    ObjectifyService.register(Banner.class);
    ObjectifyService.register(CallCenterProfile.class);
    ObjectifyService.register(CallCenterUser.class);
    ObjectifyService.register(City.class);
    ObjectifyService.register(Credential.class);
    ObjectifyService.register(DeviceRegistry.class);
    ObjectifyService.register(Item.class);
    ObjectifyService.register(Segment.class);
    ObjectifyService.register(Store.class);
    ObjectifyService.register(StoreGroup.class);
    ObjectifyService.register(Token.class);
    ObjectifyService.register(User.class);
    ObjectifyService.register(UserPass.class);
    ObjectifyService.register(Classification.class);
    ObjectifyService.register(Department.class);
    ObjectifyService.register(Category.class);
    ObjectifyService.register(SubCategory.class);
    ObjectifyService.register(SubCategory.class);
    ObjectifyService.register(FilterName.class);
    ObjectifyService.register(Filter.class);
    ObjectifyService.register(DeliveryOrder.class);
    ObjectifyService.register(DeliveryOrderItem.class);
    ObjectifyService.register(Offer.class);
    ObjectifyService.register(CrossSales.class);
    ObjectifyService.register(Substitutes.class);
    ObjectifyService.register(DeliveryCost.class);
    ObjectifyService.register(Highlight.class);
    ObjectifyService.register(ItemGroup.class);
    ObjectifyService.register(ItemMostSales.class);
    ObjectifyService.register(VersionControl.class);
    ObjectifyService.register(StoreList.class);

  }

  public static Objectify ofy() {
    return ObjectifyService.ofy();
  }

  public static ObjectifyFactory factory() {
    return ObjectifyService.factory();
  }
}
