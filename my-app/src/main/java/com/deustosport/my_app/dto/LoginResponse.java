package com.deustosport.my_app.dto;

import java.math.BigDecimal;

public class LoginResponse {
    private Long usuarioId;
    private String nombreCompleto;
    private String email;
    private String rol;
    private boolean esSocio;
    private BigDecimal billetera;
    private Long ayuntamientoId;
    private Long polideportivoId;
    private String mensaje;
    private boolean exitoso;

    public LoginResponse() {
    }

    // Constructor actualizado
    public LoginResponse(Long usuarioId, String nombreCompleto, String email, String rol,
                         boolean esSocio, BigDecimal billetera, String mensaje, boolean exitoso) {
        this.usuarioId = usuarioId;
        this.nombreCompleto = nombreCompleto;
        this.email = email;
        this.rol = rol;
        this.esSocio = esSocio;
        this.billetera = billetera;
        this.mensaje = mensaje;
        this.exitoso = exitoso;
    }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public boolean isEsSocio() { return esSocio; }
    public void setEsSocio(boolean esSocio) { this.esSocio = esSocio; }

    public BigDecimal getBilletera() { return billetera; }
    public void setBilletera(BigDecimal billetera) { this.billetera = billetera; }

    public Long getAyuntamientoId() { return ayuntamientoId; }
    public void setAyuntamientoId(Long ayuntamientoId) { this.ayuntamientoId = ayuntamientoId; }

    public Long getPolideportivoId() { return polideportivoId; }
    public void setPolideportivoId(Long polideportivoId) { this.polideportivoId = polideportivoId; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public boolean isExitoso() { return exitoso; }
    public void setExitoso(boolean exitoso) { this.exitoso = exitoso; }
}   