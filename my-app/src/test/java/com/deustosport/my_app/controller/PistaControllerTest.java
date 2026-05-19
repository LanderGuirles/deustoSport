package com.deustosport.my_app.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.deustosport.my_app.dto.PistaDisponibleDTO;
import com.deustosport.my_app.dto.PistaRequest;
import com.deustosport.my_app.dto.PistaResponse;
import com.deustosport.my_app.entity.Pista;
import com.deustosport.my_app.entity.Polideportivo;
import com.deustosport.my_app.enums.TipoDeporte;
import com.deustosport.my_app.service.PistaService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"null", "unchecked"})
class PistaControllerTest {

    private static final Logger log = LoggerFactory.getLogger(PistaControllerTest.class);

    @Mock private PistaService pistaService;
    @InjectMocks private PistaController pistaController;

    private Pista pista;
    private PistaResponse pistaResponse;
    private PistaRequest pistaRequest;

    @BeforeEach
    void setUp() {
        Polideportivo polideportivo = new Polideportivo();
        polideportivo.setId(1L);
        polideportivo.setNombre("Polideportivo Central");

        pista = new Pista();
        pista.setId(10L);
        pista.setNombre("Pista Pádel A");
        pista.setTipoDeporte(TipoDeporte.PADEL);
        pista.setMaxJugadores(4);
        pista.setActiva(true);
        pista.setPolideportivo(polideportivo);

        pistaResponse = new PistaResponse();
        pistaResponse.setId(10L);
        pistaResponse.setNombre("Pista Pádel A");
        pistaResponse.setTipoDeporte(TipoDeporte.PADEL);
        pistaResponse.setMaxJugadores(4);
        pistaResponse.setActiva(true);
        pistaResponse.setPolideportivoId(1L);
        pistaResponse.setPolideportivoNombre("Polideportivo Central");

        pistaRequest = new PistaRequest();
        pistaRequest.setNombre("Pista Pádel A");
        pistaRequest.setTipoDeporte(TipoDeporte.PADEL);
        pistaRequest.setMaxJugadores(4);
        pistaRequest.setPolideportivoId(1L);
        pistaRequest.setActiva(true);
    }

    // ── listarTodas ───────────────────────────────────────────────────────────

    @Test
    void listarTodas_devuelveListaOk() {
        log.info("[TEST] PistaController.listarTodas");
        when(pistaService.obtenerTodasLasPistas()).thenReturn(List.of(pistaResponse));

        ResponseEntity<List<PistaResponse>> resp = pistaController.listarTodas();

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(1, resp.getBody().size());
        assertEquals("Pista Pádel A", resp.getBody().get(0).getNombre());
        log.info("[TEST] Pistas listadas: {}", resp.getBody().size());
    }

    @Test
    void listarTodas_listaVacia_devuelveOk() {
        log.info("[TEST] PistaController.listarTodas - lista vacía");
        when(pistaService.obtenerTodasLasPistas()).thenReturn(List.of());

        ResponseEntity<List<PistaResponse>> resp = pistaController.listarTodas();

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertTrue(resp.getBody().isEmpty());
        log.info("[TEST] Lista vacía devuelta correctamente");
    }

    // ── obtenerPorId ──────────────────────────────────────────────────────────

    @Test
    void obtenerPorId_existente_devuelveOk() {
        log.info("[TEST] PistaController.obtenerPorId - id=10 existe");
        when(pistaService.obtenerPistaPorId(10L)).thenReturn(pistaResponse);

        ResponseEntity<?> resp = pistaController.obtenerPorId(10L);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        PistaResponse body = (PistaResponse) resp.getBody();
        assertEquals(10L, body.getId());
        assertEquals(TipoDeporte.PADEL, body.getTipoDeporte());
        log.info("[TEST] Pista encontrada: {}", body.getNombre());
    }

    @Test
    void obtenerPorId_noExistente_devuelveNotFound() {
        log.info("[TEST] PistaController.obtenerPorId - id=999 no existe");
        when(pistaService.obtenerPistaPorId(999L))
                .thenThrow(new RuntimeException("La pista con ID 999 no existe."));

        ResponseEntity<?> resp = pistaController.obtenerPorId(999L);

        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
        assertTrue(resp.getBody().toString().contains("999"));
        log.info("[TEST] 404 correcto para pista inexistente");
    }

    // ── crearPista ────────────────────────────────────────────────────────────

