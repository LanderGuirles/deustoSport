package com.deustosport.my_app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deustosport.my_app.entity.Pista;
import com.deustosport.my_app.entity.Tarifa;
import com.deustosport.my_app.enums.TipoDeporte;
import com.deustosport.my_app.repository.PistaRepository;
import com.deustosport.my_app.repository.TarifaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
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
class TarifaServiceTest {

    private static final Logger log = LoggerFactory.getLogger(TarifaServiceTest.class);

    @Mock
    private TarifaRepository tarifaRepository;

    @Mock
    private PistaRepository pistaRepository;

    @Mock
    private ConfiguracionService configuracionService;

    @InjectMocks
    private TarifaService tarifaService;

    @Test
    void calcularPrecio_aplicaTarifaYDescuentoSocio() {
        log.info("[TEST] calcularPrecio_aplicaTarifaYDescuentoSocio - PADEL lunes 10:00-11:30 socio=true");
        LocalDate fecha = LocalDate.of(2026, 4, 20); // lunes
        LocalTime horaInicio = LocalTime.of(10, 0);
        LocalTime horaFin = LocalTime.of(11, 30);

        Tarifa tarifa = tarifaValida(TipoDeporte.PADEL, 1, LocalTime.of(9, 0), LocalTime.of(12, 0), "12.00", true);

        when(tarifaRepository.findActiveByDeporteDiaAndFecha(TipoDeporte.PADEL, 1, fecha))
                .thenReturn(List.of(tarifa));
        when(configuracionService.obtenerFactorDescuentoSocio())
                .thenReturn(new BigDecimal("0.20"));

        BigDecimal precio = tarifaService.calcularPrecio(TipoDeporte.PADEL, fecha, horaInicio, horaFin, true);

        assertEquals(new BigDecimal("14.40"), precio);
    }

    @Test
    void calcularPrecio_sinTarifaActiva_usaFallback() {
        log.info("[TEST] calcularPrecio_sinTarifaActiva_usaFallback → precio fallback 7.50€");
        LocalDate fecha = LocalDate.of(2026, 4, 21);
        LocalTime horaInicio = LocalTime.of(10, 0);
        LocalTime horaFin = LocalTime.of(10, 45);

        when(tarifaRepository.findActiveByDeporteDiaAndFecha(TipoDeporte.TENIS, 2, fecha))
                .thenReturn(List.of());

        BigDecimal precio = tarifaService.calcularPrecio(TipoDeporte.TENIS, fecha, horaInicio, horaFin, false);

        assertEquals(0, precio.compareTo(new BigDecimal("7.50")));
        verify(configuracionService, never()).obtenerFactorDescuentoSocio();
    }

    @Test
    void crearTarifa_horaFinIgualHoraInicio_lanzaExcepcion() {
        log.info("[TEST] crearTarifa_horaFinIgualHoraInicio_lanzaExcepcion → horaInicio==horaFin=18:00");
        Tarifa invalida = tarifaValida(TipoDeporte.FUTBOL, 3, LocalTime.of(18, 0), LocalTime.of(18, 0), "20.00", true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> tarifaService.crearTarifa(invalida));

        assertTrue(ex.getMessage().contains("hora de inicio"));
        verify(tarifaRepository, never()).save(org.mockito.ArgumentMatchers.any(Tarifa.class));
    }

    @Test
    void obtenerPorPistaId_devuelveSoloTarifasActivas() {
        log.info("[TEST] obtenerPorPistaId_devuelveSoloTarifasActivas - pistaId=11, debe filtrar inactivas");
        Pista pista = new Pista();
        pista.setId(11L);
        pista.setTipoDeporte(TipoDeporte.TENIS);

        Tarifa activa = tarifaValida(TipoDeporte.TENIS, 4, LocalTime.of(8, 0), LocalTime.of(9, 0), "10.00", true);
        Tarifa inactiva = tarifaValida(TipoDeporte.TENIS, 4, LocalTime.of(9, 0), LocalTime.of(10, 0), "10.00", false);

        when(pistaRepository.findById(11L)).thenReturn(Optional.of(pista));
        when(tarifaRepository.findByTipoDeporte(TipoDeporte.TENIS)).thenReturn(List.of(activa, inactiva));

        List<Tarifa> tarifas = tarifaService.obtenerPorPistaId(11L);

        assertEquals(1, tarifas.size());
        assertTrue(tarifas.get(0).isActiva());
    }

    private Tarifa tarifaValida(TipoDeporte deporte, int diaSemana, LocalTime inicio, LocalTime fin,
                                String precioHora, boolean activa) {
        Tarifa tarifa = new Tarifa();
        tarifa.setTipoDeporte(deporte);
        tarifa.setDiaSemana(diaSemana);
        tarifa.setHoraInicio(inicio);
        tarifa.setHoraFin(fin);
        tarifa.setPrecioPorHora(new BigDecimal(precioHora));
        tarifa.setVigenteDesde(LocalDate.of(2026, 1, 1));
        tarifa.setVigenteHasta(null);
        tarifa.setActiva(activa);
        return tarifa;
    }
}