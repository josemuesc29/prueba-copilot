package com.imaginamos.farmatodo.model.algolia;

import java.util.List;

public class CustomerCallCenterAlgolia {
    private List<CustomerCallCenterData> users;

    public List<CustomerCallCenterData> getUsers() {
        return users;
    }

    public void setUsers(List<CustomerCallCenterData> users) {
        this.users = users;
    }

    @Override
    public String toString() {
        return "CustomerCallCenterAlgolia{" +
                "users=" + users +
                '}';
    }
}
