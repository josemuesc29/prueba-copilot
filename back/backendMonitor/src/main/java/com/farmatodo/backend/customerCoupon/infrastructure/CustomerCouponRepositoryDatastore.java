package com.farmatodo.backend.customerCoupon.infrastructure;

import com.googlecode.objectify.Key;
import com.imaginamos.farmatodo.model.coupon.CustomerCoupon;
import com.imaginamos.farmatodo.model.user.User;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.farmatodo.backend.OfyService.ofy;

@Service
public class CustomerCouponRepositoryDatastore implements CustomerCouponRepositoryInterface{
    @Override
    public void delete(CustomerCoupon customerCoupon) {
        ofy().delete().entity(customerCoupon).now();
    }

    public List<CustomerCoupon> getCustomerCoupons(Key<User> userKey) {
        return ofy().load().type(CustomerCoupon.class)
                .filter("customerKey", userKey)
                .orderKey(false)
                .list();
    }
}
