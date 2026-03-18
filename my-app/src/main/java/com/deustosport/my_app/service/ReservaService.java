package com.deustosport.my_app.service;

import com.deustosport.my_app.entity.Pista;
import com.deustosport.my_app.entity.Reserva;
import com.deustosport.my_app.entity.Usuario;
import com.deustosport.my_app.enums.EstadoReserva;
import com.deustosport.my_app.repository.PistaRepository;
import com.deustosport.my_app.repository.ReservaRepository;
import com.deustosport.my_app.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public ReservaService(ReservaRepository reservaRepository, PistaRepository pistaRepository, UsuarioRepository usuarioRepository) {
        this.reservaRepository = reservaRepository;
        this.pistaRepository = pistaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Crea una nueva reserva verificando disponibilidad y estado de la pista.
     */
    @Transactional
    public Reserva crearReserva(Long usuarioId, Long pistaId, LocalDate fecha, LocalTime horaInicio, Integer duracionMinutos) {
        // 1. Validar que el usuario existe
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + usuarioId));

        // 2. Validar que la pista existe y está activa (usando método JPA estándar)
        Pista pista = pistaRepository.findById(pistaId)
                .orElseThrow(() -> new IllegalArgumentException("Pista no encontrada con ID: " + pistaId));

        if (!pista.isActiva()) {
            throw new IllegalStateException("La pista seleccionada no está disponible para reservas.");
        }

        if (duracionMinutos == null || duracionMinutos <= 0) {
            throw new IllegalArgumentException("La duración de la reserva debe ser mayor que 0 minutos.");
        }

        // 3. Calcular hora de fin
        LocalTime horaFin = horaInicio.plusMinutes(duracionMinutos);

        validarHorarioInstalacion(pista, horaInicio, horaFin);

        // 4. Verificar conflictos de horario usando la query personalizada del repositorio
        // La query findConflictingReservations ya excluye las reservas canceladas
        List<Reserva> conflictos = reservaRepository.findConflictingReservations(pistaId, fecha, horaInicio, horaFin);
        
        if (!conflictos.isEmpty()) {
            throw new IllegalStateException("La pista ya está reservada en el horario seleccionado.");
        }

        // 5. Calcular precio (Lógica base: 10.00 por hora)
        // Esto podría refinares obteniendo el precio de la pista si existiera ese campo
        BigDecimal precioBasePorHora = new BigDecimal("10.00");
        BigDecimal duracionEnHoras = new BigDecimal(duracionMinutos).divide(new BigDecimal(60), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal precioTotal = precioBasePorHora.multiply(duracionEnHoras);

        // 6. Crear y guardar la reserva
        Reserva nuevaReserva = new Reserva();
        nuevaReserva.setUsuario(usuario);
        nuevaReserva.setPista(pista);
        nuevaReserva.setFechaReserva(fecha);
        nuevaReserva.setHoraInicio(horaInicio);
        nuevaReserva.setHoraFin(horaFin);
        nuevaReserva.setPrecioTotal(precioTotal);
        nuevaReserva.setEstado(EstadoReserva.CONFIRMADA); // Asumimos confirmación inmediata por defecto
        nuevaReserva.setCreditosUsados(0); // Por defecto 0 si no usa bono

        return reservaRepository.save(nuevaReserva);
    }

    /**
     * Obtiene el historial de reservas de un usuario específico.
     */
    public List<Reserva> obtenerMisReservas(Long usuarioId) {
        return reservaRepository.findByUsuarioId(usuarioId);
    }

    /**
     * Cancela una reserva si pertenece al usuario y cumple las políticas.
     */
    @Transactional
    public Reserva cancelarReserva(Long reservaId, Long usuarioId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        // Verificar pertenencia
        if (!reserva.getUsuario().getId().equals(usuarioId)) {
            throw new SecurityException("No tienes permiso para cancelar esta reserva.");
        }

        // Verificar estado actual
        if (reserva.getEstado() == EstadoReserva.CANCELADA) {
            throw new IllegalStateException("La reserva ya está cancelada.");
        }

        // Validar política de cancelación (Ej: no permitir cancelar reservas pasadas)
        if (reserva.getFechaReserva().isBefore(LocalDate.now()) ||
           (reserva.getFechaReserva().isEqual(LocalDate.now()) && reserva.getHoraInicio().isBefore(LocalTime.now()))) {
            throw new IllegalStateException("No se pueden cancelar reservas pasadas.");
        }

        reserva.setEstado(EstadoReserva.CANCELADA);
        return reservaRepository.save(reserva);
    }

    /**
     * Método auxiliar para consultar disponibilidad sin reservar
     */
    public boolean consultarDisponibilidad(Long pistaId, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        Pista pista = pistaRepository.findById(pistaId)
                .orElseThrow(() -> new IllegalArgumentException("Pista no encontrada con ID: " + pistaId));

        validarHorarioInstalacion(pista, horaInicio, horaFin);

        List<Reserva> conflictos = reservaRepository.findConflictingReservations(pistaId, fecha, horaInicio, horaFin);
        return conflictos.isEmpty();
    }

    private void validarHorarioInstalacion(Pista pista, LocalTime horaInicio, LocalTime horaFin) {
        if (horaInicio == null || horaFin == null || !horaFin.isAfter(horaInicio)) {
            throw new IllegalArgumentException("El rango horario de la reserva no es válido.");
        }

        if (pista.getInstalacion() == null) {
            throw new IllegalStateException("La pista no tiene una instalación asociada.");
        }

        LocalTime horaApertura = pista.getInstalacion().getHoraApertura();
        LocalTime horaCierre = pista.getInstalacion().getHoraCierre();

        if (horaApertura == null || horaCierre == null || !horaCierre.isAfter(horaApertura)) {
            throw new IllegalStateException("La instalación no tiene un horario general válido configurado.");
        }

        if (horaInicio.isBefore(horaApertura) || horaFin.isAfter(horaCierre)) {
            throw new IllegalStateException(
                    "La reserva debe estar dentro del horario general del polideportivo: "
                            + horaApertura + " - " + horaCierre + ".");
        }
    }
}