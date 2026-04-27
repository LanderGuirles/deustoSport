package com.deustosport.my_app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.deustosport.my_app.dto.SecretariaReservaResumenDto;
import com.deustosport.my_app.dto.SecretariaUsuarioResumenDto;
import com.deustosport.my_app.entity.Pista;
import com.deustosport.my_app.entity.Reserva;
import com.deustosport.my_app.entity.Usuario;
import com.deustosport.my_app.enums.EstadoReserva;
import com.deustosport.my_app.repository.ReservaRepository;
import com.deustosport.my_app.repository.UsuarioRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class SecretariaServiceTest {

    private static final Logger log = LoggerFactory.getLogger(SecretariaServiceTest.class);

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private ReservaRepository reservaRepository;

    @InjectMocks
    private SecretariaService secretariaService;

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Usuario usuarioBase(Long id, String nombre, String dni) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNombreCompleto(nombre);
        u.setDni(dni);
        u.setEmail(nombre.toLowerCase().replace(" ", ".") + "@test.com");
        u.setTelefono("666000111");
        u.setActivo(true);
        return u;
    }

    private Reserva reservaBase(Long id, LocalDate fecha) {
        Usuario usuario = usuarioBase(1L, "Ana Lopez", "12345678A");

        Pista pista = new Pista();
        pista.setNombre("Pista 1");

        Reserva r = new Reserva();
        r.setId(id);
        r.setFechaReserva(fecha);
        r.setHoraInicio(LocalTime.of(10, 0));
        r.setHoraFin(LocalTime.of(11, 0));
        r.setEstado(EstadoReserva.CONFIRMADA);
        r.setPista(pista);
        r.setUsuario(usuario);
        return r;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  buscarUsuarios
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void buscarUsuarios_conDni_devuelveResumenes() {
        log.info("[TEST] buscarUsuarios_conDni_devuelveResumenes - dni=12345678A");
        Usuario u = usuarioBase(1L, "Ana Lopez", "12345678A");
        when(usuarioRepository.buscarParaSecretaria("12345678A")).thenReturn(List.of(u));
        when(reservaRepository.countByUsuarioId(1L)).thenReturn(3L);

        List<SecretariaUsuarioResumenDto> resultado = secretariaService.buscarUsuarios("12345678A");

        assertEquals(1, resultado.size());
        assertEquals("Ana Lopez", resultado.get(0).getNombreCompleto());
        assertEquals(3L, resultado.get(0).getTotalReservas());
        log.info("[TEST] Resultado: {} usuario(s), totalReservas={}", resultado.size(), resultado.get(0).getTotalReservas());
    }

    @Test
    void buscarUsuarios_dniNull_pasaNullAlRepositorio() {
        log.info("[TEST] buscarUsuarios_dniNull_pasaNullAlRepositorio");
        when(usuarioRepository.buscarParaSecretaria(null)).thenReturn(List.of());

        List<SecretariaUsuarioResumenDto> resultado = secretariaService.buscarUsuarios(null);

        assertTrue(resultado.isEmpty());
        verify(usuarioRepository).buscarParaSecretaria(null);
        log.info("[TEST] Lista vacía devuelta correctamente");
    }

    @Test
    void buscarUsuarios_dniVacio_pasaNullAlRepositorio() {
        log.info("[TEST] buscarUsuarios_dniVacio_pasaNullAlRepositorio - '  ' → null");
        when(usuarioRepository.buscarParaSecretaria(null)).thenReturn(List.of());

        secretariaService.buscarUsuarios("   ");

        verify(usuarioRepository).buscarParaSecretaria(null);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  obtenerReservasPorUsuario
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void obtenerReservasPorUsuario_devuelveResumenes() {
        log.info("[TEST] obtenerReservasPorUsuario_devuelveResumenes - usuarioId=1");
        Reserva r = reservaBase(99L, LocalDate.of(2026, 5, 1));
        when(reservaRepository.findByUsuarioIdOrderByFechaReservaDescHoraInicioDesc(1L)).thenReturn(List.of(r));

        List<SecretariaReservaResumenDto> resultado = secretariaService.obtenerReservasPorUsuario(1L);

        assertEquals(1, resultado.size());
        assertEquals("CONFIRMADA", resultado.get(0).getEstado());
        assertEquals("Pista 1", resultado.get(0).getPistaNombre());
        log.info("[TEST] Reserva devuelta: id={}, estado={}", resultado.get(0).getId(), resultado.get(0).getEstado());
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  exportarReservasDeHoyCsv
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void exportarReservasDeHoyCsv_conReservas_generaCsvConHeader() {
        log.info("[TEST] exportarReservasDeHoyCsv_conReservas_generaCsvConHeader");
        LocalDate hoy = LocalDate.now();
        Reserva r = reservaBase(1L, hoy);
        when(reservaRepository.findByFechaReservaOrderByHoraInicioAscIdAsc(hoy)).thenReturn(List.of(r));

        byte[] csv = secretariaService.exportarReservasDeHoyCsv();
        String contenido = new String(csv, java.nio.charset.StandardCharsets.UTF_8);

        assertTrue(contenido.startsWith("id,fecha,hora_inicio"));
        assertTrue(contenido.contains("CONFIRMADA"));
        assertTrue(contenido.contains("Pista 1"));
        log.info("[TEST] CSV generado, {} bytes, contiene cabecera y datos", csv.length);
    }

    @Test
    void exportarReservasDeHoyCsv_sinReservas_devuelveSoloHeader() {
        log.info("[TEST] exportarReservasDeHoyCsv_sinReservas_devuelveSoloHeader");
        LocalDate hoy = LocalDate.now();
        when(reservaRepository.findByFechaReservaOrderByHoraInicioAscIdAsc(hoy)).thenReturn(List.of());

        byte[] csv = secretariaService.exportarReservasDeHoyCsv();
        String contenido = new String(csv, java.nio.charset.StandardCharsets.UTF_8);

        assertTrue(contenido.startsWith("id,fecha,hora_inicio"));
        assertEquals(1, contenido.lines().filter(l -> !l.isBlank()).count(), "Solo debe haber la cabecera");
        log.info("[TEST] CSV solo con cabecera, {} bytes", csv.length);
    }
}
