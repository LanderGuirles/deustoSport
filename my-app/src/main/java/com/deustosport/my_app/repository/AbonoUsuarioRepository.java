package com.deustosport.my_app.repository;

import com.deustosport.my_app.entity.AbonoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AbonoUsuarioRepository extends JpaRepository<AbonoUsuario, Long> {

    // Comprueba si un usuario tiene un abono activo, ya sea como titular o como beneficiario
    @Query("SELECT DISTINCT a FROM AbonoUsuario a LEFT JOIN a.beneficiarios b " +
           "WHERE (a.titular.id = :usuarioId OR b.id = :usuarioId) " +
           "AND a.activo = true AND a.fechaFin >= :fechaActual")
    Optional<AbonoUsuario> findAbonoActivoByUsuarioId(@Param("usuarioId") Long usuarioId, @Param("fechaActual") LocalDate fechaActual);

    List<AbonoUsuario> findByActivoTrueAndFechaFinBefore(LocalDate fecha);
}