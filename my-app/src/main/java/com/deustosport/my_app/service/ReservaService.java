package com.deustosport.my_app.service;

import com.deustosport.my_app.dto.ReservaResponse;
import com.deustosport.my_app.entity.Pago;
import com.deustosport.my_app.entity.Pista;
import com.deustosport.my_app.entity.Reserva;
import com.deustosport.my_app.entity.Usuario;
import com.deustosport.my_app.service.EmailService;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ReservaService {

    private static final Pattern PATRON_TARJETA  = Pattern.compile("^\\d{16}$");
    private static final Pattern PATRON_CADUCIDAD = Pattern.compile("^(0[1-9]|1[0-2])/[0-9]{2}$");
    private static final Pattern PATRON_CVV       = Pattern.compile("^\\d{3,4}$");
    private static final Pattern PATRON_BIZUM     = Pattern.compile("^(\\+34)?[6789]\\d{8}$");

    private final ReservaRepository  reservaRepository;
    private final PistaRepository    pistaRepository;
    private final UsuarioRepository  usuarioRepository;
    private final EmailService       emailService;
    private final TarifaService      tarifaService;
    private final PagoService        pagoService;

    public ReservaService(ReservaRepository reservaRepository,
                          PistaRepository pistaRepository,
                          UsuarioRepository usuarioRepository,
                          EmailService emailService,
                          TarifaService tarifaService,
                          @Lazy PagoService pagoService) {
        this.reservaRepository = reservaRepository;
        this.pistaRepository   = pistaRepository;
        this.usuarioRepository = usuarioRepository;
        this.emailService      = emailService;
        this.tarifaService     = tarifaService;
        this.pagoService       = pagoService;
    }

    // ─── Crear reserva ────────────────────────────────────────────────────────
    @Transactional
    public Reserva crearReserva(Long usuarioId, Long pistaId,
                                LocalDate fecha, LocalTime horaInicio,
                                Integer duracionMinutos) {
        Objects.requireNonNull(usuarioId, "usuarioId no puede ser null");
        Objects.requireNonNull(pistaId,   "pistaId no puede ser null");

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

        validarHorarioInstalacion(pista, horaInicio, horaFin);

        List<Reserva> conflictos = reservaRepository.findConflictingReservations(
                pistaId, fecha, horaInicio, horaFin);
        if (!conflictos.isEmpty()) {
            throw new IllegalStateException("La pista ya está reservada en el horario seleccionado.");
        }

        BigDecimal precioTotal = tarifaService.calcularPrecio(
                pista.getTipoDeporte(), fecha, horaInicio, horaFin, usuario.isEsSocio());

        Reserva r = new Reserva();
        r.setUsuario(usuario);
        r.setPista(pista);
        r.setFechaReserva(fecha);
        r.setHoraInicio(horaInicio);
        r.setHoraFin(horaFin);
        r.setPrecioTotal(precioTotal);
        r.setEstado(EstadoReserva.PENDIENTE);
        r.setCreditosUsados(0);

        return reservaRepository.save(r);
    }

    // ─── Pagar reserva ────────────────────────────────────────────────────────
    @Transactional
    public Reserva pagarReserva(Long reservaId, Long usuarioId,
                                MetodoPago metodoPago,
                                String numeroTarjeta, String titularTarjeta,
                                String caducidadTarjeta, String cvv,
                                String telefonoBizum, String iban) {
        Objects.requireNonNull(reservaId, "reservaId no puede ser null");
        Objects.requireNonNull(usuarioId, "usuarioId no puede ser null");

        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        if (!reserva.getUsuario().getId().equals(usuarioId)) {
            throw new SecurityException("No tienes permiso para pagar esta reserva.");
        }
        if (reserva.getEstado() == EstadoReserva.CANCELADA) {
            throw new IllegalStateException("No se puede pagar una reserva cancelada.");
        }
        if (reserva.getEstado() == EstadoReserva.CONFIRMADA || reserva.getFechaPago() != null) {
            throw new IllegalStateException("La reserva ya está pagada.");
        }
        if (reserva.getFechaReserva().isBefore(LocalDate.now()) ||
                (reserva.getFechaReserva().isEqual(LocalDate.now()) &&
                        reserva.getHoraInicio().isBefore(LocalTime.now()))) {
            throw new IllegalStateException("No se puede pagar una reserva pasada.");
        }

        validarDatosPago(metodoPago, numeroTarjeta, titularTarjeta,
                caducidadTarjeta, cvv, telefonoBizum, iban);

        String ibanFinal = (iban != null && !iban.isBlank())
                ? iban
                : "SIMULADO00000000000000";

        Pago pago = pagoService.procesarPagoInterno(reservaId, ibanFinal, metodoPago);

        reserva.setMetodoPago(metodoPago);
        reserva.setReferenciaPago(pago.getReferenciaPago());
        reserva.setFechaPago(pago.getFechaPago());
        reserva.setEstado(EstadoReserva.CONFIRMADA);

        Reserva reservaFinal = reservaRepository.save(reserva);

        // Enviar correo de confirmación de reserva
        emailService.enviarEmailConfirmacionReserva(
            reservaFinal.getUsuario().getEmail(),
            reservaFinal.getPista().getNombre(),
            reservaFinal.getPista().getTipoDeporte().name(),
            reservaFinal.getFechaReserva(),
            reservaFinal.getHoraInicio(),
            reservaFinal.getHoraFin(),
            reservaFinal.getPrecioTotal());

        return reservaFinal;
    }

    // ─── Mis reservas ─────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<ReservaResponse> obtenerMisReservas(Long usuarioId) {
        List<Reserva> reservas = reservaRepository.findByUsuarioId(usuarioId);
        return reservas.stream().map(this::toDto).collect(Collectors.toList());
    }

    // ─── Cancelar reserva ─────────────────────────────────────────────────────
    @Transactional
    public Reserva cancelarReserva(Long reservaId, Long usuarioId) {
        Objects.requireNonNull(reservaId, "reservaId no puede ser null");
        Objects.requireNonNull(usuarioId, "usuarioId no puede ser null");

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

    // ─── Disponibilidad ───────────────────────────────────────────────────────
    public boolean consultarDisponibilidad(Long pistaId, LocalDate fecha,
                                           LocalTime horaInicio, LocalTime horaFin) {
        Objects.requireNonNull(pistaId, "pistaId no puede ser null");

        Pista pista = pistaRepository.findById(pistaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Pista no encontrada con ID: " + pistaId));

        if (!estaDentroDeHorario(pista, horaInicio, horaFin)) return false;

        return reservaRepository.findConflictingReservations(
                pistaId, fecha, horaInicio, horaFin).isEmpty();
    }

    // ─── Slots ocupados por pista y rango de fechas (para el calendario) ──────
    @Transactional(readOnly = true)
    public List<Map<String, String>> obtenerOcupadosPorPistaYRango(
            Long pistaId, LocalDate inicio, LocalDate fin) {

        return reservaRepository.findActivasByPistaAndRango(pistaId, inicio, fin)
                .stream()
                .map(r -> {
                    Map<String, String> m = new LinkedHashMap<>();
                    m.put("fecha",      r.getFechaReserva().toString());
                    m.put("horaInicio", r.getHoraInicio().toString());
                    m.put("horaFin",    r.getHoraFin().toString());
                    return m;
                })
                .collect(Collectors.toList());
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────
    private ReservaResponse toDto(Reserva r) {
        ReservaResponse dto = new ReservaResponse();
        dto.setId(r.getId());
        dto.setUsuarioId(r.getUsuario().getId());
        dto.setPistaId(r.getPista() != null ? r.getPista().getId() : null);
        dto.setPistaNombre(r.getPista() != null ? r.getPista().getNombre() : "—");
        dto.setTipoDeporte(r.getPista() != null ? r.getPista().getTipoDeporte() : null);
        dto.setFechaReserva(r.getFechaReserva());
        dto.setHoraInicio(r.getHoraInicio());
        dto.setHoraFin(r.getHoraFin());
        dto.setPrecioTotal(r.getPrecioTotal());
        dto.setEstado(r.getEstado());
        dto.setMetodoPago(r.getMetodoPago());
        dto.setReferenciaPago(r.getReferenciaPago());
        dto.setFechaPago(r.getFechaPago());
        return dto;
    }

    private void validarHorarioInstalacion(Pista pista,
                                           LocalTime horaInicio, LocalTime horaFin) {
        if (!estaDentroDeHorario(pista, horaInicio, horaFin)) {
            LocalTime ap = pista.getInstalacion().getHoraApertura();
            LocalTime ci = pista.getInstalacion().getHoraCierre();
            throw new IllegalArgumentException(
                    "Horario fuera del horario general de la instalación (" + ap + " - " + ci + ").");
        }
    }

    private boolean estaDentroDeHorario(Pista pista,
                                        LocalTime horaInicio, LocalTime horaFin) {
        if (pista.getInstalacion() == null) return true;
        LocalTime ap = pista.getInstalacion().getHoraApertura();
        LocalTime ci = pista.getInstalacion().getHoraCierre();
        if (ap == null || ci == null) return true;
        return !horaInicio.isBefore(ap) && !horaFin.isAfter(ci);
    }

    private void validarDatosPago(MetodoPago metodoPago,
                                  String numeroTarjeta, String titularTarjeta,
                                  String caducidadTarjeta, String cvv,
                                  String telefonoBizum, String iban) {
        if (metodoPago == null) {
            throw new IllegalArgumentException("Debes indicar un método de pago válido.");
        }
        if (metodoPago == MetodoPago.TARJETA) {
            String num = limpiar(numeroTarjeta).replace(" ", "");
            if (!PATRON_TARJETA.matcher(num).matches())
                throw new IllegalArgumentException("Número de tarjeta inválido. Debe tener 16 dígitos.");
            if (limpiar(titularTarjeta).length() < 3)
                throw new IllegalArgumentException("El titular de la tarjeta no es válido.");
            if (!PATRON_CADUCIDAD.matcher(limpiar(caducidadTarjeta)).matches())
                throw new IllegalArgumentException("La caducidad debe tener formato MM/AA.");
            if (!PATRON_CVV.matcher(limpiar(cvv)).matches())
                throw new IllegalArgumentException("El CVV es inválido.");
        }
        if (metodoPago == MetodoPago.BIZUM) {
            String tel = limpiar(telefonoBizum).replace(" ", "");
            if (!PATRON_BIZUM.matcher(tel).matches())
                throw new IllegalArgumentException("Teléfono Bizum inválido.");
        }
        if (metodoPago == MetodoPago.TRANSFERENCIA) {
            if (iban == null || iban.isBlank())
                throw new IllegalArgumentException("El IBAN es obligatorio para transferencia.");
            String ibanL = iban.toUpperCase().replaceAll("\\s", "");
            if (!ibanL.matches("^[A-Z]{2}[0-9]{2}[A-Z0-9]{4,30}$"))
                throw new IllegalArgumentException(
                        "Formato de IBAN no válido. Ejemplo: ES9121000418450200051332");
        }
    }

    private String limpiar(String v) {
        return v == null ? "" : v.trim();
    }
}