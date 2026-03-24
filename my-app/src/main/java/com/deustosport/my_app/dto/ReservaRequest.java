package com.deustosport.my_app.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ReservaRequest {
    private Long usuarioId;
    private Long pistaId;

    @Schema(type = "string", example = "2026-03-25", description = "Fecha de la reserva (yyyy-MM-dd)")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fecha;

    @Schema(type = "string", example = "10:00", description = "Hora de inicio (HH:mm)")
    @JsonFormat(pattern = "HH:mm") 
    private LocalTime horaInicio;

    @Schema(example = "60", description = "Duración en minutos (por defecto 60)")
    private Integer duracionMinutos;
}