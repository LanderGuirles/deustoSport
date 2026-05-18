package com.deustosport.my_app.dto;

import jakarta.validation.constraints.*;

public class ActualizarUsuarioPoliRequest {
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

    private String rol;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}