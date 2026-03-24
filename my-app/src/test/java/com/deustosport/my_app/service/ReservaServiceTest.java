package com.deustosport.my_app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deustosport.my_app.entity.Pista;
import com.deustosport.my_app.entity.Reserva;
import com.deustosport.my_app.entity.Usuario;
import com.deustosport.my_app.enums.EstadoReserva;
import com.deustosport.my_app.enums.MetodoPago;
import com.deustosport.my_app.enums.TipoDeporte;
import com.deustosport.my_app.repository.PistaRepository;
import com.deustosport.my_app.repository.ReservaRepository;
import com.deustosport.my_app.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private PistaRepository pistaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private TarifaService tarifaService;

    @Mock
    private PaymentGatewayClient paymentGatewayClient;

    @InjectMocks
    private ReservaService reservaService;

    private Usuario usuario;
    private Pista pista;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(10L);
        usuario.setEsSocio(false);

        pista = new Pista();
        pista.setId(20L);
        pista.setNombre("Pista central");
        pista.setTipoDeporte(TipoDeporte.PADEL);
        pista.setActiva(true);
    }

    @Test
    void crearReserva_dejaEstadoPendiente() {
        LocalDate fecha = LocalDate.now().plusDays(1);
        LocalTime horaInicio = LocalTime.of(10, 0);
        LocalTime horaFin = horaInicio.plusMinutes(60);

        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(pistaRepository.findById(20L)).thenReturn(Optional.of(pista));
        when(reservaRepository.findConflictingReservations(20L, fecha, horaInicio, horaFin)).thenReturn(List.of());
        when(tarifaService.calcularPrecio(eq(TipoDeporte.PADEL), eq(fecha), eq(horaInicio), eq(horaFin), eq(false)))
                .thenReturn(new BigDecimal("24.50"));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reserva reserva = reservaService.crearReserva(10L, 20L, fecha, horaInicio, 60);

        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
        assertEquals(new BigDecimal("24.50"), reserva.getPrecioTotal());
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

        when(reservaRepository.findById(99L)).thenReturn(Optional.of(reserva));
        when(paymentGatewayClient.validarPagoExterno(
            any(),
            eq(new BigDecimal("30.00")),
            eq(MetodoPago.TARJETA),
            eq("4111111111111111"),
            eq(null)
        )).thenReturn("GW-OK-1234");
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reserva pagada = reservaService.pagarReserva(
                99L,
                10L,
                MetodoPago.TARJETA,
                "4111111111111111",
                "Usuario Prueba",
                "12/30",
                "123",
                null
        );

        assertEquals(EstadoReserva.CONFIRMADA, pagada.getEstado());
        assertEquals(MetodoPago.TARJETA, pagada.getMetodoPago());
        assertNotNull(pagada.getFechaPago());
        assertEquals("GW-OK-1234", pagada.getReferenciaPago());
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
                101L,
                10L,
                MetodoPago.BIZUM,
                null,
                null,
                null,
                null,
                "555"
        ));

        verify(reservaRepository, never()).save(any(Reserva.class));
        verify(paymentGatewayClient, never()).validarPagoExterno(any(), any(), any(), any(), any());
    }
}
