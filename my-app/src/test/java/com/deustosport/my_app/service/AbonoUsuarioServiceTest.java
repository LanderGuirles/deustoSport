package com.deustosport.my_app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.deustosport.my_app.entity.AbonoUsuario;
import com.deustosport.my_app.entity.PlanAbono;
import com.deustosport.my_app.entity.Usuario;
import com.deustosport.my_app.enums.DuracionAbonos;
import com.deustosport.my_app.repository.AbonoUsuarioRepository;
import com.deustosport.my_app.repository.PlanAbonoRepository;
import com.deustosport.my_app.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
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
class AbonoUsuarioServiceTest {

    private static final Logger log = LoggerFactory.getLogger(AbonoUsuarioServiceTest.class);

    @Mock private AbonoUsuarioRepository abonoUsuarioRepository;
    @Mock private PlanAbonoRepository planAbonoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private NotificacionService notificacionService;

    @InjectMocks
    private AbonoUsuarioService abonoUsuarioService;

    private Usuario titular;
    private PlanAbono planMensual;

    @BeforeEach
    void setUp() {
        titular = new Usuario();
        titular.setId(1L);
        titular.setEmail("titular@deustosport.com");
        titular.setFechaNacimiento(LocalDate.of(1990, 1, 1)); // 36 años
        titular.setBilletera(new BigDecimal("100.00"));

        planMensual = new PlanAbono();
        planMensual.setId(10L);
        planMensual.setNombre("Plan Mensual");
        planMensual.setActivo(true);
        planMensual.setEdadMinima(0);
        planMensual.setEdadMax(99);
        planMensual.setPrecio(new BigDecimal("20.00"));
        planMensual.setDuracion(DuracionAbonos.MENSUAL);
        planMensual.setCantidadPersonas(1);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  obtenerPlanesActivos
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void obtenerPlanesActivos_filtraSoloActivos() {
        log.info("[TEST] obtenerPlanesActivos_filtraSoloActivos");
        PlanAbono inactivo = new PlanAbono();
        inactivo.setActivo(false);
        when(planAbonoRepository.findAll()).thenReturn(List.of(planMensual, inactivo));

        List<PlanAbono> activos = abonoUsuarioService.obtenerPlanesActivos();

        assertEquals(1, activos.size());
        assertTrue(activos.get(0).isActivo());
        log.info("[TEST] Planes activos devueltos: {}", activos.size());
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  comprarAbono - casos positivos
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void comprarAbono_planMensual_creaAbonoConFechaCorrrecta() {
        log.info("[TEST] comprarAbono_planMensual_creaAbonoConFechaCorrecta - titularId=1, planId=10");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(titular));
        when(abonoUsuarioRepository.findAbonoActivoByUsuarioId(eq(1L), any())).thenReturn(Optional.empty());
        when(planAbonoRepository.findById(10L)).thenReturn(Optional.of(planMensual));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(titular);
        when(abonoUsuarioRepository.save(any(AbonoUsuario.class))).thenAnswer(inv -> inv.getArgument(0));

        AbonoUsuario resultado = abonoUsuarioService.comprarAbono(1L, 10L, List.of(), "BILLETERA");

        assertNotNull(resultado);
        assertTrue(resultado.isActivo());
        assertEquals(LocalDate.now().plusMonths(1), resultado.getFechaFin());
        assertEquals(new BigDecimal("80.00"), titular.getBilletera());
        log.info("[TEST] Abono mensual creado, fechaFin={}, saldoRestante={}", resultado.getFechaFin(), titular.getBilletera());
    }

    @Test
    void comprarAbono_planAnual_calcularFechaFinPorAnio() {
        log.info("[TEST] comprarAbono_planAnual_calcularFechaFinPorAnio");
        planMensual.setDuracion(DuracionAbonos.ANUAL);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(titular));
        when(abonoUsuarioRepository.findAbonoActivoByUsuarioId(eq(1L), any())).thenReturn(Optional.empty());
        when(planAbonoRepository.findById(10L)).thenReturn(Optional.of(planMensual));
        when(usuarioRepository.save(any())).thenReturn(titular);
        when(abonoUsuarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AbonoUsuario resultado = abonoUsuarioService.comprarAbono(1L, 10L, null, "BILLETERA");

        assertEquals(LocalDate.now().plusYears(1), resultado.getFechaFin());
        log.info("[TEST] Abono anual: fechaFin={}", resultado.getFechaFin());
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  comprarAbono - casos negativos
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void comprarAbono_yaExisteAbonoActivo_lanzaExcepcion() {
        log.info("[TEST] comprarAbono_yaExisteAbonoActivo_lanzaExcepcion");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(titular));
        when(abonoUsuarioRepository.findAbonoActivoByUsuarioId(eq(1L), any()))
                .thenReturn(Optional.of(new AbonoUsuario()));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> abonoUsuarioService.comprarAbono(1L, 10L, List.of(), "BILLETERA"));
        assertTrue(ex.getMessage().contains("abono activo"));
        log.info("[TEST] Excepción lanzada: {}", ex.getMessage());
    }

