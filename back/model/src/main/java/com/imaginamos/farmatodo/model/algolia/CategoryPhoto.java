package com.imaginamos.farmatodo.model.algolia;

public class CategoryPhoto {
    private String idCategoryPhoto;
    private Long idDepartment;
    private Integer imagePosition;
    private String imageUrl;
    private Boolean redirect;
    private String redirectUrl;
    private String tokenIdWebSafe;
    private String token;
    private String idPhotoWebSafe;

    public CategoryPhoto() {}

    public CategoryPhoto(String idCategoryPhoto, Long idDepartment, Integer imagePosition, String imageUrl, Boolean redirect, String redirectUrl, String tokenIdWebSafe, String token, String idPhotoWebSafe) {
        this.idCategoryPhoto = idCategoryPhoto;
        this.idDepartment = idDepartment;
        this.imagePosition = imagePosition;
        this.imageUrl = imageUrl;
        this.redirect = redirect;
        this.redirectUrl = redirectUrl;
        this.tokenIdWebSafe = tokenIdWebSafe;
        this.token = token;
        this.idPhotoWebSafe = idPhotoWebSafe;
    }

    public String getIdCategoryPhoto() {
        return idCategoryPhoto;
    }

    public void setIdCategoryPhoto(String idCategoryPhoto) {
        this.idCategoryPhoto = idCategoryPhoto;
    }

    public Long getIdDepartment() {
        return idDepartment;
    }

    public void setIdDepartment(Long idDepartment) {
        this.idDepartment = idDepartment;
    }

    public Integer getImagePosition() {
        return imagePosition;
    }

    public void setImagePosition(Integer imagePosition) {
        this.imagePosition = imagePosition;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Boolean getRedirect() {
        return redirect;
    }

    public void setRedirect(Boolean redirect) {
        this.redirect = redirect;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public String getTokenIdWebSafe() {
        return tokenIdWebSafe;
    }

    public void setTokenIdWebSafe(String tokenIdWebSafe) {
        this.tokenIdWebSafe = tokenIdWebSafe;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getIdPhotoWebSafe() {
        return idPhotoWebSafe;
    }

    public void setIdPhotoWebSafe(String idPhotoWebSafe) {
        this.idPhotoWebSafe = idPhotoWebSafe;
    }

    @Override
    public String toString() {
        return "CategoryPhoto{" +
                "idCategoryPhoto='" + idCategoryPhoto + '\'' +
                ", idDepartment=" + idDepartment +
                ", imagePosition=" + imagePosition +
                ", imageUrl='" + imageUrl + '\'' +
                ", redirect=" + redirect +
                ", redirectUrl='" + redirectUrl + '\'' +
                ", tokenIdWebSafe='" + tokenIdWebSafe + '\'' +
                ", token='" + token + '\'' +
                ", idPhotoWebSafe='" + idPhotoWebSafe + '\'' +
                '}';
    }
}
