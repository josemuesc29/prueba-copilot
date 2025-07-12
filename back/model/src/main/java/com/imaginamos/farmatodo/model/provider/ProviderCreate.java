package com.imaginamos.farmatodo.model.provider;

import java.util.ArrayList;
import java.util.List;

public class ProviderCreate {
    private List<WebServiceClient> providers;

    public ProviderCreate() {
        this.providers = new ArrayList<>();
    }

    public List<WebServiceClient> getProviders() {
        return providers;
    }

    public void setProviders(List<WebServiceClient> providers) {
        this.providers = providers;
    }
}
