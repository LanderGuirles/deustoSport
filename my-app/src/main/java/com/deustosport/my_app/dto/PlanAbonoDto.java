package com.deustosport.my_app.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PlanAbonoDto {
    private Long id;
    private String nombre;
    private BigDecimal descuentoPistasPorcentaje;
}