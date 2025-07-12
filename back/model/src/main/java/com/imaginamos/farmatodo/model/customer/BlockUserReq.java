package com.imaginamos.farmatodo.model.customer;

public class BlockUserReq {
    private int idUser;
    private String reasonBlock;

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public String getReasonBlock() {
        return reasonBlock;
    }

    public void setReasonBlock(String reasonBlock) {
        this.reasonBlock = reasonBlock;
    }

    public boolean isValid(){
        return idUser != 0 && reasonBlock != null && !reasonBlock.isEmpty() ;
    }
}
