package com.imaginamos.farmatodo.model.algolia;


import java.util.List;

public class PrimeConfigV2 {

    public PaymentMethodsAlgoliaConfig paymentMethods;

    public List<PaymentMethodsAlgoliaConfigV2> paymentMethodsV2;
    public List<PaymentMessageConfig> paymentMessages = null;
    public List<PrimePlan> prime_plans;
    public Long freeDaysForPrime;
    public boolean featureValidateUserPrime;
    public boolean freePrimeEnabled;

    public boolean deviceIdValidation;

    public List<PrimePlan> getPrime_plans() {
        return prime_plans;
    }

    public void setPrime_plans(List<PrimePlan> prime_plans) {
        this.prime_plans = prime_plans;
    }

    public boolean isFeatureValidateUserPrime() {
        return featureValidateUserPrime;
    }

    public void setFeatureValidateUserPrime(boolean featureValidateUserPrime) {
        this.featureValidateUserPrime = featureValidateUserPrime;
    }

    @Override
    public String toString() {
        return "PrimeConfig{" +
                "paymentMethodsV2=" + paymentMethodsV2.toString() +
                ", paymentMessages=" + paymentMessages +
                ", primePlans=" + prime_plans +
                '}';
    }

    public List<PaymentMethodsAlgoliaConfigV2> getPaymentMethodsV2() {
        return paymentMethodsV2;
    }

    public void setPaymentMethodsV2(List<PaymentMethodsAlgoliaConfigV2> paymentMethodsV2) {
        this.paymentMethodsV2 = paymentMethodsV2;
    }

    public List<PaymentMessageConfig> getPaymentMessages() {
        return paymentMessages;
    }

    public void setPaymentMessages(List<PaymentMessageConfig> paymentMessages) {
        this.paymentMessages = paymentMessages;
    }

    public List<PrimePlan> getPrimePlans() {
        return prime_plans;
    }

    public void setPrimePlans(List<PrimePlan> primePlans) {
        this.prime_plans = primePlans;
    }

    public Long getFreeDaysForPrime() {
        return freeDaysForPrime;
    }

    public void setFreeDaysForPrime(Long freeDaysForPrime) {
        this.freeDaysForPrime = freeDaysForPrime;
    }

    public boolean isFreePrimeEnabled() {
        return freePrimeEnabled;
    }

    public void setFreePrimeEnabled(boolean freePrimeEnabled) {
        this.freePrimeEnabled = freePrimeEnabled;
    }

    public boolean isDeviceIdValidation() {
        return deviceIdValidation;
    }

    public void setDeviceIdValidation(boolean deviceIdValidation) {
        this.deviceIdValidation = deviceIdValidation;
    }

    public PaymentMethodsAlgoliaConfig getPaymentMethods() {
        return paymentMethods;
    }

    public void setPaymentMethods(PaymentMethodsAlgoliaConfig paymentMethods) {
        this.paymentMethods = paymentMethods;
    }
}
