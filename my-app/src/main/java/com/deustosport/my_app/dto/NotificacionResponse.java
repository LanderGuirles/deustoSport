package com.deustosport.my_app.dto;

import com.deustosport.my_app.enums.TipoNotificacion;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class NotificacionResponse {
    private Long id;
    private Long usuarioId;
    private String titulo;
    private String mensaje;
    private TipoNotificacion tipo;
    private boolean leida;
    private LocalDateTime fechaCreacion;
    private Long reservaId;
}
