package com.deustosport.my_app.dto;

import com.deustosport.my_app.enums.EstadoIncidencia;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class IncidenciaResponse {
    private Long id;
    private Long reportadaPorId;
    private String reportadaPorNombre;
    private Long polideportivoId;
    private String polideportivoNombre;
    private Long pistaId;
    private String pistaNombre;
    private String titulo;
    private String descripcion;
    private String ubicacionEspecifica;
    private EstadoIncidencia estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}