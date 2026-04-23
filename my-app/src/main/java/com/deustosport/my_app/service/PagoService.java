package com.deustosport.my_app.service;

import com.deustosport.my_app.dto.PagoResponse;
import com.deustosport.my_app.entity.Pago;
import com.deustosport.my_app.entity.Reserva;
import com.deustosport.my_app.enums.EstadoPago;
import com.deustosport.my_app.enums.MetodoPago;
import com.deustosport.my_app.repository.PagoRepository;
import com.deustosport.my_app.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class PagoService {

    private final PagoRepository pagoRepository;
    private final ReservaRepository reservaRepository;

    public PagoService(PagoRepository pagoRepository, ReservaRepository reservaRepository) {
        this.pagoRepository = pagoRepository;
        this.reservaRepository = reservaRepository;
    }

    @Transactional(readOnly = true)
    public PagoResponse obtenerPagoPorReserva(Long reservaId) {
        Pago pago = pagoRepository.findByReservaId(reservaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe pago para la reserva con ID: " + reservaId));

        PagoResponse dto = new PagoResponse();
        dto.setReferenciaPago(pago.getReferenciaPago());
        dto.setImporte(pago.getImporte());
        dto.setMetodoPago(pago.getMetodoPago());
        dto.setEstadoPago(pago.getEstadoPago());
        dto.setIban(pago.getIban() != null ? pago.getIban() : "");
        dto.setFechaPago(pago.getFechaPago());
        return dto;
    }

    @Transactional
    public Pago procesarPagoInterno(Long reservaId, String iban, MetodoPago metodoPago) {
        Objects.requireNonNull(reservaId, "reservaId no puede ser null");

        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Reserva no encontrada con ID: " + reservaId));

        if (pagoRepository.findByReservaId(reservaId).isPresent()) {
            throw new IllegalStateException("Esta reserva ya tiene un pago registrado.");
        }

        Pago pago = new Pago();
        pago.setReserva(reserva);
        pago.setMetodoPago(metodoPago);
        pago.setImporte(reserva.getPrecioTotal());
        pago.setFechaPago(LocalDateTime.now());
        pago.setEstadoPago(EstadoPago.PROCESANDO);
        pago.setReferenciaPago("DS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        // IBAN: solo relevante para transferencia, pero la columna puede ser null.
        // Para otros métodos almacenamos una cadena vacía para evitar NOT NULL en BBDDs antiguas.
        if (metodoPago == MetodoPago.TRANSFERENCIA && iban != null && !iban.isBlank()) {
            pago.setIban(iban.toUpperCase().replaceAll("\\s", ""));
        } else {
            pago.setIban(""); // columna nullable en schema nuevo; cadena vacía como fallback
        }

        pagoRepository.save(pago);

        pago.setEstadoPago(EstadoPago.COMPLETADO);
        pagoRepository.save(pago);

        return pago;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obtenerRecaudacionMesActual() {
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicioMes = hoy.withDayOfMonth(1).atStartOfDay();
        LocalDateTime inicioMesSiguiente = inicioMes.plusMonths(1);

        BigDecimal total = pagoRepository.sumImporteByEstadoAndRangoFechas(
                EstadoPago.COMPLETADO, inicioMes, inicioMesSiguiente);
        long totalPagos = pagoRepository.countByEstadoAndRangoFechas(
                EstadoPago.COMPLETADO, inicioMes, inicioMesSiguiente);

        Month mes = hoy.getMonth();
        String nombreMes = mes.getDisplayName(TextStyle.FULL, new Locale("es", "ES"));

        return Map.of(
                "anio", hoy.getYear(),
                "mes", hoy.getMonthValue(),
                "mesNombre", nombreMes,
                "totalRecaudado", (total != null ? total : BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP),
                "totalPagos", totalPagos,
                "moneda", "EUR");
    }
}