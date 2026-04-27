package com.deustosport.my_app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.deustosport.my_app.entity.PlanAbono;
import com.deustosport.my_app.enums.DuracionAbonos;
import com.deustosport.my_app.repository.PlanAbonoRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class AbonoConfigServiceTest {

    private static final Logger log = LoggerFactory.getLogger(AbonoConfigServiceTest.class);

    @Mock
    private PlanAbonoRepository planAbonoRepository;

    @InjectMocks
    private AbonoConfigService abonoConfigService;

    private PlanAbono planBase() {
        PlanAbono p = new PlanAbono();
        p.setId(1L);
        p.setNombre("Abono Familiar");
        p.setDescripcion("Plan para familias");
        p.setCantidadPersonas(4);
        p.setEdadMinima(0);
        p.setEdadMax(99);
        p.setPrecio(new BigDecimal("49.99"));
        p.setDuracion(DuracionAbonos.MENSUAL);
        p.setDescuentoPistasPorcentaje(new BigDecimal("15.0"));
        p.setActivo(true);
        return p;
    }

    @Test
    void crearPlan_guardaYDevuelvePlan() {
        log.info("[TEST] crearPlan_guardaYDevuelvePlan");
        PlanAbono plan = planBase();
        when(planAbonoRepository.save(any(PlanAbono.class))).thenReturn(plan);

        PlanAbono resultado = abonoConfigService.crearPlan(plan);

        assertNotNull(resultado);
        assertEquals("Abono Familiar", resultado.getNombre());
        verify(planAbonoRepository).save(plan);
        log.info("[TEST] Plan creado: '{}'", resultado.getNombre());
    }

    @Test
    void listarPlanes_devuelveTodosLosPlanes() {
        log.info("[TEST] listarPlanes_devuelveTodosLosPlanes");
        when(planAbonoRepository.findAll()).thenReturn(List.of(planBase()));

        List<PlanAbono> planes = abonoConfigService.listarPlanes();

        assertEquals(1, planes.size());
        log.info("[TEST] Planes devueltos: {}", planes.size());
    }

    @Test
    void actualizarPlan_existente_actualizaCampos() {
        log.info("[TEST] actualizarPlan_existente_actualizaCampos - planId=1");
        PlanAbono existente = planBase();
        when(planAbonoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(planAbonoRepository.save(any(PlanAbono.class))).thenAnswer(inv -> inv.getArgument(0));

        PlanAbono detalles = new PlanAbono();
        detalles.setNombre("Abono Individual");
        detalles.setDescripcion("Solo para una persona");
        detalles.setCantidadPersonas(1);
        detalles.setEdadMinima(18);
        detalles.setEdadMax(65);
        detalles.setPrecio(new BigDecimal("19.99"));
        detalles.setDuracion(DuracionAbonos.ANUAL);
        detalles.setDescuentoPistasPorcentaje(new BigDecimal("10.0"));
        detalles.setActivo(true);

        PlanAbono actualizado = abonoConfigService.actualizarPlan(1L, detalles);

        assertEquals("Abono Individual", actualizado.getNombre());
        assertEquals(1, actualizado.getCantidadPersonas());
        assertEquals(DuracionAbonos.ANUAL, actualizado.getDuracion());
        log.info("[TEST] Plan actualizado: '{}', duracion={}", actualizado.getNombre(), actualizado.getDuracion());
    }

    @Test
    void actualizarPlan_noExiste_lanzaRuntimeException() {
        log.info("[TEST] actualizarPlan_noExiste_lanzaRuntimeException - planId=999");
        when(planAbonoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> abonoConfigService.actualizarPlan(999L, planBase()));
        log.info("[TEST] RuntimeException lanzada correctamente para plan inexistente");
    }

    @Test
    void eliminarPlan_llamaAlRepositorio() {
        log.info("[TEST] eliminarPlan_llamaAlRepositorio - planId=1");
        doNothing().when(planAbonoRepository).deleteById(1L);

        abonoConfigService.eliminarPlan(1L);

        verify(planAbonoRepository).deleteById(1L);
        log.info("[TEST] Plan eliminado correctamente");
    }
}
