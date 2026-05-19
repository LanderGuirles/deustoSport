package com.deustosport.my_app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.deustosport.my_app.dto.PistaDisponibleDTO;
import com.deustosport.my_app.entity.Pista;
import com.deustosport.my_app.entity.Polideportivo;
import com.deustosport.my_app.enums.TipoDeporte;
import com.deustosport.my_app.repository.PistaRepository;
import com.deustosport.my_app.repository.PolideportivoRepository;
import com.deustosport.my_app.repository.ReservaRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class PistaServiceBusquedaTest {

    private static final Logger log = LoggerFactory.getLogger(PistaServiceBusquedaTest.class);

    @Mock private PistaRepository pistaRepository;
    @Mock private PolideportivoRepository polideportivoRepository;
    @Mock private ReservaRepository reservaRepository;
    @Mock private ReservaService reservaService;

    @InjectMocks private PistaService pistaService;

    private Polideportivo polideportivo;
    private Pista pistaPadel;
    private Pista pistaOtraPadel;

    @BeforeEach
    void setUp() {
        polideportivo = new Polideportivo();
        polideportivo.setId(1L);
        polideportivo.setNombre("Polideportivo Central");
        polideportivo.setDireccion("Calle Mayor 1");
        polideportivo.setHoraApertura(LocalTime.of(8, 0));
        polideportivo.setHoraCierre(LocalTime.of(22, 0));

        pistaPadel = new Pista();
        pistaPadel.setId(10L);
        pistaPadel.setNombre("Pista Pádel 1");
        pistaPadel.setTipoDeporte(TipoDeporte.PADEL);
        pistaPadel.setMaxJugadores(4);
        pistaPadel.setActiva(true);
        pistaPadel.setPolideportivo(polideportivo);

        pistaOtraPadel = new Pista();
        pistaOtraPadel.setId(11L);
        pistaOtraPadel.setNombre("Pista Pádel 2");
        pistaOtraPadel.setTipoDeporte(TipoDeporte.PADEL);
        pistaOtraPadel.setMaxJugadores(4);
        pistaOtraPadel.setActiva(true);
        pistaOtraPadel.setPolideportivo(polideportivo);
    }

    // ── buscarPistasDisponibles (HU5) ─────────────────────────────────────────

    @Test
    void buscarDisponibles_unaLibre_devuelveLista() {
        log.info("[TEST] PistaService.buscarPistasDisponibles - una pista libre");
        LocalDate fecha = LocalDate.now().plusDays(1);
        LocalTime inicio = LocalTime.of(10, 0);
        LocalTime fin = LocalTime.of(11, 0);

        when(pistaRepository.findByTipoDeporte(TipoDeporte.PADEL)).thenReturn(List.of(pistaPadel));
        when(reservaRepository.findConflictingReservations(10L, fecha, inicio, fin))
                .thenReturn(List.of());

        List<PistaDisponibleDTO> resultado =
                pistaService.buscarPistasDisponibles(TipoDeporte.PADEL, fecha, inicio, fin);

        assertEquals(1, resultado.size());
        assertEquals(10L, resultado.get(0).getPistaId());
        assertEquals("Pista Pádel 1", resultado.get(0).getPistaNombre());
        assertEquals(1L, resultado.get(0).getPolideportivoId());
        log.info("[TEST] Pista disponible: {}", resultado.get(0).getPistaNombre());
    }

    @Test
    void buscarDisponibles_dosLibres_devuelveAmbas() {
        log.info("[TEST] PistaService.buscarPistasDisponibles - dos pistas libres");
        LocalDate fecha = LocalDate.now().plusDays(1);
        LocalTime inicio = LocalTime.of(16, 0);
        LocalTime fin = LocalTime.of(17, 0);

        when(pistaRepository.findByTipoDeporte(TipoDeporte.PADEL))
                .thenReturn(List.of(pistaPadel, pistaOtraPadel));
        when(reservaRepository.findConflictingReservations(anyLong(), any(), any(), any()))
                .thenReturn(List.of());

        List<PistaDisponibleDTO> resultado =
                pistaService.buscarPistasDisponibles(TipoDeporte.PADEL, fecha, inicio, fin);

        assertEquals(2, resultado.size());
        log.info("[TEST] {} pistas disponibles", resultado.size());
    }

    @Test
    void buscarDisponibles_unaOcupada_excluyeOcupada() {
        log.info("[TEST] PistaService.buscarPistasDisponibles - una pista ocupada se excluye");
        LocalDate fecha = LocalDate.now().plusDays(1);
        LocalTime inicio = LocalTime.of(10, 0);
        LocalTime fin = LocalTime.of(11, 0);

        com.deustosport.my_app.entity.Reserva reservaExistente =
                new com.deustosport.my_app.entity.Reserva();
        reservaExistente.setId(99L);

        when(pistaRepository.findByTipoDeporte(TipoDeporte.PADEL))
                .thenReturn(List.of(pistaPadel, pistaOtraPadel));
        when(reservaRepository.findConflictingReservations(10L, fecha, inicio, fin))
                .thenReturn(List.of(reservaExistente));
        when(reservaRepository.findConflictingReservations(11L, fecha, inicio, fin))
                .thenReturn(List.of());

        List<PistaDisponibleDTO> resultado =
                pistaService.buscarPistasDisponibles(TipoDeporte.PADEL, fecha, inicio, fin);

        assertEquals(1, resultado.size());
        assertEquals(11L, resultado.get(0).getPistaId());
        log.info("[TEST] Solo pista libre: id={}", resultado.get(0).getPistaId());
    }

    @Test
    void buscarDisponibles_pistaInactiva_noAparece() {
        log.info("[TEST] PistaService.buscarPistasDisponibles - pista inactiva excluida");
        pistaPadel.setActiva(false);
        LocalDate fecha = LocalDate.now().plusDays(1);
        LocalTime inicio = LocalTime.of(10, 0);
        LocalTime fin = LocalTime.of(11, 0);

        when(pistaRepository.findByTipoDeporte(TipoDeporte.PADEL)).thenReturn(List.of(pistaPadel));

        List<PistaDisponibleDTO> resultado =
                pistaService.buscarPistasDisponibles(TipoDeporte.PADEL, fecha, inicio, fin);

        assertTrue(resultado.isEmpty());
        verify(reservaRepository, never()).findConflictingReservations(anyLong(), any(), any(), any());
        log.info("[TEST] Pista inactiva correctamente excluida");
    }

    @Test
    void buscarDisponibles_fueraDeHorarioPoli_excluida() {
        log.info("[TEST] PistaService.buscarPistasDisponibles - fuera de horario del polideportivo");
        LocalDate fecha = LocalDate.now().plusDays(1);
        LocalTime inicio = LocalTime.of(23, 0); // después del cierre (22:00)
        LocalTime fin = LocalTime.of(23, 59);

        when(pistaRepository.findByTipoDeporte(TipoDeporte.PADEL)).thenReturn(List.of(pistaPadel));

        List<PistaDisponibleDTO> resultado =
                pistaService.buscarPistasDisponibles(TipoDeporte.PADEL, fecha, inicio, fin);

        assertTrue(resultado.isEmpty());
        verify(reservaRepository, never()).findConflictingReservations(anyLong(), any(), any(), any());
        log.info("[TEST] Pista excluida por estar fuera del horario del polideportivo");
    }

    @Test
    void buscarDisponibles_sinPistasDelDeporte_listaVacia() {
        log.info("[TEST] PistaService.buscarPistasDisponibles - sin pistas del deporte");
        when(pistaRepository.findByTipoDeporte(TipoDeporte.TENIS)).thenReturn(List.of());

        List<PistaDisponibleDTO> resultado = pistaService.buscarPistasDisponibles(
                TipoDeporte.TENIS, LocalDate.now().plusDays(1),
                LocalTime.of(10, 0), LocalTime.of(11, 0));

        assertTrue(resultado.isEmpty());
        log.info("[TEST] Lista vacía cuando no hay pistas del deporte");
    }

    @Test
    void buscarDisponibles_horaFinIgualAInicio_lanzaExcepcion() {
        log.info("[TEST] PistaService.buscarPistasDisponibles - horaFin <= horaInicio");
        LocalTime mismaHora = LocalTime.of(10, 0);

        assertThrows(IllegalArgumentException.class,
                () -> pistaService.buscarPistasDisponibles(
                        TipoDeporte.PADEL, LocalDate.now().plusDays(1), mismaHora, mismaHora));
        log.info("[TEST] Excepción correcta cuando horaFin == horaInicio");
    }

    @Test
    void buscarDisponibles_tipoDeporteNull_lanzaNullPointer() {
        log.info("[TEST] PistaService.buscarPistasDisponibles - tipoDeporte null");
        assertThrows(NullPointerException.class,
                () -> pistaService.buscarPistasDisponibles(
                        null, LocalDate.now().plusDays(1),
                        LocalTime.of(10, 0), LocalTime.of(11, 0)));
        log.info("[TEST] NPE lanzado correctamente para tipoDeporte null");
    }

    @Test
    void buscarDisponibles_dtoContieneDatosPolideportivo() {
        log.info("[TEST] PistaService.buscarPistasDisponibles - DTO incluye datos del polideportivo");
        LocalDate fecha = LocalDate.now().plusDays(1);
        LocalTime inicio = LocalTime.of(10, 0);
        LocalTime fin = LocalTime.of(11, 0);

        when(pistaRepository.findByTipoDeporte(TipoDeporte.PADEL)).thenReturn(List.of(pistaPadel));
        when(reservaRepository.findConflictingReservations(10L, fecha, inicio, fin))
                .thenReturn(List.of());

        List<PistaDisponibleDTO> resultado =
                pistaService.buscarPistasDisponibles(TipoDeporte.PADEL, fecha, inicio, fin);

        PistaDisponibleDTO dto = resultado.get(0);
        assertEquals("Polideportivo Central", dto.getPolideportivoNombre());
        assertEquals("Calle Mayor 1", dto.getPolideportivoDireccion());
        assertEquals("08:00", dto.getHoraApertura());
        assertEquals("22:00", dto.getHoraCierre());
        log.info("[TEST] DTO con datos del polideportivo: nombre={}, apertura={}",
                dto.getPolideportivoNombre(), dto.getHoraApertura());
    }
}
