package com.imaginamos.farmatodo.model.algolia;


import com.imaginamos.farmatodo.model.categories.Department;

import java.util.List;

public class DepartmentsAlgolia {

    private List<Department> urlDepartments;

    public List<Department> getUrlDepartments() {
        return urlDepartments;
    }

    public void setUrlDepartments(List<Department> urlDepartments) {
        this.urlDepartments = urlDepartments;
    }
}
