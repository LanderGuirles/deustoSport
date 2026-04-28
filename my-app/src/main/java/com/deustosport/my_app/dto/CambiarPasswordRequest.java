package com.deustosport.my_app.dto;

import jakarta.validation.constraints.*;

public class CambiarPasswordRequest {
    @NotBlank(message = "Contraseña actual es obligatoria")
    private String oldPassword;

    @NotBlank(message = "Nueva contraseña es obligatoria")
    @Size(min = 8, message = "Nueva contraseña debe tener al menos 8 caracteres")
    private String newPassword;

    // Getters and setters
    public String getOldPassword() { return oldPassword; }
    public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
