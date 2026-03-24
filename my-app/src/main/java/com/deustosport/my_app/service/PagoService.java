package com.deustosport.my_app.service;

import com.deustosport.my_app.entity.Pago;
import com.deustosport.my_app.entity.Reserva;
import com.deustosport.my_app.enums.EstadoPago;
import com.deustosport.my_app.enums.EstadoReserva;
import com.deustosport.my_app.enums.MetodoPago;
import com.deustosport.my_app.repository.PagoRepository;
import com.deustosport.my_app.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
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
    public Pago procesarPago(Long reservaId, String iban, MetodoPago metodoPago) {

        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Reserva no encontrada con ID: " + reservaId));

        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new IllegalStateException(
                    "Solo se pueden pagar reservas en estado PENDIENTE. Estado actual: "
                            + reserva.getEstado());
        }

        if (pagoRepository.findByReservaId(reservaId).isPresent()) {
            throw new IllegalStateException("Esta reserva ya tiene un pago registrado.");
        }

        validarFormatoIban(iban);

        Pago pago = new Pago();
        pago.setReserva(reserva);
        pago.setIban(iban.toUpperCase().replaceAll("\\s", ""));
        pago.setMetodoPago(metodoPago);
        pago.setImporte(reserva.getPrecioTotal());
        pago.setFechaPago(LocalDateTime.now());
        pago.setEstadoPago(EstadoPago.PROCESANDO);
        pago.setReferenciaPago("DS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        pagoRepository.save(pago);

        // Simulamos que el pago siempre se aprueba
        pago.setEstadoPago(EstadoPago.COMPLETADO);
        pagoRepository.save(pago);

        reserva.setEstado(EstadoReserva.CONFIRMADA);
        reservaRepository.save(reserva);

        return pago;
    }

    private void validarFormatoIban(String iban) {
        if (iban == null || iban.isBlank()) {
            throw new IllegalArgumentException("El IBAN es obligatorio.");
        }
        String ibanLimpio = iban.toUpperCase().replaceAll("\\s", "");
        if (!ibanLimpio.matches("^[A-Z]{2}[0-9]{2}[A-Z0-9]{4,30}$")) {
            throw new IllegalArgumentException(
                    "Formato de IBAN no válido. Ejemplo correcto: ES9121000418450200051332");
        }
    }
}