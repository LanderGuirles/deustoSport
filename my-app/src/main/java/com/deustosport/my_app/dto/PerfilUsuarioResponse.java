package com.deustosport.my_app.dto;

public class PerfilUsuarioResponse {

    private Long usuarioId;
    private String nombreCompleto;
    private String email;
    private String telefono;
    private String rol;
    private boolean esSocio;
    private String mensaje;
    private boolean exitoso;

    public PerfilUsuarioResponse() {
    }

    public PerfilUsuarioResponse(Long usuarioId, String nombreCompleto, String email, String telefono,
            String rol, boolean esSocio, String mensaje, boolean exitoso) {
        this.usuarioId = usuarioId;
        this.nombreCompleto = nombreCompleto;
        this.email = email;
        this.telefono = telefono;
        this.rol = rol;
        this.esSocio = esSocio;
        this.mensaje = mensaje;
        this.exitoso = exitoso;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
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

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public boolean isEsSocio() {
        return esSocio;
    }

    public void setEsSocio(boolean esSocio) {
        this.esSocio = esSocio;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public boolean isExitoso() {
        return exitoso;
    }

    public void setExitoso(boolean exitoso) {
        this.exitoso = exitoso;
    }
}