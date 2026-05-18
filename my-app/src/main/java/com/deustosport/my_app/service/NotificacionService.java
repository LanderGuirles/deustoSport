package com.deustosport.my_app.service;

import com.deustosport.my_app.dto.NotificacionResponse;
import com.deustosport.my_app.entity.Incidencia;
import com.deustosport.my_app.entity.Notificacion;
import com.deustosport.my_app.entity.Reserva;
import com.deustosport.my_app.entity.Usuario;
import com.deustosport.my_app.enums.Rol;
import com.deustosport.my_app.enums.TipoNotificacion;
import com.deustosport.my_app.repository.NotificacionRepository;
import com.deustosport.my_app.repository.ReservaRepository;
import com.deustosport.my_app.repository.UsuarioRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final ReservaRepository reservaRepository;
    private final EmailService emailService;

    public NotificacionService(NotificacionRepository notificacionRepository,
                               UsuarioRepository usuarioRepository,
                               ReservaRepository reservaRepository,
                               EmailService emailService) {
        this.notificacionRepository = notificacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.reservaRepository = reservaRepository;
        this.emailService = emailService;
    }

    @Transactional
    public NotificacionResponse crearNotificacionReservaConfirmada(Reserva reserva) {
        String titulo = "Reserva confirmada";
        String mensaje = "Tu reserva en " + reserva.getPista().getNombre() + " para el "
                + reserva.getFechaReserva() + " a las " + reserva.getHoraInicio()
                + " ha sido confirmada y pagada.";

        Notificacion notificacion = crearNotificacion(
                reserva.getUsuario(),
                titulo,
                mensaje,
                TipoNotificacion.RESERVA,
                reserva.getId());

        return toDto(notificacion);
    }

    @Transactional
    public NotificacionResponse notificarIncidenciaReserva(Long reservaId,
                                                           String titulo,
                                                           String mensaje,
                                                           boolean enviarEmail) {
        Objects.requireNonNull(reservaId, "reservaId no puede ser null");

        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        String tituloFinal = (titulo == null || titulo.isBlank())
                ? "Incidencia en tu reserva"
                : titulo.trim();
        String mensajeFinal = (mensaje == null || mensaje.isBlank())
                ? "Se ha detectado una incidencia en tu reserva de " + reserva.getPista().getNombre()
                    + " del día " + reserva.getFechaReserva() + ". Revisa tu planificación."
                : mensaje.trim();

        Notificacion notificacion = crearNotificacion(
                reserva.getUsuario(),
                tituloFinal,
                mensajeFinal,
                TipoNotificacion.INCIDENCIA,
                reserva.getId());

        if (enviarEmail) {
            emailService.enviarEmailNotificacion(
                    reserva.getUsuario().getEmail(),
                    tituloFinal,
                    mensajeFinal);
        }

        return toDto(notificacion);
    }

    @Transactional
    public int enviarComunicado(String titulo,
                                String mensaje,
                                List<Long> destinatarios,
                                boolean enviarEmail) {
        if (titulo == null || titulo.isBlank() || mensaje == null || mensaje.isBlank()) {
            throw new IllegalArgumentException("Titulo y mensaje son obligatorios.");
        }

        List<Usuario> usuariosDestino;
        if (destinatarios == null || destinatarios.isEmpty()) {
            usuariosDestino = usuarioRepository.findAll().stream()
                    .filter(Usuario::isActivo)
                    .filter(u -> u.getRol() == Rol.CLIENTE)
                    .collect(Collectors.toList());
        } else {
            usuariosDestino = usuarioRepository.findAllById(destinatarios).stream()
                    .filter(Usuario::isActivo)
                    .collect(Collectors.toList());
        }

        if (usuariosDestino.isEmpty()) {
            return 0;
        }

        List<Notificacion> lote = new ArrayList<>();
        for (Usuario usuario : usuariosDestino) {
            Notificacion n = new Notificacion();
            n.setUsuario(usuario);
            n.setTitulo(titulo.trim());
            n.setMensaje(mensaje.trim());
            n.setTipo(TipoNotificacion.COMUNICADO);
            n.setLeida(false);
            n.setFechaCreacion(LocalDateTime.now());
            lote.add(n);

            if (enviarEmail) {
                emailService.enviarEmailNotificacion(usuario.getEmail(), titulo.trim(), mensaje.trim());
            }
        }

        notificacionRepository.saveAll(lote);
        return lote.size();
    }

    @Transactional(readOnly = true)
    public List<NotificacionResponse> listarPorUsuario(Long usuarioId) {
        return notificacionRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long contarNoLeidas(Long usuarioId) {
        return notificacionRepository.countByUsuarioIdAndLeidaFalse(usuarioId);
    }

    @Transactional
    public void marcarComoLeida(Long notificacionId, Long usuarioId) {
        Notificacion notificacion = notificacionRepository.findById(notificacionId)
                .orElseThrow(() -> new IllegalArgumentException("Notificacion no encontrada"));

        if (!notificacion.getUsuario().getId().equals(usuarioId)) {
            throw new SecurityException("No tienes permiso para modificar esta notificacion.");
        }

        notificacion.setLeida(true);
        notificacionRepository.save(notificacion);
    }

    @Transactional
    public int marcarTodasComoLeidas(Long usuarioId) {
        List<Notificacion> notificaciones = notificacionRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId);
        int actualizadas = 0;
        for (Notificacion notificacion : notificaciones) {
            if (!notificacion.isLeida()) {
                notificacion.setLeida(true);
                actualizadas++;
            }
        }
        if (actualizadas > 0) {
            notificacionRepository.saveAll(notificaciones);
        }
        return actualizadas;
    }

    @Transactional
    public NotificacionResponse notificarModificacionReservaPorSecretaria(Reserva reserva) {
        String titulo = "Reserva modificada por Secretaría";
        String mensaje = "Tu reserva en " + reserva.getPista().getNombre() + " ha sido modificada por la secretaría. "
                + "Nueva fecha: " + reserva.getFechaReserva() + ", Hora: " + reserva.getHoraInicio() + ".";

        Notificacion notificacion = crearNotificacion(
                reserva.getUsuario(),
                titulo,
                mensaje,
                TipoNotificacion.RESERVA,
                reserva.getId());

        // Opcionalmente enviar email
        emailService.enviarEmailNotificacion(
                reserva.getUsuario().getEmail(),
                titulo,
                mensaje);

        return toDto(notificacion);
    }

    @Transactional
    public NotificacionResponse notificarCompraAbono(Usuario usuario, String nombrePlan, String fechaFin) {
        String titulo = "Suscripción confirmada";
        String mensaje = "Te has suscrito correctamente al plan " + nombrePlan + ". "
                + "Tu abono estará activo hasta el " + fechaFin + ".";

        Notificacion notificacion = crearNotificacion(
                usuario,
                titulo,
                mensaje,
                TipoNotificacion.COMUNICADO,
                null);

        return toDto(notificacion);
    }

    @Transactional
    public void notificarVencimientoProximo(Usuario usuario, String nombrePlan) {
        String titulo = "Tu suscripción vence pronto";
        String mensaje = "Tu suscripción al plan " + nombrePlan + " finalizará en 24 horas.";

        crearNotificacion(
                usuario,
                titulo,
                mensaje,
                TipoNotificacion.INCIDENCIA,
                null);

        // Opcional: enviar email también
        emailService.enviarEmailNotificacion(usuario.getEmail(), titulo, mensaje);
    }

    @Transactional
    public int alertarCoordinacionSobreIncidencia(Incidencia incidencia) {
        List<Usuario> coordinadores = usuarioRepository.findByRolAndActivoTrue(Rol.COORDINADOR);
        if (coordinadores.isEmpty()) {
            return 0;
        }

        String titulo = "Nueva incidencia de mantenimiento";
        StringBuilder mensaje = new StringBuilder();
        mensaje.append("Se ha registrado una incidencia en ")
                .append(incidencia.getPolideportivo().getNombre());

        if (incidencia.getPista() != null) {
            mensaje.append(" - ").append(incidencia.getPista().getNombre());
        }

        mensaje.append(".\n\nTítulo: ").append(incidencia.getTitulo());
        mensaje.append("\nDescripción: ").append(incidencia.getDescripcion());
        mensaje.append("\nReportada por: ").append(incidencia.getReportadaPor().getNombreCompleto());

        List<Notificacion> lote = new ArrayList<>();
        for (Usuario coordinador : coordinadores) {
            Notificacion notificacion = new Notificacion();
            notificacion.setUsuario(coordinador);
            notificacion.setTitulo(titulo);
            notificacion.setMensaje(mensaje.toString());
            notificacion.setTipo(TipoNotificacion.INCIDENCIA);
            notificacion.setLeida(false);
            notificacion.setFechaCreacion(LocalDateTime.now());
            lote.add(notificacion);

            emailService.enviarEmailNotificacion(coordinador.getEmail(), titulo, mensaje.toString());
        }

        notificacionRepository.saveAll(lote);
        return lote.size();
    }

    private Notificacion crearNotificacion(Usuario usuario,
                                           String titulo,
                                           String mensaje,
                                           TipoNotificacion tipo,
                                           Long reservaId) {
        Notificacion n = new Notificacion();
        n.setUsuario(usuario);
        n.setTitulo(titulo);
        n.setMensaje(mensaje);
        n.setTipo(tipo);
        n.setLeida(false);
        n.setFechaCreacion(LocalDateTime.now());
        n.setReservaId(reservaId);
        return notificacionRepository.save(n);
    }

    private NotificacionResponse toDto(Notificacion n) {
        NotificacionResponse dto = new NotificacionResponse();
        dto.setId(n.getId());
        dto.setUsuarioId(n.getUsuario().getId());
        dto.setTitulo(n.getTitulo());
        dto.setMensaje(n.getMensaje());
        dto.setTipo(n.getTipo());
        dto.setLeida(n.isLeida());
        dto.setFechaCreacion(n.getFechaCreacion());
        dto.setReservaId(n.getReservaId());
        return dto;
    }
}
