package com.deustosport.my_app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deustosport.my_app.entity.Configuracion;
import com.deustosport.my_app.repository.ConfiguracionRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class ConfiguracionServiceTest {

    @Mock
    private ConfiguracionRepository configuracionRepository;

    @InjectMocks
    private ConfiguracionService configuracionService;

    @Test
    void obtenerDescuentoSocioPorcentaje_sinConfiguracion_devuelveValorPorDefecto() {
        when(configuracionRepository.findById(ConfiguracionService.CLAVE_DESCUENTO_SOCIO))
                .thenReturn(Optional.empty());

        BigDecimal porcentaje = configuracionService.obtenerDescuentoSocioPorcentaje();

        assertEquals(new BigDecimal("20"), porcentaje);
    }

    @Test
    void obtenerDescuentoSocioPorcentaje_valorInvalido_devuelveValorPorDefecto() {
        when(configuracionRepository.findById(ConfiguracionService.CLAVE_DESCUENTO_SOCIO))
                .thenReturn(Optional.of(new Configuracion(
                        ConfiguracionService.CLAVE_DESCUENTO_SOCIO,
                        "xx",
                        "dato invalido")));

        BigDecimal porcentaje = configuracionService.obtenerDescuentoSocioPorcentaje();

        assertEquals(new BigDecimal("20"), porcentaje);
    }

    @Test
    void obtenerFactorDescuentoSocio_divideCorrectamente() {
        when(configuracionRepository.findById(ConfiguracionService.CLAVE_DESCUENTO_SOCIO))
                .thenReturn(Optional.of(new Configuracion(
                        ConfiguracionService.CLAVE_DESCUENTO_SOCIO,
                        "15",
                        "descuento")));

        BigDecimal factor = configuracionService.obtenerFactorDescuentoSocio();

        assertEquals(new BigDecimal("0.1500"), factor);
    }

    @Test
    void actualizarDescuentoSocio_fueraDeRango_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> configuracionService.actualizarDescuentoSocio(new BigDecimal("101")));
    }

    @Test
    void actualizarDescuentoSocio_actualizaYGuardaValor() {
        Configuracion existente = new Configuracion(
                ConfiguracionService.CLAVE_DESCUENTO_SOCIO,
                "20",
                "descuento");

        when(configuracionRepository.findById(ConfiguracionService.CLAVE_DESCUENTO_SOCIO))
                .thenReturn(Optional.of(existente));
        when(configuracionRepository.save(any(Configuracion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Configuracion actualizada = configuracionService.actualizarDescuentoSocio(new BigDecimal("12.5"));

        assertEquals("12.5", actualizada.getValor());
        verify(configuracionRepository).save(existente);
    }
}
