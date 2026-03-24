package com.deustosport.my_app.service;

import com.deustosport.my_app.entity.Pista;
import com.deustosport.my_app.entity.Reserva;
import com.deustosport.my_app.entity.Usuario;
import com.deustosport.my_app.enums.EstadoReserva;
import com.deustosport.my_app.repository.PistaRepository;
import com.deustosport.my_app.repository.ReservaRepository;
import com.deustosport.my_app.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final PistaRepository pistaRepository;
    private final UsuarioRepository usuarioRepository;
    private final TarifaService tarifaService;

    public ReservaService(ReservaRepository reservaRepository,
            PistaRepository pistaRepository,
            UsuarioRepository usuarioRepository,
            TarifaService tarifaService) {
        this.reservaRepository = reservaRepository;
        this.pistaRepository = pistaRepository;
        this.usuarioRepository = usuarioRepository;
        this.tarifaService = tarifaService;
    }

    @Transactional
    public Reserva crearReserva(Long usuarioId, Long pistaId, LocalDate fecha, LocalTime horaInicio, Integer duracionMinutos) {
        if (fecha.isBefore(LocalDate.now()) || (fecha.isEqual(LocalDate.now()) && horaInicio.isBefore(LocalTime.now()))) {
            throw new IllegalArgumentException("No se pueden hacer reservas en fechas u horas pasadas.");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + usuarioId));

        Pista pista = pistaRepository.findById(pistaId)
                .orElseThrow(() -> new IllegalArgumentException("Pista no encontrada con ID: " + pistaId));

        if (!pista.isActiva()) {
            throw new IllegalStateException("La pista seleccionada no está disponible para reservas.");
        }

        horaInicio = horaInicio.withSecond(0).withNano(0);
        LocalTime horaFin = horaInicio.plusMinutes(duracionMinutos);

        List<Reserva> conflictos = reservaRepository.findConflictingReservations(pistaId, fecha, horaInicio, horaFin);
        if (!conflictos.isEmpty()) {
            throw new IllegalStateException("La pista ya está reservada en el horario seleccionado.");
        }

        BigDecimal precioTotal = tarifaService.calcularPrecio(pista.getTipoDeporte(), fecha, horaInicio, horaFin, usuario.isEsSocio());

        Reserva nuevaReserva = new Reserva();
        nuevaReserva.setUsuario(usuario);
        nuevaReserva.setPista(pista);
        nuevaReserva.setFechaReserva(fecha);
        nuevaReserva.setHoraInicio(horaInicio);
        nuevaReserva.setHoraFin(horaFin);
        nuevaReserva.setPrecioTotal(precioTotal);
        nuevaReserva.setEstado(EstadoReserva.CONFIRMADA);
        nuevaReserva.setCreditosUsados(0);

        return reservaRepository.save(nuevaReserva);
    }

    public List<Reserva> obtenerMisReservas(Long usuarioId) {
        return reservaRepository.findByUsuarioId(usuarioId);
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
}