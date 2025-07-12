package com.imaginamos.farmatodo.backend.order.create_order.application;

import com.google.gson.Gson;
import com.imaginamos.farmatodo.model.order.DeliveryOrder;
import com.imaginamos.farmatodo.model.order.DeliveryOrderItem;
import com.imaginamos.farmatodo.model.order.DeliveryOrderProvider;
import com.imaginamos.farmatodo.model.talonone.DeductDiscount;
import com.imaginamos.farmatodo.model.util.AnswerDeduct;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.networking.cache.CachedDataManager;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

public class ExperienceCoupons {

    private static final Logger LOG = Logger.getLogger(ExperienceCoupons.class.getName());

    public void deductDiscount(String idCustomerWebSafe, DeliveryOrder deliveryOrder, AnswerDeduct traditionalCoupon) {
        if (Objects.nonNull(deliveryOrder.getItemList())) {
            DeductDiscount deductDiscount = new DeductDiscount();
            deductDiscountWithoutCoupon(deliveryOrder, deductDiscount);
            equaliceValuesOfferPrice(deliveryOrder, deductDiscount, traditionalCoupon);
            if (!deliveryOrder.hasCouponTalon()) {
                String keyNameCoupon = Constants.KEY_COUPON_CACHE;
                AnswerDeduct couponMediosDigitales = existCouponInRedis(idCustomerWebSafe, keyNameCoupon, Constants.INDEX_REDIS_FOURTEEN);
                if (Objects.nonNull(couponMediosDigitales)) {
                    //LOG.info("entro a coupon DIGITAL MEDIA");
                    com.imaginamos.farmatodo.model.talonone.Coupon coupon = mapAnswerToCouponTalonOrRPM(couponMediosDigitales, deliveryOrder);
                    deliveryOrder.setCoupon(coupon);
                    if (deliveryOrder.hasCouponDigitalMedia() && Objects.isNull(deliveryOrder.getCouponAutomaticTalonList())){
                        deliveryOrder.setOfferPrice(deliveryOrder.getOfferPrice() - coupon.getDiscountCoupon());
                    }
                    else if(deliveryOrder.hasCouponDigitalMedia()
                            && Objects.nonNull(deliveryOrder.getCouponAutomaticTalonList())
                            && deliveryOrder.getCouponAutomaticTalonList().get(0).getDiscountCoupon()>0){
                        Double totalOrder=deliveryOrder.getSubTotalPrice() -(deliveryOrder.getOfferPrice()+deliveryOrder.getCoupon().getDiscountCoupon());
                        if(totalOrder<0){
                            totalOrder=0.0;
                        }
                        totalOrder+=(Objects.nonNull(deliveryOrder.getTipPrice()) ? deliveryOrder.getTipPrice() : 0.0);
                        deliveryOrder.setTotalPrice(totalOrder);
                    }
                }
            }
            savedInRedisDeductDiscount(idCustomerWebSafe, deliveryOrder);
        }
    }

