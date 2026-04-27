package com.deustosport.my_app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deustosport.my_app.dto.PagoResponse;
import com.deustosport.my_app.entity.Pago;
import com.deustosport.my_app.entity.Reserva;
import com.deustosport.my_app.enums.EstadoPago;
import com.deustosport.my_app.enums.MetodoPago;
import com.deustosport.my_app.repository.PagoRepository;
import com.deustosport.my_app.repository.ReservaRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
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
class PagoServiceTest {

    private static final Logger log = LoggerFactory.getLogger(PagoServiceTest.class);

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private ReservaRepository reservaRepository;

    @InjectMocks
    private PagoService pagoService;

    @Test
    void obtenerPagoPorReserva_mapeaDtoYNormalizaIbanNull() {
        log.info("[TEST] obtenerPagoPorReserva_mapeaDtoYNormalizaIbanNull - reservaId=8");
        Pago pago = new Pago();
        pago.setReferenciaPago("DS-AAA11111");
        pago.setImporte(new BigDecimal("19.99"));
        pago.setMetodoPago(MetodoPago.BIZUM);
        pago.setEstadoPago(EstadoPago.COMPLETADO);
        pago.setIban(null);
        pago.setFechaPago(LocalDateTime.now());

        when(pagoRepository.findByReservaId(8L)).thenReturn(Optional.of(pago));

        PagoResponse response = pagoService.obtenerPagoPorReserva(8L);

        assertEquals("DS-AAA11111", response.getReferenciaPago());
        assertEquals(new BigDecimal("19.99"), response.getImporte());
        assertEquals(MetodoPago.BIZUM, response.getMetodoPago());
        assertEquals("", response.getIban());
    }

    @Test
    void procesarPagoInterno_transferencia_normalizaIbanYCompleta() {
        log.info("[TEST] procesarPagoInterno_transferencia_normalizaIbanYCompleta - reservaId=12");
        Reserva reserva = new Reserva();
        reserva.setId(12L);
        reserva.setPrecioTotal(new BigDecimal("35.00"));

        when(reservaRepository.findById(12L)).thenReturn(Optional.of(reserva));
        when(pagoRepository.findByReservaId(12L)).thenReturn(Optional.empty());
        when(pagoRepository.save(any(Pago.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pago pago = pagoService.procesarPagoInterno(12L, "es91 2100 0418 4502 0005 1332", MetodoPago.TRANSFERENCIA);

        assertEquals(EstadoPago.COMPLETADO, pago.getEstadoPago());
        assertEquals("ES9121000418450200051332", pago.getIban());
        assertEquals(new BigDecimal("35.00"), pago.getImporte());
        assertNotNull(pago.getReferenciaPago());
        verify(pagoRepository, times(2)).save(any(Pago.class));
    }

    @Test
    void procesarPagoInterno_siYaExistePago_lanzaExcepcion() {
        log.info("[TEST] procesarPagoInterno_siYaExistePago_lanzaExcepcion - reservaId=33");
        Reserva reserva = new Reserva();
        reserva.setId(33L);

        when(reservaRepository.findById(33L)).thenReturn(Optional.of(reserva));
        when(pagoRepository.findByReservaId(33L)).thenReturn(Optional.of(new Pago()));

        assertThrows(IllegalStateException.class,
                () -> pagoService.procesarPagoInterno(33L, null, MetodoPago.TARJETA));
    }

    @Test
    void obtenerRecaudacionMesActual_siSumaNull_devuelveCeroConMoneda() {
        log.info("[TEST] obtenerRecaudacionMesActual_siSumaNull_devuelveCeroConMoneda");
        when(pagoRepository.sumImporteByEstadoAndRangoFechas(any(), any(), any())).thenReturn(null);
        when(pagoRepository.countByEstadoAndRangoFechas(any(), any(), any())).thenReturn(4L);

        Map<String, Object> recaudacion = pagoService.obtenerRecaudacionMesActual();

        BigDecimal total = (BigDecimal) recaudacion.get("totalRecaudado");
        assertEquals(0, total.compareTo(new BigDecimal("0.00")));
        assertEquals("EUR", recaudacion.get("moneda"));
        assertEquals(4L, recaudacion.get("totalPagos"));
    }
}
