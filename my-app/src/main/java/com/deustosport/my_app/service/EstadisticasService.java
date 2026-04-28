package com.deustosport.my_app.service;

import com.deustosport.my_app.repository.ReservaRepository;
import com.deustosport.my_app.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class EstadisticasService {

    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;

    public EstadisticasService(ReservaRepository reservaRepository,
                               UsuarioRepository usuarioRepository) {
        this.reservaRepository = reservaRepository;
        this.usuarioRepository = usuarioRepository;
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
        long reservasConfirmadas = reservaRepository.countByEstado("CONFIRMADA");
        stats.put("reservasConfirmadas", reservasConfirmadas);

        // Reservas pendientes
        long reservasPendientes = reservaRepository.countByEstado("PENDIENTE");
        stats.put("reservasPendientes", reservasPendientes);

        // Reservas canceladas
        long reservasCanceladas = reservaRepository.countByEstado("CANCELADA");
        stats.put("reservasCanceladas", reservasCanceladas);

        // Ingresos totales
        BigDecimal ingresosTotales = reservaRepository.sumPrecioTotalByEstado("CONFIRMADA");
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
        long pagosTarjeta = reservaRepository.countByMetodoPago("TARJETA");
        long pagosBizum = reservaRepository.countByMetodoPago("BIZUM");
        long pagosTransferencia = reservaRepository.countByMetodoPago("TRANSFERENCIA");
        long pagosBilletera = reservaRepository.countByMetodoPago("BILLETERA");

        stats.put("pagosTarjeta", pagosTarjeta);
        stats.put("pagosBizum", pagosBizum);
        stats.put("pagosTransferencia", pagosTransferencia);
        stats.put("pagosBilletera", pagosBilletera);

        return stats;
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
}