    private  void equaliceValuesOfferPrice(DeliveryOrder deliveryOrder, DeductDiscount deductDiscount, AnswerDeduct traditionalCoupon) {
        LOG.info("equaliceValuesOfferPrice - deliveryOrder -> " + new Gson().toJson(deliveryOrder));
        Double offerPriceInitial = deliveryOrder.getOfferPrice();

        if (Objects.nonNull(deliveryOrder.getCouponAutomaticTalonList())
                && !deliveryOrder.getCouponAutomaticTalonList().isEmpty()
                && Objects.nonNull(deductDiscount)
                && Objects.nonNull(deliveryOrder.getCouponAutomaticTalonList().get(0).getDiscountCoupon())) {
            //LOG.info("entro a coupon automatico");
            Double discountCouponAutomatic = deliveryOrder.getCouponAutomaticTalonList().get(0).getDiscountCoupon();
            Double suma = 0.0;
            if(deliveryOrder.isPrimeDiscountFlag()){
                suma += deductDiscount.getTotalSaveProducts()
                        + (Objects.nonNull(deductDiscount.getDiscountProductsPrime()) ? deductDiscount.getDiscountProductsPrime() : 0D)
                        + discountCouponAutomatic;
            }else{
                suma += deductDiscount.getTotalSaveProducts() + discountCouponAutomatic;
            }

            if (Objects.nonNull(deliveryOrder.getCoupon())
                    && Objects.nonNull(deliveryOrder.getCoupon().getDiscountCoupon())
                    && deliveryOrder.getCoupon().getDiscountCoupon() > 0) {
                Double totalOrder = deliveryOrder.getSubTotalPrice() -
                        (((suma>0D)?suma:deliveryOrder.getCouponAutomaticTalonList().get(0).getDiscountCoupon()) + deliveryOrder.getCoupon().getDiscountCoupon());
                if(totalOrder<0){
                    totalOrder=0.0;
                }
                totalOrder+=(Objects.nonNull(deliveryOrder.getTipPrice()) ? deliveryOrder.getTipPrice() : 0.0)
                        +(Objects.nonNull(deliveryOrder.getDeliveryPrice()) ? deliveryOrder.getDeliveryPrice() : 0.0);
                //LOG.info("entro a coupon totalOrder: " + totalOrder);
                deliveryOrder.setOfferPrice(((suma>0D)?suma:deliveryOrder.getCouponAutomaticTalonList().get(0).getDiscountCoupon()));
                deliveryOrder.setTotalPrice(totalOrder);
            } else {
                Double totalOrder = deliveryOrder.getSubTotalPrice() -
                        ((suma>0D)?suma:deliveryOrder.getCouponAutomaticTalonList().get(0).getDiscountCoupon());
                if(totalOrder<0){
                    totalOrder=0.0;
                }
                totalOrder+=(Objects.nonNull(deliveryOrder.getTipPrice()) ? deliveryOrder.getTipPrice() : 0.0)
                        +(Objects.nonNull(deliveryOrder.getDeliveryPrice()) ? deliveryOrder.getDeliveryPrice() : 0.0);
                deliveryOrder.setOfferPrice(((suma>0D)?suma:deliveryOrder.getCouponAutomaticTalonList().get(0).getDiscountCoupon()));

                if(Objects.nonNull(traditionalCoupon) && Objects.nonNull(traditionalCoupon.getMessage())){
                    if(deliveryOrder.getTotalPrice() < totalOrder){
                        com.imaginamos.farmatodo.model.talonone.Coupon coupon = new com.imaginamos.farmatodo.model.talonone.Coupon();
                        coupon.setNameCoupon(traditionalCoupon.getNameCoupon());
                        coupon.setDiscountCoupon(traditionalCoupon.getDiscount());
                        coupon.setTypeNotificacion(traditionalCoupon.getTypeNotifcation());
                        coupon.setTalonOneOfferDescription(traditionalCoupon.getNotificationMessage());
                        //traditionalCoupon.setDiscountCoupon(deliveryOrder.getTotalPrice() - totalOrder);
                        deliveryOrder.setCoupon(coupon);
                    }
                }else
                    deliveryOrder.setTotalPrice(totalOrder);
            }
        }
        else if(Objects.nonNull(deliveryOrder.getCoupon()) && Objects.nonNull(deductDiscount)){
            //LOG.info("entro a coupon y deductDiscount");
            Double offerPrice=0.0;
            if(deliveryOrder.isPrimeDiscountFlag()){
                offerPrice += deductDiscount.getTotalSaveProducts()
                        + (Objects.nonNull(deductDiscount.getDiscountProductsPrime()) ? deductDiscount.getDiscountProductsPrime() : 0D);
            }else{
                offerPrice += deductDiscount.getTotalSaveProducts();
            }
            LOG.info("offerPrice: " + offerPrice + " deliveryOrder.getCoupon().getDiscountCoupon(): "
                    + deliveryOrder.getCoupon().getDiscountCoupon()
                    + " deliveryOrder.getSubTotalPrice(): " +deliveryOrder.getSubTotalPrice()
                    + " deliveryOrder.getTipPrice(): " + (Objects.nonNull(deliveryOrder.getTipPrice()) ? deliveryOrder.getTipPrice() : 0.0));
            Double totalOrder = deliveryOrder.getSubTotalPrice() - (offerPrice
                    + (Objects.nonNull(deliveryOrder.getCoupon().getDiscountCoupon())?deliveryOrder.getCoupon().getDiscountCoupon():0.0));
            if(totalOrder<0){
                totalOrder=0.0;
            }
            totalOrder+=(Objects.nonNull(deliveryOrder.getTipPrice()) ? deliveryOrder.getTipPrice() : 0.0)
                    +(Objects.nonNull(deliveryOrder.getDeliveryPrice()) ? deliveryOrder.getDeliveryPrice() : 0.0);
            deliveryOrder.setOfferPrice(offerPrice);
            deliveryOrder.setTotalPrice(totalOrder);
        }
        else{
            if (Objects.nonNull(deductDiscount) && Objects.isNull(deliveryOrder.getCoupon())
                    && Objects.isNull(deliveryOrder.getCouponAutomaticTalonList())
                    && ((deliveryOrder.isPrimeDiscountFlag() && Objects.nonNull(deductDiscount.getDiscountProductsPrime()) && deductDiscount.getDiscountProductsPrime() > 0)
                    || (Objects.nonNull(deductDiscount.getDiscountProductsOfferPrice()) && deductDiscount.getDiscountProductsOfferPrice() > 0))) {
                //LOG.info("entro a  deductDiscount y prime o discountOferPrice, no cupon tardicional");
                Double offerPrice = 0.0;
                if (deliveryOrder.isPrimeDiscountFlag()
                        && Objects.nonNull(deductDiscount.getDiscountProductsPrime())
                        && deductDiscount.getDiscountProductsPrime() > 0) {
                    offerPrice += deductDiscount.getTotalSaveProducts() + deductDiscount.getDiscountProductsPrime();
                } else if (Objects.nonNull(deductDiscount.getDiscountProductsOfferPrice())
                        && deductDiscount.getDiscountProductsOfferPrice() > 0) {
                    offerPrice += deductDiscount.getTotalSaveProducts();
                }
                Double totalOrder = deliveryOrder.getSubTotalPrice() - offerPrice;
                if (totalOrder < 0) {
                    totalOrder = 0.0;
                }
                totalOrder += (Objects.nonNull(deliveryOrder.getTipPrice()) ? deliveryOrder.getTipPrice() : 0.0)
                        + (Objects.nonNull(deliveryOrder.getDeliveryPrice()) ? deliveryOrder.getDeliveryPrice() : 0.0);
                deliveryOrder.setOfferPrice(offerPrice);
                // deliveryOrder.setTotalPrice(totalOrder);
            }
            else if(Objects.nonNull(deductDiscount) && deductDiscount.getTotalSaveProducts()>0) {
                 //LOG.info("entro solamente deductDiscount y prime con cupon tradicional");


                double totalDiscount = 0.0;
                if (deliveryOrder.isPrimeDiscountFlag()) {
                    totalDiscount += deductDiscount.getTotalSaveProducts() + deductDiscount.getDiscountProductsPrime();
                } else totalDiscount += deductDiscount.getTotalSaveProducts();

                if(offerPriceInitial > totalDiscount)
                    totalDiscount = offerPriceInitial;

                double totalOrder = deliveryOrder.getSubTotalPrice() - totalDiscount;

                deliveryOrder.setOfferPrice(totalDiscount);
                deliveryOrder.setTotalPrice(totalOrder + deliveryOrder.getTotalDelivery() + (Objects.nonNull(deliveryOrder.getTipPrice()) ? deliveryOrder.getTipPrice() : 0.0));

            }
        }
    }

