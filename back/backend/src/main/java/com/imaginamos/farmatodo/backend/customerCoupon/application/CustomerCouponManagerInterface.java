package com.imaginamos.farmatodo.backend.customerCoupon.application;

import com.imaginamos.farmatodo.model.util.Answer;
import org.springframework.stereotype.Service;

@Service
public interface CustomerCouponManagerInterface {
    public Answer deleteCouponByCustomerID(int customerID);
}
