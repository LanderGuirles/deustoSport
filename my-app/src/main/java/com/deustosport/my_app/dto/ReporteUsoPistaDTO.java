package com.deustosport.my_app.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para reportes de uso y rentabilidad de una pista
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteUsoPistaDTO {
    
    // Información básica de la pista
    private Long pistaId;
    private String pistaNombre;
    private String tipoDeporte;
    
    // Rango de fechas del reporte
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    
    // Métricas de uso
    private Long totalReservas;
    private Long reservasConfirmadas;
    private Long reservasCompletadas;
    private Long reservasCanceladas;
    private Long reservasPendientes;
    
    // Métricas de disponibilidad
    private Long horasDisponibles;
    private Long horasReservadas;
    private Double tasaOcupacion; // Porcentaje 0-100
    
    // Métricas financieras
    private BigDecimal ingresoTotal;
    private BigDecimal ingresoPromedioPorReserva;
    private BigDecimal ingresoPromedioPorHora;
    
    // Tasas y ratios
    private Double tasaCancelacion; // Porcentaje 0-100
    private Double tasaCompletacion; // Porcentaje 0-100
    
    // Información adicional
    private Integer maxJugadores;
    private String estado; // "Disponible" o "Bloqueada"
}
