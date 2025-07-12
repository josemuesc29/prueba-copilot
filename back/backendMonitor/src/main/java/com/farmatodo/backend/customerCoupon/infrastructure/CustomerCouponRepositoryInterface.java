package com.farmatodo.backend.customerCoupon.infrastructure;

import com.googlecode.objectify.Key;
import com.imaginamos.farmatodo.model.coupon.CustomerCoupon;
import com.imaginamos.farmatodo.model.user.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CustomerCouponRepositoryInterface {

    public List<CustomerCoupon> getCustomerCoupons(Key<User> userKey);
    public void delete(CustomerCoupon customerCoupon);
}
