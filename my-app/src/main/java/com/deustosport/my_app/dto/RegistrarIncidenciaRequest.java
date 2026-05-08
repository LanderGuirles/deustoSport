package com.deustosport.my_app.dto;

import lombok.Data;

@Data
public class RegistrarIncidenciaRequest {
    private Long usuarioId;
    private Long instalacionId;
    private Long pistaId;
    private String titulo;
    private String descripcion;
    private String ubicacionEspecifica;
}