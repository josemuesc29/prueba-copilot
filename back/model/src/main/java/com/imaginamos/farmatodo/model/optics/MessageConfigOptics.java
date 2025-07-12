package com.imaginamos.farmatodo.model.optics;

public class MessageConfigOptics {

    private String firstMessage;

    private String firstMessageWithoutStock;

    private String secondMessage;
    private String notExistsItemSameEyes;
    private String notExistsItemRightEye;
    private String notExistsItemLeftEye;

    private String powerParameter;

    private String cylinderParameter;

    private String axleParameter;

    private String additionParameter;

    private String lensColorParameter;

    public MessageConfigOptics() {
    }

    public MessageConfigOptics(String firstMessage, String firstMessageWithoutStock, String secondMessage, String notExistsItemSameEyes, String notExistsItemRightEye, String notExistsItemLeftEye, String powerParameter, String cylinderParameter, String axleParameter, String additionParameter, String lensColorParameter) {
        this.firstMessage = firstMessage;
        this.firstMessageWithoutStock = firstMessageWithoutStock;
        this.secondMessage = secondMessage;
        this.notExistsItemSameEyes = notExistsItemSameEyes;
        this.notExistsItemRightEye = notExistsItemRightEye;
        this.notExistsItemLeftEye = notExistsItemLeftEye;
        this.powerParameter = powerParameter;
        this.cylinderParameter = cylinderParameter;
        this.axleParameter = axleParameter;
        this.additionParameter = additionParameter;
        this.lensColorParameter = lensColorParameter;
    }

    public String getFirstMessageWithoutStock() {
        return firstMessageWithoutStock;
    }

    public void setFirstMessageWithoutStock(String firstMessageWithoutStock) {
        if (firstMessageWithoutStock != null) {
            this.firstMessageWithoutStock = firstMessageWithoutStock;
        }
    }

    public String getFirstMessage() {
        return firstMessage;
    }

    public void setFirstMessage(String firstMessage) {
        if (firstMessage != null) {
            this.firstMessage = firstMessage;
        }
    }

    public String getSecondMessage() {
        return secondMessage;
    }

    public void setSecondMessage(String secondMessage) {
        if (secondMessage != null) {
            this.secondMessage = secondMessage;
        }
    }

    public String getNotExistsItemSameEyes() {
        return notExistsItemSameEyes;
    }

    public void setNotExistsItemSameEyes(String notExistsItemSameEyes) {
        if (notExistsItemSameEyes != null) {
            this.notExistsItemSameEyes = notExistsItemSameEyes;
        }
    }

    public String getNotExistsItemRightEye() {
        return notExistsItemRightEye;
    }

    public void setNotExistsItemRightEye(String notExistsItemRightEye) {
        if (notExistsItemRightEye != null) {
            this.notExistsItemRightEye = notExistsItemRightEye;
        }
    }

    public String getNotExistsItemLeftEye() {
        return notExistsItemLeftEye;
    }

    public void setNotExistsItemLeftEye(String notExistsItemLeftEye) {
        if (notExistsItemLeftEye != null) {
            this.notExistsItemLeftEye = notExistsItemLeftEye;
        }
    }

    public String getPowerParameter() {
        return powerParameter;
    }

    public void setPowerParameter(String powerParameter) {
        if (powerParameter != null) {
            this.powerParameter = powerParameter;
        }
    }

    public String getCylinderParameter() {
        return cylinderParameter;
    }

    public void setCylinderParameter(String cylinderParameter) {
        if (cylinderParameter != null) {
            this.cylinderParameter = cylinderParameter;
        }
    }

    public String getAxleParameter() {
        return axleParameter;
    }

    public void setAxleParameter(String axleParameter) {
        if (axleParameter != null) {
            this.axleParameter = axleParameter;
        }
    }

    public String getAdditionParameter() {
        return additionParameter;
    }

    public void setAdditionParameter(String additionParameter) {
        if (additionParameter != null) {
            this.additionParameter = additionParameter;
        }
    }

    public String getLensColorParameter() {
        return lensColorParameter;
    }

    public void setLensColorParameter(String lensColorParameter) {
        if (lensColorParameter != null) {
            this.lensColorParameter = lensColorParameter;
        }
    }
}