    /**
     * validate if exist coupon de MD in Redis
     * existInRedis
     *
     * @param idCustomerWebSafe
     */
    public static AnswerDeduct existCouponInRedis(String idCustomerWebSafe, String nameKey, Integer NUM_BD_REDIS) {
        AnswerDeduct answerCouponTalonOrMD = null;
        if (Objects.nonNull(idCustomerWebSafe)) {
            Optional<String> couponTalonOrRPM = CachedDataManager.getJsonFromCacheIndex( nameKey, NUM_BD_REDIS);
            if (couponTalonOrRPM.isPresent()) {
                answerCouponTalonOrMD = new Gson().fromJson(couponTalonOrRPM.get(), AnswerDeduct.class);
            }
        }

        //LOG.info("ED answerCouponTalonOrMD: " + new Gson().toJson(answerCouponTalonOrMD));
        return answerCouponTalonOrMD;
    }

    private com.imaginamos.farmatodo.model.talonone.Coupon mapAnswerToCouponTalonOrRPM(AnswerDeduct answerDeduct, DeliveryOrder deliveryOrder) {
        com.imaginamos.farmatodo.model.talonone.Coupon coupon = new com.imaginamos.farmatodo.model.talonone.Coupon();
        coupon.setCouponMessage(answerDeduct.getMessage());
        coupon.setNameCoupon(answerDeduct.getNameCoupon());
        coupon.setDiscountCoupon(answerDeduct.getDiscount());
        coupon.setTalonOneOfferDescription(answerDeduct.getNotificationMessage());
        //LOG.info("answer:" + new Gson().toJson(answerDeduct));
        if (Objects.nonNull(answerDeduct.getRestrictionValue()) && answerDeduct.getRestrictionValue() < deliveryOrder.getSubTotalPrice()) {
            coupon.setTypeNotificacion(answerDeduct.getTypeNotifcation());
        } else {
            coupon.setTypeNotificacion("Error");
        }
        LOG.info("coupon:" + new Gson().toJson(coupon));
        return coupon;
    }

