package com.imaginamos.farmatodo.model.customer;

public class CustomerLifeMile {
    private Integer idCustomer;
    private String token;
    private String tokenIdWebSafe;
    private Long   lifeMileNumber;
    private Double invoiceValue;
    private String idCustomerWebSafe;

    public Integer getIdCustomer() { return idCustomer;}

    public void setIdCustomer(Integer idCustomer) { this.idCustomer = idCustomer; }

    public String getToken() { return token; }

    public void setToken(String token) { this.token = token; }

    public String getTokenIdWebSafe() { return tokenIdWebSafe; }

    public void setTokenIdWebSafe(String tokenIdWebSafe) { this.tokenIdWebSafe = tokenIdWebSafe; }

    public Long getLifeMileNumber() { return lifeMileNumber; }

    public void setLifeMileNumber(Long lifeMileNumber) { this.lifeMileNumber = lifeMileNumber; }

    public Double getInvoiceValue() { return invoiceValue; }

    public void setInvoiceValue(Double invoiceValue) { this.invoiceValue = invoiceValue; }

    public String getIdCustomerWebSafe() { return idCustomerWebSafe; }

    public void setIdCustomerWebSafe(String idCustomerWebSafe) { this.idCustomerWebSafe = idCustomerWebSafe; }

    @Override
    public String toString() {
        return "CustomerLifeMile{" +
                "idCustomer=" + idCustomer +
                ", token='" + token + '\'' +
                ", tokenIdWebSafe='" + tokenIdWebSafe + '\'' +
                ", lifeMileNumber=" + lifeMileNumber +
                ", invoiceValue=" + invoiceValue +
                ", idCustomerWebSafe='" + idCustomerWebSafe + '\'' +
                '}';
    }
}
