package com.deustosport.my_app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.deustosport.my_app.dto.AvisoDelDiaRequest;
import com.deustosport.my_app.dto.AvisoDelDiaResponse;
import com.deustosport.my_app.entity.AvisoDelDia;
import com.deustosport.my_app.enums.TipoAviso;
import com.deustosport.my_app.repository.AvisoDelDiaRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class AvisoDelDiaServiceTest {

    private static final Logger log = LoggerFactory.getLogger(AvisoDelDiaServiceTest.class);

    @Mock private AvisoDelDiaRepository avisoRepository;
    @InjectMocks private AvisoDelDiaService avisoService;

    private AvisoDelDia avisoEntity;
    private AvisoDelDiaRequest requestValido;

    @BeforeEach
    void setUp() {
        avisoEntity = new AvisoDelDia();
        avisoEntity.setId(1L);
        avisoEntity.setTitulo("Mantenimiento piscina");
        avisoEntity.setMensaje("La piscina estará cerrada el viernes para mantenimiento.");
        avisoEntity.setTipo(TipoAviso.AVISO);
        avisoEntity.setActivo(true);
        avisoEntity.setFechaCreacion(LocalDateTime.now());
        avisoEntity.setFechaExpiracion(LocalDate.now().plusDays(5));
        avisoEntity.setPrioridad(3);
        avisoEntity.setCreadoPorNombre("Secretaria Test");
        avisoEntity.setCreadoPorId(99L);

        requestValido = new AvisoDelDiaRequest();
        requestValido.setTitulo("Mantenimiento piscina");
        requestValido.setMensaje("La piscina estará cerrada el viernes para mantenimiento.");
        requestValido.setTipo(TipoAviso.AVISO);
        requestValido.setPrioridad(3);
        requestValido.setCreadoPorNombre("Secretaria Test");
        requestValido.setCreadoPorId(99L);
        requestValido.setFechaExpiracion(LocalDate.now().plusDays(5));
    }

    // ── publicarAviso ─────────────────────────────────────────────────────────

    @Test
    void publicarAviso_datosValidos_persisteYDevuelveDto() {
        log.info("[TEST] AvisoDelDiaService.publicarAviso - datos válidos");
        when(avisoRepository.save(any())).thenReturn(avisoEntity);

        AvisoDelDiaResponse resp = avisoService.publicarAviso(requestValido);

        assertNotNull(resp);
        assertEquals("Mantenimiento piscina", resp.getTitulo());
        assertEquals(TipoAviso.AVISO, resp.getTipo());
        assertTrue(resp.isActivo());
        verify(avisoRepository).save(any(AvisoDelDia.class));
        log.info("[TEST] Aviso publicado: id={}, titulo={}", resp.getId(), resp.getTitulo());
    }

    @Test
    void publicarAviso_sinTipo_asignaAvisoPorDefecto() {
        log.info("[TEST] AvisoDelDiaService.publicarAviso - tipo null → AVISO por defecto");
        requestValido.setTipo(null);
        when(avisoRepository.save(any())).thenAnswer(inv -> {
            AvisoDelDia a = inv.getArgument(0);
            a.setId(2L);
            return a;
        });

        AvisoDelDiaResponse resp = avisoService.publicarAviso(requestValido);

        assertNotNull(resp);
        assertEquals(TipoAviso.AVISO, resp.getTipo());
        log.info("[TEST] Tipo por defecto asignado: {}", resp.getTipo());
    }

    @Test
    void publicarAviso_sinPrioridad_asignaCero() {
        log.info("[TEST] AvisoDelDiaService.publicarAviso - prioridad null → 0");
        requestValido.setPrioridad(null);
        when(avisoRepository.save(any())).thenAnswer(inv -> {
            AvisoDelDia a = inv.getArgument(0);
            a.setId(3L);
            a.setPrioridad(0);
            return a;
        });

        AvisoDelDiaResponse resp = avisoService.publicarAviso(requestValido);

        assertEquals(0, resp.getPrioridad());
        log.info("[TEST] Prioridad por defecto: {}", resp.getPrioridad());
    }

    @Test
    void publicarAviso_tituloVacio_lanzaExcepcion() {
        log.info("[TEST] AvisoDelDiaService.publicarAviso - título vacío");
        requestValido.setTitulo("");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> avisoService.publicarAviso(requestValido));
        assertTrue(ex.getMessage().contains("título"));
        verify(avisoRepository, never()).save(any());
        log.info("[TEST] Excepción título vacío: {}", ex.getMessage());
    }

    @Test
    void publicarAviso_tituloNull_lanzaExcepcion() {
        log.info("[TEST] AvisoDelDiaService.publicarAviso - título null");
        requestValido.setTitulo(null);

        assertThrows(IllegalArgumentException.class, () -> avisoService.publicarAviso(requestValido));
        verify(avisoRepository, never()).save(any());
        log.info("[TEST] Excepción título null lanzada correctamente");
    }

    @Test
    void publicarAviso_mensajeVacio_lanzaExcepcion() {
        log.info("[TEST] AvisoDelDiaService.publicarAviso - mensaje vacío");
        requestValido.setMensaje("   ");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> avisoService.publicarAviso(requestValido));
        assertTrue(ex.getMessage().contains("mensaje"));
        log.info("[TEST] Excepción mensaje vacío: {}", ex.getMessage());
    }

    @Test
    void publicarAviso_prioridadNegativa_lanzaExcepcion() {
        log.info("[TEST] AvisoDelDiaService.publicarAviso - prioridad negativa");
        requestValido.setPrioridad(-1);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> avisoService.publicarAviso(requestValido));
        assertTrue(ex.getMessage().contains("prioridad"));
        log.info("[TEST] Excepción prioridad negativa: {}", ex.getMessage());
    }

    @Test
    void publicarAviso_prioridadMayorQue10_lanzaExcepcion() {
        log.info("[TEST] AvisoDelDiaService.publicarAviso - prioridad > 10");
        requestValido.setPrioridad(11);

        assertThrows(IllegalArgumentException.class, () -> avisoService.publicarAviso(requestValido));
        log.info("[TEST] Excepción prioridad > 10 lanzada correctamente");
    }

    // ── obtenerAvisoPrincipal ─────────────────────────────────────────────────

    @Test
    void obtenerAvisoPrincipal_hayAviso_devuelveOptional() {
        log.info("[TEST] AvisoDelDiaService.obtenerAvisoPrincipal - hay aviso activo");
        when(avisoRepository.findAvisoPrincipal(any())).thenReturn(Optional.of(avisoEntity));

        Optional<AvisoDelDiaResponse> resultado = avisoService.obtenerAvisoPrincipal();

        assertTrue(resultado.isPresent());
        assertEquals("Mantenimiento piscina", resultado.get().getTitulo());
        log.info("[TEST] Aviso principal: {}", resultado.get().getTitulo());
    }

    @Test
    void obtenerAvisoPrincipal_noHayAviso_devuelveVacio() {
        log.info("[TEST] AvisoDelDiaService.obtenerAvisoPrincipal - no hay avisos");
        when(avisoRepository.findAvisoPrincipal(any())).thenReturn(Optional.empty());

        Optional<AvisoDelDiaResponse> resultado = avisoService.obtenerAvisoPrincipal();

        assertFalse(resultado.isPresent());
        log.info("[TEST] Sin aviso principal: Optional vacío");
    }

    // ── listarAvisosActivosParaUsuarios ───────────────────────────────────────

    @Test
    void listarAvisosActivosParaUsuarios_devuelveLista() {
        log.info("[TEST] AvisoDelDiaService.listarAvisosActivosParaUsuarios");
        when(avisoRepository.findAvisosActivosVigentes(any())).thenReturn(List.of(avisoEntity));

        List<AvisoDelDiaResponse> resultado = avisoService.listarAvisosActivosParaUsuarios();

        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).isActivo());
        log.info("[TEST] Avisos activos para usuarios: {}", resultado.size());
    }

    // ── listarTodosLosAvisos ──────────────────────────────────────────────────

    @Test
    void listarTodosLosAvisos_devuelveHistorial() {
        log.info("[TEST] AvisoDelDiaService.listarTodosLosAvisos");
        AvisoDelDia avisoInactivo = new AvisoDelDia();
        avisoInactivo.setId(2L);
        avisoInactivo.setTitulo("Aviso antiguo");
        avisoInactivo.setMensaje("Mensaje antiguo");
        avisoInactivo.setTipo(TipoAviso.INFO);
        avisoInactivo.setActivo(false);
        avisoInactivo.setFechaCreacion(LocalDateTime.now().minusDays(10));
        avisoInactivo.setPrioridad(0);

        when(avisoRepository.findAllByOrderByFechaCreacionDesc())
                .thenReturn(List.of(avisoEntity, avisoInactivo));

        List<AvisoDelDiaResponse> resultado = avisoService.listarTodosLosAvisos();

        assertEquals(2, resultado.size());
        log.info("[TEST] Total avisos en historial: {}", resultado.size());
    }

    // ── activar/desactivar ────────────────────────────────────────────────────

    @Test
    void activarAviso_activaCorrectamente() {
        log.info("[TEST] AvisoDelDiaService.activarAviso - aviso desactivado → activado");
        avisoEntity.setActivo(false);
        when(avisoRepository.findById(1L)).thenReturn(Optional.of(avisoEntity));
        when(avisoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AvisoDelDiaResponse resp = avisoService.activarAviso(1L);

        assertTrue(resp.isActivo());
        log.info("[TEST] Aviso activado: activo={}", resp.isActivo());
    }

    @Test
    void desactivarAviso_desactivaCorrectamente() {
        log.info("[TEST] AvisoDelDiaService.desactivarAviso - aviso activo → desactivado");
        when(avisoRepository.findById(1L)).thenReturn(Optional.of(avisoEntity));
        when(avisoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AvisoDelDiaResponse resp = avisoService.desactivarAviso(1L);

        assertFalse(resp.isActivo());
        log.info("[TEST] Aviso desactivado: activo={}", resp.isActivo());
    }

    @Test
    void activarAviso_noExiste_lanzaExcepcion() {
        log.info("[TEST] AvisoDelDiaService.activarAviso - aviso 999 no existe");
        when(avisoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> avisoService.activarAviso(999L));
        log.info("[TEST] Excepción al activar aviso inexistente");
    }

    // ── eliminarAviso ─────────────────────────────────────────────────────────

    @Test
    void eliminarAviso_existente_eliminaCorrectamente() {
        log.info("[TEST] AvisoDelDiaService.eliminarAviso - id=1");
        when(avisoRepository.existsById(1L)).thenReturn(true);
        doNothing().when(avisoRepository).deleteById(1L);

        assertDoesNotThrow(() -> avisoService.eliminarAviso(1L));
        verify(avisoRepository).deleteById(1L);
        log.info("[TEST] Aviso 1 eliminado correctamente");
    }

    @Test
    void eliminarAviso_noExiste_lanzaExcepcion() {
        log.info("[TEST] AvisoDelDiaService.eliminarAviso - aviso 999 no existe");
        when(avisoRepository.existsById(999L)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> avisoService.eliminarAviso(999L));
        assertTrue(ex.getMessage().contains("999"));
        verify(avisoRepository, never()).deleteById(any());
        log.info("[TEST] Excepción al eliminar aviso inexistente: {}", ex.getMessage());
    }

    // ── limpiarAvisosExpirados ────────────────────────────────────────────────

    @Test
    void limpiarAvisosExpirados_devuelveNumeroDesactivados() {
        log.info("[TEST] AvisoDelDiaService.limpiarAvisosExpirados - 3 avisos desactivados");
        when(avisoRepository.desactivarAvisosExpirados(any())).thenReturn(3);

        int resultado = avisoService.limpiarAvisosExpirados();

        assertEquals(3, resultado);
        verify(avisoRepository).desactivarAvisosExpirados(any(LocalDate.class));
        log.info("[TEST] Avisos expirados limpiados: {}", resultado);
    }

    @Test
    void limpiarAvisosExpirados_sinExpirados_devuelveCero() {
        log.info("[TEST] AvisoDelDiaService.limpiarAvisosExpirados - sin expirados");
        when(avisoRepository.desactivarAvisosExpirados(any())).thenReturn(0);

        int resultado = avisoService.limpiarAvisosExpirados();

        assertEquals(0, resultado);
        log.info("[TEST] Sin avisos expirados que limpiar");
    }

    // ── contarAvisosActivosVigentes ───────────────────────────────────────────

    @Test
    void contarAvisosActivosVigentes_devuelveConteo() {
        log.info("[TEST] AvisoDelDiaService.contarAvisosActivosVigentes");
        when(avisoRepository.countAvisosActivosVigentes(any())).thenReturn(5L);

        long resultado = avisoService.contarAvisosActivosVigentes();

        assertEquals(5L, resultado);
        log.info("[TEST] Avisos activos vigentes: {}", resultado);
    }

    // ── actualizarAviso ───────────────────────────────────────────────────────

    @Test
    void actualizarAviso_datosValidos_actualizaCorrectamente() {
        log.info("[TEST] AvisoDelDiaService.actualizarAviso - actualización correcta");
        when(avisoRepository.findById(1L)).thenReturn(Optional.of(avisoEntity));
        when(avisoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        requestValido.setTitulo("Nuevo título");
        requestValido.setMensaje("Nuevo mensaje actualizado");

        AvisoDelDiaResponse resp = avisoService.actualizarAviso(1L, requestValido);

        assertEquals("Nuevo título", resp.getTitulo());
        assertEquals("Nuevo mensaje actualizado", resp.getMensaje());
        log.info("[TEST] Aviso actualizado: titulo={}", resp.getTitulo());
    }

    @Test
    void actualizarAviso_noExiste_lanzaExcepcion() {
        log.info("[TEST] AvisoDelDiaService.actualizarAviso - aviso 999 no existe");
        when(avisoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> avisoService.actualizarAviso(999L, requestValido));
        log.info("[TEST] Excepción al actualizar aviso inexistente");
    }
}
