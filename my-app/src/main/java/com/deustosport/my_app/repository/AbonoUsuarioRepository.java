package com.deustosport.my_app.repository;

import com.deustosport.my_app.entity.AbonoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AbonoUsuarioRepository extends JpaRepository<AbonoUsuario, Long> {

    List<AbonoUsuario> findByUsuarioIdOrderByFechaInicioDesc(Long usuarioId);

    List<AbonoUsuario> findByActivoTrueAndFechaFinBefore(LocalDate fecha);

    @Query("SELECT a FROM AbonoUsuario a WHERE a.usuario.id = :usuarioId AND a.activo = true AND a.fechaInicio <= :fechaActual AND a.fechaFin >= :fechaActual")
    Optional<AbonoUsuario> findAbonoActivoByUsuarioId(
            @Param("usuarioId") Long usuarioId, 
            @Param("fechaActual") LocalDate fechaActual
    );
}