    /**
     * save in Redis the coupons Talon-RPM and deduct discount
     * savedInRedisDeductDiscountAndCoupon
     *
     * @param idCustomerWebSafe
     * @param deliveryOrder
     */
    private static void savedInRedisDeductDiscount(String idCustomerWebSafe, DeliveryOrder deliveryOrder) {
        if (Objects.nonNull(deliveryOrder)) {
            if (Objects.nonNull(deliveryOrder.getDeductDiscount().getDiscountProductsRPM())) {
                CachedDataManager.saveJsonInCacheIndexTime(idCustomerWebSafe + Constants.KEY_DISCOUNT_RPM, String.valueOf(deliveryOrder.getDeductDiscount().getDiscountProductsRPM()), 14, 600);
            }
            if (Objects.nonNull(deliveryOrder.getDeductDiscount().getDiscountProductsPrime())) {
                CachedDataManager.saveJsonInCacheIndexTime(idCustomerWebSafe + Constants.KEY_DISCOUNT_PRIME, String.valueOf(deliveryOrder.getDeductDiscount().getDiscountProductsPrime()), 14, 600);
            }
            if (Objects.nonNull(deliveryOrder.getDeductDiscount().getDiscountProductsCampaignTalon())) {
                CachedDataManager.saveJsonInCacheIndexTime(idCustomerWebSafe + Constants.KEY_DISCOUNT_TALON, String.valueOf(deliveryOrder.getDeductDiscount().getDiscountProductsCampaignTalon()), 14, 600);
            }
            if (Objects.nonNull(deliveryOrder.getDeductDiscount().getTotalSaveProducts())) {
                CachedDataManager.saveJsonInCacheIndexTime(idCustomerWebSafe + Constants.KEY_TOTAL_SAVE, String.valueOf(deliveryOrder.getDeductDiscount().getTotalSaveProducts()), 14, 600);
            }
            if (Objects.nonNull(deliveryOrder.getDeductDiscount().getDiscountProductsOfferPrice())) {
                CachedDataManager.saveJsonInCacheIndexTime(idCustomerWebSafe + Constants.KEY_OFFER_PRICE, String.valueOf(deliveryOrder.getDeductDiscount().getDiscountProductsOfferPrice()), 14, 600);
            }
        }
    }

