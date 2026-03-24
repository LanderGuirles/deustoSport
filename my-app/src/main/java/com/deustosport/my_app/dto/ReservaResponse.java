package com.deustosport.my_app.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import com.deustosport.my_app.enums.EstadoReserva;
import lombok.Data;

@Data
public class ReservaResponse {
    private Long id;
    private Long usuarioId;
    private Long pistaId;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private BigDecimal precioTotal;
    private EstadoReserva estado;
}
