package com.deustosport.my_app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ActualizarPerfilRequest {

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(min = 2, max = 120, message = "El nombre completo debe tener entre 2 y 120 caracteres")
    private String nombreCompleto;

    @Pattern(regexp = "^$|^\\+?[0-9]{9,15}$", message = "El telefono debe contener entre 9 y 15 digitos")
    private String telefono;

    public ActualizarPerfilRequest() {
    }

    public ActualizarPerfilRequest(String nombreCompleto, String telefono) {
        this.nombreCompleto = nombreCompleto;
        this.telefono = telefono;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
}