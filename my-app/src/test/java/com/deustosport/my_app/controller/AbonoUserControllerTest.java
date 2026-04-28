package com.deustosport.my_app.controller;

import com.deustosport.my_app.dto.AbonoUsuarioRequest;
import com.deustosport.my_app.entity.AbonoUsuario;
import com.deustosport.my_app.entity.PlanAbono;
import com.deustosport.my_app.entity.Usuario;
import com.deustosport.my_app.enums.DuracionAbonos;
import com.deustosport.my_app.service.AbonoUsuarioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AbonoUserControllerTest {

    private static final Logger log = LoggerFactory.getLogger(AbonoUserControllerTest.class);

    @Mock
    private AbonoUsuarioService abonoService;

    @InjectMocks
    private AbonoUserController abonoUserController;

    private PlanAbono planDummy() {
        PlanAbono p = new PlanAbono();
        p.setId(1L);
        p.setNombre("Plan Mensual");
        p.setDescripcion("Acceso mensual completo");
        p.setDuracion(DuracionAbonos.MENSUAL);
        p.setCantidadPersonas(1);
        p.setEdadMinima(16);
        p.setEdadMax(99);
        p.setPrecio(new BigDecimal("29.99"));
        p.setDescuentoPistasPorcentaje(new BigDecimal("10"));
        p.setActivo(true);
        return p;
    }

    private AbonoUsuario abonoDummy() {
        Usuario titular = new Usuario();
        titular.setEmail("titular@test.com");

        AbonoUsuario a = new AbonoUsuario();
        a.setId(10L);
        a.setPlan(planDummy());
        a.setFechaInicio(LocalDate.now());
        a.setFechaFin(LocalDate.now().plusMonths(1));
        a.setActivo(true);
        a.setTitular(titular);
        a.setBeneficiarios(Collections.emptyList());
        return a;
    }

    // ── GET /api/usuarios/{id}/abonos/disponibles ─────────────────────────────

    @Test
    void listarPlanesDisponibles_conPlanes_devuelve200() {
        log.info("[TEST] listarPlanesDisponibles - con planes → 200");
        when(abonoService.obtenerPlanesActivos()).thenReturn(List.of(planDummy()));

        ResponseEntity<?> resp = abonoUserController.listarPlanesDisponibles(1L);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        log.info("[TEST] Planes devueltos: OK");
    }

    @Test
    void listarPlanesDisponibles_sinPlanes_devuelve200ListaVacia() {
        log.info("[TEST] listarPlanesDisponibles - sin planes → 200 lista vacía");
        when(abonoService.obtenerPlanesActivos()).thenReturn(Collections.emptyList());

        ResponseEntity<?> resp = abonoUserController.listarPlanesDisponibles(1L);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
    }

    // ── POST /api/usuarios/{id}/abonos/comprar ────────────────────────────────

    @Test
    void comprarAbono_exitoso_devuelve200() {
        log.info("[TEST] comprarAbono - compra exitosa → 200");
        when(abonoService.comprarAbono(eq(1L), eq(1L), any(), anyString()))
                .thenReturn(abonoDummy());

        AbonoUsuarioRequest req = new AbonoUsuarioRequest();
        req.setPlanAbonoId(1L);
        req.setEmailsBeneficiarios(Collections.emptyList());
        req.setMetodoPago("TARJETA");

        ResponseEntity<?> resp = abonoUserController.comprarAbono(1L, req);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        log.info("[TEST] Abono comprado con éxito");
    }

    @Test
    void comprarAbono_planInexistente_devuelve400() {
        log.info("[TEST] comprarAbono - plan no encontrado → 400");
        when(abonoService.comprarAbono(eq(1L), eq(99L), any(), anyString()))
                .thenThrow(new RuntimeException("Plan no encontrado"));

        AbonoUsuarioRequest req = new AbonoUsuarioRequest();
        req.setPlanAbonoId(99L);
        req.setEmailsBeneficiarios(Collections.emptyList());
        req.setMetodoPago("TARJETA");

        ResponseEntity<?> resp = abonoUserController.comprarAbono(1L, req);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        log.info("[TEST] Error controlado: 400");
    }

    @Test
    void comprarAbono_saldoInsuficiente_devuelve400() {
        log.info("[TEST] comprarAbono - saldo insuficiente → 400");
        when(abonoService.comprarAbono(eq(2L), eq(1L), any(), anyString()))
                .thenThrow(new RuntimeException("Saldo insuficiente en billetera"));

        AbonoUsuarioRequest req = new AbonoUsuarioRequest();
        req.setPlanAbonoId(1L);
        req.setEmailsBeneficiarios(Collections.emptyList());
        req.setMetodoPago("BILLETERA");

        ResponseEntity<?> resp = abonoUserController.comprarAbono(2L, req);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    // ── GET /api/usuarios/{id}/abonos/activo ──────────────────────────────────

    @Test
    void verAbonoActivo_conAbono_devuelve200() {
        log.info("[TEST] verAbonoActivo - usuario tiene abono activo → 200");
        when(abonoService.obtenerAbonoActivo(1L)).thenReturn(Optional.of(abonoDummy()));

        ResponseEntity<?> resp = abonoUserController.verAbonoActivo(1L);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        log.info("[TEST] Abono activo devuelto correctamente");
    }

    @Test
    void verAbonoActivo_sinAbono_devuelve200Vacio() {
        log.info("[TEST] verAbonoActivo - sin abono activo → 200 vacío");
        when(abonoService.obtenerAbonoActivo(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> resp = abonoUserController.verAbonoActivo(1L);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        log.info("[TEST] Sin abono: respuesta vacía correcta");
    }
}
