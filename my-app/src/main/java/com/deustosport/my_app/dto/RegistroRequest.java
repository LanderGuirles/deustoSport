package com.deustosport.my_app.dto;

public class RegistroRequest {
    private String nombreCompleto;
    private String email;
    private String dni;
    private String password;
    private String telefono;

    public RegistroRequest() {
    }

    public RegistroRequest(String nombreCompleto, String email, String dni, String password, String telefono) {
        this.nombreCompleto = nombreCompleto;
        this.email = email;
        this.dni = dni;
        this.password = password;
        this.telefono = telefono;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
}
