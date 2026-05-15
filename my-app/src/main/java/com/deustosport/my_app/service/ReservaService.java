package com.deustosport.my_app.service;

import com.deustosport.my_app.dto.ReservaResponse;
import com.deustosport.my_app.entity.AbonoUsuario;
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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ReservaService {

    private static final int ANTELACION_MINIMA_HORAS = 24;

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
    private final NotificacionService notificacionService;
    private final AbonoUsuarioService abonoUsuarioService; // NUEVA INYECCIÓN
    private final QRCodeService      qrCodeService;
    private final FestivoService     festivoService;

    public ReservaService(ReservaRepository reservaRepository,
                          PistaRepository pistaRepository,
                          UsuarioRepository usuarioRepository,
                          EmailService emailService,
                          TarifaService tarifaService,
                          @Lazy PagoService pagoService,
                          NotificacionService notificacionService,
                          AbonoUsuarioService abonoUsuarioService,
                          QRCodeService qrCodeService,
                          @Lazy FestivoService festivoService) {
        this.reservaRepository = reservaRepository;
        this.pistaRepository   = pistaRepository;
        this.usuarioRepository = usuarioRepository;
        this.emailService      = emailService;
        this.tarifaService     = tarifaService;
        this.pagoService       = pagoService;
        this.notificacionService = notificacionService;
        this.abonoUsuarioService = abonoUsuarioService; // INICIALIZACIÓN
        this.qrCodeService = qrCodeService;
        this.festivoService = festivoService;
    }

    // ─── Crear reserva ────────────────────────────────────────────────────────
    @Transactional
    public Reserva crearReserva(Long usuarioId, Long pistaId,
                                LocalDate fecha, LocalTime horaInicio,
                                Integer duracionMinutos) {
        Objects.requireNonNull(usuarioId, "usuarioId no puede ser null");
        Objects.requireNonNull(pistaId,   "pistaId no puede ser null");

        if (festivoService.esFechaFestiva(fecha)) {
            throw new IllegalStateException("El centro deportivo está cerrado por festividad/cierre programado el día " + fecha);
        }

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

        // ---> LÓGICA NUEVA: APLICAR DESCUENTO DE ABONO <---
        precioTotal = aplicarDescuentoAbonoSiExiste(usuarioId, precioTotal);

        Reserva r = new Reserva();
        r.setUsuario(usuario);
        r.setPista(pista);
        r.setFechaReserva(fecha);
        r.setHoraInicio(horaInicio);
        r.setHoraFin(horaFin);
        r.setPrecioTotal(precioTotal);
        Reserva reservaGuardada = reservaRepository.save(r);

        // Enviar email con detalles de la reserva y código QR
        emailService.enviarEmailCreacionReserva(
            reservaGuardada.getUsuario().getEmail(),
            reservaGuardada.getPista().getNombre(),
            reservaGuardada.getPista().getTipoDeporte().name(),
            reservaGuardada.getFechaReserva(),
            reservaGuardada.getHoraInicio(),
            reservaGuardada.getHoraFin(),
            reservaGuardada.getPrecioTotal(),
            reservaGuardada.getId(),
            reservaGuardada.getUsuario().getId());

        return reservaGuardada;
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

        if (metodoPago == MetodoPago.BILLETERA) {
            boolean esSocioEfectivo = reserva.getUsuario().isEsSocio() || 
                                     abonoUsuarioService.obtenerAbonoActivo(reserva.getUsuario().getId()).isPresent();
            if (!esSocioEfectivo) {
                throw new IllegalStateException("Los usuarios no socios no pueden pagar con billetera.");
            }
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

        if (metodoPago == MetodoPago.BILLETERA &&
            reserva.getUsuario().getBilletera().compareTo(reserva.getPrecioTotal()) < 0) {
            throw new IllegalStateException(
                "Saldo insuficiente. Tu billetera tiene " +
                reserva.getUsuario().getBilletera().toPlainString() + "€ pero la reserva cuesta " +
                reserva.getPrecioTotal().toPlainString() + "€.");
        }

        Pago pago = pagoService.procesarPagoInterno(reservaId, ibanFinal, metodoPago);

        reserva.setMetodoPago(metodoPago);
        reserva.setReferenciaPago(pago.getReferenciaPago());
        reserva.setFechaPago(pago.getFechaPago());
        reserva.setEstado(EstadoReserva.CONFIRMADA);

        Reserva reservaFinal = reservaRepository.save(reserva);

        // Generar QR y setear URL
        try {
            qrCodeService.generateAndSaveReservaQR(reservaFinal.getId(), reservaFinal.getUsuario().getId(),
                    reservaFinal.getPista().getNombre(), reservaFinal.getFechaReserva().toString(), reservaFinal.getHoraInicio().toString());
            String qrUrl = "/api/qr/reserva_" + reservaFinal.getId() + ".png";
            reservaFinal.setQrUrl(qrUrl);
            reservaRepository.save(reservaFinal);
        } catch (Exception e) {
            // Log but don't fail if QR generation fails
        }

        // Solo se mueve saldo de billetera cuando el metodo de pago es BILLETERA.
        if (metodoPago == MetodoPago.BILLETERA) {
            Usuario usuario = reserva.getUsuario();
            BigDecimal nuevoSaldo = usuario.getBilletera().subtract(reserva.getPrecioTotal());
            usuario.setBilletera(nuevoSaldo);
            usuarioRepository.save(usuario);
        }

        // Enviar correo de confirmación de reserva
        emailService.enviarEmailConfirmacionReserva(
            reservaFinal.getUsuario().getEmail(),
            reservaFinal.getPista().getNombre(),
            reservaFinal.getPista().getTipoDeporte().name(),
            reservaFinal.getFechaReserva(),
            reservaFinal.getHoraInicio(),
            reservaFinal.getHoraFin(),
            reservaFinal.getPrecioTotal(),
            reservaFinal.getId(),
            reservaFinal.getUsuario().getId());

        notificacionService.crearNotificacionReservaConfirmada(reservaFinal);

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
        if (!cumpleAntelacionMinima24h(reserva.getFechaReserva(), reserva.getHoraInicio())) {
            throw new IllegalStateException("Solo se puede cancelar una reserva con mas de 24 horas de antelacion.");
        }

        // Reembolsar si la reserva estaba confirmada (pagada)
        if (reserva.getEstado() == EstadoReserva.CONFIRMADA &&
            reserva.getPrecioTotal() != null &&
            reserva.getMetodoPago() != null) {

            if (reserva.getMetodoPago() == MetodoPago.BILLETERA) {
                // Reembolso directo a billetera
                Usuario usuario = reserva.getUsuario();
                BigDecimal nuevoSaldo = usuario.getBilletera().add(reserva.getPrecioTotal());
                usuario.setBilletera(nuevoSaldo);
                usuarioRepository.save(usuario);
            } else {
                // Reembolso externo (TARJETA, BIZUM, TRANSFERENCIA)
                // Se notifica al usuario que el reembolso se procesará por el mismo método
                String metodoTexto = reserva.getMetodoPago().name().toLowerCase();
                String titulo = "Reembolso en proceso";
                String mensaje = "Se ha iniciado la devolución de " + reserva.getPrecioTotal().toPlainString()
                        + "€ por " + metodoTexto + " de tu reserva en " + reserva.getPista().getNombre()
                        + " del " + reserva.getFechaReserva() + " a las " + reserva.getHoraInicio()
                        + ". El reembolso se completará en 3-5 días hábiles.";
                notificacionService.notificarIncidenciaReserva(reservaId, titulo, mensaje, true);
            }
        }

        reserva.setEstado(EstadoReserva.CANCELADA);
        return reservaRepository.save(reserva);
    }

    @Transactional
    public void cancelarReservaPorBloqueo(Long reservaId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        if (reserva.getEstado() == EstadoReserva.CANCELADA) {
            return;
        }

        // Solo cancelar si la reserva es futura
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime inicioReserva = LocalDateTime.of(reserva.getFechaReserva(), reserva.getHoraInicio());
        if (inicioReserva.isBefore(ahora)) {
            return;
        }

        // Reembolsar siempre si la reserva estaba pagada, ignorando la regla de las 24h
        if (reserva.getEstado() == EstadoReserva.CONFIRMADA &&
                reserva.getPrecioTotal() != null &&
                reserva.getMetodoPago() != null) {

            if (reserva.getMetodoPago() == MetodoPago.BILLETERA) {
                Usuario usuario = reserva.getUsuario();
                BigDecimal nuevoSaldo = usuario.getBilletera().add(reserva.getPrecioTotal());
                usuario.setBilletera(nuevoSaldo);
                usuarioRepository.save(usuario);
            }
            // Para métodos externos, el reembolso se notifica en el mensaje
        }

        reserva.setEstado(EstadoReserva.CANCELADA);
        reservaRepository.save(reserva);

        // Notificar al usuario
        String titulo = "Reserva cancelada por mantenimiento";
        String reembolsoTexto = "";
        if (reserva.getMetodoPago() == MetodoPago.BILLETERA) {
            reembolsoTexto = "Se ha reembolsado el importe en tu billetera.";
        } else if (reserva.getMetodoPago() != null) {
            reembolsoTexto = "Se procesará la devolución de " + reserva.getPrecioTotal().toPlainString()
                    + "€ por " + reserva.getMetodoPago().name().toLowerCase() + " en 3-5 días hábiles.";
        }
        String mensaje = "Tu reserva en " + reserva.getPista().getNombre() + " para el "
                + reserva.getFechaReserva() + " a las " + reserva.getHoraInicio()
                + " ha sido cancelada debido a que la pista ha sido bloqueada por el coordinador. "
                + reembolsoTexto;

        notificacionService.notificarIncidenciaReserva(reservaId, titulo, mensaje, true);
    }

    @Transactional
    public Reserva modificarReserva(Long reservaId, Long usuarioId,
                                    LocalDate nuevaFecha, LocalTime nuevaHoraInicio,
                                    Integer duracionMinutos) {
        Objects.requireNonNull(reservaId, "reservaId no puede ser null");
        Objects.requireNonNull(usuarioId, "usuarioId no puede ser null");
        Objects.requireNonNull(nuevaFecha, "nuevaFecha no puede ser null");
        Objects.requireNonNull(nuevaHoraInicio, "nuevaHoraInicio no puede ser null");

        int duracion = (duracionMinutos == null || duracionMinutos <= 0) ? 60 : duracionMinutos;

        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        if (!reserva.getUsuario().getId().equals(usuarioId)) {
            throw new SecurityException("No tienes permiso para modificar esta reserva.");
        }
        if (reserva.getEstado() == EstadoReserva.CANCELADA) {
            throw new IllegalStateException("No se puede modificar una reserva cancelada.");
        }
        if (!cumpleAntelacionMinima24h(reserva.getFechaReserva(), reserva.getHoraInicio())) {
            throw new IllegalStateException("Solo se puede modificar una reserva con mas de 24 horas de antelacion.");
        }

        nuevaHoraInicio = nuevaHoraInicio.withSecond(0).withNano(0);
        LocalTime nuevaHoraFin = nuevaHoraInicio.plusMinutes(duracion);

        if (!cumpleAntelacionMinima24h(nuevaFecha, nuevaHoraInicio)) {
            throw new IllegalStateException("El nuevo horario debe estar al menos a 24 horas vista.");
        }

        Pista pista = reserva.getPista();
        validarHorarioInstalacion(pista, nuevaHoraInicio, nuevaHoraFin);

        List<Reserva> conflictos = reservaRepository.findConflictingReservationsExcludingReserva(
                pista.getId(), nuevaFecha, nuevaHoraInicio, nuevaHoraFin, reservaId);
        if (!conflictos.isEmpty()) {
            throw new IllegalStateException("La pista ya está reservada en el nuevo horario seleccionado.");
        }

        BigDecimal precioAnterior = reserva.getPrecioTotal() != null ? reserva.getPrecioTotal() : BigDecimal.ZERO;
        BigDecimal nuevoPrecio = tarifaService.calcularPrecio(
                pista.getTipoDeporte(), nuevaFecha, nuevaHoraInicio, nuevaHoraFin,
                reserva.getUsuario().isEsSocio());
        nuevoPrecio = aplicarDescuentoAbonoSiExiste(usuarioId, nuevoPrecio);

        if (reserva.getEstado() == EstadoReserva.CONFIRMADA && reserva.getMetodoPago() == MetodoPago.BILLETERA) {
            ajustarBilleteraPorCambioDePrecio(reserva.getUsuario(), precioAnterior, nuevoPrecio);
        }

        reserva.setFechaReserva(nuevaFecha);
        reserva.setHoraInicio(nuevaHoraInicio);
        reserva.setHoraFin(nuevaHoraFin);
        reserva.setPrecioTotal(nuevoPrecio);

        return reservaRepository.save(reserva);
    }

    @Transactional
    public Reserva modificarReservaPorSecretaria(Long reservaId,
                                                LocalDate nuevaFecha, LocalTime nuevaHoraInicio,
                                                Integer duracionMinutos, Long nuevaPistaId) {
        Objects.requireNonNull(reservaId, "reservaId no puede ser null");
        Objects.requireNonNull(nuevaFecha, "nuevaFecha no puede ser null");
        Objects.requireNonNull(nuevaHoraInicio, "nuevaHoraInicio no puede ser null");

        int duracion = (duracionMinutos == null || duracionMinutos <= 0) ? 60 : duracionMinutos;

        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        if (reserva.getEstado() == EstadoReserva.CANCELADA) {
            throw new IllegalStateException("No se puede modificar una reserva cancelada.");
        }

        // Saltamos la comprobación de 24h porque es una acción de secretaría

        nuevaHoraInicio = nuevaHoraInicio.withSecond(0).withNano(0);
        LocalTime nuevaHoraFin = nuevaHoraInicio.plusMinutes(duracion);

        Pista pista = (nuevaPistaId != null)
                ? pistaRepository.findById(nuevaPistaId).orElseThrow(() -> new IllegalArgumentException("Pista no encontrada"))
                : reserva.getPista();

        validarHorarioInstalacion(pista, nuevaHoraInicio, nuevaHoraFin);

        List<Reserva> conflictos = reservaRepository.findConflictingReservationsExcludingReserva(
                pista.getId(), nuevaFecha, nuevaHoraInicio, nuevaHoraFin, reservaId);
        if (!conflictos.isEmpty()) {
            throw new IllegalStateException("La pista ya está reservada en el nuevo horario seleccionado.");
        }

        reserva.setPista(pista);
        reserva.setFechaReserva(nuevaFecha);
        reserva.setHoraInicio(nuevaHoraInicio);
        reserva.setHoraFin(nuevaHoraFin);

        Reserva reservaGuardada = reservaRepository.save(reserva);
        notificacionService.notificarModificacionReservaPorSecretaria(reservaGuardada);
        return reservaGuardada;
    }

    @Transactional(readOnly = true)
    public boolean cumpleAntelacionMinima24h(LocalDate fecha, LocalTime horaInicio) {
        LocalDateTime fechaHoraReserva = LocalDateTime.of(fecha, horaInicio);
        return fechaHoraReserva.isAfter(LocalDateTime.now().plusHours(ANTELACION_MINIMA_HORAS));
    }

    // ─── Disponibilidad 
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

    // precio estimado
    @Transactional(readOnly = true)
    public Map<String, Object> calcularPrecioEstimado(Long pistaId, Long usuarioId,
                                                      LocalDate fecha, LocalTime horaInicio,
                                                      Integer duracionMinutos) {
        Pista pista = pistaRepository.findById(pistaId)
                .orElseThrow(() -> new IllegalArgumentException("Pista no encontrada"));
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        LocalTime horaFin = horaInicio.plusMinutes(duracionMinutos);
        boolean esSocio = usuario.isEsSocio();

        BigDecimal precioSinDescuento = tarifaService.calcularPrecio(
                pista.getTipoDeporte(), fecha, horaInicio, horaFin, false);
        BigDecimal precioFinal = tarifaService.calcularPrecio(
                pista.getTipoDeporte(), fecha, horaInicio, horaFin, esSocio);

        // ---> LÓGICA NUEVA: APLICAR DESCUENTO DE ABONO AL ESTIMADO <---
        precioFinal = aplicarDescuentoAbonoSiExiste(usuarioId, precioFinal);

        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("precioOriginal", precioSinDescuento);
        resultado.put("precioFinal", precioFinal);
        resultado.put("esSocio", esSocio);
        resultado.put("billetera", usuario.getBilletera());
        resultado.put("saldoSuficiente", usuario.getBilletera().compareTo(precioFinal) >= 0);
        
        // Si el precio original y el final son distintos, hay descuento (por socio o por abono)
        if (precioSinDescuento.compareTo(precioFinal) > 0) {
             resultado.put("descuentoAplicado", true);
             resultado.put("importeDescuento", precioSinDescuento.subtract(precioFinal));
        } else {
             resultado.put("descuentoAplicado", false);
        }
        
        return resultado;
    }

    private BigDecimal aplicarDescuentoAbonoSiExiste(Long usuarioId, BigDecimal precioActual) {
        Optional<AbonoUsuario> abonoActivo = abonoUsuarioService.obtenerAbonoActivo(usuarioId);
        if (abonoActivo.isPresent()) {
            // CAMBIO AQUÍ: getPlan() en lugar de getTarifa().getPlanAbono()
            BigDecimal descuentoPorcentaje = abonoActivo.get().getPlan().getDescuentoPistasPorcentaje();
            if (descuentoPorcentaje != null && descuentoPorcentaje.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal factor = BigDecimal.ONE.subtract(descuentoPorcentaje.divide(new BigDecimal("100")));
                return precioActual.multiply(factor).setScale(2, RoundingMode.HALF_UP);
            }
        }
        return precioActual;
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
        boolean cumpleRegla24h = cumpleAntelacionMinima24h(r.getFechaReserva(), r.getHoraInicio());
        dto.setPuedeModificar(cumpleRegla24h && r.getEstado() != EstadoReserva.CANCELADA);
        dto.setPuedeCancelarConReembolso(cumpleRegla24h && r.getEstado() != EstadoReserva.CANCELADA);

        dto.setQrUrl(r.getQrUrl());

        // Información de descuento
        boolean esSocio = r.getUsuario().isEsSocio();
        dto.setDescuentoSocioAplicado(esSocio);
        if (esSocio && r.getPista() != null) {
            BigDecimal precioSinDescuento = tarifaService.calcularPrecio(
                    r.getPista().getTipoDeporte(), r.getFechaReserva(),
                    r.getHoraInicio(), r.getHoraFin(), false);
            dto.setPrecioOriginal(precioSinDescuento);
        } else {
            // Ponemos el precio original como el precio cobrado por defecto,
            // aunque idealmente calcularíamos el precio sin el Abono si se quisiera desglosar al 100%.
            dto.setPrecioOriginal(r.getPrecioTotal()); 
        }

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
        if (metodoPago == MetodoPago.BILLETERA) {
            return;
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

    private void ajustarBilleteraPorCambioDePrecio(Usuario usuario,
                                                    BigDecimal precioAnterior,
                                                    BigDecimal nuevoPrecio) {
        BigDecimal diferencia = nuevoPrecio.subtract(precioAnterior);
        if (diferencia.compareTo(BigDecimal.ZERO) > 0) {
            if (usuario.getBilletera().compareTo(diferencia) < 0) {
                throw new IllegalStateException(
                        "Saldo insuficiente para modificar la reserva. Necesitas "
                                + diferencia.toPlainString() + "€ adicionales.");
            }
            usuario.setBilletera(usuario.getBilletera().subtract(diferencia));
            usuarioRepository.save(usuario);
            return;
        }

        if (diferencia.compareTo(BigDecimal.ZERO) < 0) {
            BigDecimal reembolso = diferencia.abs();
            usuario.setBilletera(usuario.getBilletera().add(reembolso));
            usuarioRepository.save(usuario);
        }
    }
}