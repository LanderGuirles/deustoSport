// my-app/src/test/java/com/deustosport/my_app/service/ReservaServiceTest.java

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

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class ReservaServiceTest {

    @Mock private ReservaRepository reservaRepository;
    @Mock private PistaRepository pistaRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private TarifaService tarifaService;
    @Mock private PagoService pagoService;  // ← el que realmente usa ReservaService

    @InjectMocks
    private ReservaService reservaService;

    private Usuario usuario;
    private Pista pista;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(10L);
        usuario.setEsSocio(false);

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
    }

    @Test
    void crearReserva_dejaEstadoPendiente() {
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
        void crearReserva_fueraHorarioGeneral_lanzaExcepcion() {
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
        Reserva reserva = new Reserva();
        reserva.setId(99L);
        reserva.setUsuario(usuario);
        reserva.setPista(pista);
        reserva.setFechaReserva(LocalDate.now().plusDays(2));
        reserva.setHoraInicio(LocalTime.of(18, 0));
        reserva.setHoraFin(LocalTime.of(19, 0));
        reserva.setEstado(EstadoReserva.PENDIENTE);
        reserva.setPrecioTotal(new BigDecimal("30.00"));

        // El Pago que devuelve pagoService ya tiene referencia y fecha
        Pago pagoMock = new Pago();
        pagoMock.setReferenciaPago("DS-ABCD1234");
        pagoMock.setFechaPago(java.time.LocalDateTime.now());
        pagoMock.setMetodoPago(MetodoPago.TARJETA);

        when(reservaRepository.findById(99L)).thenReturn(Optional.of(reserva));
        when(pagoService.procesarPagoInterno(eq(99L), isNull(), eq(MetodoPago.TARJETA)))
                .thenReturn(pagoMock);
        when(reservaRepository.save(isA(Reserva.class)))
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
        verify(pagoService).procesarPagoInterno(eq(99L), isNull(), eq(MetodoPago.TARJETA));
    }

    @Test
    void pagarReserva_conBizumInvalido_lanzaExcepcion() {
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
        verify(pagoService).procesarPagoInterno(eq(200L), eq(iban), eq(MetodoPago.TRANSFERENCIA));
    }
}