package com.deustosport.my_app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para solicitud de eliminación de cuenta de usuario.
 * Requiere confirmación con contraseña para mayor seguridad.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudEliminacionCuentaRequest {

    @NotBlank(message = "La contraseña es requerida para eliminar la cuenta")
    private String contrasena;

    @NotNull(message = "Debe confirmar la eliminación")
    private Boolean confirmacion;

    @NotBlank(message = "El motivo de cancelación es requerido")
    private String motivo;

    /**
     * Motivos predefinidos para la eliminación
     */
    public enum MotivoEliminacion {
        NO_SATISFECHOS("No satisfechos con el servicio"),
        PRIVACIDAD("Preocupación por privacidad"),
        NO_UTILIZAMOS("No utilizamos el servicio"),
        MIGRACION("Migración a otra plataforma"),
        OTRO("Otro motivo");

        private final String descripcion;

        MotivoEliminacion(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }
}
