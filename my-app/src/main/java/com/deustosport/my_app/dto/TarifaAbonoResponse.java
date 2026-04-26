package com.deustosport.my_app.dto;

import com.deustosport.my_app.enums.DuracionAbonos;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class TarifaAbonoResponse {
    private Long id;
    private Long planAbonoId;
    private String nombrePlan;
    private DuracionAbonos duracion;
    private Integer edadMinima;
    private Integer edadMax;
    private BigDecimal precio;
}