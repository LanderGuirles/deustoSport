package com.deustosport.my_app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * DTO para respuesta de eliminación de cuenta.
 * Contiene información sobre el resultado del proceso de eliminación.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EliminacionCuentaResponse {

    private boolean exitoso;
    private String mensaje;
    private LocalDateTime fechaEliminacion;
    private String usuarioEmail;
    private String motivo;
    private String detalles;

    /**
     * Constructor para respuestas exitosas
     */
    public EliminacionCuentaResponse(boolean exitoso, String mensaje, LocalDateTime fechaEliminacion, String usuarioEmail) {
        this.exitoso = exitoso;
        this.mensaje = mensaje;
        this.fechaEliminacion = fechaEliminacion;
        this.usuarioEmail = usuarioEmail;
    }

    /**
     * Constructor para respuestas con error
     */
    public EliminacionCuentaResponse(boolean exitoso, String mensaje, String detalles) {
        this.exitoso = exitoso;
        this.mensaje = mensaje;
        this.detalles = detalles;
    }
}
