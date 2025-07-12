package com.imaginamos.farmatodo.model.customer;

public class CustomerLifeMileJSON {
    private Long   lifeMileNumber;
    private Double invoiceValue;
    private Double lifeMileValue;
    private Double minLifeMileValue;
    private Long   customerId;

    public Long getLifeMileNumber() { return lifeMileNumber; }

    public void setLifeMileNumber(Long lifeMileNumber) { this.lifeMileNumber = lifeMileNumber; }

    public Double getInvoiceValue() { return invoiceValue; }

    public void setInvoiceValue(Double invoiceValue) { this.invoiceValue = invoiceValue; }

    public Double getLifeMileValue() { return lifeMileValue; }

    public void setLifeMileValue(Double lifeMileValue) { this.lifeMileValue = lifeMileValue; }

    public Double getMinLifeMileValue() { return minLifeMileValue; }

    public void setMinLifeMileValue(Double minLifeMileValue) { this.minLifeMileValue = minLifeMileValue; }

    public Long getCustomerId() { return customerId; }

    public void setCustomerId(Long customerId) { this.customerId = customerId; }
}
