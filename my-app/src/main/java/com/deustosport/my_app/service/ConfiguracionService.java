package com.deustosport.my_app.service;

import com.deustosport.my_app.entity.Configuracion;
import com.deustosport.my_app.repository.ConfiguracionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ConfiguracionService {

    /** Clave en la tabla configuracion para el porcentaje de descuento socio */
    public static final String CLAVE_DESCUENTO_SOCIO = "descuento_socio_porcentaje";

    /** Valor por defecto si no existe en BD (20%) */
    private static final BigDecimal DESCUENTO_POR_DEFECTO = new BigDecimal("20");

    private final ConfiguracionRepository configuracionRepository;

    public ConfiguracionService(ConfiguracionRepository configuracionRepository) {
        this.configuracionRepository = configuracionRepository;
    }

    /**
     * Devuelve el porcentaje de descuento de socio configurado (ej. 20 para 20%).
     */
    @Transactional(readOnly = true)
    public BigDecimal obtenerDescuentoSocioPorcentaje() {
        return configuracionRepository.findById(CLAVE_DESCUENTO_SOCIO)
                .map(c -> {
                    try {
                        return new BigDecimal(c.getValor());
                    } catch (NumberFormatException e) {
                        return DESCUENTO_POR_DEFECTO;
                    }
                })
                .orElse(DESCUENTO_POR_DEFECTO);
    }

    /**
     * Devuelve el factor de descuento (ej. 0.20 para un 20%).
     */
    @Transactional(readOnly = true)
    public BigDecimal obtenerFactorDescuentoSocio() {
        return obtenerDescuentoSocioPorcentaje()
                .divide(new BigDecimal("100"), 4, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Actualiza el porcentaje de descuento de socio.
     * @param porcentaje valor entre 0 y 100
     */
    @Transactional
    public Configuracion actualizarDescuentoSocio(BigDecimal porcentaje) {
        if (porcentaje == null || porcentaje.compareTo(BigDecimal.ZERO) < 0
                || porcentaje.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException(
                    "El porcentaje de descuento debe estar entre 0 y 100.");
        }

        Configuracion config = configuracionRepository.findById(CLAVE_DESCUENTO_SOCIO)
                .orElse(new Configuracion(CLAVE_DESCUENTO_SOCIO, "20",
                        "Porcentaje de descuento aplicado a socios en reservas"));

        config.setValor(porcentaje.toPlainString());
        return configuracionRepository.save(config);
    }

    /**
     * Obtiene todas las configuraciones del sistema.
     */
    @Transactional(readOnly = true)
    public List<Configuracion> obtenerTodas() {
        return configuracionRepository.findAll();
    }
}
