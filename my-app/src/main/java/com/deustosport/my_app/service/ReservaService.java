package com.deustosport.my_app.service;

import com.deustosport.my_app.dto.ReservaResponse;
import com.deustosport.my_app.entity.Pago;
import com.deustosport.my_app.entity.Pista;
import com.deustosport.my_app.entity.Reserva;
import com.deustosport.my_app.entity.Usuario;
import com.deustosport.my_app.enums.EstadoReserva;
import com.deustosport.my_app.enums.MetodoPago;
import com.deustosport.my_app.repository.PistaRepository;
import com.deustosport.my_app.repository.ReservaRepository;
import com.deustosport.my_app.repository.UsuarioRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class ReservaService {

    private static final Pattern PATRON_TARJETA = Pattern.compile("^\\d{16}$");
    private static final Pattern PATRON_CADUCIDAD = Pattern.compile("^(0[1-9]|1[0-2])/[0-9]{2}$");
    private static final Pattern PATRON_CVV = Pattern.compile("^\\d{3,4}$");
    private static final Pattern PATRON_BIZUM = Pattern.compile("^(\\+34)?[6789]\\d{8}$");

    private final ReservaRepository reservaRepository;
    private final PistaRepository pistaRepository;
    private final UsuarioRepository usuarioRepository;
    private final TarifaService tarifaService;
    private final PagoService pagoService;

    public ReservaService(ReservaRepository reservaRepository,
            PistaRepository pistaRepository,
            UsuarioRepository usuarioRepository,
            TarifaService tarifaService,
            @Lazy PagoService pagoService) {
        this.reservaRepository = reservaRepository;
        this.pistaRepository = pistaRepository;
        this.usuarioRepository = usuarioRepository;
        this.tarifaService = tarifaService;
        this.pagoService = pagoService;
    }

    @Transactional
    public Reserva crearReserva(Long usuarioId, Long pistaId, LocalDate fecha,
            LocalTime horaInicio, Integer duracionMinutos) {

        if (fecha.isBefore(LocalDate.now()) ||
                (fecha.isEqual(LocalDate.now()) && horaInicio.isBefore(LocalTime.now()))) {
            throw new IllegalArgumentException("No se pueden hacer reservas en fechas u horas pasadas.");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuario no encontrado con ID: " + usuarioId));

        Pista pista = pistaRepository.findById(pistaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Pista no encontrada con ID: " + pistaId));

        if (!pista.isActiva()) {
            throw new IllegalStateException("La pista seleccionada no está disponible para reservas.");
        }

        horaInicio = horaInicio.withSecond(0).withNano(0);
        LocalTime horaFin = horaInicio.plusMinutes(duracionMinutos);

        List<Reserva> conflictos = reservaRepository.findConflictingReservations(
                pistaId, fecha, horaInicio, horaFin);
        if (!conflictos.isEmpty()) {
            throw new IllegalStateException("La pista ya está reservada en el horario seleccionado.");
        }

        BigDecimal precioTotal = tarifaService.calcularPrecio(
                pista.getTipoDeporte(), fecha, horaInicio, horaFin, usuario.isEsSocio());

        Reserva nuevaReserva = new Reserva();
        nuevaReserva.setUsuario(usuario);
        nuevaReserva.setPista(pista);
        nuevaReserva.setFechaReserva(fecha);
        nuevaReserva.setHoraInicio(horaInicio);
        nuevaReserva.setHoraFin(horaFin);
        nuevaReserva.setPrecioTotal(precioTotal);
        nuevaReserva.setEstado(EstadoReserva.PENDIENTE);
        nuevaReserva.setCreditosUsados(0);

        return reservaRepository.save(nuevaReserva);
    }

    @Transactional
    public Reserva pagarReserva(Long reservaId, Long usuarioId, MetodoPago metodoPago,
            String numeroTarjeta, String titularTarjeta, String caducidadTarjeta,
            String cvv, String telefonoBizum, String iban) {

        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        if (!reserva.getUsuario().getId().equals(usuarioId)) {
            throw new SecurityException("No tienes permiso para pagar esta reserva.");
        }

        if (reserva.getEstado() == EstadoReserva.CANCELADA) {
            throw new IllegalStateException("No se puede pagar una reserva cancelada.");
        }

        if (reserva.getEstado() == EstadoReserva.CONFIRMADA || reserva.getFechaPago() != null) {
            throw new IllegalStateException("La reserva ya esta pagada.");
        }

        if (reserva.getFechaReserva().isBefore(LocalDate.now()) ||
                (reserva.getFechaReserva().isEqual(LocalDate.now()) &&
                        reserva.getHoraInicio().isBefore(LocalTime.now()))) {
            throw new IllegalStateException("No se puede pagar una reserva pasada.");
        }

        validarDatosPago(metodoPago, numeroTarjeta, titularTarjeta,
                caducidadTarjeta, cvv, telefonoBizum, iban);

        String ibanFinal = (iban != null && !iban.isBlank()) ? iban : "SIMULADO00000000000000";

        Pago pago = pagoService.procesarPagoInterno(reservaId, ibanFinal, metodoPago);

        reserva.setMetodoPago(metodoPago);
        reserva.setReferenciaPago(pago.getReferenciaPago());
        reserva.setFechaPago(pago.getFechaPago());
        reserva.setEstado(EstadoReserva.CONFIRMADA);

        return reservaRepository.save(reserva);
    }

    public List<ReservaResponse> obtenerMisReservas(Long usuarioId) {
        List<Reserva> reservas = reservaRepository.findByUsuarioId(usuarioId);
        return reservas.stream().map(reserva -> {
            ReservaResponse dto = new ReservaResponse();
            dto.setId(reserva.getId());
            dto.setUsuarioId(reserva.getUsuario().getId());
            dto.setPistaId(reserva.getPista() != null ? reserva.getPista().getId() : null);
            dto.setPistaNombre(reserva.getPista() != null ? reserva.getPista().getNombre() : "Pista sin nombre");
            dto.setTipoDeporte(reserva.getPista() != null ? reserva.getPista().getTipoDeporte() : null);
            dto.setFechaReserva(reserva.getFechaReserva());
            dto.setHoraInicio(reserva.getHoraInicio());
            dto.setHoraFin(reserva.getHoraFin());
            dto.setPrecioTotal(reserva.getPrecioTotal());
            dto.setEstado(reserva.getEstado());
            dto.setMetodoPago(reserva.getMetodoPago());
            dto.setReferenciaPago(reserva.getReferenciaPago());
            dto.setFechaPago(reserva.getFechaPago());
            return dto;
        }).toList();
    }

    @Transactional
    public Reserva cancelarReserva(Long reservaId, Long usuarioId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        if (!reserva.getUsuario().getId().equals(usuarioId)) {
            throw new SecurityException("No tienes permiso para cancelar esta reserva.");
        }

        if (reserva.getEstado() == EstadoReserva.CANCELADA) {
            throw new IllegalStateException("La reserva ya está cancelada.");
        }

        if (reserva.getFechaReserva().isBefore(LocalDate.now()) ||
                (reserva.getFechaReserva().isEqual(LocalDate.now()) &&
                        reserva.getHoraInicio().isBefore(LocalTime.now()))) {
            throw new IllegalStateException("No se pueden cancelar reservas pasadas.");
        }

        reserva.setEstado(EstadoReserva.CANCELADA);
        return reservaRepository.save(reserva);
    }

    public boolean consultarDisponibilidad(Long pistaId, LocalDate fecha,
            LocalTime horaInicio, LocalTime horaFin) {
        return reservaRepository.findConflictingReservations(
                pistaId, fecha, horaInicio, horaFin).isEmpty();
    }

    private void validarDatosPago(MetodoPago metodoPago, String numeroTarjeta,
            String titularTarjeta, String caducidadTarjeta, String cvv,
            String telefonoBizum, String iban) {
        if (metodoPago == null) {
            throw new IllegalArgumentException("Debes indicar un metodo de pago valido.");
        }
        if (metodoPago == MetodoPago.TARJETA) {
            String numeroLimpio = limpiar(numeroTarjeta).replace(" ", "");
            if (!PATRON_TARJETA.matcher(numeroLimpio).matches()) {
                throw new IllegalArgumentException("Numero de tarjeta invalido. Debe tener 16 digitos.");
            }
            if (limpiar(titularTarjeta).length() < 3) {
                throw new IllegalArgumentException("El titular de la tarjeta no es valido.");
            }
            if (!PATRON_CADUCIDAD.matcher(limpiar(caducidadTarjeta)).matches()) {
                throw new IllegalArgumentException("La caducidad debe tener formato MM/AA.");
            }
            if (!PATRON_CVV.matcher(limpiar(cvv)).matches()) {
                throw new IllegalArgumentException("El CVV es invalido.");
            }
        }
        if (metodoPago == MetodoPago.BIZUM) {
            String telefonoLimpio = limpiar(telefonoBizum).replace(" ", "");
            if (!PATRON_BIZUM.matcher(telefonoLimpio).matches()) {
                throw new IllegalArgumentException("Telefono Bizum invalido.");
            }
        }
        if (metodoPago == MetodoPago.TRANSFERENCIA) {
            if (iban == null || iban.isBlank()) {
                throw new IllegalArgumentException("El IBAN es obligatorio para transferencia.");
            }
            String ibanLimpio = iban.toUpperCase().replaceAll("\\s", "");
            if (!ibanLimpio.matches("^[A-Z]{2}[0-9]{2}[A-Z0-9]{4,30}$")) {
                throw new IllegalArgumentException(
                        "Formato de IBAN no valido. Ejemplo: ES9121000418450200051332");
            }
        }
    }

    private String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }
}