    @Test
    void crearPista_datosValidos_devuelveCreated() {
        log.info("[TEST] PistaController.crearPista - datos válidos");
        when(pistaService.registrarNuevaPista(any(Pista.class))).thenReturn(pista);

        ResponseEntity<?> resp = pistaController.crearPista(pistaRequest);

        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        Pista body = (Pista) resp.getBody();
        assertEquals("Pista Pádel A", body.getNombre());
        log.info("[TEST] Pista creada: {}, status={}", body.getNombre(), resp.getStatusCode());
    }

    @Test
    void crearPista_nombreDuplicado_devuelveBadRequest() {
        log.info("[TEST] PistaController.crearPista - nombre duplicado");
        when(pistaService.registrarNuevaPista(any()))
                .thenThrow(new RuntimeException("Ya existe una pista con ese nombre."));

        ResponseEntity<?> resp = pistaController.crearPista(pistaRequest);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertTrue(resp.getBody().toString().contains("nombre"));
        log.info("[TEST] Error nombre duplicado: {}", resp.getBody());
    }

    @Test
    void crearPista_polideportivoInexistente_devuelveBadRequest() {
        log.info("[TEST] PistaController.crearPista - polideportivo no existe");
        when(pistaService.registrarNuevaPista(any()))
                .thenThrow(new RuntimeException("Error: No se encontró el polideportivo con ID: 99"));

        ResponseEntity<?> resp = pistaController.crearPista(pistaRequest);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        log.info("[TEST] Error polideportivo inexistente: {}", resp.getBody());
    }

    // ── actualizarPista ───────────────────────────────────────────────────────

    @Test
    void actualizarPista_datosValidos_devuelveOk() {
        log.info("[TEST] PistaController.actualizarPista - actualización correcta");
        pistaResponse.setNombre("Pista Pádel Actualizada");
        when(pistaService.actualizarPista(eq(10L), any(PistaRequest.class))).thenReturn(pistaResponse);

        ResponseEntity<?> resp = pistaController.actualizarPista(10L, pistaRequest);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        PistaResponse body = (PistaResponse) resp.getBody();
        assertEquals("Pista Pádel Actualizada", body.getNombre());
        log.info("[TEST] Pista actualizada: {}", body.getNombre());
    }

    @Test
    void actualizarPista_noExistente_devuelveBadRequest() {
        log.info("[TEST] PistaController.actualizarPista - pista no existe");
        when(pistaService.actualizarPista(eq(999L), any()))
                .thenThrow(new RuntimeException("No se puede modificar: La pista con ID 999 no existe."));

        ResponseEntity<?> resp = pistaController.actualizarPista(999L, pistaRequest);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        log.info("[TEST] Error actualización pista inexistente");
    }

    // ── eliminarPista ─────────────────────────────────────────────────────────

    @Test
    void eliminarPista_existente_devuelveOk() {
        log.info("[TEST] PistaController.eliminarPista - id=10");
        doNothing().when(pistaService).eliminarPista(10L);

        ResponseEntity<?> resp = pistaController.eliminarPista(10L);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertTrue(resp.getBody().toString().contains("10"));
        verify(pistaService).eliminarPista(10L);
        log.info("[TEST] Pista 10 eliminada correctamente");
    }

    @Test
    void eliminarPista_noExistente_devuelveBadRequest() {
        log.info("[TEST] PistaController.eliminarPista - pista no existe");
        doThrow(new RuntimeException("No se puede modificar: La pista con ID 999 no existe."))
                .when(pistaService).eliminarPista(999L);

        ResponseEntity<?> resp = pistaController.eliminarPista(999L);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        log.info("[TEST] Error eliminación pista inexistente");
    }

    @Test
    void eliminarPista_conReservas_devuelveBadRequest() {
        log.info("[TEST] PistaController.eliminarPista - con reservas asociadas");
        doThrow(new RuntimeException("Error al eliminar la pista: Es posible que tenga reservas asociadas."))
                .when(pistaService).eliminarPista(10L);

        ResponseEntity<?> resp = pistaController.eliminarPista(10L);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertTrue(resp.getBody().toString().contains("reservas"));
        log.info("[TEST] Error eliminación con reservas: {}", resp.getBody());
    }

    // ── bloquearPista ─────────────────────────────────────────────────────────

    @Test
    void bloquearPista_activaABloqueada_devuelveOk() {
        log.info("[TEST] PistaController.bloquearPista - activa → bloqueada");
        pistaResponse.setActiva(false);
        when(pistaService.bloquearPista(10L)).thenReturn(pistaResponse);

        ResponseEntity<?> resp = pistaController.bloquearPista(10L);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        PistaResponse body = (PistaResponse) resp.getBody();
        assertFalse(body.isActiva());
        log.info("[TEST] Pista bloqueada: activa={}", body.isActiva());
    }

