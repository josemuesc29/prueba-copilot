package com.imaginamos.farmatodo.backend;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;
import com.imaginamos.farmatodo.model.callcenter.CallCenterProfile;
import com.imaginamos.farmatodo.model.callcenter.CallCenterUser;
import com.imaginamos.farmatodo.model.categories.*;
import com.imaginamos.farmatodo.model.cms.Banner;
import com.imaginamos.farmatodo.model.cms.CategoryPhoto;
import com.imaginamos.farmatodo.model.cms.InfoPrivacy;
import com.imaginamos.farmatodo.model.copyright.Copyright;
import com.imaginamos.farmatodo.model.coupon.Coupon;
import com.imaginamos.farmatodo.model.coupon.CustomerCoupon;
import com.imaginamos.farmatodo.model.customer.AnonymousUser;
import com.imaginamos.farmatodo.model.customer.SavingsPrimeGeneral;
import com.imaginamos.farmatodo.model.delivery.DeliveryCost;
import com.imaginamos.farmatodo.model.favorite.Favorite;
import com.imaginamos.farmatodo.model.location.City;
import com.imaginamos.farmatodo.model.location.Country;
import com.imaginamos.farmatodo.model.location.Prefix;
import com.imaginamos.farmatodo.model.location.StoreList;
import com.imaginamos.farmatodo.model.offer.Offer;
import com.imaginamos.farmatodo.model.order.*;
import com.imaginamos.farmatodo.model.payment.LogIntentPayment;
import com.imaginamos.farmatodo.model.payment.PaymentType;
import com.imaginamos.farmatodo.model.product.*;
import com.imaginamos.farmatodo.model.store.Store;
import com.imaginamos.farmatodo.model.store.StoreGroup;
import com.imaginamos.farmatodo.model.user.*;
import com.imaginamos.farmatodo.model.util.AppVersion;
import com.imaginamos.farmatodo.model.util.Property;
import com.imaginamos.farmatodo.model.util.Segment;


/**
 * Created by lelal on 11/10/2016.
 * Created by Imaginamos
 */

public class OfyService {

  private OfyService() {
  }

  static {
    ObjectifyService.register(AnonymousUser.class);
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
    ObjectifyService.register(FilterName.class);
    ObjectifyService.register(Filter.class);
    ObjectifyService.register(DeliveryOrder.class);
    ObjectifyService.register(DeliveryOrderItem.class);
    ObjectifyService.register(Offer.class);
    ObjectifyService.register(CrossSales.class);
    ObjectifyService.register(Substitutes.class);
    ObjectifyService.register(DeliveryCost.class);
    ObjectifyService.register(PaymentType.class);
    ObjectifyService.register(Tracing.class);
    ObjectifyService.register(Highlight.class);
    ObjectifyService.register(Country.class);
    ObjectifyService.register(ItemMostSales.class);
    ObjectifyService.register(AppVersion.class);
    ObjectifyService.register(InfoPrivacy.class);
    ObjectifyService.register(Coupon.class);
    ObjectifyService.register(CustomerCoupon.class);
    ObjectifyService.register(Prefix.class);
    ObjectifyService.register(CategoryPhoto.class);
    ObjectifyService.register(StoreList.class);
    ObjectifyService.register(Favorite.class);
    ObjectifyService.register(DeliveryOrderProvider.class);
    ObjectifyService.register(Copyright.class);
    ObjectifyService.register(BlockedUser.class);
    ObjectifyService.register(Property.class);
    ObjectifyService.register(ProcessedOrder.class);
    ObjectifyService.register(LogIntentPayment.class);
    ObjectifyService.register(PushNotification.class);
    ObjectifyService.register(EmailChangeUser.class);
    ObjectifyService.register(SavingsPrimeGeneral.class);
  }

  public static Objectify ofy() {
    return ObjectifyService.ofy();
  }

  public static ObjectifyFactory factory() {
    return ObjectifyService.factory();
  }
}
