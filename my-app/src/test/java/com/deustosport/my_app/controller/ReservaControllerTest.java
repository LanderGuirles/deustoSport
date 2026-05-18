package com.deustosport.my_app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.deustosport.my_app.dto.PagoReservaRequest;
import com.deustosport.my_app.dto.ReservaRequest;
import com.deustosport.my_app.entity.Instalacion;
import com.deustosport.my_app.entity.Polideportivo;
import com.deustosport.my_app.entity.Pista;
import com.deustosport.my_app.entity.Reserva;
import com.deustosport.my_app.entity.Usuario;
import com.deustosport.my_app.enums.EstadoReserva;
import com.deustosport.my_app.enums.MetodoPago;
import com.deustosport.my_app.enums.TipoDeporte;
import com.deustosport.my_app.service.QRCodeService;
import com.deustosport.my_app.service.ReservaService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"null", "unchecked"})
class ReservaControllerTest {

    private static final Logger log = LoggerFactory.getLogger(ReservaControllerTest.class);

    @Mock
    private ReservaService reservaService;

    @Mock
    private QRCodeService qrCodeService;

    @InjectMocks
    private ReservaController reservaController;

    @Test
    void crearReserva_siFaltanDatos_devuelveBadRequest() {
        log.info("[TEST] crearReserva_siFaltanDatos_devuelveBadRequest");
        ReservaRequest request = new ReservaRequest();
        request.setUsuarioId(1L);

        ResponseEntity<?> response = reservaController.crearReserva(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertTrue(String.valueOf(body.get("error")).contains("Faltan datos"));
        log.info("[TEST] Resultado: {} - {}", response.getStatusCode(), body.get("error"));
    }

    @Test
    void crearReserva_conDatosValidos_devuelveOk() {
        log.info("[TEST] crearReserva_conDatosValidos_devuelveOk - usuarioId=1, pistaId=2");
        ReservaRequest request = new ReservaRequest();
        request.setUsuarioId(1L);
        request.setPistaId(2L);
        request.setFecha(LocalDate.now().plusDays(1));
        request.setHoraInicio(LocalTime.of(10, 0));
        request.setDuracionMinutos(60);

        when(reservaService.crearReserva(eq(1L), eq(2L), any(LocalDate.class), any(LocalTime.class), eq(60)))
                .thenReturn(reservaBase());

        ResponseEntity<?> response = reservaController.crearReserva(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        log.info("[TEST] Reserva creada correctamente, status={}", response.getStatusCode());
    }

    @Test
    void pagarReserva_siFaltanCampos_devuelveBadRequest() {
        log.info("[TEST] pagarReserva_siFaltanCampos_devuelveBadRequest - usuarioId=null");
        PagoReservaRequest request = new PagoReservaRequest();
        request.setUsuarioId(null);
        request.setMetodoPago("TARJETA");

        ResponseEntity<?> response = reservaController.pagarReserva(10L, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertTrue(String.valueOf(body.get("error")).contains("usuarioId"));
        log.info("[TEST] Error esperado recibido: {}", body.get("error"));
    }

    @Test
    void consultarDisponibilidad_devuelveResultadoServicio() {
        log.info("[TEST] consultarDisponibilidad_devuelveResultadoServicio - pistaId=2");
        when(reservaService.consultarDisponibilidad(eq(2L), any(LocalDate.class), any(LocalTime.class), any(LocalTime.class)))
                .thenReturn(true);

        ResponseEntity<?> response = reservaController.consultarDisponibilidad(2L, "2026-05-03", "10:00", 60);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(true, body.get("disponible"));
        log.info("[TEST] Disponibilidad={}", body.get("disponible"));
    }

    private Reserva reservaBase() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEsSocio(false);
        usuario.setBilletera(new BigDecimal("50.00"));

        Polideportivo polideportivo = new Polideportivo();
        polideportivo.setHoraApertura(LocalTime.of(8, 0));
        polideportivo.setHoraCierre(LocalTime.of(22, 0));

        Instalacion instalacion = new Instalacion();
        instalacion.setPolideportivo(polideportivo);

        Pista pista = new Pista();
        pista.setId(2L);
        pista.setNombre("Pista Test");
        pista.setTipoDeporte(TipoDeporte.PADEL);
        pista.setInstalacion(instalacion);

        Reserva reserva = new Reserva();
        reserva.setId(3L);
        reserva.setUsuario(usuario);
        reserva.setPista(pista);
        reserva.setEstado(EstadoReserva.PENDIENTE);
        reserva.setFechaReserva(LocalDate.now().plusDays(1));
        reserva.setHoraInicio(LocalTime.of(10, 0));
        reserva.setHoraFin(LocalTime.of(11, 0));
        reserva.setPrecioTotal(new BigDecimal("15.00"));
        return reserva;
    }
}