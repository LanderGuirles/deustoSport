package com.deustosport.my_app.dto;

public class CambioPasswordRequest {
    private String emailOToken;
    private String passwordNueva;

    public CambioPasswordRequest() {
    }

    public CambioPasswordRequest(String emailOToken, String passwordNueva) {
        this.emailOToken = emailOToken;
        this.passwordNueva = passwordNueva;
    }

    public String getEmailOToken() {
        return emailOToken;
    }

    public void setEmailOToken(String emailOToken) {
        this.emailOToken = emailOToken;
    }

    public String getPasswordNueva() {
        return passwordNueva;
    }

    public void setPasswordNueva(String passwordNueva) {
        this.passwordNueva = passwordNueva;
    }
}
