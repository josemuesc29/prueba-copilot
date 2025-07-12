package com.imaginamos.farmatodo.model.payment;

import java.util.List;

public class PSEResponse {
    private List<Institutions> financialInstitutions;
    private List<String> typePerson;

    public List<Institutions> getFinancialInstitutions() {
        return financialInstitutions;
    }

    public void setFinancialInstitutions(List<Institutions> financialInstitutions) {
        this.financialInstitutions = financialInstitutions;
    }

    public List<String> getTypePerson() {
        return typePerson;
    }

    public void setTypePerson(List<String> typePerson) {
        this.typePerson = typePerson;
    }
}