    /**
     * filter items by discount
     * deductDiscountWithoutCoupon
     *
     * @param deliveryOrder
     * @param deductDiscount
     */
    private  void deductDiscountWithoutCoupon(DeliveryOrder deliveryOrder, DeductDiscount deductDiscount) {
        //sum discount product prime
        deductDiscount.setDiscountProductsPrime(Double.valueOf(deliveryOrder.getItemList().stream()
                .filter(item -> Objects.nonNull(item) && Objects.nonNull(item.getPrimePrice())
                        && Objects.nonNull(item.getFullPrice()) && item.getPrimePrice() > 0.0)
                .mapToDouble(item -> (item.getFullPrice() * item.getQuantitySold()) - (item.getPrimePrice() * item.getQuantitySold())).sum()));

        //sum discount product RPM
        deductDiscount.setDiscountProductsRPM(Double.valueOf(deliveryOrder.getItemList().stream()
                .filter(item -> Objects.nonNull(item) && Objects.nonNull(item.getOfferPrice())
                        && Objects.nonNull(item.getFullPrice()) && Objects.nonNull(item.isTalonDiscount())
                        && Objects.nonNull(item.getPrimePrice()) && !item.isTalonDiscount()
                        && item.getPrimePrice().equals(0.0) && item.getOfferPrice() > 0.0)
                .mapToDouble(item -> (item.getFullPrice() * item.getQuantitySold()) - (item.getOfferPrice() * item.getQuantitySold())).sum()));

        //sum discount product TalonOne
        deductDiscount.setDiscountProductsCampaignTalon(Double.valueOf(deliveryOrder.getItemList().stream()
                .filter(item -> Objects.nonNull(item) && Objects.nonNull(item.getOfferPrice())
                        && Objects.nonNull(item.getFullPrice()) && Objects.nonNull(item.isTalonDiscount())
                        && item.getOfferPrice() > 0.0 && item.isTalonDiscount())
                .mapToDouble(item -> (item.getFullPrice() * item.getQuantitySold()) - (item.getOfferPrice() * item.getQuantitySold())).sum()));

        //sum discount product offerPrice
        deductDiscount.setDiscountProductsOfferPrice(Double.valueOf(deliveryOrder.getItemList().stream()
                .filter(item -> Objects.nonNull(item) && Objects.nonNull(item.getOfferPrice())
                        && Objects.nonNull(item.getFullPrice()) && Objects.nonNull(item.isTalonDiscount())
                        && item.getOfferPrice() > 0.0 && !item.isTalonDiscount()
                        && Objects.nonNull(item.getPrimePrice()) && item.getPrimePrice() > 0.0)
                .mapToDouble(item -> (item.getFullPrice() * item.getQuantitySold()) - (item.getOfferPrice() * item.getQuantitySold())).sum()));

        //sum discount product Provider
        double discountTotalPO = calculateDiscountProductsOnline(deliveryOrder.getProviderList());
        deductDiscount.setDiscountProductsOnLine(Double.valueOf(discountTotalPO));

        Double sumDiscountFtd = 0D;
        if (!deliveryOrder.isPrimeDiscountFlag()) {
            sumDiscountFtd = deductDiscount.getDiscountProductsRPM()
                    + deductDiscount.getDiscountProductsCampaignTalon();
            if (Objects.nonNull(deductDiscount.getDiscountProductsOfferPrice())) {
                sumDiscountFtd += deductDiscount.getDiscountProductsOfferPrice();
            }
            if (Objects.nonNull(deductDiscount.getDiscountProductsOnLine())) {
                sumDiscountFtd += deductDiscount.getDiscountProductsOnLine();
            }
        } else {
            sumDiscountFtd = deductDiscount.getDiscountProductsRPM()
                    + deductDiscount.getDiscountProductsCampaignTalon();
            if (Objects.nonNull(deductDiscount.getDiscountProductsOnLine())) {
                sumDiscountFtd += deductDiscount.getDiscountProductsOnLine();
            }
        }

        deductDiscount.setTotalSaveProducts(sumDiscountFtd);
        deliveryOrder.setDeductDiscount(deductDiscount);
    }

    private double calculateDiscountProductsOnline(List<DeliveryOrderProvider> providers) {
        if (providers.isEmpty()) return 0.0;

        DeliveryOrderProvider listProviders = providers.get(0);
        return listProviders.getItemList().stream()
                .filter(this::productIsValid)
                .mapToDouble(this::calculateDiscountItem)
                .sum();
    }
    private  boolean productIsValid(DeliveryOrderItem item) {
        return Objects.nonNull(item) &&
                Objects.nonNull(item.getOfferPrice()) &&
                item.getOfferPrice() > 0.0 &&
                !item.isTalonDiscount() &&
                Objects.nonNull(item.getFullPrice());
    }
    private double calculateDiscountItem(DeliveryOrderItem item) {
        return (item.getFullPrice() - item.getOfferPrice()) * item.getQuantitySold();
    }

}
