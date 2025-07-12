package com.imaginamos.farmatodo.backend.util;

import com.imaginamos.farmatodo.model.algolia.cuponFilters.MessagesError;
import com.imaginamos.farmatodo.model.coupon.CouponErrorTypeENUM;

import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public class CouponErrorMessages {
    private static final Logger LOG = Logger.getLogger(CouponErrorMessages.class.getName());

    public static String obtainErrorAlgolia(String couponName, CouponErrorTypeENUM errorType, List<MessagesError> errorTypeList) {
        String msg = "";
        if(couponName == null || couponName.isEmpty() || errorType == null
                || errorTypeList == null || errorTypeList.isEmpty()) {
            return msg;
        }
        for (MessagesError error : errorTypeList) {
            if (Objects.equals(error.getCouponTypeMessage(), couponName) && Objects.equals(error.getTypeError(), errorType.name())) {
                msg = error.getValue();
                return msg;
            }
        }
        return msg;
    }

}
