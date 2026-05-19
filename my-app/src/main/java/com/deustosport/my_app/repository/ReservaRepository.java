package com.deustosport.my_app.repository;

import com.deustosport.my_app.entity.Reserva;
import com.deustosport.my_app.enums.EstadoReserva;
import com.deustosport.my_app.enums.MetodoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByUsuarioId(Long usuarioId);

    List<Reserva> findByUsuarioIdOrderByFechaReservaDescHoraInicioDesc(Long usuarioId);

    List<Reserva> findAllByOrderByFechaReservaDescHoraInicioDesc();

    List<Reserva> findByFechaReservaOrderByHoraInicioAscIdAsc(LocalDate fechaReserva);

    long countByUsuarioId(Long usuarioId);

    List<Reserva> findByPistaId(Long pistaId);

    List<Reserva> findByEstado(EstadoReserva estado);

        long countByEstado(EstadoReserva estado);
    
        // Compatibility method used by tests (accepting estado as String)
        long countByEstado(String estado);

    long countByMetodoPago(MetodoPago metodoPago);

        @Query("SELECT COALESCE(SUM(r.precioTotal), 0) FROM Reserva r WHERE r.estado = :estado")
        java.math.BigDecimal sumPrecioTotalByEstado(@Param("estado") EstadoReserva estado);

        @Query("SELECT COALESCE(SUM(r.precioTotal), 0) FROM Reserva r WHERE r.fechaPago BETWEEN :inicio AND :fin")
        java.math.BigDecimal sumPrecioTotalByFechaPagoBetween(@Param("inicio") LocalDateTime inicio,
                                                             @Param("fin") LocalDateTime fin);

        @Query("SELECT COUNT(r) FROM Reserva r WHERE r.fechaPago BETWEEN :inicio AND :fin")
        long countByFechaPagoBetween(@Param("inicio") LocalDateTime inicio,
                                                                 @Param("fin") LocalDateTime fin);

        long countByFechaReservaBetween(LocalDate inicio, LocalDate fin);

    // Methods expected by tests
    @Query("SELECT COUNT(r) FROM Reserva r WHERE r.fechaPago BETWEEN :inicio AND :fin")
    long countReservasMesActual(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    @Query("SELECT COALESCE(SUM(r.precioTotal), 0) FROM Reserva r WHERE r.fechaPago BETWEEN :inicio AND :fin")
    java.math.BigDecimal sumIngresosMesActual(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    @Query("SELECT COUNT(r) FROM Reserva r WHERE r.fechaReserva = :fecha")
    long countReservasHoy(@Param("fecha") LocalDate fecha);

    @Query("SELECT COALESCE(SUM(r.precioTotal), 0) FROM Reserva r WHERE r.fechaReserva = :fecha")
    java.math.BigDecimal sumIngresosHoy(@Param("fecha") LocalDate fecha);

    @Query("SELECT r.pista.polideportivo.nombre, COUNT(r) FROM Reserva r GROUP BY r.pista.polideportivo.nombre")
    java.util.Map<String, Long> countReservasPorPolideportivo();

    @Query("SELECT r.fechaReserva, COUNT(r) FROM Reserva r WHERE r.fechaReserva BETWEEN :inicio AND :fin GROUP BY r.fechaReserva ORDER BY r.fechaReserva DESC")
    java.util.List<Object[]> countReservasPorDia(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);

        @Query(value = "SELECT COUNT(*) FROM reservas r WHERE MOD(EXTRACT(DOW FROM r.fecha_reserva) + 6, 7) + 1 = :diaSemana", nativeQuery = true)
        long countByDiaSemana(@Param("diaSemana") int diaSemana);

    @Query("SELECT r FROM Reserva r WHERE r.pista.id = :pistaId " +
           "AND r.fechaReserva = :fecha " +
           "AND r.estado != com.deustosport.my_app.enums.EstadoReserva.CANCELADA " +
           "AND NOT (r.horaFin <= :horaInicio OR r.horaInicio >= :horaFin)")
    List<Reserva> findConflictingReservations(
            @Param("pistaId") Long pistaId,
            @Param("fecha") LocalDate fecha,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin
    );

            @Query("SELECT r FROM Reserva r WHERE r.pista.id = :pistaId " +
               "AND r.fechaReserva = :fecha " +
               "AND r.id <> :reservaId " +
               "AND r.estado != com.deustosport.my_app.enums.EstadoReserva.CANCELADA " +
               "AND NOT (r.horaFin <= :horaInicio OR r.horaInicio >= :horaFin)")
            List<Reserva> findConflictingReservationsExcludingReserva(
                @Param("pistaId") Long pistaId,
                @Param("fecha") LocalDate fecha,
                @Param("horaInicio") LocalTime horaInicio,
                @Param("horaFin") LocalTime horaFin,
                @Param("reservaId") Long reservaId
            );

    List<Reserva> findByFechaReservaAndEstado(LocalDate fecha, EstadoReserva estado);

    @Query("SELECT r FROM Reserva r WHERE r.pista.id = :pistaId " +
           "AND r.fechaReserva BETWEEN :inicio AND :fin " +
           "AND r.estado != com.deustosport.my_app.enums.EstadoReserva.CANCELADA")
    List<Reserva> findActivasByPistaAndRango(
            @Param("pistaId") Long pistaId,
            @Param("inicio") LocalDate inicio,
            @Param("fin") LocalDate fin
    );

    @Query("SELECT COUNT(r) FROM Reserva r WHERE r.pista.id IN :pistaIds " +
           "AND r.fechaReserva >= :hoy " +
           "AND r.estado != com.deustosport.my_app.enums.EstadoReserva.CANCELADA")
    long countActivasFuturasByPistaIds(
            @Param("pistaIds") List<Long> pistaIds,
            @Param("hoy") LocalDate hoy
    );

    @Query("SELECT r.pista.tipoDeporte, COUNT(r) FROM Reserva r " +
           "WHERE r.estado != com.deustosport.my_app.enums.EstadoReserva.CANCELADA " +
           "GROUP BY r.pista.tipoDeporte ORDER BY COUNT(r) DESC")
    List<Object[]> countReservasPorTipoDeporte();

    @Query("SELECT r.usuario.id, r.usuario.nombreCompleto, COUNT(r) as total FROM Reserva r " +
           "WHERE r.estado != com.deustosport.my_app.enums.EstadoReserva.CANCELADA " +
           "GROUP BY r.usuario.id, r.usuario.nombreCompleto ORDER BY total DESC")
    List<Object[]> findTopUsuariosPorReservas(org.springframework.data.domain.Pageable pageable);

    @Query("SELECT r.pista.id, r.pista.nombre, r.pista.tipoDeporte, COUNT(r) FROM Reserva r " +
           "WHERE r.estado != com.deustosport.my_app.enums.EstadoReserva.CANCELADA " +
           "GROUP BY r.pista.id, r.pista.nombre, r.pista.tipoDeporte ORDER BY COUNT(r) DESC")
    List<Object[]> countReservasPorPista();
}