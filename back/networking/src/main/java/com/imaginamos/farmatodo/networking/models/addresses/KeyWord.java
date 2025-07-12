package com.imaginamos.farmatodo.networking.models.addresses;

public class KeyWord {
    private String key;
    private String value;

    public KeyWord(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public KeyWord() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "keyWord{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
