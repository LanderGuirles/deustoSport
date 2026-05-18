package com.deustosport.my_app.service;

import com.deustosport.my_app.entity.Pista;
import com.deustosport.my_app.entity.Tarifa;
import com.deustosport.my_app.enums.TipoDeporte;
import com.deustosport.my_app.repository.PistaRepository;
import com.deustosport.my_app.repository.TarifaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

@Service
public class TarifaService {

    private final TarifaRepository tarifaRepository;
    private final PistaRepository  pistaRepository;
    private final ConfiguracionService configuracionService;

    public TarifaService(TarifaRepository tarifaRepository,
                         PistaRepository pistaRepository,
                         ConfiguracionService configuracionService) {
        this.tarifaRepository    = tarifaRepository;
        this.pistaRepository     = pistaRepository;
        this.configuracionService = configuracionService;
    }

    @Transactional(readOnly = true)
    public List<Tarifa> obtenerTodas() {
        return tarifaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Tarifa> obtenerActivas() {
        return tarifaRepository.findByActivaTrue();
    }

    @Transactional(readOnly = true)
    public List<Tarifa> obtenerPorPistaId(Long pistaId) {
        Objects.requireNonNull(pistaId, "pistaId no puede ser null");
        Pista pista = pistaRepository.findById(pistaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Pista no encontrada con ID: " + pistaId));
        return tarifaRepository.findActiveByDeporteDiaAndFecha(
                pista.getTipoDeporte(),
                LocalDate.now().getDayOfWeek().getValue(),
                LocalDate.now(),
                pista.getPolideportivo().getId()
        );
    }

    @Transactional(readOnly = true)
    public List<Tarifa> obtenerPorPolideportivo(Long polideportivoId) {
        return tarifaRepository.findByPolideportivoId(polideportivoId);
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
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tarifa no encontrada con ID: " + id));
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
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tarifa no encontrada con ID: " + id));
        tarifa.setActiva(false);
        tarifaRepository.save(tarifa);
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularPrecio(TipoDeporte tipoDeporte, LocalDate fecha,
                                     LocalTime horaInicio, LocalTime horaFin,
                                     boolean esSocio, Long polideportivoId) {
        int diaSemana = fecha.getDayOfWeek().getValue();

        List<Tarifa> tarifasActivas = tarifaRepository
                .findActiveByDeporteDiaAndFecha(tipoDeporte, diaSemana, fecha, polideportivoId);

        BigDecimal precio = BigDecimal.ZERO;
        long minutosCubiertos = 0;

        for (Tarifa tarifa : tarifasActivas) {
            LocalTime inicioSolape = horaInicio.isAfter(tarifa.getHoraInicio())
                    ? horaInicio
                    : tarifa.getHoraInicio();
            LocalTime finSolape = horaFin.isBefore(tarifa.getHoraFin())
                    ? horaFin
                    : tarifa.getHoraFin();

            if (!finSolape.isAfter(inicioSolape)) {
                continue;
            }

            long minutos = java.time.Duration.between(inicioSolape, finSolape).toMinutes();
            if (minutos <= 0) {
                continue;
            }

            BigDecimal horas = new BigDecimal(minutos)
                    .divide(new BigDecimal(60), 4, RoundingMode.HALF_UP);
            BigDecimal subPrecio = tarifa.getPrecioPorHora().multiply(horas);

            precio = precio.add(subPrecio);
            minutosCubiertos += minutos;
        }

        long minutosSolicitados = java.time.Duration.between(horaInicio, horaFin).toMinutes();
        if (minutosCubiertos < minutosSolicitados || precio.compareTo(BigDecimal.ZERO) <= 0) {
            precio = calcularFallback(horaInicio, horaFin);
        }

        if (esSocio) {
            BigDecimal factor = configuracionService.obtenerFactorDescuentoSocio();
            BigDecimal descuento = precio.multiply(factor);
            precio = precio.subtract(descuento);
        }

        return precio.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcularFallback(LocalTime horaInicio, LocalTime horaFin) {
        long min = java.time.Duration.between(horaInicio, horaFin).toMinutes();
        BigDecimal h = new BigDecimal(min)
                .divide(new BigDecimal(60), 2, RoundingMode.HALF_UP);
        return new BigDecimal("10.00").multiply(h);
    }

    private void validarTarifa(Tarifa tarifa) {
        if (tarifa.getHoraFin().isBefore(tarifa.getHoraInicio()) ||
                tarifa.getHoraFin().equals(tarifa.getHoraInicio())) {
            throw new IllegalArgumentException(
                    "La hora de fin debe ser posterior a la hora de inicio.");
        }
        if (tarifa.getPrecioPorHora().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "El precio por hora debe ser mayor que 0.");
        }
        if (tarifa.getDiaSemana() < 1 || tarifa.getDiaSemana() > 7) {
            throw new IllegalArgumentException(
                    "El día de la semana debe estar entre 1 (lunes) y 7 (domingo).");
        }
    }
}