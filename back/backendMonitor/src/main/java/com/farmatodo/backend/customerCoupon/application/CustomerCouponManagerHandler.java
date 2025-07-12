package com.farmatodo.backend.customerCoupon.application;

import com.farmatodo.backend.customerCoupon.infrastructure.CustomerCouponRepositoryInterface;
import com.googlecode.objectify.Key;
import com.imaginamos.farmatodo.model.coupon.CustomerCoupon;
import com.imaginamos.farmatodo.model.user.User;
import com.imaginamos.farmatodo.model.util.Answer;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import static com.farmatodo.backend.OfyService.ofy;
import com.farmatodo.backend.user.Users;
import org.springframework.stereotype.Service;

@Service
public class CustomerCouponManagerHandler implements CustomerCouponManagerInterface {
    private final Users users;

    private final CustomerCouponRepositoryInterface customerCouponRepository;
    private static final Logger log = Logger.getLogger(CustomerCouponManagerHandler.class.getName());

    public CustomerCouponManagerHandler(
            Users users,
            CustomerCouponRepositoryInterface customerCouponRepository
    ) {
        this.users = users;
        this.customerCouponRepository = customerCouponRepository;
    }

    @Override
    public Answer deleteCouponByCustomerID(int customerID) {
        log.info("llegó a deleteCouponByCustomerID Handler -> " + customerID);
        Answer answer = new Answer();
        String message = "";
        User user = users.findUserByIdCustomer(customerID);
        if (Objects.nonNull(user)) {
            Key<User> userKey = Key.create(User.class, user.getIdUser());
            String messageUser = String.format("userKey -> %s ", userKey);
            log.info(messageUser);

            final List<CustomerCoupon> customerCoupons = this.customerCouponRepository.getCustomerCoupons(userKey);

            if (customerCoupons != null && !customerCoupons.isEmpty()) {
                CustomerCoupon couponToDelete = getLastCoupon(customerCoupons);
                message = String.format("Se elimina el cupon: %s, del customer %s ", couponToDelete.getCouponId(), customerID);
                this.customerCouponRepository.delete(couponToDelete);
            }
        }

        answer.setConfirmation(true);
        answer.setMessage(message);
        return answer;
    }

    private CustomerCoupon getLastCoupon(List<CustomerCoupon> customerCoupons) {
        customerCoupons.sort(Comparator.comparing(a -> a.getUseTime()));
        final int positionLastCoupon = customerCoupons.size() - 1;
        return customerCoupons.get(positionLastCoupon);
    }
}
