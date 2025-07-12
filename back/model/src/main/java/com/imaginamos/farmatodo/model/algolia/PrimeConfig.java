package com.imaginamos.farmatodo.model.algolia;


import java.util.List;

public class PrimeConfig {

    public PaymentMethodsAlgoliaConfig paymentMethods;

    public List<PaymentMethodsAlgoliaConfigV2> paymentMethodsV2;
    public List<PaymentMessageConfig> paymentMessages = null;

    public Long freeDaysForPrime;
    public boolean featureValidateUserPrime;
    public boolean freePrimeEnabled;

    public boolean deviceIdValidation;

    public List<PrimePlan> prime_plans;

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

    public List<PrimePlan> getPrime_plans() {
        return prime_plans;
    }

    public void setPrime_plans(List<PrimePlan> prime_plans) {
        this.prime_plans = prime_plans;
    }
}
