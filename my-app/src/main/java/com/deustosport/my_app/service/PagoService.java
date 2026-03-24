package com.deustosport.my_app.service;

import com.deustosport.my_app.entity.Pago;
import com.deustosport.my_app.entity.Reserva;
import com.deustosport.my_app.enums.EstadoPago;
import com.deustosport.my_app.enums.MetodoPago;
import com.deustosport.my_app.repository.PagoRepository;
import com.deustosport.my_app.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
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
    public Pago obtenerPagoPorReserva(Long reservaId) {
        return pagoRepository.findByReservaId(reservaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe pago para la reserva con ID: " + reservaId));
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
        // Solo se guarda el IBAN si el método es transferencia
        if (metodoPago == MetodoPago.TRANSFERENCIA && iban != null && !iban.isBlank()) {
            pago.setIban(iban.toUpperCase().replaceAll("\\s", ""));
        }
        pago.setMetodoPago(metodoPago);
        pago.setImporte(reserva.getPrecioTotal());
        pago.setFechaPago(LocalDateTime.now());
        pago.setEstadoPago(EstadoPago.PROCESANDO);
        pago.setReferenciaPago("DS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        pagoRepository.save(pago);

        pago.setEstadoPago(EstadoPago.COMPLETADO);
        pagoRepository.save(pago);

        return pago;
    }
}