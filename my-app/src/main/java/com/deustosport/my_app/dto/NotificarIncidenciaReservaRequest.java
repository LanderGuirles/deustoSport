package com.deustosport.my_app.dto;

import lombok.Data;

@Data
public class NotificarIncidenciaReservaRequest {
    private String titulo;
    private String mensaje;
    private boolean enviarEmail;
}
