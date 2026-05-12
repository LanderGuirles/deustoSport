package com.deustosport.my_app.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que envuelve múltiples reportes de uso de pistas para un período
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteUsoPistasDTO {
    
    // Información del período del reporte
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String periodDescription; // "Semana del X al Y", "Mes de X", etc.
    
    // Resumen consolidado
    private Long totalReservas;
    private Long totalReservasConfirmadas;
    private Long totalReservasCompletadas;
    private Long totalReservasCanceladas;
    
    // Métricas consolidadas
    private BigDecimal ingresoTotalConsolidado;
    private Double tasaOcupacionPromedio; // Porcentaje
    private Double tasaCancelacionPromedio; // Porcentaje
    
    // Resumen por pista
    private List<ReporteUsoPistaDTO> reportesPorPista;
    
    // Información de máquina de reportes
    private LocalDate fechaGeneracion;
    private String generadoPor; // "Sistema automático" o "Coordinador X"
}
