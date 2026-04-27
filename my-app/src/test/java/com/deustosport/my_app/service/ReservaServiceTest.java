package com.deustosport.my_app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.deustosport.my_app.entity.*;
import com.deustosport.my_app.enums.*;
import com.deustosport.my_app.repository.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class ReservaServiceTest {

    private static final Logger log = LoggerFactory.getLogger(ReservaServiceTest.class);

    @Mock private ReservaRepository reservaRepository;
    @Mock private PistaRepository pistaRepository;
    @Mock private UsuarioRepository usuarioRepository;
        @Mock private EmailService emailService;
    @Mock private TarifaService tarifaService;
        @Mock private PagoService pagoService;
        @Mock private NotificacionService notificacionService;
        @Mock private AbonoUsuarioService abonoUsuarioService;

    @InjectMocks
    private ReservaService reservaService;

    private Usuario usuario;
    private Pista pista;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(10L);
        usuario.setEmail("usuario@deustosport.com");
        usuario.setEsSocio(false);
        usuario.setBilletera(new BigDecimal("100.00"));

        Instalacion instalacion = new Instalacion();
        instalacion.setId(1L);
        instalacion.setHoraApertura(LocalTime.of(8, 0));
        instalacion.setHoraCierre(LocalTime.of(22, 0));

        pista = new Pista();
        pista.setId(20L);
        pista.setNombre("Pista central");
        pista.setTipoDeporte(TipoDeporte.PADEL);
        pista.setActiva(true);
        pista.setInstalacion(instalacion);

                lenient().when(abonoUsuarioService.obtenerAbonoActivo(anyLong())).thenReturn(Optional.empty());
    }

    @Test
    void crearReserva_dejaEstadoPendiente() {
        log.info("[TEST] crearReserva_dejaEstadoPendiente - usuario={}, pista={}", usuario.getId(), pista.getId());
        LocalDate fecha = LocalDate.now().plusDays(1);
        LocalTime horaInicio = LocalTime.of(10, 0);
        LocalTime horaFin = horaInicio.plusMinutes(60);

        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(pistaRepository.findById(20L)).thenReturn(Optional.of(pista));
        when(reservaRepository.findConflictingReservations(20L, fecha, horaInicio, horaFin))
                .thenReturn(List.of());
        when(tarifaService.calcularPrecio(eq(TipoDeporte.PADEL), eq(fecha),
                eq(horaInicio), eq(horaFin), eq(false)))
                .thenReturn(new BigDecimal("24.50"));
        when(reservaRepository.save(isA(Reserva.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Reserva reserva = reservaService.crearReserva(10L, 20L, fecha, horaInicio, 60);

        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
        assertEquals(new BigDecimal("24.50"), reserva.getPrecioTotal());
    }

    @Test
        void crearReserva_conSaldoInsuficiente_seCreaIgual() {
                log.info("[TEST] crearReserva_conSaldoInsuficiente_seCreaIgual - no debe depender de billetera");
        LocalDate fecha = LocalDate.now().plusDays(1);
        LocalTime horaInicio = LocalTime.of(10, 0);
        LocalTime horaFin = horaInicio.plusMinutes(60);
        usuario.setBilletera(new BigDecimal("5.00"));

        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(pistaRepository.findById(20L)).thenReturn(Optional.of(pista));
        when(reservaRepository.findConflictingReservations(20L, fecha, horaInicio, horaFin))
                .thenReturn(List.of());
        when(tarifaService.calcularPrecio(eq(TipoDeporte.PADEL), eq(fecha),
                eq(horaInicio), eq(horaFin), eq(false)))
                .thenReturn(new BigDecimal("24.50"));
        when(reservaRepository.save(isA(Reserva.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Reserva reserva = reservaService.crearReserva(10L, 20L, fecha, horaInicio, 60);

        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
        assertEquals(new BigDecimal("24.50"), reserva.getPrecioTotal());
        verify(reservaRepository).save(any());
    }

    @Test
    void crearReserva_fueraHorarioGeneral_lanzaExcepcion() {
        log.info("[TEST] crearReserva_fueraHorarioGeneral_lanzaExcepcion - hora 22:00 fuera de apertura");
        LocalDate fecha = LocalDate.now().plusDays(1);
        LocalTime horaInicio = LocalTime.of(22, 0);

        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(pistaRepository.findById(20L)).thenReturn(Optional.of(pista));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> reservaService.crearReserva(10L, 20L, fecha, horaInicio, 60));

        assertTrue(ex.getMessage().contains("horario general"));
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void pagarReserva_conTarjeta_confirmaReserva() {
        log.info("[TEST] pagarReserva_conTarjeta_confirmaReserva - reservaId=99, precio=30.00");
        Reserva reserva = new Reserva();
        reserva.setId(99L);
        reserva.setUsuario(usuario);
        reserva.setPista(pista);
        reserva.setFechaReserva(LocalDate.now().plusDays(2));
        reserva.setHoraInicio(LocalTime.of(18, 0));
        reserva.setHoraFin(LocalTime.of(19, 0));
        reserva.setEstado(EstadoReserva.PENDIENTE);
        reserva.setPrecioTotal(new BigDecimal("30.00"));

        Pago pagoMock = new Pago();
        pagoMock.setReferenciaPago("DS-ABCD1234");
        pagoMock.setFechaPago(java.time.LocalDateTime.now());
        pagoMock.setMetodoPago(MetodoPago.TARJETA);

        when(reservaRepository.findById(99L)).thenReturn(Optional.of(reserva));
        when(pagoService.procesarPagoInterno(eq(99L), eq("SIMULADO00000000000000"), eq(MetodoPago.TARJETA)))
                .thenReturn(pagoMock);
        when(reservaRepository.save(isA(Reserva.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(usuarioRepository.save(isA(Usuario.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Reserva pagada = reservaService.pagarReserva(
                99L, 10L, MetodoPago.TARJETA,
                "4111111111111111", "Usuario Prueba", "12/30", "123",
                null, null
        );

        assertEquals(EstadoReserva.CONFIRMADA, pagada.getEstado());
        assertEquals(MetodoPago.TARJETA, pagada.getMetodoPago());
        assertNotNull(pagada.getFechaPago());
        assertEquals("DS-ABCD1234", pagada.getReferenciaPago());
        assertEquals(new BigDecimal("100.00"), pagada.getUsuario().getBilletera());
        verify(pagoService).procesarPagoInterno(eq(99L), eq("SIMULADO00000000000000"), eq(MetodoPago.TARJETA));
        verify(usuarioRepository, never()).save(any());
        verify(emailService).enviarEmailConfirmacionReserva(
                eq("usuario@deustosport.com"),
                eq("Pista central"),
                eq("PADEL"),
                eq(pagada.getFechaReserva()),
                eq(pagada.getHoraInicio()),
                eq(pagada.getHoraFin()),
                eq(new BigDecimal("30.00")));
    }

    @Test
    void pagarReserva_conBizumInvalido_lanzaExcepcion() {
        log.info("[TEST] pagarReserva_conBizumInvalido_lanzaExcepcion - telefono='555' invalido");
        Reserva reserva = new Reserva();
        reserva.setId(101L);
        reserva.setUsuario(usuario);
        reserva.setPista(pista);
        reserva.setFechaReserva(LocalDate.now().plusDays(3));
        reserva.setHoraInicio(LocalTime.of(12, 0));
        reserva.setHoraFin(LocalTime.of(13, 0));
        reserva.setEstado(EstadoReserva.PENDIENTE);

        when(reservaRepository.findById(101L)).thenReturn(Optional.of(reserva));

        assertThrows(IllegalArgumentException.class, () -> reservaService.pagarReserva(
                101L, 10L, MetodoPago.BIZUM,
                null, null, null, null, "555", null
        ));

        verify(reservaRepository, never()).save(isA(Reserva.class));
        verify(pagoService, never()).procesarPagoInterno(any(), any(), any());
    }

    @Test
    void pagarReserva_conTransferencia_guardaIban() {
        Reserva reserva = new Reserva();
        reserva.setId(200L);
        reserva.setUsuario(usuario);
        reserva.setPista(pista);
        reserva.setFechaReserva(LocalDate.now().plusDays(1));
        reserva.setHoraInicio(LocalTime.of(9, 0));
        reserva.setHoraFin(LocalTime.of(10, 0));
        reserva.setEstado(EstadoReserva.PENDIENTE);
        reserva.setPrecioTotal(new BigDecimal("15.00"));

        Pago pagoMock = new Pago();
        pagoMock.setReferenciaPago("DS-TRANSFER1");
        pagoMock.setFechaPago(java.time.LocalDateTime.now());
        pagoMock.setMetodoPago(MetodoPago.TRANSFERENCIA);

        String iban = "ES9121000418450200051332";

        when(reservaRepository.findById(200L)).thenReturn(Optional.of(reserva));
        when(pagoService.procesarPagoInterno(eq(200L), eq(iban), eq(MetodoPago.TRANSFERENCIA)))
                .thenReturn(pagoMock);
        when(reservaRepository.save(isA(Reserva.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Reserva pagada = reservaService.pagarReserva(
                200L, 10L, MetodoPago.TRANSFERENCIA,
                null, null, null, null, null, iban
        );

        assertEquals(EstadoReserva.CONFIRMADA, pagada.getEstado());
        assertEquals(new BigDecimal("100.00"), pagada.getUsuario().getBilletera());
        verify(pagoService).procesarPagoInterno(eq(200L), eq(iban), eq(MetodoPago.TRANSFERENCIA));
        verify(usuarioRepository, never()).save(any());
        verify(emailService).enviarEmailConfirmacionReserva(
                eq("usuario@deustosport.com"), anyString(), anyString(), any(), any(), any(), any());
    }

    @Test
    void pagarReserva_conBilletera_descuentaSaldo() {
        Reserva reserva = new Reserva();
        reserva.setId(205L);
        reserva.setUsuario(usuario);
        reserva.setPista(pista);
        reserva.setFechaReserva(LocalDate.now().plusDays(1));
        reserva.setHoraInicio(LocalTime.of(11, 0));
        reserva.setHoraFin(LocalTime.of(12, 0));
        reserva.setEstado(EstadoReserva.PENDIENTE);
        reserva.setPrecioTotal(new BigDecimal("15.00"));

        Pago pagoMock = new Pago();
        pagoMock.setReferenciaPago("DS-BILLETERA1");
        pagoMock.setFechaPago(LocalDateTime.now());
        pagoMock.setMetodoPago(MetodoPago.BILLETERA);

        when(reservaRepository.findById(205L)).thenReturn(Optional.of(reserva));
        when(pagoService.procesarPagoInterno(eq(205L), eq("SIMULADO00000000000000"), eq(MetodoPago.BILLETERA)))
                .thenReturn(pagoMock);
        when(reservaRepository.save(isA(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));
        when(usuarioRepository.save(isA(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Reserva pagada = reservaService.pagarReserva(
                205L, 10L, MetodoPago.BILLETERA,
                null, null, null, null, null, null
        );

        assertEquals(new BigDecimal("85.00"), pagada.getUsuario().getBilletera());
        verify(usuarioRepository).save(any());
    }

    @Test
    void cancelarReserva_conMasDe24h_cancelaCorrectamenteYReembolsa() {
        Reserva reserva = new Reserva();
        reserva.setId(300L);
        reserva.setUsuario(usuario);
        reserva.setPista(pista);
        reserva.setFechaReserva(LocalDate.now().plusDays(2));
        reserva.setHoraInicio(LocalTime.of(10, 0));
        reserva.setHoraFin(LocalTime.of(11, 0));
        reserva.setEstado(EstadoReserva.CONFIRMADA);
        reserva.setMetodoPago(MetodoPago.BILLETERA);
        reserva.setPrecioTotal(new BigDecimal("20.00"));

        when(reservaRepository.findById(300L)).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(isA(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));
        when(usuarioRepository.save(isA(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Reserva cancelada = reservaService.cancelarReserva(300L, 10L);

        assertEquals(EstadoReserva.CANCELADA, cancelada.getEstado());
        assertEquals(new BigDecimal("120.00"), cancelada.getUsuario().getBilletera());
        verify(reservaRepository).save(reserva);
    }

        @Test
        void cancelarReserva_pagadaConTarjeta_noReembolsaBilletera() {
                Reserva reserva = new Reserva();
                reserva.setId(302L);
                reserva.setUsuario(usuario);
                reserva.setPista(pista);
                reserva.setFechaReserva(LocalDate.now().plusDays(2));
                reserva.setHoraInicio(LocalTime.of(10, 0));
                reserva.setHoraFin(LocalTime.of(11, 0));
                reserva.setEstado(EstadoReserva.CONFIRMADA);
                reserva.setMetodoPago(MetodoPago.TARJETA);
                reserva.setPrecioTotal(new BigDecimal("20.00"));

                when(reservaRepository.findById(302L)).thenReturn(Optional.of(reserva));
                when(reservaRepository.save(isA(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));

                Reserva cancelada = reservaService.cancelarReserva(302L, 10L);

                assertEquals(EstadoReserva.CANCELADA, cancelada.getEstado());
                assertEquals(new BigDecimal("100.00"), cancelada.getUsuario().getBilletera());
                verify(usuarioRepository, never()).save(any());
        }

    @Test
    void cancelarReserva_con24hOMenos_lanzaExcepcion() {
        LocalDateTime inicioEnMenosDe24h = LocalDateTime.now().plusHours(23).withSecond(0).withNano(0);

        Reserva reserva = new Reserva();
        reserva.setId(301L);
        reserva.setUsuario(usuario);
        reserva.setPista(pista);
        reserva.setFechaReserva(inicioEnMenosDe24h.toLocalDate());
        reserva.setHoraInicio(inicioEnMenosDe24h.toLocalTime());
        reserva.setHoraFin(reserva.getHoraInicio().plusHours(1));
        reserva.setEstado(EstadoReserva.CONFIRMADA);

        when(reservaRepository.findById(301L)).thenReturn(Optional.of(reserva));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> reservaService.cancelarReserva(301L, 10L));

        assertTrue(ex.getMessage().contains("24 horas"));
        verify(reservaRepository, never()).save(any());
    }

        @Test
        void modificarReserva_conMasDe24h_actualizaFechaHoraYPrecio() {
                Reserva reserva = new Reserva();
                reserva.setId(450L);
                reserva.setUsuario(usuario);
                reserva.setPista(pista);
                reserva.setFechaReserva(LocalDate.now().plusDays(3));
                reserva.setHoraInicio(LocalTime.of(12, 0));
                reserva.setHoraFin(LocalTime.of(13, 0));
                reserva.setEstado(EstadoReserva.PENDIENTE);
                reserva.setPrecioTotal(new BigDecimal("20.00"));

                LocalDate nuevaFecha = LocalDate.now().plusDays(4);
                LocalTime nuevaHora = LocalTime.of(18, 0);

                when(reservaRepository.findById(450L)).thenReturn(Optional.of(reserva));
                when(reservaRepository.findConflictingReservationsExcludingReserva(
                                eq(20L), eq(nuevaFecha), eq(nuevaHora), eq(nuevaHora.plusMinutes(60)), eq(450L)))
                                .thenReturn(List.of());
                when(tarifaService.calcularPrecio(eq(TipoDeporte.PADEL), eq(nuevaFecha), eq(nuevaHora),
                                eq(nuevaHora.plusMinutes(60)), eq(false))).thenReturn(new BigDecimal("25.00"));
                when(reservaRepository.save(isA(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));

                Reserva modificada = reservaService.modificarReserva(450L, 10L, nuevaFecha, nuevaHora, 60);

                assertEquals(nuevaFecha, modificada.getFechaReserva());
                assertEquals(nuevaHora, modificada.getHoraInicio());
                assertEquals(LocalTime.of(19, 0), modificada.getHoraFin());
                assertEquals(new BigDecimal("25.00"), modificada.getPrecioTotal());
        }

        @Test
        void modificarReserva_con24hOMenos_lanzaExcepcion() {
                LocalDateTime inicioEnMenosDe24h = LocalDateTime.now().plusHours(20).withSecond(0).withNano(0);

                Reserva reserva = new Reserva();
                reserva.setId(451L);
                reserva.setUsuario(usuario);
                reserva.setPista(pista);
                reserva.setFechaReserva(inicioEnMenosDe24h.toLocalDate());
                reserva.setHoraInicio(inicioEnMenosDe24h.toLocalTime());
                reserva.setHoraFin(reserva.getHoraInicio().plusHours(1));
                reserva.setEstado(EstadoReserva.PENDIENTE);

                when(reservaRepository.findById(451L)).thenReturn(Optional.of(reserva));

                IllegalStateException ex = assertThrows(IllegalStateException.class,
                                () -> reservaService.modificarReserva(451L, 10L,
                                                LocalDate.now().plusDays(2), LocalTime.of(10, 0), 60));

                assertTrue(ex.getMessage().contains("24 horas"));
                verify(reservaRepository, never()).save(any());
        }
}