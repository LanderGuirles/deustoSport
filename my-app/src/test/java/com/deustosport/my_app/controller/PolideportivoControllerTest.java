package com.deustosport.my_app.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.deustosport.my_app.dto.DisponibilidadPistaDTO;
import com.deustosport.my_app.dto.DisponibilidadPolideportivoDTO;
import com.deustosport.my_app.dto.HorarioPolideportivoRequest;
import com.deustosport.my_app.dto.ResumenSemanalPolideportivoDTO;
import com.deustosport.my_app.entity.Ayuntamiento;
import com.deustosport.my_app.entity.Polideportivo;
import com.deustosport.my_app.entity.Pista;
import com.deustosport.my_app.enums.TipoDeporte;
import com.deustosport.my_app.repository.AyuntamientoRepository;
import com.deustosport.my_app.service.PolideportivoService;

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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"null", "unchecked"})
class PolideportivoControllerTest {

    private static final Logger log = LoggerFactory.getLogger(PolideportivoControllerTest.class);

    @Mock private PolideportivoService polideportivoService;
    @Mock private AyuntamientoRepository ayuntamientoRepository;

    @InjectMocks private PolideportivoController polideportivoController;

    private Polideportivo polideportivo;
    private Ayuntamiento ayuntamiento;

    @BeforeEach
    void setUp() {
        ayuntamiento = new Ayuntamiento();
        ayuntamiento.setId(1L);
        ayuntamiento.setNombre("Ayuntamiento Bilbao");

        polideportivo = new Polideportivo();
        polideportivo.setId(10L);
        polideportivo.setNombre("Polideportivo Deusto");
        polideportivo.setDireccion("Calle Deusto 5");
        polideportivo.setHoraApertura(LocalTime.of(8, 0));
        polideportivo.setHoraCierre(LocalTime.of(22, 0));
        polideportivo.setAyuntamiento(ayuntamiento);
        polideportivo.setPistas(new ArrayList<>());
    }

    // ── listarPolideportivos ──────────────────────────────────────────────────

    @Test
    void listarPolideportivos_devuelveListaOk() {
        log.info("[TEST] PolideportivoController.listarPolideportivos");
        when(polideportivoService.obtenerTodos()).thenReturn(List.of(polideportivo));

        ResponseEntity<List<Polideportivo>> resp = polideportivoController.listarPolideportivos();

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(1, resp.getBody().size());
        log.info("[TEST] Polideportivos listados: {}", resp.getBody().size());
    }

    @Test
    void listarPolideportivos_listaVacia_devuelveOkVacio() {
        log.info("[TEST] PolideportivoController.listarPolideportivos - lista vacía");
        when(polideportivoService.obtenerTodos()).thenReturn(List.of());

        ResponseEntity<List<Polideportivo>> resp = polideportivoController.listarPolideportivos();

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertTrue(resp.getBody().isEmpty());
    }

    // ── listarPorAyuntamiento ─────────────────────────────────────────────────

    @Test
    void listarPorAyuntamiento_devuelveLista() {
        log.info("[TEST] PolideportivoController.listarPorAyuntamiento - ayuntamientoId=1");
        when(polideportivoService.obtenerPorAyuntamiento(1L)).thenReturn(List.of(polideportivo));

        ResponseEntity<?> resp = polideportivoController.listarPorAyuntamiento(1L);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        List<Polideportivo> body = (List<Polideportivo>) resp.getBody();
        assertEquals(1, body.size());
        log.info("[TEST] Polideportivos por ayuntamiento: {}", body.size());
    }

    // ── obtenerPorId ──────────────────────────────────────────────────────────

    @Test
    void obtenerPorId_existente_devuelveOk() {
        log.info("[TEST] PolideportivoController.obtenerPorId - id=10 existe");
        when(polideportivoService.obtenerPorId(10L)).thenReturn(polideportivo);

        ResponseEntity<?> resp = polideportivoController.obtenerPorId(10L);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(polideportivo, resp.getBody());
        log.info("[TEST] Polideportivo encontrado: {}", ((Polideportivo)resp.getBody()).getNombre());
    }

