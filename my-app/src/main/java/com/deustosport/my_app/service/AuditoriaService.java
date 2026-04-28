package com.deustosport.my_app.service;

import com.deustosport.my_app.entity.Auditoria;
import com.deustosport.my_app.repository.AuditoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaService(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    /**
     * Registra una acción en el sistema de auditoría
     */
    @Transactional
    public void registrarAccion(String usuario, String accion, String entidad,
                                Long entidadId, String detalles) {
        Auditoria auditoria = new Auditoria();
        auditoria.setUsuario(usuario);
        auditoria.setAccion(accion);
        auditoria.setEntidad(entidad);
        auditoria.setEntidadId(entidadId);
        auditoria.setDetalles(detalles);
        auditoria.setFechaHora(LocalDateTime.now());

        auditoriaRepository.save(auditoria);
    }

    /**
     * Registra login de usuario
     */
    public void registrarLogin(String usuario) {
        registrarAccion(usuario, "LOGIN", "Usuario", null, "Inicio de sesión exitoso");
    }

    /**
     * Registra logout de usuario
     */
    public void registrarLogout(String usuario) {
        registrarAccion(usuario, "LOGOUT", "Usuario", null, "Cierre de sesión");
    }

    /**
     * Registra creación de reserva
     */
    public void registrarCreacionReserva(String usuario, Long reservaId) {
        registrarAccion(usuario, "CREAR", "Reserva", reservaId, "Reserva creada");
    }

    /**
     * Registra pago de reserva
     */
    public void registrarPagoReserva(String usuario, Long reservaId, String metodoPago) {
        registrarAccion(usuario, "PAGAR", "Reserva", reservaId,
                       "Pago realizado con método: " + metodoPago);
    }

    /**
     * Registra cancelación de reserva
     */
    public void registrarCancelacionReserva(String usuario, Long reservaId) {
        registrarAccion(usuario, "CANCELAR", "Reserva", reservaId, "Reserva cancelada");
    }

    /**
     * Registra modificación de perfil
     */
    public void registrarModificacionPerfil(String usuario, Long usuarioId) {
        registrarAccion(usuario, "MODIFICAR", "Usuario", usuarioId, "Perfil actualizado");
    }

    /**
     * Registra cambio de contraseña
     */
    public void registrarCambioPassword(String usuario, Long usuarioId) {
        registrarAccion(usuario, "CAMBIAR_PASSWORD", "Usuario", usuarioId, "Contraseña cambiada");
    }

    /**
     * Registra recarga de billetera
     */
    public void registrarRecargaBilletera(String usuario, Long usuarioId, String cantidad) {
        registrarAccion(usuario, "RECARGAR", "Usuario", usuarioId,
                       "Billetera recargada con: " + cantidad + "€");
    }

    /**
     * Registra creación de usuario (por admin)
     */
    public void registrarCreacionUsuario(String admin, Long nuevoUsuarioId, String nombreUsuario) {
        registrarAccion(admin, "CREAR", "Usuario", nuevoUsuarioId,
                       "Usuario creado: " + nombreUsuario);
    }

    /**
     * Registra desactivación de usuario
     */
    public void registrarDesactivacionUsuario(String admin, Long usuarioId, String nombreUsuario) {
        registrarAccion(admin, "DESACTIVAR", "Usuario", usuarioId,
                       "Usuario desactivado: " + nombreUsuario);
    }

    /**
     * Registra activación de usuario
     */
    public void registrarActivacionUsuario(String admin, Long usuarioId, String nombreUsuario) {
        registrarAccion(admin, "ACTIVAR", "Usuario", usuarioId,
                       "Usuario activado: " + nombreUsuario);
    }

    /**
     * Registra envío de email
     */
    public void registrarEnvioEmail(String sistema, String destinatario, String tipoEmail) {
        registrarAccion(sistema, "ENVIAR_EMAIL", "Email", null,
                       "Email enviado a: " + destinatario + " - Tipo: " + tipoEmail);
    }

    /**
     * Registra creación de notificación
     */
    public void registrarCreacionNotificacion(String sistema, Long usuarioId, String tipo) {
        registrarAccion(sistema, "CREAR_NOTIFICACION", "Notificacion", usuarioId,
                       "Notificación creada - Tipo: " + tipo);
    }

    /**
     * Obtiene todas las acciones de auditoría
     */
    public List<Auditoria> obtenerTodasLasAcciones() {
        return auditoriaRepository.findAllByOrderByFechaHoraDesc();
    }

    /**
     * Obtiene acciones de auditoría por usuario
     */
    public List<Auditoria> obtenerAccionesPorUsuario(String usuario) {
        return auditoriaRepository.findByUsuarioOrderByFechaHoraDesc(usuario);
    }

    /**
     * Obtiene acciones de auditoría por tipo de acción
     */
    public List<Auditoria> obtenerAuditoriasPorAccion(String accion) {
        return auditoriaRepository.findByAccionOrderByFechaHoraDesc(accion);
    }

    /**
     * Obtiene acciones de auditoría por entidad
     */
    public List<Auditoria> obtenerAccionesPorEntidad(String entidad) {
        return auditoriaRepository.findByEntidadOrderByFechaHoraDesc(entidad);
    }

    /**
     * Obtiene acciones de auditoría por rango de fechas
     */
    public List<Auditoria> obtenerAccionesPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return auditoriaRepository.findByFechaHoraBetweenOrderByFechaHoraDesc(fechaInicio, fechaFin);
    }

    /**
     * Obtiene acciones de auditoría recientes (últimas N)
     */
    public List<Auditoria> obtenerAccionesRecientes(int limit) {
        return auditoriaRepository.findTopByOrderByFechaHoraDesc(limit);
    }

    /**
     * Obtiene estadísticas de auditoría
     */
    public long contarAccionesPorTipo(String accion) {
        return auditoriaRepository.countByAccion(accion);
    }

    /**
     * Obtiene usuarios más activos
     */
    public List<Object[]> obtenerUsuariosMasActivos(int limit) {
        return auditoriaRepository.findTopUsuariosByActividad(limit);
    }

    /**
     * Limpia registros antiguos de auditoría (más de X días)
     */
    @Transactional
    public void limpiarRegistrosAntiguos(int diasMantener) {
        LocalDateTime fechaLimite = LocalDateTime.now().minusDays(diasMantener);
        auditoriaRepository.deleteByFechaHoraBefore(fechaLimite);
    }

    /**
     * Registra intento de acceso fallido
     */
    public void registrarIntentoFallido(String usuario, String razon) {
        registrarAccion(usuario, "ACCESO_FALLIDO", "Seguridad", null,
                       "Intento de acceso fallido - Razón: " + razon);
    }

    /**
     * Registra cambio de configuración del sistema
     */
    public void registrarCambioConfiguracion(String admin, String configuracion, String valorAnterior, String valorNuevo) {
        registrarAccion(admin, "CAMBIAR_CONFIG", "Configuracion", null,
                       "Configuración '" + configuracion + "' cambiada de '" + valorAnterior + "' a '" + valorNuevo + "'");
    }

    /**
     * Registra backup del sistema
     */
    public void registrarBackup(String admin, String tipoBackup) {
        registrarAccion(admin, "BACKUP", "Sistema", null,
                       "Backup realizado - Tipo: " + tipoBackup);
    }

    /**
     * Registra mantenimiento del sistema
     */
    public void registrarMantenimiento(String admin, String descripcion) {
        registrarAccion(admin, "MANTENIMIENTO", "Sistema", null,
                       "Mantenimiento realizado: " + descripcion);
    }
}