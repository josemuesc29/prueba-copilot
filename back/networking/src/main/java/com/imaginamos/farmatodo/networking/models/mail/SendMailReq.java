package com.imaginamos.farmatodo.networking.models.mail;

public class SendMailReq {
    private String to;
    private String subject;
    private String text;

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isValid() {
        return to != null && !to.isEmpty()
                && subject != null && !subject.isEmpty()
                && text != null && !text.isEmpty();
    }


}