    @Test
    void obtenerPorId_noExistente_devuelveBadRequest() {
        log.info("[TEST] PolideportivoController.obtenerPorId - id=999 no existe");
        when(polideportivoService.obtenerPorId(999L))
                .thenThrow(new IllegalArgumentException("Polideportivo no encontrado con ID: 999"));

        ResponseEntity<?> resp = polideportivoController.obtenerPorId(999L);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) resp.getBody();
        assertTrue(body.get("error").toString().contains("999"));
        log.info("[TEST] Error correcto para id inexistente: {}", body.get("error"));
    }

    // ── crearPolideportivo ────────────────────────────────────────────────────

    @Test
    void crearPolideportivo_sinAyuntamientoId_devuelveBadRequest() {
        log.info("[TEST] PolideportivoController.crearPolideportivo - sin ayuntamientoId");
        Map<String, Object> payload = new HashMap<>();
        payload.put("nombre", "Poli Test");
        payload.put("direccion", "Calle Test 1");

        ResponseEntity<?> resp = polideportivoController.crearPolideportivo(payload);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) resp.getBody();
        assertTrue(body.get("error").toString().contains("ayuntamientoId"));
        log.info("[TEST] Validación ayuntamientoId ausente ok: {}", body.get("error"));
    }

    @Test
    void crearPolideportivo_ayuntamientoNoExiste_devuelveBadRequest() {
        log.info("[TEST] PolideportivoController.crearPolideportivo - ayuntamiento 999 no existe");
        Map<String, Object> payload = new HashMap<>();
        payload.put("nombre", "Poli Test");
        payload.put("direccion", "Calle Test 1");
        payload.put("ayuntamientoId", 999);

        when(ayuntamientoRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseEntity<?> resp = polideportivoController.crearPolideportivo(payload);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        log.info("[TEST] Error correcto para ayuntamiento inexistente");
    }

    @Test
    void crearPolideportivo_datosValidos_devuelveOk() {
        log.info("[TEST] PolideportivoController.crearPolideportivo - datos válidos");
        Map<String, Object> payload = new HashMap<>();
        payload.put("nombre", "Poli Test");
        payload.put("direccion", "Calle Test 1");
        payload.put("ayuntamientoId", 1);

        when(ayuntamientoRepository.findById(1L)).thenReturn(Optional.of(ayuntamiento));
        when(polideportivoService.crearPolideportivo(any())).thenReturn(polideportivo);

        ResponseEntity<?> resp = polideportivoController.crearPolideportivo(payload);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        log.info("[TEST] Polideportivo creado correctamente");
    }

    // ── actualizarPolideportivo ───────────────────────────────────────────────

    @Test
    void actualizarPolideportivo_datosValidos_devuelveOk() {
        log.info("[TEST] PolideportivoController.actualizarPolideportivo - actualización correcta");
        Map<String, String> payload = new HashMap<>();
        payload.put("nombre", "Nuevo Nombre");
        payload.put("direccion", "Nueva Calle 2");

        when(polideportivoService.actualizarPolideportivo(10L, "Nuevo Nombre", "Nueva Calle 2"))
                .thenReturn(polideportivo);

        ResponseEntity<?> resp = polideportivoController.actualizarPolideportivo(10L, payload);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        log.info("[TEST] Polideportivo actualizado: {}", resp.getStatusCode());
    }

    @Test
    void actualizarPolideportivo_noExistente_devuelveBadRequest() {
        log.info("[TEST] PolideportivoController.actualizarPolideportivo - id inexistente");
        Map<String, String> payload = new HashMap<>();
        payload.put("nombre", "Nombre");
        payload.put("direccion", "Calle");

        when(polideportivoService.actualizarPolideportivo(eq(999L), any(), any()))
                .thenThrow(new IllegalArgumentException("Polideportivo no encontrado"));

        ResponseEntity<?> resp = polideportivoController.actualizarPolideportivo(999L, payload);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        log.info("[TEST] Error correcto para actualizar inexistente");
    }

    // ── listarPistas ──────────────────────────────────────────────────────────

    @Test
    void listarPistas_devuelveLista() {
        log.info("[TEST] PolideportivoController.listarPistas - polideportivoId=10");
        Pista pista = new Pista();
        pista.setId(1L);
        pista.setNombre("Pista 1");
        pista.setTipoDeporte(TipoDeporte.PADEL);
        when(polideportivoService.obtenerPistasByPolideportivo(10L)).thenReturn(List.of(pista));

        ResponseEntity<List<Pista>> resp = polideportivoController.listarPistas(10L);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(1, resp.getBody().size());
        log.info("[TEST] Pistas del polideportivo: {}", resp.getBody().size());
    }

    // ── actualizarHorario ────────────────────────────────────────────────────

    @Test
    void actualizarHorario_horarioValido_devuelveOk() {
        log.info("[TEST] PolideportivoController.actualizarHorario - 08:00-22:00");
        HorarioPolideportivoRequest req = new HorarioPolideportivoRequest();
        req.setHoraApertura(LocalTime.of(8, 0));
        req.setHoraCierre(LocalTime.of(22, 0));

        when(polideportivoService.actualizarHorarioGeneral(10L, LocalTime.of(8, 0), LocalTime.of(22, 0)))
                .thenReturn(polideportivo);

        ResponseEntity<?> resp = polideportivoController.actualizarHorario(10L, req);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        log.info("[TEST] Horario actualizado: {}", resp.getStatusCode());
    }

    @Test
    void actualizarHorario_cierreAnteriorApertura_devuelveBadRequest() {
        log.info("[TEST] PolideportivoController.actualizarHorario - cierre < apertura");
        HorarioPolideportivoRequest req = new HorarioPolideportivoRequest();
        req.setHoraApertura(LocalTime.of(20, 0));
        req.setHoraCierre(LocalTime.of(8, 0));

        when(polideportivoService.actualizarHorarioGeneral(10L, LocalTime.of(20, 0), LocalTime.of(8, 0)))
                .thenThrow(new IllegalArgumentException("La hora de cierre debe ser posterior a la hora de apertura."));

        ResponseEntity<?> resp = polideportivoController.actualizarHorario(10L, req);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        log.info("[TEST] Error horario incorrecto: {}", ((Map<?,?>)resp.getBody()).get("error"));
    }

    // ── obtenerDisponibilidad (HU2) ───────────────────────────────────────────

    @Test
    void obtenerDisponibilidad_devuelveDto() {
        log.info("[TEST] PolideportivoController.obtenerDisponibilidad - polideportivoId=10");
        LocalDate fecha = LocalDate.now().plusDays(1);

        DisponibilidadPolideportivoDTO dto = new DisponibilidadPolideportivoDTO();
        dto.setPolideportivoId(10L);
        dto.setPolideportivoNombre("Polideportivo Deusto");
        dto.setFecha(fecha);
        dto.setTotalPistas(2);
        dto.setPistasActivas(2);
        dto.setPistas(List.of());

        when(polideportivoService.obtenerDisponibilidadPolideportivo(10L, fecha)).thenReturn(dto);

        ResponseEntity<?> resp = polideportivoController.obtenerDisponibilidad(10L, fecha);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        DisponibilidadPolideportivoDTO body = (DisponibilidadPolideportivoDTO) resp.getBody();
        assertEquals(10L, body.getPolideportivoId());
        assertEquals(2, body.getTotalPistas());
        log.info("[TEST] Disponibilidad devuelta: totalPistas={}", body.getTotalPistas());
    }

    @Test
    void obtenerDisponibilidad_polideportivoNoExistente_devuelveBadRequest() {
        log.info("[TEST] PolideportivoController.obtenerDisponibilidad - polideportivo no existe");
        LocalDate fecha = LocalDate.now().plusDays(1);

        when(polideportivoService.obtenerDisponibilidadPolideportivo(999L, fecha))
                .thenThrow(new IllegalArgumentException("Polideportivo no encontrado con ID: 999"));

        ResponseEntity<?> resp = polideportivoController.obtenerDisponibilidad(999L, fecha);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) resp.getBody();
        assertTrue(body.get("error").toString().contains("999"));
        log.info("[TEST] Error correcto para disponibilidad de polideportivo inexistente");
    }

    // ── eliminarPolideportivo (HU1) ───────────────────────────────────────────

    @Test
    void eliminarPolideportivo_exitoso_devuelveNoContent() {
        log.info("[TEST] PolideportivoController.eliminarPolideportivo - eliminación correcta");
        doNothing().when(polideportivoService).eliminarPolideportivo(10L);

        ResponseEntity<?> resp = polideportivoController.eliminarPolideportivo(10L);

        assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
        verify(polideportivoService).eliminarPolideportivo(10L);
        log.info("[TEST] Polideportivo eliminado, status={}", resp.getStatusCode());
    }

    @Test
    void eliminarPolideportivo_conReservasActivas_devuelveConflict() {
        log.info("[TEST] PolideportivoController.eliminarPolideportivo - con reservas activas");
        doThrow(new IllegalStateException("No se puede eliminar el polideportivo porque tiene 3 reserva(s) activa(s)."))
                .when(polideportivoService).eliminarPolideportivo(10L);

        ResponseEntity<?> resp = polideportivoController.eliminarPolideportivo(10L);

        assertEquals(HttpStatus.CONFLICT, resp.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) resp.getBody();
        assertTrue(body.get("error").toString().contains("3"));
        log.info("[TEST] Conflicto correcto al eliminar con reservas: {}", body.get("error"));
    }

    @Test
    void eliminarPolideportivo_noExistente_devuelveBadRequest() {
        log.info("[TEST] PolideportivoController.eliminarPolideportivo - polideportivo no existe");
        doThrow(new IllegalArgumentException("Polideportivo no encontrado con ID: 999"))
                .when(polideportivoService).eliminarPolideportivo(999L);

        ResponseEntity<?> resp = polideportivoController.eliminarPolideportivo(999L);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        log.info("[TEST] Error correcto al eliminar inexistente");
    }

    // ── obtenerResumenSemanal (HU4) ───────────────────────────────────────────

    @Test
    void obtenerResumenSemanal_devuelveOk() {
        log.info("[TEST] PolideportivoController.obtenerResumenSemanal - polideportivoId=10");
        ResumenSemanalPolideportivoDTO dto = new ResumenSemanalPolideportivoDTO();
        dto.setPolideportivoId(10L);
        dto.setPolideportivoNombre("Polideportivo Deusto");
        dto.setTotalPistas(3);
        dto.setPistasActivas(3);
        dto.setTotalReservasSemana(15L);
        dto.setDetallesPorPista(List.of());

        when(polideportivoService.obtenerResumenSemanal(10L)).thenReturn(dto);

        ResponseEntity<?> resp = polideportivoController.obtenerResumenSemanal(10L);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        ResumenSemanalPolideportivoDTO body = (ResumenSemanalPolideportivoDTO) resp.getBody();
        assertEquals(15L, body.getTotalReservasSemana());
        assertEquals(3, body.getTotalPistas());
        log.info("[TEST] Resumen semanal devuelto: totalReservas={}", body.getTotalReservasSemana());
    }

    @Test
    void obtenerResumenSemanal_polideportivoNoExiste_devuelveBadRequest() {
        log.info("[TEST] PolideportivoController.obtenerResumenSemanal - polideportivo no existe");
        when(polideportivoService.obtenerResumenSemanal(999L))
                .thenThrow(new IllegalArgumentException("Polideportivo no encontrado con ID: 999"));

        ResponseEntity<?> resp = polideportivoController.obtenerResumenSemanal(999L);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) resp.getBody();
        assertTrue(body.get("error").toString().contains("999"));
        log.info("[TEST] Error correcto al pedir resumen de polideportivo inexistente");
    }
}
