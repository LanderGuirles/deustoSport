package com.deustosport.my_app.service;

import com.deustosport.my_app.entity.Tarifa;
import com.deustosport.my_app.enums.TipoDeporte;
import com.deustosport.my_app.repository.TarifaRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TarifaService {

    private static final BigDecimal DESCUENTO_SOCIO = new BigDecimal("0.20");

    private final TarifaRepository tarifaRepository;

    public TarifaService(TarifaRepository tarifaRepository) {
        this.tarifaRepository = tarifaRepository;
    }

    @Transactional(readOnly = true)
    public List<Tarifa> obtenerTodas() {
        return tarifaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Tarifa> obtenerActivas() {
        return tarifaRepository.findByActivaTrue();
    }

    @Transactional
    public Tarifa crearTarifa(Tarifa tarifa) {
        validarTarifa(tarifa);
        tarifa.setId(null);
        return tarifaRepository.save(tarifa);
    }

    @Transactional
    public Tarifa actualizarTarifa(Long id, Tarifa datos) {
        Objects.requireNonNull(id, "id no puede ser null");
        Tarifa existente = tarifaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tarifa no encontrada con ID: " + id));
        validarTarifa(datos);
        existente.setTipoDeporte(datos.getTipoDeporte());
        existente.setDiaSemana(datos.getDiaSemana());
        existente.setHoraInicio(datos.getHoraInicio());
        existente.setHoraFin(datos.getHoraFin());
        existente.setPrecioPorHora(datos.getPrecioPorHora());
        existente.setVigenteDesde(datos.getVigenteDesde());
        existente.setVigenteHasta(datos.getVigenteHasta());
        existente.setActiva(datos.isActiva());
        return tarifaRepository.save(existente);
    }

    @Transactional
    public void desactivarTarifa(Long id) {
        Objects.requireNonNull(id, "id no puede ser null");
        Tarifa tarifa = tarifaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tarifa no encontrada con ID: " + id));
        tarifa.setActiva(false);
        tarifaRepository.save(tarifa);
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularPrecio(TipoDeporte tipoDeporte, LocalDate fecha,
            LocalTime horaInicio, LocalTime horaFin, boolean esSocio) {
            
        int diaSemana = fecha.getDayOfWeek().getValue();


        List<Tarifa> tarifasActivas = tarifaRepository.findByActivaTrue();

        
        Tarifa tarifaAplicable = tarifasActivas.stream()
                .filter(t -> t.getTipoDeporte() == tipoDeporte)
                .filter(t -> t.getDiaSemana() == diaSemana)
                .filter(t -> t.getVigenteDesde() == null || !fecha.isBefore(t.getVigenteDesde()))
                .filter(t -> !horaInicio.isBefore(t.getHoraInicio()) && !horaFin.isAfter(t.getHoraFin()))
                .findFirst()
                .orElse(null);

        BigDecimal precio;

        if (tarifaAplicable != null) {
            long minutos = java.time.Duration.between(horaInicio, horaFin).toMinutes();
            BigDecimal horas = new BigDecimal(minutos)
                    .divide(new BigDecimal(60), 2, RoundingMode.HALF_UP);
            precio = tarifaAplicable.getPrecioPorHora().multiply(horas);
        } else {
            precio = calcularPrecioFallback(horaInicio, horaFin);
        }

        if (esSocio) {
            BigDecimal descuento = precio.multiply(DESCUENTO_SOCIO);
            precio = precio.subtract(descuento).setScale(2, RoundingMode.HALF_UP);
        }

        return precio;
    }

    private BigDecimal calcularPrecioFallback(LocalTime horaInicio, LocalTime horaFin) {
        long minutos = java.time.Duration.between(horaInicio, horaFin).toMinutes();
        BigDecimal horas = new BigDecimal(minutos)
                .divide(new BigDecimal(60), 2, RoundingMode.HALF_UP);
        return new BigDecimal("10.00").multiply(horas);
    }

    private void validarTarifa(Tarifa tarifa) {
        if (tarifa.getHoraFin().isBefore(tarifa.getHoraInicio()) ||
                tarifa.getHoraFin().equals(tarifa.getHoraInicio())) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la hora de inicio.");
        }
        if (tarifa.getPrecioPorHora().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio por hora debe ser mayor que 0.");
        }
        if (tarifa.getDiaSemana() < 1 || tarifa.getDiaSemana() > 7) {
            throw new IllegalArgumentException("El día de la semana debe estar entre 1 (lunes) y 7 (domingo).");
        }
    }
}