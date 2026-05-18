package com.deustosport.my_app.dto;

import jakarta.validation.constraints.*;

public class RegistroUsuarioRequest {
    @NotBlank(message = "DNI es obligatorio")
    @Pattern(regexp = "\\d{8}[A-Za-z]", message = "DNI debe tener 8 dígitos y una letra")
    private String dni;

    @NotBlank(message = "Nombre es obligatorio")
    @Size(min = 2, max = 50, message = "Nombre debe tener entre 2 y 50 caracteres")
    private String nombre;

    @NotBlank(message = "Apellidos es obligatorio")
    @Size(min = 2, max = 100, message = "Apellidos debe tener entre 2 y 100 caracteres")
    private String apellidos;

    @NotBlank(message = "Email es obligatorio")
    @Email(message = "Email debe tener un formato válido")
    private String email;

    @NotBlank(message = "Teléfono es obligatorio")
    @Pattern(regexp = "^(\\+34)?[6789]\\d{8}$", message = "Teléfono español válido requerido")
    private String telefono;

    @NotBlank(message = "Contraseña es obligatoria")
    @Size(min = 8, message = "Contraseña debe tener al menos 8 caracteres")
    private String password;

    private boolean esSocio;

    private String rol; // CLIENTE, SECRETARIA, COORDINADOR, MANTENIMIENTO, AYUNTAMIENTO

    private Long polideportivoId;

    private Long ayuntamientoId;

    // Getters and setters
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

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean isEsSocio() { return esSocio; }
    public void setEsSocio(boolean esSocio) { this.esSocio = esSocio; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public Long getPolideportivoId() { return polideportivoId; }
    public void setPolideportivoId(Long polideportivoId) { this.polideportivoId = polideportivoId; }

    public Long getAyuntamientoId() { return ayuntamientoId; }
    public void setAyuntamientoId(Long ayuntamientoId) { this.ayuntamientoId = ayuntamientoId; }
}