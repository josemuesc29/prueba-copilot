package com.imaginamos.farmatodo.backend.customerCoupon.application;

import com.googlecode.objectify.Key;
import com.imaginamos.farmatodo.backend.customerCoupon.infrastructure.CustomerCouponRepositoryDatastore;
import com.imaginamos.farmatodo.backend.customerCoupon.infrastructure.CustomerCouponRepositoryInterface;
import com.imaginamos.farmatodo.backend.user.Users;
import com.imaginamos.farmatodo.model.coupon.CustomerCoupon;
import com.imaginamos.farmatodo.model.user.User;
import com.imaginamos.farmatodo.model.util.Answer;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

@Service
public class CustomerCouponManagerHandler implements CustomerCouponManagerInterface {
    private Users users;
    private CustomerCouponRepositoryDatastore customerCouponRepository;
    private static final Logger log = Logger.getLogger(CustomerCouponManagerHandler.class.getName());

    public CustomerCouponManagerHandler(
    ) {
        this.users = new Users();
        this.customerCouponRepository = new CustomerCouponRepositoryDatastore();
    }

    @Override
    public Answer deleteCouponByCustomerID(int customerID) {
        Answer answer = new Answer();
        String message = "";
        User user = users.findUserByIdCustomer(customerID);
        if (Objects.nonNull(user)) {
            Key<User> userKey = getKeyUser(user);
            String messageUser = String.format("customerID %s, userKey -> %s ", customerID, userKey);
            //log.info(messageUser);

            final List<CustomerCoupon> customerCoupons = this.customerCouponRepository.getCustomerCoupons(userKey);

            if (customerCoupons != null && !customerCoupons.isEmpty()) {
                CustomerCoupon couponToDelete = getLastCoupon(customerCoupons);
                message = String.format("Se elimina el cupon: %s, del customer %s", couponToDelete.getCouponId(), customerID);
                this.customerCouponRepository.delete(couponToDelete);
                log.info(message);
            }
        }

        answer.setConfirmation(true);
        answer.setMessage(message);
        return answer;
    }

    public Key<User> getKeyUser(User user) {
        return Key.create(User.class, user.getIdUser());
    }

    private CustomerCoupon getLastCoupon(List<CustomerCoupon> customerCoupons) {
        customerCoupons.sort(Comparator.comparing(a -> a.getUseTime()));
        final int positionLastCoupon = customerCoupons.size() - 1;
        return customerCoupons.get(positionLastCoupon);
    }
}
