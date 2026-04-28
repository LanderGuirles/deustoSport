package com.deustosport.my_app.dto;

import java.math.BigDecimal;

public class RegistroUsuarioResponse {
    private Long id;
    private String nombre;
    private String apellidos;
    private String email;
    private boolean esSocio;
    private String mensaje;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isEsSocio() { return esSocio; }
    public void setEsSocio(boolean esSocio) { this.esSocio = esSocio; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}