    @Test
    void comprarAbono_saldoInsuficiente_lanzaExcepcion() {
        log.info("[TEST] comprarAbono_saldoInsuficiente_lanzaExcepcion - billetera=5€, plan=20€");
        titular.setBilletera(new BigDecimal("5.00"));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(titular));
        when(abonoUsuarioRepository.findAbonoActivoByUsuarioId(eq(1L), any())).thenReturn(Optional.empty());
        when(planAbonoRepository.findById(10L)).thenReturn(Optional.of(planMensual));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> abonoUsuarioService.comprarAbono(1L, 10L, List.of(), "BILLETERA"));
        assertTrue(ex.getMessage().contains("Saldo insuficiente"));
        log.info("[TEST] Excepción de saldo: {}", ex.getMessage());
    }

    @Test
    void comprarAbono_planInactivo_lanzaExcepcion() {
        log.info("[TEST] comprarAbono_planInactivo_lanzaExcepcion");
        planMensual.setActivo(false);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(titular));
        when(abonoUsuarioRepository.findAbonoActivoByUsuarioId(eq(1L), any())).thenReturn(Optional.empty());
        when(planAbonoRepository.findById(10L)).thenReturn(Optional.of(planMensual));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> abonoUsuarioService.comprarAbono(1L, 10L, List.of(), "BILLETERA"));
        assertTrue(ex.getMessage().contains("activo"));
        log.info("[TEST] Excepción plan inactivo: {}", ex.getMessage());
    }

    @Test
    void comprarAbono_edadFueraDeLimite_lanzaExcepcion() {
        log.info("[TEST] comprarAbono_edadFueraDeLimite_lanzaExcepcion - plan solo para <18");
        planMensual.setEdadMinima(0);
        planMensual.setEdadMax(17);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(titular)); // titular tiene 36 años
        when(abonoUsuarioRepository.findAbonoActivoByUsuarioId(eq(1L), any())).thenReturn(Optional.empty());
        when(planAbonoRepository.findById(10L)).thenReturn(Optional.of(planMensual));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> abonoUsuarioService.comprarAbono(1L, 10L, List.of(), "BILLETERA"));
        assertTrue(ex.getMessage().contains("edad"));
        log.info("[TEST] Excepción edad: {}", ex.getMessage());
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  obtenerAbonoActivo
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void obtenerAbonoActivo_devuelveOptionalVacioSiNoHay() {
        log.info("[TEST] obtenerAbonoActivo_devuelveOptionalVacioSiNoHay - usuarioId=1");
        when(abonoUsuarioRepository.findAbonoActivoByUsuarioId(eq(1L), any())).thenReturn(Optional.empty());

        Optional<AbonoUsuario> resultado = abonoUsuarioService.obtenerAbonoActivo(1L);

        assertTrue(resultado.isEmpty());
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  desactivarAbonosCaducados
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void desactivarAbonosCaducados_marcaAbonosComoInactivos() {
        log.info("[TEST] desactivarAbonosCaducados_marcaAbonosComoInactivos - 2 caducados");
        AbonoUsuario a1 = new AbonoUsuario(); a1.setActivo(true);
        AbonoUsuario a2 = new AbonoUsuario(); a2.setActivo(true);
        when(abonoUsuarioRepository.findByActivoTrueAndFechaFinBefore(any())).thenReturn(List.of(a1, a2));

        int desactivados = abonoUsuarioService.desactivarAbonosCaducados();

        assertEquals(2, desactivados);
        assertFalse(a1.isActivo());
        assertFalse(a2.isActivo());
        verify(abonoUsuarioRepository).saveAll(anyList());
        log.info("[TEST] {} abonos desactivados", desactivados);
    }
}
