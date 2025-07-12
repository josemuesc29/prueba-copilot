/*
 * Farmatodo Colombia.
 * Copyright 2019.
 */
package com.imaginamos.farmatodo.model.customer;

import java.io.Serializable;

/**
 * @author <a href="mailto:jorge.garcia@farmatodo.com">Jorge A. Garcia E.</a>
 * @version 2.0 2019/01/02
 * @since 1.8
 */
public class CustomerPrevalidateDeleteBackend30 implements Serializable {

    private static final long serialVersionUID = -615873659510920958L;

    private String code;
    private String message;
    private Boolean data;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getData() {
        return data;
    }

    public void setData(Boolean data) {
        this.data = data;
    }
}
