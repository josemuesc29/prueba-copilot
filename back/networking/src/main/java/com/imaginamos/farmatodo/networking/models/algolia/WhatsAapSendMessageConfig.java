package com.imaginamos.farmatodo.networking.models.algolia;

public class WhatsAapSendMessageConfig {

    private String templateNameCode;
    private String templateNameLogin;
    private String templateNamespace;

    public WhatsAapSendMessageConfig() {
    }


    public WhatsAapSendMessageConfig(String templateNameCode, String templateNameLogin, String templateNamespace, String countryNumberCode) {
        this.templateNameCode = templateNameCode;
        this.templateNameLogin = templateNameLogin;
        this.templateNamespace = templateNamespace;
    }

    public String getTemplateNameCode() {
        return templateNameCode;
    }

    public void setTemplateNameCode(String templateNameCode) {
        this.templateNameCode = templateNameCode;
    }

    public String getTemplateNameLogin() {
        return templateNameLogin;
    }

    public void setTemplateNameLogin(String templateNameLogin) {
        this.templateNameLogin = templateNameLogin;
    }

    public String getTemplateNamespace() {
        return templateNamespace;
    }

    public void setTemplateNamespace(String templateNamespace) {
        this.templateNamespace = templateNamespace;
    }

    @Override
    public String toString() {
        return "WhatsAapSendMessageConfig{" +
                "templateNameCode='" + templateNameCode + '\'' +
                ", templateNameLogin='" + templateNameLogin + '\'' +
                ", templateNamespace='" + templateNamespace + '\'' +
                '}';
    }
}
