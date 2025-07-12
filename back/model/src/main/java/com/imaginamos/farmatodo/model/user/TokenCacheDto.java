package com.imaginamos.farmatodo.model.user;

import java.util.Date;

public class TokenCacheDto {
    private String tokenId;
    private String token;
    private Date tokenExp;
    private String refreshToken;
    private String ownerUserId; // Solo el ID del usuario

    public static TokenCacheDto fromToken(Token token) {
        TokenCacheDto dto = new TokenCacheDto();
        dto.tokenId = token.getTokenId();
        dto.token = token.getToken();
        dto.tokenExp = token.getTokenExp();
        dto.refreshToken = token.getRefreshToken();

        if (token.getOwner() != null) {
            dto.ownerUserId = token.getOwner().getKey().getName();
        }
        return dto;
    }

    public Token toToken() {
        Token token = new Token();
        token.setTokenId(this.tokenId);
        token.setToken(this.token);
        token.setTokenExpDate(this.tokenExp);
        token.setRefreshToken(this.refreshToken);
        // owner se quedará null - se resuelve cuando sea necesario
        return token;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getTokenExp() {
        return tokenExp;
    }

    public void setTokenExp(Date tokenExp) {
        this.tokenExp = tokenExp;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(String ownerUserId) {
        this.ownerUserId = ownerUserId;
    }
}