    @Test
    void bloquearPista_bloqueadaAActiva_devuelveOk() {
        log.info("[TEST] PistaController.bloquearPista - bloqueada → activa");
        pistaResponse.setActiva(true);
        when(pistaService.bloquearPista(10L)).thenReturn(pistaResponse);

        ResponseEntity<?> resp = pistaController.bloquearPista(10L);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        PistaResponse body = (PistaResponse) resp.getBody();
        assertTrue(body.isActiva());
        log.info("[TEST] Pista desbloqueada: activa={}", body.isActiva());
    }

    @Test
    void bloquearPista_noExistente_devuelveBadRequest() {
        log.info("[TEST] PistaController.bloquearPista - pista no existe");
        when(pistaService.bloquearPista(999L))
                .thenThrow(new RuntimeException("No se puede modificar: La pista con ID 999 no existe."));

        ResponseEntity<?> resp = pistaController.bloquearPista(999L);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        log.info("[TEST] Error bloquear pista inexistente");
    }

    // ── buscarDisponibles (HU5) ───────────────────────────────────────────────

    @Test
    void buscarDisponibles_deporeFechaHora_devuelveOk() {
        log.info("[TEST] PistaController.buscarDisponibles - PADEL mañana 10:00 60min");
        PistaDisponibleDTO dto = new PistaDisponibleDTO();
        dto.setPistaId(10L);
        dto.setPistaNombre("Pista Pádel 1");
        dto.setTipoDeporte(TipoDeporte.PADEL);
        dto.setPolideportivoNombre("Polideportivo Central");

        when(pistaService.buscarPistasDisponibles(
                eq(TipoDeporte.PADEL), any(LocalDate.class),
                eq(LocalTime.of(10, 0)), eq(LocalTime.of(11, 0))))
                .thenReturn(List.of(dto));

        LocalDate fecha = LocalDate.now().plusDays(1);
        ResponseEntity<?> resp = pistaController.buscarDisponibles(
                TipoDeporte.PADEL, fecha, LocalTime.of(10, 0), 60);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        List<PistaDisponibleDTO> body = (List<PistaDisponibleDTO>) resp.getBody();
        assertEquals(1, body.size());
        assertEquals("Pista Pádel 1", body.get(0).getPistaNombre());
        log.info("[TEST] Pistas disponibles: {}", body.size());
    }

    @Test
    void buscarDisponibles_sinPistasLibres_devuelveListaVacia() {
        log.info("[TEST] PistaController.buscarDisponibles - sin pistas disponibles");
        when(pistaService.buscarPistasDisponibles(any(), any(), any(), any()))
                .thenReturn(List.of());

        ResponseEntity<?> resp = pistaController.buscarDisponibles(
                TipoDeporte.TENIS, LocalDate.now().plusDays(1), LocalTime.of(9, 0), 90);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertTrue(((List<?>) resp.getBody()).isEmpty());
        log.info("[TEST] Lista vacía devuelta correctamente");
    }

    @Test
    void buscarDisponibles_duracionInvalida_devuelveBadRequest() {
        log.info("[TEST] PistaController.buscarDisponibles - duración 0 invalida");
        ResponseEntity<?> resp = pistaController.buscarDisponibles(
                TipoDeporte.PADEL, LocalDate.now().plusDays(1), LocalTime.of(10, 0), 0);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) resp.getBody();
        assertTrue(body.get("error").toString().contains("duración"));
        log.info("[TEST] Error duración inválida: {}", body.get("error"));
    }

    @Test
    void buscarDisponibles_duracionExcesiva_devuelveBadRequest() {
        log.info("[TEST] PistaController.buscarDisponibles - duración 500 excede límite");
        ResponseEntity<?> resp = pistaController.buscarDisponibles(
                TipoDeporte.PADEL, LocalDate.now().plusDays(1), LocalTime.of(10, 0), 500);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        log.info("[TEST] Error duración excesiva controlado");
    }

    @Test
    void buscarDisponibles_servicioLanzaExcepcion_devuelveBadRequest() {
        log.info("[TEST] PistaController.buscarDisponibles - servicio lanza excepción");
        when(pistaService.buscarPistasDisponibles(any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("La hora de fin debe ser posterior a la hora de inicio."));

        ResponseEntity<?> resp = pistaController.buscarDisponibles(
                TipoDeporte.PADEL, LocalDate.now().plusDays(1), LocalTime.of(10, 0), 60);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        log.info("[TEST] Error de servicio propagado correctamente");
    }
}
