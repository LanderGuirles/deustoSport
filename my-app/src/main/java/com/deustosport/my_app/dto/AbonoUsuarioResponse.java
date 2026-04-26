package com.deustosport.my_app.dto;

import com.deustosport.my_app.enums.DuracionAbonos;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class AbonoUsuarioResponse {
    private Long id;
    private String nombrePlan;
    private String descripcion;
    private DuracionAbonos duracion;
    private BigDecimal precioPagado;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private boolean activo;
    private String titularEmail;
    private List<String> emailsBeneficiarios;
}