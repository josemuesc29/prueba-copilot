package com.imaginamos.farmatodo.networking.models.addresses;

public class SendWhatsappCloudFunctionCodeReq {

    private String phone;
    private String name;
    private String code;
    private String templateName;
    private String templateNamespace;

    public SendWhatsappCloudFunctionCodeReq() {
    }

    public SendWhatsappCloudFunctionCodeReq(String phone, String name, String code, String templateName, String templateNamespace) {
        this.phone = phone;
        this.name = name;
        this.code = code;
        this.templateName = templateName;
        this.templateNamespace = templateNamespace;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateNamespace() {
        return templateNamespace;
    }

    public void setTemplateNamespace(String templateNamespace) {
        this.templateNamespace = templateNamespace;
    }

    @Override
    public String toString() {
        return "SendWhatsappCloudFunctionCodeReq{" +
                "phone='" + phone + '\'' +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", templateName='" + templateName + '\'' +
                ", templateNamespace='" + templateNamespace + '\'' +
                '}';
    }
}
