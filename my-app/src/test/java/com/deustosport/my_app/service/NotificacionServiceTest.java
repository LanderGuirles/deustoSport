package com.deustosport.my_app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.deustosport.my_app.dto.NotificacionResponse;
import com.deustosport.my_app.entity.Notificacion;
import com.deustosport.my_app.entity.Pista;
import com.deustosport.my_app.entity.Reserva;
import com.deustosport.my_app.entity.Usuario;
import com.deustosport.my_app.enums.Rol;
import com.deustosport.my_app.enums.TipoNotificacion;
import com.deustosport.my_app.repository.NotificacionRepository;
import com.deustosport.my_app.repository.ReservaRepository;
import com.deustosport.my_app.repository.UsuarioRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class NotificacionServiceTest {

    private static final Logger log = LoggerFactory.getLogger(NotificacionServiceTest.class);

    @Mock private NotificacionRepository notificacionRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private ReservaRepository reservaRepository;
    @Mock private EmailService emailService;

    @InjectMocks
    private NotificacionService notificacionService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombreCompleto("Test Usuario");
        usuario.setEmail("test@deustosport.com");
        usuario.setRol(Rol.CLIENTE);
        usuario.setActivo(true);
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private Notificacion notificacionBase(Long id, boolean leida) {
        Notificacion n = new Notificacion();
        n.setId(id);
        n.setUsuario(usuario);
        n.setTitulo("Titulo test");
        n.setMensaje("Mensaje test");
        n.setTipo(TipoNotificacion.RESERVA);
        n.setLeida(leida);
        n.setFechaCreacion(LocalDateTime.now());
        return n;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  listarPorUsuario
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void listarPorUsuario_devuelveListaMapeadaADto() {
        log.info("[TEST] listarPorUsuario_devuelveListaMapeadaADto - usuarioId=1");
        Notificacion n = notificacionBase(10L, false);
        when(notificacionRepository.findByUsuarioIdOrderByFechaCreacionDesc(1L)).thenReturn(List.of(n));

        List<NotificacionResponse> resultado = notificacionService.listarPorUsuario(1L);

        assertEquals(1, resultado.size());
        assertEquals("Titulo test", resultado.get(0).getTitulo());
        assertFalse(resultado.get(0).isLeida());
        log.info("[TEST] Notificaciones devueltas: {}", resultado.size());
    }

    @Test
    void listarPorUsuario_sinNotificaciones_devuelveListaVacia() {
        log.info("[TEST] listarPorUsuario_sinNotificaciones_devuelveListaVacia");
        when(notificacionRepository.findByUsuarioIdOrderByFechaCreacionDesc(99L)).thenReturn(List.of());

        List<NotificacionResponse> resultado = notificacionService.listarPorUsuario(99L);

        assertTrue(resultado.isEmpty());
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  contarNoLeidas
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void contarNoLeidas_devuelveConteoCorrect() {
        log.info("[TEST] contarNoLeidas_devuelveConteoCorrect - usuarioId=1");
        when(notificacionRepository.countByUsuarioIdAndLeidaFalse(1L)).thenReturn(5L);

        long count = notificacionService.contarNoLeidas(1L);

        assertEquals(5L, count);
        log.info("[TEST] Notificaciones no leídas: {}", count);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  marcarComoLeida
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void marcarComoLeida_exitoso_cambiaEstadoALeida() {
        log.info("[TEST] marcarComoLeida_exitoso_cambiaEstadoALeida - notifId=10, userId=1");
        Notificacion n = notificacionBase(10L, false);
        when(notificacionRepository.findById(10L)).thenReturn(Optional.of(n));
        when(notificacionRepository.save(any(Notificacion.class))).thenAnswer(inv -> inv.getArgument(0));

        notificacionService.marcarComoLeida(10L, 1L);

        assertTrue(n.isLeida());
        verify(notificacionRepository).save(n);
        log.info("[TEST] Notificación marcada como leída correctamente");
    }

    @Test
    void marcarComoLeida_usuarioNoAutorizado_lanzaSecurityException() {
        log.info("[TEST] marcarComoLeida_usuarioNoAutorizado_lanzaSecurityException - otro userId=99");
        Notificacion n = notificacionBase(10L, false);
        when(notificacionRepository.findById(10L)).thenReturn(Optional.of(n));

        assertThrows(SecurityException.class,
                () -> notificacionService.marcarComoLeida(10L, 99L));

        verify(notificacionRepository, never()).save(any());
        log.info("[TEST] SecurityException lanzada correctamente para usuario no autorizado");
    }

    @Test
    void marcarComoLeida_notificacionInexistente_lanzaIllegalArgument() {
        log.info("[TEST] marcarComoLeida_notificacionInexistente_lanzaIllegalArgument - notifId=999");
        when(notificacionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> notificacionService.marcarComoLeida(999L, 1L));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  marcarTodasComoLeidas
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void marcarTodasComoLeidas_actualizaSoloLasNoLeidas() {
        log.info("[TEST] marcarTodasComoLeidas_actualizaSoloLasNoLeidas - 2 no leídas de 3");
        Notificacion leida    = notificacionBase(1L, true);
        Notificacion noLeida1 = notificacionBase(2L, false);
        Notificacion noLeida2 = notificacionBase(3L, false);
        when(notificacionRepository.findByUsuarioIdOrderByFechaCreacionDesc(1L))
                .thenReturn(List.of(leida, noLeida1, noLeida2));

        int actualizadas = notificacionService.marcarTodasComoLeidas(1L);

        assertEquals(2, actualizadas);
        assertTrue(noLeida1.isLeida());
        assertTrue(noLeida2.isLeida());
        verify(notificacionRepository).saveAll(anyList());
        log.info("[TEST] Actualizadas {} de 3 notificaciones", actualizadas);
    }

    @Test
    void marcarTodasComoLeidas_todasYaLeidas_noGuardaNada() {
        log.info("[TEST] marcarTodasComoLeidas_todasYaLeidas_noGuardaNada");
        when(notificacionRepository.findByUsuarioIdOrderByFechaCreacionDesc(1L))
                .thenReturn(List.of(notificacionBase(1L, true), notificacionBase(2L, true)));

        int actualizadas = notificacionService.marcarTodasComoLeidas(1L);

        assertEquals(0, actualizadas);
        verify(notificacionRepository, never()).saveAll(any());
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  enviarComunicado
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void enviarComunicado_tituloVacio_lanzaIllegalArgument() {
        log.info("[TEST] enviarComunicado_tituloVacio_lanzaIllegalArgument");
        assertThrows(IllegalArgumentException.class,
                () -> notificacionService.enviarComunicado("", "Mensaje", null, false));
    }

    @Test
    void enviarComunicado_sinDestinatarios_enviaATodosLosClientesActivos() {
        log.info("[TEST] enviarComunicado_sinDestinatarios_enviaATodosLosClientesActivos");
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));
        when(notificacionRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        int enviadas = notificacionService.enviarComunicado("Aviso", "Texto del comunicado", null, false);

        assertEquals(1, enviadas);
        verify(notificacionRepository).saveAll(anyList());
        verify(emailService, never()).enviarEmailNotificacion(any(), any(), any());
        log.info("[TEST] Comunicado enviado a {} usuarios", enviadas);
    }

    @Test
    void enviarComunicado_conEmail_llamaAlEmailService() {
        log.info("[TEST] enviarComunicado_conEmail_llamaAlEmailService");
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));
        when(notificacionRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        notificacionService.enviarComunicado("Aviso", "Texto", null, true);

        verify(emailService).enviarEmailNotificacion(
                eq("test@deustosport.com"), eq("Aviso"), eq("Texto"));
        log.info("[TEST] EmailService llamado correctamente");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  crearNotificacionReservaConfirmada
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void crearNotificacionReservaConfirmada_creaNotificacionConDatosReserva() {
        log.info("[TEST] crearNotificacionReservaConfirmada_creaNotificacionConDatosReserva");
        Pista pista = new Pista();
        pista.setNombre("Pista Central");

        Reserva reserva = new Reserva();
        reserva.setId(5L);
        reserva.setUsuario(usuario);
        reserva.setPista(pista);
        reserva.setFechaReserva(LocalDate.of(2026, 6, 1));
        reserva.setHoraInicio(LocalTime.of(10, 0));

        Notificacion guardada = new Notificacion();
        guardada.setId(20L);
        guardada.setUsuario(usuario);
        guardada.setTitulo("Reserva confirmada");
        guardada.setMensaje("Tu reserva...");
        guardada.setTipo(TipoNotificacion.RESERVA);
        guardada.setLeida(false);
        guardada.setFechaCreacion(LocalDateTime.now());
        when(notificacionRepository.save(any(Notificacion.class))).thenReturn(guardada);

        NotificacionResponse response = notificacionService.crearNotificacionReservaConfirmada(reserva);

        assertNotNull(response);
        assertEquals("Reserva confirmada", response.getTitulo());
        assertEquals(TipoNotificacion.RESERVA, response.getTipo());
        log.info("[TEST] Notificación creada: titulo='{}'", response.getTitulo());
    }
}
