package com.deustosport.my_app.dto;

public class SecretariaUsuarioResumenDto {

    private Long id;
    private String nombreCompleto;
    private String dni;
    private String email;
    private String telefono;
    private boolean activo;
    private long totalReservas;

    public SecretariaUsuarioResumenDto() {
    }

    public SecretariaUsuarioResumenDto(Long id, String nombreCompleto, String dni, String email,
            String telefono, boolean activo, long totalReservas) {
        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.dni = dni;
        this.email = email;
        this.telefono = telefono;
        this.activo = activo;
        this.totalReservas = totalReservas;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
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

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public long getTotalReservas() {
        return totalReservas;
    }

    public void setTotalReservas(long totalReservas) {
        this.totalReservas = totalReservas;
    }
}
