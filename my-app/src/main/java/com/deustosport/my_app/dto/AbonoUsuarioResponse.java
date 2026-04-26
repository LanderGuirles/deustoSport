package com.deustosport.my_app.dto;

import com.deustosport.my_app.enums.DuracionAbonos;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AbonoUsuarioResponse {
    private Long id;
    private String nombrePlan;
    private DuracionAbonos duracion;
    private BigDecimal precioPagado;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private boolean activo;
}