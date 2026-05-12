package com.deustosport.my_app.service;

import com.deustosport.my_app.repository.ReservaRepository;
import com.deustosport.my_app.repository.UsuarioRepository;
import com.deustosport.my_app.repository.PistaRepository;
import com.deustosport.my_app.dto.ReporteUsoPistaDTO;
import com.deustosport.my_app.dto.ReporteUsoPistasDTO;
import com.deustosport.my_app.entity.Reserva;
import com.deustosport.my_app.entity.Pista;
import com.deustosport.my_app.enums.EstadoReserva;
import com.deustosport.my_app.enums.MetodoPago;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EstadisticasService {

    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PistaRepository pistaRepository;

    public EstadisticasService(ReservaRepository reservaRepository,
                               UsuarioRepository usuarioRepository,
                               PistaRepository pistaRepository) {
        this.reservaRepository = reservaRepository;
        this.usuarioRepository = usuarioRepository;
        this.pistaRepository = pistaRepository;
    }

    /**
     * Obtiene estadísticas generales del sistema
     */
    public Map<String, Object> obtenerEstadisticasGenerales() {
        Map<String, Object> stats = new HashMap<>();

        // Total de usuarios
        long totalUsuarios = usuarioRepository.count();
        stats.put("totalUsuarios", totalUsuarios);

        // Usuarios activos
        long usuariosActivos = usuarioRepository.countByActivoTrue();
        stats.put("usuariosActivos", usuariosActivos);

        // Usuarios socios
        long usuariosSocios = usuarioRepository.countByEsSocioTrue();
        stats.put("usuariosSocios", usuariosSocios);

        // Total de reservas
        long totalReservas = reservaRepository.count();
        stats.put("totalReservas", totalReservas);

        // Reservas confirmadas
        long reservasConfirmadas = reservaRepository.countByEstado(EstadoReserva.CONFIRMADA);
        stats.put("reservasConfirmadas", reservasConfirmadas);

        // Reservas pendientes
        long reservasPendientes = reservaRepository.countByEstado(EstadoReserva.PENDIENTE);
        stats.put("reservasPendientes", reservasPendientes);

        // Reservas canceladas
        long reservasCanceladas = reservaRepository.countByEstado(EstadoReserva.CANCELADA);
        stats.put("reservasCanceladas", reservasCanceladas);

        // Ingresos totales
        BigDecimal ingresosTotales = reservaRepository.sumPrecioTotalByEstado(EstadoReserva.CONFIRMADA);
        stats.put("ingresosTotales", ingresosTotales != null ? ingresosTotales : BigDecimal.ZERO);

        // Ingresos del mes actual
        LocalDateTime inicioMes = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime finMes = LocalDate.now().plusMonths(1).withDayOfMonth(1).atStartOfDay();
        BigDecimal ingresosMesActual = reservaRepository.sumPrecioTotalByFechaPagoBetween(inicioMes, finMes);
        stats.put("ingresosMesActual", ingresosMesActual != null ? ingresosMesActual : BigDecimal.ZERO);

        return stats;
    }

    /**
     * Estadísticas de reservas por mes
     */
    public Map<String, Object> obtenerEstadisticasReservasPorMes(int year) {
        Map<String, Object> stats = new HashMap<>();

        for (int month = 1; month <= 12; month++) {
            LocalDateTime inicioMes = LocalDate.of(year, month, 1).atStartOfDay();
            LocalDateTime finMes = inicioMes.plusMonths(1);

            long reservasMes = reservaRepository.countByFechaReservaBetween(inicioMes.toLocalDate(), finMes.toLocalDate().minusDays(1));
            BigDecimal ingresosMes = reservaRepository.sumPrecioTotalByFechaPagoBetween(inicioMes, finMes);

            stats.put("mes" + month + "_reservas", reservasMes);
            stats.put("mes" + month + "_ingresos", ingresosMes != null ? ingresosMes : BigDecimal.ZERO);
        }

        return stats;
    }

    /**
     * Estadísticas de uso de pistas
     */
    public Map<String, Object> obtenerEstadisticasUsoPistas() {
        Map<String, Object> stats = new HashMap<>();

        // Aquí irían consultas para estadísticas de pistas
        // Por simplicidad, devolvemos un mapa vacío por ahora
        stats.put("mensaje", "Estadísticas de uso de pistas - Implementación pendiente");

        return stats;
    }

    /**
     * Top usuarios por número de reservas
     */
    public Map<String, Object> obtenerTopUsuariosReservas(int limit) {
        Map<String, Object> stats = new HashMap<>();

        // Implementación simplificada
        stats.put("mensaje", "Top usuarios por reservas - Implementación pendiente");
        stats.put("limit", limit);

        return stats;
    }

    /**
     * Estadísticas de pagos por método
     */
    public Map<String, Object> obtenerEstadisticasPagos() {
        Map<String, Object> stats = new HashMap<>();

        // Conteo por método de pago
        long pagosTarjeta = reservaRepository.countByMetodoPago(MetodoPago.TARJETA);
        long pagosBizum = reservaRepository.countByMetodoPago(MetodoPago.BIZUM);
        long pagosTransferencia = reservaRepository.countByMetodoPago(MetodoPago.TRANSFERENCIA);
        long pagosBilletera = reservaRepository.countByMetodoPago(MetodoPago.BILLETERA);

        stats.put("pagosTarjeta", pagosTarjeta);
        stats.put("pagosBizum", pagosBizum);
        stats.put("pagosTransferencia", pagosTransferencia);
        stats.put("pagosBilletera", pagosBilletera);

        return stats;
    }

    // Compatibility method expected by tests
    public com.deustosport.my_app.dto.EstadisticasDTO obtenerEstadisticas() {
        com.deustosport.my_app.dto.EstadisticasDTO dto = new com.deustosport.my_app.dto.EstadisticasDTO();

        long confirmadas = reservaRepository.countByEstado("CONFIRMADA");
        long pendientes = reservaRepository.countByEstado("PENDIENTE");
        long canceladas = reservaRepository.countByEstado("CANCELADA");

        LocalDateTime inicioMes = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime finMes = LocalDate.now().plusMonths(1).withDayOfMonth(1).atStartOfDay();

        long reservasMes = reservaRepository.countReservasMesActual(inicioMes, finMes);
        java.math.BigDecimal ingresosMes = reservaRepository.sumIngresosMesActual(inicioMes, finMes);

        long reservasHoy = reservaRepository.countReservasHoy(LocalDate.now());
        java.math.BigDecimal ingresosHoy = reservaRepository.sumIngresosHoy(LocalDate.now());

        long socios = usuarioRepository.countByEsSocio(true);
        long noSocios = usuarioRepository.countByEsSocio(false);
        java.math.BigDecimal saldoBilleteras = usuarioRepository.sumBilleteraTotal();

        dto.setTotalReservasConfirmadas(confirmadas);
        dto.setTotalReservasPendientes(pendientes);
        dto.setTotalReservasCanceladas(canceladas);
        dto.setReservasMesActual(reservasMes);
        dto.setIngresosMesActual(ingresosMes != null ? ingresosMes : java.math.BigDecimal.ZERO);
        dto.setReservasHoy(reservasHoy);
        dto.setIngresosHoy(ingresosHoy != null ? ingresosHoy : java.math.BigDecimal.ZERO);
        dto.setTotalSocios(socios);
        dto.setTotalNoSocios(noSocios);
        dto.setSaldoTotalBilleteras(saldoBilleteras != null ? saldoBilleteras : java.math.BigDecimal.ZERO);

        return dto;
    }

    // Compatibility: return map of reservations per installation
    public java.util.Map<String, Long> obtenerEstadisticasReservasPorInstalacion() {
        try {
            return reservaRepository.countReservasPorInstalacion();
        } catch (Exception e) {
            return java.util.Collections.emptyMap();
        }
    }

    // Compatibility: return list of objects per day
    public java.util.List<Object[]> obtenerEstadisticasReservasPorDia(int dias) {
        // For tests we just delegate to repository with a small range
        LocalDate fin = LocalDate.now();
        LocalDate inicio = fin.minusDays(dias - 1);
        try {
            return reservaRepository.countReservasPorDia(inicio, fin);
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }

    /**
     * Estadísticas de ocupación por día de la semana
     */
    public Map<String, Object> obtenerEstadisticasOcupacionSemanal() {
        Map<String, Object> stats = new HashMap<>();

        String[] dias = {"LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES", "SABADO", "DOMINGO"};

        for (int i = 0; i < dias.length; i++) {
            // Implementación simplificada - contar reservas por día de semana
            long reservasDia = reservaRepository.countByDiaSemana(i + 1);
            stats.put("dia_" + dias[i].toLowerCase(), reservasDia);
        }

        return stats;
    }

    /**
     * Estadísticas de reservas por tipo de deporte
     */
    public Map<String, Object> obtenerEstadisticasPorDeporte() {
        Map<String, Object> stats = new HashMap<>();

        // Implementación simplificada
        stats.put("mensaje", "Estadísticas por deporte - Implementación pendiente");

        return stats;
    }

    /**
     * Reporte de ingresos por rango de fechas
     */
    public Map<String, Object> obtenerReporteIngresos(LocalDate fechaInicio, LocalDate fechaFin) {
        Map<String, Object> reporte = new HashMap<>();

        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(23, 59, 59);

        BigDecimal ingresosTotales = reservaRepository.sumPrecioTotalByFechaPagoBetween(inicio, fin);
        long numeroReservas = reservaRepository.countByFechaPagoBetween(inicio, fin);

        reporte.put("fechaInicio", fechaInicio);
        reporte.put("fechaFin", fechaFin);
        reporte.put("ingresosTotales", ingresosTotales != null ? ingresosTotales : BigDecimal.ZERO);
        reporte.put("numeroReservas", numeroReservas);

        if (numeroReservas > 0) {
            BigDecimal ingresoPromedio = ingresosTotales.divide(BigDecimal.valueOf(numeroReservas), 2, BigDecimal.ROUND_HALF_UP);
            reporte.put("ingresoPromedio", ingresoPromedio);
        } else {
            reporte.put("ingresoPromedio", BigDecimal.ZERO);
        }

        return reporte;
    }

    /**
     * Genera un reporte de uso y rentabilidad para una pista en un rango de fechas
     */
    public ReporteUsoPistaDTO generarReporteUsoPista(Long pistaId, LocalDate fechaInicio, LocalDate fechaFin) {
        Pista pista = pistaRepository.findById(pistaId).orElse(null);
        if (pista == null) {
            return null;
        }

        // Obtener todas las reservas para esta pista en el rango de fechas
        List<Reserva> reservas = reservaRepository.findActivasByPistaAndRango(pistaId, fechaInicio, fechaFin);

        ReporteUsoPistaDTO reporte = new ReporteUsoPistaDTO();
        reporte.setPistaId(pistaId);
        reporte.setPistaNombre(pista.getNombre());
        reporte.setTipoDeporte(pista.getTipoDeporte().name());
        reporte.setFechaInicio(fechaInicio);
        reporte.setFechaFin(fechaFin);
        reporte.setMaxJugadores(pista.getMaxJugadores());
        reporte.setEstado(pista.isActiva() ? "Disponible" : "Bloqueada");

        // Contar reservas por estado
        long totalReservas = reservas.size();
        long confirmadas = reservas.stream().filter(r -> r.getEstado() == EstadoReserva.CONFIRMADA).count();
        long completadas = reservas.stream().filter(r -> r.getEstado() == EstadoReserva.COMPLETADA).count();
        long canceladas = reservas.stream().filter(r -> r.getEstado() == EstadoReserva.CANCELADA).count();
        long pendientes = reservas.stream().filter(r -> r.getEstado() == EstadoReserva.PENDIENTE).count();

        reporte.setTotalReservas(totalReservas);
        reporte.setReservasConfirmadas(confirmadas);
        reporte.setReservasCompletadas(completadas);
        reporte.setReservasCanceladas(canceladas);
        reporte.setReservasPendientes(pendientes);

        // Calcular horas
        long diasEnRango = ChronoUnit.DAYS.between(fechaInicio, fechaFin) + 1;
        long horasDisponiblesEstimadas = diasEnRango * 12; // Asumir 12 horas de operación diaria (8am-8pm)
        
        long horasReservadas = 0;
        for (Reserva reserva : reservas) {
            if (reserva.getEstado() != EstadoReserva.CANCELADA) {
                long minutos = ChronoUnit.MINUTES.between(reserva.getHoraInicio(), reserva.getHoraFin());
                horasReservadas += minutos / 60;
            }
        }

        reporte.setHorasDisponibles(horasDisponiblesEstimadas);
        reporte.setHorasReservadas(horasReservadas);

        // Calcular tasa de ocupación
        if (horasDisponiblesEstimadas > 0) {
            double tasaOcupacion = (double) horasReservadas / horasDisponiblesEstimadas * 100;
            reporte.setTasaOcupacion(Math.round(tasaOcupacion * 100.0) / 100.0); // 2 decimales
        } else {
            reporte.setTasaOcupacion(0.0);
        }

        // Calcular ingresos
        BigDecimal ingresoTotal = reservas.stream()
                .filter(r -> r.getEstado() != EstadoReserva.CANCELADA)
                .map(Reserva::getPrecioTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        reporte.setIngresoTotal(ingresoTotal);

        // Ingresos promedio
        long reservasConfirmadasYCompletadas = confirmadas + completadas;
        if (reservasConfirmadasYCompletadas > 0) {
            BigDecimal promedioPorReserva = ingresoTotal.divide(
                    BigDecimal.valueOf(reservasConfirmadasYCompletadas),
                    2,
                    RoundingMode.HALF_UP
            );
            reporte.setIngresoPromedioPorReserva(promedioPorReserva);
        } else {
            reporte.setIngresoPromedioPorReserva(BigDecimal.ZERO);
        }

        // Ingresos por hora
        if (horasReservadas > 0) {
            BigDecimal promedioPorHora = ingresoTotal.divide(
                    BigDecimal.valueOf(horasReservadas),
                    2,
                    RoundingMode.HALF_UP
            );
            reporte.setIngresoPromedioPorHora(promedioPorHora);
        } else {
            reporte.setIngresoPromedioPorHora(BigDecimal.ZERO);
        }

        // Calcular tasas
        if (totalReservas > 0) {
            double tasaCancelacion = (double) canceladas / totalReservas * 100;
            reporte.setTasaCancelacion(Math.round(tasaCancelacion * 100.0) / 100.0);
            
            double tasaCompletacion = (double) (confirmadas + completadas) / totalReservas * 100;
            reporte.setTasaCompletacion(Math.round(tasaCompletacion * 100.0) / 100.0);
        } else {
            reporte.setTasaCancelacion(0.0);
            reporte.setTasaCompletacion(0.0);
        }

        return reporte;
    }

    /**
     * Genera un reporte consolidado de uso y rentabilidad para todas las pistas
     */
    public ReporteUsoPistasDTO generarReporteUsoPistas(LocalDate fechaInicio, LocalDate fechaFin) {
        List<Pista> pistas = pistaRepository.findAll();
        List<ReporteUsoPistaDTO> reportesPorPista = new ArrayList<>();

        for (Pista pista : pistas) {
            ReporteUsoPistaDTO reportePista = generarReporteUsoPista(pista.getId(), fechaInicio, fechaFin);
            if (reportePista != null) {
                reportesPorPista.add(reportePista);
            }
        }

        ReporteUsoPistasDTO reporteConsolidado = new ReporteUsoPistasDTO();
        reporteConsolidado.setFechaInicio(fechaInicio);
        reporteConsolidado.setFechaFin(fechaFin);
        reporteConsolidado.setReportesPorPista(reportesPorPista);
        reporteConsolidado.setFechaGeneracion(LocalDate.now());
        reporteConsolidado.setGeneradoPor("Sistema automático");

        // Calcular descripción del período
        long dias = ChronoUnit.DAYS.between(fechaInicio, fechaFin) + 1;
        if (dias == 7) {
            String semana = "Semana del " + fechaInicio + " al " + fechaFin;
            reporteConsolidado.setPeriodDescription(semana);
        } else if (fechaInicio.getDayOfMonth() == 1 && fechaFin.getMonthValue() != fechaInicio.getMonthValue()) {
            String mes = "Mes de " + fechaInicio.getMonth().toString();
            reporteConsolidado.setPeriodDescription(mes);
        } else {
            reporteConsolidado.setPeriodDescription("Período del " + fechaInicio + " al " + fechaFin);
        }

        // Sumar totales consolidados
        long totalReservas = reportesPorPista.stream().mapToLong(ReporteUsoPistaDTO::getTotalReservas).sum();
        long totalConfirmadas = reportesPorPista.stream().mapToLong(ReporteUsoPistaDTO::getReservasConfirmadas).sum();
        long totalCompletadas = reportesPorPista.stream().mapToLong(ReporteUsoPistaDTO::getReservasCompletadas).sum();
        long totalCanceladas = reportesPorPista.stream().mapToLong(ReporteUsoPistaDTO::getReservasCanceladas).sum();

        reporteConsolidado.setTotalReservas(totalReservas);
        reporteConsolidado.setTotalReservasConfirmadas(totalConfirmadas);
        reporteConsolidado.setTotalReservasCompletadas(totalCompletadas);
        reporteConsolidado.setTotalReservasCanceladas(totalCanceladas);

        // Ingresos consolidados
        BigDecimal ingresoTotal = reportesPorPista.stream()
                .map(ReporteUsoPistaDTO::getIngresoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        reporteConsolidado.setIngresoTotalConsolidado(ingresoTotal);

        // Promedios
        if (!reportesPorPista.isEmpty()) {
            double tasaOcupacionPromedio = reportesPorPista.stream()
                    .mapToDouble(ReporteUsoPistaDTO::getTasaOcupacion)
                    .average()
                    .orElse(0.0);
            reporteConsolidado.setTasaOcupacionPromedio(Math.round(tasaOcupacionPromedio * 100.0) / 100.0);

            double tasaCancelacionPromedio = reportesPorPista.stream()
                    .mapToDouble(ReporteUsoPistaDTO::getTasaCancelacion)
                    .average()
                    .orElse(0.0);
            reporteConsolidado.setTasaCancelacionPromedio(Math.round(tasaCancelacionPromedio * 100.0) / 100.0);
        }

        return reporteConsolidado;
    }
}