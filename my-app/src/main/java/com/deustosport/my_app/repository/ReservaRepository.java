package com.deustosport.my_app.repository;

import com.deustosport.my_app.entity.Reserva;
import com.deustosport.my_app.enums.EstadoReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByUsuarioId(Long usuarioId);

    List<Reserva> findByUsuarioIdOrderByFechaReservaDescHoraInicioDesc(Long usuarioId);

    long countByUsuarioId(Long usuarioId);

    List<Reserva> findByPistaId(Long pistaId);

    List<Reserva> findByEstado(EstadoReserva estado);

    @Query("SELECT r FROM Reserva r WHERE r.pista.id = :pistaId " +
           "AND r.fechaReserva = :fecha " +
           "AND r.estado != 'CANCELADA' " +
           "AND NOT (r.horaFin <= :horaInicio OR r.horaInicio >= :horaFin)")
    List<Reserva> findConflictingReservations(
            @Param("pistaId") Long pistaId,
            @Param("fecha") LocalDate fecha,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin
    );

    List<Reserva> findByFechaReservaAndEstado(LocalDate fecha, EstadoReserva estado);
}
