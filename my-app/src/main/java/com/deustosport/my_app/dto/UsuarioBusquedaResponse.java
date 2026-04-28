package com.deustosport.my_app.dto;

public class UsuarioBusquedaResponse {
    private Long id;
    private String dni;
    private String nombre;
    private String apellidos;
    private String email;
    private String telefono;
    private boolean esSocio;
    private boolean activo;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public boolean isEsSocio() { return esSocio; }
    public void setEsSocio(boolean esSocio) { this.esSocio = esSocio; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
