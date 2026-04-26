package com.deustosport.my_app.dto;

import com.deustosport.my_app.enums.DuracionAbonos;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PlanAbonoResponse {
    private Long planAbonoId;
    private String nombrePlan;
    private String descripcion;
    private DuracionAbonos duracion;
    private Integer cantidadPersonas;
    private Integer edadMinima;
    private Integer edadMax;
    private BigDecimal precio;
    private BigDecimal descuentoPistasPorcentaje;
    private boolean activo;
}