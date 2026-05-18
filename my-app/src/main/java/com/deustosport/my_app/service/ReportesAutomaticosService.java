package com.deustosport.my_app.service;

import com.deustosport.my_app.dto.ReporteUsoPistasDTO;
import com.deustosport.my_app.entity.Notificacion;
import com.deustosport.my_app.entity.Usuario;
import com.deustosport.my_app.enums.Rol;
import com.deustosport.my_app.enums.TipoNotificacion;
import com.deustosport.my_app.repository.NotificacionRepository;
import com.deustosport.my_app.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio para generar reportes automáticos de uso de pistas
 * Los reportes se generan cada lunes a las 8:00 AM (cron: 0 8 * * 1)
 */
@Service
public class ReportesAutomaticosService {

    private static final Logger logger = LoggerFactory.getLogger(ReportesAutomaticosService.class);

    private final EstadisticasService estadisticasService;
    private final UsuarioRepository usuarioRepository;
    private final NotificacionRepository notificacionRepository;

    public ReportesAutomaticosService(EstadisticasService estadisticasService,
                                       UsuarioRepository usuarioRepository,
                                       NotificacionRepository notificacionRepository) {
        this.estadisticasService = estadisticasService;
        this.usuarioRepository = usuarioRepository;
        this.notificacionRepository = notificacionRepository;
    }

    /**
     * Genera y envía un reporte de la semana anterior a los coordinadores
     * Se ejecuta automáticamente cada lunes a las 8:00 AM
     * Cron: segundo=0, minuto=0, hora=8, día del mes=*, mes=*, día de la semana=1 (lunes)
     */
    @Scheduled(cron = "0 0 8 * * 1", zone = "Europe/Madrid")
    public void generarReporteSemanal() {
        logger.info("Iniciando generación automática de reporte semanal...");

        try {
            // Calcular período: semana anterior (lunes a domingo)
            LocalDate hoy = LocalDate.now();
            // Calcular último domingo
            LocalDate ultimoDomingo = hoy.minusDays(hoy.getDayOfWeek().getValue());
            // Calcular lunes de hace una semana
            LocalDate inicioSemana = ultimoDomingo.minusDays(6);
            LocalDate finSemana = ultimoDomingo;

            // Generar el reporte
            ReporteUsoPistasDTO reporte = estadisticasService.generarReporteUsoPistas(inicioSemana, finSemana);
            reporte.setGeneradoPor("Sistema automático - Reporte semanal");

            logger.info("Reporte generado para período: {} a {}", inicioSemana, finSemana);
            logger.info("Ingresos totales: {}", reporte.getIngresoTotalConsolidado());
            logger.info("Tasa ocupación promedio: {}%", reporte.getTasaOcupacionPromedio());

            // Enviar notificación a todos los coordinadores
            enviarReporteACoordinadores(reporte);

            logger.info("Reporte semanal completado exitosamente");

        } catch (Exception e) {
            logger.error("Error al generar reporte semanal automático", e);
        }
    }

    /**
     * Envía el reporte a todos los coordinadores del sistema
     */
    private void enviarReporteACoordinadores(ReporteUsoPistasDTO reporte) {
        List<Usuario> coordinadores = usuarioRepository.findByRolAndActivoTrue(Rol.COORDINADOR);

        if (coordinadores.isEmpty()) {
            logger.warn("No hay coordinadores para enviar el reporte");
            return;
        }

        logger.info("Enviando reporte a {} coordinador(es)", coordinadores.size());

        for (Usuario coordinador : coordinadores) {
            try {
                enviarReporteAlCoordinador(coordinador, reporte);
            } catch (Exception e) {
                logger.error("Error al enviar reporte al coordinador: {}", coordinador.getNombreCompleto(), e);
            }
        }
    }

    /**
     * Crea y envía el reporte a un coordinador específico
     */
    private void enviarReporteAlCoordinador(Usuario coordinador, ReporteUsoPistasDTO reporte) {
        // Construir mensaje con resumen del reporte
        StringBuilder mensajeBuilder = new StringBuilder();
        mensajeBuilder.append("📊 Reporte Semanal de Rentabilidad de Pistas\n\n");
        mensajeBuilder.append("Período: ").append(reporte.getPeriodDescription()).append("\n");
        mensajeBuilder.append("Generado: ").append(LocalDateTime.now()).append("\n\n");

        mensajeBuilder.append("📈 Resumen Consolidado:\n");
        mensajeBuilder.append("- Total Reservas: ").append(reporte.getTotalReservas()).append("\n");
        mensajeBuilder.append("- Reservas Confirmadas: ").append(reporte.getTotalReservasConfirmadas()).append("\n");
        mensajeBuilder.append("- Ingresos Totales: €").append(reporte.getIngresoTotalConsolidado()).append("\n");
        mensajeBuilder.append("- Ocupación Promedio: ").append(reporte.getTasaOcupacionPromedio()).append("%\n\n");

        mensajeBuilder.append("Revisa el panel de coordinación para ver el detalle completo por pista.");

        String titulo = "📊 Reporte Semanal de Rentabilidad - " + reporte.getPeriodDescription();
        String mensaje = mensajeBuilder.toString();

        // Crear y guardar notificación
        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(coordinador);
        notificacion.setTitulo(titulo);
        notificacion.setMensaje(mensaje);
        notificacion.setFechaCreacion(LocalDateTime.now());
        notificacion.setLeida(false);
        notificacion.setTipo(TipoNotificacion.COMUNICADO);
        notificacion.setReservaId(null);

        notificacionRepository.save(notificacion);

        logger.info("Notificación enviada al coordinador: {}", coordinador.getNombreCompleto());
    }

    /**
     * Genera y envía un reporte mensual a los coordinadores
     * Se ejecuta automáticamente el primer día del mes a las 9:00 AM
     * Cron: segundo=0, minuto=0, hora=9, día del mes=1, mes=*, día de la semana=*
     */
    @Scheduled(cron = "0 0 9 1 * *", zone = "Europe/Madrid")
    public void generarReporteMensual() {
        logger.info("Iniciando generación automática de reporte mensual...");

        try {
            // Calcular período: mes anterior
            LocalDate hoy = LocalDate.now();
            LocalDate inicioMes = hoy.minusMonths(1).withDayOfMonth(1);
            LocalDate finMes = hoy.withDayOfMonth(1).minusDays(1);

            // Generar el reporte
            ReporteUsoPistasDTO reporte = estadisticasService.generarReporteUsoPistas(inicioMes, finMes);
            reporte.setGeneradoPor("Sistema automático - Reporte mensual");

            logger.info("Reporte mensual generado para período: {} a {}", inicioMes, finMes);
            logger.info("Ingresos totales: {}", reporte.getIngresoTotalConsolidado());

            // Enviar notificación a todos los coordinadores
            enviarReporteACoordinadores(reporte);

            logger.info("Reporte mensual completado exitosamente");

        } catch (Exception e) {
            logger.error("Error al generar reporte mensual automático", e);
        }
    }
}
