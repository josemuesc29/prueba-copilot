package com.imaginamos.farmatodo.model.product;

import java.util.List;

public class ItemFilter {

    private Integer id;
    private String name;
    private String value;
    private Boolean handleQuantity;
    private List<Integer> dependencies;

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getValue() { return value; }

    public void setValue(String value) { this.value = value; }

    public Boolean getHandleQuantity() { return handleQuantity; }

    public void setHandleQuantity(Boolean handleQuantity) { this.handleQuantity = handleQuantity; }

    public Integer getId() { return id; }

    public void setId(Integer id) { this.id = id; }

    public List<Integer> getDependencies() { return dependencies; }

    public void setDependencies(List<Integer> dependencies) { this.dependencies = dependencies; }

    @Override
    public String toString() {
        return "ItemFilter{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", handleQuantity='" + handleQuantity + '\'' +
                '}';
    }
}
